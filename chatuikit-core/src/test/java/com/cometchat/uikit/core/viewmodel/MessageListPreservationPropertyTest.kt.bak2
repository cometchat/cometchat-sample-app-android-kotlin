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
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.property.Arb
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
 * Preservation Property Tests for Message List Behavior.
 *
 * **Validates: Requirements 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.7, 3.8**
 *
 * ## Purpose
 *
 * These tests verify that existing behaviors are PRESERVED after the reverseLayout=false fix.
 * They test behaviors that should NOT change regardless of the message ordering fix.
 *
 * ## Property 2: Preservation - Pagination and Real-time Behavior
 *
 * _For any_ input that does NOT involve message list ordering (pagination triggers,
 * real-time events, message updates), the fixed code SHALL produce exactly the same
 * behavior as the original code, preserving all existing functionality.
 *
 * ## Test Strategy
 *
 * These tests should PASS on the UNFIXED code because they test behaviors that
 * must be preserved. If any test fails, it indicates a regression.
 *
 * Feature: message-list-reverse-false-fix
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MessageListPreservationPropertyTest : FunSpec({

    val testDispatcher = StandardTestDispatcher()

    beforeSpec {
        Dispatchers.setMain(testDispatcher)
    }

    afterSpec {
        Dispatchers.resetMain()
    }

    // ========================================================================
    // Helper Functions
    // ========================================================================

    /**
     * Creates a mock TextMessage with specified id and sentAt timestamp.
     *
     * @param id Message ID
     * @param sentAt Timestamp in milliseconds
     * @param senderId Optional sender UID (defaults to "sender_{id}")
     * @param readAt Optional read timestamp (0 = not read)
     * @param deliveredAt Optional delivered timestamp (0 = not delivered)
     */
    fun createMessage(
        id: Long,
        sentAt: Long,
        senderId: String = "sender_$id",
        readAt: Long = 0,
        deliveredAt: Long = 0
    ): BaseMessage {
        return TextMessage(
            "receiver_$id",
            "Test message $id",
            CometChatConstants.RECEIVER_TYPE_USER
        ).apply {
            this.id = id
            this.sentAt = sentAt
            this.readAt = readAt
            this.deliveredAt = deliveredAt
            this.sender = User().apply { uid = senderId }
        }
    }

    /**
     * Creates a list of messages in chronological order [oldest, ..., newest].
     *
     * @param count Number of messages to create
     * @param startId Starting message ID
     * @param startSentAt Starting timestamp
     */
    fun createChronologicalMessages(
        count: Int,
        startId: Long = 1L,
        startSentAt: Long = 1000L
    ): List<BaseMessage> {
        return (0 until count).map { i ->
            createMessage(
                id = startId + i,
                sentAt = startSentAt + (i * 100)
            )
        }
    }

    /**
     * Creates a mock Conversation using reflection (constructor is private).
     *
     * @param conversationId The conversation ID
     * @param lastMessageId The last message ID
     * @param lastReadMessageId The last read message ID
     * @param unreadCount The number of unread messages
     */
    fun createMockConversation(
        conversationId: String = "test_conversation",
        lastMessageId: Long = 100L,
        lastReadMessageId: Long = 50L,
        unreadCount: Int = 10
    ): Conversation {
        val lastMessage = if (lastMessageId > 0) {
            createMessage(id = lastMessageId, sentAt = lastMessageId * 100)
        } else {
            null
        }

        // Use reflection to create Conversation since constructor is private
        val constructor = Conversation::class.java.getDeclaredConstructor()
        constructor.isAccessible = true
        val conversation = constructor.newInstance()

        // Set fields using reflection
        Conversation::class.java.getDeclaredField("conversationId").apply {
            isAccessible = true
            set(conversation, conversationId)
        }
        Conversation::class.java.getDeclaredField("lastMessage").apply {
            isAccessible = true
            set(conversation, lastMessage)
        }
        Conversation::class.java.getDeclaredField("lastReadMessageId").apply {
            isAccessible = true
            set(conversation, lastReadMessageId.toString())
        }
        Conversation::class.java.getDeclaredField("unreadMessageCount").apply {
            isAccessible = true
            setInt(conversation, unreadCount)
        }

        return conversation
    }


    /**
     * Mock repository for testing ViewModel without SDK dependencies.
     */
    class MockMessageListRepository : MessageListRepository {
        var fetchPreviousMessagesResult: Result<List<BaseMessage>> = Result.success(emptyList())
        var fetchNextMessagesResult: Result<List<BaseMessage>> = Result.success(emptyList())
        var getConversationResult: Result<Conversation>? = null
        var getMessageResult: Result<BaseMessage>? = null
        var fetchSurroundingMessagesResult: Result<SurroundingMessagesResult>? = null
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
        var fetchPreviousCallCount: Int = 0
        var fetchNextCallCount: Int = 0
        var lastFetchNextFromId: Long = -1
        var rebuildRequestFromMessageIdCalled: Boolean = false
        var rebuildRequestMessageId: Long = -1

        override suspend fun fetchPreviousMessages(): Result<List<BaseMessage>> {
            fetchPreviousCallCount++
            return fetchPreviousMessagesResult
        }

        override suspend fun fetchNextMessages(fromMessageId: Long): Result<List<BaseMessage>> {
            fetchNextCallCount++
            lastFetchNextFromId = fromMessageId
            return fetchNextMessagesResult
        }

        override suspend fun getConversation(id: String, type: String): Result<Conversation> {
            return getConversationResult ?: Result.failure(Exception("Not configured"))
        }

        override suspend fun getMessage(messageId: Long): Result<BaseMessage> {
            return getMessageResult ?: Result.failure(Exception("Not configured"))
        }

        override suspend fun fetchSurroundingMessages(messageId: Long): Result<SurroundingMessagesResult> {
            return fetchSurroundingMessagesResult ?: Result.failure(Exception("Not configured"))
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

        override suspend fun markAsRead(message: BaseMessage): Result<Unit> = markAsReadResult
        override suspend fun markAsDelivered(message: BaseMessage): Result<Unit> = markAsDeliveredResult
        override suspend fun markAsUnread(message: BaseMessage): Result<Conversation> = markAsUnreadResult
        override fun hasMorePreviousMessages(): Boolean = hasMorePrevious
        override fun resetRequest() { hasMorePrevious = true }

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

        override suspend fun fetchActionMessages(fromMessageId: Long) = Result.success<List<BaseMessage>>(emptyList())

        override fun rebuildRequestFromMessageId(messageId: Long) {
            rebuildRequestFromMessageIdCalled = true
            rebuildRequestMessageId = messageId
        }

        private var latestMessageId: Long = -1
        override fun getLatestMessageId(): Long = latestMessageId
        override fun setLatestMessageId(messageId: Long) { latestMessageId = messageId }
    }


    // ========================================================================
    // Property 2.1: Pagination - fetchPrevious() loads older messages
    // Validates: Requirement 3.1
    // ========================================================================

    context("Property 2.1: Pagination - fetchPrevious() loads older messages (Requirement 3.1)") {

        test("fetchMessages() should call repository.fetchPreviousMessages()") {
            runTest {
                val repository = MockMessageListRepository()
                val messages = createChronologicalMessages(5)
                repository.fetchPreviousMessagesResult = Result.success(messages)

                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)

                viewModel.fetchMessages()
                advanceUntilIdle()

                repository.fetchPreviousCallCount shouldBe 1
            }
        }

        test("fetchMessages() should prepend older messages to existing list") {
            runTest {
                val repository = MockMessageListRepository()
                // Initial messages: IDs 10, 11, 12
                val initialMessages = createChronologicalMessages(3, startId = 10, startSentAt = 1000)
                repository.fetchPreviousMessagesResult = Result.success(initialMessages)

                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)

                viewModel.fetchMessages()
                advanceUntilIdle()

                viewModel.messages.value.size shouldBe 3

                // Now fetch older messages: IDs 1, 2, 3
                val olderMessages = createChronologicalMessages(3, startId = 1, startSentAt = 100)
                repository.fetchPreviousMessagesResult = Result.success(olderMessages)

                viewModel.fetchMessages()
                advanceUntilIdle()

                // Should have 6 messages total
                viewModel.messages.value.size shouldBe 6

                // Older messages should be at the beginning (lower indices)
                val messageIds = viewModel.messages.value.map { it.id }
                messageIds shouldContainAll listOf(1L, 2L, 3L, 10L, 11L, 12L)
            }
        }

        /**
         * **Validates: Requirement 3.1**
         *
         * Property: For any number of pagination calls, fetchPrevious() should
         * always be called and older messages should be prepended.
         */
        test("Property 2.1: fetchMessages() should always call fetchPreviousMessages()") {
            checkAll(20, Arb.int(1, 10)) { messageCount ->
                runTest {
                    val repository = MockMessageListRepository()
                    val messages = createChronologicalMessages(messageCount)
                    repository.fetchPreviousMessagesResult = Result.success(messages)

                    val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)

                    viewModel.fetchMessages()
                    advanceUntilIdle()

                    repository.fetchPreviousCallCount shouldBe 1
                    viewModel.messages.value.size shouldBe messageCount
                }
            }
        }

        test("fetchMessages() should update hasMorePreviousMessages based on result") {
            runTest {
                val repository = MockMessageListRepository()
                // Non-empty result means more messages might be available
                val messages = createChronologicalMessages(3)
                repository.fetchPreviousMessagesResult = Result.success(messages)

                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)

                viewModel.fetchMessages()
                advanceUntilIdle()

                viewModel.hasMorePreviousMessages.value shouldBe true

                // Empty result means no more messages
                repository.fetchPreviousMessagesResult = Result.success(emptyList())

                viewModel.fetchMessages()
                advanceUntilIdle()

                viewModel.hasMorePreviousMessages.value shouldBe false
            }
        }
    }


    // ========================================================================
    // Property 2.2: Real-time messages displayed at bottom
    // Validates: Requirement 3.2
    // ========================================================================

    context("Property 2.2: Real-time messages displayed at bottom (Requirement 3.2)") {

        test("addItem() should append new message to end of list") {
            runTest {
                val repository = MockMessageListRepository()
                val initialMessages = createChronologicalMessages(3, startId = 1, startSentAt = 1000)
                repository.fetchPreviousMessagesResult = Result.success(initialMessages)

                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)

                viewModel.fetchMessages()
                advanceUntilIdle()

                // Add a new real-time message
                val newMessage = createMessage(id = 100, sentAt = 2000)
                viewModel.addItem(newMessage)

                // New message should be at the end (last index)
                viewModel.messages.value.last().id shouldBe 100L
                viewModel.messages.value.size shouldBe 4
            }
        }

        test("addMessage() should append message to end of list") {
            runTest {
                val repository = MockMessageListRepository()
                val initialMessages = createChronologicalMessages(2, startId = 1, startSentAt = 1000)
                repository.fetchPreviousMessagesResult = Result.success(initialMessages)

                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)

                viewModel.fetchMessages()
                advanceUntilIdle()

                val newMessage = createMessage(id = 999, sentAt = 5000)
                viewModel.addMessage(newMessage)

                viewModel.messages.value.last().id shouldBe 999L
            }
        }

        /**
         * **Validates: Requirement 3.2**
         *
         * Property: For any new message added via addItem(), it should always
         * appear at the end of the list (highest index).
         */
        test("Property 2.2: New messages should always be appended at end") {
            checkAll(20, Arb.int(1, 10), Arb.long(100, 1000)) { initialCount, newMessageId ->
                runTest {
                    val repository = MockMessageListRepository()
                    val initialMessages = createChronologicalMessages(initialCount)
                    repository.fetchPreviousMessagesResult = Result.success(initialMessages)

                    val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)

                    viewModel.fetchMessages()
                    advanceUntilIdle()

                    val newMessage = createMessage(id = newMessageId, sentAt = 99999)
                    viewModel.addItem(newMessage)

                    // New message should always be at the end
                    viewModel.messages.value.last().id shouldBe newMessageId
                    viewModel.messages.value.size shouldBe initialCount + 1
                }
            }
        }

        test("Multiple addItem() calls should maintain order of addition") {
            runTest {
                val repository = MockMessageListRepository()
                repository.fetchPreviousMessagesResult = Result.success(emptyList())

                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)

                viewModel.fetchMessages()
                advanceUntilIdle()

                // Add messages in order
                viewModel.addItem(createMessage(id = 1, sentAt = 1000))
                viewModel.addItem(createMessage(id = 2, sentAt = 2000))
                viewModel.addItem(createMessage(id = 3, sentAt = 3000))

                val messageIds = viewModel.messages.value.map { it.id }
                messageIds shouldBe listOf(1L, 2L, 3L)
            }
        }
    }


    // ========================================================================
    // Property 2.3: startFromUnreadMessages functionality
    // Validates: Requirement 3.3
    // ========================================================================

    context("Property 2.3: startFromUnreadMessages functionality (Requirement 3.3)") {

        test("fetchMessagesWithUnreadCount() should call goToMessage when startFromUnreadMessages is enabled") {
            runTest {
                val repository = MockMessageListRepository()

                // Setup conversation with unread messages
                val conversation = createMockConversation(
                    conversationId = "test_conv",
                    lastMessageId = 60,
                    lastReadMessageId = 50,
                    unreadCount = 10
                )
                repository.getConversationResult = Result.success(conversation)

                // Setup goToMessage response
                val targetMessage = createMessage(id = 50, sentAt = 5000)
                repository.getMessageResult = Result.success(targetMessage)

                val surroundingResult = SurroundingMessagesResult(
                    targetMessage = targetMessage,
                    olderMessages = createChronologicalMessages(5, startId = 45, startSentAt = 4500),
                    newerMessages = createChronologicalMessages(5, startId = 51, startSentAt = 5100),
                    hasMorePrevious = true,
                    hasMoreNext = true
                )
                repository.fetchSurroundingMessagesResult = Result.success(surroundingResult)

                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)

                // Configure for user
                val user = User().apply { uid = "test_user" }
                viewModel.setUser(user)

                // Enable startFromUnreadMessages
                viewModel.setStartFromUnreadMessages(true)

                viewModel.fetchMessagesWithUnreadCount()
                advanceUntilIdle()

                // Should have navigated to the lastReadMessageId
                viewModel.scrollToMessageId.value shouldBe 50L
            }
        }

        test("startFromUnreadMessages should not trigger when unreadCount is 0") {
            runTest {
                val repository = MockMessageListRepository()

                // Setup conversation with NO unread messages
                val conversation = createMockConversation(
                    conversationId = "test_conv",
                    lastMessageId = 60,
                    lastReadMessageId = 50,
                    unreadCount = 0
                )
                repository.getConversationResult = Result.success(conversation)

                // Setup regular fetchMessages response
                val messages = createChronologicalMessages(5)
                repository.fetchPreviousMessagesResult = Result.success(messages)

                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)

                val user = User().apply { uid = "test_user" }
                viewModel.setUser(user)
                viewModel.setStartFromUnreadMessages(true)

                viewModel.fetchMessagesWithUnreadCount()
                advanceUntilIdle()

                // Should have called fetchMessages instead of goToMessage
                repository.fetchPreviousCallCount shouldBe 1
                viewModel.scrollToMessageId.value shouldBe null
            }
        }

        /**
         * **Validates: Requirement 3.3**
         *
         * Property: When startFromUnreadMessages is enabled AND there are unread messages,
         * the system should navigate to the first unread message position.
         */
        test("Property 2.3: startFromUnreadMessages should navigate to lastReadMessageId when unread > 0") {
            checkAll(10, Arb.int(1, 100), Arb.long(1, 1000)) { unreadCount, lastReadId ->
                runTest {
                    val repository = MockMessageListRepository()

                    val conversation = createMockConversation(
                        conversationId = "test_conv",
                        lastMessageId = lastReadId + 10,
                        lastReadMessageId = lastReadId,
                        unreadCount = unreadCount
                    )
                    repository.getConversationResult = Result.success(conversation)

                    val targetMessage = createMessage(id = lastReadId, sentAt = 5000)
                    repository.getMessageResult = Result.success(targetMessage)

                    val surroundingResult = SurroundingMessagesResult(
                        targetMessage = targetMessage,
                        olderMessages = emptyList(),
                        newerMessages = emptyList(),
                        hasMorePrevious = true,
                        hasMoreNext = true
                    )
                    repository.fetchSurroundingMessagesResult = Result.success(surroundingResult)

                    val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)

                    val user = User().apply { uid = "test_user" }
                    viewModel.setUser(user)
                    viewModel.setStartFromUnreadMessages(true)

                    viewModel.fetchMessagesWithUnreadCount()
                    advanceUntilIdle()

                    // Should navigate to lastReadMessageId
                    viewModel.scrollToMessageId.value shouldBe lastReadId
                }
            }
        }
    }


    // ========================================================================
    // Property 2.4: gotoMessageId navigation
    // Validates: Requirement 3.4
    // ========================================================================

    context("Property 2.4: gotoMessageId navigation (Requirement 3.4)") {

        test("goToMessage() should fetch target message and surrounding messages") {
            runTest {
                val repository = MockMessageListRepository()

                val targetMessage = createMessage(id = 50, sentAt = 5000)
                repository.getMessageResult = Result.success(targetMessage)

                val surroundingResult = SurroundingMessagesResult(
                    targetMessage = targetMessage,
                    olderMessages = createChronologicalMessages(3, startId = 47, startSentAt = 4700),
                    newerMessages = createChronologicalMessages(3, startId = 51, startSentAt = 5100),
                    hasMorePrevious = true,
                    hasMoreNext = true
                )
                repository.fetchSurroundingMessagesResult = Result.success(surroundingResult)

                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)

                viewModel.goToMessage(50)
                advanceUntilIdle()

                // Should have messages from surrounding fetch
                viewModel.messages.value.size shouldBe 7 // 3 older + 1 target + 3 newer
                viewModel.scrollToMessageId.value shouldBe 50L
            }
        }

        test("goToMessage() should set highlightScroll when highlight=true") {
            runTest {
                val repository = MockMessageListRepository()

                val targetMessage = createMessage(id = 25, sentAt = 2500)
                repository.getMessageResult = Result.success(targetMessage)

                val surroundingResult = SurroundingMessagesResult(
                    targetMessage = targetMessage,
                    olderMessages = emptyList(),
                    newerMessages = emptyList(),
                    hasMorePrevious = false,
                    hasMoreNext = false
                )
                repository.fetchSurroundingMessagesResult = Result.success(surroundingResult)

                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)

                viewModel.goToMessage(25, highlight = true)
                advanceUntilIdle()

                viewModel.highlightScroll.value shouldBe true
            }
        }

        test("goToMessage() should NOT set highlightScroll when highlight=false") {
            runTest {
                val repository = MockMessageListRepository()

                val targetMessage = createMessage(id = 25, sentAt = 2500)
                repository.getMessageResult = Result.success(targetMessage)

                val surroundingResult = SurroundingMessagesResult(
                    targetMessage = targetMessage,
                    olderMessages = emptyList(),
                    newerMessages = emptyList(),
                    hasMorePrevious = false,
                    hasMoreNext = false
                )
                repository.fetchSurroundingMessagesResult = Result.success(surroundingResult)

                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)

                viewModel.goToMessage(25, highlight = false)
                advanceUntilIdle()

                viewModel.highlightScroll.value shouldBe false
            }
        }

        /**
         * **Validates: Requirement 3.4**
         *
         * Property: For any valid messageId, goToMessage() should navigate to that
         * message and emit the scrollToMessageId.
         */
        test("Property 2.4: goToMessage() should always emit scrollToMessageId for valid message") {
            checkAll(20, Arb.long(1, 1000)) { messageId ->
                runTest {
                    val repository = MockMessageListRepository()

                    val targetMessage = createMessage(id = messageId, sentAt = messageId * 100)
                    repository.getMessageResult = Result.success(targetMessage)

                    val surroundingResult = SurroundingMessagesResult(
                        targetMessage = targetMessage,
                        olderMessages = emptyList(),
                        newerMessages = emptyList(),
                        hasMorePrevious = false,
                        hasMoreNext = false
                    )
                    repository.fetchSurroundingMessagesResult = Result.success(surroundingResult)

                    val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)

                    viewModel.goToMessage(messageId)
                    advanceUntilIdle()

                    viewModel.scrollToMessageId.value shouldBe messageId
                    viewModel.messages.value.any { it.id == messageId } shouldBe true
                }
            }
        }

        test("goToMessage() should update pagination flags from surrounding result") {
            runTest {
                val repository = MockMessageListRepository()

                val targetMessage = createMessage(id = 50, sentAt = 5000)
                repository.getMessageResult = Result.success(targetMessage)

                val surroundingResult = SurroundingMessagesResult(
                    targetMessage = targetMessage,
                    olderMessages = createChronologicalMessages(2, startId = 48, startSentAt = 4800),
                    newerMessages = createChronologicalMessages(2, startId = 51, startSentAt = 5100),
                    hasMorePrevious = true,
                    hasMoreNext = true
                )
                repository.fetchSurroundingMessagesResult = Result.success(surroundingResult)

                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)

                viewModel.goToMessage(50)
                advanceUntilIdle()

                viewModel.hasMorePreviousMessages.value shouldBe true
                viewModel.hasMoreNewMessages.value shouldBe true
            }
        }
    }


    // ========================================================================
    // Property 2.5: fetchNextMessages() loads newer messages
    // Validates: Requirement 3.6 (pagination maintains position)
    // ========================================================================

    context("Property 2.5: fetchNextMessages() loads newer messages (Requirement 3.6)") {

        test("fetchNextMessages() should append newer messages to end of list") {
            runTest {
                val repository = MockMessageListRepository()

                // Initial messages
                val initialMessages = createChronologicalMessages(3, startId = 1, startSentAt = 1000)
                repository.fetchPreviousMessagesResult = Result.success(initialMessages)

                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)

                viewModel.fetchMessages()
                advanceUntilIdle()

                // Setup newer messages
                val newerMessages = createChronologicalMessages(3, startId = 10, startSentAt = 2000)
                repository.fetchNextMessagesResult = Result.success(newerMessages)

                viewModel.fetchNextMessages()
                advanceUntilIdle()

                // Should have 6 messages total
                viewModel.messages.value.size shouldBe 6

                // Newer messages should be at the end
                val lastThreeIds = viewModel.messages.value.takeLast(3).map { it.id }
                lastThreeIds shouldContainAll listOf(10L, 11L, 12L)
            }
        }

        test("fetchNextMessages() should call repository with last message ID") {
            runTest {
                val repository = MockMessageListRepository()

                val initialMessages = createChronologicalMessages(3, startId = 5, startSentAt = 1000)
                repository.fetchPreviousMessagesResult = Result.success(initialMessages)

                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)

                viewModel.fetchMessages()
                advanceUntilIdle()

                repository.fetchNextMessagesResult = Result.success(emptyList())

                viewModel.fetchNextMessages()
                advanceUntilIdle()

                // Should have called fetchNextMessages with the last message ID (7)
                repository.fetchNextCallCount shouldBe 1
                repository.lastFetchNextFromId shouldBe 7L
            }
        }

        /**
         * **Validates: Requirement 3.6**
         *
         * Property: fetchNextMessages() should always append newer messages at the end,
         * preserving the existing message order.
         */
        test("Property 2.5: fetchNextMessages() should always append at end") {
            checkAll(20, Arb.int(1, 10), Arb.int(1, 10)) { initialCount, newCount ->
                runTest {
                    val repository = MockMessageListRepository()

                    val initialMessages = createChronologicalMessages(initialCount, startId = 1, startSentAt = 1000)
                    repository.fetchPreviousMessagesResult = Result.success(initialMessages)

                    val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)

                    viewModel.fetchMessages()
                    advanceUntilIdle()

                    val newerMessages = createChronologicalMessages(newCount, startId = 100, startSentAt = 5000)
                    repository.fetchNextMessagesResult = Result.success(newerMessages)

                    viewModel.fetchNextMessages()
                    advanceUntilIdle()

                    // Total should be initial + new
                    viewModel.messages.value.size shouldBe initialCount + newCount

                    // Newer messages should be at the end
                    val lastNewIds = viewModel.messages.value.takeLast(newCount).map { it.id }
                    lastNewIds.first() shouldBe 100L
                }
            }
        }

        test("fetchNextMessages() should update hasMoreNewMessages based on result") {
            runTest {
                val repository = MockMessageListRepository()

                val initialMessages = createChronologicalMessages(3)
                repository.fetchPreviousMessagesResult = Result.success(initialMessages)

                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)

                viewModel.fetchMessages()
                advanceUntilIdle()

                // Non-empty result means more messages might be available
                val newerMessages = createChronologicalMessages(2, startId = 10, startSentAt = 2000)
                repository.fetchNextMessagesResult = Result.success(newerMessages)

                viewModel.fetchNextMessages()
                advanceUntilIdle()

                viewModel.hasMoreNewMessages.value shouldBe true

                // Empty result means no more messages
                repository.fetchNextMessagesResult = Result.success(emptyList())

                viewModel.fetchNextMessages()
                advanceUntilIdle()

                viewModel.hasMoreNewMessages.value shouldBe false
            }
        }
    }


    // ========================================================================
    // Property 2.6: Message updates (edit/delete) work correctly
    // Validates: Requirements 3.7, 3.8
    // ========================================================================

    context("Property 2.6: Message updates preserve order (Requirements 3.7, 3.8)") {

        test("updateItem() should update message in place without reordering") {
            runTest {
                val repository = MockMessageListRepository()

                val initialMessages = createChronologicalMessages(5, startId = 1, startSentAt = 1000)
                repository.fetchPreviousMessagesResult = Result.success(initialMessages)

                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)

                viewModel.fetchMessages()
                advanceUntilIdle()

                // Get original order
                val originalOrder = viewModel.messages.value.map { it.id }

                // Update message with ID 3 (middle of list)
                val updatedMessage = createMessage(id = 3, sentAt = 1200, readAt = 5000)
                viewModel.updateItem(updatedMessage) { it.id == 3L }

                // Order should be preserved
                val newOrder = viewModel.messages.value.map { it.id }
                newOrder shouldBe originalOrder

                // Message should be updated
                val updated = viewModel.messages.value.find { it.id == 3L }
                updated?.readAt shouldBe 5000L
            }
        }

        test("removeItem() should remove message without affecting other messages order") {
            runTest {
                val repository = MockMessageListRepository()

                val initialMessages = createChronologicalMessages(5, startId = 1, startSentAt = 1000)
                repository.fetchPreviousMessagesResult = Result.success(initialMessages)

                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)

                viewModel.fetchMessages()
                advanceUntilIdle()

                // Remove message with ID 3
                val messageToRemove = viewModel.messages.value.find { it.id == 3L }!!
                viewModel.removeItem(messageToRemove)

                // Should have 4 messages
                viewModel.messages.value.size shouldBe 4

                // Remaining messages should maintain order
                val remainingIds = viewModel.messages.value.map { it.id }
                remainingIds shouldBe listOf(1L, 2L, 4L, 5L)
            }
        }

        /**
         * **Validates: Requirement 3.7**
         *
         * Property: When message receipts are updated, the message state should
         * change but the list order should remain unchanged.
         */
        test("Property 2.6a: Receipt updates should not affect list order") {
            checkAll(20, Arb.int(3, 10), Arb.int(0, 9)) { messageCount, updateIndex ->
                runTest {
                    val repository = MockMessageListRepository()

                    val initialMessages = createChronologicalMessages(messageCount)
                    repository.fetchPreviousMessagesResult = Result.success(initialMessages)

                    val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)

                    viewModel.fetchMessages()
                    advanceUntilIdle()

                    val safeIndex = updateIndex.coerceIn(0, messageCount - 1)
                    val originalOrder = viewModel.messages.value.map { it.id }
                    val messageToUpdate = viewModel.messages.value[safeIndex]

                    // Update with new receipt timestamps
                    val updatedMessage = createMessage(
                        id = messageToUpdate.id,
                        sentAt = messageToUpdate.sentAt,
                        readAt = System.currentTimeMillis(),
                        deliveredAt = System.currentTimeMillis()
                    )
                    viewModel.updateItem(updatedMessage) { it.id == messageToUpdate.id }

                    // Order should be unchanged
                    val newOrder = viewModel.messages.value.map { it.id }
                    newOrder shouldBe originalOrder
                }
            }
        }

        /**
         * **Validates: Requirement 3.8**
         *
         * Property: When messages are edited or deleted, the specific message
         * should be updated in place without reordering the list.
         */
        test("Property 2.6b: Edit/delete should update in place without reordering") {
            checkAll(20, Arb.int(3, 10)) { messageCount ->
                runTest {
                    val repository = MockMessageListRepository()

                    val initialMessages = createChronologicalMessages(messageCount)
                    repository.fetchPreviousMessagesResult = Result.success(initialMessages)

                    val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)

                    viewModel.fetchMessages()
                    advanceUntilIdle()

                    val originalOrder = viewModel.messages.value.map { it.id }

                    // Update a random message (simulate edit)
                    val randomIndex = (0 until messageCount).random()
                    val messageToEdit = viewModel.messages.value[randomIndex]
                    val editedMessage = TextMessage(
                        "receiver_${messageToEdit.id}",
                        "Edited message content",
                        CometChatConstants.RECEIVER_TYPE_USER
                    ).apply {
                        id = messageToEdit.id
                        sentAt = messageToEdit.sentAt
                        editedAt = System.currentTimeMillis()
                    }

                    viewModel.updateItem(editedMessage) { it.id == messageToEdit.id }

                    // Order should be unchanged
                    val newOrder = viewModel.messages.value.map { it.id }
                    newOrder shouldBe originalOrder
                }
            }
        }

        test("batch operations should maintain message order") {
            runTest {
                val repository = MockMessageListRepository()

                val initialMessages = createChronologicalMessages(5, startId = 1, startSentAt = 1000)
                repository.fetchPreviousMessagesResult = Result.success(initialMessages)

                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)

                viewModel.fetchMessages()
                advanceUntilIdle()

                // Perform batch update
                viewModel.batch {
                    // Update message 2
                    update(createMessage(id = 2, sentAt = 1100, readAt = 5000)) { it.id == 2L }
                    // Update message 4
                    update(createMessage(id = 4, sentAt = 1300, readAt = 6000)) { it.id == 4L }
                }

                // Order should be preserved
                val messageIds = viewModel.messages.value.map { it.id }
                messageIds shouldBe listOf(1L, 2L, 3L, 4L, 5L)
            }
        }
    }


    // ========================================================================
    // Property 2.7: Manual scroll allowed (Requirement 3.5)
    // ========================================================================

    context("Property 2.7: Manual scroll state management (Requirement 3.5)") {

        test("clearScrollToMessage() should reset scrollToMessageId to null") {
            runTest {
                val repository = MockMessageListRepository()

                val targetMessage = createMessage(id = 50, sentAt = 5000)
                repository.getMessageResult = Result.success(targetMessage)

                val surroundingResult = SurroundingMessagesResult(
                    targetMessage = targetMessage,
                    olderMessages = emptyList(),
                    newerMessages = emptyList(),
                    hasMorePrevious = false,
                    hasMoreNext = false
                )
                repository.fetchSurroundingMessagesResult = Result.success(surroundingResult)

                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)

                viewModel.goToMessage(50)
                advanceUntilIdle()

                viewModel.scrollToMessageId.value shouldBe 50L

                // Clear scroll request (simulating UI has scrolled)
                viewModel.clearScrollToMessage()

                viewModel.scrollToMessageId.value shouldBe null
            }
        }

        test("clearHighlightScroll() should reset highlightScroll to false") {
            runTest {
                val repository = MockMessageListRepository()

                val targetMessage = createMessage(id = 25, sentAt = 2500)
                repository.getMessageResult = Result.success(targetMessage)

                val surroundingResult = SurroundingMessagesResult(
                    targetMessage = targetMessage,
                    olderMessages = emptyList(),
                    newerMessages = emptyList(),
                    hasMorePrevious = false,
                    hasMoreNext = false
                )
                repository.fetchSurroundingMessagesResult = Result.success(surroundingResult)

                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)

                viewModel.goToMessage(25, highlight = true)
                advanceUntilIdle()

                viewModel.highlightScroll.value shouldBe true

                // Clear highlight (simulating animation complete)
                viewModel.clearHighlightScroll()

                viewModel.highlightScroll.value shouldBe false
            }
        }

        /**
         * **Validates: Requirement 3.5**
         *
         * Property: The ViewModel should not force scroll position after
         * clearScrollToMessage() is called, allowing free user scrolling.
         */
        test("Property 2.7: After clearScrollToMessage(), no forced scroll should occur") {
            checkAll(10, Arb.long(1, 100)) { messageId ->
                runTest {
                    val repository = MockMessageListRepository()

                    val targetMessage = createMessage(id = messageId, sentAt = messageId * 100)
                    repository.getMessageResult = Result.success(targetMessage)

                    val surroundingResult = SurroundingMessagesResult(
                        targetMessage = targetMessage,
                        olderMessages = emptyList(),
                        newerMessages = emptyList(),
                        hasMorePrevious = false,
                        hasMoreNext = false
                    )
                    repository.fetchSurroundingMessagesResult = Result.success(surroundingResult)

                    val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)

                    viewModel.goToMessage(messageId)
                    advanceUntilIdle()

                    viewModel.scrollToMessageId.value shouldBe messageId

                    viewModel.clearScrollToMessage()

                    // After clearing, scrollToMessageId should be null
                    // This allows the UI to handle manual scrolling without interference
                    viewModel.scrollToMessageId.value shouldBe null
                }
            }
        }
    }


    // ========================================================================
    // Property 2.8: Duplicate filtering preserved
    // ========================================================================

    context("Property 2.8: Duplicate message filtering") {

        test("fetchMessages() should filter out duplicate messages by ID") {
            runTest {
                val repository = MockMessageListRepository()

                // First fetch
                val firstBatch = createChronologicalMessages(3, startId = 1, startSentAt = 1000)
                repository.fetchPreviousMessagesResult = Result.success(firstBatch)

                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)

                viewModel.fetchMessages()
                advanceUntilIdle()

                viewModel.messages.value.size shouldBe 3

                // Second fetch with some duplicates (IDs 2, 3 overlap)
                val secondBatch = listOf(
                    createMessage(id = 2, sentAt = 1100), // duplicate
                    createMessage(id = 3, sentAt = 1200), // duplicate
                    createMessage(id = 0, sentAt = 500)   // new older message
                )
                repository.fetchPreviousMessagesResult = Result.success(secondBatch)

                viewModel.fetchMessages()
                advanceUntilIdle()

                // Should only have 4 unique messages (1, 2, 3 from first + 0 from second)
                viewModel.messages.value.size shouldBe 4

                // Verify no duplicates
                val ids = viewModel.messages.value.map { it.id }
                ids.distinct().size shouldBe ids.size
            }
        }

        test("fetchNextMessages() should filter out duplicate messages by ID") {
            runTest {
                val repository = MockMessageListRepository()

                val initialMessages = createChronologicalMessages(3, startId = 1, startSentAt = 1000)
                repository.fetchPreviousMessagesResult = Result.success(initialMessages)

                val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)

                viewModel.fetchMessages()
                advanceUntilIdle()

                // Fetch next with some duplicates
                val nextBatch = listOf(
                    createMessage(id = 3, sentAt = 1200), // duplicate
                    createMessage(id = 10, sentAt = 2000), // new
                    createMessage(id = 11, sentAt = 2100)  // new
                )
                repository.fetchNextMessagesResult = Result.success(nextBatch)

                viewModel.fetchNextMessages()
                advanceUntilIdle()

                // Should have 5 unique messages
                viewModel.messages.value.size shouldBe 5

                val ids = viewModel.messages.value.map { it.id }
                ids.distinct().size shouldBe ids.size
            }
        }

        /**
         * Property: Duplicate filtering should work for any combination of
         * existing and new messages.
         */
        test("Property 2.8: Duplicate filtering should always produce unique message list") {
            checkAll(20, Arb.int(1, 10), Arb.int(1, 10)) { existingCount, newCount ->
                runTest {
                    val repository = MockMessageListRepository()

                    val existingMessages = createChronologicalMessages(existingCount, startId = 10, startSentAt = 1000)
                    repository.fetchPreviousMessagesResult = Result.success(existingMessages)

                    val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)

                    viewModel.fetchMessages()
                    advanceUntilIdle()

                    // Create new messages with some potential overlap
                    val newMessages = createChronologicalMessages(newCount, startId = 5, startSentAt = 500)
                    repository.fetchPreviousMessagesResult = Result.success(newMessages)

                    viewModel.fetchMessages()
                    advanceUntilIdle()

                    // All message IDs should be unique
                    val ids = viewModel.messages.value.map { it.id }
                    ids.distinct().size shouldBe ids.size
                }
            }
        }
    }
})
