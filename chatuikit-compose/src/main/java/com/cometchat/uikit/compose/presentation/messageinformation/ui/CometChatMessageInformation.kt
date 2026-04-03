package com.cometchat.uikit.compose.presentation.messageinformation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.BaseMessage
import com.cometchat.uikit.compose.R
import com.cometchat.uikit.compose.presentation.messageinformation.style.CometChatMessageInformationStyle
import com.cometchat.uikit.compose.presentation.shared.formatters.CometChatMentionsFormatter
import com.cometchat.uikit.compose.presentation.shared.formatters.CometChatTextFormatter
import com.cometchat.uikit.compose.presentation.shared.messagebubble.BubbleFactory
import com.cometchat.uikit.compose.presentation.shared.messagebubble.toFactoryMap
import com.cometchat.uikit.compose.presentation.shared.messagebubble.ui.CometChatMessageBubble
import com.cometchat.uikit.compose.presentation.shared.messagebubble.ui.buildFactoryKey
import com.cometchat.uikit.compose.presentation.shared.receipts.CometChatReceipts
import com.cometchat.uikit.compose.presentation.shared.receipts.Receipt
import com.cometchat.uikit.compose.presentation.shared.shimmer.ui.CometChatListItemShimmer
import com.cometchat.uikit.compose.presentation.shared.shimmer.utils.ProvideShimmerAnimation
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.factory.CometChatMessageInformationViewModelFactory
import com.cometchat.uikit.core.state.MessageInformationUIState
import com.cometchat.uikit.core.viewmodel.CometChatMessageInformationViewModel


