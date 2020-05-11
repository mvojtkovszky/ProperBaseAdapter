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
class AdapterViewHolder<AIV: View>(
    itemView: View,
    val viewType: Int // View type in use when this view holder got created
) : RecyclerView.ViewHolder(itemView) {

    /**
     * Unique id for this view holder
     */
    val id: String = UUID.randomUUID().toString()
    private var callbackListener: OnCallbackListener<AIV>? = null

    fun setCallbackListener(callbackListener: OnCallbackListener<AIV>) {
        this.callbackListener = callbackListener
    }

    fun onItemViewAttached() {
        @Suppress("UNCHECKED_CAST")
        callbackListener?.onItemViewAttached(itemView as AIV)
    }

    fun onItemViewDetached() {
        @Suppress("UNCHECKED_CAST")
        callbackListener?.onItemViewDetached(itemView as AIV)
    }

    fun onItemViewRecycled() {
        @Suppress("UNCHECKED_CAST")
        callbackListener?.onItemViewRecycled(itemView as AIV)
    }

    fun onItemViewFailedToRecycle() {
        @Suppress("UNCHECKED_CAST")
        callbackListener?.onItemViewFailedToRecycle(itemView as AIV)
    }

    interface OnCallbackListener<in AIV: View> {
        fun onItemViewAttached(view: AIV)
        fun onItemViewDetached(view: AIV)
        fun onItemViewRecycled(view: AIV)
        fun onItemViewFailedToRecycle(view: AIV)
    }
}