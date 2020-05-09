package com.vojtkovszky.properbaseadapter

import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.annotation.AnimRes
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlin.reflect.KClass

/**
 * Created by Marcel Vojtkovszky on 2019/07/23.
 */
class ProperBaseAdapter constructor(data: MutableList<AdapterItem<*>> = mutableListOf()):
    RecyclerView.Adapter<AdapterViewHolder<View>>() {

    // cache view type ids
    var viewTypeCachingEnabled = true

    // Represents data in the adapter
    private lateinit var data: MutableList<AdapterItem<*>>
    // Represents cache of view type ids from our data.
    // That's because we calculate view type ids by calculating hash from type name and since retrieving type id is a
    // common operation, we ask for it only once for each instance of data.
    // dataViewTypeIds is always of same size as data
    private var dataViewTypeIds: IntArray = IntArray(0)

    // animation related thingies.
    // default animation to be applied to items in this recycler view, unless item has own animation specified
    @AnimRes var defaultAnimation: Int = 0
    private var lastAnimationPosition: Int = 0

    init {
        setItems(newData = data, notifyDataSetChanged = false)
    }

    companion object {
        private const val TAG = "ProperBaseAdapter"
        private const val VIEW_TYPE_ID_UNSET = -1
    }
    //-------------------
    // [END CONSTRUCT]
    //-------------------


    //------------------------
    // [BEGIN PUBLIC METHODS]
    //------------------------
    fun getItemAt(position: Int): AdapterItem<*>? {
        return if (position > data.size) null
        else data[position]
    }

    fun getItemTypeAt(position: Int): KClass<*> {
        return if (position > data.size) Nothing::class
        else data[position]::class
    }

    fun getItemByViewTag(viewTag: Any): AdapterItem<*>? {
        for (position in 0 until itemCount) {
            data[position].let {
                if (it.viewTag == viewTag) return it
            }
        }
        return null
    }

    fun getPositionWithItemWithViewTag(viewTag: Any): Int? {
        for (position in 0 until itemCount) {
            data[position].let {
                if (it.viewTag == viewTag) return position
            }
        }
        return null
    }

    fun setItems(newData: MutableList<AdapterItem<*>>, notifyDataSetChanged: Boolean = true) {
        data = newData

        resetDataViewTypeIds()
        resetLastAnimationPosition()

        if (notifyDataSetChanged) {
            notifyDataSetChanged()
        }
    }

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

    fun removeAllItems(notifyDataSetChanged: Boolean = true) {
        val numItems = data.size
        data.clear()

        resetDataViewTypeIds()
        resetLastAnimationPosition()

        if (notifyDataSetChanged) {
            notifyItemRangeRemoved(0, numItems)
        }
    }

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

    fun notifyItemWithViewTagChanged(viewTag: Any) {
        for (position in 0 until itemCount) {
            if (viewTag == data[position].viewTag) notifyItemChanged(position)
        }
    }
    //------------------------
    // [END PUBLIC METHODS]
    //------------------------


    //--------------------------------
    // [BEGIN PARENT ADAPTER METHODS]
    //--------------------------------
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
        // ask first available adapter item using same view type for a new view as
        // the adapter item knows details about it.
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
        holder.onItemViewRecycleFailed()
        return super.onFailedToRecycleView(holder)
    }

    override fun onBindViewHolder(viewHolder: AdapterViewHolder<View>, position: Int) {
        data[position].let { adapterItem ->
            // Bind it first. This will setup view holder to adapter item with all the callbacks
            // and call onViewBound on adapter item
            adapterItem.bind(viewHolder, position)

            // retrieve params or create new
            val itemViewLayoutParams =
                if (viewHolder.itemView.layoutParams != null) viewHolder.itemView.layoutParams as RecyclerView.LayoutParams
                else RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            // apply margins as defined by adapter item
            itemViewLayoutParams.setMargins(adapterItem.startMargin, adapterItem.topMargin,
                adapterItem.endMargin, adapterItem.bottomMargin)
            // set params if not set until now. One might set it during getNewView call in adapter view
            if (viewHolder.itemView.layoutParams == null) {
                viewHolder.itemView.layoutParams = itemViewLayoutParams
            }
            adapterItem.layoutParamsInitialized = true

            // add generic click listener, if present
            if (adapterItem.clickListener != null) {
                viewHolder.itemView.setOnClickListener(adapterItem.clickListener)
            }

            // tag to view, if present
            if (adapterItem.viewTag != null) {
                viewHolder.itemView.tag = adapterItem.viewTag
            }

            // play animations if item defines any or default one is set
            if (adapterItem.animation != 0 || defaultAnimation != 0) {
                startAnimation(
                    viewHolder.itemView,
                    position,
                    if (adapterItem.animation != 0) adapterItem.animation else defaultAnimation
                )
            }
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }
    //--------------------------------
    // [END PARENT ADAPTER METHODS]
    //--------------------------------


    //--------------------------
    // [BEGIN PRIVATE METHODS]
    //--------------------------
    private fun getFirstItemMatchingViewType(viewType: Int): Int {
        for (i in data.indices) {
            if (viewType == getItemViewType(i)) {
                return i
            }
        }
        return 0
    }

    private fun startAnimation(viewToAnimate: View, @AnimRes animationRes: Int, position: Int) {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastAnimationPosition) {
            val animation = AnimationUtils.loadAnimation(viewToAnimate.context, animationRes)
            viewToAnimate.startAnimation(animation)
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
}