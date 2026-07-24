package com.cometchat.uikit.kotlin.presentation.search.ui.viewholders

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.MediaMessage
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.databinding.CometchatSearchMessageItemImageBinding
import com.cometchat.uikit.kotlin.presentation.search.style.CometChatSearchStyle
import com.cometchat.uikit.kotlin.presentation.search.utils.SearchMessagesViewHolderListener
import com.cometchat.uikit.kotlin.presentation.shared.baseelements.date.CometChatDate
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.multiattachment.MultiAttachmentUtils
import com.cometchat.uikit.kotlin.shared.interfaces.DateTimeFormatterCallback

/**
 * ViewHolder for image message items in search results.
 *
 * Displays the sender name, "Photo" label, timestamp, thread indicator, and image thumbnail.
 *
 * @param binding The ViewBinding for the image message item layout
 */
class SearchImageMessageViewHolder(
    val binding: CometchatSearchMessageItemImageBinding
) : BaseSearchMessageViewHolder(binding.root) {

    companion object {
        /**
         * Creates a new SearchImageMessageViewHolder.
         *
         * @param parent The parent ViewGroup
         * @return A new SearchImageMessageViewHolder instance
         */
        fun create(parent: ViewGroup): SearchImageMessageViewHolder {
            val binding = CometchatSearchMessageItemImageBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return SearchImageMessageViewHolder(binding)
        }
    }

    override val titleTextView: TextView = binding.tvMessageTitle
    override val subtitleTextView: TextView = binding.tvSubtitleView
    override val timestampDateView: CometChatDate? = null  // Image items don't have timestamps per v5 design
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
     * Binds image message data to the views.
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

        // ENG-36737 media-row rules (single + multi share the subtitle): sender-prefixed caption
        // if present, else "N Images" for multi, else the file name. The blurred "+N" scrim stays
        // multi-only; both decorations are also reset here for recycled single-attachment rows.
        val attachments = MultiAttachmentUtils.resolveAttachments(message)
        val isMulti = attachments.size > 1
        binding.multiAttachmentOverlay.visibility = if (isMulti) View.VISIBLE else View.GONE
        setThumbnailBlur(binding.ivThumbnail, isMulti)

        if (isMulti) binding.tvMultiAttachmentCount.text = "+${attachments.size - 1}"
        binding.tvSubtitleView.text = buildMediaSubtitle(
            message, attachments.size, R.drawable.cometchat_ic_conversations_photo,
            R.string.cometchat_message_image, uid, guid
        )

        // Load the thumbnail — the Thumbnail Generation extension's url_medium (generated from
        // the first attachment) when present, else the first/single attachment's full fileUrl.
        val imageUrl = MultiAttachmentUtils.thumbnailUrl(message)
            ?: (if (isMulti) attachments.first() else message.attachment)?.fileUrl
        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.cometchat_image_placeholder)
                .error(R.drawable.cometchat_image_placeholder)
                .centerCrop()
                .into(binding.ivThumbnail)
        } else {
            binding.ivThumbnail.setImageResource(R.drawable.cometchat_image_placeholder)
        }
    }
}
