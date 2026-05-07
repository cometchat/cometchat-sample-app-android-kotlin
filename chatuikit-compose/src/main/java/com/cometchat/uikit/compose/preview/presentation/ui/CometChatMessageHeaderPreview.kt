package com.cometchat.uikit.compose.preview.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.User
import com.cometchat.uikit.compose.presentation.messageheader.style.CometChatMessageHeaderStyle
import com.cometchat.uikit.compose.presentation.messageheader.ui.CometChatMessageHeader
import com.cometchat.uikit.core.factory.CometChatMessageHeaderViewModelFactory
import com.cometchat.uikit.core.viewmodel.CometChatMessageHeaderViewModel
import com.cometchat.uikit.compose.preview.data.repository.PreviewMessageHeaderRepository
import com.cometchat.uikit.compose.preview.domain.PreviewMockData
import com.cometchat.uikit.compose.preview.presentation.viewmodels.PreviewMessageHeaderViewModelFactory
import com.cometchat.uikit.compose.theme.CometChatTheme

/**
 * Helper to create a preview ViewModel for MessageHeader.
 */
@Composable
private fun rememberPreviewMessageHeaderViewModel(
    mockUser: User = PreviewMockData.createMockUser(name = "Alice Smith"),
    mockGroup: Group = PreviewMockData.createMockGroup(name = "Engineering Team", membersCount = 12),
    simulateError: Boolean = false
): CometChatMessageHeaderViewModel {
    val factory = remember(mockUser, mockGroup, simulateError) {
        CometChatMessageHeaderViewModelFactory(
            repository = PreviewMessageHeaderRepository(
                mockUser = mockUser,
                mockGroup = mockGroup,
                simulateError = simulateError
            ),
            enableListeners = false
        )
    }
    return viewModel(factory = factory)
}

// ============================================================================
// SECTION 1: USER CONVERSATION HEADER PREVIEWS
// ============================================================================

/**
 * Preview showing a user conversation header with online status.
 */
@Preview(showBackground = true, name = "MessageHeader - User - Online")
@Composable
fun PreviewMessageHeaderUserOnline() {
    CometChatTheme {
        val user = PreviewMockData.createMockUser(
            name = "Alice Smith",
            status = CometChatConstants.USER_STATUS_ONLINE
        )
        val viewModel = rememberPreviewMessageHeaderViewModel(mockUser = user)
        CometChatMessageHeader(
            user = user,
            messageHeaderViewModel = viewModel
        )
    }
}

/**
 * Preview showing a user conversation header with offline status.
 */
@Preview(showBackground = true, name = "MessageHeader - User - Offline")
@Composable
fun PreviewMessageHeaderUserOffline() {
    CometChatTheme {
        val user = PreviewMockData.createMockUser(
            name = "Bob Johnson",
            status = CometChatConstants.USER_STATUS_OFFLINE
        )
        val viewModel = rememberPreviewMessageHeaderViewModel(mockUser = user)
        CometChatMessageHeader(
            user = user,
            messageHeaderViewModel = viewModel
        )
    }
}

/**
 * Preview showing a user header without back button.
 */
@Preview(showBackground = true, name = "MessageHeader - User - No Back Button")
@Composable
fun PreviewMessageHeaderUserNoBackButton() {
    CometChatTheme {
        val user = PreviewMockData.createMockUser(name = "Charlie Brown")
        val viewModel = rememberPreviewMessageHeaderViewModel(mockUser = user)
        CometChatMessageHeader(
            user = user,
            messageHeaderViewModel = viewModel,
            hideBackButton = true
        )
    }
}

// ============================================================================
// SECTION 2: GROUP CONVERSATION HEADER PREVIEWS
// ============================================================================

/**
 * Preview showing a public group conversation header.
 */
@Preview(showBackground = true, name = "MessageHeader - Group - Public")
@Composable
fun PreviewMessageHeaderGroupPublic() {
    CometChatTheme {
        val group = PreviewMockData.createMockGroup(
            name = "Engineering Team",
            groupType = CometChatConstants.GROUP_TYPE_PUBLIC,
            membersCount = 12
        )
        val viewModel = rememberPreviewMessageHeaderViewModel(mockGroup = group)
        CometChatMessageHeader(
            group = group,
            messageHeaderViewModel = viewModel
        )
    }
}

/**
 * Preview showing a private group conversation header.
 */
