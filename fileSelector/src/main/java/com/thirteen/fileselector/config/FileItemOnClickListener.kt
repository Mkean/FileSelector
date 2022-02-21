package com.thirteen.fileselector.config

import android.view.View
import com.thirteen.fileselector.adapter.FileListAdapter

/**
 * @Author:
 * @Description:
 */

interface FileItemOnClickListener {


    fun onItemClick(
        itemAdapter: FileListAdapter,
        itemView: View,
        position: Int,
    )

    fun onItemChildClick(
        itemAdapter: FileListAdapter,
        itemView: View,
        position: Int,
    )

    fun onItemLongClick(
        itemAdapter: FileListAdapter,
        itemView: View,
        position: Int,
    )

}