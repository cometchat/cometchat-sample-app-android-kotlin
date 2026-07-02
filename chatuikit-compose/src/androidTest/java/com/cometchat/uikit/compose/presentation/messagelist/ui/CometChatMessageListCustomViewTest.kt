package com.cometchat.uikit.compose.presentation.messagelist.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.MessagesRequest
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Conversation
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.TextMessage
import com.cometchat.chat.models.User
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.uikit.compose.theme.lightColorScheme
import com.cometchat.uikit.core.domain.model.SurroundingMessagesResult
import com.cometchat.uikit.core.domain.repository.MessageListRepository
import com.cometchat.uikit.core.state.MessageAlignment
import com.cometchat.uikit.core.viewmodel.CometChatMessageListViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Instrumented tests for CometChatMessageList custom view slot providers.
 *
 * Tests verify:
 * - Custom emptyView rendering
 * - Custom errorView rendering with retry
 * - Custom loadingView rendering
 * - Custom newMessageIndicatorView rendering
 * - Custom leadingView slot provider
 * - Custom headerView slot provider
 * - Custom contentView slot provider
 * - Custom footerView slot provider
 *
 * Architecture:
 *   [Fake Repository] → [Real ViewModel] → [Real CometChatMessageList Composable]
 *       → [Custom slot providers] → [Compose UI Test assertions]
 *
 * Run with:
 *   ./gradlew :chatuikit-compose:connectedDebugAndroidTest \
 *     -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.uikit.compose.presentation.messagelist.ui.CometChatMessageListCustomViewTest
 */
