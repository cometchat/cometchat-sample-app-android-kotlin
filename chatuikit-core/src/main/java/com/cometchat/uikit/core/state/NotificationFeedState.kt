package com.cometchat.uikit.core.state

import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.NotificationCategory
import com.cometchat.chat.models.NotificationFeedItem

/**
 * Sealed class representing UI states for the notification feed screen.
 * Used by the ViewModel to communicate current state to the UI.
 */
sealed class NotificationFeedUIState {
    /**
     * Loading state - displayed while fetching initial feed items.
     */
    object Loading : NotificationFeedUIState()

    /**
     * Empty state - displayed when no feed items exist (after successful fetch).
     */
    object Empty : NotificationFeedUIState()

    /**
     * Error state - displayed when fetching fails.
     * @param exception The exception that caused the error
     */
    data class Error(val exception: CometChatException) : NotificationFeedUIState()

    /**
     * Content state - displayed when feed items are available.
     * @param groupedItems The feed items grouped by timestamp
     */
    data class Content(val groupedItems: List<TimestampGroup>) : NotificationFeedUIState()
}

/**
 * Represents a group of feed items sharing the same timestamp label.
 * Used for section headers in the feed list.
 *
 * @param label Display label (e.g., "Today", "Yesterday", "Monday", "Jan 15, 2025")
 * @param items Feed items within this group, ordered newest to oldest
 */
data class TimestampGroup(
    val label: String,
    val items: List<NotificationFeedItem>
)

/**
 * Represents the state of a single filter chip.
 *
 * @param id Category ID (or "all" for the hardcoded "All" chip)
 * @param label Display text for the chip
 * @param isActive Whether this chip is currently selected
 * @param unreadCount Badge count (0 = no badge displayed)
 */
data class FilterChipState(
    val id: String,
    val label: String,
    val isActive: Boolean,
    val unreadCount: Int
)
