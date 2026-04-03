package com.cometchat.uikit.kotlin.presentation.reactionlist.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.StyleRes
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Reaction
import com.cometchat.uikit.core.CometChatUIKit
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.databinding.CometchatReactedUserItemBinding
import com.cometchat.uikit.kotlin.presentation.reactionlist.style.CometChatReactionListItemStyle
import com.cometchat.uikit.kotlin.presentation.reactionlist.utils.ReactionListViewHolderListener

/**
 * Adapter class for displaying reacted users in a vertical RecyclerView.
 * 
 * This adapter handles the display and interaction of users who reacted to a message:
 * - Shows user avatar, name, and the emoji they reacted with
 * - Displays "You" and "Tap to remove" for the logged-in user's reactions
 * - Supports click handling for reaction removal
 * - Supports custom views via ReactionListViewHolderListener
 * 
 * The adapter follows the pattern from the Java implementation in
 * chatuikit/src/main/java/com/cometchat/chatuikit/reactionlist/adapter/ReactedUsersAdapter.java
 */
class ReactedUsersAdapter(
    private val context: Context
) : RecyclerView.Adapter<ReactedUsersAdapter.ReactedUserViewHolder>() {

    /**
     * Callback interface for item click events.
     */
    fun interface OnItemClickListener {
        /**
         * Called when a reaction item is clicked.
         * 
         * @param reaction The Reaction object that was clicked
         * @param baseMessage The BaseMessage associated with the reaction
         */
        fun onItemClick(reaction: Reaction, baseMessage: BaseMessage?)
    }

    /**
     * Internal callback interface for adapter events.
     */
    fun interface AdapterEventListener {
        /**
         * Called when an item in the list is clicked.
         * 
         * @param baseMessage The base message associated with the reaction
         * @param reactionList The list of reactions
         * @param position The position of the clicked item
         */
        fun itemClicked(baseMessage: BaseMessage?, reactionList: List<Reaction>, position: Int)
    }

    // Data
    private val reactions = mutableListOf<Reaction>()
    private var baseMessage: BaseMessage? = null

    // Callbacks
    private var onItemClickListener: OnItemClickListener? = null
    private var adapterEventListener: AdapterEventListener? = null

    // Custom view listeners
    private var itemViewListener: ReactionListViewHolderListener? = null
    private var leadingViewListener: ReactionListViewHolderListener? = null
    private var titleViewListener: ReactionListViewHolderListener? = null
    private var subtitleViewListener: ReactionListViewHolderListener? = null
    private var trailingViewListener: ReactionListViewHolderListener? = null

    // Styling
    @StyleRes
    private var avatarStyle: Int = 0

    @StyleRes
    private var titleTextAppearance: Int = 0

    @ColorInt
    private var titleTextColor: Int = 0

    @StyleRes
    private var subtitleTextAppearance: Int = 0

    @ColorInt
    private var subtitleTextColor: Int = 0

    @StyleRes
    private var tailViewTextAppearance: Int = 0

    @ColorInt
    private var tailViewTextColor: Int = 0

    // Separator styling
    @ColorInt
    private var separatorColor: Int = 0
    
    private var separatorHeight: Int = 0
    
    private var hideSeparator: Boolean = true

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReactedUserViewHolder {
        val binding = CometchatReactedUserItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ReactedUserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReactedUserViewHolder, position: Int) {
        if (position == RecyclerView.NO_POSITION || position >= reactions.size) return
        
        val reaction = reactions[position]
        
        // Always update custom views to ensure they're properly created/removed
        holder.updateCustomViews(
            itemViewListener,
            leadingViewListener,
            titleViewListener,
            subtitleViewListener,
            trailingViewListener
        )
        
        // Handle custom item view (full replacement)
        if (itemViewListener != null && holder.customItemView != null) {
            itemViewListener?.bindView(context, holder.customItemView!!, reaction, reactions.toList(), position)
            setupClickListener(holder, position)
            return
        }
        
        val reactedBy = reaction.reactedBy
        
        // Handle custom leading view or default avatar
        if (leadingViewListener != null && holder.customLeadingView != null) {
            leadingViewListener?.bindView(context, holder.customLeadingView!!, reaction, reactions.toList(), position)
        } else {
            // Set avatar (default behavior)
            if (avatarStyle != 0) {
                holder.binding.avatar.setStyle(avatarStyle)
            }
            holder.binding.avatar.setAvatar(
                reactedBy?.name ?: "",
                reactedBy?.avatar
            )
            holder.binding.avatar.visibility = View.VISIBLE
        }
        
        // Check if this is the logged-in user
        val loggedInUserId = getLoggedInUserSafe()?.uid
        val isCurrentUser = reaction.uid == loggedInUserId
        
        // Handle custom title view or default title
        if (titleViewListener != null && holder.customTitleView != null) {
            titleViewListener?.bindView(context, holder.customTitleView!!, reaction, reactions.toList(), position)
        } else {
            // Set title (user name or "You") - default behavior
            holder.binding.tvTitle.apply {
                visibility = View.VISIBLE
                text = if (isCurrentUser) {
                    context.getString(R.string.cometchat_you)
                } else {
                    reactedBy?.name ?: ""
                }
                if (titleTextAppearance != 0) {
                    setTextAppearance(titleTextAppearance)
                }
                if (titleTextColor != 0) {
                    setTextColor(titleTextColor)
                }
            }
        }
        
        // Handle custom subtitle view or default subtitle
        if (subtitleViewListener != null && holder.customSubtitleView != null) {
            subtitleViewListener?.bindView(context, holder.customSubtitleView!!, reaction, reactions.toList(), position)
        } else {
            // Set subtitle ("Tap to remove" for logged-in user, hidden for others) - default behavior
            holder.binding.tvSubtitle.apply {
                if (isCurrentUser) {
                    visibility = View.VISIBLE
                    text = context.getString(R.string.cometchat_tap_to_remove)
                    if (subtitleTextAppearance != 0) {
                        setTextAppearance(subtitleTextAppearance)
                    }
                    if (subtitleTextColor != 0) {
                        setTextColor(subtitleTextColor)
                    }
                } else {
                    visibility = View.GONE
                }
            }
        }
        
        // Handle custom trailing view or default tail view (emoji)
        if (trailingViewListener != null && holder.customTrailingView != null) {
            trailingViewListener?.bindView(context, holder.customTrailingView!!, reaction, reactions.toList(), position)
        } else {
            // Set tail view (emoji) - default behavior
            holder.binding.tailView.removeAllViews()
            val reactionText = TextView(context).apply {
                text = reaction.reaction
                if (tailViewTextAppearance != 0) {
                    setTextAppearance(tailViewTextAppearance)
                }
                if (tailViewTextColor != 0) {
                    setTextColor(tailViewTextColor)
                }
            }
            holder.binding.tailView.addView(reactionText)
            holder.binding.tailView.visibility = View.VISIBLE
        }
        
        setupClickListener(holder, position)
    }
    
    private fun setupClickListener(holder: ReactedUserViewHolder, position: Int) {
        // Handle click
        holder.binding.parentLayout.setOnClickListener {
            @Suppress("DEPRECATION")
            val clickedPosition = holder.adapterPosition
            if (clickedPosition != RecyclerView.NO_POSITION && clickedPosition < reactions.size) {
                adapterEventListener?.itemClicked(baseMessage, reactions, clickedPosition)
                onItemClickListener?.onItemClick(reactions[clickedPosition], baseMessage)
            }
        }
        
        // Handle separator visibility and styling
        // When hideSeparator is true, separator is hidden (GONE)
        // When hideSeparator is false, separator is visible (VISIBLE)
        holder.binding.itemSeparator.apply {
            visibility = if (hideSeparator) View.GONE else View.VISIBLE
            if (!hideSeparator) {
                if (separatorColor != 0) {
                    setBackgroundColor(separatorColor)
                }
                if (separatorHeight > 0) {
                    layoutParams = layoutParams.apply {
                        height = separatorHeight
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int = reactions.size

    /**
     * Updates the list of reactions using DiffUtil for efficient updates.
     * 
     * @param newReactions The new list of Reaction objects to display
     */
    fun setReactions(newReactions: List<Reaction>) {
        val diffCallback = ReactionDiffCallback(reactions, newReactions)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        
        reactions.clear()
        reactions.addAll(newReactions)
        diffResult.dispatchUpdatesTo(this)
    }

    /**
     * Gets the current list of reactions.
     * 
     * @return The current list of Reaction objects
     */
    fun getReactions(): List<Reaction> = reactions.toList()

    /**
     * Sets the base message associated with the reactions.
     * 
     * @param message The BaseMessage to associate with reactions
     */
    fun setBaseMessage(message: BaseMessage?) {
        this.baseMessage = message
    }

    /**
     * Gets the base message associated with the reactions.
     * 
     * @return The BaseMessage associated with reactions
     */
    fun getBaseMessage(): BaseMessage? = baseMessage

    /**
     * Sets the item click listener.
     * 
     * @param listener The listener to invoke when an item is clicked
     */
    fun setOnItemClick(listener: OnItemClickListener?) {
        this.onItemClickListener = listener
    }

    /**
     * Gets the item click listener.
     * 
     * @return The current item click listener
     */
    fun getOnItemClick(): OnItemClickListener? = onItemClickListener

    /**
     * Sets the adapter event listener.
     * 
     * @param listener The listener for adapter events
     */
    fun setAdapterEventListener(listener: AdapterEventListener?) {
        this.adapterEventListener = listener
    }

    /**
     * Gets the adapter event listener.
     * 
     * @return The current adapter event listener
     */
    fun getAdapterEventListener(): AdapterEventListener? = adapterEventListener

    // ==================== Custom View Setters ====================

    /**
     * Sets custom item view listener for replacing entire item.
     * 
     * @param listener The listener for custom item view
     */
    fun setItemView(listener: ReactionListViewHolderListener?) {
        itemViewListener = listener
        notifyDataSetChanged()
    }

    /**
     * Gets the custom item view listener.
     * 
     * @return The current item view listener
     */
    fun getItemView(): ReactionListViewHolderListener? = itemViewListener

    /**
     * Sets custom leading view listener (avatar area).
     * 
     * @param listener The listener for custom leading view
     */
    fun setLeadingView(listener: ReactionListViewHolderListener?) {
        leadingViewListener = listener
        notifyDataSetChanged()
    }

    /**
     * Gets the custom leading view listener.
     * 
     * @return The current leading view listener
     */
    fun getLeadingView(): ReactionListViewHolderListener? = leadingViewListener

    /**
     * Sets custom title view listener.
     * 
     * @param listener The listener for custom title view
     */
    fun setTitleView(listener: ReactionListViewHolderListener?) {
        titleViewListener = listener
        notifyDataSetChanged()
    }

    /**
     * Gets the custom title view listener.
     * 
     * @return The current title view listener
     */
    fun getTitleView(): ReactionListViewHolderListener? = titleViewListener

    /**
     * Sets custom subtitle view listener.
     * 
     * @param listener The listener for custom subtitle view
     */
    fun setSubtitleView(listener: ReactionListViewHolderListener?) {
        subtitleViewListener = listener
        notifyDataSetChanged()
    }

    /**
     * Gets the custom subtitle view listener.
     * 
     * @return The current subtitle view listener
     */
    fun getSubtitleView(): ReactionListViewHolderListener? = subtitleViewListener

    /**
     * Sets custom trailing view listener (emoji area).
     * 
     * @param listener The listener for custom trailing view
     */
    fun setTrailingView(listener: ReactionListViewHolderListener?) {
        trailingViewListener = listener
        notifyDataSetChanged()
    }

    /**
     * Gets the custom trailing view listener.
     * 
     * @return The current trailing view listener
     */
    fun getTrailingView(): ReactionListViewHolderListener? = trailingViewListener

    /**
     * Applies style from CometChatReactionListItemStyle.
     * 
     * @param style The style to apply
     */
    fun applyStyle(style: CometChatReactionListItemStyle) {
        this.avatarStyle = style.avatarStyleResId
        this.titleTextAppearance = style.titleTextAppearance
        this.titleTextColor = style.titleTextColor
        this.subtitleTextAppearance = style.subtitleTextAppearance
        this.subtitleTextColor = style.subtitleTextColor
        this.tailViewTextAppearance = style.tailViewTextAppearance
        this.tailViewTextColor = style.tailViewTextColor
        this.separatorColor = style.separatorColor
        this.separatorHeight = style.separatorHeight
        notifyDataSetChanged()
    }

    // Individual style setters

    /**
     * Sets the avatar style resource.
     * 
     * @param style The avatar style resource ID
     */
    fun setAvatarStyle(@StyleRes style: Int) {
        this.avatarStyle = style
        notifyDataSetChanged()
    }

    /**
     * Gets the avatar style resource.
     * 
     * @return The avatar style resource ID
     */
    @StyleRes
    fun getAvatarStyle(): Int = avatarStyle

    /**
     * Sets the title text appearance.
     * 
     * @param appearance The text appearance resource ID
     */
    fun setTitleTextAppearance(@StyleRes appearance: Int) {
        this.titleTextAppearance = appearance
        notifyDataSetChanged()
    }

    /**
     * Gets the title text appearance.
     * 
     * @return The text appearance resource ID
     */
    @StyleRes
    fun getTitleTextAppearance(): Int = titleTextAppearance

    /**
     * Sets the title text color.
     * 
     * @param color The color to use for title text
     */
    fun setTitleTextColor(@ColorInt color: Int) {
        this.titleTextColor = color
        notifyDataSetChanged()
    }

    /**
     * Gets the title text color.
     * 
     * @return The color used for title text
     */
    @ColorInt
    fun getTitleTextColor(): Int = titleTextColor

    /**
     * Sets the subtitle text appearance.
     * 
     * @param appearance The text appearance resource ID
     */
    fun setSubtitleTextAppearance(@StyleRes appearance: Int) {
        this.subtitleTextAppearance = appearance
        notifyDataSetChanged()
    }

    /**
     * Gets the subtitle text appearance.
     * 
     * @return The text appearance resource ID
     */
    @StyleRes
    fun getSubtitleTextAppearance(): Int = subtitleTextAppearance

    /**
     * Sets the subtitle text color.
     * 
     * @param color The color to use for subtitle text
     */
    fun setSubtitleTextColor(@ColorInt color: Int) {
        this.subtitleTextColor = color
        notifyDataSetChanged()
    }

    /**
     * Gets the subtitle text color.
     * 
     * @return The color used for subtitle text
     */
    @ColorInt
    fun getSubtitleTextColor(): Int = subtitleTextColor

    /**
     * Sets the tail view text appearance.
     * 
     * @param appearance The text appearance resource ID
     */
    fun setTailViewTextAppearance(@StyleRes appearance: Int) {
        this.tailViewTextAppearance = appearance
        notifyDataSetChanged()
    }

    /**
     * Gets the tail view text appearance.
     * 
     * @return The text appearance resource ID
     */
    @StyleRes
    fun getTailViewTextAppearance(): Int = tailViewTextAppearance

    /**
     * Sets the tail view text color.
     * 
     * @param color The color to use for tail view text
     */
    fun setTailViewTextColor(@ColorInt color: Int) {
        this.tailViewTextColor = color
        notifyDataSetChanged()
    }

    /**
     * Gets the tail view text color.
     * 
     * @return The color used for tail view text
     */
    @ColorInt
    fun getTailViewTextColor(): Int = tailViewTextColor

    /**
     * Sets whether to hide item separator.
     * 
     * @param hide True to hide the separator
     */
    fun setHideSeparator(hide: Boolean) {
        this.hideSeparator = hide
        notifyDataSetChanged()
    }

    /**
     * Gets whether the separator is hidden.
     * 
     * @return True if separator is hidden
     */
    fun isHideSeparator(): Boolean = hideSeparator

    /**
     * Sets the separator color.
     * 
     * @param color The color for the separator
     */
    fun setSeparatorColor(@ColorInt color: Int) {
        this.separatorColor = color
        notifyDataSetChanged()
    }

    /**
     * Gets the separator color.
     * 
     * @return The separator color
     */
    @ColorInt
    fun getSeparatorColor(): Int = separatorColor

    /**
     * Sets the separator height.
     * 
     * @param height The height in pixels
     */
    fun setSeparatorHeight(height: Int) {
        this.separatorHeight = height
        notifyDataSetChanged()
    }

    /**
     * Gets the separator height.
     * 
     * @return The separator height in pixels
     */
    fun getSeparatorHeight(): Int = separatorHeight

    /**
     * Safely gets the logged-in user, returning null if SDK is not initialized.
     */
    private fun getLoggedInUserSafe(): com.cometchat.chat.models.User? {
        return try {
            CometChatUIKit.getLoggedInUser()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * ViewHolder for reacted user items.
     * 
     * Uses ViewBinding for the cometchat_reacted_user_item layout.
     * Supports custom views via ReactionListViewHolderListener.
     */
    class ReactedUserViewHolder(
        val binding: CometchatReactedUserItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        private val context: Context = binding.root.context
        
        // Custom views created by listeners
        var customItemView: View? = null
            private set
        var customLeadingView: View? = null
            private set
        var customTitleView: View? = null
            private set
        var customSubtitleView: View? = null
            private set
        var customTrailingView: View? = null
            private set
        
        // Track which listeners were used to create custom views
        private var currentItemViewListener: ReactionListViewHolderListener? = null
        private var currentLeadingViewListener: ReactionListViewHolderListener? = null
        private var currentTitleViewListener: ReactionListViewHolderListener? = null
        private var currentSubtitleViewListener: ReactionListViewHolderListener? = null
        private var currentTrailingViewListener: ReactionListViewHolderListener? = null
        
        /**
         * Updates custom views based on the provided listeners.
         * Creates new views when listeners change, removes views when listeners are null.
         */
        fun updateCustomViews(
            itemViewListener: ReactionListViewHolderListener?,
            leadingViewListener: ReactionListViewHolderListener?,
            titleViewListener: ReactionListViewHolderListener?,
            subtitleViewListener: ReactionListViewHolderListener?,
            trailingViewListener: ReactionListViewHolderListener?
        ) {
            // Handle item view (replaces entire item)
            if (itemViewListener !== currentItemViewListener) {
                currentItemViewListener = itemViewListener
                if (itemViewListener != null) {
                    // Create custom item view and replace entire content
                    customItemView = itemViewListener.createView(context, null)
                    binding.contentLayout.visibility = View.GONE
                    binding.itemSeparator.visibility = View.GONE
                    
                    // Remove old custom view if exists
                    if (binding.parentLayout.childCount > 2) {
                        binding.parentLayout.removeViewAt(0)
                    }
                    binding.parentLayout.addView(customItemView, 0)
                } else {
                    // Remove custom item view and restore default layout
                    customItemView?.let { view ->
                        binding.parentLayout.removeView(view)
                    }
                    customItemView = null
                    binding.contentLayout.visibility = View.VISIBLE
                }
            }
            
            // Only handle section views if no full item replacement
            if (itemViewListener != null) return
            
            // Handle leading view (avatar area)
            if (leadingViewListener !== currentLeadingViewListener) {
                currentLeadingViewListener = leadingViewListener
                binding.leadingViewContainer.removeAllViews()
                if (leadingViewListener != null) {
                    customLeadingView = leadingViewListener.createView(context, null)
                    binding.avatar.visibility = View.GONE
                    binding.leadingViewContainer.addView(customLeadingView)
                    binding.leadingViewContainer.visibility = View.VISIBLE
                } else {
                    customLeadingView = null
                    binding.avatar.visibility = View.VISIBLE
                    binding.leadingViewContainer.visibility = View.GONE
                }
            }
            
            // Handle title view
            if (titleViewListener !== currentTitleViewListener) {
                currentTitleViewListener = titleViewListener
                binding.titleViewContainer.removeAllViews()
                if (titleViewListener != null) {
                    customTitleView = titleViewListener.createView(context, null)
                    binding.tvTitle.visibility = View.GONE
                    binding.titleViewContainer.addView(customTitleView)
                    binding.titleViewContainer.visibility = View.VISIBLE
                } else {
                    customTitleView = null
                    binding.tvTitle.visibility = View.VISIBLE
                    binding.titleViewContainer.visibility = View.GONE
                }
            }
            
            // Handle subtitle view
            if (subtitleViewListener !== currentSubtitleViewListener) {
                currentSubtitleViewListener = subtitleViewListener
                binding.subtitleViewContainer.removeAllViews()
                if (subtitleViewListener != null) {
                    customSubtitleView = subtitleViewListener.createView(context, null)
                    binding.tvSubtitle.visibility = View.GONE
                    binding.subtitleViewContainer.addView(customSubtitleView)
                    binding.subtitleViewContainer.visibility = View.VISIBLE
                } else {
                    customSubtitleView = null
                    // Subtitle visibility is controlled by bind logic
                    binding.subtitleViewContainer.visibility = View.GONE
                }
            }
            
            // Handle trailing view (emoji area)
            if (trailingViewListener !== currentTrailingViewListener) {
                currentTrailingViewListener = trailingViewListener
                binding.trailingViewContainer.removeAllViews()
                if (trailingViewListener != null) {
                    customTrailingView = trailingViewListener.createView(context, null)
                    binding.tailView.visibility = View.GONE
                    binding.trailingViewContainer.addView(customTrailingView)
                    binding.trailingViewContainer.visibility = View.VISIBLE
                } else {
                    customTrailingView = null
                    binding.tailView.visibility = View.VISIBLE
                    binding.trailingViewContainer.visibility = View.GONE
                }
            }
        }
    }

    /**
     * DiffUtil callback for efficient list updates.
     */
    private class ReactionDiffCallback(
        private val oldList: List<Reaction>,
        private val newList: List<Reaction>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = oldList[oldItemPosition]
            val newItem = newList[newItemPosition]
            // Items are the same if they have the same user ID and reaction
            return oldItem.uid == newItem.uid && oldItem.reaction == newItem.reaction
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = oldList[oldItemPosition]
            val newItem = newList[newItemPosition]
            // Contents are the same if all relevant fields match
            return oldItem.uid == newItem.uid &&
                    oldItem.reaction == newItem.reaction &&
                    oldItem.reactedBy?.name == newItem.reactedBy?.name &&
                    oldItem.reactedBy?.avatar == newItem.reactedBy?.avatar
        }
    }
}
