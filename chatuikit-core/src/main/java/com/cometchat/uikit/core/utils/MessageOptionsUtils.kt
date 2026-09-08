package com.cometchat.uikit.core.utils

import android.content.Context
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.CometChatUIKit
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
     * Default options for media messages (image/video/audio/file).
     *
     * Ordered so the common conversational actions (reply, share, …) come first — matching the
     * text ordering — and the less-frequent actions (pin/save, thread subscription, info) fall
     * into the paginated "More" overflow instead of leading the list.
     *
     * Declared before [defaultOptionsMap] because that map references it during initialization.
     */
    private val mediaMessageDefaultOptions: List<String> = listOf(
        // Main page (Copy is text-only, so it's absent here): Reply, Reply in thread, Notify me, Edit, Delete
        UIKitConstants.MessageOption.REPLY,
        UIKitConstants.MessageOption.REPLY_IN_THREAD,
        UIKitConstants.MessageOption.THREAD_SUBSCRIPTION,
        UIKitConstants.MessageOption.EDIT,
        UIKitConstants.MessageOption.DELETE,
        // "More" overflow: the rest
        UIKitConstants.MessageOption.SHARE,
        UIKitConstants.MessageOption.MARK_AS_UNREAD,
        UIKitConstants.MessageOption.PIN,
        UIKitConstants.MessageOption.UNPIN,
        UIKitConstants.MessageOption.SAVE,
        UIKitConstants.MessageOption.UNSAVE,
        UIKitConstants.MessageOption.MESSAGE_INFORMATION,
        UIKitConstants.MessageOption.REPORT,
        UIKitConstants.MessageOption.MESSAGE_PRIVATELY
    )

    /**
     * Map of message key (category_type) to list of option IDs.
     * The order of options in each list determines the display order.
     */
    private val defaultOptionsMap: Map<String, List<String>> = mapOf(
        "message_text" to listOf(
            // Main page: Reply, Reply in thread, Notify me, Copy, Edit, Delete
            UIKitConstants.MessageOption.REPLY,
            UIKitConstants.MessageOption.REPLY_IN_THREAD,
            UIKitConstants.MessageOption.THREAD_SUBSCRIPTION,
            UIKitConstants.MessageOption.COPY,
            UIKitConstants.MessageOption.EDIT,
            UIKitConstants.MessageOption.DELETE,
            // "More" overflow: the rest
            UIKitConstants.MessageOption.SHARE,
            UIKitConstants.MessageOption.TRANSLATE,
            UIKitConstants.MessageOption.MARK_AS_UNREAD,
            UIKitConstants.MessageOption.PIN,
            UIKitConstants.MessageOption.UNPIN,
            UIKitConstants.MessageOption.SAVE,
            UIKitConstants.MessageOption.UNSAVE,
            UIKitConstants.MessageOption.MESSAGE_INFORMATION,
            UIKitConstants.MessageOption.REPORT,
            UIKitConstants.MessageOption.MESSAGE_PRIVATELY
        ),
        "message_image" to mediaMessageDefaultOptions,
        "message_video" to mediaMessageDefaultOptions,
        "message_audio" to mediaMessageDefaultOptions,
        "message_file" to mediaMessageDefaultOptions
    )

    /**
     * Default options for card messages (category = "card").
     * Includes delete, info, reply, reply in thread, mark as unread, and report.
     * Excludes edit, copy, translate, and share (not meaningful for rich cards).
     */
    private val cardMessageDefaultOptions: List<String> = listOf(
        // Main page (Copy/Edit don't apply to cards): Reply, Reply in thread, Notify me, Delete
        UIKitConstants.MessageOption.REPLY,
        UIKitConstants.MessageOption.REPLY_IN_THREAD,
        UIKitConstants.MessageOption.THREAD_SUBSCRIPTION,
        UIKitConstants.MessageOption.DELETE,
        // "More" overflow: the rest (pin/save apply to every pinnable type, same as text/media)
        UIKitConstants.MessageOption.MARK_AS_UNREAD,
        UIKitConstants.MessageOption.PIN,
        UIKitConstants.MessageOption.UNPIN,
        UIKitConstants.MessageOption.SAVE,
        UIKitConstants.MessageOption.UNSAVE,
        UIKitConstants.MessageOption.MESSAGE_INFORMATION,
        UIKitConstants.MessageOption.REPORT,
        UIKitConstants.MessageOption.MESSAGE_PRIVATELY
    )

    /**
     * Default options for custom messages (category = "custom").
     * These are common options that apply to any custom message type.
     */
    private val customMessageDefaultOptions: List<String> = listOf(
        // Main page (Copy/Edit don't apply to custom messages): Reply, Reply in thread, Notify me, Delete
        UIKitConstants.MessageOption.REPLY,
        UIKitConstants.MessageOption.REPLY_IN_THREAD,
        UIKitConstants.MessageOption.THREAD_SUBSCRIPTION,
        UIKitConstants.MessageOption.DELETE,
        // "More" overflow: the rest. Pin/save apply to every pinnable type — this list serves
        // stickers, polls, whiteboard/document and any unknown custom type, all of which must
        // offer Pin/Save like text and media do (the per-option rules still gate by role/state).
        UIKitConstants.MessageOption.MARK_AS_UNREAD,
        UIKitConstants.MessageOption.PIN,
        UIKitConstants.MessageOption.UNPIN,
        UIKitConstants.MessageOption.SAVE,
        UIKitConstants.MessageOption.UNSAVE,
        UIKitConstants.MessageOption.MESSAGE_INFORMATION,
        UIKitConstants.MessageOption.REPORT,
        UIKitConstants.MessageOption.MESSAGE_PRIVATELY
    )

    /**
     * Default options for a meeting message (category `custom`, type `meeting`).
     *
     * The custom-message set minus REPORT, which keeps strict v5 parity: there the meet bubble took
     * `getCommonOptions`, and that only added Report for category `message`, so a meeting message
     * never offered it. Derived from [customMessageDefaultOptions] rather than spelled out, so a
     * future addition to the custom set reaches meetings too — REPORT stays the single difference.
     */
    private val meetingMessageDefaultOptions: List<String> =
        customMessageDefaultOptions.filterNot { it == UIKitConstants.MessageOption.REPORT }

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
        // A meeting message takes the custom set without Report — see [meetingMessageDefaultOptions].
        if (isMeetingMessage(category, type)) {
            return meetingMessageDefaultOptions
        }
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
     * A meeting message — category `custom`, type `meeting` — is the direct-call / meet bubble.
     *
     * It long-presses like any other custom message: the bubble's Join button is an extra
     * affordance, not its only one, which is how v5 behaved. [getDefaultOptionIds] routes it
     * through [meetingMessageDefaultOptions] — the custom set minus Report — and the kits'
     * `InternalContentRenderer` uses this same predicate to draw the bubble with minimal slots.
     *
     * @param category the message category
     * @param type the message type
     */
    fun isMeetingMessage(category: String?, type: String?): Boolean =
        category.equals(UIKitConstants.MessageCategory.CUSTOM, ignoreCase = true) &&
            type.equals(UIKitConstants.MessageType.MEETING, ignoreCase = true)

    /** @see isMeetingMessage */
    fun isMeetingMessage(message: BaseMessage?): Boolean =
        message != null && isMeetingMessage(message.category, message.type)

    /**
     * Returns the list of [CometChatMessageOption] objects for a message,
     * applying business rules to filter options based on context.
     *
     * @param context Android context for accessing resources
     * @param message The message for which to get options
     * @param user The user in a 1-on-1 conversation (null for group conversations)
     * @param group The group in a group conversation (null for 1-on-1 conversations)
     * @param isThreadView Whether the message is being viewed in a thread
     * @param threadSubscribed Overrides the thread-subscription state the option renders with, for a
     *   caller holding a fresher value than the message itself — in a thread view the parent message
     *   is authoritative for all of its replies, which are fetched without the flag.
     * @return List of message options filtered by business rules
     */
    @JvmOverloads
    fun getDefaultMessageOptions(
        context: Context,
        message: BaseMessage,
        user: User?,
        group: Group?,
        isThreadView: Boolean = false,
        threadSubscribed: Boolean? = null
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
        // Admin/Moderator/Owner. The owner is checked explicitly because a group owner's scope is
        // not always reported as "admin", which otherwise hid admin-only options from them.
        val isGroupAdmin = group?.let {
            it.scope == com.cometchat.chat.constants.CometChatConstants.SCOPE_ADMIN ||
                it.scope == com.cometchat.chat.constants.CometChatConstants.SCOPE_MODERATOR ||
                (loggedInUser != null && it.owner == loggedInUser.uid)
        } ?: false

        return filteredOptionIds.mapNotNull { optionId ->
            createMessageOption(
                context = context,
                optionId = optionId,
                message = message,
                isMyMessage = isMyMessage,
                isGroupAdmin = isGroupAdmin,
                isInGroup = group != null,
                isThreadView = isThreadView,
                threadSubscribed = threadSubscribed
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
        isThreadView: Boolean,
        threadSubscribed: Boolean? = null
    ): CometChatMessageOption? {
        return when (optionId) {
            UIKitConstants.MessageOption.REPLY_IN_THREAD -> {
                if (isThreadView || message.parentMessageId != 0L) null
                else replyInThread(context)
            }
            UIKitConstants.MessageOption.THREAD_SUBSCRIPTION -> {
                // Offered in BOTH 1-1 and group conversations (ENG-38903). Threads exist in a 1-1
                // too, so the reply-notification control belongs wherever REPLY_IN_THREAD is —
                // there is no sender/receiver gate either, so it shows on messages you sent as
                // well as ones you received. The thread-header bell follows the same rule via
                // [CometChatThreadSubscription.isAvailableForThread].
                // Still hidden on the ineligible categories (see [isThreadSubscriptionEligible])
                // and by the (default-off) feature gate. Otherwise shown on every message — a root
                // targets its own thread, a reply its parent thread — and never gated on replyCount.
                if (!isThreadSubscriptionEligible(message.category) ||
                    !CometChatUIKit.isThreadSubscriptionEnabled()
                ) null
                else threadSubscription(
                    context,
                    message,
                    threadSubscribed ?: message.isThreadSubscribed()
                )
            }
            UIKitConstants.MessageOption.PIN -> {
                // Conversation-wide pin. Shown only when NOT already pinned (else UNPIN shows).
                // Product rule: the option is visible to EVERYONE — participants included, no
                // client-side role gate. Enforcement is the server's (RBAC): if the user lacks
                // permission the pin call fails with ERR_PERMISSION_DENIED and the UI surfaces a
                // "you don't have permission" toast.
                if (!CometChatUIKit.isPinMessageEnabled() || isPinSaveIneligible(message) || message.isPinned) null
                else pin(context)
            }
            UIKitConstants.MessageOption.UNPIN -> {
                if (!CometChatUIKit.isPinMessageEnabled() || isPinSaveIneligible(message) || !message.isPinned) null
                else unpin(context)
            }
            UIKitConstants.MessageOption.SAVE -> {
                // Per-user private save. No role gating. Shown only when NOT already saved.
                if (!CometChatUIKit.isSaveMessageEnabled() || isPinSaveIneligible(message) || message.isSaved) null
                else save(context)
            }
            UIKitConstants.MessageOption.UNSAVE -> {
                if (!CometChatUIKit.isSaveMessageEnabled() || isPinSaveIneligible(message) || !message.isSaved) null
                else unsave(context)
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

    /**
     * Creates the thread-subscription (mute / unmute reply notifications) message option.
     *
     * <p>Title and icon flip on the subscription state read off the message itself: subscribed →
     * bell-off icon + "Unsubscribe from thread" (tapping unsubscribes); not subscribed → bell icon +
     * "Subscribe to thread" (tapping subscribes). The state is the server's flag as it arrived, so a
     * message that never carried one (a socket delivery) renders as the un-subscribed affordance.
     * The subscribe/unsubscribe action itself, with the optimistic flip, is wired at the message-list
     * layer keyed on the option id.
     *
     * @param subscribed Overrides the message's own flag when the caller holds a fresher value —
     *   typically the thread's parent message, which is authoritative for all of its replies.
     */
    @JvmOverloads
    fun threadSubscription(
        context: Context,
        message: BaseMessage,
        subscribed: Boolean = message.isThreadSubscribed()
    ): CometChatMessageOption {
        return CometChatMessageOption(
            id = UIKitConstants.MessageOption.THREAD_SUBSCRIPTION,
            title = context.getString(
                if (subscribed) R.string.cometchat_thread_subscription_unsubscribe
                else R.string.cometchat_thread_subscription_subscribe
            ),
            // Action-labelled: the icon reflects what tapping does. "Stop reply notifications" (when
            // subscribed) shows the crossed bell; "Notify me about replies" (when not) shows the plain bell.
            icon = if (subscribed) R.drawable.cometchat_ic_notifications_off
            else R.drawable.cometchat_ic_notifications
        )
    }

    /** The id of the thread a message belongs to: its own id if a root, else its parentMessageId. */
    fun threadRootId(message: BaseMessage): Long =
        CometChatThreadSubscription.threadRootId(message)

    /** Categories the thread-subscription toggle is not offered on — see [isThreadSubscriptionEligible]. */
    private val threadSubscriptionIneligibleCategories = setOf(
        UIKitConstants.MessageCategory.INTERACTIVE,
        UIKitConstants.MessageCategory.ACTION,
        UIKitConstants.MessageCategory.CALL
    )

    /**
     * Whether the thread-subscription toggle should be offered on a message of [category].
     *
     * Excluded for two different reasons, both leading to the same place:
     * - **action / call** are system messages; no thread is created for them, so following one is
     *   meaningless.
     * - **interactive** is retired at the product level — no longer sent, though the SDK still
     *   carries the type. Independently of that, the SDK does not stamp `threadSubscribed` on the
     *   interactive parse path, so the option could only ever render "Subscribe" regardless of the
     *   real state. That is the same unknowable-state reasoning that keeps the option off the pinned
     *   panel, and it is why the exclusion is sound even for a legacy interactive message that does
     *   have a thread.
     *
     * This mainly guards the option's *fallback* path: an unrecognised `category_type` key resolves
     * to the custom-message option set, which does include THREAD_SUBSCRIPTION. An unknown category
     * stays eligible, matching that permissive fallback.
     */
    fun isThreadSubscriptionEligible(category: String?): Boolean =
        category?.lowercase() !in threadSubscriptionIneligibleCategories

    /**
     * Pin/Save are not offered on a deleted or not-yet-sent message. (Action-category messages
     * never reach the options map, and moderation-pending/disapproved are already restricted
     * upstream in [getDefaultMessageOptions].)
     */
    private fun isPinSaveIneligible(message: BaseMessage): Boolean =
        message.deletedAt > 0 || message.id == 0L

    /** Creates a "Pin message" option. */
    fun pin(context: Context): CometChatMessageOption {
        return CometChatMessageOption(
            id = UIKitConstants.MessageOption.PIN,
            title = context.getString(R.string.cometchat_pin),
            icon = R.drawable.cometchat_ic_pin
        )
    }

    /** Creates an "Unpin message" option. */
    fun unpin(context: Context): CometChatMessageOption {
        return CometChatMessageOption(
            id = UIKitConstants.MessageOption.UNPIN,
            title = context.getString(R.string.cometchat_unpin),
            icon = R.drawable.cometchat_ic_pin_off
        )
    }

    /** Creates a "Save message" option. */
    fun save(context: Context): CometChatMessageOption {
        return CometChatMessageOption(
            id = UIKitConstants.MessageOption.SAVE,
            title = context.getString(R.string.cometchat_save),
            icon = R.drawable.cometchat_ic_bookmark
        )
    }

    /** Creates an "Unsave message" option. */
    fun unsave(context: Context): CometChatMessageOption {
        return CometChatMessageOption(
            id = UIKitConstants.MessageOption.UNSAVE,
            title = context.getString(R.string.cometchat_unsave),
            icon = R.drawable.cometchat_ic_bookmark_filled
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
