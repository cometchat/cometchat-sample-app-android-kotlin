package com.cometchat.uikit.core.domain.usecase

import com.cometchat.calls.model.CallLog
import com.cometchat.calls.model.CallUser
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.Call
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Use case for initiating a call based on a call log.
 * Determines the correct receiver based on who initiated the original call.
 */
open class InitiateCallUseCase {
    
    /**
     * Initiates a call based on the call log and call type.
     * Determines the receiver - if logged-in user was initiator, call the receiver; 
     * otherwise call the initiator.
     * 
     * @param callLog The call log to base the new call on
     * @param callType The type of call to initiate (audio/video)
     * @return Result containing the initiated Call on success, or exception on failure
     */
    open suspend operator fun invoke(callLog: CallLog, callType: String): Result<Call> {
        val receiverId = getReceiverId(callLog)
        
        if (receiverId.isEmpty()) {
            return Result.failure(
                CometChatException(
                    "INVALID_RECEIVER",
                    "Could not determine receiver for call"
                )
            )
        }
        
        return suspendCoroutine { continuation ->
            val call = Call(receiverId, CometChatConstants.RECEIVER_TYPE_USER, callType)
            CometChat.initiateCall(call, object : CometChat.CallbackListener<Call>() {
                override fun onSuccess(initiatedCall: Call) {
                    continuation.resume(Result.success(initiatedCall))
                }
                
                override fun onError(exception: CometChatException) {
                    continuation.resume(Result.failure(exception))
                }
            })
        }
    }
    
    /**
     * Determines the receiver ID for the call.
     * If the logged-in user was the initiator of the original call, 
     * the receiver becomes the target. Otherwise, the initiator becomes the target.
     * 
     * @param callLog The call log to determine receiver from
     * @return The UID of the user to call
     */
    private fun getReceiverId(callLog: CallLog): String {
        val initiator = callLog.initiator as? CallUser
        val receiver = callLog.receiver as? CallUser
        val loggedInUserId = CometChat.getLoggedInUser()?.uid
        
        return if (initiator?.uid == loggedInUserId) {
            receiver?.uid ?: ""
        } else {
            initiator?.uid ?: ""
        }
    }
}
