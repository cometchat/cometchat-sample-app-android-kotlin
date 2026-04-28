package com.cometchat.uikit.core.data.repository

import com.cometchat.chat.core.ConversationsRequest
import com.cometchat.chat.core.MessagesRequest
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Conversation
import com.cometchat.uikit.core.data.datasource.SearchDataSource
import com.cometchat.uikit.core.domain.repository.SearchRepository

/**
 * Repository implementation that coordinates data sources for search functionality.
 * Decides where data comes from and handles data transformation.
 * Accepts SearchDataSource interface for flexibility.
 * 
 * @param dataSource The data source to fetch data (interface, not concrete)
 */
class SearchRepositoryImpl(
    private val dataSource: SearchDataSource
) : SearchRepository {
    
    private var hasMoreConversations = true
    private var hasMoreMessages = true
    
    /**
     * Fetches conversations from the data source based on search criteria.
     * Tracks pagination state based on results.
     * 
     * @param request The configured ConversationsRequest
     * @return Result containing list of conversations or error
     */
    override suspend fun getConversations(
        request: ConversationsRequest
    ): Result<List<Conversation>> {
        return try {
            val conversations = dataSource.fetchConversations(request)
            hasMoreConversations = conversations.isNotEmpty()
            Result.success(conversations)
        } catch (e: CometChatException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Fetches messages from the data source based on search criteria.
     * Tracks pagination state based on results.
     * 
     * @param request The configured MessagesRequest
     * @return Result containing list of messages or error
     */
    override suspend fun getMessages(
        request: MessagesRequest
    ): Result<List<BaseMessage>> {
        return try {
            val messages = dataSource.fetchMessages(request)
            hasMoreMessages = messages.isNotEmpty()
            Result.success(messages)
        } catch (e: CometChatException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Checks if there are more conversations available for pagination.
     * @return true if more conversations can be fetched
     */
    override fun hasMoreConversations(): Boolean = hasMoreConversations
    
    /**
     * Checks if there are more messages available for pagination.
     * @return true if more messages can be fetched
     */
    override fun hasMoreMessages(): Boolean = hasMoreMessages
}
