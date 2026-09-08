package com.cometchat.uikit.compose.presentation.pinnedmessages.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.TextMessage
import com.cometchat.chat.models.User
import com.cometchat.uikit.compose.R
import com.cometchat.uikit.compose.presentation.shared.defaultstates.CometChatEmptyState
import com.cometchat.uikit.compose.presentation.shared.defaultstates.CometChatEmptyStateStyle
import com.cometchat.uikit.compose.presentation.messageinformation.ui.CometChatMessageInformation
import com.cometchat.uikit.compose.presentation.shared.popupmenu.CometChatPopupMenu
import com.cometchat.uikit.compose.shared.views.popupmenu.MenuItem
import com.cometchat.uikit.compose.presentation.shared.dialog.CometChatConfirmDialog
import com.cometchat.uikit.compose.presentation.shared.dialog.CometChatConfirmDialogStyle
import com.cometchat.uikit.compose.presentation.shared.formatters.CometChatMentionsFormatter
import com.cometchat.uikit.compose.presentation.shared.formatters.CometChatTextFormatter
import com.cometchat.uikit.compose.presentation.shared.formatters.style.CometChatMentionStyle
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatAudioBubbleStyle
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatAudiosBubbleStyle
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatCollaborativeBubbleStyle
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatFileBubbleStyle
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatFilesBubbleStyle
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatImageBubbleStyle
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatImagesBubbleStyle
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatMeetCallBubbleStyle
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatMessageBubbleStyle
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatPollBubbleStyle
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatTextBubbleStyle
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatVideoBubbleStyle
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatVideosBubbleStyle
import com.cometchat.uikit.compose.presentation.shared.messagebubble.ui.CometChatMessageBubble
import com.cometchat.uikit.compose.presentation.shared.toolbar.CometChatToolbar
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.events.CometChatEvents
import com.cometchat.uikit.core.events.CometChatMessageEvent
import com.cometchat.uikit.core.factory.CometChatPinnedMessagesViewModelFactory
import com.cometchat.uikit.core.state.PinnedSavedListUIState
import com.cometchat.uikit.core.utils.MessageOptionsUtils
import com.cometchat.uikit.core.utils.PinSaveUtils
import com.cometchat.uikit.core.viewmodel.CometChatPinnedMessagesViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Full-screen list of a conversation's pinned messages, rendered as the real message bubbles
 * (avatar + sender·date header + content + pinned footer). The logged-in user's own messages use the
 * outgoing (purple) style and read "You". Read-only: tap a row to jump to the message; long-press to
 * unpin. Scope with [user] (1-1) or [group].
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CometChatPinnedMessages(
    modifier: Modifier = Modifier,
    user: User? = null,
    group: Group? = null,
    viewModel: CometChatPinnedMessagesViewModel? = null,
    textFormatters: List<CometChatTextFormatter>? = null,
    onBackClick: () -> Unit = {},
    onMessageClick: (BaseMessage) -> Unit = {}
) {
    val vm = viewModel ?: remember {
        CometChatPinnedMessagesViewModelFactory().create(CometChatPinnedMessagesViewModel::class.java)
    }

    LaunchedEffect(user?.uid, group?.guid) {
        vm.configure(user?.uid, group?.guid)
    }

    // Live upkeep: reflect pins/unpins made elsewhere while this screen is open.
    LaunchedEffect(Unit) {
        CometChatEvents.messageEvents.collect { event ->
            when (event) {
                is CometChatMessageEvent.MessagePinned -> vm.onMessagePinnedExternally(event.message)
                is CometChatMessageEvent.MessageUnpinned -> vm.onMessageUnpinnedExternally(event.message)
                else -> {}
            }
        }
    }

    val messages by vm.messages.collectAsState()
    val uiState by vm.uiState.collectAsState()

    // Same default text formatters as the message list (mentions), so @mentions/links render the
    // same way here — the pinned bubble is rendered exactly like the list except for alignment.
    val context = LocalContext.current
    val effectiveTextFormatters = textFormatters ?: remember(context) {
        listOf<CometChatTextFormatter>(CometChatMentionsFormatter(context))
    }

    // Own rows render LEFT-aligned but with the outgoing (purple) bubble, and the formatter picks the
    // mention style by alignment — LEFT yields the incoming look (primary purple), which is
    // illegible on purple. This variant remaps LEFT to the outgoing (white) mention style. Note the
    // setter is alignment-swapped: the "incoming" setter feeds the styles LEFT-aligned bubbles read.
    // A caller-supplied list is used for own and other rows alike, so this remap applies only to the
    // built-in default — a caller styling mentions themselves owns both looks.
    val ownMentionStyle = CometChatMentionStyle.outgoing()
    val ownTextFormatters = textFormatters ?: remember(context, ownMentionStyle) {
        listOf<CometChatTextFormatter>(
            CometChatMentionsFormatter(context).apply { setIncomingBubbleMentionStyle(ownMentionStyle) }
        )
    }

    var popupTarget by remember { mutableStateOf<BaseMessage?>(null) }
    var deleteTarget by remember { mutableStateOf<BaseMessage?>(null) }
    var unpinTarget by remember { mutableStateOf<BaseMessage?>(null) }
    var infoTarget by remember { mutableStateOf<BaseMessage?>(null) }

    // Routes a chosen option to its action. Shared by every row's menu.
    val onPinnedOptionSelected: (String, BaseMessage) -> Unit = { optionId, message ->
        when (optionId) {
            UIKitConstants.MessageOption.MESSAGE_INFORMATION -> infoTarget = message
            UIKitConstants.MessageOption.COPY -> copyMessageToClipboard(context, message)
            UIKitConstants.MessageOption.TRANSLATE -> vm.translate(message)
            UIKitConstants.MessageOption.PIN -> vm.pin(message)
            UIKitConstants.MessageOption.UNPIN -> unpinTarget = message
            UIKitConstants.MessageOption.DELETE -> deleteTarget = message
        }
    }

    // Surface action outcomes (failures) as toasts.
    LaunchedEffect(Unit) {
        vm.actionResult.collect { result ->
            val text: String? = when (result) {
                CometChatPinnedMessagesViewModel.PinnedActionResult.DELETE_FAILED,
                CometChatPinnedMessagesViewModel.PinnedActionResult.PIN_FAILED,
                CometChatPinnedMessagesViewModel.PinnedActionResult.TRANSLATE_FAILED ->
                    context.getString(R.string.cometchat_something_went_wrong)
                // The cap was hit: name it (from app settings) rather than a generic failure.
                CometChatPinnedMessagesViewModel.PinnedActionResult.PIN_LIMIT_REACHED ->
                    PinSaveUtils.pinnedMessagesLimit()
                        ?.let { context.getString(R.string.cometchat_pin_limit_reached, it) }
                        ?: context.getString(R.string.cometchat_pin_limit_reached_unknown)
                // Pin/unpin is offered to every member; the server enforces RBAC — a denial
                // surfaces as the shared permission toast.
                CometChatPinnedMessagesViewModel.PinnedActionResult.PERMISSION_DENIED ->
                    context.getString(R.string.cometchat_action_permission_denied)
                // Successful pin/delete update the list directly — no toast needed.
                CometChatPinnedMessagesViewModel.PinnedActionResult.DELETED,
                CometChatPinnedMessagesViewModel.PinnedActionResult.PINNED -> null
            }
            text?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CometChatTheme.colorScheme.backgroundColor1)
    ) {
        CometChatToolbar(
            title = stringResource(R.string.cometchat_pinned_messages),
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
                            icon = painterResource(com.cometchat.uikit.core.R.drawable.cometchat_ic_pin_empty),
                            iconTint = Color.Unspecified,
                            iconSize = 120.dp
                        ),
                        title = stringResource(R.string.cometchat_no_pinned_messages),
                        subtitle = stringResource(R.string.cometchat_no_pinned_messages_subtitle)
                    )
                }

                // Load failure (e.g. RBAC 403 on listPinnedMessages): same "Oops!" error copy the
                // conversations/message-list components show — but no retry button here (per
                // design). Action failures never land here — the ViewModel reserves Error for
                // load failures.
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
                            // Long press shows the option list — no scrim and no lifted copy of the
                            // bubble, matching the kotlin kit's pinned list.
                            val isMenuOpen = popupTarget?.id == message.id
                            Box(modifier = Modifier.fillMaxWidth()) {
                                PinnedMessageRow(
                                    message = message,
                                    textFormatters = effectiveTextFormatters,
                                    ownTextFormatters = ownTextFormatters,
                                    onClick = onMessageClick,
                                    onLongClick = { popupTarget = it }
                                )
                                // The menu hangs off an empty anchor at the row's end, so it opens
                                // on the right like the kotlin kit's. Anchoring it to the row itself
                                // would start-align it and strand it at the far left.
                                Box(modifier = Modifier.align(Alignment.BottomEnd)) {
                                    CometChatPopupMenu(
                                        expanded = isMenuOpen,
                                        onDismissRequest = { popupTarget = null },
                                        menuItems = if (isMenuOpen) {
                                            pinnedMenuItems(context, message, user, group)
                                        } else {
                                            emptyList()
                                        },
                                        onMenuItemClick = { id, _ ->
                                            popupTarget = null
                                            onPinnedOptionSelected(id, message)
                                        }
                                    ) {}
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Delete confirmation.
    deleteTarget?.let { message ->
        CometChatConfirmDialog(
            title = stringResource(R.string.cometchat_delete_message_title),
            subtitle = stringResource(R.string.cometchat_delete_message_subtitle),
            positiveButtonText = stringResource(R.string.cometchat_delete),
            negativeButtonText = stringResource(R.string.cometchat_cancel),
            icon = painterResource(com.cometchat.uikit.core.R.drawable.cometchat_ic_delete),
            style = CometChatConfirmDialogStyle.default(
                positiveButtonBackgroundColor = CometChatTheme.colorScheme.errorColor
            ),
            onPositiveClick = {
                vm.delete(message)
                deleteTarget = null
            },
            onNegativeClick = { deleteTarget = null },
            onDismiss = { deleteTarget = null }
        )
    }

    // Unpin confirmation — only unpin confirms (pin is immediate elsewhere).
    unpinTarget?.let { message ->
        CometChatConfirmDialog(
            title = stringResource(R.string.cometchat_unpin_message_confirm_title),
            subtitle = stringResource(R.string.cometchat_unpin_message_confirm_body),
            positiveButtonText = stringResource(R.string.cometchat_unpin),
            negativeButtonText = stringResource(R.string.cometchat_cancel),
            hideIcon = true,
            // Non-destructive: primary (purple) positive button, not the default red.
            style = CometChatConfirmDialogStyle.default(
                positiveButtonBackgroundColor = CometChatTheme.colorScheme.primary
            ),
            onPositiveClick = {
                vm.unpin(message)
                unpinTarget = null
            },
            onNegativeClick = { unpinTarget = null },
            onDismiss = { unpinTarget = null }
        )
    }

    // Message information overlay.
    infoTarget?.let { message ->
        CometChatMessageInformation(
            message = message,
            textFormatters = effectiveTextFormatters,
            onDismiss = { infoTarget = null }
        )
    }
}

/** The options shown on long-press of a pinned message, in display order. */
private val PINNED_OPTION_ORDER = listOf(
    UIKitConstants.MessageOption.MESSAGE_INFORMATION,
    UIKitConstants.MessageOption.COPY,
    // Text-only (the option map offers it for text messages); translates in place — the
    // bubble re-renders with the translation, exactly like the message list.
    UIKitConstants.MessageOption.TRANSLATE,
    UIKitConstants.MessageOption.PIN,
    UIKitConstants.MessageOption.UNPIN,
    // No THREAD_SUBSCRIPTION here, deliberately: a pinned thread message can be appended in
    // realtime over the websocket, and a socket-delivered message carries no threadSubscribed
    // flag — so this panel cannot know the thread's state and must not offer a toggle that
    // would render with a wrong label.
    UIKitConstants.MessageOption.DELETE
)

/**
 * Builds the long-press menu for a pinned row — Info / Copy / Translate / Pin / Unpin /
 * Subscribe to thread / Delete.
 *
 * Options come from [MessageOptionsUtils] so the per-message and permission rules apply (Copy only
 * for text, Delete only for own or admin, Unpin for the already-pinned rows), then are filtered and
 * ordered to the set this screen offers. Mirrors `PinnedMessagesAdapter.showOptionsMenu()` in the
 * kotlin kit, down to the destructive tint on Delete.
 */
@Composable
private fun pinnedMenuItems(
    context: Context,
    message: BaseMessage,
    user: User?,
    group: Group?
): List<MenuItem> {
    val errorColor = CometChatTheme.colorScheme.errorColor
    val options = remember(message.id) {
        MessageOptionsUtils.getDefaultMessageOptions(context, message, user, group)
            .filter { PINNED_OPTION_ORDER.contains(it.id) }
            .sortedBy { PINNED_OPTION_ORDER.indexOf(it.id) }
    }
    return options.map { option ->
        val isDelete = option.id == UIKitConstants.MessageOption.DELETE
        MenuItem(
            id = option.id,
            name = option.title,
            startIcon = if (option.icon != 0) painterResource(option.icon) else null,
            startIconTint = when {
                isDelete -> errorColor
                option.iconTintColor != 0 -> Color(option.iconTintColor)
                else -> null
            },
            textColor = when {
                isDelete -> errorColor
                option.titleColor != 0 -> Color(option.titleColor)
                else -> null
            }
        )
    }
}

/** Copies a text message's body to the clipboard (no-op for non-text messages). */
private fun copyMessageToClipboard(context: Context, message: BaseMessage) {
    val text = (message as? TextMessage)?.text ?: return
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("message", text))
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PinnedMessageRow(
    message: BaseMessage,
    textFormatters: List<CometChatTextFormatter>,
    ownTextFormatters: List<CometChatTextFormatter>,
    onClick: (BaseMessage) -> Unit,
    onLongClick: (BaseMessage) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            // No ripple on the row. Tap jumps to the message. Long-press is captured HERE, at the
            // row, the same way MessageListItem and SavedMessageRow do it: only the text bubble
            // forwards a long-press from its own gesture detector, so wiring it on the bubble
            // alone left every other row type (image, file, audio, poll, sticker…) without a menu
            // — and, on the pinned list, no menu at all in practice (ENG-38917).
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onClick(message) },
                onLongClick = { onLongClick(message) }
            )
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        PinnedMessageBubble(
            message = message,
            textFormatters = textFormatters,
            ownTextFormatters = ownTextFormatters,
            onLongClick = { onLongClick(message) }
        )
    }
}

