package com.cometchat.uikit.compose.presentation.notificationfeed.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.cometchat.chat.models.NotificationFeedItem
import com.cometchat.uikit.compose.presentation.notificationfeed.style.CometChatNotificationFeedStyle
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.uikit.core.state.TimestampGroup

/**
 * Scrollable feed list with timestamp group headers, infinite scroll,
 * and visibility tracking for engagement reporting.
 *
 * @param groupedItems Feed items grouped by timestamp
 * @param isLoadingMore Whether a pagination fetch is in progress
 * @param hasMorePages Whether more pages are available
 * @param style Style configuration
 * @param onLoadMore Callback to trigger next page fetch
 * @param onItemVisible Callback when an item enters the viewport
 * @param onItemHidden Callback when an item leaves the viewport
 * @param onItemClick Callback when a feed item is tapped
 * @param onActionClick Callback when an action button is tapped
 * @param modifier Modifier for the list container
 */
@Composable
fun NotificationFeedList(
    groupedItems: List<TimestampGroup>,
    isLoadingMore: Boolean,
    hasMorePages: Boolean,
    isPaginationError: Boolean = false,
    style: CometChatNotificationFeedStyle,
    activeCategory: String? = null,
    onLoadMore: () -> Unit,
    onRetryPagination: () -> Unit = {},
    onItemVisible: (NotificationFeedItem) -> Unit,
    onItemHidden: (NotificationFeedItem) -> Unit,
    onItemClick: (NotificationFeedItem) -> Unit,
    onActionClick: (NotificationFeedItem, Map<String, Any>) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    // Infinite scroll trigger — load more when near bottom
    LaunchedEffect(listState, hasMorePages, isLoadingMore) {
        snapshotFlow {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
            val totalItems = listState.layoutInfo.totalItemsCount
            lastVisibleItem != null &&
                totalItems > 0 &&
                lastVisibleItem.index >= totalItems - 3 &&
                hasMorePages &&
                !isLoadingMore
        }.collect { shouldLoad ->
            if (shouldLoad) {
                onLoadMore()
            }
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.testTag("notification-feed-list"),
        contentPadding = PaddingValues(top = 4.dp)
    ) {
        groupedItems.forEach { group ->
            // Feed items — each with its own category + timestamp row
            itemsIndexed(
                items = group.items,
                key = { _, item -> item.id ?: item.hashCode().toString() }
            ) { _, item ->
                // Per-item category + timestamp row
                // Hide category label when viewing a specific category (redundant)
                ItemCategoryTimestampRow(
                    item = item,
                    style = style,
                    showCategory = activeCategory == null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 2.dp)
                )

                NotificationFeedItemCard(
                    item = item,
                    style = style,
                    onItemVisible = onItemVisible,
                    onItemHidden = onItemHidden,
                    onItemClick = onItemClick,
                    onActionClick = onActionClick,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Loading more indicator
        if (isLoadingMore) {
            item(key = "loading_more") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(40.dp)
                                .testTag("notification-feed-loading-more"),
                            color = CometChatTheme.colorScheme.primary,
                            trackColor = CometChatTheme.colorScheme.strokeColorDefault,
                            strokeWidth = 4.dp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Loading...",
                            style = CometChatTheme.typography.bodyRegular,
                            color = CometChatTheme.colorScheme.textColorSecondary
                        )
                    }
                }
            }
        }

        // Pagination error with retry
        if (isPaginationError && !isLoadingMore) {
            item(key = "pagination_error") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onRetryPagination() }
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            painter = androidx.compose.ui.res.painterResource(id = com.cometchat.uikit.compose.R.drawable.cometchat_ic_retry),
                            contentDescription = "Retry",
                            tint = CometChatTheme.colorScheme.errorColor,
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("notification-feed-pagination-error-icon")
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Couldn't load more",
                            style = CometChatTheme.typography.bodyRegular,
                            color = CometChatTheme.colorScheme.textColorSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap to retry",
                            style = CometChatTheme.typography.bodyRegular,
                            color = CometChatTheme.colorScheme.primary,
                            modifier = Modifier.testTag("notification-feed-pagination-retry")
                        )
                    }
                }
            }
        }
    }
}

/**
 * Timestamp group section header.
 * Text style: caption1Regular (12sp regular), color: textColorSecondary.
 */
@Composable
private fun TimestampGroupHeader(
    label: String,
    style: CometChatNotificationFeedStyle,
    modifier: Modifier = Modifier
) {
    Text(
        text = label,
        color = style.timestampTextColor,
        style = CometChatTheme.typography.caption1Regular,
        modifier = modifier.testTag("notification-feed-timestamp-header-$label")
    )
}

/**
 * Per-item row showing category name on left and relative timestamp on right.
 * Matches Figma: timestamp-header component per feed item.
 */
@Composable
private fun ItemCategoryTimestampRow(
    item: NotificationFeedItem,
    style: CometChatNotificationFeedStyle,
    showCategory: Boolean = true,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Category label (left) — hidden when viewing a specific category or null
        if (showCategory && !item.category.isNullOrEmpty() && item.category != "null") {
            Text(
                text = item.category,
                color = style.timestampTextColor,
                style = CometChatTheme.typography.caption1Regular,
            )
        } else {
            // Empty spacer to keep timestamp on the right
            Spacer(modifier = Modifier.weight(1f))
        }

        // Relative timestamp (right)
        Text(
            text = getRelativeTime(item.sentAt),
            color = style.timestampTextColor,
            style = CometChatTheme.typography.caption1Regular
        )
    }
}

private fun getRelativeTime(sentAtSeconds: Long): String {
    val timestampMs = sentAtSeconds * 1000
    val date = java.util.Date(timestampMs)
    val calendar = java.util.Calendar.getInstance()
    calendar.time = date

    val today = java.util.Calendar.getInstance()
    val currentYear = today.get(java.util.Calendar.YEAR)
    val inputYear = calendar.get(java.util.Calendar.YEAR)

    val isToday = calendar.get(java.util.Calendar.YEAR) == today.get(java.util.Calendar.YEAR) &&
            calendar.get(java.util.Calendar.DAY_OF_YEAR) == today.get(java.util.Calendar.DAY_OF_YEAR)

    val format = when {
        isToday -> java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault())
        inputYear == currentYear -> java.text.SimpleDateFormat("d MMMM, h:mm a", java.util.Locale.getDefault())
        else -> java.text.SimpleDateFormat("d MMMM yyyy, h:mm a", java.util.Locale.getDefault())
    }

    return format.format(date)
}
