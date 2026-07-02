package com.cometchat.uikit.compose.presentation.search.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.models.Conversation
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.User
import com.cometchat.uikit.compose.R
import com.cometchat.uikit.compose.presentation.search.style.SearchConversationItemStyle
import com.cometchat.uikit.compose.presentation.shared.baseelements.avatar.CometChatAvatar
import com.cometchat.uikit.compose.presentation.shared.baseelements.badgecount.CometChatBadgeCount
import com.cometchat.uikit.compose.presentation.shared.interfaces.DateTimeFormatterCallback
import com.cometchat.uikit.compose.presentation.shared.receipts.CometChatReceipts
import com.cometchat.uikit.compose.presentation.shared.receipts.MessageReceiptUtils
import com.cometchat.uikit.compose.presentation.shared.statusindicator.CometChatStatusIndicator
import com.cometchat.uikit.compose.presentation.shared.statusindicator.StatusIndicator
import com.cometchat.uikit.compose.presentation.shared.formatters.CometChatTextFormatter
import com.cometchat.uikit.compose.presentation.shared.formatters.FormatterUtils
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.CometChatUIKit
import com.cometchat.uikit.core.utils.AgentChatDetector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private const val AVATAR_SIZE = 48
private const val STATUS_INDICATOR_SIZE = 14

/**
 * Dimension constants for SearchConversationItem matching the reference implementation.
 * 
 * Reference source values (uikit-android-v5/chatuikit):
 * - cometchat_padding_4 = 16dp (horizontal padding - from cometchat_spacing_4)
 * - cometchat_padding_3 = 12dp (vertical padding - from cometchat_spacing_3)
 * - cometchat_margin_3 = 12dp (leading spacer after avatar - from cometchat_spacing_3)
 * - cometchat_margin_4 = 16dp (trailing spacer before tail view - from cometchat_spacing_4)
 */
private object ConversationItemDimens {
    val horizontalPadding = 16.dp  // cometchat_padding_4
    val verticalPadding = 12.dp    // cometchat_padding_3
    val leadingSpacer = 12.dp      // cometchat_margin_3
    val trailingSpacer = 16.dp     // cometchat_margin_4
}

/**
 * A composable that displays a conversation search result item.
 */
@Composable
fun SearchConversationItem(
    conversation: Conversation,
    onClick: (Conversation) -> Unit,
    modifier: Modifier = Modifier,
    style: SearchConversationItemStyle = SearchConversationItemStyle.default(),
    dateTimeFormatter: DateTimeFormatterCallback? = null,
    textFormatters: List<CometChatTextFormatter> = emptyList(),
    leadingView: (@Composable (Conversation) -> Unit)? = null,
    titleView: (@Composable (Conversation) -> Unit)? = null,
    subtitleView: (@Composable (Conversation) -> Unit)? = null,
    trailingView: (@Composable (Conversation) -> Unit)? = null
) {
    val conversationName = getConversationTitle(conversation)
    val accessibilityDescription = buildAccessibilityDescription(conversation, conversationName)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(style.backgroundColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(),
                onClick = { onClick(conversation) }
            )
            .padding(horizontal = ConversationItemDimens.horizontalPadding, vertical = ConversationItemDimens.verticalPadding)
            .semantics {
                contentDescription = accessibilityDescription
                role = Role.Button
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leadingView != null) {
            leadingView(conversation)
        } else {
            DefaultLeadingView(conversation = conversation, style = style)
        }

        Spacer(modifier = Modifier.width(ConversationItemDimens.leadingSpacer))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            if (titleView != null) {
                titleView(conversation)
            } else {
                DefaultTitleView(conversation = conversation, style = style)
            }

            Spacer(modifier = Modifier.size(2.dp))

            if (subtitleView != null) {
                subtitleView(conversation)
            } else {
                DefaultSubtitleView(conversation = conversation, style = style, textFormatters = textFormatters)
            }
        }

        Spacer(modifier = Modifier.width(ConversationItemDimens.trailingSpacer))

        if (trailingView != null) {
            trailingView(conversation)
        } else {
            DefaultTrailingView(conversation = conversation, style = style, dateTimeFormatter = dateTimeFormatter)
        }
    }
}

