package com.cometchat.uikit.core.state

import com.cometchat.chat.core.Call
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.uikit.core.testutils.MockFactory
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * Tests for IncomingCallUIState sealed class.
 * Verifies construction, type checking, property access, and data class equality
 * for all variants: Idle, Ringing, Accepted, Rejected, Cancelled, Error.
 *
 * Layer 4 — pure data class tests, no mocking of the UIState itself.
 * Call and CometChatException objects are mocked because the SDK classes
 * have private constructors.
 *
 * Reference: CallLogsUIStateTest.kt
 *
 * Validates: Requirements 1.4, 4.1, 4.2
 *
 * Run with:
 * ./gradlew :chatuikit-core:testDebugUnitTest --tests "*IncomingCallUIStateTest"
 */
class IncomingCallUIStateTest : FunSpec({

    // ==================== Idle ====================

    test("Idle should be a singleton instance") {
        val state: IncomingCallUIState = IncomingCallUIState.Idle

        println("  → Verifying Idle is a singleton instance of IncomingCallUIState")

        state.shouldBeInstanceOf<IncomingCallUIState.Idle>()

        println("  ✅ Idle is IncomingCallUIState.Idle")
    }

    test("Idle should be the same reference on multiple accesses") {
        val state1 = IncomingCallUIState.Idle
        val state2 = IncomingCallUIState.Idle

        println("  → Verifying Idle is referentially equal across accesses")

        (state1 === state2) shouldBe true

        println("  ✅ Idle is referentially equal")
    }

    // ==================== Ringing ====================

    test("Ringing should hold the incoming Call object") {
        val call = MockFactory.createCall(
            sessionId = "session-ring-1",
            type = "audio",
            callerUid = "caller-1",
            callerName = "Alice"
        )

        val state = IncomingCallUIState.Ringing(call)

        println("  → Verifying Ringing holds the Call object with sessionId=${call.sessionId}")

        state.shouldBeInstanceOf<IncomingCallUIState.Ringing>()
        state.call shouldBe call
        state.call.sessionId shouldBe "session-ring-1"

        println("  ✅ Ringing holds call with sessionId=session-ring-1")
    }

    test("Ringing data class equality should work with same call reference") {
        val call = MockFactory.createCall(sessionId = "session-eq-1")

        val state1 = IncomingCallUIState.Ringing(call)
        val state2 = IncomingCallUIState.Ringing(call)

        println("  → Verifying Ringing data class equality with same call reference")

        state1 shouldBe state2

        println("  ✅ Ringing equality works for identical call reference")
    }

    test("Ringing with different call objects should not be equal") {
        val call1 = MockFactory.createCall(sessionId = "session-a")
        val call2 = MockFactory.createCall(sessionId = "session-b")

        val state1 = IncomingCallUIState.Ringing(call1)
        val state2 = IncomingCallUIState.Ringing(call2)

        println("  → Verifying Ringing with different calls are not equal")

        (state1 == state2) shouldBe false

        println("  ✅ Ringing with different calls are not equal")
    }

    // ==================== Accepted ====================

    test("Accepted should hold the accepted Call object") {
        val call = MockFactory.createCall(
            sessionId = "session-accept-1",
            type = "video",
            callerUid = "caller-2",
            callerName = "Bob"
        )

        val state = IncomingCallUIState.Accepted(call)

        println("  → Verifying Accepted holds the Call object with sessionId=${call.sessionId}")

        state.shouldBeInstanceOf<IncomingCallUIState.Accepted>()
        state.call shouldBe call
        state.call.sessionId shouldBe "session-accept-1"
        state.call.type shouldBe "video"

        println("  ✅ Accepted holds call with sessionId=session-accept-1, type=video")
    }

    test("Accepted data class equality should work with same call reference") {
        val call = MockFactory.createCall(sessionId = "session-eq-accept")

        val state1 = IncomingCallUIState.Accepted(call)
        val state2 = IncomingCallUIState.Accepted(call)

        println("  → Verifying Accepted data class equality")

        state1 shouldBe state2

        println("  ✅ Accepted equality works")
    }

    test("Accepted with different call objects should not be equal") {
        val call1 = MockFactory.createCall(sessionId = "accept-a")
        val call2 = MockFactory.createCall(sessionId = "accept-b")

        val state1 = IncomingCallUIState.Accepted(call1)
        val state2 = IncomingCallUIState.Accepted(call2)

        println("  → Verifying Accepted with different calls are not equal")

        (state1 == state2) shouldBe false

        println("  ✅ Accepted with different calls are not equal")
    }

    // ==================== Rejected ====================

    test("Rejected should hold the rejected Call object") {
        val call = MockFactory.createCall(
            sessionId = "session-reject-1",
            type = "audio",
            callerUid = "caller-3",
            callerName = "Charlie"
        )

        val state = IncomingCallUIState.Rejected(call)

        println("  → Verifying Rejected holds the Call object with sessionId=${call.sessionId}")

        state.shouldBeInstanceOf<IncomingCallUIState.Rejected>()
        state.call shouldBe call
        state.call.sessionId shouldBe "session-reject-1"

        println("  ✅ Rejected holds call with sessionId=session-reject-1")
    }

    test("Rejected data class equality should work with same call reference") {
        val call = MockFactory.createCall(sessionId = "session-eq-reject")

        val state1 = IncomingCallUIState.Rejected(call)
        val state2 = IncomingCallUIState.Rejected(call)

        println("  → Verifying Rejected data class equality")

        state1 shouldBe state2

        println("  ✅ Rejected equality works")
    }

    test("Rejected with different call objects should not be equal") {
        val call1 = MockFactory.createCall(sessionId = "reject-a")
        val call2 = MockFactory.createCall(sessionId = "reject-b")

        val state1 = IncomingCallUIState.Rejected(call1)
        val state2 = IncomingCallUIState.Rejected(call2)

        println("  → Verifying Rejected with different calls are not equal")

        (state1 == state2) shouldBe false

        println("  ✅ Rejected with different calls are not equal")
    }

    // ==================== Cancelled ====================

    test("Cancelled should hold the cancelled Call object") {
        val call = MockFactory.createCall(
            sessionId = "session-cancel-1",
            type = "video",
            callerUid = "caller-4",
            callerName = "Diana"
        )

        val state = IncomingCallUIState.Cancelled(call)

        println("  → Verifying Cancelled holds the Call object with sessionId=${call.sessionId}")

        state.shouldBeInstanceOf<IncomingCallUIState.Cancelled>()
        state.call shouldBe call
        state.call.sessionId shouldBe "session-cancel-1"

        println("  ✅ Cancelled holds call with sessionId=session-cancel-1")
    }

    test("Cancelled data class equality should work with same call reference") {
        val call = MockFactory.createCall(sessionId = "session-eq-cancel")

        val state1 = IncomingCallUIState.Cancelled(call)
        val state2 = IncomingCallUIState.Cancelled(call)

        println("  → Verifying Cancelled data class equality")

        state1 shouldBe state2

        println("  ✅ Cancelled equality works")
    }

    test("Cancelled with different call objects should not be equal") {
        val call1 = MockFactory.createCall(sessionId = "cancel-a")
        val call2 = MockFactory.createCall(sessionId = "cancel-b")

        val state1 = IncomingCallUIState.Cancelled(call1)
        val state2 = IncomingCallUIState.Cancelled(call2)

        println("  → Verifying Cancelled with different calls are not equal")

        (state1 == state2) shouldBe false

        println("  ✅ Cancelled with different calls are not equal")
    }

    // ==================== Error ====================

    test("Error should hold the CometChatException") {
        val exception = MockFactory.createCometChatException("ERR_ACCEPT", "Failed to accept call")

        val state = IncomingCallUIState.Error(exception)

        println("  → Verifying Error holds the CometChatException with code=${exception.code}")

        state.shouldBeInstanceOf<IncomingCallUIState.Error>()
        state.exception shouldBe exception
        state.exception.code shouldBe "ERR_ACCEPT"
        state.exception.message shouldBe "Failed to accept call"

        println("  ✅ Error holds exception with code=ERR_ACCEPT")
    }

    test("Error data class equality should work with same exception reference") {
        val exception = MockFactory.createCometChatException("ERR", "Something went wrong")

        val state1 = IncomingCallUIState.Error(exception)
        val state2 = IncomingCallUIState.Error(exception)

        println("  → Verifying Error data class equality with same exception reference")

        state1 shouldBe state2

        println("  ✅ Error equality works for identical exceptions")
    }

    test("Error with different exceptions should not be equal") {
        val ex1 = MockFactory.createCometChatException("ERR_1", "Error 1")
        val ex2 = MockFactory.createCometChatException("ERR_2", "Error 2")

        val state1 = IncomingCallUIState.Error(ex1)
        val state2 = IncomingCallUIState.Error(ex2)

        println("  → Verifying Error with different exceptions are not equal")

        (state1 == state2) shouldBe false

        println("  ✅ Error with different exceptions are not equal")
    }

    // ==================== Type Discrimination ====================

    test("all variants should be distinguishable via type checking") {
        val call = MockFactory.createCall(sessionId = "session-disc")
        val exception = MockFactory.createCometChatException("ERR", "Test")

        val states: List<IncomingCallUIState> = listOf(
            IncomingCallUIState.Idle,
            IncomingCallUIState.Ringing(call),
            IncomingCallUIState.Accepted(call),
            IncomingCallUIState.Rejected(call),
            IncomingCallUIState.Cancelled(call),
            IncomingCallUIState.Error(exception)
        )

        println("  → Verifying all 6 variants are distinguishable via type checking")

        states[0].shouldBeInstanceOf<IncomingCallUIState.Idle>()
        states[1].shouldBeInstanceOf<IncomingCallUIState.Ringing>()
        states[2].shouldBeInstanceOf<IncomingCallUIState.Accepted>()
        states[3].shouldBeInstanceOf<IncomingCallUIState.Rejected>()
        states[4].shouldBeInstanceOf<IncomingCallUIState.Cancelled>()
        states[5].shouldBeInstanceOf<IncomingCallUIState.Error>()

        println("  ✅ All 6 variants are correctly type-discriminated")
    }

    test("when expression should exhaustively match all variants") {
        val call = MockFactory.createCall(sessionId = "session-when")
        val exception = MockFactory.createCometChatException("ERR", "Test")

        val states: List<IncomingCallUIState> = listOf(
            IncomingCallUIState.Idle,
            IncomingCallUIState.Ringing(call),
            IncomingCallUIState.Accepted(call),
            IncomingCallUIState.Rejected(call),
            IncomingCallUIState.Cancelled(call),
            IncomingCallUIState.Error(exception)
        )

        println("  → Verifying when expression exhaustively matches all variants")

        val results = states.map { state ->
            when (state) {
                is IncomingCallUIState.Idle -> "idle"
                is IncomingCallUIState.Ringing -> "ringing"
                is IncomingCallUIState.Accepted -> "accepted"
                is IncomingCallUIState.Rejected -> "rejected"
                is IncomingCallUIState.Cancelled -> "cancelled"
                is IncomingCallUIState.Error -> "error"
            }
        }

        results shouldBe listOf("idle", "ringing", "accepted", "rejected", "cancelled", "error")

        println("  ✅ when expression exhaustively matches: $results")
    }

    // ==================== Ringing with different call types ====================

    test("Ringing should work with audio call type") {
        val audioCall = MockFactory.createCall(sessionId = "audio-session", type = "audio")
        val state = IncomingCallUIState.Ringing(audioCall)

        println("  → Verifying Ringing with audio call type")

        state.call.type shouldBe "audio"

        println("  ✅ Ringing with audio call type works")
    }

    test("Ringing should work with video call type") {
        val videoCall = MockFactory.createCall(sessionId = "video-session", type = "video")
        val state = IncomingCallUIState.Ringing(videoCall)

        println("  → Verifying Ringing with video call type")

        state.call.type shouldBe "video"

        println("  ✅ Ringing with video call type works")
    }
})
