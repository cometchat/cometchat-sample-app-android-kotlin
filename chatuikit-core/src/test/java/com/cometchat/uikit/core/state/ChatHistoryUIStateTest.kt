package com.cometchat.uikit.core.state

import com.cometchat.chat.models.BaseMessage
import com.cometchat.uikit.core.testutils.MockFactory
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll

/**
 * Tests for ChatHistoryUIState sealed class.
 * Pure data class tests — no mocking needed for state verification.
 *
 * Verifies:
 * - Loading is a singleton object
 * - Empty is a singleton object
 * - Error holds the CometChatException
 * - Content holds the messages list
 * - Data class equality and type checking
 *
 * Run with:
 *   ./gradlew :chatuikit-core:testDebugUnitTest --tests "...ChatHistoryUIStateTest"
 */
class ChatHistoryUIStateTest : FunSpec({

    beforeTest {
        println("  🧪 ${it.name.testName}")
    }

    afterTest {
        println()
    }

    // ==================== Loading ====================

    test("Loading should be a singleton object instance") {
        val state: ChatHistoryUIState = ChatHistoryUIState.Loading
        state.shouldBeInstanceOf<ChatHistoryUIState.Loading>()
        println("    → ChatHistoryUIState.Loading is singleton")
        println("    ✅ Loading is object (singleton)")
    }

    test("Loading should equal itself") {
        val state1 = ChatHistoryUIState.Loading
        val state2 = ChatHistoryUIState.Loading
        (state1 === state2) shouldBe true
        println("    ✅ Loading referential equality (same instance)")
    }

    // ==================== Empty ====================

    test("Empty should be a singleton object instance") {
        val state: ChatHistoryUIState = ChatHistoryUIState.Empty
        state.shouldBeInstanceOf<ChatHistoryUIState.Empty>()
        println("    → ChatHistoryUIState.Empty is singleton")
        println("    ✅ Empty is object (singleton)")
    }

    test("Empty should equal itself") {
        val state1 = ChatHistoryUIState.Empty
        val state2 = ChatHistoryUIState.Empty
        (state1 === state2) shouldBe true
        println("    ✅ Empty referential equality (same instance)")
    }

    // ==================== Error ====================

    test("Error should hold the CometChatException") {
        val exception = MockFactory.createCometChatException("ERR_FETCH", "Network error")
        val state = ChatHistoryUIState.Error(exception)

        state.shouldBeInstanceOf<ChatHistoryUIState.Error>()
        state.exception shouldBe exception
        state.exception.code shouldBe "ERR_FETCH"
        state.exception.message shouldBe "Network error"
        println("    → Error(exception.code='ERR_FETCH', message='Network error')")
        println("    ✅ Error holds exception with correct code and message")
    }

    test("for any error code and message: Error should preserve them") {
        checkAll(30, Arb.string(1..20), Arb.string(1..50)) { code, message ->
            val exception = MockFactory.createCometChatException(code, message)
            val state = ChatHistoryUIState.Error(exception)

            state.exception.code shouldBe code
            state.exception.message shouldBe message
            println("    → code='$code', message='$message' ✅")
        }
    }

    test("two Error states with same exception should be equal") {
        val exception = MockFactory.createCometChatException("ERR", "Same error")
        val state1 = ChatHistoryUIState.Error(exception)
        val state2 = ChatHistoryUIState.Error(exception)

        (state1 == state2) shouldBe true
        println("    ✅ Error equality with same exception reference")
    }

    test("two Error states with different exceptions should not be equal") {
        val exception1 = MockFactory.createCometChatException("ERR_1", "Error 1")
        val exception2 = MockFactory.createCometChatException("ERR_2", "Error 2")
        val state1 = ChatHistoryUIState.Error(exception1)
        val state2 = ChatHistoryUIState.Error(exception2)

        (state1 == state2) shouldBe false
        println("    ✅ Error inequality with different exceptions")
    }

    // ==================== Content ====================

    test("Content should hold the messages list") {
        val messages = listOf(
            MockFactory.createTextMessage(id = 1L, text = "Hello"),
            MockFactory.createTextMessage(id = 2L, text = "World")
        )
        val state = ChatHistoryUIState.Content(messages)

        state.shouldBeInstanceOf<ChatHistoryUIState.Content>()
        state.messages shouldHaveSize 2
        println("    → Content(messages.size=2)")
        println("    ✅ Content holds messages list with correct size")
    }

    test("Content with empty list should still be Content type") {
        val state = ChatHistoryUIState.Content(emptyList())

        state.shouldBeInstanceOf<ChatHistoryUIState.Content>()
        state.messages shouldHaveSize 0
        println("    → Content(messages.size=0)")
        println("    ✅ Content with empty list is valid")
    }

    test("for any message count: Content should hold correct number of messages") {
        checkAll(30, Arb.int(0..20)) { count ->
            val messages = (1..count).map { i ->
                MockFactory.createTextMessage(id = i.toLong(), text = "Message $i")
            }
            val state = ChatHistoryUIState.Content(messages)

            state.messages shouldHaveSize count
            println("    → count=$count ✅")
        }
    }

    test("two Content states with same messages should be equal") {
        val messages = listOf(
            MockFactory.createTextMessage(id = 1L, text = "Same")
        )
        val state1 = ChatHistoryUIState.Content(messages)
        val state2 = ChatHistoryUIState.Content(messages)

        (state1 == state2) shouldBe true
        println("    ✅ Content equality with same messages reference")
    }

    // ==================== Type Discrimination ====================

    test("all four states should be distinguishable via when expression") {
        val states = listOf(
            ChatHistoryUIState.Loading,
            ChatHistoryUIState.Empty,
            ChatHistoryUIState.Error(MockFactory.createCometChatException("E", "err")),
            ChatHistoryUIState.Content(emptyList())
        )

        val results = states.map { state ->
            when (state) {
                is ChatHistoryUIState.Loading -> "loading"
                is ChatHistoryUIState.Empty -> "empty"
                is ChatHistoryUIState.Error -> "error"
                is ChatHistoryUIState.Content -> "content"
            }
        }

        results shouldBe listOf("loading", "empty", "error", "content")
        println("    → All 4 states discriminated correctly")
        println("    ✅ when expression covers all sealed class variants")
    }

    test("Loading and Empty should not be equal to each other") {
        val loading: ChatHistoryUIState = ChatHistoryUIState.Loading
        val empty: ChatHistoryUIState = ChatHistoryUIState.Empty

        (loading == empty) shouldBe false
        println("    ✅ Loading != Empty")
    }

    test("Error and Content should not be equal") {
        val error: ChatHistoryUIState = ChatHistoryUIState.Error(
            MockFactory.createCometChatException("E", "err")
        )
        val content: ChatHistoryUIState = ChatHistoryUIState.Content(emptyList())

        (error == content) shouldBe false
        println("    ✅ Error != Content")
    }
})
