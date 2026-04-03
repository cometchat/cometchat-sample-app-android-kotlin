package com.cometchat.uikit.kotlin.presentation.shared.messagebubble

import com.cometchat.chat.constants.CometChatConstants
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.element
import io.kotest.property.checkAll

/**
 * Preservation Property Tests for Extension Bubble Display Fix.
 *
 * These tests verify that existing functionality is preserved and will NOT be
 * affected by the fix. They establish a baseline of correct behavior that must
 * continue to work after the fix is applied.
 *
 * **Property 2: Preservation** - Existing Message Types Unchanged
 *
 * **Key Observations on UNFIXED code:**
 * - `EXTENSION_STICKER` constant is `"extension_sticker"` (already correct)
 * - Standard message type constants (TEXT, IMAGE, VIDEO, AUDIO, FILE) are SDK constants
 * - These are NOT affected by the extension constant changes
 *
 * **EXPECTED OUTCOME ON UNFIXED CODE:** Tests PASS
 * This confirms the baseline behavior that must be preserved.
 *
 * **Validates: Requirements 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.7, 3.8, 3.9**
 *
 * Feature: sticker-bubble-display-fix
 */
class ExtensionTypePreservationTest : FunSpec({

    /**
     * Property 2: Preservation - Sticker Extension Constant
     *
     * The sticker constant is already correct and should remain unchanged.
     * This test verifies the baseline behavior that must be preserved.
     *
     * **Validates: Requirement 3.9**
     */
    context("Sticker extension constant should remain correct (already working)") {

        test("EXTENSION_STICKER constant should be 'extension_sticker'") {
            // The sticker constant is already correct in the unfixed code
            // This test establishes the baseline that must be preserved
            val expectedValue = "extension_sticker"
            val actualValue = InternalContentRenderer.EXTENSION_STICKER

            // This should PASS on unfixed code - sticker is already correct
            actualValue shouldBe expectedValue
        }

        test("BubbleFactory.getKey('custom', EXTENSION_STICKER) should return 'custom_extension_sticker'") {
            // When a sticker message arrives with category "custom" and type "extension_sticker",
            // the factory key is "custom_extension_sticker"
            // The renderer correctly generates this key (already working)
            val expectedKey = "custom_extension_sticker"
            val actualKey = BubbleFactory.getKey("custom", InternalContentRenderer.EXTENSION_STICKER)

            // This should PASS on unfixed code - sticker key generation is correct
            actualKey shouldBe expectedKey
        }
    }

    /**
     * Property 2: Preservation - Standard Message Types
     *
     * Standard message types (text, image, video, audio, file) use SDK constants
     * and are NOT affected by the extension constant changes.
     *
     * **Validates: Requirements 3.1, 3.2, 3.3, 3.4, 3.5**
     */
    context("Standard message type factory keys should be unaffected") {

        test("Text message factory key should be 'message_text'") {
            // Text messages use SDK constant MESSAGE_TYPE_TEXT = "text"
            val expectedKey = "message_text"
            val actualKey = BubbleFactory.getKey(
                CometChatConstants.CATEGORY_MESSAGE,
                CometChatConstants.MESSAGE_TYPE_TEXT
            )

            // This should PASS - standard message types are unaffected
            actualKey shouldBe expectedKey
        }

        test("Image message factory key should be 'message_image'") {
            // Image messages use SDK constant MESSAGE_TYPE_IMAGE = "image"
            val expectedKey = "message_image"
            val actualKey = BubbleFactory.getKey(
                CometChatConstants.CATEGORY_MESSAGE,
                CometChatConstants.MESSAGE_TYPE_IMAGE
            )

            // This should PASS - standard message types are unaffected
            actualKey shouldBe expectedKey
        }

        test("Video message factory key should be 'message_video'") {
            // Video messages use SDK constant MESSAGE_TYPE_VIDEO = "video"
            val expectedKey = "message_video"
            val actualKey = BubbleFactory.getKey(
                CometChatConstants.CATEGORY_MESSAGE,
                CometChatConstants.MESSAGE_TYPE_VIDEO
            )

            // This should PASS - standard message types are unaffected
            actualKey shouldBe expectedKey
        }

        test("Audio message factory key should be 'message_audio'") {
            // Audio messages use SDK constant MESSAGE_TYPE_AUDIO = "audio"
            val expectedKey = "message_audio"
            val actualKey = BubbleFactory.getKey(
                CometChatConstants.CATEGORY_MESSAGE,
                CometChatConstants.MESSAGE_TYPE_AUDIO
            )

            // This should PASS - standard message types are unaffected
            actualKey shouldBe expectedKey
        }

        test("File message factory key should be 'message_file'") {
            // File messages use SDK constant MESSAGE_TYPE_FILE = "file"
            val expectedKey = "message_file"
            val actualKey = BubbleFactory.getKey(
                CometChatConstants.CATEGORY_MESSAGE,
                CometChatConstants.MESSAGE_TYPE_FILE
            )

            // This should PASS - standard message types are unaffected
            actualKey shouldBe expectedKey
        }
    }

    /**
     * Property 2: Preservation - Action and Call Message Types
     *
     * Action and call message types use SDK constants and are NOT affected
     * by the extension constant changes.
     *
     * **Validates: Requirements 3.6, 3.7, 3.8**
     */
    context("Action and call message factory keys should be unaffected") {

        test("Group action message factory key should use SDK constants") {
            // Group action messages use SDK constant ACTION_TYPE_GROUP_MEMBER
            val expectedKey = "action_groupMember"
            val actualKey = BubbleFactory.getKey(
                CometChatConstants.CATEGORY_ACTION,
                CometChatConstants.ActionKeys.ACTION_TYPE_GROUP_MEMBER
            )

            // This should PASS - action message types are unaffected
            actualKey shouldBe expectedKey
        }

        test("Audio call message factory key should use SDK constants") {
            // Audio call messages use SDK constant CALL_TYPE_AUDIO = "audio"
            val expectedKey = "call_audio"
            val actualKey = BubbleFactory.getKey(
                CometChatConstants.CATEGORY_CALL,
                CometChatConstants.CALL_TYPE_AUDIO
            )

            // This should PASS - call message types are unaffected
            actualKey shouldBe expectedKey
        }

        test("Video call message factory key should use SDK constants") {
            // Video call messages use SDK constant CALL_TYPE_VIDEO = "video"
            val expectedKey = "call_video"
            val actualKey = BubbleFactory.getKey(
                CometChatConstants.CATEGORY_CALL,
                CometChatConstants.CALL_TYPE_VIDEO
            )

            // This should PASS - call message types are unaffected
            actualKey shouldBe expectedKey
        }
    }

    /**
     * Property 2: Preservation - Factory Key Format Consistency
     *
     * Property-based test to verify that factory key generation follows
     * the consistent format "{category}_{type}" for all message types.
     *
     * **Validates: Requirements 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.7, 3.8, 3.9**
     */
    context("Factory key format should be consistent for all preserved message types") {

        // Arbitrary generator for standard message types that should be preserved
        val preservedMessageTypeArb = Arb.element(
            // Standard message types (Requirements 3.1-3.5)
            CometChatConstants.MESSAGE_TYPE_TEXT,
            CometChatConstants.MESSAGE_TYPE_IMAGE,
            CometChatConstants.MESSAGE_TYPE_VIDEO,
            CometChatConstants.MESSAGE_TYPE_AUDIO,
            CometChatConstants.MESSAGE_TYPE_FILE
        )

        // Arbitrary generator for categories
        val categoryArb = Arb.element(
            CometChatConstants.CATEGORY_MESSAGE,
            CometChatConstants.CATEGORY_CUSTOM,
            CometChatConstants.CATEGORY_ACTION,
            CometChatConstants.CATEGORY_CALL
        )

        test("Factory key format should be '{category}_{type}' for all standard message types") {
            checkAll(20, categoryArb, preservedMessageTypeArb) { category, type ->
                val factoryKey = BubbleFactory.getKey(category, type)

                // Verify format is {category}_{type}
                factoryKey shouldBe "${category}_${type}"
            }
        }

        test("Sticker factory key should follow the same format pattern") {
            // Sticker uses the EXTENSION_STICKER constant which is already correct
            val stickerKey = BubbleFactory.getKey(
                CometChatConstants.CATEGORY_CUSTOM,
                InternalContentRenderer.EXTENSION_STICKER
            )

            // Verify it follows the same format
            stickerKey shouldBe "${CometChatConstants.CATEGORY_CUSTOM}_${InternalContentRenderer.EXTENSION_STICKER}"
            stickerKey shouldBe "custom_extension_sticker"
        }
    }

    /**
     * Property 2: Preservation - Deleted Message Key
     *
     * Deleted messages use a special key that should remain unchanged.
     *
     * **Validates: Requirement 3.6**
     */
    context("Deleted message key should remain unchanged") {

        test("DELETED_KEY constant should be 'deleted'") {
            // Deleted messages use a special key regardless of original type
            val expectedKey = "deleted"
            val actualKey = BubbleFactory.DELETED_KEY

            // This should PASS - deleted key is unaffected
            actualKey shouldBe expectedKey
        }
    }
})
