package com.cometchat.uikit.core.data.repository

import com.cometchat.chat.core.GroupsRequest
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.Group
import com.cometchat.uikit.core.data.datasource.GroupsDataSource
import com.cometchat.uikit.core.domain.repository.GroupsRepository

/**
 * Repository implementation that coordinates data sources for groups.
 * Decides where data comes from and handles data transformation.
 * Accepts GroupsDataSource interface for flexibility.
 * 
 * @param dataSource The data source to fetch/save data (interface, not concrete)
 */
class GroupsRepositoryImpl(
    private val dataSource: GroupsDataSource
) : GroupsRepository {
    
    private var hasMore = true
    
    /**
     * Fetches groups from the data source.
     * Tracks pagination state based on results.
     * 
     * @param request The configured GroupsRequest
     * @return Result containing list of groups or error
     */
    override suspend fun fetchGroups(request: GroupsRequest): Result<List<Group>> {
        return try {
            val groups = dataSource.fetchGroups(request)
            hasMore = groups.isNotEmpty()
            Result.success(groups)
        } catch (e: CometChatException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Joins a group via the data source.
     * Sets hasJoined flag and scope on the returned group.
     * 
     * @param groupId The ID of the group to join
     * @param groupType The type of group (public, private, password)
     * @param password Optional password for password-protected groups
     * @return Result containing the joined group or error
     */
    override suspend fun joinGroup(
        groupId: String,
        groupType: String,
        password: String?
    ): Result<Group> {
        return try {
            val group = dataSource.joinGroup(groupId, groupType, password)
            group.setHasJoined(true)
            Result.success(group)
        } catch (e: CometChatException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Checks if there are more groups available for pagination.
     * @return true if more groups can be fetched
     */
    override fun hasMoreGroups(): Boolean = hasMore
}
