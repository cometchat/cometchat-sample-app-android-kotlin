package com.cometchat.uikit.core.domain.usecase

import com.cometchat.chat.models.Group
import com.cometchat.chat.core.GroupsRequest
import com.cometchat.uikit.core.domain.repository.GroupsRepository

/**
 * Use case for fetching groups.
 * Contains business logic for group retrieval with pagination support.
 * 
 * @param repository The repository to fetch groups from
 */
open class FetchGroupsUseCase(
    private val repository: GroupsRepository
) {
    /**
     * Fetches groups based on the provided request.
     * @param request The configured GroupsRequest
     * @return Result containing list of groups or error
     */
    open suspend operator fun invoke(request: GroupsRequest): Result<List<Group>> {
        return repository.fetchGroups(request)
    }
    
    /**
     * Checks if there are more groups available for pagination.
     * @return true if more groups can be fetched
     */
    open fun hasMore(): Boolean = repository.hasMoreGroups()
}
