package com.cometchat.uikit.compose.presentation.users

import com.cometchat.chat.core.UsersRequest
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.domain.repository.UsersRepository
import com.cometchat.uikit.core.domain.usecase.FetchUsersUseCase
import com.cometchat.uikit.core.domain.usecase.SearchUsersUseCase
import com.cometchat.uikit.core.state.UsersUIState
import com.cometchat.uikit.core.viewmodel.CometChatUsersViewModel
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
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * Tests for CometChatUsers Compose component rendering states.
 *
 * Verifies that the ViewModel produces the correct UsersUIState for each scenario,
 * which the Compose component observes to show/hide composables.
 *
 * These tests validate the ViewModel state that drives rendering:
 * - UsersUIState.Loading → component shows shimmer/loading
 * - UsersUIState.Empty → component shows empty state composable
 * - UsersUIState.Error → component shows error state composable
 * - UsersUIState.Content → component shows LazyColumn with user items
 *
 * Mirrors: chatuikit-kotlin CometChatUsersRenderingTest (if exists)
 *
 * Run with:
 *   ./gradlew :chatuikit-compose:testDebugUnitTest --tests "*.users.CometChatUsersRenderingTest"
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CometChatUsersRenderingTest : FunSpec({

    val testDispatcher = UnconfinedTestDispatcher()

    beforeTest {
        Dispatchers.setMain(testDispatcher)
        println("  🧪 ${it.name.testName}")
    }

    afterTest {
        Dispatchers.resetMain()
        println()
    }

    test("ViewModel with empty repository → UsersUIState.Empty (drives empty state composable)") {
        println("=== TEST: Empty repository produces Empty state ===")
        runTest {
            println("STEP 1: Creating ViewModel with empty user list")
            val viewModel = createViewModel(emptyList())
            advanceUntilIdle()

            println("STEP 2: Asserting UIState is Empty")
            viewModel.uiState.value shouldBe UsersUIState.Empty
            viewModel.users.value shouldHaveSize 0
            println("RESULT: uiState=${viewModel.uiState.value}, users.size=${viewModel.users.value.size}")
        }
    }

    test("ViewModel with error repository → UsersUIState.Error (drives error state composable)") {
        println("=== TEST: Error repository produces Error state ===")
        runTest {
            println("STEP 1: Creating ViewModel with failing repository")
            val viewModel = createViewModelWithError("LOAD_ERR", "Failed to load users")
            advanceUntilIdle()

            println("STEP 2: Asserting UIState is Error")
            viewModel.uiState.value.shouldBeInstanceOf<UsersUIState.Error>()
            val error = viewModel.uiState.value as UsersUIState.Error
            println("RESULT: uiState=Error, code=${error.exception.code}")
        }
    }

    test("ViewModel with users → UsersUIState.Content with correct item count") {
        println("=== TEST: Users repository produces Content state ===")
        runTest {
            println("STEP 1: Creating ViewModel with 5 users")
            val users = createMockUsers(5)
            val viewModel = createViewModel(users)
            advanceUntilIdle()

            println("STEP 2: Asserting UIState is Content with 5 items")
            viewModel.uiState.value shouldBe UsersUIState.Content
            viewModel.users.value shouldHaveSize 5
            println("RESULT: uiState=${viewModel.uiState.value}, users.size=${viewModel.users.value.size}")
        }
    }

    test("ViewModel users contain correct user data for rendering") {
        println("=== TEST: User data is correctly accessible for composable rendering ===")
        runTest {
            println("STEP 1: Creating ViewModel with 3 users")
            val users = createMockUsers(3)
            val viewModel = createViewModel(users)
            advanceUntilIdle()

            println("STEP 2: Verifying each user's data")
            val items = viewModel.users.value
            items shouldHaveSize 3

            items.forEachIndexed { index, user ->
                user.uid shouldBe "user-${index + 1}"
                user.name shouldBe "User ${index + 1}"
                println("  → User[$index]: uid=${user.uid}, name=${user.name}")
            }
            println("RESULT: All 3 users have correct uid and name")
        }
    }

    test("ViewModel initial state is Loading before fetch completes") {
        println("=== TEST: Initial state is Loading ===")
        runTest {
            println("STEP 1: Creating ViewModel (fetch not yet triggered)")
            val repository = object : UsersRepository {
                override suspend fun getUsers(request: UsersRequest): Result<List<User>> {
                    return Result.success(createMockUsers(3))
                }
                override fun hasMoreUsers() = true
            }
            val fetchUseCase = FetchUsersUseCase(repository)
            val searchUseCase = SearchUsersUseCase(repository)
            val viewModel = CometChatUsersViewModel(fetchUseCase, searchUseCase, enableListeners = false)

            println("STEP 2: Before fetchUsers, state should be Loading")
            viewModel.uiState.value shouldBe UsersUIState.Loading
            println("RESULT: Initial state is Loading as expected")
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

private fun createViewModelWithError(code: String, message: String): CometChatUsersViewModel {
    val repository = object : UsersRepository {
        override suspend fun getUsers(request: UsersRequest) =
            Result.failure<List<User>>(CometChatException(code, message))
        override fun hasMoreUsers() = true
    }
    val fetchUseCase = FetchUsersUseCase(repository)
    val searchUseCase = SearchUsersUseCase(repository)
    val vm = CometChatUsersViewModel(fetchUseCase, searchUseCase, enableListeners = false)
    vm.fetchUsers() // Trigger initial fetch
    return vm
}
