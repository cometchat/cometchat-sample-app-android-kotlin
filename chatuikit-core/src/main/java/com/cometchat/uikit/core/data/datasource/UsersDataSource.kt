package com.cometchat.uikit.core.data.datasource

import com.cometchat.chat.core.UsersRequest
import com.cometchat.chat.models.User

/**
 * Interface defining data source operations for users.
 * Lives in data layer - defines contract for data fetching.
 * Allows for different implementations (remote, local, mock).
 */
interface UsersDataSource {
    
    /**
     * Fetches users from the data source.
     * @param request The configured UsersRequest
     * @return Raw list of User objects
     * @throws Exception if fetching fails
     */
    suspend fun fetchUsers(request: UsersRequest): List<User>
}
