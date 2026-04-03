package com.cometchat.uikit.compose.presentation.shared.receipts

import com.cometchat.chat.enums.ModerationStatus
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.MediaMessage
import com.cometchat.chat.models.TextMessage
import com.cometchat.uikit.core.CometChatUIKit
import org.json.JSONObject

/**
 * Utility object for determining message receipt status.
 * 
 * This mirrors the View-based MessageReceiptUtils.java implementation
 * for use in Jetpack Compose components.
 */
object MessageReceiptUtils {

    /**
     * Determines the receipt status for a given message.
     * 
     * Checks for error metadata first, then delegates to [getReceipt]
     * for standard receipt status determination.
     *
     * @param baseMessage The message to get receipt status for, can be null
     * @return The appropriate [Receipt] status for the message
     */
    fun getMessageReceipt(baseMessage: BaseMessage?): Receipt {
        if (baseMessage == null) {
            return Receipt.ERROR
        }

        val metadata: JSONObject? = baseMessage.metadata
        if (metadata != null) {
            try {
                val exception = metadata.getString("error")
                if (exception.isNotEmpty()) {
                    return Receipt.ERROR
                }
            } catch (e: Exception) {
                // No error in metadata, continue to normal receipt check
            }
        }

        return getReceipt(baseMessage)
    }

    /**
     * Determines the receipt status based on message properties.
     * 
     * Priority order:
     * 1. DISAPPROVED moderation status → ERROR
     * 2. Message ID is 0 → IN_PROGRESS
     * 3. readAt > 0 → READ
     * 4. deliveredAt > 0 → DELIVERED
     * 5. sentAt > 0 or PENDING moderation → SENT
     * 6. Otherwise → IN_PROGRESS
     *
     * @param baseMessage The message to check
     * @return The appropriate [Receipt] status
     */
    private fun getReceipt(baseMessage: BaseMessage): Receipt {
        val moderationStatus = getModerationStatus(baseMessage)

        return when {
            moderationStatus == ModerationStatus.DISAPPROVED -> Receipt.ERROR
            baseMessage.id == 0L -> Receipt.IN_PROGRESS
            baseMessage.readAt != 0L -> Receipt.READ
            baseMessage.deliveredAt != 0L -> Receipt.DELIVERED
            baseMessage.sentAt > 0 || moderationStatus == ModerationStatus.PENDING -> Receipt.SENT
            else -> Receipt.IN_PROGRESS
        }
    }

    /**
     * Determines whether the receipt indicator should be hidden for a message.
     * 
     * Receipts should be hidden when:
     * - The message is null
     * - The message is deleted
     * - No logged-in user exists
     * - The message was not sent by the logged-in user
     *
     * @param baseMessage The message to check
     * @return true if the receipt should be hidden, false otherwise
     */
    fun shouldHideReceipt(baseMessage: BaseMessage?): Boolean {
        if (baseMessage == null || baseMessage.deletedAt != 0L) {
            return true
        }

        val loggedInUser = try { CometChatUIKit.getLoggedInUser() } catch (e: Exception) { null } ?: return true
        val sender = baseMessage.sender ?: return true

        val category = baseMessage.category
        val isMessageOrInteractive = category == MESSAGE_CATEGORY || category == INTERACTIVE_CATEGORY

        if (isMessageOrInteractive) {
            return sender.uid != loggedInUser.uid
        }

        // Check for incrementUnreadCount in metadata
        val metadata = baseMessage.metadata
        if (metadata != null && metadata.has(INCREMENT_UNREAD_COUNT_KEY)) {
            return sender.uid != loggedInUser.uid
        }

        return true
    }

    /**
     * Gets the moderation status for a message.
     * 
     * Only TextMessage and MediaMessage have moderation status.
     * Other message types return APPROVED by default.
     * If moderation status is null (e.g., for mock messages), returns APPROVED.
     *
     * @param baseMessage The message to check
     * @return The moderation status of the message, or APPROVED if null
     */
    private fun getModerationStatus(baseMessage: BaseMessage): ModerationStatus {
        return when (baseMessage) {
            is TextMessage -> baseMessage.moderationStatus ?: ModerationStatus.APPROVED
            is MediaMessage -> baseMessage.moderationStatus ?: ModerationStatus.APPROVED
            else -> ModerationStatus.APPROVED
        }
    }

    // Constants matching UIKitConstants.MessageCategory
    private const val MESSAGE_CATEGORY = "message"
    private const val INTERACTIVE_CATEGORY = "interactive"
    private const val INCREMENT_UNREAD_COUNT_KEY = "incrementUnreadCount"
}
