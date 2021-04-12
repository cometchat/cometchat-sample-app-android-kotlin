package com.cometchat.pro.uikit.ui_components.calls.call_manager.listener

import android.content.Context
import android.widget.Toast
import com.cometchat.pro.constants.CometChatConstants
import com.cometchat.pro.core.Call
import com.cometchat.pro.core.CometChat
import com.cometchat.pro.core.CometChat.CallbackListener
import com.cometchat.pro.exceptions.CometChatException
import com.cometchat.pro.models.Group
import com.cometchat.pro.models.User
import com.cometchat.pro.uikit.ui_components.calls.call_manager.CometChatCallActivity
import com.cometchat.pro.uikit.ui_resources.utils.ErrorMessagesUtils
import com.cometchat.pro.uikit.ui_resources.utils.Utils

/**
 * CometChatCallListener.class is used to add and remove CallListener in app.
 * It also has method to make call to user passed in parameter;
 */
object CometChatCallListener {
    /**
     * This method is used to add CallListener in app
     * @param TAG is a unique Identifier
     * @param context is a object of Context.
     */
    fun addCallListener(TAG: String?, context: Context?) {
        CometChat.addCallListener(TAG!!, object : CometChat.CallListener() {
            override fun onIncomingCallReceived(call: Call) {
                if (CometChat.getActiveCall() == null) {
                    if (call.receiverType == CometChatConstants.RECEIVER_TYPE_USER) {
                        Utils.startCallIntent(context!!, call.callInitiator as User, call.type,
                                false, call.sessionId)
                    } else {
                        Utils.startGroupCallIntent(context!!, call.receiver as Group, call.type,
                                false, call.sessionId)
                    }
                } else {
                    CometChat.rejectCall(call.sessionId, CometChatConstants.CALL_STATUS_BUSY, object : CallbackListener<Call?>() {
                        override fun onSuccess(call: Call?) {}
                        override fun onError(e: CometChatException) {
//                            Toast.makeText(context, "Error:" + e.message, Toast.LENGTH_LONG).show()
                            if (context != null) {
                                ErrorMessagesUtils.cometChatErrorMessage(context, e.code)
                            }
                        }
                    })
                }
            }

            override fun onOutgoingCallAccepted(call: Call) {
                if (CometChatCallActivity.mainView != null) {
                    CometChatCallActivity.cometChatAudioHelper!!.stop(false)
                    Utils.startCall(CometChatCallActivity.callActivity!!, call, CometChatCallActivity.mainView)
                }
            }

            override fun onOutgoingCallRejected(call: Call) {
                if (CometChatCallActivity.callActivity != null) CometChatCallActivity.callActivity!!.finish()
            }

            override fun onIncomingCallCancelled(call: Call) {
                if (CometChatCallActivity.callActivity != null) CometChatCallActivity.callActivity!!.finish()
            }
        })
    }

    /**
     * It is used to remove call listener from app.
     * @param TAG is a unique Identifier
     */
    fun removeCallListener(TAG: String?) {
        CometChat.removeCallListener(TAG!!)
    }

    /**
     * This method is used to make a initiate a call.
     * @param context is a object of Context.
     * @param receiverId is a String, It is unique receiverId. It can be either uid of user or
     * guid of group
     * @param receiverType is a String, It can be either CometChatConstant.RECEIVER_TYPE_USER or
     * CometChatConstant.RECEIVER_TYPE_GROUP
     * @param callType is a String, It is call type which can be either CometChatConstant.CALL_TYPE_AUDIO
     * or CometChatConstant.CALL_TYPE_VIDEO
     *
     * @see CometChat.initiateCall
     */
    fun makeCall(context: Context?, receiverId: String?, receiverType: String?, callType: String?) {
        Utils.initiatecall(context!!, receiverId, receiverType, callType)
    }
}