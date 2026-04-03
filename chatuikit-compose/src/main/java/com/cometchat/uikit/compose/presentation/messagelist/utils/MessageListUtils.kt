package com.cometchat.uikit.compose.presentation.messagelist.utils

import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.state.MessageAlignment
import java.util.Calendar

/**
 * Determines the alignment for a message based on sender and message category.
 *
 * Alignment rules:
 * - CENTER: Action messages (group member actions) and Call messages
 * - RIGHT: Messages sent by the current logged-in user (outgoing)
 * - LEFT: Messages from other users (incoming)
 *
 * @param message The message to determine alignment for
 * @param loggedInUser The currently logged-in user (optional, will fetch from CometChat if null)
 * @return The [MessageAlignment] for the message
 */
fun getMessageAlignment(message: BaseMessage, loggedInUser: User? = null): MessageAlignment {
    val currentUser = loggedInUser ?: CometChat.getLoggedInUser()
    
    return when {
        // Action messages (group member joined, left, kicked, banned, scope changed)
        message.category == CometChatConstants.CATEGORY_ACTION -> MessageAlignment.CENTER
        
        // Call messages (audio, video calls)
        message.category == CometChatConstants.CATEGORY_CALL -> MessageAlignment.CENTER
        
        // Outgoing messages (sent by current user)
        message.sender?.uid == currentUser?.uid -> MessageAlignment.RIGHT
        
        // Incoming messages (from other users)
        else -> MessageAlignment.LEFT
    }
}

/**
 * Determines whether a date separator should be shown between two messages.
 *
 * A date separator is shown when:
 * - The previous message is null (first message in the list)
 * - The current and previous messages are from different calendar days
 *
 * @param currentMessage The current message being rendered
 * @param previousMessage The previous message in the list (null if current is first)
 * @return True if a date separator should be displayed before the current message
 */
fun shouldShowDateSeparator(currentMessage: BaseMessage, previousMessage: BaseMessage?): Boolean {
    if (previousMessage == null) return true
    
    val currentCal = Calendar.getInstance().apply {
        timeInMillis = currentMessage.sentAt * 1000
    }
    val previousCal = Calendar.getInstance().apply {
        timeInMillis = previousMessage.sentAt * 1000
    }
    
    return currentCal.get(Calendar.DAY_OF_YEAR) != previousCal.get(Calendar.DAY_OF_YEAR) ||
            currentCal.get(Calendar.YEAR) != previousCal.get(Calendar.YEAR)
}

/**
 * Formats a timestamp for display in a date separator.
 *
 * Returns:
 * - "Today" for today's date
 * - "Yesterday" for yesterday's date
 * - Formatted date string (e.g., "January 15, 2024") for older dates
 *
 * @param timestamp The timestamp in seconds
 * @return The formatted date string
 */
fun formatDateSeparator(timestamp: Long): String {
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
            val dateFormat = java.text.SimpleDateFormat("MMMM d, yyyy", java.util.Locale.getDefault())
            dateFormat.format(java.util.Date(timestamp * 1000))
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
