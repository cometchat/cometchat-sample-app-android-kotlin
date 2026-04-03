package com.cometchat.uikit.core.data.repository

import com.cometchat.chat.core.ReactionsRequest
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Reaction
import com.cometchat.uikit.core.data.datasource.ReactionListDataSource
import com.cometchat.uikit.core.domain.repository.ReactionListRepository

/**
 * Repository implementation that coordinates data sources for reaction list.
 * Decides where data comes from and handles data transformation.
 * Accepts ReactionListDataSource interface for flexibility.
 *
 * @param dataSource The data source to fetch data (interface, not concrete)
 */
class ReactionListRepositoryImpl(
    private val dataSource: ReactionListDataSource
) : ReactionListRepository {

    /**
     * Fetches reactions from the data source.
     *
     * @param request The configured ReactionsRequest with pagination and filters
     * @return Result containing list of Reaction objects or error
     */
    override suspend fun fetchReactions(request: ReactionsRequest): Result<List<Reaction>> {
        return dataSource.fetchReactions(request)
    }

    /**
     * Removes a reaction from a message via the data source.
     *
     * @param messageId The ID of the message to remove reaction from
     * @param emoji The emoji reaction to remove
     * @return Result containing the updated BaseMessage or error
     */
    override suspend fun removeReaction(messageId: Long, emoji: String): Result<BaseMessage> {
        return dataSource.removeReaction(messageId, emoji)
    }
}
