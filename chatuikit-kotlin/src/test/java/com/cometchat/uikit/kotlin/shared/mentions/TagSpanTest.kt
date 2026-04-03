package com.cometchat.uikit.kotlin.shared.mentions

import android.graphics.Color
import com.cometchat.uikit.kotlin.shared.formatters.SuggestionItem
import com.cometchat.uikit.kotlin.shared.formatters.style.PromptTextStyle
import com.cometchat.uikit.kotlin.shared.spans.TagSpan
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

/**
 * Unit tests for TagSpan.
 * 
 * Tests cover:
 * - Span creation with and without click handler
 * - Getter/setter methods
 * - Style inheritance from SuggestionItem
 * 
 * Note: Tests that require Android runtime (TextPaint, View click) are excluded.
 * Those are covered by integration tests with Robolectric.
 * 
 * **Validates: Requirements FR-8.2, FR-8.5**
 */
class TagSpanTest : DescribeSpec({

    describe("TagSpan") {

        describe("construction") {

            it("should create span without click handler") {
                val suggestionItem = createSuggestionItem("user1", "John", null)
                
                val span = TagSpan('@', "@John", suggestionItem, null)
                
                span.getId() shouldBe '@'
                span.getText() shouldBe "@John"
            }

            it("should inherit style from suggestion item") {
                val style = PromptTextStyle().setColor(Color.RED)
                val suggestionItem = createSuggestionItem("user1", "John", style)
                
                val span = TagSpan('@', "@John", suggestionItem, null)
                
                span.getTextAppearance() shouldBe style
            }

            it("should store suggestion item correctly") {
                val style = PromptTextStyle().setColor(Color.BLUE)
                val suggestionItem = createSuggestionItem("user1", "John", style)
                
                val span = TagSpan('@', "@John", suggestionItem, null)
                
                span.getSuggestionItem() shouldBe suggestionItem
            }

            it("should handle suggestion item without style") {
                val suggestionItem = createSuggestionItem("user1", "John", null)
                
                val span = TagSpan('@', "@John", suggestionItem, null)
                
                span.getTextAppearance() shouldBe null
            }
        }

        describe("getters and setters") {

            it("should update id") {
                val suggestionItem = createSuggestionItem("user1", "John", null)
                val span = TagSpan('@', "@John", suggestionItem, null)
                
                span.setId('#')
                
                span.getId() shouldBe '#'
            }

            it("should update text") {
                val suggestionItem = createSuggestionItem("user1", "John", null)
                val span = TagSpan('@', "@John", suggestionItem, null)
                
                span.setText("@Jane")
                
                span.getText() shouldBe "@Jane"
            }

            it("should update suggestion item and inherit style") {
                val oldItem = createSuggestionItem("user1", "John", null)
                val span = TagSpan('@', "@John", oldItem, null)
                
                val newStyle = PromptTextStyle().setColor(Color.CYAN)
                val newItem = createSuggestionItem("user2", "Jane", newStyle)
                
                span.setSuggestionItem(newItem)
                
                span.getSuggestionItem() shouldBe newItem
                span.getTextAppearance() shouldBe newStyle
            }

            it("should update text appearance directly") {
                val suggestionItem = createSuggestionItem("user1", "John", null)
                val span = TagSpan('@', "@John", suggestionItem, null)
                val newStyle = PromptTextStyle().setColor(Color.YELLOW)
                
                span.setTextAppearance(newStyle)
                
                span.getTextAppearance() shouldBe newStyle
            }
        }

        describe("edge cases") {

            it("should handle empty text") {
                val suggestionItem = SuggestionItem(
                    id = "user1",
                    name = "",
                    promptText = "",
                    underlyingText = "<@uid:user1>",
                    data = null
                )
                val span = TagSpan('@', "", suggestionItem, null)
                
                span.getText() shouldBe ""
            }

            it("should handle special characters in text") {
                val suggestionItem = createSuggestionItem("user1", "John Doe 123!", null)
                val span = TagSpan('@', "@John Doe 123!", suggestionItem, null)
                
                span.getText() shouldBe "@John Doe 123!"
            }

            it("should handle unicode characters") {
                val suggestionItem = SuggestionItem(
                    id = "user1",
                    name = "用户名",
                    promptText = "@用户名",
                    underlyingText = "<@uid:user1>",
                    data = null
                )
                val span = TagSpan('@', "@用户名", suggestionItem, null)
                
                span.getText() shouldBe "@用户名"
            }

            it("should handle suggestion item with null data") {
                val suggestionItem = SuggestionItem(
                    id = "user1",
                    name = "John",
                    promptText = "@John",
                    underlyingText = "<@uid:user1>",
                    data = null
                )
                val span = TagSpan('@', "@John", suggestionItem, null)
                
                span.getSuggestionItem().data shouldBe null
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
