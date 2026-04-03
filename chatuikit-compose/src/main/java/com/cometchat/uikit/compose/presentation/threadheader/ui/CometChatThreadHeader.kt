package com.cometchat.uikit.compose.presentation.threadheader.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.models.BaseMessage
import com.cometchat.uikit.compose.R
import com.cometchat.uikit.compose.presentation.shared.formatters.CometChatMentionsFormatter
import com.cometchat.uikit.compose.presentation.shared.formatters.CometChatTextFormatter
import com.cometchat.uikit.compose.presentation.shared.messagebubble.BubbleFactory
import com.cometchat.uikit.compose.presentation.shared.messagebubble.toFactoryMap
import com.cometchat.uikit.compose.presentation.shared.messagebubble.ui.CometChatMessageBubble
import com.cometchat.uikit.compose.presentation.shared.messagebubble.ui.buildFactoryKey
import com.cometchat.uikit.compose.presentation.threadheader.style.CometChatThreadHeaderStyle
import com.cometchat.uikit.compose.presentation.threadheader.viewmodel.ThreadHeaderViewModel
import com.cometchat.uikit.core.constants.UIKitConstants
import java.text.SimpleDateFormat

/**
 * CometChatThreadHeader displays the parent message in a threaded conversation view.
 *
 * This composable provides context for users viewing and composing thread replies by
 * displaying the original message that started the thread, along with a reply count
 * indicator showing the number of replies in the thread.
 *
 * ## Features
 * - Displays parent message using CometChatMessageBubble
 * - Shows reply count with proper formatting ("1 Reply" / "X Replies")
 * - Real-time updates for message edits, reactions, and reply count
 * - Customizable visibility for reactions, avatar, receipts, and reply count
 * - Support for custom message bubble and reply count views
 * - BubbleFactory integration for custom message type rendering
 * - Style customization via [CometChatThreadHeaderStyle]
 *
 * ## Usage
 *
 * ```kotlin
 * CometChatThreadHeader(
 *     parentMessage = message,
 *     style = CometChatThreadHeaderStyle.default(),
 *     hideReactions = false,
 *     maxHeight = 300.dp
 * )
 * ```
 *
 * ## Custom Views
 *
 * ```kotlin
 * CometChatThreadHeader(
 *     parentMessage = message,
 *     messageBubbleView = { msg ->
 *         // Custom message bubble rendering
 *         MyCustomMessageBubble(message = msg)
 *     },
 *     replyCountView = { count ->
 *         // Custom reply count rendering
 *         Text("$count responses")
 *     }
 * )
 * ```
 *
 * ## BubbleFactory Integration
 *
 * ```kotlin
 * val customFactories = listOf(
 *     MyCustomBubbleFactory()
 * )
 * CometChatThreadHeader(
 *     parentMessage = message,
 *     bubbleFactories = customFactories
 * )
 * ```
 *
 * @param modifier Modifier applied to the root container
 * @param parentMessage The parent message to display (the message that started the thread)
 * @param viewModel The ViewModel managing thread header state (optional, creates default if not provided)
 * @param style Style configuration for the component
 * @param hideReactions Whether to hide reactions on the parent message bubble
 * @param hideAvatar Whether to hide the avatar on the parent message bubble
 * @param hideReceipts Whether to hide read receipts on the parent message bubble
 * @param hideReplyCount Whether to hide the reply count text
 * @param hideReplyCountBar Whether to hide the entire reply count bar section
 * @param maxHeight Maximum height constraint for the thread header (Dp.Unspecified for no limit)
 * @param alignment Message alignment mode (STANDARD or LEFT_ALIGNED)
 * @param textFormatters List of text formatters for mentions and markdown rendering
 * @param timeFormat Custom time format for message timestamps
 * @param bubbleFactories List of [BubbleFactory] instances for custom content rendering.
 *   Each factory declares its own category and type. Defaults to empty list which uses
 *   internal rendering for all standard message types.
 * @param leftBubbleMargin Padding for left-aligned (incoming) message bubbles
 * @param rightBubbleMargin Padding for right-aligned (outgoing) message bubbles
 * @param messageBubbleView Custom composable for rendering the parent message bubble
 * @param replyCountView Custom composable for rendering the reply count
 *
 * **Validates: Requirements 17.1, 17.2, 17.3, 17.4, 17.5**
 *
 * @see CometChatThreadHeaderStyle
 * @see ThreadHeaderViewModel
 * @see BubbleFactory
 */
