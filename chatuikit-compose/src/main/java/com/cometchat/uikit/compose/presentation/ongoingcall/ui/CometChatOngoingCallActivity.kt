package com.cometchat.uikit.compose.presentation.ongoingcall.ui

import android.Manifest
import android.app.PictureInPictureParams
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Rational
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cometchat.calls.core.CometChatCalls
import com.cometchat.chat.core.CometChat
import com.cometchat.uikit.compose.presentation.ongoingcall.style.CometChatOngoingCallStyle
import com.cometchat.uikit.core.CometChatUIKit
import com.cometchat.uikit.core.constants.UIKitConstants.CallWorkFlow
import com.cometchat.uikit.core.models.OngoingCallEvent
import com.cometchat.uikit.core.models.OngoingCallUIState
import com.cometchat.uikit.core.utils.CallingState
import com.cometchat.uikit.core.viewmodel.CometChatOngoingCallViewModel

/**
 * CometChatOngoingCallActivity is a Compose Activity that hosts the CometChatOngoingCall composable.
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
class CometChatOngoingCallActivity : ComponentActivity() {

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

    // ==================== State ====================
    private var sessionId: String? = null
    private var callType: String? = null
    private var callWorkFlow: CallWorkFlow = CallWorkFlow.DEFAULT
    private var isInPipMode: Boolean = false
    
    // Track if we're transitioning to PIP (to avoid premature exit handling)
    private var isEnteringPipMode: Boolean = false
    
    // ==================== ViewModel Reference ====================
    private var ongoingCallViewModel: CometChatOngoingCallViewModel? = null

    // ==================== Lifecycle Methods ====================

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set screen management flags (Requirements 8.1, 8.2, 8.3)
        setupScreenManagement()
        
        // Request necessary permissions for calls (matching Java implementation)
        requestCallPermissions()

        // Extract intent extras
        sessionId = intent.getStringExtra(EXTRA_SESSION_ID)
        callType = intent.getStringExtra(EXTRA_CALL_TYPE)
        callWorkFlow = intent.getStringExtra(EXTRA_CALL_WORKFLOW)?.let {
            try { CallWorkFlow.valueOf(it) } catch (e: Exception) { CallWorkFlow.DEFAULT }
        } ?: CallWorkFlow.DEFAULT

        // Apply window insets for edge-to-edge (Requirement 8.4)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Set Compose content
        setContent {
            val sid = sessionId
            val type = callType
            // Use provided builder, or UIKit's configured builder, or create default
            val builder = onGoingCallSettingsBuilder 
                ?: CometChatUIKit.getCallSettingsBuilder() 
                ?: CometChatCalls.CallSettingsBuilder(this)

            if (sid != null && type != null) {
                val viewModel: CometChatOngoingCallViewModel = viewModel()
                val style = ongoingCallStyle ?: CometChatOngoingCallStyle.default()
                
                // Store ViewModel reference for PIP handling
                ongoingCallViewModel = viewModel

                // Observe events for call ended and user joined
                LaunchedEffect(Unit) {
                    viewModel.events.collect { event ->
                        when (event) {
                            is OngoingCallEvent.CallEnded,
                            is OngoingCallEvent.SessionTimeout -> {
                                finish()
                            }
                            is OngoingCallEvent.UserJoined -> {
                                // Handle PIP mode entry for current user (Requirement 18.1)
                                if (event.isCurrentUser && isInPipMode) {
                                    CometChatCalls.enterPIPMode()
                                }
                            }
                            else -> { /* handled by composable */ }
                        }
                    }
                }

                // Observe UI state for Ended state
                LaunchedEffect(Unit) {
                    viewModel.uiState.collect { state ->
                        if (state is OngoingCallUIState.Ended) {
                            finish()
                        }
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    CometChatOngoingCall(
                        sessionId = sid,
                        callType = type,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        viewModel = viewModel,
                        callWorkFlow = callWorkFlow,
                        callSettingsBuilder = builder,
                        style = style,
                        onCallEnded = { finish() },
                        onError = { /* Error handled by composable */ }
                    )
                }
            }
        }
        
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
     * Requests RECORD_AUDIO and CAMERA permissions required for calls.
     * Matches the Java implementation behavior.
     */
    private fun requestCallPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val permissions = arrayOf(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CAMERA
            )
            requestPermissions(permissions, 101)
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
        // Set flag to indicate we're entering PIP mode (not exiting)
        isEnteringPipMode = true
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
        
        val wasInPipMode = isInPipMode
        isInPipMode = isInPictureInPictureMode

        if (isInPictureInPictureMode) {
            // Entering PIP mode (Requirement 9.3)
            CometChatCalls.enterPIPMode()
            // Reset the entering flag after we've entered
            isEnteringPipMode = false
        } else {
            // Exiting PIP mode (Requirement 9.4)
            CometChatCalls.exitPIPMode()
            
            // Only call handlePiPExit if the activity is finishing (user dismissed PIP)
            // If the activity is NOT finishing, user clicked PIP to return to full screen
            if (wasInPipMode && isFinishing) {
                handlePiPExit()
            }
        }
    }

    /**
     * Called when the activity is stopped.
     * 
     * IMPORTANT: We should NOT call handlePiPExit() here when entering PIP mode.
     * The onStop() is called both when:
     * 1. Entering PIP mode (activity goes to background) - call should continue
     * 2. Activity is being destroyed - cleanup is handled in onDestroy or onPictureInPictureModeChanged
     * 
     * The handlePiPExit() is now called from onPictureInPictureModeChanged() when
     * the user dismisses the PIP window (isInPictureInPictureMode changes from true to false).
     *
     * **Validates: Requirements 10.1, 10.2, 10.3, 10.4**
     */
    override fun onStop() {
        super.onStop()
        // No action needed - PIP exit handled in onPictureInPictureModeChanged
    }

    /**
     * Handles PIP exit by ending the call appropriately based on workflow.
     *
     * **Validates: Requirements 10.2, 10.3, 10.4**
     */
    private fun handlePiPExit() {
        // Remove listeners
        ongoingCallViewModel?.removeListeners()

        // Workflow-dependent cleanup (Requirements 10.2, 10.3)
        if (callWorkFlow != CallWorkFlow.MEETING) {
            // DEFAULT workflow: end call and clear active call
            ongoingCallViewModel?.endCall()
            CometChat.clearActiveCall()
        } else {
            // MEETING workflow: end session only
            CometChatCalls.endSession()
        }

        CallingState.setIsActiveMeeting(false)

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

        // Clear static builder (Requirement 11.1)
        onGoingCallSettingsBuilder = null
        ongoingCallStyle = null

        // Clear active call (Requirement 11.2)
        CallingState.setActiveCall(null)
        CometChat.clearActiveCall()
    }
}
