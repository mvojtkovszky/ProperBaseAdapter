package com.vojtkovszky.properbaseadapter.example

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

import com.vojtkovszky.properbaseadapter.*

class MainActivity : AppCompatActivity(), ProperBaseAdapterImplementation {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // let's start
        refreshRecyclerView(
            refreshType = DataDispatchMethod.SET_DATA_AND_REFRESH,
            delayMillis = 1000)
    }

    override fun getAdapterData(data: MutableList<AdapterItem<*>>): MutableList<AdapterItem<*>> {
        // let's put an image on top
        data.add(ImageViewRecyclerItem(ContextCompat.getDrawable(this, android.R.drawable.btn_radio))
            .withMargins(
                topMargin = resources.getDimensionPixelSize(R.dimen.dp16),
                bottomMargin = resources.getDimensionPixelSize(R.dimen.dp16)))

        // then 10 text items
        for (i in 1..10) {
            data.add(TextViewRecyclerItem("Text item $i")
                .withMargins(
                    startMargin = resources.getDimensionPixelSize(R.dimen.dp16),
                    topMargin = resources.getDimensionPixelSize(R.dimen.dp8),
                    endMargin = resources.getDimensionPixelSize(R.dimen.dp16),
                    bottomMargin = resources.getDimensionPixelSize(R.dimen.dp8))
                .withAnimation(R.anim.item_fall_down)
                .withClickListener(View.OnClickListener {
                    Toast.makeText(this, "Clicked item $i", Toast.LENGTH_SHORT).show()
                }))
        }

        // and another image for the last row
        data.add(ImageViewRecyclerItem(ContextCompat.getDrawable(this, android.R.drawable.ic_btn_speak_now))
            .withViewTag("BOTTOM_IMAGE"))

        return data
    }

    override fun getRecyclerView(): RecyclerView? {
        return findViewById(R.id.recyclerView)
    }
}
