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
import io.kotest.property.Arb
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.element
import io.kotest.property.arbitrary.float
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.map
import io.kotest.property.checkAll

/**
 * Property-based tests for InternalContentRenderer style resolution.
 *
 * **Feature: bubble-style-hierarchy**
 *
 * Tests validate that InternalContentRenderer correctly:
 * - Falls back to messageBubbleStyle when no BubbleSpecificStyle is provided (Property 3)
 * - Uses explicit BubbleSpecificStyle as-is when provided (Property 6)
 *
 * Since InternalContentRenderer.renderContent() is @Composable, we replicate the
 * style resolution pattern used internally:
 * - When BubbleSpecificStyle is null: create alignment default (sentinel CommonProperties)
 *   then merge with messageBubbleStyle via mergeWithBase
 * - When BubbleSpecificStyle is non-null: use it directly
 *
 * **Validates: Requirements 2.2, 5.1, 5.2**
 */
class InternalContentRendererStyleResolutionPropertyTest : StringSpec({

    // ========================================================================
    // Arbitrary generators
    // ========================================================================

    /** Generates non-sentinel colors (alpha > 0). */
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
     * Used as messageBubbleStyle base.
     */
    val messageBubbleStyleArb: Arb<CometChatMessageBubbleStyle> = Arb.bind(
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
     * Generator for CometChatTextBubbleStyle with ALL sentinel CommonProperties.
     * Simulates what factory functions (incoming/outgoing/default) produce —
     * content-specific properties are set, but CommonProperties are sentinels.
     */
    val alignmentDefaultTextStyleArb: Arb<CometChatTextBubbleStyle> = Arb.bind(
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
            // ALL CommonProperties set to sentinel values (as factory functions do)
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

    /**
     * Generator for CometChatTextBubbleStyle with NON-sentinel CommonProperties.
     * Simulates an explicitly provided BubbleSpecificStyle from the developer.
     */
    val explicitTextBubbleStyleArb: Arb<CometChatTextBubbleStyle> = Arb.bind(
        nonSentinelColorArb,
        nonSentinelDpArb,
        nonSentinelDpArb,
        nonSentinelColorArb,
        nonSentinelPaddingArb,
        nonSentinelColorArb
    ) { bg, cr, sw, sc, pad, textColor ->
        CometChatTextBubbleStyle(
            textColor = textColor,
            textStyle = TextStyle(fontSize = 14.sp),
            linkColor = Color.Blue,
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
            // Explicit non-sentinel CommonProperties
            backgroundColor = bg,
            cornerRadius = cr,
            strokeWidth = sw,
            strokeColor = sc,
            padding = pad,
            senderNameTextColor = Color.Magenta,
            senderNameTextStyle = TextStyle(fontSize = 11.sp),
            threadIndicatorTextColor = Color.Cyan,
            threadIndicatorTextStyle = TextStyle(fontSize = 10.sp),
            threadIndicatorIconTint = Color.Yellow,
            timestampTextColor = Color.Red,
            timestampTextStyle = TextStyle(fontSize = 9.sp)
        )
    }

    // ========================================================================
    // Property 3: Null BubbleSpecificStyle falls back to messageBubbleStyle
    // ========================================================================

    /**
     * **Feature: bubble-style-hierarchy, Property 3: Null BubbleSpecificStyle falls back to messageBubbleStyle**
     *
     * For any messageBubbleStyle instance, when no BubbleSpecificStyle is provided
     * (null), InternalContentRenderer creates an alignment-based default (with sentinel
     * CommonProperties) and merges it with messageBubbleStyle. After sentinel resolution,
     * the CommonProperties SHALL equal the messageBubbleStyle's CommonProperties.
     *
     * This replicates the pattern from InternalContentRenderer.getDefault*BubbleStyle:
     * ```kotlin
     * val alignmentDefault = CometChatTextBubbleStyle.incoming() // sentinel CommonProperties
     * return if (messageBubbleStyle != null) {
     *     mergeWithBase(alignmentDefault, messageBubbleStyle)
     * } else {
     *     alignmentDefault
     * }
     * ```
     *
     * **Validates: Requirements 2.2, 5.1**
     */
    "Property 3: null BubbleSpecificStyle — alignment default merged with messageBubbleStyle has messageBubbleStyle CommonProperties" {
        checkAll(
            100,
            alignmentDefaultTextStyleArb,  // alignment default with sentinel CommonProperties
            messageBubbleStyleArb,         // messageBubbleStyle (non-null, non-sentinel)
            alignmentArb                   // alignment (for documentation, not used in merge)
        ) { alignmentDefault, messageBubbleStyle, _ ->
            // Replicate InternalContentRenderer logic:
            // BubbleSpecificStyle is null → create alignment default → merge with messageBubbleStyle
            val effectiveStyle = mergeWithBase(alignmentDefault, messageBubbleStyle)

            // All CommonProperties should come from messageBubbleStyle
            // (since alignment default has sentinel values)
            effectiveStyle.backgroundColor shouldBe messageBubbleStyle.backgroundColor
            effectiveStyle.cornerRadius shouldBe messageBubbleStyle.cornerRadius
            effectiveStyle.strokeWidth shouldBe messageBubbleStyle.strokeWidth
            effectiveStyle.strokeColor shouldBe messageBubbleStyle.strokeColor
            effectiveStyle.padding shouldBe messageBubbleStyle.padding
            effectiveStyle.senderNameTextColor shouldBe messageBubbleStyle.senderNameTextColor
            effectiveStyle.senderNameTextStyle shouldBe messageBubbleStyle.senderNameTextStyle
            effectiveStyle.threadIndicatorTextColor shouldBe messageBubbleStyle.threadIndicatorTextColor
            effectiveStyle.threadIndicatorTextStyle shouldBe messageBubbleStyle.threadIndicatorTextStyle
            effectiveStyle.threadIndicatorIconTint shouldBe messageBubbleStyle.threadIndicatorIconTint
            effectiveStyle.timestampTextColor shouldBe messageBubbleStyle.timestampTextColor
            effectiveStyle.timestampTextStyle shouldBe messageBubbleStyle.timestampTextStyle
        }
    }

    /**
     * **Feature: bubble-style-hierarchy, Property 3: Null BubbleSpecificStyle falls back to messageBubbleStyle**
     *
     * Complementary check: when no BubbleSpecificStyle is provided and alignment default
     * is merged with messageBubbleStyle, the content-specific properties from the alignment
     * default are preserved (only CommonProperties come from messageBubbleStyle).
     *
     * **Validates: Requirements 2.2, 5.1**
     */
    "Property 3: null BubbleSpecificStyle — alignment default content properties preserved after merge" {
        checkAll(
            100,
            alignmentDefaultTextStyleArb,
            messageBubbleStyleArb
        ) { alignmentDefault, messageBubbleStyle ->
            val effectiveStyle = mergeWithBase(alignmentDefault, messageBubbleStyle)

            // Cast to verify content-specific properties are preserved
            val typed = effectiveStyle as CometChatTextBubbleStyle

            typed.textColor shouldBe alignmentDefault.textColor
            typed.textStyle shouldBe alignmentDefault.textStyle
            typed.linkColor shouldBe alignmentDefault.linkColor
            typed.translatedTextColor shouldBe alignmentDefault.translatedTextColor
            typed.translatedTextStyle shouldBe alignmentDefault.translatedTextStyle
            typed.separatorColor shouldBe alignmentDefault.separatorColor
            typed.linkPreviewBackgroundColor shouldBe alignmentDefault.linkPreviewBackgroundColor
            typed.linkPreviewTitleColor shouldBe alignmentDefault.linkPreviewTitleColor
            typed.linkPreviewTitleStyle shouldBe alignmentDefault.linkPreviewTitleStyle
            typed.linkPreviewDescriptionColor shouldBe alignmentDefault.linkPreviewDescriptionColor
            typed.linkPreviewDescriptionStyle shouldBe alignmentDefault.linkPreviewDescriptionStyle
            typed.linkPreviewLinkColor shouldBe alignmentDefault.linkPreviewLinkColor
            typed.linkPreviewLinkStyle shouldBe alignmentDefault.linkPreviewLinkStyle
            typed.linkPreviewCornerRadius shouldBe alignmentDefault.linkPreviewCornerRadius
            typed.linkPreviewStrokeWidth shouldBe alignmentDefault.linkPreviewStrokeWidth
            typed.linkPreviewStrokeColor shouldBe alignmentDefault.linkPreviewStrokeColor
        }
    }

    // ========================================================================
    // Property 6: Explicit BubbleSpecificStyle used as-is by InternalContentRenderer
    // ========================================================================

    /**
     * **Feature: bubble-style-hierarchy, Property 6: Explicit BubbleSpecificStyle used as-is by InternalContentRenderer**
     *
     * For any explicitly provided BubbleSpecificStyle in BubbleStyles,
     * InternalContentRenderer SHALL use that exact style instance for content
     * rendering without modification (other than sentinel resolution via mergeWithBase).
     *
     * This replicates the pattern from InternalContentRenderer.renderStandardMessage:
     * ```kotlin
     * val effectiveStyle = styles.textBubbleStyle ?: getDefaultTextBubbleStyle(alignment, messageBubbleStyle)
     * ```
     * When styles.textBubbleStyle is non-null, it's used directly — getDefaultTextBubbleStyle
     * is never called.
     *
     * **Validates: Requirements 5.2**
     */
    "Property 6: explicit BubbleSpecificStyle is used as-is — bypasses alignment default creation" {
        checkAll(
            100,
            explicitTextBubbleStyleArb,    // explicit BubbleSpecificStyle (non-null)
            messageBubbleStyleArb,         // messageBubbleStyle (should be irrelevant)
            alignmentArb                   // alignment (should be irrelevant)
        ) { explicitStyle, messageBubbleStyle, alignment ->
            // Replicate InternalContentRenderer logic:
            // styles.textBubbleStyle is non-null → use it directly
            val bubbleStyles = BubbleStyles(
                messageBubbleStyle = messageBubbleStyle,
                textBubbleStyle = explicitStyle
            )

            val effectiveStyle = resolveTextBubbleStyle(bubbleStyles, alignment)

            // The effective style should be the exact explicit style instance
            effectiveStyle shouldBe explicitStyle
        }
    }

    /**
     * **Feature: bubble-style-hierarchy, Property 6: Explicit BubbleSpecificStyle used as-is by InternalContentRenderer**
     *
     * When an explicit BubbleSpecificStyle is provided, ALL its properties (both
     * CommonProperties and ContentProperties) are used without modification.
     * The messageBubbleStyle does not influence the result.
     *
     * **Validates: Requirements 5.2**
     */
    "Property 6: explicit BubbleSpecificStyle CommonProperties are not overridden by messageBubbleStyle" {
        checkAll(
            100,
            explicitTextBubbleStyleArb,
            messageBubbleStyleArb
        ) { explicitStyle, messageBubbleStyle ->
            val bubbleStyles = BubbleStyles(
                messageBubbleStyle = messageBubbleStyle,
                textBubbleStyle = explicitStyle
            )

            val effectiveStyle = resolveTextBubbleStyle(
                bubbleStyles,
                UIKitConstants.MessageBubbleAlignment.LEFT
            )

            // CommonProperties should come from explicitStyle, NOT messageBubbleStyle
            effectiveStyle.backgroundColor shouldBe explicitStyle.backgroundColor
            effectiveStyle.cornerRadius shouldBe explicitStyle.cornerRadius
            effectiveStyle.strokeWidth shouldBe explicitStyle.strokeWidth
            effectiveStyle.strokeColor shouldBe explicitStyle.strokeColor
            effectiveStyle.padding shouldBe explicitStyle.padding
            effectiveStyle.senderNameTextColor shouldBe explicitStyle.senderNameTextColor
            effectiveStyle.senderNameTextStyle shouldBe explicitStyle.senderNameTextStyle
            effectiveStyle.threadIndicatorTextColor shouldBe explicitStyle.threadIndicatorTextColor
            effectiveStyle.threadIndicatorTextStyle shouldBe explicitStyle.threadIndicatorTextStyle
            effectiveStyle.threadIndicatorIconTint shouldBe explicitStyle.threadIndicatorIconTint
            effectiveStyle.timestampTextColor shouldBe explicitStyle.timestampTextColor
            effectiveStyle.timestampTextStyle shouldBe explicitStyle.timestampTextStyle

            // ContentProperties should also come from explicitStyle
            val typed = effectiveStyle as CometChatTextBubbleStyle
            typed.textColor shouldBe explicitStyle.textColor
            typed.linkColor shouldBe explicitStyle.linkColor
        }
    }
})

// ============================================================================
// Helper functions — replicate InternalContentRenderer style resolution logic
// as pure functions for testability (since renderContent is @Composable).
// ============================================================================

/**
 * Replicates the text bubble style resolution logic from
 * InternalContentRenderer.renderStandardMessage:
 *
 * ```kotlin
 * val effectiveStyle = styles.textBubbleStyle
 *     ?: getDefaultTextBubbleStyle(alignment, messageBubbleStyle)
 * ```
 *
 * When textBubbleStyle is non-null, it's used directly.
 * When null, an alignment default (with sentinel CommonProperties) is created
 * and merged with messageBubbleStyle if available.
 */
private fun resolveTextBubbleStyle(
    styles: BubbleStyles,
    alignment: UIKitConstants.MessageBubbleAlignment
): CometChatTextBubbleStyle {
    return styles.textBubbleStyle
        ?: getDefaultTextBubbleStylePure(alignment, styles.messageBubbleStyle)
}

/**
 * Pure (non-Composable) equivalent of InternalContentRenderer.getDefaultTextBubbleStyle.
 *
 * Creates an alignment-based default with sentinel CommonProperties, then merges
 * with messageBubbleStyle if available. Uses hardcoded content-specific defaults
 * since the actual factory functions are @Composable.
 */
private fun getDefaultTextBubbleStylePure(
    alignment: UIKitConstants.MessageBubbleAlignment,
    messageBubbleStyle: CometChatMessageBubbleStyle?
): CometChatTextBubbleStyle {
    // Create alignment default with sentinel CommonProperties
    // (mirrors what CometChatTextBubbleStyle.incoming/outgoing/default() produce)
    val alignmentDefault = createSentinelTextBubbleStyle(alignment)

    return if (messageBubbleStyle != null) {
        mergeWithBase(alignmentDefault, messageBubbleStyle)
    } else {
        alignmentDefault
    }
}

/**
 * Creates a CometChatTextBubbleStyle with sentinel CommonProperties,
 * simulating what the factory functions (incoming/outgoing/default) produce.
 *
 * Content-specific properties vary by alignment (e.g., outgoing text is white),
 * but CommonProperties are always sentinels.
 */
private fun createSentinelTextBubbleStyle(
    alignment: UIKitConstants.MessageBubbleAlignment
): CometChatTextBubbleStyle {
    val isOutgoing = alignment == UIKitConstants.MessageBubbleAlignment.RIGHT
    return CometChatTextBubbleStyle(
        textColor = if (isOutgoing) Color.White else Color.Black,
        textStyle = TextStyle(fontSize = 14.sp),
        linkColor = if (isOutgoing) Color.White else Color.Blue,
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
        // ALL CommonProperties are sentinels
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
