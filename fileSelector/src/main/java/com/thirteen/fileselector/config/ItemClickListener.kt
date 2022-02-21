package com.thirteen.fileselector.config

import android.view.View
import com.thirteen.fileselector.adapter.FileListAdapter

/**
 * @Author:
 * @Description:
 * If the user return true means the event has been consumed.
 */
interface ItemClickListener {

    fun onItemClick(
        itemAdapter: FileListAdapter,
        itemView: View,
        position: Int,
    ): Boolean

    fun onItemChildClick(
        itemAdapter: FileListAdapter,
        itemView: View,
        position: Int,
    ): Boolean

    fun onItemLongClick(
        itemAdapter: FileListAdapter,
        itemView: View,
        position: Int,
    ): Boolean
}