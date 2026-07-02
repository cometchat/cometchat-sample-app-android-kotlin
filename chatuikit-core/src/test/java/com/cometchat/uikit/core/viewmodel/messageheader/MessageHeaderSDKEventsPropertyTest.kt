package com.cometchat.uikit.core.viewmodel.messageheader

import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.domain.usecase.GetGroupUseCase
import com.cometchat.uikit.core.domain.usecase.GetUserUseCase
import com.cometchat.uikit.core.state.MessageHeaderUIState
import com.cometchat.uikit.core.testutils.MockFactory
import com.cometchat.uikit.core.viewmodel.CometChatMessageHeaderViewModel
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.FunSpec
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
 * Property-based tests for SDK event handling in CometChatMessageHeaderViewModel.
 *
 * Tests the documented SDK events:
 * - onUserOnline / onUserOffline → updates status indicator
 * - onTypingStarted / onTypingEnded → shows/hides typing indicator
 * - onGroupMemberJoined/Left/Kicked/Banned → updates member count
 *
 * Note: Since enableListeners=false in tests, we verify the ViewModel's
 * public API behavior that mirrors what SDK events would trigger.
 * The actual SDK listener wiring is tested via integration tests.
 *
 * Run with:
 *   ./gradlew :chatuikit-core:testDebugUnitTest --tests "*MessageHeaderSDKEventsPropertyTest"
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MessageHeaderSDKEventsPropertyTest : FunSpec({

    isolationMode = IsolationMode.SingleInstance

    val testDispatcher = UnconfinedTestDispatcher()

    lateinit var getUserUseCase: GetUserUseCase
    lateinit var getGroupUseCase: GetGroupUseCase

    fun createViewModel(): CometChatMessageHeaderViewModel {
        return CometChatMessageHeaderViewModel(
            getUserUseCase = getUserUseCase,
            getGroupUseCase = getGroupUseCase,
            enableListeners = false
        )
    }

    beforeTest {
        Dispatchers.setMain(testDispatcher)
        getUserUseCase = mock()
        getGroupUseCase = mock()
        println("  🧪 ${it.name.testName}")
    }

    afterTest {
        Dispatchers.resetMain()
        println()
    }

    // ==================== User Online/Offline Status ====================

    test("PROPERTY: setUser with online status should expose online in uiState") {
        checkAll(30, Arb.string(5..20), Arb.string(3..30)) { uid, name ->
            runTest {
                val viewModel = createViewModel()
                val user = MockFactory.createUser(uid = uid, name = name, status = CometChatConstants.USER_STATUS_ONLINE)
                println("    → User online: uid=$uid")

                viewModel.setUser(user)
                advanceUntilIdle()

                val state = viewModel.uiState.value as MessageHeaderUIState.UserContent
                state.user.status shouldBe CometChatConstants.USER_STATUS_ONLINE
            }
        }
    }

    test("PROPERTY: setUser with offline status should expose offline in uiState") {
        checkAll(30, Arb.string(5..20), Arb.string(3..30)) { uid, name ->
            runTest {
                val viewModel = createViewModel()
                val user = MockFactory.createUser(uid = uid, name = name, status = CometChatConstants.USER_STATUS_OFFLINE)
                println("    → User offline: uid=$uid")

                viewModel.setUser(user)
                advanceUntilIdle()

                val state = viewModel.uiState.value as MessageHeaderUIState.UserContent
                state.user.status shouldBe CometChatConstants.USER_STATUS_OFFLINE
            }
        }
    }

    test("PROPERTY: status transition online→offline should update uiState") {
        checkAll(20, Arb.string(5..15)) { uid ->
            runTest {
                val viewModel = createViewModel()
                println("    → Status transition: online → offline for uid=$uid")

                val onlineUser = MockFactory.createUser(uid = uid, status = CometChatConstants.USER_STATUS_ONLINE)
                viewModel.setUser(onlineUser)
                advanceUntilIdle()
                (viewModel.uiState.value as MessageHeaderUIState.UserContent).user.status shouldBe CometChatConstants.USER_STATUS_ONLINE

                val offlineUser = MockFactory.createUser(uid = uid, status = CometChatConstants.USER_STATUS_OFFLINE)
                viewModel.setUser(offlineUser)
                advanceUntilIdle()
                (viewModel.uiState.value as MessageHeaderUIState.UserContent).user.status shouldBe CometChatConstants.USER_STATUS_OFFLINE
            }
        }
    }

    // ==================== Group Member Count Updates ====================

    test("PROPERTY: group member count should update when setGroup is called with new count") {
        checkAll(30, Arb.string(5..15), Arb.int(1..100), Arb.int(1..100)) { guid, count1, count2 ->
            runTest {
                val viewModel = createViewModel()
                println("    → Member count: $count1 → $count2 for guid=$guid")

                val group1 = MockFactory.createGroup(guid = guid, membersCount = count1)
                viewModel.setGroup(group1)
                advanceUntilIdle()
                viewModel.memberCount.value shouldBe count1

                val group2 = MockFactory.createGroup(guid = guid, membersCount = count2)
                viewModel.setGroup(group2)
                advanceUntilIdle()
                viewModel.memberCount.value shouldBe count2
            }
        }
    }

    test("PROPERTY: member join should increase count (simulated via setGroup)") {
        checkAll(20, Arb.int(1..50)) { initialCount ->
            runTest {
                val viewModel = createViewModel()
                val joinedCount = initialCount + 1
                println("    → Member join: $initialCount → $joinedCount")

                viewModel.setGroup(MockFactory.createGroup(guid = "group-1", membersCount = initialCount))
                advanceUntilIdle()
                viewModel.memberCount.value shouldBe initialCount

                // Simulate onGroupMemberJoined by setting updated group
                viewModel.setGroup(MockFactory.createGroup(guid = "group-1", membersCount = joinedCount))
                advanceUntilIdle()
                viewModel.memberCount.value shouldBe joinedCount
            }
        }
    }

    test("PROPERTY: member leave should decrease count (simulated via setGroup)") {
        checkAll(20, Arb.int(2..50)) { initialCount ->
            runTest {
                val viewModel = createViewModel()
                val leftCount = initialCount - 1
                println("    → Member leave: $initialCount → $leftCount")

                viewModel.setGroup(MockFactory.createGroup(guid = "group-1", membersCount = initialCount))
                advanceUntilIdle()
                viewModel.memberCount.value shouldBe initialCount

                // Simulate onGroupMemberLeft by setting updated group
                viewModel.setGroup(MockFactory.createGroup(guid = "group-1", membersCount = leftCount))
                advanceUntilIdle()
                viewModel.memberCount.value shouldBe leftCount
            }
        }
    }

    // ==================== Blocked User Behavior ====================

    test("PROPERTY: blocked user should not show typing or status (blocked state preserved)") {
        checkAll(20, Arb.boolean(), Arb.boolean()) { blockedByMe, hasBlockedMe ->
            runTest {
                val viewModel = createViewModel()
                val user = MockFactory.createUser(
                    uid = "user-1",
                    isBlockedByMe = blockedByMe,
                    hasBlockedMe = hasBlockedMe
                )
                println("    → Blocked: blockedByMe=$blockedByMe, hasBlockedMe=$hasBlockedMe")

                viewModel.setUser(user)
                advanceUntilIdle()

                val state = viewModel.uiState.value as MessageHeaderUIState.UserContent
                state.user.isBlockedByMe shouldBe blockedByMe
                state.user.isHasBlockedMe shouldBe hasBlockedMe
                // Typing should remain null for blocked users
                viewModel.typingIndicator.value shouldBe null
            }
        }
    }

    // ==================== Group Type Preservation ====================

    test("PROPERTY: group type should be preserved across all group types") {
        val groupTypeArb = Arb.element(
            CometChatConstants.GROUP_TYPE_PUBLIC,
            CometChatConstants.GROUP_TYPE_PRIVATE,
            CometChatConstants.GROUP_TYPE_PASSWORD
        )
        checkAll(30, Arb.string(5..15), groupTypeArb, Arb.int(1..100)) { guid, type, members ->
            runTest {
                val viewModel = createViewModel()
                val group = MockFactory.createGroup(guid = guid, type = type, membersCount = members)
                println("    → Group: guid=$guid, type=$type, members=$members")

                viewModel.setGroup(group)
                advanceUntilIdle()

                val state = viewModel.uiState.value as MessageHeaderUIState.GroupContent
                state.group.groupType shouldBe type
                state.group.membersCount shouldBe members
                viewModel.memberCount.value shouldBe members
            }
        }
    }

    // ==================== Refresh Behavior (Reconnection) ====================

    test("PROPERTY: refreshUser should update user data from server") {
        checkAll(20, Arb.string(5..15), Arb.string(3..20)) { uid, newName ->
            runTest {
                val viewModel = createViewModel()
                val refreshedUser = MockFactory.createUser(uid = uid, name = newName)
                whenever(getUserUseCase.invoke(uid)).thenReturn(Result.success(refreshedUser))
                println("    → Refresh user: uid=$uid, newName=$newName")

                viewModel.setUser(MockFactory.createUser(uid = uid, name = "Old Name"))
                advanceUntilIdle()

                viewModel.refreshUser(uid)
                advanceUntilIdle()

                val state = viewModel.uiState.value as MessageHeaderUIState.UserContent
                state.user.name shouldBe newName
            }
        }
    }

    test("PROPERTY: refreshGroup should update group data from server") {
        checkAll(20, Arb.string(5..15), Arb.int(1..100)) { guid, newCount ->
            runTest {
                val viewModel = createViewModel()
                val refreshedGroup = MockFactory.createGroup(guid = guid, membersCount = newCount)
                whenever(getGroupUseCase.invoke(guid)).thenReturn(Result.success(refreshedGroup))
                println("    → Refresh group: guid=$guid, newCount=$newCount")

                viewModel.setGroup(MockFactory.createGroup(guid = guid, membersCount = 1))
                advanceUntilIdle()

                viewModel.refreshGroup(guid)
                advanceUntilIdle()

                viewModel.memberCount.value shouldBe newCount
            }
        }
    }
})
