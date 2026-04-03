package com.cometchat.uikit.compose.presentation.messagelist

import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.state.MessageAlignment
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.enum
import io.kotest.property.checkAll

/**
 * Pure function that resolves the bubble alignment based on message alignment
 * and message list alignment setting.
 *
 * This mirrors the logic in MessageListItem.kt:
 * - LEFT_ALIGNED mode: forces all non-CENTER messages to LEFT
 * - STANDARD mode: maps MessageAlignment directly to MessageBubbleAlignment
 *
 * @param alignment The per-message alignment (LEFT for incoming, RIGHT for outgoing, CENTER for action/call)
 * @param messageListAlignment The list-level alignment setting (STANDARD or LEFT_ALIGNED)
 * @return The resolved bubble alignment
 */
fun resolveBubbleAlignment(
    alignment: MessageAlignment,
    messageListAlignment: UIKitConstants.MessageListAlignment
): UIKitConstants.MessageBubbleAlignment {
    return if (messageListAlignment == UIKitConstants.MessageListAlignment.LEFT_ALIGNED) {
        when (alignment) {
            MessageAlignment.CENTER -> UIKitConstants.MessageBubbleAlignment.CENTER
            else -> UIKitConstants.MessageBubbleAlignment.LEFT
        }
    } else {
        when (alignment) {
            MessageAlignment.LEFT -> UIKitConstants.MessageBubbleAlignment.LEFT
            MessageAlignment.RIGHT -> UIKitConstants.MessageBubbleAlignment.RIGHT
            MessageAlignment.CENTER -> UIKitConstants.MessageBubbleAlignment.CENTER
        }
    }
}

/**
 * Property-based tests for message list alignment logic.
 *
 * Feature: jetpack-message-bubble-parity, Property 11: Message list alignment determines bubble alignment
 *
 * **Validates: Requirements 11.2, 11.3**
 *
 * Property 11: *For any* message, when `messageListAlignment` is `LEFT_ALIGNED`,
 * the bubble alignment SHALL be `LEFT` regardless of sender identity
 * (except for `CENTER`-aligned action/call messages). When `messageListAlignment`
 * is `STANDARD`, incoming messages SHALL align `LEFT` and outgoing messages SHALL align `RIGHT`.
 */
