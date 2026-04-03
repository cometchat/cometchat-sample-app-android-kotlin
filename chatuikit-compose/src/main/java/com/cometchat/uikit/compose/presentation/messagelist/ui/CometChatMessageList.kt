package com.cometchat.uikit.compose.presentation.messagelist.ui

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.core.MessagesRequest
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.FlagReason
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.MediaMessage
import com.cometchat.chat.models.TextMessage
import com.cometchat.chat.models.User
import com.cometchat.uikit.compose.R
import com.cometchat.uikit.compose.presentation.messageinformation.style.CometChatMessageInformationStyle
import com.cometchat.uikit.compose.presentation.messageinformation.ui.CometChatMessageInformation
import com.cometchat.uikit.compose.presentation.messagelist.style.CometChatMessageListStyle
import com.cometchat.uikit.compose.presentation.reactionlist.style.CometChatReactionListStyle
import com.cometchat.uikit.compose.presentation.reactionlist.ui.CometChatReactionList
import com.cometchat.uikit.compose.presentation.emojikeyboard.ui.CometChatEmojiKeyboard
import com.cometchat.uikit.compose.presentation.messagelist.utils.getMessageAlignment
import com.cometchat.uikit.compose.presentation.messagelist.utils.shouldShowDateSeparator
import com.cometchat.uikit.compose.presentation.messagelist.utils.SwipeToReplyWrapper
import com.cometchat.uikit.compose.presentation.report.CometChatFlagMessageDialog
import com.cometchat.uikit.compose.presentation.shared.aiconversationstarter.CometChatAIConversationStarterStyle
import com.cometchat.uikit.compose.presentation.shared.aiconversationstarter.CometChatAIConversationStarterView
import com.cometchat.uikit.compose.presentation.shared.aiconversationsummary.CometChatAIConversationSummaryStyle
import com.cometchat.uikit.compose.presentation.shared.aiconversationsummary.CometChatAIConversationSummaryView
import com.cometchat.uikit.compose.presentation.shared.aismartreplies.CometChatAISmartRepliesStyle
import com.cometchat.uikit.compose.presentation.shared.aismartreplies.CometChatAISmartRepliesView
import com.cometchat.uikit.compose.presentation.shared.dialog.CometChatConfirmDialog
import com.cometchat.uikit.compose.presentation.shared.dialog.CometChatConfirmDialogStyle
import com.cometchat.uikit.compose.presentation.shared.formatters.CometChatMentionsFormatter
import com.cometchat.uikit.compose.presentation.shared.formatters.CometChatTextFormatter
import com.cometchat.uikit.compose.presentation.shared.formatters.FormatterUtils
import com.cometchat.uikit.compose.presentation.shared.messagebubble.BubbleFactory
import com.cometchat.uikit.compose.presentation.shared.messagebubble.toFactoryMap
import com.cometchat.uikit.compose.presentation.shared.messagebubble.ui.CometChatMessageBubble
import com.cometchat.uikit.compose.presentation.shared.messagebubble.ui.buildFactoryKey
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.data.repository.MessageListRepositoryImpl
import com.cometchat.uikit.core.domain.model.CometChatMessageOption
import com.cometchat.uikit.core.events.CometChatEvents
import com.cometchat.uikit.core.events.CometChatUIEvent
import com.cometchat.uikit.core.state.ConversationStarterUIState
import com.cometchat.uikit.core.state.ConversationSummaryUIState
import com.cometchat.uikit.core.state.MessageAlignment
import com.cometchat.uikit.core.state.MessageFlagState
import com.cometchat.uikit.core.state.MessageListUIState
import com.cometchat.uikit.core.state.SmartRepliesUIState
import com.cometchat.uikit.core.utils.MessageOptionsUtils
import com.cometchat.uikit.core.viewmodel.CometChatMessageListViewModel
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * A composable that displays a list of messages in a chat interface.
 *
 * This composable provides:
 * - Message display in reverse chronological order (newest at bottom)
 * - Automatic pagination when scrolling
 * - Real-time message updates via ViewModel
 * - Date separators between messages from different days
 * - New message indicator when scrolled up
 * - Customizable slot providers for all bubble areas
 * - Intelligent avatar visibility based on conversation type
 *
 * ## Avatar Visibility Behavior
 *
 * Avatar visibility is automatically managed based on message direction and conversation type:
 *
 * - **Outgoing messages (RIGHT alignment)**: Avatars are always hidden by default
 * - **Incoming messages in group conversations**: Avatars are shown by default
 * - **Incoming messages in 1-on-1 conversations**: Avatars are hidden by default
 * - **Action messages (CENTER alignment)**: Avatars are never shown
 *
 * The [hideAvatar] parameter takes precedence and will hide all avatars when set to `true`.
 *
 * ### Avatar Visibility Examples
 *
 * ```kotlin
 * // Group conversation - incoming messages show avatars by default
 * CometChatMessageList(
 *     group = myGroup
 * )
 *
 * // 1-on-1 conversation - avatars hidden by default
 * CometChatMessageList(
 *     user = myUser
 * )
 *
 * // Force hide all avatars regardless of conversation type
 * CometChatMessageList(
 *     group = myGroup,
 *     hideAvatar = true
 * )
 *
 * // Custom avatar logic using leadingView slot
 * CometChatMessageList(
 *     user = myUser,
 *     leadingView = { message, alignment ->
 *         // Custom leadingView overrides default avatar visibility
 *         if (alignment == MessageAlignment.LEFT) {
 *             CometChatAvatar(user = message.sender)
 *         }
 *     }
 * )
 * ```
 *
 * ## Slot Provider Callbacks
 *
 * **IMPORTANT:** This composable accepts slot provider callbacks that receive BOTH
 * the message AND its alignment. This allows customization based on message direction:
 *
 * ```kotlin
 * CometChatMessageList(
 *     user = user,
 *     leadingView = { message, alignment ->
 *         if (alignment == MessageAlignment.LEFT) {
 *             CometChatAvatar(user = message.sender)
 *         }
 *     },
 *     statusInfoView = { message, alignment ->
 *         if (alignment == MessageAlignment.RIGHT) {
 *             MessageReceipts(message = message)
 *         }
 *     }
 * )
 * ```
 *
 * ## MessageAlignment Values
 *
 * - [MessageAlignment.LEFT]: Incoming messages from other users
 * - [MessageAlignment.RIGHT]: Outgoing messages from current user
 * - [MessageAlignment.CENTER]: Action/system messages (group actions, call actions)
 *
 * ## Timestamp Alignment
 *
 * Control where timestamps are displayed using [timeStampAlignment]:
 *
 * - [UIKitConstants.TimeStampAlignment.TOP]: Timestamp appears in the header alongside sender name
 * - [UIKitConstants.TimeStampAlignment.BOTTOM]: Timestamp appears in the status info view (default)
 *
 * ```kotlin
 * // Show timestamp at the top of message bubbles
 * CometChatMessageList(
 *     user = user,
 *     timeStampAlignment = UIKitConstants.TimeStampAlignment.TOP
 * )
 * ```
 *
 * ## Message Alignment
 *
 * Control the overall layout of messages using [messageAlignment]:
 *
 * - [UIKitConstants.MessageListAlignment.STANDARD]: Outgoing messages on right, incoming on left (default)
 * - [UIKitConstants.MessageListAlignment.LEFT_ALIGNED]: All messages aligned to the left
 *
 * ```kotlin
 * // All messages aligned to the left (like Slack)
 * CometChatMessageList(
 *     user = user,
 *     messageAlignment = UIKitConstants.MessageListAlignment.LEFT_ALIGNED
 * )
 * ```
 *
 * ## AI Features
 *
 * Enable AI-powered features to enhance the chat experience:
 *
 * ```kotlin
 * CometChatMessageList(
 *     user = user,
 *     enableConversationStarter = true,  // Show AI conversation starters
 *     enableSmartReplies = true,          // Show AI smart reply suggestions
 *     enableConversationSummary = true    // Enable AI conversation summaries
 * )
 * ```
 *
 * ## BubbleFactory Integration
 *
 * Content rendering is handled internally by default for all standard message types.
 * Custom [BubbleFactory] instances can be provided via [bubbleFactories] parameter
 * for custom message types or to override default rendering:
 *
 * ```kotlin
 * val customFactories = listOf(
 *     MyLocationBubbleFactory()
 * )
 *
 * CometChatMessageList(
 *     user = user,
 *     bubbleFactories = customFactories
 * )
 * ```
 *
 * @param modifier Modifier for the root composable
 * @param viewModel Optional ViewModel instance. If not provided, a new instance is created
 *   internally with default configuration.
 * @param style Visual style configuration for the message list. Defaults to
 *   [CometChatMessageListStyle.default].
 * @param user The user for a 1-on-1 conversation. When set, avatars are hidden for incoming
 *   messages by default. Mutually exclusive with [group] - if both are provided, [group]
 *   takes precedence.
 * @param group The group for a group conversation. When set, avatars are shown for incoming
 *   messages by default. Takes precedence over [user] if both are provided.
 * @param parentMessageId Parent message ID for threaded conversations. Use -1 (default) for
 *   the main conversation thread.
 * @param messagesRequestBuilder Optional custom builder for message requests. Allows
 *   customization of message fetching parameters.
 * @param scrollToBottomOnNewMessage Whether to automatically scroll to the bottom when new
 *   messages arrive and the user is already at the bottom. Defaults to `true`.
 * @param swipeToReplyEnabled Whether the swipe-to-reply gesture is enabled. Defaults to `true`.
 * @param autoFetch Whether to automatically fetch messages when the component initializes.
 *   Defaults to `true`.
 * @param startFromUnreadMessages Whether to start the message list from the first unread
 *   message. Defaults to `false`.
 * @param unreadMessageThreshold The threshold count for showing the unread message anchor.
 *   Defaults to 30.
 * @param disableSoundForMessages Whether to disable notification sounds for incoming messages.
 *   Defaults to `false`.
 * @param disableReceipt Whether to disable sending read receipts when messages are viewed.
 *   Defaults to `false`.
 * @param hideLoadingState Whether to hide the loading state view during initial fetch.
 *   Defaults to `false`.
 * @param hideEmptyState Whether to hide the empty state view when no messages exist.
 *   Defaults to `false`.
 * @param hideErrorState Whether to hide the error state view when an error occurs.
 *   Defaults to `false`.
 * @param hideAvatar Whether to hide avatars in message bubbles. When `true`, avatars are
 *   hidden for all messages regardless of conversation type. When `false` (default), avatar
 *   visibility follows the conversation type rules:
 *   - Group conversations: Show avatars for incoming messages
 *   - 1-on-1 conversations: Hide avatars for incoming messages
 *   - Outgoing messages: Always hide avatars
 * @param hideReceipts Whether to hide read/delivery receipt indicators. Defaults to `false`.
 * @param hideGroupActionMessages Whether to hide group action messages (member joined, left,
 *   etc.). Defaults to `false`.
 * @param hideDateSeparator Whether to hide date separators between messages from different
 *   days. Defaults to `false`.
 * @param hideReplyInThreadOption Whether to hide the "Reply in Thread" option in message
 *   options menu. Defaults to `false`.
 * @param hideReplyOption Whether to hide the "Reply" option in message options menu.
 *   Defaults to `false`.
 * @param hideCopyMessageOption Whether to hide the "Copy" option in message options menu.
 *   Defaults to `false`.
 * @param hideEditMessageOption Whether to hide the "Edit" option in message options menu.
 *   Defaults to `false`.
 * @param hideDeleteMessageOption Whether to hide the "Delete" option in message options menu.
 *   Defaults to `false`.
 * @param hideShareMessageOption Whether to hide the "Share" option in message options menu.
 *   Defaults to `false`.
 * @param hideMessagePrivatelyOption Whether to hide the "Message Privately" option in message
 *   options menu. Defaults to `false`.
 * @param hideMessageInfoOption Whether to hide the "Message Info" option in message options
 *   menu. Defaults to `false`.
 * @param hideMarkAsUnreadOption Whether to hide the "Mark as Unread" option in message
 *   options menu. Defaults to `false`.
 * @param hideTranslateMessageOption Whether to hide the "Translate" option in message options
 *   menu. Defaults to `false`.
 * @param hideMessageReactionOption Whether to hide the reaction option in message options
 *   menu. Defaults to `false`.
 * @param hideFlagOption Whether to hide the flag/report option in message options menu.
 *   When `true`, users cannot report messages. Defaults to `false`.
 * @param hideFlagRemarkInputField Whether to hide the remark input field in the flag message
 *   dialog. When `true`, users can only select a reason without adding additional remarks.
 *   Defaults to `false`.
 * @param hideAiAssistantSuggestedMessages Whether to hide AI assistant suggested messages.
 *   When `true`, AI-generated message suggestions are not displayed. Defaults to `false`.
 * @param hideModerationView Whether to hide the moderation view in message bubbles. When
 *   `true`, moderation indicators (e.g., flagged content warnings) are not shown.
 *   Defaults to `false`.
 * @param hideStickyDate Whether to hide the sticky date header that appears at the top of
 *   the message list while scrolling. Defaults to `false`.
 * @param timeStampAlignment Controls where the timestamp is displayed in message bubbles.
 *   - [UIKitConstants.TimeStampAlignment.TOP]: Timestamp in header view with sender name
 *   - [UIKitConstants.TimeStampAlignment.BOTTOM]: Timestamp in status info view (default)
 * @param messageAlignment Controls the overall alignment of messages in the list.
 *   - [UIKitConstants.MessageListAlignment.STANDARD]: Outgoing right, incoming left (default)
 *   - [UIKitConstants.MessageListAlignment.LEFT_ALIGNED]: All messages aligned to the left
 * @param enableConversationStarter Whether to enable AI conversation starters. When `true`,
 *   AI-generated conversation starter suggestions are shown to help users begin conversations.
 *   Defaults to `false`.
 * @param enableSmartReplies Whether to enable AI smart replies. When `true`, AI-generated
 *   smart reply suggestions are shown based on the conversation context. Defaults to `false`.
 * @param enableConversationSummary Whether to enable AI conversation summary. When `true`,
 *   AI-generated conversation summaries are available to provide quick overviews of the
 *   conversation. Defaults to `false`.
 * @param conversationStarterStyle Style configuration for the AI conversation starter view.
 *   Allows customization of colors, typography, and spacing for the conversation starter
 *   suggestions displayed in the empty state.
 * @param conversationStarterView Custom composable for the conversation starter view.
 *   When provided, this replaces the default CometChatAIConversationStarterView.
 *   Receives the UI state and an onClick callback.
 * @param conversationSummaryStyle Style configuration for the AI conversation summary view.
 *   Allows customization of colors, typography, and spacing for the conversation summary
 *   displayed at the top of the message list.
 * @param conversationSummaryView Custom composable for the conversation summary view.
 *   When provided, this replaces the default CometChatAIConversationSummaryView.
 *   Receives the UI state and an onCloseClick callback.
 * @param smartRepliesStyle Style configuration for the AI smart replies view.
 *   Allows customization of colors, typography, and spacing for the smart reply
 *   suggestions displayed above the message composer.
 * @param smartRepliesView Custom composable for the smart replies view.
 *   When provided, this replaces the default CometChatAISmartRepliesView.
 *   Receives the UI state, an onClick callback for reply selection, and an onCloseClick callback.
 * @param bubbleFactories List of [BubbleFactory] instances for custom content rendering.
 *   Each factory declares its own category and type. The list is converted to an internal map
 *   once at this level. Defaults to empty list which uses internal rendering for all standard
 *   message types. Custom factories can be provided for custom message types.
 * @param leadingView Provider for the leading area of message bubbles. Receives the message
 *   and its alignment. When provided, this overrides the default avatar visibility logic.
 * @param headerView Provider for the header area of message bubbles. Receives the message
 *   and its alignment.
 * @param replyView Provider for the reply preview area of message bubbles. Receives the
 *   message and its alignment.
 * @param contentView Provider to override factory-based content rendering. Receives the
 *   message and its alignment.
 * @param bottomView Provider for the bottom area of message bubbles. Receives the message
 *   and its alignment.
 * @param statusInfoView Provider for the status info area (timestamp, receipts). Receives
 *   the message and its alignment.
 * @param threadView Provider for the thread indicator area. Receives the message and its
 *   alignment.
 * @param footerView Provider for the footer area of message bubbles. Receives the message
 *   and its alignment.
 * @param loadingView Custom composable for the loading state view.
 * @param emptyView Custom composable for the empty state view.
 * @param errorView Custom composable for the error state view. Receives a retry callback.
 * @param newMessageIndicatorView Custom composable for the new message indicator. Receives
 *   the count of new messages and an onClick callback.
 * @param quickReactions List of emoji strings for quick reactions. Defaults to common emojis.
 * @param onError Callback invoked when an error occurs during message operations.
 * @param onLoad Callback invoked when messages are successfully loaded.
 * @param onEmpty Callback invoked when the message list is empty.
 * @param onMessageClick Callback invoked when a message is clicked.
 * @param onMessageLongClick Callback invoked when a message is long-clicked.
 * @param onThreadRepliesClick Callback invoked when the thread indicator is clicked.
 * @param onReactionClick Callback invoked when a reaction is clicked.
 * @param onReactionLongClick Callback invoked when a reaction is long-clicked.
 * @param onAddMoreReactionsClick Callback invoked when the add reaction button is clicked.
 * @param onMessageOptionClick Callback invoked when a message option is clicked. Receives the message, option ID, and option display name.
 * @param onEmojiPickerClick Callback invoked when the emoji picker is requested.
 * @param onQuickReactionClick Callback invoked when a quick reaction is clicked.
 * @param onMessageEdit Callback invoked when edit message is requested.
 * @param onMessageReply Callback invoked when reply to message is requested.
 * @param onConversationStarterClick Callback invoked when a conversation starter is clicked.
 *   If provided, this callback is invoked instead of the default behavior (emitting
 *   ComposeMessage event). The callback receives the starter text and its position in the list.
 * @param onSmartReplyClick Callback invoked when a smart reply is clicked.
 *   If provided, this callback is invoked instead of the default behavior (emitting
 *   ComposeMessage event). The callback receives the reply text and its position in the list.
 *
 * @see CometChatMessageListStyle
 * @see CometChatMessageListViewModel
 * @see MessageAlignment
 * @see BubbleFactory
 * @see UIKitConstants.TimeStampAlignment
 * @see UIKitConstants.MessageListAlignment
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CometChatMessageList(
    modifier: Modifier = Modifier,
    viewModel: CometChatMessageListViewModel? = null,
    style: CometChatMessageListStyle = CometChatMessageListStyle.default(),
    
    // Configuration
    user: User? = null,
    group: Group? = null,
    parentMessageId: Long = -1,
    messagesRequestBuilder: MessagesRequest.MessagesRequestBuilder? = null,
    
    // Behavior
    scrollToBottomOnNewMessage: Boolean = true,
    swipeToReplyEnabled: Boolean = true,
    autoFetch: Boolean = true,
    startFromUnreadMessages: Boolean = false,
    unreadMessageThreshold: Int = 30,
    disableSoundForMessages: Boolean = false,
    disableReceipt: Boolean = false,
    
    // Visibility controls
    hideLoadingState: Boolean = false,
    hideEmptyState: Boolean = false,
    hideErrorState: Boolean = false,
    hideAvatar: Boolean = false,
    hideReceipts: Boolean = false,
    hideGroupActionMessages: Boolean = false,
    hideDateSeparator: Boolean = false,
    
    // Message option visibility
    hideReplyInThreadOption: Boolean = false,
    hideReplyOption: Boolean = false,
    hideCopyMessageOption: Boolean = false,
    hideEditMessageOption: Boolean = false,
    hideDeleteMessageOption: Boolean = false,
    hideShareMessageOption: Boolean = false,
    hideMessagePrivatelyOption: Boolean = false,
    hideMessageInfoOption: Boolean = false,
    hideMarkAsUnreadOption: Boolean = false,
    hideTranslateMessageOption: Boolean = false,
    hideMessageReactionOption: Boolean = false,
    hideFlagOption: Boolean = false,
    hideFlagRemarkInputField: Boolean = false,
    hideAiAssistantSuggestedMessages: Boolean = false,
    hideModerationView: Boolean = false,
    hideStickyDate: Boolean = false,

    /** Whether to hide the "New Messages" separator above the first unread message. */
    hideNewMessagesSeparator: Boolean = false,
    
    // Alignment controls
    timeStampAlignment: UIKitConstants.TimeStampAlignment = UIKitConstants.TimeStampAlignment.BOTTOM,
    messageAlignment: UIKitConstants.MessageListAlignment = UIKitConstants.MessageListAlignment.STANDARD,
    
    // AI feature controls
    /**
     * Enable AI conversation starters.
     * When true, AI-generated conversation starter suggestions will be shown
     * to help users begin conversations.
     */
    enableConversationStarter: Boolean = false,
    /**
     * Enable AI smart replies.
     * When true, AI-generated smart reply suggestions will be shown
     * based on the conversation context.
     */
    enableSmartReplies: Boolean = false,
    /**
     * Enable AI conversation summary.
     * When true, AI-generated conversation summaries will be available
     * to provide quick overviews of the conversation.
     */
    enableConversationSummary: Boolean = false,

    /**
     * Style configuration for the AI conversation starter view.
     * Allows customization of colors, typography, and spacing for the
     * conversation starter suggestions displayed in the empty state.
     */
    conversationStarterStyle: CometChatAIConversationStarterStyle = CometChatAIConversationStarterStyle.default(),

    /**
     * Custom composable for the conversation starter view.
     * When provided, this replaces the default CometChatAIConversationStarterView.
     * Receives the UI state and an onClick callback.
     */
    conversationStarterView: (@Composable (
        uiState: ConversationStarterUIState,
        onClick: (starter: String, position: Int) -> Unit
    ) -> Unit)? = null,

    /**
     * Style configuration for the AI conversation summary view.
     * Allows customization of colors, typography, and spacing for the
     * conversation summary displayed at the top of the message list.
     */
    conversationSummaryStyle: CometChatAIConversationSummaryStyle = CometChatAIConversationSummaryStyle.default(),

    /**
     * Custom composable for the conversation summary view.
     * When provided, this replaces the default CometChatAIConversationSummaryView.
     * Receives the UI state and an onCloseClick callback.
     */
    conversationSummaryView: (@Composable (
        uiState: ConversationSummaryUIState,
        onCloseClick: () -> Unit
    ) -> Unit)? = null,

    /**
     * Style configuration for the AI smart replies view.
     * Allows customization of colors, typography, and spacing for the
     * smart reply suggestions displayed above the message composer.
     */
    smartRepliesStyle: CometChatAISmartRepliesStyle = CometChatAISmartRepliesStyle.default(),

    /**
     * Custom composable for the smart replies view.
     * When provided, this replaces the default CometChatAISmartRepliesView.
     * Receives the UI state, an onClick callback for reply selection,
     * and an onCloseClick callback for dismissal.
     */
    smartRepliesView: (@Composable (
        uiState: SmartRepliesUIState,
        onClick: (reply: String, position: Int) -> Unit,
        onCloseClick: () -> Unit
    ) -> Unit)? = null,
    
    // Reaction list style
    reactionListStyle: CometChatReactionListStyle = CometChatReactionListStyle.default(),
    
    // Text formatters
    textFormatters: List<CometChatTextFormatter>? = null,

    // BubbleFactory integration
    bubbleFactories: List<BubbleFactory> = emptyList(),
    
    // Slot provider callbacks (receive message AND alignment)
    leadingView: (@Composable (BaseMessage, MessageAlignment) -> Unit)? = null,
    headerView: (@Composable (BaseMessage, MessageAlignment) -> Unit)? = null,
    replyView: (@Composable (BaseMessage, MessageAlignment) -> Unit)? = null,
    contentView: (@Composable (BaseMessage, MessageAlignment) -> Unit)? = null,
    bottomView: (@Composable (BaseMessage, MessageAlignment) -> Unit)? = null,
    statusInfoView: (@Composable (BaseMessage, MessageAlignment) -> Unit)? = null,
    threadView: (@Composable (BaseMessage, MessageAlignment) -> Unit)? = null,
    footerView: (@Composable (BaseMessage, MessageAlignment) -> Unit)? = null,
    
    // Custom state views
    loadingView: (@Composable () -> Unit)? = null,
    emptyView: (@Composable () -> Unit)? = null,
    errorView: (@Composable (onRetry: () -> Unit) -> Unit)? = null,
    newMessageIndicatorView: (@Composable (count: Int, onClick: () -> Unit) -> Unit)? = null,

    /** Custom composable for the "New Messages" separator above the first unread message. */
    newMessagesSeparatorView: (@Composable () -> Unit)? = null,
    
    // Quick reactions
    quickReactions: List<String> = listOf("👍", "❤️", "😂", "😮", "😢", "🙏"),
    
    // Message options customization
    /**
     * Callback that **replaces** the default message options for a given message.
     *
     * When this callback returns a non-null list, that list is used as the entire set of
     * options (default options are discarded). When it returns `null`, the default options
     * are shown as usual.
     *
     * Takes precedence over [addOptions] — when this returns a non-null list,
     * [addOptions] is not invoked.
     */
    options: ((BaseMessage) -> List<CometChatMessageOption>?)? = null,
    /**
     * Callback that **appends** additional options after the default message options.
     *
     * Invoked only when [options] is not set or returns `null`.
     */
    addOptions: ((BaseMessage) -> List<CometChatMessageOption>)? = null,
    
    // Callbacks
    onError: ((CometChatException) -> Unit)? = null,
    onLoad: ((List<BaseMessage>) -> Unit)? = null,
    onEmpty: (() -> Unit)? = null,
    onMessageClick: ((BaseMessage) -> Unit)? = null,
    onMessageLongClick: ((BaseMessage) -> Unit)? = null,
    onThreadRepliesClick: ((BaseMessage) -> Unit)? = null,
    onReactionClick: ((BaseMessage, String) -> Unit)? = null,
    onReactionLongClick: ((BaseMessage, String) -> Unit)? = null,
    onAddMoreReactionsClick: ((BaseMessage) -> Unit)? = null,
    onMessageOptionClick: ((BaseMessage, String, String) -> Unit)? = null,
    onEmojiPickerClick: ((BaseMessage) -> Unit)? = null,
    onQuickReactionClick: ((BaseMessage, String) -> Unit)? = null,
    onMessageEdit: ((BaseMessage) -> Unit)? = null,
    onMessageReply: ((BaseMessage) -> Unit)? = null,
    /**
     * Callback invoked when a conversation starter is clicked.
     * If provided, this callback is invoked instead of the default behavior
     * (emitting ComposeMessage event). The callback receives the starter text
     * and its position in the list.
     */
    onConversationStarterClick: ((starter: String, position: Int) -> Unit)? = null,
    /**
     * Callback invoked when a smart reply is clicked.
     * If provided, this callback is invoked instead of the default behavior
     * (emitting ComposeMessage event). The callback receives the reply text
     * and its position in the list.
     */
    onSmartReplyClick: ((reply: String, position: Int) -> Unit)? = null,
    /**
     * Callback invoked when the "Message Privately" option is selected.
     * The callback receives the fetched [User] object representing the message sender.
     * This allows integrators to navigate to a private conversation with the sender.
     */
    onMessagePrivately: ((User) -> Unit)? = null
) {
    // ========================================
    // State Management (Task 39)
    // ========================================
    
    // Convert list of factories to map once, keyed by "category_type"
    val factoryMap = remember(bubbleFactories) {
        bubbleFactories.toFactoryMap()
    }

    // Build option visibility map from hide* parameters (negated: !hide* → visible)
    val optionVisibilityMap = remember(
        hideReplyInThreadOption,
        hideReplyOption,
        hideCopyMessageOption,
        hideEditMessageOption,
        hideDeleteMessageOption,
        hideShareMessageOption,
        hideMessagePrivatelyOption,
        hideMessageInfoOption,
        hideMarkAsUnreadOption,
        hideTranslateMessageOption,
        hideMessageReactionOption,
        hideFlagOption
    ) {
        mapOf(
            UIKitConstants.MessageOption.REPLY_IN_THREAD to !hideReplyInThreadOption,
            UIKitConstants.MessageOption.REPLY to !hideReplyOption,
            UIKitConstants.MessageOption.REPLY_TO_MESSAGE to !hideReplyOption,
            UIKitConstants.MessageOption.COPY to !hideCopyMessageOption,
            UIKitConstants.MessageOption.EDIT to !hideEditMessageOption,
            UIKitConstants.MessageOption.DELETE to !hideDeleteMessageOption,
            UIKitConstants.MessageOption.TRANSLATE to !hideTranslateMessageOption,
            UIKitConstants.MessageOption.SHARE to !hideShareMessageOption,
            UIKitConstants.MessageOption.MESSAGE_PRIVATELY to !hideMessagePrivatelyOption,
            UIKitConstants.MessageOption.MESSAGE_INFORMATION to !hideMessageInfoOption,
            UIKitConstants.MessageOption.REPORT to !hideFlagOption,
            UIKitConstants.MessageOption.MARK_AS_UNREAD to !hideMarkAsUnreadOption,
            UIKitConstants.MessageOption.REACT to !hideMessageReactionOption
        )
    }

    // Get or create ViewModel
    val vm = viewModel ?: remember {
        CometChatMessageListViewModel(
            repository = MessageListRepositoryImpl(),
            enableListeners = true
        )
    }

    // Derive isThreadView from parentMessageId
    val isThreadView = parentMessageId > 0

    // Wire options/addOptions callbacks into the ViewModel
    LaunchedEffect(vm, options, addOptions) {
        if (options != null) vm.setOptions(options)
        if (addOptions != null) vm.addOptions(addOptions)
    }

    // Context for MessageOptionsUtils
    val context = LocalContext.current
    
    // Coroutine scope for async operations (e.g., image sharing)
    val scope = rememberCoroutineScope()

    // Create default text formatters if none provided.
    // Matches kotlin reference's processMentionsFormatter(): creates a CometChatMentionsFormatter
    // and uses it as the default formatter list for text message rendering.
    val effectiveTextFormatters = textFormatters ?: remember(context) {
        listOf(CometChatMentionsFormatter(context))
    }

    // State for the long-pressed message and its computed options
    var longPressedMessage by remember { mutableStateOf<BaseMessage?>(null) }
    var messageOptions by remember { mutableStateOf<List<CometChatMessageOption>>(emptyList()) }
    var showPopupMenu by remember { mutableStateOf(false) }

    // State for delete confirmation dialog
    var deleteConfirmationMessage by remember { mutableStateOf<BaseMessage?>(null) }

    // State for message information bottom sheet
    var showMessageInformation by remember { mutableStateOf(false) }
    var messageInformationMessage by remember { mutableStateOf<BaseMessage?>(null) }
    
    // State for flag message dialog
    var showFlagDialog by remember { mutableStateOf(false) }
    var flagDialogMessage by remember { mutableStateOf<BaseMessage?>(null) }
    var flagReasons by remember { mutableStateOf<List<FlagReason>>(emptyList()) }
    var showFlagError by remember { mutableStateOf(false) }
    var showFlagProgress by remember { mutableStateOf(false) }

    // State for reaction list bottom sheet
    var showReactionListSheet by remember { mutableStateOf(false) }
    var reactionListMessage by remember { mutableStateOf<BaseMessage?>(null) }
    var reactionListSelectedEmoji by remember { mutableStateOf<String?>(null) }

    // State for emoji keyboard bottom sheet (add more reactions)
    var showEmojiKeyboardSheet by remember { mutableStateOf(false) }
    var emojiKeyboardMessage by remember { mutableStateOf<BaseMessage?>(null) }

    // Build message options using the core pipeline when a message is long-pressed
    val deleteErrorColor = CometChatTheme.colorScheme.errorColor
    val handleMessageLongClick: (BaseMessage) -> Unit = remember(
        optionVisibilityMap, user, group, isThreadView
    ) {
        handleLongClick@{ message: BaseMessage ->
            // Skip showing options for in-progress or deleted messages
            if (message.id == 0L || message.sentAt == 0L || message.deletedAt > 0) {
                return@handleLongClick
            }

            // Step 1: Get default options with business rules applied
            val defaults = MessageOptionsUtils.getDefaultMessageOptions(
                context, message, user, group, isThreadView
            )
            // Step 2: Filter by presentation-layer visibility map
            val filtered = MessageOptionsUtils.getFilteredMessageOptions(defaults, optionVisibilityMap)
            // Step 3: Apply error color to delete option (core module can't resolve theme colors)
            val errorColorInt = deleteErrorColor.toArgb()
            val themed = filtered.map { option ->
                if (option.id == UIKitConstants.MessageOption.DELETE && option.titleColor == 0) {
                    option.copy(titleColor = errorColorInt, iconTintColor = errorColorInt)
                } else {
                    option
                }
            }
            // Step 4: Resolve final options via ViewModel callbacks (setOptions / addOptions)
            val finalOptions = vm.resolveMessageOptions(message, themed)

            longPressedMessage = message
            messageOptions = finalOptions
            showPopupMenu = true

            // Still invoke the integrator's callback
            onMessageLongClick?.invoke(message)
        }
    }

    /**
     * Handles a message option selection from the option sheet.
     *
     * Flow:
     * 1. Delegates to ViewModel for business-logic actions (copy, edit, reply, delete, mark unread, translate)
     * 2. If ViewModel doesn't handle it, handles locally for UI-context actions (thread, share, info, report, message privately)
     * 3. Always invokes the integrator's [onMessageOptionClick] callback
     */
    val handleMessageOptionSelected: (String, BaseMessage) -> Unit = remember(vm, context, textFormatters) {
        { optionId: String, message: BaseMessage ->
            // Create text formatter callback if formatters are available
            val textFormatterCallback = if (!textFormatters.isNullOrEmpty()) {
                CometChatMessageListViewModel.TextFormatterCallback { ctx, msg, formattingType, alignment, text ->
                    FormatterUtils.getFormattedText(ctx, msg, formattingType, alignment, text, textFormatters)
                }
            } else {
                null
            }

            // 1. Delegate to ViewModel for business-logic actions
            val handled = vm.handleMessageOptionClick(context, optionId, message, textFormatterCallback)

            // 2. If ViewModel didn't handle it, handle locally (UI-context actions)
            if (!handled) {
                when (optionId) {
                    UIKitConstants.MessageOption.REPLY_IN_THREAD -> {
                        onThreadRepliesClick?.invoke(message)
                    }
                    UIKitConstants.MessageOption.SHARE -> {
                        shareMessage(context, message, scope, textFormatters)
                    }
                    UIKitConstants.MessageOption.MESSAGE_INFORMATION -> {
                        // Show message information bottom sheet
                        messageInformationMessage = message
                        showMessageInformation = true
                    }
                    UIKitConstants.MessageOption.REPORT -> {
                        // Show flag message dialog
                        flagDialogMessage = message
                        showFlagError = false
                        showFlagProgress = false
                        // Fetch flag reasons from SDK
                        CometChat.getFlagReasons(object : CometChat.CallbackListener<List<FlagReason>>() {
                            override fun onSuccess(reasons: List<FlagReason>?) {
                                flagReasons = reasons ?: emptyList()
                                showFlagDialog = true
                            }
                            override fun onError(e: CometChatException?) {
                                flagReasons = emptyList()
                                showFlagDialog = true
                            }
                        })
                    }
                    UIKitConstants.MessageOption.MESSAGE_PRIVATELY -> {
                        // Fetch the message sender
                        vm.fetchMessageSender(message)
                    }
                }
            }

            // 3. Always invoke integrator callback with optionName from messageOptions
            val optionName = messageOptions.find { it.id == optionId }?.title ?: optionId
            onMessageOptionClick?.invoke(message, optionId, optionName)

            // Clear the long-pressed state
            longPressedMessage = null
            messageOptions = emptyList()
        }
    }

    // Collect state flows
    val uiState by vm.uiState.collectAsState()
    val messagesFromVm by vm.messages.collectAsState()
    val isInProgress by vm.isInProgress.collectAsState()
    val scrollToMessageId by vm.scrollToMessageId.collectAsState()
    val unreadCount by vm.unreadCount.collectAsState()
    val unreadMessageAnchor by vm.unreadMessageAnchor.collectAsState()
    val hasMoreNewMessages by vm.hasMoreNewMessages.collectAsState()
    val hasMorePreviousMessages by vm.hasMorePreviousMessages.collectAsState()
    val highlightScroll by vm.highlightScroll.collectAsState()

    // Conversation starter state flows
    val conversationStarterUIState by vm.conversationStarterUIState.collectAsState()
    
    // Conversation summary state flows
    val conversationSummaryUIState by vm.conversationSummaryUIState.collectAsState()
    
    // Smart replies state flows
    val smartRepliesUIState by vm.smartRepliesUIState.collectAsState()
    
    // Track whether conversation starter should be shown
    // It should be hidden when removeConversationStarter event is emitted
    var showConversationStarter by remember { mutableStateOf(true) }
    
    // Track whether conversation summary should be shown
    // It should be hidden when removeConversationSummary event is emitted
    var showConversationSummary by remember { mutableStateOf(true) }
    
    // Highlight state for jump-to-parent-message feature
    // When a user clicks on a reply preview, we scroll to and highlight the parent message
    var highlightedMessageId by remember { mutableStateOf(-1L) }
    var highlightAlpha by remember { mutableStateOf(0f) }

    // Derive unread anchor ID for separator rendering
    val unreadAnchorId = unreadMessageAnchor?.id

    // Force recomposition when a message is updated in-place (e.g., reply count change).
    // StateFlow conflation suppresses emissions for same-reference mutations, so this
    // SharedFlow-based counter ensures the LazyColumn re-reads the mutated message.
    var messageUpdateTick by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        vm.messageUpdated.collect {
            messageUpdateTick++
        }
    }
    
    // Messages for display with reverseLayout=false
    // ViewModel stores: [oldest, ..., newest] (index 0 = oldest)
    // With reverseLayout=false, index 0 appears at TOP
    // So we keep the order as-is: [oldest, ..., newest] so oldest appears at top, newest at bottom
    // Filter out duplicates by message ID to prevent LazyColumn key conflicts
    val messages = remember(messagesFromVm, messageUpdateTick) { 
        messagesFromVm.distinctBy { it.id }
    }
    
    // LazyListState for scroll control
    val listState = rememberLazyListState()
    
    // Track if we've done the initial scroll to unread anchor
    // When startFromUnreadMessages is enabled, we hide the list until scroll is complete
    // to prevent the visible "jump" from position 0 to the target position
    var hasCompletedInitialScroll by remember { mutableStateOf(!startFromUnreadMessages) }
    
    // Track if we've done the initial scroll to bottom for the default case
    // Hide the list until scroll completes to prevent visible "jump" from top to bottom
    var hasCompletedDefaultInitialScroll by remember { mutableStateOf(false) }

    // Calculate target index for initial scroll
    val targetUnreadIndex = remember(messages, unreadAnchorId) {
        if (unreadAnchorId != null && messages.isNotEmpty()) {
            messages.indexOfFirst { it.id == unreadAnchorId }.takeIf { it >= 0 }
        } else {
            null
        }
    }
    
    // Determine if the unread anchor scroll will handle positioning
    val unreadAnchorWillHandle = startFromUnreadMessages && targetUnreadIndex != null

    // Determine if we should show the list content
    // Hide until initial scroll is complete to avoid visible jump
    // For unread anchor case: hide until hasCompletedInitialScroll
    // For default scroll-to-bottom case: hide until hasCompletedDefaultInitialScroll
    val shouldShowListContent = when {
        // Not loaded yet — show whatever we have (loading state handles visibility)
        uiState !is MessageListUIState.Loaded || messages.isEmpty() -> true
        // Unread anchor case: wait for unread anchor scroll to complete
        unreadAnchorWillHandle -> hasCompletedInitialScroll
        // GoToMessage case: no need to hide
        scrollToMessageId != null -> true
        // Default scroll-to-bottom case: wait for scroll to complete
        else -> hasCompletedDefaultInitialScroll
    }
    
    // Track if user is at bottom (last items visible due to reverseLayout=false)
    // With reverseLayout=false, user is at bottom when viewing the last items (highest indices)
    val isAtBottom by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            if (totalItems == 0) {
                true // Empty list is considered "at bottom"
            } else {
                val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()
                lastVisibleItem != null && lastVisibleItem.index >= totalItems - 1
            }
        }
    }
    
    // Track new message count when scrolled up
    var newMessageCount by remember { mutableIntStateOf(0) }
    
    // Track if we need to scroll to bottom after a refresh (scroll-to-bottom button click)
    var pendingScrollToBottom by remember { mutableStateOf(false) }
    
    // Get logged in user
    val loggedInUser = remember { CometChat.getLoggedInUser() }
    
    // Derive conversation type for avatar visibility logic
    // Edge cases:
    // - Both user and group null → isGroupConversation = false
    // - Both user and group provided → group takes precedence (matches XML behavior)
    val isGroupConversation = group != null
    
    /**
     * Handles click on message preview (quoted message) to jump to parent message.
     *
     * If the quoted message is in the current list, scrolls to it and highlights.
     * If not in the list, calls ViewModel to fetch and navigate to it.
     */
    val handleMessagePreviewClick: (BaseMessage) -> Unit = remember(vm, messages) {
        { quotedMessage: BaseMessage ->
            val existingMessage = messages.find { it.id == quotedMessage.id }

            if (existingMessage != null) {
                // Message is in current list - scroll to it directly with highlight
                val index = messages.indexOfFirst { it.id == quotedMessage.id }
                if (index >= 0) {
                    scope.launch {
                        val viewportHeight = listState.layoutInfo.viewportSize.height
                        val centerOffset = if (viewportHeight > 0) -(viewportHeight / 2) else 0
                        listState.scrollToItem(index, centerOffset)
                        // Trigger highlight animation - start at 1.0 alpha (matching Java implementation)
                        highlightedMessageId = quotedMessage.id
                        highlightAlpha = 1.0f
                    }
                }
            } else {
                // Message not in list - fetch it via goToMessage
                vm.goToMessage(quotedMessage.id, highlight = true)
            }
        }
    }

    // ========================================
    // Effects (Task 40)
    // ========================================

    // User/Group initialization effect
    LaunchedEffect(user, group) {
        vm.setDisableReceipt(disableReceipt)
        vm.setHideDeleteMessage(false)
        vm.setStartFromUnreadMessages(startFromUnreadMessages)
        vm.setUnreadThreshold(unreadMessageThreshold)
        vm.setDisableSoundForMessages(disableSoundForMessages)
        
        // AI feature controls
        vm.setEnableConversationStarter(enableConversationStarter)
        vm.setEnableSmartReplies(enableSmartReplies)
        vm.setEnableConversationSummary(enableConversationSummary)
        
        when {
            user != null -> {
                vm.setUser(
                    user = user,
                    parentMessageId = parentMessageId,
                    messagesRequestBuilder = messagesRequestBuilder
                )
            }
            group != null -> {
                vm.setGroup(
                    group = group,
                    parentMessageId = parentMessageId,
                    messagesRequestBuilder = messagesRequestBuilder
                )
            }
        }
        
        if (autoFetch && (user != null || group != null)) {
            if (startFromUnreadMessages) {
                vm.fetchMessagesWithUnreadCount()
            } else {
                vm.fetchMessages()
            }
        }
    }
    
    // Scroll to message effect
    // Must depend on BOTH scrollToMessageId AND messages because:
    // 1. scrollToMessageId is set by goToMessage() BEFORE messages are populated
    // 2. When messages arrive, we need to re-check if we can scroll to the target
    // Without messages dependency, the effect runs when scrollToMessageId is set but
    // messages is still empty, so indexOfFirst returns -1 and scroll never happens.
    //
    // Centering logic:
    // With reverseLayout=false, the list renders from top to bottom.
    // To center an item, we calculate the offset to position it in the middle of the viewport.
    LaunchedEffect(scrollToMessageId, messages, highlightScroll) {
        scrollToMessageId?.let { messageId ->
            val index = messages.indexOfFirst { it.id == messageId }
            if (index >= 0) {
                // Get viewport height to calculate center offset
                val viewportHeight = listState.layoutInfo.viewportSize.height
                if (viewportHeight > 0) {
                    // Calculate offset to center the item
                    // With reverseLayout=false, a negative offset moves the item UP from its position
                    // We want to position it roughly in the middle of the viewport
                    val centerOffset = -(viewportHeight / 2)
                    
                    // Use scrollToItem with offset to center the message
                    listState.scrollToItem(index, centerOffset)
                } else {
                    // Fallback: if viewport not measured yet, just scroll to item
                    listState.scrollToItem(index)
                }
                vm.clearScrollToMessage()

                // Trigger highlight animation if requested - start at 1.0 alpha (matching Java)
                if (highlightScroll) {
                    highlightedMessageId = messageId
                    highlightAlpha = 1.0f
                    vm.clearHighlightScroll()
                }
            }
        }
    }

    // Highlight fade-out animation effect
    // When a message is highlighted, animate the alpha from 1.0 to 0 over 2 seconds
    LaunchedEffect(highlightedMessageId) {
        if (highlightedMessageId > 0) {
            // Wait a brief moment before starting fade-out
            kotlinx.coroutines.delay(100)
            // Animate alpha from current value to 0 over 2 seconds
            val startAlpha = highlightAlpha
            val duration = 2000L
            val startTime = System.currentTimeMillis()
            while (highlightAlpha > 0f) {
                val elapsed = System.currentTimeMillis() - startTime
                val progress = (elapsed.toFloat() / duration).coerceIn(0f, 1f)
                highlightAlpha = startAlpha * (1f - progress)
                if (progress >= 1f) break
                kotlinx.coroutines.delay(16) // ~60fps
            }
            // Clear highlight after animation
            highlightedMessageId = -1L
            highlightAlpha = 0f
        }
    }

    // Initial scroll to unread anchor effect
    // When startFromUnreadMessages is enabled and we have an unread anchor,
    // scroll to position the first unread message (with separator) in the middle of the screen.
    // This runs BEFORE the first visible frame to avoid the "jump" effect.
    LaunchedEffect(targetUnreadIndex, startFromUnreadMessages) {
        if (startFromUnreadMessages && targetUnreadIndex != null && !hasCompletedInitialScroll) {
            // Wait for layout to be measured
            snapshotFlow { listState.layoutInfo.viewportSize.height }
                .first { it > 0 }
            
            val viewportHeight = listState.layoutInfo.viewportSize.height
            val centerOffset = -(viewportHeight / 2)
            listState.scrollToItem(targetUnreadIndex, centerOffset)
            hasCompletedInitialScroll = true
        }
    }
    
    // Pagination effect - Bidirectional pagination support
    // With reverseLayout=false and messages list in [oldest, ..., newest] order:
    // - Index 0 = oldest message (at top visually)
    // - Last index = newest message (at bottom visually)
    // - Scrolling UP decreases firstVisibleIndex toward 0
    // - Scrolling DOWN increases lastVisibleIndex toward the end
    // - Fetch older messages when near the top (start of list)
    // - Fetch newer messages when near the bottom (end of list)
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo }
            .map { layoutInfo ->
                val totalItems = layoutInfo.totalItemsCount
                val lastVisibleIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                val firstVisibleIndex = layoutInfo.visibleItemsInfo.firstOrNull()?.index ?: 0
                Triple(totalItems, lastVisibleIndex, firstVisibleIndex)
            }
            .distinctUntilChanged()
            .debounce(100)
            .collect { (totalItems, lastVisibleIndex, firstVisibleIndex) ->
                if (!isInProgress && totalItems > 0) {
                    // Fetch older messages when scrolled near the top
                    // With reverseLayout=false: firstVisibleIndex approaches 0 when scrolling up
                    if (firstVisibleIndex <= 5 && hasMorePreviousMessages) {
                        vm.fetchMessages()
                    }
                    // Fetch newer messages when scrolled near the bottom
                    // With reverseLayout=false: lastVisibleIndex approaches totalItems-1 when scrolling down
                    else if (lastVisibleIndex >= totalItems - 5 && hasMoreNewMessages) {
                        vm.fetchNextMessages()
                    }
                }
            }
    }
    
    // Track the ID of the last message to detect real-time new messages (not pagination)
    var lastKnownMessageId by remember { mutableStateOf(0L) }
    
    // Track the previous isInProgress state to detect when pagination completes
    var wasInProgress by remember { mutableStateOf(false) }
    
    // New message tracking effect
    // Only increment when NEW messages arrive via real-time listeners, NOT via fetchNextMessages() pagination
    // The ViewModel stores messages as [oldest, ..., newest], so new messages are appended at the end
    // 
    // Key distinction:
    // - fetchNextMessages() pagination: isInProgress transitions from true -> false when messages are added
    // - Real-time listener messages: isInProgress remains false when messages are added
    LaunchedEffect(messagesFromVm, isInProgress) {
        val currentLastMessageId = messagesFromVm.lastOrNull()?.id ?: 0L
        
        // Detect if this update came from pagination completing (isInProgress was true, now false)
        val isPaginationComplete = wasInProgress && !isInProgress
        
        // If the last message ID changed and is greater than what we knew before,
        // AND this is NOT from pagination completing, it means real-time messages arrived
        if (currentLastMessageId > lastKnownMessageId && lastKnownMessageId > 0L && !isAtBottom && !isPaginationComplete) {
            // Real-time new message arrived while user is scrolled up
            // Count how many new messages were added at the end
            val newMessagesCount = messagesFromVm.count { it.id > lastKnownMessageId }
            newMessageCount += newMessagesCount
        }
        
        // Reset count and mark conversation as read when user scrolls to bottom
        if (isAtBottom) {
            newMessageCount = 0
            vm.markConversationRead()
        }
        
        // Update the last known message ID
        if (currentLastMessageId > 0L) {
            lastKnownMessageId = currentLastMessageId
        }
        
        // Track the previous isInProgress state for next iteration
        wasInProgress = isInProgress
    }
    
    // Sync newMessageCount with ViewModel's unreadCount
    // When markConversationRead() succeeds, unreadCount becomes 0, so reset local count too
    LaunchedEffect(unreadCount) {
        if (unreadCount == 0) {
            newMessageCount = 0
        }
    }
    
    // Initial scroll to bottom for default case
    // Scrolls to bottom when no goToMessage target and unread anchor won't handle it
    if (!hasCompletedDefaultInitialScroll
        && uiState is MessageListUIState.Loaded
        && messages.isNotEmpty()
        && scrollToMessageId == null
        && !unreadAnchorWillHandle
    ) {
        LaunchedEffect(Unit) {
            // Wait for the LazyColumn to have items visible on screen
            snapshotFlow { listState.layoutInfo.visibleItemsInfo.size }
                .first { it > 0 }
            listState.scrollToItem(messages.lastIndex)
            hasCompletedDefaultInitialScroll = true
        }
    }

    // Auto-scroll effect
    LaunchedEffect(messages.size) {
        if (scrollToBottomOnNewMessage && isAtBottom && messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }
    
    // Scroll to bottom after refresh (triggered by scroll-to-bottom button click)
    // This effect watches for the pendingScrollToBottom flag and scrolls when messages are loaded
    LaunchedEffect(pendingScrollToBottom, messages.size, uiState) {
        if (pendingScrollToBottom && uiState is MessageListUIState.Loaded && messages.isNotEmpty()) {
            listState.scrollToItem(messages.lastIndex)
            pendingScrollToBottom = false
        }
    }
    
    // Observe removeConversationStarter event to hide conversation starter view
    // This is emitted when a message is added to the conversation
    LaunchedEffect(Unit) {
        vm.removeConversationStarter.collect {
            showConversationStarter = false
        }
    }
    
    // Reset showConversationStarter when conversation starter is re-enabled or user/group changes
    LaunchedEffect(enableConversationStarter, user, group) {
        if (enableConversationStarter) {
            showConversationStarter = true
        }
    }
    
    // Observe removeConversationSummary event to hide conversation summary view
    // This is emitted when the user dismisses the summary
    LaunchedEffect(Unit) {
        vm.removeConversationSummary.collect {
            showConversationSummary = false
        }
    }
    
    // Reset showConversationSummary when conversation summary is re-enabled or user/group changes
    LaunchedEffect(enableConversationSummary, user, group) {
        if (enableConversationSummary) {
            showConversationSummary = true
        }
    }
    
    // Observe scrollToBottomEvent to scroll to newest messages
    // This is emitted after initial message load and when new messages arrive
    // With reverseLayout=false, newest messages are at the end (highest index)
    LaunchedEffect(Unit) {
        vm.scrollToBottomEvent.collect {
            if (messages.isNotEmpty()) {
                listState.animateScrollToItem(messages.lastIndex.coerceAtLeast(0))
            }
        }
    }
    
    // Get receiver ID for ComposeMessage event
    val receiverId = remember(user, group) {
        user?.uid ?: group?.guid ?: ""
    }

    // Observe delete confirmation requests from ViewModel
    // When the user selects "Delete", the ViewModel emits a request via SharedFlow.
    // The composable shows a confirmation dialog before proceeding with deletion.
    LaunchedEffect(Unit) {
        vm.deleteConfirmationRequest.collect { message ->
            deleteConfirmationMessage = message
        }
    }
    
    // Observe message sender fetched from ViewModel
    // When the user selects "Message Privately", the ViewModel fetches the sender
    // and emits the User via SharedFlow. The composable invokes the callback.
    LaunchedEffect(Unit) {
        vm.messageSenderFetched.collect { fetchedUser ->
            onMessagePrivately?.invoke(fetchedUser)
        }
    }
    
    // Observe flag state from ViewModel
    // When the user reports a message, the ViewModel updates the flag state.
    val flagState by vm.flagState.collectAsState()
    LaunchedEffect(flagState) {
        when (flagState) {
            is MessageFlagState.Success -> {
                showFlagDialog = false
                flagDialogMessage = null
                showFlagError = false
                showFlagProgress = false
                vm.resetFlagState()
            }
            is MessageFlagState.Error -> {
                showFlagError = true
                showFlagProgress = false
                vm.resetFlagState()
            }
            is MessageFlagState.InProgress -> {
                // Progress is shown by the dialog
            }
            is MessageFlagState.Idle -> {
                // No action needed
            }
        }
    }

    // Observe message translation completions from ViewModel
    // When the ViewModel successfully translates a message, it emits the updated message.
    // The message list already observes vm.messages, so the translated message is
    // automatically reflected in the list via the ViewModel's updateMessage() call.
    // No additional handling needed here for Compose since the list recomposes on state change.

    // ========================================
    // UI Rendering (Task 41)
    // ========================================
    
    val shape = RoundedCornerShape(style.cornerRadius)
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(color = style.backgroundColor, shape = shape)
            .then(
                if (style.strokeWidth > 0.dp) {
                    Modifier.border(style.strokeWidth, style.strokeColor, shape)
                } else {
                    Modifier
                }
            )
            .semantics { contentDescription = "Message list" }
    ) {
        when (uiState) {
            // Loading state
            is MessageListUIState.Loading -> {
                if (!hideLoadingState) {
                    loadingView?.invoke() ?: DefaultLoadingView()
                }
            }
            
            // Empty state
            is MessageListUIState.Empty -> {
                // Show conversation starter if enabled and should be shown
                val shouldShowConversationStarter = enableConversationStarter && 
                    showConversationStarter && 
                    conversationStarterUIState !is ConversationStarterUIState.Idle
                
                if (shouldShowConversationStarter) {
                    // Handle conversation starter click
                    val handleConversationStarterClick: (String, Int) -> Unit = { starter, position ->
                        if (onConversationStarterClick != null) {
                            // Use custom callback if provided
                            onConversationStarterClick.invoke(starter, position)
                        } else {
                            // Default behavior: emit ComposeMessage event
                            CometChatEvents.emitUIEvent(
                                CometChatUIEvent.ComposeMessage(
                                    id = receiverId,
                                    text = starter
                                )
                            )
                        }
                        // Clear conversation starter after selection
                        vm.clearConversationStarter()
                    }
                    
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (conversationStarterView != null) {
                            // Use custom conversation starter view
                            conversationStarterView.invoke(
                                conversationStarterUIState,
                                handleConversationStarterClick
                            )
                        } else {
                            // Use default conversation starter view
                            CometChatAIConversationStarterView(
                                uiState = conversationStarterUIState,
                                style = conversationStarterStyle,
                                onClick = handleConversationStarterClick
                            )
                        }
                    }
                } else if (!hideEmptyState) {
                    emptyView?.invoke() ?: DefaultEmptyView(
                        style = style,
                        user = user,
                        group = group
                    )
                }
                onEmpty?.invoke()
            }
            
            // Error state
            is MessageListUIState.Error -> {
                val error = (uiState as MessageListUIState.Error).exception
                if (!hideErrorState) {
                    errorView?.invoke { vm.fetchMessages() } ?: DefaultErrorView(
                        style = style,
                        onRetry = { vm.fetchMessages() }
                    )
                }
                onError?.invoke(
                    error as? CometChatException
                        ?: CometChatException("Unknown", error.message ?: "Unknown error")
                )
            }
            
            // Loaded state
            is MessageListUIState.Loaded -> {
                onLoad?.invoke(messages)
                
                // Track the topmost visible message for sticky date header
                // With reverseLayout=false and messages list in [oldest, ..., newest] order:
                // - visibleItemsInfo is ordered from top to bottom (index 0 = top, last = bottom)
                // - firstVisibleIndex points to the topmost visible item
                // - We want the timestamp of that message for the sticky header
                //
                // Key insight for reverseLayout=false:
                // - firstVisibleItemIndex is the TOP of the visible area (oldest visible messages)
                // - visibleItemsInfo.firstOrNull()?.index is the TOP of the visible area
                val topmostVisibleMessageTimestamp by remember(messages) {
                    derivedStateOf {
                        val firstVisibleIndex = listState.layoutInfo.visibleItemsInfo.firstOrNull()?.index
                        if (firstVisibleIndex != null && firstVisibleIndex < messages.size) {
                            messages[firstVisibleIndex].sentAt
                        } else if (messages.isNotEmpty()) {
                            messages.first().sentAt // oldest message
                        } else {
                            0L
                        }
                    }
                }
                
                // Calculate the date ID for the sticky header
                // This is used to coordinate with date separators to prevent duplicate dates
                // When the sticky header shows a date, we hide the inline date separator for that date
                val stickyHeaderDate = remember(topmostVisibleMessageTimestamp) {
                    if (topmostVisibleMessageTimestamp > 0L) {
                        getDateId(topmostVisibleMessageTimestamp)
                    } else null
                }
                
                Box(modifier = Modifier.fillMaxSize()) {
                    // Track whether the list content fills the viewport.
                    // Used to coordinate sticky header visibility and inline date separator suppression.
                    val isListScrollable by remember {
                        derivedStateOf {
                            listState.canScrollForward || listState.canScrollBackward
                        }
                    }

                    // Message list
                    // Use alpha to hide list during initial scroll to prevent visible "jump"
                    LazyColumn(
                        state = listState,
                        reverseLayout = false,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer { alpha = if (shouldShowListContent) 1f else 0f }
                    ) {
                        // Message items
                        // Note: messages list is in chronological order [oldest, ..., newest]:
                        // - index 0 = oldest message (at top visually)
                        // - last index = newest message (at bottom visually)
                        // - index-1 = older message (above current in visual order)
                        itemsIndexed(
                            items = messages,
                            key = { _, message -> message.id }
                        ) { index, message ->
                            // With chronological list, index-1 is the OLDER message (visually above)
                            val previousMessage = messages.getOrNull(index - 1)
                            
                            // Determine if this message needs a date separator
                            val needsDateSeparator = !hideDateSeparator &&
                                    shouldShowDateSeparator(message, previousMessage)
                            
                            // Determine if the separator should be visually hidden (but still occupy space)
                            // to avoid layout oscillation when the sticky header shows the same date.
                            // Using alpha instead of conditional composition prevents the feedback loop
                            // where hiding the separator shifts layout → changes first visible item →
                            // changes sticky header date → shows separator again → repeat (vibration).
                            val isSeparatorHiddenByStickyHeader = needsDateSeparator &&
                                    isListScrollable && isDateSeparatorHiddenByStickyHeader(
                                        messageIndex = index,
                                        messageDate = getDateId(message.sentAt),
                                        stickyHeaderDate = stickyHeaderDate,
                                        firstVisibleIndex = listState.layoutInfo.visibleItemsInfo.firstOrNull()?.index
                                    )
                            
                            // Compute bubble alignment for this message
                            val alignment = remember(message.id, message.sender?.uid, loggedInUser?.uid) {
                                getMessageAlignment(message, loggedInUser)
                            }
                            // When messageAlignment is LEFT_ALIGNED, override alignment to LEFT
                            // regardless of sender (except for CENTER-aligned action messages)
                            val bubbleAlignment = remember(alignment, messageAlignment) {
                                if (messageAlignment == UIKitConstants.MessageListAlignment.LEFT_ALIGNED) {
                                    // LEFT_ALIGNED mode: all messages align LEFT
                                    UIKitConstants.MessageBubbleAlignment.LEFT
                                } else {
                                    // STANDARD mode: use normal alignment based on sender
                                    when (alignment) {
                                        MessageAlignment.LEFT -> UIKitConstants.MessageBubbleAlignment.LEFT
                                        MessageAlignment.RIGHT -> UIKitConstants.MessageBubbleAlignment.RIGHT
                                        MessageAlignment.CENTER -> UIKitConstants.MessageBubbleAlignment.CENTER
                                    }
                                }
                            }
                            
                            // Check if this message is the first unread message (unread anchor)
                            val isUnreadAnchor = unreadAnchorId != null && message.id == unreadAnchorId
                            
                            Column {
                                // Date separator (shown above message in visual order)
                                // Always compose when needed to maintain stable layout;
                                // use alpha=0 to visually hide when the sticky header covers it.
                                if (needsDateSeparator) {
                                    DateSeparator(
                                        timestamp = message.sentAt,
                                        style = style,
                                        modifier = Modifier.graphicsLayer {
                                            alpha = if (isSeparatorHiddenByStickyHeader) 0f else 1f
                                        }
                                    )
                                }
                                
                                // New messages separator (shown above the first unread message)
                                // Rendered after date separator so it appears below the date
                                if (isUnreadAnchor && !hideNewMessagesSeparator) {
                                    newMessagesSeparatorView?.invoke() ?: NewMessagesSeparator(
                                        style = style
                                    )
                                }

                                // Calculate highlight color for this message
                                // Applied at MessageBubbleWrapper level to cover full row width
                                val messageHighlightColor = if (message.id == highlightedMessageId && highlightAlpha > 0f) {
                                    CometChatTheme.colorScheme.extendedPrimaryColor800.copy(alpha = highlightAlpha)
                                } else {
                                    Color.Transparent
                                }

                                // SwipeToReplyWrapper wraps the full row so the reply icon
                                // is positioned relative to the row edge (matching Kotlin XML)
                                SwipeToReplyWrapper(
                                    message = message,
                                    enabled = swipeToReplyEnabled,
                                    onReply = { msg -> vm.onMessageReply(msg) }
                                ) {
                                    // MessageBubbleWrapper applies row-level spacing inside
                                    MessageBubbleWrapper(
                                        alignment = bubbleAlignment,
                                        highlightColor = messageHighlightColor
                                    ) {
                                        MessageListItem(
                                            message = message,
                                            loggedInUser = loggedInUser,
                                            style = style,
                                            bubbleFactories = factoryMap,
                                            hideAvatar = hideAvatar,
                                            isGroupConversation = isGroupConversation,
                                            hideReceipts = hideReceipts,
                                            hideGroupActionMessages = hideGroupActionMessages,
                                            hideModerationView = hideModerationView,
                                            timeStampAlignment = timeStampAlignment,
                                            messageListAlignment = messageAlignment,
                                            incomingMessageBubbleStyle = style.incomingMessageBubbleStyle,
                                            outgoingMessageBubbleStyle = style.outgoingMessageBubbleStyle,
                                            leadingView = leadingView,
                                            headerView = headerView,
                                            replyView = replyView,
                                            contentView = contentView,
                                            bottomView = bottomView,
                                            statusInfoView = statusInfoView,
                                            threadView = threadView,
                                            footerView = footerView,
                                            onMessageClick = onMessageClick,
                                            onMessageLongClick = handleMessageLongClick,
                                            onThreadRepliesClick = onThreadRepliesClick,
                                            onReactionClick = onReactionClick ?: { msg, emoji ->
                                                // Default toggle: remove if already reacted, add otherwise
                                                val alreadyReacted = msg.reactions?.any {
                                                    it.reaction == emoji && it.reactedByMe
                                                } == true
                                                if (alreadyReacted) {
                                                    vm.removeReaction(msg, emoji)
                                                } else {
                                                    vm.addReaction(msg, emoji)
                                                }
                                            },
                                            onReactionLongClick = onReactionLongClick ?: { msg, emoji ->
                                                reactionListMessage = msg
                                                reactionListSelectedEmoji = emoji
                                                showReactionListSheet = true
                                            },
                                            onAddMoreReactionsClick = onAddMoreReactionsClick ?: { msg ->
                                                emojiKeyboardMessage = msg
                                                showEmojiKeyboardSheet = true
                                            },
                                            onMessagePreviewClick = handleMessagePreviewClick,
                                            // Pass -1 for highlightedMessageId since highlight is now at wrapper level
                                            highlightedMessageId = -1L,
                                            highlightAlpha = 0f,
                                            textFormatters = effectiveTextFormatters
                                        )
                                    }
                                }
                            }
                        }

                        // Loading indicator at the start (top visually with reverseLayout=false)
                        // This shows when loading older messages via pagination
                        if (isInProgress) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        color = CometChatTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                    
                    // Sticky date header overlay (shown at top of list when scrollable)
                    // Only show when:
                    // 1. hideStickyDate is false
                    // 2. There are messages with valid timestamps
                    // 3. Content fills the viewport (canScrollForward or canScrollBackward)
                    //    When content doesn't fill the viewport (few messages with Arrangement.Bottom),
                    //    inline date separators are already visible, so the sticky header is hidden
                    //    to avoid overlapping the first message bubble.
                    if (!hideStickyDate && messages.isNotEmpty() && topmostVisibleMessageTimestamp > 0L && isListScrollable) {
                        StickyDateHeader(
                            timestamp = topmostVisibleMessageTimestamp,
                            style = style,
                            modifier = Modifier.align(Alignment.TopCenter)
                        )
                    }
                    
                    // Conversation summary view (shown at top of list when enabled)
                    // Displayed when there are many unread messages (above threshold)
                    val shouldShowConversationSummary = enableConversationSummary && 
                        showConversationSummary && 
                        conversationSummaryUIState !is ConversationSummaryUIState.Idle
                    
                    if (shouldShowConversationSummary) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            if (conversationSummaryView != null) {
                                // Use custom conversation summary view
                                conversationSummaryView.invoke(
                                    conversationSummaryUIState,
                                    { vm.dismissConversationSummary() }
                                )
                            } else {
                                // Use default conversation summary view
                                CometChatAIConversationSummaryView(
                                    uiState = conversationSummaryUIState,
                                    style = conversationSummaryStyle,
                                    onCloseClick = { vm.dismissConversationSummary() }
                                )
                            }
                        }
                    }
                    
                    // Smart replies view (shown at bottom of list when enabled)
                    // Displayed when smart replies are available after receiving a message
                    val shouldShowSmartReplies = enableSmartReplies && 
                        smartRepliesUIState !is SmartRepliesUIState.Idle
                    
                    if (shouldShowSmartReplies) {
                        // Handle smart reply click
                        val handleSmartReplyClick: (String, Int) -> Unit = { reply, position ->
                            if (onSmartReplyClick != null) {
                                // Use custom callback if provided
                                onSmartReplyClick.invoke(reply, position)
                            } else {
                                // Default behavior: emit ComposeMessage event
                                CometChatEvents.emitUIEvent(
                                    CometChatUIEvent.ComposeMessage(
                                        id = receiverId,
                                        text = reply
                                    )
                                )
                            }
                            // Clear smart replies after selection
                            vm.clearSmartReplies()
                        }
                        
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 8.dp)
                        ) {
                            if (smartRepliesView != null) {
                                // Use custom smart replies view
                                smartRepliesView.invoke(
                                    smartRepliesUIState,
                                    handleSmartReplyClick,
                                    { vm.clearSmartReplies() }
                                )
                            } else {
                                // Use default smart replies view
                                CometChatAISmartRepliesView(
                                    uiState = smartRepliesUIState,
                                    style = smartRepliesStyle,
                                    onClick = handleSmartReplyClick,
                                    onCloseClick = { vm.clearSmartReplies() }
                                )
                            }
                        }
                    }
                    
                    // New message indicator / Scroll-to-bottom button
                    // Show whenever user is NOT at bottom, regardless of new message count
                    // This matches the XML implementation behavior
                    if (!isAtBottom) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(end = 8.dp, bottom = 8.dp)
                        ) {
                            newMessageIndicatorView?.invoke(newMessageCount) {
                                // Reset and fetch behavior (matches XML implementation)
                                newMessageCount = 0
                                pendingScrollToBottom = true
                                vm.clear()
                                vm.fetchMessages()
                            } ?: DefaultNewMessageIndicator(
                                count = newMessageCount,
                                style = style,
                                onClick = {
                                    // Reset and fetch behavior (matches XML implementation)
                                    // 1. Reset new message count
                                    // 2. Set pending scroll flag to scroll after messages load
                                    // 3. Clear message list and reset request
                                    // 4. Fetch fresh messages from the latest
                                    newMessageCount = 0
                                    pendingScrollToBottom = true
                                    vm.clear()
                                    vm.fetchMessages()
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Delete confirmation dialog
    // Shown when the ViewModel emits a delete confirmation request via SharedFlow
    deleteConfirmationMessage?.let { message ->
        CometChatConfirmDialog(
            title = stringResource(R.string.cometchat_delete_message_title),
            subtitle = stringResource(R.string.cometchat_delete_message_subtitle),
            positiveButtonText = stringResource(R.string.cometchat_delete),
            negativeButtonText = stringResource(R.string.cometchat_cancel),
            icon = painterResource(R.drawable.cometchat_ic_delete),
            style = style.deleteDialogStyle ?: CometChatConfirmDialogStyle.deleteConfirmation(
                icon = painterResource(R.drawable.cometchat_ic_delete)
            ),
            dismissOnClickOutside = false,
            onPositiveClick = {
                vm.deleteMessage(message)
                deleteConfirmationMessage = null
            },
            onNegativeClick = {
                deleteConfirmationMessage = null
            },
            onDismiss = {
                deleteConfirmationMessage = null
            }
        )
    }
    
    // Flag message dialog
    // Shown when the user selects the REPORT option
    if (showFlagDialog && flagDialogMessage != null) {
        CometChatFlagMessageDialog(
            flagReasons = flagReasons,
            showError = showFlagError,
            showProgress = showFlagProgress,
            dismissOnClickOutside = false,
            onReportClick = { flagDetail ->
                showFlagProgress = true
                showFlagError = false
                flagDialogMessage?.let { message ->
                    vm.flagMessage(message, flagDetail.reasonId ?: "", flagDetail.remark ?: "")
                }
            },
            onCancelClick = {
                showFlagDialog = false
                flagDialogMessage = null
                showFlagError = false
                showFlagProgress = false
            },
            onCloseClick = {
                showFlagDialog = false
                flagDialogMessage = null
                showFlagError = false
                showFlagProgress = false
            },
            onDismiss = {
                showFlagDialog = false
                flagDialogMessage = null
                showFlagError = false
                showFlagProgress = false
            }
        )
    }

    // Message popup menu overlay
    // Shown when a message is long-pressed with computed options
    if (showPopupMenu && longPressedMessage != null) {
        val popupMessage = longPressedMessage!!

        // Determine quick reactions visibility based on message properties
        val isInteractive = popupMessage.category == UIKitConstants.MessageCategory.INTERACTIVE
        val isDisapproved = when (popupMessage) {
            is TextMessage -> popupMessage.moderationStatus?.name == "DISAPPROVED"
            is MediaMessage -> popupMessage.moderationStatus?.name == "DISAPPROVED"
            else -> false
        }
        val showReactions = !isInteractive
            && !hideMessageReactionOption
            && !isDisapproved

        // Compute bubble alignment for the preview
        val previewAlignment = getMessageAlignment(popupMessage)
        val bubbleAlignment = if (messageAlignment == UIKitConstants.MessageListAlignment.LEFT_ALIGNED) {
            UIKitConstants.MessageBubbleAlignment.LEFT
        } else {
            when (previewAlignment) {
                MessageAlignment.LEFT -> UIKitConstants.MessageBubbleAlignment.LEFT
                MessageAlignment.RIGHT -> UIKitConstants.MessageBubbleAlignment.RIGHT
                MessageAlignment.CENTER -> UIKitConstants.MessageBubbleAlignment.CENTER
            }
        }

        CometChatMessagePopupMenu(
            message = popupMessage,
            menuItems = messageOptions,
            quickReactions = quickReactions,
            showQuickReactions = showReactions,
            messageAlignment = messageAlignment,
            onOptionClick = { option ->
                option.onClick?.invoke()
                handleMessageOptionSelected(option.id, popupMessage)
                showPopupMenu = false
            },
            onReactionClick = { emoji ->
                if (onQuickReactionClick != null) {
                    onQuickReactionClick.invoke(popupMessage, emoji)
                } else {
                    vm.addReaction(popupMessage, emoji)
                }
                showPopupMenu = false
                longPressedMessage = null
                messageOptions = emptyList()
            },
            onEmojiPickerClick = {
                onEmojiPickerClick?.invoke(popupMessage)
                showPopupMenu = false
                longPressedMessage = null
                messageOptions = emptyList()
            },
            onDismiss = {
                showPopupMenu = false
                longPressedMessage = null
                messageOptions = emptyList()
            },
            messageBubbleContent = {
                // Filter the appropriate factory from the map based on message type
                val popupFactory = remember(popupMessage.id, popupMessage.deletedAt, factoryMap) {
                    factoryMap[buildFactoryKey(popupMessage)]
                }
                
                CometChatMessageBubble(
                    message = popupMessage,
                    alignment = bubbleAlignment,
                    style = style.messageBubbleStyle,
                    factory = popupFactory,
                    shouldShowDefaultAvatar = false,
                    timeStampAlignment = timeStampAlignment,
                    textFormatters = effectiveTextFormatters,
                    textBubbleStyle = style.textBubbleStyle,
                    imageBubbleStyle = style.imageBubbleStyle,
                    videoBubbleStyle = style.videoBubbleStyle,
                    audioBubbleStyle = style.audioBubbleStyle,
                    fileBubbleStyle = style.fileBubbleStyle,
                    deleteBubbleStyle = style.deleteBubbleStyle,
                    incomingMessageBubbleStyle = style.incomingMessageBubbleStyle,
                    outgoingMessageBubbleStyle = style.outgoingMessageBubbleStyle
                )
            }
        )
    }

    // Dismiss popup menu when the displayed message is deleted
    LaunchedEffect(Unit) {
        vm.messageDeleted.collect { deletedMessage ->
            if (showPopupMenu && longPressedMessage != null
                && deletedMessage.id == longPressedMessage?.id
            ) {
                showPopupMenu = false
                longPressedMessage = null
                messageOptions = emptyList()
            }
        }
    }

    // Message Information bottom sheet
    if (showMessageInformation && messageInformationMessage != null) {
        CometChatMessageInformation(
            message = messageInformationMessage!!,
            style = style.messageInformationStyle ?: CometChatMessageInformationStyle.default(),
            bubbleFactories = bubbleFactories,
            onDismiss = {
                showMessageInformation = false
                messageInformationMessage = null
            },
            onError = onError
        )
    }

    // Reaction list bottom sheet
    // Shown when a reaction is long-clicked (default handler) — mirrors the Java UIKit behavior
    // Java UIKit opens the bottom sheet at 50% of screen height
    if (showReactionListSheet && reactionListMessage != null) {
        val reactionSheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = false
        )

        ModalBottomSheet(
            onDismissRequest = {
                showReactionListSheet = false
                reactionListMessage = null
                reactionListSelectedEmoji = null
            },
            sheetState = reactionSheetState,
            shape = RoundedCornerShape(
                topStart = reactionListStyle.cornerRadius,
                topEnd = reactionListStyle.cornerRadius,
                bottomStart = 0.dp,
                bottomEnd = 0.dp
            ),
            containerColor = reactionListStyle.backgroundColor,
            scrimColor = Color.Black.copy(alpha = 0.5f),
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .size(width = 32.dp, height = 4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(reactionListStyle.strokeColor)
                )
            }
        ) {
            CometChatReactionList(
                baseMessage = reactionListMessage!!,
                selectedReaction = reactionListSelectedEmoji,
                style = reactionListStyle,
                modifier = Modifier.fillMaxHeight(0.5f),
                onEmpty = {
                    showReactionListSheet = false
                    reactionListMessage = null
                    reactionListSelectedEmoji = null
                }
            )
        }
    }

    // Emoji keyboard bottom sheet
    // Shown when the "+" add-more-reactions button is clicked in the message bubble footer
    if (showEmojiKeyboardSheet && emojiKeyboardMessage != null) {
        val emojiSheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = false
        )

        ModalBottomSheet(
            onDismissRequest = {
                showEmojiKeyboardSheet = false
                emojiKeyboardMessage = null
            },
            sheetState = emojiSheetState,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            containerColor = CometChatTheme.colorScheme.backgroundColor1,
            scrimColor = Color.Black.copy(alpha = 0.5f),
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .size(width = 32.dp, height = 4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(CometChatTheme.colorScheme.strokeColorLight)
                )
            }
        ) {
            CometChatEmojiKeyboard(
                modifier = Modifier.fillMaxHeight(0.5f),
                onClick = { emoji ->
                    emojiKeyboardMessage?.let { msg ->
                        vm.addReaction(msg, emoji)
                    }
                    showEmojiKeyboardSheet = false
                    emojiKeyboardMessage = null
                }
            )
        }
    }
}

