package com.cometchat.uikit.core.domain.usecase

import com.cometchat.chat.models.TextMessage
import com.cometchat.uikit.core.utils.CometChatThreadSubscription
import com.cometchat.uikit.core.domain.repository.MessageComposerRepository

/**
 * Use case for sending a text message.
 * Contains business logic for text message sending operations.
 *
 * This use case is used by the CometChatMessageComposerViewModel to send
 * text messages to users or groups. The message may include quoted message
 * information for reply functionality.
 *
 * @param repository The repository to send messages through
 */
open class SendTextMessageUseCase(
    private val repository: MessageComposerRepository
) {
    /**
     * Sends a text message to the configured receiver.
     *
     * This method sends a text message containing the composed text content
     * to either a user or group receiver. The message object should have:
     * - receiverId: The ID of the user or group to send to
     * - receiverType: Either "user" or "group"
     * - text: The message content
     * - parentMessageId (optional): For threaded messages
     * - quotedMessage (optional): For reply/quote functionality
     *
     * @param message The TextMessage object to send
     * @return Result containing the sent TextMessage with updated metadata on success,
     *         or error on failure
     */
    open suspend operator fun invoke(message: TextMessage): Result<TextMessage> {
        return repository.sendTextMessage(message).onSuccess { sentMessage ->
            // The server subscribes the sender to the message's thread on every send, but
            // stamps `threadSubscribed` only on FETCHED copies — the send result comes back
            // flagless. Stamp it here so the message renders as followed straight away
            // instead of only after the next fetch (ENG-38903): a reply also mirrors the flip
            // onto its parent thread (Case 4), a root is stamped for its own thread (Case 2).
            CometChatThreadSubscription.applyOwnMessageSent(sentMessage)
        }
    }
}
