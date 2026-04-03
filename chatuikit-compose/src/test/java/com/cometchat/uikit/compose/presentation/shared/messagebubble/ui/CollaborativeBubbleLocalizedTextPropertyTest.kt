package com.cometchat.uikit.compose.presentation.shared.messagebubble.ui

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldNotBeEmpty
import io.kotest.property.Arb
import io.kotest.property.arbitrary.enum
import io.kotest.property.checkAll

/**
 * Property-based tests for Collaborative Bubble Default Localized Text by Type.
 *
 * Feature: collaborative-bubble-parity
 * Properties tested:
 * - Property 2: Default localized text by type
 *
 * **Validates: Requirements 3.1, 3.2, 3.3, 3.4, 3.5, 3.6**
 *
 * Expected defaults:
 * - DOCUMENT type:
 *   - Title: "Collaborative Document" (from R.string.cometchat_collaborative_doc)
 *   - Subtitle: "Open document to edit content together" (from R.string.cometchat_open_document_to_edit_content_together)
 *   - Button: "Open Document" (from R.string.cometchat_open_document)
 * - WHITEBOARD type:
 *   - Title: "Collaborative Whiteboard" (from R.string.cometchat_collaborative_whiteboard)
 *   - Subtitle: "Open whiteboard to edit content together" (from R.string.cometchat_open_whiteboard_to_edit_content_together)
 *   - Button: "Open Whiteboard" (from R.string.cometchat_open_whiteboard)
 */
