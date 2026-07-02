package com.cometchat.uikit.kotlin.presentation.messagelist

import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.MessagesRequest
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Conversation
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.domain.model.SurroundingMessagesResult
import com.cometchat.uikit.core.domain.repository.MessageListRepository
import com.cometchat.uikit.core.state.MessageListUIState
import com.cometchat.uikit.core.viewmodel.CometChatMessageListViewModel
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
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
 * Tests for CometChatMessageList Kotlin component rendering states.
 *
 * Verifies that the ViewModel produces the correct MessageListUIState for each scenario,
 * which the Kotlin XML component observes to show/hide views:
 * - MessageListUIState.Loading → component shows shimmer loading view
 * - MessageListUIState.Empty → component shows empty state view (avatar + title + subtitle)
 * - MessageListUIState.Loaded → component shows RecyclerView with messages
 * - MessageListUIState.Error → component shows error state view (image + title + subtitle)
 *
 * Also verifies that the messages list provides correct data for rendering
 * (adapter binding, item click callbacks).
 *
 * Run with:
 *   ./gradlew :chatuikit-kotlin:testDebugUnitTest --tests "*CometChatMessageListRenderingTest"
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CometChatMessageListRenderingTest : FunSpec({

    isolationMode = io.kotest.core.spec.IsolationMode.SingleInstance

    val testDispatcher = UnconfinedTestDispatcher()

    beforeTest {
        Dispatchers.setMain(testDispatcher)
        println("  🧪 ${it.name.testName}")
    }

    afterTest {
        Dispatchers.resetMain()
        println()
    }

    // ==================== UIState Rendering Tests ====================

    test("ViewModel initial state is Loading before fetchMessages is called") {
        runTest {
            // Create ViewModel without triggering fetch
            val viewModel = createMessageListViewModel(emptyList(), autoFetch = false)

            viewModel.uiState.value shouldBe MessageListUIState.Loading
            println("    ✅ Initial state is Loading → component shows shimmer loading view")
        }
    }

    test("ViewModel with empty repository → MessageListUIState.Empty (drives empty state view)") {
        runTest {
            val viewModel = createMessageListViewModel(emptyList())
            advanceUntilIdle()

            viewModel.uiState.value shouldBe MessageListUIState.Empty
            viewModel.messages.value shouldHaveSize 0
            println("    ✅ MessageListUIState.Empty → component shows empty state view")
        }
    }

    test("ViewModel with messages → MessageListUIState.Loaded with correct item count") {
        runTest {
            val messages = createMockMessages(5)
            val viewModel = createMessageListViewModel(messages)
            advanceUntilIdle()

            viewModel.uiState.value shouldBe MessageListUIState.Loaded
            viewModel.messages.value shouldHaveSize 5
            println("    ✅ MessageListUIState.Loaded with 5 items → component shows RecyclerView")
        }
    }

    test("ViewModel Loaded state contains the same message objects from repository") {
        runTest {
            val messages = createMockMessages(3)
            val viewModel = createMessageListViewModel(messages)
            advanceUntilIdle()

            viewModel.uiState.value shouldBe MessageListUIState.Loaded
            val loadedMessages = viewModel.messages.value
            loadedMessages shouldHaveSize 3
            // Messages should be the same objects (by ID)
            loadedMessages.map { it.id }.toSet() shouldBe messages.map { it.id }.toSet()
            println("    ✅ Loaded state holds the exact message objects from the repository")
        }
    }

    test("ViewModel messages contain correct data for rendering (sender, text, timestamp)") {
        runTest {
            val messages = createMockMessages(3)
            val viewModel = createMessageListViewModel(messages)
            advanceUntilIdle()

            val items = viewModel.messages.value
            items shouldHaveSize 3

            // Verify each message has the data needed for bubble rendering
            items.forEachIndexed { index, message ->
                message.sender shouldNotBe null
                message.sender.uid shouldNotBe null
                message.sentAt shouldNotBe 0L
            }
            // Verify messages are from our mock data (IDs 1, 2, 3)
            items.map { it.id }.toSet() shouldBe setOf(1L, 2L, 3L)
            println("    ✅ All 3 messages have correct data for bubble rendering (sender, timestamp)")
        }
    }

    // ==================== Item Access for Adapter Binding ====================

    test("getItemAt returns correct message for RecyclerView position (drives onItemClick)") {
        runTest {
            val messages = createMockMessages(5)
            val viewModel = createMessageListViewModel(messages)
            advanceUntilIdle()

            // Simulate RecyclerView adapter accessing items by position
            val item0 = viewModel.getItemAt(0)
            val item2 = viewModel.getItemAt(2)
            val item4 = viewModel.getItemAt(4)
            val outOfBounds = viewModel.getItemAt(10)

            item0 shouldBe messages[0]
            item2 shouldBe messages[2]
            item4 shouldBe messages[4]
            outOfBounds shouldBe null
            println("    ✅ getItemAt: correct message for positions 0, 2, 4; null for out-of-bounds")
        }
    }

    test("getItemCount returns correct count for adapter") {
        runTest {
            val messages = createMockMessages(7)
            val viewModel = createMessageListViewModel(messages)
            advanceUntilIdle()

            viewModel.getItemCount() shouldBe 7
            println("    ✅ getItemCount: returns 7 for adapter binding")
        }
    }

    test("messages list provides correct item for onItemClick callback at any valid index") {
        runTest {
            val messages = createMockMessages(7)
            val viewModel = createMessageListViewModel(messages)
            advanceUntilIdle()

            // Verify every item in the list can be accessed correctly
            viewModel.messages.value.forEachIndexed { index, message ->
                val retrieved = viewModel.getItemAt(index)
                retrieved shouldBe message
            }
            println("    ✅ All 7 message items accessible by index for onItemClick callback")
        }
    }

    // ==================== Pagination Indicator State ====================

    test("hasMorePreviousMessages is true initially (drives top pagination indicator)") {
        runTest {
            val viewModel = createMessageListViewModel(emptyList(), autoFetch = false)

            viewModel.hasMorePreviousMessages.value shouldBe true
            println("    ✅ hasMorePreviousMessages=true → top pagination indicator can show")
        }
    }

    test("hasMorePreviousMessages becomes false after empty fetch (hides top pagination indicator)") {
        runTest {
            val viewModel = createMessageListViewModel(emptyList())
            advanceUntilIdle()

            viewModel.hasMorePreviousMessages.value shouldBe false
            println("    ✅ hasMorePreviousMessages=false after empty fetch → top indicator hidden")
        }
    }

    test("hasMorePreviousMessages remains true after non-empty fetch (shows top pagination indicator)") {
        runTest {
            val messages = createMockMessages(5)
            val viewModel = createMessageListViewModel(messages, hasMore = true)
            advanceUntilIdle()

            viewModel.hasMorePreviousMessages.value shouldBe true
            println("    ✅ hasMorePreviousMessages=true after non-empty fetch → top indicator visible")
        }
    }

    test("isInProgress is false after fetch completes (hides loading indicator)") {
        runTest {
            val messages = createMockMessages(5)
            val viewModel = createMessageListViewModel(messages)
            advanceUntilIdle()

            viewModel.isInProgress.value shouldBe false
            println("    ✅ isInProgress=false after fetch → loading indicator hidden")
        }
    }

    // ==================== Error State (last to avoid coroutine leaking) ====================

    test("ViewModel with error repository → MessageListUIState.Error (drives error state view)") {
        runTest {
            val viewModel = createMessageListViewModelWithError("LOAD_ERR", "Failed to load messages")
            advanceUntilIdle()

            viewModel.uiState.value.shouldBeInstanceOf<MessageListUIState.Error>()
            val errorState = viewModel.uiState.value as MessageListUIState.Error
            errorState.exception.shouldBeInstanceOf<CometChatException>()
            (errorState.exception as CometChatException).code shouldBe "LOAD_ERR"
            println("    ✅ MessageListUIState.Error → component shows error state view with message")
        }
    }
})

