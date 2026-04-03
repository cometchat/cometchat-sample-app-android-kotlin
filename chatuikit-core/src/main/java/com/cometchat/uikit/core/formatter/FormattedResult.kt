package com.cometchat.uikit.core.formatter

/**
 * Data class representing the result of a text formatting operation.
 * Contains the formatted text and the new cursor selection positions.
 * 
 * @param text The formatted text after applying or removing formatting
 * @param newSelectionStart The new cursor selection start position
 * @param newSelectionEnd The new cursor selection end position
 */
data class FormattedResult(
    val text: String,
    val newSelectionStart: Int,
    val newSelectionEnd: Int
)
