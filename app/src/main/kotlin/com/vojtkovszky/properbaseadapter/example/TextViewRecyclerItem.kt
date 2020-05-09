package com.vojtkovszky.properbaseadapter.example

import android.view.ViewGroup
import android.widget.TextView
import com.vojtkovszky.properbaseadapter.AdapterItem

class TextViewRecyclerItem(private val text: String): AdapterItem<TextView>() {

    override fun getNewView(parent: ViewGroup): TextView {
        return TextView(parent.context)
    }

    override fun onViewBound(view: TextView) {
        view.text = text
    }
}