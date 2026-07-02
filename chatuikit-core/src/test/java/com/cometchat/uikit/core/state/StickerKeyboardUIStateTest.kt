package com.cometchat.uikit.core.state

import com.cometchat.chat.exceptions.CometChatException
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.types.shouldBeSameInstanceAs

/**
 * Tests for StickerKeyboardUIState sealed class.
 *
 * Verifies:
 * - Loading is a singleton (object)
 * - Content is a singleton (object)
 * - Empty is a singleton (object)
 * - Error holds exception with correct code and message
 * - Type checking works correctly
 * - Equality behavior for Error data class
 *
 * Run with:
 *   ./gradlew :chatuikit-core:testDebugUnitTest --tests "*.StickerKeyboardUIStateTest"
 */
class StickerKeyboardUIStateTest : FunSpec({

    context("Loading state") {

        test("Loading is a singleton instance") {
            val state: StickerKeyboardUIState = StickerKeyboardUIState.Loading
            state.shouldBeInstanceOf<StickerKeyboardUIState.Loading>()
            println("    ✅ Loading is correct type")
        }

        test("Loading references are the same instance") {
            val state1 = StickerKeyboardUIState.Loading
            val state2 = StickerKeyboardUIState.Loading
            state1 shouldBeSameInstanceAs state2
            println("    ✅ Loading is singleton")
        }

        test("Loading is not Content, Empty, or Error") {
            val state: StickerKeyboardUIState = StickerKeyboardUIState.Loading
            (state is StickerKeyboardUIState.Content) shouldBe false
            (state is StickerKeyboardUIState.Empty) shouldBe false
            (state is StickerKeyboardUIState.Error) shouldBe false
            println("    ✅ Loading is distinct from other states")
        }
    }

    context("Content state") {

        test("Content is a singleton instance") {
            val state: StickerKeyboardUIState = StickerKeyboardUIState.Content
            state.shouldBeInstanceOf<StickerKeyboardUIState.Content>()
            println("    ✅ Content is correct type")
        }

        test("Content references are the same instance") {
            val state1 = StickerKeyboardUIState.Content
            val state2 = StickerKeyboardUIState.Content
            state1 shouldBeSameInstanceAs state2
            println("    ✅ Content is singleton")
        }

        test("Content is not Loading, Empty, or Error") {
            val state: StickerKeyboardUIState = StickerKeyboardUIState.Content
            (state is StickerKeyboardUIState.Loading) shouldBe false
            (state is StickerKeyboardUIState.Empty) shouldBe false
            (state is StickerKeyboardUIState.Error) shouldBe false
            println("    ✅ Content is distinct from other states")
        }
    }

    context("Empty state") {

        test("Empty is a singleton instance") {
            val state: StickerKeyboardUIState = StickerKeyboardUIState.Empty
            state.shouldBeInstanceOf<StickerKeyboardUIState.Empty>()
            println("    ✅ Empty is correct type")
        }

        test("Empty references are the same instance") {
            val state1 = StickerKeyboardUIState.Empty
            val state2 = StickerKeyboardUIState.Empty
            state1 shouldBeSameInstanceAs state2
            println("    ✅ Empty is singleton")
        }

        test("Empty is not Loading, Content, or Error") {
            val state: StickerKeyboardUIState = StickerKeyboardUIState.Empty
            (state is StickerKeyboardUIState.Loading) shouldBe false
            (state is StickerKeyboardUIState.Content) shouldBe false
            (state is StickerKeyboardUIState.Error) shouldBe false
            println("    ✅ Empty is distinct from other states")
        }
    }

    context("Error state") {

        test("Error holds the exception") {
            val exception = CometChatException("ERR_FETCH", "Network error")
            val state = StickerKeyboardUIState.Error(exception)

            state.shouldBeInstanceOf<StickerKeyboardUIState.Error>()
            state.exception shouldBe exception
            state.exception.code shouldBe "ERR_FETCH"
            state.exception.message shouldBe "Network error"
            println("    ✅ Error holds exception with correct code and message")
        }

        test("Error is not Loading, Content, or Empty") {
            val exception = CometChatException("ERR", "Fail")
            val state: StickerKeyboardUIState = StickerKeyboardUIState.Error(exception)
            (state is StickerKeyboardUIState.Loading) shouldBe false
            (state is StickerKeyboardUIState.Content) shouldBe false
            (state is StickerKeyboardUIState.Empty) shouldBe false
            println("    ✅ Error is distinct from other states")
        }

        test("Two Error states with same exception are equal (data class)") {
            val exception = CometChatException("ERR", "Fail")
            val state1 = StickerKeyboardUIState.Error(exception)
            val state2 = StickerKeyboardUIState.Error(exception)

            state1 shouldBe state2
            state1.hashCode() shouldBe state2.hashCode()
            println("    ✅ Error equality works via data class")
        }

        test("Two Error states with different exceptions are not equal") {
            val exception1 = CometChatException("ERR_1", "First error")
            val exception2 = CometChatException("ERR_2", "Second error")
            val state1 = StickerKeyboardUIState.Error(exception1)
            val state2 = StickerKeyboardUIState.Error(exception2)

            state1 shouldNotBe state2
            println("    ✅ Different errors are not equal")
        }
    }

    context("when expression exhaustiveness") {

        test("All states can be matched in when expression") {
            val states = listOf(
                StickerKeyboardUIState.Loading,
                StickerKeyboardUIState.Content,
                StickerKeyboardUIState.Empty,
                StickerKeyboardUIState.Error(CometChatException("ERR", "Fail"))
            )

            states.forEach { state ->
                val result = when (state) {
                    is StickerKeyboardUIState.Loading -> "loading"
                    is StickerKeyboardUIState.Content -> "content"
                    is StickerKeyboardUIState.Empty -> "empty"
                    is StickerKeyboardUIState.Error -> "error"
                }
                (result.isNotEmpty()) shouldBe true
            }
            println("    ✅ All 4 states matched in when expression")
        }
    }
})
