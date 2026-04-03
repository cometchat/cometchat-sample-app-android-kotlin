package com.cometchat.uikit.compose.presentation.shared.utils

import android.content.Context
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.Call
import com.cometchat.chat.models.User
import com.cometchat.uikit.compose.R
import com.cometchat.uikit.core.CometChatUIKit
import com.cometchat.uikit.core.constants.UIKitConstants

/**
 * Utility object for call-related operations.
 *
 * Provides helper methods for formatting call information and determining call states.
 */
object CallsUtils {

    /**
     * Safely gets the logged-in user, returning null if SDK is not initialized (preview mode).
     */
    private fun getLoggedInUserSafe(): User? {
        return try {
            CometChatUIKit.getLoggedInUser()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Returns the caller name with optional separator for group calls.
     * For user-to-user calls, returns empty string.
     * For group calls, returns "You: " or "[Name]: " based on who initiated the call.
     *
     * @param context Android context for accessing resources
     * @param call The Call object
     * @param showSeparator Whether to include the ": " separator
     * @return Caller name with separator or empty string
     */
    fun getCallerName(context: Context, call: Call, showSeparator: Boolean = true): String {
        val separator = if (showSeparator) ": " else ""
        
        return if (call.receiverType == CometChatConstants.RECEIVER_TYPE_GROUP) {
            val initiator = call.callInitiator as? User
            val loggedInUser = getLoggedInUserSafe()
            if (loggedInUser != null && initiator?.uid == loggedInUser.uid) {
                context.getString(R.string.cometchat_you) + separator
            } else {
                (initiator?.name ?: "") + separator
            }
        } else {
            ""
        }
    }

    /**
     * Checks if the call was initiated by the current logged-in user.
     *
     * @param call The Call object
     * @return True if call was initiated by current user, false otherwise
     */
    fun isCallInitiatedByMe(call: Call): Boolean {
        val initiator = call.callInitiator as? User
        val loggedInUser = getLoggedInUserSafe()
        return loggedInUser != null && initiator?.uid == loggedInUser.uid
    }

    /**
     * Checks if the call is a video call.
     *
     * @param call The Call object
     * @return True if video call, false if audio call
     */
    fun isVideoCall(call: Call): Boolean {
        return call.type == CometChatConstants.CALL_TYPE_VIDEO
    }

    /**
     * Returns a formatted call status string based on call state.
     * Handles different call statuses: initiated, ongoing, ended, unanswered, cancelled, rejected, busy.
     *
     * @param context Android context for accessing resources
     * @param call The Call object
     * @return Formatted call status string
     */
    fun getCallStatus(context: Context, call: Call): String {
        if (call.receiverType != CometChatConstants.RECEIVER_TYPE_USER) {
            return ""
        }

        val initiator = call.callInitiator as? User
        val loggedInUser = getLoggedInUserSafe()
        val isInitiatedByMe = loggedInUser != null && initiator?.uid == loggedInUser.uid

        return when (call.callStatus) {
            UIKitConstants.CallStatusConstants.INITIATED -> {
                if (isInitiatedByMe) {
                    "${context.getString(R.string.cometchat_outgoing)} ${context.getString(R.string.cometchat_call)}"
                } else {
                    "${context.getString(R.string.cometchat_incoming)} ${context.getString(R.string.cometchat_call)}"
                }
            }
            UIKitConstants.CallStatusConstants.ONGOING -> {
                "${context.getString(R.string.cometchat_call)} ${context.getString(R.string.cometchat_accepted)}"
            }
            UIKitConstants.CallStatusConstants.ENDED -> {
                "${context.getString(R.string.cometchat_call)} ${context.getString(R.string.cometchat_ended)}"
            }
            UIKitConstants.CallStatusConstants.UNANSWERED -> {
                if (isInitiatedByMe) {
                    "${context.getString(R.string.cometchat_call)} ${context.getString(R.string.cometchat_unanswered)}"
                } else {
                    "${context.getString(R.string.cometchat_missed_call)} ${context.getString(R.string.cometchat_call)}"
                }
            }
            UIKitConstants.CallStatusConstants.CANCELLED -> {
                if (isInitiatedByMe) {
                    "${context.getString(R.string.cometchat_call)} ${context.getString(R.string.cometchat_cancel_call)}"
                } else {
                    "${context.getString(R.string.cometchat_missed_call)} ${context.getString(R.string.cometchat_call)}"
                }
            }
            UIKitConstants.CallStatusConstants.REJECTED -> {
                "${context.getString(R.string.cometchat_call)} ${context.getString(R.string.cometchat_rejected_call)}"
            }
            UIKitConstants.CallStatusConstants.BUSY -> {
                if (isInitiatedByMe) {
                    "${context.getString(R.string.cometchat_call)} ${context.getString(R.string.cometchat_busy_call)}"
                } else {
                    "${context.getString(R.string.cometchat_missed_call)} ${context.getString(R.string.cometchat_call)}"
                }
            }
            else -> ""
        }
    }

    /**
     * Checks if the call is a missed call (unanswered, cancelled, or busy by non-initiator).
     *
     * @param call The Call object
     * @return True if it's a missed call, false otherwise
     */
    fun isMissedCall(call: Call): Boolean {
        if (call.receiverType != CometChatConstants.RECEIVER_TYPE_USER) {
            return false
        }

        val initiator = call.callInitiator as? User
        val loggedInUser = getLoggedInUserSafe()
        val isInitiatedByMe = loggedInUser != null && initiator?.uid == loggedInUser.uid

        return when (call.callStatus) {
            UIKitConstants.CallStatusConstants.UNANSWERED,
            UIKitConstants.CallStatusConstants.CANCELLED,
            UIKitConstants.CallStatusConstants.BUSY -> !isInitiatedByMe
            else -> false
        }
    }
}
