package com.thirteen.fileselector.config

import com.thirteen.fileselector.bean.FileItemBeanImpl
import com.thirteen.fileselector.filetype.*

/**
 * @Author:
 * @Description:
 */
class DefaultFileDetector : AbstractFileDetector() {

    var enableCustomTypes: Boolean = false
        private set

    private val allDefaultFileType: ArrayList<FileType> by lazy {
        ArrayList()
    }

    fun registerDefaultTypes() {
        with(allDefaultFileType) {
            clear()
            add(AudioFileType())
            add(CompressedFileType())
            add(DataBaseFileType())
            add(DataFileType())
            add(ExecutableFileType())
            add(FontFileType())
            add(PageLayoutFileType())
            add(RasterImageFileType())
            add(TextFileType())
            add(VideoFileType())
            add(WebFileType())
        }
        enableCustomTypes = false
    }

    fun registerCustomTypes(customFileTYpes: ArrayList<FileType>) {
        allDefaultFileType.clear()
        allDefaultFileType.addAll(customFileTYpes)
        enableCustomTypes = true
    }

    fun clear() {
        allDefaultFileType.clear()
        enableCustomTypes = false
    }

    override fun fillFileType(itemBeanImpl: FileItemBeanImpl): FileItemBeanImpl {
        for (type in allDefaultFileType) {
            if (type.verify(itemBeanImpl.fileName)) {
                itemBeanImpl.fileType = type
                break
            }
        }
        return itemBeanImpl
    }
}