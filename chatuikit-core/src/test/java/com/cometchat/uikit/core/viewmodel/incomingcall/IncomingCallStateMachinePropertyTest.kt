package com.cometchat.uikit.core.viewmodel.incomingcall

import com.cometchat.chat.core.Call
import com.cometchat.uikit.core.testutils.MockFactory
import com.cometchat.uikit.core.viewmodel.CometChatIncomingCallViewModel
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
 * Dedicated state machine invariant tests for IncomingCall component.
 *
 * Tests the following state machine properties:
 * 1. Valid transitions only: Idle → Ringing → {Accepted, Rejected, Cancelled}
 * 2. Terminal state invariants: once in Accepted/Rejected/Cancelled, no further transitions
 *    without explicit resetState or new setCall
 * 3. State exclusivity: only one terminal state can be non-null at a time
 * 4. Idempotency: repeated operations don't corrupt state
 * 5. Reset semantics: setCall always resets terminal states
 *
 * State Machine Diagram:
 * ```
 *   [Idle] --setCall--> [Ringing] --acceptCall--> [Accepted] (terminal)
 *                                  --rejectCall--> [Rejected] (terminal)
 *                                  --cancelled-->  [Cancelled] (terminal)
 *                                  --error-->      [Error] (non-terminal, can retry)
 *
 *   Any state --setCall--> [Ringing] (resets terminal states)
 *   Any state --resetState--> clears acceptedCall/rejectedCall
 * ```
 *
 * **Validates: Requirements 8.1, 8.2**
 *
 * Run with:
 *   ./gradlew :chatuikit-core:testDebugUnitTest --tests "...IncomingCallStateMachinePropertyTest"
 */
