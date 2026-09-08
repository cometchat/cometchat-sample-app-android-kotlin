package com.cometchat.uikit.core.viewmodel.pinnedsaved

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.core.MessagesRequest
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.BaseMessage
import com.cometchat.uikit.core.factory.CometChatPinnedMessagesViewModelFactory
import com.cometchat.uikit.core.state.PinnedSavedListUIState
import com.cometchat.uikit.core.testutils.MockFactory
import com.cometchat.uikit.core.viewmodel.CometChatPinnedMessagesViewModel
import com.cometchat.uikit.core.viewmodel.CometChatPinnedMessagesViewModel.PinnedActionResult
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.mockito.MockedConstruction
import org.mockito.MockedStatic
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Unit tests for CometChatPinnedMessagesViewModel.
 *
 * Architecture (matches MessageListReactionsPropertyTest):
 * - `Mockito.mockStatic(CometChat)` for the pin/unpin/delete/thread-subscription facade calls,
 *   capturing the CallbackListener so success/error can be driven explicitly.
 * - `Mockito.mockConstruction(MessagesRequestBuilder)` so reload()'s builder chain yields a mocked
 *   MessagesRequest whose fetchNext() serves a scripted queue of pages.
 * - `enableListeners = false` + UnconfinedTestDispatcher as Main.
 *
 * Run with:
 *   ./gradlew :chatuikit-core:testDebugUnitTest --tests "*CometChatPinnedMessagesViewModelTest"
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CometChatPinnedMessagesViewModelTest : FunSpec({

    isolationMode = IsolationMode.SingleInstance

    val testDispatcher = UnconfinedTestDispatcher()

    lateinit var cometChatMock: MockedStatic<CometChat>
    lateinit var builderConstruction: MockedConstruction<MessagesRequest.MessagesRequestBuilder>
    lateinit var currentRequest: MessagesRequest

    /** MessagesRequest mock that serves the given pages in order, then empty pages forever. */
    fun requestServing(vararg pages: List<BaseMessage>): MessagesRequest {
        val queue = ArrayDeque(pages.toList())
        val req = mock<MessagesRequest>()
        doAnswer { invocation ->
            val cb = invocation.getArgument<CometChat.CallbackListener<List<BaseMessage>>>(0)
            cb.onSuccess(queue.removeFirstOrNull() ?: emptyList())
            null
        }.whenever(req).fetchNext(any())
        return req
    }

    /** MessagesRequest mock that fails every fetch with the given exception. */
    fun requestFailing(exception: CometChatException): MessagesRequest {
        val req = mock<MessagesRequest>()
        doAnswer { invocation ->
            val cb = invocation.getArgument<CometChat.CallbackListener<List<BaseMessage>>>(0)
            cb.onError(exception)
            null
        }.whenever(req).fetchNext(any())
        return req
    }

    fun messagesOf(vararg ids: Long): List<BaseMessage> =
        ids.map { MockFactory.createTextMessage(id = it, text = "msg-$it") }

    beforeSpec {
        Dispatchers.setMain(testDispatcher)
    }

    afterSpec {
        Thread.sleep(50)
        Dispatchers.resetMain()
    }

    beforeTest {
        currentRequest = requestServing()
        // Every MessagesRequestBuilder the VM constructs is fluent and builds `currentRequest`.
        builderConstruction = Mockito.mockConstruction(
            MessagesRequest.MessagesRequestBuilder::class.java
        ) { builder, _ ->
            whenever(builder.setPinned(any())).thenReturn(builder)
            whenever(builder.setSaved(any())).thenReturn(builder)
            whenever(builder.setLimit(any())).thenReturn(builder)
            whenever(builder.setTypes(any())).thenReturn(builder)
            whenever(builder.setCategories(any())).thenReturn(builder)
            whenever(builder.setUID(any())).thenReturn(builder)
            whenever(builder.setGUID(any())).thenReturn(builder)
            whenever(builder.build()).thenAnswer { currentRequest }
        }
        cometChatMock = Mockito.mockStatic(CometChat::class.java)
        println("  🧪 ${it.name.testName}")
    }

    afterTest {
        cometChatMock.close()
        builderConstruction.close()
        println()
    }

    test("warmup - absorb leaked exceptions") {
        try { runTest(testDispatcher) { } } catch (_: Exception) { }
    }

    // ==================== A. Fetch & UI State ====================

    test("PBT: for any single-page size, configure → Empty when 0, else Content with exact count") {
        checkAll(20, Arb.int(0..20)) { count ->
            runTest(testDispatcher) {
                val page = messagesOf(*(1..count).map { it.toLong() }.toLongArray())
                currentRequest = if (count == 0) requestServing() else requestServing(page)

                val vm = CometChatPinnedMessagesViewModel(enableListeners = false)
                vm.configure(uid = "peer-1", guid = null)

                if (count == 0) {
                    vm.uiState.value shouldBe PinnedSavedListUIState.Empty
                } else {
                    vm.uiState.value shouldBe PinnedSavedListUIState.Content
                }
                vm.messages.value shouldHaveSize count
                vm.count.value shouldBe count
            }
        }
    }

    test("multi-page fetch accumulates pages until an empty page") {
        runTest(testDispatcher) {
            currentRequest = requestServing(messagesOf(1, 2), messagesOf(3, 4, 5))

            val vm = CometChatPinnedMessagesViewModel(enableListeners = false)
            vm.configure(uid = null, guid = "group-1")

            vm.messages.value.map { it.id } shouldBe listOf(1L, 2L, 3L, 4L, 5L)
            vm.count.value shouldBe 5
            vm.uiState.value shouldBe PinnedSavedListUIState.Content
            // 2 content pages + 1 terminating empty page
            verify(currentRequest, times(3)).fetchNext(any())
        }
    }

    test("fetch stops paging once the 100-message cap is reached") {
        runTest(testDispatcher) {
            val pages = (0 until 5).map { pageIndex ->
                messagesOf(*(1..30).map { (pageIndex * 30 + it).toLong() }.toLongArray())
            }
            currentRequest = requestServing(*pages.toTypedArray())

            val vm = CometChatPinnedMessagesViewModel(enableListeners = false)
            vm.configure(uid = "peer-1", guid = null)

            // 30+30+30+30 = 120 ≥ cap, so the 5th page is never requested.
            vm.count.value shouldBe 120
            verify(currentRequest, times(4)).fetchNext(any())
        }
    }

    test("fetch error → Error state carrying the exception") {
        runTest(testDispatcher) {
            val exception = MockFactory.createCometChatException("ERR_TEST", "boom")
            currentRequest = requestFailing(exception)

            val vm = CometChatPinnedMessagesViewModel(enableListeners = false)
            vm.configure(uid = "peer-1", guid = null)

            vm.uiState.value.shouldBeInstanceOf<PinnedSavedListUIState.Error>()
            (vm.uiState.value as PinnedSavedListUIState.Error).exception shouldBe exception
            vm.messages.value shouldHaveSize 0
        }
    }

    test("reload clears previous content and refetches") {
        runTest(testDispatcher) {
            currentRequest = requestServing(messagesOf(1, 2))
            val vm = CometChatPinnedMessagesViewModel(enableListeners = false)
            vm.configure(uid = "peer-1", guid = null)
            vm.count.value shouldBe 2

            currentRequest = requestServing(messagesOf(9))
            vm.reload()

            vm.messages.value.map { it.id } shouldBe listOf(9L)
            vm.count.value shouldBe 1
            vm.uiState.value shouldBe PinnedSavedListUIState.Content
        }
    }

    test("switching conversation mid-load discards the stale load (ENG-38060 regression)") {
        runTest(testDispatcher) {
            // Conversation A's paging never calls back — it is still in flight when we switch.
            val stalledCallbacks = mutableListOf<CometChat.CallbackListener<List<BaseMessage>>>()
            val stalledRequest = mock<MessagesRequest>()
            doAnswer { invocation ->
                stalledCallbacks.add(invocation.getArgument(0))
                null
            }.whenever(stalledRequest).fetchNext(any())

            currentRequest = stalledRequest
            val vm = CometChatPinnedMessagesViewModel(enableListeners = false)
            vm.configure(uid = "peer-A", guid = null)
            vm.messages.value shouldHaveSize 0

            // Switch to B while A is mid-load: B must load rather than be blocked by A.
            currentRequest = requestServing(messagesOf(7, 8))
            vm.configure(uid = "peer-B", guid = null)
            vm.messages.value.map { it.id } shouldBe listOf(7L, 8L)

            // A's page lands late — it must not be published under B's header.
            stalledCallbacks.forEach { it.onSuccess(messagesOf(1, 2, 3)) }

            vm.messages.value.map { it.id } shouldBe listOf(7L, 8L)
            vm.count.value shouldBe 2
            vm.uiState.value shouldBe PinnedSavedListUIState.Content
        }
    }

    test("unpin failure re-inserts only the removed row, keeping a concurrent pin") {
        runTest(testDispatcher) {
            var capturedListener: CometChat.CallbackListener<BaseMessage>? = null
            cometChatMock.`when`<Unit> { CometChat.unpinMessage(any(), any()) }.thenAnswer { inv ->
                capturedListener = inv.getArgument(1)
                null
            }
            currentRequest = requestServing(messagesOf(1, 2))
            val vm = CometChatPinnedMessagesViewModel(enableListeners = false)
            vm.configure(uid = "peer-1", guid = null)
            val target = vm.messages.value.first { it.id == 1L }

            vm.unpin(target)
            // A pin delivered by the SDK listener between the optimistic removal and the failure.
            vm.onMessagePinnedExternally(
                MockFactory.createTextMessage(id = 9L, text = "msg-9", receiverId = "peer-1")
            )

            capturedListener!!.onError(MockFactory.createCometChatException("ERR_UNPIN", "denied"))

            // Row 1 goes back to the index it was removed from, and the pin that arrived in
            // between survives — a whole-snapshot restore would have discarded it.
            vm.messages.value.map { it.id } shouldBe listOf(1L, 9L, 2L)
            vm.count.value shouldBe 3
            vm.uiState.value shouldBe PinnedSavedListUIState.Content
        }
    }

    test("configure with uid sets UID on the request builder; guid sets GUID") {
        runTest(testDispatcher) {
            val vm = CometChatPinnedMessagesViewModel(enableListeners = false)

            vm.configure(uid = "peer-1", guid = null)
            val uidBuilder = builderConstruction.constructed().last()
            verify(uidBuilder).setPinned(true)
            verify(uidBuilder).setUID("peer-1")

            vm.configure(uid = null, guid = "group-1")
            val guidBuilder = builderConstruction.constructed().last()
            verify(guidBuilder).setPinned(true)
            verify(guidBuilder).setGUID("group-1")
        }
    }

    test("fetch requests the message list's FULL types + categories (ENG-38060)") {
        runTest(testDispatcher) {
            val vm = CometChatPinnedMessagesViewModel(enableListeners = false)
            vm.configure(uid = "peer-1", guid = null)

            // Without these the server's own (narrower) defaults apply and custom-category
            // messages — polls, stickers, whiteboard/document, meetings, cards — are silently
            // absent from the panel. Assert the exact shared lists so this panel can never
            // drift from what the message list renders.
            val builder = builderConstruction.constructed().last()
            verify(builder).setTypes(com.cometchat.uikit.core.utils.getDefaultMessagesTypes())
            verify(builder).setCategories(com.cometchat.uikit.core.utils.getDefaultMessagesCategories())
        }
    }

    // ==================== B. Unpin (optimistic + revert) ====================

    test("unpin optimistically removes the message and calls CometChat.unpinMessage") {
        runTest(testDispatcher) {
            currentRequest = requestServing(messagesOf(1, 2, 3))
            val vm = CometChatPinnedMessagesViewModel(enableListeners = false)
            vm.configure(uid = "peer-1", guid = null)
            val target = vm.messages.value.first { it.id == 2L }

            vm.unpin(target)

            vm.messages.value.map { it.id } shouldBe listOf(1L, 3L)
            vm.count.value shouldBe 2
            vm.uiState.value shouldBe PinnedSavedListUIState.Content
            cometChatMock.verify { CometChat.unpinMessage(eq(2L), any()) }
        }
    }

    test("unpin of the last message → Empty state") {
        runTest(testDispatcher) {
            currentRequest = requestServing(messagesOf(1))
            val vm = CometChatPinnedMessagesViewModel(enableListeners = false)
            vm.configure(uid = "peer-1", guid = null)

            vm.unpin(vm.messages.value.single())

            vm.messages.value shouldHaveSize 0
            vm.count.value shouldBe 0
            vm.uiState.value shouldBe PinnedSavedListUIState.Empty
        }
    }

    test("unpin error reverts the optimistic removal and stays on Content (Error is load-only)") {
        runTest(testDispatcher) {
            var capturedListener: CometChat.CallbackListener<BaseMessage>? = null
            cometChatMock.`when`<Unit> { CometChat.unpinMessage(any(), any()) }.thenAnswer { inv ->
                capturedListener = inv.getArgument(1)
                null
            }
            currentRequest = requestServing(messagesOf(1, 2))
            val vm = CometChatPinnedMessagesViewModel(enableListeners = false)
            vm.configure(uid = "peer-1", guid = null)
            val target = vm.messages.value.first { it.id == 1L }

            vm.unpin(target)
            vm.count.value shouldBe 1

            val exception = MockFactory.createCometChatException("ERR_UNPIN", "denied")
            capturedListener!!.onError(exception)

            // The full-screen Error state is reserved for LOAD failures — a failed action on a
            // healthy list restores the row, stays on Content and surfaces a toast instead.
            vm.messages.value.map { it.id } shouldBe listOf(1L, 2L)
            vm.count.value shouldBe 2
            vm.uiState.value shouldBe PinnedSavedListUIState.Content
        }
    }

    // ==================== C. Pin action results ====================

    test("pin success emits PINNED; pin error emits PIN_FAILED") {
        runTest(testDispatcher) {
            var capturedListener: CometChat.CallbackListener<BaseMessage>? = null
            cometChatMock.`when`<Unit> { CometChat.pinMessage(any(), any()) }.thenAnswer { inv ->
                capturedListener = inv.getArgument(1)
                null
            }
            val vm = CometChatPinnedMessagesViewModel(enableListeners = false)
            val results = mutableListOf<PinnedActionResult>()
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                vm.actionResult.collect { results.add(it) }
            }
            val message = MockFactory.createTextMessage(id = 7L)

            vm.pin(message)
            capturedListener!!.onSuccess(message)
            results shouldBe listOf(PinnedActionResult.PINNED)

            vm.pin(message)
            capturedListener!!.onError(MockFactory.createCometChatException("ERR", "cap"))
            results shouldBe listOf(PinnedActionResult.PINNED, PinnedActionResult.PIN_FAILED)
        }
    }

    test("ERR_PERMISSION_DENIED maps to the shared PERMISSION_DENIED for both pin and unpin") {
        runTest(testDispatcher) {
            var pinListener: CometChat.CallbackListener<BaseMessage>? = null
            var unpinListener: CometChat.CallbackListener<BaseMessage>? = null
            cometChatMock.`when`<Unit> { CometChat.pinMessage(any(), any()) }.thenAnswer { inv ->
                pinListener = inv.getArgument(1)
                null
            }
            cometChatMock.`when`<Unit> { CometChat.unpinMessage(any(), any()) }.thenAnswer { inv ->
                unpinListener = inv.getArgument(1)
                null
            }
            currentRequest = requestServing(messagesOf(1, 2))
            val vm = CometChatPinnedMessagesViewModel(enableListeners = false)
            vm.configure(uid = "peer-1", guid = null)
            val results = mutableListOf<PinnedActionResult>()
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                vm.actionResult.collect { results.add(it) }
            }
            val denied = MockFactory.createCometChatException("ERR_PERMISSION_DENIED", "denied")

            vm.pin(vm.messages.value.first { it.id == 1L })
            pinListener!!.onError(denied)
            results shouldBe listOf(PinnedActionResult.PERMISSION_DENIED)

            vm.unpin(vm.messages.value.first { it.id == 1L })
            unpinListener!!.onError(denied)
            results shouldBe listOf(
                PinnedActionResult.PERMISSION_DENIED,
                PinnedActionResult.PERMISSION_DENIED
            )
        }
    }

    // ==================== D. Delete (optimistic + revert) ====================

    test("delete optimistically removes, emits DELETED on success and reverts + DELETE_FAILED on error") {
        runTest(testDispatcher) {
            var capturedListener: CometChat.CallbackListener<BaseMessage>? = null
            cometChatMock.`when`<Unit> { CometChat.deleteMessage(any(), any()) }.thenAnswer { inv ->
                capturedListener = inv.getArgument(1)
                null
            }
            currentRequest = requestServing(messagesOf(1, 2))
            val vm = CometChatPinnedMessagesViewModel(enableListeners = false)
            vm.configure(uid = "peer-1", guid = null)
            val results = mutableListOf<PinnedActionResult>()
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                vm.actionResult.collect { results.add(it) }
            }
            val target = vm.messages.value.first { it.id == 1L }

            vm.delete(target)
            vm.messages.value.map { it.id } shouldBe listOf(2L)

            capturedListener!!.onSuccess(target)
            results shouldBe listOf(PinnedActionResult.DELETED)
            vm.messages.value.map { it.id } shouldBe listOf(2L)

            val other = vm.messages.value.single()
            vm.delete(other)
            vm.messages.value shouldHaveSize 0
            vm.uiState.value shouldBe PinnedSavedListUIState.Empty

            capturedListener!!.onError(MockFactory.createCometChatException("ERR", "nope"))
            vm.messages.value.map { it.id } shouldBe listOf(2L)
            results shouldBe listOf(PinnedActionResult.DELETED, PinnedActionResult.DELETE_FAILED)
        }
    }

    // ==================== F. External live upkeep ====================

    test("externally pinned message for THIS conversation is prepended once; duplicates ignored") {
        runTest(testDispatcher) {
            currentRequest = requestServing(messagesOf(1))
            val vm = CometChatPinnedMessagesViewModel(enableListeners = false)
            vm.configure(uid = null, guid = "group-1")

            val incoming = MockFactory.createTextMessage(
                id = 2L, receiverId = "group-1",
                receiverType = CometChatConstants.RECEIVER_TYPE_GROUP
            )
            vm.onMessagePinnedExternally(incoming)
            vm.messages.value.map { it.id } shouldBe listOf(2L, 1L)
            vm.uiState.value shouldBe PinnedSavedListUIState.Content

            vm.onMessagePinnedExternally(incoming)
            vm.messages.value shouldHaveSize 2
        }
    }

    test("externally pinned message for ANOTHER conversation is ignored") {
        runTest(testDispatcher) {
            currentRequest = requestServing(messagesOf(1))
            val vm = CometChatPinnedMessagesViewModel(enableListeners = false)
            vm.configure(uid = null, guid = "group-1")

            val otherGroup = MockFactory.createTextMessage(
                id = 2L, receiverId = "group-OTHER",
                receiverType = CometChatConstants.RECEIVER_TYPE_GROUP
            )
            vm.onMessagePinnedExternally(otherGroup)

            vm.messages.value.map { it.id } shouldBe listOf(1L)
        }
    }

    test("externally unpinned message is removed; removing the last one → Empty") {
        runTest(testDispatcher) {
            currentRequest = requestServing(messagesOf(1, 2))
            val vm = CometChatPinnedMessagesViewModel(enableListeners = false)
            vm.configure(uid = "peer-1", guid = null)

            vm.onMessageUnpinnedExternally(vm.messages.value.first { it.id == 1L })
            vm.messages.value.map { it.id } shouldBe listOf(2L)
            vm.uiState.value shouldBe PinnedSavedListUIState.Content

            vm.onMessageUnpinnedExternally(vm.messages.value.single())
            vm.messages.value shouldHaveSize 0
            vm.uiState.value shouldBe PinnedSavedListUIState.Empty
        }
    }

    // ==================== G. Own SDK listener (second-device realtime) ====================
    //
    // The panel must not depend on CometChatMessageListViewModel being alive to feed the UIKit bus:
    // it is routinely opened with no message list mounted, and on a second device the SDK callback is
    // the only signal. So it registers its own CometChat.MessageListener.

    test("with listeners enabled the VM registers its own SDK message listener") {
        runTest(testDispatcher) {
            currentRequest = requestServing(messagesOf(1))
            val vm = CometChatPinnedMessagesViewModel(enableListeners = true)
            vm.configure(uid = null, guid = "group-1")

            cometChatMock.verify({ CometChat.addMessageListener(any(), any()) }, atLeastOnce())
        }
    }

    test("SDK onMessagePinned inserts a row with no UIKit-bus involvement") {
        runTest(testDispatcher) {
            currentRequest = requestServing(messagesOf(1))
            val vm = CometChatPinnedMessagesViewModel(enableListeners = true)
            vm.configure(uid = null, guid = "group-1")

            val listenerCaptor = argumentCaptor<CometChat.MessageListener>()
            cometChatMock.verify(
                { CometChat.addMessageListener(any(), listenerCaptor.capture()) },
                atLeastOnce()
            )

            val pinnedElsewhere = MockFactory.createTextMessage(
                id = 7L, receiverId = "group-1",
                receiverType = CometChatConstants.RECEIVER_TYPE_GROUP
            )
            listenerCaptor.lastValue.onMessagePinned(pinnedElsewhere)

            vm.messages.value.map { it.id } shouldBe listOf(7L, 1L)
            vm.count.value shouldBe 2
        }
    }

    test("SDK onMessageUnpinned removes the row; the conversation gate still applies") {
        runTest(testDispatcher) {
            currentRequest = requestServing(messagesOf(1, 2))
            val vm = CometChatPinnedMessagesViewModel(enableListeners = true)
            vm.configure(uid = null, guid = "group-1")

            val listenerCaptor = argumentCaptor<CometChat.MessageListener>()
            cometChatMock.verify(
                { CometChat.addMessageListener(any(), listenerCaptor.capture()) },
                atLeastOnce()
            )
            val listener = listenerCaptor.lastValue

            // A pin from a different conversation must not leak into this panel.
            listener.onMessagePinned(
                MockFactory.createTextMessage(
                    id = 99L, receiverId = "group-OTHER",
                    receiverType = CometChatConstants.RECEIVER_TYPE_GROUP
                )
            )
            vm.messages.value.map { it.id } shouldBe listOf(1L, 2L)

            listener.onMessageUnpinned(vm.messages.value.first { it.id == 1L })
            vm.messages.value.map { it.id } shouldBe listOf(2L)
        }
    }

    test("clearing the VM removes the SDK listener it registered") {
        runTest(testDispatcher) {
            currentRequest = requestServing()
            val store = ViewModelStore()
            ViewModelProvider(store, CometChatPinnedMessagesViewModelFactory())[
                CometChatPinnedMessagesViewModel::class.java
            ]

            val tagCaptor = argumentCaptor<String>()
            cometChatMock.verify(
                { CometChat.addMessageListener(tagCaptor.capture(), any()) },
                atLeastOnce()
            )
            val tag = tagCaptor.lastValue

            store.clear()

            cometChatMock.verify { CometChat.removeMessageListener(eq(tag)) }
        }
    }
})
