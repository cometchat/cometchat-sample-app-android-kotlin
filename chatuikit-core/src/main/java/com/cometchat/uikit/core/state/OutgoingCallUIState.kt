package com.cometchat.uikit.core.state

import com.cometchat.chat.core.Call
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.User

/**
 * Sealed class representing UI states for the outgoing call screen.
 * Used by the ViewModel to communicate current state to the UI.
 *
 * Validates: Requirements 10.1-10.7
 */
sealed class OutgoingCallUIState {
    /**
     * Idle state - initial state before any call is initiated.
     *
     * Validates: Requirement 10.2
     */
    object Idle : OutgoingCallUIState()

    /**
     * Calling state - displayed when an outgoing call is being made.
     *
     * @param call The outgoing Call object
     * @param user The User being called (optional, may be null for group calls)
     *
     * Validates: Requirement 10.3
     */
    data class Calling(val call: Call, val user: User?) : OutgoingCallUIState()

    /**
     * Accepted state - displayed when the call has been accepted by the recipient.
     *
     * @param call The accepted Call object
     *
     * Validates: Requirement 10.4
     */
    data class Accepted(val call: Call) : OutgoingCallUIState()

    /**
     * Rejected state - displayed when the call has been rejected by the recipient.
     *
     * @param call The rejected Call object
     *
     * Validates: Requirement 10.5
     */
    data class Rejected(val call: Call) : OutgoingCallUIState()

    /**
     * OngoingCall state - displayed when the call is in progress.
     *
     * @param sessionId The session ID of the ongoing call
     * @param callType The type of call (audio/video)
     *
     * Validates: Requirement 10.6
     */
    data class OngoingCall(val sessionId: String, val callType: String) : OutgoingCallUIState()

    /**
     * Error state - displayed when an error occurs during call handling.
     *
     * @param exception The CometChatException that caused the error
     *
     * Validates: Requirement 10.7
     */
    data class Error(val exception: CometChatException) : OutgoingCallUIState()
}
