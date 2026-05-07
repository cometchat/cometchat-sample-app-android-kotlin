package com.cometchat.uikit.compose.preview.data.repository

import com.cometchat.chat.models.Group
import com.cometchat.chat.models.User
import com.cometchat.uikit.compose.preview.domain.PreviewMockData
import com.cometchat.uikit.core.domain.repository.MessageHeaderRepository

/**
 * A mock repository implementation for Compose Previews of CometChatMessageHeader.
 * Returns pre-populated mock user/group data without making any SDK calls.
 *
 * @param mockUser The user to return from getUser(). Defaults to a sample online user.
 * @param mockGroup The group to return from getGroup(). Defaults to a sample public group.
 * @param simulateError If true, all operations will return failure results.
 */
class PreviewMessageHeaderRepository(
    private val mockUser: User = PreviewMockData.createMockUser(
        name = "Alice Smith"
    ),
    private val mockGroup: Group = PreviewMockData.createMockGroup(
        name = "Engineering Team",
        membersCount = 12
    ),
    private val simulateError: Boolean = false
) : MessageHeaderRepository {

    override suspend fun getUser(uid: String): Result<User> {
        if (simulateError) {
            return Result.failure(
                com.cometchat.chat.exceptions.CometChatException(
                    "PREVIEW_ERROR",
                    "Simulated error for preview"
                )
            )
        }
        return Result.success(mockUser)
    }

    override suspend fun getGroup(guid: String): Result<Group> {
        if (simulateError) {
            return Result.failure(
                com.cometchat.chat.exceptions.CometChatException(
                    "PREVIEW_ERROR",
                    "Simulated error for preview"
                )
            )
        }
        return Result.success(mockGroup)
    }
}
