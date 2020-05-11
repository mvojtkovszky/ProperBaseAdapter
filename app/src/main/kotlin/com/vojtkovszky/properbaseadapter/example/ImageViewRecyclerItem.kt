package com.vojtkovszky.properbaseadapter.example

import android.graphics.drawable.Drawable
import android.view.ViewGroup
import android.widget.ImageView
import com.vojtkovszky.properbaseadapter.AdapterItem

class ImageViewRecyclerItem(private val drawable: Drawable?): AdapterItem<ImageView>() {

    // here we define how view is created
    override fun getNewView(parent: ViewGroup): ImageView {
        return ImageView(parent.context)
    }

    // here we define how view can look like
    override fun onViewBound(view: ImageView) {
        view.setImageDrawable(drawable)
        view.scaleType = ImageView.ScaleType.CENTER
    }
}