@Preview(showBackground = true, name = "MessageHeader - Group - Private")
@Composable
fun PreviewMessageHeaderGroupPrivate() {
    CometChatTheme {
        val group = PreviewMockData.createMockGroup(
            name = "Project Alpha",
            groupType = CometChatConstants.GROUP_TYPE_PRIVATE,
            membersCount = 8
        )
        val viewModel = rememberPreviewMessageHeaderViewModel(mockGroup = group)
        CometChatMessageHeader(
            group = group,
            messageHeaderViewModel = viewModel
        )
    }
}

/**
 * Preview showing a password-protected group conversation header.
 */
@Preview(showBackground = true, name = "MessageHeader - Group - Protected")
@Composable
fun PreviewMessageHeaderGroupProtected() {
    CometChatTheme {
        val group = PreviewMockData.createMockGroup(
            name = "VIP Lounge",
            groupType = CometChatConstants.GROUP_TYPE_PASSWORD,
            membersCount = 5
        )
        val viewModel = rememberPreviewMessageHeaderViewModel(mockGroup = group)
        CometChatMessageHeader(
            group = group,
            messageHeaderViewModel = viewModel
        )
    }
}

/**
 * Preview showing a group header with large member count.
 */
@Preview(showBackground = true, name = "MessageHeader - Group - Large Member Count")
@Composable
fun PreviewMessageHeaderGroupLargeMemberCount() {
    CometChatTheme {
        val group = PreviewMockData.createMockGroup(
            name = "Global Community",
            groupType = CometChatConstants.GROUP_TYPE_PUBLIC,
            membersCount = 500
        )
        val viewModel = rememberPreviewMessageHeaderViewModel(mockGroup = group)
        CometChatMessageHeader(
            group = group,
            messageHeaderViewModel = viewModel
        )
    }
}

// ============================================================================
// SECTION 3: VISIBILITY PROPS PREVIEWS
// ============================================================================

/**
 * Preview with hidden user status.
 */
@Preview(showBackground = true, name = "MessageHeader - Visibility - No User Status")
@Composable
fun PreviewMessageHeaderHideUserStatus() {
    CometChatTheme {
        val user = PreviewMockData.createMockUser(
            name = "Alice Smith",
            status = CometChatConstants.USER_STATUS_ONLINE
        )
        val viewModel = rememberPreviewMessageHeaderViewModel(mockUser = user)
        CometChatMessageHeader(
            user = user,
            messageHeaderViewModel = viewModel,
            hideUserStatus = true
        )
    }
}

/**
 * Preview with hidden group status.
 */
@Preview(showBackground = true, name = "MessageHeader - Visibility - No Group Status")
@Composable
fun PreviewMessageHeaderHideGroupStatus() {
    CometChatTheme {
        val group = PreviewMockData.createMockGroup(
            name = "Private Team",
            groupType = CometChatConstants.GROUP_TYPE_PRIVATE,
            membersCount = 8
        )
        val viewModel = rememberPreviewMessageHeaderViewModel(mockGroup = group)
        CometChatMessageHeader(
            group = group,
            messageHeaderViewModel = viewModel,
            hideGroupStatus = true
        )
    }
}

/**
 * Preview with video and voice call buttons visible.
 */
@Preview(showBackground = true, name = "MessageHeader - Visibility - Call Buttons")
@Composable
fun PreviewMessageHeaderCallButtons() {
    CometChatTheme {
        val user = PreviewMockData.createMockUser(
            name = "Alice Smith",
            status = CometChatConstants.USER_STATUS_ONLINE
        )
        val viewModel = rememberPreviewMessageHeaderViewModel(mockUser = user)
        CometChatMessageHeader(
            user = user,
            messageHeaderViewModel = viewModel,
            hideVideoCallButton = false,
            hideVoiceCallButton = false
        )
    }
}

/**
 * Preview with back button hidden.
 */
@Preview(showBackground = true, name = "MessageHeader - Visibility - No Back Button")
@Composable
fun PreviewMessageHeaderNoBackButton() {
    CometChatTheme {
        val user = PreviewMockData.createMockUser(name = "Alice Smith")
        val viewModel = rememberPreviewMessageHeaderViewModel(mockUser = user)
        CometChatMessageHeader(
            user = user,
            messageHeaderViewModel = viewModel,
            hideBackButton = true
        )
    }
}

// ============================================================================
// SECTION 4: CUSTOM VIEW OVERRIDES
// ============================================================================

