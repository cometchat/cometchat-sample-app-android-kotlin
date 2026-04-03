package com.cometchat.uikit.kotlin.shared.mentions

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

/**
 * Unit tests for mention detection logic in chatuikit-kotlin.
 * 
 * Tests cover:
 * - Mention detection when typing '@' character
 * - Query string extraction after '@'
 * - Valid trigger positions (start of text, after space/newline)
 * - Invalid trigger positions (inside word, inside existing mention)
 * 
 * Based on the detection algorithm from design document section 3.1.
 * 
 * **Validates: Requirements FR-1.1, FR-1.2, FR-1.3**
 */
class MentionDetectionLogicTest : DescribeSpec({

    describe("Mention Detection Algorithm") {

        describe("detectMention") {

            it("should detect @ at start of text") {
                val result = detectMention(
                    text = "@",
                    cursorPosition = 1,
                    trackingCharacter = '@',
                    spanRanges = emptyList()
                )
                
                result shouldBe 0
            }

            it("should detect @ after space") {
                val result = detectMention(
                    text = "Hello @john",
                    cursorPosition = 11,
                    trackingCharacter = '@',
                    spanRanges = emptyList()
                )
                
                result shouldBe 6
            }

            it("should detect @ after newline") {
                val result = detectMention(
                    text = "Hello\n@john",
                    cursorPosition = 11,
                    trackingCharacter = '@',
                    spanRanges = emptyList()
                )
                
                result shouldBe 6
            }

            it("should not detect @ in middle of word") {
                val result = detectMention(
                    text = "email@test.com",
                    cursorPosition = 14,
                    trackingCharacter = '@',
                    spanRanges = emptyList()
                )
                
                result shouldBe -1
            }

            it("should not detect when cursor is inside existing span") {
                // Simulate a span at positions 6-10 (@John)
                val spanRanges = listOf(6..10)
                
                val result = detectMention(
                    text = "Hello @John world",
                    cursorPosition = 8, // Inside the span
                    trackingCharacter = '@',
                    spanRanges = spanRanges
                )
                
                result shouldBe -1
            }

            it("should not detect when @ is followed by space") {
                val result = detectMention(
                    text = "Hello @ world",
                    cursorPosition = 8,
                    trackingCharacter = '@',
                    spanRanges = emptyList()
                )
                
                result shouldBe -1
            }

            it("should return -1 for empty text") {
                val result = detectMention(
                    text = "",
                    cursorPosition = 0,
                    trackingCharacter = '@',
                    spanRanges = emptyList()
                )
                
                result shouldBe -1
            }

            it("should support custom tracking character") {
                val result = detectMention(
                    text = "Hello #tag",
                    cursorPosition = 10,
                    trackingCharacter = '#',
                    spanRanges = emptyList()
                )
                
                result shouldBe 6
            }
        }

        describe("getQueryString") {

            it("should return empty string when just @ typed") {
                val query = getQueryString(
                    text = "@",
                    cursorPosition = 1,
                    triggerKey = '@'
                )
                
                query shouldBe ""
            }

            it("should extract query after @") {
                val query = getQueryString(
                    text = "@john",
                    cursorPosition = 5,
                    triggerKey = '@'
                )
                
                query shouldBe "john"
            }

            it("should extract partial query") {
                val query = getQueryString(
                    text = "Hello @joh",
                    cursorPosition = 10,
                    triggerKey = '@'
                )
                
                query shouldBe "joh"
            }

            it("should return empty for empty text") {
                val query = getQueryString(
                    text = "",
                    cursorPosition = 0,
                    triggerKey = '@'
                )
                
                query shouldBe ""
            }

            it("should handle query with numbers") {
                val query = getQueryString(
                    text = "@user123",
                    cursorPosition = 8,
                    triggerKey = '@'
                )
                
                query shouldBe "user123"
            }

            it("should handle query with underscores") {
                val query = getQueryString(
                    text = "@user_name",
                    cursorPosition = 10,
                    triggerKey = '@'
                )
                
                query shouldBe "user_name"
            }
        }

        describe("edge cases") {

            it("should handle multiple @ characters - find nearest valid one") {
                val result = detectMention(
                    text = "@john @jane",
                    cursorPosition = 11,
                    trackingCharacter = '@',
                    spanRanges = emptyList()
                )
                
                result shouldBe 6
            }

            it("should handle @ at very end of text") {
                val result = detectMention(
                    text = "Hello @",
                    cursorPosition = 7,
                    trackingCharacter = '@',
                    spanRanges = emptyList()
                )
                
                result shouldBe 6
            }

            it("should handle multiple spaces before @") {
                val result = detectMention(
                    text = "Hello   @john",
                    cursorPosition = 13,
                    trackingCharacter = '@',
                    spanRanges = emptyList()
                )
                
                result shouldBe 8
            }

            it("should handle @ after tab character") {
                val result = detectMention(
                    text = "Hello\t@john",
                    cursorPosition = 11,
                    trackingCharacter = '@',
                    spanRanges = emptyList()
                )
                
                // Tab is not whitespace in our detection logic (only space and newline)
                result shouldBe -1
            }

            it("should handle cursor at position 0") {
                val result = detectMention(
                    text = "@john",
                    cursorPosition = 0,
                    trackingCharacter = '@',
                    spanRanges = emptyList()
                )
                
                result shouldBe -1
            }

            it("should handle very long query") {
                val longName = "a".repeat(100)
                val text = "@$longName"
                
                val query = getQueryString(
                    text = text,
                    cursorPosition = text.length,
                    triggerKey = '@'
                )
                
                query shouldBe longName
            }
        }

        describe("span boundary detection") {

            it("should not detect when scanning through a span") {
                // Span at 0-5 (@John)
                val spanRanges = listOf(0..5)
                
                val result = detectMention(
                    text = "@John @jane",
                    cursorPosition = 11,
                    trackingCharacter = '@',
                    spanRanges = spanRanges
                )
                
                // Should find the second @ at position 6
                result shouldBe 6
            }

            it("should stop detection when hitting span boundary") {
                // Span at 6-11 (@Jane)
                val spanRanges = listOf(6..11)
                
                val result = detectMention(
                    text = "Hello @Jane@test",
                    cursorPosition = 16,
                    trackingCharacter = '@',
                    spanRanges = spanRanges
                )
                
                // The @ at position 11 is right after the span, but preceded by span content
                // Detection should find @ at 11 but it's not preceded by whitespace
                result shouldBe -1
            }
        }
    }
})

