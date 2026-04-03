package com.cometchat.uikit.core.domain.usecase

import com.cometchat.uikit.core.domain.repository.GroupMembersRepository

/**
 * Use case for changing a member's scope in a group.
 * Contains business logic for member scope change operation.
 * 
 * @param repository The repository to perform scope change operation
 */
open class ChangeMemberScopeUseCase(
    private val repository: GroupMembersRepository
) {
    /**
     * Changes a member's scope in the group.
     * @param guid The group ID
     * @param uid The user ID
     * @param scope The new scope (admin/moderator/participant)
     * @return Result indicating success or failure
     */
    open suspend operator fun invoke(
        guid: String,
        uid: String,
        scope: String
    ): Result<Unit> {
        return repository.changeMemberScope(guid, uid, scope)
    }
}
