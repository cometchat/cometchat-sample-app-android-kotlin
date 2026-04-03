package com.cometchat.uikit.core.formatter

import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import java.util.regex.Pattern

/**
 * Formatter for bold text using markdown syntax: **text**
 * 
 * Applies bold formatting to selected text by wrapping it with double asterisks.
 * Renders preview with StyleSpan(Typeface.BOLD).
 */
class BoldTextFormatter : RichTextFormatter {
    
    override val formatType: RichTextFormat = RichTextFormat.BOLD
    override val markdownPrefix: String = "**"
    override val markdownSuffix: String = "**"
    
    /**
     * Applies bold formatting to the selected text.
     * Wraps the selection with ** markers.
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
     * Removes bold formatting from the selected text.
     * Removes the ** markers from around the selection.
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
     * Checks if bold formatting is applied to the selected text.
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
     * Generates a formatted preview with bold spans.
     * Converts **text** to visually bold text.
     */
    override fun getFormattedPreview(text: String): SpannableString {
        // Pattern to match **text**
        val pattern = Pattern.compile("\\*\\*(.+?)\\*\\*")
        val matcher = pattern.matcher(text)
        
        // Build the display text without markers
        val displayText = StringBuilder(text)
        val spans = mutableListOf<Triple<Int, Int, Any>>()
        var offset = 0
        
        while (matcher.find()) {
            val start = matcher.start() - offset
            val end = matcher.end() - offset
            val content = matcher.group(1) ?: ""
            
            // Remove the markers
            displayText.replace(start, end, content)
            
            // Track span position (adjusted for removed markers)
            spans.add(Triple(start, start + content.length, StyleSpan(Typeface.BOLD)))
            
            // Update offset for removed markers (4 characters: ** at start and end)
            offset += 4
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
