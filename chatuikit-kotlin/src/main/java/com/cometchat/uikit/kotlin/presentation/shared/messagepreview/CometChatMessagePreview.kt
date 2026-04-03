package com.cometchat.uikit.kotlin.presentation.shared.messagepreview

import android.content.Context
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import android.text.SpannableStringBuilder
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.DrawableRes
import androidx.annotation.StyleRes
import androidx.appcompat.content.res.AppCompatResources
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.CustomMessage
import com.cometchat.chat.models.MediaMessage
import com.cometchat.chat.models.TextMessage
import com.cometchat.uikit.core.CometChatUIKit
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.databinding.CometchatMessagePreviewBinding
import com.cometchat.uikit.kotlin.shared.formatters.CometChatTextFormatter
import com.cometchat.uikit.kotlin.shared.resources.utils.Utils
import com.cometchat.uikit.kotlin.theme.CometChatTheme
import com.google.android.material.card.MaterialCardView

/**
 * A message preview component that displays a quoted/replied message preview.
 *
 * This component is used in:
 * - Message bubbles to show the quoted message being replied to
 * - Message composer to show the message being replied to or edited
 *
 * Features:
 * - Title text (typically sender name)
 * - Subtitle text (message content preview)
 * - Optional message icon (for media messages)
 * - Close button (for dismissing in composer)
 * - Separator bar for visual distinction
 * - Min/max width constraints
 * - Click listeners for preview and close actions
 *
 * @see CometChatMessagePreviewStyle
 */
