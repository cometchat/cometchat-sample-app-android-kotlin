package com.cometchat.uikit.core.state

import com.cometchat.calls.exceptions.CometChatException
import com.cometchat.calls.model.CallLog
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * Tests for CallLogsUIState sealed class.
 * Verifies construction, type checking, property access, and data class equality
 * for all variants: Loading, Empty, Content, Error.
 *
 * Layer 4 — pure data class tests, no mocking of the UIState itself.
 * CallLog and CometChatException objects are mocked because the calls SDK
 * classes have private constructors and are testCompileOnly.
 *
 * Reference: ConversationUIStateTest.kt
 *
 * Validates: Requirements 4.1, 4.3, 4.4, 4.5
 *
 * Run with:
 * ./gradlew :chatuikit-core:testDebugUnitTest --tests "*CallLogsUIStateTest"
 */
class CallLogsUIStateTest : FunSpec({

    // ==================== Loading ====================

    test("Loading should be a singleton instance") {
        val state: CallLogsUIState = CallLogsUIState.Loading

        println("  → Verifying Loading is a singleton instance of CallLogsUIState")

        state.shouldBeInstanceOf<CallLogsUIState.Loading>()

        println("  ✅ Loading is CallLogsUIState.Loading")
    }

    // ==================== Empty ====================

    test("Empty should be a singleton instance") {
        val state: CallLogsUIState = CallLogsUIState.Empty

        println("  → Verifying Empty is a singleton instance of CallLogsUIState")

        state.shouldBeInstanceOf<CallLogsUIState.Empty>()

        println("  ✅ Empty is CallLogsUIState.Empty")
    }

    // ==================== Content ====================

    test("Content should hold the call logs list") {
        val callLog1 = mock<CallLog>()
        val callLog2 = mock<CallLog>()
        val callLog3 = mock<CallLog>()

        val callLogs = listOf(callLog1, callLog2, callLog3)
        val state = CallLogsUIState.Content(callLogs)

        println("  → Verifying Content holds the call logs list with ${callLogs.size} items")

        state.shouldBeInstanceOf<CallLogsUIState.Content>()
        state.callLogs shouldBe callLogs
        state.callLogs.size shouldBe 3

        println("  ✅ Content holds ${state.callLogs.size} call logs")
    }

    test("Content data class equality should work") {
        val callLog1 = mock<CallLog>()
        val callLog2 = mock<CallLog>()
        val callLogs = listOf(callLog1, callLog2)

        val state1 = CallLogsUIState.Content(callLogs)
        val state2 = CallLogsUIState.Content(callLogs)

        println("  → Verifying Content data class equality with same list reference")

        state1 shouldBe state2

        println("  ✅ Content equality works for identical data")
    }

    test("Content with different lists should not be equal") {
        val callLog1 = mock<CallLog>()
        val callLog2 = mock<CallLog>()

        val state1 = CallLogsUIState.Content(listOf(callLog1))
        val state2 = CallLogsUIState.Content(listOf(callLog2))

        println("  → Verifying Content with different lists are not equal")

        (state1 == state2) shouldBe false

        println("  ✅ Content with different lists are not equal")
    }

    test("Content with empty list should be valid") {
        val state = CallLogsUIState.Content(emptyList())

        println("  → Verifying Content can hold an empty list")

        state.shouldBeInstanceOf<CallLogsUIState.Content>()
        state.callLogs shouldBe emptyList()
        state.callLogs.size shouldBe 0

        println("  ✅ Content with empty list is valid")
    }

    // ==================== Error ====================

    test("Error should hold the exception") {
        val exception = mock<CometChatException>()
        whenever(exception.code).thenReturn("ERR_FETCH")
        whenever(exception.message).thenReturn("Failed to fetch call logs")

        val state = CallLogsUIState.Error(exception)

        println("  → Verifying Error holds the CometChatException")

        state.shouldBeInstanceOf<CallLogsUIState.Error>()
        state.exception shouldBe exception
        state.exception.code shouldBe "ERR_FETCH"
        state.exception.message shouldBe "Failed to fetch call logs"

        println("  ✅ Error holds exception with code=${state.exception.code}")
    }

    test("Error data class equality should work") {
        val exception = mock<CometChatException>()
        whenever(exception.code).thenReturn("ERR")
        whenever(exception.message).thenReturn("Something went wrong")

        val state1 = CallLogsUIState.Error(exception)
        val state2 = CallLogsUIState.Error(exception)

        println("  → Verifying Error data class equality with same exception reference")

        state1 shouldBe state2

        println("  ✅ Error equality works for identical exceptions")
    }

    test("Error with different exceptions should not be equal") {
        val ex1 = mock<CometChatException>()
        val ex2 = mock<CometChatException>()
        whenever(ex1.code).thenReturn("ERR_1")
        whenever(ex2.code).thenReturn("ERR_2")

        val state1 = CallLogsUIState.Error(ex1)
        val state2 = CallLogsUIState.Error(ex2)

        println("  → Verifying Error with different exceptions are not equal")

        (state1 == state2) shouldBe false

        println("  ✅ Error with different exceptions are not equal")
    }

    // ==================== Type Discrimination ====================

    test("all variants should be distinguishable via type checking") {
        val callLogs = listOf(mock<CallLog>())
        val exception = mock<CometChatException>()

        val states: List<CallLogsUIState> = listOf(
            CallLogsUIState.Loading,
            CallLogsUIState.Empty,
            CallLogsUIState.Content(callLogs),
            CallLogsUIState.Error(exception)
        )

        println("  → Verifying all 4 variants are distinguishable via type checking")

        states[0].shouldBeInstanceOf<CallLogsUIState.Loading>()
        states[1].shouldBeInstanceOf<CallLogsUIState.Empty>()
        states[2].shouldBeInstanceOf<CallLogsUIState.Content>()
        states[3].shouldBeInstanceOf<CallLogsUIState.Error>()

        println("  ✅ All 4 variants are correctly type-discriminated")
    }
})
