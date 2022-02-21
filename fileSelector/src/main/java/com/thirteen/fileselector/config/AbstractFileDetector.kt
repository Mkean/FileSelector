package com.thirteen.fileselector.config

import com.thirteen.fileselector.bean.FileItemBeanImpl


/**
 *
 * 这个类用于注册自己的文件类型检测方法，需要遵循以下步骤：
 *
 * <ul>
 *     <li>实现自己的文件类型[FileType]，也就是其中的 [FileType.verify] 方法</li>
 *     <li>构建此类的一个子类，并在 [AbstractFileDetector.fillFileType] 中，检测文件类型，并赋值给 [FileItemBeanImpl.fileType] 属性</li>
 * </ul>
 */
abstract class AbstractFileDetector {

    /**
     * 自定义文件类型识别方法，传入 @param itemBeanImpl 条目数据对象，
     * 由实现者来实现文件类型的甄别，返回填充了 fileType 的方法
     */
    abstract fun fillFileType(itemBeanImpl: FileItemBeanImpl): FileItemBeanImpl
}