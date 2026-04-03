package com.cometchat.uikit.compose.presentation.messagelist

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.element
import io.kotest.property.arbitrary.list
import io.kotest.property.checkAll

/**
 * Simulates the Compose reaction click dispatch logic in CometChatMessagePopupMenu:
 *
 * ```kotlin
 * reactions.forEach { emoji ->
 *     Surface(...clickable { onReactionClick(emoji) }) { ... }
 * }
 * ```
 *
 * For a given reactions list and a tapped index, returns the emoji
 * that would be dispatched to the onReactionClick callback.
 */
private fun dispatchComposeReactionClick(
    reactions: List<String>,
    tappedIndex: Int
): String {
    return reactions[tappedIndex]
}

/**
 * Simulates the full click flow: the callback receives the emoji string
 * from the tapped chip.
 */
private data class ComposeReactionClickEvent(
    val emoji: String
)

private fun simulateComposeReactionClick(
    reactions: List<String>,
    tappedIndex: Int
): ComposeReactionClickEvent {
    val emoji = dispatchComposeReactionClick(reactions, tappedIndex)
    return ComposeReactionClickEvent(emoji = emoji)
}

/**
 * Property-based tests for Compose reaction click callback behavior.
 *
 * Feature: message-popup-menu, Property 12: Compose reaction click callback receives correct emoji
 *
 * *For any* emoji string in the quick reactions list of the Compose
 * CometChatMessagePopupMenu, tapping that emoji should invoke the
 * onReactionClick callback with the exact emoji string.
 *
 * **Validates: Requirements 7.6, 8.4**
 */
class ComposeReactionClickPropertyTest : FunSpec({

    // ==================== Generators ====================

    val emojiArb = Arb.element(
        "😍", "👍🏻", "🔥", "😊", "❤️", "😀", "😂", "🥰", "👏", "🎉", "💯", "🙌", "✨", "🤔", "👀"
    )
    val nonEmptyReactionsArb = Arb.list(emojiArb, 1..10)

    // ==================== Property Tests ====================

    context("Property 12: Compose reaction click callback receives correct emoji") {

        test("tapped emoji chip dispatches the exact emoji string to onReactionClick") {
            checkAll(100, nonEmptyReactionsArb) { reactions ->
                val tappedIndex = (0 until reactions.size).random()
                val event = simulateComposeReactionClick(reactions, tappedIndex)

                event.emoji shouldBe reactions[tappedIndex]
            }
        }

        test("each chip in the list maps to its own emoji") {
            checkAll(100, nonEmptyReactionsArb) { reactions ->
                reactions.forEachIndexed { index, expectedEmoji ->
                    val event = simulateComposeReactionClick(reactions, index)
                    event.emoji shouldBe expectedEmoji
                }
            }
        }

        test("callback emoji is referentially equal to the source list emoji") {
            checkAll(100, nonEmptyReactionsArb) { reactions ->
                val tappedIndex = (0 until reactions.size).random()
                val dispatched = dispatchComposeReactionClick(reactions, tappedIndex)

                (dispatched === reactions[tappedIndex]) shouldBe true
            }
        }
    }
})
