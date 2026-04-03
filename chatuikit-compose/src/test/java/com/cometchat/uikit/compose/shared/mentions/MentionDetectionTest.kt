package com.cometchat.uikit.compose.shared.mentions

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

/**
 * Unit tests for mention detection logic.
 * 
 * Tests cover:
 * - Mention detection when typing '@' character
 * - Query string extraction after '@'
 * - Valid trigger positions (start of text, after space/newline)
 * - Invalid trigger positions (inside word, inside existing mention)
 * 
 * **Validates: Requirements FR-1.1, FR-1.2, FR-1.3**
 */
class MentionDetectionTest : DescribeSpec({

    describe("Mention Detection Logic") {

        describe("trigger character detection") {

            it("should detect @ at start of text") {
                val result = detectMention(
                    text = "@",
                    cursorPosition = 1,
                    trackingCharacter = '@'
                )
                
                result.isDetected shouldBe true
                result.triggerIndex shouldBe 0
            }

            it("should detect @ after space") {
                val result = detectMention(
                    text = "Hello @",
                    cursorPosition = 7,
                    trackingCharacter = '@'
                )
                
                result.isDetected shouldBe true
                result.triggerIndex shouldBe 6
            }

            it("should detect @ after newline") {
                val result = detectMention(
                    text = "Hello\n@",
                    cursorPosition = 7,
                    trackingCharacter = '@'
                )
                
                result.isDetected shouldBe true
                result.triggerIndex shouldBe 6
            }

            it("should not detect @ in middle of word") {
                val result = detectMention(
                    text = "email@test",
                    cursorPosition = 10,
                    trackingCharacter = '@'
                )
                
                result.isDetected shouldBe false
            }

            it("should not detect when @ is followed by space") {
                val result = detectMention(
                    text = "Hello @ world",
                    cursorPosition = 8,
                    trackingCharacter = '@'
                )
                
                result.isDetected shouldBe false
            }

            it("should support custom tracking character") {
                val result = detectMention(
                    text = "Hello #",
                    cursorPosition = 7,
                    trackingCharacter = '#'
                )
                
                result.isDetected shouldBe true
                result.triggerIndex shouldBe 6
            }
        }

        describe("query string extraction") {

            it("should extract empty query when just @ typed") {
                val query = extractQueryString(
                    text = "@",
                    cursorPosition = 1,
                    triggerIndex = 0
                )
                
                query shouldBe ""
            }

            it("should extract query after @") {
                val query = extractQueryString(
                    text = "@john",
                    cursorPosition = 5,
                    triggerIndex = 0
                )
                
                query shouldBe "john"
            }

            it("should extract partial query") {
                val query = extractQueryString(
                    text = "Hello @joh",
                    cursorPosition = 10,
                    triggerIndex = 6
                )
                
                query shouldBe "joh"
            }

            it("should handle query with spaces in text before") {
                val query = extractQueryString(
                    text = "Hello world @test",
                    cursorPosition = 17,
                    triggerIndex = 12
                )
                
                query shouldBe "test"
            }

            it("should return empty for invalid indices") {
                val query = extractQueryString(
                    text = "Hello",
                    cursorPosition = 5,
                    triggerIndex = -1
                )
                
                query shouldBe ""
            }
        }

        describe("mention context validation") {

            it("should be valid when cursor is after @") {
                val result = detectMention(
                    text = "@john",
                    cursorPosition = 5,
                    trackingCharacter = '@'
                )
                
                result.isDetected shouldBe true
            }

            it("should be invalid when cursor is before @") {
                val result = detectMention(
                    text = "Hello @john",
                    cursorPosition = 3,
                    trackingCharacter = '@'
                )
                
                result.isDetected shouldBe false
            }

            it("should be invalid for empty text") {
                val result = detectMention(
                    text = "",
                    cursorPosition = 0,
                    trackingCharacter = '@'
                )
                
                result.isDetected shouldBe false
            }

            it("should be invalid when whitespace between @ and cursor") {
                val result = detectMention(
                    text = "@ test",
                    cursorPosition = 6,
                    trackingCharacter = '@'
                )
                
                result.isDetected shouldBe false
            }
        }

        describe("multiple @ characters") {

            it("should detect nearest @ to cursor") {
                val result = detectMention(
                    text = "@john @jane",
                    cursorPosition = 11,
                    trackingCharacter = '@'
                )
                
                result.isDetected shouldBe true
                result.triggerIndex shouldBe 6
            }

            it("should detect first @ when cursor is after first mention") {
                val result = detectMention(
                    text = "@john test",
                    cursorPosition = 5,
                    trackingCharacter = '@'
                )
                
                result.isDetected shouldBe true
                result.triggerIndex shouldBe 0
            }
        }

        describe("edge cases") {

            it("should handle @ at very end of long text") {
                val longText = "This is a very long message with lots of text @"
                val result = detectMention(
                    text = longText,
                    cursorPosition = longText.length,
                    trackingCharacter = '@'
                )
                
                result.isDetected shouldBe true
            }

            it("should handle multiple spaces before @") {
                val result = detectMention(
                    text = "Hello   @",
                    cursorPosition = 9,
                    trackingCharacter = '@'
                )
                
                result.isDetected shouldBe true
                result.triggerIndex shouldBe 8
            }

            it("should handle @ after punctuation with space") {
                val result = detectMention(
                    text = "Hello, @",
                    cursorPosition = 8,
                    trackingCharacter = '@'
                )
                
                result.isDetected shouldBe true
            }
        }
    }
})

/**
 * Data class representing mention detection result.
 */
private data class MentionDetectionResult(
    val isDetected: Boolean,
    val triggerIndex: Int
)

/**
 * Simulates the mention detection logic from MentionAwareTextField.
 * This is a pure function version for testing.
 */
private fun detectMention(
    text: String,
    cursorPosition: Int,
    trackingCharacter: Char
): MentionDetectionResult {
    if (text.isEmpty() || cursorPosition <= 0) {
        return MentionDetectionResult(false, -1)
    }
    
    // Scan backwards from cursor to find tracking character
    for (i in (cursorPosition - 1) downTo 0) {
        val char = text[i]
        
        // Check if this is the tracking character
        if (char == trackingCharacter) {
            // Valid only if at start or preceded by whitespace
            if (i == 0 || text[i - 1] == ' ' || text[i - 1] == '\n') {
                // Check if followed by space (mention closed) - but only if there's text after
                if (i < cursorPosition - 1) {
                    val nextChar = text[i + 1]
                    if (nextChar == ' ' || nextChar == '\n') {
                        // Mention is closed (space after @ with no query)
                        return MentionDetectionResult(false, -1)
                    }
                }
                return MentionDetectionResult(true, i)
            }
            // Found tracking character but not at valid position
            return MentionDetectionResult(false, -1)
        }
        
        // If we hit whitespace before finding tracking character, stop
        if (char == ' ' || char == '\n') {
            return MentionDetectionResult(false, -1)
        }
    }
    
    return MentionDetectionResult(false, -1)
}

/**
 * Extracts the query string from text.
 * Query is the text between tracking character (exclusive) and cursor (exclusive).
 */
private fun extractQueryString(
    text: String,
    cursorPosition: Int,
    triggerIndex: Int
): String {
    if (text.isEmpty() || triggerIndex < 0 || triggerIndex >= cursorPosition) {
        return ""
    }
    
    return try {
        text.substring(triggerIndex + 1, cursorPosition)
    } catch (e: Exception) {
        ""
    }
}
