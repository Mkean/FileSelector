package com.thirteen.fileselector.bean


class FileNavBeanImpl(val dirName: String, val dirPath: String) : FileBean {

    override var fileName: String
        get() = dirName
        set(value) {}

    override var filePath: String
        get() = dirPath
        set(value) {}
}