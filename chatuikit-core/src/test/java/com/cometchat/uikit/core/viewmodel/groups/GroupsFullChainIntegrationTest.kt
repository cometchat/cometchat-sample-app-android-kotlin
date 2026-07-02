package com.cometchat.uikit.core.viewmodel.groups

import com.cometchat.chat.core.GroupsRequest
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.Group
import com.cometchat.uikit.core.data.datasource.GroupsDataSource
import com.cometchat.uikit.core.data.repository.GroupsRepositoryImpl
import com.cometchat.uikit.core.domain.usecase.FetchGroupsUseCase
import com.cometchat.uikit.core.domain.usecase.JoinGroupUseCase
import com.cometchat.uikit.core.state.GroupsUIState
import com.cometchat.uikit.core.testutils.MockFactory
import com.cometchat.uikit.core.viewmodel.CometChatGroupsViewModel
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
 * Full-chain integration test for the Groups component.
 *
 * Tests the complete dependency chain end-to-end:
 *   ViewModel → UseCases → GroupsRepositoryImpl → DataSource (interface impl)
 *
 * Instead of mocking use cases or the repository, we provide a custom
 * GroupsDataSource implementation (via `object : GroupsDataSource`)
 * and let all real production objects wire through.
 *
 * The ONLY fake is the DataSource at the SDK boundary.
 * MockFactory helpers are used only for creating SDK objects (Group, etc.)
 * that have private constructors.
 *
 * Each test prints a step-by-step trace showing data flowing through every layer:
 *   [DataSource] → [Repository] → [UseCase] → [ViewModel]
 *
 * Run with:
 *   ./gradlew :chatuikit-core:testDebugUnitTest --tests "*.GroupsFullChainIntegrationTest"
 */
