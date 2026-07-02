package com.cometchat.uikit.compose.presentation.aiassistantchathistory.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cometchat.chat.models.BaseMessage
import com.cometchat.uikit.compose.R
import com.cometchat.uikit.compose.presentation.aiassistantchathistory.style.CometChatAIAssistantChatHistoryStyle
import com.cometchat.uikit.compose.presentation.shared.defaultstates.CometChatEmptyState
import com.cometchat.uikit.compose.presentation.shared.defaultstates.CometChatErrorState
import com.cometchat.uikit.compose.presentation.shared.defaultstates.CometChatLoadingState
import com.cometchat.uikit.compose.presentation.shared.dialog.CometChatDialog
import com.cometchat.uikit.compose.shared.views.popupmenu.MenuItem
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.factory.CometChatAIAssistantChatHistoryViewModelFactory
import com.cometchat.uikit.core.state.ChatHistoryUIState
import com.cometchat.uikit.core.viewmodel.CometChatAIAssistantChatHistoryViewModel

/**
 * CometChatAIAssistantChatHistory displays a panel of past AI assistant text messages
 * grouped by date, with support for pagination, message deletion, loading/empty/error states,
 * a header with close and "New Chat" actions, popup menu customization, and comprehensive theming.
 *
 * The composable collects state from the shared [CometChatAIAssistantChatHistoryViewModel]
 * and manages lifecycle via [DisposableEffect] for listener registration/cleanup.
 *
 * @param modifier Modifier applied to the root container
 * @param viewModel The ViewModel managing chat history state (optional, creates default if not provided)
 * @param style Style configuration for the component
 * @param onCloseClick Callback invoked when the header close icon is tapped
 * @param onNewChatClick Callback invoked when the "New Chat" row is tapped
 * @param onItemClick Callback invoked when a message item is tapped
 * @param onItemLongClick Callback invoked when a message item is long-pressed; if set, replaces the default popup menu
 * @param emptyStateView Custom composable for the empty state; if null, uses default empty state
 * @param errorStateView Custom composable for the error state; if null, uses default error state
 * @param loadingStateView Custom composable for the loading state; if null, uses default shimmer loading
 * @param options Function to replace all popup menu options for a given message
 * @param addOptions Function to append additional popup menu options after the default delete option
 */
