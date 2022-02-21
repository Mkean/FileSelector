package com.thirteen.fileselector

import android.Manifest
import android.content.pm.PackageManager
import android.os.*
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.collection.ArrayMap
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.thirteen.fileselector.adapter.BaseAdapter
import com.thirteen.fileselector.adapter.FileListAdapter
import com.thirteen.fileselector.adapter.FileNavAdapter
import com.thirteen.fileselector.adapter.RecyclerViewListener
import com.thirteen.fileselector.bean.FileBean
import com.thirteen.fileselector.bean.FileItemBeanImpl
import com.thirteen.fileselector.bean.FileNavBeanImpl
import com.thirteen.fileselector.config.FilePickerManager
import com.thirteen.fileselector.utils.FileUtils
import com.thirteen.fileselector.utils.dp
import com.thirteen.fileselector.widget.PosLinearLayoutManager
import com.thirteen.fileselector.widget.RecyclerViewFilePicker
import java.io.File
import java.util.concurrent.*

class FilePickerActivity : AppCompatActivity(), View.OnClickListener,
    RecyclerViewListener.OnItemClickListener {

    private var rvList: RecyclerViewFilePicker? = null
    private var rvNav: RecyclerView? = null
    private var tvToolbarTitle: TextView? = null
    private var btnConfirm: Button? = null
    private var btnSelectedAll: Button? = null
    private var btnGoBack: ImageView? = null
    private var mainHandler = Handler(Looper.getMainLooper())

    // Creates a thread pool manager
    private val loadingThreadPool: ExecutorService =
        FilePickerManager.config.threadPool ?: ThreadPoolExecutor(
            1,      // Initial pool size
            1,      // Max pool size
            KEEP_ALIVE_TIME,
            TimeUnit.MINUTES,
            LinkedBlockingDeque()
        )

    private val loadFileRunnable: Runnable by lazy {
        Runnable {
            val customRootPathFile = pickerConfig.customRootPathFile
            val rootFile = when {
                customRootPathFile?.exists() == true -> {
                    // move to custom root dir
                    navDataSource.clear()
                    val root = FileUtils.getRootFile()
                    var curPath = customRootPathFile.absolutePath
                    while (curPath != root.parent && !curPath.isNullOrBlank()) {
                        Log.i("loadFileRunnable", "curPath = $curPath")
                        val f = File(curPath)
                        val fileNavBeanImpl = FileNavBeanImpl(
                            FileUtils.getDirAlias(f),
                            f.absolutePath
                        )
                        navDataSource.add(0, fileNavBeanImpl)
                        curPath = f.parent
                    }
                    pickerConfig.resetCustomFile()
                    customRootPathFile
                }
                navDataSource.isEmpty() && pickerConfig.isSkipDir -> {
                    FileUtils.getRootFile()
                }
                navDataSource.isEmpty() && !pickerConfig.isSkipDir -> {
                    // 如果是文件夹作为可选项时，需要让根目录也作为 item 被点击，故而取根目录上级作为 rootFiles
                    FileUtils.getRootFile().parentFile
                }
                else -> {
                    File(navDataSource.last().dirPath)
                }
            }

            val listData = FileUtils.produceListDataSource(rootFile)

            // 导航栏数据集
            navDataSource = FileUtils.produceNavDataSource(
                navDataSource,
                if (navDataSource.isEmpty()) {
                    rootFile.path
                } else {
                    navDataSource.last().dirPath
                }
            )
            mainHandler.post {
                initRv(listData, navDataSource)
            }

        }
    }

    // 权限请求
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            onRequestResult(granted)
        }

    /**
     * 文件列表适配器
     */
    private val listAdapter: FileListAdapter by lazy {
        FileListAdapter(this,
            FilePickerManager.config.singleChoice)
            .apply {
                addListener {
                    onCheckSizeChanged {
                        updateItemUI()
                    }
                }
            }
    }


    /**
     * 导航栏列表适配器
     */
    private val navAdapter: FileNavAdapter by lazy {
        FileNavAdapter(this)
    }

    /**
     * 导航栏数据集
     */
    private var navDataSource = ArrayList<FileNavBeanImpl>()

    /**
     * 文件夹为空时展示的空视图
     */
    private val selectedCount
        get() = listAdapter.checkedCount
    private val maxSelectable = FilePickerManager.config.maxSelectable
    private val pickerConfig by lazy { FilePickerManager.config }
    private var fileListListener: RecyclerViewListener? = null
        get() {
            if (field == null) {
                field = getListener(rvList)
            }
            return field
        }
    private var navListener: RecyclerViewListener? = null
        get() {
            if (field == null) {
                field = getListener(rvNav)
            }
            return field
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_picker)

        initView()

        // 检验权限
        if (isPermissionGrated()) {
            loadList()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "")
        val shouldShutDownThreadPool = pickerConfig.threadPool != loadingThreadPool
                || pickerConfig.threadPoolAutoShutDown
        if (!loadingThreadPool.isShutdown && shouldShutDownThreadPool) {
            Log.i(TAG, "shutdown thread pool")
            loadingThreadPool.shutdown()
        }
        currOffsetMap.clear()
        currPosMap.clear()
    }

    private fun isPermissionGrated() = ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.READ_EXTERNAL_STORAGE
    ) == PackageManager.PERMISSION_GRANTED

    /**
     * 申请权限结果返回
     */
    private fun onRequestResult(granted: Boolean) {
        if (granted) {
            loadList()
        } else {
            Toast.makeText(this, getString(R.string.file_picker_request_permission_failed),
                Toast.LENGTH_SHORT).show()
        }
    }

    fun initView() {
        btnGoBack = findViewById(R.id.btn_go_back_file_picker)
        btnGoBack!!.setOnClickListener(this)
        btnSelectedAll = findViewById(R.id.btn_selected_all_file_picker)
        btnSelectedAll?.apply {
            // 单选模式时隐藏并且不初始化
            if (pickerConfig.singleChoice) {
                visibility = View.GONE
                return@apply
            }
            setOnClickListener(this@FilePickerActivity)
            FilePickerManager.config.selectAllText.let {
                text = it
            }
        }
        btnConfirm = findViewById(R.id.btn_confirm_file_picker)
        btnConfirm?.apply {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
                // 小于 4.4 的样式兼容
                // compatible with 4.4 api
                layoutParams = RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
                    addRule(RelativeLayout.CENTER_VERTICAL)
                    setMargins(0, 0, 16.dp, 0)
                }
            }
            setOnClickListener(this@FilePickerActivity)
            FilePickerManager.config.confirmText.let {
                text = it
            }
        }

        tvToolbarTitle = findViewById(R.id.tv_toolbar_title_file_picker)
        tvToolbarTitle?.visibility = if (pickerConfig.singleChoice) {
            View.GONE
        } else {
            View.VISIBLE
        }

        rvNav = findViewById<RecyclerView>(R.id.rv_nav_file_picker).apply {
            layoutManager =
                LinearLayoutManager(
                    this@FilePickerActivity,
                    LinearLayoutManager.HORIZONTAL, false
                )
            adapter = navAdapter
        }

        rvList = findViewById<RecyclerViewFilePicker>(R.id.rv_list_file_picker).apply {
            setHasFixedSize(true)
            adapter = listAdapter
            layoutAnimation =
                AnimationUtils.loadLayoutAnimation(context, R.anim.layout_item_anim_file_picker)
            layoutManager = PosLinearLayoutManager(this@FilePickerActivity)
            if (!hasEmptyView()) {
                emptyView = LayoutInflater.from(context)
                    .inflate(R.layout.empty_file_list_file_picker, this, false).apply {
                        this.findViewById<TextView>(R.id.tv_empty_list).text =
                            pickerConfig.emptyListTips
                    }
            }
        }
    }


    private fun loadList() {
        if (!isPermissionGrated()) {
            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            return
        }
        if (Environment.getExternalStorageState() != Environment.MEDIA_MOUNTED) {
            Log.e(TAG,
                "Enternal storage is not avilanle =====> " +
                        "Environment.getEnternalStorageState() != MEDIA_MOUNTED")
            return
        }

        try {
            Log.i(TAG, "loadList in ${Thread.currentThread()} in $loadingThreadPool")
            loadingThreadPool.submit(loadFileRunnable)
        } catch (e: RejectedExecutionException) {
            Log.e(TAG, "submit job failed")
        }
    }

    private fun initRv(
        listData: ArrayList<FileItemBeanImpl>,
        navDataList: ArrayList<FileNavBeanImpl>,
    ) {
        switchButton(true)
        // 导航栏适配器
        navAdapter.setNewData(navDataList)
        rvNav?.apply {
            navListener?.let { removeOnItemTouchListener(it) }
            navListener?.let { addOnItemTouchListener(it) }
        }

        // 列表适配器
        listAdapter.apply {
            isSingleChoice = FilePickerManager.config.singleChoice
            setNewData(listData)
        }
        rvList?.apply {
            fileListListener?.let { removeOnItemTouchListener(it) }
            fileListListener?.let { addOnItemTouchListener(it) }
        }
    }

    /**
     * 获取两个列表的监听器
     */
    private fun getListener(recyclerView: RecyclerView?): RecyclerViewListener? {
        if (recyclerView == null) {
            return null
        }
        return RecyclerViewListener(recyclerView, this)
    }

    private val currPosMap: ArrayMap<String, Int> by lazy {
        ArrayMap(4)
    }

    private val currOffsetMap: ArrayMap<String, Int> by lazy {
        ArrayMap(4)
    }

    private fun saveCurrPos(item: FileNavBeanImpl?, position: Int) {
        item?.run {
            currPosMap[filePath] = position
            (rvList?.layoutManager as? LinearLayoutManager)?.let {
                currOffsetMap.put(filePath, it.findViewByPosition(position)?.top ?: 0)
            }
        }
    }

    /* ---------------------------- Item click listener start ---------------------------- */

    /**
     * 传递 item 点击事件
     */
    override fun onItemClick(
        adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>,
        view: View,
        position: Int,
    ) {
        val item = (adapter as BaseAdapter).getItem(position)
        item ?: return
        val file = File(item.filePath)
        if (!file.exists()) return
        when (view.id) {
            R.id.item_list_file_picker -> {
                // Check the lib users whether if intercept the click event.
                val hookItemClick = FilePickerManager.config.itemClickListener?.onItemClick(
                    adapter as FileListAdapter,
                    view,
                    position
                ) == true
                if (hookItemClick) return
                if (file.isDirectory) {
                    (rvNav?.adapter as? FileNavAdapter)?.let {
                        saveCurrPos(it.dataList.last(), position)
                    }
                    // 如果是文件夹，则进入
                    enterDirAndUpdateUI(item)
                } else {

                }
            }

            R.id.item_nav_file_picker -> {
                if (file.isDirectory) {
                    (rvNav?.adapter as? FileNavAdapter)?.let {
                        saveCurrPos(it.dataList.last(), position)
                    }
                    // 如果是文件夹，则进入
                    enterDirAndUpdateUI(item)
                }
            }
        }
    }

    /**
     * 子控件点击事件
     */
    override fun onItemChildClick(
        adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>,
        view: View,
        position: Int,
    ) {
        when (view.id) {
            R.id.tv_btn_nav_file_picker -> {
                val item = (adapter as FileNavAdapter).getItem(position)
                item ?: return
                enterDirAndUpdateUI(item)
            }
            else -> {
                val item = (adapter as FileListAdapter).getItem(position) ?: return
                // Check the lib users whether if intercept the click event.
                val hookItemClick = FilePickerManager.config.itemClickListener?.onItemChildClick(
                    adapter, view, position
                ) == true
                if (hookItemClick) return

                // 文件夹直接进入
                if (item.isDir && pickerConfig.isSkipDir) {
                    enterDirAndUpdateUI(item)
                    return
                }
                if (pickerConfig.singleChoice) {
                    listAdapter.singleCheck(position)
                } else {
                    listAdapter.multipleCheckOrNo(item, position, ::isCanSelect) {
                        Toast.makeText(this,
                            getString(pickerConfig.maxSelectCountTips, maxSelectable),
                            Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    /**
     * 条目被长按
     */
    override fun onItemLongClick(
        adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>,
        view: View,
        position: Int,
    ) {
        if (view.id != R.id.item_list_file_picker) return
        val item = (adapter as FileListAdapter).getItem(position) ?: return
        // Check the lib users whether if intercept the click event.
        val hookItemClick = FilePickerManager.config.itemClickListener?.onItemLongClick(
            adapter, view, position
        ) == true
        if (hookItemClick) return

        val file = File(item.filePath)
        val isSkipDir = FilePickerManager.config.isSkipDir
        // current item is directory and should skip directory, because long click would make the item been selected.
        if (file.exists() && file.isDirectory && isSkipDir) return
        // same action like child click
        if (item.isDir && pickerConfig.isSkipDir) {
            enterDirAndUpdateUI(item)
            return
        }
        if (pickerConfig.singleChoice) {
            listAdapter.singleCheck(position)
        } else {
            listAdapter.multipleCheckOrNo(item, position, ::isCanSelect) {
                Toast.makeText(
                    this@FilePickerActivity.applicationContext,
                    getString(pickerConfig.maxSelectCountTips, maxSelectable),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    }

    /* ---------------------------- Item click listener end ---------------------------- */

    /**
     * 从导航栏中调用本方法，需要传入 pos，以便产生新的 nav adapter
     */
    private fun enterDirAndUpdateUI(fileBean: FileBean) {
        // 清除当前选中状态
        resetViewState()
        // 获取文件夹文件
        val nextFiles = File(fileBean.filePath)
        // 更新列表数据集
        listAdapter.setNewData(FileUtils.produceListDataSource(nextFiles))
        // 更新导航栏数据集
        navDataSource = FileUtils.produceNavDataSource(
            ArrayList(navAdapter.dataList),
            fileBean.filePath)
        navAdapter.setNewData(navDataSource)
        rvNav?.adapter?.itemCount?.let {
            rvNav?.smoothScrollToPosition(
                if (it == 0) {
                    0
                } else {
                    it - 1
                }
            )
        }
        notifyDataChangedForList(fileBean)
    }

    private fun notifyDataChangedForList(fileBean: FileBean) {
        rvList?.apply {
            (layoutManager as? PosLinearLayoutManager)?.setTargetPos(
                currPosMap[fileBean.filePath] ?: 0,
                currOffsetMap[fileBean.filePath] ?: 0
            )
            scheduleLayoutAnimation()
        }
    }

    private fun switchButton(isEnable: Boolean) {
        btnConfirm?.isEnabled = isEnable
        btnSelectedAll?.isEnabled = isEnable
    }

    private fun resetViewState() {
        listAdapter.resetCheck()
        updateItemUI()
    }

    private fun updateItemUI() {
        if (pickerConfig.singleChoice) {
            return
        }
        // 取消选中，并且选中数为 0
        if (selectedCount == 0) {
            btnSelectedAll?.text = pickerConfig.selectAllText
            tvToolbarTitle?.text = ""
            return
        }
        btnSelectedAll?.text = pickerConfig.deSelectAllText
        tvToolbarTitle?.text =
            resources.getString(pickerConfig.hadSelectedText, selectedCount)
    }

    override fun onBackPressed() {
        if ((rvNav?.adapter as? FileNavAdapter)?.itemCount ?: 0 <= 1) {
            super.onBackPressed()
        } else {
            // 即将进入的 item 索引
            (rvNav?.adapter as? FileNavAdapter)?.run {
                enterDirAndUpdateUI(getItem(this.itemCount - 2)!!)
            }
        }
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            // 全选
            R.id.btn_selected_all_file_picker -> {
                // 只要当前选中项数量大于 0，那么本按钮则为取消全选按钮
                if (selectedCount > 0) {
                    listAdapter.disCheckAll()
                } else if (isCanSelect()) {
                    // 当前选中数小于最大选中数，则即将执行选中
                    listAdapter.checkAll()
                }
            }

            // 确认按钮
            R.id.btn_confirm_file_picker -> {
                if (listAdapter.dataList.isNullOrEmpty())
                    return
                val list = ArrayList<FileItemBeanImpl>()

                for (data in listAdapter.dataList) {
                    if (data.isChecked()) {
                        list.add(data)
                    }
                }

                pickerConfig.requestCallback?.onResult(list)

                finish()

            }

            R.id.btn_go_back_file_picker -> {
                onBackPressed()
            }
        }
    }


    private fun isCanSelect() = selectedCount < maxSelectable

    companion object {
        private const val TAG = "FilePickerActivity"

        // Sets the amount of time an idle thread waits before terminating
        private const val KEEP_ALIVE_TIME = 10L
    }
}