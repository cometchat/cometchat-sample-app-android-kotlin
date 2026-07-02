package com.cometchat.uikit.kotlin.presentation.search.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.cometchat.chat.models.Action
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.CardMessage
import com.cometchat.chat.models.MediaMessage
import com.cometchat.chat.models.TextMessage
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.kotlin.presentation.search.style.CometChatSearchStyle
import com.cometchat.uikit.kotlin.presentation.search.ui.viewholders.DateItemHolder
import com.cometchat.uikit.kotlin.presentation.search.ui.viewholders.SearchAudioMessageViewHolder
import com.cometchat.uikit.kotlin.presentation.search.ui.viewholders.SearchDocumentMessageViewHolder
import com.cometchat.uikit.kotlin.presentation.search.ui.viewholders.SearchImageMessageViewHolder
import com.cometchat.uikit.kotlin.presentation.search.ui.viewholders.SearchLinkMessageViewHolder
import com.cometchat.uikit.kotlin.presentation.search.ui.viewholders.SearchTextMessageViewHolder
import com.cometchat.uikit.kotlin.presentation.search.ui.viewholders.SearchVideoMessageViewHolder
import com.cometchat.uikit.kotlin.presentation.search.utils.SearchMessagesViewHolderListener
import com.cometchat.uikit.kotlin.shared.interfaces.DateTimeFormatterCallback
import com.cometchat.uikit.kotlin.shared.resources.utils.sticky_header.StickyHeaderAdapter
import java.util.Calendar

/**
 * RecyclerView adapter for displaying message search results.
 *
 * This adapter supports multiple message types (text, image, video, audio, document, link)
 * with different ViewHolder types. It also implements StickyHeaderAdapter for date separators.
 */
