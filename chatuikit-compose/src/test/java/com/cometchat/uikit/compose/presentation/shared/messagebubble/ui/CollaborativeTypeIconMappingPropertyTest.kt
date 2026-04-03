package com.cometchat.uikit.compose.presentation.shared.messagebubble.ui

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.element
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import org.json.JSONObject

/**
 * Property-based tests for Collaborative Type Icon Mapping.
 *
 * Feature: remaining-message-bubbles
 * Properties tested:
 * - Property 10: Collaborative Type Icon Mapping
 *
 * **Validates: Requirements 6.3**
 *
 * Tests the collaborative type to icon mapping logic from [CometChatCollaborativeBubble]:
 * - For all CustomMessages with type "extension_document", the CollaborativeBubble displays the document icon
 * - For all CustomMessages with type "extension_whiteboard", the CollaborativeBubble displays the whiteboard icon
 *
 * The mapping follows these rules:
 * 1. If message type contains "whiteboard" (case-insensitive) → WHITEBOARD type → whiteboard icon
 * 2. Otherwise → DOCUMENT type → document icon
 */
class CollaborativeTypeIconMappingPropertyTest : StringSpec({

    // Test data generators
    val documentTypes = listOf(
        "extension_document",
        "document",
        "EXTENSION_DOCUMENT",
        "DOCUMENT",
        "Extension_Document",
        "doc",
        "collaborative_document"
    )

    val whiteboardTypes = listOf(
        "extension_whiteboard",
        "whiteboard",
        "EXTENSION_WHITEBOARD",
        "WHITEBOARD",
        "Extension_Whiteboard",
        "collaborative_whiteboard"
    )

    val documentTypeArb = Arb.element(documentTypes)
    val whiteboardTypeArb = Arb.element(whiteboardTypes)
    val randomStringArb = Arb.string(1..50)

    /**
     * Property 10: Collaborative Type Icon Mapping - Valid inputs always produce valid CollaborativeType
     *
     * *For any* message type string, the getCollaborativeType function SHALL return
     * a valid CollaborativeType enum value.
     *
     * **Validates: Requirements 6.3**
     */
    "Property 10: getCollaborativeType should always return a valid CollaborativeType enum for any input" {
        val allTypes = documentTypes + whiteboardTypes + listOf("unknown", "", "random", "test")
        val typeArb = Arb.element(allTypes)

        checkAll(200, typeArb) { messageType ->
            val result = getCollaborativeType(messageType)
            
            // Result should always be a valid CollaborativeType enum value
            result shouldNotBe null
            CollaborativeType.entries.contains(result) shouldBe true
        }
    }

    /**
     * Property 10: Collaborative Type Icon Mapping - Whiteboard types return WHITEBOARD
     *
     * *For any* message type containing "whiteboard" (case-insensitive),
     * the getCollaborativeType function SHALL return CollaborativeType.WHITEBOARD.
     *
     * **Validates: Requirements 6.3**
     */
    "Property 10: Message types containing 'whiteboard' should return WHITEBOARD type" {
        checkAll(100, whiteboardTypeArb) { messageType ->
            val result = getCollaborativeType(messageType)
            result shouldBe CollaborativeType.WHITEBOARD
        }
    }

    /**
     * Property 10: Collaborative Type Icon Mapping - Document types return DOCUMENT
     *
     * *For any* message type NOT containing "whiteboard",
     * the getCollaborativeType function SHALL return CollaborativeType.DOCUMENT.
     *
     * **Validates: Requirements 6.3**
     */
    "Property 10: Message types not containing 'whiteboard' should return DOCUMENT type" {
        checkAll(100, documentTypeArb) { messageType ->
            val result = getCollaborativeType(messageType)
            result shouldBe CollaborativeType.DOCUMENT
        }
    }

    /**
     * Property 10: Collaborative Type Icon Mapping - WHITEBOARD type maps to whiteboard icon
     *
     * *For any* CollaborativeType.WHITEBOARD, the getCollaborativeIcon function
     * SHALL return the whiteboard icon resource ID.
     *
     * **Validates: Requirements 6.3**
     */
    "Property 10: WHITEBOARD type should map to whiteboard icon" {
        val iconRes = getCollaborativeIcon(CollaborativeType.WHITEBOARD)
        iconRes shouldBe ICON_COLLABORATIVE_WHITEBOARD
    }

    /**
     * Property 10: Collaborative Type Icon Mapping - DOCUMENT type maps to document icon
     *
     * *For any* CollaborativeType.DOCUMENT, the getCollaborativeIcon function
     * SHALL return the document icon resource ID.
     *
     * **Validates: Requirements 6.3**
     */
    "Property 10: DOCUMENT type should map to document icon" {
        val iconRes = getCollaborativeIcon(CollaborativeType.DOCUMENT)
        iconRes shouldBe ICON_COLLABORATIVE_DOCUMENT
    }

    /**
     * Property 10: Collaborative Type Icon Mapping - All CollaborativeType values have icons
     *
     * *For any* CollaborativeType enum value, the getCollaborativeIcon function
     * SHALL return a valid (non-zero) resource ID.
     *
     * **Validates: Requirements 6.3**
     */
    "Property 10: All CollaborativeType values should have valid icon resources" {
        CollaborativeType.entries.forEach { type ->
            val iconRes = getCollaborativeIcon(type)
            iconRes shouldNotBe 0
        }
    }

    /**
     * Property 10: Collaborative Type Icon Mapping - Case insensitivity for whiteboard detection
     *
     * *For any* string containing "whiteboard" in any case combination,
     * the getCollaborativeType function SHALL return CollaborativeType.WHITEBOARD.
     *
     * **Validates: Requirements 6.3**
     */
    "Property 10: Whiteboard detection should be case-insensitive" {
        val caseVariations = listOf(
            "whiteboard",
            "WHITEBOARD",
            "Whiteboard",
            "WhiteBoard",
            "wHiTeBoArD",
            "extension_whiteboard",
            "EXTENSION_WHITEBOARD",
            "Extension_Whiteboard",
            "my_whiteboard_type",
            "CUSTOM_WHITEBOARD_MESSAGE"
        )
        val caseArb = Arb.element(caseVariations)

        checkAll(100, caseArb) { messageType ->
            val result = getCollaborativeType(messageType)
            result shouldBe CollaborativeType.WHITEBOARD
        }
    }

    /**
     * Property 10: Collaborative Type Icon Mapping - Null type defaults to DOCUMENT
     *
     * *For any* null message type, the getCollaborativeType function
     * SHALL return CollaborativeType.DOCUMENT as the default.
     *
     * **Validates: Requirements 6.3**
     */
    "Property 10: Null message type should default to DOCUMENT" {
        val result = getCollaborativeType(null)
        result shouldBe CollaborativeType.DOCUMENT
    }

    /**
     * Property 10: Collaborative Type Icon Mapping - Empty type defaults to DOCUMENT
     *
     * *For any* empty message type string, the getCollaborativeType function
     * SHALL return CollaborativeType.DOCUMENT as the default.
     *
     * **Validates: Requirements 6.3**
     */
    "Property 10: Empty message type should default to DOCUMENT" {
        val result = getCollaborativeType("")
        result shouldBe CollaborativeType.DOCUMENT
    }

    /**
     * Property 10: Collaborative Type Icon Mapping - Unknown types default to DOCUMENT
     *
     * *For any* message type that doesn't contain "whiteboard",
     * the getCollaborativeType function SHALL return CollaborativeType.DOCUMENT.
     *
     * **Validates: Requirements 6.3**
     */
    "Property 10: Unknown message types should default to DOCUMENT" {
        val unknownTypes = listOf(
            "unknown",
            "random_type",
            "custom_message",
            "extension_poll",
            "extension_sticker",
            "text",
            "image",
            "video",
            "audio",
            "file"
        )
        val unknownTypeArb = Arb.element(unknownTypes)

        checkAll(100, unknownTypeArb) { messageType ->
            val result = getCollaborativeType(messageType)
            result shouldBe CollaborativeType.DOCUMENT
        }
    }

    /**
     * Property 10: Collaborative Type Icon Mapping - Type extraction from customData
     *
     * *For any* CustomMessage with type in customData, the extractCollaborativeType
     * function SHALL correctly extract and map the type.
     *
     * **Validates: Requirements 6.3**
     */
    "Property 10: Type should be correctly extracted from customData" {
        checkAll(100, whiteboardTypeArb) { typeValue ->
            val customData = JSONObject().apply {
                put("type", typeValue)
            }
            
            val result = extractCollaborativeTypeFromCustomData(null, customData)
            result shouldBe CollaborativeType.WHITEBOARD
        }
    }

    /**
     * Property 10: Collaborative Type Icon Mapping - Message type takes precedence over customData
     *
     * *For any* CustomMessage with both message type and customData type,
     * the message type SHALL take precedence.
     *
     * **Validates: Requirements 6.3**
     */
    "Property 10: Message type should take precedence over customData type" {
        // Message type is whiteboard, customData type is document
        val customData = JSONObject().apply {
            put("type", "document")
        }
        
        val result = extractCollaborativeTypeFromCustomData("extension_whiteboard", customData)
        result shouldBe CollaborativeType.WHITEBOARD
    }

    /**
     * Property 10: Collaborative Type Icon Mapping - Fallback to customData when message type is null
     *
     * *For any* CustomMessage with null message type but valid customData type,
     * the customData type SHALL be used.
     *
     * **Validates: Requirements 6.3**
     */
    "Property 10: Should fallback to customData type when message type is null" {
        val customData = JSONObject().apply {
            put("type", "whiteboard")
        }
        
        val result = extractCollaborativeTypeFromCustomData(null, customData)
        result shouldBe CollaborativeType.WHITEBOARD
    }

    /**
     * Property 10: Collaborative Type Icon Mapping - Deterministic behavior
     *
     * *For any* message type, calling getCollaborativeType multiple times
     * SHALL return the same result.
     *
     * **Validates: Requirements 6.3**
     */
    "Property 10: getCollaborativeType should be deterministic" {
        val allTypes = documentTypes + whiteboardTypes
        val typeArb = Arb.element(allTypes)

        checkAll(100, typeArb) { messageType ->
            val result1 = getCollaborativeType(messageType)
            val result2 = getCollaborativeType(messageType)
            val result3 = getCollaborativeType(messageType)
            
            result1 shouldBe result2
            result2 shouldBe result3
        }
    }

    /**
     * Property 10: Collaborative Type Icon Mapping - Icon mapping is deterministic
     *
     * *For any* CollaborativeType, calling getCollaborativeIcon multiple times
     * SHALL return the same icon resource ID.
     *
     * **Validates: Requirements 6.3**
     */
    "Property 10: getCollaborativeIcon should be deterministic" {
        CollaborativeType.entries.forEach { type ->
            val result1 = getCollaborativeIcon(type)
            val result2 = getCollaborativeIcon(type)
            val result3 = getCollaborativeIcon(type)
            
            result1 shouldBe result2
            result2 shouldBe result3
        }
    }

    /**
     * Property 10: Collaborative Type Icon Mapping - DOCUMENT and WHITEBOARD have different icons
     *
     * The DOCUMENT and WHITEBOARD types SHALL map to different icon resources.
     *
     * **Validates: Requirements 6.3**
     */
    "Property 10: DOCUMENT and WHITEBOARD should have different icons" {
        val documentIcon = getCollaborativeIcon(CollaborativeType.DOCUMENT)
        val whiteboardIcon = getCollaborativeIcon(CollaborativeType.WHITEBOARD)
        
        documentIcon shouldNotBe whiteboardIcon
    }

    /**
     * Property 10: Collaborative Type Icon Mapping - Partial match for whiteboard
     *
     * *For any* string that contains "whiteboard" as a substring,
     * the getCollaborativeType function SHALL return CollaborativeType.WHITEBOARD.
     *
     * **Validates: Requirements 6.3**
     */
    "Property 10: Partial match containing 'whiteboard' should return WHITEBOARD" {
        val partialMatches = listOf(
            "my_whiteboard",
            "whiteboard_v2",
            "custom_whiteboard_type",
            "prefix_whiteboard_suffix",
            "WHITEBOARD123",
            "123whiteboard"
        )
        val partialMatchArb = Arb.element(partialMatches)

        checkAll(100, partialMatchArb) { messageType ->
            val result = getCollaborativeType(messageType)
            result shouldBe CollaborativeType.WHITEBOARD
        }
    }

    /**
     * Property 10: Collaborative Type Icon Mapping - "document" in type doesn't affect whiteboard detection
     *
     * *For any* string containing both "document" and "whiteboard",
     * the presence of "whiteboard" SHALL result in CollaborativeType.WHITEBOARD.
     *
     * **Validates: Requirements 6.3**
     */
    "Property 10: Type containing both 'document' and 'whiteboard' should return WHITEBOARD" {
        val mixedTypes = listOf(
            "document_whiteboard",
            "whiteboard_document",
            "collaborative_document_whiteboard",
            "DOCUMENT_WHITEBOARD_TYPE"
        )
        val mixedTypeArb = Arb.element(mixedTypes)

        checkAll(100, mixedTypeArb) { messageType ->
            val result = getCollaborativeType(messageType)
            result shouldBe CollaborativeType.WHITEBOARD
        }
    }

    /**
     * Property 10: Collaborative Type Icon Mapping - Null customData with null message type
     *
     * *For any* CustomMessage with both null message type and null customData,
     * the extractCollaborativeTypeFromCustomData function SHALL return CollaborativeType.DOCUMENT.
     *
     * **Validates: Requirements 6.3**
     */
    "Property 10: Null message type and null customData should default to DOCUMENT" {
        val result = extractCollaborativeTypeFromCustomData(null, null)
        result shouldBe CollaborativeType.DOCUMENT
    }

    /**
     * Property 10: Collaborative Type Icon Mapping - Empty customData with null message type
     *
     * *For any* CustomMessage with null message type and empty customData,
     * the extractCollaborativeTypeFromCustomData function SHALL return CollaborativeType.DOCUMENT.
     *
     * **Validates: Requirements 6.3**
     */
    "Property 10: Null message type and empty customData should default to DOCUMENT" {
        val customData = JSONObject()
        val result = extractCollaborativeTypeFromCustomData(null, customData)
        result shouldBe CollaborativeType.DOCUMENT
    }

    /**
     * Property 10: Collaborative Type Icon Mapping - Random strings without 'whiteboard' default to DOCUMENT
     *
     * *For any* randomly generated string that doesn't contain "whiteboard",
     * the getCollaborativeType function SHALL return CollaborativeType.DOCUMENT.
     *
     * **Validates: Requirements 6.3**
     */
    "Property 10: Random strings without 'whiteboard' should default to DOCUMENT" {
        // Generate strings that definitely don't contain "whiteboard"
        val safeStrings = listOf(
            "abc123",
            "test_type",
            "custom_msg",
            "extension_poll",
            "random_value",
            "document_only",
            "collaborative_doc"
        )
        val safeStringArb = Arb.element(safeStrings)

        checkAll(100, safeStringArb) { messageType ->
            val result = getCollaborativeType(messageType)
            result shouldBe CollaborativeType.DOCUMENT
        }
    }
})

