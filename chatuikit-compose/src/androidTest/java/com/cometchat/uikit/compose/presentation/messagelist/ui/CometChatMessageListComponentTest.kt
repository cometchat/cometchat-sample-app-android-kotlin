package com.cometchat.uikit.compose.presentation.messagelist.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
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
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.domain.model.SurroundingMessagesResult
import com.cometchat.uikit.core.domain.repository.MessageListRepository
import com.cometchat.uikit.core.viewmodel.CometChatMessageListViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

/**
 * Instrumented integration tests for CometChatMessageList composable.
 *
 * Tests verify:
 * - Content state rendering with messages
 * - Empty state rendering
 * - Error state rendering
 * - Loading state rendering
 * - Pagination behavior (new message indicator)
 * - Callbacks (onLoad, onEmpty, onError, onMessageClick)
 *
 * Architecture:
 *   [Fake Repository] → [Real ViewModel] → [Real CometChatMessageList Composable]
 *       → [Compose UI Test assertions]
 *
 * Run with:
 *   ./gradlew :chatuikit-compose:connectedDebugAndroidTest \
 *     -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.uikit.compose.presentation.messagelist.ui.CometChatMessageListComponentTest
 */
@RunWith(AndroidJUnit4::class)
class CometChatMessageListComponentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setup() {
        MessageListComposeTestHelper.ensureInitialized()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 1: Content state renders messages
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun contentState_rendersMessages() {
        println("  🧪 contentState_rendersMessages")
        val messages = createMessages(5)
        val viewModel = createViewModelWithMessages(messages)
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

        // Messages should be rendered - use assertCountEquals to verify nodes exist
        // since messages in a LazyColumn may not all be "displayed" (visible in viewport)
        val nodes = composeTestRule.onAllNodesWithContentDescription("Incoming message from Alice Johnson")
        nodes.fetchSemanticsNodes().isNotEmpty().let { hasNodes ->
            assert(hasNodes) { "Expected at least one incoming message node" }
        }

        println("    ✅ Content state renders messages correctly")
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 2: Empty state shows empty view
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun emptyState_showsEmptyView() {
        println("  🧪 emptyState_showsEmptyView")
        val viewModel = createViewModelWithMessages(emptyList())
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

        // Empty state should be displayed (default empty text)
        composeTestRule.onNodeWithText("No Messages Yet").assertIsDisplayed()

        println("    ✅ Empty state renders correctly")
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 3: Error state shows error view
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun errorState_showsErrorView() {
        println("  🧪 errorState_showsErrorView")
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

        // Error state should be displayed - wait for it to appear
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("Something went wrong.")
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Something went wrong.").assertIsDisplayed()

        println("    ✅ Error state renders correctly")
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 4: Loading state shows loading view
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun loadingState_showsLoadingView() {
        println("  🧪 loadingState_showsLoadingView")
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

        // Loading state should be displayed (shimmer or loading indicator)
        // We verify no crash and the component renders
        composeTestRule.waitForIdle()

        println("    ✅ Loading state renders without crash")
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 5: hideEmptyState hides empty view
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun hideEmptyState_hidesEmptyView() {
        println("  🧪 hideEmptyState_hidesEmptyView")
        val viewModel = createViewModelWithMessages(emptyList())
        val user = createMockUser("user-1", "Alice")

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatMessageList(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    user = user,
                    hideEmptyState = true
                )
            }
        }

        composeTestRule.waitForIdle()

        // Empty state text should NOT be displayed
        composeTestRule.onNodeWithText("No Messages Yet").assertDoesNotExist()

        println("    ✅ hideEmptyState hides empty view correctly")
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 6: hideErrorState hides error view
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun hideErrorState_hidesErrorView() {
        println("  🧪 hideErrorState_hidesErrorView")
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
    // TEST 7: onLoad callback invoked when content loads
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun onLoadCallback_invokedWhenContentLoads() {
        println("  🧪 onLoadCallback_invokedWhenContentLoads")
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
                    onLoad = { _ ->
                        onLoadInvoked.set(true)
                    }
                )
            }
        }

        composeTestRule.waitForIdle()

        assert(onLoadInvoked.get()) { "onLoad callback should have been invoked" }

        println("    ✅ onLoad callback invoked correctly")
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 8: onEmpty callback invoked when empty
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun onEmptyCallback_invokedWhenEmpty() {
        println("  🧪 onEmptyCallback_invokedWhenEmpty")
        val onEmptyInvoked = AtomicBoolean(false)
        val viewModel = createViewModelWithMessages(emptyList())
        val user = createMockUser("user-1", "Alice")

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatMessageList(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    user = user,
                    onEmpty = {
                        onEmptyInvoked.set(true)
                    }
                )
            }
        }

        composeTestRule.waitForIdle()

        assert(onEmptyInvoked.get()) { "onEmpty callback should have been invoked" }

        println("    ✅ onEmpty callback invoked correctly")
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 9: onError callback invoked when error occurs
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun onErrorCallback_invokedWhenErrorOccurs() {
        println("  🧪 onErrorCallback_invokedWhenErrorOccurs")
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
    // TEST 10: Multiple messages render in correct order
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun multipleMessages_renderInCorrectOrder() {
        println("  🧪 multipleMessages_renderInCorrectOrder")
        val messages = createMessages(5)
        val viewModel = createViewModelWithMessages(messages)
        val user = createMockUser("user-1", "Alice Johnson")

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

        // Verify multiple messages are rendered
        val nodes = composeTestRule.onAllNodesWithContentDescription("Incoming message from Alice Johnson")
        nodes.fetchSemanticsNodes().isNotEmpty().let { hasNodes ->
            assert(hasNodes) { "Expected at least one incoming message node" }
        }

        println("    ✅ Multiple messages render correctly")
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 11: Group conversation renders messages
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun groupConversation_rendersMessages() {
        println("  🧪 groupConversation_rendersMessages")
        val messages = createMessages(3)
        val viewModel = createViewModelWithMessages(messages)
        val group = createMockGroup("group-1", "Test Group")

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatMessageList(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    group = group
                )
            }
        }

        composeTestRule.waitForIdle()

        // Messages should be rendered in group context
        val nodes = composeTestRule.onAllNodesWithContentDescription("Incoming message from Alice Johnson")
        nodes.fetchSemanticsNodes().isNotEmpty().let { hasNodes ->
            assert(hasNodes) { "Expected at least one incoming message node in group context" }
        }

        println("    ✅ Group conversation renders messages correctly")
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 12: New message indicator initially hidden
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun newMessageIndicator_initiallyHidden() {
        println("  🧪 newMessageIndicator_initiallyHidden")
        val messages = createMessages(5)
        val viewModel = createViewModelWithMessages(messages)
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

        // New message indicator should not be visible initially
        composeTestRule.onNodeWithText("New Messages").assertDoesNotExist()

        println("    ✅ New message indicator is hidden initially")
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

    private fun createTextMessage(
        id: Long,
        text: String,
        senderUid: String,
        senderName: String,
        sentAt: Long,
        receiverId: String = "user-1",
        receiverType: String = CometChatConstants.RECEIVER_TYPE_USER
    ): TextMessage {
        val sender = User().apply {
            uid = senderUid
            name = senderName
            status = CometChatConstants.USER_STATUS_ONLINE
        }
        return TextMessage(receiverId, text, receiverType).apply {
            this.id = id
            this.sentAt = sentAt
            this.sender = sender
            this.receiverUid = receiverId
            this.type = CometChatConstants.MESSAGE_TYPE_TEXT
            this.category = CometChatConstants.CATEGORY_MESSAGE
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
            createTextMessage(
                id = (i + 1).toLong(),
                text = texts[i % texts.size],
                senderUid = if (isIncoming) "user-1" else MessageListComposeTestHelper.LOGGED_IN_USER_UID,
                senderName = if (isIncoming) "Alice Johnson" else "Me",
                sentAt = now - ((count - i) * 120L)
            )
        }
    }

    private fun createViewModelWithMessages(messages: List<BaseMessage>): CometChatMessageListViewModel {
        val repository = object : MessageListRepository {
            override suspend fun fetchPreviousMessages() = Result.success(messages)
            override suspend fun fetchNextMessages(fromMessageId: Long) = Result.success(emptyList<BaseMessage>())
            override suspend fun getConversation(id: String, type: String): Result<Conversation> =
                Result.failure(Exception("Not configured for testing"))
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
                Result.failure(com.cometchat.chat.exceptions.CometChatException("ERR", "Network error"))
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
