package com.vojtkovszky.properbaseadapter

/**
 * Created by Marcel Vojtkovszky on 2019/07/30.
 */
enum class DataDispatchMethod {
    /**
     * Will use diff utils to dispatch changes only to items that changed.
     * Items affected depend on equals implementation to be correct
     */
    DISPATCH_ONLY_CHANGES,
    /**
     * Will set new data and call notifyDataSetChanged(), causing all items to update
     */
    SET_DATA_AND_REFRESH,
    /**
     * Will set new data only and no data set notification request will be dispatched to adapter
     */
    SET_DATA_ONLY
}