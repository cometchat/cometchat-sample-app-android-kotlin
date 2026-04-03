package com.cometchat.uikit.core.state

import com.cometchat.chat.core.Call
import com.cometchat.chat.exceptions.CometChatException

/**
 * Sealed class representing UI states for the incoming call screen.
 * Used by the ViewModel to communicate current state to the UI.
 *
 * Validates: Requirements 6.1-6.7
 */
sealed class IncomingCallUIState {
    /**
     * Idle state - initial state before any call is received.
     *
     * Validates: Requirement 6.2
     */
    object Idle : IncomingCallUIState()

    /**
     * Ringing state - displayed when an incoming call is being received.
     *
     * @param call The incoming Call object
     *
     * Validates: Requirement 6.3
     */
    data class Ringing(val call: Call) : IncomingCallUIState()

    /**
     * Accepted state - displayed when the call has been accepted.
     *
     * @param call The accepted Call object
     *
     * Validates: Requirement 6.4
     */
    data class Accepted(val call: Call) : IncomingCallUIState()

    /**
     * Rejected state - displayed when the call has been rejected.
     *
     * @param call The rejected Call object
     *
     * Validates: Requirement 6.5
     */
    data class Rejected(val call: Call) : IncomingCallUIState()

    /**
     * Cancelled state - displayed when the call has been cancelled by the caller.
     *
     * @param call The cancelled Call object
     *
     * Validates: Requirement 6.6
     */
    data class Cancelled(val call: Call) : IncomingCallUIState()

    /**
     * Error state - displayed when an error occurs during call handling.
     *
     * @param exception The CometChatException that caused the error
     *
     * Validates: Requirement 6.7
     */
    data class Error(val exception: CometChatException) : IncomingCallUIState()
}
