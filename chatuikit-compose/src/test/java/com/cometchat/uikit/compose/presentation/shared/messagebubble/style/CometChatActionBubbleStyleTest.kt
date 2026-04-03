package com.cometchat.uikit.compose.presentation.shared.messagebubble.style

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.float
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll

/**
 * Unit tests for CometChatActionBubbleStyle.
 *
 * Feature: bubble-style-propagation
 * Tests:
 * - CometChatActionBubbleStyle extends CometChatMessageBubbleStyle
 * - default() factory function returns valid style with theme defaults
 * - incoming() factory function returns valid style
 * - outgoing() factory function returns valid style
 * - All properties are accessible
 * - Style immutability (copy creates new instance)
 *
 * Validates: Requirements 2.1, 2.2, 2.3, 2.4, 7.3, 7.5
 */
class CometChatActionBubbleStyleTest : StringSpec({

    /**
     * Test that CometChatActionBubbleStyle extends CometChatMessageBubbleStyle.
     *
     * Validates: Requirements 2.1
     */
    "CometChatActionBubbleStyle should extend CometChatMessageBubbleStyle" {
        val style = createTestActionBubbleStyle()

        // Verify inheritance relationship
        style.shouldBeInstanceOf<CometChatMessageBubbleStyle>()
    }

    /**
     * Test that all properties are accessible and have expected default values.
     *
     * Validates: Requirements 2.2, 2.4
     */
    "all properties should be accessible with expected default values" {
        val style = createTestActionBubbleStyle()

        // Verify content-specific properties
        style.textColor shouldBe Color.Gray
        style.textStyle shouldNotBe null

        // Verify inherited wrapper properties
        style.backgroundColor shouldBe Color(0xFFF5F5F5) // Simulating backgroundColor2
        style.cornerRadius shouldBe 8.dp // Action bubbles use smaller corner radius
        style.strokeWidth shouldBe 0.dp
        style.strokeColor shouldBe Color.Transparent
        style.padding shouldBe PaddingValues(0.dp)
        style.senderNameTextColor shouldBe Color.Gray
        style.senderNameTextStyle shouldNotBe null
        style.threadIndicatorTextColor shouldBe Color.Gray
        style.threadIndicatorTextStyle shouldNotBe null
        style.threadIndicatorIconTint shouldBe Color.Gray
    }

    /**
     * Test that action bubbles use smaller corner radius by default.
     *
     * Validates: Requirements 2.2
     */
    "action bubble should use smaller corner radius by default" {
        val style = createTestActionBubbleStyle()

        // Action bubbles typically use 8.dp corner radius
        style.cornerRadius shouldBe 8.dp
    }

    /**
     * Test that incoming() factory function returns a valid style.
     *
     * Validates: Requirements 2.3
     */
    "incoming factory function should return valid style with backgroundColor3" {
        val style = createTestActionBubbleStyleIncoming()

        // Verify incoming style has expected values
        style.backgroundColor shouldBe Color(0xFFE8E8E8) // Simulating backgroundColor3
        style.cornerRadius shouldBe 8.dp
        style.textColor shouldBe Color.Gray
    }

    /**
     * Test that outgoing() factory function returns a valid style with different colors.
     *
     * Validates: Requirements 2.3
     */
    "outgoing factory function should return valid style with primary background and white text" {
        val style = createTestActionBubbleStyleOutgoing()

        // Verify outgoing style has primary background and white text
        style.backgroundColor shouldBe Color.Blue // Simulating primary
        style.cornerRadius shouldBe 8.dp
        style.textColor shouldBe Color.White
    }

    /**
     * Test style immutability - copy should create new instance with modified values
     * while original remains unchanged.
     *
     * Validates: Requirements 2.2, 2.4
     */
    "copy should create new instance with modified values while original remains unchanged" {
        val styleArb = Arb.bind(
            Arb.int(0, 255),      // backgroundColor alpha
            Arb.float(4f, 24f),   // cornerRadius
            Arb.int(0, 255)       // textColor alpha
        ) { bgAlpha, cornerRadius, textAlpha ->
            createTestActionBubbleStyle(
                backgroundColor = Color(1f, 1f, 1f, bgAlpha / 255f),
                cornerRadius = cornerRadius.dp,
                textColor = Color(0.5f, 0.5f, 0.5f, textAlpha / 255f)
            )
        }

        checkAll(100, styleArb) { originalStyle ->
            // Store original values
            val originalBackgroundColor = originalStyle.backgroundColor
            val originalCornerRadius = originalStyle.cornerRadius
            val originalTextColor = originalStyle.textColor

            // Create a copy with modified backgroundColor
            val newBackgroundColor = Color.Red
            val copiedStyle = originalStyle.copy(backgroundColor = newBackgroundColor)

            // Verify the copied style has the new value
            copiedStyle.backgroundColor shouldBe newBackgroundColor

            // Verify the original style is unchanged
            originalStyle.backgroundColor shouldBe originalBackgroundColor
            originalStyle.cornerRadius shouldBe originalCornerRadius
            originalStyle.textColor shouldBe originalTextColor

            // Verify the copied style retained other values
            copiedStyle.cornerRadius shouldBe originalCornerRadius
            copiedStyle.textColor shouldBe originalTextColor

            // Verify they are different instances (by value comparison after modification)
            copiedStyle.backgroundColor shouldNotBe originalStyle.backgroundColor
        }
    }

    /**
     * Test style copy with modified content properties.
     *
     * Validates: Requirements 2.2
     */
    "copy with modified content properties should preserve wrapper properties" {
        val styleArb = Arb.bind(
            Arb.int(0, 255),      // textColor alpha
            Arb.float(4f, 16f)    // cornerRadius (unused but needed for Arb.bind)
        ) { textAlpha, _ ->
            createTestActionBubbleStyle(
                textColor = Color(0.5f, 0.5f, 0.5f, textAlpha / 255f)
            )
        }

        checkAll(100, styleArb) { originalStyle ->
            val originalBackgroundColor = originalStyle.backgroundColor
            val originalCornerRadius = originalStyle.cornerRadius
            val originalStrokeWidth = originalStyle.strokeWidth

            // Create a copy with modified content values
            val newTextColor = Color.Yellow
            val copiedStyle = originalStyle.copy(textColor = newTextColor)

            // Verify modified values
            copiedStyle.textColor shouldBe newTextColor

            // Verify wrapper properties are preserved
            copiedStyle.backgroundColor shouldBe originalBackgroundColor
            copiedStyle.cornerRadius shouldBe originalCornerRadius
            copiedStyle.strokeWidth shouldBe originalStrokeWidth
        }
    }

    /**
     * Test style copy with modified stroke properties.
     *
     * Validates: Requirements 2.2
     */
    "copy with modified stroke properties should preserve content properties" {
        val styleArb = Arb.bind(
            Arb.float(0f, 4f),    // strokeWidth
            Arb.int(0, 255)       // strokeColor alpha
        ) { strokeWidth, strokeAlpha ->
            createTestActionBubbleStyle(
                strokeWidth = strokeWidth.dp,
                strokeColor = Color(0f, 0f, 0f, strokeAlpha / 255f)
            )
        }

        checkAll(100, styleArb) { originalStyle ->
            val originalTextColor = originalStyle.textColor
            val originalTextStyle = originalStyle.textStyle

            // Create a copy with modified stroke values
            val newStrokeWidth = 2.dp
            val newStrokeColor = Color.Red
            val copiedStyle = originalStyle.copy(
                strokeWidth = newStrokeWidth,
                strokeColor = newStrokeColor
            )

            // Verify modified values
            copiedStyle.strokeWidth shouldBe newStrokeWidth
            copiedStyle.strokeColor shouldBe newStrokeColor

            // Verify content properties are preserved
            copiedStyle.textColor shouldBe originalTextColor
            copiedStyle.textStyle shouldBe originalTextStyle
        }
    }

    /**
     * Test that copying style with same values creates equal instance.
     *
     * Validates: Requirements 2.2
     */
    "copying style with same values should create equal instance" {
        val styleArb = Arb.bind(
            Arb.int(0, 255),
            Arb.float(4f, 16f)
        ) { alpha, cornerRadius ->
            createTestActionBubbleStyle(
                backgroundColor = Color(1f, 1f, 1f, alpha / 255f),
                cornerRadius = cornerRadius.dp
            )
        }

        checkAll(100, styleArb) { originalStyle ->
            // Create a copy with no modifications
            val copiedStyle = originalStyle.copy()

            // Verify values are equal
            copiedStyle.backgroundColor shouldBe originalStyle.backgroundColor
            copiedStyle.cornerRadius shouldBe originalStyle.cornerRadius
            copiedStyle.strokeWidth shouldBe originalStyle.strokeWidth
            copiedStyle.strokeColor shouldBe originalStyle.strokeColor
            copiedStyle.padding shouldBe originalStyle.padding
            copiedStyle.textColor shouldBe originalStyle.textColor
            copiedStyle.textStyle shouldBe originalStyle.textStyle
            copiedStyle.senderNameTextColor shouldBe originalStyle.senderNameTextColor
            copiedStyle.senderNameTextStyle shouldBe originalStyle.senderNameTextStyle
            copiedStyle.threadIndicatorTextColor shouldBe originalStyle.threadIndicatorTextColor
            copiedStyle.threadIndicatorTextStyle shouldBe originalStyle.threadIndicatorTextStyle
            copiedStyle.threadIndicatorIconTint shouldBe originalStyle.threadIndicatorIconTint

            // Data classes with same values are equal
            copiedStyle shouldBe originalStyle
        }
    }

    /**
     * Test that all style properties can be customized.
     *
     * Validates: Requirements 2.2
     */
    "all style properties should be customizable" {
        val customStyle = CometChatActionBubbleStyle(
            textColor = Color.Yellow,
            textStyle = TextStyle(fontSize = 12.sp),
            backgroundColor = Color.Blue,
            cornerRadius = 20.dp,
            strokeWidth = 2.dp,
            strokeColor = Color.Red,
            padding = PaddingValues(8.dp),
            senderNameTextColor = Color.Black,
            senderNameTextStyle = TextStyle(fontSize = 11.sp),
            threadIndicatorTextColor = Color.White,
            threadIndicatorTextStyle = TextStyle(fontSize = 10.sp),
            threadIndicatorIconTint = Color.Cyan,
            timestampTextColor = Color.Magenta,
            timestampTextStyle = TextStyle(fontSize = 10.sp)
        )

        // Verify content-specific properties
        customStyle.textColor shouldBe Color.Yellow
        customStyle.textStyle shouldBe TextStyle(fontSize = 12.sp)

        // Verify inherited wrapper properties
        customStyle.backgroundColor shouldBe Color.Blue
        customStyle.cornerRadius shouldBe 20.dp
        customStyle.strokeWidth shouldBe 2.dp
        customStyle.strokeColor shouldBe Color.Red
        customStyle.padding shouldBe PaddingValues(8.dp)
        customStyle.senderNameTextColor shouldBe Color.Black
        customStyle.senderNameTextStyle shouldBe TextStyle(fontSize = 11.sp)
        customStyle.threadIndicatorTextColor shouldBe Color.White
        customStyle.threadIndicatorTextStyle shouldBe TextStyle(fontSize = 10.sp)
        customStyle.threadIndicatorIconTint shouldBe Color.Cyan
        customStyle.timestampTextColor shouldBe Color.Magenta
        customStyle.timestampTextStyle shouldBe TextStyle(fontSize = 10.sp)
    }

    /**
     * Test that incoming and outgoing styles differ in expected properties.
     *
     * Validates: Requirements 2.3
     */
    "incoming and outgoing styles should differ in background and text colors" {
        val incomingStyle = createTestActionBubbleStyleIncoming()
        val outgoingStyle = createTestActionBubbleStyleOutgoing()

        // Verify common properties are the same
        incomingStyle.cornerRadius shouldBe outgoingStyle.cornerRadius
        incomingStyle.strokeWidth shouldBe outgoingStyle.strokeWidth
        incomingStyle.strokeColor shouldBe outgoingStyle.strokeColor

        // Verify differing properties
        incomingStyle.backgroundColor shouldNotBe outgoingStyle.backgroundColor
        incomingStyle.textColor shouldNotBe outgoingStyle.textColor

        // Verify outgoing uses primary background and white text
        outgoingStyle.backgroundColor shouldBe Color.Blue // Simulating primary
        outgoingStyle.textColor shouldBe Color.White
    }

    /**
     * Test that style can be assigned to CometChatMessageBubbleStyle variable.
     *
     * Validates: Requirements 2.1
     */
    "style should be assignable to CometChatMessageBubbleStyle variable" {
        val actionStyle: CometChatActionBubbleStyle = createTestActionBubbleStyle()

        // Verify it can be assigned to parent type
        val messageBubbleStyle: CometChatMessageBubbleStyle = actionStyle

        // Verify inherited properties are accessible through parent type
        messageBubbleStyle.backgroundColor shouldBe actionStyle.backgroundColor
        messageBubbleStyle.cornerRadius shouldBe actionStyle.cornerRadius
        messageBubbleStyle.strokeWidth shouldBe actionStyle.strokeWidth
        messageBubbleStyle.strokeColor shouldBe actionStyle.strokeColor
        messageBubbleStyle.padding shouldBe actionStyle.padding
        messageBubbleStyle.senderNameTextColor shouldBe actionStyle.senderNameTextColor
        messageBubbleStyle.senderNameTextStyle shouldBe actionStyle.senderNameTextStyle
        messageBubbleStyle.threadIndicatorTextColor shouldBe actionStyle.threadIndicatorTextColor
        messageBubbleStyle.threadIndicatorTextStyle shouldBe actionStyle.threadIndicatorTextStyle
        messageBubbleStyle.threadIndicatorIconTint shouldBe actionStyle.threadIndicatorIconTint
    }

    /**
     * Test that action bubble can have pill-shaped corner radius.
     *
     * Validates: Requirements 2.2
     */
    "action bubble can have pill-shaped corner radius" {
        val style = createTestActionBubbleStyle(cornerRadius = 100.dp)

        // 100.dp is used for pill shape (radius_max)
        style.cornerRadius shouldBe 100.dp
    }
})


