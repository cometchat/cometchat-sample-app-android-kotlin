package com.cometchat.uikit.core.domain.usecase

import com.cometchat.chat.models.Group
import com.cometchat.uikit.core.domain.repository.MessageHeaderRepository

/**
 * Use case for fetching a group by GUID.
 * Contains business logic for group retrieval for the message header component.
 *
 * This use case is used by the CometChatMessageHeaderViewModel to fetch
 * group information when displaying a group conversation header.
 *
 * @param repository The repository to fetch group data from
 */
open class GetGroupUseCase(
    private val repository: MessageHeaderRepository
) {
    /**
     * Fetches a group by their GUID.
     *
     * This method retrieves the complete group information including name, icon,
     * member count, and group type for display in the message header.
     *
     * @param guid The unique identifier of the group
     * @return Result containing Group on success or error on failure
     */
    open suspend operator fun invoke(guid: String): Result<Group> {
        return repository.getGroup(guid)
    }
}
