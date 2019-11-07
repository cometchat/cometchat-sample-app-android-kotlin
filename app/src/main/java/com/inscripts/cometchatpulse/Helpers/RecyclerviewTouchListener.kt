package com.inscripts.cometchatpulse.Helpers

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View

class RecyclerviewTouchListener(var1: Context, var2: androidx.recyclerview.widget.RecyclerView, private val clickListener: ClickListener?) :
        androidx.recyclerview.widget.RecyclerView.OnItemTouchListener {

    private val gestureDetector: GestureDetector

    init {
        this.gestureDetector = GestureDetector(var1, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapUp(var1: MotionEvent): Boolean {
                return true
            }

            override fun onLongPress(var1: MotionEvent) {
                val var2x = var2.findChildViewUnder(var1.x, var1.y)
                if (var2x != null && clickListener != null) {
                    clickListener!!.onLongClick(var2x, var2.getChildPosition(var2x))
                }

            }
        })
    }

    override fun onInterceptTouchEvent(var1: androidx.recyclerview.widget.RecyclerView, var2: MotionEvent): Boolean {
        val var3 = var1.findChildViewUnder(var2.x, var2.y)
        if (var3 != null && this.clickListener != null && this.gestureDetector.onTouchEvent(var2)) {
            this.clickListener.onClick(var3, var1.getChildPosition(var3))
        }

        return false
    }

    override fun onTouchEvent(var1: androidx.recyclerview.widget.RecyclerView, var2: MotionEvent) {}

    override fun onRequestDisallowInterceptTouchEvent(var1: Boolean) {}

    interface ClickListener {
        fun onClick(var1: View, var2: Int)
        fun onLongClick(var1: View?, var2: Int)
    }
}