package com.cometchat.uikit.kotlin.shared.formatters

import android.content.Context
import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.TypefaceSpan
import android.text.style.UnderlineSpan
import com.cometchat.chat.models.BaseMessage
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.formatter.RichTextFormat
import com.cometchat.uikit.core.formatter.RichTextSpanManager

/**
 * Text formatter that parses markdown syntax and applies visual formatting spans.
 * Matches the Java CometChatRichTextFormatter behavior.
 *
 * Supported: **bold**, _italic_, ~~strikethrough~~, `inline code`, ```code block```, <u>underline</u>
 */
class CometChatRichTextFormatter : CometChatTextFormatter('\u0000') {

    private val spanManager = RichTextSpanManager()

    override fun search(context: Context, queryString: String?) { /* no-op */ }
    override fun onScrollToBottom() { /* no-op */ }

    override fun prepareLeftMessageBubbleSpan(
        context: Context, baseMessage: BaseMessage, spannable: SpannableStringBuilder
    ): SpannableStringBuilder = applyMarkdown(spannable)

    override fun prepareRightMessageBubbleSpan(
        context: Context, baseMessage: BaseMessage, spannable: SpannableStringBuilder
    ): SpannableStringBuilder = applyMarkdown(spannable)

    override fun prepareComposerSpan(
        context: Context, baseMessage: BaseMessage, spannable: SpannableStringBuilder
    ): SpannableStringBuilder = spannable

    override fun prepareConversationSpan(
        context: Context, baseMessage: BaseMessage, spannable: SpannableStringBuilder
    ): SpannableStringBuilder {
        val result = applyMarkdown(spannable)
        // Truncate to first line for conversation subtitle (matching Java)
        val text = result.toString()
        val newline = text.indexOf('\n')
        if (newline >= 0 && newline < result.length) {
            result.delete(newline, result.length)
        }
        return result
    }

    private fun applyMarkdown(spannable: SpannableStringBuilder): SpannableStringBuilder {
        if (spannable.isEmpty()) return spannable

        val originalText = spannable.toString()
        val (plainText, richSpans) = spanManager.fromMarkdown(originalText)

        if (richSpans.isEmpty()) return spannable

        // Preserve existing spans (e.g., mention spans from CometChatMentionsFormatter)
        val existingSpans = spannable.getSpans(0, spannable.length, Any::class.java)
        val spanData = existingSpans.map { span ->
            Triple(span, spannable.getSpanStart(span), spannable.getSpanEnd(span))
        }

        val result = SpannableStringBuilder(plainText)

        // Apply markdown format spans
        for (richSpan in richSpans) {
            if (richSpan.start >= 0 && richSpan.end <= result.length && richSpan.start < richSpan.end) {
                for (format in richSpan.formats) {
                    val androidSpan = formatToSpan(format) ?: continue
                    result.setSpan(androidSpan, richSpan.start, richSpan.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }
        }

        // Re-apply existing spans with position mapping
        val posMap = buildPositionMap(originalText, plainText)
        for ((span, origStart, origEnd) in spanData) {
            val newStart = mapPosition(origStart, posMap, plainText.length)
            val newEnd = mapPosition(origEnd, posMap, plainText.length)
            if (newStart >= 0 && newEnd > newStart && newEnd <= result.length) {
                try {
                    result.setSpan(span, newStart, newEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                } catch (_: Exception) { }
            }
        }

        return result
    }

    private fun formatToSpan(format: RichTextFormat): Any? = when (format) {
        RichTextFormat.BOLD -> StyleSpan(Typeface.BOLD)
        RichTextFormat.ITALIC -> StyleSpan(Typeface.ITALIC)
        RichTextFormat.STRIKETHROUGH -> StrikethroughSpan()
        RichTextFormat.UNDERLINE -> UnderlineSpan()
        RichTextFormat.INLINE_CODE -> TypefaceSpan("monospace")
        RichTextFormat.CODE_BLOCK -> TypefaceSpan("monospace")
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