/**
 * Simulates the mention detection logic from CometChatMessageComposer.
 * Based on design document section 3.1.
 * 
 * @param text The current text
 * @param cursorPosition Current cursor position
 * @param trackingCharacter The character that triggers mention (default '@')
 * @param spanRanges List of ranges where NonEditableSpans exist
 * @return The index of the tracking character, or -1 if not found
 */
private fun detectMention(
    text: String,
    cursorPosition: Int,
    trackingCharacter: Char,
    spanRanges: List<IntRange>
): Int {
    if (text.isEmpty() || cursorPosition <= 0) return -1
    
    // Scan backwards from cursor to find tracking character
    for (i in (cursorPosition - 1) downTo 0) {
        val char = text[i]
        
        // Check if we're inside a span - if so, stop
        if (spanRanges.any { i in it }) {
            return -1
        }
        
        // Check if this is the tracking character
        if (char == trackingCharacter) {
            // Valid only if at start or preceded by whitespace
            if (i == 0 || text[i - 1] == ' ' || text[i - 1] == '\n') {
                // Check if followed by space (mention closed)
                if (i < cursorPosition - 1 && (text[i + 1] == ' ' || text[i + 1] == '\n')) {
                    return -1
                }
                return i
            }
            return -1
        }
        
        // If we hit whitespace before finding tracking character, stop
        if (char == ' ' || char == '\n') {
            return -1
        }
    }
    
    return -1
}

/**
 * Extracts the query string from text.
 * Based on design document section 3.1.
 * 
 * @param text The current text
 * @param cursorPosition Current cursor position
 * @param triggerKey The tracking character
 * @return The query string (text after @ and before cursor)
 */
private fun getQueryString(
    text: String,
    cursorPosition: Int,
    triggerKey: Char
): String {
    if (text.isEmpty()) return ""
    
    var startIndex = cursorPosition
    for (i in (cursorPosition - 1) downTo 0) {
        if (text[i] == triggerKey) {
            startIndex = i
            break
        }
    }
    
    return try {
        text.substring(startIndex + 1, cursorPosition)
    } catch (e: Exception) {
        ""
    }
}
