package com.cometchat.uikit.core.viewmodel.messageheader

import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.exceptions.CometChatException
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
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Comprehensive property-based tests for CometChatMessageHeaderViewModel.
 * Layer 6 — mocks UseCases via Mockito.
 *
 * Categories:
 * A. Initial State & setUser/setGroup
 * B. User Status (online/offline)
 * C. Group Member Count
 * D. Typing Indicators
 * E. Refresh (reconnection)
 * F. Error Handling
 * G. Block/Unblock
 * H. UIKit Local Events
 *
 * Run with:
 *   ./gradlew :chatuikit-core:testDebugUnitTest --tests "*CometChatMessageHeaderViewModelTest"
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CometChatMessageHeaderViewModelTest : FunSpec({

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

    // ==================== A. Initial State & setUser/setGroup ====================

    test("initial uiState should be Loading") {
        val viewModel = createViewModel()
        println("    → Initial state: ${viewModel.uiState.value}")
        viewModel.uiState.value.shouldBeInstanceOf<MessageHeaderUIState.Loading>()
    }

    test("setUser should transition uiState to UserContent") {
        checkAll(50, Arb.string(5..20), Arb.string(3..30)) { uid, name ->
            runTest {
                val viewModel = createViewModel()
                val user = MockFactory.createUser(uid = uid, name = name)
                println("    → setUser(uid=$uid, name=$name)")

                viewModel.setUser(user)
                advanceUntilIdle()

                val state = viewModel.uiState.value
                state.shouldBeInstanceOf<MessageHeaderUIState.UserContent>()
                state.user.uid shouldBe uid
                state.user.name shouldBe name
            }
        }
    }

    test("setGroup should transition uiState to GroupContent") {
        checkAll(50, Arb.string(5..20), Arb.string(3..30), Arb.int(1..100)) { guid, name, members ->
            runTest {
                val viewModel = createViewModel()
                val group = MockFactory.createGroup(guid = guid, name = name, membersCount = members)
                println("    → setGroup(guid=$guid, name=$name, members=$members)")

                viewModel.setGroup(group)
                advanceUntilIdle()

                val state = viewModel.uiState.value
                state.shouldBeInstanceOf<MessageHeaderUIState.GroupContent>()
                state.group.guid shouldBe guid
                state.group.name shouldBe name
                viewModel.memberCount.value shouldBe members
            }
        }
    }

    test("setUser should clear group state") {
        runTest {
            val viewModel = createViewModel()
            val group = MockFactory.createGroup(guid = "group-1", name = "Group")
            val user = MockFactory.createUser(uid = "user-1", name = "User")
            println("    → setGroup then setUser")

            viewModel.setGroup(group)
            advanceUntilIdle()
            viewModel.group.value?.guid shouldBe "group-1"

            viewModel.setUser(user)
            advanceUntilIdle()
            viewModel.group.value shouldBe null
            viewModel.user.value?.uid shouldBe "user-1"
            viewModel.uiState.value.shouldBeInstanceOf<MessageHeaderUIState.UserContent>()
        }
    }

    test("setGroup should clear user state") {
        runTest {
            val viewModel = createViewModel()
            val user = MockFactory.createUser(uid = "user-1", name = "User")
            val group = MockFactory.createGroup(guid = "group-1", name = "Group")
            println("    → setUser then setGroup")

            viewModel.setUser(user)
            advanceUntilIdle()
            viewModel.user.value?.uid shouldBe "user-1"

            viewModel.setGroup(group)
            advanceUntilIdle()
            viewModel.user.value shouldBe null
            viewModel.group.value?.guid shouldBe "group-1"
            viewModel.uiState.value.shouldBeInstanceOf<MessageHeaderUIState.GroupContent>()
        }
    }

    // ==================== B. User Status ====================

    test("for any user status: UserContent should reflect the status") {
        val statusArb = Arb.element(CometChatConstants.USER_STATUS_ONLINE, CometChatConstants.USER_STATUS_OFFLINE)
        checkAll(20, Arb.string(5..15), statusArb) { uid, status ->
            runTest {
                val viewModel = createViewModel()
                val user = MockFactory.createUser(uid = uid, status = status)
                println("    → setUser(uid=$uid, status=$status)")

                viewModel.setUser(user)
                advanceUntilIdle()

                val state = viewModel.uiState.value as MessageHeaderUIState.UserContent
                state.user.status shouldBe status
            }
        }
    }

    // ==================== C. Group Member Count ====================

    test("setGroup should update memberCount flow") {
        checkAll(30, Arb.int(1..500)) { count ->
            runTest {
                val viewModel = createViewModel()
                val group = MockFactory.createGroup(guid = "group-1", membersCount = count)
                println("    → setGroup with membersCount=$count")

                viewModel.setGroup(group)
                advanceUntilIdle()

                viewModel.memberCount.value shouldBe count
            }
        }
    }

    test("memberCount should update when group is set multiple times") {
        runTest {
            val viewModel = createViewModel()
            println("    → Setting group with different member counts")

            val group1 = MockFactory.createGroup(guid = "group-1", membersCount = 5)
            viewModel.setGroup(group1)
            advanceUntilIdle()
            viewModel.memberCount.value shouldBe 5

            val group2 = MockFactory.createGroup(guid = "group-1", membersCount = 10)
            viewModel.setGroup(group2)
            advanceUntilIdle()
            viewModel.memberCount.value shouldBe 10
            println("    → memberCount updated: 5 → 10")
        }
    }

    // ==================== D. Typing Indicators ====================

    test("typingIndicator should initially be null") {
        val viewModel = createViewModel()
        println("    → Initial typingIndicator: ${viewModel.typingIndicator.value}")
        viewModel.typingIndicator.value shouldBe null
    }

    // ==================== E. Refresh (Reconnection) ====================

    test("refreshUser should call getUserUseCase and update state on success") {
        runTest {
            val viewModel = createViewModel()
            val user = MockFactory.createUser(uid = "user-1", name = "Refreshed Alice")
            whenever(getUserUseCase.invoke("user-1")).thenReturn(Result.success(user))
            println("    → refreshUser(uid=user-1)")

            // First set user to establish currentId
            viewModel.setUser(MockFactory.createUser(uid = "user-1", name = "Old Alice"))
            advanceUntilIdle()

            viewModel.refreshUser("user-1")
            advanceUntilIdle()

            val state = viewModel.uiState.value as MessageHeaderUIState.UserContent
            state.user.name shouldBe "Refreshed Alice"
            verify(getUserUseCase).invoke("user-1")
            println("    → State updated to: ${state.user.name}")
        }
    }

    test("refreshUser should emit errorEvent on failure") {
        runTest {
            val viewModel = createViewModel()
            val exception = MockFactory.createCometChatException("ERR_REFRESH", "Refresh failed")
            whenever(getUserUseCase.invoke("user-1")).thenReturn(Result.failure(exception))
            println("    → refreshUser with failure")

            // Set user first
            viewModel.setUser(MockFactory.createUser(uid = "user-1"))
            advanceUntilIdle()

            // Collect error events - start collecting BEFORE triggering the action
            var emittedError: CometChatException? = null
            val job = launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.errorEvent.collect { emittedError = it }
            }

            viewModel.refreshUser("user-1")
            advanceUntilIdle()

            emittedError?.code shouldBe "ERR_REFRESH"
            println("    → Error emitted: code=${emittedError?.code}")
            job.cancel()
        }
    }

    test("refreshGroup should call getGroupUseCase and update state on success") {
        runTest {
            val viewModel = createViewModel()
            val group = MockFactory.createGroup(guid = "group-1", name = "Refreshed Group", membersCount = 15)
            whenever(getGroupUseCase.invoke("group-1")).thenReturn(Result.success(group))
            println("    → refreshGroup(guid=group-1)")

            // First set group to establish currentId
            viewModel.setGroup(MockFactory.createGroup(guid = "group-1", name = "Old Group"))
            advanceUntilIdle()

            viewModel.refreshGroup("group-1")
            advanceUntilIdle()

            val state = viewModel.uiState.value as MessageHeaderUIState.GroupContent
            state.group.name shouldBe "Refreshed Group"
            viewModel.memberCount.value shouldBe 15
            verify(getGroupUseCase).invoke("group-1")
            println("    → State updated to: ${state.group.name}, members=${viewModel.memberCount.value}")
        }
    }

    test("refreshGroup should emit errorEvent on failure") {
        runTest {
            val viewModel = createViewModel()
            val exception = MockFactory.createCometChatException("ERR_REFRESH", "Group refresh failed")
            whenever(getGroupUseCase.invoke("group-1")).thenReturn(Result.failure(exception))
            println("    → refreshGroup with failure")

            // Set group first
            viewModel.setGroup(MockFactory.createGroup(guid = "group-1"))
            advanceUntilIdle()

            var emittedError: CometChatException? = null
            val job = launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.errorEvent.collect { emittedError = it }
            }

            viewModel.refreshGroup("group-1")
            advanceUntilIdle()

            emittedError?.code shouldBe "ERR_REFRESH"
            println("    → Error emitted: code=${emittedError?.code}")
            job.cancel()
        }
    }

    // Regression for ENG-37016: navigating away mid-refresh cancels the ViewModel scope;
    // the resulting CancellationException must not be cast to CometChatException (crash)
    // nor surface as an error event.
    test("refreshGroup with CancellationException failure should not crash or emit error") {
        runTest {
            val viewModel = createViewModel()
            whenever(getGroupUseCase.invoke("group-1"))
                .thenReturn(Result.failure(CancellationException("scope cancelled")))
            println("    → refreshGroup with CancellationException failure")

            viewModel.setGroup(MockFactory.createGroup(guid = "group-1"))
            advanceUntilIdle()

            var emittedError: CometChatException? = null
            val job = launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.errorEvent.collect { emittedError = it }
            }

            viewModel.refreshGroup("group-1")
            advanceUntilIdle()

            emittedError shouldBe null
            println("    → No crash, no error emitted")
            job.cancel()
        }
    }

    test("refreshUser with CancellationException failure should not crash or emit error") {
        runTest {
            val viewModel = createViewModel()
            whenever(getUserUseCase.invoke("user-1"))
                .thenReturn(Result.failure(CancellationException("scope cancelled")))
            println("    → refreshUser with CancellationException failure")

            viewModel.setUser(MockFactory.createUser(uid = "user-1"))
            advanceUntilIdle()

            var emittedError: CometChatException? = null
            val job = launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.errorEvent.collect { emittedError = it }
            }

            viewModel.refreshUser("user-1")
            advanceUntilIdle()

            emittedError shouldBe null
            println("    → No crash, no error emitted")
            job.cancel()
        }
    }

    test("refreshGroup with non-CometChat exception should emit wrapped UNKNOWN_ERROR instead of crashing") {
        runTest {
            val viewModel = createViewModel()
            whenever(getGroupUseCase.invoke("group-1"))
                .thenReturn(Result.failure(IllegalStateException("Network timeout")))
            println("    → refreshGroup with IllegalStateException failure")

            viewModel.setGroup(MockFactory.createGroup(guid = "group-1"))
            advanceUntilIdle()

            var emittedError: CometChatException? = null
            val job = launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.errorEvent.collect { emittedError = it }
            }

            viewModel.refreshGroup("group-1")
            advanceUntilIdle()

            emittedError?.code shouldBe "UNKNOWN_ERROR"
            emittedError?.message shouldBe "Network timeout"
            println("    → Wrapped error emitted: code=${emittedError?.code}")
            job.cancel()
        }
    }

    // ==================== F. Error Handling ====================

    test("for any error code: refreshUser failure should emit correct error") {
        checkAll(20, Arb.string(3..10), Arb.string(5..50)) { code, message ->
            runTest {
                val viewModel = createViewModel()
                val exception = MockFactory.createCometChatException(code, message)
                whenever(getUserUseCase.invoke("user-1")).thenReturn(Result.failure(exception))
                println("    → Testing error: code=$code, message=$message")

                viewModel.setUser(MockFactory.createUser(uid = "user-1"))
                advanceUntilIdle()

                var emittedError: CometChatException? = null
                val job = launch(UnconfinedTestDispatcher(testScheduler)) {
                    viewModel.errorEvent.collect { emittedError = it }
                }

                viewModel.refreshUser("user-1")
                advanceUntilIdle()

                emittedError?.code shouldBe code
                emittedError?.message shouldBe message
                job.cancel()
            }
        }
    }

    // ==================== G. Block/Unblock ====================

    test("setUser with blocked user should still update state") {
        checkAll(20, Arb.boolean(), Arb.boolean()) { blockedByMe, hasBlockedMe ->
            runTest {
                val viewModel = createViewModel()
                val user = MockFactory.createUser(
                    uid = "user-1",
                    name = "Blocked User",
                    isBlockedByMe = blockedByMe,
                    hasBlockedMe = hasBlockedMe
                )
                println("    → setUser(blockedByMe=$blockedByMe, hasBlockedMe=$hasBlockedMe)")

                viewModel.setUser(user)
                advanceUntilIdle()

                val state = viewModel.uiState.value
                state.shouldBeInstanceOf<MessageHeaderUIState.UserContent>()
                state.user.isBlockedByMe shouldBe blockedByMe
                state.user.isHasBlockedMe shouldBe hasBlockedMe
            }
        }
    }

    // ==================== H. Group Type ====================

    test("setGroup should preserve group type in state") {
        val groupTypeArb = Arb.element(
            CometChatConstants.GROUP_TYPE_PUBLIC,
            CometChatConstants.GROUP_TYPE_PRIVATE,
            CometChatConstants.GROUP_TYPE_PASSWORD
        )
        checkAll(20, Arb.string(5..15), groupTypeArb) { guid, groupType ->
            runTest {
                val viewModel = createViewModel()
                val group = MockFactory.createGroup(guid = guid, type = groupType)
                println("    → setGroup(guid=$guid, type=$groupType)")

                viewModel.setGroup(group)
                advanceUntilIdle()

                val state = viewModel.uiState.value as MessageHeaderUIState.GroupContent
                state.group.groupType shouldBe groupType
            }
        }
    }

    // ==================== I. State Flow Exposure ====================

    test("user flow should expose current user") {
        runTest {
            val viewModel = createViewModel()
            println("    → Testing user StateFlow exposure")

            viewModel.user.value shouldBe null

            val user = MockFactory.createUser(uid = "user-1", name = "Alice")
            viewModel.setUser(user)
            advanceUntilIdle()

            viewModel.user.value?.uid shouldBe "user-1"
            viewModel.user.value?.name shouldBe "Alice"
            println("    → user.value: uid=${viewModel.user.value?.uid}")
        }
    }

    test("group flow should expose current group") {
        runTest {
            val viewModel = createViewModel()
            println("    → Testing group StateFlow exposure")

            viewModel.group.value shouldBe null

            val group = MockFactory.createGroup(guid = "group-1", name = "Devs")
            viewModel.setGroup(group)
            advanceUntilIdle()

            viewModel.group.value?.guid shouldBe "group-1"
            viewModel.group.value?.name shouldBe "Devs"
            println("    → group.value: guid=${viewModel.group.value?.guid}")
        }
    }

    // ==================== J. Listener Cleanup ====================

    test("removeListeners should not throw when called") {
        val viewModel = createViewModel()
        println("    → Calling removeListeners (should not throw)")
        viewModel.removeListeners()
        println("    → removeListeners completed without exception")
    }
})
