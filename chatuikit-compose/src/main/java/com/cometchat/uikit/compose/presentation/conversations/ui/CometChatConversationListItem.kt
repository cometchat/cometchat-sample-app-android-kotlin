package com.cometchat.uikit.compose.presentation.conversations.ui

import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.focusable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ripple
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Conversation
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.TextMessage
import com.cometchat.chat.models.User
import com.cometchat.uikit.compose.R
import com.cometchat.uikit.compose.presentation.conversations.utils.ConversationUtils
import com.cometchat.uikit.compose.presentation.conversations.utils.TypingIndicator
import com.cometchat.uikit.compose.presentation.conversations.style.CometChatConversationListItemStyle
import com.cometchat.uikit.compose.presentation.shared.baseelements.avatar.CometChatAvatar
import com.cometchat.uikit.compose.presentation.shared.baseelements.badgecount.CometChatBadgeCount
import com.cometchat.uikit.compose.presentation.shared.baseelements.date.CometChatDate
import com.cometchat.uikit.compose.presentation.shared.baseelements.date.Pattern
import com.cometchat.uikit.core.CometChatUIKit
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.compose.presentation.shared.formatters.CometChatTextFormatter
import com.cometchat.uikit.compose.presentation.shared.formatters.FormatterUtils
import com.cometchat.uikit.compose.presentation.shared.interfaces.DateTimeFormatterCallback
import com.cometchat.uikit.compose.presentation.shared.receipts.CometChatReceipts
import com.cometchat.uikit.compose.presentation.shared.receipts.Receipt
import com.cometchat.uikit.compose.presentation.shared.statusindicator.CometChatStatusIndicator
import com.cometchat.uikit.compose.presentation.shared.statusindicator.StatusIndicator

/**
 * Default leading view composable that displays the avatar with status indicator.
 *
 * @param conversation The conversation to display
 * @param typingIndicator Optional typing indicator for this conversation
 * @param hideUserStatus Whether to hide the user online/offline status indicator
 * @param hideGroupType Whether to hide the group type indicator
 * @param style Style configuration for the component
 */
@Composable
internal fun DefaultLeadingView(
    conversation: Conversation,
    typingIndicator: TypingIndicator?,
    hideUserStatus: Boolean,
    hideGroupType: Boolean,
    style: CometChatConversationListItemStyle
) {
    val avatarSize = 48.dp
    val statusIndicatorSize = style.statusIndicatorStyle.size
    
    Box(
        modifier = Modifier.size(avatarSize),
        contentAlignment = Alignment.Center
    ) {
        // Avatar
        CometChatAvatar(
            modifier = Modifier.size(avatarSize),
            name = ConversationUtils.getConversationTitle(conversation),
            avatarUrl = ConversationUtils.getConversationAvatar(conversation),
            style = style.avatarStyle
        )
        
        // Convert conversation to StatusIndicator enum
        val statusIndicator = getStatusIndicatorFromConversation(conversation, hideUserStatus, hideGroupType)
        
        // Status Indicator overlay at bottom-end (only render if not OFFLINE or PUBLIC_GROUP)
        if (statusIndicator != StatusIndicator.OFFLINE && statusIndicator != StatusIndicator.PUBLIC_GROUP) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(statusIndicatorSize)
            ) {
                CometChatStatusIndicator(
                    status = statusIndicator,
                    style = style.statusIndicatorStyle
                )
            }
        }
    }
}

/**
 * Converts a Conversation to a StatusIndicator enum value.
 *
 * @param conversation The conversation to convert
 * @param hideUserStatus Whether to hide user status (returns OFFLINE if true for user conversations)
 * @param hideGroupType Whether to hide group type (returns PUBLIC_GROUP if true for group conversations)
 * @return The appropriate StatusIndicator enum value
 */
