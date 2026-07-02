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
 * JVM rendering tests for CometChatMessageHeader (chatuikit-kotlin).
 * Verifies ViewModel states produce correct UIState for View rendering.
 *
 * These tests validate the state-to-UI mapping logic that the View layer
 * uses to determine what to display (user name, group name, status, member count).
 *
 * Run with:
 *   ./gradlew :chatuikit-kotlin:testDebugUnitTest --tests "*CometChatMessageHeaderRenderingTest"
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CometChatMessageHeaderRenderingTest : FunSpec({

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

    // ==================== User Rendering States ====================

    test("user with online status should produce UserContent with online status") {
        checkAll(20, Arb.string(5..20), Arb.string(3..30)) { uid, name ->
            runTest {
                val viewModel = createViewModel()
                val user = createMockUser(uid = uid, name = name, status = CometChatConstants.USER_STATUS_ONLINE)
                println("    → Rendering user: uid=$uid, name=$name, status=online")

                viewModel.setUser(user)
                advanceUntilIdle()

                val state = viewModel.uiState.value as MessageHeaderUIState.UserContent
                state.user.name shouldBe name
                state.user.status shouldBe CometChatConstants.USER_STATUS_ONLINE
            }
        }
    }

    test("user with offline status should produce UserContent with offline status") {
        checkAll(20, Arb.string(5..20), Arb.string(3..30)) { uid, name ->
            runTest {
                val viewModel = createViewModel()
                val user = createMockUser(uid = uid, name = name, status = CometChatConstants.USER_STATUS_OFFLINE)
                println("    → Rendering user: uid=$uid, name=$name, status=offline")

                viewModel.setUser(user)
                advanceUntilIdle()

                val state = viewModel.uiState.value as MessageHeaderUIState.UserContent
                state.user.name shouldBe name
                state.user.status shouldBe CometChatConstants.USER_STATUS_OFFLINE
            }
        }
    }

    test("blocked user should still produce UserContent state") {
        runTest {
            val viewModel = createViewModel()
            val user = createMockUser(uid = "user-1", name = "Blocked", isBlockedByMe = true)
            println("    → Rendering blocked user")

            viewModel.setUser(user)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            state.shouldBeInstanceOf<MessageHeaderUIState.UserContent>()
            (state as MessageHeaderUIState.UserContent).user.isBlockedByMe shouldBe true
        }
    }

    // ==================== Group Rendering States ====================

    test("group should produce GroupContent with member count") {
        checkAll(20, Arb.string(5..20), Arb.string(3..30), Arb.int(1..100)) { guid, name, members ->
            runTest {
                val viewModel = createViewModel()
                val group = createMockGroup(guid = guid, name = name, membersCount = members)
                println("    → Rendering group: guid=$guid, name=$name, members=$members")

                viewModel.setGroup(group)
                advanceUntilIdle()

                val state = viewModel.uiState.value as MessageHeaderUIState.GroupContent
                state.group.name shouldBe name
                state.group.membersCount shouldBe members
                viewModel.memberCount.value shouldBe members
            }
        }
    }

    test("group type should be preserved for rendering status indicator") {
        val groupTypeArb = Arb.element(
            CometChatConstants.GROUP_TYPE_PUBLIC,
            CometChatConstants.GROUP_TYPE_PRIVATE,
            CometChatConstants.GROUP_TYPE_PASSWORD
        )
        checkAll(20, Arb.string(5..15), groupTypeArb) { guid, groupType ->
            runTest {
                val viewModel = createViewModel()
                val group = createMockGroup(guid = guid, type = groupType)
                println("    → Rendering group type: guid=$guid, type=$groupType")

                viewModel.setGroup(group)
                advanceUntilIdle()

                val state = viewModel.uiState.value as MessageHeaderUIState.GroupContent
                state.group.groupType shouldBe groupType
            }
        }
    }

    // ==================== Initial State ====================

    test("initial state should be Loading for rendering placeholder") {
        val viewModel = createViewModel()
        println("    → Initial state for rendering")
        viewModel.uiState.value.shouldBeInstanceOf<MessageHeaderUIState.Loading>()
    }

    // ==================== Typing Indicator Rendering ====================

    test("typingIndicator null means subtitle should show status/member count") {
        runTest {
            val viewModel = createViewModel()
            val user = createMockUser(uid = "user-1", name = "Alice", status = CometChatConstants.USER_STATUS_ONLINE)
            println("    → No typing indicator → show status")

            viewModel.setUser(user)
            advanceUntilIdle()

            viewModel.typingIndicator.value shouldBe null
            // View should render subtitle as "Online" or last seen
        }
    }

    // ==================== Member Count Rendering ====================

    test("memberCount flow should reflect group members for subtitle rendering") {
        checkAll(20, Arb.int(1..500)) { count ->
            runTest {
                val viewModel = createViewModel()
                val group = createMockGroup(guid = "group-1", membersCount = count)
                println("    → Member count for subtitle: $count")

                viewModel.setGroup(group)
                advanceUntilIdle()

                viewModel.memberCount.value shouldBe count
                // View renders: "$count Members" or "$count Member"
            }
        }
    }
})
