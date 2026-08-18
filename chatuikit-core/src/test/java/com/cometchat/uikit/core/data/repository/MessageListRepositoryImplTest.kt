package com.cometchat.uikit.core.data.repository

import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.MessagesRequest
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Conversation
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.data.datasource.MessageListDataSource
import com.cometchat.uikit.core.domain.model.SurroundingMessagesResult
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
 * Tests for MessageListRepositoryImpl.
 *
 * Layer 2 in the 6-layer bottom-up architecture.
 * Wraps DataSource calls in Result<T>, manages pagination state,
 * builds MessagesRequest objects, and handles configuration.
 *
 * Mocks the MessageListDataSource interface — no SDK involved.
 *
 * Reference: ConversationListRepositoryImplTest.kt
 */
class MessageListRepositoryImplTest : FunSpec({

    lateinit var dataSource: MessageListDataSource
    lateinit var repository: MessageListRepositoryImpl

    beforeTest {
        dataSource = mock()
        repository = MessageListRepositoryImpl(dataSource)
    }

    // ==================== Task 3.2: configureForUser ====================

    context("configureForUser") {

        test("configureForUser should set UID and reset hasMore to true") {
            val user = MockFactory.createUser(uid = "alice")

            println("  → Testing configureForUser sets UID and resets hasMore")

            repository.configureForUser(
                user = user,
                messagesTypes = listOf(CometChatConstants.MESSAGE_TYPE_TEXT),
                messagesCategories = listOf(CometChatConstants.CATEGORY_MESSAGE),
                parentMessageId = -1L,
                messagesRequestBuilder = null
            )

            // After configuration, hasMore should be true
            repository.hasMorePreviousMessages() shouldBe true
            println("  ✅ configureForUser: hasMore=true, UID set")
        }

        test("configureForUser should allow fetchPreviousMessages to succeed") {
            runTest {
                val user = MockFactory.createUser(uid = "alice")
                val messages = MockFactory.createMessages(3, senderUid = "bob", receiverId = "alice")

                repository.configureForUser(
                    user = user,
                    messagesTypes = listOf(CometChatConstants.MESSAGE_TYPE_TEXT),
                    messagesCategories = listOf(CometChatConstants.CATEGORY_MESSAGE),
                    parentMessageId = -1L,
                    messagesRequestBuilder = null
                )

                whenever(dataSource.fetchPreviousMessages(any())).thenReturn(messages)

                println("  → Testing configureForUser enables fetchPreviousMessages")

                val result = repository.fetchPreviousMessages()

                result.isSuccess shouldBe true
                result.getOrNull()?.size shouldBe 3
                println("  ✅ fetchPreviousMessages returned ${result.getOrNull()?.size} messages after configureForUser")
            }
        }
    }

    // ==================== Task 3.3: configureForGroup ====================

    context("configureForGroup") {

        test("configureForGroup should set GUID and reset hasMore to true") {
            val group = MockFactory.createGroup(guid = "dev-team")

            println("  → Testing configureForGroup sets GUID and resets hasMore")

            repository.configureForGroup(
                group = group,
                messagesTypes = listOf(CometChatConstants.MESSAGE_TYPE_TEXT),
                messagesCategories = listOf(CometChatConstants.CATEGORY_MESSAGE),
                parentMessageId = -1L,
                messagesRequestBuilder = null
            )

            repository.hasMorePreviousMessages() shouldBe true
            println("  ✅ configureForGroup: hasMore=true, GUID set")
        }

        test("configureForGroup should allow fetchPreviousMessages to succeed") {
            runTest {
                val group = MockFactory.createGroup(guid = "dev-team")
                val messages = MockFactory.createMessages(
                    5, senderUid = "alice", receiverId = "dev-team",
                    receiverType = CometChatConstants.RECEIVER_TYPE_GROUP
                )

                repository.configureForGroup(
                    group = group,
                    messagesTypes = listOf(CometChatConstants.MESSAGE_TYPE_TEXT),
                    messagesCategories = listOf(CometChatConstants.CATEGORY_MESSAGE),
                    parentMessageId = -1L,
                    messagesRequestBuilder = null
                )

                whenever(dataSource.fetchPreviousMessages(any())).thenReturn(messages)

                println("  → Testing configureForGroup enables fetchPreviousMessages")

                val result = repository.fetchPreviousMessages()

                result.isSuccess shouldBe true
                result.getOrNull()?.size shouldBe 5
                println("  ✅ fetchPreviousMessages returned ${result.getOrNull()?.size} messages after configureForGroup")
            }
        }
    }

    // ==================== Task 3.4: configureForUser with parentMessageId and custom builder ====================

    context("configureForUser with parentMessageId and custom builder") {

        test("configureForUser with parentMessageId > -1 should configure for threaded messages") {
            runTest {
                val user = MockFactory.createUser(uid = "alice")
                val threadedMessages = MockFactory.createMessages(2, senderUid = "bob", receiverId = "alice")

                repository.configureForUser(
                    user = user,
                    messagesTypes = listOf(CometChatConstants.MESSAGE_TYPE_TEXT),
                    messagesCategories = listOf(CometChatConstants.CATEGORY_MESSAGE),
                    parentMessageId = 42L,
                    messagesRequestBuilder = null
                )

                whenever(dataSource.fetchPreviousMessages(any())).thenReturn(threadedMessages)

                println("  → Testing configureForUser with parentMessageId=42")

                val result = repository.fetchPreviousMessages()

                result.isSuccess shouldBe true
                result.getOrNull()?.size shouldBe 2
                println("  ✅ Threaded configuration works with parentMessageId=42")
            }
        }

        test("configureForUser with custom builder should use provided builder") {
            runTest {
                val user = MockFactory.createUser(uid = "alice")
                val messages = MockFactory.createMessages(4, senderUid = "bob", receiverId = "alice")
                val customBuilder = mock<MessagesRequest.MessagesRequestBuilder>()
                val mockRequest = mock<MessagesRequest>()

                whenever(customBuilder.setUID(any())).thenReturn(customBuilder)
                whenever(customBuilder.build()).thenReturn(mockRequest)
                whenever(dataSource.fetchPreviousMessages(mockRequest)).thenReturn(messages)

                println("  → Testing configureForUser with custom MessagesRequestBuilder")

                repository.configureForUser(
                    user = user,
                    messagesTypes = listOf(CometChatConstants.MESSAGE_TYPE_TEXT),
                    messagesCategories = listOf(CometChatConstants.CATEGORY_MESSAGE),
                    parentMessageId = -1L,
                    messagesRequestBuilder = customBuilder
                )

                val result = repository.fetchPreviousMessages()

                result.isSuccess shouldBe true
                result.getOrNull()?.size shouldBe 4
                verify(customBuilder).setUID("alice")
                verify(customBuilder).build()
                println("  ✅ Custom builder used: setUID and build called")
            }
        }
    }

    // ==================== Task 3.5: fetchPreviousMessages wraps in Result, updates hasMore ====================

    context("fetchPreviousMessages") {

        test("fetchPreviousMessages should delegate to dataSource and wrap in Result.success") {
            runTest {
                val user = MockFactory.createUser(uid = "alice")
                val messages = MockFactory.createMessages(3, senderUid = "bob", receiverId = "alice")

                repository.configureForUser(user, listOf(), listOf(), -1L, null)
                whenever(dataSource.fetchPreviousMessages(any())).thenReturn(messages)

                println("  → Testing fetchPreviousMessages delegation with ${messages.size} messages")

                val result = repository.fetchPreviousMessages()

                result.isSuccess shouldBe true
                result.getOrNull() shouldBe messages
                verify(dataSource).fetchPreviousMessages(any())
                println("  ✅ Result.success with ${result.getOrNull()?.size} messages")
            }
        }

        test("fetchPreviousMessages should wrap exception in Result.failure") {
            runTest {
                val user = MockFactory.createUser(uid = "alice")
                val exception = MockFactory.createCometChatException("ERR_FETCH", "Network error")

                repository.configureForUser(user, listOf(), listOf(), -1L, null)
                whenever(dataSource.fetchPreviousMessages(any())).thenAnswer { throw exception }

                println("  → Testing fetchPreviousMessages exception wrapping")

                val result = repository.fetchPreviousMessages()

                result.isFailure shouldBe true
                result.exceptionOrNull() shouldBe exception
                println("  ✅ Result.failure with code=${(result.exceptionOrNull() as CometChatException).code}")
            }
        }

        test("fetchPreviousMessages should update hasMore to false when empty list returned") {
            runTest {
                val user = MockFactory.createUser(uid = "alice")

                repository.configureForUser(user, listOf(), listOf(), -1L, null)
                whenever(dataSource.fetchPreviousMessages(any())).thenReturn(emptyList())

                println("  → Testing fetchPreviousMessages updates hasMore on empty result")

                repository.fetchPreviousMessages()

                repository.hasMorePreviousMessages() shouldBe false
                println("  ✅ hasMore=false after empty fetch")
            }
        }

        test("fetchPreviousMessages should keep hasMore true when non-empty list returned") {
            runTest {
                val user = MockFactory.createUser(uid = "alice")
                val messages = MockFactory.createMessages(5, senderUid = "bob", receiverId = "alice")

                repository.configureForUser(user, listOf(), listOf(), -1L, null)
                whenever(dataSource.fetchPreviousMessages(any())).thenReturn(messages)

                println("  → Testing fetchPreviousMessages keeps hasMore=true on non-empty result")

                repository.fetchPreviousMessages()

                repository.hasMorePreviousMessages() shouldBe true
                println("  ✅ hasMore=true after non-empty fetch")
            }
        }
    }

    // ==================== Task 3.6: fetchPreviousMessages when not configured ====================

    context("fetchPreviousMessages when not configured") {

        test("fetchPreviousMessages should return Result.failure when not configured") {
            runTest {
                println("  → Testing fetchPreviousMessages without configuration")

                val result = repository.fetchPreviousMessages()

                result.isFailure shouldBe true
                val exception = result.exceptionOrNull()
                exception.shouldBeInstanceOf<CometChatException>()
                println("  ✅ Result.failure: ${exception?.message}")
            }
        }
    }

    // ==================== Task 3.7: fetchNextMessages ====================

    context("fetchNextMessages") {

        test("fetchNextMessages should build new request with fromMessageId and return Result.success") {
            runTest {
                val user = MockFactory.createUser(uid = "alice")
                val newerMessages = MockFactory.createMessages(4, startId = 100L, senderUid = "bob", receiverId = "alice")

                // Configure with a custom builder we can verify
                val customBuilder = mock<MessagesRequest.MessagesRequestBuilder>()
                val mockRequest = mock<MessagesRequest>()

                whenever(customBuilder.setUID(any())).thenReturn(customBuilder)
                whenever(customBuilder.setMessageId(any())).thenReturn(customBuilder)
                whenever(customBuilder.build()).thenReturn(mockRequest)
                whenever(dataSource.fetchNextMessages(mockRequest)).thenReturn(newerMessages)

                repository.configureForUser(user, listOf(), listOf(), -1L, customBuilder)

                println("  → Testing fetchNextMessages with fromMessageId=50")

                val result = repository.fetchNextMessages(fromMessageId = 50L)

                result.isSuccess shouldBe true
                result.getOrNull()?.size shouldBe 4
                verify(customBuilder).setMessageId(50L)
                verify(dataSource).fetchNextMessages(mockRequest)
                println("  ✅ fetchNextMessages returned ${result.getOrNull()?.size} messages")
            }
        }

        test("fetchNextMessages should wrap exception in Result.failure") {
            runTest {
                val user = MockFactory.createUser(uid = "alice")
                val exception = MockFactory.createCometChatException("ERR_NEXT", "Fetch next failed")

                val customBuilder = mock<MessagesRequest.MessagesRequestBuilder>()
                val mockRequest = mock<MessagesRequest>()

                whenever(customBuilder.setUID(any())).thenReturn(customBuilder)
                whenever(customBuilder.setMessageId(any())).thenReturn(customBuilder)
                whenever(customBuilder.build()).thenReturn(mockRequest)
                whenever(dataSource.fetchNextMessages(mockRequest)).thenAnswer { throw exception }

                repository.configureForUser(user, listOf(), listOf(), -1L, customBuilder)

                println("  → Testing fetchNextMessages failure wrapping")

                val result = repository.fetchNextMessages(fromMessageId = 50L)

                result.isFailure shouldBe true
                result.exceptionOrNull() shouldBe exception
                println("  ✅ Result.failure with code=${(result.exceptionOrNull() as CometChatException).code}")
            }
        }

        test("fetchNextMessages should return Result.failure when not configured") {
            runTest {
                println("  → Testing fetchNextMessages without configuration")

                val result = repository.fetchNextMessages(fromMessageId = 50L)

                result.isFailure shouldBe true
                result.exceptionOrNull().shouldBeInstanceOf<CometChatException>()
                println("  ✅ Result.failure when builder not configured")
            }
        }
    }

    // ==================== Task 3.8: Delegation tests ====================

    context("delegation methods") {

        test("getConversation should delegate to dataSource and wrap in Result.success") {
            runTest {
                val conversation = MockFactory.createUserConversation(uid = "alice")
                whenever(dataSource.getConversation("alice", "user")).thenReturn(conversation)

                println("  → Testing getConversation delegation")

                val result = repository.getConversation("alice", "user")

                result.isSuccess shouldBe true
                result.getOrNull() shouldBe conversation
                verify(dataSource).getConversation("alice", "user")
                println("  ✅ getConversation delegated successfully")
            }
        }

        test("getConversation should wrap exception in Result.failure") {
            runTest {
                val exception = MockFactory.createCometChatException("ERR_CONV", "Not found")
                whenever(dataSource.getConversation("alice", "user")).thenAnswer { throw exception }

                println("  → Testing getConversation failure wrapping")

                val result = repository.getConversation("alice", "user")

                result.isFailure shouldBe true
                result.exceptionOrNull() shouldBe exception
                println("  ✅ getConversation wrapped exception")
            }
        }

        test("getMessage should delegate to dataSource and wrap in Result.success") {
            runTest {
                val message = MockFactory.createTextMessage(id = 42L, text = "Hello")
                whenever(dataSource.getMessage(42L)).thenReturn(message)

                println("  → Testing getMessage delegation")

                val result = repository.getMessage(42L)

                result.isSuccess shouldBe true
                result.getOrNull() shouldBe message
                verify(dataSource).getMessage(42L)
                println("  ✅ getMessage delegated successfully")
            }
        }

        test("getMessage should wrap exception in Result.failure") {
            runTest {
                val exception = MockFactory.createCometChatException("ERR_MSG", "Message not found")
                whenever(dataSource.getMessage(99L)).thenAnswer { throw exception }

                println("  → Testing getMessage failure wrapping")

                val result = repository.getMessage(99L)

                result.isFailure shouldBe true
                result.exceptionOrNull() shouldBe exception
                println("  ✅ getMessage wrapped exception")
            }
        }

        test("deleteMessage should delegate to dataSource using message.id and wrap in Result.success") {
            runTest {
                val message = MockFactory.createTextMessage(id = 10L, text = "Delete me")
                val deletedMessage = MockFactory.createTextMessage(id = 10L, text = "Delete me", deletedAt = 1700000000L)
                whenever(dataSource.deleteMessage(10L)).thenReturn(deletedMessage)

                println("  → Testing deleteMessage delegation")

                val result = repository.deleteMessage(message)

                result.isSuccess shouldBe true
                result.getOrNull() shouldBe deletedMessage
                verify(dataSource).deleteMessage(10L)
                println("  ✅ deleteMessage delegated with message.id=10")
            }
        }

        test("deleteMessage should wrap exception in Result.failure") {
            runTest {
                val message = MockFactory.createTextMessage(id = 10L, text = "Delete me")
                val exception = MockFactory.createCometChatException("ERR_DEL", "Permission denied")
                whenever(dataSource.deleteMessage(10L)).thenAnswer { throw exception }

                println("  → Testing deleteMessage failure wrapping")

                val result = repository.deleteMessage(message)

                result.isFailure shouldBe true
                result.exceptionOrNull() shouldBe exception
                println("  ✅ deleteMessage wrapped exception")
            }
        }

        test("flagMessage should delegate to dataSource and wrap in Result.success") {
            runTest {
                whenever(dataSource.flagMessage(5L, "spam", "Looks like spam")).thenReturn(Unit)

                println("  → Testing flagMessage delegation")

                val result = repository.flagMessage(5L, "spam", "Looks like spam")

                result.isSuccess shouldBe true
                verify(dataSource).flagMessage(5L, "spam", "Looks like spam")
                println("  ✅ flagMessage delegated successfully")
            }
        }

        test("flagMessage should wrap exception in Result.failure") {
            runTest {
                val exception = MockFactory.createCometChatException("ERR_FLAG", "Flag failed")
                whenever(dataSource.flagMessage(5L, "spam", "remark")).thenAnswer { throw exception }

                println("  → Testing flagMessage failure wrapping")

                val result = repository.flagMessage(5L, "spam", "remark")

                result.isFailure shouldBe true
                result.exceptionOrNull() shouldBe exception
                println("  ✅ flagMessage wrapped exception")
            }
        }
    }

    // ==================== Task 3.9: addReaction, removeReaction, markAsDelivered, markAsRead, markAsUnread ====================

    context("reaction and receipt delegation") {

        test("addReaction should delegate to dataSource and wrap in Result.success") {
            runTest {
                val updatedMessage = MockFactory.createTextMessage(id = 7L, text = "Reacted")
                whenever(dataSource.addReaction(7L, "👍")).thenReturn(updatedMessage)

                println("  → Testing addReaction delegation")

                val result = repository.addReaction(7L, "👍")

                result.isSuccess shouldBe true
                result.getOrNull() shouldBe updatedMessage
                verify(dataSource).addReaction(7L, "👍")
                println("  ✅ addReaction delegated successfully")
            }
        }

        test("addReaction should wrap exception in Result.failure") {
            runTest {
                val exception = MockFactory.createCometChatException("ERR_REACT", "Reaction failed")
                whenever(dataSource.addReaction(7L, "👍")).thenAnswer { throw exception }

                println("  → Testing addReaction failure wrapping")

                val result = repository.addReaction(7L, "👍")

                result.isFailure shouldBe true
                result.exceptionOrNull() shouldBe exception
                println("  ✅ addReaction wrapped exception")
            }
        }

        test("removeReaction should delegate to dataSource and wrap in Result.success") {
            runTest {
                val updatedMessage = MockFactory.createTextMessage(id = 7L, text = "Unreacted")
                whenever(dataSource.removeReaction(7L, "👍")).thenReturn(updatedMessage)

                println("  → Testing removeReaction delegation")

                val result = repository.removeReaction(7L, "👍")

                result.isSuccess shouldBe true
                result.getOrNull() shouldBe updatedMessage
                verify(dataSource).removeReaction(7L, "👍")
                println("  ✅ removeReaction delegated successfully")
            }
        }

        test("removeReaction should wrap exception in Result.failure") {
            runTest {
                val exception = MockFactory.createCometChatException("ERR_REACT", "Remove failed")
                whenever(dataSource.removeReaction(7L, "❤️")).thenAnswer { throw exception }

                println("  → Testing removeReaction failure wrapping")

                val result = repository.removeReaction(7L, "❤️")

                result.isFailure shouldBe true
                result.exceptionOrNull() shouldBe exception
                println("  ✅ removeReaction wrapped exception")
            }
        }

        test("markAsDelivered should delegate to dataSource and wrap in Result.success") {
            runTest {
                val message = MockFactory.createTextMessage(id = 20L, text = "Delivered")
                whenever(dataSource.markAsDelivered(message)).thenReturn(Unit)

                println("  → Testing markAsDelivered delegation")

                val result = repository.markAsDelivered(message)

                result.isSuccess shouldBe true
                verify(dataSource).markAsDelivered(message)
                println("  ✅ markAsDelivered delegated successfully")
            }
        }

        test("markAsDelivered should wrap exception in Result.failure") {
            runTest {
                val message = MockFactory.createTextMessage(id = 20L, text = "Delivered")
                val exception = MockFactory.createCometChatException("ERR_DELIVER", "Delivery failed")
                whenever(dataSource.markAsDelivered(message)).thenAnswer { throw exception }

                println("  → Testing markAsDelivered failure wrapping")

                val result = repository.markAsDelivered(message)

                result.isFailure shouldBe true
                result.exceptionOrNull() shouldBe exception
                println("  ✅ markAsDelivered wrapped exception")
            }
        }

        test("markAsRead should delegate to dataSource and wrap in Result.success") {
            runTest {
                val message = MockFactory.createTextMessage(id = 21L, text = "Read")
                whenever(dataSource.markAsRead(message)).thenReturn(Unit)

                println("  → Testing markAsRead delegation")

                val result = repository.markAsRead(message)

                result.isSuccess shouldBe true
                verify(dataSource).markAsRead(message)
                println("  ✅ markAsRead delegated successfully")
            }
        }

        test("markAsRead should wrap exception in Result.failure") {
            runTest {
                val message = MockFactory.createTextMessage(id = 21L, text = "Read")
                val exception = MockFactory.createCometChatException("ERR_READ", "Read failed")
                whenever(dataSource.markAsRead(message)).thenAnswer { throw exception }

                println("  → Testing markAsRead failure wrapping")

                val result = repository.markAsRead(message)

                result.isFailure shouldBe true
                result.exceptionOrNull() shouldBe exception
                println("  ✅ markAsRead wrapped exception")
            }
        }

        test("markAsUnread should delegate to dataSource and wrap in Result.success") {
            runTest {
                val message = MockFactory.createTextMessage(id = 22L, text = "Unread")
                val conversation = MockFactory.createUserConversation(uid = "alice", unreadCount = 1)
                whenever(dataSource.markAsUnread(message)).thenReturn(conversation)

                println("  → Testing markAsUnread delegation")

                val result = repository.markAsUnread(message)

                result.isSuccess shouldBe true
                result.getOrNull() shouldBe conversation
                verify(dataSource).markAsUnread(message)
                println("  ✅ markAsUnread delegated successfully")
            }
        }

        test("markAsUnread should wrap exception in Result.failure") {
            runTest {
                val message = MockFactory.createTextMessage(id = 22L, text = "Unread")
                val exception = MockFactory.createCometChatException("ERR_UNREAD", "Unread failed")
                whenever(dataSource.markAsUnread(message)).thenAnswer { throw exception }

                println("  → Testing markAsUnread failure wrapping")

                val result = repository.markAsUnread(message)

                result.isFailure shouldBe true
                result.exceptionOrNull() shouldBe exception
                println("  ✅ markAsUnread wrapped exception")
            }
        }
    }

    // ==================== Task 3.10: hasMorePreviousMessages ====================

    context("hasMorePreviousMessages") {

        test("hasMorePreviousMessages should be true initially") {
            println("  → Testing initial hasMore state")
            repository.hasMorePreviousMessages() shouldBe true
            println("  ✅ hasMorePreviousMessages=true initially")
        }

        test("hasMorePreviousMessages should be false after empty fetch") {
            runTest {
                val user = MockFactory.createUser(uid = "alice")
                repository.configureForUser(user, listOf(), listOf(), -1L, null)
                whenever(dataSource.fetchPreviousMessages(any())).thenReturn(emptyList())

                println("  → Testing hasMore after empty fetch")

                repository.fetchPreviousMessages()

                repository.hasMorePreviousMessages() shouldBe false
                println("  ✅ hasMorePreviousMessages=false after empty result")
            }
        }

        test("hasMorePreviousMessages should remain true when exception occurs") {
            runTest {
                val user = MockFactory.createUser(uid = "alice")
                repository.configureForUser(user, listOf(), listOf(), -1L, null)
                whenever(dataSource.fetchPreviousMessages(any())).thenAnswer {
                    throw CometChatException("ERR", "Fail")
                }

                println("  → Testing hasMore after exception")

                repository.fetchPreviousMessages()

                // Exception path doesn't update hasMore — it stays true
                repository.hasMorePreviousMessages() shouldBe true
                println("  ✅ hasMorePreviousMessages=true (exception doesn't change state)")
            }
        }

        test("pagination state should track across multiple fetches") {
            runTest {
                val user = MockFactory.createUser(uid = "alice")
                repository.configureForUser(user, listOf(), listOf(), -1L, null)

                println("  → Testing pagination state across multiple fetches")

                // First fetch: non-empty → hasMore = true
                val messages = MockFactory.createMessages(10, senderUid = "bob", receiverId = "alice")
                whenever(dataSource.fetchPreviousMessages(any())).thenReturn(messages)
                repository.fetchPreviousMessages()
                repository.hasMorePreviousMessages() shouldBe true
                println("    Fetch 1: 10 results → hasMore=true")

                // Second fetch: empty → hasMore = false
                whenever(dataSource.fetchPreviousMessages(any())).thenReturn(emptyList())
                repository.fetchPreviousMessages()
                repository.hasMorePreviousMessages() shouldBe false
                println("    Fetch 2: 0 results → hasMore=false")

                println("  ✅ Pagination state tracked correctly")
            }
        }
    }

    // ==================== Task 3.11: resetRequest ====================

    context("resetRequest") {

        test("resetRequest should reset hasMore to true") {
            runTest {
                val user = MockFactory.createUser(uid = "alice")
                repository.configureForUser(user, listOf(), listOf(), -1L, null)
                whenever(dataSource.fetchPreviousMessages(any())).thenReturn(emptyList())

                // Fetch empty to set hasMore to false
                repository.fetchPreviousMessages()
                repository.hasMorePreviousMessages() shouldBe false

                println("  → Testing resetRequest resets hasMore")

                repository.resetRequest()

                repository.hasMorePreviousMessages() shouldBe true
                println("  ✅ resetRequest: hasMore reset to true")
            }
        }

        test("resetRequest should allow fetching again after reset") {
            runTest {
                val user = MockFactory.createUser(uid = "alice")
                repository.configureForUser(user, listOf(), listOf(), -1L, null)

                // First: fetch empty → hasMore=false
                whenever(dataSource.fetchPreviousMessages(any())).thenReturn(emptyList())
                repository.fetchPreviousMessages()
                repository.hasMorePreviousMessages() shouldBe false

                // Reset
                repository.resetRequest()

                // Now fetch should work again
                val messages = MockFactory.createMessages(3, senderUid = "bob", receiverId = "alice")
                whenever(dataSource.fetchPreviousMessages(any())).thenReturn(messages)

                println("  → Testing resetRequest enables fetching again")

                val result = repository.fetchPreviousMessages()

                result.isSuccess shouldBe true
                result.getOrNull()?.size shouldBe 3
                repository.hasMorePreviousMessages() shouldBe true
                println("  ✅ After reset, fetch returned ${result.getOrNull()?.size} messages")
            }
        }

        test("resetRequest on unconfigured repository should not crash") {
            println("  → Testing resetRequest on unconfigured repository")

            // Should not throw
            repository.resetRequest()

            repository.hasMorePreviousMessages() shouldBe true
            println("  ✅ resetRequest on unconfigured repo: no crash")
        }
    }

    // ==================== Task 3.12: rebuildRequestFromMessageId ====================

    context("rebuildRequestFromMessageId") {

        test("rebuildRequestFromMessageId should reset hasMore to true") {
            runTest {
                val user = MockFactory.createUser(uid = "alice")
                repository.configureForUser(user, listOf(), listOf(), -1L, null)
                whenever(dataSource.fetchPreviousMessages(any())).thenReturn(emptyList())

                // Fetch empty to set hasMore to false
                repository.fetchPreviousMessages()
                repository.hasMorePreviousMessages() shouldBe false

                println("  → Testing rebuildRequestFromMessageId resets hasMore")

                repository.rebuildRequestFromMessageId(50L)

                repository.hasMorePreviousMessages() shouldBe true
                println("  ✅ rebuildRequestFromMessageId: hasMore reset to true")
            }
        }

        test("rebuildRequestFromMessageId should allow fetching from new position") {
            runTest {
                val user = MockFactory.createUser(uid = "alice")
                repository.configureForUser(user, listOf(), listOf(), -1L, null)

                repository.rebuildRequestFromMessageId(50L)

                val messages = MockFactory.createMessages(5, startId = 45L, senderUid = "bob", receiverId = "alice")
                whenever(dataSource.fetchPreviousMessages(any())).thenReturn(messages)

                println("  → Testing rebuildRequestFromMessageId enables fetch from new position")

                val result = repository.fetchPreviousMessages()

                result.isSuccess shouldBe true
                result.getOrNull()?.size shouldBe 5
                println("  ✅ Fetched ${result.getOrNull()?.size} messages from rebuilt position")
            }
        }
    }

    // ==================== Task 3.13: fetchSurroundingMessages ====================

    context("fetchSurroundingMessages") {

        test("fetchSurroundingMessages should combine older + target + newer into SurroundingMessagesResult") {
            runTest {
                val user = MockFactory.createUser(uid = "alice")
                val customBuilder = mock<MessagesRequest.MessagesRequestBuilder>()
                val mockRequest = mock<MessagesRequest>()

                whenever(customBuilder.setUID(any())).thenReturn(customBuilder)
                whenever(customBuilder.setMessageId(any())).thenReturn(customBuilder)
                whenever(customBuilder.build()).thenReturn(mockRequest)

                repository.configureForUser(user, listOf(), listOf(), -1L, customBuilder)

                // Target message
                val targetMessage = MockFactory.createTextMessage(id = 50L, text = "Target")
                whenever(dataSource.getMessage(50L)).thenReturn(targetMessage)

                // Older messages (fetched via fetchPreviousMessages with messageId=50)
                val olderMessages = MockFactory.createMessages(5, startId = 45L, senderUid = "bob", receiverId = "alice")
                whenever(dataSource.fetchPreviousMessages(mockRequest)).thenReturn(olderMessages)

                // Newer messages (fetched via fetchNextMessages with messageId=50)
                val newerMessages = MockFactory.createMessages(3, startId = 51L, senderUid = "bob", receiverId = "alice")
                whenever(dataSource.fetchNextMessages(mockRequest)).thenReturn(newerMessages)

                println("  → Testing fetchSurroundingMessages combines older+target+newer")

                val result = repository.fetchSurroundingMessages(50L)

                result.isSuccess shouldBe true
                val surrounding = result.getOrNull()!!
                surrounding.targetMessage shouldBe targetMessage
                surrounding.olderMessages shouldBe olderMessages
                surrounding.newerMessages shouldBe newerMessages
                surrounding.hasMorePrevious shouldBe true
                surrounding.hasMoreNext shouldBe true
                println("  ✅ SurroundingMessagesResult: older=${surrounding.olderMessages.size}, newer=${surrounding.newerMessages.size}")
            }
        }

        test("fetchSurroundingMessages should set hasMorePrevious=false when older is empty") {
            runTest {
                val user = MockFactory.createUser(uid = "alice")
                val customBuilder = mock<MessagesRequest.MessagesRequestBuilder>()
                val mockRequest = mock<MessagesRequest>()

                whenever(customBuilder.setUID(any())).thenReturn(customBuilder)
                whenever(customBuilder.setMessageId(any())).thenReturn(customBuilder)
                whenever(customBuilder.build()).thenReturn(mockRequest)

                repository.configureForUser(user, listOf(), listOf(), -1L, customBuilder)

                val targetMessage = MockFactory.createTextMessage(id = 1L, text = "First message")
                whenever(dataSource.getMessage(1L)).thenReturn(targetMessage)
                whenever(dataSource.fetchPreviousMessages(mockRequest)).thenReturn(emptyList())

                val newerMessages = MockFactory.createMessages(3, startId = 2L, senderUid = "bob", receiverId = "alice")
                whenever(dataSource.fetchNextMessages(mockRequest)).thenReturn(newerMessages)

                println("  → Testing fetchSurroundingMessages with empty older")

                val result = repository.fetchSurroundingMessages(1L)

                result.isSuccess shouldBe true
                val surrounding = result.getOrNull()!!
                surrounding.hasMorePrevious shouldBe false
                surrounding.hasMoreNext shouldBe true
                println("  ✅ hasMorePrevious=false when no older messages")
            }
        }

        test("fetchSurroundingMessages should return failure when getMessage fails") {
            runTest {
                val user = MockFactory.createUser(uid = "alice")
                val customBuilder = mock<MessagesRequest.MessagesRequestBuilder>()
                val mockRequest = mock<MessagesRequest>()

                whenever(customBuilder.setUID(any())).thenReturn(customBuilder)
                whenever(customBuilder.setMessageId(any())).thenReturn(customBuilder)
                whenever(customBuilder.build()).thenReturn(mockRequest)

                repository.configureForUser(user, listOf(), listOf(), -1L, customBuilder)

                val exception = MockFactory.createCometChatException("ERR_MSG", "Message not found")
                whenever(dataSource.getMessage(999L)).thenAnswer { throw exception }

                println("  → Testing fetchSurroundingMessages failure when getMessage fails")

                val result = repository.fetchSurroundingMessages(999L)

                result.isFailure shouldBe true
                println("  ✅ Result.failure when target message not found")
            }
        }

        test("fetchSurroundingMessages should return failure when fetchPreviousMessages fails") {
            runTest {
                val user = MockFactory.createUser(uid = "alice")
                val customBuilder = mock<MessagesRequest.MessagesRequestBuilder>()
                val mockRequest = mock<MessagesRequest>()

                whenever(customBuilder.setUID(any())).thenReturn(customBuilder)
                whenever(customBuilder.setMessageId(any())).thenReturn(customBuilder)
                whenever(customBuilder.build()).thenReturn(mockRequest)

                repository.configureForUser(user, listOf(), listOf(), -1L, customBuilder)

                val targetMessage = MockFactory.createTextMessage(id = 50L, text = "Target")
                whenever(dataSource.getMessage(50L)).thenReturn(targetMessage)

                val exception = MockFactory.createCometChatException("ERR_PREV", "Fetch older failed")
                whenever(dataSource.fetchPreviousMessages(mockRequest)).thenAnswer { throw exception }

                println("  → Testing fetchSurroundingMessages failure when fetchPrevious fails")

                val result = repository.fetchSurroundingMessages(50L)

                result.isFailure shouldBe true
                println("  ✅ Result.failure when fetchPreviousMessages throws")
            }
        }

        test("fetchSurroundingMessages should return failure when fetchNextMessages fails") {
            runTest {
                val user = MockFactory.createUser(uid = "alice")
                val customBuilder = mock<MessagesRequest.MessagesRequestBuilder>()
                val mockRequest = mock<MessagesRequest>()

                whenever(customBuilder.setUID(any())).thenReturn(customBuilder)
                whenever(customBuilder.setMessageId(any())).thenReturn(customBuilder)
                whenever(customBuilder.build()).thenReturn(mockRequest)

                repository.configureForUser(user, listOf(), listOf(), -1L, customBuilder)

                val targetMessage = MockFactory.createTextMessage(id = 50L, text = "Target")
                whenever(dataSource.getMessage(50L)).thenReturn(targetMessage)

                val olderMessages = MockFactory.createMessages(5, startId = 45L, senderUid = "bob", receiverId = "alice")
                whenever(dataSource.fetchPreviousMessages(mockRequest)).thenReturn(olderMessages)

                val exception = MockFactory.createCometChatException("ERR_NEXT", "Fetch newer failed")
                whenever(dataSource.fetchNextMessages(mockRequest)).thenAnswer { throw exception }

                println("  → Testing fetchSurroundingMessages failure when fetchNext fails")

                val result = repository.fetchSurroundingMessages(50L)

                result.isFailure shouldBe true
                println("  ✅ Result.failure when fetchNextMessages throws")
            }
        }

        test("fetchSurroundingMessages should return failure when not configured") {
            runTest {
                println("  → Testing fetchSurroundingMessages without configuration")

                val result = repository.fetchSurroundingMessages(50L)

                result.isFailure shouldBe true
                result.exceptionOrNull().shouldBeInstanceOf<CometChatException>()
                println("  ✅ Result.failure when builder not configured")
            }
        }
    }

    // ==================== Task 3.14: fetchActionMessages ====================

    context("fetchActionMessages") {

        test("fetchActionMessages should build ACTION category request for user conversation") {
            runTest {
                val user = MockFactory.createUser(uid = "alice")
                repository.configureForUser(user, listOf(), listOf(), -1L, null)

                val actionMessages = listOf(
                    MockFactory.createActionMessage(id = 100L, action = "edited", senderUid = "bob", receiverId = "alice"),
                    MockFactory.createActionMessage(id = 101L, action = "deleted", senderUid = "bob", receiverId = "alice")
                )
                whenever(dataSource.fetchNextMessages(any())).thenReturn(actionMessages)

                println("  → Testing fetchActionMessages for user conversation")

                val result = repository.fetchActionMessages(fromMessageId = 50L)

                result.isSuccess shouldBe true
                result.getOrNull()?.size shouldBe 2
                verify(dataSource).fetchNextMessages(any())
                println("  ✅ fetchActionMessages returned ${result.getOrNull()?.size} action messages")
            }
        }

        test("fetchActionMessages should build ACTION category request for group conversation") {
            runTest {
                val group = MockFactory.createGroup(guid = "dev-team")
                repository.configureForGroup(group, listOf(), listOf(), -1L, null)

                val actionMessages = listOf(
                    MockFactory.createActionMessage(id = 200L, action = "kicked", receiverId = "dev-team")
                )
                whenever(dataSource.fetchNextMessages(any())).thenReturn(actionMessages)

                println("  → Testing fetchActionMessages for group conversation")

                val result = repository.fetchActionMessages(fromMessageId = 100L)

                result.isSuccess shouldBe true
                result.getOrNull()?.size shouldBe 1
                println("  ✅ fetchActionMessages returned ${result.getOrNull()?.size} action messages for group")
            }
        }

        test("fetchActionMessages should wrap exception in Result.failure") {
            runTest {
                val user = MockFactory.createUser(uid = "alice")
                repository.configureForUser(user, listOf(), listOf(), -1L, null)

                val exception = MockFactory.createCometChatException("ERR_ACTION", "Action fetch failed")
                whenever(dataSource.fetchNextMessages(any())).thenAnswer { throw exception }

                println("  → Testing fetchActionMessages failure wrapping")

                val result = repository.fetchActionMessages(fromMessageId = 50L)

                result.isFailure shouldBe true
                result.exceptionOrNull() shouldBe exception
                println("  ✅ fetchActionMessages wrapped exception")
            }
        }

        test("fetchActionMessages should return failure when not configured") {
            runTest {
                println("  → Testing fetchActionMessages without configuration")

                val result = repository.fetchActionMessages(fromMessageId = 50L)

                result.isFailure shouldBe true
                result.exceptionOrNull().shouldBeInstanceOf<CometChatException>()
                println("  ✅ Result.failure when repository not configured")
            }
        }
    }

    // ==================== Task 3.15: getLatestMessageId / setLatestMessageId ====================

    context("latestMessageId getter/setter") {

        test("getLatestMessageId should return -1 initially") {
            println("  → Testing initial latestMessageId")

            repository.getLatestMessageId() shouldBe -1L
            println("  ✅ getLatestMessageId=-1 initially")
        }

        test("setLatestMessageId should update the stored value") {
            println("  → Testing setLatestMessageId")

            repository.setLatestMessageId(42L)

            repository.getLatestMessageId() shouldBe 42L
            println("  ✅ getLatestMessageId=42 after set")
        }

        test("setLatestMessageId should allow overwriting with a new value") {
            println("  → Testing setLatestMessageId overwrite")

            repository.setLatestMessageId(10L)
            repository.getLatestMessageId() shouldBe 10L

            repository.setLatestMessageId(99L)
            repository.getLatestMessageId() shouldBe 99L
            println("  ✅ latestMessageId updated from 10 to 99")
        }

        test("setLatestMessageId should accept zero") {
            println("  → Testing setLatestMessageId with zero")

            repository.setLatestMessageId(0L)

            repository.getLatestMessageId() shouldBe 0L
            println("  ✅ getLatestMessageId=0 after setting zero")
        }
    }

    // ==================== ENG-38259: effective filter accessors ====================

    context("effective messages filter") {

        test("a custom builder's categories and types win over the defaults") {
            println("  → Testing effective filter comes from the caller's builder")

            val customBuilder = MessagesRequest.MessagesRequestBuilder()
                .setLimit(30)
                .setCategories(listOf(CometChatConstants.CATEGORY_CUSTOM))
                .setTypes(listOf("ban_notice"))

            repository.configureForGroup(
                group = MockFactory.createGroup(guid = "test-group"),
                messagesTypes = listOf(CometChatConstants.MESSAGE_TYPE_TEXT),
                messagesCategories = listOf(CometChatConstants.CATEGORY_MESSAGE),
                parentMessageId = -1L,
                messagesRequestBuilder = customBuilder
            )

            repository.getEffectiveMessagesCategories() shouldBe listOf(CometChatConstants.CATEGORY_CUSTOM)
            repository.getEffectiveMessagesTypes() shouldBe listOf("ban_notice")
            println("  ✅ builder's filter reported, defaults ignored")
        }

        test("without a custom builder the supplied defaults are reported") {
            println("  → Testing effective filter falls back to the defaults")

            repository.configureForUser(
                user = MockFactory.createUser(uid = "alice"),
                messagesTypes = listOf(CometChatConstants.MESSAGE_TYPE_TEXT),
                messagesCategories = listOf(CometChatConstants.CATEGORY_MESSAGE),
                parentMessageId = -1L,
                messagesRequestBuilder = null
            )

            repository.getEffectiveMessagesCategories() shouldBe listOf(CometChatConstants.CATEGORY_MESSAGE)
            repository.getEffectiveMessagesTypes() shouldBe listOf(CometChatConstants.MESSAGE_TYPE_TEXT)
            println("  ✅ defaults reported when no builder is supplied")
        }

        test("an unconfigured repository reports no restriction") {
            println("  → Testing effective filter before configuration")

            repository.getEffectiveMessagesCategories() shouldBe emptyList()
            repository.getEffectiveMessagesTypes() shouldBe emptyList()
            println("  ✅ empty means no restriction")
        }
    }
})
