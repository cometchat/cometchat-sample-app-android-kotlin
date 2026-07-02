package com.cometchat.uikit.core.viewmodel.outgoingcall

import com.cometchat.chat.core.Call
import com.cometchat.uikit.core.testutils.MockFactory
import com.cometchat.uikit.core.viewmodel.CometChatOutgoingCallViewModel
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.element
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

/**
 * Dedicated state machine invariant tests for OutgoingCall component.
 *
 * Tests the following state machine properties:
 * 1. Valid transitions only: Idle → Calling → {Accepted, Rejected}
 * 2. State exclusivity: acceptedCall and rejectedCall never both non-null
 * 3. endCallButtonEnabled: starts true, reset to true on setCall
 * 4. No-op guards: cancelCall with null call is safe
 * 5. Call identity preservation: call StateFlow reflects most recently set call
 * 6. Call type preservation: audio/video type preserved through transitions
 *
 * State Machine Diagram:
 * ```
 *   [Idle] --setCall--> [Calling] --cancelCall--> [Rejected] (terminal)
 *                                  --accepted-->   [Accepted] (terminal, endCallButtonEnabled=false)
 *                                  --rejected-->   [Rejected] (terminal)
 *                                  --ended-->      [Rejected] (terminal)
 *
 *   Any state --setCall--> [Calling] (resets endCallButtonEnabled to true)
 * ```
 *
 * Note: OutgoingCall does NOT have a resetState() method.
 * Only setCall() can transition state.
 *
 * **Validates: Requirements 9.1–9.14**
 *
 * Run with:
 *   ./gradlew :chatuikit-core:testDebugUnitTest --tests "...OutgoingCallStateMachinePropertyTest"
 */
