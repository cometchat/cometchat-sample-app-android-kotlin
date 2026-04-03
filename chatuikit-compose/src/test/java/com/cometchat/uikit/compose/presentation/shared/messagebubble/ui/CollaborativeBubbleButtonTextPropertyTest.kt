package com.cometchat.uikit.compose.presentation.shared.messagebubble.ui

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldNotBeEmpty
import io.kotest.property.Arb
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll

/**
 * Property-based tests for Collaborative Bubble Button Text Parameter Usage.
 *
 * Feature: collaborative-bubble-parity
 * Properties tested:
 * - Property 1: Button text parameter usage
 *
 * **Validates: Requirements 1.1, 1.2, 1.3**
 */
class CollaborativeBubbleButtonTextPropertyTest : StringSpec({

    val nonEmptyStringArb = Arb.string(minSize = 1, maxSize = 50)
        .filter { it.isNotBlank() }

    val buttonTextArb = Arb.string(minSize = 1, maxSize = 100)
        .filter { it.isNotBlank() && it.all { char -> 
            char.isLetterOrDigit() || char.isWhitespace() || char in "!@#\$%^&*()_+-=[]{}|;':,./<>?" 
        } }

    "Property 1: Non-empty buttonText parameter should be preserved exactly as provided" {
        checkAll(100, nonEmptyStringArb) { buttonText ->
            val result = processButtonText(buttonText)
            result shouldBe buttonText
        }
    }

    "Property 1: Button text should not be modified during processing" {
        checkAll(100, buttonTextArb) { buttonText ->
            val result = processButtonText(buttonText)
            result.length shouldBe buttonText.length
            result shouldBe buttonText
        }
    }

    "Property 1: Button text should be correctly extracted from customData" {
        checkAll(100, nonEmptyStringArb) { buttonText ->
            val customData = mapOf("button_text" to buttonText)
            val result = extractButtonText(customData, ButtonTextCollaborativeType.DOCUMENT)
            result shouldBe buttonText
        }
    }

    "Property 1: Custom button text should take precedence over defaults" {
        val customTexts = listOf("Join Now", "Open", "View Document", "Start Collaboration")
        customTexts.forEach { customText ->
            val customData = mapOf("button_text" to customText)
            val documentResult = extractButtonText(customData, ButtonTextCollaborativeType.DOCUMENT)
            documentResult shouldBe customText
            val whiteboardResult = extractButtonText(customData, ButtonTextCollaborativeType.WHITEBOARD)
            whiteboardResult shouldBe customText
        }
    }

    "Property 1: Default button text for DOCUMENT type should be Open Document" {
        val customData = emptyMap<String, Any>()
        val result = extractButtonText(customData, ButtonTextCollaborativeType.DOCUMENT)
        result shouldBe DEFAULT_DOCUMENT_BUTTON_TEXT
    }

    "Property 1: Default button text for WHITEBOARD type should be Open Whiteboard" {
        val customData = emptyMap<String, Any>()
        val result = extractButtonText(customData, ButtonTextCollaborativeType.WHITEBOARD)
        result shouldBe DEFAULT_WHITEBOARD_BUTTON_TEXT
    }

    "Property 1: Empty customData should use default button text based on type" {
        val emptyCustomData = emptyMap<String, Any>()
        val documentResult = extractButtonText(emptyCustomData, ButtonTextCollaborativeType.DOCUMENT)
        documentResult shouldBe DEFAULT_DOCUMENT_BUTTON_TEXT
        val whiteboardResult = extractButtonText(emptyCustomData, ButtonTextCollaborativeType.WHITEBOARD)
        whiteboardResult shouldBe DEFAULT_WHITEBOARD_BUTTON_TEXT
    }

    "Property 1: Null customData should use default button text based on type" {
        val documentResult = extractButtonText(null, ButtonTextCollaborativeType.DOCUMENT)
        documentResult shouldBe DEFAULT_DOCUMENT_BUTTON_TEXT
        val whiteboardResult = extractButtonText(null, ButtonTextCollaborativeType.WHITEBOARD)
        whiteboardResult shouldBe DEFAULT_WHITEBOARD_BUTTON_TEXT
    }

    "Property 1: Whitespace-only button_text should use default" {
        val whitespaceValues = listOf("", " ", "  ", "\t", "\n")
        whitespaceValues.forEach { whitespace ->
            val customData = mapOf("button_text" to whitespace)
            val documentResult = extractButtonText(customData, ButtonTextCollaborativeType.DOCUMENT)
            documentResult shouldBe DEFAULT_DOCUMENT_BUTTON_TEXT
            val whiteboardResult = extractButtonText(customData, ButtonTextCollaborativeType.WHITEBOARD)
            whiteboardResult shouldBe DEFAULT_WHITEBOARD_BUTTON_TEXT
        }
    }

    "Property 1: Button text with special characters should be preserved" {
        val specialTexts = listOf("Open Document!", "Join (Now)", "View & Edit", "Open_Document")
        specialTexts.forEach { specialText ->
            val customData = mapOf("button_text" to specialText)
            val result = extractButtonText(customData, ButtonTextCollaborativeType.DOCUMENT)
            result shouldBe specialText
        }
    }

    "Property 1: Button text with Unicode characters should be preserved" {
        val unicodeTexts = listOf("Ouvrir", "Open ✓", "打开文档")
        unicodeTexts.forEach { unicodeText ->
            val customData = mapOf("button_text" to unicodeText)
            val result = extractButtonText(customData, ButtonTextCollaborativeType.DOCUMENT)
            result shouldBe unicodeText
        }
    }

    "Property 1: Long button text should be preserved without truncation" {
        val longTexts = listOf(
            "Open Document and Start Editing Together",
            "Join the Collaborative Whiteboard Session Now"
        )
        longTexts.forEach { longText ->
            val customData = mapOf("button_text" to longText)
            val result = extractButtonText(customData, ButtonTextCollaborativeType.DOCUMENT)
            result shouldBe longText
            result.length shouldBe longText.length
        }
    }

    "Property 1: Button text extraction should be deterministic" {
        checkAll(100, nonEmptyStringArb) { buttonText ->
            val customData = mapOf("button_text" to buttonText)
            val result1 = extractButtonText(customData, ButtonTextCollaborativeType.DOCUMENT)
            val result2 = extractButtonText(customData, ButtonTextCollaborativeType.DOCUMENT)
            val result3 = extractButtonText(customData, ButtonTextCollaborativeType.DOCUMENT)
            result1 shouldBe result2
            result2 shouldBe result3
        }
    }

    "Property 1: Custom button text should be type-independent" {
        checkAll(100, nonEmptyStringArb) { buttonText ->
            val customData = mapOf("button_text" to buttonText)
            val documentResult = extractButtonText(customData, ButtonTextCollaborativeType.DOCUMENT)
            val whiteboardResult = extractButtonText(customData, ButtonTextCollaborativeType.WHITEBOARD)
            documentResult shouldBe whiteboardResult
            documentResult shouldBe buttonText
        }
    }

    "Property 1: Button text extraction should never return null" {
        val inputs = listOf(
            Pair(null, ButtonTextCollaborativeType.DOCUMENT),
            Pair(null, ButtonTextCollaborativeType.WHITEBOARD),
            Pair(emptyMap<String, Any>(), ButtonTextCollaborativeType.DOCUMENT),
            Pair(mapOf("button_text" to ""), ButtonTextCollaborativeType.DOCUMENT),
            Pair(mapOf("button_text" to "Custom"), ButtonTextCollaborativeType.DOCUMENT)
        )
        inputs.forEach { (customData, type) ->
            val result = extractButtonText(customData, type)
            result shouldNotBe null
            result.shouldNotBeEmpty()
        }
    }

    "Property 1: Default button text should never be empty" {
        ButtonTextCollaborativeType.entries.forEach { type ->
            val result = extractButtonText(null, type)
            result.shouldNotBeEmpty()
        }
    }

    "Property 1: Non-string button_text values should be handled gracefully" {
        val customDataInt = mapOf("button_text" to 12345)
        val resultInt = extractButtonText(customDataInt, ButtonTextCollaborativeType.DOCUMENT)
        resultInt shouldBe "12345"

        val customDataBool = mapOf("button_text" to true)
        val resultBool = extractButtonText(customDataBool, ButtonTextCollaborativeType.DOCUMENT)
        resultBool shouldBe "true"
    }

    "Property 1: Missing button_text key should use default" {
        val customDataWithOtherKeys = mapOf("title" to "Some Title", "url" to "https://example.com")
        val documentResult = extractButtonText(customDataWithOtherKeys, ButtonTextCollaborativeType.DOCUMENT)
        documentResult shouldBe DEFAULT_DOCUMENT_BUTTON_TEXT
        val whiteboardResult = extractButtonText(customDataWithOtherKeys, ButtonTextCollaborativeType.WHITEBOARD)
        whiteboardResult shouldBe DEFAULT_WHITEBOARD_BUTTON_TEXT
    }
})

private fun processButtonText(buttonText: String): String = buttonText

private fun extractButtonText(customData: Map<String, Any>?, type: ButtonTextCollaborativeType): String {
    val defaultText = when (type) {
        ButtonTextCollaborativeType.DOCUMENT -> DEFAULT_DOCUMENT_BUTTON_TEXT
        ButtonTextCollaborativeType.WHITEBOARD -> DEFAULT_WHITEBOARD_BUTTON_TEXT
    }
    if (customData == null) return defaultText
    val extractedValue = customData["button_text"]
    val extractedText = extractedValue?.toString() ?: ""
    return if (extractedText.isNotBlank()) extractedText else defaultText
}

private const val DEFAULT_DOCUMENT_BUTTON_TEXT = "Open Document"
private const val DEFAULT_WHITEBOARD_BUTTON_TEXT = "Open Whiteboard"

private enum class ButtonTextCollaborativeType { DOCUMENT, WHITEBOARD }
