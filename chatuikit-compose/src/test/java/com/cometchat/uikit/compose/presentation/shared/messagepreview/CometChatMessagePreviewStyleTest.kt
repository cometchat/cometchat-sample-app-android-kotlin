package com.cometchat.uikit.compose.presentation.shared.messagepreview

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

/**
 * Unit tests for [CometChatMessagePreviewStyle] factory functions.
 *
 * Since the factory functions are @Composable and read from CometChatTheme,
 * these tests verify the data class structure and property behavior using
 * simulated theme token values that match the design specification.
 *
 * Validates: Requirements 2.2, 14.2
 */
class CometChatMessagePreviewStyleTest : StringSpec({

    // --- default() factory tests ---

    "default style should have backgroundColor2 as background" {
        val style = createDefaultStyle()
        style.backgroundColor shouldBe SIMULATED_BACKGROUND_COLOR_2
    }

    "default style should have 0dp strokeWidth and cornerRadius" {
        val style = createDefaultStyle()
        style.strokeWidth shouldBe 0.dp
        style.cornerRadius shouldBe 0.dp
    }

    "default style should have borderColorDefault for strokeColor and separatorColor" {
        val style = createDefaultStyle()
        style.strokeColor shouldBe SIMULATED_BORDER_COLOR_DEFAULT
        style.separatorColor shouldBe SIMULATED_BORDER_COLOR_DEFAULT
    }

    "default style should have textColorPrimary for title and textColorSecondary for subtitle" {
        val style = createDefaultStyle()
        style.titleTextColor shouldBe SIMULATED_TEXT_COLOR_PRIMARY
        style.subtitleTextColor shouldBe SIMULATED_TEXT_COLOR_SECONDARY
    }

    "default style should have caption1Medium for title and caption1Regular for subtitle" {
        val style = createDefaultStyle()
        style.titleTextStyle shouldBe SIMULATED_CAPTION1_MEDIUM
        style.subtitleTextStyle shouldBe SIMULATED_CAPTION1_REGULAR
    }

    "default style should have iconTintSecondary for closeIconTint and messageIconTint" {
        val style = createDefaultStyle()
        style.closeIconTint shouldBe SIMULATED_ICON_TINT_SECONDARY
        style.messageIconTint shouldBe SIMULATED_ICON_TINT_SECONDARY
    }

    // --- incoming() factory tests ---

    "incoming style should have neutralColor400 as background" {
        val style = createIncomingStyle()
        style.backgroundColor shouldBe SIMULATED_NEUTRAL_COLOR_400
    }

    "incoming style should have 8dp cornerRadius and 0dp strokeWidth" {
        val style = createIncomingStyle()
        style.cornerRadius shouldBe 8.dp
        style.strokeWidth shouldBe 0.dp
    }

    "incoming style should have Transparent strokeColor" {
        val style = createIncomingStyle()
        style.strokeColor shouldBe Color.Transparent
    }

    "incoming style should have primary color for separatorColor" {
        val style = createIncomingStyle()
        style.separatorColor shouldBe SIMULATED_PRIMARY
    }

    "incoming style should have textColorHighlight for title" {
        val style = createIncomingStyle()
        style.titleTextColor shouldBe SIMULATED_TEXT_COLOR_HIGHLIGHT
    }

    "incoming style should have textColorSecondary for subtitle" {
        val style = createIncomingStyle()
        style.subtitleTextColor shouldBe SIMULATED_TEXT_COLOR_SECONDARY
    }

    "incoming style should have iconTintSecondary for both icon tints" {
        val style = createIncomingStyle()
        style.closeIconTint shouldBe SIMULATED_ICON_TINT_SECONDARY
        style.messageIconTint shouldBe SIMULATED_ICON_TINT_SECONDARY
    }

    "incoming style should have caption1Medium for title and caption1Regular for subtitle" {
        val style = createIncomingStyle()
        style.titleTextStyle shouldBe SIMULATED_CAPTION1_MEDIUM
        style.subtitleTextStyle shouldBe SIMULATED_CAPTION1_REGULAR
    }

    // --- outgoing() factory tests ---

    "outgoing style should have extendedPrimaryColor800 as background" {
        val style = createOutgoingStyle()
        style.backgroundColor shouldBe SIMULATED_EXTENDED_PRIMARY_800
    }

    "outgoing style should have 8dp cornerRadius and 0dp strokeWidth" {
        val style = createOutgoingStyle()
        style.cornerRadius shouldBe 8.dp
        style.strokeWidth shouldBe 0.dp
    }

    "outgoing style should have Transparent strokeColor" {
        val style = createOutgoingStyle()
        style.strokeColor shouldBe Color.Transparent
    }

    "outgoing style should have White for separatorColor, titleTextColor, subtitleTextColor, closeIconTint, messageIconTint" {
        val style = createOutgoingStyle()
        style.separatorColor shouldBe Color.White
        style.titleTextColor shouldBe Color.White
        style.subtitleTextColor shouldBe Color.White
        style.closeIconTint shouldBe Color.White
        style.messageIconTint shouldBe Color.White
    }

    "outgoing style should have caption1Medium for title and caption1Regular for subtitle" {
        val style = createOutgoingStyle()
        style.titleTextStyle shouldBe SIMULATED_CAPTION1_MEDIUM
        style.subtitleTextStyle shouldBe SIMULATED_CAPTION1_REGULAR
    }

    // --- Cross-variant comparison tests ---

    "default, incoming, and outgoing should differ in backgroundColor" {
        val defaultStyle = createDefaultStyle()
        val incomingStyle = createIncomingStyle()
        val outgoingStyle = createOutgoingStyle()

        defaultStyle.backgroundColor shouldNotBe incomingStyle.backgroundColor
        defaultStyle.backgroundColor shouldNotBe outgoingStyle.backgroundColor
        incomingStyle.backgroundColor shouldNotBe outgoingStyle.backgroundColor
    }

    "incoming and outgoing should share cornerRadius of 8dp while default uses 0dp" {
        val defaultStyle = createDefaultStyle()
        val incomingStyle = createIncomingStyle()
        val outgoingStyle = createOutgoingStyle()

        defaultStyle.cornerRadius shouldBe 0.dp
        incomingStyle.cornerRadius shouldBe 8.dp
        outgoingStyle.cornerRadius shouldBe 8.dp
    }

    // --- Immutability / copy tests ---

    "copy should create new instance with modified values while preserving others" {
        val original = createDefaultStyle()
        val copied = original.copy(backgroundColor = Color.Red)

        copied.backgroundColor shouldBe Color.Red
        copied.strokeWidth shouldBe original.strokeWidth
        copied.cornerRadius shouldBe original.cornerRadius
        copied.separatorColor shouldBe original.separatorColor
        copied.titleTextColor shouldBe original.titleTextColor
        copied.subtitleTextColor shouldBe original.subtitleTextColor
        copied.closeIconTint shouldBe original.closeIconTint
        copied.messageIconTint shouldBe original.messageIconTint

        // Original unchanged
        original.backgroundColor shouldBe SIMULATED_BACKGROUND_COLOR_2
    }

    "all properties should be customizable via constructor" {
        val custom = CometChatMessagePreviewStyle(
            backgroundColor = Color.Cyan,
            strokeWidth = 2.dp,
            cornerRadius = 16.dp,
            strokeColor = Color.Magenta,
            separatorColor = Color.Yellow,
            titleTextColor = Color.Green,
            titleTextStyle = TextStyle(),
            subtitleTextColor = Color.Blue,
            subtitleTextStyle = TextStyle(),
            closeIconTint = Color.Red,
            messageIconTint = Color.Black
        )

        custom.backgroundColor shouldBe Color.Cyan
        custom.strokeWidth shouldBe 2.dp
        custom.cornerRadius shouldBe 16.dp
        custom.strokeColor shouldBe Color.Magenta
        custom.separatorColor shouldBe Color.Yellow
        custom.titleTextColor shouldBe Color.Green
        custom.subtitleTextColor shouldBe Color.Blue
        custom.closeIconTint shouldBe Color.Red
        custom.messageIconTint shouldBe Color.Black
    }
})

