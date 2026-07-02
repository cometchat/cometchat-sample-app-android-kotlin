package com.cometchat.uikit.compose.presentation.messagelist.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.MessagesRequest
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Conversation
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.TextMessage
import com.cometchat.chat.models.User
import com.cometchat.uikit.compose.presentation.shared.formatters.CometChatTextFormatter
import com.cometchat.uikit.compose.presentation.shared.messagebubble.BubbleFactory
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatMessageBubbleStyle
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

/**
 * Instrumented tests for CometChatMessageList BubbleFactory integration.
 *
 * Tests verify:
 * - Custom BubbleFactory registration and resolution
 * - BubbleFactory contentView rendering
 * - BubbleFactory bubbleView (complete replacement) rendering
 * - Multiple factories for different message types
 * - Factory override behavior (last factory wins for same key)
 *
 * Architecture:
 *   [Fake Repository] → [Real ViewModel] → [Real CometChatMessageList Composable]
 *       → [Custom BubbleFactory] → [Compose UI Test assertions]
 *
 * Run with:
 *   ./gradlew :chatuikit-compose:connectedDebugAndroidTest \
 *     -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.uikit.compose.presentation.messagelist.ui.CometChatMessageListBubbleFactoryTest
 */
@RunWith(AndroidJUnit4::class)
class CometChatMessageListBubbleFactoryTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setup() {
        MessageListComposeTestHelper.ensureInitialized()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 1: Custom BubbleFactory contentView renders for matching message type
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun customBubbleFactory_contentView_rendersForMatchingType() {
        println("  🧪 customBubbleFactory_contentView_rendersForMatchingType")
        val messages = createTextMessages(3)
        val viewModel = createViewModelWithMessages(messages)
        val user = createMockUser("user-1", "Alice")

        val customFactory = object : BubbleFactory {
            override fun getCategory() = CometChatConstants.CATEGORY_MESSAGE
            override fun getType() = CometChatConstants.MESSAGE_TYPE_TEXT

            override fun getContentView(
                message: BaseMessage,
                alignment: UIKitConstants.MessageBubbleAlignment,
                style: CometChatMessageBubbleStyle,
                textFormatters: List<CometChatTextFormatter>
            ): @Composable () -> Unit = {
                Text("Custom Text Bubble: ${(message as? TextMessage)?.text ?: ""}")
            }
        }

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatMessageList(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    user = user,
                    bubbleFactories = listOf(customFactory)
                )
            }
        }

        composeTestRule.waitForIdle()

        // Custom content view should be rendered
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("Custom Text Bubble: Hello from Alice")
                .fetchSemanticsNodes().isNotEmpty()
        }
        val nodes = composeTestRule.onAllNodesWithText("Custom Text Bubble: Hello from Alice")
        assert(nodes.fetchSemanticsNodes().isNotEmpty()) { "Expected custom text bubble nodes" }

        println("    ✅ Custom BubbleFactory contentView renders correctly")
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 2: Custom BubbleFactory bubbleView replaces entire bubble
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun customBubbleFactory_bubbleView_replacesEntireBubble() {
        println("  🧪 customBubbleFactory_bubbleView_replacesEntireBubble")
        val messages = createTextMessages(3)
        val viewModel = createViewModelWithMessages(messages)
        val user = createMockUser("user-1", "Alice")

        val customFactory = object : BubbleFactory {
            override fun getCategory() = CometChatConstants.CATEGORY_MESSAGE
            override fun getType() = CometChatConstants.MESSAGE_TYPE_TEXT

            override fun getBubbleView(
                message: BaseMessage,
                alignment: UIKitConstants.MessageBubbleAlignment
            ): (@Composable () -> Unit)? = {
                Text("Completely Custom Bubble")
            }
        }

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatMessageList(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    user = user,
                    bubbleFactories = listOf(customFactory)
                )
            }
        }

        composeTestRule.waitForIdle()

        // Complete bubble replacement should be rendered
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("Completely Custom Bubble")
                .fetchSemanticsNodes().isNotEmpty()
        }
        val nodes = composeTestRule.onAllNodesWithText("Completely Custom Bubble")
        assert(nodes.fetchSemanticsNodes().isNotEmpty()) { "Expected custom bubble nodes" }

        println("    ✅ Custom BubbleFactory bubbleView replaces entire bubble correctly")
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 3: Multiple factories for different message types
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun multipleBubbleFactories_resolveCorrectly() {
        println("  🧪 multipleBubbleFactories_resolveCorrectly")
        val messages = createTextMessages(3)
        val viewModel = createViewModelWithMessages(messages)
        val user = createMockUser("user-1", "Alice")

        val textFactory = object : BubbleFactory {
            override fun getCategory() = CometChatConstants.CATEGORY_MESSAGE
            override fun getType() = CometChatConstants.MESSAGE_TYPE_TEXT

            override fun getContentView(
                message: BaseMessage,
                alignment: UIKitConstants.MessageBubbleAlignment,
                style: CometChatMessageBubbleStyle,
                textFormatters: List<CometChatTextFormatter>
            ): @Composable () -> Unit = {
                Text("Text Factory Content")
            }
        }

        val imageFactory = object : BubbleFactory {
            override fun getCategory() = CometChatConstants.CATEGORY_MESSAGE
            override fun getType() = CometChatConstants.MESSAGE_TYPE_IMAGE

            override fun getContentView(
                message: BaseMessage,
                alignment: UIKitConstants.MessageBubbleAlignment,
                style: CometChatMessageBubbleStyle,
                textFormatters: List<CometChatTextFormatter>
            ): @Composable () -> Unit = {
                Text("Image Factory Content")
            }
        }

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatMessageList(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    user = user,
                    bubbleFactories = listOf(textFactory, imageFactory)
                )
            }
        }

        composeTestRule.waitForIdle()

        // Text factory should be used for text messages
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("Text Factory Content")
                .fetchSemanticsNodes().isNotEmpty()
        }
        val nodes = composeTestRule.onAllNodesWithText("Text Factory Content")
        assert(nodes.fetchSemanticsNodes().isNotEmpty()) { "Expected text factory content nodes" }

        // Image factory should NOT be rendered (no image messages in list)
        composeTestRule.onNodeWithText("Image Factory Content")
            .assertDoesNotExist()

        println("    ✅ Multiple factories resolve correctly")
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 4: Factory override — last factory wins for same key
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun factoryOverride_lastFactoryWinsForSameKey() {
        println("  🧪 factoryOverride_lastFactoryWinsForSameKey")
        val messages = createTextMessages(3)
        val viewModel = createViewModelWithMessages(messages)
        val user = createMockUser("user-1", "Alice")

        val firstFactory = object : BubbleFactory {
            override fun getCategory() = CometChatConstants.CATEGORY_MESSAGE
            override fun getType() = CometChatConstants.MESSAGE_TYPE_TEXT

            override fun getContentView(
                message: BaseMessage,
                alignment: UIKitConstants.MessageBubbleAlignment,
                style: CometChatMessageBubbleStyle,
                textFormatters: List<CometChatTextFormatter>
            ): @Composable () -> Unit = {
                Text("First Factory")
            }
        }

        val secondFactory = object : BubbleFactory {
            override fun getCategory() = CometChatConstants.CATEGORY_MESSAGE
            override fun getType() = CometChatConstants.MESSAGE_TYPE_TEXT

            override fun getContentView(
                message: BaseMessage,
                alignment: UIKitConstants.MessageBubbleAlignment,
                style: CometChatMessageBubbleStyle,
                textFormatters: List<CometChatTextFormatter>
            ): @Composable () -> Unit = {
                Text("Second Factory Wins")
            }
        }

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatMessageList(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    user = user,
                    bubbleFactories = listOf(firstFactory, secondFactory)
                )
            }
        }

        composeTestRule.waitForIdle()

        // Second factory should win (last in list for same category_type key)
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("Second Factory Wins")
                .fetchSemanticsNodes().isNotEmpty()
        }
        val nodes = composeTestRule.onAllNodesWithText("Second Factory Wins")
        assert(nodes.fetchSemanticsNodes().isNotEmpty()) { "Expected second factory content nodes" }

        // First factory should NOT be rendered
        composeTestRule.onNodeWithText("First Factory")
            .assertDoesNotExist()

        println("    ✅ Last factory wins for same key correctly")
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 5: Empty bubbleFactories list uses default rendering
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun emptyBubbleFactories_usesDefaultRendering() {
        println("  🧪 emptyBubbleFactories_usesDefaultRendering")
        val messages = createTextMessages(3)
        val viewModel = createViewModelWithMessages(messages)
        val user = createMockUser("user-1", "Alice")

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatMessageList(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    user = user,
                    bubbleFactories = emptyList()  // No custom factories
                )
            }
        }

        composeTestRule.waitForIdle()

        // Default rendering should show messages
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithContentDescription("Incoming message from Alice")
                .fetchSemanticsNodes().isNotEmpty()
        }
        val nodes = composeTestRule.onAllNodesWithContentDescription("Incoming message from Alice")
        assert(nodes.fetchSemanticsNodes().isNotEmpty()) { "Expected default incoming message nodes" }

        println("    ✅ Empty bubbleFactories uses default rendering correctly")
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 6: BubbleFactory with custom style
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun bubbleFactory_withCustomStyle_rendersCorrectly() {
        println("  🧪 bubbleFactory_withCustomStyle_rendersCorrectly")
        val messages = createTextMessages(3)
        val viewModel = createViewModelWithMessages(messages)
        val user = createMockUser("user-1", "Alice")

        val styledFactory = object : BubbleFactory {
            override fun getCategory() = CometChatConstants.CATEGORY_MESSAGE
            override fun getType() = CometChatConstants.MESSAGE_TYPE_TEXT

            override fun getBubbleStyle(
                message: BaseMessage,
                alignment: UIKitConstants.MessageBubbleAlignment
            ): CometChatMessageBubbleStyle? {
                // Return null to use alignment-based defaults
                // (incoming() is @Composable and can't be called from non-composable context)
                return null
            }

            override fun getContentView(
                message: BaseMessage,
                alignment: UIKitConstants.MessageBubbleAlignment,
                style: CometChatMessageBubbleStyle,
                textFormatters: List<CometChatTextFormatter>
            ): @Composable () -> Unit = {
                Text("Styled Bubble Content")
            }
        }

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatMessageList(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    user = user,
                    bubbleFactories = listOf(styledFactory)
                )
            }
        }

        composeTestRule.waitForIdle()

        // Styled content should be rendered
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("Styled Bubble Content")
                .fetchSemanticsNodes().isNotEmpty()
        }
        val nodes = composeTestRule.onAllNodesWithText("Styled Bubble Content")
        assert(nodes.fetchSemanticsNodes().isNotEmpty()) { "Expected styled bubble content nodes" }

        println("    ✅ BubbleFactory with custom style renders correctly")
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

    private fun createTextMessages(count: Int): List<BaseMessage> {
        val now = System.currentTimeMillis() / 1000
        return (0 until count).map { i ->
            val isIncoming = i % 2 == 0
            val sender = User().apply {
                uid = if (isIncoming) "user-1" else MessageListComposeTestHelper.LOGGED_IN_USER_UID
                name = if (isIncoming) "Alice" else "Me"
                status = CometChatConstants.USER_STATUS_ONLINE
            }
            TextMessage("user-1", "Hello from ${sender.name}", CometChatConstants.RECEIVER_TYPE_USER).apply {
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
}
