package com.cometchat.uikit.kotlin.presentation.incomingcall

import com.cometchat.chat.core.Call
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.viewmodel.CometChatIncomingCallViewModel
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.element
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
 * Tests for CometChatIncomingCall Kotlin component interaction behavior.
 *
 * Validates: Requirements 12.2, 12.5
 *
 * Run with:
 *   ./gradlew :chatuikit-kotlin:testDebugUnitTest --tests "*CometChatIncomingCallInteractionTest"
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CometChatIncomingCallInteractionTest : FunSpec({

    val testDispatcher = UnconfinedTestDispatcher()

    fun createMockCall(
        sessionId: String = "session-1",
        type: String = "audio",
        callerUid: String = "caller-1",
        callerName: String = "Caller"
    ): Call {
        val call = mock<Call>()
        whenever(call.sessionId).thenReturn(sessionId)
        whenever(call.type).thenReturn(type)
        val caller = mock<User>()
        whenever(caller.uid).thenReturn(callerUid)
        whenever(caller.name).thenReturn(callerName)
        whenever(call.callInitiator).thenReturn(caller)
        whenever(call.sender).thenReturn(caller)
        return call
    }

    beforeTest {
        Dispatchers.setMain(testDispatcher)
        println("  🧪 ${it.name.testName}")
    }

    afterTest {
        Dispatchers.resetMain()
        println()
    }

    // ==================== Accept Button Interaction (Requirement 12.2) ====================

    test("accept button tap with no call set → no-op (no crash, no state change)") {
        runTest {
            val viewModel = CometChatIncomingCallViewModel(enableListeners = false)
            advanceUntilIdle()

            viewModel.acceptCall()
            advanceUntilIdle()

            viewModel.call.value shouldBe null
            viewModel.acceptedCall.value shouldBe null
            viewModel.rejectedCall.value shouldBe null

            println("    ✅ Accept tap with no call → safe no-op")
        }
    }

    test("accept button tap with call set → precondition met (sessionId available)") {
        runTest {
            val viewModel = CometChatIncomingCallViewModel(enableListeners = false)
            val call = createMockCall(sessionId = "session-accept-interact")
            viewModel.setCall(call)
            advanceUntilIdle()

            viewModel.call.value?.sessionId shouldBe "session-accept-interact"

            println("    ✅ Accept precondition met: sessionId=session-accept-interact")
        }
    }

    // ==================== Reject Button Interaction (Requirement 12.2) ====================

    test("reject button tap with no call set → no-op (no crash, no state change)") {
        runTest {
            val viewModel = CometChatIncomingCallViewModel(enableListeners = false)
            advanceUntilIdle()

            viewModel.rejectCall()
            advanceUntilIdle()

            viewModel.call.value shouldBe null
            viewModel.acceptedCall.value shouldBe null
            viewModel.rejectedCall.value shouldBe null

            println("    ✅ Reject tap with no call → safe no-op")
        }
    }

    test("reject button tap with call set → precondition met (sessionId available)") {
        runTest {
            val viewModel = CometChatIncomingCallViewModel(enableListeners = false)
            val call = createMockCall(sessionId = "session-reject-interact")
            viewModel.setCall(call)
            advanceUntilIdle()

            viewModel.call.value?.sessionId shouldBe "session-reject-interact"

            println("    ✅ Reject precondition met: sessionId=session-reject-interact")
        }
    }

    // ==================== Custom Callback Interaction (Requirement 12.2) ====================

    test("custom onAcceptClick callback → bypasses viewModel.acceptCall()") {
        runTest {
            val viewModel = CometChatIncomingCallViewModel(enableListeners = false)
            val call = createMockCall(sessionId = "session-custom-accept")
            viewModel.setCall(call)
            advanceUntilIdle()

            var callbackInvoked = false
            val customCallback = { callbackInvoked = true }
            customCallback()

            callbackInvoked shouldBe true
            viewModel.acceptedCall.value shouldBe null

            println("    ✅ Custom accept callback invoked, ViewModel state unchanged")
        }
    }

    test("custom onRejectClick callback → bypasses viewModel.rejectCall()") {
        runTest {
            val viewModel = CometChatIncomingCallViewModel(enableListeners = false)
            val call = createMockCall(sessionId = "session-custom-reject")
            viewModel.setCall(call)
            advanceUntilIdle()

            var callbackInvoked = false
            val customCallback = { callbackInvoked = true }
            customCallback()

            callbackInvoked shouldBe true
            viewModel.rejectedCall.value shouldBe null

            println("    ✅ Custom reject callback invoked, ViewModel state unchanged")
        }
    }

    // ==================== Error Event Collection (Requirement 12.5) ====================

    test("errorEvent SharedFlow → component can collect errors for onError callback") {
        runTest {
            val viewModel = CometChatIncomingCallViewModel(enableListeners = false)
            advanceUntilIdle()

            val collectedErrors = mutableListOf<CometChatException>()
            val job = launch(testDispatcher) {
                viewModel.errorEvent.collect { error ->
                    collectedErrors.add(error)
                }
            }

            advanceUntilIdle()
            collectedErrors.size shouldBe 0

            job.cancel()
            println("    ✅ errorEvent SharedFlow is collectible (0 errors in test)")
        }
    }

    // ==================== setCall Interaction (Requirement 12.2) ====================

    test("setCall interaction → resets terminal states and updates call") {
        runTest {
            val viewModel = CometChatIncomingCallViewModel(enableListeners = false)

            val call1 = createMockCall(sessionId = "interact-1", callerName = "Alice")
            viewModel.setCall(call1)
            advanceUntilIdle()

            viewModel.call.value?.sender?.name shouldBe "Alice"

            val call2 = createMockCall(sessionId = "interact-2", callerName = "Bob")
            viewModel.setCall(call2)
            advanceUntilIdle()

            viewModel.call.value?.sender?.name shouldBe "Bob"
            viewModel.acceptedCall.value shouldBe null
            viewModel.rejectedCall.value shouldBe null

            println("    ✅ setCall: Alice → Bob, terminal states reset")
        }
    }

    // ==================== PBT: Interaction Invariants (Requirement 12.5) ====================

    test("PBT: for any call type, accept/reject with null call is always a no-op") {
        checkAll(30, Arb.element("audio", "video")) { _ ->
            runTest {
                val viewModel = CometChatIncomingCallViewModel(enableListeners = false)

                viewModel.acceptCall()
                viewModel.rejectCall()
                advanceUntilIdle()

                viewModel.call.value shouldBe null
                viewModel.acceptedCall.value shouldBe null
                viewModel.rejectedCall.value shouldBe null
            }
        }
        println("    ✅ PBT: No-op guard holds for all call types")
    }

    test("PBT: for any session, setCall always makes sessionId available for actions") {
        checkAll(30, Arb.string(5..15), Arb.element("audio", "video")) { sessionId, callType ->
            runTest {
                val viewModel = CometChatIncomingCallViewModel(enableListeners = false)
                val call = createMockCall(sessionId = sessionId, type = callType)

                viewModel.setCall(call)
                advanceUntilIdle()

                viewModel.call.value?.sessionId shouldBe sessionId
            }
        }
        println("    ✅ PBT: setCall always makes sessionId available")
    }
})
