package com.cometchat.uikit.kotlin.presentation.groups.adapter

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.cometchat.chat.models.Group
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.kotlin.presentation.groups.style.CometChatGroupsItemStyle
import com.cometchat.uikit.kotlin.presentation.groups.utils.GroupsViewHolderListener

/**
 * RecyclerView adapter for displaying a list of groups.
 * Supports custom view slots, selection modes, and styling.
 * 
 * This adapter uses GroupsViewHolder which integrates with GroupsViewHolderListener
 * for custom view callbacks following the chatuikit Java pattern:
 * 
 * - createView() is called once during ViewHolder creation (onCreateViewHolder)
 * - bindView() is called during bind operations (onBindViewHolder) with group data
 * - Custom views replace default views when listeners are set
 * 
 * Implements DiffUtil for efficient list updates.
 */
class GroupsAdapter : RecyclerView.Adapter<GroupsViewHolder>() {

    private var groupList: List<Group> = emptyList()
    private var selectedGroups: Set<Group> = emptySet()
    private var selectionMode: UIKitConstants.SelectionMode = UIKitConstants.SelectionMode.NONE

    // View holder listeners for custom views
    private var itemViewListener: GroupsViewHolderListener? = null
    private var leadingViewListener: GroupsViewHolderListener? = null
    private var titleViewListener: GroupsViewHolderListener? = null
    private var subtitleViewListener: GroupsViewHolderListener? = null
    private var trailingViewListener: GroupsViewHolderListener? = null

    // Click listeners
    private var onItemClick: ((View, Int, Group) -> Unit)? = null
    private var onItemLongClick: ((View, Int, Group) -> Unit)? = null

    // Style
    private var itemStyle: CometChatGroupsItemStyle = CometChatGroupsItemStyle()

    // Visibility
    private var hideGroupType: Boolean = false
    private var hideSeparator: Boolean = false

    /**
     * Sets the list of groups to display.
     */
    fun setGroupList(groups: List<Group>) {
        val diffCallback = GroupsDiffCallback(groupList, groups)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        this.groupList = groups
        diffResult.dispatchUpdatesTo(this)
    }

    /**
     * Sets the selected groups.
     */
    fun setSelectedGroups(selected: Set<Group>) {
        this.selectedGroups = selected
        notifyDataSetChanged()
    }

    /**
     * Sets the selection mode.
     */
    fun setSelectionMode(mode: UIKitConstants.SelectionMode) {
        this.selectionMode = mode
        notifyDataSetChanged()
    }

    /**
     * Sets the item style.
     * Uses notifyItemRangeChanged for smoother updates that preserve scroll position.
     */
    fun setItemStyle(style: CometChatGroupsItemStyle) {
        this.itemStyle = style
        // Use notifyItemRangeChanged instead of notifyDataSetChanged to preserve scroll position
        if (groupList.isNotEmpty()) {
            notifyItemRangeChanged(0, groupList.size)
        }
    }

    /**
     * Sets whether to hide the group type indicator.
     */
    fun setHideGroupType(hide: Boolean) {
        this.hideGroupType = hide
        notifyDataSetChanged()
    }

    /**
     * Sets whether to hide the separator.
     */
    fun setHideSeparator(hide: Boolean) {
        this.hideSeparator = hide
        notifyDataSetChanged()
    }

    /**
     * Sets the item click listener.
     */
    fun setOnItemClick(listener: (View, Int, Group) -> Unit) {
        this.onItemClick = listener
    }

    /**
     * Sets the item long click listener.
     */
    fun setOnItemLongClick(listener: (View, Int, Group) -> Unit) {
        this.onItemLongClick = listener
    }

    /**
     * Sets custom item view listener for replacing entire item.
     */
    fun setItemView(listener: GroupsViewHolderListener?) {
        itemViewListener = listener
        notifyDataSetChanged()
    }

    /**
     * Sets custom leading view listener.
     */
    fun setLeadingView(listener: GroupsViewHolderListener?) {
        leadingViewListener = listener
        notifyDataSetChanged()
    }

    /**
     * Sets custom title view listener.
     */
    fun setTitleView(listener: GroupsViewHolderListener?) {
        titleViewListener = listener
        notifyDataSetChanged()
    }

    /**
     * Sets custom subtitle view listener.
     */
    fun setSubtitleView(listener: GroupsViewHolderListener?) {
        subtitleViewListener = listener
        notifyDataSetChanged()
    }

    /**
     * Sets custom trailing view listener.
     */
    fun setTrailingView(listener: GroupsViewHolderListener?) {
        trailingViewListener = listener
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupsViewHolder {
        // Create ViewHolder with CometChatGroupsItem as the row view
        val holder = GroupsViewHolder.create(parent)

        // Call createView() on all non-null listeners
        // This follows the chatuikit pattern where custom views are created once
        // during ViewHolder creation and cached for reuse
        holder.createCustomViews(
            itemViewListener,
            leadingViewListener,
            titleViewListener,
            subtitleViewListener,
            trailingViewListener
        )

        return holder
    }

    override fun onBindViewHolder(holder: GroupsViewHolder, position: Int) {
        val group = groupList[position]
        val isSelected = selectedGroups.any { it.guid == group.guid }

        // Update custom views if listeners have changed
        // This ensures custom views are recreated when setSubtitleView etc. is called
        holder.createCustomViews(
            itemViewListener,
            leadingViewListener,
            titleViewListener,
            subtitleViewListener,
            trailingViewListener
        )

        // Bind data to the ViewHolder
        holder.bind(
            group = group,
            groupList = groupList,
            position = position,
            isSelected = isSelected,
            selectionMode = selectionMode,
            hideGroupType = hideGroupType,
            hideSeparator = hideSeparator || position == groupList.lastIndex,
            style = itemStyle,
            itemViewListener = itemViewListener,
            leadingViewListener = leadingViewListener,
            titleViewListener = titleViewListener,
            subtitleViewListener = subtitleViewListener,
            trailingViewListener = trailingViewListener
        )

        // Setup click listeners on the CometChatGroupsItem
        holder.groupsItem.setOnItemClick { g ->
            onItemClick?.invoke(holder.itemView, position, g)
        }

        holder.groupsItem.setOnItemLongClick { g ->
            onItemLongClick?.invoke(holder.itemView, position, g)
        }
    }

    override fun getItemCount(): Int = groupList.size

    /**
     * DiffUtil callback for efficient list updates.
     * Compares all relevant group properties to detect changes.
     */
    private class GroupsDiffCallback(
        private val oldList: List<Group>,
        private val newList: List<Group>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].guid == newList[newItemPosition].guid
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldGroup = oldList[oldItemPosition]
            val newGroup = newList[newItemPosition]
            // Compare all properties that affect the UI display
            return oldGroup.guid == newGroup.guid &&
                    oldGroup.name == newGroup.name &&
                    oldGroup.icon == newGroup.icon &&
                    oldGroup.membersCount == newGroup.membersCount &&
                    oldGroup.groupType == newGroup.groupType &&
                    oldGroup.isJoined == newGroup.isJoined &&
                    oldGroup.scope == newGroup.scope
        }
    }
}
