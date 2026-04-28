package com.cometchat.uikit.compose.presentation.shared.messagebubble.ui

import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.uikit.compose.theme.cometchatFontBold
import com.cometchat.uikit.core.formatter.MarkdownRenderer
import com.cometchat.uikit.core.formatter.RichTextFormat

/** Annotation tag for inline code spans — matches CometChatTextBubble's tag. */
private const val INLINE_CODE_ANNOTATION_TAG = "INLINE_CODE"
/** Annotation tag for blockquote spans — used to draw left stripe + background. */
private const val BLOCKQUOTE_ANNOTATION_TAG = "BLOCKQUOTE"

/**
 * Builds a single-line [AnnotatedString] from [MarkdownRenderer.RenderedSegment]s
 * for preview contexts (reply preview, edit preview, conversation subtitle).
 *
 * Flattens segments into a single line. For lists, shows only the first item.
 * Adds annotations for inline code and blockquote so [FormattedPreviewText]
 * can draw backgrounds/borders/stripes matching the bubble's visual style.
 */
fun buildPreviewAnnotatedString(
    segments: List<MarkdownRenderer.RenderedSegment>,
    textColor: Color,
    linkColor: Color,
    inlineCodeTextColor: Color = Color.Unspecified
): AnnotatedString = buildAnnotatedString {
    // Filter segments: for lists, keep only the first bullet/ordered item
    var seenBullet = false
    var seenOrdered = false
    val filteredSegments = segments.filter { segment ->
        when (segment) {
            is MarkdownRenderer.RenderedSegment.BulletItem -> {
                if (seenBullet) false else { seenBullet = true; true }
            }
            is MarkdownRenderer.RenderedSegment.OrderedItem -> {
                if (seenOrdered) false else { seenOrdered = true; true }
            }
            else -> true
        }
    }

    var isFirst = true
    for (segment in filteredSegments) {
        if (!isFirst) append(" ")
        isFirst = false

        when (segment) {
            is MarkdownRenderer.RenderedSegment.CodeBlock -> {
                // Add a small leading space so the background doesn't glue to preceding text
                if (!isFirst) append(" ")
                val startIdx = length
                append(segment.code)
                addStyle(SpanStyle(color = textColor), startIdx, length)
                addStyle(SpanStyle(fontFamily = FontFamily.Monospace), startIdx, length)
                addStringAnnotation(INLINE_CODE_ANNOTATION_TAG, "true", startIdx, length)
            }

            is MarkdownRenderer.RenderedSegment.Blockquote -> {
                val (plain, spans) = MarkdownRenderer.parseInline(segment.text)
                val startIdx = length
                // Add a small leading space so the background doesn't glue to preceding text
                append(" ")
                val contentStart = length
                append(plain)
                addStyle(SpanStyle(color = textColor), startIdx, length)
                applyInlineSpans(spans, contentStart, plain.length, linkColor, inlineCodeTextColor)
                // Mark as blockquote so FormattedPreviewText draws stripe + bg
                addStringAnnotation(BLOCKQUOTE_ANNOTATION_TAG, "true", contentStart, length)
            }

            is MarkdownRenderer.RenderedSegment.BulletItem -> {
                val (plain, spans) = MarkdownRenderer.parseInline(segment.text)
                val startIdx = length
                append("• ")
                append(plain)
                addStyle(SpanStyle(color = textColor), startIdx, length)
                applyInlineSpans(spans, startIdx + 2, plain.length, linkColor, inlineCodeTextColor)
            }

            is MarkdownRenderer.RenderedSegment.OrderedItem -> {
                val (plain, spans) = MarkdownRenderer.parseInline(segment.text)
                val prefix = "${segment.number}. "
                val startIdx = length
                append(prefix)
                append(plain)
                addStyle(SpanStyle(color = textColor), startIdx, length)
                applyInlineSpans(spans, startIdx + prefix.length, plain.length, linkColor, inlineCodeTextColor)
            }

            is MarkdownRenderer.RenderedSegment.Text -> {
                val (plain, spans) = MarkdownRenderer.parseInline(segment.text)
                val startIdx = length
                append(plain)
                addStyle(SpanStyle(color = textColor), startIdx, length)
                applyInlineSpans(spans, startIdx, plain.length, linkColor, inlineCodeTextColor)
            }
        }
    }
}

/**
 * Builds a single-line [AnnotatedString] for reply/edit preview contexts.
 *
 * This is a "partial" formatting mode:
 * - Code blocks, inline code, blockquotes → plain text (markers stripped, no visual treatment)
 * - Bold, italic, underline, strikethrough → keep the text decoration styling
 * - Links → plain display text only
 * - Lists → only first item shown
 */
