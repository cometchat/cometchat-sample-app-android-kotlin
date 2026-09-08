package com.cometchat.uikit.core.domain.usecase

import com.cometchat.chat.models.CustomMessage
import com.cometchat.uikit.core.utils.CometChatThreadSubscription
import com.cometchat.uikit.core.domain.repository.MessageComposerRepository

/**
 * Use case for sending a custom message.
 * Contains business logic for custom message sending operations.
 *
 * This use case is used by the CometChatMessageComposerViewModel to send
 * custom messages with custom data payloads to users or groups.
 * Custom messages are used for implementing custom message types beyond
 * text and media, such as polls, locations, stickers, etc.
 *
 * @param repository The repository to send messages through
 */
open class SendCustomMessageUseCase(
    private val repository: MessageComposerRepository
) {
    /**
     * Sends a custom message to the configured receiver.
     *
     * This method sends a custom message containing a custom data payload to either
     * a user or group receiver. The message object should have:
     * - receiverId: The ID of the user or group to send to
     * - receiverType: Either "user" or "group"
     * - customType: The custom message type identifier
     * - customData: The custom data payload as JSONObject
     * - parentMessageId (optional): For threaded messages
     *
     * @param message The CustomMessage object to send
     * @return Result containing the sent CustomMessage with updated metadata on success,
     *         or error on failure
     */
    open suspend operator fun invoke(message: CustomMessage): Result<CustomMessage> {
        return repository.sendCustomMessage(message).onSuccess { sentMessage ->
            // The server subscribes the sender to the message's thread on every send, but
            // stamps `threadSubscribed` only on FETCHED copies — the send result comes back
            // flagless. Stamp it here so the message renders as followed straight away
            // instead of only after the next fetch (ENG-38903): a reply also mirrors the flip
            // onto its parent thread (Case 4), a root is stamped for its own thread (Case 2).
            CometChatThreadSubscription.applyOwnMessageSent(sentMessage)
        }
    }
}
