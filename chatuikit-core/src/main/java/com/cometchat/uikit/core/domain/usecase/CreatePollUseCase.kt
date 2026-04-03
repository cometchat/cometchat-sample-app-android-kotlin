package com.cometchat.uikit.core.domain.usecase

import com.cometchat.uikit.core.domain.repository.PollRepository
import org.json.JSONArray

/**
 * Use case for creating a poll via the CometChat Extensions API.
 * Contains business logic for poll creation.
 *
 * This use case is used by the CometChatCreatePollViewModel to create
 * polls through the CometChat Extensions API.
 *
 * @param repository The repository to use for poll operations
 */
open class CreatePollUseCase(
    private val repository: PollRepository
) {
    /**
     * Creates a poll with the given parameters.
     *
     * @param question The poll question
     * @param options The poll options as a JSONArray
     * @param receiverId The ID of the receiver (user UID or group GUID)
     * @param receiverType The type of receiver ("user" or "group")
     * @param quotedMessageId Optional ID of the message being replied to
     * @return Result containing success or error
     */
    open suspend operator fun invoke(
        question: String,
        options: JSONArray,
        receiverId: String,
        receiverType: String,
        quotedMessageId: Long? = null
    ): Result<Unit> {
        return repository.createPoll(question, options, receiverId, receiverType, quotedMessageId)
    }
}
