package com.cometchat.uikit.kotlin.presentation.groups.utils

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.cometchat.chat.models.Group
import com.cometchat.uikit.kotlin.databinding.CometchatGroupsListItemBinding
import com.cometchat.uikit.kotlin.shared.interfaces.ViewHolderCallBack

/**
 * Abstract class that serves as a listener for managing the creation and
 * binding of custom views within the groups list.
 * 
 * This follows the same pattern as the chatuikit Java implementation,
 * allowing developers to provide custom views for group list items
 * or specific sections (leading, title, subtitle, trailing).
 * 
 * Usage:
 * ```kotlin
 * groupsList.setSubtitleView(object : GroupsViewHolderListener() {
 *     override fun createView(
 *         context: Context,
 *         binding: CometchatGroupsListItemBinding
 *     ): View {
 *         return CustomSubtitleView(context)
 *     }
 *     
 *     override fun bindView(
 *         context: Context,
 *         createdView: View,
 *         group: Group,
 *         holder: RecyclerView.ViewHolder,
 *         groupList: List<Group>,
 *         position: Int
 *     ) {
 *         (createdView as CustomSubtitleView).bind(group)
 *     }
 * })
 * ```
 */
abstract class GroupsViewHolderListener : ViewHolderCallBack {

    companion object {
        private val TAG = GroupsViewHolderListener::class.java.simpleName
    }

    /**
     * Creates a custom view to be used in the group list item.
     * This is called once when the ViewHolder is created.
     * 
     * @param context The context
     * @param binding The ViewBinding for the group list item layout
     * @return The custom view to display
     */
    abstract fun createView(
        context: Context,
        binding: CometchatGroupsListItemBinding
    ): View

    /**
     * Binds data to the custom view.
     * This is called each time the ViewHolder is bound to a group.
     * 
     * @param context The context
     * @param createdView The view created by createView()
     * @param group The group to bind
     * @param holder The ViewHolder
     * @param groupList The full list of groups
     * @param position The position in the list
     */
    abstract fun bindView(
        context: Context,
        createdView: View,
        group: Group,
        holder: RecyclerView.ViewHolder,
        groupList: List<Group>,
        position: Int
    )
}
