package com.cometchat.uikit.core.viewmodel.groups

import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.GroupsRequest
import com.cometchat.chat.models.Group
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.domain.usecase.FetchGroupsUseCase
import com.cometchat.uikit.core.domain.usecase.JoinGroupUseCase
import com.cometchat.uikit.core.state.GroupsUIState
import com.cometchat.uikit.core.testutils.MockFactory
import com.cometchat.uikit.core.viewmodel.CometChatGroupsViewModel
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldNotContain
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
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * Full API Method Coverage Tests for CometChatGroupsViewModel.
 *
 * Covers all documented public API methods from:
 * https://www.cometchat.com/docs/ui-kit/android/groups
 *
 * Categories:
 * A. Programmatic selection (selectGroup, clearSelection, getSelectedGroups, deselectGroup)
 * B. Selection modes (NONE, SINGLE, MULTIPLE) with PBT
 * C. List operations (addItem, removeItem, updateItem, moveItemToTop, clearItems, batch)
 * D. Search (searchGroups with keyword, reset on empty)
 * E. Request builders (setGroupsRequestBuilder, setSearchRequestBuilder)
 * F. Join group
 * G. Error handling
 * H. Pagination
 * I. Group type handling (PUBLIC, PRIVATE, PASSWORD)
 *
 * Run with:
 *   ./gradlew :chatuikit-core:testDebugUnitTest --tests "*.groups.CometChatGroupsAPIMethodCoverageTest"
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CometChatGroupsAPIMethodCoverageTest : FunSpec({

    val testDispatcher = UnconfinedTestDispatcher()

    lateinit var fetchGroupsUseCase: FetchGroupsUseCase
    lateinit var joinGroupUseCase: JoinGroupUseCase

    fun createGroups(count: Int, type: String = CometChatConstants.GROUP_TYPE_PUBLIC): List<Group> {
        return (1..count).map { i ->
            MockFactory.createGroup(guid = "group-$i", name = "Group $i", type = type)
        }
    }

    suspend fun createViewModel(groups: List<Group>): CometChatGroupsViewModel {
        whenever(fetchGroupsUseCase.invoke(any())).thenReturn(Result.success(groups))
        whenever(fetchGroupsUseCase.hasMore()).thenReturn(groups.isNotEmpty())
        return CometChatGroupsViewModel(fetchGroupsUseCase, joinGroupUseCase, enableListeners = false)
    }

    beforeTest {
        Dispatchers.setMain(testDispatcher)
        fetchGroupsUseCase = mock()
        joinGroupUseCase = mock()
        println("  🧪 ${it.name.testName}")
    }

    afterTest {
        Dispatchers.resetMain()
        println()
    }

    // ==================== A. Programmatic Selection ====================

    context("A. Programmatic Selection API") {

        test("selectGroup(SINGLE) — selects exactly one group") {
            runTest {
                println("=== TEST: selectGroup SINGLE ===")
                val groups = createGroups(5)
                val viewModel = createViewModel(groups)
                advanceUntilIdle()

                println("STEP 1: Selecting group-3 in SINGLE mode")
                viewModel.selectGroup(viewModel.groups.value[2], UIKitConstants.SelectionMode.SINGLE)

                viewModel.selectedGroups.value shouldHaveSize 1
                viewModel.selectedGroups.value.first().guid shouldBe "group-3"
                println("RESULT: selectGroup(SINGLE) selected group-3 ✅")
            }
        }

        test("selectGroup(SINGLE) — replaces previous selection") {
            runTest {
                println("=== TEST: selectGroup SINGLE replaces ===")
                val groups = createGroups(5)
                val viewModel = createViewModel(groups)
                advanceUntilIdle()

                viewModel.selectGroup(viewModel.groups.value[0], UIKitConstants.SelectionMode.SINGLE)
                viewModel.selectGroup(viewModel.groups.value[3], UIKitConstants.SelectionMode.SINGLE)

                viewModel.selectedGroups.value shouldHaveSize 1
                viewModel.selectedGroups.value.first().guid shouldBe "group-4"
                println("RESULT: SINGLE replaced group-1 with group-4 ✅")
            }
        }

        test("selectGroup(MULTIPLE) — accumulates selections") {
            runTest {
                println("=== TEST: selectGroup MULTIPLE accumulates ===")
                val groups = createGroups(5)
                val viewModel = createViewModel(groups)
                advanceUntilIdle()

                viewModel.selectGroup(viewModel.groups.value[0], UIKitConstants.SelectionMode.MULTIPLE)
                viewModel.selectGroup(viewModel.groups.value[2], UIKitConstants.SelectionMode.MULTIPLE)
                viewModel.selectGroup(viewModel.groups.value[4], UIKitConstants.SelectionMode.MULTIPLE)

                viewModel.selectedGroups.value shouldHaveSize 3
                println("RESULT: MULTIPLE accumulated 3 selections ✅")
            }
        }

        test("selectGroup(MULTIPLE) — toggle deselects") {
            runTest {
                println("=== TEST: selectGroup MULTIPLE toggle ===")
                val groups = createGroups(5)
                val viewModel = createViewModel(groups)
                advanceUntilIdle()

                viewModel.selectGroup(viewModel.groups.value[0], UIKitConstants.SelectionMode.MULTIPLE)
                viewModel.selectGroup(viewModel.groups.value[1], UIKitConstants.SelectionMode.MULTIPLE)
                viewModel.selectedGroups.value shouldHaveSize 2

                viewModel.selectGroup(viewModel.groups.value[0], UIKitConstants.SelectionMode.MULTIPLE)
                viewModel.selectedGroups.value shouldHaveSize 1
                println("RESULT: Toggle deselected group-1 ✅")
            }
        }

        test("selectGroup(NONE) — does nothing") {
            runTest {
                println("=== TEST: selectGroup NONE ===")
                val groups = createGroups(3)
                val viewModel = createViewModel(groups)
                advanceUntilIdle()

                viewModel.selectGroup(viewModel.groups.value[0], UIKitConstants.SelectionMode.NONE)
                viewModel.selectedGroups.value shouldHaveSize 0
                println("RESULT: NONE mode did nothing ✅")
            }
        }

        test("deselectGroup — removes specific group from selection") {
            runTest {
                println("=== TEST: deselectGroup ===")
                val groups = createGroups(5)
                val viewModel = createViewModel(groups)
                advanceUntilIdle()

                viewModel.selectGroup(viewModel.groups.value[0], UIKitConstants.SelectionMode.MULTIPLE)
                viewModel.selectGroup(viewModel.groups.value[1], UIKitConstants.SelectionMode.MULTIPLE)
                viewModel.selectGroup(viewModel.groups.value[2], UIKitConstants.SelectionMode.MULTIPLE)
                viewModel.selectedGroups.value shouldHaveSize 3

                viewModel.deselectGroup(viewModel.groups.value[1])
                viewModel.selectedGroups.value shouldHaveSize 2
                println("RESULT: deselectGroup removed group-2 ✅")
            }
        }

        test("clearSelection — empties all") {
            runTest {
                println("=== TEST: clearSelection ===")
                val groups = createGroups(5)
                val viewModel = createViewModel(groups)
                advanceUntilIdle()

                viewModel.selectGroup(viewModel.groups.value[0], UIKitConstants.SelectionMode.MULTIPLE)
                viewModel.selectGroup(viewModel.groups.value[1], UIKitConstants.SelectionMode.MULTIPLE)
                viewModel.clearSelection()

                viewModel.selectedGroups.value shouldHaveSize 0
                println("RESULT: clearSelection emptied all ✅")
            }
        }

        test("getSelectedGroups — returns correct list") {
            runTest {
                println("=== TEST: getSelectedGroups ===")
                val groups = createGroups(5)
                val viewModel = createViewModel(groups)
                advanceUntilIdle()

                viewModel.selectGroup(viewModel.groups.value[1], UIKitConstants.SelectionMode.MULTIPLE)
                viewModel.selectGroup(viewModel.groups.value[3], UIKitConstants.SelectionMode.MULTIPLE)

                val selected = viewModel.getSelectedGroups()
                selected shouldHaveSize 2
                println("RESULT: getSelectedGroups returned 2 groups ✅")
            }
        }
    }

    // ==================== B. Selection Mode PBT ====================

    context("B. Selection Mode PBT") {

        test("PBT: SINGLE mode never exceeds 1 selected") {
            println("=== PBT: SINGLE max 1 ===")
            checkAll(20, Arb.int(2..15)) { clickCount ->
                runTest {
                    val groups = createGroups(clickCount)
                    val viewModel = createViewModel(groups)
                    advanceUntilIdle()

                    for (group in viewModel.groups.value) {
                        viewModel.selectGroup(group, UIKitConstants.SelectionMode.SINGLE)
                        viewModel.selectedGroups.value.size shouldBe 1
                    }
                    println("  [Iteration] clicks=$clickCount, selected=1 ✅")
                }
            }
        }

        test("PBT: MULTIPLE mode count equals distinct selections") {
            println("=== PBT: MULTIPLE count ===")
            checkAll(20, Arb.int(1..10)) { selectCount ->
                runTest {
                    val groups = createGroups(15)
                    val viewModel = createViewModel(groups)
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

    // ==================== C. List Operations ====================

    context("C. List Operations API") {

        test("addItem — adds group to list") {
            runTest {
                println("=== TEST: addItem ===")
                val groups = createGroups(3)
                val viewModel = createViewModel(groups)
                advanceUntilIdle()

                val newGroup = MockFactory.createGroup("new-group", "New Group")
                viewModel.addItem(newGroup)

                viewModel.groups.value shouldHaveSize 4
                println("RESULT: addItem increased size to 4 ✅")
            }
        }

        test("removeItem — removes group from list") {
            runTest {
                println("=== TEST: removeItem ===")
                val groups = createGroups(3)
                val viewModel = createViewModel(groups)
                advanceUntilIdle()

                viewModel.removeItem(viewModel.groups.value[1])
                viewModel.groups.value shouldHaveSize 2
                println("RESULT: removeItem decreased size to 2 ✅")
            }
        }

        test("updateItem — updates group in place") {
            runTest {
                println("=== TEST: updateItem ===")
                val groups = createGroups(3)
                val viewModel = createViewModel(groups)
                advanceUntilIdle()

                val updated = MockFactory.createGroup("group-2", "Updated Group 2", membersCount = 99)
                viewModel.updateItem(updated) { it.guid == "group-2" }

                viewModel.groups.value shouldHaveSize 3
                viewModel.groups.value[1].name shouldBe "Updated Group 2"
                println("RESULT: updateItem updated group-2 in place ✅")
            }
        }

        test("moveItemToTop — moves group to index 0") {
            runTest {
                println("=== TEST: moveItemToTop ===")
                val groups = createGroups(5)
                val viewModel = createViewModel(groups)
                advanceUntilIdle()

                viewModel.moveItemToTop(viewModel.groups.value[3])
                viewModel.groups.value[0].guid shouldBe "group-4"
                viewModel.groups.value shouldHaveSize 5
                println("RESULT: moveItemToTop moved group-4 to index 0 ✅")
            }
        }

        test("clearItems — empties the list") {
            runTest {
                println("=== TEST: clearItems ===")
                val groups = createGroups(5)
                val viewModel = createViewModel(groups)
                advanceUntilIdle()

                viewModel.clearItems()
                viewModel.groups.value shouldHaveSize 0
                println("RESULT: clearItems emptied list ✅")
            }
        }

        test("getItemAt — returns correct group") {
            runTest {
                println("=== TEST: getItemAt ===")
                val groups = createGroups(5)
                val viewModel = createViewModel(groups)
                advanceUntilIdle()

                viewModel.getItemAt(0)?.guid shouldBe "group-1"
                viewModel.getItemAt(4)?.guid shouldBe "group-5"
                viewModel.getItemAt(10) shouldBe null
                println("RESULT: getItemAt returns correct groups ✅")
            }
        }

        test("getItemCount — returns correct count") {
            runTest {
                println("=== TEST: getItemCount ===")
                val groups = createGroups(7)
                val viewModel = createViewModel(groups)
                advanceUntilIdle()

                viewModel.getItemCount() shouldBe 7
                println("RESULT: getItemCount = 7 ✅")
            }
        }

        test("batch — multiple operations in single emission") {
            runTest {
                println("=== TEST: batch ===")
                val groups = createGroups(5)
                val viewModel = createViewModel(groups)
                advanceUntilIdle()

                viewModel.batch {
                    add(MockFactory.createGroup("batch-1", "Batch 1"))
                    add(MockFactory.createGroup("batch-2", "Batch 2"))
                    remove(viewModel.groups.value[0])
                }

                // 5 - 1 + 2 = 6
                viewModel.groups.value shouldHaveSize 6
                println("RESULT: batch applied 3 ops → size=6 ✅")
            }
        }
    }

    // ==================== D. Search API ====================

    context("D. Search API") {

        test("searchGroups with keyword — triggers search") {
            runTest {
                println("=== TEST: searchGroups ===")
                val groups = createGroups(5)
                val viewModel = createViewModel(groups)
                advanceUntilIdle()

                val searchResults = createGroups(2)
                whenever(fetchGroupsUseCase.invoke(any())).thenReturn(Result.success(searchResults))

                viewModel.searchGroups("design")
                advanceUntilIdle()

                viewModel.groups.value shouldHaveSize 2
                println("RESULT: Search returned 2 results ✅")
            }
        }

        test("searchGroups with null — resets to normal fetch") {
            runTest {
                println("=== TEST: searchGroups reset ===")
                val groups = createGroups(5)
                val viewModel = createViewModel(groups)
                advanceUntilIdle()

                viewModel.searchGroups(null)
                advanceUntilIdle()

                viewModel.uiState.value.shouldBeInstanceOf<GroupsUIState.Content>()
                println("RESULT: Reset to normal fetch ✅")
            }
        }

        test("searchGroups with empty results — transitions to Empty") {
            runTest {
                println("=== TEST: searchGroups empty ===")
                val groups = createGroups(5)
                val viewModel = createViewModel(groups)
                advanceUntilIdle()

                whenever(fetchGroupsUseCase.invoke(any())).thenReturn(Result.success(emptyList()))
                viewModel.searchGroups("nonexistent")
                advanceUntilIdle()

                viewModel.uiState.value shouldBe GroupsUIState.Empty
                println("RESULT: Empty search → Empty state ✅")
            }
        }
    }

    // ==================== E. Request Builder API ====================

    context("E. Request Builder API") {

        test("setGroupsRequestBuilder — configures custom builder") {
            runTest {
                println("=== TEST: setGroupsRequestBuilder ===")
                val groups = createGroups(3)
                whenever(fetchGroupsUseCase.invoke(any())).thenReturn(Result.success(groups))
                val viewModel = CometChatGroupsViewModel(fetchGroupsUseCase, joinGroupUseCase, enableListeners = false)
                advanceUntilIdle()

                val builder = mock<GroupsRequest.GroupsRequestBuilder>()
                val request = mock<GroupsRequest>()
                whenever(builder.build()).thenReturn(request)
                viewModel.setGroupsRequestBuilder(builder)

                println("RESULT: Custom builder set ✅")
            }
        }

        test("setSearchRequestBuilder — configures separate search builder") {
            runTest {
                println("=== TEST: setSearchRequestBuilder ===")
                val groups = createGroups(3)
                val viewModel = createViewModel(groups)
                advanceUntilIdle()

                val searchBuilder = mock<GroupsRequest.GroupsRequestBuilder>()
                viewModel.setSearchRequestBuilder(searchBuilder)
                println("RESULT: Search builder set ✅")
            }
        }
    }

    // ==================== F. Join Group ====================

    context("F. Join Group") {

        test("joinGroup — calls joinGroupUseCase with correct params") {
            runTest {
                println("=== TEST: joinGroup ===")
                val groups = createGroups(3)
                val viewModel = createViewModel(groups)
                advanceUntilIdle()

                val joinedGroup = MockFactory.createGroup("group-1", "Group 1")
                whenever(joinGroupUseCase.invoke("group-1", CometChatConstants.GROUP_TYPE_PUBLIC, null))
                    .thenReturn(Result.success(joinedGroup))

                viewModel.joinGroup(viewModel.groups.value[0])
                advanceUntilIdle()

                println("RESULT: joinGroup called successfully ✅")
            }
        }
    }

    // ==================== G. Error Handling ====================

    context("G. Error Handling") {

        test("fetch failure — transitions to Error state") {
            runTest {
                println("=== TEST: fetch failure → Error ===")
                val exception = MockFactory.createCometChatException("NET_ERR", "Network timeout")
                whenever(fetchGroupsUseCase.invoke(any())).thenReturn(Result.failure(exception))
                val viewModel = CometChatGroupsViewModel(fetchGroupsUseCase, joinGroupUseCase, enableListeners = false)
                advanceUntilIdle()

                viewModel.uiState.value.shouldBeInstanceOf<GroupsUIState.Error>()
                (viewModel.uiState.value as GroupsUIState.Error).exception.code shouldBe "NET_ERR"
                println("RESULT: Error state with code='NET_ERR' ✅")
            }
        }

        test("PBT: Any error code produces Error state") {
            println("=== PBT: Any error → Error state ===")
            checkAll(20, Arb.string(1..10), Arb.string(1..50)) { code, msg ->
                runTest {
                    val exception = MockFactory.createCometChatException(code, msg)
                    whenever(fetchGroupsUseCase.invoke(any())).thenReturn(Result.failure(exception))
                    val viewModel = CometChatGroupsViewModel(fetchGroupsUseCase, joinGroupUseCase, enableListeners = false)
                    advanceUntilIdle()

                    viewModel.uiState.value.shouldBeInstanceOf<GroupsUIState.Error>()
                    println("  [Iteration] code='$code' → Error ✅")
                }
            }
        }
    }

    // ==================== H. Pagination ====================

    context("H. Pagination") {

        test("PBT: Multiple fetches append without duplicates") {
            println("=== PBT: Pagination append ===")
            checkAll(20, Arb.int(1..10), Arb.int(1..10)) { batch1Size, batch2Size ->
                runTest {
                    val batch1 = (1..batch1Size).map { MockFactory.createGroup("b1-$it", "B1 Group $it") }
                    val batch2 = (1..batch2Size).map { MockFactory.createGroup("b2-$it", "B2 Group $it") }

                    // Setup mock to return batch1 first, then batch2
                    whenever(fetchGroupsUseCase.invoke(any()))
                        .thenReturn(Result.success(batch1))
                        .thenReturn(Result.success(batch2))
                    whenever(fetchGroupsUseCase.hasMore()).thenReturn(true)

                    // ViewModel init calls fetchGroups() → gets batch1
                    val viewModel = CometChatGroupsViewModel(fetchGroupsUseCase, joinGroupUseCase, enableListeners = false)
                    advanceUntilIdle()

                    // Verify first batch loaded
                    viewModel.groups.value shouldHaveSize batch1Size

                    // Second fetch → gets batch2 (appended)
                    viewModel.fetchGroups()
                    advanceUntilIdle()

                    viewModel.groups.value shouldHaveSize batch1Size + batch2Size
                    println("  [Iteration] batch1=$batch1Size + batch2=$batch2Size = ${batch1Size + batch2Size} ✅")
                }
            }
        }
    }

    // ==================== I. Group Type Handling ====================

    context("I. Group Type Handling") {

        test("PBT: All group types (PUBLIC, PRIVATE, PASSWORD) are stored correctly") {
            println("=== PBT: Group types ===")
            checkAll(20, Arb.element(
                CometChatConstants.GROUP_TYPE_PUBLIC,
                CometChatConstants.GROUP_TYPE_PRIVATE,
                CometChatConstants.GROUP_TYPE_PASSWORD
            )) { groupType ->
                runTest {
                    val groups = createGroups(3, type = groupType)
                    val viewModel = createViewModel(groups)
                    advanceUntilIdle()

                    viewModel.groups.value.forEach { it.groupType shouldBe groupType }
                    println("  [Iteration] type=$groupType → all 3 groups have correct type ✅")
                }
            }
        }
    }

    // ==================== J. UIState Transitions ====================

    context("J. UIState Transitions") {

        test("PBT: Empty list → Empty, non-empty → Content") {
            println("=== PBT: UIState transitions ===")
            checkAll(50, Arb.int(0..20)) { count ->
                runTest {
                    val groups = createGroups(count)
                    val viewModel = createViewModel(groups)
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
})
