package com.cometchat.uikit.kotlin.presentation.shared.messagebubble.textbubble

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.text.SpannableString
import android.text.Spanned
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.DrawableRes
import androidx.annotation.StyleRes
import androidx.core.widget.TextViewCompat
import com.bumptech.glide.Glide
import com.cometchat.chat.models.TextMessage
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.markdown.MarkdownViewRenderer
import com.cometchat.uikit.kotlin.shared.formatters.CometChatTextFormatter
import com.cometchat.uikit.kotlin.shared.formatters.FormatterUtils
import com.cometchat.uikit.kotlin.shared.resources.utils.Utils
import com.cometchat.uikit.kotlin.shared.spans.MentionMovementMethod
import com.cometchat.uikit.kotlin.shared.spans.TagSpan
import com.google.android.material.card.MaterialCardView

/**
 * A custom view that represents a text bubble used for displaying messages in a
 * chat interface. It provides several customization options such as text color,
 * background color, font, border styling, and more.
 *
 * This class extends [MaterialCardView] to provide rich material design support.
 *
 * Features:
 * - Text message display with formatting support
 * - Link preview with title, description, and image
 * - Message translation display
 * - Edited message indicator
 * - Customizable styling via XML attributes or programmatically
 */
class CometChatTextBubble @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    // View references bound from XML layout
    private lateinit var parentViewLayout: LinearLayout
    private lateinit var markdownContentContainer: LinearLayout
    private lateinit var messageTextView: TextView
    private lateinit var separator: View
    private lateinit var translationContainer: LinearLayout
    private lateinit var translateTextView: TextView
    private lateinit var textTranslatedTextView: TextView
    private lateinit var editedTextView: TextView
    private lateinit var linkPreviewContainer: LinearLayout
    private lateinit var linkMessageContainerCard: MaterialCardView
    private lateinit var bannerPreviewImageView: ImageView
    private lateinit var fabIconImageView: ImageView
    private lateinit var headingTextView: TextView
    private lateinit var descriptionTextView: TextView
    private lateinit var linkTextView: TextView

    // Single style object - nullable during initialization to handle parent constructor calls
    private var style: CometChatTextBubbleStyle? = null
    
    // Current alignment for styling
    private var currentAlignment: UIKitConstants.MessageBubbleAlignment = UIKitConstants.MessageBubbleAlignment.LEFT

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
        LayoutInflater.from(context).inflate(R.layout.cometchat_text_bubble, this, true)

        // Bind UI elements to their corresponding IDs from XML layout
        parentViewLayout = findViewById(R.id.text_bubble_parent_view)
        markdownContentContainer = findViewById(R.id.markdown_content_container)
        messageTextView = findViewById(R.id.cometchat_text_bubble_text_view)
        separator = findViewById(R.id.separator)
        translationContainer = findViewById(R.id.translation_message_container)
        translateTextView = findViewById(R.id.translate_message)
        textTranslatedTextView = findViewById(R.id.text_translated)
        editedTextView = findViewById(R.id.text_edited)
        linkPreviewContainer = findViewById(R.id.link_Message_container)
        linkMessageContainerCard = findViewById(R.id.link_message_card_container)
        bannerPreviewImageView = findViewById(R.id.preview_banner)
        fabIconImageView = findViewById(R.id.fab_icon_image)
        headingTextView = findViewById(R.id.link_heading)
        descriptionTextView = findViewById(R.id.link_description)
        linkTextView = findViewById(R.id.link)

        // Set long click listener to propagate to parent for message actions
        setOnLongClickListener { v ->
            Utils.performAdapterClick(v)
            true
        }

        // Apply style attributes from XML
        applyStyleAttributes(attrs, defStyleAttr)
    }

    /**
     * Extracts style attributes from XML and applies them.
     */
    private fun applyStyleAttributes(attrs: AttributeSet?, defStyleAttr: Int) {
        var typedArray = context.theme.obtainStyledAttributes(
            attrs, R.styleable.CometChatTextBubble, defStyleAttr, 0
        )
        val styleResId = typedArray.getResourceId(
            R.styleable.CometChatTextBubble_cometchatTextBubbleStyle, 0
        )
        typedArray.recycle()

        typedArray = context.theme.obtainStyledAttributes(
            attrs, R.styleable.CometChatTextBubble, defStyleAttr, styleResId
        )
        // fromTypedArray handles recycling internally
        style = CometChatTextBubbleStyle.fromTypedArray(context, typedArray)
        applyStyle()
    }

    /**
     * Applies all style properties to views.
     */
    private fun applyStyle() {
        val currentStyle = style ?: return

        // Text styling
        if (currentStyle.textColor != 0) applyTextColor(currentStyle.textColor)
        if (currentStyle.textAppearance != 0) applyTextAppearance(currentStyle.textAppearance)
        if (currentStyle.textLinkColor != 0) applyTextLinkColor(currentStyle.textLinkColor)

        // Bubble container styling
        // Background handled by wrapper CometChatMessageBubble - content views are transparent by default
        // if (currentStyle.backgroundColor != 0) applyBackgroundColor(currentStyle.backgroundColor)
        if (currentStyle.cornerRadius != 0f) applyCornerRadius(currentStyle.cornerRadius)
        if (currentStyle.strokeWidth != 0f) applyStrokeWidth(currentStyle.strokeWidth)
        if (currentStyle.strokeColor != 0) applyStrokeColor(currentStyle.strokeColor)
        currentStyle.backgroundDrawable?.let { applyBackgroundDrawable(it) }

        // Translation styling
        if (currentStyle.translatedTextColor != 0) applyTranslatedTextColor(currentStyle.translatedTextColor)
        if (currentStyle.translatedTextAppearance != 0) applyTranslatedTextAppearance(currentStyle.translatedTextAppearance)
        if (currentStyle.separatorColor != 0) applySeparatorColor(currentStyle.separatorColor)

        // Link preview styling
        if (currentStyle.linkPreviewTitleColor != 0) applyLinkPreviewTitleColor(currentStyle.linkPreviewTitleColor)
        if (currentStyle.linkPreviewTitleAppearance != 0) applyLinkPreviewTitleAppearance(currentStyle.linkPreviewTitleAppearance)
        if (currentStyle.linkPreviewDescriptionColor != 0) applyLinkPreviewDescriptionColor(currentStyle.linkPreviewDescriptionColor)
        if (currentStyle.linkPreviewDescriptionAppearance != 0) applyLinkPreviewDescriptionAppearance(currentStyle.linkPreviewDescriptionAppearance)
        if (currentStyle.linkPreviewLinkColor != 0) applyLinkPreviewLinkColor(currentStyle.linkPreviewLinkColor)
        if (currentStyle.linkPreviewLinkAppearance != 0) applyLinkPreviewLinkAppearance(currentStyle.linkPreviewLinkAppearance)
        if (currentStyle.linkPreviewBackgroundColor != 0) applyLinkPreviewBackgroundColor(currentStyle.linkPreviewBackgroundColor)
        currentStyle.linkPreviewBackgroundDrawable?.let { applyLinkPreviewBackgroundDrawable(it) }
        if (currentStyle.linkPreviewStrokeColor != 0) applyLinkPreviewStrokeColor(currentStyle.linkPreviewStrokeColor)
        if (currentStyle.linkPreviewStrokeWidth != 0f) applyLinkPreviewStrokeWidth(currentStyle.linkPreviewStrokeWidth)
        if (currentStyle.linkPreviewCornerRadius != 0f) applyLinkPreviewCornerRadius(currentStyle.linkPreviewCornerRadius)
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }

    // ========================================
    // Public Style Methods
    // ========================================

    /**
     * Sets the style from a style object.
     */
    fun setStyle(style: CometChatTextBubbleStyle) {
        this.style = style
        applyStyle()
    }

    /**
     * Sets the style from a style resource.
     */
    fun setStyle(@StyleRes styleRes: Int) {
        if (styleRes != 0) {
            val typedArray = context.theme.obtainStyledAttributes(
                styleRes, R.styleable.CometChatTextBubble
            )
            // fromTypedArray handles recycling internally
            setStyle(CometChatTextBubbleStyle.fromTypedArray(context, typedArray))
        }
    }

    // ========================================
    // Message Methods
    // ========================================

    /**
     * Sets the message to display in the text bubble.
     * Parses markdown and renders formatted text with support for:
     * - Bold, italic, underline, strikethrough
     * - Inline code
     * - Code blocks
     * - Bullet lists
     * - Ordered lists
     * - Blockquotes
     */
    fun setMessage(
        message: TextMessage?,
        textFormatters: List<CometChatTextFormatter>?,
        alignment: UIKitConstants.MessageBubbleAlignment
    ) {
        if (message != null) {
            currentAlignment = alignment
            
            // Apply text formatters first to transform mention patterns
            val formattedText = FormatterUtils.getFormattedText(
                context,
                message,
                UIKitConstants.FormattingType.MESSAGE_BUBBLE,
                alignment,
                message.text,
                textFormatters ?: emptyList()
            )
            
            // Formatters run first, markdown is parsed from their output — the mention spans they
            // produced are re-overlaid per segment by the renderer.
            renderMarkdown(formattedText.toString(), formattedText as? Spanned)

            linkPreviewContainer.visibility = GONE
            editedTextView.visibility = if (message.editedAt == 0L) View.GONE else View.VISIBLE

            val linkPreview = extractLinkPreview(message)
            if (linkPreview != null) {
                linkPreviewContainer.visibility = View.VISIBLE
                setLinkPreview(
                    linkPreview.title,
                    linkPreview.description,
                    linkPreview.url,
                    linkPreview.imageUrl,
                    linkPreview.favIconUrl
                )
                return
            }

            val translatedText = extractTranslatedText(message)
            if (translatedText != null) {
                setTranslatedText(translatedText)
                translationContainer.visibility = View.VISIBLE
                separator.visibility = View.VISIBLE
            } else {
                translationContainer.visibility = View.GONE
                separator.visibility = View.GONE
            }
        }
        resetWidth()
    }
    
    /**
     * Renders [markdown] as block-level views (paragraph / code block / list / blockquote) into the
     * content container, delegating to the shared [MarkdownViewRenderer] that media-message captions
     * use as well. [formatterSpans] is the text-formatter output the markdown was parsed from, so
     * its mention spans can be re-overlaid on the marker-stripped text.
     */
    private fun renderMarkdown(markdown: String, formatterSpans: Spanned? = null) {
        markdownContentContainer.removeAllViews()
        markdownContentContainer.visibility = View.VISIBLE
        messageTextView.visibility = View.GONE

        val currentStyle = style ?: return
        MarkdownViewRenderer.render(
            markdownContentContainer,
            markdown,
            formatterSpans,
            MarkdownViewRenderer.Style(
                textColor = currentStyle.textColor,
                linkColor = currentStyle.textLinkColor,
                textAppearance = currentStyle.textAppearance,
                isOutgoing = currentAlignment == UIKitConstants.MessageBubbleAlignment.RIGHT
            )
        )
    }

    private data class LinkPreviewData(
        val title: String,
        val description: String,
        val url: String,
        val imageUrl: String?,
        val favIconUrl: String?
    )

    private fun extractLinkPreview(message: TextMessage): LinkPreviewData? {
        return try {
            val metadata = message.metadata ?: return null
            if (metadata.has("@injected")) {
                val injected = metadata.getJSONObject("@injected")
                if (injected.has("extensions")) {
                    val extensions = injected.getJSONObject("extensions")
                    if (extensions.has("link-preview")) {
                        val linkPreviewJson = extensions.getJSONObject("link-preview")
                        if (linkPreviewJson.has("links")) {
                            val linksArray = linkPreviewJson.getJSONArray("links")
                            if (linksArray.length() > 0) {
                                val firstLink = linksArray.getJSONObject(0)
                                return LinkPreviewData(
                                    title = firstLink.optString("title", ""),
                                    description = firstLink.optString("description", ""),
                                    url = firstLink.optString("url", ""),
                                    imageUrl = firstLink.optString("image", null),
                                    favIconUrl = firstLink.optString("favicon", null)
                                )
                            }
                        }
                    }
                }
            }
            null
        } catch (e: Exception) {
            null
        }
    }

    private fun extractTranslatedText(message: TextMessage): String? {
        return try {
            message.metadata?.let { metadata ->
                if (metadata.has("translated_message")) {
                    val translated = metadata.getString("translated_message")
                    if (translated.isNotEmpty()) translated else null
                } else null
            }
        } catch (e: Exception) {
            null
        }
    }

    fun setText(text: SpannableString) {
        // For direct SpannableString, use the fallback TextView
        markdownContentContainer.visibility = View.GONE
        messageTextView.visibility = View.VISIBLE
        messageTextView.setText(text, TextView.BufferType.SPANNABLE)
        // Set MentionMovementMethod for handling TagSpan clicks
        messageTextView.movementMethod = MentionMovementMethod.getInstance()
    }

    fun setText(text: String) {
        // Plain string input carries no formatter spans — markdown only.
        renderMarkdown(text)
    }

    fun setLinkPreview(
        title: String,
        description: String,
        url: String,
        bannerImage: String?,
        fabIcon: String?
    ) {
        headingTextView.text = title
        descriptionTextView.text = description
        linkTextView.text = url

        if (bannerImage.isNullOrEmpty()) {
            bannerPreviewImageView.visibility = View.GONE
            if (!fabIcon.isNullOrEmpty()) {
                fabIconImageView.visibility = View.VISIBLE
                Glide.with(context).load(fabIcon).into(fabIconImageView)
            } else {
                fabIconImageView.visibility = View.GONE
            }
        } else {
            fabIconImageView.visibility = View.GONE
            bannerPreviewImageView.visibility = View.VISIBLE
            Glide.with(context).load(bannerImage).into(bannerPreviewImageView)
        }
        adjustWidthForLinkPreview()
    }

    fun setTranslatedText(text: String) {
        textTranslatedTextView.text = text
    }

    private fun resetWidth() {
        val layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        parentViewLayout.layoutParams = layoutParams
    }

    private fun adjustWidthForLinkPreview() {
        val layoutParams = LinearLayout.LayoutParams(dpToPx(240), ViewGroup.LayoutParams.WRAP_CONTENT)
        parentViewLayout.layoutParams = layoutParams
    }

    fun getTextView(): TextView = messageTextView

    fun setCompoundDrawable(
        @DrawableRes start: Int,
        @DrawableRes top: Int,
        @DrawableRes end: Int,
        @DrawableRes bottom: Int
    ) {
        TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(
            messageTextView, start, top, end, bottom
        )
    }

    fun setCompoundDrawableIconTint(@ColorInt color: Int) {
        TextViewCompat.setCompoundDrawableTintList(messageTextView, ColorStateList.valueOf(color))
    }

    fun setTextViewMargin(leftMargin: Int, topMargin: Int, rightMargin: Int, bottomMargin: Int) {
        val layoutParams = messageTextView.layoutParams
        if (layoutParams is ViewGroup.MarginLayoutParams) {
            layoutParams.setMargins(
                if (leftMargin > -1) dpToPx(leftMargin) else 0,
                if (topMargin > -1) dpToPx(topMargin) else 0,
                if (rightMargin > -1) dpToPx(rightMargin) else 0,
                if (bottomMargin > -1) dpToPx(bottomMargin) else 0
            )
            messageTextView.layoutParams = layoutParams
        }
    }

    // ========================================
    // Getters (read from style object)
    // ========================================

    fun getTextColor(): Int = style?.textColor ?: 0
    fun getTextAppearance(): Int = style?.textAppearance ?: 0
    fun getTextLinkColor(): Int = style?.textLinkColor ?: 0
    fun getBubbleBackgroundColor(): Int = style?.backgroundColor ?: 0
    fun getBubbleCornerRadius(): Float = style?.cornerRadius ?: 0f
    fun getBubbleStrokeWidth(): Float = style?.strokeWidth ?: 0f
    fun getBubbleStrokeColor(): Int = style?.strokeColor ?: 0
    fun getBubbleBackgroundDrawable(): Drawable? = style?.backgroundDrawable
    fun getTranslatedTextColor(): Int = style?.translatedTextColor ?: 0
    fun getTranslatedTextAppearance(): Int = style?.translatedTextAppearance ?: 0
    fun getSeparatorColor(): Int = style?.separatorColor ?: 0
    fun getLinkPreviewTitleColor(): Int = style?.linkPreviewTitleColor ?: 0
    fun getLinkPreviewTitleAppearance(): Int = style?.linkPreviewTitleAppearance ?: 0
    fun getLinkPreviewDescriptionColor(): Int = style?.linkPreviewDescriptionColor ?: 0
    fun getLinkPreviewDescriptionAppearance(): Int = style?.linkPreviewDescriptionAppearance ?: 0
    fun getLinkPreviewLinkColor(): Int = style?.linkPreviewLinkColor ?: 0
    fun getLinkPreviewLinkAppearance(): Int = style?.linkPreviewLinkAppearance ?: 0
    fun getLinkPreviewBackgroundColor(): Int = style?.linkPreviewBackgroundColor ?: 0
    fun getLinkPreviewBackgroundDrawable(): Drawable? = style?.linkPreviewBackgroundDrawable
    fun getLinkPreviewStrokeColor(): Int = style?.linkPreviewStrokeColor ?: 0
    fun getLinkPreviewStrokeWidth(): Float = style?.linkPreviewStrokeWidth ?: 0f
    fun getLinkPreviewCornerRadius(): Float = style?.linkPreviewCornerRadius ?: 0f

    // ========================================
    // Setters (update style object + apply)
    // ========================================

    fun setTextColor(@ColorInt color: Int) {
        style = style?.copy(textColor = color) ?: CometChatTextBubbleStyle(textColor = color)
        applyTextColor(color)
    }

    fun setTextAppearance(@StyleRes appearance: Int) {
        style = style?.copy(textAppearance = appearance) ?: CometChatTextBubbleStyle(textAppearance = appearance)
        applyTextAppearance(appearance)
    }

    fun setTextLinkColor(@ColorInt color: Int) {
        style = style?.copy(textLinkColor = color) ?: CometChatTextBubbleStyle(textLinkColor = color)
        applyTextLinkColor(color)
    }

    override fun setBackgroundColor(@ColorInt color: Int) {
        style = style?.copy(backgroundColor = color) ?: CometChatTextBubbleStyle(backgroundColor = color)
        applyBackgroundColor(color)
    }

    fun setCornerRadius(@Dimension radius: Float) {
        style = style?.copy(cornerRadius = radius) ?: CometChatTextBubbleStyle(cornerRadius = radius)
        applyCornerRadius(radius)
    }

    fun setBubbleStrokeWidth(@Dimension width: Float) {
        style = style?.copy(strokeWidth = width) ?: CometChatTextBubbleStyle(strokeWidth = width)
        applyStrokeWidth(width)
    }

    fun setBubbleStrokeColor(@ColorInt color: Int) {
        style = style?.copy(strokeColor = color) ?: CometChatTextBubbleStyle(strokeColor = color)
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

    fun setTranslatedTextColor(@ColorInt color: Int) {
        style = style?.copy(translatedTextColor = color) ?: CometChatTextBubbleStyle(translatedTextColor = color)
        applyTranslatedTextColor(color)
    }

    fun setTranslatedTextAppearance(@StyleRes appearance: Int) {
        style = style?.copy(translatedTextAppearance = appearance) ?: CometChatTextBubbleStyle(translatedTextAppearance = appearance)
        applyTranslatedTextAppearance(appearance)
    }

    fun setSeparatorColor(@ColorInt color: Int) {
        style = style?.copy(separatorColor = color) ?: CometChatTextBubbleStyle(separatorColor = color)
        applySeparatorColor(color)
    }

    fun setLinkPreviewTitleColor(@ColorInt color: Int) {
        style = style?.copy(linkPreviewTitleColor = color) ?: CometChatTextBubbleStyle(linkPreviewTitleColor = color)
        applyLinkPreviewTitleColor(color)
    }

    fun setLinkPreviewTitleAppearance(@StyleRes appearance: Int) {
        style = style?.copy(linkPreviewTitleAppearance = appearance) ?: CometChatTextBubbleStyle(linkPreviewTitleAppearance = appearance)
        applyLinkPreviewTitleAppearance(appearance)
    }

    fun setLinkPreviewDescriptionColor(@ColorInt color: Int) {
        style = style?.copy(linkPreviewDescriptionColor = color) ?: CometChatTextBubbleStyle(linkPreviewDescriptionColor = color)
        applyLinkPreviewDescriptionColor(color)
    }

    fun setLinkPreviewDescriptionAppearance(@StyleRes appearance: Int) {
        style = style?.copy(linkPreviewDescriptionAppearance = appearance) ?: CometChatTextBubbleStyle(linkPreviewDescriptionAppearance = appearance)
        applyLinkPreviewDescriptionAppearance(appearance)
    }

    fun setLinkPreviewLinkColor(@ColorInt color: Int) {
        style = style?.copy(linkPreviewLinkColor = color) ?: CometChatTextBubbleStyle(linkPreviewLinkColor = color)
        applyLinkPreviewLinkColor(color)
    }

    fun setLinkPreviewLinkAppearance(@StyleRes appearance: Int) {
        style = style?.copy(linkPreviewLinkAppearance = appearance) ?: CometChatTextBubbleStyle(linkPreviewLinkAppearance = appearance)
        applyLinkPreviewLinkAppearance(appearance)
    }

    fun setLinkPreviewBackgroundColor(@ColorInt color: Int) {
        style = style?.copy(linkPreviewBackgroundColor = color) ?: CometChatTextBubbleStyle(linkPreviewBackgroundColor = color)
        applyLinkPreviewBackgroundColor(color)
    }

    fun setLinkPreviewBackgroundDrawable(drawable: Drawable?) {
        style = style?.copy(linkPreviewBackgroundDrawable = drawable) ?: CometChatTextBubbleStyle(linkPreviewBackgroundDrawable = drawable)
        drawable?.let { applyLinkPreviewBackgroundDrawable(it) }
    }

    fun setLinkPreviewStrokeColor(@ColorInt color: Int) {
        style = style?.copy(linkPreviewStrokeColor = color) ?: CometChatTextBubbleStyle(linkPreviewStrokeColor = color)
        applyLinkPreviewStrokeColor(color)
    }

    fun setLinkPreviewStrokeWidth(@Dimension width: Float) {
        style = style?.copy(linkPreviewStrokeWidth = width) ?: CometChatTextBubbleStyle(linkPreviewStrokeWidth = width)
        applyLinkPreviewStrokeWidth(width)
    }

    fun setLinkPreviewCornerRadius(@Dimension radius: Float) {
        style = style?.copy(linkPreviewCornerRadius = radius) ?: CometChatTextBubbleStyle(linkPreviewCornerRadius = radius)
        applyLinkPreviewCornerRadius(radius)
    }

    // ========================================
    // Private Apply Methods
    // ========================================

    private fun applyTextColor(@ColorInt color: Int) {
        messageTextView.setTextColor(color)
    }

    private fun applyTextAppearance(@StyleRes appearance: Int) {
        if (appearance != 0) {
            messageTextView.setTextAppearance(appearance)
        }
    }

    private fun applyTextLinkColor(@ColorInt color: Int) {
        messageTextView.setLinkTextColor(color)
    }

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

    private fun applyTranslatedTextColor(@ColorInt color: Int) {
        translateTextView.setTextColor(color)
        textTranslatedTextView.setTextColor(color)
        editedTextView.setTextColor(color)
    }

    private fun applyTranslatedTextAppearance(@StyleRes appearance: Int) {
        if (appearance != 0) {
            translateTextView.setTextAppearance(appearance)
        }
    }

    private fun applySeparatorColor(@ColorInt color: Int) {
        separator.setBackgroundColor(color)
    }

    private fun applyLinkPreviewTitleColor(@ColorInt color: Int) {
        headingTextView.setTextColor(color)
    }

    private fun applyLinkPreviewTitleAppearance(@StyleRes appearance: Int) {
        if (appearance != 0) {
            headingTextView.setTextAppearance(appearance)
        }
    }

    private fun applyLinkPreviewDescriptionColor(@ColorInt color: Int) {
        descriptionTextView.setTextColor(color)
    }

    private fun applyLinkPreviewDescriptionAppearance(@StyleRes appearance: Int) {
        if (appearance != 0) {
            descriptionTextView.setTextAppearance(appearance)
        }
    }

    private fun applyLinkPreviewLinkColor(@ColorInt color: Int) {
        linkTextView.setTextColor(color)
    }

    private fun applyLinkPreviewLinkAppearance(@StyleRes appearance: Int) {
        if (appearance != 0) {
            linkTextView.setTextAppearance(appearance)
        }
    }

    private fun applyLinkPreviewBackgroundColor(@ColorInt color: Int) {
        linkMessageContainerCard.setCardBackgroundColor(color)
    }

    private fun applyLinkPreviewBackgroundDrawable(drawable: Drawable) {
        linkMessageContainerCard.background = drawable
    }

    private fun applyLinkPreviewStrokeColor(@ColorInt color: Int) {
        linkMessageContainerCard.strokeColor = color
    }

    private fun applyLinkPreviewStrokeWidth(@Dimension width: Float) {
        linkMessageContainerCard.strokeWidth = width.toInt()
    }

    private fun applyLinkPreviewCornerRadius(@Dimension radius: Float) {
        linkMessageContainerCard.radius = radius
    }

    companion object {
        private val TAG = CometChatTextBubble::class.java.simpleName
    }
}
