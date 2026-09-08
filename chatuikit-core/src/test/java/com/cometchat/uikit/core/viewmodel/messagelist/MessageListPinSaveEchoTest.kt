package com.cometchat.uikit.core.viewmodel.messagelist

import android.util.Log
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.TextMessage
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.domain.repository.MessageListRepository
import com.cometchat.uikit.core.testutils.MockFactory
import com.cometchat.uikit.core.viewmodel.CometChatMessageListViewModel
import io.kotest.core.spec.style.FunSpec
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
 * Pin/save echo handling in [CometChatMessageListViewModel].
 *
 * When a message is pinned/unpinned/saved/unsaved — from the bubble menu, from the Pinned/Saved
 * panel, or from another device — the SDK hands the listener the message as parsed from the
 * pin/save REST response, NOT the copy this list is holding. That object is not guaranteed to
 * carry routing fields (receiverType / receiverUid), and the old code both gated on them
 * (`isMessageForCurrentChat`) and swapped the echoed object into the list wholesale. A partial
 * response therefore left the bubble's indicator stale with no error anywhere.
 *
 * `applyPinSaveEcho` now matches by id against the loaded list and carries the pin/save change onto
 * a clone of the loaded message. These tests use REAL SDK model instances (not mocks) because the
 * behaviour under test is exactly clone + field mutation + BaseMessage.contentEquals.
 *
 * Categories:
 * A. Partial echo — routing-less echo still updates the loaded bubble
 * B. Field preservation — the loaded message's own fields survive the update
 * C. Emission — the swap is a distinct instance, so the StateFlow actually emits
 * D. Fallback — a message this list hasn't loaded still uses the routing gate
 *
 * Run: ./gradlew :chatuikit-core:testDebugUnitTest --tests "*MessageListPinSaveEchoTest"
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MessageListPinSaveEchoTest : FunSpec({

    val testDispatcher = UnconfinedTestDispatcher()

    val peerUid = "peer-1"

    lateinit var repository: MessageListRepository
    lateinit var logMock: MockedStatic<Log>
    lateinit var cometChatMock: MockedStatic<CometChat>

    /** A real TextMessage in the current 1:1 chat — clone/equals/field writes all behave for real. */
    fun message(id: Long, text: String = "msg-$id", pinnedAt: Long = 0L, savedAt: Long = 0L) =
        TextMessage(peerUid, text, CometChatConstants.RECEIVER_TYPE_USER).apply {
            this.id = id
            this.sentAt = 1735689600L
            this.sender = User().apply { uid = peerUid; name = "Peer" }
            this.pinnedAt = pinnedAt
            this.savedAt = savedAt
        }

    /**
     * An echo carrying only the id and the pin/save fields — no usable receiverType/receiverUid,
     * i.e. what a partial pin/save REST response parses into.
     */
    fun routinglessEcho(id: Long, pinnedAt: Long = 0L, savedAt: Long = 0L) =
        TextMessage("", "", "").apply {
            this.id = id
            this.pinnedAt = pinnedAt
            this.savedAt = savedAt
        }

    // Every VM is hosted in a ViewModelStore that afterTest clears — an uncleared VM's leaked
    // coroutines get pinned on the NEXT spec's first runTest (UncaughtExceptionsBeforeTest).
    val stores = mutableListOf<androidx.lifecycle.ViewModelStore>()

    suspend fun viewModelWith(
        messages: List<BaseMessage>
    ): Pair<CometChatMessageListViewModel, CometChat.MessageListener> {
        whenever(repository.fetchPreviousMessages()).thenReturn(Result.success(messages))
        whenever(repository.hasMorePreviousMessages()).thenReturn(true)

        val store = androidx.lifecycle.ViewModelStore()
        stores += store
        val vm = androidx.lifecycle.ViewModelProvider(
            store,
            object : androidx.lifecycle.ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T =
                    CometChatMessageListViewModel(repository = repository, enableListeners = true) as T
            }
        )[CometChatMessageListViewModel::class.java]
        vm.setUser(MockFactory.createUser(uid = peerUid, name = "Peer"))
        vm.fetchMessages()

        val listenerCaptor = argumentCaptor<CometChat.MessageListener>()
        cometChatMock.verify({ CometChat.addMessageListener(any(), listenerCaptor.capture()) }, atLeastOnce())
        return vm to listenerCaptor.lastValue
    }

    // Main is installed once for the whole spec, not per test. `applyPinSaveEcho` fires
    // CometChatEvents.emitMessageEvent, which launches on the bus's own Default-dispatcher scope —
    // a launch advanceUntilIdle() cannot see. With a per-test resetMain() that stray launch could
    // resume this spec's Main-bound collectors AFTER Main was torn down, crashing on
    // "Dispatchers.Main was accessed when the platform dispatcher was absent" and pinning the
    // leaked exception on the NEXT test's runTest (UncaughtExceptionsBeforeTest). Keeping Main set
    // across tests makes those late resumes harmless no-ops on cancelled collectors.
    beforeSpec {
        Dispatchers.setMain(testDispatcher)
    }

    afterSpec {
        // Let any still-queued bus emissions run while Main is still installed; every VM is already
        // cleared, so they resume nobody — this only closes the end-of-spec race window.
        Thread.sleep(100)
        Dispatchers.resetMain()
    }

    beforeTest {
        logMock = Mockito.mockStatic(Log::class.java)
        cometChatMock = Mockito.mockStatic(CometChat::class.java)
        repository = mock()
        whenever(repository.hasMorePreviousMessages()).thenReturn(true)
        whenever(repository.fetchPreviousMessages()).thenReturn(Result.success(emptyList()))
        whenever(repository.markAsRead(any())).thenReturn(Result.success(Unit))
        whenever(repository.markAsDelivered(any())).thenReturn(Result.success(Unit))
    }

    afterTest {
        // Clear VMs while the CometChat statics are still mocked (onCleared → removeListeners).
        stores.forEach { it.clear() }
        stores.clear()
        cometChatMock.close()
        logMock.close()
    }

    // Repo-convention guard: earlier specs in the same JVM can leak coroutine exceptions that
    // kotlinx-coroutines-test then pins on THIS spec's first runTest (UncaughtExceptionsBeforeTest).
    // This throwaway test absorbs them so the real vectors start clean — same pattern as the other
    // VM specs. Without it this spec is green in isolation but flaky in the full-suite run.
    test("warmup - absorb leaked exceptions") {
        try { runTest { } } catch (_: Exception) { }
    }

    // ==================== A. Partial echo ====================

    test("onMessageUnpinned clears the indicator even when the echo carries no routing fields") {
        runTest {
            val (vm, listener) = viewModelWith(listOf(message(id = 2L, pinnedAt = 1700000000L)))
            advanceUntilIdle()
            vm.getItems().single().isPinned shouldBe true

            listener.onMessageUnpinned(routinglessEcho(id = 2L))
            advanceUntilIdle()

            vm.getItems().single().isPinned shouldBe false
        }
    }

    test("onMessagePinned sets the indicator even when the echo carries no routing fields") {
        runTest {
            val (vm, listener) = viewModelWith(listOf(message(id = 2L)))
            advanceUntilIdle()

            listener.onMessagePinned(routinglessEcho(id = 2L, pinnedAt = 1700000000L))
            advanceUntilIdle()

            vm.getItems().single().isPinned shouldBe true
            vm.getItems().single().pinnedAt shouldBe 1700000000L
        }
    }

    test("onMessagePinned stamps a timestamp when the echo omits pinnedAt entirely") {
        runTest {
            val (vm, listener) = viewModelWith(listOf(message(id = 2L)))
            advanceUntilIdle()

            // pinnedAt = 0 on a "pinned" event: isPinned() is pinnedAt > 0, so trusting the echo
            // verbatim would leave the message reading as unpinned.
            listener.onMessagePinned(routinglessEcho(id = 2L, pinnedAt = 0L))
            advanceUntilIdle()

            vm.getItems().single().isPinned shouldBe true
        }
    }

    test("save/unsave echoes behave the same as pin/unpin") {
        runTest {
            val (vm, listener) = viewModelWith(listOf(message(id = 2L)))
            advanceUntilIdle()

            listener.onMessageSaved(routinglessEcho(id = 2L, savedAt = 1700000000L))
            advanceUntilIdle()
            vm.getItems().single().isSaved shouldBe true

            listener.onMessageUnsaved(routinglessEcho(id = 2L))
            advanceUntilIdle()
            vm.getItems().single().isSaved shouldBe false
        }
    }

    // ==================== B. Field preservation ====================

    test("the loaded message's own fields survive the update — the thin echo is not swapped in") {
        runTest {
            val (vm, listener) = viewModelWith(listOf(message(id = 2L, text = "hello", pinnedAt = 1700000000L)))
            advanceUntilIdle()

            listener.onMessageUnpinned(routinglessEcho(id = 2L))
            advanceUntilIdle()

            val updated = vm.getItems().single()
            (updated as TextMessage).text shouldBe "hello"
            updated.sentAt shouldBe 1735689600L
            updated.sender?.uid shouldBe peerUid
            updated.receiverUid shouldBe peerUid
            updated.isPinned shouldBe false
        }
    }

    test("only the targeted message changes") {
        runTest {
            val (vm, listener) = viewModelWith(
                listOf(
                    message(id = 1L, pinnedAt = 1700000000L),
                    message(id = 2L, pinnedAt = 1700000000L)
                )
            )
            advanceUntilIdle()

            listener.onMessageUnpinned(routinglessEcho(id = 2L))
            advanceUntilIdle()

            vm.getItems().first { it.id == 1L }.isPinned shouldBe true
            vm.getItems().first { it.id == 2L }.isPinned shouldBe false
        }
    }

    // ==================== C. Emission ====================

    test("the update is a distinct instance, so the messages StateFlow emits") {
        runTest {
            val original = message(id = 2L, pinnedAt = 1700000000L)
            val (vm, listener) = viewModelWith(listOf(original))
            advanceUntilIdle()

            val emissions = mutableListOf<List<BaseMessage>>()
            val collector = launch(testDispatcher) { vm.messages.collect { emissions.add(it) } }
            val before = emissions.size

            listener.onMessageUnpinned(routinglessEcho(id = 2L))
            advanceUntilIdle()

            // Mutating `original` in place would have made the new list equals() the old one and
            // StateFlow would have dropped the emission — the bubble would never rebind.
            emissions.size shouldBe before + 1
            emissions.last().single() shouldNotBe original
            emissions.last().single().isPinned shouldBe false
            original.isPinned shouldBe true
            collector.cancel()
        }
    }

    // ==================== D. Fallback ====================

    test("an echo for a message this list hasn't loaded leaves the list untouched") {
        runTest {
            val (vm, listener) = viewModelWith(listOf(message(id = 2L, pinnedAt = 1700000000L)))
            advanceUntilIdle()

            listener.onMessageUnpinned(routinglessEcho(id = 999L))
            advanceUntilIdle()

            vm.getItemCount() shouldBe 1
            vm.getItems().single().isPinned shouldBe true
        }
    }
})
