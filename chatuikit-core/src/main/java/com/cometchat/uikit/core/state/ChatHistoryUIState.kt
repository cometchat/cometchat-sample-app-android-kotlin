package com.cometchat.uikit.core.state

import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.BaseMessage

/**
 * Sealed class representing UI states for the AI assistant chat history screen.
 * Used by the CometChatAIAssistantChatHistoryViewModel to communicate current state to the UI.
 */
sealed class ChatHistoryUIState {
    /**
     * Loading state - displayed while fetching chat history messages.
     */
    object Loading : ChatHistoryUIState()

    /**
     * Empty state - displayed when no chat history messages exist.
     */
    object Empty : ChatHistoryUIState()

    /**
     * Error state - displayed when fetching chat history fails.
     * @param exception The exception that caused the error
     */
    data class Error(val exception: CometChatException) : ChatHistoryUIState()

    /**
     * Content state - displayed when chat history messages are available.
     * @param messages The list of chat history messages to display
     */
    data class Content(val messages: List<BaseMessage>) : ChatHistoryUIState()
}
