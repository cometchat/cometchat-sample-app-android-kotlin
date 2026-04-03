package com.cometchat.uikit.kotlin.presentation.groupmembers.utils

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.GroupMember
import com.cometchat.uikit.kotlin.databinding.CometchatGroupMemberListItemBinding
import com.cometchat.uikit.kotlin.shared.interfaces.ViewHolderCallBack

/**
 * Abstract class for managing custom views within the group members list.
 *
 * Follows the chatuikit Java pattern where:
 * - [createView] is called once during ViewHolder creation
 * - [bindView] is called during bind operations with member data
 */
abstract class GroupMembersViewHolderListener : ViewHolderCallBack {

    /**
     * Creates a custom view to be used in the group member list item.
     * Called once when the ViewHolder is created.
     *
     * @param context The context
     * @param binding The ViewBinding for the group member list item layout
     * @return The custom view to display
     */
    abstract fun createView(
        context: Context,
        binding: CometchatGroupMemberListItemBinding
    ): View

    /**
     * Binds data to the custom view.
     * Called each time the ViewHolder is bound to a group member.
     *
     * @param context The context
     * @param createdView The view created by [createView]
     * @param groupMember The group member to bind
     * @param group The group the member belongs to
     * @param holder The ViewHolder
     * @param memberList The full list of group members
     * @param position The position in the list
     */
    abstract fun bindView(
        context: Context,
        createdView: View,
        groupMember: GroupMember,
        group: Group?,
        holder: RecyclerView.ViewHolder,
        memberList: List<GroupMember>,
        position: Int
    )
}
