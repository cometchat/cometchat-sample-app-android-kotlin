package com.cometchat.uikit.core.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.Call
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.uikit.core.events.CometChatCallEvent
import com.cometchat.uikit.core.events.CometChatEvents
import com.cometchat.uikit.core.utils.CallManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for CometChatIncomingCall component.
 * Manages incoming call state and actions for both Compose and XML Views implementations.
 * 
 * This ViewModel:
 * - Exposes StateFlows for the current call, accepted call, and rejected call states
 * - Exposes a SharedFlow for error events
 * - Provides methods to accept and reject incoming calls
 * - Listens for onIncomingCallCancelled events to handle caller cancellation
 * - Emits CometChatCallEvent.CallAccepted/CallRejected through CometChatEvents
 * 
 * Validates: Requirements 5.1-5.14
 */
open class CometChatIncomingCallViewModel(
    private val enableListeners: Boolean = true
) : ViewModel() {

    companion object {
        private const val TAG = "CometChatIncomingCallViewModel"
    }

    // ==================== State Flows ====================

    /**
     * StateFlow for the current incoming call.
     * 
     * Validates: Requirement 5.2
     */
    private val _call = MutableStateFlow<Call?>(null)
    val call: StateFlow<Call?> = _call.asStateFlow()

    /**
     * StateFlow for the accepted call result.
     * Emits the Call object when acceptCall succeeds.
     * 
     * Validates: Requirement 5.3
     */
    private val _acceptedCall = MutableStateFlow<Call?>(null)
    val acceptedCall: StateFlow<Call?> = _acceptedCall.asStateFlow()

    /**
     * StateFlow for the rejected/cancelled call result.
     * Emits the Call object when rejectCall succeeds or when the call is cancelled by the caller.
     * 
     * Validates: Requirement 5.4
     */
    private val _rejectedCall = MutableStateFlow<Call?>(null)
    val rejectedCall: StateFlow<Call?> = _rejectedCall.asStateFlow()

    /**
     * SharedFlow for error events.
     * Emits CometChatException when an error occurs during call operations.
     * 
     * Validates: Requirement 5.5
     */
    private val _errorEvent = MutableSharedFlow<CometChatException>()
    val errorEvent: SharedFlow<CometChatException> = _errorEvent.asSharedFlow()

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
     * Sets the current incoming call.
     * Resets any stale state from previous calls to prevent unintended actions.
     * 
     * @param call The incoming Call object
     */
    fun setCall(call: Call) {
        // Reset stale state from previous calls to prevent unintended actions
        // This is important when the ViewModel is reused (e.g., in Compose with remember)
        resetState()
        _call.value = call
    }

    /**
     * Gets the current call.
     * 
     * @return The current Call or null if not set
     */
    fun getCall(): Call? = _call.value

    /**
     * Resets the ViewModel state to initial values.
     * Called when a new call is set to prevent stale state from triggering actions.
     */
    fun resetState() {
        _acceptedCall.value = null
        _rejectedCall.value = null
    }

    /**
     * Accepts the current incoming call.
     * 
     * On success:
     * - Emits the accepted Call through acceptedCall StateFlow
     * - Emits CometChatCallEvent.CallAccepted through CometChatEvents
     * 
     * On failure:
     * - Emits the CometChatException through errorEvent SharedFlow
     * 
     * Validates: Requirements 5.6, 5.8, 5.9, 5.12
     */
    fun acceptCall() {
        val currentCall = _call.value ?: return
        
        CometChat.acceptCall(currentCall.sessionId, object : CometChat.CallbackListener<Call>() {
            override fun onSuccess(call: Call) {
                // Emit accepted call through StateFlow
                _acceptedCall.value = call
                
                // Emit CallAccepted event through CometChatEvents
                CometChatEvents.emitCallEvent(CometChatCallEvent.CallAccepted(call))
            }

            override fun onError(exception: CometChatException) {
                // Emit error through SharedFlow
                viewModelScope.launch {
                    _errorEvent.emit(exception)
                }
            }
        })
    }

    /**
     * Rejects the current incoming call.
     * 
     * On success:
     * - Emits the rejected Call through rejectedCall StateFlow
     * - Emits CometChatCallEvent.CallRejected through CometChatEvents
     * 
     * On failure:
     * - Emits the CometChatException through errorEvent SharedFlow
     * - Still emits CallRejected event (following Java implementation pattern)
     * 
     * Validates: Requirements 5.7, 5.10, 5.11, 5.12
     */
    fun rejectCall() {
        val currentCall = _call.value ?: return
        
        CometChat.rejectCall(
            currentCall.sessionId,
            CometChatConstants.CALL_STATUS_REJECTED,
            object : CometChat.CallbackListener<Call>() {
                override fun onSuccess(call: Call) {
                    // Emit rejected call through StateFlow
                    _rejectedCall.value = call
                    
                    // Emit CallRejected event through CometChatEvents
                    CometChatEvents.emitCallEvent(CometChatCallEvent.CallRejected(call))
                }

                override fun onError(exception: CometChatException) {
                    // Emit CallRejected event even on error (following Java implementation)
                    _call.value?.let { call ->
                        CometChatEvents.emitCallEvent(CometChatCallEvent.CallRejected(call))
                    }
                    
                    // Emit error through SharedFlow
                    viewModelScope.launch {
                        _errorEvent.emit(exception)
                    }
                }
            }
        )
    }

    /**
     * Adds CometChat SDK call listeners for handling incoming call cancellation.
     * 
     * Validates: Requirement 5.13
     */
    fun addListeners() {
        if (listenerId != null) {
            // Listeners already added
            return
        }
        
        listenerId = "IncomingCall_${System.currentTimeMillis()}"
        
        CometChat.addCallListener(listenerId!!, object : CometChat.CallListener() {
            override fun onIncomingCallReceived(call: Call?) {
                // Not handled - incoming calls are set via setCall()
            }

            override fun onOutgoingCallAccepted(call: Call?) {
                // Not handled by incoming call component
            }

            override fun onOutgoingCallRejected(call: Call?) {
                // Not handled by incoming call component
            }

            /**
             * Called when the caller cancels the incoming call.
             * 
             * If the session ID matches the current call (or if there's no active call
             * in CallManager and the session ID matches), emit the cancelled call
             * through the rejectedCall StateFlow.
             * 
             * Validates: Requirement 5.14
             */
            override fun onIncomingCallCancelled(call: Call?) {
                call?.let { cancelledCall ->
                    handleIncomingCallCancelled(cancelledCall)
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
     * Handles the onIncomingCallCancelled event.
     * 
     * Checks if the cancelled call's session ID matches the current call's session ID.
     * If there's no active call in CometChat SDK and the session IDs match (or if
     * CallManager has no active call), emits the cancelled call through rejectedCall.
     * 
     * This follows the same logic as the Java implementation in IncomingCallViewModel.
     * 
     * @param cancelledCall The call that was cancelled by the caller
     */
    private fun handleIncomingCallCancelled(cancelledCall: Call) {
        // Get the session ID from CallManager's active call (if any)
        val activeCallSessionId = CallManager.getActiveCall()?.sessionId ?: ""
        
        // Check if there's no active call in CometChat SDK
        // and if the session IDs match (or if there's no active call in CallManager)
        if (CometChat.getActiveCall() == null && 
            (cancelledCall.sessionId == activeCallSessionId || activeCallSessionId.isEmpty())) {
            // Also check against the current call in this ViewModel
            val currentCall = _call.value
            if (currentCall == null || currentCall.sessionId == cancelledCall.sessionId) {
                _rejectedCall.value = cancelledCall
            }
        }
    }

    // ==================== Lifecycle ====================

    override fun onCleared() {
        super.onCleared()
        removeListeners()
    }
}
