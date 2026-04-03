package com.cometchat.uikit.core.domain.repository

import com.cometchat.chat.core.ReactionsRequest
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Reaction

/**
 * Repository interface defining data operations contract for reaction list.
 * Lives in domain layer - no implementation details.
 *
 * This interface allows for custom implementations to be injected,
 * enabling flexibility in data fetching strategies (remote, local, cached).
 */
interface ReactionListRepository {

    /**
     * Fetches reactions based on the provided request configuration.
     * @param request The configured ReactionsRequest with pagination and filters
     * @return Result containing list of Reaction objects or error
     */
    suspend fun fetchReactions(request: ReactionsRequest): Result<List<Reaction>>

    /**
     * Removes a reaction from a message.
     * @param messageId The ID of the message to remove reaction from
     * @param emoji The emoji reaction to remove
     * @return Result containing the updated BaseMessage or error
     */
    suspend fun removeReaction(messageId: Long, emoji: String): Result<BaseMessage>
}
