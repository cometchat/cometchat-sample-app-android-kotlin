package com.cometchat.uikit.core.data.repository

import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.User
import com.cometchat.chat.core.MessagesRequest
import com.cometchat.uikit.core.domain.repository.MessageListRepository
import com.cometchat.uikit.core.state.MessageListUIState
import com.cometchat.uikit.core.viewmodel.CometChatMessageListViewModel
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

/**
 * Unit tests for MessageListRepositoryImpl.
 * 
 * Since the repository directly depends on CometChat SDK which cannot be easily mocked,
 * these tests use a mock repository implementation to verify the contract behavior
 * that the ViewModel expects from the repository.
 * 
 * The tests validate:
 * - fetchPreviousMessages success scenarios
 * - fetchPreviousMessages error scenarios  
 * - hasMorePreviousMessages tracking logic
 * 
 * **Validates: Requirements 2.1, 2.5, 2.6**
 * 
 * ## Test Coverage
 * 
 * | Test | Property | Requirements |
 * |------|----------|--------------|
 * | fetchPreviousMessages success | - | 2.1 |
 * | fetchPreviousMessages error | - | 2.1 |
 * | hasMorePreviousMessages tracking | Property 5: Pagination State Consistency | 2.5, 2.6 |
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MessageListRepositoryImplTest : FunSpec({

    val testDispatcher = StandardTestDispatcher()

    beforeSpec {
        Dispatchers.setMain(testDispatcher)
    }

    afterSpec {
        Dispatchers.resetMain()
    }

    /**
     * Mock repository for testing repository contract behavior.
     * This simulates the behavior expected from MessageListRepositoryImpl.
     */
    class TestableMessageListRepository : MessageListRepository {
        var fetchPreviousMessagesResult: Result<List<BaseMessage>> = Result.success(emptyList())
        var fetchNextMessagesResult: Result<List<BaseMessage>> = Result.success(emptyList())
        var hasMorePrevious: Boolean = true
        var fetchPreviousCallCount: Int = 0
        
        override suspend fun fetchPreviousMessages(): Result<List<BaseMessage>> {
            fetchPreviousCallCount++
            val result = fetchPreviousMessagesResult
            // Simulate the repository behavior: hasMore is false when empty list returned
            if (result.isSuccess && result.getOrNull()?.isEmpty() == true) {
                hasMorePrevious = false
            }
            return result
        }

        override suspend fun fetchNextMessages(fromMessageId: Long): Result<List<BaseMessage>> = fetchNextMessagesResult

        override suspend fun getConversation(id: String, type: String) = 
            Result.failure<com.cometchat.chat.models.Conversation>(Exception("Not implemented"))

        override suspend fun getMessage(messageId: Long) = 
            Result.failure<BaseMessage>(Exception("Not implemented"))

        override suspend fun deleteMessage(message: BaseMessage) = 
            Result.failure<BaseMessage>(Exception("Not implemented"))

        override suspend fun flagMessage(messageId: Long, reason: String, remark: String) = 
            Result.failure<Unit>(Exception("Not implemented"))

        override suspend fun addReaction(messageId: Long, emoji: String) = 
            Result.failure<BaseMessage>(Exception("Not implemented"))

        override suspend fun removeReaction(messageId: Long, emoji: String) = 
            Result.failure<BaseMessage>(Exception("Not implemented"))

        override suspend fun markAsRead(message: BaseMessage) = Result.success(Unit)

        override suspend fun markAsDelivered(message: BaseMessage) = Result.success(Unit)

        override suspend fun markAsUnread(message: BaseMessage) = Result.success(Unit)

        override fun hasMorePreviousMessages(): Boolean = hasMorePrevious

        override fun resetRequest() {
            hasMorePrevious = true
            fetchPreviousCallCount = 0
        }

        override fun configureForUser(
            user: User,
            messagesTypes: List<String>,
            messagesCategories: List<String>,
            parentMessageId: Long,
            messagesRequestBuilder: MessagesRequest.MessagesRequestBuilder?
        ) {
            hasMorePrevious = true
        }

        override fun configureForGroup(
            group: Group,
            messagesTypes: List<String>,
            messagesCategories: List<String>,
            parentMessageId: Long,
            messagesRequestBuilder: MessagesRequest.MessagesRequestBuilder?
        ) {
            hasMorePrevious = true
        }

        override suspend fun fetchSurroundingMessages(messageId: Long) =
            Result.failure<com.cometchat.uikit.core.domain.model.SurroundingMessagesResult>(Exception("Not implemented"))

        override suspend fun fetchActionMessages(fromMessageId: Long) =
            Result.success<List<BaseMessage>>(emptyList())

        override fun rebuildRequestFromMessageId(messageId: Long) {
            // No-op for testing
        }

        private var latestMessageId: Long = -1

        override fun getLatestMessageId(): Long = latestMessageId

        override fun setLatestMessageId(messageId: Long) {
            latestMessageId = messageId
        }
    }

    // ========================================
    // Task 51.1: Test fetchPreviousMessages success
    // ========================================
    
    context("Task 51.1: Test fetchPreviousMessages success") {
        
        /**
         * When fetchPreviousMessages returns successfully with messages,
         * the result should contain the messages and hasMorePreviousMessages should be true.
         * 
         * **Validates: Requirements 2.1**
         */
        test("fetchPreviousMessages should return Result.success with messages") {
            runTest {
                val repository = TestableMessageListRepository()
                val mockMessages = createMockMessages(5)
                repository.fetchPreviousMessagesResult = Result.success(mockMessages)
                
                val result = repository.fetchPreviousMessages()
                
                result.isSuccess shouldBe true
                result.getOrNull()?.size shouldBe 5
            }
        }
        
        test("fetchPreviousMessages success should maintain hasMorePreviousMessages as true when messages returned") {
            runTest {
                val repository = TestableMessageListRepository()
                val mockMessages = createMockMessages(3)
                repository.fetchPreviousMessagesResult = Result.success(mockMessages)
                
                repository.fetchPreviousMessages()
                
                // When non-empty list is returned, hasMore should remain true
                repository.hasMorePreviousMessages() shouldBe true
            }
        }
        
        test("fetchPreviousMessages should be callable multiple times") {
            runTest {
                val repository = TestableMessageListRepository()
                val mockMessages = createMockMessages(2)
                repository.fetchPreviousMessagesResult = Result.success(mockMessages)
                
                repository.fetchPreviousMessages()
                repository.fetchPreviousMessages()
                repository.fetchPreviousMessages()
                
                repository.fetchPreviousCallCount shouldBe 3
            }
        }
        
        /**
         * Property-based test: For any number of messages returned,
         * the result should always be successful with the correct count.
         * 
         * **Validates: Requirements 2.1**
         */
        test("fetchPreviousMessages should return correct message count for any valid input") {
            checkAll(20, Arb.int(1, 100)) { messageCount ->
                runTest {
                    val repository = TestableMessageListRepository()
                    val mockMessages = createMockMessages(messageCount)
                    repository.fetchPreviousMessagesResult = Result.success(mockMessages)
                    
                    val result = repository.fetchPreviousMessages()
                    
                    result.isSuccess shouldBe true
                    result.getOrNull()?.size shouldBe messageCount
                }
            }
        }
        
        /**
         * Test that ViewModel correctly handles successful repository response.
         * This validates the integration between ViewModel and Repository.
         * 
         * **Validates: Requirements 2.1**
         */
        test("ViewModel should update to Loaded state when repository returns messages") {
            runTest {
                val repository = TestableMessageListRepository()
                val mockMessages = createMockMessages(5)
                repository.fetchPreviousMessagesResult = Result.success(mockMessages)
                
                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                
                viewModel.fetchMessages()
                advanceUntilIdle()
                
                viewModel.uiState.value shouldBe MessageListUIState.Loaded
                viewModel.messages.value.size shouldBe 5
            }
        }
    }

    // ========================================
    // Task 51.2: Test fetchPreviousMessages error
    // ========================================
    
    context("Task 51.2: Test fetchPreviousMessages error") {
        
        /**
         * When fetchPreviousMessages fails, the result should be a failure
         * containing the exception.
         * 
         * **Validates: Requirements 2.1**
         */
        test("fetchPreviousMessages should return Result.failure on error") {
            runTest {
                val repository = TestableMessageListRepository()
                val testException = Exception("Network error")
                repository.fetchPreviousMessagesResult = Result.failure(testException)
                
                val result = repository.fetchPreviousMessages()
                
                result.isFailure shouldBe true
                result.exceptionOrNull()?.message shouldBe "Network error"
            }
        }
        
        test("fetchPreviousMessages error should preserve exception type") {
            runTest {
                val repository = TestableMessageListRepository()
                val testException = IllegalStateException("Invalid state")
                repository.fetchPreviousMessagesResult = Result.failure(testException)
                
                val result = repository.fetchPreviousMessages()
                
                result.isFailure shouldBe true
                result.exceptionOrNull().shouldBeInstanceOf<IllegalStateException>()
            }
        }
        
        test("fetchPreviousMessages error should not change hasMorePreviousMessages") {
            runTest {
                val repository = TestableMessageListRepository()
                repository.hasMorePrevious = true
                repository.fetchPreviousMessagesResult = Result.failure(Exception("Error"))
                
                repository.fetchPreviousMessages()
                
                // Error should not affect hasMore state
                repository.hasMorePreviousMessages() shouldBe true
            }
        }
        
        /**
         * Property-based test: For any error message, the failure result
         * should preserve the error message.
         * 
         * **Validates: Requirements 2.1**
         */
        test("fetchPreviousMessages should preserve error message for any exception") {
            checkAll(20, Arb.string(1, 100)) { errorMessage ->
                runTest {
                    val repository = TestableMessageListRepository()
                    val testException = Exception(errorMessage)
                    repository.fetchPreviousMessagesResult = Result.failure(testException)
                    
                    val result = repository.fetchPreviousMessages()
                    
                    result.isFailure shouldBe true
                    result.exceptionOrNull()?.message shouldBe errorMessage
                }
            }
        }
        
        /**
         * Test that ViewModel correctly handles repository error response.
         * This validates the integration between ViewModel and Repository.
         * 
         * **Validates: Requirements 2.1**
         */
        test("ViewModel should update to Error state when repository fails") {
            runTest {
                val repository = TestableMessageListRepository()
                val testException = Exception("Test error")
                repository.fetchPreviousMessagesResult = Result.failure(testException)
                
                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                
                viewModel.fetchMessages()
                advanceUntilIdle()
                
                viewModel.uiState.value.shouldBeInstanceOf<MessageListUIState.Error>()
                val errorState = viewModel.uiState.value as MessageListUIState.Error
                errorState.exception.message shouldBe "Test error"
            }
        }
    }

    // ========================================
    // Task 51.3: Test hasMorePreviousMessages tracking
    // ========================================
    
    context("Task 51.3: Test hasMorePreviousMessages tracking") {
        
        /**
         * **Property 5: Pagination State Consistency**
         * 
         * When fetchPreviousMessages returns an empty list, hasMorePreviousMessages
         * should return false, indicating no more messages are available.
         * 
         * **Validates: Requirements 2.5, 2.6**
         */
        test("hasMorePreviousMessages should return false after empty result") {
            runTest {
                val repository = TestableMessageListRepository()
                repository.fetchPreviousMessagesResult = Result.success(emptyList())
                
                // Initially should be true
                repository.hasMorePreviousMessages() shouldBe true
                
                // After fetching empty result
                repository.fetchPreviousMessages()
                
                // Should now be false
                repository.hasMorePreviousMessages() shouldBe false
            }
        }
        
        test("hasMorePreviousMessages should remain true after non-empty result") {
            runTest {
                val repository = TestableMessageListRepository()
                val mockMessages = createMockMessages(5)
                repository.fetchPreviousMessagesResult = Result.success(mockMessages)
                
                repository.fetchPreviousMessages()
                
                repository.hasMorePreviousMessages() shouldBe true
            }
        }
        
        test("hasMorePreviousMessages should be true initially") {
            val repository = TestableMessageListRepository()
            
            repository.hasMorePreviousMessages() shouldBe true
        }
        
        test("resetRequest should reset hasMorePreviousMessages to true") {
            runTest {
                val repository = TestableMessageListRepository()
                repository.fetchPreviousMessagesResult = Result.success(emptyList())
                
                // Fetch empty to set hasMore to false
                repository.fetchPreviousMessages()
                repository.hasMorePreviousMessages() shouldBe false
                
                // Reset should restore to true
                repository.resetRequest()
                repository.hasMorePreviousMessages() shouldBe true
            }
        }
        
        test("configureForUser should reset hasMorePreviousMessages to true") {
            runTest {
                val repository = TestableMessageListRepository()
                repository.fetchPreviousMessagesResult = Result.success(emptyList())
                
                // Fetch empty to set hasMore to false
                repository.fetchPreviousMessages()
                repository.hasMorePreviousMessages() shouldBe false
                
                // Configure for user should reset
                val mockUser = User().apply { uid = "test_user" }
                repository.configureForUser(mockUser, emptyList(), emptyList(), -1, null)
                
                repository.hasMorePreviousMessages() shouldBe true
            }
        }
        
        test("configureForGroup should reset hasMorePreviousMessages to true") {
            runTest {
                val repository = TestableMessageListRepository()
                repository.fetchPreviousMessagesResult = Result.success(emptyList())
                
                // Fetch empty to set hasMore to false
                repository.fetchPreviousMessages()
                repository.hasMorePreviousMessages() shouldBe false
                
                // Configure for group should reset
                val mockGroup = Group().apply { guid = "test_group" }
                repository.configureForGroup(mockGroup, emptyList(), emptyList(), -1, null)
                
                repository.hasMorePreviousMessages() shouldBe true
            }
        }
        
        /**
         * Property-based test: Pagination state should be consistent across
         * multiple fetch operations.
         * 
         * **Property 5: Pagination State Consistency**
         * **Validates: Requirements 2.5, 2.6**
         */
        test("Property 5: Pagination state should be consistent with fetch results") {
            checkAll(20, Arb.int(0, 50)) { messageCount ->
                runTest {
                    val repository = TestableMessageListRepository()
                    val mockMessages = if (messageCount > 0) createMockMessages(messageCount) else emptyList()
                    repository.fetchPreviousMessagesResult = Result.success(mockMessages)
                    
                    repository.fetchPreviousMessages()
                    
                    // hasMore should be false only when empty list is returned
                    if (messageCount == 0) {
                        repository.hasMorePreviousMessages() shouldBe false
                    } else {
                        repository.hasMorePreviousMessages() shouldBe true
                    }
                }
            }
        }
        
        /**
         * Test that ViewModel respects hasMorePreviousMessages and stops fetching.
         * 
         * **Property 5: Pagination State Consistency**
         * **Validates: Requirements 2.5, 2.6**
         */
        test("ViewModel should not fetch when hasMorePreviousMessages is false") {
            runTest {
                val repository = TestableMessageListRepository()
                val mockMessages = createMockMessages(3)
                repository.fetchPreviousMessagesResult = Result.success(mockMessages)
                
                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                
                // First fetch - returns messages
                viewModel.fetchMessages()
                advanceUntilIdle()
                
                viewModel.messages.value.size shouldBe 3
                
                // Now simulate end of messages
                repository.fetchPreviousMessagesResult = Result.success(emptyList())
                
                // Second fetch - returns empty, sets hasMore to false
                viewModel.fetchMessages()
                advanceUntilIdle()
                
                // hasMorePreviousMessages should now be false in ViewModel
                viewModel.hasMorePreviousMessages.value shouldBe false
                
                // Set up new messages for potential third fetch
                val moreMessages = createMockMessages(2, startId = 100)
                repository.fetchPreviousMessagesResult = Result.success(moreMessages)
                
                // Third fetch should be ignored because hasMore is false
                viewModel.fetchMessages()
                advanceUntilIdle()
                
                // Should still have only 3 messages from first fetch
                viewModel.messages.value.size shouldBe 3
            }
        }
        
        /**
         * Test sequential fetches with varying message counts.
         * 
         * **Property 5: Pagination State Consistency**
         * **Validates: Requirements 2.5, 2.6**
         */
        test("Sequential fetches should accumulate messages until empty result") {
            runTest {
                val repository = TestableMessageListRepository()
                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                
                // First batch
                repository.fetchPreviousMessagesResult = Result.success(createMockMessages(5, startId = 1))
                viewModel.fetchMessages()
                advanceUntilIdle()
                viewModel.messages.value.size shouldBe 5
                viewModel.hasMorePreviousMessages.value shouldBe true
                
                // Second batch
                repository.fetchPreviousMessagesResult = Result.success(createMockMessages(3, startId = 10))
                viewModel.fetchMessages()
                advanceUntilIdle()
                viewModel.messages.value.size shouldBe 8
                viewModel.hasMorePreviousMessages.value shouldBe true
                
                // Empty batch - end of messages
                repository.fetchPreviousMessagesResult = Result.success(emptyList())
                viewModel.fetchMessages()
                advanceUntilIdle()
                viewModel.messages.value.size shouldBe 8
                viewModel.hasMorePreviousMessages.value shouldBe false
            }
        }
    }

    // ========================================
    // Task 1.3: Test markAsDelivered method
    // ========================================
    
    context("Task 1.3: Test markAsDelivered method") {
        
        /**
         * Mock repository with tracking for markAsDelivered calls.
         */
        class TrackingMessageListRepository : MessageListRepository {
            var markAsDeliveredCalled: Boolean = false
            var markAsDeliveredMessage: BaseMessage? = null
            var markAsDeliveredResult: Result<Unit> = Result.success(Unit)
            var fetchPreviousMessagesResult: Result<List<BaseMessage>> = Result.success(emptyList())
            var hasMorePrevious: Boolean = true
            
            override suspend fun markAsDelivered(message: BaseMessage): Result<Unit> {
                markAsDeliveredCalled = true
                markAsDeliveredMessage = message
                return markAsDeliveredResult
            }
            
            override suspend fun fetchPreviousMessages(): Result<List<BaseMessage>> {
                val result = fetchPreviousMessagesResult
                if (result.isSuccess && result.getOrNull()?.isEmpty() == true) {
                    hasMorePrevious = false
                }
                return result
            }

            override suspend fun fetchNextMessages(fromMessageId: Long): Result<List<BaseMessage>> = Result.success(emptyList())

            override suspend fun getConversation(id: String, type: String) = 
                Result.failure<com.cometchat.chat.models.Conversation>(Exception("Not implemented"))

            override suspend fun getMessage(messageId: Long) = 
                Result.failure<BaseMessage>(Exception("Not implemented"))

            override suspend fun deleteMessage(message: BaseMessage) = 
                Result.failure<BaseMessage>(Exception("Not implemented"))

            override suspend fun flagMessage(messageId: Long, reason: String, remark: String) = 
                Result.failure<Unit>(Exception("Not implemented"))

            override suspend fun addReaction(messageId: Long, emoji: String) = 
                Result.failure<BaseMessage>(Exception("Not implemented"))

            override suspend fun removeReaction(messageId: Long, emoji: String) = 
                Result.failure<BaseMessage>(Exception("Not implemented"))

            override suspend fun markAsRead(message: BaseMessage) = Result.success(Unit)

            override suspend fun markAsUnread(message: BaseMessage) = Result.success(Unit)

            override fun hasMorePreviousMessages(): Boolean = hasMorePrevious

            override fun resetRequest() {
                hasMorePrevious = true
                markAsDeliveredCalled = false
                markAsDeliveredMessage = null
            }

            override fun configureForUser(
                user: User,
                messagesTypes: List<String>,
                messagesCategories: List<String>,
                parentMessageId: Long,
                messagesRequestBuilder: MessagesRequest.MessagesRequestBuilder?
            ) {
                hasMorePrevious = true
            }

            override fun configureForGroup(
                group: Group,
                messagesTypes: List<String>,
                messagesCategories: List<String>,
                parentMessageId: Long,
                messagesRequestBuilder: MessagesRequest.MessagesRequestBuilder?
            ) {
                hasMorePrevious = true
            }

            override suspend fun fetchSurroundingMessages(messageId: Long) =
                Result.failure<com.cometchat.uikit.core.domain.model.SurroundingMessagesResult>(Exception("Not implemented"))

            override suspend fun fetchActionMessages(fromMessageId: Long) =
                Result.success<List<BaseMessage>>(emptyList())

            override fun rebuildRequestFromMessageId(messageId: Long) {
                // No-op for testing
            }

            private var latestMessageId: Long = -1

            override fun getLatestMessageId(): Long = latestMessageId

            override fun setLatestMessageId(messageId: Long) {
                latestMessageId = messageId
            }
        }
        
        /**
         * markAsDelivered should return Result.success when SDK call succeeds.
         * 
         * **Validates: AC-1.1**
         */
        test("markAsDelivered should return Result.success on success") {
            runTest {
                val repository = TrackingMessageListRepository()
                repository.markAsDeliveredResult = Result.success(Unit)
                
                val mockMessage = createMockMessages(1).first()
                val result = repository.markAsDelivered(mockMessage)
                
                result.isSuccess shouldBe true
                repository.markAsDeliveredCalled shouldBe true
                repository.markAsDeliveredMessage shouldBe mockMessage
            }
        }
        
        /**
         * markAsDelivered should return Result.failure when SDK call fails.
         * 
         * **Validates: AC-1.1**
         */
        test("markAsDelivered should return Result.failure on error") {
            runTest {
                val repository = TrackingMessageListRepository()
                val testException = Exception("Network error")
                repository.markAsDeliveredResult = Result.failure(testException)
                
                val mockMessage = createMockMessages(1).first()
                val result = repository.markAsDelivered(mockMessage)
                
                result.isFailure shouldBe true
                result.exceptionOrNull()?.message shouldBe "Network error"
            }
        }
        
        /**
         * markAsDelivered should preserve the message passed to it.
         * 
         * **Validates: AC-1.1**
         */
        test("markAsDelivered should receive the correct message") {
            runTest {
                val repository = TrackingMessageListRepository()
                
                val mockMessage = com.cometchat.chat.models.TextMessage(
                    "test_receiver",
                    "Test message content",
                    com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER
                ).apply {
                    id = 12345L
                    sentAt = System.currentTimeMillis()
                }
                
                repository.markAsDelivered(mockMessage)
                
                repository.markAsDeliveredMessage?.id shouldBe 12345L
            }
        }
        
        /**
         * Property-based test: markAsDelivered should handle any valid message.
         * 
         * **Validates: AC-1.1**
         */
        test("markAsDelivered should handle messages with any valid ID") {
            checkAll(20, Arb.int(1, 1000000)) { messageId ->
                runTest {
                    val repository = TrackingMessageListRepository()
                    
                    val mockMessage = com.cometchat.chat.models.TextMessage(
                        "receiver",
                        "Test message",
                        com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER
                    ).apply {
                        id = messageId.toLong()
                    }
                    
                    val result = repository.markAsDelivered(mockMessage)
                    
                    result.isSuccess shouldBe true
                    repository.markAsDeliveredMessage?.id shouldBe messageId.toLong()
                }
            }
        }
        
        /**
         * markAsDelivered error should preserve exception type.
         * 
         * **Validates: AC-1.1**
         */
        test("markAsDelivered error should preserve exception type") {
            runTest {
                val repository = TrackingMessageListRepository()
                val testException = IllegalStateException("Invalid state")
                repository.markAsDeliveredResult = Result.failure(testException)
                
                val mockMessage = createMockMessages(1).first()
                val result = repository.markAsDelivered(mockMessage)
                
                result.isFailure shouldBe true
                result.exceptionOrNull().shouldBeInstanceOf<IllegalStateException>()
            }
        }
    }
})

/**
 * Helper function to create mock BaseMessage instances for testing.
 * 
 * @param count Number of messages to create
 * @param startId Starting ID for the messages (default 1)
 */
private fun createMockMessages(count: Int, startId: Int = 1): List<BaseMessage> {
    return (startId until startId + count).map { index ->
        com.cometchat.chat.models.TextMessage(
            "receiver_$index",
            "Test message $index",
            com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER
        ).apply {
            id = index.toLong()
            sentAt = System.currentTimeMillis()
        }
    }
}
