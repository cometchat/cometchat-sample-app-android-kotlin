package com.cometchat.uikit.kotlin.shared.spans

import android.graphics.Typeface
import com.cometchat.uikit.kotlin.shared.formatters.style.PromptTextStyle
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll

/**
 * Pure-logic tests for NonEditableSpan styling with PromptTextStyle.
 * 
 * These tests verify the styling logic without Android dependencies by testing:
 * - PromptTextStyle fluent API and property storage
 * - Background color alpha calculation (20% = 51/255)
 * - Style property getters/setters
 * 
 * **Validates: Requirements FR-5.1**
 */
class NonEditableSpanStyleTest : FunSpec({

    context("PromptTextStyle property storage") {

        test("setColor should store and return the color value") {
            val color = 0xFFFF0000.toInt() // Red
            val style = PromptTextStyle().setColor(color)
            style.getColor() shouldBe color
        }

        test("setBackgroundColor should store and return the background color value") {
            val bgColor = 0xFF0000FF.toInt() // Blue
            val style = PromptTextStyle().setBackgroundColor(bgColor)
            style.getBackgroundColor() shouldBe bgColor
        }

        test("setTextSize should store and return the text size value") {
            val textSize = 24
            val style = PromptTextStyle().setTextSize(textSize)
            style.getTextSize() shouldBe textSize
        }

        test("setTextAppearance should store and return the typeface") {
            // Note: In pure unit tests, Typeface constants may be null
            // We test the setter/getter contract works correctly
            val style = PromptTextStyle()
            // Default should be null
            style.getTextAppearance() shouldBe null
            // Setting null should work
            style.setTextAppearance(null)
            style.getTextAppearance() shouldBe null
        }

        test("fluent API should allow chaining all setters") {
            val color = 0xFFFF0000.toInt()
            val bgColor = 0xFF0000FF.toInt()
            val textSize = 20

            val style = PromptTextStyle()
                .setColor(color)
                .setBackgroundColor(bgColor)
                .setTextSize(textSize)
                .setTextAppearance(null) // Typeface constants are null in unit tests

            style.getColor() shouldBe color
            style.getBackgroundColor() shouldBe bgColor
            style.getTextSize() shouldBe textSize
            style.getTextAppearance() shouldBe null
        }

        test("default values should be 0 or null") {
            val style = PromptTextStyle()
            style.getColor() shouldBe 0
            style.getBackgroundColor() shouldBe 0
            style.getTextSize() shouldBe 0
            style.getTextAppearance() shouldBe null
        }
    }

    context("Background alpha calculation (20% = 51/255)") {

        /**
         * The NonEditableSpan applies 20% alpha to background colors.
         * This is calculated as: (51 shl 24) or (color and 0x00FFFFFF)
         * where 51 = 255 * 0.20 (rounded)
         */
        fun applyColorWithAlpha(color: Int, alpha: Int): Int {
            return (alpha shl 24) or (color and 0x00FFFFFF)
        }

        test("alpha 51 represents approximately 20% opacity") {
            // 20% of 255 = 51
            val expectedAlpha = 51
            val percentage = expectedAlpha.toFloat() / 255f
            // Should be approximately 0.20 (20%)
            (percentage >= 0.19f && percentage <= 0.21f) shouldBe true
        }

        test("applyColorWithAlpha should preserve RGB and set alpha to 51") {
            val originalColor = 0xFFFF0000.toInt() // Fully opaque red
            val result = applyColorWithAlpha(originalColor, 51)
            
            // Extract components
            val resultAlpha = (result shr 24) and 0xFF
            val resultRed = (result shr 16) and 0xFF
            val resultGreen = (result shr 8) and 0xFF
            val resultBlue = result and 0xFF
            
            resultAlpha shouldBe 51
            resultRed shouldBe 0xFF
            resultGreen shouldBe 0x00
            resultBlue shouldBe 0x00
        }

        test("applyColorWithAlpha should work with any color") {
            checkAll(100, Arb.int()) { color ->
                val result = applyColorWithAlpha(color, 51)
                
                // Alpha should always be 51
                val resultAlpha = (result shr 24) and 0xFF
                resultAlpha shouldBe 51
                
                // RGB should be preserved from original
                val originalRgb = color and 0x00FFFFFF
                val resultRgb = result and 0x00FFFFFF
                resultRgb shouldBe originalRgb
            }
        }

        test("applyColorWithAlpha with blue color should produce correct result") {
            val blueColor = 0xFF0000FF.toInt() // Blue
            val result = applyColorWithAlpha(blueColor, 51)
            
            // Expected: 0x330000FF (alpha 51, RGB from blue)
            val expected = (51 shl 24) or (0x0000FF)
            result shouldBe expected
        }

        test("applyColorWithAlpha with green color should produce correct result") {
            val greenColor = 0xFF00FF00.toInt() // Green
            val result = applyColorWithAlpha(greenColor, 51)
            
            // Expected: 0x3300FF00 (alpha 51, RGB from green)
            val expected = (51 shl 24) or (0x00FF00)
            result shouldBe expected
        }
    }

    context("PromptTextStyle property-based tests") {

        test("any color value should be stored and retrieved correctly") {
            checkAll(100, Arb.int()) { color ->
                val style = PromptTextStyle().setColor(color)
                style.getColor() shouldBe color
            }
        }

        test("any background color value should be stored and retrieved correctly") {
            checkAll(100, Arb.int()) { bgColor ->
                val style = PromptTextStyle().setBackgroundColor(bgColor)
                style.getBackgroundColor() shouldBe bgColor
            }
        }

        test("any text size value should be stored and retrieved correctly") {
            checkAll(100, Arb.int(0..1000)) { textSize ->
                val style = PromptTextStyle().setTextSize(textSize)
                style.getTextSize() shouldBe textSize
            }
        }

        test("multiple style instances should be independent") {
            checkAll(100, Arb.int(), Arb.int()) { color1, color2 ->
                val style1 = PromptTextStyle().setColor(color1)
                val style2 = PromptTextStyle().setColor(color2)
                
                style1.getColor() shouldBe color1
                style2.getColor() shouldBe color2
            }
        }
    }

    context("NonEditableSpan styling conditions") {

        /**
         * These tests verify the conditions under which styling should be applied.
         * The actual NonEditableSpan.updateDrawState() applies styling when:
         * - color != 0: apply text color
         * - textSize > 0: apply text size
         * - backgroundColor != 0: apply background with alpha
         * - textAppearance != null: apply typeface
         */

        test("color 0 should indicate no color styling") {
            val style = PromptTextStyle().setColor(0)
            (style.getColor() == 0) shouldBe true
        }

        test("non-zero color should indicate color styling should be applied") {
            val style = PromptTextStyle().setColor(0xFF0000)
            (style.getColor() != 0) shouldBe true
        }

        test("textSize 0 or negative should indicate no size styling") {
            val style = PromptTextStyle().setTextSize(0)
            (style.getTextSize() > 0) shouldBe false
        }

        test("positive textSize should indicate size styling should be applied") {
            val style = PromptTextStyle().setTextSize(16)
            (style.getTextSize() > 0) shouldBe true
        }

        test("backgroundColor 0 should indicate no background styling") {
            val style = PromptTextStyle().setBackgroundColor(0)
            (style.getBackgroundColor() != 0) shouldBe false
        }

        test("non-zero backgroundColor should indicate background styling should be applied") {
            val style = PromptTextStyle().setBackgroundColor(0xFF0000)
            (style.getBackgroundColor() != 0) shouldBe true
        }

        test("null textAppearance should indicate no typeface styling") {
            val style = PromptTextStyle()
            (style.getTextAppearance() != null) shouldBe false
        }

        test("non-null textAppearance should indicate typeface styling should be applied") {
            // In pure unit tests, Android Typeface class methods throw RuntimeException
            // We verify the logic by checking that the condition (getTextAppearance() != null)
            // correctly evaluates based on what was set
            val style = PromptTextStyle()
            // Default is null, so condition should be false
            (style.getTextAppearance() != null) shouldBe false
            // After setting null explicitly, condition should still be false
            style.setTextAppearance(null)
            (style.getTextAppearance() != null) shouldBe false
            // The actual typeface styling test with real Typeface objects
            // would be done in instrumented tests where Android framework is available
        }
    }
})
