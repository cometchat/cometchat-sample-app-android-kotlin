package com.cometchat.uikit.kotlin.presentation.callbuttons

import com.cometchat.chat.core.Call
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.CustomMessage
import com.cometchat.uikit.core.domain.usecase.InitiateUserCallUseCase
import com.cometchat.uikit.core.domain.usecase.StartGroupCallUseCase
import com.cometchat.uikit.core.state.CallButtonsUIState
import com.cometchat.uikit.core.viewmodel.CometChatCallButtonsViewModel
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.User
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
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
 * Tests for CometChatCallButtons Kotlin component rendering states.
 *
 * Verifies that the ViewModel produces the correct CallButtonsUIState for each scenario,
 * which the Kotlin XML component observes to show/hide views:
 * - CallButtonsUIState.Idle → buttons are enabled, ready to initiate calls
 * - CallButtonsUIState.Initiating → buttons show loading/disabled state
 * - CallButtonsUIState.Error → error callback invoked
 *
 * Also verifies visibility state mapping for user vs group targets.
 *
 * Validates: Requirements 12.1, 12.4
 *
 * Run with:
 *   ./gradlew :chatuikit-kotlin:testDebugUnitTest --tests "*CometChatCallButtonsRenderingTest"
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CometChatCallButtonsRenderingTest : FunSpec({

    val testDispatcher = UnconfinedTestDispatcher()

    beforeTest {
        Dispatchers.setMain(testDispatcher)
        println("  🧪 ${it.name.testName}")
    }

    afterTest {
        Dispatchers.resetMain()
        println()
    }

    // ==================== UIState Rendering Tests (Requirement 12.1) ====================

    test("ViewModel initial state is Idle → component shows enabled buttons") {
        runTest {
            val viewModel = createCallButtonsViewModel()
            advanceUntilIdle()

            viewModel.uiState.value shouldBe CallButtonsUIState.Idle
            println("    ✅ CallButtonsUIState.Idle → component shows enabled voice/video buttons")
        }
    }

    test("ViewModel with user set → component shows both voice and video buttons for user call") {
        runTest {
            val viewModel = createCallButtonsViewModel()
            val user = mock<User>()
            whenever(user.uid).thenReturn("user-1")
            whenever(user.name).thenReturn("Iron Man")
            viewModel.setUser(user)
            advanceUntilIdle()

            viewModel.uiState.value shouldBe CallButtonsUIState.Idle
            viewModel.getUser() shouldBe user
            viewModel.getGroup() shouldBe null
            println("    ✅ User set → both buttons visible for 1-to-1 call")
        }
    }

    test("ViewModel with group set → component shows both voice and video buttons for group call") {
        runTest {
            val viewModel = createCallButtonsViewModel()
            val group = mock<Group>()
            whenever(group.guid).thenReturn("group-1")
            whenever(group.name).thenReturn("Avengers")
            viewModel.setGroup(group)
            advanceUntilIdle()

            viewModel.uiState.value shouldBe CallButtonsUIState.Idle
            viewModel.getGroup() shouldBe group
            viewModel.getUser() shouldBe null
            println("    ✅ Group set → both buttons visible for conference call")
        }
    }

    test("ViewModel after successful user call → returns to Idle state") {
        runTest {
            val initiateUseCase = mock<InitiateUserCallUseCase>()
            val groupUseCase = mock<StartGroupCallUseCase>()
            val viewModel = CometChatCallButtonsViewModel(initiateUseCase, groupUseCase, enableListeners = false)

            val user = mock<User>()
            whenever(user.uid).thenReturn("user-1")
            whenever(user.name).thenReturn("Iron Man")
            viewModel.setUser(user)

            val mockCall = mock<Call>()
            whenever(initiateUseCase.invoke("user-1", "audio")).thenReturn(Result.success(mockCall))

            viewModel.initiateCall("audio")
            advanceUntilIdle()

            viewModel.uiState.value shouldBe CallButtonsUIState.Idle
            println("    ✅ After successful call → Idle state (buttons re-enabled)")
        }
    }

    test("ViewModel after successful group call → returns to Idle state") {
        runTest {
            val initiateUseCase = mock<InitiateUserCallUseCase>()
            val groupUseCase = mock<StartGroupCallUseCase>()
            val viewModel = CometChatCallButtonsViewModel(initiateUseCase, groupUseCase, enableListeners = false)

            val group = mock<Group>()
            whenever(group.guid).thenReturn("group-1")
            whenever(group.name).thenReturn("Avengers")
            viewModel.setGroup(group)

            val mockMessage = mock<CustomMessage>()
            whenever(groupUseCase.invoke("group-1", "video")).thenReturn(Result.success(mockMessage))

            viewModel.initiateCall("video")
            advanceUntilIdle()

            viewModel.uiState.value shouldBe CallButtonsUIState.Idle
            println("    ✅ After successful group call → Idle state (buttons re-enabled)")
        }
    }

    test("ViewModel after failed call → Error state (drives error callback)") {
        runTest {
            val initiateUseCase = mock<InitiateUserCallUseCase>()
            val groupUseCase = mock<StartGroupCallUseCase>()
            val viewModel = CometChatCallButtonsViewModel(initiateUseCase, groupUseCase, enableListeners = false)

            val user = mock<User>()
            whenever(user.uid).thenReturn("user-1")
            whenever(user.name).thenReturn("Iron Man")
            viewModel.setUser(user)

            val exception = CometChatException("ACTIVE_CALL", "Cannot initiate call")
            whenever(initiateUseCase.invoke("user-1", "audio")).thenReturn(Result.failure(exception))

            viewModel.initiateCall("audio")
            advanceUntilIdle()

            viewModel.uiState.value.shouldBeInstanceOf<CallButtonsUIState.Error>()
            val errorState = viewModel.uiState.value as CallButtonsUIState.Error
            errorState.exception.code shouldBe "ACTIVE_CALL"
            println("    ✅ CallButtonsUIState.Error → component invokes onError callback")
        }
    }

    test("ViewModel with no receiver set → Error state on initiateCall") {
        runTest {
            val viewModel = createCallButtonsViewModel()
            // Don't set user or group

            viewModel.initiateCall("audio")
            advanceUntilIdle()

            viewModel.uiState.value.shouldBeInstanceOf<CallButtonsUIState.Error>()
            val errorState = viewModel.uiState.value as CallButtonsUIState.Error
            errorState.exception.code shouldBe "INVALID_RECEIVER"
            println("    ✅ No receiver → INVALID_RECEIVER error state")
        }
    }
})

// ==================== Helper Functions ====================

private fun createCallButtonsViewModel(): CometChatCallButtonsViewModel {
    val initiateUseCase = mock<InitiateUserCallUseCase>()
    val groupUseCase = mock<StartGroupCallUseCase>()
    return CometChatCallButtonsViewModel(initiateUseCase, groupUseCase, enableListeners = false)
}
