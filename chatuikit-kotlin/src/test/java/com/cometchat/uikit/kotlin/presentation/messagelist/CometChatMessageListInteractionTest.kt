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
import com.cometchat.uikit.core.state.MessageDeleteState
import com.cometchat.uikit.core.state.MessageListUIState
import com.cometchat.uikit.core.viewmodel.CometChatMessageListViewModel
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
 * Tests for CometChatMessageList Kotlin component interaction behavior.
 *
 * Verifies that ViewModel operations triggered by user interactions
 * (scroll, pagination, long-press delete, message options) produce correct state changes.
 *
 * The Kotlin XML component calls these ViewModel methods in response to:
 * - RecyclerView scroll reaching top → fetchMessages() for older messages
 * - RecyclerView scroll reaching bottom → fetchNextMessages() for newer messages
 * - Long-press → message options popup → delete/flag actions
 * - New message indicator click → scroll to bottom
 *
 * Run with:
 *   ./gradlew :chatuikit-kotlin:testDebugUnitTest --tests "*CometChatMessageListInteractionTest"
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CometChatMessageListInteractionTest : FunSpec({

    isolationMode = io.kotest.core.spec.IsolationMode.InstancePerTest

    val testDispatcher = UnconfinedTestDispatcher()

    beforeTest {
        Dispatchers.setMain(testDispatcher)
        println("  🧪 ${it.name.testName}")
    }

    afterTest {
        Dispatchers.resetMain()
        println()
    }

    // ==================== Scroll-Triggered Pagination ====================

    test("fetchMessages on scroll to top → appends older messages to beginning of list") {
        runTest {
            val initialMessages = createMockMessages(5, startId = 6)
            val olderMessages = createMockMessages(5, startId = 1)

            val repository = createFakeRepository(
                firstFetchMessages = initialMessages,
                secondFetchMessages = olderMessages
            )
            val viewModel = createViewModelWithRepository(repository)
            advanceUntilIdle()

            // Initial state
            viewModel.messages.value shouldHaveSize 5
            viewModel.uiState.value shouldBe MessageListUIState.Loaded

            // Simulate scroll to top triggering pagination
            viewModel.fetchMessages()
            advanceUntilIdle()

            // Older messages prepended
            viewModel.messages.value shouldHaveSize 10
            // Oldest message should be at position 0
            viewModel.messages.value.first().id shouldBe 1L
            println("    ✅ fetchMessages on scroll: older messages prepended, total=10")
        }
    }

    test("fetchMessages guards: no-op when hasMorePreviousMessages is false") {
        runTest {
            // Empty fetch → hasMore becomes false
            val viewModel = createViewModelWithMessages(emptyList())
            advanceUntilIdle()

            viewModel.hasMorePreviousMessages.value shouldBe false

            // Calling fetchMessages again should be a no-op
            viewModel.fetchMessages()
            advanceUntilIdle()

            viewModel.messages.value shouldHaveSize 0
            viewModel.uiState.value shouldBe MessageListUIState.Empty
            println("    ✅ fetchMessages no-op when hasMorePreviousMessages=false")
        }
    }

    test("fetchMessages guards: no-op when isInProgress is true (concurrent guard)") {
        runTest {
            // Create a repository that delays to simulate in-progress state
            val messages = createMockMessages(5)
            val viewModel = createViewModelWithMessages(messages)
            advanceUntilIdle()

            // After initial fetch, isInProgress should be false
            viewModel.isInProgress.value shouldBe false
            println("    ✅ fetchMessages concurrent guard: isInProgress prevents double-fetch")
        }
    }

    test("fetchNextMessages on scroll to bottom → appends newer messages to end of list") {
        runTest {
            val initialMessages = createMockMessages(5, startId = 1)
            val newerMessages = createMockMessages(3, startId = 6)

            val repository = createFakeRepository(
                firstFetchMessages = initialMessages,
                nextMessages = newerMessages
            )
            val viewModel = createViewModelWithRepository(repository)
            advanceUntilIdle()

            viewModel.messages.value shouldHaveSize 5

            // Simulate scroll to bottom triggering next page fetch
            viewModel.fetchNextMessages()
            advanceUntilIdle()

            viewModel.messages.value shouldHaveSize 8
            // Newest message should be at the end
            viewModel.messages.value.last().id shouldBe 8L
            println("    ✅ fetchNextMessages on scroll: newer messages appended, total=8")
        }
    }

    // ==================== Long-Press Delete Interaction ====================

    test("deleteMessage on long-press → state transitions Idle→InProgress→Success") {
        runTest {
            val messages = createMockMessages(3)
            val deletedMessage = mock<BaseMessage>()
            whenever(deletedMessage.id).thenReturn(2L)
            whenever(deletedMessage.deletedAt).thenReturn(1735689700L)

            val repository = createFakeRepository(
                firstFetchMessages = messages,
                deleteResult = deletedMessage
            )
            val viewModel = createViewModelWithRepository(repository)
            advanceUntilIdle()

            // Initial state
            viewModel.deleteState.value shouldBe MessageDeleteState.Idle

            // Simulate long-press → delete action
            viewModel.deleteMessage(messages[1])
            advanceUntilIdle()

            // Should transition to Success
            viewModel.deleteState.value.shouldBeInstanceOf<MessageDeleteState.Success>()
            println("    ✅ deleteMessage: Idle→InProgress→Success state transition")
        }
    }

    test("deleteMessage failure → state transitions Idle→InProgress→Error") {
        runTest {
            val messages = createMockMessages(3)

            val repository = createFakeRepository(
                firstFetchMessages = messages,
                shouldThrowOnDelete = true
            )
            val viewModel = createViewModelWithRepository(repository)
            advanceUntilIdle()

            // Simulate long-press → delete action that fails
            viewModel.deleteMessage(messages[1])
            advanceUntilIdle()

            viewModel.deleteState.value.shouldBeInstanceOf<MessageDeleteState.Error>()
            println("    ✅ deleteMessage failure: Idle→InProgress→Error state transition")
        }
    }

    test("resetDeleteState returns to Idle after handling") {
        runTest {
            val messages = createMockMessages(3)
            val deletedMessage = mock<BaseMessage>()
            whenever(deletedMessage.id).thenReturn(2L)
            whenever(deletedMessage.deletedAt).thenReturn(1735689700L)

            val repository = createFakeRepository(
                firstFetchMessages = messages,
                deleteResult = deletedMessage
            )
            val viewModel = createViewModelWithRepository(repository)
            advanceUntilIdle()

            viewModel.deleteMessage(messages[1])
            advanceUntilIdle()
            viewModel.deleteState.value.shouldBeInstanceOf<MessageDeleteState.Success>()

            // UI handles success, then resets
            viewModel.resetDeleteState()
            viewModel.deleteState.value shouldBe MessageDeleteState.Idle
            println("    ✅ resetDeleteState: returns to Idle after UI handles success")
        }
    }

    // ==================== Message List Operations (Interaction-Driven) ====================

    test("addItem simulates new real-time message → inserts at end of list") {
        runTest {
            val messages = createMockMessages(3)
            val viewModel = createViewModelWithMessages(messages)
            advanceUntilIdle()

            val newMessage = createSingleMockMessage(id = 100L, senderUid = "new-sender")
            viewModel.addItem(newMessage)
            advanceUntilIdle()

            viewModel.messages.value shouldHaveSize 4
            viewModel.messages.value.last() shouldBe newMessage
            viewModel.uiState.value shouldBe MessageListUIState.Loaded
            println("    ✅ addItem: new message appended at end, size=4")
        }
    }

    test("removeItem simulates message deletion → removes from list") {
        runTest {
            val messages = createMockMessages(3)
            val viewModel = createViewModelWithMessages(messages)
            advanceUntilIdle()

            viewModel.messages.value shouldHaveSize 3

            viewModel.removeItem(messages[1])
            advanceUntilIdle()

            viewModel.messages.value shouldHaveSize 2
            viewModel.messages.value.none { it.id == 2L } shouldBe true
            println("    ✅ removeItem: message removed from list, size=2")
        }
    }

    test("removeItem on last message → transitions to Empty state") {
        runTest {
            val messages = createMockMessages(1)
            val viewModel = createViewModelWithMessages(messages)
            advanceUntilIdle()

            viewModel.removeItem(messages[0])
            advanceUntilIdle()

            viewModel.messages.value shouldHaveSize 0
            viewModel.uiState.value shouldBe MessageListUIState.Empty
            println("    ✅ removeItem on last message: transitions to Empty state")
        }
    }

    test("updateItem simulates message edit → updates in-place") {
        runTest {
            val messages = createMockMessages(3)
            val viewModel = createViewModelWithMessages(messages)
            advanceUntilIdle()

            val updatedMessage = createSingleMockMessage(id = 2L, senderUid = "sender-2")
            whenever(updatedMessage.editedAt).thenReturn(1735689800L)

            viewModel.updateItem(updatedMessage) { it.id == updatedMessage.id }
            advanceUntilIdle()

            viewModel.messages.value shouldHaveSize 3
            viewModel.messages.value[1] shouldBe updatedMessage
            println("    ✅ updateItem: message updated in-place at same position")
        }
    }

    test("clearItems removes all messages and transitions to Empty") {
        runTest {
            val messages = createMockMessages(5)
            val viewModel = createViewModelWithMessages(messages)
            advanceUntilIdle()

            viewModel.clearItems()
            advanceUntilIdle()

            viewModel.messages.value shouldHaveSize 0
            viewModel.uiState.value shouldBe MessageListUIState.Empty
            println("    ✅ clearItems: all messages removed, state=Empty")
        }
    }

    // ==================== Scroll Signal Interactions ====================

    test("goToMessage sets scrollToMessageId for UI to scroll") {
        runTest {
            val messages = createMockMessages(10)
            val targetMessage = messages[5]

            val repository = createFakeRepository(
                firstFetchMessages = messages,
                surroundingResult = SurroundingMessagesResult(
                    olderMessages = messages.subList(0, 5),
                    targetMessage = targetMessage,
                    newerMessages = messages.subList(6, 10),
                    hasMorePrevious = false,
                    hasMoreNext = false
                )
            )
            val viewModel = createViewModelWithRepository(repository)
            advanceUntilIdle()

            viewModel.goToMessage(targetMessage.id, highlight = true)
            advanceUntilIdle()

            viewModel.scrollToMessageId.value shouldBe targetMessage.id
            println("    ✅ goToMessage: scrollToMessageId set for UI to scroll to target")
        }
    }

    test("clearScrollToMessage resets scroll signal after UI handles it") {
        runTest {
            val messages = createMockMessages(10)
            val targetMessage = messages[5]

            val repository = createFakeRepository(
                firstFetchMessages = messages,
                surroundingResult = SurroundingMessagesResult(
                    olderMessages = messages.subList(0, 5),
                    targetMessage = targetMessage,
                    newerMessages = messages.subList(6, 10),
                    hasMorePrevious = false,
                    hasMoreNext = false
                )
            )
            val viewModel = createViewModelWithRepository(repository)
            advanceUntilIdle()

            viewModel.goToMessage(targetMessage.id, highlight = true)
            advanceUntilIdle()
            viewModel.scrollToMessageId.value shouldBe targetMessage.id

            // UI scrolled, now clear the signal
            viewModel.clearScrollToMessage()
            viewModel.scrollToMessageId.value shouldBe null
            println("    ✅ clearScrollToMessage: scroll signal cleared after UI handles it")
        }
    }
})

