package com.cometchat.uikit.compose.presentation.messageheader.ui

import android.content.Context
import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.TypingIndicator
import com.cometchat.chat.models.User
import com.cometchat.uikit.compose.R
import com.cometchat.uikit.compose.presentation.callbuttons.style.CometChatCallButtonsStyle
import com.cometchat.uikit.compose.presentation.callbuttons.ui.CometChatCallButtons
import com.cometchat.uikit.compose.presentation.messageheader.style.CometChatMessageHeaderStyle
import com.cometchat.uikit.compose.presentation.shared.baseelements.avatar.CometChatAvatar
import com.cometchat.uikit.compose.presentation.shared.interfaces.DateTimeFormatterCallback
import com.cometchat.uikit.compose.presentation.shared.popupmenu.CometChatPopupMenu
import com.cometchat.uikit.compose.presentation.shared.statusindicator.CometChatStatusIndicator
import com.cometchat.uikit.compose.presentation.shared.statusindicator.StatusIndicator
import com.cometchat.uikit.compose.shared.views.popupmenu.MenuItem
import com.cometchat.uikit.core.factory.CometChatMessageHeaderViewModelFactory
import com.cometchat.uikit.core.state.MessageHeaderUIState
import com.cometchat.uikit.core.utils.CallsUtils
import com.cometchat.uikit.core.viewmodel.CometChatMessageHeaderViewModel

/**
 * CometChatMessageHeader displays the header of a chat conversation.
 * Shows user/group information including avatar, name, status, and typing indicators.
 * 
 * This component supports both user and group conversations with real-time updates
 * for online/offline status, typing indicators, and member count changes.
 * 
 * @param modifier Modifier applied to the parent container
 * @param user The User object to display (for one-on-one conversations)
 * @param group The Group object to display (for group conversations)
 * @param messageHeaderViewModel The ViewModel managing header state (optional, creates default if not provided)
 * @param style Style configuration for the component
 * @param hideBackButton Whether to hide the back navigation button
 * @param hideUserStatus Whether to hide user online/offline status
 * @param hideGroupStatus Whether to hide group type indicator
 * @param hideVideoCallButton Whether to hide the video call button
 * @param hideVoiceCallButton Whether to hide the voice call button
 * @param hideNewChatButton Whether to hide the AI new chat button
 * @param hideChatHistoryButton Whether to hide the AI chat history button
 * @param hideMenuIcon Whether to hide the overflow menu icon
 * @param options Menu options to display in the overflow menu
 * @param leadingView Custom composable for the leading section (avatar area)
 * @param titleView Custom composable for the title section
 * @param subtitleView Custom composable for the subtitle section
 * @param trailingView Custom composable for the trailing section
 * @param auxiliaryView Custom composable for the auxiliary section (call buttons, AI buttons)
 * @param itemView Custom composable that replaces the entire header content
 * @param lastSeenTextFormatter Custom formatter for last seen text
 * @param dateTimeFormatter Custom date/time formatter callback
 * @param onBackPress Callback for back navigation
 * @param onError Callback for errors
 * @param onNewChatClick Callback for AI new chat button click
 * @param onChatHistoryClick Callback for AI chat history button click
 * @param onVideoCallClick Callback for video call button click (overrides default CometChatCallButtons behavior)
 * @param onVoiceCallClick Callback for voice call button click (overrides default CometChatCallButtons behavior)
 */
