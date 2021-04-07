package com.cometchat.pro.uikit.ui_resources.utils.sticker_header

import android.graphics.Canvas
import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import kotlin.collections.HashMap

class StickyHeaderDecoration @JvmOverloads constructor(private val mAdapter: StickyHeaderAdapter<*>, var2: Boolean = false) : RecyclerView.ItemDecoration() {
    private val mHeaderCache: MutableMap<Long?, RecyclerView.ViewHolder?>
    private val mRenderInline: Boolean
    override fun getItemOffsets(var1: Rect, var2: View, var3: RecyclerView, var4: RecyclerView.State) {
        val var5 = var3.getChildAdapterPosition(var2)
        var var6 = 0
        if (var5 != -1 && hasHeader(var5) && showHeaderAboveItem(var5)) {
            val var7 = getHeader(var3, var5)!!.itemView
            var6 = getHeaderHeightForLayout(var7)
        }
        var1[0, var6, 0] = 0
    }

    private fun showHeaderAboveItem(var1: Int): Boolean {
        return if (var1 == 0) {
            true
        } else {
            mAdapter.getHeaderId(var1 - 1) != mAdapter.getHeaderId(var1)
        }
    }

    fun clearHeaderCache() {
        mHeaderCache.clear()
    }

    fun findHeaderViewUnder(var1: Float, var2: Float): View? {
        val var3: Iterator<*> = mHeaderCache.values.iterator()
        var var5: View
        var var6: Float
        var var7: Float
        do {
            if (!var3.hasNext()) {
                return null
            }
            val var4 = var3.next() as RecyclerView.ViewHolder
            var5 = var4.itemView
            var6 = ViewCompat.getTranslationX(var5)
            var7 = ViewCompat.getTranslationY(var5)
        } while (var1 < var5.left.toFloat() + var6 || var1 > var5.right.toFloat() + var6 || var2 < var5.top.toFloat() + var7 || var2 > var5.bottom.toFloat() + var7)
        return var5
    }

    private fun hasHeader(var1: Int): Boolean {
        return mAdapter.getHeaderId(var1) != -1L
    }

    private fun getHeader(var1: RecyclerView, var2: Int): RecyclerView.ViewHolder? {
        val var3 = mAdapter.getHeaderId(var2)
        return if (mHeaderCache.containsKey(var3)) {
            mHeaderCache[var3]
        } else {
            val var5 = mAdapter.onCreateHeaderViewHolder(var1)
            val var6 = var5!!.itemView
            mAdapter.onBindHeaderViewHolder(var5, var2, var3)
            val var7 = View.MeasureSpec.makeMeasureSpec(var1.measuredWidth, View.MeasureSpec.getMode(1073741824))
            val var8 = View.MeasureSpec.makeMeasureSpec(var1.measuredHeight, View.MeasureSpec.getMode(0))
            val var9 = ViewGroup.getChildMeasureSpec(var7, var1.paddingLeft + var1.paddingRight, var6.layoutParams.width)
            val var10 = ViewGroup.getChildMeasureSpec(var8, var1.paddingTop + var1.paddingBottom, var6.layoutParams.height)
            var6.measure(var9, var10)
            var6.layout(0, 0, var6.measuredWidth, var6.measuredHeight)
            mHeaderCache[var3] = var5
            var5
        }
    }

    override fun onDrawOver(var1: Canvas, var2: RecyclerView, var3: RecyclerView.State) {
        val var4 = var2.childCount
        var var5 = -1L
        for (var7 in 0 until var4) {
            val var8 = var2.getChildAt(var7)
            val var9 = var2.getChildAdapterPosition(var8)
            if (var9 != -1 && hasHeader(var9)) {
                val var10 = mAdapter.getHeaderId(var9)
                if (var10 != var5) {
                    var5 = var10
                    val var12 = getHeader(var2, var9)!!.itemView
                    var1.save()
                    val var13 = var8.left
                    val var14 = getHeaderTop(var2, var8, var12, var9, var7)
                    var1.translate(var13.toFloat(), var14.toFloat())
                    var12.translationX = var13.toFloat()
                    var12.translationY = var14.toFloat()
                    var12.draw(var1)
                    var1.restore()
                }
            }
        }
    }

    private fun getHeaderTop(var1: RecyclerView, var2: View, var3: View, var4: Int, var5: Int): Int {
        val var6 = getHeaderHeightForLayout(var3)
        var var7 = var2.y.toInt() - var6
        if (var5 == 0) {
            val var8 = var1.childCount
            val var9 = mAdapter.getHeaderId(var4)
            for (var11 in 1 until var8) {
                val var12 = var1.getChildAdapterPosition(var1.getChildAt(var11))
                if (var12 != -1) {
                    val var13 = mAdapter.getHeaderId(var12)
                    if (var13 != var9) {
                        val var15 = var1.getChildAt(var11)
                        val var16 = var15.y.toInt() - (var6 + getHeader(var1, var12)!!.itemView.height)
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
        return if (mRenderInline) 0 else var1.height
    }

    companion object {
        const val NO_HEADER_ID = -1L
    }

    init {
        mHeaderCache = HashMap()
        mRenderInline = var2
    }
}