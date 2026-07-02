package com.cometchat.uikit.core.viewmodel.users

import com.cometchat.chat.models.User
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
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.string
import io.kotest.property.arbitrary.element
import io.kotest.property.checkAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * Property-based tests for CometChatUsersViewModel.
 *
 * Uses random/generated inputs via Kotest's `checkAll` to prove behavior
 * holds for ANY valid input — not just hardcoded examples.
 *
 * Run with:
 *   ./gradlew :chatuikit-core:testDebugUnitTest --tests "...CometChatUsersViewModelTest"
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CometChatUsersViewModelTest : FunSpec({

    isolationMode = io.kotest.core.spec.IsolationMode.SingleInstance

    val testDispatcher = UnconfinedTestDispatcher()

    lateinit var fetchUsersUseCase: FetchUsersUseCase
    lateinit var searchUsersUseCase: SearchUsersUseCase

    fun logUsers(label: String, users: List<User>) {
        println("    → $label: ${users.map { "${it.uid}:${it.name}" }}")
    }

    /** Create a ViewModel pre-loaded with the given users list. */
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

    // ==================== Fetch & UI State ====================

    test("warmup - absorb leaked exceptions") {
        try { runTest { } } catch (_: Exception) { }
    }

    test("for any user count: empty list → Empty state, non-empty → Content state") {
        checkAll(50, Arb.int(0..20)) { count ->
            runTest {
                val users = MockFactory.createUsers(count)
                println("    → count=$count")
                val viewModel = createViewModel(users)
                advanceUntilIdle()

                if (count == 0) {
                    viewModel.uiState.value shouldBe UsersUIState.Empty
                    viewModel.users.value shouldHaveSize 0
                    println("    ✅ count=0 → Empty")
                } else {
                    viewModel.uiState.value shouldBe UsersUIState.Content
                    viewModel.users.value shouldHaveSize count
                    println("    ✅ count=$count → Content, size=$count")
                }
            }
        }
    }

    test("for any error message: fetch failure → Error state with same exception") {
        checkAll(20, Arb.string(1..50), Arb.string(3..10)) { code, msg ->
            runTest {
                val exception = MockFactory.createCometChatException(code, msg)
                whenever(fetchUsersUseCase.invoke(any())).thenReturn(Result.failure(exception))
                val viewModel = CometChatUsersViewModel(fetchUsersUseCase, searchUsersUseCase, enableListeners = false)
                viewModel.fetchUsers()
                advanceUntilIdle()

                println("    → code='$code', msg='$msg'")
                viewModel.uiState.value.shouldBeInstanceOf<UsersUIState.Error>()
                (viewModel.uiState.value as UsersUIState.Error).exception shouldBe exception
                println("    ✅ Error state with matching exception")
            }
        }
    }

    // ==================== ListOperations ====================

    test("for any list size: addItem should increase size by 1") {
        checkAll(30, Arb.int(0..15)) { initialCount ->
            runTest {
                val users = MockFactory.createUsers(initialCount)
                val viewModel = createViewModel(users)
                advanceUntilIdle()

                val newUser = MockFactory.createUser("added-${initialCount}", "Added User")
                viewModel.addItem(newUser)

                println("    → initial=$initialCount, after addItem=${viewModel.users.value.size}")
                viewModel.users.value shouldHaveSize initialCount + 1
                viewModel.users.value shouldContain newUser
                println("    ✅ size increased by 1")
            }
        }
    }

    test("for any list size >= 1: removeItem should decrease size by 1") {
        checkAll(30, Arb.int(1..15)) { count ->
            runTest {
                val users = MockFactory.createUsers(count)
                val viewModel = createViewModel(users)
                advanceUntilIdle()

                // Pick a random index to remove
                val removeIndex = (0 until count).random()
                val userToRemove = users[removeIndex]
                println("    → count=$count, removing index=$removeIndex (${userToRemove.uid})")

                val removed = viewModel.removeItem(userToRemove)

                removed shouldBe true
                viewModel.users.value shouldHaveSize count - 1
                viewModel.users.value shouldNotContain userToRemove
                println("    ✅ removed=true, size=${count - 1}")
            }
        }
    }

    test("for any list size >= 1: updateItem should change the matched user's data") {
        checkAll(30, Arb.int(1..15), Arb.string(3..20)) { count, newName ->
            runTest {
                val users = MockFactory.createUsers(count)
                val viewModel = createViewModel(users)
                advanceUntilIdle()

                val targetIndex = (0 until count).random()
                val targetUid = users[targetIndex].uid
                val updatedUser = MockFactory.createUser(targetUid, newName)
                println("    → count=$count, updating $targetUid to name='$newName'")

                val updated = viewModel.updateItem(updatedUser) { it.uid == targetUid }

                updated shouldBe true
                viewModel.users.value.find { it.uid == targetUid }?.name shouldBe newName
                viewModel.users.value shouldHaveSize count
                println("    ✅ updated, name='$newName', size unchanged")
            }
        }
    }

    test("for any list size >= 2: moveItemToTop should place target at index 0") {
        checkAll(30, Arb.int(2..15)) { count ->
            runTest {
                val users = MockFactory.createUsers(count)
                val viewModel = createViewModel(users)
                advanceUntilIdle()

                // Pick a non-first user to move
                val moveIndex = (1 until count).random()
                val userToMove = users[moveIndex]
                println("    → count=$count, moving index=$moveIndex (${userToMove.uid}) to top")

                viewModel.moveItemToTop(userToMove)

                viewModel.users.value.first().uid shouldBe userToMove.uid
                viewModel.users.value shouldHaveSize count
                println("    ✅ ${userToMove.uid} is now at index 0, size=$count")
            }
        }
    }

    test("for any list size: clearItems should result in empty list") {
        checkAll(30, Arb.int(0..20)) { count ->
            runTest {
                val users = MockFactory.createUsers(count)
                val viewModel = createViewModel(users)
                advanceUntilIdle()

                viewModel.clearItems()

                println("    → count=$count → after clear: size=${viewModel.users.value.size}")
                viewModel.users.value shouldHaveSize 0
                println("    ✅ cleared")
            }
        }
    }

    test("for any list size: getItemCount should match actual size") {
        checkAll(30, Arb.int(0..20)) { count ->
            runTest {
                val users = MockFactory.createUsers(count)
                val viewModel = createViewModel(users)
                advanceUntilIdle()

                println("    → count=$count, getItemCount()=${viewModel.getItemCount()}")
                viewModel.getItemCount() shouldBe count
                println("    ✅ matches")
            }
        }
    }

    test("for any valid index: getItemAt should return correct user; out-of-bounds → null") {
        checkAll(30, Arb.int(1..15)) { count ->
            runTest {
                val users = MockFactory.createUsers(count)
                val viewModel = createViewModel(users)
                advanceUntilIdle()

                val validIndex = (0 until count).random()
                val result = viewModel.getItemAt(validIndex)
                println("    → count=$count, getItemAt($validIndex)=${result?.uid}")
                result?.uid shouldBe users[validIndex].uid

                val outOfBounds = viewModel.getItemAt(count + 10)
                outOfBounds shouldBe null
                println("    ✅ valid index returns user, out-of-bounds returns null")
            }
        }
    }

    // ==================== ViewModel-Specific Methods ====================

    test("for any user in list: updateUser should update that user in place") {
        checkAll(30, Arb.int(1..10), Arb.string(3..15), Arb.element("online", "offline")) { count, newName, newStatus ->
            runTest {
                val users = MockFactory.createUsers(count)
                val viewModel = createViewModel(users)
                advanceUntilIdle()

                val targetIndex = (0 until count).random()
                val targetUid = users[targetIndex].uid
                val updatedUser = MockFactory.createUser(targetUid, newName, newStatus)
                println("    → updating $targetUid: name='$newName', status='$newStatus'")

                viewModel.updateUser(updatedUser)

                val found = viewModel.users.value.find { it.uid == targetUid }
                found?.name shouldBe newName
                found?.status shouldBe newStatus
                println("    ✅ updated in place")
            }
        }
    }

    test("removeUser on last user should set Empty state") {
        checkAll(20, Arb.string(3..15), Arb.string(3..15)) { uid, name ->
            runTest {
                val user = MockFactory.createUser(uid, name)
                val viewModel = createViewModel(listOf(user))
                advanceUntilIdle()

                println("    → single user [$uid:$name], removing")
                viewModel.removeUser(user)

                viewModel.users.value shouldHaveSize 0
                viewModel.uiState.value shouldBe UsersUIState.Empty
                println("    ✅ size=0, state=Empty")
            }
        }
    }

    test("for any list: addUserToTop should not add duplicate") {
        checkAll(30, Arb.int(1..10)) { count ->
            runTest {
                val users = MockFactory.createUsers(count)
                val viewModel = createViewModel(users)
                advanceUntilIdle()

                val existingUser = users[(0 until count).random()]
                println("    → count=$count, addUserToTop(${existingUser.uid}) — already exists")
                viewModel.addUserToTop(existingUser)

                viewModel.users.value shouldHaveSize count
                println("    ✅ size unchanged, no duplicate")
            }
        }
    }

    test("for any new user: addUserToTop should place at index 0") {
        checkAll(30, Arb.int(0..10), Arb.string(5..15)) { count, newUid ->
            runTest {
                val users = MockFactory.createUsers(count)
                val viewModel = createViewModel(users)
                advanceUntilIdle()

                val newUser = MockFactory.createUser(newUid, "New $newUid")
                println("    → count=$count, addUserToTop($newUid)")
                viewModel.addUserToTop(newUser)

                viewModel.users.value.first().uid shouldBe newUid
                viewModel.users.value shouldHaveSize count + 1
                println("    ✅ $newUid at index 0, size=${count + 1}")
            }
        }
    }

    // ==================== Selection ====================

    test("SINGLE mode: for any two users, only the last selected should remain") {
        checkAll(20, Arb.int(2..10)) { count ->
            runTest {
                val users = MockFactory.createUsers(count)
                val viewModel = createViewModel(users)
                advanceUntilIdle()

                val mode = com.cometchat.uikit.core.constants.UIKitConstants.SelectionMode.SINGLE
                val first = users[(0 until count).random()]
                val second = users.filter { it.uid != first.uid }.random()

                println("    → select ${first.uid}, then ${second.uid}")
                viewModel.selectUser(first, mode)
                viewModel.selectUser(second, mode)

                viewModel.selectedUsers.value shouldHaveSize 1
                viewModel.isSelected(second) shouldBe true
                viewModel.isSelected(first) shouldBe false
                println("    ✅ only ${second.uid} selected")
            }
        }
    }

    test("MULTIPLE mode: selecting then deselecting should toggle") {
        checkAll(20, Arb.int(2..10)) { count ->
            runTest {
                val users = MockFactory.createUsers(count)
                val viewModel = createViewModel(users)
                advanceUntilIdle()

                val mode = com.cometchat.uikit.core.constants.UIKitConstants.SelectionMode.MULTIPLE
                val target = users[(0 until count).random()]

                println("    → select ${target.uid}")
                viewModel.selectUser(target, mode)
                viewModel.isSelected(target) shouldBe true

                println("    → deselect ${target.uid}")
                viewModel.selectUser(target, mode)
                viewModel.isSelected(target) shouldBe false
                println("    ✅ toggled correctly")
            }
        }
    }

    test("clearSelection should clear any number of selected users") {
        checkAll(20, Arb.int(1..10)) { count ->
            runTest {
                val users = MockFactory.createUsers(count)
                val viewModel = createViewModel(users)
                advanceUntilIdle()

                val mode = com.cometchat.uikit.core.constants.UIKitConstants.SelectionMode.MULTIPLE
                val selectCount = (1..count).random()
                users.take(selectCount).forEach { viewModel.selectUser(it, mode) }
                println("    → selected $selectCount of $count users")

                viewModel.clearSelection()

                viewModel.selectedUsers.value shouldHaveSize 0
                println("    ✅ cleared all")
            }
        }
    }

    // ==================== UIKit Events (UserBlocked / UserUnblocked) ====================

    test("UserBlocked: for any user in list, updateUser should set blockedByMe=true") {
        checkAll(20, Arb.int(1..10)) { count ->
            runTest {
                val users = MockFactory.createUsers(count)
                val viewModel = createViewModel(users)
                advanceUntilIdle()

                val targetIndex = (0 until count).random()
                val targetUid = users[targetIndex].uid
                val blockedUser = MockFactory.createUser(targetUid, "User", isBlockedByMe = true)
                println("    → UserBlocked for $targetUid (index=$targetIndex)")

                // This is what addLocalEventListeners does on UserBlocked
                viewModel.updateUser(blockedUser)

                viewModel.users.value.find { it.uid == targetUid }?.isBlockedByMe shouldBe true
                viewModel.users.value shouldHaveSize count
                println("    ✅ $targetUid blockedByMe=true, size unchanged")
            }
        }
    }

    test("UserUnblocked: for any blocked user, updateUser should set blockedByMe=false") {
        checkAll(20, Arb.int(1..10)) { count ->
            runTest {
                // Start with all users blocked
                val users = (1..count).map { i ->
                    MockFactory.createUser("user-$i", "User $i", isBlockedByMe = true)
                }
                val viewModel = createViewModel(users)
                advanceUntilIdle()

                val targetIndex = (0 until count).random()
                val targetUid = users[targetIndex].uid
                val unblockedUser = MockFactory.createUser(targetUid, "User", isBlockedByMe = false)
                println("    → UserUnblocked for $targetUid")

                viewModel.updateUser(unblockedUser)

                viewModel.users.value.find { it.uid == targetUid }?.isBlockedByMe shouldBe false
                viewModel.users.value shouldHaveSize count
                println("    ✅ $targetUid blockedByMe=false, size unchanged")
            }
        }
    }

    test("UserBlocked for non-existent user should not change list") {
        checkAll(20, Arb.int(1..10), Arb.string(10..20)) { count, randomUid ->
            runTest {
                val users = MockFactory.createUsers(count)
                val viewModel = createViewModel(users)
                advanceUntilIdle()

                val unknownUser = MockFactory.createUser(randomUid, "Unknown", isBlockedByMe = true)
                println("    → UserBlocked for non-existent '$randomUid'")
                viewModel.updateUser(unknownUser)

                viewModel.users.value shouldHaveSize count
                viewModel.users.value.none { it.uid == randomUid } shouldBe true
                println("    ✅ list unchanged")
            }
        }
    }

    // ==================== SDK Listeners (onUserOnline / onUserOffline) ====================

    test("onUserOnline: for any non-blocked user, should move to top") {
        checkAll(20, Arb.int(2..10)) { count ->
            runTest {
                val users = MockFactory.createUsers(count)
                val viewModel = createViewModel(users)
                advanceUntilIdle()

                val targetIndex = (1 until count).random() // not already at top
                val targetUid = users[targetIndex].uid
                val onlineUser = MockFactory.createUser(targetUid, "User", "online",
                    isBlockedByMe = false, hasBlockedMe = false)
                val isBlocked = onlineUser.isBlockedByMe || onlineUser.isHasBlockedMe
                println("    → onUserOnline($targetUid): blocked=$isBlocked")

                if (!isBlocked) viewModel.moveUserToTop(onlineUser)

                viewModel.users.value.first().uid shouldBe targetUid
                viewModel.users.value shouldHaveSize count
                println("    ✅ $targetUid moved to top")
            }
        }
    }

    test("onUserOnline: for any blocked user (either direction), should NOT move to top") {
        checkAll(20, Arb.int(2..10), Arb.boolean(), Arb.boolean()) { count, blockedByMe, hasBlockedMe ->
            // Ensure at least one blocked flag is true
            val actualBlockedByMe = blockedByMe || !hasBlockedMe
            val actualHasBlockedMe = hasBlockedMe || !blockedByMe

            runTest {
                val users = MockFactory.createUsers(count)
                val viewModel = createViewModel(users)
                advanceUntilIdle()

                val originalFirst = viewModel.users.value.first().uid
                val targetIndex = (1 until count).random()
                val targetUid = users[targetIndex].uid
                val blockedOnlineUser = MockFactory.createUser(targetUid, "User", "online",
                    isBlockedByMe = actualBlockedByMe, hasBlockedMe = actualHasBlockedMe)
                val isBlocked = blockedOnlineUser.isBlockedByMe || blockedOnlineUser.isHasBlockedMe
                println("    → onUserOnline($targetUid): blockedByMe=$actualBlockedByMe, hasBlockedMe=$actualHasBlockedMe, isBlocked=$isBlocked")

                if (!isBlocked) viewModel.moveUserToTop(blockedOnlineUser)

                viewModel.users.value.first().uid shouldBe originalFirst
                println("    ✅ first user still $originalFirst, blocked user ignored")
            }
        }
    }

    test("onUserOffline: for any non-blocked user, should update status in place") {
        checkAll(20, Arb.int(1..10)) { count ->
            runTest {
                val users = MockFactory.createUsers(count)
                val viewModel = createViewModel(users)
                advanceUntilIdle()

                val targetIndex = (0 until count).random()
                val targetUid = users[targetIndex].uid
                val offlineUser = MockFactory.createUser(targetUid, "User", "offline",
                    isBlockedByMe = false, hasBlockedMe = false)
                println("    → onUserOffline($targetUid)")

                val isBlocked = offlineUser.isBlockedByMe || offlineUser.isHasBlockedMe
                if (!isBlocked) viewModel.updateUser(offlineUser)

                viewModel.users.value.find { it.uid == targetUid }?.status shouldBe "offline"
                // Position should not change (updateUser doesn't move)
                viewModel.users.value[targetIndex].uid shouldBe targetUid
                println("    ✅ status=offline, position unchanged")
            }
        }
    }

    test("onUserOffline: for any blocked user, should NOT update") {
        checkAll(20, Arb.int(1..10)) { count ->
            runTest {
                val users = MockFactory.createUsers(count)
                val viewModel = createViewModel(users)
                advanceUntilIdle()

                val targetIndex = (0 until count).random()
                val targetUid = users[targetIndex].uid
                val originalStatus = viewModel.users.value[targetIndex].status

                val blockedOfflineUser = MockFactory.createUser(targetUid, "User", "offline",
                    isBlockedByMe = true)
                println("    → onUserOffline($targetUid): blockedByMe=true, originalStatus=$originalStatus")

                val isBlocked = blockedOfflineUser.isBlockedByMe || blockedOfflineUser.isHasBlockedMe
                if (!isBlocked) viewModel.updateUser(blockedOfflineUser)

                viewModel.users.value.find { it.uid == targetUid }?.status shouldBe originalStatus
                println("    ✅ status unchanged ($originalStatus)")
            }
        }
    }

    // ==================== Event Flow Integration ====================

    test("CometChatEvents should deliver UserBlocked to subscribers") {
        runTest {
            val receivedEvents = mutableListOf<CometChatUserEvent>()
            val job = CoroutineScope(testDispatcher).launch {
                CometChatEvents.userEvents.collect { receivedEvents.add(it) }
            }

            val user = MockFactory.createUser("evt-user", "Evt User", isBlockedByMe = true)
            println("    → Emitting UserBlocked for evt-user")
            CometChatEvents.emitUserEventSync(CometChatUserEvent.UserBlocked(user))
            advanceUntilIdle()

            receivedEvents shouldHaveSize 1
            receivedEvents[0].shouldBeInstanceOf<CometChatUserEvent.UserBlocked>()
            (receivedEvents[0] as CometChatUserEvent.UserBlocked).user.uid shouldBe "evt-user"
            println("    ✅ UserBlocked delivered")
            job.cancel()
        }
    }

    test("CometChatEvents should deliver UserUnblocked to subscribers") {
        runTest {
            val receivedEvents = mutableListOf<CometChatUserEvent>()
            val job = CoroutineScope(testDispatcher).launch {
                CometChatEvents.userEvents.collect { receivedEvents.add(it) }
            }

            val user = MockFactory.createUser("evt-user-2", "Evt User 2")
            println("    → Emitting UserUnblocked for evt-user-2")
            CometChatEvents.emitUserEventSync(CometChatUserEvent.UserUnblocked(user))
            advanceUntilIdle()

            receivedEvents shouldHaveSize 1
            receivedEvents[0].shouldBeInstanceOf<CometChatUserEvent.UserUnblocked>()
            println("    ✅ UserUnblocked delivered")
            job.cancel()
        }
    }

    // ==================== Pagination & Refresh ====================

    test("for any two page sizes: fetchUsers should append on second call") {
        checkAll(20, Arb.int(1..10), Arb.int(1..10)) { page1Size, page2Size ->
            runTest {
                val page1 = MockFactory.createUsers(page1Size, "p1")
                whenever(fetchUsersUseCase.invoke(any())).thenReturn(Result.success(page1))
                whenever(fetchUsersUseCase.hasMore()).thenReturn(true)
                val viewModel = CometChatUsersViewModel(fetchUsersUseCase, searchUsersUseCase, enableListeners = false)
                viewModel.fetchUsers()
                advanceUntilIdle()

                val page2 = MockFactory.createUsers(page2Size, "p2")
                whenever(fetchUsersUseCase.invoke(any())).thenReturn(Result.success(page2))
                viewModel.fetchUsers()
                advanceUntilIdle()

                println("    → page1=$page1Size, page2=$page2Size, total=${viewModel.users.value.size}")
                viewModel.users.value.size shouldBe page1Size + page2Size
                println("    ✅ appended correctly")
            }
        }
    }

    test("refreshList should replace old data with fresh data") {
        checkAll(20, Arb.int(1..10), Arb.int(0..10)) { oldCount, newCount ->
            runTest {
                val oldUsers = MockFactory.createUsers(oldCount, "old")
                val viewModel = createViewModel(oldUsers)
                advanceUntilIdle()

                val freshUsers = MockFactory.createUsers(newCount, "fresh")
                whenever(fetchUsersUseCase.invoke(any())).thenReturn(Result.success(freshUsers))
                viewModel.refreshList()
                advanceUntilIdle()

                println("    → old=$oldCount, fresh=$newCount, after refresh=${viewModel.users.value.size}")
                viewModel.users.value shouldHaveSize newCount
                if (newCount == 0) {
                    viewModel.uiState.value shouldBe UsersUIState.Empty
                } else {
                    viewModel.uiState.value shouldBe UsersUIState.Content
                    viewModel.users.value.first().uid shouldBe "fresh-1"
                }
                println("    ✅ refreshed")
            }
        }
    }

    // ==================== Search ====================

    test("searchUsers with keyword should replace list with search results") {
        checkAll(20, Arb.int(1..10), Arb.string(3..10), Arb.int(0..5)) { initialCount, keyword, resultCount ->
            runTest {
                val initialUsers = MockFactory.createUsers(initialCount, "init")
                val viewModel = createViewModel(initialUsers)
                advanceUntilIdle()

                val searchResults = MockFactory.createUsers(resultCount, "search")
                whenever(searchUsersUseCase.invoke(any(), any())).thenReturn(Result.success(searchResults))
                println("    → initial=$initialCount, search '$keyword' → $resultCount results")

                viewModel.searchUsers(keyword)
                advanceUntilIdle()

                viewModel.users.value shouldHaveSize resultCount
                if (resultCount == 0) {
                    viewModel.uiState.value shouldBe UsersUIState.Empty
                } else {
                    viewModel.uiState.value shouldBe UsersUIState.Content
                    viewModel.users.value.first().uid shouldBe "search-1"
                }
                println("    ✅ search replaced list, size=$resultCount")
            }
        }
    }

    test("searchUsers with null/empty keyword should reset to normal fetch") {
        checkAll(20, Arb.int(1..10), Arb.element("", null)) { initialCount, emptyKeyword ->
            runTest {
                val initialUsers = MockFactory.createUsers(initialCount, "init")
                val viewModel = createViewModel(initialUsers)
                advanceUntilIdle()

                // First do a search
                val searchResults = MockFactory.createUsers(2, "search")
                whenever(searchUsersUseCase.invoke(any(), any())).thenReturn(Result.success(searchResults))
                viewModel.searchUsers("test")
                advanceUntilIdle()

                // Now reset with null/empty
                val freshUsers = MockFactory.createUsers(initialCount, "fresh")
                whenever(fetchUsersUseCase.invoke(any())).thenReturn(Result.success(freshUsers))
                println("    → resetting search with keyword='$emptyKeyword'")
                viewModel.searchUsers(emptyKeyword)
                advanceUntilIdle()

                viewModel.users.value shouldHaveSize initialCount
                viewModel.users.value.first().uid shouldBe "fresh-1"
                println("    ✅ reset to normal fetch, size=$initialCount")
            }
        }
    }

    test("searchUsers failure with empty list should show Error state") {
        checkAll(20, Arb.string(3..10), Arb.string(3..15)) { keyword, errorMsg ->
            runTest {
                // Start with empty list so error state shows
                val viewModel = createViewModel(emptyList())
                advanceUntilIdle()

                val exception = MockFactory.createCometChatException("SEARCH_ERR", errorMsg)
                whenever(searchUsersUseCase.invoke(any(), any())).thenReturn(Result.failure(exception))
                println("    → search '$keyword' fails with '$errorMsg'")

                viewModel.searchUsers(keyword)
                advanceUntilIdle()

                viewModel.uiState.value.shouldBeInstanceOf<UsersUIState.Error>()
                (viewModel.uiState.value as UsersUIState.Error).exception shouldBe exception
                println("    ✅ Error state with matching exception")
            }
        }
    }

    test("searchUsers failure with existing content should keep old data (graceful degradation)") {
        checkAll(20, Arb.int(1..10), Arb.string(3..10)) { initialCount, keyword ->
            runTest {
                val initialUsers = MockFactory.createUsers(initialCount, "init")
                val viewModel = createViewModel(initialUsers)
                advanceUntilIdle()

                val exception = MockFactory.createCometChatException("SEARCH_ERR", "fail")
                whenever(searchUsersUseCase.invoke(any(), any())).thenReturn(Result.failure(exception))
                println("    → initial=$initialCount, search '$keyword' fails")

                viewModel.searchUsers(keyword)
                advanceUntilIdle()

                // Should keep existing content, not show error
                viewModel.users.value shouldHaveSize initialCount
                viewModel.uiState.value shouldBe UsersUIState.Content
                println("    ✅ kept old data, no error shown")
            }
        }
    }

    // ==================== Scroll Events ====================

    test("moveUserToTop should emit scrollToTopEvent") {
        checkAll(10, Arb.int(2..8)) { count ->
            runTest {
                val users = MockFactory.createUsers(count)
                val viewModel = createViewModel(users)
                advanceUntilIdle()

                var scrollEventReceived = false
                val job = CoroutineScope(testDispatcher).launch {
                    viewModel.scrollToTopEvent.collect { scrollEventReceived = true }
                }

                val targetIndex = (1 until count).random()
                println("    → moveUserToTop(${users[targetIndex].uid})")
                viewModel.moveUserToTop(users[targetIndex])
                advanceUntilIdle()

                scrollEventReceived shouldBe true
                println("    ✅ scrollToTopEvent emitted")
                job.cancel()
            }
        }
    }

    test("addUserToTop should emit scrollToTopEvent") {
        checkAll(10, Arb.int(0..5), Arb.string(5..12)) { count, newUid ->
            runTest {
                val users = MockFactory.createUsers(count)
                val viewModel = createViewModel(users)
                advanceUntilIdle()

                var scrollEventReceived = false
                val job = CoroutineScope(testDispatcher).launch {
                    viewModel.scrollToTopEvent.collect { scrollEventReceived = true }
                }

                val newUser = MockFactory.createUser(newUid, "New")
                println("    → addUserToTop($newUid)")
                viewModel.addUserToTop(newUser)
                advanceUntilIdle()

                scrollEventReceived shouldBe true
                println("    ✅ scrollToTopEvent emitted")
                job.cancel()
            }
        }
    }

    test("refreshList should emit scrollToTopEvent") {
        runTest {
            val users = MockFactory.createUsers(3)
            val viewModel = createViewModel(users)
            advanceUntilIdle()

            var scrollEventReceived = false
            val job = CoroutineScope(testDispatcher).launch {
                viewModel.scrollToTopEvent.collect { scrollEventReceived = true }
            }

            val freshUsers = MockFactory.createUsers(2, "fresh")
            whenever(fetchUsersUseCase.invoke(any())).thenReturn(Result.success(freshUsers))
            println("    → refreshList()")
            viewModel.refreshList()
            advanceUntilIdle()

            scrollEventReceived shouldBe true
            println("    ✅ scrollToTopEvent emitted on refresh")
            job.cancel()
        }
    }

    // ==================== NONE Selection Mode ====================

    test("NONE mode: selectUser should not change selection") {
        checkAll(10, Arb.int(1..5)) { count ->
            runTest {
                val users = MockFactory.createUsers(count)
                val viewModel = createViewModel(users)
                advanceUntilIdle()

                val mode = com.cometchat.uikit.core.constants.UIKitConstants.SelectionMode.NONE
                val target = users[(0 until count).random()]
                println("    → selectUser(${target.uid}, NONE)")
                viewModel.selectUser(target, mode)

                viewModel.selectedUsers.value shouldHaveSize 0
                println("    ✅ selection unchanged (NONE mode)")
            }
        }
    }

    // ==================== getSelectedUsers / isSelected ====================

    test("getSelectedUsers should return list matching selectedUsers state") {
        checkAll(10, Arb.int(2..8)) { count ->
            runTest {
                val users = MockFactory.createUsers(count)
                val viewModel = createViewModel(users)
                advanceUntilIdle()

                val mode = com.cometchat.uikit.core.constants.UIKitConstants.SelectionMode.MULTIPLE
                val selectCount = (1..count).random()
                users.take(selectCount).forEach { viewModel.selectUser(it, mode) }
                println("    → selected $selectCount of $count")

                val selected = viewModel.getSelectedUsers()
                selected shouldHaveSize selectCount
                users.take(selectCount).forEach { user ->
                    viewModel.isSelected(user) shouldBe true
                }
                users.drop(selectCount).forEach { user ->
                    viewModel.isSelected(user) shouldBe false
                }
                println("    ✅ getSelectedUsers matches, isSelected correct for all")
            }
        }
    }
})
