package com.cometchat.uikit.core.domain.usecase

import com.cometchat.chat.models.Group
import com.cometchat.uikit.core.domain.repository.GroupsRepository

/**
 * Use case for joining a group.
 * Contains business logic for group joining operation.
 * 
 * @param repository The repository to perform join operation
 */
open class JoinGroupUseCase(
    private val repository: GroupsRepository
) {
    /**
     * Joins a group with the specified parameters.
     * @param groupId The ID of the group to join
     * @param groupType The type of group (public, private, password)
     * @param password Optional password for password-protected groups
     * @return Result containing the joined group or error
     */
    open suspend operator fun invoke(
        groupId: String,
        groupType: String,
        password: String? = null
    ): Result<Group> {
        return repository.joinGroup(groupId, groupType, password)
    }
}