fun buildReplyPreviewAnnotatedString(
    segments: List<MarkdownRenderer.RenderedSegment>,
    textColor: Color
): AnnotatedString = buildAnnotatedString {
    // Filter: only first bullet and first ordered item
    var seenBullet = false
    var seenOrdered = false
    val filtered = segments.filter { seg ->
        when (seg) {
            is MarkdownRenderer.RenderedSegment.BulletItem -> {
                if (seenBullet) false else { seenBullet = true; true }
            }
            is MarkdownRenderer.RenderedSegment.OrderedItem -> {
                if (seenOrdered) false else { seenOrdered = true; true }
            }
            else -> true
        }
    }

    var isFirst = true
    for (segment in filtered) {
        if (!isFirst) append(" ")
        isFirst = false

        // Extract text from segment
        val rawText = when (segment) {
            is MarkdownRenderer.RenderedSegment.CodeBlock -> segment.code
            is MarkdownRenderer.RenderedSegment.Text -> segment.text
            is MarkdownRenderer.RenderedSegment.BulletItem -> segment.text
            is MarkdownRenderer.RenderedSegment.OrderedItem -> segment.text
            is MarkdownRenderer.RenderedSegment.Blockquote -> segment.text
        }

        // Add list prefix
        val prefix = when (segment) {
            is MarkdownRenderer.RenderedSegment.BulletItem -> "• "
            is MarkdownRenderer.RenderedSegment.OrderedItem -> "${segment.number}. "
            else -> ""
        }

        if (segment is MarkdownRenderer.RenderedSegment.CodeBlock) {
            // Code blocks: plain text, no formatting at all
            val startIdx = length
            append(segment.code)
            addStyle(SpanStyle(color = textColor), startIdx, length)
        } else {
            // Parse inline to strip markers and get spans
            val (plain, spans) = MarkdownRenderer.parseInline(rawText)
            val startIdx = length
            append(prefix)
            append(plain)
            addStyle(SpanStyle(color = textColor), startIdx, length)

            // Apply ONLY bold, italic, underline, strikethrough — skip code/link visual treatment
            for (span in spans) {
                val s = (startIdx + prefix.length + span.start).coerceAtMost(startIdx + prefix.length + plain.length)
                val e = (startIdx + prefix.length + span.end).coerceAtMost(startIdx + prefix.length + plain.length)
                if (s >= e) continue

                when (span.format) {
                    RichTextFormat.BOLD -> addStyle(
                        SpanStyle(fontWeight = FontWeight.Bold, fontFamily = cometchatFontBold), s, e
                    )
                    RichTextFormat.ITALIC -> addStyle(
                        SpanStyle(fontStyle = FontStyle.Italic, fontFamily = FontFamily.Default), s, e
                    )
                    RichTextFormat.UNDERLINE -> addStyle(
                        SpanStyle(textDecoration = TextDecoration.Underline), s, e
                    )
                    RichTextFormat.STRIKETHROUGH -> addStyle(
                        SpanStyle(textDecoration = TextDecoration.LineThrough), s, e
                    )
                    // INLINE_CODE, LINK, BLOCKQUOTE, etc. → no visual treatment, just plain text
                    else -> { }
                }
            }
        }
    }
}


/**
 * Renders formatted preview text with visual treatments matching the message bubble:
 * - Inline code / code blocks: background tint + border (like ClickableLinkText)
 * - Blockquotes: left stripe + background fill (like BlockquoteBubble)
 */
