package com.cometchat.uikit.core.viewmodel

import android.util.Log
import android.widget.RelativeLayout
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cometchat.calls.core.CallSettings
import com.cometchat.calls.core.CometChatCalls
import com.cometchat.calls.listeners.CometChatCallsEventsListener
import com.cometchat.calls.model.AudioMode
import com.cometchat.calls.model.CallSwitchRequestInfo
import com.cometchat.calls.model.GenerateToken
import com.cometchat.calls.model.RTCMutedUser
import com.cometchat.calls.model.RTCRecordingInfo
import com.cometchat.calls.model.RTCUser
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.Call
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.uikit.core.constants.UIKitConstants.CallWorkFlow
import com.cometchat.uikit.core.events.CometChatCallEvent
import com.cometchat.uikit.core.events.CometChatEvents
import com.cometchat.uikit.core.models.OngoingCallEvent
import com.cometchat.uikit.core.models.OngoingCallUIState
import com.cometchat.uikit.core.utils.CallingState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for CometChatOngoingCall component.
 * Manages ongoing call state and actions for both Compose and XML Views implementations.
 *
 * This ViewModel:
 * - Exposes StateFlow<OngoingCallUIState> for the current UI state (Loading, Connected, Ended, Error)
 * - Exposes StateFlow<Boolean> for loading indicator visibility
 * - Exposes SharedFlow<OngoingCallEvent> for one-time events that should not replay on configuration changes
 * - Provides methods to configure and manage call sessions
 * - Handles call lifecycle events from CometChatCalls SDK
 *
 * Validates: Requirements 1.1, 1.2, 1.3
 */
open class CometChatOngoingCallViewModel : ViewModel() {

    companion object {
        private const val TAG = "CometChatOngoingCallViewModel"
    }

    // ==================== State Flows ====================

    /**
     * StateFlow for the current UI state.
     * Represents Loading, Connected, Ended, or Error states.
     *
     * Validates: Requirement 1.1
     */
    private val _uiState = MutableStateFlow<OngoingCallUIState>(OngoingCallUIState.Loading)
    val uiState: StateFlow<OngoingCallUIState> = _uiState.asStateFlow()

    /**
     * StateFlow for loading indicator visibility.
     * True when connecting to the call, false when connected or ended.
     *
     * Validates: Requirement 1.2
     */
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    /**
     * SharedFlow for one-time events.
     * Emits CallEnded, SessionTimeout, UserJoined, UserLeft, and Error events.
     * Uses SharedFlow to prevent replay on configuration changes.
     *
     * Validates: Requirement 1.3
     */
    private val _events = MutableSharedFlow<OngoingCallEvent>()
    val events: SharedFlow<OngoingCallEvent> = _events.asSharedFlow()

    // ==================== Internal State ====================

    /**
     * Unique listener ID for CometChatCalls event listeners.
     * Generated using timestamp when listeners are added.
     */
    internal var listenerId: String? = null

    /**
     * Current call session ID.
     * Set via setSessionId() method.
     */
    internal var sessionId: String? = null

    /**
     * Current call type ("audio" or "video").
     * Set via setCallType() method.
     */
    internal var callType: String? = null

    /**
     * Current call workflow.
     * DEFAULT for 1:1 calls, MEETING for group calls.
     * Defaults to DEFAULT.
     */
    internal var callWorkFlow: CallWorkFlow = CallWorkFlow.DEFAULT

    /**
     * Custom call settings builder provided by the user.
     * Used to configure call settings before starting the session.
     */
    internal var callSettingsBuilder: CometChatCalls.CallSettingsBuilder? = null

    /**
     * Built call settings used for the current session.
     * Created from callSettingsBuilder with audio-only configuration applied.
     */
    internal var callSettings: CallSettings? = null

    // ==================== Protected Mutable State Accessors ====================

    /**
     * Protected accessor for mutable UI state.
     * Used by subclasses or internal methods to update state.
     */
    protected val mutableUiState: MutableStateFlow<OngoingCallUIState>
        get() = _uiState

    /**
     * Protected accessor for mutable loading state.
     * Used by subclasses or internal methods to update loading state.
     */
    protected val mutableIsLoading: MutableStateFlow<Boolean>
        get() = _isLoading

    /**
     * Protected accessor for mutable events flow.
     * Used by subclasses or internal methods to emit events.
     */
    protected val mutableEvents: MutableSharedFlow<OngoingCallEvent>
        get() = _events

