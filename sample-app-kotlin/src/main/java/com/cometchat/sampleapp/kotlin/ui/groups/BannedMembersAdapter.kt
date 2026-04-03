package com.cometchat.sampleapp.kotlin.ui.groups

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cometchat.chat.models.GroupMember
import com.cometchat.sampleapp.kotlin.R
import com.cometchat.uikit.kotlin.presentation.shared.baseelements.avatar.CometChatAvatar

/**
 * Adapter for displaying banned members in a RecyclerView.
 *
 * @param members The list of banned group members
 * @param onUnbanClick Callback when unban button is clicked
 */
class BannedMembersAdapter(
    private val members: MutableList<GroupMember>,
    private val onUnbanClick: (GroupMember) -> Unit
) : RecyclerView.Adapter<BannedMembersAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_banned_member, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val member = members[position]
        holder.bind(member)
    }

    override fun getItemCount(): Int = members.size

    /**
     * Removes a member from the list.
     */
    fun removeMember(member: GroupMember) {
        val index = members.indexOfFirst { it.uid == member.uid }
        if (index != -1) {
            members.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val avatar: CometChatAvatar = itemView.findViewById(R.id.avatar)
        private val name: TextView = itemView.findViewById(R.id.tv_name)
        private val unbanButton: ImageView = itemView.findViewById(R.id.iv_unban)

        fun bind(member: GroupMember) {
            avatar.setAvatar(member.name, member.avatar)
            name.text = member.name
            
            unbanButton.setOnClickListener {
                onUnbanClick(member)
            }
        }
    }
}
