package com.cometchat.uikit.compose.presentation.messagelist.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cometchat.uikit.compose.presentation.shared.shimmer.ui.CometChatMessageListShimmer
import com.cometchat.uikit.compose.presentation.shared.shimmer.utils.ProvideShimmerAnimation
import com.cometchat.uikit.compose.presentation.shared.baseelements.badgecount.CometChatBadgeCount
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.User
import com.cometchat.uikit.compose.R
import com.cometchat.uikit.compose.presentation.messagelist.style.CometChatMessageListStyle
import com.cometchat.uikit.compose.theme.CometChatTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Displays a date separator between messages from different days.
 *
 * The separator shows the formatted date (Today, Yesterday, or the actual date)
 * centered with a styled background.
 *
 * @param timestamp The timestamp in seconds to display
 * @param style The message list style containing date separator styling
 * @param modifier Optional modifier for the composable
 */
@Composable
internal fun DateSeparator(
    timestamp: Long,
    style: CometChatMessageListStyle,
    modifier: Modifier = Modifier
) {
    val formattedDate = remember(timestamp) { formatDateSeparator(timestamp) }
    val accessibilityDescription = "Date separator: $formattedDate"
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .semantics { contentDescription = accessibilityDescription },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = formattedDate,
            style = style.dateSeparatorTextStyle,
            color = style.dateSeparatorTextColor,
            modifier = Modifier
                .background(
                    color = style.dateSeparatorBackgroundColor,
                    shape = RoundedCornerShape(style.dateSeparatorCornerRadius)
                )
                .then(
                    if (style.dateSeparatorStrokeWidth > 0.dp) {
                        Modifier.border(
                            width = style.dateSeparatorStrokeWidth,
                            color = style.dateSeparatorStrokeColor,
                            shape = RoundedCornerShape(style.dateSeparatorCornerRadius)
                        )
                    } else {
                        Modifier
                    }
                )
                .padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}

/**
 * Default loading view for the message list.
 *
 * Displays a shimmer effect with alternating left and right aligned bubbles
 * to simulate message loading, matching the Java chatuikit implementation.
 *
 * @param modifier Optional modifier for the composable
 */
@Composable
internal fun DefaultLoadingView(
    modifier: Modifier = Modifier
) {
    ProvideShimmerAnimation {
        CometChatMessageListShimmer(
            modifier = modifier
                .fillMaxSize()
                .semantics { contentDescription = "Loading messages" }
        )
    }
}

/**
 * Default empty view for the message list.
 *
 * Displays a greeting message when there are no messages in the conversation.
 *
 * @param style The message list style containing empty state styling
 * @param user The user for the conversation (null for group conversations)
 * @param group The group for the conversation (null for user conversations)
 * @param modifier Optional modifier for the composable
 */
