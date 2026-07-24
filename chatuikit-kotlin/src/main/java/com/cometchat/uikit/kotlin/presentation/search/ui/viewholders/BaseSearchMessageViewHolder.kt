package com.cometchat.uikit.kotlin.presentation.search.ui.viewholders

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RenderEffect
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ImageSpan
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.MediaMessage
import com.cometchat.uikit.core.CometChatUIKit
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.presentation.search.style.CometChatSearchStyle
import com.cometchat.uikit.kotlin.presentation.search.utils.SearchMessagesViewHolderListener
import com.cometchat.uikit.kotlin.presentation.shared.baseelements.date.CometChatDate
import com.cometchat.uikit.kotlin.presentation.shared.baseelements.date.DatePattern
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.multiattachment.MultiAttachmentUtils
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

    /**
     * Non-truncating count suffix next to the subtitle ("· 6 Files") — only the audio/document
     * rows have one; it shares the subtitle's style.
     */
    protected open val subtitleSuffixTextView: TextView? = null

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
     * Subtitle for an image/video media result (single + multi share the structure): optional
     * "Sender: " prefix (global search only — same rule as the text rows), the conversation-list
     * media-type icon inline, then the message caption when present (markdown-parsed), the
     * "N Images"/"N Videos" count label for multi-attachment messages, or the single
     * attachment's file name (falling back to [fallbackLabelRes]).
     */
    protected fun buildMediaSubtitle(
        message: MediaMessage,
        count: Int,
        @DrawableRes iconRes: Int,
        @StringRes fallbackLabelRes: Int,
        uid: String?,
        guid: String?
    ): CharSequence {
        val builder = SpannableStringBuilder()

        if (uid == null && guid == null) {
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
            if (senderName.isNotEmpty()) builder.append("$senderName: ")
        }

        val textView = subtitleTextView
        val icon = AppCompatResources.getDrawable(context, iconRes)?.mutate()
        if (textView != null && icon != null) {
            icon.setTint(textView.currentTextColor)
            val size = textView.textSize.toInt()
            icon.setBounds(0, 0, size, size)
            val start = builder.length
            builder.append("￼ ")
            builder.setSpan(
                CenterAlignedImageSpan(icon),
                start,
                start + 1,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        val caption = message.caption?.takeIf { it.isNotBlank() }
        builder.append(
            when {
                caption != null -> MultiAttachmentUtils.renderCaption(context, caption)
                count > 1 -> MultiAttachmentUtils.countLabel(context, message.type, count)
                else -> message.attachment?.fileName?.takeIf { it.isNotEmpty() }
                    ?: context.getString(fallbackLabelRes)
            }
        )
        return builder
    }

    /**
     * Shows "· N Files"/"· N Audio" after the subtitle for a multi-attachment message WITH a
     * caption — the caption ellipsizes, the count never does. Hidden (recycle-safe) otherwise.
     */
    protected fun bindMediaCountSuffix(message: MediaMessage, count: Int) {
        val suffix = subtitleSuffixTextView ?: return
        val show = count > 1 && !message.caption.isNullOrBlank()
        suffix.visibility = if (show) View.VISIBLE else View.GONE
        if (show) {
            suffix.text = " · ${MultiAttachmentUtils.countLabel(context, message.type, count)}"
        }
    }

    /**
     * Toggles the stack behind a document row's leading type icon: two faded copies offset
     * toward the bottom-right, peeking out like sheets in a pile (documents-only per design
     * mock — audio keeps its plain play circle). Reset for recycled single-attachment rows.
     */
    protected fun setStackedTypeIcon(
        stack1: ImageView,
        stack2: ImageView,
        isMulti: Boolean
    ) {
        stack1.visibility = if (isMulti) View.VISIBLE else View.GONE
        stack2.visibility = if (isMulti) View.VISIBLE else View.GONE
    }

    /**
     * Blurs the multi-attachment thumbnail under the "+N" scrim (API 31+; below that the scrim
     * alone carries the treatment — same fallback as the composer tray tiles).
     */
    protected fun setThumbnailBlur(imageView: ImageView, blurred: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val density = context.resources.displayMetrics.density
            imageView.setRenderEffect(
                if (blurred) {
                    RenderEffect.createBlurEffect(3 * density, 3 * density, Shader.TileMode.CLAMP)
                } else {
                    null
                }
            )
        }
    }

    /**
     * [ImageSpan] that centers the drawable on the line vertically — the built-in ALIGN_BOTTOM/
     * ALIGN_BASELINE sink the icon below the text's optical center, and ALIGN_CENTER needs
     * API 29 (minSdk is 28).
     */
    private class CenterAlignedImageSpan(drawable: Drawable) : ImageSpan(drawable) {
        override fun draw(
            canvas: Canvas,
            text: CharSequence,
            start: Int,
            end: Int,
            x: Float,
            top: Int,
            y: Int,
            bottom: Int,
            paint: Paint
        ) {
            canvas.save()
            val transY = top + (bottom - top - drawable.bounds.height()) / 2f
            canvas.translate(x, transY)
            drawable.draw(canvas)
            canvas.restore()
        }
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
            subtitleSuffixTextView?.setTextAppearance(it)
        }
        style.messageSubtitleTextColor?.let {
            if (it != 0) {
                subtitleTextView?.setTextColor(it)
                subtitleSuffixTextView?.setTextColor(it)
            }
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
