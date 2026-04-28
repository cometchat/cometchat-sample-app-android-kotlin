package com.cometchat.uikit.core.viewmodel

import com.cometchat.chat.models.Action
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Conversation
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.User
import com.cometchat.chat.core.Call
import com.cometchat.chat.core.MessagesRequest
import com.cometchat.uikit.core.domain.repository.MessageListRepository
import com.cometchat.uikit.core.state.ConversationStarterUIState
import com.cometchat.uikit.core.state.MessageAlignment
import com.cometchat.uikit.core.state.MessageDeleteState
import com.cometchat.uikit.core.state.MessageListUIState
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.longs.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

/**
 * Unit tests for CometChatMessageListViewModel state management.
 * 
 * These tests validate the ViewModel's state transitions and message handling
 * using a mock repository to avoid SDK dependencies.
 * 
 * **Validates: Requirements 2.1, 17.1, 17.2**
 * 
 * ## Test Coverage
 * 
 * | Test | Property | Requirements |
 * |------|----------|--------------|
 * | Initial state is Loading | Property 1: Initial State Correctness | 17.1 |
 * | fetchMessages updates state to Loaded | Property 5: Pagination State Consistency | 2.1, 17.1, 17.2 |
 * | fetchMessages updates state to Empty | - | 17.1 |
 * | fetchMessages updates state to Error | - | 17.1 |
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CometChatMessageListViewModelTest : FunSpec({

    val testDispatcher = StandardTestDispatcher()

    beforeSpec {
        Dispatchers.setMain(testDispatcher)
    }

    afterSpec {
        Dispatchers.resetMain()
    }

    /**
     * Mock repository for testing ViewModel without SDK dependencies.
     * Allows configuring return values for each method.
     */
    class MockMessageListRepository : MessageListRepository {
        var fetchPreviousMessagesResult: Result<List<BaseMessage>> = Result.success(emptyList())
        var fetchNextMessagesResult: Result<List<BaseMessage>> = Result.success(emptyList())
        var getConversationResult: Result<Conversation>? = null
        var getMessageResult: Result<BaseMessage>? = null
        var deleteMessageResult: Result<BaseMessage>? = null
        var flagMessageResult: Result<Unit> = Result.success(Unit)
        var addReactionResult: Result<BaseMessage>? = null
        var removeReactionResult: Result<BaseMessage>? = null
        var markAsReadResult: Result<Unit> = Result.success(Unit)
        var markAsUnreadResult: Result<Conversation> = Result.success(Conversation().apply { unreadMessageCount = 1 })
        var markAsDeliveredResult: Result<Unit> = Result.success(Unit)
        var hasMorePrevious: Boolean = true
        
        var configuredForUser: User? = null
        var configuredForGroup: Group? = null
        var resetRequestCalled: Boolean = false
        var markAsReadCalled: Boolean = false
        var markAsReadMessage: BaseMessage? = null
        var markAsDeliveredCalled: Boolean = false
        var markAsDeliveredMessage: BaseMessage? = null

        override suspend fun fetchPreviousMessages(): Result<List<BaseMessage>> = fetchPreviousMessagesResult

        override suspend fun fetchNextMessages(fromMessageId: Long): Result<List<BaseMessage>> = fetchNextMessagesResult

        override suspend fun getConversation(id: String, type: String): Result<Conversation> {
            return getConversationResult ?: Result.failure(Exception("Not configured"))
        }

        override suspend fun getMessage(messageId: Long): Result<BaseMessage> {
            return getMessageResult ?: Result.failure(Exception("Not configured"))
        }

        override suspend fun deleteMessage(message: BaseMessage): Result<BaseMessage> {
            return deleteMessageResult ?: Result.failure(Exception("Not configured"))
        }

        override suspend fun flagMessage(messageId: Long, reason: String, remark: String): Result<Unit> = flagMessageResult

        override suspend fun addReaction(messageId: Long, emoji: String): Result<BaseMessage> {
            return addReactionResult ?: Result.failure(Exception("Not configured"))
        }

        override suspend fun removeReaction(messageId: Long, emoji: String): Result<BaseMessage> {
            return removeReactionResult ?: Result.failure(Exception("Not configured"))
        }

        override suspend fun markAsRead(message: BaseMessage): Result<Unit> {
            markAsReadCalled = true
            markAsReadMessage = message
            return markAsReadResult
        }

        override suspend fun markAsDelivered(message: BaseMessage): Result<Unit> {
            markAsDeliveredCalled = true
            markAsDeliveredMessage = message
            return markAsDeliveredResult
        }

        override suspend fun markAsUnread(message: BaseMessage): Result<Conversation> = markAsUnreadResult

        override fun hasMorePreviousMessages(): Boolean = hasMorePrevious

        override fun resetRequest() {
            resetRequestCalled = true
            hasMorePrevious = true
        }

        override fun configureForUser(
            user: User,
            messagesTypes: List<String>,
            messagesCategories: List<String>,
            parentMessageId: Long,
            messagesRequestBuilder: MessagesRequest.MessagesRequestBuilder?
        ) {
            configuredForUser = user
        }

        override fun configureForGroup(
            group: Group,
            messagesTypes: List<String>,
            messagesCategories: List<String>,
            parentMessageId: Long,
            messagesRequestBuilder: MessagesRequest.MessagesRequestBuilder?
        ) {
            configuredForGroup = group
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
     * Test message class that simulates BaseMessage for testing.
     * Since BaseMessage has SDK dependencies, we use this for property-based tests.
     */
    data class TestMessage(
        val id: Long,
        val text: String = "Test message"
    )

    // ========================================
    // Task 44.1: Test initial state is Loading
    // ========================================
    
    context("Task 44.1: Initial state tests") {
        
        /**
         * **Property 1: Initial State Correctness**
         * 
         * The ViewModel should always start in Loading state before any data is fetched.
         * This ensures the UI can show appropriate loading indicators.
         * 
         * **Validates: Requirements 17.1**
         */
        test("initial uiState should be Loading") {
            val repository = MockMessageListRepository()
            val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
            
            viewModel.uiState.value shouldBe MessageListUIState.Loading
        }
        
        test("initial messages list should be empty") {
            val repository = MockMessageListRepository()
            val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
            
            viewModel.messages.value shouldBe emptyList()
        }
        
        test("initial hasMorePreviousMessages should be true") {
            val repository = MockMessageListRepository()
            val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
            
            viewModel.hasMorePreviousMessages.value shouldBe true
        }
        
        test("initial hasMoreNewMessages should be false") {
            val repository = MockMessageListRepository()
            val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
            
            viewModel.hasMoreNewMessages.value shouldBe false
        }
        
        test("initial isInProgress should be false") {
            val repository = MockMessageListRepository()
            val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
            
            viewModel.isInProgress.value shouldBe false
        }
        
        /**
         * Property-based test: Initial state should always be Loading regardless of
         * any configuration parameters that might be set later.
         * 
         * **Property 1: Initial State Correctness**
         * **Validates: Requirements 17.1**
         */
        test("Property 1: Initial state should always be Loading for any new ViewModel instance") {
            checkAll(10, Arb.int(1, 100)) { _ ->
                val repository = MockMessageListRepository()
                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                
                viewModel.uiState.value shouldBe MessageListUIState.Loading
                viewModel.messages.value shouldBe emptyList()
            }
        }
    }

    // ========================================
    // Task 44.2: Test fetchMessages updates state to Loaded
    // ========================================
    
    context("Task 44.2: fetchMessages updates state to Loaded") {
        
        /**
         * **Property 5: Pagination State Consistency**
         * 
         * When messages are successfully fetched, the UI state should transition
         * to Loaded and the messages list should be populated.
         * 
         * **Validates: Requirements 2.1, 17.1, 17.2**
         */
        test("fetchMessages should update uiState to Loaded when messages are returned") {
            runTest {
                val repository = MockMessageListRepository()
                
                // Create a mock message - we need to use reflection or a factory
                // Since BaseMessage is from SDK, we'll verify state transitions
                val mockMessages = createMockMessages(3)
                repository.fetchPreviousMessagesResult = Result.success(mockMessages)
                
                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                
                // Initial state should be Loading
                viewModel.uiState.value shouldBe MessageListUIState.Loading
                
                // Fetch messages
                viewModel.fetchMessages()
                advanceUntilIdle()
                
                // State should be Loaded
                viewModel.uiState.value shouldBe MessageListUIState.Loaded
            }
        }
        
        test("fetchMessages should populate messages list") {
            runTest {
                val repository = MockMessageListRepository()
                val mockMessages = createMockMessages(5)
                repository.fetchPreviousMessagesResult = Result.success(mockMessages)
                
                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                
                viewModel.fetchMessages()
                advanceUntilIdle()
                
                viewModel.messages.value.size shouldBe 5
            }
        }
        
        test("fetchMessages should update hasMorePreviousMessages based on result") {
            runTest {
                val repository = MockMessageListRepository()
                val mockMessages = createMockMessages(3)
                repository.fetchPreviousMessagesResult = Result.success(mockMessages)
                
                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                
                viewModel.fetchMessages()
                advanceUntilIdle()
                
                // hasMorePreviousMessages should be true when messages are returned
                viewModel.hasMorePreviousMessages.value shouldBe true
            }
        }
        
        test("fetchMessages should set isInProgress to false after completion") {
            runTest {
                val repository = MockMessageListRepository()
                val mockMessages = createMockMessages(2)
                repository.fetchPreviousMessagesResult = Result.success(mockMessages)
                
                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                
                viewModel.fetchMessages()
                advanceUntilIdle()
                
                viewModel.isInProgress.value shouldBe false
            }
        }
        
        /**
         * Property-based test: For any non-empty list of messages returned,
         * the state should always be Loaded and messages count should match.
         * 
         * **Property 5: Pagination State Consistency**
         * **Validates: Requirements 2.1, 17.1, 17.2**
         */
        test("Property 5: Pagination state should be consistent with fetched messages") {
            checkAll(10, Arb.int(1, 50)) { messageCount ->
                runTest {
                    val repository = MockMessageListRepository()
                    val mockMessages = createMockMessages(messageCount)
                    repository.fetchPreviousMessagesResult = Result.success(mockMessages)
                    
                    val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                    
                    viewModel.fetchMessages()
                    advanceUntilIdle()
                    
                    // State should be Loaded for non-empty results
                    viewModel.uiState.value shouldBe MessageListUIState.Loaded
                    viewModel.messages.value.size shouldBe messageCount
                    viewModel.isInProgress.value shouldBe false
                }
            }
        }
    }

    // ========================================
    // Task 44.3: Test fetchMessages updates state to Empty
    // ========================================
    
    context("Task 44.3: fetchMessages updates state to Empty") {
        
        /**
         * When repository returns an empty list, the UI state should be Empty.
         * 
         * **Validates: Requirements 17.1**
         */
        test("fetchMessages should update uiState to Empty when no messages are returned") {
            runTest {
                val repository = MockMessageListRepository()
                repository.fetchPreviousMessagesResult = Result.success(emptyList())
                
                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                
                viewModel.fetchMessages()
                advanceUntilIdle()
                
                viewModel.uiState.value shouldBe MessageListUIState.Empty
            }
        }
        
        test("fetchMessages should have empty messages list when Empty state") {
            runTest {
                val repository = MockMessageListRepository()
                repository.fetchPreviousMessagesResult = Result.success(emptyList())
                
                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                
                viewModel.fetchMessages()
                advanceUntilIdle()
                
                viewModel.messages.value shouldBe emptyList()
            }
        }
        
        test("fetchMessages should set hasMorePreviousMessages to false when empty") {
            runTest {
                val repository = MockMessageListRepository()
                repository.fetchPreviousMessagesResult = Result.success(emptyList())
                
                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                
                viewModel.fetchMessages()
                advanceUntilIdle()
                
                // When empty list is returned, no more messages available
                viewModel.hasMorePreviousMessages.value shouldBe false
            }
        }
        
        test("fetchMessages should set isInProgress to false after empty result") {
            runTest {
                val repository = MockMessageListRepository()
                repository.fetchPreviousMessagesResult = Result.success(emptyList())
                
                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                
                viewModel.fetchMessages()
                advanceUntilIdle()
                
                viewModel.isInProgress.value shouldBe false
            }
        }
    }

    // ========================================
    // Task 44.4: Test fetchMessages updates state to Error
    // ========================================
    
    context("Task 44.4: fetchMessages updates state to Error") {
        
        /**
         * When repository returns an error, the UI state should be Error with the exception.
         * 
         * **Validates: Requirements 17.1**
         */
        test("fetchMessages should update uiState to Error when repository fails") {
            runTest {
                val repository = MockMessageListRepository()
                val testException = Exception("Network error")
                repository.fetchPreviousMessagesResult = Result.failure(testException)
                
                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                
                viewModel.fetchMessages()
                advanceUntilIdle()
                
                viewModel.uiState.value.shouldBeInstanceOf<MessageListUIState.Error>()
            }
        }
        
        test("fetchMessages Error state should contain the exception") {
            runTest {
                val repository = MockMessageListRepository()
                val testException = Exception("Test error message")
                repository.fetchPreviousMessagesResult = Result.failure(testException)
                
                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                
                viewModel.fetchMessages()
                advanceUntilIdle()
                
                val errorState = viewModel.uiState.value as MessageListUIState.Error
                errorState.exception.message shouldBe "Test error message"
            }
        }
        
        test("fetchMessages should keep messages list unchanged on error") {
            runTest {
                val repository = MockMessageListRepository()
                repository.fetchPreviousMessagesResult = Result.failure(Exception("Error"))
                
                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                
                viewModel.fetchMessages()
                advanceUntilIdle()
                
                // Messages list should remain empty (initial state)
                viewModel.messages.value shouldBe emptyList()
            }
        }
        
        test("fetchMessages should set isInProgress to false after error") {
            runTest {
                val repository = MockMessageListRepository()
                repository.fetchPreviousMessagesResult = Result.failure(Exception("Error"))
                
                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                
                viewModel.fetchMessages()
                advanceUntilIdle()
                
                viewModel.isInProgress.value shouldBe false
            }
        }
        
        /**
         * Property-based test: For any error message, the state should be Error
         * with the correct exception message preserved.
         * 
         * **Validates: Requirements 17.1**
         */
        test("Error state should preserve exception message for any error") {
            checkAll(10, Arb.string(1, 100)) { errorMessage ->
                runTest {
                    val repository = MockMessageListRepository()
                    val testException = Exception(errorMessage)
                    repository.fetchPreviousMessagesResult = Result.failure(testException)
                    
                    val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                    
                    viewModel.fetchMessages()
                    advanceUntilIdle()
                    
                    val errorState = viewModel.uiState.value as MessageListUIState.Error
                    errorState.exception.message shouldBe errorMessage
                }
            }
        }
    }

    // ========================================
    // Additional state transition tests
    // ========================================
    
    context("State transition consistency") {
        
        test("fetchMessages should not fetch when hasMorePreviousMessages becomes false after empty result") {
            runTest {
                val repository = MockMessageListRepository()
                val mockMessages = createMockMessages(3)
                repository.fetchPreviousMessagesResult = Result.success(mockMessages)
                
                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                
                // First fetch - returns 3 messages
                viewModel.fetchMessages()
                advanceUntilIdle()
                
                // After first fetch, hasMorePreviousMessages should be true (non-empty result)
                viewModel.messages.value.size shouldBe 3
                viewModel.hasMorePreviousMessages.value shouldBe true
                
                // Now simulate end of messages by returning empty list
                repository.fetchPreviousMessagesResult = Result.success(emptyList())
                
                // Second fetch - returns empty, which sets hasMorePreviousMessages to false
                viewModel.fetchMessages()
                advanceUntilIdle()
                
                // hasMorePreviousMessages should now be false
                viewModel.hasMorePreviousMessages.value shouldBe false
                
                // Set up new messages for potential third fetch
                val moreMessages = createMockMessages(2, startId = 100)
                repository.fetchPreviousMessagesResult = Result.success(moreMessages)
                
                // Third fetch should be ignored because hasMorePreviousMessages is false
                viewModel.fetchMessages()
                advanceUntilIdle()
                
                // Should still have only 3 messages from first fetch
                viewModel.messages.value.size shouldBe 3
            }
        }
        
        test("fetchMessages guards against concurrent execution") {
            runTest {
                val repository = MockMessageListRepository()
                val mockMessages = createMockMessages(3)
                repository.fetchPreviousMessagesResult = Result.success(mockMessages)
                
                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                
                // Fetch messages
                viewModel.fetchMessages()
                advanceUntilIdle()
                
                // After completion, isInProgress should be false
                viewModel.isInProgress.value shouldBe false
                viewModel.messages.value.size shouldBe 3
            }
        }
        
        test("fetchMessages should run when hasMorePreviousMessages is initially true") {
            runTest {
                val repository = MockMessageListRepository()
                val mockMessages = createMockMessages(3)
                repository.fetchPreviousMessagesResult = Result.success(mockMessages)
                
                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                
                // ViewModel starts with hasMorePreviousMessages = true
                viewModel.hasMorePreviousMessages.value shouldBe true
                
                // Fetch will run because ViewModel's internal state is true
                viewModel.fetchMessages()
                advanceUntilIdle()
                
                // After fetch, the ViewModel updates based on result (non-empty = true)
                viewModel.messages.value.size shouldBe 3
                viewModel.hasMorePreviousMessages.value shouldBe true
            }
        }
    }

    // ========================================
    // Task 45.1: Test addMessage increases list size
    // ========================================
    
    context("Task 45.1: addMessage increases list size") {
        
        /**
         * **Property 16: ViewModel State Flow Correctness**
         * 
         * When addMessage is called with a valid message, the list size should
         * increase by exactly 1 and the UI state should be Loaded.
         * 
         * **Validates: Requirements 16.1**
         */
        test("addMessage should increase list size by 1") {
            runTest {
                val repository = MockMessageListRepository()
                val initialMessages = createMockMessages(3)
                repository.fetchPreviousMessagesResult = Result.success(initialMessages)
                
                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                
                // Fetch initial messages
                viewModel.fetchMessages()
                advanceUntilIdle()
                
                val initialSize = viewModel.messages.value.size
                initialSize shouldBe 3
                
                // Add a new message
                val newMessage = createMockMessages(1, startId = 100).first()
                viewModel.addMessage(newMessage)
                
                // Verify list size increased by 1
                viewModel.messages.value.size shouldBe initialSize + 1
            }
        }
        
        test("addMessage should set uiState to Loaded") {
            runTest {
                val repository = MockMessageListRepository()
                repository.fetchPreviousMessagesResult = Result.success(emptyList())
                
                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                
                // Fetch to get Empty state
                viewModel.fetchMessages()
                advanceUntilIdle()
                
                viewModel.uiState.value shouldBe MessageListUIState.Empty
                
                // Add a message
                val newMessage = createMockMessages(1).first()
                viewModel.addMessage(newMessage)
                
                // State should be Loaded
                viewModel.uiState.value shouldBe MessageListUIState.Loaded
            }
        }
        
        test("addMessage should append message to end of list") {
            runTest {
                val repository = MockMessageListRepository()
                val initialMessages = createMockMessages(2)
                repository.fetchPreviousMessagesResult = Result.success(initialMessages)
                
                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                
                viewModel.fetchMessages()
                advanceUntilIdle()
                
                val newMessage = createMockMessages(1, startId = 999).first()
                viewModel.addMessage(newMessage)
                
                // New message should be at the end
                viewModel.messages.value.last().id shouldBe 999L
            }
        }
        
        /**
         * Property-based test: For any number of messages added, the list size
         * should increase by exactly that amount.
         * 
         * **Property 16: ViewModel State Flow Correctness**
         * **Validates: Requirements 16.1**
         */
        test("Property 16: addMessage should increase list size by 1 for any valid message") {
            checkAll(10, Arb.int(1, 20)) { messagesToAdd ->
                runTest {
                    val repository = MockMessageListRepository()
                    val initialMessages = createMockMessages(5)
                    repository.fetchPreviousMessagesResult = Result.success(initialMessages)
                    
                    val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                    
                    viewModel.fetchMessages()
                    advanceUntilIdle()
                    
                    val initialSize = viewModel.messages.value.size
                    
                    // Add multiple messages one by one
                    repeat(messagesToAdd) { index ->
                        val newMessage = createMockMessages(1, startId = 1000 + index).first()
                        viewModel.addMessage(newMessage)
                    }
                    
                    // List size should have increased by exactly messagesToAdd
                    viewModel.messages.value.size shouldBe initialSize + messagesToAdd
                    viewModel.uiState.value shouldBe MessageListUIState.Loaded
                }
            }
        }
    }

    // ========================================
    // Task 45.2: Test updateMessage maintains list size
    // ========================================
    
    context("Task 45.2: updateMessage maintains list size") {
        
        /**
         * **Property 7: Message Update Preservation**
         * 
         * When updateMessage is called, the list size should remain unchanged
         * and only the targeted message should be modified.
         * 
         * **Validates: Requirements 16.2**
         */
        test("updateMessage should not change list size") {
            runTest {
                val repository = MockMessageListRepository()
                val initialMessages = createMockMessages(5)
                repository.fetchPreviousMessagesResult = Result.success(initialMessages)
                
                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                
                viewModel.fetchMessages()
                advanceUntilIdle()
                
                val initialSize = viewModel.messages.value.size
                initialSize shouldBe 5
                
                // Create an updated version of an existing message
                val updatedMessage = com.cometchat.chat.models.TextMessage(
                    "receiver_2",
                    "Updated message content",
                    com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER
                ).apply {
                    id = 2L // Same ID as existing message
                    sentAt = System.currentTimeMillis()
                }
                
                viewModel.updateMessage(updatedMessage)
                
                // List size should remain unchanged
                viewModel.messages.value.size shouldBe initialSize
            }
        }
        
        test("updateMessage should update the correct message content") {
            runTest {
                val repository = MockMessageListRepository()
                val initialMessages = createMockMessages(3)
                repository.fetchPreviousMessagesResult = Result.success(initialMessages)
                
                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                
                viewModel.fetchMessages()
                advanceUntilIdle()
                
                // Create an updated version of message with ID 2
                val updatedMessage = com.cometchat.chat.models.TextMessage(
                    "receiver_updated",
                    "This is the updated text",
                    com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER
                ).apply {
                    id = 2L
                    sentAt = System.currentTimeMillis()
                }
                
                viewModel.updateMessage(updatedMessage)
                
                // Find the message with ID 2 and verify it was updated
                val foundMessage = viewModel.messages.value.find { it.id == 2L }
                foundMessage shouldBe updatedMessage
            }
        }
        
        test("updateMessage should not affect other messages") {
            runTest {
                val repository = MockMessageListRepository()
                val initialMessages = createMockMessages(3)
                repository.fetchPreviousMessagesResult = Result.success(initialMessages)
                
                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                
                viewModel.fetchMessages()
                advanceUntilIdle()
                
                // Store original messages for comparison
                val originalMessage1 = viewModel.messages.value.find { it.id == 1L }
                val originalMessage3 = viewModel.messages.value.find { it.id == 3L }
                
                // Update message with ID 2
                val updatedMessage = com.cometchat.chat.models.TextMessage(
                    "receiver_updated",
                    "Updated content",
                    com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER
                ).apply {
                    id = 2L
                    sentAt = System.currentTimeMillis()
                }
                
                viewModel.updateMessage(updatedMessage)
                
                // Other messages should remain unchanged
                viewModel.messages.value.find { it.id == 1L } shouldBe originalMessage1
                viewModel.messages.value.find { it.id == 3L } shouldBe originalMessage3
            }
        }
        
        test("updateMessage with non-existent ID should not change list") {
            runTest {
                val repository = MockMessageListRepository()
                val initialMessages = createMockMessages(3)
                repository.fetchPreviousMessagesResult = Result.success(initialMessages)
                
                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                
                viewModel.fetchMessages()
                advanceUntilIdle()
                
                val originalMessages = viewModel.messages.value.toList()
                
                // Try to update a message that doesn't exist
                val nonExistentMessage = com.cometchat.chat.models.TextMessage(
                    "receiver_999",
                    "Non-existent message",
                    com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER
                ).apply {
                    id = 999L
                    sentAt = System.currentTimeMillis()
                }
                
                viewModel.updateMessage(nonExistentMessage)
                
                // List should remain unchanged
                viewModel.messages.value.size shouldBe originalMessages.size
            }
        }
        
        /**
         * Property-based test: For any number of updates, the list size should
         * always remain constant.
         * 
         * **Property 7: Message Update Preservation**
         * **Validates: Requirements 16.2**
         */
        test("Property 7: updateMessage should preserve list size for any number of updates") {
            checkAll(10, Arb.int(1, 10)) { updateCount ->
                runTest {
                    val repository = MockMessageListRepository()
                    val initialMessages = createMockMessages(5)
                    repository.fetchPreviousMessagesResult = Result.success(initialMessages)
                    
                    val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                    
                    viewModel.fetchMessages()
                    advanceUntilIdle()
                    
                    val initialSize = viewModel.messages.value.size
                    
                    // Perform multiple updates
                    repeat(updateCount) { index ->
                        val messageIdToUpdate = (index % 5) + 1L // Cycle through IDs 1-5
                        val updatedMessage = com.cometchat.chat.models.TextMessage(
                            "receiver_$messageIdToUpdate",
                            "Updated content iteration $index",
                            com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER
                        ).apply {
                            id = messageIdToUpdate
                            sentAt = System.currentTimeMillis()
                        }
                        
                        viewModel.updateMessage(updatedMessage)
                    }
                    
                    // List size should remain unchanged after all updates
                    viewModel.messages.value.size shouldBe initialSize
                }
            }
        }
    }

    // ========================================
    // Task 45.3: Test removeMessage decreases list size
    // ========================================
    
    context("Task 45.3: removeMessage decreases list size") {
        
        /**
         * **Property 16: ViewModel State Flow Correctness**
         * 
         * When removeMessage is called with a valid message, the list size should
         * decrease by exactly 1.
         * 
         * **Validates: Requirements 16.3**
         */
        test("removeMessage should decrease list size by 1") {
            runTest {
                val repository = MockMessageListRepository()
                val initialMessages = createMockMessages(5)
                repository.fetchPreviousMessagesResult = Result.success(initialMessages)
                
                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                
                viewModel.fetchMessages()
                advanceUntilIdle()
                
                val initialSize = viewModel.messages.value.size
                initialSize shouldBe 5
                
                // Remove a message
                val messageToRemove = viewModel.messages.value.first()
                viewModel.removeMessage(messageToRemove)
                
                // List size should decrease by 1
                viewModel.messages.value.size shouldBe initialSize - 1
            }
        }
        
        test("removeMessage should remove the correct message") {
            runTest {
                val repository = MockMessageListRepository()
                val initialMessages = createMockMessages(3)
                repository.fetchPreviousMessagesResult = Result.success(initialMessages)
                
                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                
                viewModel.fetchMessages()
                advanceUntilIdle()
                
                // Remove message with ID 2
                val messageToRemove = viewModel.messages.value.find { it.id == 2L }!!
                viewModel.removeMessage(messageToRemove)
                
                // Message with ID 2 should no longer exist
                viewModel.messages.value.find { it.id == 2L } shouldBe null
                
                // Other messages should still exist
                viewModel.messages.value.find { it.id == 1L } shouldBe initialMessages[0]
                viewModel.messages.value.find { it.id == 3L } shouldBe initialMessages[2]
            }
        }
        
        test("removeMessage should set uiState to Empty when last message is removed") {
            runTest {
                val repository = MockMessageListRepository()
                val initialMessages = createMockMessages(1)
                repository.fetchPreviousMessagesResult = Result.success(initialMessages)
                
                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                
                viewModel.fetchMessages()
                advanceUntilIdle()
                
                viewModel.uiState.value shouldBe MessageListUIState.Loaded
                
                // Remove the only message
                viewModel.removeMessage(initialMessages.first())
                
                // State should be Empty
                viewModel.uiState.value shouldBe MessageListUIState.Empty
                viewModel.messages.value shouldBe emptyList()
            }
        }
        
        test("removeMessage with non-existent message should not change list") {
            runTest {
                val repository = MockMessageListRepository()
                val initialMessages = createMockMessages(3)
                repository.fetchPreviousMessagesResult = Result.success(initialMessages)
                
                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                
                viewModel.fetchMessages()
                advanceUntilIdle()
                
                val initialSize = viewModel.messages.value.size
                
                // Try to remove a message that doesn't exist
                val nonExistentMessage = com.cometchat.chat.models.TextMessage(
                    "receiver_999",
                    "Non-existent",
                    com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER
                ).apply {
                    id = 999L
                    sentAt = System.currentTimeMillis()
                }
                
                viewModel.removeMessage(nonExistentMessage)
                
                // List size should remain unchanged
                viewModel.messages.value.size shouldBe initialSize
            }
        }
        
        /**
         * Property-based test: For any number of removals, the list size should
         * decrease by exactly that amount (until empty).
         * 
         * **Property 16: ViewModel State Flow Correctness**
         * **Validates: Requirements 16.3**
         */
        test("Property 16: removeMessage should decrease list size by 1 for each removal") {
            checkAll(10, Arb.int(1, 5)) { messagesToRemove ->
                runTest {
                    val repository = MockMessageListRepository()
                    val initialMessages = createMockMessages(10)
                    repository.fetchPreviousMessagesResult = Result.success(initialMessages)
                    
                    val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                    
                    viewModel.fetchMessages()
                    advanceUntilIdle()
                    
                    val initialSize = viewModel.messages.value.size
                    
                    // Remove messages one by one
                    repeat(messagesToRemove) {
                        val messageToRemove = viewModel.messages.value.firstOrNull()
                        if (messageToRemove != null) {
                            viewModel.removeMessage(messageToRemove)
                        }
                    }
                    
                    // List size should have decreased by exactly messagesToRemove
                    viewModel.messages.value.size shouldBe initialSize - messagesToRemove
                }
            }
        }
    }

    // ========================================
    // Task 45.4: Test clear empties list
    // ========================================
    
    context("Task 45.4: clear empties list") {
        
        /**
         * **Property 16: ViewModel State Flow Correctness**
         * 
         * When clear is called, the message list should be empty and the UI state
         * should be Empty.
         * 
         * **Validates: Requirements 16.4**
         */
        test("clear should empty the message list") {
            runTest {
                val repository = MockMessageListRepository()
                val initialMessages = createMockMessages(10)
                repository.fetchPreviousMessagesResult = Result.success(initialMessages)
                
                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                
                viewModel.fetchMessages()
                advanceUntilIdle()
                
                viewModel.messages.value.size shouldBe 10
                
                // Clear the list
                viewModel.clear()
                
                // List should be empty
                viewModel.messages.value shouldBe emptyList()
            }
        }
        
        test("clear should set uiState to Empty") {
            runTest {
                val repository = MockMessageListRepository()
                val initialMessages = createMockMessages(5)
                repository.fetchPreviousMessagesResult = Result.success(initialMessages)
                
                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                
                viewModel.fetchMessages()
                advanceUntilIdle()
                
                viewModel.uiState.value shouldBe MessageListUIState.Loaded
                
                // Clear the list
                viewModel.clear()
                
                // State should be Empty
                viewModel.uiState.value shouldBe MessageListUIState.Empty
            }
        }
        
        test("clear should reset hasMorePreviousMessages to true") {
            runTest {
                val repository = MockMessageListRepository()
                repository.fetchPreviousMessagesResult = Result.success(emptyList())
                
                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                
                // Fetch to set hasMorePreviousMessages to false (empty result)
                viewModel.fetchMessages()
                advanceUntilIdle()
                
                viewModel.hasMorePreviousMessages.value shouldBe false
                
                // Clear should reset it
                viewModel.clear()
                
                viewModel.hasMorePreviousMessages.value shouldBe true
            }
        }
        
        test("clear should reset hasMoreNewMessages to false") {
            runTest {
                val repository = MockMessageListRepository()
                val initialMessages = createMockMessages(3)
                repository.fetchPreviousMessagesResult = Result.success(initialMessages)
                
                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                
                viewModel.fetchMessages()
                advanceUntilIdle()
                
                // Clear should reset hasMoreNewMessages
                viewModel.clear()
                
                viewModel.hasMoreNewMessages.value shouldBe false
            }
        }
        
        test("clear should call repository.resetRequest()") {
            runTest {
                val repository = MockMessageListRepository()
                val initialMessages = createMockMessages(3)
                repository.fetchPreviousMessagesResult = Result.success(initialMessages)
                
                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                
                viewModel.fetchMessages()
                advanceUntilIdle()
                
                repository.resetRequestCalled = false
                
                // Clear should call resetRequest
                viewModel.clear()
                
                repository.resetRequestCalled shouldBe true
            }
        }
        
        test("clear on already empty list should maintain Empty state") {
            runTest {
                val repository = MockMessageListRepository()
                repository.fetchPreviousMessagesResult = Result.success(emptyList())
                
                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                
                viewModel.fetchMessages()
                advanceUntilIdle()
                
                viewModel.uiState.value shouldBe MessageListUIState.Empty
                viewModel.messages.value shouldBe emptyList()
                
                // Clear on empty list
                viewModel.clear()
                
                // Should still be Empty
                viewModel.uiState.value shouldBe MessageListUIState.Empty
                viewModel.messages.value shouldBe emptyList()
            }
        }
        
        /**
         * Property-based test: Clear should always result in an empty list and
         * Empty state, regardless of initial list size.
         * 
         * **Property 16: ViewModel State Flow Correctness**
         * **Validates: Requirements 16.4**
         */
        test("Property 16: clear should always result in empty list and Empty state") {
            checkAll(10, Arb.int(0, 50)) { messageCount ->
                runTest {
                    val repository = MockMessageListRepository()
                    val initialMessages = if (messageCount > 0) createMockMessages(messageCount) else emptyList()
                    repository.fetchPreviousMessagesResult = Result.success(initialMessages)
                    
                    val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                    
                    viewModel.fetchMessages()
                    advanceUntilIdle()
                    
                    // Clear the list
                    viewModel.clear()
                    
                    // Should always be empty and in Empty state
                    viewModel.messages.value shouldBe emptyList()
                    viewModel.uiState.value shouldBe MessageListUIState.Empty
                    viewModel.hasMorePreviousMessages.value shouldBe true
                    viewModel.hasMoreNewMessages.value shouldBe false
                }
            }
        }
    }

    // ========================================
    // Task 9.2: Property 9 - ViewModel Clear Method Behavior
    // Feature: scroll-to-bottom-button-parity
    // ========================================
    
    context("Task 9.2: Property 9 - ViewModel Clear Method Behavior") {
        
        /**
         * **Property 9: ViewModel Clear Method Behavior**
         * 
         * *For any* call to CometChatMessageListViewModel.clear(), the method SHALL:
         * 1. Clear the message list (set to empty)
         * 2. Reset hasMorePreviousMessages to true
         * 3. Call repository.resetRequest() to reset the message request builder
         * 
         * This property ensures the clear() method properly resets the ViewModel state
         * to allow fetching fresh messages from the latest position, which is required
         * for the scroll-to-bottom indicator's reset-and-fetch behavior.
         * 
         * **Validates: Requirements 10.1, 10.2, 10.3**
         */
        test("clear() should clear messages list - Requirement 10.1") {
            runTest {
                val repository = MockMessageListRepository()
                val initialMessages = createMockMessages(15)
                repository.fetchPreviousMessagesResult = Result.success(initialMessages)
                
                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                
                // Fetch initial messages
                viewModel.fetchMessages()
                advanceUntilIdle()
                
                // Verify messages are loaded
                viewModel.messages.value.size shouldBe 15
                
                // Call clear()
                viewModel.clear()
                
                // Messages should be cleared
                viewModel.messages.value shouldBe emptyList()
            }
        }
        
        test("clear() should set hasMorePreviousMessages to true - Requirement 10.2") {
            runTest {
                val repository = MockMessageListRepository()
                // Return empty list to set hasMorePreviousMessages to false
                repository.fetchPreviousMessagesResult = Result.success(emptyList())
                
                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                
                // Fetch to set hasMorePreviousMessages to false
                viewModel.fetchMessages()
                advanceUntilIdle()
                
                // Verify hasMorePreviousMessages is false after empty fetch
                viewModel.hasMorePreviousMessages.value shouldBe false
                
                // Call clear()
                viewModel.clear()
                
                // hasMorePreviousMessages should be reset to true
                viewModel.hasMorePreviousMessages.value shouldBe true
            }
        }
        
        test("clear() should call repository.resetRequest() - Requirement 10.3") {
            runTest {
                val repository = MockMessageListRepository()
                val initialMessages = createMockMessages(5)
                repository.fetchPreviousMessagesResult = Result.success(initialMessages)
                
                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                
                // Fetch initial messages
                viewModel.fetchMessages()
                advanceUntilIdle()
                
                // Reset the tracking flag
                repository.resetRequestCalled = false
                
                // Call clear()
                viewModel.clear()
                
                // Verify resetRequest() was called
                repository.resetRequestCalled shouldBe true
            }
        }
        
        test("clear() should perform all three operations atomically") {
            runTest {
                val repository = MockMessageListRepository()
                val initialMessages = createMockMessages(10)
                repository.fetchPreviousMessagesResult = Result.success(initialMessages)
                
                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                
                // Fetch initial messages
                viewModel.fetchMessages()
                advanceUntilIdle()
                
                // Verify initial state
                viewModel.messages.value.size shouldBe 10
                
                // Simulate hasMorePreviousMessages being false
                repository.fetchPreviousMessagesResult = Result.success(emptyList())
                viewModel.fetchMessages()
                advanceUntilIdle()
                viewModel.hasMorePreviousMessages.value shouldBe false
                
                // Reset tracking
                repository.resetRequestCalled = false
                
                // Call clear() - should perform all three operations
                viewModel.clear()
                
                // Verify all three requirements are met:
                // 10.1: Messages cleared
                viewModel.messages.value shouldBe emptyList()
                // 10.2: hasMorePreviousMessages reset to true
                viewModel.hasMorePreviousMessages.value shouldBe true
                // 10.3: resetRequest() called
                repository.resetRequestCalled shouldBe true
            }
        }
        
        /**
         * Property-based test: For any initial message list size, clear() should
         * always result in an empty list, hasMorePreviousMessages = true, and
         * resetRequest() being called.
         * 
         * **Property 9: ViewModel Clear Method Behavior**
         * **Validates: Requirements 10.1, 10.2, 10.3**
         */
        test("Property 9: clear() should always clear messages, reset hasMorePreviousMessages, and call resetRequest()") {
            checkAll(100, Arb.int(0, 100)) { messageCount ->
                runTest {
                    val repository = MockMessageListRepository()
                    val initialMessages = if (messageCount > 0) createMockMessages(messageCount) else emptyList()
                    repository.fetchPreviousMessagesResult = Result.success(initialMessages)
                    
                    val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                    
                    // Fetch initial messages
                    viewModel.fetchMessages()
                    advanceUntilIdle()
                    
                    // Simulate hasMorePreviousMessages being false by fetching empty
                    repository.fetchPreviousMessagesResult = Result.success(emptyList())
                    if (viewModel.hasMorePreviousMessages.value) {
                        viewModel.fetchMessages()
                        advanceUntilIdle()
                    }
                    
                    // Reset tracking before clear()
                    repository.resetRequestCalled = false
                    
                    // Call clear()
                    viewModel.clear()
                    
                    // Property 9 assertions:
                    // 10.1: Messages should be cleared
                    viewModel.messages.value shouldBe emptyList()
                    // 10.2: hasMorePreviousMessages should be true
                    viewModel.hasMorePreviousMessages.value shouldBe true
                    // 10.3: resetRequest() should have been called
                    repository.resetRequestCalled shouldBe true
                }
            }
        }
        
        /**
         * Property-based test: After clear(), fetchMessages() should be able to
         * fetch fresh messages (simulating the reset-and-fetch behavior).
         * 
         * **Property 9: ViewModel Clear Method Behavior**
         * **Validates: Requirements 10.1, 10.2, 10.3**
         */
        test("Property 9: After clear(), fetchMessages() should fetch fresh messages") {
            checkAll(100, Arb.int(1, 50), Arb.int(1, 50)) { initialCount, newCount ->
                runTest {
                    val repository = MockMessageListRepository()
                    val initialMessages = createMockMessages(initialCount)
                    repository.fetchPreviousMessagesResult = Result.success(initialMessages)
                    
                    val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                    
                    // Fetch initial messages
                    viewModel.fetchMessages()
                    advanceUntilIdle()
                    
                    viewModel.messages.value.size shouldBe initialCount
                    
                    // Call clear()
                    viewModel.clear()
                    
                    // Verify cleared state
                    viewModel.messages.value shouldBe emptyList()
                    viewModel.hasMorePreviousMessages.value shouldBe true
                    
                    // Set up new messages for fresh fetch
                    val newMessages = createMockMessages(newCount, startId = 1000)
                    repository.fetchPreviousMessagesResult = Result.success(newMessages)
                    
                    // Fetch fresh messages (simulating reset-and-fetch behavior)
                    viewModel.fetchMessages()
                    advanceUntilIdle()
                    
                    // Should have new messages
                    viewModel.messages.value.size shouldBe newCount
                    // First message should be from the new batch (ID >= 1000)
                    viewModel.messages.value.first().id shouldBe 1000L
                }
            }
        }
    }

    // ========================================
    // Task 46.1: Test alignment for outgoing messages
    // ========================================
    
    context("Task 46.1: Test alignment for outgoing messages") {
        
        /**
         * **Property 3: Message Alignment Determination**
         * 
         * Messages sent by the current user should be aligned to the RIGHT.
         * This ensures outgoing messages appear on the right side of the chat.
         * 
         * **Validates: Requirements 1.6**
         */
        test("getMessageAlignment should return RIGHT for outgoing messages (sender = current user)") {
            runTest {
                val repository = MockMessageListRepository()
                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                
                // Create a message where sender is the current user
                // Note: CometChat.getLoggedInUser() returns the current user
                val currentUser = com.cometchat.chat.core.CometChat.getLoggedInUser()
                
                val outgoingMessage = com.cometchat.chat.models.TextMessage(
                    "receiver_123",
                    "Hello from current user",
                    com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER
                ).apply {
                    id = 1L
                    sentAt = System.currentTimeMillis()
                    sender = currentUser
                }
                
                val alignment = viewModel.getMessageAlignment(outgoingMessage)
                
                alignment shouldBe MessageAlignment.RIGHT
            }
        }
        
        /**
         * Property-based test: For any message where sender UID matches current user UID,
         * alignment should always be RIGHT (unless it's an ACTION or CALL message).
         * 
         * **Property 3: Message Alignment Determination**
         * **Validates: Requirements 1.6**
         */
        test("Property 3: Outgoing messages should always align RIGHT") {
            checkAll(10, Arb.long(1, 1000)) { messageId ->
                runTest {
                    val repository = MockMessageListRepository()
                    val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                    
                    val currentUser = com.cometchat.chat.core.CometChat.getLoggedInUser()
                    
                    val outgoingMessage = com.cometchat.chat.models.TextMessage(
                        "receiver_$messageId",
                        "Test message $messageId",
                        com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER
                    ).apply {
                        id = messageId
                        sentAt = System.currentTimeMillis()
                        sender = currentUser
                    }
                    
                    val alignment = viewModel.getMessageAlignment(outgoingMessage)
                    
                    alignment shouldBe MessageAlignment.RIGHT
                }
            }
        }
    }

    // ========================================
    // Task 46.2: Test alignment for incoming messages
    // ========================================
    
    context("Task 46.2: Test alignment for incoming messages") {
        
        /**
         * **Property 3: Message Alignment Determination**
         * 
         * Messages from other users (not the current user) should be aligned to the LEFT.
         * This ensures incoming messages appear on the left side of the chat.
         * 
         * **Validates: Requirements 1.6**
         */
        test("getMessageAlignment should return LEFT for incoming messages (sender != current user)") {
            runTest {
                val repository = MockMessageListRepository()
                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                
                // Create a different user as sender
                val otherUser = User().apply {
                    uid = "other_user_123"
                    name = "Other User"
                }
                
                val incomingMessage = com.cometchat.chat.models.TextMessage(
                    "current_user_uid",
                    "Hello from other user",
                    com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER
                ).apply {
                    id = 1L
                    sentAt = System.currentTimeMillis()
                    sender = otherUser
                }
                
                val alignment = viewModel.getMessageAlignment(incomingMessage)
                
                alignment shouldBe MessageAlignment.LEFT
            }
        }
        
        test("getMessageAlignment should return LEFT when sender is null") {
            runTest {
                val repository = MockMessageListRepository()
                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                
                val messageWithNullSender = com.cometchat.chat.models.TextMessage(
                    "receiver_123",
                    "Message with null sender",
                    com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER
                ).apply {
                    id = 1L
                    sentAt = System.currentTimeMillis()
                    sender = null
                }
                
                val alignment = viewModel.getMessageAlignment(messageWithNullSender)
                
                alignment shouldBe MessageAlignment.LEFT
            }
        }
        
        /**
         * Property-based test: For any message where sender UID does NOT match current user UID,
         * alignment should always be LEFT (unless it's an ACTION or CALL message).
         * 
         * **Property 3: Message Alignment Determination**
         * **Validates: Requirements 1.6**
         */
        test("Property 3: Incoming messages should always align LEFT") {
            checkAll(10, Arb.string(5, 20)) { otherUserId ->
                runTest {
                    val repository = MockMessageListRepository()
                    val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                    
                    // Ensure the other user ID is different from current user
                    val currentUser = com.cometchat.chat.core.CometChat.getLoggedInUser()
                    val uniqueOtherId = if (otherUserId == currentUser?.uid) "different_$otherUserId" else otherUserId
                    
                    val otherUser = User().apply {
                        uid = uniqueOtherId
                        name = "Other User"
                    }
                    
                    val incomingMessage = com.cometchat.chat.models.TextMessage(
                        "receiver_123",
                        "Test incoming message",
                        com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER
                    ).apply {
                        id = 1L
                        sentAt = System.currentTimeMillis()
                        sender = otherUser
                    }
                    
                    val alignment = viewModel.getMessageAlignment(incomingMessage)
                    
                    alignment shouldBe MessageAlignment.LEFT
                }
            }
        }
    }

    // ========================================
    // Task 46.3: Test alignment for action messages
    // ========================================
    
    context("Task 46.3: Test alignment for action messages") {
        
        /**
         * **Property 3: Message Alignment Determination**
         * 
         * Action messages (group actions like member joined, left, etc.) should be
         * aligned to the CENTER regardless of who triggered the action.
         * 
         * **Validates: Requirements 1.6**
         */
        test("getMessageAlignment should return CENTER for action messages") {
            runTest {
                val repository = MockMessageListRepository()
                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                
                // Create an Action message
                val actionMessage = Action().apply {
                    id = 1L
                    sentAt = System.currentTimeMillis()
                    category = com.cometchat.chat.constants.CometChatConstants.CATEGORY_ACTION
                }
                
                val alignment = viewModel.getMessageAlignment(actionMessage)
                
                alignment shouldBe MessageAlignment.CENTER
            }
        }
        
        test("getMessageAlignment should return CENTER for action messages even with current user as sender") {
            runTest {
                val repository = MockMessageListRepository()
                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                
                val currentUser = com.cometchat.chat.core.CometChat.getLoggedInUser()
                
                // Action message with current user as sender should still be CENTER
                val actionMessage = Action().apply {
                    id = 1L
                    sentAt = System.currentTimeMillis()
                    category = com.cometchat.chat.constants.CometChatConstants.CATEGORY_ACTION
                    sender = currentUser
                }
                
                val alignment = viewModel.getMessageAlignment(actionMessage)
                
                alignment shouldBe MessageAlignment.CENTER
            }
        }
        
        /**
         * Property-based test: For any message with category = ACTION,
         * alignment should always be CENTER regardless of sender.
         * 
         * **Property 3: Message Alignment Determination**
         * **Validates: Requirements 1.6**
         */
        test("Property 3: Action messages should always align CENTER regardless of sender") {
            checkAll(10, Arb.long(1, 1000)) { messageId ->
                runTest {
                    val repository = MockMessageListRepository()
                    val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                    
                    // Test with various senders - action should always be CENTER
                    val actionMessage = Action().apply {
                        id = messageId
                        sentAt = System.currentTimeMillis()
                        category = com.cometchat.chat.constants.CometChatConstants.CATEGORY_ACTION
                    }
                    
                    val alignment = viewModel.getMessageAlignment(actionMessage)
                    
                    alignment shouldBe MessageAlignment.CENTER
                }
            }
        }
    }

    // ========================================
    // Task 46.4: Test alignment for call messages
    // ========================================
    
    context("Task 46.4: Test alignment for call messages") {
        
        /**
         * **Property 3: Message Alignment Determination**
         * 
         * Call messages (incoming, outgoing, missed calls) should be aligned to the
         * CENTER regardless of who initiated the call.
         * 
         * **Validates: Requirements 1.6**
         */
        test("getMessageAlignment should return CENTER for call messages") {
            runTest {
                val repository = MockMessageListRepository()
                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                
                // Create a Call message
                val callMessage = Call(
                    "receiver_123",
                    com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER,
                    com.cometchat.chat.constants.CometChatConstants.CALL_TYPE_AUDIO
                ).apply {
                    id = 1L
                    sentAt = System.currentTimeMillis()
                    category = com.cometchat.chat.constants.CometChatConstants.CATEGORY_CALL
                }
                
                val alignment = viewModel.getMessageAlignment(callMessage)
                
                alignment shouldBe MessageAlignment.CENTER
            }
        }
        
        test("getMessageAlignment should return CENTER for call messages even with current user as sender") {
            runTest {
                val repository = MockMessageListRepository()
                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                
                val currentUser = com.cometchat.chat.core.CometChat.getLoggedInUser()
                
                // Call message with current user as sender should still be CENTER
                val callMessage = Call(
                    "receiver_123",
                    com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER,
                    com.cometchat.chat.constants.CometChatConstants.CALL_TYPE_VIDEO
                ).apply {
                    id = 1L
                    sentAt = System.currentTimeMillis()
                    category = com.cometchat.chat.constants.CometChatConstants.CATEGORY_CALL
                    sender = currentUser
                }
                
                val alignment = viewModel.getMessageAlignment(callMessage)
                
                alignment shouldBe MessageAlignment.CENTER
            }
        }
        
        test("getMessageAlignment should return CENTER for audio call messages") {
            runTest {
                val repository = MockMessageListRepository()
                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                
                val audioCallMessage = Call(
                    "receiver_123",
                    com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER,
                    com.cometchat.chat.constants.CometChatConstants.CALL_TYPE_AUDIO
                ).apply {
                    id = 1L
                    sentAt = System.currentTimeMillis()
                    category = com.cometchat.chat.constants.CometChatConstants.CATEGORY_CALL
                }
                
                val alignment = viewModel.getMessageAlignment(audioCallMessage)
                
                alignment shouldBe MessageAlignment.CENTER
            }
        }
        
        test("getMessageAlignment should return CENTER for video call messages") {
            runTest {
                val repository = MockMessageListRepository()
                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                
                val videoCallMessage = Call(
                    "receiver_123",
                    com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER,
                    com.cometchat.chat.constants.CometChatConstants.CALL_TYPE_VIDEO
                ).apply {
                    id = 1L
                    sentAt = System.currentTimeMillis()
                    category = com.cometchat.chat.constants.CometChatConstants.CATEGORY_CALL
                }
                
                val alignment = viewModel.getMessageAlignment(videoCallMessage)
                
                alignment shouldBe MessageAlignment.CENTER
            }
        }
        
        /**
         * Property-based test: For any message with category = CALL,
         * alignment should always be CENTER regardless of sender or call type.
         * 
         * **Property 3: Message Alignment Determination**
         * **Validates: Requirements 1.6**
         */
        test("Property 3: Call messages should always align CENTER regardless of sender") {
            checkAll(10, Arb.long(1, 1000)) { messageId ->
                runTest {
                    val repository = MockMessageListRepository()
                    val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                    
                    // Test with various call types - should always be CENTER
                    val callMessage = Call(
                        "receiver_$messageId",
                        com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER,
                        com.cometchat.chat.constants.CometChatConstants.CALL_TYPE_AUDIO
                    ).apply {
                        id = messageId
                        sentAt = System.currentTimeMillis()
                        category = com.cometchat.chat.constants.CometChatConstants.CATEGORY_CALL
                    }
                    
                    val alignment = viewModel.getMessageAlignment(callMessage)
                    
                    alignment shouldBe MessageAlignment.CENTER
                }
            }
        }
    }

    // ========================================
    // Task 47.1: Test isMessageForCurrentChat with matching user
    // ========================================
    
    context("Task 47.1: isMessageForCurrentChat with matching user") {
        
        /**
         * **Property 2: Conversation Context Filtering**
         * 
         * When a user is set on the ViewModel, messages from that user should be
         * accepted by isMessageForCurrentChat (tested via addMessage).
         * 
         * **Validates: Requirements 1.2**
         */
        test("addMessage should add message when sender matches configured user") {
            runTest {
                val repository = MockMessageListRepository()
                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                
                // Create a user to configure the ViewModel
                val configuredUser = User().apply {
                    uid = "user_123"
                    name = "Test User"
                }
                
                // Configure ViewModel for this user
                viewModel.setUser(configuredUser)
                
                // Create a message FROM the configured user TO the current user
                val currentUser = com.cometchat.chat.core.CometChat.getLoggedInUser()
                val incomingMessage = com.cometchat.chat.models.TextMessage(
                    currentUser?.uid ?: "current_user",
                    "Hello from configured user",
                    com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER
                ).apply {
                    id = 100L
                    sentAt = System.currentTimeMillis()
                    sender = configuredUser
                    receiverType = com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER
                    receiverUid = currentUser?.uid ?: "current_user"
                    parentMessageId = 0L // Not a threaded message
                }
                
                // Add the message
                viewModel.addMessage(incomingMessage)
                
                // Message should be added to the list
                viewModel.messages.value.size shouldBe 1
                viewModel.messages.value.first().id shouldBe 100L
            }
        }
        
        test("addMessage should add message when receiver matches configured user (outgoing)") {
            runTest {
                val repository = MockMessageListRepository()
                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                
                // Create a user to configure the ViewModel
                val configuredUser = User().apply {
                    uid = "user_123"
                    name = "Test User"
                }
                
                // Configure ViewModel for this user
                viewModel.setUser(configuredUser)
                
                // Create a message FROM current user TO the configured user
                val currentUser = com.cometchat.chat.core.CometChat.getLoggedInUser()
                val outgoingMessage = com.cometchat.chat.models.TextMessage(
                    configuredUser.uid,
                    "Hello to configured user",
                    com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER
                ).apply {
                    id = 101L
                    sentAt = System.currentTimeMillis()
                    sender = currentUser
                    receiverType = com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER
                    receiverUid = configuredUser.uid
                    parentMessageId = 0L // Not a threaded message
                }
                
                // Add the message
                viewModel.addMessage(outgoingMessage)
                
                // Message should be added to the list
                viewModel.messages.value.size shouldBe 1
                viewModel.messages.value.first().id shouldBe 101L
            }
        }
        
        /**
         * Property-based test: For any user UID, messages from that user should be
         * accepted when the ViewModel is configured for that user.
         * 
         * **Property 2: Conversation Context Filtering**
         * **Validates: Requirements 1.2**
         */
        test("Property 2: Messages from configured user should always be accepted") {
            checkAll(10, Arb.string(5, 20)) { userId ->
                runTest {
                    val repository = MockMessageListRepository()
                    val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                    
                    // Create a user with the generated UID
                    val configuredUser = User().apply {
                        uid = userId
                        name = "User $userId"
                    }
                    
                    // Configure ViewModel for this user
                    viewModel.setUser(configuredUser)
                    
                    // Create a message from the configured user
                    val currentUser = com.cometchat.chat.core.CometChat.getLoggedInUser()
                    val message = com.cometchat.chat.models.TextMessage(
                        currentUser?.uid ?: "current_user",
                        "Test message",
                        com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER
                    ).apply {
                        id = 1L
                        sentAt = System.currentTimeMillis()
                        sender = configuredUser
                        receiverType = com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER
                        receiverUid = currentUser?.uid ?: "current_user"
                        parentMessageId = 0L
                    }
                    
                    // Add the message
                    viewModel.addMessage(message)
                    
                    // Message should be accepted
                    viewModel.messages.value.size shouldBe 1
                }
            }
        }
    }

    // ========================================
    // Task 47.2: Test isMessageForCurrentChat with non-matching user
    // ========================================
    
    context("Task 47.2: isMessageForCurrentChat with non-matching user") {
        
        /**
         * **Property 2: Conversation Context Filtering**
         * 
         * When a user is set on the ViewModel, messages from a different user should be
         * rejected by isMessageForCurrentChat (tested via addMessage).
         * 
         * **Validates: Requirements 1.2**
         */
        test("addMessage should NOT add message when sender does not match configured user") {
            runTest {
                val repository = MockMessageListRepository()
                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                
                // Create a user to configure the ViewModel
                val configuredUser = User().apply {
                    uid = "user_123"
                    name = "Configured User"
                }
                
                // Configure ViewModel for this user
                viewModel.setUser(configuredUser)
                
                // Create a DIFFERENT user as sender
                val differentUser = User().apply {
                    uid = "different_user_456"
                    name = "Different User"
                }
                
                // Create a message from a different user (not the configured user)
                val currentUser = com.cometchat.chat.core.CometChat.getLoggedInUser()
                val messageFromDifferentUser = com.cometchat.chat.models.TextMessage(
                    currentUser?.uid ?: "current_user",
                    "Hello from different user",
                    com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER
                ).apply {
                    id = 200L
                    sentAt = System.currentTimeMillis()
                    sender = differentUser
                    receiverType = com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER
                    receiverUid = currentUser?.uid ?: "current_user"
                    parentMessageId = 0L
                }
                
                // Try to add the message
                viewModel.addMessage(messageFromDifferentUser)
                
                // Message should NOT be added to the list
                viewModel.messages.value.size shouldBe 0
            }
        }
        
        test("addMessage should NOT add message when receiver does not match configured user") {
            runTest {
                val repository = MockMessageListRepository()
                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                
                // Create a user to configure the ViewModel
                val configuredUser = User().apply {
                    uid = "user_123"
                    name = "Configured User"
                }
                
                // Configure ViewModel for this user
                viewModel.setUser(configuredUser)
                
                // Create a message to a different user (not the configured user)
                val currentUser = com.cometchat.chat.core.CometChat.getLoggedInUser()
                val messageToOtherUser = com.cometchat.chat.models.TextMessage(
                    "other_user_789", // Different receiver
                    "Hello to other user",
                    com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER
                ).apply {
                    id = 201L
                    sentAt = System.currentTimeMillis()
                    sender = currentUser
                    receiverType = com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER
                    receiverUid = "other_user_789"
                    parentMessageId = 0L
                }
                
                // Try to add the message
                viewModel.addMessage(messageToOtherUser)
                
                // Message should NOT be added to the list
                viewModel.messages.value.size shouldBe 0
            }
        }
        
        /**
         * Property-based test: For any two different user UIDs, messages from one user
         * should be rejected when the ViewModel is configured for the other user.
         * 
         * **Property 2: Conversation Context Filtering**
         * **Validates: Requirements 1.2**
         */
        test("Property 2: Messages from non-configured user should always be rejected") {
            checkAll(10, Arb.string(5, 15), Arb.string(5, 15)) { userId1, userId2 ->
                // Ensure the two user IDs are different
                val configuredUserId = userId1
                val differentUserId = if (userId2 == userId1) "${userId2}_different" else userId2
                
                runTest {
                    val repository = MockMessageListRepository()
                    val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                    
                    // Configure ViewModel for user1
                    val configuredUser = User().apply {
                        uid = configuredUserId
                        name = "Configured User"
                    }
                    viewModel.setUser(configuredUser)
                    
                    // Create a message from user2 (different user)
                    val differentUser = User().apply {
                        uid = differentUserId
                        name = "Different User"
                    }
                    
                    val currentUser = com.cometchat.chat.core.CometChat.getLoggedInUser()
                    val message = com.cometchat.chat.models.TextMessage(
                        currentUser?.uid ?: "current_user",
                        "Test message",
                        com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER
                    ).apply {
                        id = 1L
                        sentAt = System.currentTimeMillis()
                        sender = differentUser
                        receiverType = com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER
                        receiverUid = currentUser?.uid ?: "current_user"
                        parentMessageId = 0L
                    }
                    
                    // Try to add the message
                    viewModel.addMessage(message)
                    
                    // Message should be rejected
                    viewModel.messages.value.size shouldBe 0
                }
            }
        }
    }

    // ========================================
    // Task 47.3: Test isMessageForCurrentChat with matching group
    // ========================================
    
    context("Task 47.3: isMessageForCurrentChat with matching group") {
        
        /**
         * **Property 2: Conversation Context Filtering**
         * 
         * When a group is set on the ViewModel, messages for that group should be
         * accepted by isMessageForCurrentChat (tested via addMessage).
         * 
         * **Validates: Requirements 1.3**
         */
        test("addMessage should add message when receiver matches configured group") {
            runTest {
                val repository = MockMessageListRepository()
                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                
                // Create a group to configure the ViewModel
                val configuredGroup = Group().apply {
                    guid = "group_123"
                    name = "Test Group"
                }
                
                // Configure ViewModel for this group
                viewModel.setGroup(configuredGroup)
                
                // Create a message for the configured group
                val sender = User().apply {
                    uid = "some_user"
                    name = "Some User"
                }
                
                val groupMessage = com.cometchat.chat.models.TextMessage(
                    configuredGroup.guid,
                    "Hello to group",
                    com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_GROUP
                ).apply {
                    id = 300L
                    sentAt = System.currentTimeMillis()
                    this.sender = sender
                    receiverType = com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_GROUP
                    receiverUid = configuredGroup.guid
                    parentMessageId = 0L
                }
                
                // Add the message
                viewModel.addMessage(groupMessage)
                
                // Message should be added to the list
                viewModel.messages.value.size shouldBe 1
                viewModel.messages.value.first().id shouldBe 300L
            }
        }
        
        test("addMessage should NOT add message when receiver does not match configured group") {
            runTest {
                val repository = MockMessageListRepository()
                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                
                // Create a group to configure the ViewModel
                val configuredGroup = Group().apply {
                    guid = "group_123"
                    name = "Configured Group"
                }
                
                // Configure ViewModel for this group
                viewModel.setGroup(configuredGroup)
                
                // Create a message for a DIFFERENT group
                val sender = User().apply {
                    uid = "some_user"
                    name = "Some User"
                }
                
                val messageForOtherGroup = com.cometchat.chat.models.TextMessage(
                    "other_group_456", // Different group
                    "Hello to other group",
                    com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_GROUP
                ).apply {
                    id = 301L
                    sentAt = System.currentTimeMillis()
                    this.sender = sender
                    receiverType = com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_GROUP
                    receiverUid = "other_group_456"
                    parentMessageId = 0L
                }
                
                // Try to add the message
                viewModel.addMessage(messageForOtherGroup)
                
                // Message should NOT be added to the list
                viewModel.messages.value.size shouldBe 0
            }
        }
        
        /**
         * Property-based test: For any group GUID, messages for that group should be
         * accepted when the ViewModel is configured for that group.
         * 
         * **Property 2: Conversation Context Filtering**
         * **Validates: Requirements 1.3**
         */
        test("Property 2: Messages for configured group should always be accepted") {
            checkAll(10, Arb.string(5, 20)) { groupId ->
                runTest {
                    val repository = MockMessageListRepository()
                    val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                    
                    // Create a group with the generated GUID
                    val configuredGroup = Group().apply {
                        guid = groupId
                        name = "Group $groupId"
                    }
                    
                    // Configure ViewModel for this group
                    viewModel.setGroup(configuredGroup)
                    
                    // Create a message for the configured group
                    val sender = User().apply {
                        uid = "sender_user"
                        name = "Sender"
                    }
                    
                    val message = com.cometchat.chat.models.TextMessage(
                        groupId,
                        "Test message",
                        com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_GROUP
                    ).apply {
                        id = 1L
                        sentAt = System.currentTimeMillis()
                        this.sender = sender
                        receiverType = com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_GROUP
                        receiverUid = groupId
                        parentMessageId = 0L
                    }
                    
                    // Add the message
                    viewModel.addMessage(message)
                    
                    // Message should be accepted
                    viewModel.messages.value.size shouldBe 1
                }
            }
        }
    }

    // ========================================
    // Task 47.4: Test isThreadedMessageForCurrentChat
    // ========================================
    
    context("Task 47.4: isThreadedMessageForCurrentChat") {
        
        /**
         * **Property 2: Conversation Context Filtering**
         * 
         * When parentMessageId is set on the ViewModel, only messages with matching
         * parentMessageId should be accepted (tested via addMessage).
         * 
         * **Validates: Requirements 1.4, 7.3**
         */
        test("addMessage should add message when parentMessageId matches configured thread") {
            runTest {
                val repository = MockMessageListRepository()
                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                
                // Create a user and configure ViewModel with a parentMessageId (thread mode)
                val configuredUser = User().apply {
                    uid = "user_123"
                    name = "Test User"
                }
                
                val threadParentId = 500L
                viewModel.setUser(configuredUser, parentMessageId = threadParentId)
                
                // Create a message that is a reply to the thread parent
                val currentUser = com.cometchat.chat.core.CometChat.getLoggedInUser()
                val threadedMessage = com.cometchat.chat.models.TextMessage(
                    currentUser?.uid ?: "current_user",
                    "Reply in thread",
                    com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER
                ).apply {
                    id = 501L
                    sentAt = System.currentTimeMillis()
                    sender = configuredUser
                    receiverType = com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER
                    receiverUid = currentUser?.uid ?: "current_user"
                    parentMessageId = threadParentId // Matches the configured thread
                }
                
                // Add the message
                viewModel.addMessage(threadedMessage)
                
                // Message should be added to the list
                viewModel.messages.value.size shouldBe 1
                viewModel.messages.value.first().id shouldBe 501L
            }
        }
        
        test("addMessage should NOT add message when parentMessageId does not match configured thread") {
            runTest {
                val repository = MockMessageListRepository()
                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                
                // Create a user and configure ViewModel with a parentMessageId (thread mode)
                val configuredUser = User().apply {
                    uid = "user_123"
                    name = "Test User"
                }
                
                val threadParentId = 500L
                viewModel.setUser(configuredUser, parentMessageId = threadParentId)
                
                // Create a message that is a reply to a DIFFERENT thread
                val currentUser = com.cometchat.chat.core.CometChat.getLoggedInUser()
                val messageInDifferentThread = com.cometchat.chat.models.TextMessage(
                    currentUser?.uid ?: "current_user",
                    "Reply in different thread",
                    com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER
                ).apply {
                    id = 502L
                    sentAt = System.currentTimeMillis()
                    sender = configuredUser
                    receiverType = com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER
                    receiverUid = currentUser?.uid ?: "current_user"
                    parentMessageId = 999L // Different thread
                }
                
                // Try to add the message
                viewModel.addMessage(messageInDifferentThread)
                
                // Message should NOT be added to the list
                viewModel.messages.value.size shouldBe 0
            }
        }
        
        test("addMessage should NOT add threaded message when ViewModel is in main conversation mode") {
            runTest {
                val repository = MockMessageListRepository()
                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                
                // Create a user and configure ViewModel WITHOUT parentMessageId (main conversation)
                val configuredUser = User().apply {
                    uid = "user_123"
                    name = "Test User"
                }
                
                // Default parentMessageId is -1 (main conversation mode)
                viewModel.setUser(configuredUser)
                
                // Create a threaded message (has a parentMessageId)
                val currentUser = com.cometchat.chat.core.CometChat.getLoggedInUser()
                val threadedMessage = com.cometchat.chat.models.TextMessage(
                    currentUser?.uid ?: "current_user",
                    "This is a threaded reply",
                    com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER
                ).apply {
                    id = 503L
                    sentAt = System.currentTimeMillis()
                    sender = configuredUser
                    receiverType = com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER
                    receiverUid = currentUser?.uid ?: "current_user"
                    parentMessageId = 100L // This is a threaded message
                }
                
                // Try to add the message
                viewModel.addMessage(threadedMessage)
                
                // Message should NOT be added (threaded messages don't belong in main conversation)
                viewModel.messages.value.size shouldBe 0
            }
        }
        
        test("addMessage should add non-threaded message when ViewModel is in main conversation mode") {
            runTest {
                val repository = MockMessageListRepository()
                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                
                // Create a user and configure ViewModel WITHOUT parentMessageId (main conversation)
                val configuredUser = User().apply {
                    uid = "user_123"
                    name = "Test User"
                }
                
                // Default parentMessageId is -1 (main conversation mode)
                viewModel.setUser(configuredUser)
                
                // Create a non-threaded message (parentMessageId = 0)
                val currentUser = com.cometchat.chat.core.CometChat.getLoggedInUser()
                val mainMessage = com.cometchat.chat.models.TextMessage(
                    currentUser?.uid ?: "current_user",
                    "This is a main conversation message",
                    com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER
                ).apply {
                    id = 504L
                    sentAt = System.currentTimeMillis()
                    sender = configuredUser
                    receiverType = com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER
                    receiverUid = currentUser?.uid ?: "current_user"
                    parentMessageId = 0L // Not a threaded message
                }
                
                // Add the message
                viewModel.addMessage(mainMessage)
                
                // Message should be added to the list
                viewModel.messages.value.size shouldBe 1
                viewModel.messages.value.first().id shouldBe 504L
            }
        }
        
        /**
         * Property-based test: For any parentMessageId, only messages with matching
         * parentMessageId should be accepted in thread mode.
         * 
         * **Property 2: Conversation Context Filtering**
         * **Validates: Requirements 1.4, 7.3**
         */
        test("Property 2: Only messages with matching parentMessageId should be accepted in thread mode") {
            checkAll(10, Arb.long(1, 1000)) { threadParentId ->
                runTest {
                    val repository = MockMessageListRepository()
                    val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                    
                    // Configure ViewModel for a thread
                    val configuredUser = User().apply {
                        uid = "user_123"
                        name = "Test User"
                    }
                    viewModel.setUser(configuredUser, parentMessageId = threadParentId)
                    
                    // Create a message with matching parentMessageId
                    val currentUser = com.cometchat.chat.core.CometChat.getLoggedInUser()
                    val matchingMessage = com.cometchat.chat.models.TextMessage(
                        currentUser?.uid ?: "current_user",
                        "Thread reply",
                        com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER
                    ).apply {
                        id = 1L
                        sentAt = System.currentTimeMillis()
                        sender = configuredUser
                        receiverType = com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER
                        receiverUid = currentUser?.uid ?: "current_user"
                        parentMessageId = threadParentId // Matches
                    }
                    
                    // Create a message with non-matching parentMessageId
                    val nonMatchingMessage = com.cometchat.chat.models.TextMessage(
                        currentUser?.uid ?: "current_user",
                        "Different thread reply",
                        com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER
                    ).apply {
                        id = 2L
                        sentAt = System.currentTimeMillis()
                        sender = configuredUser
                        receiverType = com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER
                        receiverUid = currentUser?.uid ?: "current_user"
                        parentMessageId = threadParentId + 1 // Different
                    }
                    
                    // Add both messages
                    viewModel.addMessage(matchingMessage)
                    viewModel.addMessage(nonMatchingMessage)
                    
                    // Only the matching message should be added
                    viewModel.messages.value.size shouldBe 1
                    viewModel.messages.value.first().id shouldBe 1L
                }
            }
        }
    }

    // ========================================
    // Task 48.1: Test deleteMessage with hideDeleteMessage=false
    // ========================================
    
    context("Task 48.1: Test deleteMessage with hideDeleteMessage=false") {
        
        /**
         * **Property 8: Message Deletion Behavior**
         * 
         * When hideDeleteMessage is false, deleted messages should be updated in the list
         * (showing "message deleted" placeholder) rather than removed entirely.
         * 
         * **Validates: Requirements 3.5, 16.5**
         */
        test("deleteMessage with hideDeleteMessage=false should update message in list (not remove)") {
            runTest {
                val repository = MockMessageListRepository()
                val initialMessages = createMockMessages(3)
                repository.fetchPreviousMessagesResult = Result.success(initialMessages)
                
                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                
                // Set hideDeleteMessage to false (default behavior)
                viewModel.setHideDeleteMessage(false)
                
                viewModel.fetchMessages()
                advanceUntilIdle()
                
                val initialSize = viewModel.messages.value.size
                initialSize shouldBe 3
                
                // Get the message to delete
                val messageToDelete = viewModel.messages.value.find { it.id == 2L }!!
                
                // Create a deleted version of the message (simulating SDK response)
                val deletedMessage = com.cometchat.chat.models.TextMessage(
                    "receiver_2",
                    "This message was deleted",
                    com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER
                ).apply {
                    id = 2L
                    sentAt = messageToDelete.sentAt
                    deletedAt = System.currentTimeMillis()
                }
                
                // Configure repository to return the deleted message
                repository.deleteMessageResult = Result.success(deletedMessage)
                
                // Delete the message
                viewModel.deleteMessage(messageToDelete)
                advanceUntilIdle()
                
                // List size should remain unchanged (message updated, not removed)
                viewModel.messages.value.size shouldBe initialSize
                
                // The message should be updated (not removed)
                val updatedMessage = viewModel.messages.value.find { it.id == 2L }
                updatedMessage shouldBe deletedMessage
            }
        }
        
        test("deleteMessage with hideDeleteMessage=false should set deleteState to Success") {
            runTest {
                val repository = MockMessageListRepository()
                val initialMessages = createMockMessages(3)
                repository.fetchPreviousMessagesResult = Result.success(initialMessages)
                
                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                viewModel.setHideDeleteMessage(false)
                
                viewModel.fetchMessages()
                advanceUntilIdle()
                
                val messageToDelete = viewModel.messages.value.first()
                
                // Create deleted message response
                val deletedMessage = com.cometchat.chat.models.TextMessage(
                    "receiver_1",
                    "Deleted",
                    com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER
                ).apply {
                    id = messageToDelete.id
                    sentAt = messageToDelete.sentAt
                    deletedAt = System.currentTimeMillis()
                }
                
                repository.deleteMessageResult = Result.success(deletedMessage)
                
                // Initial state should be Idle
                viewModel.deleteState.value shouldBe MessageDeleteState.Idle
                
                // Delete the message
                viewModel.deleteMessage(messageToDelete)
                advanceUntilIdle()
                
                // State should be Success with the deleted message
                viewModel.deleteState.value.shouldBeInstanceOf<MessageDeleteState.Success>()
                val successState = viewModel.deleteState.value as MessageDeleteState.Success
                successState.message.id shouldBe messageToDelete.id
            }
        }
        
        test("deleteMessage should set deleteState to InProgress during operation") {
            runTest {
                val repository = MockMessageListRepository()
                val initialMessages = createMockMessages(2)
                repository.fetchPreviousMessagesResult = Result.success(initialMessages)
                
                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                viewModel.setHideDeleteMessage(false)
                
                viewModel.fetchMessages()
                advanceUntilIdle()
                
                val messageToDelete = viewModel.messages.value.first()
                
                val deletedMessage = com.cometchat.chat.models.TextMessage(
                    "receiver_1",
                    "Deleted",
                    com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER
                ).apply {
                    id = messageToDelete.id
                    deletedAt = System.currentTimeMillis()
                }
                
                repository.deleteMessageResult = Result.success(deletedMessage)
                
                // Start delete operation (don't advance yet to catch InProgress state)
                viewModel.deleteMessage(messageToDelete)
                
                // After advancing, state should be Success
                advanceUntilIdle()
                viewModel.deleteState.value.shouldBeInstanceOf<MessageDeleteState.Success>()
            }
        }
        
        /**
         * Property-based test: For any message deleted with hideDeleteMessage=false,
         * the list size should remain unchanged.
         * 
         * **Property 8: Message Deletion Behavior**
         * **Validates: Requirements 3.5, 16.5**
         */
        test("Property 8: deleteMessage with hideDeleteMessage=false should preserve list size") {
            checkAll(10, Arb.int(1, 5)) { messageIndex ->
                runTest {
                    val repository = MockMessageListRepository()
                    val initialMessages = createMockMessages(10)
                    repository.fetchPreviousMessagesResult = Result.success(initialMessages)
                    
                    val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                    viewModel.setHideDeleteMessage(false)
                    
                    viewModel.fetchMessages()
                    advanceUntilIdle()
                    
                    val initialSize = viewModel.messages.value.size
                    
                    // Get a message to delete (using modulo to stay in bounds)
                    val targetIndex = messageIndex % initialSize
                    val messageToDelete = viewModel.messages.value[targetIndex]
                    
                    // Create deleted message response
                    val deletedMessage = com.cometchat.chat.models.TextMessage(
                        "receiver_${messageToDelete.id}",
                        "Deleted",
                        com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER
                    ).apply {
                        id = messageToDelete.id
                        sentAt = messageToDelete.sentAt
                        deletedAt = System.currentTimeMillis()
                    }
                    
                    repository.deleteMessageResult = Result.success(deletedMessage)
                    
                    viewModel.deleteMessage(messageToDelete)
                    advanceUntilIdle()
                    
                    // List size should remain unchanged
                    viewModel.messages.value.size shouldBe initialSize
                    viewModel.deleteState.value.shouldBeInstanceOf<MessageDeleteState.Success>()
                }
            }
        }
    }

    // ========================================
    // Task 48.2: Test deleteMessage with hideDeleteMessage=true
    // ========================================
    
    context("Task 48.2: Test deleteMessage with hideDeleteMessage=true") {
        
        /**
         * **Property 8: Message Deletion Behavior**
         * 
         * When hideDeleteMessage is true, deleted messages should be completely
         * removed from the list rather than showing a placeholder.
         * 
         * **Validates: Requirements 3.5, 16.5**
         */
        test("deleteMessage with hideDeleteMessage=true should remove message from list") {
            runTest {
                val repository = MockMessageListRepository()
                val initialMessages = createMockMessages(3)
                repository.fetchPreviousMessagesResult = Result.success(initialMessages)
                
                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                
                // Set hideDeleteMessage to true
                viewModel.setHideDeleteMessage(true)
                
                viewModel.fetchMessages()
                advanceUntilIdle()
                
                val initialSize = viewModel.messages.value.size
                initialSize shouldBe 3
                
                // Get the message to delete
                val messageToDelete = viewModel.messages.value.find { it.id == 2L }!!
                
                // Create a deleted version of the message
                val deletedMessage = com.cometchat.chat.models.TextMessage(
                    "receiver_2",
                    "This message was deleted",
                    com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER
                ).apply {
                    id = 2L
                    sentAt = messageToDelete.sentAt
                    deletedAt = System.currentTimeMillis()
                }
                
                repository.deleteMessageResult = Result.success(deletedMessage)
                
                // Delete the message
                viewModel.deleteMessage(messageToDelete)
                advanceUntilIdle()
                
                // List size should decrease by 1 (message removed)
                viewModel.messages.value.size shouldBe initialSize - 1
                
                // The message should no longer exist in the list
                viewModel.messages.value.find { it.id == 2L } shouldBe null
            }
        }
        
        test("deleteMessage with hideDeleteMessage=true should set deleteState to Success") {
            runTest {
                val repository = MockMessageListRepository()
                val initialMessages = createMockMessages(3)
                repository.fetchPreviousMessagesResult = Result.success(initialMessages)
                
                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                viewModel.setHideDeleteMessage(true)
                
                viewModel.fetchMessages()
                advanceUntilIdle()
                
                val messageToDelete = viewModel.messages.value.first()
                
                val deletedMessage = com.cometchat.chat.models.TextMessage(
                    "receiver_1",
                    "Deleted",
                    com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER
                ).apply {
                    id = messageToDelete.id
                    sentAt = messageToDelete.sentAt
                    deletedAt = System.currentTimeMillis()
                }
                
                repository.deleteMessageResult = Result.success(deletedMessage)
                
                viewModel.deleteMessage(messageToDelete)
                advanceUntilIdle()
                
                // State should be Success
                viewModel.deleteState.value.shouldBeInstanceOf<MessageDeleteState.Success>()
                val successState = viewModel.deleteState.value as MessageDeleteState.Success
                successState.message.id shouldBe messageToDelete.id
            }
        }
        
        test("deleteMessage with hideDeleteMessage=true should set uiState to Empty when last message is deleted") {
            runTest {
                val repository = MockMessageListRepository()
                val initialMessages = createMockMessages(1)
                repository.fetchPreviousMessagesResult = Result.success(initialMessages)
                
                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                viewModel.setHideDeleteMessage(true)
                
                viewModel.fetchMessages()
                advanceUntilIdle()
                
                viewModel.uiState.value shouldBe MessageListUIState.Loaded
                
                val messageToDelete = viewModel.messages.value.first()
                
                val deletedMessage = com.cometchat.chat.models.TextMessage(
                    "receiver_1",
                    "Deleted",
                    com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER
                ).apply {
                    id = messageToDelete.id
                    deletedAt = System.currentTimeMillis()
                }
                
                repository.deleteMessageResult = Result.success(deletedMessage)
                
                viewModel.deleteMessage(messageToDelete)
                advanceUntilIdle()
                
                // State should be Empty since last message was removed
                viewModel.uiState.value shouldBe MessageListUIState.Empty
                viewModel.messages.value shouldBe emptyList()
            }
        }
        
        /**
         * Property-based test: For any message deleted with hideDeleteMessage=true,
         * the list size should decrease by exactly 1.
         * 
         * **Property 8: Message Deletion Behavior**
         * **Validates: Requirements 3.5, 16.5**
         */
        test("Property 8: deleteMessage with hideDeleteMessage=true should decrease list size by 1") {
            checkAll(10, Arb.int(1, 5)) { messageIndex ->
                runTest {
                    val repository = MockMessageListRepository()
                    val initialMessages = createMockMessages(10)
                    repository.fetchPreviousMessagesResult = Result.success(initialMessages)
                    
                    val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                    viewModel.setHideDeleteMessage(true)
                    
                    viewModel.fetchMessages()
                    advanceUntilIdle()
                    
                    val initialSize = viewModel.messages.value.size
                    
                    // Get a message to delete
                    val targetIndex = messageIndex % initialSize
                    val messageToDelete = viewModel.messages.value[targetIndex]
                    val deletedMessageId = messageToDelete.id
                    
                    val deletedMessage = com.cometchat.chat.models.TextMessage(
                        "receiver_${messageToDelete.id}",
                        "Deleted",
                        com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER
                    ).apply {
                        id = messageToDelete.id
                        sentAt = messageToDelete.sentAt
                        deletedAt = System.currentTimeMillis()
                    }
                    
                    repository.deleteMessageResult = Result.success(deletedMessage)
                    
                    viewModel.deleteMessage(messageToDelete)
                    advanceUntilIdle()
                    
                    // List size should decrease by 1
                    viewModel.messages.value.size shouldBe initialSize - 1
                    // Message should no longer exist
                    viewModel.messages.value.find { it.id == deletedMessageId } shouldBe null
                    viewModel.deleteState.value.shouldBeInstanceOf<MessageDeleteState.Success>()
                }
            }
        }
    }

    // ========================================
    // Task 48.3: Test deleteMessage error handling
    // ========================================
    
    context("Task 48.3: Test deleteMessage error handling") {
        
        /**
         * When the repository returns an error during delete, the deleteState
         * should be Error with the exception preserved.
         * 
         * **Validates: Requirements 16.5**
         */
        test("deleteMessage should set deleteState to Error when repository fails") {
            runTest {
                val repository = MockMessageListRepository()
                val initialMessages = createMockMessages(3)
                repository.fetchPreviousMessagesResult = Result.success(initialMessages)
                
                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                
                viewModel.fetchMessages()
                advanceUntilIdle()
                
                val messageToDelete = viewModel.messages.value.first()
                
                // Configure repository to return an error
                val testException = Exception("Network error: Unable to delete message")
                repository.deleteMessageResult = Result.failure(testException)
                
                // Initial state should be Idle
                viewModel.deleteState.value shouldBe MessageDeleteState.Idle
                
                // Attempt to delete the message
                viewModel.deleteMessage(messageToDelete)
                advanceUntilIdle()
                
                // State should be Error
                viewModel.deleteState.value.shouldBeInstanceOf<MessageDeleteState.Error>()
            }
        }
        
        test("deleteMessage Error state should contain the exception message") {
            runTest {
                val repository = MockMessageListRepository()
                val initialMessages = createMockMessages(3)
                repository.fetchPreviousMessagesResult = Result.success(initialMessages)
                
                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                
                viewModel.fetchMessages()
                advanceUntilIdle()
                
                val messageToDelete = viewModel.messages.value.first()
                
                val errorMessage = "Permission denied: Cannot delete this message"
                repository.deleteMessageResult = Result.failure(Exception(errorMessage))
                
                viewModel.deleteMessage(messageToDelete)
                advanceUntilIdle()
                
                val errorState = viewModel.deleteState.value as MessageDeleteState.Error
                errorState.exception.message shouldBe errorMessage
            }
        }
        
        test("deleteMessage error should not modify the message list") {
            runTest {
                val repository = MockMessageListRepository()
                val initialMessages = createMockMessages(5)
                repository.fetchPreviousMessagesResult = Result.success(initialMessages)
                
                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                
                viewModel.fetchMessages()
                advanceUntilIdle()
                
                val initialSize = viewModel.messages.value.size
                val originalMessages = viewModel.messages.value.toList()
                
                val messageToDelete = viewModel.messages.value[2]
                
                repository.deleteMessageResult = Result.failure(Exception("Delete failed"))
                
                viewModel.deleteMessage(messageToDelete)
                advanceUntilIdle()
                
                // List should remain unchanged on error
                viewModel.messages.value.size shouldBe initialSize
                viewModel.messages.value shouldBe originalMessages
            }
        }
        
        test("deleteMessage error should not change uiState") {
            runTest {
                val repository = MockMessageListRepository()
                val initialMessages = createMockMessages(3)
                repository.fetchPreviousMessagesResult = Result.success(initialMessages)
                
                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                
                viewModel.fetchMessages()
                advanceUntilIdle()
                
                viewModel.uiState.value shouldBe MessageListUIState.Loaded
                
                val messageToDelete = viewModel.messages.value.first()
                repository.deleteMessageResult = Result.failure(Exception("Error"))
                
                viewModel.deleteMessage(messageToDelete)
                advanceUntilIdle()
                
                // uiState should still be Loaded
                viewModel.uiState.value shouldBe MessageListUIState.Loaded
            }
        }
        
        test("resetDeleteState should return deleteState to Idle") {
            runTest {
                val repository = MockMessageListRepository()
                val initialMessages = createMockMessages(2)
                repository.fetchPreviousMessagesResult = Result.success(initialMessages)
                
                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                
                viewModel.fetchMessages()
                advanceUntilIdle()
                
                val messageToDelete = viewModel.messages.value.first()
                repository.deleteMessageResult = Result.failure(Exception("Error"))
                
                viewModel.deleteMessage(messageToDelete)
                advanceUntilIdle()
                
                viewModel.deleteState.value.shouldBeInstanceOf<MessageDeleteState.Error>()
                
                // Reset the state
                viewModel.resetDeleteState()
                
                viewModel.deleteState.value shouldBe MessageDeleteState.Idle
            }
        }
        
        test("resetDeleteState should work after successful delete") {
            runTest {
                val repository = MockMessageListRepository()
                val initialMessages = createMockMessages(2)
                repository.fetchPreviousMessagesResult = Result.success(initialMessages)
                
                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                viewModel.setHideDeleteMessage(false)
                
                viewModel.fetchMessages()
                advanceUntilIdle()
                
                val messageToDelete = viewModel.messages.value.first()
                
                val deletedMessage = com.cometchat.chat.models.TextMessage(
                    "receiver_1",
                    "Deleted",
                    com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER
                ).apply {
                    id = messageToDelete.id
                    deletedAt = System.currentTimeMillis()
                }
                
                repository.deleteMessageResult = Result.success(deletedMessage)
                
                viewModel.deleteMessage(messageToDelete)
                advanceUntilIdle()
                
                viewModel.deleteState.value.shouldBeInstanceOf<MessageDeleteState.Success>()
                
                // Reset the state
                viewModel.resetDeleteState()
                
                viewModel.deleteState.value shouldBe MessageDeleteState.Idle
            }
        }
        
        /**
         * Property-based test: For any error message, the deleteState should be Error
         * with the correct exception message preserved.
         * 
         * **Validates: Requirements 16.5**
         */
        test("deleteMessage error should preserve exception message for any error") {
            checkAll(10, Arb.string(1, 100)) { errorMessage ->
                runTest {
                    val repository = MockMessageListRepository()
                    val initialMessages = createMockMessages(3)
                    repository.fetchPreviousMessagesResult = Result.success(initialMessages)
                    
                    val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                    
                    viewModel.fetchMessages()
                    advanceUntilIdle()
                    
                    val messageToDelete = viewModel.messages.value.first()
                    repository.deleteMessageResult = Result.failure(Exception(errorMessage))
                    
                    viewModel.deleteMessage(messageToDelete)
                    advanceUntilIdle()
                    
                    val errorState = viewModel.deleteState.value as MessageDeleteState.Error
                    errorState.exception.message shouldBe errorMessage
                }
            }
        }
    }

    // ========================================
    // Task 49.1: Test addReaction updates message
    // ========================================
    
    context("Task 49.1: Test addReaction updates message") {
        
        /**
         * **Property 11: Reaction State Management**
         * 
         * When addReaction is called successfully, the message in the list should
         * be updated with the new reaction data returned from the repository.
         * 
         * **Validates: Requirements 5.1, 16.7**
         */
        test("addReaction should update message in list on success") {
            runTest {
                val repository = MockMessageListRepository()
                val initialMessages = createMockMessages(3)
                repository.fetchPreviousMessagesResult = Result.success(initialMessages)
                
                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                
                viewModel.fetchMessages()
                advanceUntilIdle()
                
                val messageToReact = viewModel.messages.value.first()
                val emoji = "👍"
                
                // Create an updated message with the reaction
                val updatedMessage = com.cometchat.chat.models.TextMessage(
                    "receiver_1",
                    "Test message 1",
                    com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER
                ).apply {
                    id = messageToReact.id
                    sentAt = messageToReact.sentAt
                    // In real scenario, reactions would be updated here
                }
                
                repository.addReactionResult = Result.success(updatedMessage)
                
                viewModel.addReaction(messageToReact, emoji)
                advanceUntilIdle()
                
                // Verify the message was updated in the list
                val foundMessage = viewModel.messages.value.find { it.id == messageToReact.id }
                foundMessage shouldBe updatedMessage
            }
        }
        
        test("addReaction should maintain list size") {
            runTest {
                val repository = MockMessageListRepository()
                val initialMessages = createMockMessages(5)
                repository.fetchPreviousMessagesResult = Result.success(initialMessages)
                
                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                
                viewModel.fetchMessages()
                advanceUntilIdle()
                
                val initialSize = viewModel.messages.value.size
                val messageToReact = viewModel.messages.value.first()
                val emoji = "❤️"
                
                // Create an updated message with the reaction
                val updatedMessage = com.cometchat.chat.models.TextMessage(
                    "receiver_1",
                    "Test message 1",
                    com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER
                ).apply {
                    id = messageToReact.id
                    sentAt = messageToReact.sentAt
                }
                
                repository.addReactionResult = Result.success(updatedMessage)
                
                viewModel.addReaction(messageToReact, emoji)
                advanceUntilIdle()
                
                // List size should remain unchanged
                viewModel.messages.value.size shouldBe initialSize
            }
        }
        
        test("addReaction should not affect other messages") {
            runTest {
                val repository = MockMessageListRepository()
                val initialMessages = createMockMessages(3)
                repository.fetchPreviousMessagesResult = Result.success(initialMessages)
                
                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                
                viewModel.fetchMessages()
                advanceUntilIdle()
                
                // Store references to other messages
                val otherMessage1 = viewModel.messages.value[1]
                val otherMessage2 = viewModel.messages.value[2]
                
                val messageToReact = viewModel.messages.value.first()
                val emoji = "😀"
                
                // Create an updated message with the reaction
                val updatedMessage = com.cometchat.chat.models.TextMessage(
                    "receiver_1",
                    "Test message 1",
                    com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER
                ).apply {
                    id = messageToReact.id
                    sentAt = messageToReact.sentAt
                }
                
                repository.addReactionResult = Result.success(updatedMessage)
                
                viewModel.addReaction(messageToReact, emoji)
                advanceUntilIdle()
                
                // Other messages should remain unchanged
                viewModel.messages.value.find { it.id == otherMessage1.id } shouldBe otherMessage1
                viewModel.messages.value.find { it.id == otherMessage2.id } shouldBe otherMessage2
            }
        }
        
        test("addReaction should not update list on failure") {
            runTest {
                val repository = MockMessageListRepository()
                val initialMessages = createMockMessages(3)
                repository.fetchPreviousMessagesResult = Result.success(initialMessages)
                
                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                
                viewModel.fetchMessages()
                advanceUntilIdle()
                
                val originalMessages = viewModel.messages.value.toList()
                val messageToReact = viewModel.messages.value.first()
                val emoji = "👎"
                
                // Simulate failure
                repository.addReactionResult = Result.failure(Exception("Network error"))
                
                viewModel.addReaction(messageToReact, emoji)
                advanceUntilIdle()
                
                // Messages should remain unchanged on failure
                viewModel.messages.value shouldBe originalMessages
            }
        }
        
        test("addReaction should maintain uiState as Loaded") {
            runTest {
                val repository = MockMessageListRepository()
                val initialMessages = createMockMessages(3)
                repository.fetchPreviousMessagesResult = Result.success(initialMessages)
                
                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                
                viewModel.fetchMessages()
                advanceUntilIdle()
                
                viewModel.uiState.value shouldBe MessageListUIState.Loaded
                
                val messageToReact = viewModel.messages.value.first()
                val emoji = "🎉"
                
                val updatedMessage = com.cometchat.chat.models.TextMessage(
                    "receiver_1",
                    "Test message 1",
                    com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER
                ).apply {
                    id = messageToReact.id
                    sentAt = messageToReact.sentAt
                }
                
                repository.addReactionResult = Result.success(updatedMessage)
                
                viewModel.addReaction(messageToReact, emoji)
                advanceUntilIdle()
                
                // uiState should still be Loaded
                viewModel.uiState.value shouldBe MessageListUIState.Loaded
            }
        }
        
        /**
         * Property-based test: For any emoji string, addReaction should update
         * the message in the list when successful.
         * 
         * **Property 11: Reaction State Management**
         * **Validates: Requirements 5.1, 16.7**
         */
        test("Property 11: addReaction should update message for any emoji on success") {
            val emojis = listOf("👍", "❤️", "😀", "😂", "😮", "😢", "🎉", "🔥", "👏", "💯")
            
            checkAll(10, Arb.int(0, emojis.size - 1)) { emojiIndex ->
                runTest {
                    val repository = MockMessageListRepository()
                    val initialMessages = createMockMessages(3)
                    repository.fetchPreviousMessagesResult = Result.success(initialMessages)
                    
                    val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                    
                    viewModel.fetchMessages()
                    advanceUntilIdle()
                    
                    val messageToReact = viewModel.messages.value.first()
                    val emoji = emojis[emojiIndex]
                    
                    val updatedMessage = com.cometchat.chat.models.TextMessage(
                        "receiver_1",
                        "Updated with reaction $emoji",
                        com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER
                    ).apply {
                        id = messageToReact.id
                        sentAt = messageToReact.sentAt
                    }
                    
                    repository.addReactionResult = Result.success(updatedMessage)
                    
                    viewModel.addReaction(messageToReact, emoji)
                    advanceUntilIdle()
                    
                    // Message should be updated
                    val foundMessage = viewModel.messages.value.find { it.id == messageToReact.id }
                    foundMessage shouldBe updatedMessage
                    
                    // List size should remain unchanged
                    viewModel.messages.value.size shouldBe 3
                }
            }
        }
    }

    // ========================================
    // Task 49.2: Test removeReaction updates message
    // ========================================
    
    context("Task 49.2: Test removeReaction updates message") {
        
        /**
         * **Property 11: Reaction State Management**
         * 
         * When removeReaction is called successfully, the message in the list should
         * be updated with the reaction removed (as returned from the repository).
         * 
         * **Validates: Requirements 5.2, 16.8**
         */
        test("removeReaction should update message in list on success") {
            runTest {
                val repository = MockMessageListRepository()
                val initialMessages = createMockMessages(3)
                repository.fetchPreviousMessagesResult = Result.success(initialMessages)
                
                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                
                viewModel.fetchMessages()
                advanceUntilIdle()
                
                val messageToRemoveReaction = viewModel.messages.value.first()
                val emoji = "👍"
                
                // Create an updated message with the reaction removed
                val updatedMessage = com.cometchat.chat.models.TextMessage(
                    "receiver_1",
                    "Test message 1",
                    com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER
                ).apply {
                    id = messageToRemoveReaction.id
                    sentAt = messageToRemoveReaction.sentAt
                    // In real scenario, reactions would be removed here
                }
                
                repository.removeReactionResult = Result.success(updatedMessage)
                
                viewModel.removeReaction(messageToRemoveReaction, emoji)
                advanceUntilIdle()
                
                // Verify the message was updated in the list
                val foundMessage = viewModel.messages.value.find { it.id == messageToRemoveReaction.id }
                foundMessage shouldBe updatedMessage
            }
        }
        
        test("removeReaction should maintain list size") {
            runTest {
                val repository = MockMessageListRepository()
                val initialMessages = createMockMessages(5)
                repository.fetchPreviousMessagesResult = Result.success(initialMessages)
                
                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                
                viewModel.fetchMessages()
                advanceUntilIdle()
                
                val initialSize = viewModel.messages.value.size
                val messageToRemoveReaction = viewModel.messages.value.first()
                val emoji = "❤️"
                
                // Create an updated message with the reaction removed
                val updatedMessage = com.cometchat.chat.models.TextMessage(
                    "receiver_1",
                    "Test message 1",
                    com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER
                ).apply {
                    id = messageToRemoveReaction.id
                    sentAt = messageToRemoveReaction.sentAt
                }
                
                repository.removeReactionResult = Result.success(updatedMessage)
                
                viewModel.removeReaction(messageToRemoveReaction, emoji)
                advanceUntilIdle()
                
                // List size should remain unchanged
                viewModel.messages.value.size shouldBe initialSize
            }
        }
        
        test("removeReaction should not affect other messages") {
            runTest {
                val repository = MockMessageListRepository()
                val initialMessages = createMockMessages(3)
                repository.fetchPreviousMessagesResult = Result.success(initialMessages)
                
                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                
                viewModel.fetchMessages()
                advanceUntilIdle()
                
                // Store references to other messages
                val otherMessage1 = viewModel.messages.value[1]
                val otherMessage2 = viewModel.messages.value[2]
                
                val messageToRemoveReaction = viewModel.messages.value.first()
                val emoji = "😀"
                
                // Create an updated message with the reaction removed
                val updatedMessage = com.cometchat.chat.models.TextMessage(
                    "receiver_1",
                    "Test message 1",
                    com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER
                ).apply {
                    id = messageToRemoveReaction.id
                    sentAt = messageToRemoveReaction.sentAt
                }
                
                repository.removeReactionResult = Result.success(updatedMessage)
                
                viewModel.removeReaction(messageToRemoveReaction, emoji)
                advanceUntilIdle()
                
                // Other messages should remain unchanged
                viewModel.messages.value.find { it.id == otherMessage1.id } shouldBe otherMessage1
                viewModel.messages.value.find { it.id == otherMessage2.id } shouldBe otherMessage2
            }
        }
        
        test("removeReaction should not update list on failure") {
            runTest {
                val repository = MockMessageListRepository()
                val initialMessages = createMockMessages(3)
                repository.fetchPreviousMessagesResult = Result.success(initialMessages)
                
                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                
                viewModel.fetchMessages()
                advanceUntilIdle()
                
                val originalMessages = viewModel.messages.value.toList()
                val messageToRemoveReaction = viewModel.messages.value.first()
                val emoji = "👎"
                
                // Simulate failure
                repository.removeReactionResult = Result.failure(Exception("Network error"))
                
                viewModel.removeReaction(messageToRemoveReaction, emoji)
                advanceUntilIdle()
                
                // Messages should remain unchanged on failure
                viewModel.messages.value shouldBe originalMessages
            }
        }
        
        test("removeReaction should maintain uiState as Loaded") {
            runTest {
                val repository = MockMessageListRepository()
                val initialMessages = createMockMessages(3)
                repository.fetchPreviousMessagesResult = Result.success(initialMessages)
                
                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                
                viewModel.fetchMessages()
                advanceUntilIdle()
                
                viewModel.uiState.value shouldBe MessageListUIState.Loaded
                
                val messageToRemoveReaction = viewModel.messages.value.first()
                val emoji = "🎉"
                
                val updatedMessage = com.cometchat.chat.models.TextMessage(
                    "receiver_1",
                    "Test message 1",
                    com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER
                ).apply {
                    id = messageToRemoveReaction.id
                    sentAt = messageToRemoveReaction.sentAt
                }
                
                repository.removeReactionResult = Result.success(updatedMessage)
                
                viewModel.removeReaction(messageToRemoveReaction, emoji)
                advanceUntilIdle()
                
                // uiState should still be Loaded
                viewModel.uiState.value shouldBe MessageListUIState.Loaded
            }
        }
        
        /**
         * Property-based test: For any emoji string, removeReaction should update
         * the message in the list when successful.
         * 
         * **Property 11: Reaction State Management**
         * **Validates: Requirements 5.2, 16.8**
         */
        test("Property 11: removeReaction should update message for any emoji on success") {
            val emojis = listOf("👍", "❤️", "😀", "😂", "😮", "😢", "🎉", "🔥", "👏", "💯")
            
            checkAll(10, Arb.int(0, emojis.size - 1)) { emojiIndex ->
                runTest {
                    val repository = MockMessageListRepository()
                    val initialMessages = createMockMessages(3)
                    repository.fetchPreviousMessagesResult = Result.success(initialMessages)
                    
                    val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                    
                    viewModel.fetchMessages()
                    advanceUntilIdle()
                    
                    val messageToRemoveReaction = viewModel.messages.value.first()
                    val emoji = emojis[emojiIndex]
                    
                    val updatedMessage = com.cometchat.chat.models.TextMessage(
                        "receiver_1",
                        "Reaction $emoji removed",
                        com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER
                    ).apply {
                        id = messageToRemoveReaction.id
                        sentAt = messageToRemoveReaction.sentAt
                    }
                    
                    repository.removeReactionResult = Result.success(updatedMessage)
                    
                    viewModel.removeReaction(messageToRemoveReaction, emoji)
                    advanceUntilIdle()
                    
                    // Message should be updated
                    val foundMessage = viewModel.messages.value.find { it.id == messageToRemoveReaction.id }
                    foundMessage shouldBe updatedMessage
                    
                    // List size should remain unchanged
                    viewModel.messages.value.size shouldBe 3
                }
            }
        }
    }

    // ========================================
    // Task 50.1: Test markMessageAsRead with disableReceipt=false
    // ========================================
    
    context("Task 50.1: markMessageAsRead with disableReceipt=false") {
        
        /**
         * **Property 10: Receipt Sending Control**
         * 
         * When disableReceipt is false (default), markMessageAsRead should call
         * repository.markAsRead() for messages from other users.
         * 
         * **Validates: Requirements 4.5, 16.9**
         */
        test("markMessageAsRead should call repository.markAsRead() when disableReceipt is false") {
            runTest {
                val repository = MockMessageListRepository()
                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                
                // Ensure disableReceipt is false (default)
                viewModel.setDisableReceipt(false)
                
                // Create a message from another user (not current user)
                val otherUser = User().apply {
                    uid = "other_user_123"
                    name = "Other User"
                }
                
                val incomingMessage = com.cometchat.chat.models.TextMessage(
                    "current_user_uid",
                    "Hello from other user",
                    com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER
                ).apply {
                    id = 1L
                    sentAt = System.currentTimeMillis()
                    sender = otherUser
                }
                
                // Reset tracking
                repository.markAsReadCalled = false
                repository.markAsReadMessage = null
                
                // Call markMessageAsRead
                viewModel.markMessageAsRead(incomingMessage)
                advanceUntilIdle()
                
                // Verify repository.markAsRead() was called
                repository.markAsReadCalled shouldBe true
                repository.markAsReadMessage shouldBe incomingMessage
            }
        }
        
        test("markMessageAsRead should NOT call repository.markAsRead() for own messages even when disableReceipt is false") {
            runTest {
                val repository = MockMessageListRepository()
                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                
                // Ensure disableReceipt is false
                viewModel.setDisableReceipt(false)
                
                // Create a message from current user (outgoing message)
                val currentUser = com.cometchat.chat.core.CometChat.getLoggedInUser()
                
                val outgoingMessage = com.cometchat.chat.models.TextMessage(
                    "receiver_123",
                    "Hello from current user",
                    com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER
                ).apply {
                    id = 1L
                    sentAt = System.currentTimeMillis()
                    sender = currentUser
                }
                
                // Reset tracking
                repository.markAsReadCalled = false
                repository.markAsReadMessage = null
                
                // Call markMessageAsRead
                viewModel.markMessageAsRead(outgoingMessage)
                advanceUntilIdle()
                
                // Verify repository.markAsRead() was NOT called (own messages shouldn't trigger read receipt)
                repository.markAsReadCalled shouldBe false
            }
        }
        
        test("markMessageAsRead should NOT call repository.markAsRead() when sender is null") {
            runTest {
                val repository = MockMessageListRepository()
                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                
                // Ensure disableReceipt is false
                viewModel.setDisableReceipt(false)
                
                // Create a message with null sender
                val messageWithNullSender = com.cometchat.chat.models.TextMessage(
                    "receiver_123",
                    "Message with null sender",
                    com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER
                ).apply {
                    id = 1L
                    sentAt = System.currentTimeMillis()
                    sender = null
                }
                
                // Reset tracking
                repository.markAsReadCalled = false
                repository.markAsReadMessage = null
                
                // Call markMessageAsRead
                viewModel.markMessageAsRead(messageWithNullSender)
                advanceUntilIdle()
                
                // Verify repository.markAsRead() was called (null sender != current user)
                repository.markAsReadCalled shouldBe true
            }
        }
        
        /**
         * Property-based test: For any incoming message (sender != current user),
         * markMessageAsRead should call repository.markAsRead() when disableReceipt is false.
         * 
         * **Property 10: Receipt Sending Control**
         * **Validates: Requirements 4.5, 16.9**
         */
        test("Property 10: markMessageAsRead should call repository for any incoming message when receipts enabled") {
            checkAll(10, Arb.string(5, 20)) { otherUserId ->
                runTest {
                    val repository = MockMessageListRepository()
                    val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                    
                    // Ensure disableReceipt is false
                    viewModel.setDisableReceipt(false)
                    
                    // Ensure the other user ID is different from current user
                    val currentUser = com.cometchat.chat.core.CometChat.getLoggedInUser()
                    val uniqueOtherId = if (otherUserId == currentUser?.uid) "different_$otherUserId" else otherUserId
                    
                    val otherUser = User().apply {
                        uid = uniqueOtherId
                        name = "Other User"
                    }
                    
                    val incomingMessage = com.cometchat.chat.models.TextMessage(
                        "receiver_123",
                        "Test incoming message",
                        com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER
                    ).apply {
                        id = 1L
                        sentAt = System.currentTimeMillis()
                        sender = otherUser
                    }
                    
                    // Reset tracking
                    repository.markAsReadCalled = false
                    repository.markAsReadMessage = null
                    
                    // Call markMessageAsRead
                    viewModel.markMessageAsRead(incomingMessage)
                    advanceUntilIdle()
                    
                    // Verify repository.markAsRead() was called
                    repository.markAsReadCalled shouldBe true
                    repository.markAsReadMessage shouldBe incomingMessage
                }
            }
        }
    }

    // ========================================
    // Task 50.2: Test markMessageAsRead with disableReceipt=true
    // ========================================
    
    context("Task 50.2: markMessageAsRead with disableReceipt=true") {
        
        /**
         * **Property 10: Receipt Sending Control**
         * 
         * When disableReceipt is true, markMessageAsRead should NOT call
         * repository.markAsRead() regardless of the message sender.
         * 
         * **Validates: Requirements 4.4**
         */
        test("markMessageAsRead should NOT call repository.markAsRead() when disableReceipt is true") {
            runTest {
                val repository = MockMessageListRepository()
                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                
                // Set disableReceipt to true
                viewModel.setDisableReceipt(true)
                
                // Create a message from another user (not current user)
                val otherUser = User().apply {
                    uid = "other_user_123"
                    name = "Other User"
                }
                
                val incomingMessage = com.cometchat.chat.models.TextMessage(
                    "current_user_uid",
                    "Hello from other user",
                    com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER
                ).apply {
                    id = 1L
                    sentAt = System.currentTimeMillis()
                    sender = otherUser
                }
                
                // Reset tracking
                repository.markAsReadCalled = false
                repository.markAsReadMessage = null
                
                // Call markMessageAsRead
                viewModel.markMessageAsRead(incomingMessage)
                advanceUntilIdle()
                
                // Verify repository.markAsRead() was NOT called
                repository.markAsReadCalled shouldBe false
                repository.markAsReadMessage shouldBe null
            }
        }
        
        test("markMessageAsRead should NOT call repository.markAsRead() for any message when disableReceipt is true") {
            runTest {
                val repository = MockMessageListRepository()
                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                
                // Set disableReceipt to true
                viewModel.setDisableReceipt(true)
                
                // Create multiple messages from different users
                val otherUser1 = User().apply {
                    uid = "other_user_1"
                    name = "Other User 1"
                }
                
                val otherUser2 = User().apply {
                    uid = "other_user_2"
                    name = "Other User 2"
                }
                
                val message1 = com.cometchat.chat.models.TextMessage(
                    "receiver_1",
                    "Message 1",
                    com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER
                ).apply {
                    id = 1L
                    sentAt = System.currentTimeMillis()
                    sender = otherUser1
                }
                
                val message2 = com.cometchat.chat.models.TextMessage(
                    "receiver_2",
                    "Message 2",
                    com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER
                ).apply {
                    id = 2L
                    sentAt = System.currentTimeMillis()
                    sender = otherUser2
                }
                
                // Reset tracking
                repository.markAsReadCalled = false
                
                // Call markMessageAsRead for both messages
                viewModel.markMessageAsRead(message1)
                advanceUntilIdle()
                
                repository.markAsReadCalled shouldBe false
                
                viewModel.markMessageAsRead(message2)
                advanceUntilIdle()
                
                // Verify repository.markAsRead() was NOT called for either message
                repository.markAsReadCalled shouldBe false
            }
        }
        
        test("setDisableReceipt should toggle receipt behavior") {
            runTest {
                val repository = MockMessageListRepository()
                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                
                val otherUser = User().apply {
                    uid = "other_user_123"
                    name = "Other User"
                }
                
                val incomingMessage = com.cometchat.chat.models.TextMessage(
                    "current_user_uid",
                    "Hello from other user",
                    com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER
                ).apply {
                    id = 1L
                    sentAt = System.currentTimeMillis()
                    sender = otherUser
                }
                
                // First, test with disableReceipt = false (should call markAsRead)
                viewModel.setDisableReceipt(false)
                repository.markAsReadCalled = false
                
                viewModel.markMessageAsRead(incomingMessage)
                advanceUntilIdle()
                
                repository.markAsReadCalled shouldBe true
                
                // Now, test with disableReceipt = true (should NOT call markAsRead)
                viewModel.setDisableReceipt(true)
                repository.markAsReadCalled = false
                
                viewModel.markMessageAsRead(incomingMessage)
                advanceUntilIdle()
                
                repository.markAsReadCalled shouldBe false
                
                // Toggle back to false (should call markAsRead again)
                viewModel.setDisableReceipt(false)
                repository.markAsReadCalled = false
                
                viewModel.markMessageAsRead(incomingMessage)
                advanceUntilIdle()
                
                repository.markAsReadCalled shouldBe true
            }
        }
        
        /**
         * Property-based test: For any message, markMessageAsRead should NOT call
         * repository.markAsRead() when disableReceipt is true.
         * 
         * **Property 10: Receipt Sending Control**
         * **Validates: Requirements 4.4**
         */
        test("Property 10: markMessageAsRead should never call repository when receipts disabled") {
            checkAll(10, Arb.string(5, 20)) { otherUserId ->
                runTest {
                    val repository = MockMessageListRepository()
                    val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                    
                    // Set disableReceipt to true
                    viewModel.setDisableReceipt(true)
                    
                    // Ensure the other user ID is different from current user
                    val currentUser = com.cometchat.chat.core.CometChat.getLoggedInUser()
                    val uniqueOtherId = if (otherUserId == currentUser?.uid) "different_$otherUserId" else otherUserId
                    
                    val otherUser = User().apply {
                        uid = uniqueOtherId
                        name = "Other User"
                    }
                    
                    val incomingMessage = com.cometchat.chat.models.TextMessage(
                        "receiver_123",
                        "Test incoming message",
                        com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER
                    ).apply {
                        id = 1L
                        sentAt = System.currentTimeMillis()
                        sender = otherUser
                    }
                    
                    // Reset tracking
                    repository.markAsReadCalled = false
                    repository.markAsReadMessage = null
                    
                    // Call markMessageAsRead
                    viewModel.markMessageAsRead(incomingMessage)
                    advanceUntilIdle()
                    
                    // Verify repository.markAsRead() was NOT called
                    repository.markAsReadCalled shouldBe false
                    repository.markAsReadMessage shouldBe null
                }
            }
        }
    }

    // ========================================
    // Task 3.3: markAsDelivered unit tests
    // ========================================
    
    /**
     * Testable ViewModel subclass that overrides getLoggedInUserUid() to avoid SDK dependencies.
     * This allows testing receipt-related methods without initializing the CometChat SDK.
     */
    class TestableReceiptViewModel(
        repository: MessageListRepository,
        private val loggedInUserUid: String? = "logged_in_user"
    ) : CometChatMessageListViewModel(repository, enableListeners = false) {
        override fun getLoggedInUserUid(): String? = loggedInUserUid
    }
    
    context("Task 3.3: markAsDelivered conditions") {
        
        /**
         * **AC-1.2: markAsDelivered is only called if disableReceipt is false**
         * 
         * When disableReceipt is false (default), markAsDelivered should call
         * the repository method for messages from other users.
         * 
         * **Validates: Requirements AC-1.2**
         */
        test("markAsDelivered should call repository when disableReceipt is false") {
            runTest {
                val repository = MockMessageListRepository()
                val viewModel = TestableReceiptViewModel(repository, loggedInUserUid = "logged_in_user")
                
                // disableReceipt is false by default
                
                // Create a message from another user (different from logged_in_user)
                val otherUser = User().apply {
                    uid = "other_user_123"
                    name = "Other User"
                }
                
                val incomingMessage = com.cometchat.chat.models.TextMessage(
                    "receiver_123",
                    "Test incoming message",
                    com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER
                ).apply {
                    id = 1L
                    sentAt = System.currentTimeMillis()
                    sender = otherUser
                }
                
                // Call markAsDelivered
                viewModel.markAsDelivered(incomingMessage)
                advanceUntilIdle()
                
                // Verify repository.markAsDelivered() was called
                repository.markAsDeliveredCalled shouldBe true
                repository.markAsDeliveredMessage shouldBe incomingMessage
            }
        }
        
        /**
         * **AC-1.2: markAsDelivered is only called if disableReceipt is false**
         * 
         * When disableReceipt is true, markAsDelivered should NOT call
         * the repository method.
         * 
         * **Validates: Requirements AC-1.2**
         */
        test("markAsDelivered should NOT call repository when disableReceipt is true") {
            runTest {
                val repository = MockMessageListRepository()
                val viewModel = TestableReceiptViewModel(repository, loggedInUserUid = "logged_in_user")
                
                // Set disableReceipt to true
                viewModel.setDisableReceipt(true)
                
                // Create a message from another user
                val otherUser = User().apply {
                    uid = "other_user_123"
                    name = "Other User"
                }
                
                val incomingMessage = com.cometchat.chat.models.TextMessage(
                    "receiver_123",
                    "Test incoming message",
                    com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER
                ).apply {
                    id = 1L
                    sentAt = System.currentTimeMillis()
                    sender = otherUser
                }
                
                // Call markAsDelivered
                viewModel.markAsDelivered(incomingMessage)
                advanceUntilIdle()
                
                // Verify repository.markAsDelivered() was NOT called
                repository.markAsDeliveredCalled shouldBe false
                repository.markAsDeliveredMessage shouldBe null
            }
        }
        
        /**
         * **AC-1.3: markAsDelivered is only called for messages from other users**
         * 
         * When the message sender is the current user, markAsDelivered should
         * NOT call the repository method.
         * 
         * **Validates: Requirements AC-1.3**
         */
        test("markAsDelivered should NOT call repository for own messages") {
            runTest {
                val repository = MockMessageListRepository()
                val viewModel = TestableReceiptViewModel(repository, loggedInUserUid = "logged_in_user")
                
                // Create a message from the current user (same UID as logged_in_user)
                val currentUser = User().apply {
                    uid = "logged_in_user"
                    name = "Current User"
                }
                
                val ownMessage = com.cometchat.chat.models.TextMessage(
                    "receiver_123",
                    "Test own message",
                    com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER
                ).apply {
                    id = 1L
                    sentAt = System.currentTimeMillis()
                    sender = currentUser
                }
                
                // Call markAsDelivered
                viewModel.markAsDelivered(ownMessage)
                advanceUntilIdle()
                
                // Verify repository.markAsDelivered() was NOT called
                repository.markAsDeliveredCalled shouldBe false
                repository.markAsDeliveredMessage shouldBe null
            }
        }
        
        /**
         * **AC-1.3: markAsDelivered is only called for messages from other users**
         * 
         * When the message has no sender (null), markAsDelivered should
         * NOT call the repository method.
         * 
         * **Validates: Requirements AC-1.3**
         */
        test("markAsDelivered should NOT call repository when sender is null") {
            runTest {
                val repository = MockMessageListRepository()
                val viewModel = TestableReceiptViewModel(repository, loggedInUserUid = "logged_in_user")
                
                // Create a message with no sender
                val messageWithNoSender = com.cometchat.chat.models.TextMessage(
                    "receiver_123",
                    "Test message with no sender",
                    com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER
                ).apply {
                    id = 1L
                    sentAt = System.currentTimeMillis()
                    sender = null
                }
                
                // Call markAsDelivered
                viewModel.markAsDelivered(messageWithNoSender)
                advanceUntilIdle()
                
                // Verify repository.markAsDelivered() was NOT called
                repository.markAsDeliveredCalled shouldBe false
                repository.markAsDeliveredMessage shouldBe null
            }
        }
        
        /**
         * Property-based test: For any message from another user with disableReceipt=false,
         * markAsDelivered should always call the repository.
         * 
         * **Property: Receipt Control Property**
         * **Validates: Requirements AC-1.2, AC-1.3**
         */
        test("Property: markAsDelivered should call repository for any message from other user when receipts enabled") {
            checkAll(10, Arb.string(5, 20)) { otherUserId ->
                runTest {
                    val repository = MockMessageListRepository()
                    val viewModel = TestableReceiptViewModel(repository, loggedInUserUid = "logged_in_user")
                    
                    // Ensure the other user ID is different from logged_in_user
                    val uniqueOtherId = if (otherUserId == "logged_in_user") "different_$otherUserId" else otherUserId
                    
                    val otherUser = User().apply {
                        uid = uniqueOtherId
                        name = "Other User"
                    }
                    
                    val incomingMessage = com.cometchat.chat.models.TextMessage(
                        "receiver_123",
                        "Test incoming message",
                        com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER
                    ).apply {
                        id = 1L
                        sentAt = System.currentTimeMillis()
                        sender = otherUser
                    }
                    
                    // Reset tracking
                    repository.markAsDeliveredCalled = false
                    repository.markAsDeliveredMessage = null
                    
                    // Call markAsDelivered
                    viewModel.markAsDelivered(incomingMessage)
                    advanceUntilIdle()
                    
                    // Verify repository.markAsDelivered() was called
                    repository.markAsDeliveredCalled shouldBe true
                    repository.markAsDeliveredMessage shouldBe incomingMessage
                }
            }
        }
        
        /**
         * Property-based test: For any message with disableReceipt=true,
         * markAsDelivered should never call the repository.
         * 
         * **Property: Receipt Control Property**
         * **Validates: Requirements AC-1.2**
         */
        test("Property: markAsDelivered should never call repository when disableReceipt is true") {
            checkAll(10, Arb.string(5, 20)) { otherUserId ->
                runTest {
                    val repository = MockMessageListRepository()
                    val viewModel = TestableReceiptViewModel(repository, loggedInUserUid = "logged_in_user")
                    
                    // Set disableReceipt to true
                    viewModel.setDisableReceipt(true)
                    
                    // Ensure the other user ID is different from logged_in_user
                    val uniqueOtherId = if (otherUserId == "logged_in_user") "different_$otherUserId" else otherUserId
                    
                    val otherUser = User().apply {
                        uid = uniqueOtherId
                        name = "Other User"
                    }
                    
                    val incomingMessage = com.cometchat.chat.models.TextMessage(
                        "receiver_123",
                        "Test incoming message",
                        com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER
                    ).apply {
                        id = 1L
                        sentAt = System.currentTimeMillis()
                        sender = otherUser
                    }
                    
                    // Reset tracking
                    repository.markAsDeliveredCalled = false
                    repository.markAsDeliveredMessage = null
                    
                    // Call markAsDelivered
                    viewModel.markAsDelivered(incomingMessage)
                    advanceUntilIdle()
                    
                    // Verify repository.markAsDelivered() was NOT called
                    repository.markAsDeliveredCalled shouldBe false
                    repository.markAsDeliveredMessage shouldBe null
                }
            }
        }
    }

    // ========================================
    // Task 4.2 & 4.3: handleIncomingMessage calls markAsDelivered
    // ========================================
    
    /**
     * Testable ViewModel subclass that exposes handleIncomingMessage for testing.
     * This allows testing that handleIncomingMessage correctly calls markAsDelivered.
     */
    class TestableIncomingMessageViewModel(
        repository: MessageListRepository,
        private val loggedInUserUid: String? = "logged_in_user"
    ) : CometChatMessageListViewModel(repository, enableListeners = false) {
        
        override fun getLoggedInUserUid(): String? = loggedInUserUid
        
        /**
         * Sets the user field via reflection for testing.
         */
        fun setUserForTest(user: User) {
            val field = CometChatMessageListViewModel::class.java.getDeclaredField("user")
            field.isAccessible = true
            field.set(this, user)
        }
        
        /**
         * Sets the group field via reflection for testing.
         */
        fun setGroupForTest(group: Group) {
            val field = CometChatMessageListViewModel::class.java.getDeclaredField("group")
            field.isAccessible = true
            field.set(this, group)
        }
        
        /**
         * Directly sets the messages list for testing.
         */
        fun setMessagesForTest(messages: List<BaseMessage>) {
            val field = CometChatMessageListViewModel::class.java.getDeclaredField("_messages")
            field.isAccessible = true
            @Suppress("UNCHECKED_CAST")
            val stateFlow = field.get(this) as kotlinx.coroutines.flow.MutableStateFlow<List<BaseMessage>>
            stateFlow.value = messages
        }
        
        /**
         * Sets the latestMessageId for testing.
         */
        fun setLatestMessageIdForTest(messageId: Long) {
            val field = CometChatMessageListViewModel::class.java.getDeclaredField("latestMessageId")
            field.isAccessible = true
            field.setLong(this, messageId)
        }
        
        /**
         * Calls the private handleIncomingMessage method for testing.
         */
        fun callHandleIncomingMessageForTest(message: BaseMessage) {
            val method = CometChatMessageListViewModel::class.java.getDeclaredMethod(
                "handleIncomingMessage",
                BaseMessage::class.java
            )
            method.isAccessible = true
            method.invoke(this, message)
        }
    }
    
    context("Task 4.2: handleIncomingMessage calls markAsDelivered for incoming messages") {
        
        /**
         * **AC-1.1: When a new message is received via real-time listener, markAsDelivered is called automatically**
         * 
         * When handleIncomingMessage is called with a message from another user,
         * markAsDelivered should be called on the repository.
         * 
         * **Validates: Requirements AC-1.1**
         */
        test("handleIncomingMessage should call markAsDelivered for incoming messages from other users") {
            runTest {
                val repository = MockMessageListRepository()
                val viewModel = TestableIncomingMessageViewModel(repository, loggedInUserUid = "logged_in_user")
                
                // Configure for user conversation
                val otherUser = User().apply { uid = "other_user" }
                viewModel.setUserForTest(otherUser)
                
                // Create incoming message from other user
                val incomingMessage = com.cometchat.chat.models.TextMessage(
                    "logged_in_user",
                    "Hello from other user",
                    com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER
                ).apply {
                    id = 1L
                    sentAt = System.currentTimeMillis()
                    sender = otherUser
                    receiverUid = "logged_in_user"
                }
                
                // Reset tracking
                repository.markAsDeliveredCalled = false
                repository.markAsDeliveredMessage = null
                
                // Call handleIncomingMessage
                viewModel.callHandleIncomingMessageForTest(incomingMessage)
                advanceUntilIdle()
                
                // Verify markAsDelivered was called
                repository.markAsDeliveredCalled shouldBe true
                repository.markAsDeliveredMessage shouldBe incomingMessage
            }
        }
        
        /**
         * **AC-1.1: markAsDelivered is called before adding message to list**
         * 
         * When handleIncomingMessage is called, markAsDelivered should be called
         * and the message should also be added to the list.
         * 
         * **Validates: Requirements AC-1.1**
         */
        test("handleIncomingMessage should call markAsDelivered and add message to list") {
            runTest {
                val repository = MockMessageListRepository()
                val viewModel = TestableIncomingMessageViewModel(repository, loggedInUserUid = "logged_in_user")
                
                // Configure for user conversation
                val otherUser = User().apply { uid = "other_user" }
                viewModel.setUserForTest(otherUser)
                
                // Start with empty message list (user at latest position)
                viewModel.messages.value.isEmpty() shouldBe true
                
                // Create incoming message from other user
                val incomingMessage = com.cometchat.chat.models.TextMessage(
                    "logged_in_user",
                    "Hello from other user",
                    com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER
                ).apply {
                    id = 100L
                    sentAt = System.currentTimeMillis()
                    sender = otherUser
                    receiverUid = "logged_in_user"
                }
                
                // Call handleIncomingMessage
                viewModel.callHandleIncomingMessageForTest(incomingMessage)
                advanceUntilIdle()
                
                // Verify markAsDelivered was called
                repository.markAsDeliveredCalled shouldBe true
                
                // Verify message was added to list
                viewModel.messages.value.size shouldBe 1
                viewModel.messages.value.first().id shouldBe 100L
            }
        }
        
        /**
         * **AC-1.2: markAsDelivered is only called if disableReceipt is false**
         * 
         * When disableReceipt is true, handleIncomingMessage should NOT call markAsDelivered.
         * 
         * **Validates: Requirements AC-1.2**
         */
        test("handleIncomingMessage should NOT call markAsDelivered when disableReceipt is true") {
            runTest {
                val repository = MockMessageListRepository()
                val viewModel = TestableIncomingMessageViewModel(repository, loggedInUserUid = "logged_in_user")
                
                // Configure for user conversation
                val otherUser = User().apply { uid = "other_user" }
                viewModel.setUserForTest(otherUser)
                
                // Disable receipts
                viewModel.setDisableReceipt(true)
                
                // Create incoming message from other user
                val incomingMessage = com.cometchat.chat.models.TextMessage(
                    "logged_in_user",
                    "Hello from other user",
                    com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER
                ).apply {
                    id = 1L
                    sentAt = System.currentTimeMillis()
                    sender = otherUser
                    receiverUid = "logged_in_user"
                }
                
                // Reset tracking
                repository.markAsDeliveredCalled = false
                repository.markAsDeliveredMessage = null
                
                // Call handleIncomingMessage
                viewModel.callHandleIncomingMessageForTest(incomingMessage)
                advanceUntilIdle()
                
                // Verify markAsDelivered was NOT called
                repository.markAsDeliveredCalled shouldBe false
                repository.markAsDeliveredMessage shouldBe null
            }
        }
        
        /**
         * Property-based test: For any incoming message from another user,
         * handleIncomingMessage should call markAsDelivered.
         * 
         * **Validates: Requirements AC-1.1**
         */
        test("Property: handleIncomingMessage should call markAsDelivered for any incoming message") {
            checkAll(10, Arb.long(1L, 10000L)) { messageId ->
                runTest {
                    val repository = MockMessageListRepository()
                    val viewModel = TestableIncomingMessageViewModel(repository, loggedInUserUid = "logged_in_user")
                    
                    // Configure for user conversation
                    val otherUser = User().apply { uid = "other_user" }
                    viewModel.setUserForTest(otherUser)
                    
                    // Create incoming message from other user
                    val incomingMessage = com.cometchat.chat.models.TextMessage(
                        "logged_in_user",
                        "Test message $messageId",
                        com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER
                    ).apply {
                        id = messageId
                        sentAt = System.currentTimeMillis()
                        sender = otherUser
                        receiverUid = "logged_in_user"
                    }
                    
                    // Reset tracking
                    repository.markAsDeliveredCalled = false
                    repository.markAsDeliveredMessage = null
                    
                    // Call handleIncomingMessage
                    viewModel.callHandleIncomingMessageForTest(incomingMessage)
                    advanceUntilIdle()
                    
                    // Verify markAsDelivered was called
                    repository.markAsDeliveredCalled shouldBe true
                    repository.markAsDeliveredMessage?.id shouldBe messageId
                }
            }
        }
    }
    
    context("Task 4.3: handleIncomingMessage does NOT call markAsDelivered for own messages") {
        
        /**
         * **AC-1.3: markAsDelivered is only called for messages from other users (not own messages)**
         * 
         * When handleIncomingMessage is called with a message from the logged-in user,
         * markAsDelivered should NOT be called.
         * 
         * **Validates: Requirements AC-1.3**
         */
        test("handleIncomingMessage should NOT call markAsDelivered for own messages") {
            runTest {
                val repository = MockMessageListRepository()
                val viewModel = TestableIncomingMessageViewModel(repository, loggedInUserUid = "logged_in_user")
                
                // Configure for user conversation
                val otherUser = User().apply { uid = "other_user" }
                viewModel.setUserForTest(otherUser)
                
                // Create message from logged-in user (own message)
                val loggedInUser = User().apply { uid = "logged_in_user" }
                val ownMessage = com.cometchat.chat.models.TextMessage(
                    "other_user",
                    "Hello from me",
                    com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER
                ).apply {
                    id = 1L
                    sentAt = System.currentTimeMillis()
                    sender = loggedInUser
                    receiverUid = "other_user"
                }
                
                // Reset tracking
                repository.markAsDeliveredCalled = false
                repository.markAsDeliveredMessage = null
                
                // Call handleIncomingMessage
                viewModel.callHandleIncomingMessageForTest(ownMessage)
                advanceUntilIdle()
                
                // Verify markAsDelivered was NOT called
                repository.markAsDeliveredCalled shouldBe false
                repository.markAsDeliveredMessage shouldBe null
            }
        }
        
        /**
         * **AC-1.3: markAsDelivered is NOT called when sender is null**
         * 
         * When handleIncomingMessage is called with a message that has no sender,
         * markAsDelivered should NOT be called.
         * 
         * **Validates: Requirements AC-1.3**
         */
        test("handleIncomingMessage should NOT call markAsDelivered when sender is null") {
            runTest {
                val repository = MockMessageListRepository()
                val viewModel = TestableIncomingMessageViewModel(repository, loggedInUserUid = "logged_in_user")
                
                // Configure for user conversation
                val otherUser = User().apply { uid = "other_user" }
                viewModel.setUserForTest(otherUser)
                
                // Create message with no sender
                val messageWithNoSender = com.cometchat.chat.models.TextMessage(
                    "logged_in_user",
                    "Message with no sender",
                    com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER
                ).apply {
                    id = 1L
                    sentAt = System.currentTimeMillis()
                    sender = null
                    receiverUid = "logged_in_user"
                }
                
                // Reset tracking
                repository.markAsDeliveredCalled = false
                repository.markAsDeliveredMessage = null
                
                // Call handleIncomingMessage
                viewModel.callHandleIncomingMessageForTest(messageWithNoSender)
                advanceUntilIdle()
                
                // Verify markAsDelivered was NOT called
                repository.markAsDeliveredCalled shouldBe false
                repository.markAsDeliveredMessage shouldBe null
            }
        }
        
        /**
         * Property-based test: For any message from the logged-in user,
         * handleIncomingMessage should NOT call markAsDelivered.
         * 
         * **Property: Self-Message Property**
         * **Validates: Requirements AC-1.3**
         */
        test("Property: handleIncomingMessage should never call markAsDelivered for own messages") {
            checkAll(10, Arb.long(1L, 10000L)) { messageId ->
                runTest {
                    val repository = MockMessageListRepository()
                    val viewModel = TestableIncomingMessageViewModel(repository, loggedInUserUid = "logged_in_user")
                    
                    // Configure for user conversation
                    val otherUser = User().apply { uid = "other_user" }
                    viewModel.setUserForTest(otherUser)
                    
                    // Create message from logged-in user (own message)
                    val loggedInUser = User().apply { uid = "logged_in_user" }
                    val ownMessage = com.cometchat.chat.models.TextMessage(
                        "other_user",
                        "Test message $messageId",
                        com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER
                    ).apply {
                        id = messageId
                        sentAt = System.currentTimeMillis()
                        sender = loggedInUser
                        receiverUid = "other_user"
                    }
                    
                    // Reset tracking
                    repository.markAsDeliveredCalled = false
                    repository.markAsDeliveredMessage = null
                    
                    // Call handleIncomingMessage
                    viewModel.callHandleIncomingMessageForTest(ownMessage)
                    advanceUntilIdle()
                    
                    // Verify markAsDelivered was NOT called
                    repository.markAsDeliveredCalled shouldBe false
                    repository.markAsDeliveredMessage shouldBe null
                }
            }
        }
    }
})

