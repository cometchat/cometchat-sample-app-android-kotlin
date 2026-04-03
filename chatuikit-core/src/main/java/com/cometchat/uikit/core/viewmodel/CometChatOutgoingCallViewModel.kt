package com.cometchat.uikit.core.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.Call
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.uikit.core.events.CometChatCallEvent
import com.cometchat.uikit.core.events.CometChatEvents
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for CometChatOutgoingCall component.
 * Manages outgoing call state and actions for both Compose and XML Views implementations.
 * 
 * This ViewModel:
 * - Exposes StateFlows for the current call, accepted call, rejected call, and endCallButtonEnabled states
 * - Exposes a SharedFlow for error events
 * - Provides methods to set the call and cancel outgoing calls
 * - Listens for onOutgoingCallAccepted, onOutgoingCallRejected, and onCallEndedMessageReceived events
 * - Emits CometChatCallEvent.CallRejected through CometChatEvents when call is cancelled
 * - Sets endCallButtonEnabled to false when call is accepted
 * 
 * Validates: Requirements 9.1-9.14, 2.2, 2.3, 2.4
 */
open class CometChatOutgoingCallViewModel(
    private val enableListeners: Boolean = true
) : ViewModel() {

    companion object {
        private const val TAG = "CometChatOutgoingCallViewModel"
    }

    // ==================== State Flows ====================

    /**
     * StateFlow for the current outgoing call.
     * 
     * Validates: Requirement 9.2
     */
    private val _call = MutableStateFlow<Call?>(null)
    val call: StateFlow<Call?> = _call.asStateFlow()

    /**
     * StateFlow for the accepted call result.
     * Emits the Call object when the recipient accepts the call.
     * 
     * Validates: Requirement 9.3
     */
    private val _acceptedCall = MutableStateFlow<Call?>(null)
    val acceptedCall: StateFlow<Call?> = _acceptedCall.asStateFlow()

    /**
     * StateFlow for the rejected/cancelled call result.
     * Emits the Call object when cancelCall succeeds or when the recipient rejects the call.
     * 
     * Validates: Requirement 9.4
     */
    private val _rejectedCall = MutableStateFlow<Call?>(null)
    val rejectedCall: StateFlow<Call?> = _rejectedCall.asStateFlow()

    /**
     * SharedFlow for error events.
     * Emits CometChatException when an error occurs during call operations.
     * 
     * Validates: Requirement 9.5
     */
    private val _errorEvent = MutableSharedFlow<CometChatException>()
    val errorEvent: SharedFlow<CometChatException> = _errorEvent.asSharedFlow()

    /**
     * StateFlow for the end call button enabled state.
     * Set to false when the call is accepted to prevent cancellation during transition.
     * 
     * Validates: Requirement 9.6
     */
    private val _endCallButtonEnabled = MutableStateFlow(true)
    val endCallButtonEnabled: StateFlow<Boolean> = _endCallButtonEnabled.asStateFlow()

    // ==================== Internal State ====================

    private var listenerId: String? = null

    // ==================== Initialization ====================

    init {
        if (enableListeners) {
            addListeners()
        }
    }

    // ==================== Public Methods ====================

    /**
     * Sets the current outgoing call.
     * 
     * @param call The outgoing Call object
     */
    fun setCall(call: Call) {
        _call.value = call
        // Reset end call button enabled state when a new call is set
        _endCallButtonEnabled.value = true
    }

    /**
     * Gets the current call.
     * 
     * @return The current Call or null if not set
     */
    fun getCall(): Call? = _call.value

    /**
     * Cancels the current outgoing call.
     * 
     * Calls CometChat.rejectCall with CALL_STATUS_CANCELLED status.
     * 
     * On success:
     * - Emits the cancelled Call through rejectedCall StateFlow
     * - Emits CometChatCallEvent.CallRejected through CometChatEvents
     * 
     * On failure:
     * - Emits the CometChatException through errorEvent SharedFlow
     * 
     * Validates: Requirements 9.7, 9.8, 9.9, 9.10
     */
    fun cancelCall() {
        val currentCall = _call.value ?: return
        
        CometChat.rejectCall(
            currentCall.sessionId,
            CometChatConstants.CALL_STATUS_CANCELLED,
            object : CometChat.CallbackListener<Call>() {
                override fun onSuccess(call: Call) {
                    // Emit cancelled call through StateFlow
                    _rejectedCall.value = call
                    
                    // Emit CallRejected event through CometChatEvents
                    CometChatEvents.emitCallEvent(CometChatCallEvent.CallRejected(call))
                }

                override fun onError(exception: CometChatException) {
                    // Emit error through SharedFlow
                    viewModelScope.launch {
                        _errorEvent.emit(exception)
                    }
                }
            }
        )
    }

    /**
     * Adds CometChat SDK call listeners for handling outgoing call events.
     * 
     * Listens for:
     * - onOutgoingCallAccepted: When the recipient accepts the call
     * - onOutgoingCallRejected: When the recipient rejects the call
     * - onCallEndedMessageReceived: When the call ends due to timeout, busy, or system termination
     * 
     * Validates: Requirement 9.11, 2.2, 2.3, 2.4
     */
    fun addListeners() {
        if (listenerId != null) {
            // Listeners already added
            return
        }
        
        listenerId = "OutgoingCall_${System.currentTimeMillis()}"
        
        CometChat.addCallListener(listenerId!!, object : CometChat.CallListener() {
            override fun onIncomingCallReceived(call: Call?) {
                // Not handled by outgoing call component
            }

            /**
             * Called when the recipient accepts the outgoing call.
             * 
             * - Emits the accepted Call through acceptedCall StateFlow
             * - Sets endCallButtonEnabled to false to prevent cancellation during transition
             * 
             * Validates: Requirements 9.12, 9.13
             */
            override fun onOutgoingCallAccepted(call: Call?) {
                call?.let { acceptedCall ->
                    handleOutgoingCallAccepted(acceptedCall)
                }
            }

            /**
             * Called when the recipient rejects the outgoing call.
             * 
             * Emits the rejected Call through rejectedCall StateFlow.
             * 
             * Validates: Requirement 9.14
             */
            override fun onOutgoingCallRejected(call: Call?) {
                call?.let { rejectedCall ->
                    handleOutgoingCallRejected(rejectedCall)
                }
            }

            override fun onIncomingCallCancelled(call: Call?) {
                // Not handled by outgoing call component
            }

            /**
             * Called when a call ends due to timeout, busy status, or system termination.
             * 
             * Validates session ID matches current outgoing call before emitting.
             * Emits the ended Call through rejectedCall StateFlow to trigger screen dismissal.
             * 
             * Validates: Requirements 2.2, 2.3, 2.4
             */
            override fun onCallEndedMessageReceived(call: Call?) {
                call?.let { endedCall ->
                    handleCallEndedMessageReceived(endedCall)
                }
            }
        })
    }

    /**
     * Removes the CometChat SDK call listeners.
     */
    fun removeListeners() {
        listenerId?.let { id ->
            CometChat.removeCallListener(id)
            listenerId = null
        }
    }

    // ==================== Private Methods ====================

    /**
     * Handles the onOutgoingCallAccepted event.
     * 
     * - Emits the accepted call through acceptedCall StateFlow
     * - Sets endCallButtonEnabled to false to prevent cancellation during transition
     * 
     * @param acceptedCall The call that was accepted by the recipient
     */
    private fun handleOutgoingCallAccepted(acceptedCall: Call) {
        // Set endCallButtonEnabled to false to prevent cancellation during transition
        _endCallButtonEnabled.value = false
        
        // Emit accepted call through StateFlow
        _acceptedCall.value = acceptedCall
    }

    /**
     * Handles the onOutgoingCallRejected event.
     * 
     * Emits the rejected call through rejectedCall StateFlow.
     * 
     * @param rejectedCall The call that was rejected by the recipient
     */
    private fun handleOutgoingCallRejected(rejectedCall: Call) {
        _rejectedCall.value = rejectedCall
    }

    /**
     * Handles the onCallEndedMessageReceived event.
     * 
     * Only processes the event if the ended call's session ID matches the current
     * outgoing call's session ID to avoid dismissing the screen for unrelated calls.
     * 
     * Emits the ended call through rejectedCall StateFlow to trigger screen dismissal.
     * 
     * @param endedCall The call that ended (timeout, busy, system termination)
     */
    private fun handleCallEndedMessageReceived(endedCall: Call) {
        val currentCall = _call.value ?: return
        
        // Only handle if session IDs match
        if (endedCall.sessionId == currentCall.sessionId) {
            _rejectedCall.value = endedCall
        }
    }

    // ==================== Lifecycle ====================

    override fun onCleared() {
        super.onCleared()
        removeListeners()
    }
}
