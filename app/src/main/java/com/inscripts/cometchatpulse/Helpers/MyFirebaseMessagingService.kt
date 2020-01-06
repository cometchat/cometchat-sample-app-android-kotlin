package com.inscripts.cometchatpulse.Helpers

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.cometchat.pro.constants.CometChatConstants
import com.cometchat.pro.core.Call
import com.cometchat.pro.helpers.CometChatHelper
import com.cometchat.pro.models.BaseMessage
import com.cometchat.pro.models.Group
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.inscripts.cometchatpulse.Activities.MainActivity
import com.inscripts.cometchatpulse.R
import com.inscripts.cometchatpulse.StringContract
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

class MyFirebaseMessagingService : FirebaseMessagingService() {
    private var json: JSONObject? = null
    private var intent: Intent? = null
    private var count = 0
    private var call: Call? = null

    private var isCall: Boolean = false

    override fun onNewToken(s: String?) {
        Log.d(TAG, "onNewToken: " + s!!)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        try {
            count++
            json = JSONObject(remoteMessage.data)
            Log.d(TAG, "JSONObject: " + json!!.toString())
            val messageData = JSONObject(json!!.getString("message"))
            val baseMessage = CometChatHelper.processMessage(JSONObject(remoteMessage.data["message"]))
            intent = Intent(applicationContext,MainActivity::class.java);
            intent?.putExtra(StringContract.IntentString.RECIVER_TYPE,baseMessage.receiverType)
            if (baseMessage.receiverType.equals(CometChatConstants.RECEIVER_TYPE_USER)) {
                intent?.putExtra(StringContract.IntentString.USER_ID, baseMessage.sender.uid)
                intent?.putExtra(StringContract.IntentString.USER_NAME,baseMessage.sender.name)
                intent?.putExtra(StringContract.IntentString.USER_AVATAR,baseMessage.sender.avatar)
                intent?.putExtra(StringContract.IntentString.USER_STATUS,baseMessage.sender.status)
            }
            else
            {
                intent?.putExtra(StringContract.IntentString.GROUP_ID, baseMessage.receiverUid)
                intent?.putExtra(StringContract.IntentString.GROUP_NAME,(baseMessage.receiver as Group).name)
                intent?.putExtra(StringContract.IntentString.GROUP_ICON,(baseMessage.receiver as Group).icon)
                intent?.putExtra(StringContract.IntentString.GROUP_DESCRIPTION,(baseMessage.receiver as Group).description)
                intent?.putExtra(StringContract.IntentString.GROUP_OWNER,(baseMessage.receiver as Group).owner)
                intent?.putExtra(StringContract.IntentString.USER_SCOPE,(baseMessage.receiver as Group).scope)
            }
            intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            val pendingIntent = PendingIntent.getActivity(applicationContext, baseMessage.id, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            if (baseMessage is Call) {
                call = baseMessage
                isCall = true
            }
            showNotifcation(pendingIntent, baseMessage)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

    }

    fun getBitmapFromURL(strURL: String): Bitmap? {
        try {
            val url = URL(strURL)
            val connection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val input = connection.inputStream
            return BitmapFactory.decodeStream(input)
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }

    }

    private fun showNotifcation(pendingIntent: PendingIntent, baseMessage: BaseMessage) {

        try {
            val m = Date().time.toInt()
            val GROUP_ID = "group_id"

            val builder = NotificationCompat.Builder(this, "2")
                    .setSmallIcon(R.drawable.cc_small)
                    .setContentTitle(json!!.getString("title"))
                    .setContentText(json!!.getString("alert"))
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent)
                    .setColor(resources.getColor(R.color.primaryColor))
                    .setLargeIcon(getBitmapFromURL(baseMessage.sender.avatar))
                    .setGroup(GROUP_ID)
                    .setAutoCancel(true)
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))

            val summaryBuilder = NotificationCompat.Builder(this, "2")
                    .setContentTitle("CometChat")
                    .setContentText("$count messages")
                    .setSmallIcon(R.drawable.cc_small)
                    .setGroup(GROUP_ID)
                    .setGroupSummary(true)
            val notificationManager = NotificationManagerCompat.from(this)

            if (isCall) {
                builder.setGroup(GROUP_ID + "Call")
                if (json!!.getString("alert") == "Incoming audio call") {
                    builder.setOngoing(true)
                    builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE))
                    builder.addAction(0, "Answers", PendingIntent.getBroadcast(applicationContext, REQUEST_CODE, getCallIntent("Answers"), PendingIntent.FLAG_UPDATE_CURRENT))
                    builder.addAction(0, "Decline", PendingIntent.getBroadcast(applicationContext, 1, getCallIntent("Decline"), PendingIntent.FLAG_UPDATE_CURRENT))
                }
                notificationManager.notify(5, builder.build())
            } else {
                notificationManager.notify(baseMessage.id, builder.build())
                notificationManager.notify(0, summaryBuilder.build())
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun getCallIntent(title: String): Intent {
        val callIntent = Intent(applicationContext, CallNotificationAction::class.java)
        callIntent.putExtra(StringContract.IntentString.SESSION_ID, call!!.sessionId)
        if (call?.receiverType.equals(CometChatConstants.RECEIVER_TYPE_USER)) {
            callIntent.putExtra(StringContract.IntentString.USER_NAME, call?.sender?.name)
            callIntent.putExtra(StringContract.IntentString.USER_AVATAR, call?.sender?.avatar)
        }
        else
        {
            callIntent.putExtra(StringContract.IntentString.GROUP_NAME, (call?.sender as Group).name)
            callIntent.putExtra(StringContract.IntentString.GROUP_ICON, (call?.sender as Group).icon)
        }
        callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        callIntent.action = title
        return callIntent
    }

    companion object {
        private val TAG = "MyFirebaseService"
        private val REQUEST_CODE = 12


        fun subscribeUser(UID: String) {
            FirebaseMessaging.getInstance().subscribeToTopic(StringContract.AppDetails.APP_ID + "_" + CometChatConstants.RECEIVER_TYPE_USER + "_" +
                    UID)
        }

        fun unsubscribeUser(UID: String) {
            FirebaseMessaging.getInstance().unsubscribeFromTopic(StringContract.AppDetails.APP_ID + "_" + CometChatConstants.RECEIVER_TYPE_USER + "_" +
                    UID)
        }

        fun subscribeGroup(GUID: String) {
            FirebaseMessaging.getInstance().subscribeToTopic(StringContract.AppDetails.APP_ID + "_" + CometChatConstants.RECEIVER_TYPE_GROUP + "_" +
                    GUID)
        }

        fun unsubscribeGroup(GUID: String) {
            FirebaseMessaging.getInstance().unsubscribeFromTopic(StringContract.AppDetails.APP_ID + "_" + CometChatConstants.RECEIVER_TYPE_GROUP + "_" +
                    GUID)
        }
    }

}