package com.vojtkovszky.properbaseadapter

import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.annotation.AnimRes
import androidx.annotation.Px
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlin.reflect.KClass

/**
 * Created by Marcel Vojtkovszky on 2019/07/23.
 */
@Suppress("Unused")
class ProperBaseAdapter constructor(data: MutableList<AdapterItem<*>> = mutableListOf()):
    RecyclerView.Adapter<AdapterViewHolder<View>>() {

    /**
     * To differentiate different types of data in the adapter, we calculate the view type for
     * each added item when adapter asks for it by invoking [getItemViewType]
     * We calculate the type by calculating hash from the qualified name of the class.
     * If this value is true, calculations of view types will be cached, so next time
     * [getItemViewType] is required from the item, it is retrieved from cache rather than calculated
     * again.
     */
    @SuppressWarnings("WeakerAccess")
    var viewTypeCachingEnabled = true

    /**
     * Allows us to properly set layout parameters in case LinearLayout is used, as item uses
     * with to match parent by default when it's layout parameters are built.
     * This isn't so useful if layout manager uses horizontal orientation, so make sure to tell
     * adapter about it.
     */
    var linearLayoutManagerOrientation = RecyclerView.VERTICAL

    // Represents data in the adapter
    private lateinit var data: MutableList<AdapterItem<*>>
    // dataViewTypeIds is always of same size as data
    private var dataViewTypeIds: IntArray = IntArray(0)

    // animation related thingies.
    // default animation to be applied to items in this recycler view, unless item has own animation specified
    @AnimRes var defaultAnimation: Int = 0
    private var lastAnimationPosition: Int = 0

    // default margins
    @Px private var defaultMarginStart: Int = 0
    @Px private var defaultMarginTop: Int = 0
    @Px private var defaultMarginEnd: Int = 0
    @Px private var defaultMarginBottom: Int = 0

    init {
        setItems(newData = data, notifyDataSetChanged = false)
    }

    companion object {
        private const val TAG = "ProperBaseAdapter"
        private const val VIEW_TYPE_ID_UNSET = -1
    }

    //region PUBLIC METHODS
    /**
     * Retrieve instance of [AdapterItem] at a given position.
     * If position is out of bounds, null will be returned
     */
    fun getItemAt(position: Int): AdapterItem<*>? {
        return if (position > data.size || position < 0) null
        else data[position]
    }

    /**
     * Retrieve instance of [AdapterItem] by a given tag or null if no such item is found
     */
    fun getItemByViewTag(viewTag: Any): AdapterItem<*>? {
        for (position in 0 until itemCount) {
            data[position].let {
                if (it.viewTag == viewTag) return it
            }
        }
        return null
    }

    /**
     * Retrieve type of [AdapterItem] at a given position.
     * If position is out of bounds, [Nothing.annotationClass] will be returned
     */
    fun getItemTypeAt(position: Int): KClass<*> {
        return if (position > data.size || position < 0) Nothing::class
        else data[position]::class
    }

    /**
     * Retrieve an indexed position for an [AdapterItem] with a given tag or null if no such item
     * is found
     */
    fun getPositionForItemWithViewTag(viewTag: Any): Int? {
        for (position in 0 until itemCount) {
            data[position].let {
                if (it.viewTag == viewTag) return position
            }
        }
        return null
    }


    /**
     * Add new items at the end of existing data set and define whether
     * [RecyclerView.Adapter.notifyDataSetChanged] should be called afterwards.
     *
     * TODO: Insert should be possible too, just need to look into modifying
     * [addDefaultToDataViewTypeIds] method
     */
    fun addItems(dataObjects: List<AdapterItem<*>>?, notifyItemRangeChanged: Boolean = true) {
        if (dataObjects == null || dataObjects.isEmpty()) {
            return
        }

        val addStartPosition = if (data.isEmpty()) 0 else data.size
        data.addAll(dataObjects)
        addDefaultToDataViewTypeIds(addStartPosition)

        if (notifyItemRangeChanged) {
            notifyItemRangeChanged(addStartPosition, dataObjects.size)
        }
    }

    /**
     * Set items to the the adapter and define whether [RecyclerView.Adapter.notifyDataSetChanged]
     * should be called afterwards.
     */
    fun setItems(newData: MutableList<AdapterItem<*>>, notifyDataSetChanged: Boolean = true) {
        data = newData

        resetDataViewTypeIds()
        resetLastAnimationPosition()

        if (notifyDataSetChanged) {
            notifyDataSetChanged()
        }
    }

    /**
     * Replace data with given items and dispatch changes in adapter only for items that have
     * changed (Based on evaluation from [BaseDiffUtilCallBack])
     */
    fun updateItems(newItems: List<AdapterItem<*>>) {
        val diffResult = DiffUtil.calculateDiff(BaseDiffUtilCallBack(data, newItems), false)

        data.clear()
        data.addAll(newItems)

        resetDataViewTypeIds()
        resetLastAnimationPosition()

        try {
            diffResult.dispatchUpdatesTo(this)
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }

    /**
     * Remove all items from adapter and define whether [RecyclerView.Adapter.notifyDataSetChanged]
     * should be called afterwards.
     */
    fun removeAllItems(notifyDataSetChanged: Boolean = true) {
        val numItems = data.size
        data.clear()

        resetDataViewTypeIds()
        resetLastAnimationPosition()

        if (notifyDataSetChanged) {
            notifyItemRangeRemoved(0, numItems)
        }
    }

    /**
     * Remove items from adapter by providing starting position and amount.
     * Also define whether [RecyclerView.Adapter.notifyDataSetChanged] should be called afterwards.
     */
    fun removeItems(fromPosition: Int, itemCount: Int = 1, notifyDataSetChanged: Boolean = true) {
        if (itemCount <= 0) {
            return
        }

        for (i in 0 until itemCount) {
            data.removeAt(fromPosition+i)
        }

        removeFromDataViewTypeIds(fromPosition, itemCount)
        resetLastAnimationPosition()

        if (notifyDataSetChanged) {
            notifyItemRangeRemoved(fromPosition, itemCount)
        }
    }

    /**
     * Call [RecyclerView.Adapter.notifyItemChanged] for an item matching given tag.
     */
    fun notifyItemWithViewTagChanged(viewTag: Any) {
        for (position in 0 until itemCount) {
            if (viewTag == data[position].viewTag) notifyItemChanged(position)
        }
    }

    /**
     * Determine if adapter contains at least one sticky header item
     */
    fun hasStickyHeaders(): Boolean {
        return data.any { it.isStickyHeader }
    }

    /**
     * Margin to be used on a view when bound, if an item doesn't have defined margin of its own.
     * This is useful if all of the items for example have same side margins, so you don't have
     * to define same margin to each item in an adapter.
     */
    fun setDefaultItemMargins(@Px start: Int = 0, @Px top: Int = 0, @Px end: Int = 0, @Px bottom: Int = 0) {
        this.defaultMarginStart = start
        this.defaultMarginTop = top
        this.defaultMarginEnd = end
        this.defaultMarginBottom = bottom
    }
    //endregion

    //region PARENT ADAPTER METHODS
    override fun getItemViewType(position: Int): Int {
        // if it exists in cache, just return it
        if (hasCachedDataTypeIdForPosition(position)) {
            return dataViewTypeIds[position]
        }
        // calculate id
        data[position].getViewTypeId().let {
            // cache it
            setDataViewTypeIdForPosition(position, it)
            // and return it
            return it
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdapterViewHolder<View> {
        // ask first available adapter item using same view type to generate us a new view.
        val firstItemMatchingViewType = getFirstItemMatchingViewType(viewType)
        return AdapterViewHolder(data[firstItemMatchingViewType].getNewView(parent), viewType)
    }

    override fun onViewAttachedToWindow(holder: AdapterViewHolder<View>) {
        holder.onItemViewAttached()
        super.onViewAttachedToWindow(holder)
    }

    override fun onViewDetachedFromWindow(holder: AdapterViewHolder<View>) {
        holder.onItemViewDetached()
        // if any animations are playing, cancel it
        if (holder.itemView.animation != null) {
            holder.itemView.clearAnimation()
        }
    }

    override fun onViewRecycled(holder: AdapterViewHolder<View>) {
        holder.onItemViewRecycled()
        super.onViewRecycled(holder)
    }

    override fun onFailedToRecycleView(holder: AdapterViewHolder<View>): Boolean {
        holder.onItemViewFailedToRecycle()
        return super.onFailedToRecycleView(holder)
    }

    override fun onBindViewHolder(viewHolder: AdapterViewHolder<View>, position: Int) {
        data[position].let { adapterItem ->
            // Bind it first. This will setup view holder to adapter item with all the callbacks
            // and call onViewBound on adapter item
            adapterItem.bind(viewHolder, position)

            // retrieve params or create new. By default parameters take full available width
            val itemViewLayoutParams =
                if (viewHolder.itemView.layoutParams != null) viewHolder.itemView.layoutParams as RecyclerView.LayoutParams
                else {
                    val width =
                        if (linearLayoutManagerOrientation == RecyclerView.VERTICAL) ViewGroup.LayoutParams.MATCH_PARENT
                        else ViewGroup.LayoutParams.WRAP_CONTENT
                    RecyclerView.LayoutParams(width, ViewGroup.LayoutParams.WRAP_CONTENT)
                }
            // apply margins as defined by adapter item, or global default
            itemViewLayoutParams.setMargins(
                if (adapterItem.marginStart != 0) adapterItem.marginStart else defaultMarginStart,
                if (adapterItem.marginTop != 0) adapterItem.marginTop else defaultMarginTop,
                if (adapterItem.marginEnd != 0) adapterItem.marginEnd else defaultMarginEnd,
                if (adapterItem.marginBottom != 0) adapterItem.marginBottom else defaultMarginBottom
            )

            // set params if not set until now. One might set it during getNewView call in adapter view
            if (viewHolder.itemView.layoutParams == null) {
                viewHolder.itemView.layoutParams = itemViewLayoutParams
            }

            // add generic click listener, if present
            if (adapterItem.clickListener != null) {
                viewHolder.itemView.setOnClickListener(adapterItem.clickListener)
            }

            // tag to view, if present
            if (adapterItem.viewTag != null) {
                viewHolder.itemView.tag = adapterItem.viewTag
            }

            // play animations if item defines any or default one is set
            val animation = if (adapterItem.animation != 0) adapterItem.animation else defaultAnimation
            if (animation != 0) startAnimation(viewHolder.itemView, position, animation)
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }
    //endregion

    //region PRIVATE METHODS
    private fun getFirstItemMatchingViewType(viewType: Int): Int {
        for (i in data.indices) {
            if (viewType == getItemViewType(i)) {
                return i
            }
        }
        return 0
    }

    private fun startAnimation(viewToAnimate: View, position: Int, @AnimRes animationRes: Int) {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastAnimationPosition) {
            AnimationUtils.loadAnimation(viewToAnimate.context, animationRes).let {
                viewToAnimate.startAnimation(it)
            }
            lastAnimationPosition = position
        }
    }

    private fun resetLastAnimationPosition() {
        lastAnimationPosition = -1
    }

    private fun resetDataViewTypeIds() {
        addDefaultToDataViewTypeIds(0)
    }

    private fun addDefaultToDataViewTypeIds(fromPosition: Int) {
        if (!viewTypeCachingEnabled || data.isEmpty() || fromPosition > data.size) {
            return
        }
        // preserve old array if adding
        val oldArray: IntArray? =
            if (fromPosition > dataViewTypeIds.size) dataViewTypeIds.copyOf()
            else null
        // new type ids array has same size as data with unset view types
        dataViewTypeIds = IntArray(data.size) { VIEW_TYPE_ID_UNSET }
        // if old data existed, copy it into beginning of new array, which now has bigger size
        oldArray?.copyInto(dataViewTypeIds)
    }

    private fun removeFromDataViewTypeIds(fromPosition: Int, itemCount: Int) {
        if (!viewTypeCachingEnabled || data.isEmpty() || fromPosition > data.lastIndex || itemCount <= 0) {
            return
        }
        // split and combine, without removed element
        dataViewTypeIds = dataViewTypeIds.copyOfRange(0, fromPosition-1).also {
            if (fromPosition+itemCount <= dataViewTypeIds.lastIndex)
                it.plus(dataViewTypeIds.copyOfRange(fromPosition+itemCount, dataViewTypeIds.lastIndex))
        }
    }

    private fun setDataViewTypeIdForPosition(position: Int, viewTypeId: Int) {
        if (viewTypeCachingEnabled && dataViewTypeIds.size > position) {
            dataViewTypeIds[position] = viewTypeId
        }
    }

    private fun hasCachedDataTypeIdForPosition(position: Int): Boolean {
        return if (viewTypeCachingEnabled)
            dataViewTypeIds.size > position && dataViewTypeIds[position] != VIEW_TYPE_ID_UNSET
        else false
    }
    //endregion
}