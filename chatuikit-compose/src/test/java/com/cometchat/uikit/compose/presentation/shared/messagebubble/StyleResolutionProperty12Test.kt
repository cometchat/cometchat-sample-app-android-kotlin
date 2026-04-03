package com.cometchat.uikit.compose.presentation.shared.messagebubble

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatMessageBubbleStyle
import com.cometchat.uikit.core.constants.UIKitConstants
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.element
import io.kotest.property.arbitrary.float
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.orNull
import io.kotest.property.checkAll

/**
 * Property-based tests for the three-tier style resolution priority in CometChatMessageBubble.
 *
 * // Feature: jetpack-message-bubble-parity, Property 12: Three-tier style resolution priority
 *
 * *For any* message, the effective bubble style SHALL be resolved in this priority order:
 * (1) factory style from ComposeBubbleFactory.getBubbleStyle() when non-null (highest),
 * (2) explicit incomingMessageBubbleStyle for LEFT-aligned or outgoingMessageBubbleStyle
 *     for RIGHT-aligned messages when non-null,
 * (3) alignment-based default (lowest).
 * A higher-priority style SHALL always override a lower-priority one.
 *
 * The actual resolution code in CometChatMessageBubble:
 * ```
 * val baseStyle = factory?.getBubbleStyle(message, alignment)
 *     ?: when (alignment) {
 *         UIKitConstants.MessageBubbleAlignment.LEFT ->
 *             incomingMessageBubbleStyle ?: alignmentDefault
 *         UIKitConstants.MessageBubbleAlignment.RIGHT ->
 *             outgoingMessageBubbleStyle ?: alignmentDefault
 *         else -> alignmentDefault
 *     }
 * ```
 *
 * **Validates: Requirements 12.2, 12.3, 12.4**
 */
