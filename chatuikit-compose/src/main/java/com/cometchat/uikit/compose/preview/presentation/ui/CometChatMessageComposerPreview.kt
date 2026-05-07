package com.cometchat.uikit.compose.preview.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
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
import com.cometchat.uikit.compose.presentation.messagecomposer.style.CometChatMessageComposerStyle
import com.cometchat.uikit.compose.presentation.messagecomposer.ui.CometChatMessageComposer
import com.cometchat.uikit.core.domain.model.ComposerLayoutMode
import com.cometchat.uikit.core.factory.CometChatMessageComposerViewModelFactory
import com.cometchat.uikit.core.viewmodel.CometChatMessageComposerViewModel
import com.cometchat.uikit.compose.preview.data.repository.PreviewMessageComposerRepository
import com.cometchat.uikit.compose.presentation.messageheader.ui.CometChatMessageHeader
import com.cometchat.uikit.compose.preview.domain.PreviewMockData
import com.cometchat.uikit.compose.preview.presentation.viewmodels.PreviewMessageHeaderViewModelFactory
import com.cometchat.uikit.compose.theme.CometChatTheme

/**
 * Helper to create a preview ViewModel for MessageComposer.
 */
@Composable
private fun rememberPreviewComposerViewModel(
    simulateError: Boolean = false
): CometChatMessageComposerViewModel {
    val factory = remember(simulateError) {
        CometChatMessageComposerViewModelFactory(
            repository = PreviewMessageComposerRepository(
                simulateError = simulateError
            ),
            enableListeners = false
        )
    }
    return viewModel(factory = factory)
}

// ============================================================================
// SECTION 1: BASIC COMPOSER PREVIEWS
// ============================================================================

/**
 * Preview showing the default composer for a user conversation.
 */
@Preview(showBackground = true, name = "Composer - Default - User")
@Composable
fun PreviewComposerDefaultUser() {
    CometChatTheme {
        val user = PreviewMockData.createMockUser(
            name = "Alice Smith",
            status = CometChatConstants.USER_STATUS_ONLINE
        )
        val viewModel = rememberPreviewComposerViewModel()
        CometChatMessageComposer(
            user = user,
            viewModel = viewModel
        )
    }
}

/**
 * Preview showing the default composer for a group conversation.
 */
@Preview(showBackground = true, name = "Composer - Default - Group")
@Composable
fun PreviewComposerDefaultGroup() {
    CometChatTheme {
        val group = PreviewMockData.createMockGroup(
            name = "Engineering Team",
            groupType = CometChatConstants.GROUP_TYPE_PUBLIC,
            membersCount = 12
        )
        val viewModel = rememberPreviewComposerViewModel()
        CometChatMessageComposer(
            group = group,
            viewModel = viewModel
        )
    }
}

/**
 * Preview showing the composer with custom placeholder text.
 */
@Preview(showBackground = true, name = "Composer - Custom Placeholder")
@Composable
fun PreviewComposerCustomPlaceholder() {
    CometChatTheme {
        val user = PreviewMockData.createMockUser(name = "Alice Smith")
        val viewModel = rememberPreviewComposerViewModel()
        CometChatMessageComposer(
            user = user,
            viewModel = viewModel,
            placeholderText = "Write something nice..."
        )
    }
}

// ============================================================================
// SECTION 2: VISIBILITY PROPS PREVIEWS
// ============================================================================

/**
 * Preview with attachment button hidden.
 */
@Preview(showBackground = true, name = "Composer - Visibility - No Attachment")
@Composable
fun PreviewComposerNoAttachment() {
    CometChatTheme {
        val user = PreviewMockData.createMockUser(name = "Alice Smith")
        val viewModel = rememberPreviewComposerViewModel()
        CometChatMessageComposer(
            user = user,
            viewModel = viewModel,
            hideAttachmentButton = true
        )
    }
}

/**
 * Preview with voice recording button hidden.
 */
@Preview(showBackground = true, name = "Composer - Visibility - No Voice Recording")
@Composable
fun PreviewComposerNoVoiceRecording() {
    CometChatTheme {
        val user = PreviewMockData.createMockUser(name = "Alice Smith")
        val viewModel = rememberPreviewComposerViewModel()
        CometChatMessageComposer(
            user = user,
            viewModel = viewModel,
            hideVoiceRecordingButton = true
        )
    }
}

