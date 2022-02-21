package com.thirteen.fileselector.utils

import android.content.res.Resources
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.*

/**
 *
 */

val Int.dp: Int
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        Resources.getSystem().displayMetrics
    ).toInt()


/**
 * 时间戳 To 字符串日期
 *
 * @param format
 * @param timesstamp
 * @return
 */
fun getTimestampToDate(format: String, timesstamp: Long): String {
    return SimpleDateFormat(format, Locale.getDefault()).format(timesstamp)
}


/**
 * 格式化文件大小
 * @param length 文件大小（常以 byte 为单位）
 * @return String  格式化的文件大小（保留两位小数）
 */
fun formatFileSize(length: Long): String {
    var result = ""
    var subString = 0

    when {
        length >= 1073741824 -> {
            // 1024*1024*1024
            // 大于等于 1GB
            subString = (length.toFloat() / 1073741824).toString().indexOf(".")
            result = ("${length.toFloat() / 1073741824}000").substring(0,
                subString + 3) + "GB"
        }
        length >= 1048576 -> {
            // 1024*1024
            // 大于等于 1MB 小于 1GB
            subString = (length.toFloat() / 1048576).toString().indexOf(".")
            result = ("${length.toFloat() / 1048576}000").substring(0, subString + 3) + "MB"
        }
        length >= 1024 -> {
            // 大于等于 1KB 小于 1MB
            subString = (length.toFloat() / 1024).toString().indexOf(".")
            result = ("${length.toFloat() / 1024}000").substring(0, subString + 3) + "KB"
        }
        else -> {
            // 小于 1KB
            result = "${length}B"
        }
    }

    return result

}

private typealias _beforeChanged = (s: CharSequence?, start: Int, count: Int, after: Int) -> Unit

private typealias _onTextChanged = (s: CharSequence?, start: Int, before: Int, count: Int) -> Unit

private typealias _afterTextChanged = (s: Editable?) -> Unit


class TextWatcherAdapter : TextWatcher {

    private var _beforeChanged: _beforeChanged? = null
    private var _onTextChanged: _onTextChanged? = null
    private var _afterTextChanged: _afterTextChanged? = null

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        _beforeChanged?.invoke(s, start, count, after)
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        _onTextChanged?.invoke(s, start, before, count)
    }

    override fun afterTextChanged(s: Editable?) {
        _afterTextChanged?.invoke(s)
    }

    fun beforeChanged(listener: _beforeChanged) {
        this._beforeChanged = listener
    }

    fun onTextChanged(listener: _onTextChanged) {
        this._onTextChanged = listener
    }

    fun afterTextChanged(listener: _afterTextChanged) {
        this._afterTextChanged = listener
    }

}

/*inline fun TextView.textWatcher(init: TextWatcherAdapter.() -> Unit) {
    addTextChangedListener(TextWatcherAdapter().apply(init))
}*/

inline fun TextView.textWatcher(init: TextWatcherAdapter.() -> Unit) {
    addTextChangedListener(TextWatcherAdapter().apply {
        init()
    })
}