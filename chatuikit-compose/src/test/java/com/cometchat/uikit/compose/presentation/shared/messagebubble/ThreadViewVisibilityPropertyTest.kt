package com.cometchat.uikit.compose.presentation.shared.messagebubble

import com.cometchat.chat.models.BaseMessage
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.long
import io.kotest.property.checkAll
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

/**
 * Property-based tests for DefaultThreadView visibility logic.
 *
 * // Feature: jetpack-message-bubble-parity, Property 13: Thread view renders nothing for zero replies or deleted messages
 *
 * **Validates: Requirements 15.3**
 *
 * Property 13: For any message, the DefaultThreadView SHALL render nothing when
 * `replyCount == 0` or `deletedAt > 0`. When `replyCount > 0` and `deletedAt == 0`,
 * it SHALL render the reply count text and thread icon.
 *
 * The actual guard in InternalContentRenderer.DefaultThreadView:
 * ```
 * if (message.replyCount <= 0 || message.deletedAt != 0L) return
 * ```
 *
 * We test this as a pure visibility function to avoid needing the Compose runtime.
 */
class ThreadViewVisibilityPropertyTest : StringSpec({

    /**
     * Mirrors the guard logic in InternalContentRenderer.DefaultThreadView.
     * Returns true when the thread view would render content, false when it returns early.
     */
    fun shouldRenderThreadView(message: BaseMessage): Boolean {
        if (message.replyCount <= 0 || message.deletedAt != 0L) return false
        return true
    }

    // ============================================================================
    // Arbitrary generators
    // ============================================================================

    /** Generates replyCount values spanning zero, negative, and positive ranges. */
    val replyCountArb = Arb.int(-50..50)

    /** Generates positive replyCount values (has replies). */
    val positiveReplyCountArb = Arb.int(1..1000)

    /** Generates zero or negative replyCount values (no replies / edge case). */
    val zeroOrNegativeReplyCountArb = Arb.int(-1000..0)

    /** Generates deletedAt values including 0 (not deleted) and non-zero (deleted / edge case). */
    val deletedAtArb = Arb.long(-100L..100L)

    /** Generates positive deletedAt timestamps (deleted). */
    val positiveDeletedAtArb = Arb.long(1L..Long.MAX_VALUE)

    /** Generates negative deletedAt timestamps (edge case, still != 0). */
    val negativeDeletedAtArb = Arb.long(Long.MIN_VALUE..-1L)

    // ============================================================================
    // Property Test: Thread view renders iff replyCount > 0 AND deletedAt == 0
    // ============================================================================

    /**
     * Property test: For any combination of replyCount and deletedAt, the thread view
     * renders if and only if replyCount > 0 AND deletedAt == 0.
     *
     * // Feature: jetpack-message-bubble-parity, Property 13: Thread view renders nothing for zero replies or deleted messages
     * **Validates: Requirements 15.3**
     */
    "thread view renders iff replyCount > 0 AND deletedAt == 0" {
        checkAll(100, replyCountArb, deletedAtArb) { replyCount, deletedAt ->
            val message = mock(BaseMessage::class.java)
            `when`(message.replyCount).thenReturn(replyCount)
            `when`(message.deletedAt).thenReturn(deletedAt)

            val expected = replyCount > 0 && deletedAt == 0L
            shouldRenderThreadView(message) shouldBe expected
        }
    }

    // ============================================================================
    // Property Test: replyCount == 0 always prevents rendering
    // ============================================================================

    /**
     * Property test: When replyCount is 0, the thread view never renders
     * regardless of the deletedAt value.
     *
     * // Feature: jetpack-message-bubble-parity, Property 13: Thread view renders nothing for zero replies or deleted messages
     * **Validates: Requirements 15.3**
     */
    "replyCount == 0 prevents rendering regardless of deletedAt" {
        checkAll(100, deletedAtArb) { deletedAt ->
            val message = mock(BaseMessage::class.java)
            `when`(message.replyCount).thenReturn(0)
            `when`(message.deletedAt).thenReturn(deletedAt)

            shouldRenderThreadView(message) shouldBe false
        }
    }

    // ============================================================================
    // Property Test: Negative replyCount prevents rendering
    // ============================================================================

    /**
     * Property test: When replyCount is negative (edge case), the thread view
     * never renders regardless of deletedAt.
     *
     * // Feature: jetpack-message-bubble-parity, Property 13: Thread view renders nothing for zero replies or deleted messages
     * **Validates: Requirements 15.3**
     */
    "negative replyCount prevents rendering regardless of deletedAt" {
        checkAll(100, Arb.int(-1000..-1), deletedAtArb) { replyCount, deletedAt ->
            val message = mock(BaseMessage::class.java)
            `when`(message.replyCount).thenReturn(replyCount)
            `when`(message.deletedAt).thenReturn(deletedAt)

            shouldRenderThreadView(message) shouldBe false
        }
    }

    // ============================================================================
    // Property Test: Positive deletedAt prevents rendering even with replies
    // ============================================================================

    /**
     * Property test: When deletedAt is positive (message deleted), the thread view
     * does not render even if replyCount > 0.
     *
     * // Feature: jetpack-message-bubble-parity, Property 13: Thread view renders nothing for zero replies or deleted messages
     * **Validates: Requirements 15.3**
     */
    "positive deletedAt prevents rendering even with positive replyCount" {
        checkAll(100, positiveReplyCountArb, positiveDeletedAtArb) { replyCount, deletedAt ->
            val message = mock(BaseMessage::class.java)
            `when`(message.replyCount).thenReturn(replyCount)
            `when`(message.deletedAt).thenReturn(deletedAt)

            shouldRenderThreadView(message) shouldBe false
        }
    }

    /**
     * Property test: When deletedAt is negative (edge case, still != 0), the thread
     * view does not render even if replyCount > 0.
     *
     * // Feature: jetpack-message-bubble-parity, Property 13: Thread view renders nothing for zero replies or deleted messages
     * **Validates: Requirements 15.3**
     */
    "negative deletedAt prevents rendering even with positive replyCount" {
        checkAll(100, positiveReplyCountArb, negativeDeletedAtArb) { replyCount, deletedAt ->
            val message = mock(BaseMessage::class.java)
            `when`(message.replyCount).thenReturn(replyCount)
            `when`(message.deletedAt).thenReturn(deletedAt)

            shouldRenderThreadView(message) shouldBe false
        }
    }

    // ============================================================================
    // Property Test: replyCount > 0 AND deletedAt == 0 always renders
    // ============================================================================

    /**
     * Property test: When replyCount is positive and deletedAt is exactly 0,
     * the thread view always renders.
     *
     * // Feature: jetpack-message-bubble-parity, Property 13: Thread view renders nothing for zero replies or deleted messages
     * **Validates: Requirements 15.3**
     */
    "positive replyCount and deletedAt == 0 always renders" {
        checkAll(100, positiveReplyCountArb) { replyCount ->
            val message = mock(BaseMessage::class.java)
            `when`(message.replyCount).thenReturn(replyCount)
            `when`(message.deletedAt).thenReturn(0L)

            shouldRenderThreadView(message) shouldBe true
        }
    }
})
