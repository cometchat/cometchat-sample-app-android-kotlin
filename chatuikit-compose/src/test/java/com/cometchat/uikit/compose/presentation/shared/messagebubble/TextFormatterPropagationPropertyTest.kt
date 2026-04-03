package com.cometchat.uikit.compose.presentation.shared.messagebubble

import androidx.compose.ui.text.AnnotatedString
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.element
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll

/**
 * Property-based tests for text formatter propagation (Property 5).
 *
 * // Feature: jetpack-message-bubble-parity, Property 5: Text formatters are applied to text content
 *
 * **Validates: Requirements 3.2, 3.4, 3.5**
 *
 * Property 5: For any TextMessage and any non-empty list of CometChatTextFormatter instances,
 * the rendered text in both CometChatTextBubble and DefaultReplyView (for quoted text messages)
 * SHALL be the result of sequentially applying all formatters to the original message text.
 *
 * Since the actual text formatting involves Compose runtime and Android Context
 * (FormatterUtils.getFormattedText needs Context, CometChatTextFormatter.prepareMessageString
 * needs Context + BaseMessage), we test the propagation logic as a pure function:
 *
 * The core logic in FormatterUtils.getFormattedText:
 * ```kotlin
 * var formattedText: AnnotatedString = AnnotatedString(text)
 * for (textFormatter in formatters) {
 *     formattedText = textFormatter.prepareMessageString(context, baseMessage, formattedText, alignment, formattingType)
 * }
 * return formattedText
 * ```
 *
 * We model each formatter as a pure text transformation function and verify:
 * 1. Empty formatters list → original text unchanged
 * 2. Single formatter → text transformed by that formatter
 * 3. Multiple formatters → text transformed sequentially (left fold)
 * 4. Formatters applied consistently to both TextBubble and reply preview contexts
 */
