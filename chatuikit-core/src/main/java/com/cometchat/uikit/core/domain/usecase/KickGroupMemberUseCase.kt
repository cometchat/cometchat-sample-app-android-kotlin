package com.cometchat.uikit.core.domain.usecase

import com.cometchat.uikit.core.domain.repository.GroupMembersRepository

/**
 * Use case for kicking a member from a group.
 * Contains business logic for member kick operation.
 * 
 * @param repository The repository to perform kick operation
 */
open class KickGroupMemberUseCase(
    private val repository: GroupMembersRepository
) {
    /**
     * Kicks a member from the group.
     * @param guid The group ID
     * @param uid The user ID to kick
     * @return Result indicating success or failure
     */
    open suspend operator fun invoke(
        guid: String,
        uid: String
    ): Result<Unit> {
        return repository.kickMember(guid, uid)
    }
}
