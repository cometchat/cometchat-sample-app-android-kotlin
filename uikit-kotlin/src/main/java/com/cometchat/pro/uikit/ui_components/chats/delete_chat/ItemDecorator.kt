package com.cometchat.pro.uikit.ui_components.chats.delete_chat

import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.TextPaint
import android.util.TypedValue
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView


data class ItemDecorator(
    var canvas: Canvas, var recyclerView: RecyclerView, var viewHolder: RecyclerView.ViewHolder,
    var dX: Float, var actionState: Int
) {
    @ColorInt
    private var mBgColorFromStartToEnd =
        ContextCompat.getColor(recyclerView.context, android.R.color.transparent)

    @ColorInt
    private var mBgColorFromEndToStart =
        ContextCompat.getColor(recyclerView.context, android.R.color.transparent)

    @ColorInt
    private var mIconTintFromStartToEnd: Int? = null

    @ColorInt
    private var mIconTintFromEndToStart: Int? = null


    @ColorInt
    private var mTextColorFromStartToEnd = Color.DKGRAY

    @ColorInt
    private var mTextColorFromEndToStart = Color.DKGRAY

    @DrawableRes
    private var mIconResIdFromStartToEnd = 0

    @DrawableRes
    private var mIconResIdFromEndToStart = 0

    private var mTextFromStartToEnd: String? = null
    private var mTextFromEndToStart: String? = null
    private var mTypefaceFromStartToEnd = Typeface.SANS_SERIF
    private var mTypefaceFromEndToStart = Typeface.SANS_SERIF

    private var mTextSizeFromStartToEnd = 14f
    private var mTextSizeFromEndToStart = 14f

    /* Default values */
    private var mDefaultIconHorizontalMargin = 0
    private var mDefaultTextUnit = TypedValue.COMPLEX_UNIT_SP

    init {
        mDefaultIconHorizontalMargin = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            16f,
            recyclerView.context.resources.displayMetrics
        ).toInt()
    }
    class Builder(
        canvas: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        actionState: Int
    ) {
        private val mDecorator = ItemDecorator(canvas, recyclerView, viewHolder, dX, actionState)


        fun setDefaultBgColor(@ColorInt color: Int): Builder {
            mDecorator.mBgColorFromStartToEnd = color
            mDecorator.mBgColorFromEndToStart = color
            return this
        }

        fun setDefaultIcon(@DrawableRes resourceId: Int): Builder {
            mDecorator.mIconResIdFromStartToEnd = resourceId
            mDecorator.mIconResIdFromEndToStart = resourceId
            return this
        }

        fun setDefaultIconTintColor(@ColorInt color: Int): Builder {
            mDecorator.mIconTintFromStartToEnd = color
            mDecorator.mIconTintFromEndToStart = color
            return this
        }

        fun setDefaultText(text: String?): Builder {
            mDecorator.mTextFromStartToEnd = text
            mDecorator.mTextFromEndToStart = text
            return this
        }
        fun setDefaultTextColor(@ColorInt color: Int): Builder {
            mDecorator.mTextColorFromStartToEnd = color
            mDecorator.mTextColorFromEndToStart = color
            return this
        }

        fun setDefaultTextSize(unit: Int = TypedValue.COMPLEX_UNIT_SP, size: Float): Builder {
            mDecorator.mDefaultTextUnit = unit
            mDecorator.mTextSizeFromStartToEnd = size
            mDecorator.mTextSizeFromEndToStart = size
            return this
        }


        fun setDefaultTypeFace(typeface: Typeface): Builder {
            mDecorator.mTypefaceFromStartToEnd = typeface
            mDecorator.mTypefaceFromEndToStart = typeface
            return this
        }

        fun setIconHorizontalMargin(
            unit: Int = TypedValue.COMPLEX_UNIT_DIP,
            iconHorizontalMargin: Int
        ): Builder {
            mDecorator.mDefaultIconHorizontalMargin = TypedValue.applyDimension(
                unit,
                iconHorizontalMargin.toFloat(),
                mDecorator.recyclerView.context.resources.displayMetrics
            ).toInt()
            return this
        }

        fun setFromStartToEndBgColor(@ColorInt color: Int): Builder {
            mDecorator.mBgColorFromStartToEnd = color
            return this
        }

        fun setFromEndToStartBgColor(@ColorInt color: Int): Builder {
            mDecorator.mBgColorFromEndToStart = color
            return this
        }

        fun setFromStartToEndIcon(@DrawableRes drawableId: Int): Builder {
            mDecorator.mIconResIdFromStartToEnd = drawableId
            return this
        }

        fun setFromEndToStartIcon(@DrawableRes drawableId: Int): Builder {
            mDecorator.mIconResIdFromEndToStart = drawableId
            return this
        }
        fun setFromStartToEndIconTint(@ColorInt tintColor: Int): Builder {
            mDecorator.mIconTintFromStartToEnd = tintColor
            return this
        }

        fun setFromEndToStartIconTint(@ColorInt tintColor: Int): Builder {
            mDecorator.mIconTintFromEndToStart = tintColor
            return this
        }

        fun setFromStartToEndText(text: String?): Builder {
            mDecorator.mTextFromStartToEnd = text
            return this
        }

        fun setFromEndToStartText(text: String?): Builder {
            mDecorator.mTextFromEndToStart = text
            return this
        }

        fun setFromStartToEndTextColor(@ColorInt color: Int): Builder {
            mDecorator.mTextColorFromStartToEnd = color
            return this
        }

        fun setFromEndToStartTextColor(@ColorInt color: Int): Builder {
            mDecorator.mTextColorFromEndToStart = color
            return this
        }

        fun setFromStartToEndTextSize(
            unit: Int = TypedValue.COMPLEX_UNIT_SP,
            size: Float
        ): Builder {
            mDecorator.mDefaultTextUnit = unit
            mDecorator.mTextSizeFromStartToEnd = size
            return this
        }

        fun setFromEndToStartTextSize(
            unit: Int = TypedValue.COMPLEX_UNIT_SP,
            size: Float
        ): Builder {
            mDecorator.mDefaultTextUnit = unit
            mDecorator.mTextSizeFromEndToStart = size
            return this
        }

        fun setFromStartToEndTypeface(typeface: Typeface): Builder {
            mDecorator.mTypefaceFromStartToEnd = typeface
            return this
        }

        fun setFromEndToStartTypeface(typeface: Typeface): Builder {
            mDecorator.mTypefaceFromEndToStart = typeface
            return this
        }

        fun create(): ItemDecorator = mDecorator
    }

    private fun Drawable.colorFilter(@ColorInt tintColor: Int) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            this.setColorFilter(tintColor, PorterDuff.Mode.MULTIPLY)
        } else {
            this.colorFilter = BlendModeColorFilter(tintColor, BlendMode.SRC_ATOP)
        }
    }

    fun decorate() {
        if (actionState != ItemTouchHelper.ACTION_STATE_SWIPE) return
        if (dX > 0) fromStartToEndBehavior() else fromEndToStartBehavior()
    }

    private fun fromStartToEndBehavior() {
        canvas.clipRect(
            viewHolder.itemView.left,
            viewHolder.itemView.top,
            viewHolder.itemView.left + dX.toInt(),
            viewHolder.itemView.bottom
        )
        // Draws a color drawable on the canvas, with the same size as the canvas
        if (mBgColorFromStartToEnd != 0) {
            val cvBackgroundColor = ColorDrawable(mBgColorFromStartToEnd)
            cvBackgroundColor.bounds = canvas.clipBounds
            cvBackgroundColor.draw(canvas)
        }
        // Draws the icon contextualizing the swipe action
        var iconSize = 0
        if (mIconResIdFromStartToEnd != 0 && dX > mDefaultIconHorizontalMargin) {
            val icon = ContextCompat.getDrawable(
                recyclerView.context, mIconResIdFromStartToEnd
            )
            icon?.let {
                iconSize = it.intrinsicHeight
                val halfIcon = iconSize / 2
                val top =
                    viewHolder.itemView.top +
                            ((viewHolder.itemView.bottom - viewHolder.itemView.top) / 2 - halfIcon)
                it.setBounds(
                    viewHolder.itemView.left + mDefaultIconHorizontalMargin,
                    top,
                    viewHolder.itemView.left + mDefaultIconHorizontalMargin + it.intrinsicWidth,
                    top + iconSize
                )
                mIconTintFromStartToEnd?.let { iconTintColor ->
                    it.colorFilter(iconTintColor)
                }
                it.draw(canvas)
            }
        }
        // Draws the descriptive text contextualizing the swipe action
        mTextFromStartToEnd?.let {
            if (dX > mDefaultIconHorizontalMargin + iconSize) {
                val textPaint = TextPaint()
                textPaint.isAntiAlias = true
                textPaint.textSize = TypedValue.applyDimension(
                    mDefaultTextUnit,
                    mTextSizeFromStartToEnd,
                    recyclerView.context.resources.displayMetrics
                )
                textPaint.color = mTextColorFromStartToEnd
                textPaint.typeface = mTypefaceFromStartToEnd
                val textTop =
                    (viewHolder.itemView.top + (viewHolder.itemView.bottom - viewHolder.itemView.top) / 2.0 + textPaint.textSize / 2).toInt()
                canvas.drawText(
                    it,
                    viewHolder.itemView.left + mDefaultIconHorizontalMargin + iconSize + (if (iconSize > 0) mDefaultIconHorizontalMargin / 2 else 0).toFloat(),
                    textTop.toFloat(),
                    textPaint
                )
            }
        }
    }

    private fun fromEndToStartBehavior() {
        canvas.clipRect(
            viewHolder.itemView.right + dX.toInt(),
            viewHolder.itemView.top,
            viewHolder.itemView.right,
            viewHolder.itemView.bottom
        )
        // Draws a color drawable on the canvas, with the same size as the canvas
        if (mBgColorFromEndToStart != 0) {
            val cvBackgroundColor = ColorDrawable(mBgColorFromEndToStart)
            cvBackgroundColor.bounds = canvas.clipBounds
            cvBackgroundColor.draw(canvas)
        }
        // Draws the icon contextualizing the swipe action
        var imgEnd = viewHolder.itemView.right
        var iconSize = 0
        if (mIconResIdFromEndToStart != 0 && dX < -mDefaultIconHorizontalMargin) {
            val icon = ContextCompat.getDrawable(
                recyclerView.context, mIconResIdFromEndToStart
            )
            icon?.let {
                iconSize = it.intrinsicHeight
                val halfIcon = iconSize / 2
                val top =
                    viewHolder.itemView.top +
                            ((viewHolder.itemView.bottom - viewHolder.itemView.top) / 2 - halfIcon)
                imgEnd = viewHolder.itemView.right - mDefaultIconHorizontalMargin - iconSize * 2
                it.setBounds(
                    imgEnd,
                    top,
                    viewHolder.itemView.right - mDefaultIconHorizontalMargin - iconSize,
                    top + it.intrinsicHeight
                )
                mIconTintFromEndToStart?.let { iconTintColor ->
                    it.colorFilter(iconTintColor)
                }
                it.draw(canvas)
            }

        }
        // Draws the descriptive text contextualizing the swipe action
        mTextFromEndToStart?.let {
            if (dX < -mDefaultIconHorizontalMargin - iconSize) {
                val textPaint = TextPaint()
                textPaint.isAntiAlias = true
                textPaint.textSize = TypedValue.applyDimension(
                    mDefaultTextUnit,
                    mTextSizeFromEndToStart,
                    recyclerView.context.resources.displayMetrics
                )
                textPaint.color = mTextColorFromEndToStart
                textPaint.typeface = mTypefaceFromEndToStart
                val width = textPaint.measureText(it)
                val textTop =
                    (viewHolder.itemView.top + (viewHolder.itemView.bottom - viewHolder.itemView.top) / 2.0 + textPaint.textSize / 2.0).toInt()

                canvas.drawText(
                    it,
                    imgEnd - width - if (imgEnd == viewHolder.itemView.right) mDefaultIconHorizontalMargin else mDefaultIconHorizontalMargin / 2,
                    textTop.toFloat(),
                    textPaint
                )
            }
        }
    }
}