package com.cometchat.uikit.core.events

import com.cometchat.chat.models.Action
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.GroupMember
import com.cometchat.chat.models.User

/**
 * Sealed class hierarchy representing all group-related events.
 * Provides type-safe event handling for group membership and configuration changes.
 */
sealed class CometChatGroupEvent {
    /**
     * Event emitted when a group is created.
     * @param group The created group
     */
    data class GroupCreated(
        val group: Group
    ) : CometChatGroupEvent()

    /**
     * Event emitted when a group is deleted.
     * @param group The deleted group
     */
    data class GroupDeleted(
        val group: Group
    ) : CometChatGroupEvent()

    /**
     * Event emitted when a user leaves a group.
     * @param action The action message indicating the user left
     * @param user The user who left the group
     * @param group The group from which the user left
     */
    data class GroupLeft(
        val action: Action,
        val user: User,
        val group: Group
    ) : CometChatGroupEvent()

    /**
     * Event emitted when a user joins a group.
     * @param user The user who joined the group
     * @param group The group to which the user joined
     */
    data class MemberJoined(
        val user: User,
        val group: Group
    ) : CometChatGroupEvent()

    /**
     * Event emitted when users are added to a group.
     * @param actions The action messages indicating users were added
     * @param users The users who were added
     * @param group The group to which users were added
     * @param addedBy The user who added the other users
     */
    data class MembersAdded(
        val actions: List<Action>,
        val users: List<User>,
        val group: Group,
        val addedBy: User
    ) : CometChatGroupEvent()


    /**
     * Event emitted when a user is kicked from a group.
     * @param action The action message indicating the user was kicked
     * @param user The user who was kicked
     * @param kickedBy The user who kicked the other user
     * @param group The group from which the user was kicked
     */
    data class MemberKicked(
        val action: Action,
        val user: User,
        val kickedBy: User,
        val group: Group
    ) : CometChatGroupEvent()

    /**
     * Event emitted when a user is banned from a group.
     * @param action The action message indicating the user was banned
     * @param user The user who was banned
     * @param bannedBy The user who banned the other user
     * @param group The group from which the user was banned
     */
    data class MemberBanned(
        val action: Action,
        val user: User,
        val bannedBy: User,
        val group: Group
    ) : CometChatGroupEvent()

    /**
     * Event emitted when a user is unbanned from a group.
     * @param action The action message indicating the user was unbanned
     * @param user The user who was unbanned
     * @param unbannedBy The user who unbanned the other user
     * @param group The group from which the user was unbanned
     */
    data class MemberUnbanned(
        val action: Action,
        val user: User,
        val unbannedBy: User,
        val group: Group
    ) : CometChatGroupEvent()

    /**
     * Event emitted when a group member's scope changes.
     * @param action The action message indicating the scope change
     * @param user The user whose scope was changed
     * @param newScope The new scope value
     * @param oldScope The previous scope value
     * @param group The group in which the scope was changed
     */
    data class MemberScopeChanged(
        val action: Action,
        val user: User,
        val newScope: String,
        val oldScope: String,
        val group: Group
    ) : CometChatGroupEvent()

    /**
     * Event emitted when group ownership changes.
     * @param group The group for which ownership changed
     * @param newOwner The new owner of the group
     */
    data class OwnershipChanged(
        val group: Group,
        val newOwner: GroupMember
    ) : CometChatGroupEvent()
}
