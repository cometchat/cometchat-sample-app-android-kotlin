package com.cometchat.uikit.compose.presentation.shared.baseelements.date

/**
 * Enum representing different patterns for formatting dates in CometChatDate.
 *
 * Each pattern defines how the timestamp should be displayed:
 * - TIME: Shows only time (e.g., "2:30 PM")
 * - DAY_DATE: Shows day or date with smart formatting ("Today", "Yesterday", or "dd MMM yyyy")
 * - DAY_DATE_TIME: Shows time for today, day name for last week, or full date for older
 */
enum class Pattern {
    /**
     * Time pattern - displays only the time portion (e.g., "2:30 PM")
     */
    TIME,
    
    /**
     * Day and date pattern - displays "Today", "Yesterday", or formatted date (e.g., "15 Dec 2023")
     */
    DAY_DATE,
    
    /**
     * Day, date, and time pattern - smart formatting based on recency:
     * - Today: Shows time (e.g., "2:30 PM")
     * - Yesterday: Shows "Yesterday"
     * - Last 7 days: Shows day name (e.g., "Monday")
     * - Older: Shows full date (e.g., "15 Dec 2023")
     */
    DAY_DATE_TIME
}
