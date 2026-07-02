package com.cometchat.uikit.compose.presentation.messageheader

import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.uikit.core.domain.usecase.GetGroupUseCase
import com.cometchat.uikit.core.domain.usecase.GetUserUseCase
import com.cometchat.uikit.core.state.MessageHeaderUIState
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.User
import org.mockito.kotlin.whenever
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

/**
 * JVM rendering tests for CometChatMessageHeader (chatuikit-compose).
 * Verifies ViewModel states produce correct UIState for composable rendering.
 *
 * Run with:
 *   ./gradlew :chatuikit-compose:testDebugUnitTest --tests "*CometChatMessageHeaderRenderingTest"
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

    // ==================== Initial State ====================

    test("initial state should be Loading for composable to show placeholder") {
        val viewModel = createViewModel()
        println("    → Initial state: ${viewModel.uiState.value}")
        viewModel.uiState.value.shouldBeInstanceOf<MessageHeaderUIState.Loading>()
    }

    // ==================== User Rendering ====================

    test("user online should produce UserContent for composable to render online status") {
        checkAll(20, Arb.string(5..20), Arb.string(3..30)) { uid, name ->
            runTest {
                val viewModel = createViewModel()
                val user = createMockUser(uid = uid, name = name, status = CometChatConstants.USER_STATUS_ONLINE)
                println("    → Compose render: user=$name, status=online")

                viewModel.setUser(user)
                advanceUntilIdle()

                val state = viewModel.uiState.value as MessageHeaderUIState.UserContent
                state.user.name shouldBe name
                state.user.status shouldBe CometChatConstants.USER_STATUS_ONLINE
            }
        }
    }

    test("user offline should produce UserContent for composable to render offline/last seen") {
        checkAll(20, Arb.string(5..20), Arb.string(3..30)) { uid, name ->
            runTest {
                val viewModel = createViewModel()
                val user = createMockUser(uid = uid, name = name, status = CometChatConstants.USER_STATUS_OFFLINE)
                println("    → Compose render: user=$name, status=offline")

                viewModel.setUser(user)
                advanceUntilIdle()

                val state = viewModel.uiState.value as MessageHeaderUIState.UserContent
                state.user.name shouldBe name
                state.user.status shouldBe CometChatConstants.USER_STATUS_OFFLINE
            }
        }
    }

    // ==================== Group Rendering ====================

    test("group should produce GroupContent for composable to render member count") {
        checkAll(20, Arb.string(5..20), Arb.string(3..30), Arb.int(1..100)) { guid, name, members ->
            runTest {
                val viewModel = createViewModel()
                val group = createMockGroup(guid = guid, name = name, membersCount = members)
                println("    → Compose render: group=$name, members=$members")

                viewModel.setGroup(group)
                advanceUntilIdle()

                val state = viewModel.uiState.value as MessageHeaderUIState.GroupContent
                state.group.name shouldBe name
                viewModel.memberCount.value shouldBe members
            }
        }
    }

    test("group type should be available for composable to render status indicator") {
        val groupTypeArb = Arb.element(
            CometChatConstants.GROUP_TYPE_PUBLIC,
            CometChatConstants.GROUP_TYPE_PRIVATE,
            CometChatConstants.GROUP_TYPE_PASSWORD
        )
        checkAll(20, Arb.string(5..15), groupTypeArb) { guid, groupType ->
            runTest {
                val viewModel = createViewModel()
                val group = createMockGroup(guid = guid, type = groupType)
                println("    → Compose render: group type=$groupType")

                viewModel.setGroup(group)
                advanceUntilIdle()

                val state = viewModel.uiState.value as MessageHeaderUIState.GroupContent
                state.group.groupType shouldBe groupType
            }
        }
    }

    // ==================== Typing Indicator ====================

    test("typingIndicator null should signal composable to show subtitle instead") {
        runTest {
            val viewModel = createViewModel()
            val user = createMockUser(uid = "user-1", name = "Alice")
            println("    → No typing → composable shows subtitle")

            viewModel.setUser(user)
            advanceUntilIdle()

            viewModel.typingIndicator.value shouldBe null
        }
    }

    // ==================== Blocked User ====================

    test("blocked user should still produce UserContent for composable to handle") {
        runTest {
            val viewModel = createViewModel()
            val user = createMockUser(uid = "user-1", name = "Blocked", isBlockedByMe = true)
            println("    → Compose render: blocked user")

            viewModel.setUser(user)
            advanceUntilIdle()

            val state = viewModel.uiState.value as MessageHeaderUIState.UserContent
            state.user.isBlockedByMe shouldBe true
        }
    }

    // ==================== Context Switching ====================

    test("switching user to group should update state for composable recomposition") {
        runTest {
            val viewModel = createViewModel()
            println("    → Compose: user → group recomposition")

            viewModel.setUser(createMockUser(uid = "user-1", name = "Alice"))
            advanceUntilIdle()
            viewModel.uiState.value.shouldBeInstanceOf<MessageHeaderUIState.UserContent>()

            viewModel.setGroup(createMockGroup(guid = "group-1", name = "Devs", membersCount = 5))
            advanceUntilIdle()
            viewModel.uiState.value.shouldBeInstanceOf<MessageHeaderUIState.GroupContent>()
            viewModel.user.value shouldBe null
        }
    }
})