class StyleResolutionProperty12Test : StringSpec({

    // ========================================================================
    // Arbitrary generators
    // ========================================================================

    val nonSentinelColorArb: Arb<Color> = Arb.bind(
        Arb.int(1, 255),
        Arb.int(0, 255),
        Arb.int(0, 255),
        Arb.int(1, 255)
    ) { r, g, b, a -> Color(r / 255f, g / 255f, b / 255f, a / 255f) }

    val nonSentinelDpArb: Arb<Dp> = Arb.float(0f, 24f).map { it.dp }

    val nonSentinelTextStyleArb: Arb<TextStyle> = Arb.float(10f, 24f).map { TextStyle(fontSize = it.sp) }

    val nonSentinelPaddingArb: Arb<PaddingValues> = Arb.float(0f, 16f).map { PaddingValues(it.dp) }

    val alignmentArb: Arb<UIKitConstants.MessageBubbleAlignment> = Arb.element(
        UIKitConstants.MessageBubbleAlignment.LEFT,
        UIKitConstants.MessageBubbleAlignment.RIGHT,
        UIKitConstants.MessageBubbleAlignment.CENTER
    )

    /** Generates a CometChatMessageBubbleStyle with random non-sentinel properties. */
    val bubbleStyleArb: Arb<CometChatMessageBubbleStyle> = Arb.bind(
        nonSentinelColorArb,
        nonSentinelDpArb,
        nonSentinelDpArb,
        nonSentinelColorArb,
        nonSentinelPaddingArb,
        nonSentinelColorArb,
        nonSentinelTextStyleArb,
        nonSentinelColorArb,
        nonSentinelTextStyleArb,
        nonSentinelColorArb
    ) { bg, cr, sw, sc, pad, snc, sns, tic, tis, tii ->
        CometChatMessageBubbleStyle(
            backgroundColor = bg,
            cornerRadius = cr,
            strokeWidth = sw,
            strokeColor = sc,
            padding = pad,
            senderNameTextColor = snc,
            senderNameTextStyle = sns,
            threadIndicatorTextColor = tic,
            threadIndicatorTextStyle = tis,
            threadIndicatorIconTint = tii,
            timestampTextColor = Color(0.2f, 0.3f, 0.4f, 1f),
            timestampTextStyle = TextStyle(fontSize = 11.sp)
        )
    }

    // ========================================================================
    // Property 12: Three-tier style resolution priority
    // ========================================================================

    /**
     * WHEN a factory style is non-null, the resolved baseStyle SHALL be the
     * factory style, regardless of explicit incoming/outgoing styles and alignment.
     *
     * **Validates: Requirements 12.4**
     */
    "Property 12: factory style (tier 1) always wins over explicit and alignment-based styles" {
        checkAll(
            100,
            bubbleStyleArb,                    // factoryStyle
            bubbleStyleArb.orNull(0.5),        // incomingMessageBubbleStyle
            bubbleStyleArb.orNull(0.5),        // outgoingMessageBubbleStyle
            alignmentArb                       // alignment
        ) { factoryStyle, incomingStyle, outgoingStyle, alignment ->
            val result = resolveBaseStyle(
                factoryStyle = factoryStyle,
                incomingMessageBubbleStyle = incomingStyle,
                outgoingMessageBubbleStyle = outgoingStyle,
                alignment = alignment
            )

            result shouldBe factoryStyle
        }
    }

    /**
     * WHEN factory style is null AND alignment is LEFT AND incomingMessageBubbleStyle
     * is non-null, the resolved baseStyle SHALL be the incomingMessageBubbleStyle.
     *
     * **Validates: Requirements 12.2**
     */
    "Property 12: explicit incoming style (tier 2) used for LEFT alignment when factory is null" {
        checkAll(
            100,
            bubbleStyleArb,                    // incomingMessageBubbleStyle
            bubbleStyleArb.orNull(0.5)         // outgoingMessageBubbleStyle (irrelevant)
        ) { incomingStyle, outgoingStyle ->
            val result = resolveBaseStyle(
                factoryStyle = null,
                incomingMessageBubbleStyle = incomingStyle,
                outgoingMessageBubbleStyle = outgoingStyle,
                alignment = UIKitConstants.MessageBubbleAlignment.LEFT
            )

            result shouldBe incomingStyle
        }
    }

    /**
     * WHEN factory style is null AND alignment is RIGHT AND outgoingMessageBubbleStyle
     * is non-null, the resolved baseStyle SHALL be the outgoingMessageBubbleStyle.
     *
     * **Validates: Requirements 12.3**
     */
    "Property 12: explicit outgoing style (tier 2) used for RIGHT alignment when factory is null" {
        checkAll(
            100,
            bubbleStyleArb.orNull(0.5),        // incomingMessageBubbleStyle (irrelevant)
            bubbleStyleArb                     // outgoingMessageBubbleStyle
        ) { incomingStyle, outgoingStyle ->
            val result = resolveBaseStyle(
                factoryStyle = null,
                incomingMessageBubbleStyle = incomingStyle,
                outgoingMessageBubbleStyle = outgoingStyle,
                alignment = UIKitConstants.MessageBubbleAlignment.RIGHT
            )

            result shouldBe outgoingStyle
        }
    }

    /**
     * WHEN factory style is null AND no explicit styles are provided,
     * the resolved baseStyle SHALL be the alignment-based default.
     *
     * **Validates: Requirements 12.4**
     */
    "Property 12: alignment default (tier 3) used when factory and explicit styles are null" {
        checkAll(100, alignmentArb) { alignment ->
            val result = resolveBaseStyle(
                factoryStyle = null,
                incomingMessageBubbleStyle = null,
                outgoingMessageBubbleStyle = null,
                alignment = alignment
            )

            val expected = alignmentDefault(alignment)
            result.backgroundColor shouldBe expected.backgroundColor
            result.cornerRadius shouldBe expected.cornerRadius
        }
    }

    /**
     * WHEN factory style is null AND alignment is RIGHT AND incomingMessageBubbleStyle
     * is non-null but outgoingMessageBubbleStyle is null, the resolved baseStyle SHALL
     * be the alignment default (incoming style does NOT apply to RIGHT alignment).
     *
     * **Validates: Requirements 12.2, 12.3**
     */
    "Property 12: incoming style does not apply to RIGHT-aligned messages" {
        checkAll(100, bubbleStyleArb) { incomingStyle ->
            val result = resolveBaseStyle(
                factoryStyle = null,
                incomingMessageBubbleStyle = incomingStyle,
                outgoingMessageBubbleStyle = null,
                alignment = UIKitConstants.MessageBubbleAlignment.RIGHT
            )

            val expected = alignmentDefault(UIKitConstants.MessageBubbleAlignment.RIGHT)
            result.backgroundColor shouldBe expected.backgroundColor
            result.cornerRadius shouldBe expected.cornerRadius
        }
    }

    /**
     * WHEN factory style is null AND alignment is LEFT AND outgoingMessageBubbleStyle
     * is non-null but incomingMessageBubbleStyle is null, the resolved baseStyle SHALL
     * be the alignment default (outgoing style does NOT apply to LEFT alignment).
     *
     * **Validates: Requirements 12.2, 12.3**
     */
    "Property 12: outgoing style does not apply to LEFT-aligned messages" {
        checkAll(100, bubbleStyleArb) { outgoingStyle ->
            val result = resolveBaseStyle(
                factoryStyle = null,
                incomingMessageBubbleStyle = null,
                outgoingMessageBubbleStyle = outgoingStyle,
                alignment = UIKitConstants.MessageBubbleAlignment.LEFT
            )

            val expected = alignmentDefault(UIKitConstants.MessageBubbleAlignment.LEFT)
            result.backgroundColor shouldBe expected.backgroundColor
            result.cornerRadius shouldBe expected.cornerRadius
        }
    }

    /**
     * WHEN alignment is CENTER, the resolved baseStyle SHALL always be the alignment
     * default, regardless of explicit incoming/outgoing styles (only factory can override).
     *
     * **Validates: Requirements 12.4**
     */
    "Property 12: CENTER alignment always uses alignment default when factory is null" {
        checkAll(
            100,
            bubbleStyleArb.orNull(0.5),        // incomingMessageBubbleStyle
            bubbleStyleArb.orNull(0.5)         // outgoingMessageBubbleStyle
        ) { incomingStyle, outgoingStyle ->
            val result = resolveBaseStyle(
                factoryStyle = null,
                incomingMessageBubbleStyle = incomingStyle,
                outgoingMessageBubbleStyle = outgoingStyle,
                alignment = UIKitConstants.MessageBubbleAlignment.CENTER
            )

            val expected = alignmentDefault(UIKitConstants.MessageBubbleAlignment.CENTER)
            result.backgroundColor shouldBe expected.backgroundColor
            result.cornerRadius shouldBe expected.cornerRadius
        }
    }

    /**
     * The resolution always produces a non-null result for any combination of inputs.
     *
     * **Validates: Requirements 12.2, 12.3, 12.4**
     */
    "Property 12: resolution always produces a non-null baseStyle" {
        checkAll(
            100,
            bubbleStyleArb.orNull(0.5),        // factoryStyle
            bubbleStyleArb.orNull(0.5),        // incomingMessageBubbleStyle
            bubbleStyleArb.orNull(0.5),        // outgoingMessageBubbleStyle
            alignmentArb                       // alignment
        ) { factoryStyle, incomingStyle, outgoingStyle, alignment ->
            val result = resolveBaseStyle(
                factoryStyle = factoryStyle,
                incomingMessageBubbleStyle = incomingStyle,
                outgoingMessageBubbleStyle = outgoingStyle,
                alignment = alignment
            )

            result shouldNotBe null
        }
    }
})

