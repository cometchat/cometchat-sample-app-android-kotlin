package com.cometchat.ai.sampleapp.ui.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cometchat.ai.sampleapp.data.interfaces.OnSampleUserSelected
import com.cometchat.ai.sampleapp.databinding.SampleUserItemsBinding
import com.cometchat.chat.models.User
import com.cometchat.chatuikit.CometChatTheme

class SampleUsersAdapter(
    private val userList: MutableList<User>,
    private val onSampleUserSelected: OnSampleUserSelected
) : RecyclerView.Adapter<SampleUsersAdapter.UserViewHolder>() {
    private var selectedPosition = RecyclerView.NO_POSITION

    fun updateList(users: List<User>) {
        userList.clear()
        userList.addAll(users)
        notifyDataSetChanged()
    }

    fun clearSelection() {
        selectedPosition = RecyclerView.NO_POSITION
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): UserViewHolder {
        val binding = SampleUserItemsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding.root)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(
        holder: UserViewHolder,
        position: Int
    ) {
        val user = userList[position]
        holder.binding.avatar.setAvatar(user.name, user.avatar)
        holder.binding.tvUserName.text = user.name
        holder.binding.tvUserUid.text = user.uid

        if (position == selectedPosition) {
            holder.binding.ivSelected.visibility = View.VISIBLE
            holder.binding.cardUser.strokeColor = CometChatTheme.getStrokeColorHighlight(holder.itemView.context)
            holder.binding.cardUser.setCardBackgroundColor(
                CometChatTheme.getExtendedPrimaryColor50(
                    holder.itemView.context
                )
            )
        } else {
            holder.binding.ivSelected.visibility = View.GONE
            holder.binding.cardUser.strokeColor = CometChatTheme.getStrokeColorLight(holder.itemView.context)
            holder.binding.cardUser.setCardBackgroundColor(CometChatTheme.getBackgroundColor1(holder.itemView.context))
        }

        holder.itemView.setOnClickListener { v: View? ->
            onSampleUserSelected.onSelect(user)
            selectedPosition = if ((selectedPosition == position)) RecyclerView.NO_POSITION else position
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding: SampleUserItemsBinding = SampleUserItemsBinding.bind(itemView)
    }
}