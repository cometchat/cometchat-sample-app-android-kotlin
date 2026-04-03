package com.cometchat.uikit.kotlin.presentation.shared.messagebubble.deletebubble

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.text.SpannableString
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.DrawableRes
import androidx.annotation.StyleRes
import com.cometchat.chat.models.BaseMessage
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.shared.resources.utils.Utils
import com.google.android.material.card.MaterialCardView

/**
 * A custom view that represents a deleted message bubble used for displaying
 * placeholder content when a message has been deleted.
 *
 * This class extends [MaterialCardView] to provide rich material design support.
 *
 * Features:
 * - Delete icon display
 * - "This message was deleted" text with italic styling
 * - Customizable styling via XML attributes or programmatically
 */
class CometChatDeleteBubble @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    private lateinit var deleteIconImageView: ImageView
    private lateinit var messageTextView: TextView

    // Single style object - NO individual style properties
    private var style: CometChatDeleteBubbleStyle = CometChatDeleteBubbleStyle()

    // Non-style properties
    private var deleteText: String = context.getString(R.string.cometchat_this_message_deleted)

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
        // Use View.inflate() and addView() to match Java implementation
        val view = View.inflate(context, R.layout.cometchat_delete_bubble, null)
        deleteIconImageView = view.findViewById(R.id.cometchat_delete_bubble_image_view)
        messageTextView = view.findViewById(R.id.cometchat_delete_bubble_text_view)
        addView(view)
        applyStyleAttributes(attrs, defStyleAttr)
    }

    private fun applyStyleAttributes(attrs: AttributeSet?, defStyleAttr: Int) {
        if (attrs == null) return

        var typedArray = context.theme.obtainStyledAttributes(
            attrs, R.styleable.CometChatDeleteBubble, defStyleAttr, 0
        )
        val styleResId = typedArray.getResourceId(
            R.styleable.CometChatDeleteBubble_cometchatDeleteBubbleStyle, 0
        )
        typedArray.recycle()

        typedArray = context.theme.obtainStyledAttributes(
            attrs, R.styleable.CometChatDeleteBubble, defStyleAttr, styleResId
        )
        // fromTypedArray handles recycling internally
        style = CometChatDeleteBubbleStyle.fromTypedArray(context, typedArray)
        applyStyle()
    }

    /**
     * Applies all style properties to views.
     */
    private fun applyStyle() {
        // Bubble container styling
        // Background handled by wrapper CometChatMessageBubble - content views are transparent by default
        // if (style.backgroundColor != 0) applyBackgroundColor(style.backgroundColor)
        if (style.cornerRadius != 0f) applyCornerRadius(style.cornerRadius)
        if (style.strokeWidth != 0f) applyStrokeWidth(style.strokeWidth)
        if (style.strokeColor != 0) applyStrokeColor(style.strokeColor)
        style.backgroundDrawable?.let { applyBackgroundDrawable(it) }

        // Text styling
        if (style.textColor != 0) applyTextColor(style.textColor)
        if (style.textAppearance != 0) applyTextAppearance(style.textAppearance)

        // Icon styling
        if (style.iconTint != 0) applyIconTint(style.iconTint)
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
            messageTextView.setTypeface(messageTextView.typeface, Typeface.ITALIC)
        }
    }

    private fun applyIconTint(@ColorInt color: Int) {
        deleteIconImageView.imageTintList = ColorStateList.valueOf(color)
    }

    // ========================================
    // Public API - Message Setting
    // ========================================

    /**
     * Sets the message to display in the delete bubble.
     * The message is used for context but the display is always "This message was deleted".
     *
     * @param message The deleted message
     */
    fun setMessage(message: BaseMessage?) {
        // The message is used for context, but display is always the delete text
        messageTextView.text = deleteText
    }

    /**
     * Sets the text to display in the delete bubble.
     *
     * @param text The text to display
     */
    fun setText(text: String) {
        deleteText = text
        messageTextView.text = text
    }

    /**
     * Sets the text content of the delete bubble using a [SpannableString].
     *
     * @param text The spannable string to display in the bubble.
     */
    fun setText(text: SpannableString) {
        messageTextView.setText(text, TextView.BufferType.SPANNABLE)
    }

    /**
     * Gets the [TextView] associated with this delete bubble.
     *
     * @return The TextView within the bubble.
     */
    fun getTextView(): TextView = messageTextView

    /**
     * Gets the current text displayed in the delete bubble.
     */
    fun getText(): String = deleteText

    /**
     * Sets the delete icon resource.
     *
     * @param icon The drawable resource ID
     */
    fun setDeleteIcon(@DrawableRes icon: Int) {
        deleteIconImageView.setImageResource(icon)
    }

    // ========================================
    // Public Style Methods
    // ========================================

    /**
     * Sets the style from a style object.
     */
    fun setStyle(style: CometChatDeleteBubbleStyle) {
        this.style = style
        applyStyle()
    }

    /**
     * Sets the style from a style resource.
     */
    fun setStyle(@StyleRes styleRes: Int) {
        if (styleRes != 0) {
            val typedArray = context.theme.obtainStyledAttributes(
                styleRes, R.styleable.CometChatDeleteBubble
            )
            // fromTypedArray handles recycling internally
            setStyle(CometChatDeleteBubbleStyle.fromTypedArray(context, typedArray))
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
    fun getDeleteIconTint(): Int = style.iconTint
    fun getSenderNameTextColor(): Int = style.senderNameTextColor
    fun getSenderNameTextAppearance(): Int = style.senderNameTextAppearance

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

    /**
     * Sets the delete icon tint color.
     *
     * @param color The tint color
     */
    fun setDeleteIconTint(@ColorInt color: Int) {
        style = style.copy(iconTint = color)
        applyIconTint(color)
    }

    fun setSenderNameTextColor(@ColorInt color: Int) {
        style = style.copy(senderNameTextColor = color)
    }

    fun setSenderNameTextAppearance(@StyleRes appearance: Int) {
        style = style.copy(senderNameTextAppearance = appearance)
    }

    companion object {
        private val TAG = CometChatDeleteBubble::class.java.simpleName
    }
}
