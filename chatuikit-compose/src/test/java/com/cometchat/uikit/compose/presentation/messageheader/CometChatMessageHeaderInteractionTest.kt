package com.cometchat.uikit.compose.presentation.messageheader

import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.exceptions.CometChatException
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
 * JVM interaction tests for CometChatMessageHeader (chatuikit-compose).
 * Verifies user interactions produce correct ViewModel state changes
 * that trigger composable recomposition.
 *
 * Run with:
 *   ./gradlew :chatuikit-compose:testDebugUnitTest --tests "*CometChatMessageHeaderInteractionTest"
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

    // ==================== LaunchedEffect: Setting User ====================

    test("LaunchedEffect setting user should trigger UserContent state") {
        checkAll(20, Arb.string(5..20), Arb.string(3..30)) { uid, name ->
            runTest {
                val viewModel = createViewModel()
                val user = createMockUser(uid = uid, name = name)
                println("    → LaunchedEffect: setUser(uid=$uid)")

                // Simulates what LaunchedEffect does in the composable
                viewModel.setUser(user)
                advanceUntilIdle()

                viewModel.uiState.value.shouldBeInstanceOf<MessageHeaderUIState.UserContent>()
                viewModel.user.value?.uid shouldBe uid
            }
        }
    }

    // ==================== LaunchedEffect: Setting Group ====================

    test("LaunchedEffect setting group should trigger GroupContent state") {
        checkAll(20, Arb.string(5..20), Arb.string(3..30), Arb.int(1..50)) { guid, name, members ->
            runTest {
                val viewModel = createViewModel()
                val group = createMockGroup(guid = guid, name = name, membersCount = members)
                println("    → LaunchedEffect: setGroup(guid=$guid)")

                viewModel.setGroup(group)
                advanceUntilIdle()

                viewModel.uiState.value.shouldBeInstanceOf<MessageHeaderUIState.GroupContent>()
                viewModel.group.value?.guid shouldBe guid
                viewModel.memberCount.value shouldBe members
            }
        }
    }

    // ==================== Refresh Interaction ====================

    test("refresh user should update collectAsState for recomposition") {
        runTest {
            val viewModel = createViewModel()
            val initialUser = createMockUser(uid = "user-1", name = "Alice", status = CometChatConstants.USER_STATUS_OFFLINE)
            val refreshedUser = createMockUser(uid = "user-1", name = "Alice", status = CometChatConstants.USER_STATUS_ONLINE)
            whenever(getUserUseCase.invoke("user-1")).thenReturn(Result.success(refreshedUser))
            println("    → Refresh: offline → online for recomposition")

            viewModel.setUser(initialUser)
            advanceUntilIdle()

            viewModel.refreshUser("user-1")
            advanceUntilIdle()

            val state = viewModel.uiState.value as MessageHeaderUIState.UserContent
            state.user.status shouldBe CometChatConstants.USER_STATUS_ONLINE
            println("    → After refresh: status=${state.user.status}")
        }
    }

    test("refresh group should update memberCount for recomposition") {
        runTest {
            val viewModel = createViewModel()
            val initialGroup = createMockGroup(guid = "group-1", membersCount = 5)
            val refreshedGroup = createMockGroup(guid = "group-1", membersCount = 15)
            whenever(getGroupUseCase.invoke("group-1")).thenReturn(Result.success(refreshedGroup))
            println("    → Refresh: members 5 → 15 for recomposition")

            viewModel.setGroup(initialGroup)
            advanceUntilIdle()
            viewModel.memberCount.value shouldBe 5

            viewModel.refreshGroup("group-1")
            advanceUntilIdle()
            viewModel.memberCount.value shouldBe 15
        }
    }

    // ==================== Error Event Collection ====================

    test("error event should be collectible by LaunchedEffect for onError callback") {
        runTest {
            val viewModel = createViewModel()
            val exception = createMockException("ERR_REFRESH", "Failed")
            whenever(getUserUseCase.invoke("user-1")).thenReturn(Result.failure(exception))
            println("    → Error event for LaunchedEffect collection")

            viewModel.setUser(createMockUser(uid = "user-1"))
            advanceUntilIdle()

            var collectedError: CometChatException? = null
            val job = launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.errorEvent.collect { collectedError = it }
            }

            viewModel.refreshUser("user-1")
            advanceUntilIdle()

            collectedError?.code shouldBe "ERR_REFRESH"
            println("    → Error collected: code=${collectedError?.code}")
            job.cancel()
        }
    }

    // ==================== Rapid State Changes ====================

    test("rapid user/group switches should settle on last state for recomposition") {
        runTest {
            val viewModel = createViewModel()
            println("    → Rapid switches: user → group → user")

            viewModel.setUser(createMockUser(uid = "user-1", name = "Alice"))
            viewModel.setGroup(createMockGroup(guid = "group-1", name = "Devs"))
            viewModel.setUser(createMockUser(uid = "user-2", name = "Bob"))
            advanceUntilIdle()

            val state = viewModel.uiState.value as MessageHeaderUIState.UserContent
            state.user.uid shouldBe "user-2"
            state.user.name shouldBe "Bob"
            viewModel.group.value shouldBe null
            println("    → Settled: user=user-2 (Bob)")
        }
    }
})
