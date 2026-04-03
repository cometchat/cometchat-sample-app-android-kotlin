package com.cometchat.uikit.kotlin.shared.resources.utils.unread_message_decoration

import android.graphics.Canvas
import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cometchat.uikit.kotlin.shared.resources.utils.sticky_header.StickyHeaderAdapter

/**
 * RecyclerView ItemDecoration that displays a "New Messages" indicator above the first unread message.
 *
 * This decoration works with adapters implementing [NewMessageIndicatorDecorationAdapter] to:
 * - Draw a separator view above the message marked as the unread anchor
 * - Handle proper positioning when combined with sticky date headers
 * - Cache created views for performance
 *
 * The indicator is shown when [setUnreadMessageId] is called with a valid message ID,
 * and the decoration will draw the indicator above the message with that ID.
 *
 * @param adapter The adapter implementing [NewMessageIndicatorDecorationAdapter]
 */
class NewMessageIndicatorDecoration<T : RecyclerView.ViewHolder>(
    private val adapter: NewMessageIndicatorDecorationAdapter<T>
) : RecyclerView.ItemDecoration() {

    private var unreadMessageId: Long = -1
    private val headerCache: MutableMap<Long, RecyclerView.ViewHolder> = mutableMapOf()
    private val stickyHeaderCache: MutableMap<Long, RecyclerView.ViewHolder> = mutableMapOf()

    /**
     * Sets the message ID that marks the first unread message.
     * The "New Messages" indicator will be drawn above this message.
     *
     * @param messageId The ID of the first unread message, or -1 to hide the indicator
     */
    fun setUnreadMessageId(messageId: Long) {
        this.unreadMessageId = messageId
        headerCache.clear()
        stickyHeaderCache.clear()
    }

    /**
     * Gets the current unread message ID.
     */
    fun getUnreadMessageId(): Long = unreadMessageId

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = getPosition(parent, view)
        if (position == RecyclerView.NO_POSITION) return

        if (hasHeader(position)) {
            val header = getHeader(parent, position).itemView
            outRect.top += header.measuredHeight
        } else {
            outRect.top += 0
        }
    }

    private fun getPosition(parent: RecyclerView, view: View): Int {
        var position = parent.getChildAdapterPosition(view)
        if (position == RecyclerView.NO_POSITION) {
            position = parent.getChildLayoutPosition(view)
        }
        return position
    }

    private fun hasHeader(position: Int): Boolean {
        val message = adapter.getNewMessageIndicatorId(position)
        return message != null && message.id == unreadMessageId
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val childCount = parent.childCount
        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)
            val position = getPosition(parent, child)

            if (position == RecyclerView.NO_POSITION) continue

            if (hasHeader(position)) {
                val header = getHeader(parent, position).itemView

                c.save()
                val left = child.left
                val top = getHeaderTop(parent, child, header, position, i)

                c.translate(left.toFloat(), top.toFloat())

                if (header.isLayoutRequested) {
                    header.layout(0, 0, header.measuredWidth, header.measuredHeight)
                }

                header.draw(c)
                c.restore()
            }
        }
    }

    private fun getHeaderTop(
        parent: RecyclerView,
        child: View,
        header: View,
        adapterPos: Int,
        layoutPos: Int
    ): Int {
        var top = child.y.toInt() - header.height

        // If the adapter also implements StickyHeaderAdapter, account for sticky headers
        if (adapter is StickyHeaderAdapter<*>) {
            @Suppress("UNCHECKED_CAST")
            val stickyAdapter = adapter as StickyHeaderAdapter<RecyclerView.ViewHolder>
            if (hasStickyHeader(stickyAdapter, adapterPos)) {
                val stickyHeaderId = stickyAdapter.getHeaderId(adapterPos)
                val stickyHolder: RecyclerView.ViewHolder

                if (stickyHeaderCache.containsKey(stickyHeaderId)) {
                    stickyHolder = stickyHeaderCache[stickyHeaderId]!!
                } else {
                    stickyHolder = stickyAdapter.onCreateHeaderViewHolder(parent)
                    val stickyView = stickyHolder.itemView
                    stickyAdapter.onBindHeaderViewHolder(stickyHolder, adapterPos, stickyHeaderId)

                    val widthSpec = View.MeasureSpec.makeMeasureSpec(
                        parent.measuredWidth,
                        View.MeasureSpec.EXACTLY
                    )
                    val heightSpec = View.MeasureSpec.makeMeasureSpec(
                        parent.measuredHeight,
                        View.MeasureSpec.UNSPECIFIED
                    )

                    val childWidth = ViewGroup.getChildMeasureSpec(
                        widthSpec,
                        parent.paddingLeft + parent.paddingRight,
                        stickyView.layoutParams?.width ?: ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    val childHeight = ViewGroup.getChildMeasureSpec(
                        heightSpec,
                        parent.paddingTop + parent.paddingBottom,
                        stickyView.layoutParams?.height ?: ViewGroup.LayoutParams.WRAP_CONTENT
                    )

                    stickyView.measure(childWidth, childHeight)
                    stickyHeaderCache[stickyHeaderId] = stickyHolder
                }

                top -= stickyHolder.itemView.measuredHeight
            }
        }
        return top
    }

    private fun hasStickyHeader(adapter: StickyHeaderAdapter<*>, position: Int): Boolean {
        if (position == 0) return true
        val currentHeaderId = adapter.getHeaderId(position)
        val previousHeaderId = adapter.getHeaderId(position - 1)
        return currentHeaderId != -1L && currentHeaderId != previousHeaderId
    }

    @Suppress("UNCHECKED_CAST")
    private fun getHeader(parent: RecyclerView, position: Int): RecyclerView.ViewHolder {
        if (headerCache.containsKey(unreadMessageId)) {
            return headerCache[unreadMessageId]!!
        } else {
            val holder = adapter.onCreateNewMessageViewHolder(parent) as RecyclerView.ViewHolder
            val header = holder.itemView
            if (header.layoutParams == null) {
                header.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }
            adapter.onBindNewMessageViewHolder(holder as T, position, unreadMessageId)

            val widthSpec = View.MeasureSpec.makeMeasureSpec(
                parent.measuredWidth,
                View.MeasureSpec.EXACTLY
            )
            val heightSpec = View.MeasureSpec.makeMeasureSpec(
                parent.measuredHeight,
                View.MeasureSpec.UNSPECIFIED
            )

            val childWidth = ViewGroup.getChildMeasureSpec(
                widthSpec,
                parent.paddingLeft + parent.paddingRight,
                header.layoutParams.width
            )
            val childHeight = ViewGroup.getChildMeasureSpec(
                heightSpec,
                parent.paddingTop + parent.paddingBottom,
                header.layoutParams.height
            )

            header.measure(childWidth, childHeight)
            header.layout(0, 0, header.measuredWidth, header.measuredHeight)
            headerCache[unreadMessageId] = holder
            return holder
        }
    }
}
