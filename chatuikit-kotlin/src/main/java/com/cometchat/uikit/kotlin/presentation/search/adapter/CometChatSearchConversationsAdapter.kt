package com.cometchat.uikit.kotlin.presentation.search.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.cometchat.chat.models.Conversation
import com.cometchat.uikit.kotlin.presentation.search.style.CometChatSearchStyle
import com.cometchat.uikit.kotlin.presentation.search.ui.viewholders.SearchConversationViewHolder
import com.cometchat.uikit.kotlin.presentation.search.utils.SearchConversationsViewHolderListener
import com.cometchat.uikit.kotlin.shared.interfaces.DateTimeFormatterCallback

/**
 * RecyclerView adapter for displaying conversation search results.
 *
 * This adapter uses ListAdapter with DiffUtil for efficient updates and supports
 * custom ViewHolder listeners for view customization.
 */
class CometChatSearchConversationsAdapter : ListAdapter<Conversation, SearchConversationViewHolder>(
    ConversationDiffCallback()
) {

    // Style configuration
    private var style: CometChatSearchStyle? = null

    // Date/time formatter
    private var dateTimeFormatter: DateTimeFormatterCallback? = null

    // Visibility flags
    private var hideUserStatus: Boolean = false
    private var hideGroupType: Boolean = false

    // Click callback
    private var onConversationClick: ((Conversation) -> Unit)? = null

    // Custom ViewHolder listeners
    private var itemViewListener: SearchConversationsViewHolderListener? = null
    private var leadingViewListener: SearchConversationsViewHolderListener? = null
    private var titleViewListener: SearchConversationsViewHolderListener? = null
    private var subtitleViewListener: SearchConversationsViewHolderListener? = null
    private var trailingViewListener: SearchConversationsViewHolderListener? = null

    // Text formatters for rich text rendering (mentions, etc.)
    private var textFormatters: List<com.cometchat.uikit.kotlin.shared.formatters.CometChatTextFormatter> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchConversationViewHolder {
        val holder = SearchConversationViewHolder.create(parent)
        
        // Create custom views during ViewHolder creation
        holder.createCustomViews(
            itemViewListener,
            leadingViewListener,
            titleViewListener,
            subtitleViewListener,
            trailingViewListener
        )
        
        return holder
    }

    override fun onBindViewHolder(holder: SearchConversationViewHolder, position: Int) {
        val conversation = getItem(position)
        
        holder.bind(
            conversation = conversation,
            conversationList = currentList,
            position = position,
            style = style,
            dateTimeFormatter = dateTimeFormatter,
            hideUserStatus = hideUserStatus,
            hideGroupType = hideGroupType,
            onClick = onConversationClick,
            itemViewListener = itemViewListener,
            leadingViewListener = leadingViewListener,
            titleViewListener = titleViewListener,
            subtitleViewListener = subtitleViewListener,
            trailingViewListener = trailingViewListener,
            textFormatters = textFormatters
        )
    }

    /**
     * Sets the style configuration for conversation items.
     */
    fun setStyle(style: CometChatSearchStyle) {
        this.style = style
        notifyDataSetChanged()
    }

    /**
     * Sets the date/time formatter for timestamps.
     */
    fun setDateTimeFormatter(formatter: DateTimeFormatterCallback?) {
        this.dateTimeFormatter = formatter
        notifyDataSetChanged()
    }

    /**
     * Sets whether to hide user online status indicator.
     */
    fun setHideUserStatus(hide: Boolean) {
        this.hideUserStatus = hide
        notifyDataSetChanged()
    }

    /**
     * Sets whether to hide group type indicator.
     */
    fun setHideGroupType(hide: Boolean) {
        this.hideGroupType = hide
        notifyDataSetChanged()
    }

    /**
     * Sets the click callback for conversation items.
     */
    fun setOnConversationClick(callback: ((Conversation) -> Unit)?) {
        this.onConversationClick = callback
    }

    /**
     * Sets the custom item view listener.
     * When set, the entire conversation item is replaced with a custom view.
     */
    fun setItemViewListener(listener: SearchConversationsViewHolderListener?) {
        this.itemViewListener = listener
        notifyDataSetChanged()
    }

    /**
     * Sets the custom leading view listener.
     * When set, the avatar/status indicator area is replaced with a custom view.
     */
    fun setLeadingViewListener(listener: SearchConversationsViewHolderListener?) {
        this.leadingViewListener = listener
        notifyDataSetChanged()
    }

    /**
     * Sets the custom title view listener.
     * When set, the title area is replaced with a custom view.
     */
    fun setTitleViewListener(listener: SearchConversationsViewHolderListener?) {
        this.titleViewListener = listener
        notifyDataSetChanged()
    }

    /**
     * Sets the custom subtitle view listener.
     * When set, the subtitle area is replaced with a custom view.
     */
    fun setSubtitleViewListener(listener: SearchConversationsViewHolderListener?) {
        this.subtitleViewListener = listener
        notifyDataSetChanged()
    }

    /**
     * Sets the custom trailing view listener.
     * When set, the timestamp/badge area is replaced with a custom view.
     */
    fun setTrailingViewListener(listener: SearchConversationsViewHolderListener?) {
        this.trailingViewListener = listener
        notifyDataSetChanged()
    }

    /**
     * Sets text formatters for message preview rendering.
     *
     * @param formatters The list of text formatters to apply.
     */
    fun setTextFormatters(formatters: List<com.cometchat.uikit.kotlin.shared.formatters.CometChatTextFormatter>) {
        this.textFormatters = formatters
        notifyDataSetChanged()
    }

    /**
     * DiffUtil callback for efficient conversation list updates.
     */
    private class ConversationDiffCallback : DiffUtil.ItemCallback<Conversation>() {
        override fun areItemsTheSame(oldItem: Conversation, newItem: Conversation): Boolean {
            return oldItem.conversationId == newItem.conversationId
        }

        override fun areContentsTheSame(oldItem: Conversation, newItem: Conversation): Boolean {
            return oldItem.conversationId == newItem.conversationId &&
                   oldItem.updatedAt == newItem.updatedAt &&
                   oldItem.unreadMessageCount == newItem.unreadMessageCount &&
                   oldItem.lastMessage?.id == newItem.lastMessage?.id
        }
    }
}
