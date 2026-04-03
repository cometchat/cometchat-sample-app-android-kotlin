package com.cometchat.uikit.compose.preview.domain

import com.cometchat.chat.core.ConversationsRequest
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.Conversation
import com.cometchat.uikit.core.domain.repository.ConversationListRepository
import com.cometchat.uikit.core.domain.usecase.DeleteConversationUseCase
import com.cometchat.uikit.core.domain.usecase.GetConversationListUseCase
import com.cometchat.uikit.core.domain.usecase.RefreshConversationListUseCase

/**
 * Preview-specific use cases that provide controlled behavior for testing
 * different scenarios in Compose previews.
 */

// ============================================================================
// Custom GetConversationListUseCase Implementations
// ============================================================================

/**
 * A custom GetConversationListUseCase that returns predefined conversations.
 * Useful for testing specific conversation scenarios.
 */
class PreviewGetConversationListUseCase(
    private val conversations: List<Conversation> = PreviewMockData.createSampleConversations(),
    private val simulateError: Boolean = false,
    private val simulateDelay: Boolean = false
) : GetConversationListUseCase(PreviewNoOpRepository()) {
    
    override suspend operator fun invoke(request: ConversationsRequest): Result<List<Conversation>> {
        if (simulateDelay) {
            kotlinx.coroutines.delay(2000) // Simulate network delay
        }
        
        if (simulateError) {
            return Result.failure(
                CometChatException("PREVIEW_ERROR", "Simulated error for preview testing")
            )
        }
        
        return Result.success(conversations)
    }
    
    override fun hasMore(): Boolean = false
}

/**
 * GetConversationListUseCase that always returns an empty list.
 * Useful for testing empty state.
 */
class PreviewEmptyConversationListUseCase : GetConversationListUseCase(PreviewNoOpRepository()) {
    override suspend operator fun invoke(request: ConversationsRequest): Result<List<Conversation>> {
        return Result.success(emptyList())
    }
    
    override fun hasMore(): Boolean = false
}

/**
 * GetConversationListUseCase that always returns an error.
 * Useful for testing error state.
 */
class PreviewErrorConversationListUseCase(
    private val errorCode: String = "NETWORK_ERROR",
    private val errorMessage: String = "Failed to load conversations. Please check your connection."
) : GetConversationListUseCase(PreviewNoOpRepository()) {
    override suspend operator fun invoke(request: ConversationsRequest): Result<List<Conversation>> {
        return Result.failure(CometChatException(errorCode, errorMessage))
    }
    
    override fun hasMore(): Boolean = false
}

/**
 * GetConversationListUseCase that supports pagination simulation.
 */
class PreviewPaginatedConversationListUseCase(
    private val allConversations: List<Conversation> = PreviewMockData.createLargeConversationList(),
    private val pageSize: Int = 10
) : GetConversationListUseCase(PreviewNoOpRepository()) {
    
    private var currentPage = 0
    
    override suspend operator fun invoke(request: ConversationsRequest): Result<List<Conversation>> {
        val startIndex = currentPage * pageSize
        val endIndex = minOf(startIndex + pageSize, allConversations.size)
        
        if (startIndex >= allConversations.size) {
            return Result.success(emptyList())
        }
        
        currentPage++
        return Result.success(allConversations.subList(startIndex, endIndex))
    }
    
    override fun hasMore(): Boolean = currentPage * pageSize < allConversations.size
    
    fun reset() {
        currentPage = 0
    }
}

// ============================================================================
// Custom DeleteConversationUseCase Implementations
// ============================================================================

/**
 * DeleteConversationUseCase that always succeeds.
 */
class PreviewSuccessDeleteUseCase : DeleteConversationUseCase(PreviewNoOpRepository()) {
    override suspend operator fun invoke(conversation: Conversation): Result<Unit> {
        return Result.success(Unit)
    }
}

/**
 * DeleteConversationUseCase that always fails.
 */
class PreviewFailDeleteUseCase(
    private val errorMessage: String = "Failed to delete conversation"
) : DeleteConversationUseCase(PreviewNoOpRepository()) {
    override suspend operator fun invoke(conversation: Conversation): Result<Unit> {
        return Result.failure(CometChatException("DELETE_ERROR", errorMessage))
    }
}

/**
 * DeleteConversationUseCase with configurable delay.
 */
class PreviewDelayedDeleteUseCase(
    private val delayMs: Long = 1500,
    private val shouldSucceed: Boolean = true
) : DeleteConversationUseCase(PreviewNoOpRepository()) {
    override suspend operator fun invoke(conversation: Conversation): Result<Unit> {
        kotlinx.coroutines.delay(delayMs)
        return if (shouldSucceed) {
            Result.success(Unit)
        } else {
            Result.failure(CometChatException("DELETE_ERROR", "Delete operation timed out"))
        }
    }
}

// ============================================================================
// Custom RefreshConversationListUseCase Implementations
// ============================================================================

/**
 * RefreshConversationListUseCase that returns fresh data.
 */
class PreviewRefreshUseCase(
    private val conversations: List<Conversation> = PreviewMockData.createSampleConversations()
) : RefreshConversationListUseCase(PreviewNoOpRepository()) {
    override suspend operator fun invoke(
        requestBuilder: ConversationsRequest.ConversationsRequestBuilder
    ): Result<List<Conversation>> {
        return Result.success(conversations)
    }
}

/**
 * RefreshConversationListUseCase that simulates refresh with new data.
 */
class PreviewRefreshWithNewDataUseCase : RefreshConversationListUseCase(PreviewNoOpRepository()) {
    private var refreshCount = 0
    
    override suspend operator fun invoke(
        requestBuilder: ConversationsRequest.ConversationsRequestBuilder
    ): Result<List<Conversation>> {
        refreshCount++
        // Return different data on each refresh to simulate real updates
        return Result.success(PreviewMockData.createSampleConversations().map { conv ->
            conv.apply {
                unreadMessageCount = (0..10).random()
            }
        })
    }
}

// ============================================================================
// No-Op Repository for Use Cases
// ============================================================================

/**
 * A no-op repository implementation used as a placeholder for preview use cases.
 * The actual logic is overridden in the use case implementations.
 */
private class PreviewNoOpRepository : ConversationListRepository {
    override suspend fun getConversations(request: ConversationsRequest): Result<List<Conversation>> {
        return Result.success(emptyList())
    }
    
    override suspend fun deleteConversation(conversationWith: String, conversationType: String): Result<Unit> {
        return Result.success(Unit)
    }
    
    override suspend fun markAsDelivered(conversation: Conversation): Result<Unit> {
        return Result.success(Unit)
    }
    
    override fun hasMoreConversations(): Boolean = false
}