@Composable
fun CometChatMessageHeader(
    modifier: Modifier = Modifier,
    // Data configuration - provide either user or group
    user: User? = null,
    group: Group? = null,
    // ViewModel (optional, creates default if not provided)
    messageHeaderViewModel: CometChatMessageHeaderViewModel? = null,
    // Style
    style: CometChatMessageHeaderStyle = CometChatMessageHeaderStyle.default(),
    // Visibility controls
    hideBackButton: Boolean = false,
    hideUserStatus: Boolean = false,
    hideGroupStatus: Boolean = false,
    hideVideoCallButton: Boolean = true,
    hideVoiceCallButton: Boolean = true,
    hideNewChatButton: Boolean = true,
    hideChatHistoryButton: Boolean = true,
    hideMenuIcon: Boolean = true,
    // Menu options
    options: List<MenuItem>? = null,
    // Custom view slots
    leadingView: (@Composable (User?, Group?) -> Unit)? = null,
    titleView: (@Composable (User?, Group?) -> Unit)? = null,
    subtitleView: (@Composable (User?, Group?) -> Unit)? = null,
    trailingView: (@Composable (User?, Group?) -> Unit)? = null,
    auxiliaryView: (@Composable (User?, Group?) -> Unit)? = null,
    itemView: (@Composable (User?, Group?) -> Unit)? = null,
    // Formatters
    lastSeenTextFormatter: ((Context, User) -> String)? = null,
    dateTimeFormatter: DateTimeFormatterCallback? = null,
    // Callbacks
    onBackPress: (() -> Unit)? = null,
    onError: ((CometChatException) -> Unit)? = null,
    onNewChatClick: (() -> Unit)? = null,
    onChatHistoryClick: (() -> Unit)? = null,
    onVideoCallClick: ((User?, Group?) -> Unit)? = null,
    onVoiceCallClick: ((User?, Group?) -> Unit)? = null
) {
    val context = LocalContext.current

    // Create default ViewModel if none provided
    val viewModel = messageHeaderViewModel ?: viewModel(
        factory = CometChatMessageHeaderViewModelFactory()
    )

    // Set user or group on ViewModel
    LaunchedEffect(user, group) {
        user?.let { viewModel.setUser(it) }
        group?.let { viewModel.setGroup(it) }
    }

    // Collect state from ViewModel
    val uiState by viewModel.uiState.collectAsState()
    val currentUser by viewModel.user.collectAsState()
    val currentGroup by viewModel.group.collectAsState()
    val typingIndicator by viewModel.typingIndicator.collectAsState()
    val memberCount by viewModel.memberCount.collectAsState()

    // Handle error events
    LaunchedEffect(Unit) {
        viewModel.errorEvent.collect { error ->
            onError?.invoke(error)
        }
    }

    // Popup menu state
    var showPopupMenu by remember { mutableStateOf(false) }

    // If itemView is provided, use it exclusively
    if (itemView != null) {
        Box(modifier = modifier.background(style.backgroundColor)) {
            itemView(currentUser, currentGroup)
        }
        return
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(style.backgroundColor)
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back button
        if (!hideBackButton) {
            IconButton(
                onClick = {
                    onBackPress?.invoke()
                },
                modifier = Modifier.semantics {
                    contentDescription = "Back"
                    role = Role.Button
                }
            ) {
                style.backIcon?.let { icon ->
                    Icon(
                        painter = icon,
                        contentDescription = null,
                        tint = style.backIconTint
                    )
                }
            }
        }

        // Leading view (Avatar)
        if (leadingView != null) {
            leadingView(currentUser, currentGroup)
        } else {
            DefaultLeadingView(
                user = currentUser,
                group = currentGroup,
                hideUserStatus = hideUserStatus,
                hideGroupStatus = hideGroupStatus,
                style = style
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Title and Subtitle
        Column(modifier = Modifier.weight(1f)) {
            if (titleView != null) {
                titleView(currentUser, currentGroup)
            } else {
                DefaultTitleView(
                    user = currentUser,
                    group = currentGroup,
                    style = style
                )
            }

            if (subtitleView != null) {
                subtitleView(currentUser, currentGroup)
            } else {
                DefaultSubtitleView(
                    user = currentUser,
                    group = currentGroup,
                    typingIndicator = typingIndicator,
                    memberCount = memberCount,
                    hideUserStatus = hideUserStatus,
                    lastSeenTextFormatter = lastSeenTextFormatter,
                    dateTimeFormatter = dateTimeFormatter,
                    style = style,
                    context = context
                )
            }
        }

        // Auxiliary view (call buttons, AI buttons)
        if (auxiliaryView != null) {
            auxiliaryView(currentUser, currentGroup)
        } else {
            DefaultAuxiliaryView(
                user = currentUser,
                group = currentGroup,
                hideVideoCallButton = hideVideoCallButton,
                hideVoiceCallButton = hideVoiceCallButton,
                hideNewChatButton = hideNewChatButton,
                hideChatHistoryButton = hideChatHistoryButton,
                onVideoCallClick = onVideoCallClick,
                onVoiceCallClick = onVoiceCallClick,
                onNewChatClick = onNewChatClick,
                onChatHistoryClick = onChatHistoryClick,
                onError = onError,
                style = style
            )
        }

        // Trailing view
        if (trailingView != null) {
            trailingView(currentUser, currentGroup)
        }

        // Menu icon
        if (!hideMenuIcon && !options.isNullOrEmpty()) {
            Box {
                IconButton(
                    onClick = { showPopupMenu = true },
                    modifier = Modifier.semantics {
                        contentDescription = context.getString(R.string.cometchat_menu)
                        role = Role.Button
                    }
                ) {
                    style.menuIcon?.let { icon ->
                        Icon(
                            painter = icon,
                            contentDescription = null,
                            tint = style.menuIconTint
                        )
                    }
                }

                CometChatPopupMenu(
                    expanded = showPopupMenu,
                    onDismissRequest = { showPopupMenu = false },
                    menuItems = options,
                    style = style.popupMenuStyle
                ) {
                    // Empty content - the menu icon is already rendered above
                }
            }
        }
    }
}

/**
 * Default leading view displaying avatar with status indicator.
 */
@Composable
private fun DefaultLeadingView(
    user: User?,
    group: Group?,
    hideUserStatus: Boolean,
    hideGroupStatus: Boolean,
    style: CometChatMessageHeaderStyle
) {
    Box(modifier = Modifier.size(48.dp)) {
        // Avatar
        CometChatAvatar(
            modifier = Modifier.size(48.dp),
            name = user?.name ?: group?.name ?: "",
            avatarUrl = user?.avatar ?: group?.icon,
            style = style.avatarStyle
        )

        // Status indicator
        val statusIndicator = when {
            user != null && !hideUserStatus && !isBlocked(user) -> {
                if (user.status == CometChatConstants.USER_STATUS_ONLINE) {
                    StatusIndicator.ONLINE
                } else {
                    StatusIndicator.OFFLINE
                }
            }
            group != null && !hideGroupStatus -> {
                when (group.groupType) {
                    CometChatConstants.GROUP_TYPE_PRIVATE -> StatusIndicator.PRIVATE_GROUP
                    CometChatConstants.GROUP_TYPE_PASSWORD -> StatusIndicator.PROTECTED_GROUP
                    else -> null
                }
            }
            else -> null
        }

        statusIndicator?.let { status ->
            if (status != StatusIndicator.OFFLINE) {
                CometChatStatusIndicator(
                    modifier = Modifier
                        .size(style.statusIndicatorStyle.size)
                        .align(Alignment.BottomEnd),
                    status = status,
                    style = style.statusIndicatorStyle
                )
            }
        }
    }
}

/**
 * Default title view displaying user/group name.
 */
@Composable
private fun DefaultTitleView(
    user: User?,
    group: Group?,
    style: CometChatMessageHeaderStyle
) {
    val name = user?.name ?: group?.name ?: ""
    Text(
        text = name,
        color = style.titleTextColor,
        style = style.titleTextStyle,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

/**
 * Default subtitle view displaying status, member count, or typing indicator.
 */
@Composable
private fun DefaultSubtitleView(
    user: User?,
    group: Group?,
    typingIndicator: TypingIndicator?,
    memberCount: Int,
    hideUserStatus: Boolean,
    lastSeenTextFormatter: ((Context, User) -> String)?,
    dateTimeFormatter: DateTimeFormatterCallback?,
    style: CometChatMessageHeaderStyle,
    context: Context
) {
    // Show typing indicator if present
    if (typingIndicator != null) {
        val typingText = if (typingIndicator.receiverType == CometChatConstants.RECEIVER_TYPE_USER) {
            context.getString(R.string.cometchat_typing)
        } else {
            "${typingIndicator.sender.name} ${context.getString(R.string.cometchat_is_typing)}"
        }
        Text(
            text = typingText,
            color = style.typingIndicatorTextColor,
            style = style.typingIndicatorTextStyle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        return
    }

    // Show user status or group member count
    when {
        user != null && !hideUserStatus && !isBlocked(user) -> {
            val statusText = if (user.status == CometChatConstants.USER_STATUS_ONLINE) {
                context.getString(R.string.cometchat_online)
            } else {
                lastSeenTextFormatter?.invoke(context, user)
                    ?: getLastSeenText(context, user, dateTimeFormatter)
            }
            Text(
                text = statusText,
                color = style.subtitleTextColor,
                style = style.subtitleTextStyle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        group != null -> {
            val memberText = if (memberCount > 1) {
                "$memberCount ${context.getString(R.string.cometchat_members)}"
            } else {
                "$memberCount ${context.getString(R.string.cometchat_member)}"
            }
            Text(
                text = memberText,
                color = style.subtitleTextColor,
                style = style.subtitleTextStyle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Default auxiliary view displaying call buttons and AI buttons.
 * Uses CometChatCallButtons component for proper call initiation with SDK integration.
 * 
 * Call buttons are only shown when:
 * 1. The hide flag is false
 * 2. CallsUtils.isCallingEnabled() returns true (SDK available AND enableCalling is true)
 * 3. A user or group is set
 * 
 * **Validates: Requirements 4.1-4.4**
 */
@Composable
private fun DefaultAuxiliaryView(
    user: User?,
    group: Group?,
    hideVideoCallButton: Boolean,
    hideVoiceCallButton: Boolean,
    hideNewChatButton: Boolean,
    hideChatHistoryButton: Boolean,
    onVideoCallClick: ((User?, Group?) -> Unit)?,
    onVoiceCallClick: ((User?, Group?) -> Unit)?,
    onNewChatClick: (() -> Unit)?,
    onChatHistoryClick: (() -> Unit)?,
    onError: ((CometChatException) -> Unit)?,
    style: CometChatMessageHeaderStyle
) {
    // Check if calling is enabled (SDK available AND enableCalling is true)
    val isCallingEnabled = CallsUtils.isCallingEnabled()
    
    Row(verticalAlignment = Alignment.CenterVertically) {
        // Call buttons using CometChatCallButtons component for proper SDK integration
        // Only show if calling is enabled AND at least one button should be visible
        val showVideoCall = !hideVideoCallButton && isCallingEnabled
        val showVoiceCall = !hideVoiceCallButton && isCallingEnabled
        val showCallButtons = showVideoCall || showVoiceCall
        
        if (showCallButtons && (user != null || group != null)) {
            CometChatCallButtons(
                user = user,
                group = group,
                voiceCallButtonVisibility = if (showVoiceCall) View.VISIBLE else View.GONE,
                videoCallButtonVisibility = if (showVideoCall) View.VISIBLE else View.GONE,
                buttonTextVisibility = View.GONE,
                buttonIconVisibility = View.VISIBLE,
                onVoiceCallClick = onVoiceCallClick,
                onVideoCallClick = onVideoCallClick,
                onError = onError,
                style = CometChatCallButtonsStyle.default()
            )
        }

        // New chat button (AI)
        if (!hideNewChatButton) {
            style.newChatIcon?.let { icon ->
                IconButton(onClick = { onNewChatClick?.invoke() }) {
                    Icon(
                        painter = icon,
                        contentDescription = "New chat",
                        tint = style.newChatIconTint
                    )
                }
            }
        }

        // Chat history button (AI)
        if (!hideChatHistoryButton) {
            style.chatHistoryIcon?.let { icon ->
                IconButton(onClick = { onChatHistoryClick?.invoke() }) {
                    Icon(
                        painter = icon,
                        contentDescription = "Chat history",
                        tint = style.chatHistoryIconTint
                    )
                }
            }
        }
    }
}

/**
 * Checks if a user is blocked (either blocked by me or has blocked me).
 */
private fun isBlocked(user: User?): Boolean {
    return user?.let { it.isBlockedByMe || it.isHasBlockedMe } ?: false
}

/**
 * Gets the last seen text for a user.
 */
private fun getLastSeenText(
    context: Context,
    user: User,
    dateTimeFormatter: DateTimeFormatterCallback?
): String {
    return if (user.lastActiveAt == 0L) {
        context.getString(R.string.cometchat_offline)
    } else {
        // Use the dateTimeFormatter if provided, otherwise use default formatting
        val formattedTime = dateTimeFormatter?.otherDays(user.lastActiveAt)
        if (formattedTime != null) {
            "${context.getString(R.string.cometchat_last_seen)} $formattedTime"
        } else {
            "${context.getString(R.string.cometchat_last_seen)} ${formatLastSeen(user.lastActiveAt)}"
        }
    }
}

/**
 * Formats the last seen timestamp to a human-readable string.
 */
private fun formatLastSeen(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - (timestamp * 1000) // Convert seconds to milliseconds if needed
    
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24
    
    return when {
        days > 0 -> "$days day${if (days > 1) "s" else ""} ago"
        hours > 0 -> "$hours hour${if (hours > 1) "s" else ""} ago"
        minutes > 0 -> "$minutes minute${if (minutes > 1) "s" else ""} ago"
        else -> "Just now"
    }
}
