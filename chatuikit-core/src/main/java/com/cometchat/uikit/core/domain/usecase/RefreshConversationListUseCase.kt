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
     * Refreshes the conversation list by fetching the first page of [request].
     *
     * The caller builds (and owns) the fresh request and keeps it after a successful
     * refresh: its page pointer has then already consumed page 1, so subsequent
     * pagination on the same request continues from page 2 instead of re-fetching
     * page 1 and appending duplicates (ENG-37363).
     *
     * @param request A freshly built ConversationsRequest (pagination at the start)
     * @return Result containing list of conversations or error
     */
    open suspend operator fun invoke(
        request: ConversationsRequest
    ): Result<List<Conversation>> {
        return repository.getConversations(request)
    }
}
