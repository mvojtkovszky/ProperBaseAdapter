package com.vojtkovszky.properbaseadapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import java.util.*

/**
 * Created by Marcel Vojtkovszky on 2019/07/23.
 *
 * Class representing RecyclerView.ViewHolder, with added callbacks from adapter towards
 * the view holder.
 */
@Suppress("unused")
class AdapterViewHolder<AIV: View>(
    itemView: View,
    val viewType: Int // View type in use when this view holder got created
) : RecyclerView.ViewHolder(itemView) {

    /**
     * Unique id for this view holder
     */
    val id: String = UUID.randomUUID().toString()
    private var callbackListener: OnCallbackListener<AIV>? = null

    internal fun setCallbackListener(callbackListener: OnCallbackListener<AIV>) {
        this.callbackListener = callbackListener
    }

    fun onItemViewAttached() {
        @Suppress("UNCHECKED_CAST")
        callbackListener?.onItemViewAttached(super.itemView as AIV)
    }

    fun onItemViewDetached() {
        @Suppress("UNCHECKED_CAST")
        callbackListener?.onItemViewDetached(super.itemView as AIV)
    }

    fun onItemViewRecycled() {
        @Suppress("UNCHECKED_CAST")
        callbackListener?.onItemViewRecycled(super.itemView as AIV)
    }

    fun onItemViewFailedToRecycle() {
        @Suppress("UNCHECKED_CAST")
        callbackListener?.onItemViewFailedToRecycle(super.itemView as AIV)
    }

    interface OnCallbackListener<in AIV: View> {
        fun onItemViewAttached(view: AIV)
        fun onItemViewDetached(view: AIV)
        fun onItemViewRecycled(view: AIV)
        fun onItemViewFailedToRecycle(view: AIV)
    }
}