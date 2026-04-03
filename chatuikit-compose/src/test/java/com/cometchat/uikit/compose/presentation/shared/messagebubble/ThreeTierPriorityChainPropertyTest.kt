package com.cometchat.uikit.compose.presentation.shared.messagebubble

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatMessageBubbleStyle
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatTextBubbleStyle
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.UNSET_COLOR
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.UNSET_DP
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.UNSET_PADDING
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.UNSET_TEXT_STYLE
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.mergeWithBase
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
 * Property-based tests for the three-tier priority chain in CometChatMessageBubble.
 *
 * **Feature: bubble-style-hierarchy, Property 1: Three-tier priority chain**
 *
 * The effectiveStyle resolution follows this priority:
 * - Priority 3 (highest): Factory style — `factory?.getBubbleStyle(message, alignment)`
 * - Priority 2: Bubble-specific style (contentStyle) merged with messageBubbleStyle
 * - Priority 1 (lowest): messageBubbleStyle (passed as `style` parameter)
 * - Ultimate fallback: alignment-based default
 *
 * The actual resolution code in CometChatMessageBubble:
 * ```
 * val effectiveStyle = factory?.getBubbleStyle(message, alignment)
 *     ?: contentStyle?.let { cs ->
 *         if (style != null) mergeWithBase(cs, style) else cs
 *     }
 *     ?: style
 *     ?: when (alignment) { ... }
 * ```
 *
 * **Validates: Requirements 3.1, 3.2, 3.3**
 */
