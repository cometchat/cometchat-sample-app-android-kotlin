package com.cometchat.uikit.kotlin.presentation.messageheader.utils

import android.content.Context
import android.view.View
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.User

/**
 * Interface for creating custom views in the CometChatMessageHeader component.
 * 
 * This interface follows the ViewHolderListener pattern used throughout the CometChat UIKit
 * for providing custom view implementations. Implement this interface to replace default
 * views in the message header with custom implementations.
 * 
 * Usage example:
 * ```kotlin
 * messageHeader.setLeadingViewListener(object : MessageHeaderViewHolderListener {
 *     override fun createView(context: Context, user: User?, group: Group?): View {
 *         return CustomAvatarView(context).apply {
 *             // Configure custom view
 *         }
 *     }
 * })
 * ```
 */
interface MessageHeaderViewHolderListener {
    /**
     * Creates a custom view to be displayed in the message header.
     * 
     * This method is called when the message header needs to display the custom view.
     * The returned view will replace the default view for the corresponding slot
     * (leading, title, subtitle, trailing, or auxiliary).
     * 
     * @param context The Android context for creating views
     * @param user The current user being displayed, or null if displaying a group
     * @param group The current group being displayed, or null if displaying a user
     * @return A View to be displayed in the message header
     */
    fun createView(context: Context, user: User?, group: Group?): View
}