/**
 * CometChatMessageInformation displays detailed receipt information for a message.
 * 
 * This composable shows when a message was delivered and read, with different UI layouts
 * for user (one-to-one) and group conversations. It is presented as a modal bottom sheet.
 *
 * Per design doc: Overview section.
 *
 * Features:
 * - Presented as a modal bottom sheet dialog with rounded top corners
 * - For USER conversations: Shows static read/delivered timestamps from the message
 * - For GROUP conversations: Fetches and displays a list of receipts from all group members
 * - Supports real-time receipt updates via CometChatMessageEvents
 * - Displays the message bubble at the top for context (rendered internally)
 *
 * @param message The message to display information for
 * @param modifier Modifier applied to the container
 * @param viewModel Optional ViewModel (creates default if not provided)
 * @param style Style configuration for the component
 * @param toolBarTitleText Title text in toolbar (default: "Message Info")
 * @param hideToolBar Whether to hide the toolbar
 * @param bubbleFactories Optional list of bubble factories for custom message rendering
 * @param textFormatters Optional list of text formatters for mentions and markdown rendering.
 *   If not provided, a default CometChatMentionsFormatter will be created.
 * @param bubbleView Optional custom function to render message bubble (overrides default)
 * @param onDismiss Callback when bottom sheet is dismissed
 * @param onError Callback when an error occurs
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CometChatMessageInformation(
    message: BaseMessage,
    modifier: Modifier = Modifier,
    viewModel: CometChatMessageInformationViewModel? = null,
    style: CometChatMessageInformationStyle = CometChatMessageInformationStyle.default(),
    toolBarTitleText: String = stringResource(R.string.cometchat_message_info),
    hideToolBar: Boolean = false,
    bubbleFactories: List<BubbleFactory> = emptyList(),
    textFormatters: List<CometChatTextFormatter>? = null,
    bubbleView: (@Composable (BaseMessage) -> Unit)? = null,
    onDismiss: () -> Unit = {},
    onError: ((CometChatException) -> Unit)? = null
) {
    // Get context for creating default formatters
    val context = LocalContext.current
    
    // Create default ViewModel if none provided
    val messageInfoViewModel = viewModel ?: viewModel(
        factory = CometChatMessageInformationViewModelFactory()
    )

    // Initialize ViewModel with message synchronously during composition
    // This must happen BEFORE collecting state to avoid a frame delay where
    // uiState is null (showing shimmer) before setMessage() triggers the Loaded state.
    // Using LaunchedEffect would defer this to after the first composition frame,
    // causing the read receipt to not show on first click (ENG-32496).
    remember(message) {
        messageInfoViewModel.setMessage(message)
        true
    }

    // Collect state from ViewModel
    val uiState by messageInfoViewModel.state.collectAsState()
    val receipts by messageInfoViewModel.listData.collectAsState()
    val exception by messageInfoViewModel.exception.collectAsState()

    // Convert bubble factories to map
    val factoryMap = remember(bubbleFactories) {
        bubbleFactories.toFactoryMap()
    }
    
    // Create default text formatters if none provided (same pattern as CometChatThreadHeader)
    // This ensures mentions are properly rendered in the message bubble
    val effectiveTextFormatters = textFormatters ?: remember(context) {
        listOf(CometChatMentionsFormatter(context))
    }

    // Bottom sheet state
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    // Handle error callback
    LaunchedEffect(exception) {
        exception?.let { error ->
            onError?.invoke(error)
        }
    }

    // Lifecycle management - add/remove listener
    DisposableEffect(Unit) {
        messageInfoViewModel.addListener()
        onDispose {
            messageInfoViewModel.removeListener()
        }
    }

    // Determine conversation type
    val conversationType = messageInfoViewModel.getConversationType()

    // Modal Bottom Sheet
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(
            topStart = style.cornerRadius,
            topEnd = style.cornerRadius,
            bottomStart = 0.dp,
            bottomEnd = 0.dp
        ),
        containerColor = style.backgroundColor,
        scrimColor = Color.Black.copy(alpha = 0.5f),
        dragHandle = {
            // Drag handle indicator - 32dp x 4dp, centered
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .size(width = 32.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(style.strokeColor)
            )
        },
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Toolbar section
            if (!hideToolBar) {
                MessageInformationToolbar(
                    title = toolBarTitleText,
                    style = style
                )
            }

            // Message bubble section - render internally or use custom bubbleView
            MessageBubbleSection(
                message = message,
                factoryMap = factoryMap,
                textFormatters = effectiveTextFormatters,
                customBubbleView = bubbleView,
                style = style
            )

            // Content based on state and conversation type
            when (uiState) {
                is MessageInformationUIState.Loading -> {
                    // Shimmer loading state
                    MessageInformationShimmer(style = style)
                }

                is MessageInformationUIState.Loaded -> {
                    when (conversationType) {
                        CometChatConstants.RECEIVER_TYPE_USER -> {
                            // User receipt view
                            UserReceiptView(
                                message = message,
                                style = style
                            )
                        }
                        CometChatConstants.RECEIVER_TYPE_GROUP -> {
                            // Group receipt list
                            GroupReceiptList(
                                receipts = receipts,
                                style = style
                            )
                        }
                    }
                }

                is MessageInformationUIState.Empty -> {
                    // Empty state - show user receipt view for USER, hide for GROUP
                    if (conversationType == CometChatConstants.RECEIVER_TYPE_USER) {
                        UserReceiptView(
                            message = message,
                            style = style
                        )
                    }
                }

                is MessageInformationUIState.Error -> {
                    // Error state - treat as empty
                    if (conversationType == CometChatConstants.RECEIVER_TYPE_USER) {
                        UserReceiptView(
                            message = message,
                            style = style
                        )
                    }
                }

                null -> {
                    // Initial state - show shimmer
                    MessageInformationShimmer(style = style)
                }
            }

            // Spacer to fill remaining space
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}


/**
 * Toolbar section for the message information bottom sheet.
 * Per design doc: Toolbar Display section.
 */
@Composable
private fun MessageInformationToolbar(
    title: String,
    style: CometChatMessageInformationStyle
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = title,
            style = style.titleTextStyle,
            color = style.titleTextColor
        )
    }
}

/**
 * Message bubble section with highlight background and touch overlay.
 * Per design doc: Message Bubble Display section.
 * 
 * Renders the message bubble internally using CometChatMessageBubble,
 * or uses a custom bubbleView if provided.
 */
@Composable
private fun MessageBubbleSection(
    message: BaseMessage,
    factoryMap: Map<String, BubbleFactory>,
    textFormatters: List<CometChatTextFormatter>,
    customBubbleView: (@Composable (BaseMessage) -> Unit)?,
    style: CometChatMessageInformationStyle
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(style.backgroundHighlightColor)
            .padding(16.dp)
    ) {
        // Message bubble aligned to end (right side)
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.CenterEnd
        ) {
            if (customBubbleView != null) {
                // Use custom bubble view if provided
                customBubbleView(message)
            } else {
                // Filter the appropriate factory from the map based on message type
                val factory = remember(message.id, message.deletedAt, factoryMap) {
                    factoryMap[buildFactoryKey(message)]
                }
                
                // Render bubble internally using CometChatMessageBubble
                CometChatMessageBubble(
                    message = message,
                    alignment = UIKitConstants.MessageBubbleAlignment.RIGHT,
                    factory = factory,
                    shouldShowDefaultAvatar = false,
                    timeStampAlignment = UIKitConstants.TimeStampAlignment.BOTTOM,
                    textFormatters = textFormatters
                )
            }
        }

        // Overlay to block touch interactions
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { /* Block touches */ }
                )
        )
    }
}