/**
 * Shares a message's content via Android's share intent.
 *
 * For text messages, shares the formatted text content (applying text formatters if available).
 * For image messages, downloads the image via Coil and shares it as a file.
 * For other media messages, downloads the file and shares it.
 *
 * @param context The Android context.
 * @param message The [BaseMessage] to share.
 * @param scope The coroutine scope for async operations.
 * @param textFormatters The list of text formatters to apply for text messages.
 */
private fun shareMessage(
    context: android.content.Context,
    message: BaseMessage,
    scope: kotlinx.coroutines.CoroutineScope,
    textFormatters: List<CometChatTextFormatter>? = null
) {
    when {
        message is TextMessage -> {
            // Share text message with formatted text (similar to copy)
            try {
                val rawText = message.text
                val shareText = if (!textFormatters.isNullOrEmpty()) {
                    FormatterUtils.getFormattedText(
                        context,
                        message,
                        UIKitConstants.FormattingType.MESSAGE_BUBBLE,
                        UIKitConstants.MessageBubbleAlignment.RIGHT,
                        rawText,
                        textFormatters
                    ).toString()
                } else {
                    rawText
                }
                if (shareText.isNotEmpty()) {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.cometchat_share))
                        putExtra(Intent.EXTRA_TEXT, shareText)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.cometchat_share)))
                }
            } catch (e: Exception) {
                android.util.Log.e("CometChatMessageList", "Share failed: ${e.message}")
            }
        }
        message is MediaMessage && 
            message.type == com.cometchat.chat.constants.CometChatConstants.MESSAGE_TYPE_IMAGE -> {
            // Share image message via Coil bitmap download
            val attachment = message.attachment ?: return
            val fileUrl = attachment.fileUrl ?: return
            val mediaName = attachment.fileName ?: "shared_image"
            val mimeType = attachment.fileMimeType ?: "image/*"
            
            scope.launch {
                try {
                    val imageLoader = coil.ImageLoader(context)
                    val request = coil.request.ImageRequest.Builder(context)
                        .data(fileUrl)
                        .allowHardware(false)
                        .build()
                    
                    val result = imageLoader.execute(request)
                    val bitmap = (result.drawable as? android.graphics.drawable.BitmapDrawable)?.bitmap
                    
                    if (bitmap != null) {
                        @Suppress("DEPRECATION")
                        val path = android.provider.MediaStore.Images.Media.insertImage(
                            context.contentResolver,
                            bitmap,
                            mediaName,
                            null
                        )
                        if (path != null) {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                putExtra(Intent.EXTRA_STREAM, android.net.Uri.parse(path))
                                type = mimeType
                            }
                            context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.cometchat_share)))
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("CometChatMessageList", "Failed to share image: ${e.message}")
                }
            }
        }
        message is MediaMessage -> {
            // Share other media types (video, audio, files) via file download
            val attachment = message.attachment ?: return
            val fileUrl = attachment.fileUrl ?: return
            val fileName = "${message.id}${attachment.fileName ?: "media_file"}"
            val mimeType = attachment.fileMimeType ?: "*/*"
            
            scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                try {
                    val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(
                        android.os.Environment.DIRECTORY_DOWNLOADS
                    )
                    val file = java.io.File(downloadsDir, fileName)
                    
                    if (!file.exists()) {
                        val url = java.net.URL(fileUrl)
                        val connection = url.openConnection() as java.net.HttpURLConnection
                        connection.connect()
                        
                        if (connection.responseCode != java.net.HttpURLConnection.HTTP_OK) {
                            throw Exception("HTTP error: ${connection.responseCode}")
                        }
                        
                        connection.inputStream.use { input ->
                            java.io.FileOutputStream(file).use { output ->
                                val buffer = ByteArray(4096)
                                var bytesRead: Int
                                while (input.read(buffer).also { bytesRead = it } != -1) {
                                    output.write(buffer, 0, bytesRead)
                                }
                                output.flush()
                            }
                        }
                    }
                    
                    // Share the file on the main thread
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        try {
                            val authority = "${context.packageName}.provider"
                            val fileUri = androidx.core.content.FileProvider.getUriForFile(context, authority, file)
                            
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                putExtra(Intent.EXTRA_STREAM, fileUri)
                                type = mimeType
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(
                                Intent.createChooser(shareIntent, context.getString(R.string.cometchat_share))
                            )
                        } catch (e: Exception) {
                            android.util.Log.e("CometChatMessageList", "Failed to share file: ${e.message}")
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("CometChatMessageList", "Failed to download and share media: ${e.message}")
                }
            }
        }
    }
}
