package com.cometchat.uikit.compose.calls

import android.app.PictureInPictureParams
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Rational
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.cometchat.calls.core.CometChatCalls
import com.cometchat.chat.core.Call
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.CustomMessage
import com.cometchat.chat.models.User
import com.cometchat.uikit.compose.presentation.incomingcall.style.CometChatIncomingCallStyle
import com.cometchat.uikit.compose.presentation.incomingcall.ui.CometChatIncomingCall
import com.cometchat.uikit.compose.presentation.ongoingcall.ui.CometChatOngoingCallActivity
import com.cometchat.uikit.compose.presentation.outgoingcall.style.CometChatOutgoingCallStyle
import com.cometchat.uikit.compose.presentation.outgoingcall.ui.CometChatOutgoingCall
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.uikit.core.constants.UIKitConstants

/**
 * CometChatCallActivity is a ComponentActivity that hosts call screens (outgoing call, ongoing call).
 * 
 * This activity:
 * - Displays CometChatOutgoingCall for outgoing calls
 * - Handles Picture-in-Picture mode for ongoing calls
 * - Manages call lifecycle and cleanup
 * 
 * Usage:
 * ```kotlin
 * CometChatCallActivity.launchOutgoingCallScreen(context, call, null)
 * ```
 */
class CometChatCallActivity : ComponentActivity() {

