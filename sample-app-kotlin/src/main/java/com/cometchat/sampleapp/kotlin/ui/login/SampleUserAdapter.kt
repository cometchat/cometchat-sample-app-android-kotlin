package com.cometchat.sampleapp.kotlin.ui.login

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.cometchat.chat.models.User
import com.cometchat.uikit.kotlin.theme.CometChatTheme
import com.cometchat.sampleapp.kotlin.databinding.ItemSampleUserBinding

/**
 * RecyclerView adapter for displaying sample users in a grid layout.
 * Handles selection state with visual feedback (border and background color changes).
 * Matches master-app-kotlin2 SampleUserAdapter pattern.
 *
 * Selection Visual Feedback:
 * - Selected: strokeColor = cometchatStrokeColorHighlight, backgroundColor = cometchatExtendedPrimaryColor50
 * - Unselected: strokeColor = cometchatStrokeColorLight, backgroundColor = cometchatBackgroundColor1
 */
class SampleUserAdapter(
    private val onUserClick: (User) -> Unit
) : ListAdapter<User, SampleUserAdapter.SampleUserViewHolder>(UserDiffCallback()) {

    private var selectedUserId: String? = null

    /**
     * Updates the selected user and refreshes the UI.
     * Uses targeted notifyItemChanged() for efficient updates.
     * @param user The newly selected user, or null to clear selection
     */
    fun setSelectedUser(user: User?) {
        val previousSelectedId = selectedUserId
        selectedUserId = user?.uid

        // Find and update the previously selected item
        if (previousSelectedId != null) {
            val previousIndex = currentList.indexOfFirst { it.uid == previousSelectedId }
            if (previousIndex != -1) {
                notifyItemChanged(previousIndex)
            }
        }

        // Find and update the newly selected item
        if (selectedUserId != null) {
            val newIndex = currentList.indexOfFirst { it.uid == selectedUserId }
            if (newIndex != -1) {
                notifyItemChanged(newIndex)
            }
        }
    }

    /**
     * Clears the current selection and refreshes the UI.
     * Matches master-app-kotlin SampleUsersAdapter.clearSelection() behavior.
     */
    fun clearSelection() {
        val previousSelectedId = selectedUserId
        selectedUserId = null

        // Find and update the previously selected item
        if (previousSelectedId != null) {
            val previousIndex = currentList.indexOfFirst { it.uid == previousSelectedId }
            if (previousIndex != -1) {
                notifyItemChanged(previousIndex)
            }
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

            // Set user data using CometChatAvatar
            binding.avatar.setAvatar(user.name ?: "", user.avatar)
            binding.tvUserName.text = user.name
            binding.tvUserUid.text = user.uid

            // Apply selection visual feedback
            if (isSelected) {
                // Selected state: highlight border and background
                binding.cardUser.strokeColor = CometChatTheme.getStrokeColorHighlight(context)
                binding.cardUser.setCardBackgroundColor(
                    ColorStateList.valueOf(CometChatTheme.getExtendedPrimaryColor50(context))
                )
                binding.ivSelected.visibility = View.VISIBLE
            } else {
                // Unselected state: default border and background
                binding.cardUser.strokeColor = CometChatTheme.getStrokeColorLight(context)
                binding.cardUser.setCardBackgroundColor(
                    ColorStateList.valueOf(CometChatTheme.getBackgroundColor1(context))
                )
                binding.ivSelected.visibility = View.GONE
            }

            // Handle click
            binding.cardUser.setOnClickListener {
                onUserClick(user)
            }
        }
    }

    /**
     * DiffUtil callback for efficient list updates.
     */
    private class UserDiffCallback : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.uid == newItem.uid
        }

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.uid == newItem.uid &&
                    oldItem.name == newItem.name &&
                    oldItem.avatar == newItem.avatar
        }
    }
}
