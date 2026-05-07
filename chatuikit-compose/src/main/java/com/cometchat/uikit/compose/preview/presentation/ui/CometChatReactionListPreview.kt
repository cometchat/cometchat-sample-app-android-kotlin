package com.cometchat.uikit.compose.preview.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cometchat.uikit.compose.theme.CometChatTheme

/**
 * NOTE: CometChatReactionList cannot be used directly in @Preview because it
 * internally calls CometChatUIKit.getLoggedInUser() and the ViewModel's
 * fetchReactedUsers() calls the real SDK.
 *
 * These previews simulate the ReactionList appearance using basic Compose primitives.
 */

// ============================================================================
// Helper: Simulated Reaction Tab
// ============================================================================

private data class ReactionTab(val emoji: String, val count: Int)
private data class ReactedUser(val name: String, val initials: String, val emoji: String, val isCurrentUser: Boolean = false)

@Composable
private fun SimulatedReactionListUI(
    tabs: List<ReactionTab> = listOf(
        ReactionTab("All", 8),
        ReactionTab("👍", 4),
        ReactionTab("❤️", 2),
        ReactionTab("😂", 2)
    ),
    users: List<ReactedUser> = listOf(
        ReactedUser("Alice Smith", "AS", "👍"),
        ReactedUser("Bob Johnson", "BJ", "👍"),
        ReactedUser("Charlie Brown", "CB", "❤️"),
        ReactedUser("Diana Prince", "DP", "👍"),
        ReactedUser("You", "ME", "😂", isCurrentUser = true),
        ReactedUser("Edward Norton", "EN", "😂"),
        ReactedUser("Fiona Apple", "FA", "❤️"),
        ReactedUser("George Lucas", "GL", "👍")
    ),
    selectedTabIndex: Int = 0
) {
    var activeTab by remember { mutableIntStateOf(selectedTabIndex) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CometChatTheme.colorScheme.backgroundColor1)
    ) {
        // Tab row
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(tabs.size) { index ->
                val tab = tabs[index]
                val isSelected = index == activeTab
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (isSelected) CometChatTheme.colorScheme.primary.copy(alpha = 0.1f)
                            else CometChatTheme.colorScheme.backgroundColor3
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "${tab.emoji} ${tab.count}",
                        style = CometChatTheme.typography.caption1Medium,
                        color = if (isSelected) CometChatTheme.colorScheme.primary
                               else CometChatTheme.colorScheme.textColorPrimary
                    )
                }
            }
        }

        HorizontalDivider(color = CometChatTheme.colorScheme.strokeColorLight)

        // User list
        LazyColumn {
            items(users) { user ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(CometChatTheme.colorScheme.extendedPrimaryColor500),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user.initials,
                            color = Color.White,
                            style = CometChatTheme.typography.caption1Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    // Name + subtitle
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = user.name,
                            style = CometChatTheme.typography.heading4Medium,
                            color = CometChatTheme.colorScheme.textColorPrimary
                        )
                        if (user.isCurrentUser) {
                            Text(
                                text = "Tap to remove",
                                style = CometChatTheme.typography.caption1Regular,
                                color = CometChatTheme.colorScheme.textColorSecondary
                            )
                        }
                    }
                    // Emoji
                    Text(
                        text = user.emoji,
                        style = CometChatTheme.typography.heading4Medium
                    )
                }
            }
        }
    }
}

// ============================================================================
// SECTION 1: DEFAULT PREVIEWS
// ============================================================================

@Preview(showBackground = true, name = "ReactionList - Default")
@Composable
fun PreviewReactionListDefault() {
    CometChatTheme {
        SimulatedReactionListUI()
    }
}

@Preview(showBackground = true, name = "ReactionList - Single Reaction")
@Composable
fun PreviewReactionListSingleReaction() {
    CometChatTheme {
        SimulatedReactionListUI(
            tabs = listOf(
                ReactionTab("All", 3),
                ReactionTab("👍", 3)
            ),
            users = listOf(
                ReactedUser("Alice Smith", "AS", "👍"),
                ReactedUser("Bob Johnson", "BJ", "👍"),
                ReactedUser("You", "ME", "👍", isCurrentUser = true)
            )
        )
    }
}

@Preview(showBackground = true, name = "ReactionList - Many Reactions")
@Composable
fun PreviewReactionListManyReactions() {
    CometChatTheme {
        SimulatedReactionListUI(
            tabs = listOf(
                ReactionTab("All", 15),
                ReactionTab("👍", 5),
                ReactionTab("❤️", 4),
                ReactionTab("😂", 3),
                ReactionTab("😮", 2),
                ReactionTab("🙏", 1)
            )
        )
    }
}

// ============================================================================
// SECTION 2: TAB SELECTION PREVIEWS
// ============================================================================

@Preview(showBackground = true, name = "ReactionList - Thumbs Up Tab Selected")
@Composable
fun PreviewReactionListThumbsUpTab() {
    CometChatTheme {
        SimulatedReactionListUI(
            selectedTabIndex = 1,
            users = listOf(
                ReactedUser("Alice Smith", "AS", "👍"),
                ReactedUser("Bob Johnson", "BJ", "👍"),
                ReactedUser("Diana Prince", "DP", "👍"),
                ReactedUser("George Lucas", "GL", "👍")
            )
        )
    }
}

@Preview(showBackground = true, name = "ReactionList - Heart Tab Selected")
@Composable
fun PreviewReactionListHeartTab() {
    CometChatTheme {
        SimulatedReactionListUI(
            selectedTabIndex = 2,
            users = listOf(
                ReactedUser("Charlie Brown", "CB", "❤️"),
                ReactedUser("Fiona Apple", "FA", "❤️")
            )
        )
    }
}

// ============================================================================
// SECTION 3: CURRENT USER PREVIEWS
// ============================================================================

@Preview(showBackground = true, name = "ReactionList - Current User Reaction")
@Composable
fun PreviewReactionListCurrentUser() {
    CometChatTheme {
        SimulatedReactionListUI(
            tabs = listOf(
                ReactionTab("All", 2),
                ReactionTab("👍", 1),
                ReactionTab("❤️", 1)
            ),
            users = listOf(
                ReactedUser("You", "ME", "👍", isCurrentUser = true),
                ReactedUser("Alice Smith", "AS", "❤️")
            )
        )
    }
}
