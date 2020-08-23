package com.vojtkovszky.properbaseadapter.example

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.vojtkovszky.properbaseadapter.*
import com.vojtkovszky.properbaseadapter.example.items.ImageViewItem
import com.vojtkovszky.properbaseadapter.example.items.SectionHeaderItem
import com.vojtkovszky.properbaseadapter.example.items.TextViewItem

class MainActivity : AppCompatActivity(), ProperBaseAdapterImplementation {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // let's start
        refreshRecyclerView(
            refreshType = DataDispatchMethod.SET_DATA_AND_REFRESH,
            delayMillis = 500)
    }

    override fun getAdapterData(data: MutableList<AdapterItem<*>>): MutableList<AdapterItem<*>> {
        // let's put an image on top
        data.add(ImageViewItem(ContextCompat.getDrawable(this, android.R.drawable.star_big_on))
            .withTopBottomMargins(resources.getDimensionPixelSize(R.dimen.dp16)))

        // then 10 text items
        for (i in 1..100) {
            // and a sticky header every 10 elements
            if (i % 10 == 0) {
                data.add(SectionHeaderItem("SECTION HEADER ${(i/10)}")
                    .withStickyHeader(true))
            }

            data.add(TextViewItem("Text item $i")
                .withAllMargins(resources.getDimensionPixelSize(R.dimen.dp16))
                .withAnimation(R.anim.item_fall_down)
                .withClickListener(View.OnClickListener {
                    Toast.makeText(this, "Clicked item $i", Toast.LENGTH_SHORT).show()
                }))
        }

        // and another image for the last row
        data.add(ImageViewItem(ContextCompat.getDrawable(this, android.R.drawable.ic_btn_speak_now))
            .withMargins(marginBottom = resources.getDimensionPixelSize(R.dimen.dp16))
            .withViewTag("BOTTOM_IMAGE"))

        return data
    }

    override fun getRecyclerView(): RecyclerView? {
        return findViewById(R.id.recyclerView)
    }

    override fun getNewLayoutManager(): RecyclerView.LayoutManager? {
        return getRecyclerView()?.let {
            LinearLayoutManager(it.context, RecyclerView.VERTICAL, false)
        }
    }

    override fun fadeOutStickyHeaders(): Boolean {
        return true
    }
}
