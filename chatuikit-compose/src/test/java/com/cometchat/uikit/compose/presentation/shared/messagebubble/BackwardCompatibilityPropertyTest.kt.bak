package com.cometchat.uikit.compose.presentation.shared.messagebubble

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatMessageBubbleStyle
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatTextBubbleStyle
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatImageBubbleStyle
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatAudioBubbleStyle
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatVideoBubbleStyle
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatFileBubbleStyle
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatDeleteBubbleStyle
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.UNSET_COLOR
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.UNSET_DP
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.UNSET_PADDING
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.UNSET_TEXT_STYLE
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.mergeWithBase
import com.cometchat.uikit.core.constants.UIKitConstants
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.element
import io.kotest.property.arbitrary.float
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.map
import io.kotest.property.checkAll

/**
 * Property-based tests for backward compatibility of the bubble style hierarchy.
 *
 * **Feature: bubble-style-hierarchy, Property 5: Backward compatibility with all defaults**
 *
 * When messageBubbleStyle uses the default value and no BubbleSpecificStyle is explicitly
 * set, the resolved effectiveStyle SHALL be equivalent to the current alignment-based
 * default for that bubble type.
 *
 * The backward compatibility guarantee relies on:
 * 1. Default messageBubbleStyle has theme-based CommonProperty values
 * 2. Factory functions (incoming/outgoing/default) produce sentinel CommonProperties
 * 3. mergeWithBase fills sentinels from the default messageBubbleStyle
 * 4. Result: CommonProperties match what the old hardcoded alignment defaults produced
 *
 * Since factory functions are @Composable, we test with pure functions that simulate
 * the same pattern: create a "default" messageBubbleStyle with known values, create
 * alignment defaults with sentinel CommonProperties, merge, and verify the result.
 *
 * **Validates: Requirements 8.1, 8.2**
 */
