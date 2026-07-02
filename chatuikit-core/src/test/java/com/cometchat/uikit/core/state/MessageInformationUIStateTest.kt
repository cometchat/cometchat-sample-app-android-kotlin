package com.cometchat.uikit.core.state

import com.cometchat.chat.exceptions.CometChatException
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll

/**
 * Tests for MessageInformationUIState sealed class.
 * Pure data class tests — no mocking needed.
 *
 * Validates:
 * - Sealed class variant construction
 * - Type checking / discrimination
 * - Singleton identity for Loading, Loaded, Empty
 * - Data class equality for Error
 * - Property access for Error.exception
 *
 * Run with:
 *   ./gradlew :chatuikit-core:testDebugUnitTest --tests "*.state.MessageInformationUIStateTest"
 */
class MessageInformationUIStateTest : FunSpec({

    beforeTest {
        println("  🧪 ${it.name.testName}")
    }

    afterTest {
        println()
    }

    // ==================== Loading State ====================

    test("Loading should be a singleton instance") {
        println("=== TEST: Loading is singleton ===")
        println("STEP 1: Creating Loading state")
        val state: MessageInformationUIState = MessageInformationUIState.Loading

        println("STEP 2: Asserting type")
        state.shouldBeInstanceOf<MessageInformationUIState.Loading>()

        println("STEP 3: Asserting singleton identity")
        val state2 = MessageInformationUIState.Loading
        (state === state2) shouldBe true

        println("RESULT: Loading is a singleton ✅")
    }

    // ==================== Loaded State ====================

    test("Loaded should be a singleton instance") {
        println("=== TEST: Loaded is singleton ===")
        println("STEP 1: Creating Loaded state")
        val state: MessageInformationUIState = MessageInformationUIState.Loaded

        println("STEP 2: Asserting type")
        state.shouldBeInstanceOf<MessageInformationUIState.Loaded>()

        println("STEP 3: Asserting singleton identity")
        val state2 = MessageInformationUIState.Loaded
        (state === state2) shouldBe true

        println("RESULT: Loaded is a singleton ✅")
    }

    // ==================== Empty State ====================

    test("Empty should be a singleton instance") {
        println("=== TEST: Empty is singleton ===")
        println("STEP 1: Creating Empty state")
        val state: MessageInformationUIState = MessageInformationUIState.Empty

        println("STEP 2: Asserting type")
        state.shouldBeInstanceOf<MessageInformationUIState.Empty>()

        println("STEP 3: Asserting singleton identity")
        val state2 = MessageInformationUIState.Empty
        (state === state2) shouldBe true

        println("RESULT: Empty is a singleton ✅")
    }

    // ==================== Error State ====================

    test("Error should hold the CometChatException") {
        println("=== TEST: Error holds exception ===")
        println("STEP 1: Creating Error state with exception")
        val exception = CometChatException("NET_ERR", "Network error")
        val state = MessageInformationUIState.Error(exception)

        println("STEP 2: Asserting type")
        state.shouldBeInstanceOf<MessageInformationUIState.Error>()

        println("STEP 3: Asserting exception properties")
        state.exception shouldBe exception
        state.exception.code shouldBe "NET_ERR"
        state.exception.message shouldBe "Network error"

        println("RESULT: Error holds exception with code='NET_ERR' ✅")
    }

    test("Error data class equality — same exception produces equal states") {
        println("=== TEST: Error equality ===")
        val exception = CometChatException("ERR", "Test")
        val state1 = MessageInformationUIState.Error(exception)
        val state2 = MessageInformationUIState.Error(exception)

        println("STEP 1: Asserting equality")
        state1 shouldBe state2

        println("RESULT: Same exception → equal Error states ✅")
    }

    test("Error data class inequality — different exceptions produce unequal states") {
        println("=== TEST: Error inequality ===")
        val exception1 = CometChatException("ERR_1", "Error 1")
        val exception2 = CometChatException("ERR_2", "Error 2")
        val state1 = MessageInformationUIState.Error(exception1)
        val state2 = MessageInformationUIState.Error(exception2)

        println("STEP 1: Asserting inequality")
        state1 shouldNotBe state2

        println("RESULT: Different exceptions → unequal Error states ✅")
    }

    // ==================== Type Discrimination ====================

    test("All four states are distinct types") {
        println("=== TEST: Type discrimination ===")
        val loading: MessageInformationUIState = MessageInformationUIState.Loading
        val loaded: MessageInformationUIState = MessageInformationUIState.Loaded
        val empty: MessageInformationUIState = MessageInformationUIState.Empty
        val error: MessageInformationUIState = MessageInformationUIState.Error(CometChatException("E", "e"))

        println("STEP 1: Asserting each is its own type")
        loading.shouldBeInstanceOf<MessageInformationUIState.Loading>()
        loaded.shouldBeInstanceOf<MessageInformationUIState.Loaded>()
        empty.shouldBeInstanceOf<MessageInformationUIState.Empty>()
        error.shouldBeInstanceOf<MessageInformationUIState.Error>()

        println("STEP 2: Asserting they are not equal to each other")
        (loading == loaded) shouldBe false
        (loading == empty) shouldBe false
        (loading == error) shouldBe false
        (loaded == empty) shouldBe false
        (loaded == error) shouldBe false
        (empty == error) shouldBe false

        println("RESULT: All 4 states are distinct ✅")
    }

    // ==================== PBT: Error with any code/message ====================

    test("PBT: Error state preserves any exception code and message") {
        println("=== PBT: Error preserves any code/message ===")
        checkAll(20, Arb.string(1..20), Arb.string(1..100)) { code, message ->
            val exception = CometChatException(code, message)
            val state = MessageInformationUIState.Error(exception)

            state.exception.code shouldBe code
            state.exception.message shouldBe message
            println("  [Iteration] code='$code', message='${message.take(20)}...' ✅")
        }
    }

    // ==================== when() exhaustiveness ====================

    test("when() expression covers all variants") {
        println("=== TEST: when() exhaustiveness ===")
        val states = listOf(
            MessageInformationUIState.Loading,
            MessageInformationUIState.Loaded,
            MessageInformationUIState.Empty,
            MessageInformationUIState.Error(CometChatException("E", "e"))
        )

        states.forEach { state ->
            val label = when (state) {
                is MessageInformationUIState.Loading -> "Loading"
                is MessageInformationUIState.Loaded -> "Loaded"
                is MessageInformationUIState.Empty -> "Empty"
                is MessageInformationUIState.Error -> "Error(${state.exception.code})"
            }
            println("  → $label")
            label shouldNotBe null
        }

        println("RESULT: All 4 variants covered in when() ✅")
    }
})
