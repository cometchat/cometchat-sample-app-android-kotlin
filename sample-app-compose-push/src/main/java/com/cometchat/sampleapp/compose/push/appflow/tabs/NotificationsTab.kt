package com.cometchat.sampleapp.compose.push.appflow.tabs

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.uikit.compose.presentation.notificationfeed.ui.CometChatNotificationFeed

/**
 * Notifications tab displaying the notification feed.
 * Shows campaign/promotional notifications with category filtering,
 * timestamp grouping, and engagement reporting.
 *
 * @param contentPadding Padding from the scaffold
 */
@Composable
fun NotificationsTab(
    contentPadding: PaddingValues
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
    ) {
        val context = LocalContext.current
        CometChatNotificationFeed(
            modifier = Modifier.fillMaxSize(),
            title = "Notifications",
            showBackButton = false,
            showFilterChips = true,
            onItemClick = { feedItem ->
                Log.d("NotificationsTab", "Item clicked: ${feedItem.id}")
            },
            onActionClick = { feedItem, action ->
                Log.d("NotificationsTab", "Action clicked on item: ${feedItem.id}, action: $action")
                android.widget.Toast.makeText(
                    context,
                    "Action: ${action["type"]} | Element: ${action["elementId"]}",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
                // Report engagement with topic "clicked" when user interacts with a card action
                CometChat.reportFeedEngagement(feedItem, "clicked", object : CometChat.CallbackListener<Void>() {
                    override fun onSuccess(result: Void?) {
                        Log.d("NotificationsTab", "Engagement reported for item: ${feedItem.id}")
                    }
                    override fun onError(e: CometChatException) {
                        Log.e("NotificationsTab", "Failed to report engagement: ${e.message}")
                    }
                })
            },
            onError = { exception ->
                Log.e("NotificationsTab", "Error: ${exception.message}")
            }
        )
    }
}
