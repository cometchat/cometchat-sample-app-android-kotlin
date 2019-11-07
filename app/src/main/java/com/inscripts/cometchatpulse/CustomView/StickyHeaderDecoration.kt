package com.inscripts.cometchatpulse.CustomView

import android.graphics.Canvas
import android.graphics.Rect
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.inscripts.cometchatpulse.ViewHolder.TextHeaderHolder
import java.util.HashMap

class StickyHeaderDecoration  constructor(private val mAdapter: StickyHeaderAdapter<TextHeaderHolder>
     ) : androidx.recyclerview.widget.RecyclerView.ItemDecoration() {

    private val mHeaderCache: MutableMap<Long, androidx.recyclerview.widget.RecyclerView.ViewHolder>
    private val mRenderInline: Boolean = false
    init {
        this.mHeaderCache = HashMap()
    }

    override fun getItemOffsets(var1: Rect, var2: View, var3: androidx.recyclerview.widget.RecyclerView, var4: androidx.recyclerview.widget.RecyclerView.State) {
        val var5 = var3.getChildAdapterPosition(var2)
        var var6 = 0
        if (var5 != -1 && this.hasHeader(var5) && this.showHeaderAboveItem(var5)) {
            val var7 = this.getHeader(var3, var5).itemView
            var6 = this.getHeaderHeightForLayout(var7)
        }

        var1.set(0, var6, 0, 0)
    }

    private fun showHeaderAboveItem(var1: Int): Boolean {
        return if (var1 == 0) {
            true
        } else {
            this.mAdapter.getHeaderId(var1 - 1) !== this.mAdapter.getHeaderId(var1)
        }
    }

    fun clearHeaderCache() {
        this.mHeaderCache.clear()
    }

    fun findHeaderViewUnder(var1: Float, var2: Float): View? {
        val var3 = this.mHeaderCache.values.iterator()

        var var5: View
        var var6: Float
        var var7: Float
        do {
            if (!var3.hasNext()) {
                return null
            }

            var5 = var3.next().itemView
            var6 = ViewCompat.getTranslationX(var5)
            var7 = ViewCompat.getTranslationY(var5)
        } while (var1 < var5.left.toFloat() + var6 || var1 > var5.right.toFloat() + var6 || var2 < var5.top.toFloat() + var7 || var2 > var5.bottom.toFloat() + var7)

        return var5
    }

    private fun hasHeader(var1: Int): Boolean {
        return this.mAdapter.getHeaderId(var1) !== -1L
    }

    private fun getHeader(var1: androidx.recyclerview.widget.RecyclerView, var2: Int): androidx.recyclerview.widget.RecyclerView.ViewHolder {
        val var3 = this.mAdapter.getHeaderId(var2)
        if (this.mHeaderCache.containsKey(var3)) {
            return this.mHeaderCache[var3] as androidx.recyclerview.widget.RecyclerView.ViewHolder
        } else {
            val var5 = this.mAdapter.onCreateHeaderViewHolder(var1)
            val var6 = var5.itemView
            this.mAdapter.onBindHeaderViewHolder(var5, var2, var3)
            val var7 = View.MeasureSpec.makeMeasureSpec(var1.measuredWidth, 1073741824)
            val var8 = View.MeasureSpec.makeMeasureSpec(var1.measuredHeight, 0)
            val var9 = ViewGroup.getChildMeasureSpec(var7, var1.paddingLeft + var1.paddingRight, var6.getLayoutParams().width)
            val var10 = ViewGroup.getChildMeasureSpec(var8, var1.paddingTop + var1.paddingBottom, var6.getLayoutParams().height)
            var6.measure(var9, var10)
            var6.layout(0, 0, var6.getMeasuredWidth(), var6.getMeasuredHeight())
            this.mHeaderCache[var3] = var5
            return var5
        }
    }

    override fun onDrawOver(var1: Canvas, var2: androidx.recyclerview.widget.RecyclerView, var3: androidx.recyclerview.widget.RecyclerView.State) {
        val var4 = var2.childCount
        var var5 = -1L

        for (var7 in 0 until var4) {
            val var8 = var2.getChildAt(var7)
            val var9 = var2.getChildAdapterPosition(var8)
            if (var9 != -1 && this.hasHeader(var9)) {
                val var10 = this.mAdapter.getHeaderId(var9)
                if (var10 != var5) {
                    var5 = var10
                    val var12 = this.getHeader(var2, var9).itemView
                    var1.save()
                    val var13 = var8.left
                    val var14 = this.getHeaderTop(var2, var8, var12, var9, var7)
                    var1.translate(var13.toFloat(), var14.toFloat())
                    var12.translationX = var13.toFloat()
                    var12.translationY = var14.toFloat()
                    var12.draw(var1)
                    var1.restore()
                }
            }
        }

    }

    private fun getHeaderTop(var1: androidx.recyclerview.widget.RecyclerView, var2: View, var3: View, var4: Int, var5: Int): Int {
        val var6 = this.getHeaderHeightForLayout(var3)
        var var7 = var2.y.toInt() - var6
        if (var5 == 0) {
            val var8 = var1.childCount
            val var9 = this.mAdapter.getHeaderId(var4)

            for (var11 in 1 until var8) {
                val var12 = var1.getChildAdapterPosition(var1.getChildAt(var11))
                if (var12 != -1) {
                    val var13 = this.mAdapter.getHeaderId(var12)
                    if (var13 != var9) {
                        val var15 = var1.getChildAt(var11)
                        val var16 = var15.y.toInt() - (var6 + this.getHeader(var1, var12).itemView.height)
                        if (var16 < 0) {
                            return var16
                        }
                        break
                    }
                }
            }

            var7 = Math.max(0, var7)
        }

        return var7
    }

    private fun getHeaderHeightForLayout(var1: View): Int {
        return if (this.mRenderInline) 0 else var1.height
    }

    companion object {
        val NO_HEADER_ID = -1L
    }
}