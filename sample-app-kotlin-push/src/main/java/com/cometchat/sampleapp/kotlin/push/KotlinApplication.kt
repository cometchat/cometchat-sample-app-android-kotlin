package com.cometchat.sampleapp.kotlin.push

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import com.cometchat.calls.core.CometChatCalls
import com.cometchat.calls.core.CallAppSettings
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.Call
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.helpers.Logger
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.User
import com.cometchat.pushnotification.CometChatPushNotifications
import com.cometchat.pushnotification.PNConfiguration
import com.cometchat.pushnotification.listeners.CallAnsweredHandler
import com.cometchat.pushnotification.listeners.NotificationTapListener
import com.cometchat.pushnotification.models.PNCallInfo
import com.cometchat.sampleapp.kotlin.push.appflow.MessagesActivity
import com.cometchat.sampleapp.kotlin.push.fcm.AppFCMService
import com.google.firebase.FirebaseApp
import com.google.gson.Gson
import com.cometchat.uikit.core.CometChatUIKit
import com.cometchat.uikit.core.UIKitSettings
import com.cometchat.uikit.core.events.CometChatCallEvent
import com.cometchat.uikit.core.events.CometChatEvents
import com.cometchat.uikit.core.resources.soundmanager.CometChatSoundManager
import com.cometchat.uikit.core.resources.soundmanager.Sound
import com.cometchat.uikit.core.utils.CallManager
import com.cometchat.uikit.kotlin.presentation.incomingcall.CometChatIncomingCall
import com.cometchat.uikit.kotlin.presentation.ongoingcall.ui.CometChatOngoingCallActivity
import com.cometchat.uikit.kotlin.shared.resources.utils.Utils
import com.cometchat.sampleapp.kotlin.push.shared.AppPreferences
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Application class for managing call notifications and handling CometChat events.
 * Uses a Snackbar to display incoming call UI at the top of the screen.
 *
 * SDK initialization is handled by SplashActivity/AppCredentialsActivity.
 * Call listeners are registered via onSDKInitialized() callback.
 */
class KotlinApplication : Application() {
    private val TAG: String = "Application"
    private var currentActivityInstance: Activity? = null
    private var snackBar: Snackbar? = null
    private var soundManager: CometChatSoundManager? = null
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val isConnectedToWebSockets = AtomicBoolean(false)

    /** Flag to track if SDK has been initialized and listeners registered */
    private var isSDKInitialized = false

    companion object {
        private val LISTENER_ID = "AppCallListener_${System.currentTimeMillis()}"

        private var isAppInForeground: Boolean = false
        var currentActivity: Activity? = null
        private var tempCall: Call? = null

        fun isAppInForeground(): Boolean = isAppInForeground
    }

