package com.cometchat.uikit.compose.preview.data.repository

import com.cometchat.chat.core.ConversationsRequest
import com.cometchat.chat.core.MessagesRequest
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Conversation
import com.cometchat.uikit.compose.preview.domain.PreviewMockData
import com.cometchat.uikit.core.domain.repository.SearchRepository

/**
 * A mock repository implementation for Compose Previews of CometChatSearch.
 * Returns pre-populated mock data without making any SDK calls.
 *
 * @param conversations The list of conversations to return for search.
 * @param simulateError If true, all operations will return failure results.
 * @param simulateEmpty If true, returns empty lists.
 */
class PreviewSearchRepository(
    private val conversations: List<Conversation> = PreviewMockData.createSampleConversations(),
    private val simulateError: Boolean = false,
    private val simulateEmpty: Boolean = false
) : SearchRepository {

    override suspend fun getConversations(request: ConversationsRequest): Result<List<Conversation>> {
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
        return Result.success(conversations)
    }

    override suspend fun getMessages(request: MessagesRequest): Result<List<BaseMessage>> {
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
        // Return some sample text messages
        return Result.success(
            listOf(
                PreviewMockData.createMockTextMessage(
                    id = 1L,
                    text = "Hey! Are you coming to the meeting?",
                    sender = PreviewMockData.createMockUser(name = "Alice Smith")
                ),
                PreviewMockData.createMockTextMessage(
                    id = 2L,
                    text = "The deployment is complete!",
                    sender = PreviewMockData.createMockUser(uid = "u2", name = "Bob Johnson")
                )
            )
        )
    }

    override fun hasMoreConversations(): Boolean = false

    override fun hasMoreMessages(): Boolean = false
}
