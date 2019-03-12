package com.inscripts.cometchatpulse.Fcm

import android.app.PendingIntent
import android.content.Intent
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.inscripts.cometchatpulse.Activities.MainActivity
import com.inscripts.cometchatpulse.R
import com.inscripts.cometchatpulse.StringContract
import org.json.JSONObject

class FirebaseService : FirebaseMessagingService() {

    private val TAG="FirebaseService"

    private  val CHANNEL_ID:String="1"

    private  lateinit var json:JSONObject

    private  var REQUEST_CODE:Int=2

//  private lateinit var senderUid

    override fun onNewToken(p0: String?) {
        Log.d(TAG, "Refreshed token: $p0")
    }

    override fun onMessageReceived(p0: RemoteMessage?) {
        Log.d(TAG, "From: ${p0?.from}")
        Log.d(TAG, "RemoteMessage: ${p0?.data}")

        json =JSONObject(p0?.data)

        Log.d(TAG,"onMessageReceived: "+json.toString())

        val intent=Intent(this,MainActivity::class.java).apply {
            flags=Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val pendingIntent=PendingIntent.getActivity(this,REQUEST_CODE,intent,0)

        showNotification(pendingIntent)

    }

    private fun showNotification(pendingIntent: PendingIntent){
        val builder=NotificationCompat.Builder(this,CHANNEL_ID)
                .setSmallIcon(R.drawable.cc_small)
                .setContentText(json.getString("alert"))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setBadgeIconType(R.drawable.cc_small)
                .setColor(StringContract.Color.primaryColor)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

        with(NotificationManagerCompat.from(this)){
            notify(json.getString("id").toInt(),builder.build())
        }
    }

}