package com.cometchat.uikit.core.domain.usecase

import com.cometchat.chat.models.Conversation
import com.cometchat.chat.core.ConversationsRequest
import com.cometchat.uikit.core.domain.repository.SearchRepository

/**
 * Use case for fetching conversations in search context.
 * Contains business logic for conversation retrieval with pagination support.
 * 
 * @param repository The search repository to fetch conversations from
 */
open class FetchConversationsUseCase(
    private val repository: SearchRepository
) {
    /**
     * Fetches conversations based on the provided request.
     * @param request The configured ConversationsRequest
     * @return Result containing list of conversations or error
     */
    open suspend operator fun invoke(
        request: ConversationsRequest
    ): Result<List<Conversation>> {
        return repository.getConversations(request)
    }
    
    /**
     * Checks if there are more conversations available for pagination.
     * @return true if more conversations can be fetched
     */
    open fun hasMore(): Boolean = repository.hasMoreConversations()
}
