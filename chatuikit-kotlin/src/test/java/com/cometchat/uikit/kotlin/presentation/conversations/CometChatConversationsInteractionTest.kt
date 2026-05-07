package com.cometchat.uikit.kotlin.presentation.conversations

import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.ConversationsRequest
import com.cometchat.chat.models.Conversation
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.domain.repository.ConversationListRepository
import com.cometchat.uikit.core.domain.usecase.DeleteConversationUseCase
import com.cometchat.uikit.core.domain.usecase.GetConversationListUseCase
import com.cometchat.uikit.core.domain.usecase.RefreshConversationListUseCase
import com.cometchat.uikit.core.state.DeleteState
import com.cometchat.uikit.core.viewmodel.CometChatConversationsViewModel
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
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
 * Tests for CometChatConversations Kotlin component interaction behavior.
 *
 * Verifies that ViewModel operations triggered by user interactions
 * (click, long-click, selection, delete) produce correct state changes.
 *
 * The Kotlin XML component calls these ViewModel methods in response to
 * RecyclerView item clicks and menu actions.
 *
 * Requirements: 14.5, 14.6
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CometChatConversationsInteractionTest : FunSpec({

    val testDispatcher = UnconfinedTestDispatcher()

    beforeTest {
        Dispatchers.setMain(testDispatcher)
        println("  🧪 ${it.name.testName}")
    }

    afterTest {
        Dispatchers.resetMain()
        println()
    }

    test("selectConversation in SINGLE mode → selectedConversations contains 1 item") {
        runTest {
            val conversations = createMockConversations(3)
            val viewModel = createViewModel(conversations)
            advanceUntilIdle()

            // Simulate item click in SINGLE selection mode
            viewModel.selectConversation(conversations[1], UIKitConstants.SelectionMode.SINGLE)

            viewModel.selectedConversations.value shouldHaveSize 1
            println("    ✅ SINGLE selection: 1 item selected after click")
        }
    }

    test("selectConversation in MULTIPLE mode → toggle behavior") {
        runTest {
            val conversations = createMockConversations(3)
            val viewModel = createViewModel(conversations)
            advanceUntilIdle()

            // Select first item
            viewModel.selectConversation(conversations[0], UIKitConstants.SelectionMode.MULTIPLE)
            viewModel.selectedConversations.value shouldHaveSize 1

            // Select second item
            viewModel.selectConversation(conversations[1], UIKitConstants.SelectionMode.MULTIPLE)
            viewModel.selectedConversations.value shouldHaveSize 2

            // Deselect first item (toggle)
            viewModel.selectConversation(conversations[0], UIKitConstants.SelectionMode.MULTIPLE)
            viewModel.selectedConversations.value shouldHaveSize 1

            println("    ✅ MULTIPLE selection: toggle works correctly")
        }
    }

    test("deleteConversation → removes from list and sets DeleteState.Success") {
        runTest {
            val conversations = createMockConversations(3)
            val viewModel = createViewModel(conversations)
            advanceUntilIdle()

            viewModel.conversations.value shouldHaveSize 3

            // Simulate delete action from long-click menu
            viewModel.deleteConversation(conversations[1])
            advanceUntilIdle()

            viewModel.conversations.value shouldHaveSize 2
            viewModel.deleteState.value shouldBe DeleteState.Success
            println("    ✅ Delete: removed from list, DeleteState.Success")
        }
    }

    test("clearSelection → empties selected set") {
        runTest {
            val conversations = createMockConversations(3)
            val viewModel = createViewModel(conversations)
            advanceUntilIdle()

            // Select some items
            viewModel.selectConversation(conversations[0], UIKitConstants.SelectionMode.MULTIPLE)
            viewModel.selectConversation(conversations[2], UIKitConstants.SelectionMode.MULTIPLE)
            viewModel.selectedConversations.value shouldHaveSize 2

            // Clear selection (discard button click)
            viewModel.clearSelection()
            viewModel.selectedConversations.value shouldHaveSize 0
            println("    ✅ clearSelection: all selections cleared")
        }
    }

    test("getItemAt returns correct conversation for RecyclerView position") {
        runTest {
            val conversations = createMockConversations(5)
            val viewModel = createViewModel(conversations)
            advanceUntilIdle()

            // Simulate RecyclerView adapter accessing items by position
            val item0 = viewModel.getItemAt(0)
            val item2 = viewModel.getItemAt(2)
            val item4 = viewModel.getItemAt(4)
            val outOfBounds = viewModel.getItemAt(10)

            item0?.conversationId shouldBe "conv-1"
            item2?.conversationId shouldBe "conv-3"
            item4?.conversationId shouldBe "conv-5"
            outOfBounds shouldBe null
            println("    ✅ getItemAt: correct items for positions 0, 2, 4; null for out-of-bounds")
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
