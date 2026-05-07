package com.cometchat.uikit.compose.preview.presentation.ui.shared

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.uikit.compose.presentation.shared.baseelements.avatar.AvatarStyle
import com.cometchat.uikit.compose.presentation.shared.baseelements.avatar.CometChatAvatar
import com.cometchat.uikit.compose.preview.domain.PreviewMockData
import com.cometchat.uikit.compose.theme.CometChatTheme

// ============================================================================
// SECTION 1: NAME-BASED AVATAR PREVIEWS
// ============================================================================

/**
 * Preview showing avatar with initials from name.
 */
@Preview(showBackground = true, name = "Avatar - Initials")
@Composable
fun PreviewAvatarInitials() {
    CometChatTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CometChatAvatar(
                name = "Alice Smith",
                modifier = Modifier.size(48.dp)
            )
            CometChatAvatar(
                name = "Bob Johnson",
                modifier = Modifier.size(48.dp)
            )
            CometChatAvatar(
                name = "Charlie",
                modifier = Modifier.size(48.dp)
            )
        }
    }
}

/**
 * Preview showing avatar with image URL (will show initials in preview since URL won't load).
 */
@Preview(showBackground = true, name = "Avatar - With URL")
@Composable
fun PreviewAvatarWithUrl() {
    CometChatTheme {
        CometChatAvatar(
            name = "Alice Smith",
            avatarUrl = "https://example.com/avatar.jpg",
            modifier = Modifier
                .size(48.dp)
                .padding(16.dp)
        )
    }
}

// ============================================================================
// SECTION 2: USER/GROUP AVATAR PREVIEWS
// ============================================================================

/**
 * Preview showing avatar from User object.
 */
@Preview(showBackground = true, name = "Avatar - User Object")
@Composable
fun PreviewAvatarUser() {
    CometChatTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CometChatAvatar(
                user = PreviewMockData.createMockUser(name = "Alice Smith"),
                modifier = Modifier.size(48.dp)
            )
            CometChatAvatar(
                user = PreviewMockData.createMockUser(name = "Bob Johnson"),
                modifier = Modifier.size(48.dp)
            )
        }
    }
}

/**
 * Preview showing avatar from Group object.
 */
@Preview(showBackground = true, name = "Avatar - Group Object")
@Composable
fun PreviewAvatarGroup() {
    CometChatTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CometChatAvatar(
                group = PreviewMockData.createMockGroup(name = "Engineering Team"),
                modifier = Modifier.size(48.dp)
            )
            CometChatAvatar(
                group = PreviewMockData.createMockGroup(name = "Design Squad"),
                modifier = Modifier.size(48.dp)
            )
        }
    }
}

// ============================================================================
// SECTION 3: SIZE VARIATIONS
// ============================================================================

/**
 * Preview showing avatars in different sizes.
 */
@Preview(showBackground = true, name = "Avatar - Sizes")
@Composable
fun PreviewAvatarSizes() {
    CometChatTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CometChatAvatar(name = "AS", modifier = Modifier.size(24.dp))
            CometChatAvatar(name = "AS", modifier = Modifier.size(36.dp))
            CometChatAvatar(name = "AS", modifier = Modifier.size(48.dp))
            CometChatAvatar(name = "AS", modifier = Modifier.size(64.dp))
            CometChatAvatar(name = "AS", modifier = Modifier.size(80.dp))
        }
    }
}

// ============================================================================
// SECTION 4: STYLE CUSTOMIZATION PREVIEWS
// ============================================================================

/**
 * Preview showing avatar with custom background color.
 */
@Preview(showBackground = true, name = "Avatar - Custom Colors")
@Composable
fun PreviewAvatarCustomColors() {
    CometChatTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CometChatAvatar(
                name = "Alice",
                modifier = Modifier.size(48.dp),
                style = AvatarStyle.default(
                    backgroundColor = Color(0xFF6200EE)
                )
            )
            CometChatAvatar(
                name = "Bob",
                modifier = Modifier.size(48.dp),
                style = AvatarStyle.default(
                    backgroundColor = Color(0xFF03DAC5)
                )
            )
            CometChatAvatar(
                name = "Charlie",
                modifier = Modifier.size(48.dp),
                style = AvatarStyle.default(
                    backgroundColor = Color(0xFFFF5722)
                )
            )
        }
    }
}

/**
 * Preview showing avatar with custom corner radius (square vs rounded vs circle).
 */
@Preview(showBackground = true, name = "Avatar - Corner Radius")
@Composable
fun PreviewAvatarCornerRadius() {
    CometChatTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Square
            CometChatAvatar(
                name = "SQ",
                modifier = Modifier.size(48.dp),
                style = AvatarStyle.default(cornerRadius = 0.dp)
            )
            // Rounded
            CometChatAvatar(
                name = "RD",
                modifier = Modifier.size(48.dp),
                style = AvatarStyle.default(cornerRadius = 8.dp)
            )
            // Circle (default)
            CometChatAvatar(
                name = "CR",
                modifier = Modifier.size(48.dp),
                style = AvatarStyle.default(cornerRadius = 100.dp)
            )
        }
    }
}

/**
 * Preview showing avatar with border.
 */
@Preview(showBackground = true, name = "Avatar - With Border")
@Composable
fun PreviewAvatarWithBorder() {
    CometChatTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CometChatAvatar(
                name = "Alice",
                modifier = Modifier.size(48.dp),
                style = AvatarStyle.default(
                    borderWidth = 2.dp,
                    borderColor = CometChatTheme.colorScheme.primary
                )
            )
            CometChatAvatar(
                name = "Bob",
                modifier = Modifier.size(48.dp),
                style = AvatarStyle.default(
                    borderWidth = 3.dp,
                    borderColor = CometChatTheme.colorScheme.successColor
                )
            )
        }
    }
}
