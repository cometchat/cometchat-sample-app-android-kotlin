package com.cometchat.uikit.core.viewmodel.pinnedsaved

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.core.MessagesRequest
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.BaseMessage
import com.cometchat.uikit.core.factory.CometChatSavedMessagesViewModelFactory
import com.cometchat.uikit.core.state.PinnedSavedListUIState
import com.cometchat.uikit.core.testutils.MockFactory
import com.cometchat.uikit.core.viewmodel.CometChatSavedMessagesViewModel
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
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Unit tests for CometChatSavedMessagesViewModel.
 *
 * Same harness as CometChatPinnedMessagesViewModelTest: mockStatic(CometChat) for unsaveMessage,
 * mockConstruction(MessagesRequestBuilder) so reload() fetches from a scripted MessagesRequest.
 *
 * Run with:
 *   ./gradlew :chatuikit-core:testDebugUnitTest --tests "*CometChatSavedMessagesViewModelTest"
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CometChatSavedMessagesViewModelTest : FunSpec({

    isolationMode = IsolationMode.SingleInstance

    val testDispatcher = UnconfinedTestDispatcher()

    lateinit var cometChatMock: MockedStatic<CometChat>
    lateinit var builderConstruction: MockedConstruction<MessagesRequest.MessagesRequestBuilder>
    lateinit var currentRequest: MessagesRequest

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
        builderConstruction = Mockito.mockConstruction(
            MessagesRequest.MessagesRequestBuilder::class.java
        ) { builder, _ ->
            whenever(builder.setPinned(any())).thenReturn(builder)
            whenever(builder.setSaved(any())).thenReturn(builder)
            whenever(builder.setLimit(any())).thenReturn(builder)
            whenever(builder.setTypes(any())).thenReturn(builder)
            whenever(builder.setCategories(any())).thenReturn(builder)
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

    test("PBT: for any single-page size, reload → Empty when 0, else Content with exact count") {
        checkAll(20, Arb.int(0..20)) { count ->
            runTest(testDispatcher) {
                val page = messagesOf(*(1..count).map { it.toLong() }.toLongArray())
                currentRequest = if (count == 0) requestServing() else requestServing(page)

                val vm = CometChatSavedMessagesViewModel(enableListeners = false)
                vm.reload()

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

    test("reload builds a saved-messages request (setSaved(true), no UID/GUID scope)") {
        runTest(testDispatcher) {
            val vm = CometChatSavedMessagesViewModel(enableListeners = false)
            vm.reload()

            val builder = builderConstruction.constructed().last()
            verify(builder).setSaved(true)
            verify(builder, times(0)).setUID(any())
            verify(builder, times(0)).setGUID(any())
        }
    }

    test("fetch requests the message list's FULL types + categories (ENG-38060)") {
        runTest(testDispatcher) {
            val vm = CometChatSavedMessagesViewModel(enableListeners = false)
            vm.reload()

            // Same reasoning as the pinned panel: without these, custom-category messages
            // (polls, stickers, whiteboard/document, meetings, cards) never come back from the
            // saved-messages fetch at all.
            val builder = builderConstruction.constructed().last()
            verify(builder).setTypes(com.cometchat.uikit.core.utils.getDefaultMessagesTypes())
            verify(builder).setCategories(com.cometchat.uikit.core.utils.getDefaultMessagesCategories())
        }
    }

    test("multi-page fetch accumulates until an empty page; caps at 100") {
        runTest(testDispatcher) {
            currentRequest = requestServing(messagesOf(1, 2), messagesOf(3))
            val vm = CometChatSavedMessagesViewModel(enableListeners = false)
            vm.reload()
            vm.messages.value.map { it.id } shouldBe listOf(1L, 2L, 3L)
            verify(currentRequest, times(3)).fetchNext(any())

            val bigPages = (0 until 5).map { pageIndex ->
                messagesOf(*(1..30).map { (pageIndex * 30 + it).toLong() }.toLongArray())
            }
            currentRequest = requestServing(*bigPages.toTypedArray())
            vm.reload()
            vm.count.value shouldBe 120
            verify(currentRequest, times(4)).fetchNext(any())
        }
    }

    test("fetch error → Error state carrying the exception") {
        runTest(testDispatcher) {
            val exception = MockFactory.createCometChatException("ERR_TEST", "boom")
            currentRequest = requestFailing(exception)

            val vm = CometChatSavedMessagesViewModel(enableListeners = false)
            vm.reload()

            vm.uiState.value.shouldBeInstanceOf<PinnedSavedListUIState.Error>()
            (vm.uiState.value as PinnedSavedListUIState.Error).exception shouldBe exception
        }
    }

    // ==================== B. Unsave (optimistic + revert + toast signal) ====================

    test("a reload during an in-flight load discards the stale run") {
        runTest(testDispatcher) {
            // The first run's paging never calls back — it is still in flight when we reload.
            val stalledCallbacks = mutableListOf<CometChat.CallbackListener<List<BaseMessage>>>()
            val stalledRequest = mock<MessagesRequest>()
            doAnswer { invocation ->
                stalledCallbacks.add(invocation.getArgument(0))
                null
            }.whenever(stalledRequest).fetchNext(any())

            currentRequest = stalledRequest
            val vm = CometChatSavedMessagesViewModel(enableListeners = false)
            vm.reload()
            vm.messages.value shouldHaveSize 0

            // The second reload must load rather than be blocked by the first.
            currentRequest = requestServing(messagesOf(7, 8))
            vm.reload()
            vm.messages.value.map { it.id } shouldBe listOf(7L, 8L)

            // The first run's page lands late — it must not overwrite the fresher list.
            stalledCallbacks.forEach { it.onSuccess(messagesOf(1, 2, 3)) }

            vm.messages.value.map { it.id } shouldBe listOf(7L, 8L)
            vm.count.value shouldBe 2
            vm.uiState.value shouldBe PinnedSavedListUIState.Content
        }
    }

    test("unsave failure re-inserts only the removed row, keeping a concurrent save") {
        runTest(testDispatcher) {
            var capturedListener: CometChat.CallbackListener<BaseMessage>? = null
            cometChatMock.`when`<Unit> { CometChat.unsaveMessage(any(), any()) }.thenAnswer { inv ->
                capturedListener = inv.getArgument(1)
                null
            }
            currentRequest = requestServing(messagesOf(1, 2))
            val vm = CometChatSavedMessagesViewModel(enableListeners = false)
            vm.reload()
            val target = vm.messages.value.first { it.id == 1L }

            vm.unsave(target)
            // A save delivered by the SDK listener between the optimistic removal and the failure.
            vm.onMessageSavedExternally(MockFactory.createTextMessage(id = 9L, text = "msg-9"))

            capturedListener!!.onError(MockFactory.createCometChatException("ERR_UNSAVE", "denied"))

            // Row 1 goes back to the index it was removed from, and the save that arrived in
            // between survives — a whole-snapshot restore would have discarded it.
            vm.messages.value.map { it.id } shouldBe listOf(1L, 9L, 2L)
            vm.count.value shouldBe 3
            vm.uiState.value shouldBe PinnedSavedListUIState.Content
        }
    }

    test("unsave optimistically removes the message and calls CometChat.unsaveMessage") {
        runTest(testDispatcher) {
            currentRequest = requestServing(messagesOf(1, 2, 3))
            val vm = CometChatSavedMessagesViewModel(enableListeners = false)
            vm.reload()

            vm.unsave(vm.messages.value.first { it.id == 2L })

            vm.messages.value.map { it.id } shouldBe listOf(1L, 3L)
            vm.count.value shouldBe 2
            cometChatMock.verify { CometChat.unsaveMessage(eq(2L), any()) }
        }
    }

    test("unsave success emits the one-shot unsaveSuccess toast signal") {
        runTest(testDispatcher) {
            var capturedListener: CometChat.CallbackListener<BaseMessage>? = null
            cometChatMock.`when`<Unit> { CometChat.unsaveMessage(any(), any()) }.thenAnswer { inv ->
                capturedListener = inv.getArgument(1)
                null
            }
            currentRequest = requestServing(messagesOf(1))
            val vm = CometChatSavedMessagesViewModel(enableListeners = false)
            vm.reload()
            var successCount = 0
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                vm.unsaveSuccess.collect { successCount++ }
            }
            val target = vm.messages.value.single()

            vm.unsave(target)
            successCount shouldBe 0
            capturedListener!!.onSuccess(target)

            successCount shouldBe 1
            vm.uiState.value shouldBe PinnedSavedListUIState.Empty
        }
    }

    test("unsave error reverts the optimistic removal and stays on Content (Error is load-only)") {
        runTest(testDispatcher) {
            var capturedListener: CometChat.CallbackListener<BaseMessage>? = null
            cometChatMock.`when`<Unit> { CometChat.unsaveMessage(any(), any()) }.thenAnswer { inv ->
                capturedListener = inv.getArgument(1)
                null
            }
            currentRequest = requestServing(messagesOf(1, 2))
            val vm = CometChatSavedMessagesViewModel(enableListeners = false)
            vm.reload()

            vm.unsave(vm.messages.value.first { it.id == 1L })
            vm.count.value shouldBe 1

            capturedListener!!.onError(MockFactory.createCometChatException("ERR", "denied"))

            // The full-screen Error state is reserved for LOAD failures — a failed unsave on a
            // healthy list just restores the row and stays on Content.
            vm.messages.value.map { it.id } shouldBe listOf(1L, 2L)
            vm.count.value shouldBe 2
            vm.uiState.value shouldBe PinnedSavedListUIState.Content
        }
    }

    // ==================== C. External live upkeep ====================

    test("externally saved message is prepended once; duplicates ignored") {
        runTest(testDispatcher) {
            currentRequest = requestServing(messagesOf(1))
            val vm = CometChatSavedMessagesViewModel(enableListeners = false)
            vm.reload()

            val incoming = MockFactory.createTextMessage(id = 2L)
            vm.onMessageSavedExternally(incoming)
            vm.messages.value.map { it.id } shouldBe listOf(2L, 1L)
            vm.uiState.value shouldBe PinnedSavedListUIState.Content

            vm.onMessageSavedExternally(incoming)
            vm.messages.value shouldHaveSize 2
        }
    }

    test("externally unsaved message is removed; removing the last one → Empty") {
        runTest(testDispatcher) {
            currentRequest = requestServing(messagesOf(1, 2))
            val vm = CometChatSavedMessagesViewModel(enableListeners = false)
            vm.reload()

            vm.onMessageUnsavedExternally(vm.messages.value.first { it.id == 1L })
            vm.messages.value.map { it.id } shouldBe listOf(2L)

            vm.onMessageUnsavedExternally(vm.messages.value.single())
            vm.messages.value shouldHaveSize 0
            vm.uiState.value shouldBe PinnedSavedListUIState.Empty
        }
    }

    // ==================== D. Own SDK listener (second-device realtime) ====================
    //
    // Saved Messages is user-level and normally opened straight from the conversations menu, with no
    // message list mounted to feed the UIKit bus — and on a second device the SDK callback is the
    // only signal. So the VM registers its own CometChat.MessageListener.

    test("SDK onMessageSaved inserts a row with no UIKit-bus involvement") {
        runTest(testDispatcher) {
            currentRequest = requestServing(messagesOf(1))
            val vm = CometChatSavedMessagesViewModel(enableListeners = true)
            vm.reload()

            val listenerCaptor = argumentCaptor<CometChat.MessageListener>()
            cometChatMock.verify(
                { CometChat.addMessageListener(any(), listenerCaptor.capture()) },
                atLeastOnce()
            )

            listenerCaptor.lastValue.onMessageSaved(MockFactory.createTextMessage(id = 7L))

            vm.messages.value.map { it.id } shouldBe listOf(7L, 1L)
            vm.count.value shouldBe 2
        }
    }

    test("SDK onMessageUnsaved removes the row") {
        runTest(testDispatcher) {
            currentRequest = requestServing(messagesOf(1, 2))
            val vm = CometChatSavedMessagesViewModel(enableListeners = true)
            vm.reload()

            val listenerCaptor = argumentCaptor<CometChat.MessageListener>()
            cometChatMock.verify(
                { CometChat.addMessageListener(any(), listenerCaptor.capture()) },
                atLeastOnce()
            )

            listenerCaptor.lastValue.onMessageUnsaved(vm.messages.value.first { it.id == 1L })

            vm.messages.value.map { it.id } shouldBe listOf(2L)
        }
    }

    test("enableListeners = false registers no SDK listener") {
        runTest(testDispatcher) {
            currentRequest = requestServing()
            CometChatSavedMessagesViewModel(enableListeners = false).reload()

            cometChatMock.verify({ CometChat.addMessageListener(any(), any()) }, never())
        }
    }

    test("clearing the VM removes the SDK listener it registered") {
        runTest(testDispatcher) {
            currentRequest = requestServing()
            val store = ViewModelStore()
            ViewModelProvider(store, CometChatSavedMessagesViewModelFactory())[
                CometChatSavedMessagesViewModel::class.java
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
