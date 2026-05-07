package com.cometchat.uikit.compose.preview.data.repository

import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.models.GroupMember
import com.cometchat.uikit.core.domain.repository.GroupMembersRepository

/**
 * A mock repository implementation for Compose Previews of CometChatGroupMembers.
 * Returns pre-populated mock group member data without making any SDK calls.
 *
 * @param initialMembers The list of members to return. Defaults to sample members.
 * @param simulateError If true, all operations will return failure results.
 * @param simulateEmpty If true, returns an empty list instead of members.
 */
class PreviewGroupMembersRepository(
    private val initialMembers: List<GroupMember> = createSampleGroupMembers(),
    private val simulateError: Boolean = false,
    private val simulateEmpty: Boolean = false
) : GroupMembersRepository {

    override suspend fun fetchGroupMembers(
        guid: String,
        limit: Int,
        searchKeyword: String?
    ): Result<List<GroupMember>> {
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
        val filtered = if (searchKeyword.isNullOrBlank()) {
            initialMembers
        } else {
            initialMembers.filter {
                it.name?.contains(searchKeyword, ignoreCase = true) == true
            }
        }
        return Result.success(filtered.take(limit))
    }

    override suspend fun kickMember(guid: String, uid: String): Result<Unit> =
        Result.success(Unit)

    override suspend fun banMember(guid: String, uid: String): Result<Unit> =
        Result.success(Unit)

    override suspend fun changeMemberScope(guid: String, uid: String, scope: String): Result<Unit> =
        Result.success(Unit)

    override fun hasMore(): Boolean = false

    override fun resetRequest() { /* no-op */ }

    companion object {
        fun createSampleGroupMembers(): List<GroupMember> {
            val names = listOf(
                "Alice Smith" to CometChatConstants.SCOPE_ADMIN,
                "Bob Johnson" to CometChatConstants.SCOPE_MODERATOR,
                "Charlie Brown" to CometChatConstants.SCOPE_PARTICIPANT,
                "Diana Prince" to CometChatConstants.SCOPE_PARTICIPANT,
                "Edward Norton" to CometChatConstants.SCOPE_PARTICIPANT,
                "Fiona Apple" to CometChatConstants.SCOPE_PARTICIPANT
            )
            return names.mapIndexed { index, (name, scope) ->
                GroupMember("member_$index", scope).apply {
                    this.name = name
                    this.status = if (index % 2 == 0)
                        CometChatConstants.USER_STATUS_ONLINE
                    else
                        CometChatConstants.USER_STATUS_OFFLINE
                }
            }
        }
    }
}