@Composable
fun CometChatAIAssistantChatHistory(
    modifier: Modifier = Modifier,
    viewModel: CometChatAIAssistantChatHistoryViewModel = viewModel(
        factory = CometChatAIAssistantChatHistoryViewModelFactory()
    ),
    style: CometChatAIAssistantChatHistoryStyle = CometChatAIAssistantChatHistoryStyle.default(),
    // Callbacks
    onCloseClick: (() -> Unit)? = null,
    onNewChatClick: (() -> Unit)? = null,
    onItemClick: ((BaseMessage) -> Unit)? = null,
    onItemLongClick: ((BaseMessage) -> Unit)? = null,
    // Custom view slots
    emptyStateView: (@Composable () -> Unit)? = null,
    errorStateView: (@Composable () -> Unit)? = null,
    loadingStateView: (@Composable () -> Unit)? = null,
    // Popup menu customization
    options: ((BaseMessage) -> List<MenuItem>)? = null,
    addOptions: ((BaseMessage) -> List<MenuItem>)? = null
) {
    val context = LocalContext.current

    // Collect ViewModel StateFlows
    val uiState by viewModel.uiState.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val hasMore by viewModel.hasMore.collectAsState()
    val isInProgress by viewModel.isInProgress.collectAsState()

    // Local state for delete confirmation dialog
    var showDeleteDialog by remember { mutableStateOf(false) }
    var messageToDelete by remember { mutableStateOf<BaseMessage?>(null) }

    // Lifecycle management — register/unregister listeners
    DisposableEffect(viewModel) {
        viewModel.addListeners()
        onDispose {
            viewModel.removeListeners()
        }
    }

    // Handle delete state events from SharedFlow
    LaunchedEffect(viewModel) {
        viewModel.deleteState.collect { state ->
            when (state) {
                UIKitConstants.DeleteState.SUCCESS_DELETE -> {
                    showDeleteDialog = false
                    messageToDelete = null
                }
                UIKitConstants.DeleteState.FAILURE_DELETE -> {
                    showDeleteDialog = false
                    messageToDelete = null
                    Toast.makeText(
                        context,
                        context.getString(R.string.cometchat_something_went_wrong_please_try_again),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else -> { /* INITIATED_DELETE — no UI action needed */ }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(style.chatHistoryBackgroundColor)
    ) {
        // Header row: close icon + "Chat History" title
        ChatHistoryHeader(
            style = style,
            onCloseClick = { onCloseClick?.invoke() }
        )

        // Divider between header and new chat row
        HorizontalDivider(
            color = CometChatTheme.colorScheme.borderColorLight,
            thickness = 1.dp
        )

        // New Chat row: icon + "New Chat" label
        NewChatRow(
            style = style,
            onNewChatClick = { onNewChatClick?.invoke() }
        )

        // Content area with state-based rendering
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            when (uiState) {
                is ChatHistoryUIState.Loading -> {
                    loadingStateView?.invoke() ?: CometChatLoadingState()
                }

                is ChatHistoryUIState.Empty -> {
                    emptyStateView?.invoke() ?: CometChatEmptyState(
                        title = context.getString(R.string.cometchat_no_conversations_history_title),
                        subtitle = context.getString(R.string.cometchat_no_conversations_history_subtitle)
                    )
                }

                is ChatHistoryUIState.Error -> {
                    errorStateView?.invoke() ?: CometChatErrorState(
                        title = context.getString(R.string.cometchat_something_went_wrong),
                        subtitle = context.getString(R.string.cometchat_something_went_wrong_please_try_again),
                        onRetry = { viewModel.fetchMessages() }
                    )
                }

                is ChatHistoryUIState.Content -> {
                    ChatHistoryListContent(
                        messages = messages,
                        style = style,
                        hasMore = hasMore,
                        isInProgress = isInProgress,
                        onFetchMore = { viewModel.fetchMessages() },
                        onItemClick = { message ->
                            onItemClick?.invoke(message)
                        },
                        onItemLongClick = { message ->
                            if (onItemLongClick != null) {
                                // Custom long-click overrides default popup menu
                                onItemLongClick.invoke(message)
                            }
                        },
                        popupMenuItems = { message, onDismiss ->
                            if (onItemLongClick != null) {
                                // Custom long-click handler provided — no default popup
                                emptyList()
                            } else {
                                buildPopupMenuItems(
                                    context = context,
                                    message = message,
                                    style = style,
                                    options = options,
                                    addOptions = addOptions,
                                    onDelete = {
                                        onDismiss()
                                        messageToDelete = message
                                        showDeleteDialog = true
                                    }
                                )
                            }
                        }
                    )
                }
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog && messageToDelete != null) {
        CometChatDialog(
            title = context.getString(R.string.cometchat_delete_message_title),
            message = context.getString(R.string.cometchat_delete_message_subtitle),
            positiveButtonText = context.getString(R.string.cometchat_delete),
            negativeButtonText = context.getString(R.string.cometchat_cancel),
            icon = painterResource(R.drawable.cometchat_ic_delete),
            onPositiveClick = {
                viewModel.deleteChatHistoryItem(messageToDelete!!)
            },
            onNegativeClick = {
                showDeleteDialog = false
                messageToDelete = null
            }
        )
    }
}

/**
 * Header row containing a close icon button and "Chat History" title text.
 */
@Composable
private fun ChatHistoryHeader(
    style: CometChatAIAssistantChatHistoryStyle,
    onCloseClick: () -> Unit
) {
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(style.chatHistoryHeaderBackgroundColor)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Close icon button
        IconButton(
            onClick = onCloseClick,
            modifier = Modifier
                .size(40.dp)
                .semantics {
                    contentDescription = context.getString(R.string.cometchat_close)
                }
        ) {
            style.chatHistoryHeaderCloseIcon?.let { icon ->
                Icon(
                    painter = icon,
                    contentDescription = null,
                    tint = style.chatHistoryHeaderCloseIconTint,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(4.dp))

        // "Chat History" title
        Text(
            text = context.getString(R.string.cometchat_chat_history),
            color = style.chatHistoryHeaderTextColor,
            style = style.chatHistoryHeaderTextStyle
        )
    }
}

/**
 * "New Chat" action row with an icon and "New Chat" label text.
 */
@Composable
private fun NewChatRow(
    style: CometChatAIAssistantChatHistoryStyle,
    onNewChatClick: () -> Unit
) {
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(style.newChatBackgroundColor)
            .clickable { onNewChatClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // New Chat icon
        style.newChatIcon?.let { icon ->
            Icon(
                painter = icon,
                contentDescription = null,
                tint = style.newChatIconTint,
                modifier = Modifier
                    .size(24.dp)
                    .padding(4.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // "New Chat" label
        Text(
            text = context.getString(R.string.cometchat_new_chat),
            color = style.newChatTextColor,
            style = style.newChatTextStyle
        )
    }
}

/**
 * Builds the popup menu items for a long-pressed message.
 * Supports custom options (complete replacement), addOptions (append to defaults),
 * and the default delete option.
 */
@Composable
private fun buildPopupMenuItems(
    context: android.content.Context,
    message: BaseMessage,
    style: CometChatAIAssistantChatHistoryStyle,
    options: ((BaseMessage) -> List<MenuItem>)?,
    addOptions: ((BaseMessage) -> List<MenuItem>)?,
    onDelete: () -> Unit
): List<MenuItem> {
    // If custom options callback is provided, use it as the complete popup menu
    if (options != null) {
        return options(message)
    }

    val menuItems = mutableListOf<MenuItem>()

    // Default delete option
    menuItems.add(
        MenuItem(
            id = UIKitConstants.ConversationOption.DELETE,
            name = context.getString(R.string.cometchat_delete),
            startIcon = style.deleteOptionIcon,
            startIconTint = style.deleteOptionIconTint,
            textColor = style.deleteOptionTextColor,
            textStyle = style.deleteOptionTextStyle,
            onClick = onDelete
        )
    )

    // Append additional options if provided
    if (addOptions != null) {
        menuItems.addAll(addOptions(message))
    }

    return menuItems
}
