package com.cometchat.uikit.compose.presentation.shared.formatters

import android.content.Context
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import com.cometchat.chat.models.BaseMessage
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.formatter.RichTextFormat
import com.cometchat.uikit.core.formatter.RichTextSpanManager

/**
 * Text formatter that parses markdown syntax and applies visual formatting.
 * Matches the Java CometChatRichTextFormatter behavior for Compose.
 *
 * Supported: **bold**, _italic_, ~~strikethrough~~, `inline code`, ```code block```, <u>underline</u>
 */
class CometChatRichTextFormatter : CometChatTextFormatter('\u0000') {

    private val spanManager = RichTextSpanManager()

    override fun search(context: Context, queryString: String?) { /* no-op */ }
    override fun onScrollToBottom() { /* no-op */ }

    override fun prepareLeftMessageBubbleSpan(
        context: Context, baseMessage: BaseMessage, text: AnnotatedString
    ): AnnotatedString = applyMarkdown(text)

    override fun prepareRightMessageBubbleSpan(
        context: Context, baseMessage: BaseMessage, text: AnnotatedString
    ): AnnotatedString = applyMarkdown(text)

    override fun prepareComposerSpan(
        context: Context, baseMessage: BaseMessage, text: AnnotatedString
    ): AnnotatedString = text

    override fun prepareConversationSpan(
        context: Context, baseMessage: BaseMessage, text: AnnotatedString
    ): AnnotatedString {
        val result = applyMarkdown(text)
        // Truncate to first line for conversation subtitle (matching Java)
        val newline = result.text.indexOf('\n')
        return if (newline >= 0) {
            result.subSequence(0, newline) as AnnotatedString
        } else {
            result
        }
    }

    private fun applyMarkdown(annotatedString: AnnotatedString): AnnotatedString {
        val originalText = annotatedString.text
        if (originalText.isEmpty()) return annotatedString

        val (plainText, richSpans) = spanManager.fromMarkdown(originalText)
        if (richSpans.isEmpty()) return annotatedString

        return buildAnnotatedString {
            append(plainText)

            // Apply markdown format spans
            for (richSpan in richSpans) {
                if (richSpan.start >= 0 && richSpan.end <= plainText.length && richSpan.start < richSpan.end) {
                    for (format in richSpan.formats) {
                        val style = formatToSpanStyle(format) ?: continue
                        addStyle(style, richSpan.start, richSpan.end)
                    }
                }
            }

            // Re-apply existing annotation spans with position mapping
            val posMap = buildPositionMap(originalText, plainText)
            for (range in annotatedString.spanStyles) {
                val newStart = mapPosition(range.start, posMap, plainText.length)
                val newEnd = mapPosition(range.end, posMap, plainText.length)
                if (newStart >= 0 && newEnd > newStart && newEnd <= plainText.length) {
                    addStyle(range.item, newStart, newEnd)
                }
            }
        }
    }

    private fun formatToSpanStyle(format: RichTextFormat): SpanStyle? = when (format) {
        RichTextFormat.BOLD -> SpanStyle(fontWeight = FontWeight.Bold)
        RichTextFormat.ITALIC -> SpanStyle(fontStyle = FontStyle.Italic)
        RichTextFormat.STRIKETHROUGH -> SpanStyle(textDecoration = TextDecoration.LineThrough)
        RichTextFormat.UNDERLINE -> SpanStyle(textDecoration = TextDecoration.Underline)
        RichTextFormat.INLINE_CODE -> SpanStyle(fontFamily = FontFamily.Monospace)
        RichTextFormat.CODE_BLOCK -> SpanStyle(fontFamily = FontFamily.Monospace)
        else -> null
    }

    private fun buildPositionMap(original: String, parsed: String): IntArray {
        val map = IntArray(original.length + 1) { -1 }
        var oi = 0
        var pi = 0
        while (oi < original.length && pi < parsed.length) {
            if (original[oi] == parsed[pi]) {
                map[oi] = pi
                oi++
                pi++
            } else {
                map[oi] = pi
                oi++
            }
        }
        while (oi <= original.length) {
            map[oi] = pi.coerceAtMost(parsed.length)
            oi++
        }
        return map
    }

    private fun mapPosition(pos: Int, map: IntArray, maxLen: Int): Int {
        if (pos < 0) return -1
        if (pos >= map.size) return maxLen
        return map[pos].coerceIn(0, maxLen)
    }
}
