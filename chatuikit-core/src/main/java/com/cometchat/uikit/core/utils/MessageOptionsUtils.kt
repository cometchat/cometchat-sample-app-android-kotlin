package com.cometchat.uikit.core.utils

import android.content.Context
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.R
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.domain.model.CometChatMessageOption

/**
 * Utility object that provides default message options based on message type.
 *
 * This centralizes the logic for determining which message options should be available
 * for different message types (text, image, video, audio, file) and applies business
 * rules to filter options based on context (user permissions, thread view, etc.).
 *
 * Message Key Format: `{category}_{type}` (e.g., "message_text", "message_image")
 *
 * Business Rules:
 * - REPLY_IN_THREAD: Only if not already in a thread view and message has no parent
 * - MARK_AS_UNREAD: Only if not my message and not in thread view
 * - MESSAGE_INFORMATION: Only if my message
 * - EDIT: Only if my message; for media messages only when a caption is present
 * - DELETE: If my message OR group admin/moderator
 * - REPORT: Only if not my message
 * - MESSAGE_PRIVATELY: Only if in group and not my message
 * - REPLY, SHARE, COPY, TRANSLATE: Always included when present in the default map
 */
object MessageOptionsUtils {

    /**
     * Map of message key (category_type) to list of option IDs.
     * The order of options in each list determines the display order.
     */
    private val defaultOptionsMap: Map<String, List<String>> = mapOf(
        "message_text" to listOf(
            UIKitConstants.MessageOption.REPLY_IN_THREAD,
            UIKitConstants.MessageOption.REPLY,
            UIKitConstants.MessageOption.SHARE,
            UIKitConstants.MessageOption.COPY,
            UIKitConstants.MessageOption.TRANSLATE,
            UIKitConstants.MessageOption.MARK_AS_UNREAD,
            UIKitConstants.MessageOption.MESSAGE_INFORMATION,
            UIKitConstants.MessageOption.EDIT,
            UIKitConstants.MessageOption.DELETE,
            UIKitConstants.MessageOption.REPORT,
            UIKitConstants.MessageOption.MESSAGE_PRIVATELY
        ),
        "message_image" to listOf(
            UIKitConstants.MessageOption.MESSAGE_INFORMATION,
            UIKitConstants.MessageOption.MARK_AS_UNREAD,
            UIKitConstants.MessageOption.REPLY_IN_THREAD,
            UIKitConstants.MessageOption.REPLY,
            UIKitConstants.MessageOption.SHARE,
            UIKitConstants.MessageOption.EDIT,
            UIKitConstants.MessageOption.REPORT,
            UIKitConstants.MessageOption.DELETE,
            UIKitConstants.MessageOption.MESSAGE_PRIVATELY
        ),
        "message_video" to listOf(
            UIKitConstants.MessageOption.MESSAGE_INFORMATION,
            UIKitConstants.MessageOption.MARK_AS_UNREAD,
            UIKitConstants.MessageOption.REPLY_IN_THREAD,
            UIKitConstants.MessageOption.REPLY,
            UIKitConstants.MessageOption.SHARE,
            UIKitConstants.MessageOption.EDIT,
            UIKitConstants.MessageOption.REPORT,
            UIKitConstants.MessageOption.DELETE,
            UIKitConstants.MessageOption.MESSAGE_PRIVATELY
        ),
        "message_audio" to listOf(
            UIKitConstants.MessageOption.MESSAGE_INFORMATION,
            UIKitConstants.MessageOption.MARK_AS_UNREAD,
            UIKitConstants.MessageOption.REPLY_IN_THREAD,
            UIKitConstants.MessageOption.REPLY,
            UIKitConstants.MessageOption.SHARE,
            UIKitConstants.MessageOption.EDIT,
            UIKitConstants.MessageOption.REPORT,
            UIKitConstants.MessageOption.DELETE,
            UIKitConstants.MessageOption.MESSAGE_PRIVATELY
        ),
        "message_file" to listOf(
            UIKitConstants.MessageOption.MESSAGE_INFORMATION,
            UIKitConstants.MessageOption.MARK_AS_UNREAD,
            UIKitConstants.MessageOption.REPLY_IN_THREAD,
            UIKitConstants.MessageOption.REPLY,
            UIKitConstants.MessageOption.SHARE,
            UIKitConstants.MessageOption.EDIT,
            UIKitConstants.MessageOption.REPORT,
            UIKitConstants.MessageOption.DELETE,
            UIKitConstants.MessageOption.MESSAGE_PRIVATELY
        )
    )

