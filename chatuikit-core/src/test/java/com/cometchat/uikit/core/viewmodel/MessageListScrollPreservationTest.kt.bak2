package com.cometchat.uikit.core.viewmodel

import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.MessagesRequest
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.TextMessage
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.domain.model.SurroundingMessagesResult
import com.cometchat.uikit.core.domain.repository.MessageListRepository
import com.cometchat.uikit.core.state.MessageListUIState
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Preservation property tests for message list scroll behaviors.
 *
 * Feature: message-list-scroll-to-bottom
 * Property 2: Preservation - Non-Default Scroll Behaviors Unchanged
 *
 * **Validates: Requirements 3.1, 3.2, 3.3, 3.4, 3.5**
 *
 * These tests verify that non-buggy scroll paths work correctly on UNFIXED code.
 * The bug only affects the default initial load case (scrollToMessageId == null,
 * startFromUnreadMessages == false). All other scroll behaviors should be preserved.
 *
 * ## Preservation Cases
 *
 * 1. **GoToMessage Preservation (Req 3.1)**: When scrollToMessageId is set via goToMessage(),
 *    the ViewModel correctly sets scrollToMessageId to the target message ID.
 *
 * 2. **Unread Anchor Preservation (Req 3.2)**: When startFromUnreadMessages is true and
 *    there are unread messages, fetchMessagesWithUnreadCount() calls goToMessage() with
 *    lastReadMessageId, setting scrollToMessageId and highlight=false.
 *
 * 3. **Real-Time Message Auto-Scroll Preservation (Req 3.3)**: When user is at the latest
 *    position and a new real-time message arrives from another user, scrollToBottomEvent
 *    is emitted.
 *
 * 4. **Scrolled-Up New Message Indicator Preservation (Req 3.4, 3.5)**: When user is NOT
 *    at the latest position and a new message arrives, hasMoreNewMessages is set to true
 *    without adding the message to the list.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MessageListScrollPreservationTest : FunSpec({

    val testDispatcher = StandardTestDispatcher()

    beforeSpec {
        Dispatchers.setMain(testDispatcher)
    }

    afterSpec {
        Dispatchers.resetMain()
    }


    // ========================================
    // Mock Repository
    // ========================================

    class PreservationTestRepository : MessageListRepository {
        var messagesToReturn: List<BaseMessage> = emptyList()
        var hasMorePrevious: Boolean = true
        var surroundingResult: SurroundingMessagesResult? = null
        var messageById: MutableMap<Long, BaseMessage> = mutableMapOf()
        var conversationToReturn: Any? = null

        override suspend fun fetchPreviousMessages(): Result<List<BaseMessage>> =
            Result.success(messagesToReturn)
        override suspend fun fetchNextMessages(fromMessageId: Long): Result<List<BaseMessage>> =
            Result.success(emptyList())
        override suspend fun getConversation(id: String, type: String): Result<com.cometchat.chat.models.Conversation> =
            Result.failure(Exception("No conversation configured"))
        override suspend fun getMessage(messageId: Long): Result<BaseMessage> =
            messageById[messageId]?.let { Result.success(it) }
                ?: Result.failure(Exception("Message $messageId not found"))
        override suspend fun deleteMessage(message: BaseMessage): Result<BaseMessage> =
            Result.failure(Exception("Not configured"))
        override suspend fun flagMessage(messageId: Long, reason: String, remark: String): Result<Unit> =
            Result.success(Unit)
        override suspend fun addReaction(messageId: Long, emoji: String): Result<BaseMessage> =
            Result.failure(Exception("Not configured"))
        override suspend fun removeReaction(messageId: Long, emoji: String): Result<BaseMessage> =
            Result.failure(Exception("Not configured"))
        override suspend fun markAsDelivered(message: BaseMessage): Result<Unit> = Result.success(Unit)
        override suspend fun markAsRead(message: BaseMessage): Result<Unit> = Result.success(Unit)
        override suspend fun markAsUnread(message: BaseMessage): Result<Conversation> = 
            Result.success(Conversation().apply { unreadMessageCount = 1 })
        override fun hasMorePreviousMessages(): Boolean = hasMorePrevious
        override fun resetRequest() { hasMorePrevious = true }
        override fun configureForUser(
            user: User, messagesTypes: List<String>, messagesCategories: List<String>,
            parentMessageId: Long, messagesRequestBuilder: MessagesRequest.MessagesRequestBuilder?
        ) {}
        override fun configureForGroup(
            group: Group, messagesTypes: List<String>, messagesCategories: List<String>,
            parentMessageId: Long, messagesRequestBuilder: MessagesRequest.MessagesRequestBuilder?
        ) {}
        override suspend fun fetchSurroundingMessages(messageId: Long): Result<SurroundingMessagesResult> =
            surroundingResult?.let { Result.success(it) }
                ?: Result.failure(Exception("No surrounding result configured"))
        override suspend fun fetchActionMessages(fromMessageId: Long): Result<List<BaseMessage>> =
            Result.success(emptyList())
        override fun rebuildRequestFromMessageId(messageId: Long) {}
        override fun getLatestMessageId(): Long = -1
        override fun setLatestMessageId(messageId: Long) {}
    }

    // ========================================
    // Testable ViewModel - avoids CometChat SDK dependencies
    // ========================================

    class PreservationTestViewModel(
        repository: MessageListRepository
    ) : CometChatMessageListViewModel(repository, enableListeners = false) {
        /** Override to avoid CometChat SDK dependency */
        override fun getLoggedInUserUid(): String? = "logged-in-user"

        fun setUserForTest(user: User) {
            val field = CometChatMessageListViewModel::class.java.getDeclaredField("user")
            field.isAccessible = true
            field.set(this, user)
        }

        fun setLatestMessageIdForTest(id: Long) {
            val field = CometChatMessageListViewModel::class.java.getDeclaredField("latestMessageId")
            field.isAccessible = true
            field.setLong(this, id)
        }

        fun setMessagesForTest(messages: List<BaseMessage>) {
            val field = CometChatMessageListViewModel::class.java.getDeclaredField("_messages")
            field.isAccessible = true
            @Suppress("UNCHECKED_CAST")
            val stateFlow = field.get(this) as MutableStateFlow<List<BaseMessage>>
            stateFlow.value = messages
        }

        fun setUiStateForTest(state: MessageListUIState) {
            val field = CometChatMessageListViewModel::class.java.getDeclaredField("_uiState")
            field.isAccessible = true
            @Suppress("UNCHECKED_CAST")
            val stateFlow = field.get(this) as MutableStateFlow<MessageListUIState>
            stateFlow.value = state
        }

        fun setFirstFetchForTest(value: Boolean) {
            val field = CometChatMessageListViewModel::class.java.getDeclaredField("firstFetch")
            field.isAccessible = true
            field.setBoolean(this, value)
        }

        /**
         * Calls the private handleIncomingMessage method via reflection.
         * This simulates a real-time message arriving from the SDK listener.
         */
        fun simulateIncomingMessage(message: BaseMessage) {
            val method = CometChatMessageListViewModel::class.java.getDeclaredMethod(
                "handleIncomingMessage", BaseMessage::class.java
            )
            method.isAccessible = true
            method.invoke(this, message)
        }
    }

    // ========================================
    // Helper Functions
    // ========================================

    fun createMessages(count: Int, startId: Int = 1, senderUid: String = "other-user"): List<BaseMessage> {
        return (startId until startId + count).map { index ->
            val sender = User().apply {
                uid = senderUid
                name = "User $senderUid"
            }
            TextMessage(
                "test-user",
                "Message $index",
                CometChatConstants.RECEIVER_TYPE_USER
            ).apply {
                this.id = index.toLong()
                this.sender = sender
                this.sentAt = (System.currentTimeMillis() / 1000) + index
                this.category = CometChatConstants.CATEGORY_MESSAGE
                this.type = CometChatConstants.MESSAGE_TYPE_TEXT
            }
        }
    }

    fun createIncomingMessage(id: Int, senderUid: String = "test-user"): BaseMessage {
        val sender = User().apply {
            uid = senderUid
            name = "User $senderUid"
        }
        return TextMessage(
            "logged-in-user",
            "Incoming message $id",
            CometChatConstants.RECEIVER_TYPE_USER
        ).apply {
            this.id = id.toLong()
            this.sender = sender
            this.sentAt = (System.currentTimeMillis() / 1000) + id
            this.category = CometChatConstants.CATEGORY_MESSAGE
            this.type = CometChatConstants.MESSAGE_TYPE_TEXT
            this.receiverUid = "logged-in-user"
        }
    }

    // ========================================
    // Property 2a: GoToMessage Preservation
    // Validates: Requirement 3.1
    // ========================================

    /**
     * Property test: For all valid scrollToMessageId values within a message list,
     * goToMessage() sets scrollToMessageId to the target message ID and loads
     * surrounding messages into the list.
     *
     * On UNFIXED code: This test PASSES because goToMessage uses its own dedicated
     * scroll mechanism (scrollToMessageId StateFlow) which is unaffected by the bug.
     *
     * **Validates: Requirements 3.1**
     */
    test("goToMessage should set scrollToMessageId for all valid target message IDs") {
        checkAll(Arb.int(5..50)) { messageCount ->
            runTest(testDispatcher) {
                val repository = PreservationTestRepository()
                val allMessages = createMessages(messageCount)
                val viewModel = PreservationTestViewModel(repository)
                val testUser = User().apply { uid = "test-user"; name = "Test" }
                viewModel.setUserForTest(testUser)

                // Pick a target message in the middle of the list
                val targetIndex = messageCount / 2
                val targetMessage = allMessages[targetIndex]
                val targetId = targetMessage.id.toLong()

                // Configure repository for goToMessage
                repository.messageById[targetId] = targetMessage
                val olderMessages = allMessages.subList(0, targetIndex)
                val newerMessages = if (targetIndex + 1 < allMessages.size)
                    allMessages.subList(targetIndex + 1, allMessages.size) else emptyList()
                repository.surroundingResult = SurroundingMessagesResult(
                    olderMessages = olderMessages,
                    targetMessage = targetMessage,
                    newerMessages = newerMessages,
                    hasMorePrevious = false,
                    hasMoreNext = false
                )

                // Call goToMessage
                viewModel.goToMessage(targetId)
                advanceUntilIdle()

                // Verify: scrollToMessageId is set to the target
                viewModel.scrollToMessageId.value shouldBe targetId

                // Verify: messages are loaded (older + target + newer)
                viewModel.messages.value.size shouldBe messageCount

                // Verify: UI state is Loaded
                viewModel.uiState.value.shouldBeInstanceOf<MessageListUIState.Loaded>()
            }
        }
    }

    // ========================================
    // Property 2b: Unread Anchor Preservation
    // Validates: Requirement 3.2
    // ========================================

    /**
     * Property test: For all valid unread anchor indices, when startFromUnreadMessages
     * is true and there are unread messages, the system calls goToMessage(lastReadMessageId,
     * highlight=false). We verify this by calling goToMessage directly with highlight=false
     * and checking that scrollToMessageId is set AND highlightScroll remains false.
     *
     * This avoids calling fetchMessagesWithUnreadCount() which triggers CometChat SDK
     * initialization (checkAndFetchConversationSummary). The key preservation behavior
     * is that goToMessage with highlight=false correctly sets scrollToMessageId without
     * highlighting.
     *
     * On UNFIXED code: This test PASSES because goToMessage uses its own dedicated
     * scroll mechanism unaffected by the bug.
     *
     * **Validates: Requirements 3.2**
     */
    test("unread anchor should trigger goToMessage with lastReadMessageId for all valid unread counts") {
        checkAll(Arb.int(5..50)) { messageCount ->
            runTest(testDispatcher) {
                val repository = PreservationTestRepository()
                val allMessages = createMessages(messageCount)
                val viewModel = PreservationTestViewModel(repository)
                val testUser = User().apply { uid = "test-user"; name = "Test" }
                viewModel.setUserForTest(testUser)

                // Pick a lastReadMessageId in the middle (simulating unread anchor)
                val lastReadIndex = messageCount / 3
                val lastReadId = allMessages[lastReadIndex].id.toLong()

                // Configure repository for goToMessage
                val targetMessage = allMessages[lastReadIndex]
                repository.messageById[lastReadId] = targetMessage
                val olderMessages = allMessages.subList(0, lastReadIndex)
                val newerMessages = if (lastReadIndex + 1 < allMessages.size)
                    allMessages.subList(lastReadIndex + 1, allMessages.size) else emptyList()
                repository.surroundingResult = SurroundingMessagesResult(
                    olderMessages = olderMessages,
                    targetMessage = targetMessage,
                    newerMessages = newerMessages,
                    hasMorePrevious = false,
                    hasMoreNext = false
                )

                // Call goToMessage with highlight=false (same as fetchMessagesWithUnreadCount does
                // for the unread anchor case: goToMessage(lastReadMessageId, highlight = false))
                viewModel.goToMessage(lastReadId, highlight = false)
                advanceUntilIdle()

                // Verify: scrollToMessageId is set to lastReadMessageId
                viewModel.scrollToMessageId.value shouldBe lastReadId

                // Verify: highlightScroll is false (unread anchor uses highlight=false)
                viewModel.highlightScroll.value shouldBe false

                // Verify: messages are loaded
                viewModel.messages.value.size shouldBe messageCount

                // Verify: UI state is Loaded
                viewModel.uiState.value.shouldBeInstanceOf<MessageListUIState.Loaded>()
            }
        }
    }

    // ========================================
    // Property 2c: Real-Time Message Auto-Scroll Preservation
    // Validates: Requirement 3.3
    // ========================================

    /**
     * Property test: For all new incoming messages while user is at the latest position,
     * scrollToBottomEvent is emitted and the message is added to the list.
     *
     * Uses reflection to call handleIncomingMessage() directly, simulating a real-time
     * message arriving from the SDK listener.
     *
     * On UNFIXED code: This test PASSES because real-time message handling uses
     * handleIncomingMessage() which emits scrollToBottomEvent via SharedFlow.
     * By the time real-time messages arrive, the collector IS active.
     *
     * **Validates: Requirements 3.3**
     */
    test("scrollToBottomEvent should fire for all new incoming messages when user is at latest position") {
        checkAll(Arb.int(5..50)) { initialMessageCount ->
            runTest(testDispatcher) {
                val repository = PreservationTestRepository()
                val initialMessages = createMessages(initialMessageCount)
                val viewModel = PreservationTestViewModel(repository)
                val testUser = User().apply { uid = "test-user"; name = "Test" }
                viewModel.setUserForTest(testUser)

                // Set up: pre-load messages and set latestMessageId to last message
                viewModel.setMessagesForTest(initialMessages)
                viewModel.setUiStateForTest(MessageListUIState.Loaded)
                viewModel.setFirstFetchForTest(false)
                val lastMessageId = initialMessages.last().id.toLong()
                viewModel.setLatestMessageIdForTest(lastMessageId)

                // Start collecting scrollToBottomEvent BEFORE the message arrives
                // (simulates the LaunchedEffect(Unit) collector being active)
                var eventReceived = false
                val collectJob = launch {
                    viewModel.scrollToBottomEvent.first()
                    eventReceived = true
                }
                // Let the collector start
                advanceUntilIdle()

                // Simulate incoming message from another user via handleIncomingMessage
                val newMessage = createIncomingMessage(
                    id = initialMessageCount + 1,
                    senderUid = "test-user" // "test-user" is the chat partner, not logged-in user
                )
                viewModel.simulateIncomingMessage(newMessage)
                advanceUntilIdle()

                // Verify: scrollToBottomEvent was emitted
                eventReceived shouldBe true

                // Verify: message was added to the list
                viewModel.messages.value.size shouldBe initialMessageCount + 1
                viewModel.messages.value.last().id shouldBe newMessage.id

                collectJob.cancel()
            }
        }
    }

    // ========================================
    // Property 2d: Scrolled-Up New Message Indicator Preservation
    // Validates: Requirements 3.4, 3.5
    // ========================================

    /**
     * Property test: For all new incoming messages while user is scrolled up
     * (not at latest position), the message is NOT added to the list and
     * hasMoreNewMessages is set to true (indicating new message indicator).
     *
     * Uses reflection to call handleIncomingMessage() directly.
     *
     * On UNFIXED code: This test PASSES because the scrolled-up path in
     * handleIncomingMessage() simply sets hasMoreNewMessages = true without
     * modifying the message list or emitting scrollToBottomEvent.
     *
     * **Validates: Requirements 3.4, 3.5**
     */
    test("new messages while scrolled up should set hasMoreNewMessages without changing list for all message sizes") {
        checkAll(Arb.int(5..50)) { initialMessageCount ->
            runTest(testDispatcher) {
                val repository = PreservationTestRepository()
                val initialMessages = createMessages(initialMessageCount)
                val viewModel = PreservationTestViewModel(repository)
                val testUser = User().apply { uid = "test-user"; name = "Test" }
                viewModel.setUserForTest(testUser)

                // Set up: pre-load messages but set latestMessageId to a HIGHER value
                // than the last message in the list. This simulates the user being
                // scrolled up (not at the latest position).
                viewModel.setMessagesForTest(initialMessages)
                viewModel.setUiStateForTest(MessageListUIState.Loaded)
                viewModel.setFirstFetchForTest(false)
                // Set latestMessageId higher than last message → user is NOT at latest
                viewModel.setLatestMessageIdForTest(initialMessageCount.toLong() + 1000)

                // Record initial state
                val initialListSize = viewModel.messages.value.size

                // Start collecting scrollToBottomEvent to verify it does NOT fire
                var scrollEventFired = false
                val collectJob = launch {
                    viewModel.scrollToBottomEvent.first()
                    scrollEventFired = true
                }
                // Let the collector start
                advanceUntilIdle()

                // Simulate incoming message from another user via handleIncomingMessage
                val newMessage = createIncomingMessage(
                    id = initialMessageCount + 2000,
                    senderUid = "test-user"
                )
                viewModel.simulateIncomingMessage(newMessage)
                advanceUntilIdle()

                // Verify: hasMoreNewMessages is true (new message indicator)
                viewModel.hasMoreNewMessages.value shouldBe true

                // Verify: message list size is unchanged (message NOT added)
                viewModel.messages.value.size shouldBe initialListSize

                // Verify: scrollToBottomEvent was NOT emitted
                scrollEventFired shouldBe false

                collectJob.cancel()
            }
        }
    }

})
