package com.cometchat.uikit.kotlin.shared.resources.utils

import android.content.Context
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.shared.interfaces.DateTimeFormatterCallback
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Utility object for date and time formatting operations.
 */
object DateUtils {

    /**
     * Formats a timestamp for display as the last message date in conversations.
     *
     * Format rules:
     * - Within 24 hours: Shows time (e.g., "3:45 PM")
     * - Within 48 hours: Shows "Yesterday"
     * - Within 7 days: Shows day of week (e.g., "Mon")
     * - Older: Shows date (e.g., "15 Jan 2024")
     *
     * @param context The context for accessing string resources
     * @param timestamp The timestamp in milliseconds
     * @param dateTimeFormatter Optional custom formatter callback
     * @return Formatted date/time string
     */
    fun getLastMessageDate(
        context: Context,
        timestamp: Long,
        dateTimeFormatter: DateTimeFormatterCallback? = null
    ): String {
        val currentTimeStamp = System.currentTimeMillis()
        val diffTimeStamp = currentTimeStamp - timestamp

        // Try custom formatter first if provided
        if (dateTimeFormatter != null) {
            val diffInMinutes = diffTimeStamp / (60 * 1000)
            val diffInHours = diffTimeStamp / (60 * 60 * 1000)

            val customResult = when {
                diffTimeStamp < 24 * 60 * 60 * 1000 -> {
                    dateTimeFormatter.today(timestamp)
                        ?: dateTimeFormatter.time(timestamp)
                }
                diffTimeStamp < 48 * 60 * 60 * 1000 -> {
                    dateTimeFormatter.yesterday(timestamp)
                }
                diffTimeStamp < 7 * 24 * 60 * 60 * 1000 -> {
                    dateTimeFormatter.lastWeek(timestamp)
                }
                else -> {
                    dateTimeFormatter.otherDays(timestamp)
                }
            }
            if (customResult != null) return customResult
        }

        // Default formatting
        val lastMessageTime = SimpleDateFormat("h:mm a", Locale.US).format(Date(timestamp))
        val lastMessageDate = SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date(timestamp))
        val lastMessageWeek = SimpleDateFormat("EEE", Locale.US).format(Date(timestamp))

        return when {
            diffTimeStamp < 24 * 60 * 60 * 1000 -> lastMessageTime
            diffTimeStamp < 48 * 60 * 60 * 1000 -> context.getString(R.string.cometchat_yesterday)
            diffTimeStamp < 7 * 24 * 60 * 60 * 1000 -> lastMessageWeek
            else -> lastMessageDate
        }
    }

    /**
     * Extracts the first URL from a text string.
     *
     * @param text The text to search for URLs
     * @return The first URL found, or null if no URL is present
     */
    fun extractFirstUrl(text: String?): String? {
        if (text.isNullOrEmpty()) return null

        val urlPattern = Regex(
            """(https?://[^\s<>"{}|\\^`\[\]]+)""",
            RegexOption.IGNORE_CASE
        )
        return urlPattern.find(text)?.value
    }
}
