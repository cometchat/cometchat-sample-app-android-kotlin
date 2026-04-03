package com.cometchat.uikit.compose.shared.mentions

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.cometchat.uikit.compose.presentation.shared.formatters.SuggestionItem
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

/**
 * Unit tests for MentionTextFieldState.
 * 
 * Tests cover:
 * - Cursor position validation (prevent cursor inside mentions)
 * - Mention insertion at correct position
 * - Backspace handling for mention deletion
 * - Selected mentions tracking (add, remove, clear)
 * - Processed text generation
 * 
 * **Validates: Requirements FR-4, FR-5**
 */
class MentionTextFieldStateTest : DescribeSpec({

    val defaultMentionStyle = SpanStyle(color = Color.Blue)

    describe("MentionTextFieldState") {

        describe("cursor position validation") {

            it("should allow cursor at mention boundaries") {
                val state = MentionTextFieldState()
                
                // Insert a mention
                state.updateTextFieldValue(TextFieldValue("Hello ", TextRange(6)))
                state.insertMention(
                    suggestionItem = createSuggestionItem("user1", "John"),
                    triggerIndex = 6,
                    mentionStyle = defaultMentionStyle
                )
                
                // Cursor at start of mention should be allowed
                val valueAtStart = TextFieldValue(state.textFieldValue.text, TextRange(6))
                val validatedAtStart = state.onValueChange(valueAtStart)
                validatedAtStart.selection.start shouldBe 6
                
                // Cursor at end of mention should be allowed
                val mentionEnd = 6 + "@John".length
                val valueAtEnd = TextFieldValue(state.textFieldValue.text, TextRange(mentionEnd))
                val validatedAtEnd = state.onValueChange(valueAtEnd)
                validatedAtEnd.selection.start shouldBe mentionEnd
            }

            it("should move cursor to nearest edge when inside mention") {
                val state = MentionTextFieldState()
                
                // Set up text with a mention
                state.updateTextFieldValue(TextFieldValue("Hello ", TextRange(6)))
                state.insertMention(
                    suggestionItem = createSuggestionItem("user1", "John"),
                    triggerIndex = 6,
                    mentionStyle = defaultMentionStyle
                )
                
                // The mention is "@John" at positions 6-10
                // Try to place cursor inside the mention (position 8, between @ and J)
                val valueInsideMention = TextFieldValue(state.textFieldValue.text, TextRange(8))
                val validated = state.onValueChange(valueInsideMention)
                
                // Cursor should be moved to nearest edge (either 6 or 11)
                val cursorPos = validated.selection.start
                (cursorPos == 6 || cursorPos == 11) shouldBe true
            }

            it("should handle selection range crossing mention") {
                val state = MentionTextFieldState()
                
                // Set up text with a mention
                state.updateTextFieldValue(TextFieldValue("Hello ", TextRange(6)))
                state.insertMention(
                    suggestionItem = createSuggestionItem("user1", "John"),
                    triggerIndex = 6,
                    mentionStyle = defaultMentionStyle
                )
                
                // Try to select from before mention to inside mention
                val selectionCrossingMention = TextFieldValue(
                    state.textFieldValue.text, 
                    TextRange(4, 8) // Selection from "lo" to inside "@John"
                )
                val validated = state.onValueChange(selectionCrossingMention)
                
                // Selection end should be adjusted to not cross into mention
                validated.selection.start shouldBe 4
            }
        }

        describe("mention insertion") {

            it("should insert mention at trigger position") {
                val state = MentionTextFieldState()
                
                // Simulate typing "Hello @"
                state.updateTextFieldValue(TextFieldValue("Hello @", TextRange(7)))
                
                // Insert mention at position 6 (where @ is)
                state.insertMention(
                    suggestionItem = createSuggestionItem("user1", "John"),
                    triggerIndex = 6,
                    mentionStyle = defaultMentionStyle
                )
                
                // Text should be "Hello @John "
                state.textFieldValue.text shouldBe "Hello @John "
                
                // Cursor should be after the mention and space
                state.textFieldValue.selection.start shouldBe 12
            }

            it("should replace query text with mention") {
                val state = MentionTextFieldState()
                
                // Simulate typing "Hello @joh" (partial query)
                state.updateTextFieldValue(TextFieldValue("Hello @joh", TextRange(10)))
                
                // Insert mention at position 6 (where @ is)
                state.insertMention(
                    suggestionItem = createSuggestionItem("user1", "John"),
                    triggerIndex = 6,
                    mentionStyle = defaultMentionStyle
                )
                
                // Text should be "Hello @John " (query "joh" replaced)
                state.textFieldValue.text shouldBe "Hello @John "
            }

            it("should add space after inserted mention") {
                val state = MentionTextFieldState()
                
                state.updateTextFieldValue(TextFieldValue("@", TextRange(1)))
                state.insertMention(
                    suggestionItem = createSuggestionItem("user1", "John"),
                    triggerIndex = 0,
                    mentionStyle = defaultMentionStyle
                )
                
                // Should end with space
                state.textFieldValue.text.endsWith(" ") shouldBe true
            }

            it("should track inserted mention in selectedMentions") {
                val state = MentionTextFieldState()
                
                state.updateTextFieldValue(TextFieldValue("@", TextRange(1)))
                state.insertMention(
                    suggestionItem = createSuggestionItem("user1", "John"),
                    triggerIndex = 0,
                    mentionStyle = defaultMentionStyle
                )
                
                state.selectedMentions shouldHaveSize 1
                state.selectedMentions[0].id shouldBe "user1"
                state.selectedMentions[0].promptText shouldBe "@John"
            }

            it("should handle multiple mentions") {
                val state = MentionTextFieldState()
                
                // Insert first mention
                state.updateTextFieldValue(TextFieldValue("@", TextRange(1)))
                state.insertMention(
                    suggestionItem = createSuggestionItem("user1", "John"),
                    triggerIndex = 0,
                    mentionStyle = defaultMentionStyle
                )
                
                // Insert second mention
                val currentText = state.textFieldValue.text
                state.updateTextFieldValue(TextFieldValue(currentText + "@", TextRange(currentText.length + 1)))
                state.insertMention(
                    suggestionItem = createSuggestionItem("user2", "Jane"),
                    triggerIndex = currentText.length,
                    mentionStyle = defaultMentionStyle
                )
                
                state.selectedMentions shouldHaveSize 2
                state.selectedMentions.map { it.id } shouldContain "user1"
                state.selectedMentions.map { it.id } shouldContain "user2"
            }
        }

        describe("backspace handling") {

            it("should delete entire mention on backspace at boundary") {
                val state = MentionTextFieldState()
                var deletedMention: SelectedMention? = null
                
                // Insert a mention
                state.updateTextFieldValue(TextFieldValue("@", TextRange(1)))
                state.insertMention(
                    suggestionItem = createSuggestionItem("user1", "John"),
                    triggerIndex = 0,
                    mentionStyle = defaultMentionStyle
                )
                
                // Simulate backspace at end of mention
                // Current text is "@John " with cursor at 6
                // After backspace, text would be "@Joh " (one char deleted from mention)
                val textAfterBackspace = "@Joh "
                val cursorAfterBackspace = 4
                
                state.onValueChange(
                    TextFieldValue(textAfterBackspace, TextRange(cursorAfterBackspace)),
                    onMentionDeleted = { deletedMention = it }
                )
                
                // Mention should be deleted
                deletedMention shouldNotBe null
                deletedMention?.id shouldBe "user1"
            }

            it("should remove mention from tracking when deleted") {
                val state = MentionTextFieldState()
                
                // Insert a mention
                state.updateTextFieldValue(TextFieldValue("@", TextRange(1)))
                state.insertMention(
                    suggestionItem = createSuggestionItem("user1", "John"),
                    triggerIndex = 0,
                    mentionStyle = defaultMentionStyle
                )
                
                state.selectedMentions shouldHaveSize 1
                
                // Simulate backspace deleting part of mention
                val textAfterBackspace = "@Joh "
                state.onValueChange(TextFieldValue(textAfterBackspace, TextRange(4)))
                
                // Mention should be removed from tracking
                state.selectedMentions.shouldBeEmpty()
            }
        }

        describe("selected mentions tracking") {

            it("should return correct mention count") {
                val state = MentionTextFieldState()
                
                state.getMentionCount() shouldBe 0
                
                state.updateTextFieldValue(TextFieldValue("@", TextRange(1)))
                state.insertMention(
                    suggestionItem = createSuggestionItem("user1", "John"),
                    triggerIndex = 0,
                    mentionStyle = defaultMentionStyle
                )
                
                state.getMentionCount() shouldBe 1
            }

            it("should clear all mentions on clear()") {
                val state = MentionTextFieldState()
                
                // Insert mentions
                state.updateTextFieldValue(TextFieldValue("@", TextRange(1)))
                state.insertMention(
                    suggestionItem = createSuggestionItem("user1", "John"),
                    triggerIndex = 0,
                    mentionStyle = defaultMentionStyle
                )
                
                state.selectedMentions shouldHaveSize 1
                
                state.clear()
                
                state.selectedMentions.shouldBeEmpty()
                state.textFieldValue.text shouldBe ""
            }

            it("should check if position is in mention") {
                val state = MentionTextFieldState()
                
                state.updateTextFieldValue(TextFieldValue("Hello @", TextRange(7)))
                state.insertMention(
                    suggestionItem = createSuggestionItem("user1", "John"),
                    triggerIndex = 6,
                    mentionStyle = defaultMentionStyle
                )
                
                // Position 5 is before mention
                state.isPositionInMention(5) shouldBe false
                
                // Position 7 is inside mention (@John is at 6-10)
                state.isPositionInMention(7) shouldBe true
                
                // Position 12 is after mention
                state.isPositionInMention(12) shouldBe false
            }

            it("should get mention at position") {
                val state = MentionTextFieldState()
                
                state.updateTextFieldValue(TextFieldValue("Hello @", TextRange(7)))
                state.insertMention(
                    suggestionItem = createSuggestionItem("user1", "John"),
                    triggerIndex = 6,
                    mentionStyle = defaultMentionStyle
                )
                
                val mentionAtPos7 = state.getMentionAt(7)
                mentionAtPos7 shouldNotBe null
                mentionAtPos7?.id shouldBe "user1"
                
                val mentionAtPos5 = state.getMentionAt(5)
                mentionAtPos5 shouldBe null
            }
        }

        describe("processed text generation") {

            it("should replace prompt text with underlying text") {
                val state = MentionTextFieldState()
                
                state.updateTextFieldValue(TextFieldValue("Hello @", TextRange(7)))
                state.insertMention(
                    suggestionItem = createSuggestionItem("user1", "John"),
                    triggerIndex = 6,
                    mentionStyle = defaultMentionStyle
                )
                
                val processed = state.getProcessedText()
                
                // "@John" should be replaced with "<@uid:user1>"
                processed shouldBe "Hello <@uid:user1> "
            }

            it("should handle multiple mentions in processed text") {
                val state = MentionTextFieldState()
                
                // Insert first mention
                state.updateTextFieldValue(TextFieldValue("@", TextRange(1)))
                state.insertMention(
                    suggestionItem = createSuggestionItem("user1", "John"),
                    triggerIndex = 0,
                    mentionStyle = defaultMentionStyle
                )
                
                // Add text and second mention
                val text1 = state.textFieldValue.text // "@John "
                state.updateTextFieldValue(TextFieldValue(text1 + "and @", TextRange(text1.length + 5)))
                state.insertMention(
                    suggestionItem = createSuggestionItem("user2", "Jane"),
                    triggerIndex = text1.length + 4,
                    mentionStyle = defaultMentionStyle
                )
                
                val processed = state.getProcessedText()
                
                processed.contains("<@uid:user1>") shouldBe true
                processed.contains("<@uid:user2>") shouldBe true
            }

            it("should return prompt to underlying map") {
                val state = MentionTextFieldState()
                
                state.updateTextFieldValue(TextFieldValue("@", TextRange(1)))
                state.insertMention(
                    suggestionItem = createSuggestionItem("user1", "John"),
                    triggerIndex = 0,
                    mentionStyle = defaultMentionStyle
                )
                
                val map = state.getPromptToUnderlyingMap()
                
                map["@John"] shouldBe "<@uid:user1>"
            }
        }

        describe("annotated string building") {

            it("should build annotated string with styled mentions") {
                val state = MentionTextFieldState()
                
                state.updateTextFieldValue(TextFieldValue("Hello @", TextRange(7)))
                state.insertMention(
                    suggestionItem = createSuggestionItem("user1", "John"),
                    triggerIndex = 6,
                    mentionStyle = defaultMentionStyle
                )
                
                val annotated = state.buildAnnotatedString(defaultMentionStyle)
                
                annotated.text shouldBe "Hello @John "
                
                // Should have annotation for the mention
                val annotations = annotated.getStringAnnotations(
                    MentionTextFieldState.MENTION_ANNOTATION_TAG,
                    0,
                    annotated.length
                )
                annotations shouldHaveSize 1
                annotations[0].item shouldBe "user1"
            }
        }

        describe("edge cases") {

            it("should handle empty text") {
                val state = MentionTextFieldState()
                
                state.textFieldValue.text shouldBe ""
                state.selectedMentions.shouldBeEmpty()
                state.getProcessedText() shouldBe ""
            }

            it("should handle mention at start of text") {
                val state = MentionTextFieldState()
                
                state.updateTextFieldValue(TextFieldValue("@", TextRange(1)))
                state.insertMention(
                    suggestionItem = createSuggestionItem("user1", "John"),
                    triggerIndex = 0,
                    mentionStyle = defaultMentionStyle
                )
                
                state.textFieldValue.text shouldBe "@John "
                state.selectedMentions[0].range.first shouldBe 0
            }

            it("should handle mention at end of text") {
                val state = MentionTextFieldState()
                
                state.updateTextFieldValue(TextFieldValue("Hello @", TextRange(7)))
                state.insertMention(
                    suggestionItem = createSuggestionItem("user1", "John"),
                    triggerIndex = 6,
                    mentionStyle = defaultMentionStyle
                )
                
                // Cursor should be at the end
                state.textFieldValue.selection.start shouldBe state.textFieldValue.text.length
            }
        }
    }
})

/**
 * Helper function to create a SuggestionItem for testing.
 */
private fun createSuggestionItem(id: String, name: String): SuggestionItem {
    return SuggestionItem(
        id = id,
        name = name,
        leadingIconUrl = null,
        status = null,
        promptText = "@$name",
        underlyingText = "<@uid:$id>",
        data = null,
        promptTextStyle = null
    )
}
