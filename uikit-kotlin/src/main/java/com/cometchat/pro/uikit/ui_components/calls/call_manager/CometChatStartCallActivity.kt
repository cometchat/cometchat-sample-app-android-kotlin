package com.cometchat.pro.uikit.ui_components.calls.call_manager

import android.app.ActivityManager
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cometchat.pro.core.Call
import com.cometchat.pro.core.CallSettings
import com.cometchat.pro.core.CallSettings.CallSettingsBuilder
import com.cometchat.pro.core.CometChat
import com.cometchat.pro.exceptions.CometChatException
import com.cometchat.pro.models.AudioMode
import com.cometchat.pro.models.User
import com.cometchat.pro.uikit.R
import com.cometchat.pro.uikit.ui_components.calls.call_manager.ongoing_call.OngoingCallService
import com.cometchat.pro.uikit.ui_components.cometchat_ui.CometChatUI
import com.cometchat.pro.uikit.ui_resources.constants.UIKitConstants
import com.cometchat.pro.uikit.ui_resources.utils.ErrorMessagesUtils

class CometChatStartCallActivity : AppCompatActivity() {


    lateinit var activity: CometChatStartCallActivity

    private lateinit var mainView: RelativeLayout

    private var sessionID: String? = null

    private lateinit var callSettings: CallSettings

    private var ongoingCallService: OngoingCallService? = null

    private var mServiceIntent: Intent? = null

    private var isDefaultCall = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cometchat_start_call)
        activity = this
        mainView = findViewById(R.id.call_view)
        if (intent != null) sessionID =
            intent.getStringExtra(UIKitConstants.IntentStrings.SESSION_ID)
        ongoingCallService = OngoingCallService()
        mServiceIntent = Intent(this, ongoingCallService?.javaClass)
        isDefaultCall = intent.getBooleanExtra(UIKitConstants.IntentStrings.IS_DEFAULT_CALL, false)
        if (isDefaultCall && !isMyServiceRunning(ongoingCallService?.javaClass!!)) {
            startService(mServiceIntent)
        }

        callSettings =
            CallSettingsBuilder(this, mainView).setSessionId(sessionID).startWithAudioMuted(true)
                .startWithVideoMuted(true).build()

        CometChat.startCall(callSettings, object : CometChat.OngoingCallListener {
            override fun onUserJoined(p0: User?) {
                p0?.uid?.let { Log.i("onUserJoined: ", it) }
            }

            override fun onUserLeft(p0: User?) {
                p0?.uid?.let { Log.i("onUserLeft: ", it) }
            }

            override fun onError(p0: CometChatException?) {
                p0?.message?.let { Log.i("onstartcallError: ", it) }
                ErrorMessagesUtils.cometChatErrorMessage(this@CometChatStartCallActivity, p0?.code)
            }

            override fun onCallEnded(p0: Call?) {
                Log.i("TAG", "onCallEnded: ")
                showToast()
                finish()
                stopService(mServiceIntent)
            }

            override fun onUserListUpdated(p0: MutableList<User>?) {
                Log.i("TAG", "onUserListUpdated: " + p0.toString())
            }

            override fun onAudioModesUpdated(p0: MutableList<AudioMode>?) {
                Log.i("TAG", "onAudioModesUpdated: " + p0.toString())
            }

            override fun onRecordingStarted(p0: User?) {
                Log.i("TAG", "onRecordingStarted: " + p0.toString())
            }

            override fun onRecordingStopped(p0: User?) {
                Log.i("TAG", "onRecordingStopped: " + p0.toString())
            }

            override fun onUserMuted(p0: User?, p1: User?) {
                Log.i("TAG", "onUserMuted: " + p0.toString())
            }

            override fun onCallSwitchedToVideo(p0: String?, p1: User?, p2: User?) {
                Log.i("TAG", "onCallSwitchedToVideo: " + p0.toString())
            }

        })
    }

    private fun showToast() {
        val layoutInflater = layoutInflater
        val layout: View = layoutInflater.inflate(
            R.layout.custom_toast, findViewById<ViewGroup>(R.id.group_layout)
        )
        val tvMessage = layout.findViewById<TextView>(R.id.message)
        tvMessage.text = "Call Ended"
        val toast = Toast(applicationContext)
        toast.duration = Toast.LENGTH_SHORT
        toast.setGravity(Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM, 0, 0)
        toast.view = layout

        toast.show()
    }

    override fun onBackPressed() {
        startActivity(Intent(this@CometChatStartCallActivity, CometChatUI::class.java))
    }

    private fun isMyServiceRunning(serviceClass: Class<out OngoingCallService?>): Boolean {
        val manager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        return manager.getRunningServices(Integer.MAX_VALUE)
            .any { it.service.className == serviceClass.canonicalName }

    }
}
