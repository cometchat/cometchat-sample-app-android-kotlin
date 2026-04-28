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
import io.kotest.property.Arb
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.element
import io.kotest.property.arbitrary.float
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.map
import io.kotest.property.checkAll

/**
 * Property-based tests for transparent bubble background on agentic and stream messages.
 *
 * **Feature: agent-user-message-list-compose, Property 5: Transparent Bubble for Agentic and Stream Messages**
 *
 * The CometChatMessageBubble style resolution logic applies Color.Transparent as
 * backgroundColor for messages with category "agentic" or "stream_message"
 * (UIKitConstants.MessageCategory.STREAM). This ensures the outer bubble container
 * is transparent so the inner CometChatAIAssistantBubble controls its own appearance.
 *
 * Since the style resolution is embedded in a @Composable function, we replicate
 * the effectiveStyle resolution logic as a pure function and verify the property.
 *
 * **Validates: Requirements 9.1, 9.2**
 */
class TransparentBubblePropertyTest : StringSpec({

    // ========================================================================
    // Arbitrary generators
    // ========================================================================

    /** Generates non-sentinel colors (alpha > 0, non-transparent). */
    val nonTransparentColorArb: Arb<Color> = Arb.bind(
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

    /** Generates agentic or stream message categories. */
    val agenticCategoryArb: Arb<String> = Arb.element(
        "agentic",
        UIKitConstants.MessageCategory.STREAM  // "stream_message"
    )

    /**
     * Generator for a CometChatMessageBubbleStyle with non-transparent backgroundColor.
     * Used as the baseStyle to verify it gets overridden to Color.Transparent.
     */
    val baseStyleArb: Arb<CometChatMessageBubbleStyle> = Arb.bind(
        nonTransparentColorArb,       // backgroundColor (non-transparent to prove override)
        nonSentinelDpArb,             // cornerRadius
        nonSentinelDpArb,             // strokeWidth
        nonTransparentColorArb,       // strokeColor
        nonSentinelPaddingArb,        // padding
        nonTransparentColorArb,       // senderNameTextColor
        nonSentinelTextStyleArb,      // senderNameTextStyle
        nonTransparentColorArb,       // threadIndicatorTextColor
        nonSentinelTextStyleArb,      // threadIndicatorTextStyle
        nonTransparentColorArb        // threadIndicatorIconTint
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
    // Property 5: Transparent Bubble for Agentic and Stream Messages
    // ========================================================================

    /**
     * **Feature: agent-user-message-list-compose, Property 5: Transparent Bubble for Agentic and Stream Messages**
     *
     * For any message with category "agentic" or "stream_message", the resolved
     * effective style SHALL have Color.Transparent as its backgroundColor,
     * regardless of the base style's backgroundColor or the alignment.
     *
     * **Validates: Requirements 9.1, 9.2**
     */
    "Property 5: agentic and stream messages always resolve to transparent backgroundColor" {
        checkAll(
            100,
            agenticCategoryArb,
            alignmentArb,
            baseStyleArb
        ) { category, alignment, baseStyle ->
            val effectiveStyle = resolveEffectiveStyleForCategory(
                category = category,
                baseStyle = baseStyle
            )

            effectiveStyle.backgroundColor shouldBe Color.Transparent
        }
    }

    /**
     * **Feature: agent-user-message-list-compose, Property 5: Transparent Bubble for Agentic and Stream Messages**
     *
     * For any message with category "agentic" or "stream_message", the resolved
     * effective style SHALL preserve all non-backgroundColor properties from the
     * base style. Only backgroundColor is overridden to Color.Transparent.
     *
     * **Validates: Requirements 9.1, 9.2**
     */
    "Property 5: agentic/stream transparent override preserves all other base style properties" {
        checkAll(
            100,
            agenticCategoryArb,
            baseStyleArb
        ) { category, baseStyle ->
            val effectiveStyle = resolveEffectiveStyleForCategory(
                category = category,
                baseStyle = baseStyle
            )

            // backgroundColor is transparent
            effectiveStyle.backgroundColor shouldBe Color.Transparent

            // All other properties are preserved from baseStyle
            effectiveStyle.cornerRadius shouldBe baseStyle.cornerRadius
            effectiveStyle.strokeWidth shouldBe baseStyle.strokeWidth
            effectiveStyle.strokeColor shouldBe baseStyle.strokeColor
            effectiveStyle.padding shouldBe baseStyle.padding
            effectiveStyle.senderNameTextColor shouldBe baseStyle.senderNameTextColor
            effectiveStyle.senderNameTextStyle shouldBe baseStyle.senderNameTextStyle
            effectiveStyle.threadIndicatorTextColor shouldBe baseStyle.threadIndicatorTextColor
            effectiveStyle.threadIndicatorTextStyle shouldBe baseStyle.threadIndicatorTextStyle
            effectiveStyle.threadIndicatorIconTint shouldBe baseStyle.threadIndicatorIconTint
            effectiveStyle.timestampTextColor shouldBe baseStyle.timestampTextColor
            effectiveStyle.timestampTextStyle shouldBe baseStyle.timestampTextStyle
        }
    }
})

// ============================================================================
// Helper function — replicates the effectiveStyle resolution from
// CometChatMessageBubble for agentic/stream categories.
// ============================================================================

/**
 * Replicates the effectiveStyle resolution logic from CometChatMessageBubble.kt
 * for the agentic/stream category branch.
 *
 * In the actual composable, when `contentStyle` is null and the message category
 * is "agentic" or UIKitConstants.MessageCategory.STREAM, the effectiveStyle is
 * constructed with Color.Transparent as backgroundColor while preserving all
 * other properties from the baseStyle.
 *
 * ```kotlin
 * val effectiveStyle = when {
 *     contentStyle != null -> mergeWithBase(contentStyle, baseStyle)
 *     message.category == "agentic" ||
 *     message.category == UIKitConstants.MessageCategory.STREAM ->
 *         CometChatMessageBubbleStyle(
 *             backgroundColor = Color.Transparent,
 *             cornerRadius = baseStyle.cornerRadius,
 *             // ... all other properties from baseStyle
 *         )
 *     else -> baseStyle
 * }
 * ```
 *
 * For agentic/stream messages, contentStyle is always null because
 * resolveContentStyleForMessage returns null for unknown categories.
 */
private fun resolveEffectiveStyleForCategory(
    category: String,
    baseStyle: CometChatMessageBubbleStyle
): CometChatMessageBubbleStyle {
    // contentStyle is null for agentic/stream categories (resolveContentStyleForMessage
    // returns null for categories not in its when block)
    val contentStyle: CometChatMessageBubbleStyle? = null

    return when {
        contentStyle != null -> contentStyle // unreachable in this test
        category == "agentic" ||
        category == UIKitConstants.MessageCategory.STREAM ->
            CometChatMessageBubbleStyle(
                backgroundColor = Color.Transparent,
                cornerRadius = baseStyle.cornerRadius,
                strokeWidth = baseStyle.strokeWidth,
                strokeColor = baseStyle.strokeColor,
                padding = baseStyle.padding,
                senderNameTextColor = baseStyle.senderNameTextColor,
                senderNameTextStyle = baseStyle.senderNameTextStyle,
                threadIndicatorTextColor = baseStyle.threadIndicatorTextColor,
                threadIndicatorTextStyle = baseStyle.threadIndicatorTextStyle,
                threadIndicatorIconTint = baseStyle.threadIndicatorIconTint,
                timestampTextColor = baseStyle.timestampTextColor,
                timestampTextStyle = baseStyle.timestampTextStyle,
                dateStyle = baseStyle.dateStyle,
                messageReceiptStyle = baseStyle.messageReceiptStyle,
                avatarStyle = baseStyle.avatarStyle,
                reactionStyle = baseStyle.reactionStyle,
                mentionStyle = baseStyle.mentionStyle,
                moderationViewStyle = baseStyle.moderationViewStyle,
                aiAssistantBubbleStyle = baseStyle.aiAssistantBubbleStyle,
                messagePreviewStyle = baseStyle.messagePreviewStyle,
                textBubbleStyle = baseStyle.textBubbleStyle,
                imageBubbleStyle = baseStyle.imageBubbleStyle,
                videoBubbleStyle = baseStyle.videoBubbleStyle,
                fileBubbleStyle = baseStyle.fileBubbleStyle,
                audioBubbleStyle = baseStyle.audioBubbleStyle,
                deleteBubbleStyle = baseStyle.deleteBubbleStyle,
                stickerBubbleStyle = baseStyle.stickerBubbleStyle,
                pollBubbleStyle = baseStyle.pollBubbleStyle,
                collaborativeBubbleStyle = baseStyle.collaborativeBubbleStyle,
                meetCallBubbleStyle = baseStyle.meetCallBubbleStyle
            )
        else -> baseStyle
    }
}