/**
 * Preview with send button hidden.
 */
@Preview(showBackground = true, name = "Composer - Visibility - No Send Button")
@Composable
fun PreviewComposerNoSendButton() {
    CometChatTheme {
        val user = PreviewMockData.createMockUser(name = "Alice Smith")
        val viewModel = rememberPreviewComposerViewModel()
        CometChatMessageComposer(
            user = user,
            viewModel = viewModel,
            hideSendButton = true
        )
    }
}

/**
 * Preview with auxiliary buttons hidden.
 */
@Preview(showBackground = true, name = "Composer - Visibility - No Auxiliary")
@Composable
fun PreviewComposerNoAuxiliary() {
    CometChatTheme {
        val user = PreviewMockData.createMockUser(name = "Alice Smith")
        val viewModel = rememberPreviewComposerViewModel()
        CometChatMessageComposer(
            user = user,
            viewModel = viewModel,
            hideAuxiliaryButton = true
        )
    }
}

/**
 * Preview with all optional buttons hidden (minimal composer).
 */
@Preview(showBackground = true, name = "Composer - Visibility - Minimal")
@Composable
fun PreviewComposerMinimal() {
    CometChatTheme {
        val user = PreviewMockData.createMockUser(name = "Alice Smith")
        val viewModel = rememberPreviewComposerViewModel()
        CometChatMessageComposer(
            user = user,
            viewModel = viewModel,
            hideAttachmentButton = true,
            hideVoiceRecordingButton = true,
            hideAuxiliaryButton = true,
            hideStickersButton = true
        )
    }
}

/**
 * Preview with stickers button hidden.
 */
@Preview(showBackground = true, name = "Composer - Visibility - No Stickers")
@Composable
fun PreviewComposerNoStickers() {
    CometChatTheme {
        val user = PreviewMockData.createMockUser(name = "Alice Smith")
        val viewModel = rememberPreviewComposerViewModel()
        CometChatMessageComposer(
            user = user,
            viewModel = viewModel,
            hideStickersButton = true
        )
    }
}

// ============================================================================
// SECTION 3: LAYOUT MODE PREVIEWS
// ============================================================================

/**
 * Preview showing single-line layout mode (default).
 */
@Preview(showBackground = true, name = "Composer - Layout - Single Line")
@Composable
fun PreviewComposerSingleLineLayout() {
    CometChatTheme {
        val user = PreviewMockData.createMockUser(name = "Alice Smith")
        val viewModel = rememberPreviewComposerViewModel()
        CometChatMessageComposer(
            user = user,
            viewModel = viewModel,
            layoutMode = ComposerLayoutMode.SINGLE_LINE
        )
    }
}

/**
 * Preview showing two-row layout mode.
 */
@Preview(showBackground = true, name = "Composer - Layout - Multi Line")
@Composable
fun PreviewComposerMultiLineLayout() {
    CometChatTheme {
        val user = PreviewMockData.createMockUser(name = "Alice Smith")
        val viewModel = rememberPreviewComposerViewModel()
        CometChatMessageComposer(
            user = user,
            viewModel = viewModel,
            layoutMode = ComposerLayoutMode.MULTI_LINE
        )
    }
}

// ============================================================================
// SECTION 4: RICH TEXT FORMATTING PREVIEWS
// ============================================================================

/**
 * Preview with rich text formatting enabled.
 */
@Preview(showBackground = true, name = "Composer - Rich Text - Enabled")
@Composable
fun PreviewComposerRichTextEnabled() {
    CometChatTheme {
        val user = PreviewMockData.createMockUser(name = "Alice Smith")
        val viewModel = rememberPreviewComposerViewModel()
        CometChatMessageComposer(
            user = user,
            viewModel = viewModel,
            enableRichTextFormatting = true
        )
    }
}

/**
 * Preview with rich text formatting disabled (default).
 */
@Preview(showBackground = true, name = "Composer - Rich Text - Disabled")
@Composable
fun PreviewComposerRichTextDisabled() {
    CometChatTheme {
        val user = PreviewMockData.createMockUser(name = "Alice Smith")
        val viewModel = rememberPreviewComposerViewModel()
        CometChatMessageComposer(
            user = user,
            viewModel = viewModel,
            enableRichTextFormatting = false
        )
    }
}

// ============================================================================
// SECTION 5: CUSTOM VIEW OVERRIDES
// ============================================================================

