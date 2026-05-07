package com.cometchat.uikit.compose.presentation.conversations

import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.ConversationsRequest
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.Conversation
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.domain.repository.ConversationListRepository
import com.cometchat.uikit.core.domain.usecase.DeleteConversationUseCase
import com.cometchat.uikit.core.domain.usecase.GetConversationListUseCase
import com.cometchat.uikit.core.domain.usecase.RefreshConversationListUseCase
import com.cometchat.uikit.core.state.UIState
import com.cometchat.uikit.core.viewmodel.CometChatConversationsViewModel
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * Tests for CometChatConversations Compose component rendering states.
 *
 * Verifies that the ViewModel produces the correct UIState for each scenario,
 * which the Compose component observes to show/hide composables.
 *
 * These tests validate the ViewModel state that drives rendering:
 * - UIState.Loading → component shows shimmer
 * - UIState.Empty → component shows empty state composable
 * - UIState.Error → component shows error state composable
 * - UIState.Content → component shows LazyColumn with items
 *
 * Mirrors: chatuikit-kotlin CometChatConversationsRenderingTest
 *
 * Requirements: 14.1, 14.2, 14.3, 14.4
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CometChatConversationsRenderingTest : FunSpec({

    val testDispatcher = UnconfinedTestDispatcher()

    beforeTest {
        Dispatchers.setMain(testDispatcher)
    }

    afterTest {
        Dispatchers.resetMain()
    }

    test("ViewModel with empty repository → UIState.Empty (drives empty state composable)") {
        runTest {
            val viewModel = createViewModel(emptyList())
            advanceUntilIdle()

            viewModel.uiState.value shouldBe UIState.Empty
            viewModel.conversations.value shouldHaveSize 0
        }
    }

    test("ViewModel with error repository → UIState.Error (drives error state composable)") {
        runTest {
            val viewModel = createViewModelWithError("LOAD_ERR", "Failed to load")
            advanceUntilIdle()

            viewModel.uiState.value.shouldBeInstanceOf<UIState.Error>()
        }
    }

    test("ViewModel with conversations → UIState.Content with correct item count") {
        runTest {
            val conversations = createMockConversations(5)
            val viewModel = createViewModel(conversations)
            advanceUntilIdle()

            viewModel.uiState.value.shouldBeInstanceOf<UIState.Content>()
            viewModel.conversations.value shouldHaveSize 5
        }
    }

    test("ViewModel conversations contain correct user data for rendering") {
        runTest {
            val conversations = createMockConversations(3)
            val viewModel = createViewModel(conversations)
            advanceUntilIdle()

            val items = viewModel.conversations.value
            items shouldHaveSize 3

            items.forEachIndexed { index, conv ->
                conv.conversationId shouldBe "conv-${index + 1}"
                conv.conversationType shouldBe CometChatConstants.RECEIVER_TYPE_USER
                val user = conv.conversationWith as User
                user.name shouldBe "User ${index + 1}"
                user.uid shouldBe "user-${index + 1}"
            }
        }
    }
})

// ==================== Helper Functions ====================

private fun createMockConversations(count: Int): List<Conversation> {
    return (1..count).map { i ->
        val user = mock<User>()
        whenever(user.uid).thenReturn("user-$i")
        whenever(user.name).thenReturn("User $i")
        whenever(user.status).thenReturn("online")
        whenever(user.avatar).thenReturn(null)

        val conversation = mock<Conversation>()
        whenever(conversation.conversationId).thenReturn("conv-$i")
        whenever(conversation.conversationType).thenReturn(CometChatConstants.RECEIVER_TYPE_USER)
        whenever(conversation.conversationWith).thenReturn(user)
        whenever(conversation.unreadMessageCount).thenReturn(0)
        whenever(conversation.lastMessage).thenReturn(null)
        conversation
    }
}

private fun createViewModel(conversations: List<Conversation>): CometChatConversationsViewModel {
    val repository = object : ConversationListRepository {
        override suspend fun getConversations(request: ConversationsRequest) =
            Result.success(conversations)
        override suspend fun deleteConversation(conversationWith: String, conversationType: String) =
            Result.success(Unit)
        override suspend fun markAsDelivered(conversation: Conversation) =
            Result.success(Unit)
        override fun hasMoreConversations() = false
    }
    return CometChatConversationsViewModel(
        getConversationListUseCase = GetConversationListUseCase(repository),
        deleteConversationUseCase = DeleteConversationUseCase(repository),
        refreshConversationListUseCase = RefreshConversationListUseCase(repository),
        enableListeners = false
    )
}

private fun createViewModelWithError(code: String, message: String): CometChatConversationsViewModel {
    val repository = object : ConversationListRepository {
        override suspend fun getConversations(request: ConversationsRequest) =
            Result.failure<List<Conversation>>(CometChatException(code, message))
        override suspend fun deleteConversation(conversationWith: String, conversationType: String) =
            Result.success(Unit)
        override suspend fun markAsDelivered(conversation: Conversation) =
            Result.success(Unit)
        override fun hasMoreConversations() = false
    }
    return CometChatConversationsViewModel(
        getConversationListUseCase = GetConversationListUseCase(repository),
        deleteConversationUseCase = DeleteConversationUseCase(repository),
        refreshConversationListUseCase = RefreshConversationListUseCase(repository),
        enableListeners = false
    )
}
