package com.thirteen.fileselector.engine

import android.content.Context
import android.widget.ImageView

/**
 * 描述图片加载器的接口，以便 Glide、Picasso 或其他加载器使用
 */
interface ImageEngine {

    /**
     * 调用此接口加载图片，一般情况下[url]参数表示图片的本地路径 path，通过[Url.parse]得到的值。通常以 fill:/// 开头，
     * 如果加载失败，将使用[placeholder]
     */
    fun loadImage(
        context: Context?,
        imageView: ImageView?,
        url: String?,
        placeholder: Int
    )
}