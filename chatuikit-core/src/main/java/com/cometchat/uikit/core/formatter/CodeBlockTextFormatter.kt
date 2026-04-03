package com.cometchat.uikit.core.formatter

import android.graphics.Color
import android.text.SpannableString
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.text.style.TypefaceSpan
import java.util.regex.Pattern

/**
 * Formatter for code block text using markdown syntax: ```text```
 * 
 * Applies code block formatting to selected text by wrapping it with triple backticks.
 * Supports multi-line code blocks with proper newline handling.
 * Renders preview with BackgroundColorSpan and TypefaceSpan("monospace").
 */
class CodeBlockTextFormatter(
    private val codeBackgroundColor: Int = Color.parseColor("#E8E8E8")
) : RichTextFormatter {
    
    override val formatType: RichTextFormat = RichTextFormat.CODE_BLOCK
    override val markdownPrefix: String = "```\n"
    override val markdownSuffix: String = "\n```"
    
    /**
     * Applies code block formatting to the selected text.
     * Wraps the selection with ``` markers on separate lines.
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
        
        // Ensure code block is on its own lines
        val prefix = if (actualStart > 0 && text[actualStart - 1] != '\n') "\n$markdownPrefix" else markdownPrefix
        val suffix = if (actualEnd < text.length && text[actualEnd] != '\n') "$markdownSuffix\n" else markdownSuffix
        
        val formattedText = "$prefix$selectedText$suffix"
        val newText = text.replaceRange(actualStart, actualEnd, formattedText)
        
        val prefixLength = prefix.length
        return FormattedResult(
            text = newText,
            newSelectionStart = actualStart + prefixLength,
            newSelectionEnd = actualEnd + prefixLength
        )
    }
    
    /**
     * Removes code block formatting from the selected text.
     * Removes the ``` markers from around the selection.
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
        
        // Find the code block markers
        val prefixPattern = "```\\n?"
        val suffixPattern = "\\n?```"
        
        // Look for prefix before selection
        val beforeText = text.substring(0, actualStart)
        val prefixMatch = Regex("$prefixPattern$").find(beforeText)
        
        // Look for suffix after selection
        val afterText = text.substring(actualEnd)
        val suffixMatch = Regex("^$suffixPattern").find(afterText)
        
        if (prefixMatch != null && suffixMatch != null) {
            val prefixStart = prefixMatch.range.first
            val suffixEnd = actualEnd + suffixMatch.range.last + 1
            
            val selectedContent = text.substring(actualStart, actualEnd)
            val newText = text.replaceRange(prefixStart, suffixEnd, selectedContent)
            
            return FormattedResult(
                text = newText,
                newSelectionStart = prefixStart,
                newSelectionEnd = prefixStart + selectedContent.length
            )
        }
        
        // No formatting to remove
        return FormattedResult(text, actualStart, actualEnd)
    }
    
    /**
     * Checks if code block formatting is applied to the selected text.
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
        
        // Look for ``` before selection
        val beforeText = text.substring(0, actualStart)
        val hasPrefix = beforeText.endsWith("```\n") || beforeText.endsWith("```")
        
        // Look for ``` after selection
        val afterText = text.substring(actualEnd)
        val hasSuffix = afterText.startsWith("\n```") || afterText.startsWith("```")
        
        return hasPrefix && hasSuffix
    }
    
    /**
     * Generates a formatted preview with code block spans.
     * Converts ```text``` to visually styled code block with background and monospace font.
     */
    override fun getFormattedPreview(text: String): SpannableString {
        // Pattern to match ```text``` (with optional newlines)
        val pattern = Pattern.compile("```\\n?([\\s\\S]*?)\\n?```", Pattern.MULTILINE)
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
            
            // Update offset for removed markers
            val markerLength = matcher.group(0)?.length ?: 0
            offset += markerLength - content.length
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
