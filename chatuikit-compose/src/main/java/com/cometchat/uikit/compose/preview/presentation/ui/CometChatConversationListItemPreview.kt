package com.cometchat.uikit.compose.preview.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.compose.presentation.conversations.ui.CometChatConversationListItem
import com.cometchat.uikit.compose.presentation.conversations.style.CometChatConversationListItemStyle
import com.cometchat.uikit.compose.presentation.conversations.utils.ConversationUtils
import com.cometchat.uikit.compose.presentation.conversations.utils.TypingIndicator
import com.cometchat.uikit.compose.preview.domain.PreviewMockData
import com.cometchat.uikit.compose.theme.CometChatTheme

// ============================================================================
// Preview: User Conversation
// ============================================================================

/**
 * Preview showing a basic user conversation.
 */
@Preview(showBackground = true, name = "User Conversation")
@Composable
fun PreviewUserConversation() {
    CometChatTheme {
        val conversation = PreviewMockData.createUserConversation(
            user = PreviewMockData.createMockUser(
                name = "Alice Smith",
                status = CometChatConstants.USER_STATUS_ONLINE
            ),
            lastMessage = PreviewMockData.createMockTextMessage(
                text = "Hey! Are you coming to the meeting today?"
            )
        )
        
        CometChatConversationListItem(
            conversation = conversation,
            onItemClick = { }
        )
    }
}


/**
 * Preview showing a user conversation with offline status.
 */
@Preview(showBackground = true, name = "User Conversation - Offline")
@Composable
fun PreviewUserConversationOffline() {
    CometChatTheme {
        val conversation = PreviewMockData.createUserConversation(
            user = PreviewMockData.createMockUser(
                name = "Bob Johnson",
                status = CometChatConstants.USER_STATUS_OFFLINE
            ),
            lastMessage = PreviewMockData.createMockTextMessage(
                text = "See you tomorrow!"
            )
        )
        
        CometChatConversationListItem(
            conversation = conversation,
            onItemClick = { }
        )
    }
}

// ============================================================================
// Preview: Group Conversation
// ============================================================================

/**
 * Preview showing a public group conversation.
 */
@Preview(showBackground = true, name = "Group Conversation - Public")
@Composable
fun PreviewGroupConversationPublic() {
    CometChatTheme {
        val conversation = PreviewMockData.createGroupConversation(
            group = PreviewMockData.createMockGroup(
                name = "Engineering Team",
                groupType = CometChatConstants.GROUP_TYPE_PUBLIC
            ),
            lastMessage = PreviewMockData.createMockTextMessage(
                text = "The deployment is complete!"
            )
        )
        
        CometChatConversationListItem(
            conversation = conversation,
            onItemClick = { }
        )
    }
}

/**
 * Preview showing a private group conversation.
 */
@Preview(showBackground = true, name = "Group Conversation - Private")
@Composable
fun PreviewGroupConversationPrivate() {
    CometChatTheme {
        val conversation = PreviewMockData.createGroupConversation(
            group = PreviewMockData.createMockGroup(
                name = "Project Alpha",
                groupType = CometChatConstants.GROUP_TYPE_PRIVATE
            ),
            lastMessage = PreviewMockData.createMockTextMessage(
                text = "Let's discuss the roadmap"
            )
        )
        
        CometChatConversationListItem(
            conversation = conversation,
            onItemClick = { }
        )
    }
}

/**
 * Preview showing a password-protected group conversation.
 */
@Preview(showBackground = true, name = "Group Conversation - Protected")
@Composable
fun PreviewGroupConversationProtected() {
    CometChatTheme {
        val conversation = PreviewMockData.createGroupConversation(
            group = PreviewMockData.createMockGroup(
                name = "VIP Lounge",
                groupType = CometChatConstants.GROUP_TYPE_PASSWORD
            ),
            lastMessage = PreviewMockData.createMockTextMessage(
                text = "Welcome to the exclusive group!"
            )
        )
        
        CometChatConversationListItem(
            conversation = conversation,
            onItemClick = { }
        )
    }
}

// ============================================================================
// Preview: Selection Mode
// ============================================================================

/**
 * Preview showing single selection mode with unselected item.
 */
