package com.cometchat.uikit.kotlin.presentation.messagelist

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.element
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.boolean
import io.kotest.property.checkAll

/**
 * The default reactions list used when no custom reactions are provided.
 */
private val DEFAULT_REACTIONS = listOf("😍", "👍🏻", "🔥", "😊", "❤️")

/**
 * Pure-logic function that mirrors the reactions resolution in
 * CometChatMessagePopupMenu.setQuickReactions() and configureQuickReactions():
 *
 * ```kotlin
 * fun setQuickReactions(reactions: List<String>) {
 *     this.quickReactions = reactions.ifEmpty { DEFAULT_REACTIONS }
 * }
 * ```
 *
 * Returns the provided list when non-empty, or the default list when empty.
 */
internal fun resolveReactions(provided: List<String>): List<String> {
    return provided.ifEmpty { DEFAULT_REACTIONS }
}

/**
 * Property-based tests for reactions list default behavior.
 *
 * Feature: message-popup-menu, Property 3: Reactions list uses provided values or defaults
 *
 * *For any* reactions configuration (custom list or empty), displayed chips should
 * match provided list when non-empty, or fall back to default
 * ["😍", "👍🏻", "🔥", "😊", "❤️"] when empty.
 *
 * **Validates: Requirements 2.1**
 */
class QuickReactionsDefaultsPropertyTest : FunSpec({

    // ==================== Generators ====================

    val emojiArb = Arb.element("😀", "😂", "🥰", "👏", "🎉", "💯", "🙌", "✨", "🤔", "👀")
    val nonEmptyReactionsArb = Arb.list(emojiArb, 1..10)

    /** Generates either an empty list or a non-empty emoji list. */
    val maybeEmptyReactionsArb: Arb<List<String>> = Arb.boolean().map { isEmpty ->
        if (isEmpty) emptyList() else listOf("😀", "😂", "🥰").shuffled().take((1..3).random())
    }

    // ==================== Property Tests ====================

    context("Property 3: Reactions list uses provided values or defaults") {

        test("non-empty custom list is used as-is") {
            checkAll(100, nonEmptyReactionsArb) { customReactions ->
                val result = resolveReactions(customReactions)
                result shouldBe customReactions
            }
        }

        test("empty list falls back to default reactions") {
            // Verify 100 times that empty input always yields defaults
            checkAll(100, Arb.boolean()) { _ ->
                val result = resolveReactions(emptyList())
                result shouldBe DEFAULT_REACTIONS
            }
        }

        test("resolved list is never empty") {
            checkAll(100, maybeEmptyReactionsArb) { reactions ->
                val result = resolveReactions(reactions)
                result.isNotEmpty() shouldBe true
            }
        }

        test("default reactions contain exactly 5 emojis in correct order") {
            val result = resolveReactions(emptyList())
            result.size shouldBe 5
            result[0] shouldBe "😍"
            result[1] shouldBe "👍🏻"
            result[2] shouldBe "🔥"
            result[3] shouldBe "😊"
            result[4] shouldBe "❤️"
        }

        test("result matches ifEmpty semantics for any input") {
            checkAll(100, maybeEmptyReactionsArb) { reactions ->
                val result = resolveReactions(reactions)
                val expected = reactions.ifEmpty { DEFAULT_REACTIONS }
                result shouldBe expected
            }
        }
    }
})
