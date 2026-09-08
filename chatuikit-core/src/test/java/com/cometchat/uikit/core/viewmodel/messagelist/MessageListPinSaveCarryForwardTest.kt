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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
 * Pin/save state survives non-pin/save replacements in [CometChatMessageListViewModel].
 *
 * Regression for ENG-38915 (saved icon disappears after editing): an edit's REST response and
 * realtime frame carry the new content but not the viewer's `savedAt` (and often no `pinnedAt`),
 * and the list swapped that payload in wholesale. Now every replacement that is not itself a
 * pin/save event carries the loaded state forward; the pin/save events stay authoritative so an
 * unpin/unsave can still clear.
 *
 * Same scaffold as [MessageListPinSaveEchoTest] (real SDK models, captured SDK listener).
 *
 * Run: ./gradlew :chatuikit-core:testDebugUnitTest --tests "*MessageListPinSaveCarryForwardTest"
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MessageListPinSaveCarryForwardTest : FunSpec({

    val testDispatcher = UnconfinedTestDispatcher()
    val peerUid = "peer-1"

    lateinit var repository: MessageListRepository
    lateinit var logMock: MockedStatic<Log>
    lateinit var cometChatMock: MockedStatic<CometChat>

    fun message(id: Long, text: String = "msg-$id", pinnedAt: Long = 0L, pinnedBy: String? = null, savedAt: Long = 0L) =
        TextMessage(peerUid, text, CometChatConstants.RECEIVER_TYPE_USER).apply {
            this.id = id
            this.sentAt = 1735689600L
            this.sender = User().apply { uid = peerUid; name = "Peer" }
            this.pinnedAt = pinnedAt
            this.pinnedBy = pinnedBy
            this.savedAt = savedAt
        }

    /** What the edit REST response / realtime frame parses into: new text, no pin/save keys. */
    fun editedFrame(id: Long, text: String) = message(id, text = text).apply { editedAt = 1735689700L }

    val stores = mutableListOf<androidx.lifecycle.ViewModelStore>()

    suspend fun viewModelWith(messages: List<BaseMessage>): Pair<CometChatMessageListViewModel, CometChat.MessageListener> {
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

    beforeSpec { Dispatchers.setMain(testDispatcher) }

    afterSpec {
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
        stores.forEach { it.clear() }
        stores.clear()
        cometChatMock.close()
        logMock.close()
    }

    test("warmup - absorb leaked exceptions") {
        try { runTest { } } catch (_: Exception) { }
    }

    test("realtime edit of a SAVED message keeps the saved indicator") {
        runTest {
            val (vm, listener) = viewModelWith(listOf(message(2L, savedAt = 1700000000L)))
            advanceUntilIdle()

            listener.onMessageEdited(editedFrame(2L, "edited text"))
            advanceUntilIdle()

            val row = vm.getItems().single()
            (row as TextMessage).text shouldBe "edited text"
            row.isSaved shouldBe true
            row.savedAt shouldBe 1700000000L
        }
    }

    test("realtime edit of a PINNED message keeps pinnedAt and pinnedBy") {
        runTest {
            val (vm, listener) = viewModelWith(listOf(message(2L, pinnedAt = 1700000000L, pinnedBy = "admin")))
            advanceUntilIdle()

            listener.onMessageEdited(editedFrame(2L, "edited text"))
            advanceUntilIdle()

            val row = vm.getItems().single()
            row.isPinned shouldBe true
            row.pinnedBy shouldBe "admin"
        }
    }

    test("updateMessage (own edit / reaction / translate paths) keeps the pin/save state") {
        runTest {
            val (vm, _) = viewModelWith(listOf(message(2L, pinnedAt = 1700000000L, savedAt = 1700000001L)))
            advanceUntilIdle()

            vm.updateMessage(editedFrame(2L, "edited text"))
            advanceUntilIdle()

            val row = vm.getItems().single()
            row.isPinned shouldBe true
            row.isSaved shouldBe true
        }
    }

    test("an unpin / unsave echo after an edit still clears — the pin/save events stay authoritative") {
        runTest {
            val (vm, listener) = viewModelWith(listOf(message(2L, pinnedAt = 1700000000L, savedAt = 1700000001L)))
            advanceUntilIdle()

            listener.onMessageEdited(editedFrame(2L, "edited text"))
            advanceUntilIdle()
            vm.getItems().single().isPinned shouldBe true

            listener.onMessageUnpinned(message(2L))
            listener.onMessageUnsaved(message(2L))
            advanceUntilIdle()

            val row = vm.getItems().single()
            row.isPinned shouldBe false
            row.isSaved shouldBe false
        }
    }

    test("an edit that arrives with fresh pin/save values keeps the server's values") {
        runTest {
            val (vm, listener) = viewModelWith(listOf(message(2L, pinnedAt = 100L, pinnedBy = "admin")))
            advanceUntilIdle()

            listener.onMessageEdited(message(2L, text = "edited", pinnedAt = 200L, pinnedBy = "peer-1"))
            advanceUntilIdle()

            val row = vm.getItems().single()
            row.pinnedAt shouldBe 200L
            row.pinnedBy shouldBe "peer-1"
        }
    }
})
