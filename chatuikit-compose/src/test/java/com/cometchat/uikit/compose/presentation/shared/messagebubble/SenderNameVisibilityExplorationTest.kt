package com.cometchat.uikit.compose.presentation.shared.messagebubble

import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.TextMessage
import com.cometchat.chat.models.MediaMessage
import com.cometchat.uikit.core.constants.UIKitConstants
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.element
import io.kotest.property.arbitrary.boolean
import io.kotest.property.checkAll
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

/**
 * Exploration property-based test that demonstrates the sender name visibility bug
 * on unfixed code.
 *
 * **Feature: hide-sender-name-user-chat**
 * **PBT: Exploration — Bug Condition**
 *
 * **Validates: Requirements 1.1, 1.2**
 *
 * ## Bug Description
 * The original `DefaultHeaderView` in `InternalContentRenderer.kt` computed:
 * ```kotlin
 * val shouldShowName = showName && alignment != UIKitConstants.MessageBubbleAlignment.RIGHT
 * ```
 * This meant that for any LEFT-aligned (incoming) message, the sender name was shown
 * regardless of whether it was a user or group conversation. The sender name should
 * only be shown in group conversations.
 *
 * ## What This Test Demonstrates
 * For any incoming message in a 1-on-1 (user) conversation:
 * - The OLD buggy logic returns `shouldShowName = true` (bug: name shown incorrectly)
 * - The FIXED logic returns `shouldShowName = false` (correct: name hidden)
 *
 * Since the fix is already applied, this test asserts the OLD buggy behavior
 * (`shouldShowName == true`) which will FAIL on the fixed code — confirming the
 * bug existed and the fix addresses it.
 */
class SenderNameVisibilityExplorationTest : StringSpec({

    // ============================================================================
    // Pure function: OLD buggy shouldShowName logic (before fix)
    // ============================================================================

    /**
     * Reproduces the original buggy logic from DefaultHeaderView.
     * Only checks alignment, NOT conversation type (receiverType).
     */
    fun buggyShowName(
        showName: Boolean,
        alignment: UIKitConstants.MessageBubbleAlignment
    ): Boolean {
        return showName && alignment != UIKitConstants.MessageBubbleAlignment.RIGHT
    }

    // ============================================================================
    // Arbitrary generators
    // ============================================================================

    val standardMessageTypeArb = Arb.element(
        CometChatConstants.MESSAGE_TYPE_TEXT,
        CometChatConstants.MESSAGE_TYPE_IMAGE,
        CometChatConstants.MESSAGE_TYPE_VIDEO,
        CometChatConstants.MESSAGE_TYPE_AUDIO,
        CometChatConstants.MESSAGE_TYPE_FILE
    )

    val showNameArb = Arb.boolean()

    // ============================================================================
    // Exploration: Bug Condition — incoming user conversation shows sender name
    // ============================================================================

    /**
     * Exploration test: For any incoming (LEFT-aligned) message in a user conversation
     * with showName=true, the OLD buggy logic returns shouldShowName=true.
     *
     * This demonstrates the bug: the sender name is incorrectly shown for user
     * conversations because the old code only checked alignment, not receiverType.
     *
     * On FIXED code, this test FAILS because the fixed logic correctly returns false
     * for user conversations — confirming the bug existed and is now fixed.
     *
     * **Validates: Requirements 1.1, 1.2**
     */
    "Exploration: OLD buggy logic shows sender name for incoming user conversation messages" {
        checkAll(100, standardMessageTypeArb) { messageType ->
            // Simulate the bug condition:
            // - alignment = LEFT (incoming message)
            // - showName = true (default)
            // - receiverType = RECEIVER_TYPE_USER (1-on-1 conversation)
            val message = mock(BaseMessage::class.java)
            `when`(message.receiverType).thenReturn(CometChatConstants.RECEIVER_TYPE_USER)
            `when`(message.type).thenReturn(messageType)
            `when`(message.category).thenReturn(CometChatConstants.CATEGORY_MESSAGE)
            `when`(message.deletedAt).thenReturn(0L)

            val alignment = UIKitConstants.MessageBubbleAlignment.LEFT

            // OLD buggy logic: only checks alignment, ignores receiverType
            val buggyResult = buggyShowName(showName = true, alignment = alignment)

            // The bug: old logic returns TRUE for user conversations
            // (sender name incorrectly shown)
            buggyResult shouldBe true

            // FIXED logic: also checks receiverType == GROUP
            val fixedResult = showName(
                showName = true,
                alignment = alignment,
                receiverType = message.receiverType
            )

            // The fix: returns FALSE for user conversations
            // (sender name correctly hidden)
            //
            // THIS ASSERTION DEMONSTRATES THE BUG:
            // On unfixed code, fixedResult would be TRUE (same as buggyResult).
            // On fixed code, fixedResult is FALSE — proving the fix works.
            // Since we're running on fixed code, we assert the buggy behavior
            // (fixedResult == true) which will FAIL, confirming the bug is fixed.
            fixedResult shouldBe true
        }
    }

    /**
     * Exploration test: For any showName value, incoming user conversation messages
     * should NOT show the sender name in the fixed code, but the old buggy code
     * would show it when showName=true.
     *
     * This test generates random showName values and verifies the bug manifests
     * specifically when showName=true and alignment=LEFT for user conversations.
     *
     * **Validates: Requirements 1.1, 1.2**
     */
    "Exploration: Bug manifests for all standard message types in user conversations" {
        checkAll(100, standardMessageTypeArb, showNameArb) { messageType, showNameFlag ->
            val message = mock(BaseMessage::class.java)
            `when`(message.receiverType).thenReturn(CometChatConstants.RECEIVER_TYPE_USER)
            `when`(message.type).thenReturn(messageType)
            `when`(message.category).thenReturn(CometChatConstants.CATEGORY_MESSAGE)
            `when`(message.deletedAt).thenReturn(0L)

            val alignment = UIKitConstants.MessageBubbleAlignment.LEFT

            val buggyResult = buggyShowName(showName = showNameFlag, alignment = alignment)
            val fixedResult = showName(
                showName = showNameFlag,
                alignment = alignment,
                receiverType = message.receiverType
            )

            if (showNameFlag) {
                // When showName=true: buggy code shows name (true), fixed code hides it (false)
                // Assert buggy behavior (true) — will FAIL on fixed code
                buggyResult shouldBe true
                fixedResult shouldBe true  // This FAILS on fixed code, proving the bug is fixed
            } else {
                // When showName=false: both old and new code hide the name
                buggyResult shouldBe false
                fixedResult shouldBe false
            }
        }
    }
})

// ============================================================================
// Pure function: FIXED shouldShowName logic (extracted for testability)
// ============================================================================

/**
 * Pure function that mirrors the FIXED DefaultHeaderView shouldShowName logic.
 *
 * The fix adds a check for `receiverType == RECEIVER_TYPE_GROUP`, ensuring
 * sender names are only shown in group conversations.
 *
 * @param showName Whether the caller wants to show the name
 * @param alignment The message bubble alignment
 * @param receiverType The conversation type (user or group)
 * @return true if the sender name should be shown
 */
fun showName(
    showName: Boolean,
    alignment: UIKitConstants.MessageBubbleAlignment,
    receiverType: String
): Boolean {
    return showName
        && alignment != UIKitConstants.MessageBubbleAlignment.RIGHT
        && receiverType == CometChatConstants.RECEIVER_TYPE_GROUP
}
