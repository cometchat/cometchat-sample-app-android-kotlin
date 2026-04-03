package com.cometchat.uikit.compose.presentation.shared.messagebubble

import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.models.BaseMessage
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
 * Preservation property-based test that verifies existing behavior is maintained
 * after the sender name visibility fix.
 *
 * **Feature: hide-sender-name-user-chat**
 * **PBT: Preservation — Correctness Properties 2 & 3**
 *
 * ## What This Test Verifies
 *
 * Property 2: For any incoming message (LEFT-aligned) in a group conversation,
 * the fixed code SHALL continue to display the sender name.
 * **Validates: Requirements 2.2, 3.2**
 *
 * Property 3: For any outgoing message (RIGHT-aligned) in any conversation type,
 * the fixed code SHALL continue to hide the sender name.
 * **Validates: Requirements 3.1**
 *
 * This test PASSES on the fixed code, confirming preservation of existing behavior.
 */
class SenderNameVisibilityPreservationTest : StringSpec({

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

    val receiverTypeArb = Arb.element(
        CometChatConstants.RECEIVER_TYPE_USER,
        CometChatConstants.RECEIVER_TYPE_GROUP
    )

    // ============================================================================
    // Property 2: Sender name shown for incoming group conversation messages
    // ============================================================================

    /**
     * Property 2: For any incoming (LEFT-aligned) message in a group conversation
     * with showName=true, the fixed showName() function returns true — preserving
     * the existing group conversation sender name display.
     *
     * **Validates: Requirements 2.2, 3.2**
     */
    "Preservation: sender name is shown for all incoming group conversation messages" {
        checkAll(200, standardMessageTypeArb) { messageType ->
            val message = mock(BaseMessage::class.java)
            `when`(message.receiverType).thenReturn(CometChatConstants.RECEIVER_TYPE_GROUP)
            `when`(message.type).thenReturn(messageType)
            `when`(message.category).thenReturn(CometChatConstants.CATEGORY_MESSAGE)

            val result = showName(
                showName = true,
                alignment = UIKitConstants.MessageBubbleAlignment.LEFT,
                receiverType = message.receiverType
            )

            result shouldBe true
        }
    }

    /**
     * Property 2 (extended): Even with random showName flag values, the result
     * matches the showName flag for LEFT-aligned group messages — when showName
     * is true the name is shown, when false it is hidden.
     *
     * **Validates: Requirements 2.2, 3.2**
     */
    "Preservation: sender name visibility respects showName flag for group conversations" {
        checkAll(200, standardMessageTypeArb, Arb.boolean()) { messageType, showNameFlag ->
            val message = mock(BaseMessage::class.java)
            `when`(message.receiverType).thenReturn(CometChatConstants.RECEIVER_TYPE_GROUP)
            `when`(message.type).thenReturn(messageType)

            val result = showName(
                showName = showNameFlag,
                alignment = UIKitConstants.MessageBubbleAlignment.LEFT,
                receiverType = message.receiverType
            )

            // For LEFT-aligned group messages, result follows the showName flag
            result shouldBe showNameFlag
        }
    }

    // ============================================================================
    // Property 3: Sender name hidden for outgoing (RIGHT-aligned) messages
    // ============================================================================

    /**
     * Property 3: For any outgoing (RIGHT-aligned) message in any conversation type,
     * the fixed showName() function returns false — preserving the existing outgoing
     * message behavior where sender name is always hidden.
     *
     * **Validates: Requirements 3.1**
     */
    "Preservation: sender name is hidden for all outgoing messages regardless of conversation type" {
        checkAll(200, standardMessageTypeArb, receiverTypeArb) { messageType, receiverType ->
            val message = mock(BaseMessage::class.java)
            `when`(message.receiverType).thenReturn(receiverType)
            `when`(message.type).thenReturn(messageType)
            `when`(message.category).thenReturn(CometChatConstants.CATEGORY_MESSAGE)

            val result = showName(
                showName = true,
                alignment = UIKitConstants.MessageBubbleAlignment.RIGHT,
                receiverType = message.receiverType
            )

            result shouldBe false
        }
    }

    /**
     * Property 3 (extended): Even with random showName flag values, RIGHT-aligned
     * messages always hide the sender name regardless of conversation type.
     *
     * **Validates: Requirements 3.1**
     */
    "Preservation: outgoing messages hide sender name regardless of showName flag and conversation type" {
        checkAll(200, standardMessageTypeArb, receiverTypeArb, Arb.boolean()) { messageType, receiverType, showNameFlag ->
            val message = mock(BaseMessage::class.java)
            `when`(message.receiverType).thenReturn(receiverType)
            `when`(message.type).thenReturn(messageType)

            val result = showName(
                showName = showNameFlag,
                alignment = UIKitConstants.MessageBubbleAlignment.RIGHT,
                receiverType = message.receiverType
            )

            // RIGHT-aligned messages NEVER show sender name
            result shouldBe false
        }
    }
})