@Preview(showBackground = true, name = "Selection Mode - Single Unselected")
@Composable
fun PreviewSelectionModeSingleUnselected() {
    CometChatTheme {
        val conversation = PreviewMockData.createUserConversation(
            user = PreviewMockData.createMockUser(name = "Charlie Brown")
        )
        
        CometChatConversationListItem(
            conversation = conversation,
            onItemClick = { },
            selectionMode = UIKitConstants.SelectionMode.SINGLE,
            isSelected = false
        )
    }
}

/**
 * Preview showing single selection mode with selected item.
 */
@Preview(showBackground = true, name = "Selection Mode - Single Selected")
@Composable
fun PreviewSelectionModeSingleSelected() {
    CometChatTheme {
        val conversation = PreviewMockData.createUserConversation(
            user = PreviewMockData.createMockUser(name = "Charlie Brown")
        )
        
        CometChatConversationListItem(
            conversation = conversation,
            onItemClick = { },
            selectionMode = UIKitConstants.SelectionMode.SINGLE,
            isSelected = true
        )
    }
}

/**
 * Preview showing multiple selection mode with mixed selection states.
 */
@Preview(showBackground = true, name = "Selection Mode - Multiple")
@Composable
fun PreviewSelectionModeMultiple() {
    CometChatTheme {
        Column {
            val conversation1 = PreviewMockData.createUserConversation(
                user = PreviewMockData.createMockUser(uid = "1", name = "Alice")
            )
            val conversation2 = PreviewMockData.createUserConversation(
                user = PreviewMockData.createMockUser(uid = "2", name = "Bob")
            )
            val conversation3 = PreviewMockData.createUserConversation(
                user = PreviewMockData.createMockUser(uid = "3", name = "Charlie")
            )
            
            CometChatConversationListItem(
                conversation = conversation1,
                onItemClick = { },
                selectionMode = UIKitConstants.SelectionMode.MULTIPLE,
                isSelected = true
            )
            HorizontalDivider()
            CometChatConversationListItem(
                conversation = conversation2,
                onItemClick = { },
                selectionMode = UIKitConstants.SelectionMode.MULTIPLE,
                isSelected = false
            )
            HorizontalDivider()
            CometChatConversationListItem(
                conversation = conversation3,
                onItemClick = { },
                selectionMode = UIKitConstants.SelectionMode.MULTIPLE,
                isSelected = true
            )
        }
    }
}


// ============================================================================
// Preview: Custom Views
// ============================================================================

/**
 * Preview showing custom title view.
 */
