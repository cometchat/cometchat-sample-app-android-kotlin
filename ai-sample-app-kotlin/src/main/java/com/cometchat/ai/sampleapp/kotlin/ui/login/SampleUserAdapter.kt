package com.cometchat.ai.sampleapp.kotlin.ui.login

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.cometchat.ai.sampleapp.kotlin.databinding.ItemSampleUserBinding
import com.cometchat.chat.models.User
import com.cometchat.uikit.kotlin.theme.CometChatTheme

/**
 * RecyclerView adapter for displaying sample users in a grid layout.
 */
class SampleUserAdapter(
    private val onUserClick: (User) -> Unit
) : ListAdapter<User, SampleUserAdapter.SampleUserViewHolder>(UserDiffCallback()) {

    private var selectedUserId: String? = null

    fun setSelectedUser(user: User?) {
        val previousSelectedId = selectedUserId
        selectedUserId = user?.uid

        previousSelectedId?.let { prevId ->
            val previousIndex = currentList.indexOfFirst { it.uid == prevId }
            if (previousIndex != -1) notifyItemChanged(previousIndex)
        }
        selectedUserId?.let { newId ->
            val newIndex = currentList.indexOfFirst { it.uid == newId }
            if (newIndex != -1) notifyItemChanged(newIndex)
        }
    }

    fun clearSelection() {
        val previousSelectedId = selectedUserId
        selectedUserId = null
        previousSelectedId?.let { prevId ->
            val previousIndex = currentList.indexOfFirst { it.uid == prevId }
            if (previousIndex != -1) notifyItemChanged(previousIndex)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SampleUserViewHolder {
        val binding = ItemSampleUserBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SampleUserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SampleUserViewHolder, position: Int) {
        val user = getItem(position)
        holder.bind(user, user.uid == selectedUserId)
    }

    inner class SampleUserViewHolder(
        private val binding: ItemSampleUserBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(user: User, isSelected: Boolean) {
            val context = binding.root.context

            binding.avatar.setAvatar(user.name ?: "", user.avatar)
            binding.tvUserName.text = user.name
            binding.tvUserUid.text = user.uid

            if (isSelected) {
                binding.cardUser.strokeColor = CometChatTheme.getStrokeColorHighlight(context)
                binding.cardUser.setCardBackgroundColor(
                    ColorStateList.valueOf(CometChatTheme.getExtendedPrimaryColor50(context))
                )
                binding.ivSelected.visibility = View.VISIBLE
            } else {
                binding.cardUser.strokeColor = CometChatTheme.getStrokeColorLight(context)
                binding.cardUser.setCardBackgroundColor(
                    ColorStateList.valueOf(CometChatTheme.getBackgroundColor1(context))
                )
                binding.ivSelected.visibility = View.GONE
            }

            binding.cardUser.setOnClickListener {
                onUserClick(user)
            }
        }
    }

    private class UserDiffCallback : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean =
            oldItem.uid == newItem.uid

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean =
            oldItem.uid == newItem.uid &&
                oldItem.name == newItem.name &&
                oldItem.avatar == newItem.avatar
    }
}
