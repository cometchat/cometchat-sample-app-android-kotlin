package com.cometchat.uikit.core.domain.usecase

import com.cometchat.chat.models.GroupMember
import com.cometchat.uikit.core.domain.repository.GroupMembersRepository

/**
 * Use case for fetching group members.
 * Contains business logic for group member retrieval with pagination support.
 * 
 * @param repository The repository to fetch group members from
 */
open class FetchGroupMembersUseCase(
    private val repository: GroupMembersRepository
) {
    /**
     * Fetches group members based on the provided parameters.
     * @param guid The group ID
     * @param limit Number of members to fetch per page (default: 30)
     * @param searchKeyword Optional search keyword to filter members
     * @return Result containing list of group members or error
     */
    open suspend operator fun invoke(
        guid: String,
        limit: Int = 30,
        searchKeyword: String? = null
    ): Result<List<GroupMember>> {
        return repository.fetchGroupMembers(guid, limit, searchKeyword)
    }
    
    /**
     * Checks if there are more members available for pagination.
     * @return true if more members can be fetched
     */
    open fun hasMore(): Boolean = repository.hasMore()
    
    /**
     * Resets the current request so the next fetch builds a fresh one.
     * Call this when the search keyword changes or when refreshing.
     */
    open fun resetRequest() = repository.resetRequest()
}