/**
 * User receipt view for USER conversations.
 * Per design doc: User Conversation Receipt Display section.
 * 
 * Layout matches chatuikit-kotlin:
 * - Read section: icon (16dp) + label, timestamp below with 20dp left padding
 * - 16dp vertical spacing between sections
 * - Delivered section: icon (16dp) + label, timestamp below with 20dp left padding
 */
@Composable
private fun UserReceiptView(
    message: BaseMessage,
    style: CometChatMessageInformationStyle
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Read section
        UserReceiptSection(
            receipt = Receipt.READ,
            label = stringResource(R.string.cometchat_read),
            timestamp = message.readAt,
            style = style,
            isRead = true
        )

        // Spacer between sections (matches cometchat_padding_4 = 16dp)
        Spacer(modifier = Modifier.height(16.dp))

        // Delivered section
        UserReceiptSection(
            receipt = Receipt.DELIVERED,
            label = stringResource(R.string.cometchat_deliver),
            timestamp = message.deliveredAt,
            style = style,
            isRead = false
        )
    }
}

/**
 * Single receipt section (Read or Delivered) for USER conversations.
 * 
 * Layout matches chatuikit-kotlin XML:
 * - Row: Receipt icon (16dp x 16dp) + 4dp margin + Label (bodyRegular)
 * - Timestamp below with 20dp left margin and 4dp top margin (caption1Regular)
 * 
 * Note: USER view uses bodyRegular for labels, while GROUP view uses caption1Regular.
 * This is handled here by using CometChatTheme.typography.bodyRegular directly.
 */
@Composable
private fun UserReceiptSection(
    receipt: Receipt,
    label: String,
    timestamp: Long,
    style: CometChatMessageInformationStyle,
    isRead: Boolean
) {
    Column {
        // Icon and label row
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Receipt icon - 16dp x 16dp (matches cometchat_16dp)
            CometChatReceipts(
                receipt = receipt,
                style = style.messageReceiptStyle,
                modifier = Modifier.size(16.dp)
            )

            // 4dp margin (matches cometchat_padding_1)
            Spacer(modifier = Modifier.width(4.dp))

            // Label with bodyRegular style (matches cometchatTextAppearanceBodyRegular in USER view XML)
            Text(
                text = label,
                style = CometChatTheme.typography.bodyRegular,
                color = if (isRead) style.itemReadTextColor else style.itemDeliveredTextColor
            )
        }

        // Timestamp - only show if > 0
        // 20dp left margin (matches cometchat_20dp) and 4dp top margin (matches cometchat_padding_1)
        if (timestamp > 0) {
            Text(
                text = formatDateTime(timestamp * 1000), // Convert seconds to milliseconds
                style = if (isRead) style.itemReadDateTextStyle else style.itemDeliveredDateTextStyle,
                color = if (isRead) style.itemReadDateTextColor else style.itemDeliveredDateTextColor,
                modifier = Modifier.padding(start = 20.dp, top = 4.dp)
            )
        }
    }
}

/**
 * Group receipt list for GROUP conversations.
 * Per design doc: Group Conversation Receipt List section.
 */
@Composable
private fun GroupReceiptList(
    receipts: List<com.cometchat.chat.models.MessageReceipt>,
    style: CometChatMessageInformationStyle
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth()
    ) {
        items(
            items = receipts,
            key = { receipt -> "${receipt.sender?.uid}_${receipt.messageId}" }
        ) { receipt ->
            ReceiptListItem(
                receipt = receipt,
                style = style
            )

            // Separator
            if (receipts.last() != receipt) {
                HorizontalDivider(
                    color = style.separatorColor,
                    thickness = style.separatorHeight,
                    modifier = Modifier.padding(start = 68.dp) // Align with text content
                )
            }
        }
    }
}

/**
 * Shimmer loading state for message information.
 * Per design doc: Loading State section.
 */
@Composable
private fun MessageInformationShimmer(
    style: CometChatMessageInformationStyle
) {
    ProvideShimmerAnimation {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            repeat(5) {
                CometChatListItemShimmer()
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
