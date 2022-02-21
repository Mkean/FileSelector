package com.thirteen.fileselector.engine

import android.content.Context
import android.util.Log
import android.widget.ImageView
import com.thirteen.fileselector.R
import com.thirteen.fileselector.config.FilePickerManager

/**
 * 一个全局的图片加载控制类，包含了判断是否存在以及存在哪种图片加载引擎
 */
object ImageLoadController {

    private val enableGlide by lazy {
        try {
            Class.forName("com.bumptech.glide.Glide")
            true
        } catch (e: ClassNotFoundException) {
            false
        } catch (e: ExceptionInInitializerError) {
            false
        }
    }


    private val enablePicasso by lazy {
        try {
            Class.forName("com.squareup.picasso.Picasso")
            true
        } catch (e: ClassNotFoundException) {
            false
        } catch (e: ExceptionInInitializerError) {
            false
        }
    }

    private var engine: ImageEngine? = null

    fun load(
        context: Context,
        iv: ImageView,
        url: String,
        placeholder: Int? = R.drawable.ic_unknown_file_picker
    ) {
        if (engine == null && !initEngine()) {
            iv.setImageResource(placeholder ?: R.drawable.ic_unknown_file_picker)
            return
        }
        try {
            engine?.loadImage(context, iv, url, placeholder ?: R.drawable.ic_unknown_file_picker)
        } catch (e: NoSuchMethodError) {
            Log.d(
                "ImageLoadController", """
                FileSelector throw NoSuchMethodError which means current Glide version was not supported.
                We recommend using 4.9+ or you should implements your own ImageEngine.
                """.trimIndent()
            )
        }
        iv.setImageResource(placeholder ?: R.drawable.ic_unknown_file_picker)
    }

    /**
     * 每次更新配置的时候，都需要重新初始化图片加载框架
     */
    private fun initEngine(): Boolean {
        engine = when {

            FilePickerManager.config.customImageEngine != null -> {
                FilePickerManager.config.customImageEngine
            }
            enableGlide -> {
                GlideEngine()
            }
            enablePicasso -> {
                PicassoEngine()
            }
            else -> null

        }
        return engine != null
    }

    fun reset() {
        engine = null
    }
}