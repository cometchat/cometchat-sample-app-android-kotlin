package com.cometchat.uikit.kotlin.presentation.stickerbubble.ui

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.cometchat.chat.models.CustomMessage
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.kotlin.presentation.stickerbubble.style.CometChatStickerBubbleStyle
import com.google.android.material.card.MaterialCardView

/**
 * CometChatStickerBubble is a View-based component for displaying sticker messages.
 *
 * Features:
 * - Displays sticker images from CustomMessage's sticker_url field
 * - Supports GIF animations
 * - Supports left/right alignment for incoming/outgoing messages
 * - Style customization via XML attributes or programmatically
 *
 * Usage:
 * ```kotlin
 * val stickerBubble = CometChatStickerBubble(context)
 * stickerBubble.setMessage(customMessage)
 * ```
 */
class CometChatStickerBubble @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.cometchatStickerBubbleStyle
) : MaterialCardView(context, attrs, defStyleAttr) {

    // ==================== Views ====================
    private val ivSticker: ImageView
    private val progressBar: ProgressBar

    // ==================== Data ====================
    private var imageUrl: String? = null
    private var message: CustomMessage? = null

    // ==================== Style ====================
    private var style: CometChatStickerBubbleStyle = CometChatStickerBubbleStyle.default(context)

    init {
        // Initialize card properties
        cardElevation = 0f
        setCardBackgroundColor(android.graphics.Color.TRANSPARENT)
        radius = 0f

        // Inflate layout
        LayoutInflater.from(context).inflate(R.layout.cometchat_sticker_bubble, this, true)

        // Find views
        ivSticker = findViewById(R.id.iv_sticker)
        progressBar = findViewById(R.id.progress_bar)

        // Apply XML attributes
        attrs?.let { applyAttributes(it, defStyleAttr) }

        // Apply default style
        applyStyle(style)
    }

    // ==================== XML Attribute Handling ====================

    private fun applyAttributes(attrs: AttributeSet, defStyleAttr: Int) {
        val typedArray = context.obtainStyledAttributes(
            attrs,
            R.styleable.CometChatStickerBubble,
            defStyleAttr,
            0
        )
        style = CometChatStickerBubbleStyle.fromTypedArray(context, typedArray)
        // Note: typedArray is recycled inside fromTypedArray
    }

    // ==================== Style Application ====================

    private fun applyStyle(style: CometChatStickerBubbleStyle) {
        this.style = style

        // Apply background color (transparent for stickers)
        setCardBackgroundColor(style.backgroundColor)

        // Apply corner radius
        radius = style.cornerRadius.toFloat()

        // Apply stroke
        strokeWidth = style.strokeWidth
        strokeColor = style.strokeColor
    }

    // ==================== Public API ====================

    /**
     * Sets the sticker image from a CustomMessage.
     *
     * Extracts the sticker_url from the message's custom data and displays it.
     *
     * @param message The CustomMessage containing the sticker data
     */
    fun setMessage(message: CustomMessage) {
        this.message = message
        try {
            val stickerUrl = message.customData?.getString("sticker_url")
            if (stickerUrl != null) {
                setImageUrl(stickerUrl)
            }
        } catch (e: Exception) {
            // Ignore JSON parsing errors
        }
    }

    /**
     * Sets the sticker image from a URL.
     *
     * Supports both static images and GIF animations.
     *
     * @param url The URL of the sticker image
     */
    fun setImageUrl(url: String?) {
        if (url == null) return
        this.imageUrl = url

        // Show loading indicator
        progressBar.visibility = View.VISIBLE

        // Build fallback request for non-GIF images
        val fallbackRequest = Glide.with(context)
            .load(url)

        // Load sticker image with Glide
        // Try loading as drawable (handles both GIF and static images)
        // Use error() for fallback to avoid callback issues
        Glide.with(context)
            .load(url)
            .error(fallbackRequest)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>,
                    isFirstResource: Boolean
                ): Boolean {
                    progressBar.visibility = View.GONE
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: Target<Drawable>?,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    progressBar.visibility = View.GONE
                    return false
                }
            })
            .into(ivSticker)
    }

    /**
     * Sets a drawable directly on the sticker image view.
     *
     * @param drawable The drawable to display
     */
    fun setDrawable(drawable: Drawable?) {
        ivSticker.setImageDrawable(drawable)
    }

    /**
     * Sets the alignment of the sticker bubble.
     *
     * @param alignment The alignment (LEFT for incoming, RIGHT for outgoing)
     */
    fun setAlignment(alignment: UIKitConstants.MessageBubbleAlignment) {
        // Alignment is typically handled by the parent layout
        // This method is provided for API compatibility
    }

    /**
     * Sets the style for the sticker bubble.
     *
     * @param style The style configuration to apply
     */
    fun setStyle(style: CometChatStickerBubbleStyle) {
        applyStyle(style)
    }

    /**
     * Gets the current style configuration.
     *
     * @return The current style
     */
    fun getStyle(): CometChatStickerBubbleStyle = style

    // ==================== Getters ====================

    /**
     * Gets the current message.
     */
    fun getMessage(): CustomMessage? = message

    /**
     * Gets the current image URL.
     */
    fun getImageUrl(): String? = imageUrl

    /**
     * Gets the sticker ImageView.
     */
    fun getStickerImageView(): ImageView = ivSticker
}
