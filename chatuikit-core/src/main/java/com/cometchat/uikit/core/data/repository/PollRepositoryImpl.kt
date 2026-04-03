package com.cometchat.uikit.core.data.repository

import com.cometchat.uikit.core.data.datasource.PollDataSource
import com.cometchat.uikit.core.domain.repository.PollRepository
import org.json.JSONArray

/**
 * Implementation of PollRepository that delegates to a data source.
 * This is the default implementation used for data operations.
 *
 * @param dataSource The data source to use for poll operations
 */
class PollRepositoryImpl(
    private val dataSource: PollDataSource
) : PollRepository {

    /**
     * Creates a poll by delegating to the data source.
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
    ): Result<Unit> {
        return dataSource.createPoll(question, options, receiverId, receiverType, quotedMessageId)
    }
}
