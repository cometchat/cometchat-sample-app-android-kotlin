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
import io.kotest.property.Arb
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

/**
 * Property-based tests for message receipt functionality in [CometChatMessageListViewModel].
 * 
 * Feature: message-receipts-parity
 * **Validates: Requirements AC-1.2, AC-1.3, AC-2.1, AC-2.3, AC-2.4, AC-4.1, AC-4.2, AC-5.1, AC-5.2**
 * 
 * ## Test Coverage
 * 
 * | Property | Description | Requirements |
 * |----------|-------------|--------------|
 * | Property 1 | Receipt Control Property | AC-1.2, AC-2.3, AC-5.1, AC-5.2 |
 * | Property 2 | Self-Message Property | AC-1.3, AC-2.4 |
 * | Property 3 | Idempotency Property | AC-2.1 |
 * | Property 4 | Thread Context Property | AC-4.1, AC-4.2 |
 * 
 * ## Testing Approach
 * 
 * These tests use a testable ViewModel subclass that:
 * - Overrides getLoggedInUserUid() to return a configurable test user
 * - Exposes internal fields via reflection for testing
 * - Uses a mock repository that tracks receipt method calls
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MessageReceiptsPropertyTest : FunSpec({

    val testDispatcher = StandardTestDispatcher()

    beforeSpec {
        Dispatchers.setMain(testDispatcher)
    }

    afterSpec {
        Dispatchers.resetMain()
    }

    // Constants for test users
    val LOGGED_IN_USER_UID = "logged_in_user"
    val OTHER_USER_UID = "other_user"

    /**
     * Mock repository for testing receipt functionality.
     * Tracks calls to markAsRead and markAsDelivered.
     */
    class ReceiptMockRepository : MessageListRepository {
        var markAsReadCalled: Boolean = false
        var markAsReadMessage: BaseMessage? = null
        var markAsReadResult: Result<Unit> = Result.success(Unit)
        
        var markAsDeliveredCalled: Boolean = false
        var markAsDeliveredMessage: BaseMessage? = null
        var markAsDeliveredResult: Result<Unit> = Result.success(Unit)
        
        var hasMorePrevious: Boolean = true
        var fetchPreviousMessagesResult: Result<List<BaseMessage>> = Result.success(emptyList())
        
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
        
        override suspend fun fetchPreviousMessages(): Result<List<BaseMessage>> = fetchPreviousMessagesResult
        override suspend fun fetchNextMessages(fromMessageId: Long): Result<List<BaseMessage>> = 
            Result.success(emptyList())
        override suspend fun getMessage(messageId: Long): Result<BaseMessage> = 
            Result.failure(Exception("Not configured"))
        override suspend fun fetchSurroundingMessages(messageId: Long): Result<SurroundingMessagesResult> = 
            Result.failure(Exception("Not configured"))
        override fun rebuildRequestFromMessageId(messageId: Long) {}
        override suspend fun getConversation(id: String, type: String): Result<Conversation> = 
            Result.failure(Exception("Not configured"))
        override suspend fun deleteMessage(message: BaseMessage): Result<BaseMessage> = 
            Result.failure(Exception("Not configured"))
        override suspend fun flagMessage(messageId: Long, reason: String, remark: String): Result<Unit> = 
            Result.success(Unit)
        override suspend fun addReaction(messageId: Long, emoji: String): Result<BaseMessage> = 
            Result.failure(Exception("Not configured"))
        override suspend fun removeReaction(messageId: Long, emoji: String): Result<BaseMessage> = 
            Result.failure(Exception("Not configured"))
        override suspend fun markAsUnread(message: BaseMessage): Result<Conversation> = 
            Result.success(Conversation().apply { unreadMessageCount = 1 })
        override fun hasMorePreviousMessages(): Boolean = hasMorePrevious
        override fun resetRequest() { hasMorePrevious = true }
        override fun configureForUser(
            user: User, 
            messagesTypes: List<String>, 
            messagesCategories: List<String>, 
            parentMessageId: Long, 
            messagesRequestBuilder: MessagesRequest.MessagesRequestBuilder?
        ) {}
        override fun configureForGroup(
            group: Group, 
            messagesTypes: List<String>, 
            messagesCategories: List<String>, 
            parentMessageId: Long, 
            messagesRequestBuilder: MessagesRequest.MessagesRequestBuilder?
        ) {}
        override suspend fun fetchActionMessages(fromMessageId: Long): Result<List<BaseMessage>> = 
            Result.success(emptyList())
        override fun getLatestMessageId(): Long = -1
        override fun setLatestMessageId(messageId: Long) {}
        
        fun reset() {
            markAsReadCalled = false
            markAsReadMessage = null
            markAsDeliveredCalled = false
            markAsDeliveredMessage = null
        }
    }

    /**
     * Testable ViewModel subclass that exposes internal methods and fields for testing.
     */
    class TestableReceiptViewModel(
        repository: MessageListRepository,
        private val loggedInUserUid: String = LOGGED_IN_USER_UID
    ) : CometChatMessageListViewModel(repository, enableListeners = false) {
        
        /**
         * Override to return configurable logged-in user UID for tests.
         */
        override fun getLoggedInUserUid(): String = loggedInUserUid
        
        /**
         * Sets the disableReceipt field via reflection for testing.
         */
        fun setDisableReceiptForTest(disable: Boolean) {
            val field = CometChatMessageListViewModel::class.java.getDeclaredField("disableReceipt")
            field.isAccessible = true
            field.setBoolean(this, disable)
        }
        
        /**
         * Gets the disableReceipt field value for verification.
         */
        fun getDisableReceiptForTest(): Boolean {
            val field = CometChatMessageListViewModel::class.java.getDeclaredField("disableReceipt")
            field.isAccessible = true
            return field.getBoolean(this)
        }
        
        /**
         * Sets the parentMessageId field via reflection for testing.
         */
        fun setParentMessageIdForTest(parentMessageId: Long) {
            val field = CometChatMessageListViewModel::class.java.getDeclaredField("parentMessageId")
            field.isAccessible = true
            field.setLong(this, parentMessageId)
        }
        
        /**
         * Gets the parentMessageId field value for verification.
         */
        fun getParentMessageIdForTest(): Long {
            val field = CometChatMessageListViewModel::class.java.getDeclaredField("parentMessageId")
            field.isAccessible = true
            return field.getLong(this)
        }
        
        /**
         * Sets the user field via reflection for testing.
         */
        fun setUserForTest(user: User) {
            val field = CometChatMessageListViewModel::class.java.getDeclaredField("user")
            field.isAccessible = true
            field.set(this, user)
        }
        
        /**
         * Directly sets the messages list for testing.
         */
        fun setMessagesForTest(messages: List<BaseMessage>) {
            val field = CometChatMessageListViewModel::class.java.getDeclaredField("_messages")
            field.isAccessible = true
            @Suppress("UNCHECKED_CAST")
            val stateFlow = field.get(this) as MutableStateFlow<List<BaseMessage>>
            stateFlow.value = messages
        }
    }

    /**
     * Creates a test user for configuring the ViewModel.
     */
    fun createTestUser(uid: String = OTHER_USER_UID): User {
        return User().apply { this.uid = uid }
    }

    /**
     * Creates a mock message for testing.
     * 
     * @param messageId The ID for the message
     * @param senderId Sender UID
     * @param readAt The readAt timestamp (0 = unread)
     * @param parentMessageId The parent message ID (0 = main conversation, >0 = thread reply)
     */
    fun createMockMessage(
        messageId: Long,
        senderId: String,
        readAt: Long = 0L,
        parentMessageId: Long = 0L
    ): BaseMessage {
        return TextMessage(
            LOGGED_IN_USER_UID,
            "Test message $messageId",
            CometChatConstants.RECEIVER_TYPE_USER
        ).apply {
            this.id = messageId
            sentAt = System.currentTimeMillis()
            sender = User().apply { uid = senderId }
            receiverUid = LOGGED_IN_USER_UID
            this.readAt = readAt
            this.parentMessageId = parentMessageId
        }
    }


    // ========================================
    // Property 1: Receipt Control Property
    // ========================================
    
    context("Property 1: Receipt Control Property - receipts only sent when disableReceipt is false") {
        
        /**
         * **Property 1: Receipt Control Property**
         * 
         * *For any* message and disableReceipt value:
         * - IF disableReceipt is true, THEN no receipts SHALL be sent
         * - IF disableReceipt is false, THEN receipts MAY be sent (subject to other conditions)
         * 
         * **Validates: Requirements AC-1.2, AC-2.3, AC-5.1, AC-5.2**
         */
        
        test("Property 1: markAsDelivered should NOT call repository when disableReceipt is true") {
            checkAll(100, Arb.long(1L, 10000L)) { messageId ->
                runTest {
                    val repository = ReceiptMockRepository()
                    val viewModel = TestableReceiptViewModel(repository)
                    
                    // Set disableReceipt to true
                    viewModel.setDisableReceiptForTest(true)
                    
                    // Create message from another user
                    val message = createMockMessage(messageId, OTHER_USER_UID)
                    
                    // Call markAsDelivered
                    viewModel.markAsDelivered(message)
                    advanceUntilIdle()
                    
                    // Repository should NOT be called
                    repository.markAsDeliveredCalled shouldBe false
                }
            }
        }
        
        test("Property 1: markAsDelivered should call repository when disableReceipt is false") {
            checkAll(100, Arb.long(1L, 10000L)) { messageId ->
                runTest {
                    val repository = ReceiptMockRepository()
                    val viewModel = TestableReceiptViewModel(repository)
                    
                    // Set disableReceipt to false (default)
                    viewModel.setDisableReceiptForTest(false)
                    
                    // Create message from another user
                    val message = createMockMessage(messageId, OTHER_USER_UID)
                    
                    // Call markAsDelivered
                    viewModel.markAsDelivered(message)
                    advanceUntilIdle()
                    
                    // Repository should be called
                    repository.markAsDeliveredCalled shouldBe true
                    repository.markAsDeliveredMessage?.id shouldBe messageId
                }
            }
        }
        
        test("Property 1: markLastMessageAsRead should NOT call repository when disableReceipt is true") {
            checkAll(100, Arb.long(1L, 10000L)) { messageId ->
                runTest {
                    val repository = ReceiptMockRepository()
                    val viewModel = TestableReceiptViewModel(repository)
                    
                    // Set disableReceipt to true
                    viewModel.setDisableReceiptForTest(true)
                    
                    // Set parentMessageId to -1 (main conversation)
                    viewModel.setParentMessageIdForTest(-1L)
                    
                    // Create unread message from another user (parentMessageId = 0 for main conversation)
                    val message = createMockMessage(messageId, OTHER_USER_UID, readAt = 0L, parentMessageId = 0L)
                    
                    // Call markLastMessageAsRead
                    viewModel.markLastMessageAsRead(message)
                    advanceUntilIdle()
                    
                    // Repository should NOT be called
                    repository.markAsReadCalled shouldBe false
                }
            }
        }
        
        test("Property 1: markLastMessageAsRead should call repository when disableReceipt is false") {
            checkAll(100, Arb.long(1L, 10000L)) { messageId ->
                runTest {
                    val repository = ReceiptMockRepository()
                    val viewModel = TestableReceiptViewModel(repository)
                    
                    // Set disableReceipt to false (default)
                    viewModel.setDisableReceiptForTest(false)
                    
                    // Set parentMessageId to -1 (main conversation)
                    viewModel.setParentMessageIdForTest(-1L)
                    
                    // Create unread message from another user (parentMessageId = 0 for main conversation)
                    val message = createMockMessage(messageId, OTHER_USER_UID, readAt = 0L, parentMessageId = 0L)
                    
                    // Call markLastMessageAsRead
                    viewModel.markLastMessageAsRead(message)
                    advanceUntilIdle()
                    
                    // Repository should be called
                    repository.markAsReadCalled shouldBe true
                    repository.markAsReadMessage?.id shouldBe messageId
                }
            }
        }
        
        test("Property 1: For any boolean disableReceipt value, receipt behavior is consistent") {
            checkAll(100, Arb.boolean(), Arb.long(1L, 10000L)) { disableReceipt, messageId ->
                runTest {
                    val repository = ReceiptMockRepository()
                    val viewModel = TestableReceiptViewModel(repository)
                    
                    // Set disableReceipt to the generated value
                    viewModel.setDisableReceiptForTest(disableReceipt)
                    
                    // Set parentMessageId to -1 (main conversation)
                    viewModel.setParentMessageIdForTest(-1L)
                    
                    // Create unread message from another user
                    val message = createMockMessage(messageId, OTHER_USER_UID, readAt = 0L, parentMessageId = 0L)
                    
                    // Call markAsDelivered
                    viewModel.markAsDelivered(message)
                    advanceUntilIdle()
                    
                    // Verify: receipts only sent when disableReceipt is false
                    repository.markAsDeliveredCalled shouldBe !disableReceipt
                    
                    // Reset for next test
                    repository.reset()
                    
                    // Call markLastMessageAsRead
                    viewModel.markLastMessageAsRead(message)
                    advanceUntilIdle()
                    
                    // Verify: receipts only sent when disableReceipt is false
                    repository.markAsReadCalled shouldBe !disableReceipt
                }
            }
        }
    }


    // ========================================
    // Property 2: Self-Message Property
    // ========================================
    
    context("Property 2: Self-Message Property - no receipts for own messages") {
        
        /**
         * **Property 2: Self-Message Property**
         * 
         * *For any* message where sender == currentUser:
         * - No receipts SHALL be sent (neither delivered nor read)
         * 
         * **Validates: Requirements AC-1.3, AC-2.4**
         */
        
        test("Property 2: markAsDelivered should NOT call repository for own messages") {
            checkAll(100, Arb.long(1L, 10000L)) { messageId ->
                runTest {
                    val repository = ReceiptMockRepository()
                    val viewModel = TestableReceiptViewModel(repository)
                    
                    // Ensure disableReceipt is false
                    viewModel.setDisableReceiptForTest(false)
                    
                    // Create message from the LOGGED IN USER (own message)
                    val message = createMockMessage(messageId, LOGGED_IN_USER_UID)
                    
                    // Call markAsDelivered
                    viewModel.markAsDelivered(message)
                    advanceUntilIdle()
                    
                    // Repository should NOT be called for own messages
                    repository.markAsDeliveredCalled shouldBe false
                }
            }
        }
        
        test("Property 2: markAsDelivered should call repository for messages from other users") {
            checkAll(100, Arb.long(1L, 10000L)) { messageId ->
                runTest {
                    val repository = ReceiptMockRepository()
                    val viewModel = TestableReceiptViewModel(repository)
                    
                    // Ensure disableReceipt is false
                    viewModel.setDisableReceiptForTest(false)
                    
                    // Create message from ANOTHER USER
                    val message = createMockMessage(messageId, OTHER_USER_UID)
                    
                    // Call markAsDelivered
                    viewModel.markAsDelivered(message)
                    advanceUntilIdle()
                    
                    // Repository should be called for messages from other users
                    repository.markAsDeliveredCalled shouldBe true
                }
            }
        }
        
        test("Property 2: markLastMessageAsRead should NOT call repository for own messages") {
            checkAll(100, Arb.long(1L, 10000L)) { messageId ->
                runTest {
                    val repository = ReceiptMockRepository()
                    val viewModel = TestableReceiptViewModel(repository)
                    
                    // Ensure disableReceipt is false
                    viewModel.setDisableReceiptForTest(false)
                    
                    // Set parentMessageId to -1 (main conversation)
                    viewModel.setParentMessageIdForTest(-1L)
                    
                    // Create unread message from the LOGGED IN USER (own message)
                    val message = createMockMessage(messageId, LOGGED_IN_USER_UID, readAt = 0L, parentMessageId = 0L)
                    
                    // Call markLastMessageAsRead
                    viewModel.markLastMessageAsRead(message)
                    advanceUntilIdle()
                    
                    // Repository should NOT be called for own messages
                    repository.markAsReadCalled shouldBe false
                }
            }
        }
        
        test("Property 2: markLastMessageAsRead should call repository for messages from other users") {
            checkAll(100, Arb.long(1L, 10000L)) { messageId ->
                runTest {
                    val repository = ReceiptMockRepository()
                    val viewModel = TestableReceiptViewModel(repository)
                    
                    // Ensure disableReceipt is false
                    viewModel.setDisableReceiptForTest(false)
                    
                    // Set parentMessageId to -1 (main conversation)
                    viewModel.setParentMessageIdForTest(-1L)
                    
                    // Create unread message from ANOTHER USER
                    val message = createMockMessage(messageId, OTHER_USER_UID, readAt = 0L, parentMessageId = 0L)
                    
                    // Call markLastMessageAsRead
                    viewModel.markLastMessageAsRead(message)
                    advanceUntilIdle()
                    
                    // Repository should be called for messages from other users
                    repository.markAsReadCalled shouldBe true
                }
            }
        }
        
        test("Property 2: For any sender UID, receipts are only sent for non-self messages") {
            checkAll(100, Arb.string(5, 20), Arb.long(1L, 10000L)) { senderUid, messageId ->
                runTest {
                    val repository = ReceiptMockRepository()
                    val viewModel = TestableReceiptViewModel(repository)
                    
                    // Ensure disableReceipt is false
                    viewModel.setDisableReceiptForTest(false)
                    
                    // Set parentMessageId to -1 (main conversation)
                    viewModel.setParentMessageIdForTest(-1L)
                    
                    // Create message with the generated sender UID
                    val message = createMockMessage(messageId, senderUid, readAt = 0L, parentMessageId = 0L)
                    
                    val isSelfMessage = senderUid == LOGGED_IN_USER_UID
                    
                    // Call markAsDelivered
                    viewModel.markAsDelivered(message)
                    advanceUntilIdle()
                    
                    // Verify: no receipts for self messages
                    repository.markAsDeliveredCalled shouldBe !isSelfMessage
                    
                    // Reset for next test
                    repository.reset()
                    
                    // Call markLastMessageAsRead
                    viewModel.markLastMessageAsRead(message)
                    advanceUntilIdle()
                    
                    // Verify: no receipts for self messages
                    repository.markAsReadCalled shouldBe !isSelfMessage
                }
            }
        }
    }


    // ========================================
    // Property 3: Idempotency Property
    // ========================================
    
    context("Property 3: Idempotency Property - already-read messages not re-marked") {
        
        /**
         * **Property 3: Idempotency Property**
         * 
         * *For any* message where readAt > 0 (already read):
         * - Calling markLastMessageAsRead SHALL have no effect
         * - The repository.markAsRead SHALL NOT be called
         * 
         * **Validates: Requirements AC-2.1**
         */
        
        test("Property 3: markLastMessageAsRead should NOT call repository when readAt > 0") {
            checkAll(100, Arb.long(1L, 10000L), Arb.long(1L, Long.MAX_VALUE / 2)) { messageId, readAt ->
                runTest {
                    val repository = ReceiptMockRepository()
                    val viewModel = TestableReceiptViewModel(repository)
                    
                    // Ensure disableReceipt is false
                    viewModel.setDisableReceiptForTest(false)
                    
                    // Set parentMessageId to -1 (main conversation)
                    viewModel.setParentMessageIdForTest(-1L)
                    
                    // Create ALREADY READ message from another user (readAt > 0)
                    val message = createMockMessage(messageId, OTHER_USER_UID, readAt = readAt, parentMessageId = 0L)
                    
                    // Call markLastMessageAsRead
                    viewModel.markLastMessageAsRead(message)
                    advanceUntilIdle()
                    
                    // Repository should NOT be called for already-read messages
                    repository.markAsReadCalled shouldBe false
                }
            }
        }
        
        test("Property 3: markLastMessageAsRead should call repository when readAt == 0") {
            checkAll(100, Arb.long(1L, 10000L)) { messageId ->
                runTest {
                    val repository = ReceiptMockRepository()
                    val viewModel = TestableReceiptViewModel(repository)
                    
                    // Ensure disableReceipt is false
                    viewModel.setDisableReceiptForTest(false)
                    
                    // Set parentMessageId to -1 (main conversation)
                    viewModel.setParentMessageIdForTest(-1L)
                    
                    // Create UNREAD message from another user (readAt == 0)
                    val message = createMockMessage(messageId, OTHER_USER_UID, readAt = 0L, parentMessageId = 0L)
                    
                    // Call markLastMessageAsRead
                    viewModel.markLastMessageAsRead(message)
                    advanceUntilIdle()
                    
                    // Repository should be called for unread messages
                    repository.markAsReadCalled shouldBe true
                }
            }
        }
        
        test("Property 3: For any readAt value, markLastMessageAsRead only marks unread messages") {
            checkAll(100, Arb.long(0L, Long.MAX_VALUE / 2), Arb.long(1L, 10000L)) { readAt, messageId ->
                runTest {
                    val repository = ReceiptMockRepository()
                    val viewModel = TestableReceiptViewModel(repository)
                    
                    // Ensure disableReceipt is false
                    viewModel.setDisableReceiptForTest(false)
                    
                    // Set parentMessageId to -1 (main conversation)
                    viewModel.setParentMessageIdForTest(-1L)
                    
                    // Create message with the generated readAt value
                    val message = createMockMessage(messageId, OTHER_USER_UID, readAt = readAt, parentMessageId = 0L)
                    
                    val isUnread = readAt == 0L
                    
                    // Call markLastMessageAsRead
                    viewModel.markLastMessageAsRead(message)
                    advanceUntilIdle()
                    
                    // Verify: only unread messages (readAt == 0) are marked
                    repository.markAsReadCalled shouldBe isUnread
                }
            }
        }
        
        test("Property 3: Multiple calls to markLastMessageAsRead on same already-read message have no effect") {
            checkAll(100, Arb.long(1L, 10000L), Arb.long(1L, Long.MAX_VALUE / 2)) { messageId, readAt ->
                runTest {
                    val repository = ReceiptMockRepository()
                    val viewModel = TestableReceiptViewModel(repository)
                    
                    // Ensure disableReceipt is false
                    viewModel.setDisableReceiptForTest(false)
                    
                    // Set parentMessageId to -1 (main conversation)
                    viewModel.setParentMessageIdForTest(-1L)
                    
                    // Create ALREADY READ message
                    val message = createMockMessage(messageId, OTHER_USER_UID, readAt = readAt, parentMessageId = 0L)
                    
                    // Call markLastMessageAsRead multiple times
                    repeat(5) {
                        viewModel.markLastMessageAsRead(message)
                        advanceUntilIdle()
                    }
                    
                    // Repository should NEVER be called for already-read messages
                    repository.markAsReadCalled shouldBe false
                }
            }
        }
    }


    // ========================================
    // Property 4: Thread Context Property
    // ========================================
    
    context("Property 4: Thread Context Property - correct parentMessageId filtering") {
        
        /**
         * **Property 4: Thread Context Property**
         * 
         * *For any* message in a thread context:
         * - IF parentMessageId == -1 (main conversation), THEN only messages with 
         *   message.parentMessageId == 0 SHALL be marked
         * - IF parentMessageId > -1 (thread view), THEN only messages with 
         *   message.parentMessageId == parentMessageId SHALL be marked
         * 
         * **Validates: Requirements AC-4.1, AC-4.2**
         */
        
        // ----------------------------------------
        // Main Conversation Context (parentMessageId == -1)
        // ----------------------------------------
        
        test("Property 4: In main conversation, messages with parentMessageId == 0 are marked") {
            checkAll(100, Arb.long(1L, 10000L)) { messageId ->
                runTest {
                    val repository = ReceiptMockRepository()
                    val viewModel = TestableReceiptViewModel(repository)
                    
                    // Ensure disableReceipt is false
                    viewModel.setDisableReceiptForTest(false)
                    
                    // Set parentMessageId to -1 (main conversation)
                    viewModel.setParentMessageIdForTest(-1L)
                    
                    // Create unread message with parentMessageId == 0 (main conversation message)
                    val message = createMockMessage(messageId, OTHER_USER_UID, readAt = 0L, parentMessageId = 0L)
                    
                    // Call markLastMessageAsRead
                    viewModel.markLastMessageAsRead(message)
                    advanceUntilIdle()
                    
                    // Repository should be called for main conversation messages
                    repository.markAsReadCalled shouldBe true
                }
            }
        }
        
        test("Property 4: In main conversation, thread replies (parentMessageId > 0) are NOT marked") {
            checkAll(100, Arb.long(1L, 10000L), Arb.long(1L, 10000L)) { messageId, threadParentId ->
                runTest {
                    val repository = ReceiptMockRepository()
                    val viewModel = TestableReceiptViewModel(repository)
                    
                    // Ensure disableReceipt is false
                    viewModel.setDisableReceiptForTest(false)
                    
                    // Set parentMessageId to -1 (main conversation)
                    viewModel.setParentMessageIdForTest(-1L)
                    
                    // Create unread message with parentMessageId > 0 (thread reply)
                    val message = createMockMessage(messageId, OTHER_USER_UID, readAt = 0L, parentMessageId = threadParentId)
                    
                    // Call markLastMessageAsRead
                    viewModel.markLastMessageAsRead(message)
                    advanceUntilIdle()
                    
                    // Repository should NOT be called for thread replies in main conversation
                    repository.markAsReadCalled shouldBe false
                }
            }
        }
        
        // ----------------------------------------
        // Thread View Context (parentMessageId > -1)
        // ----------------------------------------
        
        test("Property 4: In thread view, messages with matching parentMessageId are marked") {
            checkAll(100, Arb.long(1L, 10000L), Arb.long(1L, 10000L)) { messageId, threadParentId ->
                runTest {
                    val repository = ReceiptMockRepository()
                    val viewModel = TestableReceiptViewModel(repository)
                    
                    // Ensure disableReceipt is false
                    viewModel.setDisableReceiptForTest(false)
                    
                    // Set parentMessageId to the thread parent (thread view)
                    viewModel.setParentMessageIdForTest(threadParentId)
                    
                    // Create unread message with MATCHING parentMessageId
                    val message = createMockMessage(messageId, OTHER_USER_UID, readAt = 0L, parentMessageId = threadParentId)
                    
                    // Call markLastMessageAsRead
                    viewModel.markLastMessageAsRead(message)
                    advanceUntilIdle()
                    
                    // Repository should be called for matching thread messages
                    repository.markAsReadCalled shouldBe true
                }
            }
        }
        
        test("Property 4: In thread view, messages with non-matching parentMessageId are NOT marked") {
            checkAll(100, Arb.long(1L, 10000L), Arb.long(1L, 5000L), Arb.long(5001L, 10000L)) { 
                messageId, threadParentId, differentParentId ->
                runTest {
                    val repository = ReceiptMockRepository()
                    val viewModel = TestableReceiptViewModel(repository)
                    
                    // Ensure disableReceipt is false
                    viewModel.setDisableReceiptForTest(false)
                    
                    // Set parentMessageId to the thread parent (thread view)
                    viewModel.setParentMessageIdForTest(threadParentId)
                    
                    // Create unread message with DIFFERENT parentMessageId
                    val message = createMockMessage(messageId, OTHER_USER_UID, readAt = 0L, parentMessageId = differentParentId)
                    
                    // Call markLastMessageAsRead
                    viewModel.markLastMessageAsRead(message)
                    advanceUntilIdle()
                    
                    // Repository should NOT be called for non-matching thread messages
                    repository.markAsReadCalled shouldBe false
                }
            }
        }
        
        test("Property 4: In thread view, main conversation messages (parentMessageId == 0) are NOT marked") {
            checkAll(100, Arb.long(1L, 10000L), Arb.long(1L, 10000L)) { messageId, threadParentId ->
                runTest {
                    val repository = ReceiptMockRepository()
                    val viewModel = TestableReceiptViewModel(repository)
                    
                    // Ensure disableReceipt is false
                    viewModel.setDisableReceiptForTest(false)
                    
                    // Set parentMessageId to the thread parent (thread view)
                    viewModel.setParentMessageIdForTest(threadParentId)
                    
                    // Create unread message with parentMessageId == 0 (main conversation message)
                    val message = createMockMessage(messageId, OTHER_USER_UID, readAt = 0L, parentMessageId = 0L)
                    
                    // Call markLastMessageAsRead
                    viewModel.markLastMessageAsRead(message)
                    advanceUntilIdle()
                    
                    // Repository should NOT be called for main conversation messages in thread view
                    repository.markAsReadCalled shouldBe false
                }
            }
        }
        
        test("Property 4: Thread context filtering is consistent for any parentMessageId combination") {
            checkAll(100, Arb.long(-1L, 10000L), Arb.long(0L, 10000L), Arb.long(1L, 10000L)) { 
                viewModelParentId, messageParentId, messageId ->
                runTest {
                    val repository = ReceiptMockRepository()
                    val viewModel = TestableReceiptViewModel(repository)
                    
                    // Ensure disableReceipt is false
                    viewModel.setDisableReceiptForTest(false)
                    
                    // Set parentMessageId for the ViewModel
                    viewModel.setParentMessageIdForTest(viewModelParentId)
                    
                    // Create unread message with the generated parentMessageId
                    val message = createMockMessage(messageId, OTHER_USER_UID, readAt = 0L, parentMessageId = messageParentId)
                    
                    // Determine expected behavior based on thread context
                    val shouldMark = when {
                        // Main conversation: only mark messages with parentMessageId == 0
                        viewModelParentId == -1L -> messageParentId == 0L
                        // Thread view: only mark messages matching the thread
                        else -> messageParentId == viewModelParentId
                    }
                    
                    // Call markLastMessageAsRead
                    viewModel.markLastMessageAsRead(message)
                    advanceUntilIdle()
                    
                    // Verify thread context filtering
                    repository.markAsReadCalled shouldBe shouldMark
                }
            }
        }
    }
})
