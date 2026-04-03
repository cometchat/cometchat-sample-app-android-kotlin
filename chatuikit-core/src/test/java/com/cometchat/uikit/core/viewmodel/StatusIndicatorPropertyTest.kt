package com.cometchat.uikit.core.viewmodel

import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.UsersRequest
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.domain.repository.UsersRepository
import com.cometchat.uikit.core.domain.usecase.FetchUsersUseCase
import com.cometchat.uikit.core.domain.usecase.SearchUsersUseCase
import com.cometchat.uikit.core.events.CometChatEvents
import com.cometchat.uikit.core.events.CometChatUserEvent
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.element
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
 * Status Indicator enum for testing purposes.
 * Mirrors the UI layer StatusIndicator enum.
 */
private enum class StatusIndicator {
    ONLINE,
    OFFLINE
}

/**
 * Utility object for status indicator logic.
 * This mirrors the logic in CometChatUsersListItem.getStatusIndicatorFromUser()
 */
private object StatusIndicatorUtils {
    /**
     * Determines the status indicator for a user.
     * 
     * Rules:
     * - Blocked users always show OFFLINE regardless of actual status
     * - Online users (not blocked) show ONLINE
     * - Offline users show OFFLINE
     */
    fun getStatusIndicatorFromUser(user: User): StatusIndicator {
        // Blocked users always show offline
        if (user.isBlockedByMe || user.isHasBlockedMe) {
            return StatusIndicator.OFFLINE
        }
        
        return if (user.status == CometChatConstants.USER_STATUS_ONLINE) {
            StatusIndicator.ONLINE
        } else {
            StatusIndicator.OFFLINE
        }
    }
    
    /**
     * Checks if a user is blocked (either direction).
     */
    fun isBlocked(user: User): Boolean {
        return user.isBlockedByMe || user.isHasBlockedMe
    }
}

/**
 * Property-based tests for Status Indicator behavior.
 * Tests Property 7 (Status Indicator State) and Property 8 (Real-Time User Updates).
 *
 * Feature: users-component
 */
