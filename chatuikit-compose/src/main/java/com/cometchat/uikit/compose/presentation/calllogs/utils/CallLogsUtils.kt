package com.cometchat.uikit.compose.presentation.calllogs.utils

import android.content.Context
import com.cometchat.calls.model.CallLog
import com.cometchat.uikit.compose.R
import com.cometchat.uikit.core.utils.CallLogsUtils as CoreCallLogsUtils

/**
 * Utility object for call log-related operations in Compose UI.
 * Provides helper methods for extracting and formatting call log data for display.
 * 
 * This extends the core CallLogsUtils with Compose-specific functionality
 * such as localized string formatting.
 */
object CallLogsUtils {
    
    /**
     * Gets the display name for the call log.
     * Returns the name of the other participant (not the logged-in user).
     * 
     * @param callLog The call log to get the name from
     * @return The display name of the other participant
     */
    fun getDisplayName(callLog: CallLog): String {
        return CoreCallLogsUtils.getDisplayName(callLog)
    }
    
    /**
     * Gets the avatar URL for the call log.
     * Returns the avatar of the other participant (not the logged-in user).
     * 
     * @param callLog The call log to get the avatar from
     * @return The avatar URL of the other participant, or null if not available
     */
    fun getAvatarUrl(callLog: CallLog): String? {
        return CoreCallLogsUtils.getAvatarUrl(callLog)
    }
    
    /**
     * Gets the UID of the other participant in the call.
     * 
     * @param callLog The call log to get the UID from
     * @return The UID of the other participant
     */
    fun getOtherParticipantUid(callLog: CallLog): String {
        return CoreCallLogsUtils.getOtherParticipantUid(callLog)
    }
    
    /**
     * Determines if the call was outgoing (initiated by the logged-in user).
     * 
     * @param callLog The call log to check
     * @return true if the logged-in user initiated the call, false otherwise
     */
    fun isOutgoingCall(callLog: CallLog): Boolean {
        return CoreCallLogsUtils.isOutgoingCall(callLog)
    }
    
    /**
     * Determines if the call was missed or unanswered.
     * 
     * @param callLog The call log to check
     * @return true if the call was missed/unanswered, false otherwise
     */
    fun isMissedCall(callLog: CallLog): Boolean {
        return CoreCallLogsUtils.isMissedCall(callLog)
    }
    
    /**
     * Determines if the call was incoming (received by the logged-in user).
     * 
     * @param callLog The call log to check
     * @return true if the call was incoming and answered, false otherwise
     */
    fun isIncomingCall(callLog: CallLog): Boolean {
        return CoreCallLogsUtils.isIncomingCall(callLog)
    }
    
    /**
     * Determines if the call was an audio-only call.
     * 
     * @param callLog The call log to check
     * @return true if the call was audio-only, false otherwise
     */
    fun isAudioCall(callLog: CallLog): Boolean {
        return CoreCallLogsUtils.isAudioCall(callLog)
    }
    
    /**
     * Determines if the call was a video call.
     * 
     * @param callLog The call log to check
     * @return true if the call was a video call, false otherwise
     */
    fun isVideoCall(callLog: CallLog): Boolean {
        return CoreCallLogsUtils.isVideoCall(callLog)
    }
    
    /**
     * Gets a localized string describing the call direction.
     * 
     * @param context Android context for accessing resources
     * @param callLog The call log to get the direction for
     * @return Localized string: "Outgoing", "Incoming", or "Missed"
     */
    fun getCallDirectionText(context: Context, callLog: CallLog): String {
        return when {
            isMissedCall(callLog) -> context.getString(R.string.cometchat_missed_call)
            isOutgoingCall(callLog) -> context.getString(R.string.cometchat_outgoing)
            else -> context.getString(R.string.cometchat_incoming)
        }
    }
    
    /**
     * Gets a localized string describing the call type.
     * 
     * @param context Android context for accessing resources
     * @param callLog The call log to get the type for
     * @return Localized string: "Video Call" or "Audio Call"
     */
    fun getCallTypeText(context: Context, callLog: CallLog): String {
        return if (isVideoCall(callLog)) {
            context.getString(R.string.cometchat_video_call)
        } else {
            context.getString(R.string.cometchat_audio_call)
        }
    }
    
    /**
     * Gets a complete accessibility description for the call log.
     * 
     * @param context Android context for accessing resources
     * @param callLog The call log to describe
     * @return Accessibility description string
     */
    fun getAccessibilityDescription(context: Context, callLog: CallLog): String {
        val displayName = getDisplayName(callLog)
        val callDirection = getCallDirectionText(context, callLog)
        val callType = getCallTypeText(context, callLog)
        return "$displayName, $callDirection, $callType"
    }
}
