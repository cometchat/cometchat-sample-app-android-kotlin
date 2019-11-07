package com.inscripts.cometchatpulse.Helpers

import android.content.Context
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.ItemTouchHelper



open class CardItemTouchHelper(val context: Context,val icon: Drawable, val backgroundColor:Int)
    : ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.LEFT) {
    override fun onSwiped(p0: androidx.recyclerview.widget.RecyclerView.ViewHolder, p1: Int) {

    }

    private val intrinsicWidth:Int
    private val intrinsicHeight:Int
    private val background:ColorDrawable
    private val clearPaint:Paint

    init {
        intrinsicWidth = icon.intrinsicWidth
        intrinsicHeight = icon.intrinsicHeight
        background=ColorDrawable()
        clearPaint = Paint()
    }


    override fun onMove(p0: androidx.recyclerview.widget.RecyclerView, p1: androidx.recyclerview.widget.RecyclerView.ViewHolder, p2: androidx.recyclerview.widget.RecyclerView.ViewHolder): Boolean {
       return false
    }


    override fun onChildDraw(c: Canvas, recyclerView: androidx.recyclerview.widget.RecyclerView, viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {

        val itemView = viewHolder.itemView
        val itemHeight = itemView.bottom - itemView.top
        val isCanceled = dX == 0f && !isCurrentlyActive

        if (isCanceled) {
            clearCanvas(c, itemView.right + dX, itemView.top.toFloat(), itemView.right.toFloat(), itemView.bottom.toFloat())
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            return
        }

        background.color = backgroundColor
        background.setBounds(itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom)
        background.draw(c)

        // Calculate position of icon
        val IconTop = itemView.top + (itemHeight - intrinsicHeight) / 2
        val IconMargin = (itemHeight - intrinsicHeight) / 2
        val IconLeft = itemView.right - IconMargin - intrinsicWidth
        val IconRight = itemView.right -IconMargin
        val IconBottom = IconTop + intrinsicHeight

        // Draw the  icon
        icon.setBounds(IconLeft, IconTop, IconRight, IconBottom)
        icon.draw(c)

        super.onChildDraw(c, recyclerView, viewHolder, dX/2.5f, dY, actionState, isCurrentlyActive)
    }


    private fun clearCanvas(c: Canvas?, left: Float, top: Float, right: Float, bottom: Float) {
        c?.drawRect(left, top, right, bottom, clearPaint)
    }


}