class CometChatMessagePreview @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    private val binding: CometchatMessagePreviewBinding
    
    // Width constraints
    private var minWidthDp: Int = 0
    private var maxWidthDp: Int = -1
    
    // Click listeners
    private var onMessagePreviewClick: (() -> Unit)? = null
    private var onCloseClick: (() -> Unit)? = null

    init {
        Utils.initMaterialCard(this)
        binding = CometchatMessagePreviewBinding.inflate(LayoutInflater.from(context), this, true)
        initClickListeners()
        applyStyleAttributes(attrs, defStyleAttr, 0)
    }

    private fun initClickListeners() {
        binding.messagePreviewParent.setOnClickListener {
            onMessagePreviewClick?.invoke()
        }
        binding.ivMessageClose.setOnClickListener {
            onCloseClick?.invoke()
        }
    }

    private fun applyStyleAttributes(attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) {
        val typedArray = context.theme.obtainStyledAttributes(
            attrs, R.styleable.CometChatMessagePreview, defStyleAttr, defStyleRes
        )
        val styleResId = typedArray.getResourceId(
            R.styleable.CometChatMessagePreview_cometChatMessagePreviewStyle, 0
        )
        val styledArray = context.theme.obtainStyledAttributes(
            attrs, R.styleable.CometChatMessagePreview, defStyleRes, styleResId
        )
        extractAttributesAndApplyDefaults(styledArray)
    }

    private fun extractAttributesAndApplyDefaults(typedArray: TypedArray) {
        try {
            setBackgroundColor(
                typedArray.getColor(
                    R.styleable.CometChatMessagePreview_cometChatMessagePreviewBackgroundColor,
                    CometChatTheme.getExtendedPrimaryColor800(context)
                )
            )
            setStrokeColor(
                typedArray.getColor(
                    R.styleable.CometChatMessagePreview_cometChatMessagePreviewStrokeColor,
                    0
                )
            )
            setSeparatorColor(
                typedArray.getColor(
                    R.styleable.CometChatMessagePreview_cometChatMessagePreviewSeparatorColor,
                    CometChatTheme.getColorWhite(context)
                )
            )
            setTitleTextColor(
                typedArray.getColor(
                    R.styleable.CometChatMessagePreview_cometChatMessagePreviewTitleTextColor,
                    CometChatTheme.getColorWhite(context)
                )
            )
            setSubtitleTextColor(
                typedArray.getColor(
                    R.styleable.CometChatMessagePreview_cometChatMessagePreviewSubtitleTextColor,
                    CometChatTheme.getColorWhite(context)
                )
            )
            setCloseIconTint(
                typedArray.getColor(
                    R.styleable.CometChatMessagePreview_cometChatMessagePreviewCloseIconTint,
                    CometChatTheme.getIconTintSecondary(context)
                )
            )
            setMessageIconTint(
                typedArray.getColor(
                    R.styleable.CometChatMessagePreview_cometChatMessagePreviewMessageIconTint,
                    CometChatTheme.getIconTintSecondary(context)
                )
            )
            setStrokeWidth(
                typedArray.getDimension(
                    R.styleable.CometChatMessagePreview_cometChatMessagePreviewStrokeWidth,
                    0f
                )
            )
            setCornerRadius(
                typedArray.getDimension(
                    R.styleable.CometChatMessagePreview_cometChatMessagePreviewCornerRadius,
                    0f
                )
            )
            setTitleTextAppearance(
                typedArray.getResourceId(
                    R.styleable.CometChatMessagePreview_cometChatMessagePreviewTitleTextAppearance,
                    0
                )
            )
            setSubtitleTextAppearance(
                typedArray.getResourceId(
                    R.styleable.CometChatMessagePreview_cometChatMessagePreviewSubtitleTextAppearance,
                    0
                )
            )
            typedArray.getDrawable(
                R.styleable.CometChatMessagePreview_cometChatMessagePreviewCloseIcon
            )?.let { setCloseIcon(it) }
        } finally {
            typedArray.recycle()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minWidthPx = (minWidthDp * resources.displayMetrics.density).toInt()
        val maxWidthPx = if (maxWidthDp == -1) -1 else (maxWidthDp * resources.displayMetrics.density).toInt()
        
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        if (minWidthDp == -1 && maxWidthDp == -1) {
            return
        }

        var measuredWidth = getMeasuredWidth()
        var finalWidth = measuredWidth

        // Apply minimum width constraint
        if (minWidthDp != -1) {
            finalWidth = maxOf(finalWidth, minWidthPx)
        }

        // Apply maximum width constraint
        if (maxWidthDp != -1) {
            finalWidth = minOf(finalWidth, maxWidthPx)
        }

        if (finalWidth != measuredWidth) {
            val newWidthSpec = MeasureSpec.makeMeasureSpec(finalWidth, MeasureSpec.EXACTLY)
            super.onMeasure(newWidthSpec, heightMeasureSpec)
        }
    }

    // ========================================
    // Width Constraints
    // ========================================

    override fun setMinimumWidth(minWidth: Int) {
        this.minWidthDp = minWidth
        measure(measuredWidth, measuredHeight)
    }

    /**
     * Sets the maximum width in dp.
     *
     * @param maxWidth Maximum width in dp, or -1 for no constraint
     */
    fun setMaxWidth(maxWidth: Int) {
        this.maxWidthDp = maxWidth
        measure(measuredWidth, measuredHeight)
    }

    // ========================================
    // Click Listeners
    // ========================================

    /**
     * Sets the click listener for the message preview area.
     *
     * @param listener The click listener, or null to remove
     */
    fun setOnMessagePreviewClickListener(listener: (() -> Unit)?) {
        this.onMessagePreviewClick = listener
    }

    /**
     * Sets the click listener for the close button.
     *
     * @param listener The click listener, or null to remove
     */
    fun setOnCloseClickListener(listener: (() -> Unit)?) {
        this.onCloseClick = listener
    }

    // ========================================
    // Content Setters
    // ========================================

    /**
     * Sets the title text (typically sender name).
     *
     * @param text The title text
     */
    fun setMessagePreviewTitleText(text: String) {
        binding.tvMessageLayoutTitle.text = text
    }

    /**
     * Sets the subtitle text (message content preview).
     *
     * @param text The subtitle text
     */
    fun setMessagePreviewSubtitleText(text: String) {
        binding.tvMessageLayoutSubtitle.text = text
    }

    /**
     * Sets the visibility of the message icon.
     *
     * @param visibility View visibility constant
     */
    fun setMessageIconVisibility(visibility: Int) {
        binding.messageIcon.visibility = visibility
    }

    /**
     * Sets the visibility of the close icon.
     *
     * @param visibility View visibility constant
     */
    fun setCloseIconVisibility(visibility: Int) {
        binding.ivMessageClose.visibility = visibility
    }

    // ========================================
    // Style Setters
    // ========================================

    override fun setBackgroundColor(@ColorInt color: Int) {
        binding.messagePreviewParent.setCardBackgroundColor(color)
    }

    /**
     * Sets the stroke color of the preview card.
     *
     * @param color The stroke color
     */
    override fun setStrokeColor(@ColorInt color: Int) {
        binding.messagePreviewParent.strokeColor = color
    }

    /**
     * Sets the stroke width of the preview card.
     *
     * @param dimension The stroke width in pixels
     */
    fun setStrokeWidth(@Dimension dimension: Float) {
        binding.messagePreviewParent.strokeWidth = dimension.toInt()
    }

    /**
     * Sets the tint color of the message icon.
     *
     * @param color The tint color
     */
    fun setMessageIconTint(@ColorInt color: Int) {
        binding.messageIcon.setColorFilter(color)
    }

    /**
     * Sets the corner radius of the preview card.
     *
     * @param radius The corner radius in pixels
     */
    fun setCornerRadius(@Dimension radius: Float) {
        binding.messagePreviewParent.radius = radius
    }

    /**
     * Sets the color of the separator bar.
     *
     * @param color The separator color
     */
    fun setSeparatorColor(@ColorInt color: Int) {
        binding.separatorView.setBackgroundColor(color)
    }

    /**
     * Sets the text color of the title.
     *
     * @param color The text color
     */
    fun setTitleTextColor(@ColorInt color: Int) {
        binding.tvMessageLayoutTitle.setTextColor(color)
    }

    /**
     * Sets the text appearance of the title.
     *
     * @param resourceId The text appearance resource ID
     */
    fun setTitleTextAppearance(@StyleRes resourceId: Int) {
        if (resourceId != 0) {
            binding.tvMessageLayoutTitle.setTextAppearance(resourceId)
        }
    }

    /**
     * Sets the text color of the subtitle.
     *
     * @param color The text color
     */
    fun setSubtitleTextColor(@ColorInt color: Int) {
        binding.tvMessageLayoutSubtitle.setTextColor(color)
    }

    /**
     * Sets the text appearance of the subtitle.
     *
     * @param resourceId The text appearance resource ID
     */
    fun setSubtitleTextAppearance(@StyleRes resourceId: Int) {
        if (resourceId != 0) {
            binding.tvMessageLayoutSubtitle.setTextAppearance(resourceId)
        }
    }

    /**
     * Sets the close icon drawable.
     *
     * @param drawable The close icon drawable
     */
    fun setCloseIcon(drawable: Drawable?) {
        binding.ivMessageClose.setImageDrawable(drawable)
    }

    /**
     * Sets the message icon drawable from a resource ID.
     *
     * @param icon The drawable resource ID
     */
    fun setMessageIcon(@DrawableRes icon: Int) {
        val drawable = AppCompatResources.getDrawable(context, icon)
        binding.messageIcon.setImageDrawable(drawable)
    }

    /**
     * Sets the message icon drawable.
     *
     * @param drawable The message icon drawable
     */
    fun setMessageIcon(drawable: Drawable?) {
        binding.messageIcon.setImageDrawable(drawable)
    }

    /**
     * Sets the tint color of the close icon.
     *
     * @param color The tint color
     */
    fun setCloseIconTint(@ColorInt color: Int) {
        binding.ivMessageClose.setColorFilter(color)
    }

    /**
     * Applies a style from a style resource.
     *
     * @param messagePreviewStyle The style resource ID
     */
    fun setStyle(@StyleRes messagePreviewStyle: Int) {
        if (messagePreviewStyle != 0) {
            val typedArray = context.theme.obtainStyledAttributes(
                messagePreviewStyle,
                R.styleable.CometChatMessagePreview
            )
            extractAttributesAndApplyDefaults(typedArray)
        }
    }

    /**
     * Applies a [CometChatMessagePreviewStyle] to this view.
     *
     * @param style The style to apply
     */
    fun setStyle(style: CometChatMessagePreviewStyle) {
        setBackgroundColor(style.backgroundColor)
        setStrokeWidth(style.strokeWidth)
        setCornerRadius(style.cornerRadius)
        setStrokeColor(style.strokeColor)
        setSeparatorColor(style.separatorColor)
        setTitleTextColor(style.titleTextColor)
        setTitleTextAppearance(style.titleTextAppearance)
        setSubtitleTextColor(style.subtitleTextColor)
        setSubtitleTextAppearance(style.subtitleTextAppearance)
        style.closeIcon?.let { setCloseIcon(it) }
        style.messageIcon?.let { setMessageIcon(it) }
        setCloseIconTint(style.closeIconTint)
        setMessageIconTint(style.messageIconTint)
    }

    // ========================================
    // Getters
    // ========================================

    /**
     * Gets the subtitle TextView for advanced customization.
     *
     * @return The subtitle TextView
     */
    fun getSubtitleView(): TextView = binding.tvMessageLayoutSubtitle

    /**
     * Gets the title TextView for advanced customization.
     *
     * @return The title TextView
     */
    fun getTitleView(): TextView = binding.tvMessageLayoutTitle

    /**
     * Gets the message icon ImageView for advanced customization.
     *
     * @return The message icon ImageView
     */
    fun getMessageIconView(): ImageView = binding.messageIcon

    /**
     * Gets the close icon ImageView for advanced customization.
     *
     * @return The close icon ImageView
     */
    fun getCloseIconView(): ImageView = binding.ivMessageClose

    /**
     * Gets the separator View for advanced customization.
     *
     * @return The separator View
     */
    fun getSeparatorView(): View = binding.separatorView

    // ========================================
    // Message Binding
    // ========================================

    /**
     * Sets the message content for the preview.
     * Handles different message types (text, media, custom) and applies text formatters.
     *
     * @param context The Android context
     * @param message The message to display
     * @param textFormatters Optional list of text formatters for text messages
     * @param formattingType The formatting type context
     * @param alignment The bubble alignment
     */
    fun setMessage(
        context: Context,
        message: BaseMessage,
        textFormatters: List<CometChatTextFormatter> = emptyList(),
        formattingType: UIKitConstants.FormattingType = UIKitConstants.FormattingType.MESSAGE_BUBBLE,
        alignment: UIKitConstants.MessageBubbleAlignment = UIKitConstants.MessageBubbleAlignment.LEFT
    ) {
        try {
            val loggedInUser = CometChatUIKit.getLoggedInUser()
            val sender = if (message.sender?.uid != loggedInUser?.uid) {
                message.sender?.name ?: ""
            } else {
                context.getString(R.string.cometchat_you)
            }

            when (message) {
                is TextMessage -> handleTextMessagePreview(context, sender, message, textFormatters, formattingType, alignment)
                is MediaMessage -> handleMediaMessagePreview(context, sender, message)
                is CustomMessage -> handleCustomMessagePreview(context, sender, message)
                else -> {
                    setMessagePreviewTitleText(sender)
                    setMessagePreviewSubtitleText(message.type ?: "")
                    setMessageIconVisibility(View.GONE)
                }
            }
        } catch (e: Exception) {
            // Fallback to basic display
            setMessagePreviewTitleText(message.sender?.name ?: "")
            setMessagePreviewSubtitleText(getBasicMessageText(message))
            setMessageIconVisibility(View.GONE)
        }
    }

    private fun handleTextMessagePreview(
        context: Context,
        sender: String,
        message: TextMessage,
        textFormatters: List<CometChatTextFormatter>,
        formattingType: UIKitConstants.FormattingType,
        alignment: UIKitConstants.MessageBubbleAlignment
    ) {
        val text: String = when {
            message.deletedAt == 0L -> {
                var spannableBuilder = SpannableStringBuilder(message.text ?: "")
                for (formatter in textFormatters) {
                    spannableBuilder = formatter.prepareMessageString(
                        context, message, spannableBuilder, alignment, formattingType
                    ) ?: spannableBuilder
                }
                spannableBuilder.toString()
            }
            message.deletedAt > 0 -> context.getString(R.string.cometchat_this_message_deleted)
            else -> context.getString(R.string.cometchat_this_message_type_is_not_supported)
        }

        setMessagePreviewTitleText(sender)
        setMessagePreviewSubtitleText(text)
        setMessageIconVisibility(View.GONE)
    }

    private fun handleMediaMessagePreview(context: Context, sender: String, message: MediaMessage) {
        val iconRes = when (message.type?.lowercase()) {
            UIKitConstants.MessageType.IMAGE -> R.drawable.cometchat_ic_message_preview_image
            UIKitConstants.MessageType.VIDEO -> R.drawable.cometchat_ic_message_preview_image
            UIKitConstants.MessageType.AUDIO -> R.drawable.cometchat_ic_message_preview_audio_mic
            UIKitConstants.MessageType.FILE -> R.drawable.cometchat_ic_message_preview_document
            else -> null
        }

        setMessagePreviewTitleText(sender)
        val subtitle = message.attachment?.fileName ?: message.type ?: ""
        setMessagePreviewSubtitleText(subtitle)

        if (iconRes != null) {
            setMessageIcon(iconRes)
            setMessageIconVisibility(View.VISIBLE)
        } else {
            setMessageIconVisibility(View.GONE)
        }
    }

    private fun handleCustomMessagePreview(context: Context, sender: String, message: CustomMessage) {
        when (message.type) {
            EXTENSION_POLL -> {
                setMessagePreviewTitleText(sender)
                setMessagePreviewSubtitleText(context.getString(R.string.cometchat_poll))
                setMessageIcon(R.drawable.cometchat_ic_message_preview_poll)
                setMessageIconVisibility(View.VISIBLE)
            }
            EXTENSION_STICKER -> {
                setMessagePreviewTitleText(sender)
                setMessagePreviewSubtitleText(context.getString(R.string.cometchat_message_sticker))
                setMessageIcon(R.drawable.cometchat_ic_message_preview_sticker)
                setMessageIconVisibility(View.VISIBLE)
            }
            EXTENSION_LOCATION -> {
                setMessagePreviewTitleText(sender)
                setMessagePreviewSubtitleText(context.getString(R.string.cometchat_message_location))
                setMessageIcon(R.drawable.cometchat_ic_message_preview_location)
                setMessageIconVisibility(View.VISIBLE)
            }
            EXTENSION_DOCUMENT -> {
                setMessagePreviewTitleText(sender)
                setMessagePreviewSubtitleText(context.getString(R.string.cometchat_message_document))
                setMessageIcon(R.drawable.cometchat_ic_message_preview_collaborative_document)
                setMessageIconVisibility(View.VISIBLE)
            }
            EXTENSION_WHITEBOARD -> {
                setMessagePreviewTitleText(sender)
                setMessagePreviewSubtitleText(context.getString(R.string.cometchat_collaborative_whiteboard))
                setMessageIcon(R.drawable.cometchat_ic_conversations_collabrative_document)
                setMessageIconVisibility(View.VISIBLE)
            }
            UIKitConstants.MessageType.MEETING -> {
                setMessagePreviewTitleText(sender)
                setMessagePreviewSubtitleText(context.getString(R.string.cometchat_meeting))
                setMessageIcon(R.drawable.cometchat_ic_message_preview_call)
                setMessageIconVisibility(View.VISIBLE)
            }
            else -> {
                setMessagePreviewTitleText(sender)
                val subtitle = message.conversationText?.takeIf { it.isNotEmpty() }
                    ?: message.metadata?.optString("pushNotification")?.takeIf { it.isNotEmpty() }
                    ?: message.type
                    ?: ""
                setMessagePreviewSubtitleText(subtitle)
                setMessageIconVisibility(View.GONE)
            }
        }
    }

    private fun getBasicMessageText(message: BaseMessage): String {
        return when (message) {
            is TextMessage -> message.text ?: ""
            is MediaMessage -> {
                when (message.type?.lowercase()) {
                    "image" -> "📷 Photo"
                    "video" -> "📹 Video"
                    "audio" -> "🎵 Audio"
                    "file" -> "📎 File"
                    else -> message.type ?: ""
                }
            }
            else -> message.type ?: ""
        }
    }

    companion object {
        // Extension type constants
        private const val EXTENSION_POLL = "extension_poll"
        private const val EXTENSION_STICKER = "extension_sticker"
        private const val EXTENSION_LOCATION = "extension_location"
        private const val EXTENSION_DOCUMENT = "extension_document"
        private const val EXTENSION_WHITEBOARD = "extension_whiteboard"
    }
}
