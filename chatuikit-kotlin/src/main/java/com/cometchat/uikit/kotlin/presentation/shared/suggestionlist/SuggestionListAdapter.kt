package com.cometchat.uikit.kotlin.presentation.shared.suggestionlist

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.annotation.StyleRes
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.databinding.CometchatSuggestionListItemBinding
import com.cometchat.uikit.kotlin.shared.formatters.SuggestionItem

/**
 * Adapter for displaying suggestion items in a RecyclerView.
 * 
 * @param context The context for inflating views
 * @param viewHolderListener Optional listener for custom item views
 */
class SuggestionListAdapter(
    private val context: Context,
    private var viewHolderListener: SuggestionListViewHolderListener? = null
) : ListAdapter<SuggestionItem, SuggestionListAdapter.SuggestionViewHolder>(SuggestionDiffCallback()) {

    private var showAvatar: Boolean = true
    @StyleRes private var itemAvatarStyle: Int = 0
    @StyleRes private var itemTextAppearance: Int = 0
    @ColorInt private var itemTextColor: Int = 0
    @StyleRes private var itemInfoTextAppearance: Int = 0
    @ColorInt private var itemInfoTextColor: Int = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SuggestionViewHolder {
        val binding = CometchatSuggestionListItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return SuggestionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SuggestionViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    /**
     * Updates the list with new suggestion items.
     */
    fun updateList(items: List<SuggestionItem>) {
        android.util.Log.d("MentionDebug", "[SuggestionListAdapter] updateList() - submitting ${items.size} items: ${items.map { it.name }}")
        submitList(items.toList())
    }

    /**
     * Sets whether to show avatars in suggestion items.
     */
    fun showAvatar(show: Boolean) {
        showAvatar = show
        notifyDataSetChanged()
    }

    /**
     * Sets the avatar style for suggestion items.
     */
    fun setItemAvatarStyle(@StyleRes style: Int) {
        itemAvatarStyle = style
        notifyDataSetChanged()
    }

    /**
     * Sets the text appearance for suggestion item names.
     */
    fun setItemTextAppearance(@StyleRes style: Int) {
        itemTextAppearance = style
        notifyDataSetChanged()
    }

    /**
     * Sets the text color for suggestion item names.
     */
    fun setItemTextColor(@ColorInt color: Int) {
        itemTextColor = color
        notifyDataSetChanged()
    }

    /**
     * Sets the text appearance for suggestion item info text.
     */
    fun setItemInfoTextAppearance(@StyleRes style: Int) {
        itemInfoTextAppearance = style
        notifyDataSetChanged()
    }

    /**
     * Sets the text color for suggestion item info text.
     */
    fun setItemInfoTextColor(@ColorInt color: Int) {
        itemInfoTextColor = color
        notifyDataSetChanged()
    }

    /**
     * Sets the custom view holder listener for custom item views.
     */
    fun setViewHolderListener(listener: SuggestionListViewHolderListener?) {
        viewHolderListener = listener
        notifyDataSetChanged()
    }

    inner class SuggestionViewHolder(
        private val binding: CometchatSuggestionListItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SuggestionItem) {
            // Store item in tag for click handling
            binding.root.setTag(R.string.cometchat_tag_item, item)

            if (viewHolderListener != null) {
                // Use custom view if provided
                val customView = viewHolderListener?.createView(context, item)
                if (customView != null) {
                    binding.parentLayout.removeAllViews()
                    binding.parentLayout.addView(customView)
                    viewHolderListener?.bindView(context, customView, item, adapterPosition)
                    return
                }
            }

            // Default binding
            bindDefaultView(item)
        }

        private fun bindDefaultView(item: SuggestionItem) {
            // Avatar
            binding.suggestionItemAvatar.isVisible = showAvatar && !item.hideLeadingIcon
            if (showAvatar && !item.hideLeadingIcon) {
                binding.suggestionItemAvatar.setAvatar(item.name, item.leadingIconUrl)
                if (itemAvatarStyle != 0) {
                    binding.suggestionItemAvatar.setStyle(itemAvatarStyle)
                }
            }

            // Name
            binding.tvSuggestionItemName.text = item.name
            if (itemTextAppearance != 0) {
                binding.tvSuggestionItemName.setTextAppearance(itemTextAppearance)
            }
            if (itemTextColor != 0) {
                binding.tvSuggestionItemName.setTextColor(itemTextColor)
            }

            // Info text
            val infoText = item.data?.optString("infoText")
            if (!infoText.isNullOrEmpty()) {
                binding.tvSuggestionItemInfo.isVisible = true
                binding.tvSuggestionItemInfo.text = infoText
                if (itemInfoTextAppearance != 0) {
                    binding.tvSuggestionItemInfo.setTextAppearance(itemInfoTextAppearance)
                }
                if (itemInfoTextColor != 0) {
                    binding.tvSuggestionItemInfo.setTextColor(itemInfoTextColor)
                }
            } else {
                binding.tvSuggestionItemInfo.isVisible = false
            }
        }
    }

    /**
     * DiffUtil callback for efficient list updates.
     */
    private class SuggestionDiffCallback : DiffUtil.ItemCallback<SuggestionItem>() {
        override fun areItemsTheSame(oldItem: SuggestionItem, newItem: SuggestionItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: SuggestionItem, newItem: SuggestionItem): Boolean {
            return oldItem == newItem
        }
    }
}

/**
 * Interface for custom suggestion list item views.
 */
interface SuggestionListViewHolderListener {
    /**
     * Creates a custom view for a suggestion item.
     * 
     * @param context The context for creating views
     * @param item The suggestion item
     * @return A custom view, or null to use the default view
     */
    fun createView(context: Context, item: SuggestionItem): View?

    /**
     * Binds data to a custom view.
     * 
     * @param context The context
     * @param view The custom view
     * @param item The suggestion item
     * @param position The adapter position
     */
    fun bindView(context: Context, view: View, item: SuggestionItem, position: Int)
}
