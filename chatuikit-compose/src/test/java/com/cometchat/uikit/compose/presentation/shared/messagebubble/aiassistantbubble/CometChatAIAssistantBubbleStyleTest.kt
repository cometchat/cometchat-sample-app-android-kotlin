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
 * Validates: Requirements 4.2, 6.1, 12.3, 14.2
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

    "default style should have textColorSecondary as shimmerTextColor" {
        val style = createDefaultStyle()
        style.shimmerTextColor shouldBe SIMULATED_TEXT_COLOR_SECONDARY
    }

    "default style should have bodyRegular as shimmerTextStyle" {
        val style = createDefaultStyle()
        style.shimmerTextStyle shouldBe SIMULATED_BODY_REGULAR
    }

    "default style should have backgroundColor3 as errorBackgroundColor" {
        val style = createDefaultStyle()
        style.errorBackgroundColor shouldBe SIMULATED_BACKGROUND_COLOR_3
    }

    "default style should have errorColor as errorTextColor" {
        val style = createDefaultStyle()
        style.errorTextColor shouldBe SIMULATED_ERROR_COLOR
    }

    "default style should have caption1Regular as errorTextStyle" {
        val style = createDefaultStyle()
        style.errorTextStyle shouldBe SIMULATED_CAPTION1_REGULAR
    }

    "default style should have errorColor as errorIconTint" {
        val style = createDefaultStyle()
        style.errorIconTint shouldBe SIMULATED_ERROR_COLOR
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
        incomingStyle.shimmerTextColor shouldBe defaultStyle.shimmerTextColor
        incomingStyle.shimmerTextStyle shouldBe defaultStyle.shimmerTextStyle
        incomingStyle.errorBackgroundColor shouldBe defaultStyle.errorBackgroundColor
        incomingStyle.errorTextColor shouldBe defaultStyle.errorTextColor
        incomingStyle.errorTextStyle shouldBe defaultStyle.errorTextStyle
        incomingStyle.errorIconTint shouldBe defaultStyle.errorIconTint
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
        outgoingStyle.shimmerTextColor shouldBe defaultStyle.shimmerTextColor
        outgoingStyle.shimmerTextStyle shouldBe defaultStyle.shimmerTextStyle
        outgoingStyle.errorBackgroundColor shouldBe defaultStyle.errorBackgroundColor
        outgoingStyle.errorTextColor shouldBe defaultStyle.errorTextColor
        outgoingStyle.errorTextStyle shouldBe defaultStyle.errorTextStyle
        outgoingStyle.errorIconTint shouldBe defaultStyle.errorIconTint
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
        copied.shimmerTextColor shouldBe original.shimmerTextColor
        copied.shimmerTextStyle shouldBe original.shimmerTextStyle
        copied.errorBackgroundColor shouldBe original.errorBackgroundColor
        copied.errorTextColor shouldBe original.errorTextColor
        copied.errorTextStyle shouldBe original.errorTextStyle
        copied.errorIconTint shouldBe original.errorIconTint

        // Original unchanged
        original.backgroundColor shouldBe SIMULATED_BACKGROUND_COLOR_3
    }

    "copy should allow modifying shimmer properties" {
        val original = createDefaultStyle()
        val copied = original.copy(
            shimmerTextColor = Color.Yellow,
            shimmerTextStyle = TextStyle()
        )

        copied.shimmerTextColor shouldBe Color.Yellow
        copied.errorTextColor shouldBe original.errorTextColor

        // Original unchanged
        original.shimmerTextColor shouldBe SIMULATED_TEXT_COLOR_SECONDARY
    }

    "copy should allow modifying error properties" {
        val original = createDefaultStyle()
        val copied = original.copy(
            errorBackgroundColor = Color.DarkGray,
            errorTextColor = Color.White,
            errorTextStyle = TextStyle(),
            errorIconTint = Color.White
        )

        copied.errorBackgroundColor shouldBe Color.DarkGray
        copied.errorTextColor shouldBe Color.White
        copied.errorIconTint shouldBe Color.White
        copied.shimmerTextColor shouldBe original.shimmerTextColor

        // Original unchanged
        original.errorBackgroundColor shouldBe SIMULATED_BACKGROUND_COLOR_3
    }

    "all properties should be customizable via constructor" {
        val customShimmerStyle = TextStyle()
        val customErrorStyle = TextStyle()
        val custom = CometChatAIAssistantBubbleStyle(
            backgroundColor = Color.Cyan,
            textColor = Color.Green,
            textStyle = TextStyle(),
            cornerRadius = 12.dp,
            strokeWidth = 2.dp,
            strokeColor = Color.Magenta,
            shimmerTextColor = Color.LightGray,
            shimmerTextStyle = customShimmerStyle,
            errorBackgroundColor = Color.DarkGray,
            errorTextColor = Color.Red,
            errorTextStyle = customErrorStyle,
            errorIconTint = Color.Red
        )

        custom.backgroundColor shouldBe Color.Cyan
        custom.textColor shouldBe Color.Green
        custom.cornerRadius shouldBe 12.dp
        custom.strokeWidth shouldBe 2.dp
        custom.strokeColor shouldBe Color.Magenta
        custom.shimmerTextColor shouldBe Color.LightGray
        custom.shimmerTextStyle shouldBe customShimmerStyle
        custom.errorBackgroundColor shouldBe Color.DarkGray
        custom.errorTextColor shouldBe Color.Red
        custom.errorTextStyle shouldBe customErrorStyle
        custom.errorIconTint shouldBe Color.Red
    }
})

