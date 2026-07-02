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
 * Full chain integration test for OutgoingCall component.
 *
 * Unlike list-based components (Conversations, CallLogs) that have:
 *   fake DataSource → real Repository → real UseCase → real ViewModel
 *
 * OutgoingCall has NO DataSource/Repository layers. It interacts directly
 * with the CometChat SDK from the ViewModel. Therefore, the "full chain"
 * for OutgoingCall tests the ViewModel's complete lifecycle:
 *
 *   setCall → state observation → cancelCall → terminal state
 *
 * The integration here verifies:
 * 1. ViewModel creation with enableListeners=false (no SDK side effects)
 * 2. Complete call lifecycle: set → observe → endCallButtonEnabled
 * 3. Multiple call handling (sequential calls)
 * 4. State reset between calls (endCallButtonEnabled resets on setCall)
 * 5. Error resilience (actions on null call)
 *
 * **Validates: Requirements 9.1–9.14**
 *
 * Run with:
 *   ./gradlew :chatuikit-core:testDebugUnitTest --tests "...OutgoingCallFullChainIntegrationTest"
 */
@OptIn(ExperimentalCoroutinesApi::class)
class OutgoingCallFullChainIntegrationTest : FunSpec({

    isolationMode = IsolationMode.SingleInstance

    val testDispatcher = UnconfinedTestDispatcher()

    // ==================== Arb Generators ====================

    val callTypeArb = Arb.element("audio", "video")
    val sessionIdArb = Arb.string(5..15)

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

    // ==================== Full Lifecycle Tests ====================

    test("full chain: ViewModel creation → initial state (all null, endCallButtonEnabled=true)") {
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
            viewModel.endCallButtonEnabled.value shouldBe true

            println("    [RESULT] ViewModel starts in Idle state with endCallButtonEnabled=true")
            println("    ✅ Full chain: creation → Idle verified")
        }
    }

    test("full chain: setCall → call data accessible") {
        /**
         * Verifies the complete flow from Idle to Calling state.
         * The ViewModel should expose the call object with all its properties.
         */
        checkAll(20, sessionIdArb, callTypeArb) { sessionId, callType ->
            runTest {
                println("    [STEP 1] Create ViewModel")
                val viewModel = createViewModel()
                advanceUntilIdle()

                println("    [STEP 2] Set outgoing call (sessionId=$sessionId, type=$callType)")
                val call = MockFactory.createCall(
                    sessionId = sessionId,
                    type = callType,
                    callerUid = "caller-integration",
                    callerName = "Integration Caller"
                )
                viewModel.setCall(call)
                advanceUntilIdle()

                println("    [STEP 3] Verify Calling state")
                viewModel.call.value shouldBe call
                viewModel.call.value?.sessionId shouldBe sessionId
                viewModel.call.value?.type shouldBe callType
                viewModel.call.value?.sender?.uid shouldBe "caller-integration"
                viewModel.call.value?.sender?.name shouldBe "Integration Caller"
                viewModel.acceptedCall.value shouldBe null
                viewModel.rejectedCall.value shouldBe null
                viewModel.endCallButtonEnabled.value shouldBe true

                println("    [RESULT] Calling state with sessionId=$sessionId, type=$callType")
                println("    ✅ Full chain: setCall → Calling verified")
            }
        }
    }

    test("full chain: cancelCall with null call → no state change") {
        /**
         * Verifies that calling cancelCall before setCall is safe.
         * The ViewModel should remain in Idle state.
         */
        runTest {
            println("    [STEP 1] Create ViewModel (no call set)")
            val viewModel = createViewModel()
            advanceUntilIdle()

            println("    [STEP 2] Call cancelCall (should be no-op)")
            viewModel.cancelCall()
            advanceUntilIdle()

            println("    [STEP 3] Verify state unchanged")
            viewModel.call.value shouldBe null
            viewModel.acceptedCall.value shouldBe null
            viewModel.rejectedCall.value shouldBe null
            viewModel.endCallButtonEnabled.value shouldBe true

            println("    [RESULT] State unchanged after cancelCall on null call")
            println("    ✅ Full chain: cancelCall no-op verified")
        }
    }

    test("full chain: sequential calls → each setCall updates call and resets endCallButtonEnabled") {
        /**
         * Verifies that handling multiple sequential outgoing calls works correctly.
         * Each new setCall should set the new call and reset endCallButtonEnabled to true.
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
                    viewModel.endCallButtonEnabled.value shouldBe true

                    println("      → Call ${index + 1}: sessionId=chain-session-${index + 1}, state=Calling")
                }

                println("    [STEP 3] Verify final state is last call")
                viewModel.call.value shouldBe calls.last()
                viewModel.call.value?.sessionId shouldBe "chain-session-$callCount"

                println("    [RESULT] All $callCount calls processed correctly")
                println("    ✅ Full chain: sequential calls verified")
            }
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
            viewModel.endCallButtonEnabled.value shouldBe true

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
            viewModel.endCallButtonEnabled.value shouldBe true

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

    test("full chain: listener-disabled mode → ViewModel works correctly") {
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
            viewModel.endCallButtonEnabled.value shouldBe true

            println("    [STEP 3] Verify removeListeners is safe")
            viewModel.removeListeners()
            // State should not be affected
            viewModel.call.value?.sessionId shouldBe "listener-test"

            println("    [RESULT] ViewModel works correctly without listeners")
            println("    ✅ Full chain: listener-disabled mode verified")
        }
    }

    test("full chain: stress test → rapid setCall cycles") {
        /**
         * Verifies that rapid state changes don't corrupt the ViewModel.
         * Since OutgoingCall has no resetState(), we only cycle through setCall.
         */
        checkAll(10, Arb.int(5..20)) { cycleCount ->
            runTest {
                println("    [STEP 1] Create ViewModel")
                val viewModel = createViewModel()
                advanceUntilIdle()

                println("    [STEP 2] Perform $cycleCount rapid setCall cycles")
                repeat(cycleCount) { i ->
                    val call = MockFactory.createCall(sessionId = "stress-$i")
                    viewModel.setCall(call)
                }
                advanceUntilIdle()

                println("    [STEP 3] Verify final state is consistent")
                // After all cycles, terminal states should be null
                viewModel.acceptedCall.value shouldBe null
                viewModel.rejectedCall.value shouldBe null
                // endCallButtonEnabled should be true (reset by last setCall)
                viewModel.endCallButtonEnabled.value shouldBe true
                // call should be the last one set
                viewModel.call.value?.sessionId shouldBe "stress-${cycleCount - 1}"

                println("    [RESULT] $cycleCount rapid cycles completed without corruption")
                println("    ✅ Full chain: stress test verified")
            }
        }
    }
})