// ==================== Helper Functions ====================

private fun createMockMessages(count: Int): List<BaseMessage> {
    return (1..count).map { i ->
        val message = mock<BaseMessage>()
        val sender = mock<User>()
        whenever(sender.uid).thenReturn("sender-$i")
        whenever(sender.name).thenReturn("Sender $i")
        whenever(message.id).thenReturn(i.toLong())
        whenever(message.sender).thenReturn(sender)
        whenever(message.sentAt).thenReturn(1735689600L + (i * 60))
        whenever(message.receiverUid).thenReturn("receiver-1")
        whenever(message.receiverType).thenReturn(CometChatConstants.RECEIVER_TYPE_USER)
        whenever(message.type).thenReturn(CometChatConstants.MESSAGE_TYPE_TEXT)
        whenever(message.category).thenReturn(CometChatConstants.CATEGORY_MESSAGE)
        whenever(message.deletedAt).thenReturn(0L)
        whenever(message.editedAt).thenReturn(0L)
        whenever(message.readAt).thenReturn(0L)
        whenever(message.deliveredAt).thenReturn(0L)
        message
    }
}

/**
 * Creates a ViewModel with a fake repository that returns the given messages.
 * If autoFetch is true, configures for a user and triggers fetchMessages.
 */
private fun createMessageListViewModel(
    messages: List<BaseMessage>,
    autoFetch: Boolean = true,
    hasMore: Boolean = false
): CometChatMessageListViewModel {
    val repository = object : MessageListRepository {
        private var fetchCount = 0

        override suspend fun fetchPreviousMessages(): Result<List<BaseMessage>> {
            fetchCount++
            return if (fetchCount == 1) Result.success(messages)
            else Result.success(emptyList())
        }

        override suspend fun fetchNextMessages(fromMessageId: Long): Result<List<BaseMessage>> =
            Result.success(emptyList())

        override suspend fun getConversation(id: String, type: String): Result<Conversation> =
            Result.failure(CometChatException("ERR", "Not implemented"))

        override suspend fun getMessage(messageId: Long): Result<BaseMessage> =
            Result.failure(CometChatException("ERR", "Not implemented"))

        override suspend fun deleteMessage(message: BaseMessage): Result<BaseMessage> =
            Result.failure(CometChatException("ERR", "Not implemented"))

        override suspend fun flagMessage(messageId: Long, reason: String, remark: String): Result<Unit> =
            Result.failure(CometChatException("ERR", "Not implemented"))

        override suspend fun addReaction(messageId: Long, emoji: String): Result<BaseMessage> =
            Result.failure(CometChatException("ERR", "Not implemented"))

        override suspend fun removeReaction(messageId: Long, emoji: String): Result<BaseMessage> =
            Result.failure(CometChatException("ERR", "Not implemented"))

        override suspend fun markAsDelivered(message: BaseMessage): Result<Unit> =
            Result.success(Unit)

        override suspend fun markAsRead(message: BaseMessage): Result<Unit> =
            Result.success(Unit)

        override suspend fun markAsUnread(message: BaseMessage): Result<Conversation> =
            Result.failure(CometChatException("ERR", "Not implemented"))

        override fun hasMorePreviousMessages(): Boolean = if (hasMore) true else fetchCount < 1

        override fun resetRequest() {}

        override fun configureForUser(
            user: User,
            messagesTypes: List<String>,
            messagesCategories: List<String>,
            parentMessageId: Long,
            messagesRequestBuilder: MessagesRequest.MessagesRequestBuilder?
        ) {}

        override fun configureForGroup(
            group: Group,
            messagesTypes: List<String>,
            messagesCategories: List<String>,
            parentMessageId: Long,
            messagesRequestBuilder: MessagesRequest.MessagesRequestBuilder?
        ) {}

        override suspend fun fetchSurroundingMessages(messageId: Long): Result<SurroundingMessagesResult> =
            Result.failure(CometChatException("ERR", "Not implemented"))

        override suspend fun fetchActionMessages(fromMessageId: Long): Result<List<BaseMessage>> =
            Result.success(emptyList())

        override fun rebuildRequestFromMessageId(messageId: Long) {}

        override fun getLatestMessageId(): Long = -1L

        override fun setLatestMessageId(messageId: Long) {}
    }

    val viewModel = CometChatMessageListViewModel(
        repository = repository,
        enableListeners = false
    )

    if (autoFetch) {
        val user = mock<User>()
        whenever(user.uid).thenReturn("user-1")
        whenever(user.name).thenReturn("Test User")
        viewModel.setUser(user)
        viewModel.fetchMessages()
    }

    return viewModel
}

