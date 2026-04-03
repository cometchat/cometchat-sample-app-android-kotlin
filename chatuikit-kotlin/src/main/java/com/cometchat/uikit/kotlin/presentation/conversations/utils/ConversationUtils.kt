package com.cometchat.uikit.kotlin.presentation.conversations.utils

import android.content.Context
import androidx.annotation.DrawableRes
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.Call
import com.cometchat.chat.models.Action
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Conversation
import com.cometchat.chat.models.CustomMessage
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.InteractiveMessage
import com.cometchat.chat.models.MediaMessage
import com.cometchat.chat.models.TextMessage
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.CometChatUIKit
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.presentation.shared.utils.CallsUtils
import com.cometchat.uikit.kotlin.shared.formatters.CometChatTextFormatter
import com.cometchat.uikit.kotlin.shared.formatters.FormatterUtils
import org.json.JSONObject
import java.util.Locale

/**
 * Utility object for conversation-related operations.
 *
 * Provides helper methods for extracting and formatting conversation data
 * for display in the conversation list item.
 */
object ConversationUtils {

    /**
     * Returns a formatted string representation of the last message.
     * Handles different message types and returns appropriate placeholder text.
     *
     * @param context Android context for accessing resources
     * @param message The BaseMessage to format
     * @return Formatted string for display in subtitle
     */
    fun getLastMessageText(context: Context, message: BaseMessage?): String {
        if (message == null) {
            return context.getString(R.string.cometchat_start_conv_hint)
        }

        // Check if message is deleted
        if (message.deletedAt > 0) {
            return context.getString(R.string.cometchat_this_message_deleted)
        }

        return when (message) {
            is TextMessage -> getTextMessageText(context, message)
            is MediaMessage -> getMediaMessageText(context, message)
            is CustomMessage -> getCustomMessageText(context, message)
            is Call -> CallsUtils.getCallStatus(context, message)
            is InteractiveMessage -> context.getString(R.string.cometchat_this_message_type_is_not_supported)
            is Action -> getActionMessageText(context, message)
            else -> context.getString(R.string.cometchat_start_conv_hint)
        }
    }

    /**
     * Returns the drawable resource ID for the message type icon.
     * Returns null for DEFAULT message type (plain text without special icon).
     *
     * @param message The BaseMessage to get icon for
     * @return Drawable resource ID or null for plain text messages
     */
    @DrawableRes
    fun getLastMessageIcon(message: BaseMessage?): Int? {
        if (message == null) return null

        // Check if message is deleted
        if (message.deletedAt > 0) {
            return R.drawable.cometchat_ic_conversations_deleted_message
        }

        // Check if message is a thread reply
        if (message.parentMessageId != 0L) {
            return R.drawable.cometchat_ic_conversations_thread
        }

        return when (message) {
            is TextMessage -> getTextMessageIcon(message)
            is MediaMessage -> getMediaMessageIcon(message)
            is CustomMessage -> getCustomMessageIcon(message)
            is Call -> getCallIcon(message)
            is InteractiveMessage -> R.drawable.cometchat_ic_conversations_deleted_message // Not supported
            is Action -> null // Action messages don't have icons
            else -> null
        }
    }


    /**
     * Returns the sender name prefix for group messages.
     * Returns "You: " for messages sent by current user.
     * Returns empty string for user-to-user conversations.
     * Returns empty string for Action messages (group actions already contain actor name).
     *
     * @param context Android context for accessing resources
     * @param message The BaseMessage to get prefix for
     * @return Sender name prefix (e.g., "John: " or "You: ") or empty string
     */
    fun getMessagePrefix(context: Context, message: BaseMessage): String {
        // Only show prefix for group messages
        if (message.receiverType != CometChatConstants.RECEIVER_TYPE_GROUP) {
            return ""
        }

        // Don't show prefix for Action messages - they already contain the actor name
        // (e.g., "John joined", "John left", "John kicked Mary")
        if (message is Action) {
            return ""
        }

        val sender = message.sender ?: return ""

        // Safely get logged-in user, return sender name if SDK not initialized (preview mode)
        val currentUser = try {
            CometChatUIKit.getLoggedInUser()
        } catch (e: Exception) {
            null
        }

        return if (currentUser != null && sender.uid == currentUser.uid) {
            "${context.getString(R.string.cometchat_you)}: "
        } else {
            "${sender.name}: "
        }
    }

    /**
     * Returns the complete formatted subtitle text with prefix and message text concatenated.
     * This is the main method to use for displaying conversation subtitles in the UI.
     *
     * @param context Android context for accessing resources
     * @param message The BaseMessage to format
     * @return Complete formatted string with prefix (for group messages) + message text
     */
    fun getFormattedSubtitleText(context: Context, message: BaseMessage?): String {
        if (message == null) {
            return context.getString(R.string.cometchat_start_conv_hint)
        }

        val prefix = getMessagePrefix(context, message)
        val messageText = getLastMessageText(context, message)

        return prefix + messageText
    }

