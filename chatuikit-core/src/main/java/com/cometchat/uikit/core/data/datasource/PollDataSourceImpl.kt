package com.cometchat.uikit.core.data.datasource

import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONArray
import org.json.JSONObject
import kotlin.coroutines.resume

/**
 * Implementation of PollDataSource that communicates with CometChat SDK.
 * Contains NO business logic - just raw API calls.
 * This is the default implementation used for remote data operations.
 */
class PollDataSourceImpl : PollDataSource {

    companion object {
        private const val EXTENSION_POLLS = "polls"
        private const val EXTENSION_POST = "POST"
        private const val EXTENSION_CREATE_PATH = "/v2/create"
    }

    /**
     * Creates a poll via CometChat Extensions API.
     *
     * @param question The poll question
     * @param options The poll options as a JSONArray
     * @param receiverId The ID of the receiver (user UID or group GUID)
     * @param receiverType The type of receiver ("user" or "group")
     * @param quotedMessageId Optional ID of the message being replied to
     * @return Result containing success or error
     */
    override suspend fun createPoll(
        question: String,
        options: JSONArray,
        receiverId: String,
        receiverType: String,
        quotedMessageId: Long?
    ): Result<Unit> = suspendCancellableCoroutine { continuation ->
        try {
            val jsonObject = JSONObject().apply {
                put("question", question)
                put("options", options)
                put("receiver", receiverId)
                put("receiverType", receiverType)
                if (quotedMessageId != null && quotedMessageId > -1) {
                    put("quotedMessageId", quotedMessageId)
                }
            }

            CometChat.callExtension(
                EXTENSION_POLLS,
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
                        "ERR_POLL_CREATION",
                        e.message ?: "Failed to create poll",
                        EXTENSION_POLLS
                    )
                )
            )
        }
    }
}
