package com.thirteen.fileselector.utils

import android.os.Environment
import com.thirteen.fileselector.bean.FileItemBeanImpl
import com.thirteen.fileselector.bean.FileNavBeanImpl
import com.thirteen.fileselector.config.FilePickerConfig.Companion.STORAGE_CUSTOM_ROOT_PATH
import com.thirteen.fileselector.config.FilePickerConfig.Companion.STORAGE_EXTERNAL_STORAGE
import com.thirteen.fileselector.config.FilePickerManager.config
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

/**
 *
 */
object FileUtils {

    /**
     * 根据配置参数获取根目录文件
     */
    fun getRootFile(): File {
        return when (config.mediaStorageType) {
            STORAGE_EXTERNAL_STORAGE -> {
                File(Environment.getExternalStorageDirectory().absoluteFile.toURI())
            }
            else -> {
                File(Environment.getExternalStorageDirectory().absoluteFile.toURI())
            }
        }
    }

    /**
     * 获取给定文件对象 [rootFile] 下的所有文件，生成列表项对象
     */
    fun produceListDataSource(rootFile: File): ArrayList<FileItemBeanImpl> {
        val listData: ArrayList<FileItemBeanImpl> = ArrayList()
        var isDetected = false

        // 查看是否在根目录上级
        val realRoot = getRootFile()
        val isInRootParent = rootFile.list() == null
                && !config.isSkipDir
                && rootFile.parent == realRoot.parentFile.parent
        if (isInRootParent) {
            // 如果是文件夹作为可选项时，需要让根目录也作为 item 被点击
            listData.add(
                FileItemBeanImpl(
                    realRoot.name,
                    realRoot.path,
                    false,
                    null,
                    isDir = true,
                    isHide = false,
                    fileUpdateTime = realRoot.lastModified()
                )
            )

            return config.selfFileter?.doFilter(listData) ?: listData
        }
        if (rootFile.listFiles().isNullOrEmpty()) {
            return listData
        }
        for (file in rootFile.listFiles()) {
            // 以符号 . 开头的视为隐藏文件或隐藏文件夹，根据需求进行过滤
            val isHiddenFile = file.name.startsWith(".")
            if (!config.isShowHiddenFiles && isHiddenFile) {
                continue
            }
            if (file.isDirectory) {
                listData.add(
                    FileItemBeanImpl(
                        file.name,
                        file.path,
                        false,
                        null,
                        isDir = true,
                        isHiddenFile,
                        fileUpdateTime = file.lastModified()
                    )
                )
                continue
            }

            val itemBean = FileItemBeanImpl(
                file.name,
                file.absolutePath,
                false,
                null,
                false,
                isHiddenFile,
                fileSize = file.length(),
                fileUpdateTime = file.lastModified()
            )
            // 如果调用者没有实现文件类型甄别器，则使用默认的甄别器
            config.customDetector?.fillFileType(itemBean)
                ?: config.defaultFileDetector.fillFileType(itemBean)
            isDetected = itemBean.fileType != null
            if (config.defaultFileDetector.enableCustomTypes
                && config.isAutoFilter
                && !isDetected
            ) {
                // enable auto filter AND using user's custom file type. Filter them.
                continue
            }
            listData.add(itemBean)
        }

        // 默认字典排序
        listData.sortWith(
            compareBy(
                { !it.isDir },
                {
                    it.fileName.uppercase(Locale.getDefault())
                }
            )
        )

        // 将当前列表数据暴露，以供调用者自己处理数据
        return config.selfFileter?.doFilter(listData) ?: listData
    }

    /**
     * 为导航栏添加数据，也就是没进入一个文件夹，导航栏的列表就添加一个对象
     * 如果是回退到上层文件夹，则删除后续子目录元素
     */
    fun produceNavDataSource(
        currentDataSource: ArrayList<FileNavBeanImpl>,
        nextPath: String,
    ): ArrayList<FileNavBeanImpl> {
        // 优先级：目标设备名称 --> 自定义路径 --> 默认 SD 卡
        if (currentDataSource.isEmpty()) {
            val dirName = getDirAlias(getRootFile())
            currentDataSource.add(FileNavBeanImpl(dirName, nextPath))

            return currentDataSource
        }

        for (data in currentDataSource) {
            // 如果是回到根目录
            if (nextPath == currentDataSource.first().dirPath) {
                return ArrayList(currentDataSource.subList(0, 1))
            }

            // 如果是回到当前目录（不包含根目录情况）
            // 直接返回
            val isCurrent = nextPath == currentDataSource[currentDataSource.size - 1].dirPath
            if (isCurrent) {
                return currentDataSource
            }

            // 如果是回到上层的某一目录（即当前列表中有该路径）
            // 将列表截取至目标路径元素
            val isBackToAbove = nextPath == data.dirPath
            if (isBackToAbove) {
                return ArrayList(
                    currentDataSource.subList(
                        0,
                        currentDataSource.indexOf(data) + 1)
                )
            }
        }
        // 循环到此，意味着将是将是进入子目录
        currentDataSource.add(
            FileNavBeanImpl(
                nextPath.substring(nextPath.lastIndexOf("/") + 1),
                nextPath
            )
        )
        return currentDataSource
    }

    fun getDirAlias(file: File): String {
        val isCustomRoot = config.mediaStorageType == STORAGE_CUSTOM_ROOT_PATH
                && file.absolutePath == config.customRootPath
        val isPreSetStorageRoot = config.mediaStorageType == STORAGE_EXTERNAL_STORAGE
                && file.absolutePath == getRootFile().absolutePath
        val isDefaultRoot = file.absolutePath == getRootFile().absolutePath

        return when {
            isCustomRoot || isPreSetStorageRoot -> {
                config.mediaStorageName
            }
            isDefaultRoot -> {
                config.defaultStoreName
            }
            else -> {
                file.name
            }
        }
    }
}