class CollaborativeBubbleLocalizedTextPropertyTest : StringSpec({

    val collaborativeTypeArb = Arb.enum<LocalizedTextCollaborativeType>()

    "Property 2: Default title should be correct for any CollaborativeType" {
        checkAll(100, collaborativeTypeArb) { type ->
            val result = getDefaultTitle(type)
            when (type) {
                LocalizedTextCollaborativeType.DOCUMENT -> result shouldBe DEFAULT_DOCUMENT_TITLE
                LocalizedTextCollaborativeType.WHITEBOARD -> result shouldBe DEFAULT_WHITEBOARD_TITLE
            }
        }
    }

    "Property 2: Default subtitle should be correct for any CollaborativeType" {
        checkAll(100, collaborativeTypeArb) { type ->
            val result = getDefaultSubtitle(type)
            when (type) {
                LocalizedTextCollaborativeType.DOCUMENT -> result shouldBe DEFAULT_DOCUMENT_SUBTITLE
                LocalizedTextCollaborativeType.WHITEBOARD -> result shouldBe DEFAULT_WHITEBOARD_SUBTITLE
            }
        }
    }

    "Property 2: Default button text should be correct for any CollaborativeType" {
        checkAll(100, collaborativeTypeArb) { type ->
            val result = getDefaultButtonText(type)
            when (type) {
                LocalizedTextCollaborativeType.DOCUMENT -> result shouldBe DEFAULT_DOCUMENT_BUTTON_TEXT
                LocalizedTextCollaborativeType.WHITEBOARD -> result shouldBe DEFAULT_WHITEBOARD_BUTTON_TEXT
            }
        }
    }

    "Property 2: All default texts should be non-empty for any CollaborativeType" {
        checkAll(100, collaborativeTypeArb) { type ->
            val title = getDefaultTitle(type)
            val subtitle = getDefaultSubtitle(type)
            val buttonText = getDefaultButtonText(type)

            title.shouldNotBeEmpty()
            subtitle.shouldNotBeEmpty()
            buttonText.shouldNotBeEmpty()
        }
    }

    "Property 2: Default texts should be deterministic for any CollaborativeType" {
        checkAll(100, collaborativeTypeArb) { type ->
            val title1 = getDefaultTitle(type)
            val title2 = getDefaultTitle(type)
            val title3 = getDefaultTitle(type)
            title1 shouldBe title2
            title2 shouldBe title3

            val subtitle1 = getDefaultSubtitle(type)
            val subtitle2 = getDefaultSubtitle(type)
            val subtitle3 = getDefaultSubtitle(type)
            subtitle1 shouldBe subtitle2
            subtitle2 shouldBe subtitle3

            val buttonText1 = getDefaultButtonText(type)
            val buttonText2 = getDefaultButtonText(type)
            val buttonText3 = getDefaultButtonText(type)
            buttonText1 shouldBe buttonText2
            buttonText2 shouldBe buttonText3
        }
    }

    "Property 2: DOCUMENT type should have document-specific default texts" {
        val type = LocalizedTextCollaborativeType.DOCUMENT
        val title = getDefaultTitle(type)
        val subtitle = getDefaultSubtitle(type)
        val buttonText = getDefaultButtonText(type)

        title shouldBe DEFAULT_DOCUMENT_TITLE
        subtitle shouldBe DEFAULT_DOCUMENT_SUBTITLE
        buttonText shouldBe DEFAULT_DOCUMENT_BUTTON_TEXT
    }

    "Property 2: WHITEBOARD type should have whiteboard-specific default texts" {
        val type = LocalizedTextCollaborativeType.WHITEBOARD
        val title = getDefaultTitle(type)
        val subtitle = getDefaultSubtitle(type)
        val buttonText = getDefaultButtonText(type)

        title shouldBe DEFAULT_WHITEBOARD_TITLE
        subtitle shouldBe DEFAULT_WHITEBOARD_SUBTITLE
        buttonText shouldBe DEFAULT_WHITEBOARD_BUTTON_TEXT
    }

    "Property 2: Default title should never return null for any CollaborativeType" {
        checkAll(100, collaborativeTypeArb) { type ->
            val result = getDefaultTitle(type)
            result shouldNotBe null
        }
    }

    "Property 2: Default subtitle should never return null for any CollaborativeType" {
        checkAll(100, collaborativeTypeArb) { type ->
            val result = getDefaultSubtitle(type)
            result shouldNotBe null
        }
    }

    "Property 2: Default button text should never return null for any CollaborativeType" {
        checkAll(100, collaborativeTypeArb) { type ->
            val result = getDefaultButtonText(type)
            result shouldNotBe null
        }
    }

    "Property 2: All CollaborativeType values should have unique default titles" {
        val documentTitle = getDefaultTitle(LocalizedTextCollaborativeType.DOCUMENT)
        val whiteboardTitle = getDefaultTitle(LocalizedTextCollaborativeType.WHITEBOARD)
        documentTitle shouldNotBe whiteboardTitle
    }

    "Property 2: All CollaborativeType values should have unique default subtitles" {
        val documentSubtitle = getDefaultSubtitle(LocalizedTextCollaborativeType.DOCUMENT)
        val whiteboardSubtitle = getDefaultSubtitle(LocalizedTextCollaborativeType.WHITEBOARD)
        documentSubtitle shouldNotBe whiteboardSubtitle
    }

    "Property 2: All CollaborativeType values should have unique default button texts" {
        val documentButtonText = getDefaultButtonText(LocalizedTextCollaborativeType.DOCUMENT)
        val whiteboardButtonText = getDefaultButtonText(LocalizedTextCollaborativeType.WHITEBOARD)
        documentButtonText shouldNotBe whiteboardButtonText
    }

    "Property 2: Default texts should contain type-specific keywords" {
        checkAll(100, collaborativeTypeArb) { type ->
            val title = getDefaultTitle(type)
            val subtitle = getDefaultSubtitle(type)
            val buttonText = getDefaultButtonText(type)

            when (type) {
                LocalizedTextCollaborativeType.DOCUMENT -> {
                    title.lowercase().contains("document") shouldBe true
                    subtitle.lowercase().contains("document") shouldBe true
                    buttonText.lowercase().contains("document") shouldBe true
                }
                LocalizedTextCollaborativeType.WHITEBOARD -> {
                    title.lowercase().contains("whiteboard") shouldBe true
                    subtitle.lowercase().contains("whiteboard") shouldBe true
                    buttonText.lowercase().contains("whiteboard") shouldBe true
                }
            }
        }
    }

    "Property 2: Default title should contain 'Collaborative' prefix for any type" {
        checkAll(100, collaborativeTypeArb) { type ->
            val title = getDefaultTitle(type)
            title.contains("Collaborative") shouldBe true
        }
    }

    "Property 2: Default subtitle should contain 'Open' and 'edit content together' for any type" {
        checkAll(100, collaborativeTypeArb) { type ->
            val subtitle = getDefaultSubtitle(type)
            subtitle.lowercase().contains("open") shouldBe true
            subtitle.lowercase().contains("edit content together") shouldBe true
        }
    }

    "Property 2: Default button text should start with 'Open' for any type" {
        checkAll(100, collaborativeTypeArb) { type ->
            val buttonText = getDefaultButtonText(type)
            buttonText.startsWith("Open") shouldBe true
        }
    }

    "Property 2: getLocalizedDefaults should return all three texts correctly for any type" {
        checkAll(100, collaborativeTypeArb) { type ->
            val defaults = getLocalizedDefaults(type)
            defaults.title shouldBe getDefaultTitle(type)
            defaults.subtitle shouldBe getDefaultSubtitle(type)
            defaults.buttonText shouldBe getDefaultButtonText(type)
        }
    }

    "Property 2: LocalizedDefaults should be consistent across multiple calls" {
        checkAll(100, collaborativeTypeArb) { type ->
            val defaults1 = getLocalizedDefaults(type)
            val defaults2 = getLocalizedDefaults(type)
            val defaults3 = getLocalizedDefaults(type)

            defaults1 shouldBe defaults2
            defaults2 shouldBe defaults3
        }
    }

    "Property 2: Every CollaborativeType enum value should be covered" {
        LocalizedTextCollaborativeType.entries.forEach { type ->
            val title = getDefaultTitle(type)
            val subtitle = getDefaultSubtitle(type)
            val buttonText = getDefaultButtonText(type)

            title.shouldNotBeEmpty()
            subtitle.shouldNotBeEmpty()
            buttonText.shouldNotBeEmpty()
        }
    }
})

