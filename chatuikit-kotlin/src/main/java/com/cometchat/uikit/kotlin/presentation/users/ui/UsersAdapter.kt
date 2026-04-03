package com.cometchat.uikit.kotlin.presentation.users.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.annotation.ColorInt
import androidx.annotation.StyleRes
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.cometchat.chat.models.User
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.presentation.users.style.CometChatUsersListItemStyle
import com.cometchat.uikit.kotlin.presentation.users.utils.UsersDiffCallback
import com.cometchat.uikit.kotlin.presentation.users.utils.UsersViewHolderListener
import com.cometchat.uikit.kotlin.shared.resources.utils.sticky_header.StickyHeaderAdapter

/**
 * RecyclerView adapter for displaying user items.
 * 
 * This adapter uses CometChatUsersListItem as the row view and integrates
 * with UsersViewHolderListener for custom view callbacks following the
 * chatuikit Java pattern:
 * 
 * - createView() is called once during ViewHolder creation (onCreateViewHolder)
 * - bindView() is called during bind operations (onBindViewHolder) with user data
 * - Custom views replace default views when listeners are set
 * 
 * Implements DiffUtil for efficient list updates.
 * Supports custom item views and section views (leading, title, subtitle, trailing).
 * Implements StickyHeaderAdapter for sticky alphabetical headers.
 */
class UsersAdapter : RecyclerView.Adapter<UsersViewHolder>(), StickyHeaderAdapter<StickyViewHolder> {

    companion object {
        private val TAG = UsersAdapter::class.java.simpleName
    }

    // Current list of users
    private var users: List<User> = emptyList()

    // Selection state
    private var selectedUsers: Set<User> = emptySet()
    private var selectionEnabled: Boolean = false

    // Click listeners
    private var onItemClick: ((android.view.View, Int, User) -> Unit)? = null
    private var onItemLongClick: ((android.view.View, Int, User) -> Unit)? = null

    // Custom view listeners
    private var itemViewListener: UsersViewHolderListener? = null
    private var leadingViewListener: UsersViewHolderListener? = null
    private var titleViewListener: UsersViewHolderListener? = null
    private var subtitleViewListener: UsersViewHolderListener? = null
    private var trailingViewListener: UsersViewHolderListener? = null

    // Style
    private var itemStyle: CometChatUsersListItemStyle = CometChatUsersListItemStyle()

    // Visibility controls
    private var hideUserStatus: Boolean = false
    private var hideSeparator: Boolean = false

    // Sticky header styling
    @ColorInt private var stickyTitleColor: Int = 0
    @StyleRes private var stickyTitleAppearance: Int = 0
    @ColorInt private var stickyTitleBackgroundColor: Int = 0

