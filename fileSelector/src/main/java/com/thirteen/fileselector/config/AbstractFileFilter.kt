package com.thirteen.fileselector.config

import com.thirteen.fileselector.bean.FileItemBeanImpl


/**
 *
 */
abstract class AbstractFileFilter {

    /**
     * 自定义过滤接口，此接口在生成列表数据的时候被调用
     * 返回一个经过处理的列表数据，进而生成列表视图
     */
    abstract fun doFilter(listData: ArrayList<FileItemBeanImpl>): ArrayList<FileItemBeanImpl>
}