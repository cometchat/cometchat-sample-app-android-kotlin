package com.cometchat.uikit.core.domain.repository

import com.cometchat.chat.models.Conversation
import com.cometchat.chat.core.ConversationsRequest

/**
 * Repository interface defining data operations contract for conversations.
 * Lives in domain layer - no implementation details.
 * 
 * This interface allows for custom implementations to be injected,
 * enabling flexibility in data fetching strategies (remote, local, cached).
 */
interface ConversationListRepository {
    
    /**
     * Fetches conversations based on the provided request configuration.
     * @param request The configured ConversationsRequest with pagination and filters
     * @return Result containing list of conversations or error
     */
    suspend fun getConversations(
        request: ConversationsRequest
    ): Result<List<Conversation>>
    
    /**
     * Deletes a conversation.
     * @param conversationWith The ID of the user or group
     * @param conversationType The type of conversation (user/group)
     * @return Result indicating success or failure
     */
    suspend fun deleteConversation(
        conversationWith: String,
        conversationType: String
    ): Result<Unit>
    
    /**
     * Marks a conversation as delivered, clearing unread count.
     * @param conversation The conversation to mark as delivered
     * @return Result indicating success or failure
     */
    suspend fun markAsDelivered(
        conversation: Conversation
    ): Result<Unit>
    
    /**
     * Checks if there are more conversations to fetch (pagination).
     * @return true if more conversations are available
     */
    fun hasMoreConversations(): Boolean
}
