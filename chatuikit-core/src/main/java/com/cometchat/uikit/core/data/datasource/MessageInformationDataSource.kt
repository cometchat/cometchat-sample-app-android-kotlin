package com.cometchat.uikit.core.data.datasource

import com.cometchat.chat.models.MessageReceipt

/**
 * Interface defining data source operations for message information.
 * Lives in data layer - defines contract for data fetching.
 * Allows for different implementations (remote, local, mock).
 */
interface MessageInformationDataSource {

    /**
     * Fetches message receipts for a specific message.
     * @param messageId The ID of the message to fetch receipts for
     * @return Result containing list of MessageReceipt objects or error
     */
    suspend fun getMessageReceipts(messageId: Long): Result<List<MessageReceipt>>
}