    // ==================== Configuration Methods ====================

    /**
     * Sets the call session ID.
     * @param sessionId The unique identifier for the call session
     *
     * Validates: Requirement 2.5
     */
    fun setSessionId(sessionId: String) {
        this.sessionId = sessionId
    }

    /**
     * Sets the call type.
     * @param callType The type of call ("audio" or "video")
     *
     * Validates: Requirement 2.6
     */
    fun setCallType(callType: String) {
        this.callType = callType
    }

    /**
     * Sets the call workflow.
     * @param workFlow DEFAULT for 1:1 calls, MEETING for group calls
     *
     * Validates: Requirement 2.4
     */
    fun setCallWorkFlow(workFlow: CallWorkFlow) {
        this.callWorkFlow = workFlow
    }

    /**
     * Sets the custom call settings builder.
     * @param builder The CometChatCalls.CallSettingsBuilder to use for call configuration
     *
     * Validates: Requirement 2.3
     */
    fun setCallSettingsBuilder(builder: CometChatCalls.CallSettingsBuilder?) {
        this.callSettingsBuilder = builder
    }

    // ==================== Call Methods ====================

    /**
     * Starts the call session with the configured session ID and call type.
     * 
     * Flow:
     * 1. Set UI state to Loading, isLoading to true
     * 2. Get auth token via CometChat.getUserAuthToken()
     * 3. Build CallSettings with isAudioOnly based on callType
     * 4. Generate call token via CometChatCalls.generateToken()
     * 5. Start session via CometChatCalls.startSession()
     * 6. On success: set isLoading false, UI state to Connected
     * 7. On failure: emit Error event
     *
     * @param callViewContainer The RelativeLayout container where the call UI will be rendered
     *
     * Validates: Requirements 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.7, 3.8, 17.1, 17.2, 17.3
     */
    fun startCall(callViewContainer: RelativeLayout) {
        // Validate required parameters
        val sid = sessionId ?: return
        val type = callType ?: return
        val builder = callSettingsBuilder ?: return

        // Set UI state to Loading, isLoading to true (Requirement 3.1)
        mutableUiState.value = OngoingCallUIState.Loading
        mutableIsLoading.value = true

        // Get auth token (Requirement 3.2)
        val userAuthToken = CometChat.getUserAuthToken() ?: return

        // Build CallSettings with isAudioOnly based on callType (Requirements 17.1, 17.2, 17.3)
        // Property 12: Call Type to Audio-Only Mapping
        val isAudioOnly = type.equals(CometChatConstants.CALL_TYPE_AUDIO, ignoreCase = true)
        callSettings = builder.setIsAudioOnly(isAudioOnly).build()

        // Generate call token (Requirement 3.3)
        CometChatCalls.generateToken(sid, userAuthToken, object : CometChatCalls.CallbackListener<GenerateToken>() {
            override fun onSuccess(generateToken: GenerateToken) {
                // Start session with generated token (Requirement 3.4)
                startSession(generateToken.token, callViewContainer)
            }

            override fun onError(e: com.cometchat.calls.exceptions.CometChatException) {
                // On failure: emit Error event (Requirement 3.7)
                Log.e(TAG, "generateToken error: $e")
                val chatException = CometChatException(e.code, e.message)
                viewModelScope.launch {
                    mutableEvents.emit(OngoingCallEvent.Error(chatException))
                }
            }
        })
    }

    /**
     * Starts the call session with the generated token.
     *
     * @param token The generated call token
     * @param callViewContainer The RelativeLayout container where the call UI will be rendered
     */
    private fun startSession(token: String, callViewContainer: RelativeLayout) {
        val settings = callSettings ?: return
        val sid = sessionId ?: return
        val type = callType ?: return

        CometChatCalls.startSession(token, settings, callViewContainer, object : CometChatCalls.CallbackListener<String>() {
            override fun onSuccess(s: String) {
                // On success: set isLoading false, UI state to Connected (Requirements 3.5, 3.6)
                mutableIsLoading.value = false
                mutableUiState.value = OngoingCallUIState.Connected(sid, type)
            }

            override fun onError(e: com.cometchat.calls.exceptions.CometChatException) {
                // On failure: emit Error event (Requirement 3.8)
                Log.e(TAG, "startSession error: $e")
                val chatException = CometChatException(e.code, e.message)
                viewModelScope.launch {
                    mutableEvents.emit(OngoingCallEvent.Error(chatException))
                }
            }
        })
    }

