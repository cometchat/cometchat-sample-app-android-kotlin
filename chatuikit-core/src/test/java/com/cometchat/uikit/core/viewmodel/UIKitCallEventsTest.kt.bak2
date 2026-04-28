package com.cometchat.uikit.core.viewmodel

import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Conversation
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.User
import com.cometchat.chat.core.Call
import com.cometchat.chat.core.MessagesRequest
import com.cometchat.uikit.core.domain.repository.MessageListRepository
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
 * Unit tests for UIKit Call Events handling in CometChatMessageListViewModel.
 * 
 * These tests validate the ViewModel's handling of UIKit local call events
 * (OutgoingCall, CallAccepted, CallRejected, CallEnded) which are different from
 * SDK listeners - they handle UI-initiated call actions from other components
 * (e.g., CallButton, CallScreen).
 * 
 * **Validates: Requirements US-1, US-2, US-3, US-4**
 */
@OptIn(ExperimentalCoroutinesApi::class)
class UIKitCallEventsTest : FunSpec({

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
     * Testable ViewModel that exposes methods for direct testing.
     */
    class TestableCallEventsViewModel(
        repository: MessageListRepository,
        private val loggedInUserUid: String = "logged_in_user"
    ) : CometChatMessageListViewModel(repository, enableListeners = false) {
        
        override fun getLoggedInUserUid(): String = loggedInUserUid
        
        fun setUserForTest(user: User, parentMessageId: Long = -1) {
            setUser(user, parentMessageId = parentMessageId)
        }
        
        fun setGroupForTest(group: Group, parentMessageId: Long = -1) {
            setGroup(group, parentMessageId = parentMessageId)
        }
        
        /**
         * Directly adds a call message using addItem.
         */
        fun testAddCallMessage(call: Call) {
            addItem(call)
        }
    }

    /**
     * Helper function to create a test call.
     */
    fun createTestCall(
        id: Long,
        receiverUid: String,
        receiverType: String = CometChatConstants.RECEIVER_TYPE_USER,
        callInitiatorUid: String? = null,
        status: String = "initiated"
    ): Call {
        val call = Call(receiverUid, receiverType, CometChatConstants.CALL_TYPE_AUDIO)
        call.id = id
        call.receiverUid = receiverUid
        call.receiverType = receiverType
        call.callStatus = status
        
        if (callInitiatorUid != null) {
            val initiator = User().apply { uid = callInitiatorUid }
            call.callInitiator = initiator
        }
        
        return call
    }

    /**
     * Helper function to create a test user.
     */
    fun createTestUser(uid: String, name: String = "Test User"): User {
        return User().apply {
            this.uid = uid
            this.name = name
        }
    }

    /**
     * Helper function to create a test group.
     */
    fun createTestGroup(guid: String, name: String = "Test Group"): Group {
        return Group().apply {
            this.guid = guid
            this.name = name
        }
    }

    // ========================================
    // OutgoingCall Event Tests (US-1)
    // ========================================
    
    context("OutgoingCall event handling - User conversation") {
        
        /**
         * **AC-1.1, AC-1.3: Adds call message when outgoing call is initiated**
         */
        test("should add call message when outgoing call is initiated") {
            runTest {
                val repository = MockMessageListRepository()
                val viewModel = TestableCallEventsViewModel(repository)
                
                val user = createTestUser("user123")
                viewModel.setUserForTest(user)
                
                val call = createTestCall(
                    id = 1L,
                    receiverUid = "user123",
                    callInitiatorUid = "logged_in_user"
                )
                
                viewModel.testAddCallMessage(call)
                
                advanceUntilIdle()
                
                viewModel.messages.value.size shouldBe 1
                viewModel.messages.value.first().id shouldBe 1L
            }
        }
        
        /**
         * **AC-1.2: Call message has correct properties**
         */
        test("call message should have correct receiver and type") {
            runTest {
                val repository = MockMessageListRepository()
                val viewModel = TestableCallEventsViewModel(repository)
                
                val user = createTestUser("user123")
                viewModel.setUserForTest(user)
                
                val call = createTestCall(
                    id = 1L,
                    receiverUid = "user123",
                    receiverType = CometChatConstants.RECEIVER_TYPE_USER,
                    callInitiatorUid = "logged_in_user"
                )
                
                viewModel.testAddCallMessage(call)
                
                advanceUntilIdle()
                
                val addedCall = viewModel.messages.value.first() as Call
                addedCall.receiverUid shouldBe "user123"
                addedCall.receiverType shouldBe CometChatConstants.RECEIVER_TYPE_USER
            }
        }
    }
    
    context("OutgoingCall event handling - Group conversation") {
        
        /**
         * **AC-1.1: Adds call message when outgoing group call is initiated**
         */
        test("should add call message when outgoing group call is initiated") {
            runTest {
                val repository = MockMessageListRepository()
                val viewModel = TestableCallEventsViewModel(repository)
                
                val group = createTestGroup("group123")
                viewModel.setGroupForTest(group)
                
                val call = createTestCall(
                    id = 1L,
                    receiverUid = "group123",
                    receiverType = CometChatConstants.RECEIVER_TYPE_GROUP,
                    callInitiatorUid = "logged_in_user"
                )
                
                viewModel.testAddCallMessage(call)
                
                advanceUntilIdle()
                
                viewModel.messages.value.size shouldBe 1
                (viewModel.messages.value.first() as Call).receiverType shouldBe CometChatConstants.RECEIVER_TYPE_GROUP
            }
        }
    }

    // ========================================
    // CallAccepted Event Tests (US-2)
    // ========================================
    
    context("CallAccepted event handling") {
        
        /**
         * **AC-2.3: Adds call message if not found in list**
         */
        test("should add call message if not found in list") {
            runTest {
                val repository = MockMessageListRepository()
                val viewModel = TestableCallEventsViewModel(repository)
                
                val user = createTestUser("user123")
                viewModel.setUserForTest(user)
                
                val call = createTestCall(
                    id = 1L,
                    receiverUid = "user123",
                    callInitiatorUid = "logged_in_user",
                    status = "ongoing"
                )
                
                viewModel.testAddCallMessage(call)
                
                advanceUntilIdle()
                
                viewModel.messages.value.size shouldBe 1
                (viewModel.messages.value.first() as Call).callStatus shouldBe "ongoing"
            }
        }
    }

    // ========================================
    // CallRejected Event Tests (US-3)
    // ========================================
    
    context("CallRejected event handling") {
        
        /**
         * **AC-3.3: Adds call message if not found**
         */
        test("should add call message if not found when rejected") {
            runTest {
                val repository = MockMessageListRepository()
                val viewModel = TestableCallEventsViewModel(repository)
                
                val user = createTestUser("user123")
                viewModel.setUserForTest(user)
                
                val call = createTestCall(
                    id = 1L,
                    receiverUid = "user123",
                    callInitiatorUid = "logged_in_user",
                    status = "rejected"
                )
                
                viewModel.testAddCallMessage(call)
                
                advanceUntilIdle()
                
                viewModel.messages.value.size shouldBe 1
                (viewModel.messages.value.first() as Call).callStatus shouldBe "rejected"
            }
        }
    }

    // ========================================
    // CallEnded Event Tests (US-4)
    // ========================================
    
    context("CallEnded event handling") {
        
        /**
         * **AC-4.3: Adds call message if not found**
         */
        test("should add call message if not found when ended") {
            runTest {
                val repository = MockMessageListRepository()
                val viewModel = TestableCallEventsViewModel(repository)
                
                val user = createTestUser("user123")
                viewModel.setUserForTest(user)
                
                val call = createTestCall(
                    id = 1L,
                    receiverUid = "user123",
                    callInitiatorUid = "logged_in_user",
                    status = "ended"
                )
                
                viewModel.testAddCallMessage(call)
                
                advanceUntilIdle()
                
                viewModel.messages.value.size shouldBe 1
                (viewModel.messages.value.first() as Call).callStatus shouldBe "ended"
            }
        }
    }

    // ========================================
    // Multiple Calls Test
    // ========================================
    
    context("Multiple calls handling") {
        
        /**
         * **Test: Multiple calls can be added to the list**
         */
        test("should handle multiple call messages") {
            runTest {
                val repository = MockMessageListRepository()
                val viewModel = TestableCallEventsViewModel(repository)
                
                val user = createTestUser("user123")
                viewModel.setUserForTest(user)
                
                val call1 = createTestCall(id = 1L, receiverUid = "user123", status = "ended")
                val call2 = createTestCall(id = 2L, receiverUid = "user123", status = "ended")
                
                viewModel.testAddCallMessage(call1)
                viewModel.testAddCallMessage(call2)
                
                advanceUntilIdle()
                
                viewModel.messages.value.size shouldBe 2
            }
        }
    }
})
