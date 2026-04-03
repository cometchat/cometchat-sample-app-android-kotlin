package com.cometchat.uikit.core.data.repository

import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.data.datasource.MessageHeaderDataSource
import com.cometchat.uikit.core.domain.repository.MessageHeaderRepository

/**
 * Repository implementation that coordinates data sources for message header.
 * Decides where data comes from and handles data transformation.
 * Accepts MessageHeaderDataSource interface for flexibility.
 *
 * This implementation wraps data source calls in Result type for proper
 * error handling, allowing the ViewModel to handle errors gracefully
 * and maintain the last known valid state.
 *
 * @param dataSource The data source to fetch user/group data (interface, not concrete)
 */
class MessageHeaderRepositoryImpl(
    private val dataSource: MessageHeaderDataSource
) : MessageHeaderRepository {

    /**
     * Fetches a user by their UID from the data source.
     *
     * Wraps the data source call in a Result type to provide proper error handling.
     * Both CometChatException and general exceptions are caught and wrapped as failures.
     *
     * @param uid The unique identifier of the user
     * @return Result containing User on success or error on failure
     */
    override suspend fun getUser(uid: String): Result<User> {
        return try {
            val user = dataSource.getUser(uid)
            Result.success(user)
        } catch (e: CometChatException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetches a group by their GUID from the data source.
     *
     * Wraps the data source call in a Result type to provide proper error handling.
     * Both CometChatException and general exceptions are caught and wrapped as failures.
     *
     * @param guid The unique identifier of the group
     * @return Result containing Group on success or error on failure
     */
    override suspend fun getGroup(guid: String): Result<Group> {
        return try {
            val group = dataSource.getGroup(guid)
            Result.success(group)
        } catch (e: CometChatException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
