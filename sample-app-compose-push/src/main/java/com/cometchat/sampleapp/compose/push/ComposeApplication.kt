package com.cometchat.sampleapp.compose.push

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.cometchat.calls.core.CallAppSettings
import com.cometchat.calls.core.CometChatCalls
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.Call
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.User
import com.cometchat.pushnotification.CometChatPushNotifications
import com.cometchat.pushnotification.PNConfiguration
import com.cometchat.pushnotification.listeners.CallAnsweredHandler
import com.cometchat.pushnotification.listeners.NotificationTapListener
import com.cometchat.pushnotification.models.PNCallInfo
import com.cometchat.sampleapp.compose.push.fcm.AppFCMService
import com.cometchat.sampleapp.compose.push.utils.AppConstants
import com.google.firebase.FirebaseApp
import com.cometchat.uikit.compose.presentation.ongoingcall.ui.CometChatOngoingCallActivity
import com.cometchat.uikit.core.CometChatUIKit
import com.cometchat.uikit.core.UIKitSettings
import com.cometchat.uikit.core.constants.UIKitConstants.CallWorkFlow
import com.cometchat.uikit.core.events.CometChatCallEvent
import com.cometchat.uikit.core.events.CometChatEvents
import com.cometchat.uikit.core.resources.soundmanager.CometChatSoundManager
import com.cometchat.uikit.core.resources.soundmanager.Sound
import com.cometchat.uikit.core.utils.CallManager
import com.cometchat.sampleapp.compose.push.shared.AppPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Application class for managing call notifications and handling CometChat events.
 * Exposes incoming call state via StateFlow for Compose UI to observe.
 *
 * SDK initialization is NOT performed here - it is handled by SplashScreen or AppCredentialsScreen.
 * Call listeners are registered via onSDKInitialized() callback after SDK is initialized elsewhere.
 */
class ComposeApplication : Application() {
    private val TAG: String = "ComposeApplication"
    private var currentActivityInstance: Activity? = null
    private var soundManager: CometChatSoundManager? = null
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val isConnectedToWebSockets = AtomicBoolean(false)

    // Track if SDK has been initialized and listeners registered
    private var isSDKInitialized = false

    // Incoming call state exposed for Compose UI
    private val _incomingCall = MutableStateFlow<Call?>(null)
    val incomingCall: StateFlow<Call?> = _incomingCall.asStateFlow()
    
