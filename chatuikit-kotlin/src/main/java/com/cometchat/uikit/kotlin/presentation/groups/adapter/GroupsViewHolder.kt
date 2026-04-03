package com.cometchat.uikit.kotlin.presentation.groups.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cometchat.chat.models.Group
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.kotlin.presentation.groups.style.CometChatGroupsItemStyle
import com.cometchat.uikit.kotlin.presentation.groups.ui.CometChatGroupsItem
import com.cometchat.uikit.kotlin.presentation.groups.utils.GroupsViewHolderListener

/**
 * ViewHolder for group list items.
 * Uses CometChatGroupsItem as the row view and integrates with
 * GroupsViewHolderListener for custom view callbacks.
 * 
 * This implementation follows the chatuikit Java pattern where:
 * - createView() is called once during ViewHolder creation for each non-null listener
 * - bindView() is called during bind operations with group data
 * - Custom views replace default views when listeners are set
 * 
 * The change detection logic ensures proper restoration when custom views are removed,
 * preventing the "lag" issue where old custom views appear briefly before being replaced.
 */
class GroupsViewHolder(
    val groupsItem: CometChatGroupsItem
) : RecyclerView.ViewHolder(groupsItem) {

    companion object {
        /**
         * Creates a new GroupsViewHolder with CometChatGroupsItem as the row view.
         */
        fun create(parent: ViewGroup): GroupsViewHolder {
            val context = parent.context
            val groupsItem = CometChatGroupsItem(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }
            return GroupsViewHolder(groupsItem)
        }
    }

    private val context: Context = groupsItem.context

    // Custom views created by listeners (cached for reuse across bind operations)
    private var customItemView: View? = null
    private var customLeadingView: View? = null
    private var customTitleView: View? = null
    private var customSubtitleView: View? = null
    private var customTrailingView: View? = null

    // Track which listeners were used to create custom views
    private var lastItemViewListener: GroupsViewHolderListener? = null
    private var lastLeadingViewListener: GroupsViewHolderListener? = null
    private var lastTitleViewListener: GroupsViewHolderListener? = null
    private var lastSubtitleViewListener: GroupsViewHolderListener? = null
    private var lastTrailingViewListener: GroupsViewHolderListener? = null

    // Track whether custom views were actually applied (separate from listener reference)
    // This ensures restoration triggers when listener is null but custom view was applied
    private var hasCustomItemView: Boolean = false
    private var hasCustomLeadingView: Boolean = false
    private var hasCustomTitleView: Boolean = false
    private var hasCustomSubtitleView: Boolean = false
    private var hasCustomTrailingView: Boolean = false

    /**
     * Creates or updates custom views using the provided listeners.
     * Custom views are recreated when listeners change.
     * 
     * The change detection logic checks both:
     * 1. Listener reference change (listener !== lastListener)
     * 2. Need to restore defaults (listener == null && hasCustomView)
     * 
     * This ensures proper restoration when custom views are removed.
     */
    fun createCustomViews(
        itemViewListener: GroupsViewHolderListener?,
        leadingViewListener: GroupsViewHolderListener?,
        titleViewListener: GroupsViewHolderListener?,
        subtitleViewListener: GroupsViewHolderListener?,
        trailingViewListener: GroupsViewHolderListener?
    ) {
        // Check if any listener has changed OR if we need to restore defaults
        // Pattern: viewChanged = listener !== lastListener || (listener == null && hasCustomView)
        // Also check if listener class type changed (handles new instance of same listener type)
        val itemViewChanged = itemViewListener !== lastItemViewListener || 
            (itemViewListener == null && hasCustomItemView) ||
            (itemViewListener != null && lastItemViewListener != null && 
             itemViewListener::class.java != lastItemViewListener!!::class.java)
        val leadingViewChanged = leadingViewListener !== lastLeadingViewListener ||
            (leadingViewListener == null && hasCustomLeadingView) ||
            (leadingViewListener != null && lastLeadingViewListener != null && 
             leadingViewListener::class.java != lastLeadingViewListener!!::class.java)
        val titleViewChanged = titleViewListener !== lastTitleViewListener ||
            (titleViewListener == null && hasCustomTitleView) ||
            (titleViewListener != null && lastTitleViewListener != null && 
             titleViewListener::class.java != lastTitleViewListener!!::class.java)
        val subtitleViewChanged = subtitleViewListener !== lastSubtitleViewListener ||
            (subtitleViewListener == null && hasCustomSubtitleView) ||
            (subtitleViewListener != null && lastSubtitleViewListener != null && 
             subtitleViewListener::class.java != lastSubtitleViewListener!!::class.java)
        val trailingViewChanged = trailingViewListener !== lastTrailingViewListener ||
            (trailingViewListener == null && hasCustomTrailingView) ||
            (trailingViewListener != null && lastTrailingViewListener != null && 
             trailingViewListener::class.java != lastTrailingViewListener!!::class.java)

        // Update tracked listeners
        lastItemViewListener = itemViewListener
        lastLeadingViewListener = leadingViewListener
        lastTitleViewListener = titleViewListener
        lastSubtitleViewListener = subtitleViewListener
        lastTrailingViewListener = trailingViewListener

        // Handle item view (replaces entire item)
        if (itemViewChanged) {
            if (itemViewListener != null) {
                customItemView = itemViewListener.createView(context, groupsItem.binding)
                groupsItem.binding.parentLayout.removeAllViews()
                groupsItem.binding.parentLayout.addView(customItemView)
                hasCustomItemView = true
            } else {
                // Listener removed, restore default layout
                customItemView = null
                hasCustomItemView = false
                groupsItem.restoreDefaultLayout()
            }
        }

        // Only handle section views if no full item replacement
        if (customItemView == null) {
            // Handle leading view
            if (leadingViewChanged) {
                if (leadingViewListener != null) {
                    customLeadingView = leadingViewListener.createView(context, groupsItem.binding)
                    groupsItem.setLeadingView(customLeadingView)
                    hasCustomLeadingView = true
                } else {
                    customLeadingView = null
                    hasCustomLeadingView = false
                    groupsItem.setLeadingView(null)
                }
            }

            // Handle title view
            if (titleViewChanged) {
                if (titleViewListener != null) {
                    customTitleView = titleViewListener.createView(context, groupsItem.binding)
                    groupsItem.setTitleView(customTitleView)
                    hasCustomTitleView = true
                } else {
                    customTitleView = null
                    hasCustomTitleView = false
                    groupsItem.setTitleView(null)
                }
            }

            // Handle subtitle view
            if (subtitleViewChanged) {
                if (subtitleViewListener != null) {
                    customSubtitleView = subtitleViewListener.createView(context, groupsItem.binding)
                    groupsItem.setSubtitleView(customSubtitleView)
                    hasCustomSubtitleView = true
                } else {
                    customSubtitleView = null
                    hasCustomSubtitleView = false
                    groupsItem.setSubtitleView(null)
                }
            }

            // Handle trailing view
            if (trailingViewChanged) {
                if (trailingViewListener != null) {
                    customTrailingView = trailingViewListener.createView(context, groupsItem.binding)
                    groupsItem.setTrailingView(customTrailingView)
                    hasCustomTrailingView = true
                } else {
                    customTrailingView = null
                    hasCustomTrailingView = false
                    groupsItem.setTrailingView(null)
                }
            }
        }
    }

    /**
     * Binds group data to the views.
     * 
     * This method:
     * 1. Sets the group on CometChatGroupsItem (renders default data)
     * 2. Calls bindView() on all non-null listeners (allows custom views to update)
     */
    fun bind(
        group: Group,
        groupList: List<Group>,
        position: Int,
        isSelected: Boolean,
        selectionMode: UIKitConstants.SelectionMode,
        hideGroupType: Boolean,
        hideSeparator: Boolean,
        style: CometChatGroupsItemStyle,
        itemViewListener: GroupsViewHolderListener?,
        leadingViewListener: GroupsViewHolderListener?,
        titleViewListener: GroupsViewHolderListener?,
        subtitleViewListener: GroupsViewHolderListener?,
        trailingViewListener: GroupsViewHolderListener?
    ) {
        // Apply style to the item
        groupsItem.setStyle(style)

        // Apply visibility controls
        groupsItem.setHideGroupType(hideGroupType)
        groupsItem.setHideSeparator(hideSeparator)

        // Set selection state
        groupsItem.setSelectionMode(selectionMode)
        groupsItem.setItemSelected(isSelected)

        // Handle custom item view (full replacement)
        if (itemViewListener != null && customItemView != null) {
            // For full item replacement, only call bindView on the listener
            itemViewListener.bindView(
                context,
                customItemView!!,
                group,
                this,
                groupList,
                position
            )
            return
        }

        // Set group data on the item (renders default views)
        groupsItem.setGroup(group)

        // Call bindView on custom section listeners (allows custom views to update with data)
        leadingViewListener?.let { listener ->
            customLeadingView?.let { view ->
                listener.bindView(context, view, group, this, groupList, position)
            }
        }

        titleViewListener?.let { listener ->
            customTitleView?.let { view ->
                listener.bindView(context, view, group, this, groupList, position)
            }
        }

        subtitleViewListener?.let { listener ->
            customSubtitleView?.let { view ->
                listener.bindView(context, view, group, this, groupList, position)
            }
        }

        trailingViewListener?.let { listener ->
            customTrailingView?.let { view ->
                listener.bindView(context, view, group, this, groupList, position)
            }
        }
    }
}
