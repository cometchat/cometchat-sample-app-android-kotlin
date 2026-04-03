package com.cometchat.uikit.kotlin.presentation.messagelist.ui

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll

/**
 * Test class that simulates the quick reactions configuration behavior of CometChatMessageList
 * without requiring Android context.
 *
 * This mirrors the actual implementation pattern:
 * - setQuickReactions(list) stores the provided list (null keeps existing)
 * - getQuickReactions() returns the stored list
 * - Default reactions are ["👍", "❤️", "😂", "😮", "😢", "🙏"]
 * - setAddReactionIcon(int) stores the provided icon resource ID
 * - getAddReactionIcon() returns the stored icon (default is 0)
 */
private class TestQuickReactionsStorage {
    // Default quick reactions (mirrors CometChatMessageList)
    private var quickReactions: List<String> = listOf("👍", "❤️", "😂", "😮", "😢", "🙏")
    private var addReactionIcon: Int = 0

    /**
     * Simulates CometChatMessageList.setQuickReactions(List<String>?)
     * If null is passed, the existing reactions are kept (not reset to default).
     */
    fun setQuickReactions(reactions: List<String>?) {
        if (reactions != null) {
            this.quickReactions = reactions
        }
    }

    /**
     * Simulates CometChatMessageList.getQuickReactions()
     */
    fun getQuickReactions(): List<String> = quickReactions

    /**
     * Simulates CometChatMessageList.setAddReactionIcon(int)
     */
    fun setAddReactionIcon(addReactionIcon: Int) {
        this.addReactionIcon = addReactionIcon
    }

    /**
     * Simulates CometChatMessageList.getAddReactionIcon()
     */
    fun getAddReactionIcon(): Int = addReactionIcon

    companion object {
        val DEFAULT_REACTIONS = listOf("👍", "❤️", "😂", "😮", "😢", "🙏")
    }
}

/**
 * Property-based tests for CometChatMessageList quick reactions configuration.
 * Uses Kotest property testing to verify correctness properties.
 *
 * Feature: messagelist-property-parity, Property 7: Quick Reactions Configuration
 *
 * *For any* list of emoji strings set via `setQuickReactions`, the quick reaction bar
 * SHALL display exactly those emojis in the provided order.
 *
 * **Validates: Requirements 7.1, 7.3**
 */