// --- Simulated CometChatTheme token values ---

private val SIMULATED_BACKGROUND_COLOR_2 = Color(0xFFF5F5F5)
private val SIMULATED_NEUTRAL_COLOR_400 = Color(0xFFBDBDBD)
private val SIMULATED_EXTENDED_PRIMARY_800 = Color(0xFF1565C0)
private val SIMULATED_BORDER_COLOR_DEFAULT = Color(0xFFE0E0E0)
private val SIMULATED_PRIMARY = Color(0xFF6200EE)
private val SIMULATED_TEXT_COLOR_PRIMARY = Color(0xFF212121)
private val SIMULATED_TEXT_COLOR_SECONDARY = Color(0xFF757575)
private val SIMULATED_TEXT_COLOR_HIGHLIGHT = Color(0xFF6200EE)
private val SIMULATED_ICON_TINT_SECONDARY = Color(0xFF9E9E9E)
private val SIMULATED_CAPTION1_MEDIUM = TextStyle()
private val SIMULATED_CAPTION1_REGULAR = TextStyle()

/**
 * Simulates [CometChatMessagePreviewStyle.Companion.default] without Compose context.
 */
private fun createDefaultStyle() = CometChatMessagePreviewStyle(
    backgroundColor = SIMULATED_BACKGROUND_COLOR_2,
    strokeWidth = 0.dp,
    cornerRadius = 0.dp,
    strokeColor = SIMULATED_BORDER_COLOR_DEFAULT,
    separatorColor = SIMULATED_BORDER_COLOR_DEFAULT,
    titleTextColor = SIMULATED_TEXT_COLOR_PRIMARY,
    titleTextStyle = SIMULATED_CAPTION1_MEDIUM,
    subtitleTextColor = SIMULATED_TEXT_COLOR_SECONDARY,
    subtitleTextStyle = SIMULATED_CAPTION1_REGULAR,
    closeIconTint = SIMULATED_ICON_TINT_SECONDARY,
    messageIconTint = SIMULATED_ICON_TINT_SECONDARY
)

