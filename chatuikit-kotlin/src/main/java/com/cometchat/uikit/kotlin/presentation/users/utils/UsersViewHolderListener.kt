package com.cometchat.uikit.kotlin.presentation.users.utils

import android.content.Context
import android.view.View
import com.cometchat.chat.models.User

/**
 * Interface for custom view callbacks in UsersAdapter.
 * 
 * This follows the chatuikit Java pattern where:
 * - createView() is called once during ViewHolder creation (onCreateViewHolder)
 * - bindView() is called during bind operations (onBindViewHolder) with user data
 * 
 * Custom views replace default views when listeners are set.
 */
interface UsersViewHolderListener {
    /**
     * Called once during ViewHolder creation to create the custom view.
     * The returned view will be cached and reused for all bind operations.
     * 
     * @param context The context for creating views
     * @param user The user (may be null during initial creation)
     * @return The custom view to display
     */
    fun createView(context: Context, user: User?): View

    /**
     * Called during bind operations to update the custom view with user data.
     * 
     * @param context The context
     * @param view The view created by createView()
     * @param user The user to display
     * @param userList The full list of users
     * @param position The position in the list
     */
    fun bindView(context: Context, view: View, user: User, userList: List<User>, position: Int)
}