@Composable
private fun DefaultLeadingView(
    conversation: Conversation,
    style: SearchConversationItemStyle
) {
    Box(
        modifier = Modifier.size(AVATAR_SIZE.dp),
        contentAlignment = Alignment.Center
    ) {
        CometChatAvatar(
            modifier = Modifier.size(AVATAR_SIZE.dp),
            name = getConversationTitle(conversation),
            avatarUrl = getConversationAvatar(conversation),
            style = style.avatarStyle
        )

        val statusIndicator = getStatusIndicator(conversation)
        if (statusIndicator != StatusIndicator.OFFLINE) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(STATUS_INDICATOR_SIZE.dp)
            ) {
                CometChatStatusIndicator(
                    status = statusIndicator,
                    style = style.statusIndicatorStyle
                )
            }
        }
    }
}

@Composable
private fun DefaultTitleView(
    conversation: Conversation,
    style: SearchConversationItemStyle
) {
    Text(
        text = getConversationTitle(conversation),
        color = style.titleTextColor,
        style = style.titleTextStyle,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
private fun DefaultSubtitleView(
    conversation: Conversation,
    style: SearchConversationItemStyle,
    textFormatters: List<CometChatTextFormatter> = emptyList()
) {
    val context = LocalContext.current
    val lastMessage = conversation.lastMessage

    // For text messages with formatters, use rich text
    val subtitle: AnnotatedString = if (lastMessage is com.cometchat.chat.models.TextMessage && textFormatters.isNotEmpty() && !lastMessage.text.isNullOrEmpty()) {
        val formatted = FormatterUtils.getFormattedText(
            context = context,
            baseMessage = lastMessage,
            formattingType = UIKitConstants.FormattingType.CONVERSATIONS,
            alignment = UIKitConstants.MessageBubbleAlignment.LEFT,
            text = lastMessage.text,
            formatters = textFormatters
        )
        // Add sender prefix for group conversations
        val prefix = getSenderPrefix(conversation, lastMessage, context)
        if (prefix.isNotEmpty()) {
            buildAnnotatedString {
                append(prefix)
                append(formatted)
            }
        } else {
            formatted
        }
    } else {
        AnnotatedString(getLastMessagePreview(conversation))
    }

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Receipt icon for outgoing messages (matching Java SubtitleView)
        if (lastMessage != null && !MessageReceiptUtils.shouldHideReceipt(lastMessage)) {
            val receipt = MessageReceiptUtils.getMessageReceipt(lastMessage)
            CometChatReceipts(
                receipt = receipt,
                style = style.receiptStyle
            )
            Spacer(modifier = Modifier.width(4.dp))
        }

        Text(
            text = subtitle,
            color = style.subtitleTextColor,
            style = style.subtitleTextStyle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun DefaultTrailingView(
    conversation: Conversation,
    style: SearchConversationItemStyle,
    dateTimeFormatter: DateTimeFormatterCallback? = null
) {
    val yesterdayText = stringResource(R.string.cometchat_yesterday)
    
    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.Center
    ) {
        val timestamp = conversation.updatedAt
        if (timestamp > 0) {
            Text(
                text = formatTimestamp(timestamp, yesterdayText, dateTimeFormatter),
                color = style.timestampTextColor,
                style = style.timestampTextStyle,
                maxLines = 1
            )
        }

        val unreadCount = conversation.unreadMessageCount
        val user = conversation.conversationWith as? User
        val isAgentChat = user != null && AgentChatDetector.isAgentChat(user)
        if (unreadCount > 0 && !isAgentChat) {
            Spacer(modifier = Modifier.size(4.dp))
            CometChatBadgeCount(
                count = unreadCount,
                style = style.badgeStyle
            )
        }
    }
}

private fun getConversationTitle(conversation: Conversation): String {
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

private fun getConversationAvatar(conversation: Conversation): String? {
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

private fun getStatusIndicator(conversation: Conversation): StatusIndicator {
    return when (conversation.conversationType) {
        CometChatConstants.CONVERSATION_TYPE_USER -> {
            val user = conversation.conversationWith as? User
            if (user?.status == CometChatConstants.USER_STATUS_ONLINE) {
                // Check for blocked user before showing ONLINE (matching reference)
                if (user.isBlockedByMe || user.isHasBlockedMe) {
                    StatusIndicator.OFFLINE
                } else {
                    StatusIndicator.ONLINE
                }
            } else {
                StatusIndicator.OFFLINE
            }
        }
        CometChatConstants.CONVERSATION_TYPE_GROUP -> {
            val group = conversation.conversationWith as? Group
            when (group?.groupType) {
                CometChatConstants.GROUP_TYPE_PRIVATE -> StatusIndicator.PRIVATE_GROUP
                CometChatConstants.GROUP_TYPE_PASSWORD -> StatusIndicator.PROTECTED_GROUP
                else -> StatusIndicator.PUBLIC_GROUP
            }
        }
        else -> StatusIndicator.OFFLINE
    }
}

private fun getLastMessagePreview(conversation: Conversation): String {
    val lastMessage = conversation.lastMessage ?: return ""

    // Handle deleted messages
    if (lastMessage.deletedAt > 0) {
        return "This message was deleted"
    }

    val messageText = when (lastMessage) {
        is com.cometchat.chat.models.TextMessage -> {
            val text = lastMessage.text
            if (!text.isNullOrEmpty()) text else "This message was deleted"
        }
        is com.cometchat.chat.models.MediaMessage -> {
            when (lastMessage.type) {
                CometChatConstants.MESSAGE_TYPE_IMAGE -> {
                    val attachment = lastMessage.attachment
                    if (attachment?.toString()?.contains(".gif") == true) "GIF" else "Photo"
                }
                CometChatConstants.MESSAGE_TYPE_VIDEO -> "Video"
                CometChatConstants.MESSAGE_TYPE_AUDIO -> "Audio"
                CometChatConstants.MESSAGE_TYPE_FILE -> "Document"
                else -> "Document"
            }
        }
        is com.cometchat.chat.models.CustomMessage -> {
            val conversationText = lastMessage.conversationText
            if (!conversationText.isNullOrEmpty()) {
                conversationText
            } else {
                when (lastMessage.type) {
                    "extension_poll" -> "Poll"
                    "extension_sticker" -> "Sticker"
                    "location" -> "Location"
                    "extension_document" -> "Collaborative Document"
                    "extension_whiteboard" -> "Collaborative Whiteboard"
                    else -> {
                        lastMessage.metadata?.optString("pushNotification")?.takeIf { it.isNotEmpty() }
                            ?: lastMessage.type ?: ""
                    }
                }
            }
        }
        is com.cometchat.chat.core.Call -> {
            getCallStatusText(lastMessage)
        }
        is com.cometchat.chat.models.Action -> {
            getActionMessageText(lastMessage)
        }
        is com.cometchat.chat.models.InteractiveMessage -> {
            "This message type is not supported"
        }
        else -> ""
    }

    // Add sender prefix for group conversations (matching reference)
    if (conversation.conversationType == CometChatConstants.CONVERSATION_TYPE_GROUP && messageText.isNotEmpty()) {
        // Don't add prefix for Action messages — they already contain the actor name
        if (lastMessage is com.cometchat.chat.models.Action) return messageText

        val sender = lastMessage.sender
        if (sender != null) {
            val currentUser = try {
                CometChatUIKit.getLoggedInUser()
            } catch (e: Exception) {
                null
            }
            val senderName = if (currentUser != null && sender.uid == currentUser.uid) {
                "You"
            } else {
                sender.name ?: ""
            }
            return if (senderName.isNotEmpty()) "$senderName: $messageText" else messageText
        }
    }

    return messageText
}

private fun getCallStatusText(call: com.cometchat.chat.core.Call): String {
    val callType = if (call.type == CometChatConstants.CALL_TYPE_VIDEO) "Video" else "Voice"
    return when (call.callStatus) {
        "unanswered", "cancelled" -> "Missed $callType Call"
        "rejected" -> "Rejected $callType Call"
        "busy" -> "Busy"
        "ended" -> "$callType Call"
        "ongoing" -> "$callType Call"
        "initiated" -> "$callType Call"
        else -> "$callType Call"
    }
}

private fun getActionMessageText(action: com.cometchat.chat.models.Action): String {
    return action.message ?: action.action ?: ""
}

/**
 * Gets the sender prefix for group conversations (e.g., "You: " or "SenderName: ").
 * Returns empty string for non-group conversations.
 */
private fun getSenderPrefix(conversation: Conversation, message: com.cometchat.chat.models.BaseMessage, context: android.content.Context): String {
    if (conversation.conversationType != CometChatConstants.CONVERSATION_TYPE_GROUP) return ""
    val sender = message.sender ?: return ""
    val currentUser = try { CometChatUIKit.getLoggedInUser() } catch (e: Exception) { null }
    val senderName = if (currentUser != null && sender.uid == currentUser.uid) {
        "You"
    } else {
        sender.name ?: ""
    }
    return if (senderName.isNotEmpty()) "$senderName: " else ""
}

/**
 * Formats a timestamp using the DAY_DATE_TIME pattern matching the reference implementation.
 * 
 * Pattern behavior:
 * - Today: Show time (e.g., "3:45 PM")
 * - Yesterday: Show "Yesterday" (localized)
 * - Last 7 days: Show day name (e.g., "Monday")
 * - Older: Show date (e.g., "15 Jan 2024")
 * 
 * @param timestamp Unix timestamp in seconds
 * @param yesterdayText Localized "Yesterday" text
 * @return Formatted date string
 */
private fun formatTimestamp(
    timestamp: Long,
    yesterdayText: String,
    dateTimeFormatter: DateTimeFormatterCallback? = null
): String {
    val timeInMillis = timestamp * 1000
    val date = Date(timeInMillis)
    val now = Calendar.getInstance()
    val timeToCheck = Calendar.getInstance(Locale.ENGLISH).apply {
        this.timeInMillis = timeInMillis
    }

    return when {
        // Today - show time
        now.get(Calendar.DAY_OF_YEAR) == timeToCheck.get(Calendar.DAY_OF_YEAR) &&
        now.get(Calendar.YEAR) == timeToCheck.get(Calendar.YEAR) -> {
            dateTimeFormatter?.time(timeInMillis)
                ?: SimpleDateFormat("h:mm a", Locale.getDefault()).format(date)
        }
        // Yesterday - show localized "Yesterday"
        (now.get(Calendar.DAY_OF_YEAR) - 1) == timeToCheck.get(Calendar.DAY_OF_YEAR) &&
        now.get(Calendar.YEAR) == timeToCheck.get(Calendar.YEAR) -> {
            dateTimeFormatter?.yesterday(timeInMillis) ?: yesterdayText
        }
        // Last 7 days - show day name
        (now.get(Calendar.DAY_OF_YEAR) - 7) <= timeToCheck.get(Calendar.DAY_OF_YEAR) &&
        now.get(Calendar.YEAR) == timeToCheck.get(Calendar.YEAR) -> {
            dateTimeFormatter?.lastWeek(timeInMillis)
                ?: SimpleDateFormat("EEE", Locale.getDefault()).format(date)
        }
        // Older - show date
        else -> {
            dateTimeFormatter?.otherDays(timeInMillis)
                ?: SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(date)
        }
    }
}

private fun buildAccessibilityDescription(
    conversation: Conversation,
    conversationName: String
): String {
    return buildString {
        append(conversationName)
        val lastMessage = getLastMessagePreview(conversation)
        if (lastMessage.isNotEmpty()) {
            append(", ")
            append(lastMessage)
        }
        if (conversation.unreadMessageCount > 0) {
            append(", ")
            append("${conversation.unreadMessageCount} unread messages")
        }
    }
}