@Composable
fun FormattedPreviewText(
    text: AnnotatedString,
    style: TextStyle,
    modifier: Modifier = Modifier,
    maxLines: Int = 1,
    overflow: TextOverflow = TextOverflow.Ellipsis,
    inlineCodeBorderColor: Color = CometChatTheme.colorScheme.strokeColorDark,
    inlineCodeBgColor: Color = CometChatTheme.colorScheme.backgroundColor3,
    blockquoteStripeColor: Color = CometChatTheme.colorScheme.strokeColorHighlight,
    blockquoteBgColor: Color = CometChatTheme.colorScheme.backgroundColor3,
    inlineCodeCornerRadius: Float = 5f,
    blockquoteCornerRadius: Float = 8f
) {
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    val density = androidx.compose.ui.platform.LocalDensity.current
    val codeCornerPx = with(density) { inlineCodeCornerRadius.dp.toPx() }
    val codeBorderPx = with(density) { 1.dp.toPx() }
    val quoteCornerPx = with(density) { blockquoteCornerRadius.dp.toPx() }
    val quoteStripePx = with(density) { 3.dp.toPx() }

    val inlineCodeAnnotations = remember(text) {
        text.getStringAnnotations(tag = INLINE_CODE_ANNOTATION_TAG, start = 0, end = text.length)
    }
    val blockquoteAnnotations = remember(text) {
        text.getStringAnnotations(tag = BLOCKQUOTE_ANNOTATION_TAG, start = 0, end = text.length)
    }

    val hasAnnotations = inlineCodeAnnotations.isNotEmpty() || blockquoteAnnotations.isNotEmpty()

    val drawModifier = if (hasAnnotations) {
        modifier.drawWithContent {
            val layout = textLayoutResult
            if (layout != null) {
                val hPad = 6f
                val vPad = 3f

                // Draw blockquote backgrounds + stripes BEFORE content
                for (annotation in blockquoteAnnotations) {
                    val pathBounds = layout.getPathForRange(annotation.start, annotation.end)
                    val bounds = pathBounds.getBounds()
                    if (bounds.width > 0f && bounds.height > 0f) {
                        // Background
                        drawRoundRect(
                            color = blockquoteBgColor,
                            topLeft = Offset(bounds.left - hPad, bounds.top - vPad),
                            size = Size(bounds.width + hPad * 2, bounds.height + vPad * 2),
                            cornerRadius = CornerRadius(quoteCornerPx, quoteCornerPx)
                        )
                        // Left stripe clipped to rounded rect
                        val path = Path().apply {
                            addRoundRect(
                                androidx.compose.ui.geometry.RoundRect(
                                    left = bounds.left - hPad,
                                    top = bounds.top - vPad,
                                    right = bounds.left - hPad + bounds.width + hPad * 2,
                                    bottom = bounds.top - vPad + bounds.height + vPad * 2,
                                    cornerRadius = CornerRadius(quoteCornerPx, quoteCornerPx)
                                )
                            )
                        }
                        clipPath(path) {
                            drawRect(
                                color = blockquoteStripeColor,
                                topLeft = Offset(bounds.left - hPad, bounds.top - vPad),
                                size = Size(quoteStripePx, bounds.height + vPad * 2)
                            )
                        }
                    }
                }

                // Draw inline code backgrounds BEFORE content
                for (annotation in inlineCodeAnnotations) {
                    val pathBounds = layout.getPathForRange(annotation.start, annotation.end)
                    val bounds = pathBounds.getBounds()
                    if (bounds.width > 0f && bounds.height > 0f) {
                        drawRoundRect(
                            color = inlineCodeBgColor,
                            topLeft = Offset(bounds.left - hPad, bounds.top - vPad),
                            size = Size(bounds.width + hPad * 2, bounds.height + vPad * 2),
                            cornerRadius = CornerRadius(codeCornerPx, codeCornerPx)
                        )
                    }
                }
            }

            drawContent()

            // Draw borders AFTER content
            if (layout != null) {
                val hPad = 6f
                val vPad = 3f
                for (annotation in inlineCodeAnnotations) {
                    val pathBounds = layout.getPathForRange(annotation.start, annotation.end)
                    val bounds = pathBounds.getBounds()
                    if (bounds.width > 0f && bounds.height > 0f) {
                        drawRoundRect(
                            color = inlineCodeBorderColor,
                            topLeft = Offset(bounds.left - hPad, bounds.top - vPad),
                            size = Size(bounds.width + hPad * 2, bounds.height + vPad * 2),
                            cornerRadius = CornerRadius(codeCornerPx, codeCornerPx),
                            style = Stroke(width = codeBorderPx)
                        )
                    }
                }
            }
        }
    } else {
        modifier
    }

    BasicText(
        text = text,
        style = style,
        maxLines = maxLines,
        overflow = overflow,
        onTextLayout = { layoutResult ->
            textLayoutResult = layoutResult
        },
        modifier = drawModifier
    )
}


/**
 * Applies inline formatting [SpanStyle]s to the [AnnotatedString.Builder].
 * Mappings match `buildStyledText()` in CometChatTextBubble.kt.
 */
private fun AnnotatedString.Builder.applyInlineSpans(
    spans: List<MarkdownRenderer.InlineSpan>,
    baseOffset: Int,
    textLength: Int,
    linkColor: Color,
    inlineCodeTextColor: Color
) {
    for (span in spans) {
        val start = (baseOffset + span.start).coerceAtMost(baseOffset + textLength)
        val end = (baseOffset + span.end).coerceAtMost(baseOffset + textLength)
        if (start >= end) continue

        val decorations = mutableListOf<TextDecoration>()

        when (span.format) {
            RichTextFormat.BOLD -> addStyle(
                SpanStyle(fontWeight = FontWeight.Bold, fontFamily = cometchatFontBold),
                start, end
            )
            RichTextFormat.ITALIC -> addStyle(
                SpanStyle(fontStyle = FontStyle.Italic, fontFamily = FontFamily.Default),
                start, end
            )
            RichTextFormat.UNDERLINE -> decorations.add(TextDecoration.Underline)
            RichTextFormat.STRIKETHROUGH -> decorations.add(TextDecoration.LineThrough)
            RichTextFormat.INLINE_CODE -> {
                addStyle(
                    SpanStyle(fontFamily = FontFamily.Monospace, color = inlineCodeTextColor),
                    start, end
                )
                addStringAnnotation(INLINE_CODE_ANNOTATION_TAG, "true", start, end)
            }
            RichTextFormat.LINK -> {
                decorations.add(TextDecoration.Underline)
                addStyle(SpanStyle(color = linkColor), start, end)
                span.url?.let { url ->
                    addStringAnnotation("URL", url, start, end)
                }
            }
            else -> { /* block-level formats not applicable in preview */ }
        }

        if (decorations.isNotEmpty()) {
            addStyle(
                SpanStyle(textDecoration = TextDecoration.combine(decorations)),
                start, end
            )
        }
    }
}
