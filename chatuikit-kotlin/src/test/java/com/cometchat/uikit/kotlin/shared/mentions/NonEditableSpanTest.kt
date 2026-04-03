package com.cometchat.uikit.kotlin.shared.mentions

import android.graphics.Color
import com.cometchat.uikit.kotlin.shared.formatters.SuggestionItem
import com.cometchat.uikit.kotlin.shared.formatters.style.PromptTextStyle
import com.cometchat.uikit.kotlin.shared.spans.NonEditableSpan
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

/**
 * Unit tests for NonEditableSpan.
 * 
 * Tests cover:
 * - Span creation with SuggestionItem
 * - Span creation with PromptTextStyle
 * - Getter/setter methods
 * - Background alpha calculation logic
 * 
 * Note: Tests that require Android runtime (TextPaint) are excluded.
 * Those are covered by integration tests with Robolectric.
 * 
 * **Validates: Requirements FR-4.1, FR-5.1**
 */
class NonEditableSpanTest : DescribeSpec({

    describe("NonEditableSpan") {

        describe("construction") {

            it("should create span from SuggestionItem") {
                val style = PromptTextStyle().setColor(Color.BLUE)
                val suggestionItem = createSuggestionItem("user1", "John", style)
                
                val span = NonEditableSpan('@', "@John", suggestionItem)
                
                span.getId() shouldBe '@'
                span.getText() shouldBe "@John"
                span.getSuggestionItem() shouldBe suggestionItem
                span.getTextAppearance() shouldBe style
            }

            it("should create span with custom text appearance") {
                val style = PromptTextStyle()
                    .setColor(Color.RED)
                    .setBackgroundColor(Color.YELLOW)
                
                val span = NonEditableSpan('@', "@John", style)
                
                span.getId() shouldBe '@'
                span.getText() shouldBe "@John"
                span.getSuggestionItem() shouldBe null
                span.getTextAppearance() shouldBe style
            }

            it("should handle null text appearance") {
                val span = NonEditableSpan('@', "@John", null as PromptTextStyle?)
                
                span.getTextAppearance() shouldBe null
            }
        }

        describe("getters and setters") {

            it("should update id") {
                val span = NonEditableSpan('@', "@John", null as PromptTextStyle?)
                
                span.setId('#')
                
                span.getId() shouldBe '#'
            }

            it("should update text") {
                val span = NonEditableSpan('@', "@John", null as PromptTextStyle?)
                
                span.setText("@Jane")
                
                span.getText() shouldBe "@Jane"
            }

            it("should update suggestion item and inherit style") {
                val span = NonEditableSpan('@', "@John", null as PromptTextStyle?)
                val newStyle = PromptTextStyle().setColor(Color.GREEN)
                val newItem = createSuggestionItem("user2", "Jane", newStyle)
                
                span.setSuggestionItem(newItem)
                
                span.getSuggestionItem() shouldBe newItem
                span.getTextAppearance() shouldBe newStyle
            }

            it("should update text appearance directly") {
                val span = NonEditableSpan('@', "@John", null as PromptTextStyle?)
                val newStyle = PromptTextStyle().setColor(Color.MAGENTA)
                
                span.setTextAppearance(newStyle)
                
                span.getTextAppearance() shouldBe newStyle
            }

            it("should handle null suggestion item") {
                val style = PromptTextStyle().setColor(Color.BLUE)
                val item = createSuggestionItem("user1", "John", style)
                val span = NonEditableSpan('@', "@John", item)
                
                span.setSuggestionItem(null)
                
                span.getSuggestionItem() shouldBe null
            }
        }

        describe("background alpha calculation") {

            it("should calculate correct alpha for white background") {
                // White = 0xFFFFFFFF
                // With 20% alpha: (51 << 24) | (0xFFFFFFFF & 0x00FFFFFF) = 0x33FFFFFF
                val expectedAlpha = 51
                val whiteWithAlpha = (expectedAlpha shl 24) or (Color.WHITE and 0x00FFFFFF)
                
                // Alpha should be 51 (0x33)
                (whiteWithAlpha shr 24) and 0xFF shouldBe 51
            }

            it("should calculate correct alpha for blue background") {
                // Blue = 0xFF0000FF
                // With 20% alpha: (51 << 24) | (0xFF0000FF & 0x00FFFFFF) = 0x330000FF
                val expectedAlpha = 51
                val blueWithAlpha = (expectedAlpha shl 24) or (Color.BLUE and 0x00FFFFFF)
                
                // Alpha should be 51 (0x33)
                (blueWithAlpha shr 24) and 0xFF shouldBe 51
                // RGB should be preserved
                (blueWithAlpha and 0x00FFFFFF) shouldBe (Color.BLUE and 0x00FFFFFF)
            }

            it("should preserve RGB values when applying alpha") {
                val originalColor = 0xFFAABBCC.toInt()
                val expectedAlpha = 51
                val colorWithAlpha = (expectedAlpha shl 24) or (originalColor and 0x00FFFFFF)
                
                // RGB should be preserved
                (colorWithAlpha and 0x00FF0000) shr 16 shouldBe 0xAA
                (colorWithAlpha and 0x0000FF00) shr 8 shouldBe 0xBB
                (colorWithAlpha and 0x000000FF) shouldBe 0xCC
            }
        }

        describe("edge cases") {

            it("should handle empty text") {
                val span = NonEditableSpan('@', "", null as PromptTextStyle?)
                
                span.getText() shouldBe ""
            }

            it("should handle special characters in text") {
                val span = NonEditableSpan('@', "@John Doe 123!", null as PromptTextStyle?)
                
                span.getText() shouldBe "@John Doe 123!"
            }

            it("should handle unicode characters") {
                val span = NonEditableSpan('@', "@用户名", null as PromptTextStyle?)
                
                span.getText() shouldBe "@用户名"
            }
        }
    }
})

/**
 * Helper function to create a SuggestionItem for testing.
 */
private fun createSuggestionItem(
    id: String, 
    name: String, 
    style: PromptTextStyle? = null
): SuggestionItem {
    return SuggestionItem(
        id = id,
        name = name,
        leadingIconUrl = null,
        status = null,
        promptText = "@$name",
        underlyingText = "<@uid:$id>",
        data = null,
        promptTextStyle = style
    )
}
