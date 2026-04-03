package com.cometchat.uikit.core.domain.usecase

import com.cometchat.uikit.core.domain.repository.GroupMembersRepository

/**
 * Use case for banning a member from a group.
 * Contains business logic for member ban operation.
 * 
 * @param repository The repository to perform ban operation
 */
open class BanGroupMemberUseCase(
    private val repository: GroupMembersRepository
) {
    /**
     * Bans a member from the group.
     * @param guid The group ID
     * @param uid The user ID to ban
     * @return Result indicating success or failure
     */
    open suspend operator fun invoke(
        guid: String,
        uid: String
    ): Result<Unit> {
        return repository.banMember(guid, uid)
    }
}