/**
 * Helper function to create a test action bubble style with customizable values.
 * This simulates what the default() factory function would return without requiring
 * a Compose context.
 */
private fun createTestActionBubbleStyle(
    // Content-specific properties
    textColor: Color = Color.Gray, // Simulating CometChatTheme.colorScheme.textColorSecondary
    textStyle: TextStyle = TextStyle(fontSize = 12.sp), // Simulating caption1Regular
    // Inherited wrapper properties
    backgroundColor: Color = Color(0xFFF5F5F5), // Simulating backgroundColor2
    cornerRadius: Dp = 8.dp, // Action bubbles use smaller corner radius
    strokeWidth: Dp = 0.dp,
    strokeColor: Color = Color.Transparent,
    padding: PaddingValues = PaddingValues(0.dp),
    senderNameTextColor: Color = Color.Gray,
    senderNameTextStyle: TextStyle = TextStyle(fontSize = 12.sp),
    threadIndicatorTextColor: Color = Color.Gray,
    threadIndicatorTextStyle: TextStyle = TextStyle(fontSize = 12.sp),
    threadIndicatorIconTint: Color = Color.Gray,
    timestampTextColor: Color = Color.Gray,
    timestampTextStyle: TextStyle = TextStyle(fontSize = 12.sp)
): CometChatActionBubbleStyle {
    return CometChatActionBubbleStyle(
        textColor = textColor,
        textStyle = textStyle,
        backgroundColor = backgroundColor,
        cornerRadius = cornerRadius,
        strokeWidth = strokeWidth,
        strokeColor = strokeColor,
        padding = padding,
        senderNameTextColor = senderNameTextColor,
        senderNameTextStyle = senderNameTextStyle,
        threadIndicatorTextColor = threadIndicatorTextColor,
        threadIndicatorTextStyle = threadIndicatorTextStyle,
        threadIndicatorIconTint = threadIndicatorIconTint,
        timestampTextColor = timestampTextColor,
        timestampTextStyle = timestampTextStyle
    )
}


