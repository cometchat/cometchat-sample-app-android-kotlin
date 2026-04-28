package com.cometchat.uikit.kotlin.presentation.aiassistantchathistory.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.annotation.StyleRes
import androidx.recyclerview.widget.RecyclerView
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.TextMessage
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.databinding.CometchatAiAssistantChatHistoryItemBinding
import com.cometchat.uikit.kotlin.presentation.shared.baseelements.date.CometChatDate
import com.cometchat.uikit.kotlin.presentation.shared.baseelements.date.DatePattern
import com.cometchat.uikit.kotlin.shared.interfaces.DateTimeFormatterCallback
import com.cometchat.uikit.kotlin.shared.resources.localise.CometChatLocalize
import com.cometchat.uikit.kotlin.shared.resources.utils.Utils
import com.cometchat.uikit.kotlin.shared.resources.utils.sticky_header.StickyHeaderAdapter
import java.text.SimpleDateFormat

/**
 * RecyclerView adapter for displaying AI assistant chat history messages.
 *
 * This adapter binds [BaseMessage] items to single-line truncated text rows and
 * implements [StickyHeaderAdapter] for date-based sticky header grouping.
 * Date headers use [CometChatDate] with "MMM dd, yyyy" format and support
 * custom [DateTimeFormatterCallback] for locale-aware formatting.
 *
 * Ported from the Java implementation:
 * chatuikit/src/main/java/com/cometchat/chatuikit/aiassistantchathistory/AIAssistantChatHistoryAdapter.java
 */
class AIAssistantChatHistoryAdapter(
    private val context: Context
) : RecyclerView.Adapter<AIAssistantChatHistoryAdapter.MessageViewHolder>(),
    StickyHeaderAdapter<AIAssistantChatHistoryAdapter.DateItemHolder> {

    private var messages: List<BaseMessage> = emptyList()

    // Style properties for message items
    @ColorInt private var itemBackgroundColor: Int = 0
    @StyleRes private var itemTextAppearance: Int = 0
    @ColorInt private var itemTextColor: Int = 0

    // Style properties for date separator headers
    @ColorInt private var dateSeparatorTextColor: Int = 0
    @StyleRes private var dateSeparatorTextAppearance: Int = 0
    @ColorInt private var dateSeparatorBackgroundColor: Int = 0

    // Date formatters
    private val timeFormat = SimpleDateFormat("h:mm a", CometChatLocalize.getDefault())
    private val dateSeparatorFormat = SimpleDateFormat("MMM dd, yyyy", CometChatLocalize.getDefault())

    // Custom date/time formatter callback
    private var dateTimeFormatter: DateTimeFormatterCallback? = null

    // ==================== RecyclerView.Adapter Implementation ====================

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val binding = CometchatAiAssistantChatHistoryItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return MessageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]
        holder.bind(message)
    }

    override fun getItemCount(): Int = messages.size

    // ==================== Public Data Methods ====================

    /**
     * Returns the message at the given position, or null if the position is out of bounds.
     *
     * @param position The adapter position
     * @return The [BaseMessage] at the position, or null
     */
    fun getItem(position: Int): BaseMessage? {
        return if (position in messages.indices) {
            messages[position]
        } else {
            null
        }
    }

    /**
     * Replaces the current message list and refreshes the adapter.
     *
     * @param messageList The new list of messages to display
     */
    fun setMessageList(messageList: List<BaseMessage>) {
        this.messages = messageList
        notifyDataSetChanged()
    }

    // ==================== Style Setters ====================

    /**
     * Sets the background color for message item rows.
     */
    fun setItemBackgroundColor(@ColorInt color: Int) {
        this.itemBackgroundColor = color
    }

    /**
     * Sets the text color for message item text.
     */
    fun setItemTextColor(@ColorInt color: Int) {
        this.itemTextColor = color
    }

    /**
     * Sets the text appearance for message item text.
     */
    fun setItemTextAppearance(@StyleRes appearance: Int) {
        this.itemTextAppearance = appearance
    }

    /**
     * Sets the text color for date separator headers.
     */
    fun setDateSeparatorTextColor(@ColorInt color: Int) {
        this.dateSeparatorTextColor = color
    }

    /**
     * Sets the text appearance for date separator headers.
     */
    fun setDateSeparatorTextAppearance(@StyleRes appearance: Int) {
        this.dateSeparatorTextAppearance = appearance
    }

    /**
     * Sets the background color for date separator headers.
     */
    fun setDateSeparatorBackgroundColor(@ColorInt color: Int) {
        this.dateSeparatorBackgroundColor = color
    }

    /**
     * Sets the custom date/time formatter callback for date headers.
     */
    fun setDateTimeFormatter(formatter: DateTimeFormatterCallback?) {
        this.dateTimeFormatter = formatter
    }

    // ==================== StickyHeaderAdapter Implementation ====================

    override fun getHeaderId(position: Int): Long {
        return if (position in messages.indices) {
            val baseMessage = messages[position]
            Utils.getDateId(baseMessage.sentAt * 1000)
        } else {
            0L
        }
    }

    override fun onCreateHeaderViewHolder(parent: ViewGroup): DateItemHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.cometchat_ai_assistant_chat_history_sticky_header, parent, false)
        return DateItemHolder(view)
    }

    override fun onBindHeaderViewHolder(holder: DateItemHolder, position: Int, headerId: Long) {
        if (position in messages.indices) {
            val baseMessage = messages[position]

            if (baseMessage.sentAt > 0) {
                holder.txtMessageDate.setDateFormat(dateSeparatorFormat)
                dateTimeFormatter?.let { holder.txtMessageDate.setDateTimeFormatterCallback(it) }
                holder.txtMessageDate.setDate(baseMessage.sentAt, DatePattern.DAY_DATE)
            } else {
                holder.txtMessageDate.setDateText(context.getString(R.string.cometchat_updating))
            }

            // Apply style to the date header
            holder.txtMessageDate.setDateTextColor(dateSeparatorTextColor)
            if (dateSeparatorTextAppearance != 0) {
                holder.txtMessageDate.setDateTextAppearance(dateSeparatorTextAppearance)
            }
            holder.txtMessageDate.setBackgroundColor(dateSeparatorBackgroundColor)
        }
    }

    // ==================== ViewHolder Classes ====================

    /**
     * ViewHolder for displaying a single message item in the chat history list.
     * Binds the message text (single-line, ellipsis) and applies item style properties.
     */
    inner class MessageViewHolder(
        private val binding: CometchatAiAssistantChatHistoryItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        /**
         * Binds a [BaseMessage] to this ViewHolder.
         * Extracts text from [TextMessage] instances and applies style properties.
         *
         * @param message The message to bind
         */
        fun bind(message: BaseMessage) {
            val messageText = if (message is TextMessage) {
                message.text
            } else {
                ""
            }

            binding.tvMessageText.text = messageText
            if (itemTextAppearance != 0) {
                binding.tvMessageText.setTextAppearance(itemTextAppearance)
            }
            binding.tvMessageText.setTextColor(itemTextColor)
            binding.chatHistoryItemParent.setBackgroundColor(itemBackgroundColor)
        }
    }

    /**
     * ViewHolder for displaying date separator headers in the chat history list.
     * Uses [CometChatDate] for formatted date display with sticky header support.
     */
    class DateItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtMessageDate: CometChatDate = itemView.findViewById(R.id.txt_message_date)
    }
}