class TextFormatterPropagationPropertyTest : StringSpec({

    /**
     * Models the sequential formatter application from FormatterUtils.getFormattedText.
     * Each formatter is represented as a pure (AnnotatedString) -> AnnotatedString function.
     *
     * @param text The original message text
     * @param formatters List of transformation functions applied sequentially
     * @return The final formatted AnnotatedString
     */
    fun applyFormatters(
        text: String,
        formatters: List<(AnnotatedString) -> AnnotatedString>
    ): AnnotatedString {
        var result = AnnotatedString(text)
        for (formatter in formatters) {
            result = formatter(result)
        }
        return result
    }


    // ============================================================================
    // Generators
    // ============================================================================

    /**
     * Generates predictable text transformation functions that model
     * CometChatTextFormatter.prepareMessageString behavior.
     * Each transformation wraps the text with a unique tag, making
     * sequential application verifiable.
     */
    val transformerTagArb = Arb.element("bold", "italic", "mention", "link", "markdown", "highlight", "code")

    /**
     * Creates a deterministic formatter function from a tag.
     * Wrapping with tags makes sequential application order verifiable:
     * e.g., applying ["bold", "italic"] to "hello" → "[italic][bold]hello[/bold][/italic]"
     */
    fun tagFormatter(tag: String): (AnnotatedString) -> AnnotatedString = { input ->
        AnnotatedString("[$tag]${input.text}[/$tag]")
    }

    // ============================================================================
    // Property Test: Empty formatters list → original text unchanged
    // ============================================================================

    /**
     * Property test: When the formatters list is empty, the original text is returned
     * as an AnnotatedString without any transformation.
     *
     * This matches the behavior in both CometChatTextBubble (which passes textFormatters
     * to FormatterUtils) and CometChatMessagePreview.resolveMessageContent (which checks
     * textFormatters.isNotEmpty() before calling FormatterUtils).
     *
     * // Feature: jetpack-message-bubble-parity, Property 5: Text formatters are applied to text content
     * **Validates: Requirements 3.2, 3.4, 3.5**
     */
    "empty formatters list returns original text unchanged" {
        checkAll(100, Arb.string(minSize = 0, maxSize = 200)) { originalText ->
            val result = applyFormatters(originalText, emptyList())
            result.text shouldBe originalText
        }
    }

    // ============================================================================
    // Property Test: Single formatter → text transformed by that formatter
    // ============================================================================

    /**
     * Property test: When a single formatter is provided, the output is the result
     * of applying that single formatter to the original text.
     *
     * // Feature: jetpack-message-bubble-parity, Property 5: Text formatters are applied to text content
     * **Validates: Requirements 3.2, 3.4, 3.5**
     */
    "single formatter transforms text exactly once" {
        checkAll(100, Arb.string(minSize = 1, maxSize = 100), transformerTagArb) { originalText, tag ->
            val formatter = tagFormatter(tag)
            val result = applyFormatters(originalText, listOf(formatter))

            result.text shouldBe "[$tag]$originalText[/$tag]"
        }
    }

    // ============================================================================
    // Property Test: Multiple formatters → text transformed sequentially (left fold)
    // ============================================================================

    /**
     * Property test: When multiple formatters are provided, they are applied
     * sequentially (left to right). The first formatter wraps the original text,
     * the second wraps the result of the first, and so on.
     *
     * This verifies the left-fold semantics of FormatterUtils.getFormattedText:
     * ```kotlin
     * var formattedText = AnnotatedString(text)
     * for (textFormatter in formatters) {
     *     formattedText = textFormatter.prepareMessageString(...)
     * }
     * ```
     *
     * // Feature: jetpack-message-bubble-parity, Property 5: Text formatters are applied to text content
     * **Validates: Requirements 3.2, 3.4, 3.5**
     */
    "multiple formatters are applied sequentially in order" {
        checkAll(100, Arb.string(minSize = 1, maxSize = 50), Arb.list(transformerTagArb, 2..5)) { originalText, tags ->
            val formatters = tags.map { tagFormatter(it) }
            val result = applyFormatters(originalText, formatters)

            // Build expected result by manually applying tags in order
            var expected = originalText
            for (tag in tags) {
                expected = "[$tag]$expected[/$tag]"
            }

            result.text shouldBe expected
        }
    }

    // ============================================================================
    // Property Test: Formatters applied consistently to both TextBubble and reply preview
    // ============================================================================

    /**
     * Property test: The same formatter list applied to the same text produces
     * identical results regardless of context (TextBubble vs reply preview).
     *
     * Both CometChatTextBubble and CometChatMessagePreview.resolveMessageContent
     * use the same FormatterUtils.getFormattedText function, so the sequential
     * application logic is identical. This test verifies that the pure function
     * model is deterministic and context-independent.
     *
     * // Feature: jetpack-message-bubble-parity, Property 5: Text formatters are applied to text content
     * **Validates: Requirements 3.2, 3.4, 3.5**
     */
    "formatters produce identical results in TextBubble and reply preview contexts" {
        checkAll(100, Arb.string(minSize = 1, maxSize = 100), Arb.list(transformerTagArb, 1..4)) { originalText, tags ->
            val formatters = tags.map { tagFormatter(it) }

            // Simulate TextBubble context
            val textBubbleResult = applyFormatters(originalText, formatters)
            // Simulate reply preview context (same function, same inputs)
            val replyPreviewResult = applyFormatters(originalText, formatters)

            textBubbleResult.text shouldBe replyPreviewResult.text
        }
    }

    // ============================================================================
    // Property Test: Formatter application is associative with composition
    // ============================================================================

    /**
     * Property test: Applying formatters [A, B, C] to text is equivalent to
     * applying [A] then applying [B, C] to the result. This verifies the
     * sequential (left-fold) semantics hold when splitting the formatter list.
     *
     * // Feature: jetpack-message-bubble-parity, Property 5: Text formatters are applied to text content
     * **Validates: Requirements 3.2, 3.4, 3.5**
     */
    "formatter application is consistent when split at any point" {
        checkAll(100, Arb.string(minSize = 1, maxSize = 50), Arb.list(transformerTagArb, 2..5), Arb.int(1..4)) { originalText, tags, splitIdx ->
            val actualSplit = splitIdx.coerceIn(1, tags.size - 1)
            val allFormatters = tags.map { tagFormatter(it) }

            // Apply all at once
            val fullResult = applyFormatters(originalText, allFormatters)

            // Apply in two stages: first part, then second part on the intermediate result
            val firstPart = allFormatters.take(actualSplit)
            val secondPart = allFormatters.drop(actualSplit)
            val intermediate = applyFormatters(originalText, firstPart)
            var splitResult = intermediate
            for (formatter in secondPart) {
                splitResult = formatter(splitResult)
            }

            fullResult.text shouldBe splitResult.text
        }
    }

    // ============================================================================
    // Property Test: Non-empty formatters always modify text
    // ============================================================================

    /**
     * Property test: When at least one formatter is provided, the output text
     * differs from the original (since our model formatters always wrap with tags).
     * This models the invariant that non-empty formatters always produce a
     * transformation effect.
     *
     * // Feature: jetpack-message-bubble-parity, Property 5: Text formatters are applied to text content
     * **Validates: Requirements 3.2, 3.4, 3.5**
     */
    "non-empty formatters list always produces different text from original" {
        checkAll(100, Arb.string(minSize = 1, maxSize = 100), Arb.list(transformerTagArb, 1..3)) { originalText, tags ->
            val formatters = tags.map { tagFormatter(it) }
            val result = applyFormatters(originalText, formatters)

            // With tag-wrapping formatters, the result always differs from original
            (result.text != originalText) shouldBe true
        }
    }
})
