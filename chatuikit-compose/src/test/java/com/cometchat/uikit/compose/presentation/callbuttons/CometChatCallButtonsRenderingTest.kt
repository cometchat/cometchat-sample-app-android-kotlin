package com.cometchat.uikit.compose.presentation.callbuttons

import com.cometchat.chat.core.Call
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.CustomMessage
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.domain.usecase.InitiateUserCallUseCase
import com.cometchat.uikit.core.domain.usecase.StartGroupCallUseCase
import com.cometchat.uikit.core.state.CallButtonsUIState
import com.cometchat.uikit.core.viewmodel.CometChatCallButtonsViewModel
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.element
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
 * Tests for CometChatCallButtons Compose component rendering states.
 *
 * Verifies that the ViewModel produces the correct CallButtonsUIState for each scenario,
 * which the Compose component observes to show/hide composables:
 * - CallButtonsUIState.Idle → component shows enabled voice/video buttons
 * - CallButtonsUIState.Initiating → component shows loading/disabled state
 * - CallButtonsUIState.Error → component invokes onError callback
 *
 * Also verifies visibility state mapping for user vs group targets.
 *
 * Mirrors: chatuikit-kotlin CometChatCallButtonsRenderingTest
 *
 * Validates: Requirements 15.1, 15.4
 *
 * Run with:
 *   ./gradlew :chatuikit-compose:testDebugUnitTest --tests "*.callbuttons.CometChatCallButtonsRenderingTest"
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

    // ==================== UIState Rendering Tests (Requirement 15.1) ====================

    test("ViewModel initial state is Idle → composable shows enabled buttons") {
        println("=== TEST: Initial state → Idle ===")
        runTest {
            val viewModel = createCallButtonsViewModel()
            advanceUntilIdle()

            viewModel.uiState.value shouldBe CallButtonsUIState.Idle
            println("    ✅ CallButtonsUIState.Idle → composable shows enabled voice/video buttons")
        }
    }

    test("ViewModel with user set → composable shows both voice and video buttons for user call") {
        println("=== TEST: User set → both buttons visible ===")
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

    test("ViewModel with group set → composable shows both voice and video buttons for group call") {
        println("=== TEST: Group set → both buttons visible ===")
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

    test("ViewModel after successful user call → returns to Idle state (buttons re-enabled)") {
        println("=== TEST: Successful user call → Idle ===")
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
        println("=== TEST: Successful group call → Idle ===")
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

    test("ViewModel after failed call → Error state (drives error composable/callback)") {
        println("=== TEST: Failed call → Error state ===")
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
            println("    ✅ CallButtonsUIState.Error → composable invokes onError callback")
        }
    }

    test("ViewModel with no receiver set → Error state on initiateCall") {
        println("=== TEST: No receiver → Error ===")
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

    // ==================== PBT: Call Type Rendering (Requirement 15.4) ====================

    test("PBT: Any call type (audio/video) with user → Idle after success") {
        println("=== PBT: Call type → Idle after success ===")
        checkAll(20, Arb.element("audio", "video")) { callType ->
            runTest {
                val initiateUseCase = mock<InitiateUserCallUseCase>()
                val groupUseCase = mock<StartGroupCallUseCase>()
                val viewModel = CometChatCallButtonsViewModel(initiateUseCase, groupUseCase, enableListeners = false)

                val user = mock<User>()
                whenever(user.uid).thenReturn("user-1")
                whenever(user.name).thenReturn("Iron Man")
                viewModel.setUser(user)

                val mockCall = mock<Call>()
                whenever(initiateUseCase.invoke("user-1", callType)).thenReturn(Result.success(mockCall))

                viewModel.initiateCall(callType)
                advanceUntilIdle()

                viewModel.uiState.value shouldBe CallButtonsUIState.Idle
                println("  [Iteration] callType=$callType → Idle ✅")
            }
        }
    }

    test("PBT: Any call type (audio/video) with group → Idle after success") {
        println("=== PBT: Group call type → Idle after success ===")
        checkAll(20, Arb.element("audio", "video")) { callType ->
            runTest {
                val initiateUseCase = mock<InitiateUserCallUseCase>()
                val groupUseCase = mock<StartGroupCallUseCase>()
                val viewModel = CometChatCallButtonsViewModel(initiateUseCase, groupUseCase, enableListeners = false)

                val group = mock<Group>()
                whenever(group.guid).thenReturn("group-1")
                whenever(group.name).thenReturn("Avengers")
                viewModel.setGroup(group)

                val mockMessage = mock<CustomMessage>()
                whenever(groupUseCase.invoke("group-1", callType)).thenReturn(Result.success(mockMessage))

                viewModel.initiateCall(callType)
                advanceUntilIdle()

                viewModel.uiState.value shouldBe CallButtonsUIState.Idle
                println("  [Iteration] callType=$callType (group) → Idle ✅")
            }
        }
    }

    test("PBT: Any call type with no receiver → always Error") {
        println("=== PBT: No receiver → always Error ===")
        checkAll(20, Arb.element("audio", "video")) { callType ->
            runTest {
                val viewModel = createCallButtonsViewModel()

                viewModel.initiateCall(callType)
                advanceUntilIdle()

                viewModel.uiState.value.shouldBeInstanceOf<CallButtonsUIState.Error>()
                val errorState = viewModel.uiState.value as CallButtonsUIState.Error
                errorState.exception.code shouldBe "INVALID_RECEIVER"
                println("  [Iteration] callType=$callType, no receiver → Error ✅")
            }
        }
    }
})

// ==================== Helper Functions ====================

private fun createCallButtonsViewModel(): CometChatCallButtonsViewModel {
    val initiateUseCase = mock<InitiateUserCallUseCase>()
    val groupUseCase = mock<StartGroupCallUseCase>()
    return CometChatCallButtonsViewModel(initiateUseCase, groupUseCase, enableListeners = false)
}
