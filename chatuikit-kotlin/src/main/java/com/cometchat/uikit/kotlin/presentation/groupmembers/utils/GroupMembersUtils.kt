package com.cometchat.uikit.kotlin.presentation.groupmembers.utils

import android.content.Context
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.GroupMember
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.utils.GroupMembersPermissionUtils
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.presentation.shared.popupmenu.CometChatPopupMenu

/**
 * XML Views-specific utility object for group members functionality.
 *
 * Permission checks, scope utilities, and conversion helpers are delegated to
 * [GroupMembersPermissionUtils] in chatuikit-core. This object adds UI-layer
 * concerns: localized display strings and [CometChatPopupMenu.MenuItem] generation.
 */
object GroupMembersUtils {

    // ==================== Permission Checks (delegated to core) ====================

    /** @see GroupMembersPermissionUtils.canKickMember */
    fun canKickMember(
        loggedInUserScope: String,
        targetMemberScope: String,
        loggedInUserId: String,
        targetMemberId: String,
        groupOwnerId: String?
    ): Boolean = GroupMembersPermissionUtils.canKickMember(
        loggedInUserScope, targetMemberScope,
        loggedInUserId, targetMemberId, groupOwnerId
    )

    /** @see GroupMembersPermissionUtils.canBanMember */
    fun canBanMember(
        loggedInUserScope: String,
        targetMemberScope: String,
        loggedInUserId: String,
        targetMemberId: String,
        groupOwnerId: String?
    ): Boolean = GroupMembersPermissionUtils.canBanMember(
        loggedInUserScope, targetMemberScope,
        loggedInUserId, targetMemberId, groupOwnerId
    )

    /** @see GroupMembersPermissionUtils.canChangeMemberScope */
    fun canChangeMemberScope(
        loggedInUserId: String,
        targetMemberId: String,
        groupOwnerId: String?
    ): Boolean = GroupMembersPermissionUtils.canChangeMemberScope(
        loggedInUserId, targetMemberId, groupOwnerId
    )

    // ==================== Menu Options (XML Views-specific) ====================

    /**
     * Generates the default popup menu items for a group member based on permissions.
     *
     * @param context Android context for string resources
     * @param groupMember The target group member
     * @param group The group containing the member
     * @param loggedInUserId The UID of the logged-in user
     * @param loggedInUserScope The scope of the logged-in user
     * @param disableKick Whether to disable the kick option
     * @param disableBan Whether to disable the ban option
     * @param disableChangeScope Whether to disable the change scope option
     * @return List of popup menu items for the member
     */
    fun getDefaultGroupMemberOptions(
        context: Context,
        groupMember: GroupMember,
        group: Group,
        loggedInUserId: String,
        loggedInUserScope: String,
        disableKick: Boolean = false,
        disableBan: Boolean = false,
        disableChangeScope: Boolean = false
    ): List<CometChatPopupMenu.MenuItem> {
        val menuItems = mutableListOf<CometChatPopupMenu.MenuItem>()

        val canKick = !disableKick && canKickMember(
            loggedInUserScope = loggedInUserScope,
            targetMemberScope = groupMember.scope,
            loggedInUserId = loggedInUserId,
            targetMemberId = groupMember.uid,
            groupOwnerId = group.owner
        )

        val canBan = !disableBan && canBanMember(
            loggedInUserScope = loggedInUserScope,
            targetMemberScope = groupMember.scope,
            loggedInUserId = loggedInUserId,
            targetMemberId = groupMember.uid,
            groupOwnerId = group.owner
        )

        val canChangeScope = !disableChangeScope && canChangeMemberScope(
            loggedInUserId = loggedInUserId,
            targetMemberId = groupMember.uid,
            groupOwnerId = group.owner
        )

        if (canChangeScope) {
            menuItems.add(
                CometChatPopupMenu.MenuItem(
                    UIKitConstants.GroupMemberOption.CHANGE_SCOPE,
                    context.getString(R.string.cometchat_scope_change),
                    null, null, null
                )
            )
        }

        if (canBan) {
            menuItems.add(
                CometChatPopupMenu.MenuItem(
                    UIKitConstants.GroupMemberOption.BAN,
                    context.getString(R.string.cometchat_ban),
                    null, null, null
                )
            )
        }

        if (canKick) {
            menuItems.add(
                CometChatPopupMenu.MenuItem(
                    UIKitConstants.GroupMemberOption.KICK,
                    context.getString(R.string.cometchat_kick),
                    null, null, null
                )
            )
        }

        return menuItems
    }

    // ==================== Scope Display (XML Views-specific, uses string resources) ====================

    /**
     * Gets a localized display string for a member scope.
     *
     * @param context Android context for string resources
     * @param scope The scope constant (OWNER, ADMIN, MODERATOR, PARTICIPANT)
     * @return Localized scope display string
     */
    fun getScopeDisplayName(context: Context, scope: String): String {
        return when {
            scope.equals(GroupMembersPermissionUtils.SCOPE_OWNER, ignoreCase = true) ->
                context.getString(R.string.cometchat_owner)
            scope.equals(CometChatConstants.SCOPE_ADMIN, ignoreCase = true) ->
                context.getString(R.string.cometchat_admin)
            scope.equals(CometChatConstants.SCOPE_MODERATOR, ignoreCase = true) ->
                context.getString(R.string.cometchat_moderator)
            scope.equals(CometChatConstants.SCOPE_PARTICIPANT, ignoreCase = true) ->
                context.getString(R.string.cometchat_participant)
            else -> scope
        }
    }

    /**
     * Gets localized scope options for the scope change dialog.
     *
     * @param context Android context for string resources
     * @return List of pairs containing scope constant and display name
     */
    fun getScopeOptions(context: Context): List<Pair<String, String>> {
        return GroupMembersPermissionUtils.getAssignableScopes().map { scope ->
            scope to getScopeDisplayName(context, scope)
        }
    }

    // ==================== Delegated Utilities ====================

    /** @see GroupMembersPermissionUtils.getAssignableScopes */
    fun getAssignableScopes(): List<String> = GroupMembersPermissionUtils.getAssignableScopes()

    /** @see GroupMembersPermissionUtils.userToGroupMember */
    fun userToGroupMember(
        user: User,
        scope: String = CometChatConstants.SCOPE_PARTICIPANT
    ): GroupMember = GroupMembersPermissionUtils.userToGroupMember(user, scope)

    /** @see GroupMembersPermissionUtils.hasScope */
    fun hasScope(member: GroupMember, scope: String): Boolean =
        GroupMembersPermissionUtils.hasScope(member, scope)

    /** @see GroupMembersPermissionUtils.isOwner */
    fun isOwner(member: GroupMember, group: Group): Boolean =
        GroupMembersPermissionUtils.isOwner(member, group)

    /** @see GroupMembersPermissionUtils.getScopeLevel */
    fun getScopeLevel(scope: String): Int = GroupMembersPermissionUtils.getScopeLevel(scope)

    /** @see GroupMembersPermissionUtils.compareScopes */
    fun compareScopes(scope1: String, scope2: String): Int =
        GroupMembersPermissionUtils.compareScopes(scope1, scope2)
}