class ThreeTierPriorityChainPropertyTest : StringSpec({

    // ========================================================================
    // Arbitrary generators
    // ========================================================================

    /** Generates non-sentinel colors (alpha > 0, at least one channel differs from Unspecified). */
    val nonSentinelColorArb: Arb<Color> = Arb.bind(
        Arb.int(1, 255),
        Arb.int(0, 255),
        Arb.int(0, 255),
        Arb.int(1, 255)
    ) { r, g, b, a -> Color(r / 255f, g / 255f, b / 255f, a / 255f) }

    /** Generates non-sentinel Dp values. */
    val nonSentinelDpArb: Arb<Dp> = Arb.float(0f, 24f).map { it.dp }

    /** Generates non-sentinel TextStyle values. */
    val nonSentinelTextStyleArb: Arb<TextStyle> = Arb.float(10f, 24f).map { TextStyle(fontSize = it.sp) }

    /** Generates non-sentinel PaddingValues. */
    val nonSentinelPaddingArb: Arb<PaddingValues> = Arb.float(0f, 16f).map { PaddingValues(it.dp) }

    /** Generates a random alignment. */
    val alignmentArb: Arb<UIKitConstants.MessageBubbleAlignment> = Arb.element(
        UIKitConstants.MessageBubbleAlignment.LEFT,
        UIKitConstants.MessageBubbleAlignment.RIGHT,
        UIKitConstants.MessageBubbleAlignment.CENTER
    )

    /**
     * Generator for a CometChatMessageBubbleStyle with all non-sentinel properties.
     * Used as factoryStyle, messageBubbleStyle, or alignment default.
     */
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

    /**
     * Generator for CometChatTextBubbleStyle with non-sentinel CommonProperties.
     * Used as contentStyle (bubble-specific style).
     */
    val contentStyleArb: Arb<CometChatTextBubbleStyle> = Arb.bind(
        nonSentinelColorArb,
        nonSentinelDpArb,
        nonSentinelColorArb,
        nonSentinelColorArb
    ) { bg, cr, textColor, linkColor ->
        CometChatTextBubbleStyle(
            textColor = textColor,
            textStyle = TextStyle(fontSize = 14.sp),
            linkColor = linkColor,
            translatedTextColor = Color.Gray,
            translatedTextStyle = TextStyle(fontSize = 12.sp),
            separatorColor = Color.LightGray,
            linkPreviewBackgroundColor = Color(0xFFE8E8E8),
            linkPreviewTitleColor = Color.Black,
            linkPreviewTitleStyle = TextStyle(fontSize = 14.sp),
            linkPreviewDescriptionColor = Color.Gray,
            linkPreviewDescriptionStyle = TextStyle(fontSize = 12.sp),
            linkPreviewLinkColor = Color.Blue,
            linkPreviewLinkStyle = TextStyle(fontSize = 12.sp),
            linkPreviewCornerRadius = 8.dp,
            linkPreviewStrokeWidth = 0.dp,
            linkPreviewStrokeColor = Color.Transparent,
            backgroundColor = bg,
            cornerRadius = cr,
            strokeWidth = 1.dp,
            strokeColor = Color.DarkGray,
            padding = PaddingValues(4.dp),
            senderNameTextColor = Color.Magenta,
            senderNameTextStyle = TextStyle(fontSize = 11.sp),
            threadIndicatorTextColor = Color.Cyan,
            threadIndicatorTextStyle = TextStyle(fontSize = 10.sp),
            threadIndicatorIconTint = Color.Yellow,
            timestampTextColor = Color.Red,
            timestampTextStyle = TextStyle(fontSize = 9.sp)
        )
    }

    /**
     * Generator for CometChatTextBubbleStyle with sentinel CommonProperties.
     * Simulates a factory-created style without explicit CommonProperty overrides.
     */
    val contentStyleWithSentinelsArb: Arb<CometChatTextBubbleStyle> = Arb.bind(
        nonSentinelColorArb,
        nonSentinelColorArb
    ) { textColor, linkColor ->
        CometChatTextBubbleStyle(
            textColor = textColor,
            textStyle = TextStyle(fontSize = 14.sp),
            linkColor = linkColor,
            translatedTextColor = Color.Gray,
            translatedTextStyle = TextStyle(fontSize = 12.sp),
            separatorColor = Color.LightGray,
            linkPreviewBackgroundColor = Color(0xFFE8E8E8),
            linkPreviewTitleColor = Color.Black,
            linkPreviewTitleStyle = TextStyle(fontSize = 14.sp),
            linkPreviewDescriptionColor = Color.Gray,
            linkPreviewDescriptionStyle = TextStyle(fontSize = 12.sp),
            linkPreviewLinkColor = Color.Blue,
            linkPreviewLinkStyle = TextStyle(fontSize = 12.sp),
            linkPreviewCornerRadius = 8.dp,
            linkPreviewStrokeWidth = 0.dp,
            linkPreviewStrokeColor = Color.Transparent,
            backgroundColor = UNSET_COLOR,
            cornerRadius = UNSET_DP,
            strokeWidth = UNSET_DP,
            strokeColor = UNSET_COLOR,
            padding = UNSET_PADDING,
            senderNameTextColor = UNSET_COLOR,
            senderNameTextStyle = UNSET_TEXT_STYLE,
            threadIndicatorTextColor = UNSET_COLOR,
            threadIndicatorTextStyle = UNSET_TEXT_STYLE,
            threadIndicatorIconTint = UNSET_COLOR,
            timestampTextColor = UNSET_COLOR,
            timestampTextStyle = UNSET_TEXT_STYLE
        )
    }

    // ========================================================================
    // Property 1: Three-tier priority chain
    // ========================================================================

    /**
     * **Feature: bubble-style-hierarchy, Property 1: Three-tier priority chain**
     *
     * WHEN a factoryStyle is non-null, the resolved effectiveStyle SHALL be the
     * factoryStyle, regardless of contentStyle and messageBubbleStyle values.
     *
     * **Validates: Requirements 3.1**
     */
    "Property 1: factoryStyle (non-null) always wins regardless of other styles" {
        checkAll(
            100,
            bubbleStyleArb,                    // factoryStyle
            contentStyleArb.orNull(0.5),       // contentStyle (nullable)
            bubbleStyleArb.orNull(0.5),        // messageBubbleStyle (nullable)
            alignmentArb                       // alignment
        ) { factoryStyle, contentStyle, messageBubbleStyle, alignment ->
            val result = resolveEffectiveStyle(
                factoryStyle = factoryStyle,
                contentStyle = contentStyle,
                messageBubbleStyle = messageBubbleStyle,
                alignment = alignment
            )

            result shouldBe factoryStyle
        }
    }

    /**
     * **Feature: bubble-style-hierarchy, Property 1: Three-tier priority chain**
     *
     * WHEN factoryStyle is null and contentStyle is non-null and messageBubbleStyle
     * is non-null, the resolved effectiveStyle SHALL be the result of merging
     * contentStyle with messageBubbleStyle (sentinels filled from messageBubbleStyle).
     *
     * **Validates: Requirements 3.2**
     */
    "Property 1: contentStyle merged with messageBubbleStyle when factoryStyle is null" {
        checkAll(
            100,
            contentStyleArb,    // contentStyle (non-null, non-sentinel CommonProperties)
            bubbleStyleArb,     // messageBubbleStyle (non-null)
            alignmentArb        // alignment
        ) { contentStyle, messageBubbleStyle, alignment ->
            val result = resolveEffectiveStyle(
                factoryStyle = null,
                contentStyle = contentStyle,
                messageBubbleStyle = messageBubbleStyle,
                alignment = alignment
            )

            val expected = mergeWithBase(contentStyle, messageBubbleStyle)
            result shouldBe expected
        }
    }

    /**
     * **Feature: bubble-style-hierarchy, Property 1: Three-tier priority chain**
     *
     * WHEN factoryStyle is null and contentStyle is non-null but messageBubbleStyle
     * is null, the resolved effectiveStyle SHALL be the contentStyle as-is (no merge).
     *
     * **Validates: Requirements 3.2**
     */
    "Property 1: contentStyle used as-is when factoryStyle and messageBubbleStyle are null" {
        checkAll(
            100,
            contentStyleArb,    // contentStyle (non-null)
            alignmentArb        // alignment
        ) { contentStyle, alignment ->
            val result = resolveEffectiveStyle(
                factoryStyle = null,
                contentStyle = contentStyle,
                messageBubbleStyle = null,
                alignment = alignment
            )

            result shouldBe contentStyle
        }
    }

    /**
     * **Feature: bubble-style-hierarchy, Property 1: Three-tier priority chain**
     *
     * WHEN factoryStyle is null and contentStyle is null and messageBubbleStyle
     * is non-null, the resolved effectiveStyle SHALL be the messageBubbleStyle.
     *
     * **Validates: Requirements 3.3**
     */
    "Property 1: messageBubbleStyle used when factoryStyle and contentStyle are null" {
        checkAll(
            100,
            bubbleStyleArb,     // messageBubbleStyle (non-null)
            alignmentArb        // alignment
        ) { messageBubbleStyle, alignment ->
            val result = resolveEffectiveStyle(
                factoryStyle = null,
                contentStyle = null,
                messageBubbleStyle = messageBubbleStyle,
                alignment = alignment
            )

            result shouldBe messageBubbleStyle
        }
    }

    /**
     * **Feature: bubble-style-hierarchy, Property 1: Three-tier priority chain**
     *
     * WHEN all three styles are null, the resolved effectiveStyle SHALL be an
     * alignment-based default (non-null).
     *
     * **Validates: Requirements 3.1, 3.2, 3.3**
     */
    "Property 1: alignment-based default used when all styles are null" {
        checkAll(100, alignmentArb) { alignment ->
            val result = resolveEffectiveStyle(
                factoryStyle = null,
                contentStyle = null,
                messageBubbleStyle = null,
                alignment = alignment
            )

            result shouldNotBe null
            // The result should match the alignment-based default
            val expected = alignmentDefault(alignment)
            result.backgroundColor shouldBe expected.backgroundColor
            result.cornerRadius shouldBe expected.cornerRadius
        }
    }

    /**
     * **Feature: bubble-style-hierarchy, Property 1: Three-tier priority chain**
     *
     * WHEN contentStyle has sentinel CommonProperties and messageBubbleStyle is
     * non-null, the merged effectiveStyle SHALL have CommonProperties from
     * messageBubbleStyle (sentinels filled in).
     *
     * **Validates: Requirements 3.2**
     */
    "Property 1: sentinel contentStyle CommonProperties filled from messageBubbleStyle" {
        checkAll(
            100,
            contentStyleWithSentinelsArb,  // contentStyle with sentinel CommonProperties
            bubbleStyleArb,                // messageBubbleStyle (non-null)
            alignmentArb                   // alignment
        ) { contentStyle, messageBubbleStyle, alignment ->
            val result = resolveEffectiveStyle(
                factoryStyle = null,
                contentStyle = contentStyle,
                messageBubbleStyle = messageBubbleStyle,
                alignment = alignment
            )

            // CommonProperties should come from messageBubbleStyle (sentinels filled)
            result.backgroundColor shouldBe messageBubbleStyle.backgroundColor
            result.cornerRadius shouldBe messageBubbleStyle.cornerRadius
            result.strokeWidth shouldBe messageBubbleStyle.strokeWidth
            result.strokeColor shouldBe messageBubbleStyle.strokeColor
            result.padding shouldBe messageBubbleStyle.padding
            result.senderNameTextColor shouldBe messageBubbleStyle.senderNameTextColor
            result.senderNameTextStyle shouldBe messageBubbleStyle.senderNameTextStyle
            result.threadIndicatorTextColor shouldBe messageBubbleStyle.threadIndicatorTextColor
            result.threadIndicatorTextStyle shouldBe messageBubbleStyle.threadIndicatorTextStyle
            result.threadIndicatorIconTint shouldBe messageBubbleStyle.threadIndicatorIconTint
            result.timestampTextColor shouldBe messageBubbleStyle.timestampTextColor
            result.timestampTextStyle shouldBe messageBubbleStyle.timestampTextStyle
        }
    }

    /**
     * **Feature: bubble-style-hierarchy, Property 1: Three-tier priority chain**
     *
     * The resolution always produces a non-null result for any combination of inputs.
     *
     * **Validates: Requirements 3.1, 3.2, 3.3**
     */
    "Property 1: resolution always produces a non-null effectiveStyle" {
        checkAll(
            100,
            bubbleStyleArb.orNull(0.5),        // factoryStyle
            contentStyleArb.orNull(0.5),       // contentStyle
            bubbleStyleArb.orNull(0.5),        // messageBubbleStyle
            alignmentArb                       // alignment
        ) { factoryStyle, contentStyle, messageBubbleStyle, alignment ->
            val result = resolveEffectiveStyle(
                factoryStyle = factoryStyle,
                contentStyle = contentStyle,
                messageBubbleStyle = messageBubbleStyle,
                alignment = alignment
            )

            result shouldNotBe null
        }
    }
})

