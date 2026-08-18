package com.cometchat.uikit.core.viewmodel.conversations

import com.cometchat.chat.models.Conversation
import com.cometchat.uikit.core.domain.usecase.DeleteConversationUseCase
import com.cometchat.uikit.core.domain.usecase.GetConversationListUseCase
import com.cometchat.uikit.core.domain.usecase.RefreshConversationListUseCase
import com.cometchat.uikit.core.testutils.MockFactory
import com.cometchat.uikit.core.viewmodel.CometChatConversationsViewModel
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
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
 * ENG-35566 — duplicate `conversationId` reaching the conversation list.
 *
 * The crash: `CometChatConversations` keys its `LazyColumn` by `conversation.conversationId`,
 * so two entries sharing an id kill the app with
 * `IllegalArgumentException: Key "group_<guid>" was already used`.
 *
 * The paginated fetch already deduplicated, but `refreshList` published the server response
 * verbatim and `addItem`/`addItems` appended unconditionally — the public list API that
 * integrators call. These tests pin the list to unique ids across those paths. The render
 * boundary in `ConversationListContent` deduplicates as well, so a duplicate from any future
 * path still cannot crash Compose.
 *
 * Run: ./gradlew :chatuikit-core:testDebugUnitTest --tests "*ConversationsDuplicateKeyTest"
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ConversationsDuplicateKeyTest : FunSpec({

    val testDispatcher = UnconfinedTestDispatcher()

    lateinit var getConversationListUseCase: GetConversationListUseCase
    lateinit var deleteConversationUseCase: DeleteConversationUseCase
    lateinit var refreshConversationListUseCase: RefreshConversationListUseCase

    suspend fun createViewModel(conversations: List<Conversation>): CometChatConversationsViewModel {
        whenever(getConversationListUseCase.invoke(any())).thenReturn(Result.success(conversations))
        return CometChatConversationsViewModel(
            getConversationListUseCase,
            deleteConversationUseCase,
            refreshConversationListUseCase,
            enableListeners = false
        )
    }

    fun conversationIds(vm: CometChatConversationsViewModel): List<String?> =
        vm.getItems().map { it.conversationId }

    beforeTest {
        Dispatchers.setMain(testDispatcher)
        getConversationListUseCase = mock()
        deleteConversationUseCase = mock()
        refreshConversationListUseCase = mock()
    }

    afterTest {
        Dispatchers.resetMain()
    }

    test("addItem does not append a conversation already in the list") {
        runTest {
            val existing = MockFactory.createGroupConversation(guid = "g1")
            val vm = createViewModel(listOf(existing))
            advanceUntilIdle()

            // Same conversationId, different instance — as a real-time event would deliver it.
            vm.addItem(MockFactory.createGroupConversation(guid = "g1"))
            advanceUntilIdle()

            vm.getItemCount() shouldBe 1
            conversationIds(vm).distinct() shouldHaveSize 1
        }
    }

    test("addItems drops entries already present and duplicates within the batch") {
        runTest {
            val vm = createViewModel(listOf(MockFactory.createGroupConversation(guid = "g1")))
            advanceUntilIdle()

            vm.addItems(
                listOf(
                    MockFactory.createGroupConversation(guid = "g1"), // already present
                    MockFactory.createGroupConversation(guid = "g2"),
                    MockFactory.createGroupConversation(guid = "g2")  // duplicate in batch
                )
            )
            advanceUntilIdle()

            vm.getItemCount() shouldBe 2
            conversationIds(vm) shouldBe conversationIds(vm).distinct()
        }
    }

    test("addItem still appends a genuinely new conversation") {
        runTest {
            val vm = createViewModel(listOf(MockFactory.createGroupConversation(guid = "g1")))
            advanceUntilIdle()

            vm.addItem(MockFactory.createGroupConversation(guid = "g2"))
            advanceUntilIdle()

            vm.getItemCount() shouldBe 2
        }
    }

    test("refreshList deduplicates a server response that repeats a conversation") {
        runTest {
            val vm = createViewModel(emptyList())
            advanceUntilIdle()

            // Build the mocks first — creating them inside thenReturn() nests stubbing and
            // trips Mockito's UnfinishedStubbingException.
            val serverResponse = listOf(
                MockFactory.createGroupConversation(guid = "g1"),
                MockFactory.createGroupConversation(guid = "g1"),
                MockFactory.createUserConversation(uid = "u1")
            )
            whenever(refreshConversationListUseCase.invoke(any()))
                .thenReturn(Result.success(serverResponse))

            vm.refreshList()
            advanceUntilIdle()

            vm.getItemCount() shouldBe 2
            conversationIds(vm) shouldBe conversationIds(vm).distinct()
        }
    }
})
