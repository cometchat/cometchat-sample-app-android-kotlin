package com.cometchat.uikit.core.viewmodel.messagelist

import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.MessagesRequest
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Conversation
import com.cometchat.uikit.core.data.datasource.MessageListDataSource
import com.cometchat.uikit.core.data.repository.MessageListRepositoryImpl
import com.cometchat.uikit.core.state.MessageDeleteState
import com.cometchat.uikit.core.state.MessageListUIState
import com.cometchat.uikit.core.testutils.MockFactory
import com.cometchat.uikit.core.viewmodel.CometChatMessageListViewModel
import io.kotest.core.spec.IsolationMode
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
 * Layer 5 — Full Chain Integration Test.
 *
 * Tests the complete chain: fake DataSource → real Repository → real ViewModel.
 * Only the DataSource is faked — everything else is real.
 *
 * Tests:
 * - fake DataSource → real Repo → real ViewModel → fetchMessages → Loaded
 * - configure → fetch → paginate (fetchNext) → messages appended
 * - goToMessage → surrounding messages → scroll signal
 * - fetchMessagesWithUnreadCount → correct branch
 * - deleteMessage → state machine → list updated
 * - empty DataSource → Empty state
 *
 * Architecture:
 * - Only fakes the DataSource layer (everything else is real)
 * - Uses `enableListeners = false` to avoid SDK dependencies
 * - Uses UnconfinedTestDispatcher + Dispatchers.setMain/resetMain
 *
 * Run with:
 *   ./gradlew :chatuikit-core:testDebugUnitTest --tests "*MessageListFullChainIntegrationTest"
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MessageListFullChainIntegrationTest : FunSpec({

    isolationMode = IsolationMode.SingleInstance

    val testDispatcher = UnconfinedTestDispatcher()

    beforeSpec {
        Dispatchers.setMain(testDispatcher)
    }

    afterSpec {
        Thread.sleep(50)
        Dispatchers.resetMain()
    }

    beforeTest {
        println("  🧪 ${it.name.testName}")
    }

    afterTest {
        println()
    }

    test("warmup - absorb leaked exceptions") {
        try { runTest(testDispatcher) { } } catch (_: Exception) { }
    }

    /**
     * Creates a fake DataSource that returns the given messages on fetchPreviousMessages.
     */
    fun createFakeDataSource(
        messages: List<BaseMessage> = emptyList(),
        nextMessages: List<BaseMessage> = emptyList(),
        conversation: Conversation? = null,
        deleteResult: BaseMessage? = null,
        shouldThrowOnFetch: Boolean = false,
        shouldThrowOnDelete: Boolean = false
    ): MessageListDataSource {
        return object : MessageListDataSource {
            private var fetchCount = 0

            override suspend fun fetchPreviousMessages(request: MessagesRequest): List<BaseMessage> {
                if (shouldThrowOnFetch) throw CometChatException("ERR_FETCH", "Network error")
                fetchCount++
                // Return messages on first call, empty on subsequent (simulating end of pagination)
                return if (fetchCount == 1) messages else emptyList()
            }

            override suspend fun fetchNextMessages(request: MessagesRequest): List<BaseMessage> {
                return nextMessages
            }

            override suspend fun getConversation(id: String, type: String): Conversation {
                return conversation ?: throw CometChatException("ERR_CONV", "Not found")
            }

            override suspend fun getMessage(messageId: Long): BaseMessage {
                return messages.find { it.id == messageId }
                    ?: throw CometChatException("ERR_MSG", "Message not found")
            }

            override suspend fun deleteMessage(messageId: Long): BaseMessage {
                if (shouldThrowOnDelete) throw CometChatException("ERR_DELETE", "Delete failed")
                return deleteResult ?: messages.find { it.id == messageId }
                    ?: throw CometChatException("ERR_DELETE", "Message not found")
            }

            override suspend fun flagMessage(messageId: Long, reason: String, remark: String) {
                // No-op for tests
            }

            override suspend fun addReaction(messageId: Long, emoji: String): BaseMessage {
                return messages.find { it.id == messageId }
                    ?: throw CometChatException("ERR_REACT", "Message not found")
            }

            override suspend fun removeReaction(messageId: Long, emoji: String): BaseMessage {
                return messages.find { it.id == messageId }
                    ?: throw CometChatException("ERR_REACT", "Message not found")
            }

            override suspend fun markAsDelivered(message: BaseMessage) {
                // No-op
            }

            override suspend fun markAsRead(message: BaseMessage) {
                // No-op
            }

            override suspend fun markAsUnread(message: BaseMessage): Conversation {
                return conversation ?: mock()
            }
        }
    }


    // ==================== 19.2 fake DataSource → real Repo → real ViewModel → fetchMessages → Loaded ====================

    test("full chain: DataSource returning items → ViewModel shows Loaded state") {
        runTest(testDispatcher) {
            val fakeMessages = MockFactory.createMessages(5, startId = 1L, senderUid = "other-user", receiverId = "test-user")
            val dataSource = createFakeDataSource(messages = fakeMessages.map { it as BaseMessage })
            val repository = MessageListRepositoryImpl(dataSource)

            val viewModel = CometChatMessageListViewModel(
                repository = repository,
                enableListeners = false
            )

            val user = MockFactory.createUser(uid = "test-user", name = "Test User")
            viewModel.setUser(user)
            viewModel.fetchMessages()
            advanceUntilIdle()

            viewModel.uiState.value.shouldBeInstanceOf<MessageListUIState.Loaded>()
            viewModel.getItems() shouldHaveSize 5
            println("    ✅ Full chain: DataSource → Repo → ViewModel → Loaded with 5 messages")
        }
    }


    // ==================== 19.3 configure → fetch → paginate (fetchNext) → messages appended ====================

    test("full chain: configure → fetch → fetchNext → messages appended") {
        runTest(testDispatcher) {
            val initialMessages = MockFactory.createMessages(5, startId = 1L, senderUid = "other-user", receiverId = "test-user")
            val nextMessages = MockFactory.createMessages(3, startId = 6L, senderUid = "other-user", receiverId = "test-user")

            val dataSource = createFakeDataSource(
                messages = initialMessages.map { it as BaseMessage },
                nextMessages = nextMessages.map { it as BaseMessage }
            )
            val repository = MessageListRepositoryImpl(dataSource)

            val viewModel = CometChatMessageListViewModel(
                repository = repository,
                enableListeners = false
            )

            val user = MockFactory.createUser(uid = "test-user", name = "Test User")
            viewModel.setUser(user)
            viewModel.fetchMessages()
            advanceUntilIdle()

            // Initial fetch
            viewModel.getItems() shouldHaveSize 5

            // Fetch next (newer messages)
            viewModel.fetchNextMessages()
            advanceUntilIdle()

            // Messages should be appended
            viewModel.getItems() shouldHaveSize 8
            println("    ✅ Full chain: fetch → fetchNext → 5 + 3 = 8 messages")
        }
    }


    // ==================== 19.4 goToMessage → surrounding messages → scroll signal ====================

    test("full chain: goToMessage → surrounding messages → scrollToMessageId set") {
        runTest(testDispatcher) {
            // Create messages that include the target
            val allMessages = MockFactory.createMessages(15, startId = 1L, senderUid = "other-user", receiverId = "test-user")
            val targetId = 8L

            val dataSource = createFakeDataSource(
                messages = allMessages.map { it as BaseMessage }
            )
            val repository = MessageListRepositoryImpl(dataSource)

            val viewModel = CometChatMessageListViewModel(
                repository = repository,
                enableListeners = false
            )

            val user = MockFactory.createUser(uid = "test-user", name = "Test User")
            viewModel.setUser(user)
            viewModel.fetchMessages()
            advanceUntilIdle()

            // Call goToMessage
            viewModel.goToMessage(targetId)
            advanceUntilIdle()

            // scrollToMessageId should be set to the target
            viewModel.scrollToMessageId.value shouldBe targetId
            println("    ✅ Full chain: goToMessage → scrollToMessageId=$targetId")
        }
    }


    // ==================== 19.5 fetchMessagesWithUnreadCount → correct branch ====================

    test("full chain: fetchMessagesWithUnreadCount with no unread → fetchMessages branch") {
        runTest(testDispatcher) {
            val messages = MockFactory.createMessages(5, startId = 1L, senderUid = "other-user", receiverId = "test-user")
            val conversation = MockFactory.createConversation(
                unreadCount = 0,
                lastReadMessageId = 0L,
                latestMessageId = 5L
            )
            whenever(conversation.latestMessageId).thenReturn(5L)

            val dataSource = createFakeDataSource(
                messages = messages.map { it as BaseMessage },
                conversation = conversation
            )
            val repository = MessageListRepositoryImpl(dataSource)

            val viewModel = CometChatMessageListViewModel(
                repository = repository,
                enableListeners = false
            )
            viewModel.setUnreadThreshold(Int.MAX_VALUE)

            val user = MockFactory.createUser(uid = "test-user", name = "Test User")
            viewModel.setUser(user)
            viewModel.setStartFromUnreadMessages(false)
            viewModel.fetchMessagesWithUnreadCount()
            advanceUntilIdle()

            // Should fall through to fetchMessages (no unread conditions met)
            viewModel.uiState.value.shouldBeInstanceOf<MessageListUIState.Loaded>()
            viewModel.scrollToMessageId.value shouldBe null
            println("    ✅ Full chain: fetchMessagesWithUnreadCount → fetchMessages branch (no unread)")
        }
    }


    // ==================== 19.6 deleteMessage → state machine → list updated ====================

    test("full chain: deleteMessage → Success state → message updated in list") {
        runTest(testDispatcher) {
            val messages = MockFactory.createMessages(5, startId = 1L, senderUid = "other-user", receiverId = "test-user")
            val deletedMessage = MockFactory.createTextMessage(
                id = 3L,
                text = "Message deleted",
                senderUid = "other-user",
                receiverId = "test-user",
                deletedAt = 1700000100L
            )

            val dataSource = createFakeDataSource(
                messages = messages.map { it as BaseMessage },
                deleteResult = deletedMessage as BaseMessage
            )
            val repository = MessageListRepositoryImpl(dataSource)

            val viewModel = CometChatMessageListViewModel(
                repository = repository,
                enableListeners = false
            )

            val user = MockFactory.createUser(uid = "test-user", name = "Test User")
            viewModel.setUser(user)
            viewModel.setHideDeleteMessage(false)
            viewModel.fetchMessages()
            advanceUntilIdle()

            viewModel.getItems() shouldHaveSize 5

            // Delete message
            viewModel.deleteMessage(messages[2] as BaseMessage)
            advanceUntilIdle()

            // State should be Success
            viewModel.deleteState.value.shouldBeInstanceOf<MessageDeleteState.Success>()
            // Message should be updated in-place (not removed)
            viewModel.getItems() shouldHaveSize 5
            println("    ✅ Full chain: deleteMessage → Success → message updated in list")
        }
    }

    test("full chain: deleteMessage with hideDeleteMessage=true → message removed") {
        runTest(testDispatcher) {
            val messages = MockFactory.createMessages(5, startId = 1L, senderUid = "other-user", receiverId = "test-user")
            val deletedMessage = MockFactory.createTextMessage(
                id = 3L,
                text = "Deleted",
                senderUid = "other-user",
                receiverId = "test-user",
                deletedAt = 1700000100L
            )

            val dataSource = createFakeDataSource(
                messages = messages.map { it as BaseMessage },
                deleteResult = deletedMessage as BaseMessage
            )
            val repository = MessageListRepositoryImpl(dataSource)

            val viewModel = CometChatMessageListViewModel(
                repository = repository,
                enableListeners = false
            )

            val user = MockFactory.createUser(uid = "test-user", name = "Test User")
            viewModel.setUser(user)
            viewModel.setHideDeleteMessage(true)
            viewModel.fetchMessages()
            advanceUntilIdle()

            viewModel.getItems() shouldHaveSize 5

            // Delete message
            viewModel.deleteMessage(messages[2] as BaseMessage)
            advanceUntilIdle()

            // Message should be removed
            viewModel.getItems() shouldHaveSize 4
            viewModel.deleteState.value.shouldBeInstanceOf<MessageDeleteState.Success>()
            println("    ✅ Full chain: deleteMessage with hide=true → message removed")
        }
    }


    // ==================== 19.7 empty DataSource → Empty state ====================

    test("full chain: DataSource returning empty → ViewModel shows Empty state") {
        runTest(testDispatcher) {
            val dataSource = createFakeDataSource(messages = emptyList())
            val repository = MessageListRepositoryImpl(dataSource)

            val viewModel = CometChatMessageListViewModel(
                repository = repository,
                enableListeners = false
            )

            val user = MockFactory.createUser(uid = "test-user", name = "Test User")
            viewModel.setUser(user)
            viewModel.fetchMessages()
            advanceUntilIdle()

            viewModel.uiState.value shouldBe MessageListUIState.Empty
            viewModel.getItems() shouldHaveSize 0
            println("    ✅ Full chain: empty DataSource → Empty state")
        }
    }

    test("full chain: DataSource throwing → ViewModel shows Error state") {
        runTest(testDispatcher) {
            val dataSource = createFakeDataSource(shouldThrowOnFetch = true)
            val repository = MessageListRepositoryImpl(dataSource)

            val viewModel = CometChatMessageListViewModel(
                repository = repository,
                enableListeners = false
            )

            val user = MockFactory.createUser(uid = "test-user", name = "Test User")
            viewModel.setUser(user)
            viewModel.fetchMessages()
            advanceUntilIdle()

            viewModel.uiState.value.shouldBeInstanceOf<MessageListUIState.Error>()
            println("    ✅ Full chain: DataSource throwing → Error state")
        }
    }
})
