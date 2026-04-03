package com.cometchat.uikit.core.domain.repository

import com.cometchat.chat.models.Group
import com.cometchat.chat.core.GroupsRequest

/**
 * Repository interface defining data operations contract for groups.
 * Lives in domain layer - no implementation details.
 * 
 * This interface allows for custom implementations to be injected,
 * enabling flexibility in data fetching strategies (remote, local, cached).
 */
interface GroupsRepository {
    
    /**
     * Fetches groups based on the provided request configuration.
     * @param request The configured GroupsRequest with pagination and filters
     * @return Result containing list of groups or error
     */
    suspend fun fetchGroups(request: GroupsRequest): Result<List<Group>>
    
    /**
     * Joins a group.
     * @param groupId The ID of the group to join
     * @param groupType The type of group (public, private, password)
     * @param password Optional password for password-protected groups
     * @return Result containing the joined group or error
     */
    suspend fun joinGroup(
        groupId: String,
        groupType: String,
        password: String? = null
    ): Result<Group>
    
    /**
     * Checks if there are more groups to fetch (pagination).
     * @return true if more groups are available
     */
    fun hasMoreGroups(): Boolean
}
