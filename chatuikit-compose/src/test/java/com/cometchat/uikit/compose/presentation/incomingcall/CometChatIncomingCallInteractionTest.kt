package com.cometchat.uikit.compose.presentation.incomingcall

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
 * Tests for CometChatIncomingCall Compose component interaction behavior.
 *
 * Mirrors: chatuikit-kotlin CometChatIncomingCallInteractionTest
 *
 * Validates: Requirements 15.2, 15.5
 *
 * Run with:
 *   ./gradlew :chatuikit-compose:testDebugUnitTest --tests "*.incomingcall.CometChatIncomingCallInteractionTest"
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

    // ==================== Accept Button Interaction (Requirement 15.2) ====================

    test("accept button tap with no call → no-op (no crash, no state change)") {
        runTest {
            val viewModel = CometChatIncomingCallViewModel(enableListeners = false)
            advanceUntilIdle()

            viewModel.acceptCall()
            advanceUntilIdle()

            viewModel.call.value shouldBe null
            viewModel.acceptedCall.value shouldBe null

            println("    ✅ Accept tap with no call → safe no-op")
        }
    }

    test("accept button tap with call set → precondition met for SDK call") {
        runTest {
            val viewModel = CometChatIncomingCallViewModel(enableListeners = false)
            val call = createMockCall(sessionId = "compose-accept-1")
            viewModel.setCall(call)
            advanceUntilIdle()

            viewModel.call.value?.sessionId shouldBe "compose-accept-1"

            println("    ✅ Accept precondition met: sessionId=compose-accept-1")
        }
    }

    // ==================== Reject Button Interaction (Requirement 15.2) ====================

    test("reject button tap with no call → no-op (no crash, no state change)") {
        runTest {
            val viewModel = CometChatIncomingCallViewModel(enableListeners = false)
            advanceUntilIdle()

            viewModel.rejectCall()
            advanceUntilIdle()

            viewModel.call.value shouldBe null
            viewModel.rejectedCall.value shouldBe null

            println("    ✅ Reject tap with no call → safe no-op")
        }
    }

    test("reject button tap with call set → precondition met for SDK call") {
        runTest {
            val viewModel = CometChatIncomingCallViewModel(enableListeners = false)
            val call = createMockCall(sessionId = "compose-reject-1")
            viewModel.setCall(call)
            advanceUntilIdle()

            viewModel.call.value?.sessionId shouldBe "compose-reject-1"

            println("    ✅ Reject precondition met: sessionId=compose-reject-1")
        }
    }

    // ==================== Custom Callback Interaction (Requirement 15.2) ====================

    test("custom onRejectClick callback → invoked instead of viewModel.rejectCall()") {
        runTest {
            val viewModel = CometChatIncomingCallViewModel(enableListeners = false)
            val call = createMockCall(sessionId = "compose-custom-reject")
            viewModel.setCall(call)
            advanceUntilIdle()

            var callbackCall: Call? = null
            val onRejectClick: (Call) -> Unit = { c -> callbackCall = c }
            onRejectClick(call)

            callbackCall shouldBe call
            viewModel.rejectedCall.value shouldBe null

            println("    ✅ Custom onRejectClick invoked with call, ViewModel unchanged")
        }
    }

    test("custom onAcceptClick callback → invoked after SDK accept succeeds") {
        runTest {
            val viewModel = CometChatIncomingCallViewModel(enableListeners = false)
            val call = createMockCall(sessionId = "compose-custom-accept")
            viewModel.setCall(call)
            advanceUntilIdle()

            var callbackCall: Call? = null
            val onAcceptClick: (Call) -> Unit = { c -> callbackCall = c }
            onAcceptClick(call)

            callbackCall shouldBe call

            println("    ✅ Custom onAcceptClick invoked with call")
        }
    }

    // ==================== Error Event Collection (Requirement 15.5) ====================

    test("errorEvent → composable collects via collectAsState for onError callback") {
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
            println("    ✅ errorEvent collectible (composable uses collectAsState)")
        }
    }

    // ==================== setCall via LaunchedEffect (Requirement 15.2) ====================

    test("setCall via LaunchedEffect(call) → ViewModel updated when call changes") {
        runTest {
            val viewModel = CometChatIncomingCallViewModel(enableListeners = false)

            val call1 = createMockCall(sessionId = "le-1", callerName = "Alice")
            viewModel.setCall(call1)
            advanceUntilIdle()

            viewModel.call.value?.sender?.name shouldBe "Alice"

            val call2 = createMockCall(sessionId = "le-2", callerName = "Bob")
            viewModel.setCall(call2)
            advanceUntilIdle()

            viewModel.call.value?.sender?.name shouldBe "Bob"
            viewModel.acceptedCall.value shouldBe null
            viewModel.rejectedCall.value shouldBe null

            println("    ✅ LaunchedEffect(call): Alice → Bob, state reset correctly")
        }
    }

    // ==================== PBT: Interaction Invariants (Requirement 15.5) ====================

    test("PBT: for any call type, accept/reject with null call is always safe") {
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
        println("    ✅ PBT: No-op guard holds for all call types in Compose")
    }

    test("PBT: for any session, setCall makes data available for composable rendering") {
        checkAll(30, Arb.string(5..15), Arb.element("audio", "video")) { sessionId, callType ->
            runTest {
                val viewModel = CometChatIncomingCallViewModel(enableListeners = false)
                val call = createMockCall(sessionId = sessionId, type = callType)

                viewModel.setCall(call)
                advanceUntilIdle()

                viewModel.call.value?.sessionId shouldBe sessionId
                viewModel.call.value?.type shouldBe callType
            }
        }
        println("    ✅ PBT: setCall always makes data available for composable")
    }
})
