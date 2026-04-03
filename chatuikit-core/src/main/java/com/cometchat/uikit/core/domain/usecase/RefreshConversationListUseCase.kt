package com.cometchat.uikit.core.domain.usecase

import com.cometchat.chat.models.Conversation
import com.cometchat.chat.core.ConversationsRequest
import com.cometchat.uikit.core.domain.repository.ConversationListRepository

/**
 * Use case for refreshing the conversation list.
 * Resets pagination and fetches fresh data from the beginning.
 * 
 * @param repository The repository to fetch conversations from
 */
open class RefreshConversationListUseCase(
    private val repository: ConversationListRepository
) {
    /**
     * Refreshes the conversation list by building a new request and fetching fresh data.
     * This resets any pagination state and starts from the beginning.
     * 
     * @param requestBuilder The builder to create a fresh ConversationsRequest
     * @return Result containing list of conversations or error
     */
    open suspend operator fun invoke(
        requestBuilder: ConversationsRequest.ConversationsRequestBuilder
    ): Result<List<Conversation>> {
        val request = requestBuilder.build()
        return repository.getConversations(request)
    }
}
