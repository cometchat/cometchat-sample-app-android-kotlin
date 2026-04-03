package com.cometchat.sampleapp.kotlin.app

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import com.cometchat.calls.core.CallAppSettings
import com.cometchat.calls.core.CometChatCalls
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.Call
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.User
import com.cometchat.sampleapp.kotlin.utils.AppPreferences
import com.cometchat.uikit.core.CometChatUIKit
import com.cometchat.uikit.core.UIKitSettings
import com.cometchat.uikit.core.events.CometChatCallEvent
import com.cometchat.uikit.core.events.CometChatEvents
import com.cometchat.uikit.core.resources.soundmanager.CometChatSoundManager
import com.cometchat.uikit.core.resources.soundmanager.Sound
import com.cometchat.uikit.core.utils.CallManager
import com.cometchat.uikit.kotlin.presentation.incomingcall.CometChatIncomingCall
import com.cometchat.uikit.kotlin.shared.resources.utils.Utils
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Application class for the CometChat Sample App (XML Views / ViewBinding).
 *
 * This class serves as the entry point for the application and provides
 * methods to initialize the CometChat SDK with configurable credentials.
 * It also manages incoming call notifications using a Snackbar at the top of the screen.
 *
 * ## Key Design Decisions:
 * - SDK initialization is **deferred to the login flow** rather than being
 *   performed in [onCreate]. This allows the app to support credential
 *   switching and ensures initialization happens with valid credentials.
 * - **No Firebase/FCM initialization** - Push notifications are excluded
 *   from this sample app to maintain simplicity and focus on core chat features.
 * - **No VoIP/Calling setup** - Real-time VoIP calling is excluded from this sample app.
 * - **CometChatCalls SDK** is initialized for Calls UI features (call buttons, call logs).
 *
 * ## Usage:
 * ```kotlin
 * // Get the application instance
 * val app = application as SampleApplication
 *
 * // Initialize CometChat SDK
 * app.initializeCometChat(
 *     appId = "YOUR_APP_ID",
 *     region = "YOUR_REGION",
 *     authKey = "YOUR_AUTH_KEY",
 *     onSuccess = {
 *         // SDK initialized successfully, proceed to login
 *     },
 *     onError = { exception ->
 *         // Handle initialization error
 *         Log.e("SampleApp", "Init failed: ${exception.message}")
 *     }
 * )
 * ```
 *
 * @see CometChatUIKit
 * @see UIKitSettings
 */
class SampleApplication : Application() {

    companion object {
        private const val TAG = "SampleApplication"
        private val LISTENER_ID = "AppCallListener_${System.currentTimeMillis()}"

        // Static state for tracking app foreground/background
        var currentOpenChatId: String? = null
        private var isAppInForeground: Boolean = false
        var currentActivity: Activity? = null
        private var tempCall: Call? = null

        fun isAppInForeground(): Boolean = isAppInForeground

        fun getTempCall(): Call? = tempCall

        fun setTempCall(call: Call?) {
            tempCall = call
        }
    }

    private var currentActivityInstance: Activity? = null
    private var snackBar: Snackbar? = null
    private var soundManager: CometChatSoundManager? = null
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val isConnectedToWebSockets = AtomicBoolean(false)

    /** Flag to track if SDK has been initialized and listeners registered */
    private var isSDKInitialized = false

