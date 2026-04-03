package com.cometchat.uikit.core.data.repository

import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.MessageReceipt
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.CometChatUIKit
import com.cometchat.uikit.core.data.datasource.MessageInformationDataSource
import com.cometchat.uikit.core.domain.repository.MessageInformationRepository

/**
 * Implementation of MessageInformationRepository.
 * Handles fetching message receipts and creating receipts from messages.
 *
 * @param dataSource The data source for fetching receipts from SDK
 */
class MessageInformationRepositoryImpl(
    private val dataSource: MessageInformationDataSource
) : MessageInformationRepository {

    /**
     * Fetches message receipts for a specific message.
     * @param messageId The ID of the message to fetch receipts for
     * @return Result containing list of MessageReceipt objects or error
     */
    override suspend fun fetchReceipts(messageId: Long): Result<List<MessageReceipt>> {
        return dataSource.getMessageReceipts(messageId)
    }

    /**
     * Creates a MessageReceipt from a BaseMessage for USER conversations.
     * Per design doc: createMessageReceipt() utility function.
     * 
     * @param message The message to create a receipt from
     * @return MessageReceipt created from the message data
     */
    override fun createReceiptFromMessage(message: BaseMessage): MessageReceipt {
        return MessageReceipt().apply {
            sender = if (message.receiver is User) message.receiver as User else null
            readAt = message.readAt
            timestamp = message.readAt
            deliveredAt = if (message.deliveredAt == 0L) message.readAt else message.deliveredAt
            messageId = message.id
            receiverType = message.receiverType
            receiverId = CometChatUIKit.getLoggedInUser()?.uid ?: ""
            messageSender = message.sender?.toString() ?: ""
        }
    }
}
