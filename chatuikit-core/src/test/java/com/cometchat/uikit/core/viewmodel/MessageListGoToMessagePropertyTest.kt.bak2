package com.cometchat.uikit.core.viewmodel

import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Conversation
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.TextMessage
import com.cometchat.chat.models.User
import com.cometchat.chat.core.MessagesRequest
import com.cometchat.uikit.core.domain.model.SurroundingMessagesResult
import com.cometchat.uikit.core.domain.repository.MessageListRepository
import com.cometchat.uikit.core.state.MessageListUIState
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.long
import io.kotest.property.checkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

/**
 * Property-based tests for goToMessage functionality in [CometChatMessageListViewModel].
 * Each test validates a correctness property from the design document.
 *
 * Feature: message-list-fetching-parity
 * **Validates: Requirements 1.1-1.9, 2.1-2.4, 8.1-8.3**
 *
 * ## Test Coverage
 *
 * | Property | Description | Requirements |
 * |----------|-------------|--------------|
 * | Property 1 | goToMessage Triggers Surrounding Message Fetch | 1.1, 1.2 |
 * | Property 2 | Message Window Chronological Ordering | 1.4 |
 * | Property 3 | Message List Replacement After goToMessage | 1.5 |
 * | Property 4 | Pagination State Updates After goToMessage | 1.6, 1.7 |
 * | Property 5 | scrollToMessageId Emission After goToMessage | 1.8 |
 * | Property 6 | Error State on goToMessage Failure | 1.9, 8.3 |
 * | Property 7 | Request Rebuilding After goToMessage | 2.1, 2.2, 2.3, 2.4 |
 * | Property 15 | UI State Transitions During goToMessage | 8.1, 8.2 |
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MessageListGoToMessagePropertyTest : FunSpec({

    val testDispatcher = StandardTestDispatcher()

    beforeSpec {
        Dispatchers.setMain(testDispatcher)
    }

    afterSpec {
        Dispatchers.resetMain()
    }

    /**
     * Mock repository for testing goToMessage functionality.
     * Tracks method calls and allows configuring return values.
     */
    class GoToMessageMockRepository : MessageListRepository {
        // Tracking variables for method calls
        var getMessageCalled = false
        var getMessageId: Long? = null
        var fetchSurroundingMessagesCalled = false
        var fetchSurroundingMessagesId: Long? = null
        var rebuildRequestCalled = false
        var rebuildRequestMessageId: Long? = null
        
        // Configurable return values
        var getMessageResult: Result<BaseMessage>? = null
        var fetchSurroundingMessagesResult: Result<SurroundingMessagesResult>? = null
        var fetchPreviousMessagesResult: Result<List<BaseMessage>> = Result.success(emptyList())
        var fetchNextMessagesResult: Result<List<BaseMessage>> = Result.success(emptyList())
        var hasMorePrevious: Boolean = true
        
        private var latestMessageId: Long = -1
        
        override suspend fun getMessage(messageId: Long): Result<BaseMessage> {
            getMessageCalled = true
            getMessageId = messageId
            return getMessageResult ?: Result.failure(Exception("getMessage not configured"))
        }
        
        override suspend fun fetchSurroundingMessages(messageId: Long): Result<SurroundingMessagesResult> {
            fetchSurroundingMessagesCalled = true
            fetchSurroundingMessagesId = messageId
            return fetchSurroundingMessagesResult ?: Result.failure(Exception("fetchSurroundingMessages not configured"))
        }
        
        override fun rebuildRequestFromMessageId(messageId: Long) {
            rebuildRequestCalled = true
            rebuildRequestMessageId = messageId
        }
        
        override suspend fun fetchPreviousMessages(): Result<List<BaseMessage>> = fetchPreviousMessagesResult
        override suspend fun fetchNextMessages(fromMessageId: Long): Result<List<BaseMessage>> = fetchNextMessagesResult
        override suspend fun getConversation(id: String, type: String): Result<Conversation> = Result.failure(Exception("Not configured"))
        override suspend fun deleteMessage(message: BaseMessage): Result<BaseMessage> = Result.failure(Exception("Not configured"))
        override suspend fun flagMessage(messageId: Long, reason: String, remark: String): Result<Unit> = Result.success(Unit)
        override suspend fun addReaction(messageId: Long, emoji: String): Result<BaseMessage> = Result.failure(Exception("Not configured"))
        override suspend fun removeReaction(messageId: Long, emoji: String): Result<BaseMessage> = Result.failure(Exception("Not configured"))
        override suspend fun markAsRead(message: BaseMessage): Result<Unit> = Result.success(Unit)
        override suspend fun markAsDelivered(message: BaseMessage): Result<Unit> = Result.success(Unit)
        override suspend fun markAsUnread(message: BaseMessage): Result<Conversation> = 
            Result.success(Conversation().apply { unreadMessageCount = 1 })
        override fun hasMorePreviousMessages(): Boolean = hasMorePrevious
        override fun resetRequest() { hasMorePrevious = true }
        override fun configureForUser(user: User, messagesTypes: List<String>, messagesCategories: List<String>, parentMessageId: Long, messagesRequestBuilder: MessagesRequest.MessagesRequestBuilder?) {}
        override fun configureForGroup(group: Group, messagesTypes: List<String>, messagesCategories: List<String>, parentMessageId: Long, messagesRequestBuilder: MessagesRequest.MessagesRequestBuilder?) {}
        override suspend fun fetchActionMessages(fromMessageId: Long): Result<List<BaseMessage>> = Result.success(emptyList())
        override fun getLatestMessageId(): Long = latestMessageId
        override fun setLatestMessageId(messageId: Long) { latestMessageId = messageId }
        
        fun reset() {
            getMessageCalled = false
            getMessageId = null
            fetchSurroundingMessagesCalled = false
            fetchSurroundingMessagesId = null
            rebuildRequestCalled = false
            rebuildRequestMessageId = null
        }
    }

    /**
     * Creates mock messages for testing.
     * 
     * @param count Number of messages to create
     * @param startId Starting ID for the messages
     * @param senderId Optional sender UID (defaults to "sender_{id}")
     */
    fun createMockMessages(count: Int, startId: Long = 1L, senderId: String? = null): List<BaseMessage> {
        return (0 until count).map { index ->
            val id = startId + index
            TextMessage(
                "receiver_$id",
                "Test message $id",
                CometChatConstants.RECEIVER_TYPE_USER
            ).apply {
                this.id = id
                sentAt = System.currentTimeMillis() + (index * 1000)
                sender = User().apply { uid = senderId ?: "sender_$id" }
            }
        }
    }

    /**
     * Creates a SurroundingMessagesResult for testing.
     */
    fun createSurroundingResult(
        olderCount: Int,
        targetId: Long,
        newerCount: Int,
        hasMorePrevious: Boolean = true,
        hasMoreNext: Boolean = true
    ): SurroundingMessagesResult {
        val olderMessages = createMockMessages(olderCount, startId = targetId - olderCount)
        val targetMessage = createMockMessages(1, startId = targetId).first()
        val newerMessages = createMockMessages(newerCount, startId = targetId + 1)
        
        return SurroundingMessagesResult(
            olderMessages = olderMessages,
            targetMessage = targetMessage,
            newerMessages = newerMessages,
            hasMorePrevious = hasMorePrevious,
            hasMoreNext = hasMoreNext
        )
    }

    // ========================================
    // Property 1: goToMessage Triggers Surrounding Message Fetch
    // ========================================
    
    context("Property 1: goToMessage Triggers Surrounding Message Fetch") {
        
        /**
         * **Property 1: goToMessage Triggers Surrounding Message Fetch**
         * 
         * *For any* valid message ID passed to `goToMessage()`, the ViewModel SHALL call
         * the repository's `getMessage()` followed by `fetchSurroundingMessages()` with that message ID.
         * 
         * **Validates: Requirements 1.1, 1.2**
         */
        test("Property 1: goToMessage calls getMessage with the provided messageId") {
            checkAll(100, Arb.long(1L, 10000L)) { messageId ->
                runTest {
                    val repository = GoToMessageMockRepository()
                    val targetMessage = createMockMessages(1, startId = messageId).first()
                    val surroundingResult = createSurroundingResult(5, messageId, 5)
                    
                    repository.getMessageResult = Result.success(targetMessage)
                    repository.fetchSurroundingMessagesResult = Result.success(surroundingResult)
                    
                    val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                    
                    viewModel.goToMessage(messageId)
                    advanceUntilIdle()
                    
                    repository.getMessageCalled shouldBe true
                    repository.getMessageId shouldBe messageId
                }
            }
        }
        
        test("Property 1: goToMessage calls fetchSurroundingMessages after getMessage succeeds") {
            checkAll(100, Arb.long(1L, 10000L)) { messageId ->
                runTest {
                    val repository = GoToMessageMockRepository()
                    val targetMessage = createMockMessages(1, startId = messageId).first()
                    val surroundingResult = createSurroundingResult(5, messageId, 5)
                    
                    repository.getMessageResult = Result.success(targetMessage)
                    repository.fetchSurroundingMessagesResult = Result.success(surroundingResult)
                    
                    val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                    
                    viewModel.goToMessage(messageId)
                    advanceUntilIdle()
                    
                    repository.fetchSurroundingMessagesCalled shouldBe true
                    repository.fetchSurroundingMessagesId shouldBe messageId
                }
            }
        }
        
        test("Property 1: goToMessage does not call fetchSurroundingMessages when getMessage fails") {
            checkAll(100, Arb.long(1L, 10000L)) { messageId ->
                runTest {
                    val repository = GoToMessageMockRepository()
                    repository.getMessageResult = Result.failure(Exception("Message not found"))
                    
                    val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                    
                    viewModel.goToMessage(messageId)
                    advanceUntilIdle()
                    
                    repository.getMessageCalled shouldBe true
                    repository.fetchSurroundingMessagesCalled shouldBe false
                }
            }
        }
    }

    // ========================================
    // Property 2: Message Window Reverse Chronological Ordering
    // ========================================
    
    context("Property 2: Message Window Reverse Chronological Ordering") {
        
        /**
         * **Property 2: Message Window Reverse Chronological Ordering**
         * 
         * *For any* set of older messages, target message, and newer messages returned by
         * `fetchSurroundingMessages()`, the resulting message list SHALL be ordered as
         * `[newer...] + [target] + [older...]` in reverse chronological order (newest first).
         * 
         * With reverseLayout=true, index 0 is displayed at the bottom (newest messages),
         * and higher indices are displayed at the top (older messages).
         * 
         * **Validates: Requirements 1.4**
         */
        test("Property 2: Message list is ordered reverse chronologically after goToMessage") {
            checkAll(100, Arb.int(0, 20), Arb.long(100L, 1000L), Arb.int(0, 20)) { olderCount, targetId, newerCount ->
                runTest {
                    val repository = GoToMessageMockRepository()
                    val targetMessage = createMockMessages(1, startId = targetId).first()
                    val surroundingResult = createSurroundingResult(olderCount, targetId, newerCount)
                    
                    repository.getMessageResult = Result.success(targetMessage)
                    repository.fetchSurroundingMessagesResult = Result.success(surroundingResult)
                    
                    val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                    
                    viewModel.goToMessage(targetId)
                    advanceUntilIdle()
                    
                    val messages = viewModel.messages.value
                    val expectedSize = olderCount + 1 + newerCount
                    messages.size shouldBe expectedSize
                    
                    // Verify reverse chronological ordering (IDs should be descending)
                    // With reverseLayout=true: index 0 = newest (bottom), last index = oldest (top)
                    if (messages.size > 1) {
                        for (i in 0 until messages.size - 1) {
                            (messages[i].id > messages[i + 1].id) shouldBe true
                        }
                    }
                }
            }
        }
        
        test("Property 2: Target message is at correct position in the list") {
            checkAll(100, Arb.int(1, 10), Arb.long(100L, 1000L), Arb.int(1, 10)) { olderCount, targetId, newerCount ->
                runTest {
                    val repository = GoToMessageMockRepository()
                    val targetMessage = createMockMessages(1, startId = targetId).first()
                    val surroundingResult = createSurroundingResult(olderCount, targetId, newerCount)
                    
                    repository.getMessageResult = Result.success(targetMessage)
                    repository.fetchSurroundingMessagesResult = Result.success(surroundingResult)
                    
                    val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                    
                    viewModel.goToMessage(targetId)
                    advanceUntilIdle()
                    
                    val messages = viewModel.messages.value
                    
                    // With reverse ordering: target message should be at index = newerCount
                    // (newer messages come before target in the list)
                    messages[newerCount].id shouldBe targetId
                }
            }
        }
        
        test("Property 2: Newer messages come before target message") {
            checkAll(100, Arb.int(1, 15), Arb.long(100L, 1000L)) { newerCount, targetId ->
                runTest {
                    val repository = GoToMessageMockRepository()
                    val targetMessage = createMockMessages(1, startId = targetId).first()
                    val surroundingResult = createSurroundingResult(0, targetId, newerCount)
                    
                    repository.getMessageResult = Result.success(targetMessage)
                    repository.fetchSurroundingMessagesResult = Result.success(surroundingResult)
                    
                    val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                    
                    viewModel.goToMessage(targetId)
                    advanceUntilIdle()
                    
                    val messages = viewModel.messages.value
                    
                    // With reverse ordering: newer messages (indices 0 to newerCount-1) should have IDs > targetId
                    for (i in 0 until newerCount) {
                        (messages[i].id > targetId) shouldBe true
                    }
                }
            }
        }
        
        test("Property 2: Older messages come after target message") {
            checkAll(100, Arb.int(1, 15), Arb.long(100L, 1000L)) { olderCount, targetId ->
                runTest {
                    val repository = GoToMessageMockRepository()
                    val targetMessage = createMockMessages(1, startId = targetId).first()
                    val surroundingResult = createSurroundingResult(olderCount, targetId, 0)
                    
                    repository.getMessageResult = Result.success(targetMessage)
                    repository.fetchSurroundingMessagesResult = Result.success(surroundingResult)
                    
                    val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                    
                    viewModel.goToMessage(targetId)
                    advanceUntilIdle()
                    
                    val messages = viewModel.messages.value
                    
                    // With reverse ordering: older messages (indices after target) should have IDs < targetId
                    // Target is at index 0 (no newer messages), older messages start at index 1
                    for (i in 1 until messages.size) {
                        (messages[i].id < targetId) shouldBe true
                    }
                }
            }
        }
    }

    // ========================================
    // Property 3: Message List Replacement After goToMessage
    // ========================================
    
    context("Property 3: Message List Replacement After goToMessage") {
        
        /**
         * **Property 3: Message List Replacement After goToMessage**
         * 
         * *For any* successful `goToMessage()` operation, the message list SHALL equal exactly
         * the combined snapshot from `fetchSurroundingMessages()`, with no messages from the
         * previous list retained.
         * 
         * **Validates: Requirements 1.5**
         */
        test("Property 3: goToMessage replaces existing message list completely") {
            checkAll(100, Arb.int(1, 10), Arb.long(100L, 500L), Arb.int(1, 10)) { olderCount, targetId, newerCount ->
                runTest {
                    val repository = GoToMessageMockRepository()
                    
                    // Set up initial messages
                    val initialMessages = createMockMessages(5, startId = 1000L)
                    repository.fetchPreviousMessagesResult = Result.success(initialMessages)
                    
                    val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                    
                    // Fetch initial messages
                    viewModel.fetchMessages()
                    advanceUntilIdle()
                    
                    viewModel.messages.value.size shouldBe 5
                    
                    // Now set up goToMessage
                    val targetMessage = createMockMessages(1, startId = targetId).first()
                    val surroundingResult = createSurroundingResult(olderCount, targetId, newerCount)
                    
                    repository.getMessageResult = Result.success(targetMessage)
                    repository.fetchSurroundingMessagesResult = Result.success(surroundingResult)
                    
                    // Call goToMessage
                    viewModel.goToMessage(targetId)
                    advanceUntilIdle()
                    
                    val messages = viewModel.messages.value
                    val expectedSize = olderCount + 1 + newerCount
                    
                    // List should be completely replaced
                    messages.size shouldBe expectedSize
                    
                    // No messages from initial list should remain (IDs 1000-1004)
                    messages.none { it.id >= 1000L && it.id <= 1004L } shouldBe true
                }
            }
        }
        
        test("Property 3: Message list equals exactly the combined snapshot") {
            checkAll(100, Arb.int(0, 10), Arb.long(50L, 200L), Arb.int(0, 10)) { olderCount, targetId, newerCount ->
                runTest {
                    val repository = GoToMessageMockRepository()
                    val targetMessage = createMockMessages(1, startId = targetId).first()
                    val surroundingResult = createSurroundingResult(olderCount, targetId, newerCount)
                    
                    repository.getMessageResult = Result.success(targetMessage)
                    repository.fetchSurroundingMessagesResult = Result.success(surroundingResult)
                    
                    val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                    
                    viewModel.goToMessage(targetId)
                    advanceUntilIdle()
                    
                    val messages = viewModel.messages.value
                    
                    // Verify the list contains exactly the expected messages
                    val expectedIds = surroundingResult.olderMessages.map { it.id } +
                            listOf(surroundingResult.targetMessage.id) +
                            surroundingResult.newerMessages.map { it.id }
                    
                    messages.map { it.id } shouldBe expectedIds
                }
            }
        }
    }

    // ========================================
    // Property 4: Pagination State Updates After goToMessage
    // ========================================
    
    context("Property 4: Pagination State Updates After goToMessage") {
        
        /**
         * **Property 4: Pagination State Updates After goToMessage**
         * 
         * *For any* `SurroundingMessagesResult` returned by `fetchSurroundingMessages()`,
         * `hasMorePreviousMessages` SHALL equal `result.hasMorePrevious` AND
         * `hasMoreNewMessages` SHALL equal `result.hasMoreNext`.
         * 
         * **Validates: Requirements 1.6, 1.7**
         */
        test("Property 4: hasMorePreviousMessages equals result.hasMorePrevious") {
            checkAll(100, Arb.long(100L, 1000L), Arb.boolean()) { targetId, hasMorePrevious ->
                runTest {
                    val repository = GoToMessageMockRepository()
                    val targetMessage = createMockMessages(1, startId = targetId).first()
                    val surroundingResult = createSurroundingResult(
                        olderCount = 5,
                        targetId = targetId,
                        newerCount = 5,
                        hasMorePrevious = hasMorePrevious,
                        hasMoreNext = true
                    )
                    
                    repository.getMessageResult = Result.success(targetMessage)
                    repository.fetchSurroundingMessagesResult = Result.success(surroundingResult)
                    
                    val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                    
                    viewModel.goToMessage(targetId)
                    advanceUntilIdle()
                    
                    viewModel.hasMorePreviousMessages.value shouldBe hasMorePrevious
                }
            }
        }
        
        test("Property 4: hasMoreNewMessages equals result.hasMoreNext") {
            checkAll(100, Arb.long(100L, 1000L), Arb.boolean()) { targetId, hasMoreNext ->
                runTest {
                    val repository = GoToMessageMockRepository()
                    val targetMessage = createMockMessages(1, startId = targetId).first()
                    val surroundingResult = createSurroundingResult(
                        olderCount = 5,
                        targetId = targetId,
                        newerCount = 5,
                        hasMorePrevious = true,
                        hasMoreNext = hasMoreNext
                    )
                    
                    repository.getMessageResult = Result.success(targetMessage)
                    repository.fetchSurroundingMessagesResult = Result.success(surroundingResult)
                    
                    val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                    
                    viewModel.goToMessage(targetId)
                    advanceUntilIdle()
                    
                    viewModel.hasMoreNewMessages.value shouldBe hasMoreNext
                }
            }
        }
        
        test("Property 4: Both pagination flags are updated correctly") {
            checkAll(100, Arb.long(100L, 1000L), Arb.boolean(), Arb.boolean()) { targetId, hasMorePrevious, hasMoreNext ->
                runTest {
                    val repository = GoToMessageMockRepository()
                    val targetMessage = createMockMessages(1, startId = targetId).first()
                    val surroundingResult = createSurroundingResult(
                        olderCount = 3,
                        targetId = targetId,
                        newerCount = 3,
                        hasMorePrevious = hasMorePrevious,
                        hasMoreNext = hasMoreNext
                    )
                    
                    repository.getMessageResult = Result.success(targetMessage)
                    repository.fetchSurroundingMessagesResult = Result.success(surroundingResult)
                    
                    val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                    
                    viewModel.goToMessage(targetId)
                    advanceUntilIdle()
                    
                    viewModel.hasMorePreviousMessages.value shouldBe hasMorePrevious
                    viewModel.hasMoreNewMessages.value shouldBe hasMoreNext
                }
            }
        }
    }

    // ========================================
    // Property 5: scrollToMessageId Emission After goToMessage
    // ========================================
    
    context("Property 5: scrollToMessageId Emission After goToMessage") {
        
        /**
         * **Property 5: scrollToMessageId Emission After goToMessage**
         * 
         * *For any* successful `goToMessage(messageId)` operation, `scrollToMessageId`
         * SHALL emit the target `messageId`.
         * 
         * **Validates: Requirements 1.8**
         */
        test("Property 5: scrollToMessageId emits the target messageId on success") {
            checkAll(100, Arb.long(1L, 10000L)) { messageId ->
                runTest {
                    val repository = GoToMessageMockRepository()
                    val targetMessage = createMockMessages(1, startId = messageId).first()
                    val surroundingResult = createSurroundingResult(5, messageId, 5)
                    
                    repository.getMessageResult = Result.success(targetMessage)
                    repository.fetchSurroundingMessagesResult = Result.success(surroundingResult)
                    
                    val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                    
                    // Initial scrollToMessageId should be null
                    viewModel.scrollToMessageId.value shouldBe null
                    
                    viewModel.goToMessage(messageId)
                    advanceUntilIdle()
                    
                    viewModel.scrollToMessageId.value shouldBe messageId
                }
            }
        }
        
        test("Property 5: scrollToMessageId is not emitted on getMessage failure") {
            checkAll(100, Arb.long(1L, 10000L)) { messageId ->
                runTest {
                    val repository = GoToMessageMockRepository()
                    repository.getMessageResult = Result.failure(Exception("Message not found"))
                    
                    val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                    
                    viewModel.goToMessage(messageId)
                    advanceUntilIdle()
                    
                    viewModel.scrollToMessageId.value shouldBe null
                }
            }
        }
        
        test("Property 5: scrollToMessageId is not emitted on fetchSurroundingMessages failure") {
            checkAll(100, Arb.long(1L, 10000L)) { messageId ->
                runTest {
                    val repository = GoToMessageMockRepository()
                    val targetMessage = createMockMessages(1, startId = messageId).first()
                    
                    repository.getMessageResult = Result.success(targetMessage)
                    repository.fetchSurroundingMessagesResult = Result.failure(Exception("Fetch failed"))
                    
                    val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                    
                    viewModel.goToMessage(messageId)
                    advanceUntilIdle()
                    
                    viewModel.scrollToMessageId.value shouldBe null
                }
            }
        }
    }

    // ========================================
    // Property 6: Error State on goToMessage Failure
    // ========================================
    
    context("Property 6: Error State on goToMessage Failure") {
        
        /**
         * **Property 6: Error State on goToMessage Failure**
         * 
         * *For any* `goToMessage()` call where `getMessage()` or `fetchSurroundingMessages()`
         * fails, `uiState` SHALL transition to `MessageListUIState.Error`.
         * 
         * **Validates: Requirements 1.9, 8.3**
         */
        test("Property 6: uiState transitions to Error when getMessage fails") {
            checkAll(100, Arb.long(1L, 10000L)) { messageId ->
                runTest {
                    val repository = GoToMessageMockRepository()
                    val errorMessage = "Message not found: $messageId"
                    repository.getMessageResult = Result.failure(Exception(errorMessage))
                    
                    val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                    
                    viewModel.goToMessage(messageId)
                    advanceUntilIdle()
                    
                    viewModel.uiState.value.shouldBeInstanceOf<MessageListUIState.Error>()
                    val errorState = viewModel.uiState.value as MessageListUIState.Error
                    errorState.exception.message shouldBe errorMessage
                }
            }
        }
        
        test("Property 6: uiState transitions to Error when fetchSurroundingMessages fails") {
            checkAll(100, Arb.long(1L, 10000L)) { messageId ->
                runTest {
                    val repository = GoToMessageMockRepository()
                    val targetMessage = createMockMessages(1, startId = messageId).first()
                    val errorMessage = "Failed to fetch surrounding messages"
                    
                    repository.getMessageResult = Result.success(targetMessage)
                    repository.fetchSurroundingMessagesResult = Result.failure(Exception(errorMessage))
                    
                    val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                    
                    viewModel.goToMessage(messageId)
                    advanceUntilIdle()
                    
                    viewModel.uiState.value.shouldBeInstanceOf<MessageListUIState.Error>()
                    val errorState = viewModel.uiState.value as MessageListUIState.Error
                    errorState.exception.message shouldBe errorMessage
                }
            }
        }
        
        test("Property 6: Message list is not modified on error") {
            checkAll(100, Arb.long(1L, 10000L)) { messageId ->
                runTest {
                    val repository = GoToMessageMockRepository()
                    
                    // Set up initial messages
                    val initialMessages = createMockMessages(5, startId = 1L)
                    repository.fetchPreviousMessagesResult = Result.success(initialMessages)
                    
                    val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                    
                    // Fetch initial messages
                    viewModel.fetchMessages()
                    advanceUntilIdle()
                    
                    val initialMessageIds = viewModel.messages.value.map { it.id }
                    
                    // Now set up goToMessage to fail
                    repository.getMessageResult = Result.failure(Exception("Error"))
                    
                    viewModel.goToMessage(messageId)
                    advanceUntilIdle()
                    
                    // Message list should remain unchanged
                    viewModel.messages.value.map { it.id } shouldBe initialMessageIds
                }
            }
        }
    }

    // ========================================
    // Property 7: Request Rebuilding After goToMessage
    // ========================================
    
    context("Property 7: Request Rebuilding After goToMessage") {
        
        /**
         * **Property 7: Request Rebuilding After goToMessage**
         * 
         * *For any* successful `goToMessage()` operation with a non-empty message list,
         * `rebuildRequestFromMessageId()` SHALL be called with the ID of the oldest message
         * in the resulting list.
         * 
         * **Validates: Requirements 2.1, 2.2, 2.3, 2.4**
         */
        test("Property 7: rebuildRequestFromMessageId is called with oldest message ID") {
            checkAll(100, Arb.int(1, 10), Arb.long(100L, 1000L), Arb.int(0, 10)) { olderCount, targetId, newerCount ->
                runTest {
                    val repository = GoToMessageMockRepository()
                    val targetMessage = createMockMessages(1, startId = targetId).first()
                    val surroundingResult = createSurroundingResult(olderCount, targetId, newerCount)
                    
                    repository.getMessageResult = Result.success(targetMessage)
                    repository.fetchSurroundingMessagesResult = Result.success(surroundingResult)
                    
                    val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                    
                    viewModel.goToMessage(targetId)
                    advanceUntilIdle()
                    
                    repository.rebuildRequestCalled shouldBe true
                    
                    // The oldest message ID should be targetId - olderCount
                    val expectedOldestId = targetId - olderCount
                    repository.rebuildRequestMessageId shouldBe expectedOldestId
                }
            }
        }
        
        test("Property 7: rebuildRequestFromMessageId is called with targetId when no older messages") {
            checkAll(100, Arb.long(100L, 1000L), Arb.int(0, 10)) { targetId, newerCount ->
                runTest {
                    val repository = GoToMessageMockRepository()
                    val targetMessage = createMockMessages(1, startId = targetId).first()
                    val surroundingResult = createSurroundingResult(0, targetId, newerCount)
                    
                    repository.getMessageResult = Result.success(targetMessage)
                    repository.fetchSurroundingMessagesResult = Result.success(surroundingResult)
                    
                    val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                    
                    viewModel.goToMessage(targetId)
                    advanceUntilIdle()
                    
                    repository.rebuildRequestCalled shouldBe true
                    repository.rebuildRequestMessageId shouldBe targetId
                }
            }
        }
        
        test("Property 7: rebuildRequestFromMessageId is not called on failure") {
            checkAll(100, Arb.long(1L, 10000L)) { messageId ->
                runTest {
                    val repository = GoToMessageMockRepository()
                    repository.getMessageResult = Result.failure(Exception("Error"))
                    
                    val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                    
                    viewModel.goToMessage(messageId)
                    advanceUntilIdle()
                    
                    repository.rebuildRequestCalled shouldBe false
                }
            }
        }
    }

    // ========================================
    // Property 15: UI State Transitions During goToMessage
    // ========================================
    
    context("Property 15: UI State Transitions During goToMessage") {
        
        /**
         * **Property 15: UI State Transitions During goToMessage**
         * 
         * *For any* `goToMessage()` call, `uiState` SHALL transition to `Loading` immediately,
         * then to `Loaded` on success or `Error` on failure.
         * 
         * **Validates: Requirements 8.1, 8.2**
         */
        test("Property 15: uiState transitions to Loaded on successful goToMessage") {
            checkAll(100, Arb.int(0, 10), Arb.long(100L, 1000L), Arb.int(0, 10)) { olderCount, targetId, newerCount ->
                runTest {
                    val repository = GoToMessageMockRepository()
                    val targetMessage = createMockMessages(1, startId = targetId).first()
                    val surroundingResult = createSurroundingResult(olderCount, targetId, newerCount)
                    
                    repository.getMessageResult = Result.success(targetMessage)
                    repository.fetchSurroundingMessagesResult = Result.success(surroundingResult)
                    
                    val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                    
                    viewModel.goToMessage(targetId)
                    advanceUntilIdle()
                    
                    // Final state should be Loaded (or Empty if no messages)
                    val expectedState = if (olderCount + 1 + newerCount > 0) {
                        MessageListUIState.Loaded
                    } else {
                        MessageListUIState.Empty
                    }
                    viewModel.uiState.value shouldBe expectedState
                }
            }
        }
        
        test("Property 15: uiState transitions to Error on getMessage failure") {
            checkAll(100, Arb.long(1L, 10000L)) { messageId ->
                runTest {
                    val repository = GoToMessageMockRepository()
                    repository.getMessageResult = Result.failure(Exception("Error"))
                    
                    val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                    
                    viewModel.goToMessage(messageId)
                    advanceUntilIdle()
                    
                    viewModel.uiState.value.shouldBeInstanceOf<MessageListUIState.Error>()
                }
            }
        }
        
        test("Property 15: uiState transitions to Error on fetchSurroundingMessages failure") {
            checkAll(100, Arb.long(1L, 10000L)) { messageId ->
                runTest {
                    val repository = GoToMessageMockRepository()
                    val targetMessage = createMockMessages(1, startId = messageId).first()
                    
                    repository.getMessageResult = Result.success(targetMessage)
                    repository.fetchSurroundingMessagesResult = Result.failure(Exception("Error"))
                    
                    val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                    
                    viewModel.goToMessage(messageId)
                    advanceUntilIdle()
                    
                    viewModel.uiState.value.shouldBeInstanceOf<MessageListUIState.Error>()
                }
            }
        }
        
        test("Property 15: highlightScroll is set to true when highlight parameter is true") {
            checkAll(100, Arb.long(100L, 1000L)) { targetId ->
                runTest {
                    val repository = GoToMessageMockRepository()
                    val targetMessage = createMockMessages(1, startId = targetId).first()
                    val surroundingResult = createSurroundingResult(5, targetId, 5)
                    
                    repository.getMessageResult = Result.success(targetMessage)
                    repository.fetchSurroundingMessagesResult = Result.success(surroundingResult)
                    
                    val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                    
                    // Initial highlightScroll should be false
                    viewModel.highlightScroll.value shouldBe false
                    
                    viewModel.goToMessage(targetId, highlight = true)
                    advanceUntilIdle()
                    
                    viewModel.highlightScroll.value shouldBe true
                }
            }
        }
        
        test("Property 15: highlightScroll is not set when highlight parameter is false") {
            checkAll(100, Arb.long(100L, 1000L)) { targetId ->
                runTest {
                    val repository = GoToMessageMockRepository()
                    val targetMessage = createMockMessages(1, startId = targetId).first()
                    val surroundingResult = createSurroundingResult(5, targetId, 5)
                    
                    repository.getMessageResult = Result.success(targetMessage)
                    repository.fetchSurroundingMessagesResult = Result.success(surroundingResult)
                    
                    val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                    
                    viewModel.goToMessage(targetId, highlight = false)
                    advanceUntilIdle()
                    
                    viewModel.highlightScroll.value shouldBe false
                }
            }
        }
    }

    // ========================================
    // Additional Edge Case Tests
    // ========================================
    
    context("Edge Cases") {
        
        test("goToMessage with empty surrounding messages sets message list to just target") {
            checkAll(100, Arb.long(100L, 1000L)) { targetId ->
                runTest {
                    val repository = GoToMessageMockRepository()
                    val targetMessage = createMockMessages(1, startId = targetId).first()
                    val surroundingResult = createSurroundingResult(0, targetId, 0)
                    
                    repository.getMessageResult = Result.success(targetMessage)
                    repository.fetchSurroundingMessagesResult = Result.success(surroundingResult)
                    
                    val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                    
                    viewModel.goToMessage(targetId)
                    advanceUntilIdle()
                    
                    viewModel.messages.value.size shouldBe 1
                    viewModel.messages.value.first().id shouldBe targetId
                }
            }
        }
        
        test("Multiple goToMessage calls replace list each time") {
            runTest {
                val repository = GoToMessageMockRepository()
                
                // First goToMessage
                val targetId1 = 100L
                val targetMessage1 = createMockMessages(1, startId = targetId1).first()
                val surroundingResult1 = createSurroundingResult(3, targetId1, 3)
                
                repository.getMessageResult = Result.success(targetMessage1)
                repository.fetchSurroundingMessagesResult = Result.success(surroundingResult1)
                
                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                
                viewModel.goToMessage(targetId1)
                advanceUntilIdle()
                
                viewModel.messages.value.size shouldBe 7
                viewModel.messages.value.any { it.id == targetId1 } shouldBe true
                
                // Second goToMessage
                val targetId2 = 500L
                val targetMessage2 = createMockMessages(1, startId = targetId2).first()
                val surroundingResult2 = createSurroundingResult(2, targetId2, 2)
                
                repository.getMessageResult = Result.success(targetMessage2)
                repository.fetchSurroundingMessagesResult = Result.success(surroundingResult2)
                repository.reset()
                
                viewModel.goToMessage(targetId2)
                advanceUntilIdle()
                
                viewModel.messages.value.size shouldBe 5
                viewModel.messages.value.any { it.id == targetId2 } shouldBe true
                // First target should no longer be in list
                viewModel.messages.value.any { it.id == targetId1 } shouldBe false
            }
        }
        
        test("clearScrollToMessage resets scrollToMessageId to null") {
            runTest {
                val repository = GoToMessageMockRepository()
                val targetId = 100L
                val targetMessage = createMockMessages(1, startId = targetId).first()
                val surroundingResult = createSurroundingResult(5, targetId, 5)
                
                repository.getMessageResult = Result.success(targetMessage)
                repository.fetchSurroundingMessagesResult = Result.success(surroundingResult)
                
                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                
                viewModel.goToMessage(targetId)
                advanceUntilIdle()
                
                viewModel.scrollToMessageId.value shouldBe targetId
                
                viewModel.clearScrollToMessage()
                
                viewModel.scrollToMessageId.value shouldBe null
            }
        }
        
        test("clearHighlightScroll resets highlightScroll to false") {
            runTest {
                val repository = GoToMessageMockRepository()
                val targetId = 100L
                val targetMessage = createMockMessages(1, startId = targetId).first()
                val surroundingResult = createSurroundingResult(5, targetId, 5)
                
                repository.getMessageResult = Result.success(targetMessage)
                repository.fetchSurroundingMessagesResult = Result.success(surroundingResult)
                
                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                
                viewModel.goToMessage(targetId, highlight = true)
                advanceUntilIdle()
                
                viewModel.highlightScroll.value shouldBe true
                
                viewModel.clearHighlightScroll()
                
                viewModel.highlightScroll.value shouldBe false
            }
        }
    }
})
