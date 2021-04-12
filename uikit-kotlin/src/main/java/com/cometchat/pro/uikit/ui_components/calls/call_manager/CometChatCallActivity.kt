package com.cometchat.pro.uikit.ui_components.calls.call_manager

import android.Manifest
import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cometchat.pro.constants.CometChatConstants
import com.cometchat.pro.core.Call
import com.cometchat.pro.core.CometChat
import com.cometchat.pro.core.CometChat.CallbackListener
import com.cometchat.pro.exceptions.CometChatException
import com.cometchat.pro.uikit.ui_components.shared.cometchatAvatar.CometChatAvatar
import com.cometchat.pro.uikit.R
import com.cometchat.pro.uikit.ui_components.calls.call_manager.helper.CometChatAudioHelper
import com.cometchat.pro.uikit.ui_components.calls.call_manager.helper.OutgoingAudioHelper
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.cometchat.pro.uikit.ui_resources.constants.UIKitConstants
import com.cometchat.pro.uikit.ui_resources.utils.AnimUtil
import com.cometchat.pro.uikit.ui_resources.utils.ErrorMessagesUtils
import com.cometchat.pro.uikit.ui_resources.utils.Utils

/**
 * CometChatCallActivity.class is a activity class which is used to laod the incoming and outgoing
 * call screens. It is used to handle the audio and video call.
 *
 * Created At : 29th March 2020
 *
 * Modified On : 29th March 2020
 *
 */
class CometChatCallActivity : AppCompatActivity(), View.OnClickListener {
    private val TAG = CometChatCallActivity::class.java.simpleName
    private var callTv: TextView? = null

    //Incoming Call Screen
    private var callerName: TextView? = null
    private var callMessage: TextView? = null
    private var callerAvatar: CometChatAvatar? = null
    private var acceptCall: MaterialButton? = null
    private var declineCall: MaterialButton? = null
    private var incomingCallView: MaterialCardView? = null

    //
    //Outgoing call
    private var outgoingCallView: RelativeLayout? = null
    private var userTv: TextView? = null
    private var tvDots: TextView? = null
    private var userAv: CometChatAvatar? = null
    private var hangUp: FloatingActionButton? = null

