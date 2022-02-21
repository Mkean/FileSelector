package com.thirteen.fileselector.bean

import com.thirteen.fileselector.filetype.FileType


/**
 * 文件列表项
 * @property isChecked 是否被选中
 * @property fileType 文件类型
 * @property isHide 是否为隐藏文件，以符号 . 开头的视为隐藏文件
 *
 * FileBean 接口属性
 * @property fileName 文件名
 * @property filePath 文件路径
 */
class FileItemBeanImpl(
    override var fileName: String,
    override var filePath: String,
    private var isChecked: Boolean,
    var fileType: FileType?,
    val isDir: Boolean,
    var isHide: Boolean,
    val fileSize: Long = 0,
    val fileUpdateTime: Long,
) : FileBean {

    fun isChecked(): Boolean {
        return isChecked
    }

    fun setChecked(check: Boolean) {
        isChecked = check;
    }

    override fun toString(): String {
        return "FileItemBeanImpl(fileName='$fileName', filePath='$filePath', isChecked=$isChecked, fileType=$fileType, isDir=$isDir, isHide=$isHide)"
    }


}