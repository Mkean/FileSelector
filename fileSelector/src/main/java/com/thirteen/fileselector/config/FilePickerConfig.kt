package com.thirteen.fileselector.config

import android.content.Intent
import androidx.annotation.NonNull
import androidx.annotation.StringRes
import com.thirteen.fileselector.FilePickerActivity
import com.thirteen.fileselector.R
import com.thirteen.fileselector.engine.ImageEngine
import com.thirteen.fileselector.filetype.FileType
import java.io.File
import java.util.concurrent.ExecutorService

/**
 *
 */
class FilePickerConfig(private val pickerManager: FilePickerManager) {

    var isAutoFilter: Boolean = false

    private val customFileTypes: ArrayList<FileType> by lazy {
        ArrayList(2)
    }

    private val contextRes = FilePickerManager.contextRef!!.get()!!.resources

    /**
     * 是否显示隐藏文件，默认隐藏
     * 以符号 . 开头的文件或文件夹视为隐藏
     */
    var isShowHiddenFiles = false
        private set

    /**
     * 是否显示选中框，模式显示
     */
    var isShowingCheckBox = true
        private set

    /**
     * 在选中时是否忽略文件夹
     */
    var isSkipDir = true
        private set

    /**
     * 是否是单选
     * 如果是单选，则隐藏顶部【全选/取消全选按钮】
     */
    var singleChoice = false
        private set

    /**
     * 最大可被选中数量
     */
    var maxSelectable = Int.MAX_VALUE
        private set

    internal val defaultStoreName = contextRes.getString(R.string.file_picker_tv_sd_card)

    /**
     * 存储类型
     */
    var mediaStorageName = defaultStoreName
        private set

    /**
     * 自定义存储类型，根据此返回根目录
     */
    @get:StorageMediaType
    @set:StorageMediaType
    var mediaStorageType: String = STORAGE_EXTERNAL_STORAGE

    /**
     * 自定义根目录路径，需要先设置 [mediaStorageType] 为 [STORAGE_CUSTOM_ROOT_PATH]
     */
    var customRootPath: String = ""
        private set

    internal var customRootPathFile: File? = null
        private set

    /**
     * 自定义过滤器
     */
    var selfFileter: AbstractFileFilter? = null
        private set

    /**
     * 自定文件类型甄别器和默认类型甄别起
     */
    @Deprecated("Use 'register' function instead.",
        replaceWith = ReplaceWith("registerFileType()"),
        level = DeprecationLevel.WARNING)
    var customDetector: AbstractFileDetector? = null

    val defaultFileDetector: DefaultFileDetector by lazy { DefaultFileDetector().apply { registerDefaultTypes() } }

    /**
     * 点击操作接口
     */
    var itemClickListener: ItemClickListener? = null
        private set

    /**
     * 全选文字、取消全选文字、返回文字、已选择文字、确认按钮、选择限制提示语、空列表提示
     */
    var selectAllText: String = contextRes.getString(R.string.file_picker_tv_select_all)
        private set

    var deSelectAllText: String = contextRes.getString(R.string.file_picker_tv_deselect_all)
        private set

    @StringRes
    var hadSelectedText = R.string.file_picker_selected_count
        private set

    var confirmText = contextRes.getString(R.string.file_picker_tv_select_done)
        private set

    @StringRes
    var maxSelectCountTips = R.string.max_select_count_tips
        private set

    var emptyListTips = contextRes.getString(R.string.empty_list_tips_file_picker)
        private set

    /**
     * 允许使用项目中的线程池
     */
    internal var threadPool: ExecutorService? = null

    /**
     * 自定义线程池不会默认关闭，如果你需要在结束文件选择时关闭，请传 true
     */
    internal var threadPoolAutoShutDown = false

    /**
     * 自定义图片加载框架
     */
    var customImageEngine: ImageEngine? = null
        private set

    /**
     * 结果返回
     */
    var requestCallback: RequestCallback? = null
        private set

    fun setHiddenFiles(isShow: Boolean): FilePickerConfig {
        isShowHiddenFiles = isShow
        return this
    }

    fun showCheckBox(isShow: Boolean): FilePickerConfig {
        isShowingCheckBox = isShow
        return this
    }

    fun skipDirWhenSelect(isSkip: Boolean): FilePickerConfig {
        isSkipDir = isSkip
        return this
    }

    fun maxSelectable(max: Int): FilePickerConfig {
        maxSelectable = if (max < 0) Int.MAX_VALUE else max
        return this
    }

    @JvmOverloads
    fun storageType(
        volumeName: String,
        @StorageMediaType storageMediaType: String,
    ): FilePickerConfig {
        mediaStorageName = volumeName
        mediaStorageType = storageMediaType
        return this
    }

    fun setCUstomRootPath(path: String): FilePickerConfig {
        customRootPath = path

        path.takeIf {
            it.isNotBlank()
        }?.let {
            File(it)
        }?.takeIf {
            it.exists()
        }?.let {
            customRootPathFile = it
        }
        return this
    }

    fun filter(fileFilter: AbstractFileFilter): FilePickerConfig {
        selfFileter = fileFilter
        return this
    }