    companion object {
        private const val TAG = "CometChatCallActivity"
        private const val OUTGOING_CALL = "outgoing_call"
        private const val INCOMING_CALL = "incoming_call"
        private const val DIRECT_CALL = "direct_call"

        private var baseMessage: BaseMessage? = null
        private var call: Call? = null
        private var user: User? = null
        private var outgoingCallStyle: CometChatOutgoingCallStyle? = null
        private var incomingCallStyle: CometChatIncomingCallStyle? = null
        private var onGoingCallSettingsBuilder: CometChatCalls.CallSettingsBuilder? = null
        private var callingType: String? = null
        
        // Flag to prevent double launches
        @Volatile
        private var isLaunching: Boolean = false

        /**
         * Launches the outgoing call screen.
         * 
         * @param context The context to start the activity from
         * @param call The Call object representing the outgoing call
         * @param style Optional style configuration for the outgoing call screen
         */
        @JvmStatic
        @Synchronized
        fun launchOutgoingCallScreen(
            context: Context,
            call: Call,
            style: CometChatOutgoingCallStyle? = null
        ) {
            // Prevent double launches
            if (isLaunching) {
                return
            }
            isLaunching = true
            callingType = OUTGOING_CALL
            outgoingCallStyle = style
            Companion.call = call
            baseMessage = null
            if (call.receiverType == "user" && call.receiver != null) {
                user = call.receiver as? User
            }
            startActivity(context)
        }

        /**
         * Launches the incoming call screen.
         * 
         * @param context The context to start the activity from
         * @param call The Call object representing the incoming call
         * @param style Optional style configuration for the incoming call screen
         */
        @JvmStatic
        fun launchIncomingCallScreen(
            context: Context,
            call: Call,
            style: CometChatIncomingCallStyle? = null
        ) {
            callingType = INCOMING_CALL
            incomingCallStyle = style
            Companion.call = call
            baseMessage = null
            if (call.callInitiator != null) {
                user = call.callInitiator as? User
            }
            startActivity(context)
        }

        /**
         * Launches the conference call screen.
         * 
         * @param context The context to start the activity from
         * @param baseMessage The BaseMessage containing call information
         * @param callSettingsBuilder Optional call settings builder
         */
        @JvmStatic
        fun launchConferenceCallScreen(
            context: Context,
            baseMessage: BaseMessage,
            callSettingsBuilder: CometChatCalls.CallSettingsBuilder? = null
        ) {
            callingType = DIRECT_CALL
            Companion.baseMessage = baseMessage
            onGoingCallSettingsBuilder = callSettingsBuilder
            startActivity(context)
        }

        private fun startActivity(context: Context) {
            val intent = Intent(context, CometChatCallActivity::class.java)
            // Use SINGLE_TOP to prevent multiple instances and NEW_TASK for non-Activity contexts
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Reset the launching flag now that activity is created
        isLaunching = false
        
        // Guard: If callingType is null, the static data was cleared (activity recreated after finish)
        // Just finish immediately to prevent blank screen
        if (callingType == null) {
            finish()
            return
        }
        
        // Handle direct call (group conference) - launch ongoing call activity directly
        // and finish this activity to prevent blank screen when call ends
        if (callingType == DIRECT_CALL) {
            handleDirectCall()
            return
        }
        
        setContent {
            CometChatTheme {
                CallActivityContent(
                    callingType = callingType,
                    call = call,
                    baseMessage = baseMessage,
                    callSettingsBuilder = onGoingCallSettingsBuilder,
                    outgoingStyle = outgoingCallStyle,
                    incomingStyle = incomingCallStyle,
                    onBackPress = { finish() }
                )
            }
        }
    }
    
    /**
     * Handles direct call (group conference) by launching CometChatOngoingCallActivity
     * and finishing this activity to prevent blank screen when call ends.
     */
    private fun handleDirectCall() {
        val message = baseMessage as? CustomMessage
        
        if (message != null && message.type == UIKitConstants.MessageType.MEETING) {
            val sessionId = message.customData?.optString(UIKitConstants.CallingJSONConstants.CALL_SESSION_ID)
            val callType = message.customData?.optString(UIKitConstants.CallingJSONConstants.CALL_TYPE)
            
            if (!sessionId.isNullOrEmpty() && !callType.isNullOrEmpty()) {
                CometChatOngoingCallActivity.launchOngoingCallActivity(
                    context = this,
                    sessionId = sessionId,
                    callType = callType,
                    callWorkFlow = UIKitConstants.CallWorkFlow.MEETING,
                    callSettingsBuilder = onGoingCallSettingsBuilder
                )
                
                // Finish this activity so it's not in the back stack
                finish()
                // Disable transition animation to prevent blank screen flash
                @Suppress("DEPRECATION")
                overridePendingTransition(0, 0)
                return
            }
        }
        
        // If we couldn't launch the ongoing call, finish this activity
        finish()
    }

    private fun startPictureInPictureMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val metrics = resources.displayMetrics
            enterPictureInPictureMode(
                PictureInPictureParams.Builder()
                    .setAspectRatio(Rational(metrics.widthPixels, metrics.heightPixels))
                    .build()
            )
        }
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode)
        if (isInPictureInPictureMode) {
            CometChatCalls.enterPIPMode()
        } else {
            CometChatCalls.exitPIPMode()
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        startPictureInPictureMode()
    }

    override fun onDestroy() {
        super.onDestroy()
        
        // Store callingType before clearing to check if we should end session
        val wasDirectCall = callingType == DIRECT_CALL
        
        // IMPORTANT: Do NOT call endSession() for DIRECT_CALL!
        // For DIRECT_CALL, we launch CometChatOngoingCallActivity and finish this activity.
        // The ongoing call activity manages the session. If we call endSession() here,
        // it will end the session that was just started by CometChatOngoingCallActivity,
        // causing the PIP window to disappear.
        // 
        // Also do NOT call endSession() if callingType is null - this means the activity
        // was recreated from the back stack after we already cleared the static data.
        // Calling endSession() in this case would kill an ongoing call.
        // 
        // Only call endSession() for OUTGOING_CALL and INCOMING_CALL where this activity
        // hosts the call UI directly.
        if (callingType != null && !wasDirectCall) {
            CometChatCalls.endSession()
            CometChat.clearActiveCall()
        }
        
        // Reset the launching flag to allow new calls
        isLaunching = false
        
        // Clear static references
        baseMessage = null
        call = null
        callingType = null
        user = null
        onGoingCallSettingsBuilder = null
        outgoingCallStyle = null
        incomingCallStyle = null
    }
}

@Composable
private fun CallActivityContent(
    callingType: String?,
    call: Call?,
    baseMessage: BaseMessage?,
    callSettingsBuilder: CometChatCalls.CallSettingsBuilder?,
    outgoingStyle: CometChatOutgoingCallStyle?,
    incomingStyle: CometChatIncomingCallStyle?,
    onBackPress: () -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        when (callingType) {
            "outgoing_call" -> {
                call?.let { currentCall ->
                    CometChatOutgoingCall(
                        call = currentCall,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        style = outgoingStyle ?: CometChatOutgoingCallStyle.default(),
                        onBackPress = onBackPress
                    )
                }
            }
            "incoming_call" -> {
                call?.let { currentCall ->
                    CometChatIncomingCall(
                        call = currentCall,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        style = incomingStyle ?: CometChatIncomingCallStyle.default(),
                        onRejectClick = { _ ->
                            onBackPress()
                        },
                        onAcceptClick = { _ ->
                            // Call accepted - the component handles starting the call
                        },
                        onError = { _ ->
                            onBackPress()
                        }
                    )
                }
            }
            // Note: "direct_call" case is handled in onCreate before setContent
        }
    }
}
