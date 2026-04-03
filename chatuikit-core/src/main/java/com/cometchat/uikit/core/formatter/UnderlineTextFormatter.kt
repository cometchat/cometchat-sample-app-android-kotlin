package com.cometchat.uikit.core.formatter

import android.text.SpannableString
import android.text.Spanned
import android.text.style.UnderlineSpan
import java.util.regex.Pattern

/**
 * Formatter for underline text using HTML syntax: <u>text</u>
 *
 * Applies underline formatting to selected text by wrapping it with <u></u> tags.
 * Renders preview with UnderlineSpan.
 */
class UnderlineTextFormatter : RichTextFormatter {

    override val formatType: RichTextFormat = RichTextFormat.UNDERLINE
    override val markdownPrefix: String = "<u>"
    override val markdownSuffix: String = "</u>"

    override fun applyFormat(text: String, selectionStart: Int, selectionEnd: Int): FormattedResult {
        val safeStart = selectionStart.coerceIn(0, text.length)
        val safeEnd = selectionEnd.coerceIn(0, text.length)
        val (actualStart, actualEnd) = if (safeStart <= safeEnd) safeStart to safeEnd else safeEnd to safeStart

        val selectedText = text.substring(actualStart, actualEnd)
        val formattedText = "$markdownPrefix$selectedText$markdownSuffix"
        val newText = text.replaceRange(actualStart, actualEnd, formattedText)
        return FormattedResult(
            text = newText,
            newSelectionStart = actualStart + markdownPrefix.length,
            newSelectionEnd = actualEnd + markdownPrefix.length
        )
    }

    override fun removeFormat(text: String, selectionStart: Int, selectionEnd: Int): FormattedResult {
        val safeStart = selectionStart.coerceIn(0, text.length)
        val safeEnd = selectionEnd.coerceIn(0, text.length)
        val (actualStart, actualEnd) = if (safeStart <= safeEnd) safeStart to safeEnd else safeEnd to safeStart

        val beforeStart = maxOf(0, actualStart - markdownPrefix.length)
        val afterEnd = minOf(text.length, actualEnd + markdownSuffix.length)

        if (beforeStart < actualStart && actualEnd < afterEnd &&
            text.substring(beforeStart, actualStart) == markdownPrefix &&
            text.substring(actualEnd, afterEnd) == markdownSuffix) {
            val newText = text.removeRange(actualEnd, afterEnd)
                .removeRange(beforeStart, actualStart)
            return FormattedResult(
                text = newText,
                newSelectionStart = beforeStart,
                newSelectionEnd = actualEnd - markdownPrefix.length
            )
        }
        return FormattedResult(text, actualStart, actualEnd)
    }

    override fun isFormatApplied(text: String, selectionStart: Int, selectionEnd: Int): Boolean {
        val safeStart = selectionStart.coerceIn(0, text.length)
        val safeEnd = selectionEnd.coerceIn(0, text.length)
        val (actualStart, actualEnd) = if (safeStart <= safeEnd) safeStart to safeEnd else safeEnd to safeStart

        val beforeStart = maxOf(0, actualStart - markdownPrefix.length)
        val afterEnd = minOf(text.length, actualEnd + markdownSuffix.length)

        return beforeStart < actualStart && actualEnd < afterEnd &&
               text.substring(beforeStart, actualStart) == markdownPrefix &&
               text.substring(actualEnd, afterEnd) == markdownSuffix
    }

    override fun getFormattedPreview(text: String): SpannableString {
        val pattern = Pattern.compile("<u>(.+?)</u>")
        val matcher = pattern.matcher(text)

        val displayText = StringBuilder(text)
        val spans = mutableListOf<Triple<Int, Int, Any>>()
        var offset = 0

        while (matcher.find()) {
            val start = matcher.start() - offset
            val end = matcher.end() - offset
            val content = matcher.group(1) ?: ""
            displayText.replace(start, end, content)
            spans.add(Triple(start, start + content.length, UnderlineSpan()))
            offset += markdownPrefix.length + markdownSuffix.length
        }

        val spannable = SpannableString(displayText.toString())
        for ((spanStart, spanEnd, span) in spans) {
            if (spanStart >= 0 && spanEnd <= spannable.length) {
                spannable.setSpan(span, spanStart, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
        return spannable
    }
}
