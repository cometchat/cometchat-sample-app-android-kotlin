package com.cometchat.uikit.kotlin.shared.resources.utils.unread_message_decoration

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cometchat.chat.models.BaseMessage

/**
 * Interface for adapters that support new message indicator decoration.
 * Adapters implementing this interface can provide new message indicator information
 * for drawing unread message separators on RecyclerView.
 *
 * @param T The type of ViewHolder used for the new message indicator
 */
interface NewMessageIndicatorDecorationAdapter<T : RecyclerView.ViewHolder> {

    /**
     * Returns the BaseMessage at the given position for indicator positioning.
     *
     * @param position The adapter position
     * @return The BaseMessage at the position, or null if not available
     */
    fun getNewMessageIndicatorId(position: Int): BaseMessage?

    /**
     * Creates a new ViewHolder for the new message indicator view.
     *
     * @param parent The parent ViewGroup
     * @return A new indicator ViewHolder
     */
    fun onCreateNewMessageViewHolder(parent: ViewGroup): T

    /**
     * Binds data to the new message indicator ViewHolder.
     *
     * @param holder The indicator ViewHolder
     * @param position The adapter position
     * @param messageId The message ID for the indicator
     */
    fun onBindNewMessageViewHolder(holder: T, position: Int, messageId: Long)
}
