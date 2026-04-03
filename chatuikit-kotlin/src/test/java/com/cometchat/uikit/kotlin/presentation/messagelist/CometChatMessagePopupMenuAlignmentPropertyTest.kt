package com.cometchat.uikit.kotlin.presentation.messagelist

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.element
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll

/**
 * Simulates UIKitConstants.MessageListAlignment for pure-logic testing.
 */
private enum class MessageListAlignment {
    STANDARD,
    LEFT_ALIGNED
}

/**
 * Pure-logic function that mirrors the alignment determination in
 * CometChatMessagePopupMenu.determineIsLeftAligned():
 *
 * ```kotlin
 * val isLeft = baseMessage.sender.uid != loggedInUser.uid
 *     || messageAlignment == MessageListAlignment.LEFT_ALIGNED
 * ```
 *
 * Returns true when content should be aligned to start (left).
 */
private fun computeIsLeftAligned(
    senderUid: String,
    loggedInUserUid: String,
    messageAlignment: MessageListAlignment
): Boolean {
    return senderUid != loggedInUserUid
        || messageAlignment == MessageListAlignment.LEFT_ALIGNED
}

/**
 * Property-based tests for popup menu alignment determination.
 *
 * Feature: message-popup-menu, Property 1: Popup alignment is determined by sender and alignment setting
 *
 * *For any* BaseMessage and MessageListAlignment setting, the popup menu content
 * (reactions bar and option list card) should be aligned to start (left) when the
 * message sender is not the logged-in user OR when messageAlignment == LEFT_ALIGNED,
 * and aligned to end (right) otherwise.
 *
 * **Validates: Requirements 1.4, 1.5**
 */
class CometChatMessagePopupMenuAlignmentPropertyTest : FunSpec({

    // ==================== Generators ====================

    val userIdArb = Arb.string(1..20)
    val alignmentArb = Arb.element(MessageListAlignment.STANDARD, MessageListAlignment.LEFT_ALIGNED)

    // ==================== Property Tests ====================

    context("Property 1: Popup alignment is determined by sender and alignment setting") {

        test("incoming messages (sender != loggedInUser) are always left-aligned") {
            checkAll(100, userIdArb, userIdArb, alignmentArb) { senderUid, loggedInUid, alignment ->
                // Ensure sender is different from logged-in user
                val effectiveSender = if (senderUid == loggedInUid) "${senderUid}_other" else senderUid

                val isLeft = computeIsLeftAligned(effectiveSender, loggedInUid, alignment)

                isLeft shouldBe true
            }
        }

        test("outgoing messages with LEFT_ALIGNED are left-aligned") {
            checkAll(100, userIdArb) { uid ->
                val isLeft = computeIsLeftAligned(uid, uid, MessageListAlignment.LEFT_ALIGNED)

                isLeft shouldBe true
            }
        }

        test("outgoing messages with STANDARD are right-aligned") {
            checkAll(100, userIdArb) { uid ->
                val isLeft = computeIsLeftAligned(uid, uid, MessageListAlignment.STANDARD)

                isLeft shouldBe false
            }
        }

        test("LEFT_ALIGNED mode forces left alignment for all messages") {
            checkAll(100, userIdArb, userIdArb) { senderUid, loggedInUid ->
                val isLeft = computeIsLeftAligned(senderUid, loggedInUid, MessageListAlignment.LEFT_ALIGNED)

                isLeft shouldBe true
            }
        }

        test("alignment result matches the boolean expression from design doc") {
            checkAll(100, userIdArb, userIdArb, alignmentArb) { senderUid, loggedInUid, alignment ->
                val isLeft = computeIsLeftAligned(senderUid, loggedInUid, alignment)

                val expected = senderUid != loggedInUid
                    || alignment == MessageListAlignment.LEFT_ALIGNED

                isLeft shouldBe expected
            }
        }
    }
})
