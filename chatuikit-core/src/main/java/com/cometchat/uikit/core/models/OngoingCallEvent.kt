package com.cometchat.uikit.core.models

import com.cometchat.chat.exceptions.CometChatException

/**
 * Sealed class representing one-time events for the ongoing call screen.
 * Used by the ViewModel to emit events that should not replay on configuration changes.
 *
 * Validates: Requirement 1.5
 */
sealed class OngoingCallEvent {
    /**
     * Event emitted when the call has ended.
     */
    object CallEnded : OngoingCallEvent()

    /**
     * Event emitted when the call session times out.
     */
    object SessionTimeout : OngoingCallEvent()

    /**
     * Event emitted when a user joins the call.
     *
     * @param userId The unique identifier of the user who joined
     * @param isCurrentUser True if the joined user is the current logged-in user
     */
    data class UserJoined(val userId: String, val isCurrentUser: Boolean) : OngoingCallEvent()

    /**
     * Event emitted when a user leaves the call.
     *
     * @param userId The unique identifier of the user who left
     */
    data class UserLeft(val userId: String) : OngoingCallEvent()

    /**
     * Event emitted when an error occurs during the call.
     *
     * @param exception The CometChatException that caused the error
     */
    data class Error(val exception: CometChatException) : OngoingCallEvent()
}
