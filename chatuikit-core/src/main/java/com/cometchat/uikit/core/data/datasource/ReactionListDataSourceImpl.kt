package com.cometchat.uikit.core.data.datasource

import com.cometchat.chat.core.CometChat
import com.cometchat.chat.core.ReactionsRequest
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Reaction
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Implementation of ReactionListDataSource that communicates with CometChat SDK.
 * Contains NO business logic - just raw data fetching.
 * This is the default implementation used for remote data operations.
 */
class ReactionListDataSourceImpl : ReactionListDataSource {

    /**
     * Fetches reactions from CometChat SDK.
     * @param request The configured ReactionsRequest
     * @return Result containing list of Reaction objects or error
     */
    override suspend fun fetchReactions(request: ReactionsRequest): Result<List<Reaction>> =
        suspendCancellableCoroutine { continuation ->
            request.fetchNext(object : CometChat.CallbackListener<List<Reaction>>() {
                override fun onSuccess(reactions: List<Reaction>) {
                    continuation.resume(Result.success(reactions))
                }

                override fun onError(exception: CometChatException) {
                    continuation.resume(Result.failure(exception))
                }
            })
        }

    /**
     * Removes a reaction from a message using CometChat SDK.
     * @param messageId The ID of the message to remove reaction from
     * @param emoji The emoji reaction to remove
     * @return Result containing the updated BaseMessage or error
     */
    override suspend fun removeReaction(messageId: Long, emoji: String): Result<BaseMessage> =
        suspendCancellableCoroutine { continuation ->
            CometChat.removeReaction(messageId, emoji, object : CometChat.CallbackListener<BaseMessage>() {
                override fun onSuccess(message: BaseMessage) {
                    continuation.resume(Result.success(message))
                }

                override fun onError(exception: CometChatException) {
                    continuation.resume(Result.failure(exception))
                }
            })
        }
}