/**
 * Preview with custom leading view (avatar area).
 */
@Preview(showBackground = true, name = "MessageHeader - Custom View - Leading")
@Composable
fun PreviewMessageHeaderCustomLeadingView() {
    CometChatTheme {
        val user = PreviewMockData.createMockUser(name = "Alice Smith")
        val viewModel = rememberPreviewMessageHeaderViewModel(mockUser = user)
        CometChatMessageHeader(
            user = user,
            messageHeaderViewModel = viewModel,
            leadingView = { u, _ ->
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(CometChatTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = u?.name?.firstOrNull()?.uppercase() ?: "?",
                        style = CometChatTheme.typography.heading4Bold,
                        color = CometChatTheme.colorScheme.colorWhite
                    )
                }
            }
        )
    }
}

/**
 * Preview with custom title view.
 */
@Preview(showBackground = true, name = "MessageHeader - Custom View - Title")
@Composable
fun PreviewMessageHeaderCustomTitleView() {
    CometChatTheme {
        val user = PreviewMockData.createMockUser(name = "Alice Smith")
        val viewModel = rememberPreviewMessageHeaderViewModel(mockUser = user)
        CometChatMessageHeader(
            user = user,
            messageHeaderViewModel = viewModel,
            titleView = { u, _ ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "★",
                        color = CometChatTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = u?.name ?: "Unknown",
                        style = CometChatTheme.typography.heading4Bold,
                        color = CometChatTheme.colorScheme.primary
                    )
                }
            }
        )
    }
}

/**
 * Preview with custom subtitle view.
 */
@Preview(showBackground = true, name = "MessageHeader - Custom View - Subtitle")
@Composable
fun PreviewMessageHeaderCustomSubtitleView() {
    CometChatTheme {
        val user = PreviewMockData.createMockUser(
            name = "Alice Smith",
            status = CometChatConstants.USER_STATUS_ONLINE
        )
        val viewModel = rememberPreviewMessageHeaderViewModel(mockUser = user)
        CometChatMessageHeader(
            user = user,
            messageHeaderViewModel = viewModel,
            subtitleView = { u, _ ->
                Text(
                    text = "🟢 Active now",
                    style = CometChatTheme.typography.caption1Medium,
                    color = CometChatTheme.colorScheme.successColor
                )
            }
        )
    }
}

/**
 * Preview with custom trailing view.
 */
@Preview(showBackground = true, name = "MessageHeader - Custom View - Trailing")
@Composable
fun PreviewMessageHeaderCustomTrailingView() {
    CometChatTheme {
        val user = PreviewMockData.createMockUser(name = "Alice Smith")
        val viewModel = rememberPreviewMessageHeaderViewModel(mockUser = user)
        CometChatMessageHeader(
            user = user,
            messageHeaderViewModel = viewModel,
            trailingView = { _, _ ->
                IconButton(onClick = { }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = CometChatTheme.colorScheme.iconTintPrimary
                    )
                }
            }
        )
    }
}

/**
 * Preview with all custom views combined.
 */
@Preview(showBackground = true, name = "MessageHeader - Custom View - All Combined")
@Composable
fun PreviewMessageHeaderAllCustomViews() {
    CometChatTheme {
        val user = PreviewMockData.createMockUser(name = "Alice Smith")
        val viewModel = rememberPreviewMessageHeaderViewModel(mockUser = user)
        CometChatMessageHeader(
            user = user,
            messageHeaderViewModel = viewModel,
            leadingView = { u, _ ->
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(CometChatTheme.colorScheme.infoColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = u?.name?.take(2)?.uppercase() ?: "??",
                        style = CometChatTheme.typography.caption1Bold,
                        color = Color.White
                    )
                }
            },
            titleView = { u, _ ->
                Text(
                    text = "★ ${u?.name ?: "Unknown"}",
                    style = CometChatTheme.typography.heading4Bold,
                    color = CometChatTheme.colorScheme.primary
                )
            },
            subtitleView = { _, _ ->
                Text(
                    text = "Custom subtitle",
                    style = CometChatTheme.typography.caption1Regular,
                    color = CometChatTheme.colorScheme.textColorSecondary
                )
            },
            trailingView = { _, _ ->
                IconButton(onClick = { }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = CometChatTheme.colorScheme.iconTintPrimary
                    )
                }
            }
        )
    }
}

// ============================================================================
// SECTION 5: STYLE CUSTOMIZATION PREVIEWS
// ============================================================================

