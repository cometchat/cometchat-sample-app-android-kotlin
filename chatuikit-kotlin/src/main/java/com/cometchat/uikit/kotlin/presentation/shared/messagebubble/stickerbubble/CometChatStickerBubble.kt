package com.cometchat.uikit.kotlin.presentation.shared.messagebubble.stickerbubble

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.AttributeSet

import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.StyleRes
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.cometchat.chat.models.CustomMessage
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.kotlin.shared.resources.utils.Utils
import com.google.android.material.card.MaterialCardView

/**
 * A custom view that represents a sticker bubble used for displaying sticker
 * images in a chat interface.
 *
 * This class extends [MaterialCardView] to provide rich material design support.
 *
 * Features:
 * - Sticker image display with aspect ratio preservation
 * - Loading state with progress indicator
 * - Error state handling
 * - Customizable styling via XML attributes or programmatically
 */
class CometChatStickerBubble @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    private lateinit var stickerImageView: ImageView

    // Single style object - nullable during initialization to handle parent constructor calls
    private var style: CometChatStickerBubbleStyle? = null

    private var stickerUrl: String = ""
    private var onClickListener: (() -> Unit)? = null

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
        LayoutInflater.from(context).inflate(R.layout.cometchat_message_sticker_bubble, this, true)
        stickerImageView = findViewById(R.id.cometchat_sticker_bubble_image_view)
        applyStyleAttributes(attrs, defStyleAttr)
        
        setOnClickListener { onClickListener?.invoke() }

        // Set long click listener to propagate to parent for message actions
        setOnLongClickListener { v ->
            Utils.performAdapterClick(v)
            true
        }
    }

    /**
     * Extracts style attributes from XML and applies them.
     */
    private fun applyStyleAttributes(attrs: AttributeSet?, defStyleAttr: Int) {
        if (attrs == null) return

        var typedArray = context.theme.obtainStyledAttributes(
            attrs, R.styleable.CometChatStickerBubble, defStyleAttr, 0
        )
        val styleResId = typedArray.getResourceId(
            R.styleable.CometChatStickerBubble_cometchatStickerBubbleStyle, 0
        )
        typedArray.recycle()

        typedArray = context.theme.obtainStyledAttributes(
            attrs, R.styleable.CometChatStickerBubble, defStyleAttr, styleResId
        )
        // fromTypedArray handles recycling internally
        style = CometChatStickerBubbleStyle.fromTypedArray(context, typedArray)
        applyStyle()
    }

    /**
     * Applies all style properties to views.
     */
    private fun applyStyle() {
        val currentStyle = style ?: return

        // Bubble container styling - stickers default to transparent
        // Background handled by wrapper CometChatMessageBubble - content views are transparent by default
        if (currentStyle.cornerRadius != 0f) applyCornerRadius(currentStyle.cornerRadius)
        if (currentStyle.strokeWidth != 0f) applyStrokeWidth(currentStyle.strokeWidth)
        if (currentStyle.strokeColor != 0) applyStrokeColor(currentStyle.strokeColor)
        currentStyle.backgroundDrawable?.let { applyBackgroundDrawable(it) }
    }

    /**
     * Sets the message to display in the sticker bubble.
     * Extracts the sticker URL from the message's customData.
     *
     * @param message The custom message containing sticker data
     */
    fun setMessage(message: CustomMessage?) {
        if (message == null) {
            setStickerUrl("")
            return
        }

        val url = extractStickerUrl(message)
        setStickerUrl(url)
    }

    /**
     * Sets the sticker URL directly.
     *
     * @param url The URL of the sticker image
     */
    fun setStickerUrl(url: String) {
        stickerUrl = url
        loadSticker()
    }

    /**
     * Sets the image URL for the sticker.
     * This is an alias for [setStickerUrl] to maintain API compatibility.
     *
     * @param url The URL of the sticker image
     */
    fun setImageUrl(url: String) {
        setStickerUrl(url)
    }

    /**
     * Sets a drawable directly on the sticker image view.
     *
     * @param drawable The drawable to display
     */
    fun setDrawable(drawable: Drawable) {
        stickerImageView.setImageDrawable(drawable)
    }

    /**
     * Sets the alignment of the sticker bubble.
     *
     * @param alignment The message bubble alignment (left/right)
     */
    fun setAlignment(alignment: UIKitConstants.MessageBubbleAlignment) {
        // Alignment is typically handled by the parent container
        // This method is provided for API compatibility
    }

    /**
     * Gets the sticker ImageView.
     *
     * @return The ImageView displaying the sticker
     */
    fun getStickerImageView(): ImageView = stickerImageView

    /**
     * Gets the current sticker URL.
     */
    fun getStickerUrl(): String = stickerUrl

    private fun loadSticker() {
        if (stickerUrl.isEmpty()) {
            stickerImageView.setImageDrawable(null)
            return
        }

        Glide.with(context)
            .load(stickerUrl)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(stickerImageView)
    }

    private fun extractStickerUrl(message: CustomMessage): String {
        return try {
            val customData = message.customData
            customData?.optString("sticker_url", "")
                ?: customData?.optString("url", "")
                ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * Sets the click listener for the sticker.
     *
     * @param listener The click listener
     */
    fun setOnStickerClickListener(listener: (() -> Unit)?) {
        onClickListener = listener
    }

    // ========================================
    // Public Style Methods
    // ========================================

    /**
     * Sets the style from a style object.
     */
    fun setStyle(style: CometChatStickerBubbleStyle) {
        this.style = style
        applyStyle()
    }

    /**
     * Sets the style from a style resource.
     */
    fun setStyle(@StyleRes styleRes: Int) {
        if (styleRes != 0) {
            val typedArray = context.theme.obtainStyledAttributes(
                styleRes, R.styleable.CometChatStickerBubble
            )
            // fromTypedArray handles recycling internally
            setStyle(CometChatStickerBubbleStyle.fromTypedArray(context, typedArray))
        }
    }

    // ========================================
    // Getters (read from style object)
    // ========================================

    fun getBubbleBackgroundColor(): Int = style?.backgroundColor ?: Color.TRANSPARENT
    fun getBubbleCornerRadius(): Float = style?.cornerRadius ?: 0f
    fun getBubbleStrokeWidth(): Float = style?.strokeWidth ?: 0f
    fun getBubbleStrokeColor(): Int = style?.strokeColor ?: 0
    fun getSenderNameTextColor(): Int = style?.senderNameTextColor ?: 0
    fun getSenderNameTextAppearance(): Int = style?.senderNameTextAppearance ?: 0
    fun getThreadIndicatorTextColor(): Int = style?.threadIndicatorTextColor ?: 0
    fun getThreadIndicatorTextAppearance(): Int = style?.threadIndicatorTextAppearance ?: 0
    fun getThreadIndicatorIconTint(): Int = style?.threadIndicatorIconTint ?: 0

    // ========================================
    // Setters (update style object + apply)
    // ========================================

    override fun setBackgroundColor(@ColorInt color: Int) {
        style = style?.copy(backgroundColor = color) ?: CometChatStickerBubbleStyle(backgroundColor = color)
        applyBackgroundColor(color)
    }

    fun setCornerRadius(@Dimension radius: Float) {
        style = style?.copy(cornerRadius = radius) ?: CometChatStickerBubbleStyle(cornerRadius = radius)
        applyCornerRadius(radius)
    }

    fun setBubbleStrokeWidth(@Dimension width: Float) {
        style = style?.copy(strokeWidth = width) ?: CometChatStickerBubbleStyle(strokeWidth = width)
        applyStrokeWidth(width)
    }

    fun setBubbleStrokeColor(@ColorInt color: Int) {
        style = style?.copy(strokeColor = color) ?: CometChatStickerBubbleStyle(strokeColor = color)
        applyStrokeColor(color)
    }

    override fun setBackgroundDrawable(drawable: Drawable?) {
        // Guard against calls during parent constructor initialization when style is null
        if (style == null) {
            super.setBackgroundDrawable(drawable)
            return
        }
        style = style?.copy(backgroundDrawable = drawable)
        drawable?.let { applyBackgroundDrawable(it) }
    }

    fun setSenderNameTextColor(@ColorInt color: Int) {
        style = style?.copy(senderNameTextColor = color) ?: CometChatStickerBubbleStyle(senderNameTextColor = color)
    }

    fun setSenderNameTextAppearance(@StyleRes appearance: Int) {
        style = style?.copy(senderNameTextAppearance = appearance) ?: CometChatStickerBubbleStyle(senderNameTextAppearance = appearance)
    }

    fun setThreadIndicatorTextColor(@ColorInt color: Int) {
        style = style?.copy(threadIndicatorTextColor = color) ?: CometChatStickerBubbleStyle(threadIndicatorTextColor = color)
    }

    fun setThreadIndicatorTextAppearance(@StyleRes appearance: Int) {
        style = style?.copy(threadIndicatorTextAppearance = appearance) ?: CometChatStickerBubbleStyle(threadIndicatorTextAppearance = appearance)
    }

    fun setThreadIndicatorIconTint(@ColorInt color: Int) {
        style = style?.copy(threadIndicatorIconTint = color) ?: CometChatStickerBubbleStyle(threadIndicatorIconTint = color)
    }

    // ========================================
    // Private Apply Methods
    // ========================================

    private fun applyBackgroundColor(@ColorInt color: Int) {
        setCardBackgroundColor(color)
    }

    private fun applyCornerRadius(@Dimension radius: Float) {
        setRadius(radius)
    }

    private fun applyStrokeWidth(@Dimension width: Float) {
        strokeWidth = width.toInt()
    }

    private fun applyStrokeColor(@ColorInt color: Int) {
        strokeColor = color
    }

    private fun applyBackgroundDrawable(drawable: Drawable) {
        super.setBackgroundDrawable(drawable)
    }


}
