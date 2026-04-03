package com.cometchat.uikit.core.domain.repository

import com.cometchat.chat.models.GroupMember

/**
 * Repository interface defining data operations contract for group members.
 * Lives in domain layer - no implementation details.
 * 
 * This interface allows for custom implementations to be injected,
 * enabling flexibility in data fetching strategies (remote, local, cached).
 */
interface GroupMembersRepository {
    
    /**
     * Fetches group members based on the provided parameters.
     * @param guid The group ID
     * @param limit Number of members to fetch per page
     * @param searchKeyword Optional search keyword to filter members
     * @return Result containing list of group members or error
     */
    suspend fun fetchGroupMembers(
        guid: String,
        limit: Int,
        searchKeyword: String?
    ): Result<List<GroupMember>>
    
    /**
     * Kicks a member from the group.
     * @param guid The group ID
     * @param uid The user ID to kick
     * @return Result indicating success or failure
     */
    suspend fun kickMember(
        guid: String,
        uid: String
    ): Result<Unit>
    
    /**
     * Bans a member from the group.
     * @param guid The group ID
     * @param uid The user ID to ban
     * @return Result indicating success or failure
     */
    suspend fun banMember(
        guid: String,
        uid: String
    ): Result<Unit>
    
    /**
     * Changes a member's scope in the group.
     * @param guid The group ID
     * @param uid The user ID
     * @param scope The new scope (admin/moderator/participant)
     * @return Result indicating success or failure
     */
    suspend fun changeMemberScope(
        guid: String,
        uid: String,
        scope: String
    ): Result<Unit>
    
    /**
     * Checks if there are more members to fetch (pagination).
     * @return true if more members are available
     */
    fun hasMore(): Boolean
    
    /**
     * Resets the current request so the next fetch builds a fresh one.
     * Call this when the search keyword changes or when refreshing.
     */
    fun resetRequest()
}