    /**
     * Ends the current call with workflow-dependent cleanup.
     *
     * For DEFAULT workflow (1:1 calls):
     * 1. Call CometChat.endCall(sessionId)
     * 2. On success: call CometChatCalls.endSession(), CometChat.clearActiveCall()
     * 3. Set UI state to Ended, emit CallEnded event
     * 4. On failure: still set Ended state, emit Error event
     *
     * For MEETING workflow (group calls):
     * 1. Call CometChatCalls.endSession() only
     * 2. Set CallingState.setIsActiveMeeting(false)
     * 3. Set UI state to Ended, emit CallEnded event
     *
     * Property 6: Workflow-Dependent Call End Behavior
     * Validates: Requirements 7.1, 7.2, 7.3, 7.4, 7.5, 7.6, 7.7
     */
    fun endCall() {
        when (callWorkFlow) {
            CallWorkFlow.MEETING -> {
                // For MEETING: call CometChatCalls.endSession() only
                CometChatCalls.endSession()
                CallingState.setIsActiveMeeting(false)
                mutableUiState.value = OngoingCallUIState.Ended
                viewModelScope.launch {
                    mutableEvents.emit(OngoingCallEvent.CallEnded)
                }
            }
            CallWorkFlow.DEFAULT -> {
                // For DEFAULT: call CometChat.endCall(), then cleanup
                val sid = sessionId ?: run {
                    // If no session ID, still transition to Ended state
                    mutableUiState.value = OngoingCallUIState.Ended
                    return
                }

                CometChat.endCall(sid, object : CometChat.CallbackListener<Call>() {
                    override fun onSuccess(call: Call?) {
                        // On success: call CometChatCalls.endSession(), CometChat.clearActiveCall()
                        // (Requirement 7.2, 7.3)
                        CometChatCalls.endSession()
                        CometChat.clearActiveCall()
                        
                        // Set UI state to Ended, emit CallEnded event (Requirement 7.4, 7.5)
                        mutableUiState.value = OngoingCallUIState.Ended
                        viewModelScope.launch {
                            mutableEvents.emit(OngoingCallEvent.CallEnded)
                        }
                        
                        // Emit UIKit event for inter-component communication
                        // Matches Java's CometChatUIKitHelper.onCallEnded(call) behavior
                        call?.let {
                            CometChatEvents.emitCallEvent(CometChatCallEvent.CallEnded(it))
                        }
                    }

                    override fun onError(e: CometChatException) {
                        // On failure: still set Ended state, emit Error event (Requirement 7.6, 7.7)
                        Log.e(TAG, "endCall error: $e")
                        mutableUiState.value = OngoingCallUIState.Ended
                        viewModelScope.launch {
                            mutableEvents.emit(OngoingCallEvent.Error(e))
                        }
                    }
                })
            }
        }
    }

    // ==================== Listener Methods ====================