/**
 * Preview with custom header view.
 */
@Preview(showBackground = true, name = "Composer - Custom View - Header")
@Composable
fun PreviewComposerCustomHeaderView() {
    CometChatTheme {
        val user = PreviewMockData.createMockUser(name = "Alice Smith")
        val viewModel = rememberPreviewComposerViewModel()
        CometChatMessageComposer(
            user = user,
            viewModel = viewModel,
            headerView = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            CometChatTheme.colorScheme.infoColor.copy(alpha = 0.1f)
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "ℹ️ Replying to Alice's message",
                        style = CometChatTheme.typography.caption1Medium,
                        color = CometChatTheme.colorScheme.infoColor
                    )
                }
            }
        )
    }
}

/**
 * Preview with custom footer view.
 */
@Preview(showBackground = true, name = "Composer - Custom View - Footer")
@Composable
fun PreviewComposerCustomFooterView() {
    CometChatTheme {
        val user = PreviewMockData.createMockUser(name = "Alice Smith")
        val viewModel = rememberPreviewComposerViewModel()
        CometChatMessageComposer(
            user = user,
            viewModel = viewModel,
            footerView = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "End-to-end encrypted 🔒",
                        style = CometChatTheme.typography.caption2Regular,
                        color = CometChatTheme.colorScheme.textColorTertiary
                    )
                }
            }
        )
    }
}

/**
 * Preview with custom send button view.
 */
@Preview(showBackground = true, name = "Composer - Custom View - Send Button")
@Composable
fun PreviewComposerCustomSendButton() {
    CometChatTheme {
        val user = PreviewMockData.createMockUser(name = "Alice Smith")
        val viewModel = rememberPreviewComposerViewModel()
        CometChatMessageComposer(
            user = user,
            viewModel = viewModel,
            sendButtonView = { onClick, isActive, _ ->
                IconButton(
                    onClick = onClick,
                    enabled = isActive
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                if (isActive) CometChatTheme.colorScheme.primary
                                else CometChatTheme.colorScheme.backgroundColor3
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send",
                            tint = if (isActive) Color.White
                                   else CometChatTheme.colorScheme.iconTintSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        )
    }
}

/**
 * Preview with both header and footer views.
 */
@Preview(showBackground = true, name = "Composer - Custom View - Header + Footer")
@Composable
fun PreviewComposerHeaderAndFooter() {
    CometChatTheme {
        val user = PreviewMockData.createMockUser(name = "Alice Smith")
        val viewModel = rememberPreviewComposerViewModel()
        CometChatMessageComposer(
            user = user,
            viewModel = viewModel,
            headerView = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            CometChatTheme.colorScheme.warningColor.copy(alpha = 0.1f)
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "⚠️ Slow mode enabled — 1 message per minute",
                        style = CometChatTheme.typography.caption1Medium,
                        color = CometChatTheme.colorScheme.warningColor
                    )
                }
            },
            footerView = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Messages are stored securely",
                        style = CometChatTheme.typography.caption2Regular,
                        color = CometChatTheme.colorScheme.textColorTertiary
                    )
                }
            }
        )
    }
}

// ============================================================================
// SECTION 6: ATTACHMENT OPTION VISIBILITY PREVIEWS
// ============================================================================

/**
 * Preview with camera option hidden.
 */
@Preview(showBackground = true, name = "Composer - Attachment - No Camera")
@Composable
fun PreviewComposerNoCameraOption() {
    CometChatTheme {
        val user = PreviewMockData.createMockUser(name = "Alice Smith")
        val viewModel = rememberPreviewComposerViewModel()
        CometChatMessageComposer(
            user = user,
            viewModel = viewModel,
            hideCameraOption = true
        )
    }
}

/**
 * Preview with only image and document options.
 */
@Preview(showBackground = true, name = "Composer - Attachment - Image + Document Only")
@Composable
fun PreviewComposerImageDocumentOnly() {
    CometChatTheme {
        val user = PreviewMockData.createMockUser(name = "Alice Smith")
        val viewModel = rememberPreviewComposerViewModel()
        CometChatMessageComposer(
            user = user,
            viewModel = viewModel,
            hideCameraOption = true,
            hideVideoOption = true,
            hideAudioOption = true
        )
    }
}

// ============================================================================
// SECTION 7: STYLE CUSTOMIZATION PREVIEWS
// ============================================================================

