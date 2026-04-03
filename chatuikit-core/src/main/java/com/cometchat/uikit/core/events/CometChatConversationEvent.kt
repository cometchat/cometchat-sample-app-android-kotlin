package com.cometchat.uikit.core.events

import com.cometchat.chat.models.Conversation

/**
 * Sealed class hierarchy representing all conversation-related events.
 * Provides type-safe event handling for conversation state changes.
 */
sealed class CometChatConversationEvent {
    /**
     * Event emitted when a conversation is deleted.
     * @param conversation The deleted conversation
     */
    data class ConversationDeleted(
        val conversation: Conversation
    ) : CometChatConversationEvent()
    
    /**
     * Event emitted when a conversation is updated.
     * @param conversation The updated conversation
     */
    data class ConversationUpdated(
        val conversation: Conversation
    ) : CometChatConversationEvent()
}
