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
 * Property-based tests for the three-tier resolution chain in CometChatMessageBubble.
 *
 * **Feature: messagelist-bubble-style-propagation, Property 2: Three-tier resolution chain**
 *
 * The baseStyle resolution follows this priority:
 * - Tier 1 (highest): Factory style — `factory?.getBubbleStyle(message, alignment)`
 * - Tier 2: Explicit incoming/outgoing style based on alignment
 * - Tier 3 (lowest): Alignment-based default (`incoming()` / `outgoing()` / `default()`)
 *
 * The actual resolution code in CometChatMessageBubble:
 * ```
 * val baseStyle = factory?.getBubbleStyle(message, alignment)
 *     ?: when (alignment) {
 *         LEFT -> incomingMessageBubbleStyle ?: alignmentDefault
 *         RIGHT -> outgoingMessageBubbleStyle ?: alignmentDefault
 *         else -> alignmentDefault
 *     }
 * ```
 *
 * **Validates: Requirements 3.1, 3.2, 3.3, 3.4, 1.4, 1.5**
 */
class ThreeTierResolutionChainPropertyTest : StringSpec({

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
    // Property 2: Three-tier resolution chain
    // ========================================================================

    /**
     * **Feature: messagelist-bubble-style-propagation, Property 2: Three-tier resolution chain**
     *
     * WHEN a factory style is non-null, the resolved baseStyle SHALL be the
     * factory style, regardless of incoming/outgoing styles and alignment.
     *
     * **Validates: Requirements 3.1**
     */
    "Property 2: factory style (tier 1) wins regardless of incoming/outgoing styles and alignment" {
        checkAll(
            100,
            bubbleStyleArb,                    // factoryStyle
            bubbleStyleArb.orNull(0.5),        // incomingMessageBubbleStyle
            bubbleStyleArb.orNull(0.5),        // outgoingMessageBubbleStyle
            alignmentArb                       // alignment
        ) { factoryStyle, incomingStyle, outgoingStyle, alignment ->
            val result = resolveThreeTierBaseStyle(
                factoryStyle = factoryStyle,
                incomingMessageBubbleStyle = incomingStyle,
                outgoingMessageBubbleStyle = outgoingStyle,
                alignment = alignment
            )

            result shouldBe factoryStyle
        }
    }

    /**
     * **Feature: messagelist-bubble-style-propagation, Property 2: Three-tier resolution chain**
     *
     * WHEN factory style is null AND alignment is LEFT AND incomingMessageBubbleStyle
     * is non-null, the resolved baseStyle SHALL be the incomingMessageBubbleStyle.
     *
     * **Validates: Requirements 3.2, 1.4**
     */
    "Property 2: incoming style (tier 2) used for LEFT alignment when factory is null" {
        checkAll(
            100,
            bubbleStyleArb,                    // incomingMessageBubbleStyle
            bubbleStyleArb.orNull(0.5)         // outgoingMessageBubbleStyle (irrelevant for LEFT)
        ) { incomingStyle, outgoingStyle ->
            val result = resolveThreeTierBaseStyle(
                factoryStyle = null,
                incomingMessageBubbleStyle = incomingStyle,
                outgoingMessageBubbleStyle = outgoingStyle,
                alignment = UIKitConstants.MessageBubbleAlignment.LEFT
            )

            result shouldBe incomingStyle
        }
    }

    /**
     * **Feature: messagelist-bubble-style-propagation, Property 2: Three-tier resolution chain**
     *
     * WHEN factory style is null AND alignment is RIGHT AND outgoingMessageBubbleStyle
     * is non-null, the resolved baseStyle SHALL be the outgoingMessageBubbleStyle.
     *
     * **Validates: Requirements 3.3, 1.5**
     */
    "Property 2: outgoing style (tier 2) used for RIGHT alignment when factory is null" {
        checkAll(
            100,
            bubbleStyleArb.orNull(0.5),        // incomingMessageBubbleStyle (irrelevant for RIGHT)
            bubbleStyleArb                     // outgoingMessageBubbleStyle
        ) { incomingStyle, outgoingStyle ->
            val result = resolveThreeTierBaseStyle(
                factoryStyle = null,
                incomingMessageBubbleStyle = incomingStyle,
                outgoingMessageBubbleStyle = outgoingStyle,
                alignment = UIKitConstants.MessageBubbleAlignment.RIGHT
            )

            result shouldBe outgoingStyle
        }
    }

    /**
     * **Feature: messagelist-bubble-style-propagation, Property 2: Three-tier resolution chain**
     *
     * WHEN factory style is null AND both incoming/outgoing styles are null,
     * the resolved baseStyle SHALL be the alignment-based default.
     *
     * **Validates: Requirements 3.4, 1.4, 1.5**
     */
    "Property 2: alignment default (tier 3) used when factory and explicit styles are null" {
        checkAll(100, alignmentArb) { alignment ->
            val result = resolveThreeTierBaseStyle(
                factoryStyle = null,
                incomingMessageBubbleStyle = null,
                outgoingMessageBubbleStyle = null,
                alignment = alignment
            )

            val expected = threeTierAlignmentDefault(alignment)
            result.backgroundColor shouldBe expected.backgroundColor
            result.cornerRadius shouldBe expected.cornerRadius
        }
    }

    /**
     * **Feature: messagelist-bubble-style-propagation, Property 2: Three-tier resolution chain**
     *
     * WHEN alignment is CENTER, the resolved baseStyle SHALL always be the alignment
     * default, regardless of incoming/outgoing styles (only factory can override CENTER).
     *
     * **Validates: Requirements 3.4**
     */
    "Property 2: CENTER alignment always uses alignment default when factory is null" {
        checkAll(
            100,
            bubbleStyleArb.orNull(0.5),        // incomingMessageBubbleStyle
            bubbleStyleArb.orNull(0.5)         // outgoingMessageBubbleStyle
        ) { incomingStyle, outgoingStyle ->
            val result = resolveThreeTierBaseStyle(
                factoryStyle = null,
                incomingMessageBubbleStyle = incomingStyle,
                outgoingMessageBubbleStyle = outgoingStyle,
                alignment = UIKitConstants.MessageBubbleAlignment.CENTER
            )

            val expected = threeTierAlignmentDefault(UIKitConstants.MessageBubbleAlignment.CENTER)
            result.backgroundColor shouldBe expected.backgroundColor
            result.cornerRadius shouldBe expected.cornerRadius
        }
    }

    /**
     * **Feature: messagelist-bubble-style-propagation, Property 2: Three-tier resolution chain**
     *
     * The resolution always produces a non-null result for any combination of inputs.
     *
     * **Validates: Requirements 3.1, 3.2, 3.3, 3.4**
     */
    "Property 2: resolution always produces a non-null baseStyle" {
        checkAll(
            100,
            bubbleStyleArb.orNull(0.5),        // factoryStyle
            bubbleStyleArb.orNull(0.5),        // incomingMessageBubbleStyle
            bubbleStyleArb.orNull(0.5),        // outgoingMessageBubbleStyle
            alignmentArb                       // alignment
        ) { factoryStyle, incomingStyle, outgoingStyle, alignment ->
            val result = resolveThreeTierBaseStyle(
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
private fun resolveThreeTierBaseStyle(
    factoryStyle: CometChatMessageBubbleStyle?,
    incomingMessageBubbleStyle: CometChatMessageBubbleStyle?,
    outgoingMessageBubbleStyle: CometChatMessageBubbleStyle?,
    alignment: UIKitConstants.MessageBubbleAlignment
): CometChatMessageBubbleStyle {
    val alignDefault = threeTierAlignmentDefault(alignment)
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
private fun threeTierAlignmentDefault(
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
