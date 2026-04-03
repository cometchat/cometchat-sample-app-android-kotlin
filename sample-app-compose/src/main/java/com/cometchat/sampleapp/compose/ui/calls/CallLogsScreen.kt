package com.cometchat.sampleapp.compose.ui.calls

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.cometchat.calls.model.CallLog
import com.cometchat.calls.model.CallGroup
import com.cometchat.calls.model.CallUser
import com.cometchat.uikit.compose.presentation.calllogs.ui.CometChatCallLogs

/**
 * Call logs screen composable for the Calls tab.
 *
 * This screen displays the list of call logs using the
 * CometChatCallLogs component from the UIKit.
 *
 * ## Features:
 * - Displays call history with caller/callee info
 * - Shows call type (audio/video)
 * - Shows call status (missed/answered/outgoing)
 * - Shows call timestamp
 * - Handles call log tap to navigate to messages
 *
 * ## Usage:
 * ```kotlin
 * CallLogsScreen(
 *     onCallLogClick = { callLog ->
 *         // Navigate to messages screen with the call participant
 *     }
 * )
 * ```
 *
 * @param onCallLogClick Callback when a call log is tapped
 *
 * Validates: Requirements 5.3, 14.1, 14.2, 14.3, 14.4, 14.5
 */
@Composable
fun CallLogsScreen(
    onCallLogClick: (CallLog) -> Unit
) {
    CometChatCallLogs(
        modifier = Modifier.fillMaxSize(),
        // Show toolbar with title (matching sample-app-kotlin)
        hideToolbar = false,
        // Hide back button since this is a tab (Requirement 14.5)
        hideBackButton = true,
        // Hide item separators (matching sample-app-kotlin)
        hideSeparator = true,
        // Callbacks
        onItemClick = { callLog ->
            onCallLogClick(callLog)
        },
        onError = { exception ->
            // Error handling is done internally by the component
            // Additional error handling can be added here if needed
        }
    )
}

/**
 * Extension function to get the group ID from a CallLog.
 *
 * @return The group GUID if this is a group call, null otherwise
 */
fun CallLog.getCallLogGroupId(): String? {
    val receiver = this.receiver
    return if (receiver is CallGroup) {
        receiver.guid
    } else {
        null
    }
}

/**
 * Extension function to get the other user's ID from a CallLog.
 *
 * @param loggedInUserUid The UID of the currently logged-in user
 * @return The UID of the other participant if this is a 1-on-1 call, null otherwise
 */
fun CallLog.getCallLogUserId(loggedInUserUid: String?): String? {
    val initiator = this.initiator
    val receiver = this.receiver

    // If it's a group call, return null
    if (receiver is CallGroup) {
        return null
    }

    // Determine the other user in the call
    return when {
        initiator is CallUser && initiator.uid != loggedInUserUid -> initiator.uid
        receiver is CallUser && receiver.uid != loggedInUserUid -> receiver.uid
        initiator is CallUser -> initiator.uid
        receiver is CallUser -> receiver.uid
        else -> null
    }
}
