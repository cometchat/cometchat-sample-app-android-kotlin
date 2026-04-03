package com.cometchat.uikit.core.domain.usecase

import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.TextMessage
import com.cometchat.uikit.core.domain.repository.MessageComposerRepository

/**
 * Use case for editing an existing text message.
 * Contains business logic for message editing operations.
 *
 * This use case is used by the CometChatMessageComposerViewModel to edit
 * existing text messages. Only text messages sent by the current user
 * can be edited.
 *
 * @param repository The repository to edit messages through
 */
open class EditMessageUseCase(
    private val repository: MessageComposerRepository
) {
    /**
     * Edits an existing text message.
     *
     * This method updates the text content of an existing message. The message
     * object should have:
     * - id: The original message ID
     * - text: The updated message content
     * - receiverId: The ID of the user or group
     * - receiverType: Either "user" or "group"
     *
     * Note: Only text messages sent by the current user can be edited.
     * The edit operation will fail if the message was sent by another user
     * or if the message is not a text message.
     *
     * @param message The TextMessage object with updated text content
     * @return Result containing the edited BaseMessage with updated metadata on success,
     *         or error on failure
     */
    open suspend operator fun invoke(message: TextMessage): Result<BaseMessage> {
        return repository.editMessage(message)
    }
}