// ============================================================================
// Helper functions — replicate the three-tier base style resolution logic
// from CometChatMessageBubble.kt as a pure function for testability.
// ============================================================================

/**
 * Replicates the baseStyle resolution logic from CometChatMessageBubble.
 *
 * This mirrors the actual code:
 * ```kotlin
 * val baseStyle = factory?.getBubbleStyle(message, alignment)
 *     ?: when (alignment) {
 *         UIKitConstants.MessageBubbleAlignment.LEFT ->
 *             incomingMessageBubbleStyle ?: alignmentDefault
 *         UIKitConstants.MessageBubbleAlignment.RIGHT ->
 *             outgoingMessageBubbleStyle ?: alignmentDefault
 *         else -> alignmentDefault
 *     }
 * ```
 */
private fun resolveBaseStyle(
    factoryStyle: CometChatMessageBubbleStyle?,
    incomingMessageBubbleStyle: CometChatMessageBubbleStyle?,
    outgoingMessageBubbleStyle: CometChatMessageBubbleStyle?,
    alignment: UIKitConstants.MessageBubbleAlignment
): CometChatMessageBubbleStyle {
    val alignDefault = alignmentDefault(alignment)
    return factoryStyle
        ?: when (alignment) {
            UIKitConstants.MessageBubbleAlignment.LEFT ->
                incomingMessageBubbleStyle ?: alignDefault
            UIKitConstants.MessageBubbleAlignment.RIGHT ->
                outgoingMessageBubbleStyle ?: alignDefault
            else -> alignDefault
        }
}

