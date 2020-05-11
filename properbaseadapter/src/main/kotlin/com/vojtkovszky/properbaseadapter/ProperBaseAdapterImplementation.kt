package com.vojtkovszky.properbaseadapter

import android.os.Handler
import android.os.Looper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.lang.Exception

/**
 * Created by Marcel Vojtkovszky on 2020/05/09.
 *
 * Convenient interface to use the library in the most straight forward way.
 * We can, however, choose not to use it and therefore and use ProperBaseAdapter as we please.
 */
interface ProperBaseAdapterImplementation {

    /**
     * Define list of Adapter Items.
     * Supplied argument is a conveniently typed empty list which we add items to and return
     * it as a result in the end.
     */
    fun getAdapterData(data: MutableList<AdapterItem<*>> = mutableListOf()): MutableList<AdapterItem<*>>

    /**
     * Define a recycler view.
     */
    fun getRecyclerView(): RecyclerView?

    /**
     * Retrieve an adapter. Will only exist if RecyclerView is set-up and populated.
     */
    fun getAdapter(): ProperBaseAdapter? {
        return if (getRecyclerView()?.adapter != null && getRecyclerView()?.adapter is ProperBaseAdapter)
            getRecyclerView()?.adapter as ProperBaseAdapter else null
    }

    /**
     * Define a layout manager.
     * Default implementation will use LinearLayoutManager
     */
    fun getLayoutManager(): RecyclerView.LayoutManager? {
        return getRecyclerView()?.let { LinearLayoutManager(it.context) }
    }

    /**
     * Called whenever we want to refresh recycler view.
     * In order to see changes, RecyclerView should not be null at this point
     */
    fun refreshRecyclerView(refreshType: DataDispatchMethod = DataDispatchMethod.DISPATCH_ONLY_CHANGES,
                            delayMillis: Long? = null) {
        if (delayMillis != null) {
            Handler(Looper.getMainLooper()).postDelayed({
                refreshRecyclerView(refreshType = refreshType, delayMillis = null)
            }, delayMillis)
            return
        }

        try {
            getRecyclerView()?.let {
                // set layout manager if not set
                if (it.layoutManager == null) {
                    it.layoutManager = getLayoutManager()
                }

                // setup data to adapter based on adapter's state
                val adapter =
                    if (getAdapter() != null) {
                        it.adapter as ProperBaseAdapter
                    } else {
                        ProperBaseAdapter(getAdapterData())
                    }
                when (refreshType) {
                    DataDispatchMethod.DISPATCH_ONLY_CHANGES -> adapter.updateItems(getAdapterData())
                    DataDispatchMethod.SET_DATA_AND_REFRESH -> adapter.setItems(getAdapterData(), true)
                    DataDispatchMethod.SET_DATA_ONLY -> adapter.setItems(getAdapterData(), false)
                }

                // set adapter to recycler view if not set
                if (it.adapter == null) {
                    it.adapter = adapter
                }
            }
        } catch (ignore: Exception) {
            // views may no longer be valid, simply let it fail
        }
    }
}