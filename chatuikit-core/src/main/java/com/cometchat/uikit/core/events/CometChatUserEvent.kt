package com.cometchat.uikit.core.events

import com.cometchat.chat.models.User

/**
 * Sealed class hierarchy representing all user-related events.
 * Provides type-safe event handling for user state changes.
 */
sealed class CometChatUserEvent {
    /**
     * Event emitted when a user is blocked.
     * @param user The blocked user
     */
    data class UserBlocked(
        val user: User
    ) : CometChatUserEvent()

    /**
     * Event emitted when a user is unblocked.
     * @param user The unblocked user
     */
    data class UserUnblocked(
        val user: User
    ) : CometChatUserEvent()
}
