package com.vojtkovszky.properbaseadapter.sticky

import android.graphics.Canvas
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.RelativeLayout
import androidx.recyclerview.widget.RecyclerView

class StickyHeaderItemDecoration(
    recyclerView: RecyclerView,
    private val listener: StickyHeaderInterface
) : RecyclerView.ItemDecoration() {

    private val headerContainer = FrameLayout(recyclerView.context)
    private var stickyHeaderHeight: Int = 0
    private var currentHeader: View? = null
    private var currentHeaderPosition = 0

    init {
        val layout = RelativeLayout(recyclerView.context)
        val params = recyclerView.layoutParams
        val parent = recyclerView.parent as ViewGroup
        val index = parent.indexOfChild(recyclerView)
        parent.addView(layout, index, params)
        parent.removeView(recyclerView)
        layout.addView(recyclerView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        layout.addView(headerContainer, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(c, parent, state)

        val topChild = parent.getChildAt(0) ?: return

        val topChildPosition = parent.getChildAdapterPosition(topChild)
        if (topChildPosition == RecyclerView.NO_POSITION) {
            return
        }

        val currentHeader = getHeaderViewForItem(topChildPosition, parent)
        fixLayoutSize(parent, currentHeader)
        val contactPoint = currentHeader.bottom
        val childInContact = getChildInContact(parent, contactPoint) ?: return

        val nextPosition = parent.getChildAdapterPosition(childInContact)
        if (listener.isHeader(nextPosition)) {
            moveHeader(currentHeader, childInContact, topChildPosition, nextPosition)
            return
        }

        drawHeader(currentHeader, topChildPosition)
    }

    private fun getHeaderViewForItem(itemPosition: Int, parent: RecyclerView): View {
        val headerPosition = listener.getHeaderPositionForItem(itemPosition)
        val layoutResId = listener.getHeaderLayout(headerPosition)
        val header = LayoutInflater.from(parent.context).inflate(layoutResId, parent, false)
        listener.bindHeaderData(header, headerPosition)
        return header
    }

    private fun drawHeader(header: View, position: Int) {
        headerContainer.layoutParams.height = stickyHeaderHeight
        setCurrentHeader(header, position)
    }

    private fun moveHeader(currentHead: View, nextHead: View, currentPos: Int, nextPos: Int) {
        val marginTop = nextHead.top - currentHead.height
        if (currentHeaderPosition == nextPos && currentPos != nextPos) setCurrentHeader(currentHead, currentPos)

        val params = currentHeader?.layoutParams as? ViewGroup.MarginLayoutParams ?: return
        params.setMargins(0, marginTop, 0, 0)
        currentHeader?.layoutParams = params

        headerContainer.layoutParams.height = stickyHeaderHeight + marginTop
    }

    private fun setCurrentHeader(header: View, position: Int) {
        currentHeader = header
        currentHeaderPosition = position
        headerContainer.removeAllViews()
        headerContainer.addView(currentHeader)
    }

    private fun getChildInContact(parent: RecyclerView, contactPoint: Int): View? =
        (0 until parent.childCount)
            .map { parent.getChildAt(it) }
            .firstOrNull { it.bottom > contactPoint && it.top <= contactPoint }

    private fun fixLayoutSize(parent: ViewGroup, view: View) {

        val widthSpec = View.MeasureSpec.makeMeasureSpec(parent.width, View.MeasureSpec.EXACTLY)
        val heightSpec = View.MeasureSpec.makeMeasureSpec(parent.height, View.MeasureSpec.UNSPECIFIED)

        val childWidthSpec = ViewGroup.getChildMeasureSpec(widthSpec,
            parent.paddingLeft + parent.paddingRight,
            view.layoutParams.width)
        val childHeightSpec = ViewGroup.getChildMeasureSpec(heightSpec,
            parent.paddingTop + parent.paddingBottom,
            view.layoutParams.height)

        view.measure(childWidthSpec, childHeightSpec)

        stickyHeaderHeight = view.measuredHeight
        view.layout(0, 0, view.measuredWidth, stickyHeaderHeight)
    }

    interface StickyHeaderInterface {

        fun getHeaderPositionForItem(itemPosition: Int): Int

        fun getHeaderLayout(headerPosition: Int): Int

        fun bindHeaderData(header: View, headerPosition: Int)

        fun isHeader(itemPosition: Int): Boolean
    }
}