package com.cometchat.uikit.compose.preview.data.repository

import com.cometchat.chat.core.ConversationsRequest
import com.cometchat.chat.models.Conversation
import com.cometchat.uikit.compose.preview.domain.PreviewMockData
import com.cometchat.uikit.core.domain.repository.ConversationListRepository

/**
 * A mock repository implementation for Compose Previews.
 * Returns pre-populated mock data without making any SDK calls.
 * 
 * This allows previews to use the actual CometChatConversations component
 * with realistic data, ensuring the preview accurately represents the real component.
 * 
 * @param initialConversations The list of conversations to return. Defaults to sample conversations.
 * @param simulateError If true, all operations will return failure results.
 * @param simulateEmpty If true, returns an empty list instead of conversations.
 */
class PreviewConversationListRepository(
    private val initialConversations: List<Conversation> = PreviewMockData.createSampleConversations(),
    private val simulateError: Boolean = false,
    private val simulateEmpty: Boolean = false
) : ConversationListRepository {
    
    private var hasMoreData = false // Preview doesn't need pagination
    
    override suspend fun getConversations(
        request: ConversationsRequest
    ): Result<List<Conversation>> {
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
        
        return Result.success(initialConversations)
    }
    
    override suspend fun deleteConversation(
        conversationWith: String,
        conversationType: String
    ): Result<Unit> {
        if (simulateError) {
            return Result.failure(
                com.cometchat.chat.exceptions.CometChatException(
                    "PREVIEW_ERROR",
                    "Simulated error for preview"
                )
            )
        }
        return Result.success(Unit)
    }
    
    override suspend fun markAsDelivered(conversation: Conversation): Result<Unit> {
        return Result.success(Unit)
    }
    
    override fun hasMoreConversations(): Boolean = hasMoreData
}