/**
 * Preview with custom background color.
 */
@Preview(showBackground = true, name = "Composer - Style - Custom Background")
@Composable
fun PreviewComposerCustomBackground() {
    CometChatTheme {
        val user = PreviewMockData.createMockUser(name = "Alice Smith")
        val viewModel = rememberPreviewComposerViewModel()
        CometChatMessageComposer(
            user = user,
            viewModel = viewModel,
            style = CometChatMessageComposerStyle.default(
                backgroundColor = Color(0xFFF5F5F5)
            )
        )
    }
}

/**
 * Preview with custom compose box styling.
 */
@Preview(showBackground = true, name = "Composer - Style - Custom Compose Box")
@Composable
fun PreviewComposerCustomComposeBox() {
    CometChatTheme {
        val user = PreviewMockData.createMockUser(name = "Alice Smith")
        val viewModel = rememberPreviewComposerViewModel()
        CometChatMessageComposer(
            user = user,
            viewModel = viewModel,
            style = CometChatMessageComposerStyle.default(
                composeBoxBackgroundColor = Color(0xFFF0F4FF),
                composeBoxStrokeColor = CometChatTheme.colorScheme.primary,
                composeBoxCornerRadius = 24.dp
            )
        )
    }
}

// ============================================================================
// SECTION 8: COMPREHENSIVE PREVIEWS
// ============================================================================

/**
 * Preview showing composer with all features for a user conversation.
 */
@Preview(showBackground = true, name = "Composer - Comprehensive - User Full")
@Composable
fun PreviewComposerUserFull() {
    CometChatTheme {
        val user = PreviewMockData.createMockUser(
            name = "Alice Smith",
            status = CometChatConstants.USER_STATUS_ONLINE
        )
        val viewModel = rememberPreviewComposerViewModel()
        CometChatMessageComposer(
            user = user,
            viewModel = viewModel,
            placeholderText = "Message Alice...",
            enableRichTextFormatting = true
        )
    }
}

/**
 * Preview showing composer with all features for a group conversation.
 */
@Preview(showBackground = true, name = "Composer - Comprehensive - Group Full")
@Composable
fun PreviewComposerGroupFull() {
    CometChatTheme {
        val group = PreviewMockData.createMockGroup(
            name = "Engineering Team",
            groupType = CometChatConstants.GROUP_TYPE_PRIVATE,
            membersCount = 12
        )
        val viewModel = rememberPreviewComposerViewModel()
        CometChatMessageComposer(
            group = group,
            viewModel = viewModel,
            placeholderText = "Message Engineering Team...",
            enableRichTextFormatting = true
        )
    }
}

/**
 * Preview showing the most minimal composer configuration.
 */
@Preview(showBackground = true, name = "Composer - Comprehensive - Ultra Minimal")
@Composable
fun PreviewComposerUltraMinimal() {
    CometChatTheme {
        val user = PreviewMockData.createMockUser(name = "Alice Smith")
        val viewModel = rememberPreviewComposerViewModel()
        CometChatMessageComposer(
            user = user,
            viewModel = viewModel,
            hideAttachmentButton = true,
            hideVoiceRecordingButton = true,
            hideAuxiliaryButton = true,
            hideStickersButton = true,
            placeholderText = "Type a message..."
        )
    }
}

/**
 * Preview showing composer in a full chat context (header + composer).
 */
@Preview(showBackground = true, name = "Composer - Comprehensive - Chat Context")
@Composable
fun PreviewComposerChatContext() {
    CometChatTheme {
        val user = PreviewMockData.createMockUser(
            name = "Alice Smith",
            status = CometChatConstants.USER_STATUS_ONLINE
        )
        Column {
            // Simulated message header
            CometChatMessageHeader(
                user = user,
                messageHeaderViewModel = remember {
                    PreviewMessageHeaderViewModelFactory.createUserHeaderViewModel(user)
                }
            )

            // Spacer to simulate message area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(CometChatTheme.colorScheme.backgroundColor1),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Messages would appear here",
                    style = CometChatTheme.typography.bodyRegular,
                    color = CometChatTheme.colorScheme.textColorTertiary
                )
            }

            // Composer at the bottom
            val viewModel = rememberPreviewComposerViewModel()
            CometChatMessageComposer(
                user = user,
                viewModel = viewModel,
                placeholderText = "Message Alice..."
            )
        }
    }
}
