package com.cometchat.uikit.core.data.repository

import com.cometchat.chat.core.ConversationsRequest
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.Conversation
import com.cometchat.uikit.core.data.datasource.ConversationListDataSource
import com.cometchat.uikit.core.testutils.MockFactory
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Tests for ConversationListRepositoryImpl.
 *
 * This layer wraps DataSource calls in Result<T> and manages pagination state.
 * We mock the ConversationListDataSource interface — no SDK involved.
 *
 * Reference: UsersRepositoryImplTest.kt
 */
class ConversationListRepositoryImplTest : FunSpec({

    lateinit var dataSource: ConversationListDataSource
    lateinit var repository: ConversationListRepositoryImpl

    beforeTest {
        dataSource = mock()
        repository = ConversationListRepositoryImpl(dataSource)
    }

    // ==================== getConversations ====================

    test("getConversations should delegate to dataSource and wrap in Result.success") {
        runTest {
            val conversations = MockFactory.createUserConversations(3)
            val request = mock<ConversationsRequest>()
            whenever(dataSource.fetchConversations(request)).thenReturn(conversations)

            println("  → Testing getConversations delegation with ${conversations.size} conversations")

            val result = repository.getConversations(request)

            result.isSuccess shouldBe true
            result.getOrNull() shouldBe conversations
            verify(dataSource).fetchConversations(request)
            println("  ✅ Result.success with ${result.getOrNull()?.size} conversations")
        }
    }

    test("getConversations should wrap CometChatException in Result.failure") {
        runTest {
            val request = mock<ConversationsRequest>()
            val exception = MockFactory.createCometChatException("ERR_FETCH", "Network error")
            whenever(dataSource.fetchConversations(request)).thenAnswer { throw exception }

            println("  → Testing getConversations CometChatException wrapping")

            val result = repository.getConversations(request)

            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe exception
            println("  ✅ Result.failure with code=${(result.exceptionOrNull() as CometChatException).code}")
        }
    }

    test("getConversations should wrap generic Exception in Result.failure") {
        runTest {
            val request = mock<ConversationsRequest>()
            val exception = RuntimeException("Unexpected error")
            whenever(dataSource.fetchConversations(any())).thenAnswer { throw exception }

            println("  → Testing getConversations generic exception wrapping")

            val result = repository.getConversations(request)

            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe exception
            println("  ✅ Result.failure with RuntimeException")
        }
    }

    // ==================== deleteConversation ====================

    test("deleteConversation should delegate to dataSource and wrap in Result.success") {
        runTest {
            whenever(dataSource.deleteConversation("user-1", "user")).thenReturn("Deleted")

            println("  → Testing deleteConversation delegation")

            val result = repository.deleteConversation("user-1", "user")

            result.isSuccess shouldBe true
            verify(dataSource).deleteConversation("user-1", "user")
            println("  ✅ Result.success(Unit)")
        }
    }

    test("deleteConversation should wrap CometChatException in Result.failure") {
        runTest {
            val exception = MockFactory.createCometChatException("ERR_DELETE", "Delete failed")
            whenever(dataSource.deleteConversation(eq("user-1"), eq("user"))).thenAnswer { throw exception }

            println("  → Testing deleteConversation failure wrapping")

            val result = repository.deleteConversation("user-1", "user")

            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe exception
            println("  ✅ Result.failure with code=${(result.exceptionOrNull() as CometChatException).code}")
        }
    }

    // ==================== markAsDelivered ====================

    test("markAsDelivered should delegate to dataSource and wrap in Result.success") {
        runTest {
            val message = MockFactory.createBaseMessage(id = 42L)
            val conversation = MockFactory.createUserConversation(uid = "user-1", lastMessage = message)

            println("  → Testing markAsDelivered delegation")

            val result = repository.markAsDelivered(conversation)

            result.isSuccess shouldBe true
            verify(dataSource).markAsDelivered(message)
            println("  ✅ Result.success(Unit)")
        }
    }

    test("markAsDelivered should succeed when conversation has no lastMessage") {
        runTest {
            val conversation = MockFactory.createUserConversation(uid = "user-1", lastMessage = null)

            println("  → Testing markAsDelivered with null lastMessage")

            val result = repository.markAsDelivered(conversation)

            result.isSuccess shouldBe true
            println("  ✅ Result.success(Unit) — no dataSource call made")
        }
    }

    // ==================== Pagination State ====================

    test("hasMoreConversations should be true initially") {
        println("  → Testing initial pagination state")
        repository.hasMoreConversations() shouldBe true
        println("  ✅ hasMoreConversations = true initially")
    }

    test("hasMoreConversations should be true when dataSource returns non-empty list") {
        runTest {
            val conversations = MockFactory.createUserConversations(5)
            val request = mock<ConversationsRequest>()
            whenever(dataSource.fetchConversations(request)).thenReturn(conversations)

            println("  → Testing pagination after non-empty fetch")

            repository.getConversations(request)

            repository.hasMoreConversations() shouldBe true
            println("  ✅ hasMoreConversations = true after ${conversations.size} results")
        }
    }

    test("hasMoreConversations should be false when dataSource returns empty list") {
        runTest {
            val request = mock<ConversationsRequest>()
            whenever(dataSource.fetchConversations(request)).thenReturn(emptyList())

            println("  → Testing pagination after empty fetch")

            repository.getConversations(request)

            repository.hasMoreConversations() shouldBe false
            println("  ✅ hasMoreConversations = false after empty result")
        }
    }

    test("hasMoreConversations should remain true when dataSource throws exception") {
        runTest {
            val request = mock<ConversationsRequest>()
            whenever(dataSource.fetchConversations(request)).thenAnswer {
                throw CometChatException("ERR", "Fail")
            }

            println("  → Testing pagination state after exception")

            repository.getConversations(request)

            // Exception path doesn't update hasMore — it stays true
            repository.hasMoreConversations() shouldBe true
            println("  ✅ hasMoreConversations = true (exception doesn't change state)")
        }
    }

    test("pagination state should track across multiple fetches") {
        runTest {
            val ds = mock<ConversationListDataSource>()
            val repo = ConversationListRepositoryImpl(ds)
            val request = mock<ConversationsRequest>()
            val nonEmptyConversations = MockFactory.createUserConversations(10)

            println("  → Testing pagination state across multiple fetches")

            // First fetch: non-empty → hasMore = true
            whenever(ds.fetchConversations(any())).thenReturn(nonEmptyConversations)
            repo.getConversations(request)
            repo.hasMoreConversations() shouldBe true
            println("    Fetch 1: 10 results → hasMore=true")

            // Second fetch: empty → hasMore = false (end of list)
            whenever(ds.fetchConversations(any())).thenReturn(emptyList())
            repo.getConversations(request)
            repo.hasMoreConversations() shouldBe false
            println("    Fetch 2: 0 results → hasMore=false")

            println("  ✅ Pagination state tracked correctly")
        }
    }
})