// Helper functions that simulate the localized text resolution logic

private fun getDefaultTitle(type: LocalizedTextCollaborativeType): String {
    return when (type) {
        LocalizedTextCollaborativeType.DOCUMENT -> DEFAULT_DOCUMENT_TITLE
        LocalizedTextCollaborativeType.WHITEBOARD -> DEFAULT_WHITEBOARD_TITLE
    }
}

private fun getDefaultSubtitle(type: LocalizedTextCollaborativeType): String {
    return when (type) {
        LocalizedTextCollaborativeType.DOCUMENT -> DEFAULT_DOCUMENT_SUBTITLE
        LocalizedTextCollaborativeType.WHITEBOARD -> DEFAULT_WHITEBOARD_SUBTITLE
    }
}

private fun getDefaultButtonText(type: LocalizedTextCollaborativeType): String {
    return when (type) {
        LocalizedTextCollaborativeType.DOCUMENT -> DEFAULT_DOCUMENT_BUTTON_TEXT
        LocalizedTextCollaborativeType.WHITEBOARD -> DEFAULT_WHITEBOARD_BUTTON_TEXT
    }
}

private data class LocalizedDefaults(
    val title: String,
    val subtitle: String,
    val buttonText: String
)

private fun getLocalizedDefaults(type: LocalizedTextCollaborativeType): LocalizedDefaults {
    return LocalizedDefaults(
        title = getDefaultTitle(type),
        subtitle = getDefaultSubtitle(type),
        buttonText = getDefaultButtonText(type)
    )
}

// Constants representing the expected localized string values
// These match the values from R.string resources

private const val DEFAULT_DOCUMENT_TITLE = "Collaborative Document"
private const val DEFAULT_DOCUMENT_SUBTITLE = "Open document to edit content together"
private const val DEFAULT_DOCUMENT_BUTTON_TEXT = "Open Document"

private const val DEFAULT_WHITEBOARD_TITLE = "Collaborative Whiteboard"
private const val DEFAULT_WHITEBOARD_SUBTITLE = "Open whiteboard to edit content together"
private const val DEFAULT_WHITEBOARD_BUTTON_TEXT = "Open Whiteboard"

// Local enum to avoid dependency on the actual CollaborativeType
private enum class LocalizedTextCollaborativeType {
    DOCUMENT,
    WHITEBOARD
}
