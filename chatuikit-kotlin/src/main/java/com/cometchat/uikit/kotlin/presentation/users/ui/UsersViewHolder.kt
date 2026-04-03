package com.cometchat.uikit.kotlin.presentation.users.ui

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.kotlin.presentation.users.style.CometChatUsersListItemStyle
import com.cometchat.uikit.kotlin.presentation.users.utils.UsersViewHolderListener

/**
 * ViewHolder for user list items.
 * Uses CometChatUsersListItem as the row view and integrates with
 * UsersViewHolderListener for custom view callbacks.
 * 
 * This implementation follows the chatuikit Java pattern where:
 * - createView() is called once during ViewHolder creation for each non-null listener
 * - bindView() is called during bind operations with user data
 * - Custom views replace default views when listeners are set
 */
class UsersViewHolder(
    val usersListItem: CometChatUsersListItem
) : RecyclerView.ViewHolder(usersListItem) {

    companion object {
        /**
         * Creates a new UsersViewHolder with CometChatUsersListItem as the row view.
         */
        fun create(parent: ViewGroup): UsersViewHolder {
            val context = parent.context
            val usersListItem = CometChatUsersListItem(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }
            return UsersViewHolder(usersListItem)
        }
    }

    private val context: Context = usersListItem.context

    // Custom views created by listeners (cached for reuse across bind operations)
    private var customItemView: View? = null
    private var customLeadingView: View? = null
    private var customTitleView: View? = null
    private var customSubtitleView: View? = null
    private var customTrailingView: View? = null

    // Track which listeners were used to create custom views
    private var lastItemViewListener: UsersViewHolderListener? = null
    private var lastLeadingViewListener: UsersViewHolderListener? = null
    private var lastTitleViewListener: UsersViewHolderListener? = null
    private var lastSubtitleViewListener: UsersViewHolderListener? = null
    private var lastTrailingViewListener: UsersViewHolderListener? = null

    // Track whether custom views were actually applied
    private var hasCustomItemView: Boolean = false
    private var hasCustomLeadingView: Boolean = false
    private var hasCustomTitleView: Boolean = false
    private var hasCustomSubtitleView: Boolean = false
    private var hasCustomTrailingView: Boolean = false

    /**
     * Creates or updates custom views using the provided listeners.
     * Custom views are recreated when listeners change.
     */
    fun createCustomViews(
        itemViewListener: UsersViewHolderListener?,
        leadingViewListener: UsersViewHolderListener?,
        titleViewListener: UsersViewHolderListener?,
        subtitleViewListener: UsersViewHolderListener?,
        trailingViewListener: UsersViewHolderListener?
    ) {
        val itemViewChanged = itemViewListener !== lastItemViewListener || 
            (itemViewListener == null && hasCustomItemView)
        val leadingViewChanged = leadingViewListener !== lastLeadingViewListener ||
            (leadingViewListener == null && hasCustomLeadingView)
        val titleViewChanged = titleViewListener !== lastTitleViewListener ||
            (titleViewListener == null && hasCustomTitleView)
        val subtitleViewChanged = subtitleViewListener !== lastSubtitleViewListener ||
            (subtitleViewListener == null && hasCustomSubtitleView)
        val trailingViewChanged = trailingViewListener !== lastTrailingViewListener ||
            (trailingViewListener == null && hasCustomTrailingView)

        // Update tracked listeners
        lastItemViewListener = itemViewListener
        lastLeadingViewListener = leadingViewListener
        lastTitleViewListener = titleViewListener
        lastSubtitleViewListener = subtitleViewListener
        lastTrailingViewListener = trailingViewListener

        // Handle item view (replaces entire item)
        if (itemViewChanged) {
            if (itemViewListener != null) {
                customItemView = itemViewListener.createView(context, null)
                usersListItem.getParentLayout().removeAllViews()
                usersListItem.getParentLayout().addView(customItemView)
                hasCustomItemView = true
            } else {
                customItemView = null
                hasCustomItemView = false
                usersListItem.restoreDefaultLayout()
            }
        }

        // Only handle section views if no full item replacement
        if (customItemView == null) {
            if (leadingViewChanged) {
                if (leadingViewListener != null) {
                    customLeadingView = leadingViewListener.createView(context, null)
                    usersListItem.setLeadingView(customLeadingView)
                    hasCustomLeadingView = true
                } else {
                    customLeadingView = null
                    hasCustomLeadingView = false
                    usersListItem.setLeadingView(null)
                }
            }

            if (titleViewChanged) {
                if (titleViewListener != null) {
                    customTitleView = titleViewListener.createView(context, null)
                    usersListItem.setTitleView(customTitleView)
                    hasCustomTitleView = true
                } else {
                    customTitleView = null
                    hasCustomTitleView = false
                    usersListItem.setTitleView(null)
                }
            }

            if (subtitleViewChanged) {
                if (subtitleViewListener != null) {
                    customSubtitleView = subtitleViewListener.createView(context, null)
                    usersListItem.setSubtitleView(customSubtitleView)
                    hasCustomSubtitleView = true
                } else {
                    customSubtitleView = null
                    hasCustomSubtitleView = false
                    usersListItem.setSubtitleView(null)
                }
            }

            if (trailingViewChanged) {
                if (trailingViewListener != null) {
                    customTrailingView = trailingViewListener.createView(context, null)
                    usersListItem.setTrailingView(customTrailingView)
                    hasCustomTrailingView = true
                } else {
                    customTrailingView = null
                    hasCustomTrailingView = false
                    usersListItem.setTrailingView(null)
                }
            }
        }
    }

    /**
     * Binds user data to the views.
     */
    fun bind(
        user: User,
        userList: List<User>,
        position: Int,
        isSelected: Boolean,
        selectionEnabled: Boolean,
        hideUserStatus: Boolean,
        hideSeparator: Boolean,
        style: CometChatUsersListItemStyle,
        itemViewListener: UsersViewHolderListener?,
        leadingViewListener: UsersViewHolderListener?,
        titleViewListener: UsersViewHolderListener?,
        subtitleViewListener: UsersViewHolderListener?,
        trailingViewListener: UsersViewHolderListener?
    ) {
        // Apply style to the item
        usersListItem.setStyle(style)

        // Apply visibility controls
        usersListItem.setHideUserStatus(hideUserStatus)
        usersListItem.setHideSeparator(hideSeparator)

        // Set selection state
        usersListItem.setSelectionMode(
            if (selectionEnabled) UIKitConstants.SelectionMode.MULTIPLE
            else UIKitConstants.SelectionMode.NONE
        )
        usersListItem.setItemSelected(isSelected)

        // Handle custom item view (full replacement)
        if (itemViewListener != null && customItemView != null) {
            itemViewListener.bindView(context, customItemView!!, user, userList, position)
            return
        }

        // Set user data on the item (renders default views)
        usersListItem.setUser(user)

        // Call bindView on custom section listeners
        leadingViewListener?.let { listener ->
            customLeadingView?.let { view ->
                listener.bindView(context, view, user, userList, position)
            }
        }

        titleViewListener?.let { listener ->
            customTitleView?.let { view ->
                listener.bindView(context, view, user, userList, position)
            }
        }

        subtitleViewListener?.let { listener ->
            customSubtitleView?.let { view ->
                listener.bindView(context, view, user, userList, position)
            }
        }

        trailingViewListener?.let { listener ->
            customTrailingView?.let { view ->
                listener.bindView(context, view, user, userList, position)
            }
        }
    }
}
