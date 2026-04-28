package com.cometchat.uikit.core.viewmodel

import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Conversation
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.User
import com.cometchat.chat.core.MessagesRequest
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.domain.repository.MessageListRepository
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.maps.shouldContainExactly
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

/**
 * Unit tests for ID Map Management in CometChatMessageListViewModel.
 *
 * These tests validate the idMap StateFlow, generateIdMap(), getIdMap(), and matchesIdMap()
 * functionality for conversation context identification.
 *
 * **Validates: Requirements US-1, US-2, US-3**
 *
 * ## Test Coverage
 *
 * | Test | Acceptance Criteria |
 * |------|---------------------|
 * | idMap is empty initially | AC-2.1 |
 * | setUser generates correct idMap | AC-1.2, AC-1.5, AC-2.2 |
 * | setGroup generates correct idMap | AC-1.3, AC-1.5, AC-2.2 |
 * | setUser with parentMessageId includes PARENT_MESSAGE_ID | AC-1.4 |
 * | matchesIdMap returns true for matching conversation | AC-3.1, AC-3.2 |
 * | matchesIdMap returns false for different receiver | AC-3.1, AC-3.2 |
 * | matchesIdMap handles thread context correctly | AC-3.1, AC-3.2 |
 * | getIdMap returns current idMap value | AC-1.1 |
 */