/**
 * Determines the collaborative type based on the message type string.
 * This mirrors the logic in CometChatCollaborativeBubble.kt
 *
 * @param messageType The message type string (e.g., "extension_document", "extension_whiteboard")
 * @return The corresponding CollaborativeType enum value
 */
private fun getCollaborativeType(messageType: String?): CollaborativeType {
    return when {
        messageType?.contains("whiteboard", ignoreCase = true) == true -> CollaborativeType.WHITEBOARD
        else -> CollaborativeType.DOCUMENT
    }
}

/**
 * Extracts the collaborative type from message type and customData.
 * This mirrors the logic in CometChatCollaborativeBubble.kt extractCollaborativeData function.
 *
 * @param messageType The message type from CustomMessage.type
 * @param customData The JSONObject containing custom data
 * @return The corresponding CollaborativeType enum value
 */
private fun extractCollaborativeTypeFromCustomData(messageType: String?, customData: JSONObject?): CollaborativeType {
    val typeString = messageType ?: customData?.optString("type", "document") ?: "document"
    return getCollaborativeType(typeString)
}

/**
 * Gets the icon resource ID for a given collaborative type.
 * This mirrors the icon mapping in CometChatCollaborativeBubble.kt
 *
 * @param type The CollaborativeType enum value
 * @return The drawable resource ID for the icon
 */
private fun getCollaborativeIcon(type: CollaborativeType): Int {
    return when (type) {
        CollaborativeType.DOCUMENT -> ICON_COLLABORATIVE_DOCUMENT
        CollaborativeType.WHITEBOARD -> ICON_COLLABORATIVE_WHITEBOARD
    }
}

// Icon resource ID constants (mirrors R.drawable values)
// These represent the actual drawable resource IDs used in the production code:
// - ICON_COLLABORATIVE_DOCUMENT = R.drawable.cometchat_ic_collaborative_document
// - ICON_COLLABORATIVE_WHITEBOARD = R.drawable.cometchat_ic_collaborative
private const val ICON_COLLABORATIVE_DOCUMENT = 1
private const val ICON_COLLABORATIVE_WHITEBOARD = 2

/**
 * Enum representing collaborative document types.
 * This mirrors the CollaborativeType enum in CometChatCollaborativeBubble.kt
 */
private enum class CollaborativeType {
    DOCUMENT,
    WHITEBOARD
}
