package com.cometchat.sampleapp.kotlin.ui.calls

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.cometchat.calls.model.CallLog
import com.cometchat.calls.model.CallUser
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.Call
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.User

/**
 * ViewModel for CallsFragment handling call initiation logic.
 *
 * Manages the call initiation flow including:
 * - Determining the target user from call log
 * - Checking block status before initiating
 * - Initiating the call via CometChat SDK
 *
 * Validates: Requirements 9.3, 9.4
 */
class CallsFragmentViewModel : ViewModel() {
    
    private val onCallStart = MutableLiveData<Call>()
    private val onError = MutableLiveData<CometChatException>()

    fun onCallStart(): MutableLiveData<Call> = onCallStart

    fun onError(): MutableLiveData<CometChatException> = onError

    /**
     * Starts a call based on the call log.
     * Checks if the user is blocked before initiating the call.
     *
     * @param callType The type of call (audio/video)
     * @param callLog The call log to base the new call on
     * @param listener Callback for success/failure
     */
    fun startCall(
        callType: String,
        callLog: CallLog,
        listener: CometChat.CallbackListener<Void>
    ) {
        val initiator = callLog.initiator as? CallUser
        val loggedInUser = CometChat.getLoggedInUser()
        val isLoggedInUser = initiator?.uid == loggedInUser?.uid
        
        val targetUser = if (isLoggedInUser) {
            callLog.receiver as? CallUser
        } else {
            initiator
        }
        
        if (targetUser == null) {
            val exception = CometChatException("INVALID_USER", "Could not determine target user")
            listener.onError(exception)
            onError.value = exception
            return
        }
        
        CometChat.getUser(targetUser.uid, object : CometChat.CallbackListener<User>() {
            override fun onSuccess(userObj: User) {
                when {
                    userObj.isBlockedByMe -> {
                        val exception = CometChatException(
                            "BLOCKED_BY_ME",
                            "Call cannot be initiated as user is blocked"
                        )
                        listener.onError(exception)
                        onError.value = exception
                    }
                    userObj.isHasBlockedMe -> {
                        val exception = CometChatException(
                            "BLOCKED_BY_USER",
                            "Call cannot be initiated as user has blocked you"
                        )
                        listener.onError(exception)
                        onError.value = exception
                    }
                    else -> {
                        initiateCall(callType, userObj, listener)
                    }
                }
            }

            override fun onError(e: CometChatException) {
                listener.onError(e)
                onError.value = e
            }
        })
    }

    /**
     * Initiates a call to the specified user.
     *
     * @param callType The type of call (audio/video)
     * @param user The user to call
     * @param listener Callback for success/failure
     */
    private fun initiateCall(
        callType: String,
        user: User,
        listener: CometChat.CallbackListener<Void>
    ) {
        val call = Call(user.uid, CometChatConstants.RECEIVER_TYPE_USER, callType)
        CometChat.initiateCall(call, object : CometChat.CallbackListener<Call>() {
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
