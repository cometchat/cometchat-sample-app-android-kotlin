package com.cometchat.uikit.compose.presentation.conversations.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.ConversationsRequest
import com.cometchat.chat.models.Conversation
import com.cometchat.chat.models.User
import com.cometchat.uikit.compose.presentation.conversations.ui.CometChatConversations
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.uikit.compose.theme.lightColorScheme
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.domain.repository.ConversationListRepository
import com.cometchat.uikit.core.domain.usecase.DeleteConversationUseCase
import com.cometchat.uikit.core.domain.usecase.GetConversationListUseCase
import com.cometchat.uikit.core.domain.usecase.RefreshConversationListUseCase
import com.cometchat.uikit.core.viewmodel.CometChatConversationsViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


/**
 * Instrumented tests for CometChatConversations selection mode behavior.
 *
 * Tests verify:
 * - SINGLE selection mode shows selection count in toolbar
 * - Discard selection icon clears selections and returns to normal toolbar
 *
 * Requirements: 8.4, 8.5
 */
@RunWith(AndroidJUnit4::class)
class CometChatConversationsSelectionModeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun singleSelectionMode_clickingItem_displaysSelectionCountInToolbar() {
        val conversations = createMockConversations(3)
        val viewModel = createViewModelWithConversations(conversations)

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatConversations(
                    modifier = Modifier.fillMaxSize(),
                    conversationListViewModel = viewModel,
                    selectionMode = UIKitConstants.SelectionMode.SINGLE
                )
            }
        }

        composeTestRule.waitForIdle()

        // Click on the first conversation item (by user name)
        composeTestRule.onNodeWithText("User 1").performClick()
        composeTestRule.waitForIdle()

        // Selection toolbar should show count "1"
        composeTestRule.onNodeWithText("1").assertIsDisplayed()
        // Discard selection icon should be visible
        composeTestRule.onNodeWithContentDescription("Discard selection").assertIsDisplayed()
    }

    @Test
    fun selectionMode_discardSelection_clearsAndReturnsToNormalToolbar() {
        val conversations = createMockConversations(3)
        val viewModel = createViewModelWithConversations(conversations)

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatConversations(
                    modifier = Modifier.fillMaxSize(),
                    conversationListViewModel = viewModel,
                    selectionMode = UIKitConstants.SelectionMode.SINGLE,
                    title = "Chats"
                )
            }
        }

        composeTestRule.waitForIdle()

        // Select a conversation
        composeTestRule.onNodeWithText("User 1").performClick()
        composeTestRule.waitForIdle()

        // Verify selection mode is active
        composeTestRule.onNodeWithContentDescription("Discard selection").assertIsDisplayed()

        // Click discard selection
        composeTestRule.onNodeWithContentDescription("Discard selection").performClick()
        composeTestRule.waitForIdle()

        // Normal toolbar should be back with the title
        composeTestRule.onNodeWithText("Chats").assertIsDisplayed()
    }
}

// ==================== Helper Functions ====================

private fun createMockConversations(count: Int): List<Conversation> {
    return (1..count).map { i ->
        val user = User().apply {
            uid = "user-$i"
            name = "User $i"
            status = "online"
            avatar = null
        }

        Conversation("conv-$i", CometChatConstants.RECEIVER_TYPE_USER).apply {
            conversationWith = user
            unreadMessageCount = 0
            lastMessage = null
        }
    }
}

private fun createViewModelWithConversations(
    conversations: List<Conversation>
): CometChatConversationsViewModel {
    val repository = object : ConversationListRepository {
        private var fetched = false
        override suspend fun getConversations(request: ConversationsRequest): Result<List<Conversation>> {
            if (fetched) return Result.success(emptyList())
            fetched = true
            return Result.success(conversations)
        }
        override suspend fun deleteConversation(conversationWith: String, conversationType: String) =
            Result.success(Unit)
        override suspend fun markAsDelivered(conversation: Conversation) =
            Result.success(Unit)
        override fun hasMoreConversations() = false
    }
    return CometChatConversationsViewModel(
        getConversationListUseCase = GetConversationListUseCase(repository),
        deleteConversationUseCase = DeleteConversationUseCase(repository),
        refreshConversationListUseCase = RefreshConversationListUseCase(repository)
    )
}
