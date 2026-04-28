package com.cometchat.uikit.kotlin.presentation.search.ui.viewholders

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.MediaMessage
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.databinding.CometchatSearchMessageItemAudioBinding
import com.cometchat.uikit.kotlin.presentation.search.style.CometChatSearchStyle
import com.cometchat.uikit.kotlin.presentation.search.utils.SearchMessagesViewHolderListener
import com.cometchat.uikit.kotlin.presentation.shared.baseelements.date.CometChatDate
import com.cometchat.uikit.kotlin.shared.interfaces.DateTimeFormatterCallback

/**
 * ViewHolder for audio message items in search results.
 *
 * Displays the sender name, "Audio" label, timestamp, thread indicator, and audio icon.
 *
 * @param binding The ViewBinding for the audio message item layout
 */
class SearchAudioMessageViewHolder(
    val binding: CometchatSearchMessageItemAudioBinding
) : BaseSearchMessageViewHolder(binding.root) {

    companion object {
        /**
         * Creates a new SearchAudioMessageViewHolder.
         *
         * @param parent The parent ViewGroup
         * @return A new SearchAudioMessageViewHolder instance
         */
        fun create(parent: ViewGroup): SearchAudioMessageViewHolder {
            val binding = CometchatSearchMessageItemAudioBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return SearchAudioMessageViewHolder(binding)
        }
    }

    override val titleTextView: TextView = binding.tvMessageTitle
    override val subtitleTextView: TextView = binding.tvSubtitleView
    override val timestampDateView: CometChatDate? = binding.date
    override val threadIndicator: ImageView = binding.icThreadMessage
    override val parentLayout: View = binding.parentLayout

    /**
     * Creates custom view using the provided listener.
     */
    fun createCustomView(
        itemViewListener: SearchMessagesViewHolderListener<MediaMessage>?
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
     * Binds audio message data to the views.
     *
     * @param message The media message to display
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
        message: MediaMessage,
        messageList: List<BaseMessage>,
        position: Int,
        style: CometChatSearchStyle?,
        dateTimeFormatter: DateTimeFormatterCallback?,
        onClick: ((BaseMessage) -> Unit)?,
        itemViewListener: SearchMessagesViewHolderListener<MediaMessage>?,
        uid: String? = null,
        guid: String? = null
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

        // Set subtitle label — use file name with fallback (matching reference)
        val fileName = message.attachment?.fileName
        binding.tvSubtitleView.text = if (!fileName.isNullOrEmpty()) fileName else context.getString(R.string.cometchat_message_audio)

        // Audio icon is set in the layout, no additional binding needed
    }
}
