package com.cometchat.uikit.core.data.datasource

import com.cometchat.chat.models.Group
import com.cometchat.chat.models.User

/**
 * Interface defining data source operations for message header.
 * Lives in data layer - defines contract for data fetching.
 * Allows for different implementations (remote, local, mock).
 */
interface MessageHeaderDataSource {
    
    /**
     * Fetches a user by their UID from the data source.
     * @param uid The unique identifier of the user
     * @return The User object
     * @throws Exception if fetching fails
     */
    suspend fun getUser(uid: String): User
    
    /**
     * Fetches a group by their GUID from the data source.
     * @param guid The unique identifier of the group
     * @return The Group object
     * @throws Exception if fetching fails
     */
    suspend fun getGroup(guid: String): Group
}
