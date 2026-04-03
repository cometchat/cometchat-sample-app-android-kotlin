package com.cometchat.uikit.core.utils

import com.cometchat.calls.constants.CometChatCallsConstants
import com.cometchat.calls.model.CallGroup
import com.cometchat.calls.model.CallLog
import com.cometchat.calls.model.CallUser
import com.cometchat.chat.core.CometChat

/**
 * Utility functions for call log classification and data extraction.
 * Provides helper methods for determining call direction, type, and participant info.
 */
object CallLogsUtils {
    
    /**
     * Determines if the call was outgoing (initiated by the logged-in user).
     * 
     * @param callLog The call log to check
     * @return true if the logged-in user initiated the call, false otherwise
     */
    fun isOutgoingCall(callLog: CallLog): Boolean {
        val initiator = callLog.initiator as? CallUser
        val loggedInUserId = CometChat.getLoggedInUser()?.uid
        return initiator?.uid == loggedInUserId
    }
    
    /**
     * Determines if the call was missed or unanswered.
     * A call is considered missed if it was incoming and has status UNANSWERED or MISSED.
     * 
     * @param callLog The call log to check
     * @return true if the call was missed/unanswered, false otherwise
     */
    fun isMissedCall(callLog: CallLog): Boolean {
        // Only incoming calls can be "missed" from the user's perspective
        if (isOutgoingCall(callLog)) return false
        
        return callLog.status == CometChatCallsConstants.CALL_STATUS_UNANSWERED ||
               callLog.status == CometChatCallsConstants.CALL_STATUS_MISSED ||
               callLog.status == CometChatCallsConstants.CALL_STATUS_CANCELLED
    }
    
    /**
     * Determines if the call was incoming (received by the logged-in user).
     * An incoming call is one that was not initiated by the logged-in user and was not missed.
     * 
     * @param callLog The call log to check
     * @return true if the call was incoming and answered, false otherwise
     */
    fun isIncomingCall(callLog: CallLog): Boolean {
        return !isOutgoingCall(callLog) && !isMissedCall(callLog)
    }
    
    /**
     * Determines if the call was an audio-only call.
     * 
     * @param callLog The call log to check
     * @return true if the call was audio-only, false otherwise
     */
    fun isAudioCall(callLog: CallLog): Boolean {
        return callLog.type == CometChatCallsConstants.CALL_TYPE_AUDIO
    }
    
    /**
     * Determines if the call was a video call.
     * Includes both video and audio_video types.
     * 
     * @param callLog The call log to check
     * @return true if the call was a video call, false otherwise
     */
    fun isVideoCall(callLog: CallLog): Boolean {
        return callLog.type == CometChatCallsConstants.CALL_TYPE_VIDEO ||
               callLog.type == CometChatCallsConstants.CALL_TYPE_AUDIO_VIDEO
    }
    
    /**
     * Gets the display name for the call log.
     * Returns the name of the other participant (not the logged-in user).
     * Handles both user-to-user and group calls.
     * 
     * @param callLog The call log to get the name from
     * @return The display name of the other participant
     */
    fun getDisplayName(callLog: CallLog): String {
        // Handle group calls
        if (callLog.receiverType == CometChatCallsConstants.RECEIVER_TYPE_GROUP) {
            val receiver = callLog.receiver as? CallGroup
            return receiver?.name ?: ""
        }
        
        // Handle user-to-user calls
        val initiator = callLog.initiator as? CallUser
        val receiver = callLog.receiver as? CallUser
        val loggedInUserId = CometChat.getLoggedInUser()?.uid
        
        return if (initiator?.uid == loggedInUserId) {
            receiver?.name ?: ""
        } else {
            initiator?.name ?: ""
        }
    }
    
    /**
     * Gets the avatar URL for the call log.
     * Returns the avatar of the other participant (not the logged-in user).
     * Handles both user-to-user and group calls.
     * 
     * @param callLog The call log to get the avatar from
     * @return The avatar URL of the other participant, or null if not available
     */
    fun getAvatarUrl(callLog: CallLog): String? {
        // Handle group calls
        if (callLog.receiverType == CometChatCallsConstants.RECEIVER_TYPE_GROUP) {
            val receiver = callLog.receiver as? CallGroup
            return receiver?.icon
        }
        
        // Handle user-to-user calls
        val initiator = callLog.initiator as? CallUser
        val receiver = callLog.receiver as? CallUser
        val loggedInUserId = CometChat.getLoggedInUser()?.uid
        
        return if (initiator?.uid == loggedInUserId) {
            receiver?.avatar
        } else {
            initiator?.avatar
        }
    }
    
    /**
     * Gets the UID of the other participant in the call.
     * For group calls, returns the group GUID.
     * 
     * @param callLog The call log to get the UID from
     * @return The UID of the other participant or group GUID
     */
    fun getOtherParticipantUid(callLog: CallLog): String {
        // Handle group calls
        if (callLog.receiverType == CometChatCallsConstants.RECEIVER_TYPE_GROUP) {
            val receiver = callLog.receiver as? CallGroup
            return receiver?.guid ?: ""
        }
        
        // Handle user-to-user calls
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
