package com.cometchat.uikit.core.domain.usecase

import com.cometchat.chat.core.UsersRequest
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.domain.repository.UsersRepository

/**
 * Use case for searching users.
 * Contains business logic for user search with keyword filtering.
 * 
 * @param repository The repository to search users from
 */
open class SearchUsersUseCase(
    private val repository: UsersRepository
) {
    /**
     * Searches users based on the provided keyword and request builder.
     * @param searchKeyword The keyword to search for
     * @param builder The UsersRequestBuilder to configure the search
     * @return Result containing list of matching users or error
     */
    open suspend operator fun invoke(
        searchKeyword: String,
        builder: UsersRequest.UsersRequestBuilder
    ): Result<List<User>> {
        val request = builder.setSearchKeyword(searchKeyword).build()
        return repository.getUsers(request)
    }
}
