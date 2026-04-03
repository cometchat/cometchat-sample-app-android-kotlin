package com.cometchat.uikit.core.data.datasource

import com.cometchat.chat.models.Group
import com.cometchat.chat.core.GroupsRequest

/**
 * Interface defining data source operations for groups.
 * Lives in data layer - defines contract for data fetching.
 * Allows for different implementations (remote, local, mock).
 */
interface GroupsDataSource {
    
    /**
     * Fetches groups from the data source.
     * @param request The configured GroupsRequest
     * @return Raw list of Group objects
     * @throws Exception if fetching fails
     */
    suspend fun fetchGroups(request: GroupsRequest): List<Group>
    
    /**
     * Joins a group.
     * @param groupId The ID of the group to join
     * @param groupType The type of group (public, private, password)
     * @param password Optional password for password-protected groups
     * @return The joined Group
     * @throws Exception if joining fails
     */
    suspend fun joinGroup(
        groupId: String,
        groupType: String,
        password: String?
    ): Group
}
