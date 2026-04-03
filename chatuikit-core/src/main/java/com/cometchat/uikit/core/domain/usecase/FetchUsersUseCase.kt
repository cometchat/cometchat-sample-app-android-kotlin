package com.cometchat.uikit.core.domain.usecase

import com.cometchat.chat.core.UsersRequest
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.domain.repository.UsersRepository

/**
 * Use case for fetching users.
 * Contains business logic for user retrieval with pagination support.
 * 
 * @param repository The repository to fetch users from
 */
open class FetchUsersUseCase(
    private val repository: UsersRepository
) {
    /**
     * Fetches users based on the provided request.
     * @param request The configured UsersRequest
     * @return Result containing list of users or error
     */
    open suspend operator fun invoke(request: UsersRequest): Result<List<User>> {
        return repository.getUsers(request)
    }
    
    /**
     * Checks if there are more users available for pagination.
     * @return true if more users can be fetched
     */
    open fun hasMore(): Boolean = repository.hasMoreUsers()
}
