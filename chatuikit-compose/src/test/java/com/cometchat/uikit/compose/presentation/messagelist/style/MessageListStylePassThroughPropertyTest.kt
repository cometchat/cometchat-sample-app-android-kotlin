package com.cometchat.uikit.compose.presentation.messagelist.style

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatMessageBubbleStyle
import io.kotest.core.spec.style.StringSpec
import io.kotest.property.Arb
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.float
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.map
import io.kotest.property.checkAll

/**
 * Property-based tests for incoming/outgoing bubble style factory pass-through
 * on [CometChatMessageListStyle].
 *
 * **Feature: messagelist-bubble-style-propagation, Property 1 (full):
 * Factory function style pass-through**
 *
 * For any [CometChatMessageBubbleStyle] instances passed as
 * `incomingMessageBubbleStyle` and `outgoingMessageBubbleStyle` to the
 * constructor (mirroring `CometChatMessageListStyle.default()`), the
 * constructed object's corresponding properties SHALL be referentially
 * identical (`===`) to the input instances.
 *
 * **Validates: Requirements 1.3**
 */
class MessageListStylePassThroughPropertyTest : StringSpec({

    // ========================================================================
    // Arbitrary generators
    // ========================================================================

    val colorArb = Arb.bind(
        Arb.int(1, 255),
        Arb.int(0, 255),
        Arb.int(0, 255),
        Arb.int(1, 255)
    ) { r, g, b, a -> Color(r / 255f, g / 255f, b / 255f, a / 255f) }

    val textStyleArb = Arb.float(10f, 24f).map { TextStyle(fontSize = it.sp) }

    /**
     * Generates random [CometChatMessageBubbleStyle] instances with fully
     * randomized core properties. Each generated instance is a unique object.
     */
    val bubbleStyleArb: Arb<CometChatMessageBubbleStyle> = Arb.bind(
        colorArb,     // backgroundColor
        colorArb,     // strokeColor
        colorArb,     // senderNameTextColor
        colorArb,     // threadIndicatorTextColor
        colorArb,     // threadIndicatorIconTint
        colorArb,     // timestampTextColor
        textStyleArb, // senderNameTextStyle
        textStyleArb, // threadIndicatorTextStyle
        textStyleArb  // timestampTextStyle
    ) { bg, sc, snc, tic, tii, tsc, sns, tis, tss ->
        CometChatMessageBubbleStyle(
            backgroundColor = bg,
            cornerRadius = 12.dp,
            strokeWidth = 0.dp,
            strokeColor = sc,
            padding = PaddingValues(0.dp),
            senderNameTextColor = snc,
            senderNameTextStyle = sns,
            threadIndicatorTextColor = tic,
            threadIndicatorTextStyle = tis,
            threadIndicatorIconTint = tii,
            timestampTextColor = tsc,
            timestampTextStyle = tss
        )
    }

    // ========================================================================
    // Property 1 (full): incomingMessageBubbleStyle pass-through
    // ========================================================================

    /**
     * **Feature: messagelist-bubble-style-propagation, Property 1:
     * Factory function style pass-through**
     *
     * For any random [CometChatMessageBubbleStyle] instance passed as
     * `incomingMessageBubbleStyle`, the constructed
     * [CometChatMessageListStyle]'s `incomingMessageBubbleStyle` SHALL be
     * referentially identical (`===`) to the input.
     *
     * **Validates: Requirements 1.3**
     */
    "Property 1: default() preserves incomingMessageBubbleStyle referential identity" {
        checkAll(100, bubbleStyleArb) { incomingStyle ->
            val result = buildDefaultMessageListStyle(
                incomingMessageBubbleStyle = incomingStyle
            )
            assert(result.incomingMessageBubbleStyle === incomingStyle) {
                "default() did not preserve referential identity of incomingMessageBubbleStyle"
            }
        }
    }

    // ========================================================================
    // Property 1 (full): outgoingMessageBubbleStyle pass-through
    // ========================================================================

    /**
     * **Feature: messagelist-bubble-style-propagation, Property 1:
     * Factory function style pass-through**
     *
     * For any random [CometChatMessageBubbleStyle] instance passed as
     * `outgoingMessageBubbleStyle`, the constructed
     * [CometChatMessageListStyle]'s `outgoingMessageBubbleStyle` SHALL be
     * referentially identical (`===`) to the input.
     *
     * **Validates: Requirements 1.3**
     */
    "Property 1: default() preserves outgoingMessageBubbleStyle referential identity" {
        checkAll(100, bubbleStyleArb) { outgoingStyle ->
            val result = buildDefaultMessageListStyle(
                outgoingMessageBubbleStyle = outgoingStyle
            )
            assert(result.outgoingMessageBubbleStyle === outgoingStyle) {
                "default() did not preserve referential identity of outgoingMessageBubbleStyle"
            }
        }
    }

    // ========================================================================
    // Property 1 (full): both styles simultaneously
    // ========================================================================

    /**
     * **Feature: messagelist-bubble-style-propagation, Property 1:
     * Factory function style pass-through**
     *
     * For any pair of random [CometChatMessageBubbleStyle] instances passed
     * simultaneously as `incomingMessageBubbleStyle` and
     * `outgoingMessageBubbleStyle`, both properties on the constructed
     * [CometChatMessageListStyle] SHALL be referentially identical (`===`)
     * to their respective inputs.
     *
     * **Validates: Requirements 1.3**
     */
    "Property 1: default() preserves both incoming and outgoing referential identity simultaneously" {
        checkAll(100, bubbleStyleArb, bubbleStyleArb) { incomingStyle, outgoingStyle ->
            val result = buildDefaultMessageListStyle(
                incomingMessageBubbleStyle = incomingStyle,
                outgoingMessageBubbleStyle = outgoingStyle
            )
            assert(result.incomingMessageBubbleStyle === incomingStyle) {
                "default() did not preserve referential identity of incomingMessageBubbleStyle"
            }
            assert(result.outgoingMessageBubbleStyle === outgoingStyle) {
                "default() did not preserve referential identity of outgoingMessageBubbleStyle"
            }
        }
    }
})

