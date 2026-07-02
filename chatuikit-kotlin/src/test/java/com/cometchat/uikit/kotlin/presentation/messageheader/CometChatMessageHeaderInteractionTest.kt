package com.cometchat.uikit.kotlin.presentation.messageheader

import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.domain.usecase.GetGroupUseCase
import com.cometchat.uikit.core.domain.usecase.GetUserUseCase
import com.cometchat.uikit.core.state.MessageHeaderUIState
import com.cometchat.uikit.core.viewmodel.CometChatMessageHeaderViewModel
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * JVM interaction tests for CometChatMessageHeader (chatuikit-kotlin).
 * Verifies user interactions produce correct ViewModel state changes.
 *
 * Tests the interaction patterns:
 * - Setting user/group (simulates opening a conversation)
 * - Refreshing data (simulates reconnection or pull-to-refresh)
 * - Switching conversations
 *
 * Run with:
 *   ./gradlew :chatuikit-kotlin:testDebugUnitTest --tests "*CometChatMessageHeaderInteractionTest"
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CometChatMessageHeaderInteractionTest : FunSpec({

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

    fun createMockUser(
        uid: String = "user-1",
        name: String = "Test User",
        status: String = CometChatConstants.USER_STATUS_ONLINE,
        isBlockedByMe: Boolean = false
    ): User {
        val user = mock<User>()
        whenever(user.uid).thenReturn(uid)
        whenever(user.name).thenReturn(name)
        whenever(user.status).thenReturn(status)
        whenever(user.isBlockedByMe).thenReturn(isBlockedByMe)
        whenever(user.isHasBlockedMe).thenReturn(false)
        return user
    }

    fun createMockGroup(
        guid: String = "group-1",
        name: String = "Test Group",
        type: String = CometChatConstants.GROUP_TYPE_PUBLIC,
        membersCount: Int = 5
    ): Group {
        val group = mock<Group>()
        whenever(group.guid).thenReturn(guid)
        whenever(group.name).thenReturn(name)
        whenever(group.groupType).thenReturn(type)
        whenever(group.membersCount).thenReturn(membersCount)
        return group
    }

    fun createMockException(code: String = "ERR", message: String = "Error"): com.cometchat.chat.exceptions.CometChatException {
        return com.cometchat.chat.exceptions.CometChatException(code, message)
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

    // ==================== Opening a User Conversation ====================

    test("opening user conversation should set user and update state") {
        checkAll(20, Arb.string(5..20), Arb.string(3..30)) { uid, name ->
            runTest {
                val viewModel = createViewModel()
                val user = createMockUser(uid = uid, name = name)
                println("    → Opening user conversation: uid=$uid, name=$name")

                viewModel.setUser(user)
                advanceUntilIdle()

                viewModel.user.value?.uid shouldBe uid
                viewModel.uiState.value.shouldBeInstanceOf<MessageHeaderUIState.UserContent>()
            }
        }
    }

    // ==================== Opening a Group Conversation ====================

    test("opening group conversation should set group and update state") {
        checkAll(20, Arb.string(5..20), Arb.string(3..30), Arb.int(1..50)) { guid, name, members ->
            runTest {
                val viewModel = createViewModel()
                val group = createMockGroup(guid = guid, name = name, membersCount = members)
                println("    → Opening group conversation: guid=$guid, name=$name, members=$members")

                viewModel.setGroup(group)
                advanceUntilIdle()

                viewModel.group.value?.guid shouldBe guid
                viewModel.memberCount.value shouldBe members
                viewModel.uiState.value.shouldBeInstanceOf<MessageHeaderUIState.GroupContent>()
            }
        }
    }

    // ==================== Switching Conversations ====================

    test("switching from user to group should clear user and show group") {
        runTest {
            val viewModel = createViewModel()
            val user = createMockUser(uid = "user-1", name = "Alice")
            val group = createMockGroup(guid = "group-1", name = "Devs", membersCount = 5)
            println("    → Switch: user → group")

            viewModel.setUser(user)
            advanceUntilIdle()
            viewModel.uiState.value.shouldBeInstanceOf<MessageHeaderUIState.UserContent>()

            viewModel.setGroup(group)
            advanceUntilIdle()
            viewModel.user.value shouldBe null
            viewModel.group.value?.guid shouldBe "group-1"
            viewModel.uiState.value.shouldBeInstanceOf<MessageHeaderUIState.GroupContent>()
            println("    → After switch: user=null, group=group-1")
        }
    }

    test("switching from group to user should clear group and show user") {
        runTest {
            val viewModel = createViewModel()
            val group = createMockGroup(guid = "group-1", name = "Devs")
            val user = createMockUser(uid = "user-1", name = "Alice")
            println("    → Switch: group → user")

            viewModel.setGroup(group)
            advanceUntilIdle()
            viewModel.uiState.value.shouldBeInstanceOf<MessageHeaderUIState.GroupContent>()

            viewModel.setUser(user)
            advanceUntilIdle()
            viewModel.group.value shouldBe null
            viewModel.user.value?.uid shouldBe "user-1"
            viewModel.uiState.value.shouldBeInstanceOf<MessageHeaderUIState.UserContent>()
            println("    → After switch: group=null, user=user-1")
        }
    }

    // ==================== Refresh Interaction ====================

    test("refresh user interaction should update state on success") {
        runTest {
            val viewModel = createViewModel()
            val initialUser = createMockUser(uid = "user-1", name = "Alice", status = CometChatConstants.USER_STATUS_OFFLINE)
            val refreshedUser = createMockUser(uid = "user-1", name = "Alice", status = CometChatConstants.USER_STATUS_ONLINE)
            whenever(getUserUseCase.invoke("user-1")).thenReturn(Result.success(refreshedUser))
            println("    → Refresh user: offline → online")

            viewModel.setUser(initialUser)
            advanceUntilIdle()

            viewModel.refreshUser("user-1")
            advanceUntilIdle()

            val state = viewModel.uiState.value as MessageHeaderUIState.UserContent
            state.user.status shouldBe CometChatConstants.USER_STATUS_ONLINE
            println("    → After refresh: status=${state.user.status}")
        }
    }

    test("refresh group interaction should update member count") {
        runTest {
            val viewModel = createViewModel()
            val initialGroup = createMockGroup(guid = "group-1", name = "Devs", membersCount = 5)
            val refreshedGroup = createMockGroup(guid = "group-1", name = "Devs", membersCount = 12)
            whenever(getGroupUseCase.invoke("group-1")).thenReturn(Result.success(refreshedGroup))
            println("    → Refresh group: members 5 → 12")

            viewModel.setGroup(initialGroup)
            advanceUntilIdle()
            viewModel.memberCount.value shouldBe 5

            viewModel.refreshGroup("group-1")
            advanceUntilIdle()
            viewModel.memberCount.value shouldBe 12
            println("    → After refresh: members=${viewModel.memberCount.value}")
        }
    }

    // ==================== Error Interaction ====================

    test("refresh failure should emit error without changing current state") {
        runTest {
            val viewModel = createViewModel()
            val user = createMockUser(uid = "user-1", name = "Alice")
            val exception = createMockException("ERR", "Failed")
            whenever(getUserUseCase.invoke("user-1")).thenReturn(Result.failure(exception))
            println("    → Refresh with failure, state should remain UserContent")

            viewModel.setUser(user)
            advanceUntilIdle()

            var emittedError: com.cometchat.chat.exceptions.CometChatException? = null
            val job = launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.errorEvent.collect { emittedError = it }
            }

            viewModel.refreshUser("user-1")
            advanceUntilIdle()

            // State should still be UserContent (not Error)
            viewModel.uiState.value.shouldBeInstanceOf<MessageHeaderUIState.UserContent>()
            emittedError?.code shouldBe "ERR"
            println("    → State preserved as UserContent, error emitted via errorEvent")
            job.cancel()
        }
    }

    // ==================== Multiple Rapid Updates ====================

    test("rapid user updates should settle on the last user") {
        runTest {
            val viewModel = createViewModel()
            println("    → Rapid user updates: user-1, user-2, user-3")

            viewModel.setUser(createMockUser(uid = "user-1", name = "First"))
            viewModel.setUser(createMockUser(uid = "user-2", name = "Second"))
            viewModel.setUser(createMockUser(uid = "user-3", name = "Third"))
            advanceUntilIdle()

            val state = viewModel.uiState.value as MessageHeaderUIState.UserContent
            state.user.uid shouldBe "user-3"
            state.user.name shouldBe "Third"
            println("    → Settled on: uid=${state.user.uid}, name=${state.user.name}")
        }
    }
})
