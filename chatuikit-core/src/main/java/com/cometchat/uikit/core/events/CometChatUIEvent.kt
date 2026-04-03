package com.cometchat.uikit.core.events

import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.User

/**
 * Sealed class hierarchy representing all UI-related events.
 * Provides type-safe event handling for UI state coordination across components.
 */
sealed class CometChatUIEvent {
    /**
     * Event emitted to show a custom panel.
     * @param id The ID map of the panel
     * @param position The position of the panel
     * @param content The content to display (cast to appropriate UI type in consuming module)
     */
    data class ShowPanel(
        val id: Map<String, String>,
        val position: CustomUIPosition,
        val content: Any
    ) : CometChatUIEvent()

    /**
     * Event emitted to hide a custom panel.
     * @param id The ID map of the panel
     * @param position The position of the panel
     */
    data class HidePanel(
        val id: Map<String, String>,
        val position: CustomUIPosition
    ) : CometChatUIEvent()

    /**
     * Event emitted when the active chat changes.
     * @param id The ID map of the active chat
     * @param message The last message in the chat (nullable)
     * @param user The user associated with the chat (nullable for group chats)
     * @param group The group associated with the chat (nullable for user chats)
     * @param unreadCount The unread message count
     */
    data class ActiveChatChanged(
        val id: Map<String, String>,
        val message: BaseMessage?,
        val user: User?,
        val group: Group?,
        val unreadCount: Int = 0
    ) : CometChatUIEvent()

    /**
     * Event emitted for compose message actions.
     * @param id The ID of the compose action
     * @param text The text to compose
     */
    data class ComposeMessage(
        val id: String,
        val text: String
    ) : CometChatUIEvent()

    /**
     * Event emitted to open a chat with a user or group.
     * @param user The user to open chat with (nullable for group chats)
     * @param group The group to open chat with (nullable for user chats)
     */
    data class OpenChat(
        val user: User?,
        val group: Group?
    ) : CometChatUIEvent()
}