    //
    private var sessionId: String? = null
    private var avatar: String? = null
    private var name: String? = null
    private var isVideo = false
    private var isIncoming = false
    private var isOngoing = false
    private var notification: Uri? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        callActivity = this
        handleIntent()
        setContentView(R.layout.activity_cometchat_callmanager)
        handleIntent()
        initView()
        setValues()
    }

    /**
     * This method is used to handle the intent values received to this class. Based on this intent
     * value it handles call.
     */
    private fun handleIntent() {
        val intent = intent
        if (intent.hasExtra(UIKitConstants.IntentStrings.JOIN_ONGOING)) {
            isOngoing = intent.getBooleanExtra(UIKitConstants.IntentStrings.JOIN_ONGOING, false)
        }
        if (intent.hasExtra(UIKitConstants.IntentStrings.ID)) {
            val id = intent.getStringExtra(UIKitConstants.IntentStrings.ID)
        }
        if (intent.hasExtra(UIKitConstants.IntentStrings.SESSION_ID)) {
            sessionId = intent.getStringExtra(UIKitConstants.IntentStrings.SESSION_ID)
        }
        if (intent.hasExtra(UIKitConstants.IntentStrings.AVATAR)) {
            avatar = intent.getStringExtra(UIKitConstants.IntentStrings.AVATAR)
        }
        if (intent.hasExtra(UIKitConstants.IntentStrings.NAME)) {
            name = intent.getStringExtra(UIKitConstants.IntentStrings.NAME)
        }
        if (!isOngoing) {
            try {
                isVideo = intent.action == CometChatConstants.CALL_TYPE_VIDEO
                isIncoming = intent.type == UIKitConstants.IntentStrings.INCOMING
                if (isIncoming) setTheme(R.style.TransparentCompat) else setTheme(R.style.AppTheme)
            } catch (e: NullPointerException) {
                e.printStackTrace()
            }
        }
    }

    /**
     * This method is used to initialize the view of this activity class.
     */
    private fun initView() {
        callerName = findViewById(R.id.caller_name)
        callMessage = findViewById(R.id.call_type)
        callerAvatar = findViewById(R.id.caller_av)
        acceptCall = findViewById(R.id.accept_incoming)
        acceptCall!!.setOnClickListener(this)
        declineCall = findViewById(R.id.decline_incoming)
        declineCall!!.setOnClickListener(this)
        incomingCallView = findViewById(R.id.incoming_call_view)
        outgoingCallView = findViewById(R.id.outgoing_call_view)
        callTv = findViewById(R.id.calling_tv)
        userTv = findViewById(R.id.user_tv)
        userAv = findViewById(R.id.user_av)
        hangUp = findViewById(R.id.call_hang_btn)
        tvDots = findViewById(R.id.tv_dots)
        hangUp!!.setOnClickListener(this)
        hangUp!!.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.red_600))
        mainView = findViewById(R.id.main_view)
        cometChatAudioHelper = CometChatAudioHelper(this)
        cometChatAudioHelper!!.initAudio()
        val packageName = packageName
        notification = Uri.parse("android.resource://" + packageName + "/" + R.raw.incoming_call)
        setCallType(isVideo, isIncoming)
        if (!Utils.hasPermissions(this, Manifest.permission.RECORD_AUDIO) && !Utils.hasPermissions(this, Manifest.permission.CAMERA)) {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA), REQUEST_PERMISSION)
        }
    }

    /**
     * This method is used to set the values recieve from `handleIntent()`.
     */
    private fun setValues() {
        if (isOngoing) {
            cometChatAudioHelper!!.stop(false)
            if (CometChat.getActiveCall() != null) Utils.startCall(this, CometChat.getActiveCall(), mainView) else onBackPressed()
        }
        userTv!!.text = name
        callerName!!.text = name
        userAv!!.setAvatar(avatar)
        callerAvatar!!.setAvatar(avatar)
    }

    /**
     * This method is used to set the call type by checking the parameter passed in this method.
     * It also sets the ringtone or calltone based on call type.
     *
     * @param isVideoCall is a boolean, It helps to identify whether call is Audio call or Video Call
     * @param isIncoming is a boolean, It helps to identify whether call is incoming or outgoing.
     *
     * @see CometChatAudioHelper
     */
    fun setCallType(isVideoCall: Boolean, isIncoming: Boolean) {
        AnimUtil.blinkAnimation(tvDots!!)
        if (isIncoming) {
            cometChatAudioHelper!!.startIncomingAudio(notification, true)
            incomingCallView!!.visibility = View.VISIBLE
            outgoingCallView!!.visibility = View.GONE
            if (isVideoCall) {
                callMessage!!.text = resources.getString(R.string.incoming_video_call)
                callMessage!!.setCompoundDrawablesWithIntrinsicBounds(resources.getDrawable(R.drawable.ic_videocam_white_24dp), null, null, null)
            } else {
                callMessage!!.text = resources.getString(R.string.incoming_audio_call)
                callMessage!!.setCompoundDrawablesWithIntrinsicBounds(resources.getDrawable(R.drawable.ic_call_incoming_24dp), null, null, null)
            }
        } else {
            callTv!!.text = getString(R.string.calling)
            cometChatAudioHelper!!.startOutgoingAudio(OutgoingAudioHelper.Type.IN_COMMUNICATION)
            incomingCallView!!.visibility = View.GONE
            outgoingCallView!!.visibility = View.VISIBLE
            hangUp!!.visibility = View.VISIBLE
            if (isVideoCall) {
                hangUp!!.setImageDrawable(resources.getDrawable(R.drawable.ic_videocam_white_24dp))
            } else {
                hangUp!!.setImageDrawable(resources.getDrawable(R.drawable.ic_call_end_white_24dp))
            }
        }
        if (supportActionBar != null) supportActionBar!!.hide()
    }

    /**
     * This method is used to handle the click events of the views present in this activity.
     *
     * @param v is object of View, It is used to identify the view which is clicked and based on it
     * perform certain actions.
     */
    override fun onClick(v: View) {
        val id = v.id
        if (id == R.id.call_hang_btn) {
            cometChatAudioHelper!!.stop(false)
            AnimUtil.stopBlinkAnimation(tvDots!!)
            rejectCall(sessionId, CometChatConstants.CALL_STATUS_CANCELLED)
        } else if (id == R.id.accept_incoming) {
            cometChatAudioHelper!!.stop(false)
            incomingCallView!!.visibility = View.GONE
            answerCall(mainView, sessionId)
        } else if (id == R.id.decline_incoming) {
            cometChatAudioHelper!!.stop(true)
            rejectCall(sessionId, CometChatConstants.CALL_STATUS_REJECTED)
            finish()
        }
    }

    /**
     * This methof is used to reject the call.
     *
     * @param sessionId is a String, It is call session Id.
     * @param callStatus is a String, It the reason for call being rejected.
     *
     * @see CometChat.rejectCall
     * @see Call
     */
    private fun rejectCall(sessionId: String?, callStatus: String) {
        CometChat.rejectCall(sessionId!!, callStatus, object : CallbackListener<Call?>() {
            override fun onSuccess(call: Call?) {
                finish()
            }

            override fun onError(e: CometChatException) {
                finish()
                Log.e(TAG, "onErrorReject: " + e.message + " " + e.code)
                ErrorMessagesUtils.cometChatErrorMessage(this@CometChatCallActivity, e.code)
            }
        })
    }

    /**
     * This method is used to accept the incoming call receievd.
     *
     * @param mainView is a object of Relativelayout, It is used to load the CallingComponent after
     * the call is accepted.
     * @param sessionId is a String, It is sessionId of call.
     *
     * @see CometChat.acceptCall
     * @see Call
     */
    private fun answerCall(mainView: RelativeLayout?, sessionId: String?) {
        CometChat.acceptCall(sessionId!!, object : CallbackListener<Call>() {
            override fun onSuccess(call: Call) {
                Log.e("CallMeta", call.toString())
                startCall(mainView, call)
            }

            override fun onError(e: CometChatException) {
                ErrorMessagesUtils.cometChatErrorMessage(this@CometChatCallActivity, e.code)
                finish()
                Log.e(TAG, "onErrorAccept: " + e.message + " " + e.code)

            }
        })
    }

    /**
     * This method is used to start the call after the call is accepted from both the end.
     * Here we are calling `Utils.startCall()` as it is being used for other purpose
     * also.
     * @param mainView is a object of RelativeLayout where the Calling Component will be loaded.
     * @param call is a object of Call.
     *
     * @see CometChat.startCall
     */
    private fun startCall(mainView: RelativeLayout?, call: Call) {
        hangUp!!.visibility = View.GONE
        Utils.startCall(this@CometChatCallActivity, call, mainView)
    }

    fun startOnGoingCall(call: Call?) {}
    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        cometChatAudioHelper!!.stop(false)
    }

    companion object {
        private const val REQUEST_PERMISSION = 1
        var mainView: RelativeLayout? = null
        var cometChatAudioHelper: CometChatAudioHelper? = null

        @SuppressLint("StaticFieldLeak")
        var callActivity: CometChatCallActivity? = null
    }
}