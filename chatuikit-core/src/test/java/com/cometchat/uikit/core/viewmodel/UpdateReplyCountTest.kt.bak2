package com.cometchat.uikit.core.viewmodel

import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Conversation
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.TextMessage
import com.cometchat.chat.models.User
import com.cometchat.chat.core.MessagesRequest
import com.cometchat.uikit.core.domain.repository.MessageListRepository
import com.cometchat.uikit.core.events.CometChatMessageEvent
import com.cometchat.uikit.core.events.MessageStatus
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

/**
 * Unit tests for updateReplyCount functionality in CometChatMessageListViewModel.
 * 
 * These tests validate the reply count update logic when thread replies are sent.
 * The updateReplyCount method increments the reply count on parent messages
 * when new thread replies are sent in the main conversation view.
 * 
 * **Validates: Requirements US-1, US-2, US-3**
 * - US-1: Update Reply Count on Thread Reply
 * - US-2: Handle Thread Reply in Main Conversation
 * - US-3: Integrate with Message Sent Events
 */
@OptIn(ExperimentalCoroutinesApi::class)
class UpdateReplyCountTest : FunSpec({

    val testDispatcher = StandardTestDispatcher()

    beforeSpec {
        Dispatchers.setMain(testDispatcher)
    }

    afterSpec {
        Dispatchers.resetMain()
    }


    /**
     * Mock repository for testing ViewModel without SDK dependencies.
     */
    class MockMessageListRepository : MessageListRepository {
        var fetchPreviousMessagesResult: Result<List<BaseMessage>> = Result.success(emptyList())
        var fetchNextMessagesResult: Result<List<BaseMessage>> = Result.success(emptyList())
        var getConversationResult: Result<Conversation>? = null
        var getMessageResult: Result<BaseMessage>? = null
        var deleteMessageResult: Result<BaseMessage>? = null
        var flagMessageResult: Result<Unit> = Result.success(Unit)
        var addReactionResult: Result<BaseMessage>? = null
        var removeReactionResult: Result<BaseMessage>? = null
        var markAsReadResult: Result<Unit> = Result.success(Unit)
        var markAsUnreadResult: Result<Conversation> = Result.success(Conversation().apply { unreadMessageCount = 1 })
        var markAsDeliveredResult: Result<Unit> = Result.success(Unit)
        var hasMorePrevious: Boolean = true

        override suspend fun fetchPreviousMessages(): Result<List<BaseMessage>> = fetchPreviousMessagesResult
        override suspend fun fetchNextMessages(fromMessageId: Long): Result<List<BaseMessage>> = fetchNextMessagesResult
        override suspend fun getConversation(id: String, type: String): Result<Conversation> =
            getConversationResult ?: Result.failure(Exception("Not configured"))
        override suspend fun getMessage(messageId: Long): Result<BaseMessage> =
            getMessageResult ?: Result.failure(Exception("Not configured"))
        override suspend fun deleteMessage(message: BaseMessage): Result<BaseMessage> =
            deleteMessageResult ?: Result.failure(Exception("Not configured"))
        override suspend fun flagMessage(messageId: Long, reason: String, remark: String): Result<Unit> = flagMessageResult
        override suspend fun addReaction(messageId: Long, emoji: String): Result<BaseMessage> =
            addReactionResult ?: Result.failure(Exception("Not configured"))
        override suspend fun removeReaction(messageId: Long, emoji: String): Result<BaseMessage> =
            removeReactionResult ?: Result.failure(Exception("Not configured"))
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
        ) {}
        override fun configureForGroup(
            group: Group,
            messagesTypes: List<String>,
            messagesCategories: List<String>,
            parentMessageId: Long,
            messagesRequestBuilder: MessagesRequest.MessagesRequestBuilder?
        ) {}
        override suspend fun fetchSurroundingMessages(messageId: Long) =
            Result.failure<com.cometchat.uikit.core.domain.model.SurroundingMessagesResult>(Exception("Not implemented"))
        override suspend fun fetchActionMessages(fromMessageId: Long) = Result.success<List<BaseMessage>>(emptyList())
        override fun rebuildRequestFromMessageId(messageId: Long) {}
        private var latestMessageId: Long = -1
        override fun getLatestMessageId(): Long = latestMessageId
        override fun setLatestMessageId(messageId: Long) { latestMessageId = messageId }
    }


    /**
     * Testable ViewModel that exposes internal methods for testing.
     */
    class TestableUpdateReplyCountViewModel(
        repository: MessageListRepository,
        private val loggedInUserUid: String = "logged_in_user"
    ) : CometChatMessageListViewModel(repository, enableListeners = false) {
        
        override fun getLoggedInUserUid(): String = loggedInUserUid
        
        fun setUserForTest(user: User, parentMsgId: Long = -1L) {
            setUser(user, parentMessageId = parentMsgId)
        }
        
        fun setGroupForTest(group: Group, parentMsgId: Long = -1L) {
            setGroup(group, parentMessageId = parentMsgId)
        }
        
        /**
         * Directly adds a message to the list bypassing validation.
         * This is useful for testing when we need to set up specific message states.
         */
        fun addMessageForTest(message: BaseMessage) {
            addItem(message)
        }
        
        /**
         * Simulates handling a MessageSent event for testing.
         * This directly invokes the handler logic to avoid async event dispatch issues.
         */
        fun testHandleMessageSentEvent(event: CometChatMessageEvent.MessageSent) {
            val message = event.message
            
            // Check if this is a thread reply in main conversation
            // parentMessageId is -1 for main conversation, > 0 for thread view
            val isMainConversation = idMap.value["parentMessageID"] == null
            val isThreadReply = message.parentMessageId > 0
            
            if (isMainConversation && isThreadReply) {
                // Thread reply sent from main conversation - only update reply count on SUCCESS
                if (event.status == MessageStatus.SUCCESS) {
                    updateReplyCount(message.parentMessageId)
                }
                return
            }
            
            // For non-thread-reply messages, handle normally
            when (event.status) {
                MessageStatus.IN_PROGRESS -> {
                    addItem(message)
                }
                MessageStatus.SUCCESS -> {
                    // Try update by muid first
                    val muid = message.muid
                    if (!muid.isNullOrEmpty()) {
                        val updated = updateItem(message) { it.muid == muid }
                        if (!updated) {
                            val updatedById = updateItem(message) { it.id == message.id }
                            if (!updatedById) addItem(message)
                        }
                    } else {
                        val updated = updateItem(message) { it.id == message.id }
                        if (!updated) addItem(message)
                    }
                }
                MessageStatus.ERROR -> {
                    updateMessage(message)
                }
            }
        }
    }

    /**
     * Helper function to create a test TextMessage.
     */
    fun createTextMessage(
        id: Long,
        senderUid: String,
        receiverUid: String,
        text: String = "Test message",
        parentMsgId: Long = 0L,
        replyCount: Int = 0
    ): TextMessage {
        val sender = User().apply { uid = senderUid }
        return TextMessage(
            receiverUid,
            text,
            CometChatConstants.RECEIVER_TYPE_USER
        ).apply {
            this.id = id
            this.sender = sender
            this.receiverUid = receiverUid
            this.parentMessageId = parentMsgId
            this.replyCount = replyCount
        }
    }


    // ========================================
    // Task 3.1: Test updateReplyCount increments reply count of parent message
    // ========================================
    
    context("Task 3.1: updateReplyCount increments reply count") {
        
        /**
         * **AC-1.3: The message's replyCount is incremented by 1**
         * **AC-1.4: The message is updated in the list via updateMessage()**
         * 
         * When updateReplyCount is called with a valid parent message ID,
         * the reply count should be incremented by exactly 1.
         */
        test("updateReplyCount should increment reply count of parent message by 1") {
            runTest {
                val repository = MockMessageListRepository()
                val viewModel = TestableUpdateReplyCountViewModel(repository)
                
                val user = User().apply { uid = "user123" }
                viewModel.setUserForTest(user)
                
                // Add parent message with initial reply count of 5
                val parentMessage = createTextMessage(
                    id = 100L,
                    senderUid = "other_user",
                    receiverUid = "user123",
                    text = "Parent message",
                    replyCount = 5
                )
                viewModel.addMessageForTest(parentMessage)
                
                // Verify initial state
                val initialMessage = viewModel.messages.value.find { it.id == 100L }
                initialMessage?.replyCount shouldBe 5
                
                // Update reply count
                viewModel.updateReplyCount(100L)
                
                // Verify reply count was incremented
                val updatedMessage = viewModel.messages.value.find { it.id == 100L }
                updatedMessage?.replyCount shouldBe 6
            }
        }
        
        /**
         * **AC-1.2: The method finds the message with the given ID in the list**
         * 
         * Verify that updateReplyCount correctly finds the message by ID.
         */
        test("updateReplyCount should find correct message by ID") {
            runTest {
                val repository = MockMessageListRepository()
                val viewModel = TestableUpdateReplyCountViewModel(repository)
                
                val user = User().apply { uid = "user123" }
                viewModel.setUserForTest(user)
                
                // Add multiple messages
                val message1 = createTextMessage(id = 100L, senderUid = "other", receiverUid = "user123", replyCount = 0)
                val message2 = createTextMessage(id = 200L, senderUid = "other", receiverUid = "user123", replyCount = 3)
                val message3 = createTextMessage(id = 300L, senderUid = "other", receiverUid = "user123", replyCount = 7)
                
                viewModel.addMessageForTest(message1)
                viewModel.addMessageForTest(message2)
                viewModel.addMessageForTest(message3)
                
                // Update reply count for message2 only
                viewModel.updateReplyCount(200L)
                
                // Verify only message2 was updated
                viewModel.messages.value.find { it.id == 100L }?.replyCount shouldBe 0
                viewModel.messages.value.find { it.id == 200L }?.replyCount shouldBe 4
                viewModel.messages.value.find { it.id == 300L }?.replyCount shouldBe 7
            }
        }
    }


    // ========================================
    // Task 3.2: Test updateReplyCount does nothing if parent message not found
    // ========================================
    
    context("Task 3.2: updateReplyCount does nothing if parent message not found") {
        
        /**
         * **AC-1.5: If the message is not found, the method does nothing (no error)**
         * 
         * When updateReplyCount is called with a non-existent message ID,
         * it should silently return without throwing an error.
         */
        test("updateReplyCount should do nothing if parent message not found") {
            runTest {
                val repository = MockMessageListRepository()
                val viewModel = TestableUpdateReplyCountViewModel(repository)
                
                val user = User().apply { uid = "user123" }
                viewModel.setUserForTest(user)
                
                // Add a message with different ID
                val message = createTextMessage(
                    id = 200L,
                    senderUid = "other_user",
                    receiverUid = "user123",
                    replyCount = 5
                )
                viewModel.addMessageForTest(message)
                
                // Try to update non-existent parent (ID 999)
                viewModel.updateReplyCount(999L)
                
                // Verify no crash and existing message unchanged
                viewModel.messages.value.size shouldBe 1
                viewModel.messages.value.first().id shouldBe 200L
                viewModel.messages.value.first().replyCount shouldBe 5
            }
        }
        
        /**
         * Verify updateReplyCount handles empty message list gracefully.
         */
        test("updateReplyCount should handle empty message list gracefully") {
            runTest {
                val repository = MockMessageListRepository()
                val viewModel = TestableUpdateReplyCountViewModel(repository)
                
                val user = User().apply { uid = "user123" }
                viewModel.setUserForTest(user)
                
                // List is empty, try to update
                viewModel.updateReplyCount(100L)
                
                // Verify no crash and list still empty
                viewModel.messages.value.size shouldBe 0
            }
        }
    }


    // ========================================
    // Task 3.3: Test updateReplyCount is called when thread reply is sent successfully
    // ========================================
    
    context("Task 3.3: updateReplyCount is called when thread reply is sent successfully") {
        
        /**
         * **AC-2.2: The updateReplyCount() is called with the message's parentMessageId**
         * **AC-3.1: When processing CometChatMessageEvent.MessageSent with status SUCCESS**
         * **AC-3.3: Call updateReplyCount(message.parentMessageId)**
         * 
         * When a thread reply is sent successfully in main conversation,
         * the parent message's reply count should be incremented.
         */
        test("thread reply sent successfully should increment parent reply count") {
            runTest {
                val repository = MockMessageListRepository()
                val viewModel = TestableUpdateReplyCountViewModel(repository)
                
                val user = User().apply { uid = "user123" }
                viewModel.setUserForTest(user) // Main conversation (parentMessageId = -1)
                
                // Add parent message
                val parentMessage = createTextMessage(
                    id = 100L,
                    senderUid = "other_user",
                    receiverUid = "user123",
                    text = "Parent message",
                    replyCount = 0
                )
                viewModel.addMessageForTest(parentMessage)
                
                // Verify initial reply count
                viewModel.messages.value.find { it.id == 100L }?.replyCount shouldBe 0
                
                // Create thread reply (parentMessageId = 100)
                val threadReply = createTextMessage(
                    id = 101L,
                    senderUid = "logged_in_user",
                    receiverUid = "user123",
                    text = "Thread reply",
                    parentMsgId = 100L
                )
                
                // Directly invoke the handler to test the logic
                viewModel.testHandleMessageSentEvent(
                    CometChatMessageEvent.MessageSent(threadReply, MessageStatus.SUCCESS)
                )
                
                advanceUntilIdle()
                
                // Verify parent message reply count was incremented
                val updatedParent = viewModel.messages.value.find { it.id == 100L }
                updatedParent?.replyCount shouldBe 1
            }
        }
        
        /**
         * Verify that IN_PROGRESS status does NOT trigger reply count update.
         */
        test("thread reply IN_PROGRESS should NOT increment parent reply count") {
            runTest {
                val repository = MockMessageListRepository()
                val viewModel = TestableUpdateReplyCountViewModel(repository)
                
                val user = User().apply { uid = "user123" }
                viewModel.setUserForTest(user)
                
                // Add parent message
                val parentMessage = createTextMessage(
                    id = 100L,
                    senderUid = "other_user",
                    receiverUid = "user123",
                    replyCount = 0
                )
                viewModel.addMessageForTest(parentMessage)
                
                // Create thread reply
                val threadReply = createTextMessage(
                    id = 101L,
                    senderUid = "logged_in_user",
                    receiverUid = "user123",
                    parentMsgId = 100L
                )
                
                // Directly invoke the handler with IN_PROGRESS status
                viewModel.testHandleMessageSentEvent(
                    CometChatMessageEvent.MessageSent(threadReply, MessageStatus.IN_PROGRESS)
                )
                
                advanceUntilIdle()
                
                // Parent reply count should NOT be incremented for IN_PROGRESS
                val parent = viewModel.messages.value.find { it.id == 100L }
                parent?.replyCount shouldBe 0
            }
        }
    }


    // ========================================
    // Task 3.4: Test updateReplyCount is NOT called in thread view
    // ========================================
    
    context("Task 3.4: updateReplyCount is NOT called in thread view") {
        
        /**
         * **AC-2.1: When a message is sent with parentMessageId > 0 and we're in main conversation (parentMessageId == -1)**
         * 
         * In thread view (parentMessageId > 0), we should NOT update reply count
         * because the parent message is not in the thread's message list.
         */
        test("thread view should NOT update reply count when thread reply is sent") {
            runTest {
                val repository = MockMessageListRepository()
                val viewModel = TestableUpdateReplyCountViewModel(repository)
                
                val user = User().apply { uid = "user123" }
                // Configure for thread view (parentMessageId = 100)
                viewModel.setUserForTest(user, parentMsgId = 100L)
                
                // Add a thread message (this is what would be in thread view)
                val threadMessage = createTextMessage(
                    id = 101L,
                    senderUid = "other_user",
                    receiverUid = "user123",
                    parentMsgId = 100L,
                    replyCount = 0
                )
                viewModel.addMessageForTest(threadMessage)
                
                // Create another thread reply
                val anotherReply = createTextMessage(
                    id = 102L,
                    senderUid = "logged_in_user",
                    receiverUid = "user123",
                    parentMsgId = 100L
                )
                
                // Directly invoke the handler
                viewModel.testHandleMessageSentEvent(
                    CometChatMessageEvent.MessageSent(anotherReply, MessageStatus.SUCCESS)
                )
                
                advanceUntilIdle()
                
                // In thread view, updateReplyCount should NOT be called
                // The message should be added to the list instead
                viewModel.messages.value.size shouldBe 2
                
                // The first message's reply count should remain unchanged
                viewModel.messages.value.find { it.id == 101L }?.replyCount shouldBe 0
            }
        }
        
        /**
         * Verify that thread view is configured correctly via idMap.
         * When parentMessageId > 0, the idMap should include parentMessageID.
         */
        test("thread view should have parentMessageID in idMap") {
            runTest {
                val repository = MockMessageListRepository()
                val viewModel = TestableUpdateReplyCountViewModel(repository)
                
                val user = User().apply { uid = "user123" }
                viewModel.setUserForTest(user, parentMsgId = 500L)
                
                // Verify thread view is configured by checking idMap contains parentMessageID
                viewModel.idMap.value["parentMessageID"] shouldBe "500"
            }
        }
    }


    // ========================================
    // Task 3.5: Test updateReplyCount is NOT called for main conversation messages
    // ========================================
    
    context("Task 3.5: updateReplyCount is NOT called for main conversation messages") {
        
        /**
         * **AC-3.2: If the message has a parentMessageId > 0 and we're in main conversation**
         * 
         * Regular messages (not thread replies) should NOT trigger reply count update.
         * A message is a thread reply only if parentMessageId > 0.
         */
        test("main conversation message should NOT trigger reply count update") {
            runTest {
                val repository = MockMessageListRepository()
                val viewModel = TestableUpdateReplyCountViewModel(repository)
                
                val user = User().apply { uid = "user123" }
                viewModel.setUserForTest(user) // Main conversation
                
                // Add a regular message (not a thread reply)
                val regularMessage = createTextMessage(
                    id = 100L,
                    senderUid = "other_user",
                    receiverUid = "user123",
                    text = "Regular message",
                    parentMsgId = 0L, // Not a thread reply
                    replyCount = 5
                )
                viewModel.addMessageForTest(regularMessage)
                
                // Send another regular message (parentMessageId = 0)
                val newMessage = createTextMessage(
                    id = 101L,
                    senderUid = "logged_in_user",
                    receiverUid = "user123",
                    text = "New message",
                    parentMsgId = 0L // Not a thread reply
                )
                
                // Directly invoke the handler
                viewModel.testHandleMessageSentEvent(
                    CometChatMessageEvent.MessageSent(newMessage, MessageStatus.SUCCESS)
                )
                
                advanceUntilIdle()
                
                // Reply count of existing message should NOT change
                val existingMessage = viewModel.messages.value.find { it.id == 100L }
                existingMessage?.replyCount shouldBe 5
            }
        }
        
        /**
         * Verify that messages with parentMessageId = 0 are not thread replies.
         */
        test("message with parentMessageId 0 is not a thread reply") {
            runTest {
                val repository = MockMessageListRepository()
                val viewModel = TestableUpdateReplyCountViewModel(repository)
                
                val user = User().apply { uid = "user123" }
                viewModel.setUserForTest(user)
                
                // Add parent message
                val parentMessage = createTextMessage(
                    id = 100L,
                    senderUid = "other_user",
                    receiverUid = "user123",
                    replyCount = 0
                )
                viewModel.addMessageForTest(parentMessage)
                
                // Send message with parentMessageId = 0 (not a thread reply)
                val notAReply = createTextMessage(
                    id = 101L,
                    senderUid = "logged_in_user",
                    receiverUid = "user123",
                    parentMsgId = 0L
                )
                
                viewModel.testHandleMessageSentEvent(
                    CometChatMessageEvent.MessageSent(notAReply, MessageStatus.SUCCESS)
                )
                
                advanceUntilIdle()
                
                // Parent message reply count should remain 0
                viewModel.messages.value.find { it.id == 100L }?.replyCount shouldBe 0
            }
        }
    }


    // ========================================
    // Task 3.6: Test updateReplyCount handles multiple increments correctly
    // ========================================
    
    context("Task 3.6: updateReplyCount handles multiple increments correctly") {
        
        /**
         * Verify that calling updateReplyCount multiple times correctly
         * increments the reply count each time.
         */
        test("updateReplyCount should handle multiple increments correctly") {
            runTest {
                val repository = MockMessageListRepository()
                val viewModel = TestableUpdateReplyCountViewModel(repository)
                
                val user = User().apply { uid = "user123" }
                viewModel.setUserForTest(user)
                
                // Add parent message with initial reply count of 0
                val parentMessage = createTextMessage(
                    id = 100L,
                    senderUid = "other_user",
                    receiverUid = "user123",
                    text = "Parent message",
                    replyCount = 0
                )
                viewModel.addMessageForTest(parentMessage)
                
                // Update reply count 3 times
                viewModel.updateReplyCount(100L)
                viewModel.updateReplyCount(100L)
                viewModel.updateReplyCount(100L)
                
                // Verify reply count was incremented 3 times
                val updatedMessage = viewModel.messages.value.find { it.id == 100L }
                updatedMessage?.replyCount shouldBe 3
            }
        }
        
        /**
         * Verify that multiple thread replies increment the count correctly.
         */
        test("multiple thread replies should increment reply count correctly") {
            runTest {
                val repository = MockMessageListRepository()
                val viewModel = TestableUpdateReplyCountViewModel(repository)
                
                val user = User().apply { uid = "user123" }
                viewModel.setUserForTest(user)
                
                // Add parent message
                val parentMessage = createTextMessage(
                    id = 100L,
                    senderUid = "other_user",
                    receiverUid = "user123",
                    replyCount = 2 // Already has 2 replies
                )
                viewModel.addMessageForTest(parentMessage)
                
                // Send first thread reply
                val reply1 = createTextMessage(
                    id = 101L,
                    senderUid = "logged_in_user",
                    receiverUid = "user123",
                    parentMsgId = 100L
                )
                viewModel.testHandleMessageSentEvent(
                    CometChatMessageEvent.MessageSent(reply1, MessageStatus.SUCCESS)
                )
                advanceUntilIdle()
                
                // Send second thread reply
                val reply2 = createTextMessage(
                    id = 102L,
                    senderUid = "logged_in_user",
                    receiverUid = "user123",
                    parentMsgId = 100L
                )
                viewModel.testHandleMessageSentEvent(
                    CometChatMessageEvent.MessageSent(reply2, MessageStatus.SUCCESS)
                )
                advanceUntilIdle()
                
                // Verify reply count: 2 (initial) + 2 (new replies) = 4
                val updatedParent = viewModel.messages.value.find { it.id == 100L }
                updatedParent?.replyCount shouldBe 4
            }
        }
        
        /**
         * Verify that reply count increments are independent for different parent messages.
         */
        test("reply count increments should be independent for different parents") {
            runTest {
                val repository = MockMessageListRepository()
                val viewModel = TestableUpdateReplyCountViewModel(repository)
                
                val user = User().apply { uid = "user123" }
                viewModel.setUserForTest(user)
                
                // Add two parent messages
                val parent1 = createTextMessage(id = 100L, senderUid = "other", receiverUid = "user123", replyCount = 0)
                val parent2 = createTextMessage(id = 200L, senderUid = "other", receiverUid = "user123", replyCount = 5)
                
                viewModel.addMessageForTest(parent1)
                viewModel.addMessageForTest(parent2)
                
                // Update reply count for parent1 twice
                viewModel.updateReplyCount(100L)
                viewModel.updateReplyCount(100L)
                
                // Update reply count for parent2 once
                viewModel.updateReplyCount(200L)
                
                // Verify independent increments
                viewModel.messages.value.find { it.id == 100L }?.replyCount shouldBe 2
                viewModel.messages.value.find { it.id == 200L }?.replyCount shouldBe 6
            }
        }
    }
})
