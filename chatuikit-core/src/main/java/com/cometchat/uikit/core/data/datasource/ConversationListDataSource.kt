package com.cometchat.uikit.core.data.datasource

import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Conversation
import com.cometchat.chat.core.ConversationsRequest

/**
 * Interface defining data source operations for conversations.
 * Lives in data layer - defines contract for data fetching.
 * Allows for different implementations (remote, local, mock).
 */
interface ConversationListDataSource {
    
    /**
     * Fetches conversations from the data source.
     * @param request The configured ConversationsRequest
     * @return Raw list of Conversation objects
     * @throws Exception if fetching fails
     */
    suspend fun fetchConversations(
        request: ConversationsRequest
    ): List<Conversation>
    
    /**
     * Deletes a conversation.
     * @param conversationWith The ID of the user or group
     * @param conversationType The type of conversation (user/group)
     * @return Success message
     * @throws Exception if deletion fails
     */
    suspend fun deleteConversation(
        conversationWith: String,
        conversationType: String
    ): String
    
    /**
     * Marks messages as delivered.
     * @param message The message to mark as delivered
     * @throws Exception if marking fails
     */
    suspend fun markAsDelivered(message: BaseMessage)
}
