package com.cometchat.uikit.core.state

import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.Conversation

/**
 * Sealed class representing UI states for the conversations screen.
 * Used by the ViewModel to communicate current state to the UI.
 */
sealed class UIState {
    /**
     * Loading state - displayed while fetching conversations.
     */
    object Loading : UIState()
    
    /**
     * Empty state - displayed when no conversations exist.
     */
    object Empty : UIState()
    
    /**
     * Error state - displayed when fetching fails.
     * @param exception The exception that caused the error
     */
    data class Error(val exception: CometChatException) : UIState()
    
    /**
     * Content state - displayed when conversations are available.
     * @param conversations The list of conversations to display
     */
    data class Content(val conversations: List<Conversation>) : UIState()
}