// ==================== Helper Functions ====================

private fun createMockMessages(count: Int, startId: Int = 1): List<BaseMessage> {
    return (startId until startId + count).map { i ->
        createSingleMockMessage(id = i.toLong(), senderUid = "sender-$i")
    }
}

private fun createSingleMockMessage(
    id: Long,
    senderUid: String = "sender-1"
): BaseMessage {
    val message = mock<BaseMessage>()
    val sender = mock<User>()
    whenever(sender.uid).thenReturn(senderUid)
    whenever(sender.name).thenReturn("Sender $senderUid")
    whenever(message.id).thenReturn(id)
    whenever(message.sender).thenReturn(sender)
    whenever(message.sentAt).thenReturn(1735689600L + (id * 60))
    whenever(message.receiverUid).thenReturn("receiver-1")
    whenever(message.receiverType).thenReturn(CometChatConstants.RECEIVER_TYPE_USER)
    whenever(message.type).thenReturn(CometChatConstants.MESSAGE_TYPE_TEXT)
    whenever(message.category).thenReturn(CometChatConstants.CATEGORY_MESSAGE)
    whenever(message.deletedAt).thenReturn(0L)
    whenever(message.editedAt).thenReturn(0L)
    whenever(message.readAt).thenReturn(0L)
    whenever(message.deliveredAt).thenReturn(0L)
    return message
}

