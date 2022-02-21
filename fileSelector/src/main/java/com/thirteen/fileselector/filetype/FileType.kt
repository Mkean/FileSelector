package com.thirteen.fileselector.filetype

interface FileType {

    /**
     * 文件类型
     */
    val fileType: String


    val fileIconResId: Int

    /**
     * 传入文件路径，判断是否为该类型
     * @param fileName String
     * @return
     */
    fun verify(fileName: String): Boolean
}