/**
 * Preview with custom background color.
 */
@Preview(showBackground = true, name = "MessageHeader - Style - Custom Background")
@Composable
fun PreviewMessageHeaderCustomBackground() {
    CometChatTheme {
        val user = PreviewMockData.createMockUser(name = "Alice Smith")
        val viewModel = rememberPreviewMessageHeaderViewModel(mockUser = user)
        CometChatMessageHeader(
            user = user,
            messageHeaderViewModel = viewModel,
            style = CometChatMessageHeaderStyle.default(
                backgroundColor = Color(0xFFF0F4FF)
            )
        )
    }
}

/**
 * Preview with custom title text color.
 */
@Preview(showBackground = true, name = "MessageHeader - Style - Custom Title Color")
@Composable
fun PreviewMessageHeaderCustomTitleColor() {
    CometChatTheme {
        val user = PreviewMockData.createMockUser(name = "Alice Smith")
        val viewModel = rememberPreviewMessageHeaderViewModel(mockUser = user)
        CometChatMessageHeader(
            user = user,
            messageHeaderViewModel = viewModel,
            style = CometChatMessageHeaderStyle.default(
                titleTextColor = CometChatTheme.colorScheme.primary
            )
        )
    }
}

// ============================================================================
// SECTION 6: COMPREHENSIVE PREVIEWS
// ============================================================================

/**
 * Preview showing user header with all features.
 */
@Preview(showBackground = true, name = "MessageHeader - Comprehensive - User Full")
@Composable
fun PreviewMessageHeaderUserFull() {
    CometChatTheme {
        val user = PreviewMockData.createMockUser(
            name = "Alice Smith",
            status = CometChatConstants.USER_STATUS_ONLINE
        )
        val viewModel = rememberPreviewMessageHeaderViewModel(mockUser = user)
        CometChatMessageHeader(
            user = user,
            messageHeaderViewModel = viewModel,
            hideVideoCallButton = false,
            hideVoiceCallButton = false,
            onBackPress = { }
        )
    }
}

/**
 * Preview showing group header with all features.
 */
@Preview(showBackground = true, name = "MessageHeader - Comprehensive - Group Full")
@Composable
fun PreviewMessageHeaderGroupFull() {
    CometChatTheme {
        val group = PreviewMockData.createMockGroup(
            name = "Engineering Team",
            groupType = CometChatConstants.GROUP_TYPE_PRIVATE,
            membersCount = 12
        )
        val viewModel = rememberPreviewMessageHeaderViewModel(mockGroup = group)
        CometChatMessageHeader(
            group = group,
            messageHeaderViewModel = viewModel,
            hideVideoCallButton = false,
            hideVoiceCallButton = false,
            onBackPress = { }
        )
    }
}

/**
 * Preview showing minimal header configuration.
 */
@Preview(showBackground = true, name = "MessageHeader - Comprehensive - Minimal")
@Composable
fun PreviewMessageHeaderMinimal() {
    CometChatTheme {
        val user = PreviewMockData.createMockUser(name = "Alice Smith")
        val viewModel = rememberPreviewMessageHeaderViewModel(mockUser = user)
        CometChatMessageHeader(
            user = user,
            messageHeaderViewModel = viewModel,
            hideBackButton = true,
            hideUserStatus = true
        )
    }
}

/**
 * Preview comparing user and group headers side by side.
 */
@Preview(showBackground = true, name = "MessageHeader - Comprehensive - User vs Group")
@Composable
fun PreviewMessageHeaderUserVsGroup() {
    CometChatTheme {
        Column {
            // User header
            val user = PreviewMockData.createMockUser(
                name = "Alice Smith",
                status = CometChatConstants.USER_STATUS_ONLINE
            )
            val userViewModel = remember {
                PreviewMessageHeaderViewModelFactory.createUserHeaderViewModel(user)
            }
            CometChatMessageHeader(
                user = user,
                messageHeaderViewModel = userViewModel
            )

            // Group header
            val group = PreviewMockData.createMockGroup(
                name = "Engineering Team",
                groupType = CometChatConstants.GROUP_TYPE_PRIVATE,
                membersCount = 12
            )
            val groupViewModel = remember {
                PreviewMessageHeaderViewModelFactory.createGroupHeaderViewModel(group)
            }
            CometChatMessageHeader(
                group = group,
                messageHeaderViewModel = groupViewModel
            )
        }
    }
}
