package com.cometchat.uikit.compose.preview.data.repository

import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.MessagesRequest
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Conversation
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.TextMessage
import com.cometchat.chat.models.User
import com.cometchat.uikit.compose.preview.domain.PreviewMockData
import com.cometchat.uikit.core.domain.model.SurroundingMessagesResult
import com.cometchat.uikit.core.domain.repository.MessageListRepository

/**
 * A mock repository implementation for Compose Previews of CometChatMessageList.
 * Returns pre-populated mock messages without making any SDK calls.
 *
 * @param initialMessages The list of messages to return. Defaults to sample messages.
 * @param simulateError If true, all operations will return failure results.
 * @param simulateEmpty If true, returns an empty list instead of messages.
 */
class PreviewMessageListRepository(
    private val initialMessages: List<BaseMessage> = createSampleMessages(),
    private val simulateError: Boolean = false,
    private val simulateEmpty: Boolean = false
) : MessageListRepository {

    private var latestMessageId: Long = -1

    override suspend fun fetchPreviousMessages(): Result<List<BaseMessage>> {
        if (simulateError) {
            return Result.failure(
                com.cometchat.chat.exceptions.CometChatException(
                    "PREVIEW_ERROR",
                    "Simulated error for preview"
                )
            )
        }
        if (simulateEmpty) {
            return Result.success(emptyList())
        }
        return Result.success(initialMessages)
    }

    override suspend fun fetchNextMessages(fromMessageId: Long): Result<List<BaseMessage>> =
        Result.success(emptyList())

    override suspend fun getConversation(id: String, type: String): Result<Conversation> {
        val conversation = Conversation(id, type)
        return Result.success(conversation)
    }

    override suspend fun getMessage(messageId: Long): Result<BaseMessage> {
        val msg = initialMessages.find { it.id == messageId }
        return if (msg != null) Result.success(msg)
        else Result.failure(
            com.cometchat.chat.exceptions.CometChatException("NOT_FOUND", "Message not found")
        )
    }

    override suspend fun deleteMessage(message: BaseMessage): Result<BaseMessage> =
        Result.success(message)

    override suspend fun flagMessage(messageId: Long, reason: String, remark: String): Result<Unit> =
        Result.success(Unit)

    override suspend fun addReaction(messageId: Long, emoji: String): Result<BaseMessage> {
        val msg = initialMessages.find { it.id == messageId }
        return if (msg != null) Result.success(msg)
        else Result.failure(
            com.cometchat.chat.exceptions.CometChatException("NOT_FOUND", "Message not found")
        )
    }

    override suspend fun removeReaction(messageId: Long, emoji: String): Result<BaseMessage> {
        val msg = initialMessages.find { it.id == messageId }
        return if (msg != null) Result.success(msg)
        else Result.failure(
            com.cometchat.chat.exceptions.CometChatException("NOT_FOUND", "Message not found")
        )
    }

    override suspend fun markAsDelivered(message: BaseMessage): Result<Unit> =
        Result.success(Unit)

    override suspend fun markAsRead(message: BaseMessage): Result<Unit> =
        Result.success(Unit)

    override suspend fun markAsUnread(message: BaseMessage): Result<Conversation> =
        Result.success(Conversation("conv_1", CometChatConstants.CONVERSATION_TYPE_USER))

    override fun hasMorePreviousMessages(): Boolean = false

    override fun resetRequest() { /* no-op */ }

    override fun configureForUser(
        user: User,
        messagesTypes: List<String>,
        messagesCategories: List<String>,
        parentMessageId: Long,
        messagesRequestBuilder: MessagesRequest.MessagesRequestBuilder?
    ) { /* no-op */ }

    override fun configureForGroup(
        group: Group,
        messagesTypes: List<String>,
        messagesCategories: List<String>,
        parentMessageId: Long,
        messagesRequestBuilder: MessagesRequest.MessagesRequestBuilder?
    ) { /* no-op */ }

    override suspend fun fetchSurroundingMessages(messageId: Long): Result<SurroundingMessagesResult> {
        val targetMsg = initialMessages.firstOrNull()
            ?: PreviewMockData.createMockTextMessage(id = messageId, text = "Target message")
        return Result.success(
            SurroundingMessagesResult(
                olderMessages = emptyList(),
                targetMessage = targetMsg,
                newerMessages = emptyList(),
                hasMorePrevious = false,
                hasMoreNext = false
            )
        )
    }

    override suspend fun fetchActionMessages(fromMessageId: Long): Result<List<BaseMessage>> =
        Result.success(emptyList())

    override fun rebuildRequestFromMessageId(messageId: Long) { /* no-op */ }

    override fun getLatestMessageId(): Long = latestMessageId

    override fun setLatestMessageId(messageId: Long) {
        latestMessageId = messageId
    }

    companion object {
        fun createSampleMessages(): List<BaseMessage> {
            val alice = PreviewMockData.createMockUser(uid = "u1", name = "Alice Smith")
            val bob = PreviewMockData.createMockUser(uid = "u2", name = "Bob Johnson")
            val me = PreviewMockData.createMockUser(uid = "me", name = "Me")

            val baseTime = System.currentTimeMillis() / 1000

            return listOf(
                PreviewMockData.createMockTextMessage(
                    id = 1L, text = "Hey! How are you?",
                    sentAt = baseTime - 300, sender = alice
                ),
                PreviewMockData.createMockTextMessage(
                    id = 2L, text = "I'm doing great, thanks! How about you?",
                    sentAt = baseTime - 240, sender = me
                ),
                PreviewMockData.createMockTextMessage(
                    id = 3L, text = "Pretty good! Working on the new feature.",
                    sentAt = baseTime - 180, sender = alice
                ),
                PreviewMockData.createMockTextMessage(
                    id = 4L, text = "That sounds exciting! Need any help?",
                    sentAt = baseTime - 120, sender = me
                ),
                PreviewMockData.createMockTextMessage(
                    id = 5L, text = "Sure, let's discuss in the meeting tomorrow.",
                    sentAt = baseTime - 60, sender = alice
                ),
                PreviewMockData.createMockTextMessage(
                    id = 6L, text = "Sounds good! See you then 👍",
                    sentAt = baseTime, sender = me,
                    deliveredAt = baseTime, readAt = baseTime
                )
            )
        }
    }
}
