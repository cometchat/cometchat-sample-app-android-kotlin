package com.cometchat.uikit.compose.presentation.shared.messagebubble.style

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cometchat.uikit.compose.presentation.shared.messagepreview.CometChatMessagePreviewStyle
import io.kotest.core.spec.style.StringSpec
import io.kotest.property.Arb
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.float
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.map
import io.kotest.property.checkAll

/**
 * Property-based tests for messagePreviewStyle factory pass-through on
 * [CometChatMessageBubbleStyle].
 *
 * **Feature: messagelist-bubble-style-propagation, Property 1 (partial):
 * Factory function style pass-through for messagePreviewStyle**
 *
 * For any [CometChatMessagePreviewStyle] instance passed to the constructor
 * (mirroring `default()`, `incoming()`, and `outgoing()` factory functions),
 * the constructed object's `messagePreviewStyle` property SHALL be
 * referentially identical (`===`) to the input instance.
 *
 * **Validates: Requirements 4.2**
 */
class MessagePreviewStylePassThroughPropertyTest : StringSpec({

    // ========================================================================
    // Arbitrary generators
    // ========================================================================

    val colorArb: Arb<Color> = Arb.bind(
        Arb.int(1, 255),
        Arb.int(0, 255),
        Arb.int(0, 255),
        Arb.int(1, 255)
    ) { r, g, b, a -> Color(r / 255f, g / 255f, b / 255f, a / 255f) }

    val textStyleArb: Arb<TextStyle> = Arb.float(10f, 24f).map { TextStyle(fontSize = it.sp) }

    val dpArb: Arb<Dp> = Arb.float(0f, 24f).map { it.dp }

    /**
     * Generates random [CometChatMessagePreviewStyle] instances with fully
     * randomized properties. Each generated instance is a unique object.
     */
    val messagePreviewStyleArb: Arb<CometChatMessagePreviewStyle> = Arb.bind(
        colorArb,     // backgroundColor
        dpArb,        // strokeWidth
        dpArb,        // cornerRadius
        colorArb,     // strokeColor
        colorArb,     // separatorColor
        colorArb,     // titleTextColor
        textStyleArb, // titleTextStyle
        colorArb,     // subtitleTextColor
        textStyleArb, // subtitleTextStyle
        colorArb      // closeIconTint
    ) { bg, sw, cr, sc, sep, ttc, tts, stc, sts, cit ->
        CometChatMessagePreviewStyle(
            backgroundColor = bg,
            strokeWidth = sw,
            cornerRadius = cr,
            strokeColor = sc,
            separatorColor = sep,
            titleTextColor = ttc,
            titleTextStyle = tts,
            subtitleTextColor = stc,
            subtitleTextStyle = sts,
            closeIconTint = cit,
            messageIconTint = Color(0.5f, 0.5f, 0.5f, 1f)
        )
    }

    // ========================================================================
    // Property 1 (partial): messagePreviewStyle pass-through via default()
    // ========================================================================

    /**
     * **Feature: messagelist-bubble-style-propagation, Property 1 (partial):
     * Factory function style pass-through for messagePreviewStyle**
     *
     * For any random [CometChatMessagePreviewStyle] instance passed to
     * `default()`, the constructed object's `messagePreviewStyle` SHALL be
     * referentially identical (`===`) to the input.
     *
     * **Validates: Requirements 4.2**
     */
    "Property 1 (partial): default() preserves messagePreviewStyle referential identity" {
        checkAll(100, messagePreviewStyleArb) { previewStyle ->
            val result = buildDefault(messagePreviewStyle = previewStyle)
            assert(result.messagePreviewStyle === previewStyle) {
                "default() did not preserve referential identity of messagePreviewStyle"
            }
        }
    }

    // ========================================================================
    // Property 1 (partial): messagePreviewStyle pass-through via incoming()
    // ========================================================================

    /**
     * **Feature: messagelist-bubble-style-propagation, Property 1 (partial):
     * Factory function style pass-through for messagePreviewStyle**
     *
     * For any random [CometChatMessagePreviewStyle] instance passed to
     * `incoming()`, the constructed object's `messagePreviewStyle` SHALL be
     * referentially identical (`===`) to the input.
     *
     * **Validates: Requirements 4.2**
     */
    "Property 1 (partial): incoming() preserves messagePreviewStyle referential identity" {
        checkAll(100, messagePreviewStyleArb) { previewStyle ->
            val result = buildIncoming(messagePreviewStyle = previewStyle)
            assert(result.messagePreviewStyle === previewStyle) {
                "incoming() did not preserve referential identity of messagePreviewStyle"
            }
        }
    }

    // ========================================================================
    // Property 1 (partial): messagePreviewStyle pass-through via outgoing()
    // ========================================================================

    /**
     * **Feature: messagelist-bubble-style-propagation, Property 1 (partial):
     * Factory function style pass-through for messagePreviewStyle**
     *
     * For any random [CometChatMessagePreviewStyle] instance passed to
     * `outgoing()`, the constructed object's `messagePreviewStyle` SHALL be
     * referentially identical (`===`) to the input.
     *
     * **Validates: Requirements 4.2**
     */
    "Property 1 (partial): outgoing() preserves messagePreviewStyle referential identity" {
        checkAll(100, messagePreviewStyleArb) { previewStyle ->
            val result = buildOutgoing(messagePreviewStyle = previewStyle)
            assert(result.messagePreviewStyle === previewStyle) {
                "outgoing() did not preserve referential identity of messagePreviewStyle"
            }
        }
    }
})

// ============================================================================
// Helper functions — construct CometChatMessageBubbleStyle mirroring factory
// functions. Direct constructor calls since the real factories are @Composable.
// ============================================================================

/**
 * Mirrors `CometChatMessageBubbleStyle.default(messagePreviewStyle = ...)`.
 */
private fun buildDefault(
    messagePreviewStyle: CometChatMessagePreviewStyle?
): CometChatMessageBubbleStyle = CometChatMessageBubbleStyle(
    backgroundColor = Color(0xFFF5F5F5),
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
    timestampTextStyle = TextStyle(fontSize = 11.sp),
    messagePreviewStyle = messagePreviewStyle
)

/**
 * Mirrors `CometChatMessageBubbleStyle.incoming(messagePreviewStyle = ...)`.
 */
private fun buildIncoming(
    messagePreviewStyle: CometChatMessagePreviewStyle?
): CometChatMessageBubbleStyle = CometChatMessageBubbleStyle(
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
    timestampTextStyle = TextStyle(fontSize = 11.sp),
    messagePreviewStyle = messagePreviewStyle
)

/**
 * Mirrors `CometChatMessageBubbleStyle.outgoing(messagePreviewStyle = ...)`.
 */
private fun buildOutgoing(
    messagePreviewStyle: CometChatMessagePreviewStyle?
): CometChatMessageBubbleStyle = CometChatMessageBubbleStyle(
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
    timestampTextColor = Color.White.copy(alpha = 0.8f),
    timestampTextStyle = TextStyle(fontSize = 11.sp),
    messagePreviewStyle = messagePreviewStyle
)
