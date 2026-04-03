package com.cometchat.uikit.compose.presentation.messagelist.ui

import com.cometchat.uikit.core.constants.UIKitConstants
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for the [shouldShowAvatar] utility function.
 *
 * **Validates: Requirements 1.1, 2.1, 2.2, 2.3**
 *
 * Tests cover all combinations from the decision table:
 * | Alignment | hideAvatar | isGroupConversation | Show Avatar? |
 * |-----------|------------|---------------------|--------------|
 * | RIGHT     | false      | true                | NO           |
 * | RIGHT     | false      | false               | NO           |
 * | RIGHT     | true       | true                | NO           |
 * | RIGHT     | true       | false               | NO           |
 * | CENTER    | false      | true                | NO           |
 * | CENTER    | false      | false               | NO           |
 * | CENTER    | true       | true                | NO           |
 * | CENTER    | true       | false               | NO           |
 * | LEFT      | false      | true                | YES          |
 * | LEFT      | false      | false               | NO           |
 * | LEFT      | true       | true                | NO           |
 * | LEFT      | true       | false               | NO           |
 */
class ShouldShowAvatarTest {

    // ========================================
    // RIGHT Alignment Tests (Outgoing Messages)
    // ========================================

    @Test
    fun `RIGHT alignment with hideAvatar=false and isGroupConversation=true returns false`() {
        val result = shouldShowAvatar(
            alignment = UIKitConstants.MessageBubbleAlignment.RIGHT,
            hideAvatar = false,
            isGroupConversation = true
        )

        assertFalse("Outgoing messages should never show avatar", result)
    }

    @Test
    fun `RIGHT alignment with hideAvatar=false and isGroupConversation=false returns false`() {
        val result = shouldShowAvatar(
            alignment = UIKitConstants.MessageBubbleAlignment.RIGHT,
            hideAvatar = false,
            isGroupConversation = false
        )

        assertFalse("Outgoing messages should never show avatar", result)
    }

    @Test
    fun `RIGHT alignment with hideAvatar=true and isGroupConversation=true returns false`() {
        val result = shouldShowAvatar(
            alignment = UIKitConstants.MessageBubbleAlignment.RIGHT,
            hideAvatar = true,
            isGroupConversation = true
        )

        assertFalse("Outgoing messages should never show avatar", result)
    }

    @Test
    fun `RIGHT alignment with hideAvatar=true and isGroupConversation=false returns false`() {
        val result = shouldShowAvatar(
            alignment = UIKitConstants.MessageBubbleAlignment.RIGHT,
            hideAvatar = true,
            isGroupConversation = false
        )

        assertFalse("Outgoing messages should never show avatar", result)
    }

    // ========================================
    // CENTER Alignment Tests (Action/System Messages)
    // ========================================

    @Test
    fun `CENTER alignment with hideAvatar=false and isGroupConversation=true returns false`() {
        val result = shouldShowAvatar(
            alignment = UIKitConstants.MessageBubbleAlignment.CENTER,
            hideAvatar = false,
            isGroupConversation = true
        )

        assertFalse("Action messages should never show avatar", result)
    }

    @Test
    fun `CENTER alignment with hideAvatar=false and isGroupConversation=false returns false`() {
        val result = shouldShowAvatar(
            alignment = UIKitConstants.MessageBubbleAlignment.CENTER,
            hideAvatar = false,
            isGroupConversation = false
        )

        assertFalse("Action messages should never show avatar", result)
    }

    @Test
    fun `CENTER alignment with hideAvatar=true and isGroupConversation=true returns false`() {
        val result = shouldShowAvatar(
            alignment = UIKitConstants.MessageBubbleAlignment.CENTER,
            hideAvatar = true,
            isGroupConversation = true
        )

        assertFalse("Action messages should never show avatar", result)
    }

    @Test
    fun `CENTER alignment with hideAvatar=true and isGroupConversation=false returns false`() {
        val result = shouldShowAvatar(
            alignment = UIKitConstants.MessageBubbleAlignment.CENTER,
            hideAvatar = true,
            isGroupConversation = false
        )

        assertFalse("Action messages should never show avatar", result)
    }

    // ========================================
    // LEFT Alignment Tests (Incoming Messages)
    // ========================================

    @Test
    fun `LEFT alignment with hideAvatar=false and isGroupConversation=true returns true`() {
        val result = shouldShowAvatar(
            alignment = UIKitConstants.MessageBubbleAlignment.LEFT,
            hideAvatar = false,
            isGroupConversation = true
        )

        assertTrue("Incoming messages in group conversations should show avatar", result)
    }

