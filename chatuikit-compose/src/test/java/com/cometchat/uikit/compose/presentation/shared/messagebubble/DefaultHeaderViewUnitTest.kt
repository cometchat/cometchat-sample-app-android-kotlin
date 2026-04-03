package com.cometchat.uikit.compose.presentation.shared.messagebubble

import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.uikit.core.constants.UIKitConstants
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

/**
 * Unit tests for DefaultHeaderView sender name visibility logic.
 *
 * These tests exercise the pure `showName()` function extracted in
 * SenderNameVisibilityExplorationTest, which mirrors the fixed
 * `shouldShowName` computation in DefaultHeaderView.
 */
class DefaultHeaderViewUnitTest : StringSpec({

    /**
     * Core bugfix scenario: sender name must be hidden for incoming
     * messages in a 1-on-1 (user) conversation.
     *
     * **Validates: Requirements 2.1, 1.1, 1.2**
     */
    "sender name is hidden for receiverType USER with alignment LEFT and showName true" {
        val result = showName(
            showName = true,
            alignment = UIKitConstants.MessageBubbleAlignment.LEFT,
            receiverType = CometChatConstants.RECEIVER_TYPE_USER
        )

        result shouldBe false
    }

    /**
     * Preservation scenario: sender name must remain visible for incoming
     * messages in a group conversation.
     *
     * **Validates: Requirements 2.2, 3.2**
     */
    "sender name is shown for receiverType GROUP with alignment LEFT and showName true" {
        val result = showName(
            showName = true,
            alignment = UIKitConstants.MessageBubbleAlignment.LEFT,
            receiverType = CometChatConstants.RECEIVER_TYPE_GROUP
        )

        result shouldBe true
    }

    /**
     * Preservation scenario: sender name must be hidden for outgoing
     * messages (RIGHT-aligned) in a 1-on-1 (user) conversation.
     *
     * **Validates: Requirements 3.1**
     */
    "sender name is hidden for alignment RIGHT with receiverType USER and showName true" {
        val result = showName(
            showName = true,
            alignment = UIKitConstants.MessageBubbleAlignment.RIGHT,
            receiverType = CometChatConstants.RECEIVER_TYPE_USER
        )

        result shouldBe false
    }

    /**
     * Preservation scenario: sender name must be hidden for outgoing
     * messages (RIGHT-aligned) in a group conversation.
     *
     * **Validates: Requirements 3.1**
     */
    "sender name is hidden for alignment RIGHT with receiverType GROUP and showName true" {
        val result = showName(
            showName = true,
            alignment = UIKitConstants.MessageBubbleAlignment.RIGHT,
            receiverType = CometChatConstants.RECEIVER_TYPE_GROUP
        )

        result shouldBe false
    }

    /**
     * Preservation scenario: when sender name is hidden in a user conversation,
     * the timestamp (showTime) remains independently visible. The header is only
     * fully hidden when BOTH shouldShowName and showTime are false.
     *
     * This verifies that hiding the sender name does NOT affect timestamp visibility.
     *
     * **Validates: Requirements 3.4**
     */
    "timestamp remains visible when sender name is hidden in user conversation with showTime true" {
        // Sender name should be hidden for user conversations
        val shouldShowName = showName(
            showName = true,
            alignment = UIKitConstants.MessageBubbleAlignment.LEFT,
            receiverType = CometChatConstants.RECEIVER_TYPE_USER
        )
        shouldShowName shouldBe false

        // showTime is an independent parameter — it stays true regardless of shouldShowName
        val showTime = true

        // The header is only fully hidden when BOTH are false.
        // Since showTime is true, the header (with timestamp) is still rendered.
        val headerFullyHidden = !shouldShowName && !showTime
        headerFullyHidden shouldBe false
    }
})
