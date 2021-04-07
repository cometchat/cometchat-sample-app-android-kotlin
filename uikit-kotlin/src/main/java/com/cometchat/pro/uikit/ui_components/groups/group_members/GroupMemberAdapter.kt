package com.cometchat.pro.uikit.ui_components.groups.group_members

import com.cometchat.pro.uikit.ui_components.groups.group_members.GroupMemberAdapter.GroupMemberViewHolder
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.cometchat.pro.constants.CometChatConstants
import com.cometchat.pro.core.CometChat
import com.cometchat.pro.models.GroupMember
import com.cometchat.pro.uikit.R
import com.cometchat.pro.uikit.databinding.CometchatUserListItemBinding
import com.cometchat.pro.uikit.ui_resources.utils.FontUtils
import com.cometchat.pro.uikit.ui_resources.utils.Utils
import java.util.*

/**
 * Purpose - GroupMemberAdapter is a subclass of RecyclerView Adapter which is used to display
 * the list of group members. It helps to organize the list data in recyclerView.
 * It also help to perform search operation on list of groups members.
 *
 * Created on - 20th December 2019
 *
 * Modified on  - 24th January 2020
 *
 */
class GroupMemberAdapter : RecyclerView.Adapter<GroupMemberViewHolder> {
    private lateinit var context: Context
    private var groupOwnerId: String? = null
    private var groupMemberList: MutableList<GroupMember> = ArrayList()
    private lateinit var fontUtils: FontUtils


    /**
     * It is constructor which takes groupMemberList as parameter and bind it with groupMemberList in adapter.
     *
     * @param context is a object of Context.
     * @param groupMemberList is a list of group member used in this adapter.
     */
    constructor(context: Context, groupMemberList: List<GroupMember>?, groupOwnerId: String?):super(){
        this.groupMemberList = groupMemberList!! as MutableList<GroupMember>
        this.groupOwnerId = groupOwnerId
        this.context = context
        fontUtils = FontUtils.getInstance(context)
    }

    constructor(context: Context) : super() {
        this.context = context
        fontUtils = FontUtils.getInstance(context)
    }


    init {
        updateGroupMembers(groupMemberList!!)
//        this.groupOwnerId = groupOwnerId
//        this.context = context
//        fontUtils = FontUtils.getInstance(context)
    }

