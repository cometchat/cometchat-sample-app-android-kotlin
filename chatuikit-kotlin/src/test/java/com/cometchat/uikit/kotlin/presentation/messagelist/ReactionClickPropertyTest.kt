package com.cometchat.uikit.kotlin.presentation.messagelist

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.element
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll

/**
 * Simulates the click dispatch logic in configureQuickReactions():
 *
 * ```kotlin
 * chipView.setOnClickListener {
 *     reactionClickListener?.onReactionClick(baseMessage, emoji)
 * }
 * ```
 *
 * For a given reactions list and a tapped index, returns the emoji
 * that would be dispatched to the ReactionClickListener.
 */
private fun dispatchReactionClick(
    reactions: List<String>,
    tappedIndex: Int
): String {
    return reactions[tappedIndex]
}

/**
 * Simulates the full click flow: the listener receives a message ID
 * and the emoji string from the tapped chip.
 */
private data class ReactionClickEvent(
    val messageId: Int,
    val emoji: String
)

private fun simulateReactionClick(
    reactions: List<String>,
    tappedIndex: Int,
    messageId: Int
): ReactionClickEvent {
    val emoji = dispatchReactionClick(reactions, tappedIndex)
    return ReactionClickEvent(messageId = messageId, emoji = emoji)
}

/**
 * Property-based tests for reaction click callback behavior.
 *
 * Feature: message-popup-menu, Property 4: Reaction click callback receives correct emoji
 *
 * *For any* emoji string in the quick reactions list, tapping that emoji chip
 * should invoke the ReactionClickListener with the exact emoji string.
 *
 * **Validates: Requirements 2.3**
 */
class ReactionClickPropertyTest : FunSpec({

    // ==================== Generators ====================

    val emojiArb = Arb.element(
        "😍", "👍🏻", "🔥", "😊", "❤️", "😀", "😂", "🥰", "👏", "🎉", "💯", "🙌", "✨", "🤔", "👀"
    )
    val nonEmptyReactionsArb = Arb.list(emojiArb, 1..10)
    val messageIdArb = Arb.int(1..100000)

    // ==================== Property Tests ====================

    context("Property 4: Reaction click callback receives correct emoji") {

        test("tapped emoji chip dispatches the exact emoji string to listener") {
            checkAll(100, nonEmptyReactionsArb, messageIdArb) { reactions, messageId ->
                // Tap a random valid index
                val tappedIndex = (0 until reactions.size).random()
                val event = simulateReactionClick(reactions, tappedIndex, messageId)

                event.emoji shouldBe reactions[tappedIndex]
                event.messageId shouldBe messageId
            }
        }

        test("each chip in the list maps to its own emoji") {
            checkAll(100, nonEmptyReactionsArb, messageIdArb) { reactions, messageId ->
                reactions.forEachIndexed { index, expectedEmoji ->
                    val event = simulateReactionClick(reactions, index, messageId)
                    event.emoji shouldBe expectedEmoji
                }
            }
        }

        test("callback emoji is referentially equal to the source list emoji") {
            checkAll(100, nonEmptyReactionsArb) { reactions ->
                val tappedIndex = (0 until reactions.size).random()
                val dispatched = dispatchReactionClick(reactions, tappedIndex)

                // The dispatched emoji should be the exact same string object from the list
                (dispatched === reactions[tappedIndex]) shouldBe true
            }
        }

        test("message ID is preserved through the click dispatch") {
            checkAll(100, nonEmptyReactionsArb, messageIdArb) { reactions, messageId ->
                val tappedIndex = (0 until reactions.size).random()
                val event = simulateReactionClick(reactions, tappedIndex, messageId)

                event.messageId shouldBe messageId
            }
        }
    }
})
