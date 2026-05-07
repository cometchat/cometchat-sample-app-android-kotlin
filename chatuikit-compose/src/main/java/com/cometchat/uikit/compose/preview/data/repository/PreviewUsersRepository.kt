package com.cometchat.uikit.compose.preview.data.repository

import com.cometchat.chat.core.UsersRequest
import com.cometchat.chat.models.User
import com.cometchat.uikit.compose.preview.domain.PreviewMockData
import com.cometchat.uikit.core.domain.repository.UsersRepository

/**
 * A mock repository implementation for Compose Previews of CometChatUsers.
 * Returns pre-populated mock data without making any SDK calls.
 *
 * @param initialUsers The list of users to return. Defaults to sample users.
 * @param simulateError If true, all operations will return failure results.
 * @param simulateEmpty If true, returns an empty list instead of users.
 */
class PreviewUsersRepository(
    private val initialUsers: List<User> = PreviewMockData.createSampleUsers(),
    private val simulateError: Boolean = false,
    private val simulateEmpty: Boolean = false
) : UsersRepository {

    override suspend fun getUsers(request: UsersRequest): Result<List<User>> {
        if (simulateError) {
            return Result.failure(
                com.cometchat.chat.exceptions.CometChatException(
                    "PREVIEW_ERROR",
                    "Simulated error for preview"
                )
            )
        }

        if (simulateEmpty) {
            return Result.success(emptyList())
        }

        return Result.success(initialUsers)
    }

    override fun hasMoreUsers(): Boolean = false
}