    companion object {
        private val LISTENER_ID = "AppCallListener_${System.currentTimeMillis()}"
        
        // Singleton instance for accessing from Compose
        private var instance: ComposeApplication? = null

        private var isAppInForeground: Boolean = false
        var currentActivity: Activity? = null
        private var tempCall: Call? = null

        fun getInstance(): ComposeApplication? = instance

        fun isAppInForeground(): Boolean = isAppInForeground
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Initialize sound manager for incoming call sounds
        soundManager = CometChatSoundManager(this)

        // ── CometChat Push Notifications ──
        FirebaseApp.initializeApp(this)
        setupPushNotificationListeners()
        // Ensure the CometChat SDK (and then PN/Calls) is initialized here in Application.onCreate.
        // Required for KILLED-STATE pushes: FCM starts the process and Application.onCreate runs
        // before AppFCMService.onMessageReceived, but no Splash/UI runs to initialize the SDK.
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

                // Connect to WebSocket when app comes to foreground (only if SDK is initialized)
                if (isSDKInitialized && isConnectedToWebSockets.compareAndSet(false, true)) {
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
                // Re-show incoming call UI if there's a pending call
                if (_incomingCall.value != null && tempCall != null) {
                    _incomingCall.value = tempCall
                }
            }
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {
                isActivityChangingConfigurations = activity.isChangingConfigurations
                if (--activityReferences == 0 && !isActivityChangingConfigurations) {
                    isAppInForeground = false
                    Log.d(TAG, "App is now in BACKGROUND")

                    // Disconnect WebSocket when app goes to background (only if SDK is initialized)
                    if (isSDKInitialized) {
                        CometChat.disconnect(object : CometChat.CallbackListener<String?>() {
                            override fun onSuccess(s: String?) {
                                isConnectedToWebSockets.set(false)
                            }
                            override fun onError(e: CometChatException) {}
                        })
                    }
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
     * Called by SplashScreen or AppCredentialsScreen after SDK initialization succeeds.
     * Registers call listeners and initializes CometChatCalls SDK.
     *
     * This method is idempotent - calling it multiple times has no effect after the first call.
     */
    fun onSDKInitialized() {
        if (!isSDKInitialized) {
            isSDKInitialized = true
            Log.d(TAG, "onSDKInitialized called, registering call listeners")

            // Initialize CometChatCalls SDK
            initCometChatCalls()

            // Add UIKit call events listener
            addCallEventsListener()

            // Initialize Push Notifications now that credentials are available
            initPushNotifications()
        } else {
            Log.d(TAG, "onSDKInitialized called but already initialized, skipping")
        }
    }

    /**
     * Initializes the CometChat Push Notifications SDK using credentials from SharedPreferences.
     * Safe to call multiple times; no-ops when credentials are not yet available (first launch
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
            .setAutoEstablishSocketConnection(true)
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
     * Registers the CallAnsweredHandler (accept call from a push) and the NotificationTapListener.
     * The tap listener launches the single host MainActivity with the app's existing FCM extras so
     * MainActivity.handleNotificationIntent routes via the NavHost. (Routing to the exact
     * conversation is a known follow-up — see integration notes.)
     */
    private fun setupPushNotificationListeners() {
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

        CometChatPushNotifications.setOnNotificationTapListener(object : NotificationTapListener {
            override fun onNotificationTapped(
                context: Context,
                user: User?,
                group: Group?,
                message: BaseMessage?
            ) {
                // Launch the host Activity with the tapped conversation id; MainActivity routes via
                // the NavHost. (Deep routing to the exact conversation can be refined in MainActivity.)
                val intent = Intent(context, MainActivity::class.java).apply {
                    putExtra(
                        AppConstants.FCMConstants.NOTIFICATION_TYPE,
                        AppConstants.FCMConstants.NOTIFICATION_TYPE_MESSAGE
                    )
                    (user?.uid ?: group?.guid)?.let { putExtra(AppConstants.FCMConstants.KEY_UID, it) }
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
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
                dismissIncomingCall()
            }

            override fun onOutgoingCallRejected(call: Call) {
                Log.d(TAG, "onOutgoingCallRejected: ${call.sessionId}")
                dismissIncomingCall()
            }

            override fun onIncomingCallCancelled(call: Call) {
                Log.d(TAG, "onIncomingCallCancelled: ${call.sessionId}")
                dismissIncomingCall()
            }

            override fun onCallEndedMessageReceived(call: Call) {
                Log.d(TAG, "onCallEndedMessageReceived: ${call.sessionId}")
                dismissIncomingCall()
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
                        if (_incomingCall.value != null) {
                            Log.d(TAG, "CallAccepted: Launching ongoing call from foreground UI")
                            val call = event.call
                            currentActivityInstance?.let { activity ->
                                CometChatOngoingCallActivity.launchOngoingCallActivity(
                                    activity,
                                    call.sessionId,
                                    call.type,
                                    CallWorkFlow.DEFAULT,
                                    null,
                                    null
                                )
                            }
                        }
                        dismissIncomingCall()
                    }
                    is CometChatCallEvent.CallRejected -> {
                        Log.d(TAG, "CallRejected event received")
                        dismissIncomingCall()
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
            tempCall = call
            _incomingCall.value = call
        } else {
            rejectCallWithBusyStatus(call)
        }
    }
    
    /**
     * Dismisses the incoming call UI.
     */
    fun dismissIncomingCall() {
        if (_incomingCall.value != null) {
            _incomingCall.value = null
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
