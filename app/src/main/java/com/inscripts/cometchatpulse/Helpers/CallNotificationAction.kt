package com.inscripts.cometchatpulse.Helpers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat
import com.cometchat.pro.constants.CometChatConstants
import com.cometchat.pro.core.Call
import com.cometchat.pro.core.CometChat
import com.cometchat.pro.exceptions.CometChatException
import com.inscripts.cometchatpulse.Activities.CallActivity
import com.inscripts.cometchatpulse.CometChatPro
import com.inscripts.cometchatpulse.StringContract

class CallNotificationAction : BroadcastReceiver() {

    internal var TAG = "RejectAction"
    override fun onReceive(context: Context, intent: Intent) {
        val sessionID = intent.getStringExtra(StringContract.IntentString.SESSION_ID)
        Log.e(TAG, "onReceive: " + intent.getStringExtra(StringContract.IntentString.SESSION_ID))
        if (intent.action == "Answers") {
            val acceptIntent = Intent(context, CallActivity::class.java)
            acceptIntent.putExtra(StringContract.IntentString.SESSION_ID, sessionID)
            acceptIntent.putExtra("NotificationIntent","Call_Notification")
            if (intent.hasExtra(StringContract.IntentString.USER_NAME) && intent.hasExtra(StringContract.IntentString.USER_AVATAR))
            {
                acceptIntent.putExtra(StringContract.IntentString.USER_NAME,intent.getStringExtra(StringContract.IntentString.USER_NAME))
                acceptIntent.putExtra(StringContract.IntentString.USER_AVATAR,intent.getStringExtra(StringContract.IntentString.USER_AVATAR))
            }
            else
            {
                acceptIntent.putExtra(StringContract.IntentString.GROUP_NAME,intent.getStringExtra(StringContract.IntentString.GROUP_NAME))
                acceptIntent.putExtra(StringContract.IntentString.GROUP_ICON,intent.getStringExtra(StringContract.IntentString.GROUP_ICON))
            }
            acceptIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(acceptIntent)
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.cancel(5)

        } else {
            CometChat.rejectCall(sessionID, CometChatConstants.CALL_STATUS_REJECTED, object : CometChat.CallbackListener<Call>() {
                override fun onSuccess(p0: Call?) {
                    Log.d(TAG, "onSuccess: " )
                    Toast.makeText(CometChatPro.applicationContext(), "Call Rejected", Toast.LENGTH_SHORT).show()
                    val notificationManager = NotificationManagerCompat.from(context)
                    notificationManager.cancel(5)
                }

                override fun onError(p0: CometChatException?) {
                    Toast.makeText(CometChatPro.applicationContext(), p0?.message, Toast.LENGTH_SHORT).show()
                }

            })
        }
    }
}