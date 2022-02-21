package com.thirteen.fileselector.engine

import android.content.Context
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.thirteen.fileselector.R

/**
 * 使用 Glide 加载图片
 */
class GlideEngine : ImageEngine {

    override fun loadImage(
        context: Context?,
        imageView: ImageView?,
        url: String?,
        placeholder: Int
    ) {
        if (context == null || imageView == null) {
            return
        }
        Glide.with(context)
            .asBitmap()
            .load(url)
            .override(imageView.width, imageView.height)
            .error(R.drawable.ic_unknown_file_picker)
            .into(imageView)
    }
}