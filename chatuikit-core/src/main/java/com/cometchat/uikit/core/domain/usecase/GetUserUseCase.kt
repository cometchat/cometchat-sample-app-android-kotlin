package com.cometchat.uikit.core.domain.usecase

import com.cometchat.chat.models.User
import com.cometchat.uikit.core.domain.repository.MessageHeaderRepository

/**
 * Use case for fetching a user by UID.
 * Contains business logic for user retrieval for the message header component.
 *
 * This use case is used by the CometChatMessageHeaderViewModel to fetch
 * user information when displaying a one-on-one conversation header.
 *
 * @param repository The repository to fetch user data from
 */
open class GetUserUseCase(
    private val repository: MessageHeaderRepository
) {
    /**
     * Fetches a user by their UID.
     *
     * This method retrieves the complete user profile including name, avatar,
     * status, and last active timestamp for display in the message header.
     *
     * @param uid The unique identifier of the user
     * @return Result containing User on success or error on failure
     */
    open suspend operator fun invoke(uid: String): Result<User> {
        return repository.getUser(uid)
    }
}
