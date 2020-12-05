package com.vojtkovszky.properbaseadapter

import android.os.Handler
import android.os.Looper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * Created by Marcel Vojtkovszky on 2020/05/09.
 *
 * Convenient interface to use the library in the most straight forward way by implementing it
 * in a View, Fragment or Activity.
 *
 * We can, however, choose not to use it and therefore and use ProperBaseAdapter in full manual mode.
 * In that case we have to manually construct the adapter and data, set it to RecyclerView, define
 * LayoutManager and all that.
 */
interface ProperBaseAdapterImplementation {

    /**
     * Retrieve an adapter. Will only exist if RecyclerView is set-up and populated.
     */
    fun getAdapter(): ProperBaseAdapter? {
        return if (adapterExistsAndSet()) getRecyclerView()?.adapter as ProperBaseAdapter else null
    }

    /**
     * Define list of Adapter Items.
     * Supplied argument is a conveniently typed empty list which we add items to and return
     * it as a result in the end.
     */
    fun getAdapterData(data: MutableList<AdapterItem<*>> = mutableListOf()): MutableList<AdapterItem<*>>

    /**
     * Define a layout manager.
     * Default implementation will use LinearLayoutManager
     */
    fun getNewLayoutManager(): RecyclerView.LayoutManager? {
        return getRecyclerView()?.let { LinearLayoutManager(it.context) }
    }

    /**
     * Define a recycler view.
     * Allows for it to be null, in case views are not yet set In this case, nothing will happen.
     */
    fun getRecyclerView(): RecyclerView?

    /**
     * Called whenever we want to refresh recycler view.
     * In order to see changes, RecyclerView should not be null at this point
     *
     * @param refreshType see [DataDispatchMethod]
     * @param waitUntilRecyclerViewLaidDown determine if we should wait until RecyclerView is laid down
     * before refreshing data by calling [RecyclerView.post]
     * @param delayMillis refresh with a delay, in ms
     */
    fun refreshRecyclerView(refreshType: DataDispatchMethod = DataDispatchMethod.DISPATCH_ONLY_CHANGES,
                            waitUntilRecyclerViewLaidDown: Boolean = false,
                            delayMillis: Long? = null) {
        // handle delay
        if (delayMillis != null) {
            Handler(Looper.getMainLooper()).postDelayed({
                refreshRecyclerView(
                    refreshType = refreshType,
                    waitUntilRecyclerViewLaidDown = false,
                    delayMillis = null)
            }, delayMillis)
            return
        }

        // trigger populate, either directly or wait until recycler view laid down
        getRecyclerView()?.let {
            if (waitUntilRecyclerViewLaidDown) {
                it.post { setupAndPopulateRecyclerView(it, refreshType) }
            } else {
                setupAndPopulateRecyclerView(it, refreshType)
            }
        }
    }

    /**
     * Define sticky header behaviour when one sticky header is in the process of replacing another.
     * Defaults to [Boolean.false]. If set to [Boolean.true], it will apply a fade effect.
     * Only applies if adapter contains sticky headers to begin with.
     */
    fun fadeOutStickyHeaders(): Boolean {
        return false
    }

    // determine if current recycler view has adapter set and this adapter is ProperBaseAdapter
    private fun adapterExistsAndSet(): Boolean {
        return getRecyclerView()?.adapter != null && getRecyclerView()?.adapter is ProperBaseAdapter
    }

    // setup adapter to recycler view and populate it
    private fun setupAndPopulateRecyclerView(recyclerView: RecyclerView, refreshType: DataDispatchMethod) {
        // set layout manager if not set
        if (recyclerView.layoutManager == null) {
            recyclerView.layoutManager = getNewLayoutManager()
        }

        // setup adapters
        val adapter = getAdapter() ?: ProperBaseAdapter().also {
            // allows us to properly set default layout parameters in case LinearLayout is used
            if (recyclerView.layoutManager is LinearLayoutManager) {
                it.linearLayoutManagerOrientation = (recyclerView.layoutManager as LinearLayoutManager).orientation
            }
        }

        // different behaviour based on refresh type
        when (refreshType) {
            DataDispatchMethod.DISPATCH_ONLY_CHANGES -> adapter.updateItems(getAdapterData())
            DataDispatchMethod.SET_DATA_AND_REFRESH -> adapter.setItems(getAdapterData(), true)
            DataDispatchMethod.SET_DATA_ONLY -> adapter.setItems(getAdapterData(), false)
        }

        // add support for sticky headers if at least one item supports it
        if (adapter.hasStickyHeaders() && recyclerView.itemDecorationCount == 0) {
            recyclerView.addItemDecoration(StickyHeaderItemDecoration(recyclerView, fadeOutStickyHeaders()) {
                adapter.getItemAt(it)?.isStickyHeader == true
            })
        }

        // set adapter to recycler view if not set
        if (recyclerView.adapter == null) {
            recyclerView.adapter = adapter
        }
    }
}