package com.cometchat.uikit.compose.presentation.conversations.ui

import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.CollectionInfo
import androidx.compose.ui.semantics.collectionInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.cometchat.chat.models.Conversation
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.User
import com.cometchat.uikit.compose.R
import com.cometchat.uikit.compose.presentation.conversations.style.CometChatConversationsStyle
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.compose.presentation.conversations.utils.TypingIndicator
import com.cometchat.uikit.compose.presentation.shared.formatters.CometChatTextFormatter
import com.cometchat.uikit.compose.presentation.shared.interfaces.DateTimeFormatterCallback
import com.cometchat.uikit.compose.presentation.shared.popupmenu.CometChatPopupMenu
import com.cometchat.uikit.compose.shared.views.popupmenu.MenuItem
import kotlinx.coroutines.flow.SharedFlow

/**
 * Content composable for the conversation list.
 * Uses LazyColumn with pagination support.
 * 
 * @param conversations List of conversations to display
 * @param typingIndicators Map of typing indicators by conversation
 * @param selectedConversations Set of selected conversations
 * @param selectionMode Current selection mode
 * @param style Style configuration for the component
 * @param hideUserStatus Whether to hide user online/offline indicators
 * @param hideGroupType Whether to hide group type indicators
 * @param hideReceipts Whether to hide message read receipts
 * @param hideSeparator Whether to hide item separators
 * @param hideDeleteOption Whether to hide delete option in menu
 * @param dateTimeFormatter Custom date/time formatter callback
 * @param textFormatters List of text formatters for message preview
 * @param itemView Custom item composable replacing entire item
 * @param leadingView Custom leading section composable
 * @param titleView Custom title section composable
 * @param subtitleView Custom subtitle section composable
 * @param trailingView Custom trailing section composable
 * @param options Function to replace all menu options
 * @param addOptions Function to add options to default menu
 * @param onItemClick Callback for item clicks
 * @param onItemLongClick Callback for item long-clicks
 * @param onDeleteConversation Callback when delete is selected from menu
 * @param onLoadMore Callback when more items should be loaded
 * @param scrollToTopEvent SharedFlow that emits when list should scroll to top
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun ConversationListContent(
    conversations: List<Conversation>,
    typingIndicators: Map<String, com.cometchat.chat.models.TypingIndicator>,
    selectedConversations: Set<Conversation>,
    selectionMode: UIKitConstants.SelectionMode,
    style: CometChatConversationsStyle,
    hideUserStatus: Boolean,
    hideGroupType: Boolean,
    hideReceipts: Boolean,
    hideSeparator: Boolean,
    hideDeleteOption: Boolean,
    dateTimeFormatter: DateTimeFormatterCallback?,
    textFormatters: List<CometChatTextFormatter>,
    itemView: (@Composable (Conversation, TypingIndicator?) -> Unit)?,
    leadingView: (@Composable (Conversation, TypingIndicator?) -> Unit)?,
    titleView: (@Composable (Conversation, TypingIndicator?) -> Unit)?,
    subtitleView: (@Composable (Conversation, TypingIndicator?) -> Unit)?,
    trailingView: (@Composable (Conversation, TypingIndicator?) -> Unit)?,
    options: ((Context, Conversation) -> List<MenuItem>)?,
    addOptions: ((Context, Conversation) -> List<MenuItem>)?,
    onItemClick: (Conversation) -> Unit,
    onItemLongClick: (Conversation) -> Unit,
    onDeleteConversation: (Conversation) -> Unit,
    onLoadMore: () -> Unit,
    scrollToTopEvent: SharedFlow<Unit>? = null
) {
    val listState = rememberLazyListState()
    val context = LocalContext.current
    // Detect when we need to load more items
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
            lastVisibleItem != null && lastVisibleItem.index >= conversations.size - 5
        }
    }
    
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore && conversations.isNotEmpty()) {
            onLoadMore()
        }
    }
    
    // Auto-scroll to top when new message received (if first 3 items are visible)
    LaunchedEffect(scrollToTopEvent) {
        scrollToTopEvent?.collect {
            if (listState.firstVisibleItemIndex < 3) {
                listState.animateScrollToItem(0)
            }
        }
    }
    
    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .background(style.backgroundColor)
            .semantics { 
                contentDescription = context.getString(
                    R.string.cometchat_conversation_list_description,
                    conversations.size
                )
                collectionInfo = CollectionInfo(
                    rowCount = conversations.size,
                    columnCount = 1
                )
            }
    ) {
        itemsIndexed(
            items = conversations,
            key = { _, conversation -> conversation.conversationId }
        ) { index, conversation ->
            val isSelected = selectedConversations.contains(conversation)
            val typingIndicator = getTypingIndicatorForConversation(conversation, typingIndicators)
            
            // Local state for popup menu for this item
            var showPopupMenu by remember { mutableStateOf(false) }
            
            // Build menu items for this conversation (composable due to painterResource)
            val menuItems = buildMenuItems(
                context = context,
                conversation = conversation,
                style = style,
                hideDeleteOption = hideDeleteOption,
                options = options,
                addOptions = addOptions,
                onDelete = { onDeleteConversation(conversation) }
            )
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .focusable()
                    .combinedClickable(
                        onClick = { onItemClick(conversation) },
                        onLongClick = { 
                            if (selectionMode == UIKitConstants.SelectionMode.NONE) {
                                showPopupMenu = true
                                onItemLongClick(conversation)
                            }
                        }
                    )
            ) {
                if (itemView != null) {
                    // Use custom item view
                    itemView(conversation, typingIndicator)
                } else {
                    // Use default CometChatConversationListItem
                    CometChatConversationListItem(
                        conversation = conversation,
                        onItemClick = { onItemClick(conversation)},
                        isSelected = isSelected,
                        selectionMode = selectionMode,
                        typingIndicator = typingIndicator,
                        hideUserStatus = hideUserStatus,
                        hideGroupType = hideGroupType,
                        hideReceipts = hideReceipts,
                        textFormatters = textFormatters,
                        dateTimeFormatter = dateTimeFormatter,
                        style = style.itemStyle,
                        onItemLongClick = { 
                            if (selectionMode == UIKitConstants.SelectionMode.NONE) {
                                showPopupMenu = true
                                onItemLongClick(conversation)
                            }
                        },
                        leadingView = leadingView?.let { { conv, typing -> it(conv, typing) } },
                        titleView = titleView?.let { { conv, typing -> it(conv, typing) } },
                        subtitleView = subtitleView?.let { { conv, typing -> it(conv, typing) } },
                        trailingView = trailingView?.let { { conv, typing -> it(conv, typing) } }
                    )
                }
                
                // Popup menu anchored to the bottom-right of the item (matching Kotlin implementation)
                // Using a Box at the end to anchor the DropdownMenu
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 12.dp, bottom = 10.dp)
                ) {
                    CometChatPopupMenu(
                        expanded = showPopupMenu,
                        onDismissRequest = { showPopupMenu = false },
                        menuItems = menuItems,
                        style = style.popupMenuStyle,
                        offset = DpOffset(0.dp, 0.dp),
                        onMenuItemClick = { _, _ -> showPopupMenu = false }
                    ) {
                        // Empty anchor - the popup will appear at this position
                    }
                }
            }
            
            // Separator
            if (!hideSeparator && index < conversations.size - 1) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 72.dp)
                        .height(style.separatorHeight)
                        .background(style.separatorColor)
                )
            }
        }
    }
}

/**
 * Gets the typing indicator for a specific conversation.
 * Converts from SDK TypingIndicator to UIKit TypingIndicator.
 */
