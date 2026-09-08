package com.cometchat.uikit.compose.presentation.savedmessages.ui

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.models.Action
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.TextMessage
import com.cometchat.chat.models.User
import com.cometchat.uikit.compose.R
import com.cometchat.uikit.compose.presentation.conversations.utils.ConversationUtils
import com.cometchat.uikit.compose.presentation.shared.formatters.CometChatMentionsFormatter
import com.cometchat.uikit.compose.presentation.shared.formatters.CometChatTextFormatter
import com.cometchat.uikit.compose.presentation.shared.formatters.FormatterUtils
import com.cometchat.uikit.compose.presentation.shared.messagebubble.ui.FormattedPreviewText
import com.cometchat.uikit.compose.presentation.shared.messagebubble.ui.buildPreviewAnnotatedString
import com.cometchat.uikit.core.formatter.MarkdownRenderer
import com.cometchat.uikit.compose.presentation.shared.baseelements.avatar.CometChatAvatar
import com.cometchat.uikit.compose.presentation.shared.baseelements.date.CometChatDate
import com.cometchat.uikit.compose.presentation.shared.baseelements.date.Pattern
import com.cometchat.uikit.compose.presentation.shared.defaultstates.CometChatEmptyState
import com.cometchat.uikit.compose.presentation.shared.defaultstates.CometChatEmptyStateStyle
import com.cometchat.uikit.compose.presentation.shared.dialog.CometChatConfirmDialog
import com.cometchat.uikit.compose.presentation.shared.dialog.CometChatConfirmDialogStyle
import com.cometchat.uikit.compose.presentation.shared.popupmenu.CometChatPopupMenu
import com.cometchat.uikit.compose.presentation.shared.toolbar.CometChatToolbar
import com.cometchat.uikit.compose.shared.views.popupmenu.MenuItem
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.factory.CometChatSavedMessagesViewModelFactory
import com.cometchat.uikit.core.events.CometChatEvents
import com.cometchat.uikit.core.events.CometChatMessageEvent
import com.cometchat.uikit.core.state.PinnedSavedListUIState
import com.cometchat.uikit.core.viewmodel.CometChatSavedMessagesViewModel

