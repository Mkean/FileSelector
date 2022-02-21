package com.thirteen.fileselector.utils

/**
 *
 */
interface FileListAdapterListener {

    fun onCheckSizeChanged(count: Int)
}

private typealias OnCheckSizeChagned = (count: Int) -> Unit

class FileListAdapterListenerBuilder : FileListAdapterListener {

    private var onCheckSizeChanged: OnCheckSizeChagned? = null

    override fun onCheckSizeChanged(count: Int) {
        onCheckSizeChanged?.invoke(count)
    }

    fun onCheckSizeChanged(onCheckSizeChanged: OnCheckSizeChagned) {
        this.onCheckSizeChanged = onCheckSizeChanged
    }

}

