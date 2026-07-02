package com.cometchat.uikit.kotlin.presentation.incomingcall

import com.cometchat.chat.core.Call
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.viewmodel.CometChatIncomingCallViewModel
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
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * Property-based tests for CometChatIncomingCall Kotlin component.
 *
 * Validates: Requirements 13.3, 33.5
 *
 * Run with:
 *   ./gradlew :chatuikit-kotlin:testDebugUnitTest --tests "*CometChatIncomingCallPropertyTest"
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CometChatIncomingCallPropertyTest : FunSpec({

    val testDispatcher = UnconfinedTestDispatcher()

    val callTypeArb = Arb.element("audio", "video")
    val sessionIdArb = Arb.string(5..15)
    val callerNameArb = Arb.string(3..20)

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

    // ==================== State Exclusivity (Requirement 13.3) ====================

    test("PROPERTY: acceptedCall and rejectedCall are never both non-null simultaneously") {
        checkAll(50, sessionIdArb, callTypeArb) { sessionId, callType ->
            runTest {
                val viewModel = CometChatIncomingCallViewModel(enableListeners = false)
                val call = createMockCall(sessionId = sessionId, type = callType)

                viewModel.setCall(call)
                advanceUntilIdle()

                val accepted = viewModel.acceptedCall.value
                val rejected = viewModel.rejectedCall.value
                val bothNonNull = accepted != null && rejected != null
                bothNonNull shouldBe false
            }
        }
        println("    ✅ PROPERTY: State exclusivity maintained across all inputs")
    }

    // ==================== Call Identity Preservation (Requirement 13.3) ====================

    test("PROPERTY: setCall always makes the new call the current call") {
        checkAll(50, sessionIdArb, callTypeArb, callerNameArb) { sessionId, callType, callerName ->
            runTest {
                val viewModel = CometChatIncomingCallViewModel(enableListeners = false)
                val call = createMockCall(
                    sessionId = sessionId,
                    type = callType,
                    callerName = callerName,
                    callerUid = "uid-$sessionId"
                )

                viewModel.setCall(call)
                advanceUntilIdle()

                viewModel.call.value shouldBe call
                viewModel.call.value?.sessionId shouldBe sessionId
                viewModel.call.value?.type shouldBe callType
                viewModel.call.value?.sender?.name shouldBe callerName
            }
        }
        println("    ✅ PROPERTY: Call identity always preserved after setCall")
    }

    // ==================== Reset Semantics (Requirement 13.3) ====================

    test("PROPERTY: setCall always clears terminal states") {
        checkAll(30, Arb.int(2..6)) { callCount ->
            runTest {
                val viewModel = CometChatIncomingCallViewModel(enableListeners = false)

                repeat(callCount) { i ->
                    val call = createMockCall(sessionId = "prop-$i")
                    viewModel.setCall(call)
                    advanceUntilIdle()

                    viewModel.acceptedCall.value shouldBe null
                    viewModel.rejectedCall.value shouldBe null
                }
            }
        }
        println("    ✅ PROPERTY: setCall always clears terminal states")
    }

    // ==================== Type Preservation (Requirement 33.5) ====================

    test("PROPERTY: call type is always preserved through state transitions") {
        checkAll(50, callTypeArb, sessionIdArb) { callType, sessionId ->
            runTest {
                val viewModel = CometChatIncomingCallViewModel(enableListeners = false)
                val call = createMockCall(sessionId = sessionId, type = callType)

                viewModel.setCall(call)
                advanceUntilIdle()
                viewModel.call.value?.type shouldBe callType

                viewModel.resetState()
                advanceUntilIdle()
                viewModel.call.value?.type shouldBe callType
            }
        }
        println("    ✅ PROPERTY: Call type always preserved")
    }

    // ==================== No-Op Guard (Requirement 13.3) ====================

    test("PROPERTY: acceptCall/rejectCall with null call never changes state") {
        checkAll(30, Arb.int(1..10)) { repeatCount ->
            runTest {
                val viewModel = CometChatIncomingCallViewModel(enableListeners = false)

                repeat(repeatCount) {
                    viewModel.acceptCall()
                    viewModel.rejectCall()
                }
                advanceUntilIdle()

                viewModel.call.value shouldBe null
                viewModel.acceptedCall.value shouldBe null
                viewModel.rejectedCall.value shouldBe null
            }
        }
        println("    ✅ PROPERTY: No-op guard holds for all repeat counts")
    }

    // ==================== Last Call Wins (Requirement 13.3) ====================

    test("PROPERTY: for any sequence of N calls, only the last call is current") {
        checkAll(20, Arb.int(2..8)) { callCount ->
            runTest {
                val viewModel = CometChatIncomingCallViewModel(enableListeners = false)
                val calls = (0 until callCount).map { i ->
                    createMockCall(sessionId = "last-wins-$i", callerName = "Caller $i")
                }

                calls.forEach { call -> viewModel.setCall(call) }
                advanceUntilIdle()

                viewModel.call.value shouldBe calls.last()
                viewModel.call.value?.sessionId shouldBe "last-wins-${callCount - 1}"
            }
        }
        println("    ✅ PROPERTY: Last call always wins")
    }

    // ==================== resetState Idempotency (Requirement 13.3) ====================

    test("PROPERTY: resetState is idempotent") {
        checkAll(20, Arb.int(1..10), sessionIdArb) { repeatCount, sessionId ->
            runTest {
                val viewModel = CometChatIncomingCallViewModel(enableListeners = false)
                val call = createMockCall(sessionId = sessionId)
                viewModel.setCall(call)
                advanceUntilIdle()

                repeat(repeatCount) { viewModel.resetState() }
                advanceUntilIdle()

                viewModel.acceptedCall.value shouldBe null
                viewModel.rejectedCall.value shouldBe null
                viewModel.call.value?.sessionId shouldBe sessionId
            }
        }
        println("    ✅ PROPERTY: resetState is idempotent")
    }
})