    override fun onCreateViewHolder(parent: ViewGroup, i: Int): GroupMemberViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val userListRowBinding: CometchatUserListItemBinding = DataBindingUtil.inflate(layoutInflater, R.layout.cometchat_user_list_item, parent, false)
        return GroupMemberViewHolder(userListRowBinding)
    }

    /**
     * This method is used to bind the GroupMemberViewHolder contents with groupMember at given
     * position. It set name icon, scope with respective GroupMemberViewHolder content.
     *
     * @param groupMemberViewHolder is a object of GroupMemberViewHolder.
     * @param i is a position of item in recyclerView.
     * @see GroupMember
     */
    override fun onBindViewHolder(groupMemberViewHolder: GroupMemberViewHolder, i: Int) {
        val groupMember = groupMemberList[i]
        groupMemberViewHolder.userListRowBinding.avUser.setBackgroundColor(context.resources.getColor(R.color.colorPrimary))
        groupMemberViewHolder.userListRowBinding.root.setTag(R.string.user, groupMember)
        if (groupMember.uid == CometChat.getLoggedInUser().uid) {
            groupMemberViewHolder.userListRowBinding.txtUserName.setText(R.string.you)
        } else groupMemberViewHolder.userListRowBinding.txtUserName.text = groupMember.name
        if (groupOwnerId != null && groupMember.uid == groupOwnerId && groupMember.scope == CometChatConstants.SCOPE_ADMIN) {
            groupMemberViewHolder.userListRowBinding.txtUserScope.setText(R.string.owner)
        } else if (groupMember.scope == CometChatConstants.SCOPE_ADMIN) {
            groupMemberViewHolder.userListRowBinding.txtUserScope.setText(R.string.admin)
        } else if (groupMember.scope == CometChatConstants.SCOPE_MODERATOR) {
            groupMemberViewHolder.userListRowBinding.txtUserScope.setText(R.string.moderator)
        } else {
            groupMemberViewHolder.userListRowBinding.txtUserScope.text = ""
        }
        groupMemberViewHolder.userListRowBinding.txtUserName.typeface = fontUtils.getTypeFace(FontUtils.robotoMedium)
        if (groupMember.avatar == null || groupMember.avatar.isEmpty()) groupMemberViewHolder.userListRowBinding.avUser.setInitials(groupMember.name) else groupMemberViewHolder.userListRowBinding.avUser.setAvatar(groupMember.avatar)
        if (groupMember.status.equals(CometChatConstants.USER_STATUS_ONLINE, ignoreCase = true)) groupMemberViewHolder.userListRowBinding.statusIndicator.visibility = View.VISIBLE

        groupMemberViewHolder.userListRowBinding.statusIndicator.setUserStatus(groupMember.status)

        if (Utils.isDarkMode(context)) {
            groupMemberViewHolder.userListRowBinding.txtUserName.setTextColor(context.resources.getColor(R.color.textColorWhite))
            groupMemberViewHolder.userListRowBinding.tvSeprator.setBackgroundColor(context.resources.getColor(R.color.grey))
            groupMemberViewHolder.userListRowBinding.txtUserScope.setTextColor(context.resources.getColor(R.color.textColorWhite))
        } else {
            groupMemberViewHolder.userListRowBinding.txtUserName.setTextColor(context.resources.getColor(R.color.primaryTextColor))
            groupMemberViewHolder.userListRowBinding.tvSeprator.setBackgroundColor(context.resources.getColor(R.color.light_grey))
            groupMemberViewHolder.userListRowBinding.txtUserScope.setTextColor(context.resources.getColor(R.color.secondaryTextColor))
        }
        if (i == itemCount - 1) groupMemberViewHolder.userListRowBinding.tvSeprator.visibility = View.GONE
    }

    override fun getItemCount(): Int {
        return groupMemberList.size
    }

    /**
     * This method is used to add group members in groupMemberList of adapter
     *
     * @param groupMembers is a list of group members which will be added in adapter.
     * @see GroupMember
     */
    fun addAll(groupMembers: List<GroupMember?>?) {
        for (groupMember in groupMembers!!) {
            if (!groupMemberList.contains(groupMember)) {
                groupMemberList.add(groupMember!!)
            }
        }
        notifyDataSetChanged()
    }

    /**
     * This method is used to set search list in a groupMemberList of adapter.
     *
     * @param filterlist is a list of searched group members.
     */
    fun searchGroupMembers(filterlist: MutableList<GroupMember>) {
        groupMemberList = filterlist
        notifyDataSetChanged()
    }

    /**
     * This method is used to add group member in a groupMemberList of adapter.
     *
     * @param joinedUser is object of GroupMember which will be added in a groupList.
     * @see GroupMember
     */
    fun addGroupMember(joinedUser: GroupMember) {
        groupMemberList.add(joinedUser)
        notifyDataSetChanged()
    }

    /**
     * This method is used to remove group member from groupMemberList of adapter.
     *
     * @param groupMember is a object of GroupMember which will be removed from groupList.
     * @see GroupMember
     */
    fun removeGroupMember(groupMember: GroupMember?) {
        if (groupMemberList.contains(groupMember)) {
            groupMemberList.remove(groupMember)
            notifyDataSetChanged()
        }
    }

    /**
     * This method is used to update group member from a groupMemberList of a adapter.
     *
     * @param groupMember is a object of GroupMember which will updated with old group member in
     * groupMemberList.
     * @see GroupMember
     */
    fun updateMember(groupMember: GroupMember) {
        if (groupMemberList.contains(groupMember)) {
            val index = groupMemberList.indexOf(groupMember)
            groupMemberList.remove(groupMember)
            groupMemberList.add(index, groupMember)
            notifyItemChanged(index)
        }
    }

    fun resetAdapter() {
        groupMemberList.clear()
        notifyDataSetChanged()
    }

    inner class GroupMemberViewHolder(var userListRowBinding: CometchatUserListItemBinding) : RecyclerView.ViewHolder(userListRowBinding.root)

    /**
     * This method is used to update group members in a groupMemberList of a adapter.
     *
     * @param groupMembers is a list of updated group members.
     */
    fun updateGroupMembers(groupMembers: List<GroupMember>) {
        for (i in groupMembers.indices) {
            if (groupMemberList.contains(groupMembers[i])) {
                val index = groupMemberList.indexOf(groupMembers[i])
                groupMemberList.removeAt(index)
                groupMemberList.add(index, groupMembers[i])
            } else {
                groupMemberList.add(groupMembers[i])
            }
        }
        notifyDataSetChanged()
    }

    companion object {
        private val TAG = GroupMemberAdapter::class.java.simpleName
    }
}