package com.cometchat.pro.uikit.ui_resources.utils.recycler_touch

import android.content.Context
import android.graphics.Canvas
import android.view.GestureDetector
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView.OnItemTouchListener
import com.cometchat.pro.uikit.ui_resources.utils.recycler_touch.RecyclerTouchListener.RecyclerItemSwipeListener
import androidx.recyclerview.widget.RecyclerView
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import com.cometchat.pro.uikit.R

class RecyclerTouchListener : ItemTouchHelper.SimpleCallback, OnItemTouchListener {
    private var clickListener: ClickListener? = null
    private var gestureDetector: GestureDetector? = null
    private var swipeListener: RecyclerItemSwipeListener? = null

    constructor(
        recyclerView: RecyclerView?,
        dragDirs: Int,
        swipeDirs: Int,
        listener: RecyclerItemSwipeListener?
    ) : super(dragDirs, swipeDirs) {
        swipeListener = listener
    }

    constructor(var1: Context?, var2: RecyclerView, var3: ClickListener?) : super(0, 0) {
        clickListener = var3
        gestureDetector = GestureDetector(var1, object : SimpleOnGestureListener() {
            override fun onSingleTapUp(var1: MotionEvent): Boolean {
                return true
            }

            override fun onLongPress(var1: MotionEvent) {
                val var2x = var2.findChildViewUnder(var1.x, var1.y)
                if (var2x != null && var3 != null) {
                    var3.onLongClick(var2x, var2.getChildPosition(var2x))
                }
            }
        })
    }

    override fun onInterceptTouchEvent(var1: RecyclerView, var2: MotionEvent): Boolean {
        val var3 = var1.findChildViewUnder(var2.x, var2.y)
        if (var3 != null && clickListener != null && gestureDetector!!.onTouchEvent(var2)) {
            clickListener!!.onClick(var3, var1.getChildPosition(var3))
        }
        return false
    }

    override fun onTouchEvent(var1: RecyclerView, var2: MotionEvent) {}
    override fun onRequestDisallowInterceptTouchEvent(var1: Boolean) {}
    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return true
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        if (viewHolder != null) {
            val foregroundView = viewHolder.itemView.findViewById<View>(R.id.view_foreground)
            if (foregroundView != null) getDefaultUIUtil().onSelected(foregroundView)
        }
    }

    override fun onChildDrawOver(
        c: Canvas, recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float,
        actionState: Int, isCurrentlyActive: Boolean
    ) {
        val foregroundView = viewHolder.itemView.findViewById<View>(R.id.view_foreground)
        if (foregroundView != null) getDefaultUIUtil().onDrawOver(
            c, recyclerView, foregroundView, dX, dY,
            actionState, isCurrentlyActive
        )
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        val foregroundView = viewHolder.itemView.findViewById<View>(R.id.view_foreground)
        if (foregroundView != null) getDefaultUIUtil().clearView(foregroundView)
    }

    override fun onChildDraw(
        c: Canvas, recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float,
        actionState: Int, isCurrentlyActive: Boolean
    ) {
        val foregroundView = viewHolder.itemView.findViewById<View>(R.id.view_foreground)
        if (foregroundView != null) getDefaultUIUtil().onDraw(
            c, recyclerView, foregroundView, dX, dY,
            actionState, isCurrentlyActive
        )
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        swipeListener!!.onSwiped(viewHolder, direction, viewHolder.adapterPosition)
    }

    override fun convertToAbsoluteDirection(flags: Int, layoutDirection: Int): Int {
        return super.convertToAbsoluteDirection(flags, layoutDirection)
    }

    interface RecyclerItemSwipeListener {
        fun onSwiped(viewHolder: RecyclerView.ViewHolder?, direction: Int, position: Int)
    }
}