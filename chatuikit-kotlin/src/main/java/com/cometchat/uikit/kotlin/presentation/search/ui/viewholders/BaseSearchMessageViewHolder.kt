package com.cometchat.uikit.kotlin.presentation.search.ui.viewholders

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Group
import com.cometchat.uikit.core.CometChatUIKit
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.presentation.search.style.CometChatSearchStyle
import com.cometchat.uikit.kotlin.presentation.search.utils.SearchMessagesViewHolderListener
import com.cometchat.uikit.kotlin.presentation.shared.baseelements.date.CometChatDate
import com.cometchat.uikit.kotlin.presentation.shared.baseelements.date.DatePattern
import com.cometchat.uikit.kotlin.shared.interfaces.DateTimeFormatterCallback

/**
 * Base ViewHolder for search message items.
 *
 * This abstract class provides common functionality for all message type ViewHolders,
 * including title, subtitle, timestamp, and thread indicator handling.
 *
 * @param itemView The root view for the ViewHolder
 */
abstract class BaseSearchMessageViewHolder(
    itemView: View
) : RecyclerView.ViewHolder(itemView) {

    protected val context: Context = itemView.context

    // Common view references (to be set by subclasses)
    protected abstract val titleTextView: TextView?
    protected abstract val subtitleTextView: TextView?
    protected abstract val timestampDateView: CometChatDate?
    protected abstract val threadIndicator: ImageView?
    protected abstract val parentLayout: View?

    // Custom view tracking
    protected var customItemView: View? = null
    protected var lastItemViewListener: SearchMessagesViewHolderListener<*>? = null

    /**
     * Gets the conversation title based on message context.
     *
     * For uid/guid context (searching within specific conversation):
     * - Returns sender name (or "You" for logged-in user)
     *
     * For general search:
     * - Returns group name for group messages
     * - Returns sender name for 1:1 messages
     *
     * @param message The message to get title for
     * @param uid Optional user ID for context-aware display
     * @param guid Optional group ID for context-aware display
     * @return The conversation title string
     */
    protected fun getConversationTitle(message: BaseMessage, uid: String?, guid: String?): String {
        // If searching within specific conversation context
        if (uid != null || guid != null) {
            val senderUid = message.sender?.uid
            return if (senderUid == CometChatUIKit.getLoggedInUser()?.uid) {
                context.getString(R.string.cometchat_you)
            } else {
                message.sender?.name ?: ""
            }
        }

        // For general search, show conversation/group name
        val receiver = message.receiver
        return if (receiver is Group) {
            receiver.name ?: ""
        } else {
            message.sender?.name ?: ""
        }
    }

    /**
     * Binds common message data to the views.
     *
     * @param message The message to display
     * @param style The style configuration
     * @param dateTimeFormatter Optional custom date/time formatter
     * @param onClick Click callback for the item
     * @param uid Optional user ID for context-aware display
     * @param guid Optional group ID for context-aware display
     */
    protected fun bindCommonData(
        message: BaseMessage,
        style: CometChatSearchStyle?,
        dateTimeFormatter: DateTimeFormatterCallback?,
        onClick: ((BaseMessage) -> Unit)?,
        uid: String? = null,
        guid: String? = null
    ) {
        // Set title (conversation/group name)
        titleTextView?.text = getConversationTitle(message, uid, guid)

        // Set timestamp using CometChatDate API
        // Set timestamp matching reference: uses updatedAt with "dd MMM, yyyy" format
        // Reference uses setDateText() with pre-formatted string, not setDate() with pattern
        val timestamp = message.updatedAt * 1000
        val formattedDate = java.text.SimpleDateFormat(
            "dd MMM, yyyy",
            com.cometchat.uikit.kotlin.shared.resources.localise.CometChatLocalize.getDefault()
        ).format(java.util.Date(timestamp))
        timestampDateView?.setDateText(formattedDate)
        dateTimeFormatter?.let { timestampDateView?.setDateTimeFormatterCallback(it) }

        // Set thread indicator visibility
        val isThreadMessage = message.parentMessageId > 0
        threadIndicator?.visibility = if (isThreadMessage) View.VISIBLE else View.GONE

        // Set click listener
        itemView.setOnClickListener {
            onClick?.invoke(message)
        }

        // Apply style
        style?.let { applyCommonStyle(it) }
    }

    /**
     * Applies common style configuration to the views.
     * 
     * Style application follows the reference implementation pattern:
     * 1. Apply textAppearance first (sets font, size, and potentially color)
     * 2. Apply textColor second (overrides any color from textAppearance)
     */
    protected open fun applyCommonStyle(style: CometChatSearchStyle) {
        // Apply background color
        style.messageItemBackgroundColor?.let {
            parentLayout?.setBackgroundColor(it)
        }

        // Apply title style - textAppearance FIRST, then textColor to ensure color takes precedence
        style.messageTitleTextAppearance?.let {
            titleTextView?.setTextAppearance(it)
        }
        style.messageTitleTextColor?.let {
            if (it != 0) titleTextView?.setTextColor(it)
        }

        // Apply subtitle style - textAppearance FIRST, then textColor to ensure color takes precedence
        style.messageSubtitleTextAppearance?.let {
            subtitleTextView?.setTextAppearance(it)
        }
        style.messageSubtitleTextColor?.let {
            if (it != 0) subtitleTextView?.setTextColor(it)
        }

        // Apply timestamp style using CometChatDate API
        // CometChatDate internally handles the order: textAppearance first, then textColor
        style.messageTimestampTextAppearance?.let {
            timestampDateView?.setDateTextAppearance(it)
        }
        style.messageTimestampTextColor?.let {
            if (it != 0) timestampDateView?.setDateTextColor(it)
        }

        // Apply thread icon tint
        style.messageThreadIconTint?.let {
            threadIndicator?.setColorFilter(it)
        }
        style.messageThreadIcon?.let {
            threadIndicator?.setImageDrawable(it)
        }
    }
}
