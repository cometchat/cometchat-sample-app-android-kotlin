package com.cometchat.uikit.core.domain.usecase

import com.cometchat.chat.models.BaseMessage
import com.cometchat.uikit.core.domain.repository.ReactionListRepository

/**
 * Use case for removing a reaction from a message.
 * Contains business logic for reaction removal operations.
 *
 * This use case is used by the CometChatReactionListViewModel to allow
 * users to remove their own reactions from a message.
 *
 * @param repository The repository to remove reactions through
 */
open class RemoveReactionUseCase(
    private val repository: ReactionListRepository
) {
    /**
     * Removes a reaction from a message.
     *
     * This method removes the specified emoji reaction from the message.
     * Only the logged-in user's own reactions can be removed.
     *
     * @param messageId The ID of the message to remove reaction from
     * @param emoji The emoji reaction to remove (e.g., "👍", "❤️")
     * @return Result containing the updated BaseMessage on success, or error on failure
     */
    open suspend operator fun invoke(messageId: Long, emoji: String): Result<BaseMessage> {
        return repository.removeReaction(messageId, emoji)
    }
}
