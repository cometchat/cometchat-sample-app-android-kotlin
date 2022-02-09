package com.cometchat.pro.uikit.ui_components.calls.call_manager.ongoing_call

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.cometchat.pro.uikit.ui_components.calls.call_manager.CometChatStartCallActivity
import com.cometchat.pro.uikit.ui_resources.constants.UIKitConstants

class OngoingCallBroadcast : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val sessionID = intent.getStringExtra(UIKitConstants.IntentStrings.SESSION_ID)
        val type = intent.getStringExtra(UIKitConstants.IntentStrings.TYPE)
        Log.e("onReceive: ", sessionID!!)
        if (intent.action == "Ongoing") {
            val joinOngoingIntent = Intent(context, CometChatStartCallActivity::class.java)
            joinOngoingIntent.putExtra(UIKitConstants.IntentStrings.SESSION_ID, sessionID)
            joinOngoingIntent.putExtra(UIKitConstants.IntentStrings.TYPE, type)
            joinOngoingIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(joinOngoingIntent)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(Intent(context, OngoingCallService::class.java))
        } else {
            context.startService(Intent(context, OngoingCallService::class.java))
        }
    }
}