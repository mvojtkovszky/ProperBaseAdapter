package com.vojtkovszky.properbaseadapter.example.items

import android.view.ViewGroup
import android.widget.TextView
import com.vojtkovszky.properbaseadapter.AdapterItem

class TextViewItem(private val text: String): AdapterItem<TextView>() {

    override fun getNewView(parent: ViewGroup): TextView {
        return TextView(parent.context)
    }

    override fun onViewBound(view: TextView) {
        view.text = text
    }
}