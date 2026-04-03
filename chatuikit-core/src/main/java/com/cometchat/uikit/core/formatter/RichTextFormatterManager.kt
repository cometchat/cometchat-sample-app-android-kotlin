package com.cometchat.uikit.core.formatter

import android.text.SpannableString
import android.text.SpannableStringBuilder

/**
 * Manager class that coordinates all rich text formatters.
 * Provides a unified interface for applying, removing, and toggling formatting.
 * 
 * The manager initializes formatters based on the provided configuration,
 * only creating instances for enabled format types.
 * 
 * @param configuration The configuration specifying which formatters to enable
 */
class RichTextFormatterManager(
    private val configuration: RichTextConfiguration
) {
    private val formatters = mutableMapOf<RichTextFormat, RichTextFormatter>()
    
    init {
        initializeFormatters()
    }
    
    /**
     * Initializes formatters based on configuration.
     */
    private fun initializeFormatters() {
        if (configuration.enableBold) {
            formatters[RichTextFormat.BOLD] = BoldTextFormatter()
        }
        if (configuration.enableItalic) {
            formatters[RichTextFormat.ITALIC] = ItalicTextFormatter()
        }
        if (configuration.enableUnderline) {
            formatters[RichTextFormat.UNDERLINE] = UnderlineTextFormatter()
        }
        if (configuration.enableStrikethrough) {
            formatters[RichTextFormat.STRIKETHROUGH] = StrikethroughTextFormatter()
        }
        if (configuration.enableInlineCode) {
            formatters[RichTextFormat.INLINE_CODE] = InlineCodeTextFormatter()
        }
        if (configuration.enableCodeBlock) {
            formatters[RichTextFormat.CODE_BLOCK] = CodeBlockTextFormatter()
        }
        if (configuration.enableLink) {
            formatters[RichTextFormat.LINK] = LinkTextFormatter()
        }
        if (configuration.enableBulletList) {
            formatters[RichTextFormat.BULLET_LIST] = BulletListTextFormatter()
        }
        if (configuration.enableOrderedList) {
            formatters[RichTextFormat.ORDERED_LIST] = OrderedListTextFormatter()
        }
        if (configuration.enableBlockquote) {
            formatters[RichTextFormat.BLOCKQUOTE] = BlockquoteTextFormatter()
        }
    }
    
    /**
     * Returns a list of enabled format types.
     */
    fun getEnabledFormats(): List<RichTextFormat> {
        return formatters.keys.toList()
    }
    
    /**
     * Checks if a specific format is enabled.
     * 
     * @param format The format type to check
     * @return True if the format is enabled
     */
    fun isFormatEnabled(format: RichTextFormat): Boolean {
        return formatters.containsKey(format)
    }
    
    /**
     * Applies formatting to the selected text.
     * 
     * @param format The format type to apply
     * @param text The full text content
     * @param selectionStart The start position of the selection
     * @param selectionEnd The end position of the selection
     * @return FormattedResult or null if format is not enabled
     */
    fun applyFormat(
        format: RichTextFormat,
        text: String,
        selectionStart: Int,
        selectionEnd: Int
    ): FormattedResult? {
        return formatters[format]?.applyFormat(text, selectionStart, selectionEnd)
    }
    
    /**
     * Removes formatting from the selected text.
     * 
     * @param format The format type to remove
     * @param text The full text content
     * @param selectionStart The start position of the selection
     * @param selectionEnd The end position of the selection
     * @return FormattedResult or null if format is not enabled
     */
    fun removeFormat(
        format: RichTextFormat,
        text: String,
        selectionStart: Int,
        selectionEnd: Int
    ): FormattedResult? {
        return formatters[format]?.removeFormat(text, selectionStart, selectionEnd)
    }
    
    /**
     * Toggles formatting on the selected text.
     * If formatting is applied, removes it. If not applied, adds it.
     * 
     * @param format The format type to toggle
     * @param text The full text content
     * @param selectionStart The start position of the selection
     * @param selectionEnd The end position of the selection
     * @return FormattedResult or null if format is not enabled
     */
    fun toggleFormat(
        format: RichTextFormat,
        text: String,
        selectionStart: Int,
        selectionEnd: Int
    ): FormattedResult? {
        val formatter = formatters[format] ?: return null
        
        return if (formatter.isFormatApplied(text, selectionStart, selectionEnd)) {
            formatter.removeFormat(text, selectionStart, selectionEnd)
        } else {
            formatter.applyFormat(text, selectionStart, selectionEnd)
        }
    }
    
    /**
     * Checks if a specific format is applied to the selected text.
     * 
     * @param format The format type to check
     * @param text The full text content
     * @param selectionStart The start position of the selection
     * @param selectionEnd The end position of the selection
     * @return True if the format is applied, false otherwise
     */
    fun isFormatApplied(
        format: RichTextFormat,
        text: String,
        selectionStart: Int,
        selectionEnd: Int
    ): Boolean {
        return formatters[format]?.isFormatApplied(text, selectionStart, selectionEnd) ?: false
    }
    
    /**
     * Returns a list of formats currently applied to the selected text.
     * 
     * @param text The full text content
     * @param selectionStart The start position of the selection
     * @param selectionEnd The end position of the selection
     * @return List of applied format types
     */
    fun getActiveFormats(
        text: String,
        selectionStart: Int,
        selectionEnd: Int
    ): List<RichTextFormat> {
        return formatters.filter { (_, formatter) ->
            formatter.isFormatApplied(text, selectionStart, selectionEnd)
        }.keys.toList()
    }
    
    /**
     * Generates a formatted preview of the text with all enabled formatters applied.
     * Combines the preview spans from all formatters.
     * 
     * @param text The text containing markdown formatting
     * @return SpannableString with visual formatting applied
     */
    fun getFormattedPreview(text: String): SpannableString {
        if (formatters.isEmpty()) {
            return SpannableString(text)
        }
        
        // Apply each formatter's preview in sequence
        // Note: This is a simplified approach. For complex overlapping formats,
        // a more sophisticated merging strategy may be needed.
        var result = SpannableString(text)
        
        for ((_, formatter) in formatters) {
            result = formatter.getFormattedPreview(result.toString())
        }
        
        return result
    }
    
    /**
     * Gets the formatter for a specific format type.
     * 
     * @param format The format type
     * @return The formatter or null if not enabled
     */
    fun getFormatter(format: RichTextFormat): RichTextFormatter? {
        return formatters[format]
    }
    
    /**
     * Applies link formatting with a specific URL.
     * Convenience method for the LinkTextFormatter.
     * 
     * @param text The full text content
     * @param selectionStart The start position of the selection
     * @param selectionEnd The end position of the selection
     * @param url The URL to link to
     * @return FormattedResult or null if link format is not enabled
     */
    fun applyLinkFormat(
        text: String,
        selectionStart: Int,
        selectionEnd: Int,
        url: String
    ): FormattedResult? {
        val linkFormatter = formatters[RichTextFormat.LINK] as? LinkTextFormatter
        return linkFormatter?.applyFormatWithUrl(text, selectionStart, selectionEnd, url)
    }
}
