package com.cometchat.uikit.core.data.datasource

import com.cometchat.chat.core.CometChat
import com.cometchat.chat.core.ConversationsRequest
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.Conversation
import com.cometchat.uikit.core.testutils.MockFactory
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * Tests for ConversationListDataSourceImpl.
 *
 * This is the lowest layer — it bridges CometChat SDK callbacks to coroutines.
 * We mock the ConversationsRequest object and simulate SDK callback behavior.
 *
 * Note: deleteConversation and markAsDelivered use CometChat static methods
 * which cannot be mocked without SDK initialization. Those paths are tested
 * at the Repository layer (which mocks the DataSource interface).
 *
 * Reference: UsersDataSourceImplTest.kt
 */
class ConversationListDataSourceImplTest : FunSpec({

    lateinit var dataSource: ConversationListDataSourceImpl

    beforeTest {
        dataSource = ConversationListDataSourceImpl()
    }

    // ==================== fetchConversations ====================

    test("fetchConversations should return conversations when SDK callback succeeds") {
        runTest {
            val mockRequest = mock<ConversationsRequest>()
            val expectedConversations = MockFactory.createUserConversations(3)

            println("  → Testing fetchConversations success with ${expectedConversations.size} conversations")

            whenever(mockRequest.fetchNext(any())).thenAnswer { invocation ->
                @Suppress("UNCHECKED_CAST")
                val callback = invocation.getArgument<CometChat.CallbackListener<List<Conversation>>>(0)
                callback.onSuccess(expectedConversations)
            }

            val result = dataSource.fetchConversations(mockRequest)

            result shouldBe expectedConversations
            result.size shouldBe 3
            println("  ✅ Returned ${result.size} conversations")
        }
    }

    test("fetchConversations should throw CometChatException when SDK callback fails") {
        runTest {
            val mockRequest = mock<ConversationsRequest>()
            val expectedException = MockFactory.createCometChatException("ERR_FETCH", "Network error")

            println("  → Testing fetchConversations failure with code=${expectedException.code}")

            whenever(mockRequest.fetchNext(any())).thenAnswer { invocation ->
                @Suppress("UNCHECKED_CAST")
                val callback = invocation.getArgument<CometChat.CallbackListener<List<Conversation>>>(0)
                callback.onError(expectedException)
            }

            val thrown = shouldThrow<CometChatException> {
                dataSource.fetchConversations(mockRequest)
            }

            thrown.code shouldBe "ERR_FETCH"
            thrown.message shouldBe "Network error"
            println("  ✅ Threw CometChatException with code=${thrown.code}")
        }
    }

    test("fetchConversations should return empty list when SDK returns empty") {
        runTest {
            val mockRequest = mock<ConversationsRequest>()

            println("  → Testing fetchConversations with empty result")

            whenever(mockRequest.fetchNext(any())).thenAnswer { invocation ->
                @Suppress("UNCHECKED_CAST")
                val callback = invocation.getArgument<CometChat.CallbackListener<List<Conversation>>>(0)
                callback.onSuccess(emptyList())
            }

            val result = dataSource.fetchConversations(mockRequest)

            result shouldBe emptyList()
            println("  ✅ Returned empty list")
        }
    }

    test("fetchConversations should return large list when SDK returns many conversations") {
        runTest {
            val mockRequest = mock<ConversationsRequest>()
            val expectedConversations = MockFactory.createUserConversations(20)

            println("  → Testing fetchConversations with ${expectedConversations.size} conversations (large page)")

            whenever(mockRequest.fetchNext(any())).thenAnswer { invocation ->
                @Suppress("UNCHECKED_CAST")
                val callback = invocation.getArgument<CometChat.CallbackListener<List<Conversation>>>(0)
                callback.onSuccess(expectedConversations)
            }

            val result = dataSource.fetchConversations(mockRequest)

            result.size shouldBe 20
            result shouldBe expectedConversations
            println("  ✅ Returned ${result.size} conversations")
        }
    }
})
