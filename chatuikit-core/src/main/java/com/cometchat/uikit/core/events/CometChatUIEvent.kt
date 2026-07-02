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

    /**
     * Event emitted when the MessageList resolves the agent chat thread parentMessageId.
     * This happens when [fetchLastAgentConversation] determines the thread root from the
     * last conversation message. The Composer observes this to sync its parentMessageId
     * so that sent messages are correctly associated with the thread.
     *
     * @param receiverId The user/group ID of the agent conversation
     * @param parentMessageId The resolved parent message ID for the thread
     */
    data class AgentChatThreadResolved(
        val receiverId: String,
        val parentMessageId: Long
    ) : CometChatUIEvent()

    /**
     * Event emitted when a user taps a card action (developer card or agent card block).
     * For developer cards, [message] is a CardMessage.
     * For agent card blocks (nested inside the AI-assistant bubble), [message] is the owning AIAssistantMessage.
     * [actionEvent] is the raw renderer action event from the cards library.
     *
     * This event enables the app to receive actions from nested agent-card blocks
     * where a direct callback/lambda path is not reachable (§2.6.1 of the Card Messages spec).
     */
    data class CardActionClicked(
        val message: BaseMessage,
        val actionEvent: Any
    ) : CometChatUIEvent()
}
