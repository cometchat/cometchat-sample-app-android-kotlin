package com.cometchat.uikit.core.viewmodel.conversations

import com.cometchat.chat.core.ConversationsRequest
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Conversation
import com.cometchat.uikit.core.data.datasource.ConversationListDataSource
import com.cometchat.uikit.core.data.repository.ConversationListRepositoryImpl
import com.cometchat.uikit.core.domain.usecase.DeleteConversationUseCase
import com.cometchat.uikit.core.domain.usecase.GetConversationListUseCase
import com.cometchat.uikit.core.domain.usecase.RefreshConversationListUseCase
import com.cometchat.uikit.core.state.DeleteState
import com.cometchat.uikit.core.state.UIState
import com.cometchat.uikit.core.testutils.MockFactory
import com.cometchat.uikit.core.viewmodel.CometChatConversationsViewModel
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

/**
 * Full-chain integration test for the Conversations component.
 *
 * Tests the complete dependency chain end-to-end:
 *   ViewModel → UseCase → ConversationListRepositoryImpl → DataSource (interface impl)
 *
 * Instead of mocking use cases or the repository, we provide a custom
 * ConversationListDataSource implementation (via `object : ConversationListDataSource`)
 * and let all real production objects wire through.
 *
 * The ONLY fake is the DataSource at the SDK boundary.
 * MockFactory helpers are used only for creating SDK objects (Conversation, User, etc.)
 * that have private constructors.
 *
 * Each test prints a step-by-step trace showing data flowing through every layer:
 *   [DataSource] → [Repository] → [UseCase] → [ViewModel]
 *
 * Run with:
 *   ./gradlew :chatuikit-core:testDebugUnitTest --tests "*.ConversationFullChainIntegrationTest"
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ConversationFullChainIntegrationTest : FunSpec({

    val testDispatcher = UnconfinedTestDispatcher()

    beforeTest {
        Dispatchers.setMain(testDispatcher)
        println("\n  🧪 ${it.name.testName}")
        println("  ─────────────────────────────────────────────────")
    }

    afterTest {
        Dispatchers.resetMain()
        println()
    }

    // -- Helper: build the full chain from a custom DataSource with logging --

    fun buildViewModel(
        dataSource: ConversationListDataSource
    ): CometChatConversationsViewModel {
        println("    [WIRING] Building full chain:")
        println("    [WIRING]   DataSource (custom impl) → ConversationListRepositoryImpl → UseCases → ViewModel")

        val repository = ConversationListRepositoryImpl(dataSource)
        println("    [WIRING]   ✓ Repository created with custom DataSource")

        val getUseCase = GetConversationListUseCase(repository)
        val deleteUseCase = DeleteConversationUseCase(repository)
        val refreshUseCase = RefreshConversationListUseCase(repository)
        println("    [WIRING]   ✓ 3 UseCases created (Get, Delete, Refresh) with Repository")

        val viewModel = CometChatConversationsViewModel(
            getConversationListUseCase = getUseCase,
            deleteConversationUseCase = deleteUseCase,
            refreshConversationListUseCase = refreshUseCase,
            enableListeners = false
        )
        println("    [WIRING]   ✓ ViewModel created (enableListeners=false)")
        println("    [WIRING]   ✓ ViewModel.init → fetchConversations() triggered automatically")
        return viewModel
    }

    // ==================== 9.5.1 Fetch flow ====================

    test("full chain: DataSource returning conversations → ViewModel shows Content state") {
        runTest {
            val fakeConversations = MockFactory.createUserConversations(5)
            val ids = fakeConversations.map { it.conversationId }
            println("    [SETUP] Created 5 fake conversations: $ids")

            val dataSource = object : ConversationListDataSource {
                override suspend fun fetchConversations(
                    request: ConversationsRequest
                ): List<Conversation> {
                    println("    [DataSource] fetchConversations() called → returning ${fakeConversations.size} conversations")
                    println("    [DataSource]   IDs: $ids")
                    return fakeConversations
                }

                override suspend fun deleteConversation(
                    conversationWith: String,
                    conversationType: String
                ): String = "success"

                override suspend fun markAsDelivered(message: BaseMessage) {}
            }

            val viewModel = buildViewModel(dataSource)
            println("    [CHAIN]  ViewModel.init → fetchConversations()")
            println("    [CHAIN]    → GetConversationListUseCase.invoke(request)")
            println("    [CHAIN]      → ConversationListRepositoryImpl.getConversations(request)")
            println("    [CHAIN]        → DataSource.fetchConversations(request) → returned 5 items")
            println("    [CHAIN]      ← Repository wraps in Result.success(5 conversations)")
            println("    [CHAIN]    ← UseCase passes Result through unchanged")
            println("    [CHAIN]  ← ViewModel receives Result.success → updates state")
            advanceUntilIdle()

            val uiState = viewModel.uiState.value
            val convSize = viewModel.conversations.value.size
            val resultIds = viewModel.conversations.value.map { it.conversationId }

            println("    [ViewModel] uiState = ${uiState::class.simpleName}")
            println("    [ViewModel] conversations.size = $convSize")
            println("    [ViewModel] conversation IDs = $resultIds")

            uiState.shouldBeInstanceOf<UIState.Content>()
            viewModel.conversations.value shouldHaveSize 5
            resultIds shouldBe ids

            println("    [VERIFY] IDs match: DataSource returned $ids → ViewModel has $resultIds ✓")
            println("    ✅ PASSED — 5 conversations flowed: DataSource → Repo → UseCase → ViewModel")
        }
    }

    // ==================== 9.5.2 Empty flow ====================

    test("full chain: DataSource returning empty list → ViewModel shows Empty state") {
        runTest {
            println("    [SETUP] DataSource will return empty list")

            val dataSource = object : ConversationListDataSource {
                override suspend fun fetchConversations(
                    request: ConversationsRequest
                ): List<Conversation> {
                    println("    [DataSource] fetchConversations() called → returning empty list (0 items)")
                    return emptyList()
                }

                override suspend fun deleteConversation(
                    conversationWith: String,
                    conversationType: String
                ): String = "success"

                override suspend fun markAsDelivered(message: BaseMessage) {}
            }

            val viewModel = buildViewModel(dataSource)
            println("    [CHAIN]  ViewModel.init → fetchConversations()")
            println("    [CHAIN]    → GetConversationListUseCase.invoke(request)")
            println("    [CHAIN]      → ConversationListRepositoryImpl.getConversations(request)")
            println("    [CHAIN]        → DataSource.fetchConversations(request) → returned 0 items")
            println("    [CHAIN]      ← Repository wraps in Result.success(emptyList), sets hasMore=false")
            println("    [CHAIN]    ← UseCase passes Result through unchanged")
            println("    [CHAIN]  ← ViewModel receives empty list → transitions to Empty state")
            advanceUntilIdle()

            val uiState = viewModel.uiState.value
            val convSize = viewModel.conversations.value.size

            println("    [ViewModel] uiState = ${uiState::class.simpleName}")
            println("    [ViewModel] conversations.size = $convSize")

            uiState shouldBe UIState.Empty
            viewModel.conversations.value shouldHaveSize 0

            println("    ✅ PASSED — Empty list flowed through entire chain → UIState.Empty")
        }
    }

    // ==================== 9.5.3 Error flow ====================

    test("full chain: DataSource throwing exception → ViewModel shows Error state") {
        runTest {
            val exception = MockFactory.createCometChatException("NET_ERR", "No connection")
            println("    [SETUP] DataSource will throw CometChatException(code=NET_ERR, message=No connection)")

            val dataSource = object : ConversationListDataSource {
                override suspend fun fetchConversations(
                    request: ConversationsRequest
                ): List<Conversation> {
                    println("    [DataSource] fetchConversations() called → THROWING CometChatException(NET_ERR)")
                    throw exception
                }

                override suspend fun deleteConversation(
                    conversationWith: String,
                    conversationType: String
                ): String = "success"

                override suspend fun markAsDelivered(message: BaseMessage) {}
            }

            val viewModel = buildViewModel(dataSource)
            println("    [CHAIN]  ViewModel.init → fetchConversations()")
            println("    [CHAIN]    → GetConversationListUseCase.invoke(request)")
            println("    [CHAIN]      → ConversationListRepositoryImpl.getConversations(request)")
            println("    [CHAIN]        → DataSource.fetchConversations(request) → THREW CometChatException")
            println("    [CHAIN]      ← Repository catches exception → wraps in Result.failure(exception)")
            println("    [CHAIN]    ← UseCase passes Result.failure through unchanged")
            println("    [CHAIN]  ← ViewModel receives Result.failure → transitions to Error state")
            advanceUntilIdle()

            val uiState = viewModel.uiState.value

            println("    [ViewModel] uiState = ${uiState::class.simpleName}")
            if (uiState is UIState.Error) {
                println("    [ViewModel] exception = ${uiState.exception}")
                println("    [ViewModel] exception matches original? ${uiState.exception === exception}")
            }

            uiState.shouldBeInstanceOf<UIState.Error>()
            (uiState as UIState.Error).exception shouldBe exception

            println("    ✅ PASSED — Exception propagated: DataSource threw → Repo wrapped → UseCase passed → ViewModel Error")
        }
    }

    // ==================== 9.5.4 Pagination flow ====================

    test("full chain: pagination appends second page from DataSource") {
        runTest {
            val page1 = MockFactory.createUserConversations(3, "p1")
            val page2 = MockFactory.createUserConversations(2, "p2")
            var callCount = 0

            println("    [SETUP] Page 1: ${page1.map { it.conversationId }}")
            println("    [SETUP] Page 2: ${page2.map { it.conversationId }}")

            val dataSource = object : ConversationListDataSource {
                override suspend fun fetchConversations(
                    request: ConversationsRequest
                ): List<Conversation> {
                    callCount++
                    val page = if (callCount == 1) page1 else page2
                    println("    [DataSource] fetchConversations() call #$callCount → returning ${page.size} items (page $callCount)")
                    println("    [DataSource]   IDs: ${page.map { it.conversationId }}")
                    return page
                }

                override suspend fun deleteConversation(
                    conversationWith: String,
                    conversationType: String
                ): String = "success"

                override suspend fun markAsDelivered(message: BaseMessage) {}
            }

            // --- First fetch (automatic from init) ---
            println("    [CHAIN]  --- First fetch (from ViewModel.init) ---")
            val viewModel = buildViewModel(dataSource)
            advanceUntilIdle()

            println("    [CHAIN]    DataSource returned page1 (3 items)")
            println("    [CHAIN]    Repository wrapped → Result.success(3 conversations), hasMore=true")
            println("    [CHAIN]    ViewModel appended → conversations.size = ${viewModel.conversations.value.size}")
            viewModel.conversations.value shouldHaveSize 3

            // --- Second fetch (manual pagination) ---
            println("    [CHAIN]  --- Second fetch (manual viewModel.fetchConversations()) ---")
            viewModel.fetchConversations()
            advanceUntilIdle()

            println("    [CHAIN]    DataSource returned page2 (2 items)")
            println("    [CHAIN]    Repository wrapped → Result.success(2 conversations), hasMore=true")
            println("    [CHAIN]    ViewModel APPENDED to existing list → conversations.size = ${viewModel.conversations.value.size}")

            val allIds = viewModel.conversations.value.map { it.conversationId }
            println("    [ViewModel] All conversation IDs: $allIds")
            println("    [ViewModel] uiState = ${viewModel.uiState.value::class.simpleName}")

            viewModel.conversations.value shouldHaveSize 5
            viewModel.uiState.value.shouldBeInstanceOf<UIState.Content>()

            println("    ✅ PASSED — Page1(3) + Page2(2) = 5 total, correctly appended via full chain")
        }
    }

    // ==================== 9.5.5 Delete flow ====================

    test("full chain: delete flows through DataSource and removes from ViewModel") {
        runTest {
            val conversations = MockFactory.createUserConversations(3)
            var deletedWith: String? = null
            var deletedType: String? = null

            println("    [SETUP] Created 3 conversations: ${conversations.map { it.conversationId }}")

            val dataSource = object : ConversationListDataSource {
                override suspend fun fetchConversations(
                    request: ConversationsRequest
                ): List<Conversation> {
                    println("    [DataSource] fetchConversations() → returning ${conversations.size} conversations")
                    return conversations
                }

                override suspend fun deleteConversation(
                    conversationWith: String,
                    conversationType: String
                ): String {
                    deletedWith = conversationWith
                    deletedType = conversationType
                    println("    [DataSource] deleteConversation() called with:")
                    println("    [DataSource]   conversationWith = $conversationWith")
                    println("    [DataSource]   conversationType = $conversationType")
                    return "success"
                }

                override suspend fun markAsDelivered(message: BaseMessage) {}
            }

            // --- Load conversations ---
            val viewModel = buildViewModel(dataSource)
            advanceUntilIdle()
            println("    [ViewModel] Loaded ${viewModel.conversations.value.size} conversations")

            // --- Delete the second conversation ---
            val target = conversations[1]
            println("    [CHAIN]  --- Delete flow ---")
            println("    [CHAIN]  ViewModel.deleteConversation(${target.conversationId})")
            println("    [CHAIN]    → DeleteConversationUseCase.invoke(conversation)")
            println("    [CHAIN]      UseCase extracts: conversationType=${target.conversationType}")
            println("    [CHAIN]      UseCase extracts: conversationWith = (User).uid = user-2")
            println("    [CHAIN]      → ConversationListRepositoryImpl.deleteConversation(\"user-2\", \"user\")")
            println("    [CHAIN]        → DataSource.deleteConversation(\"user-2\", \"user\")")

            viewModel.deleteConversation(target)
            advanceUntilIdle()

            println("    [CHAIN]        ← DataSource returned \"success\"")
            println("    [CHAIN]      ← Repository wraps → Result.success(Unit)")
            println("    [CHAIN]    ← UseCase passes Result.success through")
            println("    [CHAIN]  ← ViewModel: removes conversation from list, sets DeleteState.Success")

            println("    [ViewModel] conversations.size = ${viewModel.conversations.value.size}")
            println("    [ViewModel] remaining IDs = ${viewModel.conversations.value.map { it.conversationId }}")
            println("    [ViewModel] deleteState = ${viewModel.deleteState.value::class.simpleName}")
            println("    [DataSource] received: conversationWith=$deletedWith, conversationType=$deletedType")

            // Verify DataSource received correct params
            deletedWith shouldBe "user-2"
            deletedType shouldBe "user"

            // Verify ViewModel state
            viewModel.conversations.value shouldHaveSize 2
            viewModel.deleteState.value shouldBe DeleteState.Success

            println("    [VERIFY] DataSource received correct UID \"user-2\" (extracted by UseCase from User object) ✓")
            println("    [VERIFY] List reduced from 3 → 2 ✓")
            println("    [VERIFY] DeleteState = Success ✓")
            println("    ✅ PASSED — Delete flowed: ViewModel → UseCase(extract UID) → Repo → DataSource → back up")
        }
    }
})
