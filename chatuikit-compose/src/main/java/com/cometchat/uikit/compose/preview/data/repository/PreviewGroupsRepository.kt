package com.cometchat.uikit.compose.preview.data.repository

import com.cometchat.chat.core.GroupsRequest
import com.cometchat.chat.models.Group
import com.cometchat.uikit.compose.preview.domain.PreviewMockData
import com.cometchat.uikit.core.domain.repository.GroupsRepository

/**
 * A mock repository implementation for Compose Previews of CometChatGroups.
 * Returns pre-populated mock data without making any SDK calls.
 *
 * @param initialGroups The list of groups to return. Defaults to sample groups.
 * @param simulateError If true, all operations will return failure results.
 * @param simulateEmpty If true, returns an empty list instead of groups.
 */
class PreviewGroupsRepository(
    private val initialGroups: List<Group> = PreviewMockData.createSampleGroups(),
    private val simulateError: Boolean = false,
    private val simulateEmpty: Boolean = false
) : GroupsRepository {

    override suspend fun fetchGroups(request: GroupsRequest): Result<List<Group>> {
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

        return Result.success(initialGroups)
    }

    override suspend fun joinGroup(
        groupId: String,
        groupType: String,
        password: String?
    ): Result<Group> {
        return Result.success(
            PreviewMockData.createMockGroup(guid = groupId, groupType = groupType)
        )
    }

    override fun hasMoreGroups(): Boolean = false
}