@Composable
internal fun DefaultEmptyView(
    style: CometChatMessageListStyle,
    user: User? = null,
    group: Group? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val name = user?.name ?: group?.name ?: ""
    
    val title = if (name.isNotEmpty()) {
        stringResource(R.string.cometchat_no_messages_yet)
    } else {
        stringResource(R.string.cometchat_no_messages_yet)
    }
    
    val subtitle = if (name.isNotEmpty()) {
        stringResource(R.string.cometchat_say_hi_to, name)
    } else {
        stringResource(R.string.cometchat_start_conversation)
    }
    
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(32.dp)
                .semantics { contentDescription = "$title. $subtitle" }
        ) {
            Text(
                text = title,
                style = style.emptyChatGreetingTitleTextStyle,
                color = style.emptyChatGreetingTitleTextColor,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = subtitle,
                style = style.emptyChatGreetingSubtitleTextStyle,
                color = style.emptyChatGreetingSubtitleTextColor,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Default error view for the message list.
 *
 * Displays an error message with a retry button.
 *
 * @param style The message list style containing error state styling
 * @param onRetry Callback invoked when the retry button is clicked
 * @param modifier Optional modifier for the composable
 */
@Composable
internal fun DefaultErrorView(
    style: CometChatMessageListStyle,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val title = stringResource(R.string.cometchat_something_went_wrong)
    val subtitle = stringResource(R.string.cometchat_try_again_later)
    val retryText = stringResource(R.string.cometchat_retry)
    
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(32.dp)
                .semantics { contentDescription = "$title. $subtitle" }
        ) {
            Text(
                text = title,
                style = style.errorStateTitleTextStyle,
                color = style.errorStateTitleTextColor,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = subtitle,
                style = style.errorStateSubtitleTextStyle,
                color = style.errorStateSubtitleTextColor,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            TextButton(
                onClick = onRetry,
                modifier = Modifier.semantics { contentDescription = retryText }
            ) {
                Text(
                    text = retryText,
                    color = CometChatTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * Default new message indicator for the message list.
 *
 * Displays a badge showing the count of new messages with a down arrow icon
 * in a vertical layout. The badge count appears above the arrow icon only when
 * there are new messages (count > 0). When count is 0, only the arrow icon is shown.
 * Clicking the indicator triggers a reset-and-fetch to show the latest messages.
 *
 * @param count The number of new messages (0 shows only arrow icon)
 * @param style The message list style containing indicator styling
 * @param onClick Callback invoked when the indicator is clicked
 * @param modifier Optional modifier for the composable
 */
@Composable
internal fun DefaultNewMessageIndicator(
    count: Int,
    style: CometChatMessageListStyle,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accessibilityText = when {
        count >= 1000 -> "999+ new messages. Tap to scroll to bottom"
        count == 1 -> "1 new message. Tap to scroll to bottom"
        count > 0 -> "$count new messages. Tap to scroll to bottom"
        else -> "Tap to scroll to bottom"
    }
    
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(style.newMessageIndicatorCornerRadius),
        color = style.newMessageIndicatorBackgroundColor,
        shadowElevation = style.newMessageIndicatorElevation,
        modifier = modifier
            .border(
                width = style.newMessageIndicatorStrokeWidth,
                color = style.newMessageIndicatorStrokeColor,
                shape = RoundedCornerShape(style.newMessageIndicatorCornerRadius)
            )
            .semantics {
                contentDescription = accessibilityText
                role = Role.Button
            }
    ) {
        Column(
            modifier = Modifier.padding(style.newMessageIndicatorPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Only show badge when count > 0
            if (count > 0) {
                CometChatBadgeCount(count = count)
                Spacer(modifier = Modifier.height(4.dp))
            }
            
            Icon(
                painter = painterResource(id = R.drawable.cometchat_ic_arrow_down),
                contentDescription = null,
                tint = style.newMessageIndicatorIconTint,
                modifier = Modifier.size(style.newMessageIndicatorIconSize)
            )
        }
    }
}

/**
 * Displays a sticky date header that stays pinned at the top of the message list.
 *
 * This header shows the date of the topmost visible message and remains visible
 * while scrolling through messages from that day. It provides context about
 * when the currently visible messages were sent.
 *
 * The sticky header differs from inline date separators:
 * - Inline separators appear between messages from different days
 * - Sticky header stays pinned at the top showing the current scroll position's date
 *
 * @param timestamp The timestamp in seconds of the topmost visible message
 * @param style The message list style containing date separator styling
 * @param modifier Optional modifier for the composable
 */
@Composable
internal fun StickyDateHeader(
    timestamp: Long,
    style: CometChatMessageListStyle,
    modifier: Modifier = Modifier
) {
    val formattedDate = remember(timestamp) { formatDateSeparator(timestamp) }
    val accessibilityDescription = "Current date: $formattedDate"
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .semantics { contentDescription = accessibilityDescription },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = formattedDate,
            style = style.dateSeparatorTextStyle,
            color = style.dateSeparatorTextColor,
            modifier = Modifier
                .background(
                    color = style.dateSeparatorBackgroundColor,
                    shape = RoundedCornerShape(style.dateSeparatorCornerRadius)
                )
                .then(
                    if (style.dateSeparatorStrokeWidth > 0.dp) {
                        Modifier.border(
                            width = style.dateSeparatorStrokeWidth,
                            color = style.dateSeparatorStrokeColor,
                            shape = RoundedCornerShape(style.dateSeparatorCornerRadius)
                        )
                    } else {
                        Modifier
                    }
                )
                .padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}

/**
 * Displays a "New Messages" separator above the first unread message.
 *
 * This separator visually divides read messages from unread messages,
 * helping users quickly identify where new messages begin in the conversation.
 * The layout consists of horizontal lines on either side of the "New" text.
 *
 * Layout: [--- line ---] [New] [--- line ---]
 *
 * @param style The message list style containing separator styling
 * @param modifier Optional modifier for the composable
 */
@Composable
internal fun NewMessagesSeparator(
    style: CometChatMessageListStyle,
    modifier: Modifier = Modifier
) {
    val newText = stringResource(R.string.cometchat_new)
    val accessibilityDescription = "New messages separator"
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = style.newMessagesSeparatorVerticalPadding)
            .semantics { contentDescription = accessibilityDescription },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left line
        Box(
            modifier = Modifier
                .weight(1f)
                .height(style.newMessagesSeparatorLineHeight)
                .padding(horizontal = 5.dp)
                .background(style.newMessagesSeparatorLineColor)
        )
        
        // "New" text
        Text(
            text = newText,
            color = style.newMessagesSeparatorTextColor,
            style = style.newMessagesSeparatorTextStyle,
            modifier = Modifier.padding(horizontal = 5.dp)
        )
        
        // Right line
        Box(
            modifier = Modifier
                .weight(1f)
                .height(style.newMessagesSeparatorLineHeight)
                .padding(horizontal = 5.dp)
                .background(style.newMessagesSeparatorLineColor)
        )
    }
}

/**
 * Formats a timestamp for display in the date separator.
 *
 * Returns "Today" for today's date, "Yesterday" for yesterday,
 * or a formatted date string for older dates.
 *
 * @param timestamp The timestamp in seconds
 * @return The formatted date string
 */
internal fun formatDateSeparator(timestamp: Long): String {
    val messageDate = Calendar.getInstance().apply {
        timeInMillis = timestamp * 1000
    }
    
    val today = Calendar.getInstance()
    val yesterday = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, -1)
    }
    
    return when {
        isSameDay(messageDate, today) -> "Today"
        isSameDay(messageDate, yesterday) -> "Yesterday"
        else -> {
            val dateFormat = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
            dateFormat.format(Date(timestamp * 1000))
        }
    }
}

/**
 * Checks if two calendar instances represent the same day.
 *
 * @param cal1 The first calendar
 * @param cal2 The second calendar
 * @return True if both calendars represent the same day
 */
private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

/**
 * Gets a unique date identifier for grouping messages by day.
 * 
 * Returns a Long in YYYYMMDD format for consistent date comparison.
 * This matches the behavior of the Java implementation's Utils.getDateId()
 * but uses YYYYMMDD format for better chronological sorting.
 *
 * Example: December 25, 2024 returns 20241225L
 *
 * @param timestampSeconds The timestamp in seconds (Unix epoch)
 * @return A Long representing the date in YYYYMMDD format
 */
internal fun getDateId(timestampSeconds: Long): Long {
    val calendar = Calendar.getInstance(Locale.ENGLISH).apply {
        timeInMillis = timestampSeconds * 1000
    }
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH) + 1 // Calendar.MONTH is 0-indexed
    val day = calendar.get(Calendar.DAY_OF_MONTH)
    return (year * 10000L + month * 100L + day)
}

/**
 * Determines if a date separator should be hidden because the sticky header
 * is already showing the same date at the top of the viewport.
 *
 * This function prevents duplicate date displays by hiding the inline date separator
 * when it would appear at the top of the visible area and the sticky header is
 * already showing the same date.
 *
 * The logic ensures:
 * - Only the topmost visible message's date separator is considered for hiding
 * - The separator is only hidden when its date matches the sticky header's date
 * - Other date separators (not at the top) remain visible
 *
 * @param messageIndex The index of the message in the list
 * @param messageDate The date ID of the message (from getDateId())
 * @param stickyHeaderDate The date ID currently shown in the sticky header (null if not visible)
 * @param firstVisibleIndex The index of the topmost visible message (null if unknown)
 * @return True if the date separator should be hidden, false otherwise
 *
 * @see getDateId for date ID calculation
 */
internal fun isDateSeparatorHiddenByStickyHeader(
    messageIndex: Int,
    messageDate: Long,
    stickyHeaderDate: Long?,
    firstVisibleIndex: Int?
): Boolean {
    // Only hide if this is the topmost visible message with a date separator
    // and the sticky header is showing the same date
    if (stickyHeaderDate == null || firstVisibleIndex == null) return false
    
    // The message at firstVisibleIndex is the topmost visible message
    // If this message's date matches the sticky header, hide its separator
    return messageIndex == firstVisibleIndex && messageDate == stickyHeaderDate
}
