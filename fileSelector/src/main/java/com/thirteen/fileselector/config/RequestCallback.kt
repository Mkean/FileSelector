package com.thirteen.fileselector.config

import com.thirteen.fileselector.bean.FileItemBeanImpl


/**
 * @Author:
 * @Description:
 */
interface RequestCallback {

    /**
     * 结果回调
     */
    fun onResult(data: ArrayList<FileItemBeanImpl>)
}