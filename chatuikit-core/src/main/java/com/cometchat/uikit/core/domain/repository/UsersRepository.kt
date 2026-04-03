package com.cometchat.uikit.core.domain.repository

import com.cometchat.chat.core.UsersRequest
import com.cometchat.chat.models.User

/**
 * Repository interface defining data operations contract for users.
 * Lives in domain layer - no implementation details.
 * 
 * This interface allows for custom implementations to be injected,
 * enabling flexibility in data fetching strategies (remote, local, cached).
 */
interface UsersRepository {
    
    /**
     * Fetches users based on the provided request configuration.
     * @param request The configured UsersRequest with pagination and filters
     * @return Result containing list of users or error
     */
    suspend fun getUsers(request: UsersRequest): Result<List<User>>
    
    /**
     * Checks if there are more users to fetch (pagination).
     * @return true if more users are available
     */
    fun hasMoreUsers(): Boolean
}
