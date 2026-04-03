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
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

/**
 * Property-based tests for fetchMessagesWithUnreadCount functionality in [CometChatMessageListViewModel].
 * Each test validates a correctness property from the design document.
 *
 * Feature: message-list-fetching-parity
 * **Validates: Requirements 4.1-4.6**
 *
 * ## Test Coverage
 *
 * | Property | Description | Requirements |
 * |----------|-------------|--------------|
 * | Property 10 | fetchMessagesWithUnreadCount Branching Logic | 4.1, 4.3, 4.4, 4.5 |
 * | Property 11 | latestMessageId Tracking from Conversation | 4.2 |
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MessageListFetchWithUnreadPropertyTest : FunSpec({

    val testDispatcher = StandardTestDispatcher()

    beforeSpec {
        Dispatchers.setMain(testDispatcher)
    }

    afterSpec {
        Dispatchers.resetMain()
    }


    /**
     * Testable ViewModel subclass that exposes internal state for testing.
     * 
     * This subclass allows us to:
     * - Set gotoMessageId and startFromUnreadMessages properties via reflection
     * - Override getLoggedInUserUid to avoid SDK dependency
     */
    class TestableMessageListViewModel(
        repository: MessageListRepository
    ) : CometChatMessageListViewModel(repository, enableListeners = false) {
        
        /**
         * Override to return a test user UID.
         * This avoids loading the CometChat SDK which is not initialized in tests.
         */
        override fun getLoggedInUserUid(): String? = "test_logged_in_user"
        
        /**
         * Sets the gotoMessageId for testing.
         * Uses reflection to access the private field.
         */
        fun setGotoMessageIdForTest(messageId: Long) {
            val field = CometChatMessageListViewModel::class.java.getDeclaredField("gotoMessageId")
            field.isAccessible = true
            field.setLong(this, messageId)
        }
        
        /**
         * Sets the startFromUnreadMessages for testing.
         * Uses reflection to access the private field.
         */
        fun setStartFromUnreadMessagesForTest(enabled: Boolean) {
            val field = CometChatMessageListViewModel::class.java.getDeclaredField("startFromUnreadMessages")
            field.isAccessible = true
            field.setBoolean(this, enabled)
        }
        
        /**
         * Gets the latestMessageId for testing.
         * Uses reflection to access the private field.
         */
        fun getLatestMessageIdForTest(): Long {
            val field = CometChatMessageListViewModel::class.java.getDeclaredField("latestMessageId")
            field.isAccessible = true
            return field.getLong(this)
        }
        
        /**
         * Gets the lastReadMessageId for testing.
         * Uses reflection to access the private field.
         */
        fun getLastReadMessageIdForTest(): Long {
            val field = CometChatMessageListViewModel::class.java.getDeclaredField("lastReadMessageId")
            field.isAccessible = true
            return field.getLong(this)
        }
    }


    /**
     * Mock repository for testing fetchMessagesWithUnreadCount functionality.
     * Tracks method calls and allows configuring return values.
     */
    class FetchWithUnreadMockRepository : MessageListRepository {
        // Tracking variables for method calls
        var getConversationCalled = false
        var getConversationId: String? = null
        var getConversationType: String? = null
        var getMessageCalled = false
        var getMessageId: Long? = null
        var fetchSurroundingMessagesCalled = false
        var fetchSurroundingMessagesId: Long? = null
        var fetchPreviousMessagesCalled = false
        
        // Configurable return values
        var getConversationResult: Result<Conversation>? = null
        var getMessageResult: Result<BaseMessage>? = null
        var fetchSurroundingMessagesResult: Result<SurroundingMessagesResult>? = null
        var fetchPreviousMessagesResult: Result<List<BaseMessage>> = Result.success(emptyList())
        var fetchNextMessagesResult: Result<List<BaseMessage>> = Result.success(emptyList())
        var hasMorePrevious: Boolean = true
        
        private var latestMessageId: Long = -1
        
        override suspend fun getConversation(id: String, type: String): Result<Conversation> {
            getConversationCalled = true
            getConversationId = id
            getConversationType = type
            return getConversationResult ?: Result.failure(Exception("getConversation not configured"))
        }
        
        override suspend fun getMessage(messageId: Long): Result<BaseMessage> {
            getMessageCalled = true
            getMessageId = messageId
            return getMessageResult ?: Result.failure(Exception("getMessage not configured"))
        }
        
        override suspend fun fetchSurroundingMessages(messageId: Long): Result<SurroundingMessagesResult> {
            fetchSurroundingMessagesCalled = true
            fetchSurroundingMessagesId = messageId
            return fetchSurroundingMessagesResult ?: Result.failure(Exception("fetchSurroundingMessages not configured"))
        }
        
        override suspend fun fetchPreviousMessages(): Result<List<BaseMessage>> {
            fetchPreviousMessagesCalled = true
            return fetchPreviousMessagesResult
        }
        
        override suspend fun fetchNextMessages(fromMessageId: Long): Result<List<BaseMessage>> = fetchNextMessagesResult
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
        override fun rebuildRequestFromMessageId(messageId: Long) {}
        override fun configureForUser(user: User, messagesTypes: List<String>, messagesCategories: List<String>, parentMessageId: Long, messagesRequestBuilder: MessagesRequest.MessagesRequestBuilder?) {}
        override fun configureForGroup(group: Group, messagesTypes: List<String>, messagesCategories: List<String>, parentMessageId: Long, messagesRequestBuilder: MessagesRequest.MessagesRequestBuilder?) {}
        override suspend fun fetchActionMessages(fromMessageId: Long): Result<List<BaseMessage>> = Result.success(emptyList())
        override fun getLatestMessageId(): Long = latestMessageId
        override fun setLatestMessageId(messageId: Long) { latestMessageId = messageId }
        
        fun reset() {
            getConversationCalled = false
            getConversationId = null
            getConversationType = null
            getMessageCalled = false
            getMessageId = null
            fetchSurroundingMessagesCalled = false
            fetchSurroundingMessagesId = null
            fetchPreviousMessagesCalled = false
        }
    }


    /**
     * Creates a mock User for testing.
     */
    fun createMockUser(uid: String = "test_user"): User {
        return User().apply {
            this.uid = uid
            name = "Test User"
        }
    }
    
    /**
     * Creates a mock Group for testing.
     */
    fun createMockGroup(guid: String = "test_group"): Group {
        return Group().apply {
            this.guid = guid
            name = "Test Group"
        }
    }

    /**
     * Creates mock messages for testing.
     * 
     * @param count Number of messages to create
     * @param startId Starting ID for the messages
     * @param senderId Optional sender UID (defaults to "sender_{id}")
     */
    fun createMockMessages(count: Int, startId: Long = 1L, senderId: String? = null): List<BaseMessage> {
        return (0 until count).map { index ->
            val id = startId + index
            TextMessage(
                "receiver_$id",
                "Test message $id",
                CometChatConstants.RECEIVER_TYPE_USER
            ).apply {
                this.id = id
                sentAt = System.currentTimeMillis() + (index * 1000)
                sender = User().apply { uid = senderId ?: "sender_$id" }
            }
        }
    }

    /**
     * Creates a mock Conversation for testing using reflection.
     * The Conversation class has a private constructor, so we use reflection.
     * 
     * @param lastMessageId The ID of the last message in the conversation
     * @param lastReadMessageId The ID of the last read message
     * @param unreadCount The number of unread messages
     */
    fun createMockConversation(
        conversationId: String = "test_conversation",
        lastMessageId: Long = 100L,
        lastReadMessageId: Long = 50L,
        unreadCount: Int = 10
    ): Conversation {
        val lastMessage = if (lastMessageId > 0) {
            createMockMessages(1, startId = lastMessageId).first()
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
     * Creates a SurroundingMessagesResult for testing.
     */
    fun createSurroundingResult(
        olderCount: Int,
        targetId: Long,
        newerCount: Int,
        hasMorePrevious: Boolean = true,
        hasMoreNext: Boolean = true
    ): SurroundingMessagesResult {
        val olderMessages = createMockMessages(olderCount, startId = targetId - olderCount)
        val targetMessage = createMockMessages(1, startId = targetId).first()
        val newerMessages = createMockMessages(newerCount, startId = targetId + 1)
        
        return SurroundingMessagesResult(
            olderMessages = olderMessages,
            targetMessage = targetMessage,
            newerMessages = newerMessages,
            hasMorePrevious = hasMorePrevious,
            hasMoreNext = hasMoreNext
        )
    }


    // ========================================
    // Property 10: fetchMessagesWithUnreadCount Branching Logic
    // ========================================
    
    context("Property 10: fetchMessagesWithUnreadCount Branching Logic") {
        
        /**
         * **Property 10: fetchMessagesWithUnreadCount Branching Logic**
         * 
         * *For any* call to `fetchMessagesWithUnreadCount()`:
         * - IF `gotoMessageId > 0`, THEN `goToMessage(gotoMessageId)` SHALL be called
         * - ELSE IF `startFromUnreadMessages && lastReadMessageId > 0 && unreadCount > 0`, 
         *   THEN `goToMessage(lastReadMessageId, highlight=false)` SHALL be called
         * - ELSE `fetchMessages()` SHALL be called
         * 
         * **Validates: Requirements 4.1, 4.3, 4.4, 4.5**
         */
        test("Property 10: When gotoMessageId > 0, goToMessage is called (getMessage is invoked)") {
            checkAll(100, Arb.long(1L, 10000L)) { gotoMessageId ->
                runTest {
                    val repository = FetchWithUnreadMockRepository()
                    val user = createMockUser()
                    val conversation = createMockConversation(
                        lastMessageId = gotoMessageId + 100,
                        lastReadMessageId = gotoMessageId - 10,
                        unreadCount = 5
                    )
                    
                    // Set up repository to return conversation
                    repository.getConversationResult = Result.success(conversation)
                    
                    // Set up goToMessage to succeed
                    val targetMessage = createMockMessages(1, startId = gotoMessageId).first()
                    val surroundingResult = createSurroundingResult(5, gotoMessageId, 5)
                    repository.getMessageResult = Result.success(targetMessage)
                    repository.fetchSurroundingMessagesResult = Result.success(surroundingResult)
                    
                    val viewModel = TestableMessageListViewModel(repository)
                    viewModel.setUser(user, gotoMessageId = gotoMessageId)
                    
                    viewModel.fetchMessagesWithUnreadCount()
                    advanceUntilIdle()
                    
                    // goToMessage should be called, which means getMessage is invoked
                    repository.getMessageCalled shouldBe true
                    repository.getMessageId shouldBe gotoMessageId
                    // fetchPreviousMessages should NOT be called (goToMessage path)
                    repository.fetchPreviousMessagesCalled shouldBe false
                }
            }
        }
        
        test("Property 10: When gotoMessageId > 0, highlightScroll is set to true") {
            checkAll(100, Arb.long(1L, 10000L)) { gotoMessageId ->
                runTest {
                    val repository = FetchWithUnreadMockRepository()
                    val user = createMockUser()
                    val conversation = createMockConversation(
                        lastMessageId = gotoMessageId + 100,
                        lastReadMessageId = gotoMessageId - 10,
                        unreadCount = 5
                    )
                    
                    repository.getConversationResult = Result.success(conversation)
                    
                    val targetMessage = createMockMessages(1, startId = gotoMessageId).first()
                    val surroundingResult = createSurroundingResult(5, gotoMessageId, 5)
                    repository.getMessageResult = Result.success(targetMessage)
                    repository.fetchSurroundingMessagesResult = Result.success(surroundingResult)
                    
                    val viewModel = TestableMessageListViewModel(repository)
                    viewModel.setUser(user, gotoMessageId = gotoMessageId)
                    
                    viewModel.fetchMessagesWithUnreadCount()
                    advanceUntilIdle()
                    
                    // Default highlight should be true for gotoMessageId path
                    viewModel.highlightScroll.value shouldBe true
                }
            }
        }

        
        test("Property 10: When startFromUnreadMessages && lastReadMessageId > 0 && unreadCount > 0, goToMessage(lastReadMessageId) is called") {
            checkAll(100, Arb.long(1L, 5000L), Arb.int(1, 100)) { lastReadMessageId, unreadCount ->
                runTest {
                    val repository = FetchWithUnreadMockRepository()
                    val user = createMockUser()
                    val conversation = createMockConversation(
                        lastMessageId = lastReadMessageId + unreadCount,
                        lastReadMessageId = lastReadMessageId,
                        unreadCount = unreadCount
                    )
                    
                    repository.getConversationResult = Result.success(conversation)
                    
                    // Set up goToMessage to succeed
                    val targetMessage = createMockMessages(1, startId = lastReadMessageId).first()
                    val surroundingResult = createSurroundingResult(5, lastReadMessageId, 5)
                    repository.getMessageResult = Result.success(targetMessage)
                    repository.fetchSurroundingMessagesResult = Result.success(surroundingResult)
                    
                    val viewModel = TestableMessageListViewModel(repository)
                    viewModel.setUser(user, gotoMessageId = 0) // No gotoMessageId
                    viewModel.setStartFromUnreadMessages(true)
                    
                    viewModel.fetchMessagesWithUnreadCount()
                    advanceUntilIdle()
                    
                    // goToMessage should be called with lastReadMessageId
                    repository.getMessageCalled shouldBe true
                    repository.getMessageId shouldBe lastReadMessageId
                    // fetchPreviousMessages should NOT be called (goToMessage path)
                    repository.fetchPreviousMessagesCalled shouldBe false
                }
            }
        }
        
        test("Property 10: When startFromUnreadMessages path is taken, highlightScroll is false") {
            checkAll(100, Arb.long(1L, 5000L), Arb.int(1, 100)) { lastReadMessageId, unreadCount ->
                runTest {
                    val repository = FetchWithUnreadMockRepository()
                    val user = createMockUser()
                    val conversation = createMockConversation(
                        lastMessageId = lastReadMessageId + unreadCount,
                        lastReadMessageId = lastReadMessageId,
                        unreadCount = unreadCount
                    )
                    
                    repository.getConversationResult = Result.success(conversation)
                    
                    val targetMessage = createMockMessages(1, startId = lastReadMessageId).first()
                    val surroundingResult = createSurroundingResult(5, lastReadMessageId, 5)
                    repository.getMessageResult = Result.success(targetMessage)
                    repository.fetchSurroundingMessagesResult = Result.success(surroundingResult)
                    
                    val viewModel = TestableMessageListViewModel(repository)
                    viewModel.setUser(user, gotoMessageId = 0)
                    viewModel.setStartFromUnreadMessages(true)
                    
                    viewModel.fetchMessagesWithUnreadCount()
                    advanceUntilIdle()
                    
                    // highlight should be false for startFromUnreadMessages path
                    viewModel.highlightScroll.value shouldBe false
                }
            }
        }
        
        test("Property 10: When startFromUnreadMessages is false, fetchMessages is called") {
            checkAll(100, Arb.long(1L, 5000L), Arb.int(1, 100)) { lastReadMessageId, unreadCount ->
                runTest {
                    val repository = FetchWithUnreadMockRepository()
                    val user = createMockUser()
                    val conversation = createMockConversation(
                        lastMessageId = lastReadMessageId + unreadCount,
                        lastReadMessageId = lastReadMessageId,
                        unreadCount = unreadCount
                    )
                    
                    repository.getConversationResult = Result.success(conversation)
                    repository.fetchPreviousMessagesResult = Result.success(createMockMessages(10))
                    
                    val viewModel = TestableMessageListViewModel(repository)
                    viewModel.setUser(user, gotoMessageId = 0)
                    viewModel.setStartFromUnreadMessages(false) // Disabled
                    
                    viewModel.fetchMessagesWithUnreadCount()
                    advanceUntilIdle()
                    
                    // goToMessage should NOT be called (getMessage not invoked)
                    repository.getMessageCalled shouldBe false
                    // fetchPreviousMessages should be called
                    repository.fetchPreviousMessagesCalled shouldBe true
                }
            }
        }
        
        test("Property 10: When lastReadMessageId <= 0, fetchMessages is called") {
            checkAll(100, Arb.long(-100L, 0L), Arb.int(1, 100)) { lastReadMessageId, unreadCount ->
                runTest {
                    val repository = FetchWithUnreadMockRepository()
                    val user = createMockUser()
                    val conversation = createMockConversation(
                        lastMessageId = 100L,
                        lastReadMessageId = lastReadMessageId,
                        unreadCount = unreadCount
                    )
                    
                    repository.getConversationResult = Result.success(conversation)
                    repository.fetchPreviousMessagesResult = Result.success(createMockMessages(10))
                    
                    val viewModel = TestableMessageListViewModel(repository)
                    viewModel.setUser(user, gotoMessageId = 0)
                    viewModel.setStartFromUnreadMessages(true)
                    
                    viewModel.fetchMessagesWithUnreadCount()
                    advanceUntilIdle()
                    
                    // goToMessage should NOT be called because lastReadMessageId <= 0
                    repository.getMessageCalled shouldBe false
                    repository.fetchPreviousMessagesCalled shouldBe true
                }
            }
        }

        
        test("Property 10: When unreadCount <= 0, fetchMessages is called") {
            checkAll(100, Arb.long(1L, 5000L), Arb.int(-10, 0)) { lastReadMessageId, unreadCount ->
                runTest {
                    val repository = FetchWithUnreadMockRepository()
                    val user = createMockUser()
                    val conversation = createMockConversation(
                        lastMessageId = lastReadMessageId + 10,
                        lastReadMessageId = lastReadMessageId,
                        unreadCount = unreadCount
                    )
                    
                    repository.getConversationResult = Result.success(conversation)
                    repository.fetchPreviousMessagesResult = Result.success(createMockMessages(10))
                    
                    val viewModel = TestableMessageListViewModel(repository)
                    viewModel.setUser(user, gotoMessageId = 0)
                    viewModel.setStartFromUnreadMessages(true)
                    
                    viewModel.fetchMessagesWithUnreadCount()
                    advanceUntilIdle()
                    
                    // goToMessage should NOT be called because unreadCount <= 0
                    repository.getMessageCalled shouldBe false
                    repository.fetchPreviousMessagesCalled shouldBe true
                }
            }
        }
        
        test("Property 10: gotoMessageId takes priority over startFromUnreadMessages") {
            checkAll(100, Arb.long(1L, 5000L), Arb.long(5001L, 10000L), Arb.int(1, 100)) { lastReadMessageId, gotoMessageId, unreadCount ->
                runTest {
                    val repository = FetchWithUnreadMockRepository()
                    val user = createMockUser()
                    val conversation = createMockConversation(
                        lastMessageId = gotoMessageId + 100,
                        lastReadMessageId = lastReadMessageId,
                        unreadCount = unreadCount
                    )
                    
                    repository.getConversationResult = Result.success(conversation)
                    
                    // Set up goToMessage to succeed
                    val targetMessage = createMockMessages(1, startId = gotoMessageId).first()
                    val surroundingResult = createSurroundingResult(5, gotoMessageId, 5)
                    repository.getMessageResult = Result.success(targetMessage)
                    repository.fetchSurroundingMessagesResult = Result.success(surroundingResult)
                    
                    val viewModel = TestableMessageListViewModel(repository)
                    viewModel.setUser(user, gotoMessageId = gotoMessageId)
                    viewModel.setStartFromUnreadMessages(true) // Also enabled
                    
                    viewModel.fetchMessagesWithUnreadCount()
                    advanceUntilIdle()
                    
                    // goToMessage should be called with gotoMessageId, NOT lastReadMessageId
                    repository.getMessageCalled shouldBe true
                    repository.getMessageId shouldBe gotoMessageId
                    // highlight should be true (default for gotoMessageId)
                    viewModel.highlightScroll.value shouldBe true
                }
            }
        }
        
        test("Property 10: When all conditions are false, fetchMessages is called") {
            checkAll(100, Arb.long(1L, 5000L)) { lastMessageId ->
                runTest {
                    val repository = FetchWithUnreadMockRepository()
                    val user = createMockUser()
                    val conversation = createMockConversation(
                        lastMessageId = lastMessageId,
                        lastReadMessageId = 0, // No last read
                        unreadCount = 0 // No unread
                    )
                    
                    repository.getConversationResult = Result.success(conversation)
                    repository.fetchPreviousMessagesResult = Result.success(createMockMessages(10))
                    
                    val viewModel = TestableMessageListViewModel(repository)
                    viewModel.setUser(user, gotoMessageId = 0) // No gotoMessageId
                    viewModel.setStartFromUnreadMessages(false) // Disabled
                    
                    viewModel.fetchMessagesWithUnreadCount()
                    advanceUntilIdle()
                    
                    // goToMessage should NOT be called
                    repository.getMessageCalled shouldBe false
                    // fetchPreviousMessages should be called
                    repository.fetchPreviousMessagesCalled shouldBe true
                }
            }
        }
    }


    // ========================================
    // Property 11: latestMessageId Tracking from Conversation
    // ========================================
    
    context("Property 11: latestMessageId Tracking from Conversation") {
        
        /**
         * **Property 11: latestMessageId Tracking from Conversation**
         * 
         * *For any* successful `fetchMessagesWithUnreadCount()` call where the conversation
         * has a `lastMessage`, `latestMessageId` SHALL equal `conversation.lastMessage.id`.
         * 
         * **Validates: Requirements 4.2**
         */
        test("Property 11: latestMessageId is set from conversation.lastMessage.id") {
            checkAll(100, Arb.long(1L, 10000L)) { lastMessageId ->
                runTest {
                    val repository = FetchWithUnreadMockRepository()
                    val user = createMockUser()
                    val conversation = createMockConversation(
                        lastMessageId = lastMessageId,
                        lastReadMessageId = lastMessageId - 10,
                        unreadCount = 5
                    )
                    
                    repository.getConversationResult = Result.success(conversation)
                    repository.fetchPreviousMessagesResult = Result.success(createMockMessages(10))
                    
                    val viewModel = TestableMessageListViewModel(repository)
                    viewModel.setUser(user, gotoMessageId = 0)
                    viewModel.setStartFromUnreadMessages(false)
                    
                    // Initial latestMessageId should be -1
                    viewModel.getLatestMessageIdForTest() shouldBe -1L
                    
                    viewModel.fetchMessagesWithUnreadCount()
                    advanceUntilIdle()
                    
                    // After fetchMessagesWithUnreadCount, latestMessageId should be set
                    viewModel.getLatestMessageIdForTest() shouldBe lastMessageId
                }
            }
        }
        
        test("Property 11: lastReadMessageId is stored from conversation") {
            checkAll(100, Arb.long(1L, 10000L), Arb.long(1L, 5000L)) { lastMessageId, lastReadMessageId ->
                runTest {
                    val repository = FetchWithUnreadMockRepository()
                    val user = createMockUser()
                    val conversation = createMockConversation(
                        lastMessageId = lastMessageId,
                        lastReadMessageId = lastReadMessageId,
                        unreadCount = 0
                    )
                    
                    repository.getConversationResult = Result.success(conversation)
                    repository.fetchPreviousMessagesResult = Result.success(createMockMessages(5))
                    
                    val viewModel = TestableMessageListViewModel(repository)
                    viewModel.setUser(user, gotoMessageId = 0)
                    viewModel.setStartFromUnreadMessages(false)
                    
                    viewModel.fetchMessagesWithUnreadCount()
                    advanceUntilIdle()
                    
                    // lastReadMessageId should be stored
                    viewModel.getLastReadMessageIdForTest() shouldBe lastReadMessageId
                }
            }
        }

        
        test("Property 11: latestMessageId tracking works for group conversations") {
            checkAll(100, Arb.long(1L, 10000L)) { lastMessageId ->
                runTest {
                    val repository = FetchWithUnreadMockRepository()
                    val group = createMockGroup()
                    val conversation = createMockConversation(
                        lastMessageId = lastMessageId,
                        lastReadMessageId = 0,
                        unreadCount = 0
                    )
                    
                    repository.getConversationResult = Result.success(conversation)
                    repository.fetchPreviousMessagesResult = Result.success(createMockMessages(5))
                    
                    val viewModel = TestableMessageListViewModel(repository)
                    viewModel.setGroup(group, gotoMessageId = 0)
                    
                    viewModel.fetchMessagesWithUnreadCount()
                    advanceUntilIdle()
                    
                    // Verify conversation was fetched with correct parameters for group
                    repository.getConversationCalled shouldBe true
                    repository.getConversationId shouldBe group.guid
                    repository.getConversationType shouldBe CometChatConstants.RECEIVER_TYPE_GROUP
                    
                    // latestMessageId should be set
                    viewModel.getLatestMessageIdForTest() shouldBe lastMessageId
                }
            }
        }
        
        test("Property 11: When conversation has no lastMessage (id <= 0), latestMessageId is -1") {
            runTest {
                val repository = FetchWithUnreadMockRepository()
                val user = createMockUser()
                
                // Create conversation with no lastMessage (lastMessageId = -1)
                val conversation = createMockConversation(
                    lastMessageId = -1, // No last message
                    lastReadMessageId = 0,
                    unreadCount = 0
                )
                
                repository.getConversationResult = Result.success(conversation)
                repository.fetchPreviousMessagesResult = Result.success(emptyList())
                
                val viewModel = TestableMessageListViewModel(repository)
                viewModel.setUser(user, gotoMessageId = 0)
                
                viewModel.fetchMessagesWithUnreadCount()
                advanceUntilIdle()
                
                // latestMessageId should remain -1 when there's no lastMessage
                viewModel.getLatestMessageIdForTest() shouldBe -1L
            }
        }
        
        test("Property 11: On getConversation failure, gotoMessageId is still honored if set") {
            checkAll(100, Arb.long(1L, 10000L)) { gotoMessageId ->
                runTest {
                    val repository = FetchWithUnreadMockRepository()
                    val user = createMockUser()
                    
                    // Conversation fetch fails
                    repository.getConversationResult = Result.failure(Exception("Network error"))
                    
                    // But goToMessage should still work
                    val targetMessage = createMockMessages(1, startId = gotoMessageId).first()
                    val surroundingResult = createSurroundingResult(5, gotoMessageId, 5)
                    repository.getMessageResult = Result.success(targetMessage)
                    repository.fetchSurroundingMessagesResult = Result.success(surroundingResult)
                    
                    val viewModel = TestableMessageListViewModel(repository)
                    viewModel.setUser(user, gotoMessageId = gotoMessageId)
                    
                    viewModel.fetchMessagesWithUnreadCount()
                    advanceUntilIdle()
                    
                    // goToMessage should still be called with gotoMessageId
                    repository.getMessageCalled shouldBe true
                    repository.getMessageId shouldBe gotoMessageId
                }
            }
        }
        
        test("Property 11: On getConversation failure without gotoMessageId, fetchMessages is called") {
            runTest {
                val repository = FetchWithUnreadMockRepository()
                val user = createMockUser()
                
                // Conversation fetch fails
                repository.getConversationResult = Result.failure(Exception("Network error"))
                repository.fetchPreviousMessagesResult = Result.success(createMockMessages(10))
                
                val viewModel = TestableMessageListViewModel(repository)
                viewModel.setUser(user, gotoMessageId = 0) // No gotoMessageId
                
                viewModel.fetchMessagesWithUnreadCount()
                advanceUntilIdle()
                
                // fetchMessages should be called as fallback
                repository.getMessageCalled shouldBe false
                repository.fetchPreviousMessagesCalled shouldBe true
            }
        }
    }


    // ========================================
    // Additional Edge Case Tests
    // ========================================
    
    context("Edge Cases") {
        
        test("fetchMessagesWithUnreadCount stores unreadCount from conversation") {
            checkAll(100, Arb.int(0, 1000)) { unreadCount ->
                runTest {
                    val repository = FetchWithUnreadMockRepository()
                    val user = createMockUser()
                    val conversation = createMockConversation(
                        lastMessageId = 100L,
                        lastReadMessageId = 50L,
                        unreadCount = unreadCount
                    )
                    
                    repository.getConversationResult = Result.success(conversation)
                    repository.fetchPreviousMessagesResult = Result.success(createMockMessages(10))
                    
                    val viewModel = TestableMessageListViewModel(repository)
                    viewModel.setUser(user, gotoMessageId = 0)
                    viewModel.setStartFromUnreadMessages(false)
                    
                    viewModel.fetchMessagesWithUnreadCount()
                    advanceUntilIdle()
                    
                    // Verify unreadCount is stored
                    viewModel.unreadCount.value shouldBe unreadCount
                }
            }
        }
        
        test("fetchMessagesWithUnreadCount works with both user and group conversations") {
            runTest {
                val repository = FetchWithUnreadMockRepository()
                
                // Test with user
                val user = createMockUser("user_123")
                val userConversation = createMockConversation(
                    lastMessageId = 100L,
                    lastReadMessageId = 50L,
                    unreadCount = 5
                )
                
                repository.getConversationResult = Result.success(userConversation)
                repository.fetchPreviousMessagesResult = Result.success(createMockMessages(5))
                
                val viewModel1 = TestableMessageListViewModel(repository)
                viewModel1.setUser(user, gotoMessageId = 0)
                
                viewModel1.fetchMessagesWithUnreadCount()
                advanceUntilIdle()
                
                repository.getConversationId shouldBe "user_123"
                repository.getConversationType shouldBe CometChatConstants.RECEIVER_TYPE_USER
                
                // Reset and test with group
                repository.reset()
                
                val group = createMockGroup("group_456")
                val groupConversation = createMockConversation(
                    lastMessageId = 200L,
                    lastReadMessageId = 150L,
                    unreadCount = 10
                )
                
                repository.getConversationResult = Result.success(groupConversation)
                
                val viewModel2 = TestableMessageListViewModel(repository)
                viewModel2.setGroup(group, gotoMessageId = 0)
                
                viewModel2.fetchMessagesWithUnreadCount()
                advanceUntilIdle()
                
                repository.getConversationId shouldBe "group_456"
                repository.getConversationType shouldBe CometChatConstants.RECEIVER_TYPE_GROUP
            }
        }
        
        test("Multiple fetchMessagesWithUnreadCount calls update state correctly") {
            runTest {
                val repository = FetchWithUnreadMockRepository()
                val user = createMockUser()
                
                // First call
                val conversation1 = createMockConversation(
                    lastMessageId = 100L,
                    lastReadMessageId = 50L,
                    unreadCount = 5
                )
                repository.getConversationResult = Result.success(conversation1)
                repository.fetchPreviousMessagesResult = Result.success(createMockMessages(5))
                
                val viewModel = TestableMessageListViewModel(repository)
                viewModel.setUser(user, gotoMessageId = 0)
                viewModel.setStartFromUnreadMessages(false)
                
                viewModel.fetchMessagesWithUnreadCount()
                advanceUntilIdle()
                
                viewModel.unreadCount.value shouldBe 5
                viewModel.getLatestMessageIdForTest() shouldBe 100L
                
                // Second call with different values
                val conversation2 = createMockConversation(
                    lastMessageId = 200L,
                    lastReadMessageId = 150L,
                    unreadCount = 20
                )
                repository.getConversationResult = Result.success(conversation2)
                repository.reset()
                
                viewModel.fetchMessagesWithUnreadCount()
                advanceUntilIdle()
                
                viewModel.unreadCount.value shouldBe 20
                viewModel.getLatestMessageIdForTest() shouldBe 200L
            }
        }
    }
})
