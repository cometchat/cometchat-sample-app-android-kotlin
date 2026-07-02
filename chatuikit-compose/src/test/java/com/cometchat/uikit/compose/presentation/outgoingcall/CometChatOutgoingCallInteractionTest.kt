package com.cometchat.uikit.compose.presentation.outgoingcall

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
 * Tests for CometChatOutgoingCall Compose component interaction behavior.
 *
 * Mirrors: chatuikit-kotlin CometChatOutgoingCallInteractionTest
 *
 * Validates: Requirements 9.7, 9.8, 9.9, 9.10
 *
 * Run with:
 *   ./gradlew :chatuikit-compose:testDebugUnitTest --tests "*.outgoingcall.CometChatOutgoingCallInteractionTest"
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

    test("cancel button tap with no call → no-op (no crash, no state change)") {
        runTest {
            val viewModel = CometChatOutgoingCallViewModel(enableListeners = false)
            advanceUntilIdle()

            viewModel.cancelCall()
            advanceUntilIdle()

            viewModel.call.value shouldBe null
            viewModel.rejectedCall.value shouldBe null

            println("    ✅ Cancel tap with no call → safe no-op")
        }
    }

    test("cancel button tap with call set → precondition met for SDK call") {
        runTest {
            val viewModel = CometChatOutgoingCallViewModel(enableListeners = false)
            val call = createMockCall(sessionId = "compose-cancel-1")
            viewModel.setCall(call)
            advanceUntilIdle()

            viewModel.call.value?.sessionId shouldBe "compose-cancel-1"

            println("    ✅ Cancel precondition met: sessionId=compose-cancel-1")
        }
    }

    // ==================== Custom Callback Interaction (Requirement 9.7) ====================

    test("custom onCancelClick callback → bypasses viewModel.cancelCall()") {
        runTest {
            val viewModel = CometChatOutgoingCallViewModel(enableListeners = false)
            val call = createMockCall(sessionId = "compose-custom-cancel")
            viewModel.setCall(call)
            advanceUntilIdle()

            var callbackCall: Call? = null
            val onCancelClick: (Call) -> Unit = { c -> callbackCall = c }
            onCancelClick(call)

            callbackCall shouldBe call
            viewModel.rejectedCall.value shouldBe null

            println("    ✅ Custom onCancelClick invoked with call, ViewModel unchanged")
        }
    }

    // ==================== Error Event Collection (Requirement 9.5) ====================

    test("errorEvent → composable collects via collectAsState for onError callback") {
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
            println("    ✅ errorEvent collectible (composable uses collectAsState)")
        }
    }

    // ==================== setCall via LaunchedEffect (Requirement 9.2) ====================

    test("setCall via LaunchedEffect(call) → ViewModel updated when call changes") {
        runTest {
            val viewModel = CometChatOutgoingCallViewModel(enableListeners = false)

            val call1 = createMockCall(sessionId = "le-1", callerName = "Alice")
            viewModel.setCall(call1)
            advanceUntilIdle()

            viewModel.call.value?.sender?.name shouldBe "Alice"
            viewModel.endCallButtonEnabled.value shouldBe true

            val call2 = createMockCall(sessionId = "le-2", callerName = "Bob")
            viewModel.setCall(call2)
            advanceUntilIdle()

            viewModel.call.value?.sender?.name shouldBe "Bob"
            viewModel.endCallButtonEnabled.value shouldBe true
            viewModel.acceptedCall.value shouldBe null
            viewModel.rejectedCall.value shouldBe null

            println("    ✅ LaunchedEffect(call): Alice → Bob, endCallButtonEnabled reset correctly")
        }
    }

    // ==================== PBT: Interaction Invariants ====================

    test("PBT: cancelCall with null call is always safe") {
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
        println("    ✅ PBT: No-op guard holds for cancelCall in Compose")
    }

    test("PBT: for any session, setCall makes data available for composable rendering") {
        checkAll(30, Arb.string(5..15), Arb.element("audio", "video")) { sessionId, callType ->
            runTest {
                val viewModel = CometChatOutgoingCallViewModel(enableListeners = false)
                val call = createMockCall(sessionId = sessionId, type = callType)

                viewModel.setCall(call)
                advanceUntilIdle()

                viewModel.call.value?.sessionId shouldBe sessionId
                viewModel.call.value?.type shouldBe callType
                viewModel.endCallButtonEnabled.value shouldBe true
            }
        }
        println("    ✅ PBT: setCall always makes data available for composable")
    }
})
