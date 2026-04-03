package com.cometchat.uikit.core.domain.repository

import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.MessageReceipt

/**
 * Repository interface defining data operations contract for message information.
 * Lives in domain layer - no implementation details.
 *
 * This interface allows for custom implementations to be injected,
 * enabling flexibility in data fetching strategies (remote, local, cached).
 */
interface MessageInformationRepository {

    /**
     * Fetches message receipts for a specific message.
     * @param messageId The ID of the message to fetch receipts for
     * @return Result containing list of MessageReceipt objects or error
     */
    suspend fun fetchReceipts(messageId: Long): Result<List<MessageReceipt>>

    /**
     * Creates a MessageReceipt from a BaseMessage for USER conversations.
     * This is used when displaying receipt information for one-to-one chats
     * where the receipt data is derived from the message itself.
     * 
     * @param message The message to create a receipt from
     * @return MessageReceipt created from the message data
     */
    fun createReceiptFromMessage(message: BaseMessage): MessageReceipt
}
