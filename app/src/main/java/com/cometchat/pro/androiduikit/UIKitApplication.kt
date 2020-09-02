package com.cometchat.pro.androiduikit

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import com.cometchat.pro.core.AppSettings
import com.cometchat.pro.core.CometChat
import com.cometchat.pro.exceptions.CometChatException
import com.cometchat.pro.androiduikit.constants.AppConfig

import listeners.CometChatCallListener

class UIKitApplication : Application() {

    override fun onCreate() {

        super.onCreate()
        Log.d(TAG, "onCreate: " + this.packageName)

        val appSettings = AppSettings.AppSettingsBuilder().subscribePresenceForAllUsers().setRegion(AppConfig.AppDetails.REGION).build()
        CometChat.init(this, AppConfig.AppDetails.APP_ID, appSettings, object : CometChat.CallbackListener<String>() {
            override fun onSuccess(s: String) {
                Log.d(TAG, "onSuccess: $s")
            }

            override fun onError(e: CometChatException) {
                Toast.makeText(this@UIKitApplication, e.message, Toast.LENGTH_SHORT).show()
            }
        })
        CometChatCallListener.addCallListener(TAG, this)
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.app_name)
            val description = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("2", name, importance)
            channel.description = description
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager!!.createNotificationChannel(channel)
        }

    }

    override fun onTerminate() {
        super.onTerminate()
        CometChatCallListener.removeCallListener(TAG)
    }

    companion object {

        private val TAG = "UIKitApplication"
    }
}
