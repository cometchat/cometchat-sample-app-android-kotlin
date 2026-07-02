package com.cometchat.uikit.compose.presentation.users

import com.cometchat.chat.core.UsersRequest
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.domain.repository.UsersRepository
import com.cometchat.uikit.core.domain.usecase.FetchUsersUseCase
import com.cometchat.uikit.core.domain.usecase.SearchUsersUseCase
import com.cometchat.uikit.core.viewmodel.CometChatUsersViewModel
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
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * Tests for CometChatUsers Compose component interaction behavior.
 *
 * Verifies that ViewModel operations triggered by user interactions
 * (click, selection, search) produce correct state changes.
 *
 * The Compose component calls these ViewModel methods in response to
 * user gestures on LazyColumn items.
 *
 * Run with:
 *   ./gradlew :chatuikit-compose:testDebugUnitTest --tests "*.users.CometChatUsersInteractionTest"
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CometChatUsersInteractionTest : FunSpec({

    val testDispatcher = UnconfinedTestDispatcher()

    beforeTest {
        Dispatchers.setMain(testDispatcher)
        println("  🧪 ${it.name.testName}")
    }

    afterTest {
        Dispatchers.resetMain()
        println()
    }

    test("selectUser in SINGLE mode → selectedUsers contains exactly 1 item") {
        println("=== TEST: SINGLE selection contains 1 item ===")
        runTest {
            val users = createMockUsers(3)
            val viewModel = createViewModel(users)
            advanceUntilIdle()

            println("STEP 1: Selecting user at index 1 in SINGLE mode")
            viewModel.selectUser(users[1], UIKitConstants.SelectionMode.SINGLE)

            println("STEP 2: Asserting selectedUsers has 1 item")
            viewModel.selectedUsers.value shouldHaveSize 1
            viewModel.selectedUsers.value.first().uid shouldBe "user-2"
            println("RESULT: selectedUsers=[${viewModel.selectedUsers.value.map { it.uid }}]")
        }
    }

    test("selectUser in SINGLE mode → selecting another replaces previous") {
        println("=== TEST: SINGLE selection replaces previous ===")
        runTest {
            val users = createMockUsers(3)
            val viewModel = createViewModel(users)
            advanceUntilIdle()

            println("STEP 1: Selecting user-1")
            viewModel.selectUser(users[0], UIKitConstants.SelectionMode.SINGLE)
            viewModel.selectedUsers.value shouldHaveSize 1

            println("STEP 2: Selecting user-3 (should replace user-1)")
            viewModel.selectUser(users[2], UIKitConstants.SelectionMode.SINGLE)
            viewModel.selectedUsers.value shouldHaveSize 1
            viewModel.selectedUsers.value.first().uid shouldBe "user-3"
            println("RESULT: Selection replaced — selectedUsers=[user-3]")
        }
    }

    test("selectUser in MULTIPLE mode → toggle behavior") {
        println("=== TEST: MULTIPLE selection toggle behavior ===")
        runTest {
            val users = createMockUsers(3)
            val viewModel = createViewModel(users)
            advanceUntilIdle()

            println("STEP 1: Selecting user-1 and user-2")
            viewModel.selectUser(users[0], UIKitConstants.SelectionMode.MULTIPLE)
            viewModel.selectUser(users[1], UIKitConstants.SelectionMode.MULTIPLE)
            viewModel.selectedUsers.value shouldHaveSize 2
            println("  → After 2 selections: size=${viewModel.selectedUsers.value.size}")

            println("STEP 2: Deselecting user-1 (toggle)")
            viewModel.selectUser(users[0], UIKitConstants.SelectionMode.MULTIPLE)
            viewModel.selectedUsers.value shouldHaveSize 1
            viewModel.selectedUsers.value.first().uid shouldBe "user-2"
            println("RESULT: Toggle deselected user-1 — selectedUsers=[user-2]")
        }
    }

    test("clearSelection → empties selected set") {
        println("=== TEST: clearSelection empties set ===")
        runTest {
            val users = createMockUsers(3)
            val viewModel = createViewModel(users)
            advanceUntilIdle()

            println("STEP 1: Selecting 2 users")
            viewModel.selectUser(users[0], UIKitConstants.SelectionMode.MULTIPLE)
            viewModel.selectUser(users[2], UIKitConstants.SelectionMode.MULTIPLE)
            viewModel.selectedUsers.value shouldHaveSize 2

            println("STEP 2: Clearing selection")
            viewModel.clearSelection()
            viewModel.selectedUsers.value shouldHaveSize 0
            println("RESULT: selectedUsers is empty after clearSelection")
        }
    }

    test("getSelectedUsers returns correct list") {
        println("=== TEST: getSelectedUsers returns correct list ===")
        runTest {
            val users = createMockUsers(5)
            val viewModel = createViewModel(users)
            advanceUntilIdle()

            println("STEP 1: Selecting users at indices 0, 2, 4")
            viewModel.selectUser(users[0], UIKitConstants.SelectionMode.MULTIPLE)
            viewModel.selectUser(users[2], UIKitConstants.SelectionMode.MULTIPLE)
            viewModel.selectUser(users[4], UIKitConstants.SelectionMode.MULTIPLE)

            println("STEP 2: Getting selected users")
            val selected = viewModel.getSelectedUsers()
            selected shouldHaveSize 3
            println("RESULT: getSelectedUsers() returned ${selected.size} users: ${selected.map { it.uid }}")
        }
    }

    test("getItemAt returns correct user for LazyColumn index") {
        println("=== TEST: getItemAt returns correct user ===")
        runTest {
            val users = createMockUsers(5)
            val viewModel = createViewModel(users)
            advanceUntilIdle()

            println("STEP 1: Getting items at various indices")
            val item0 = viewModel.getItemAt(0)
            val item2 = viewModel.getItemAt(2)
            val item4 = viewModel.getItemAt(4)
            val outOfBounds = viewModel.getItemAt(10)

            println("STEP 2: Asserting correct UIDs")
            item0?.uid shouldBe "user-1"
            item2?.uid shouldBe "user-3"
            item4?.uid shouldBe "user-5"
            outOfBounds shouldBe null
            println("RESULT: getItemAt(0)=user-1, getItemAt(2)=user-3, getItemAt(4)=user-5, getItemAt(10)=null")
        }
    }

    test("isSelected returns true for selected user, false for unselected") {
        println("=== TEST: isSelected correctness ===")
        runTest {
            val users = createMockUsers(3)
            val viewModel = createViewModel(users)
            advanceUntilIdle()

            println("STEP 1: Selecting user-2")
            viewModel.selectUser(users[1], UIKitConstants.SelectionMode.SINGLE)

            println("STEP 2: Checking isSelected for each user")
            viewModel.isSelected(users[0]) shouldBe false
            viewModel.isSelected(users[1]) shouldBe true
            viewModel.isSelected(users[2]) shouldBe false
            println("RESULT: isSelected(user-1)=false, isSelected(user-2)=true, isSelected(user-3)=false")
        }
    }
})

// ==================== Helper Functions ====================

private fun createMockUsers(count: Int): List<User> {
    return (1..count).map { i ->
        val user = mock<User>()
        whenever(user.uid).thenReturn("user-$i")
        whenever(user.name).thenReturn("User $i")
        whenever(user.status).thenReturn("online")
        whenever(user.avatar).thenReturn(null)
        whenever(user.isBlockedByMe).thenReturn(false)
        whenever(user.isHasBlockedMe).thenReturn(false)
        user
    }
}

private fun createViewModel(users: List<User>): CometChatUsersViewModel {
    val repository = object : UsersRepository {
        override suspend fun getUsers(request: UsersRequest) = Result.success(users)
        override fun hasMoreUsers() = users.isNotEmpty()
    }
    val fetchUseCase = FetchUsersUseCase(repository)
    val searchUseCase = SearchUsersUseCase(repository)
    val vm = CometChatUsersViewModel(fetchUseCase, searchUseCase, enableListeners = false)
    vm.fetchUsers() // Trigger initial fetch (ViewModel doesn't auto-fetch in init)
    return vm
}