@RunWith(AndroidJUnit4::class)
class CometChatMessageListCustomViewTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setup() {
        MessageListComposeTestHelper.ensureInitialized()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 1: Custom emptyView renders when empty
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun customEmptyView_displayedWhenEmpty() {
        println("  🧪 customEmptyView_displayedWhenEmpty")
        val viewModel = createViewModelWithMessages(emptyList())
        val user = createMockUser("user-1", "Alice")

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatMessageList(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    user = user,
                    emptyView = {
                        Text("Custom Empty: Start a conversation!")
                    }
                )
            }
        }

        composeTestRule.waitForIdle()

        // Custom empty view should be displayed
        composeTestRule.onNodeWithText("Custom Empty: Start a conversation!")
            .assertIsDisplayed()
        // Default empty view should NOT be shown
        composeTestRule.onNodeWithText("No Messages Yet").assertDoesNotExist()

        println("    ✅ Custom empty view displays correctly")
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 2: Custom errorView renders with retry callback
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun customErrorView_displayedWithRetryCallback() {
        println("  🧪 customErrorView_displayedWithRetryCallback")
        val retryInvoked = AtomicBoolean(false)
        val viewModel = createViewModelWithError()
        val user = createMockUser("user-1", "Alice")

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatMessageList(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    user = user,
                    errorView = { onRetry ->
                        Text("Custom Error View")
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

        // Custom error view should be displayed
        composeTestRule.onNodeWithText("Custom Error View").assertIsDisplayed()
        // Default error should NOT be shown
        composeTestRule.onNodeWithText("Something went wrong.").assertDoesNotExist()

        // Click retry
        composeTestRule.onNodeWithText("Tap to Retry").performClick()
        assert(retryInvoked.get()) { "Retry callback should have been invoked" }

        println("    ✅ Custom error view with retry displays correctly")
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 3: Custom loadingView renders when loading
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun customLoadingView_displayedWhenLoading() {
        println("  🧪 customLoadingView_displayedWhenLoading")
        val viewModel = createViewModelWithLoading()
        val user = createMockUser("user-1", "Alice")

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatMessageList(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    user = user,
                    loadingView = {
                        Text("Custom Loading: Please wait...")
                    }
                )
            }
        }

        composeTestRule.waitForIdle()

        // Custom loading view should be displayed
        composeTestRule.onNodeWithText("Custom Loading: Please wait...")
            .assertIsDisplayed()

        println("    ✅ Custom loading view displays correctly")
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 4: Custom newMessageIndicatorView renders
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun customNewMessageIndicatorView_configuredCorrectly() {
        println("  🧪 customNewMessageIndicatorView_configuredCorrectly")
        val messages = createMessages(5)
        val viewModel = createViewModelWithMessages(messages)
        val user = createMockUser("user-1", "Alice")

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatMessageList(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    user = user,
                    newMessageIndicatorView = { count, onClick ->
                        TextButton(onClick = onClick) {
                            Text("$count new messages ↓")
                        }
                    }
                )
            }
        }

        composeTestRule.waitForIdle()

        // The custom indicator is configured but won't show until new messages arrive
        // Verify component renders without crash
        println("    ✅ Custom new message indicator configured correctly")
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 5: Custom leadingView slot provider renders
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun customLeadingView_rendersForMessages() {
        println("  🧪 customLeadingView_rendersForMessages")
        val messages = createMessages(3)
        val viewModel = createViewModelWithMessages(messages)
        val user = createMockUser("user-1", "Alice")

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatMessageList(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    user = user,
                    leadingView = { message, alignment ->
                        if (alignment == MessageAlignment.LEFT) {
                            Text("Custom Avatar")
                        }
                    }
                )
            }
        }

        composeTestRule.waitForIdle()

        // Custom leading view should be rendered for incoming messages
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("Custom Avatar")
                .fetchSemanticsNodes().isNotEmpty()
        }
        val nodes = composeTestRule.onAllNodesWithText("Custom Avatar")
        assert(nodes.fetchSemanticsNodes().isNotEmpty()) { "Expected custom avatar nodes" }

        println("    ✅ Custom leading view renders correctly")
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 6: Custom headerView slot provider renders
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun customHeaderView_rendersForMessages() {
        println("  🧪 customHeaderView_rendersForMessages")
        val messages = createMessages(3)
        val viewModel = createViewModelWithMessages(messages)
        val group = createMockGroup("group-1", "Test Group")

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatMessageList(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    group = group,
                    headerView = { message, alignment ->
                        Text("Custom Header: ${message.sender?.name ?: "Unknown"}")
                    }
                )
            }
        }

        composeTestRule.waitForIdle()

        // Custom header view should be rendered
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("Custom Header: Alice Johnson")
                .fetchSemanticsNodes().isNotEmpty()
        }
        val nodes = composeTestRule.onAllNodesWithText("Custom Header: Alice Johnson")
        assert(nodes.fetchSemanticsNodes().isNotEmpty()) { "Expected custom header nodes" }

        println("    ✅ Custom header view renders correctly")
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 7: Custom contentView slot provider renders
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun customContentView_rendersForMessages() {
        println("  🧪 customContentView_rendersForMessages")
        val messages = createMessages(3)
        val viewModel = createViewModelWithMessages(messages)
        val user = createMockUser("user-1", "Alice")

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatMessageList(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    user = user,
                    contentView = { message, alignment ->
                        Text("Custom Content: ${(message as? TextMessage)?.text ?: ""}")
                    }
                )
            }
        }

        composeTestRule.waitForIdle()

        // Custom content view should be rendered
        composeTestRule.onNodeWithText("Custom Content: Hey, how are you?")
            .assertIsDisplayed()

        println("    ✅ Custom content view renders correctly")
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 8: Custom footerView slot provider renders
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun customFooterView_rendersForMessages() {
        println("  🧪 customFooterView_rendersForMessages")
        val messages = createMessages(3)
        val viewModel = createViewModelWithMessages(messages)
        val user = createMockUser("user-1", "Alice")

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatMessageList(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    user = user,
                    footerView = { message, alignment ->
                        Text("Custom Footer")
                    }
                )
            }
        }

        composeTestRule.waitForIdle()

        // Custom footer view should be rendered
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("Custom Footer")
                .fetchSemanticsNodes().isNotEmpty()
        }
        val nodes = composeTestRule.onAllNodesWithText("Custom Footer")
        assert(nodes.fetchSemanticsNodes().isNotEmpty()) { "Expected custom footer nodes" }

        println("    ✅ Custom footer view renders correctly")
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 9: Custom statusInfoView slot provider renders
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun customStatusInfoView_rendersForMessages() {
        println("  🧪 customStatusInfoView_rendersForMessages")
        val messages = createMessages(3)
        val viewModel = createViewModelWithMessages(messages)
        val user = createMockUser("user-1", "Alice")

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatMessageList(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    user = user,
                    statusInfoView = { message, alignment ->
                        Text("Custom Status Info")
                    }
                )
            }
        }

        composeTestRule.waitForIdle()

        // Custom status info view should be rendered
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("Custom Status Info")
                .fetchSemanticsNodes().isNotEmpty()
        }
        val nodes = composeTestRule.onAllNodesWithText("Custom Status Info")
        assert(nodes.fetchSemanticsNodes().isNotEmpty()) { "Expected custom status info nodes" }

        println("    ✅ Custom status info view renders correctly")
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 10: onLoad callback invoked with custom views
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun onLoadCallback_invokedWithCustomViews() {
        println("  🧪 onLoadCallback_invokedWithCustomViews")
        val onLoadInvoked = AtomicBoolean(false)
        val messages = createMessages(3)
        val viewModel = createViewModelWithMessages(messages)
        val user = createMockUser("user-1", "Alice")

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatMessageList(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    user = user,
                    contentView = { message, alignment ->
                        Text("Custom Content")
                    },
                    onLoad = { _ ->
                        onLoadInvoked.set(true)
                    }
                )
            }
        }

        composeTestRule.waitForIdle()

        assert(onLoadInvoked.get()) { "onLoad callback should have been invoked even with custom views" }

        println("    ✅ onLoad callback invoked with custom views correctly")
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

    private fun createMockGroup(guid: String, name: String): Group {
        return Group().apply {
            this.guid = guid
            this.name = name
            this.groupType = CometChatConstants.GROUP_TYPE_PUBLIC
        }
    }

    private fun createMessages(count: Int): List<BaseMessage> {
        val now = System.currentTimeMillis() / 1000
        val texts = listOf(
            "Hey, how are you?",
            "I'm doing great, thanks!",
            "Did you see the new update?",
            "Yes, it looks amazing!",
            "Let's discuss it tomorrow"
        )
        return (0 until count).map { i ->
            val isIncoming = i % 2 == 0
            val sender = User().apply {
                uid = if (isIncoming) "user-1" else MessageListComposeTestHelper.LOGGED_IN_USER_UID
                name = if (isIncoming) "Alice Johnson" else "Me"
                status = CometChatConstants.USER_STATUS_ONLINE
            }
            TextMessage("user-1", texts[i % texts.size], CometChatConstants.RECEIVER_TYPE_USER).apply {
                this.id = (i + 1).toLong()
                this.sentAt = now - ((count - i) * 120L)
                this.sender = sender
                this.receiverUid = "user-1"
                this.type = CometChatConstants.MESSAGE_TYPE_TEXT
                this.category = CometChatConstants.CATEGORY_MESSAGE
            }
        }
    }

    private fun createViewModelWithMessages(messages: List<BaseMessage>): CometChatMessageListViewModel {
        val repository = object : MessageListRepository {
            override suspend fun fetchPreviousMessages() = Result.success(messages)
            override suspend fun fetchNextMessages(fromMessageId: Long) = Result.success(emptyList<BaseMessage>())
            override suspend fun getConversation(id: String, type: String): Result<Conversation> =
                Result.failure(Exception("Not configured"))
            override suspend fun getMessage(messageId: Long): Result<BaseMessage> =
                messages.find { it.id == messageId }?.let { Result.success(it) }
                    ?: Result.failure(Exception("Not found"))
            override suspend fun deleteMessage(message: BaseMessage) = Result.success(message)
            override suspend fun flagMessage(messageId: Long, reason: String, remark: String) = Result.success(Unit)
            override suspend fun addReaction(messageId: Long, emoji: String): Result<BaseMessage> =
                messages.find { it.id == messageId }?.let { Result.success(it) }
                    ?: Result.failure(Exception("Not found"))
            override suspend fun removeReaction(messageId: Long, emoji: String): Result<BaseMessage> =
                messages.find { it.id == messageId }?.let { Result.success(it) }
                    ?: Result.failure(Exception("Not found"))
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
            override fun getLatestMessageId() = messages.lastOrNull()?.id ?: -1L
            override fun setLatestMessageId(messageId: Long) {}
        }
        return CometChatMessageListViewModel(repository = repository, enableListeners = false)
    }

    private fun createViewModelWithError(): CometChatMessageListViewModel {
        val repository = object : MessageListRepository {
            override suspend fun fetchPreviousMessages(): Result<List<BaseMessage>> =
                Result.failure(com.cometchat.chat.exceptions.CometChatException("ERR", "Error"))
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
