package com.cometchat.uikit.compose.presentation.notificationfeed.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.cometchat.cards.CometChatCardComposable
import com.cometchat.cards.models.CometChatCardThemeMode
import com.cometchat.chat.models.NotificationFeedItem
import com.cometchat.uikit.compose.presentation.notificationfeed.style.CometChatNotificationFeedStyle
import com.cometchat.uikit.compose.theme.CometChatTheme

/**
 * Individual feed item card with unread indicator, card content rendering,
 * and visibility tracking for engagement reporting.
 *
 * @param item The notification feed item to render
 * @param style Style configuration
 * @param onItemVisible Callback when item enters viewport
 * @param onItemHidden Callback when item leaves viewport
 * @param onItemClick Callback when item is tapped
 * @param onActionClick Callback when an action button is tapped
 * @param modifier Modifier for the card container
 */
@Composable
fun NotificationFeedItemCard(
    item: NotificationFeedItem,
    style: CometChatNotificationFeedStyle,
    onItemVisible: (NotificationFeedItem) -> Unit,
    onItemHidden: (NotificationFeedItem) -> Unit,
    onItemClick: (NotificationFeedItem) -> Unit,
    onActionClick: (NotificationFeedItem, Map<String, Any>) -> Unit,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    // Track visibility based on lifecycle
    DisposableEffect(lifecycleOwner, item.id) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE,
                Lifecycle.Event.ON_STOP -> {
                    if (isVisible) {
                        isVisible = false
                        onItemHidden(item)
                    }
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            if (isVisible) {
                onItemHidden(item)
            }
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onItemClick(item) }
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .onGloballyPositioned { coordinates ->
                val newVisibility = coordinates.size.height > 0
                if (newVisibility && !isVisible) {
                    isVisible = true
                    onItemVisible(item)
                } else if (!newVisibility && isVisible) {
                    isVisible = false
                    onItemHidden(item)
                }
            }
            .testTag("notification-feed-item-${item.id}")
            .semantics {
                contentDescription = "Notification from ${item.category ?: "unknown"}"
            },
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            // CometChatCardsRenderer content
            val cardJson = item.content?.toString() ?: ""
            if (cardJson.isNotEmpty()) {
                CometChatCardComposable(
                    cardJson = cardJson,
                    themeMode = CometChatCardThemeMode.AUTO,
                    logLevel = com.cometchat.cards.models.CometChatCardLogLevel.VERBOSE,
                    onAction = { event ->
                        onActionClick(
                            item,
                            mapOf(
                                "type" to event.action,
                                "elementId" to event.elementId
                            )
                        )
                    }
                )
            } else {
                Text(
                    text = "Unable to display notification",
                    style = CometChatTheme.typography.bodyRegular,
                    color = CometChatTheme.colorScheme.textColorSecondary
                )
            }
        }
    }
}
