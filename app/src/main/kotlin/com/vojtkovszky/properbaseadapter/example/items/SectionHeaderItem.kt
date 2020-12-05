package com.vojtkovszky.properbaseadapter.example.items

import android.view.View
import android.view.ViewGroup
import com.vojtkovszky.properbaseadapter.AdapterItem
import com.vojtkovszky.properbaseadapter.example.R
import com.vojtkovszky.properbaseadapter.example.databinding.SectionHeaderViewBinding

class SectionHeaderItem(private val text: String): AdapterItem<View>() {

    override fun getNewView(parent: ViewGroup): View {
        return getViewFromLayout(parent, R.layout.section_header_view)
    }

    override fun onViewBound(view: View) {
        SectionHeaderViewBinding.bind(view).apply {
            textView.text = text
        }
    }
}