package com.cometchat.uikit.core.viewmodel

import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.models.Action
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
 * Property-based tests for missed messages fetching on reconnection in [CometChatMessageListViewModel].
 * Each test validates a correctness property from the design document.
 *
 * Feature: message-list-fetching-parity
 * **Validates: Requirements 5.1-5.7**
 *
 * ## Test Coverage
 *
 * | Property | Description | Requirements |
 * |----------|-------------|--------------|
 * | Property 12 | Missed Messages Fetch on Reconnection | 5.1, 5.2, 5.5 |
 * | Property 13 | Action Message Updates on Reconnection | 5.3, 5.4 |
 *
 * ## Testing Approach
 *
 * These tests use a testable ViewModel subclass that exposes internal methods for testing.
 * The mock repository tracks calls to fetchActionMessages and fetchNextMessages to verify
 * the correct sequence of operations during reconnection.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MessageListReconnectionPropertyTest : FunSpec({

    val testDispatcher = StandardTestDispatcher()

    beforeSpec {
        Dispatchers.setMain(testDispatcher)
    }

    afterSpec {
        Dispatchers.resetMain()
    }


    /**
     * Mock repository for testing reconnection functionality.
     * Tracks method calls and allows configuring return values.
     */
    class ReconnectionMockRepository : MessageListRepository {
        // Tracking variables for method calls
        var fetchActionMessagesCalled = false
        var fetchActionMessagesFromId: Long? = null
        var fetchNextMessagesCalled = false
        var fetchNextMessagesFromId: Long? = null
        var fetchPreviousMessagesCalled = false
        
        // Track call order
        var callOrder = mutableListOf<String>()
        
        // Configurable return values
        var fetchActionMessagesResult: Result<List<BaseMessage>> = Result.success(emptyList())
        var fetchNextMessagesResult: Result<List<BaseMessage>> = Result.success(emptyList())
        var fetchPreviousMessagesResult: Result<List<BaseMessage>> = Result.success(emptyList())
        var getMessageResult: Result<BaseMessage>? = null
        var fetchSurroundingMessagesResult: Result<SurroundingMessagesResult>? = null
        var hasMorePrevious: Boolean = true
        
        private var latestMessageId: Long = -1
        
        override suspend fun fetchActionMessages(fromMessageId: Long): Result<List<BaseMessage>> {
            fetchActionMessagesCalled = true
            fetchActionMessagesFromId = fromMessageId
            callOrder.add("fetchActionMessages")
            return fetchActionMessagesResult
        }
        
        override suspend fun fetchNextMessages(fromMessageId: Long): Result<List<BaseMessage>> {
            fetchNextMessagesCalled = true
            fetchNextMessagesFromId = fromMessageId
            callOrder.add("fetchNextMessages")
            return fetchNextMessagesResult
        }
        
        override suspend fun fetchPreviousMessages(): Result<List<BaseMessage>> {
            fetchPreviousMessagesCalled = true
            callOrder.add("fetchPreviousMessages")
            return fetchPreviousMessagesResult
        }
        
        override suspend fun getMessage(messageId: Long): Result<BaseMessage> {
            return getMessageResult ?: Result.failure(Exception("getMessage not configured"))
        }
        
        override suspend fun fetchSurroundingMessages(messageId: Long): Result<SurroundingMessagesResult> {
            return fetchSurroundingMessagesResult ?: Result.failure(Exception("fetchSurroundingMessages not configured"))
        }
        
        override fun rebuildRequestFromMessageId(messageId: Long) {}
        override suspend fun getConversation(id: String, type: String): Result<Conversation> = Result.failure(Exception("Not configured"))
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
        override fun getLatestMessageId(): Long = latestMessageId
        override fun setLatestMessageId(messageId: Long) { latestMessageId = messageId }
        
        fun reset() {
            fetchActionMessagesCalled = false
            fetchActionMessagesFromId = null
            fetchNextMessagesCalled = false
            fetchNextMessagesFromId = null
            fetchPreviousMessagesCalled = false
            callOrder.clear()
        }
    }


    /**
     * Testable ViewModel subclass that exposes internal methods for testing.
     * This allows us to directly call fetchMissedMessages and set up the message list.
     */
    class TestableReconnectionViewModel(
        repository: MessageListRepository
    ) : CometChatMessageListViewModel(repository, enableListeners = false) {
        
        /**
         * Override to return null for logged-in user UID in tests.
         * This avoids loading the CometChat SDK which is not initialized in tests.
         */
        override fun getLoggedInUserUid(): String? = null
        
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
         * Calls the private fetchMissedMessages method for testing.
         * Uses reflection to access the private method.
         */
        fun callFetchMissedMessagesForTest() {
            val method = CometChatMessageListViewModel::class.java.getDeclaredMethod("fetchMissedMessages")
            method.isAccessible = true
            method.invoke(this)
        }
    }

    // Use a sender ID that is different from the logged-in user
    val OTHER_USER_UID = "other_user"

    /**
     * Creates mock messages for testing.
     * 
     * @param count Number of messages to create
     * @param startId Starting ID for the messages
     * @param senderId Sender UID for all messages
     */
    fun createMockMessages(
        count: Int, 
        startId: Long = 1L, 
        senderId: String = OTHER_USER_UID
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
            }
        }
    }


    /**
     * Creates a mock Action message for testing.
     * 
     * @param actionId The ID of the action message
     * @param actionOnMessage The message that was acted upon (edited/deleted)
     */
    fun createMockActionMessage(
        actionId: Long,
        actionOnMessage: BaseMessage
    ): Action {
        return Action().apply {
            this.id = actionId
            this.category = CometChatConstants.CATEGORY_ACTION
            this.actionOn = actionOnMessage
            this.sentAt = System.currentTimeMillis()
            this.sender = User().apply { uid = OTHER_USER_UID }
        }
    }

    // ========================================
    // Property 12: Missed Messages Fetch on Reconnection
    // ========================================
    
    context("Property 12: Missed Messages Fetch on Reconnection") {
        
        /**
         * **Property 12: Missed Messages Fetch on Reconnection**
         * 
         * *For any* `onConnected` event from the connection listener, the ViewModel SHALL call
         * `fetchMissedMessages()` which triggers `fetchActionMessages()` followed by `fetchNextMessages()`.
         * 
         * **Validates: Requirements 5.1, 5.2, 5.5**
         */
        test("Property 12: fetchMissedMessages calls fetchActionMessages with last message ID") {
            checkAll(100, Arb.int(1, 20), Arb.long(1L, 1000L)) { messageCount, startId ->
                runTest {
                    val repository = ReconnectionMockRepository()
                    val viewModel = TestableReconnectionViewModel(repository)
                    
                    // Set up initial messages
                    val messages = createMockMessages(messageCount, startId = startId)
                    viewModel.setMessagesForTest(messages)
                    
                    val lastMessageId = messages.last().id
                    
                    // Call fetchMissedMessages
                    viewModel.callFetchMissedMessagesForTest()
                    advanceUntilIdle()
                    
                    // fetchActionMessages should be called with the last message ID
                    repository.fetchActionMessagesCalled shouldBe true
                    repository.fetchActionMessagesFromId shouldBe lastMessageId
                }
            }
        }
        
        test("Property 12: fetchMissedMessages calls fetchNextMessages after fetchActionMessages") {
            checkAll(100, Arb.int(1, 20), Arb.long(1L, 1000L)) { messageCount, startId ->
                runTest {
                    val repository = ReconnectionMockRepository()
                    val viewModel = TestableReconnectionViewModel(repository)
                    
                    // Set up initial messages
                    val messages = createMockMessages(messageCount, startId = startId)
                    viewModel.setMessagesForTest(messages)
                    
                    val lastMessageId = messages.last().id
                    
                    // Call fetchMissedMessages
                    viewModel.callFetchMissedMessagesForTest()
                    advanceUntilIdle()
                    
                    // fetchNextMessages should be called with the last message ID
                    repository.fetchNextMessagesCalled shouldBe true
                    repository.fetchNextMessagesFromId shouldBe lastMessageId
                }
            }
        }

        
        test("Property 12: fetchMissedMessages calls fetchActionMessages before fetchNextMessages") {
            checkAll(100, Arb.int(1, 20), Arb.long(1L, 1000L)) { messageCount, startId ->
                runTest {
                    val repository = ReconnectionMockRepository()
                    val viewModel = TestableReconnectionViewModel(repository)
                    
                    // Set up initial messages
                    val messages = createMockMessages(messageCount, startId = startId)
                    viewModel.setMessagesForTest(messages)
                    
                    // Call fetchMissedMessages
                    viewModel.callFetchMissedMessagesForTest()
                    advanceUntilIdle()
                    
                    // Verify call order: fetchActionMessages should be called before fetchNextMessages
                    repository.callOrder.size shouldBe 2
                    repository.callOrder[0] shouldBe "fetchActionMessages"
                    repository.callOrder[1] shouldBe "fetchNextMessages"
                }
            }
        }
        
        test("Property 12: fetchMissedMessages is a no-op when message list is empty") {
            checkAll(100, Arb.long(1L, 1000L)) { _ ->
                runTest {
                    val repository = ReconnectionMockRepository()
                    val viewModel = TestableReconnectionViewModel(repository)
                    
                    // Message list is empty by default
                    viewModel.messages.value.isEmpty() shouldBe true
                    
                    // Call fetchMissedMessages
                    viewModel.callFetchMissedMessagesForTest()
                    advanceUntilIdle()
                    
                    // Neither method should be called when message list is empty
                    repository.fetchActionMessagesCalled shouldBe false
                    repository.fetchNextMessagesCalled shouldBe false
                }
            }
        }
        
        test("Property 12: fetchNextMessages adds new messages to the end of the list") {
            checkAll(100, Arb.int(1, 10), Arb.int(1, 10), Arb.long(1L, 500L)) { initialCount, newCount, startId ->
                runTest {
                    val repository = ReconnectionMockRepository()
                    val viewModel = TestableReconnectionViewModel(repository)
                    
                    // Set up initial messages
                    val initialMessages = createMockMessages(initialCount, startId = startId)
                    viewModel.setMessagesForTest(initialMessages)
                    
                    val lastMessageId = initialMessages.last().id
                    
                    // Set up new messages to be returned by fetchNextMessages
                    val newMessages = createMockMessages(newCount, startId = lastMessageId + 1)
                    repository.fetchNextMessagesResult = Result.success(newMessages)
                    
                    // Call fetchMissedMessages
                    viewModel.callFetchMissedMessagesForTest()
                    advanceUntilIdle()
                    
                    // Message list should contain initial + new messages
                    val finalMessages = viewModel.messages.value
                    finalMessages.size shouldBe (initialCount + newCount)
                    
                    // New messages should be at the end
                    for (i in 0 until newCount) {
                        finalMessages[initialCount + i].id shouldBe newMessages[i].id
                    }
                }
            }
        }

        
        test("Property 12: hasMoreNewMessages is set to true when new messages are returned") {
            checkAll(100, Arb.int(1, 10), Arb.int(1, 10), Arb.long(1L, 500L)) { initialCount, newCount, startId ->
                runTest {
                    val repository = ReconnectionMockRepository()
                    val viewModel = TestableReconnectionViewModel(repository)
                    
                    // Set up initial messages
                    val initialMessages = createMockMessages(initialCount, startId = startId)
                    viewModel.setMessagesForTest(initialMessages)
                    
                    val lastMessageId = initialMessages.last().id
                    
                    // Set up new messages to be returned
                    val newMessages = createMockMessages(newCount, startId = lastMessageId + 1)
                    repository.fetchNextMessagesResult = Result.success(newMessages)
                    
                    // Call fetchMissedMessages
                    viewModel.callFetchMissedMessagesForTest()
                    advanceUntilIdle()
                    
                    // hasMoreNewMessages should be true when messages are returned
                    viewModel.hasMoreNewMessages.value shouldBe true
                }
            }
        }
        
        test("Property 12: hasMoreNewMessages is set to false when no new messages are returned") {
            checkAll(100, Arb.int(1, 10), Arb.long(1L, 500L)) { initialCount, startId ->
                runTest {
                    val repository = ReconnectionMockRepository()
                    val viewModel = TestableReconnectionViewModel(repository)
                    
                    // Set up initial messages
                    val initialMessages = createMockMessages(initialCount, startId = startId)
                    viewModel.setMessagesForTest(initialMessages)
                    
                    // No new messages returned
                    repository.fetchNextMessagesResult = Result.success(emptyList())
                    
                    // Call fetchMissedMessages
                    viewModel.callFetchMissedMessagesForTest()
                    advanceUntilIdle()
                    
                    // hasMoreNewMessages should be false when no messages are returned
                    viewModel.hasMoreNewMessages.value shouldBe false
                }
            }
        }
        
        test("Property 12: fetchNextMessages continues even if fetchActionMessages fails") {
            checkAll(100, Arb.int(1, 10), Arb.long(1L, 500L)) { initialCount, startId ->
                runTest {
                    val repository = ReconnectionMockRepository()
                    val viewModel = TestableReconnectionViewModel(repository)
                    
                    // Set up initial messages
                    val initialMessages = createMockMessages(initialCount, startId = startId)
                    viewModel.setMessagesForTest(initialMessages)
                    
                    // fetchActionMessages fails
                    repository.fetchActionMessagesResult = Result.failure(Exception("Network error"))
                    
                    // fetchNextMessages succeeds
                    val newMessages = createMockMessages(3, startId = initialMessages.last().id + 1)
                    repository.fetchNextMessagesResult = Result.success(newMessages)
                    
                    // Call fetchMissedMessages
                    viewModel.callFetchMissedMessagesForTest()
                    advanceUntilIdle()
                    
                    // fetchNextMessages should still be called
                    repository.fetchNextMessagesCalled shouldBe true
                    
                    // New messages should be added
                    viewModel.messages.value.size shouldBe (initialCount + 3)
                }
            }
        }
    }


    // ========================================
    // Property 13: Action Message Updates on Reconnection
    // ========================================
    
    context("Property 13: Action Message Updates on Reconnection") {
        
        /**
         * **Property 13: Action Message Updates on Reconnection**
         * 
         * *For any* action message returned by `fetchActionMessages()` where the `actionOn`
         * is a `BaseMessage`, the corresponding message in the list SHALL be updated with
         * the `actionOn` message data.
         * 
         * **Validates: Requirements 5.3, 5.4**
         */
        test("Property 13: Action messages update existing messages in the list") {
            checkAll(100, Arb.int(3, 15), Arb.long(1L, 500L)) { messageCount, startId ->
                runTest {
                    val repository = ReconnectionMockRepository()
                    val viewModel = TestableReconnectionViewModel(repository)
                    
                    // Set up initial messages
                    val initialMessages = createMockMessages(messageCount, startId = startId)
                    viewModel.setMessagesForTest(initialMessages)
                    
                    // Pick a message to update (middle of the list)
                    val messageToUpdateIndex = messageCount / 2
                    val messageToUpdate = initialMessages[messageToUpdateIndex]
                    
                    // Create an updated version of the message
                    val updatedMessage = TextMessage(
                        "receiver_${messageToUpdate.id}",
                        "Updated message content",
                        CometChatConstants.RECEIVER_TYPE_USER
                    ).apply {
                        this.id = messageToUpdate.id
                        this.sentAt = messageToUpdate.sentAt
                        this.sender = messageToUpdate.sender
                        this.editedAt = System.currentTimeMillis() // Mark as edited
                    }
                    
                    // Create action message with the updated message as actionOn
                    val actionMessage = createMockActionMessage(
                        actionId = initialMessages.last().id + 100,
                        actionOnMessage = updatedMessage
                    )
                    
                    repository.fetchActionMessagesResult = Result.success(listOf(actionMessage))
                    
                    // Call fetchMissedMessages
                    viewModel.callFetchMissedMessagesForTest()
                    advanceUntilIdle()
                    
                    // The message at the same index should be updated
                    val finalMessages = viewModel.messages.value
                    finalMessages.size shouldBe messageCount
                    
                    // Find the updated message by ID
                    val updatedInList = finalMessages.find { it.id == messageToUpdate.id }
                    updatedInList shouldBe updatedMessage
                }
            }
        }

        
        test("Property 13: Multiple action messages update multiple messages in the list") {
            checkAll(100, Arb.int(5, 15), Arb.long(1L, 500L)) { messageCount, startId ->
                runTest {
                    val repository = ReconnectionMockRepository()
                    val viewModel = TestableReconnectionViewModel(repository)
                    
                    // Set up initial messages
                    val initialMessages = createMockMessages(messageCount, startId = startId)
                    viewModel.setMessagesForTest(initialMessages)
                    
                    // Pick two messages to update
                    val firstIndex = 1
                    val secondIndex = messageCount - 2
                    
                    val firstMessageToUpdate = initialMessages[firstIndex]
                    val secondMessageToUpdate = initialMessages[secondIndex]
                    
                    // Create updated versions
                    val firstUpdated = TextMessage(
                        "receiver_${firstMessageToUpdate.id}",
                        "First updated content",
                        CometChatConstants.RECEIVER_TYPE_USER
                    ).apply {
                        this.id = firstMessageToUpdate.id
                        this.sentAt = firstMessageToUpdate.sentAt
                        this.sender = firstMessageToUpdate.sender
                        this.editedAt = System.currentTimeMillis()
                    }
                    
                    val secondUpdated = TextMessage(
                        "receiver_${secondMessageToUpdate.id}",
                        "Second updated content",
                        CometChatConstants.RECEIVER_TYPE_USER
                    ).apply {
                        this.id = secondMessageToUpdate.id
                        this.sentAt = secondMessageToUpdate.sentAt
                        this.sender = secondMessageToUpdate.sender
                        this.editedAt = System.currentTimeMillis()
                    }
                    
                    // Create action messages
                    val actionMessages = listOf(
                        createMockActionMessage(initialMessages.last().id + 100, firstUpdated),
                        createMockActionMessage(initialMessages.last().id + 101, secondUpdated)
                    )
                    
                    repository.fetchActionMessagesResult = Result.success(actionMessages)
                    
                    // Call fetchMissedMessages
                    viewModel.callFetchMissedMessagesForTest()
                    advanceUntilIdle()
                    
                    // Both messages should be updated
                    val finalMessages = viewModel.messages.value
                    finalMessages.size shouldBe messageCount
                    
                    val firstInList = finalMessages.find { it.id == firstMessageToUpdate.id }
                    val secondInList = finalMessages.find { it.id == secondMessageToUpdate.id }
                    
                    firstInList shouldBe firstUpdated
                    secondInList shouldBe secondUpdated
                }
            }
        }
        
        test("Property 13: Action messages with non-matching IDs do not affect the list") {
            checkAll(100, Arb.int(3, 10), Arb.long(1L, 500L)) { messageCount, startId ->
                runTest {
                    val repository = ReconnectionMockRepository()
                    val viewModel = TestableReconnectionViewModel(repository)
                    
                    // Set up initial messages
                    val initialMessages = createMockMessages(messageCount, startId = startId)
                    viewModel.setMessagesForTest(initialMessages)
                    
                    // Create an action message for a message ID that doesn't exist in the list
                    val nonExistentId = initialMessages.last().id + 1000
                    val updatedMessage = TextMessage(
                        "receiver_$nonExistentId",
                        "Updated content for non-existent message",
                        CometChatConstants.RECEIVER_TYPE_USER
                    ).apply {
                        this.id = nonExistentId
                        this.sentAt = System.currentTimeMillis()
                        this.sender = User().apply { uid = OTHER_USER_UID }
                    }
                    
                    val actionMessage = createMockActionMessage(
                        actionId = initialMessages.last().id + 100,
                        actionOnMessage = updatedMessage
                    )
                    
                    repository.fetchActionMessagesResult = Result.success(listOf(actionMessage))
                    
                    // Call fetchMissedMessages
                    viewModel.callFetchMissedMessagesForTest()
                    advanceUntilIdle()
                    
                    // List should remain unchanged (same size, same messages)
                    val finalMessages = viewModel.messages.value
                    finalMessages.size shouldBe messageCount
                    
                    // All original messages should still be there
                    for (i in 0 until messageCount) {
                        finalMessages[i].id shouldBe initialMessages[i].id
                    }
                }
            }
        }

        
        test("Property 13: Empty action messages list does not modify the message list") {
            checkAll(100, Arb.int(3, 10), Arb.long(1L, 500L)) { messageCount, startId ->
                runTest {
                    val repository = ReconnectionMockRepository()
                    val viewModel = TestableReconnectionViewModel(repository)
                    
                    // Set up initial messages
                    val initialMessages = createMockMessages(messageCount, startId = startId)
                    viewModel.setMessagesForTest(initialMessages)
                    
                    // Empty action messages
                    repository.fetchActionMessagesResult = Result.success(emptyList())
                    
                    // Call fetchMissedMessages
                    viewModel.callFetchMissedMessagesForTest()
                    advanceUntilIdle()
                    
                    // List should remain unchanged
                    val finalMessages = viewModel.messages.value
                    finalMessages.size shouldBe messageCount
                    
                    for (i in 0 until messageCount) {
                        finalMessages[i].id shouldBe initialMessages[i].id
                    }
                }
            }
        }
        
        test("Property 13: Action messages preserve list order after updates") {
            checkAll(100, Arb.int(5, 15), Arb.long(1L, 500L)) { messageCount, startId ->
                runTest {
                    val repository = ReconnectionMockRepository()
                    val viewModel = TestableReconnectionViewModel(repository)
                    
                    // Set up initial messages
                    val initialMessages = createMockMessages(messageCount, startId = startId)
                    viewModel.setMessagesForTest(initialMessages)
                    
                    // Update the first message
                    val messageToUpdate = initialMessages[0]
                    val updatedMessage = TextMessage(
                        "receiver_${messageToUpdate.id}",
                        "Updated first message",
                        CometChatConstants.RECEIVER_TYPE_USER
                    ).apply {
                        this.id = messageToUpdate.id
                        this.sentAt = messageToUpdate.sentAt
                        this.sender = messageToUpdate.sender
                        this.editedAt = System.currentTimeMillis()
                    }
                    
                    val actionMessage = createMockActionMessage(
                        actionId = initialMessages.last().id + 100,
                        actionOnMessage = updatedMessage
                    )
                    
                    repository.fetchActionMessagesResult = Result.success(listOf(actionMessage))
                    
                    // Call fetchMissedMessages
                    viewModel.callFetchMissedMessagesForTest()
                    advanceUntilIdle()
                    
                    // List order should be preserved
                    val finalMessages = viewModel.messages.value
                    finalMessages.size shouldBe messageCount
                    
                    // IDs should be in the same order
                    for (i in 0 until messageCount) {
                        finalMessages[i].id shouldBe initialMessages[i].id
                    }
                    
                    // First message should be updated
                    finalMessages[0] shouldBe updatedMessage
                }
            }
        }
        
        test("Property 13: Action messages with non-ACTION category are ignored") {
            checkAll(100, Arb.int(3, 10), Arb.long(1L, 500L)) { messageCount, startId ->
                runTest {
                    val repository = ReconnectionMockRepository()
                    val viewModel = TestableReconnectionViewModel(repository)
                    
                    // Set up initial messages
                    val initialMessages = createMockMessages(messageCount, startId = startId)
                    viewModel.setMessagesForTest(initialMessages)
                    
                    // Create a regular text message (not an action message)
                    val regularMessage = TextMessage(
                        "receiver",
                        "Regular message",
                        CometChatConstants.RECEIVER_TYPE_USER
                    ).apply {
                        this.id = initialMessages.last().id + 100
                        this.category = CometChatConstants.CATEGORY_MESSAGE // Not ACTION
                        this.sentAt = System.currentTimeMillis()
                        this.sender = User().apply { uid = OTHER_USER_UID }
                    }
                    
                    repository.fetchActionMessagesResult = Result.success(listOf(regularMessage))
                    
                    // Call fetchMissedMessages
                    viewModel.callFetchMissedMessagesForTest()
                    advanceUntilIdle()
                    
                    // List should remain unchanged (non-ACTION messages are ignored)
                    val finalMessages = viewModel.messages.value
                    finalMessages.size shouldBe messageCount
                    
                    for (i in 0 until messageCount) {
                        finalMessages[i].id shouldBe initialMessages[i].id
                    }
                }
            }
        }
    }
})
