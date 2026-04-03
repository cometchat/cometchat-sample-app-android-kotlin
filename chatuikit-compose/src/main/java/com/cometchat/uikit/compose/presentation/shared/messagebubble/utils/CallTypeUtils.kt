package com.cometchat.uikit.compose.presentation.shared.messagebubble.utils

import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.Call
import com.cometchat.chat.models.User
import com.cometchat.uikit.compose.R
import com.cometchat.uikit.core.CometChatUIKit

/**
 * Enum representing different call types for display in call action bubbles.
 */
enum class CallType {
    AUDIO_INCOMING,
    AUDIO_OUTGOING,
    VIDEO_INCOMING,
    VIDEO_OUTGOING,
    AUDIO_MISSED,
    VIDEO_MISSED
}

/**
 * Determines the call type based on the Call object properties.
 *
 * @param call The Call object
 * @return The corresponding [CallType] enum value
 */
fun getCallType(call: Call): CallType {
    val loggedInUserId = CometChatUIKit.getLoggedInUser()?.uid
    val initiatorUid = (call.callInitiator as? User)?.uid
    return getCallType(call.type, call.callStatus, initiatorUid, loggedInUserId)
}

/**
 * Determines the call type based on call properties.
 * This overload is useful for testing without SDK dependencies.
 *
 * Missed call logic (per Java reference):
 * - A call is considered "missed" when:
 *   1. Call status is UNANSWERED, AND
 *   2. The logged-in user is NOT the call initiator (they received the call but didn't answer)
 * - If the logged-in user initiated the call and it was unanswered, it's an outgoing call (not missed)
 *
 * @param callType The call type (audio/video) from CometChatConstants
 * @param callStatus The call status from CometChatConstants
 * @param initiatorUid The UID of the call initiator
 * @param loggedInUserId The UID of the currently logged-in user
 * @return The corresponding [CallType] enum value
 */
fun getCallType(
    callType: String?,
    callStatus: String?,
    initiatorUid: String?,
    loggedInUserId: String?
): CallType {
    val isVideo = callType == CometChatConstants.CALL_TYPE_VIDEO
    
    // isIncomingCall: true if logged-in user IS the initiator (they made the call)
    // Per Java reference: isIncomingCall = user.getUid().equals(CometChatUIKit.getLoggedInUser().getUid())
    val isInitiator = initiatorUid == loggedInUserId
    
    // Missed call: status is UNANSWERED AND logged-in user is NOT the initiator
    // This means the user received a call but didn't answer it
    val isMissed = callStatus == CometChatConstants.CALL_STATUS_UNANSWERED && !isInitiator
    
    // For initiated status, determine direction based on who initiated
    val isInitiated = callStatus == CometChatConstants.CALL_STATUS_INITIATED
    
    return when {
        // Missed calls (unanswered incoming calls)
        isMissed && isVideo -> CallType.VIDEO_MISSED
        isMissed -> CallType.AUDIO_MISSED
        
        // Initiated calls - show direction based on initiator
        isInitiated && isInitiator && isVideo -> CallType.VIDEO_OUTGOING
        isInitiated && isInitiator -> CallType.AUDIO_OUTGOING
        isInitiated && isVideo -> CallType.VIDEO_INCOMING
        isInitiated -> CallType.AUDIO_INCOMING
        
        // Other statuses (ongoing, ended, etc.) - show based on initiator
        isInitiator && isVideo -> CallType.VIDEO_OUTGOING
        isInitiator -> CallType.AUDIO_OUTGOING
        isVideo -> CallType.VIDEO_INCOMING
        else -> CallType.AUDIO_INCOMING
    }
}

/**
 * Checks if the call type represents a missed call.
 *
 * @param callType The call type
 * @return True if the call was missed
 */
fun isMissedCall(callType: CallType): Boolean {
    return callType == CallType.AUDIO_MISSED || callType == CallType.VIDEO_MISSED
}

/**
 * Checks if the call type represents a video call.
 *
 * @param callType The call type
 * @return True if the call is a video call
 */
fun isVideoCall(callType: CallType): Boolean {
    return callType == CallType.VIDEO_INCOMING || 
           callType == CallType.VIDEO_OUTGOING || 
           callType == CallType.VIDEO_MISSED
}

/**
 * Checks if the call type represents an incoming call.
 *
 * @param callType The call type
 * @return True if the call is incoming
 */
fun isIncomingCall(callType: CallType): Boolean {
    return callType == CallType.AUDIO_INCOMING || callType == CallType.VIDEO_INCOMING
}

/**
 * Gets the drawable resource ID for a given call type.
 *
 * @param callType The call type
 * @return The drawable resource ID for the call type icon
 */
fun getCallTypeIcon(callType: CallType): Int {
    return when (callType) {
        CallType.AUDIO_INCOMING -> R.drawable.cometchat_ic_incoming_voice_call
        CallType.AUDIO_OUTGOING -> R.drawable.cometchat_ic_outgoing_voice_call
        CallType.VIDEO_INCOMING -> R.drawable.cometchat_ic_incoming_video_call
        CallType.VIDEO_OUTGOING -> R.drawable.cometchat_ic_outgoing_video_call
        CallType.AUDIO_MISSED -> R.drawable.cometchat_ic_missed_voice_call
        CallType.VIDEO_MISSED -> R.drawable.cometchat_ic_missed_video_call
    }
}

/**
 * Gets the display text for a given call type.
 *
 * @param callType The call type
 * @return The display text for the call type
 */
fun getCallTypeText(callType: CallType): String {
    return when (callType) {
        CallType.AUDIO_INCOMING -> "Incoming voice call"
        CallType.AUDIO_OUTGOING -> "Outgoing voice call"
        CallType.VIDEO_INCOMING -> "Incoming video call"
        CallType.VIDEO_OUTGOING -> "Outgoing video call"
        CallType.AUDIO_MISSED -> "Missed voice call"
        CallType.VIDEO_MISSED -> "Missed video call"
    }
}
