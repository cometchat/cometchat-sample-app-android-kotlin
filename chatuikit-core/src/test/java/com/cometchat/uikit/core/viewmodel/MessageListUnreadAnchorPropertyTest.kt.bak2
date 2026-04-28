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
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
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
 * Property-based tests for unread anchor detection in [CometChatMessageListViewModel].
 * Each test validates a correctness property from the design document.
 *
 * Feature: message-list-fetching-parity
 * **Validates: Requirements 3.1-3.6**
 *
 * ## Test Coverage
 *
 * | Property | Description | Requirements |
 * |----------|-------------|--------------|
 * | Property 8 | Unread Anchor Detection | 3.1, 3.2, 3.3, 3.4, 3.5 |
 * | Property 9 | Unread Anchor Disabled for Threads | 3.6 |
 * 
 * ## Testing Approach
 * 
 * These tests use the goToMessage() flow to trigger unread anchor detection.
 * The goToMessage() method calls handleUnreadMessageState() internally after
 * setting up the message list, which then calls getFirstUnreadMessage().
 * 
 * Since CometChat.getLoggedInUser() returns null in unit tests (SDK not initialized),
 * the "skip messages from logged-in user" check becomes: sender?.uid != null
 * This means messages with non-null senders pass the check.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MessageListUnreadAnchorPropertyTest : FunSpec({

    val testDispatcher = StandardTestDispatcher()


    beforeSpec {
        Dispatchers.setMain(testDispatcher)
    }

    afterSpec {
        Dispatchers.resetMain()
    }

    /**
     * Mock repository for testing unread anchor detection functionality.
     * Tracks method calls and allows configuring return values.
     */
    class UnreadAnchorMockRepository : MessageListRepository {
        // Configurable return values
        var getMessageResult: Result<BaseMessage>? = null
        var fetchSurroundingMessagesResult: Result<SurroundingMessagesResult>? = null
        var fetchPreviousMessagesResult: Result<List<BaseMessage>> = Result.success(emptyList())
        var fetchNextMessagesResult: Result<List<BaseMessage>> = Result.success(emptyList())
        var getConversationResult: Result<Conversation>? = null
        var hasMorePrevious: Boolean = true
        
        // Track method calls
        var getMessageCalled = false
        var fetchSurroundingMessagesCalled = false
        var rebuildRequestCalled = false
        var rebuildRequestMessageId: Long = -1
        
        private var latestMessageId: Long = -1
        
        override suspend fun getMessage(messageId: Long): Result<BaseMessage> {
            getMessageCalled = true
            return getMessageResult ?: Result.failure(Exception("getMessage not configured"))
        }
        
        override suspend fun fetchSurroundingMessages(messageId: Long): Result<SurroundingMessagesResult> {
            fetchSurroundingMessagesCalled = true
            return fetchSurroundingMessagesResult ?: Result.failure(Exception("fetchSurroundingMessages not configured"))
        }
        
        override fun rebuildRequestFromMessageId(messageId: Long) {
            rebuildRequestCalled = true
            rebuildRequestMessageId = messageId
        }
        
        override suspend fun fetchPreviousMessages(): Result<List<BaseMessage>> = fetchPreviousMessagesResult
        override suspend fun fetchNextMessages(fromMessageId: Long): Result<List<BaseMessage>> = fetchNextMessagesResult
        override suspend fun getConversation(id: String, type: String): Result<Conversation> {
            return getConversationResult ?: Result.failure(Exception("getConversation not configured"))
        }
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
            fetchSurroundingMessagesCalled = false
            rebuildRequestCalled = false
            rebuildRequestMessageId = -1
        }
    }


    /**
     * Testable ViewModel subclass that exposes private fields for testing.
     * This allows us to set lastReadMessageId and parentMessageId directly
     * without going through the full fetchMessagesWithUnreadCount flow.
     */
    class TestableMessageListViewModel(
        repository: MessageListRepository
    ) : CometChatMessageListViewModel(repository, enableListeners = false) {
        
        /**
         * Override to return null for logged-in user UID in tests.
         * This avoids loading the CometChat SDK which is not initialized in tests.
         */
        override fun getLoggedInUserUid(): String? = null
        
        /**
         * Sets the lastReadMessageId for testing unread anchor detection.
         * Uses reflection to access the private field.
         */
        fun setLastReadMessageIdForTest(messageId: Long) {
            val field = CometChatMessageListViewModel::class.java.getDeclaredField("lastReadMessageId")
            field.isAccessible = true
            field.setLong(this, messageId)
        }
        
        /**
         * Sets the parentMessageId for testing thread behavior.
         * Uses reflection to access the private field.
         */
        fun setParentMessageIdForTest(messageId: Long) {
            val field = CometChatMessageListViewModel::class.java.getDeclaredField("parentMessageId")
            field.isAccessible = true
            field.setLong(this, messageId)
        }
        
        /**
         * Directly sets the messages list for testing.
         * Uses reflection to access the private _messages field.
         */
        fun setMessagesForTest(messages: List<BaseMessage>) {
            val field = CometChatMessageListViewModel::class.java.getDeclaredField("_messages")
            field.isAccessible = true
            @Suppress("UNCHECKED_CAST")
            val stateFlow = field.get(this) as kotlinx.coroutines.flow.MutableStateFlow<List<BaseMessage>>
            stateFlow.value = messages
        }
        
        /**
         * Directly sets the unreadMessageAnchor for testing.
         * Uses reflection to access the private _unreadMessageAnchor field.
         */
        fun setUnreadMessageAnchorForTest(message: BaseMessage?) {
            val field = CometChatMessageListViewModel::class.java.getDeclaredField("_unreadMessageAnchor")
            field.isAccessible = true
            @Suppress("UNCHECKED_CAST")
            val stateFlow = field.get(this) as kotlinx.coroutines.flow.MutableStateFlow<BaseMessage?>
            stateFlow.value = message
        }
    }

    // Use a sender ID that is different from the logged-in user
    // Since CometChat.getLoggedInUser() returns null in tests (SDK not initialized),
    // any non-null sender will be considered "other user"
    val OTHER_USER_UID = "other_user"


    /**
     * Creates mock messages for testing.
     * 
     * @param count Number of messages to create
     * @param startId Starting ID for the messages
     * @param senderId Sender UID for all messages
     * @param deletedAt Optional deletedAt timestamp (0 = not deleted)
     */
    fun createMockMessages(
        count: Int, 
        startId: Long = 1L, 
        senderId: String = OTHER_USER_UID,
        deletedAt: Long = 0L
    ): List<BaseMessage> {
        return (0 until count).map { index ->
            val id = startId + index
            TextMessage(
                "receiver_$id",
                "Test message $id",
                CometChatConstants.RECEIVER_TYPE_USER
            ).apply {
                this.id = id
                sentAt = System.currentTimeMillis() + (index * 1000)
                sender = User().apply { uid = senderId }
                this.deletedAt = deletedAt
            }
        }
    }

    /**
     * Creates a mixed list of messages with different senders and deletion states.
     * 
     * @param messageConfigs List of (id, senderId, deletedAt) tuples
     */
    fun createMixedMessages(messageConfigs: List<Triple<Long, String, Long>>): List<BaseMessage> {
        return messageConfigs.map { (id, senderId, deletedAt) ->
            TextMessage(
                "receiver_$id",
                "Test message $id",
                CometChatConstants.RECEIVER_TYPE_USER
            ).apply {
                this.id = id
                sentAt = System.currentTimeMillis() + (id * 1000)
                sender = User().apply { uid = senderId }
                this.deletedAt = deletedAt
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
        hasMoreNext: Boolean = true,
        senderId: String = OTHER_USER_UID
    ): SurroundingMessagesResult {
        val olderMessages = createMockMessages(olderCount, startId = targetId - olderCount, senderId = senderId)
        val targetMessage = createMockMessages(1, startId = targetId, senderId = senderId).first()
        val newerMessages = createMockMessages(newerCount, startId = targetId + 1, senderId = senderId)
        
        return SurroundingMessagesResult(
            olderMessages = olderMessages,
            targetMessage = targetMessage,
            newerMessages = newerMessages,
            hasMorePrevious = hasMorePrevious,
            hasMoreNext = hasMoreNext
        )
    }


    // ========================================
    // Property 8: Unread Anchor Detection
    // ========================================
    
    context("Property 8: Unread Anchor Detection") {
        
        /**
         * **Property 8: Unread Anchor Detection**
         * 
         * *For any* message list and `lastReadMessageId > 0`, the `unreadMessageAnchor` SHALL be
         * the first message where: `message.id > lastReadMessageId` AND 
         * `message.sender.uid != loggedInUser.uid` AND `message.deletedAt == 0`.
         * If no such message exists, `unreadMessageAnchor` SHALL be null.
         * 
         * **Validates: Requirements 3.1, 3.2, 3.3, 3.4, 3.5**
         * 
         * Note: These tests use goToMessage() to trigger unread anchor detection through
         * the public API. The goToMessage() method internally calls handleUnreadMessageState()
         * after setting up the message list.
         */
        test("Property 8: unreadMessageAnchor is the first message with id > lastReadMessageId") {
            checkAll(100, Arb.long(1L, 100L), Arb.int(1, 10)) { lastReadId, messageCount ->
                runTest {
                    val repository = UnreadAnchorMockRepository()
                    val viewModel = TestableMessageListViewModel(repository)
                    
                    // Create messages with IDs starting after lastReadId
                    val messages = createMockMessages(
                        count = messageCount,
                        startId = lastReadId + 1,
                        senderId = OTHER_USER_UID
                    )
                    
                    // Configure repository to return these messages via goToMessage
                    val targetId = lastReadId + 1
                    val targetMessage = messages.first()
                    repository.getMessageResult = Result.success(targetMessage)
                    repository.fetchSurroundingMessagesResult = Result.success(
                        SurroundingMessagesResult(
                            olderMessages = emptyList(),
                            targetMessage = targetMessage,
                            newerMessages = messages.drop(1),
                            hasMorePrevious = false,
                            hasMoreNext = false
                        )
                    )
                    
                    // Set lastReadMessageId before calling goToMessage
                    viewModel.setLastReadMessageIdForTest(lastReadId)
                    viewModel.setParentMessageIdForTest(-1L) // Not a thread
                    
                    // Trigger goToMessage which will call handleUnreadMessageState
                    viewModel.goToMessage(targetId)
                    advanceUntilIdle()
                    
                    // The first unread message should be the first message in the list
                    viewModel.unreadMessageAnchor.value shouldNotBe null
                    viewModel.unreadMessageAnchor.value?.id shouldBe (lastReadId + 1)
                }
            }
        }

        
        test("Property 8: unreadMessageAnchor skips deleted messages (deletedAt > 0)") {
            checkAll(100, Arb.long(1L, 100L)) { lastReadId ->
                runTest {
                    val repository = UnreadAnchorMockRepository()
                    val viewModel = TestableMessageListViewModel(repository)
                    
                    // Create messages: first deleted, second not deleted
                    val deletedMessage = TextMessage(
                        "receiver",
                        "Deleted message",
                        CometChatConstants.RECEIVER_TYPE_USER
                    ).apply {
                        this.id = lastReadId + 1
                        sentAt = System.currentTimeMillis()
                        sender = User().apply { uid = OTHER_USER_UID }
                        this.deletedAt = System.currentTimeMillis() // Deleted
                    }
                    
                    val validMessage = TextMessage(
                        "receiver",
                        "Valid message",
                        CometChatConstants.RECEIVER_TYPE_USER
                    ).apply {
                        this.id = lastReadId + 2
                        sentAt = System.currentTimeMillis() + 1000
                        sender = User().apply { uid = OTHER_USER_UID }
                        this.deletedAt = 0L // Not deleted
                    }
                    
                    // Configure repository
                    repository.getMessageResult = Result.success(deletedMessage)
                    repository.fetchSurroundingMessagesResult = Result.success(
                        SurroundingMessagesResult(
                            olderMessages = emptyList(),
                            targetMessage = deletedMessage,
                            newerMessages = listOf(validMessage),
                            hasMorePrevious = false,
                            hasMoreNext = false
                        )
                    )
                    
                    viewModel.setLastReadMessageIdForTest(lastReadId)
                    viewModel.setParentMessageIdForTest(-1L)
                    
                    viewModel.goToMessage(lastReadId + 1)
                    advanceUntilIdle()
                    
                    // Should skip the first message (deleted) and return the second
                    viewModel.unreadMessageAnchor.value shouldNotBe null
                    viewModel.unreadMessageAnchor.value?.id shouldBe (lastReadId + 2)
                }
            }
        }

        
        test("Property 8: unreadMessageAnchor is null when all messages are deleted") {
            checkAll(100, Arb.long(1L, 100L), Arb.int(1, 5)) { lastReadId, messageCount ->
                runTest {
                    val repository = UnreadAnchorMockRepository()
                    val viewModel = TestableMessageListViewModel(repository)
                    
                    // Create messages all deleted
                    val messages = createMockMessages(
                        count = messageCount,
                        startId = lastReadId + 1,
                        senderId = OTHER_USER_UID,
                        deletedAt = System.currentTimeMillis()
                    )
                    
                    val targetMessage = messages.first()
                    repository.getMessageResult = Result.success(targetMessage)
                    repository.fetchSurroundingMessagesResult = Result.success(
                        SurroundingMessagesResult(
                            olderMessages = emptyList(),
                            targetMessage = targetMessage,
                            newerMessages = messages.drop(1),
                            hasMorePrevious = false,
                            hasMoreNext = false
                        )
                    )
                    
                    viewModel.setLastReadMessageIdForTest(lastReadId)
                    viewModel.setParentMessageIdForTest(-1L)
                    
                    viewModel.goToMessage(lastReadId + 1)
                    advanceUntilIdle()
                    
                    // No unread anchor should be set (all messages are deleted)
                    viewModel.unreadMessageAnchor.value shouldBe null
                }
            }
        }
        
        test("Property 8: unreadMessageAnchor is null when all messages are before lastReadMessageId") {
            checkAll(100, Arb.long(100L, 200L), Arb.int(1, 5)) { lastReadId, messageCount ->
                runTest {
                    val repository = UnreadAnchorMockRepository()
                    val viewModel = TestableMessageListViewModel(repository)
                    
                    // Create messages with IDs before lastReadId
                    val messages = createMockMessages(
                        count = messageCount,
                        startId = lastReadId - messageCount,
                        senderId = OTHER_USER_UID
                    )
                    
                    val targetMessage = messages.first()
                    repository.getMessageResult = Result.success(targetMessage)
                    repository.fetchSurroundingMessagesResult = Result.success(
                        SurroundingMessagesResult(
                            olderMessages = emptyList(),
                            targetMessage = targetMessage,
                            newerMessages = messages.drop(1),
                            hasMorePrevious = false,
                            hasMoreNext = false
                        )
                    )
                    
                    viewModel.setLastReadMessageIdForTest(lastReadId)
                    viewModel.setParentMessageIdForTest(-1L)
                    
                    viewModel.goToMessage(lastReadId - messageCount)
                    advanceUntilIdle()
                    
                    // No unread anchor should be set (all messages are already read)
                    viewModel.unreadMessageAnchor.value shouldBe null
                }
            }
        }

        
        test("Property 8: unreadMessageAnchor is null when lastReadMessageId is 0 or negative") {
            checkAll(100, Arb.long(-100L, 0L), Arb.int(1, 5)) { lastReadId, messageCount ->
                runTest {
                    val repository = UnreadAnchorMockRepository()
                    val viewModel = TestableMessageListViewModel(repository)
                    
                    // Create messages
                    val messages = createMockMessages(
                        count = messageCount,
                        startId = 1L,
                        senderId = OTHER_USER_UID
                    )
                    
                    val targetMessage = messages.first()
                    repository.getMessageResult = Result.success(targetMessage)
                    repository.fetchSurroundingMessagesResult = Result.success(
                        SurroundingMessagesResult(
                            olderMessages = emptyList(),
                            targetMessage = targetMessage,
                            newerMessages = messages.drop(1),
                            hasMorePrevious = false,
                            hasMoreNext = false
                        )
                    )
                    
                    viewModel.setLastReadMessageIdForTest(lastReadId)
                    viewModel.setParentMessageIdForTest(-1L)
                    
                    viewModel.goToMessage(1L)
                    advanceUntilIdle()
                    
                    // No unread anchor should be set when lastReadMessageId <= 0
                    viewModel.unreadMessageAnchor.value shouldBe null
                }
            }
        }
        
        test("Property 8: unreadMessageAnchor is null when message list is empty") {
            checkAll(100, Arb.long(1L, 100L)) { lastReadId ->
                runTest {
                    val repository = UnreadAnchorMockRepository()
                    val viewModel = TestableMessageListViewModel(repository)
                    
                    // Create a single target message (minimum for goToMessage)
                    val targetMessage = createMockMessages(1, startId = lastReadId - 1).first()
                    
                    repository.getMessageResult = Result.success(targetMessage)
                    repository.fetchSurroundingMessagesResult = Result.success(
                        SurroundingMessagesResult(
                            olderMessages = emptyList(),
                            targetMessage = targetMessage,
                            newerMessages = emptyList(),
                            hasMorePrevious = false,
                            hasMoreNext = false
                        )
                    )
                    
                    viewModel.setLastReadMessageIdForTest(lastReadId)
                    viewModel.setParentMessageIdForTest(-1L)
                    
                    viewModel.goToMessage(lastReadId - 1)
                    advanceUntilIdle()
                    
                    // The only message is before lastReadId, so no unread anchor
                    viewModel.unreadMessageAnchor.value shouldBe null
                }
            }
        }

        
        test("Property 8: unreadMessageAnchor finds first non-deleted message after lastReadMessageId") {
            checkAll(100, Arb.long(50L, 100L)) { lastReadId ->
                runTest {
                    val repository = UnreadAnchorMockRepository()
                    val viewModel = TestableMessageListViewModel(repository)
                    
                    // Create a complex mixed list:
                    // - Some messages before lastReadId (already read)
                    // - Some deleted messages after lastReadId (should skip)
                    // - First valid unread message
                    val readMessage1 = TextMessage("r", "m", CometChatConstants.RECEIVER_TYPE_USER).apply {
                        id = lastReadId - 2
                        sender = User().apply { uid = OTHER_USER_UID }
                        deletedAt = 0L
                    }
                    val readMessage2 = TextMessage("r", "m", CometChatConstants.RECEIVER_TYPE_USER).apply {
                        id = lastReadId - 1
                        sender = User().apply { uid = OTHER_USER_UID }
                        deletedAt = 0L
                    }
                    val atLastRead = TextMessage("r", "m", CometChatConstants.RECEIVER_TYPE_USER).apply {
                        id = lastReadId
                        sender = User().apply { uid = OTHER_USER_UID }
                        deletedAt = 0L
                    }
                    val deletedAfter1 = TextMessage("r", "m", CometChatConstants.RECEIVER_TYPE_USER).apply {
                        id = lastReadId + 1
                        sender = User().apply { uid = OTHER_USER_UID }
                        deletedAt = System.currentTimeMillis() // Deleted
                    }
                    val deletedAfter2 = TextMessage("r", "m", CometChatConstants.RECEIVER_TYPE_USER).apply {
                        id = lastReadId + 2
                        sender = User().apply { uid = OTHER_USER_UID }
                        deletedAt = System.currentTimeMillis() // Deleted
                    }
                    val firstValidUnread = TextMessage("r", "m", CometChatConstants.RECEIVER_TYPE_USER).apply {
                        id = lastReadId + 3
                        sender = User().apply { uid = OTHER_USER_UID }
                        deletedAt = 0L // Not deleted - first valid unread!
                    }
                    val anotherUnread = TextMessage("r", "m", CometChatConstants.RECEIVER_TYPE_USER).apply {
                        id = lastReadId + 4
                        sender = User().apply { uid = OTHER_USER_UID }
                        deletedAt = 0L
                    }
                    
                    repository.getMessageResult = Result.success(readMessage1)
                    repository.fetchSurroundingMessagesResult = Result.success(
                        SurroundingMessagesResult(
                            olderMessages = emptyList(),
                            targetMessage = readMessage1,
                            newerMessages = listOf(readMessage2, atLastRead, deletedAfter1, deletedAfter2, firstValidUnread, anotherUnread),
                            hasMorePrevious = false,
                            hasMoreNext = false
                        )
                    )
                    
                    viewModel.setLastReadMessageIdForTest(lastReadId)
                    viewModel.setParentMessageIdForTest(-1L)
                    
                    viewModel.goToMessage(lastReadId - 2)
                    advanceUntilIdle()
                    
                    // Should find the first valid unread message (id = lastReadId + 3)
                    viewModel.unreadMessageAnchor.value shouldNotBe null
                    viewModel.unreadMessageAnchor.value?.id shouldBe (lastReadId + 3)
                }
            }
        }

        
        test("Property 8: unreadMessageAnchor correctly handles messages at boundary") {
            checkAll(100, Arb.long(1L, 100L)) { lastReadId ->
                runTest {
                    val repository = UnreadAnchorMockRepository()
                    val viewModel = TestableMessageListViewModel(repository)
                    
                    // Message with id == lastReadId should NOT be considered unread
                    // Only messages with id > lastReadId are unread
                    val atBoundary = TextMessage("r", "m", CometChatConstants.RECEIVER_TYPE_USER).apply {
                        id = lastReadId
                        sender = User().apply { uid = OTHER_USER_UID }
                        deletedAt = 0L
                    }
                    val firstUnread = TextMessage("r", "m", CometChatConstants.RECEIVER_TYPE_USER).apply {
                        id = lastReadId + 1
                        sender = User().apply { uid = OTHER_USER_UID }
                        deletedAt = 0L
                    }
                    
                    repository.getMessageResult = Result.success(atBoundary)
                    repository.fetchSurroundingMessagesResult = Result.success(
                        SurroundingMessagesResult(
                            olderMessages = emptyList(),
                            targetMessage = atBoundary,
                            newerMessages = listOf(firstUnread),
                            hasMorePrevious = false,
                            hasMoreNext = false
                        )
                    )
                    
                    viewModel.setLastReadMessageIdForTest(lastReadId)
                    viewModel.setParentMessageIdForTest(-1L)
                    
                    viewModel.goToMessage(lastReadId)
                    advanceUntilIdle()
                    
                    // Should find the message with id > lastReadId
                    viewModel.unreadMessageAnchor.value shouldNotBe null
                    viewModel.unreadMessageAnchor.value?.id shouldBe (lastReadId + 1)
                }
            }
        }
        
        test("Property 8: unreadMessageAnchor skips messages with null sender") {
            checkAll(100, Arb.long(1L, 100L)) { lastReadId ->
                runTest {
                    val repository = UnreadAnchorMockRepository()
                    val viewModel = TestableMessageListViewModel(repository)
                    
                    // Create messages where first has null sender
                    val messageWithNullSender = TextMessage("r", "m", CometChatConstants.RECEIVER_TYPE_USER).apply {
                        id = lastReadId + 1
                        sender = null // Null sender
                        deletedAt = 0L
                    }
                    val messageWithSender = TextMessage("r", "m", CometChatConstants.RECEIVER_TYPE_USER).apply {
                        id = lastReadId + 2
                        sender = User().apply { uid = OTHER_USER_UID }
                        deletedAt = 0L
                    }
                    
                    repository.getMessageResult = Result.success(messageWithNullSender)
                    repository.fetchSurroundingMessagesResult = Result.success(
                        SurroundingMessagesResult(
                            olderMessages = emptyList(),
                            targetMessage = messageWithNullSender,
                            newerMessages = listOf(messageWithSender),
                            hasMorePrevious = false,
                            hasMoreNext = false
                        )
                    )
                    
                    viewModel.setLastReadMessageIdForTest(lastReadId)
                    viewModel.setParentMessageIdForTest(-1L)
                    
                    viewModel.goToMessage(lastReadId + 1)
                    advanceUntilIdle()
                    
                    // When sender is null, sender?.uid is null, which equals loggedInUser?.uid (also null)
                    // So the message is skipped. The second message should be the anchor.
                    viewModel.unreadMessageAnchor.value shouldNotBe null
                    viewModel.unreadMessageAnchor.value?.id shouldBe (lastReadId + 2)
                }
            }
        }
    }


    // ========================================
    // Property 9: Unread Anchor Disabled for Threads
    // ========================================
    
    context("Property 9: Unread Anchor Disabled for Threads") {
        
        /**
         * **Property 9: Unread Anchor Disabled for Threads**
         * 
         * *For any* ViewModel configured with `parentMessageId > 0`, `unreadMessageAnchor`
         * SHALL always be null regardless of `lastReadMessageId` or message list contents.
         * 
         * **Validates: Requirements 3.6**
         */
        test("Property 9: unreadMessageAnchor is null when parentMessageId > 0") {
            checkAll(100, Arb.long(1L, 100L), Arb.long(1L, 1000L), Arb.int(1, 5)) { lastReadId, parentMessageId, messageCount ->
                runTest {
                    val repository = UnreadAnchorMockRepository()
                    val viewModel = TestableMessageListViewModel(repository)
                    
                    // Create messages that would normally trigger an unread anchor
                    val messages = createMockMessages(
                        count = messageCount,
                        startId = lastReadId + 1,
                        senderId = OTHER_USER_UID
                    )
                    
                    val targetMessage = messages.first()
                    repository.getMessageResult = Result.success(targetMessage)
                    repository.fetchSurroundingMessagesResult = Result.success(
                        SurroundingMessagesResult(
                            olderMessages = emptyList(),
                            targetMessage = targetMessage,
                            newerMessages = messages.drop(1),
                            hasMorePrevious = false,
                            hasMoreNext = false
                        )
                    )
                    
                    viewModel.setLastReadMessageIdForTest(lastReadId)
                    viewModel.setParentMessageIdForTest(parentMessageId) // Thread mode
                    
                    viewModel.goToMessage(lastReadId + 1)
                    advanceUntilIdle()
                    
                    // Unread anchor should always be null in thread mode
                    viewModel.unreadMessageAnchor.value shouldBe null
                }
            }
        }

        
        test("Property 9: unreadMessageAnchor is null for threads even with valid unread messages") {
            checkAll(100, Arb.long(1L, 100L), Arb.long(1L, 1000L)) { lastReadId, parentMessageId ->
                runTest {
                    val repository = UnreadAnchorMockRepository()
                    val viewModel = TestableMessageListViewModel(repository)
                    
                    // Create a list with clear unread messages
                    val readMessage = TextMessage("r", "m", CometChatConstants.RECEIVER_TYPE_USER).apply {
                        id = lastReadId - 1
                        sender = User().apply { uid = OTHER_USER_UID }
                        deletedAt = 0L
                    }
                    val atLastRead = TextMessage("r", "m", CometChatConstants.RECEIVER_TYPE_USER).apply {
                        id = lastReadId
                        sender = User().apply { uid = OTHER_USER_UID }
                        deletedAt = 0L
                    }
                    val unread1 = TextMessage("r", "m", CometChatConstants.RECEIVER_TYPE_USER).apply {
                        id = lastReadId + 1
                        sender = User().apply { uid = OTHER_USER_UID }
                        deletedAt = 0L
                    }
                    val unread2 = TextMessage("r", "m", CometChatConstants.RECEIVER_TYPE_USER).apply {
                        id = lastReadId + 2
                        sender = User().apply { uid = OTHER_USER_UID }
                        deletedAt = 0L
                    }
                    
                    repository.getMessageResult = Result.success(readMessage)
                    repository.fetchSurroundingMessagesResult = Result.success(
                        SurroundingMessagesResult(
                            olderMessages = emptyList(),
                            targetMessage = readMessage,
                            newerMessages = listOf(atLastRead, unread1, unread2),
                            hasMorePrevious = false,
                            hasMoreNext = false
                        )
                    )
                    
                    viewModel.setLastReadMessageIdForTest(lastReadId)
                    viewModel.setParentMessageIdForTest(parentMessageId) // Thread mode
                    
                    viewModel.goToMessage(lastReadId - 1)
                    advanceUntilIdle()
                    
                    // Even with valid unread messages, anchor should be null in thread mode
                    viewModel.unreadMessageAnchor.value shouldBe null
                }
            }
        }

        
        test("Property 9: unreadMessageAnchor works normally when parentMessageId is -1") {
            checkAll(100, Arb.long(1L, 100L), Arb.int(1, 5)) { lastReadId, messageCount ->
                runTest {
                    val repository = UnreadAnchorMockRepository()
                    val viewModel = TestableMessageListViewModel(repository)
                    
                    // Create messages that would trigger an unread anchor
                    val messages = createMockMessages(
                        count = messageCount,
                        startId = lastReadId + 1,
                        senderId = OTHER_USER_UID
                    )
                    
                    val targetMessage = messages.first()
                    repository.getMessageResult = Result.success(targetMessage)
                    repository.fetchSurroundingMessagesResult = Result.success(
                        SurroundingMessagesResult(
                            olderMessages = emptyList(),
                            targetMessage = targetMessage,
                            newerMessages = messages.drop(1),
                            hasMorePrevious = false,
                            hasMoreNext = false
                        )
                    )
                    
                    viewModel.setLastReadMessageIdForTest(lastReadId)
                    viewModel.setParentMessageIdForTest(-1L) // Main conversation (not a thread)
                    
                    viewModel.goToMessage(lastReadId + 1)
                    advanceUntilIdle()
                    
                    // Unread anchor should be set in main conversation mode
                    viewModel.unreadMessageAnchor.value shouldNotBe null
                    viewModel.unreadMessageAnchor.value?.id shouldBe (lastReadId + 1)
                }
            }
        }
        
        test("Property 9: parentMessageId == 0 is treated as thread mode (anchor is null)") {
            checkAll(100, Arb.long(1L, 100L), Arb.int(1, 5)) { lastReadId, messageCount ->
                runTest {
                    val repository = UnreadAnchorMockRepository()
                    val viewModel = TestableMessageListViewModel(repository)
                    
                    // Create messages that would trigger an unread anchor
                    val messages = createMockMessages(
                        count = messageCount,
                        startId = lastReadId + 1,
                        senderId = OTHER_USER_UID
                    )
                    
                    val targetMessage = messages.first()
                    repository.getMessageResult = Result.success(targetMessage)
                    repository.fetchSurroundingMessagesResult = Result.success(
                        SurroundingMessagesResult(
                            olderMessages = emptyList(),
                            targetMessage = targetMessage,
                            newerMessages = messages.drop(1),
                            hasMorePrevious = false,
                            hasMoreNext = false
                        )
                    )
                    
                    viewModel.setLastReadMessageIdForTest(lastReadId)
                    viewModel.setParentMessageIdForTest(0L) // 0 is NOT -1, so it's treated as thread mode
                    
                    viewModel.goToMessage(lastReadId + 1)
                    advanceUntilIdle()
                    
                    // parentMessageId == 0 is not -1, so unread anchor should be null
                    // The implementation checks: parentMessageId == -1L
                    viewModel.unreadMessageAnchor.value shouldBe null
                }
            }
        }

        
        test("Property 9: thread mode with empty message list still returns null") {
            checkAll(100, Arb.long(1L, 100L), Arb.long(1L, 1000L)) { lastReadId, parentMessageId ->
                runTest {
                    val repository = UnreadAnchorMockRepository()
                    val viewModel = TestableMessageListViewModel(repository)
                    
                    // Create a single target message
                    val targetMessage = createMockMessages(1, startId = lastReadId + 1).first()
                    
                    repository.getMessageResult = Result.success(targetMessage)
                    repository.fetchSurroundingMessagesResult = Result.success(
                        SurroundingMessagesResult(
                            olderMessages = emptyList(),
                            targetMessage = targetMessage,
                            newerMessages = emptyList(),
                            hasMorePrevious = false,
                            hasMoreNext = false
                        )
                    )
                    
                    viewModel.setLastReadMessageIdForTest(lastReadId)
                    viewModel.setParentMessageIdForTest(parentMessageId) // Thread mode
                    
                    viewModel.goToMessage(lastReadId + 1)
                    advanceUntilIdle()
                    
                    viewModel.unreadMessageAnchor.value shouldBe null
                }
            }
        }
        
        test("Property 9: thread mode with lastReadMessageId <= 0 still returns null") {
            checkAll(100, Arb.long(-100L, 0L), Arb.long(1L, 1000L), Arb.int(1, 5)) { lastReadId, parentMessageId, messageCount ->
                runTest {
                    val repository = UnreadAnchorMockRepository()
                    val viewModel = TestableMessageListViewModel(repository)
                    
                    // Create messages that would trigger an unread anchor in main conversation
                    val messages = createMockMessages(
                        count = messageCount,
                        startId = 1L,
                        senderId = OTHER_USER_UID
                    )
                    
                    val targetMessage = messages.first()
                    repository.getMessageResult = Result.success(targetMessage)
                    repository.fetchSurroundingMessagesResult = Result.success(
                        SurroundingMessagesResult(
                            olderMessages = emptyList(),
                            targetMessage = targetMessage,
                            newerMessages = messages.drop(1),
                            hasMorePrevious = false,
                            hasMoreNext = false
                        )
                    )
                    
                    viewModel.setLastReadMessageIdForTest(lastReadId) // <= 0
                    viewModel.setParentMessageIdForTest(parentMessageId) // Thread mode
                    
                    viewModel.goToMessage(1L)
                    advanceUntilIdle()
                    
                    // Both conditions fail: thread mode AND lastReadMessageId <= 0
                    viewModel.unreadMessageAnchor.value shouldBe null
                }
            }
        }
    }
})
