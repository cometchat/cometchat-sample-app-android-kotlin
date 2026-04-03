package com.cometchat.uikit.kotlin.calls

import android.app.PictureInPictureParams
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Rational
import android.view.View
import android.widget.FrameLayout
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.cometchat.calls.core.CometChatCalls
import com.cometchat.chat.core.Call
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.CustomMessage
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.kotlin.presentation.incomingcall.CometChatIncomingCall
import com.cometchat.uikit.kotlin.presentation.incomingcall.style.CometChatIncomingCallStyle
import com.cometchat.uikit.kotlin.presentation.ongoingcall.ui.CometChatOngoingCallActivity
import com.cometchat.uikit.kotlin.presentation.outgoingcall.CometChatOutgoingCall
import com.cometchat.uikit.kotlin.presentation.outgoingcall.style.CometChatOutgoingCallStyle
import com.cometchat.uikit.kotlin.shared.resources.utils.Utils

/**
 * CometChatCallActivity is an Activity that hosts call screens (outgoing call, ongoing call).
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
class CometChatCallActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "CometChatCallActivity"
        private const val OUTGOING_CALL = "outgoing_call"
        private const val INCOMING_CALL = "incoming_call"
        private const val DIRECT_CALL = "direct_call"
        private const val STORE_INSTANCE = "store_instance"

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

    private lateinit var callScreenContainer: FrameLayout
    private var outgoingCallView: CometChatOutgoingCall? = null
    private var incomingCallView: CometChatIncomingCall? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Reset the launching flag now that activity is created
        isLaunching = false
        
        // Handle direct call (group conference) - launch ongoing call activity directly
        // and finish this activity to prevent blank screen when call ends
        if (callingType == DIRECT_CALL) {
            handleDirectCall()
            return
        }
        
        // Create container layout
        callScreenContainer = FrameLayout(this).apply {
            id = View.generateViewId()
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }
        setContentView(callScreenContainer)
        
        applyWindowInsets()

        // Restore state if needed
        if (savedInstanceState != null && savedInstanceState.containsKey(STORE_INSTANCE)) {
            // State is preserved in companion object
        }

        when (callingType) {
            OUTGOING_CALL -> setupOutgoingCall()
            INCOMING_CALL -> setupIncomingCall()
        }

        // Handle back press
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                startPictureInPictureMode()
            }
        })
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
            }
        }
        // Finish this activity so it's not in the back stack
        finish()
    }

    private fun applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(callScreenContainer) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupOutgoingCall() {
        val currentCall = call ?: return
        val currentUser = user

        outgoingCallView = CometChatOutgoingCall(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            
            // Apply style if provided
            outgoingCallStyle?.let { setStyle(it) }
            
            // Set call and user
            setCall(currentCall)
            currentUser?.let { setUser(it) }
            
            // Set back press handler
            setOnBackPressListener {
                finish()
            }
        }

        callScreenContainer.addView(outgoingCallView)
    }

    private fun setupIncomingCall() {
        val currentCall = call ?: return

        incomingCallView = CometChatIncomingCall(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            
            // Apply style if provided
            incomingCallStyle?.let { setStyle(it) }
            
            // Set call
            setCall(currentCall)
            
            // Set callbacks using OnClick functional interface
            setOnRejectClickListener(com.cometchat.uikit.kotlin.shared.interfaces.OnClick {
                finish()
            })
            
            setOnAcceptClickListener(com.cometchat.uikit.kotlin.shared.interfaces.OnClick {
                // Call accepted - the component handles starting the call
                // Activity will be finished when call ends
            })
            
            setOnError { exception ->
                android.util.Log.e(TAG, "Incoming call error: ${exception.message}")
                finish()
            }
        }

        callScreenContainer.addView(incomingCallView)
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(STORE_INSTANCE, true)
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
        CometChatCalls.endSession()
        CometChat.clearActiveCall()
        
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
