package com.cometchat.uikit.compose.presentation.messagelist

import com.cometchat.chat.models.AIAssistantMessage
import com.cometchat.uikit.compose.presentation.messagelist.utils.isSwipeToReplyEligible
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.domain.model.StreamMessage
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.element
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll

/**
 * Property-based tests for swipe-to-reply ineligibility of agentic and stream messages.
 *
 * **Feature: agent-user-message-list-compose, Property 4: Swipe Ineligible for Agentic and Stream Categories**
 *
 * The `isSwipeToReplyEligible` function in `SwipeToReplyModifier.kt` returns `false`
 * for any message with category `"agentic"` or `"stream_message"`
 * (`UIKitConstants.MessageCategory.STREAM`). This ensures that AI assistant responses
 * (both completed and streaming) cannot be swiped to reply.
 *
 * We test this using real `AIAssistantMessage` and `StreamMessage` instances which
 * automatically set the correct categories in their constructors. Messages are given
 * valid `sentAt` and `id` values (non-zero) and `deletedAt = 0` so they are not
 * filtered out by the "not yet sent" or "deleted" checks — the category check alone
 * must cause ineligibility.
 *
 * **Validates: Requirements 10.1, 10.2**
 */
class SwipeIneligiblePropertyTest : StringSpec({

    // ========================================================================
    // Arbitrary generators
    // ========================================================================

    /** Generates receiver UIDs. */
    val receiverUidArb: Arb<String> = Arb.string(1..50)

    /** Generates receiver types ("user" or "group"). */
    val receiverTypeArb: Arb<String> = Arb.element("user", "group")

    /** Generates message text content. */
    val textArb: Arb<String> = Arb.string(0..200)

    /** Generates valid (non-zero) message IDs. */
    val validIdArb: Arb<Long> = Arb.long(1L..Long.MAX_VALUE)

    /** Generates valid (non-zero) sentAt timestamps. */
    val validSentAtArb: Arb<Long> = Arb.long(1L..Long.MAX_VALUE)

    /**
     * Generates AIAssistantMessage instances with valid id and sentAt values.
     * AIAssistantMessage automatically sets category to "agentic".
     */
    val aiAssistantMessageArb: Arb<AIAssistantMessage> = Arb.bind(
        receiverUidArb,
        receiverTypeArb,
        textArb,
        validIdArb,
        validSentAtArb
    ) { uid, type, text, id, sentAt ->
        AIAssistantMessage(uid, type, text).apply {
            this.id = id
            this.sentAt = sentAt
            this.deletedAt = 0
        }
    }

    /**
     * Generates StreamMessage instances with valid id and sentAt values.
     * StreamMessage automatically sets category to "stream_message"
     * (UIKitConstants.MessageCategory.STREAM).
     */
    val streamMessageArb: Arb<StreamMessage> = Arb.bind(
        receiverUidArb,
        receiverTypeArb,
        textArb,
        validIdArb,
        validSentAtArb
    ) { uid, type, text, id, sentAt ->
        StreamMessage(uid, type, text).apply {
            this.id = id
            this.sentAt = sentAt
            this.deletedAt = 0
        }
    }

    // ========================================================================
    // Property 4: Swipe Ineligible for Agentic and Stream Categories
    // ========================================================================

    /**
     * **Feature: agent-user-message-list-compose, Property 4: Swipe Ineligible for Agentic and Stream Categories**
     *
     * For any AIAssistantMessage (category "agentic") with valid id, sentAt, and
     * deletedAt = 0, `isSwipeToReplyEligible` SHALL return `false`.
     *
     * **Validates: Requirements 10.1**
     */
    "Property 4: isSwipeToReplyEligible returns false for any AIAssistantMessage (agentic category)" {
        checkAll(100, aiAssistantMessageArb) { message ->
            // Confirm the message has the expected category
            message.category shouldBe "agentic"

            // The function should return false due to the agentic category
            isSwipeToReplyEligible(message) shouldBe false
        }
    }

    /**
     * **Feature: agent-user-message-list-compose, Property 4: Swipe Ineligible for Agentic and Stream Categories**
     *
     * For any StreamMessage (category "stream_message") with valid id, sentAt, and
     * deletedAt = 0, `isSwipeToReplyEligible` SHALL return `false`.
     *
     * **Validates: Requirements 10.2**
     */
    "Property 4: isSwipeToReplyEligible returns false for any StreamMessage (stream_message category)" {
        checkAll(100, streamMessageArb) { message ->
            // Confirm the message has the expected category
            message.category shouldBe UIKitConstants.MessageCategory.STREAM

            // The function should return false due to the stream_message category
            isSwipeToReplyEligible(message) shouldBe false
        }
    }

    /**
     * **Feature: agent-user-message-list-compose, Property 4: Swipe Ineligible for Agentic and Stream Categories**
     *
     * For any message with category from {"agentic", "stream_message"} and valid
     * id/sentAt/deletedAt values, `isSwipeToReplyEligible` SHALL return `false`.
     * This combined test uses both AIAssistantMessage and StreamMessage to verify
     * the property holds across both agentic categories.
     *
     * **Validates: Requirements 10.1, 10.2**
     */
    "Property 4: isSwipeToReplyEligible returns false for both agentic and stream categories" {
        // Test with AIAssistantMessage instances
        checkAll(100, aiAssistantMessageArb) { message ->
            isSwipeToReplyEligible(message) shouldBe false
        }

        // Test with StreamMessage instances
        checkAll(100, streamMessageArb) { message ->
            isSwipeToReplyEligible(message) shouldBe false
        }
    }
})
