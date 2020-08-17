package com.vojtkovszky.properbaseadapter

import android.view.View
import android.view.ViewGroup
import androidx.annotation.AnimRes
import androidx.recyclerview.widget.RecyclerView
import kotlin.reflect.KClass

/**
 * Created by Marcel Vojtkovszky on 2019/07/23.
 *
 * Represents an item for an adapter.
 * It contains both a view holder and logic to populate or manipulate the view.
 */
@Suppress("UNCHECKED_CAST")
abstract class AdapterItem<AIV: View> : AdapterViewHolder.OnCallbackListener<AIV> {

    private var viewHolder: AdapterViewHolder<AIV>? = null
    private var boundPosition = RecyclerView.NO_POSITION // position when bind is called

    @AnimRes internal var animation: Int = 0
    internal var layoutParamsInitialized = false

    // allow margins to be set additionally
    internal var startMargin = 0
    internal var topMargin = 0
    internal var endMargin = 0
    internal var bottomMargin = 0
    // header option
    internal var isStickyHeader: Boolean = false
    // generic click listener
    internal var clickListener: View.OnClickListener? = null
    internal var viewTag: Any? = null

    companion object {
        /**
         * Used in case we want to determine view type id from outside
         * the adapter context for comparison.
         * Handy when using different layout manager
         */
        fun getViewTypeIdForClass(clazz: KClass<*>): Int {
            var h = 0
            clazz.simpleName?.let {
                for (element in it) {
                    h = 31 * h + element.toInt()
                }
            }
            return h
        }
    }

    /**
     * Called when underlying adapter calls
     * [ProperBaseAdapter.onCreateViewHolder]
     * indicating item has to provide a view on demand.
     *
     * Important!!!
     * Note that adapter might call this method for any of the adapter item matching the view
     * type id associated with it, just because it wants to have a new instance of view itself,
     * therefore we shouldn't set any dynamic data to it.
     */
    abstract fun getNewView(parent: ViewGroup): AIV

    /**
     * Called when underlying adapter triggers
     * [ProperBaseAdapter.onBindViewHolder]
     * indicating this item should populate the view
     *
     * @param view view provided with [AdapterItem.getNewView]
     */
    abstract fun onViewBound(view: AIV)

    /**
     * Result from [AdapterViewHolder.OnCallbackListener] interface
     */
    override fun onItemViewAttached(view: AIV) {}

    /**
     * Result from [AdapterViewHolder.OnCallbackListener] interface
     */
    override fun onItemViewDetached(view: AIV) {}

    /**
     * Result from [AdapterViewHolder.OnCallbackListener] interface
     */
    override fun onItemViewRecycled(view: AIV) {}

    /**
     * Result from [AdapterViewHolder.OnCallbackListener] interface
     */
    override fun onItemViewFailedToRecycle(view: AIV) {}

    //region PUBLIC METHODS
    /**
     * @return Returns view bound to this adapter item, with correct casting.
     * Mind that this will be null if called before view is bound by adapter.
     */
    fun getView(): AIV? {
        return if (boundPosition != RecyclerView.NO_POSITION) viewHolder?.itemView as AIV
        else null
    }

    /**
     * See [withAnimation]
     */
    fun setAnimation(@AnimRes animation: Int) {
        withAnimation(animation)
    }

    /**
     * See [withClickListener]
     */
    fun setClickListener(clickListener: View.OnClickListener?) {
        withClickListener(clickListener)
    }

    /**
     * See [withMargins]
     */
    fun setMargins(startMargin: Int = 0, topMargin: Int = 0, endMargin: Int = 0, bottomMargin: Int = 0) {
        withMargins(startMargin, topMargin, endMargin, bottomMargin)
    }

    /**
     * See [withStickyHeader]
     */
    fun setIsStickyHeader(isStickyHeader: Boolean) {
        withStickyHeader(isStickyHeader)
    }

    /**
     * See [withViewTag]
     */
    fun setViewTag(viewTag: Any?) {
        withViewTag(viewTag)
    }

    /**
     * Define a custom animation for view of this item to be used when visibility changes.
     * If none defined, BaseAdapter will use it's own default animation for it (if defined).
     */
    fun withAnimation(@AnimRes animation: Int): AdapterItem<AIV> {
        this.animation = animation
        return this
    }

    /**
     * Define a generic view click listener to be set to view bound to this item.
     * This gets useful if we just want to have a simple click listener on an item.
     */
    fun withClickListener(clickListener: View.OnClickListener?): AdapterItem<AIV> {
        this.clickListener = clickListener
        return this
    }

    /**
     * Define custom margins for this item to be applied when view gets bound.
     */
    open fun withMargins(startMargin: Int = 0, topMargin: Int = 0, endMargin: Int = 0,
                    bottomMargin: Int = 0): AdapterItem<AIV> {
        this.startMargin = startMargin
        this.topMargin = topMargin
        this.endMargin = endMargin
        this.bottomMargin = bottomMargin
        return this
    }

    /**
     * Convenience function of [withMargins] defining only one parameter which will be applied
     * as [startMargin] and [endMargin], leaving [topMargin] and [bottomMargin] as they are
     */
    fun withSideMargins(sideMargins: Int): AdapterItem<AIV> {
        return withMargins(
            startMargin = sideMargins,
            topMargin = this.topMargin,
            endMargin = sideMargins,
            bottomMargin = this.bottomMargin)
    }

    /**
     * Convenience function of [withMargins] defining only one parameter which will be applied
     * as [topMargin] and [bottomMargin], leaving [startMargin] and [endMargin] as they are
     */
    fun withTopBottomMargins(topAndBottomMargins: Int): AdapterItem<AIV> {
        return withMargins(
            startMargin = this.startMargin,
            topMargin = topAndBottomMargins,
            endMargin = this.endMargin,
            bottomMargin = topAndBottomMargins)
    }

    /**
     * Convenience function of [withMargins] defining only one parameter which will be applied
     * as all margins
     */
    fun withAllMargins(margins: Int): AdapterItem<AIV> {
        return withMargins(
            startMargin = margins,
            topMargin = margins,
            endMargin = margins,
            bottomMargin = margins)
    }

    /**
     * Define whether item should be have as a sticky header item.
     * Mind that margin parameters will be ignored if set to true
     */
    fun withStickyHeader(isStickyHeader: Boolean): AdapterItem<AIV> {
        this.isStickyHeader = isStickyHeader
        return this
    }

    /**
     * Define a tag to be set to view bound to this item.
     * This gets useful if we want to retrieve an item from adapter by using
     * [ProperBaseAdapter.getItemByViewTag]
     */
    fun withViewTag(viewTag: Any?): AdapterItem<AIV> {
        this.viewTag = viewTag
        return this
    }
    //endregion

    // called when recycler view calls onBindViewHolder on this item.
    internal fun bind(viewHolder: AdapterViewHolder<View>, position: Int) {
        this.viewHolder = viewHolder as AdapterViewHolder<AIV>
        this.boundPosition = position
        this.viewHolder?.setCallbackListener(this)
        onViewBound(viewHolder.itemView as AIV)
    }

    // Calculate and return view type whenever adapter asks for it.
    // Mind that adapter caches view type ids and associates it with data to minimize recalculations.
    internal fun getViewTypeId(): Int {
        return getViewTypeIdForClass(this::class)
    }
}