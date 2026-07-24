package com.cometchat.sampleapp.kotlin.push.fcm

import android.util.Log
import com.cometchat.pushnotification.CometChatPushNotifications
import com.cometchat.pushnotification.models.PushPlatform
import com.cometchat.sampleapp.kotlin.push.shared.AppCredentials
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Minimal FCM service that delegates all push handling to the CometChat Push Notifications SDK.
 * Skips call notifications in the foreground — the real-time CallListener (see KotlinApplication)
 * handles those via WebSocket to avoid a duplicate incoming-call UI.
 */
class AppFCMService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "AppFCMService"
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val data = message.data

        if (CometChatPushNotifications.isAppInForeground() && isCallNotification(data)) {
            Log.d(TAG, "Skipping call push in foreground — CallListener handles it")
            return
        }

        CometChatPushNotifications.handlePushNotification(context = this, data = data)
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "onNewToken: $token")
        CometChatPushNotifications.handleTokenRefresh(
            PushPlatform.FCM_ANDROID,
            token,
            AppCredentials.PROVIDER_ID
        )
    }

    private fun isCallNotification(data: Map<String, String>): Boolean {
        val type = data["type"] ?: ""
        return type.contains("call", ignoreCase = true)
    }
}
