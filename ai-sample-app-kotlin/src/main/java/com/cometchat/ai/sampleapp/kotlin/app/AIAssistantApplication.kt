package com.cometchat.ai.sampleapp.kotlin.app

import android.app.Application
import android.util.Log
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.uikit.core.CometChatUIKit
import com.cometchat.uikit.core.UIKitSettings

/**
 * Application class for the AI Assistant Sample App (v6 UIKit).
 *
 * This sample demonstrates the AI Agent chat experience using CometChat v6
 * (`chatuikit-kotlin` + `chatuikit-core`). The SDK is initialized via the
 * credentials flow (see [initializeCometChat]) and NOT automatically at app
 * start, so users can switch credentials.
 *
 * Notes:
 * - No VoIP/calling — this sample is text-only (AI chat).
 * - No FCM/push — excluded from sample apps.
 */
class AIAssistantApplication : Application() {

    companion object {
        private const val TAG = "AIAssistantApplication"

        /** UID of the currently open chat (used for in-app notification suppression). */
        var currentOpenChatId: String? = null
    }

    /**
     * Initializes the CometChat SDK with the provided credentials.
     *
     * Called from [com.cometchat.ai.sampleapp.kotlin.ui.credentials.AppCredentialsActivity]
     * (first-time setup) and [com.cometchat.ai.sampleapp.kotlin.ui.splash.SplashActivity]
     * (auto-init for returning users).
     */
    fun initializeCometChat(
        appId: String,
        region: String,
        authKey: String,
        onSuccess: () -> Unit,
        onError: (CometChatException) -> Unit
    ) {
        Log.d(TAG, "Initializing CometChat SDK (appId=$appId, region=$region)")

        val settings = UIKitSettings.UIKitSettingsBuilder()
            .setAppId(appId)
            .setRegion(region)
            .setAuthKey(authKey)
            .subscribePresenceForAllUsers()
            .setAutoEstablishSocketConnection(true)
            .build()

        CometChatUIKit.init(
            context = this,
            authSettings = settings,
            callbackListener = object : CometChat.CallbackListener<String>() {
                override fun onSuccess(result: String?) {
                    Log.d(TAG, "CometChat SDK initialized: $result")
                    onSuccess()
                }

                override fun onError(exception: CometChatException?) {
                    val error = exception ?: CometChatException(
                        "UNKNOWN_ERROR",
                        "Unknown initialization error",
                        "An unexpected error occurred during SDK initialization"
                    )
                    Log.e(TAG, "SDK init failed: ${error.message}")
                    onError(error)
                }
            }
        )
    }

    fun isSDKInitialized(): Boolean = CometChatUIKit.isSDKInitialized()
}
