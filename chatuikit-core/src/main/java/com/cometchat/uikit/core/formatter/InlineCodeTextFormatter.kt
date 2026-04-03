package com.cometchat.uikit.core.formatter

import android.graphics.Color
import android.text.SpannableString
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.text.style.TypefaceSpan
import java.util.regex.Pattern

/**
 * Formatter for inline code text using markdown syntax: `text`
 * 
 * Applies inline code formatting to selected text by wrapping it with backticks.
 * Renders preview with BackgroundColorSpan and TypefaceSpan("monospace").
 */
class InlineCodeTextFormatter(
    private val codeBackgroundColor: Int = Color.parseColor("#E8E8E8")
) : RichTextFormatter {
    
    override val formatType: RichTextFormat = RichTextFormat.INLINE_CODE
    override val markdownPrefix: String = "`"
    override val markdownSuffix: String = "`"
    
    /**
     * Applies inline code formatting to the selected text.
     * Wraps the selection with ` markers.
     */
    override fun applyFormat(text: String, selectionStart: Int, selectionEnd: Int): FormattedResult {
        // Validate bounds to prevent StringIndexOutOfBoundsException
        val safeStart = selectionStart.coerceIn(0, text.length)
        val safeEnd = selectionEnd.coerceIn(0, text.length)
        
        // Ensure start <= end
        val (actualStart, actualEnd) = if (safeStart <= safeEnd) {
            safeStart to safeEnd
        } else {
            safeEnd to safeStart
        }
        
        val selectedText = text.substring(actualStart, actualEnd)
        val formattedText = "$markdownPrefix$selectedText$markdownSuffix"
        val newText = text.replaceRange(actualStart, actualEnd, formattedText)
        return FormattedResult(
            text = newText,
            newSelectionStart = actualStart + markdownPrefix.length,
            newSelectionEnd = actualEnd + markdownPrefix.length
        )
    }
    
    /**
     * Removes inline code formatting from the selected text.
     * Removes the ` markers from around the selection.
     */
    override fun removeFormat(text: String, selectionStart: Int, selectionEnd: Int): FormattedResult {
        // Validate bounds to prevent StringIndexOutOfBoundsException
        val safeStart = selectionStart.coerceIn(0, text.length)
        val safeEnd = selectionEnd.coerceIn(0, text.length)
        
        // Ensure start <= end
        val (actualStart, actualEnd) = if (safeStart <= safeEnd) {
            safeStart to safeEnd
        } else {
            safeEnd to safeStart
        }
        
        val beforeStart = maxOf(0, actualStart - markdownPrefix.length)
        val afterEnd = minOf(text.length, actualEnd + markdownSuffix.length)
        
        // Check if markers exist
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
        
        // No formatting to remove
        return FormattedResult(text, actualStart, actualEnd)
    }
    
    /**
     * Checks if inline code formatting is applied to the selected text.
     */
    override fun isFormatApplied(text: String, selectionStart: Int, selectionEnd: Int): Boolean {
        // Validate bounds to prevent StringIndexOutOfBoundsException
        val safeStart = selectionStart.coerceIn(0, text.length)
        val safeEnd = selectionEnd.coerceIn(0, text.length)
        
        // Ensure start <= end
        val (actualStart, actualEnd) = if (safeStart <= safeEnd) {
            safeStart to safeEnd
        } else {
            safeEnd to safeStart
        }
        
        val beforeStart = maxOf(0, actualStart - markdownPrefix.length)
        val afterEnd = minOf(text.length, actualEnd + markdownSuffix.length)
        
        return beforeStart < actualStart && actualEnd < afterEnd &&
               text.substring(beforeStart, actualStart) == markdownPrefix &&
               text.substring(actualEnd, afterEnd) == markdownSuffix
    }
    
    /**
     * Generates a formatted preview with code spans.
     * Converts `text` to visually styled code text with background and monospace font.
     */
    override fun getFormattedPreview(text: String): SpannableString {
        // Pattern to match `text` (but not ```text``` which is code block)
        val pattern = Pattern.compile("(?<!`)`([^`]+)`(?!`)")
        val matcher = pattern.matcher(text)
        
        // Build the display text without markers
        val displayText = StringBuilder(text)
        val spans = mutableListOf<Triple<Int, Int, List<Any>>>()
        var offset = 0
        
        while (matcher.find()) {
            val start = matcher.start() - offset
            val end = matcher.end() - offset
            val content = matcher.group(1) ?: ""
            
            // Remove the markers
            displayText.replace(start, end, content)
            
            // Track span positions (adjusted for removed markers)
            val spanList = listOf(
                BackgroundColorSpan(codeBackgroundColor),
                TypefaceSpan("monospace")
            )
            spans.add(Triple(start, start + content.length, spanList))
            
            // Update offset for removed markers (2 characters: ` at start and end)
            offset += 2
        }
        
        val spannable = SpannableString(displayText.toString())
        for ((spanStart, spanEnd, spanList) in spans) {
            if (spanStart >= 0 && spanEnd <= spannable.length) {
                for (span in spanList) {
                    spannable.setSpan(span, spanStart, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }
        }
        
        return spannable
    }
}