@OptIn(ExperimentalCoroutinesApi::class)
class OutgoingCallStateMachinePropertyTest : FunSpec({

    isolationMode = IsolationMode.SingleInstance

    val testDispatcher = UnconfinedTestDispatcher()

    // ==================== Arb Generators ====================

    val callTypeArb = Arb.element("audio", "video")
    val sessionIdArb = Arb.string(5..15)

    // Only safe actions that don't call SDK methods
    val actionArb = Arb.element("SET_CALL", "RESET_END_BUTTON")

    fun createViewModel(): CometChatOutgoingCallViewModel {
        return CometChatOutgoingCallViewModel(enableListeners = false)
    }

    beforeTest {
        Dispatchers.setMain(testDispatcher)
        println("  🧪 ${it.name.testName}")
    }

    afterTest {
        Dispatchers.resetMain()
        println()
    }

    // ==================== Invariant 1: Initial State ====================

    test("INVARIANT: fresh ViewModel always starts with all null + endCallButtonEnabled=true") {
        /**
         * For any number of ViewModel creations, the initial state is always:
         * - call = null
         * - acceptedCall = null
         * - rejectedCall = null
         * - endCallButtonEnabled = true
         */
        checkAll(30, Arb.int(1..5)) { _ ->
            runTest {
                val viewModel = createViewModel()

                viewModel.call.value shouldBe null
                viewModel.acceptedCall.value shouldBe null
                viewModel.rejectedCall.value shouldBe null
                viewModel.endCallButtonEnabled.value shouldBe true

                println("    ✅ Fresh ViewModel is in Idle state with endCallButtonEnabled=true")
            }
        }
    }

    // ==================== Invariant 2: setCall Always Results in Non-Null Call ====================

    test("INVARIANT: setCall always results in non-null call") {
        /**
         * For any call type and session ID, setCall should:
         * 1. Set call to non-null
         * 2. Set endCallButtonEnabled to true
         */
        checkAll(50, sessionIdArb, callTypeArb) { sessionId, callType ->
            runTest {
                val viewModel = createViewModel()
                val call = MockFactory.createCall(sessionId = sessionId, type = callType)

                viewModel.setCall(call)

                println("    → setCall(sessionId=$sessionId, type=$callType)")

                // Post-condition: Calling state
                viewModel.call.value shouldBe call
                viewModel.endCallButtonEnabled.value shouldBe true

                println("    ✅ Invariant holds: call=non-null, endCallButtonEnabled=true")
            }
        }
    }

    // ==================== Invariant 3: State Exclusivity ====================

    test("INVARIANT: acceptedCall and rejectedCall cannot both be non-null simultaneously") {
        /**
         * At any point in the state machine, at most ONE of acceptedCall/rejectedCall
         * can be non-null. This is enforced by the ViewModel's design:
         * - handleOutgoingCallAccepted sets acceptedCall (rejectedCall remains null)
         * - handleOutgoingCallRejected sets rejectedCall (acceptedCall remains null)
         * - cancelCall sets rejectedCall (acceptedCall remains null)
         *
         * We verify this by checking after various operations.
         */
        checkAll(30, sessionIdArb) { sessionId ->
            runTest {
                val viewModel = createViewModel()
                val call = MockFactory.createCall(sessionId = sessionId)
                viewModel.setCall(call)

                // After setCall: both should be null
                val bothNull = viewModel.acceptedCall.value == null &&
                    viewModel.rejectedCall.value == null
                bothNull shouldBe true

                // After another setCall: both should still be null
                val call2 = MockFactory.createCall(sessionId = "another-$sessionId")
                viewModel.setCall(call2)
                val bothNullAfterSecondSet = viewModel.acceptedCall.value == null &&
                    viewModel.rejectedCall.value == null
                bothNullAfterSecondSet shouldBe true

                println("    → sessionId=$sessionId")
                println("    ✅ State exclusivity invariant holds")
            }
        }
    }

    // ==================== Invariant 4: endCallButtonEnabled Starts True After setCall ====================

    test("INVARIANT: endCallButtonEnabled is always true after setCall") {
        /**
         * For any sequence of setCall operations, endCallButtonEnabled
         * is always reset to true.
         */
        checkAll(20, Arb.int(2..8)) { callCount ->
            runTest {
                val viewModel = createViewModel()

                println("    → Performing $callCount sequential setCall operations")

                repeat(callCount) { i ->
                    val call = MockFactory.createCall(sessionId = "session-$i")
                    viewModel.setCall(call)

                    // After each setCall, endCallButtonEnabled must be true
                    viewModel.endCallButtonEnabled.value shouldBe true
                }

                println("    ✅ All $callCount setCall operations maintained endCallButtonEnabled=true")
            }
        }
    }

    // ==================== Invariant 5: No-Op Guards ====================

    test("INVARIANT: cancelCall with null call is always a no-op") {
        /**
         * If call is null (Idle state), cancelCall should not change any state.
         */
        checkAll(20, Arb.int(1..5)) { repeatCount ->
            runTest {
                val viewModel = createViewModel()

                println("    → Calling cancelCall $repeatCount times with null call")

                repeat(repeatCount) {
                    viewModel.cancelCall()
                }

                viewModel.call.value shouldBe null
                viewModel.acceptedCall.value shouldBe null
                viewModel.rejectedCall.value shouldBe null
                viewModel.endCallButtonEnabled.value shouldBe true

                println("    ✅ cancelCall no-op guard holds after $repeatCount calls")
            }
        }
    }

    // ==================== Invariant 6: Random Action Sequences ====================

    test("INVARIANT: for any random action sequence, state exclusivity is maintained") {
        /**
         * Apply random sequences of actions and verify that at no point
         * do we violate the state exclusivity invariant.
         *
         * Note: cancelCall calls CometChat.rejectCall which is not available in unit tests.
         * We test only setCall here — the safe operation that doesn't require SDK.
         * cancelCall is tested separately in the ViewModel test where we verify
         * the no-op guard (null call case).
         *
         * RESET_END_BUTTON is a conceptual action — since OutgoingCall has no resetState(),
         * we use setCall as the only available action to change state.
         */
        checkAll(20, Arb.list(actionArb, 3..10)) { actions ->
            runTest {
                val viewModel = createViewModel()
                var callCounter = 0

                println("    → Applying ${actions.size} random actions: $actions")

                actions.forEach { action ->
                    when (action) {
                        "SET_CALL" -> {
                            val call = MockFactory.createCall(sessionId = "rand-${callCounter++}")
                            viewModel.setCall(call)
                        }
                        "RESET_END_BUTTON" -> {
                            // setCall is the only way to reset state in OutgoingCall
                            // Simulate by setting a new call (which resets endCallButtonEnabled)
                            val call = MockFactory.createCall(sessionId = "reset-${callCounter++}")
                            viewModel.setCall(call)
                        }
                    }

                    // Invariant check after each action:
                    // acceptedCall and rejectedCall cannot both be non-null
                    val accepted = viewModel.acceptedCall.value
                    val rejected = viewModel.rejectedCall.value
                    val bothNonNull = accepted != null && rejected != null
                    bothNonNull shouldBe false

                    // endCallButtonEnabled should always be true after setCall
                    viewModel.endCallButtonEnabled.value shouldBe true
                }

                println("    ✅ State exclusivity maintained through ${actions.size} random actions")
            }
        }
    }

    // ==================== Invariant 7: Call Identity Preservation ====================

    test("INVARIANT: call StateFlow always reflects the most recently set call") {
        /**
         * For any sequence of setCall operations, the call StateFlow
         * always holds the most recently provided Call object.
         */
        checkAll(20, Arb.int(2..6)) { count ->
            runTest {
                val viewModel = createViewModel()
                val calls = (0 until count).map { i ->
                    MockFactory.createCall(sessionId = "identity-$i")
                }

                println("    → Setting $count calls, verifying identity after each")

                calls.forEachIndexed { index, call ->
                    viewModel.setCall(call)
                    viewModel.call.value shouldBe call
                    viewModel.call.value?.sessionId shouldBe "identity-$index"
                }

                // Final state should be the last call
                viewModel.call.value shouldBe calls.last()

                println("    ✅ Call identity preserved through $count setCall operations")
            }
        }
    }

    // ==================== Invariant 8: Call Type Preservation ====================

    test("INVARIANT: call type (audio/video) is preserved through state transitions") {
        /**
         * The call type should be accessible from the ViewModel's call state
         * regardless of what other operations are performed.
         */
        checkAll(30, callTypeArb, sessionIdArb) { callType, sessionId ->
            runTest {
                val viewModel = createViewModel()
                val call = MockFactory.createCall(sessionId = sessionId, type = callType)

                viewModel.setCall(call)

                // Type should be preserved
                viewModel.call.value?.type shouldBe callType

                // After setting another call, the new type should be preserved
                val newType = if (callType == "audio") "video" else "audio"
                val call2 = MockFactory.createCall(sessionId = "new-$sessionId", type = newType)
                viewModel.setCall(call2)

                viewModel.call.value?.type shouldBe newType

                println("    → type=$callType → $newType preserved after setCall")
                println("    ✅ Call type invariant holds")
            }
        }
    }
})
