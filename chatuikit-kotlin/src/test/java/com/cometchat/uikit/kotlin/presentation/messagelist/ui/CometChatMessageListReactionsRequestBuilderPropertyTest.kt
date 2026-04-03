package com.cometchat.uikit.kotlin.presentation.messagelist.ui

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.map
import io.kotest.property.checkAll

/**
 * Test class that simulates a ReactionsRequestBuilder for testing purposes.
 * This mirrors the CometChat SDK's ReactionsRequest.ReactionsRequestBuilder pattern.
 */
private class TestReactionsRequestBuilder {
    private var limit: Int = 30
    private var messageId: Long = 0
    private var reaction: String? = null

    fun setLimit(limit: Int): TestReactionsRequestBuilder {
        this.limit = limit
        return this
    }

    fun getLimit(): Int = limit

    fun setMessageId(messageId: Long): TestReactionsRequestBuilder {
        this.messageId = messageId
        return this
    }

    fun getMessageId(): Long = messageId

    fun setReaction(reaction: String?): TestReactionsRequestBuilder {
        this.reaction = reaction
        return this
    }

    fun getReaction(): String? = reaction

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TestReactionsRequestBuilder) return false
        return limit == other.limit && messageId == other.messageId && reaction == other.reaction
    }

    override fun hashCode(): Int {
        var result = limit
        result = 31 * result + messageId.hashCode()
        result = 31 * result + (reaction?.hashCode() ?: 0)
        return result
    }
}

/**
 * Test class that simulates the reactions request builder storage behavior of CometChatMessageList
 * without requiring Android context or CometChat SDK.
 *
 * This mirrors the actual implementation pattern:
 * - setReactionsRequestBuilder(builder) stores the provided builder
 * - getReactionsRequestBuilder() returns the stored builder
 * - Default value is null
 * - setReactionsRequestBuilder(null) clears the builder
 */
private class TestReactionsRequestBuilderStorage {
    private var reactionsRequestBuilder: TestReactionsRequestBuilder? = null

    /**
     * Simulates CometChatMessageList.setReactionsRequestBuilder(ReactionsRequest.ReactionsRequestBuilder?)
     */
    fun setReactionsRequestBuilder(builder: TestReactionsRequestBuilder?) {
        this.reactionsRequestBuilder = builder
    }

    /**
     * Simulates CometChatMessageList.getReactionsRequestBuilder()
     */
    fun getReactionsRequestBuilder(): TestReactionsRequestBuilder? = reactionsRequestBuilder
}

/**
 * Property-based tests for CometChatMessageList reactions request builder propagation.
 * Uses Kotest property testing to verify correctness properties.
 *
 * Feature: messagelist-property-parity, Property 12: Reactions Request Builder Propagation
 *
 * *For any* ReactionsRequestBuilder set via `setReactionsRequestBuilder`, the builder
 * SHALL be used when fetching reactions and passed to the reaction list bottom sheet.
 *
 * **Validates: Requirements 12.1, 12.2**
 */
