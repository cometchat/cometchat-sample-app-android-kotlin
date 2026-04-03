package com.cometchat.uikit.core.events

import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.CustomMessage
import com.cometchat.chat.models.InteractionReceipt
import com.cometchat.chat.models.MediaMessage
import com.cometchat.chat.models.MessageReceipt
import com.cometchat.chat.models.ReactionEvent
import com.cometchat.chat.models.TextMessage
import com.cometchat.chat.models.TransientMessage
import com.cometchat.chat.models.TypingIndicator

/**
 * Sealed class hierarchy representing all message-related events.
 * Provides type-safe event handling for message lifecycle changes.
 */
sealed class CometChatMessageEvent {
    /**
     * Event emitted when a message is sent.
     * @param message The sent message
     * @param status The status of the sent message (IN_PROGRESS, SUCCESS, ERROR)
     */
    data class MessageSent(
        val message: BaseMessage,
        val status: MessageStatus
    ) : CometChatMessageEvent()

    /**
     * Event emitted when a message is edited.
     * @param message The edited message
     * @param status The status of the edit operation
     */
    data class MessageEdited(
        val message: BaseMessage,
        val status: MessageStatus
    ) : CometChatMessageEvent()

    /**
     * Event emitted when a message is deleted.
     * @param message The deleted message
     */
    data class MessageDeleted(
        val message: BaseMessage
    ) : CometChatMessageEvent()

    /**
     * Event emitted when a message is read.
     * @param message The read message
     */
    data class MessageRead(
        val message: BaseMessage
    ) : CometChatMessageEvent()

    /**
     * Event emitted when a text message is received.
     * @param message The received text message
     */
    data class TextMessageReceived(
        val message: TextMessage
    ) : CometChatMessageEvent()

    /**
     * Event emitted when a media message is received.
     * @param message The received media message
     */
    data class MediaMessageReceived(
        val message: MediaMessage
    ) : CometChatMessageEvent()

    /**
     * Event emitted when a custom message is received.
     * @param message The received custom message
     */
    data class CustomMessageReceived(
        val message: CustomMessage
    ) : CometChatMessageEvent()


    /**
     * Event emitted when typing starts.
     * @param indicator The typing indicator
     */
    data class TypingStarted(
        val indicator: TypingIndicator
    ) : CometChatMessageEvent()

    /**
     * Event emitted when typing ends.
     * @param indicator The typing indicator
     */
    data class TypingEnded(
        val indicator: TypingIndicator
    ) : CometChatMessageEvent()

    /**
     * Event emitted when messages are delivered.
     * @param receipt The message receipt
     */
    data class MessagesDelivered(
        val receipt: MessageReceipt
    ) : CometChatMessageEvent()

    /**
     * Event emitted when messages are read.
     * @param receipt The message receipt
     */
    data class MessagesRead(
        val receipt: MessageReceipt
    ) : CometChatMessageEvent()

    /**
     * Event emitted when a reaction is added to a message.
     * @param event The reaction event
     */
    data class ReactionAdded(
        val event: ReactionEvent
    ) : CometChatMessageEvent()

    /**
     * Event emitted when a reaction is removed from a message.
     * @param event The reaction event
     */
    data class ReactionRemoved(
        val event: ReactionEvent
    ) : CometChatMessageEvent()

    /**
     * Event emitted when a transient message is received.
     * @param message The transient message
     */
    data class TransientMessageReceived(
        val message: TransientMessage
    ) : CometChatMessageEvent()

    /**
     * Event emitted when a live reaction is received.
     * @param icon The drawable resource ID of the live reaction
     */
    data class LiveReaction(
        val icon: Int
    ) : CometChatMessageEvent()

    /**
     * Event emitted when replying to a message.
     * @param message The message being replied to
     * @param status The status of the reply operation
     */
    data class ReplyToMessage(
        val message: BaseMessage,
        val status: MessageStatus
    ) : CometChatMessageEvent()

    /**
     * Event emitted when a message is edited by another user.
     * @param message The edited message
     */
    data class MessageEditedByOther(
        val message: BaseMessage
    ) : CometChatMessageEvent()

    /**
     * Event emitted when a message is deleted by another user.
     * @param message The deleted message
     */
    data class MessageDeletedByOther(
        val message: BaseMessage
    ) : CometChatMessageEvent()

    /**
     * Event emitted when messages are delivered to all recipients.
     * @param receipt The message receipt
     */
    data class MessagesDeliveredToAll(
        val receipt: MessageReceipt
    ) : CometChatMessageEvent()

    /**
     * Event emitted when messages are read by all recipients.
     * @param receipt The message receipt
     */
    data class MessagesReadByAll(
        val receipt: MessageReceipt
    ) : CometChatMessageEvent()

    /**
     * Event emitted when a message is moderated.
     * @param message The moderated message
     */
    data class MessageModerated(
        val message: BaseMessage
    ) : CometChatMessageEvent()

    /**
     * Event emitted when an interaction goal is completed.
     * @param receipt The interaction receipt
     */
    data class InteractionGoalCompleted(
        val receipt: InteractionReceipt
    ) : CometChatMessageEvent()
}