class CometChatSearchMessageListAdapter : ListAdapter<BaseMessage, RecyclerView.ViewHolder>(
    MessageDiffCallback()
), StickyHeaderAdapter<DateItemHolder> {

    companion object {
        const val VIEW_TYPE_TEXT = 1
        const val VIEW_TYPE_IMAGE = 2
        const val VIEW_TYPE_VIDEO = 3
        const val VIEW_TYPE_AUDIO = 4
        const val VIEW_TYPE_DOCUMENT = 5
        const val VIEW_TYPE_LINK = 6
    }

    // Style configuration
    private var style: CometChatSearchStyle? = null

    // Date/time formatter
    private var dateTimeFormatter: DateTimeFormatterCallback? = null

    // Click callback
    private var onMessageClick: ((BaseMessage) -> Unit)? = null

    // User/Group ID for context-aware display
    private var uid: String? = null
    private var guid: String? = null

    // Custom ViewHolder listeners
    private var textMessageItemViewListener: SearchMessagesViewHolderListener<TextMessage>? = null

    // Text formatters for rich text rendering (mentions, links, etc.)
    private var textFormatters: List<com.cometchat.uikit.kotlin.shared.formatters.CometChatTextFormatter> = emptyList()
    private var imageMessageItemViewListener: SearchMessagesViewHolderListener<MediaMessage>? = null
    private var videoMessageItemViewListener: SearchMessagesViewHolderListener<MediaMessage>? = null
    private var audioMessageItemViewListener: SearchMessagesViewHolderListener<MediaMessage>? = null
    private var documentMessageItemViewListener: SearchMessagesViewHolderListener<MediaMessage>? = null
    private var linkMessageItemViewListener: SearchMessagesViewHolderListener<TextMessage>? = null

    override fun getItemViewType(position: Int): Int {
        val message = getItem(position)
        return when (message.type) {
            UIKitConstants.MessageType.IMAGE -> VIEW_TYPE_IMAGE
            UIKitConstants.MessageType.VIDEO -> VIEW_TYPE_VIDEO
            UIKitConstants.MessageType.AUDIO -> VIEW_TYPE_AUDIO
            UIKitConstants.MessageType.FILE -> VIEW_TYPE_DOCUMENT
            UIKitConstants.MessageType.TEXT -> {
                if (message is TextMessage && hasLinkInMessage(message.text)) {
                    VIEW_TYPE_LINK
                } else {
                    VIEW_TYPE_TEXT
                }
            }
            else -> VIEW_TYPE_TEXT
        }
    }
    
    /**
     * Checks if the given text contains a URL link.
     *
     * @param text The text to check
     * @return true if the text contains a URL, false otherwise
     */
    private fun hasLinkInMessage(text: String?): Boolean {
        if (text.isNullOrEmpty()) return false
        val urlPattern = "(https?://[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=%]+)".toRegex()
        return urlPattern.containsMatchIn(text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_TEXT -> {
                val holder = SearchTextMessageViewHolder.create(parent)
                holder.createCustomView(textMessageItemViewListener)
                holder
            }
            VIEW_TYPE_IMAGE -> {
                val holder = SearchImageMessageViewHolder.create(parent)
                holder.createCustomView(imageMessageItemViewListener)
                holder
            }
            VIEW_TYPE_VIDEO -> {
                val holder = SearchVideoMessageViewHolder.create(parent)
                holder.createCustomView(videoMessageItemViewListener)
                holder
            }
            VIEW_TYPE_AUDIO -> {
                val holder = SearchAudioMessageViewHolder.create(parent)
                holder.createCustomView(audioMessageItemViewListener)
                holder
            }
            VIEW_TYPE_DOCUMENT -> {
                val holder = SearchDocumentMessageViewHolder.create(parent)
                holder.createCustomView(documentMessageItemViewListener)
                holder
            }
            VIEW_TYPE_LINK -> {
                val holder = SearchLinkMessageViewHolder.create(parent)
                holder.createCustomView(linkMessageItemViewListener)
                holder
            }
            else -> {
                val holder = SearchTextMessageViewHolder.create(parent)
                holder.createCustomView(textMessageItemViewListener)
                holder
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = getItem(position)
        
        when (holder) {
            is SearchTextMessageViewHolder -> {
                // Handle both TextMessage and Action messages safely
                when (message) {
                    is TextMessage -> {
                        holder.bind(
                            message = message,
                            messageList = currentList,
                            position = position,
                            style = style,
                            dateTimeFormatter = dateTimeFormatter,
                            onClick = onMessageClick,
                            itemViewListener = textMessageItemViewListener,
                            uid = uid,
                            guid = guid,
                            textFormatters = textFormatters
                        )
                    }
                    is Action -> {
                        // For Action messages, bind with action message text
                        holder.bindAction(
                            action = message,
                            messageList = currentList,
                            position = position,
                            style = style,
                            dateTimeFormatter = dateTimeFormatter,
                            onClick = onMessageClick
                        )
                    }
                    is CardMessage -> {
                        // Show the card's text as subtitle (falls back to "Card Message"),
                        // matching the conversation/reply preview handling
                        holder.bindCard(
                            message = message,
                            messageList = currentList,
                            position = position,
                            style = style,
                            dateTimeFormatter = dateTimeFormatter,
                            onClick = onMessageClick,
                            uid = uid,
                            guid = guid
                        )
                    }
                    else -> {
                        // Render unmapped/unsupported types with a generic fallback,
                        // matching the Compose DefaultMessageContent
                        holder.bindDefault(
                            message = message,
                            messageList = currentList,
                            position = position,
                            style = style,
                            dateTimeFormatter = dateTimeFormatter,
                            onClick = onMessageClick,
                            uid = uid,
                            guid = guid
                        )
                    }
                }
            }
            is SearchImageMessageViewHolder -> {
                if (message is MediaMessage) {
                    holder.bind(
                        message = message,
                        messageList = currentList,
                        position = position,
                        style = style,
                        dateTimeFormatter = dateTimeFormatter,
                        onClick = onMessageClick,
                        itemViewListener = imageMessageItemViewListener,
                        uid = uid,
                        guid = guid
                    )
                }
            }
            is SearchVideoMessageViewHolder -> {
                if (message is MediaMessage) {
                    holder.bind(
                        message = message,
                        messageList = currentList,
                        position = position,
                        style = style,
                        dateTimeFormatter = dateTimeFormatter,
                        onClick = onMessageClick,
                        itemViewListener = videoMessageItemViewListener,
                        uid = uid,
                        guid = guid
                    )
                }
            }
            is SearchAudioMessageViewHolder -> {
                if (message is MediaMessage) {
                    holder.bind(
                        message = message,
                        messageList = currentList,
                        position = position,
                        style = style,
                        dateTimeFormatter = dateTimeFormatter,
                        onClick = onMessageClick,
                        itemViewListener = audioMessageItemViewListener,
                        uid = uid,
                        guid = guid
                    )
                }
            }
            is SearchDocumentMessageViewHolder -> {
                if (message is MediaMessage) {
                    holder.bind(
                        message = message,
                        messageList = currentList,
                        position = position,
                        style = style,
                        dateTimeFormatter = dateTimeFormatter,
                        onClick = onMessageClick,
                        itemViewListener = documentMessageItemViewListener,
                        uid = uid,
                        guid = guid
                    )
                }
            }
            is SearchLinkMessageViewHolder -> {
                if (message is TextMessage) {
                    holder.bind(
                        message = message,
                        messageList = currentList,
                        position = position,
                        style = style,
                        dateTimeFormatter = dateTimeFormatter,
                        onClick = onMessageClick,
                        itemViewListener = linkMessageItemViewListener,
                        uid = uid,
                        guid = guid
                    )
                }
            }
        }
    }

    // StickyHeaderAdapter implementation

    override fun getHeaderId(position: Int): Long {
        if (position < 0 || position >= itemCount) return -1
        
        val message = getItem(position)
        val calendar = Calendar.getInstance().apply {
            timeInMillis = message.sentAt * 1000
        }
        
        // Group by year + day of year for unique date identification
        return (calendar.get(Calendar.YEAR) * 1000L + calendar.get(Calendar.DAY_OF_YEAR))
    }

    override fun onCreateHeaderViewHolder(parent: ViewGroup): DateItemHolder {
        return DateItemHolder.create(parent)
    }

    override fun onBindHeaderViewHolder(holder: DateItemHolder, position: Int, headerId: Long) {
        if (position < 0 || position >= itemCount) return
        
        val message = getItem(position)
        // CometChatDate.setDate() expects seconds — it internally multiplies by 1000
        // Pass sentAt directly (in seconds), not sentAt * 1000
        val timestamp = message.sentAt
        
        holder.bind(
            timestamp = timestamp,
            style = style,
            dateTimeFormatter = dateTimeFormatter
        )
    }

    // Style and configuration setters

    /**
     * Sets the style configuration for message items.
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
     * Sets the click callback for message items.
     */
    fun setOnMessageClick(callback: ((BaseMessage) -> Unit)?) {
        this.onMessageClick = callback
    }

    /**
     * Sets the user ID for context-aware display.
     * When set, message titles will show sender name instead of conversation name.
     */
    fun setUid(uid: String?) {
        this.uid = uid
    }

    /**
     * Sets the group ID for context-aware display.
     * When set, message titles will show sender name instead of conversation name.
     */
    fun setGuid(guid: String?) {
        this.guid = guid
    }

    // Custom ViewHolder listener setters

    /**
     * Sets the custom view listener for text message items.
     */
    fun setTextMessageItemViewListener(listener: SearchMessagesViewHolderListener<TextMessage>?) {
        this.textMessageItemViewListener = listener
        notifyDataSetChanged()
    }

    /**
     * Sets the custom view listener for image message items.
     */
    fun setImageMessageItemViewListener(listener: SearchMessagesViewHolderListener<MediaMessage>?) {
        this.imageMessageItemViewListener = listener
        notifyDataSetChanged()
    }

    /**
     * Sets the custom view listener for video message items.
     */
    fun setVideoMessageItemViewListener(listener: SearchMessagesViewHolderListener<MediaMessage>?) {
        this.videoMessageItemViewListener = listener
        notifyDataSetChanged()
    }

    /**
     * Sets the custom view listener for audio message items.
     */
    fun setAudioMessageItemViewListener(listener: SearchMessagesViewHolderListener<MediaMessage>?) {
        this.audioMessageItemViewListener = listener
        notifyDataSetChanged()
    }

    /**
     * Sets the custom view listener for document message items.
     */
    fun setDocumentMessageItemViewListener(listener: SearchMessagesViewHolderListener<MediaMessage>?) {
        this.documentMessageItemViewListener = listener
        notifyDataSetChanged()
    }

    /**
     * Sets the custom view listener for link message items.
     */
    fun setLinkMessageItemViewListener(listener: SearchMessagesViewHolderListener<TextMessage>?) {
        this.linkMessageItemViewListener = listener
        notifyDataSetChanged()
    }

    /**
     * Sets text formatters for message text rendering.
     *
     * @param formatters The list of text formatters to apply.
     */
    fun setTextFormatters(formatters: List<com.cometchat.uikit.kotlin.shared.formatters.CometChatTextFormatter>) {
        this.textFormatters = formatters
        notifyDataSetChanged()
    }

    /**
     * DiffUtil callback for efficient message list updates.
     */
    private class MessageDiffCallback : DiffUtil.ItemCallback<BaseMessage>() {
        override fun areItemsTheSame(oldItem: BaseMessage, newItem: BaseMessage): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: BaseMessage, newItem: BaseMessage): Boolean {
            return oldItem.id == newItem.id &&
                   oldItem.sentAt == newItem.sentAt &&
                   oldItem.updatedAt == newItem.updatedAt &&
                   oldItem.deletedAt == newItem.deletedAt
        }
    }
}
