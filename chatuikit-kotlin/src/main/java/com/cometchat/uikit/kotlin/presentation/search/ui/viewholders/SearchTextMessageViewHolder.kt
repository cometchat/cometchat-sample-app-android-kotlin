package com.cometchat.uikit.kotlin.presentation.search.ui.viewholders

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.models.Action
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.CardMessage
import com.cometchat.chat.models.TextMessage
import com.cometchat.uikit.core.CometChatUIKit
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.databinding.CometchatSearchMessageItemTextBinding
import com.cometchat.uikit.kotlin.presentation.search.style.CometChatSearchStyle
import com.cometchat.uikit.kotlin.presentation.search.utils.SearchMessagesViewHolderListener
import com.cometchat.uikit.kotlin.presentation.shared.baseelements.date.CometChatDate
import com.cometchat.uikit.kotlin.shared.interfaces.DateTimeFormatterCallback
import com.cometchat.uikit.kotlin.shared.formatters.CometChatTextFormatter
import com.cometchat.uikit.kotlin.shared.formatters.FormatterUtils
import com.cometchat.uikit.core.constants.UIKitConstants

/**
 * ViewHolder for text message items in search results.
 *
 * Displays the sender name, message text, timestamp, and thread indicator.
 *
 * @param binding The ViewBinding for the text message item layout
 */