    /**
     * Default options for card messages (category = "card").
     * Includes delete, info, reply, reply in thread, mark as unread, and report.
     * Excludes edit, copy, translate, and share (not meaningful for rich cards).
     */
    private val cardMessageDefaultOptions: List<String> = listOf(
        UIKitConstants.MessageOption.REPLY_IN_THREAD,
        UIKitConstants.MessageOption.REPLY,
        UIKitConstants.MessageOption.MARK_AS_UNREAD,
        UIKitConstants.MessageOption.MESSAGE_INFORMATION,
        UIKitConstants.MessageOption.DELETE,
        UIKitConstants.MessageOption.REPORT,
        UIKitConstants.MessageOption.MESSAGE_PRIVATELY
    )

    /**
     * Default options for custom messages (category = "custom").
     * These are common options that apply to any custom message type.
     */
    private val customMessageDefaultOptions: List<String> = listOf(
        UIKitConstants.MessageOption.MESSAGE_INFORMATION,
        UIKitConstants.MessageOption.MARK_AS_UNREAD,
        UIKitConstants.MessageOption.REPLY_IN_THREAD,
        UIKitConstants.MessageOption.REPLY,
        UIKitConstants.MessageOption.REPORT,
        UIKitConstants.MessageOption.DELETE,
        UIKitConstants.MessageOption.MESSAGE_PRIVATELY
    )

    /**
     * Default options for agent (agentic) messages — COPY ONLY.
     *
     * Agent messages (category "agentic", type "assistant") expose only the copy
     * action. No react, reply, edit, delete, or other options are available.
     * The copy button is also rendered as a visible footer in the bubble itself.
     */
    private val agenticMessageDefaultOptions: List<String> = listOf(
        UIKitConstants.MessageOption.COPY
    )

    /**
     * Returns the list of default option IDs for a given message category and type.
     *
     * @param category The message category (e.g., "message", "custom")
     * @param type The message type (e.g., "text", "image", "video", "audio", "file")
     * @return List of option IDs for the message type. For custom messages or unknown types,
     *         returns common options that apply to all messages.
     */
    fun getDefaultOptionIds(category: String, type: String): List<String> {
        // Agent (agentic) messages — copy only, no react/reply/edit/delete
        if (category.lowercase() == UIKitConstants.MessageCategory.AGENTIC) {
            return agenticMessageDefaultOptions
        }
        // Card messages route on category alone (type is arbitrary/developer-chosen)
        if (category.lowercase() == UIKitConstants.MessageCategory.CARD) {
            return cardMessageDefaultOptions
        }
        val key = "${category}_$type".lowercase()
        return defaultOptionsMap[key] ?: customMessageDefaultOptions
    }

