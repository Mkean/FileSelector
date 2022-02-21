package com.thirteen.fileselector.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.collection.ArraySet
import androidx.recyclerview.widget.RecyclerView
import com.thirteen.fileselector.FilePickerActivity
import com.thirteen.fileselector.R
import com.thirteen.fileselector.bean.FileBean
import com.thirteen.fileselector.bean.FileItemBeanImpl
import com.thirteen.fileselector.config.FilePickerManager.config
import com.thirteen.fileselector.engine.ImageLoadController
import com.thirteen.fileselector.filetype.RasterImageFileType
import com.thirteen.fileselector.filetype.VideoFileType
import com.thirteen.fileselector.utils.FileListAdapterListener
import com.thirteen.fileselector.utils.FileListAdapterListenerBuilder
import com.thirteen.fileselector.utils.formatFileSize
import com.thirteen.fileselector.utils.getTimestampToDate

/**
 * @Author:
 * @Description:
 * 文件列表适配器类
 */
class FileListAdapter(
    private val context: FilePickerActivity,
    var isSingleChoice: Boolean = config.singleChoice,
) : BaseAdapter() {

    val dataList: ArrayList<FileItemBeanImpl> = ArrayList(10)
    private var latestChoicePos = -1
    private lateinit var recyclerView: RecyclerView

    private var listener: FileListAdapterListener? = null

    fun addListener(block: FileListAdapterListenerBuilder.() -> Unit) {
        this.listener = FileListAdapterListenerBuilder().also(block)
    }

    private val checkedSet: ArraySet<FileBean> by lazy {
        ArraySet(20)
    }

    val checkedCount: Int
        get() = checkedSet.count()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (parent is RecyclerView) {
            recyclerView = parent
        }
        return FileListItemHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_list_file_picker,
                parent,
                false
            )
        )
    }

    override fun getItemView(position: Int): View? {
        return recyclerView.findViewHolderForAdapterPosition(position)?.itemView
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun getItemViewType(position: Int): Int {
        return DEFAULT_FILE_TYPE
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as BaseViewHolder).bind(dataList[position], position)
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: MutableList<Any>,
    ) {
        // Using payload to refresh partly
        // 使用 payload 进行局部刷新
        if (payloads.isNullOrEmpty()) {
            onBindViewHolder(holder, position)
            return
        }
        (holder as FileListItemHolder).check(getItem(position)?.isChecked() ?: false)
    }

    override fun getItem(position: Int): FileItemBeanImpl? {
        if (position >= 0 &&
            position < dataList.size &&
            getItemViewType(position) == DEFAULT_FILE_TYPE
        ) return dataList[position]
        return null
    }

    /*--------------------------ViewHolder Begin------------------------------*/

    abstract inner class BaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun bind(itemImpl: FileItemBeanImpl, position: Int)
    }


    inner class FileListItemHolder(itemView: View) :
        BaseViewHolder(itemView) {

        private val isSkipDir: Boolean = config.isSkipDir
        private val tvFileName = itemView.findViewById<TextView>(R.id.tv_list_file_picker)!!
        private val tvFileSize = itemView.findViewById<TextView>(R.id.tv_size_list_file_picker)!!
        private val tvUpdateTime =
            itemView.findViewById<TextView>(R.id.tv_updatetime_list_file_picker)!!
        private val checkBox = itemView.findViewById<CheckBox>(R.id.cb_list_file_picker)!!
        private val ivIcon = itemView.findViewById<ImageView>(R.id.iv_icon_list_file_picker)!!
        private val radioButton = itemView.findViewById<RadioButton>(R.id.rb_list_file_picker)!!

        fun check(isCheck: Boolean) {
            if (config.singleChoice) {
                radioButton.isChecked = isCheck
            } else {
                checkBox.isChecked = isCheck
            }
        }

        private fun onCheck(
            itemImpl: FileItemBeanImpl,
            buttonView: CompoundButton,
            isChecked: Boolean,
            position: Int,
        ) {
            if (isChecked) {
                checkedSet.add(itemImpl)
            } else {
                checkedSet.remove(itemImpl)
            }
            itemImpl.setChecked(isChecked)
            listener?.onCheckSizeChanged(checkedCount)
//            listener?.onCheck(isChecked, buttonView, position)
        }

        @SuppressLint("SetTextI18n")
        override fun bind(itemImpl: FileItemBeanImpl, position: Int) {
            tvFileName.text = itemImpl.fileName
            tvUpdateTime.text =
                "更新时间：${getTimestampToDate("yyyy-MM-dd HH:mm:ss", itemImpl.fileUpdateTime)}"

            checkBox.apply {
                tag = itemImpl
                visibility = when {
                    (isSkipDir && itemImpl.isDir) || config.singleChoice -> {
                        View.GONE
                    }
                    else -> {
                        View.VISIBLE
                    }
                }
                setOnCheckedChangeListener { buttonView, isChecked ->
                    if (tag != itemImpl) return@setOnCheckedChangeListener
                    onCheck(itemImpl, buttonView, isChecked, position)
                }
                isChecked = itemImpl.isChecked()
            }

            radioButton.apply {
                tag = itemImpl
                visibility = when {
                    (isSkipDir && itemImpl.isDir) || !config.singleChoice -> {
                        View.GONE
                    }
                    else -> {
                        View.VISIBLE
                    }
                }
                setOnCheckedChangeListener { buttonView, isChecked ->
                    if (tag != itemImpl) return@setOnCheckedChangeListener
                    onCheck(itemImpl, buttonView, isChecked, position)
                }
                isChecked = itemImpl.isChecked()
            }

            when {
                itemImpl.isDir -> {
                    ivIcon.setImageResource(R.drawable.ic_folder_file_picker)
                    tvFileSize.visibility = View.GONE
                }
                else -> {
                    tvFileSize.visibility = View.VISIBLE
                    tvFileSize.text = "文件大小：${formatFileSize(itemImpl.fileSize)}"
                    val resId: Int =
                        itemImpl.fileType?.fileIconResId ?: R.drawable.ic_unknown_file_picker
                    when (itemImpl.fileType) {
                        is RasterImageFileType, is VideoFileType -> {
                            ImageLoadController.load(
                                context,
                                ivIcon,
                                itemImpl.filePath,
                                resId
                            )
                        }
                        else -> {
                            ivIcon.setImageResource(resId)
                        }
                    }
                }
            }
        }
    }

    /*--------------------------ViewHolder End------------------------------*/

    /*--------------------------OutSide call method begin------------------------------*/

    fun setNewData(list: List<FileItemBeanImpl>?) {
        list?.let {
            dataList.clear()
            dataList.addAll(it)
            notifyDataSetChanged()
        }
    }

    inline fun multipleCheckOrNo(
        item: FileItemBeanImpl,
        position: Int,
        isCanSelect: () -> Boolean,
        checkFailedFunc: () -> Unit,
    ) {
        when {
            item.isChecked() -> {
                // 当前被选中，说明即将取消选中
                // had selected, will dis-select
                multipleDisCheck(position)
            }
            isCanSelect() -> {
                // 当前未被选中，并且检查合格，则即将新增选中
                // current item is not selected, and can be selected, will select
                multipleCheck(position)
            }
            else -> {
                // 新增选中项失败的情况
                // add new selected item failed
                checkFailedFunc()
            }
        }
    }

    fun multipleCheck(position: Int) {
        getItem(position)?.let {
            it.setChecked(true)
            notifyItemChanged(position, true)
        }
    }

    fun multipleDisCheck(position: Int) {
        getItem(position)?.let {
            it.setChecked(false)
            notifyItemChanged(position, false)
        }
    }

    fun singleCheck(position: Int) {
        when (latestChoicePos) {
            -1 -> {
                // 从未选中过
                getItem(position)?.let {
                    it.setChecked(true)
                    notifyItemChanged(position, true)
                }
                latestChoicePos = position
            }
            position -> {
                // 取消选中
                getItem(latestChoicePos)?.let {
                    it.setChecked(false)
                    notifyItemChanged(latestChoicePos, false)
                }
                latestChoicePos = -1
            }
            else -> {
                // disCheck the old one
                getItem(latestChoicePos)?.let {
                    it.setChecked(false)
                    notifyItemChanged(latestChoicePos, false)
                }
                // check the new one
                latestChoicePos = position
                getItem(latestChoicePos)?.let {
                    it.setChecked(true)
                    notifyItemChanged(latestChoicePos, true)
                }
            }
        }
    }

    fun disCheckAll() {
        dataList
            .forEachIndexed { index, item ->
                if (!(config.isSkipDir && item.isDir) && item.isChecked()) {
                    item.setChecked(false)
                    checkedSet.remove(item)
                    listener?.onCheckSizeChanged(checkedCount)
                    notifyItemChanged(index, false)
                }
            }
    }

    fun checkAll() {
        dataList
            .forEachIndexed { index, item ->
                if (checkedSet.size >= config.maxSelectable) {
                    return
                }
                if (!(config.isSkipDir && item.isDir) && !item.isChecked()) {
                    item.setChecked(true)
                    checkedSet.add(item)
                    listener?.onCheckSizeChanged(checkedCount)
                    notifyItemChanged(index, true)
                }
            }
    }

    fun resetCheck() {
        checkedSet.clear()
    }


    /*--------------------------OutSide call method end------------------------------*/
    companion object {
        const val DEFAULT_FILE_TYPE = 10001
    }
}