    /**
     * Updates the user list using DiffUtil for efficient updates.
     */
    fun setList(newList: List<User>) {
        val diffCallback = UsersDiffCallback(users, newList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        users = newList
        diffResult.dispatchUpdatesTo(this)
    }

    /**
     * Gets the current list of users.
     */
    fun getList(): List<User> = users

    /**
     * Updates selection state for users.
     * Note: This method only updates the selected users set. The selectionEnabled flag
     * is controlled separately by setSelectionEnabled() to ensure checkbox visibility
     * is not affected by the selection state.
     */
    fun selectUsers(selected: Set<User>) {
        selectedUsers = selected
        // selectionEnabled is NOT modified here - it's controlled by setSelectionEnabled()
        notifyDataSetChanged()
    }

    /**
     * Sets the item click listener.
     */
    fun setOnItemClick(listener: (android.view.View, Int, User) -> Unit) {
        onItemClick = listener
    }

    /**
     * Sets the item long click listener.
     */
    fun setOnLongClick(listener: (android.view.View, Int, User) -> Unit) {
        onItemLongClick = listener
    }

    /**
     * Sets custom item view listener for replacing entire item.
     */
    fun setItemView(listener: UsersViewHolderListener?) {
        itemViewListener = listener
        notifyDataSetChanged()
    }

    /**
     * Sets custom leading view listener.
     */
    fun setLeadingView(listener: UsersViewHolderListener?) {
        leadingViewListener = listener
        notifyDataSetChanged()
    }

    /**
     * Sets custom title view listener.
     */
    fun setTitleView(listener: UsersViewHolderListener?) {
        titleViewListener = listener
        notifyDataSetChanged()
    }

    /**
     * Sets custom subtitle view listener.
     */
    fun setSubtitleView(listener: UsersViewHolderListener?) {
        subtitleViewListener = listener
        notifyDataSetChanged()
    }

    /**
     * Sets custom trailing view listener.
     */
    fun setTrailingView(listener: UsersViewHolderListener?) {
        trailingViewListener = listener
        notifyDataSetChanged()
    }

    /**
     * Sets the item style.
     */
    fun setItemStyle(style: CometChatUsersListItemStyle) {
        itemStyle = style
        notifyDataSetChanged()
    }

    /**
     * Sets whether to hide user status indicator.
     */
    fun setHideUserStatus(hide: Boolean) {
        hideUserStatus = hide
        notifyDataSetChanged()
    }

    /**
     * Sets whether to hide item separator.
     */
    fun setHideSeparator(hide: Boolean) {
        hideSeparator = hide
        notifyDataSetChanged()
    }

    /**
     * Enables or disables selection mode UI (checkboxes) on all items.
     */
    fun setSelectionEnabled(enabled: Boolean) {
        selectionEnabled = enabled
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsersViewHolder {
        val holder = UsersViewHolder.create(parent)

        // Call createView() on all non-null listeners
        holder.createCustomViews(
            itemViewListener,
            leadingViewListener,
            titleViewListener,
            subtitleViewListener,
            trailingViewListener
        )

        return holder
    }

    override fun onBindViewHolder(holder: UsersViewHolder, position: Int) {
        val user = users[position]
        val isSelected = selectedUsers.any { it.uid == user.uid }

        // Update custom views if listeners have changed
        holder.createCustomViews(
            itemViewListener,
            leadingViewListener,
            titleViewListener,
            subtitleViewListener,
            trailingViewListener
        )

        // Bind data to the ViewHolder
        holder.bind(
            user = user,
            userList = users,
            position = position,
            isSelected = isSelected,
            selectionEnabled = selectionEnabled || selectedUsers.isNotEmpty(),
            hideUserStatus = hideUserStatus,
            hideSeparator = hideSeparator || position == users.lastIndex,
            style = itemStyle,
            itemViewListener = itemViewListener,
            leadingViewListener = leadingViewListener,
            titleViewListener = titleViewListener,
            subtitleViewListener = subtitleViewListener,
            trailingViewListener = trailingViewListener
        )

        // Setup click listeners
        holder.usersListItem.setOnItemClick { u ->
            onItemClick?.invoke(holder.itemView, position, u)
        }

        holder.usersListItem.setOnItemLongClick { u ->
            onItemLongClick?.invoke(holder.itemView, position, u)
        }
    }

    override fun getItemCount(): Int = users.size

    // ==================== StickyHeaderAdapter Implementation ====================

    override fun getHeaderId(position: Int): Long {
        if (position < 0 || position >= users.size) {
            return StickyHeaderAdapter.NO_HEADER_ID
        }

        val user = users[position]
        val name = user.name

        if (name.isNullOrEmpty()) {
            return '#'.code.toLong()
        }

        val firstChar = name[0].uppercaseChar()
        return if (firstChar.isLetter()) {
            firstChar.code.toLong()
        } else {
            '#'.code.toLong()
        }
    }

    override fun onCreateHeaderViewHolder(parent: ViewGroup): StickyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.cometchat_user_list_sticky_header, parent, false)
        return StickyViewHolder(view)
    }

    override fun onBindHeaderViewHolder(holder: StickyViewHolder, position: Int, headerId: Long) {
        val user = users[position]
        val name = user.name

        val headerChar = if (name.isNullOrEmpty()) {
            '#'
        } else {
            val firstChar = name[0].uppercaseChar()
            if (firstChar.isLetter()) firstChar else '#'
        }

        holder.binding.tvTitle.text = headerChar.toString()

        // Apply styling
        holder.binding.tvTitle.setTextColor(stickyTitleColor)
        if (stickyTitleAppearance != 0) {
            holder.binding.tvTitle.setTextAppearance(stickyTitleAppearance)
        }
        holder.binding.stickyView.setBackgroundColor(stickyTitleBackgroundColor)

        // Adjust margin based on selection mode
        val layoutParams = holder.binding.tvTitle.layoutParams as? RelativeLayout.LayoutParams
        layoutParams?.let { params ->
            val context = holder.itemView.context
            val marginStart = if (selectionEnabled) {
                context.resources.getDimensionPixelSize(R.dimen.cometchat_padding_5)
            } else {
                context.resources.getDimensionPixelSize(R.dimen.cometchat_padding_4)
            }
            params.marginStart = marginStart
            holder.binding.tvTitle.layoutParams = params
        }
    }

    // ==================== Sticky Header Styling Setters ====================

    /**
     * Sets the sticky header title text color.
     */
    fun setStickyTitleColor(@ColorInt color: Int) {
        stickyTitleColor = color
    }

    /**
     * Sets the sticky header title text appearance.
     */
    fun setStickyTitleAppearance(@StyleRes appearance: Int) {
        stickyTitleAppearance = appearance
    }

    /**
     * Sets the sticky header background color.
     */
    fun setStickyTitleBackgroundColor(@ColorInt color: Int) {
        stickyTitleBackgroundColor = color
    }

    /**
     * Gets the current selection enabled state.
     */
    fun isSelectionEnabled(): Boolean = selectionEnabled
}
