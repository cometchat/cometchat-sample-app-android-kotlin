package com.cometchat.uikit.core.state

import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.Group

/**
 * Sealed class representing UI states for the groups screen.
 * Used by the ViewModel to communicate current state to the UI.
 */
sealed class GroupsUIState {
    /**
     * Loading state - displayed while fetching groups.
     */
    object Loading : GroupsUIState()
    
    /**
     * Empty state - displayed when no groups exist.
     */
    object Empty : GroupsUIState()
    
    /**
     * Error state - displayed when fetching fails.
     * @param exception The exception that caused the error
     */
    data class Error(val exception: CometChatException) : GroupsUIState()
    
    /**
     * Content state - displayed when groups are available.
     * @param groups The list of groups to display
     */
    data class Content(val groups: List<Group>) : GroupsUIState()
}
