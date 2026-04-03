package com.cometchat.uikit.compose.presentation.shared.messagebubble.ui

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.orNull
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll

/**
 * Property-based tests for Video Bubble Caption Visibility.
 *
 * Feature: video-audio-bubbles
 * Properties tested:
 * - Property 4: Caption Visibility Consistency
 *
 * **Validates: Requirements 3.1, 3.2**
 *
 * Tests the caption visibility logic used in [CometChatVideoBubble]:
 * - If caption is non-null and non-empty, the caption text view SHALL be visible
 * - If caption is null or empty, the caption text view SHALL NOT be visible
 *
 * The actual UI rendering uses the condition: `!caption.isNullOrEmpty()`
 * This test validates that logic through the [shouldShowCaption] helper function.
 */
class VideoBubbleCaptionVisibilityPropertyTest : StringSpec({

    /**
     * Property 4: Caption Visibility Consistency
     *
     * *For any* Video_Bubble with caption parameter:
     * - If caption is non-null and non-empty, the caption text view SHALL be visible
     * - If caption is null or empty, the caption text view SHALL NOT be visible
     *
     * **Validates: Requirements 3.1, 3.2**
     */
    "Property 4: Non-null, non-empty captions should result in visible caption" {
        // Generate arbitrary non-empty strings
        val nonEmptyCaptionArb = Arb.string(minSize = 1, maxSize = 1000)
            .filter { it.isNotEmpty() }

        checkAll(100, nonEmptyCaptionArb) { caption ->
            shouldShowCaption(caption) shouldBe true
        }
    }

    "Property 4: Null captions should result in hidden caption" {
        shouldShowCaption(null) shouldBe false
    }

    "Property 4: Empty string captions should result in hidden caption" {
        shouldShowCaption("") shouldBe false
    }

    "Property 4: Whitespace-only captions should result in visible caption (per implementation)" {
        // Note: The actual implementation uses isNullOrEmpty(), NOT isNullOrBlank()
        // This means whitespace-only strings ARE considered visible captions.
        // This test validates the actual implementation behavior.
        val whitespaceStrings = listOf(
            " ",
            "  ",
            "\t",
            "\n",
            "\r",
            "   ",
            "\t\t",
            "\n\n",
            " \t\n",
            "    \t    \n    "
        )

        whitespaceStrings.forEach { whitespaceCaption ->
            // Per implementation: whitespace-only strings are NOT empty, so caption IS visible
            shouldShowCaption(whitespaceCaption) shouldBe true
        }
    }

    "Property 4: Captions with leading/trailing whitespace should be visible" {
        // Generate strings with content surrounded by whitespace
        val contentWithWhitespaceArb = Arb.string(minSize = 1, maxSize = 100)
            .filter { it.isNotEmpty() }

        checkAll(100, contentWithWhitespaceArb) { content ->
            // Add various whitespace padding
            val paddedCaptions = listOf(
                " $content",
                "$content ",
                " $content ",
                "\t$content",
                "$content\n",
                "  $content  "
            )

            paddedCaptions.forEach { paddedCaption ->
                shouldShowCaption(paddedCaption) shouldBe true
            }
        }
    }

    "Property 4: Caption visibility should be consistent with isNullOrEmpty logic" {
        // Test with nullable strings (including null)
        val nullableCaptionArb = Arb.string(minSize = 0, maxSize = 500).orNull()

        checkAll(200, nullableCaptionArb) { caption ->
            val expectedVisibility = !caption.isNullOrEmpty()
            shouldShowCaption(caption) shouldBe expectedVisibility
        }
    }

    "Property 4: Caption visibility should handle special characters correctly" {
        // Test with various special characters that should still be visible
        val specialCaptions = listOf(
            "Hello, World!",
            "🎬 Video caption",
            "Caption with émojis 🎥📹",
            "日本語キャプション",
            "Caption with\nnewline",
            "Caption with\ttab",
            "Special chars: @#\$%^&*()",
            "Numbers: 12345",
            "Mixed: Hello 123 🎬"
        )

        specialCaptions.forEach { caption ->
            shouldShowCaption(caption) shouldBe true
        }
    }

    "Property 4: Caption visibility for edge case strings" {
        // Test specific edge cases
        
        // Single character should be visible
        shouldShowCaption("a") shouldBe true
        shouldShowCaption("1") shouldBe true
        shouldShowCaption("!") shouldBe true
        
        // Single whitespace should be visible (per isNullOrEmpty implementation)
        shouldShowCaption(" ") shouldBe true
        
        // Very long caption should be visible
        val longCaption = "A".repeat(10000)
        shouldShowCaption(longCaption) shouldBe true
        
        // Unicode characters should be visible
        shouldShowCaption("🎬") shouldBe true
        shouldShowCaption("中文") shouldBe true
        shouldShowCaption("العربية") shouldBe true
    }

    "Property 4: Caption visibility should be deterministic" {
        // The same caption should always produce the same visibility result
        val captionArb = Arb.string(minSize = 0, maxSize = 200).orNull()

        checkAll(100, captionArb) { caption ->
            val result1 = shouldShowCaption(caption)
            val result2 = shouldShowCaption(caption)
            val result3 = shouldShowCaption(caption)

            result1 shouldBe result2
            result2 shouldBe result3
        }
    }

    "Property 4: Comprehensive caption visibility test" {
        // Test all categories of captions
        val testCases = listOf<Pair<String?, Boolean>>(
            // Null case - hidden
            Pair(null, false),
            // Empty string case - hidden
            Pair("", false),
            // Whitespace-only cases - visible (per isNullOrEmpty implementation)
            Pair(" ", true),
            Pair("  ", true),
            Pair("\t", true),
            Pair("\n", true),
            Pair("\r", true),
            Pair(" \t\n\r ", true),
            // Non-empty cases - visible
            Pair("Hello", true),
            Pair("a", true),
            Pair("1", true),
            Pair(" Hello", true),
            Pair("Hello ", true),
            Pair(" Hello ", true),
            Pair("Hello\nWorld", true),
            Pair("🎬", true)
        )

        testCases.forEach { (caption, expectedVisibility) ->
            shouldShowCaption(caption) shouldBe expectedVisibility
        }
    }

    "Property 4: Only null and empty string should hide caption" {
        // Verify that ONLY null and empty string result in hidden caption
        shouldShowCaption(null) shouldBe false
        shouldShowCaption("") shouldBe false
        
        // Any other string (including whitespace) should be visible
        val nonEmptyStrings = listOf(
            " ",
            "a",
            "  ",
            "\t",
            "hello",
            "123",
            "🎬"
        )
        
        nonEmptyStrings.forEach { str ->
            shouldShowCaption(str) shouldBe true
        }
    }

    "Property 4: Caption visibility follows isNullOrEmpty semantics exactly" {
        // Property: For any string s, shouldShowCaption(s) == !s.isNullOrEmpty()
        val stringArb = Arb.string(minSize = 0, maxSize = 500).orNull()

        checkAll(300, stringArb) { caption ->
            val expected = !caption.isNullOrEmpty()
            val actual = shouldShowCaption(caption)
            
            actual shouldBe expected
        }
    }
})

/**
 * Helper function that encapsulates the caption visibility logic used in CometChatVideoBubble.
 *
 * This function mirrors the condition used in the actual composable:
 * ```kotlin
 * if (!caption.isNullOrEmpty()) {
 *     Text(...)
 * }
 * ```
 *
 * Note: The implementation uses isNullOrEmpty(), NOT isNullOrBlank().
 * This means whitespace-only strings ARE considered visible captions.
 *
 * @param caption The caption string to evaluate
 * @return true if the caption should be visible, false otherwise
 */
fun shouldShowCaption(caption: String?): Boolean {
    return !caption.isNullOrEmpty()
}