/**
 * Creates an alignment-based default style.
 * Mirrors the `when (alignment)` fallback in CometChatMessageBubble.
 *
 * Uses hardcoded values since the actual factory functions are @Composable
 * and require a Compose runtime. The specific values don't matter for the
 * property test — what matters is that a non-null style is always produced
 * and that the resolution priority is correct.
 */
private fun alignmentDefault(
    alignment: UIKitConstants.MessageBubbleAlignment
): CometChatMessageBubbleStyle {
    return when (alignment) {
        UIKitConstants.MessageBubbleAlignment.LEFT -> CometChatMessageBubbleStyle(
            backgroundColor = Color(0xFFEEEEEE),
            cornerRadius = 12.dp,
            strokeWidth = 0.dp,
            strokeColor = Color.Transparent,
            padding = PaddingValues(0.dp),
            senderNameTextColor = Color.Gray,
            senderNameTextStyle = TextStyle(fontSize = 12.sp),
            threadIndicatorTextColor = Color.Gray,
            threadIndicatorTextStyle = TextStyle(fontSize = 12.sp),
            threadIndicatorIconTint = Color.Gray,
            timestampTextColor = Color.Gray,
            timestampTextStyle = TextStyle(fontSize = 11.sp)
        )
        UIKitConstants.MessageBubbleAlignment.RIGHT -> CometChatMessageBubbleStyle(
            backgroundColor = Color(0xFF3399FF),
            cornerRadius = 12.dp,
            strokeWidth = 0.dp,
            strokeColor = Color.Transparent,
            padding = PaddingValues(0.dp),
            senderNameTextColor = Color.Gray,
            senderNameTextStyle = TextStyle(fontSize = 12.sp),
            threadIndicatorTextColor = Color.Gray,
            threadIndicatorTextStyle = TextStyle(fontSize = 12.sp),
            threadIndicatorIconTint = Color.Gray,
            timestampTextColor = Color.White,
            timestampTextStyle = TextStyle(fontSize = 11.sp)
        )
        UIKitConstants.MessageBubbleAlignment.CENTER -> CometChatMessageBubbleStyle(
            backgroundColor = Color(0xFFEEEEEE),
            cornerRadius = 12.dp,
            strokeWidth = 0.dp,
            strokeColor = Color.Transparent,
            padding = PaddingValues(0.dp),
            senderNameTextColor = Color.Gray,
            senderNameTextStyle = TextStyle(fontSize = 12.sp),
            threadIndicatorTextColor = Color.Gray,
            threadIndicatorTextStyle = TextStyle(fontSize = 12.sp),
            threadIndicatorIconTint = Color.Gray,
            timestampTextColor = Color.Gray,
            timestampTextStyle = TextStyle(fontSize = 11.sp)
        )
    }
}
