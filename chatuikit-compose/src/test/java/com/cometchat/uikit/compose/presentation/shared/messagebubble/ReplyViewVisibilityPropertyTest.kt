package com.cometchat.uikit.compose.presentation.shared.messagebubble

import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.TextMessage
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.long
import io.kotest.property.checkAll
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

/**
 * Property-based tests for DefaultReplyView visibility logic.
 *
 * // Feature: jetpack-message-bubble-parity, Property 3: Reply view visibility based on quotedMessage and deletedAt
 *
 * **Validates: Requirements 2.3**
 *
 * Property 3: For any BaseMessage, the DefaultReplyView SHALL render the
 * CometChatMessagePreview composable if and only if `message.quotedMessage != null`
 * AND `message.deletedAt == 0`. Otherwise, it SHALL render nothing.
 *
 * Since DefaultReplyView is a @Composable function, we test the pure visibility
 * logic (the two early-return guard conditions) that determines whether the
 * CometChatMessagePreview is rendered.
 */
class ReplyViewVisibilityPropertyTest : StringSpec({

    /**
     * Determines whether DefaultReplyView would render the CometChatMessagePreview.
     * This mirrors the guard logic in InternalContentRenderer.DefaultReplyView:
     *   val quotedMessage = message.quotedMessage ?: return
     *   if (message.deletedAt != 0L) return
     */
    fun shouldRenderReplyView(message: BaseMessage): Boolean {
        val quotedMessage = message.quotedMessage ?: return false
        if (message.deletedAt != 0L) return false
        return true
    }

    // ============================================================================
    // Arbitrary generators
    // ============================================================================

    /**
     * Generates random deletedAt values including 0 (not deleted),
     * positive values (deleted), and negative values (edge case).
     */
    val deletedAtArb = Arb.long(-100L..100L)

    /**
     * Generates random positive deletedAt timestamps (always deleted).
     */
    val positiveDeletedAtArb = Arb.long(1L..Long.MAX_VALUE)

    /**
     * Generates random negative deletedAt timestamps (edge case, still != 0).
     */
    val negativeDeletedAtArb = Arb.long(Long.MIN_VALUE..-1L)

    // ============================================================================
    // Property Test: Reply view renders iff quotedMessage != null AND deletedAt == 0
    // ============================================================================

    /**
     * Property test: For any combination of quotedMessage presence and deletedAt value,
     * the reply view renders if and only if quotedMessage is non-null AND deletedAt == 0.
     *
     * // Feature: jetpack-message-bubble-parity, Property 3: Reply view visibility based on quotedMessage and deletedAt
     * **Validates: Requirements 2.3**
     */
    "reply view renders iff quotedMessage != null AND deletedAt == 0" {
        checkAll(100, Arb.boolean(), deletedAtArb) { hasQuotedMessage, deletedAt ->
            val message = mock(BaseMessage::class.java)
            `when`(message.deletedAt).thenReturn(deletedAt)
            `when`(message.category).thenReturn(CometChatConstants.CATEGORY_MESSAGE)
            `when`(message.type).thenReturn(CometChatConstants.MESSAGE_TYPE_TEXT)

            if (hasQuotedMessage) {
                val quotedMsg = mock(TextMessage::class.java)
                `when`(message.quotedMessage).thenReturn(quotedMsg)
            } else {
                `when`(message.quotedMessage).thenReturn(null)
            }

            val expected = hasQuotedMessage && deletedAt == 0L
            shouldRenderReplyView(message) shouldBe expected
        }
    }

    // ============================================================================
    // Property Test: Null quotedMessage always prevents rendering
    // ============================================================================

    /**
     * Property test: When quotedMessage is null, the reply view never renders
     * regardless of the deletedAt value.
     *
     * // Feature: jetpack-message-bubble-parity, Property 3: Reply view visibility based on quotedMessage and deletedAt
     * **Validates: Requirements 2.3**
     */
    "null quotedMessage prevents rendering regardless of deletedAt" {
        checkAll(100, deletedAtArb) { deletedAt ->
            val message = mock(BaseMessage::class.java)
            `when`(message.deletedAt).thenReturn(deletedAt)
            `when`(message.quotedMessage).thenReturn(null)

            shouldRenderReplyView(message) shouldBe false
        }
    }

    // ============================================================================
    // Property Test: Non-zero deletedAt prevents rendering even with quotedMessage
    // ============================================================================

    /**
     * Property test: When deletedAt is positive (message deleted), the reply view
     * does not render even if quotedMessage is present.
     *
     * // Feature: jetpack-message-bubble-parity, Property 3: Reply view visibility based on quotedMessage and deletedAt
     * **Validates: Requirements 2.3**
     */
    "positive deletedAt prevents rendering even with quotedMessage" {
        checkAll(100, positiveDeletedAtArb) { deletedAt ->
            val message = mock(BaseMessage::class.java)
            `when`(message.deletedAt).thenReturn(deletedAt)
            val quotedMsg = mock(TextMessage::class.java)
            `when`(message.quotedMessage).thenReturn(quotedMsg)

            shouldRenderReplyView(message) shouldBe false
        }
    }

    /**
     * Property test: When deletedAt is negative (edge case, still != 0), the reply
     * view does not render even if quotedMessage is present.
     *
     * // Feature: jetpack-message-bubble-parity, Property 3: Reply view visibility based on quotedMessage and deletedAt
     * **Validates: Requirements 2.3**
     */
    "negative deletedAt prevents rendering even with quotedMessage" {
        checkAll(100, negativeDeletedAtArb) { deletedAt ->
            val message = mock(BaseMessage::class.java)
            `when`(message.deletedAt).thenReturn(deletedAt)
            val quotedMsg = mock(TextMessage::class.java)
            `when`(message.quotedMessage).thenReturn(quotedMsg)

            shouldRenderReplyView(message) shouldBe false
        }
    }

    // ============================================================================
    // Property Test: quotedMessage present AND deletedAt == 0 always renders
    // ============================================================================

    /**
     * Property test: When quotedMessage is non-null and deletedAt is exactly 0,
     * the reply view always renders.
     *
     * // Feature: jetpack-message-bubble-parity, Property 3: Reply view visibility based on quotedMessage and deletedAt
     * **Validates: Requirements 2.3**
     */
    "quotedMessage present and deletedAt == 0 always renders" {
        checkAll(100, Arb.boolean()) { _ ->
            val message = mock(BaseMessage::class.java)
            `when`(message.deletedAt).thenReturn(0L)
            val quotedMsg = mock(TextMessage::class.java)
            `when`(message.quotedMessage).thenReturn(quotedMsg)

            shouldRenderReplyView(message) shouldBe true
        }
    }
})
