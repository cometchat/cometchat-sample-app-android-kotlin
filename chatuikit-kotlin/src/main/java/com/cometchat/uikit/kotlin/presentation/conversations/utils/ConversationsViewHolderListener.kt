package com.cometchat.uikit.kotlin.presentation.conversations.utils

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.cometchat.chat.models.Conversation
import com.cometchat.uikit.kotlin.databinding.CometchatConversationsListItemsBinding
import com.cometchat.uikit.kotlin.shared.interfaces.ViewHolderCallBack

/**
 * Abstract class that serves as a listener for managing the creation and
 * binding of custom views within the conversation list.
 * 
 * This follows the same pattern as the chatuikit Java implementation,
 * allowing developers to provide custom views for conversation list items
 * or specific sections (leading, title, subtitle, trailing).
 * 
 * Usage:
 * ```kotlin
 * conversationList.setSubtitleView(object : ConversationsViewHolderListener() {
 *     override fun createView(
 *         context: Context,
 *         binding: CometchatConversationsListItemsBinding
 *     ): View {
 *         return CustomSubtitleView(context)
 *     }
 *     
 *     override fun bindView(
 *         context: Context,
 *         createdView: View,
 *         conversation: Conversation,
 *         typingIndicator: TypingIndicator?,
 *         holder: RecyclerView.ViewHolder,
 *         conversationList: List<Conversation>,
 *         position: Int
 *     ) {
 *         (createdView as CustomSubtitleView).bind(conversation, typingIndicator)
 *     }
 * })
 * ```
 */
abstract class ConversationsViewHolderListener : ViewHolderCallBack {

    companion object {
        private val TAG = ConversationsViewHolderListener::class.java.simpleName
    }

    /**
     * Creates a custom view to be used in the conversation list item.
     * This is called once when the ViewHolder is created.
     * 
     * @param context The context
     * @param binding The ViewBinding for the conversation list item layout
     * @return The custom view to display
     */
    abstract fun createView(
        context: Context,
        binding: CometchatConversationsListItemsBinding
    ): View

    /**
     * Binds data to the custom view.
     * This is called each time the ViewHolder is bound to a conversation.
     * 
     * @param context The context
     * @param createdView The view created by createView()
     * @param conversation The conversation to bind
     * @param typingIndicator The typing indicator info for this conversation, or null if no one is typing.
     *                        Contains list of all users currently typing (supports multiple users in groups).
     * @param holder The ViewHolder
     * @param conversationList The full list of conversations
     * @param position The position in the list
     */
    abstract fun bindView(
        context: Context,
        createdView: View,
        conversation: Conversation,
        typingIndicator: TypingIndicator?,
        holder: RecyclerView.ViewHolder,
        conversationList: List<Conversation>,
        position: Int
    )
}
