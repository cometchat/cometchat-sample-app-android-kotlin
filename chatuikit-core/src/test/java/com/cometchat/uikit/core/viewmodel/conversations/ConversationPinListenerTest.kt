package com.cometchat.uikit.core.viewmodel.conversations

import com.cometchat.chat.core.CometChat
import com.cometchat.chat.models.Conversation
import com.cometchat.uikit.core.domain.usecase.DeleteConversationUseCase
import com.cometchat.uikit.core.domain.usecase.GetConversationListUseCase
import com.cometchat.uikit.core.domain.usecase.RefreshConversationListUseCase
import com.cometchat.uikit.core.testutils.MockFactory
import com.cometchat.uikit.core.viewmodel.CometChatConversationsViewModel
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
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
 * Conversation pin/unpin realtime wiring in [CometChatConversationsViewModel].
 *
 * The gap this closes: the VM only updated the list from its OWN pin/unpin call's onSuccess. A pin
 * arriving via the SDK's [CometChat.ConversationListener] — the self-echo on this device, a second
 * device, or another surface in-app — changed nothing until a refetch. The VM now registers its own
 * ConversationListener and routes both paths through the same apply helpers.
 *
 * Harness notes (repo convention, see MessageListModerationUpdateTest): mockStatic(CometChat)
 * captures the listener the VM registers, then drives it directly. Conversation mocks don't carry
 * real pin state through setters, so the known-conversation tests stub `clone()` explicitly —
 * `withPinState` clones the LOADED instance rather than adopting the (partial) event object, and
 * instance identity is what these tests assert.
 *
 * Run: ./gradlew :chatuikit-core:testDebugUnitTest --tests "*ConversationPinListenerTest"
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ConversationPinListenerTest : FunSpec({

    isolationMode = IsolationMode.SingleInstance

    val testDispatcher = UnconfinedTestDispatcher()

    lateinit var getConversationListUseCase: GetConversationListUseCase
    lateinit var deleteConversationUseCase: DeleteConversationUseCase
    lateinit var refreshConversationListUseCase: RefreshConversationListUseCase
    lateinit var cometChatMock: MockedStatic<CometChat>

    // Every VM is hosted in a ViewModelStore that afterTest clears. These VMs run with listeners
    // ON, which launches viewModelScope collectors on the UIKit event bus — left uncleared they
    // outlive the spec and their leaked exceptions get pinned on the NEXT spec's first runTest
    // (UncaughtExceptionsBeforeTest in whatever suite happens to follow alphabetically).
    val stores = mutableListOf<androidx.lifecycle.ViewModelStore>()

    fun hostedViewModel(): CometChatConversationsViewModel {
        val store = androidx.lifecycle.ViewModelStore()
        stores += store
        return androidx.lifecycle.ViewModelProvider(
            store,
            object : androidx.lifecycle.ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T =
                    CometChatConversationsViewModel(
                        getConversationListUseCase,
                        deleteConversationUseCase,
                        refreshConversationListUseCase,
                        enableListeners = true
                    ) as T
            }
        )[CometChatConversationsViewModel::class.java]
    }

    /** VM with listeners ON, pre-loaded with [conversations], plus the captured SDK listener. */
    suspend fun createViewModelWithListener(
        conversations: List<Conversation>
    ): Pair<CometChatConversationsViewModel, CometChat.ConversationListener> {
        whenever(getConversationListUseCase.invoke(any())).thenReturn(Result.success(conversations))
        whenever(getConversationListUseCase.hasMore()).thenReturn(conversations.isNotEmpty())
        val vm = hostedViewModel()
        vm.fetchConversations()

        val captor = argumentCaptor<CometChat.ConversationListener>()
        cometChatMock.verify({ CometChat.addConversationListener(any(), captor.capture()) }, atLeastOnce())
        return vm to captor.lastValue
    }

    beforeTest {
        Dispatchers.setMain(testDispatcher)
        cometChatMock = Mockito.mockStatic(CometChat::class.java)
        getConversationListUseCase = mock()
        deleteConversationUseCase = mock()
        refreshConversationListUseCase = mock()
        println("  🧪 ${it.name.testName}")
    }

    afterTest {
        // Clear VMs while the CometChat statics are still mocked (onCleared → removeListeners).
        stores.forEach { it.clear() }
        stores.clear()
        cometChatMock.close()
        Dispatchers.resetMain()
        println()
    }

    test("warmup - absorb leaked exceptions") {
        try { runTest { } } catch (_: Exception) { }
    }

    // ==================== A. Registration lifecycle ====================

    test("with listeners enabled the VM registers a ConversationListener under its own tag") {
        runTest {
            createViewModelWithListener(MockFactory.createUserConversations(1))
            advanceUntilIdle()
            // Captured in the helper — reaching here proves registration happened.
        }
    }

    test("clearing the VM removes the ConversationListener under the same tag") {
        runTest {
            val store = androidx.lifecycle.ViewModelStore()
            whenever(getConversationListUseCase.invoke(any())).thenReturn(Result.success(emptyList()))
            whenever(getConversationListUseCase.hasMore()).thenReturn(false)
            val provider = androidx.lifecycle.ViewModelProvider(
                store,
                object : androidx.lifecycle.ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T =
                        CometChatConversationsViewModel(
                            getConversationListUseCase,
                            deleteConversationUseCase,
                            refreshConversationListUseCase,
                            enableListeners = true
                        ) as T
                }
            )
            provider[CometChatConversationsViewModel::class.java]

            val tagCaptor = argumentCaptor<String>()
            cometChatMock.verify(
                { CometChat.addConversationListener(tagCaptor.capture(), any()) },
                atLeastOnce()
            )
            val tag = tagCaptor.lastValue

            store.clear()

            cometChatMock.verify { CometChat.removeConversationListener(tag) }
        }
    }

    // ==================== B. onConversationPinned ====================

    test("pinned event for a loaded conversation moves its clone to the top, dropping the old instance") {
        runTest {
            val conversations = MockFactory.createUserConversations(3)
            val target = conversations[2]
            val pinnedClone = MockFactory.createUserConversation(uid = "user-3", name = "User 3")
            whenever(target.clone()).thenReturn(pinnedClone)
            val (vm, listener) = createViewModelWithListener(conversations)
            advanceUntilIdle()

            // The event delivers the SDK's (partial) response object — same id, different instance.
            listener.onConversationPinned(
                MockFactory.createUserConversation(uid = "user-3", name = "User 3")
            )

            vm.conversations.value shouldHaveSize 3
            vm.conversations.value[0] shouldBeSameInstanceAs pinnedClone
            vm.conversations.value.count { it.conversationId == target.conversationId } shouldBe 1
        }
    }

    test("pinned event for a conversation this list has not loaded inserts it at the top") {
        runTest {
            val (vm, listener) = createViewModelWithListener(MockFactory.createUserConversations(2))
            advanceUntilIdle()

            val unknown = MockFactory.createUserConversation(uid = "user-99", name = "New Peer")
            listener.onConversationPinned(unknown)

            vm.conversations.value shouldHaveSize 3
            vm.conversations.value[0] shouldBeSameInstanceAs unknown
        }
    }

    test("pinned event delivered twice is idempotent — same size, same top identity") {
        runTest {
            val (vm, listener) = createViewModelWithListener(MockFactory.createUserConversations(2))
            advanceUntilIdle()

            val unknown = MockFactory.createUserConversation(uid = "user-99", name = "New Peer")
            whenever(unknown.clone()).thenReturn(unknown)

            // Acting device gets the SDK self-echo AND its own REST onSuccess with no suppression
            // registry between them — double application must not duplicate the row.
            listener.onConversationPinned(unknown)
            listener.onConversationPinned(unknown)

            vm.conversations.value shouldHaveSize 3
            vm.conversations.value[0] shouldBeSameInstanceAs unknown
            vm.conversations.value.count { it.conversationId == unknown.conversationId } shouldBe 1
        }
    }

    // ==================== C. onConversationUnpinned ====================

    test("unpinned event repositions the clone to the top of the unpinned section") {
        runTest {
            // list: [pinnedA (stays pinned), target (being unpinned), plainB]
            val pinnedA = MockFactory.createUserConversation(uid = "user-a", name = "A")
            whenever(pinnedA.isPinned).thenReturn(true)
            val target = MockFactory.createUserConversation(uid = "user-t", name = "T")
            val plainB = MockFactory.createUserConversation(uid = "user-b", name = "B")
            val unpinnedClone = MockFactory.createUserConversation(uid = "user-t", name = "T")
            whenever(target.clone()).thenReturn(unpinnedClone)

            val (vm, listener) = createViewModelWithListener(listOf(pinnedA, target, plainB))
            advanceUntilIdle()

            listener.onConversationUnpinned(
                MockFactory.createUserConversation(uid = "user-t", name = "T")
            )

            // Moves below the pinned block — index 1, between pinnedA and plainB.
            vm.conversations.value.map { it.conversationId } shouldBe
                listOf(pinnedA, unpinnedClone, plainB).map { it.conversationId }
            vm.conversations.value[1] shouldBeSameInstanceAs unpinnedClone
        }
    }

    test("unpinned event for an unknown conversation is ignored") {
        runTest {
            val conversations = MockFactory.createUserConversations(2)
            val (vm, listener) = createViewModelWithListener(conversations)
            advanceUntilIdle()
            val before = vm.conversations.value

            listener.onConversationUnpinned(
                MockFactory.createUserConversation(uid = "user-99", name = "Nobody")
            )

            vm.conversations.value shouldBe before
        }
    }
})
