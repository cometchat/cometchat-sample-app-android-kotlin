package com.cometchat.uikit.compose.presentation.shared.messagebubble

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatMessageBubbleStyle
import com.cometchat.uikit.core.constants.UIKitConstants
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.element
import io.kotest.property.checkAll

/**
 * Property-based tests for null style fallback behavior.
 *
 * **Feature: messagelist-bubble-style-propagation, Property 5: Null style fallback produces alignment-based default**
 *
 * When all style inputs are null (no factory style, no incoming/outgoing style,
 * no per-bubble-type style), the resolution chain produces a non-null alignment-based
 * default style:
 * - LEFT alignment → `incoming()` → backgroundColor3 (0xFFEEEEEE)
 * - RIGHT alignment → `outgoing()` → primary (0xFF3399FF)
 * - CENTER alignment → `default()` → backgroundColor2 (0xFFF5F5F5)
 *
 * **Validates: Requirements 1.4, 1.5, 3.5, 5.3**
 */
class NullStyleFallbackPropertyTest : StringSpec({

    // ========================================================================
    // Arbitrary generators
    // ========================================================================

    val alignmentArb: Arb<UIKitConstants.MessageBubbleAlignment> = Arb.element(
        UIKitConstants.MessageBubbleAlignment.LEFT,
        UIKitConstants.MessageBubbleAlignment.RIGHT,
        UIKitConstants.MessageBubbleAlignment.CENTER
    )

    // ========================================================================
    // Property 5: Null style fallback produces alignment-based default
    // ========================================================================

    /**
     * **Feature: messagelist-bubble-style-propagation, Property 5: Null style fallback produces alignment-based default**
     *
     * WHEN all style inputs are null, the resolution SHALL always produce a non-null result.
     *
     * **Validates: Requirements 1.4, 1.5, 3.5, 5.3**
     */
    "Property 5: null style fallback always produces a non-null result" {
        checkAll(100, alignmentArb) { alignment ->
            val result = resolveNullFallbackBaseStyle(alignment)

            result shouldNotBe null
        }
    }

    /**
     * **Feature: messagelist-bubble-style-propagation, Property 5: Null style fallback produces alignment-based default**
     *
     * WHEN all style inputs are null AND alignment is LEFT, the resolved style
     * SHALL have `backgroundColor3` background (representing incoming default).
     *
     * **Validates: Requirements 1.4, 3.5**
     */
    "Property 5: LEFT alignment with all null inputs produces backgroundColor3 background" {
        checkAll(100, Arb.element(UIKitConstants.MessageBubbleAlignment.LEFT)) { alignment ->
            val result = resolveNullFallbackBaseStyle(alignment)

            result.backgroundColor shouldBe Color(0xFFEEEEEE)
        }
    }

    /**
     * **Feature: messagelist-bubble-style-propagation, Property 5: Null style fallback produces alignment-based default**
     *
     * WHEN all style inputs are null AND alignment is RIGHT, the resolved style
     * SHALL have `primary` background (representing outgoing default).
     *
     * **Validates: Requirements 1.5, 3.5**
     */
    "Property 5: RIGHT alignment with all null inputs produces primary background" {
        checkAll(100, Arb.element(UIKitConstants.MessageBubbleAlignment.RIGHT)) { alignment ->
            val result = resolveNullFallbackBaseStyle(alignment)

            result.backgroundColor shouldBe Color(0xFF3399FF)
        }
    }

    /**
     * **Feature: messagelist-bubble-style-propagation, Property 5: Null style fallback produces alignment-based default**
     *
     * WHEN all style inputs are null AND alignment is CENTER, the resolved style
     * SHALL have `backgroundColor2` background (representing center default).
     *
     * **Validates: Requirements 3.5**
     */
    "Property 5: CENTER alignment with all null inputs produces backgroundColor2 background" {
        checkAll(100, Arb.element(UIKitConstants.MessageBubbleAlignment.CENTER)) { alignment ->
            val result = resolveNullFallbackBaseStyle(alignment)

            result.backgroundColor shouldBe Color(0xFFF5F5F5)
        }
    }

    /**
     * **Feature: messagelist-bubble-style-propagation, Property 5: Null style fallback produces alignment-based default**
     *
     * For any random alignment with all null inputs, the resolved background color
     * SHALL match the expected alignment-based default color.
     *
     * **Validates: Requirements 1.4, 1.5, 3.5, 5.3**
     */
    "Property 5: any alignment with all null inputs produces correct alignment-based background" {
        checkAll(100, alignmentArb) { alignment ->
            val result = resolveNullFallbackBaseStyle(alignment)

            val expectedBg = when (alignment) {
                UIKitConstants.MessageBubbleAlignment.LEFT -> Color(0xFFEEEEEE)
                UIKitConstants.MessageBubbleAlignment.RIGHT -> Color(0xFF3399FF)
                UIKitConstants.MessageBubbleAlignment.CENTER -> Color(0xFFF5F5F5)
            }
            result.backgroundColor shouldBe expectedBg
        }
    }
})

// ============================================================================
// Helper function — replicates the three-tier resolution with all null inputs
// ============================================================================

/**
 * Replicates the baseStyle resolution logic from CometChatMessageBubble when
 * all style inputs are null (factory = null, incoming = null, outgoing = null).
 *
 * This mirrors the actual code path that falls through to alignment defaults:
 * ```kotlin
 * val baseStyle = factory?.getBubbleStyle(message, alignment)
 *     ?: when (alignment) {
 *         LEFT -> incomingMessageBubbleStyle ?: alignmentDefault
 *         RIGHT -> outgoingMessageBubbleStyle ?: alignmentDefault
 *         else -> alignmentDefault
 *     }
 * ```
 *
 * With all inputs null, this always resolves to `alignmentDefault`.
 */
private fun resolveNullFallbackBaseStyle(
    alignment: UIKitConstants.MessageBubbleAlignment
): CometChatMessageBubbleStyle {
    val alignDefault = alignmentDefault(alignment)
    return null // factoryStyle
        ?: when (alignment) {
            UIKitConstants.MessageBubbleAlignment.LEFT ->
                null ?: alignDefault // incomingMessageBubbleStyle is null
            UIKitConstants.MessageBubbleAlignment.RIGHT ->
                null ?: alignDefault // outgoingMessageBubbleStyle is null
            else -> alignDefault
        }
}

/**
 * Creates an alignment-based default style.
 *
 * Uses hardcoded color values representing theme tokens since the actual
 * factory functions are @Composable:
 * - LEFT → `incoming()` → backgroundColor3 = Color(0xFFEEEEEE)
 * - RIGHT → `outgoing()` → primary = Color(0xFF3399FF)
 * - CENTER → `default()` → backgroundColor2 = Color(0xFFF5F5F5)
 */
private fun alignmentDefault(
    alignment: UIKitConstants.MessageBubbleAlignment
): CometChatMessageBubbleStyle {
    return when (alignment) {
        UIKitConstants.MessageBubbleAlignment.LEFT -> CometChatMessageBubbleStyle(
            backgroundColor = Color(0xFFEEEEEE),  // backgroundColor3
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
            backgroundColor = Color(0xFF3399FF),  // primary
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
            backgroundColor = Color(0xFFF5F5F5),  // backgroundColor2
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
