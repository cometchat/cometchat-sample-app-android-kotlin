package com.cometchat.uikit.kotlin.presentation.messagelist

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.long
import io.kotest.property.checkAll

/**
 * Represents a simplified message for testing dismiss-on-delete logic.
 */
private data class TestMessage(
    val id: Long,
    val deletedAt: Long
)

/**
 * Pure-logic function that mirrors the dismiss-on-delete check in
 * CometChatMessageList.handleMessagesUpdate():
 *
 * ```kotlin
 * val popupMessage = cometchatPopUpMenuMessage?.getCurrentMessage()
 * if (popupMessage != null) {
 *     val updatedMessage = messages.find { it.id == popupMessage.id }
 *     if (updatedMessage != null && updatedMessage.deletedAt > 0) {
 *         cometchatPopUpMenuMessage?.dismiss()
 *         clearCurrentLongPressedMessage()
 *     }
 * }
 * ```
 *
 * @param popupMessageId The ID of the message currently shown in the popup (null if no popup)
 * @param messageIds The list of message IDs from the updated messages
 * @param deletedAts The list of deletedAt values corresponding to each message
 * @return true if the popup should be dismissed
 */
private fun shouldDismissPopup(
    popupMessageId: Long?,
    messageIds: List<Long>,
    deletedAts: List<Long>
): Boolean {
    if (popupMessageId == null) return false
    val index = messageIds.indexOf(popupMessageId)
    if (index == -1) return false
    return deletedAts[index] > 0
}

/**
 * Property-based tests for popup dismiss on message deletion.
 *
 * Feature: message-popup-menu, Property 11: Popup dismisses when displayed message is deleted
 *
 * *For any* message currently displayed in the popup menu, if that message's deletedAt
 * becomes greater than 0 (indicating deletion), the popup should be dismissed.
 *
 * **Validates: Requirements 6.9**
 */
class DismissOnDeletePropertyTest : FunSpec({

    // ==================== Generators ====================

    val messageIdArb = Arb.long(1L..100000L)
    val deletedAtArb = Arb.long(0L..999999L)
    val positiveDeletedAtArb = Arb.long(1L..999999L)

    // ==================== Property Tests ====================

    context("Property 11: Popup dismisses when displayed message is deleted") {

        test("popup dismisses when displayed message has deletedAt > 0") {
            checkAll(100, messageIdArb, positiveDeletedAtArb) { msgId, deletedAt ->
                val result = shouldDismissPopup(
                    popupMessageId = msgId,
                    messageIds = listOf(msgId),
                    deletedAts = listOf(deletedAt)
                )
                result shouldBe true
            }
        }

        test("popup does not dismiss when displayed message has deletedAt == 0") {
            checkAll(100, messageIdArb) { msgId ->
                val result = shouldDismissPopup(
                    popupMessageId = msgId,
                    messageIds = listOf(msgId),
                    deletedAts = listOf(0L)
                )
                result shouldBe false
            }
        }

        test("popup does not dismiss when no popup is showing (null popupMessageId)") {
            checkAll(100, messageIdArb, deletedAtArb) { msgId, deletedAt ->
                val result = shouldDismissPopup(
                    popupMessageId = null,
                    messageIds = listOf(msgId),
                    deletedAts = listOf(deletedAt)
                )
                result shouldBe false
            }
        }

        test("popup does not dismiss when displayed message is not in the updated list") {
            checkAll(100, messageIdArb, messageIdArb) { popupId, otherId ->
                val actualOtherId = if (otherId == popupId) popupId + 1 else otherId
                val result = shouldDismissPopup(
                    popupMessageId = popupId,
                    messageIds = listOf(actualOtherId),
                    deletedAts = listOf(999L)
                )
                result shouldBe false
            }
        }

        test("dismiss result matches the boolean expression from design doc") {
            checkAll(100, messageIdArb, deletedAtArb, Arb.boolean()) { msgId, deletedAt, messageInList ->
                val ids = if (messageInList) listOf(msgId) else listOf(msgId + 1)
                val deletedAts = listOf(deletedAt)

                val result = shouldDismissPopup(
                    popupMessageId = msgId,
                    messageIds = ids,
                    deletedAts = deletedAts
                )
                val expected = messageInList && deletedAt > 0
                result shouldBe expected
            }
        }
    }
})
