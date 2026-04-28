package com.cometchat.uikit.core.data.repository

import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.ConversationsRequest
import com.cometchat.chat.core.MessagesRequest
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Conversation
import com.cometchat.chat.models.TextMessage
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.data.datasource.SearchDataSource
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.test.runTest

/**
 * Unit tests for SearchRepositoryImpl.
 *
 * Tests verify that:
 * - getConversations returns success with conversations from data source
 * - getMessages returns success with messages from data source
 * - getConversations returns failure when data source throws exception
 * - getMessages returns failure when data source throws exception
 * - hasMoreConversations is true when conversations are returned
 * - hasMoreConversations is false when empty list is returned
 * - hasMoreMessages is true when messages are returned
 * - hasMoreMessages is false when empty list is returned
 *
 * **Validates: Requirements 1.3, 1.4, 7.5, 7.6**
 */
class SearchRepositoryImplTest : FunSpec({

    /**
     * Mock SearchDataSource for testing repository without SDK dependencies.
     */
    class MockSearchDataSource : SearchDataSource {
        var conversationsResult: Result<List<Conversation>> = Result.success(emptyList())
        var messagesResult: Result<List<BaseMessage>> = Result.success(emptyList())
        var fetchConversationsCallCount = 0
        var fetchMessagesCallCount = 0

        override suspend fun fetchConversations(request: ConversationsRequest): List<Conversation> {
            fetchConversationsCallCount++
            return conversationsResult.getOrThrow()
        }

        override suspend fun fetchMessages(request: MessagesRequest): List<BaseMessage> {
            fetchMessagesCallCount++
            return messagesResult.getOrThrow()
        }
    }

    /**
     * Helper function to create mock Conversation instances for testing using reflection.
     * The Conversation class has a private constructor, so we use reflection.
     */
    fun createMockConversations(count: Int): List<Conversation> {
        return (1..count).map { id ->
            // Use reflection to create Conversation since constructor is private
            val constructor = Conversation::class.java.getDeclaredConstructor()
            constructor.isAccessible = true
            val conversation = constructor.newInstance()

            // Set conversationId using reflection
            Conversation::class.java.getDeclaredField("conversationId").apply {
                isAccessible = true
                set(conversation, "conv_$id")
            }

            conversation
        }
    }

    /**
     * Helper function to create mock BaseMessage instances for testing.
     */
    fun createMockMessages(count: Int): List<BaseMessage> {
        return (1..count).map { id ->
            TextMessage(
                "receiver_$id",
                "Test message $id",
                CometChatConstants.RECEIVER_TYPE_USER
            ).apply {
                this.id = id.toLong()
                sentAt = System.currentTimeMillis() + (id * 1000)
                sender = User().apply { uid = "sender_$id" }
            }
        }
    }

    // ========================================
    // Test 1: getConversations returns success with conversations from data source
    // ========================================

    context("getConversations success scenarios") {

        test("getConversations should return success with conversations from data source") {
            runTest {
                val mockDataSource = MockSearchDataSource()
                val mockConversations = createMockConversations(3)
                mockDataSource.conversationsResult = Result.success(mockConversations)

                val repository = SearchRepositoryImpl(mockDataSource)
                val request = ConversationsRequest.ConversationsRequestBuilder().build()

                val result = repository.getConversations(request)

                result.isSuccess shouldBe true
                result.getOrNull()?.size shouldBe 3
                result.getOrNull() shouldBe mockConversations
                mockDataSource.fetchConversationsCallCount shouldBe 1
            }
        }

        test("getConversations should return success with empty list when data source returns empty") {
            runTest {
                val mockDataSource = MockSearchDataSource()
                mockDataSource.conversationsResult = Result.success(emptyList())

                val repository = SearchRepositoryImpl(mockDataSource)
                val request = ConversationsRequest.ConversationsRequestBuilder().build()

                val result = repository.getConversations(request)

                result.isSuccess shouldBe true
                result.getOrNull() shouldBe emptyList()
            }
        }
    }

    // ========================================
    // Test 2: getMessages returns success with messages from data source
    // ========================================

    context("getMessages success scenarios") {

        test("getMessages should return success with messages from data source") {
            runTest {
                val mockDataSource = MockSearchDataSource()
                val mockMessages = createMockMessages(5)
                mockDataSource.messagesResult = Result.success(mockMessages)

                val repository = SearchRepositoryImpl(mockDataSource)
                val request = MessagesRequest.MessagesRequestBuilder().build()

                val result = repository.getMessages(request)

                result.isSuccess shouldBe true
                result.getOrNull()?.size shouldBe 5
                result.getOrNull() shouldBe mockMessages
                mockDataSource.fetchMessagesCallCount shouldBe 1
            }
        }

        test("getMessages should return success with empty list when data source returns empty") {
            runTest {
                val mockDataSource = MockSearchDataSource()
                mockDataSource.messagesResult = Result.success(emptyList())

                val repository = SearchRepositoryImpl(mockDataSource)
                val request = MessagesRequest.MessagesRequestBuilder().build()

                val result = repository.getMessages(request)

                result.isSuccess shouldBe true
                result.getOrNull() shouldBe emptyList()
            }
        }
    }

    // ========================================
    // Test 3: getConversations returns failure when data source throws exception
    // ========================================

    context("getConversations error handling") {

        test("getConversations should return failure when data source throws CometChatException") {
            runTest {
                val mockDataSource = MockSearchDataSource()
                val exception = CometChatException("ERR_NETWORK", "Network error")
                mockDataSource.conversationsResult = Result.failure(exception)

                val repository = SearchRepositoryImpl(mockDataSource)
                val request = ConversationsRequest.ConversationsRequestBuilder().build()

                val result = repository.getConversations(request)

                result.isFailure shouldBe true
                result.exceptionOrNull().shouldBeInstanceOf<CometChatException>()
                val caughtException = result.exceptionOrNull() as CometChatException
                caughtException.code shouldBe "ERR_NETWORK"
                caughtException.message shouldBe "Network error"
            }
        }

        test("getConversations should return failure when data source throws generic Exception") {
            runTest {
                val mockDataSource = MockSearchDataSource()
                val exception = RuntimeException("Unexpected error")
                mockDataSource.conversationsResult = Result.failure(exception)

                val repository = SearchRepositoryImpl(mockDataSource)
                val request = ConversationsRequest.ConversationsRequestBuilder().build()

                val result = repository.getConversations(request)

                result.isFailure shouldBe true
                result.exceptionOrNull().shouldBeInstanceOf<RuntimeException>()
                result.exceptionOrNull()?.message shouldBe "Unexpected error"
            }
        }
    }

    // ========================================
    // Test 4: getMessages returns failure when data source throws exception
    // ========================================

    context("getMessages error handling") {

        test("getMessages should return failure when data source throws CometChatException") {
            runTest {
                val mockDataSource = MockSearchDataSource()
                val exception = CometChatException("ERR_AUTH", "Authentication failed")
                mockDataSource.messagesResult = Result.failure(exception)

                val repository = SearchRepositoryImpl(mockDataSource)
                val request = MessagesRequest.MessagesRequestBuilder().build()

                val result = repository.getMessages(request)

                result.isFailure shouldBe true
                result.exceptionOrNull().shouldBeInstanceOf<CometChatException>()
                val caughtException = result.exceptionOrNull() as CometChatException
                caughtException.code shouldBe "ERR_AUTH"
                caughtException.message shouldBe "Authentication failed"
            }
        }

        test("getMessages should return failure when data source throws generic Exception") {
            runTest {
                val mockDataSource = MockSearchDataSource()
                val exception = IllegalStateException("Invalid state")
                mockDataSource.messagesResult = Result.failure(exception)

                val repository = SearchRepositoryImpl(mockDataSource)
                val request = MessagesRequest.MessagesRequestBuilder().build()

                val result = repository.getMessages(request)

                result.isFailure shouldBe true
                result.exceptionOrNull().shouldBeInstanceOf<IllegalStateException>()
                result.exceptionOrNull()?.message shouldBe "Invalid state"
            }
        }
    }

    // ========================================
    // Test 5: hasMoreConversations is true when conversations are returned
    // ========================================

    context("hasMoreConversations flag updates") {

        test("hasMoreConversations should be true when conversations are returned") {
            runTest {
                val mockDataSource = MockSearchDataSource()
                val mockConversations = createMockConversations(3)
                mockDataSource.conversationsResult = Result.success(mockConversations)

                val repository = SearchRepositoryImpl(mockDataSource)
                val request = ConversationsRequest.ConversationsRequestBuilder().build()

                // Before fetch, hasMore should be true (default)
                repository.hasMoreConversations() shouldBe true

                // Fetch conversations
                repository.getConversations(request)

                // After fetch with results, hasMore should be true
                repository.hasMoreConversations() shouldBe true
            }
        }

        // ========================================
        // Test 6: hasMoreConversations is false when empty list is returned
        // ========================================

        test("hasMoreConversations should be false when empty list is returned") {
            runTest {
                val mockDataSource = MockSearchDataSource()
                mockDataSource.conversationsResult = Result.success(emptyList())

                val repository = SearchRepositoryImpl(mockDataSource)
                val request = ConversationsRequest.ConversationsRequestBuilder().build()

                // Before fetch, hasMore should be true (default)
                repository.hasMoreConversations() shouldBe true

                // Fetch conversations (empty result)
                repository.getConversations(request)

                // After fetch with empty results, hasMore should be false
                repository.hasMoreConversations() shouldBe false
            }
        }

        test("hasMoreConversations should remain true after error") {
            runTest {
                val mockDataSource = MockSearchDataSource()
                val exception = CometChatException("ERR_NETWORK", "Network error")
                mockDataSource.conversationsResult = Result.failure(exception)

                val repository = SearchRepositoryImpl(mockDataSource)
                val request = ConversationsRequest.ConversationsRequestBuilder().build()

                // Before fetch, hasMore should be true (default)
                repository.hasMoreConversations() shouldBe true

                // Fetch conversations (error)
                repository.getConversations(request)

                // After error, hasMore should remain true (unchanged)
                repository.hasMoreConversations() shouldBe true
            }
        }
    }

    // ========================================
    // Test 7: hasMoreMessages is true when messages are returned
    // ========================================

    context("hasMoreMessages flag updates") {

        test("hasMoreMessages should be true when messages are returned") {
            runTest {
                val mockDataSource = MockSearchDataSource()
                val mockMessages = createMockMessages(5)
                mockDataSource.messagesResult = Result.success(mockMessages)

                val repository = SearchRepositoryImpl(mockDataSource)
                val request = MessagesRequest.MessagesRequestBuilder().build()

                // Before fetch, hasMore should be true (default)
                repository.hasMoreMessages() shouldBe true

                // Fetch messages
                repository.getMessages(request)

                // After fetch with results, hasMore should be true
                repository.hasMoreMessages() shouldBe true
            }
        }

        // ========================================
        // Test 8: hasMoreMessages is false when empty list is returned
        // ========================================

        test("hasMoreMessages should be false when empty list is returned") {
            runTest {
                val mockDataSource = MockSearchDataSource()
                mockDataSource.messagesResult = Result.success(emptyList())

                val repository = SearchRepositoryImpl(mockDataSource)
                val request = MessagesRequest.MessagesRequestBuilder().build()

                // Before fetch, hasMore should be true (default)
                repository.hasMoreMessages() shouldBe true

                // Fetch messages (empty result)
                repository.getMessages(request)

                // After fetch with empty results, hasMore should be false
                repository.hasMoreMessages() shouldBe false
            }
        }

        test("hasMoreMessages should remain true after error") {
            runTest {
                val mockDataSource = MockSearchDataSource()
                val exception = CometChatException("ERR_TIMEOUT", "Request timeout")
                mockDataSource.messagesResult = Result.failure(exception)

                val repository = SearchRepositoryImpl(mockDataSource)
                val request = MessagesRequest.MessagesRequestBuilder().build()

                // Before fetch, hasMore should be true (default)
                repository.hasMoreMessages() shouldBe true

                // Fetch messages (error)
                repository.getMessages(request)

                // After error, hasMore should remain true (unchanged)
                repository.hasMoreMessages() shouldBe true
            }
        }
    }

    // ========================================
    // Additional integration-like tests
    // ========================================

    context("Multiple fetch operations") {

        test("hasMoreConversations should update correctly across multiple fetches") {
            runTest {
                val mockDataSource = MockSearchDataSource()
                val repository = SearchRepositoryImpl(mockDataSource)
                val request = ConversationsRequest.ConversationsRequestBuilder().build()

                // First fetch: return conversations
                mockDataSource.conversationsResult = Result.success(createMockConversations(3))
                repository.getConversations(request)
                repository.hasMoreConversations() shouldBe true

                // Second fetch: return more conversations
                mockDataSource.conversationsResult = Result.success(createMockConversations(2))
                repository.getConversations(request)
                repository.hasMoreConversations() shouldBe true

                // Third fetch: return empty (no more data)
                mockDataSource.conversationsResult = Result.success(emptyList())
                repository.getConversations(request)
                repository.hasMoreConversations() shouldBe false
            }
        }

        test("hasMoreMessages should update correctly across multiple fetches") {
            runTest {
                val mockDataSource = MockSearchDataSource()
                val repository = SearchRepositoryImpl(mockDataSource)
                val request = MessagesRequest.MessagesRequestBuilder().build()

                // First fetch: return messages
                mockDataSource.messagesResult = Result.success(createMockMessages(5))
                repository.getMessages(request)
                repository.hasMoreMessages() shouldBe true

                // Second fetch: return more messages
                mockDataSource.messagesResult = Result.success(createMockMessages(3))
                repository.getMessages(request)
                repository.hasMoreMessages() shouldBe true

                // Third fetch: return empty (no more data)
                mockDataSource.messagesResult = Result.success(emptyList())
                repository.getMessages(request)
                repository.hasMoreMessages() shouldBe false
            }
        }

        test("conversations and messages hasMore flags should be independent") {
            runTest {
                val mockDataSource = MockSearchDataSource()
                val repository = SearchRepositoryImpl(mockDataSource)
                val conversationsRequest = ConversationsRequest.ConversationsRequestBuilder().build()
                val messagesRequest = MessagesRequest.MessagesRequestBuilder().build()

                // Fetch conversations with results
                mockDataSource.conversationsResult = Result.success(createMockConversations(3))
                repository.getConversations(conversationsRequest)
                repository.hasMoreConversations() shouldBe true
                repository.hasMoreMessages() shouldBe true // Still default

                // Fetch messages with empty result
                mockDataSource.messagesResult = Result.success(emptyList())
                repository.getMessages(messagesRequest)
                repository.hasMoreConversations() shouldBe true // Unchanged
                repository.hasMoreMessages() shouldBe false // Updated

                // Fetch conversations with empty result
                mockDataSource.conversationsResult = Result.success(emptyList())
                repository.getConversations(conversationsRequest)
                repository.hasMoreConversations() shouldBe false // Updated
                repository.hasMoreMessages() shouldBe false // Unchanged
            }
        }
    }
})
