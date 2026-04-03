package com.cometchat.uikit.kotlin.shared.resources.utils.sticky_header

import android.graphics.Canvas
import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

/**
 * ItemDecoration that draws sticky headers on top of a RecyclerView.
 *
 * Headers are drawn using canvas operations and remain fixed at the top
 * while scrolling through items belonging to that header group.
 *
 * @param adapter The adapter implementing StickyHeaderAdapter
 * @param renderInline If true, headers don't take up space (drawn over items)
 */
class StickyHeaderDecoration(
    private val adapter: StickyHeaderAdapter<RecyclerView.ViewHolder>,
    private val renderInline: Boolean = false
) : RecyclerView.ItemDecoration() {

    private val headerCache = mutableMapOf<Long, RecyclerView.ViewHolder>()

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        var headerHeight = 0

        if (position != RecyclerView.NO_POSITION && hasHeader(position) && showHeaderAboveItem(position)) {
            val header = getHeader(parent, position).itemView
            headerHeight = getHeaderHeightForLayout(header)
        }

        outRect.set(0, headerHeight, 0, 0)
    }

    override fun onDrawOver(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val childCount = parent.childCount
        var previousHeaderId = -1L

        for (layoutPosition in 0 until childCount) {
            val child = parent.getChildAt(layoutPosition)
            val adapterPosition = parent.getChildAdapterPosition(child)

            if (adapterPosition != RecyclerView.NO_POSITION && hasHeader(adapterPosition)) {
                val headerId = adapter.getHeaderId(adapterPosition)

                if (headerId != previousHeaderId) {
                    previousHeaderId = headerId
                    val header = getHeader(parent, adapterPosition).itemView

                    canvas.save()
                    // Center the header horizontally
                    val left = (parent.width - header.width) / 2
                    val top = getHeaderTop(parent, child, header, adapterPosition, layoutPosition)
                    canvas.translate(left.toFloat(), top.toFloat())
                    header.translationX = left.toFloat()
                    header.translationY = top.toFloat()
                    header.draw(canvas)
                    canvas.restore()
                }
            }
        }
    }


    /**
     * Clears the header cache. Call this when adapter data changes.
     */
    fun clearHeaderCache() {
        headerCache.clear()
    }

    private fun hasHeader(position: Int): Boolean {
        return adapter.getHeaderId(position) != StickyHeaderAdapter.NO_HEADER_ID
    }

    private fun showHeaderAboveItem(position: Int): Boolean {
        if (position == 0) return true
        return adapter.getHeaderId(position - 1) != adapter.getHeaderId(position)
    }

    private fun getHeader(parent: RecyclerView, position: Int): RecyclerView.ViewHolder {
        val headerId = adapter.getHeaderId(position)

        headerCache[headerId]?.let { return it }

        val holder = adapter.onCreateHeaderViewHolder(parent)
        val headerView = holder.itemView

        adapter.onBindHeaderViewHolder(holder, position, headerId)

        // Measure and layout the header
        val widthSpec = View.MeasureSpec.makeMeasureSpec(parent.measuredWidth, View.MeasureSpec.EXACTLY)
        val heightSpec = View.MeasureSpec.makeMeasureSpec(parent.measuredHeight, View.MeasureSpec.UNSPECIFIED)

        val childWidthSpec = ViewGroup.getChildMeasureSpec(
            widthSpec,
            parent.paddingLeft + parent.paddingRight,
            headerView.layoutParams?.width ?: ViewGroup.LayoutParams.MATCH_PARENT
        )
        val childHeightSpec = ViewGroup.getChildMeasureSpec(
            heightSpec,
            parent.paddingTop + parent.paddingBottom,
            headerView.layoutParams?.height ?: ViewGroup.LayoutParams.WRAP_CONTENT
        )

        headerView.measure(childWidthSpec, childHeightSpec)
        headerView.layout(0, 0, headerView.measuredWidth, headerView.measuredHeight)

        headerCache[headerId] = holder
        return holder
    }

    private fun getHeaderTop(
        parent: RecyclerView,
        child: View,
        header: View,
        adapterPosition: Int,
        layoutPosition: Int
    ): Int {
        val headerHeight = getHeaderHeightForLayout(header)
        var top = child.y.toInt() - headerHeight

        if (layoutPosition == 0) {
            val childCount = parent.childCount
            val currentHeaderId = adapter.getHeaderId(adapterPosition)

            // Check if next header is pushing current header up
            for (i in 1 until childCount) {
                val nextAdapterPosition = parent.getChildAdapterPosition(parent.getChildAt(i))
                if (nextAdapterPosition != RecyclerView.NO_POSITION) {
                    val nextHeaderId = adapter.getHeaderId(nextAdapterPosition)
                    if (nextHeaderId != currentHeaderId) {
                        val nextChild = parent.getChildAt(i)
                        val nextHeaderHeight = getHeader(parent, nextAdapterPosition).itemView.height
                        val offset = nextChild.y.toInt() - (headerHeight + nextHeaderHeight)
                        if (offset < 0) {
                            return offset
                        }
                        break
                    }
                }
            }

            // Keep header at top
            top = maxOf(0, top)
        }

        return top
    }

    private fun getHeaderHeightForLayout(header: View): Int {
        return if (renderInline) 0 else header.height
    }
}