/**
 * Creates a ViewModel with a fake repository that throws an error on fetch.
 */
private fun createMessageListViewModelWithError(
    code: String,
    message: String
): CometChatMessageListViewModel {
    val repository = object : MessageListRepository {
        override suspend fun fetchPreviousMessages(): Result<List<BaseMessage>> =
            Result.failure(CometChatException(code, message))

        override suspend fun fetchNextMessages(fromMessageId: Long): Result<List<BaseMessage>> =
            Result.success(emptyList())

        override suspend fun getConversation(id: String, type: String): Result<Conversation> =
            Result.failure(CometChatException("ERR", "Not implemented"))

        override suspend fun getMessage(messageId: Long): Result<BaseMessage> =
            Result.failure(CometChatException("ERR", "Not implemented"))

        override suspend fun deleteMessage(message: BaseMessage): Result<BaseMessage> =
            Result.failure(CometChatException("ERR", "Not implemented"))

        override suspend fun flagMessage(messageId: Long, reason: String, remark: String): Result<Unit> =
            Result.failure(CometChatException("ERR", "Not implemented"))

        override suspend fun addReaction(messageId: Long, emoji: String): Result<BaseMessage> =
            Result.failure(CometChatException("ERR", "Not implemented"))

        override suspend fun removeReaction(messageId: Long, emoji: String): Result<BaseMessage> =
            Result.failure(CometChatException("ERR", "Not implemented"))

        override suspend fun markAsDelivered(message: BaseMessage): Result<Unit> =
            Result.success(Unit)

        override suspend fun markAsRead(message: BaseMessage): Result<Unit> =
            Result.success(Unit)

        override suspend fun markAsUnread(message: BaseMessage): Result<Conversation> =
            Result.failure(CometChatException("ERR", "Not implemented"))

        override fun hasMorePreviousMessages(): Boolean = true

        override fun resetRequest() {}

        override fun configureForUser(
            user: User,
            messagesTypes: List<String>,
            messagesCategories: List<String>,
            parentMessageId: Long,
            messagesRequestBuilder: MessagesRequest.MessagesRequestBuilder?
        ) {}

        override fun configureForGroup(
            group: Group,
            messagesTypes: List<String>,
            messagesCategories: List<String>,
            parentMessageId: Long,
            messagesRequestBuilder: MessagesRequest.MessagesRequestBuilder?
        ) {}

        override suspend fun fetchSurroundingMessages(messageId: Long): Result<SurroundingMessagesResult> =
            Result.failure(CometChatException("ERR", "Not implemented"))

        override suspend fun fetchActionMessages(fromMessageId: Long): Result<List<BaseMessage>> =
            Result.success(emptyList())

        override fun rebuildRequestFromMessageId(messageId: Long) {}

        override fun getLatestMessageId(): Long = -1L

        override fun setLatestMessageId(messageId: Long) {}
    }

    val viewModel = CometChatMessageListViewModel(
        repository = repository,
        enableListeners = false
    )

    val user = mock<User>()
    whenever(user.uid).thenReturn("user-1")
    whenever(user.name).thenReturn("Test User")
    viewModel.setUser(user)
    viewModel.fetchMessages()

    return viewModel
}
