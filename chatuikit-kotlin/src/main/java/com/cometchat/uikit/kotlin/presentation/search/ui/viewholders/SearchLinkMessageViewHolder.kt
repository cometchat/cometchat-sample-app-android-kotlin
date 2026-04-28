package com.cometchat.uikit.kotlin.presentation.search.ui.viewholders

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.TextMessage
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.databinding.CometchatSearchMessageItemLinkBinding
import com.cometchat.uikit.kotlin.presentation.search.style.CometChatSearchStyle
import com.cometchat.uikit.kotlin.presentation.search.utils.SearchMessagesViewHolderListener
import com.cometchat.uikit.kotlin.presentation.shared.baseelements.date.CometChatDate
import com.cometchat.uikit.kotlin.shared.interfaces.DateTimeFormatterCallback
import com.cometchat.uikit.kotlin.shared.resources.utils.DateUtils
import java.net.URL

/**
 * ViewHolder for link message items in search results.
 *
 * Displays the sender name, message text with link, timestamp, thread indicator,
 * and link preview (favicon or preview image).
 *
 * @param binding The ViewBinding for the link message item layout
 */
class SearchLinkMessageViewHolder(
    val binding: CometchatSearchMessageItemLinkBinding
) : BaseSearchMessageViewHolder(binding.root) {

    companion object {
        /**
         * Creates a new SearchLinkMessageViewHolder.
         *
         * @param parent The parent ViewGroup
         * @return A new SearchLinkMessageViewHolder instance
         */
        fun create(parent: ViewGroup): SearchLinkMessageViewHolder {
            val binding = CometchatSearchMessageItemLinkBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return SearchLinkMessageViewHolder(binding)
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
     * Binds link message data to the views.
     *
     * @param message The text message containing a link
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

        // Set message text
        binding.tvSubtitleView.text = message.text

        // Apply link text color if available
        style?.messageLinkTextColor?.let {
            binding.tvSubtitleView.setLinkTextColor(it)
        }

        // Load link preview
        loadLinkPreview(message)
    }

    /**
     * Loads the link preview image or favicon.
     */
    private fun loadLinkPreview(message: TextMessage) {
        val text = message.text
        val url = DateUtils.extractFirstUrl(text)

        if (url != null) {
            // Try to load favicon
            val faviconUrl = getFaviconUrl(url)
            if (faviconUrl != null) {
                Glide.with(context)
                    .load(faviconUrl)
                    .placeholder(R.drawable.cometchat_ic_link_outlined)
                    .error(R.drawable.cometchat_ic_link_outlined)
                    .centerCrop()
                    .into(binding.messageIvLinkThumbnail)
            } else {
                // Show default link icon
                binding.messageIvLinkThumbnail.setImageResource(R.drawable.cometchat_ic_link_outlined)
            }
        } else {
            // No URL found, show default link icon
            binding.messageIvLinkThumbnail.setImageResource(R.drawable.cometchat_ic_link_outlined)
        }
    }

    /**
     * Gets the favicon URL for a given URL.
     */
    private fun getFaviconUrl(urlString: String): String? {
        return try {
            val url = URL(urlString)
            val host = url.host
            "https://www.google.com/s2/favicons?domain=$host&sz=64"
        } catch (e: Exception) {
            null
        }
    }

    override fun applyCommonStyle(style: CometChatSearchStyle) {
        super.applyCommonStyle(style)

        // Apply link-specific styling
        style.messageLinkTextColor?.let {
            binding.tvSubtitleView.setLinkTextColor(it)
        }
        style.messageLinkTextAppearance?.let {
            binding.tvSubtitleView.setTextAppearance(it)
        }
    }
}
