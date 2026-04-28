package com.cometchat.uikit.core.state

import com.cometchat.chat.exceptions.CometChatException

/**
 * Sealed interface representing UI states for the search screen.
 * Used by the ViewModel to communicate current state to the UI.
 */
sealed interface SearchUIState {
    /**
     * Initial state - displayed before any search is performed.
     */
    data object Initial : SearchUIState

    /**
     * Loading state - displayed while search is in progress.
     */
    data object Loading : SearchUIState

    /**
     * Content state - displayed when search results are available.
     */
    data object Content : SearchUIState

    /**
     * Empty state - displayed when no search results are found.
     */
    data object Empty : SearchUIState

    /**
     * Error state - displayed when search fails.
     * @param exception The exception that caused the error
     */
    data class Error(val exception: CometChatException) : SearchUIState
}
