package com.cometchat.uikit.kotlin.presentation.shared.messagebubble.textbubble

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.TypefaceSpan
import android.text.style.UnderlineSpan
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.DrawableRes
import androidx.annotation.StyleRes
import androidx.core.content.ContextCompat
import androidx.core.widget.TextViewCompat
import com.bumptech.glide.Glide
import com.cometchat.chat.models.TextMessage
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.formatter.MarkdownRenderer
import com.cometchat.uikit.core.formatter.RichTextFormat
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.shared.formatters.CometChatTextFormatter
import com.cometchat.uikit.kotlin.shared.formatters.FormatterUtils
import com.cometchat.uikit.kotlin.shared.resources.utils.Utils
import com.cometchat.uikit.kotlin.shared.spans.MentionMovementMethod
import com.cometchat.uikit.kotlin.shared.spans.TagSpan
import com.cometchat.uikit.kotlin.theme.CometChatTheme
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
            
            // Extract mention spans before markdown parsing
            val mentionSpans = if (formattedText is SpannableStringBuilder) {
                extractMentionSpans(formattedText)
            } else {
                emptyList()
            }
            
            // Parse markdown and render segments with mention spans
            val segments = MarkdownRenderer.parse(formattedText.toString())
            renderMarkdownSegments(segments, formattedText.toString(), mentionSpans)
            
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
     * Data class to hold extracted mention span information.
     */
    private data class MentionSpanInfo(
        val start: Int,
        val end: Int,
        val span: Any
    )
    
    /**
     * Extracts TagSpan (mention) spans from a SpannableStringBuilder.
     * TagSpan is used for clickable mentions in message bubbles.
     */
    private fun extractMentionSpans(spannable: SpannableStringBuilder): List<MentionSpanInfo> {
        val spans = spannable.getSpans(0, spannable.length, Any::class.java)
        return spans.mapNotNull { span ->
            // Check if it's a TagSpan (clickable mention span for bubbles)
            if (span is TagSpan) {
                val start = spannable.getSpanStart(span)
                val end = spannable.getSpanEnd(span)
                if (start >= 0 && end > start) {
                    MentionSpanInfo(start, end, span)
                } else null
            } else null
        }
    }
    
    /**
     * Finds mention spans that fall within a segment's text range.
     * Returns spans with positions adjusted relative to the segment's plain text.
     * 
     * The mention spans are positioned in the formatted text (after formatter processing).
     * We need to find which mentions fall within this segment and adjust their positions
     * to account for markdown markers that will be stripped.
     */
    private fun findMentionSpansForSegment(
        segmentText: String,
        originalText: String,
        originalOffset: Int,
        mentionSpans: List<MentionSpanInfo>
    ): List<MentionSpanInfo> {
        if (mentionSpans.isEmpty() || segmentText.isEmpty()) return emptyList()
        
        // The segmentText is the raw text from the segment (may contain markdown markers)
        // We need to find where this segment appears in the original formatted text
        
        // Find the segment in the original text starting from offset
        var segmentStartInOriginal = -1
        var searchStart = originalOffset
        
        // Try to find the segment text in the original
        // Account for the fact that segment text might span multiple lines
        val segmentLines = segmentText.split("\n")
        if (segmentLines.isNotEmpty()) {
            val firstLine = segmentLines[0]
            if (firstLine.isNotEmpty()) {
                segmentStartInOriginal = originalText.indexOf(firstLine, searchStart)
            }
        }
        
        if (segmentStartInOriginal < 0) {
            // Fallback: use offset directly
            segmentStartInOriginal = originalOffset
        }
        
        val segmentEndInOriginal = segmentStartInOriginal + segmentText.length
        
        // Find mention spans that overlap with this segment
        return mentionSpans.mapNotNull { mention ->
            // Check if mention overlaps with segment range in original text
            if (mention.end > segmentStartInOriginal && mention.start < segmentEndInOriginal) {
                // Calculate position relative to segment start
                val relativeStart = (mention.start - segmentStartInOriginal).coerceAtLeast(0)
                val relativeEnd = (mention.end - segmentStartInOriginal).coerceAtMost(segmentText.length)
                
                if (relativeEnd > relativeStart) {
                    MentionSpanInfo(relativeStart, relativeEnd, mention.span)
                } else null
            } else null
        }
    }
    
    /**
     * Renders markdown segments into the content container.
     */
    private fun renderMarkdownSegments(
        segments: List<MarkdownRenderer.RenderedSegment>,
        originalText: String = "",
        mentionSpans: List<MentionSpanInfo> = emptyList()
    ) {
        markdownContentContainer.removeAllViews()
        markdownContentContainer.visibility = View.VISIBLE
        messageTextView.visibility = View.GONE
        
        val currentStyle = style ?: return
        val textColor = if (currentStyle.textColor != 0) currentStyle.textColor else CometChatTheme.getTextColorPrimary(context)
        val linkColor = if (currentStyle.textLinkColor != 0) currentStyle.textLinkColor else CometChatTheme.getInfoColor(context)
        
        // Track position in original text to map mention spans to segments
        var originalOffset = 0
        
        for (segment in segments) {
            when (segment) {
                is MarkdownRenderer.RenderedSegment.Text -> {
                    val textView = createTextView()
                    // Find mention spans that fall within this segment's range in original text
                    val segmentMentions = findMentionSpansForSegment(
                        segment.text, originalText, originalOffset, mentionSpans
                    )
                    val styledText = buildStyledText(segment.text, segment.spans, textColor, linkColor, segmentMentions)
                    textView.text = styledText
                    markdownContentContainer.addView(textView)
                    // Update offset: segment text length + newline
                    originalOffset += segment.text.length + 1
                }
                
                is MarkdownRenderer.RenderedSegment.CodeBlock -> {
                    val codeBlockView = createCodeBlockView(segment.code, segment.language)
                    markdownContentContainer.addView(codeBlockView)
                }
                
                is MarkdownRenderer.RenderedSegment.BulletItem -> {
                    val bulletView = createBulletItemView(segment.text, segment.spans, textColor, linkColor)
                    markdownContentContainer.addView(bulletView)
                }
                
                is MarkdownRenderer.RenderedSegment.OrderedItem -> {
                    val orderedView = createOrderedItemView(segment.number, segment.text, segment.spans, textColor, linkColor)
                    markdownContentContainer.addView(orderedView)
                }
                
                is MarkdownRenderer.RenderedSegment.Blockquote -> {
                    val quoteView = createBlockquoteView(segment.text, segment.spans, textColor, linkColor)
                    markdownContentContainer.addView(quoteView)
                }
            }
        }
    }
    
    /**
     * Creates a basic TextView for text segments.
     * Sets MentionMovementMethod for handling mention click events.
     */
    private fun createTextView(): TextView {
        val currentStyle = style
        return TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            if (currentStyle?.textAppearance != 0) {
                setTextAppearance(currentStyle?.textAppearance ?: 0)
            }
            if (currentStyle?.textColor != 0) {
                setTextColor(currentStyle?.textColor ?: CometChatTheme.getTextColorPrimary(context))
            }
            if (currentStyle?.textLinkColor != 0) {
                setLinkTextColor(currentStyle?.textLinkColor ?: CometChatTheme.getInfoColor(context))
            }
            // Set MentionMovementMethod for handling TagSpan clicks
            movementMethod = MentionMovementMethod.getInstance()
        }
    }
    
    /**
     * Creates a code block view with dark background and monospace font.
     */
    private fun createCodeBlockView(code: String, language: String): View {
        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dpToPx(4)
                bottomMargin = dpToPx(4)
            }
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#1E1E1E"))
                cornerRadius = dpToPx(8).toFloat()
            }
        }
        
        // Language label
        if (language.isNotEmpty()) {
            val languageLabel = TextView(context).apply {
                text = language
                setTextColor(Color.parseColor("#888888"))
                typeface = Typeface.MONOSPACE
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
                setPadding(dpToPx(12), dpToPx(8), dpToPx(12), 0)
            }
            container.addView(languageLabel)
        }
        
        // Horizontal scroll for code
        val scrollView = HorizontalScrollView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            isHorizontalScrollBarEnabled = false
        }
        
        val codeTextView = TextView(context).apply {
            text = code
            setTextColor(Color.parseColor("#D4D4D4"))
            typeface = Typeface.MONOSPACE
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            setPadding(dpToPx(12), dpToPx(8), dpToPx(12), dpToPx(8))
        }
        
        scrollView.addView(codeTextView)
        container.addView(scrollView)
        
        return container
    }
    
    /**
     * Creates a bullet list item view.
     */
    private fun createBulletItemView(
        itemText: String,
        spans: List<MarkdownRenderer.InlineSpan>,
        @ColorInt textColor: Int,
        @ColorInt linkColor: Int
    ): View {
        val container = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        
        val bulletTextView = TextView(context).apply {
            text = "•  "
            setTextColor(linkColor)
            setTypeface(typeface, Typeface.BOLD)
            if (style?.textAppearance != 0) {
                setTextAppearance(style?.textAppearance ?: 0)
            }
        }
        
        val contentTextView = createTextView().apply {
            val styledText = buildStyledText(itemText, spans, textColor, linkColor)
            text = styledText
        }
        
        container.addView(bulletTextView)
        container.addView(contentTextView)
        
        return container
    }
    
    /**
     * Creates an ordered list item view.
     */
    private fun createOrderedItemView(
        number: Int,
        itemText: String,
        spans: List<MarkdownRenderer.InlineSpan>,
        @ColorInt textColor: Int,
        @ColorInt linkColor: Int
    ): View {
        val container = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        
        val numberTextView = TextView(context).apply {
            text = "$number. "
            setTextColor(linkColor)
            setTypeface(typeface, Typeface.BOLD)
            if (style?.textAppearance != 0) {
                setTextAppearance(style?.textAppearance ?: 0)
            }
        }
        
        val contentTextView = createTextView().apply {
            val styledText = buildStyledText(itemText, spans, textColor, linkColor)
            text = styledText
        }
        
        container.addView(numberTextView)
        container.addView(contentTextView)
        
        return container
    }
    
    /**
     * Creates a blockquote view with vertical bar indicator.
     */
    private fun createBlockquoteView(
        itemText: String,
        spans: List<MarkdownRenderer.InlineSpan>,
        @ColorInt textColor: Int,
        @ColorInt linkColor: Int
    ): View {
        val container = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        
        val quoteBarTextView = TextView(context).apply {
            text = "┃  "
            setTextColor(linkColor)
            setTypeface(typeface, Typeface.BOLD)
            if (style?.textAppearance != 0) {
                setTextAppearance(style?.textAppearance ?: 0)
            }
        }
        
        // Quote text is slightly dimmed
        val quoteTextColor = adjustAlpha(textColor, 0.6f)
        val contentTextView = createTextView().apply {
            val styledText = buildStyledText(itemText, spans, quoteTextColor, linkColor)
            text = styledText
            setTextColor(quoteTextColor)
        }
        
        container.addView(quoteBarTextView)
        container.addView(contentTextView)
        
        return container
    }
    
    /**
     * Builds styled text with inline formatting spans applied.
     */
    private fun buildStyledText(
        text: String,
        spans: List<MarkdownRenderer.InlineSpan>,
        @ColorInt defaultColor: Int,
        @ColorInt linkColor: Int,
        mentionSpans: List<MentionSpanInfo> = emptyList()
    ): SpannableString {
        // First parse inline markdown to get clean text and spans
        val (plainText, inlineSpans) = MarkdownRenderer.parseInline(text)
        
        val spannable = SpannableString(plainText)
        
        // Apply base color
        spannable.setSpan(
            ForegroundColorSpan(defaultColor),
            0,
            plainText.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        
        // Apply mention spans first (they take priority for styling)
        // We need to map positions from original text to plain text
        if (mentionSpans.isNotEmpty()) {
            val positionMap = buildPositionMap(text, plainText)
            for (mention in mentionSpans) {
                val mappedStart = mapPosition(mention.start, positionMap, plainText.length)
                val mappedEnd = mapPosition(mention.end, positionMap, plainText.length)
                if (mappedStart >= 0 && mappedEnd > mappedStart && mappedEnd <= plainText.length) {
                    spannable.setSpan(
                        mention.span,
                        mappedStart,
                        mappedEnd,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            }
        }
        
        // Apply inline formatting spans
        for (span in inlineSpans) {
            val start = span.start.coerceAtMost(plainText.length)
            val end = span.end.coerceAtMost(plainText.length)
            if (start >= end) continue
            
            when (span.format) {
                RichTextFormat.BOLD -> {
                    spannable.setSpan(
                        StyleSpan(Typeface.BOLD),
                        start,
                        end,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
                RichTextFormat.ITALIC -> {
                    spannable.setSpan(
                        StyleSpan(Typeface.ITALIC),
                        start,
                        end,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
                RichTextFormat.UNDERLINE -> {
                    spannable.setSpan(
                        UnderlineSpan(),
                        start,
                        end,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
                RichTextFormat.STRIKETHROUGH -> {
                    spannable.setSpan(
                        StrikethroughSpan(),
                        start,
                        end,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
                RichTextFormat.INLINE_CODE -> {
                    spannable.setSpan(
                        TypefaceSpan("monospace"),
                        start,
                        end,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    spannable.setSpan(
                        BackgroundColorSpan(adjustAlpha(Color.GRAY, 0.3f)),
                        start,
                        end,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
                RichTextFormat.LINK -> {
                    // Add URLSpan to make the link clickable
                    span.url?.let { url ->
                        spannable.setSpan(
                            android.text.style.URLSpan(url),
                            start,
                            end,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
                    spannable.setSpan(
                        ForegroundColorSpan(linkColor),
                        start,
                        end,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    spannable.setSpan(
                        UnderlineSpan(),
                        start,
                        end,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
                else -> { /* Line-level formats handled at segment level */ }
            }
        }
        
        return spannable
    }
    
    /**
     * Builds a position map from original text positions to plain text positions.
     * This accounts for markdown markers that are stripped.
     */
    private fun buildPositionMap(original: String, plain: String): IntArray {
        // Simple approach: for each position in original, find corresponding position in plain
        // by matching characters
        val map = IntArray(original.length + 1) { -1 }
        var plainIdx = 0
        var origIdx = 0
        
        while (origIdx < original.length && plainIdx < plain.length) {
            if (original[origIdx] == plain[plainIdx]) {
                map[origIdx] = plainIdx
                plainIdx++
            }
            origIdx++
        }
        // Map end position
        map[original.length] = plain.length
        
        return map
    }
    
    /**
     * Maps a position from original text to plain text using the position map.
     */
    private fun mapPosition(pos: Int, map: IntArray, plainLength: Int): Int {
        if (pos < 0) return -1
        if (pos >= map.size) return plainLength
        
        val mapped = map[pos]
        if (mapped >= 0) return mapped
        
        // If exact position not mapped, find nearest mapped position
        for (i in pos downTo 0) {
            if (map[i] >= 0) return map[i]
        }
        return 0
    }
    
    /**
     * Adjusts the alpha of a color.
     */
    private fun adjustAlpha(@ColorInt color: Int, factor: Float): Int {
        val alpha = (Color.alpha(color) * factor).toInt()
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color))
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
        // Parse markdown for plain string input
        val segments = MarkdownRenderer.parse(text)
        renderMarkdownSegments(segments)
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
