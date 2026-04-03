package com.cometchat.uikit.compose.presentation.shared.messagebubble.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
        // Parse markdown on the formatter-transformed text so positions align
        val renderedSegments = remember(message.id, textForMarkdown) {
            MarkdownRenderer.parse(textForMarkdown)
        }

        // Render markdown segments with formatter spans overlaid
        val accentColor = style.linkColor

        Column(modifier = Modifier.padding(start = 12.dp, top = 12.dp, end = 12.dp)) {
            for (segment in renderedSegments) {
                when (segment) {
                    is MarkdownRenderer.RenderedSegment.Text -> {
                        // Use MentionText for clickable mentions when message has mentions
                        // and mention click callback is provided
                        if (hasMentions && onMentionClick != null) {
                            // Apply RTL-aware text style
                            val effectiveTextStyle = remember(style.textStyle, layoutDirection) {
                                if (layoutDirection == LayoutDirection.Rtl) {
                                    style.textStyle.copy(
                                        textDirection = androidx.compose.ui.text.style.TextDirection.Rtl
                                    )
                                } else {
                                    style.textStyle
                                }
                            }
                            
                            MentionText(
                                text = segment.text,
                                mentionedUsers = message.mentionedUsers ?: emptyList(),
                                onMentionClick = onMentionClick,
                                onMentionAllClick = onMentionAllClick,
                                style = effectiveMentionStyle,
                                textStyle = effectiveTextStyle
                            )
                        } else {
                            // Use ClickableText for messages without mentions to handle link clicks
                            val styledText = remember(segment.text, formattedText) {
                                buildSegmentText(segment.text, formattedText, message.text, style.textColor, style.linkColor)
                            }
                            ClickableLinkText(
                                text = styledText,
                                style = style.textStyle,
                                onLinkClick = onLinkClick
                            )
                        }
                    }

                    is MarkdownRenderer.RenderedSegment.CodeBlock -> {
                        CodeBlockBubble(
                            code = segment.code,
                            language = segment.language,
                            isOutgoing = alignment == UIKitConstants.MessageBubbleAlignment.RIGHT
                        )
                    }

                    is MarkdownRenderer.RenderedSegment.BulletItem -> {
                        val styledText = remember(segment.text, formattedText) {
                            buildSegmentText(segment.text, formattedText, message.text, style.textColor, style.linkColor)
                        }
                        Row {
                            Text(
                                text = "•  ",
                                style = style.textStyle.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = accentColor
                                )
                            )
                            ClickableLinkText(
                                text = styledText,
                                style = style.textStyle,
                                onLinkClick = onLinkClick
                            )
                        }
                    }

                    is MarkdownRenderer.RenderedSegment.OrderedItem -> {
                        val styledText = remember(segment.text, formattedText) {
                            buildSegmentText(segment.text, formattedText, message.text, style.textColor, style.linkColor)
                        }
                        Row {
                            Text(
                                text = "${segment.number}. ",
                                style = style.textStyle.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = accentColor
                                )
                            )
                            ClickableLinkText(
                                text = styledText,
                                style = style.textStyle,
                                onLinkClick = onLinkClick
                            )
                        }
                    }

                    is MarkdownRenderer.RenderedSegment.Blockquote -> {
                        val quoteTextColor = style.textColor.copy(alpha = 0.6f)
                        val styledText = remember(segment.text, formattedText) {
                            buildSegmentText(segment.text, formattedText, message.text, quoteTextColor, style.linkColor)
                        }
                        Row {
                            Text(
                                text = "┃  ",
                                style = style.textStyle.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontStyle = FontStyle.Normal,
                                    color = accentColor
                                )
                            )
                            ClickableLinkText(
                                text = styledText,
                                style = style.textStyle,
                                onLinkClick = onLinkClick
                            )
                        }
                    }
                }
            }
        }

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
 * A composable that renders text with clickable links.
 * Uses ClickableText to handle URL annotation clicks and opens links in browser.
 */
@Composable
private fun ClickableLinkText(
    text: AnnotatedString,
    style: androidx.compose.ui.text.TextStyle,
    onLinkClick: ((String) -> Unit)?
) {
    val context = LocalContext.current
    
    ClickableText(
        text = text,
        style = style,
        onClick = { offset ->
            // Check if there's a URL annotation at the clicked position
            text.getStringAnnotations(tag = URL_ANNOTATION_TAG, start = offset, end = offset)
                .firstOrNull()?.let { annotation ->
                    val url = annotation.item
                    if (onLinkClick != null) {
                        onLinkClick(url)
                    } else {
                        // Default behavior: open URL in browser
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            // Handle invalid URL or no browser available
                        }
                    }
                }
        }
    )
}

/**
 * Builds styled text for a markdown segment, combining markdown inline formatting
 * with text formatter spans (e.g., mentions). Since formatters are applied BEFORE
 * markdown parsing, the segment text and formatted text are now aligned, making
 * span overlay straightforward.
 */
private fun buildSegmentText(
    segmentText: String,
    formattedText: AnnotatedString?,
    fullText: String,
    defaultColor: Color,
    linkColor: Color
): AnnotatedString {
    // First, apply markdown inline formatting
    val (plain, spans) = MarkdownRenderer.parseInline(segmentText)
    val mdStyled = buildStyledText(plain, spans, defaultColor, linkColor)

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
 */
private fun buildPositionMap(original: String, plain: String): IntArray {
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
private fun mapPositionUsingMap(pos: Int, map: IntArray, plainLength: Int): Int {
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
 */
private fun buildStyledText(
    text: String,
    spans: List<MarkdownRenderer.InlineSpan>,
    defaultColor: Color,
    linkColor: Color
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
            RichTextFormat.INLINE_CODE -> addStyle(SpanStyle(
                fontFamily = FontFamily.Monospace,
                background = Color.LightGray.copy(alpha = 0.3f)
            ), start, end)
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
 * Renders a fenced code block with dark background, monospace font,
 * and optional language label.
 */
@Composable
private fun CodeBlockBubble(
    code: String,
    language: String,
    isOutgoing: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF1E1E1E))
    ) {
        if (language.isNotEmpty()) {
            Text(
                text = language,
                color = Color(0xFF888888),
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
                .horizontalScroll(rememberScrollState())
        ) {
            Text(
                text = code,
                color = Color(0xFFD4D4D4),
                style = androidx.compose.ui.text.TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp
                ),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }
    }
}
