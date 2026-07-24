package com.cometchat.uikit.compose.presentation.shared.messagebubble.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.drawscope.Stroke
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.cometchat.chat.models.TextMessage
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.formatter.MarkdownRenderer
import com.cometchat.uikit.core.formatter.RichTextFormat
import com.cometchat.uikit.compose.presentation.shared.formatters.CometChatTextFormatter
import com.cometchat.uikit.compose.presentation.shared.formatters.FormatterUtils
import com.cometchat.uikit.compose.presentation.shared.mentions.MentionDisplayContext
import com.cometchat.uikit.compose.presentation.shared.mentions.MentionText
import com.cometchat.uikit.compose.presentation.shared.mentions.MentionTextStyle
import com.cometchat.uikit.compose.presentation.shared.mentions.hasMentions
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatTextBubbleStyle
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.uikit.compose.theme.cometchatFontBold
import org.json.JSONObject

// URL annotation tag for clickable links
private const val URL_ANNOTATION_TAG = "URL"
// Annotation tag for inline code spans (used to draw borders)
private const val INLINE_CODE_ANNOTATION_TAG = "INLINE_CODE"

/**
 * A composable that displays a text message bubble.
 *
 * This component renders text messages with support for:
 * - Plain text display with formatting
 * - Link preview with title, description, and image
 * - Message translation display
 * - Edited message indicator
 * - Clickable mentions with appropriate styling for incoming/outgoing bubbles
 * - RTL layout direction support
 *
 * Example usage:
 * ```kotlin
 * CometChatTextBubble(
 *     message = textMessage,
 *     alignment = MessageBubbleAlignment.LEFT,
 *     style = CometChatTextBubbleStyle.incoming(),
 *     onMentionClick = { user -> navigateToUserProfile(user) }
 * )
 * ```
 *
 * @param message The [TextMessage] to display
 * @param alignment The bubble alignment (LEFT, RIGHT, or CENTER)
 * @param modifier Modifier for the bubble container
 * @param style Style configuration for the bubble appearance. Since [CometChatTextBubbleStyle]
 *              extends [CometChatMessageBubbleStyle], all wrapper properties (backgroundColor,
 *              cornerRadius, etc.) are directly accessible on the style object.
 * @param textFormatters List of text formatters for mention and other text formatting
 * @param onLinkClick Callback when a link in the text is clicked
 * @param onMentionClick Callback when a user mention is clicked, receives the User object
 * @param onMentionAllClick Callback when "mention all" (@all) is clicked
 * @param mentionTextStyle Optional custom style for mentions. If null, uses context-based defaults
 */
