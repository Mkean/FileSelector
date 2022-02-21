package com.thirteen.fileselector.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

/**
 * @Author:
 * @Description:
 */
class RecyclerViewFilePicker @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null,
) : RecyclerView(context, attrs) {


    var emptyView: View? = null
        set(value) {
            field = value
            (this@RecyclerViewFilePicker.rootView as ViewGroup).addView(value)
            field?.visibility = View.GONE
        }

    fun hasEmptyView() = emptyView != null

    override fun setAdapter(adapter: Adapter<*>?) {
        super.setAdapter(adapter)
        adapter?.registerAdapterDataObserver(adapterDataObserver)
        adapterDataObserver.onChanged()
    }

    private val adapterDataObserver by lazy {
        object : AdapterDataObserver() {
            override fun onChanged() {

                if (adapter?.itemCount ?: 0 == 0 && emptyView != null) {
                    emptyView?.animate()
                        ?.alpha(1f)
                        ?.withStartAction {
                            emptyView?.visibility = View.VISIBLE
                        }
                        ?.start()
                    this@RecyclerViewFilePicker.visibility = View.GONE
                } else {
                    emptyView?.animate()
                        ?.alpha(0f)
                        ?.withEndAction {
                            emptyView?.visibility = View.GONE
                        }
                    this@RecyclerViewFilePicker.visibility = View.VISIBLE
                }
            }

            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                onChanged()
            }

            override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
                onChanged()
            }

            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                onChanged()
            }

            override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
                onChanged()
            }

            override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) {
                onChanged()
            }
        }
    }
}