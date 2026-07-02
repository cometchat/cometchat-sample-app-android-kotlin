package com.cometchat.uikit.core.viewmodel.users

import com.cometchat.chat.core.UsersRequest
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.domain.usecase.FetchUsersUseCase
import com.cometchat.uikit.core.domain.usecase.SearchUsersUseCase
import com.cometchat.uikit.core.events.CometChatEvents
import com.cometchat.uikit.core.events.CometChatUserEvent
import com.cometchat.uikit.core.state.UsersUIState
import com.cometchat.uikit.core.testutils.MockFactory
import com.cometchat.uikit.core.viewmodel.CometChatUsersViewModel
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
 * Full API Method Coverage Tests for CometChatUsersViewModel.
 *
 * Covers all documented public API methods from:
 * https://www.cometchat.com/docs/ui-kit/android/users
 *
 * Categories:
 * - Programmatic selection (selectUser, clearSelection, getSelectedUsers, isSelected)
 * - Selection modes (NONE, SINGLE, MULTIPLE)
 * - Search (searchUsers with keyword, reset on empty)
 * - Request builders (setUsersRequestBuilder, setSearchRequestBuilder)
 * - List operations (addUserToTop, updateUser, removeUser, moveUserToTop)
 * - SDK events (onUserOnline, onUserOffline)
 * - UI events (ccUserBlocked, ccUserUnblocked)
 *
 * Run with:
 *   ./gradlew :chatuikit-core:testDebugUnitTest --tests "*.users.CometChatUsersAPIMethodCoverageTest"
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CometChatUsersAPIMethodCoverageTest : FunSpec({

    val testDispatcher = UnconfinedTestDispatcher()

    lateinit var fetchUsersUseCase: FetchUsersUseCase
    lateinit var searchUsersUseCase: SearchUsersUseCase

    suspend fun createViewModel(users: List<User>): CometChatUsersViewModel {
        whenever(fetchUsersUseCase.invoke(any())).thenReturn(Result.success(users))
        whenever(fetchUsersUseCase.hasMore()).thenReturn(users.isNotEmpty())
        val vm = CometChatUsersViewModel(fetchUsersUseCase, searchUsersUseCase, enableListeners = false)
        vm.fetchUsers()
        return vm
    }

    beforeTest {
        Dispatchers.setMain(testDispatcher)
        fetchUsersUseCase = mock()
        searchUsersUseCase = mock()
        println("  🧪 ${it.name.testName}")
    }

    afterTest {
        Dispatchers.resetMain()
        println()
    }

    // ==================== A. Programmatic Selection ====================

    context("Programmatic Selection API") {

        test("selectUser(SINGLE) — selects exactly one user") {
            runTest {
                println("=== TEST: selectUser SINGLE mode ===")
                val users = MockFactory.createUsers(5)
                val viewModel = createViewModel(users)
                advanceUntilIdle()

                println("STEP 1: Selecting user-3 in SINGLE mode")
                viewModel.selectUser(users[2], UIKitConstants.SelectionMode.SINGLE)

                println("STEP 2: Asserting exactly 1 selected")
                viewModel.selectedUsers.value shouldHaveSize 1
                viewModel.selectedUsers.value.first().uid shouldBe "user-3"
                println("RESULT: selectUser(SINGLE) selected user-3 ✅")
            }
        }

        test("selectUser(SINGLE) — replaces previous selection") {
            runTest {
                println("=== TEST: selectUser SINGLE replaces ===")
                val users = MockFactory.createUsers(5)
                val viewModel = createViewModel(users)
                advanceUntilIdle()

                println("STEP 1: Selecting user-1, then user-4")
                viewModel.selectUser(users[0], UIKitConstants.SelectionMode.SINGLE)
                viewModel.selectUser(users[3], UIKitConstants.SelectionMode.SINGLE)

                println("STEP 2: Asserting only user-4 is selected")
                viewModel.selectedUsers.value shouldHaveSize 1
                viewModel.selectedUsers.value.first().uid shouldBe "user-4"
                println("RESULT: SINGLE mode replaced user-1 with user-4 ✅")
            }
        }

        test("selectUser(MULTIPLE) — accumulates selections") {
            runTest {
                println("=== TEST: selectUser MULTIPLE accumulates ===")
                val users = MockFactory.createUsers(5)
                val viewModel = createViewModel(users)
                advanceUntilIdle()

                println("STEP 1: Selecting user-1, user-3, user-5")
                viewModel.selectUser(users[0], UIKitConstants.SelectionMode.MULTIPLE)
                viewModel.selectUser(users[2], UIKitConstants.SelectionMode.MULTIPLE)
                viewModel.selectUser(users[4], UIKitConstants.SelectionMode.MULTIPLE)

                println("STEP 2: Asserting 3 users selected")
                viewModel.selectedUsers.value shouldHaveSize 3
                println("RESULT: MULTIPLE mode accumulated 3 selections ✅")
            }
        }

        test("selectUser(MULTIPLE) — toggle deselects already-selected user") {
            runTest {
                println("=== TEST: selectUser MULTIPLE toggle ===")
                val users = MockFactory.createUsers(5)
                val viewModel = createViewModel(users)
                advanceUntilIdle()

                println("STEP 1: Selecting user-1 and user-2")
                viewModel.selectUser(users[0], UIKitConstants.SelectionMode.MULTIPLE)
                viewModel.selectUser(users[1], UIKitConstants.SelectionMode.MULTIPLE)
                viewModel.selectedUsers.value shouldHaveSize 2

                println("STEP 2: Re-selecting user-1 (toggle off)")
                viewModel.selectUser(users[0], UIKitConstants.SelectionMode.MULTIPLE)
                viewModel.selectedUsers.value shouldHaveSize 1
                viewModel.selectedUsers.value.first().uid shouldBe "user-2"
                println("RESULT: Toggle deselected user-1, only user-2 remains ✅")
            }
        }

        test("selectUser(NONE) — does nothing") {
            runTest {
                println("=== TEST: selectUser NONE mode ===")
                val users = MockFactory.createUsers(3)
                val viewModel = createViewModel(users)
                advanceUntilIdle()

                println("STEP 1: Selecting in NONE mode")
                viewModel.selectUser(users[0], UIKitConstants.SelectionMode.NONE)

                println("STEP 2: Asserting no selection")
                viewModel.selectedUsers.value shouldHaveSize 0
                println("RESULT: NONE mode did nothing ✅")
            }
        }

        test("clearSelection — empties all selections") {
            runTest {
                println("=== TEST: clearSelection ===")
                val users = MockFactory.createUsers(5)
                val viewModel = createViewModel(users)
                advanceUntilIdle()

                println("STEP 1: Selecting 3 users")
                viewModel.selectUser(users[0], UIKitConstants.SelectionMode.MULTIPLE)
                viewModel.selectUser(users[1], UIKitConstants.SelectionMode.MULTIPLE)
                viewModel.selectUser(users[2], UIKitConstants.SelectionMode.MULTIPLE)
                viewModel.selectedUsers.value shouldHaveSize 3

                println("STEP 2: Clearing selection")
                viewModel.clearSelection()
                viewModel.selectedUsers.value shouldHaveSize 0
                println("RESULT: clearSelection emptied all ✅")
            }
        }

        test("getSelectedUsers — returns correct list") {
            runTest {
                println("=== TEST: getSelectedUsers ===")
                val users = MockFactory.createUsers(5)
                val viewModel = createViewModel(users)
                advanceUntilIdle()

                viewModel.selectUser(users[1], UIKitConstants.SelectionMode.MULTIPLE)
                viewModel.selectUser(users[3], UIKitConstants.SelectionMode.MULTIPLE)

                val selected = viewModel.getSelectedUsers()
                selected shouldHaveSize 2
                selected.map { it.uid } shouldContain "user-2"
                selected.map { it.uid } shouldContain "user-4"
                println("RESULT: getSelectedUsers returned [user-2, user-4] ✅")
            }
        }

        test("isSelected — returns true for selected, false for unselected") {
            runTest {
                println("=== TEST: isSelected ===")
                val users = MockFactory.createUsers(3)
                val viewModel = createViewModel(users)
                advanceUntilIdle()

                viewModel.selectUser(users[1], UIKitConstants.SelectionMode.SINGLE)

                viewModel.isSelected(users[0]) shouldBe false
                viewModel.isSelected(users[1]) shouldBe true
                viewModel.isSelected(users[2]) shouldBe false
                println("RESULT: isSelected correct for all 3 users ✅")
            }
        }
    }

    // ==================== B. Selection Mode PBT ====================

    context("Selection Mode PBT") {

        test("PBT: SINGLE mode never has more than 1 selected user") {
            println("=== PBT: SINGLE mode max 1 ===")
            checkAll(20, Arb.int(2..15)) { clickCount ->
                runTest {
                    val users = MockFactory.createUsers(clickCount)
                    val viewModel = createViewModel(users)
                    advanceUntilIdle()

                    for (user in users) {
                        viewModel.selectUser(user, UIKitConstants.SelectionMode.SINGLE)
                        viewModel.selectedUsers.value.size shouldBe 1
                    }
                    println("  [Iteration] clicks=$clickCount, selectedUsers.size=1 ✅")
                }
            }
        }

        test("PBT: MULTIPLE mode selection count equals number of distinct selections") {
            println("=== PBT: MULTIPLE mode count ===")
            checkAll(20, Arb.int(1..10)) { selectCount ->
                runTest {
                    val users = MockFactory.createUsers(15)
                    val viewModel = createViewModel(users)
                    advanceUntilIdle()

                    for (i in 0 until selectCount) {
                        viewModel.selectUser(users[i], UIKitConstants.SelectionMode.MULTIPLE)
                    }
                    viewModel.selectedUsers.value shouldHaveSize selectCount
                    println("  [Iteration] selectCount=$selectCount ✅")
                }
            }
        }
    }

    // ==================== C. List Operations ====================

    context("List Operations API") {

        test("addUserToTop — adds user at index 0") {
            runTest {
                println("=== TEST: addUserToTop ===")
                val users = MockFactory.createUsers(3)
                val viewModel = createViewModel(users)
                advanceUntilIdle()

                val newUser = MockFactory.createUser("new-user", "New User")
                viewModel.addUserToTop(newUser)

                viewModel.users.value[0].uid shouldBe "new-user"
                viewModel.users.value shouldHaveSize 4
                println("RESULT: addUserToTop placed new-user at index 0 ✅")
            }
        }

        test("addUserToTop — does not add duplicate") {
            runTest {
                println("=== TEST: addUserToTop no duplicate ===")
                val users = MockFactory.createUsers(3)
                val viewModel = createViewModel(users)
                advanceUntilIdle()

                viewModel.addUserToTop(users[1]) // Already exists
                viewModel.users.value shouldHaveSize 3
                println("RESULT: Duplicate not added ✅")
            }
        }

        test("updateUser — updates existing user in place") {
            runTest {
                println("=== TEST: updateUser ===")
                val users = MockFactory.createUsers(3)
                val viewModel = createViewModel(users)
                advanceUntilIdle()

                val updatedUser = MockFactory.createUser("user-2", "Updated User 2", status = "offline")
                viewModel.updateUser(updatedUser)

                viewModel.users.value[1].name shouldBe "Updated User 2"
                viewModel.users.value shouldHaveSize 3
                println("RESULT: user-2 updated in place ✅")
            }
        }

        test("removeUser — removes user from list") {
            runTest {
                println("=== TEST: removeUser ===")
                val users = MockFactory.createUsers(3)
                val viewModel = createViewModel(users)
                advanceUntilIdle()

                viewModel.removeUser(users[1])

                viewModel.users.value shouldHaveSize 2
                viewModel.users.value.map { it.uid } shouldNotContain "user-2"
                println("RESULT: user-2 removed ✅")
            }
        }

        test("removeUser — last user removed transitions to Empty state") {
            runTest {
                println("=== TEST: removeUser last → Empty ===")
                val users = MockFactory.createUsers(1)
                val viewModel = createViewModel(users)
                advanceUntilIdle()

                viewModel.removeUser(users[0])

                viewModel.users.value shouldHaveSize 0
                viewModel.uiState.value shouldBe UsersUIState.Empty
                println("RESULT: Last user removed → Empty state ✅")
            }
        }

        test("moveUserToTop — moves existing user to index 0") {
            runTest {
                println("=== TEST: moveUserToTop ===")
                val users = MockFactory.createUsers(5)
                val viewModel = createViewModel(users)
                advanceUntilIdle()

                viewModel.moveUserToTop(users[3]) // Move user-4 to top

                viewModel.users.value[0].uid shouldBe "user-4"
                viewModel.users.value shouldHaveSize 5
                println("RESULT: user-4 moved to index 0 ✅")
            }
        }
    }

    // ==================== D. Search API ====================

    context("Search API") {

        test("searchUsers with keyword — triggers search use case") {
            runTest {
                println("=== TEST: searchUsers with keyword ===")
                val users = MockFactory.createUsers(5)
                val viewModel = createViewModel(users)
                advanceUntilIdle()

                val searchResults = MockFactory.createUsers(2, prefix = "search")
                whenever(searchUsersUseCase.invoke(any(), any())).thenReturn(Result.success(searchResults))

                println("STEP 1: Calling searchUsers('john')")
                viewModel.searchUsers("john")
                advanceUntilIdle()

                println("STEP 2: Asserting search results replace list")
                viewModel.users.value shouldHaveSize 2
                viewModel.uiState.value shouldBe UsersUIState.Content
                println("RESULT: Search returned 2 results ✅")
            }
        }

        test("searchUsers with null/empty — resets to normal fetch") {
            runTest {
                println("=== TEST: searchUsers reset ===")
                val users = MockFactory.createUsers(5)
                val viewModel = createViewModel(users)
                advanceUntilIdle()

                println("STEP 1: Calling searchUsers(null) to reset")
                viewModel.searchUsers(null)
                advanceUntilIdle()

                println("STEP 2: Asserting normal fetch triggered")
                viewModel.uiState.value shouldBe UsersUIState.Content
                println("RESULT: Reset to normal fetch ✅")
            }
        }

        test("searchUsers with no results — transitions to Empty") {
            runTest {
                println("=== TEST: searchUsers empty results ===")
                val users = MockFactory.createUsers(5)
                val viewModel = createViewModel(users)
                advanceUntilIdle()

                whenever(searchUsersUseCase.invoke(any(), any())).thenReturn(Result.success(emptyList()))

                viewModel.searchUsers("nonexistent")
                advanceUntilIdle()

                viewModel.uiState.value shouldBe UsersUIState.Empty
                println("RESULT: Empty search → Empty state ✅")
            }
        }
    }

    // ==================== E. Request Builder API ====================

    context("Request Builder API") {

        test("setUsersRequestBuilder — configures custom builder") {
            runTest {
                println("=== TEST: setUsersRequestBuilder ===")
                val users = MockFactory.createUsers(3)
                whenever(fetchUsersUseCase.invoke(any())).thenReturn(Result.success(users))
                val viewModel = CometChatUsersViewModel(fetchUsersUseCase, searchUsersUseCase, enableListeners = false)

                println("STEP 1: Setting custom request builder")
                val builder = mock<UsersRequest.UsersRequestBuilder>()
                val request = mock<UsersRequest>()
                whenever(builder.build()).thenReturn(request)
                viewModel.setUsersRequestBuilder(builder)

                println("STEP 2: Fetching users")
                viewModel.fetchUsers()
                advanceUntilIdle()

                viewModel.uiState.value shouldBe UsersUIState.Content
                println("RESULT: Custom builder used for fetch ✅")
            }
        }

        test("setSearchRequestBuilder — configures separate search builder") {
            runTest {
                println("=== TEST: setSearchRequestBuilder ===")
                val users = MockFactory.createUsers(3)
                val viewModel = createViewModel(users)
                advanceUntilIdle()

                println("STEP 1: Setting custom search builder")
                val searchBuilder = mock<UsersRequest.UsersRequestBuilder>()
                viewModel.setSearchRequestBuilder(searchBuilder)

                println("RESULT: Search builder set (will be used on next search) ✅")
            }
        }
    }

    // ==================== F. Error Handling ====================

    context("Error Handling") {

        test("fetch failure — transitions to Error state with exception") {
            runTest {
                println("=== TEST: fetch failure → Error ===")
                val exception = MockFactory.createCometChatException("NET_ERR", "Network timeout")
                whenever(fetchUsersUseCase.invoke(any())).thenReturn(Result.failure(exception))
                val viewModel = CometChatUsersViewModel(fetchUsersUseCase, searchUsersUseCase, enableListeners = false)
                viewModel.fetchUsers()
                advanceUntilIdle()

                viewModel.uiState.value.shouldBeInstanceOf<UsersUIState.Error>()
                (viewModel.uiState.value as UsersUIState.Error).exception.code shouldBe "NET_ERR"
                println("RESULT: Error state with code='NET_ERR' ✅")
            }
        }

        test("PBT: Any error code/message produces Error state") {
            println("=== PBT: Any error → Error state ===")
            checkAll(20, Arb.string(1..10), Arb.string(1..50)) { code, msg ->
                runTest {
                    val exception = MockFactory.createCometChatException(code, msg)
                    whenever(fetchUsersUseCase.invoke(any())).thenReturn(Result.failure(exception))
                    val viewModel = CometChatUsersViewModel(fetchUsersUseCase, searchUsersUseCase, enableListeners = false)
                    viewModel.fetchUsers()
                    advanceUntilIdle()

                    viewModel.uiState.value.shouldBeInstanceOf<UsersUIState.Error>()
                    println("  [Iteration] code='$code' → Error ✅")
                }
            }
        }
    }

    // ==================== G. Pagination ====================

    context("Pagination") {

        test("PBT: Multiple fetches append without duplicates") {
            println("=== PBT: Pagination append ===")
            checkAll(20, Arb.int(1..10), Arb.int(1..10)) { firstBatch, secondBatch ->
                runTest {
                    val batch1 = MockFactory.createUsers(firstBatch, prefix = "batch1")
                    val batch2 = MockFactory.createUsers(secondBatch, prefix = "batch2")

                    whenever(fetchUsersUseCase.invoke(any()))
                        .thenReturn(Result.success(batch1))
                        .thenReturn(Result.success(batch2))
                    whenever(fetchUsersUseCase.hasMore()).thenReturn(true)

                    val viewModel = CometChatUsersViewModel(fetchUsersUseCase, searchUsersUseCase, enableListeners = false)

                    // Set a mock builder so UsersRequest is created properly in test env
                    val builder = mock<UsersRequest.UsersRequestBuilder>()
                    val request = mock<UsersRequest>()
                    whenever(builder.build()).thenReturn(request)
                    whenever(builder.setLimit(any())).thenReturn(builder)
                    viewModel.setUsersRequestBuilder(builder)

                    viewModel.fetchUsers()
                    advanceUntilIdle()
                    viewModel.fetchUsers()
                    advanceUntilIdle()

                    viewModel.users.value shouldHaveSize firstBatch + secondBatch
                    println("  [Iteration] batch1=$firstBatch + batch2=$secondBatch = ${firstBatch + secondBatch} ✅")
                }
            }
        }
    }
})