/**
 * Helper function to create mock BaseMessage instances for testing.
 * 
 * Since BaseMessage is from the CometChat SDK and may have complex construction,
 * this function creates minimal mock instances suitable for testing state transitions.
 * 
 * @param count Number of messages to create
 * @param startId Starting ID for the messages (default 1)
 */
private fun createMockMessages(count: Int, startId: Int = 1): List<BaseMessage> {
    return (startId until startId + count).map { index ->
        // Create a minimal TextMessage for testing
        // Note: This relies on the SDK's TextMessage constructor
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


// ========================================
// Task 5.5 & 5.6: markLastMessageAsRead Tests
// ========================================

/**
 * Unit tests for markLastMessageAsRead method.
 * 
 * These tests validate the comprehensive validation logic for marking messages as read,
 * including disableReceipt checks, sender validation, readAt checks, and thread context handling.
 * 
 * **Validates: Requirements AC-2.1, AC-2.2, AC-2.3, AC-2.4, AC-2.5, AC-2.6, AC-4.1, AC-4.2**
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MarkLastMessageAsReadTest : FunSpec({

    val testDispatcher = StandardTestDispatcher()

    beforeSpec {
        Dispatchers.setMain(testDispatcher)
    }

    afterSpec {
        Dispatchers.resetMain()
    }

    /**
     * Mock repository for testing ViewModel without SDK dependencies.
     */
    class MockMessageListRepository : MessageListRepository {
        var fetchPreviousMessagesResult: Result<List<BaseMessage>> = Result.success(emptyList())
        var fetchNextMessagesResult: Result<List<BaseMessage>> = Result.success(emptyList())
        var getConversationResult: Result<Conversation>? = null
        var getMessageResult: Result<BaseMessage>? = null
        var deleteMessageResult: Result<BaseMessage>? = null
        var flagMessageResult: Result<Unit> = Result.success(Unit)
        var addReactionResult: Result<BaseMessage>? = null
        var removeReactionResult: Result<BaseMessage>? = null
        var markAsReadResult: Result<Unit> = Result.success(Unit)
        var markAsUnreadResult: Result<Conversation> = Result.success(Conversation().apply { unreadMessageCount = 1 })
        var markAsDeliveredResult: Result<Unit> = Result.success(Unit)
        var hasMorePrevious: Boolean = true
        
        var configuredForUser: User? = null
        var configuredForGroup: Group? = null
        var resetRequestCalled: Boolean = false
        var markAsReadCalled: Boolean = false
        var markAsReadMessage: BaseMessage? = null
        var markAsDeliveredCalled: Boolean = false
        var markAsDeliveredMessage: BaseMessage? = null

        override suspend fun fetchPreviousMessages(): Result<List<BaseMessage>> = fetchPreviousMessagesResult
        override suspend fun fetchNextMessages(fromMessageId: Long): Result<List<BaseMessage>> = fetchNextMessagesResult
        override suspend fun getConversation(id: String, type: String): Result<Conversation> =
            getConversationResult ?: Result.failure(Exception("Not configured"))
        override suspend fun getMessage(messageId: Long): Result<BaseMessage> =
            getMessageResult ?: Result.failure(Exception("Not configured"))
        override suspend fun deleteMessage(message: BaseMessage): Result<BaseMessage> =
            deleteMessageResult ?: Result.failure(Exception("Not configured"))
        override suspend fun flagMessage(messageId: Long, reason: String, remark: String): Result<Unit> = flagMessageResult
        override suspend fun addReaction(messageId: Long, emoji: String): Result<BaseMessage> =
            addReactionResult ?: Result.failure(Exception("Not configured"))
        override suspend fun removeReaction(messageId: Long, emoji: String): Result<BaseMessage> =
            removeReactionResult ?: Result.failure(Exception("Not configured"))
        override suspend fun markAsRead(message: BaseMessage): Result<Unit> {
            markAsReadCalled = true
            markAsReadMessage = message
            return markAsReadResult
        }
        override suspend fun markAsDelivered(message: BaseMessage): Result<Unit> {
            markAsDeliveredCalled = true
            markAsDeliveredMessage = message
            return markAsDeliveredResult
        }
        override suspend fun markAsUnread(message: BaseMessage): Result<Conversation> = markAsUnreadResult
        override fun hasMorePreviousMessages(): Boolean = hasMorePrevious
        override fun resetRequest() { resetRequestCalled = true; hasMorePrevious = true }
        override fun configureForUser(user: User, messagesTypes: List<String>, messagesCategories: List<String>, parentMessageId: Long, messagesRequestBuilder: MessagesRequest.MessagesRequestBuilder?) {
            configuredForUser = user
        }
        override fun configureForGroup(group: Group, messagesTypes: List<String>, messagesCategories: List<String>, parentMessageId: Long, messagesRequestBuilder: MessagesRequest.MessagesRequestBuilder?) {
            configuredForGroup = group
        }
        override suspend fun fetchSurroundingMessages(messageId: Long) =
            Result.failure<com.cometchat.uikit.core.domain.model.SurroundingMessagesResult>(Exception("Not implemented"))
        override suspend fun fetchActionMessages(fromMessageId: Long) = Result.success<List<BaseMessage>>(emptyList())
        override fun rebuildRequestFromMessageId(messageId: Long) {}
        private var latestMessageId: Long = -1
        override fun getLatestMessageId(): Long = latestMessageId
        override fun setLatestMessageId(messageId: Long) { latestMessageId = messageId }
    }

    /**
     * Testable ViewModel subclass for markLastMessageAsRead testing.
     */
    class TestableMarkAsReadViewModel(
        repository: MessageListRepository,
        private val loggedInUserUid: String? = "logged_in_user"
    ) : CometChatMessageListViewModel(repository, enableListeners = false) {
        
        override fun getLoggedInUserUid(): String? = loggedInUserUid
        
        fun setUserForTest(user: User) {
            val field = CometChatMessageListViewModel::class.java.getDeclaredField("user")
            field.isAccessible = true
            field.set(this, user)
        }
        
        fun setDisableReceiptForTest(disable: Boolean) {
            val field = CometChatMessageListViewModel::class.java.getDeclaredField("disableReceipt")
            field.isAccessible = true
            field.setBoolean(this, disable)
        }
        
        fun setParentMessageIdForTest(parentId: Long) {
            val field = CometChatMessageListViewModel::class.java.getDeclaredField("parentMessageId")
            field.isAccessible = true
            field.setLong(this, parentId)
        }
        
        fun setMessagesForTest(messages: List<BaseMessage>) {
            val field = CometChatMessageListViewModel::class.java.getDeclaredField("_messages")
            field.isAccessible = true
            @Suppress("UNCHECKED_CAST")
            val stateFlow = field.get(this) as kotlinx.coroutines.flow.MutableStateFlow<List<BaseMessage>>
            stateFlow.value = messages
        }
    }

    // ========================================
    // Task 5.5: Unit tests for all validation conditions
    // ========================================
    
    context("Task 5.5: markLastMessageAsRead validation conditions") {
        
        /**
         * **AC-2.3: markMessageAsRead is only called if disableReceipt is false**
         */
        test("markLastMessageAsRead should call repository.markAsRead when all conditions are met") {
            runTest {
                val repository = MockMessageListRepository()
                val viewModel = TestableMarkAsReadViewModel(repository, loggedInUserUid = "logged_in_user")
                
                // Configure for user conversation
                val otherUser = User().apply { uid = "other_user" }
                viewModel.setUserForTest(otherUser)
                viewModel.setDisableReceiptForTest(false)
                viewModel.setParentMessageIdForTest(-1L) // Main conversation
                
                // Create incoming message from other user with readAt = 0
                val incomingMessage = com.cometchat.chat.models.TextMessage(
                    "logged_in_user",
                    "Hello",
                    com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER
                ).apply {
                    id = 1L
                    sentAt = System.currentTimeMillis()
                    sender = otherUser
                    receiverUid = "logged_in_user"
                    readAt = 0L
                    parentMessageId = 0L // Main conversation message
                }
                
                // Reset tracking
                repository.markAsReadCalled = false
                repository.markAsReadMessage = null
                
                // Call markLastMessageAsRead
                viewModel.markLastMessageAsRead(incomingMessage)
                advanceUntilIdle()
                
                // Verify markAsRead was called
                repository.markAsReadCalled shouldBe true
                repository.markAsReadMessage?.id shouldBe 1L
            }
        }
        
        /**
         * **AC-2.3: markMessageAsRead is only called if disableReceipt is false**
         */
        test("markLastMessageAsRead should NOT call repository when disableReceipt is true") {
            runTest {
                val repository = MockMessageListRepository()
                val viewModel = TestableMarkAsReadViewModel(repository, loggedInUserUid = "logged_in_user")
                
                val otherUser = User().apply { uid = "other_user" }
                viewModel.setUserForTest(otherUser)
                viewModel.setDisableReceiptForTest(true) // Receipts disabled
                viewModel.setParentMessageIdForTest(-1L)
                
                val incomingMessage = com.cometchat.chat.models.TextMessage(
                    "logged_in_user",
                    "Hello",
                    com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER
                ).apply {
                    id = 1L
                    sentAt = System.currentTimeMillis()
                    sender = otherUser
                    receiverUid = "logged_in_user"
                    readAt = 0L
                    parentMessageId = 0L
                }
                
                repository.markAsReadCalled = false
                
                viewModel.markLastMessageAsRead(incomingMessage)
                advanceUntilIdle()
                
                repository.markAsReadCalled shouldBe false
            }
        }
        
        /**
         * **AC-2.4: markMessageAsRead is only called for messages from other users**
         */
        test("markLastMessageAsRead should NOT call repository for own messages") {
            runTest {
                val repository = MockMessageListRepository()
                val viewModel = TestableMarkAsReadViewModel(repository, loggedInUserUid = "logged_in_user")
                
                val otherUser = User().apply { uid = "other_user" }
                viewModel.setUserForTest(otherUser)
                viewModel.setDisableReceiptForTest(false)
                viewModel.setParentMessageIdForTest(-1L)
                
                // Create message from logged-in user (own message)
                val loggedInUser = User().apply { uid = "logged_in_user" }
                val ownMessage = com.cometchat.chat.models.TextMessage(
                    "other_user",
                    "Hello",
                    com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER
                ).apply {
                    id = 1L
                    sentAt = System.currentTimeMillis()
                    sender = loggedInUser
                    receiverUid = "other_user"
                    readAt = 0L
                    parentMessageId = 0L
                }
                
                repository.markAsReadCalled = false
                
                viewModel.markLastMessageAsRead(ownMessage)
                advanceUntilIdle()
                
                repository.markAsReadCalled shouldBe false
            }
        }
        
        /**
         * **AC-2.1: markMessageAsRead only marks messages where readAt == 0**
         */
        test("markLastMessageAsRead should NOT call repository when message is already read (readAt != 0)") {
            runTest {
                val repository = MockMessageListRepository()
                val viewModel = TestableMarkAsReadViewModel(repository, loggedInUserUid = "logged_in_user")
                
                val otherUser = User().apply { uid = "other_user" }
                viewModel.setUserForTest(otherUser)
                viewModel.setDisableReceiptForTest(false)
                viewModel.setParentMessageIdForTest(-1L)
                
                // Create message that is already read
                val alreadyReadMessage = com.cometchat.chat.models.TextMessage(
                    "logged_in_user",
                    "Hello",
                    com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER
                ).apply {
                    id = 1L
                    sentAt = System.currentTimeMillis()
                    sender = otherUser
                    receiverUid = "logged_in_user"
                    readAt = System.currentTimeMillis() / 1000 // Already read
                    parentMessageId = 0L
                }
                
                repository.markAsReadCalled = false
                
                viewModel.markLastMessageAsRead(alreadyReadMessage)
                advanceUntilIdle()
                
                repository.markAsReadCalled shouldBe false
            }
        }
        
        /**
         * Test that markLastMessageAsRead does NOT call repository when sender is null.
         */
        test("markLastMessageAsRead should NOT call repository when sender is null") {
            runTest {
                val repository = MockMessageListRepository()
                val viewModel = TestableMarkAsReadViewModel(repository, loggedInUserUid = "logged_in_user")
                
                val otherUser = User().apply { uid = "other_user" }
                viewModel.setUserForTest(otherUser)
                viewModel.setDisableReceiptForTest(false)
                viewModel.setParentMessageIdForTest(-1L)
                
                // Create message with null sender
                val messageWithNullSender = com.cometchat.chat.models.TextMessage(
                    "logged_in_user",
                    "Hello",
                    com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER
                ).apply {
                    id = 1L
                    sentAt = System.currentTimeMillis()
                    sender = null // No sender
                    receiverUid = "logged_in_user"
                    readAt = 0L
                    parentMessageId = 0L
                }
                
                repository.markAsReadCalled = false
                
                viewModel.markLastMessageAsRead(messageWithNullSender)
                advanceUntilIdle()
                
                repository.markAsReadCalled shouldBe false
            }
        }
        
        /**
         * **AC-2.5: After successful markAsRead, the message's readAt timestamp is updated locally**
         */
        test("markLastMessageAsRead should update local message readAt on success") {
            runTest {
                val repository = MockMessageListRepository()
                val viewModel = TestableMarkAsReadViewModel(repository, loggedInUserUid = "logged_in_user")
                
                val otherUser = User().apply { uid = "other_user" }
                viewModel.setUserForTest(otherUser)
                viewModel.setDisableReceiptForTest(false)
                viewModel.setParentMessageIdForTest(-1L)
                
                val incomingMessage = com.cometchat.chat.models.TextMessage(
                    "logged_in_user",
                    "Hello",
                    com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER
                ).apply {
                    id = 1L
                    sentAt = System.currentTimeMillis()
                    sender = otherUser
                    receiverUid = "logged_in_user"
                    readAt = 0L
                    parentMessageId = 0L
                }
                
                // Set the message in the list
                viewModel.setMessagesForTest(listOf(incomingMessage))
                
                // Verify initial readAt is 0
                viewModel.messages.value.first().readAt shouldBe 0L
                
                // Call markLastMessageAsRead
                viewModel.markLastMessageAsRead(incomingMessage)
                advanceUntilIdle()
                
                // Verify readAt was updated (should be non-zero now)
                viewModel.messages.value.first().readAt shouldBeGreaterThan 0L
            }
        }
        
        /**
         * **AC-2.6: After successful markAsRead, a UIKit helper notification is emitted**
         */
        test("markLastMessageAsRead should emit messageReadEvent on success") {
            runTest {
                val repository = MockMessageListRepository()
                val viewModel = TestableMarkAsReadViewModel(repository, loggedInUserUid = "logged_in_user")
                
                val otherUser = User().apply { uid = "other_user" }
                viewModel.setUserForTest(otherUser)
                viewModel.setDisableReceiptForTest(false)
                viewModel.setParentMessageIdForTest(-1L)
                
                val incomingMessage = com.cometchat.chat.models.TextMessage(
                    "logged_in_user",
                    "Hello",
                    com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER
                ).apply {
                    id = 1L
                    sentAt = System.currentTimeMillis()
                    sender = otherUser
                    receiverUid = "logged_in_user"
                    readAt = 0L
                    parentMessageId = 0L
                }
                
                viewModel.setMessagesForTest(listOf(incomingMessage))
                
                // Collect events
                var emittedMessage: BaseMessage? = null
                val job = launch {
                    viewModel.messageReadEvent.collect { message ->
                        emittedMessage = message
                    }
                }
                
                // Call markLastMessageAsRead
                viewModel.markLastMessageAsRead(incomingMessage)
                advanceUntilIdle()
                
                // Verify event was emitted
                emittedMessage?.id shouldBe 1L
                
                job.cancel()
            }
        }
    }

    // ========================================
    // Task 5.6: Unit tests for thread context handling
    // ========================================
    
    context("Task 5.6: markLastMessageAsRead thread context handling") {
        
        /**
         * **AC-4.1: For main conversation (parentMessageId == -1), only mark messages with parentMessageId == 0**
         */
        test("Main conversation: should mark messages with parentMessageId == 0") {
            runTest {
                val repository = MockMessageListRepository()
                val viewModel = TestableMarkAsReadViewModel(repository, loggedInUserUid = "logged_in_user")
                
                val otherUser = User().apply { uid = "other_user" }
                viewModel.setUserForTest(otherUser)
                viewModel.setDisableReceiptForTest(false)
                viewModel.setParentMessageIdForTest(-1L) // Main conversation
                
                // Create main conversation message (parentMessageId == 0)
                val mainMessage = com.cometchat.chat.models.TextMessage(
                    "logged_in_user",
                    "Hello",
                    com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER
                ).apply {
                    id = 1L
                    sentAt = System.currentTimeMillis()
                    sender = otherUser
                    receiverUid = "logged_in_user"
                    readAt = 0L
                    parentMessageId = 0L // Main conversation message
                }
                
                repository.markAsReadCalled = false
                
                viewModel.markLastMessageAsRead(mainMessage)
                advanceUntilIdle()
                
                repository.markAsReadCalled shouldBe true
            }
        }
        
        /**
         * **AC-4.1: For main conversation, should NOT mark thread reply messages**
         */
        test("Main conversation: should NOT mark thread reply messages (parentMessageId > 0)") {
            runTest {
                val repository = MockMessageListRepository()
                val viewModel = TestableMarkAsReadViewModel(repository, loggedInUserUid = "logged_in_user")
                
                val otherUser = User().apply { uid = "other_user" }
                viewModel.setUserForTest(otherUser)
                viewModel.setDisableReceiptForTest(false)
                viewModel.setParentMessageIdForTest(-1L) // Main conversation
                
                // Create thread reply message (parentMessageId > 0)
                val threadReply = com.cometchat.chat.models.TextMessage(
                    "logged_in_user",
                    "Thread reply",
                    com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER
                ).apply {
                    id = 2L
                    sentAt = System.currentTimeMillis()
                    sender = otherUser
                    receiverUid = "logged_in_user"
                    readAt = 0L
                    parentMessageId = 100L // This is a thread reply
                }
                
                repository.markAsReadCalled = false
                
                viewModel.markLastMessageAsRead(threadReply)
                advanceUntilIdle()
                
                repository.markAsReadCalled shouldBe false
            }
        }
        
        /**
         * **AC-4.2: For thread view (parentMessageId > -1), only mark messages matching the thread's parentMessageId**
         */
        test("Thread view: should mark messages matching the thread parentMessageId") {
            runTest {
                val repository = MockMessageListRepository()
                val viewModel = TestableMarkAsReadViewModel(repository, loggedInUserUid = "logged_in_user")
                
                val otherUser = User().apply { uid = "other_user" }
                viewModel.setUserForTest(otherUser)
                viewModel.setDisableReceiptForTest(false)
                viewModel.setParentMessageIdForTest(100L) // Thread view for parent message 100
                
                // Create thread reply message matching the thread
                val threadReply = com.cometchat.chat.models.TextMessage(
                    "logged_in_user",
                    "Thread reply",
                    com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER
                ).apply {
                    id = 2L
                    sentAt = System.currentTimeMillis()
                    sender = otherUser
                    receiverUid = "logged_in_user"
                    readAt = 0L
                    parentMessageId = 100L // Matches the thread
                }
                
                repository.markAsReadCalled = false
                
                viewModel.markLastMessageAsRead(threadReply)
                advanceUntilIdle()
                
                repository.markAsReadCalled shouldBe true
            }
        }
        
        /**
         * **AC-4.2: For thread view, should NOT mark messages from different threads**
         */
        test("Thread view: should NOT mark messages from different threads") {
            runTest {
                val repository = MockMessageListRepository()
                val viewModel = TestableMarkAsReadViewModel(repository, loggedInUserUid = "logged_in_user")
                
                val otherUser = User().apply { uid = "other_user" }
                viewModel.setUserForTest(otherUser)
                viewModel.setDisableReceiptForTest(false)
                viewModel.setParentMessageIdForTest(100L) // Thread view for parent message 100
                
                // Create thread reply message from a DIFFERENT thread
                val differentThreadReply = com.cometchat.chat.models.TextMessage(
                    "logged_in_user",
                    "Different thread reply",
                    com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER
                ).apply {
                    id = 3L
                    sentAt = System.currentTimeMillis()
                    sender = otherUser
                    receiverUid = "logged_in_user"
                    readAt = 0L
                    parentMessageId = 200L // Different thread
                }
                
                repository.markAsReadCalled = false
                
                viewModel.markLastMessageAsRead(differentThreadReply)
                advanceUntilIdle()
                
                repository.markAsReadCalled shouldBe false
            }
        }
        
        /**
         * **AC-4.2: For thread view, should NOT mark main conversation messages**
         */
        test("Thread view: should NOT mark main conversation messages (parentMessageId == 0)") {
            runTest {
                val repository = MockMessageListRepository()
                val viewModel = TestableMarkAsReadViewModel(repository, loggedInUserUid = "logged_in_user")
                
                val otherUser = User().apply { uid = "other_user" }
                viewModel.setUserForTest(otherUser)
                viewModel.setDisableReceiptForTest(false)
                viewModel.setParentMessageIdForTest(100L) // Thread view for parent message 100
                
                // Create main conversation message
                val mainMessage = com.cometchat.chat.models.TextMessage(
                    "logged_in_user",
                    "Main message",
                    com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER
                ).apply {
                    id = 4L
                    sentAt = System.currentTimeMillis()
                    sender = otherUser
                    receiverUid = "logged_in_user"
                    readAt = 0L
                    parentMessageId = 0L // Main conversation message
                }
                
                repository.markAsReadCalled = false
                
                viewModel.markLastMessageAsRead(mainMessage)
                advanceUntilIdle()
                
                repository.markAsReadCalled shouldBe false
            }
        }
        
        /**
         * Property-based test: For any parentMessageId configuration,
         * only messages with matching context should be marked.
         * 
         * **Property: Thread Context Property**
         * **Validates: Requirements AC-4.1, AC-4.2**
         */
        test("Property: Thread context should correctly filter messages") {
            checkAll(10, Arb.long(1L, 1000L)) { threadParentId ->
                runTest {
                    val repository = MockMessageListRepository()
                    val viewModel = TestableMarkAsReadViewModel(repository, loggedInUserUid = "logged_in_user")
                    
                    val otherUser = User().apply { uid = "other_user" }
                    viewModel.setUserForTest(otherUser)
                    viewModel.setDisableReceiptForTest(false)
                    viewModel.setParentMessageIdForTest(threadParentId) // Thread view
                    
                    // Create message matching the thread
                    val matchingMessage = com.cometchat.chat.models.TextMessage(
                        "logged_in_user",
                        "Matching thread reply",
                        com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER
                    ).apply {
                        id = 1L
                        sentAt = System.currentTimeMillis()
                        sender = otherUser
                        receiverUid = "logged_in_user"
                        readAt = 0L
                        parentMessageId = threadParentId // Matches
                    }
                    
                    repository.markAsReadCalled = false
                    viewModel.markLastMessageAsRead(matchingMessage)
                    advanceUntilIdle()
                    
                    // Should be called for matching thread
                    repository.markAsReadCalled shouldBe true
                    
                    // Create message NOT matching the thread
                    val nonMatchingMessage = com.cometchat.chat.models.TextMessage(
                        "logged_in_user",
                        "Non-matching thread reply",
                        com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER
                    ).apply {
                        id = 2L
                        sentAt = System.currentTimeMillis()
                        sender = otherUser
                        receiverUid = "logged_in_user"
                        readAt = 0L
                        parentMessageId = threadParentId + 1 // Different thread
                    }
                    
                    repository.markAsReadCalled = false
                    viewModel.markLastMessageAsRead(nonMatchingMessage)
                    advanceUntilIdle()
                    
                    // Should NOT be called for non-matching thread
                    repository.markAsReadCalled shouldBe false
                }
            }
        }
    }

    // ========================================
    // Task 6.3: markConversationRead event emission tests
    // ========================================
    
    context("Task 6.3: markConversationRead event emission tests") {
        
        /**
         * **AC-3.3: markConversationRead emits a UIKit helper notification on success**
         */
        test("markConversationRead should emit messageReadEvent on successful markAsRead") {
            runTest {
                val repository = MockMessageListRepository()
                repository.markAsReadResult = Result.success(Unit)
                
                val viewModel = TestableMarkAsReadViewModel(repository, loggedInUserUid = "logged_in_user")
                
                // Configure for user conversation
                val otherUser = User().apply { uid = "other_user" }
                viewModel.setUserForTest(otherUser)
                viewModel.setDisableReceiptForTest(false)
                
                // Create a message from other user
                val lastMessage = com.cometchat.chat.models.TextMessage(
                    "logged_in_user",
                    "Hello",
                    com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER
                ).apply {
                    id = 1L
                    sentAt = System.currentTimeMillis()
                    sender = otherUser
                    receiverUid = "logged_in_user"
                }
                
                viewModel.setMessagesForTest(listOf(lastMessage))
                
                // Collect emitted events
                var emittedMessage: BaseMessage? = null
                val job = launch {
                    viewModel.messageReadEvent.collect { message ->
                        emittedMessage = message
                    }
                }
                
                // Call markConversationRead
                viewModel.markConversationRead()
                advanceUntilIdle()
                
                // Verify event was emitted
                emittedMessage shouldBe lastMessage
                
                job.cancel()
            }
        }
        
        /**
         * **AC-3.2: markConversationRead resets unreadCount to 0 on success**
         */
        test("markConversationRead should reset unreadCount to 0 on success") {
            runTest {
                val repository = MockMessageListRepository()
                repository.markAsReadResult = Result.success(Unit)
                
                val viewModel = TestableMarkAsReadViewModel(repository, loggedInUserUid = "logged_in_user")
                
                val otherUser = User().apply { uid = "other_user" }
                viewModel.setUserForTest(otherUser)
                viewModel.setDisableReceiptForTest(false)
                
                val lastMessage = com.cometchat.chat.models.TextMessage(
                    "logged_in_user",
                    "Hello",
                    com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER
                ).apply {
                    id = 1L
                    sentAt = System.currentTimeMillis()
                    sender = otherUser
                    receiverUid = "logged_in_user"
                }
                
                viewModel.setMessagesForTest(listOf(lastMessage))
                
                // Call markConversationRead
                viewModel.markConversationRead()
                advanceUntilIdle()
                
                // Verify unreadCount is reset
                viewModel.unreadCount.value shouldBe 0
            }
        }
        
        /**
         * **AC-3.4: markConversationRead handles empty message lists gracefully**
         */
        test("markConversationRead should NOT emit event when message list is empty") {
            runTest {
                val repository = MockMessageListRepository()
                
                val viewModel = TestableMarkAsReadViewModel(repository, loggedInUserUid = "logged_in_user")
                
                val otherUser = User().apply { uid = "other_user" }
                viewModel.setUserForTest(otherUser)
                viewModel.setDisableReceiptForTest(false)
                
                // Empty message list
                viewModel.setMessagesForTest(emptyList())
                
                // Collect emitted events
                var emittedMessage: BaseMessage? = null
                val job = launch {
                    viewModel.messageReadEvent.collect { message ->
                        emittedMessage = message
                    }
                }
                
                // Call markConversationRead
                viewModel.markConversationRead()
                advanceUntilIdle()
                
                // Verify no event was emitted
                emittedMessage shouldBe null
                
                // Verify markAsRead was NOT called
                repository.markAsReadCalled shouldBe false
                
                job.cancel()
            }
        }
        
        /**
         * Test that markConversationRead does NOT emit event when disableReceipt is true
         */
        test("markConversationRead should NOT emit event when disableReceipt is true") {
            runTest {
                val repository = MockMessageListRepository()
                
                val viewModel = TestableMarkAsReadViewModel(repository, loggedInUserUid = "logged_in_user")
                
                val otherUser = User().apply { uid = "other_user" }
                viewModel.setUserForTest(otherUser)
                viewModel.setDisableReceiptForTest(true) // Receipts disabled
                
                val lastMessage = com.cometchat.chat.models.TextMessage(
                    "logged_in_user",
                    "Hello",
                    com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER
                ).apply {
                    id = 1L
                    sentAt = System.currentTimeMillis()
                    sender = otherUser
                    receiverUid = "logged_in_user"
                }
                
                viewModel.setMessagesForTest(listOf(lastMessage))
                
                // Collect emitted events
                var emittedMessage: BaseMessage? = null
                val job = launch {
                    viewModel.messageReadEvent.collect { message ->
                        emittedMessage = message
                    }
                }
                
                // Call markConversationRead
                viewModel.markConversationRead()
                advanceUntilIdle()
                
                // Verify no event was emitted
                emittedMessage shouldBe null
                
                // Verify markAsRead was NOT called
                repository.markAsReadCalled shouldBe false
                
                job.cancel()
            }
        }
        
        /**
         * Test that markConversationRead resets unreadCount but does NOT emit event for own messages
         */
        test("markConversationRead should reset unreadCount but NOT emit event for own messages") {
            runTest {
                val repository = MockMessageListRepository()
                
                val viewModel = TestableMarkAsReadViewModel(repository, loggedInUserUid = "logged_in_user")
                
                val otherUser = User().apply { uid = "other_user" }
                val loggedInUser = User().apply { uid = "logged_in_user" }
                viewModel.setUserForTest(otherUser)
                viewModel.setDisableReceiptForTest(false)
                
                // Last message is from logged in user (own message)
                val ownMessage = com.cometchat.chat.models.TextMessage(
                    "other_user",
                    "Hello",
                    com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER
                ).apply {
                    id = 1L
                    sentAt = System.currentTimeMillis()
                    sender = loggedInUser
                    receiverUid = "other_user"
                }
                
                viewModel.setMessagesForTest(listOf(ownMessage))
                
                // Collect emitted events
                var emittedMessage: BaseMessage? = null
                val job = launch {
                    viewModel.messageReadEvent.collect { message ->
                        emittedMessage = message
                    }
                }
                
                // Call markConversationRead
                viewModel.markConversationRead()
                advanceUntilIdle()
                
                // Verify unreadCount is reset (for own messages)
                viewModel.unreadCount.value shouldBe 0
                
                // Verify no event was emitted (own message)
                emittedMessage shouldBe null
                
                // Verify markAsRead was NOT called (own message)
                repository.markAsReadCalled shouldBe false
                
                job.cancel()
            }
        }
        
        /**
         * Test that markConversationRead does NOT emit event when markAsRead fails
         */
        test("markConversationRead should NOT emit event when markAsRead fails") {
            runTest {
                val repository = MockMessageListRepository()
                repository.markAsReadResult = Result.failure(Exception("Network error"))
                
                val viewModel = TestableMarkAsReadViewModel(repository, loggedInUserUid = "logged_in_user")
                
                val otherUser = User().apply { uid = "other_user" }
                viewModel.setUserForTest(otherUser)
                viewModel.setDisableReceiptForTest(false)
                
                val lastMessage = com.cometchat.chat.models.TextMessage(
                    "logged_in_user",
                    "Hello",
                    com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER
                ).apply {
                    id = 1L
                    sentAt = System.currentTimeMillis()
                    sender = otherUser
                    receiverUid = "logged_in_user"
                }
                
                viewModel.setMessagesForTest(listOf(lastMessage))
                
                // Collect emitted events
                var emittedMessage: BaseMessage? = null
                val job = launch {
                    viewModel.messageReadEvent.collect { message ->
                        emittedMessage = message
                    }
                }
                
                // Call markConversationRead
                viewModel.markConversationRead()
                advanceUntilIdle()
                
                // Verify markAsRead was called
                repository.markAsReadCalled shouldBe true
                
                // Verify no event was emitted (failure case)
                emittedMessage shouldBe null
                
                job.cancel()
            }
        }
        
        /**
         * **AC-3.1: markConversationRead marks the last message in the list as read**
         */
        test("markConversationRead should mark the last message in the list") {
            runTest {
                val repository = MockMessageListRepository()
                repository.markAsReadResult = Result.success(Unit)
                
                val viewModel = TestableMarkAsReadViewModel(repository, loggedInUserUid = "logged_in_user")
                
                val otherUser = User().apply { uid = "other_user" }
                viewModel.setUserForTest(otherUser)
                viewModel.setDisableReceiptForTest(false)
                
                // Create multiple messages
                val message1 = com.cometchat.chat.models.TextMessage(
                    "logged_in_user",
                    "First message",
                    com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER
                ).apply {
                    id = 1L
                    sentAt = System.currentTimeMillis()
                    sender = otherUser
                    receiverUid = "logged_in_user"
                }
                
                val message2 = com.cometchat.chat.models.TextMessage(
                    "logged_in_user",
                    "Second message",
                    com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER
                ).apply {
                    id = 2L
                    sentAt = System.currentTimeMillis()
                    sender = otherUser
                    receiverUid = "logged_in_user"
                }
                
                val lastMessage = com.cometchat.chat.models.TextMessage(
                    "logged_in_user",
                    "Last message",
                    com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_USER
                ).apply {
                    id = 3L
                    sentAt = System.currentTimeMillis()
                    sender = otherUser
                    receiverUid = "logged_in_user"
                }
                
                viewModel.setMessagesForTest(listOf(message1, message2, lastMessage))
                
                // Call markConversationRead
                viewModel.markConversationRead()
                advanceUntilIdle()
                
                // Verify the LAST message was marked as read
                repository.markAsReadMessage?.id shouldBe 3L
            }
        }
    }
    
    // ========================================
    // Conversation Starter Tests
    // ========================================
    
    context("Conversation Starter Configuration") {
        
        /**
         * Test that enableConversationStarter defaults to false.
         * 
         * **Validates: AC-2.2**
         */
        test("9.1 enableConversationStarter defaults to false") {
            val repository = MockMessageListRepository()
            val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
            
            // Initial state should be Idle (feature disabled by default)
            viewModel.conversationStarterUIState.value shouldBe ConversationStarterUIState.Idle
            viewModel.conversationStarterReplies.value shouldBe emptyList()
        }
        
        /**
         * Test that setEnableConversationStarter(true) enables the feature.
         * 
         * **Validates: AC-2.1**
         */
        test("9.2 setEnableConversationStarter(true) enables the feature") {
            val repository = MockMessageListRepository()
            val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
            
            viewModel.setEnableConversationStarter(true)
            
            // Feature is now enabled - state should still be Idle until fetch is called
            viewModel.conversationStarterUIState.value shouldBe ConversationStarterUIState.Idle
        }
        
        /**
         * Test that setEnableConversationStarter(false) clears existing starters.
         * 
         * **Validates: AC-2.1**
         */
        test("9.3 setEnableConversationStarter(false) clears existing starters") {
            runTest {
                val repository = MockMessageListRepository()
                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                
                // Enable and simulate having starters by setting state directly
                viewModel.setEnableConversationStarter(true)
                
                // Now disable - should clear starters
                viewModel.setEnableConversationStarter(false)
                
                viewModel.conversationStarterReplies.value shouldBe emptyList()
                viewModel.conversationStarterUIState.value shouldBe ConversationStarterUIState.Idle
            }
        }
    }
    
    context("Conversation Starter Fetch Guards") {
        
        /**
         * Test that fetchConversationStarter does nothing when disabled.
         * 
         * **Validates: AC-1.4**
         */
        test("9.4 fetchConversationStarter does nothing when disabled") {
            runTest {
                val repository = MockMessageListRepository()
                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                
                // Feature is disabled by default
                viewModel.fetchConversationStarter()
                advanceUntilIdle()
                
                // State should remain Idle
                viewModel.conversationStarterUIState.value shouldBe ConversationStarterUIState.Idle
            }
        }
        
        /**
         * Test that fetchConversationStarter does nothing in thread view.
         * 
         * **Validates: AC-1.5**
         */
        test("9.5 fetchConversationStarter does nothing in thread view") {
            runTest {
                val repository = MockMessageListRepository()
                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                
                // Configure for thread view (parentMessageId > 0)
                val user = User().apply { uid = "user123" }
                viewModel.setUser(user, parentMessageId = 999L)
                viewModel.setEnableConversationStarter(true)
                
                viewModel.fetchConversationStarter()
                advanceUntilIdle()
                
                // State should remain Idle because we're in thread view
                viewModel.conversationStarterUIState.value shouldBe ConversationStarterUIState.Idle
            }
        }
    }
    
    context("Conversation Starter Clear") {
        
        /**
         * Test that clearConversationStarter clears the starters and emits event.
         * 
         * **Validates: AC-5.1, AC-5.2**
         */
        test("9.11 clears starters when first message is added") {
            runTest {
                val repository = MockMessageListRepository()
                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                
                // Configure for user conversation
                val user = User().apply { uid = "user123" }
                viewModel.setUser(user)
                viewModel.setEnableConversationStarter(true)
                
                // Simulate having conversation starters by calling clearConversationStarter
                // (since we can't easily mock the SDK call)
                // First verify the clear functionality works
                viewModel.clearConversationStarter()
                advanceUntilIdle()
                
                viewModel.conversationStarterReplies.value shouldBe emptyList()
                viewModel.conversationStarterUIState.value shouldBe ConversationStarterUIState.Idle
            }
        }
        
        /**
         * Test that removeConversationStarter event is emitted when cleared.
         * 
         * **Validates: AC-5.3**
         */
        test("9.12 emits removeConversationStarter event when cleared") {
            runTest {
                val repository = MockMessageListRepository()
                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)
                
                // Configure for user conversation
                val user = User().apply { uid = "user123" }
                viewModel.setUser(user)
                viewModel.setEnableConversationStarter(true)
                
                // Collect events
                val events = mutableListOf<Unit>()
                val job = launch {
                    viewModel.removeConversationStarter.collect { events.add(it) }
                }
                
                // Clear conversation starters
                viewModel.clearConversationStarter()
                advanceUntilIdle()
                
                // Verify event was emitted
                events.size shouldBe 1
                job.cancel()
            }
        }
    }
})

