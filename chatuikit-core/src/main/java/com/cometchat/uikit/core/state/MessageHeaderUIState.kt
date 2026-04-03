package com.cometchat.uikit.core.state

import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.User

/**
 * Sealed class representing UI states for the message header.
 * Used by the CometChatMessageHeaderViewModel to communicate current state to the UI.
 * 
 * The message header can display either user or group information, with appropriate
 * states for loading, content display, and error handling.
 * 
 * @see com.cometchat.uikit.core.viewmodel.CometChatMessageHeaderViewModel
 */
sealed class MessageHeaderUIState {
    
    /**
     * Loading state - displayed while fetching user/group details.
     * This is the initial state before user or group data is set.
     */
    object Loading : MessageHeaderUIState()
    
    /**
     * User content state - displayed when showing a user conversation.
     * Contains the user object with all relevant information including
     * name, avatar, online/offline status, and last active timestamp.
     * 
     * @param user The CometChat User object to display in the header
     */
    data class UserContent(val user: User) : MessageHeaderUIState()
    
    /**
     * Group content state - displayed when showing a group conversation.
     * Contains the group object with all relevant information including
     * name, icon, member count, and group type (public/private/password).
     * 
     * @param group The CometChat Group object to display in the header
     */
    data class GroupContent(val group: Group) : MessageHeaderUIState()
    
    /**
     * Error state - displayed when fetching user/group details fails.
     * The UI should handle this state by showing an error message or
     * maintaining the last known valid state.
     * 
     * @param exception The CometChatException that caused the error
     */
    data class Error(val exception: CometChatException) : MessageHeaderUIState()
}