    /**
     * Returns the formatted last message text with text formatters applied.
     * This method applies text formatters (like mentions) to TextMessages and falls back
     * to plain text for other message types.
     *
     * Use this method when you need styled/formatted text (e.g., with mention highlighting).
     * Use [getLastMessageText] when you only need plain text.
     *
     * @param context Android context for accessing resources
     * @param message The BaseMessage to format
     * @param textFormatters List of text formatters to apply (e.g., CometChatMentionsFormatter)
     * @return Formatted CharSequence with styling applied for TextMessages, plain String for others
     */
    fun getFormattedLastMessageText(
        context: Context,
        message: BaseMessage?,
        textFormatters: List<CometChatTextFormatter>
    ): CharSequence {
        if (message == null) {
            return context.getString(R.string.cometchat_start_conv_hint)
        }

        // Only apply text formatters if message is a TextMessage, not deleted, has valid text, and formatters are available
        return if (message is TextMessage &&
            message.deletedAt == 0L &&
            !message.text.isNullOrEmpty() &&
            textFormatters.isNotEmpty()
        ) {
            // Apply text formatters using FormatterUtils
            FormatterUtils.getFormattedText(
                context = context,
                baseMessage = message,
                formattingType = UIKitConstants.FormattingType.CONVERSATIONS,
                alignment = UIKitConstants.MessageBubbleAlignment.LEFT,
                text = message.text,
                formatters = textFormatters
            )
        } else {
            // Use plain text for all other message types (handles deleted, media, custom, call, action, etc.)
            getLastMessageText(context, message)
        }
    }

    /**
     * Returns the conversation title (name of user or group).
     *
     * @param conversation The conversation to get the title from
     * @return The name of the user or group
     */
    fun getConversationTitle(conversation: Conversation): String {
        return when (conversation.conversationType) {
            CometChatConstants.CONVERSATION_TYPE_USER -> {
                (conversation.conversationWith as? User)?.name ?: ""
            }
            CometChatConstants.CONVERSATION_TYPE_GROUP -> {
                (conversation.conversationWith as? Group)?.name ?: ""
            }
            else -> ""
        }
    }

    /**
     * Returns the conversation avatar URL.
     *
     * @param conversation The conversation to get the avatar from
     * @return The avatar URL of the user or group icon
     */
    fun getConversationAvatar(conversation: Conversation): String? {
        return when (conversation.conversationType) {
            CometChatConstants.CONVERSATION_TYPE_USER -> {
                (conversation.conversationWith as? User)?.avatar
            }
            CometChatConstants.CONVERSATION_TYPE_GROUP -> {
                (conversation.conversationWith as? Group)?.icon
            }
            else -> null
        }
    }

    /**
     * Returns the User object if the conversation is with a user.
     *
     * @param conversation The conversation
     * @return The User object or null if it's a group conversation
     */
    fun getUser(conversation: Conversation): User? {
        return if (conversation.conversationType == CometChatConstants.CONVERSATION_TYPE_USER) {
            conversation.conversationWith as? User
        } else null
    }

    /**
     * Returns the Group object if the conversation is with a group.
     *
     * @param conversation The conversation
     * @return The Group object or null if it's a user conversation
     */
    fun getGroup(conversation: Conversation): Group? {
        return if (conversation.conversationType == CometChatConstants.CONVERSATION_TYPE_GROUP) {
            conversation.conversationWith as? Group
        } else null
    }

    /**
     * Checks if the conversation is with a user.
     *
     * @param conversation The conversation to check
     * @return True if it's a user conversation, false otherwise
     */
    fun isUserConversation(conversation: Conversation): Boolean {
        return conversation.conversationType == CometChatConstants.CONVERSATION_TYPE_USER
    }

    /**
     * Checks if the conversation is with a group.
     *
     * @param conversation The conversation to check
     * @return True if it's a group conversation, false otherwise
     */
    fun isGroupConversation(conversation: Conversation): Boolean {
        return conversation.conversationType == CometChatConstants.CONVERSATION_TYPE_GROUP
    }

    // Private helper methods

    private fun getTextMessageText(context: Context, message: TextMessage): String {
        val text = message.text
        return if (!text.isNullOrEmpty()) {
            // TODO: Add profanity filter and data masking support via Extensions
            text
        } else {
            context.getString(R.string.cometchat_this_message_deleted)
        }
    }

