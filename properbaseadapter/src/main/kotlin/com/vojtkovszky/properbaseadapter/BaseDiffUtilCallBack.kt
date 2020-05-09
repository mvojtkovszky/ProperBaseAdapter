package com.vojtkovszky.properbaseadapter

import androidx.recyclerview.widget.DiffUtil

/**
 * Created by Marcel Vojtkovszky on 2019/07/23.
 */
internal class BaseDiffUtilCallBack(
    private val oldList: List<AdapterItem<*>>,
    private val newList: List<AdapterItem<*>>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return try {
            oldList[oldItemPosition]::class == newList[oldItemPosition]::class
        } catch (e: IndexOutOfBoundsException) {
            false
        }
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}