@Composable
fun CometChatThreadHeader(
    modifier: Modifier = Modifier,
    parentMessage: BaseMessage,
    viewModel: ThreadHeaderViewModel = viewModel(),
    style: CometChatThreadHeaderStyle = CometChatThreadHeaderStyle.default(),
    // Visibility parameters
    hideReactions: Boolean = false,
    hideAvatar: Boolean = false,
    hideReceipts: Boolean = false,
    hideReplyCount: Boolean = false,
    hideReplyCountBar: Boolean = false,
    // Customization parameters
    maxHeight: Dp = Dp.Unspecified,
    alignment: UIKitConstants.MessageListAlignment = UIKitConstants.MessageListAlignment.STANDARD,
    textFormatters: List<CometChatTextFormatter>? = null,
    timeFormat: SimpleDateFormat? = null,
    // BubbleFactory integration
    bubbleFactories: List<BubbleFactory> = emptyList(),
    // Bubble margins
    leftBubbleMargin: PaddingValues = PaddingValues(start = 8.dp, end = 8.dp, top = 0.dp, bottom = 0.dp),
    rightBubbleMargin: PaddingValues = PaddingValues(start = 8.dp, end = 8.dp, top = 0.dp, bottom = 0.dp),
    // Custom view slots
    messageBubbleView: (@Composable (BaseMessage) -> Unit)? = null,
    replyCountView: (@Composable (Int) -> Unit)? = null
) {
    val context = LocalContext.current

    // Create default text formatters if none provided (same pattern as CometChatMessageList)
    // This ensures mentions are properly rendered in the parent message bubble
    val effectiveTextFormatters = textFormatters ?: remember(context) {
        listOf(CometChatMentionsFormatter(context))
    }

    // Convert bubble factories list to map for efficient lookup
    val factoryMap = remember(bubbleFactories) {
        bubbleFactories.toFactoryMap()
    }

    // Set parent message on ViewModel when it changes
    LaunchedEffect(parentMessage) {
        viewModel.setParentMessage(parentMessage)
    }

    // Set hideReaction flag on ViewModel
    LaunchedEffect(hideReactions) {
        viewModel.hideReaction = hideReactions
    }

    // Collect state from ViewModel
    val replyCount by viewModel.replyCountStateFlow.collectAsState()
    val messageList by viewModel.parentMessageListStateFlow.collectAsState()

    // Manage listener lifecycle
    DisposableEffect(Unit) {
        viewModel.addListener()
        viewModel.addLocalEventListeners()
        onDispose {
            viewModel.removeListener()
        }
    }

    // Apply max height constraint if specified
    val heightModifier = if (maxHeight != Dp.Unspecified) {
        Modifier.heightIn(max = maxHeight)
    } else {
        Modifier
    }

    // Container shape
    val shape = RoundedCornerShape(style.cornerRadius)

    // Main container
    Column(
        modifier = modifier
            .fillMaxWidth()
            .then(heightModifier)
            .clip(shape)
            .background(style.backgroundColor, shape)
            .then(
                if (style.strokeWidth > 0.dp) {
                    Modifier.border(style.strokeWidth, style.strokeColor, shape)
                } else {
                    Modifier
                }
            )
    ) {
        // Parent message bubble section
        // Weight is used to allow the bubble to take available space while reply count stays at bottom
        Box(
            modifier = Modifier
                .weight(1f, fill = false)
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 8.dp)
        ) {
            // Render message bubble
            if (messageBubbleView != null) {
                // Use custom message bubble view
                messageList.firstOrNull()?.let { message ->
                    messageBubbleView(message)
                }
            } else {
                // Use default message bubble rendering
                messageList.firstOrNull()?.let { message ->
                    DefaultMessageBubbleContent(
                        message = message,
                        alignment = alignment,
                        style = style,
                        hideReactions = hideReactions,
                        hideAvatar = hideAvatar,
                        hideReceipts = hideReceipts,
                        textFormatters = effectiveTextFormatters,
                        timeFormat = timeFormat,
                        bubbleFactories = factoryMap,
                        leftBubbleMargin = leftBubbleMargin,
                        rightBubbleMargin = rightBubbleMargin
                    )
                }
            }
        }

        // Reply count bar section
        if (!hideReplyCountBar) {
            ReplyCountBar(
                replyCount = replyCount,
                hideReplyCount = hideReplyCount,
                style = style,
                replyCountView = replyCountView
            )
        }
    }
}

