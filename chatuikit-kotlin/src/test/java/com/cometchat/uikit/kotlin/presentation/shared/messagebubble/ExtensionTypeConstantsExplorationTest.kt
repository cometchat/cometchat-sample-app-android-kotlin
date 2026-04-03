package com.cometchat.uikit.kotlin.presentation.shared.messagebubble

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

/**
 * Bug Condition Exploration Test for Extension Bubble Display Fix.
 *
 * This test verifies that the extension type constants in InternalContentRenderer
 * match the expected SDK message type values. The factory key is generated as
 * "${category}_${type}", so the constants MUST match the actual message type
 * values from the SDK for bubbles to be created correctly.
 *
 * **Bug Context:**
 * Extension bubbles (polls, document, whiteboard) are not displaying because
 * the constants in InternalContentRenderer have incorrect values:
 * - EXTENSION_POLLS = "polls" (should be "extension_poll")
 * - EXTENSION_DOCUMENT = "document" (should be "extension_document")
 * - EXTENSION_WHITEBOARD = "whiteboard" (should be "extension_whiteboard")
 *
 * **EXPECTED OUTCOME ON UNFIXED CODE:** Test FAILS
 * This failure confirms the bug exists because the constants have wrong values.
 *
 * **Validates: Requirements 1.1, 1.2, 1.3**
 *
 * Feature: sticker-bubble-display-fix
 */
class ExtensionTypeConstantsExplorationTest : FunSpec({

    /**
     * Property 1: Fault Condition - Extension Bubble Factory Key Mismatch
     *
     * The SDK sends messages with type "extension_poll", "extension_document",
     * and "extension_whiteboard". The InternalContentRenderer constants MUST
     * match these values exactly for the factory key lookup to succeed.
     *
     * **Validates: Requirements 1.1, 1.2, 1.3**
     */
    context("Extension type constants should match SDK message type values") {

        test("EXTENSION_POLLS constant should be 'extension_poll' to match SDK poll message type") {
            // SDK sends poll messages with type "extension_poll"
            // The constant must match for BubbleFactory.getKey("custom", EXTENSION_POLLS)
            // to return "custom_extension_poll" which matches the actual factory key
            val expectedValue = "extension_poll"
            val actualValue = InternalContentRenderer.EXTENSION_POLLS

            // This assertion will FAIL on unfixed code because:
            // - Current value: "polls"
            // - Expected value: "extension_poll"
            // - Generated key with current: "custom_polls"
            // - Actual message key: "custom_extension_poll"
            // - Result: Keys don't match → poll bubble not created
            actualValue shouldBe expectedValue
        }

        test("EXTENSION_DOCUMENT constant should be 'extension_document' to match SDK document message type") {
            // SDK sends collaborative document messages with type "extension_document"
            // The constant must match for BubbleFactory.getKey("custom", EXTENSION_DOCUMENT)
            // to return "custom_extension_document" which matches the actual factory key
            val expectedValue = "extension_document"
            val actualValue = InternalContentRenderer.EXTENSION_DOCUMENT

            // This assertion will FAIL on unfixed code because:
            // - Current value: "document"
            // - Expected value: "extension_document"
            // - Generated key with current: "custom_document"
            // - Actual message key: "custom_extension_document"
            // - Result: Keys don't match → document bubble not created
            actualValue shouldBe expectedValue
        }

        test("EXTENSION_WHITEBOARD constant should be 'extension_whiteboard' to match SDK whiteboard message type") {
            // SDK sends collaborative whiteboard messages with type "extension_whiteboard"
            // The constant must match for BubbleFactory.getKey("custom", EXTENSION_WHITEBOARD)
            // to return "custom_extension_whiteboard" which matches the actual factory key
            val expectedValue = "extension_whiteboard"
            val actualValue = InternalContentRenderer.EXTENSION_WHITEBOARD

            // This assertion will FAIL on unfixed code because:
            // - Current value: "whiteboard"
            // - Expected value: "extension_whiteboard"
            // - Generated key with current: "custom_whiteboard"
            // - Actual message key: "custom_extension_whiteboard"
            // - Result: Keys don't match → whiteboard bubble not created
            actualValue shouldBe expectedValue
        }
    }

    context("Factory key generation should produce correct keys for extension types") {

        test("BubbleFactory.getKey('custom', EXTENSION_POLLS) should return 'custom_extension_poll'") {
            // When a poll message arrives with category "custom" and type "extension_poll",
            // the factory key is "custom_extension_poll"
            // The renderer must generate the same key to find the correct bubble factory
            val expectedKey = "custom_extension_poll"
            val actualKey = BubbleFactory.getKey("custom", InternalContentRenderer.EXTENSION_POLLS)

            // This assertion will FAIL on unfixed code because:
            // - EXTENSION_POLLS = "polls"
            // - Generated key: "custom_polls"
            // - Expected key: "custom_extension_poll"
            actualKey shouldBe expectedKey
        }

        test("BubbleFactory.getKey('custom', EXTENSION_DOCUMENT) should return 'custom_extension_document'") {
            // When a document message arrives with category "custom" and type "extension_document",
            // the factory key is "custom_extension_document"
            // The renderer must generate the same key to find the correct bubble factory
            val expectedKey = "custom_extension_document"
            val actualKey = BubbleFactory.getKey("custom", InternalContentRenderer.EXTENSION_DOCUMENT)

            // This assertion will FAIL on unfixed code because:
            // - EXTENSION_DOCUMENT = "document"
            // - Generated key: "custom_document"
            // - Expected key: "custom_extension_document"
            actualKey shouldBe expectedKey
        }

        test("BubbleFactory.getKey('custom', EXTENSION_WHITEBOARD) should return 'custom_extension_whiteboard'") {
            // When a whiteboard message arrives with category "custom" and type "extension_whiteboard",
            // the factory key is "custom_extension_whiteboard"
            // The renderer must generate the same key to find the correct bubble factory
            val expectedKey = "custom_extension_whiteboard"
            val actualKey = BubbleFactory.getKey("custom", InternalContentRenderer.EXTENSION_WHITEBOARD)

            // This assertion will FAIL on unfixed code because:
            // - EXTENSION_WHITEBOARD = "whiteboard"
            // - Generated key: "custom_whiteboard"
            // - Expected key: "custom_extension_whiteboard"
            actualKey shouldBe expectedKey
        }
    }
})
