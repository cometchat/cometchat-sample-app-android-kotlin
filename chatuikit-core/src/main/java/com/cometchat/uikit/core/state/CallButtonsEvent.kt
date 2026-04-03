package com.cometchat.uikit.core.state

import com.cometchat.chat.core.Call
import com.cometchat.chat.models.BaseMessage

/**
 * Sealed class representing one-time events emitted by CometChatCallButtons component.
 * These events are used to notify the UI about call-related actions.
 */
sealed class CallButtonsEvent {
    /**
     * Event emitted when a user call is successfully initiated.
     * @param call The initiated Call object
     */
    data class CallInitiated(val call: Call) : CallButtonsEvent()

    /**
     * Event emitted when a group call message is successfully sent.
     * Used to start a direct/conference call with a group.
     * @param message The CustomMessage sent for the group call
     */
    data class StartDirectCall(val message: BaseMessage) : CallButtonsEvent()

    /**
     * Event emitted when a call is rejected or ended.
     * @param call The rejected/ended Call object
     */
    data class CallRejected(val call: Call) : CallButtonsEvent()
}
