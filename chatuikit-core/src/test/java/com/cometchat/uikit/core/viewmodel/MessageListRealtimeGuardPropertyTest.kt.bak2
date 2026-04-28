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
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.long
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
 * Property-based tests for real-time message reception guard in [CometChatMessageListViewModel].
 * Each test validates Property 14 from the design document.
 *
 * Feature: message-list-fetching-parity
 * **Validates: Requirements 6.1-6.5**
 *
 * ## Test Coverage
 *
 * | Property | Description | Requirements |
 * |----------|-------------|--------------|
 * | Property 14 | Real-time Message Reception Guard | 6.1, 6.2, 6.3, 6.4, 6.5 |
 *
 * ## Property 14: Real-time Message Reception Guard
 *
 * *For any* incoming real-time message:
 * - IF `messages.isEmpty() || messages.last().id == latestMessageId`, THEN the message SHALL be added
 *   and `latestMessageId` SHALL be updated
 * - ELSE the message SHALL NOT be added AND `hasMoreNewMessages` SHALL be set to true
 *
 * ## Testing Approach
 *
 * These tests use a testable ViewModel subclass that exposes the handleIncomingMessage method
 * for testing. The mock repository tracks latestMessageId updates to verify the correct
 * behavior of the real-time message guard.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MessageListRealtimeGuardPropertyTest : FunSpec({

    val testDispatcher = StandardTestDispatcher()

    beforeSpec {
        Dispatchers.setMain(testDispatcher)
    }

    afterSpec {
        Dispatchers.resetMain()
    }


    /**
     * Mock repository for testing real-time message guard functionality.
     * Tracks latestMessageId updates.
     */
    class RealtimeGuardMockRepository : MessageListRepository {
        private var latestMessageId: Long = -1
        var hasMorePrevious: Boolean = true
        var fetchPreviousMessagesResult: Result<List<BaseMessage>> = Result.success(emptyList())
        var fetchNextMessagesResult: Result<List<BaseMessage>> = Result.success(emptyList())
        
        override fun getLatestMessageId(): Long = latestMessageId
        override fun setLatestMessageId(messageId: Long) { latestMessageId = messageId }
        
        override suspend fun fetchPreviousMessages(): Result<List<BaseMessage>> = fetchPreviousMessagesResult
        override suspend fun fetchNextMessages(fromMessageId: Long): Result<List<BaseMessage>> = fetchNextMessagesResult
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
        override suspend fun markAsRead(message: BaseMessage): Result<Unit> = Result.success(Unit)
        override suspend fun markAsDelivered(message: BaseMessage): Result<Unit> = Result.success(Unit)
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
        
        fun reset() {
            latestMessageId = -1
            hasMorePrevious = true
        }
    }

    /**
     * Testable ViewModel subclass that exposes internal methods for testing.
     * This allows us to directly call handleIncomingMessage and set up the message list
     * and latestMessageId for testing the real-time message guard.
     */
    class TestableRealtimeGuardViewModel(
        repository: MessageListRepository
    ) : CometChatMessageListViewModel(repository, enableListeners = false) {
        
        /**
         * Override to return null for logged-in user UID in tests.
         * This avoids loading the CometChat SDK which is not initialized in tests.
         */
        override fun getLoggedInUserUid(): String? = null
        
        /**
         * Sets the user field via reflection for testing.
         * This avoids calling setUser() which triggers repository configuration.
         */
        fun setUserForTest(user: User) {
            val field = CometChatMessageListViewModel::class.java.getDeclaredField("user")
            field.isAccessible = true
            field.set(this, user)
        }
        
        /**
         * Sets the group field via reflection for testing.
         * This avoids calling setGroup() which triggers repository configuration.
         */
        fun setGroupForTest(group: Group) {
            val field = CometChatMessageListViewModel::class.java.getDeclaredField("group")
            field.isAccessible = true
            field.set(this, group)
        }
        
        /**
         * Directly sets the messages list for testing.
         * Uses reflection to access the private _messages field.
         */
        fun setMessagesForTest(messages: List<BaseMessage>) {
            val field = CometChatMessageListViewModel::class.java.getDeclaredField("_messages")
            field.isAccessible = true
            @Suppress("UNCHECKED_CAST")
            val stateFlow = field.get(this) as MutableStateFlow<List<BaseMessage>>
            stateFlow.value = messages
        }
        
        /**
         * Sets the latestMessageId for testing.
         * Uses reflection to access the private latestMessageId field.
         */
        fun setLatestMessageIdForTest(messageId: Long) {
            val field = CometChatMessageListViewModel::class.java.getDeclaredField("latestMessageId")
            field.isAccessible = true
            field.setLong(this, messageId)
        }
        
        /**
         * Gets the latestMessageId for verification.
         * Uses reflection to access the private latestMessageId field.
         */
        fun getLatestMessageIdForTest(): Long {
            val field = CometChatMessageListViewModel::class.java.getDeclaredField("latestMessageId")
            field.isAccessible = true
            return field.getLong(this)
        }
        
        /**
         * Calls the private handleIncomingMessage method for testing.
         * Uses reflection to access the private method.
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

    // In a 1-on-1 chat, the `user` field represents the OTHER user (the person you're chatting with)
    // The logged-in user is retrieved via getLoggedInUserUid() which we override to return null in tests
    val OTHER_USER_UID = "other_user"
    val LOGGED_IN_USER_UID = "logged_in_user"

    /**
     * Creates a test user for configuring the ViewModel.
     * This represents the OTHER user (the person we're chatting with), not the logged-in user.
     */
    fun createOtherUser(): User {
        return User().apply { uid = OTHER_USER_UID }
    }

    /**
     * Creates mock messages for testing.
     * Messages are from the OTHER_USER (the person we're chatting with) to the logged-in user.
     * 
     * @param count Number of messages to create
     * @param startId Starting ID for the messages
     * @param senderId Sender UID for all messages (defaults to OTHER_USER - the person we're chatting with)
     * @param receiverId Receiver UID for all messages (defaults to LOGGED_IN_USER)
     */
    fun createMockMessages(
        count: Int, 
        startId: Long = 1L, 
        senderId: String = OTHER_USER_UID,
        receiverId: String = LOGGED_IN_USER_UID
    ): List<BaseMessage> {
        return (0 until count).map { index ->
            val id = startId + index
            TextMessage(
                receiverId,
                "Test message $id",
                CometChatConstants.RECEIVER_TYPE_USER
            ).apply {
                this.id = id
                sentAt = System.currentTimeMillis() + (index * 1000)
                sender = User().apply { uid = senderId }
                receiverUid = receiverId
            }
        }
    }

    /**
     * Creates a single incoming message for testing.
     * The message is from the OTHER_USER (the person we're chatting with) to the logged-in user.
     * 
     * @param messageId The ID for the message
     * @param senderId Sender UID (defaults to OTHER_USER - the person we're chatting with)
     * @param receiverId Receiver UID (defaults to LOGGED_IN_USER)
     */
    fun createIncomingMessage(
        messageId: Long,
        senderId: String = OTHER_USER_UID,
        receiverId: String = LOGGED_IN_USER_UID
    ): BaseMessage {
        return TextMessage(
            receiverId,
            "Incoming message $messageId",
            CometChatConstants.RECEIVER_TYPE_USER
        ).apply {
            this.id = messageId
            sentAt = System.currentTimeMillis()
            sender = User().apply { uid = senderId }
            receiverUid = receiverId
        }
    }


    // ========================================
    // Property 14: Real-time Message Reception Guard
    // ========================================
    
    context("Property 14: Real-time Message Reception Guard") {
        
        /**
         * **Property 14: Real-time Message Reception Guard**
         * 
         * *For any* incoming real-time message:
         * - IF `messages.isEmpty() || messages.last().id == latestMessageId`, THEN the message 
         *   SHALL be added and `latestMessageId` SHALL be updated
         * - ELSE the message SHALL NOT be added AND `hasMoreNewMessages` SHALL be set to true
         * 
         * **Validates: Requirements 6.1, 6.2, 6.3, 6.4, 6.5**
         */
        
        // ----------------------------------------
        // Scenario 1: Empty message list
        // ----------------------------------------
        
        test("Property 14: When message list is empty, incoming message is added") {
            checkAll(100, Arb.long(1L, 10000L)) { messageId ->
                runTest {
                    val repository = RealtimeGuardMockRepository()
                    val viewModel = TestableRealtimeGuardViewModel(repository)
                    
                    // Configure for user conversation via reflection
                    val testUser = createOtherUser()
                    viewModel.setUserForTest(testUser)
                    
                    // Message list is empty by default
                    viewModel.messages.value.isEmpty() shouldBe true
                    
                    // Create incoming message from OTHER_USER to TEST_USER
                    val incomingMessage = createIncomingMessage(messageId)
                    
                    // Call handleIncomingMessage
                    viewModel.callHandleIncomingMessageForTest(incomingMessage)
                    advanceUntilIdle()
                    
                    // Message should be added
                    viewModel.messages.value.size shouldBe 1
                    viewModel.messages.value.first().id shouldBe messageId
                }
            }
        }
        
        test("Property 14: When message list is empty, latestMessageId is updated to new message ID") {
            checkAll(100, Arb.long(1L, 10000L)) { messageId ->
                runTest {
                    val repository = RealtimeGuardMockRepository()
                    val viewModel = TestableRealtimeGuardViewModel(repository)
                    
                    // Configure for user conversation via reflection
                    val testUser = createOtherUser()
                    viewModel.setUserForTest(testUser)
                    
                    // Message list is empty, latestMessageId is -1
                    viewModel.messages.value.isEmpty() shouldBe true
                    viewModel.getLatestMessageIdForTest() shouldBe -1L
                    
                    // Create incoming message
                    val incomingMessage = createIncomingMessage(messageId)
                    
                    // Call handleIncomingMessage
                    viewModel.callHandleIncomingMessageForTest(incomingMessage)
                    advanceUntilIdle()
                    
                    // latestMessageId should be updated
                    viewModel.getLatestMessageIdForTest() shouldBe messageId
                }
            }
        }
        
        // ----------------------------------------
        // Scenario 2: Last message ID equals latestMessageId (user at latest position)
        // ----------------------------------------
        
        test("Property 14: When last message ID equals latestMessageId, incoming message is added") {
            checkAll(100, Arb.int(1, 20), Arb.long(1L, 1000L)) { messageCount, startId ->
                runTest {
                    val repository = RealtimeGuardMockRepository()
                    val viewModel = TestableRealtimeGuardViewModel(repository)
                    
                    // Configure for user conversation via reflection
                    val testUser = createOtherUser()
                    viewModel.setUserForTest(testUser)
                    
                    // Set up initial messages
                    val initialMessages = createMockMessages(messageCount, startId = startId)
                    viewModel.setMessagesForTest(initialMessages)
                    
                    val lastMessageId = initialMessages.last().id
                    
                    // Set latestMessageId to match the last message ID (user is at latest position)
                    viewModel.setLatestMessageIdForTest(lastMessageId)
                    
                    // Create incoming message with a new ID
                    val newMessageId = lastMessageId + 1
                    val incomingMessage = createIncomingMessage(newMessageId)
                    
                    // Call handleIncomingMessage
                    viewModel.callHandleIncomingMessageForTest(incomingMessage)
                    advanceUntilIdle()
                    
                    // Message should be added
                    viewModel.messages.value.size shouldBe (messageCount + 1)
                    viewModel.messages.value.last().id shouldBe newMessageId
                }
            }
        }
        
        test("Property 14: When last message ID equals latestMessageId, latestMessageId is updated") {
            checkAll(100, Arb.int(1, 20), Arb.long(1L, 1000L)) { messageCount, startId ->
                runTest {
                    val repository = RealtimeGuardMockRepository()
                    val viewModel = TestableRealtimeGuardViewModel(repository)
                    
                    // Configure for user conversation via reflection
                    val testUser = createOtherUser()
                    viewModel.setUserForTest(testUser)
                    
                    // Set up initial messages
                    val initialMessages = createMockMessages(messageCount, startId = startId)
                    viewModel.setMessagesForTest(initialMessages)
                    
                    val lastMessageId = initialMessages.last().id
                    
                    // Set latestMessageId to match the last message ID
                    viewModel.setLatestMessageIdForTest(lastMessageId)
                    
                    // Create incoming message with a new ID
                    val newMessageId = lastMessageId + 1
                    val incomingMessage = createIncomingMessage(newMessageId)
                    
                    // Call handleIncomingMessage
                    viewModel.callHandleIncomingMessageForTest(incomingMessage)
                    advanceUntilIdle()
                    
                    // latestMessageId should be updated to the new message ID
                    viewModel.getLatestMessageIdForTest() shouldBe newMessageId
                }
            }
        }

        
        // ----------------------------------------
        // Scenario 3: Last message ID does NOT equal latestMessageId (user scrolled up)
        // ----------------------------------------
        
        test("Property 14: When last message ID does NOT equal latestMessageId, message is NOT added") {
            checkAll(100, Arb.int(1, 20), Arb.long(1L, 500L), Arb.long(100L, 200L)) { 
                messageCount, startId, latestIdOffset ->
                runTest {
                    val repository = RealtimeGuardMockRepository()
                    val viewModel = TestableRealtimeGuardViewModel(repository)
                    
                    // Configure for user conversation via reflection
                    val testUser = createOtherUser()
                    viewModel.setUserForTest(testUser)
                    
                    // Set up initial messages
                    val initialMessages = createMockMessages(messageCount, startId = startId)
                    viewModel.setMessagesForTest(initialMessages)
                    
                    val lastMessageId = initialMessages.last().id
                    
                    // Set latestMessageId to a DIFFERENT value (simulating user scrolled up)
                    // latestMessageId is higher than the last message in the list
                    val latestMessageId = lastMessageId + latestIdOffset
                    viewModel.setLatestMessageIdForTest(latestMessageId)
                    
                    // Create incoming message
                    val newMessageId = latestMessageId + 1
                    val incomingMessage = createIncomingMessage(newMessageId)
                    
                    // Call handleIncomingMessage
                    viewModel.callHandleIncomingMessageForTest(incomingMessage)
                    advanceUntilIdle()
                    
                    // Message should NOT be added
                    viewModel.messages.value.size shouldBe messageCount
                    viewModel.messages.value.none { it.id == newMessageId } shouldBe true
                }
            }
        }
        
        test("Property 14: When last message ID does NOT equal latestMessageId, hasMoreNewMessages is true") {
            checkAll(100, Arb.int(1, 20), Arb.long(1L, 500L), Arb.long(100L, 200L)) { 
                messageCount, startId, latestIdOffset ->
                runTest {
                    val repository = RealtimeGuardMockRepository()
                    val viewModel = TestableRealtimeGuardViewModel(repository)
                    
                    // Configure for user conversation via reflection
                    val testUser = createOtherUser()
                    viewModel.setUserForTest(testUser)
                    
                    // Set up initial messages
                    val initialMessages = createMockMessages(messageCount, startId = startId)
                    viewModel.setMessagesForTest(initialMessages)
                    
                    val lastMessageId = initialMessages.last().id
                    
                    // Set latestMessageId to a DIFFERENT value (simulating user scrolled up)
                    val latestMessageId = lastMessageId + latestIdOffset
                    viewModel.setLatestMessageIdForTest(latestMessageId)
                    
                    // hasMoreNewMessages should initially be false
                    viewModel.hasMoreNewMessages.value shouldBe false
                    
                    // Create incoming message
                    val newMessageId = latestMessageId + 1
                    val incomingMessage = createIncomingMessage(newMessageId)
                    
                    // Call handleIncomingMessage
                    viewModel.callHandleIncomingMessageForTest(incomingMessage)
                    advanceUntilIdle()
                    
                    // hasMoreNewMessages should be set to true
                    viewModel.hasMoreNewMessages.value shouldBe true
                }
            }
        }
        
        test("Property 14: When user scrolled up, latestMessageId is NOT updated") {
            checkAll(100, Arb.int(1, 20), Arb.long(1L, 500L), Arb.long(100L, 200L)) { 
                messageCount, startId, latestIdOffset ->
                runTest {
                    val repository = RealtimeGuardMockRepository()
                    val viewModel = TestableRealtimeGuardViewModel(repository)
                    
                    // Configure for user conversation via reflection
                    val testUser = createOtherUser()
                    viewModel.setUserForTest(testUser)
                    
                    // Set up initial messages
                    val initialMessages = createMockMessages(messageCount, startId = startId)
                    viewModel.setMessagesForTest(initialMessages)
                    
                    val lastMessageId = initialMessages.last().id
                    
                    // Set latestMessageId to a DIFFERENT value (simulating user scrolled up)
                    val latestMessageId = lastMessageId + latestIdOffset
                    viewModel.setLatestMessageIdForTest(latestMessageId)
                    
                    // Create incoming message
                    val newMessageId = latestMessageId + 1
                    val incomingMessage = createIncomingMessage(newMessageId)
                    
                    // Call handleIncomingMessage
                    viewModel.callHandleIncomingMessageForTest(incomingMessage)
                    advanceUntilIdle()
                    
                    // latestMessageId should NOT be updated (remains the same)
                    viewModel.getLatestMessageIdForTest() shouldBe latestMessageId
                }
            }
        }

        
        // ----------------------------------------
        // Additional edge cases
        // ----------------------------------------
        
        test("Property 14: Multiple consecutive messages at latest position are all added") {
            checkAll(100, Arb.int(1, 10), Arb.long(1L, 500L), Arb.int(1, 5)) { 
                initialCount, startId, newMessageCount ->
                runTest {
                    val repository = RealtimeGuardMockRepository()
                    val viewModel = TestableRealtimeGuardViewModel(repository)
                    
                    // Configure for user conversation via reflection
                    val testUser = createOtherUser()
                    viewModel.setUserForTest(testUser)
                    
                    // Set up initial messages
                    val initialMessages = createMockMessages(initialCount, startId = startId)
                    viewModel.setMessagesForTest(initialMessages)
                    
                    var currentLatestId = initialMessages.last().id
                    viewModel.setLatestMessageIdForTest(currentLatestId)
                    
                    // Add multiple messages consecutively
                    for (i in 1..newMessageCount) {
                        val newMessageId = currentLatestId + 1
                        val incomingMessage = createIncomingMessage(newMessageId)
                        
                        viewModel.callHandleIncomingMessageForTest(incomingMessage)
                        advanceUntilIdle()
                        
                        // Update expected latestId for next iteration
                        currentLatestId = newMessageId
                    }
                    
                    // All messages should be added
                    viewModel.messages.value.size shouldBe (initialCount + newMessageCount)
                    
                    // latestMessageId should be the last added message
                    viewModel.getLatestMessageIdForTest() shouldBe currentLatestId
                }
            }
        }
        
        test("Property 14: hasMoreNewMessages remains true after multiple messages when scrolled up") {
            checkAll(100, Arb.int(1, 10), Arb.long(1L, 500L), Arb.int(2, 5)) { 
                initialCount, startId, newMessageCount ->
                runTest {
                    val repository = RealtimeGuardMockRepository()
                    val viewModel = TestableRealtimeGuardViewModel(repository)
                    
                    // Configure for user conversation via reflection
                    val testUser = createOtherUser()
                    viewModel.setUserForTest(testUser)
                    
                    // Set up initial messages
                    val initialMessages = createMockMessages(initialCount, startId = startId)
                    viewModel.setMessagesForTest(initialMessages)
                    
                    val lastMessageId = initialMessages.last().id
                    
                    // Set latestMessageId to a DIFFERENT value (simulating user scrolled up)
                    val latestMessageId = lastMessageId + 100
                    viewModel.setLatestMessageIdForTest(latestMessageId)
                    
                    // Add multiple messages while scrolled up
                    for (i in 1..newMessageCount) {
                        val newMessageId = latestMessageId + i
                        val incomingMessage = createIncomingMessage(newMessageId)
                        
                        viewModel.callHandleIncomingMessageForTest(incomingMessage)
                        advanceUntilIdle()
                    }
                    
                    // No messages should be added
                    viewModel.messages.value.size shouldBe initialCount
                    
                    // hasMoreNewMessages should be true
                    viewModel.hasMoreNewMessages.value shouldBe true
                }
            }
        }
        
        test("Property 14: Guard correctly identifies latest position with single message in list") {
            checkAll(100, Arb.long(1L, 10000L)) { messageId ->
                runTest {
                    val repository = RealtimeGuardMockRepository()
                    val viewModel = TestableRealtimeGuardViewModel(repository)
                    
                    // Configure for user conversation via reflection
                    val testUser = createOtherUser()
                    viewModel.setUserForTest(testUser)
                    
                    // Set up single message
                    val singleMessage = createMockMessages(1, startId = messageId)
                    viewModel.setMessagesForTest(singleMessage)
                    
                    // Set latestMessageId to match the single message
                    viewModel.setLatestMessageIdForTest(messageId)
                    
                    // Create incoming message
                    val newMessageId = messageId + 1
                    val incomingMessage = createIncomingMessage(newMessageId)
                    
                    // Call handleIncomingMessage
                    viewModel.callHandleIncomingMessageForTest(incomingMessage)
                    advanceUntilIdle()
                    
                    // Message should be added (user is at latest position)
                    viewModel.messages.value.size shouldBe 2
                    viewModel.messages.value.last().id shouldBe newMessageId
                    viewModel.getLatestMessageIdForTest() shouldBe newMessageId
                }
            }
        }
    }
})