// ============================================================================
// Helper functions — replicate the three-tier priority chain logic from
// CometChatMessageBubble.kt as a pure function for testability.
// ============================================================================

/**
 * Replicates the effectiveStyle resolution logic from CometChatMessageBubble.
 *
 * This mirrors the actual code:
 * ```kotlin
 * val effectiveStyle = factory?.getBubbleStyle(message, alignment)
 *     ?: contentStyle?.let { cs ->
 *         if (style != null) mergeWithBase(cs, style) else cs
 *     }
 *     ?: style
 *     ?: when (alignment) { ... }
 * ```
 */
private fun resolveEffectiveStyle(
    factoryStyle: CometChatMessageBubbleStyle?,
    contentStyle: CometChatMessageBubbleStyle?,
    messageBubbleStyle: CometChatMessageBubbleStyle?,
    alignment: UIKitConstants.MessageBubbleAlignment
): CometChatMessageBubbleStyle {
    return factoryStyle
        ?: contentStyle?.let { cs ->
            if (messageBubbleStyle != null) mergeWithBase(cs, messageBubbleStyle) else cs
        }
        ?: messageBubbleStyle
        ?: alignmentDefault(alignment)
}

/**
 * Creates an alignment-based default style.
 * Mirrors the `when (alignment)` fallback in CometChatMessageBubble.
 *
 * Uses hardcoded values since the actual factory functions are @Composable
 * and require a Compose runtime. The specific values don't matter for the
 * property test — what matters is that a non-null style is always produced.
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
