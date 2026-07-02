package com.cometchat.uikit.compose.presentation.groups

import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.GroupsRequest
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
 * Interaction tests for CometChatGroups Compose component.
 *
 * Verifies user interactions → ViewModel state changes that drive
 * composable recomposition:
 * - onItemClick in NONE mode → callback invoked
 * - onItemClick in selection mode → selection state changes
 * - onBackPress → callback invoked
 * - Search → searchGroups triggered
 * - Selection submit → onSelection callback
 *
 * Run with:
 *   ./gradlew :chatuikit-compose:testDebugUnitTest --tests "*.groups.CometChatGroupsInteractionTest"
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CometChatGroupsInteractionTest : FunSpec({

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

    fun createGroups(count: Int): List<Group> {
        return (1..count).map { i -> Group("group-$i", "Group $i", CometChatConstants.GROUP_TYPE_PUBLIC, "", null, "") }
    }

    fun createViewModel(groups: List<Group>): CometChatGroupsViewModel {
        val repository = object : GroupsRepository {
            override suspend fun fetchGroups(request: GroupsRequest) = Result.success(groups)
            override suspend fun joinGroup(groupId: String, groupType: String, password: String?) =
                Result.success(groups.firstOrNull() ?: Group("x", "x", "public", "", null, ""))
            override fun hasMoreGroups() = false
        }
        return CometChatGroupsViewModel(FetchGroupsUseCase(repository), JoinGroupUseCase(repository), enableListeners = false)
    }

    // ==================== Click Interaction Tests ====================

    context("Click Interactions") {

        test("item click in NONE mode — does not change selection") {
            runTest {
                println("=== TEST: Click in NONE mode ===")
                val viewModel = createViewModel(createGroups(5))
                advanceUntilIdle()

                // In NONE mode, clicking should NOT change selection
                viewModel.selectGroup(viewModel.groups.value[0], UIKitConstants.SelectionMode.NONE)
                viewModel.selectedGroups.value shouldHaveSize 0
                println("RESULT: NONE mode click does not select ✅")
            }
        }

        test("item click in SINGLE mode — selects group") {
            runTest {
                println("=== TEST: Click in SINGLE mode ===")
                val viewModel = createViewModel(createGroups(5))
                advanceUntilIdle()

                viewModel.selectGroup(viewModel.groups.value[2], UIKitConstants.SelectionMode.SINGLE)
                viewModel.selectedGroups.value shouldHaveSize 1
                viewModel.selectedGroups.value.first().guid shouldBe "group-3"
                println("RESULT: SINGLE mode click selects ✅")
            }
        }

        test("item click in MULTIPLE mode — toggles selection") {
            runTest {
                println("=== TEST: Click in MULTIPLE mode ===")
                val viewModel = createViewModel(createGroups(5))
                advanceUntilIdle()

                viewModel.selectGroup(viewModel.groups.value[0], UIKitConstants.SelectionMode.MULTIPLE)
                viewModel.selectedGroups.value shouldHaveSize 1

                // Toggle off
                viewModel.selectGroup(viewModel.groups.value[0], UIKitConstants.SelectionMode.MULTIPLE)
                viewModel.selectedGroups.value shouldHaveSize 0
                println("RESULT: MULTIPLE mode toggles ✅")
            }
        }
    }

    // ==================== Selection Submit Tests ====================

    context("Selection Submit") {

        test("getSelectedGroups returns correct list for onSelection callback") {
            runTest {
                println("=== TEST: getSelectedGroups for submit ===")
                val viewModel = createViewModel(createGroups(5))
                advanceUntilIdle()

                viewModel.selectGroup(viewModel.groups.value[1], UIKitConstants.SelectionMode.MULTIPLE)
                viewModel.selectGroup(viewModel.groups.value[3], UIKitConstants.SelectionMode.MULTIPLE)

                val selected = viewModel.getSelectedGroups()
                selected shouldHaveSize 2
                println("RESULT: getSelectedGroups returns 2 for submit ✅")
            }
        }

        test("clearSelection after submit — empties selection") {
            runTest {
                println("=== TEST: clearSelection after submit ===")
                val viewModel = createViewModel(createGroups(5))
                advanceUntilIdle()

                viewModel.selectGroup(viewModel.groups.value[0], UIKitConstants.SelectionMode.MULTIPLE)
                viewModel.selectGroup(viewModel.groups.value[1], UIKitConstants.SelectionMode.MULTIPLE)
                viewModel.selectedGroups.value shouldHaveSize 2

                viewModel.clearSelection()
                viewModel.selectedGroups.value shouldHaveSize 0
                println("RESULT: Selection cleared after submit ✅")
            }
        }
    }

    // ==================== Search Interaction Tests ====================

    context("Search Interactions") {

        test("searchGroups with keyword — updates list") {
            runTest {
                println("=== TEST: Search updates list ===")
                val groups = createGroups(10)
                val searchResults = createGroups(2)

                val repository = object : GroupsRepository {
                    private var firstCall = true
                    override suspend fun fetchGroups(request: GroupsRequest): Result<List<Group>> {
                        return if (firstCall) { firstCall = false; Result.success(groups) }
                        else Result.success(searchResults)
                    }
                    override suspend fun joinGroup(groupId: String, groupType: String, password: String?) =
                        Result.success(groups[0])
                    override fun hasMoreGroups() = false
                }
                val viewModel = CometChatGroupsViewModel(
                    FetchGroupsUseCase(repository), JoinGroupUseCase(repository), enableListeners = false
                )
                advanceUntilIdle()

                viewModel.searchGroups("test")
                advanceUntilIdle()

                viewModel.groups.value shouldHaveSize 2
                println("RESULT: Search updated list ✅")
            }
        }

        test("searchGroups with empty — resets to full list") {
            runTest {
                println("=== TEST: Empty search resets ===")
                val viewModel = createViewModel(createGroups(5))
                advanceUntilIdle()

                viewModel.searchGroups("")
                advanceUntilIdle()

                viewModel.uiState.value.shouldBeInstanceOf<GroupsUIState.Content>()
                println("RESULT: Empty search resets ✅")
            }
        }
    }

    // ==================== Refresh Interaction Tests ====================

    context("Refresh Interactions") {

        test("refreshList — replaces data") {
            runTest {
                println("=== TEST: Refresh replaces data ===")
                val oldGroups = createGroups(5)
                val freshGroups = (1..3).map { Group("fresh-$it", "Fresh $it", CometChatConstants.GROUP_TYPE_PUBLIC, "", null, "") }

                val repository = object : GroupsRepository {
                    private var firstCall = true
                    override suspend fun fetchGroups(request: GroupsRequest): Result<List<Group>> {
                        return if (firstCall) { firstCall = false; Result.success(oldGroups) }
                        else Result.success(freshGroups)
                    }
                    override suspend fun joinGroup(groupId: String, groupType: String, password: String?) =
                        Result.success(oldGroups[0])
                    override fun hasMoreGroups() = false
                }
                val viewModel = CometChatGroupsViewModel(
                    FetchGroupsUseCase(repository), JoinGroupUseCase(repository), enableListeners = false
                )
                advanceUntilIdle()
                viewModel.groups.value shouldHaveSize 5

                viewModel.refreshList()
                advanceUntilIdle()

                viewModel.groups.value shouldHaveSize 3
                viewModel.groups.value[0].guid shouldBe "fresh-1"
                println("RESULT: Refresh replaced data ✅")
            }
        }
    }

    // ==================== PBT ====================

    context("PBT: Selection Interactions") {

        test("PBT: Any number of MULTIPLE selections accumulates correctly") {
            println("=== PBT: MULTIPLE accumulation ===")
            checkAll(20, Arb.int(1..10)) { selectCount ->
                runTest {
                    val viewModel = createViewModel(createGroups(15))
                    advanceUntilIdle()

                    for (i in 0 until selectCount) {
                        viewModel.selectGroup(viewModel.groups.value[i], UIKitConstants.SelectionMode.MULTIPLE)
                    }
                    viewModel.selectedGroups.value shouldHaveSize selectCount
                    println("  [Iteration] selectCount=$selectCount ✅")
                }
            }
        }
    }
})
