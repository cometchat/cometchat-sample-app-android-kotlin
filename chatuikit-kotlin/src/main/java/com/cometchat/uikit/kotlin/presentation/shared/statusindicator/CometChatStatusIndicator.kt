package com.cometchat.uikit.kotlin.presentation.shared.statusindicator

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.StyleRes
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.shared.resources.utils.Utils
import com.google.android.material.card.MaterialCardView

/**
 * CometChatStatusIndicator is a custom MaterialCardView that represents the status indicator.
 * It provides methods to customize the appearance of the status indicator such as setting
 * the stroke color, stroke width, corner radius, background color, and background image.
 */
class CometChatStatusIndicator @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    companion object {
        private val TAG = CometChatStatusIndicator::class.java.simpleName
    }

    // Single style object - NO individual style properties
    private var style: CometChatStatusIndicatorStyle = CometChatStatusIndicatorStyle()

    private var statusIndicator: StatusIndicator = StatusIndicator.ONLINE
    private val imageView: ImageView

    init {
        Utils.initMaterialCard(this)
        imageView = ImageView(context)
        addView(imageView)
        
        if (!isInEditMode) {
            applyStyleAttributes(attrs, defStyleAttr)
        }
    }

    /**
     * Applies style attributes from XML using the style class factory method.
     */
    private fun applyStyleAttributes(attrs: AttributeSet?, defStyleAttr: Int) {
        var typedArray = context.theme.obtainStyledAttributes(
            attrs, R.styleable.CometChatStatusIndicator, defStyleAttr, 0
        )
        val styleResId = typedArray.getResourceId(
            R.styleable.CometChatStatusIndicator_cometchatStatusIndicatorStyle, 0
        )
        typedArray.recycle()
        
        typedArray = context.theme.obtainStyledAttributes(
            attrs, R.styleable.CometChatStatusIndicator, defStyleAttr, styleResId
        )
        // fromTypedArray handles recycling internally
        style = CometChatStatusIndicatorStyle.fromTypedArray(context, typedArray)
        applyStyle()
    }

    /**
     * Applies all style properties from the style object to views.
     */
    private fun applyStyle() {
        if (style.strokeColor != 0) applyStrokeColor(style.strokeColor)
        if (style.strokeWidth != 0f) applyStrokeWidth(style.strokeWidth)
        if (style.cornerRadius != 0f) applyCornerRadius(style.cornerRadius)
        updateStatusIndicatorDisplay()
    }

    // ========================================
    // Private Apply Methods
    // ========================================

    private fun applyStrokeColor(@ColorInt color: Int) {
        strokeColor = color
    }

    private fun applyStrokeWidth(@Dimension width: Float) {
        strokeWidth = width.toInt()
    }

    private fun applyCornerRadius(@Dimension radius: Float) {
        this.radius = radius
    }

    private fun setStatus(drawable: Drawable?) {
        if (drawable == null) {
            visibility = GONE
        } else {
            visibility = VISIBLE
            setStatusIndicatorBackgroundImage(drawable)
        }
    }

    private fun updateStatusIndicatorDisplay() {
        when (statusIndicator) {
            StatusIndicator.ONLINE -> setStatus(style.onlineIcon)
            StatusIndicator.OFFLINE -> setStatus(null)
            StatusIndicator.PUBLIC_GROUP -> setStatus(null)
            StatusIndicator.PRIVATE_GROUP -> setStatus(style.privateGroupIcon)
            StatusIndicator.PROTECTED_GROUP -> setStatus(style.protectedGroupIcon)
        }
    }

    fun setStatusIndicatorBackgroundImage(image: Drawable?) {
        image?.let { imageView.setImageDrawable(it) }
    }

    // ========================================
    // Public Style Methods
    // ========================================

    /**
     * Sets the style from a style resource.
     */
    fun setStyle(@StyleRes styleRes: Int) {
        if (styleRes != 0) {
            val typedArray = context.theme.obtainStyledAttributes(styleRes, R.styleable.CometChatStatusIndicator)
            // fromTypedArray handles recycling internally
            setStyle(CometChatStatusIndicatorStyle.fromTypedArray(context, typedArray))
        }
    }

    /**
     * Sets the style from a CometChatStatusIndicatorStyle object.
     */
    fun setStyle(style: CometChatStatusIndicatorStyle) {
        this.style = style
        applyStyle()
    }

    // ========================================
    // Getters (read from style object)
    // ========================================

    fun getStatusIndicatorStrokeWidth(): Float = style.strokeWidth

    fun getStatusIndicatorStrokeColor(): Int = style.strokeColor

    fun getStatusIndicatorCornerRadius(): Float = style.cornerRadius

    fun getStatusIndicatorOnlineIcon(): Drawable? = style.onlineIcon

    fun getStatusIndicatorPrivateGroupIcon(): Drawable? = style.privateGroupIcon

    fun getStatusIndicatorProtectedGroupIcon(): Drawable? = style.protectedGroupIcon

    // ========================================
    // Setters (update style object + apply)
    // ========================================

    fun setStatusIndicatorStrokeWidth(@Dimension width: Float) {
        style = style.copy(strokeWidth = width)
        applyStrokeWidth(width)
    }

    fun setStatusIndicatorStrokeColor(@ColorInt color: Int) {
        style = style.copy(strokeColor = color)
        applyStrokeColor(color)
    }

    fun setStatusIndicatorCornerRadius(@Dimension radius: Float) {
        style = style.copy(cornerRadius = radius)
        applyCornerRadius(radius)
    }

    fun setStatusIndicatorBackgroundColor(@ColorInt color: Int) {
        setCardBackgroundColor(color)
    }

    fun setStatusIndicatorOnlineIcon(icon: Drawable?) {
        style = style.copy(onlineIcon = icon)
        updateStatusIndicatorDisplay()
    }

    fun setStatusIndicatorPrivateGroupIcon(icon: Drawable?) {
        style = style.copy(privateGroupIcon = icon)
        updateStatusIndicatorDisplay()
    }

    fun setStatusIndicatorProtectedGroupIcon(icon: Drawable?) {
        style = style.copy(protectedGroupIcon = icon)
        updateStatusIndicatorDisplay()
    }

    fun getStatusIndicator(): StatusIndicator = statusIndicator

    fun setStatusIndicator(indicator: StatusIndicator) {
        this.statusIndicator = indicator
        updateStatusIndicatorDisplay()
    }
}