    /**
     * Returns the list of [CometChatMessageOption] objects for a message,
     * applying business rules to filter options based on context.
     *
     * @param context Android context for accessing resources
     * @param message The message for which to get options
     * @param user The user in a 1-on-1 conversation (null for group conversations)
     * @param group The group in a group conversation (null for 1-on-1 conversations)
     * @param isThreadView Whether the message is being viewed in a thread
     * @return List of message options filtered by business rules
     */
    fun getDefaultMessageOptions(
        context: Context,
        message: BaseMessage,
        user: User?,
        group: Group?,
        isThreadView: Boolean = false
    ): List<CometChatMessageOption> {
        val optionIds = getDefaultOptionIds(message.category, message.type)

        // Check moderation status — if DISAPPROVED, restrict to Copy, Delete, Translate only
        val isDisapproved = when (message) {
            is com.cometchat.chat.models.TextMessage -> message.moderationStatus?.name == "DISAPPROVED"
            is com.cometchat.chat.models.MediaMessage -> message.moderationStatus?.name == "DISAPPROVED"
            else -> false
        }
        val filteredOptionIds = if (isDisapproved) {
            optionIds.filter {
                it == UIKitConstants.MessageOption.COPY ||
                    it == UIKitConstants.MessageOption.DELETE ||
                    it == UIKitConstants.MessageOption.TRANSLATE
            }
        } else {
            optionIds
        }

        val loggedInUser = CometChat.getLoggedInUser()
        val isMyMessage = message.sender?.uid == loggedInUser?.uid
        val isGroupAdmin = group?.let {
            it.scope == com.cometchat.chat.constants.CometChatConstants.SCOPE_ADMIN ||
                it.scope == com.cometchat.chat.constants.CometChatConstants.SCOPE_MODERATOR
        } ?: false

        return filteredOptionIds.mapNotNull { optionId ->
            createMessageOption(
                context = context,
                optionId = optionId,
                message = message,
                isMyMessage = isMyMessage,
                isGroupAdmin = isGroupAdmin,
                isInGroup = group != null,
                isThreadView = isThreadView
            )
        }
    }

    /**
     * Filters a list of message options based on visibility settings.
     * Options not present in the map default to visible.
     *
     * @param options The list of options to filter
     * @param optionVisibilityMap Map of option ID to visibility (true = visible, false = hidden)
     * @return Filtered list of options where visibility is true or not specified
     */
    fun getFilteredMessageOptions(
        options: List<CometChatMessageOption>,
        optionVisibilityMap: Map<String, Boolean>
    ): List<CometChatMessageOption> {
        return options.filter { option ->
            optionVisibilityMap[option.id] ?: true
        }
    }

    /**
     * Creates a [CometChatMessageOption] if the option should be shown for this context.
     * Returns null if the option should not be shown based on business rules.
     */
    private fun createMessageOption(
        context: Context,
        optionId: String,
        message: BaseMessage,
        isMyMessage: Boolean,
        isGroupAdmin: Boolean,
        isInGroup: Boolean,
        isThreadView: Boolean
    ): CometChatMessageOption? {
        return when (optionId) {
            UIKitConstants.MessageOption.REPLY_IN_THREAD -> {
                if (isThreadView || message.parentMessageId != 0L) null
                else replyInThread(context)
            }
            UIKitConstants.MessageOption.MESSAGE_INFORMATION -> {
                if (!isMyMessage) null
                else messageInfo(context)
            }
            UIKitConstants.MessageOption.EDIT -> {
                // Media messages are only editable when there is caption text to edit.
                val hasEditableContent = when (message) {
                    is com.cometchat.chat.models.MediaMessage -> !message.caption.isNullOrEmpty()
                    else -> true
                }
                if (!isMyMessage || !hasEditableContent) null
                else edit(context)
            }
            UIKitConstants.MessageOption.DELETE -> {
                if (!isMyMessage && !isGroupAdmin) null
                else delete(context)
            }
            UIKitConstants.MessageOption.REPORT -> {
                if (isMyMessage) null
                else report(context)
            }
            UIKitConstants.MessageOption.MESSAGE_PRIVATELY -> {
                if (!isInGroup || isMyMessage) null
                else messagePrivately(context)
            }
            UIKitConstants.MessageOption.REPLY, UIKitConstants.MessageOption.REPLY_TO_MESSAGE -> {
                reply(context)
            }
            UIKitConstants.MessageOption.SHARE -> {
                share(context)
            }
            UIKitConstants.MessageOption.COPY -> {
                copy(context)
            }
            UIKitConstants.MessageOption.TRANSLATE -> {
                translate(context)
            }
            UIKitConstants.MessageOption.MARK_AS_UNREAD -> {
                if (isMyMessage || isThreadView) null
                else markAsUnread(context)
            }
            else -> null
        }
    }

