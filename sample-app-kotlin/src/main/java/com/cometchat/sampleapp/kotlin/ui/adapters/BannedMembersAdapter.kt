package com.cometchat.sampleapp.kotlin.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.GroupMember
import com.cometchat.chatuikit.CometChatTheme
import com.cometchat.sampleapp.kotlin.databinding.BannedMemberRowBinding

/**
 * Adapter class for displaying a list of banned group members in a
 * RecyclerView.
 */
class BannedMembersAdapter(
    /**
     * Gets the context of the adapter.
     *
     * @return The context associated with the adapter.
     */
    val context: Context
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    // Return the context
    private var groupMemberList: List<GroupMember> // Return the group reference

    /**
     * Gets the group associated with the banned members.
     *
     * @return The group instance.
     */
    var group: Group? = null
        /**
         * Sets the group associated with the banned members.
         *
         * @param group
         * The group instance.
         */
        set(group) {
            field = group // Update the group reference
            notifyDataSetChanged() // Notify the adapter to refresh the views
        }

    /**
     * Constructor for BannedMembersAdapter.
     *
     * @param context
     * The context of the calling activity or fragment.
     */
    init {
        groupMemberList = ArrayList() // Initialize the list of group members
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): RecyclerView.ViewHolder {
        val binding = BannedMemberRowBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return MyViewHolder(binding.root)
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder, position: Int
    ) {
        val groupMember = groupMemberList[position] // Get the group member at the specified position
        (holder as MyViewHolder).bindView(groupMember) // Bind the group member data to the view holder
    }

    override fun getItemCount(): Int {
        return groupMemberList.size // Return the total number of banned members
    }

    /**
     * Gets the list of banned group members.
     *
     * @return The current list of group members.
     */
    fun getGroupMemberList(): List<GroupMember> {
        return groupMemberList // Return the list of group members
    }

    /**
     * Sets the list of banned group members.
     *
     * @param list
     * The list of group members to be displayed.
     */
    fun setGroupMemberList(list: List<GroupMember>?) {
        if (list != null) {
            this.groupMemberList = list // Update the group member list
            notifyDataSetChanged() // Notify the adapter to refresh the views
        }
    }

    /**
     * ViewHolder class for representing a single banned member in the RecyclerView.
     */
    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding: BannedMemberRowBinding =
            BannedMemberRowBinding.bind(itemView) // Binding for the banned member row layout // Bind the layout to this view holder

        /**
         * Binds the data of a group member to the views in the ViewHolder.
         *
         * @param groupMember
         * The group member data to bind.
         */
        fun bindView(groupMember: GroupMember) {
            binding.tvMemberTitle.setTextColor(CometChatTheme.getTextColorPrimary(itemView.context)) // Set the text color for the member's name
            binding.ivUnbanMember.setColorFilter(CometChatTheme.getIconTintSecondary(itemView.context)) // Set the color filter for the unban icon
            binding.memberAvatar.setAvatar(
                groupMember.name, groupMember.avatar
            ) // Set the member's avatar
            binding.tvMemberTitle.text = groupMember.name // Set the member's name
            itemView.setTag(com.cometchat.chatuikit.R.string.cometchat_member, groupMember) // Set a tag for the view
        }
    }
}
