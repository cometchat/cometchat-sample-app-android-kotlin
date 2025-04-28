package com.cometchat.sampleapp.kotlin.fcm.voip

import android.content.Context
import android.content.Intent
import android.os.Process
import android.telecom.Connection
import android.telecom.DisconnectCause
import android.util.Log
import com.cometchat.calls.core.CometChatCalls
import com.cometchat.calls.listeners.CometChatCallsEventsListener
import com.cometchat.calls.model.AudioMode
import com.cometchat.calls.model.CallSwitchRequestInfo
import com.cometchat.calls.model.RTCMutedUser
import com.cometchat.calls.model.RTCRecordingInfo
import com.cometchat.calls.model.RTCUser
import com.cometchat.chat.core.Call
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chatuikit.calls.CometChatOngoingCallActivity
import com.cometchat.chatuikit.logger.CometChatLogger
import com.cometchat.sampleapp.kotlin.fcm.R
import com.cometchat.sampleapp.kotlin.fcm.data.repository.Repository
import com.cometchat.sampleapp.kotlin.fcm.utils.MyApplication
import kotlin.system.exitProcess

class CometChatVoIPConnection(private val context: Context) : Connection(), CometChatCallsEventsListener {
    // Constructor to pass context
    init {
        CometChatCalls.addCallsEventListeners(TAG, this)
    }

    override fun onAbort() {
        super.onAbort()
        CometChatLogger.e(TAG, "onAbort: ")
    }

    override fun onAnswer() {
        super.onAnswer()
        setDisconnected(DisconnectCause(DisconnectCause.CANCELED, "Accepted"))
        val extras = extras
        val receiverUid = extras.getString("receiverUid")
        val receiverType = extras.getString("receiverType")
        val sessionId = extras.getString("sessionId")
        val callType = extras.getString("callType")

        if (receiverUid == null || receiverType == null || sessionId == null || callType == null) {
            Log.e(TAG, "onAnswer: receiverUid, receiverType, sessionId, or callType is null")
            return
        }
        val call = Call(receiverUid, receiverType, callType)
        call.sessionId = sessionId

        Repository.acceptCall(call, object : CometChat.CallbackListener<Call>() {
            override fun onSuccess(call: Call) {
                val intent = Intent(context, CometChatOngoingCallActivity::class.java)
                CometChatVoIPUtils.isCallOngoing = true
                intent.putExtra(context.getString(R.string.app_session_id), call.sessionId)
                intent.putExtra(context.getString(R.string.app_call_type), call.type)
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }

            override fun onError(e: CometChatException) {
                CometChatVoIPUtils.isCallOngoing = false
            }
        })
    }

    override fun onReject() {
        super.onReject()
        val extras = extras
        val receiverUid = extras.getString("receiverUid")
        val receiverType = extras.getString("receiverType")
        val sessionId = extras.getString("sessionId")
        val callType = extras.getString("callType")

        if (receiverUid == null || receiverType == null || sessionId == null || callType == null) {
            CometChatLogger.e(TAG, "onReject: receiverUid, receiverType, sessionId, or callType is null")
            return
        }
        val call = Call(receiverUid, receiverType, callType)
        call.sessionId = sessionId
        Repository.rejectCall(call, object : CometChat.CallbackListener<Call>() {
            override fun onSuccess(call: Call) {
            }

            override fun onError(e: CometChatException) {
            }
        })
        setDisconnected(DisconnectCause(DisconnectCause.REJECTED, "Rejected"))
        CometChatVoIPUtils.isCallOngoing = false
    }

    private fun killAppAndClearTask() {
        try {
            MyApplication.currentActivity?.finishAffinity()
            Process.killProcess(Process.myPid())
            exitProcess(0)
        } catch (e: Exception) {
            CometChatLogger.e("KillApp", "Error while clearing task or killing app: " + e.message)
        }
    }

    override fun onCallEnded() {
        setDisconnected(DisconnectCause(DisconnectCause.CANCELED, "Canceled"))
        CometChatVoIPUtils.isCallOngoing = false
    }

    override fun onCallEndButtonPressed() {
        setDisconnected(DisconnectCause(DisconnectCause.CANCELED, "Canceled"))
        CometChatVoIPUtils.isCallOngoing = false
    }

    override fun onSessionTimeout() {
        setDisconnected(DisconnectCause(DisconnectCause.CANCELED, "Canceled"))
        CometChatVoIPUtils.isCallOngoing = false
    }

    override fun onUserJoined(rtcUser: RTCUser) {
    }

    override fun onUserLeft(rtcUser: RTCUser) {
    }

    override fun onUserListChanged(arrayList: ArrayList<RTCUser>) {
    }

    override fun onAudioModeChanged(arrayList: ArrayList<AudioMode>) {
    }

    override fun onCallSwitchedToVideo(callSwitchRequestInfo: CallSwitchRequestInfo) {
    }

    override fun onUserMuted(rtcMutedUser: RTCMutedUser) {
    }

    override fun onRecordingToggled(rtcRecordingInfo: RTCRecordingInfo) {
    }

    override fun onError(e: com.cometchat.calls.exceptions.CometChatException) {
    }

    companion object {
        private val TAG: String = CometChatVoIPConnection::class.java.simpleName
    }
}
