package com.cometchat.uikit.core.domain.usecase

import com.cometchat.chat.core.ReactionsRequest
import com.cometchat.chat.models.Reaction
import com.cometchat.uikit.core.domain.repository.ReactionListRepository

/**
 * Use case for fetching reactions for a message.
 * Contains business logic for reaction retrieval with pagination support.
 *
 * This use case is used by the CometChatReactionListViewModel to fetch
 * users who reacted to a message with specific emojis.
 *
 * @param repository The repository to fetch reactions from
 */
open class FetchReactionsUseCase(
    private val repository: ReactionListRepository
) {
    /**
     * Fetches reactions based on the provided request configuration.
     *
     * The request can be configured with:
     * - messageId: The ID of the message to fetch reactions for
     * - reaction: Optional emoji filter to fetch reactions for a specific emoji
     * - limit: Number of reactions to fetch per page (default 10)
     *
     * @param request The configured ReactionsRequest with pagination and filters
     * @return Result containing list of Reaction objects or error
     */
    open suspend operator fun invoke(request: ReactionsRequest): Result<List<Reaction>> {
        return repository.fetchReactions(request)
    }
}
