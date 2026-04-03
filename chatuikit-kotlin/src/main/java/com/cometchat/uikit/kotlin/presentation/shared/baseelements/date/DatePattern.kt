package com.cometchat.uikit.kotlin.presentation.shared.baseelements.date

/**
 * Enum representing different date display patterns.
 */
enum class DatePattern {
    /**
     * Displays only the time (e.g., "2:30 PM")
     */
    TIME,

    /**
     * Displays the day and date (e.g., "Today", "Yesterday", "15 Jan 2024")
     */
    DAY_DATE,

    /**
     * Displays the day, date, and time combined
     */
    DAY_DATE_TIME
}