private fun getTypingIndicatorForConversation(
    conversation: Conversation,
    typingIndicators: Map<String, com.cometchat.chat.models.TypingIndicator>
): TypingIndicator? {
    val conversationType = conversation.conversationType
    
    // Find all typing indicators that match this conversation
    // For user-to-user: match sender's UID with conversation's user UID
    // For groups: match receiverId with group's GUID
    val matchingTypingIndicators = typingIndicators.values.filter { indicator ->
        when (conversationType) {
            UIKitConstants.ConversationType.USERS -> {
                // For user conversations, the typing indicator's sender is the one typing
                // We need to match the sender's UID with the conversation's user UID
                val conversationUser = conversation.conversationWith as? User
                indicator.receiverType == conversationType && 
                    conversationUser?.uid == indicator.sender?.uid
            }
            UIKitConstants.ConversationType.GROUPS -> {
                // For group conversations, match the receiverId with the group's GUID
                val conversationGroup = conversation.conversationWith as? Group
                indicator.receiverType == conversationType && 
                    indicator.receiverId == conversationGroup?.guid
            }
            else -> false
        }
    }
    
    if (matchingTypingIndicators.isEmpty()) {
        return null
    }
    
    // Convert SDK TypingIndicators to UIKit TypingIndicator
    // Collect all users who are typing in this conversation
    val typingUsers = matchingTypingIndicators.mapNotNull { it.sender }
    
    return TypingIndicator(
        typingUsers = typingUsers,
        isTyping = true
    )
}

/**
 * Builds the menu items for the popup menu.
 * Supports custom options, additional options, and default delete option.
 */
@Composable
private fun buildMenuItems(
    context: Context,
    conversation: Conversation,
    style: CometChatConversationsStyle,
    hideDeleteOption: Boolean,
    options: ((Context, Conversation) -> List<MenuItem>)?,
    addOptions: ((Context, Conversation) -> List<MenuItem>)?,
    onDelete: () -> Unit
): List<MenuItem> {
    // If custom options are provided, use them exclusively
    if (options != null) {
        return options(context, conversation)
    }
    
    val menuItems = mutableListOf<MenuItem>()
    
    // Add default delete option if not hidden
    if (!hideDeleteOption) {
        menuItems.add(
            MenuItem(
                id = "delete",
                name = context.getString(R.string.cometchat_delete),
                startIcon = painterResource(R.drawable.cometchat_ic_delete),
                startIconTint = style.deleteOptionIconTint,
                textColor = style.deleteOptionTextColor,
                textStyle = style.deleteOptionTextStyle,
                onClick = onDelete
            )
        )
    }
    
    // Add additional options if provided
    if (addOptions != null) {
        menuItems.addAll(addOptions(context, conversation))
    }
    
    return menuItems
}
