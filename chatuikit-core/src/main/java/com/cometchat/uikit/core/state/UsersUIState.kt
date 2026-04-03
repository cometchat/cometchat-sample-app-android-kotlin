package com.cometchat.uikit.core.state

import com.cometchat.chat.exceptions.CometChatException

/**
 * Sealed class representing UI states for the users screen.
 * Used by the CometChatUsersViewModel to communicate current state to the UI.
 */
sealed class UsersUIState {
    /**
     * Loading state - displayed while fetching users.
     */
    object Loading : UsersUIState()
    
    /**
     * Empty state - displayed when no users exist.
     */
    object Empty : UsersUIState()
    
    /**
     * Error state - displayed when fetching fails.
     * @param exception The exception that caused the error
     */
    data class Error(val exception: CometChatException) : UsersUIState()
    
    /**
     * Content state - displayed when users are available.
     * The actual user data is stored in the ViewModel's users StateFlow,
     * this state just indicates that content is ready to display.
     */
    object Content : UsersUIState()
}
