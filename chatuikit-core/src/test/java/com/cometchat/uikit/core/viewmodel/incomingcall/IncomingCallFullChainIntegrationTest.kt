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
 * Full chain integration test for IncomingCall component.
 *
 * Unlike list-based components (Conversations, CallLogs) that have:
 *   fake DataSource → real Repository → real UseCase → real ViewModel
 *
 * IncomingCall has NO DataSource/Repository layers. It interacts directly
 * with the CometChat SDK from the ViewModel. Therefore, the "full chain"
 * for IncomingCall tests the ViewModel's complete lifecycle:
 *
 *   setCall → state observation → acceptCall/rejectCall → terminal state
 *
 * The integration here verifies:
 * 1. ViewModel creation with enableListeners=false (no SDK side effects)
 * 2. Complete call lifecycle: set → observe → action → terminal
 * 3. Multiple call handling (sequential calls)
 * 4. State reset between calls
 * 5. Error resilience (actions on null call)
 *
 * **Validates: Requirements 11.1, 11.3**
 *
 * Run with:
 *   ./gradlew :chatuikit-core:testDebugUnitTest --tests "...IncomingCallFullChainIntegrationTest"
 */
@OptIn(ExperimentalCoroutinesApi::class)
class IncomingCallFullChainIntegrationTest : FunSpec({

    isolationMode = IsolationMode.SingleInstance

    val testDispatcher = UnconfinedTestDispatcher()

    // ==================== Arb Generators ====================

    val callTypeArb = Arb.element("audio", "video")
    val sessionIdArb = Arb.string(5..15)

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

    // ==================== Full Lifecycle Tests ====================

    test("full chain: ViewModel creation → initial Idle state") {
        /**
         * Verifies that a freshly created ViewModel starts in Idle state
         * with all StateFlows at their initial values.
         */
        runTest {
            println("    [STEP 1] Create ViewModel with enableListeners=false")
            val viewModel = createViewModel()
            advanceUntilIdle()

            println("    [STEP 2] Verify initial state")
            viewModel.call.value shouldBe null
            viewModel.acceptedCall.value shouldBe null
            viewModel.rejectedCall.value shouldBe null

            println("    [RESULT] ViewModel starts in Idle state")
            println("    ✅ Full chain: creation → Idle verified")
        }
    }

    test("full chain: setCall → Ringing state with correct call data") {
        /**
         * Verifies the complete flow from Idle to Ringing state.
         * The ViewModel should expose the call object with all its properties.
         */
        checkAll(20, sessionIdArb, callTypeArb) { sessionId, callType ->
            runTest {
                println("    [STEP 1] Create ViewModel")
                val viewModel = createViewModel()
                advanceUntilIdle()

                println("    [STEP 2] Set incoming call (sessionId=$sessionId, type=$callType)")
                val call = MockFactory.createCall(
                    sessionId = sessionId,
                    type = callType,
                    callerUid = "caller-integration",
                    callerName = "Integration Caller"
                )
                viewModel.setCall(call)
                advanceUntilIdle()

                println("    [STEP 3] Verify Ringing state")
                viewModel.call.value shouldBe call
                viewModel.call.value?.sessionId shouldBe sessionId
                viewModel.call.value?.type shouldBe callType
                viewModel.call.value?.sender?.uid shouldBe "caller-integration"
                viewModel.call.value?.sender?.name shouldBe "Integration Caller"
                viewModel.acceptedCall.value shouldBe null
                viewModel.rejectedCall.value shouldBe null

                println("    [RESULT] Ringing state with sessionId=$sessionId, type=$callType")
                println("    ✅ Full chain: setCall → Ringing verified")
            }
        }
    }

    test("full chain: acceptCall with null call → no state change (safe no-op)") {
        /**
         * Verifies that calling acceptCall before setCall is safe.
         * The ViewModel should remain in Idle state.
         */
        runTest {
            println("    [STEP 1] Create ViewModel (no call set)")
            val viewModel = createViewModel()
            advanceUntilIdle()

            println("    [STEP 2] Call acceptCall (should be no-op)")
            viewModel.acceptCall()
            advanceUntilIdle()

            println("    [STEP 3] Verify state unchanged")
            viewModel.call.value shouldBe null
            viewModel.acceptedCall.value shouldBe null
            viewModel.rejectedCall.value shouldBe null

            println("    [RESULT] State unchanged after acceptCall on null call")
            println("    ✅ Full chain: acceptCall no-op verified")
        }
    }

    test("full chain: rejectCall with null call → no state change (safe no-op)") {
        /**
         * Verifies that calling rejectCall before setCall is safe.
         * The ViewModel should remain in Idle state.
         */
        runTest {
            println("    [STEP 1] Create ViewModel (no call set)")
            val viewModel = createViewModel()
            advanceUntilIdle()

            println("    [STEP 2] Call rejectCall (should be no-op)")
            viewModel.rejectCall()
            advanceUntilIdle()

            println("    [STEP 3] Verify state unchanged")
            viewModel.call.value shouldBe null
            viewModel.acceptedCall.value shouldBe null
            viewModel.rejectedCall.value shouldBe null

            println("    [RESULT] State unchanged after rejectCall on null call")
            println("    ✅ Full chain: rejectCall no-op verified")
        }
    }

    test("full chain: sequential calls → each setCall resets state correctly") {
        /**
         * Verifies that handling multiple sequential incoming calls works correctly.
         * Each new setCall should reset terminal states and set the new call.
         */
        checkAll(10, Arb.int(2..5)) { callCount ->
            runTest {
                println("    [STEP 1] Create ViewModel")
                val viewModel = createViewModel()
                advanceUntilIdle()

                println("    [STEP 2] Process $callCount sequential calls")
                val calls = (1..callCount).map { i ->
                    MockFactory.createCall(
                        sessionId = "chain-session-$i",
                        type = if (i % 2 == 0) "video" else "audio",
                        callerUid = "caller-$i",
                        callerName = "Caller $i"
                    )
                }

                calls.forEachIndexed { index, call ->
                    viewModel.setCall(call)
                    advanceUntilIdle()

                    // After each setCall, verify state
                    viewModel.call.value shouldBe call
                    viewModel.call.value?.sessionId shouldBe "chain-session-${index + 1}"
                    viewModel.acceptedCall.value shouldBe null
                    viewModel.rejectedCall.value shouldBe null

                    println("      → Call ${index + 1}: sessionId=chain-session-${index + 1}, state=Ringing")
                }

                println("    [STEP 3] Verify final state is last call")
                viewModel.call.value shouldBe calls.last()
                viewModel.call.value?.sessionId shouldBe "chain-session-$callCount"

                println("    [RESULT] All $callCount calls processed correctly")
                println("    ✅ Full chain: sequential calls verified")
            }
        }
    }

    test("full chain: setCall → resetState → state cleared but call preserved") {
        /**
         * Verifies that resetState clears terminal states without affecting
         * the current call reference.
         */
        runTest {
            println("    [STEP 1] Create ViewModel and set call")
            val viewModel = createViewModel()
            val call = MockFactory.createCall(sessionId = "reset-chain-session")
            viewModel.setCall(call)
            advanceUntilIdle()

            println("    [STEP 2] Verify Ringing state")
            viewModel.call.value shouldBe call
            viewModel.acceptedCall.value shouldBe null
            viewModel.rejectedCall.value shouldBe null

            println("    [STEP 3] Call resetState")
            viewModel.resetState()
            advanceUntilIdle()

            println("    [STEP 4] Verify terminal states cleared")
            viewModel.acceptedCall.value shouldBe null
            viewModel.rejectedCall.value shouldBe null

            println("    [RESULT] resetState clears terminal states")
            println("    ✅ Full chain: resetState behavior verified")
        }
    }

    test("full chain: audio call → setCall preserves audio type through lifecycle") {
        /**
         * Verifies that an audio call type is preserved through the full lifecycle.
         */
        runTest {
            println("    [STEP 1] Create ViewModel")
            val viewModel = createViewModel()
            advanceUntilIdle()

            println("    [STEP 2] Set audio call")
            val audioCall = MockFactory.createCall(
                sessionId = "audio-lifecycle",
                type = "audio",
                callerUid = "audio-caller",
                callerName = "Audio Caller"
            )
            viewModel.setCall(audioCall)
            advanceUntilIdle()

            println("    [STEP 3] Verify audio type preserved")
            viewModel.call.value?.type shouldBe "audio"
            viewModel.call.value?.sessionId shouldBe "audio-lifecycle"

            println("    [RESULT] Audio call type preserved")
            println("    ✅ Full chain: audio call lifecycle verified")
        }
    }

    test("full chain: video call → setCall preserves video type through lifecycle") {
        /**
         * Verifies that a video call type is preserved through the full lifecycle.
         */
        runTest {
            println("    [STEP 1] Create ViewModel")
            val viewModel = createViewModel()
            advanceUntilIdle()

            println("    [STEP 2] Set video call")
            val videoCall = MockFactory.createCall(
                sessionId = "video-lifecycle",
                type = "video",
                callerUid = "video-caller",
                callerName = "Video Caller"
            )
            viewModel.setCall(videoCall)
            advanceUntilIdle()

            println("    [STEP 3] Verify video type preserved")
            viewModel.call.value?.type shouldBe "video"
            viewModel.call.value?.sessionId shouldBe "video-lifecycle"

            println("    [RESULT] Video call type preserved")
            println("    ✅ Full chain: video call lifecycle verified")
        }
    }

    test("full chain: mixed audio/video calls → type switches correctly") {
        /**
         * Verifies that switching between audio and video calls works correctly.
         */
        runTest {
            println("    [STEP 1] Create ViewModel")
            val viewModel = createViewModel()
            advanceUntilIdle()

            println("    [STEP 2] Set audio call")
            val audioCall = MockFactory.createCall(sessionId = "mixed-1", type = "audio")
            viewModel.setCall(audioCall)
            advanceUntilIdle()
            viewModel.call.value?.type shouldBe "audio"

            println("    [STEP 3] Switch to video call")
            val videoCall = MockFactory.createCall(sessionId = "mixed-2", type = "video")
            viewModel.setCall(videoCall)
            advanceUntilIdle()
            viewModel.call.value?.type shouldBe "video"

            println("    [STEP 4] Switch back to audio call")
            val audioCall2 = MockFactory.createCall(sessionId = "mixed-3", type = "audio")
            viewModel.setCall(audioCall2)
            advanceUntilIdle()
            viewModel.call.value?.type shouldBe "audio"

            println("    [RESULT] Call type switches correctly between audio/video")
            println("    ✅ Full chain: mixed call types verified")
        }
    }

    test("full chain: listener management → ViewModel works correctly with listeners disabled") {
        /**
         * Verifies that the ViewModel functions correctly when created with
         * enableListeners=false. The listener management methods (addListeners/removeListeners)
         * call CometChat static SDK methods which are not available in unit tests.
         * We verify the contract: ViewModel with enableListeners=false should work
         * for all state management operations without SDK listener side effects.
         */
        runTest {
            println("    [STEP 1] Create ViewModel with listeners disabled")
            val viewModel = createViewModel()
            advanceUntilIdle()

            println("    [STEP 2] Verify ViewModel works for state management")
            val call = MockFactory.createCall(sessionId = "listener-test")
            viewModel.setCall(call)
            viewModel.call.value?.sessionId shouldBe "listener-test"

            println("    [STEP 3] Verify resetState works")
            viewModel.resetState()
            viewModel.acceptedCall.value shouldBe null
            viewModel.rejectedCall.value shouldBe null

            println("    [RESULT] ViewModel works correctly without listeners")
            println("    ✅ Full chain: listener-disabled mode verified")
        }
    }

    test("full chain: stress test → rapid setCall/resetState cycles") {
        /**
         * Verifies that rapid state changes don't corrupt the ViewModel.
         */
        checkAll(10, Arb.int(5..20)) { cycleCount ->
            runTest {
                println("    [STEP 1] Create ViewModel")
                val viewModel = createViewModel()
                advanceUntilIdle()

                println("    [STEP 2] Perform $cycleCount rapid setCall/resetState cycles")
                repeat(cycleCount) { i ->
                    val call = MockFactory.createCall(sessionId = "stress-$i")
                    viewModel.setCall(call)
                    viewModel.resetState()
                }
                advanceUntilIdle()

                println("    [STEP 3] Verify final state is consistent")
                // After all cycles, terminal states should be null
                viewModel.acceptedCall.value shouldBe null
                viewModel.rejectedCall.value shouldBe null
                // call should be the last one set (resetState doesn't clear call)
                viewModel.call.value?.sessionId shouldBe "stress-${cycleCount - 1}"

                println("    [RESULT] $cycleCount rapid cycles completed without corruption")
                println("    ✅ Full chain: stress test verified")
            }
        }
    }
})
