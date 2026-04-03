package com.cometchat.uikit.kotlin.presentation.ongoingcall.ui

import android.app.PictureInPictureParams
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Rational
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.cometchat.calls.core.CometChatCalls
import com.cometchat.chat.core.CometChat
import com.cometchat.uikit.core.CometChatUIKit
import com.cometchat.uikit.core.constants.UIKitConstants.CallWorkFlow
import com.cometchat.uikit.core.models.OngoingCallEvent
import com.cometchat.uikit.core.models.OngoingCallUIState
import com.cometchat.uikit.core.utils.CallingState
import com.cometchat.uikit.kotlin.presentation.ongoingcall.style.CometChatOngoingCallStyle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * CometChatOngoingCallActivity is an Activity that hosts the CometChatOngoingCall component.
 *
 * This activity:
 * - Displays the ongoing call interface with proper screen management
 * - Supports Picture-in-Picture (PIP) mode for multitasking during calls
 * - Shows when locked and turns screen on for incoming calls
 * - Handles call lifecycle and cleanup
 *
 * **Validates: Requirements 8.1, 8.2, 8.3, 8.4, 9.1, 9.2, 9.3, 9.4, 9.5, 10.1, 10.2, 10.3, 10.4, 11.1, 11.2, 12.1, 12.2, 12.3, 12.4**
 *
 * Usage:
 * ```kotlin
 * CometChatOngoingCallActivity.launchOngoingCallActivity(
 *     context = context,
 *     sessionId = "session-id",
 *     callType = "video",
 *     callSettingsBuilder = builder
 * )
 * ```
 */
class CometChatOngoingCallActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "CometChatOngoingCallActivity"
        
        // Intent extras
        private const val EXTRA_SESSION_ID = "extra_session_id"
        private const val EXTRA_CALL_TYPE = "extra_call_type"
        private const val EXTRA_CALL_WORKFLOW = "extra_call_workflow"
        
        // Static storage for CallSettingsBuilder (cannot be passed via Intent)
        // **Validates: Requirement 12.4**
        @Volatile
        private var onGoingCallSettingsBuilder: CometChatCalls.CallSettingsBuilder? = null
        
        // Static storage for style
        @Volatile
        private var ongoingCallStyle: CometChatOngoingCallStyle? = null

        /**
         * Launches the ongoing call activity.
         *
         * **Validates: Requirements 12.1, 12.2, 12.3, 12.4**
         *
         * @param context The context to start the activity from
         * @param sessionId The unique identifier for the call session
         * @param callType The type of call ("audio" or "video")
         * @param callWorkFlow The workflow type (DEFAULT for 1:1, MEETING for group)
         * @param callSettingsBuilder Optional call settings builder for custom configuration
         * @param style Optional style configuration for the ongoing call screen
         */
        @JvmStatic
        fun launchOngoingCallActivity(
            context: Context,
            sessionId: String,
            callType: String,
            callWorkFlow: CallWorkFlow = CallWorkFlow.DEFAULT,
            callSettingsBuilder: CometChatCalls.CallSettingsBuilder? = null,
            style: CometChatOngoingCallStyle? = null
        ) {
            // Store CallSettingsBuilder in static variable (Requirement 12.4)
            onGoingCallSettingsBuilder = callSettingsBuilder
            ongoingCallStyle = style
            
            val intent = Intent(context, CometChatOngoingCallActivity::class.java).apply {
                putExtra(EXTRA_SESSION_ID, sessionId)
                putExtra(EXTRA_CALL_TYPE, callType)
                putExtra(EXTRA_CALL_WORKFLOW, callWorkFlow.name)
                // Add FLAG_ACTIVITY_NEW_TASK (Requirement 12.3)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }
    }

    // ==================== Views ====================
    private lateinit var container: FrameLayout
    private var ongoingCallView: CometChatOngoingCall? = null

    // ==================== State ====================
    private var sessionId: String? = null
    private var callType: String? = null
    private var callWorkFlow: CallWorkFlow = CallWorkFlow.DEFAULT
    private var isInPipMode: Boolean = false
    
    // ==================== Coroutine Scope ====================
    private val activityScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var eventsJob: Job? = null

    // ==================== Lifecycle Methods ====================

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Set screen management flags (Requirements 8.1, 8.2, 8.3)
        setupScreenManagement()
        
        // Extract intent extras
        sessionId = intent.getStringExtra(EXTRA_SESSION_ID)
        callType = intent.getStringExtra(EXTRA_CALL_TYPE)
        callWorkFlow = intent.getStringExtra(EXTRA_CALL_WORKFLOW)?.let {
            try { CallWorkFlow.valueOf(it) } catch (e: Exception) { CallWorkFlow.DEFAULT }
        } ?: CallWorkFlow.DEFAULT
        
        // Create container layout
        container = FrameLayout(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }
        setContentView(container)
        
        // Apply window insets (Requirement 8.4)
        applyWindowInsets()
        
        // Setup ongoing call view
        setupOngoingCall()
        
        // Handle back press to enter PIP mode (Requirement 9.1)
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                enterPipMode()
            }
        })
    }

    /**
     * Sets up screen management for showing when locked and turning screen on.
     *
     * **Validates: Requirements 8.1, 8.2, 8.3**
     */
    private fun setupScreenManagement() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            // API 27+ (Requirement 8.1, 8.2)
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            // API < 27 (Requirement 8.3)
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
            )
        }
    }

    /**
     * Applies window insets for proper system bar handling.
     *
     * **Validates: Requirement 8.4**
     */
    private fun applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(container) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    /**
     * Sets up the CometChatOngoingCall view.
     *
     * **Validates: Requirements 1.1, 1.2, 2.1, 2.2, 3.1**
     */
    private fun setupOngoingCall() {
        val sid = sessionId ?: return
        val type = callType ?: return
        // Use provided builder, or UIKit's configured builder, or create default
        val builder = onGoingCallSettingsBuilder 
            ?: CometChatUIKit.getCallSettingsBuilder() 
            ?: CometChatCalls.CallSettingsBuilder(this)

        ongoingCallView = CometChatOngoingCall(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            
            // Apply style if provided
            ongoingCallStyle?.let { setStyle(it) }
            
            // Configure the call
            setSessionId(sid)
            setCallType(type)
            setCallWorkFlow(callWorkFlow)
            setCallSettingsBuilder(builder)
            
            // Set error callback
            setOnError { exception ->
                android.util.Log.e(TAG, "Ongoing call error: ${exception.message}")
            }
        }

        container.addView(ongoingCallView)
        
        // Observe events for call ended and user joined
        observeEvents()
        
        // Start the call
        ongoingCallView?.startCall()
    }

    /**
     * Observes ViewModel events for call lifecycle handling.
     */
    private fun observeEvents() {
        val viewModel = ongoingCallView?.getViewModel() ?: return
        
        eventsJob = activityScope.launch {
            viewModel.events.collect { event ->
                when (event) {
                    is OngoingCallEvent.CallEnded -> {
                        finish()
                    }
                    is OngoingCallEvent.SessionTimeout -> {
                        finish()
                    }
                    is OngoingCallEvent.UserJoined -> {
                        // Handle PIP mode entry for current user (Requirement 18.1)
                        if (event.isCurrentUser && isInPipMode) {
                            CometChatCalls.enterPIPMode()
                        }
                    }
                    is OngoingCallEvent.UserLeft -> {
                        // No action needed
                    }
                    is OngoingCallEvent.Error -> {
                        // Error handled by onError callback
                    }
                }
            }
        }
        
        // Also observe UI state for Ended state
        activityScope.launch {
            viewModel.uiState.collect { state ->
                if (state is OngoingCallUIState.Ended) {
                    finish()
                }
            }
        }
    }

    // ==================== PIP Mode Methods ====================

    /**
     * Enters Picture-in-Picture mode.
     *
     * **Validates: Requirements 9.1, 9.2, 9.5**
     */
    private fun enterPipMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val metrics = resources.displayMetrics
            // Use screen aspect ratio for PIP dimensions (Requirement 9.5)
            enterPictureInPictureMode(
                PictureInPictureParams.Builder()
                    .setAspectRatio(Rational(metrics.widthPixels, metrics.heightPixels))
                    .build()
            )
        }
    }

    /**
     * Called when the user leaves the activity without explicitly finishing it.
     * Enters PIP mode.
     *
     * **Validates: Requirement 9.2**
     */
    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        enterPipMode()
    }

    /**
     * Called when PIP mode changes.
     * Calls CometChatCalls.enterPIPMode/exitPIPMode accordingly.
     *
     * **Validates: Requirements 9.3, 9.4**
     */
    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode)
        isInPipMode = isInPictureInPictureMode
        
        if (isInPictureInPictureMode) {
            // Entering PIP mode (Requirement 9.3)
            CometChatCalls.enterPIPMode()
        } else {
            // Exiting PIP mode (Requirement 9.4)
            CometChatCalls.exitPIPMode()
        }
    }

    /**
     * Called when the activity is stopped.
     * Handles PIP exit if in PIP mode.
     *
     * **Validates: Requirements 10.1, 10.2, 10.3, 10.4**
     * 
     * IMPORTANT: We must check isInPictureInPictureMode directly (not the cached isInPipMode variable)
     * because when user dismisses PIP, onPictureInPictureModeChanged(false) is called BEFORE onStop(),
     * which sets isInPipMode to false. But isInPictureInPictureMode() still returns true during onStop().
     * This matches the Java v5 implementation behavior.
     */
    override fun onStop() {
        super.onStop()
        
        // Handle PIP exit when in PIP mode (Requirement 10.1)
        // Use isInPictureInPictureMode directly, not the cached variable
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && isInPictureInPictureMode) {
            handlePiPExit()
        }
    }

    /**
     * Handles PIP exit by ending the call appropriately based on workflow.
     *
     * **Validates: Requirements 10.2, 10.3, 10.4**
     */
    private fun handlePiPExit() {
        // Use the view's handlePiPExit method which handles workflow-dependent cleanup
        ongoingCallView?.handlePiPExit(callWorkFlow)
        
        // Finish activity after cleanup (Requirement 10.4)
        finish()
    }

    /**
     * Called when the activity is destroyed.
     * Clears static builder and active call.
     *
     * **Validates: Requirements 11.1, 11.2**
     */
    override fun onDestroy() {
        super.onDestroy()
        
        // Cancel coroutine scope
        eventsJob?.cancel()
        activityScope.cancel()
        
        // Clear static builder (Requirement 11.1)
        onGoingCallSettingsBuilder = null
        ongoingCallStyle = null
        
        // Clear active call (Requirement 11.2)
        CallingState.setActiveCall(null)
        CometChat.clearActiveCall()
    }
}
