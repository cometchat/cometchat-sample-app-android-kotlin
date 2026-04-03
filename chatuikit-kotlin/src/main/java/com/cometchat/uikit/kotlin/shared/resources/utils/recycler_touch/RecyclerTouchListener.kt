package com.cometchat.uikit.kotlin.shared.resources.utils.recycler_touch

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * Touch listener for RecyclerView that handles click and long-click events.
 * 
 * @param context The context
 * @param recyclerView The RecyclerView to attach the listener to
 * @param clickListener The listener for click events
 */
class RecyclerTouchListener(
    context: Context,
    private val recyclerView: RecyclerView,
    private val clickListener: ClickListener
) : RecyclerView.OnItemTouchListener {

    private val gestureDetector: GestureDetector

    init {
        gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent): Boolean {
                return true
            }

            override fun onLongPress(e: MotionEvent) {
                val childView = recyclerView.findChildViewUnder(e.x, e.y)
                if (childView != null) {
                    clickListener.onLongClick(
                        childView,
                        recyclerView.getChildAdapterPosition(childView)
                    )
                }
            }
        })
    }

    override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
        val childView = rv.findChildViewUnder(e.x, e.y)
        if (childView != null && gestureDetector.onTouchEvent(e)) {
            clickListener.onClick(childView, rv.getChildAdapterPosition(childView))
        }
        return false
    }

    override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {
        // Not used
    }

    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
        // Not used
    }
}

/**
 * Interface for handling click events on RecyclerView items.
 */
interface ClickListener {
    /**
     * Called when an item is clicked.
     * 
     * @param view The clicked view
     * @param position The position of the item
     */
    fun onClick(view: View, position: Int)

    /**
     * Called when an item is long-clicked.
     * 
     * @param view The long-clicked view
     * @param position The position of the item
     */
    fun onLongClick(view: View, position: Int)
}
