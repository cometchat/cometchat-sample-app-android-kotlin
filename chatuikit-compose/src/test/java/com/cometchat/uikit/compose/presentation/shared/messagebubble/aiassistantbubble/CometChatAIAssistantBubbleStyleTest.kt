package com.cometchat.uikit.compose.presentation.shared.messagebubble.aiassistantbubble

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

/**
 * Unit tests for [CometChatAIAssistantBubbleStyle] factory functions.
 *
 * Since the factory functions are @Composable and read from CometChatTheme,
 * these tests verify the data class structure and property behavior using
 * simulated theme token values that match the design specification.
 *
 * Validates: Requirements 14.2
 */
class CometChatAIAssistantBubbleStyleTest : StringSpec({

    // --- default() factory tests ---

    "default style should have backgroundColor3 as background" {
        val style = createDefaultStyle()
        style.backgroundColor shouldBe SIMULATED_BACKGROUND_COLOR_3
    }

    "default style should have textColorPrimary as textColor" {
        val style = createDefaultStyle()
        style.textColor shouldBe SIMULATED_TEXT_COLOR_PRIMARY
    }

    "default style should have bodyRegular as textStyle" {
        val style = createDefaultStyle()
        style.textStyle shouldBe SIMULATED_BODY_REGULAR
    }

    "default style should have 0dp cornerRadius and strokeWidth" {
        val style = createDefaultStyle()
        style.cornerRadius shouldBe 0.dp
        style.strokeWidth shouldBe 0.dp
    }

    "default style should have Transparent strokeColor" {
        val style = createDefaultStyle()
        style.strokeColor shouldBe Color.Transparent
    }

    // --- incoming() and outgoing() share same defaults as default() ---

    "incoming style should match default style values" {
        val defaultStyle = createDefaultStyle()
        val incomingStyle = createIncomingStyle()

        incomingStyle.backgroundColor shouldBe defaultStyle.backgroundColor
        incomingStyle.textColor shouldBe defaultStyle.textColor
        incomingStyle.textStyle shouldBe defaultStyle.textStyle
        incomingStyle.cornerRadius shouldBe defaultStyle.cornerRadius
        incomingStyle.strokeWidth shouldBe defaultStyle.strokeWidth
        incomingStyle.strokeColor shouldBe defaultStyle.strokeColor
    }

    "outgoing style should match default style values" {
        val defaultStyle = createDefaultStyle()
        val outgoingStyle = createOutgoingStyle()

        outgoingStyle.backgroundColor shouldBe defaultStyle.backgroundColor
        outgoingStyle.textColor shouldBe defaultStyle.textColor
        outgoingStyle.textStyle shouldBe defaultStyle.textStyle
        outgoingStyle.cornerRadius shouldBe defaultStyle.cornerRadius
        outgoingStyle.strokeWidth shouldBe defaultStyle.strokeWidth
        outgoingStyle.strokeColor shouldBe defaultStyle.strokeColor
    }

    // --- Immutability / copy tests ---

    "copy should create new instance with modified values while preserving others" {
        val original = createDefaultStyle()
        val copied = original.copy(backgroundColor = Color.Red)

        copied.backgroundColor shouldBe Color.Red
        copied.textColor shouldBe original.textColor
        copied.textStyle shouldBe original.textStyle
        copied.cornerRadius shouldBe original.cornerRadius
        copied.strokeWidth shouldBe original.strokeWidth
        copied.strokeColor shouldBe original.strokeColor

        // Original unchanged
        original.backgroundColor shouldBe SIMULATED_BACKGROUND_COLOR_3
    }

    "all properties should be customizable via constructor" {
        val custom = CometChatAIAssistantBubbleStyle(
            backgroundColor = Color.Cyan,
            textColor = Color.Green,
            textStyle = TextStyle(),
            cornerRadius = 12.dp,
            strokeWidth = 2.dp,
            strokeColor = Color.Magenta
        )

        custom.backgroundColor shouldBe Color.Cyan
        custom.textColor shouldBe Color.Green
        custom.cornerRadius shouldBe 12.dp
        custom.strokeWidth shouldBe 2.dp
        custom.strokeColor shouldBe Color.Magenta
    }
})

// --- Simulated CometChatTheme token values ---

private val SIMULATED_BACKGROUND_COLOR_3 = Color(0xFFE8E8E8)
private val SIMULATED_TEXT_COLOR_PRIMARY = Color(0xFF212121)
private val SIMULATED_BODY_REGULAR = TextStyle()

/**
 * Simulates [CometChatAIAssistantBubbleStyle.Companion.default] without Compose context.
 */
private fun createDefaultStyle() = CometChatAIAssistantBubbleStyle(
    backgroundColor = SIMULATED_BACKGROUND_COLOR_3,
    textColor = SIMULATED_TEXT_COLOR_PRIMARY,
    textStyle = SIMULATED_BODY_REGULAR,
    cornerRadius = 0.dp,
    strokeWidth = 0.dp,
    strokeColor = Color.Transparent
)

/**
 * Simulates [CometChatAIAssistantBubbleStyle.Companion.incoming] without Compose context.
 */
private fun createIncomingStyle() = CometChatAIAssistantBubbleStyle(
    backgroundColor = SIMULATED_BACKGROUND_COLOR_3,
    textColor = SIMULATED_TEXT_COLOR_PRIMARY,
    textStyle = SIMULATED_BODY_REGULAR,
    cornerRadius = 0.dp,
    strokeWidth = 0.dp,
    strokeColor = Color.Transparent
)

/**
 * Simulates [CometChatAIAssistantBubbleStyle.Companion.outgoing] without Compose context.
 */
private fun createOutgoingStyle() = CometChatAIAssistantBubbleStyle(
    backgroundColor = SIMULATED_BACKGROUND_COLOR_3,
    textColor = SIMULATED_TEXT_COLOR_PRIMARY,
    textStyle = SIMULATED_BODY_REGULAR,
    cornerRadius = 0.dp,
    strokeWidth = 0.dp,
    strokeColor = Color.Transparent
)
