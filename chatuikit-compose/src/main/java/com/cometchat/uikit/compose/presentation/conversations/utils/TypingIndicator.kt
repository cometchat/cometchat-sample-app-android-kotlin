package com.cometchat.uikit.compose.presentation.conversations.utils

import com.cometchat.chat.models.User

/**
 * Data class representing a typing indicator state.
 *
 * This class holds information about users who are currently typing
 * in a conversation.
 *
 * @param typingUsers List of users who are currently typing
 * @param isTyping Whether typing is currently active
 */
data class TypingIndicator(
    val typingUsers: List<User>,
    val isTyping: Boolean
)