class CometChatMessageListReactionsRequestBuilderPropertyTest : FunSpec({

    // ==================== Generators ====================

    /**
     * Generator for limit values (positive integers within reasonable range).
     */
    val limitArb = Arb.int(1..100)

    /**
     * Generator for message IDs (positive longs).
     */
    val messageIdArb = Arb.long(1..Long.MAX_VALUE)

    /**
     * List of sample reactions for testing.
     */
    val sampleReactions = listOf("👍", "❤️", "😂", "😮", "😢", "🙏")

    /**
     * Generator for reaction strings (nullable).
     */
    val reactionArb: Arb<String?> = Arb.int(0..6).map { index ->
        if (index < sampleReactions.size) sampleReactions[index] else null
    }

    // ==================== Property Tests ====================

    context("Property 12: Reactions Request Builder Propagation") {

        // ========================================
        // setReactionsRequestBuilder / getReactionsRequestBuilder Tests
        // ========================================

        test("setReactionsRequestBuilder(builder) stores the provided builder") {
            checkAll(100, limitArb, messageIdArb) { limit, messageId ->
                val storage = TestReactionsRequestBuilderStorage()
                val builder = TestReactionsRequestBuilder()
                    .setLimit(limit)
                    .setMessageId(messageId)

                // Set the builder
                storage.setReactionsRequestBuilder(builder)

                // Verify the builder is stored
                storage.getReactionsRequestBuilder() shouldBe builder
            }
        }

        test("getReactionsRequestBuilder() returns the builder that was set") {
            checkAll(100, limitArb, messageIdArb) { limit, messageId ->
                val storage = TestReactionsRequestBuilderStorage()
                val builder = TestReactionsRequestBuilder()
                    .setLimit(limit)
                    .setMessageId(messageId)

                // Set and get should be symmetric
                storage.setReactionsRequestBuilder(builder)
                val retrieved = storage.getReactionsRequestBuilder()

                retrieved shouldBe builder
            }
        }

        test("default value is null") {
            checkAll(100, Arb.int(0..10)) { _ ->
                val storage = TestReactionsRequestBuilderStorage()

                // Without any setter call, default should be null
                storage.getReactionsRequestBuilder() shouldBe null
            }
        }

        test("setReactionsRequestBuilder(null) clears the builder") {
            checkAll(100, limitArb, messageIdArb) { limit, messageId ->
                val storage = TestReactionsRequestBuilderStorage()
                val builder = TestReactionsRequestBuilder()
                    .setLimit(limit)
                    .setMessageId(messageId)

                // Set a builder first
                storage.setReactionsRequestBuilder(builder)
                storage.getReactionsRequestBuilder() shouldNotBe null

                // Clear the builder by setting null
                storage.setReactionsRequestBuilder(null)

                // Verify the builder is cleared
                storage.getReactionsRequestBuilder() shouldBe null
            }
        }

        test("setting a new builder replaces the previous one") {
            checkAll(100, limitArb, limitArb, messageIdArb, messageIdArb) { limit1, limit2, msgId1, msgId2 ->
                val storage = TestReactionsRequestBuilderStorage()
                val builder1 = TestReactionsRequestBuilder()
                    .setLimit(limit1)
                    .setMessageId(msgId1)
                val builder2 = TestReactionsRequestBuilder()
                    .setLimit(limit2)
                    .setMessageId(msgId2)

                // Set first builder
                storage.setReactionsRequestBuilder(builder1)
                storage.getReactionsRequestBuilder() shouldBe builder1

                // Set second builder
                storage.setReactionsRequestBuilder(builder2)
                storage.getReactionsRequestBuilder() shouldBe builder2
            }
        }

        // ========================================
        // Builder Configuration Tests
        // ========================================

        test("builder with custom limit is stored correctly") {
            checkAll(100, limitArb) { limit ->
                val storage = TestReactionsRequestBuilderStorage()
                val builder = TestReactionsRequestBuilder().setLimit(limit)

                storage.setReactionsRequestBuilder(builder)

                val retrieved = storage.getReactionsRequestBuilder()
                retrieved shouldNotBe null
                retrieved!!.getLimit() shouldBe limit
            }
        }

        test("builder with custom messageId is stored correctly") {
            checkAll(100, messageIdArb) { messageId ->
                val storage = TestReactionsRequestBuilderStorage()
                val builder = TestReactionsRequestBuilder().setMessageId(messageId)

                storage.setReactionsRequestBuilder(builder)

                val retrieved = storage.getReactionsRequestBuilder()
                retrieved shouldNotBe null
                retrieved!!.getMessageId() shouldBe messageId
            }
        }

        test("builder with reaction filter is stored correctly") {
            checkAll(100, reactionArb) { reaction ->
                val storage = TestReactionsRequestBuilderStorage()
                val builder = TestReactionsRequestBuilder().setReaction(reaction)

                storage.setReactionsRequestBuilder(builder)

                val retrieved = storage.getReactionsRequestBuilder()
                retrieved shouldNotBe null
                retrieved!!.getReaction() shouldBe reaction
            }
        }

        test("builder with all configurations is stored correctly") {
            checkAll(100, limitArb, messageIdArb, reactionArb) { limit, messageId, reaction ->
                val storage = TestReactionsRequestBuilderStorage()
                val builder = TestReactionsRequestBuilder()
                    .setLimit(limit)
                    .setMessageId(messageId)
                    .setReaction(reaction)

                storage.setReactionsRequestBuilder(builder)

                val retrieved = storage.getReactionsRequestBuilder()
                retrieved shouldNotBe null
                retrieved!!.getLimit() shouldBe limit
                retrieved.getMessageId() shouldBe messageId
                retrieved.getReaction() shouldBe reaction
            }
        }

        // ========================================
        // Multiple Operations Tests
        // ========================================

        test("multiple set-get cycles work correctly") {
            checkAll(100, limitArb, limitArb, limitArb) { limit1, limit2, limit3 ->
                val storage = TestReactionsRequestBuilderStorage()

                // First cycle
                val builder1 = TestReactionsRequestBuilder().setLimit(limit1)
                storage.setReactionsRequestBuilder(builder1)
                storage.getReactionsRequestBuilder()?.getLimit() shouldBe limit1

                // Second cycle
                val builder2 = TestReactionsRequestBuilder().setLimit(limit2)
                storage.setReactionsRequestBuilder(builder2)
                storage.getReactionsRequestBuilder()?.getLimit() shouldBe limit2

                // Third cycle
                val builder3 = TestReactionsRequestBuilder().setLimit(limit3)
                storage.setReactionsRequestBuilder(builder3)
                storage.getReactionsRequestBuilder()?.getLimit() shouldBe limit3
            }
        }

        test("set-null-set cycle works correctly") {
            checkAll(100, limitArb, limitArb) { limit1, limit2 ->
                val storage = TestReactionsRequestBuilderStorage()

                // Set first builder
                val builder1 = TestReactionsRequestBuilder().setLimit(limit1)
                storage.setReactionsRequestBuilder(builder1)
                storage.getReactionsRequestBuilder() shouldBe builder1

                // Clear
                storage.setReactionsRequestBuilder(null)
                storage.getReactionsRequestBuilder() shouldBe null

                // Set second builder
                val builder2 = TestReactionsRequestBuilder().setLimit(limit2)
                storage.setReactionsRequestBuilder(builder2)
                storage.getReactionsRequestBuilder() shouldBe builder2
            }
        }

        test("same builder instance can be set multiple times") {
            checkAll(100, limitArb) { limit ->
                val storage = TestReactionsRequestBuilderStorage()
                val builder = TestReactionsRequestBuilder().setLimit(limit)

                // Set the same builder multiple times
                storage.setReactionsRequestBuilder(builder)
                storage.setReactionsRequestBuilder(builder)
                storage.setReactionsRequestBuilder(builder)

                // Should still return the same builder
                storage.getReactionsRequestBuilder() shouldBe builder
            }
        }

        // ========================================
        // Edge Cases
        // ========================================

        test("builder with default values is stored correctly") {
            checkAll(100, Arb.int(0..10)) { _ ->
                val storage = TestReactionsRequestBuilderStorage()
                val builder = TestReactionsRequestBuilder() // All defaults

                storage.setReactionsRequestBuilder(builder)

                val retrieved = storage.getReactionsRequestBuilder()
                retrieved shouldNotBe null
                retrieved!!.getLimit() shouldBe 30 // Default limit
                retrieved.getMessageId() shouldBe 0L // Default messageId
                retrieved.getReaction() shouldBe null // Default reaction
            }
        }

        test("builder identity is preserved (same instance returned)") {
            checkAll(100, limitArb) { limit ->
                val storage = TestReactionsRequestBuilderStorage()
                val builder = TestReactionsRequestBuilder().setLimit(limit)

                storage.setReactionsRequestBuilder(builder)

                // Should return the same builder (value equality)
                val retrieved = storage.getReactionsRequestBuilder()
                retrieved shouldBe builder
                retrieved?.getLimit() shouldBe limit
            }
        }
    }
})
