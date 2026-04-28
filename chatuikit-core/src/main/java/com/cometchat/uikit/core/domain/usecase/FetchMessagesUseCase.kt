package com.cometchat.uikit.core.domain.usecase

import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.core.MessagesRequest
import com.cometchat.uikit.core.domain.repository.SearchRepository

/**
 * Use case for fetching messages in search context.
 * Contains business logic for message retrieval with pagination support.
 * 
 * @param repository The search repository to fetch messages from
 */
open class FetchMessagesUseCase(
    private val repository: SearchRepository
) {
    /**
     * Fetches messages based on the provided request.
     * @param request The configured MessagesRequest
     * @return Result containing list of messages or error
     */
    open suspend operator fun invoke(
        request: MessagesRequest
    ): Result<List<BaseMessage>> {
        return repository.getMessages(request)
    }
    
    /**
     * Checks if there are more messages available for pagination.
     * @return true if more messages can be fetched
     */
    open fun hasMore(): Boolean = repository.hasMoreMessages()
}
