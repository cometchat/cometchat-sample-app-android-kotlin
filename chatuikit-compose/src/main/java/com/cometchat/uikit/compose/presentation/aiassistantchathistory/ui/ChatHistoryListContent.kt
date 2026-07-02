package com.cometchat.uikit.compose.presentation.aiassistantchathistory.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.TextMessage
import com.cometchat.uikit.compose.presentation.aiassistantchathistory.style.CometChatAIAssistantChatHistoryStyle
import com.cometchat.uikit.compose.presentation.shared.popupmenu.CometChatPopupMenu
import com.cometchat.uikit.compose.shared.views.popupmenu.MenuItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Content composable for the AI assistant chat history message list.
 *
 * Displays messages grouped by date using [LazyColumn] with sticky headers.
 * Each date group has a pinned header formatted as "MMM dd, yyyy" and message
 * items are rendered as single-line truncated text with ellipsis overflow.
 *
 * Supports pagination by detecting when fewer than 2 items remain below the
 * last visible position and invoking [onFetchMore].
 *
 * @param messages List of messages to display
 * @param style Style configuration for the component
 * @param hasMore Whether more messages are available for pagination
 * @param isInProgress Whether a fetch operation is currently in progress
 * @param listState The [LazyListState] for controlling and observing scroll position
 * @param onFetchMore Callback invoked when more messages should be loaded
 * @param onItemClick Callback invoked when a message item is tapped
 * @param onItemLongClick Callback invoked when a message item is long-pressed
 * @param popupMenuItems Function that builds popup menu items for a given message
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun ChatHistoryListContent(
    messages: List<BaseMessage>,
    style: CometChatAIAssistantChatHistoryStyle,
    hasMore: Boolean,
    isInProgress: Boolean,
    listState: LazyListState = rememberLazyListState(),
    onFetchMore: () -> Unit,
    onItemClick: (BaseMessage) -> Unit,
    onItemLongClick: (BaseMessage) -> Unit,
    popupMenuItems: @Composable (BaseMessage, onDismiss: () -> Unit) -> List<MenuItem> = { _, _ -> emptyList() }
) {
    // Date formatter for grouping messages — "MMM dd, yyyy"
    val dateFormat = remember {
        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    }

    // Detect when we need to load more items (fewer than 2 items below last visible)
    val shouldLoadMore by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()
            lastVisibleItem != null &&
                lastVisibleItem.index >= layoutInfo.totalItemsCount - 2
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore && hasMore && !isInProgress && messages.isNotEmpty()) {
            onFetchMore()
        }
    }

    // Group messages by formatted date
    val groupedMessages = remember(messages) {
        messages.groupBy { message ->
            formatDate(message.sentAt, dateFormat)
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .background(style.chatHistoryBackgroundColor)
    ) {
        groupedMessages.forEach { (date, dateMessages) ->
            // Sticky date header for each group
            stickyHeader(key = "header_$date") {
                DateSeparatorHeader(
                    date = date,
                    style = style
                )
            }

            // Message items within this date group
            items(
                items = dateMessages,
                key = { message -> message.id }
            ) { message ->
                // Per-item popup state (same pattern as CometChatConversations)
                var showPopupMenu by remember { mutableStateOf(false) }

                val menuItems = popupMenuItems(message) { showPopupMenu = false }

                ChatHistoryMessageItem(
                    message = message,
                    style = style,
                    showPopupMenu = showPopupMenu,
                    popupMenuItems = menuItems,
                    onClick = { onItemClick(message) },
                    onLongClick = {
                        showPopupMenu = true
                        onItemLongClick(message)
                    },
                    onDismissPopup = { showPopupMenu = false },
                    onMenuItemClick = { showPopupMenu = false }
                )
            }
        }
    }
}

/**
 * Sticky date separator header displayed above each date group.
 *
 * @param date The formatted date string (e.g., "Jan 15, 2025")
 * @param style Style configuration for the date separator
 */
@Composable
private fun DateSeparatorHeader(
    date: String,
    style: CometChatAIAssistantChatHistoryStyle
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(style.dateSeparatorBackgroundColor)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = date,
            color = style.dateSeparatorTextColor,
            style = style.dateSeparatorTextStyle
        )
    }
}

/**
 * Single message item in the chat history list.
 * Displays the message text as a single-line truncated row with ellipsis overflow.
 * Includes an anchored popup menu for long-press actions.
 *
 * @param message The message to display
 * @param style Style configuration for the message item
 * @param showPopupMenu Whether the popup menu is currently shown for this item
 * @param popupMenuItems The menu items to display in the popup
 * @param onClick Callback invoked when the item is tapped
 * @param onLongClick Callback invoked when the item is long-pressed
 * @param onDismissPopup Callback invoked when the popup menu is dismissed
 * @param onMenuItemClick Callback invoked when a popup menu item is clicked
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ChatHistoryMessageItem(
    message: BaseMessage,
    style: CometChatAIAssistantChatHistoryStyle,
    showPopupMenu: Boolean = false,
    popupMenuItems: List<MenuItem> = emptyList(),
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onDismissPopup: () -> Unit = {},
    onMenuItemClick: () -> Unit = {}
) {
    val messageText = if (message is TextMessage) {
        message.text
    } else {
        ""
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(style.itemBackgroundColor)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = messageText,
            color = style.itemTextColor,
            style = style.itemTextStyle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // Popup menu anchored to the bottom-end of the item
        Box(
            modifier = Modifier.align(Alignment.BottomEnd)
        ) {
            CometChatPopupMenu(
                expanded = showPopupMenu,
                onDismissRequest = onDismissPopup,
                menuItems = popupMenuItems,
                onMenuItemClick = { _, _ -> onMenuItemClick() }
            ) {
                // Empty anchor - the popup will appear at this position
            }
        }
    }
}

/**
 * Formats a Unix timestamp (in seconds) to a date string using the provided formatter.
 * Returns "Updating" for timestamps that are zero or negative.
 *
 * @param sentAt The message sent timestamp in seconds
 * @param dateFormat The [SimpleDateFormat] to use for formatting
 * @return The formatted date string
 */
private fun formatDate(sentAt: Long, dateFormat: SimpleDateFormat): String {
    return if (sentAt > 0) {
        dateFormat.format(Date(sentAt * 1000))
    } else {
        "Updating"
    }
}
