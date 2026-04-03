package com.cometchat.uikit.kotlin.presentation.shared.baseelements.badgecount

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.StyleRes
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.shared.resources.utils.Utils
import com.google.android.material.card.MaterialCardView

/**
 * CometChatBadgeCount is a custom view that represents a badge with a count
 * displayed inside a MaterialCardView. It provides methods to customize the
 * appearance of the badge, such as setting the count, text size, text color,
 * background color, corner radius, stroke color, and more.
 *
 * Created on: 06 September 2024 Modified on: 10 September 2024
 */
@Suppress("unused")
class CometChatBadgeCount @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.cometchatBadgeStyle
) : MaterialCardView(context, attrs, defStyleAttr) {

    companion object {
        private val TAG = CometChatBadgeCount::class.java.simpleName
    }

    private lateinit var tvMessageCount: TextView
    private var count: Int = 0

    // Single style object - NO individual style properties
    private var style: CometChatBadgeCountStyle = CometChatBadgeCountStyle()

    init {
        if (!isInEditMode) {
            inflateAndInitializeView(attrs, defStyleAttr)
        }
    }

    /**
     * Inflates and initializes the view by setting up the layout, retrieving the
     * attributes, and applying styles.
     *
     * @param attrs        The attributes of the XML tag that is inflating the view.
     * @param defStyleAttr The default style to apply to this view.
     */
    private fun inflateAndInitializeView(attrs: AttributeSet?, defStyleAttr: Int) {
        // Initialize the MaterialCardView
        Utils.initMaterialCard(this)

        // Set the layout parameters - use WRAP_CONTENT for responsive sizing
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        // Create a new TextView to display the message count
        tvMessageCount = TextView(context).apply {
            // Set layout params to WRAP_CONTENT for responsive text sizing
            layoutParams = LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
            )
            // Center the text inside the TextView
            gravity = Gravity.CENTER
            includeFontPadding = false
            // Set text alignment explicitly to center
            textAlignment = TEXT_ALIGNMENT_CENTER
            // Set padding (left, top, right, bottom) for the TextView
            setPadding(
                resources.getDimensionPixelSize(R.dimen.cometchat_4dp),
                resources.getDimensionPixelSize(R.dimen.cometchat_4dp),
                resources.getDimensionPixelSize(R.dimen.cometchat_4dp),
                resources.getDimensionPixelSize(R.dimen.cometchat_4dp)
            )
        }

        // Add the TextView to the MaterialCardView
        addView(tvMessageCount)

        // Apply the style attributes
        applyStyleAttributes(attrs, defStyleAttr)
    }

    /**
     * Applies the style attributes from XML using the style class factory method.
     *
     * @param attrs        The attributes of the XML tag that is inflating the view.
     * @param defStyleAttr The default style to apply to this view.
     */
    private fun applyStyleAttributes(attrs: AttributeSet?, defStyleAttr: Int) {
        var typedArray = context.theme.obtainStyledAttributes(
            attrs, R.styleable.CometChatBadge, defStyleAttr, 0
        )
        val styleResId = typedArray.getResourceId(
            R.styleable.CometChatBadge_cometchatBadgeStyle, 0
        )
        typedArray.recycle()
        
        typedArray = context.theme.obtainStyledAttributes(
            attrs, R.styleable.CometChatBadge, defStyleAttr, styleResId
        )
        // fromTypedArray handles recycling internally
        style = CometChatBadgeCountStyle.fromTypedArray(context, typedArray)
        applyStyle()
    }

    /**
     * Applies all style properties from the style object to views.
     */
    private fun applyStyle() {
        setCount(count)
        if (style.textAppearance != 0) applyTextAppearance(style.textAppearance)
        if (style.textColor != 0) applyTextColor(style.textColor)
        if (style.cornerRadius != 0f) applyCornerRadius(style.cornerRadius)
        if (style.backgroundColor != 0) applyBackgroundColor(style.backgroundColor)
        if (style.borderWidth != 0f) applyStrokeWidth(style.borderWidth)
        if (style.borderColor != 0) applyStrokeColor(style.borderColor)
    }

    // ========================================
    // Private Apply Methods
    // ========================================

    private fun applyTextColor(@ColorInt color: Int) {
        tvMessageCount.setTextColor(color)
    }

    private fun applyTextAppearance(@StyleRes appearance: Int) {
        tvMessageCount.setTextAppearance(appearance)
    }

    private fun applyCornerRadius(@Dimension radius: Float) {
        this.radius = radius
    }

    private fun applyBackgroundColor(@ColorInt color: Int) {
        setCardBackgroundColor(color)
    }

    private fun applyStrokeWidth(@Dimension width: Float) {
        strokeWidth = width.toInt()
    }

    private fun applyStrokeColor(@ColorInt color: Int) {
        strokeColor = color
    }

    // ========================================
    // Public Style Methods
    // ========================================

    /**
     * Sets the style for the CometChatBadgeCount view by applying a style resource.
     *
     * @param styleRes The style resource to apply.
     */
    fun setStyle(@StyleRes styleRes: Int) {
        if (styleRes != 0) {
            val typedArray = context.theme.obtainStyledAttributes(styleRes, R.styleable.CometChatBadge)
            // fromTypedArray handles recycling internally
            setStyle(CometChatBadgeCountStyle.fromTypedArray(context, typedArray))
        }
    }

    /**
     * Sets the style from a CometChatBadgeCountStyle object.
     */
    fun setStyle(style: CometChatBadgeCountStyle) {
        this.style = style
        applyStyle()
    }

    // ========================================
    // Getters (read from style object)
    // ========================================

    /**
     * Gets the count text color.
     *
     * @return The count text color.
     */
    fun getBadgeTextColor(): Int = style.textColor

    /**
     * Gets the count text style.
     *
     * @return The count text style.
     */
    fun getBadgeTextAppearance(): Int = style.textAppearance

    /**
     * Gets the badge corner radius.
     *
     * @return The badge corner radius.
     */
    fun getBadgeCornerRadius(): Float = style.cornerRadius

    /**
     * Gets the badge background color.
     *
     * @return The badge background color.
     */
    fun getBadgeBackgroundColor(): Int = style.backgroundColor

    /**
     * Gets the badge stroke width.
     *
     * @return The badge stroke width.
     */
    fun getBadgeStrokeWidth(): Float = style.borderWidth

    /**
     * Gets the badge stroke color.
     *
     * @return The badge stroke color.
     */
    fun getBadgeStrokeColor(): Int = style.borderColor

    // ========================================
    // Setters (update style object + apply)
    // ========================================

    /**
     * Gets the count for the badge.
     *
     * @return The count of the badge.
     */
    fun getCount(): Int = count

    /**
     * Sets the count for the badge.
     *
     * @param count The count to set.
     */
    fun setCount(count: Int) {
        this.count = count
        if(count<10){
            tvMessageCount.text = buildString {
                append(" ")
                append(count)
                append(" ")
            }
        }
        else if (count < 999) {
            tvMessageCount.text = count.toString()
        } else {
            tvMessageCount.setText(R.string.cometchat_unread_message_count_max)
        }
        // Use WRAP_CONTENT for both dimensions to allow responsive sizing based on text/font size
        // The minWidth and minHeight on the TextView ensure minimum dimensions are maintained
        layoutParams?.let { params ->
            params.width = LayoutParams.WRAP_CONTENT
            params.height = LayoutParams.WRAP_CONTENT
            layoutParams = params
        }
    }

    /**
     * Sets the count text color.
     *
     * @param badgeTextColor The count text color to set.
     */
    fun setBadgeTextColor(@ColorInt badgeTextColor: Int) {
        style = style.copy(textColor = badgeTextColor)
        applyTextColor(badgeTextColor)
    }

    /**
     * Sets the count text style.
     *
     * @param badgeTextAppearance The count text style to set.
     */
    fun setBadgeTextAppearance(@StyleRes badgeTextAppearance: Int) {
        style = style.copy(textAppearance = badgeTextAppearance)
        applyTextAppearance(badgeTextAppearance)
    }

    /**
     * Sets the badge corner radius.
     *
     * @param badgeCornerRadius The badge corner radius to set.
     */
    fun setBadgeCornerRadius(@Dimension badgeCornerRadius: Float) {
        style = style.copy(cornerRadius = badgeCornerRadius)
        applyCornerRadius(badgeCornerRadius)
    }

    /**
     * Sets the badge background color.
     *
     * @param badgeBackgroundColor The badge background color to set.
     */
    fun setBadgeBackgroundColor(@ColorInt badgeBackgroundColor: Int) {
        style = style.copy(backgroundColor = badgeBackgroundColor)
        applyBackgroundColor(badgeBackgroundColor)
    }

    /**
     * Sets the badge stroke width.
     *
     * @param badgeStrokeWidth The badge stroke width to set.
     */
    fun setBadgeStrokeWidth(@Dimension badgeStrokeWidth: Float) {
        style = style.copy(borderWidth = badgeStrokeWidth)
        applyStrokeWidth(badgeStrokeWidth)
    }

    /**
     * Sets the badge stroke color.
     *
     * @param badgeStrokeColor The badge stroke color to set.
     */
    fun setBadgeStrokeColor(@ColorInt badgeStrokeColor: Int) {
        style = style.copy(borderColor = badgeStrokeColor)
        applyStrokeColor(badgeStrokeColor)
    }
}
