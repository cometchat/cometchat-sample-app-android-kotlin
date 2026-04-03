package com.cometchat.uikit.core.formatter

import android.text.SpannableString
import android.text.Spanned
import android.text.style.LeadingMarginSpan
import java.util.regex.Pattern

/**
 * Formatter for ordered list text using markdown syntax: 1. item
 * 
 * Applies ordered list formatting to selected text by prefixing each line with "N. ".
 * Supports multi-line selections with auto-numbering.
 * Renders preview with LeadingMarginSpan.
 */
class OrderedListTextFormatter(
    private val leadingMargin: Int = 32
) : RichTextFormatter {
    
    override val formatType: RichTextFormat = RichTextFormat.ORDERED_LIST
    override val markdownPrefix: String = "1. "
    override val markdownSuffix: String = ""
    
    /**
     * Applies ordered list formatting to the selected text.
     * Prefixes each line with "N. " where N is the line number.
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
        
        var lineNumber = 1
        val formattedLines = lines.map { line ->
            if (line.isNotBlank() && !isNumberedLine(line)) {
                val formatted = "$lineNumber. $line"
                lineNumber++
                formatted
            } else if (isNumberedLine(line)) {
                // Re-number existing numbered lines
                val content = line.replace(Regex("^\\d+\\.\\s*"), "")
                val formatted = "$lineNumber. $content"
                lineNumber++
                formatted
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
     * Removes ordered list formatting from the selected text.
     * Removes "N. " prefix from each line.
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
            if (isNumberedLine(line)) {
                line.replace(Regex("^\\d+\\.\\s*"), "")
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
     * Checks if ordered list formatting is applied to the selected text.
     * Returns true if all non-empty lines start with "N. ".
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
            line.isBlank() || isNumberedLine(line)
        }
    }
    
    /**
     * Checks if a line starts with a number followed by ". ".
     */
    private fun isNumberedLine(line: String): Boolean {
        return line.trimStart().matches(Regex("^\\d+\\.\\s.*"))
    }
    
    /**
     * Generates a formatted preview with leading margin spans.
     * Converts "N. item" lines to visually numbered items.
     */
    override fun getFormattedPreview(text: String): SpannableString {
        // Pattern to match lines starting with "N. "
        val pattern = Pattern.compile("^(\\d+)\\.\\s(.+)$", Pattern.MULTILINE)
        val matcher = pattern.matcher(text)
        
        // Build the display text without markers
        val displayText = StringBuilder(text)
        val spans = mutableListOf<Triple<Int, Int, LeadingMarginSpan.Standard>>()
        var offset = 0
        
        while (matcher.find()) {
            val start = matcher.start() - offset
            val end = matcher.end() - offset
            val number = matcher.group(1) ?: "1"
            val content = matcher.group(2) ?: ""
            
            // Replace with "N. content" format for display
            val displayContent = "$number. $content"
            displayText.replace(start, end, displayContent)
            
            // Track span position
            spans.add(Triple(start, start + displayContent.length, LeadingMarginSpan.Standard(leadingMargin)))
            
            // Update offset
            val originalLength = matcher.group(0)?.length ?: 0
            offset += originalLength - displayContent.length
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
     * Adds a new numbered item after the current line.
     * Automatically increments the number based on the previous line.
     * 
     * @param text The full text content
     * @param cursorPosition The current cursor position
     * @return FormattedResult with new numbered item added
     */
    fun addNewNumberedItem(text: String, cursorPosition: Int): FormattedResult {
        // Find the current line to determine the next number
        val beforeCursor = text.substring(0, cursorPosition)
        val lastLineStart = beforeCursor.lastIndexOf('\n') + 1
        val currentLine = beforeCursor.substring(lastLineStart)
        
        val nextNumber = if (isNumberedLine(currentLine)) {
            val match = Regex("^(\\d+)\\.").find(currentLine.trimStart())
            (match?.groupValues?.get(1)?.toIntOrNull() ?: 0) + 1
        } else {
            1
        }
        
        val newText = StringBuilder(text)
        newText.insert(cursorPosition, "\n$nextNumber. ")
        
        val newCursorPosition = cursorPosition + 1 + "$nextNumber. ".length
        return FormattedResult(
            text = newText.toString(),
            newSelectionStart = newCursorPosition,
            newSelectionEnd = newCursorPosition
        )
    }
}
