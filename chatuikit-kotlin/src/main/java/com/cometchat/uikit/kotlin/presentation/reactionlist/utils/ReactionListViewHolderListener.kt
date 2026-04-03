package com.cometchat.uikit.kotlin.presentation.reactionlist.utils

import android.content.Context
import android.view.View
import com.cometchat.chat.models.Reaction

/**
 * Interface for custom view callbacks in ReactedUsersAdapter.
 * 
 * This follows the chatuikit Java pattern where:
 * - createView() is called once during ViewHolder creation (onCreateViewHolder)
 * - bindView() is called during bind operations (onBindViewHolder) with reaction data
 * 
 * Custom views replace default views when listeners are set.
 */
interface ReactionListViewHolderListener {
    /**
     * Called once during ViewHolder creation to create the custom view.
     * The returned view will be cached and reused for all bind operations.
     * 
     * @param context The context for creating views
     * @param reaction The reaction (may be null during initial creation)
     * @return The custom view to display
     */
    fun createView(context: Context, reaction: Reaction?): View

    /**
     * Called during bind operations to update the custom view with reaction data.
     * 
     * @param context The context
     * @param view The view created by createView()
     * @param reaction The reaction to display
     * @param reactionList The full list of reactions
     * @param position The position in the list
     */
    fun bindView(context: Context, view: View, reaction: Reaction, reactionList: List<Reaction>, position: Int)
}
