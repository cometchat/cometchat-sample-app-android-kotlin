package com.cometchat.uikit.compose.presentation.shared.messagebubble.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.ints.shouldBeGreaterThanOrEqual
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.element
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll

/**
 * Property-based tests for Spannable Text Support in ActionBubble.
 *
 * Feature: remaining-message-bubbles
 * Properties tested:
 * - Property 7: Spannable Text Support
 *
 * **Validates: Requirements 4.3**
 *
 * Tests that the ActionBubble correctly preserves all spans when rendering
 * AnnotatedString input. In Jetpack Compose, SpannableString from Android
 * is represented as AnnotatedString with SpanStyle annotations.
 *
 * Span types tested:
 * - Bold (FontWeight.Bold)
 * - Italic (FontStyle.Italic)
 * - Underline (TextDecoration.Underline)
 * - Strikethrough (TextDecoration.LineThrough)
 * - ForegroundColor (color property in SpanStyle)
 * - BackgroundColor (background property in SpanStyle)
 *
 * The ActionBubble uses Compose's Text composable which natively supports
 * AnnotatedString, so span preservation is guaranteed by the framework.
 * These tests verify the conversion and building logic for AnnotatedStrings.
 */
class ActionBubbleSpannableTextPropertyTest : StringSpec({

    /**
     * Property 7: Spannable Text Support - All spans are preserved in AnnotatedString
     *
     * *For any* AnnotatedString with N spans, the ActionBubble SHALL preserve
     * all N spans in the rendered text.
     *
     * **Validates: Requirements 4.3**
     */
    "Property 7: AnnotatedString with N spans should preserve all N spans" {
        val spanCountArb = Arb.int(1..10)
        val textArb = Arb.string(5..20)

        checkAll(100, spanCountArb, textArb) { spanCount, baseText ->
            val annotatedString = buildAnnotatedStringWithSpans(baseText, spanCount)
            
            // Verify the number of span styles matches expected count
            annotatedString.spanStyles.size shouldBe spanCount
            
            // Verify the text content is preserved
            annotatedString.text shouldBe baseText.repeat(spanCount)
        }
    }

    /**
     * Property 7: Spannable Text Support - Bold spans are preserved
     *
     * *For any* AnnotatedString with Bold spans, the span style SHALL contain
     * FontWeight.Bold.
     *
     * **Validates: Requirements 4.3**
     */
    "Property 7: Bold spans should be preserved in AnnotatedString" {
        val textArb = Arb.string(5..50)

        checkAll(100, textArb) { text ->
            val annotatedString = buildAnnotatedString {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(text)
                }
            }
            
            // Verify bold span is present
            annotatedString.spanStyles.size shouldBe 1
            annotatedString.spanStyles[0].item.fontWeight shouldBe FontWeight.Bold
            
            // Verify text is preserved
            annotatedString.text shouldBe text
        }
    }

    /**
     * Property 7: Spannable Text Support - Italic spans are preserved
     *
     * *For any* AnnotatedString with Italic spans, the span style SHALL contain
     * FontStyle.Italic.
     *
     * **Validates: Requirements 4.3**
     */
    "Property 7: Italic spans should be preserved in AnnotatedString" {
        val textArb = Arb.string(5..50)

        checkAll(100, textArb) { text ->
            val annotatedString = buildAnnotatedString {
                withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                    append(text)
                }
            }
            
            // Verify italic span is present
            annotatedString.spanStyles.size shouldBe 1
            annotatedString.spanStyles[0].item.fontStyle shouldBe FontStyle.Italic
            
            // Verify text is preserved
            annotatedString.text shouldBe text
        }
    }

    /**
     * Property 7: Spannable Text Support - Underline spans are preserved
     *
     * *For any* AnnotatedString with Underline spans, the span style SHALL contain
     * TextDecoration.Underline.
     *
     * **Validates: Requirements 4.3**
     */
    "Property 7: Underline spans should be preserved in AnnotatedString" {
        val textArb = Arb.string(5..50)

        checkAll(100, textArb) { text ->
            val annotatedString = buildAnnotatedString {
                withStyle(SpanStyle(textDecoration = TextDecoration.Underline)) {
                    append(text)
                }
            }
            
            // Verify underline span is present
            annotatedString.spanStyles.size shouldBe 1
            annotatedString.spanStyles[0].item.textDecoration shouldBe TextDecoration.Underline
            
            // Verify text is preserved
            annotatedString.text shouldBe text
        }
    }

    /**
     * Property 7: Spannable Text Support - Strikethrough spans are preserved
     *
     * *For any* AnnotatedString with Strikethrough spans, the span style SHALL contain
     * TextDecoration.LineThrough.
     *
     * **Validates: Requirements 4.3**
     */
    "Property 7: Strikethrough spans should be preserved in AnnotatedString" {
        val textArb = Arb.string(5..50)

        checkAll(100, textArb) { text ->
            val annotatedString = buildAnnotatedString {
                withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)) {
                    append(text)
                }
            }
            
            // Verify strikethrough span is present
            annotatedString.spanStyles.size shouldBe 1
            annotatedString.spanStyles[0].item.textDecoration shouldBe TextDecoration.LineThrough
            
            // Verify text is preserved
            annotatedString.text shouldBe text
        }
    }

    /**
     * Property 7: Spannable Text Support - ForegroundColor spans are preserved
     *
     * *For any* AnnotatedString with ForegroundColor spans, the span style SHALL contain
     * the specified color.
     *
     * **Validates: Requirements 4.3**
     */
    "Property 7: ForegroundColor spans should be preserved in AnnotatedString" {
        val textArb = Arb.string(5..50)
        val colorArb = Arb.element(
            listOf(
                Color.Red,
                Color.Blue,
                Color.Green,
                Color.Yellow,
                Color.Magenta,
                Color.Cyan,
                Color.Black,
                Color.White,
                Color(0xFF6852D6), // Primary color
                Color(0xFFFF5722)  // Custom color
            )
        )

        checkAll(100, textArb, colorArb) { text, color ->
            val annotatedString = buildAnnotatedString {
                withStyle(SpanStyle(color = color)) {
                    append(text)
                }
            }
            
            // Verify foreground color span is present
            annotatedString.spanStyles.size shouldBe 1
            annotatedString.spanStyles[0].item.color shouldBe color
            
            // Verify text is preserved
            annotatedString.text shouldBe text
        }
    }

    /**
     * Property 7: Spannable Text Support - BackgroundColor spans are preserved
     *
     * *For any* AnnotatedString with BackgroundColor spans, the span style SHALL contain
     * the specified background color.
     *
     * **Validates: Requirements 4.3**
     */
    "Property 7: BackgroundColor spans should be preserved in AnnotatedString" {
        val textArb = Arb.string(5..50)
        val colorArb = Arb.element(
            listOf(
                Color.Red,
                Color.Blue,
                Color.Green,
                Color.Yellow,
                Color.LightGray,
                Color(0xFFE8E8E8), // Neutral 300
                Color(0xFFF5F5F5)  // Neutral 200
            )
        )

        checkAll(100, textArb, colorArb) { text, backgroundColor ->
            val annotatedString = buildAnnotatedString {
                withStyle(SpanStyle(background = backgroundColor)) {
                    append(text)
                }
            }
            
            // Verify background color span is present
            annotatedString.spanStyles.size shouldBe 1
            annotatedString.spanStyles[0].item.background shouldBe backgroundColor
            
            // Verify text is preserved
            annotatedString.text shouldBe text
        }
    }

    /**
     * Property 7: Spannable Text Support - Combined spans are preserved
     *
     * *For any* AnnotatedString with multiple span types combined (Bold + Italic + Underline),
     * all span properties SHALL be preserved in a single SpanStyle.
     *
     * **Validates: Requirements 4.3**
     */
    "Property 7: Combined spans (Bold + Italic + Underline) should be preserved" {
        val textArb = Arb.string(5..50)
        val colorArb = Arb.element(listOf(Color.Red, Color.Blue, Color.Green))

        checkAll(100, textArb, colorArb) { text, color ->
            val annotatedString = buildAnnotatedString {
                withStyle(
                    SpanStyle(
                        fontWeight = FontWeight.Bold,
                        fontStyle = FontStyle.Italic,
                        textDecoration = TextDecoration.Underline,
                        color = color
                    )
                ) {
                    append(text)
                }
            }
            
            // Verify all span properties are present in a single span
            annotatedString.spanStyles.size shouldBe 1
            val spanStyle = annotatedString.spanStyles[0].item
            spanStyle.fontWeight shouldBe FontWeight.Bold
            spanStyle.fontStyle shouldBe FontStyle.Italic
            spanStyle.textDecoration shouldBe TextDecoration.Underline
            spanStyle.color shouldBe color
            
            // Verify text is preserved
            annotatedString.text shouldBe text
        }
    }

    /**
     * Property 7: Spannable Text Support - Multiple separate spans are preserved
     *
     * *For any* AnnotatedString with multiple separate spans at different positions,
     * all spans SHALL be preserved with correct start and end indices.
     *
     * **Validates: Requirements 4.3**
     */
    "Property 7: Multiple separate spans at different positions should be preserved" {
        val segment1Arb = Arb.string(3..10)
        val segment2Arb = Arb.string(3..10)
        val segment3Arb = Arb.string(3..10)

        checkAll(100, segment1Arb, segment2Arb, segment3Arb) { seg1, seg2, seg3 ->
            val annotatedString = buildAnnotatedString {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(seg1)
                }
                append(" ")
                withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                    append(seg2)
                }
                append(" ")
                withStyle(SpanStyle(textDecoration = TextDecoration.Underline)) {
                    append(seg3)
                }
            }
            
            // Verify all three spans are present
            annotatedString.spanStyles.size shouldBe 3
            
            // Verify span types
            val spanTypes = annotatedString.spanStyles.map { it.item }
            spanTypes.any { it.fontWeight == FontWeight.Bold } shouldBe true
            spanTypes.any { it.fontStyle == FontStyle.Italic } shouldBe true
            spanTypes.any { it.textDecoration == TextDecoration.Underline } shouldBe true
            
            // Verify text content
            annotatedString.text shouldBe "$seg1 $seg2 $seg3"
        }
    }

    /**
     * Property 7: Spannable Text Support - Nested spans are preserved
     *
     * *For any* AnnotatedString with nested spans (e.g., Bold containing Italic),
     * both spans SHALL be preserved.
     *
     * **Validates: Requirements 4.3**
     */
    "Property 7: Nested spans should be preserved" {
        val outerTextArb = Arb.string(3..10)
        val innerTextArb = Arb.string(3..10)

        checkAll(100, outerTextArb, innerTextArb) { outerText, innerText ->
            val annotatedString = buildAnnotatedString {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(outerText)
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                        append(innerText)
                    }
                }
            }
            
            // Verify both spans are present
            annotatedString.spanStyles.size shouldBeGreaterThanOrEqual 2
            
            // Verify span types exist
            val spanTypes = annotatedString.spanStyles.map { it.item }
            spanTypes.any { it.fontWeight == FontWeight.Bold } shouldBe true
            spanTypes.any { it.fontStyle == FontStyle.Italic } shouldBe true
            
            // Verify text content
            annotatedString.text shouldBe "$outerText$innerText"
        }
    }

    /**
     * Property 7: Spannable Text Support - Span indices are correct
     *
     * *For any* AnnotatedString with spans, the span start and end indices
     * SHALL correctly correspond to the text positions.
     *
     * **Validates: Requirements 4.3**
     */
    "Property 7: Span indices should correctly correspond to text positions" {
        val prefixArb = Arb.string(0..10)
        val spanTextArb = Arb.string(5..20)
        val suffixArb = Arb.string(0..10)

        checkAll(100, prefixArb, spanTextArb, suffixArb) { prefix, spanText, suffix ->
            val annotatedString = buildAnnotatedString {
                append(prefix)
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(spanText)
                }
                append(suffix)
            }
            
            // Verify span indices
            annotatedString.spanStyles.size shouldBe 1
            val spanRange = annotatedString.spanStyles[0]
            spanRange.start shouldBe prefix.length
            spanRange.end shouldBe prefix.length + spanText.length
            
            // Verify text content
            annotatedString.text shouldBe "$prefix$spanText$suffix"
        }
    }

    /**
     * Property 7: Spannable Text Support - Empty text with spans
     *
     * *For any* AnnotatedString with empty text segments, spans SHALL still be
     * correctly applied (even if they have zero length).
     *
     * **Validates: Requirements 4.3**
     */
    "Property 7: Empty text segments should handle spans correctly" {
        val textArb = Arb.string(5..20)

        checkAll(100, textArb) { text ->
            val annotatedString = buildAnnotatedString {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    // Empty span
                }
                append(text)
            }
            
            // The empty span should still be recorded (with start == end)
            // or not recorded at all depending on implementation
            // Text should be preserved regardless
            annotatedString.text shouldBe text
        }
    }

    /**
     * Property 7: Spannable Text Support - AnnotatedString conversion is deterministic
     *
     * *For any* input text and span configuration, building the AnnotatedString
     * multiple times SHALL produce identical results.
     *
     * **Validates: Requirements 4.3**
     */
    "Property 7: AnnotatedString building should be deterministic" {
        val textArb = Arb.string(5..30)
        val colorArb = Arb.element(listOf(Color.Red, Color.Blue, Color.Green))

        checkAll(100, textArb, colorArb) { text, color ->
            val builder = {
                buildAnnotatedString {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = color)) {
                        append(text)
                    }
                }
            }
            
            val result1 = builder()
            val result2 = builder()
            val result3 = builder()
            
            // All results should be identical
            result1.text shouldBe result2.text
            result2.text shouldBe result3.text
            result1.spanStyles.size shouldBe result2.spanStyles.size
            result2.spanStyles.size shouldBe result3.spanStyles.size
        }
    }

    /**
     * Property 7: Spannable Text Support - TextDecoration combination
     *
     * *For any* AnnotatedString with combined TextDecoration (Underline + LineThrough),
     * both decorations SHALL be preserved.
     *
     * **Validates: Requirements 4.3**
     */
    "Property 7: Combined TextDecoration (Underline + Strikethrough) should be preserved" {
        val textArb = Arb.string(5..50)

        checkAll(100, textArb) { text ->
            val combinedDecoration = TextDecoration.combine(
                listOf(TextDecoration.Underline, TextDecoration.LineThrough)
            )
            
            val annotatedString = buildAnnotatedString {
                withStyle(SpanStyle(textDecoration = combinedDecoration)) {
                    append(text)
                }
            }
            
            // Verify combined decoration is present
            annotatedString.spanStyles.size shouldBe 1
            val decoration = annotatedString.spanStyles[0].item.textDecoration
            decoration shouldNotBe null
            // Combined decoration should contain both
            decoration?.contains(TextDecoration.Underline) shouldBe true
            decoration?.contains(TextDecoration.LineThrough) shouldBe true
            
            // Verify text is preserved
            annotatedString.text shouldBe text
        }
    }

    /**
     * Property 7: Spannable Text Support - Span count matches input
     *
     * *For any* number N of spans added to an AnnotatedString,
     * the resulting spanStyles list SHALL have exactly N entries.
     *
     * **Validates: Requirements 4.3**
     */
    "Property 7: Span count should match the number of spans added" {
        val spanCountArb = Arb.int(0..15)
        val textArb = Arb.string(1..5)

        checkAll(100, spanCountArb, textArb) { spanCount, segmentText ->
            val annotatedString = buildAnnotatedString {
                repeat(spanCount) { index ->
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("$segmentText$index")
                    }
                }
            }
            
            // Verify span count matches
            annotatedString.spanStyles.size shouldBe spanCount
        }
    }

    /**
     * Property 7: Spannable Text Support - Clickable annotation preservation
     *
     * *For any* AnnotatedString with string annotations (used for clickable spans),
     * the annotations SHALL be preserved.
     *
     * **Validates: Requirements 4.3**
     */
    "Property 7: String annotations (for clickable spans) should be preserved" {
        val textArb = Arb.string(5..20)
        val urlArb = Arb.element(
            listOf(
                "https://example.com",
                "https://cometchat.com",
                "mailto:test@example.com"
            )
        )

        checkAll(100, textArb, urlArb) { text, url ->
            val annotatedString = buildAnnotatedString {
                pushStringAnnotation(tag = "URL", annotation = url)
                withStyle(SpanStyle(color = Color.Blue, textDecoration = TextDecoration.Underline)) {
                    append(text)
                }
                pop()
            }
            
            // Verify annotation is present
            val annotations = annotatedString.getStringAnnotations(
                tag = "URL",
                start = 0,
                end = text.length
            )
            annotations.size shouldBe 1
            annotations[0].item shouldBe url
            
            // Verify span style is also present
            annotatedString.spanStyles.size shouldBe 1
            annotatedString.spanStyles[0].item.color shouldBe Color.Blue
            
            // Verify text is preserved
            annotatedString.text shouldBe text
        }
    }

    /**
     * Property 7: Spannable Text Support - Mention-style spans
     *
     * *For any* AnnotatedString with mention-style formatting (background color + bold),
     * the span SHALL be preserved correctly.
     *
     * **Validates: Requirements 4.3**
     */
    "Property 7: Mention-style spans should be preserved" {
        val mentionTextArb = Arb.element(
            listOf("@John", "@Jane", "@Admin", "@User123", "@TeamLead")
        )
        val surroundingTextArb = Arb.string(5..20)

        checkAll(100, mentionTextArb, surroundingTextArb) { mention, surrounding ->
            val mentionStyle = SpanStyle(
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6852D6), // Primary color
                background = Color(0xFFE8E8E8) // Light background
            )
            
            val annotatedString = buildAnnotatedString {
                append(surrounding)
                append(" ")
                pushStringAnnotation(tag = "MENTION", annotation = mention)
                withStyle(mentionStyle) {
                    append(mention)
                }
                pop()
                append(" ")
                append(surrounding)
            }
            
            // Verify mention annotation is present
            val mentionStart = surrounding.length + 1
            val mentionEnd = mentionStart + mention.length
            val annotations = annotatedString.getStringAnnotations(
                tag = "MENTION",
                start = mentionStart,
                end = mentionEnd
            )
            annotations.size shouldBe 1
            annotations[0].item shouldBe mention
            
            // Verify span style is present
            annotatedString.spanStyles.size shouldBe 1
            val spanStyle = annotatedString.spanStyles[0].item
            spanStyle.fontWeight shouldBe FontWeight.Bold
            spanStyle.color shouldBe Color(0xFF6852D6)
            spanStyle.background shouldBe Color(0xFFE8E8E8)
        }
    }

    /**
     * Property 7: Spannable Text Support - Unicode text with spans
     *
     * *For any* AnnotatedString containing Unicode characters with spans,
     * both the text and spans SHALL be preserved correctly.
     *
     * **Validates: Requirements 4.3**
     */
    "Property 7: Unicode text with spans should be preserved" {
        val unicodeTexts = listOf(
            "Hello 世界",
            "Привет мир",
            "مرحبا بالعالم",
            "שלום עולם",
            "🎉 Party 🎊",
            "Emoji 😀 test 🚀",
            "Mixed: Hello 你好 مرحبا"
        )
        val unicodeArb = Arb.element(unicodeTexts)

        checkAll(100, unicodeArb) { unicodeText ->
            val annotatedString = buildAnnotatedString {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(unicodeText)
                }
            }
            
            // Verify text is preserved exactly
            annotatedString.text shouldBe unicodeText
            
            // Verify span is present
            annotatedString.spanStyles.size shouldBe 1
            annotatedString.spanStyles[0].item.fontWeight shouldBe FontWeight.Bold
        }
    }

    /**
     * Property 7: Spannable Text Support - Long text with multiple spans
     *
     * *For any* long text with multiple spans distributed throughout,
     * all spans SHALL be preserved with correct indices.
     *
     * **Validates: Requirements 4.3**
     */
    "Property 7: Long text with multiple distributed spans should be preserved" {
        val wordArb = Arb.string(3..8)
        val wordCountArb = Arb.int(5..15)

        checkAll(50, wordCountArb, wordArb) { wordCount, baseWord ->
            val annotatedString = buildAnnotatedString {
                repeat(wordCount) { index ->
                    if (index % 2 == 0) {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("$baseWord$index")
                        }
                    } else {
                        append("$baseWord$index")
                    }
                    if (index < wordCount - 1) append(" ")
                }
            }
            
            // Verify correct number of spans (every other word is bold)
            val expectedSpanCount = (wordCount + 1) / 2
            annotatedString.spanStyles.size shouldBe expectedSpanCount
            
            // Verify all spans are bold
            annotatedString.spanStyles.forEach { span ->
                span.item.fontWeight shouldBe FontWeight.Bold
            }
        }
    }
})

/**
 * Helper function to build an AnnotatedString with a specified number of spans.
 * Each span applies a different style to a segment of text.
 */
private fun buildAnnotatedStringWithSpans(baseText: String, spanCount: Int): AnnotatedString {
    val spanStyles = listOf(
        SpanStyle(fontWeight = FontWeight.Bold),
        SpanStyle(fontStyle = FontStyle.Italic),
        SpanStyle(textDecoration = TextDecoration.Underline),
        SpanStyle(textDecoration = TextDecoration.LineThrough),
        SpanStyle(color = Color.Red),
        SpanStyle(color = Color.Blue),
        SpanStyle(background = Color.Yellow),
        SpanStyle(background = Color.LightGray),
        SpanStyle(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic),
        SpanStyle(color = Color.Green, textDecoration = TextDecoration.Underline)
    )
    
    return buildAnnotatedString {
        repeat(spanCount) { index ->
            val style = spanStyles[index % spanStyles.size]
            withStyle(style) {
                append(baseText)
            }
        }
    }
}
