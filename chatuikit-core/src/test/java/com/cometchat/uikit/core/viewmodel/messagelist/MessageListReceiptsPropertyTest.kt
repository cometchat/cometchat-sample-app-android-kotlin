package com.cometchat.uikit.core.viewmodel.messagelist

import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Conversation
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.domain.repository.MessageListRepository
import com.cometchat.uikit.core.state.MessageListUIState
import com.cometchat.uikit.core.testutils.MockFactory
import com.cometchat.uikit.core.viewmodel.CometChatMessageListViewModel
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.mockito.MockedStatic
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.clearInvocations
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Layer 5G — Receipts Property-Based Tests.
 *
 * Tests the CometChatMessageListViewModel's receipt handling:
 * - disableReceipt=true prevents markAsRead
 * - Own messages never marked as read
 * - Already-read messages (readAt > 0) not re-marked
 * - markAsDelivered for incoming messages, skipped when disabled/own
 * - messageReadEvent emitted on success
 * - markConversationRead resets unreadCount
 *
 * Architecture:
 * - Mocks the MessageListRepository interface (layer directly below)
 * - Uses `enableListeners = false` to avoid SDK dependencies
 * - Uses UnconfinedTestDispatcher + Dispatchers.setMain/resetMain
 * - All PBT tests use `checkAll` with `Arb` generators
 *
 * Run with:
 *   ./gradlew :chatuikit-core:testDebugUnitTest --tests "*MessageListReceiptsPropertyTest"
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MessageListReceiptsPropertyTest : FunSpec({

    isolationMode = IsolationMode.SingleInstance

    val testDispatcher = UnconfinedTestDispatcher()

    lateinit var repository: MessageListRepository
    lateinit var cometChatMock: MockedStatic<CometChat>

    /**
     * Creates a ViewModel configured with a user and pre-loaded messages.
     * Uses gotoMessageId=1 to prevent markConversationRead from being called
     * during fetchMessages() first-fetch logic, avoiding CometChatEvents emissions
     * that leak coroutines via Dispatchers.Default between tests.
     */
    suspend fun createViewModelWithMessages(
        messages: List<BaseMessage>,
        disableReceipt: Boolean = false
    ): CometChatMessageListViewModel {
        whenever(repository.fetchPreviousMessages()).thenReturn(Result.success(messages))
        whenever(repository.hasMorePreviousMessages()).thenReturn(true)
        val vm = CometChatMessageListViewModel(
            repository = repository,
            enableListeners = false
        )
        val user = MockFactory.createUser(uid = "test-user", name = "Test User")
        // gotoMessageId=1 prevents markConversationRead from firing on first fetch
        // (condition: gotoMessageId <= 0 || firstUnreadMessage != null)
        // Since lastReadMessageId defaults to -1, firstUnreadMessage is always null
        vm.setUser(user, gotoMessageId = 1L)
        vm.setDisableReceipt(disableReceipt)
        vm.fetchMessages()
        return vm
    }

    beforeSpec {
        Dispatchers.setMain(testDispatcher)
    }

    afterSpec {
        Thread.sleep(50)
        Dispatchers.resetMain()
    }

    beforeTest {
        repository = mock()
        // Mock CometChat.getLoggedInUser() to return a user with uid "test-user"
        cometChatMock = Mockito.mockStatic(CometChat::class.java)
        val loggedInUser = mock<User>()
        whenever(loggedInUser.uid).thenReturn("test-user")
        cometChatMock.`when`<User?> { CometChat.getLoggedInUser() }.thenReturn(loggedInUser)
        // Default stubs
        whenever(repository.hasMorePreviousMessages()).thenReturn(true)
        whenever(repository.fetchPreviousMessages()).thenReturn(Result.success(emptyList()))
        whenever(repository.markAsRead(any())).thenReturn(Result.success(Unit))
        whenever(repository.markAsDelivered(any())).thenReturn(Result.success(Unit))
        println("  🧪 ${it.name.testName}")
    }

    afterTest {
        cometChatMock.close()
        println()
    }

    test("warmup - absorb leaked exceptions") {
        try { runTest(testDispatcher) { } } catch (_: Exception) { }
    }


    // ==================== 12.2 PBT: disableReceipt=true prevents markAsRead ====================

    test("PBT: disableReceipt=true prevents markLastMessageAsRead from calling repository") {
        checkAll(20, Arb.long(1L..1000L)) { messageId ->
            runTest(testDispatcher) {
                val message = MockFactory.createTextMessage(
                    id = messageId,
                    text = "Incoming message",
                    senderUid = "other-user",
                    receiverId = "test-user",
                    readAt = 0L
                )

                val vm = createViewModelWithMessages(listOf(message as BaseMessage), disableReceipt = true)
                advanceUntilIdle()

                vm.markLastMessageAsRead(message)
                advanceUntilIdle()

                // markAsRead should NOT be called on repository
                verify(repository, never()).markAsRead(any())
                println("    → messageId=$messageId: markAsRead blocked by disableReceipt=true")
            }
        }
        println("    ✅ PBT: disableReceipt=true prevents markAsRead")
    }

    test("PBT: disableReceipt=false allows markLastMessageAsRead to call repository") {
        checkAll(20, Arb.long(1L..1000L)) { messageId ->
            runTest(testDispatcher) {
                val message = MockFactory.createTextMessage(
                    id = messageId,
                    text = "Incoming message",
                    senderUid = "other-user",
                    receiverId = "test-user",
                    readAt = 0L
                )

                val vm = createViewModelWithMessages(listOf(message as BaseMessage), disableReceipt = false)
                advanceUntilIdle()

                // Clear invocations from fetchMessages() -> markConversationRead() auto-call
                clearInvocations(repository)

                vm.markLastMessageAsRead(message)
                advanceUntilIdle()

                // markAsRead SHOULD be called exactly once by markLastMessageAsRead
                verify(repository).markAsRead(any())
                println("    → messageId=$messageId: markAsRead allowed with disableReceipt=false")
            }
        }
        println("    ✅ PBT: disableReceipt=false allows markAsRead")
    }


    // ==================== 12.3 PBT: own messages never marked as read ====================

    test("PBT: own messages (sender=loggedInUser) are never marked as read") {
        checkAll(20, Arb.long(1L..1000L)) { messageId ->
            runTest(testDispatcher) {
                // Message sent by the logged-in user (test-user)
                val message = MockFactory.createTextMessage(
                    id = messageId,
                    text = "My own message",
                    senderUid = "test-user",  // Same as logged-in user
                    receiverId = "other-user",
                    readAt = 0L
                )

                val vm = createViewModelWithMessages(listOf(message as BaseMessage), disableReceipt = false)
                advanceUntilIdle()

                // Clear invocations from fetchMessages() -> markConversationRead() auto-call
                clearInvocations(repository)

                vm.markLastMessageAsRead(message)
                advanceUntilIdle()

                // markAsRead should NOT be called for own messages
                verify(repository, never()).markAsRead(any())
                println("    → messageId=$messageId: own message not marked as read")
            }
        }
        println("    ✅ PBT: own messages never marked as read")
    }


    // ==================== 12.4 PBT: already-read messages (readAt > 0) not re-marked ====================

    test("PBT: already-read messages (readAt > 0) are not re-marked") {
        checkAll(20, Arb.long(1L..1000L), Arb.long(1L..2000000000L)) { messageId, readAt ->
            runTest(testDispatcher) {
                // Message already has readAt > 0
                val message = MockFactory.createTextMessage(
                    id = messageId,
                    text = "Already read message",
                    senderUid = "other-user",
                    receiverId = "test-user",
                    readAt = readAt  // Already read
                )

                val vm = createViewModelWithMessages(listOf(message as BaseMessage), disableReceipt = false)
                advanceUntilIdle()

                // Clear invocations from fetchMessages() -> markConversationRead() auto-call
                clearInvocations(repository)

                vm.markLastMessageAsRead(message)
                advanceUntilIdle()

                // markAsRead should NOT be called for already-read messages
                verify(repository, never()).markAsRead(any())
                println("    → messageId=$messageId, readAt=$readAt: already-read message not re-marked")
            }
        }
        println("    ✅ PBT: already-read messages not re-marked")
    }


    // ==================== 12.5 markAsDelivered for incoming messages, skipped when disabled/own ====================

    test("PBT: markAsDelivered skipped when disableReceipt=true") {
        checkAll(20, Arb.long(1L..1000L)) { messageId ->
            runTest(testDispatcher) {
                val message = MockFactory.createTextMessage(
                    id = messageId,
                    text = "Incoming",
                    senderUid = "other-user",
                    receiverId = "test-user"
                )

                val vm = createViewModelWithMessages(listOf(message as BaseMessage), disableReceipt = true)
                advanceUntilIdle()

                vm.markAsDelivered(message)
                advanceUntilIdle()

                verify(repository, never()).markAsDelivered(any())
                println("    → messageId=$messageId: markAsDelivered blocked by disableReceipt=true")
            }
        }
        println("    ✅ PBT: markAsDelivered skipped when disableReceipt=true")
    }

    test("PBT: markAsDelivered skipped for own messages") {
        checkAll(20, Arb.long(1L..1000L)) { messageId ->
            runTest(testDispatcher) {
                // Message sent by logged-in user
                val message = MockFactory.createTextMessage(
                    id = messageId,
                    text = "My message",
                    senderUid = "test-user",  // Same as logged-in user
                    receiverId = "other-user"
                )

                val vm = createViewModelWithMessages(listOf(message as BaseMessage), disableReceipt = false)
                advanceUntilIdle()

                // Clear invocations from fetchMessages() auto-calls
                clearInvocations(repository)

                vm.markAsDelivered(message)
                advanceUntilIdle()

                verify(repository, never()).markAsDelivered(any())
                println("    → messageId=$messageId: markAsDelivered skipped for own message")
            }
        }
        println("    ✅ PBT: markAsDelivered skipped for own messages")
    }

    test("markAsDelivered calls repository for valid incoming message") {
        runTest(testDispatcher) {
            val message = MockFactory.createTextMessage(
                id = 1L,
                text = "Incoming",
                senderUid = "other-user",
                receiverId = "test-user"
            )

            val vm = createViewModelWithMessages(listOf(message as BaseMessage), disableReceipt = false)
            advanceUntilIdle()

            // Clear invocations from fetchMessages() auto-calls
            clearInvocations(repository)

            vm.markAsDelivered(message)
            advanceUntilIdle()

            verify(repository).markAsDelivered(any())
            println("    ✅ markAsDelivered calls repository for valid incoming message")
        }
    }


    // ==================== 12.6 messageReadEvent emitted on success ====================

    test("markLastMessageAsRead emits messageReadEvent on success") {
        runTest(testDispatcher) {
            val message = MockFactory.createTextMessage(
                id = 1L,
                text = "Incoming",
                senderUid = "other-user",
                receiverId = "test-user",
                readAt = 0L
            )

            whenever(repository.markAsRead(any())).thenReturn(Result.success(Unit))

            val vm = createViewModelWithMessages(listOf(message as BaseMessage), disableReceipt = false)
            advanceUntilIdle()

            // Clear invocations from fetchMessages() auto-calls
            clearInvocations(repository)

            // Collect messageReadEvent
            var emittedMessage: BaseMessage? = null
            val job = launch(testDispatcher) {
                vm.messageReadEvent.collect { emittedMessage = it }
            }

            vm.markLastMessageAsRead(message)
            advanceUntilIdle()

            emittedMessage shouldBe message
            job.cancel()
            println("    ✅ messageReadEvent emitted on successful markAsRead")
        }
    }

    test("markLastMessageAsRead does NOT emit messageReadEvent when disableReceipt=true") {
        runTest(testDispatcher) {
            val message = MockFactory.createTextMessage(
                id = 1L,
                text = "Incoming",
                senderUid = "other-user",
                receiverId = "test-user",
                readAt = 0L
            )

            val vm = createViewModelWithMessages(listOf(message as BaseMessage), disableReceipt = true)
            advanceUntilIdle()

            var emittedMessage: BaseMessage? = null
            val job = launch(testDispatcher) {
                vm.messageReadEvent.collect { emittedMessage = it }
            }

            vm.markLastMessageAsRead(message)
            advanceUntilIdle()

            emittedMessage shouldBe null
            job.cancel()
            println("    ✅ messageReadEvent NOT emitted when disableReceipt=true")
        }
    }


    // ==================== 12.7 markConversationRead resets unreadCount ====================

    test("markConversationRead resets unreadCount to 0") {
        runTest(testDispatcher) {
            val messages = MockFactory.createMessages(5, startId = 1L, senderUid = "other-user", receiverId = "test-user")

            // Set up conversation with unread count
            val conversation = MockFactory.createConversation(
                unreadCount = 10,
                lastReadMessageId = 1L,
                latestMessageId = 5L
            )
            whenever(conversation.latestMessageId).thenReturn(5L)
            whenever(repository.getConversation(any(), any())).thenReturn(Result.success(conversation))
            whenever(repository.markAsRead(any())).thenReturn(Result.success(Unit))

            val vm = createViewModelWithMessages(messages.map { it as BaseMessage }, disableReceipt = false)
            advanceUntilIdle()

            // Clear gotoMessageId so fetchMessagesWithUnreadCount doesn't try to goToMessage
            vm.clearGoToMessageId()

            // Simulate having unread count set
            vm.fetchMessagesWithUnreadCount()
            advanceUntilIdle()

            // Clear invocations from setup
            clearInvocations(repository)

            // Now mark conversation as read
            vm.markConversationRead()
            advanceUntilIdle()

            // unreadCount should be reset to 0
            vm.unreadCount.value shouldBe 0
            println("    ✅ markConversationRead resets unreadCount to 0")
        }
    }

    test("markConversationRead does nothing when message list is empty") {
        runTest(testDispatcher) {
            val vm = createViewModelWithMessages(emptyList(), disableReceipt = false)
            advanceUntilIdle()

            vm.markConversationRead()
            advanceUntilIdle()

            // Should not crash, unreadCount remains 0
            vm.unreadCount.value shouldBe 0
            verify(repository, never()).markAsRead(any())
            println("    ✅ markConversationRead does nothing when list is empty")
        }
    }
})