    // ========================================
    // Factory methods for creating message options
    // ========================================

    /** Creates a "Reply in Thread" message option. */
    fun replyInThread(context: Context): CometChatMessageOption {
        return CometChatMessageOption(
            id = UIKitConstants.MessageOption.REPLY_IN_THREAD,
            title = context.getString(R.string.cometchat_reply_uppercase),
            icon = R.drawable.cometchat_ic_thread
        )
    }

    /** Creates a "Reply" message option. */
    fun reply(context: Context): CometChatMessageOption {
        return CometChatMessageOption(
            id = UIKitConstants.MessageOption.REPLY,
            title = context.getString(R.string.cometchat_reply),
            icon = R.drawable.cometchat_ic_reply_to_message
        )
    }

    /** Creates a "Copy" message option. */
    fun copy(context: Context): CometChatMessageOption {
        return CometChatMessageOption(
            id = UIKitConstants.MessageOption.COPY,
            title = context.getString(R.string.cometchat_copy),
            icon = R.drawable.cometchat_ic_copy_paste
        )
    }

    /** Creates an "Edit" message option. */
    fun edit(context: Context): CometChatMessageOption {
        return CometChatMessageOption(
            id = UIKitConstants.MessageOption.EDIT,
            title = context.getString(R.string.cometchat_edit),
            icon = R.drawable.cometchat_ic_edit
        )
    }

    /** Creates a "Delete" message option. */
    fun delete(context: Context): CometChatMessageOption {
        return CometChatMessageOption(
            id = UIKitConstants.MessageOption.DELETE,
            title = context.getString(R.string.cometchat_delete),
            icon = R.drawable.cometchat_ic_delete
        )
    }

    /** Creates a "Share" message option. */
    fun share(context: Context): CometChatMessageOption {
        return CometChatMessageOption(
            id = UIKitConstants.MessageOption.SHARE,
            title = context.getString(R.string.cometchat_share),
            icon = R.drawable.cometchat_ic_share
        )
    }

    /** Creates a "Translate" message option. */
    fun translate(context: Context): CometChatMessageOption {
        return CometChatMessageOption(
            id = UIKitConstants.MessageOption.TRANSLATE,
            title = context.getString(R.string.cometchat_translate),
            icon = R.drawable.cometchat_ic_translate
        )
    }

    /** Creates a "Message Info" message option. */
    fun messageInfo(context: Context): CometChatMessageOption {
        return CometChatMessageOption(
            id = UIKitConstants.MessageOption.MESSAGE_INFORMATION,
            title = context.getString(R.string.cometchat_info),
            icon = R.drawable.cometchat_ic_info
        )
    }

    /** Creates a "Report" message option. */
    fun report(context: Context): CometChatMessageOption {
        return CometChatMessageOption(
            id = UIKitConstants.MessageOption.REPORT,
            title = context.getString(R.string.cometchat_report),
            icon = R.drawable.cometchat_ic_warning
        )
    }

    /** Creates a "Message Privately" message option. */
    fun messagePrivately(context: Context): CometChatMessageOption {
        return CometChatMessageOption(
            id = UIKitConstants.MessageOption.MESSAGE_PRIVATELY,
            title = context.getString(R.string.cometchat_message_privately),
            icon = R.drawable.cometchat_ic_send_message_privately
        )
    }

    /** Creates a "Mark as Unread" message option. */
    fun markAsUnread(context: Context): CometChatMessageOption {
        return CometChatMessageOption(
            id = UIKitConstants.MessageOption.MARK_AS_UNREAD,
            title = context.getString(R.string.cometchat_mark_unread),
            icon = R.drawable.cometchat_ic_unread_outlined
        )
    }
}