class BackwardCompatibilityPropertyTest : StringSpec({

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
     * Generator for a "default" CometChatMessageBubbleStyle with all non-sentinel properties.
     * Simulates what CometChatMessageBubbleStyle.default() produces at runtime
     * (theme-based CommonProperty values).
     */
    val defaultMessageBubbleStyleArb: Arb<CometChatMessageBubbleStyle> = Arb.bind(
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
    // Property 5: Backward compatibility with all defaults
    // ========================================================================

    /**
     * **Feature: bubble-style-hierarchy, Property 5: Backward compatibility with all defaults**
     *
     * For any default messageBubbleStyle, when a text bubble alignment default (with
     * sentinel CommonProperties) is merged via mergeWithBase, the resulting
     * CommonProperties SHALL equal the default messageBubbleStyle's values.
     *
     * This proves backward compatibility because:
     * - Old code: CometChatTextBubbleStyle.incoming() had hardcoded theme CommonProperties
     * - New code: CometChatTextBubbleStyle.incoming() has sentinels, merged with default
     *   messageBubbleStyle (which has the same theme values)
     * - Result: identical CommonProperties
     *
     * **Validates: Requirements 8.1, 8.2**
     */
    "Property 5: text bubble — sentinel alignment default merged with default messageBubbleStyle produces equivalent CommonProperties" {
        checkAll(
            100,
            defaultMessageBubbleStyleArb,
            alignmentArb
        ) { defaultMBS, alignment ->
            // Simulate what factory functions produce: sentinel CommonProperties
            val alignmentDefault = createSentinelTextBubbleStyle(alignment)

            // Simulate the backward-compatible merge path:
            // InternalContentRenderer creates alignment default, merges with messageBubbleStyle
            val effectiveStyle = mergeWithBase(alignmentDefault, defaultMBS)

            // CommonProperties should match the default messageBubbleStyle
            effectiveStyle.backgroundColor shouldBe defaultMBS.backgroundColor
            effectiveStyle.cornerRadius shouldBe defaultMBS.cornerRadius
            effectiveStyle.strokeWidth shouldBe defaultMBS.strokeWidth
            effectiveStyle.strokeColor shouldBe defaultMBS.strokeColor
            effectiveStyle.padding shouldBe defaultMBS.padding
            effectiveStyle.senderNameTextColor shouldBe defaultMBS.senderNameTextColor
            effectiveStyle.senderNameTextStyle shouldBe defaultMBS.senderNameTextStyle
            effectiveStyle.threadIndicatorTextColor shouldBe defaultMBS.threadIndicatorTextColor
            effectiveStyle.threadIndicatorTextStyle shouldBe defaultMBS.threadIndicatorTextStyle
            effectiveStyle.threadIndicatorIconTint shouldBe defaultMBS.threadIndicatorIconTint
            effectiveStyle.timestampTextColor shouldBe defaultMBS.timestampTextColor
            effectiveStyle.timestampTextStyle shouldBe defaultMBS.timestampTextStyle

            // ContentProperties should be preserved from the alignment default
            effectiveStyle.shouldBeInstanceOf<CometChatTextBubbleStyle>()
            val typed = effectiveStyle as CometChatTextBubbleStyle
            typed.textColor shouldBe alignmentDefault.textColor
            typed.linkColor shouldBe alignmentDefault.linkColor
        }
    }

    /**
     * **Feature: bubble-style-hierarchy, Property 5: Backward compatibility with all defaults**
     *
     * Same backward compatibility property for image bubbles. Sentinel CommonProperties
     * from the alignment default are filled from the default messageBubbleStyle.
     *
     * **Validates: Requirements 8.1, 8.2**
     */
    "Property 5: image bubble — sentinel alignment default merged with default messageBubbleStyle produces equivalent CommonProperties" {
        checkAll(
            100,
            defaultMessageBubbleStyleArb,
            alignmentArb
        ) { defaultMBS, alignment ->
            val alignmentDefault = createSentinelImageBubbleStyle(alignment)
            val effectiveStyle = mergeWithBase(alignmentDefault, defaultMBS)

            effectiveStyle.backgroundColor shouldBe defaultMBS.backgroundColor
            effectiveStyle.cornerRadius shouldBe defaultMBS.cornerRadius
            effectiveStyle.strokeWidth shouldBe defaultMBS.strokeWidth
            effectiveStyle.strokeColor shouldBe defaultMBS.strokeColor
            effectiveStyle.padding shouldBe defaultMBS.padding
            effectiveStyle.senderNameTextColor shouldBe defaultMBS.senderNameTextColor
            effectiveStyle.senderNameTextStyle shouldBe defaultMBS.senderNameTextStyle
            effectiveStyle.threadIndicatorTextColor shouldBe defaultMBS.threadIndicatorTextColor
            effectiveStyle.threadIndicatorTextStyle shouldBe defaultMBS.threadIndicatorTextStyle
            effectiveStyle.threadIndicatorIconTint shouldBe defaultMBS.threadIndicatorIconTint
            effectiveStyle.timestampTextColor shouldBe defaultMBS.timestampTextColor
            effectiveStyle.timestampTextStyle shouldBe defaultMBS.timestampTextStyle

            effectiveStyle.shouldBeInstanceOf<CometChatImageBubbleStyle>()
        }
    }

    /**
     * **Feature: bubble-style-hierarchy, Property 5: Backward compatibility with all defaults**
     *
     * Same backward compatibility property for audio bubbles.
     *
     * **Validates: Requirements 8.1, 8.2**
     */
    "Property 5: audio bubble — sentinel alignment default merged with default messageBubbleStyle produces equivalent CommonProperties" {
        checkAll(
            100,
            defaultMessageBubbleStyleArb,
            alignmentArb
        ) { defaultMBS, alignment ->
            val alignmentDefault = createSentinelAudioBubbleStyle(alignment)
            val effectiveStyle = mergeWithBase(alignmentDefault, defaultMBS)

            effectiveStyle.backgroundColor shouldBe defaultMBS.backgroundColor
            effectiveStyle.cornerRadius shouldBe defaultMBS.cornerRadius
            effectiveStyle.strokeWidth shouldBe defaultMBS.strokeWidth
            effectiveStyle.strokeColor shouldBe defaultMBS.strokeColor
            effectiveStyle.padding shouldBe defaultMBS.padding
            effectiveStyle.senderNameTextColor shouldBe defaultMBS.senderNameTextColor
            effectiveStyle.senderNameTextStyle shouldBe defaultMBS.senderNameTextStyle
            effectiveStyle.threadIndicatorTextColor shouldBe defaultMBS.threadIndicatorTextColor
            effectiveStyle.threadIndicatorTextStyle shouldBe defaultMBS.threadIndicatorTextStyle
            effectiveStyle.threadIndicatorIconTint shouldBe defaultMBS.threadIndicatorIconTint
            effectiveStyle.timestampTextColor shouldBe defaultMBS.timestampTextColor
            effectiveStyle.timestampTextStyle shouldBe defaultMBS.timestampTextStyle

            effectiveStyle.shouldBeInstanceOf<CometChatAudioBubbleStyle>()
        }
    }

    /**
     * **Feature: bubble-style-hierarchy, Property 5: Backward compatibility with all defaults**
     *
     * End-to-end backward compatibility: simulates the full resolution path when
     * no custom styles are set. The three-tier chain resolves to the alignment default
     * merged with the default messageBubbleStyle, producing identical CommonProperties.
     *
     * **Validates: Requirements 8.1, 8.2**
     */
    "Property 5: full resolution path — no custom styles produces backward-compatible effectiveStyle" {
        checkAll(
            100,
            defaultMessageBubbleStyleArb,
            alignmentArb
        ) { defaultMBS, alignment ->
            // Simulate the full resolution path:
            // 1. No factory style (null)
            // 2. No explicit BubbleSpecificStyle (null in BubbleStyles)
            // 3. InternalContentRenderer creates alignment default with sentinels
            // 4. Merges with messageBubbleStyle from BubbleStyles
            val alignmentDefault = createSentinelTextBubbleStyle(alignment)

            // This is what InternalContentRenderer.getDefaultTextBubbleStyle does:
            val effectiveStyle = mergeWithBase(alignmentDefault, defaultMBS)

            // The outer container also uses messageBubbleStyle (since no contentStyle
            // is explicitly set, the chain falls through to messageBubbleStyle)
            // But InternalContentRenderer's resolved style should have matching CommonProperties
            effectiveStyle.backgroundColor shouldBe defaultMBS.backgroundColor
            effectiveStyle.cornerRadius shouldBe defaultMBS.cornerRadius
            effectiveStyle.strokeWidth shouldBe defaultMBS.strokeWidth
            effectiveStyle.strokeColor shouldBe defaultMBS.strokeColor
            effectiveStyle.padding shouldBe defaultMBS.padding

            // The effective style is still a CometChatTextBubbleStyle with content preserved
            effectiveStyle.shouldBeInstanceOf<CometChatTextBubbleStyle>()
            val typed = effectiveStyle as CometChatTextBubbleStyle
            typed.textColor shouldBe alignmentDefault.textColor
            typed.textStyle shouldBe alignmentDefault.textStyle
            typed.linkColor shouldBe alignmentDefault.linkColor
        }
    }

    /**
     * **Feature: bubble-style-hierarchy, Property 5: Backward compatibility with all defaults**
     *
     * Verifies that for ALL sentinel CommonProperties across multiple bubble types,
     * merging with any default messageBubbleStyle always replaces every sentinel.
     * No sentinel value should leak through to the effective style.
     *
     * **Validates: Requirements 8.1, 8.2**
     */
    "Property 5: no sentinel values leak through after merge with default messageBubbleStyle" {
        checkAll(
            100,
            defaultMessageBubbleStyleArb,
            alignmentArb
        ) { defaultMBS, alignment ->
            val textDefault = createSentinelTextBubbleStyle(alignment)
            val merged = mergeWithBase(textDefault, defaultMBS)

            // No sentinel values should remain in the merged result
            merged.backgroundColor shouldBe defaultMBS.backgroundColor
            (merged.backgroundColor != UNSET_COLOR) shouldBe true
            merged.cornerRadius shouldBe defaultMBS.cornerRadius
            (merged.cornerRadius != UNSET_DP) shouldBe true
            merged.strokeWidth shouldBe defaultMBS.strokeWidth
            (merged.strokeWidth != UNSET_DP) shouldBe true
            merged.strokeColor shouldBe defaultMBS.strokeColor
            (merged.strokeColor != UNSET_COLOR) shouldBe true
            merged.padding shouldBe defaultMBS.padding
            (merged.padding != UNSET_PADDING) shouldBe true
            merged.senderNameTextColor shouldBe defaultMBS.senderNameTextColor
            (merged.senderNameTextColor != UNSET_COLOR) shouldBe true
            merged.senderNameTextStyle shouldBe defaultMBS.senderNameTextStyle
            (merged.senderNameTextStyle != UNSET_TEXT_STYLE) shouldBe true
            merged.threadIndicatorTextColor shouldBe defaultMBS.threadIndicatorTextColor
            (merged.threadIndicatorTextColor != UNSET_COLOR) shouldBe true
            merged.threadIndicatorTextStyle shouldBe defaultMBS.threadIndicatorTextStyle
            (merged.threadIndicatorTextStyle != UNSET_TEXT_STYLE) shouldBe true
            merged.threadIndicatorIconTint shouldBe defaultMBS.threadIndicatorIconTint
            (merged.threadIndicatorIconTint != UNSET_COLOR) shouldBe true
            merged.timestampTextColor shouldBe defaultMBS.timestampTextColor
            (merged.timestampTextColor != UNSET_COLOR) shouldBe true
            merged.timestampTextStyle shouldBe defaultMBS.timestampTextStyle
            (merged.timestampTextStyle != UNSET_TEXT_STYLE) shouldBe true
        }
    }
})

// ============================================================================
// Helper functions — create sentinel-based alignment defaults for each bubble
// type, simulating what the @Composable factory functions produce.
// ============================================================================

/**
 * Creates a CometChatTextBubbleStyle with sentinel CommonProperties,
 * simulating what CometChatTextBubbleStyle.incoming/outgoing/default() produce.
 * Content-specific properties vary by alignment.
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
 * Creates a CometChatImageBubbleStyle with sentinel CommonProperties,
 * simulating what CometChatImageBubbleStyle.incoming/outgoing/default() produce.
 */
private fun createSentinelImageBubbleStyle(
    alignment: UIKitConstants.MessageBubbleAlignment
): CometChatImageBubbleStyle {
    val isOutgoing = alignment == UIKitConstants.MessageBubbleAlignment.RIGHT
    return CometChatImageBubbleStyle(
        imageCornerRadius = 8.dp,
        imageStrokeWidth = 0.dp,
        imageStrokeColor = Color.Transparent,
        captionTextColor = if (isOutgoing) Color.White else Color.Black,
        captionTextStyle = TextStyle(fontSize = 14.sp),
        progressIndicatorColor = Color.Gray,
        gridSpacing = 2.dp,
        maxGridWidth = 240.dp,
        moreOverlayBackgroundColor = Color.Black.copy(alpha = 0.6f),
        moreOverlayTextColor = Color.White,
        moreOverlayTextStyle = TextStyle(fontSize = 18.sp),
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
 * Creates a CometChatAudioBubbleStyle with sentinel CommonProperties,
 * simulating what CometChatAudioBubbleStyle.incoming/outgoing/default() produce.
 */
private fun createSentinelAudioBubbleStyle(
    alignment: UIKitConstants.MessageBubbleAlignment
): CometChatAudioBubbleStyle {
    val isOutgoing = alignment == UIKitConstants.MessageBubbleAlignment.RIGHT
    return CometChatAudioBubbleStyle(
        playIconTint = Color.Blue,
        pauseIconTint = Color.Blue,
        buttonBackgroundColor = Color.White,
        audioWaveColor = if (isOutgoing) Color.White else Color.Blue,
        subtitleTextColor = if (isOutgoing) Color.White else Color.Gray,
        subtitleTextStyle = TextStyle(fontSize = 12.sp),
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
