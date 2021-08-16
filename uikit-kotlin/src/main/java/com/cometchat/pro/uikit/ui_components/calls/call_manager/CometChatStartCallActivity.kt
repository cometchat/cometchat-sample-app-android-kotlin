package com.cometchat.pro.uikit.ui_components.calls.call_manager

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
import com.cometchat.pro.uikit.ui_resources.constants.UIKitConstants
import com.cometchat.pro.uikit.ui_resources.utils.ErrorMessagesUtils
import com.cometchat.pro.uikit.ui_resources.utils.Utils

class CometChatStartCallActivity : AppCompatActivity() {


    lateinit var activity: CometChatStartCallActivity

    private lateinit var mainView: RelativeLayout

    private var sessionID: String? = null

    private lateinit var callSettings: CallSettings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cometchat_start_call)
        activity = this
        mainView = findViewById(R.id.call_view)
        if (intent != null)
            sessionID = intent.getStringExtra(UIKitConstants.IntentStrings.SESSION_ID)


        callSettings = CallSettingsBuilder(this, mainView)
                .setSessionId(sessionID)
                .startWithAudioMuted(true)
                .startWithVideoMuted(true)
                .build()

        CometChat.startCall(callSettings, object : CometChat.OngoingCallListener {
            override fun onUserJoined(p0: User?) {
                Log.e("onUserJoined: ", p0?.uid)
            }

            override fun onUserLeft(p0: User?) {
                Log.e("onUserLeft: ", p0?.uid)
            }

            override fun onError(p0: CometChatException) {
                Log.e("onstartcallError: ", p0?.message)
                ErrorMessagesUtils.cometChatErrorMessage(this@CometChatStartCallActivity, p0.code)
            }

            override fun onCallEnded(p0: Call?) {
                Log.e("TAG", "onCallEnded: ")
                showToast()
                finish()
            }

            override fun onUserListUpdated(p0: MutableList<User>?) {
                Log.e("TAG", "onUserListUpdated: " + p0.toString())
            }

            override fun onAudioModesUpdated(p0: MutableList<AudioMode>?) {
                Log.e("TAG", "onAudioModesUpdated: "+p0.toString() )
            }

        })
    }

    private fun showToast() {
        val layoutInflater = layoutInflater
        val layout :View = layoutInflater.inflate(R.layout.custom_toast, findViewById<ViewGroup>(R.id.group_layout))
        val tvMessage = layout.findViewById<TextView>(R.id.message)
        tvMessage.text = "Call Ended"
        val toast = Toast(applicationContext)
        toast.duration = Toast.LENGTH_SHORT
        toast.setGravity(Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM,0,0)
        toast.view = layout

        toast.show()
    }
}