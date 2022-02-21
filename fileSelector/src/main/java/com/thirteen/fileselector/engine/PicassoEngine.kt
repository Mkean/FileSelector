package com.thirteen.fileselector.engine

import android.content.Context
import android.widget.ImageView
import com.squareup.picasso.Picasso
import java.io.File

/**
 * 使用 Picasson 加载图片
 */
class PicassoEngine : ImageEngine {

    override fun loadImage(
        context: Context?,
        imageView: ImageView?,
        url: String?,
        placeholder: Int
    ) {
        if (context == null || imageView == null) return
        if (url?.startsWith("http") == true) {
            Picasso.get()
                .load(url)
                .placeholder(placeholder)
                .into(imageView)
        } else {
            Picasso.get()
                .load(File(url))
                .placeholder(placeholder)
                .into(imageView)
        }
    }
}