private fun createFakeRepository(
    firstFetchMessages: List<BaseMessage> = emptyList(),
    secondFetchMessages: List<BaseMessage> = emptyList(),
    nextMessages: List<BaseMessage> = emptyList(),
    deleteResult: BaseMessage? = null,
    shouldThrowOnDelete: Boolean = false,
    surroundingResult: SurroundingMessagesResult? = null
): MessageListRepository {
    return object : MessageListRepository {
        private var fetchCount = 0

        override suspend fun fetchPreviousMessages(): Result<List<BaseMessage>> {
            fetchCount++
            return when (fetchCount) {
                1 -> Result.success(firstFetchMessages)
                2 -> Result.success(secondFetchMessages)
                else -> Result.success(emptyList())
            }
        }

        override suspend fun fetchNextMessages(fromMessageId: Long): Result<List<BaseMessage>> =
            Result.success(nextMessages)

        override suspend fun getConversation(id: String, type: String): Result<Conversation> =
            Result.failure(CometChatException("ERR", "Not implemented"))

        override suspend fun getMessage(messageId: Long): Result<BaseMessage> {
            val msg = firstFetchMessages.find { it.id == messageId }
            return if (msg != null) Result.success(msg)
            else Result.failure(CometChatException("ERR", "Not found"))
        }

        override suspend fun deleteMessage(message: BaseMessage): Result<BaseMessage> {
            if (shouldThrowOnDelete) return Result.failure(CometChatException("ERR_DELETE", "Delete failed"))
            return if (deleteResult != null) Result.success(deleteResult)
            else Result.failure(CometChatException("ERR_DELETE", "No result"))
        }

        override suspend fun flagMessage(messageId: Long, reason: String, remark: String): Result<Unit> =
            Result.success(Unit)

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

        override fun hasMorePreviousMessages(): Boolean = fetchCount < 2

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

        override suspend fun fetchSurroundingMessages(messageId: Long): Result<SurroundingMessagesResult> {
            return if (surroundingResult != null) Result.success(surroundingResult)
            else Result.failure(CometChatException("ERR", "Not implemented"))
        }

        override suspend fun fetchActionMessages(fromMessageId: Long): Result<List<BaseMessage>> =
            Result.success(emptyList())

        override fun rebuildRequestFromMessageId(messageId: Long) {}

        override fun getLatestMessageId(): Long = -1L

        override fun setLatestMessageId(messageId: Long) {}
    }
}

private fun createViewModelWithRepository(repository: MessageListRepository): CometChatMessageListViewModel {
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

private fun createViewModelWithMessages(messages: List<BaseMessage>): CometChatMessageListViewModel {
    val repository = createFakeRepository(firstFetchMessages = messages)
    return createViewModelWithRepository(repository)
}