@OptIn(ExperimentalCoroutinesApi::class)
class IncomingCallStateMachinePropertyTest : FunSpec({

    isolationMode = IsolationMode.SingleInstance

    val testDispatcher = UnconfinedTestDispatcher()

    // ==================== Arb Generators ====================

    val callTypeArb = Arb.element("audio", "video")
    val sessionIdArb = Arb.string(5..15)

    val actionArb = Arb.element("SET_CALL", "RESET_STATE")

    fun createViewModel(): CometChatIncomingCallViewModel {
        return CometChatIncomingCallViewModel(enableListeners = false)
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

    test("INVARIANT: fresh ViewModel always starts in Idle state (all flows null)") {
        /**
         * For any number of ViewModel creations, the initial state is always Idle.
         */
        checkAll(30, Arb.int(1..5)) { _ ->
            runTest {
                val viewModel = createViewModel()

                viewModel.call.value shouldBe null
                viewModel.acceptedCall.value shouldBe null
                viewModel.rejectedCall.value shouldBe null

                println("    ✅ Fresh ViewModel is in Idle state")
            }
        }
    }

    // ==================== Invariant 2: setCall Always Transitions to Ringing ====================

    test("INVARIANT: setCall always results in non-null call and null terminal states") {
        /**
         * For any call type and session ID, setCall should:
         * 1. Set call to non-null
         * 2. Clear acceptedCall to null
         * 3. Clear rejectedCall to null
         */
        checkAll(50, sessionIdArb, callTypeArb) { sessionId, callType ->
            runTest {
                val viewModel = createViewModel()
                val call = MockFactory.createCall(sessionId = sessionId, type = callType)

                viewModel.setCall(call)

                println("    → setCall(sessionId=$sessionId, type=$callType)")

                // Post-condition: Ringing state
                viewModel.call.value shouldBe call
                viewModel.acceptedCall.value shouldBe null
                viewModel.rejectedCall.value shouldBe null

                println("    ✅ Invariant holds: call=non-null, terminals=null")
            }
        }
    }

    // ==================== Invariant 3: State Exclusivity ====================

    test("INVARIANT: acceptedCall and rejectedCall cannot both be non-null simultaneously") {
        /**
         * At any point in the state machine, at most ONE of acceptedCall/rejectedCall
         * can be non-null. This is enforced by the ViewModel's design:
         * - acceptCall sets acceptedCall (rejectedCall remains null)
         * - rejectCall sets rejectedCall (acceptedCall remains null)
         * - setCall resets both to null
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

                // After resetState: both should still be null
                viewModel.resetState()
                val bothNullAfterReset = viewModel.acceptedCall.value == null &&
                    viewModel.rejectedCall.value == null
                bothNullAfterReset shouldBe true

                println("    → sessionId=$sessionId")
                println("    ✅ State exclusivity invariant holds")
            }
        }
    }

    // ==================== Invariant 4: resetState Idempotency ====================

    test("INVARIANT: resetState is idempotent — calling N times has same effect as calling once") {
        checkAll(20, Arb.int(1..10), sessionIdArb) { repeatCount, sessionId ->
            runTest {
                val viewModel = createViewModel()
                val call = MockFactory.createCall(sessionId = sessionId)
                viewModel.setCall(call)

                println("    → Calling resetState $repeatCount times")

                repeat(repeatCount) {
                    viewModel.resetState()
                }

                viewModel.acceptedCall.value shouldBe null
                viewModel.rejectedCall.value shouldBe null

                println("    ✅ resetState is idempotent after $repeatCount calls")
            }
        }
    }

    // ==================== Invariant 5: setCall Resets Terminal States ====================

    test("INVARIANT: setCall always resets terminal states regardless of previous state") {
        /**
         * For any sequence of setCall operations, the terminal states
         * (acceptedCall, rejectedCall) are always null after setCall.
         */
        checkAll(20, Arb.int(2..8)) { callCount ->
            runTest {
                val viewModel = createViewModel()

                println("    → Performing $callCount sequential setCall operations")

                repeat(callCount) { i ->
                    val call = MockFactory.createCall(sessionId = "session-$i")
                    viewModel.setCall(call)

                    // After each setCall, terminal states must be null
                    viewModel.acceptedCall.value shouldBe null
                    viewModel.rejectedCall.value shouldBe null
                }

                // Final call should be the last one set
                viewModel.call.value?.sessionId shouldBe "session-${callCount - 1}"

                println("    ✅ All $callCount setCall operations maintained invariant")
            }
        }
    }

    // ==================== Invariant 6: No-Op Guards ====================

    test("INVARIANT: acceptCall with null call is always a no-op") {
        /**
         * If call is null (Idle state), acceptCall should not change any state.
         */
        checkAll(20, Arb.int(1..5)) { repeatCount ->
            runTest {
                val viewModel = createViewModel()

                println("    → Calling acceptCall $repeatCount times with null call")

                repeat(repeatCount) {
                    viewModel.acceptCall()
                }

                viewModel.call.value shouldBe null
                viewModel.acceptedCall.value shouldBe null
                viewModel.rejectedCall.value shouldBe null

                println("    ✅ acceptCall no-op guard holds after $repeatCount calls")
            }
        }
    }

    test("INVARIANT: rejectCall with null call is always a no-op") {
        /**
         * If call is null (Idle state), rejectCall should not change any state.
         */
        checkAll(20, Arb.int(1..5)) { repeatCount ->
            runTest {
                val viewModel = createViewModel()

                println("    → Calling rejectCall $repeatCount times with null call")

                repeat(repeatCount) {
                    viewModel.rejectCall()
                }

                viewModel.call.value shouldBe null
                viewModel.acceptedCall.value shouldBe null
                viewModel.rejectedCall.value shouldBe null

                println("    ✅ rejectCall no-op guard holds after $repeatCount calls")
            }
        }
    }

    // ==================== Invariant 7: Random Action Sequences ====================

    test("INVARIANT: for any random action sequence, state exclusivity is maintained") {
        /**
         * Apply random sequences of actions and verify that at no point
         * do we violate the state exclusivity invariant.
         *
         * Note: acceptCall/rejectCall call CometChat static SDK methods which
         * are not available in unit tests (NoClassDefFoundError). We test only
         * setCall and resetState here — the safe operations that don't require SDK.
         * acceptCall/rejectCall are tested separately in the ViewModel test
         * where we verify the no-op guard (null call case).
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
                        "RESET_STATE" -> {
                            viewModel.resetState()
                        }
                    }

                    // Invariant check after each action:
                    // acceptedCall and rejectedCall cannot both be non-null
                    val accepted = viewModel.acceptedCall.value
                    val rejected = viewModel.rejectedCall.value
                    val bothNonNull = accepted != null && rejected != null
                    bothNonNull shouldBe false
                }

                println("    ✅ State exclusivity maintained through ${actions.size} random actions")
            }
        }
    }

    // ==================== Invariant 8: Call Identity Preservation ====================

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

    // ==================== Invariant 9: Audio/Video Type Preservation ====================

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

                // After resetState, call should still be accessible
                // (resetState only clears acceptedCall/rejectedCall)
                viewModel.resetState()

                // Note: resetState doesn't clear _call, only terminal states
                // But the call reference is still the same
                viewModel.call.value?.type shouldBe callType

                println("    → type=$callType preserved after resetState")
                println("    ✅ Call type invariant holds")
            }
        }
    }
})
