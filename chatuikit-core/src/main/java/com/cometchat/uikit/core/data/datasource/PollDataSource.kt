package com.cometchat.uikit.core.data.datasource

import org.json.JSONArray

/**
 * Interface defining data source operations for polls.
 * Lives in data layer - defines contract for poll creation.
 * Allows for different implementations (remote, local, mock).
 */
interface PollDataSource {

    /**
     * Creates a poll via the CometChat Extensions API.
     *
     * @param question The poll question
     * @param options The poll options as a JSONArray
     * @param receiverId The ID of the receiver (user UID or group GUID)
     * @param receiverType The type of receiver ("user" or "group")
     * @param quotedMessageId Optional ID of the message being replied to
     * @return Result containing success or error
     */
    suspend fun createPoll(
        question: String,
        options: JSONArray,
        receiverId: String,
        receiverType: String,
        quotedMessageId: Long? = null
    ): Result<Unit>
}
