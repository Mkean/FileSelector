package com.thirteen.fileselector.config

import android.app.Activity
import androidx.fragment.app.Fragment
import com.thirteen.fileselector.engine.ImageLoadController
import java.lang.ref.WeakReference

/**
 *
 */
object FilePickerManager {

    internal var contextRef: WeakReference<Activity>? = null

    internal var fragmentRef: WeakReference<Fragment>? = null

     lateinit var config: FilePickerConfig

    private var dataList: MutableList<String> = ArrayList()

    @JvmStatic
    fun from(activity: Activity): FilePickerConfig {
        reset()
        contextRef = WeakReference(activity)
        config = FilePickerConfig(this)
        return config
    }

    /**
     * 不能使用 fragmentRef.getContext()，因为无法保证外部的代码环境
     */
    @JvmStatic
    fun from(fragment: Fragment): FilePickerConfig {
        reset()
        fragmentRef = WeakReference(fragment)
        contextRef = WeakReference(fragment.activity)
        config = FilePickerConfig(this)
        return config
    }

    /**
     * 保存数据@param list List<String>到本类中
     */
    internal fun saveData(list: MutableList<String>) {
        dataList = list
    }

    /**
     * 供调用者获取结果
     * @return List<String>
     */
    @JvmOverloads
    @JvmStatic
    fun obtainData(release: Boolean = false): MutableList<String> {
        val result = ArrayList(dataList)
        if (release) {
            release()
        }
        return result
    }

    private fun reset() {
        contextRef?.clear()
        fragmentRef?.clear()
        dataList.clear()
        ImageLoadController.reset()
    }

    /**
     * 释放资源与重置属性
     * Release resources and reset attributes
     */
    @JvmStatic
    fun release() {
        reset()
        if (this::config.isInitialized) {
            config.clear()
        }
    }
}