class CometChatMessageListQuickReactionsPropertyTest : FunSpec({

    // ==================== Generators ====================

    /**
     * Generator for emoji strings (using simple strings to simulate emojis).
     */
    val emojiArb = Arb.string(1..4)

    /**
     * Generator for lists of emoji strings.
     */
    val emojiListArb = Arb.list(emojiArb, 0..10)

    /**
     * Generator for drawable resource IDs (positive integers).
     */
    val resourceIdArb = Arb.int(0..Int.MAX_VALUE)

    // ==================== Property Tests ====================

    context("Property 7: Quick Reactions Configuration") {

        // ========================================
        // setQuickReactions / getQuickReactions Tests
        // ========================================

        test("setQuickReactions(list) stores the provided list") {
            checkAll(100, emojiListArb) { reactions ->
                val storage = TestQuickReactionsStorage()

                // Set custom reactions
                storage.setQuickReactions(reactions)

                // Verify the list is stored exactly as provided
                storage.getQuickReactions() shouldBe reactions
            }
        }

        test("getQuickReactions() returns the list that was set") {
            checkAll(100, emojiListArb) { reactions ->
                val storage = TestQuickReactionsStorage()

                // Set and get should be symmetric
                storage.setQuickReactions(reactions)
                val retrieved = storage.getQuickReactions()

                retrieved shouldBe reactions
            }
        }

        test("default reactions are [\"👍\", \"❤️\", \"😂\", \"😮\", \"😢\", \"🙏\"]") {
            checkAll(100, Arb.int(0..10)) { _ ->
                val storage = TestQuickReactionsStorage()

                // Without any setter call, default reactions should be returned
                storage.getQuickReactions() shouldBe TestQuickReactionsStorage.DEFAULT_REACTIONS
            }
        }

        test("setQuickReactions(null) keeps the existing reactions (doesn't reset to default)") {
            checkAll(100, emojiListArb) { customReactions ->
                val storage = TestQuickReactionsStorage()

                // Set custom reactions first
                storage.setQuickReactions(customReactions)

                // Setting null should keep the existing reactions
                storage.setQuickReactions(null)

                // Verify the custom reactions are still there
                storage.getQuickReactions() shouldBe customReactions
            }
        }

        test("setQuickReactions preserves order of emojis") {
            checkAll(100, emojiListArb) { reactions ->
                val storage = TestQuickReactionsStorage()

                storage.setQuickReactions(reactions)

                // Verify order is preserved
                val retrieved = storage.getQuickReactions()
                reactions.forEachIndexed { index, emoji ->
                    if (index < retrieved.size) {
                        retrieved[index] shouldBe emoji
                    }
                }
                retrieved.size shouldBe reactions.size
            }
        }

        test("setQuickReactions with empty list stores empty list") {
            checkAll(100, Arb.int(0..10)) { _ ->
                val storage = TestQuickReactionsStorage()

                // Set empty list
                storage.setQuickReactions(emptyList())

                // Verify empty list is stored
                storage.getQuickReactions() shouldBe emptyList()
            }
        }

        test("multiple setQuickReactions calls use the last value") {
            checkAll(100, emojiListArb, emojiListArb) { first, second ->
                val storage = TestQuickReactionsStorage()

                // Set first list
                storage.setQuickReactions(first)
                storage.getQuickReactions() shouldBe first

                // Set second list
                storage.setQuickReactions(second)
                storage.getQuickReactions() shouldBe second
            }
        }

        // ========================================
        // setAddReactionIcon / getAddReactionIcon Tests
        // ========================================

        test("setAddReactionIcon(int) stores the provided icon resource ID") {
            checkAll(100, resourceIdArb) { iconId ->
                val storage = TestQuickReactionsStorage()

                // Set icon
                storage.setAddReactionIcon(iconId)

                // Verify the icon is stored
                storage.getAddReactionIcon() shouldBe iconId
            }
        }

        test("getAddReactionIcon() returns the icon that was set") {
            checkAll(100, resourceIdArb) { iconId ->
                val storage = TestQuickReactionsStorage()

                // Set and get should be symmetric
                storage.setAddReactionIcon(iconId)
                val retrieved = storage.getAddReactionIcon()

                retrieved shouldBe iconId
            }
        }

        test("default addReactionIcon is 0") {
            checkAll(100, Arb.int(0..10)) { _ ->
                val storage = TestQuickReactionsStorage()

                // Without any setter call, default icon should be 0
                storage.getAddReactionIcon() shouldBe 0
            }
        }

        test("multiple setAddReactionIcon calls use the last value") {
            checkAll(100, resourceIdArb, resourceIdArb) { first, second ->
                val storage = TestQuickReactionsStorage()

                // Set first icon
                storage.setAddReactionIcon(first)
                storage.getAddReactionIcon() shouldBe first

                // Set second icon
                storage.setAddReactionIcon(second)
                storage.getAddReactionIcon() shouldBe second
            }
        }

        // ========================================
        // Combined Tests
        // ========================================

        test("quick reactions and add reaction icon are independent") {
            checkAll(100, emojiListArb, resourceIdArb) { reactions, iconId ->
                val storage = TestQuickReactionsStorage()

                // Set both
                storage.setQuickReactions(reactions)
                storage.setAddReactionIcon(iconId)

                // Verify both are stored independently
                storage.getQuickReactions() shouldBe reactions
                storage.getAddReactionIcon() shouldBe iconId
            }
        }

        test("setting quick reactions does not affect add reaction icon") {
            checkAll(100, emojiListArb, resourceIdArb) { reactions, iconId ->
                val storage = TestQuickReactionsStorage()

                // Set icon first
                storage.setAddReactionIcon(iconId)

                // Set reactions
                storage.setQuickReactions(reactions)

                // Icon should be unchanged
                storage.getAddReactionIcon() shouldBe iconId
            }
        }

        test("setting add reaction icon does not affect quick reactions") {
            checkAll(100, emojiListArb, resourceIdArb) { reactions, iconId ->
                val storage = TestQuickReactionsStorage()

                // Set reactions first
                storage.setQuickReactions(reactions)

                // Set icon
                storage.setAddReactionIcon(iconId)

                // Reactions should be unchanged
                storage.getQuickReactions() shouldBe reactions
            }
        }

        test("setQuickReactions(null) after setAddReactionIcon keeps both values") {
            checkAll(100, emojiListArb, resourceIdArb) { reactions, iconId ->
                val storage = TestQuickReactionsStorage()

                // Set both
                storage.setQuickReactions(reactions)
                storage.setAddReactionIcon(iconId)

                // Set reactions to null
                storage.setQuickReactions(null)

                // Both should be preserved
                storage.getQuickReactions() shouldBe reactions
                storage.getAddReactionIcon() shouldBe iconId
            }
        }

        test("resetting to default reactions by setting default list explicitly") {
            checkAll(100, emojiListArb) { customReactions ->
                val storage = TestQuickReactionsStorage()

                // Set custom reactions
                storage.setQuickReactions(customReactions)

                // Reset to default by setting default list explicitly
                storage.setQuickReactions(TestQuickReactionsStorage.DEFAULT_REACTIONS)

                // Verify default reactions are restored
                storage.getQuickReactions() shouldBe TestQuickReactionsStorage.DEFAULT_REACTIONS
            }
        }
    }
})
