package com.cometchat.uikit.compose.presentation.shared.messagebubble.style

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.float
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.map
import io.kotest.property.checkAll

/**
 * Property-based tests for the [mergeWithBase] function in StyleMergeUtils.kt.
 *
 * **Feature: bubble-style-hierarchy**
 *
 * Tests validate that mergeWithBase correctly:
 * - Lets non-sentinel BubbleSpecificStyle CommonProperties override messageBubbleStyle (Property 2)
 * - Preserves ContentProperties and concrete type after merging (Property 4)
 * - Fills sentinel CommonProperties from messageBubbleStyle (Property 7)
 * - Retains explicitly set CommonProperties over messageBubbleStyle (Property 8)
 *
 * **Validates: Requirements 2.1, 2.3, 2.4, 7.1, 7.2, 7.3**
 */
class MergeWithBasePropertyTest : StringSpec({

    // ========================================================================
    // Arbitrary generators
    // ========================================================================

    val colorArb: Arb<Color> = Arb.bind(
        Arb.int(0, 255),
        Arb.int(0, 255),
        Arb.int(0, 255),
        Arb.int(0, 255)
    ) { r, g, b, a -> Color(r / 255f, g / 255f, b / 255f, a / 255f) }

    /**
     * Generates non-sentinel colors — excludes [Color.Unspecified].
     * We ensure alpha > 0 and at least one channel differs from Unspecified.
     */
    val nonSentinelColorArb: Arb<Color> = Arb.bind(
        Arb.int(1, 255),
        Arb.int(0, 255),
        Arb.int(0, 255),
        Arb.int(1, 255)
    ) { r, g, b, a -> Color(r / 255f, g / 255f, b / 255f, a / 255f) }


    val dpArb: Arb<Dp> = Arb.float(0f, 24f).map { it.dp }

    /** Generates non-sentinel Dp — excludes [Dp.Unspecified]. */
    val nonSentinelDpArb: Arb<Dp> = Arb.float(0f, 24f).map { it.dp }

    val textStyleArb: Arb<TextStyle> = Arb.float(10f, 24f).map { TextStyle(fontSize = it.sp) }

    /**
     * Generates non-sentinel TextStyle — differs from [TextStyle.Default] by having
     * an explicit fontSize.
     */
    val nonSentinelTextStyleArb: Arb<TextStyle> = Arb.float(10f, 24f).map { TextStyle(fontSize = it.sp) }

    val paddingArb: Arb<PaddingValues> = Arb.float(0f, 16f).map { PaddingValues(it.dp) }

    /** Generates non-sentinel PaddingValues — excludes the UNSET_PADDING sentinel. */
    val nonSentinelPaddingArb: Arb<PaddingValues> = Arb.float(0f, 16f).map { PaddingValues(it.dp) }

    /** Generator for a full CometChatMessageBubbleStyle base instance (all non-sentinel). */
    val baseBubbleStyleArb: Arb<CometChatMessageBubbleStyle> = Arb.bind(
        nonSentinelColorArb, // backgroundColor
        nonSentinelDpArb,    // cornerRadius
        nonSentinelDpArb,    // strokeWidth
        nonSentinelColorArb, // strokeColor
        nonSentinelPaddingArb, // padding
        nonSentinelColorArb, // senderNameTextColor
        nonSentinelTextStyleArb, // senderNameTextStyle
        nonSentinelColorArb, // threadIndicatorTextColor
        nonSentinelTextStyleArb, // threadIndicatorTextStyle
        nonSentinelColorArb  // threadIndicatorIconTint
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
     * Generator for CometChatTextBubbleStyle with NON-sentinel CommonProperties.
     * Content-specific properties are also randomized.
     */
    val textBubbleWithExplicitCommonArb: Arb<CometChatTextBubbleStyle> = Arb.bind(
        nonSentinelColorArb, // backgroundColor
        nonSentinelDpArb,    // cornerRadius
        nonSentinelColorArb, // textColor (content)
        nonSentinelColorArb  // linkColor (content)
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
     * Generator for CometChatTextBubbleStyle with ALL sentinel CommonProperties.
     * Simulates a factory-created style (incoming/outgoing/default) without explicit overrides.
     */
    val textBubbleWithSentinelCommonArb: Arb<CometChatTextBubbleStyle> = Arb.bind(
        nonSentinelColorArb, // textColor (content)
        nonSentinelColorArb  // linkColor (content)
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
            // ALL CommonProperties set to sentinel values
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
    // Property 2: BubbleSpecificStyle CommonProperties override messageBubbleStyle
    // ========================================================================

    /**
     * **Feature: bubble-style-hierarchy, Property 2: BubbleSpecificStyle CommonProperties override messageBubbleStyle**
     *
     * For any BubbleSpecificStyle instance and any messageBubbleStyle instance, when both
     * are provided and the BubbleSpecificStyle has NON-sentinel CommonProperties, the resolved
     * effectiveStyle's CommonProperties SHALL equal the BubbleSpecificStyle's CommonProperties
     * (not the base's).
     *
     * **Validates: Requirements 2.1, 2.3**
     */
    "Property 2: Non-sentinel BubbleSpecificStyle CommonProperties override messageBubbleStyle" {
        checkAll(100, textBubbleWithExplicitCommonArb, baseBubbleStyleArb) { bubbleStyle, base ->
            val result = mergeWithBase(bubbleStyle, base)

            // All CommonProperties should come from bubbleStyle, not base
            result.backgroundColor shouldBe bubbleStyle.backgroundColor
            result.cornerRadius shouldBe bubbleStyle.cornerRadius
            result.strokeWidth shouldBe bubbleStyle.strokeWidth
            result.strokeColor shouldBe bubbleStyle.strokeColor
            result.padding shouldBe bubbleStyle.padding
            result.senderNameTextColor shouldBe bubbleStyle.senderNameTextColor
            result.senderNameTextStyle shouldBe bubbleStyle.senderNameTextStyle
            result.threadIndicatorTextColor shouldBe bubbleStyle.threadIndicatorTextColor
            result.threadIndicatorTextStyle shouldBe bubbleStyle.threadIndicatorTextStyle
            result.threadIndicatorIconTint shouldBe bubbleStyle.threadIndicatorIconTint
            result.timestampTextColor shouldBe bubbleStyle.timestampTextColor
            result.timestampTextStyle shouldBe bubbleStyle.timestampTextStyle
        }
    }

    // ========================================================================
    // Property 4: Merging preserves ContentProperties and type
    // ========================================================================

    /**
     * **Feature: bubble-style-hierarchy, Property 4: Merging preserves ContentProperties and type**
     *
     * For any BubbleSpecificStyle instance (e.g., CometChatTextBubbleStyle), after merging
     * with a messageBubbleStyle via mergeWithBase, the content-specific properties (e.g.,
     * textColor, linkColor for text bubbles) SHALL be identical to the original
     * BubbleSpecificStyle's content-specific properties, and the result SHALL be an instance
     * of the same concrete type.
     *
     * **Validates: Requirements 2.3, 2.4**
     */
    "Property 4: mergeWithBase preserves ContentProperties and concrete type for CometChatTextBubbleStyle" {
        checkAll(100, textBubbleWithExplicitCommonArb, baseBubbleStyleArb) { bubbleStyle, base ->
            val result = mergeWithBase(bubbleStyle, base)

            // Result must be the same concrete type
            result.shouldBeInstanceOf<CometChatTextBubbleStyle>()
            val typed = result as CometChatTextBubbleStyle

            // All content-specific properties must be preserved
            typed.textColor shouldBe bubbleStyle.textColor
            typed.textStyle shouldBe bubbleStyle.textStyle
            typed.linkColor shouldBe bubbleStyle.linkColor
            typed.translatedTextColor shouldBe bubbleStyle.translatedTextColor
            typed.translatedTextStyle shouldBe bubbleStyle.translatedTextStyle
            typed.separatorColor shouldBe bubbleStyle.separatorColor
            typed.linkPreviewBackgroundColor shouldBe bubbleStyle.linkPreviewBackgroundColor
            typed.linkPreviewTitleColor shouldBe bubbleStyle.linkPreviewTitleColor
            typed.linkPreviewTitleStyle shouldBe bubbleStyle.linkPreviewTitleStyle
            typed.linkPreviewDescriptionColor shouldBe bubbleStyle.linkPreviewDescriptionColor
            typed.linkPreviewDescriptionStyle shouldBe bubbleStyle.linkPreviewDescriptionStyle
            typed.linkPreviewLinkColor shouldBe bubbleStyle.linkPreviewLinkColor
            typed.linkPreviewLinkStyle shouldBe bubbleStyle.linkPreviewLinkStyle
            typed.linkPreviewCornerRadius shouldBe bubbleStyle.linkPreviewCornerRadius
            typed.linkPreviewStrokeWidth shouldBe bubbleStyle.linkPreviewStrokeWidth
            typed.linkPreviewStrokeColor shouldBe bubbleStyle.linkPreviewStrokeColor
        }
    }

    "Property 4: mergeWithBase preserves ContentProperties and concrete type for sentinel-based CometChatTextBubbleStyle" {
        checkAll(100, textBubbleWithSentinelCommonArb, baseBubbleStyleArb) { bubbleStyle, base ->
            val result = mergeWithBase(bubbleStyle, base)

            result.shouldBeInstanceOf<CometChatTextBubbleStyle>()
            val typed = result as CometChatTextBubbleStyle

            // Content-specific properties must be preserved even when CommonProperties are sentinels
            typed.textColor shouldBe bubbleStyle.textColor
            typed.textStyle shouldBe bubbleStyle.textStyle
            typed.linkColor shouldBe bubbleStyle.linkColor
            typed.translatedTextColor shouldBe bubbleStyle.translatedTextColor
            typed.translatedTextStyle shouldBe bubbleStyle.translatedTextStyle
            typed.separatorColor shouldBe bubbleStyle.separatorColor
            typed.linkPreviewBackgroundColor shouldBe bubbleStyle.linkPreviewBackgroundColor
            typed.linkPreviewTitleColor shouldBe bubbleStyle.linkPreviewTitleColor
            typed.linkPreviewTitleStyle shouldBe bubbleStyle.linkPreviewTitleStyle
            typed.linkPreviewDescriptionColor shouldBe bubbleStyle.linkPreviewDescriptionColor
            typed.linkPreviewDescriptionStyle shouldBe bubbleStyle.linkPreviewDescriptionStyle
            typed.linkPreviewLinkColor shouldBe bubbleStyle.linkPreviewLinkColor
            typed.linkPreviewLinkStyle shouldBe bubbleStyle.linkPreviewLinkStyle
            typed.linkPreviewCornerRadius shouldBe bubbleStyle.linkPreviewCornerRadius
            typed.linkPreviewStrokeWidth shouldBe bubbleStyle.linkPreviewStrokeWidth
            typed.linkPreviewStrokeColor shouldBe bubbleStyle.linkPreviewStrokeColor
        }
    }

    // ========================================================================
    // Property 7: Sentinel CommonProperties are filled from messageBubbleStyle
    // ========================================================================

    /**
     * **Feature: bubble-style-hierarchy, Property 7: Sentinel CommonProperties are filled from messageBubbleStyle**
     *
     * For any BubbleSpecificStyle created via a factory function (incoming/outgoing/default)
     * without explicit CommonProperty overrides (i.e., all CommonProperties are sentinels),
     * and any messageBubbleStyle, after mergeWithBase, all CommonProperties SHALL equal the
     * messageBubbleStyle's values.
     *
     * **Validates: Requirements 7.1, 7.2**
     */
    "Property 7: Sentinel CommonProperties are filled from messageBubbleStyle" {
        checkAll(100, textBubbleWithSentinelCommonArb, baseBubbleStyleArb) { bubbleStyle, base ->
            val result = mergeWithBase(bubbleStyle, base)

            // All CommonProperties should come from base since bubbleStyle has sentinels
            result.backgroundColor shouldBe base.backgroundColor
            result.cornerRadius shouldBe base.cornerRadius
            result.strokeWidth shouldBe base.strokeWidth
            result.strokeColor shouldBe base.strokeColor
            result.padding shouldBe base.padding
            result.senderNameTextColor shouldBe base.senderNameTextColor
            result.senderNameTextStyle shouldBe base.senderNameTextStyle
            result.threadIndicatorTextColor shouldBe base.threadIndicatorTextColor
            result.threadIndicatorTextStyle shouldBe base.threadIndicatorTextStyle
            result.threadIndicatorIconTint shouldBe base.threadIndicatorIconTint
            result.timestampTextColor shouldBe base.timestampTextColor
            result.timestampTextStyle shouldBe base.timestampTextStyle
        }
    }

    // ========================================================================
    // Property 8: Explicitly set CommonProperties override messageBubbleStyle
    // ========================================================================

    /**
     * **Feature: bubble-style-hierarchy, Property 8: Explicitly set CommonProperties override messageBubbleStyle**
     *
     * For any BubbleSpecificStyle where a developer explicitly sets a CommonProperty
     * (e.g., backgroundColor = Color.Red), and any messageBubbleStyle, after mergeWithBase,
     * that explicitly set CommonProperty SHALL retain the developer's value, not the
     * messageBubbleStyle's value.
     *
     * This test uses a mixed style: some CommonProperties are sentinel, some are explicit.
     *
     * **Validates: Requirements 7.3**
     */
    "Property 8: Explicitly set CommonProperties override messageBubbleStyle while sentinels are filled" {
        val mixedBubbleStyleArb: Arb<CometChatTextBubbleStyle> = Arb.bind(
            nonSentinelColorArb, // explicit backgroundColor
            nonSentinelDpArb,    // explicit cornerRadius
            nonSentinelColorArb, // explicit timestampTextColor
            nonSentinelColorArb  // textColor (content)
        ) { explicitBg, explicitCr, explicitTimestamp, textColor ->
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
                // Explicit (non-sentinel) CommonProperties
                backgroundColor = explicitBg,
                cornerRadius = explicitCr,
                timestampTextColor = explicitTimestamp,
                // Sentinel CommonProperties — should be filled from base
                strokeWidth = UNSET_DP,
                strokeColor = UNSET_COLOR,
                padding = UNSET_PADDING,
                senderNameTextColor = UNSET_COLOR,
                senderNameTextStyle = UNSET_TEXT_STYLE,
                threadIndicatorTextColor = UNSET_COLOR,
                threadIndicatorTextStyle = UNSET_TEXT_STYLE,
                threadIndicatorIconTint = UNSET_COLOR,
                timestampTextStyle = UNSET_TEXT_STYLE
            )
        }

        checkAll(100, mixedBubbleStyleArb, baseBubbleStyleArb) { bubbleStyle, base ->
            val result = mergeWithBase(bubbleStyle, base)

            // Explicitly set properties retain developer's value
            result.backgroundColor shouldBe bubbleStyle.backgroundColor
            result.cornerRadius shouldBe bubbleStyle.cornerRadius
            result.timestampTextColor shouldBe bubbleStyle.timestampTextColor

            // Sentinel properties are filled from base
            result.strokeWidth shouldBe base.strokeWidth
            result.strokeColor shouldBe base.strokeColor
            result.padding shouldBe base.padding
            result.senderNameTextColor shouldBe base.senderNameTextColor
            result.senderNameTextStyle shouldBe base.senderNameTextStyle
            result.threadIndicatorTextColor shouldBe base.threadIndicatorTextColor
            result.threadIndicatorTextStyle shouldBe base.threadIndicatorTextStyle
            result.threadIndicatorIconTint shouldBe base.threadIndicatorIconTint
            result.timestampTextStyle shouldBe base.timestampTextStyle
        }
    }
})