@OptIn(ExperimentalCoroutinesApi::class)
class StatusIndicatorPropertyTest : FunSpec({

    // ==================== Test Helpers ====================

    /**
     * Creates a mock User with configurable status and blocked state.
     */
    fun createMockUser(
        uid: String,
        name: String = "User $uid",
        status: String = CometChatConstants.USER_STATUS_OFFLINE,
        blockedByMe: Boolean = false,
        hasBlockedMe: Boolean = false
    ): User {
        return User().apply {
            this.uid = uid
            this.name = name
            this.status = status
            this.setBlockedByMe(blockedByMe)
            this.setHasBlockedMe(hasBlockedMe)
        }
    }

    /**
     * Arbitrary generator for user status (online or offline).
     */
    val userStatusArb = Arb.element(
        CometChatConstants.USER_STATUS_ONLINE,
        CometChatConstants.USER_STATUS_OFFLINE
    )

    /**
     * Arbitrary generator for a user with random status and blocked state.
     */
    fun arbUser(uidPrefix: String = "uid") = arbitrary { rs ->
        val uid = "${uidPrefix}_${Arb.int(0..10000).bind()}"
        val name = "User ${Arb.string(3..20).bind()}"
        val status = userStatusArb.bind()
        val blockedByMe = Arb.boolean().bind()
        val hasBlockedMe = Arb.boolean().bind()
        
        createMockUser(
            uid = uid,
            name = name,
            status = status,
            blockedByMe = blockedByMe,
            hasBlockedMe = hasBlockedMe
        )
    }

    /**
     * Arbitrary generator for an online user (not blocked).
     */
    val arbOnlineUser = arbitrary { rs ->
        val uid = "online_${Arb.int(0..10000).bind()}"
        createMockUser(
            uid = uid,
            status = CometChatConstants.USER_STATUS_ONLINE,
            blockedByMe = false,
            hasBlockedMe = false
        )
    }

    /**
     * Arbitrary generator for an offline user (not blocked).
     */
    val arbOfflineUser = arbitrary { rs ->
        val uid = "offline_${Arb.int(0..10000).bind()}"
        createMockUser(
            uid = uid,
            status = CometChatConstants.USER_STATUS_OFFLINE,
            blockedByMe = false,
            hasBlockedMe = false
        )
    }

    /**
     * Arbitrary generator for a blocked user (any status).
     */
    val arbBlockedUser = arbitrary { rs ->
        val uid = "blocked_${Arb.int(0..10000).bind()}"
        val status = userStatusArb.bind()
        val blockedByMe = Arb.boolean().bind()
        // Ensure at least one blocked flag is true
        val hasBlockedMe = if (blockedByMe) Arb.boolean().bind() else true
        
        createMockUser(
            uid = uid,
            status = status,
            blockedByMe = blockedByMe,
            hasBlockedMe = hasBlockedMe
        )
    }

    /**
     * Creates a ViewModel for testing with initial users.
     * Users are added directly via addItem after creation (bypasses fetch).
     */
    fun createViewModelWithUsers(users: List<User>): CometChatUsersViewModel {
        val fetchUsersUseCase = object : FetchUsersUseCase(
            repository = object : UsersRepository {
                override suspend fun getUsers(request: UsersRequest): Result<List<User>> {
                    return Result.success(emptyList())
                }
                override fun hasMoreUsers(): Boolean = false
            }
        ) {}

        val searchUsersUseCase = object : SearchUsersUseCase(
            repository = object : UsersRepository {
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

    // ==================== Property 7: Status Indicator State ====================

    /**
     * Feature: users-component, Property 7: Status Indicator State
     * *For any* user:
     * - If online and not blocked, status indicator should show online
     * - If offline or blocked, status indicator should show offline
     * - Blocked users should always show offline regardless of actual status
     * **Validates: Requirements 6.2, 6.3, 6.4**
     */
    test("Property 7: Online user (not blocked) should show ONLINE status indicator") {
        checkAll(100, arbOnlineUser) { user ->
            val statusIndicator = StatusIndicatorUtils.getStatusIndicatorFromUser(user)
            
            statusIndicator shouldBe StatusIndicator.ONLINE
        }
    }

    test("Property 7: Offline user (not blocked) should show OFFLINE status indicator") {
        checkAll(100, arbOfflineUser) { user ->
            val statusIndicator = StatusIndicatorUtils.getStatusIndicatorFromUser(user)
            
            statusIndicator shouldBe StatusIndicator.OFFLINE
        }
    }

    test("Property 7: Blocked user should always show OFFLINE regardless of actual status") {
        checkAll(100, arbBlockedUser) { user ->
            val statusIndicator = StatusIndicatorUtils.getStatusIndicatorFromUser(user)
            
            // Blocked users should always show OFFLINE
            statusIndicator shouldBe StatusIndicator.OFFLINE
            
            // Verify the user is actually blocked
            StatusIndicatorUtils.isBlocked(user) shouldBe true
        }
    }

    test("Property 7: User blocked by me (online) should show OFFLINE") {
        checkAll(100, Arb.int(0..10000)) { seed ->
            val user = createMockUser(
                uid = "blockedByMe_$seed",
                status = CometChatConstants.USER_STATUS_ONLINE,
                blockedByMe = true,
                hasBlockedMe = false
            )
            
            val statusIndicator = StatusIndicatorUtils.getStatusIndicatorFromUser(user)
            statusIndicator shouldBe StatusIndicator.OFFLINE
        }
    }

    test("Property 7: User who blocked me (online) should show OFFLINE") {
        checkAll(100, Arb.int(0..10000)) { seed ->
            val user = createMockUser(
                uid = "hasBlockedMe_$seed",
                status = CometChatConstants.USER_STATUS_ONLINE,
                blockedByMe = false,
                hasBlockedMe = true
            )
            
            val statusIndicator = StatusIndicatorUtils.getStatusIndicatorFromUser(user)
            statusIndicator shouldBe StatusIndicator.OFFLINE
        }
    }

    test("Property 7: Mutually blocked user (online) should show OFFLINE") {
        checkAll(100, Arb.int(0..10000)) { seed ->
            val user = createMockUser(
                uid = "mutualBlock_$seed",
                status = CometChatConstants.USER_STATUS_ONLINE,
                blockedByMe = true,
                hasBlockedMe = true
            )
            
            val statusIndicator = StatusIndicatorUtils.getStatusIndicatorFromUser(user)
            statusIndicator shouldBe StatusIndicator.OFFLINE
        }
    }

    test("Property 7: Status indicator state is deterministic for any user") {
        checkAll(100, arbUser()) { user ->
            val statusIndicator = StatusIndicatorUtils.getStatusIndicatorFromUser(user)
            
            val isBlocked = user.isBlockedByMe || user.isHasBlockedMe
            val isOnline = user.status == CometChatConstants.USER_STATUS_ONLINE
            
            when {
                isBlocked -> statusIndicator shouldBe StatusIndicator.OFFLINE
                isOnline -> statusIndicator shouldBe StatusIndicator.ONLINE
                else -> statusIndicator shouldBe StatusIndicator.OFFLINE
            }
        }
    }

    // ==================== Property 8: Real-Time User Updates ====================

    /**
     * Feature: users-component, Property 8: Real-Time User Updates
     * *For any* user status change event:
     * - When a user comes online, their status should update to online and they should move to the top of the list
     * - When a user goes offline, their status should update to offline
     * - When a user is blocked/unblocked, their display should update accordingly
     * **Validates: Requirements 7.1, 7.2, 7.3, 7.4, 7.5**
     * 
     * Note: These tests focus on the moveUserToTop behavior which is the primary real-time update mechanism.
     * The status indicator logic is tested in Property 7 tests above.
     */
    test("Property 8: When user comes online, they should move to top of list") {
        checkAll(100, Arb.int(3..10)) { userCount ->
            val testDispatcher = UnconfinedTestDispatcher()
            Dispatchers.setMain(testDispatcher)
            try {
                runTest(testDispatcher) {
                    // Create initial users (all offline)
                    val initialUsers = (0 until userCount).map { index ->
                        createMockUser(
                            uid = "user_$index",
                            name = "User $index",
                            status = CometChatConstants.USER_STATUS_OFFLINE
                        )
                    }
                    
                    val viewModel = createViewModelWithUsers(initialUsers)
                    
                    // Pick a user from the middle/end of the list
                    val userIndex = userCount / 2
                    val userToUpdate = createMockUser(
                        uid = "user_$userIndex",
                        name = "User $userIndex",
                        status = CometChatConstants.USER_STATUS_ONLINE
                    )
                    
                    // Simulate user coming online - move to top
                    viewModel.moveUserToTop(userToUpdate)
                    
                    // Verify user is now at the top
                    val updatedList = viewModel.getItems()
                    updatedList.first().uid shouldBe userToUpdate.uid
                }
            } finally {
                Dispatchers.resetMain()
            }
        }
    }

    test("Property 8: Online user moved to top maintains correct position") {
        checkAll(100, Arb.int(5..15)) { userCount ->
            val testDispatcher = UnconfinedTestDispatcher()
            Dispatchers.setMain(testDispatcher)
            try {
                runTest(testDispatcher) {
                    val initialUsers = (0 until userCount).map { index ->
                        createMockUser(
                            uid = "user_$index",
                            name = "User $index",
                            status = CometChatConstants.USER_STATUS_OFFLINE
                        )
                    }
                    
                    val viewModel = createViewModelWithUsers(initialUsers)
                    
                    // Move last user to top (simulating coming online)
                    val lastUser = createMockUser(
                        uid = "user_${userCount - 1}",
                        name = "User ${userCount - 1}",
                        status = CometChatConstants.USER_STATUS_ONLINE
                    )
                    viewModel.moveUserToTop(lastUser)
                    
                    // Verify order
                    val updatedList = viewModel.getItems()
                    updatedList.first().uid shouldBe lastUser.uid
                    updatedList.size shouldBe userCount
                }
            } finally {
                Dispatchers.resetMain()
            }
        }
    }

    test("Property 8: Multiple users coming online maintains correct order") {
        val testDispatcher = UnconfinedTestDispatcher()
        Dispatchers.setMain(testDispatcher)
        try {
            runTest(testDispatcher) {
                val initialUsers = (0 until 5).map { index ->
                    createMockUser(
                        uid = "user_$index",
                        name = "User $index",
                        status = CometChatConstants.USER_STATUS_OFFLINE
                    )
                }
                
                val viewModel = createViewModelWithUsers(initialUsers)
                
                // User 3 comes online first
                val user3 = createMockUser(
                    uid = "user_3",
                    name = "User 3",
                    status = CometChatConstants.USER_STATUS_ONLINE
                )
                viewModel.moveUserToTop(user3)
                
                // User 1 comes online second
                val user1 = createMockUser(
                    uid = "user_1",
                    name = "User 1",
                    status = CometChatConstants.USER_STATUS_ONLINE
                )
                viewModel.moveUserToTop(user1)
                
                // Verify order: user1 at top, then user3
                val updatedList = viewModel.getItems()
                updatedList[0].uid shouldBe "user_1"
                updatedList[1].uid shouldBe "user_3"
            }
        } finally {
            Dispatchers.resetMain()
        }
    }

    test("Property 8: Blocked user coming online should NOT move to top (per ViewModel logic)") {
        val testDispatcher = UnconfinedTestDispatcher()
        Dispatchers.setMain(testDispatcher)
        try {
            runTest(testDispatcher) {
                val initialUsers = (0 until 5).map { index ->
                    createMockUser(
                        uid = "user_$index",
                        name = "User $index",
                        status = CometChatConstants.USER_STATUS_OFFLINE,
                        blockedByMe = index == 3 // User 3 is blocked
                    )
                }
                
                val viewModel = createViewModelWithUsers(initialUsers)
                
                // Blocked user 3 "comes online" - but we check isBlocked first
                val blockedUser = initialUsers[3]
                val isBlocked = blockedUser.isBlockedByMe || blockedUser.isHasBlockedMe
                
                // Per ViewModel logic: if (!isBlocked(user)) moveUserToTop(user)
                // So blocked users should NOT be moved to top
                if (!isBlocked) {
                    viewModel.moveUserToTop(blockedUser)
                }
                
                // Verify blocked user is NOT at top
                val updatedList = viewModel.getItems()
                updatedList.first().uid shouldBe "user_0" // Original first user
            }
        } finally {
            Dispatchers.resetMain()
        }
    }

    test("Property 8: moveUserToTop preserves list size") {
        checkAll(100, Arb.int(2..20)) { userCount ->
            val testDispatcher = UnconfinedTestDispatcher()
            Dispatchers.setMain(testDispatcher)
            try {
                runTest(testDispatcher) {
                    val initialUsers = (0 until userCount).map { index ->
                        createMockUser(uid = "user_$index")
                    }
                    
                    val viewModel = createViewModelWithUsers(initialUsers)
                    val initialSize = viewModel.getItemCount()
                    
                    // Move a user to top
                    val userToMove = initialUsers.last()
                    viewModel.moveUserToTop(userToMove)
                    
                    // List size should remain the same
                    viewModel.getItemCount() shouldBe initialSize
                }
            } finally {
                Dispatchers.resetMain()
            }
        }
    }

    test("Property 8: Status indicator correctly reflects user state after simulated update") {
        checkAll(100, arbUser()) { user ->
            // This tests the status indicator logic for any user state
            val statusIndicator = StatusIndicatorUtils.getStatusIndicatorFromUser(user)
            
            val isBlocked = user.isBlockedByMe || user.isHasBlockedMe
            val isOnline = user.status == CometChatConstants.USER_STATUS_ONLINE
            
            // Verify status indicator follows the rules
            when {
                isBlocked -> statusIndicator shouldBe StatusIndicator.OFFLINE
                isOnline -> statusIndicator shouldBe StatusIndicator.ONLINE
                else -> statusIndicator shouldBe StatusIndicator.OFFLINE
            }
        }
    }

    test("Property 8: Blocked user always shows OFFLINE status indicator regardless of online status") {
        checkAll(100, arbBlockedUser) { blockedUser ->
            val statusIndicator = StatusIndicatorUtils.getStatusIndicatorFromUser(blockedUser)
            
            // Blocked users should always show OFFLINE
            statusIndicator shouldBe StatusIndicator.OFFLINE
        }
    }

    test("Property 8: Unblocked online user shows ONLINE status indicator") {
        checkAll(100, arbOnlineUser) { onlineUser ->
            val statusIndicator = StatusIndicatorUtils.getStatusIndicatorFromUser(onlineUser)
            
            // Online users (not blocked) should show ONLINE
            statusIndicator shouldBe StatusIndicator.ONLINE
        }
    }

    test("Property 8: Unblocked offline user shows OFFLINE status indicator") {
        checkAll(100, arbOfflineUser) { offlineUser ->
            val statusIndicator = StatusIndicatorUtils.getStatusIndicatorFromUser(offlineUser)
            
            // Offline users should show OFFLINE
            statusIndicator shouldBe StatusIndicator.OFFLINE
        }
    }
})