@OptIn(ExperimentalCoroutinesApi::class)
class IdMapManagementTest : FunSpec({

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
        override suspend fun fetchPreviousMessages(): Result<List<BaseMessage>> = Result.success(emptyList())
        override suspend fun fetchNextMessages(fromMessageId: Long): Result<List<BaseMessage>> = Result.success(emptyList())
        override suspend fun getConversation(id: String, type: String): Result<Conversation> = Result.failure(Exception("Not configured"))
        override suspend fun getMessage(messageId: Long): Result<BaseMessage> = Result.failure(Exception("Not configured"))
        override suspend fun deleteMessage(message: BaseMessage): Result<BaseMessage> = Result.failure(Exception("Not configured"))
        override suspend fun flagMessage(messageId: Long, reason: String, remark: String): Result<Unit> = Result.success(Unit)
        override suspend fun addReaction(messageId: Long, emoji: String): Result<BaseMessage> = Result.failure(Exception("Not configured"))
        override suspend fun removeReaction(messageId: Long, emoji: String): Result<BaseMessage> = Result.failure(Exception("Not configured"))
        override suspend fun markAsRead(message: BaseMessage): Result<Unit> = Result.success(Unit)
        override suspend fun markAsDelivered(message: BaseMessage): Result<Unit> = Result.success(Unit)
        override suspend fun markAsUnread(message: BaseMessage): Result<Conversation> = 
            Result.success(Conversation().apply { unreadMessageCount = 1 })
        override fun hasMorePreviousMessages(): Boolean = true
        override fun resetRequest() {}
        override fun configureForUser(user: User, messagesTypes: List<String>, messagesCategories: List<String>, parentMessageId: Long, messagesRequestBuilder: MessagesRequest.MessagesRequestBuilder?) {}
        override fun configureForGroup(group: Group, messagesTypes: List<String>, messagesCategories: List<String>, parentMessageId: Long, messagesRequestBuilder: MessagesRequest.MessagesRequestBuilder?) {}
        override suspend fun fetchSurroundingMessages(messageId: Long) = Result.failure<com.cometchat.uikit.core.domain.model.SurroundingMessagesResult>(Exception("Not implemented"))
        override suspend fun fetchActionMessages(fromMessageId: Long) = Result.success<List<BaseMessage>>(emptyList())
        override fun rebuildRequestFromMessageId(messageId: Long) {}
        override fun getLatestMessageId(): Long = -1
        override fun setLatestMessageId(messageId: Long) {}
    }


    // ========================================
    // Task 7.1: Test idMap is empty initially
    // ========================================

    context("Task 7.1: idMap is empty initially") {

        /**
         * **AC-2.1: An idMap StateFlow is exposed for observation**
         *
         * The idMap should be empty when the ViewModel is first created,
         * before any user or group is configured.
         */
        test("idMap should be empty initially") {
            val repository = MockMessageListRepository()
            val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)

            viewModel.idMap.value shouldBe emptyMap()
        }

        test("getIdMap should return empty map initially") {
            val repository = MockMessageListRepository()
            val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)

            viewModel.getIdMap() shouldBe emptyMap()
        }
    }

    // ========================================
    // Task 7.2: Test setUser generates correct idMap for user conversation
    // ========================================

    context("Task 7.2: setUser generates correct idMap for user conversation") {

        /**
         * **AC-1.2: For user conversations, the map contains RECEIVER_ID = user.uid and RECEIVER_TYPE = "user"**
         * **AC-1.5: The map is regenerated when user/group is set**
         * **AC-2.2: The StateFlow emits when setUser() or setGroup() is called**
         */
        test("setUser should generate correct idMap for user conversation") {
            val repository = MockMessageListRepository()
            val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)

            val user = User().apply { uid = "user123" }
            viewModel.setUser(user)

            viewModel.idMap.value shouldContainExactly mapOf(
                UIKitConstants.MapId.RECEIVER_ID to "user123",
                UIKitConstants.MapId.RECEIVER_TYPE to CometChatConstants.RECEIVER_TYPE_USER
            )
        }

        test("getIdMap should return correct map after setUser") {
            val repository = MockMessageListRepository()
            val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)

            val user = User().apply { uid = "testUser456" }
            viewModel.setUser(user)

            viewModel.getIdMap() shouldContainExactly mapOf(
                UIKitConstants.MapId.RECEIVER_ID to "testUser456",
                UIKitConstants.MapId.RECEIVER_TYPE to CometChatConstants.RECEIVER_TYPE_USER
            )
        }
    }

    // ========================================
    // Task 7.3: Test setGroup generates correct idMap for group conversation
    // ========================================

    context("Task 7.3: setGroup generates correct idMap for group conversation") {

        /**
         * **AC-1.3: For group conversations, the map contains RECEIVER_ID = group.guid and RECEIVER_TYPE = "group"**
         * **AC-1.5: The map is regenerated when user/group is set**
         * **AC-2.2: The StateFlow emits when setUser() or setGroup() is called**
         */
        test("setGroup should generate correct idMap for group conversation") {
            val repository = MockMessageListRepository()
            val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)

            val group = Group().apply { guid = "group456" }
            viewModel.setGroup(group)

            viewModel.idMap.value shouldContainExactly mapOf(
                UIKitConstants.MapId.RECEIVER_ID to "group456",
                UIKitConstants.MapId.RECEIVER_TYPE to CometChatConstants.RECEIVER_TYPE_GROUP
            )
        }

        test("getIdMap should return correct map after setGroup") {
            val repository = MockMessageListRepository()
            val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)

            val group = Group().apply { guid = "testGroup789" }
            viewModel.setGroup(group)

            viewModel.getIdMap() shouldContainExactly mapOf(
                UIKitConstants.MapId.RECEIVER_ID to "testGroup789",
                UIKitConstants.MapId.RECEIVER_TYPE to CometChatConstants.RECEIVER_TYPE_GROUP
            )
        }
    }

    // ========================================
    // Task 7.4: Test setUser with parentMessageId includes PARENT_MESSAGE_ID
    // ========================================

    context("Task 7.4: setUser with parentMessageId includes PARENT_MESSAGE_ID") {

        /**
         * **AC-1.4: If parentMessageId > 0, the map contains PARENT_MESSAGE_ID = parentMessageId.toString()**
         * **AC-2.3: The StateFlow emits when parentMessageId changes**
         */
        test("setUser with parentMessageId should include PARENT_MESSAGE_ID in idMap") {
            val repository = MockMessageListRepository()
            val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)

            val user = User().apply { uid = "user123" }
            viewModel.setUser(user, parentMessageId = 999L)

            viewModel.idMap.value shouldContainExactly mapOf(
                UIKitConstants.MapId.RECEIVER_ID to "user123",
                UIKitConstants.MapId.RECEIVER_TYPE to CometChatConstants.RECEIVER_TYPE_USER,
                UIKitConstants.MapId.PARENT_MESSAGE_ID to "999"
            )
        }

        test("setGroup with parentMessageId should include PARENT_MESSAGE_ID in idMap") {
            val repository = MockMessageListRepository()
            val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)

            val group = Group().apply { guid = "group456" }
            viewModel.setGroup(group, parentMessageId = 888L)

            viewModel.idMap.value shouldContainExactly mapOf(
                UIKitConstants.MapId.RECEIVER_ID to "group456",
                UIKitConstants.MapId.RECEIVER_TYPE to CometChatConstants.RECEIVER_TYPE_GROUP,
                UIKitConstants.MapId.PARENT_MESSAGE_ID to "888"
            )
        }

        test("setUser with parentMessageId = 0 should NOT include PARENT_MESSAGE_ID") {
            val repository = MockMessageListRepository()
            val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)

            val user = User().apply { uid = "user123" }
            viewModel.setUser(user, parentMessageId = 0L)

            viewModel.idMap.value shouldContainExactly mapOf(
                UIKitConstants.MapId.RECEIVER_ID to "user123",
                UIKitConstants.MapId.RECEIVER_TYPE to CometChatConstants.RECEIVER_TYPE_USER
            )
        }

        test("setUser with parentMessageId = -1 should NOT include PARENT_MESSAGE_ID") {
            val repository = MockMessageListRepository()
            val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)

            val user = User().apply { uid = "user123" }
            viewModel.setUser(user, parentMessageId = -1L)

            viewModel.idMap.value shouldContainExactly mapOf(
                UIKitConstants.MapId.RECEIVER_ID to "user123",
                UIKitConstants.MapId.RECEIVER_TYPE to CometChatConstants.RECEIVER_TYPE_USER
            )
        }
    }


    // ========================================
    // Task 7.5: Test matchesIdMap returns true for matching conversation
    // ========================================

    context("Task 7.5: matchesIdMap returns true for matching conversation") {

        /**
         * **AC-3.1: The ID map can be compared with event metadata to filter events**
         * **AC-3.2: Helper method matchesIdMap(eventIdMap: Map<String, String>): Boolean is available**
         */
        test("matchesIdMap should return true for matching user conversation") {
            val repository = MockMessageListRepository()
            val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)

            val user = User().apply { uid = "user123" }
            viewModel.setUser(user)

            val eventIdMap = mapOf(
                UIKitConstants.MapId.RECEIVER_ID to "user123",
                UIKitConstants.MapId.RECEIVER_TYPE to CometChatConstants.RECEIVER_TYPE_USER
            )

            viewModel.matchesIdMap(eventIdMap) shouldBe true
        }

        test("matchesIdMap should return true for matching group conversation") {
            val repository = MockMessageListRepository()
            val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)

            val group = Group().apply { guid = "group456" }
            viewModel.setGroup(group)

            val eventIdMap = mapOf(
                UIKitConstants.MapId.RECEIVER_ID to "group456",
                UIKitConstants.MapId.RECEIVER_TYPE to CometChatConstants.RECEIVER_TYPE_GROUP
            )

            viewModel.matchesIdMap(eventIdMap) shouldBe true
        }
    }

    // ========================================
    // Task 7.6: Test matchesIdMap returns false for different receiver
    // ========================================

    context("Task 7.6: matchesIdMap returns false for different receiver") {

        test("matchesIdMap should return false for different receiver ID") {
            val repository = MockMessageListRepository()
            val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)

            val user = User().apply { uid = "user123" }
            viewModel.setUser(user)

            val eventIdMap = mapOf(
                UIKitConstants.MapId.RECEIVER_ID to "user456",
                UIKitConstants.MapId.RECEIVER_TYPE to CometChatConstants.RECEIVER_TYPE_USER
            )

            viewModel.matchesIdMap(eventIdMap) shouldBe false
        }

        test("matchesIdMap should return false for different receiver type") {
            val repository = MockMessageListRepository()
            val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)

            val user = User().apply { uid = "user123" }
            viewModel.setUser(user)

            val eventIdMap = mapOf(
                UIKitConstants.MapId.RECEIVER_ID to "user123",
                UIKitConstants.MapId.RECEIVER_TYPE to CometChatConstants.RECEIVER_TYPE_GROUP
            )

            viewModel.matchesIdMap(eventIdMap) shouldBe false
        }

        test("matchesIdMap should return false when both receiver ID and type differ") {
            val repository = MockMessageListRepository()
            val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)

            val user = User().apply { uid = "user123" }
            viewModel.setUser(user)

            val eventIdMap = mapOf(
                UIKitConstants.MapId.RECEIVER_ID to "group456",
                UIKitConstants.MapId.RECEIVER_TYPE to CometChatConstants.RECEIVER_TYPE_GROUP
            )

            viewModel.matchesIdMap(eventIdMap) shouldBe false
        }
    }

    // ========================================
    // Task 7.7: Test matchesIdMap handles thread context correctly
    // ========================================

    context("Task 7.7: matchesIdMap handles thread context correctly") {

        test("matchesIdMap should return true for same thread") {
            val repository = MockMessageListRepository()
            val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)

            val user = User().apply { uid = "user123" }
            viewModel.setUser(user, parentMessageId = 999L)

            val sameThreadEvent = mapOf(
                UIKitConstants.MapId.RECEIVER_ID to "user123",
                UIKitConstants.MapId.RECEIVER_TYPE to CometChatConstants.RECEIVER_TYPE_USER,
                UIKitConstants.MapId.PARENT_MESSAGE_ID to "999"
            )

            viewModel.matchesIdMap(sameThreadEvent) shouldBe true
        }

        test("matchesIdMap should return false for different thread") {
            val repository = MockMessageListRepository()
            val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)

            val user = User().apply { uid = "user123" }
            viewModel.setUser(user, parentMessageId = 999L)

            val differentThreadEvent = mapOf(
                UIKitConstants.MapId.RECEIVER_ID to "user123",
                UIKitConstants.MapId.RECEIVER_TYPE to CometChatConstants.RECEIVER_TYPE_USER,
                UIKitConstants.MapId.PARENT_MESSAGE_ID to "888"
            )

            viewModel.matchesIdMap(differentThreadEvent) shouldBe false
        }

        test("matchesIdMap should return false when current is thread but event is main conversation") {
            val repository = MockMessageListRepository()
            val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)

            val user = User().apply { uid = "user123" }
            viewModel.setUser(user, parentMessageId = 999L)

            val mainConversationEvent = mapOf(
                UIKitConstants.MapId.RECEIVER_ID to "user123",
                UIKitConstants.MapId.RECEIVER_TYPE to CometChatConstants.RECEIVER_TYPE_USER
            )

            viewModel.matchesIdMap(mainConversationEvent) shouldBe false
        }

        test("matchesIdMap should return true for main conversation when event has no parent") {
            val repository = MockMessageListRepository()
            val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)

            val user = User().apply { uid = "user123" }
            viewModel.setUser(user) // No parentMessageId = main conversation

            val mainConversationEvent = mapOf(
                UIKitConstants.MapId.RECEIVER_ID to "user123",
                UIKitConstants.MapId.RECEIVER_TYPE to CometChatConstants.RECEIVER_TYPE_USER
            )

            viewModel.matchesIdMap(mainConversationEvent) shouldBe true
        }

        test("matchesIdMap should return false for main conversation when event is for a thread") {
            val repository = MockMessageListRepository()
            val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)

            val user = User().apply { uid = "user123" }
            viewModel.setUser(user) // No parentMessageId = main conversation

            val threadEvent = mapOf(
                UIKitConstants.MapId.RECEIVER_ID to "user123",
                UIKitConstants.MapId.RECEIVER_TYPE to CometChatConstants.RECEIVER_TYPE_USER,
                UIKitConstants.MapId.PARENT_MESSAGE_ID to "999"
            )

            viewModel.matchesIdMap(threadEvent) shouldBe false
        }

        test("matchesIdMap should return true for main conversation when event has parentMessageId = 0") {
            val repository = MockMessageListRepository()
            val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)

            val user = User().apply { uid = "user123" }
            viewModel.setUser(user) // No parentMessageId = main conversation

            val eventWithZeroParent = mapOf(
                UIKitConstants.MapId.RECEIVER_ID to "user123",
                UIKitConstants.MapId.RECEIVER_TYPE to CometChatConstants.RECEIVER_TYPE_USER,
                UIKitConstants.MapId.PARENT_MESSAGE_ID to "0"
            )

            viewModel.matchesIdMap(eventWithZeroParent) shouldBe true
        }

        test("matchesIdMap should return true for main conversation when event has parentMessageId = -1") {
            val repository = MockMessageListRepository()
            val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)

            val user = User().apply { uid = "user123" }
            viewModel.setUser(user) // No parentMessageId = main conversation

            val eventWithNegativeParent = mapOf(
                UIKitConstants.MapId.RECEIVER_ID to "user123",
                UIKitConstants.MapId.RECEIVER_TYPE to CometChatConstants.RECEIVER_TYPE_USER,
                UIKitConstants.MapId.PARENT_MESSAGE_ID to "-1"
            )

            viewModel.matchesIdMap(eventWithNegativeParent) shouldBe true
        }
    }

    // ========================================
    // Task 7.8: Test getIdMap returns current idMap value
    // ========================================

    context("Task 7.8: getIdMap returns current idMap value") {

        /**
         * **AC-1.1: A getIdMap() method returns a Map<String, String> with conversation identifiers**
         */
        test("getIdMap should return current idMap value") {
            val repository = MockMessageListRepository()
            val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)

            val user = User().apply { uid = "user123" }
            viewModel.setUser(user)

            viewModel.getIdMap() shouldBe viewModel.idMap.value
        }

        test("getIdMap should update when setUser is called again") {
            val repository = MockMessageListRepository()
            val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)

            val user1 = User().apply { uid = "user123" }
            viewModel.setUser(user1)

            viewModel.getIdMap()[UIKitConstants.MapId.RECEIVER_ID] shouldBe "user123"

            val user2 = User().apply { uid = "user456" }
            viewModel.setUser(user2)

            viewModel.getIdMap()[UIKitConstants.MapId.RECEIVER_ID] shouldBe "user456"
        }

        test("getIdMap should update when switching from user to group") {
            val repository = MockMessageListRepository()
            val viewModel = CometChatMessageListViewModel(repository, enableListeners = false)

            val user = User().apply { uid = "user123" }
            viewModel.setUser(user)

            viewModel.getIdMap()[UIKitConstants.MapId.RECEIVER_TYPE] shouldBe CometChatConstants.RECEIVER_TYPE_USER

            val group = Group().apply { guid = "group456" }
            viewModel.setGroup(group)

            viewModel.getIdMap()[UIKitConstants.MapId.RECEIVER_TYPE] shouldBe CometChatConstants.RECEIVER_TYPE_GROUP
            viewModel.getIdMap()[UIKitConstants.MapId.RECEIVER_ID] shouldBe "group456"
        }
    }
})