private fun getStatusIndicatorFromConversation(
    conversation: Conversation,
    hideUserStatus: Boolean,
    hideGroupType: Boolean
): StatusIndicator {
    return when (conversation.conversationType) {
        CometChatConstants.CONVERSATION_TYPE_USER -> {
            if (hideUserStatus) {
                StatusIndicator.OFFLINE
            } else {
                val user = conversation.conversationWith as? User
                if (user?.status == CometChatConstants.USER_STATUS_ONLINE) {
                    StatusIndicator.ONLINE
                } else {
                    StatusIndicator.OFFLINE
                }
            }
        }
        CometChatConstants.CONVERSATION_TYPE_GROUP -> {
            if (hideGroupType) {
                StatusIndicator.PUBLIC_GROUP
            } else {
                val group = conversation.conversationWith as? Group
                when (group?.groupType) {
                    CometChatConstants.GROUP_TYPE_PRIVATE -> StatusIndicator.PRIVATE_GROUP
                    CometChatConstants.GROUP_TYPE_PASSWORD -> StatusIndicator.PROTECTED_GROUP
                    else -> StatusIndicator.PUBLIC_GROUP
                }
            }
        }
        else -> StatusIndicator.OFFLINE
    }
}


/**
 * Default title view composable that displays the conversation name.
 *
 * @param conversation The conversation to display
 * @param typingIndicator Optional typing indicator for this conversation
 * @param style Style configuration for the component
 */
