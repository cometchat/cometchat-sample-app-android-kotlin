package com.cometchat.uikit.compose.conversationlist.presentation.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cometchat.chat.core.ConversationsRequest
import com.cometchat.chat.models.Conversation
import com.cometchat.uikit.compose.presentation.conversations.ui.CometChatConversations
import com.cometchat.uikit.core.viewmodel.CometChatConversationsViewModel
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.uikit.compose.theme.lightColorScheme
import com.cometchat.uikit.core.domain.repository.ConversationListRepository
import com.cometchat.uikit.core.domain.usecase.DeleteConversationUseCase
import com.cometchat.uikit.core.domain.usecase.GetConversationListUseCase
import com.cometchat.uikit.core.domain.usecase.RefreshConversationListUseCase
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Instrumented UI tests for CometChatConversations.
 * 
 * Feature: conversations-compose
 * Tests verify component rendering, interactions, and state transitions.
 */
@RunWith(AndroidJUnit4::class)
class CometChatConversationListTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun conversationList_displaysToolbarWithTitle() {
        // Arrange
        val viewModel = createViewModelWithConversations(emptyList())

        // Act
        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatConversations(
                    modifier = Modifier.fillMaxSize(),
                    conversationListViewModel = viewModel,
                    title = "My Chats"
                )
            }
        }

        // Assert
        composeTestRule.onNodeWithText("My Chats").assertIsDisplayed()
    }

    @Test
    fun conversationList_hidesToolbarWhenConfigured() {
        // Arrange
        val viewModel = createViewModelWithConversations(emptyList())

        // Act
        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatConversations(
                    modifier = Modifier.fillMaxSize(),
                    conversationListViewModel = viewModel,
                    title = "Chats",
                    hideToolbar = true
                )
            }
        }

        // Assert - Title should not be displayed when toolbar is hidden
        composeTestRule.onNodeWithText("Chats").assertDoesNotExist()
    }

    @Test
    fun conversationList_displaysSearchBoxByDefault() {
        // Arrange
        val viewModel = createViewModelWithConversations(emptyList())

        // Act
        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatConversations(
                    modifier = Modifier.fillMaxSize(),
                    conversationListViewModel = viewModel,
                    searchPlaceholderText = "Search conversations"
                )
            }
        }

        // Assert
        composeTestRule.onNodeWithText("Search conversations").assertIsDisplayed()
    }

    @Test
    fun conversationList_hidesSearchBoxWhenConfigured() {
        // Arrange
        val viewModel = createViewModelWithConversations(emptyList())

        // Act
        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatConversations(
                    modifier = Modifier.fillMaxSize(),
                    conversationListViewModel = viewModel,
                    hideSearchBox = true,
                    searchPlaceholderText = "Search"
                )
            }
        }

        // Assert - Search placeholder should not be displayed
        composeTestRule.onNodeWithText("Search").assertDoesNotExist()
    }

    @Test
    fun conversationList_displaysEmptyStateWhenNoConversations() {
        // Arrange
        val viewModel = createViewModelWithConversations(emptyList())

        // Act
        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatConversations(
                    modifier = Modifier.fillMaxSize(),
                    conversationListViewModel = viewModel
                )
            }
        }

        // Wait for state to settle
        composeTestRule.waitForIdle()

        // Assert - Empty state text should be displayed
        composeTestRule.onNodeWithText("No Conversations").assertIsDisplayed()
    }

    @Test
    fun conversationList_invokesOnSearchClickCallback() {
        // Arrange
        val searchClicked = AtomicBoolean(false)
        val viewModel = createViewModelWithConversations(emptyList())

        // Act
        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatConversations(
                    modifier = Modifier.fillMaxSize(),
                    conversationListViewModel = viewModel,
                    searchPlaceholderText = "Search",
                    onSearchClick = { searchClicked.set(true) }
                )
            }
        }

        // Click on search box
        composeTestRule.onNodeWithText("Search").performClick()

        // Assert
        assert(searchClicked.get()) { "onSearchClick callback should have been invoked" }
    }

    @Test
    fun conversationList_displaysCustomEmptyView() {
        // Arrange
        val viewModel = createViewModelWithConversations(emptyList())

        // Act
        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatConversations(
                    modifier = Modifier.fillMaxSize(),
                    conversationListViewModel = viewModel,
                    emptyView = {
                        Text("Custom Empty State")
                    }
                )
            }
        }

        // Wait for state to settle
        composeTestRule.waitForIdle()

        // Assert - Custom empty view should be displayed
        composeTestRule.onNodeWithText("Custom Empty State").assertIsDisplayed()
    }

    @Test
    fun conversationList_hidesEmptyStateWhenConfigured() {
        // Arrange
        val viewModel = createViewModelWithConversations(emptyList())

        // Act
        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatConversations(
                    modifier = Modifier.fillMaxSize(),
                    conversationListViewModel = viewModel,
                    hideEmptyState = true
                )
            }
        }

        // Wait for state to settle
        composeTestRule.waitForIdle()

        // Assert - Empty state should not be displayed
        composeTestRule.onNodeWithText("No Conversations").assertDoesNotExist()
    }

    @Test
    fun conversationList_invokesOnEmptyCallback() {
        // Arrange
        val emptyCallbackInvoked = AtomicBoolean(false)
        val viewModel = createViewModelWithConversations(emptyList())

        // Act
        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatConversations(
                    modifier = Modifier.fillMaxSize(),
                    conversationListViewModel = viewModel,
                    onEmpty = { emptyCallbackInvoked.set(true) }
                )
            }
        }

        // Wait for state to settle
        composeTestRule.waitForIdle()

        // Assert
        assert(emptyCallbackInvoked.get()) { "onEmpty callback should have been invoked" }
    }
}

/**
 * Helper function to create a ViewModel with predefined conversations.
 */
private fun createViewModelWithConversations(conversations: List<Conversation>): CometChatConversationsViewModel {
    val repository = object : ConversationListRepository {
        override suspend fun getConversations(request: ConversationsRequest): Result<List<Conversation>> {
            return Result.success(conversations)
        }
        override suspend fun deleteConversation(conversationWith: String, conversationType: String): Result<Unit> {
            return Result.success(Unit)
        }
        override suspend fun markAsDelivered(conversation: Conversation): Result<Unit> {
            return Result.success(Unit)
        }
        override fun hasMoreConversations(): Boolean = false
    }

    val getConversationListUseCase = GetConversationListUseCase(repository)
    val deleteConversationUseCase = DeleteConversationUseCase(repository)
    val refreshConversationListUseCase = RefreshConversationListUseCase(repository)

    return CometChatConversationsViewModel(
        getConversationListUseCase = getConversationListUseCase,
        deleteConversationUseCase = deleteConversationUseCase,
        refreshConversationListUseCase = refreshConversationListUseCase
    )
}