// ============================================================================
// Helper function — constructs CometChatMessageListStyle mirroring the
// default() factory. Direct constructor call since the real factory is
// @Composable.
// ============================================================================

/**
 * Mirrors `CometChatMessageListStyle.default(...)` with hardcoded theme-like
 * defaults for all required non-nullable properties. Only
 * `incomingMessageBubbleStyle` and `outgoingMessageBubbleStyle` are
 * parameterized since they are the properties under test.
 */
private fun buildDefaultMessageListStyle(
    incomingMessageBubbleStyle: CometChatMessageBubbleStyle? = null,
    outgoingMessageBubbleStyle: CometChatMessageBubbleStyle? = null
): CometChatMessageListStyle {
    val defaultTextStyle = TextStyle(fontSize = 14.sp)
    val defaultSmallTextStyle = TextStyle(fontSize = 12.sp)
    val defaultBubbleStyle = CometChatMessageBubbleStyle(
        backgroundColor = Color(0xFFF5F5F5),
        cornerRadius = 12.dp,
        strokeWidth = 0.dp,
        strokeColor = Color.Transparent,
        padding = PaddingValues(0.dp),
        senderNameTextColor = Color.Gray,
        senderNameTextStyle = defaultSmallTextStyle,
        threadIndicatorTextColor = Color.Gray,
        threadIndicatorTextStyle = defaultSmallTextStyle,
        threadIndicatorIconTint = Color.Gray,
        timestampTextColor = Color.Gray,
        timestampTextStyle = TextStyle(fontSize = 11.sp)
    )

    return CometChatMessageListStyle(
        // Container
        backgroundColor = Color(0xFFF5F5F5),
        cornerRadius = 0.dp,
        strokeWidth = 0.dp,
        strokeColor = Color.Transparent,
        // Error state
        errorStateTitleTextColor = Color.Black,
        errorStateTitleTextStyle = defaultTextStyle,
        errorStateSubtitleTextColor = Color.Gray,
        errorStateSubtitleTextStyle = defaultTextStyle,
        // Empty chat greeting
        emptyChatGreetingTitleTextColor = Color.Black,
        emptyChatGreetingTitleTextStyle = defaultTextStyle,
        emptyChatGreetingSubtitleTextColor = Color.Gray,
        emptyChatGreetingSubtitleTextStyle = defaultTextStyle,
        // Date separator
        dateSeparatorTextColor = Color.Gray,
        dateSeparatorTextStyle = defaultSmallTextStyle,
        dateSeparatorBackgroundColor = Color(0xFFF5F5F5),
        dateSeparatorCornerRadius = 8.dp,
        dateSeparatorStrokeWidth = 1.dp,
        dateSeparatorStrokeColor = Color.Transparent,
        // New message indicator
        newMessageIndicatorBackgroundColor = Color.White,
        newMessageIndicatorTextColor = Color.White,
        newMessageIndicatorTextStyle = defaultSmallTextStyle,
        newMessageIndicatorCornerRadius = 28.dp,
        newMessageIndicatorElevation = 8.dp,
        newMessageIndicatorStrokeColor = Color.LightGray,
        newMessageIndicatorStrokeWidth = 1.dp,
        newMessageIndicatorIconTint = Color.Gray,
        newMessageIndicatorIconSize = 24.dp,
        newMessageIndicatorPadding = 12.dp,
        // New messages separator
        newMessagesSeparatorTextColor = Color.Red,
        newMessagesSeparatorTextStyle = defaultSmallTextStyle,
        newMessagesSeparatorLineColor = Color.Red,
        newMessagesSeparatorLineHeight = 1.dp,
        newMessagesSeparatorVerticalPadding = 8.dp,
        // Message bubble style
        messageBubbleStyle = defaultBubbleStyle,
        // Properties under test
        incomingMessageBubbleStyle = incomingMessageBubbleStyle,
        outgoingMessageBubbleStyle = outgoingMessageBubbleStyle
    )
}
