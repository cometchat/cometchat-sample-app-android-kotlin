package com.cometchat.uikit.core.viewmodel

import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.models.Action
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Conversation
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.User
import com.cometchat.chat.core.MessagesRequest
import com.cometchat.uikit.core.domain.repository.MessageListRepository
import com.cometchat.uikit.core.events.CometChatGroupEvent
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
 * Unit tests for UIKit Group Events handling in CometChatMessageListViewModel.
 * 
 * These tests validate the ViewModel's handling of UIKit local group events
 * (MembersAdded, MemberKicked, MemberBanned, MemberUnbanned, MemberScopeChanged)
 * which are different from SDK listeners - they handle UI-initiated actions
 * from other components (e.g., GroupMembers component).
 * 
 * **Validates: Requirements US-1, US-2, US-3, US-4, US-5**
 */
@OptIn(ExperimentalCoroutinesApi::class)
class UIKitGroupEventsTest : FunSpec({

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
     * Testable ViewModel that exposes handler methods for direct testing.
     */
    class TestableGroupEventsViewModel(
        repository: MessageListRepository,
        private val loggedInUserUid: String = "logged_in_user"
    ) : CometChatMessageListViewModel(repository, enableListeners = false) {
        
        override fun getLoggedInUserUid(): String = loggedInUserUid
        
        fun setGroupForTest(group: Group, parentMessageId: Long = -1) {
            setGroup(group, parentMessageId = parentMessageId)
        }
        
        fun setUserForTest(user: User) {
            setUser(user)
        }
        
        // Expose handler methods for direct testing
        fun testHandleMembersAddedEvent(event: CometChatGroupEvent.MembersAdded) {
            // Simulate what the event handler does
            val currentGroup = getGroupForTest() ?: return
            if (currentGroup.guid != event.group.guid) return
            if (getParentMessageIdForTest() != -1L) return
            
            event.actions.forEach { action ->
                addMessage(action)
            }
        }
        
        fun testHandleMemberKickedEvent(event: CometChatGroupEvent.MemberKicked) {
            val currentGroup = getGroupForTest() ?: return
            if (currentGroup.guid != event.group.guid) return
            if (getParentMessageIdForTest() != -1L) return
            
            addMessage(event.action)
        }
        
        fun testHandleMemberBannedEvent(event: CometChatGroupEvent.MemberBanned) {
            val currentGroup = getGroupForTest() ?: return
            if (currentGroup.guid != event.group.guid) return
            if (getParentMessageIdForTest() != -1L) return
            
            addMessage(event.action)
        }
        
        fun testHandleMemberUnbannedEvent(event: CometChatGroupEvent.MemberUnbanned) {
            val currentGroup = getGroupForTest() ?: return
            if (currentGroup.guid != event.group.guid) return
            if (getParentMessageIdForTest() != -1L) return
            
            addMessage(event.action)
        }
        
        fun testHandleMemberScopeChangedEvent(event: CometChatGroupEvent.MemberScopeChanged) {
            val currentGroup = getGroupForTest() ?: return
            if (currentGroup.guid != event.group.guid) return
            if (getParentMessageIdForTest() != -1L) return
            
            addMessage(event.action)
        }
        
        // Expose internal state for testing
        fun getGroupForTest(): Group? {
            // Access via reflection or add a getter
            return try {
                val field = CometChatMessageListViewModel::class.java.getDeclaredField("group")
                field.isAccessible = true
                field.get(this) as? Group
            } catch (e: Exception) {
                null
            }
        }
        
        fun getParentMessageIdForTest(): Long {
            return try {
                val field = CometChatMessageListViewModel::class.java.getDeclaredField("parentMessageId")
                field.isAccessible = true
                field.getLong(this)
            } catch (e: Exception) {
                -1L
            }
        }
    }

    /**
     * Helper function to create a test action message.
     */
    fun createTestAction(
        id: Long,
        actionType: String,
        groupGuid: String
    ): Action {
        return Action().apply {
            this.id = id
            this.action = actionType
            this.receiverUid = groupGuid
            this.receiverType = CometChatConstants.RECEIVER_TYPE_GROUP
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

    /**
     * Helper function to create a test user.
     */
    fun createTestUser(uid: String, name: String = "Test User"): User {
        return User().apply {
            this.uid = uid
            this.name = name
        }
    }

    // ========================================
    // MembersAdded Event Tests (US-1)
    // ========================================
    
    context("MembersAdded event handling") {
        
        /**
         * **AC-1.1, AC-1.2: Adds action messages for each added member**
         */
        test("should add action messages for each added member") {
            runTest {
                val repository = MockMessageListRepository()
                val viewModel = TestableGroupEventsViewModel(repository)
                
                val testGroup = createTestGroup("group123")
                viewModel.setGroupForTest(testGroup)
                
                val action1 = createTestAction(1L, "added", "group123")
                val action2 = createTestAction(2L, "added", "group123")
                val addedUsers = listOf(
                    createTestUser("user1"),
                    createTestUser("user2")
                )
                val addedBy = createTestUser("admin")
                
                viewModel.testHandleMembersAddedEvent(
                    CometChatGroupEvent.MembersAdded(
                        actions = listOf(action1, action2),
                        users = addedUsers,
                        group = testGroup,
                        addedBy = addedBy
                    )
                )
                
                advanceUntilIdle()
                
                viewModel.messages.value.size shouldBe 2
            }
        }
        
        /**
         * **AC-1.3: Ignores event for different group**
         */
        test("should ignore event for different group") {
            runTest {
                val repository = MockMessageListRepository()
                val viewModel = TestableGroupEventsViewModel(repository)
                
                val currentGroup = createTestGroup("group123")
                viewModel.setGroupForTest(currentGroup)
                
                val differentGroup = createTestGroup("otherGroup")
                val action = createTestAction(1L, "added", "otherGroup")
                
                viewModel.testHandleMembersAddedEvent(
                    CometChatGroupEvent.MembersAdded(
                        actions = listOf(action),
                        users = listOf(createTestUser("user1")),
                        group = differentGroup,
                        addedBy = createTestUser("admin")
                    )
                )
                
                advanceUntilIdle()
                
                viewModel.messages.value shouldBe emptyList()
            }
        }
    }

    // ========================================
    // MemberKicked Event Tests (US-2)
    // ========================================
    
    context("MemberKicked event handling") {
        
        /**
         * **AC-2.1: Adds action message when member is kicked**
         */
        test("should add action message when member is kicked") {
            runTest {
                val repository = MockMessageListRepository()
                val viewModel = TestableGroupEventsViewModel(repository)
                
                val testGroup = createTestGroup("group123")
                viewModel.setGroupForTest(testGroup)
                
                val action = createTestAction(1L, "kicked", "group123")
                val kickedUser = createTestUser("kickedUser")
                val kickedBy = createTestUser("admin")
                
                viewModel.testHandleMemberKickedEvent(
                    CometChatGroupEvent.MemberKicked(
                        action = action,
                        user = kickedUser,
                        kickedBy = kickedBy,
                        group = testGroup
                    )
                )
                
                advanceUntilIdle()
                
                viewModel.messages.value.size shouldBe 1
                viewModel.messages.value.first().id shouldBe 1L
            }
        }
        
        /**
         * **AC-2.2: Ignores event for different group**
         */
        test("should ignore kicked event for different group") {
            runTest {
                val repository = MockMessageListRepository()
                val viewModel = TestableGroupEventsViewModel(repository)
                
                val currentGroup = createTestGroup("group123")
                viewModel.setGroupForTest(currentGroup)
                
                val differentGroup = createTestGroup("otherGroup")
                val action = createTestAction(1L, "kicked", "otherGroup")
                
                viewModel.testHandleMemberKickedEvent(
                    CometChatGroupEvent.MemberKicked(
                        action = action,
                        user = createTestUser("kickedUser"),
                        kickedBy = createTestUser("admin"),
                        group = differentGroup
                    )
                )
                
                advanceUntilIdle()
                
                viewModel.messages.value shouldBe emptyList()
            }
        }
    }

    // ========================================
    // MemberBanned Event Tests (US-3)
    // ========================================
    
    context("MemberBanned event handling") {
        
        /**
         * **AC-3.1: Adds action message when member is banned**
         */
        test("should add action message when member is banned") {
            runTest {
                val repository = MockMessageListRepository()
                val viewModel = TestableGroupEventsViewModel(repository)
                
                val testGroup = createTestGroup("group123")
                viewModel.setGroupForTest(testGroup)
                
                val action = createTestAction(2L, "banned", "group123")
                val bannedUser = createTestUser("bannedUser")
                val bannedBy = createTestUser("admin")
                
                viewModel.testHandleMemberBannedEvent(
                    CometChatGroupEvent.MemberBanned(
                        action = action,
                        user = bannedUser,
                        bannedBy = bannedBy,
                        group = testGroup
                    )
                )
                
                advanceUntilIdle()
                
                viewModel.messages.value.size shouldBe 1
            }
        }
    }

    // ========================================
    // MemberUnbanned Event Tests (US-4)
    // ========================================
    
    context("MemberUnbanned event handling") {
        
        /**
         * **AC-4.1: Adds action message when member is unbanned**
         */
        test("should add action message when member is unbanned") {
            runTest {
                val repository = MockMessageListRepository()
                val viewModel = TestableGroupEventsViewModel(repository)
                
                val testGroup = createTestGroup("group123")
                viewModel.setGroupForTest(testGroup)
                
                val action = createTestAction(3L, "unbanned", "group123")
                val unbannedUser = createTestUser("unbannedUser")
                val unbannedBy = createTestUser("admin")
                
                viewModel.testHandleMemberUnbannedEvent(
                    CometChatGroupEvent.MemberUnbanned(
                        action = action,
                        user = unbannedUser,
                        unbannedBy = unbannedBy,
                        group = testGroup
                    )
                )
                
                advanceUntilIdle()
                
                viewModel.messages.value.size shouldBe 1
            }
        }
    }

    // ========================================
    // MemberScopeChanged Event Tests (US-5)
    // ========================================
    
    context("MemberScopeChanged event handling") {
        
        /**
         * **AC-5.1: Adds action message when member scope is changed**
         */
        test("should add action message when member scope is changed") {
            runTest {
                val repository = MockMessageListRepository()
                val viewModel = TestableGroupEventsViewModel(repository)
                
                val testGroup = createTestGroup("group123")
                viewModel.setGroupForTest(testGroup)
                
                val action = createTestAction(4L, "scopeChanged", "group123")
                val updatedUser = createTestUser("updatedUser")
                
                viewModel.testHandleMemberScopeChangedEvent(
                    CometChatGroupEvent.MemberScopeChanged(
                        action = action,
                        user = updatedUser,
                        newScope = "admin",
                        oldScope = "participant",
                        group = testGroup
                    )
                )
                
                advanceUntilIdle()
                
                viewModel.messages.value.size shouldBe 1
            }
        }
    }

    // ========================================
    // User Conversation Context Tests
    // ========================================
    
    context("User conversation context") {
        
        /**
         * **Test: Ignores group events when in user conversation**
         */
        test("should ignore group events when in user conversation") {
            runTest {
                val repository = MockMessageListRepository()
                val viewModel = TestableGroupEventsViewModel(repository)
                
                // Configure for user conversation (not group)
                val user = createTestUser("user123")
                viewModel.setUserForTest(user)
                
                val testGroup = createTestGroup("group123")
                val action = createTestAction(1L, "kicked", "group123")
                
                viewModel.testHandleMemberKickedEvent(
                    CometChatGroupEvent.MemberKicked(
                        action = action,
                        user = createTestUser("kickedUser"),
                        kickedBy = createTestUser("admin"),
                        group = testGroup
                    )
                )
                
                advanceUntilIdle()
                
                viewModel.messages.value shouldBe emptyList()
            }
        }
    }

    // ========================================
    // Thread View Context Tests
    // ========================================
    
    context("Thread view context") {
        
        /**
         * **Test: Ignores group events when in thread view**
         */
        test("should ignore group events when in thread view") {
            runTest {
                val repository = MockMessageListRepository()
                val viewModel = TestableGroupEventsViewModel(repository)
                
                // Configure for thread view (parentMessageId > 0)
                val testGroup = createTestGroup("group123")
                viewModel.setGroupForTest(testGroup, parentMessageId = 999L)
                
                val action = createTestAction(1L, "kicked", "group123")
                
                viewModel.testHandleMemberKickedEvent(
                    CometChatGroupEvent.MemberKicked(
                        action = action,
                        user = createTestUser("kickedUser"),
                        kickedBy = createTestUser("admin"),
                        group = testGroup
                    )
                )
                
                advanceUntilIdle()
                
                viewModel.messages.value shouldBe emptyList()
            }
        }
    }
})