@OptIn(ExperimentalCoroutinesApi::class)
class GroupsFullChainIntegrationTest : FunSpec({

    val testDispatcher = UnconfinedTestDispatcher()

    beforeTest {
        Dispatchers.setMain(testDispatcher)
        println("\n    🧪 ${it.name.testName}")
        println("    ─────────────────────────────────────────────────")
    }

    afterTest {
        Dispatchers.resetMain()
        println()
    }

    // -- Helper: build the full chain from a custom DataSource with logging --

    fun buildViewModel(
        dataSource: GroupsDataSource
    ): CometChatGroupsViewModel {
        println("    [WIRING] Building full chain:")
        println("    [WIRING]   DataSource (custom impl) → GroupsRepositoryImpl → UseCases → ViewModel")

        val repository = GroupsRepositoryImpl(dataSource)
        println("    [WIRING]   ✓ Repository created with custom DataSource")

        val fetchGroupsUseCase = FetchGroupsUseCase(repository)
        val joinGroupUseCase = JoinGroupUseCase(repository)
        println("    [WIRING]   ✓ 2 UseCases created (FetchGroups, JoinGroup) with Repository")

        val viewModel = CometChatGroupsViewModel(
            fetchGroupsUseCase = fetchGroupsUseCase,
            joinGroupUseCase = joinGroupUseCase,
            enableListeners = false
        )
        println("    [WIRING]   ✓ ViewModel created (enableListeners=false)")
        println("    [WIRING]   ✓ ViewModel.init → fetchGroups() triggered automatically")
        return viewModel
    }

    // ==================== Content path ====================

    test("full chain: DataSource returning 5 groups → ViewModel shows Content state") {
        runTest {
            val fakeGroups = (1..5).map { i ->
                MockFactory.createGroup(guid = "group-$i", name = "Group $i")
            }
            val guids = fakeGroups.map { it.guid }
            println("    [SETUP] Created 5 fake groups: $guids")

            val dataSource = object : GroupsDataSource {
                override suspend fun fetchGroups(request: GroupsRequest): List<Group> {
                    println("    [DataSource] fetchGroups() called → returning ${fakeGroups.size} groups")
                    println("    [DataSource]   GUIDs: $guids")
                    return fakeGroups
                }

                override suspend fun joinGroup(
                    groupId: String,
                    groupType: String,
                    password: String?
                ): Group {
                    throw UnsupportedOperationException("Not used in this test")
                }
            }

            val viewModel = buildViewModel(dataSource)
            println("    [CHAIN]  ViewModel.init → fetchGroups()")
            println("    [CHAIN]    → FetchGroupsUseCase.invoke(request)")
            println("    [CHAIN]      → GroupsRepositoryImpl.fetchGroups(request)")
            println("    [CHAIN]        → DataSource.fetchGroups(request) → returned 5 items")
            println("    [CHAIN]      ← Repository wraps in Result.success(5 groups), hasMore=true")
            println("    [CHAIN]    ← UseCase passes Result through unchanged")
            println("    [CHAIN]  ← ViewModel receives Result.success → updates state to Content")
            advanceUntilIdle()

            val uiState = viewModel.uiState.value
            val groupsSize = viewModel.groups.value.size

            println("    [ViewModel] uiState = ${uiState::class.simpleName}")
            println("    [ViewModel] groups.size = $groupsSize")

            uiState.shouldBeInstanceOf<GroupsUIState.Content>()
            viewModel.groups.value shouldHaveSize 5

            println("    [VERIFY] ViewModel shows Content state with 5 groups ✓")
            println("    ✅ PASSED — 5 groups flowed: DataSource → Repo → UseCase → ViewModel → Content")
        }
    }

    // ==================== Empty path ====================

    test("full chain: DataSource returning empty list → ViewModel shows Empty state") {
        runTest {
            println("    [SETUP] DataSource will return empty list")

            val dataSource = object : GroupsDataSource {
                override suspend fun fetchGroups(request: GroupsRequest): List<Group> {
                    println("    [DataSource] fetchGroups() called → returning empty list (0 items)")
                    return emptyList()
                }

                override suspend fun joinGroup(
                    groupId: String,
                    groupType: String,
                    password: String?
                ): Group {
                    throw UnsupportedOperationException("Not used in this test")
                }
            }

            val viewModel = buildViewModel(dataSource)
            println("    [CHAIN]  ViewModel.init → fetchGroups()")
            println("    [CHAIN]    → FetchGroupsUseCase.invoke(request)")
            println("    [CHAIN]      → GroupsRepositoryImpl.fetchGroups(request)")
            println("    [CHAIN]        → DataSource.fetchGroups(request) → returned 0 items")
            println("    [CHAIN]      ← Repository wraps in Result.success(emptyList), sets hasMore=false")
            println("    [CHAIN]    ← UseCase passes Result through unchanged")
            println("    [CHAIN]  ← ViewModel receives empty list → transitions to Empty state")
            advanceUntilIdle()

            val uiState = viewModel.uiState.value
            val groupsSize = viewModel.groups.value.size

            println("    [ViewModel] uiState = ${uiState::class.simpleName}")
            println("    [ViewModel] groups.size = $groupsSize")

            uiState shouldBe GroupsUIState.Empty
            viewModel.groups.value shouldHaveSize 0

            println("    ✅ PASSED — Empty list flowed through entire chain → GroupsUIState.Empty")
        }
    }

    // ==================== Error path ====================

    test("full chain: DataSource throwing exception → ViewModel shows Error state") {
        runTest {
            val exception = MockFactory.createCometChatException("NET_ERR", "No connection")
            println("    [SETUP] DataSource will throw CometChatException(code=NET_ERR, message=No connection)")

            val dataSource = object : GroupsDataSource {
                override suspend fun fetchGroups(request: GroupsRequest): List<Group> {
                    println("    [DataSource] fetchGroups() called → THROWING CometChatException(NET_ERR)")
                    throw exception
                }

                override suspend fun joinGroup(
                    groupId: String,
                    groupType: String,
                    password: String?
                ): Group {
                    throw UnsupportedOperationException("Not used in this test")
                }
            }

            val viewModel = buildViewModel(dataSource)
            println("    [CHAIN]  ViewModel.init → fetchGroups()")
            println("    [CHAIN]    → FetchGroupsUseCase.invoke(request)")
            println("    [CHAIN]      → GroupsRepositoryImpl.fetchGroups(request)")
            println("    [CHAIN]        → DataSource.fetchGroups(request) → THREW CometChatException")
            println("    [CHAIN]      ← Repository catches exception → wraps in Result.failure(exception)")
            println("    [CHAIN]    ← UseCase passes Result.failure through unchanged")
            println("    [CHAIN]  ← ViewModel receives Result.failure → transitions to Error state")
            advanceUntilIdle()

            val uiState = viewModel.uiState.value

            println("    [ViewModel] uiState = ${uiState::class.simpleName}")
            if (uiState is GroupsUIState.Error) {
                println("    [ViewModel] exception.code = ${uiState.exception.code}")
                println("    [ViewModel] exception.message = ${uiState.exception.message}")
                println("    [ViewModel] exception matches original? ${uiState.exception === exception}")
            }

            uiState.shouldBeInstanceOf<GroupsUIState.Error>()
            (uiState as GroupsUIState.Error).exception shouldBe exception

            println("    ✅ PASSED — Exception propagated: DataSource threw → Repo wrapped → UseCase passed → ViewModel Error")
        }
    }
})