@Composable
fun CometChatTextBubble(
    message: TextMessage,
    alignment: UIKitConstants.MessageBubbleAlignment,
    modifier: Modifier = Modifier,
    style: CometChatTextBubbleStyle = when (alignment) {
        UIKitConstants.MessageBubbleAlignment.RIGHT -> CometChatTextBubbleStyle.outgoing()
        else -> CometChatTextBubbleStyle.incoming()
    },
    textFormatters: List<CometChatTextFormatter> = emptyList(),
    onLinkClick: ((String) -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    onMentionClick: ((User) -> Unit)? = null,
    onMentionAllClick: (() -> Unit)? = null,
    mentionTextStyle: MentionTextStyle? = null
) {
    // Extract link preview data if available
    val linkPreview = remember(message.id) {
        extractLinkPreview(message)
    }

    // Extract translated text if available
    val translatedText = remember(message.id, message.metadata) {
        extractTranslatedText(message)
    }

    val isEdited = message.editedAt > 0

    // Text bubble uses wrap_content width by default (no max width constraint)
    // Only link preview section has a fixed width (240dp) when present
    val hasLinkPreview = linkPreview != null

    // Apply text formatters FIRST to transform mention patterns (e.g., <@uid:xxx> -> @username)
    // This must happen before markdown parsing so positions align correctly
    val context = LocalContext.current
    // Include mentionedUsers in the key to ensure recomposition when mentions are set
    // This is critical for IN_PROGRESS messages where mentionedUsers is set before display
    val formattedText = remember(message.id, message.muid, message.text, message.mentionedUsers, textFormatters) {
        if (textFormatters.isNotEmpty()) {
            FormatterUtils.getFormattedText(
                context = context,
                baseMessage = message,
                formattingType = UIKitConstants.FormattingType.MESSAGE_BUBBLE,
                alignment = alignment,
                text = message.text,
                formatters = textFormatters
            )
        } else {
            null
        }
    }

    // Use the formatted text (with mentions transformed) for markdown parsing
    val textForMarkdown = formattedText?.text ?: message.text
    
    // Determine mention display context based on alignment
    val mentionDisplayContext = remember(alignment) {
        when (alignment) {
            UIKitConstants.MessageBubbleAlignment.RIGHT -> MentionDisplayContext.OUTGOING_BUBBLE
            UIKitConstants.MessageBubbleAlignment.LEFT -> MentionDisplayContext.INCOMING_BUBBLE
            else -> MentionDisplayContext.DEFAULT
        }
    }
    
    // Get effective mention style - use provided style or context-based default
    val effectiveMentionStyle = mentionTextStyle ?: MentionTextStyle.forContext(mentionDisplayContext)
    
    // Check if message has mentions for optimized rendering
    // Use muid as additional key for IN_PROGRESS messages (id=0)
    val hasMentions = remember(message.id, message.muid, message.mentionedUsers) {
        message.hasMentions()
    }
    
    // Get layout direction for RTL support
    val layoutDirection = LocalLayoutDirection.current

    Column(
        modifier = modifier
            .then(
                if (hasLinkPreview) {
                    // When link preview is present, use fixed width like Java implementation
                    Modifier.width(240.dp)
                } else {
                    // No max width constraint - bubble expands based on content
                    Modifier
                }
            )
            .semantics {
                contentDescription = "Text message: ${message.text}"
            }
    ) {
        val isOutgoing = alignment == UIKitConstants.MessageBubbleAlignment.RIGHT

        // Markdown is parsed from the formatter-transformed text so span offsets align. The block
        // rendering (code blocks, quotes, lists) is shared with media captions — see MarkdownSegments.
        MarkdownSegments(
            sourceText = textForMarkdown,
            formattedText = formattedText,
            textStyle = style.textStyle,
            textColor = style.textColor,
            linkColor = style.linkColor,
            isOutgoing = isOutgoing,
            modifier = Modifier.padding(start = 12.dp, top = 12.dp, end = 12.dp),
            mentionedUsers = if (hasMentions) message.mentionedUsers ?: emptyList() else emptyList(),
            mentionTextStyle = effectiveMentionStyle,
            onLinkClick = onLinkClick,
            onLongClick = onLongClick,
            onMentionClick = onMentionClick,
            onMentionAllClick = onMentionAllClick
        )

        // Link preview section
        if (linkPreview != null) {
            LinkPreviewSection(
                linkPreview = linkPreview,
                style = style,
                onLinkClick = onLinkClick
            )
        }

        // Translation section
        if (translatedText != null) {
            TranslationSection(
                translatedText = translatedText,
                style = style
            )
        }

        // Edited indicator
        if (isEdited) {
            Text(
                text = "Edited",
                style = style.translatedTextStyle,
                color = style.translatedTextColor,
                modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 8.dp)
            )
        }
    }
}

/**
 * Displays a link preview card with image, title, description, and URL.
 */
