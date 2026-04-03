package com.cometchat.uikit.core.formatter

import android.text.SpannableString

/**
 * Interface defining the contract for rich text formatters.
 * Each formatter handles a specific markdown format type and provides methods for:
 * - Applying formatting to selected text
 * - Removing formatting from selected text
 * - Checking if formatting is applied to selected text
 * - Generating a formatted preview with Android spans
 * 
 * Implementations should extend CometChatTextFormatter for integration with
 * the existing text formatting system.
 * 
 * @see RichTextFormat for the available format types
 * @see FormattedResult for the result of formatting operations
 */
interface RichTextFormatter {
    
    /**
     * The type of rich text format this formatter handles.
     */
    val formatType: RichTextFormat
    
    /**
     * The markdown prefix used to start the formatting.
     * For example: "**" for bold, "_" for italic, "`" for inline code.
     */
    val markdownPrefix: String
    
    /**
     * The markdown suffix used to end the formatting.
     * For example: "**" for bold, "_" for italic, "`" for inline code.
     */
    val markdownSuffix: String
    
    /**
     * Applies the formatting to the selected text.
     * Wraps the selected text with the markdown prefix and suffix.
     * 
     * @param text The full text content
     * @param selectionStart The start position of the selection
     * @param selectionEnd The end position of the selection
     * @return FormattedResult containing the new text and updated selection positions
     */
    fun applyFormat(text: String, selectionStart: Int, selectionEnd: Int): FormattedResult
    
    /**
     * Removes the formatting from the selected text.
     * Removes the markdown prefix and suffix from around the selected text.
     * 
     * @param text The full text content
     * @param selectionStart The start position of the selection
     * @param selectionEnd The end position of the selection
     * @return FormattedResult containing the new text and updated selection positions
     */
    fun removeFormat(text: String, selectionStart: Int, selectionEnd: Int): FormattedResult
    
    /**
     * Checks if the formatting is applied to the selected text.
     * Verifies if the markdown prefix and suffix surround the selection.
     * 
     * @param text The full text content
     * @param selectionStart The start position of the selection
     * @param selectionEnd The end position of the selection
     * @return True if the formatting is applied, false otherwise
     */
    fun isFormatApplied(text: String, selectionStart: Int, selectionEnd: Int): Boolean
    
    /**
     * Generates a formatted preview of the text with Android spans.
     * Converts markdown syntax to visual formatting using SpannableString.
     * 
     * @param text The text containing markdown formatting
     * @return SpannableString with visual formatting applied
     */
    fun getFormattedPreview(text: String): SpannableString
}
