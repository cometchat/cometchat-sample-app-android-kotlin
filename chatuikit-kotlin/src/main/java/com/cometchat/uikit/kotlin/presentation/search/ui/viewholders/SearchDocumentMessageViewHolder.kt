package com.cometchat.uikit.kotlin.presentation.search.ui.viewholders

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.MediaMessage
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.databinding.CometchatSearchMessageItemDocumentBinding
import com.cometchat.uikit.kotlin.presentation.search.style.CometChatSearchStyle
import com.cometchat.uikit.kotlin.presentation.search.utils.SearchMessagesViewHolderListener
import com.cometchat.uikit.kotlin.presentation.shared.baseelements.date.CometChatDate
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.multiattachment.MultiAttachmentUtils
import com.cometchat.uikit.kotlin.shared.interfaces.DateTimeFormatterCallback

/**
 * ViewHolder for document/file message items in search results.
 *
 * Displays the sender name, file name, timestamp, thread indicator, and document icon.
 *
 * @param binding The ViewBinding for the document message item layout
 */
class SearchDocumentMessageViewHolder(
    val binding: CometchatSearchMessageItemDocumentBinding
) : BaseSearchMessageViewHolder(binding.root) {

    companion object {
        /**
         * Creates a new SearchDocumentMessageViewHolder.
         *
         * @param parent The parent ViewGroup
         * @return A new SearchDocumentMessageViewHolder instance
         */
        fun create(parent: ViewGroup): SearchDocumentMessageViewHolder {
            val binding = CometchatSearchMessageItemDocumentBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return SearchDocumentMessageViewHolder(binding)
        }
    }

    override val titleTextView: TextView = binding.tvMessageTitle
    override val subtitleTextView: TextView = binding.tvSubtitleView
    override val subtitleSuffixTextView: TextView = binding.tvSubtitleSuffix
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
     * Binds document message data to the views.
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

        // ENG-36737 media-row rules: sender-prefixed subtitle = caption if present (multi keeps
        // its count as a non-truncating "· N Files" suffix), else "N Files" for multi, else the
        // file name. Multi stacks the FIRST document's type icon; reset for recycled single rows.
        val attachments = MultiAttachmentUtils.resolveAttachments(message)
        val isMulti = attachments.size > 1
        binding.tvSubtitleView.text = buildMediaSubtitle(
            message, attachments.size, R.drawable.cometchat_ic_conversations_document,
            R.string.cometchat_document, uid, guid
        )
        bindMediaCountSuffix(message, attachments.size)

        // Document icon based on the (first) attachment's MIME type (matching Java reference)
        val iconRes = getDocumentIcon(attachments.firstOrNull() ?: message.attachment)
        binding.messageIvDocumentThumbnail.setImageResource(iconRes)
        binding.ivDocumentStack1.setImageResource(iconRes)
        binding.ivDocumentStack2.setImageResource(iconRes)
        setStackedTypeIcon(binding.ivDocumentStack1, binding.ivDocumentStack2, isMulti)
    }

    /**
     * Gets the appropriate document icon based on MIME type and file URL.
     * Matches the Java reference implementation's file type detection logic.
     */
    private fun getDocumentIcon(attachment: com.cometchat.chat.models.Attachment?): Int {
        if (attachment == null) return R.drawable.cometchat_unknown_file_icon
        val mimeType = attachment.fileMimeType ?: return R.drawable.cometchat_unknown_file_icon
        val fileUrl = attachment.fileUrl ?: ""

        return when {
            mimeType.contains("video") -> R.drawable.cometchat_video_file_icon
            mimeType.contains("octet-stream") -> {
                when {
                    fileUrl.endsWith(".doc") || fileUrl.endsWith(".docx") -> R.drawable.cometchat_word_file_icon
                    fileUrl.endsWith(".ppt") || fileUrl.endsWith(".pptx") -> R.drawable.cometchat_ppt_file_icon
                    fileUrl.endsWith(".xls") || fileUrl.endsWith(".xlsx") -> R.drawable.cometchat_xlsx_file_icon
                    else -> R.drawable.cometchat_unknown_file_icon
                }
            }
            mimeType.contains("pdf") -> R.drawable.cometchat_pdf_file_icon
            mimeType.contains("zip") -> R.drawable.cometchat_zip_file_icon
            fileUrl.contains(".csv") -> R.drawable.cometchat_text_file_icon
            mimeType.contains("audio") -> R.drawable.cometchat_audio_file_icon
            mimeType.contains("image") -> R.drawable.cometchat_image_file_icon
            mimeType.contains("text") -> R.drawable.cometchat_text_file_icon
            mimeType.contains("link") -> R.drawable.cometchat_link_file_icon
            else -> R.drawable.cometchat_unknown_file_icon
        }
    }
}