    private fun getMediaMessageText(context: Context, message: MediaMessage): String {
        return when (message.type) {
            CometChatConstants.MESSAGE_TYPE_IMAGE -> {
                // Check if it's a GIF
                val attachment = message.attachment
                if (attachment?.toString()?.contains(UIKitConstants.MimeType.GIF_EXTENSION) == true) {
                    context.getString(R.string.cometchat_message_gif)
                } else {
                    context.getString(R.string.cometchat_message_image)
                }
            }
            CometChatConstants.MESSAGE_TYPE_VIDEO -> context.getString(R.string.cometchat_message_video)
            CometChatConstants.MESSAGE_TYPE_AUDIO -> context.getString(R.string.cometchat_message_audio)
            CometChatConstants.MESSAGE_TYPE_FILE -> context.getString(R.string.cometchat_message_document)
            else -> context.getString(R.string.cometchat_message_document)
        }
    }

    private fun getCustomMessageText(context: Context, message: CustomMessage): String {
        // Check for conversation text first
        val conversationText = message.conversationText
        if (!conversationText.isNullOrEmpty()) {
            return conversationText
        }

        return when (message.type) {
            UIKitConstants.MessageTemplateId.EXTENSION_POLL -> context.getString(R.string.cometchat_message_poll)
            UIKitConstants.MessageTemplateId.EXTENSION_STICKER -> context.getString(R.string.cometchat_message_sticker)
            UIKitConstants.MessageTemplateId.EXTENSION_LOCATION -> context.getString(R.string.cometchat_message_location)
            UIKitConstants.MessageTemplateId.EXTENSION_DOCUMENT -> context.getString(R.string.cometchat_message_collaborative_document)
            UIKitConstants.MessageTemplateId.EXTENSION_WHITEBOARD -> context.getString(R.string.cometchat_message_collaborative_whiteboard)
            UIKitConstants.MessageType.MEETING -> {
                // Handle meeting messages with sender name
                val senderPrefix = getMessagePrefix(context, message)
                val senderName = if (senderPrefix.isNotEmpty()) {
                    senderPrefix.substringBefore(":")
                } else ""

                if (senderName == context.getString(R.string.cometchat_you)) {
                    String.format(
                        Locale.US,
                        context.getString(R.string.cometchat_meeting_initiated_by_you),
                        senderName
                    )
                } else {
                    String.format(
                        Locale.US,
                        context.getString(R.string.cometchat_meeting_initiated_by_others),
                        senderName
                    )
                }
            }
            else -> {
                // Check for pushNotification in metadata
                message.metadata?.optString("pushNotification")?.takeIf { it.isNotEmpty() }
                    ?: message.type ?: context.getString(R.string.cometchat_this_message_deleted)
            }
        }
    }


    private fun getActionMessageText(context: Context, action: Action): String {
        if (action.action.equals(CometChatConstants.ActionKeys.ACTION_TYPE_GROUP_MEMBER, ignoreCase = true)) {
            val actionBy = action.actionBy as? User
            val actionOn = action.actionOn as? User

            return when (action.action) {
                CometChatConstants.ActionKeys.ACTION_JOINED -> {
                    "${actionBy?.name} ${context.getString(R.string.cometchat_joined)}"
                }
                CometChatConstants.ActionKeys.ACTION_LEFT -> {
                    "${actionBy?.name} ${context.getString(R.string.cometchat_left)}"
                }
                CometChatConstants.ActionKeys.ACTION_KICKED -> {
                    "${actionBy?.name} ${context.getString(R.string.cometchat_kicked_by)} ${actionOn?.name}"
                }
                CometChatConstants.ActionKeys.ACTION_BANNED -> {
                    "${actionBy?.name} ${context.getString(R.string.cometchat_banned)} ${actionOn?.name}"
                }
                CometChatConstants.ActionKeys.ACTION_UNBANNED -> {
                    "${actionBy?.name} ${context.getString(R.string.cometchat_unban)} ${actionOn?.name}"
                }
                CometChatConstants.ActionKeys.ACTION_MEMBER_ADDED -> {
                    "${actionBy?.name} ${context.getString(R.string.cometchat_added)} ${actionOn?.name}"
                }
                CometChatConstants.ActionKeys.ACTION_SCOPE_CHANGED -> {
                    val newScope = action.newScope
                    val scopeText = when (newScope?.lowercase()) {
                        CometChatConstants.SCOPE_MODERATOR -> context.getString(R.string.cometchat_moderator)
                        CometChatConstants.SCOPE_ADMIN -> context.getString(R.string.cometchat_admin)
                        CometChatConstants.SCOPE_PARTICIPANT -> context.getString(R.string.cometchat_participant)
                        else -> newScope ?: ""
                    }
                    "${actionBy?.name} ${context.getString(R.string.cometchat_made)} ${actionOn?.name} $scopeText"
                }
                else -> action.message ?: context.getString(R.string.cometchat_this_message_deleted)
            }
        } else if (action.action.equals(CometChatConstants.ActionKeys.ACTION_TYPE_MESSAGE, ignoreCase = true)) {
            return when (action.action) {
                CometChatConstants.ActionKeys.ACTION_MESSAGE_EDITED ->
                    CometChatConstants.ActionMessages.ACTION_MESSAGE_EDITED_MESSAGE
                CometChatConstants.ActionKeys.ACTION_MESSAGE_DELETED ->
                    CometChatConstants.ActionMessages.ACTION_MESSAGE_DELETED_MESSAGE
                else -> action.message ?: ""
            }
        }

        return action.message ?: ""
    }

