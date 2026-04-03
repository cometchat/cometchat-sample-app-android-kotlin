package com.cometchat.uikit.core.viewmodel

import com.cometchat.chat.core.UsersRequest
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.domain.usecase.FetchUsersUseCase
import com.cometchat.uikit.core.domain.usecase.SearchUsersUseCase
import com.cometchat.uikit.core.state.UIState
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

/**
 * Property-based tests for [CometChatUsersViewModel].
 * Each test validates a correctness property from the design document.
 *
 * Feature: users-component
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CometChatUsersViewModelPropertyTest : FunSpec({

    // ==================== Test Helpers ====================

    fun createMockUser(uid: String, name: String = "User $uid"): User {
        return User().apply {
            this.uid = uid
            this.name = name
        }
    }

    fun arbUniqueUsers(range: IntRange = 0..20) = arbitrary { rs ->
        val count = Arb.int(range).bind()
        (0 until count).map { index ->
            createMockUser("uid_$index", "User $index")
        }
    }

    /**
     * Creates a ViewModel for testing ListOperations.
     * Users are added directly via addItem after creation.
     */
    fun createViewModelForListOps(users: List<User>): CometChatUsersViewModel {
        val fetchUsersUseCase = object : FetchUsersUseCase(
            repository = object : com.cometchat.uikit.core.domain.repository.UsersRepository {
                override suspend fun getUsers(request: UsersRequest): Result<List<User>> {
                    return Result.success(emptyList())
                }
                override fun hasMoreUsers(): Boolean = false
            }
        ) {}

        val searchUsersUseCase = object : SearchUsersUseCase(
            repository = object : com.cometchat.uikit.core.domain.repository.UsersRepository {
                override suspend fun getUsers(request: UsersRequest): Result<List<User>> {
                    return Result.success(emptyList())
                }
                override fun hasMoreUsers(): Boolean = false
            }
        ) {}

        val viewModel = CometChatUsersViewModel(
            fetchUsersUseCase = fetchUsersUseCase,
            searchUsersUseCase = searchUsersUseCase,
            enableListeners = false
        )
        
        // Clear any state and add users one by one
        viewModel.clearItems()
        users.forEach { user ->
            viewModel.addItem(user)
        }
        return viewModel
    }

    /**
     * Creates a ViewModel that fetches users from the mock repository.
     */
    fun createViewModelForFetch(
        users: List<User> = emptyList(),
        error: CometChatException? = null
    ): CometChatUsersViewModel {
        val fetchUsersUseCase = object : FetchUsersUseCase(
            repository = object : com.cometchat.uikit.core.domain.repository.UsersRepository {
                override suspend fun getUsers(request: UsersRequest): Result<List<User>> {
                    return if (error != null) Result.failure(error) else Result.success(users)
                }
                override fun hasMoreUsers(): Boolean = false
            }
        ) {}

        val searchUsersUseCase = object : SearchUsersUseCase(
            repository = object : com.cometchat.uikit.core.domain.repository.UsersRepository {
                override suspend fun getUsers(request: UsersRequest): Result<List<User>> {
                    return Result.success(emptyList())
                }
                override fun hasMoreUsers(): Boolean = false
            }
        ) {}

        return CometChatUsersViewModel(
            fetchUsersUseCase = fetchUsersUseCase,
            searchUsersUseCase = searchUsersUseCase,
            enableListeners = false
        )
    }

    // ==================== Property 1: ListOperations Correctness ====================

    /**
     * Feature: users-component, Property 1: ListOperations Correctness
     * *For any* sequence of ListOperations (add, remove, update, clear) on the ViewModel,
     * the resulting users list should reflect all operations applied in order,
     * and the list should never contain duplicate users (by uid).
     * **Validates: Requirements 1.7**
     */
    test("Property 1: addItem adds user and maintains no duplicates by uid") {
        checkAll(100, arbUniqueUsers(0..10)) { initialUsers ->
            val testDispatcher = UnconfinedTestDispatcher()
            Dispatchers.setMain(testDispatcher)
            try {
                runTest(testDispatcher) {
                    val viewModel = createViewModelForListOps(initialUsers)
                    
                    val newUser = createMockUser("new_uid_${System.nanoTime()}", "New User")
                    val sizeBefore = viewModel.getItemCount()

                    viewModel.addItem(newUser)

                    viewModel.getItemCount() shouldBe sizeBefore + 1
                    viewModel.getItems() shouldContain newUser

                    val uids = viewModel.getItems().map { it.uid }
                    uids.distinct().size shouldBe uids.size
                }
            } finally {
                Dispatchers.resetMain()
            }
        }
    }

    test("Property 1: removeItem removes user correctly") {
        checkAll(100, arbUniqueUsers(1..10)) { initialUsers ->
            val testDispatcher = UnconfinedTestDispatcher()
            Dispatchers.setMain(testDispatcher)
            try {
                runTest(testDispatcher) {
                    val viewModel = createViewModelForListOps(initialUsers)
                    
                    val users = viewModel.getItems()
                    if (users.isNotEmpty()) {
                        val userToRemove = users.first()
                        val sizeBefore = viewModel.getItemCount()

                        val result = viewModel.removeItem(userToRemove)

                        result shouldBe true
                        viewModel.getItemCount() shouldBe sizeBefore - 1
                        viewModel.getItems().none { it.uid == userToRemove.uid } shouldBe true
                    }
                }
            } finally {
                Dispatchers.resetMain()
            }
        }
    }

    test("Property 1: removeItem returns false for non-existent user") {
        checkAll(100, arbUniqueUsers(0..10)) { initialUsers ->
            val testDispatcher = UnconfinedTestDispatcher()
            Dispatchers.setMain(testDispatcher)
            try {
                runTest(testDispatcher) {
                    val viewModel = createViewModelForListOps(initialUsers)
                    
                    val nonExistentUser = createMockUser("non_existent_uid", "Non Existent")
                    val sizeBefore = viewModel.getItemCount()

                    val result = viewModel.removeItem(nonExistentUser)

                    result shouldBe false
                    viewModel.getItemCount() shouldBe sizeBefore
                }
            } finally {
                Dispatchers.resetMain()
            }
        }
    }

    test("Property 1: updateItem updates user correctly") {
        checkAll(100, arbUniqueUsers(1..10)) { initialUsers ->
            val testDispatcher = UnconfinedTestDispatcher()
            Dispatchers.setMain(testDispatcher)
            try {
                runTest(testDispatcher) {
                    val viewModel = createViewModelForListOps(initialUsers)
                    
                    val users = viewModel.getItems()
                    // Verify users were added
                    users.size shouldBe initialUsers.size
                    
                    if (users.isNotEmpty()) {
                        val userToUpdate = users.first()
                        val updatedUser = createMockUser(userToUpdate.uid, "Updated Name")
                        val sizeBefore = viewModel.getItemCount()

                        val result = viewModel.updateItem(updatedUser) { it.uid == userToUpdate.uid }

                        result shouldBe true
                        viewModel.getItemCount() shouldBe sizeBefore
                        viewModel.getItems().any { it.uid == updatedUser.uid && it.name == "Updated Name" } shouldBe true
                    }
                }
            } finally {
                Dispatchers.resetMain()
            }
        }
    }

    test("Property 1: clearItems empties the list") {
        checkAll(100, arbUniqueUsers(0..20)) { initialUsers ->
            val testDispatcher = UnconfinedTestDispatcher()
            Dispatchers.setMain(testDispatcher)
            try {
                runTest(testDispatcher) {
                    val viewModel = createViewModelForListOps(initialUsers)
                    
                    viewModel.clearItems()

                    viewModel.getItemCount() shouldBe 0
                    viewModel.getItems() shouldBe emptyList()
                }
            } finally {
                Dispatchers.resetMain()
            }
        }
    }

    test("Property 1: sequence of operations maintains list integrity") {
        checkAll(100, arbUniqueUsers(5..15)) { initialUsers ->
            val testDispatcher = UnconfinedTestDispatcher()
            Dispatchers.setMain(testDispatcher)
            try {
                runTest(testDispatcher) {
                    val viewModel = createViewModelForListOps(initialUsers)
                    
                    val users = viewModel.getItems()
                    if (users.size >= 2) {
                        val newUser = createMockUser("seq_new_uid", "Seq New User")
                        viewModel.addItem(newUser)
                        viewModel.getItems() shouldContain newUser

                        val userToRemove = users.first()
                        viewModel.removeItem(userToRemove)
                        viewModel.getItems().none { it.uid == userToRemove.uid } shouldBe true

                        val userToUpdate = users.last()
                        val updatedUser = createMockUser(userToUpdate.uid, "Seq Updated")
                        viewModel.updateItem(updatedUser) { it.uid == userToUpdate.uid }
                        viewModel.getItems().any { it.uid == updatedUser.uid && it.name == "Seq Updated" } shouldBe true

                        viewModel.clearItems()
                        viewModel.getItemCount() shouldBe 0
                    }
                }
            } finally {
                Dispatchers.resetMain()
            }
        }
    }


    // ==================== Property 2: State Transitions Based on Fetch Results ====================

    /**
     * Feature: users-component, Property 2: State Transitions Based on Fetch Results
     * *For any* fetch operation, if the result is successful with non-empty users,
     * uiState should be Content; if successful with empty users, uiState should be Empty;
     * if failed, uiState should be Error with the exception.
     * **Validates: Requirements 1.10, 1.11, 1.12**
     */
    test("Property 2: Successful fetch with non-empty users sets uiState to Content") {
        checkAll(100, arbUniqueUsers(1..20)) { users ->
            val testDispatcher = UnconfinedTestDispatcher()
            Dispatchers.setMain(testDispatcher)
            try {
                runTest(testDispatcher) {
                    val viewModel = createViewModelForFetch(users)
                    // UnconfinedTestDispatcher executes coroutines eagerly
                    viewModel.uiState.value shouldBe UIState.Content
                }
            } finally {
                Dispatchers.resetMain()
            }
        }
    }

    test("Property 2: Successful fetch with empty users sets uiState to Empty") {
        val testDispatcher = UnconfinedTestDispatcher()
        Dispatchers.setMain(testDispatcher)
        try {
            runTest(testDispatcher) {
                val viewModel = createViewModelForFetch(emptyList())
                viewModel.uiState.value shouldBe UIState.Empty
            }
        } finally {
            Dispatchers.resetMain()
        }
    }

    test("Property 2: Failed fetch sets uiState to Error with exception") {
        checkAll(100, Arb.string(5..50)) { errorMessage ->
            val testDispatcher = UnconfinedTestDispatcher()
            Dispatchers.setMain(testDispatcher)
            try {
                runTest(testDispatcher) {
                    val exception = CometChatException("TEST_ERROR", errorMessage)
                    val viewModel = createViewModelForFetch(error = exception)

                    val state = viewModel.uiState.value
                    state.shouldBeInstanceOf<UIState.Error>()
                    (state as UIState.Error).exception.message shouldBe errorMessage
                }
            } finally {
                Dispatchers.resetMain()
            }
        }
    }

    test("Property 2: Initial state is Loading before fetch completes") {
        val testDispatcher = UnconfinedTestDispatcher()
        Dispatchers.setMain(testDispatcher)
        try {
            runTest(testDispatcher) {
                val fetchUsersUseCase = object : FetchUsersUseCase(
                    repository = object : com.cometchat.uikit.core.domain.repository.UsersRepository {
                        override suspend fun getUsers(request: UsersRequest): Result<List<User>> {
                            kotlinx.coroutines.delay(Long.MAX_VALUE)
                            return Result.success(emptyList())
                        }
                        override fun hasMoreUsers(): Boolean = false
                    }
                ) {}

                val searchUsersUseCase = object : SearchUsersUseCase(
                    repository = object : com.cometchat.uikit.core.domain.repository.UsersRepository {
                        override suspend fun getUsers(request: UsersRequest): Result<List<User>> {
                            return Result.success(emptyList())
                        }
                        override fun hasMoreUsers(): Boolean = false
                    }
                ) {}

                val viewModel = CometChatUsersViewModel(
                    fetchUsersUseCase = fetchUsersUseCase,
                    searchUsersUseCase = searchUsersUseCase,
                    enableListeners = false
                )
                
                viewModel.uiState.value shouldBe UIState.Loading
            }
        } finally {
            Dispatchers.resetMain()
        }
    }

    // ==================== Property 6: Selection Mode Behavior ====================

    /**
     * Feature: users-component, Property 6: Selection Mode Behavior
     * *For any* sequence of user selections:
     * - In SINGLE mode, only the last selected user should be in the selection set
     * - In MULTIPLE mode, all selected users should be in the selection set
     * - The selection count should always equal the size of the selection set
     * **Validates: Requirements 5.3, 5.4, 5.6**
     */
    test("Property 6: SINGLE mode keeps only the last selected user") {
        checkAll(100, arbUniqueUsers(3..10)) { initialUsers ->
            val testDispatcher = UnconfinedTestDispatcher()
            Dispatchers.setMain(testDispatcher)
            try {
                runTest(testDispatcher) {
                    val viewModel = createViewModelForListOps(initialUsers)
                    
                    val users = viewModel.getItems()
                    if (users.size >= 3) {
                        users.forEach { user ->
                            viewModel.selectUser(user, UIKitConstants.SelectionMode.SINGLE)
                        }

                        viewModel.selectedUsers.value shouldHaveSize 1
                        viewModel.selectedUsers.value.first().uid shouldBe users.last().uid
                        viewModel.getSelectedUsers().size shouldBe viewModel.selectedUsers.value.size
                    }
                }
            } finally {
                Dispatchers.resetMain()
            }
        }
    }

    test("Property 6: MULTIPLE mode keeps all selected users") {
        checkAll(100, arbUniqueUsers(2..10)) { initialUsers ->
            val testDispatcher = UnconfinedTestDispatcher()
            Dispatchers.setMain(testDispatcher)
            try {
                runTest(testDispatcher) {
                    val viewModel = createViewModelForListOps(initialUsers)
                    
                    val users = viewModel.getItems()
                    if (users.size >= 2) {
                        users.forEach { user ->
                            viewModel.selectUser(user, UIKitConstants.SelectionMode.MULTIPLE)
                        }

                        viewModel.selectedUsers.value shouldHaveSize users.size
                        viewModel.getSelectedUsers().size shouldBe viewModel.selectedUsers.value.size
                        
                        users.forEach { user ->
                            viewModel.isSelected(user) shouldBe true
                        }
                    }
                }
            } finally {
                Dispatchers.resetMain()
            }
        }
    }

    test("Property 6: MULTIPLE mode toggles selection on re-select") {
        checkAll(100, arbUniqueUsers(3..10)) { initialUsers ->
            val testDispatcher = UnconfinedTestDispatcher()
            Dispatchers.setMain(testDispatcher)
            try {
                runTest(testDispatcher) {
                    val viewModel = createViewModelForListOps(initialUsers)
                    
                    val users = viewModel.getItems()
                    if (users.isNotEmpty()) {
                        val userToToggle = users.first()

                        viewModel.selectUser(userToToggle, UIKitConstants.SelectionMode.MULTIPLE)
                        viewModel.isSelected(userToToggle) shouldBe true

                        viewModel.selectUser(userToToggle, UIKitConstants.SelectionMode.MULTIPLE)
                        viewModel.isSelected(userToToggle) shouldBe false

                        viewModel.selectedUsers.value shouldHaveSize 0
                    }
                }
            } finally {
                Dispatchers.resetMain()
            }
        }
    }

    test("Property 6: NONE mode does not add to selection") {
        checkAll(100, arbUniqueUsers(1..10)) { initialUsers ->
            val testDispatcher = UnconfinedTestDispatcher()
            Dispatchers.setMain(testDispatcher)
            try {
                runTest(testDispatcher) {
                    val viewModel = createViewModelForListOps(initialUsers)
                    
                    val users = viewModel.getItems()
                    users.forEach { user ->
                        viewModel.selectUser(user, UIKitConstants.SelectionMode.NONE)
                    }

                    viewModel.selectedUsers.value shouldHaveSize 0
                    viewModel.getSelectedUsers().size shouldBe 0
                }
            } finally {
                Dispatchers.resetMain()
            }
        }
    }

    test("Property 6: clearSelection removes all selected users") {
        checkAll(100, arbUniqueUsers(2..10)) { initialUsers ->
            val testDispatcher = UnconfinedTestDispatcher()
            Dispatchers.setMain(testDispatcher)
            try {
                runTest(testDispatcher) {
                    val viewModel = createViewModelForListOps(initialUsers)
                    
                    val users = viewModel.getItems()
                    if (users.size >= 2) {
                        users.forEach { user ->
                            viewModel.selectUser(user, UIKitConstants.SelectionMode.MULTIPLE)
                        }
                        viewModel.selectedUsers.value shouldHaveSize users.size

                        viewModel.clearSelection()

                        viewModel.selectedUsers.value shouldHaveSize 0
                        viewModel.getSelectedUsers().size shouldBe 0
                    }
                }
            } finally {
                Dispatchers.resetMain()
            }
        }
    }

    test("Property 6: Selection count always equals selection set size") {
        checkAll(100, arbUniqueUsers(5..15), Arb.int(1..5)) { initialUsers, selectCount ->
            val testDispatcher = UnconfinedTestDispatcher()
            Dispatchers.setMain(testDispatcher)
            try {
                runTest(testDispatcher) {
                    val viewModel = createViewModelForListOps(initialUsers)
                    
                    val users = viewModel.getItems()
                    val usersToSelect = users.take(selectCount.coerceAtMost(users.size))
                    
                    usersToSelect.forEach { user ->
                        viewModel.selectUser(user, UIKitConstants.SelectionMode.MULTIPLE)
                    }

                    viewModel.getSelectedUsers().size shouldBe viewModel.selectedUsers.value.size
                    viewModel.selectedUsers.value.size shouldBe usersToSelect.size
                }
            } finally {
                Dispatchers.resetMain()
            }
        }
    }


    // ==================== Property 9: Pagination Prevents Concurrent Fetches ====================

    /**
     * Feature: users-component, Property 9: Pagination Prevents Concurrent Fetches
     * *For any* concurrent fetch attempts, only one fetch should execute at a time.
     * The isFetching flag should prevent duplicate requests.
     * When hasMore is false, no further fetches should occur.
     * **Validates: Requirements 8.3, 8.4**
     */
    test("Property 9: Concurrent fetch calls are prevented by isFetching flag") {
        val testDispatcher = UnconfinedTestDispatcher()
        Dispatchers.setMain(testDispatcher)
        try {
            runTest(testDispatcher) {
                var fetchCallCount = 0
                
                val fetchUsersUseCase = object : FetchUsersUseCase(
                    repository = object : com.cometchat.uikit.core.domain.repository.UsersRepository {
                        override suspend fun getUsers(request: UsersRequest): Result<List<User>> {
                            fetchCallCount++
                            kotlinx.coroutines.delay(100)
                            return Result.success(listOf(createMockUser("user_$fetchCallCount")))
                        }
                        override fun hasMoreUsers(): Boolean = true
                    }
                ) {}

                val searchUsersUseCase = object : SearchUsersUseCase(
                    repository = object : com.cometchat.uikit.core.domain.repository.UsersRepository {
                        override suspend fun getUsers(request: UsersRequest): Result<List<User>> {
                            return Result.success(emptyList())
                        }
                        override fun hasMoreUsers(): Boolean = false
                    }
                ) {}

                val viewModel = CometChatUsersViewModel(
                    fetchUsersUseCase = fetchUsersUseCase,
                    searchUsersUseCase = searchUsersUseCase,
                    enableListeners = false
                )

                // Try concurrent fetches - should be blocked by isFetching
                viewModel.fetchUsers()
                viewModel.fetchUsers()
                viewModel.fetchUsers()

                // Only one fetch should execute
                fetchCallCount shouldBe 1
            }
        } finally {
            Dispatchers.resetMain()
        }
    }

    test("Property 9: No fetch when hasMore is false") {
        val testDispatcher = UnconfinedTestDispatcher()
        Dispatchers.setMain(testDispatcher)
        try {
            runTest(testDispatcher) {
                var fetchCallCount = 0

                val fetchUsersUseCase = object : FetchUsersUseCase(
                    repository = object : com.cometchat.uikit.core.domain.repository.UsersRepository {
                        override suspend fun getUsers(request: UsersRequest): Result<List<User>> {
                            fetchCallCount++
                            return Result.success(emptyList())
                        }
                        override fun hasMoreUsers(): Boolean = false
                    }
                ) {}

                val searchUsersUseCase = object : SearchUsersUseCase(
                    repository = object : com.cometchat.uikit.core.domain.repository.UsersRepository {
                        override suspend fun getUsers(request: UsersRequest): Result<List<User>> {
                            return Result.success(emptyList())
                        }
                        override fun hasMoreUsers(): Boolean = false
                    }
                ) {}

                val viewModel = CometChatUsersViewModel(
                    fetchUsersUseCase = fetchUsersUseCase,
                    searchUsersUseCase = searchUsersUseCase,
                    enableListeners = false
                )

                val initialFetchCount = fetchCallCount

                // Try to fetch more - should be blocked
                viewModel.fetchUsers()
                viewModel.fetchUsers()

                fetchCallCount shouldBe initialFetchCount
            }
        } finally {
            Dispatchers.resetMain()
        }
    }

    test("Property 9: Sequential fetches work after previous completes") {
        val testDispatcher = UnconfinedTestDispatcher()
        Dispatchers.setMain(testDispatcher)
        try {
            runTest(testDispatcher) {
                var fetchCallCount = 0

                val fetchUsersUseCase = object : FetchUsersUseCase(
                    repository = object : com.cometchat.uikit.core.domain.repository.UsersRepository {
                        override suspend fun getUsers(request: UsersRequest): Result<List<User>> {
                            fetchCallCount++
                            return Result.success(listOf(createMockUser("user_$fetchCallCount")))
                        }
                        override fun hasMoreUsers(): Boolean = true
                    }
                ) {}

                val searchUsersUseCase = object : SearchUsersUseCase(
                    repository = object : com.cometchat.uikit.core.domain.repository.UsersRepository {
                        override suspend fun getUsers(request: UsersRequest): Result<List<User>> {
                            return Result.success(emptyList())
                        }
                        override fun hasMoreUsers(): Boolean = false
                    }
                ) {}

                val viewModel = CometChatUsersViewModel(
                    fetchUsersUseCase = fetchUsersUseCase,
                    searchUsersUseCase = searchUsersUseCase,
                    enableListeners = false
                )

                fetchCallCount shouldBe 1

                viewModel.fetchUsers()
                fetchCallCount shouldBe 2

                viewModel.fetchUsers()
                fetchCallCount shouldBe 3
            }
        } finally {
            Dispatchers.resetMain()
        }
    }

    test("Property 9: refreshList resets pagination state and fetches") {
        val testDispatcher = UnconfinedTestDispatcher()
        Dispatchers.setMain(testDispatcher)
        try {
            runTest(testDispatcher) {
                var fetchCallCount = 0

                val fetchUsersUseCase = object : FetchUsersUseCase(
                    repository = object : com.cometchat.uikit.core.domain.repository.UsersRepository {
                        override suspend fun getUsers(request: UsersRequest): Result<List<User>> {
                            fetchCallCount++
                            return if (fetchCallCount == 1) {
                                Result.success(listOf(createMockUser("user_1")))
                            } else {
                                Result.success(emptyList())
                            }
                        }
                        override fun hasMoreUsers(): Boolean = fetchCallCount < 2
                    }
                ) {}

                val searchUsersUseCase = object : SearchUsersUseCase(
                    repository = object : com.cometchat.uikit.core.domain.repository.UsersRepository {
                        override suspend fun getUsers(request: UsersRequest): Result<List<User>> {
                            return Result.success(emptyList())
                        }
                        override fun hasMoreUsers(): Boolean = false
                    }
                ) {}

                val viewModel = CometChatUsersViewModel(
                    fetchUsersUseCase = fetchUsersUseCase,
                    searchUsersUseCase = searchUsersUseCase,
                    enableListeners = false
                )

                fetchCallCount shouldBe 1

                fetchCallCount = 0
                viewModel.refreshList()

                fetchCallCount shouldBe 1
            }
        } finally {
            Dispatchers.resetMain()
        }
    }
})
