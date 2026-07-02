package com.cometchat.uikit.core.state

import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.uikit.core.testutils.MockFactory
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

/**
 * Tests for CallButtonsUIState sealed class.
 * Verifies construction, type checking, property access, and data class equality
 * for all variants: Idle, Initiating, Error.
 *
 * Layer 4 — pure data class tests, no mocking of the UIState itself.
 * CometChatException is created via MockFactory.
 *
 * Reference: CallLogsUIStateTest.kt
 *
 * Validates: Requirements 4.1, 4.2
 *
 * Run with:
 * ./gradlew :chatuikit-core:testDebugUnitTest --tests "*CallButtonsUIStateTest"
 */
class CallButtonsUIStateTest : FunSpec({

    // ==================== Idle ====================

    test("Idle should be a singleton instance") {
        val state: CallButtonsUIState = CallButtonsUIState.Idle

        println("  → Verifying Idle is a singleton instance of CallButtonsUIState")

        state.shouldBeInstanceOf<CallButtonsUIState.Idle>()

        println("  ✅ Idle is CallButtonsUIState.Idle")
    }

    test("Idle should be the default state") {
        val state = CallButtonsUIState.Idle

        println("  → Verifying Idle is the default/ready state")

        state shouldBe CallButtonsUIState.Idle

        println("  ✅ Idle is the default state for call buttons")
    }

    // ==================== Initiating ====================

    test("Initiating should be a singleton instance") {
        val state: CallButtonsUIState = CallButtonsUIState.Initiating

        println("  → Verifying Initiating is a singleton instance of CallButtonsUIState")

        state.shouldBeInstanceOf<CallButtonsUIState.Initiating>()

        println("  ✅ Initiating is CallButtonsUIState.Initiating")
    }

    // ==================== Error ====================

    test("Error should hold the exception") {
        val exception = MockFactory.createCometChatException("ERR_CALL", "Call initiation failed")
        val state = CallButtonsUIState.Error(exception)

        println("  → Verifying Error holds the CometChatException")

        state.shouldBeInstanceOf<CallButtonsUIState.Error>()
        state.exception shouldBe exception
        state.exception.code shouldBe "ERR_CALL"
        state.exception.message shouldBe "Call initiation failed"

        println("  ✅ Error holds exception with code=${state.exception.code}")
    }

    test("Error data class equality should work") {
        val exception = MockFactory.createCometChatException("ERR", "Something went wrong")

        val state1 = CallButtonsUIState.Error(exception)
        val state2 = CallButtonsUIState.Error(exception)

        println("  → Verifying Error data class equality with same exception reference")

        state1 shouldBe state2

        println("  ✅ Error equality works for identical exceptions")
    }

    test("Error with different exceptions should not be equal") {
        val ex1 = MockFactory.createCometChatException("ERR_1", "Error 1")
        val ex2 = MockFactory.createCometChatException("ERR_2", "Error 2")

        val state1 = CallButtonsUIState.Error(ex1)
        val state2 = CallButtonsUIState.Error(ex2)

        println("  → Verifying Error with different exceptions are not equal")

        (state1 == state2) shouldBe false

        println("  ✅ Error with different exceptions are not equal")
    }

    // ==================== Type Discrimination ====================

    test("all variants should be distinguishable via type checking") {
        val exception = MockFactory.createCometChatException("ERR", "Test error")

        val states: List<CallButtonsUIState> = listOf(
            CallButtonsUIState.Idle,
            CallButtonsUIState.Initiating,
            CallButtonsUIState.Error(exception)
        )

        println("  → Verifying all 3 variants are distinguishable via type checking")

        states[0].shouldBeInstanceOf<CallButtonsUIState.Idle>()
        states[1].shouldBeInstanceOf<CallButtonsUIState.Initiating>()
        states[2].shouldBeInstanceOf<CallButtonsUIState.Error>()

        println("  ✅ All 3 variants are correctly type-discriminated")
    }

    // ==================== CallButtonsEvent ====================

    context("CallButtonsEvent") {

        test("CallInitiated should hold the Call object") {
            val mockCall = org.mockito.kotlin.mock<com.cometchat.chat.core.Call>()
            val event = CallButtonsEvent.CallInitiated(mockCall)

            println("  → Verifying CallInitiated holds the Call object")

            event.shouldBeInstanceOf<CallButtonsEvent.CallInitiated>()
            event.call shouldBe mockCall

            println("  ✅ CallInitiated holds the Call object")
        }

        test("StartDirectCall should hold the BaseMessage object") {
            val mockMessage = org.mockito.kotlin.mock<com.cometchat.chat.models.BaseMessage>()
            val event = CallButtonsEvent.StartDirectCall(mockMessage)

            println("  → Verifying StartDirectCall holds the BaseMessage object")

            event.shouldBeInstanceOf<CallButtonsEvent.StartDirectCall>()
            event.message shouldBe mockMessage

            println("  ✅ StartDirectCall holds the BaseMessage object")
        }

        test("CallRejected should hold the Call object") {
            val mockCall = org.mockito.kotlin.mock<com.cometchat.chat.core.Call>()
            val event = CallButtonsEvent.CallRejected(mockCall)

            println("  → Verifying CallRejected holds the Call object")

            event.shouldBeInstanceOf<CallButtonsEvent.CallRejected>()
            event.call shouldBe mockCall

            println("  ✅ CallRejected holds the Call object")
        }

        test("all event variants should be distinguishable via type checking") {
            val mockCall = org.mockito.kotlin.mock<com.cometchat.chat.core.Call>()
            val mockMessage = org.mockito.kotlin.mock<com.cometchat.chat.models.BaseMessage>()

            val events: List<CallButtonsEvent> = listOf(
                CallButtonsEvent.CallInitiated(mockCall),
                CallButtonsEvent.StartDirectCall(mockMessage),
                CallButtonsEvent.CallRejected(mockCall)
            )

            println("  → Verifying all 3 event variants are distinguishable")

            events[0].shouldBeInstanceOf<CallButtonsEvent.CallInitiated>()
            events[1].shouldBeInstanceOf<CallButtonsEvent.StartDirectCall>()
            events[2].shouldBeInstanceOf<CallButtonsEvent.CallRejected>()

            println("  ✅ All 3 event variants are correctly type-discriminated")
        }
    }
})