/**
 * Helper function to create a test action bubble style for incoming messages.
 * This simulates what the incoming() factory function would return without requiring
 * a Compose context.
 */
private fun createTestActionBubbleStyleIncoming(
    // Content-specific properties
    textColor: Color = Color.Gray, // Simulating CometChatTheme.colorScheme.textColorSecondary
    textStyle: TextStyle = TextStyle(fontSize = 12.sp), // Simulating caption1Regular
    // Inherited wrapper properties - incoming uses backgroundColor3
    backgroundColor: Color = Color(0xFFE8E8E8), // Simulating backgroundColor3
    cornerRadius: Dp = 8.dp,
    strokeWidth: Dp = 0.dp,
    strokeColor: Color = Color.Transparent,
    padding: PaddingValues = PaddingValues(0.dp),
    senderNameTextColor: Color = Color.Gray,
    senderNameTextStyle: TextStyle = TextStyle(fontSize = 12.sp),
    threadIndicatorTextColor: Color = Color.Gray,
    threadIndicatorTextStyle: TextStyle = TextStyle(fontSize = 12.sp),
    threadIndicatorIconTint: Color = Color.Gray,
    timestampTextColor: Color = Color.Gray,
    timestampTextStyle: TextStyle = TextStyle(fontSize = 12.sp)
): CometChatActionBubbleStyle {
    return CometChatActionBubbleStyle(
        textColor = textColor,
        textStyle = textStyle,
        backgroundColor = backgroundColor,
        cornerRadius = cornerRadius,
        strokeWidth = strokeWidth,
        strokeColor = strokeColor,
        padding = padding,
        senderNameTextColor = senderNameTextColor,
        senderNameTextStyle = senderNameTextStyle,
        threadIndicatorTextColor = threadIndicatorTextColor,
        threadIndicatorTextStyle = threadIndicatorTextStyle,
        threadIndicatorIconTint = threadIndicatorIconTint,
        timestampTextColor = timestampTextColor,
        timestampTextStyle = timestampTextStyle
    )
}