    @Test
    fun `LEFT alignment with hideAvatar=false and isGroupConversation=false returns false`() {
        val result = shouldShowAvatar(
            alignment = UIKitConstants.MessageBubbleAlignment.LEFT,
            hideAvatar = false,
            isGroupConversation = false
        )

        assertFalse("Incoming messages in user conversations should not show avatar", result)
    }

    @Test
    fun `LEFT alignment with hideAvatar=true and isGroupConversation=true returns false`() {
        val result = shouldShowAvatar(
            alignment = UIKitConstants.MessageBubbleAlignment.LEFT,
            hideAvatar = true,
            isGroupConversation = true
        )

        assertFalse("hideAvatar=true should override group conversation avatar visibility", result)
    }

    @Test
    fun `LEFT alignment with hideAvatar=true and isGroupConversation=false returns false`() {
        val result = shouldShowAvatar(
            alignment = UIKitConstants.MessageBubbleAlignment.LEFT,
            hideAvatar = true,
            isGroupConversation = false
        )

        assertFalse("hideAvatar=true should hide avatar in user conversations", result)
    }

    // ========================================
    // Rule Verification Tests
    // ========================================

    @Test
    fun `hideAvatar=true always returns false regardless of alignment and conversation type`() {
        // Test all alignments with hideAvatar=true
        val alignments = UIKitConstants.MessageBubbleAlignment.entries
        val conversationTypes = listOf(true, false)

        for (alignment in alignments) {
            for (isGroup in conversationTypes) {
                val result = shouldShowAvatar(
                    alignment = alignment,
                    hideAvatar = true,
                    isGroupConversation = isGroup
                )
                assertFalse(
                    "hideAvatar=true should always return false (alignment=$alignment, isGroup=$isGroup)",
                    result
                )
            }
        }
    }

    @Test
    fun `only LEFT alignment in group conversation with hideAvatar=false returns true`() {
        // Test all combinations
        val alignments = UIKitConstants.MessageBubbleAlignment.entries
        val hideAvatarValues = listOf(true, false)
        val conversationTypes = listOf(true, false)

        for (alignment in alignments) {
            for (hideAvatar in hideAvatarValues) {
                for (isGroup in conversationTypes) {
                    val result = shouldShowAvatar(
                        alignment = alignment,
                        hideAvatar = hideAvatar,
                        isGroupConversation = isGroup
                    )

                    val expectedTrue = alignment == UIKitConstants.MessageBubbleAlignment.LEFT &&
                            !hideAvatar &&
                            isGroup

                    assertEquals(
                        "Expected $expectedTrue for alignment=$alignment, hideAvatar=$hideAvatar, isGroup=$isGroup",
                        expectedTrue,
                        result
                    )
                }
            }
        }
    }

    // ========================================
    // Semantic Tests
    // ========================================

    @Test
    fun `outgoing messages never show avatar`() {
        // Outgoing messages are RIGHT aligned
        val result1 = shouldShowAvatar(UIKitConstants.MessageBubbleAlignment.RIGHT, false, true)
        val result2 = shouldShowAvatar(UIKitConstants.MessageBubbleAlignment.RIGHT, false, false)

        assertFalse("Outgoing message in group should not show avatar", result1)
        assertFalse("Outgoing message in user chat should not show avatar", result2)
    }

    @Test
    fun `action messages never show avatar`() {
        // Action messages are CENTER aligned
        val result1 = shouldShowAvatar(UIKitConstants.MessageBubbleAlignment.CENTER, false, true)
        val result2 = shouldShowAvatar(UIKitConstants.MessageBubbleAlignment.CENTER, false, false)

        assertFalse("Action message in group should not show avatar", result1)
        assertFalse("Action message in user chat should not show avatar", result2)
    }

    @Test
    fun `incoming messages show avatar only in group conversations`() {
        // Incoming messages are LEFT aligned
        val groupResult = shouldShowAvatar(UIKitConstants.MessageBubbleAlignment.LEFT, false, true)
        val userResult = shouldShowAvatar(UIKitConstants.MessageBubbleAlignment.LEFT, false, false)

        assertTrue("Incoming message in group should show avatar", groupResult)
        assertFalse("Incoming message in user chat should not show avatar", userResult)
    }

    @Test
    fun `hideAvatar flag takes precedence over all other rules`() {
        // Even in the only case where avatar would normally show (LEFT + group)
        val result = shouldShowAvatar(UIKitConstants.MessageBubbleAlignment.LEFT, true, true)

        assertFalse("hideAvatar=true should override group conversation avatar visibility", result)
    }
}
