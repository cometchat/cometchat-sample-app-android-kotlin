package com.cometchat.uikit.core.data.datasource

import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.models.GroupMember

/**
 * Interface defining data source operations for group members.
 * Lives in data layer - defines contract for data fetching.
 * Allows for different implementations (remote, local, mock).
 */
interface GroupMembersDataSource {
    
    /**
     * Fetches group members from the data source.
     * @param guid The group ID
     * @param limit Number of members to fetch per page
     * @param searchKeyword Optional search keyword to filter members
     * @return Raw list of GroupMember objects
     * @throws Exception if fetching fails
     */
    suspend fun fetchGroupMembers(
        guid: String,
        limit: Int,
        searchKeyword: String?
    ): List<GroupMember>
    
    /**
     * Kicks a member from the group.
     * @param guid The group ID
     * @param uid The user ID to kick
     * @return Success message
     * @throws Exception if kick fails
     */
    suspend fun kickGroupMember(
        guid: String,
        uid: String
    ): String
    
    /**
     * Bans a member from the group.
     * @param guid The group ID
     * @param uid The user ID to ban
     * @return Success message
     * @throws Exception if ban fails
     */
    suspend fun banGroupMember(
        guid: String,
        uid: String
    ): String
    
    /**
     * Changes a member's scope in the group.
     * @param guid The group ID
     * @param uid The user ID
     * @param scope The new scope (admin/moderator/participant)
     * @return Success message
     * @throws Exception if scope change fails
     */
    suspend fun changeMemberScope(
        guid: String,
        uid: String,
        scope: String
    ): String
    
    /**
     * Checks if there are more members available for pagination.
     * @return true if more members can be fetched
     */
    fun hasMoreMembers(): Boolean
    
    /**
     * Resets the current request so the next fetch builds a fresh one.
     * Call this when the search keyword changes or when refreshing.
     */
    fun resetRequest()
}
