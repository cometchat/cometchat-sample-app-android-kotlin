package com.cometchat.uikit.core.viewmodel

import com.cometchat.chat.core.GroupMembersRequest
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.GroupMember
import com.cometchat.uikit.core.domain.usecase.BanGroupMemberUseCase
import com.cometchat.uikit.core.domain.usecase.ChangeMemberScopeUseCase
import com.cometchat.uikit.core.domain.usecase.FetchGroupMembersUseCase
import com.cometchat.uikit.core.domain.usecase.KickGroupMemberUseCase
import com.cometchat.uikit.core.state.GroupMembersUIState
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.string
import io.kotest.property.arbitrary.element
import io.kotest.property.checkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

/**
 * Unit tests for CometChatGroupMembersViewModel state management.
 * 
 * These tests validate the ViewModel's state transitions and member management
 * using mock use cases to avoid SDK dependencies.
 * 
 * **Validates: Requirements 1.1, 1.2, 1.3, 1.4, 1.5**
 * 
 * ## Test Coverage
 * 
 * | Test | Property | Requirements |
 * |------|----------|--------------|
 * | Initial state is Loading | Property 1: Initial State Correctness | 1.1 |
 * | fetchGroupMembers updates state to Content | Property 2: Fetch State Consistency | 1.1, 1.2 |
 * | fetchGroupMembers updates state to Empty | - | 1.1 |
 * | fetchGroupMembers updates state to Error | - | 1.1 |
 * 
 * ## Known Limitations
 * 
 * **SDK Initialization Dependency:**
 * The CometChatGroupMembersViewModel has a dependency on `CometChatUIKit.getLoggedInUser()`
 * during initialization (line 99 of CometChatGroupMembersViewModel.kt). This requires the
 * CometChat SDK to be initialized, which cannot be done in unit tests without Android context.
 * 
 * **Current Status:**
 * - Tests are written correctly and follow established patterns
 * - Tests will pass once SDK initialization is mocked or made optional
 * - This is a known infrastructure issue, not a test logic issue
 * 
 * **Potential Solutions:**
 * 1. Add MockK dependency to mock CometChatUIKit static methods
 * 2. Make loggedInUser lazy-initialized or nullable in ViewModel
 * 3. Add Robolectric to chatuikit-core module for Android framework mocking
 * 4. Inject loggedInUser as a constructor parameter instead of fetching it
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CometChatGroupMembersViewModelTest : FunSpec({

    val testDispatcher = StandardTestDispatcher()

    beforeSpec {
        Dispatchers.setMain(testDispatcher)
    }

    afterSpec {
        Dispatchers.resetMain()
    }

    /**
     * Mock use cases for testing ViewModel without SDK dependencies.
     */
    class MockFetchGroupMembersUseCase : FetchGroupMembersUseCase(
        repository = object : com.cometchat.uikit.core.domain.repository.GroupMembersRepository {
            override suspend fun fetchGroupMembers(
                guid: String,
                limit: Int,
                searchKeyword: String?
            ): Result<List<GroupMember>> = Result.success(emptyList())
            
            override suspend fun kickMember(guid: String, uid: String): Result<Unit> = 
                Result.success(Unit)
            
            override suspend fun banMember(guid: String, uid: String): Result<Unit> = 
                Result.success(Unit)
            
            override suspend fun changeMemberScope(
                guid: String,
                uid: String,
                scope: String
            ): Result<Unit> = Result.success(Unit)
            
            override fun hasMore(): Boolean = true
            override fun resetRequest() {}
        }
    ) {
        var result: Result<List<GroupMember>> = Result.success(emptyList())
        var resetRequestCallCount: Int = 0
        
        override suspend fun invoke(
            guid: String,
            limit: Int,
            searchKeyword: String?
        ): Result<List<GroupMember>> = result
        
        override fun resetRequest() {
            resetRequestCallCount++
        }
    }

    class MockKickGroupMemberUseCase : KickGroupMemberUseCase(
        repository = object : com.cometchat.uikit.core.domain.repository.GroupMembersRepository {
            override suspend fun fetchGroupMembers(
                guid: String,
                limit: Int,
                searchKeyword: String?
            ): Result<List<GroupMember>> = Result.success(emptyList())
            
            override suspend fun kickMember(guid: String, uid: String): Result<Unit> = 
                Result.success(Unit)
            
            override suspend fun banMember(guid: String, uid: String): Result<Unit> = 
                Result.success(Unit)
            
            override suspend fun changeMemberScope(
                guid: String,
                uid: String,
                scope: String
            ): Result<Unit> = Result.success(Unit)
            
            override fun hasMore(): Boolean = true
            override fun resetRequest() {}
        }
    ) {
        var result: Result<Unit> = Result.success(Unit)
        
        override suspend fun invoke(guid: String, uid: String): Result<Unit> = result
    }

    class MockBanGroupMemberUseCase : BanGroupMemberUseCase(
        repository = object : com.cometchat.uikit.core.domain.repository.GroupMembersRepository {
            override suspend fun fetchGroupMembers(
                guid: String,
                limit: Int,
                searchKeyword: String?
            ): Result<List<GroupMember>> = Result.success(emptyList())
            
            override suspend fun kickMember(guid: String, uid: String): Result<Unit> = 
                Result.success(Unit)
            
            override suspend fun banMember(guid: String, uid: String): Result<Unit> = 
                Result.success(Unit)
            
            override suspend fun changeMemberScope(
                guid: String,
                uid: String,
                scope: String
            ): Result<Unit> = Result.success(Unit)
            
            override fun hasMore(): Boolean = true
            override fun resetRequest() {}
        }
    ) {
        var result: Result<Unit> = Result.success(Unit)
        
        override suspend fun invoke(guid: String, uid: String): Result<Unit> = result
    }

    class MockChangeMemberScopeUseCase : ChangeMemberScopeUseCase(
        repository = object : com.cometchat.uikit.core.domain.repository.GroupMembersRepository {
            override suspend fun fetchGroupMembers(
                guid: String,
                limit: Int,
                searchKeyword: String?
            ): Result<List<GroupMember>> = Result.success(emptyList())
            
            override suspend fun kickMember(guid: String, uid: String): Result<Unit> = 
                Result.success(Unit)
            
            override suspend fun banMember(guid: String, uid: String): Result<Unit> = 
                Result.success(Unit)
            
            override suspend fun changeMemberScope(
                guid: String,
                uid: String,
                scope: String
            ): Result<Unit> = Result.success(Unit)
            
            override fun hasMore(): Boolean = true
            override fun resetRequest() {}
        }
    ) {
        var result: Result<Unit> = Result.success(Unit)
        
        override suspend fun invoke(
            guid: String,
            uid: String,
            scope: String
        ): Result<Unit> = result
    }

    /**
     * Helper function to create mock GroupMember instances for testing.
     */
    fun createMockMembers(count: Int, startId: Int = 1): List<GroupMember> {
        return (startId until startId + count).map { id ->
            GroupMember("user$id", "participant").apply {
                name = "User $id"
            }
        }
    }

    /**
     * Helper function to create a mock Group instance.
     */
    fun createMockGroup(guid: String = "test-group"): Group {
        return Group().apply {
            this.guid = guid
            this.name = "Test Group"
            this.membersCount = 10
        }
    }

    // ========================================
    // Task 1.7.4.1: Test initial state is Loading
    // ========================================
    
    context("Task 1.7.4.1: Initial state tests") {
        
        /**
         * **Property 1: Initial State Correctness**
         * 
         * The ViewModel should always start in Loading state before any data is fetched.
         * This ensures the UI can show appropriate loading indicators.
         * 
         * **Validates: Requirements 1.1**
         */
        test("initial uiState should be Loading") {
            val fetchUseCase = MockFetchGroupMembersUseCase()
            val kickUseCase = MockKickGroupMemberUseCase()
            val banUseCase = MockBanGroupMemberUseCase()
            val changeScopeUseCase = MockChangeMemberScopeUseCase()
            
            val viewModel = CometChatGroupMembersViewModel(
                fetchGroupMembersUseCase = fetchUseCase,
                kickGroupMemberUseCase = kickUseCase,
                banGroupMemberUseCase = banUseCase,
                changeMemberScopeUseCase = changeScopeUseCase,
                enableListeners = false
            )
            
            viewModel.uiState.value shouldBe GroupMembersUIState.Loading
        }
        
        test("initial members list should be empty") {
            val fetchUseCase = MockFetchGroupMembersUseCase()
            val kickUseCase = MockKickGroupMemberUseCase()
            val banUseCase = MockBanGroupMemberUseCase()
            val changeScopeUseCase = MockChangeMemberScopeUseCase()
            
            val viewModel = CometChatGroupMembersViewModel(
                fetchGroupMembersUseCase = fetchUseCase,
                kickGroupMemberUseCase = kickUseCase,
                banGroupMemberUseCase = banUseCase,
                changeMemberScopeUseCase = changeScopeUseCase,
                enableListeners = false
            )
            
            viewModel.members.value shouldBe emptyList()
        }
        
        test("initial hasMore should be true") {
            val fetchUseCase = MockFetchGroupMembersUseCase()
            val kickUseCase = MockKickGroupMemberUseCase()
            val banUseCase = MockBanGroupMemberUseCase()
            val changeScopeUseCase = MockChangeMemberScopeUseCase()
            
            val viewModel = CometChatGroupMembersViewModel(
                fetchGroupMembersUseCase = fetchUseCase,
                kickGroupMemberUseCase = kickUseCase,
                banGroupMemberUseCase = banUseCase,
                changeMemberScopeUseCase = changeScopeUseCase,
                enableListeners = false
            )
            
            viewModel.hasMore.value shouldBe true
        }
        
        test("initial isLoading should be false") {
            val fetchUseCase = MockFetchGroupMembersUseCase()
            val kickUseCase = MockKickGroupMemberUseCase()
            val banUseCase = MockBanGroupMemberUseCase()
            val changeScopeUseCase = MockChangeMemberScopeUseCase()
            
            val viewModel = CometChatGroupMembersViewModel(
                fetchGroupMembersUseCase = fetchUseCase,
                kickGroupMemberUseCase = kickUseCase,
                banGroupMemberUseCase = banUseCase,
                changeMemberScopeUseCase = changeScopeUseCase,
                enableListeners = false
            )
            
            viewModel.isLoading.value shouldBe false
        }
        
        test("initial selectedMembers should be empty") {
            val fetchUseCase = MockFetchGroupMembersUseCase()
            val kickUseCase = MockKickGroupMemberUseCase()
            val banUseCase = MockBanGroupMemberUseCase()
            val changeScopeUseCase = MockChangeMemberScopeUseCase()
            
            val viewModel = CometChatGroupMembersViewModel(
                fetchGroupMembersUseCase = fetchUseCase,
                kickGroupMemberUseCase = kickUseCase,
                banGroupMemberUseCase = banUseCase,
                changeMemberScopeUseCase = changeScopeUseCase,
                enableListeners = false
            )
            
            viewModel.selectedMembers.value shouldBe emptyMap()
        }
        
        /**
         * Property-based test: Initial state should always be Loading regardless of
         * any configuration parameters that might be set later.
         * 
         * **Property 1: Initial State Correctness**
         * **Validates: Requirements 1.1**
         */
        test("Property 1: Initial state should always be Loading for any new ViewModel instance") {
            checkAll(10, Arb.int(1, 100)) { _ ->
                val fetchUseCase = MockFetchGroupMembersUseCase()
                val kickUseCase = MockKickGroupMemberUseCase()
                val banUseCase = MockBanGroupMemberUseCase()
                val changeScopeUseCase = MockChangeMemberScopeUseCase()
                
                val viewModel = CometChatGroupMembersViewModel(
                    fetchGroupMembersUseCase = fetchUseCase,
                    kickGroupMemberUseCase = kickUseCase,
                    banGroupMemberUseCase = banUseCase,
                    changeMemberScopeUseCase = changeScopeUseCase,
                    enableListeners = false
                )
                
                viewModel.uiState.value shouldBe GroupMembersUIState.Loading
                viewModel.members.value shouldBe emptyList()
                viewModel.hasMore.value shouldBe true
                viewModel.isLoading.value shouldBe false
            }
        }
    }
    
    // ========================================
    // Task 1.7.4.2: Test fetch members updates state correctly
    // ========================================
    
    context("Task 1.7.4.2: Fetch state update tests") {
        
        /**
         * **Property 2: Fetch State Consistency**
         * 
         * When fetchGroupMembers() is called and members are returned,
         * the ViewModel should update uiState to Content and populate the members list.
         * 
         * **Validates: Requirements 1.1, 1.2**
         */
        test("fetchGroupMembers should update state to Content when members are returned") {
            runTest {
                val fetchUseCase = MockFetchGroupMembersUseCase()
                val kickUseCase = MockKickGroupMemberUseCase()
                val banUseCase = MockBanGroupMemberUseCase()
                val changeScopeUseCase = MockChangeMemberScopeUseCase()
                
                // Setup: Return 3 members
                val mockMembers = createMockMembers(3)
                fetchUseCase.result = Result.success(mockMembers)
                
                val viewModel = CometChatGroupMembersViewModel(
                    fetchGroupMembersUseCase = fetchUseCase,
                    kickGroupMemberUseCase = kickUseCase,
                    banGroupMemberUseCase = banUseCase,
                    changeMemberScopeUseCase = changeScopeUseCase,
                    enableListeners = false
                )
                
                // Set group before fetching
                val group = createMockGroup()
                viewModel.setGroup(group)
                
                // Initial state should be Loading
                viewModel.uiState.value shouldBe GroupMembersUIState.Loading
                
                // Fetch members
                viewModel.fetchGroupMembers()
                advanceUntilIdle()
                
                // Verify state updated to Content
                viewModel.uiState.value.shouldBeInstanceOf<GroupMembersUIState.Content>()
                val contentState = viewModel.uiState.value as GroupMembersUIState.Content
                contentState.members.size shouldBe 3
                
                // Verify members list updated
                viewModel.members.value.size shouldBe 3
                viewModel.members.value shouldBe mockMembers
                
                // Verify loading flag cleared
                viewModel.isLoading.value shouldBe false
            }
        }
        
        /**
         * When fetchGroupMembers() is called and no members are returned,
         * the ViewModel should update uiState to Empty.
         * 
         * **Validates: Requirements 1.1**
         */
        test("fetchGroupMembers should update state to Empty when no members are returned") {
            runTest {
                val fetchUseCase = MockFetchGroupMembersUseCase()
                val kickUseCase = MockKickGroupMemberUseCase()
                val banUseCase = MockBanGroupMemberUseCase()
                val changeScopeUseCase = MockChangeMemberScopeUseCase()
                
                // Setup: Return empty list
                fetchUseCase.result = Result.success(emptyList())
                
                val viewModel = CometChatGroupMembersViewModel(
                    fetchGroupMembersUseCase = fetchUseCase,
                    kickGroupMemberUseCase = kickUseCase,
                    banGroupMemberUseCase = banUseCase,
                    changeMemberScopeUseCase = changeScopeUseCase,
                    enableListeners = false
                )
                
                // Set group before fetching
                val group = createMockGroup()
                viewModel.setGroup(group)
                
                // Initial state should be Loading
                viewModel.uiState.value shouldBe GroupMembersUIState.Loading
                
                // Fetch members
                viewModel.fetchGroupMembers()
                advanceUntilIdle()
                
                // Verify state updated to Empty
                viewModel.uiState.value shouldBe GroupMembersUIState.Empty
                
                // Verify members list is empty
                viewModel.members.value shouldBe emptyList()
                
                // Verify loading flag cleared
                viewModel.isLoading.value shouldBe false
                
                // Verify hasMore is false (no more data to fetch)
                viewModel.hasMore.value shouldBe false
            }
        }
        
        /**
         * When fetchGroupMembers() is called and the fetch fails,
         * the ViewModel should update uiState to Error with the exception.
         * 
         * **Validates: Requirements 1.1**
         */
        test("fetchGroupMembers should update state to Error when fetch fails") {
            runTest {
                val fetchUseCase = MockFetchGroupMembersUseCase()
                val kickUseCase = MockKickGroupMemberUseCase()
                val banUseCase = MockBanGroupMemberUseCase()
                val changeScopeUseCase = MockChangeMemberScopeUseCase()
                
                // Setup: Return failure
                val exception = CometChatException("ERR_NETWORK", "Network error")
                fetchUseCase.result = Result.failure(exception)
                
                val viewModel = CometChatGroupMembersViewModel(
                    fetchGroupMembersUseCase = fetchUseCase,
                    kickGroupMemberUseCase = kickUseCase,
                    banGroupMemberUseCase = banUseCase,
                    changeMemberScopeUseCase = changeScopeUseCase,
                    enableListeners = false
                )
                
                // Set group before fetching
                val group = createMockGroup()
                viewModel.setGroup(group)
                
                // Initial state should be Loading
                viewModel.uiState.value shouldBe GroupMembersUIState.Loading
                
                // Fetch members
                viewModel.fetchGroupMembers()
                advanceUntilIdle()
                
                // Verify state updated to Error
                viewModel.uiState.value.shouldBeInstanceOf<GroupMembersUIState.Error>()
                val errorState = viewModel.uiState.value as GroupMembersUIState.Error
                errorState.exception.code shouldBe "ERR_NETWORK"
                errorState.exception.message shouldBe "Network error"
                
                // Verify members list is still empty
                viewModel.members.value shouldBe emptyList()
                
                // Verify loading flag cleared
                viewModel.isLoading.value shouldBe false
            }
        }
        
        /**
         * When fetchGroupMembers() is called without setting a group first,
         * the ViewModel should update uiState to Error.
         * 
         * **Validates: Requirements 1.1**
         */
        test("fetchGroupMembers should update state to Error when group is not set") {
            runTest {
                val fetchUseCase = MockFetchGroupMembersUseCase()
                val kickUseCase = MockKickGroupMemberUseCase()
                val banUseCase = MockBanGroupMemberUseCase()
                val changeScopeUseCase = MockChangeMemberScopeUseCase()
                
                val viewModel = CometChatGroupMembersViewModel(
                    fetchGroupMembersUseCase = fetchUseCase,
                    kickGroupMemberUseCase = kickUseCase,
                    banGroupMemberUseCase = banUseCase,
                    changeMemberScopeUseCase = changeScopeUseCase,
                    enableListeners = false
                )
                
                // Don't set group - fetch should fail
                viewModel.fetchGroupMembers()
                advanceUntilIdle()
                
                // Verify state updated to Error
                viewModel.uiState.value.shouldBeInstanceOf<GroupMembersUIState.Error>()
                val errorState = viewModel.uiState.value as GroupMembersUIState.Error
                errorState.exception.code shouldBe "ERR_GROUP_NOT_SET"
            }
        }
        
        /**
         * **Property 2: Fetch State Consistency**
         * 
         * Property-based test: For any number of members returned (0 to 100),
         * the ViewModel should correctly update state to Content or Empty.
         * 
         * **Validates: Requirements 1.1, 1.2**
         */
        test("Property 2: Fetch should consistently update state based on member count") {
            checkAll(20, Arb.int(0, 100)) { memberCount ->
                runTest {
                    val fetchUseCase = MockFetchGroupMembersUseCase()
                    val kickUseCase = MockKickGroupMemberUseCase()
                    val banUseCase = MockBanGroupMemberUseCase()
                    val changeScopeUseCase = MockChangeMemberScopeUseCase()
                    
                    // Setup: Return specified number of members
                    val mockMembers = createMockMembers(memberCount)
                    fetchUseCase.result = Result.success(mockMembers)
                    
                    val viewModel = CometChatGroupMembersViewModel(
                        fetchGroupMembersUseCase = fetchUseCase,
                        kickGroupMemberUseCase = kickUseCase,
                        banGroupMemberUseCase = banUseCase,
                        changeMemberScopeUseCase = changeScopeUseCase,
                        enableListeners = false
                    )
                    
                    // Set group before fetching
                    val group = createMockGroup()
                    viewModel.setGroup(group)
                    
                    // Fetch members
                    viewModel.fetchGroupMembers()
                    advanceUntilIdle()
                    
                    // Verify state based on member count
                    if (memberCount == 0) {
                        viewModel.uiState.value shouldBe GroupMembersUIState.Empty
                        viewModel.hasMore.value shouldBe false
                    } else {
                        viewModel.uiState.value.shouldBeInstanceOf<GroupMembersUIState.Content>()
                        val contentState = viewModel.uiState.value as GroupMembersUIState.Content
                        contentState.members.size shouldBe memberCount
                        viewModel.hasMore.value shouldBe true
                    }
                    
                    // Verify members list matches
                    viewModel.members.value.size shouldBe memberCount
                    viewModel.isLoading.value shouldBe false
                }
            }
        }
    }
    
    // ========================================
    // Task 1.7.4.3: Test pagination works
    // ========================================
    
    context("Task 1.7.4.3: Pagination tests") {
        
        test("fetchGroupMembers should append members on subsequent calls") {
            runTest {
                val fetchUseCase = MockFetchGroupMembersUseCase()
                val kickUseCase = MockKickGroupMemberUseCase()
                val banUseCase = MockBanGroupMemberUseCase()
                val changeScopeUseCase = MockChangeMemberScopeUseCase()
                
                val viewModel = CometChatGroupMembersViewModel(
                    fetchGroupMembersUseCase = fetchUseCase,
                    kickGroupMemberUseCase = kickUseCase,
                    banGroupMemberUseCase = banUseCase,
                    changeMemberScopeUseCase = changeScopeUseCase,
                    enableListeners = false
                )
                
                val group = createMockGroup()
                viewModel.setGroup(group)
                
                // First fetch: 3 members
                fetchUseCase.result = Result.success(createMockMembers(3, 1))
                viewModel.fetchGroupMembers()
                advanceUntilIdle()
                
                viewModel.members.value.size shouldBe 3
                viewModel.hasMore.value shouldBe true
                
                // Second fetch: 3 more members
                fetchUseCase.result = Result.success(createMockMembers(3, 4))
                viewModel.fetchGroupMembers()
                advanceUntilIdle()
                
                // Should have 6 members total
                viewModel.members.value.size shouldBe 6
                viewModel.hasMore.value shouldBe true
            }
        }
        
        test("hasMore should be false when empty list is returned") {
            runTest {
                val fetchUseCase = MockFetchGroupMembersUseCase()
                val kickUseCase = MockKickGroupMemberUseCase()
                val banUseCase = MockBanGroupMemberUseCase()
                val changeScopeUseCase = MockChangeMemberScopeUseCase()
                
                val viewModel = CometChatGroupMembersViewModel(
                    fetchGroupMembersUseCase = fetchUseCase,
                    kickGroupMemberUseCase = kickUseCase,
                    banGroupMemberUseCase = banUseCase,
                    changeMemberScopeUseCase = changeScopeUseCase,
                    enableListeners = false
                )
                
                val group = createMockGroup()
                viewModel.setGroup(group)
                
                // First fetch: 3 members
                fetchUseCase.result = Result.success(createMockMembers(3))
                viewModel.fetchGroupMembers()
                advanceUntilIdle()
                
                viewModel.hasMore.value shouldBe true
                
                // Second fetch: empty list (no more data)
                fetchUseCase.result = Result.success(emptyList())
                viewModel.fetchGroupMembers()
                advanceUntilIdle()
                
                // hasMore should now be false
                viewModel.hasMore.value shouldBe false
                viewModel.members.value.size shouldBe 3 // Still has previous members
            }
        }
        
        test("fetchGroupMembers should not fetch when hasMore is false") {
            runTest {
                val fetchUseCase = MockFetchGroupMembersUseCase()
                val kickUseCase = MockKickGroupMemberUseCase()
                val banUseCase = MockBanGroupMemberUseCase()
                val changeScopeUseCase = MockChangeMemberScopeUseCase()
                
                val viewModel = CometChatGroupMembersViewModel(
                    fetchGroupMembersUseCase = fetchUseCase,
                    kickGroupMemberUseCase = kickUseCase,
                    banGroupMemberUseCase = banUseCase,
                    changeMemberScopeUseCase = changeScopeUseCase,
                    enableListeners = false
                )
                
                val group = createMockGroup()
                viewModel.setGroup(group)
                
                // First fetch: empty list
                fetchUseCase.result = Result.success(emptyList())
                viewModel.fetchGroupMembers()
                advanceUntilIdle()
                
                viewModel.hasMore.value shouldBe false
                
                // Try to fetch again - should not execute because hasMore is false
                fetchUseCase.result = Result.success(createMockMembers(3))
                viewModel.fetchGroupMembers()
                advanceUntilIdle()
                
                // Should still be empty (fetch was skipped since hasMore was false)
                viewModel.members.value shouldBe emptyList()
            }
        }
    }
    
    // ========================================
    // Task 1.7.4.4: Test search filtering
    // ========================================
    
    context("Task 1.7.4.4: Search filtering tests") {
        
        test("searchGroupMembers should clear list and fetch with search keyword") {
            runTest {
                val fetchUseCase = MockFetchGroupMembersUseCase()
                val kickUseCase = MockKickGroupMemberUseCase()
                val banUseCase = MockBanGroupMemberUseCase()
                val changeScopeUseCase = MockChangeMemberScopeUseCase()
                
                val viewModel = CometChatGroupMembersViewModel(
                    fetchGroupMembersUseCase = fetchUseCase,
                    kickGroupMemberUseCase = kickUseCase,
                    banGroupMemberUseCase = banUseCase,
                    changeMemberScopeUseCase = changeScopeUseCase,
                    enableListeners = false
                )
                
                val group = createMockGroup()
                viewModel.setGroup(group)
                
                // Initial fetch: 5 members
                fetchUseCase.result = Result.success(createMockMembers(5))
                viewModel.fetchGroupMembers()
                advanceUntilIdle()
                
                viewModel.members.value.size shouldBe 5
                
                // Search: should clear list and fetch with keyword
                fetchUseCase.result = Result.success(createMockMembers(2))
                viewModel.searchGroupMembers("test")
                advanceUntilIdle()
                
                // Should have only 2 members (search results)
                viewModel.members.value.size shouldBe 2
            }
        }
        
        test("searchGroupMembers with null should clear search and fetch all") {
            runTest {
                val fetchUseCase = MockFetchGroupMembersUseCase()
                val kickUseCase = MockKickGroupMemberUseCase()
                val banUseCase = MockBanGroupMemberUseCase()
                val changeScopeUseCase = MockChangeMemberScopeUseCase()
                
                val viewModel = CometChatGroupMembersViewModel(
                    fetchGroupMembersUseCase = fetchUseCase,
                    kickGroupMemberUseCase = kickUseCase,
                    banGroupMemberUseCase = banUseCase,
                    changeMemberScopeUseCase = changeScopeUseCase,
                    enableListeners = false
                )
                
                val group = createMockGroup()
                viewModel.setGroup(group)
                
                // Search with keyword
                fetchUseCase.result = Result.success(createMockMembers(2))
                viewModel.searchGroupMembers("test")
                advanceUntilIdle()
                
                viewModel.members.value.size shouldBe 2
                
                // Clear search
                fetchUseCase.result = Result.success(createMockMembers(5))
                viewModel.searchGroupMembers(null)
                advanceUntilIdle()
                
                // Should have all members
                viewModel.members.value.size shouldBe 5
            }
        }
        
        test("searchGroupMembers should reset hasMore flag") {
            runTest {
                val fetchUseCase = MockFetchGroupMembersUseCase()
                val kickUseCase = MockKickGroupMemberUseCase()
                val banUseCase = MockBanGroupMemberUseCase()
                val changeScopeUseCase = MockChangeMemberScopeUseCase()
                
                val viewModel = CometChatGroupMembersViewModel(
                    fetchGroupMembersUseCase = fetchUseCase,
                    kickGroupMemberUseCase = kickUseCase,
                    banGroupMemberUseCase = banUseCase,
                    changeMemberScopeUseCase = changeScopeUseCase,
                    enableListeners = false
                )
                
                val group = createMockGroup()
                viewModel.setGroup(group)
                
                // Initial fetch with no more data
                fetchUseCase.result = Result.success(emptyList())
                viewModel.fetchGroupMembers()
                advanceUntilIdle()
                
                viewModel.hasMore.value shouldBe false
                
                // Search should reset hasMore
                fetchUseCase.result = Result.success(createMockMembers(2))
                viewModel.searchGroupMembers("test")
                advanceUntilIdle()
                
                // hasMore should be true again
                viewModel.hasMore.value shouldBe true
            }
        }
    }
    
    // ========================================
    // Task 1.7.4.5: Test selection modes
    // ========================================
    
    context("Task 1.7.4.5: Selection mode tests") {
        
        test("selectMember should add member to selectedMembers") {
            val fetchUseCase = MockFetchGroupMembersUseCase()
            val kickUseCase = MockKickGroupMemberUseCase()
            val banUseCase = MockBanGroupMemberUseCase()
            val changeScopeUseCase = MockChangeMemberScopeUseCase()
            
            val viewModel = CometChatGroupMembersViewModel(
                fetchGroupMembersUseCase = fetchUseCase,
                kickGroupMemberUseCase = kickUseCase,
                banGroupMemberUseCase = banUseCase,
                changeMemberScopeUseCase = changeScopeUseCase,
                enableListeners = false
            )
            
            val member = createMockMembers(1).first()
            
            viewModel.selectedMembers.value shouldBe emptyMap()
            
            viewModel.selectMember(member)
            
            viewModel.selectedMembers.value.size shouldBe 1
            viewModel.selectedMembers.value[member.uid] shouldBe member
            viewModel.isSelected(member) shouldBe true
        }
        
        test("deselectMember should remove member from selectedMembers") {
            val fetchUseCase = MockFetchGroupMembersUseCase()
            val kickUseCase = MockKickGroupMemberUseCase()
            val banUseCase = MockBanGroupMemberUseCase()
            val changeScopeUseCase = MockChangeMemberScopeUseCase()
            
            val viewModel = CometChatGroupMembersViewModel(
                fetchGroupMembersUseCase = fetchUseCase,
                kickGroupMemberUseCase = kickUseCase,
                banGroupMemberUseCase = banUseCase,
                changeMemberScopeUseCase = changeScopeUseCase,
                enableListeners = false
            )
            
            val member = createMockMembers(1).first()
            
            // Select member first
            viewModel.selectMember(member)
            viewModel.selectedMembers.value.size shouldBe 1
            
            // Deselect member
            viewModel.deselectMember(member)
            
            viewModel.selectedMembers.value shouldBe emptyMap()
            viewModel.isSelected(member) shouldBe false
        }
        
        test("clearSelection should remove all selected members") {
            val fetchUseCase = MockFetchGroupMembersUseCase()
            val kickUseCase = MockKickGroupMemberUseCase()
            val banUseCase = MockBanGroupMemberUseCase()
            val changeScopeUseCase = MockChangeMemberScopeUseCase()
            
            val viewModel = CometChatGroupMembersViewModel(
                fetchGroupMembersUseCase = fetchUseCase,
                kickGroupMemberUseCase = kickUseCase,
                banGroupMemberUseCase = banUseCase,
                changeMemberScopeUseCase = changeScopeUseCase,
                enableListeners = false
            )
            
            val members = createMockMembers(3)
            
            // Select multiple members
            members.forEach { viewModel.selectMember(it) }
            viewModel.selectedMembers.value.size shouldBe 3
            
            // Clear selection
            viewModel.clearSelection()
            
            viewModel.selectedMembers.value shouldBe emptyMap()
            members.forEach { member ->
                viewModel.isSelected(member) shouldBe false
            }
        }
        
        test("multiple members can be selected") {
            val fetchUseCase = MockFetchGroupMembersUseCase()
            val kickUseCase = MockKickGroupMemberUseCase()
            val banUseCase = MockBanGroupMemberUseCase()
            val changeScopeUseCase = MockChangeMemberScopeUseCase()
            
            val viewModel = CometChatGroupMembersViewModel(
                fetchGroupMembersUseCase = fetchUseCase,
                kickGroupMemberUseCase = kickUseCase,
                banGroupMemberUseCase = banUseCase,
                changeMemberScopeUseCase = changeScopeUseCase,
                enableListeners = false
            )
            
            val members = createMockMembers(5)
            
            // Select members one by one
            members.take(3).forEach { member ->
                viewModel.selectMember(member)
            }
            
            viewModel.selectedMembers.value.size shouldBe 3
            members.take(3).forEach { member ->
                viewModel.isSelected(member) shouldBe true
            }
            members.drop(3).forEach { member ->
                viewModel.isSelected(member) shouldBe false
            }
        }
        
        test("isSelected should return false for unselected member") {
            val fetchUseCase = MockFetchGroupMembersUseCase()
            val kickUseCase = MockKickGroupMemberUseCase()
            val banUseCase = MockBanGroupMemberUseCase()
            val changeScopeUseCase = MockChangeMemberScopeUseCase()
            
            val viewModel = CometChatGroupMembersViewModel(
                fetchGroupMembersUseCase = fetchUseCase,
                kickGroupMemberUseCase = kickUseCase,
                banGroupMemberUseCase = banUseCase,
                changeMemberScopeUseCase = changeScopeUseCase,
                enableListeners = false
            )
            
            val member = createMockMembers(1).first()
            
            viewModel.isSelected(member) shouldBe false
        }
    }
    
    // ========================================
    // Task 1.7.4.6: Test kick member operation
    // ========================================
    
    context("Task 1.7.4.6: Kick member operation tests") {
        
        test("kickMember should remove member from list on success") {
            runTest {
                val fetchUseCase = MockFetchGroupMembersUseCase()
                val kickUseCase = MockKickGroupMemberUseCase()
                val banUseCase = MockBanGroupMemberUseCase()
                val changeScopeUseCase = MockChangeMemberScopeUseCase()
                
                val viewModel = CometChatGroupMembersViewModel(
                    fetchGroupMembersUseCase = fetchUseCase,
                    kickGroupMemberUseCase = kickUseCase,
                    banGroupMemberUseCase = banUseCase,
                    changeMemberScopeUseCase = changeScopeUseCase,
                    enableListeners = false
                )
                
                val group = createMockGroup()
                viewModel.setGroup(group)
                
                // Fetch members
                val members = createMockMembers(3)
                fetchUseCase.result = Result.success(members)
                viewModel.fetchGroupMembers()
                advanceUntilIdle()
                
                viewModel.members.value.size shouldBe 3
                
                // Kick member
                kickUseCase.result = Result.success(Unit)
                val memberToKick = members.first()
                viewModel.kickMember(memberToKick)
                advanceUntilIdle()
                
                // Member should be removed
                viewModel.members.value.size shouldBe 2
                viewModel.members.value.none { it.uid == memberToKick.uid } shouldBe true
            }
        }
        
        test("kickMember should update state to Error on failure") {
            runTest {
                val fetchUseCase = MockFetchGroupMembersUseCase()
                val kickUseCase = MockKickGroupMemberUseCase()
                val banUseCase = MockBanGroupMemberUseCase()
                val changeScopeUseCase = MockChangeMemberScopeUseCase()
                
                val viewModel = CometChatGroupMembersViewModel(
                    fetchGroupMembersUseCase = fetchUseCase,
                    kickGroupMemberUseCase = kickUseCase,
                    banGroupMemberUseCase = banUseCase,
                    changeMemberScopeUseCase = changeScopeUseCase,
                    enableListeners = false
                )
                
                val group = createMockGroup()
                viewModel.setGroup(group)
                
                // Fetch members
                val members = createMockMembers(3)
                fetchUseCase.result = Result.success(members)
                viewModel.fetchGroupMembers()
                advanceUntilIdle()
                
                // Kick member with error
                val exception = CometChatException("ERR_KICK_FAILED", "Permission denied")
                kickUseCase.result = Result.failure(exception)
                val memberToKick = members.first()
                viewModel.kickMember(memberToKick)
                advanceUntilIdle()
                
                // State should be Error
                viewModel.uiState.value.shouldBeInstanceOf<GroupMembersUIState.Error>()
                val errorState = viewModel.uiState.value as GroupMembersUIState.Error
                errorState.exception.code shouldBe "ERR_KICK_FAILED"
                
                // Member should still be in list
                viewModel.members.value.size shouldBe 3
            }
        }
        
        test("kickMember should update state to Empty when last member is kicked") {
            runTest {
                val fetchUseCase = MockFetchGroupMembersUseCase()
                val kickUseCase = MockKickGroupMemberUseCase()
                val banUseCase = MockBanGroupMemberUseCase()
                val changeScopeUseCase = MockChangeMemberScopeUseCase()
                
                val viewModel = CometChatGroupMembersViewModel(
                    fetchGroupMembersUseCase = fetchUseCase,
                    kickGroupMemberUseCase = kickUseCase,
                    banGroupMemberUseCase = banUseCase,
                    changeMemberScopeUseCase = changeScopeUseCase,
                    enableListeners = false
                )
                
                val group = createMockGroup()
                viewModel.setGroup(group)
                
                // Fetch single member
                val members = createMockMembers(1)
                fetchUseCase.result = Result.success(members)
                viewModel.fetchGroupMembers()
                advanceUntilIdle()
                
                // Kick the only member
                kickUseCase.result = Result.success(Unit)
                viewModel.kickMember(members.first())
                advanceUntilIdle()
                
                // State should be Empty
                viewModel.uiState.value shouldBe GroupMembersUIState.Empty
                viewModel.members.value shouldBe emptyList()
            }
        }
    }
    
    // ========================================
    // Task 1.7.4.7: Test ban member operation
    // ========================================
    
    context("Task 1.7.4.7: Ban member operation tests") {
        
        test("banMember should remove member from list on success") {
            runTest {
                val fetchUseCase = MockFetchGroupMembersUseCase()
                val kickUseCase = MockKickGroupMemberUseCase()
                val banUseCase = MockBanGroupMemberUseCase()
                val changeScopeUseCase = MockChangeMemberScopeUseCase()
                
                val viewModel = CometChatGroupMembersViewModel(
                    fetchGroupMembersUseCase = fetchUseCase,
                    kickGroupMemberUseCase = kickUseCase,
                    banGroupMemberUseCase = banUseCase,
                    changeMemberScopeUseCase = changeScopeUseCase,
                    enableListeners = false
                )
                
                val group = createMockGroup()
                viewModel.setGroup(group)
                
                // Fetch members
                val members = createMockMembers(3)
                fetchUseCase.result = Result.success(members)
                viewModel.fetchGroupMembers()
                advanceUntilIdle()
                
                viewModel.members.value.size shouldBe 3
                
                // Ban member
                banUseCase.result = Result.success(Unit)
                val memberToBan = members.first()
                viewModel.banMember(memberToBan)
                advanceUntilIdle()
                
                // Member should be removed
                viewModel.members.value.size shouldBe 2
                viewModel.members.value.none { it.uid == memberToBan.uid } shouldBe true
            }
        }
        
        test("banMember should update state to Error on failure") {
            runTest {
                val fetchUseCase = MockFetchGroupMembersUseCase()
                val kickUseCase = MockKickGroupMemberUseCase()
                val banUseCase = MockBanGroupMemberUseCase()
                val changeScopeUseCase = MockChangeMemberScopeUseCase()
                
                val viewModel = CometChatGroupMembersViewModel(
                    fetchGroupMembersUseCase = fetchUseCase,
                    kickGroupMemberUseCase = kickUseCase,
                    banGroupMemberUseCase = banUseCase,
                    changeMemberScopeUseCase = changeScopeUseCase,
                    enableListeners = false
                )
                
                val group = createMockGroup()
                viewModel.setGroup(group)
                
                // Fetch members
                val members = createMockMembers(3)
                fetchUseCase.result = Result.success(members)
                viewModel.fetchGroupMembers()
                advanceUntilIdle()
                
                // Ban member with error
                val exception = CometChatException("ERR_BAN_FAILED", "Permission denied")
                banUseCase.result = Result.failure(exception)
                val memberToBan = members.first()
                viewModel.banMember(memberToBan)
                advanceUntilIdle()
                
                // State should be Error
                viewModel.uiState.value.shouldBeInstanceOf<GroupMembersUIState.Error>()
                val errorState = viewModel.uiState.value as GroupMembersUIState.Error
                errorState.exception.code shouldBe "ERR_BAN_FAILED"
                
                // Member should still be in list
                viewModel.members.value.size shouldBe 3
            }
        }
    }
    
    // ========================================
    // Task 1.7.4.8: Test change scope operation
    // ========================================
    
    context("Task 1.7.4.8: Change scope operation tests") {
        
        test("changeMemberScope should update member scope on success") {
            runTest {
                val fetchUseCase = MockFetchGroupMembersUseCase()
                val kickUseCase = MockKickGroupMemberUseCase()
                val banUseCase = MockBanGroupMemberUseCase()
                val changeScopeUseCase = MockChangeMemberScopeUseCase()
                
                val viewModel = CometChatGroupMembersViewModel(
                    fetchGroupMembersUseCase = fetchUseCase,
                    kickGroupMemberUseCase = kickUseCase,
                    banGroupMemberUseCase = banUseCase,
                    changeMemberScopeUseCase = changeScopeUseCase,
                    enableListeners = false
                )
                
                val group = createMockGroup()
                viewModel.setGroup(group)
                
                // Fetch members
                val members = createMockMembers(3)
                fetchUseCase.result = Result.success(members)
                viewModel.fetchGroupMembers()
                advanceUntilIdle()
                
                val memberToUpdate = members.first()
                val oldScope = memberToUpdate.scope
                val newScope = "admin"
                
                // Change scope
                changeScopeUseCase.result = Result.success(Unit)
                viewModel.changeMemberScope(memberToUpdate, newScope)
                advanceUntilIdle()
                
                // Member scope should be updated
                val updatedMember = viewModel.members.value.find { it.uid == memberToUpdate.uid }
                updatedMember?.scope shouldBe newScope
            }
        }
        
        test("changeMemberScope should update state to Error on failure") {
            runTest {
                val fetchUseCase = MockFetchGroupMembersUseCase()
                val kickUseCase = MockKickGroupMemberUseCase()
                val banUseCase = MockBanGroupMemberUseCase()
                val changeScopeUseCase = MockChangeMemberScopeUseCase()
                
                val viewModel = CometChatGroupMembersViewModel(
                    fetchGroupMembersUseCase = fetchUseCase,
                    kickGroupMemberUseCase = kickUseCase,
                    banGroupMemberUseCase = banUseCase,
                    changeMemberScopeUseCase = changeScopeUseCase,
                    enableListeners = false
                )
                
                val group = createMockGroup()
                viewModel.setGroup(group)
                
                // Fetch members
                val members = createMockMembers(3)
                fetchUseCase.result = Result.success(members)
                viewModel.fetchGroupMembers()
                advanceUntilIdle()
                
                // Change scope with error
                val exception = CometChatException("ERR_SCOPE_CHANGE_FAILED", "Permission denied")
                changeScopeUseCase.result = Result.failure(exception)
                val memberToUpdate = members.first()
                val oldScope = memberToUpdate.scope
                viewModel.changeMemberScope(memberToUpdate, "admin")
                advanceUntilIdle()
                
                // State should be Error
                viewModel.uiState.value.shouldBeInstanceOf<GroupMembersUIState.Error>()
                val errorState = viewModel.uiState.value as GroupMembersUIState.Error
                errorState.exception.code shouldBe "ERR_SCOPE_CHANGE_FAILED"
                
                // Member scope should not change
                val unchangedMember = viewModel.members.value.find { it.uid == memberToUpdate.uid }
                unchangedMember?.scope shouldBe oldScope
            }
        }
    }
    
    // ========================================
    // Task 1.7.4.9: Test permission checks (dialog management)
    // ========================================
    
    context("Task 1.7.4.9: Permission check and dialog management tests") {
        
        test("showKickConfirmation should update dialogState to ConfirmKick") {
            val fetchUseCase = MockFetchGroupMembersUseCase()
            val kickUseCase = MockKickGroupMemberUseCase()
            val banUseCase = MockBanGroupMemberUseCase()
            val changeScopeUseCase = MockChangeMemberScopeUseCase()
            
            val viewModel = CometChatGroupMembersViewModel(
                fetchGroupMembersUseCase = fetchUseCase,
                kickGroupMemberUseCase = kickUseCase,
                banGroupMemberUseCase = banUseCase,
                changeMemberScopeUseCase = changeScopeUseCase,
                enableListeners = false
            )
            
            val member = createMockMembers(1).first()
            
            viewModel.dialogState.value shouldBe com.cometchat.uikit.core.state.DialogState.Hidden
            
            viewModel.showKickConfirmation(member)
            
            viewModel.dialogState.value.shouldBeInstanceOf<com.cometchat.uikit.core.state.DialogState.ConfirmKick>()
            val dialogState = viewModel.dialogState.value as com.cometchat.uikit.core.state.DialogState.ConfirmKick
            dialogState.member shouldBe member
        }
        
        test("showBanConfirmation should update dialogState to ConfirmBan") {
            val fetchUseCase = MockFetchGroupMembersUseCase()
            val kickUseCase = MockKickGroupMemberUseCase()
            val banUseCase = MockBanGroupMemberUseCase()
            val changeScopeUseCase = MockChangeMemberScopeUseCase()
            
            val viewModel = CometChatGroupMembersViewModel(
                fetchGroupMembersUseCase = fetchUseCase,
                kickGroupMemberUseCase = kickUseCase,
                banGroupMemberUseCase = banUseCase,
                changeMemberScopeUseCase = changeScopeUseCase,
                enableListeners = false
            )
            
            val member = createMockMembers(1).first()
            
            viewModel.dialogState.value shouldBe com.cometchat.uikit.core.state.DialogState.Hidden
            
            viewModel.showBanConfirmation(member)
            
            viewModel.dialogState.value.shouldBeInstanceOf<com.cometchat.uikit.core.state.DialogState.ConfirmBan>()
            val dialogState = viewModel.dialogState.value as com.cometchat.uikit.core.state.DialogState.ConfirmBan
            dialogState.member shouldBe member
        }
        
        test("showScopeSelection should update dialogState to SelectScope") {
            val fetchUseCase = MockFetchGroupMembersUseCase()
            val kickUseCase = MockKickGroupMemberUseCase()
            val banUseCase = MockBanGroupMemberUseCase()
            val changeScopeUseCase = MockChangeMemberScopeUseCase()
            
            val viewModel = CometChatGroupMembersViewModel(
                fetchGroupMembersUseCase = fetchUseCase,
                kickGroupMemberUseCase = kickUseCase,
                banGroupMemberUseCase = banUseCase,
                changeMemberScopeUseCase = changeScopeUseCase,
                enableListeners = false
            )
            
            val member = createMockMembers(1).first()
            member.scope = "participant"
            
            viewModel.dialogState.value shouldBe com.cometchat.uikit.core.state.DialogState.Hidden
            
            viewModel.showScopeSelection(member)
            
            viewModel.dialogState.value.shouldBeInstanceOf<com.cometchat.uikit.core.state.DialogState.SelectScope>()
            val dialogState = viewModel.dialogState.value as com.cometchat.uikit.core.state.DialogState.SelectScope
            dialogState.member shouldBe member
            dialogState.currentScope shouldBe "participant"
        }
        
        test("dismissDialog should update dialogState to Hidden") {
            val fetchUseCase = MockFetchGroupMembersUseCase()
            val kickUseCase = MockKickGroupMemberUseCase()
            val banUseCase = MockBanGroupMemberUseCase()
            val changeScopeUseCase = MockChangeMemberScopeUseCase()
            
            val viewModel = CometChatGroupMembersViewModel(
                fetchGroupMembersUseCase = fetchUseCase,
                kickGroupMemberUseCase = kickUseCase,
                banGroupMemberUseCase = banUseCase,
                changeMemberScopeUseCase = changeScopeUseCase,
                enableListeners = false
            )
            
            val member = createMockMembers(1).first()
            
            // Show dialog first
            viewModel.showKickConfirmation(member)
            viewModel.dialogState.value.shouldBeInstanceOf<com.cometchat.uikit.core.state.DialogState.ConfirmKick>()
            
            // Dismiss dialog
            viewModel.dismissDialog()
            
            viewModel.dialogState.value shouldBe com.cometchat.uikit.core.state.DialogState.Hidden
        }
        
        test("kickMember should hide dialog after execution") {
            runTest {
                val fetchUseCase = MockFetchGroupMembersUseCase()
                val kickUseCase = MockKickGroupMemberUseCase()
                val banUseCase = MockBanGroupMemberUseCase()
                val changeScopeUseCase = MockChangeMemberScopeUseCase()
                
                val viewModel = CometChatGroupMembersViewModel(
                    fetchGroupMembersUseCase = fetchUseCase,
                    kickGroupMemberUseCase = kickUseCase,
                    banGroupMemberUseCase = banUseCase,
                    changeMemberScopeUseCase = changeScopeUseCase,
                    enableListeners = false
                )
                
                val group = createMockGroup()
                viewModel.setGroup(group)
                
                // Fetch members
                val members = createMockMembers(3)
                fetchUseCase.result = Result.success(members)
                viewModel.fetchGroupMembers()
                advanceUntilIdle()
                
                val memberToKick = members.first()
                
                // Show kick confirmation
                viewModel.showKickConfirmation(memberToKick)
                viewModel.dialogState.value.shouldBeInstanceOf<com.cometchat.uikit.core.state.DialogState.ConfirmKick>()
                
                // Kick member
                kickUseCase.result = Result.success(Unit)
                viewModel.kickMember(memberToKick)
                advanceUntilIdle()
                
                // Dialog should be hidden
                viewModel.dialogState.value shouldBe com.cometchat.uikit.core.state.DialogState.Hidden
            }
        }
    }
    
    // ========================================
    // Task 1.7.4.10: Test real-time event handling
    // ========================================
    
    context("Task 1.7.4.10: Real-time event handling tests (ListOperations)") {
        
        test("addItem should add member to list") {
            val fetchUseCase = MockFetchGroupMembersUseCase()
            val kickUseCase = MockKickGroupMemberUseCase()
            val banUseCase = MockBanGroupMemberUseCase()
            val changeScopeUseCase = MockChangeMemberScopeUseCase()
            
            val viewModel = CometChatGroupMembersViewModel(
                fetchGroupMembersUseCase = fetchUseCase,
                kickGroupMemberUseCase = kickUseCase,
                banGroupMemberUseCase = banUseCase,
                changeMemberScopeUseCase = changeScopeUseCase,
                enableListeners = false
            )
            
            val member = createMockMembers(1).first()
            
            viewModel.members.value shouldBe emptyList()
            
            viewModel.addItem(member)
            
            viewModel.members.value.size shouldBe 1
            viewModel.members.value.first() shouldBe member
        }
        
        test("removeItem should remove member from list") {
            runTest {
                val fetchUseCase = MockFetchGroupMembersUseCase()
                val kickUseCase = MockKickGroupMemberUseCase()
                val banUseCase = MockBanGroupMemberUseCase()
                val changeScopeUseCase = MockChangeMemberScopeUseCase()
                
                val viewModel = CometChatGroupMembersViewModel(
                    fetchGroupMembersUseCase = fetchUseCase,
                    kickGroupMemberUseCase = kickUseCase,
                    banGroupMemberUseCase = banUseCase,
                    changeMemberScopeUseCase = changeScopeUseCase,
                    enableListeners = false
                )
                
                val group = createMockGroup()
                viewModel.setGroup(group)
                
                // Fetch members
                val members = createMockMembers(3)
                fetchUseCase.result = Result.success(members)
                viewModel.fetchGroupMembers()
                advanceUntilIdle()
                
                viewModel.members.value.size shouldBe 3
                
                // Remove member
                val memberToRemove = members.first()
                val removed = viewModel.removeItem(memberToRemove)
                
                removed shouldBe true
                viewModel.members.value.size shouldBe 2
                viewModel.members.value.none { it.uid == memberToRemove.uid } shouldBe true
            }
        }
        
        test("updateItem should update member in list") {
            runTest {
                val fetchUseCase = MockFetchGroupMembersUseCase()
                val kickUseCase = MockKickGroupMemberUseCase()
                val banUseCase = MockBanGroupMemberUseCase()
                val changeScopeUseCase = MockChangeMemberScopeUseCase()
                
                val viewModel = CometChatGroupMembersViewModel(
                    fetchGroupMembersUseCase = fetchUseCase,
                    kickGroupMemberUseCase = kickUseCase,
                    banGroupMemberUseCase = banUseCase,
                    changeMemberScopeUseCase = changeScopeUseCase,
                    enableListeners = false
                )
                
                val group = createMockGroup()
                viewModel.setGroup(group)
                
                // Fetch members
                val members = createMockMembers(3)
                fetchUseCase.result = Result.success(members)
                viewModel.fetchGroupMembers()
                advanceUntilIdle()
                
                // Update member
                val memberToUpdate = members.first()
                memberToUpdate.scope = "admin"
                val updated = viewModel.updateItem(memberToUpdate) { it.uid == memberToUpdate.uid }
                
                updated shouldBe true
                val updatedMember = viewModel.members.value.find { it.uid == memberToUpdate.uid }
                updatedMember?.scope shouldBe "admin"
            }
        }
        
        test("clearItems should remove all members") {
            runTest {
                val fetchUseCase = MockFetchGroupMembersUseCase()
                val kickUseCase = MockKickGroupMemberUseCase()
                val banUseCase = MockBanGroupMemberUseCase()
                val changeScopeUseCase = MockChangeMemberScopeUseCase()
                
                val viewModel = CometChatGroupMembersViewModel(
                    fetchGroupMembersUseCase = fetchUseCase,
                    kickGroupMemberUseCase = kickUseCase,
                    banGroupMemberUseCase = banUseCase,
                    changeMemberScopeUseCase = changeScopeUseCase,
                    enableListeners = false
                )
                
                val group = createMockGroup()
                viewModel.setGroup(group)
                
                // Fetch members
                val members = createMockMembers(3)
                fetchUseCase.result = Result.success(members)
                viewModel.fetchGroupMembers()
                advanceUntilIdle()
                
                viewModel.members.value.size shouldBe 3
                
                // Clear all
                viewModel.clearItems()
                
                viewModel.members.value shouldBe emptyList()
            }
        }
        
        test("moveItemToTop should move member to top of list") {
            runTest {
                val fetchUseCase = MockFetchGroupMembersUseCase()
                val kickUseCase = MockKickGroupMemberUseCase()
                val banUseCase = MockBanGroupMemberUseCase()
                val changeScopeUseCase = MockChangeMemberScopeUseCase()
                
                val viewModel = CometChatGroupMembersViewModel(
                    fetchGroupMembersUseCase = fetchUseCase,
                    kickGroupMemberUseCase = kickUseCase,
                    banGroupMemberUseCase = banUseCase,
                    changeMemberScopeUseCase = changeScopeUseCase,
                    enableListeners = false
                )
                
                val group = createMockGroup()
                viewModel.setGroup(group)
                
                // Fetch members
                val members = createMockMembers(3)
                fetchUseCase.result = Result.success(members)
                viewModel.fetchGroupMembers()
                advanceUntilIdle()
                
                val lastMember = members.last()
                
                // Move last member to top
                viewModel.moveItemToTop(lastMember)
                
                viewModel.members.value.first().uid shouldBe lastMember.uid
            }
        }
        
        test("getItemCount should return correct count") {
            runTest {
                val fetchUseCase = MockFetchGroupMembersUseCase()
                val kickUseCase = MockKickGroupMemberUseCase()
                val banUseCase = MockBanGroupMemberUseCase()
                val changeScopeUseCase = MockChangeMemberScopeUseCase()
                
                val viewModel = CometChatGroupMembersViewModel(
                    fetchGroupMembersUseCase = fetchUseCase,
                    kickGroupMemberUseCase = kickUseCase,
                    banGroupMemberUseCase = banUseCase,
                    changeMemberScopeUseCase = changeScopeUseCase,
                    enableListeners = false
                )
                
                val group = createMockGroup()
                viewModel.setGroup(group)
                
                viewModel.getItemCount() shouldBe 0
                
                // Fetch members
                val members = createMockMembers(5)
                fetchUseCase.result = Result.success(members)
                viewModel.fetchGroupMembers()
                advanceUntilIdle()
                
                viewModel.getItemCount() shouldBe 5
            }
        }
    }
    
    // ========================================
    // Task 1.7.4.11: Test error handling
    // ========================================
    
    context("Task 1.7.4.11: Error handling tests") {
        
        test("fetchGroupMembers should handle network errors") {
            runTest {
                val fetchUseCase = MockFetchGroupMembersUseCase()
                val kickUseCase = MockKickGroupMemberUseCase()
                val banUseCase = MockBanGroupMemberUseCase()
                val changeScopeUseCase = MockChangeMemberScopeUseCase()
                
                val viewModel = CometChatGroupMembersViewModel(
                    fetchGroupMembersUseCase = fetchUseCase,
                    kickGroupMemberUseCase = kickUseCase,
                    banGroupMemberUseCase = banUseCase,
                    changeMemberScopeUseCase = changeScopeUseCase,
                    enableListeners = false
                )
                
                val group = createMockGroup()
                viewModel.setGroup(group)
                
                // Simulate network error
                val exception = CometChatException("ERR_NETWORK", "Network connection failed")
                fetchUseCase.result = Result.failure(exception)
                
                viewModel.fetchGroupMembers()
                advanceUntilIdle()
                
                viewModel.uiState.value.shouldBeInstanceOf<GroupMembersUIState.Error>()
                val errorState = viewModel.uiState.value as GroupMembersUIState.Error
                errorState.exception.code shouldBe "ERR_NETWORK"
                errorState.exception.message shouldBe "Network connection failed"
            }
        }
        
        test("fetchGroupMembers should handle permission errors") {
            runTest {
                val fetchUseCase = MockFetchGroupMembersUseCase()
                val kickUseCase = MockKickGroupMemberUseCase()
                val banUseCase = MockBanGroupMemberUseCase()
                val changeScopeUseCase = MockChangeMemberScopeUseCase()
                
                val viewModel = CometChatGroupMembersViewModel(
                    fetchGroupMembersUseCase = fetchUseCase,
                    kickGroupMemberUseCase = kickUseCase,
                    banGroupMemberUseCase = banUseCase,
                    changeMemberScopeUseCase = changeScopeUseCase,
                    enableListeners = false
                )
                
                val group = createMockGroup()
                viewModel.setGroup(group)
                
                // Simulate permission error
                val exception = CometChatException("ERR_PERMISSION_DENIED", "Access denied")
                fetchUseCase.result = Result.failure(exception)
                
                viewModel.fetchGroupMembers()
                advanceUntilIdle()
                
                viewModel.uiState.value.shouldBeInstanceOf<GroupMembersUIState.Error>()
                val errorState = viewModel.uiState.value as GroupMembersUIState.Error
                errorState.exception.code shouldBe "ERR_PERMISSION_DENIED"
            }
        }
        
        test("kickMember should handle permission errors") {
            runTest {
                val fetchUseCase = MockFetchGroupMembersUseCase()
                val kickUseCase = MockKickGroupMemberUseCase()
                val banUseCase = MockBanGroupMemberUseCase()
                val changeScopeUseCase = MockChangeMemberScopeUseCase()
                
                val viewModel = CometChatGroupMembersViewModel(
                    fetchGroupMembersUseCase = fetchUseCase,
                    kickGroupMemberUseCase = kickUseCase,
                    banGroupMemberUseCase = banUseCase,
                    changeMemberScopeUseCase = changeScopeUseCase,
                    enableListeners = false
                )
                
                val group = createMockGroup()
                viewModel.setGroup(group)
                
                // Fetch members
                val members = createMockMembers(3)
                fetchUseCase.result = Result.success(members)
                viewModel.fetchGroupMembers()
                advanceUntilIdle()
                
                // Try to kick with permission error
                val exception = CometChatException("ERR_PERMISSION_DENIED", "Cannot kick admin")
                kickUseCase.result = Result.failure(exception)
                
                viewModel.kickMember(members.first())
                advanceUntilIdle()
                
                viewModel.uiState.value.shouldBeInstanceOf<GroupMembersUIState.Error>()
                val errorState = viewModel.uiState.value as GroupMembersUIState.Error
                errorState.exception.code shouldBe "ERR_PERMISSION_DENIED"
                
                // Member should still be in list
                viewModel.members.value.size shouldBe 3
            }
        }
        
        test("banMember should handle network errors") {
            runTest {
                val fetchUseCase = MockFetchGroupMembersUseCase()
                val kickUseCase = MockKickGroupMemberUseCase()
                val banUseCase = MockBanGroupMemberUseCase()
                val changeScopeUseCase = MockChangeMemberScopeUseCase()
                
                val viewModel = CometChatGroupMembersViewModel(
                    fetchGroupMembersUseCase = fetchUseCase,
                    kickGroupMemberUseCase = kickUseCase,
                    banGroupMemberUseCase = banUseCase,
                    changeMemberScopeUseCase = changeScopeUseCase,
                    enableListeners = false
                )
                
                val group = createMockGroup()
                viewModel.setGroup(group)
                
                // Fetch members
                val members = createMockMembers(3)
                fetchUseCase.result = Result.success(members)
                viewModel.fetchGroupMembers()
                advanceUntilIdle()
                
                // Try to ban with network error
                val exception = CometChatException("ERR_NETWORK", "Connection timeout")
                banUseCase.result = Result.failure(exception)
                
                viewModel.banMember(members.first())
                advanceUntilIdle()
                
                viewModel.uiState.value.shouldBeInstanceOf<GroupMembersUIState.Error>()
                val errorState = viewModel.uiState.value as GroupMembersUIState.Error
                errorState.exception.code shouldBe "ERR_NETWORK"
            }
        }
        
        test("changeMemberScope should handle invalid scope errors") {
            runTest {
                val fetchUseCase = MockFetchGroupMembersUseCase()
                val kickUseCase = MockKickGroupMemberUseCase()
                val banUseCase = MockBanGroupMemberUseCase()
                val changeScopeUseCase = MockChangeMemberScopeUseCase()
                
                val viewModel = CometChatGroupMembersViewModel(
                    fetchGroupMembersUseCase = fetchUseCase,
                    kickGroupMemberUseCase = kickUseCase,
                    banGroupMemberUseCase = banUseCase,
                    changeMemberScopeUseCase = changeScopeUseCase,
                    enableListeners = false
                )
                
                val group = createMockGroup()
                viewModel.setGroup(group)
                
                // Fetch members
                val members = createMockMembers(3)
                fetchUseCase.result = Result.success(members)
                viewModel.fetchGroupMembers()
                advanceUntilIdle()
                
                // Try to change scope with error
                val exception = CometChatException("ERR_INVALID_SCOPE", "Invalid scope value")
                changeScopeUseCase.result = Result.failure(exception)
                
                viewModel.changeMemberScope(members.first(), "invalid_scope")
                advanceUntilIdle()
                
                viewModel.uiState.value.shouldBeInstanceOf<GroupMembersUIState.Error>()
                val errorState = viewModel.uiState.value as GroupMembersUIState.Error
                errorState.exception.code shouldBe "ERR_INVALID_SCOPE"
            }
        }
        
        test("error state should preserve existing members list") {
            runTest {
                val fetchUseCase = MockFetchGroupMembersUseCase()
                val kickUseCase = MockKickGroupMemberUseCase()
                val banUseCase = MockBanGroupMemberUseCase()
                val changeScopeUseCase = MockChangeMemberScopeUseCase()
                
                val viewModel = CometChatGroupMembersViewModel(
                    fetchGroupMembersUseCase = fetchUseCase,
                    kickGroupMemberUseCase = kickUseCase,
                    banGroupMemberUseCase = banUseCase,
                    changeMemberScopeUseCase = changeScopeUseCase,
                    enableListeners = false
                )
                
                val group = createMockGroup()
                viewModel.setGroup(group)
                
                // Fetch members successfully
                val members = createMockMembers(3)
                fetchUseCase.result = Result.success(members)
                viewModel.fetchGroupMembers()
                advanceUntilIdle()
                
                viewModel.members.value.size shouldBe 3
                
                // Try to kick with error
                val exception = CometChatException("ERR_NETWORK", "Network error")
                kickUseCase.result = Result.failure(exception)
                viewModel.kickMember(members.first())
                advanceUntilIdle()
                
                // Members list should be preserved
                viewModel.members.value.size shouldBe 3
                viewModel.uiState.value.shouldBeInstanceOf<GroupMembersUIState.Error>()
            }
        }
        
        test("multiple consecutive errors should update state correctly") {
            runTest {
                val fetchUseCase = MockFetchGroupMembersUseCase()
                val kickUseCase = MockKickGroupMemberUseCase()
                val banUseCase = MockBanGroupMemberUseCase()
                val changeScopeUseCase = MockChangeMemberScopeUseCase()
                
                val viewModel = CometChatGroupMembersViewModel(
                    fetchGroupMembersUseCase = fetchUseCase,
                    kickGroupMemberUseCase = kickUseCase,
                    banGroupMemberUseCase = banUseCase,
                    changeMemberScopeUseCase = changeScopeUseCase,
                    enableListeners = false
                )
                
                val group = createMockGroup()
                viewModel.setGroup(group)
                
                // First error
                val exception1 = CometChatException("ERR_NETWORK", "Network error")
                fetchUseCase.result = Result.failure(exception1)
                viewModel.fetchGroupMembers()
                advanceUntilIdle()
                
                viewModel.uiState.value.shouldBeInstanceOf<GroupMembersUIState.Error>()
                val errorState1 = viewModel.uiState.value as GroupMembersUIState.Error
                errorState1.exception.code shouldBe "ERR_NETWORK"
                
                // Second error (different)
                val exception2 = CometChatException("ERR_PERMISSION_DENIED", "Access denied")
                fetchUseCase.result = Result.failure(exception2)
                viewModel.fetchGroupMembers()
                advanceUntilIdle()
                
                viewModel.uiState.value.shouldBeInstanceOf<GroupMembersUIState.Error>()
                val errorState2 = viewModel.uiState.value as GroupMembersUIState.Error
                errorState2.exception.code shouldBe "ERR_PERMISSION_DENIED"
            }
        }
    }
    
    // ========================================
    // Additional coverage tests for >90%
    // ========================================
    
    context("Configuration methods") {
        
        test("setExcludeOwner should filter owner from fetched members") {
            runTest {
                val fetchUseCase = MockFetchGroupMembersUseCase()
                val kickUseCase = MockKickGroupMemberUseCase()
                val banUseCase = MockBanGroupMemberUseCase()
                val changeScopeUseCase = MockChangeMemberScopeUseCase()
                
                val viewModel = CometChatGroupMembersViewModel(
                    fetchGroupMembersUseCase = fetchUseCase,
                    kickGroupMemberUseCase = kickUseCase,
                    banGroupMemberUseCase = banUseCase,
                    changeMemberScopeUseCase = changeScopeUseCase,
                    enableListeners = false
                )
                
                val group = createMockGroup()
                group.owner = "user1"
                viewModel.setGroup(group)
                viewModel.setExcludeOwner(true)
                
                // Return members including the owner
                val members = createMockMembers(3) // user1, user2, user3
                fetchUseCase.result = Result.success(members)
                viewModel.fetchGroupMembers()
                advanceUntilIdle()
                
                // Owner (user1) should be excluded
                viewModel.members.value.size shouldBe 2
                viewModel.members.value.none { it.uid == "user1" } shouldBe true
            }
        }
        
        test("setGroupMembersRequestBuilder should accept custom builder") {
            val fetchUseCase = MockFetchGroupMembersUseCase()
            val kickUseCase = MockKickGroupMemberUseCase()
            val banUseCase = MockBanGroupMemberUseCase()
            val changeScopeUseCase = MockChangeMemberScopeUseCase()
            
            val viewModel = CometChatGroupMembersViewModel(
                fetchGroupMembersUseCase = fetchUseCase,
                kickGroupMemberUseCase = kickUseCase,
                banGroupMemberUseCase = banUseCase,
                changeMemberScopeUseCase = changeScopeUseCase,
                enableListeners = false
            )
            
            val group = createMockGroup()
            viewModel.setGroup(group)
            
            // Should not throw
            val builder = GroupMembersRequest.GroupMembersRequestBuilder(group.guid)
                .setLimit(50)
            viewModel.setGroupMembersRequestBuilder(builder)
        }
        
        test("setSearchRequestBuilder should accept custom builder") {
            val fetchUseCase = MockFetchGroupMembersUseCase()
            val kickUseCase = MockKickGroupMemberUseCase()
            val banUseCase = MockBanGroupMemberUseCase()
            val changeScopeUseCase = MockChangeMemberScopeUseCase()
            
            val viewModel = CometChatGroupMembersViewModel(
                fetchGroupMembersUseCase = fetchUseCase,
                kickGroupMemberUseCase = kickUseCase,
                banGroupMemberUseCase = banUseCase,
                changeMemberScopeUseCase = changeScopeUseCase,
                enableListeners = false
            )
            
            val builder = GroupMembersRequest.GroupMembersRequestBuilder("test-group")
            viewModel.setSearchRequestBuilder(builder)
        }
    }
    
    context("refreshList tests") {
        
        test("refreshList should clear members and refetch") {
            runTest {
                val fetchUseCase = MockFetchGroupMembersUseCase()
                val kickUseCase = MockKickGroupMemberUseCase()
                val banUseCase = MockBanGroupMemberUseCase()
                val changeScopeUseCase = MockChangeMemberScopeUseCase()
                
                val viewModel = CometChatGroupMembersViewModel(
                    fetchGroupMembersUseCase = fetchUseCase,
                    kickGroupMemberUseCase = kickUseCase,
                    banGroupMemberUseCase = banUseCase,
                    changeMemberScopeUseCase = changeScopeUseCase,
                    enableListeners = false
                )
                
                val group = createMockGroup()
                viewModel.setGroup(group)
                
                // Initial fetch
                fetchUseCase.result = Result.success(createMockMembers(3))
                viewModel.fetchGroupMembers()
                advanceUntilIdle()
                
                viewModel.members.value.size shouldBe 3
                
                // Refresh with new data
                fetchUseCase.result = Result.success(createMockMembers(5, 10))
                viewModel.refreshList()
                advanceUntilIdle()
                
                // Should have new members only
                viewModel.members.value.size shouldBe 5
                viewModel.members.value.first().uid shouldBe "user10"
            }
        }
        
        test("refreshList should reset hasMore flag") {
            runTest {
                val fetchUseCase = MockFetchGroupMembersUseCase()
                val kickUseCase = MockKickGroupMemberUseCase()
                val banUseCase = MockBanGroupMemberUseCase()
                val changeScopeUseCase = MockChangeMemberScopeUseCase()
                
                val viewModel = CometChatGroupMembersViewModel(
                    fetchGroupMembersUseCase = fetchUseCase,
                    kickGroupMemberUseCase = kickUseCase,
                    banGroupMemberUseCase = banUseCase,
                    changeMemberScopeUseCase = changeScopeUseCase,
                    enableListeners = false
                )
                
                val group = createMockGroup()
                viewModel.setGroup(group)
                
                // Fetch empty to set hasMore = false
                fetchUseCase.result = Result.success(emptyList())
                viewModel.fetchGroupMembers()
                advanceUntilIdle()
                
                viewModel.hasMore.value shouldBe false
                
                // Refresh should reset hasMore and fetch again
                fetchUseCase.result = Result.success(createMockMembers(3))
                viewModel.refreshList()
                advanceUntilIdle()
                
                viewModel.hasMore.value shouldBe true
                viewModel.members.value.size shouldBe 3
            }
        }
    }
    
    context("Additional ListOperations tests") {
        
        test("addItems should add multiple members at once") {
            val fetchUseCase = MockFetchGroupMembersUseCase()
            val kickUseCase = MockKickGroupMemberUseCase()
            val banUseCase = MockBanGroupMemberUseCase()
            val changeScopeUseCase = MockChangeMemberScopeUseCase()
            
            val viewModel = CometChatGroupMembersViewModel(
                fetchGroupMembersUseCase = fetchUseCase,
                kickGroupMemberUseCase = kickUseCase,
                banGroupMemberUseCase = banUseCase,
                changeMemberScopeUseCase = changeScopeUseCase,
                enableListeners = false
            )
            
            val members = createMockMembers(5)
            viewModel.addItems(members)
            
            viewModel.members.value.size shouldBe 5
        }
        
        test("removeItemAt should remove member at specific index") {
            val fetchUseCase = MockFetchGroupMembersUseCase()
            val kickUseCase = MockKickGroupMemberUseCase()
            val banUseCase = MockBanGroupMemberUseCase()
            val changeScopeUseCase = MockChangeMemberScopeUseCase()
            
            val viewModel = CometChatGroupMembersViewModel(
                fetchGroupMembersUseCase = fetchUseCase,
                kickGroupMemberUseCase = kickUseCase,
                banGroupMemberUseCase = banUseCase,
                changeMemberScopeUseCase = changeScopeUseCase,
                enableListeners = false
            )
            
            val members = createMockMembers(3)
            viewModel.addItems(members)
            
            val removed = viewModel.removeItemAt(1)
            
            removed?.uid shouldBe "user2"
            viewModel.members.value.size shouldBe 2
        }
        
        test("removeItemAt should return null for invalid index") {
            val fetchUseCase = MockFetchGroupMembersUseCase()
            val kickUseCase = MockKickGroupMemberUseCase()
            val banUseCase = MockBanGroupMemberUseCase()
            val changeScopeUseCase = MockChangeMemberScopeUseCase()
            
            val viewModel = CometChatGroupMembersViewModel(
                fetchGroupMembersUseCase = fetchUseCase,
                kickGroupMemberUseCase = kickUseCase,
                banGroupMemberUseCase = banUseCase,
                changeMemberScopeUseCase = changeScopeUseCase,
                enableListeners = false
            )
            
            val removed = viewModel.removeItemAt(5)
            removed shouldBe null
        }
        
        test("getItems should return current members list") {
            val fetchUseCase = MockFetchGroupMembersUseCase()
            val kickUseCase = MockKickGroupMemberUseCase()
            val banUseCase = MockBanGroupMemberUseCase()
            val changeScopeUseCase = MockChangeMemberScopeUseCase()
            
            val viewModel = CometChatGroupMembersViewModel(
                fetchGroupMembersUseCase = fetchUseCase,
                kickGroupMemberUseCase = kickUseCase,
                banGroupMemberUseCase = banUseCase,
                changeMemberScopeUseCase = changeScopeUseCase,
                enableListeners = false
            )
            
            viewModel.getItems() shouldBe emptyList()
            
            val members = createMockMembers(3)
            viewModel.addItems(members)
            
            viewModel.getItems().size shouldBe 3
            viewModel.getItems() shouldBe viewModel.members.value
        }
        
        test("getItemAt should return member at specific index") {
            val fetchUseCase = MockFetchGroupMembersUseCase()
            val kickUseCase = MockKickGroupMemberUseCase()
            val banUseCase = MockBanGroupMemberUseCase()
            val changeScopeUseCase = MockChangeMemberScopeUseCase()
            
            val viewModel = CometChatGroupMembersViewModel(
                fetchGroupMembersUseCase = fetchUseCase,
                kickGroupMemberUseCase = kickUseCase,
                banGroupMemberUseCase = banUseCase,
                changeMemberScopeUseCase = changeScopeUseCase,
                enableListeners = false
            )
            
            val members = createMockMembers(3)
            viewModel.addItems(members)
            
            viewModel.getItemAt(0)?.uid shouldBe "user1"
            viewModel.getItemAt(2)?.uid shouldBe "user3"
            viewModel.getItemAt(5) shouldBe null
        }
        
        test("batch should apply multiple operations atomically") {
            val fetchUseCase = MockFetchGroupMembersUseCase()
            val kickUseCase = MockKickGroupMemberUseCase()
            val banUseCase = MockBanGroupMemberUseCase()
            val changeScopeUseCase = MockChangeMemberScopeUseCase()
            
            val viewModel = CometChatGroupMembersViewModel(
                fetchGroupMembersUseCase = fetchUseCase,
                kickGroupMemberUseCase = kickUseCase,
                banGroupMemberUseCase = banUseCase,
                changeMemberScopeUseCase = changeScopeUseCase,
                enableListeners = false
            )
            
            val members = createMockMembers(3)
            viewModel.addItems(members)
            
            val newMember = GroupMember("user99", "admin").apply { name = "User 99" }
            
            viewModel.batch {
                remove(members.first())
                add(newMember)
            }
            
            viewModel.members.value.size shouldBe 3
            viewModel.members.value.none { it.uid == "user1" } shouldBe true
            viewModel.members.value.any { it.uid == "user99" } shouldBe true
        }
    }
    
    context("Ban member edge cases") {
        
        test("banMember should update state to Empty when last member is banned") {
            runTest {
                val fetchUseCase = MockFetchGroupMembersUseCase()
                val kickUseCase = MockKickGroupMemberUseCase()
                val banUseCase = MockBanGroupMemberUseCase()
                val changeScopeUseCase = MockChangeMemberScopeUseCase()
                
                val viewModel = CometChatGroupMembersViewModel(
                    fetchGroupMembersUseCase = fetchUseCase,
                    kickGroupMemberUseCase = kickUseCase,
                    banGroupMemberUseCase = banUseCase,
                    changeMemberScopeUseCase = changeScopeUseCase,
                    enableListeners = false
                )
                
                val group = createMockGroup()
                viewModel.setGroup(group)
                
                // Fetch single member
                val members = createMockMembers(1)
                fetchUseCase.result = Result.success(members)
                viewModel.fetchGroupMembers()
                advanceUntilIdle()
                
                // Ban the only member
                banUseCase.result = Result.success(Unit)
                viewModel.banMember(members.first())
                advanceUntilIdle()
                
                // State should be Empty
                viewModel.uiState.value shouldBe GroupMembersUIState.Empty
                viewModel.members.value shouldBe emptyList()
            }
        }
    }
    
    context("Dialog state transitions") {
        
        test("banMember should hide dialog after execution") {
            runTest {
                val fetchUseCase = MockFetchGroupMembersUseCase()
                val kickUseCase = MockKickGroupMemberUseCase()
                val banUseCase = MockBanGroupMemberUseCase()
                val changeScopeUseCase = MockChangeMemberScopeUseCase()
                
                val viewModel = CometChatGroupMembersViewModel(
                    fetchGroupMembersUseCase = fetchUseCase,
                    kickGroupMemberUseCase = kickUseCase,
                    banGroupMemberUseCase = banUseCase,
                    changeMemberScopeUseCase = changeScopeUseCase,
                    enableListeners = false
                )
                
                val group = createMockGroup()
                viewModel.setGroup(group)
                
                val members = createMockMembers(3)
                fetchUseCase.result = Result.success(members)
                viewModel.fetchGroupMembers()
                advanceUntilIdle()
                
                val memberToBan = members.first()
                viewModel.showBanConfirmation(memberToBan)
                viewModel.dialogState.value.shouldBeInstanceOf<com.cometchat.uikit.core.state.DialogState.ConfirmBan>()
                
                banUseCase.result = Result.success(Unit)
                viewModel.banMember(memberToBan)
                advanceUntilIdle()
                
                viewModel.dialogState.value shouldBe com.cometchat.uikit.core.state.DialogState.Hidden
            }
        }
        
        test("changeMemberScope should hide dialog after execution") {
            runTest {
                val fetchUseCase = MockFetchGroupMembersUseCase()
                val kickUseCase = MockKickGroupMemberUseCase()
                val banUseCase = MockBanGroupMemberUseCase()
                val changeScopeUseCase = MockChangeMemberScopeUseCase()
                
                val viewModel = CometChatGroupMembersViewModel(
                    fetchGroupMembersUseCase = fetchUseCase,
                    kickGroupMemberUseCase = kickUseCase,
                    banGroupMemberUseCase = banUseCase,
                    changeMemberScopeUseCase = changeScopeUseCase,
                    enableListeners = false
                )
                
                val group = createMockGroup()
                viewModel.setGroup(group)
                
                val members = createMockMembers(3)
                fetchUseCase.result = Result.success(members)
                viewModel.fetchGroupMembers()
                advanceUntilIdle()
                
                val member = members.first()
                viewModel.showScopeSelection(member)
                viewModel.dialogState.value.shouldBeInstanceOf<com.cometchat.uikit.core.state.DialogState.SelectScope>()
                
                changeScopeUseCase.result = Result.success(Unit)
                viewModel.changeMemberScope(member, "admin")
                advanceUntilIdle()
                
                viewModel.dialogState.value shouldBe com.cometchat.uikit.core.state.DialogState.Hidden
            }
        }
    }
    
    context("kickMember and banMember without group set") {
        
        test("kickMember should do nothing when group is not set") {
            runTest {
                val fetchUseCase = MockFetchGroupMembersUseCase()
                val kickUseCase = MockKickGroupMemberUseCase()
                val banUseCase = MockBanGroupMemberUseCase()
                val changeScopeUseCase = MockChangeMemberScopeUseCase()
                
                val viewModel = CometChatGroupMembersViewModel(
                    fetchGroupMembersUseCase = fetchUseCase,
                    kickGroupMemberUseCase = kickUseCase,
                    banGroupMemberUseCase = banUseCase,
                    changeMemberScopeUseCase = changeScopeUseCase,
                    enableListeners = false
                )
                
                val member = createMockMembers(1).first()
                
                // Should not throw, just return early
                viewModel.kickMember(member)
                advanceUntilIdle()
                
                // State should remain Loading (initial)
                viewModel.uiState.value shouldBe GroupMembersUIState.Loading
            }
        }
        
        test("banMember should do nothing when group is not set") {
            runTest {
                val fetchUseCase = MockFetchGroupMembersUseCase()
                val kickUseCase = MockKickGroupMemberUseCase()
                val banUseCase = MockBanGroupMemberUseCase()
                val changeScopeUseCase = MockChangeMemberScopeUseCase()
                
                val viewModel = CometChatGroupMembersViewModel(
                    fetchGroupMembersUseCase = fetchUseCase,
                    kickGroupMemberUseCase = kickUseCase,
                    banGroupMemberUseCase = banUseCase,
                    changeMemberScopeUseCase = changeScopeUseCase,
                    enableListeners = false
                )
                
                val member = createMockMembers(1).first()
                
                viewModel.banMember(member)
                advanceUntilIdle()
                
                viewModel.uiState.value shouldBe GroupMembersUIState.Loading
            }
        }
        
        test("changeMemberScope should do nothing when group is not set") {
            runTest {
                val fetchUseCase = MockFetchGroupMembersUseCase()
                val kickUseCase = MockKickGroupMemberUseCase()
                val banUseCase = MockBanGroupMemberUseCase()
                val changeScopeUseCase = MockChangeMemberScopeUseCase()
                
                val viewModel = CometChatGroupMembersViewModel(
                    fetchGroupMembersUseCase = fetchUseCase,
                    kickGroupMemberUseCase = kickUseCase,
                    banGroupMemberUseCase = banUseCase,
                    changeMemberScopeUseCase = changeScopeUseCase,
                    enableListeners = false
                )
                
                val member = createMockMembers(1).first()
                
                viewModel.changeMemberScope(member, "admin")
                advanceUntilIdle()
                
                viewModel.uiState.value shouldBe GroupMembersUIState.Loading
            }
        }
    }

    // ========================================
    // Bugfix: search-clear-stale-list
    // ========================================

    context("Bugfix: searchGroupMembers and refreshList should call resetRequest") {

        test("refreshList should call resetRequest exactly once") {
            runTest {
                val fetchUseCase = MockFetchGroupMembersUseCase()
                val kickUseCase = MockKickGroupMemberUseCase()
                val banUseCase = MockBanGroupMemberUseCase()
                val changeScopeUseCase = MockChangeMemberScopeUseCase()

                fetchUseCase.result = Result.success(createMockMembers(3))

                val viewModel = CometChatGroupMembersViewModel(
                    fetchGroupMembersUseCase = fetchUseCase,
                    kickGroupMemberUseCase = kickUseCase,
                    banGroupMemberUseCase = banUseCase,
                    changeMemberScopeUseCase = changeScopeUseCase,
                    enableListeners = false
                )

                val group = createMockGroup()
                viewModel.setGroup(group)

                // Initial fetch
                viewModel.fetchGroupMembers()
                advanceUntilIdle()

                // Reset counter after initial setup
                fetchUseCase.resetRequestCallCount = 0

                // refreshList should call resetRequest
                viewModel.refreshList()
                advanceUntilIdle()

                fetchUseCase.resetRequestCallCount shouldBe 1
            }
        }

        test("searchGroupMembers should call resetRequest exactly once per call") {
            runTest {
                val fetchUseCase = MockFetchGroupMembersUseCase()
                val kickUseCase = MockKickGroupMemberUseCase()
                val banUseCase = MockBanGroupMemberUseCase()
                val changeScopeUseCase = MockChangeMemberScopeUseCase()

                fetchUseCase.result = Result.success(createMockMembers(5))

                val viewModel = CometChatGroupMembersViewModel(
                    fetchGroupMembersUseCase = fetchUseCase,
                    kickGroupMemberUseCase = kickUseCase,
                    banGroupMemberUseCase = banUseCase,
                    changeMemberScopeUseCase = changeScopeUseCase,
                    enableListeners = false
                )

                val group = createMockGroup()
                viewModel.setGroup(group)

                // Search with keyword
                viewModel.searchGroupMembers("a")
                advanceUntilIdle()

                fetchUseCase.resetRequestCallCount shouldBe 1

                // Clear search
                viewModel.searchGroupMembers(null)
                advanceUntilIdle()

                fetchUseCase.resetRequestCallCount shouldBe 2
            }
        }

        test("search then clear should return full list (end-to-end bug scenario)") {
            runTest {
                val fetchUseCase = MockFetchGroupMembersUseCase()
                val kickUseCase = MockKickGroupMemberUseCase()
                val banUseCase = MockBanGroupMemberUseCase()
                val changeScopeUseCase = MockChangeMemberScopeUseCase()

                val viewModel = CometChatGroupMembersViewModel(
                    fetchGroupMembersUseCase = fetchUseCase,
                    kickGroupMemberUseCase = kickUseCase,
                    banGroupMemberUseCase = banUseCase,
                    changeMemberScopeUseCase = changeScopeUseCase,
                    enableListeners = false
                )

                val group = createMockGroup()
                viewModel.setGroup(group)

                // Initial fetch: 5 members
                fetchUseCase.result = Result.success(createMockMembers(5))
                viewModel.fetchGroupMembers()
                advanceUntilIdle()
                viewModel.members.value.size shouldBe 5

                // Search for "a": 2 filtered results
                fetchUseCase.result = Result.success(createMockMembers(2, 10))
                viewModel.searchGroupMembers("a")
                advanceUntilIdle()
                viewModel.members.value.size shouldBe 2

                // Clear search: should get full list back
                fetchUseCase.result = Result.success(createMockMembers(5))
                viewModel.searchGroupMembers(null)
                advanceUntilIdle()

                // Verify full list is returned, not stale filtered results
                viewModel.members.value.size shouldBe 5
                viewModel.uiState.value.shouldBeInstanceOf<GroupMembersUIState.Content>()
            }
        }

        test("pagination should NOT call resetRequest") {
            runTest {
                val fetchUseCase = MockFetchGroupMembersUseCase()
                val kickUseCase = MockKickGroupMemberUseCase()
                val banUseCase = MockBanGroupMemberUseCase()
                val changeScopeUseCase = MockChangeMemberScopeUseCase()

                val viewModel = CometChatGroupMembersViewModel(
                    fetchGroupMembersUseCase = fetchUseCase,
                    kickGroupMemberUseCase = kickUseCase,
                    banGroupMemberUseCase = banUseCase,
                    changeMemberScopeUseCase = changeScopeUseCase,
                    enableListeners = false
                )

                val group = createMockGroup()
                viewModel.setGroup(group)

                // First page
                fetchUseCase.result = Result.success(createMockMembers(3, 1))
                viewModel.fetchGroupMembers()
                advanceUntilIdle()

                // Second page
                fetchUseCase.result = Result.success(createMockMembers(3, 4))
                viewModel.fetchGroupMembers()
                advanceUntilIdle()

                // Pagination should never call resetRequest
                fetchUseCase.resetRequestCallCount shouldBe 0
                viewModel.members.value.size shouldBe 6
            }
        }
    }

    // ========================================
    // Feature: scope-change-listener-fix
    // Property-based tests for local event listener behavior
    // ========================================

    /**
     * Helper to create a fresh ViewModel with mock use cases and pre-populated members.
     * Uses enableListeners = false to avoid SDK dependency.
     */
    suspend fun kotlinx.coroutines.test.TestScope.createViewModelWithMembers(
        memberCount: Int,
        startId: Int = 1,
        groupGuid: String = "test-group"
    ): Pair<CometChatGroupMembersViewModel, List<GroupMember>> {
        val fetchUseCase = MockFetchGroupMembersUseCase()
        val kickUseCase = MockKickGroupMemberUseCase()
        val banUseCase = MockBanGroupMemberUseCase()
        val changeScopeUseCase = MockChangeMemberScopeUseCase()

        val members = createMockMembers(memberCount, startId)
        fetchUseCase.result = Result.success(members)

        val viewModel = CometChatGroupMembersViewModel(
            fetchGroupMembersUseCase = fetchUseCase,
            kickGroupMemberUseCase = kickUseCase,
            banGroupMemberUseCase = banUseCase,
            changeMemberScopeUseCase = changeScopeUseCase,
            enableListeners = false
        )

        val group = createMockGroup(groupGuid)
        viewModel.setGroup(group)
        viewModel.fetchGroupMembers()
        advanceUntilIdle()

        return viewModel to members
    }

    context("Feature: scope-change-listener-fix, Property 1: Non-matching group events are ignored") {

        /**
         * **Property 1: Non-matching group events are ignored**
         *
         * For any members list, if a removal/update/add operation targets a member
         * whose uid does NOT exist in the list, the list remains unchanged.
         * This simulates the guard: `eventGroup?.guid != currentGroup?.guid`.
         *
         * Since we can't trigger the actual local event listener (requires SDK),
         * we verify the underlying operations are no-ops for non-matching members.
         *
         * **Validates: Requirements 2.1**
         */
        test("Property 1: removeItem for non-existing member should not change list") {
            checkAll(20, Arb.int(1, 20)) { memberCount ->
                runTest {
                    val (viewModel, originalMembers) = createViewModelWithMembers(memberCount)

                    // Try to remove a member that doesn't exist in the list
                    val nonExistentMember = GroupMember("non-existent-uid", "participant").apply {
                        name = "Ghost User"
                    }
                    val removed = viewModel.removeItem(nonExistentMember)

                    removed shouldBe false
                    viewModel.members.value.size shouldBe memberCount
                    viewModel.members.value.map { it.uid } shouldBe originalMembers.map { it.uid }
                }
            }
        }

        test("Property 1: updateItem for non-existing member should not change list") {
            checkAll(20, Arb.int(1, 20)) { memberCount ->
                runTest {
                    val (viewModel, originalMembers) = createViewModelWithMembers(memberCount)

                    val nonExistentMember = GroupMember("non-existent-uid", "admin").apply {
                        name = "Ghost User"
                    }
                    val updated = viewModel.updateItem(nonExistentMember) { it.uid == "non-existent-uid" }

                    updated shouldBe false
                    viewModel.members.value.size shouldBe memberCount
                    viewModel.members.value.map { it.uid } shouldBe originalMembers.map { it.uid }
                }
            }
        }
    }

    context("Feature: scope-change-listener-fix, Property 2: Removal events remove the target member") {

        /**
         * **Property 2: Removal events remove the target member**
         *
         * For any members list containing a target member, removing that member
         * should result in a list that no longer contains the member and has size - 1.
         *
         * **Validates: Requirements 3.1, 3.2**
         */
        test("Property 2: removeItem should remove exactly the target member for any list size") {
            checkAll(20, Arb.int(1, 20)) { memberCount ->
                runTest {
                    val (viewModel, members) = createViewModelWithMembers(memberCount)

                    // Pick a random member to remove (use index based on memberCount)
                    val targetIndex = (memberCount - 1) / 2
                    val targetMember = members[targetIndex]

                    val removed = viewModel.removeItem(targetMember)

                    removed shouldBe true
                    viewModel.members.value.size shouldBe (memberCount - 1)
                    viewModel.members.value.none { it.uid == targetMember.uid } shouldBe true
                    // All other members should still be present
                    members.filter { it.uid != targetMember.uid }.forEach { m ->
                        viewModel.members.value.any { it.uid == m.uid } shouldBe true
                    }
                }
            }
        }

        test("Property 2: removing last member should leave empty list") {
            runTest {
                val (viewModel, members) = createViewModelWithMembers(1)

                val removed = viewModel.removeItem(members.first())

                removed shouldBe true
                viewModel.members.value shouldBe emptyList()
            }
        }
    }

    context("Feature: scope-change-listener-fix, Property 3: Scope change updates the member's scope") {

        /**
         * **Property 3: Scope change updates the member's scope**
         *
         * For any members list containing a target member, updating that member
         * with a new scope should result in the member having the new scope value.
         *
         * **Validates: Requirements 4.1, 4.2**
         */
        test("Property 3: updateItem should change scope for any valid scope value") {
            val scopes = listOf("participant", "admin", "moderator")
            checkAll(20, Arb.int(1, 15), Arb.element(scopes)) { memberCount, newScope ->
                runTest {
                    val (viewModel, members) = createViewModelWithMembers(memberCount)

                    val targetIndex = (memberCount - 1) / 2
                    val targetMember = members[targetIndex]

                    // Create updated member with new scope (mimics userToGroupMember with isScopeUpdate=true)
                    val updatedMember = GroupMember(targetMember.uid, newScope).apply {
                        name = targetMember.name
                        avatar = targetMember.avatar
                        status = targetMember.status
                    }

                    val updated = viewModel.updateItem(updatedMember) { it.uid == targetMember.uid }

                    updated shouldBe true
                    val memberInList = viewModel.members.value.find { it.uid == targetMember.uid }
                    memberInList?.scope shouldBe newScope
                    viewModel.members.value.size shouldBe memberCount
                }
            }
        }
    }

    context("Feature: scope-change-listener-fix, Property 4: Addition events add member to top of list") {

        /**
         * **Property 4: Addition events add member to top of list**
         *
         * For any members list and a new member not in the list,
         * moveItemToTop should place the new member at index 0.
         *
         * **Validates: Requirements 5.1, 5.2, 5.3**
         */
        test("Property 4: moveItemToTop should place new member at index 0 for any list size") {
            checkAll(20, Arb.int(0, 20)) { memberCount ->
                runTest {
                    val (viewModel, _) = createViewModelWithMembers(memberCount)

                    // Create a new member not in the list
                    val newMember = GroupMember("new-user-999", "participant").apply {
                        name = "New User"
                    }

                    viewModel.moveItemToTop(newMember)

                    viewModel.members.value.size shouldBe (memberCount + 1)
                    viewModel.members.value.first().uid shouldBe "new-user-999"
                }
            }
        }

        test("Property 4: moveItemToTop for existing member should move to index 0 without duplicating") {
            checkAll(20, Arb.int(2, 15)) { memberCount ->
                runTest {
                    val (viewModel, members) = createViewModelWithMembers(memberCount)

                    // Pick the last member and move to top
                    val lastMember = members.last()
                    viewModel.moveItemToTop(lastMember)

                    viewModel.members.value.size shouldBe memberCount // no duplicate
                    viewModel.members.value.first().uid shouldBe lastMember.uid
                }
            }
        }
    }

    context("Feature: scope-change-listener-fix, Property 5: Ownership change updates the new owner in list") {

        /**
         * **Property 5: Ownership change updates the new owner in list**
         *
         * For any members list containing the new owner, updating the owner's
         * GroupMember entry should reflect the new data in the list.
         *
         * **Validates: Requirements 6.1, 6.2**
         */
        test("Property 5: updateItem should update owner entry for any list size") {
            checkAll(20, Arb.int(1, 15)) { memberCount ->
                runTest {
                    val (viewModel, members) = createViewModelWithMembers(memberCount)

                    val targetIndex = 0
                    val targetMember = members[targetIndex]

                    // Simulate ownership change: update with owner scope
                    val newOwner = GroupMember(targetMember.uid, "admin").apply {
                        name = "New Owner Name"
                        avatar = targetMember.avatar
                        status = targetMember.status
                    }

                    val updated = viewModel.updateItem(newOwner) { it.uid == targetMember.uid }

                    updated shouldBe true
                    val ownerInList = viewModel.members.value.find { it.uid == targetMember.uid }
                    ownerInList?.name shouldBe "New Owner Name"
                    ownerInList?.scope shouldBe "admin"
                    viewModel.members.value.size shouldBe memberCount
                }
            }
        }
    }

    // ========================================
    // Feature: scope-change-listener-fix
    // Edge case unit tests (Task 3.6)
    // ========================================

    context("Feature: scope-change-listener-fix, Edge cases") {

        test("event received before setGroup should not crash or change list") {
            val fetchUseCase = MockFetchGroupMembersUseCase()
            val kickUseCase = MockKickGroupMemberUseCase()
            val banUseCase = MockBanGroupMemberUseCase()
            val changeScopeUseCase = MockChangeMemberScopeUseCase()

            val viewModel = CometChatGroupMembersViewModel(
                fetchGroupMembersUseCase = fetchUseCase,
                kickGroupMemberUseCase = kickUseCase,
                banGroupMemberUseCase = banUseCase,
                changeMemberScopeUseCase = changeScopeUseCase,
                enableListeners = false
            )

            // No setGroup called — simulate what would happen if event arrived
            // The list operations should be safe on an empty list
            val ghostMember = GroupMember("ghost", "participant").apply { name = "Ghost" }
            val removed = viewModel.removeItem(ghostMember)
            removed shouldBe false
            viewModel.members.value shouldBe emptyList()

            val updated = viewModel.updateItem(ghostMember) { it.uid == "ghost" }
            updated shouldBe false
            viewModel.members.value shouldBe emptyList()

            viewModel.moveItemToTop(ghostMember)
            viewModel.members.value.size shouldBe 1
            viewModel.members.value.first().uid shouldBe "ghost"
        }

        test("ViewModel with enableListeners=false should not register listeners") {
            val fetchUseCase = MockFetchGroupMembersUseCase()
            val kickUseCase = MockKickGroupMemberUseCase()
            val banUseCase = MockBanGroupMemberUseCase()
            val changeScopeUseCase = MockChangeMemberScopeUseCase()

            // enableListeners = false — no SDK calls, no local event collection
            val viewModel = CometChatGroupMembersViewModel(
                fetchGroupMembersUseCase = fetchUseCase,
                kickGroupMemberUseCase = kickUseCase,
                banGroupMemberUseCase = banUseCase,
                changeMemberScopeUseCase = changeScopeUseCase,
                enableListeners = false
            )

            val group = createMockGroup()
            viewModel.setGroup(group)

            // Should not crash — listeners are not registered
            viewModel.uiState.value shouldBe GroupMembersUIState.Loading
        }

        test("MemberKicked for member not in list should not crash") {
            runTest {
                val (viewModel, _) = createViewModelWithMembers(3)

                val nonExistentMember = GroupMember("not-in-list", "participant").apply {
                    name = "Not In List"
                }
                val removed = viewModel.removeItem(nonExistentMember)

                removed shouldBe false
                viewModel.members.value.size shouldBe 3
            }
        }
    }
})
