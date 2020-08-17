package com.vojtkovszky.properbaseadapter.example.items

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.vojtkovszky.properbaseadapter.AdapterItem
import com.vojtkovszky.properbaseadapter.example.R
import kotlinx.android.synthetic.main.section_header_view.view.*

class SectionHeaderItem(private val text: String): AdapterItem<View>() {

    override fun getNewView(parent: ViewGroup): View {
        return LayoutInflater.from(parent.context).inflate(R.layout.section_header_view, parent, false)
    }

    override fun onViewBound(view: View) {
        view.textView.text = text
    }
}