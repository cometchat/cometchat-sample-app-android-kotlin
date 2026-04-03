package com.cometchat.uikit.core.data.repository

import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.MessagesRequest
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Conversation
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.data.datasource.MessageListDataSource
import com.cometchat.uikit.core.data.datasource.MessageListDataSourceImpl
import com.cometchat.uikit.core.domain.model.SurroundingMessagesResult
import com.cometchat.uikit.core.domain.repository.MessageListRepository

/**
 * Repository implementation for message list operations.
 * Handles business logic, request building, and state management.
 * Delegates raw SDK calls to [MessageListDataSource].
 *
 * @param dataSource The data source for SDK operations. Defaults to [MessageListDataSourceImpl].
 */
class MessageListRepositoryImpl(
    private val dataSource: MessageListDataSource = MessageListDataSourceImpl()
) : MessageListRepository {
    
    private var messagesRequest: MessagesRequest? = null
    private var messagesRequestBuilder: MessagesRequest.MessagesRequestBuilder? = null
    private var hasMore: Boolean = true
    private var userId: String? = null
    private var groupId: String? = null
    private var latestMessageId: Long = -1
    
    override fun configureForUser(
        user: User,
        messagesTypes: List<String>,
        messagesCategories: List<String>,
        parentMessageId: Long,
        messagesRequestBuilder: MessagesRequest.MessagesRequestBuilder?
    ) {
        userId = user.uid
        groupId = null
        
        this.messagesRequestBuilder = messagesRequestBuilder ?: MessagesRequest.MessagesRequestBuilder()
            .setTypes(messagesTypes)
            .setCategories(messagesCategories)
            .setLimit(30)
            .hideReplies(true)
            .apply {
                if (parentMessageId > -1) setParentMessageId(parentMessageId)
            }
        
        messagesRequest = this.messagesRequestBuilder?.setUID(user.uid)?.build()
        hasMore = true
    }
    
    override fun configureForGroup(
        group: Group,
        messagesTypes: List<String>,
        messagesCategories: List<String>,
        parentMessageId: Long,
        messagesRequestBuilder: MessagesRequest.MessagesRequestBuilder?
    ) {
        groupId = group.guid
        userId = null
        
        this.messagesRequestBuilder = messagesRequestBuilder ?: MessagesRequest.MessagesRequestBuilder()
            .setTypes(messagesTypes)
            .setCategories(messagesCategories)
            .setLimit(30)
            .hideReplies(true)
            .apply {
                if (parentMessageId > -1) setParentMessageId(parentMessageId)
            }
        
        messagesRequest = this.messagesRequestBuilder?.setGUID(group.guid)?.build()
        hasMore = true
    }

    
    override suspend fun fetchPreviousMessages(): Result<List<BaseMessage>> {
        val request = messagesRequest
            ?: return Result.failure(CometChatException("ERROR", "MessagesRequest not configured"))
        
        return try {
            val messages = dataSource.fetchPreviousMessages(request)
            hasMore = messages.isNotEmpty()
            Result.success(messages)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun fetchNextMessages(fromMessageId: Long): Result<List<BaseMessage>> {
        val builder = messagesRequestBuilder
            ?: return Result.failure(CometChatException("ERROR", "MessagesRequestBuilder not configured"))
        
        val nextRequest = builder
            .setMessageId(fromMessageId)
            .build()
        
        return try {
            val messages = dataSource.fetchNextMessages(nextRequest)
            Result.success(messages)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getConversation(id: String, type: String): Result<Conversation> {
        return try {
            val conversation = dataSource.getConversation(id, type)
            Result.success(conversation)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getMessage(messageId: Long): Result<BaseMessage> {
        return try {
            val message = dataSource.getMessage(messageId)
            Result.success(message)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteMessage(message: BaseMessage): Result<BaseMessage> {
        return try {
            val deletedMessage = dataSource.deleteMessage(message.id)
            Result.success(deletedMessage)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun flagMessage(messageId: Long, reason: String, remark: String): Result<Unit> {
        return try {
            dataSource.flagMessage(messageId, reason, remark)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun addReaction(messageId: Long, emoji: String): Result<BaseMessage> {
        return try {
            val updatedMessage = dataSource.addReaction(messageId, emoji)
            Result.success(updatedMessage)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun removeReaction(messageId: Long, emoji: String): Result<BaseMessage> {
        return try {
            val updatedMessage = dataSource.removeReaction(messageId, emoji)
            Result.success(updatedMessage)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun markAsDelivered(message: BaseMessage): Result<Unit> {
        return try {
            dataSource.markAsDelivered(message)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun markAsRead(message: BaseMessage): Result<Unit> {
        return try {
            dataSource.markAsRead(message)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun markAsUnread(message: BaseMessage): Result<Conversation> {
        return try {
            val conversation = dataSource.markAsUnread(message)
            Result.success(conversation)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override fun hasMorePreviousMessages(): Boolean = hasMore
    
    override fun resetRequest() {
        hasMore = true
        val currentUserId = userId
        val currentGroupId = groupId
        // Reset messageId to -1 to fetch from the latest messages (matches XML implementation)
        // Without this, the SDK's internal pagination state isn't reset and fetchPrevious()
        // would continue from the old anchor position instead of the latest
        messagesRequest = when {
            currentUserId != null -> messagesRequestBuilder?.setMessageId(-1)?.setUID(currentUserId)?.build()
            currentGroupId != null -> messagesRequestBuilder?.setMessageId(-1)?.setGUID(currentGroupId)?.build()
            else -> null
        }
    }


    override suspend fun fetchSurroundingMessages(messageId: Long): Result<SurroundingMessagesResult> {
        val builder = messagesRequestBuilder
            ?: return Result.failure(CometChatException("ERROR", "MessagesRequestBuilder not configured"))

        // First, get the target message
        val targetMessageResult = getMessage(messageId)
        if (targetMessageResult.isFailure) {
            return Result.failure(targetMessageResult.exceptionOrNull() as? CometChatException 
                ?: CometChatException("ERROR", "Failed to get target message"))
        }
        val targetMessage = targetMessageResult.getOrThrow()

        // Build request with setMessageId to fetch previous (older) messages
        val previousRequest = builder
            .setMessageId(messageId)
            .build()

        val olderMessages = try {
            dataSource.fetchPreviousMessages(previousRequest)
        } catch (e: Exception) {
            return Result.failure(e as? CometChatException 
                ?: CometChatException("ERROR", "Failed to fetch older messages"))
        }

        // Build new request with setMessageId to fetch next (newer) messages
        val nextRequest = builder
            .setMessageId(messageId)
            .build()

        val newerMessages = try {
            dataSource.fetchNextMessages(nextRequest)
        } catch (e: Exception) {
            return Result.failure(e as? CometChatException 
                ?: CometChatException("ERROR", "Failed to fetch newer messages"))
        }

        // Combine results into SurroundingMessagesResult
        return Result.success(
            SurroundingMessagesResult(
                olderMessages = olderMessages,
                targetMessage = targetMessage,
                newerMessages = newerMessages,
                hasMorePrevious = olderMessages.isNotEmpty(),
                hasMoreNext = newerMessages.isNotEmpty()
            )
        )
    }

    override suspend fun fetchActionMessages(fromMessageId: Long): Result<List<BaseMessage>> {
        val currentUserId = userId
        val currentGroupId = groupId
        
        // Build a new request specifically for ACTION category messages
        val actionRequestBuilder = MessagesRequest.MessagesRequestBuilder()
            .setCategories(listOf(CometChatConstants.CATEGORY_ACTION))
            .setMessageId(fromMessageId)
            .setLimit(30)
        
        // Apply user or group configuration
        val request = when {
            currentUserId != null -> actionRequestBuilder.setUID(currentUserId).build()
            currentGroupId != null -> actionRequestBuilder.setGUID(currentGroupId).build()
            else -> return Result.failure(CometChatException("ERROR", "Repository not configured for user or group"))
        }
        
        return try {
            val messages = dataSource.fetchNextMessages(request)
            Result.success(messages)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun rebuildRequestFromMessageId(messageId: Long) {
        hasMore = true
        val currentUserId = userId
        val currentGroupId = groupId
        messagesRequest = when {
            currentUserId != null -> messagesRequestBuilder?.setMessageId(messageId)?.setUID(currentUserId)?.build()
            currentGroupId != null -> messagesRequestBuilder?.setMessageId(messageId)?.setGUID(currentGroupId)?.build()
            else -> null
        }
    }

    override fun getLatestMessageId(): Long = latestMessageId

    override fun setLatestMessageId(messageId: Long) {
        latestMessageId = messageId
    }
}
