package com.cometchat.sampleapp.kotlin.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.cometchat.calls.model.CallLog
import com.cometchat.calls.model.CallUser
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.Call
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.User
import com.cometchat.chatuikit.calls.utils.CallUtils
import com.cometchat.sampleapp.kotlin.data.repository.Repository

class CallsFragmentViewModel : ViewModel() {
    private val onCallStart = MutableLiveData<Call>()
    private val onError = MutableLiveData<CometChatException>()

    fun onCallStart(): MutableLiveData<Call> {
        return onCallStart
    }

    fun onError(): MutableLiveData<CometChatException> {
        return onError
    }

    fun startCall(
        callType: String, callLog: CallLog, listener: CometChat.CallbackListener<Void>
    ) {
        val initiator = callLog.initiator as CallUser
        val isLoggedInUser = CallUtils.isLoggedInUser(initiator)
        val user = if (isLoggedInUser) {
            callLog.receiver as CallUser
        } else {
            initiator
        }
        Repository.getUser(user.uid, object : CometChat.CallbackListener<User>() {
            override fun onSuccess(userObj: User) {
                if (userObj.isBlockedByMe) {
                    listener.onError(CometChatException("BLOCKED_BY_ME", "Call cannot be initiated as user is blocked"))
                    onError.setValue(CometChatException("BLOCKED_BY_ME", "Call cannot be initiated as user is blocked"))
                } else if (userObj.isHasBlockedMe) {
                    listener.onError(CometChatException("BLOCKED_BY_ME", "Call cannot be initiated as user has blocked you"))
                    onError.setValue(CometChatException("BLOCKED_BY_ME", "Call cannot be initiated as user has blocked you"))
                } else {
                    startCall(callType, userObj, listener)
                }
            }

            override fun onError(e: CometChatException) {
                listener.onError(e)
                onError.value = e
            }
        })
    }

    private fun startCall(
        callType: String, user: User, listener: CometChat.CallbackListener<Void>
    ) {
        val call = Call(user.uid, CometChatConstants.RECEIVER_TYPE_USER, callType)
        Repository.initiateCall(call, object : CometChat.CallbackListener<Call>() {
            override fun onSuccess(call: Call) {
                listener.onSuccess(null)
                onCallStart.value = call
            }

            override fun onError(e: CometChatException) {
                listener.onError(e)
                onError.value = e
            }
        })
    }
}