class MessageListAlignmentPropertyTest : StringSpec({

    // ============================================================================
    // Arbitrary generators
    // ============================================================================

    val messageAlignmentArb = Arb.enum<MessageAlignment>()
    val messageListAlignmentArb = Arb.enum<UIKitConstants.MessageListAlignment>()

    // ============================================================================
    // Property 11: Message list alignment determines bubble alignment
    // ============================================================================

    "Property 11: LEFT_ALIGNED + LEFT alignment -> LEFT bubble alignment" {
        checkAll(100, messageAlignmentArb) { alignment ->
            if (alignment == MessageAlignment.LEFT) {
                val result = resolveBubbleAlignment(
                    alignment = alignment,
                    messageListAlignment = UIKitConstants.MessageListAlignment.LEFT_ALIGNED
                )
                result shouldBe UIKitConstants.MessageBubbleAlignment.LEFT
            }
        }
    }

    "Property 11: LEFT_ALIGNED + RIGHT alignment -> LEFT bubble alignment (forced)" {
        checkAll(100, messageAlignmentArb) { alignment ->
            if (alignment == MessageAlignment.RIGHT) {
                val result = resolveBubbleAlignment(
                    alignment = alignment,
                    messageListAlignment = UIKitConstants.MessageListAlignment.LEFT_ALIGNED
                )
                result shouldBe UIKitConstants.MessageBubbleAlignment.LEFT
            }
        }
    }

    "Property 11: LEFT_ALIGNED + CENTER alignment -> CENTER bubble alignment (preserved)" {
        val result = resolveBubbleAlignment(
            alignment = MessageAlignment.CENTER,
            messageListAlignment = UIKitConstants.MessageListAlignment.LEFT_ALIGNED
        )
        result shouldBe UIKitConstants.MessageBubbleAlignment.CENTER
    }

    "Property 11: STANDARD + LEFT alignment -> LEFT bubble alignment" {
        val result = resolveBubbleAlignment(
            alignment = MessageAlignment.LEFT,
            messageListAlignment = UIKitConstants.MessageListAlignment.STANDARD
        )
        result shouldBe UIKitConstants.MessageBubbleAlignment.LEFT
    }

    "Property 11: STANDARD + RIGHT alignment -> RIGHT bubble alignment" {
        val result = resolveBubbleAlignment(
            alignment = MessageAlignment.RIGHT,
            messageListAlignment = UIKitConstants.MessageListAlignment.STANDARD
        )
        result shouldBe UIKitConstants.MessageBubbleAlignment.RIGHT
    }

    "Property 11: STANDARD + CENTER alignment -> CENTER bubble alignment" {
        val result = resolveBubbleAlignment(
            alignment = MessageAlignment.CENTER,
            messageListAlignment = UIKitConstants.MessageListAlignment.STANDARD
        )
        result shouldBe UIKitConstants.MessageBubbleAlignment.CENTER
    }

    "Property 11: LEFT_ALIGNED forces all non-CENTER messages to LEFT" {
        checkAll(100, messageAlignmentArb) { alignment ->
            val result = resolveBubbleAlignment(
                alignment = alignment,
                messageListAlignment = UIKitConstants.MessageListAlignment.LEFT_ALIGNED
            )

            val expected = when (alignment) {
                MessageAlignment.CENTER -> UIKitConstants.MessageBubbleAlignment.CENTER
                else -> UIKitConstants.MessageBubbleAlignment.LEFT
            }
            result shouldBe expected
        }
    }

    "Property 11: STANDARD uses sender-based alignment (direct mapping)" {
        checkAll(100, messageAlignmentArb) { alignment ->
            val result = resolveBubbleAlignment(
                alignment = alignment,
                messageListAlignment = UIKitConstants.MessageListAlignment.STANDARD
            )

            val expected = when (alignment) {
                MessageAlignment.LEFT -> UIKitConstants.MessageBubbleAlignment.LEFT
                MessageAlignment.RIGHT -> UIKitConstants.MessageBubbleAlignment.RIGHT
                MessageAlignment.CENTER -> UIKitConstants.MessageBubbleAlignment.CENTER
            }
            result shouldBe expected
        }
    }

    "Property 11: For any alignment and list alignment, resolution is deterministic" {
        checkAll(100, messageAlignmentArb, messageListAlignmentArb) { alignment, listAlignment ->
            val result1 = resolveBubbleAlignment(alignment, listAlignment)
            val result2 = resolveBubbleAlignment(alignment, listAlignment)
            result1 shouldBe result2
        }
    }

    "Property 11: Complete decision table verification" {
        data class AlignmentTestCase(
            val messageAlignment: MessageAlignment,
            val listAlignment: UIKitConstants.MessageListAlignment,
            val expected: UIKitConstants.MessageBubbleAlignment
        )

        val cases = listOf(
            // LEFT_ALIGNED mode
            AlignmentTestCase(MessageAlignment.LEFT, UIKitConstants.MessageListAlignment.LEFT_ALIGNED, UIKitConstants.MessageBubbleAlignment.LEFT),
            AlignmentTestCase(MessageAlignment.RIGHT, UIKitConstants.MessageListAlignment.LEFT_ALIGNED, UIKitConstants.MessageBubbleAlignment.LEFT),
            AlignmentTestCase(MessageAlignment.CENTER, UIKitConstants.MessageListAlignment.LEFT_ALIGNED, UIKitConstants.MessageBubbleAlignment.CENTER),
            // STANDARD mode
            AlignmentTestCase(MessageAlignment.LEFT, UIKitConstants.MessageListAlignment.STANDARD, UIKitConstants.MessageBubbleAlignment.LEFT),
            AlignmentTestCase(MessageAlignment.RIGHT, UIKitConstants.MessageListAlignment.STANDARD, UIKitConstants.MessageBubbleAlignment.RIGHT),
            AlignmentTestCase(MessageAlignment.CENTER, UIKitConstants.MessageListAlignment.STANDARD, UIKitConstants.MessageBubbleAlignment.CENTER),
        )

        cases.forEach { (msgAlign, listAlign, expected) ->
            val result = resolveBubbleAlignment(msgAlign, listAlign)
            result shouldBe expected
        }
    }

    "Property 11: For any combination, resolved alignment is always valid" {
        checkAll(100, messageAlignmentArb, messageListAlignmentArb) { alignment, listAlignment ->
            val result = resolveBubbleAlignment(alignment, listAlignment)

            val expected = if (listAlignment == UIKitConstants.MessageListAlignment.LEFT_ALIGNED) {
                when (alignment) {
                    MessageAlignment.CENTER -> UIKitConstants.MessageBubbleAlignment.CENTER
                    else -> UIKitConstants.MessageBubbleAlignment.LEFT
                }
            } else {
                when (alignment) {
                    MessageAlignment.LEFT -> UIKitConstants.MessageBubbleAlignment.LEFT
                    MessageAlignment.RIGHT -> UIKitConstants.MessageBubbleAlignment.RIGHT
                    MessageAlignment.CENTER -> UIKitConstants.MessageBubbleAlignment.CENTER
                }
            }

            result shouldBe expected
        }
    }
})
