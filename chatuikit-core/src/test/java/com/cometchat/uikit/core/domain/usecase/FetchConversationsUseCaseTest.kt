package com.cometchat.uikit.core.domain.usecase

import com.cometchat.chat.core.ConversationsRequest
import com.cometchat.chat.core.MessagesRequest
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Conversation
import com.cometchat.uikit.core.domain.repository.SearchRepository
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.test.runTest

/**
 * Unit tests for FetchConversationsUseCase.
 *
 * Tests verify that:
 * - invoke() calls repository.getConversations() with the provided request
 * - invoke() returns the result from repository
 * - hasMore() delegates to repository.hasMoreConversations()
 *
 * **Validates: Requirements 1.3, 3.3**
 */
class FetchConversationsUseCaseTest : FunSpec({

    /**
     * Mock SearchRepository for testing use case without actual repository dependencies.
     */
    class MockSearchRepository : SearchRepository {
        var conversationsResult: Result<List<Conversation>> = Result.success(emptyList())
        var messagesResult: Result<List<BaseMessage>> = Result.success(emptyList())
        var hasMoreConversationsValue: Boolean = true
        var hasMoreMessagesValue: Boolean = true
        var getConversationsCallCount = 0
        var capturedConversationsRequest: ConversationsRequest? = null

        override suspend fun getConversations(request: ConversationsRequest): Result<List<Conversation>> {
            getConversationsCallCount++
            capturedConversationsRequest = request
            return conversationsResult
        }

        override suspend fun getMessages(request: MessagesRequest): Result<List<BaseMessage>> {
            return messagesResult
        }

        override fun hasMoreConversations(): Boolean = hasMoreConversationsValue

        override fun hasMoreMessages(): Boolean = hasMoreMessagesValue
    }

    /**
     * Helper function to create mock Conversation instances for testing using reflection.
     * The Conversation class has a private constructor, so we use reflection.
     */
    fun createMockConversations(count: Int): List<Conversation> {
        return (1..count).map { id ->
            val constructor = Conversation::class.java.getDeclaredConstructor()
            constructor.isAccessible = true
            val conversation = constructor.newInstance()

            Conversation::class.java.getDeclaredField("conversationId").apply {
                isAccessible = true
                set(conversation, "conv_$id")
            }

            conversation
        }
    }

    // ========================================
    // Test 1: invoke() calls repository.getConversations() with the provided request
    // ========================================

    context("invoke calls repository correctly") {

        test("invoke should call repository.getConversations with the provided request") {
            runTest {
                // Arrange
                val mockRepository = MockSearchRepository()
                val useCase = FetchConversationsUseCase(mockRepository)
                val request = ConversationsRequest.ConversationsRequestBuilder()
                    .setLimit(20)
                    .build()

                // Act
                useCase(request)

                // Assert
                mockRepository.getConversationsCallCount shouldBe 1
                mockRepository.capturedConversationsRequest shouldBe request
            }
        }

        test("invoke should call repository exactly once per invocation") {
            runTest {
                // Arrange
                val mockRepository = MockSearchRepository()
                val useCase = FetchConversationsUseCase(mockRepository)
                val request = ConversationsRequest.ConversationsRequestBuilder().build()

                // Act
                useCase(request)
                useCase(request)
                useCase(request)

                // Assert
                mockRepository.getConversationsCallCount shouldBe 3
            }
        }
    }

    // ========================================
    // Test 2: invoke() returns the result from repository
    // ========================================

    context("invoke returns result from repository") {

        test("invoke should return success with conversations when repository succeeds") {
            runTest {
                // Arrange
                val mockConversations = createMockConversations(3)
                val mockRepository = MockSearchRepository().apply {
                    conversationsResult = Result.success(mockConversations)
                }
                val useCase = FetchConversationsUseCase(mockRepository)
                val request = ConversationsRequest.ConversationsRequestBuilder().build()

                // Act
                val result = useCase(request)

                // Assert
                result.isSuccess shouldBe true
                result.getOrNull()?.size shouldBe 3
                result.getOrNull() shouldBe mockConversations
            }
        }

        test("invoke should return success with empty list when repository returns empty") {
            runTest {
                // Arrange
                val mockRepository = MockSearchRepository().apply {
                    conversationsResult = Result.success(emptyList())
                }
                val useCase = FetchConversationsUseCase(mockRepository)
                val request = ConversationsRequest.ConversationsRequestBuilder().build()

                // Act
                val result = useCase(request)

                // Assert
                result.isSuccess shouldBe true
                result.getOrNull() shouldBe emptyList()
            }
        }

        test("invoke should return failure when repository returns failure") {
            runTest {
                // Arrange
                val exception = CometChatException("ERR_NETWORK", "Network error")
                val mockRepository = MockSearchRepository().apply {
                    conversationsResult = Result.failure(exception)
                }
                val useCase = FetchConversationsUseCase(mockRepository)
                val request = ConversationsRequest.ConversationsRequestBuilder().build()

                // Act
                val result = useCase(request)

                // Assert
                result.isFailure shouldBe true
                result.exceptionOrNull().shouldBeInstanceOf<CometChatException>()
                val caughtException = result.exceptionOrNull() as CometChatException
                caughtException.code shouldBe "ERR_NETWORK"
                caughtException.message shouldBe "Network error"
            }
        }

        test("invoke should return failure with generic exception when repository fails with generic exception") {
            runTest {
                // Arrange
                val exception = RuntimeException("Unexpected error")
                val mockRepository = MockSearchRepository().apply {
                    conversationsResult = Result.failure(exception)
                }
                val useCase = FetchConversationsUseCase(mockRepository)
                val request = ConversationsRequest.ConversationsRequestBuilder().build()

                // Act
                val result = useCase(request)

                // Assert
                result.isFailure shouldBe true
                result.exceptionOrNull().shouldBeInstanceOf<RuntimeException>()
                result.exceptionOrNull()?.message shouldBe "Unexpected error"
            }
        }
    }

    // ========================================
    // Test 3: hasMore() delegates to repository.hasMoreConversations()
    // ========================================

    context("hasMore delegates to repository") {

        test("hasMore should return true when repository.hasMoreConversations returns true") {
            // Arrange
            val mockRepository = MockSearchRepository().apply {
                hasMoreConversationsValue = true
            }
            val useCase = FetchConversationsUseCase(mockRepository)

            // Act & Assert
            useCase.hasMore() shouldBe true
        }

        test("hasMore should return false when repository.hasMoreConversations returns false") {
            // Arrange
            val mockRepository = MockSearchRepository().apply {
                hasMoreConversationsValue = false
            }
            val useCase = FetchConversationsUseCase(mockRepository)

            // Act & Assert
            useCase.hasMore() shouldBe false
        }

        test("hasMore should reflect repository state changes") {
            // Arrange
            val mockRepository = MockSearchRepository()
            val useCase = FetchConversationsUseCase(mockRepository)

            // Initially true
            mockRepository.hasMoreConversationsValue = true
            useCase.hasMore() shouldBe true

            // Change to false
            mockRepository.hasMoreConversationsValue = false
            useCase.hasMore() shouldBe false

            // Change back to true
            mockRepository.hasMoreConversationsValue = true
            useCase.hasMore() shouldBe true
        }
    }
})
