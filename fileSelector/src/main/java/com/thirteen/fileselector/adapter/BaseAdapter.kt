package com.thirteen.fileselector.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.thirteen.fileselector.bean.FileBean

/**
 * @Author:
 * @Description:
 */
abstract class BaseAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    abstract fun getItem(position: Int): FileBean?
    abstract fun getItemView(position: Int): View?
}