/**
 * Default message bubble content rendering.
 * Renders the parent message using CometChatMessageBubble.
 */
@Composable
private fun DefaultMessageBubbleContent(
    message: BaseMessage,
    alignment: UIKitConstants.MessageListAlignment,
    style: CometChatThreadHeaderStyle,
    hideReactions: Boolean,
    hideAvatar: Boolean,
    hideReceipts: Boolean,
    textFormatters: List<CometChatTextFormatter>,
    timeFormat: SimpleDateFormat?,
    bubbleFactories: Map<String, BubbleFactory>,
    leftBubbleMargin: PaddingValues,
    rightBubbleMargin: PaddingValues
) {
    // Determine bubble alignment based on sender
    val loggedInUser = CometChat.getLoggedInUser()
    val isOutgoing = message.sender?.uid == loggedInUser?.uid
    
    val bubbleAlignment = when {
        alignment == UIKitConstants.MessageListAlignment.LEFT_ALIGNED -> 
            UIKitConstants.MessageBubbleAlignment.LEFT
        isOutgoing -> UIKitConstants.MessageBubbleAlignment.RIGHT
        else -> UIKitConstants.MessageBubbleAlignment.LEFT
    }
    
    // Determine which margin to use based on alignment
    val bubbleMargin = if (bubbleAlignment == UIKitConstants.MessageBubbleAlignment.RIGHT) {
        rightBubbleMargin
    } else {
        leftBubbleMargin
    }
    
    // Filter the appropriate factory from the map based on message type
    val factory = remember(message.id, message.deletedAt, bubbleFactories) {
        bubbleFactories[buildFactoryKey(message)]
    }
    
    // Scrollable container for long messages
    val scrollState = rememberScrollState()
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(bubbleMargin)
    ) {
        CometChatMessageBubble(
            message = message,
            alignment = bubbleAlignment,
            modifier = Modifier.fillMaxWidth(),
            hideReactions = hideReactions,
            hideReceipts = hideReceipts,
            shouldShowDefaultAvatar = !hideAvatar,
            textFormatters = textFormatters,
            timeFormat = timeFormat?.toPattern(),
            factory = factory,
            // Hide thread view in thread header (we're already in thread context)
            threadView = {}
        )
    }
}

/**
 * Reply count bar section displaying the number of replies.
 *
 * @param replyCount The current reply count
 * @param hideReplyCount Whether to hide the reply count text
 * @param style Style configuration
 * @param replyCountView Custom composable for rendering the reply count
 */
@Composable
private fun ReplyCountBar(
    replyCount: Int,
    hideReplyCount: Boolean,
    style: CometChatThreadHeaderStyle,
    replyCountView: (@Composable (Int) -> Unit)?
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(style.replyCountBackgroundColor)
    ) {
        if (!hideReplyCount) {
            if (replyCountView != null) {
                // Use custom reply count view
                replyCountView(replyCount)
            } else {
                // Default reply count text
                val replyText = formatReplyCount(replyCount)
                Text(
                    text = replyText,
                    color = style.replyCountTextColor,
                    style = style.replyCountTextStyle,
                    modifier = Modifier.padding(
                        start = 20.dp,
                        top = 4.dp,
                        end = 20.dp,
                        bottom = 4.dp
                    )
                )
            }
        }
    }
}

/**
 * Formats the reply count text.
 *
 * - 0 replies: "0 Replies"
 * - 1 reply: "1 Reply"
 * - N replies: "N Replies"
 *
 * **Validates: Requirements 3.2**
 *
 * @param count The reply count
 * @return Formatted reply count string
 */
@Composable
private fun formatReplyCount(count: Int): String {
    return when (count) {
        1 -> "$count ${stringResource(R.string.cometchat_reply)}"
        else -> "$count ${stringResource(R.string.cometchat_replies)}"
    }
}
