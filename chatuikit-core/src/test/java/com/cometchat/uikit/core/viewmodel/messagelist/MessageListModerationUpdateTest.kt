package com.cometchat.uikit.core.viewmodel.messagelist

import android.util.Log
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.enums.ModerationStatus
import com.cometchat.chat.models.BaseMessage
import com.cometchat.uikit.core.domain.repository.MessageListRepository
import com.cometchat.uikit.core.testutils.MockFactory
import com.cometchat.uikit.core.viewmodel.CometChatMessageListViewModel
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
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
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * ENG-37020 — real-time moderation updates in [CometChatMessageListViewModel].
 *
 * The bug: a moderation verdict pushed by the server (via
 * `CometChat.MessageListener.onMessageModerated`) was ignored, so a blocked
 * message stayed visible until the user left and re-entered the conversation.
 * The fix routes the verdict through `updateModeratedMessage`, which swaps the
 * matching message (by muid, falling back to id) and re-emits it through both
 * `_messages` and `messageUpdated`.
 *
 * The listener is captured from the mocked static `CometChat.addMessageListener`
 * (the VM registers it in setUser when enableListeners=true), then driven
 * directly — exactly what the SDK does on a moderation event.
 *
 * Categories:
 * A. Verdict application — match by id, match by muid, list swap + messageUpdated emission
 * B. Sticky DISAPPROVED — a later verdict never re-enables a blocked message
 * C. Gates — unknown message ignored, other-conversation message ignored
 *
 * Run: ./gradlew :chatuikit-core:testDebugUnitTest --tests "*MessageListModerationUpdateTest"
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MessageListModerationUpdateTest : FunSpec({

    val testDispatcher = UnconfinedTestDispatcher()

    lateinit var repository: MessageListRepository
    lateinit var logMock: MockedStatic<Log>
    lateinit var cometChatMock: MockedStatic<CometChat>

    /**
     * Builds a VM for a 1:1 chat with [messages] pre-loaded and returns it together
     * with the SDK message listener the VM registered.
     */
    suspend fun createViewModelWithListener(
        messages: List<BaseMessage>
    ): Pair<CometChatMessageListViewModel, CometChat.MessageListener> {
        whenever(repository.fetchPreviousMessages()).thenReturn(Result.success(messages))
        whenever(repository.hasMorePreviousMessages()).thenReturn(true)

        val vm = CometChatMessageListViewModel(repository = repository, enableListeners = true)
        vm.setUser(MockFactory.createUser(uid = "test-user", name = "Test User"))
        vm.fetchMessages()

        val listenerCaptor = argumentCaptor<CometChat.MessageListener>()
        cometChatMock.verify({ CometChat.addMessageListener(any(), listenerCaptor.capture()) }, atLeastOnce())
        return vm to listenerCaptor.lastValue
    }

    fun moderatedCopy(
        id: Long,
        muid: String? = null,
        status: ModerationStatus = ModerationStatus.DISAPPROVED
    ): BaseMessage {
        val message = MockFactory.createTextMessage(
            id = id,
            text = "moderated",
            senderUid = "test-user",
            receiverId = "logged-in-user",
            receiverType = CometChatConstants.RECEIVER_TYPE_USER
        )
        whenever(message.muid).thenReturn(muid)
        whenever(message.moderationStatus).thenReturn(status)
        return message
    }

    beforeTest {
        Dispatchers.setMain(testDispatcher)
        logMock = Mockito.mockStatic(Log::class.java)
        cometChatMock = Mockito.mockStatic(CometChat::class.java)
        repository = mock()
        whenever(repository.hasMorePreviousMessages()).thenReturn(true)
        whenever(repository.fetchPreviousMessages()).thenReturn(Result.success(emptyList()))
        whenever(repository.markAsRead(any())).thenReturn(Result.success(Unit))
        whenever(repository.markAsDelivered(any())).thenReturn(Result.success(Unit))
    }

    afterTest {
        cometChatMock.close()
        logMock.close()
        Dispatchers.resetMain()
    }

    // ==================== A. Verdict application ====================

    test("onMessageModerated swaps the matching message by id and emits messageUpdated") {
        runTest {
            val messages = MockFactory.createMessages(3, startId = 1L, senderUid = "test-user", receiverId = "logged-in-user")
            val (vm, listener) = createViewModelWithListener(messages.map { it as BaseMessage })
            advanceUntilIdle()

            val emitted = mutableListOf<BaseMessage>()
            // Unconfined so the subscription is live before the verdict fires (no replay on the flow)
            val collector = launch(testDispatcher) { vm.messageUpdated.collect { emitted.add(it) } }

            val moderated = moderatedCopy(id = 2L)
            listener.onMessageModerated(moderated)
            advanceUntilIdle()

            vm.getItemCount() shouldBe 3
            vm.getItems().find { it.id == 2L } shouldBe moderated
            emitted shouldHaveSize 1
            emitted.single() shouldBe moderated
            collector.cancel()
        }
    }

    test("onMessageModerated matches by muid when the verdict carries no usable id") {
        runTest {
            val messages = MockFactory.createMessages(3, startId = 1L, senderUid = "test-user", receiverId = "logged-in-user")
            whenever(messages[1].muid).thenReturn("muid-optimistic-2")
            val (vm, listener) = createViewModelWithListener(messages.map { it as BaseMessage })
            advanceUntilIdle()

            // Verdict delivered against the optimistic muid, id not yet assigned
            val moderated = moderatedCopy(id = 0L, muid = "muid-optimistic-2")
            listener.onMessageModerated(moderated)
            advanceUntilIdle()

            vm.getItemCount() shouldBe 3
            vm.getItems().find { it.muid == "muid-optimistic-2" } shouldBe moderated
        }
    }

    // ==================== B. Sticky DISAPPROVED ====================

    test("a later verdict never re-enables a message already DISAPPROVED in the list") {
        runTest {
            val blocked = moderatedCopy(id = 2L, status = ModerationStatus.DISAPPROVED)
            val messages = listOf(
                MockFactory.createTextMessage(id = 1L, senderUid = "test-user", receiverId = "logged-in-user") as BaseMessage,
                blocked
            )
            val (vm, listener) = createViewModelWithListener(messages)
            advanceUntilIdle()

            val emitted = mutableListOf<BaseMessage>()
            val collector = launch(testDispatcher) { vm.messageUpdated.collect { emitted.add(it) } }

            val approvedLater = moderatedCopy(id = 2L, status = ModerationStatus.APPROVED)
            listener.onMessageModerated(approvedLater)
            advanceUntilIdle()

            // The blocked instance must survive; the approving verdict is dropped
            vm.getItems().find { it.id == 2L } shouldBe blocked
            vm.getItems().find { it.id == 2L } shouldNotBe approvedLater
            emitted shouldHaveSize 0
            collector.cancel()
        }
    }

    // ==================== C. Gates ====================

    test("a verdict for a message not in the list is ignored") {
        runTest {
            val messages = MockFactory.createMessages(3, startId = 1L, senderUid = "test-user", receiverId = "logged-in-user")
            val (vm, listener) = createViewModelWithListener(messages.map { it as BaseMessage })
            advanceUntilIdle()

            val emitted = mutableListOf<BaseMessage>()
            val collector = launch(testDispatcher) { vm.messageUpdated.collect { emitted.add(it) } }

            listener.onMessageModerated(moderatedCopy(id = 999L))
            advanceUntilIdle()

            vm.getItemCount() shouldBe 3
            vm.getItems().none { it.id == 999L } shouldBe true
            emitted shouldHaveSize 0
            collector.cancel()
        }
    }

    test("a verdict for a different conversation is ignored") {
        runTest {
            val messages = MockFactory.createMessages(3, startId = 1L, senderUid = "test-user", receiverId = "logged-in-user")
            val (vm, listener) = createViewModelWithListener(messages.map { it as BaseMessage })
            advanceUntilIdle()

            val foreign = MockFactory.createTextMessage(
                id = 2L, // same id as a loaded message — the chat gate must reject it first
                text = "other chat",
                senderUid = "stranger",
                receiverId = "another-user",
                receiverType = CometChatConstants.RECEIVER_TYPE_USER
            )
            whenever(foreign.moderationStatus).thenReturn(ModerationStatus.DISAPPROVED)
            listener.onMessageModerated(foreign)
            advanceUntilIdle()

            vm.getItems().find { it.id == 2L } shouldNotBe foreign
        }
    }
})
