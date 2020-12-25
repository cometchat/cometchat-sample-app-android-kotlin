package adapter

import adapter.GroupListAdapter.GroupViewHolder
import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.cometchat.pro.constants.CometChatConstants
import com.cometchat.pro.models.Group
import com.cometchat.pro.uikit.R
import com.cometchat.pro.uikit.databinding.GroupListRowBinding
import utils.FontUtils
import utils.Utils
import java.util.*

/**
 * Purpose - GroupListAdapter is a subclass of RecyclerView Adapter which is used to display
 * the list of groups. It helps to organize the list data in recyclerView.
 *
 * Created on - 20th December 2019
 *
 * Modified on  - 23rd March 2020
 *
 */
class GroupListAdapter(context: Context) : RecyclerView.Adapter<GroupViewHolder>() {
    private var context: Context
    private var groupList: MutableList<Group> = ArrayList()
    private var fontUtils: FontUtils

    /**
     * It is constructor which takes groupsList as parameter and bind it with groupList in adapter.
     *
     * @param context is a object of Context.
     * @param groupList is a list of groups used in this adapter.
     */
    init {
        updateGroupList(groupList)
        this.context = context
        fontUtils = FontUtils.getInstance(context)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val groupListRowBinding: GroupListRowBinding = DataBindingUtil.inflate(layoutInflater, R.layout.group_list_row, parent, false)
        return GroupViewHolder(groupListRowBinding)
    }

    /**
     * This method is used to bind the GroupViewHolder contents with group at given
     * position. It set group icon, group name in a respective GroupViewHolder content.
     *
     * @param groupViewHolder is a object of GroupViewHolder.
     * @param position is a position of item in recyclerView.
     * @see Group
     */
    override fun onBindViewHolder(groupViewHolder: GroupViewHolder, position: Int) {
        val group = groupList[position]
        groupViewHolder.groupListRowBinding.group = group
        groupViewHolder.groupListRowBinding.executePendingBindings()

        groupViewHolder.groupListRowBinding.txtUserMessage.text = "Members: "+group.membersCount

        when (group.groupType) {
            CometChatConstants.GROUP_TYPE_PRIVATE -> groupViewHolder.groupListRowBinding.txtUserName.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_security_24dp, 0)
            CometChatConstants.GROUP_TYPE_PASSWORD -> groupViewHolder.groupListRowBinding.txtUserName.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_lock_24dp, 0)
            else -> groupViewHolder.groupListRowBinding.txtUserName.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        }

        groupViewHolder.groupListRowBinding.avGroup.setBackgroundColor(context.resources.getColor(R.color.colorPrimary))
        groupViewHolder.groupListRowBinding.root.setTag(R.string.group, group)
        groupViewHolder.groupListRowBinding.txtUserMessage.typeface = fontUtils.getTypeFace(FontUtils.robotoRegular)
        groupViewHolder.groupListRowBinding.txtUserName.typeface = fontUtils.getTypeFace(FontUtils.robotoMedium)
        if (Utils.isDarkMode(context)) {
            groupViewHolder.groupListRowBinding.txtUserName.compoundDrawableTintList = ColorStateList.valueOf(context.resources.getColor(R.color.grey))
            groupViewHolder.groupListRowBinding.txtUserName.setTextColor(context.resources.getColor(R.color.textColorWhite))
            groupViewHolder.groupListRowBinding.tvSeprator.setBackgroundColor(context.resources.getColor(R.color.grey))
        } else {
            groupViewHolder.groupListRowBinding.txtUserName.compoundDrawableTintList = ColorStateList.valueOf(context.resources.getColor(R.color.message_bubble_grey))
            groupViewHolder.groupListRowBinding.txtUserName.setTextColor(context.resources.getColor(R.color.primaryTextColor))
            groupViewHolder.groupListRowBinding.tvSeprator.setBackgroundColor(context.resources.getColor(R.color.light_grey))
        }
    }

    /**
     * This method is used to update groupList in adapter.
     * @param groupList is a list of groups which will be updated in adapter.
     */
    fun updateGroupList(groupList: List<Group?>) {
        for (i in groupList.indices) {
            if (!this.groupList.contains(groupList[i])) {
                this.groupList.add(groupList[i]!!)
            }
        }
        notifyDataSetChanged()
    }

    /**
     * This method is used to update a particular group in groupList of adapter.
     *
     * @param group is an object of Group. It will be updated with previous group in a list.
     */
    fun updateGroup(group: Group?) {
        if (group != null) {
            if (groupList.contains(group)) {
                val index = groupList.indexOf(group)
                groupList.removeAt(index)
                groupList.add(group)
                notifyItemChanged(index)
            } else {
                groupList.add(group)
                notifyItemInserted(itemCount - 1)
            }
        }
    }

    /**
     * This method is used to remove particular group from groupList in adapter.
     *
     * @param group is a object of Group which will be removed from groupList.
     *
     * @see Group
     */
    fun removeGroup(group: Group?) {
        if (group != null) {
            if (groupList.contains(group)) {
                val index = groupList.indexOf(group)
                groupList.remove(group)
                notifyItemRemoved(index)
            }
        }
    }

    override fun getItemCount(): Int {
        return groupList.size
    }

    /**
     * This method is used to set searchGroupList with a groupList in adapter.
     *
     * @param groups is a list of group which will be set with a groupList in adapter.
     */
    fun searchGroup(groups: List<Group?>?) {
        if (groups != null) {
            groupList.clear();
            updateGroupList(groups)
            notifyDataSetChanged()
        }
    }

    /**
     * This method is used to add particular group in groupList of adapter.
     *
     * @param group is a object of group which will be added in groupList.
     *
     * @see Group
     */
    fun add(group: Group?) {
        group?.let { updateGroup(it) }
    }

    fun clear() {
        groupList.clear()
        notifyDataSetChanged()
    }

    inner class GroupViewHolder(var groupListRowBinding: GroupListRowBinding) : RecyclerView.ViewHolder(groupListRowBinding.root)
}