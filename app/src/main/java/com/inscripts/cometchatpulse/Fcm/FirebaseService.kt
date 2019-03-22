package com.inscripts.cometchatpulse.Fcm

import android.app.PendingIntent
import android.content.Intent
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.util.Log
import com.cometchat.pro.core.CometChat
import com.cometchat.pro.models.Group
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.inscripts.cometchatpulse.Activities.MainActivity
import com.inscripts.cometchatpulse.Fragment.OneToOneFragment
import com.inscripts.cometchatpulse.R
import com.inscripts.cometchatpulse.StringContract
import org.json.JSONObject

class FirebaseService : FirebaseMessagingService() {

    private val TAG = "FirebaseService"

    private val CHANNEL_ID: String = "1"

    private lateinit var json: JSONObject

    private val GROUP_ID:String="group_string"

    private var REQUEST_CODE: Int = 2

    private lateinit var messageData:JSONObject

    companion object {

         var alertList: MutableList<String>? = arrayListOf()

         fun clearMessageList(){

            FirebaseService.alertList=null
            FirebaseService.alertList= mutableListOf()
        }

        fun subscribeToGroup(groupList:List<Group>){

            for (group in groupList){
                FirebaseMessaging.getInstance().subscribeToTopic("${StringContract.AppDetails.APP_ID}_group_${group.guid}")
            }
        }
        fun subscribeToUser(UID:String?){
            FirebaseMessaging.getInstance().subscribeToTopic("${StringContract.AppDetails.APP_ID}_user_$UID")
        }
    }

    override fun onNewToken(p0: String?) {
        Log.d(TAG, "Refreshed token: $p0")
    }

    override fun onMessageReceived(p0: RemoteMessage?) {
        Log.d(TAG, "From: ${p0?.from}")
        Log.d(TAG, "RemoteMessage: ${p0?.data}")

        json = JSONObject(p0?.data)

        Log.d(TAG, "onMessageReceived: " + json.toString())

        val data=json.get("message")

        Log.d(TAG,"Data: ${data}")

        messageData= JSONObject(data.toString())

        Log.d(TAG,"messageData: $data")

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
          if (OneToOneFragment.currentId!=messageData.getString("sender")&&
                  CometChat.getLoggedInUser().uid!=messageData.getString("sender")) {

              val pendingIntent = PendingIntent.getActivity(this, REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT)
              FirebaseService.alertList?.add(json.getString("alert"))
              showNotification(pendingIntent)
          }

    }


    private fun showNotification(pendingIntent: PendingIntent) {

        val inbox = NotificationCompat.InboxStyle()

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.cc_small)
                .setContentTitle(json.getString("title"))
                .setContentText(json.getString("alert"))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setBadgeIconType(R.drawable.cc_small)
                .setContentIntent(pendingIntent)
                .setGroup(GROUP_ID)
                .setGroupSummary(true)
                .setAutoCancel(true)

                try {
                for (i in 0..FirebaseService.alertList?.size!!) {
                   inbox.addLine(FirebaseService.alertList!![i])
                 }
               } catch (e: Exception) {
               e.printStackTrace()
              }
         builder.setStyle(inbox)

        NotificationManagerCompat.from(this).apply {
            notify(1, builder.build())
//             notify(2,summaryNotification)
        }
    }

}