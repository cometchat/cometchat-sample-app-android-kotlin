package com.cometchat.sampleapp.kotlin.fcm.fcm

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.telecom.PhoneAccount
import android.telecom.TelecomManager
import android.telecom.VideoProfile
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.Call
import com.cometchat.chat.core.CometChat
import com.cometchat.chatuikit.logger.CometChatLogger
import com.cometchat.sampleapp.kotlin.fcm.data.repository.Repository
import com.cometchat.sampleapp.kotlin.fcm.ui.activity.SplashActivity
import com.cometchat.sampleapp.kotlin.fcm.utils.AppConstants
import com.cometchat.sampleapp.kotlin.fcm.utils.MyApplication
import com.cometchat.sampleapp.kotlin.fcm.voip.CometChatVoIP
import com.cometchat.sampleapp.kotlin.fcm.voip.CometChatVoIPUtils
import com.cometchat.sampleapp.kotlin.fcm.voip.interfaces.VoIPPermissionListener
import com.cometchat.sampleapp.kotlin.fcm.voip.model.CometChatVoIPError
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson

class FCMService : FirebaseMessagingService() {

    companion object {
        private val TAG: String = FCMService::class.java.simpleName
        var fCMToken: String? = null
            private set
        const val CALL_STATUS_INITIATED: String = "initiated"
        const val CALL_STATUS_CANCELLED: String = "cancelled"
        const val CALL_STATUS_UNANSWERED: String = "unanswered"
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        CometChatLogger.e(TAG, "onMessageReceived:>>>>" + Gson().toJson(message.data))
        if (message.data.isEmpty()) {
            CometChatLogger.e(TAG, "onMessageReceived: No data payload in message.")
            return
        }

        try {
            if (message.data.containsKey("type")) {
                val type = message.data["type"]
                if ("chat".equals(type, ignoreCase = true)) {
                    val fcmMessageDTO: FCMMessageDTO = Gson().fromJson(
                        Gson().toJson(message.data), FCMMessageDTO::class.java
                    )
                    CometChat.markAsDelivered(
                        fcmMessageDTO.tag!!.toInt(),
                        fcmMessageDTO.sender!!,
                        fcmMessageDTO.receiverType!!,
                        fcmMessageDTO.receiver!!
                    )
                    val isUser = fcmMessageDTO.receiverType == CometChatConstants.RECEIVER_TYPE_USER
                    val uid: String = if (isUser) fcmMessageDTO.sender!! else fcmMessageDTO.receiver!!
                    if (uid != MyApplication.currentOpenChatId) {
                        CometChatLogger.e(TAG, "onMessageReceived: " + Gson().toJson(fcmMessageDTO))
                        val onNotificationClickIntent = Intent(this, SplashActivity::class.java)
                        FCMMessageNotificationUtils.showNotification(
                            this,
                            fcmMessageDTO,
                            onNotificationClickIntent,
                            AppConstants.FCMConstants.NOTIFICATION_KEY_REPLY_ACTION,
                            NotificationCompat.CATEGORY_MESSAGE
                        )
                    }
                } else if ("call".equals(type, ignoreCase = true)) {
                    val sessionId = message.data["sessionId"]
                    val callAction = message.data["callAction"]
                    if (!MyApplication.isAppInForeground() && MyApplication.getTempCall() != null &&
                        (MyApplication
                            .getTempCall()
                            ?.sessionId
                            == sessionId) && (CometChatConstants.CALL_STATUS_CANCELLED == callAction || CometChatConstants.CALL_STATUS_UNANSWERED == callAction)
                    ) {
                        MyApplication.setTempCall(null)
                    }
                    if (!CometChatVoIP.hasReadPhoneStatePermission(this)) {
                        return
                    }

                    if (!CometChatVoIP.hasManageOwnCallsPermission(this)) {
                        return
                    }

                    if (!CometChatVoIP.hasAnswerPhoneCallsPermission(this)) {
                        return
                    }

                    if (CometChatVoIP.hasReadPhoneStatePermission(this) && CometChatVoIP.hasManageOwnCallsPermission(
                            this
                        ) && CometChatVoIP.hasAnswerPhoneCallsPermission(
                            this
                        )
                    ) {
                        CometChatVoIP.hasEnabledPhoneAccountForVoIP(this, object : VoIPPermissionListener {
                            override fun onPermissionsGranted() {
                                CometChatLogger.e(TAG, "VoIP Permissions granted")
                                handleCallFlow(message)
                            }

                            override fun onPermissionsDenied(error: CometChatVoIPError?) {
                                CometChatLogger.e(TAG, "VoIP Permissions denied: " + error?.message)
                            }
                        })
                    } else {
                        CometChatLogger.e(TAG, "All VoIP Permissions denied.")
                    }
                } else {
                    CometChatLogger.e(TAG, "onMessageReceived: Unsupported message type: $type")
                }
            }
        } catch (e: Exception) {
            CometChatLogger.e(TAG, "onMessageReceived: Error processing message: " + e.message)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        fCMToken = token
        CometChatLogger.e(TAG, "onNewToken: FCM Token: $fCMToken")
    }

    private fun configureVoIP(callData: FCMCallDto) {
        CometChatVoIP.init(this, applicationInfo.loadLabel(packageManager).toString())
    }

    private fun showIncomingCallScreen(callData: FCMCallDto) {
        voipIncomingCall(callData)
    }

    private fun handleCallFlow(message: RemoteMessage) {
        val gson = Gson()
        val callData: FCMCallDto = gson.fromJson(gson.toJson(message.data), FCMCallDto::class.java)
        configureVoIP(callData) // Check if the call action is "initiated"
        if (CALL_STATUS_INITIATED == callData.callAction) {
            showIncomingCallScreen(callData)
        } else if (CALL_STATUS_CANCELLED == callData.callAction || CALL_STATUS_UNANSWERED == callData.callAction) {
            if (CometChatVoIPUtils.currentSessionId == callData.sessionId) {
                if (ActivityCompat.checkSelfPermission(
                        this@FCMService, Manifest.permission.ANSWER_PHONE_CALLS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    CometChatLogger.e(
                        TAG, "Error: Permission ANSWER_PHONE_CALLS denied to end call."
                    )
                    return
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    CometChatLogger.e(TAG, "Error: " + callData.callAction)
                    CometChatVoIP.telecomManager?.endCall()
                }
            }
        }
    }

    private fun voipIncomingCall(callData: FCMCallDto) {
        if (MyApplication.isAppInForeground()) {
            CometChatLogger.e(TAG, "Call ignored as app is in the foreground.")
            return
        }
        if (CometChat.getActiveCall() != null || CometChatVoIPUtils.isCallOngoing) {
            @SuppressLint("WrongConstant") val call = Call(callData.receiver!!, callData.receiverType, callData.callType)
            call.sessionId = callData.sessionId
            Repository.rejectCallWithBusyStatus(call, null)
        } else {
            val extras = Bundle()
            val uri = Uri.fromParts(PhoneAccount.SCHEME_TEL, "", null)
            extras.putInt(
                TelecomManager.EXTRA_INCOMING_VIDEO_STATE,
                if (callData.callType == "audio") VideoProfile.STATE_AUDIO_ONLY else VideoProfile.STATE_BIDIRECTIONAL
            )
            extras.putParcelable(TelecomManager.EXTRA_INCOMING_CALL_ADDRESS, uri)
            extras.putBoolean(TelecomManager.EXTRA_START_CALL_WITH_SPEAKERPHONE, false)
            extras.putParcelable(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, CometChatVoIP.phoneAccountHandle)

            extras.putString("callerName", callData.senderName)
            extras.putString("callerAvatar", callData.senderAvatar)
            extras.putString("receiverUid", callData.receiver)
            extras.putString("receiverType", callData.receiverType)
            extras.putString("sessionId", callData.sessionId)
            extras.putString("callType", callData.callType)

            CometChatVoIPUtils.currentSessionId = callData.sessionId!!
            CometChatVoIP.addNewIncomingCall(this, extras)
        }
    }
}
