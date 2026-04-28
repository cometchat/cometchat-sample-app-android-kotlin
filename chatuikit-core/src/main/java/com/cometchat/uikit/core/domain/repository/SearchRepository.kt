package com.cometchat.uikit.core.domain.repository

import com.cometchat.chat.core.ConversationsRequest
import com.cometchat.chat.core.MessagesRequest
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Conversation

/**
 * Repository interface defining data operations contract for search functionality.
 * Lives in domain layer - no implementation details.
 * 
 * This interface allows for custom implementations to be injected,
 * enabling flexibility in data fetching strategies (remote, local, cached).
 */
interface SearchRepository {
    
    /**
     * Fetches conversations based on the provided request configuration.
     * @param request The configured ConversationsRequest with pagination and filters
     * @return Result containing list of conversations or error
     */
    suspend fun getConversations(
        request: ConversationsRequest
    ): Result<List<Conversation>>
    
    /**
     * Fetches messages based on the provided request configuration.
     * @param request The configured MessagesRequest with pagination and filters
     * @return Result containing list of messages or error
     */
    suspend fun getMessages(
        request: MessagesRequest
    ): Result<List<BaseMessage>>
    
    /**
     * Checks if there are more conversations to fetch (pagination).
     * @return true if more conversations are available
     */
    fun hasMoreConversations(): Boolean
    
    /**
     * Checks if there are more messages to fetch (pagination).
     * @return true if more messages are available
     */
    fun hasMoreMessages(): Boolean
}
