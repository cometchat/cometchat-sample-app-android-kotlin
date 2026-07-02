package com.cometchat.uikit.compose.presentation.groups

import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.GroupsRequest
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.Group
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.domain.repository.GroupsRepository
import com.cometchat.uikit.core.domain.usecase.FetchGroupsUseCase
import com.cometchat.uikit.core.domain.usecase.JoinGroupUseCase
import com.cometchat.uikit.core.state.GroupsUIState
import com.cometchat.uikit.core.viewmodel.CometChatGroupsViewModel
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.element
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

/**
 * Rendering + Interaction + PBT tests for CometChatGroups Compose component.
 *
 * Verifies ViewModel state that drives composable rendering and
 * interaction behaviors (selection, search, pagination).
 *
 * Run with:
 *   ./gradlew :chatuikit-compose:testDebugUnitTest --tests "*.groups.CometChatGroupsRenderingTest"
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CometChatGroupsRenderingTest : FunSpec({

    val testDispatcher = UnconfinedTestDispatcher()

    beforeTest {
        Dispatchers.setMain(testDispatcher)
        println("  🧪 ${it.name.testName}")
    }

    afterTest {
        Dispatchers.resetMain()
        println()
    }

    // ==================== Helpers ====================

    fun createGroups(count: Int, type: String = CometChatConstants.GROUP_TYPE_PUBLIC): List<Group> {
        return (1..count).map { i -> Group("group-$i", "Group $i", type, "", null, "") }
    }

    fun createViewModel(groups: List<Group>): CometChatGroupsViewModel {
        val repository = object : GroupsRepository {
            override suspend fun fetchGroups(request: GroupsRequest) = Result.success(groups)
            override suspend fun joinGroup(groupId: String, groupType: String, password: String?) = Result.success(groups.firstOrNull() ?: Group("x", "x", "public", "", null, ""))
            override fun hasMoreGroups() = groups.isNotEmpty()
        }
        return CometChatGroupsViewModel(FetchGroupsUseCase(repository), JoinGroupUseCase(repository), enableListeners = false)
    }

    fun createViewModelWithError(code: String, msg: String): CometChatGroupsViewModel {
        val repository = object : GroupsRepository {
            override suspend fun fetchGroups(request: GroupsRequest) = Result.failure<List<Group>>(CometChatException(code, msg))
            override suspend fun joinGroup(groupId: String, groupType: String, password: String?) = Result.failure<Group>(CometChatException(code, msg))
            override fun hasMoreGroups() = true
        }
        return CometChatGroupsViewModel(FetchGroupsUseCase(repository), JoinGroupUseCase(repository), enableListeners = false)
    }

    // ==================== Rendering State Tests ====================

    test("ViewModel with groups → Content state") {
        println("=== TEST: Groups → Content ===")
        runTest {
            val viewModel = createViewModel(createGroups(5))
            advanceUntilIdle()
            viewModel.uiState.value.shouldBeInstanceOf<GroupsUIState.Content>()
            viewModel.groups.value shouldHaveSize 5
            println("RESULT: Content with 5 groups ✅")
        }
    }

    test("ViewModel with empty list → Empty state") {
        println("=== TEST: Empty → Empty state ===")
        runTest {
            val viewModel = createViewModel(emptyList())
            advanceUntilIdle()
            viewModel.uiState.value shouldBe GroupsUIState.Empty
            println("RESULT: Empty state ✅")
        }
    }

    test("ViewModel with error → Error state") {
        println("=== TEST: Error → Error state ===")
        runTest {
            val viewModel = createViewModelWithError("ERR", "Failed")
            advanceUntilIdle()
            viewModel.uiState.value.shouldBeInstanceOf<GroupsUIState.Error>()
            println("RESULT: Error state ✅")
        }
    }

    test("Groups contain correct data for rendering") {
        println("=== TEST: Group data accessible ===")
        runTest {
            val viewModel = createViewModel(createGroups(3))
            advanceUntilIdle()
            val items = viewModel.groups.value
            items[0].guid shouldBe "group-1"
            items[0].name shouldBe "Group 1"
            items[2].guid shouldBe "group-3"
            println("RESULT: Group data correct ✅")
        }
    }

    // ==================== Selection Tests ====================

    test("selectGroup SINGLE — only 1 selected") {
        println("=== TEST: SINGLE selection ===")
        runTest {
            val viewModel = createViewModel(createGroups(5))
            advanceUntilIdle()
            val groups = viewModel.groups.value
            viewModel.selectGroup(groups[0], UIKitConstants.SelectionMode.SINGLE)
            viewModel.selectGroup(groups[2], UIKitConstants.SelectionMode.SINGLE)
            viewModel.selectedGroups.value shouldHaveSize 1
            viewModel.selectedGroups.value.first().guid shouldBe "group-3"
            println("RESULT: SINGLE keeps only last ✅")
        }
    }

    test("selectGroup MULTIPLE — accumulates") {
        println("=== TEST: MULTIPLE selection ===")
        runTest {
            val viewModel = createViewModel(createGroups(5))
            advanceUntilIdle()
            val groups = viewModel.groups.value
            viewModel.selectGroup(groups[0], UIKitConstants.SelectionMode.MULTIPLE)
            viewModel.selectGroup(groups[1], UIKitConstants.SelectionMode.MULTIPLE)
            viewModel.selectGroup(groups[2], UIKitConstants.SelectionMode.MULTIPLE)
            viewModel.selectedGroups.value shouldHaveSize 3
            println("RESULT: MULTIPLE accumulated 3 ✅")
        }
    }

    test("clearSelection — empties set") {
        println("=== TEST: clearSelection ===")
        runTest {
            val viewModel = createViewModel(createGroups(5))
            advanceUntilIdle()
            viewModel.selectGroup(viewModel.groups.value[0], UIKitConstants.SelectionMode.MULTIPLE)
            viewModel.selectGroup(viewModel.groups.value[1], UIKitConstants.SelectionMode.MULTIPLE)
            viewModel.clearSelection()
            viewModel.selectedGroups.value shouldHaveSize 0
            println("RESULT: Selection cleared ✅")
        }
    }

    // ==================== PBT ====================

    test("PBT: Any group count — correct state") {
        println("=== PBT: Group count → state ===")
        checkAll(50, Arb.int(0..20)) { count ->
            runTest {
                val viewModel = createViewModel(createGroups(count))
                advanceUntilIdle()
                if (count == 0) {
                    viewModel.uiState.value shouldBe GroupsUIState.Empty
                } else {
                    viewModel.uiState.value.shouldBeInstanceOf<GroupsUIState.Content>()
                    viewModel.groups.value shouldHaveSize count
                }
                println("  [Iteration] count=$count → ${if (count == 0) "Empty" else "Content"} ✅")
            }
        }
    }

    test("PBT: Group type filtering — all types render") {
        println("=== PBT: Group types render ===")
        checkAll(20, Arb.element(CometChatConstants.GROUP_TYPE_PUBLIC, CometChatConstants.GROUP_TYPE_PRIVATE, CometChatConstants.GROUP_TYPE_PASSWORD)) { type ->
            runTest {
                val groups = createGroups(3, type)
                val viewModel = createViewModel(groups)
                advanceUntilIdle()
                viewModel.groups.value shouldHaveSize 3
                viewModel.groups.value.forEach { it.groupType shouldBe type }
                println("  [Iteration] type=$type → 3 groups rendered ✅")
            }
        }
    }

    test("PBT: SINGLE selection never exceeds 1") {
        println("=== PBT: SINGLE max 1 ===")
        checkAll(20, Arb.int(2..10)) { clickCount ->
            runTest {
                val viewModel = createViewModel(createGroups(clickCount))
                advanceUntilIdle()
                for (group in viewModel.groups.value) {
                    viewModel.selectGroup(group, UIKitConstants.SelectionMode.SINGLE)
                    viewModel.selectedGroups.value.size shouldBe 1
                }
                println("  [Iteration] clicks=$clickCount, selected=1 ✅")
            }
        }
    }
})
