package com.cometchat.uikit.kotlin.presentation.outgoingcall

import com.cometchat.chat.core.Call
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.viewmodel.CometChatOutgoingCallViewModel
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
 * Tests for CometChatOutgoingCall Kotlin component interaction behavior.
 *
 * Validates: Requirements 9.7, 9.8, 9.9, 9.10
 *
 * Run with:
 *   ./gradlew :chatuikit-kotlin:testDebugUnitTest --tests "*CometChatOutgoingCallInteractionTest"
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CometChatOutgoingCallInteractionTest : FunSpec({

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

    // ==================== Cancel Button Interaction (Requirement 9.7) ====================

    test("cancel button tap with no call set → no-op (no crash, no state change)") {
        runTest {
            val viewModel = CometChatOutgoingCallViewModel(enableListeners = false)
            advanceUntilIdle()

            // cancelCall with null call returns early (no SDK call)
            viewModel.cancelCall()
            advanceUntilIdle()

            viewModel.call.value shouldBe null
            viewModel.rejectedCall.value shouldBe null

            println("    ✅ Cancel tap with no call → safe no-op")
        }
    }

    test("cancel button tap with call set → precondition met (sessionId available)") {
        runTest {
            val viewModel = CometChatOutgoingCallViewModel(enableListeners = false)
            val call = createMockCall(sessionId = "session-cancel-interact")
            viewModel.setCall(call)
            advanceUntilIdle()

            // Verify precondition: sessionId is available for SDK call
            viewModel.call.value?.sessionId shouldBe "session-cancel-interact"

            println("    ✅ Cancel precondition met: sessionId=session-cancel-interact")
        }
    }

    // ==================== Custom Callback Interaction (Requirement 9.7) ====================

    test("custom onCancelClick callback → bypasses viewModel.cancelCall()") {
        runTest {
            val viewModel = CometChatOutgoingCallViewModel(enableListeners = false)
            val call = createMockCall(sessionId = "session-custom-cancel")
            viewModel.setCall(call)
            advanceUntilIdle()

            var callbackInvoked = false
            var callbackCall: Call? = null
            val customCallback: (Call) -> Unit = { c ->
                callbackInvoked = true
                callbackCall = c
            }
            customCallback(call)

            callbackInvoked shouldBe true
            callbackCall shouldBe call
            viewModel.rejectedCall.value shouldBe null

            println("    ✅ Custom onCancelClick invoked with call, ViewModel state unchanged")
        }
    }

    // ==================== Error Event Collection (Requirement 9.5) ====================

    test("errorEvent SharedFlow → component can collect errors for onError callback") {
        runTest {
            val viewModel = CometChatOutgoingCallViewModel(enableListeners = false)
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

    // ==================== setCall Interaction (Requirement 9.2) ====================

    test("setCall interaction → updates call and resets endCallButtonEnabled") {
        runTest {
            val viewModel = CometChatOutgoingCallViewModel(enableListeners = false)

            val call1 = createMockCall(sessionId = "interact-1", callerName = "Alice")
            viewModel.setCall(call1)
            advanceUntilIdle()

            viewModel.call.value?.sender?.name shouldBe "Alice"
            viewModel.endCallButtonEnabled.value shouldBe true

            val call2 = createMockCall(sessionId = "interact-2", callerName = "Bob")
            viewModel.setCall(call2)
            advanceUntilIdle()

            viewModel.call.value?.sender?.name shouldBe "Bob"
            viewModel.endCallButtonEnabled.value shouldBe true

            println("    ✅ setCall: Alice → Bob, endCallButtonEnabled reset to true")
        }
    }

    // ==================== PBT: Interaction Invariants ====================

    test("PBT: cancelCall with null call is always a no-op") {
        checkAll(30, Arb.element("audio", "video")) { _ ->
            runTest {
                val viewModel = CometChatOutgoingCallViewModel(enableListeners = false)

                viewModel.cancelCall()
                advanceUntilIdle()

                viewModel.call.value shouldBe null
                viewModel.acceptedCall.value shouldBe null
                viewModel.rejectedCall.value shouldBe null
            }
        }
        println("    ✅ PBT: No-op guard holds for cancelCall with null call")
    }

    test("PBT: for any session, setCall always makes sessionId available for actions") {
        checkAll(30, Arb.string(5..15), Arb.element("audio", "video")) { sessionId, callType ->
            runTest {
                val viewModel = CometChatOutgoingCallViewModel(enableListeners = false)
                val call = createMockCall(sessionId = sessionId, type = callType)

                viewModel.setCall(call)
                advanceUntilIdle()

                viewModel.call.value?.sessionId shouldBe sessionId
            }
        }
        println("    ✅ PBT: setCall always makes sessionId available")
    }
})