    override fun onCreate() {
        super.onCreate()
        soundManager = CometChatSoundManager(this)

        // ── CometChat Push Notifications ──
        // Initialize Firebase and set the push listeners (listeners are credential-independent).
        FirebaseApp.initializeApp(this)
        setupPushNotificationListeners()
        // Ensure the CometChat SDK (and then PN/Calls) is initialized here in Application.onCreate.
        // This is required for KILLED-STATE pushes: FCM starts the process and Application.onCreate
        // runs before AppFCMService.onMessageReceived, but no Splash/UI runs to initialize the SDK.
        ensureSdkInitializedForPush()

        // Register activity lifecycle callbacks to track current activity and foreground state
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            private var isActivityChangingConfigurations = false
            private var activityReferences = 0

            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                currentActivityInstance = activity
                currentActivity = activity
            }
            override fun onActivityStarted(activity: Activity) {
                currentActivityInstance = activity
                currentActivity = activity

                // Connect to WebSocket when app comes to foreground
                if (isConnectedToWebSockets.compareAndSet(false, true)) {
                    CometChat.connect(object : CometChat.CallbackListener<String?>() {
                        override fun onSuccess(s: String?) {
                            isConnectedToWebSockets.set(true)
                        }
                        override fun onError(e: CometChatException) {
                            isConnectedToWebSockets.set(false)
                        }
                    })
                }

                if (++activityReferences == 1 && !isActivityChangingConfigurations) {
                    isAppInForeground = true
                    Log.d(TAG, "App is now in FOREGROUND")
                }
            }
            override fun onActivityResumed(activity: Activity) {
                currentActivityInstance = activity
                currentActivity = activity
                // Re-show snackbar if there's a pending call
                if (snackBar != null && tempCall != null) {
                    showTopSnackBar(tempCall)
                }
            }
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {
                isActivityChangingConfigurations = activity.isChangingConfigurations
                if (--activityReferences == 0 && !isActivityChangingConfigurations) {
                    isAppInForeground = false
                    Log.d(TAG, "App is now in BACKGROUND")

                    // Disconnect WebSocket when app goes to background
                    CometChat.disconnect(object : CometChat.CallbackListener<String?>() {
                        override fun onSuccess(s: String?) {
                            isConnectedToWebSockets.set(false)
                        }
                        override fun onError(e: CometChatException) {}
                    })
                }
            }
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {
                if (currentActivityInstance === activity) {
                    currentActivityInstance = null
                    currentActivity = null
                }
            }
        })
    }

    /**
     * Called by SplashActivity/AppCredentialsActivity after SDK initialization succeeds.
     * Registers call listeners and initializes CometChatCalls SDK.
     *
     * This method is idempotent - it will only register listeners once.
     */
    fun onSDKInitialized() {
        if (!isSDKInitialized) {
            isSDKInitialized = true
            Log.d(TAG, "onSDKInitialized: Registering call listeners")

            // Initialize CometChatCalls SDK
            initCometChatCalls()

            // Add UIKit call events listener
            addCallEventsListener()

            // Initialize Push Notifications now that credentials are available
            initPushNotifications()
        }
    }

    /**
     * Initializes the CometChat Push Notifications SDK using credentials from SharedPreferences.
     * Safe to call multiple times; no-ops when credentials are not yet available (e.g. first launch
     * before AppCredentials are entered). VoIP is enabled to match the in-app Calls setup.
     */
    /**
     * Initializes the CometChat SDK from persisted credentials when it hasn't been initialized yet
     * (e.g. a killed-state push that started the process with no Splash/UI). On success it runs
     * onSDKInitialized(), which initializes Calls and Push Notifications. When the SDK is already
     * initialized (warm start), it goes straight to onSDKInitialized().
     */
    private fun ensureSdkInitializedForPush() {
        if (CometChatUIKit.isSDKInitialized()) {
            onSDKInitialized()
            return
        }
        val appId = AppPreferences.getAppId(this)
        val region = AppPreferences.getRegion(this)
        val authKey = AppPreferences.getAuthKey(this)
        if (appId.isNullOrEmpty() || region.isNullOrEmpty() || authKey.isNullOrEmpty()) {
            Log.d(TAG, "No stored credentials — deferring SDK init to the onboarding flow")
            return
        }
        // NOTE: keep these settings identical to SplashViewModel.initUIKit() — in particular
        // setEnableCalling(true), or the UIKit will hide call buttons when the SDK is initialized here first.
        val settings = UIKitSettings.UIKitSettingsBuilder()
            .setAutoEstablishSocketConnection(false)
            .setAppId(appId)
            .setRegion(region)
            .setAuthKey(authKey)
            .subscribePresenceForAllUsers()
            .setEnableCalling(true)
            .build()
        CometChatUIKit.init(this, settings, object : CometChat.CallbackListener<String>() {
            override fun onSuccess(result: String?) {
                Log.d(TAG, "CometChat SDK initialized in Application (killed-state ready)")
                onSDKInitialized()
            }
            override fun onError(e: CometChatException) {
                Log.e(TAG, "Application SDK init failed: ${e.message}")
            }
        })
    }

    private fun initPushNotifications() {
        // The CometChat SDK must be initialized before the PN SDK (which connects to CometChat).
        // The SDK is initialized in the Splash/AppCredentials flow, which then calls
        // onSDKInitialized() — so this is a no-op on the early Application.onCreate() call.
        if (!CometChatUIKit.isSDKInitialized()) {
            Log.d(TAG, "Skipping Push Notifications init — CometChat SDK not initialized yet")
            return
        }
        val appId = AppPreferences.getAppId(this)
        val region = AppPreferences.getRegion(this)
        if (appId.isNullOrEmpty() || region.isNullOrEmpty()) {
            Log.d(TAG, "Skipping Push Notifications init — credentials not set yet")
            return
        }

        val config = PNConfiguration.Builder(appId, region)
            .setNotificationSmallIcon(R.drawable.ic_cometchat_notification)
            .setVoIPEnabled(true)
            .setInlineReplyEnabled(true)
            .build()
        CometChatPushNotifications.init(this, config)
        Log.d(TAG, "CometChat Push Notifications initialized")
    }

    /**
     * Registers the CallAnsweredHandler (accept call from a push) and the NotificationTapListener
     * (navigate to the tapped conversation). These do not depend on credentials, so they are set
     * once in onCreate().
     */
    private fun setupPushNotificationListeners() {
        // Fires when the user accepts a call from a push notification (VoIP enabled).
        CometChatPushNotifications.setOnCallAnsweredHandler(object : CallAnsweredHandler {
            override fun onCallAnswered(context: Context, callInfo: PNCallInfo) {
                val sessionId = callInfo.sessionId ?: return
                CometChat.acceptCall(sessionId, object : CometChat.CallbackListener<Call>() {
                    override fun onSuccess(call: Call?) {
                        val acceptedCall = call ?: return
                        // Launch the UIKit ongoing-call screen (it also calls CometChat.clearActiveCall()
                        // when the session ends, so no custom clear handling is needed here).
                        CometChatOngoingCallActivity.launchOngoingCallActivity(
                            context,
                            acceptedCall.sessionId,
                            acceptedCall.type
                        )
                        Log.d(TAG, "Launched ongoing call screen from push: ${acceptedCall.sessionId}")
                    }
                    override fun onError(e: CometChatException?) {
                        Log.e(TAG, "acceptCall from push failed: ${e?.message}")
                    }
                })
            }
        })

        // Fires when the user taps a chat notification — open the conversation in MessagesActivity.
        CometChatPushNotifications.setOnNotificationTapListener(object : NotificationTapListener {
            override fun onNotificationTapped(
                context: Context,
                user: User?,
                group: Group?,
                message: BaseMessage?
            ) {
                val intent = Intent(context, MessagesActivity::class.java).apply {
                    user?.let { putExtra(getString(R.string.app_user), it.toJson().toString()) }
                    group?.let { putExtra(getString(R.string.app_group), Gson().toJson(it)) }
                    message?.let { putExtra(getString(R.string.app_go_to_message), it.id.toString()) }
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            }
        })
    }
    
    /**
     * Initializes the CometChatCalls SDK using credentials from SharedPreferences.
     * Call listeners are registered after successful initialization.
     */
    private fun initCometChatCalls() {
        val appId = AppPreferences.getAppId(this)
        val region = AppPreferences.getRegion(this)

        if (appId.isNullOrEmpty() || region.isNullOrEmpty()) {
            Log.e(TAG, "Cannot initialize CometChatCalls: missing credentials")
            return
        }

        val callAppSettings = CallAppSettings.CallAppSettingBuilder()
            .setAppId(appId)
            .setRegion(region)
            .build()
        
        CometChatCalls.init(this, callAppSettings, object : CometChatCalls.CallbackListener<String>() {
            override fun onSuccess(p0: String?) {
                Log.d(TAG, "CometChatCalls init onSuccess: $p0")
                addCallListener()
            }

            override fun onError(p0: com.cometchat.calls.exceptions.CometChatException?) {
                Log.e(TAG, "CometChatCalls init onError: ${p0?.message}")
            }
        })
    }
    
    /**
     * Adds a call listener to handle incoming calls via WebSocket (in-app, foreground).
     */
    private fun addCallListener() {
        CometChat.addCallListener(LISTENER_ID, object : CometChat.CallListener() {
            override fun onIncomingCallReceived(call: Call) {
                Log.d(TAG, "onIncomingCallReceived: ${call.sessionId}")
                playSound()
                launchIncomingCallPopup(call)
            }

            override fun onOutgoingCallAccepted(call: Call) {
                Log.d(TAG, "onOutgoingCallAccepted: ${call.sessionId}")
                dismissTopSnackBar()
            }

            override fun onOutgoingCallRejected(call: Call) {
                Log.d(TAG, "onOutgoingCallRejected: ${call.sessionId}")
                dismissTopSnackBar()
            }

            override fun onIncomingCallCancelled(call: Call) {
                Log.d(TAG, "onIncomingCallCancelled: ${call.sessionId}")
                dismissTopSnackBar()
            }

            override fun onCallEndedMessageReceived(call: Call) {
                Log.d(TAG, "onCallEndedMessageReceived: ${call.sessionId}")
                dismissTopSnackBar()
            }
        })
    }
    
    /**
     * Adds UIKit call events listener to handle call accepted/rejected from UI.
     */
    private fun addCallEventsListener() {
        applicationScope.launch {
            CometChatEvents.callEvents.collect { event ->
                when (event) {
                    is CometChatCallEvent.CallAccepted -> {
                        Log.d(TAG, "CallAccepted event received")
                        dismissTopSnackBar()
                    }
                    is CometChatCallEvent.CallRejected -> {
                        Log.d(TAG, "CallRejected event received")
                        dismissTopSnackBar()
                    }
                    else -> {}
                }
            }
        }
    }
    
    /**
     * Launches an incoming call popup when an incoming call is received.
     */
    private fun launchIncomingCallPopup(call: Call) {
        val callInitiator = call.callInitiator
        if (callInitiator is User) {
            val loggedInUser = CometChatUIKit.getLoggedInUser()
            if (loggedInUser != null && loggedInUser.uid.equals(callInitiator.uid, ignoreCase = true)) {
                return
            }
        }
        
        if (CometChat.getActiveCall() == null && CallManager.getActiveCall() == null) {
            CallManager.setActiveCall(call)
            showTopSnackBar(call)
        } else {
            rejectCallWithBusyStatus(call)
        }
    }
    
    /**
     * Displays a custom SnackBar notification at the top of the screen for incoming calls.
     */
    @SuppressLint("RestrictedApi")
    private fun showTopSnackBar(call: Call?) {
        if (currentActivityInstance == null || call == null) return
        tempCall = call
        
        if (snackBar != null && snackBar!!.isShown) {
            snackBar?.dismiss()
        }

        val rootView: View = currentActivityInstance!!.findViewById(android.R.id.content)
        
        val cometChatIncomingCall = CometChatIncomingCall(currentActivityInstance!!).apply {
            setDisableSoundForCalls(true)
            setCall(call)
            fitsSystemWindows = true
            setOnError { _ -> dismissTopSnackBar() }
        }
        
        snackBar = Snackbar.make(rootView, " ", Snackbar.LENGTH_INDEFINITE)
        val layout = snackBar?.view as? Snackbar.SnackbarLayout
        layout?.let {
            val params = it.layoutParams as FrameLayout.LayoutParams
            params.gravity = Gravity.TOP
            params.topMargin = Utils.convertDpToPx(this, 35)
            it.layoutParams = params
            it.setBackgroundColor(android.graphics.Color.TRANSPARENT)
            it.addView(cometChatIncomingCall, 0)
        }
        
        snackBar?.show()
    }
    
    /**
     * Dismisses the current top SnackBar if it's being shown.
     */
    private fun dismissTopSnackBar() {
        if (snackBar != null && snackBar!!.isShown) {
            snackBar?.dismiss()
            snackBar = null
            tempCall = null
            CallManager.setActiveCall(null)
        }
        pauseSound()
    }
    
    /**
     * Rejects an incoming call with busy status when already in a call.
     */
    private fun rejectCallWithBusyStatus(call: Call) {
        Thread {
            try {
                Thread.sleep(2000)
            } catch (e: InterruptedException) {
                Log.e(TAG, "Sleep interrupted: ${e.message}")
            }
            CometChat.rejectCall(
                call.sessionId,
                CometChatConstants.CALL_STATUS_BUSY,
                object : CometChat.CallbackListener<Call>() {
                    override fun onSuccess(rejectedCall: Call) {
                        Log.d(TAG, "Call rejected with busy status")
                    }

                    override fun onError(e: CometChatException) {
                        Log.e(TAG, "Failed to reject call: ${e.message}")
                    }
                }
            )
        }.start()
    }
    
    private fun playSound() {
        soundManager?.play(Sound.INCOMING_CALL, 0)
    }
    
    private fun pauseSound() {
        soundManager?.pauseSilently()
    }
}