@Composable
private fun LinkPreviewSection(
    linkPreview: LinkPreviewData,
    style: CometChatTextBubbleStyle,
    onLinkClick: ((String) -> Unit)?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, top = 4.dp, end = 4.dp)
            .background(
                color = style.linkPreviewBackgroundColor,
                shape = RoundedCornerShape(style.linkPreviewCornerRadius)
            )
            .clip(RoundedCornerShape(style.linkPreviewCornerRadius))
    ) {
        // Banner image
        if (!linkPreview.imageUrl.isNullOrEmpty()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(linkPreview.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Link preview image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )
        } else if (!linkPreview.favIconUrl.isNullOrEmpty()) {
            // Show favicon if no banner image
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(linkPreview.favIconUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Site favicon",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
        }

        // Title
        if (linkPreview.title.isNotEmpty()) {
            Text(
                text = linkPreview.title,
                style = style.linkPreviewTitleStyle,
                color = style.linkPreviewTitleColor,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }

        // Description
        if (linkPreview.description.isNotEmpty()) {
            Text(
                text = linkPreview.description,
                style = style.linkPreviewDescriptionStyle,
                color = style.linkPreviewDescriptionColor,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }

        // URL
        Text(
            text = linkPreview.url,
            style = style.linkPreviewDescriptionStyle.copy(
                textDecoration = TextDecoration.Underline
            ),
            color = style.linkColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}

/**
 * Displays the translated text section with a separator.
 */
@Composable
private fun TranslationSection(
    translatedText: String,
    style: CometChatTextBubbleStyle
) {
    Column(
        modifier = Modifier.padding(horizontal = 12.dp)
    ) {
        HorizontalDivider(
            color = style.translatedTextColor.copy(alpha = 0.3f),
            thickness = 1.dp
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Translated message",
            style = style.translatedTextStyle,
            color = style.translatedTextColor
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = translatedText,
            style = style.textStyle,
            color = style.translatedTextColor,
            modifier = Modifier.padding(bottom = 8.dp)
        )
    }
}

/**
 * Data class for link preview information.
 */
private data class LinkPreviewData(
    val title: String,
    val description: String,
    val url: String,
    val imageUrl: String?,
    val favIconUrl: String?
)

// Extension JSON field constants (matching chatuikit ExtensionConstants.ExtensionJSONField)
private const val LINK_PREVIEW = "linkPreview"
private const val TITLE = "title"
private const val DESCRIPTION = "description"
private const val URL = "url"
private const val IMAGE = "image"
private const val FAV_ICON = "favicon"
private const val MESSAGE_TRANSLATED = "translated_message"

/**
 * Extracts link preview data from a text message's metadata.
 */
private fun extractLinkPreview(message: TextMessage): LinkPreviewData? {
    return try {
        val metadata = message.metadata ?: return null
        if (metadata.has("@injected")) {
            val injected = metadata.getJSONObject("@injected")
            if (injected.has("extensions")) {
                val extensions = injected.getJSONObject("extensions")
                if (extensions.has(LINK_PREVIEW)) {
                    val linkPreviewJson = extensions.getJSONObject(LINK_PREVIEW)
                    if (linkPreviewJson.has("links")) {
                        val linksArray = linkPreviewJson.getJSONArray("links")
                        if (linksArray.length() > 0) {
                            val firstLink = linksArray.getJSONObject(0)
                            return LinkPreviewData(
                                title = firstLink.optString(TITLE, ""),
                                description = firstLink.optString(DESCRIPTION, ""),
                                url = firstLink.optString(URL, ""),
                                imageUrl = firstLink.optString(IMAGE, null),
                                favIconUrl = firstLink.optString(FAV_ICON, null)
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

/**
 * Extracts translated text from a text message's metadata.
 */
private fun extractTranslatedText(message: TextMessage): String? {
    return try {
        message.metadata?.let { metadata ->
            if (metadata.has(MESSAGE_TRANSLATED)) {
                val translated = metadata.getString(MESSAGE_TRANSLATED)
                if (translated.isNotEmpty()) translated else null
            } else null
        }
    } catch (e: Exception) {
        null
    }
}

/**
 * A composable that renders text with clickable links and inline code borders.
 * Uses ClickableText to handle URL annotation clicks and opens links in browser.
 * Draws rounded rect borders around inline code spans using onTextLayout + drawWithContent.
 */
@Composable
internal fun ClickableLinkText(
    text: AnnotatedString,
    style: androidx.compose.ui.text.TextStyle,
    onLinkClick: ((String) -> Unit)?,
    onLongClick: (() -> Unit)? = null,
    inlineCodeBorderColor: Color = CometChatTheme.colorScheme.strokeColorDark,
    inlineCodeBgColor: Color = CometChatTheme.colorScheme.backgroundColor3,
    inlineCodeCornerRadius: Float = 5f
) {
    val context = LocalContext.current
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    val cornerRadiusPx = with(androidx.compose.ui.platform.LocalDensity.current) { inlineCodeCornerRadius.dp.toPx() }
    val borderWidthPx = with(androidx.compose.ui.platform.LocalDensity.current) { 1.dp.toPx() }

    // Get inline code annotations
    val inlineCodeAnnotations = remember(text) {
        text.getStringAnnotations(tag = INLINE_CODE_ANNOTATION_TAG, start = 0, end = text.length)
    }

    // Use BasicText + pointerInput instead of ClickableText so that long press
    // propagates to the parent (MessageListItem's combinedClickable).
    // ClickableText consumes ALL touch events, blocking long press.
    val drawModifier = if (inlineCodeAnnotations.isNotEmpty()) {
        Modifier.drawWithContent {
            val layout = textLayoutResult
            if (layout != null) {
                val hPad = 4f
                val vPad = 2f
                for (annotation in inlineCodeAnnotations) {
                    val startOffset = annotation.start
                    val endOffset = annotation.end
                    val pathBounds = layout.getPathForRange(startOffset, endOffset)
                    val bounds = pathBounds.getBounds()
                    if (bounds.width > 0f && bounds.height > 0f) {
                        val lineIdx = layout.getLineForOffset(startOffset)
                        val baseline = layout.getLineBaseline(lineIdx)
                        val ascent = layout.getLineTop(lineIdx) - baseline  // negative
                        val descent = layout.getLineBottom(lineIdx) - baseline // positive
                        // Use font metrics for tight text bounds, not full line height
                        val fontAscent = baseline + ascent  // = lineTop
                        val fontDescent = baseline + descent // = lineBottom
                        // But with increased lineHeight, lineTop/lineBottom include extra space.
                        // Use multiParagraph line metrics: top of text = baseline - fontSize, bottom = baseline + descent
                        val fontSize = layout.layoutInput.style.fontSize.value
                        val density = layout.layoutInput.density.density
                        val fontSizePx = fontSize * density
                        val textTop = baseline - fontSizePx * 0.8f  // approximate ascent
                        val textBottom = baseline + fontSizePx * 0.25f  // approximate descent
                        drawRoundRect(
                            color = inlineCodeBgColor,
                            topLeft = Offset(bounds.left - hPad, textTop - vPad),
                            size = Size(bounds.width + hPad * 2, textBottom - textTop + vPad * 2),
                            cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx)
                        )
                    }
                }
            }
            drawContent()
            if (layout != null) {
                val hPad = 4f
                val vPad = 2f
                for (annotation in inlineCodeAnnotations) {
                    val startOffset = annotation.start
                    val endOffset = annotation.end
                    val pathBounds = layout.getPathForRange(startOffset, endOffset)
                    val bounds = pathBounds.getBounds()
                    if (bounds.width > 0f && bounds.height > 0f) {
                        val lineIdx = layout.getLineForOffset(startOffset)
                        val baseline = layout.getLineBaseline(lineIdx)
                        val fontSize = layout.layoutInput.style.fontSize.value
                        val density = layout.layoutInput.density.density
                        val fontSizePx = fontSize * density
                        val textTop = baseline - fontSizePx * 0.8f
                        val textBottom = baseline + fontSizePx * 0.25f
                        drawRoundRect(
                            color = inlineCodeBorderColor,
                            topLeft = Offset(bounds.left - hPad, textTop - vPad),
                            size = Size(bounds.width + hPad * 2, textBottom - textTop + vPad * 2),
                            cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx),
                            style = Stroke(width = borderWidthPx)
                        )
                    }
                }
            }
        }
    } else Modifier

    // Increase line height when inline code is present so there's visible gap between lines
    val effectiveStyle = if (inlineCodeAnnotations.isNotEmpty()) {
        style.copy(lineHeight = (style.fontSize.value * 1.6f).sp)
    } else {
        style
    }

    BasicText(
        text = text,
        style = effectiveStyle,
        onTextLayout = { layoutResult ->
            textLayoutResult = layoutResult
        },
        modifier = drawModifier.pointerInput(text, onLinkClick) {
            detectTapGestures(
                onLongPress = { onLongClick?.invoke() },
                onTap = { offset ->
                    // Find which character was tapped
                    val layout = textLayoutResult ?: return@detectTapGestures
                    val charOffset = layout.getOffsetForPosition(offset)
                    // Check for URL annotation at tapped position
                    text.getStringAnnotations(tag = URL_ANNOTATION_TAG, start = charOffset, end = charOffset)
                        .firstOrNull()?.let { annotation ->
                            val url = annotation.item.let { rawUrl ->
                                if (!rawUrl.contains("://")) "https://$rawUrl" else rawUrl
                            }
                            if (onLinkClick != null) {
                                onLinkClick(url)
                            } else {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    // Handle invalid URL or no browser available
                                }
                            }
                        }
                }
                // Long press is NOT handled here — it propagates to the parent's
                // combinedClickable modifier for message actions (reply, copy, etc.)
            )
        }
    )
}

/**
 * Builds styled text for a markdown segment, combining markdown inline formatting
 * with text formatter spans (e.g., mentions). Since formatters are applied BEFORE
 * markdown parsing, the segment text and formatted text are now aligned, making
 * span overlay straightforward.
 */
internal fun buildSegmentText(
    segmentText: String,
    formattedText: AnnotatedString?,
    fullText: String,
    defaultColor: Color,
    linkColor: Color,
    inlineCodeBackgroundColor: Color = Color.LightGray.copy(alpha = 0.3f),
    inlineCodeTextColor: Color = Color.Unspecified
): AnnotatedString {
    // First, apply markdown inline formatting
    val (plain, spans) = MarkdownRenderer.parseInline(segmentText)
    val mdStyled = buildStyledText(plain, spans, defaultColor, linkColor, inlineCodeBackgroundColor, inlineCodeTextColor)

    if (formattedText == null) return mdStyled

    // Find where this segment's original text (before markdown stripping) appears in the formatted text
    val startInFormatted = formattedText.text.indexOf(segmentText)
    if (startInFormatted < 0) {
        // Segment not found in formatted text - try finding the plain text instead
        val startPlainInFormatted = formattedText.text.indexOf(plain)
        if (startPlainInFormatted < 0) return mdStyled
        
        // Overlay formatter spans onto the markdown-styled text
        return buildAnnotatedString {
            append(mdStyled)
            formattedText.spanStyles.forEach { spanStyle ->
                val overlapStart = (spanStyle.start - startPlainInFormatted).coerceAtLeast(0)
                val overlapEnd = (spanStyle.end - startPlainInFormatted).coerceAtMost(plain.length)
                if (overlapStart < overlapEnd) {
                    addStyle(spanStyle.item, overlapStart, overlapEnd)
                }
            }
        }
    }

    // Build a position map from segment text to plain text
    val positionMap = buildPositionMap(segmentText, plain)
    
    // Calculate the offset mapping from original segment to plain text (after markdown stripping)
    return buildAnnotatedString {
        append(mdStyled)
        
        // Apply formatter spans that fall within this segment's range
        formattedText.spanStyles.forEach { spanStyle ->
            // Check if this span overlaps with our segment in the formatted text
            val segmentEnd = startInFormatted + segmentText.length
            if (spanStyle.start < segmentEnd && spanStyle.end > startInFormatted) {
                // Calculate positions relative to the segment
                val relativeStart = (spanStyle.start - startInFormatted).coerceAtLeast(0)
                val relativeEnd = (spanStyle.end - startInFormatted).coerceAtMost(segmentText.length)
                
                // Map from segment positions to plain text positions using the position map
                val plainStart = mapPositionUsingMap(relativeStart, positionMap, plain.length)
                val plainEnd = mapPositionUsingMap(relativeEnd, positionMap, plain.length)
                
                if (plainStart < plainEnd && plainStart >= 0 && plainEnd <= plain.length) {
                    addStyle(spanStyle.item, plainStart, plainEnd)
                }
            }
        }
    }
}

/**
 * Builds a position map from original text positions to plain text positions.
 * This accounts for markdown markers that are stripped.
 *
 * Shared with the media-bubble captions ([MultiAttachmentCaption]), which overlay formatter
 * spans onto markdown-stripped text exactly the same way.
 */
internal fun buildPositionMap(original: String, plain: String): IntArray {
    // For each position in original, find corresponding position in plain
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
internal fun mapPositionUsingMap(pos: Int, map: IntArray, plainLength: Int): Int {
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
 * Builds an [AnnotatedString] from plain text and inline formatting spans.
 * Maps [MarkdownRenderer.InlineSpan] to Compose [SpanStyle]s.
 *
 * Sets [defaultColor] as the base span on the entire text so that
 * per-format span styles layer on top correctly. The caller should
 * NOT pass a separate `color` parameter to [Text] — the color lives
 * entirely inside the [AnnotatedString].
 *
 * For LINK format, adds a URL annotation that can be used with ClickableText
 * to open the link in a browser.
 *
 * For INLINE_CODE format, uses [inlineCodeBackgroundColor] and [inlineCodeTextColor]
 * to apply theme-aware styling that differentiates receiver and sender bubbles.
 */
private fun buildStyledText(
    text: String,
    spans: List<MarkdownRenderer.InlineSpan>,
    defaultColor: Color,
    linkColor: Color,
    inlineCodeBackgroundColor: Color = Color.LightGray.copy(alpha = 0.3f),
    inlineCodeTextColor: Color = Color.Unspecified
): AnnotatedString = buildAnnotatedString {
    append(text)
    // Base color for the entire text
    addStyle(SpanStyle(color = defaultColor), 0, text.length)

    for (span in spans) {
        val start = span.start.coerceAtMost(text.length)
        val end = span.end.coerceAtMost(text.length)
        if (start >= end) continue

        val decorations = mutableListOf<TextDecoration>()

        when (span.format) {
            RichTextFormat.BOLD -> addStyle(SpanStyle(
                fontWeight = FontWeight.Bold,
                fontFamily = cometchatFontBold
            ), start, end)
            RichTextFormat.ITALIC -> addStyle(SpanStyle(
                fontStyle = FontStyle.Italic,
                fontFamily = FontFamily.Default
            ), start, end)
            RichTextFormat.UNDERLINE -> decorations.add(TextDecoration.Underline)
            RichTextFormat.STRIKETHROUGH -> decorations.add(TextDecoration.LineThrough)
            RichTextFormat.INLINE_CODE -> {
                addStyle(SpanStyle(
                    fontFamily = FontFamily.Monospace,
                    color = inlineCodeTextColor
                ), start, end)
                // Add annotation so ClickableLinkText can draw background + borders
                addStringAnnotation(
                    tag = INLINE_CODE_ANNOTATION_TAG,
                    annotation = "true",
                    start = start,
                    end = end
                )
            }
            RichTextFormat.LINK -> {
                decorations.add(TextDecoration.Underline)
                addStyle(SpanStyle(color = linkColor), start, end)
                // Add URL annotation for click handling
                span.url?.let { url ->
                    addStringAnnotation(
                        tag = URL_ANNOTATION_TAG,
                        annotation = url,
                        start = start,
                        end = end
                    )
                }
            }
            else -> { /* line-level formats handled at segment level */ }
        }

        if (decorations.isNotEmpty()) {
            addStyle(SpanStyle(textDecoration = TextDecoration.combine(decorations)), start, end)
        }
    }
}

/**
 * Renders a fenced code block with theme-aware styling for receiver/sender bubbles,
 * monospace font, and optional language label.
 *
 * Receiver (left): BackgroundColor2 bg, StrokeColorDefault border, 16dp corners, 24dp/12dp padding
 * Sender (right): ExtendedPrimaryColor700 bg, appropriate border, 16dp corners, 24dp/12dp padding
 */
@Composable
internal fun CodeBlockBubble(
    code: String,
    language: String,
    isOutgoing: Boolean
) {
    val backgroundColor = if (isOutgoing) {
        CometChatTheme.colorScheme.extendedPrimaryColor700
    } else {
        CometChatTheme.colorScheme.backgroundColor2
    }
    val borderColor = if (isOutgoing) {
        CometChatTheme.colorScheme.extendedPrimaryColor700
    } else {
        CometChatTheme.colorScheme.strokeColorDefault
    }
    val textColor = if (isOutgoing) {
        Color.White
    } else {
        CometChatTheme.colorScheme.textColorPrimary
    }
    val languageLabelColor = if (isOutgoing) {
        Color.White.copy(alpha = 0.6f)
    } else {
        Color(0xFF888888)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
    ) {
        if (language.isNotEmpty()) {
            Text(
                text = language,
                color = languageLabelColor,
                style = androidx.compose.ui.text.TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp
                ),
                modifier = Modifier.padding(start = 12.dp, top = 8.dp, end = 12.dp)
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(
                text = code,
                color = textColor,
                style = androidx.compose.ui.text.TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp
                ),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }
    }
}

/**
 * Renders a blockquote with a left stripe, background fill, and indentation
 * using Modifier.drawBehind for proper visual rendering.
 *
 * Receiver (left): StrokeColorHighlight stripe, BackgroundColor3 bg, 8dp corners
 * Sender (right): White stripe, white at 20% opacity bg, 8dp corners
 */
@Composable
internal fun BlockquoteBubble(
    text: AnnotatedString,
    textStyle: androidx.compose.ui.text.TextStyle,
    isOutgoing: Boolean,
    onLinkClick: ((String) -> Unit)?,
    onLongClick: (() -> Unit)? = null,
    inlineCodeBorderColor: Color = CometChatTheme.colorScheme.strokeColorDark,
    inlineCodeBgColor: Color = CometChatTheme.colorScheme.backgroundColor3
) {
    val stripeColor = if (isOutgoing) {
        Color.White
    } else {
        CometChatTheme.colorScheme.strokeColorHighlight
    }
    val bgColor = if (isOutgoing) {
        Color.White.copy(alpha = 0.2f)
    } else {
        CometChatTheme.colorScheme.backgroundColor3
    }
    val stripeWidthDp = 3.dp
    val cornerRadiusDp = 8.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                val cornerRadiusPx = cornerRadiusDp.toPx()
                val stripeWidthPx = stripeWidthDp.toPx()

                // Draw background with rounded corners
                drawRoundRect(
                    color = bgColor,
                    topLeft = Offset.Zero,
                    size = size,
                    cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx)
                )

                // Draw stripe flush with the left edge of the background,
                // sharing the background's corner radius. The stripe occupies
                // the leftmost 3px, clipped to the background's rounded shape.
                val path = Path().apply {
                    addRoundRect(
                        RoundRect(
                            left = 0f,
                            top = 0f,
                            right = size.width,
                            bottom = size.height,
                            cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx)
                        )
                    )
                }
                clipPath(path) {
                    // Draw a plain rect for the stripe — clipping handles the corners
                    drawRect(
                        color = stripeColor,
                        topLeft = Offset.Zero,
                        size = Size(stripeWidthPx, size.height)
                    )
                }
            }
            .padding(start = 16.dp, top = 8.dp, end = 12.dp, bottom = 8.dp)
    ) {
        ClickableLinkText(
            text = text,
            style = textStyle,
            onLinkClick = onLinkClick,
            onLongClick = onLongClick,
            inlineCodeBorderColor = inlineCodeBorderColor,
            inlineCodeBgColor = inlineCodeBgColor
        )
    }
}
