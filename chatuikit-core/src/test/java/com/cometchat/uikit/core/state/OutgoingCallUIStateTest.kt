package com.cometchat.uikit.core.state

import com.cometchat.chat.core.Call
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.uikit.core.testutils.MockFactory
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

/**
 * Tests for OutgoingCallUIState sealed class.
 * Verifies construction, type checking, property access, and data class equality
 * for all variants: Idle, Calling, Accepted, Rejected, OngoingCall, Error.
 *
 * Layer 4 — pure data class tests, no mocking of the UIState itself.
 * Call and CometChatException objects are mocked because the SDK classes
 * have private constructors.
 *
 * Reference: IncomingCallUIStateTest.kt
 *
 * Validates: Requirements 10.1–10.7
 *
 * Run with:
 * ./gradlew :chatuikit-core:testDebugUnitTest --tests "*OutgoingCallUIStateTest"
 */
class OutgoingCallUIStateTest : FunSpec({

    // ==================== Idle ====================

    test("Idle should be a singleton instance") {
        val state: OutgoingCallUIState = OutgoingCallUIState.Idle

        println("  → Verifying Idle is a singleton instance of OutgoingCallUIState")

        state.shouldBeInstanceOf<OutgoingCallUIState.Idle>()

        println("  ✅ Idle is OutgoingCallUIState.Idle")
    }

    test("Idle should be the same reference on multiple accesses") {
        val state1 = OutgoingCallUIState.Idle
        val state2 = OutgoingCallUIState.Idle

        println("  → Verifying Idle is referentially equal across accesses")

        (state1 === state2) shouldBe true

        println("  ✅ Idle is referentially equal")
    }

    // ==================== Calling ====================

    test("Calling should hold the outgoing Call object and optional User") {
        val call = MockFactory.createCall(
            sessionId = "session-calling-1",
            type = "audio",
            callerUid = "caller-1",
            callerName = "Alice"
        )
        val user = MockFactory.createUser(uid = "receiver-1", name = "Bob")

        val state = OutgoingCallUIState.Calling(call, user)

        println("  → Verifying Calling holds the Call object with sessionId=${call.sessionId} and user")

        state.shouldBeInstanceOf<OutgoingCallUIState.Calling>()
        state.call shouldBe call
        state.call.sessionId shouldBe "session-calling-1"
        state.user shouldBe user
        state.user?.uid shouldBe "receiver-1"
        state.user?.name shouldBe "Bob"

        println("  ✅ Calling holds call with sessionId=session-calling-1 and user=Bob")
    }

    test("Calling should allow null user (group calls)") {
        val call = MockFactory.createCall(sessionId = "session-calling-group")

        val state = OutgoingCallUIState.Calling(call, null)

        println("  → Verifying Calling allows null user")

        state.shouldBeInstanceOf<OutgoingCallUIState.Calling>()
        state.call shouldBe call
        state.user shouldBe null

        println("  ✅ Calling with null user works (group call scenario)")
    }

    test("Calling data class equality should work with same call and user reference") {
        val call = MockFactory.createCall(sessionId = "session-eq-calling")
        val user = MockFactory.createUser(uid = "user-eq")

        val state1 = OutgoingCallUIState.Calling(call, user)
        val state2 = OutgoingCallUIState.Calling(call, user)

        println("  → Verifying Calling data class equality with same references")

        state1 shouldBe state2

        println("  ✅ Calling equality works for identical references")
    }

    test("Calling with different call objects should not be equal") {
        val call1 = MockFactory.createCall(sessionId = "session-a")
        val call2 = MockFactory.createCall(sessionId = "session-b")
        val user = MockFactory.createUser(uid = "user-1")

        val state1 = OutgoingCallUIState.Calling(call1, user)
        val state2 = OutgoingCallUIState.Calling(call2, user)

        println("  → Verifying Calling with different calls are not equal")

        (state1 == state2) shouldBe false

        println("  ✅ Calling with different calls are not equal")
    }

    // ==================== Accepted ====================

    test("Accepted should hold the accepted Call object") {
        val call = MockFactory.createCall(
            sessionId = "session-accept-1",
            type = "video",
            callerUid = "caller-2",
            callerName = "Bob"
        )

        val state = OutgoingCallUIState.Accepted(call)

        println("  → Verifying Accepted holds the Call object with sessionId=${call.sessionId}")

        state.shouldBeInstanceOf<OutgoingCallUIState.Accepted>()
        state.call shouldBe call
        state.call.sessionId shouldBe "session-accept-1"
        state.call.type shouldBe "video"

        println("  ✅ Accepted holds call with sessionId=session-accept-1, type=video")
    }

    test("Accepted data class equality should work with same call reference") {
        val call = MockFactory.createCall(sessionId = "session-eq-accept")

        val state1 = OutgoingCallUIState.Accepted(call)
        val state2 = OutgoingCallUIState.Accepted(call)

        println("  → Verifying Accepted data class equality")

        state1 shouldBe state2

        println("  ✅ Accepted equality works")
    }

    test("Accepted with different call objects should not be equal") {
        val call1 = MockFactory.createCall(sessionId = "accept-a")
        val call2 = MockFactory.createCall(sessionId = "accept-b")

        val state1 = OutgoingCallUIState.Accepted(call1)
        val state2 = OutgoingCallUIState.Accepted(call2)

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

        val state = OutgoingCallUIState.Rejected(call)

        println("  → Verifying Rejected holds the Call object with sessionId=${call.sessionId}")

        state.shouldBeInstanceOf<OutgoingCallUIState.Rejected>()
        state.call shouldBe call
        state.call.sessionId shouldBe "session-reject-1"

        println("  ✅ Rejected holds call with sessionId=session-reject-1")
    }

    test("Rejected data class equality should work with same call reference") {
        val call = MockFactory.createCall(sessionId = "session-eq-reject")

        val state1 = OutgoingCallUIState.Rejected(call)
        val state2 = OutgoingCallUIState.Rejected(call)

        println("  → Verifying Rejected data class equality")

        state1 shouldBe state2

        println("  ✅ Rejected equality works")
    }

    test("Rejected with different call objects should not be equal") {
        val call1 = MockFactory.createCall(sessionId = "reject-a")
        val call2 = MockFactory.createCall(sessionId = "reject-b")

        val state1 = OutgoingCallUIState.Rejected(call1)
        val state2 = OutgoingCallUIState.Rejected(call2)

        println("  → Verifying Rejected with different calls are not equal")

        (state1 == state2) shouldBe false

        println("  ✅ Rejected with different calls are not equal")
    }

    // ==================== OngoingCall ====================

    test("OngoingCall should hold sessionId and callType") {
        val state = OutgoingCallUIState.OngoingCall(
            sessionId = "ongoing-session-1",
            callType = "video"
        )

        println("  → Verifying OngoingCall holds sessionId and callType")

        state.shouldBeInstanceOf<OutgoingCallUIState.OngoingCall>()
        state.sessionId shouldBe "ongoing-session-1"
        state.callType shouldBe "video"

        println("  ✅ OngoingCall holds sessionId=ongoing-session-1, callType=video")
    }

    test("OngoingCall data class equality should work with same values") {
        val state1 = OutgoingCallUIState.OngoingCall("session-eq", "audio")
        val state2 = OutgoingCallUIState.OngoingCall("session-eq", "audio")

        println("  → Verifying OngoingCall data class equality")

        state1 shouldBe state2

        println("  ✅ OngoingCall equality works for identical values")
    }

    test("OngoingCall with different sessionIds should not be equal") {
        val state1 = OutgoingCallUIState.OngoingCall("session-1", "audio")
        val state2 = OutgoingCallUIState.OngoingCall("session-2", "audio")

        println("  → Verifying OngoingCall with different sessionIds are not equal")

        (state1 == state2) shouldBe false

        println("  ✅ OngoingCall with different sessionIds are not equal")
    }

    test("OngoingCall with different callTypes should not be equal") {
        val state1 = OutgoingCallUIState.OngoingCall("session-1", "audio")
        val state2 = OutgoingCallUIState.OngoingCall("session-1", "video")

        println("  → Verifying OngoingCall with different callTypes are not equal")

        (state1 == state2) shouldBe false

        println("  ✅ OngoingCall with different callTypes are not equal")
    }

    // ==================== Error ====================

    test("Error should hold the CometChatException") {
        val exception = MockFactory.createCometChatException("ERR_CANCEL", "Failed to cancel call")

        val state = OutgoingCallUIState.Error(exception)

        println("  → Verifying Error holds the CometChatException with code=${exception.code}")

        state.shouldBeInstanceOf<OutgoingCallUIState.Error>()
        state.exception shouldBe exception
        state.exception.code shouldBe "ERR_CANCEL"
        state.exception.message shouldBe "Failed to cancel call"

        println("  ✅ Error holds exception with code=ERR_CANCEL")
    }

    test("Error data class equality should work with same exception reference") {
        val exception = MockFactory.createCometChatException("ERR", "Something went wrong")

        val state1 = OutgoingCallUIState.Error(exception)
        val state2 = OutgoingCallUIState.Error(exception)

        println("  → Verifying Error data class equality with same exception reference")

        state1 shouldBe state2

        println("  ✅ Error equality works for identical exceptions")
    }

    test("Error with different exceptions should not be equal") {
        val ex1 = MockFactory.createCometChatException("ERR_1", "Error 1")
        val ex2 = MockFactory.createCometChatException("ERR_2", "Error 2")

        val state1 = OutgoingCallUIState.Error(ex1)
        val state2 = OutgoingCallUIState.Error(ex2)

        println("  → Verifying Error with different exceptions are not equal")

        (state1 == state2) shouldBe false

        println("  ✅ Error with different exceptions are not equal")
    }

    // ==================== Type Discrimination ====================

    test("all variants should be distinguishable via type checking") {
        val call = MockFactory.createCall(sessionId = "session-disc")
        val user = MockFactory.createUser(uid = "user-disc")
        val exception = MockFactory.createCometChatException("ERR", "Test")

        val states: List<OutgoingCallUIState> = listOf(
            OutgoingCallUIState.Idle,
            OutgoingCallUIState.Calling(call, user),
            OutgoingCallUIState.Accepted(call),
            OutgoingCallUIState.Rejected(call),
            OutgoingCallUIState.OngoingCall("session-disc", "audio"),
            OutgoingCallUIState.Error(exception)
        )

        println("  → Verifying all 6 variants are distinguishable via type checking")

        states[0].shouldBeInstanceOf<OutgoingCallUIState.Idle>()
        states[1].shouldBeInstanceOf<OutgoingCallUIState.Calling>()
        states[2].shouldBeInstanceOf<OutgoingCallUIState.Accepted>()
        states[3].shouldBeInstanceOf<OutgoingCallUIState.Rejected>()
        states[4].shouldBeInstanceOf<OutgoingCallUIState.OngoingCall>()
        states[5].shouldBeInstanceOf<OutgoingCallUIState.Error>()

        println("  ✅ All 6 variants are correctly type-discriminated")
    }

    test("when expression should exhaustively match all variants") {
        val call = MockFactory.createCall(sessionId = "session-when")
        val user = MockFactory.createUser(uid = "user-when")
        val exception = MockFactory.createCometChatException("ERR", "Test")

        val states: List<OutgoingCallUIState> = listOf(
            OutgoingCallUIState.Idle,
            OutgoingCallUIState.Calling(call, user),
            OutgoingCallUIState.Accepted(call),
            OutgoingCallUIState.Rejected(call),
            OutgoingCallUIState.OngoingCall("session-when", "video"),
            OutgoingCallUIState.Error(exception)
        )

        println("  → Verifying when expression exhaustively matches all variants")

        val results = states.map { state ->
            when (state) {
                is OutgoingCallUIState.Idle -> "idle"
                is OutgoingCallUIState.Calling -> "calling"
                is OutgoingCallUIState.Accepted -> "accepted"
                is OutgoingCallUIState.Rejected -> "rejected"
                is OutgoingCallUIState.OngoingCall -> "ongoing"
                is OutgoingCallUIState.Error -> "error"
            }
        }

        results shouldBe listOf("idle", "calling", "accepted", "rejected", "ongoing", "error")

        println("  ✅ when expression exhaustively matches: $results")
    }

    // ==================== Calling with different call types ====================

    test("Calling should work with audio call type") {
        val audioCall = MockFactory.createCall(sessionId = "audio-session", type = "audio")
        val state = OutgoingCallUIState.Calling(audioCall, null)

        println("  → Verifying Calling with audio call type")

        state.call.type shouldBe "audio"

        println("  ✅ Calling with audio call type works")
    }

    test("Calling should work with video call type") {
        val videoCall = MockFactory.createCall(sessionId = "video-session", type = "video")
        val state = OutgoingCallUIState.Calling(videoCall, null)

        println("  → Verifying Calling with video call type")

        state.call.type shouldBe "video"

        println("  ✅ Calling with video call type works")
    }
})
