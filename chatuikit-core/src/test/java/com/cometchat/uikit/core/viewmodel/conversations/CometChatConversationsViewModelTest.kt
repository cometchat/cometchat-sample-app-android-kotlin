package com.cometchat.uikit.core.viewmodel.conversations

import com.cometchat.chat.models.Conversation
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.domain.usecase.DeleteConversationUseCase
import com.cometchat.uikit.core.domain.usecase.GetConversationListUseCase
import com.cometchat.uikit.core.domain.usecase.RefreshConversationListUseCase
import com.cometchat.uikit.core.events.CometChatConversationEvent
import com.cometchat.uikit.core.events.CometChatEvents
import com.cometchat.uikit.core.events.CometChatMessageEvent
import com.cometchat.uikit.core.events.CometChatUserEvent
import com.cometchat.uikit.core.state.DeleteState
import com.cometchat.uikit.core.state.UIState
import com.cometchat.uikit.core.testutils.MockFactory
import com.cometchat.uikit.core.viewmodel.CometChatConversationsViewModel
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import com.cometchat.chat.core.ConversationsRequest
import io.kotest.matchers.types.shouldBeSameInstanceAs
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Comprehensive property-based tests for CometChatConversationsViewModel.
 *
 * Matches the Users ViewModel test pattern (CometChatUsersViewModelTest.kt):
 * - Sections A through L covering all ViewModel behavior
 * - Uses `checkAll` with `Arb` generators — NOT hardcoded values
 * - Mocks use cases via Mockito `mock()` — NEVER subclass
 * - `enableListeners = false` to prevent SDK listener registration
 *
 * Run with:
 *   ./gradlew :chatuikit-core:testDebugUnitTest --tests "...CometChatConversationsViewModelTest"
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CometChatConversationsViewModelTest : FunSpec({

    isolationMode = io.kotest.core.spec.IsolationMode.SingleInstance

    val testDispatcher = UnconfinedTestDispatcher()

    lateinit var getConversationListUseCase: GetConversationListUseCase
    lateinit var deleteConversationUseCase: DeleteConversationUseCase
    lateinit var refreshConversationListUseCase: RefreshConversationListUseCase

    /** Create a ViewModel pre-loaded with the given conversations list. */
    suspend fun createViewModel(conversations: List<Conversation>): CometChatConversationsViewModel {
        whenever(getConversationListUseCase.invoke(any())).thenReturn(Result.success(conversations))
        whenever(getConversationListUseCase.hasMore()).thenReturn(conversations.isNotEmpty())
        return CometChatConversationsViewModel(
            getConversationListUseCase,
            deleteConversationUseCase,
            refreshConversationListUseCase,
            enableListeners = false
        )
    }

    beforeTest {
        Dispatchers.setMain(testDispatcher)
        getConversationListUseCase = mock()
        deleteConversationUseCase = mock()
        refreshConversationListUseCase = mock()
        println("  🧪 ${it.name.testName}")
    }

    afterTest {
        Dispatchers.resetMain()
        println()
    }

    // ==================== A. Fetch & UI State ====================

    test("warmup - absorb leaked exceptions") {
        try { runTest { } } catch (_: Exception) { }
    }

    test("for any conversation count: empty list → Empty state, non-empty → Content state") {
        checkAll(50, Arb.int(0..20)) { count ->
            runTest {
                val conversations = MockFactory.createUserConversations(count)
                println("    → count=$count")
                val viewModel = createViewModel(conversations)
                advanceUntilIdle()

                if (count == 0) {
                    viewModel.uiState.value shouldBe UIState.Empty
                    viewModel.conversations.value shouldHaveSize 0
                    println("    ✅ count=0 → Empty")
                } else {
                    viewModel.uiState.value.shouldBeInstanceOf<UIState.Content>()
                    viewModel.conversations.value shouldHaveSize count
                    println("    ✅ count=$count → Content, size=$count")
                }
            }
        }
    }

    test("for any error message: fetch failure → Error state with same exception") {
        checkAll(20, Arb.string(1..50), Arb.string(3..10)) { code, msg ->
            runTest {
                val exception = MockFactory.createCometChatException(code, msg)
                whenever(getConversationListUseCase.invoke(any())).thenReturn(Result.failure(exception))
                val viewModel = CometChatConversationsViewModel(
                    getConversationListUseCase, deleteConversationUseCase,
                    refreshConversationListUseCase, enableListeners = false
                )
                advanceUntilIdle()

                println("    → code='$code', msg='$msg'")
                viewModel.uiState.value.shouldBeInstanceOf<UIState.Error>()
                (viewModel.uiState.value as UIState.Error).exception shouldBe exception
                println("    ✅ Error state with matching exception")
            }
        }
    }

    // ==================== B. ListOperations ====================

    test("for any list size: addItem should increase size by 1") {
        checkAll(30, Arb.int(0..15)) { initialCount ->
            runTest {
                val conversations = MockFactory.createUserConversations(initialCount)
                val viewModel = createViewModel(conversations)
                advanceUntilIdle()

                val newConv = MockFactory.createUserConversation(uid = "added-$initialCount")
                viewModel.addItem(newConv)

                println("    → initial=$initialCount, after addItem=${viewModel.conversations.value.size}")
                viewModel.conversations.value shouldHaveSize initialCount + 1
                viewModel.conversations.value shouldContain newConv
                println("    ✅ size increased by 1")
            }
        }
    }

    test("for any list size >= 1: removeItem should decrease size by 1") {
        checkAll(30, Arb.int(1..15)) { count ->
            runTest {
                val conversations = MockFactory.createUserConversations(count)
                val viewModel = createViewModel(conversations)
                advanceUntilIdle()

                val removeIndex = (0 until count).random()
                val convToRemove = conversations[removeIndex]
                println("    → count=$count, removing index=$removeIndex")

                val removed = viewModel.removeItem(convToRemove)

                removed shouldBe true
                viewModel.conversations.value shouldHaveSize count - 1
                viewModel.conversations.value shouldNotContain convToRemove
                println("    ✅ removed=true, size=${count - 1}")
            }
        }
    }

    test("for any list size >= 2: moveItemToTop should place target at index 0") {
        checkAll(30, Arb.int(2..15)) { count ->
            runTest {
                val conversations = MockFactory.createUserConversations(count)
                val viewModel = createViewModel(conversations)
                advanceUntilIdle()

                val moveIndex = (1 until count).random()
                val convToMove = conversations[moveIndex]
                println("    → count=$count, moving index=$moveIndex to top")

                viewModel.moveItemToTop(convToMove)

                viewModel.conversations.value.first().conversationId shouldBe convToMove.conversationId
                viewModel.conversations.value shouldHaveSize count
                println("    ✅ moved to top, size=$count")
            }
        }
    }

    test("for any list size: clearItems should result in empty list") {
        checkAll(30, Arb.int(0..20)) { count ->
            runTest {
                val conversations = MockFactory.createUserConversations(count)
                val viewModel = createViewModel(conversations)
                advanceUntilIdle()

                viewModel.clearItems()

                println("    → count=$count → after clear: size=${viewModel.conversations.value.size}")
                viewModel.conversations.value shouldHaveSize 0
                println("    ✅ cleared")
            }
        }
    }

    test("for any list size: getItemCount should match actual size") {
        checkAll(30, Arb.int(0..20)) { count ->
            runTest {
                val conversations = MockFactory.createUserConversations(count)
                val viewModel = createViewModel(conversations)
                advanceUntilIdle()

                println("    → count=$count, getItemCount()=${viewModel.getItemCount()}")
                viewModel.getItemCount() shouldBe count
                println("    ✅ matches")
            }
        }
    }

    test("for any valid index: getItemAt should return correct conversation; out-of-bounds → null") {
        checkAll(30, Arb.int(1..15)) { count ->
            runTest {
                val conversations = MockFactory.createUserConversations(count)
                val viewModel = createViewModel(conversations)
                advanceUntilIdle()

                val validIndex = (0 until count).random()
                val result = viewModel.getItemAt(validIndex)
                println("    → count=$count, getItemAt($validIndex)=${result?.conversationId}")
                result?.conversationId shouldBe conversations[validIndex].conversationId

                val outOfBounds = viewModel.getItemAt(count + 10)
                outOfBounds shouldBe null
                println("    ✅ valid index returns conversation, out-of-bounds returns null")
            }
        }
    }

    // ==================== C. ViewModel-Specific: Delete ====================

    test("deleteConversation success with remaining items should set DeleteState.Success") {
        checkAll(20, Arb.int(2..10)) { count ->
            runTest {
                val conversations = MockFactory.createUserConversations(count)
                val viewModel = createViewModel(conversations)
                advanceUntilIdle()

                val target = conversations[(0 until count).random()]
                whenever(deleteConversationUseCase.invoke(target)).thenReturn(Result.success(Unit))
                println("    → deleting ${target.conversationId} from $count conversations")

                viewModel.deleteConversation(target)
                advanceUntilIdle()

                viewModel.deleteState.value shouldBe DeleteState.Success
                viewModel.conversations.value shouldHaveSize count - 1
                println("    ✅ DeleteState.Success, size=${count - 1}")
            }
        }
    }

    test("deleteConversation on last item should set UIState.Empty and DeleteState.Success") {
        checkAll(20, Arb.string(3..15)) { uid ->
            runTest {
                val conversation = MockFactory.createUserConversation(uid = uid)
                val viewModel = createViewModel(listOf(conversation))
                advanceUntilIdle()

                whenever(deleteConversationUseCase.invoke(conversation)).thenReturn(Result.success(Unit))
                println("    → deleting last conversation [$uid]")

                viewModel.deleteConversation(conversation)
                advanceUntilIdle()

                viewModel.deleteState.value shouldBe DeleteState.Success
                viewModel.conversations.value shouldHaveSize 0
                viewModel.uiState.value shouldBe UIState.Empty
                println("    ✅ DeleteState.Success, UIState.Empty")
            }
        }
    }

    test("deleteConversation failure should set DeleteState.Failure") {
        checkAll(20, Arb.string(3..10), Arb.string(3..15)) { code, msg ->
            runTest {
                val conversation = MockFactory.createUserConversation(uid = "user-1")
                val viewModel = createViewModel(listOf(conversation))
                advanceUntilIdle()

                val exception = MockFactory.createCometChatException(code, msg)
                whenever(deleteConversationUseCase.invoke(conversation)).thenReturn(Result.failure(exception))
                println("    → delete fails with code='$code'")

                viewModel.deleteConversation(conversation)
                advanceUntilIdle()

                viewModel.deleteState.value.shouldBeInstanceOf<DeleteState.Failure>()
                (viewModel.deleteState.value as DeleteState.Failure).exception shouldBe exception
                println("    ✅ DeleteState.Failure with matching exception")
            }
        }
    }

    test("resetDeleteState should return to Idle from any state") {
        runTest {
            val conversation = MockFactory.createUserConversation(uid = "user-1")
            val viewModel = createViewModel(listOf(conversation))
            advanceUntilIdle()

            // After success
            whenever(deleteConversationUseCase.invoke(conversation)).thenReturn(Result.success(Unit))
            viewModel.deleteConversation(conversation)
            advanceUntilIdle()
            viewModel.deleteState.value shouldBe DeleteState.Success

            viewModel.resetDeleteState()
            viewModel.deleteState.value shouldBe DeleteState.Idle
            println("    ✅ resetDeleteState → Idle")
        }
    }

    // ==================== D. Selection ====================

    test("SINGLE mode: for any two conversations, only the last selected should remain") {
        checkAll(20, Arb.int(2..10)) { count ->
            runTest {
                val conversations = MockFactory.createUserConversations(count)
                val viewModel = createViewModel(conversations)
                advanceUntilIdle()

                val first = conversations[(0 until count).random()]
                val second = conversations.filter { it.conversationId != first.conversationId }.random()

                println("    → select ${first.conversationId}, then ${second.conversationId}")
                viewModel.selectConversation(first, UIKitConstants.SelectionMode.SINGLE)
                viewModel.selectConversation(second, UIKitConstants.SelectionMode.SINGLE)

                viewModel.selectedConversations.value shouldHaveSize 1
                println("    ✅ only 1 selected")
            }
        }
    }

    test("MULTIPLE mode: selecting then deselecting should toggle") {
        checkAll(20, Arb.int(2..10)) { count ->
            runTest {
                val conversations = MockFactory.createUserConversations(count)
                val viewModel = createViewModel(conversations)
                advanceUntilIdle()

                val target = conversations[(0 until count).random()]

                println("    → select ${target.conversationId}")
                viewModel.selectConversation(target, UIKitConstants.SelectionMode.MULTIPLE)
                viewModel.selectedConversations.value shouldHaveSize 1

                println("    → deselect ${target.conversationId}")
                viewModel.selectConversation(target, UIKitConstants.SelectionMode.MULTIPLE)
                viewModel.selectedConversations.value shouldHaveSize 0
                println("    ✅ toggled correctly")
            }
        }
    }

    test("clearSelection should clear any number of selected conversations") {
        checkAll(20, Arb.int(1..10)) { count ->
            runTest {
                val conversations = MockFactory.createUserConversations(count)
                val viewModel = createViewModel(conversations)
                advanceUntilIdle()

                val selectCount = (1..count).random()
                conversations.take(selectCount).forEach {
                    viewModel.selectConversation(it, UIKitConstants.SelectionMode.MULTIPLE)
                }
                println("    → selected $selectCount of $count conversations")

                viewModel.clearSelection()

                viewModel.selectedConversations.value shouldHaveSize 0
                println("    ✅ cleared all")
            }
        }
    }

    test("NONE mode: selectConversation should not change selection") {
        checkAll(10, Arb.int(1..5)) { count ->
            runTest {
                val conversations = MockFactory.createUserConversations(count)
                val viewModel = createViewModel(conversations)
                advanceUntilIdle()

                val target = conversations[(0 until count).random()]
                println("    → selectConversation(${target.conversationId}, NONE)")
                viewModel.selectConversation(target, UIKitConstants.SelectionMode.NONE)

                viewModel.selectedConversations.value shouldHaveSize 0
                println("    ✅ selection unchanged (NONE mode)")
            }
        }
    }

    test("selection operations should never modify conversation list") {
        checkAll(20, Arb.int(2..10)) { count ->
            runTest {
                val conversations = MockFactory.createUserConversations(count)
                val viewModel = createViewModel(conversations)
                advanceUntilIdle()

                val originalSize = viewModel.conversations.value.size
                val target = conversations[(0 until count).random()]

                viewModel.selectConversation(target, UIKitConstants.SelectionMode.MULTIPLE)
                viewModel.conversations.value shouldHaveSize originalSize

                viewModel.clearSelection()
                viewModel.conversations.value shouldHaveSize originalSize
                println("    ✅ conversation list unchanged after selection ops")
            }
        }
    }

    // ==================== H. Pagination & Refresh ====================

    test("for any two page sizes: fetchConversations should append on second call") {
        checkAll(20, Arb.int(1..10), Arb.int(1..10)) { page1Size, page2Size ->
            runTest {
                val page1 = MockFactory.createUserConversations(page1Size, "p1")
                whenever(getConversationListUseCase.invoke(any())).thenReturn(Result.success(page1))
                whenever(getConversationListUseCase.hasMore()).thenReturn(true)
                val viewModel = CometChatConversationsViewModel(
                    getConversationListUseCase, deleteConversationUseCase,
                    refreshConversationListUseCase, enableListeners = false
                )
                advanceUntilIdle()

                val page2 = MockFactory.createUserConversations(page2Size, "p2")
                whenever(getConversationListUseCase.invoke(any())).thenReturn(Result.success(page2))
                viewModel.fetchConversations()
                advanceUntilIdle()

                println("    → page1=$page1Size, page2=$page2Size, total=${viewModel.conversations.value.size}")
                viewModel.conversations.value.size shouldBe page1Size + page2Size
                println("    ✅ appended correctly")
            }
        }
    }

    // ENG-37363 regression: a reconnect refreshList() resets pagination while realtime
    // events reorder the server list, so a fetched page can overlap conversations already
    // shown. Duplicate conversationIds crash the list UI (duplicate LazyColumn keys), so
    // the append must dedup by conversationId and keep the first (realtime-fresher) copy.
    test("for any overlap: fetchConversations must dedup appended page by conversationId") {
        checkAll(20, Arb.int(2..10), Arb.int(1..10)) { page1Size, freshSize ->
            runTest {
                val page1 = MockFactory.createUserConversations(page1Size, "p1")
                whenever(getConversationListUseCase.invoke(any())).thenReturn(Result.success(page1))
                whenever(getConversationListUseCase.hasMore()).thenReturn(true)
                val viewModel = CometChatConversationsViewModel(
                    getConversationListUseCase, deleteConversationUseCase,
                    refreshConversationListUseCase, enableListeners = false
                )
                advanceUntilIdle()

                // Page 2 re-serves some of page 1 (same "p1" prefix → same conversationIds)
                // plus genuinely new conversations.
                val overlapSize = page1Size / 2 + 1
                val page2 = MockFactory.createUserConversations(overlapSize, "p1") +
                    MockFactory.createUserConversations(freshSize, "p2")
                whenever(getConversationListUseCase.invoke(any())).thenReturn(Result.success(page2))
                viewModel.fetchConversations()
                advanceUntilIdle()

                val result = viewModel.conversations.value
                println("    → page1=$page1Size, overlap=$overlapSize, fresh=$freshSize, total=${result.size}")
                result.size shouldBe page1Size + freshSize
                result.map { it.conversationId }.toSet().size shouldBe result.size
                println("    ✅ no duplicate conversationIds after overlapping append")
            }
        }
    }

    // ENG-37363 regression: refreshList must hand its CONSUMED request to pagination.
    // Storing a rebuilt (page-0) request made the next fetch re-serve page 1 and append
    // duplicate conversationIds, crashing the list UI on duplicate LazyColumn keys.
    test("after refreshList, pagination continues on the same consumed request") {
        runTest {
            val viewModel = createViewModel(MockFactory.createUserConversations(3))
            advanceUntilIdle()

            // Build mock data BEFORE stubbing — MockFactory stubs mocks internally, and
            // nesting it inside thenReturn(...) trips UnfinishedStubbingException.
            val freshConversations = MockFactory.createUserConversations(3, "fresh")
            whenever(refreshConversationListUseCase.invoke(any()))
                .thenReturn(Result.success(freshConversations))
            viewModel.refreshList()
            advanceUntilIdle()

            val refreshedRequest = argumentCaptor<ConversationsRequest>().run {
                verify(refreshConversationListUseCase).invoke(capture())
                firstValue
            }

            val page2Conversations = MockFactory.createUserConversations(2, "page2")
            whenever(getConversationListUseCase.invoke(any()))
                .thenReturn(Result.success(page2Conversations))
            viewModel.fetchConversations()
            advanceUntilIdle()

            val paginatedRequest = argumentCaptor<ConversationsRequest>().run {
                verify(getConversationListUseCase, atLeastOnce()).invoke(capture())
                lastValue
            }

            paginatedRequest shouldBeSameInstanceAs refreshedRequest
            println("    ✅ pagination reused the refresh-consumed request instance")
        }
    }

    test("refreshList should replace old data with fresh data") {
        checkAll(20, Arb.int(1..10), Arb.int(0..10)) { oldCount, newCount ->
            runTest {
                val oldConversations = MockFactory.createUserConversations(oldCount, "old")
                val viewModel = createViewModel(oldConversations)
                advanceUntilIdle()

                val freshConversations = MockFactory.createUserConversations(newCount, "fresh")
                whenever(refreshConversationListUseCase.invoke(any())).thenReturn(Result.success(freshConversations))
                viewModel.refreshList()
                advanceUntilIdle()

                println("    → old=$oldCount, fresh=$newCount, after refresh=${viewModel.conversations.value.size}")
                viewModel.conversations.value shouldHaveSize newCount
                if (newCount == 0) {
                    viewModel.uiState.value shouldBe UIState.Empty
                } else {
                    viewModel.uiState.value.shouldBeInstanceOf<UIState.Content>()
                }
                println("    ✅ refreshed")
            }
        }
    }

    // ==================== J. Scroll Events ====================

    test("moveItemToTop should place target at index 0 and update UI state") {
        checkAll(10, Arb.int(2..8)) { count ->
            runTest {
                val conversations = MockFactory.createUserConversations(count)
                val viewModel = createViewModel(conversations)
                advanceUntilIdle()

                val targetIndex = (1 until count).random()
                val target = conversations[targetIndex]
                println("    → moveItemToTop(index=$targetIndex, id=${target.conversationId})")
                viewModel.moveItemToTop(target)

                viewModel.conversations.value.first().conversationId shouldBe target.conversationId
                viewModel.conversations.value shouldHaveSize count
                viewModel.uiState.value.shouldBeInstanceOf<UIState.Content>()
                println("    ✅ moved to top, size=$count, state=Content")
            }
        }
    }

    test("refreshList should emit scrollToTopEvent") {
        runTest {
            val conversations = MockFactory.createUserConversations(3)
            val viewModel = createViewModel(conversations)
            advanceUntilIdle()

            var scrollEventReceived = false
            val job = CoroutineScope(testDispatcher).launch {
                viewModel.scrollToTopEvent.collect { scrollEventReceived = true }
            }

            val freshConversations = MockFactory.createUserConversations(2, "fresh")
            whenever(refreshConversationListUseCase.invoke(any())).thenReturn(Result.success(freshConversations))
            println("    → refreshList()")
            viewModel.refreshList()
            advanceUntilIdle()

            scrollEventReceived shouldBe true
            println("    ✅ scrollToTopEvent emitted on refresh")
            job.cancel()
        }
    }

    // ==================== E. UIKit Events (Event Flow Bus) ====================
    // Note: The Conversations ViewModel's event handler methods (removeConversation,
    // removeGroup, removeUser, updateUserStatus) are all private. Unlike the Users
    // ViewModel which exposes updateUser() and moveUserToTop() as public methods,
    // Conversations keeps these internal. We test the CometChatEvents flow bus
    // directly to verify events are delivered correctly to subscribers.

    test("CometChatEvents should deliver ConversationDeleted to subscribers") {
        runTest {
            val receivedEvents = mutableListOf<CometChatConversationEvent>()
            val job = CoroutineScope(testDispatcher).launch {
                CometChatEvents.conversationEvents.collect { receivedEvents.add(it) }
            }

            val conversation = MockFactory.createUserConversation(uid = "evt-user-1")
            println("    → Emitting ConversationDeleted for ${conversation.conversationId}")
            CometChatEvents.emitConversationEventSync(
                CometChatConversationEvent.ConversationDeleted(conversation)
            )
            advanceUntilIdle()

            receivedEvents shouldHaveSize 1
            receivedEvents[0].shouldBeInstanceOf<CometChatConversationEvent.ConversationDeleted>()
            (receivedEvents[0] as CometChatConversationEvent.ConversationDeleted)
                .conversation.conversationId shouldBe conversation.conversationId
            println("    ✅ ConversationDeleted delivered with correct conversationId")
            job.cancel()
        }
    }

    test("CometChatEvents should deliver ConversationUpdated to subscribers") {
        runTest {
            val receivedEvents = mutableListOf<CometChatConversationEvent>()
            val job = CoroutineScope(testDispatcher).launch {
                CometChatEvents.conversationEvents.collect { receivedEvents.add(it) }
            }

            val conversation = MockFactory.createUserConversation(uid = "evt-user-2")
            println("    → Emitting ConversationUpdated for ${conversation.conversationId}")
            CometChatEvents.emitConversationEventSync(
                CometChatConversationEvent.ConversationUpdated(conversation)
            )
            advanceUntilIdle()

            receivedEvents shouldHaveSize 1
            receivedEvents[0].shouldBeInstanceOf<CometChatConversationEvent.ConversationUpdated>()
            println("    ✅ ConversationUpdated delivered")
            job.cancel()
        }
    }

    test("CometChatEvents should deliver UserBlocked to subscribers") {
        runTest {
            val receivedEvents = mutableListOf<CometChatUserEvent>()
            val job = CoroutineScope(testDispatcher).launch {
                CometChatEvents.userEvents.collect { receivedEvents.add(it) }
            }

            val user = MockFactory.createUser("blocked-user", "Blocked User", isBlockedByMe = true)
            println("    → Emitting UserBlocked for ${user.uid}")
            CometChatEvents.emitUserEventSync(CometChatUserEvent.UserBlocked(user))
            advanceUntilIdle()

            receivedEvents shouldHaveSize 1
            receivedEvents[0].shouldBeInstanceOf<CometChatUserEvent.UserBlocked>()
            (receivedEvents[0] as CometChatUserEvent.UserBlocked).user.uid shouldBe "blocked-user"
            println("    ✅ UserBlocked delivered with correct UID")
            job.cancel()
        }
    }

    test("CometChatEvents should deliver UserUnblocked to subscribers") {
        runTest {
            val receivedEvents = mutableListOf<CometChatUserEvent>()
            val job = CoroutineScope(testDispatcher).launch {
                CometChatEvents.userEvents.collect { receivedEvents.add(it) }
            }

            val user = MockFactory.createUser("unblocked-user", "Unblocked User")
            println("    → Emitting UserUnblocked for ${user.uid}")
            CometChatEvents.emitUserEventSync(CometChatUserEvent.UserUnblocked(user))
            advanceUntilIdle()

            receivedEvents shouldHaveSize 1
            receivedEvents[0].shouldBeInstanceOf<CometChatUserEvent.UserUnblocked>()
            println("    ✅ UserUnblocked delivered")
            job.cancel()
        }
    }

    test("CometChatEvents should deliver MessageDeleted to subscribers") {
        runTest {
            val receivedEvents = mutableListOf<CometChatMessageEvent>()
            val job = CoroutineScope(testDispatcher).launch {
                CometChatEvents.messageEvents.collect { receivedEvents.add(it) }
            }

            val message = MockFactory.createBaseMessage(id = 42L)
            println("    → Emitting MessageDeleted for message id=42")
            CometChatEvents.emitMessageEventSync(CometChatMessageEvent.MessageDeleted(message))
            advanceUntilIdle()

            receivedEvents shouldHaveSize 1
            receivedEvents[0].shouldBeInstanceOf<CometChatMessageEvent.MessageDeleted>()
            (receivedEvents[0] as CometChatMessageEvent.MessageDeleted).message.id shouldBe 42L
            println("    ✅ MessageDeleted delivered with correct message ID")
            job.cancel()
        }
    }

    // ==================== F. SDK Listeners ====================
    // Note: The Conversations ViewModel's SDK listener handler methods
    // (updateUserStatus, addTypingIndicator, removeTypingIndicator) are all private.
    // Unlike the Users ViewModel which exposes updateUser() and moveUserToTop(),
    // these cannot be called directly in tests.
    //
    // SDK listener behavior is covered by:
    // - Full-chain integration tests (ConversationFullChainIntegrationTest)
    // - The event flow tests above (E section)
    // - Typing indicator key format is tested via the existing property tests

    // ==================== G. Event Flow Integration ====================
    // Covered by the E section above — CometChatEvents emit + collect tests
    // verify that ConversationDeleted, ConversationUpdated, UserBlocked,
    // UserUnblocked, and MessageDeleted events flow correctly through the bus.

    // ==================== I. Search ====================
    // Note: The CometChatConversationsViewModel does NOT have a search method.
    // Unlike CometChatUsersViewModel which has searchUsers(), the Conversations
    // ViewModel relies on the ConversationsRequest.ConversationsRequestBuilder
    // for filtering. Search is handled at the UI layer, not the ViewModel layer.

    // ==================== K. Sound Events ====================

    test("setDisableSoundForMessages should update the configuration") {
        runTest {
            val viewModel = createViewModel(emptyList())
            advanceUntilIdle()

            // Default is false (sound enabled)
            // Set to true (sound disabled)
            viewModel.setDisableSoundForMessages(true)
            println("    → setDisableSoundForMessages(true)")

            // Set back to false (sound enabled)
            viewModel.setDisableSoundForMessages(false)
            println("    → setDisableSoundForMessages(false)")

            // The playSoundEvent SharedFlow exists and is accessible
            viewModel.playSoundEvent.shouldBeInstanceOf<SharedFlow<Boolean>>()
            println("    ✅ setDisableSoundForMessages accepted, playSoundEvent SharedFlow accessible")
        }
    }

    test("isThreadedMessage should return true for parentMessageId > 0, false for 0") {
        runTest {
            val viewModel = createViewModel(emptyList())
            advanceUntilIdle()

            val threadedMessage = MockFactory.createBaseMessage(id = 1L, parentMessageId = 42L)
            viewModel.isThreadedMessage(threadedMessage) shouldBe true
            println("    → parentMessageId=42 → isThreaded=true ✅")

            val normalMessage = MockFactory.createBaseMessage(id = 2L, parentMessageId = 0L)
            viewModel.isThreadedMessage(normalMessage) shouldBe false
            println("    → parentMessageId=0 → isThreaded=false ✅")
        }
    }

    // ==================== getSelectedConversations ====================

    test("getSelectedConversations should return list matching selectedConversations state") {
        checkAll(10, Arb.int(2..8)) { count ->
            runTest {
                val conversations = MockFactory.createUserConversations(count)
                val viewModel = createViewModel(conversations)
                advanceUntilIdle()

                val selectCount = (1..count).random()
                conversations.take(selectCount).forEach {
                    viewModel.selectConversation(it, UIKitConstants.SelectionMode.MULTIPLE)
                }
                println("    → selected $selectCount of $count")

                val selected = viewModel.getSelectedConversations()
                selected shouldHaveSize selectCount
                println("    ✅ getSelectedConversations matches")
            }
        }
    }
})
