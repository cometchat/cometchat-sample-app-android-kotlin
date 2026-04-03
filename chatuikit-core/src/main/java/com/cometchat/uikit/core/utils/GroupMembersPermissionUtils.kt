package com.cometchat.uikit.core.utils

import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.GroupMember
import com.cometchat.chat.models.User

/**
 * Shared utility object for group member permission checks and scope operations.
 *
 * This lives in chatuikit-core so both chatuikit-jetpack (Compose) and
 * chatuikit-kotlin (XML Views) can share the same business logic without duplication.
 *
 * Contains:
 * - Permission checks (kick, ban, change scope)
 * - Scope hierarchy and comparison utilities
 * - User-to-GroupMember conversion
 */
object GroupMembersPermissionUtils {

    /**
     * Owner scope constant. Not defined in CometChatConstants, so we define it here.
     */
    const val SCOPE_OWNER = "OWNER"

    // ==================== Permission Checks ====================

    /**
     * Checks if the logged-in user can kick the specified group member.
     *
     * Permission rules:
     * - Moderators can kick participants
     * - Admins can kick participants and moderators
     * - Owners can kick anyone except themselves
     * - Cannot kick users with equal or higher scope
     *
     * @param loggedInUserScope The scope of the logged-in user
     * @param targetMemberScope The scope of the target member
     * @param loggedInUserId The UID of the logged-in user
     * @param targetMemberId The UID of the target member
     * @param groupOwnerId The UID of the group owner
     * @return true if the logged-in user can kick the target member
     */
    fun canKickMember(
        loggedInUserScope: String,
        targetMemberScope: String,
        loggedInUserId: String,
        targetMemberId: String,
        groupOwnerId: String?
    ): Boolean {
        // Cannot kick yourself
        if (loggedInUserId == targetMemberId) return false

        // Owner can kick anyone except themselves
        if (groupOwnerId == loggedInUserId && targetMemberId != groupOwnerId) {
            return true
        }

        // Admin can kick moderators and participants
        if (CometChatConstants.SCOPE_ADMIN.equals(loggedInUserScope, ignoreCase = true)) {
            return CometChatConstants.SCOPE_MODERATOR.equals(targetMemberScope, ignoreCase = true) ||
                    CometChatConstants.SCOPE_PARTICIPANT.equals(targetMemberScope, ignoreCase = true)
        }

        // Moderator can kick participants only
        if (CometChatConstants.SCOPE_MODERATOR.equals(loggedInUserScope, ignoreCase = true)) {
            return CometChatConstants.SCOPE_PARTICIPANT.equals(targetMemberScope, ignoreCase = true)
        }

        return false
    }

    /**
     * Checks if the logged-in user can ban the specified group member.
     *
     * Permission rules:
     * - Admins can ban participants and moderators
     * - Owners can ban anyone except themselves
     * - Moderators cannot ban anyone
     *
     * @param loggedInUserScope The scope of the logged-in user
     * @param targetMemberScope The scope of the target member
     * @param loggedInUserId The UID of the logged-in user
     * @param targetMemberId The UID of the target member
     * @param groupOwnerId The UID of the group owner
     * @return true if the logged-in user can ban the target member
     */
    fun canBanMember(
        loggedInUserScope: String,
        targetMemberScope: String,
        loggedInUserId: String,
        targetMemberId: String,
        groupOwnerId: String?
    ): Boolean {
        // Cannot ban yourself
        if (loggedInUserId == targetMemberId) return false

        // Owner can ban anyone except themselves
        if (groupOwnerId == loggedInUserId && targetMemberId != groupOwnerId) {
            return true
        }

        // Admin can ban moderators and participants
        if (CometChatConstants.SCOPE_ADMIN.equals(loggedInUserScope, ignoreCase = true)) {
            return CometChatConstants.SCOPE_MODERATOR.equals(targetMemberScope, ignoreCase = true) ||
                    CometChatConstants.SCOPE_PARTICIPANT.equals(targetMemberScope, ignoreCase = true)
        }

        // Moderators cannot ban
        return false
    }

    /**
     * Checks if the logged-in user can change the scope of the specified group member.
     *
     * Permission rules:
     * - Only the group owner can change member scopes
     * - Cannot change your own scope
     * - Cannot change the owner's scope
     *
     * @param loggedInUserId The UID of the logged-in user
     * @param targetMemberId The UID of the target member
     * @param groupOwnerId The UID of the group owner
     * @return true if the logged-in user can change the target member's scope
     */
    fun canChangeMemberScope(
        loggedInUserId: String,
        targetMemberId: String,
        groupOwnerId: String?
    ): Boolean {
        // Only owner can change scopes
        if (groupOwnerId != loggedInUserId) return false

        // Cannot change your own scope or the owner's scope
        return targetMemberId != loggedInUserId && targetMemberId != groupOwnerId
    }

    // ==================== Scope Utilities ====================

    /**
     * Gets the scope hierarchy level for sorting or comparison.
     * Higher numbers indicate higher privileges.
     *
     * @param scope The scope constant
     * @return Numeric level (4=Owner, 3=Admin, 2=Moderator, 1=Participant, 0=Unknown)
     */
    fun getScopeLevel(scope: String): Int {
        return when (scope.uppercase()) {
            SCOPE_OWNER -> 4
            CometChatConstants.SCOPE_ADMIN.uppercase() -> 3
            CometChatConstants.SCOPE_MODERATOR.uppercase() -> 2
            CometChatConstants.SCOPE_PARTICIPANT.uppercase() -> 1
            else -> 0
        }
    }

    /**
     * Compares two scopes to determine which has higher privileges.
     *
     * @param scope1 First scope to compare
     * @param scope2 Second scope to compare
     * @return Positive if scope1 > scope2, negative if scope1 < scope2, 0 if equal
     */
    fun compareScopes(scope1: String, scope2: String): Int {
        return getScopeLevel(scope1) - getScopeLevel(scope2)
    }

    /**
     * Gets the available scopes that can be assigned to a member.
     * Excludes the OWNER scope as it cannot be assigned through scope change.
     *
     * @return List of assignable scope constants
     */
    fun getAssignableScopes(): List<String> {
        return listOf(
            CometChatConstants.SCOPE_ADMIN,
            CometChatConstants.SCOPE_MODERATOR,
            CometChatConstants.SCOPE_PARTICIPANT
        )
    }

    /**
     * Checks if a member has a specific scope.
     *
     * @param member The group member to check
     * @param scope The scope to check for
     * @return true if the member has the specified scope
     */
    fun hasScope(member: GroupMember, scope: String): Boolean {
        return member.scope.equals(scope, ignoreCase = true)
    }

    /**
     * Checks if a member is the group owner.
     *
     * @param member The group member to check
     * @param group The group
     * @return true if the member is the owner
     */
    fun isOwner(member: GroupMember, group: Group): Boolean {
        return member.uid == group.owner
    }

    // ==================== Conversion Utilities ====================

    /**
     * Converts a User object to a GroupMember object.
     *
     * @param user The User object to convert
     * @param scope The scope to assign to the group member (defaults to PARTICIPANT)
     * @return A GroupMember object with the user's information
     */
    fun userToGroupMember(
        user: User,
        scope: String = CometChatConstants.SCOPE_PARTICIPANT
    ): GroupMember {
        return GroupMember(user.uid, scope).apply {
            avatar = user.avatar
            name = user.name
            status = user.status
        }
    }
}
