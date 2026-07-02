package com.cometchat.sampleapp.compose.push.appflow.tabs

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.cometchat.calls.model.CallLog
import com.cometchat.uikit.compose.presentation.calllogs.ui.CometChatCallLogs

/**
 * Calls tab displaying the call logs list.
 * Uses CometChatCallLogs component from the UIKit.
 *
 * @param onCallLogClick Callback when a call log entry is clicked
 * @param contentPadding Padding from the scaffold
 */
@Composable
fun CallsTab(
    onCallLogClick: (CallLog) -> Unit,
    contentPadding: PaddingValues
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
    ) {
        CometChatCallLogs(
            modifier = Modifier.fillMaxSize(),
            title = "Calls",
            hideSeparator = true,
            onItemClick = { callLog ->
                onCallLogClick(callLog)
            },
            onError = { exception ->
                Log.e("CallsTab", "Error: ${exception.message}")
            }
        )
    }
}
