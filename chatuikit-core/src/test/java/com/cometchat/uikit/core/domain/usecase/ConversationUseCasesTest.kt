package com.cometchat.uikit.core.domain.usecase

import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.ConversationsRequest
import com.cometchat.chat.models.Conversation
import com.cometchat.uikit.core.domain.repository.ConversationListRepository
import com.cometchat.uikit.core.testutils.MockFactory
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.test.runTest
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Tests for GetConversationListUseCase, DeleteConversationUseCase, and RefreshConversationListUseCase.
 *
 * Use cases are thin delegation layers. We mock the Repository interface
 * and verify parameters are forwarded and results are returned unchanged.
 *
 * Reference: UserUseCasesTest.kt
 */
class ConversationUseCasesTest : FunSpec({

    // ==================== GetConversationListUseCase ====================

    context("GetConversationListUseCase") {

        lateinit var repository: ConversationListRepository
        lateinit var useCase: GetConversationListUseCase

        beforeTest {
            repository = mock()
            useCase = GetConversationListUseCase(repository)
        }

        test("invoke should delegate to repository.getConversations with same request") {
            runTest {
                val request = mock<ConversationsRequest>()
                val conversations = MockFactory.createUserConversations(3)
                whenever(repository.getConversations(request)).thenReturn(Result.success(conversations))

                println("  → Testing GetConversationListUseCase delegation")

                val result = useCase(request)

                result.isSuccess shouldBe true
                result.getOrNull() shouldBe conversations
                verify(repository).getConversations(request)
                println("  ✅ Delegated correctly, returned ${result.getOrNull()?.size} conversations")
            }
        }

        test("invoke should propagate Result.failure unchanged") {
            runTest {
                val request = mock<ConversationsRequest>()
                val exception = MockFactory.createCometChatException("ERR", "Failed")
                whenever(repository.getConversations(request)).thenReturn(Result.failure(exception))

                println("  → Testing GetConversationListUseCase failure propagation")

                val result = useCase(request)

                result.isFailure shouldBe true
                result.exceptionOrNull() shouldBe exception
                println("  ✅ Failure propagated unchanged")
            }
        }

        test("hasMore should delegate to repository.hasMoreConversations") {
            println("  → Testing hasMore delegation")

            whenever(repository.hasMoreConversations()).thenReturn(true)
            useCase.hasMore() shouldBe true

            whenever(repository.hasMoreConversations()).thenReturn(false)
            useCase.hasMore() shouldBe false

            println("  ✅ hasMore delegates correctly")
        }
    }

    // ==================== DeleteConversationUseCase ====================

    context("DeleteConversationUseCase") {

        lateinit var repository: ConversationListRepository
        lateinit var useCase: DeleteConversationUseCase

        beforeTest {
            repository = mock()
            useCase = DeleteConversationUseCase(repository)
        }

        test("invoke should extract user UID and delegate to repository for user conversation") {
            runTest {
                val conversation = MockFactory.createUserConversation(uid = "alice")
                whenever(repository.deleteConversation("alice", CometChatConstants.CONVERSATION_TYPE_USER))
                    .thenReturn(Result.success(Unit))

                println("  → Testing DeleteConversationUseCase with user conversation")

                val result = useCase(conversation)

                result.isSuccess shouldBe true
                verify(repository).deleteConversation("alice", CometChatConstants.CONVERSATION_TYPE_USER)
                println("  ✅ Extracted uid='alice' and delegated correctly")
            }
        }

        test("invoke should extract group GUID and delegate to repository for group conversation") {
            runTest {
                val conversation = MockFactory.createGroupConversation(guid = "team-chat")
                whenever(repository.deleteConversation("team-chat", CometChatConstants.CONVERSATION_TYPE_GROUP))
                    .thenReturn(Result.success(Unit))

                println("  → Testing DeleteConversationUseCase with group conversation")

                val result = useCase(conversation)

                result.isSuccess shouldBe true
                verify(repository).deleteConversation("team-chat", CometChatConstants.CONVERSATION_TYPE_GROUP)
                println("  ✅ Extracted guid='team-chat' and delegated correctly")
            }
        }

        test("invoke should propagate Result.failure from repository") {
            runTest {
                val conversation = MockFactory.createUserConversation(uid = "alice")
                val exception = MockFactory.createCometChatException("ERR_DELETE", "Delete failed")
                whenever(repository.deleteConversation(any(), any())).thenReturn(Result.failure(exception))

                println("  → Testing DeleteConversationUseCase failure propagation")

                val result = useCase(conversation)

                result.isFailure shouldBe true
                result.exceptionOrNull() shouldBe exception
                println("  ✅ Failure propagated unchanged")
            }
        }

        test("invoke should return failure for invalid conversation type") {
            runTest {
                val conversation = mock<Conversation>()
                whenever(conversation.conversationType).thenReturn("invalid_type")

                println("  → Testing DeleteConversationUseCase with invalid type")

                val result = useCase(conversation)

                result.isFailure shouldBe true
                result.exceptionOrNull().shouldBeInstanceOf<IllegalArgumentException>()
                println("  ✅ Returned failure for invalid conversation type")
            }
        }
    }

    // ==================== RefreshConversationListUseCase ====================

    context("RefreshConversationListUseCase") {

        lateinit var repository: ConversationListRepository
        lateinit var useCase: RefreshConversationListUseCase

        beforeTest {
            repository = mock()
            useCase = RefreshConversationListUseCase(repository)
        }

        test("invoke should delegate the caller's request to the repository") {
            runTest {
                val conversations = MockFactory.createUserConversations(5)
                val request = mock<ConversationsRequest>()
                whenever(repository.getConversations(request)).thenReturn(Result.success(conversations))

                println("  → Testing RefreshConversationListUseCase delegation")

                val result = useCase(request)

                result.isSuccess shouldBe true
                result.getOrNull() shouldBe conversations
                verify(repository).getConversations(request)
                println("  ✅ Delegated caller's request, returned ${result.getOrNull()?.size} conversations")
            }
        }

        test("invoke should propagate Result.failure from repository") {
            runTest {
                val exception = MockFactory.createCometChatException("ERR_REFRESH", "Refresh failed")
                val request = mock<ConversationsRequest>()
                whenever(repository.getConversations(request)).thenReturn(Result.failure(exception))

                println("  → Testing RefreshConversationListUseCase failure propagation")

                val result = useCase(request)

                result.isFailure shouldBe true
                result.exceptionOrNull() shouldBe exception
                println("  ✅ Failure propagated unchanged")
            }
        }
    }
})
