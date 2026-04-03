package com.cometchat.uikit.core.formatter

import android.text.SpannableString
import android.text.Spanned
import android.text.style.URLSpan
import java.util.regex.Pattern

/**
 * Formatter for link text using markdown syntax: [text](url)
 * 
 * Applies link formatting to selected text by wrapping it with [text](url) syntax.
 * Renders preview with URLSpan.
 */
class LinkTextFormatter : RichTextFormatter {
    
    override val formatType: RichTextFormat = RichTextFormat.LINK
    override val markdownPrefix: String = "["
    override val markdownSuffix: String = "]()"
    
    /**
     * Applies link formatting to the selected text.
     * Wraps the selection with [text]() syntax, placing cursor in URL position.
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
        val formattedText = "[$selectedText]()"
        val newText = text.replaceRange(actualStart, actualEnd, formattedText)
        
        // Position cursor inside the URL parentheses
        val urlPosition = actualStart + selectedText.length + 3 // After ](
        return FormattedResult(
            text = newText,
            newSelectionStart = urlPosition,
            newSelectionEnd = urlPosition
        )
    }
    
    /**
     * Applies link formatting with a specific URL.
     * 
     * @param text The full text content
     * @param selectionStart The start position of the selection
     * @param selectionEnd The end position of the selection
     * @param url The URL to link to
     * @return FormattedResult containing the new text and updated selection positions
     */
    fun applyFormatWithUrl(text: String, selectionStart: Int, selectionEnd: Int, url: String): FormattedResult {
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
        val formattedText = "[$selectedText]($url)"
        val newText = text.replaceRange(actualStart, actualEnd, formattedText)
        
        return FormattedResult(
            text = newText,
            newSelectionStart = actualStart,
            newSelectionEnd = actualStart + formattedText.length
        )
    }
    
    /**
     * Removes link formatting from the selected text.
     * Extracts just the link text, removing the URL.
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
        
        // Find the link pattern around the selection
        val pattern = Pattern.compile("\\[([^\\]]+)\\]\\(([^)]+)\\)")
        val matcher = pattern.matcher(text)
        
        while (matcher.find()) {
            val matchStart = matcher.start()
            val matchEnd = matcher.end()
            
            // Check if selection is within this link
            if (actualStart >= matchStart && actualEnd <= matchEnd) {
                val linkText = matcher.group(1) ?: ""
                val newText = text.replaceRange(matchStart, matchEnd, linkText)
                
                return FormattedResult(
                    text = newText,
                    newSelectionStart = matchStart,
                    newSelectionEnd = matchStart + linkText.length
                )
            }
        }
        
        // No formatting to remove
        return FormattedResult(text, actualStart, actualEnd)
    }
    
    /**
     * Checks if link formatting is applied to the selected text.
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
        
        val pattern = Pattern.compile("\\[([^\\]]+)\\]\\(([^)]+)\\)")
        val matcher = pattern.matcher(text)
        
        while (matcher.find()) {
            val matchStart = matcher.start()
            val matchEnd = matcher.end()
            
            // Check if selection is within this link
            if (actualStart >= matchStart && actualEnd <= matchEnd) {
                return true
            }
        }
        
        return false
    }
    
    /**
     * Generates a formatted preview with URL spans.
     * Converts [text](url) to clickable link text.
     */
    override fun getFormattedPreview(text: String): SpannableString {
        // Pattern to match [text](url)
        val pattern = Pattern.compile("\\[([^\\]]+)\\]\\(([^)]+)\\)")
        val matcher = pattern.matcher(text)
        
        // Build the display text without markers
        val displayText = StringBuilder(text)
        val spans = mutableListOf<Triple<Int, Int, URLSpan>>()
        var offset = 0
        
        while (matcher.find()) {
            val start = matcher.start() - offset
            val end = matcher.end() - offset
            val linkText = matcher.group(1) ?: ""
            val url = matcher.group(2) ?: ""
            
            // Remove the markers, keep only link text
            displayText.replace(start, end, linkText)
            
            // Track span position (adjusted for removed markers)
            spans.add(Triple(start, start + linkText.length, URLSpan(url)))
            
            // Update offset for removed markers
            val fullMatch = matcher.group(0) ?: ""
            offset += fullMatch.length - linkText.length
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
     * Extracts the URL from a link at the given position.
     * 
     * @param text The full text content
     * @param position The cursor position
     * @return The URL if found, null otherwise
     */
    fun extractUrl(text: String, position: Int): String? {
        val pattern = Pattern.compile("\\[([^\\]]+)\\]\\(([^)]+)\\)")
        val matcher = pattern.matcher(text)
        
        while (matcher.find()) {
            if (position >= matcher.start() && position <= matcher.end()) {
                return matcher.group(2)
            }
        }
        
        return null
    }
    
    /**
     * Extracts the link text from a link at the given position.
     * 
     * @param text The full text content
     * @param position The cursor position
     * @return The link text if found, null otherwise
     */
    fun extractLinkText(text: String, position: Int): String? {
        val pattern = Pattern.compile("\\[([^\\]]+)\\]\\(([^)]+)\\)")
        val matcher = pattern.matcher(text)
        
        while (matcher.find()) {
            if (position >= matcher.start() && position <= matcher.end()) {
                return matcher.group(1)
            }
        }
        
        return null
    }
}
