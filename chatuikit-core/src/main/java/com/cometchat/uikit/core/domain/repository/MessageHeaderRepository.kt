package com.cometchat.uikit.core.domain.repository

import com.cometchat.chat.models.Group
import com.cometchat.chat.models.User

/**
 * Repository interface defining data operations contract for message header.
 * Lives in domain layer - no implementation details.
 *
 * This interface allows for custom implementations to be injected,
 * enabling flexibility in data fetching strategies (remote, local, cached).
 *
 * The message header component uses this repository to fetch user and group
 * information for display in the conversation header.
 */
interface MessageHeaderRepository {

    /**
     * Fetches a user by their UID.
     *
     * This method retrieves the complete user profile including name, avatar,
     * status, and last active timestamp for display in the message header.
     *
     * @param uid The unique identifier of the user
     * @return Result containing User on success or error on failure
     */
    suspend fun getUser(uid: String): Result<User>

    /**
     * Fetches a group by their GUID.
     *
     * This method retrieves the complete group information including name, icon,
     * member count, and group type for display in the message header.
     *
     * @param guid The unique identifier of the group
     * @return Result containing Group on success or error on failure
     */
    suspend fun getGroup(guid: String): Result<Group>
}
