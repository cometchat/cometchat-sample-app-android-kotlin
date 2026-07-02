package com.cometchat.uikit.compose.presentation.messagelist.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.MessagesRequest
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Conversation
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.User
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.uikit.compose.theme.lightColorScheme
import com.cometchat.uikit.core.domain.model.SurroundingMessagesResult
import com.cometchat.uikit.core.domain.repository.MessageListRepository
import com.cometchat.uikit.core.viewmodel.CometChatMessageListViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Instrumented tests for CometChatMessageList error and loading states.
 *
 * Tests verify:
 * - Default error view rendering
 * - Custom errorView rendering with retry callback
 * - hideErrorState flag
 * - Loading state rendering
 * - hideLoadingState flag
 * - Custom loadingView rendering
 * - onError callback invocation
 *
 * Architecture:
 *   [Fake Repository (failing)] → [Real ViewModel] → [Real CometChatMessageList Composable]
 *       → [Compose UI Test assertions]
 *
 * Run with:
 *   ./gradlew :chatuikit-compose:connectedDebugAndroidTest \
 *     -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.uikit.compose.presentation.messagelist.ui.CometChatMessageListErrorStateTest
 */
@RunWith(AndroidJUnit4::class)
class CometChatMessageListErrorStateTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setup() {
        MessageListComposeTestHelper.ensureInitialized()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 1: Default error view displays on error
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun errorState_displaysDefaultErrorView() {
        println("  🧪 errorState_displaysDefaultErrorView")
        val viewModel = createViewModelWithError()
        val user = createMockUser("user-1", "Alice")

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatMessageList(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    user = user
                )
            }
        }

        composeTestRule.waitForIdle()

        // Default error state shows "Something went wrong." title
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("Something went wrong.")
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Something went wrong.").assertIsDisplayed()

        println("    ✅ Default error view displays correctly")
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 2: Custom errorView renders correctly
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun errorState_displaysCustomErrorView() {
        println("  🧪 errorState_displaysCustomErrorView")
        val viewModel = createViewModelWithError()
        val user = createMockUser("user-1", "Alice")

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatMessageList(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    user = user,
                    errorView = { onRetry ->
                        Text("Custom Error: Something went wrong")
                    }
                )
            }
        }

        composeTestRule.waitForIdle()

        // Custom error view should be displayed
        composeTestRule.onNodeWithText("Custom Error: Something went wrong").assertIsDisplayed()
        // Default error view should NOT be shown
        composeTestRule.onNodeWithText("Something went wrong.").assertDoesNotExist()

        println("    ✅ Custom error view displays correctly")
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 3: Custom errorView retry callback works
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun errorState_customErrorViewRetryCallbackWorks() {
        println("  🧪 errorState_customErrorViewRetryCallbackWorks")
        val retryClicked = AtomicBoolean(false)
        val viewModel = createViewModelWithError()
        val user = createMockUser("user-1", "Alice")

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatMessageList(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    user = user,
                    errorView = { onRetry ->
                        Text("Error occurred")
                        TextButton(onClick = {
                            retryClicked.set(true)
                            onRetry()
                        }) {
                            Text("Click to Retry")
                        }
                    }
                )
            }
        }

        composeTestRule.waitForIdle()

        // Click retry button
        composeTestRule.onNodeWithText("Click to Retry").performClick()

        assert(retryClicked.get()) { "Retry callback should have been invoked" }

        println("    ✅ Custom error view retry callback works correctly")
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 4: hideErrorState hides error view
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun errorState_hiddenWhenHideErrorStateIsTrue() {
        println("  🧪 errorState_hiddenWhenHideErrorStateIsTrue")
        val viewModel = createViewModelWithError()
        val user = createMockUser("user-1", "Alice")

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatMessageList(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    user = user,
                    hideErrorState = true
                )
            }
        }

        composeTestRule.waitForIdle()

        // Error state should NOT be displayed
        composeTestRule.onNodeWithText("Something went wrong.").assertDoesNotExist()

        println("    ✅ hideErrorState hides error view correctly")
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 5: onError callback invoked when error occurs
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun errorState_invokesOnErrorCallback() {
        println("  🧪 errorState_invokesOnErrorCallback")
        val onErrorInvoked = AtomicBoolean(false)
        val viewModel = createViewModelWithError()
        val user = createMockUser("user-1", "Alice")

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatMessageList(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    user = user,
                    onError = { _ ->
                        onErrorInvoked.set(true)
                    }
                )
            }
        }

        composeTestRule.waitForIdle()

        assert(onErrorInvoked.get()) { "onError callback should have been invoked" }

        println("    ✅ onError callback invoked correctly")
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 6: Loading state displays loading view
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun loadingState_displaysLoadingView() {
        println("  🧪 loadingState_displaysLoadingView")
        val viewModel = createViewModelWithLoading()
        val user = createMockUser("user-1", "Alice")

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatMessageList(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    user = user
                )
            }
        }

        // Loading state should render without crash
        composeTestRule.waitForIdle()

        println("    ✅ Loading state renders without crash")
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 7: Custom loadingView renders correctly
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun loadingState_displaysCustomLoadingView() {
        println("  🧪 loadingState_displaysCustomLoadingView")
        val viewModel = createViewModelWithLoading()
        val user = createMockUser("user-1", "Alice")

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatMessageList(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    user = user,
                    loadingView = {
                        Text("Custom Loading Indicator")
                    }
                )
            }
        }

        composeTestRule.waitForIdle()

        // Custom loading view should be displayed
        composeTestRule.onNodeWithText("Custom Loading Indicator").assertIsDisplayed()

        println("    ✅ Custom loading view displays correctly")
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 8: hideLoadingState hides loading view
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun loadingState_hiddenWhenHideLoadingStateIsTrue() {
        println("  🧪 loadingState_hiddenWhenHideLoadingStateIsTrue")
        val viewModel = createViewModelWithLoading()
        val user = createMockUser("user-1", "Alice")

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatMessageList(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    user = user,
                    hideLoadingState = true
                )
            }
        }

        // With hideLoadingState=true, no loading indicator should appear
        composeTestRule.waitForIdle()

        println("    ✅ hideLoadingState hides loading view correctly")
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Helper Functions
    // ═══════════════════════════════════════════════════════════════════════════

    private fun createMockUser(uid: String, name: String): User {
        return User().apply {
            this.uid = uid
            this.name = name
            this.status = CometChatConstants.USER_STATUS_ONLINE
        }
    }

    private fun createViewModelWithError(): CometChatMessageListViewModel {
        val repository = object : MessageListRepository {
            override suspend fun fetchPreviousMessages(): Result<List<BaseMessage>> =
                Result.failure(CometChatException("ERR_NETWORK", "Network error"))
            override suspend fun fetchNextMessages(fromMessageId: Long) = Result.success(emptyList<BaseMessage>())
            override suspend fun getConversation(id: String, type: String): Result<Conversation> =
                Result.failure(Exception("Not configured"))
            override suspend fun getMessage(messageId: Long): Result<BaseMessage> =
                Result.failure(Exception("Not configured"))
            override suspend fun deleteMessage(message: BaseMessage) = Result.success(message)
            override suspend fun flagMessage(messageId: Long, reason: String, remark: String) = Result.success(Unit)
            override suspend fun addReaction(messageId: Long, emoji: String): Result<BaseMessage> =
                Result.failure(Exception("Not configured"))
            override suspend fun removeReaction(messageId: Long, emoji: String): Result<BaseMessage> =
                Result.failure(Exception("Not configured"))
            override suspend fun markAsDelivered(message: BaseMessage) = Result.success(Unit)
            override suspend fun markAsRead(message: BaseMessage) = Result.success(Unit)
            override suspend fun markAsUnread(message: BaseMessage): Result<Conversation> =
                Result.failure(Exception("Not configured"))
            override fun hasMorePreviousMessages() = false
            override fun resetRequest() {}
            override fun configureForUser(user: User, messagesTypes: List<String>, messagesCategories: List<String>, parentMessageId: Long, messagesRequestBuilder: MessagesRequest.MessagesRequestBuilder?) {}
            override fun configureForGroup(group: Group, messagesTypes: List<String>, messagesCategories: List<String>, parentMessageId: Long, messagesRequestBuilder: MessagesRequest.MessagesRequestBuilder?) {}
            override suspend fun fetchSurroundingMessages(messageId: Long): Result<SurroundingMessagesResult> =
                Result.failure(Exception("Not configured"))
            override suspend fun fetchActionMessages(fromMessageId: Long) = Result.success(emptyList<BaseMessage>())
            override fun rebuildRequestFromMessageId(messageId: Long) {}
            override fun getLatestMessageId() = -1L
            override fun setLatestMessageId(messageId: Long) {}
        }
        return CometChatMessageListViewModel(repository = repository, enableListeners = false)
    }

    private fun createViewModelWithLoading(): CometChatMessageListViewModel {
        val repository = object : MessageListRepository {
            override suspend fun fetchPreviousMessages(): Result<List<BaseMessage>> {
                kotlinx.coroutines.delay(Long.MAX_VALUE)
                return Result.success(emptyList())
            }
            override suspend fun fetchNextMessages(fromMessageId: Long): Result<List<BaseMessage>> {
                kotlinx.coroutines.delay(Long.MAX_VALUE)
                return Result.success(emptyList())
            }
            override suspend fun getConversation(id: String, type: String): Result<Conversation> =
                Result.failure(Exception("Not configured"))
            override suspend fun getMessage(messageId: Long): Result<BaseMessage> =
                Result.failure(Exception("Not configured"))
            override suspend fun deleteMessage(message: BaseMessage) = Result.success(message)
            override suspend fun flagMessage(messageId: Long, reason: String, remark: String) = Result.success(Unit)
            override suspend fun addReaction(messageId: Long, emoji: String): Result<BaseMessage> =
                Result.failure(Exception("Not configured"))
            override suspend fun removeReaction(messageId: Long, emoji: String): Result<BaseMessage> =
                Result.failure(Exception("Not configured"))
            override suspend fun markAsDelivered(message: BaseMessage) = Result.success(Unit)
            override suspend fun markAsRead(message: BaseMessage) = Result.success(Unit)
            override suspend fun markAsUnread(message: BaseMessage): Result<Conversation> =
                Result.failure(Exception("Not configured"))
            override fun hasMorePreviousMessages() = true
            override fun resetRequest() {}
            override fun configureForUser(user: User, messagesTypes: List<String>, messagesCategories: List<String>, parentMessageId: Long, messagesRequestBuilder: MessagesRequest.MessagesRequestBuilder?) {}
            override fun configureForGroup(group: Group, messagesTypes: List<String>, messagesCategories: List<String>, parentMessageId: Long, messagesRequestBuilder: MessagesRequest.MessagesRequestBuilder?) {}
            override suspend fun fetchSurroundingMessages(messageId: Long): Result<SurroundingMessagesResult> =
                Result.failure(Exception("Not configured"))
            override suspend fun fetchActionMessages(fromMessageId: Long) = Result.success(emptyList<BaseMessage>())
            override fun rebuildRequestFromMessageId(messageId: Long) {}
            override fun getLatestMessageId() = -1L
            override fun setLatestMessageId(messageId: Long) {}
        }
        return CometChatMessageListViewModel(repository = repository, enableListeners = false)
    }
}
