package com.cometchat.uikit.core.events

import com.cometchat.chat.core.Call

/**
 * Sealed class hierarchy representing all call-related events.
 * Provides type-safe event handling for call state changes.
 */
sealed class CometChatCallEvent {
    /**
     * Event emitted when an outgoing call is initiated.
     * @param call The outgoing call
     */
    data class OutgoingCall(
        val call: Call
    ) : CometChatCallEvent()

    /**
     * Event emitted when a call is accepted.
     * @param call The accepted call
     */
    data class CallAccepted(
        val call: Call
    ) : CometChatCallEvent()

    /**
     * Event emitted when a call is rejected.
     * @param call The rejected call
     */
    data class CallRejected(
        val call: Call
    ) : CometChatCallEvent()

    /**
     * Event emitted when a call ends.
     * @param call The ended call
     */
    data class CallEnded(
        val call: Call
    ) : CometChatCallEvent()
}
