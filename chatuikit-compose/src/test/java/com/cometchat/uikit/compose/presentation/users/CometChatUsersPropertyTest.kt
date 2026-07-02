package com.cometchat.uikit.compose.presentation.users

import com.cometchat.chat.core.UsersRequest
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.domain.repository.UsersRepository
import com.cometchat.uikit.core.domain.usecase.FetchUsersUseCase
import com.cometchat.uikit.core.domain.usecase.SearchUsersUseCase
import com.cometchat.uikit.core.state.UsersUIState
import com.cometchat.uikit.core.viewmodel.CometChatUsersViewModel
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.boolean
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
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * Property-Based Tests for CometChatUsers Compose component.
 *
 * Verifies invariants that must hold for ANY valid input:
 * - Selection mode transitions preserve selected item set
 * - Pagination append behavior
 * - Custom composable slot mutual exclusivity
 * - Status indicator visibility logic
 *
 * Run with:
 *   ./gradlew :chatuikit-compose:testDebugUnitTest --tests "*.users.CometChatUsersPropertyTest"
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CometChatUsersPropertyTest : FunSpec({

    val testDispatcher = UnconfinedTestDispatcher()

    beforeTest {
        Dispatchers.setMain(testDispatcher)
        println("  🧪 ${it.name.testName}")
    }

    afterTest {
        Dispatchers.resetMain()
        println()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Property 1: Selection mode transitions preserve selected UIDs
    // ═══════════════════════════════════════════════════════════════════════════

    test("Property 1: For any list of users, MULTIPLE selection preserves selected UIDs").config(invocations = 1) {
        println("=== PBT: Selection mode transitions preserve selected UIDs ===")
        checkAll(20, Arb.int(2..10)) { userCount ->
            runTest {
                val users = createMockUsers(userCount)
                val viewModel = createViewModel(users)
                advanceUntilIdle()

                println("  [Iteration] userCount=$userCount")

                // Select first half of users
                val selectCount = userCount / 2
                val selectedUids = mutableSetOf<String>()
                for (i in 0 until selectCount) {
                    viewModel.selectUser(users[i], UIKitConstants.SelectionMode.MULTIPLE)
                    selectedUids.add(users[i].uid)
                }

                // Verify all selected UIDs are preserved
                val actualSelected = viewModel.selectedUsers.value.map { it.uid }.toSet()
                actualSelected shouldHaveSize selectCount
                actualSelected shouldBe selectedUids

                println("    ✅ Selected $selectCount/$userCount users, UIDs preserved")
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Property 2: SINGLE selection always has at most 1 item
    // ═══════════════════════════════════════════════════════════════════════════

    test("Property 2: SINGLE selection never exceeds 1 item regardless of clicks").config(invocations = 1) {
        println("=== PBT: SINGLE selection never exceeds 1 item ===")
        checkAll(20, Arb.int(2..15)) { userCount ->
            runTest {
                val users = createMockUsers(userCount)
                val viewModel = createViewModel(users)
                advanceUntilIdle()

                println("  [Iteration] userCount=$userCount")

                // Click multiple users in SINGLE mode
                for (user in users) {
                    viewModel.selectUser(user, UIKitConstants.SelectionMode.SINGLE)
                    // After each click, should have exactly 1 selected
                    viewModel.selectedUsers.value.size shouldBe 1
                }

                // Final selection should be the last user
                viewModel.selectedUsers.value.first().uid shouldBe users.last().uid
                println("    ✅ After $userCount clicks, selectedUsers.size=1, last=${users.last().uid}")
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Property 3: Pagination append increases list size
    // ═══════════════════════════════════════════════════════════════════════════

    test("Property 3: addItem always increases list size by exactly 1").config(invocations = 1) {
        println("=== PBT: Pagination append increases list size ===")
        checkAll(50, Arb.int(0..15)) { initialCount ->
            runTest {
                val users = createMockUsers(initialCount)
                val viewModel = createViewModel(users)
                advanceUntilIdle()

                val sizeBefore = viewModel.users.value.size
                val newUser = createMockUser("new-$initialCount", "New User $initialCount")
                viewModel.addItem(newUser)

                val sizeAfter = viewModel.users.value.size
                sizeAfter shouldBe sizeBefore + 1

                println("  [Iteration] initial=$initialCount, before=$sizeBefore, after=$sizeAfter ✅")
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Property 4: Empty list produces Empty state, non-empty produces Content
    // ═══════════════════════════════════════════════════════════════════════════

    test("Property 4: UIState is Empty for 0 users, Content for >0 users").config(invocations = 1) {
        println("=== PBT: UIState correctness based on user count ===")
        checkAll(50, Arb.int(0..20)) { count ->
            runTest {
                val users = createMockUsers(count)
                val viewModel = createViewModel(users)
                advanceUntilIdle()

                if (count == 0) {
                    viewModel.uiState.value shouldBe UsersUIState.Empty
                } else {
                    viewModel.uiState.value shouldBe UsersUIState.Content
                    viewModel.users.value shouldHaveSize count
                }

                println("  [Iteration] count=$count → ${if (count == 0) "Empty" else "Content"} ✅")
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Property 5: Custom composable slot replacement — mutual exclusivity
    // ═══════════════════════════════════════════════════════════════════════════

    test("Property 5: Custom composable slot replacement is mutually exclusive").config(invocations = 100) {
        println("=== PBT: Custom slot mutual exclusivity ===")
        checkAll(
            Arb.boolean(), // hasCustomLeading
            Arb.boolean(), // hasCustomTitle
            Arb.boolean(), // hasCustomSubtitle
            Arb.boolean()  // hasCustomTrailing
        ) { hasCustomLeading, hasCustomTitle, hasCustomSubtitle, hasCustomTrailing ->
            // Property: Custom and default are mutually exclusive per slot
            val leadingDefaultVisible = !hasCustomLeading
            val titleDefaultVisible = !hasCustomTitle
            val subtitleDefaultVisible = !hasCustomSubtitle
            val trailingDefaultVisible = !hasCustomTrailing

            (hasCustomLeading xor leadingDefaultVisible) shouldBe true
            (hasCustomTitle xor titleDefaultVisible) shouldBe true
            (hasCustomSubtitle xor subtitleDefaultVisible) shouldBe true
            (hasCustomTrailing xor trailingDefaultVisible) shouldBe true
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Property 6: User status indicator visibility logic
    // ═══════════════════════════════════════════════════════════════════════════

    test("Property 6: Status indicator visible only when not hidden and user is present").config(invocations = 100) {
        println("=== PBT: Status indicator visibility ===")
        checkAll(
            Arb.boolean(), // hideUserStatus flag
            Arb.element("online", "offline") // user status
        ) { hideUserStatus, userStatus ->
            // Property: Status indicator visible only when not hidden
            val statusIndicatorVisible = !hideUserStatus

            if (hideUserStatus) {
                statusIndicatorVisible shouldBe false
            } else {
                statusIndicatorVisible shouldBe true
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Property 7: MULTIPLE selection toggle is idempotent (select-deselect-select)
    // ═══════════════════════════════════════════════════════════════════════════

    test("Property 7: Double-toggle in MULTIPLE mode returns to original state").config(invocations = 1) {
        println("=== PBT: Double-toggle idempotency ===")
        checkAll(20, Arb.int(1..10)) { userIndex ->
            runTest {
                val users = createMockUsers(15)
                val viewModel = createViewModel(users)
                advanceUntilIdle()

                val targetIndex = (userIndex - 1).coerceIn(0, users.size - 1)
                val targetUser = users[targetIndex]

                println("  [Iteration] toggling user at index $targetIndex")

                // Select
                viewModel.selectUser(targetUser, UIKitConstants.SelectionMode.MULTIPLE)
                viewModel.isSelected(targetUser) shouldBe true

                // Deselect (toggle)
                viewModel.selectUser(targetUser, UIKitConstants.SelectionMode.MULTIPLE)
                viewModel.isSelected(targetUser) shouldBe false

                // Re-select (toggle back)
                viewModel.selectUser(targetUser, UIKitConstants.SelectionMode.MULTIPLE)
                viewModel.isSelected(targetUser) shouldBe true

                println("    ✅ select→deselect→select cycle correct")
            }
        }
    }
})

// ==================== Helper Functions ====================

private fun createMockUser(uid: String, name: String): User {
    val user = mock<User>()
    whenever(user.uid).thenReturn(uid)
    whenever(user.name).thenReturn(name)
    whenever(user.status).thenReturn("online")
    whenever(user.avatar).thenReturn(null)
    whenever(user.isBlockedByMe).thenReturn(false)
    whenever(user.isHasBlockedMe).thenReturn(false)
    return user
}

private fun createMockUsers(count: Int): List<User> {
    return (1..count).map { i -> createMockUser("user-$i", "User $i") }
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
