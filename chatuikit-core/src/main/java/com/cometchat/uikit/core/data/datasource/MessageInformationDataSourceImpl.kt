package com.cometchat.uikit.core.data.datasource

import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.MessageReceipt
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Implementation of MessageInformationDataSource that wraps CometChat SDK calls.
 * Handles fetching message receipts from the CometChat server.
 */
class MessageInformationDataSourceImpl : MessageInformationDataSource {

    /**
     * Fetches message receipts for a specific message from CometChat SDK.
     * @param messageId The ID of the message to fetch receipts for
     * @return Result containing list of MessageReceipt objects or error
     */
    override suspend fun getMessageReceipts(messageId: Long): Result<List<MessageReceipt>> {
        return suspendCoroutine { continuation ->
            CometChat.getMessageReceipts(
                messageId,
                object : CometChat.CallbackListener<List<MessageReceipt>>() {
                    override fun onSuccess(receipts: List<MessageReceipt>) {
                        continuation.resume(Result.success(receipts))
                    }

                    override fun onError(exception: CometChatException) {
                        continuation.resume(Result.failure(exception))
                    }
                }
            )
        }
    }
}
