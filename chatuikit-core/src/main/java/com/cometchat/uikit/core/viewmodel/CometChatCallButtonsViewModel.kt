package com.cometchat.uikit.core.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cometchat.chat.core.Call
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.domain.usecase.InitiateUserCallUseCase
import com.cometchat.uikit.core.domain.usecase.StartGroupCallUseCase
import com.cometchat.uikit.core.events.CometChatCallEvent
import com.cometchat.uikit.core.events.CometChatEvents
import com.cometchat.uikit.core.state.CallButtonsEvent
import com.cometchat.uikit.core.state.CallButtonsUIState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for CometChatCallButtons component.
 * Manages call initiation state and events for both user and group calls.
 * Shared between Jetpack Compose and XML Views implementations.
 */
open class CometChatCallButtonsViewModel(
    private val initiateUserCallUseCase: InitiateUserCallUseCase,
    private val startGroupCallUseCase: StartGroupCallUseCase,
    private val enableListeners: Boolean = true
) : ViewModel() {

    // UI State
    private val _uiState = MutableStateFlow<CallButtonsUIState>(CallButtonsUIState.Idle)
    val uiState: StateFlow<CallButtonsUIState> = _uiState.asStateFlow()

    // One-time events
    private val _events = MutableSharedFlow<CallButtonsEvent>()
    val events: SharedFlow<CallButtonsEvent> = _events.asSharedFlow()

    // Error events for UI callback
    private val _errorEvent = MutableSharedFlow<CometChatException>()
    val errorEvent: SharedFlow<CometChatException> = _errorEvent.asSharedFlow()

    // Current receiver state
    private var user: User? = null
    private var group: Group? = null
    private var receiverId: String = ""
    private var receiverType: String = ""

    // Listener tag
    private var listenersTag: String? = null

    // Local event listener jobs
    private var callEventsJob: Job? = null

    init {
        if (enableListeners) {
            addListeners()
        }
    }

    /**
     * Sets the user for 1-to-1 calls.
     * @param user The User to call
     */
    fun setUser(user: User) {
        this.user = user
        this.group = null
        this.receiverId = user.uid
        this.receiverType = UIKitConstants.ReceiverType.USER
    }

    /**
     * Sets the group for conference calls.
     * @param group The Group to start a call with
     */
    fun setGroup(group: Group) {
        this.group = group
        this.user = null
        this.receiverId = group.guid
        this.receiverType = UIKitConstants.ReceiverType.GROUP
    }

    /**
     * Gets the currently set user.
     * @return The User or null if not set
     */
    fun getUser(): User? = user

    /**
     * Gets the currently set group.
     * @return The Group or null if not set
     */
    fun getGroup(): Group? = group

    /**
     * Initiates a call with the configured receiver.
     * @param callType The type of call (audio/video) - use CometChatConstants.CALL_TYPE_AUDIO or CALL_TYPE_VIDEO
     */
    fun initiateCall(callType: String) {
        viewModelScope.launch {
            _uiState.value = CallButtonsUIState.Initiating

            when (receiverType) {
                UIKitConstants.ReceiverType.USER -> initiateUserCall(callType)
                UIKitConstants.ReceiverType.GROUP -> initiateGroupCall(callType)
                else -> {
                    val exception = CometChatException(
                        "INVALID_RECEIVER",
                        "No user or group set",
                        "Please set a user or group before initiating a call"
                    )
                    _uiState.value = CallButtonsUIState.Error(exception)
                    _errorEvent.emit(exception)
                }
            }
        }
    }

    private suspend fun initiateUserCall(callType: String) {
        initiateUserCallUseCase(receiverId, callType)
            .onSuccess { call ->
                _uiState.value = CallButtonsUIState.Idle
                _events.emit(CallButtonsEvent.CallInitiated(call))
                CometChatEvents.emitCallEvent(CometChatCallEvent.OutgoingCall(call))
            }
            .onFailure { e ->
                val exception = e as? CometChatException ?: CometChatException(
                    "UNKNOWN_ERROR",
                    e.message ?: "Unknown error",
                    e.message ?: "An unknown error occurred"
                )
                _uiState.value = CallButtonsUIState.Error(exception)
                _errorEvent.emit(exception)
            }
    }

    private suspend fun initiateGroupCall(callType: String) {
        startGroupCallUseCase(receiverId, callType)
            .onSuccess { message ->
                _uiState.value = CallButtonsUIState.Idle
                _events.emit(CallButtonsEvent.StartDirectCall(message))
            }
            .onFailure { e ->
                val exception = e as? CometChatException ?: CometChatException(
                    "UNKNOWN_ERROR",
                    e.message ?: "Unknown error",
                    e.message ?: "An unknown error occurred"
                )
                _uiState.value = CallButtonsUIState.Error(exception)
                _errorEvent.emit(exception)
            }
    }

    private fun addListeners() {
        listenersTag = "CallButtons_${System.currentTimeMillis()}"

        listenersTag?.let { tag ->
            // SDK Call Listener
            CometChat.addCallListener(tag, object : CometChat.CallListener() {
                override fun onIncomingCallReceived(call: Call?) {
                    // Not handled by this component
                }

                override fun onOutgoingCallAccepted(call: Call?) {
                    // Not handled by this component
                }

                override fun onOutgoingCallRejected(call: Call?) {
                    call?.let {
                        viewModelScope.launch {
                            _events.emit(CallButtonsEvent.CallRejected(it))
                        }
                    }
                }

                override fun onIncomingCallCancelled(call: Call?) {
                    call?.let {
                        viewModelScope.launch {
                            _events.emit(CallButtonsEvent.CallRejected(it))
                        }
                    }
                }

                override fun onCallEndedMessageReceived(call: Call?) {
                    call?.let {
                        viewModelScope.launch {
                            _events.emit(CallButtonsEvent.CallRejected(it))
                        }
                    }
                }
            })

            // UIKit Local Events
            callEventsJob = viewModelScope.launch {
                CometChatEvents.callEvents.collect { event ->
                    when (event) {
                        is CometChatCallEvent.CallRejected -> {
                            _events.emit(CallButtonsEvent.CallRejected(event.call))
                        }
                        is CometChatCallEvent.CallEnded -> {
                            _events.emit(CallButtonsEvent.CallRejected(event.call))
                        }
                        else -> { /* Not handled */ }
                    }
                }
            }
        }
    }

    /**
     * Removes all listeners. Called automatically in onCleared().
     */
    fun removeListeners() {
        listenersTag?.let { tag ->
            CometChat.removeCallListener(tag)
        }
        callEventsJob?.cancel()
    }

    override fun onCleared() {
        super.onCleared()
        removeListeners()
    }
}
