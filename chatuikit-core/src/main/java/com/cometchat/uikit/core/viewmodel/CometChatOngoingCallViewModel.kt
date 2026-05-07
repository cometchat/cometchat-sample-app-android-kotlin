package com.cometchat.uikit.core.viewmodel

import android.util.Log
import android.widget.RelativeLayout
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cometchat.calls.core.CometChatCalls
import com.cometchat.calls.model.AudioMode
import com.cometchat.calls.model.SessionType
import com.cometchat.calls.listeners.ButtonClickListener
import com.cometchat.calls.listeners.SessionStatusListener
import com.cometchat.calls.listeners.ParticipantEventListener
import com.cometchat.calls.core.CallSession
import com.cometchat.calls.model.Participant
import com.cometchat.calls.core.SessionSettings
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
    internal var lifecycleOwner: LifecycleOwner? = null

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
     * Custom session settings builder provided by the user.
     * Used to configure session settings before joining the session.
     */
    internal var sessionSettingsBuilder: CometChatCalls.SessionSettingsBuilder? = null

    /**
     * Built session settings used for the current session.
     * Created from sessionSettingsBuilder with session type configuration applied.
     */
    internal var sessionSettings: SessionSettings? = null

    /**
     * The active CallSession instance returned by joinSession().
     * Used for session actions (leaveSession, etc.) and lifecycle-aware listeners.
     */
    internal var callSession: CallSession? = null

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
     * Sets the custom session settings builder.
     * @param builder The CometChatCalls.SessionSettingsBuilder to use for session configuration
     *
     * Validates: Requirement 2.3
     */
    fun setSessionSettingsBuilder(builder: CometChatCalls.SessionSettingsBuilder?) {
        this.sessionSettingsBuilder = builder
    }

    // ==================== Call Methods ====================

    /**
     * Starts the call session with the configured session ID and call type.
     * 
     * Flow (v5):
     * 1. Set UI state to Loading, isLoading to true
     * 2. Build SessionSettings with session type based on callType
     * 3. Join session via CometChatCalls.joinSession() (handles token generation internally)
     * 4. On success: store CallSession, set isLoading false, UI state to Connected
     * 5. On failure: emit Error event
     *
     * @param callViewContainer The RelativeLayout container where the call UI will be rendered
     *
     * Validates: Requirements 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.7, 3.8, 17.1, 17.2, 17.3
     */
    fun startCall(callViewContainer: RelativeLayout) {
        // Validate required parameters
        val sid = sessionId ?: return
        val type = callType ?: return
        val builder = sessionSettingsBuilder ?: return

        // Set UI state to Loading, isLoading to true (Requirement 3.1)
        mutableUiState.value = OngoingCallUIState.Loading
        mutableIsLoading.value = true

        // Build SessionSettings with session type based on callType (Requirements 17.1, 17.2, 17.3)
        val sessionType = if (type.equals(CometChatConstants.CALL_TYPE_AUDIO, ignoreCase = true)) {
            SessionType.VOICE
        } else {
            SessionType.VIDEO
        }
        sessionSettings = builder.setSessionType(sessionType).build()

        val settings = sessionSettings ?: return

        // Join session — v5 handles token generation internally (Requirements 3.3, 3.4)
        CometChatCalls.joinSession(sid, settings, callViewContainer, object : CometChatCalls.CallbackListener<CallSession>() {
            override fun onSuccess(session: CallSession) {
                // Store the CallSession for actions and events
                callSession = session
                // On success: set isLoading false, UI state to Connected (Requirements 3.5, 3.6)
                mutableIsLoading.value = false
                mutableUiState.value = OngoingCallUIState.Connected(sid, type)
            }

            override fun onError(e: com.cometchat.calls.exceptions.CometChatException) {
                // On failure: emit Error event (Requirement 3.7)
                Log.e(TAG, "joinSession error: $e")
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
     * 2. On success: call callSession.leaveSession(), CometChat.clearActiveCall()
     * 3. Set UI state to Ended, emit CallEnded event
     * 4. On failure: still set Ended state, emit Error event
     *
     * For MEETING workflow (group calls):
     * 1. Call callSession.leaveSession() only
     * 2. Set CallingState.setIsActiveMeeting(false)
     * 3. Set UI state to Ended, emit CallEnded event
     *
     * Property 6: Workflow-Dependent Call End Behavior
     * Validates: Requirements 7.1, 7.2, 7.3, 7.4, 7.5, 7.6, 7.7
     */
    fun endCall() {
        when (callWorkFlow) {
            CallWorkFlow.MEETING -> {
                // For MEETING: leave session via CallSession instance
                callSession?.leaveSession()
                    ?: CallSession.getInstance()?.leaveSession()
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
                        // On success: leave session via CallSession, clear active call
                        // (Requirement 7.2, 7.3)
                        callSession?.leaveSession()
                            ?: CallSession.getInstance()?.leaveSession()
                        CometChat.clearActiveCall()
                        
                        // Set UI state to Ended, emit CallEnded event (Requirement 7.4, 7.5)
                        mutableUiState.value = OngoingCallUIState.Ended
                        viewModelScope.launch {
                            mutableEvents.emit(OngoingCallEvent.CallEnded)
                        }
                        
                        // Emit UIKit event for inter-component communication
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
     * Registers v5 lifecycle-aware call event listeners on the CallSession instance.
     * Uses SessionStatusListener, ParticipantEventListener, and ButtonClickListener.
     *
     * v5 listeners auto-remove on lifecycle owner destroy — no manual removal needed.
     *
     * Validates: Requirements 6.1, 6.2, 6.3, 6.4, 6.5, 6.6, 6.7, 13.3, 13.4, 13.5
     */
    fun addListeners(owner: LifecycleOwner? = null) {
        val session = callSession ?: CallSession.getInstance() ?: return
        // Use provided owner, stored owner, or skip registration.
        // Listeners will be registered when startCall() succeeds if no owner is available yet.
        val resolvedOwner: LifecycleOwner = owner ?: this.lifecycleOwner ?: return

        // 1. Session lifecycle listener
        session.addSessionStatusListener(resolvedOwner, object : SessionStatusListener() {
            override fun onSessionJoined() {
                // No additional action — UI state already set in joinSession callback
            }

            /**
             * Called when the session ends (remote party ended or server-side end).
             * For DEFAULT workflow: leave session + clear active call + exit
             * For MEETING workflow: do nothing
             *
             * Validates: Requirements 6.1, 6.2
             */
            override fun onSessionLeft() {
                when (callWorkFlow) {
                    CallWorkFlow.DEFAULT -> {
                        // Session already left — just clean up and transition to Ended
                        CometChat.clearActiveCall()
                        mutableUiState.value = OngoingCallUIState.Ended
                        viewModelScope.launch {
                            mutableEvents.emit(OngoingCallEvent.CallEnded)
                        }
                    }
                    CallWorkFlow.MEETING -> {
                        // For MEETING: also transition to Ended so the screen closes
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
             * Leave session + emit SessionTimeout event + exit (same for both workflows)
             *
             * Validates: Requirement 6.5
             */
            override fun onSessionTimedOut() {
                CallingState.setIsActiveMeeting(false)
                // Session already timed out — no need to call leaveSession()
                mutableUiState.value = OngoingCallUIState.Ended
                viewModelScope.launch {
                    mutableEvents.emit(OngoingCallEvent.SessionTimeout)
                }
            }

            override fun onConnectionLost() {
                Log.w(TAG, "Call connection lost")
            }

            override fun onConnectionRestored() {
                Log.d(TAG, "Call connection restored")
            }

            override fun onConnectionClosed() {
                Log.d(TAG, "Call connection closed")
            }
        })

        // 2. Participant event listener
        session.addParticipantEventListener(resolvedOwner, object : ParticipantEventListener() {
            /**
             * Called when a participant joins the call.
             * Emits UserJoined event with isCurrentUser detection.
             *
             * Validates: Requirements 6.6, 18.2
             */
            override fun onParticipantJoined(participant: Participant) {
                val currentUserId = CometChat.getLoggedInUser()?.uid
                val isCurrentUser = participant.uid == currentUserId
                viewModelScope.launch {
                    mutableEvents.emit(OngoingCallEvent.UserJoined(participant.uid, isCurrentUser))
                }
            }

            /**
             * Called when a participant leaves the call.
             * Emits UserLeft event with the user ID.
             *
             * Validates: Requirement 6.7
             */
            override fun onParticipantLeft(participant: Participant) {
                viewModelScope.launch {
                    mutableEvents.emit(OngoingCallEvent.UserLeft(participant.uid))
                }
            }

            override fun onParticipantListChanged(participants: List<Participant>) {
                // No action required per design
            }

            override fun onParticipantAudioMuted(participant: Participant) {
                // No action required per design
            }

            override fun onParticipantAudioUnmuted(participant: Participant) {
                // No action required per design
            }

            override fun onParticipantVideoPaused(participant: Participant) {
                // No action required per design
            }

            override fun onParticipantVideoResumed(participant: Participant) {
                // No action required per design
            }

            override fun onParticipantHandRaised(participant: Participant) {
                // No action required per design
            }

            override fun onParticipantHandLowered(participant: Participant) {
                // No action required per design
            }

            override fun onParticipantStartedRecording(participant: Participant) {
                // No action required per design
            }

            override fun onParticipantStoppedRecording(participant: Participant) {
                // No action required per design
            }

            override fun onDominantSpeakerChanged(participant: Participant) {
                // No action required per design
            }
        })

        // 3. Button click listener
        session.addButtonClickListener(resolvedOwner, object : ButtonClickListener() {
            /**
             * Called when the user presses the leave session button in the SDK UI.
             * For DEFAULT workflow: call endCall()
             * For MEETING workflow: leave session only + exit
             *
             * Validates: Requirements 6.3, 6.4
             */
            override fun onLeaveSessionButtonClicked() {
                when (callWorkFlow) {
                    CallWorkFlow.DEFAULT -> {
                        endCall()
                    }
                    CallWorkFlow.MEETING -> {
                        session.leaveSession()
                        CallingState.setIsActiveMeeting(false)
                        mutableUiState.value = OngoingCallUIState.Ended
                        viewModelScope.launch {
                            mutableEvents.emit(OngoingCallEvent.CallEnded)
                        }
                    }
                }
            }

            override fun onToggleAudioButtonClicked() {
                // No action required per design
            }

            override fun onToggleVideoButtonClicked() {
                // No action required per design
            }

            override fun onSwitchCameraButtonClicked() {
                // No action required per design
            }

            override fun onRaiseHandButtonClicked() {
                // No action required per design
            }

            override fun onRecordingToggleButtonClicked() {
                // No action required per design
            }
        })
    }

    /**
     * No-op in v5 — lifecycle-aware listeners auto-remove on destroy.
     * Kept for API compatibility.
     *
     * Validates: Requirement 13.5
     */
    fun removeListeners() {
        // v5 listeners are lifecycle-aware and auto-cleanup — no manual removal needed
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
