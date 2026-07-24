package com.cometchat.uikit.compose.presentation.shared.messagebubble.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.cometchat.chat.models.User
import com.cometchat.uikit.compose.presentation.shared.mentions.MentionDisplayContext
import com.cometchat.uikit.compose.presentation.shared.mentions.MentionText
import com.cometchat.uikit.compose.presentation.shared.mentions.MentionTextStyle
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.uikit.core.formatter.MarkdownRenderer

/**
 * Renders parsed markdown as a column of block-level segments: paragraphs, fenced code blocks
 * (container + language label), blockquotes (quote bar) and indented bullet / ordered list rows.
 *
 * Shared by [CometChatTextBubble] and by media-message captions ([MultiAttachmentCaption]) so a
 * code block, quote or list looks identical wherever it appears. Inline formatting (bold, italic,
 * links, inline code) is applied per segment by [buildSegmentText], which also re-overlays the
 * spans a [com.cometchat.uikit.compose.presentation.shared.formatters.CometChatTextFormatter]
 * produced (mentions).
 *
 * @param sourceText The markdown to render. This must be the FORMATTER OUTPUT text (not the raw
 *   message text) when [formattedText] is non-null — formatters run before markdown so the two
 *   sets of span offsets line up.
 * @param formattedText Formatter output whose spans are overlaid, or null when no formatters ran.
 * @param mentionedUsers Mentioned users; with [onMentionClick] set, paragraphs render as clickable
 *   [MentionText] instead of plain styled text. Captions pass no click handler, so their mentions
 *   are styled but inert.
 */
