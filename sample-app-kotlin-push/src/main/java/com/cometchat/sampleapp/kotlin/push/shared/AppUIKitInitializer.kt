package com.cometchat.sampleapp.kotlin.push.shared

import android.content.Context
import android.util.Log
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.sampleapp.kotlin.push.KotlinApplication
import com.cometchat.uikit.core.CometChatUIKit
import com.cometchat.uikit.core.UIKitSettings

/**
 * Cold-start bootstrap for Activities that can be the app's first screen.
 *
 * Most launches initialize the UIKit in [KotlinApplication] or [SplashViewModel], but a
 * push-notification tap opens [com.cometchat.sampleapp.kotlin.push.appflow.MessagesActivity]
 * directly — from a killed state that Activity can run before either of those has. Such screens
 * call [initIfNeeded] before touching the kit.
 *
 * Settings here mirror `KotlinApplication.ensureSdkInitializedForPush()` /
 * `SplashViewModel.initUIKit()`; keep them in sync.
 */
object AppUIKitInitializer {

    private const val TAG = "AppUIKitInitializer"

    /**
     * Invokes [onReady] as soon as the UIKit is usable — synchronously when it is already
     * initialized, otherwise after initializing it with the stored credentials.
     *
     * Invokes [onError] when there are no stored credentials (the user has not onboarded) or
     * initialization fails; callers should fall back to the onboarding flow rather than render.
     * Both callbacks may arrive on a background thread.
     */
    fun initIfNeeded(context: Context, onReady: () -> Unit, onError: (String) -> Unit) {
        if (CometChatUIKit.isSDKInitialized()) {
            onReady()
            return
        }

        val appId = AppPreferences.getAppId(context)
        val region = AppPreferences.getRegion(context)
        val authKey = AppPreferences.getAuthKey(context)
        if (appId.isNullOrEmpty() || region.isNullOrEmpty() || authKey.isNullOrEmpty()) {
            onError("No stored credentials")
            return
        }

        Log.d(TAG, "SDK not initialized on cold start — initializing before rendering")
        val settings = UIKitSettings.UIKitSettingsBuilder()
            .setAutoEstablishSocketConnection(false)
            .setAppId(appId)
            .setRegion(region)
            .setAuthKey(authKey)
            .subscribePresenceForAllUsers()
            .setEnableCalling(true)
            .setEnableThreadSubscription(true)
            .build()

        CometChatUIKit.init(context, settings, object : CometChat.CallbackListener<String>() {
            override fun onSuccess(result: String?) {
                Log.d(TAG, "Cold-start SDK init succeeded")
                // Register call/push listeners in the Application (idempotent no-op if already done).
                (context.applicationContext as? KotlinApplication)?.onSDKInitialized()
                onReady()
            }

            override fun onError(e: CometChatException?) {
                onError(e?.message ?: "SDK initialization failed")
            }
        })
    }
}