@Preview(showBackground = true, name = "Custom Title View")
@Composable
fun PreviewCustomTitleView() {
    CometChatTheme {
        val conversation = PreviewMockData.createUserConversation(
            user = PreviewMockData.createMockUser(name = "Custom Title User")
        )
        
        CometChatConversationListItem(
            conversation = conversation,
            onItemClick = { },
            titleView = { conv, _ ->
                Text(
                    text = "★ ${ConversationUtils.getConversationTitle(conv)}",
                    color = CometChatTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        )
    }
}

/**
 * Preview showing custom subtitle view.
 */
@Preview(showBackground = true, name = "Custom Subtitle View")
@Composable
fun PreviewCustomSubtitleView() {
    CometChatTheme {
        val conversation = PreviewMockData.createUserConversation(
            user = PreviewMockData.createMockUser(name = "Custom Subtitle User")
        )
        
        CometChatConversationListItem(
            conversation = conversation,
            onItemClick = { },
            subtitleView = { _, _ ->
                Text(
                    text = "🔔 Custom notification message",
                    color = CometChatTheme.colorScheme.warningColor
                )
            }
        )
    }
}

/**
 * Preview showing custom trailing view.
 */
@Preview(showBackground = true, name = "Custom Trailing View")
@Composable
fun PreviewCustomTrailingView() {
    CometChatTheme {
        val conversation = PreviewMockData.createUserConversation(
            user = PreviewMockData.createMockUser(name = "Custom Trailing User")
        )
        
        CometChatConversationListItem(
            conversation = conversation,
            onItemClick = { },
            trailingView = { _, _ ->
                Text(
                    text = "📌 Pinned",
                    color = CometChatTheme.colorScheme.primary,
                    modifier = Modifier.padding(4.dp)
                )
            }
        )
    }
}

/**
 * Preview showing all custom views.
 */
@Preview(showBackground = true, name = "All Custom Views")
@Composable
fun PreviewAllCustomViews() {
    CometChatTheme {
        val conversation = PreviewMockData.createUserConversation(
            user = PreviewMockData.createMockUser(name = "Fully Customized")
        )
        
        CometChatConversationListItem(
            conversation = conversation,
            onItemClick = { },
            leadingView = { _, _ ->
                Text(
                    text = "👤",
                    modifier = Modifier
                        .background(
                            color = CometChatTheme.colorScheme.backgroundColor3,
                            shape = CircleShape
                        )
                        .padding(12.dp)
                )
            },
            titleView = { conv, _ ->
                Text(
                    text = ConversationUtils.getConversationTitle(conv),
                    fontWeight = FontWeight.Bold,
                    color = CometChatTheme.colorScheme.primary
                )
            },
            subtitleView = { _, _ ->
                Text(
                    text = "Custom subtitle content",
                    color = CometChatTheme.colorScheme.textColorSecondary
                )
            },
            trailingView = { _, _ ->
                Text(
                    text = "Now",
                    color = CometChatTheme.colorScheme.textColorTertiary
                )
            }
        )
    }
}

// ============================================================================
// Preview: Unread Messages
// ============================================================================

/**
 * Preview showing conversation with unread messages.
 */
@Preview(showBackground = true, name = "Unread Messages - Single")
@Composable
fun PreviewUnreadMessagesSingle() {
    CometChatTheme {
        val conversation = PreviewMockData.createUserConversation(
            user = PreviewMockData.createMockUser(name = "New Message User"),
            lastMessage = PreviewMockData.createMockTextMessage(
                text = "You have a new message!"
            ),
            unreadCount = 1
        )
        
        CometChatConversationListItem(
            conversation = conversation,
            onItemClick = { }
        )
    }
}

/**
 * Preview showing conversation with multiple unread messages.
 */
@Preview(showBackground = true, name = "Unread Messages - Multiple")
@Composable
fun PreviewUnreadMessagesMultiple() {
    CometChatTheme {
        val conversation = PreviewMockData.createUserConversation(
            user = PreviewMockData.createMockUser(name = "Busy Chat User"),
            lastMessage = PreviewMockData.createMockTextMessage(
                text = "Lots of messages waiting for you!"
            ),
            unreadCount = 25
        )
        
        CometChatConversationListItem(
            conversation = conversation,
            onItemClick = { }
        )
    }
}

/**
 * Preview showing conversation with high unread count.
 */
@Preview(showBackground = true, name = "Unread Messages - High Count")
@Composable
fun PreviewUnreadMessagesHighCount() {
    CometChatTheme {
        val conversation = PreviewMockData.createGroupConversation(
            group = PreviewMockData.createMockGroup(name = "Very Active Group"),
            lastMessage = PreviewMockData.createMockTextMessage(
                text = "This group is very active!"
            ),
            unreadCount = 999
        )
        
        CometChatConversationListItem(
            conversation = conversation,
            onItemClick = { }
        )
    }
}

/**
 * Preview showing multiple conversations with varying unread counts.
 */
@Preview(showBackground = true, name = "Unread Messages - List")
@Composable
fun PreviewUnreadMessagesList() {
    CometChatTheme {
        Column {
            listOf(0, 1, 5, 99).forEachIndexed { index, count ->
                val conversation = PreviewMockData.createUserConversation(
                    user = PreviewMockData.createMockUser(
                        uid = "user_$index",
                        name = "User ${index + 1}"
                    ),
                    unreadCount = count
                )
                
                CometChatConversationListItem(
                    conversation = conversation,
                    onItemClick = { }
                )
                if (index < 3) {
                    HorizontalDivider()
                }
            }
        }
    }
}

// ============================================================================
// Preview: Typing Indicator
// ============================================================================

/**
 * Preview showing typing indicator with single user.
 */
@Preview(showBackground = true, name = "Typing Indicator - Single User")
@Composable
fun PreviewTypingIndicatorSingleUser() {
    CometChatTheme {
        val typingUser = PreviewMockData.createMockUser(name = "Alice")
        val conversation = PreviewMockData.createUserConversation(
            user = typingUser
        )
        
        CometChatConversationListItem(
            conversation = conversation,
            onItemClick = { },
            typingIndicator = TypingIndicator(
                typingUsers = listOf(typingUser),
                isTyping = true
            )
        )
    }
}

/**
 * Preview showing typing indicator with multiple users.
 */
@Preview(showBackground = true, name = "Typing Indicator - Multiple Users")
@Composable
fun PreviewTypingIndicatorMultipleUsers() {
    CometChatTheme {
        val conversation = PreviewMockData.createGroupConversation(
            group = PreviewMockData.createMockGroup(name = "Team Discussion")
        )
        
        CometChatConversationListItem(
            conversation = conversation,
            onItemClick = { },
            typingIndicator = TypingIndicator(
                typingUsers = listOf(
                    PreviewMockData.createMockUser(uid = "1", name = "Alice"),
                    PreviewMockData.createMockUser(uid = "2", name = "Bob"),
                    PreviewMockData.createMockUser(uid = "3", name = "Charlie")
                ),
                isTyping = true
            )
        )
    }
}

// ============================================================================
// Preview: Combined States
// ============================================================================

/**
 * Preview showing a comprehensive list with various states.
 */
@Preview(showBackground = true, name = "Comprehensive List")
@Composable
fun PreviewComprehensiveList() {
    CometChatTheme {
        Column(
            modifier = Modifier.background(CometChatTheme.colorScheme.backgroundColor1)
        ) {
            // User conversation with unread messages
            CometChatConversationListItem(
                conversation = PreviewMockData.createUserConversation(
                    user = PreviewMockData.createMockUser(
                        name = "Alice Smith",
                        status = CometChatConstants.USER_STATUS_ONLINE
                    ),
                    lastMessage = PreviewMockData.createMockTextMessage(text = "Hey there!"),
                    unreadCount = 3
                ),
                onItemClick = { }
            )
            HorizontalDivider(color = CometChatTheme.colorScheme.strokeColorLight)
            
            // Group conversation with typing indicator
            CometChatConversationListItem(
                conversation = PreviewMockData.createGroupConversation(
                    group = PreviewMockData.createMockGroup(
                        name = "Engineering Team",
                        groupType = CometChatConstants.GROUP_TYPE_PRIVATE
                    )
                ),
                onItemClick = { },
                typingIndicator = TypingIndicator(
                    typingUsers = listOf(PreviewMockData.createMockUser(name = "Bob")),
                    isTyping = true
                )
            )
            HorizontalDivider(color = CometChatTheme.colorScheme.strokeColorLight)
            
            // Selected conversation
            CometChatConversationListItem(
                conversation = PreviewMockData.createUserConversation(
                    user = PreviewMockData.createMockUser(
                        name = "Charlie Brown",
                        status = CometChatConstants.USER_STATUS_OFFLINE
                    )
                ),
                onItemClick = { },
                selectionMode = UIKitConstants.SelectionMode.MULTIPLE,
                isSelected = true
            )
            HorizontalDivider(color = CometChatTheme.colorScheme.strokeColorLight)
            
            // Unselected conversation in selection mode
            CometChatConversationListItem(
                conversation = PreviewMockData.createGroupConversation(
                    group = PreviewMockData.createMockGroup(
                        name = "Public Group",
                        groupType = CometChatConstants.GROUP_TYPE_PUBLIC
                    ),
                    unreadCount = 15
                ),
                onItemClick = { },
                selectionMode = UIKitConstants.SelectionMode.MULTIPLE,
                isSelected = false
            )
        }
    }
}

/**
 * Preview showing dark theme appearance (simulated with custom colors).
 */
@Preview(showBackground = true, backgroundColor = 0xFF1A1A1A, name = "Dark Theme Simulation")
@Composable
fun PreviewDarkThemeSimulation() {
    CometChatTheme {
        val conversation = PreviewMockData.createUserConversation(
            user = PreviewMockData.createMockUser(name = "Dark Theme User"),
            lastMessage = PreviewMockData.createMockTextMessage(
                text = "Testing dark theme appearance"
            ),
            unreadCount = 5
        )
        
        CometChatConversationListItem(
            conversation = conversation,
            onItemClick = { },
            style = CometChatConversationListItemStyle.default(
                backgroundColor = Color(0xFF1A1A1A),
                titleTextColor = Color.White,
                subtitleTextColor = Color(0xFFB0B0B0)
            )
        )
    }
}
