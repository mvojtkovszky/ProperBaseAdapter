package com.vojtkovszky.properbaseadapter.example

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

import com.vojtkovszky.properbaseadapter.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), BaseRecyclerViewImplementation {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        refreshRecyclerView()
    }

    override fun getAdapterData(data: MutableList<AdapterItem<*>>): MutableList<AdapterItem<*>> {
        data.add(
            ImageViewRecyclerItem(ContextCompat.getDrawable(this, android.R.drawable.btn_radio))
                .withMargins(
                    topMargin = resources.getDimensionPixelSize(R.dimen.dp16),
                    endMargin = resources.getDimensionPixelSize(R.dimen.dp16)))
        for (i in 1..10) {
            data.add(
                TextViewRecyclerItem("Text item $i")
                    .withMargins(
                        startMargin = resources.getDimensionPixelSize(R.dimen.dp16),
                        topMargin = resources.getDimensionPixelSize(R.dimen.dp8),
                        endMargin = resources.getDimensionPixelSize(R.dimen.dp16),
                        bottomMargin = resources.getDimensionPixelSize(R.dimen.dp8))
                    //.withAnimation(android.R.anim.fade_in)
                    .withClickListener(View.OnClickListener {
                        Toast.makeText(this, "Clicked item $i", Toast.LENGTH_SHORT).show()
                    }))
        }
        data.add(ImageViewRecyclerItem(ContextCompat.getDrawable(this, android.R.drawable.ic_btn_speak_now)))

        return data
    }

    override fun getRecyclerView(): RecyclerView? {
        return recyclerView
    }
}
