package com.cometchat.uikit.compose.presentation.callbuttons

import com.cometchat.chat.core.Call
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.CustomMessage
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.domain.usecase.InitiateUserCallUseCase
import com.cometchat.uikit.core.domain.usecase.StartGroupCallUseCase
import com.cometchat.uikit.core.state.CallButtonsEvent
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
import kotlinx.coroutines.flow.toList
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
 * Tests for CometChatCallButtons Compose component interaction behavior.
 *
 * Verifies that button tap interactions produce correct ViewModel state changes
 * and event emissions that the Compose component observes:
 * - Voice call button tap → initiateCall("audio") → CallInitiated event
 * - Video call button tap → initiateCall("video") → CallInitiated/StartDirectCall event
 * - Error scenarios → error event emitted
 * - Switching receiver → correct use case invoked
 *
 * Mirrors: chatuikit-kotlin CometChatCallButtonsInteractionTest
 *
 * Validates: Requirements 15.2, 15.4
 *
 * Run with:
 *   ./gradlew :chatuikit-compose:testDebugUnitTest --tests "*.callbuttons.CometChatCallButtonsInteractionTest"
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CometChatCallButtonsInteractionTest : FunSpec({

    val testDispatcher = UnconfinedTestDispatcher()

    beforeTest {
        Dispatchers.setMain(testDispatcher)
        println("  🧪 ${it.name.testName}")
    }

    afterTest {
        Dispatchers.resetMain()
        println()
    }

    // ==================== Voice Call Button Tap (Requirement 15.2) ====================

    test("voice call button tap with user → initiates audio call and emits CallInitiated") {
        println("=== TEST: Voice call tap (user) ===")
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

            // Collect events
            val collectedEvents = mutableListOf<CallButtonsEvent>()
            val job = launch(testDispatcher) {
                viewModel.events.toList(collectedEvents)
            }

            // Simulate voice call button tap
            viewModel.initiateCall("audio")
            advanceUntilIdle()

            verify(initiateUseCase).invoke("user-1", "audio")
            collectedEvents.size shouldBe 1
            collectedEvents[0].shouldBeInstanceOf<CallButtonsEvent.CallInitiated>()
            (collectedEvents[0] as CallButtonsEvent.CallInitiated).call shouldBe mockCall
            viewModel.uiState.value shouldBe CallButtonsUIState.Idle

            job.cancel()
            println("    ✅ Voice call tap → audio call initiated, CallInitiated event emitted")
        }
    }

    // ==================== Video Call Button Tap (Requirement 15.2) ====================

    test("video call button tap with user → initiates video call and emits CallInitiated") {
        println("=== TEST: Video call tap (user) ===")
        runTest {
            val initiateUseCase = mock<InitiateUserCallUseCase>()
            val groupUseCase = mock<StartGroupCallUseCase>()
            val viewModel = CometChatCallButtonsViewModel(initiateUseCase, groupUseCase, enableListeners = false)

            val user = mock<User>()
            whenever(user.uid).thenReturn("user-1")
            whenever(user.name).thenReturn("Iron Man")
            viewModel.setUser(user)

            val mockCall = mock<Call>()
            whenever(initiateUseCase.invoke("user-1", "video")).thenReturn(Result.success(mockCall))

            // Collect events
            val collectedEvents = mutableListOf<CallButtonsEvent>()
            val job = launch(testDispatcher) {
                viewModel.events.toList(collectedEvents)
            }

            // Simulate video call button tap
            viewModel.initiateCall("video")
            advanceUntilIdle()

            verify(initiateUseCase).invoke("user-1", "video")
            collectedEvents.size shouldBe 1
            collectedEvents[0].shouldBeInstanceOf<CallButtonsEvent.CallInitiated>()
            viewModel.uiState.value shouldBe CallButtonsUIState.Idle

            job.cancel()
            println("    ✅ Video call tap → video call initiated, CallInitiated event emitted")
        }
    }

    // ==================== Group Call Button Tap (Requirement 15.2) ====================

    test("voice call button tap with group → starts group call and emits StartDirectCall") {
        println("=== TEST: Voice call tap (group) ===")
        runTest {
            val initiateUseCase = mock<InitiateUserCallUseCase>()
            val groupUseCase = mock<StartGroupCallUseCase>()
            val viewModel = CometChatCallButtonsViewModel(initiateUseCase, groupUseCase, enableListeners = false)

            val group = mock<Group>()
            whenever(group.guid).thenReturn("group-1")
            whenever(group.name).thenReturn("Avengers")
            viewModel.setGroup(group)

            val mockMessage = mock<CustomMessage>()
            whenever(groupUseCase.invoke("group-1", "audio")).thenReturn(Result.success(mockMessage))

            // Collect events
            val collectedEvents = mutableListOf<CallButtonsEvent>()
            val job = launch(testDispatcher) {
                viewModel.events.toList(collectedEvents)
            }

            // Simulate voice call button tap for group
            viewModel.initiateCall("audio")
            advanceUntilIdle()

            verify(groupUseCase).invoke("group-1", "audio")
            collectedEvents.size shouldBe 1
            collectedEvents[0].shouldBeInstanceOf<CallButtonsEvent.StartDirectCall>()
            (collectedEvents[0] as CallButtonsEvent.StartDirectCall).message shouldBe mockMessage
            viewModel.uiState.value shouldBe CallButtonsUIState.Idle

            job.cancel()
            println("    ✅ Voice call tap (group) → group call started, StartDirectCall event emitted")
        }
    }

    test("video call button tap with group → starts group video call and emits StartDirectCall") {
        println("=== TEST: Video call tap (group) ===")
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

            // Collect events
            val collectedEvents = mutableListOf<CallButtonsEvent>()
            val job = launch(testDispatcher) {
                viewModel.events.toList(collectedEvents)
            }

            // Simulate video call button tap for group
            viewModel.initiateCall("video")
            advanceUntilIdle()

            verify(groupUseCase).invoke("group-1", "video")
            collectedEvents.size shouldBe 1
            collectedEvents[0].shouldBeInstanceOf<CallButtonsEvent.StartDirectCall>()
            viewModel.uiState.value shouldBe CallButtonsUIState.Idle

            job.cancel()
            println("    ✅ Video call tap (group) → group video call started, StartDirectCall event emitted")
        }
    }

    // ==================== Error Interaction (Requirement 15.4) ====================

    test("call button tap when active call exists → emits error event") {
        println("=== TEST: Active call → error event ===")
        runTest {
            val initiateUseCase = mock<InitiateUserCallUseCase>()
            val groupUseCase = mock<StartGroupCallUseCase>()
            val viewModel = CometChatCallButtonsViewModel(initiateUseCase, groupUseCase, enableListeners = false)

            val user = mock<User>()
            whenever(user.uid).thenReturn("user-1")
            whenever(user.name).thenReturn("Iron Man")
            viewModel.setUser(user)

            val exception = CometChatException("ACTIVE_CALL", "Cannot initiate call while another call is active")
            whenever(initiateUseCase.invoke("user-1", "audio")).thenReturn(Result.failure(exception))

            // Collect error events
            val collectedErrors = mutableListOf<CometChatException>()
            val job = launch(testDispatcher) {
                viewModel.errorEvent.toList(collectedErrors)
            }

            // Simulate button tap when active call exists
            viewModel.initiateCall("audio")
            advanceUntilIdle()

            viewModel.uiState.value.shouldBeInstanceOf<CallButtonsUIState.Error>()
            collectedErrors.size shouldBe 1
            collectedErrors[0].code shouldBe "ACTIVE_CALL"

            job.cancel()
            println("    ✅ Button tap with active call → error event emitted to onError callback")
        }
    }

    test("group call failure → emits error event with correct code") {
        println("=== TEST: Group call failure → error event ===")
        runTest {
            val initiateUseCase = mock<InitiateUserCallUseCase>()
            val groupUseCase = mock<StartGroupCallUseCase>()
            val viewModel = CometChatCallButtonsViewModel(initiateUseCase, groupUseCase, enableListeners = false)

            val group = mock<Group>()
            whenever(group.guid).thenReturn("group-1")
            whenever(group.name).thenReturn("Avengers")
            viewModel.setGroup(group)

            val exception = CometChatException("GROUP_CALL_ERR", "Failed to start group call")
            whenever(groupUseCase.invoke("group-1", "video")).thenReturn(Result.failure(exception))

            // Collect error events
            val collectedErrors = mutableListOf<CometChatException>()
            val job = launch(testDispatcher) {
                viewModel.errorEvent.toList(collectedErrors)
            }

            viewModel.initiateCall("video")
            advanceUntilIdle()

            viewModel.uiState.value.shouldBeInstanceOf<CallButtonsUIState.Error>()
            collectedErrors.size shouldBe 1
            collectedErrors[0].code shouldBe "GROUP_CALL_ERR"

            job.cancel()
            println("    ✅ Group call failure → error event emitted with GROUP_CALL_ERR")
        }
    }

    // ==================== Receiver Switching (Requirement 15.2) ====================

    test("switching from user to group and tapping → calls correct use case") {
        println("=== TEST: Switch user→group → correct use case ===")
        runTest {
            val initiateUseCase = mock<InitiateUserCallUseCase>()
            val groupUseCase = mock<StartGroupCallUseCase>()
            val viewModel = CometChatCallButtonsViewModel(initiateUseCase, groupUseCase, enableListeners = false)

            // First set user
            val user = mock<User>()
            whenever(user.uid).thenReturn("user-1")
            whenever(user.name).thenReturn("Iron Man")
            viewModel.setUser(user)

            // Then switch to group
            val group = mock<Group>()
            whenever(group.guid).thenReturn("group-1")
            whenever(group.name).thenReturn("Avengers")
            viewModel.setGroup(group)

            val mockMessage = mock<CustomMessage>()
            whenever(groupUseCase.invoke("group-1", "video")).thenReturn(Result.success(mockMessage))

            // Tap video call button — should use group use case
            viewModel.initiateCall("video")
            advanceUntilIdle()

            verify(groupUseCase).invoke("group-1", "video")
            viewModel.uiState.value shouldBe CallButtonsUIState.Idle
            println("    ✅ After switching user→group, tap uses group use case")
        }
    }

    test("switching from group to user and tapping → calls correct use case") {
        println("=== TEST: Switch group→user → correct use case ===")
        runTest {
            val initiateUseCase = mock<InitiateUserCallUseCase>()
            val groupUseCase = mock<StartGroupCallUseCase>()
            val viewModel = CometChatCallButtonsViewModel(initiateUseCase, groupUseCase, enableListeners = false)

            // First set group
            val group = mock<Group>()
            whenever(group.guid).thenReturn("group-1")
            whenever(group.name).thenReturn("Avengers")
            viewModel.setGroup(group)

            // Then switch to user
            val user = mock<User>()
            whenever(user.uid).thenReturn("user-1")
            whenever(user.name).thenReturn("Iron Man")
            viewModel.setUser(user)

            val mockCall = mock<Call>()
            whenever(initiateUseCase.invoke("user-1", "audio")).thenReturn(Result.success(mockCall))

            // Tap audio call button — should use user use case
            viewModel.initiateCall("audio")
            advanceUntilIdle()

            verify(initiateUseCase).invoke("user-1", "audio")
            viewModel.uiState.value shouldBe CallButtonsUIState.Idle
            println("    ✅ After switching group→user, tap uses user use case")
        }
    }

    // ==================== PBT: Interaction Invariants ====================

    test("PBT: Any call type with user → always emits exactly 1 CallInitiated event on success") {
        println("=== PBT: User call → 1 event ===")
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

                val collectedEvents = mutableListOf<CallButtonsEvent>()
                val job = launch(testDispatcher) {
                    viewModel.events.toList(collectedEvents)
                }

                viewModel.initiateCall(callType)
                advanceUntilIdle()

                collectedEvents.size shouldBe 1
                collectedEvents[0].shouldBeInstanceOf<CallButtonsEvent.CallInitiated>()

                job.cancel()
                println("  [Iteration] callType=$callType → 1 CallInitiated event ✅")
            }
        }
    }

    test("PBT: Any call type with group → always emits exactly 1 StartDirectCall event on success") {
        println("=== PBT: Group call → 1 event ===")
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

                val collectedEvents = mutableListOf<CallButtonsEvent>()
                val job = launch(testDispatcher) {
                    viewModel.events.toList(collectedEvents)
                }

                viewModel.initiateCall(callType)
                advanceUntilIdle()

                collectedEvents.size shouldBe 1
                collectedEvents[0].shouldBeInstanceOf<CallButtonsEvent.StartDirectCall>()

                job.cancel()
                println("  [Iteration] callType=$callType → 1 StartDirectCall event ✅")
            }
        }
    }

    test("PBT: Any call type failure → always emits exactly 1 error event") {
        println("=== PBT: Failure → 1 error event ===")
        checkAll(20, Arb.element("audio", "video")) { callType ->
            runTest {
                val initiateUseCase = mock<InitiateUserCallUseCase>()
                val groupUseCase = mock<StartGroupCallUseCase>()
                val viewModel = CometChatCallButtonsViewModel(initiateUseCase, groupUseCase, enableListeners = false)

                val user = mock<User>()
                whenever(user.uid).thenReturn("user-1")
                whenever(user.name).thenReturn("Iron Man")
                viewModel.setUser(user)

                val exception = CometChatException("CALL_ERR", "Call failed")
                whenever(initiateUseCase.invoke("user-1", callType)).thenReturn(Result.failure(exception))

                val collectedErrors = mutableListOf<CometChatException>()
                val job = launch(testDispatcher) {
                    viewModel.errorEvent.toList(collectedErrors)
                }

                viewModel.initiateCall(callType)
                advanceUntilIdle()

                collectedErrors.size shouldBe 1
                collectedErrors[0].code shouldBe "CALL_ERR"

                job.cancel()
                println("  [Iteration] callType=$callType → 1 error event ✅")
            }
        }
    }
})
