package com.cometchat.uikit.core.state

import com.cometchat.chat.exceptions.CometChatException

/**
 * Sealed class representing UI states for the reaction list screen.
 * Used by the CometChatReactionListViewModel to communicate current state to the UI.
 */
sealed class ReactionListUIState {
    /**
     * Loading state - displayed while fetching reactions.
     */
    object Loading : ReactionListUIState()

    /**
     * Content state - displayed when reactions are available.
     * The actual reaction data is stored in the ViewModel's reactedUsers StateFlow,
     * this state just indicates that content is ready to display.
     */
    object Content : ReactionListUIState()

    /**
     * Empty state - displayed when no reactions exist.
     */
    object Empty : ReactionListUIState()

    /**
     * Error state - displayed when fetching fails.
     * @param exception The exception that caused the error
     */
    data class Error(val exception: CometChatException) : ReactionListUIState()
}