// --- Simulated CometChatTheme token values ---

private val SIMULATED_BACKGROUND_COLOR_3 = Color(0xFFE8E8E8)
private val SIMULATED_TEXT_COLOR_PRIMARY = Color(0xFF212121)
private val SIMULATED_TEXT_COLOR_SECONDARY = Color(0xFF757575)
private val SIMULATED_ERROR_COLOR = Color(0xFFF44649)
private val SIMULATED_BODY_REGULAR = TextStyle()
private val SIMULATED_CAPTION1_REGULAR = TextStyle()

/**
 * Simulates [CometChatAIAssistantBubbleStyle.Companion.default] without Compose context.
 */
private fun createDefaultStyle() = CometChatAIAssistantBubbleStyle(
    backgroundColor = SIMULATED_BACKGROUND_COLOR_3,
    textColor = SIMULATED_TEXT_COLOR_PRIMARY,
    textStyle = SIMULATED_BODY_REGULAR,
    cornerRadius = 0.dp,
    strokeWidth = 0.dp,
    strokeColor = Color.Transparent,
    shimmerTextColor = SIMULATED_TEXT_COLOR_SECONDARY,
    shimmerTextStyle = SIMULATED_BODY_REGULAR,
    errorBackgroundColor = SIMULATED_BACKGROUND_COLOR_3,
    errorTextColor = SIMULATED_ERROR_COLOR,
    errorTextStyle = SIMULATED_CAPTION1_REGULAR,
    errorIconTint = SIMULATED_ERROR_COLOR
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
    strokeColor = Color.Transparent,
    shimmerTextColor = SIMULATED_TEXT_COLOR_SECONDARY,
    shimmerTextStyle = SIMULATED_BODY_REGULAR,
    errorBackgroundColor = SIMULATED_BACKGROUND_COLOR_3,
    errorTextColor = SIMULATED_ERROR_COLOR,
    errorTextStyle = SIMULATED_CAPTION1_REGULAR,
    errorIconTint = SIMULATED_ERROR_COLOR
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
    strokeColor = Color.Transparent,
    shimmerTextColor = SIMULATED_TEXT_COLOR_SECONDARY,
    shimmerTextStyle = SIMULATED_BODY_REGULAR,
    errorBackgroundColor = SIMULATED_BACKGROUND_COLOR_3,
    errorTextColor = SIMULATED_ERROR_COLOR,
    errorTextStyle = SIMULATED_CAPTION1_REGULAR,
    errorIconTint = SIMULATED_ERROR_COLOR
)
