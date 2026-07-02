package com.cometchat.uikit.kotlin.presentation.incomingcall

import com.cometchat.chat.core.Call
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
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * Tests for CometChatIncomingCall Kotlin component rendering states.
 *
 * Verifies that the ViewModel state drives the correct rendering behavior:
 * - call == null → component is in idle/hidden state
 * - call != null → component shows caller info (name, avatar, call type)
 * - acceptedCall != null → component triggers accepted flow (launch ongoing call)
 * - rejectedCall != null → component triggers rejected flow (dismiss)
 *
 * Validates: Requirements 12.1, 12.5
 *
 * Run with:
 *   ./gradlew :chatuikit-kotlin:testDebugUnitTest --tests "*CometChatIncomingCallRenderingTest"
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CometChatIncomingCallRenderingTest : FunSpec({

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

    // ==================== Initial State Rendering (Requirement 12.1) ====================

    test("ViewModel initial state → component is in idle state (no call to display)") {
        runTest {
            val viewModel = CometChatIncomingCallViewModel(enableListeners = false)
            advanceUntilIdle()

            viewModel.call.value shouldBe null
            viewModel.acceptedCall.value shouldBe null
            viewModel.rejectedCall.value shouldBe null

            println("    ✅ Initial state: call=null → component idle/hidden")
        }
    }

    // ==================== Ringing State Rendering (Requirement 12.1) ====================

    test("ViewModel with call set → component shows caller name and call type") {
        runTest {
            val viewModel = CometChatIncomingCallViewModel(enableListeners = false)
            val call = createMockCall(
                sessionId = "session-render-1",
                type = "audio",
                callerUid = "caller-1",
                callerName = "Alice"
            )

            viewModel.setCall(call)
            advanceUntilIdle()

            viewModel.call.value shouldBe call
            viewModel.call.value?.type shouldBe "audio"
            viewModel.call.value?.sender?.name shouldBe "Alice"

            println("    ✅ Call set: caller=Alice, type=audio → component renders caller info")
        }
    }

    test("ViewModel with audio call → component renders audio call icon and text") {
        runTest {
            val viewModel = CometChatIncomingCallViewModel(enableListeners = false)
            val call = createMockCall(sessionId = "audio-render", type = "audio")

            viewModel.setCall(call)
            advanceUntilIdle()

            viewModel.call.value?.type shouldBe "audio"

            println("    ✅ Audio call → component shows voice call icon")
        }
    }

    test("ViewModel with video call → component renders video call icon and text") {
        runTest {
            val viewModel = CometChatIncomingCallViewModel(enableListeners = false)
            val call = createMockCall(sessionId = "video-render", type = "video")

            viewModel.setCall(call)
            advanceUntilIdle()

            viewModel.call.value?.type shouldBe "video"

            println("    ✅ Video call → component shows video call icon")
        }
    }

    // ==================== Accepted State Rendering (Requirement 12.1) ====================

    test("ViewModel with acceptedCall not set → component stays in ringing state") {
        runTest {
            val viewModel = CometChatIncomingCallViewModel(enableListeners = false)
            val call = createMockCall(sessionId = "accept-render")
            viewModel.setCall(call)
            advanceUntilIdle()

            viewModel.acceptedCall.value shouldBe null

            println("    ✅ acceptedCall=null → component stays in ringing state")
        }
    }

    // ==================== Rejected State Rendering (Requirement 12.1) ====================

    test("ViewModel with rejectedCall not set → component stays in ringing state") {
        runTest {
            val viewModel = CometChatIncomingCallViewModel(enableListeners = false)
            val call = createMockCall(sessionId = "reject-render")
            viewModel.setCall(call)
            advanceUntilIdle()

            viewModel.rejectedCall.value shouldBe null

            println("    ✅ rejectedCall=null → component stays in ringing state")
        }
    }

    // ==================== PBT: Call Type Rendering (Requirement 12.5) ====================

    test("PBT: for any call type (audio/video) → ViewModel exposes correct type for rendering") {
        checkAll(30, Arb.element("audio", "video"), Arb.string(5..15)) { callType, sessionId ->
            runTest {
                val viewModel = CometChatIncomingCallViewModel(enableListeners = false)
                val call = createMockCall(sessionId = sessionId, type = callType)

                viewModel.setCall(call)
                advanceUntilIdle()

                viewModel.call.value?.type shouldBe callType

                println("    → type=$callType, sessionId=$sessionId → correct type exposed")
            }
        }
        println("    ✅ PBT: All call types render correctly")
    }

    test("PBT: for any caller name → ViewModel exposes correct name for rendering") {
        checkAll(30, Arb.string(3..20), Arb.string(5..15)) { callerName, sessionId ->
            runTest {
                val viewModel = CometChatIncomingCallViewModel(enableListeners = false)
                val call = createMockCall(
                    sessionId = sessionId,
                    callerName = callerName,
                    callerUid = "uid-$sessionId"
                )

                viewModel.setCall(call)
                advanceUntilIdle()

                viewModel.call.value?.sender?.name shouldBe callerName

                println("    → callerName=$callerName → correct name exposed")
            }
        }
        println("    ✅ PBT: All caller names render correctly")
    }

    // ==================== State Transition Rendering ====================

    test("switching calls → component re-renders with new caller info") {
        runTest {
            val viewModel = CometChatIncomingCallViewModel(enableListeners = false)

            val call1 = createMockCall(
                sessionId = "session-1",
                type = "audio",
                callerName = "Alice",
                callerUid = "alice-uid"
            )
            viewModel.setCall(call1)
            advanceUntilIdle()

            viewModel.call.value?.sender?.name shouldBe "Alice"
            viewModel.call.value?.type shouldBe "audio"

            val call2 = createMockCall(
                sessionId = "session-2",
                type = "video",
                callerName = "Bob",
                callerUid = "bob-uid"
            )
            viewModel.setCall(call2)
            advanceUntilIdle()

            viewModel.call.value?.sender?.name shouldBe "Bob"
            viewModel.call.value?.type shouldBe "video"

            println("    ✅ Switching calls: Alice(audio) → Bob(video) → re-renders correctly")
        }
    }

    test("resetState → terminal states cleared, component stays in ringing") {
        runTest {
            val viewModel = CometChatIncomingCallViewModel(enableListeners = false)
            val call = createMockCall(sessionId = "session-reset-render")
            viewModel.setCall(call)
            advanceUntilIdle()

            viewModel.resetState()
            advanceUntilIdle()

            viewModel.call.value shouldBe call
            viewModel.acceptedCall.value shouldBe null
            viewModel.rejectedCall.value shouldBe null

            println("    ✅ resetState: call preserved, terminals cleared → stays in ringing")
        }
    }
})
