package com.cometchat.uikit.compose.presentation.notificationfeed.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cometchat.uikit.compose.presentation.notificationfeed.style.CometChatNotificationFeedStyle
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.uikit.core.state.FilterChipState

/**
 * Horizontal scrollable row of filter chips for category-based filtering.
 * First chip is always "All", followed by server-provided categories.
 *
 * @param chips List of filter chip states
 * @param style Style configuration
 * @param onChipClick Callback when a chip is tapped (passes chip ID)
 * @param modifier Modifier for the row container
 */
@Composable
fun NotificationFeedFilterChips(
    chips: List<FilterChipState>,
    style: CometChatNotificationFeedStyle,
    onChipClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.testTag("notification-feed-filter-chips"),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 0.dp)
    ) {
        items(
            items = chips,
            key = { it.id }
        ) { chip ->
            NotificationFeedChip(
                chip = chip,
                style = style,
                onClick = { onChipClick(chip.id) }
            )
        }
    }
}

/**
 * Individual filter chip with active/inactive states and optional unread badge.
 * Chip height: 34dp, horizontal padding: 12dp, vertical padding: 6dp.
 */
@Composable
private fun NotificationFeedChip(
    chip: FilterChipState,
    style: CometChatNotificationFeedStyle,
    onClick: () -> Unit
) {
    val chipShape = RoundedCornerShape(20.dp)
    val backgroundColor = if (chip.isActive) style.chipActiveBackgroundColor else style.chipInactiveBackgroundColor
    val textColor = when {
        chip.isActive -> style.chipActiveTextColor
        chip.unreadCount > 0 -> CometChatTheme.colorScheme.textColorSecondary  // Has unreads: medium-dark
        else -> style.chipInactiveTextColor  // No unreads: light gray
    }

    Row(
        modifier = Modifier
            .height(34.dp)
            .clip(chipShape)
            .then(
                if (!chip.isActive) {
                    Modifier.border(1.dp, style.chipBorderColor, chipShape)
                } else {
                    Modifier
                }
            )
            .background(backgroundColor, chipShape)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag("notification-feed-chip-${chip.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Chip label
        Text(
            text = chip.label,
            color = textColor,
            style = CometChatTheme.typography.bodyMedium
        )

        // Unread badge
        if (chip.unreadCount > 0) {
            Spacer(modifier = Modifier.width(6.dp))
            UnreadBadge(
                count = chip.unreadCount,
                style = style,
                isActive = chip.isActive
            )
        }
    }
}

/**
 * Small badge showing unread count inside the chip.
 * Active chip: bg = extendedPrimaryColor50, border = extendedPrimaryColor200, text = textColorHighlight
 * Inactive chip: bg = neutralColor600, no border, text = colorWhite
 */
@Composable
private fun UnreadBadge(
    count: Int,
    style: CometChatNotificationFeedStyle,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val badgeBg = if (isActive) style.badgeActiveBackgroundColor else style.badgeInactiveBackgroundColor
    val badgeText = if (isActive) style.badgeActiveTextColor else style.badgeInactiveTextColor
    val badgeShape = RoundedCornerShape(10.dp)

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(badgeShape)
            .then(
                if (isActive) {
                    Modifier.border(1.dp, style.badgeActiveBorderColor, badgeShape)
                } else {
                    Modifier
                }
            )
            .background(badgeBg, badgeShape)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = if (count > 99) "99+" else count.toString(),
            color = badgeText,
            style = CometChatTheme.typography.caption1Medium,
            textAlign = TextAlign.Center
        )
    }
}
