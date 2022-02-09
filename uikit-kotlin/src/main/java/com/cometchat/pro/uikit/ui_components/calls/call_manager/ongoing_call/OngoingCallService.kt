package com.cometchat.pro.uikit.ui_components.calls.call_manager.ongoing_call

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.cometchat.pro.core.CometChat
import com.cometchat.pro.uikit.R
import com.cometchat.pro.uikit.ui_components.cometchat_ui.CometChatUI
import com.cometchat.pro.uikit.ui_resources.constants.UIKitConstants
import java.util.*

class OngoingCallService : Service() {
    private var counter = 0
    var notificationManager: NotificationManager? = null
    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) startMyOwnForeground() else this.startForeground(
            1,
            Notification()
        )
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun startMyOwnForeground() {
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager!!.cancel(2)
        val pendingIntent: PendingIntent
        pendingIntent = if (CometChat.getActiveCall() != null) {
            PendingIntent.getBroadcast(
                applicationContext, REQUEST_CODE,
                getCallIntent("Ongoing"), PendingIntent.FLAG_UPDATE_CURRENT
            )
        } else {
            PendingIntent.getBroadcast(
                applicationContext, REQUEST_CODE,
                getCallIntent("Ongoing"), PendingIntent.FLAG_UPDATE_CURRENT
            )
        }
        val notificationBuilder = NotificationCompat.Builder(this, "2")
        val notification = notificationBuilder.setOngoing(true)
            .setSmallIcon(R.drawable.cc)
            .setColor(resources.getColor(R.color.colorPrimary))
            .setContentTitle(resources.getString(R.string.tap_to_join_call))
            .setPriority(NotificationManager.IMPORTANCE_HIGH)
            .setContentIntent(pendingIntent)
            .setCategory(Notification.CATEGORY_CALL)
            .build()
        startForeground(1, notification)
    }

    private fun getCallIntent(title: String): Intent {
        val callIntent: Intent
        return if (CometChat.getActiveCall() != null) {
            callIntent = Intent(applicationContext, OngoingCallBroadcast::class.java)
            callIntent.putExtra(
                UIKitConstants.IntentStrings.SESSION_ID,
                CometChat.getActiveCall().sessionId
            )
            callIntent.putExtra(UIKitConstants.IntentStrings.TYPE, CometChat.getActiveCall().type)
            callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            callIntent.action = title
            callIntent
        } else {
            callIntent = Intent(applicationContext, CometChatUI::class.java)
            callIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            notificationManager!!.cancel(2)
            notificationManager!!.cancel(1)
            callIntent
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        startTimer()
        startCall()
        return START_STICKY
    }

    private fun startCall() {}
    override fun onDestroy() {
        super.onDestroy()
        stopTimer()

//        Intent broadcastIntent = new Intent();
//        broadcastIntent.setAction("restartservice");
//        this.sendBroadcast(broadcastIntent);
    }

    override fun stopService(name: Intent): Boolean {
        stopTimer()
        return super.stopService(name)
    }

    private var timer: Timer? = null
    private var timerTask: TimerTask? = null
    fun startTimer() {
        timer = Timer()
        timerTask = object : TimerTask() {
            override fun run() {
                Log.d("AppInBackground: ", "" + counter++)
            }
        }
        timer!!.schedule(timerTask, 1000, 1000)
    }

    fun stopTimer() {
        if (timer != null) {
            timer!!.cancel()
            timer = null
        }
    }

    companion object {
        private const val REQUEST_CODE = 888
    }
}