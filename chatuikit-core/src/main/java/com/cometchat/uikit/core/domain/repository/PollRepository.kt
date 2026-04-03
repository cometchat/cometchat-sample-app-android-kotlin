package com.cometchat.uikit.core.domain.repository

import org.json.JSONArray

/**
 * Repository interface defining data operations contract for polls.
 * Lives in domain layer - no implementation details.
 *
 * This interface allows for custom implementations to be injected,
 * enabling flexibility in data handling strategies (remote, local, cached).
 */
interface PollRepository {

    /**
     * Creates a poll.
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