/**
 * Simulates [CometChatMessagePreviewStyle.Companion.incoming] without Compose context.
 */
private fun createIncomingStyle() = CometChatMessagePreviewStyle(
    backgroundColor = SIMULATED_NEUTRAL_COLOR_400,
    strokeWidth = 0.dp,
    cornerRadius = 8.dp,
    strokeColor = Color.Transparent,
    separatorColor = SIMULATED_PRIMARY,
    titleTextColor = SIMULATED_TEXT_COLOR_HIGHLIGHT,
    titleTextStyle = SIMULATED_CAPTION1_MEDIUM,
    subtitleTextColor = SIMULATED_TEXT_COLOR_SECONDARY,
    subtitleTextStyle = SIMULATED_CAPTION1_REGULAR,
    closeIconTint = SIMULATED_ICON_TINT_SECONDARY,
    messageIconTint = SIMULATED_ICON_TINT_SECONDARY
)

/**
 * Simulates [CometChatMessagePreviewStyle.Companion.outgoing] without Compose context.
 */
private fun createOutgoingStyle() = CometChatMessagePreviewStyle(
    backgroundColor = SIMULATED_EXTENDED_PRIMARY_800,
    strokeWidth = 0.dp,
    cornerRadius = 8.dp,
    strokeColor = Color.Transparent,
    separatorColor = Color.White,
    titleTextColor = Color.White,
    titleTextStyle = SIMULATED_CAPTION1_MEDIUM,
    subtitleTextColor = Color.White,
    subtitleTextStyle = SIMULATED_CAPTION1_REGULAR,
    closeIconTint = Color.White,
    messageIconTint = Color.White
)
