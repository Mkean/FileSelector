package com.thirteen.fileselector.filetype

import com.thirteen.fileselector.R


/**
 * 压缩文件类型
 */
class CompressedFileType : FileType {

    override val fileType: String
        get() = "Compressed"

    override val fileIconResId: Int
        get() = R.drawable.ic_compressed_file_picker

    override fun verify(fileName: String): Boolean {
        /**
         * 使用 endWith 是不可靠的，因为文件名有可能是以格式结尾，但是没有 . 符号
         * 比如 文件名仅为：example_png
         */
        val isHasSuffix = fileName.contains(".")
        if (!isHasSuffix) {
            // 如果没有 . 符号，即是没有文件后缀
            return false
        }
        return when (fileName.substring(fileName.lastIndexOf(".") + 1)) {
            "zip", "rar", "arj", "tar.gz", "tgz", "gz", "iso", "tbz", "tbz2", "7z" -> {
                true
            }
            else -> {
                false
            }
        }
    }
}