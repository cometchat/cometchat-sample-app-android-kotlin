package com.cometchat.uikit.core.data.datasource

import com.cometchat.chat.core.ReactionsRequest
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Reaction

/**
 * Interface defining data source operations for reaction list.
 * Lives in data layer - defines contract for data fetching.
 * Allows for different implementations (remote, local, mock).
 */
interface ReactionListDataSource {

    /**
     * Fetches reactions from the data source.
     * @param request The configured ReactionsRequest
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