    /**
     * Called when the application is starting, before any activity, service,
     * or receiver objects have been created.
     *
     * Note: SDK initialization is intentionally NOT performed here.
     * It is deferred to the login flow to support credential configuration
     * and switching between different CometChat app credentials.
     */
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "SampleApplication created")
        soundManager = CometChatSoundManager(this)

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

        // SDK initialization is deferred to login flow
        // No Firebase/FCM initialization (excluded from sample apps)
        // No VoIP setup (excluded from sample apps)
    }

    /**
     * Initializes the CometChat SDK with the provided credentials.
     *
     * This method should be called before attempting to log in users.
     * It configures the CometChat UIKit with the specified App ID, Region,
     * and Auth Key, then initializes the underlying SDK.
     *
     * ## Thread Safety:
     * This method can be called from any thread. The callbacks will be
     * invoked on the main thread.
     *
     * ## Error Handling:
     * If initialization fails, the [onError] callback will be invoked with
     * a [CometChatException] containing details about the failure. Common
     * failure reasons include:
     * - Invalid App ID
     * - Invalid Region
     * - Network connectivity issues
     *
     * @param appId The CometChat App ID obtained from the CometChat dashboard.
     *              This uniquely identifies your CometChat application.
     * @param region The region where your CometChat app is hosted (e.g., "us", "eu", "in").
     *               This should match the region selected during app creation.
     * @param authKey The Auth Key for your CometChat app, used for user authentication.
     *                This can be found in the CometChat dashboard under API & Auth Keys.
     * @param onSuccess Callback invoked when SDK initialization completes successfully.
     *                  After this callback, you can proceed to log in users.
     * @param onError Callback invoked when SDK initialization fails.
     *                The [CometChatException] parameter contains error details.
     *
     * @see CometChatUIKit.init
     * @see UIKitSettings
     */
    fun initializeCometChat(
        appId: String,
        region: String,
        authKey: String,
        onSuccess: () -> Unit,
        onError: (CometChatException) -> Unit
    ) {
        Log.d(TAG, "Initializing CometChat SDK with appId: $appId, region: $region")

        // Validate input parameters
        if (appId.isBlank()) {
            val exception = CometChatException(
                "INVALID_APP_ID",
                "App ID cannot be empty",
                "Please provide a valid CometChat App ID"
            )
            Log.e(TAG, "Initialization failed: ${exception.message}")
            onError(exception)
            return
        }

        if (region.isBlank()) {
            val exception = CometChatException(
                "INVALID_REGION",
                "Region cannot be empty",
                "Please provide a valid region (e.g., 'us', 'eu', 'in')"
            )
            Log.e(TAG, "Initialization failed: ${exception.message}")
            onError(exception)
            return
        }

        if (authKey.isBlank()) {
            val exception = CometChatException(
                "INVALID_AUTH_KEY",
                "Auth Key cannot be empty",
                "Please provide a valid CometChat Auth Key"
            )
            Log.e(TAG, "Initialization failed: ${exception.message}")
            onError(exception)
            return
        }

        // Build UIKit settings with the provided credentials
        val uiKitSettings = UIKitSettings.UIKitSettingsBuilder()
            .setAppId(appId)
            .setRegion(region)
            .setAuthKey(authKey)
            .subscribePresenceForAllUsers()
            .setAutoEstablishSocketConnection(true)
            .setEnableCalling(true)
            .build()

        // Initialize the CometChat UIKit
        CometChatUIKit.init(
            context = this,
            authSettings = uiKitSettings,
            callbackListener = object : CometChat.CallbackListener<String>() {
                override fun onSuccess(result: String?) {
                    Log.d(TAG, "CometChat SDK initialized successfully: $result")
                    onSuccess()
                }

                override fun onError(exception: CometChatException?) {
                    val error = exception ?: CometChatException(
                        "UNKNOWN_ERROR",
                        "Unknown initialization error",
                        "An unexpected error occurred during SDK initialization"
                    )
                    Log.e(TAG, "CometChat SDK initialization failed: ${error.message}")
                    onError(error)
                }
            }
        )
    }

    /**
     * Checks if the CometChat SDK has been initialized.
     *
     * This can be used to determine whether [initializeCometChat] needs to
     * be called before attempting to log in users.
     *
     * @return `true` if the SDK is initialized and ready for use, `false` otherwise.
     *
     * @see CometChatUIKit.isSDKInitialized
     */
    fun isSDKInitialized(): Boolean {
        return CometChatUIKit.isSDKInitialized()
    }

    /**
     * Called after SDK initialization succeeds to register call listeners
     * and initialize CometChatCalls SDK.
     *
     * This method is idempotent - it will only register listeners once.
     * Call this method after successful SDK initialization to enable
     * incoming call handling and Calls UI features.
     *
     * ## Usage:
     * ```kotlin
     * app.initializeCometChat(appId, region, authKey,
     *     onSuccess = {
     *         app.onSDKInitialized()
     *         // Proceed with login
     *     },
     *     onError = { /* handle error */ }
     * )
     * ```
     */
    fun onSDKInitialized() {
        if (!isSDKInitialized) {
            isSDKInitialized = true
            Log.d(TAG, "onSDKInitialized: Registering call listeners")

            // Initialize CometChatCalls SDK after CometChatUIKit is initialized
            // Call listeners are added in the success callback to ensure proper initialization order
            initCometChatCalls()

            // Add UIKit call events listener (this doesn't depend on CometChatCalls)
            addCallEventsListener()
        }
    }

    /**
     * Initializes the CometChatCalls SDK using credentials from SharedPreferences.
     * Call listeners are registered after successful initialization.
     *
     * Note: This initializes Calls UI features (call buttons, call logs) only.
     * VoIP real-time calling is NOT included in this sample app.
     */
    private fun initCometChatCalls() {
        val appPreferences = AppPreferences(this)
        val appId = appPreferences.getAppId()
        val region = appPreferences.getRegion()

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
                // Add call listener AFTER CometChatCalls is initialized
                addCallListener()
            }

            override fun onError(p0: com.cometchat.calls.exceptions.CometChatException?) {
                Log.e(TAG, "CometChatCalls init onError: ${p0?.message}")
            }
        })
    }

    /**
     * Adds a call listener to handle incoming calls using SDK listeners.
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
        // Check if call initiator is the logged-in user
        val callInitiator = call.callInitiator
        if (callInitiator is User) {
            val loggedInUser = CometChatUIKit.getLoggedInUser()
            if (loggedInUser != null && loggedInUser.uid.equals(callInitiator.uid, ignoreCase = true)) {
                return
            }
        }

        // Check if there's already an active call
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

        // Dismiss existing snackbar if showing to prevent multiple instances
        if (snackBar != null && snackBar!!.isShown) {
            snackBar?.dismiss()
        }

        // Get the root view of the current activity
        val rootView: View = currentActivityInstance!!.findViewById(android.R.id.content)

        // Create the incoming call component
        val cometChatIncomingCall = CometChatIncomingCall(currentActivityInstance!!).apply {
            setDisableSoundForCalls(true) // Sound is managed by Application
            setCall(call)
            fitsSystemWindows = true
            setOnError { _ -> dismissTopSnackBar() }
        }

        // Create and configure the snackbar
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

    /**
     * Plays the sound for an incoming call notification.
     */
    private fun playSound() {
        soundManager?.play(Sound.INCOMING_CALL, 0)
    }

    /**
     * Silently pauses any currently playing sound.
     */
    private fun pauseSound() {
        soundManager?.pauseSilently()
    }
}