/**
 * User-level, cross-conversation list of the current user's saved messages. Each row is a dedicated
 * saved-message item (avatar + title + subtitle with message-type icon + timestamp) — no unread
 * badge. Tap a row to open its conversation at the message; long-press to unsave.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CometChatSavedMessages(
    modifier: Modifier = Modifier,
    viewModel: CometChatSavedMessagesViewModel? = null,
    onBackClick: () -> Unit = {},
    onMessageClick: (BaseMessage) -> Unit = {}
) {
    val context = LocalContext.current
    val vm = viewModel ?: remember {
        CometChatSavedMessagesViewModelFactory().create(CometChatSavedMessagesViewModel::class.java)
    }

    LaunchedEffect(Unit) {
        vm.reload()
    }

    // Toast when an unsave succeeds (matches the message-list save/unsave toasts).
    LaunchedEffect(Unit) {
        vm.unsaveSuccess.collect {
            Toast.makeText(
                context,
                context.getString(R.string.cometchat_message_unsaved),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // Live upkeep: reflect saves/unsaves made elsewhere while this screen is open.
    LaunchedEffect(Unit) {
        CometChatEvents.messageEvents.collect { event ->
            when (event) {
                is CometChatMessageEvent.MessageSaved -> vm.onMessageSavedExternally(event.message)
                is CometChatMessageEvent.MessageUnsaved -> vm.onMessageUnsavedExternally(event.message)
                else -> {}
            }
        }
    }

    val messages by vm.messages.collectAsState()
    val uiState by vm.uiState.collectAsState()

    // Default mentions formatter so subtitle previews style mentions exactly like the
    // Conversations list item (same pipeline, same formatter).
    val textFormatters: List<CometChatTextFormatter> = remember(context) {
        listOf(CometChatMentionsFormatter(context))
    }

    // The row whose long-press options menu is open, and the row awaiting unsave confirmation.
    var menuTarget by remember { mutableStateOf<BaseMessage?>(null) }
    var unsaveTarget by remember { mutableStateOf<BaseMessage?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CometChatTheme.colorScheme.backgroundColor1)
    ) {
        CometChatToolbar(
            title = stringResource(R.string.cometchat_saved_messages),
            hideBackIcon = false,
            navigationContentDescription = "Back",
            onNavigationClick = onBackClick
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            when (uiState) {
                is PinnedSavedListUIState.Empty -> {
                    CometChatEmptyState(
                        style = CometChatEmptyStateStyle.default(
                            icon = painterResource(com.cometchat.uikit.core.R.drawable.cometchat_ic_bookmark_empty),
                            iconTint = Color.Unspecified,
                            iconSize = 120.dp
                        ),
                        title = stringResource(R.string.cometchat_no_saved_messages),
                        subtitle = stringResource(R.string.cometchat_no_saved_messages_subtitle)
                    )
                }

                // Load failure: same "Oops!" error copy the conversations component shows — but no
                // retry button (matches the pinned list, per design). Action failures never land
                // here — the ViewModel reserves Error for loads.
                is PinnedSavedListUIState.Error -> {
                    CometChatEmptyState(
                        style = CometChatEmptyStateStyle.default(
                            icon = painterResource(R.drawable.cometchat_ic_error),
                            iconTint = Color.Unspecified,
                            iconSize = 120.dp
                        ),
                        title = stringResource(R.string.cometchat_error_conversations_title),
                        subtitle = stringResource(R.string.cometchat_something_went_wrong_please_try_again)
                    )
                }

                else -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(items = messages, key = { it.id }) { message ->
                            SavedMessageRow(
                                message = message,
                                textFormatters = textFormatters,
                                onClick = onMessageClick,
                                // Long-press opens the one-item options menu; picking "Unsave"
                                // then raises the confirm dialog (same flow as the View kit).
                                onLongClick = { menuTarget = it },
                                showMenu = menuTarget?.id == message.id,
                                onDismissMenu = { menuTarget = null },
                                onUnsaveSelected = {
                                    menuTarget = null
                                    unsaveTarget = message
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    unsaveTarget?.let { message ->
        CometChatConfirmDialog(
            title = stringResource(R.string.cometchat_unsave_message_confirm_title),
            subtitle = stringResource(R.string.cometchat_unsave_message_confirm_body),
            positiveButtonText = stringResource(R.string.cometchat_unsave),
            negativeButtonText = stringResource(R.string.cometchat_cancel),
            hideIcon = true,
            // Non-destructive: primary (purple) positive button, not the default red.
            style = CometChatConfirmDialogStyle.default(
                positiveButtonBackgroundColor = CometChatTheme.colorScheme.primary
            ),
            onPositiveClick = {
                vm.unsave(message)
                unsaveTarget = null
            },
            onNegativeClick = { unsaveTarget = null },
            onDismiss = { unsaveTarget = null }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SavedMessageRow(
    message: BaseMessage,
    textFormatters: List<CometChatTextFormatter>,
    onClick: (BaseMessage) -> Unit,
    onLongClick: (BaseMessage) -> Unit,
    showMenu: Boolean = false,
    onDismissMenu: () -> Unit = {},
    onUnsaveSelected: () -> Unit = {}
) {
    val context = LocalContext.current
    val unsaveIcon = painterResource(com.cometchat.uikit.core.R.drawable.cometchat_ic_bookmark_filled)
    val unsaveLabel = stringResource(R.string.cometchat_unsave)
    val menuItems = remember(unsaveLabel) {
        listOf(
            MenuItem(
                id = UIKitConstants.MessageOption.UNSAVE,
                name = unsaveLabel,
                startIcon = unsaveIcon
            )
        )
    }

    // Resolve the row's title + avatar from the message's source conversation (group or peer).
    val title: String
    val avatarUrl: String?
    if (message.receiverType == CometChatConstants.RECEIVER_TYPE_GROUP) {
        val group = message.receiver as? Group
        title = group?.name ?: message.receiverUid
        avatarUrl = group?.icon
    } else {
        val myUid = CometChat.getLoggedInUser()?.uid
        val peer: User? = if (message.sender?.uid == myUid) message.receiver as? User else message.sender
        title = peer?.name ?: peer?.uid ?: message.receiverUid
        avatarUrl = peer?.avatar
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { onClick(message) },
                onLongClick = { onLongClick(message) }
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CometChatAvatar(
            name = title,
            avatarUrl = avatarUrl,
            modifier = Modifier.size(48.dp)
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        ) {
            Text(
                text = title,
                style = CometChatTheme.typography.heading4Medium,
                color = CometChatTheme.colorScheme.textColorPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 2.dp)
            ) {
                // Sender prefix (bold), same order as the Conversations item: [Prefix] [Icon] [Message].
                val prefix = getSavedMessagePrefix(context, message)
                if (prefix.isNotEmpty()) {
                    Text(
                        text = prefix,
                        style = CometChatTheme.typography.bodyRegular.copy(fontWeight = FontWeight.Bold),
                        color = CometChatTheme.colorScheme.textColorSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                ConversationUtils.getLastMessageIcon(message)?.let { iconRes ->
                    Icon(
                        painter = painterResource(iconRes),
                        contentDescription = stringResource(R.string.cometchat_message_type_icon),
                        tint = CometChatTheme.colorScheme.textColorSecondary,
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .size(16.dp)
                    )
                }
                // Preview text formatted the same way as the Conversations list item:
                // Step 1: run the formatter pipeline (resolves mentions with styling)
                // Step 2: parse markdown from the resolved text
                // Step 3: merge mention spans from the formatter onto the markdown result
                val subtitleColor = CometChatTheme.colorScheme.textColorSecondary
                val previewText: AnnotatedString = if (message is TextMessage &&
                    message.deletedAt == 0L &&
                    !message.text.isNullOrEmpty()
                ) {
                    val formatterResult = if (textFormatters.isNotEmpty()) {
                        FormatterUtils.getFormattedText(
                            context = context,
                            baseMessage = message,
                            formattingType = UIKitConstants.FormattingType.CONVERSATIONS,
                            alignment = UIKitConstants.MessageBubbleAlignment.LEFT,
                            text = message.text,
                            formatters = textFormatters
                        )
                    } else {
                        AnnotatedString(message.text)
                    }
                    val segments = MarkdownRenderer.parse(formatterResult.text)
                    val markdownStyled = buildPreviewAnnotatedString(
                        segments = segments,
                        textColor = subtitleColor,
                        linkColor = subtitleColor
                    )
                    if (formatterResult.spanStyles.isNotEmpty()) {
                        buildAnnotatedString {
                            append(markdownStyled)
                            for (spanStyle in formatterResult.spanStyles) {
                                if (spanStyle.end <= markdownStyled.length) {
                                    addStyle(spanStyle.item, spanStyle.start, spanStyle.end)
                                }
                            }
                        }
                    } else {
                        markdownStyled
                    }
                } else {
                    val plainText = ConversationUtils.getLastMessageText(context, message)
                    buildAnnotatedString {
                        append(plainText)
                        addStyle(SpanStyle(color = subtitleColor), 0, plainText.length)
                    }
                }
                FormattedPreviewText(
                    text = previewText,
                    style = CometChatTheme.typography.bodyRegular,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Box {
            CometChatDate(
                timestamp = message.sentAt,
                pattern = Pattern.DAY_DATE_TIME,
                modifier = Modifier.padding(start = 8.dp)
            )
            // Anchored to the row's trailing edge — the menu opens beside the timestamp.
            CometChatPopupMenu(
                expanded = showMenu,
                onDismissRequest = onDismissMenu,
                menuItems = menuItems,
                onMenuItemClick = { id, _ ->
                    if (id == UIKitConstants.MessageOption.UNSAVE) onUnsaveSelected()
                }
            ) {}
        }
    }
}

/**
 * Sender prefix for a saved row. Unlike the Conversations prefix (group-only), the saved list also
 * prefixes 1-1 messages the logged-in user sent with "You: " — out of their home conversation,
 * ownership isn't obvious from the row title alone.
 */
private fun getSavedMessagePrefix(context: Context, message: BaseMessage): String {
    val prefix = ConversationUtils.getMessagePrefix(context, message)
    if (prefix.isNotEmpty()) return prefix
    if (message is Action) return ""
    val myUid = CometChat.getLoggedInUser()?.uid
    return if (myUid != null && message.sender?.uid == myUid) {
        "${context.getString(R.string.cometchat_you)}: "
    } else {
        ""
    }
}
