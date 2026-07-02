package com.cometchat.uikit.kotlin.presentation.groups

import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.GroupsRequest
import com.cometchat.chat.models.Group
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
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

/**
 * Rendering tests for CometChatGroups (Kotlin XML View layer).
 *
 * Verifies ViewModel state → UIState mapping that drives View rendering:
 * - Loading, Content, Empty, Error states
 * - Group data accessibility for rendering (name, type, members count)
 * - Group type indicator mapping
 *
 * Run with:
 *   ./gradlew :chatuikit-kotlin:testDebugUnitTest --tests "*.groups.CometChatGroupsRenderingTest"
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
            override suspend fun joinGroup(groupId: String, groupType: String, password: String?) =
                Result.success(groups.firstOrNull() ?: Group("x", "x", "public", "", null, ""))
            override fun hasMoreGroups() = groups.isNotEmpty()
        }
        return CometChatGroupsViewModel(FetchGroupsUseCase(repository), JoinGroupUseCase(repository), enableListeners = false)
    }

    fun createViewModelWithError(code: String, msg: String): CometChatGroupsViewModel {
        val repository = object : GroupsRepository {
            override suspend fun fetchGroups(request: GroupsRequest) =
                Result.failure<List<Group>>(com.cometchat.chat.exceptions.CometChatException(code, msg))
            override suspend fun joinGroup(groupId: String, groupType: String, password: String?) =
                Result.failure<Group>(com.cometchat.chat.exceptions.CometChatException(code, msg))
            override fun hasMoreGroups() = true
        }
        return CometChatGroupsViewModel(FetchGroupsUseCase(repository), JoinGroupUseCase(repository), enableListeners = false)
    }

    // ==================== State Rendering Tests ====================

    context("UIState → View Rendering") {

        test("Content state — groups list is populated for rendering") {
            runTest {
                println("=== TEST: Content state rendering ===")
                val viewModel = createViewModel(createGroups(5))
                advanceUntilIdle()

                viewModel.uiState.value.shouldBeInstanceOf<GroupsUIState.Content>()
                viewModel.groups.value shouldHaveSize 5
                println("RESULT: Content state with 5 groups ready for rendering ✅")
            }
        }

        test("Empty state — empty view should be shown") {
            runTest {
                println("=== TEST: Empty state rendering ===")
                val viewModel = createViewModel(emptyList())
                advanceUntilIdle()

                viewModel.uiState.value shouldBe GroupsUIState.Empty
                viewModel.groups.value shouldHaveSize 0
                println("RESULT: Empty state ready for rendering ✅")
            }
        }

        test("Error state — error view should be shown with exception") {
            runTest {
                println("=== TEST: Error state rendering ===")
                val viewModel = createViewModelWithError("NET_ERR", "Network error")
                advanceUntilIdle()

                viewModel.uiState.value.shouldBeInstanceOf<GroupsUIState.Error>()
                (viewModel.uiState.value as GroupsUIState.Error).exception.code shouldBe "NET_ERR"
                println("RESULT: Error state with exception ready for rendering ✅")
            }
        }

        test("PBT: Any group count maps to correct state") {
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
    }

    // ==================== Group Data Rendering Tests ====================

    context("Group Data for Rendering") {

        test("Group name is accessible for title rendering") {
            runTest {
                println("=== TEST: Group name accessible ===")
                val viewModel = createViewModel(createGroups(3))
                advanceUntilIdle()

                viewModel.groups.value[0].name shouldBe "Group 1"
                viewModel.groups.value[1].name shouldBe "Group 2"
                viewModel.groups.value[2].name shouldBe "Group 3"
                println("RESULT: Group names accessible ✅")
            }
        }

        test("Group GUID is accessible for item identity") {
            runTest {
                println("=== TEST: Group GUID accessible ===")
                val viewModel = createViewModel(createGroups(3))
                advanceUntilIdle()

                viewModel.groups.value[0].guid shouldBe "group-1"
                viewModel.groups.value[1].guid shouldBe "group-2"
                viewModel.groups.value[2].guid shouldBe "group-3"
                println("RESULT: Group GUIDs accessible ✅")
            }
        }

        test("Group type is accessible for indicator rendering") {
            runTest {
                println("=== TEST: Group type accessible ===")
                val publicGroups = createGroups(2, CometChatConstants.GROUP_TYPE_PUBLIC)
                val privateGroups = (1..2).map { Group("priv-$it", "Private $it", CometChatConstants.GROUP_TYPE_PRIVATE, "", null, "") }
                val passwordGroups = (1..2).map { Group("pass-$it", "Password $it", CometChatConstants.GROUP_TYPE_PASSWORD, "", null, "") }

                val allGroups = publicGroups + privateGroups + passwordGroups
                val viewModel = createViewModel(allGroups)
                advanceUntilIdle()

                viewModel.groups.value[0].groupType shouldBe CometChatConstants.GROUP_TYPE_PUBLIC
                viewModel.groups.value[2].groupType shouldBe CometChatConstants.GROUP_TYPE_PRIVATE
                viewModel.groups.value[4].groupType shouldBe CometChatConstants.GROUP_TYPE_PASSWORD
                println("RESULT: Group types accessible for indicator rendering ✅")
            }
        }

        test("PBT: All group types render correctly") {
            println("=== PBT: Group types render ===")
            checkAll(20, Arb.element(
                CometChatConstants.GROUP_TYPE_PUBLIC,
                CometChatConstants.GROUP_TYPE_PRIVATE,
                CometChatConstants.GROUP_TYPE_PASSWORD
            )) { type ->
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
    }

    // ==================== Error Data Rendering Tests ====================

    context("Error Data for Rendering") {

        test("PBT: Any error code/message is accessible for error view") {
            println("=== PBT: Error data accessible ===")
            checkAll(20, Arb.string(1..10), Arb.string(1..50)) { code, msg ->
                runTest {
                    val viewModel = createViewModelWithError(code, msg)
                    advanceUntilIdle()

                    val state = viewModel.uiState.value
                    state.shouldBeInstanceOf<GroupsUIState.Error>()
                    (state as GroupsUIState.Error).exception.code shouldBe code
                    println("  [Iteration] code='$code' → Error state ✅")
                }
            }
        }
    }
})
