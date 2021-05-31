package com.vojtkovszky.properbaseadapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.AnimRes
import androidx.annotation.LayoutRes
import androidx.annotation.Px
import androidx.recyclerview.widget.RecyclerView
import kotlin.reflect.KClass

/**
 * Created by Marcel Vojtkovszky on 2019/07/23.
 *
 * Represents an item in adapter.
 * It contains both a view holder and logic to populate or manipulate the view.
 */
@Suppress("UNCHECKED_CAST")
abstract class AdapterItem<AIV: View> : AdapterViewHolder.OnCallbackListener<AIV> {

    private var viewHolder: AdapterViewHolder<AIV>? = null
    private var boundPosition = RecyclerView.NO_POSITION // position when bind is called

    @AnimRes
    internal var animation: Int = 0  // animation when view will get displayed
    internal var isStickyHeader: Boolean = false   // sticky header option.
    internal var clickListener: View.OnClickListener? = null  // generic click listener
    internal var viewTag: Any? = null  // allows to attach a tag to item
    // allow margins to be set additionally. Note that margins are not compatible with
    // isStickyHeader property and will always be 0 if isStickyHeader is set to true
    @Px
    internal var marginStart = 0
        get() = if (isStickyHeader) 0 else field
    @Px
    internal var marginTop = 0
        get() = if (isStickyHeader) 0 else field
    @Px
    internal var marginEnd = 0
        get() = if (isStickyHeader) 0 else field
    @Px
    internal var marginBottom = 0
        get() = if (isStickyHeader) 0 else field

    companion object {
        /**
         * Used in case we want to determine view type id from outside
         * the adapter context for comparison.
         * Handy when using different layout manager
         */
        fun getViewTypeIdForClass(clazz: KClass<*>): Int {
            var h = 0
            clazz.qualifiedName?.let {
                for (element in it) {
                    h = 31 * h + element.code
                }
            }
            return h
        }

        /**
         * Convenience function to return inflated view from layout.
         * Commonly used for [getNewView] when using own custom layout
         */
        fun getViewFromLayout(parent: ViewGroup, @LayoutRes layoutRes: Int): View {
            return LayoutInflater.from(parent.context).inflate(layoutRes, parent, false)
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
    fun setMargins(@Px start: Int = 0, @Px top: Int = 0, @Px end: Int = 0, @Px bottom: Int = 0) {
        withMargins(start, top, end, bottom)
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
    open fun withMargins(@Px start: Int? = null,
                         @Px top: Int? = null,
                         @Px end: Int? = null,
                         @Px bottom: Int? = null
    ): AdapterItem<AIV> {
        this.marginStart = start ?: this.marginStart
        this.marginTop = top ?: this.marginTop
        this.marginEnd = end ?: this.marginEnd
        this.marginBottom = bottom ?: this.marginBottom
        return this
    }

    /**
     * Convenience function of [withMargins] defining only one parameter which will be applied
     * as [marginStart] and [marginEnd], leaving [marginTop] and [marginBottom] as they are
     */
    fun withSideMargins(@Px startAndEnd: Int): AdapterItem<AIV> {
        return withMargins(start = startAndEnd, top = this.marginTop, end = startAndEnd, bottom = this.marginBottom)
    }

    /**
     * Convenience function of [withMargins] defining only one parameter which will be applied
     * as [marginTop] and [marginBottom], leaving [marginStart] and [marginEnd] as they are
     */
    fun withTopBottomMargins(@Px topAndBottom: Int): AdapterItem<AIV> {
        return withMargins(start = this.marginStart, top = topAndBottom, end = this.marginEnd, bottom = topAndBottom)
    }

    /**
     * Convenience function of [withMargins] defining only one parameter which will be applied
     * to all margins
     */
    fun withAllMargins(@Px all: Int): AdapterItem<AIV> {
        return withMargins(start = all, top = all, end = all, bottom = all)
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