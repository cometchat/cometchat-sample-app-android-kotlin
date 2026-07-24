package com.cometchat.uikit.kotlin.presentation.search.ui.viewholders

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.util.LruCache
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.MediaMessage
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.databinding.CometchatSearchMessageItemVideoBinding
import com.cometchat.uikit.kotlin.presentation.search.style.CometChatSearchStyle
import com.cometchat.uikit.kotlin.presentation.search.utils.SearchMessagesViewHolderListener
import com.cometchat.uikit.kotlin.presentation.shared.baseelements.date.CometChatDate
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.multiattachment.MultiAttachmentUtils
import com.cometchat.uikit.kotlin.shared.interfaces.DateTimeFormatterCallback

/**
 * ViewHolder for video message items in search results.
 *
 * Displays the sender name, "Video" label, timestamp, thread indicator,
 * and video thumbnail with play icon overlay.
 *
 * @param binding The ViewBinding for the video message item layout
 */
class SearchVideoMessageViewHolder(
    val binding: CometchatSearchMessageItemVideoBinding
) : BaseSearchMessageViewHolder(binding.root) {

    companion object {
        /** Max decoded video frames kept in memory; each entry holds a full-resolution bitmap. */
        private const val FRAME_CACHE_MAX = 50

        /**
         * URL-keyed, LRU-bounded cache so recycled rows don't re-decode a frame on every scroll
         * while capping how many bitmaps stay resident (avoids unbounded growth / OOM over a long
         * search session). [LruCache] is internally synchronized, so the decode thread and the bind
         * thread can share it safely.
         */
        private val frameCache = LruCache<String, Bitmap>(FRAME_CACHE_MAX)

        /**
         * Creates a new SearchVideoMessageViewHolder.
         *
         * @param parent The parent ViewGroup
         * @return A new SearchVideoMessageViewHolder instance
         */
        fun create(parent: ViewGroup): SearchVideoMessageViewHolder {
            val binding = CometchatSearchMessageItemVideoBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return SearchVideoMessageViewHolder(binding)
        }
    }

    /** Url whose async frame decode may still be in flight — guards recycled rows. */
    private var pendingFrameUrl: String? = null

    override val titleTextView: TextView = binding.tvMessageTitle
    override val subtitleTextView: TextView = binding.tvSubtitleView
    override val timestampDateView: CometChatDate? = null  // Video items don't have timestamps per reference design
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
     * Binds video message data to the views.
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
        // if present, else "N Videos" for multi, else the file name. The blurred "+N" scrim that
        // replaces the play badge stays multi-only; both decorations are also reset here for
        // recycled single-attachment rows.
        val attachments = MultiAttachmentUtils.resolveAttachments(message)
        val isMulti = attachments.size > 1
        binding.multiAttachmentOverlay.visibility = if (isMulti) View.VISIBLE else View.GONE
        binding.playButtonLayout.visibility = if (isMulti) View.GONE else View.VISIBLE
        setThumbnailBlur(binding.videoThumbnail, isMulti)

        binding.tvSubtitleView.text = buildMediaSubtitle(
            message, attachments.size, R.drawable.cometchat_ic_conversations_video,
            R.string.cometchat_message_video, uid, guid
        )

        // Thumbnail Generation extension url_medium (generated from the first attachment) is the
        // expected thumbnail source; the on-the-fly frame decode is the fallback for multi, and
        // the raw video url for singles.
        val thumbnailUrl = MultiAttachmentUtils.thumbnailUrl(message)

        if (isMulti) {
            binding.tvMultiAttachmentCount.text = "+${attachments.size - 1}"
            if (thumbnailUrl != null) {
                pendingFrameUrl = null
                loadThumbnail(thumbnailUrl)
            } else {
                loadVideoFrame(attachments.first().fileUrl, binding.videoThumbnail)
            }
            return
        }
        pendingFrameUrl = null

        // Load video thumbnail
        val videoUrl = thumbnailUrl ?: message.attachment?.fileUrl
        if (!videoUrl.isNullOrEmpty()) {
            loadThumbnail(videoUrl)
        } else {
            binding.videoThumbnail.setImageResource(R.drawable.cometchat_video_file_icon)
        }
    }

    private fun loadThumbnail(url: String) {
        Glide.with(context)
            .load(url)
            .placeholder(R.drawable.cometchat_video_file_icon)
            .error(R.drawable.cometchat_video_file_icon)
            .centerCrop()
            .into(binding.videoThumbnail)
    }

    /**
     * First frame of the (remote) video url via [MediaMetadataRetriever] on a background thread —
     * same on-the-fly approach as the video bubble tiles. Falls back to the placeholder icon
     * until decoded (or on failure, e.g. no network).
     */
    private fun loadVideoFrame(url: String?, target: ImageView) {
        pendingFrameUrl = url
        // A recycled row may still have a single-video Glide request in flight on this view.
        Glide.with(context).clear(target)
        frameCache.get(url ?: "")?.let {
            target.setImageBitmap(it)
            return
        }
        target.setImageResource(R.drawable.cometchat_video_file_icon)
        if (url.isNullOrEmpty()) return
        Thread {
            val frame = try {
                val retriever = MediaMetadataRetriever()
                try {
                    retriever.setDataSource(url, HashMap())
                    retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                } finally {
                    runCatching { retriever.release() }
                }
            } catch (e: Exception) {
                null
            }
            if (frame != null) {
                frameCache.put(url, frame)
                target.post { if (pendingFrameUrl == url) target.setImageBitmap(frame) }
            }
        }.start()
    }
}
