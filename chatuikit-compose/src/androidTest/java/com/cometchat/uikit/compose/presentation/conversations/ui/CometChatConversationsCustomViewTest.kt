package com.cometchat.uikit.compose.presentation.conversations.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cometchat.chat.core.ConversationsRequest
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.Conversation
import com.cometchat.uikit.compose.presentation.conversations.ui.CometChatConversations
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.uikit.compose.theme.lightColorScheme
import com.cometchat.uikit.core.domain.repository.ConversationListRepository
import com.cometchat.uikit.core.domain.usecase.DeleteConversationUseCase
import com.cometchat.uikit.core.domain.usecase.GetConversationListUseCase
import com.cometchat.uikit.core.domain.usecase.RefreshConversationListUseCase
import com.cometchat.uikit.core.viewmodel.CometChatConversationsViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Instrumented tests for CometChatConversations custom view slot rendering.
 *
 * Tests verify:
 * - Custom loadingView displayed when UIState is Loading
 * - Custom errorView displayed with working retry callback when UIState is Error
 * - Custom overflowMenu composable displayed in toolbar
 * - onLoad callback invoked when UIState transitions to Content
 * - onError callback invoked when UIState transitions to Error
 *
 * Requirements: 9.1, 9.2, 9.3, 9.4, 9.5
 */
@RunWith(AndroidJUnit4::class)
class CometChatConversationsCustomViewTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // ==================== Custom Loading View ====================

    @Test
    fun customLoadingView_displayedWhenLoading() {
        val viewModel = createViewModelWithLoading()

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatConversations(
                    modifier = Modifier.fillMaxSize(),
                    conversationListViewModel = viewModel,
                    loadingView = {
                        Text("My Custom Loader")
                    }
                )
            }
        }

        composeTestRule.onNodeWithText("My Custom Loader").assertIsDisplayed()
    }

    // ==================== Custom Error View ====================

    @Test
    fun customErrorView_displayedWithRetryCallback() {
        val retryInvoked = AtomicBoolean(false)
        val viewModel = createViewModelWithError()

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatConversations(
                    modifier = Modifier.fillMaxSize(),
                    conversationListViewModel = viewModel,
                    errorView = { onRetry ->
                        Text("Custom Error")
                        TextButton(onClick = {
                            retryInvoked.set(true)
                            onRetry()
                        }) {
                            Text("Tap to Retry")
                        }
                    }
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Custom Error").assertIsDisplayed()
        // Default error should NOT be shown
        composeTestRule.onNodeWithText("Oops!").assertDoesNotExist()

        // Click retry
        composeTestRule.onNodeWithText("Tap to Retry").performClick()
        assert(retryInvoked.get()) { "Retry callback should have been invoked" }
    }

    // ==================== Custom Overflow Menu ====================

    @Test
    fun customOverflowMenu_displayedInToolbar() {
        val viewModel = createViewModelWithConversations(emptyList())

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatConversations(
                    modifier = Modifier.fillMaxSize(),
                    conversationListViewModel = viewModel,
                    overflowMenu = {
                        Text("Custom Menu")
                    }
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Custom Menu").assertIsDisplayed()
    }

    // ==================== onLoad Callback ====================

    @Test
    fun onLoadCallback_invokedWhenContentLoaded() {
        val onLoadInvoked = AtomicBoolean(false)
        val viewModel = createViewModelWithConversations(
            createSimpleConversations(3)
        )

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatConversations(
                    modifier = Modifier.fillMaxSize(),
                    conversationListViewModel = viewModel,
                    onLoad = { conversations ->
                        onLoadInvoked.set(true)
                    }
                )
            }
        }

        composeTestRule.waitForIdle()
        assert(onLoadInvoked.get()) { "onLoad callback should have been invoked with conversations" }
    }

    // ==================== onError Callback ====================

    @Test
    fun onErrorCallback_invokedWhenErrorOccurs() {
        val onErrorInvoked = AtomicBoolean(false)
        val viewModel = createViewModelWithError()

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatConversations(
                    modifier = Modifier.fillMaxSize(),
                    conversationListViewModel = viewModel,
                    onError = { exception ->
                        onErrorInvoked.set(true)
                    }
                )
            }
        }

        composeTestRule.waitForIdle()
        assert(onErrorInvoked.get()) { "onError callback should have been invoked with CometChatException" }
    }
}

// ==================== Helper Functions ====================

private fun createSimpleConversations(count: Int): List<Conversation> {
    return (1..count).map { i ->
        val user = com.cometchat.chat.models.User().apply {
            uid = "user-$i"
            name = "User $i"
            status = "online"
            avatar = null
        }

        Conversation("conv-$i", com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER).apply {
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

private fun createViewModelWithError(): CometChatConversationsViewModel {
    val repository = object : ConversationListRepository {
        override suspend fun getConversations(request: ConversationsRequest) =
            Result.failure<List<Conversation>>(CometChatException("ERR", "Test error"))
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

private fun createViewModelWithLoading(): CometChatConversationsViewModel {
    val repository = object : ConversationListRepository {
        override suspend fun getConversations(request: ConversationsRequest): Result<List<Conversation>> {
            kotlinx.coroutines.delay(Long.MAX_VALUE)
            return Result.success(emptyList())
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
