package com.cometchat.uikit.core.state

import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.GroupMember

/**
 * Represents the UI state for the Group Members component.
 */
sealed class GroupMembersUIState {
    /**
     * Loading state - shown when fetching members
     */
    object Loading : GroupMembersUIState()
    
    /**
     * Content state - shown when members are successfully loaded
     * @param members List of group members
     */
    data class Content(val members: List<GroupMember>) : GroupMembersUIState()
    
    /**
     * Empty state - shown when group has no members
     */
    object Empty : GroupMembersUIState()
    
    /**
     * Error state - shown when fetch fails
     * @param exception The error that occurred
     */
    data class Error(val exception: CometChatException) : GroupMembersUIState()
}

/**
 * Events that occur during group member operations.
 * These are one-time events emitted via SharedFlow.
 */
sealed class GroupMembersEvent {
    /**
     * A member was updated at the specified index
     */
    data class MemberUpdated(val index: Int) : GroupMembersEvent()
    
    /**
     * A member was removed at the specified index
     */
    data class MemberRemoved(val index: Int) : GroupMembersEvent()
    
    /**
     * A new member was inserted at the top of the list
     */
    data class MemberInsertedAtTop(val index: Int) : GroupMembersEvent()
    
    /**
     * A member was moved to the top of the list
     */
    data class MemberMovedToTop(val index: Int) : GroupMembersEvent()
    
    /**
     * A member was kicked from the group
     */
    data class MemberKicked(val member: GroupMember) : GroupMembersEvent()
    
    /**
     * A member was banned from the group
     */
    data class MemberBanned(val member: GroupMember) : GroupMembersEvent()
    
    /**
     * A member's scope was changed
     */
    data class MemberScopeChanged(
        val member: GroupMember,
        val newScope: String
    ) : GroupMembersEvent()
}

/**
 * Represents the state of confirmation dialogs.
 */
sealed class DialogState {
    /**
     * No dialog is shown
     */
    object Hidden : DialogState()
    
    /**
     * Confirmation dialog for kicking a member
     */
    data class ConfirmKick(val member: GroupMember) : DialogState()
    
    /**
     * Confirmation dialog for banning a member
     */
    data class ConfirmBan(val member: GroupMember) : DialogState()
    
    /**
     * Dialog for selecting a new scope for a member
     */
    data class SelectScope(
        val member: GroupMember,
        val currentScope: String
    ) : DialogState()
}
