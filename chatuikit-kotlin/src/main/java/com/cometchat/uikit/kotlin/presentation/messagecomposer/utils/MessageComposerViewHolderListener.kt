package com.cometchat.uikit.kotlin.presentation.messagecomposer.utils

import android.content.Context
import android.view.View
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.User

/**
 * Interface for creating custom views in the CometChatMessageComposer component.
 * 
 * This interface follows the ViewHolderListener pattern used throughout the CometChat UIKit
 * for providing custom view implementations. Implement this interface to replace default
 * views in the message composer with custom implementations.
 * 
 * Usage example:
 * ```kotlin
 * messageComposer.setHeaderViewListener(object : MessageComposerViewHolderListener {
 *     override fun createView(context: Context, user: User?, group: Group?): View {
 *         return CustomHeaderView(context).apply {
 *             // Configure custom view
 *         }
 *     }
 * })
 * ```
 */
interface MessageComposerViewHolderListener {
    /**
     * Creates a custom view to be displayed in the message composer.
     * 
     * This method is called when the message composer needs to display the custom view.
     * The returned view will replace the default view for the corresponding slot
     * (header, footer, secondary button, send button, auxiliary button).
     * 
     * @param context The Android context for creating views
     * @param user The current user being messaged, or null if messaging a group
     * @param group The current group being messaged, or null if messaging a user
     * @return A View to be displayed in the message composer
     */
    fun createView(context: Context, user: User?, group: Group?): View
}
