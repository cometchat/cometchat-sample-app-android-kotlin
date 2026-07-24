package com.cometchat.uikit.core.domain.usecase

import com.cometchat.chat.models.BaseMessage
import com.cometchat.uikit.core.domain.repository.MessageComposerRepository

/**
 * Use case for editing an existing message.
 * Contains business logic for message editing operations.
 *
 * This use case is used by the CometChatMessageComposerViewModel to edit
 * existing text messages and media message captions. Only messages sent
 * by the current user can be edited.
 *
 * @param repository The repository to edit messages through
 */
open class EditMessageUseCase(
    private val repository: MessageComposerRepository
) {
    /**
     * Edits an existing message.
     *
     * This method updates the content of an existing message. The message
     * object should have:
     * - id: The original message ID
     * - the updated content (text for TextMessage, caption for MediaMessage)
     * - receiverId: The ID of the user or group
     * - receiverType: Either "user" or "group"
     *
     * Note: Only messages sent by the current user can be edited.
     * The edit operation will fail if the message was sent by another user.
     *
     * @param message The message with updated content
     * @return Result containing the edited BaseMessage with updated metadata on success,
     *         or error on failure
     */
    open suspend operator fun invoke(message: BaseMessage): Result<BaseMessage> {
        return repository.editMessage(message)
    }
}
