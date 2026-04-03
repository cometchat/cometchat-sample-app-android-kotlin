package com.cometchat.uikit.core.data.datasource

import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONObject
import kotlin.coroutines.resume

/**
 * Implementation of CollaborativeDataSource that communicates with CometChat SDK.
 * Contains NO business logic - just raw API calls.
 * This is the default implementation used for remote data operations.
 */
class CollaborativeDataSourceImpl : CollaborativeDataSource {

    companion object {
        private const val EXTENSION_WHITEBOARD = "whiteboard"
        private const val EXTENSION_DOCUMENT = "document"
        private const val EXTENSION_POST = "POST"
        private const val EXTENSION_CREATE_PATH = "/v1/create"
    }

    /**
     * Creates a collaborative whiteboard via CometChat Extensions API.
     *
     * @param receiverId The ID of the receiver (user UID or group GUID)
     * @param receiverType The type of receiver ("user" or "group")
     * @param quotedMessageId Optional ID of the message being replied to
     * @return Result containing success or error
     */
    override suspend fun createWhiteboard(
        receiverId: String,
        receiverType: String,
        quotedMessageId: Long?
    ): Result<Unit> = suspendCancellableCoroutine { continuation ->
        try {
            val jsonObject = JSONObject().apply {
                put("receiver", receiverId)
                put("receiverType", receiverType)
                if (quotedMessageId != null && quotedMessageId > -1) {
                    put("quotedMessageId", quotedMessageId)
                }
            }

            CometChat.callExtension(
                EXTENSION_WHITEBOARD,
                EXTENSION_POST,
                EXTENSION_CREATE_PATH,
                jsonObject,
                object : CometChat.CallbackListener<JSONObject>() {
                    override fun onSuccess(response: JSONObject?) {
                        continuation.resume(Result.success(Unit))
                    }

                    override fun onError(exception: CometChatException) {
                        continuation.resume(Result.failure(exception))
                    }
                }
            )
        } catch (e: Exception) {
            continuation.resume(
                Result.failure(
                    CometChatException(
                        "ERR_WHITEBOARD_CREATION",
                        e.message ?: "Failed to create whiteboard",
                        EXTENSION_WHITEBOARD
                    )
                )
            )
        }
    }

    /**
     * Creates a collaborative document via CometChat Extensions API.
     *
     * @param receiverId The ID of the receiver (user UID or group GUID)
     * @param receiverType The type of receiver ("user" or "group")
     * @param quotedMessageId Optional ID of the message being replied to
     * @return Result containing success or error
     */
    override suspend fun createDocument(
        receiverId: String,
        receiverType: String,
        quotedMessageId: Long?
    ): Result<Unit> = suspendCancellableCoroutine { continuation ->
        try {
            val jsonObject = JSONObject().apply {
                put("receiver", receiverId)
                put("receiverType", receiverType)
                if (quotedMessageId != null && quotedMessageId > -1) {
                    put("quotedMessageId", quotedMessageId)
                }
            }

            CometChat.callExtension(
                EXTENSION_DOCUMENT,
                EXTENSION_POST,
                EXTENSION_CREATE_PATH,
                jsonObject,
                object : CometChat.CallbackListener<JSONObject>() {
                    override fun onSuccess(response: JSONObject?) {
                        continuation.resume(Result.success(Unit))
                    }

                    override fun onError(exception: CometChatException) {
                        continuation.resume(Result.failure(exception))
                    }
                }
            )
        } catch (e: Exception) {
            continuation.resume(
                Result.failure(
                    CometChatException(
                        "ERR_DOCUMENT_CREATION",
                        e.message ?: "Failed to create document",
                        EXTENSION_DOCUMENT
                    )
                )
            )
        }
    }
}