class SearchTextMessageViewHolder(
    val binding: CometchatSearchMessageItemTextBinding
) : BaseSearchMessageViewHolder(binding.root) {

    companion object {
        /**
         * Creates a new SearchTextMessageViewHolder.
         *
         * @param parent The parent ViewGroup
         * @return A new SearchTextMessageViewHolder instance
         */
        fun create(parent: ViewGroup): SearchTextMessageViewHolder {
            val binding = CometchatSearchMessageItemTextBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return SearchTextMessageViewHolder(binding)
        }
    }

    override val titleTextView: TextView = binding.tvMessageTitle
    override val subtitleTextView: TextView? = binding.messageSubtitle
    override val timestampDateView: CometChatDate? = binding.date
    override val threadIndicator: ImageView = binding.icThreadMessage
    override val parentLayout: View = binding.parentLayout

    /**
     * Creates custom view using the provided listener.
     */
    fun createCustomView(
        itemViewListener: SearchMessagesViewHolderListener<TextMessage>?
    ) {
        if (itemViewListener !== lastItemViewListener) {
            lastItemViewListener = itemViewListener
            if (itemViewListener != null) {
                customItemView = itemViewListener.createView(context, binding)
                binding.parentLayout.removeAllViews()
                binding.parentLayout.addView(customItemView)
            } else {
                customItemView = null
            }
        }
    }

    /**
     * Binds text message data to the views.
     *
     * @param message The text message to display
     * @param messageList The full list of messages
     * @param position The position in the list
     * @param style The style configuration
     * @param dateTimeFormatter Optional custom date/time formatter
     * @param onClick Click callback for the item
     * @param itemViewListener Listener for custom item view
     * @param uid Optional user ID for context-aware display
     * @param guid Optional group ID for context-aware display
     */
    fun bind(
        message: TextMessage,
        messageList: List<BaseMessage>,
        position: Int,
        style: CometChatSearchStyle?,
        dateTimeFormatter: DateTimeFormatterCallback?,
        onClick: ((BaseMessage) -> Unit)?,
        itemViewListener: SearchMessagesViewHolderListener<TextMessage>?,
        uid: String? = null,
        guid: String? = null,
        textFormatters: List<CometChatTextFormatter> = emptyList()
    ) {
        // Handle custom item view
        if (itemViewListener != null && customItemView != null) {
            itemViewListener.bindView(
                context,
                customItemView!!,
                message,
                this,
                messageList,
                position
            )
            return
        }

        // Bind common data with uid/guid context
        bindCommonData(message, style, dateTimeFormatter, onClick, uid, guid)

        // Set message text with sender prefix for group messages
        val messageText = message.text ?: ""

        // Apply text formatters for rich text (mentions, etc.) matching reference
        val formattedText: CharSequence = if (textFormatters.isNotEmpty() && !message.text.isNullOrEmpty()) {
            FormatterUtils.getFormattedText(
                context = context,
                baseMessage = message,
                formattingType = UIKitConstants.FormattingType.CONVERSATIONS,
                alignment = UIKitConstants.MessageBubbleAlignment.LEFT,
                text = message.text,
                formatters = textFormatters
            )
        } else {
            messageText
        }

        // Build subtitle with sender prefix matching reference:
        // When scoped (uid/guid set): no prefix
        // When not scoped: ALWAYS add "SenderName: " or "You: " prefix
        val subtitle: CharSequence = if (uid == null && guid == null) {
            val loggedInUser = try {
                CometChatUIKit.getLoggedInUser()
            } catch (e: Exception) {
                null
            }
            val senderName = if (message.sender?.uid == loggedInUser?.uid) {
                context.getString(R.string.cometchat_you)
            } else {
                message.sender?.name ?: ""
            }
            if (senderName.isNotEmpty()) {
                android.text.SpannableStringBuilder("$senderName: ").append(formattedText)
            } else {
                formattedText
            }
        } else {
            // Scoped search — no sender prefix
            formattedText
        }
        binding.messageSubtitle.text = subtitle
    }

    /**
     * Binds action message data to the views.
     * Action messages are system messages like "user joined", "user left", etc.
     *
     * @param action The action message to display
     * @param messageList The full list of messages
     * @param position The position in the list
     * @param style The style configuration
     * @param dateTimeFormatter Optional custom date/time formatter
     * @param onClick Click callback for the item
     */
    fun bindAction(
        action: Action,
        messageList: List<BaseMessage>,
        position: Int,
        style: CometChatSearchStyle?,
        dateTimeFormatter: DateTimeFormatterCallback?,
        onClick: ((BaseMessage) -> Unit)?
    ) {
        // Bind common data
        bindCommonData(action, style, dateTimeFormatter, onClick)

        // Set action message text
        binding.messageSubtitle.text = action.message ?: action.action ?: ""
    }

    /**
     * Binds an unsupported/unmapped message type to the views as a generic fallback.
     * Mirrors the Compose `DefaultMessageContent`: shows the conversation/sender title
     * with a generic "Message" subtitle so the row is still rendered (e.g. card messages).
     *
     * @param message The message to display
     * @param messageList The full list of messages
     * @param position The position in the list
     * @param style The style configuration
     * @param dateTimeFormatter Optional custom date/time formatter
     * @param onClick Click callback for the item
     * @param uid Optional user ID for context-aware display
     * @param guid Optional group ID for context-aware display
     */
    fun bindDefault(
        message: BaseMessage,
        messageList: List<BaseMessage>,
        position: Int,
        style: CometChatSearchStyle?,
        dateTimeFormatter: DateTimeFormatterCallback?,
        onClick: ((BaseMessage) -> Unit)?,
        uid: String? = null,
        guid: String? = null
    ) {
        // Bind common data (title, timestamp, thread indicator, click, style)
        bindCommonData(message, style, dateTimeFormatter, onClick, uid, guid)

        // Generic subtitle for unmapped types, matching the Compose fallback
        binding.messageSubtitle.text = context.getString(R.string.cometchat_message_generic)
    }

    /**
     * Binds a card message to the views. Mirrors the conversation/reply preview handling:
     * shows the card's text, falling back to the generic "Card Message" label when empty.
     *
     * @param message The card message to display
     * @param messageList The full list of messages
     * @param position The position in the list
     * @param style The style configuration
     * @param dateTimeFormatter Optional custom date/time formatter
     * @param onClick Click callback for the item
     * @param uid Optional user ID for context-aware display
     * @param guid Optional group ID for context-aware display
     */
    fun bindCard(
        message: CardMessage,
        messageList: List<BaseMessage>,
        position: Int,
        style: CometChatSearchStyle?,
        dateTimeFormatter: DateTimeFormatterCallback?,
        onClick: ((BaseMessage) -> Unit)?,
        uid: String? = null,
        guid: String? = null
    ) {
        // Bind common data (title, timestamp, thread indicator, click, style)
        bindCommonData(message, style, dateTimeFormatter, onClick, uid, guid)

        // Subtitle = card text, falling back to the generic card label (matches conversation/reply)
        binding.messageSubtitle.text = message.text?.ifEmpty { null }
            ?: context.getString(R.string.cometchat_message_card)
    }
}