@Composable
internal fun DefaultTitleView(
    conversation: Conversation,
    typingIndicator: TypingIndicator?,
    style: CometChatConversationListItemStyle
) {
    Text(
        text = ConversationUtils.getConversationTitle(conversation),
        color = style.titleTextColor,
        style = style.titleTextStyle,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}


/**
 * Default subtitle view composable that displays the last message preview,
 * message type icon, read receipts, and typing indicator.
 *
 * @param conversation The conversation to display
 * @param hideReceipts Whether to hide read receipts
 * @param typingIndicator Optional typing indicator to display
 * @param textFormatters List of text formatters for message preview
 * @param style Style configuration for the component
 */
@Composable
internal fun DefaultSubtitleView(
    conversation: Conversation,
    hideReceipts: Boolean,
    typingIndicator: TypingIndicator?,
    textFormatters: List<CometChatTextFormatter>?,
    style: CometChatConversationListItemStyle
) {
    val context = LocalContext.current
    
    // Show typing indicator if active, otherwise show last message
    if (typingIndicator != null && typingIndicator.isTyping && typingIndicator.typingUsers.isNotEmpty()) {
        // Display typing indicator
        // For user-to-user conversations, just show "Typing..."
        // For group conversations, show "{username} is typing..." or "{n} people are typing..."
        val typingText = when {
            conversation.conversationType == UIKitConstants.ConversationType.USERS -> {
                // User-to-user: just show "Typing..."
                context.getString(R.string.cometchat_typing)
            }
            typingIndicator.typingUsers.size == 1 -> {
                // Group with single user typing: show "{username} is typing..."
                context.getString(R.string.cometchat_is_typing, typingIndicator.typingUsers.first().name)
            }
            else -> {
                // Group with multiple users typing: show "{n} people are typing..."
                context.getString(R.string.cometchat_are_typing, typingIndicator.typingUsers.size)
            }
        }
        
        Text(
            text = typingText,
            color = style.typingIndicatorStyle.textColor,
            style = style.typingIndicatorStyle.textStyle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    } else {
        // Display last message preview
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            // Read receipt (if not hidden and message exists)
            val lastMessage = conversation.lastMessage
            if (!hideReceipts && lastMessage != null && isOutgoingMessage(lastMessage, conversation)) {
                // Convert message to Receipt enum
                val receipt = getReceiptFromMessage(lastMessage)
                CometChatReceipts(
                    receipt = receipt,
                    style = style.receiptStyle
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            
            // Sender name prefix (bold) for group messages
            // Order: [Receipt] [Sender Name:] [Icon] [Message] (matching View-based implementation)
            val prefix = if (lastMessage != null) {
                ConversationUtils.getMessagePrefix(context, lastMessage)
            } else ""
            
            if (prefix.isNotEmpty()) {
                Text(
                    text = prefix,
                    color = style.subtitleTextColor,
                    style = style.subtitleTextStyle.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // Message type icon (if available)
            val messageIcon = ConversationUtils.getLastMessageIcon(lastMessage)
            if (messageIcon != null) {
                Icon(
                    painter = painterResource(id = messageIcon),
                    contentDescription = null,
                    tint = style.messageTypeIconTint,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            
            // Last message text with formatting
            // Only apply text formatters if message is not deleted and has valid text
            val messageText: AnnotatedString = if (lastMessage is TextMessage && 
                lastMessage.deletedAt == 0L && 
                !lastMessage.text.isNullOrEmpty() &&
                textFormatters != null && 
                textFormatters.isNotEmpty()
            ) {
                FormatterUtils.getFormattedText(
                    context = context,
                    baseMessage = lastMessage,
                    formattingType = UIKitConstants.FormattingType.CONVERSATIONS,
                    alignment = UIKitConstants.MessageBubbleAlignment.LEFT,
                    text = lastMessage.text,
                    formatters = textFormatters
                )
            } else {
                AnnotatedString(ConversationUtils.getLastMessageText(context, conversation.lastMessage))
            }
            
            Text(
                text = messageText,
                color = style.subtitleTextColor,
                style = style.subtitleTextStyle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Converts a BaseMessage to a Receipt enum value based on its delivery status.
 *
 * @param message The message to convert
 * @return The appropriate Receipt enum value
 */
private fun getReceiptFromMessage(message: BaseMessage): Receipt {
    return when {
        message.readAt > 0 -> Receipt.READ
        message.deliveredAt > 0 -> Receipt.DELIVERED
        message.sentAt > 0 -> Receipt.SENT
        else -> Receipt.IN_PROGRESS
    }
}

/**
 * Checks if the message is an outgoing message (sent by the logged-in user).
 * Returns false in preview mode when CometChat SDK is not initialized.
 */
private fun isOutgoingMessage(
    message: BaseMessage,
    conversation: Conversation
): Boolean {
    return try {
        // Get the logged-in user
        val loggedInUser = CometChatUIKit.getLoggedInUser()
        
        // Check if the message sender is the logged-in user
        message.sender?.uid != null && 
               loggedInUser?.uid != null &&
               message.sender?.uid == loggedInUser.uid
    } catch (e: Exception) {
        // Return false in preview mode when SDK is not initialized
        false
    }
}


/**
 * Default trailing view composable that displays the timestamp and unread badge.
 *
 * @param conversation The conversation to display
 * @param typingIndicator Optional typing indicator for this conversation
 * @param dateTimeFormatter Optional custom date/time formatter
 * @param style Style configuration for the component
 */
@Composable
internal fun DefaultTrailingView(
    conversation: Conversation,
    typingIndicator: TypingIndicator?,
    dateTimeFormatter: DateTimeFormatterCallback?,
    style: CometChatConversationListItemStyle
) {
    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.Center
    ) {
        // Timestamp - use conversation's updatedAt timestamp (not last message's sentAt)
        // This ensures date is always shown, even for new conversations without messages
        val timestamp = conversation.updatedAt
        if (timestamp > 0) {
            CometChatDate(
                timestamp = timestamp,
                pattern = Pattern.DAY_DATE_TIME,
                style = style.dateStyle,
                dateTimeFormatterCallback = dateTimeFormatter,
                transparentBackground = true
            )
        }
        
        // Unread badge (only show if count > 0)
        val unreadCount = conversation.unreadMessageCount
        if (unreadCount > 0) {
            Spacer(modifier = Modifier.size(4.dp))
            CometChatBadgeCount(
                count = unreadCount,
                style = style.badgeStyle
            )
        }
    }
}


/**
 * Selection checkbox composable for multi-select mode.
 *
 * @param isSelected Whether the item is currently selected
 * @param conversationName Name of the conversation for accessibility
 * @param context Android context for accessing string resources
 * @param style Style configuration for the component
 */
@Composable
internal fun SelectionCheckbox(
    isSelected: Boolean,
    conversationName: String = "",
    context: Context,
    style: CometChatConversationListItemStyle
) {
    val checkboxSize = 20.dp
    val shape = RoundedCornerShape(style.checkBoxCornerRadius)
    val selectionStateDescription = if (isSelected) {
        context.getString(R.string.cometchat_selected)
    } else {
        context.getString(R.string.cometchat_not_selected)
    }
    
    Box(
        modifier = Modifier
            .size(checkboxSize)
            .clip(shape)
            .background(
                if (isSelected) style.checkBoxCheckedBackgroundColor 
                else style.checkBoxBackgroundColor
            )
            .border(
                width = style.checkBoxStrokeWidth,
                color = if (isSelected) style.checkBoxCheckedBackgroundColor 
                        else style.checkBoxStrokeColor,
                shape = shape
            )
            .focusable()
            .semantics {
                role = Role.Checkbox
                selected = isSelected
                stateDescription = selectionStateDescription
                contentDescription = if (conversationName.isNotEmpty()) {
                    "$conversationName, $selectionStateDescription"
                } else {
                    selectionStateDescription
                }
            },
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                imageVector = style.checkBoxSelectIcon,
                contentDescription = null,
                tint = style.checkBoxSelectIconTint,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}


/**
 * CometChatConversationListItem is a composable that displays a single conversation item in a list.
 * It matches the UI and functionality of the existing Android View-based ConversationsViewHolder.
 *
 * Features:
 * - Displays avatar with status indicator
 * - Shows conversation name and last message preview
 * - Displays timestamp and unread message count
 * - Supports selection mode with checkbox
 * - Fully customizable through style and custom view lambdas
 * - Integrates with CometChatTheme for consistent styling
 *
 * @param conversation The CometChat Conversation object to display (required)
 * @param onItemClick Callback invoked when the item is clicked (required)
 * @param modifier Modifier applied to the parent container
 * @param onItemLongClick Optional callback for long-click events
 * @param isSelected Whether the item is currently selected
 * @param selectionMode The selection mode (NONE, SINGLE, MULTIPLE)
 * @param hideUserStatus Whether to hide the user online/offline status indicator
 * @param hideGroupType Whether to hide the group type indicator
 * @param hideReceipts Whether to hide read receipts in subtitle
 * @param typingIndicator Optional typing indicator to display
 * @param textFormatters List of text formatters for message preview
 * @param dateTimeFormatter Optional custom date/time formatter
 * @param style Style configuration for the component
 * @param leadingView Optional custom composable for the leading section
 * @param titleView Optional custom composable for the title section
 * @param subtitleView Optional custom composable for the subtitle section
 * @param trailingView Optional custom composable for the trailing section
 *
 * @sample
 * ```
 * // Basic usage
 * CometChatConversationListItem(
 *     conversation = conversation,
 *     onItemClick = { conv -> navigateToChat(conv) }
 * )
 *
 * // With selection mode
 * CometChatConversationListItem(
 *     conversation = conversation,
 *     onItemClick = { conv -> toggleSelection(conv) },
 *     isSelected = selectedConversations.contains(conversation),
 *     selectionMode = SelectionMode.MULTIPLE
 * )
 *
 * // With custom views
 * CometChatConversationListItem(
 *     conversation = conversation,
 *     onItemClick = { conv -> navigateToChat(conv) },
 *     titleView = { conv, typingIndicator ->
 *         Text(
 *             text = conv.conversationWith.name,
 *             fontWeight = FontWeight.Bold
 *         )
 *     }
 * )
 * ```
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CometChatConversationListItem(
    conversation: Conversation,
    onItemClick: (Conversation) -> Unit,
    modifier: Modifier = Modifier,
    onItemLongClick: ((Conversation) -> Unit)? = null,
    isSelected: Boolean = false,
    selectionMode: UIKitConstants.SelectionMode = UIKitConstants.SelectionMode.NONE,
    hideUserStatus: Boolean = false,
    hideGroupType: Boolean = false,
    hideReceipts: Boolean = false,
    typingIndicator: TypingIndicator? = null,
    textFormatters: List<CometChatTextFormatter>? = null,
    dateTimeFormatter: DateTimeFormatterCallback? = null,
    style: CometChatConversationListItemStyle = CometChatConversationListItemStyle.default(),
    leadingView: (@Composable (Conversation, TypingIndicator?) -> Unit)? = null,
    titleView: (@Composable (Conversation, TypingIndicator?) -> Unit)? = null,
    subtitleView: (@Composable (Conversation, TypingIndicator?) -> Unit)? = null,
    trailingView: (@Composable (Conversation, TypingIndicator?) -> Unit)? = null
) {
    val context = LocalContext.current
    
    // Build content description for accessibility
    val conversationName = ConversationUtils.getConversationTitle(conversation)
    val lastMessageText = ConversationUtils.getFormattedSubtitleText(context, conversation.lastMessage)
    val accessibilityDescription = buildString {
        append(conversationName)
        if (lastMessageText.isNotEmpty()) {
            append(", ")
            append(lastMessageText)
        }
        if (conversation.unreadMessageCount > 0) {
            append(", ")
            append(context.getString(R.string.cometchat_unread_messages, conversation.unreadMessageCount))
        }
        if (isSelected) {
            append(", selected")
        }
    }
    
    // Determine background color based on selection state
    val backgroundColor = if (isSelected) {
        style.selectedBackgroundColor
    } else {
        style.backgroundColor
    }
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .focusable()
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(),
                onClick = { onItemClick(conversation) },
                onLongClick = onItemLongClick?.let { { it(conversation) } }
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .semantics {
                contentDescription = accessibilityDescription
                role = Role.Button
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Checkbox (when selection mode is active)
        if (selectionMode != UIKitConstants.SelectionMode.NONE) {
            SelectionCheckbox(
                isSelected = isSelected,
                conversationName = conversationName,
                context = context,
                style = style
            )
            Spacer(modifier = Modifier.width(12.dp))
        }
        
        // Leading view (avatar with status indicator)
        if (leadingView != null) {
            leadingView(conversation, typingIndicator)
        } else {
            DefaultLeadingView(
                conversation = conversation,
                typingIndicator = typingIndicator,
                hideUserStatus = hideUserStatus,
                hideGroupType = hideGroupType,
                style = style
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Content area (title and subtitle)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            // Title view
            if (titleView != null) {
                titleView(conversation, typingIndicator)
            } else {
                DefaultTitleView(
                    conversation = conversation,
                    typingIndicator = typingIndicator,
                    style = style
                )
            }
            
            Spacer(modifier = Modifier.size(2.dp))
            
            // Subtitle view
            if (subtitleView != null) {
                subtitleView(conversation, typingIndicator)
            } else {
                DefaultSubtitleView(
                    conversation = conversation,
                    hideReceipts = hideReceipts,
                    typingIndicator = typingIndicator,
                    textFormatters = textFormatters,
                    style = style
                )
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Trailing view (date and badge)
        if (trailingView != null) {
            trailingView(conversation, typingIndicator)
        } else {
            DefaultTrailingView(
                conversation = conversation,
                typingIndicator = typingIndicator,
                dateTimeFormatter = dateTimeFormatter,
                style = style
            )
        }
    }
}
