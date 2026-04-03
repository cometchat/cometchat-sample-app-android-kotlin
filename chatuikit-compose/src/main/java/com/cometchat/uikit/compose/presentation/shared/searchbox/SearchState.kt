package com.cometchat.uikit.compose.presentation.shared.searchbox

/**
 * Enum representing the search state events.
 * Mirrors the View-based SearchState annotation.
 */
enum class SearchState {
    /**
     * Search filter applied - user has entered search text.
     */
    FILTER,
    
    /**
     * Search cleared - user has cleared the search text.
     */
    CLEAR,
    
    /**
     * Text changed - search text has been modified.
     */
    TEXT_CHANGE
}
