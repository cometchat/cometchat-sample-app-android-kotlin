package com.cometchat.uikit.core.viewmodel.conversations

import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.models.Conversation
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.TextMessage
import com.cometchat.uikit.core.domain.usecase.DeleteConversationUseCase
import com.cometchat.uikit.core.domain.usecase.GetConversationListUseCase
import com.cometchat.uikit.core.domain.usecase.RefreshConversationListUseCase
import com.cometchat.uikit.core.viewmodel.CometChatConversationsViewModel
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * ENG-38583 — `ConversationUpdated` discarded everything except `unreadMessageCount`.
 *
 * The handler cloned the *existing* entry and copied a single field off the caller's object,
 * so an integrator could not push a refreshed `conversationWith` (a `Group` whose metadata
 * changed server-side) into the list. It failed silently: no error, no log, no change.
 *
 * These tests pin the merge semantics that replaced it — supplied fields are taken, absent
 * ones leave the existing entry alone — and that a non-matching conversation is untouched.
 *
 * Real SDK models rather than mocks: the merge calls `Conversation.clone()`, which a mock
 * returns `null` for.
 *
 * Run: ./gradlew :chatuikit-core:testDebugUnitTest --tests "*ConversationUpdatedMergeTest"
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ConversationUpdatedMergeTest : FunSpec({

    val testDispatcher = UnconfinedTestDispatcher()

    lateinit var getConversationListUseCase: GetConversationListUseCase
    lateinit var deleteConversationUseCase: DeleteConversationUseCase
    lateinit var refreshConversationListUseCase: RefreshConversationListUseCase

    fun group(guid: String, description: String): Group =
        Group(guid, "Group $guid", CometChatConstants.GROUP_TYPE_PUBLIC, null)
            .apply { setDescription(description) }

    fun conversation(
        guid: String,
        description: String,
        unread: Int = 0,
        updatedAt: Long = 0
    ): Conversation =
        Conversation("group_$guid", CometChatConstants.RECEIVER_TYPE_GROUP).apply {
            conversationWith = group(guid, description)
            unreadMessageCount = unread
            this.updatedAt = updatedAt
        }

    suspend fun viewModelWith(vararg conversations: Conversation): CometChatConversationsViewModel {
        whenever(getConversationListUseCase.invoke(any()))
            .thenReturn(Result.success(conversations.toList()))
        return CometChatConversationsViewModel(
            getConversationListUseCase,
            deleteConversationUseCase,
            refreshConversationListUseCase,
            enableListeners = false
        )
    }

    fun CometChatConversationsViewModel.groupIn(index: Int): Group =
        getItems()[index].conversationWith as Group

    beforeTest {
        Dispatchers.setMain(testDispatcher)
        getConversationListUseCase = mock()
        deleteConversationUseCase = mock()
        refreshConversationListUseCase = mock()
    }

    afterTest {
        Dispatchers.resetMain()
    }

    test("a refreshed conversationWith replaces the stale one") {
        runTest {
            val vm = viewModelWith(conversation("g1", description = "STALE"))
            advanceUntilIdle()

            vm.updateConversationInList(conversation("g1", description = "FRESH"))
            advanceUntilIdle()

            vm.groupIn(0).description shouldBe "FRESH"
        }
    }

    test("the supplied group instance is adopted, not merged field by field") {
        runTest {
            val vm = viewModelWith(conversation("g1", description = "STALE"))
            advanceUntilIdle()
            val before = vm.groupIn(0)

            vm.updateConversationInList(conversation("g1", description = "FRESH"))
            advanceUntilIdle()

            vm.groupIn(0) shouldNotBe before
        }
    }

    test("a supplied lastMessage is adopted") {
        runTest {
            val vm = viewModelWith(conversation("g1", description = "STALE"))
            advanceUntilIdle()

            val update = conversation("g1", description = "FRESH").apply {
                lastMessage = TextMessage("g1", "hello", CometChatConstants.RECEIVER_TYPE_GROUP)
            }
            vm.updateConversationInList(update)
            advanceUntilIdle()

            (vm.getItems()[0].lastMessage as TextMessage).text shouldBe "hello"
        }
    }

    test("a null conversationWith leaves the existing one intact") {
        runTest {
            val vm = viewModelWith(conversation("g1", description = "KEEP"))
            advanceUntilIdle()

            val update = Conversation("group_g1", CometChatConstants.RECEIVER_TYPE_GROUP)
            vm.updateConversationInList(update)
            advanceUntilIdle()

            vm.groupIn(0).description shouldBe "KEEP"
        }
    }

    test("a null lastMessage leaves the existing one intact") {
        runTest {
            val existing = conversation("g1", description = "KEEP").apply {
                lastMessage = TextMessage("g1", "earlier", CometChatConstants.RECEIVER_TYPE_GROUP)
            }
            val vm = viewModelWith(existing)
            advanceUntilIdle()

            vm.updateConversationInList(conversation("g1", description = "FRESH"))
            advanceUntilIdle()

            (vm.getItems()[0].lastMessage as TextMessage).text shouldBe "earlier"
        }
    }

    test("unreadMessageCount is always taken, including a deliberate zero") {
        runTest {
            val vm = viewModelWith(conversation("g1", description = "KEEP", unread = 7))
            advanceUntilIdle()

            vm.updateConversationInList(conversation("g1", description = "KEEP", unread = 0))
            advanceUntilIdle()

            vm.getItems()[0].unreadMessageCount shouldBe 0
        }
    }

    test("updatedAt is taken when supplied and ignored when zero") {
        runTest {
            val vm = viewModelWith(conversation("g1", description = "KEEP", updatedAt = 100L))
            advanceUntilIdle()

            vm.updateConversationInList(conversation("g1", description = "KEEP", updatedAt = 500L))
            advanceUntilIdle()
            vm.getItems()[0].updatedAt shouldBe 500L

            vm.updateConversationInList(conversation("g1", description = "KEEP", updatedAt = 0L))
            advanceUntilIdle()
            vm.getItems()[0].updatedAt shouldBe 500L
        }
    }

    test("tags are adopted when supplied and left intact when not") {
        runTest {
            val existing = conversation("g1", description = "KEEP").apply {
                tags = listOf("pinned")
            }
            val vm = viewModelWith(existing)
            advanceUntilIdle()

            vm.updateConversationInList(conversation("g1", description = "KEEP"))
            advanceUntilIdle()
            vm.getItems()[0].tags shouldBe listOf("pinned")

            vm.updateConversationInList(
                conversation("g1", description = "KEEP").apply { tags = listOf("archived") }
            )
            advanceUntilIdle()
            vm.getItems()[0].tags shouldBe listOf("archived")
        }
    }

    test("the remaining counters are taken from the supplied conversation") {
        runTest {
            val existing = conversation("g1", description = "KEEP").apply {
                unreadMentionsCount = 3
                lastReadMessageId = 100L
                latestMessageId = 200L
            }
            val vm = viewModelWith(existing)
            advanceUntilIdle()

            vm.updateConversationInList(
                conversation("g1", description = "KEEP").apply {
                    unreadMentionsCount = 7
                    lastReadMessageId = 500L
                    latestMessageId = 600L
                }
            )
            advanceUntilIdle()

            vm.getItems()[0].unreadMentionsCount shouldBe 7
            vm.getItems()[0].lastReadMessageId shouldBe 500L
            vm.getItems()[0].latestMessageId shouldBe 600L
        }
    }

    test("a conversation that does not match is left untouched") {
        runTest {
            val vm = viewModelWith(
                conversation("g1", description = "ONE"),
                conversation("g2", description = "TWO")
            )
            advanceUntilIdle()

            vm.updateConversationInList(conversation("g1", description = "CHANGED"))
            advanceUntilIdle()

            vm.groupIn(0).description shouldBe "CHANGED"
            vm.groupIn(1).description shouldBe "TWO"
        }
    }
})