    /**
     * Returns icon for text messages (link detection).
     */
    @DrawableRes
    private fun getTextMessageIcon(message: TextMessage): Int? {
        // Check for links in metadata
        val metadata = message.metadata
        if (metadata != null && checkForLinks(metadata.toString())) {
            return R.drawable.cometchat_ic_conversations_link
        }
        // Plain text messages don't have icons
        return null
    }

    /**
     * Returns icon for media messages (image/GIF, video, audio, file).
     */
    @DrawableRes
    private fun getMediaMessageIcon(message: MediaMessage): Int? {
        return when (message.type) {
            CometChatConstants.MESSAGE_TYPE_IMAGE -> {
                // Check if it's a GIF
                val attachment = message.attachment
                if (attachment?.toString()?.contains(UIKitConstants.MimeType.GIF_EXTENSION) == true) {
                    R.drawable.cometchat_ic_conversations_gif
                } else {
                    R.drawable.cometchat_ic_conversations_photo
                }
            }
            CometChatConstants.MESSAGE_TYPE_VIDEO -> R.drawable.cometchat_ic_conversations_video
            CometChatConstants.MESSAGE_TYPE_AUDIO -> R.drawable.cometchat_ic_conversations_audio
            CometChatConstants.MESSAGE_TYPE_FILE -> R.drawable.cometchat_ic_conversations_document
            else -> null
        }
    }

    /**
     * Returns icon for custom messages (poll, sticker, location, etc.).
     */
    @DrawableRes
    private fun getCustomMessageIcon(message: CustomMessage): Int? {
        return when (message.type) {
            UIKitConstants.MessageTemplateId.EXTENSION_POLL -> R.drawable.cometchat_ic_conversations_poll
            UIKitConstants.MessageTemplateId.EXTENSION_STICKER -> R.drawable.cometchat_ic_conversations_stricker
            UIKitConstants.MessageTemplateId.EXTENSION_LOCATION -> R.drawable.cometchat_ic_conversations_location
            UIKitConstants.MessageTemplateId.EXTENSION_DOCUMENT -> R.drawable.cometchat_ic_conversations_collabrative_document
            UIKitConstants.MessageTemplateId.EXTENSION_WHITEBOARD -> R.drawable.cometchat_ic_conversations_collaborative_whiteboard
            UIKitConstants.MessageType.MEETING -> null // Meeting messages don't have icons
            else -> null
        }
    }

    /**
     * Returns icon for call messages (incoming/outgoing/missed voice/video).
     */
    @DrawableRes
    private fun getCallIcon(call: Call): Int? {
        val isVideoCall = CallsUtils.isVideoCall(call)
        val isCallInitiatedByMe = CallsUtils.isCallInitiatedByMe(call)
        val isMissed = CallsUtils.isMissedCall(call)

        return if (isVideoCall) {
            when {
                isCallInitiatedByMe -> R.drawable.cometchat_ic_conversations_outgoing_video_call
                isMissed -> R.drawable.cometchat_ic_conversations_missed_video_call
                else -> R.drawable.cometchat_ic_conversations_incoming_video_call
            }
        } else {
            when {
                isCallInitiatedByMe -> R.drawable.cometchat_ic_conversations_outgoing_voice_call
                isMissed -> R.drawable.cometchat_ic_conversations_missed_voice_call
                else -> R.drawable.cometchat_ic_conversations_incoming_voice_call
            }
        }
    }

    /**
     * Checks if text message metadata contains links.
     */
    private fun checkForLinks(jsonString: String): Boolean {
        return try {
            val jsonObject = JSONObject(jsonString)
            val injected = jsonObject.optJSONObject(UIKitConstants.JSONKeys.INJECTED)
            if (injected != null) {
                val extensions = injected.optJSONObject(UIKitConstants.JSONKeys.EXTENSIONS)
                if (extensions != null) {
                    val linkPreview = extensions.optJSONObject(UIKitConstants.JSONKeys.LINK_PREVIEW)
                    if (linkPreview != null) {
                        val links = linkPreview.optJSONArray(UIKitConstants.JSONKeys.LINKS)
                        links != null && links.length() > 0
                    } else false
                } else false
            } else false
        } catch (e: Exception) {
            false
        }
    }
}
