package com.cometchat.uikit.core.formatter

import android.text.SpannableString
import android.text.Spanned
import android.text.style.BulletSpan
import java.util.regex.Pattern

/**
 * Formatter for bullet list text using markdown syntax: - item
 * 
 * Applies bullet list formatting to selected text by prefixing each line with "- ".
 * Supports multi-line selections.
 * Renders preview with BulletSpan.
 */
class BulletListTextFormatter(
    private val bulletGapWidth: Int = 16,
    private val bulletColor: Int? = null
) : RichTextFormatter {
    
    override val formatType: RichTextFormat = RichTextFormat.BULLET_LIST
    override val markdownPrefix: String = "- "
    override val markdownSuffix: String = ""
    
    /**
     * Applies bullet list formatting to the selected text.
     * Prefixes each line with "- ".
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
            if (line.isNotBlank() && !line.trimStart().startsWith("- ")) {
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
     * Removes bullet list formatting from the selected text.
     * Removes "- " prefix from each line.
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
            if (line.trimStart().startsWith("- ")) {
                line.replaceFirst("- ", "")
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
     * Checks if bullet list formatting is applied to the selected text.
     * Returns true if all non-empty lines start with "- ".
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
            line.isBlank() || line.trimStart().startsWith("- ")
        }
    }
    
    /**
     * Generates a formatted preview with bullet spans.
     * Converts "- item" lines to visually bulleted items.
     */
    override fun getFormattedPreview(text: String): SpannableString {
        // Pattern to match lines starting with "- "
        val pattern = Pattern.compile("^- (.+)$", Pattern.MULTILINE)
        val matcher = pattern.matcher(text)
        
        // Build the display text without markers
        val displayText = StringBuilder(text)
        val spans = mutableListOf<Triple<Int, Int, BulletSpan>>()
        var offset = 0
        
        while (matcher.find()) {
            val start = matcher.start() - offset
            val end = matcher.end() - offset
            val content = matcher.group(1) ?: ""
            
            // Remove the "- " prefix
            displayText.replace(start, end, content)
            
            // Track span position (adjusted for removed markers)
            val bulletSpan = if (bulletColor != null) {
                BulletSpan(bulletGapWidth, bulletColor)
            } else {
                BulletSpan(bulletGapWidth)
            }
            spans.add(Triple(start, start + content.length, bulletSpan))
            
            // Update offset for removed markers (2 characters: "- ")
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
     * Adds a new bullet item after the current line.
     * 
     * @param text The full text content
     * @param cursorPosition The current cursor position
     * @return FormattedResult with new bullet item added
     */
    fun addNewBulletItem(text: String, cursorPosition: Int): FormattedResult {
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
