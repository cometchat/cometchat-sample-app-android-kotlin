package com.cometchat.uikit.compose.shared.formatters.style

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import com.cometchat.uikit.compose.presentation.shared.formatters.style.CometChatMentionStyle
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll

/**
 * Property-based tests for CometChatMentionStyle.
 *
 * Feature: mention-style-compose
 * Properties tested:
 * - Property 1: Color Conversion Preserves Values
 * - Property 2: Self-Mention Conversion Uses Self Properties
 * - Property 3: Default Factory Produces Valid Style
 */
class CometChatMentionStylePropertyTest : StringSpec({

    /**
     * Property 1: Color Conversion Preserves Values
     *
     * For any CometChatMentionStyle with valid Color values, converting to PromptTextStyle
     * via toPromptTextStyle() and extracting the color should yield the same ARGB integer
     * value as calling toArgb() on the original Color.
     *
     * Feature: mention-style-compose, Property 1: Color Conversion Preserves Values
     * Validates: Requirements 3.3
     */
    "Property 1: toPromptTextStyle should preserve textColor ARGB value" {
        val colorArb = Arb.bind(
            Arb.int(0, 255),  // red
            Arb.int(0, 255),  // green
            Arb.int(0, 255),  // blue
            Arb.int(0, 255)   // alpha
        ) { r, g, b, a ->
            Color(r, g, b, a)
        }

        checkAll(100, colorArb, colorArb) { textColor, backgroundColor ->
            val style = createTestMentionStyle(
                textColor = textColor,
                backgroundColor = backgroundColor
            )

            val promptTextStyle = style.toPromptTextStyle()

            // Verify color conversion preserves ARGB value
            promptTextStyle.getColor() shouldBe textColor.toArgb()
            promptTextStyle.getBackgroundColor() shouldBe backgroundColor.toArgb()
        }
    }

    /**
     * Property 1: Color Conversion Preserves Values - Edge cases
     *
     * For any CometChatMentionStyle with edge case Color values (transparent, fully opaque),
     * the conversion should still preserve the exact ARGB values.
     *
     * Feature: mention-style-compose, Property 1: Color Conversion Preserves Values
     * Validates: Requirements 3.3
     */
    "Property 1: toPromptTextStyle should handle edge case colors correctly" {
        val edgeCaseColors = listOf(
            Color.Transparent,
            Color.Black,
            Color.White,
            Color.Red,
            Color.Green,
            Color.Blue,
            Color(0x00000000),  // Fully transparent black
            Color(0xFFFFFFFF.toInt()),  // Fully opaque white
            Color(0x80808080.toInt())   // Semi-transparent gray
        )

        edgeCaseColors.forEach { testColor ->
            val style = createTestMentionStyle(textColor = testColor)
            val promptTextStyle = style.toPromptTextStyle()

            promptTextStyle.getColor() shouldBe testColor.toArgb()
        }
    }

    /**
     * Property 2: Self-Mention Conversion Uses Self Properties
     *
     * For any CometChatMentionStyle, calling toSelfPromptTextStyle() should produce a
     * PromptTextStyle where getColor() equals selfTextColor.toArgb() and
     * getBackgroundColor() equals selfBackgroundColor.toArgb().
     *
     * Feature: mention-style-compose, Property 2: Self-Mention Conversion Uses Self Properties
     * Validates: Requirements 3.5
     */
    "Property 2: toSelfPromptTextStyle should use selfTextColor and selfBackgroundColor" {
        val colorArb = Arb.bind(
            Arb.int(0, 255),  // red
            Arb.int(0, 255),  // green
            Arb.int(0, 255),  // blue
            Arb.int(0, 255)   // alpha
        ) { r, g, b, a ->
            Color(r, g, b, a)
        }

        checkAll(100, colorArb, colorArb, colorArb, colorArb) { textColor, backgroundColor, selfTextColor, selfBackgroundColor ->
            val style = createTestMentionStyle(
                textColor = textColor,
                backgroundColor = backgroundColor,
                selfTextColor = selfTextColor,
                selfBackgroundColor = selfBackgroundColor
            )

            val selfPromptTextStyle = style.toSelfPromptTextStyle()

            // Verify self-mention conversion uses self properties
            selfPromptTextStyle.getColor() shouldBe selfTextColor.toArgb()
            selfPromptTextStyle.getBackgroundColor() shouldBe selfBackgroundColor.toArgb()

            // Verify it does NOT use regular properties
            if (textColor != selfTextColor) {
                selfPromptTextStyle.getColor() shouldBe selfTextColor.toArgb()
            }
            if (backgroundColor != selfBackgroundColor) {
                selfPromptTextStyle.getBackgroundColor() shouldBe selfBackgroundColor.toArgb()
            }
        }
    }

    /**
     * Property 2: Self-Mention Conversion Uses Self Properties - Isolation test
     *
     * For any CometChatMentionStyle with distinct regular and self colors,
     * toPromptTextStyle() and toSelfPromptTextStyle() should produce different results.
     *
     * Feature: mention-style-compose, Property 2: Self-Mention Conversion Uses Self Properties
     * Validates: Requirements 3.5
     */
    "Property 2: toPromptTextStyle and toSelfPromptTextStyle should produce different results for distinct colors" {
        val colorArb = Arb.bind(
            Arb.int(0, 255),
            Arb.int(0, 255),
            Arb.int(0, 255),
            Arb.int(1, 255)  // Ensure non-zero alpha for visibility
        ) { r, g, b, a ->
            Color(r, g, b, a)
        }

        checkAll(100, colorArb, colorArb) { regularColor, selfColor ->
            // Only test when colors are different
            if (regularColor != selfColor) {
                val style = createTestMentionStyle(
                    textColor = regularColor,
                    selfTextColor = selfColor
                )

                val regularPromptStyle = style.toPromptTextStyle()
                val selfPromptStyle = style.toSelfPromptTextStyle()

                // Regular style should use regular color
                regularPromptStyle.getColor() shouldBe regularColor.toArgb()

                // Self style should use self color
                selfPromptStyle.getColor() shouldBe selfColor.toArgb()
            }
        }
    }

    /**
     * Property 3: Default Factory Produces Valid Style
     *
     * For any call to CometChatMentionStyle with explicit values, the returned style
     * should have the exact values provided.
     *
     * Note: Testing the @Composable default() function requires a Compose test environment.
     * This test verifies the data class construction with explicit values.
     *
     * Feature: mention-style-compose, Property 3: Default Factory Produces Valid Style
     * Validates: Requirements 2.2, 2.5, 2.6
     */
    "Property 3: CometChatMentionStyle construction should preserve all provided values" {
        val colorArb = Arb.bind(
            Arb.int(0, 255),
            Arb.int(0, 255),
            Arb.int(0, 255),
            Arb.int(0, 255)
        ) { r, g, b, a ->
            Color(r, g, b, a)
        }

        checkAll(100, colorArb, colorArb, colorArb, colorArb) { textColor, backgroundColor, selfTextColor, selfBackgroundColor ->
            val textStyle = TextStyle(fontSize = 14.sp)
            val selfTextStyle = TextStyle(fontSize = 16.sp)

            val style = CometChatMentionStyle(
                textColor = textColor,
                textStyle = textStyle,
                backgroundColor = backgroundColor,
                selfTextColor = selfTextColor,
                selfTextStyle = selfTextStyle,
                selfBackgroundColor = selfBackgroundColor
            )

            // Verify all values are preserved
            style.textColor shouldBe textColor
            style.textStyle shouldBe textStyle
            style.backgroundColor shouldBe backgroundColor
            style.selfTextColor shouldBe selfTextColor
            style.selfTextStyle shouldBe selfTextStyle
            style.selfBackgroundColor shouldBe selfBackgroundColor
        }
    }

    /**
     * Property 3: Default Factory Produces Valid Style - Immutability
     *
     * For any CometChatMentionStyle, calling copy() with modified values should
     * produce a new instance while the original remains unchanged.
     *
     * Feature: mention-style-compose, Property 3: Default Factory Produces Valid Style
     * Validates: Requirements 2.2, 2.5, 2.6
     */
    "Property 3: CometChatMentionStyle copy should create new instance while original remains unchanged" {
        val colorArb = Arb.bind(
            Arb.int(0, 255),
            Arb.int(0, 255),
            Arb.int(0, 255),
            Arb.int(0, 255)
        ) { r, g, b, a ->
            Color(r, g, b, a)
        }

        checkAll(100, colorArb) { originalTextColor ->
            val originalStyle = createTestMentionStyle(textColor = originalTextColor)
            val originalTextColorValue = originalStyle.textColor

            // Create a copy with modified textColor
            val newTextColor = Color.Magenta
            val copiedStyle = originalStyle.copy(textColor = newTextColor)

            // Verify the copied style has the new value
            copiedStyle.textColor shouldBe newTextColor

            // Verify the original style is unchanged
            originalStyle.textColor shouldBe originalTextColorValue

            // Verify other properties are preserved in copy
            copiedStyle.textStyle shouldBe originalStyle.textStyle
            copiedStyle.backgroundColor shouldBe originalStyle.backgroundColor
            copiedStyle.selfTextColor shouldBe originalStyle.selfTextColor
            copiedStyle.selfTextStyle shouldBe originalStyle.selfTextStyle
            copiedStyle.selfBackgroundColor shouldBe originalStyle.selfBackgroundColor
        }
    }
})

/**
 * Helper function to create a test mention style with customizable values.
 */
private fun createTestMentionStyle(
    textColor: Color = Color.Blue,
    textStyle: TextStyle = TextStyle(fontSize = 14.sp),
    backgroundColor: Color = Color.Transparent,
    selfTextColor: Color = Color.Red,
    selfTextStyle: TextStyle = TextStyle(fontSize = 14.sp),
    selfBackgroundColor: Color = Color.Transparent
): CometChatMentionStyle {
    return CometChatMentionStyle(
        textColor = textColor,
        textStyle = textStyle,
        backgroundColor = backgroundColor,
        selfTextColor = selfTextColor,
        selfTextStyle = selfTextStyle,
        selfBackgroundColor = selfBackgroundColor
    )
}
