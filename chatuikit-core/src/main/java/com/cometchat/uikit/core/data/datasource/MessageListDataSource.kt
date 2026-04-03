package com.cometchat.uikit.core.data.datasource

import com.cometchat.chat.core.MessagesRequest
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Conversation

/**
 * Interface defining data source operations for message list.
 * Lives in data layer - defines contract for data fetching.
 * Allows for different implementations (remote, local, mock).
 * 
 * This datasource handles raw SDK calls without business logic.
 * Business logic like request building and state management
 * is handled by the repository layer.
 */
interface MessageListDataSource {
    
    /**
     * Fetches previous (older) messages using the provided request.
     * @param request The configured MessagesRequest
     * @return List of BaseMessage objects
     * @throws Exception if fetching fails
     */
    suspend fun fetchPreviousMessages(request: MessagesRequest): List<BaseMessage>
    
    /**
     * Fetches next (newer) messages using the provided request.
     * @param request The configured MessagesRequest
     * @return List of BaseMessage objects
     * @throws Exception if fetching fails
     */
    suspend fun fetchNextMessages(request: MessagesRequest): List<BaseMessage>
    
    /**
     * Fetches conversation details.
     * @param id The User UID or Group GUID
     * @param type The conversation type ("user" or "group")
     * @return Conversation object
     * @throws Exception if fetching fails
     */
    suspend fun getConversation(id: String, type: String): Conversation
    
    /**
     * Fetches a single message by ID.
     * @param messageId The unique message ID
     * @return BaseMessage object
     * @throws Exception if fetching fails
     */
    suspend fun getMessage(messageId: Long): BaseMessage
    
    /**
     * Deletes a message.
     * @param messageId The ID of the message to delete
     * @return The deleted BaseMessage with updated metadata
     * @throws Exception if deletion fails
     */
    suspend fun deleteMessage(messageId: Long): BaseMessage
    
    /**
     * Flags/reports a message for moderation.
     * @param messageId The message ID to flag
     * @param reason The reason for flagging
     * @param remark Additional remarks
     * @throws Exception if flagging fails
     */
    suspend fun flagMessage(messageId: Long, reason: String, remark: String)
    
    /**
     * Adds a reaction to a message.
     * @param messageId The message ID to react to
     * @param emoji The reaction emoji
     * @return The updated BaseMessage with the reaction
     * @throws Exception if adding reaction fails
     */
    suspend fun addReaction(messageId: Long, emoji: String): BaseMessage
    
    /**
     * Removes a reaction from a message.
     * @param messageId The message ID
     * @param emoji The reaction emoji to remove
     * @return The updated BaseMessage
     * @throws Exception if removing reaction fails
     */
    suspend fun removeReaction(messageId: Long, emoji: String): BaseMessage
    
    /**
     * Marks a message as delivered.
     * @param message The message to mark as delivered
     */
    suspend fun markAsDelivered(message: BaseMessage)
    
    /**
     * Marks a message as read.
     * @param message The message to mark as read
     * @throws Exception if marking fails
     */
    suspend fun markAsRead(message: BaseMessage)
    
    /**
     * Marks a message as unread.
     * @param message The message to mark as unread
     * @return Conversation object with updated unread count
     * @throws Exception if marking fails
     */
    suspend fun markAsUnread(message: BaseMessage): Conversation
}
