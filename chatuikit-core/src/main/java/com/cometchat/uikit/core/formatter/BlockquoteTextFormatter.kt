package com.cometchat.uikit.core.formatter

import android.graphics.Color
import android.text.SpannableString
import android.text.Spanned
import android.text.style.QuoteSpan
import java.util.regex.Pattern

/**
 * Formatter for blockquote text using markdown syntax: > text
 * 
 * Applies blockquote formatting to selected text by prefixing each line with "> ".
 * Supports multi-line selections.
 * Renders preview with QuoteSpan.
 */
class BlockquoteTextFormatter(
    private val quoteColor: Int = Color.parseColor("#CCCCCC"),
    private val quoteStripeWidth: Int = 4,
    private val quoteGapWidth: Int = 16
) : RichTextFormatter {
    
    override val formatType: RichTextFormat = RichTextFormat.BLOCKQUOTE
    override val markdownPrefix: String = "> "
    override val markdownSuffix: String = ""
    
    /**
     * Applies blockquote formatting to the selected text.
     * Prefixes each line with "> ".
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
        
        // If no selection, insert at cursor position
        if (actualStart == actualEnd) {
            val newText = StringBuilder(text)
            newText.insert(actualStart, "$markdownPrefix")
            return FormattedResult(
                text = newText.toString(),
                newSelectionStart = actualStart + markdownPrefix.length,
                newSelectionEnd = actualStart + markdownPrefix.length
            )
        }
        
        val selectedText = text.substring(actualStart, actualEnd)
        val lines = selectedText.split("\n")
        
        val formattedLines = lines.map { line ->
            if (line.isNotBlank() && !line.trimStart().startsWith("> ")) {
                "$markdownPrefix$line"
            } else {
                line
            }
        }
        
        val formattedText = formattedLines.joinToString("\n")
        val newText = text.replaceRange(actualStart, actualEnd, formattedText)
        
        return FormattedResult(
            text = newText,
            newSelectionStart = actualStart,
            newSelectionEnd = actualStart + formattedText.length
        )
    }
    
    /**
     * Removes blockquote formatting from the selected text.
     * Removes "> " prefix from each line.
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
        
        // If no selection, return unchanged
        if (actualStart == actualEnd) {
            return FormattedResult(
                text = text,
                newSelectionStart = actualStart,
                newSelectionEnd = actualEnd
            )
        }
        
        val selectedText = text.substring(actualStart, actualEnd)
        val lines = selectedText.split("\n")
        
        val unformattedLines = lines.map { line ->
            if (line.trimStart().startsWith("> ")) {
                line.replaceFirst("> ", "")
            } else {
                line
            }
        }
        
        val unformattedText = unformattedLines.joinToString("\n")
        val newText = text.replaceRange(actualStart, actualEnd, unformattedText)
        
        return FormattedResult(
            text = newText,
            newSelectionStart = actualStart,
            newSelectionEnd = actualStart + unformattedText.length
        )
    }
    
    /**
     * Checks if blockquote formatting is applied to the selected text.
     * Returns true if all non-empty lines start with "> ".
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
        
        // If no selection, return false
        if (actualStart == actualEnd) {
            return false
        }
        
        val selectedText = text.substring(actualStart, actualEnd)
        val lines = selectedText.split("\n")
        
        return lines.all { line ->
            line.isBlank() || line.trimStart().startsWith("> ")
        }
    }
    
    /**
     * Generates a formatted preview with quote spans.
     * Converts "> text" lines to visually quoted items with a vertical bar.
     */
    override fun getFormattedPreview(text: String): SpannableString {
        // Pattern to match lines starting with "> "
        val pattern = Pattern.compile("^> (.+)$", Pattern.MULTILINE)
        val matcher = pattern.matcher(text)
        
        // Build the display text without markers
        val displayText = StringBuilder(text)
        val spans = mutableListOf<Triple<Int, Int, QuoteSpan>>()
        var offset = 0
        
        while (matcher.find()) {
            val start = matcher.start() - offset
            val end = matcher.end() - offset
            val content = matcher.group(1) ?: ""
            
            // Remove the "> " prefix
            displayText.replace(start, end, content)
            
            // Track span position (adjusted for removed markers)
            val quoteSpan = QuoteSpan(quoteColor)
            spans.add(Triple(start, start + content.length, quoteSpan))
            
            // Update offset for removed markers (2 characters: "> ")
            offset += 2
        }
        
        val spannable = SpannableString(displayText.toString())
        for ((spanStart, spanEnd, span) in spans) {
            if (spanStart >= 0 && spanEnd <= spannable.length) {
                spannable.setSpan(span, spanStart, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
        
        return spannable
    }
    
    /**
     * Adds a new quote line after the current line.
     * 
     * @param text The full text content
     * @param cursorPosition The current cursor position
     * @return FormattedResult with new quote line added
     */
    fun addNewQuoteLine(text: String, cursorPosition: Int): FormattedResult {
        val newText = StringBuilder(text)
        newText.insert(cursorPosition, "\n$markdownPrefix")
        
        val newCursorPosition = cursorPosition + 1 + markdownPrefix.length
        return FormattedResult(
            text = newText.toString(),
            newSelectionStart = newCursorPosition,
            newSelectionEnd = newCursorPosition
        )
    }
}
