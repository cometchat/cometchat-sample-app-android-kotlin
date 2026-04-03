package com.cometchat.uikit.kotlin.presentation.conversations.utils

import com.cometchat.chat.models.User

/**
 * Data class representing typing indicator state for a conversation.
 *
 * This class holds information about users who are currently typing
 * in a conversation. It supports multiple users typing simultaneously
 * (common in group conversations).
 *
 * This matches the Jetpack Compose's TypingIndicator class and
 * provides a unified way to handle typing indicators across the UIKit.
 *
 * @param typingUsers List of users who are currently typing
 * @param isTyping Whether typing is currently active (true if typingUsers is not empty)
 */
data class TypingIndicator(
    val typingUsers: List<User>,
    val isTyping: Boolean = typingUsers.isNotEmpty()
) {
    /**
     * Gets the first typing user, or null if no one is typing.
     * Useful for backward compatibility with single-user typing indicator APIs.
     */
    val firstTypingUser: User?
        get() = typingUsers.firstOrNull()

    companion object {
        /**
         * Creates a TypingIndicator from a single SDK TypingIndicator.
         */
        fun fromSdkIndicator(indicator: com.cometchat.chat.models.TypingIndicator?): TypingIndicator? {
            if (indicator == null) return null
            val sender = indicator.sender ?: return null
            return TypingIndicator(
                typingUsers = listOf(sender),
                isTyping = true
            )
        }

        /**
         * Creates a TypingIndicator from multiple SDK TypingIndicators.
         * Used when multiple users are typing in a group conversation.
         */
        fun fromSdkIndicators(indicators: List<com.cometchat.chat.models.TypingIndicator>): TypingIndicator? {
            if (indicators.isEmpty()) return null
            val typingUsers = indicators.mapNotNull { it.sender }
            if (typingUsers.isEmpty()) return null
            return TypingIndicator(
                typingUsers = typingUsers,
                isTyping = true
            )
        }
    }
}
