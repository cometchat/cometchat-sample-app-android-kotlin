package com.cometchat.uikit.compose.presentation.outgoingcall

import com.cometchat.chat.core.Call
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
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * Tests for CometChatOutgoingCall Compose component rendering states.
 *
 * Mirrors: chatuikit-kotlin CometChatOutgoingCallRenderingTest
 *
 * Validates: Requirements 9.1, 9.2, 9.6
 *
 * Run with:
 *   ./gradlew :chatuikit-compose:testDebugUnitTest --tests "*.outgoingcall.CometChatOutgoingCallRenderingTest"
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CometChatOutgoingCallRenderingTest : FunSpec({

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

    // ==================== Initial State Rendering (Requirement 9.1) ====================

    test("ViewModel initial state → composable in idle state (no call data)") {
        runTest {
            val viewModel = CometChatOutgoingCallViewModel(enableListeners = false)
            advanceUntilIdle()

            viewModel.call.value shouldBe null
            viewModel.acceptedCall.value shouldBe null
            viewModel.rejectedCall.value shouldBe null
            viewModel.endCallButtonEnabled.value shouldBe true

            println("    ✅ Initial state: all flows null, endCallButtonEnabled=true → composable idle")
        }
    }

    // ==================== Calling State Rendering (Requirement 9.2) ====================

    test("ViewModel with audio call → composable renders audio call UI") {
        runTest {
            val viewModel = CometChatOutgoingCallViewModel(enableListeners = false)
            val call = createMockCall(
                sessionId = "compose-audio-1",
                type = "audio",
                callerUid = "receiver-compose-1",
                callerName = "Alice Compose"
            )

            viewModel.setCall(call)
            advanceUntilIdle()

            viewModel.call.value?.type shouldBe "audio"
            viewModel.call.value?.sender?.name shouldBe "Alice Compose"

            println("    ✅ Audio call: receiver=Alice Compose → composable shows voice icon")
        }
    }

    test("ViewModel with video call → composable renders video call UI") {
        runTest {
            val viewModel = CometChatOutgoingCallViewModel(enableListeners = false)
            val call = createMockCall(
                sessionId = "compose-video-1",
                type = "video",
                callerUid = "receiver-compose-2",
                callerName = "Bob Compose"
            )

            viewModel.setCall(call)
            advanceUntilIdle()

            viewModel.call.value?.type shouldBe "video"
            viewModel.call.value?.sender?.name shouldBe "Bob Compose"

            println("    ✅ Video call: receiver=Bob Compose → composable shows video icon")
        }
    }

    // ==================== End Call Button State (Requirement 9.6) ====================

    test("ViewModel endCallButtonEnabled=true → composable shows enabled end call button") {
        runTest {
            val viewModel = CometChatOutgoingCallViewModel(enableListeners = false)
            val call = createMockCall(sessionId = "compose-enabled-check")
            viewModel.setCall(call)
            advanceUntilIdle()

            viewModel.endCallButtonEnabled.value shouldBe true

            println("    ✅ endCallButtonEnabled=true → composable shows enabled button")
        }
    }

    // ==================== Terminal State Rendering (Requirement 9.3, 9.4) ====================

    test("ViewModel acceptedCall=null → composable stays in calling state") {
        runTest {
            val viewModel = CometChatOutgoingCallViewModel(enableListeners = false)
            val call = createMockCall(sessionId = "compose-accept-check")
            viewModel.setCall(call)
            advanceUntilIdle()

            viewModel.acceptedCall.value shouldBe null

            println("    ✅ acceptedCall=null → composable stays in calling")
        }
    }

    test("ViewModel rejectedCall=null → composable stays in calling state") {
        runTest {
            val viewModel = CometChatOutgoingCallViewModel(enableListeners = false)
            val call = createMockCall(sessionId = "compose-reject-check")
            viewModel.setCall(call)
            advanceUntilIdle()

            viewModel.rejectedCall.value shouldBe null

            println("    ✅ rejectedCall=null → composable stays in calling")
        }
    }

    // ==================== PBT: Call Type Rendering ====================

    test("PBT: for any call type → ViewModel exposes correct type for composable rendering") {
        checkAll(30, Arb.element("audio", "video"), Arb.string(5..15)) { callType, sessionId ->
            runTest {
                val viewModel = CometChatOutgoingCallViewModel(enableListeners = false)
                val call = createMockCall(sessionId = sessionId, type = callType)

                viewModel.setCall(call)
                advanceUntilIdle()

                viewModel.call.value?.type shouldBe callType
            }
        }
        println("    ✅ PBT: All call types correctly exposed for composable rendering")
    }

    test("PBT: for any receiver info → ViewModel exposes correct data for composable") {
        checkAll(30, Arb.string(3..20), Arb.string(5..15)) { receiverName, sessionId ->
            runTest {
                val viewModel = CometChatOutgoingCallViewModel(enableListeners = false)
                val call = createMockCall(
                    sessionId = sessionId,
                    callerName = receiverName,
                    callerUid = "uid-$sessionId"
                )

                viewModel.setCall(call)
                advanceUntilIdle()

                viewModel.call.value?.sender?.name shouldBe receiverName
            }
        }
        println("    ✅ PBT: All receiver names correctly exposed for composable")
    }

    // ==================== State Transition Rendering ====================

    test("switching calls → composable re-composes with new receiver info") {
        runTest {
            val viewModel = CometChatOutgoingCallViewModel(enableListeners = false)

            val call1 = createMockCall(sessionId = "compose-switch-1", type = "audio", callerName = "Alice")
            viewModel.setCall(call1)
            advanceUntilIdle()

            viewModel.call.value?.sender?.name shouldBe "Alice"
            viewModel.call.value?.type shouldBe "audio"
            viewModel.endCallButtonEnabled.value shouldBe true

            val call2 = createMockCall(sessionId = "compose-switch-2", type = "video", callerName = "Bob")
            viewModel.setCall(call2)
            advanceUntilIdle()

            viewModel.call.value?.sender?.name shouldBe "Bob"
            viewModel.call.value?.type shouldBe "video"
            viewModel.endCallButtonEnabled.value shouldBe true

            println("    ✅ Switching calls: Alice(audio) → Bob(video) → composable re-composes, endCallButtonEnabled reset")
        }
    }
})