/**
 * Renders a pinned message's bubble.
 *
 * Every bubble is LEFT-aligned so the avatar + "name • date" header always shows. The logged-in
 * user's own messages still carry the outgoing (purple) style and read "You"; everyone else's stay
 * incoming (gray). Passing the outgoing per-type styles gives purple bubble + white text + white
 * footer (the footer tint follows style.timestampTextColor) even in the LEFT layout.
 *
 */
@Composable
private fun PinnedMessageBubble(
    message: BaseMessage,
    textFormatters: List<CometChatTextFormatter>,
    ownTextFormatters: List<CometChatTextFormatter>,
    onLongClick: (() -> Unit)? = null
) {
    val isOwn = message.sender?.uid == CometChat.getLoggedInUser()?.uid
    val name = if (isOwn) stringResource(R.string.cometchat_you)
    else message.sender?.name ?: message.sender?.uid ?: ""

    CometChatMessageBubble(
            message = message,
            alignment = UIKitConstants.MessageBubbleAlignment.LEFT,
            shouldShowDefaultAvatar = true,
            // Long-press handled on the bubble content (not the full-width row).
            onLongClick = onLongClick,
            headerView = { PinnedBubbleHeader(name = name, timestamp = message.sentAt) },
            // Read-only list: no thread "N Replies" row under the bubble and no quoted-parent
            // reply preview above the content (empty slots override the defaults) — the pinned row
            // shows just the message itself. Reactions still show via the default footer, but
            // view-only — no reaction/add-reaction callbacks are wired here.
            threadView = {},
            replyView = {},
            incomingMessageBubbleStyle = if (isOwn) CometChatMessageBubbleStyle.outgoing() else null,
            textBubbleStyle = if (isOwn) CometChatTextBubbleStyle.outgoing() else null,
            imageBubbleStyle = if (isOwn) CometChatImageBubbleStyle.outgoing() else null,
            videoBubbleStyle = if (isOwn) CometChatVideoBubbleStyle.outgoing() else null,
            audioBubbleStyle = if (isOwn) CometChatAudioBubbleStyle.outgoing() else null,
            fileBubbleStyle = if (isOwn) CometChatFileBubbleStyle.outgoing() else null,
            // Multi-attachment grids resolve their style from alignment (here LEFT → incoming),
            // so own messages need the outgoing variants passed explicitly to get white text on
            // the purple bubble — otherwise file/image/video/audio grids render dark (incoming) text.
            imagesBubbleStyle = if (isOwn) CometChatImagesBubbleStyle.outgoing() else null,
            videosBubbleStyle = if (isOwn) CometChatVideosBubbleStyle.outgoing() else null,
            audiosBubbleStyle = if (isOwn) CometChatAudiosBubbleStyle.outgoing() else null,
            filesBubbleStyle = if (isOwn) CometChatFilesBubbleStyle.outgoing() else null,
            // Extension bubbles (poll/whiteboard-document/meeting) resolve their style from
            // alignment too (LEFT → incoming), so own messages need the outgoing variants as well —
            // otherwise their content renders in incoming (dark) colours on the purple bubble.
            // Stickers are deliberately NOT overridden: passing a sticker style would bypass the
            // bubble's sticker special-case (transparent outer background, like the message list)
            // and wrap the sticker in a purple card.
            pollBubbleStyle = if (isOwn) CometChatPollBubbleStyle.outgoing() else null,
            collaborativeBubbleStyle = if (isOwn) CometChatCollaborativeBubbleStyle.outgoing() else null,
            meetCallBubbleStyle = if (isOwn) CometChatMeetCallBubbleStyle.outgoing() else null,
            // Match the message list: mentions/links render via the same formatters. Receipts and
            // reactions already display through the bubble defaults (hideReceipts/hideReactions=false).
            // Own rows use the variant whose LEFT-alignment mention style is the outgoing (white)
            // look — the formatter, not mentionTextStyle, is what colours mention spans here, since
            // mentionTextStyle only applies on the interactive MentionText path (onMentionClick != null).
            textFormatters = if (isOwn) ownTextFormatters else textFormatters
        )
}

private val pinnedHeaderDateFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault())

/** The bubble header for a pinned row: "name • date" (name +2sp, highlight colour). */
@Composable
private fun PinnedBubbleHeader(name: String, timestamp: Long) {
    val nameStyle = CometChatTheme.typography.caption1Medium
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 2.dp)
    ) {
        Text(
            text = name,
            color = CometChatTheme.colorScheme.textColorHighlight,
            style = nameStyle.copy(fontSize = (nameStyle.fontSize.value + 2f).sp)
        )
        Text(
            text = " ${stringResource(R.string.cometchat_middot)} ",
            color = CometChatTheme.colorScheme.textColorSecondary,
            style = CometChatTheme.typography.caption1Regular
        )
        Text(
            text = if (timestamp > 0) pinnedHeaderDateFormat.format(Date(timestamp * 1000)) else "",
            color = CometChatTheme.colorScheme.textColorSecondary,
            style = CometChatTheme.typography.caption1Regular
        )
    }
}