    /**
     * 实现 [AbstractFileDetector] 以自定义自己的文件类型检测器
     */
    @Deprecated("Use 'register' function instead.",
        ReplaceWith("registerFileTypes(types)"))
    fun customDetector(detector: AbstractFileDetector): FilePickerConfig {
        this.customDetector = detector
        return this
    }

    /**
     * Setting item click lsitener which can intercept click event.
     */
    fun setItemClickListener(
        itemClickListener: ItemClickListener,
    ): FilePickerConfig {
        this.itemClickListener = itemClickListener
        return this
    }

    /**
     * 是否启用单选模式
     */
    fun enableSingleChoice(): FilePickerConfig {
        this.singleChoice = true
        return this
    }

    /**
     * 设置界面的字符串：包括：
     * 全选 [selectAllText]
     * 取消选中 [deSelectAllText]
     * 已选择 [hadSelectedText]
     * 确认 [confirm]
     * 多选限制提示 [maxSelectCountTipsText]
     * 空视图 [emptyListTips]
     * 注意：
     * [hadSelectedText] 和 [maxSelectCountTipsText] 是 String format 限制的字符串，需要传入 [R.string.file_picker_selected_count] 类似的
     * 中的 ID，并且包含一个可以传入 Int 类型的占位符
     */
    fun setText(
        @NonNull selectAllText: String = contextRes.getString(R.string.file_picker_tv_select_all),
        @NonNull deSelectAllText: String = contextRes.getString(R.string.file_picker_tv_deselect_all),
        @NonNull @StringRes hadSelectedText: Int = R.string.file_picker_selected_count,
        @NonNull confirm: String = contextRes.getString(R.string.file_picker_tv_select_done),
        @NonNull @StringRes maxSelectCountTipsText: Int = R.string.max_select_count_tips,
        @NonNull emptyListTips: String = contextRes.getString(R.string.empty_list_tips_file_picker),
    ): FilePickerConfig {
        this.selectAllText = selectAllText
        this.deSelectAllText = deSelectAllText
        this.hadSelectedText = hadSelectedText
        this.confirmText = confirm
        this.maxSelectCountTips = maxSelectCountTipsText
        this.emptyListTips = emptyListTips
        return this
    }

    fun imageEngine(ie: ImageEngine): FilePickerConfig {
        this.customImageEngine = ie
        return this
    }

    /**
     * 用于注册自定义的文件类型
     * 库将自动调用自定义类型中的 [FileType.verify] 来识别文件。如果识别成功，就会自动填充到 [FileItemBeanImpl.fileType] 中
     * 如果[autoFilter] 为 true，那么库将自动过滤掉不符合自定义类型的文件，不会在结果中显示
     * 如果为 false，那么就只是检测类型，不会对结果列表做修改
     *
     */
    fun registerFileType(type: List<FileType>, autoFilter: Boolean = true): FilePickerConfig {
        this.customFileTypes.addAll(type)
        this.defaultFileDetector.registerCustomTypes(customFileTypes)
        this.isAutoFilter = autoFilter
        return this
    }

    /**
     * 允许使用自定义的线程池，自定义线程池不会默认关闭，如果需要在结束文件选择时关闭，请传 true
     */
    fun threadPool(threadPool: ExecutorService, autoShutdown: Boolean): FilePickerConfig {
        this.threadPool = threadPool
        this.threadPoolAutoShutDown = autoShutdown
        return this
    }

    fun resetCustomFile() {
        this.customRootPathFile = null
    }

    fun forResult(callback: RequestCallback) {
        this.requestCallback = callback

        val activity = FilePickerManager.contextRef?.get()
        val fragment = FilePickerManager.fragmentRef?.get()

        val intent = Intent(activity, FilePickerActivity::class.java)
        if (fragment == null) {
            activity?.startActivity(intent)
        } else {
            fragment.startActivity(intent)
        }
    }

    fun clear() {
        this.customFileTypes.clear()
        this.customImageEngine = null
        this.selfFileter = null
        this.defaultFileDetector.clear()
        resetCustomFile()
    }

    companion object {

        /**
         * 手机内部的外置存储，也就是内置 SD 卡
         */
        @get:StorageMediaType
        const val STORAGE_EXTERNAL_STORAGE = "STORAGE_EXTERNAL_STORAGE"

        /**
         * TODO：可拔插的 SD 卡
         */
        @get:StorageMediaType
        const val STORAGE_UUID_SD_CARD = "STORAGE_UUID_SD_CARD"

        /**
         * TODO：可拔插 U 盘
         */
        @get:StorageMediaType
        const val STORAGE_UUID_USE_DRIVE = "STORAGE_UUID_USB_DRIVE"

        /**
         * 自定义路径
         */
        @get:StorageMediaType
        const val STORAGE_CUSTOM_ROOT_PATH = "STORAGE_CUSTOM_ROOT_PATH"

        /**
         * 存储类型，目前仅支持 [STORAGE_EXTERNAL_STORAGE] 和 [STORAGE_CUSTOM_ROOT_PATH]
         */
        @Retention(AnnotationRetention.SOURCE)
        annotation class StorageMediaType

    }


}
