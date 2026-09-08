package com.cometchat.uikit.core.viewmodel.messagelist

import android.util.Log
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.MessageReceipt
import com.cometchat.chat.models.TextMessage
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.domain.repository.MessageListRepository
import com.cometchat.uikit.core.testutils.MockFactory
import com.cometchat.uikit.core.viewmodel.CometChatMessageListViewModel
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
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
 * A read/delivered receipt must reach the UI, not just the model.
 *
 * `messages` is a [kotlinx.coroutines.flow.StateFlow], which conflates: assigning a value that
 * `equals` the current one emits nothing. `setReadReceipts`/`setDeliveryReceipts` shallow-copy the
 * list and mutate the SAME BaseMessage instances in place, so the "new" list is element-wise equal
 * to the old one and the emission is dropped — the sender's tick never turns blue until something
 * else rebinds the row. This is the same trap `applyPinSaveEcho` documents and avoids by cloning.
 *
 * These tests assert the OBSERVABLE behaviour (a collector sees the change), not just the field
 * value, so they fail on the conflated version and pass once the appliers clone.
 *
 * Run: ./gradlew :chatuikit-core:testDebugUnitTest --tests "*MessageListReceiptEmissionTest"
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MessageListReceiptEmissionTest : FunSpec({

    val testDispatcher = UnconfinedTestDispatcher()
    val peerUid = "peer-1"
    val myUid = "me"

    lateinit var repository: MessageListRepository
    lateinit var logMock: MockedStatic<Log>
    lateinit var cometChatMock: MockedStatic<CometChat>

    /** A message I sent to the peer — the one whose ticks must flip. */
    fun myMessage(id: Long) = TextMessage(peerUid, "msg-$id", CometChatConstants.RECEIVER_TYPE_USER).apply {
        this.id = id
        this.sentAt = 1735689600L
        this.sender = User().apply { uid = myUid; name = "Me" }
        this.receiverUid = peerUid
    }

    fun receipt(messageId: Long, type: String) = mock<MessageReceipt>().also {
        whenever(it.messageId).thenReturn(messageId)
        whenever(it.receiverType).thenReturn(CometChatConstants.RECEIVER_TYPE_USER)
        whenever(it.receiptType).thenReturn(type)
        whenever(it.sender).thenReturn(User().apply { uid = peerUid })
        whenever(it.readAt).thenReturn(1735689999L)
        whenever(it.deliveredAt).thenReturn(1735689888L)
    }

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

        val captor = argumentCaptor<CometChat.MessageListener>()
        cometChatMock.verify({ CometChat.addMessageListener(any(), captor.capture()) }, atLeastOnce())
        return vm to captor.lastValue
    }

    beforeSpec { Dispatchers.setMain(testDispatcher) }

    afterSpec {
        Thread.sleep(100)
        Dispatchers.resetMain()
    }

    beforeTest {
        logMock = Mockito.mockStatic(Log::class.java)
        cometChatMock = Mockito.mockStatic(CometChat::class.java)
        cometChatMock.`when`<User> { CometChat.getLoggedInUser() }
            .thenReturn(User().apply { uid = myUid; name = "Me" })
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

    test("a READ receipt emits a new list, so the sender's bubble rebinds") {
        runTest {
            val (vm, listener) = viewModelWith(listOf(myMessage(1L), myMessage(2L)))
            advanceUntilIdle()

            val seen = mutableListOf<List<BaseMessage>>()
            val job = launch { vm.messages.collect { seen += it } }
            advanceUntilIdle()
            val emissionsBefore = seen.size

            listener.onMessagesRead(receipt(2L, MessageReceipt.RECEIPT_TYPE_READ))
            advanceUntilIdle()

            // The model must carry the receipt...
            vm.getItems().last().readAt shouldBe 1735689999L
            // ...AND the collector must have been told, or no bubble ever rebinds.
            (seen.size > emissionsBefore) shouldBe true
            job.cancel()
        }
    }

    test("a DELIVERED receipt emits a new list too") {
        runTest {
            val (vm, listener) = viewModelWith(listOf(myMessage(1L), myMessage(2L)))
            advanceUntilIdle()

            val seen = mutableListOf<List<BaseMessage>>()
            val job = launch { vm.messages.collect { seen += it } }
            advanceUntilIdle()
            val emissionsBefore = seen.size

            listener.onMessagesDelivered(receipt(2L, MessageReceipt.RECEIPT_TYPE_DELIVERED))
            advanceUntilIdle()

            vm.getItems().last().deliveredAt shouldBe 1735689888L
            (seen.size > emissionsBefore) shouldBe true
            job.cancel()
        }
    }

    test("the receipt does not mutate the instance the previous emission handed out") {
        runTest {
            val (vm, listener) = viewModelWith(listOf(myMessage(1L)))
            advanceUntilIdle()
            val beforeInstance = vm.getItems().last()

            listener.onMessagesRead(receipt(1L, MessageReceipt.RECEIPT_TYPE_READ))
            advanceUntilIdle()

            // A fresh instance carries the change; the one already handed to the UI is untouched,
            // which is what makes the two lists unequal and the emission survive conflation.
            beforeInstance.readAt shouldBe 0L
            vm.getItems().last().readAt shouldBe 1735689999L
        }
    }
})
