package com.cometchat.uikit.kotlin.presentation.search.utils

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Conversation

/**
 * Listener interface for customizing conversation ViewHolder views in search results.
 *
 * This interface allows consumers to provide custom views for conversation items
 * or specific sections (leading, title, subtitle, trailing) of conversation items.
 *
 * Usage:
 * - Implement createView() to create your custom view once during ViewHolder creation
 * - Implement bindView() to bind data to your custom view during bind operations
 */
interface SearchConversationsViewHolderListener {

    /**
     * Creates a custom view for the conversation item or section.
     *
     * This method is called once during ViewHolder creation. The returned view
     * will be cached and reused for subsequent bind operations.
     *
     * @param context The Android context
     * @param binding The ViewBinding for the conversation item layout
     * @return The custom view to display
     */
    fun createView(context: Context, binding: ViewBinding): View

    /**
     * Binds conversation data to the custom view.
     *
     * This method is called during each bind operation to update the custom view
     * with the current conversation data.
     *
     * @param context The Android context
     * @param view The custom view created by createView()
     * @param conversation The conversation to display
     * @param holder The ViewHolder containing the view
     * @param conversationList The full list of conversations
     * @param position The position in the list
     */
    fun bindView(
        context: Context,
        view: View,
        conversation: Conversation,
        holder: RecyclerView.ViewHolder,
        conversationList: List<Conversation>,
        position: Int
    )
}

/**
 * Listener interface for customizing message ViewHolder views in search results.
 *
 * This interface allows consumers to provide custom views for message items.
 * It is generic to support different message types (TextMessage, MediaMessage).
 *
 * Usage:
 * - Implement createView() to create your custom view once during ViewHolder creation
 * - Implement bindView() to bind data to your custom view during bind operations
 *
 * @param T The message type (e.g., TextMessage, MediaMessage)
 */
interface SearchMessagesViewHolderListener<T : BaseMessage> {

    /**
     * Creates a custom view for the message item.
     *
     * This method is called once during ViewHolder creation. The returned view
     * will be cached and reused for subsequent bind operations.
     *
     * @param context The Android context
     * @param binding The ViewBinding for the message item layout
     * @return The custom view to display
     */
    fun createView(context: Context, binding: ViewBinding): View

    /**
     * Binds message data to the custom view.
     *
     * This method is called during each bind operation to update the custom view
     * with the current message data.
     *
     * @param context The Android context
     * @param view The custom view created by createView()
     * @param message The message to display
     * @param holder The ViewHolder containing the view
     * @param messageList The full list of messages
     * @param position The position in the list
     */
    fun bindView(
        context: Context,
        view: View,
        message: T,
        holder: RecyclerView.ViewHolder,
        messageList: List<BaseMessage>,
        position: Int
    )
}