@Composable
internal fun MarkdownSegments(
    sourceText: String,
    formattedText: AnnotatedString?,
    textStyle: TextStyle,
    textColor: Color,
    linkColor: Color,
    isOutgoing: Boolean,
    modifier: Modifier = Modifier,
    mentionedUsers: List<User> = emptyList(),
    mentionTextStyle: MentionTextStyle? = null,
    onLinkClick: ((String) -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    onMentionClick: ((User) -> Unit)? = null,
    onMentionAllClick: (() -> Unit)? = null
) {
    // Theme-aware inline code colors, differentiating receiver and sender bubbles.
    val inlineCodeBgColor = if (isOutgoing) {
        Color.White.copy(alpha = 0.2f)
    } else {
        CometChatTheme.colorScheme.backgroundColor3
    }
    val inlineCodeTextColor = if (isOutgoing) {
        Color.Unspecified
    } else {
        CometChatTheme.colorScheme.textColorHighlight
    }
    val inlineCodeBorderColor = if (isOutgoing) {
        Color.White.copy(alpha = 0.3f)
    } else {
        CometChatTheme.colorScheme.strokeColorDark
    }

    val segments = remember(sourceText) { mergeBlockquotes(MarkdownRenderer.parse(sourceText)) }
    val layoutDirection = LocalLayoutDirection.current
    val useMentionText = mentionedUsers.isNotEmpty() && onMentionClick != null

    Column(modifier = modifier) {
        for ((index, segment) in segments.withIndex()) {
            // Breathing room around the two segments that render as their own container.
            if (index > 0) {
                val prev = segments[index - 1]
                val needsSpacing = prev.isContainer() || segment.isContainer()
                if (needsSpacing) Spacer(modifier = Modifier.height(6.dp))
            }

            when (segment) {
                is MarkdownRenderer.RenderedSegment.Text -> {
                    if (useMentionText) {
                        val effectiveTextStyle = remember(textStyle, layoutDirection) {
                            if (layoutDirection == LayoutDirection.Rtl) {
                                textStyle.copy(textDirection = TextDirection.Rtl)
                            } else {
                                textStyle
                            }
                        }
                        MentionText(
                            text = segment.text,
                            mentionedUsers = mentionedUsers,
                            onMentionClick = onMentionClick,
                            onMentionAllClick = onMentionAllClick,
                            style = mentionTextStyle ?: MentionTextStyle.forContext(
                                if (isOutgoing) MentionDisplayContext.OUTGOING_BUBBLE
                                else MentionDisplayContext.INCOMING_BUBBLE
                            ),
                            textStyle = effectiveTextStyle
                        )
                    } else {
                        ClickableLinkText(
                            text = segmentText(
                                segment.text, formattedText, sourceText, textColor, linkColor,
                                inlineCodeBgColor, inlineCodeTextColor
                            ),
                            style = textStyle,
                            onLinkClick = onLinkClick,
                            onLongClick = onLongClick,
                            inlineCodeBorderColor = inlineCodeBorderColor,
                            inlineCodeBgColor = inlineCodeBgColor
                        )
                    }
                }

                is MarkdownRenderer.RenderedSegment.CodeBlock -> CodeBlockBubble(
                    code = segment.code,
                    language = segment.language,
                    isOutgoing = isOutgoing
                )

                is MarkdownRenderer.RenderedSegment.BulletItem -> ListItemRow(
                    prefix = "• ",
                    text = segmentText(
                        segment.text, formattedText, sourceText, textColor, linkColor,
                        inlineCodeBgColor, inlineCodeTextColor
                    ),
                    textStyle = textStyle,
                    textColor = textColor,
                    onLinkClick = onLinkClick,
                    onLongClick = onLongClick,
                    inlineCodeBorderColor = inlineCodeBorderColor,
                    inlineCodeBgColor = inlineCodeBgColor
                )

                is MarkdownRenderer.RenderedSegment.OrderedItem -> ListItemRow(
                    prefix = "${segment.number}. ",
                    text = segmentText(
                        segment.text, formattedText, sourceText, textColor, linkColor,
                        inlineCodeBgColor, inlineCodeTextColor
                    ),
                    textStyle = textStyle,
                    textColor = textColor,
                    onLinkClick = onLinkClick,
                    onLongClick = onLongClick,
                    inlineCodeBorderColor = inlineCodeBorderColor,
                    inlineCodeBgColor = inlineCodeBgColor
                )

                is MarkdownRenderer.RenderedSegment.Blockquote -> BlockquoteBubble(
                    text = segmentText(
                        segment.text, formattedText, sourceText, textColor, linkColor,
                        inlineCodeBgColor, inlineCodeTextColor
                    ),
                    textStyle = textStyle,
                    isOutgoing = isOutgoing,
                    onLinkClick = onLinkClick,
                    onLongClick = onLongClick,
                    inlineCodeBorderColor = inlineCodeBorderColor,
                    inlineCodeBgColor = inlineCodeBgColor
                )
            }
        }
    }
}

@Composable
private fun ListItemRow(
    prefix: String,
    text: AnnotatedString,
    textStyle: TextStyle,
    textColor: Color,
    onLinkClick: ((String) -> Unit)?,
    onLongClick: (() -> Unit)?,
    inlineCodeBorderColor: Color,
    inlineCodeBgColor: Color
) {
    Row(modifier = Modifier.padding(start = 8.dp, top = 2.dp, bottom = 2.dp)) {
        Text(text = prefix, style = textStyle.copy(color = textColor))
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

@Composable
private fun segmentText(
    raw: String,
    formattedText: AnnotatedString?,
    fullText: String,
    textColor: Color,
    linkColor: Color,
    inlineCodeBgColor: Color,
    inlineCodeTextColor: Color
): AnnotatedString = remember(raw, formattedText, textColor, linkColor) {
    buildSegmentText(raw, formattedText, fullText, textColor, linkColor, inlineCodeBgColor, inlineCodeTextColor)
}

/** Code blocks and blockquotes render as their own container; the rest are inline-height rows. */
private fun MarkdownRenderer.RenderedSegment.isContainer(): Boolean =
    this is MarkdownRenderer.RenderedSegment.CodeBlock ||
        this is MarkdownRenderer.RenderedSegment.Blockquote

/**
 * Merges consecutive blockquote segments into one so a multi-line quote gets a single continuous
 * bar rather than one bar per line. Span offsets shift by the joining newline.
 */
internal fun mergeBlockquotes(
    segments: List<MarkdownRenderer.RenderedSegment>
): List<MarkdownRenderer.RenderedSegment> {
    val merged = mutableListOf<MarkdownRenderer.RenderedSegment>()
    var i = 0
    while (i < segments.size) {
        val segment = segments[i]
        if (segment !is MarkdownRenderer.RenderedSegment.Blockquote) {
            merged.add(segment)
            i++
            continue
        }
        val texts = mutableListOf(segment.text)
        val spans = mutableListOf<MarkdownRenderer.InlineSpan>().apply { addAll(segment.spans) }
        var j = i + 1
        while (j < segments.size) {
            val next = segments[j] as? MarkdownRenderer.RenderedSegment.Blockquote ?: break
            val offset = texts.joinToString("\n").length + 1 // +1 for the joining newline
            texts.add(next.text)
            spans.addAll(next.spans.map { it.copy(start = it.start + offset, end = it.end + offset) })
            j++
        }
        merged.add(
            MarkdownRenderer.RenderedSegment.Blockquote(text = texts.joinToString("\n"), spans = spans)
        )
        i = j
    }
    return merged
}
