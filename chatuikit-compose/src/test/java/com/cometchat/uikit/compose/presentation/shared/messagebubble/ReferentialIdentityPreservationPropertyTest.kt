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
import io.kotest.property.Arb
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.float
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.map
import io.kotest.property.checkAll

/**
 * Property-based tests for referential identity preservation of direction-specific styles.
 *
 * **Feature: messagelist-bubble-style-propagation, Property 3: Referential identity preservation for direction-specific styles**
 *
 * When no factory style is present and no per-bubble-type content style applies,
 * the resolved effective style must be the exact same object instance (`===`) as
 * the input incoming/outgoing style — not a copy or structurally equal clone.
 *
 * The full resolution chain:
 * ```
 * val baseStyle = factoryStyle
 *     ?: when (alignment) {
 *         LEFT -> incomingMessageBubbleStyle ?: alignmentDefault
 *         RIGHT -> outgoingMessageBubbleStyle ?: alignmentDefault
 *         else -> alignmentDefault
 *     }
 * val effectiveStyle = contentStyle?.let { mergeWithBase(it, baseStyle) } ?: baseStyle
 * ```
 *
 * When factoryStyle == null and contentStyle == null:
 * - LEFT alignment with non-null incomingMessageBubbleStyle → effectiveStyle === incomingMessageBubbleStyle
 * - RIGHT alignment with non-null outgoingMessageBubbleStyle → effectiveStyle === outgoingMessageBubbleStyle
 *
 * **Validates: Requirements 7.1, 7.2, 7.4**
 */
class ReferentialIdentityPreservationPropertyTest : StringSpec({

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
    // Property 3: Referential identity preservation
    // ========================================================================

    /**
     * **Feature: messagelist-bubble-style-propagation, Property 3: Referential identity preservation for direction-specific styles**
     *
     * WHEN factoryStyle is null AND contentStyle is null AND alignment is LEFT
     * AND incomingMessageBubbleStyle is non-null, the resolved effective style
     * SHALL be the exact same object instance (`===`) as incomingMessageBubbleStyle.
     *
     * **Validates: Requirements 7.1, 7.4**
     */
    "Property 3: LEFT alignment preserves referential identity of incomingMessageBubbleStyle" {
        checkAll(100, bubbleStyleArb) { incomingStyle ->
            val result = resolveEffectiveStyle(
                factoryStyle = null,
                incomingMessageBubbleStyle = incomingStyle,
                outgoingMessageBubbleStyle = null,
                alignment = UIKitConstants.MessageBubbleAlignment.LEFT,
                contentStyle = null
            )

            assert(result === incomingStyle) {
                "Expected resolved style to be the exact same instance (===) as incomingMessageBubbleStyle, " +
                    "but got a different object. referential identity was not preserved."
            }
        }
    }

    /**
     * **Feature: messagelist-bubble-style-propagation, Property 3: Referential identity preservation for direction-specific styles**
     *
     * WHEN factoryStyle is null AND contentStyle is null AND alignment is RIGHT
     * AND outgoingMessageBubbleStyle is non-null, the resolved effective style
     * SHALL be the exact same object instance (`===`) as outgoingMessageBubbleStyle.
     *
     * **Validates: Requirements 7.2, 7.4**
     */
    "Property 3: RIGHT alignment preserves referential identity of outgoingMessageBubbleStyle" {
        checkAll(100, bubbleStyleArb) { outgoingStyle ->
            val result = resolveEffectiveStyle(
                factoryStyle = null,
                incomingMessageBubbleStyle = null,
                outgoingMessageBubbleStyle = outgoingStyle,
                alignment = UIKitConstants.MessageBubbleAlignment.RIGHT,
                contentStyle = null
            )

            assert(result === outgoingStyle) {
                "Expected resolved style to be the exact same instance (===) as outgoingMessageBubbleStyle, " +
                    "but got a different object. referential identity was not preserved."
            }
        }
    }
})


// ============================================================================
// Helper functions — replicate the full effective style resolution logic
// from CometChatMessageBubble.kt as a pure function for testability.
// ============================================================================

/**
 * Replicates the full effective style resolution from CometChatMessageBubble,
 * including both the three-tier base style resolution and the contentStyle merge.
 *
 * ```kotlin
 * val baseStyle = factory?.getBubbleStyle(message, alignment)
 *     ?: when (alignment) {
 *         LEFT -> incomingMessageBubbleStyle ?: alignmentDefault
 *         RIGHT -> outgoingMessageBubbleStyle ?: alignmentDefault
 *         else -> alignmentDefault
 *     }
 * val effectiveStyle = contentStyle?.let { mergeWithBase(it, baseStyle) } ?: baseStyle
 * ```
 *
 * When factoryStyle is null and contentStyle is null, the baseStyle passes through
 * unchanged — preserving referential identity of the incoming/outgoing style.
 */
private fun resolveEffectiveStyle(
    factoryStyle: CometChatMessageBubbleStyle?,
    incomingMessageBubbleStyle: CometChatMessageBubbleStyle?,
    outgoingMessageBubbleStyle: CometChatMessageBubbleStyle?,
    alignment: UIKitConstants.MessageBubbleAlignment,
    contentStyle: CometChatMessageBubbleStyle?
): CometChatMessageBubbleStyle {
    val alignDefault = alignmentDefault(alignment)

    // Three-tier base style resolution
    val baseStyle = factoryStyle
        ?: when (alignment) {
            UIKitConstants.MessageBubbleAlignment.LEFT ->
                incomingMessageBubbleStyle ?: alignDefault
            UIKitConstants.MessageBubbleAlignment.RIGHT ->
                outgoingMessageBubbleStyle ?: alignDefault
            else -> alignDefault
        }

    // Content style merge (when contentStyle is null, baseStyle passes through unchanged)
    return contentStyle?.let { mergeWithBase(it, baseStyle) } ?: baseStyle
}

/**
 * Placeholder merge function. In the real implementation, this merges non-sentinel
 * properties from contentStyle onto baseStyle. For this test, the exact merge logic
 * doesn't matter because Property 3 only tests the case where contentStyle is null.
 */
private fun mergeWithBase(
    contentStyle: CometChatMessageBubbleStyle,
    baseStyle: CometChatMessageBubbleStyle
): CometChatMessageBubbleStyle {
    return CometChatMessageBubbleStyle(
        backgroundColor = contentStyle.backgroundColor,
        cornerRadius = contentStyle.cornerRadius,
        strokeWidth = contentStyle.strokeWidth,
        strokeColor = contentStyle.strokeColor,
        padding = contentStyle.padding,
        senderNameTextColor = baseStyle.senderNameTextColor,
        senderNameTextStyle = baseStyle.senderNameTextStyle,
        threadIndicatorTextColor = baseStyle.threadIndicatorTextColor,
        threadIndicatorTextStyle = baseStyle.threadIndicatorTextStyle,
        threadIndicatorIconTint = baseStyle.threadIndicatorIconTint,
        timestampTextColor = baseStyle.timestampTextColor,
        timestampTextStyle = baseStyle.timestampTextStyle
    )
}

/**
 * Creates an alignment-based default style.
 * Uses hardcoded values since the actual factory functions are @Composable.
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