    /**
     * Registers call event listeners with the CometChatCalls SDK.
     * Generates a unique listener ID using the current timestamp.
     *
     * Property 11: Unique Listener ID Generation
     * - The generated listener ID is unique (timestamp-based)
     * - Used consistently for both addCallsEventListeners() and removeCallsEventListeners()
     *
     * Validates: Requirements 6.1, 6.2, 6.3, 6.4, 6.5, 6.6, 6.7, 13.3, 13.4, 13.5
     */
    fun addListeners() {
        // Generate unique listener ID using timestamp (Requirement 13.3)
        listenerId = System.currentTimeMillis().toString()

        // Register CometChatCallsEventsListener (Requirement 13.4)
        CometChatCalls.addCallsEventListeners(listenerId!!, object : CometChatCallsEventsListener {

            /**
             * Called when the call is ended by the remote party.
             * For DEFAULT workflow: end session + clear active call + exit
             * For MEETING workflow: do nothing
             *
             * Validates: Requirements 6.1, 6.2
             */
            override fun onCallEnded() {
                when (callWorkFlow) {
                    CallWorkFlow.DEFAULT -> {
                        // End session + clear active call (Requirement 6.1)
                        CometChatCalls.endSession()
                        CometChat.clearActiveCall()
                        mutableUiState.value = OngoingCallUIState.Ended
                        viewModelScope.launch {
                            mutableEvents.emit(OngoingCallEvent.CallEnded)
                        }
                    }
                    CallWorkFlow.MEETING -> {
                        // Do nothing for MEETING workflow (Requirement 6.2)
                    }
                }
            }

            /**
             * Called when the user presses the end call button in the SDK UI.
             * For DEFAULT workflow: call endCall()
             * For MEETING workflow: end session only + exit
             *
             * Validates: Requirements 6.3, 6.4
             */
            override fun onCallEndButtonPressed() {
                when (callWorkFlow) {
                    CallWorkFlow.DEFAULT -> {
                        // Call endCall() for full cleanup (Requirement 6.3)
                        endCall()
                    }
                    CallWorkFlow.MEETING -> {
                        // End session only for MEETING workflow (Requirement 6.4)
                        CometChatCalls.endSession()
                        CallingState.setIsActiveMeeting(false)
                        mutableUiState.value = OngoingCallUIState.Ended
                        viewModelScope.launch {
                            mutableEvents.emit(OngoingCallEvent.CallEnded)
                        }
                    }
                }
            }

            /**
             * Called when the call session times out.
             * End session + emit SessionTimeout event + exit (same for both workflows)
             *
             * Validates: Requirement 6.5
             */
            override fun onSessionTimeout() {
                CallingState.setIsActiveMeeting(false)
                CometChatCalls.endSession()
                mutableUiState.value = OngoingCallUIState.Ended
                viewModelScope.launch {
                    mutableEvents.emit(OngoingCallEvent.SessionTimeout)
                }
            }

            /**
             * Called when a user joins the call.
             * Emits UserJoined event with isCurrentUser detection.
             *
             * Property 8: User Join Detection
             * - isCurrentUser equals true if and only if the joined user's UID
             *   matches CometChat.getLoggedInUser().uid
             *
             * Validates: Requirements 6.6, 18.2
             */
            override fun onUserJoined(user: RTCUser?) {
                user?.let { rtcUser ->
                    val currentUserId = CometChat.getLoggedInUser()?.uid
                    val isCurrentUser = rtcUser.uid == currentUserId
                    viewModelScope.launch {
                        mutableEvents.emit(OngoingCallEvent.UserJoined(rtcUser.uid, isCurrentUser))
                    }
                }
            }

            /**
             * Called when a user leaves the call.
             * Emits UserLeft event with the user ID.
             *
             * Validates: Requirement 6.7
             */
            override fun onUserLeft(user: RTCUser?) {
                user?.let { rtcUser ->
                    viewModelScope.launch {
                        mutableEvents.emit(OngoingCallEvent.UserLeft(rtcUser.uid))
                    }
                }
            }

            /**
             * Called when an error occurs during the call.
             * Emits Error event with the exception.
             */
            override fun onError(e: com.cometchat.calls.exceptions.CometChatException?) {
                e?.let { exception ->
                    Log.e(TAG, "Call error: $exception")
                    val chatException = CometChatException(exception.code, exception.message)
                    viewModelScope.launch {
                        mutableEvents.emit(OngoingCallEvent.Error(chatException))
                    }
                }
            }

            // ==================== Other Callbacks (No-op) ====================

            override fun onUserListChanged(users: ArrayList<RTCUser>?) {
                // No action required per design
            }

            override fun onAudioModeChanged(audioModes: ArrayList<AudioMode>?) {
                // No action required per design
            }

            override fun onCallSwitchedToVideo(info: CallSwitchRequestInfo?) {
                // No action required per design
            }

            override fun onUserMuted(mutedUser: RTCMutedUser?) {
                // No action required per design
            }

            override fun onRecordingToggled(info: RTCRecordingInfo?) {
                // No action required per design
            }
        })
    }

    /**
     * Unregisters call event listeners from the CometChatCalls SDK.
     * Uses the same listener ID that was generated in addListeners().
     *
     * Property 11: Unique Listener ID Generation
     * - Uses the same listener ID consistently for removeCallsEventListeners()
     *
     * Validates: Requirement 13.5
     */
    fun removeListeners() {
        listenerId?.let { id ->
            CometChatCalls.removeCallsEventListeners(id)
        }
    }

    // ==================== Lifecycle Methods ====================

    /**
     * Called when the ViewModel is cleared.
     * Removes all call event listeners to prevent memory leaks.
     *
     * Validates: Requirement 1.6
     */
    override fun onCleared() {
        super.onCleared()
        removeListeners()
    }
}
