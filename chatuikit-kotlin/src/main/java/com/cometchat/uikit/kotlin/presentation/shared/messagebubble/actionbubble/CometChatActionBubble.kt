package com.cometchat.uikit.kotlin.presentation.shared.messagebubble.actionbubble

import android.content.Context
import android.text.SpannableString
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.StyleRes
import com.cometchat.chat.models.Action
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.shared.resources.utils.Utils
import com.google.android.material.card.MaterialCardView

/**
 * A custom view that represents an action bubble used for displaying system
 * action messages like "User joined the group" or "User left".
 *
 * This class extends [MaterialCardView] to provide rich material design support.
 *
 * Features:
 * - Centered text display
 * - Support for SpannableString (mentions, formatting)
 * - Distinct visual style to differentiate from regular messages
 * - Customizable styling via XML attributes or programmatically
 */
class CometChatActionBubble @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    private lateinit var messageTextView: TextView

    // Single style object - NO individual style properties
    private var style: CometChatActionBubbleStyle = CometChatActionBubbleStyle()

    init {
        inflateAndInitializeView(attrs, defStyleAttr)
    }

    /**
     * Initializes the view by inflating the XML layout and binding child views.
     *
     * @param attrs The attribute set for customization.
     * @param defStyleAttr The default style attribute.
     */
    private fun inflateAndInitializeView(attrs: AttributeSet?, defStyleAttr: Int) {
        Utils.initMaterialCard(this)
        LayoutInflater.from(context).inflate(R.layout.cometchat_action_bubble, this, true)
        messageTextView = findViewById(R.id.cometchat_action_bubble_text_view)
        applyStyleAttributes(attrs, defStyleAttr)
    }

    private fun applyStyleAttributes(attrs: AttributeSet?, defStyleAttr: Int) {
        if (attrs == null) return

        var typedArray = context.theme.obtainStyledAttributes(
            attrs, R.styleable.CometChatActionBubble, defStyleAttr, 0
        )
        val styleResId = typedArray.getResourceId(
            R.styleable.CometChatActionBubble_cometchatActionBubbleStyle, 0
        )
        typedArray.recycle()

        typedArray = context.theme.obtainStyledAttributes(
            attrs, R.styleable.CometChatActionBubble, defStyleAttr, styleResId
        )
        // fromTypedArray handles recycling internally
        style = CometChatActionBubbleStyle.fromTypedArray(context, typedArray)
        applyStyle()
    }

    /**
     * Applies all style properties to views.
     */
    private fun applyStyle() {
        // Bubble container styling
        // Action bubbles apply their own background since the outer CometChatMessageBubble is transparent for CENTER-aligned messages
        if (style.backgroundColor != 0) applyBackgroundColor(style.backgroundColor)
        if (style.cornerRadius != 0f) applyCornerRadius(style.cornerRadius)
        if (style.strokeWidth != 0f) applyStrokeWidth(style.strokeWidth)
        if (style.strokeColor != 0) applyStrokeColor(style.strokeColor)
        style.backgroundDrawable?.let { applyBackgroundDrawable(it) }

        // Text styling
        if (style.textColor != 0) applyTextColor(style.textColor)
        if (style.textAppearance != 0) applyTextAppearance(style.textAppearance)
    }

    // ========================================
    // Private Apply Methods
    // ========================================

    private fun applyBackgroundColor(@ColorInt color: Int) {
        setCardBackgroundColor(color)
    }

    private fun applyCornerRadius(radius: Float) {
        setRadius(radius)
    }

    private fun applyStrokeWidth(width: Float) {
        strokeWidth = width.toInt()
    }

    private fun applyStrokeColor(@ColorInt color: Int) {
        strokeColor = color
    }

    private fun applyBackgroundDrawable(drawable: android.graphics.drawable.Drawable) {
        super.setBackgroundDrawable(drawable)
    }

    private fun applyTextColor(@ColorInt color: Int) {
        messageTextView.setTextColor(color)
    }

    private fun applyTextAppearance(@StyleRes appearance: Int) {
        if (appearance != 0) {
            messageTextView.setTextAppearance(appearance)
        }
    }

    // ========================================
    // Public API - Message Setting
    // ========================================

    /**
     * Sets the message to display in the action bubble.
     *
     * @param message The action message
     */
    fun setMessage(message: Action?) {
        if (message == null) {
            setText("")
            return
        }
        setText(message.message ?: "")
    }

    /**
     * Sets the text to display in the action bubble.
     *
     * @param text The text to display
     */
    fun setText(text: String) {
        messageTextView.text = text
    }

    /**
     * Sets the text to display in the action bubble using a SpannableString.
     * Useful for messages with mentions or other formatting.
     *
     * @param text The spannable text to display
     */
    fun setText(text: SpannableString) {
        messageTextView.setText(text, TextView.BufferType.SPANNABLE)
    }

    /**
     * Gets the current text displayed in the action bubble.
     */
    fun getText(): CharSequence = messageTextView.text

    /**
     * Gets the TextView for direct manipulation.
     */
    fun getTextView(): TextView = messageTextView

    // ========================================
    // Public Style Methods
    // ========================================

    /**
     * Sets the style from a style object.
     */
    fun setStyle(style: CometChatActionBubbleStyle) {
        this.style = style
        applyStyle()
    }

    /**
     * Sets the style from a style resource.
     */
    fun setStyle(@StyleRes styleRes: Int) {
        if (styleRes != 0) {
            val typedArray = context.theme.obtainStyledAttributes(
                styleRes, R.styleable.CometChatActionBubble
            )
            // fromTypedArray handles recycling internally
            setStyle(CometChatActionBubbleStyle.fromTypedArray(context, typedArray))
        }
    }

    // ========================================
    // Getters (read from style object)
    // ========================================

    fun getBubbleBackgroundColor(): Int = style.backgroundColor
    fun getBubbleCornerRadius(): Float = style.cornerRadius
    fun getBubbleStrokeWidth(): Float = style.strokeWidth
    fun getBubbleStrokeColor(): Int = style.strokeColor
    fun getTextColor(): Int = style.textColor
    fun getTextAppearance(): Int = style.textAppearance

    // ========================================
    // Setters (update style object + apply)
    // ========================================

    override fun setBackgroundColor(@ColorInt color: Int) {
        style = style.copy(backgroundColor = color)
        applyBackgroundColor(color)
    }

    fun setCornerRadius(@Dimension radius: Int) {
        style = style.copy(cornerRadius = radius.toFloat())
        applyCornerRadius(radius.toFloat())
    }

    fun setBubbleStrokeWidth(@Dimension width: Int) {
        style = style.copy(strokeWidth = width.toFloat())
        applyStrokeWidth(width.toFloat())
    }

    fun setBubbleStrokeColor(@ColorInt color: Int) {
        style = style.copy(strokeColor = color)
        applyStrokeColor(color)
    }

    fun setTextColor(@ColorInt color: Int) {
        style = style.copy(textColor = color)
        applyTextColor(color)
    }

    fun setTextAppearance(@StyleRes appearance: Int) {
        style = style.copy(textAppearance = appearance)
        applyTextAppearance(appearance)
    }

    companion object {
        private val TAG = CometChatActionBubble::class.java.simpleName
    }
}