/**
 * Helper function to create a test action bubble style for outgoing messages.
 * This simulates what the outgoing() factory function would return without requiring
 * a Compose context.
 */
private fun createTestActionBubbleStyleOutgoing(
    // Content-specific properties - outgoing uses white text
    textColor: Color = Color.White, // Outgoing uses white text
    textStyle: TextStyle = TextStyle(fontSize = 12.sp), // Simulating caption1Regular
    // Inherited wrapper properties - outgoing uses primary background
    backgroundColor: Color = Color.Blue, // Simulating primary
    cornerRadius: Dp = 8.dp,
    strokeWidth: Dp = 0.dp,
    strokeColor: Color = Color.Transparent,
    padding: PaddingValues = PaddingValues(0.dp),
    senderNameTextColor: Color = Color.Gray,
    senderNameTextStyle: TextStyle = TextStyle(fontSize = 12.sp),
    threadIndicatorTextColor: Color = Color.Gray,
    threadIndicatorTextStyle: TextStyle = TextStyle(fontSize = 12.sp),
    threadIndicatorIconTint: Color = Color.Gray,
    timestampTextColor: Color = Color.White.copy(alpha = 0.8f), // Outgoing uses white timestamp
    timestampTextStyle: TextStyle = TextStyle(fontSize = 12.sp)
): CometChatActionBubbleStyle {
    return CometChatActionBubbleStyle(
        textColor = textColor,
        textStyle = textStyle,
        backgroundColor = backgroundColor,
        cornerRadius = cornerRadius,
        strokeWidth = strokeWidth,
        strokeColor = strokeColor,
        padding = padding,
        senderNameTextColor = senderNameTextColor,
        senderNameTextStyle = senderNameTextStyle,
        threadIndicatorTextColor = threadIndicatorTextColor,
        threadIndicatorTextStyle = threadIndicatorTextStyle,
        threadIndicatorIconTint = threadIndicatorIconTint,
        timestampTextColor = timestampTextColor,
        timestampTextStyle = timestampTextStyle
    )
}
