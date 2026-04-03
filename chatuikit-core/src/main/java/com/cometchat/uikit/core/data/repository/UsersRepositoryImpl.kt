package com.cometchat.uikit.core.data.repository

import com.cometchat.chat.core.UsersRequest
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.data.datasource.UsersDataSource
import com.cometchat.uikit.core.domain.repository.UsersRepository

/**
 * Repository implementation that coordinates data sources for users.
 * Decides where data comes from and handles data transformation.
 * Accepts UsersDataSource interface for flexibility.
 * 
 * @param dataSource The data source to fetch data (interface, not concrete)
 */
class UsersRepositoryImpl(
    private val dataSource: UsersDataSource
) : UsersRepository {
    
    private var hasMore = true
    
    /**
     * Fetches users from the data source.
     * Tracks pagination state based on results.
     * 
     * @param request The configured UsersRequest
     * @return Result containing list of users or error
     */
    override suspend fun getUsers(request: UsersRequest): Result<List<User>> {
        return try {
            val users = dataSource.fetchUsers(request)
            hasMore = users.isNotEmpty()
            Result.success(users)
        } catch (e: CometChatException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Checks if there are more users available for pagination.
     * @return true if more users can be fetched
     */
    override fun hasMoreUsers(): Boolean = hasMore
}
