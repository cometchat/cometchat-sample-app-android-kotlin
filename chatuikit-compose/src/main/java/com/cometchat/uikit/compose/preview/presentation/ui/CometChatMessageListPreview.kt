package com.cometchat.uikit.compose.preview.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.models.TextMessage
import com.cometchat.uikit.compose.presentation.messagelist.style.CometChatMessageListStyle
import com.cometchat.uikit.compose.presentation.shared.defaultstates.CometChatEmptyState
import com.cometchat.uikit.compose.presentation.shared.defaultstates.CometChatErrorState
import com.cometchat.uikit.compose.presentation.shared.defaultstates.CometChatLoadingState
import com.cometchat.uikit.compose.preview.domain.PreviewMockData
import com.cometchat.uikit.compose.theme.CometChatTheme

/**
 * NOTE: CometChatMessageList cannot be used directly in @Preview because it
 * internally calls CometChatUIKit.getLoggedInUser(), CometChatMentionsFormatter,
 * CometChatAIStreamService, and other SDK-dependent code during composition.
 *
 * These previews simulate the MessageList appearance using basic Compose primitives
 * to demonstrate the visual layout, states, and styling options.
 */

// ============================================================================
// Helper: Simulated Message Bubble
// ============================================================================

@Composable
private fun SimulatedMessageBubble(
    text: String,
    senderName: String,
    isOutgoing: Boolean,
    time: String = "12:30 PM",
    style: CometChatMessageListStyle = CometChatMessageListStyle.default()
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = if (isOutgoing) Arrangement.End else Arrangement.Start
    ) {
        if (!isOutgoing) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(CometChatTheme.colorScheme.extendedPrimaryColor500),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = senderName.take(1).uppercase(),
                    color = Color.White,
                    style = CometChatTheme.typography.caption1Bold
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            modifier = Modifier.widthIn(max = 260.dp)
        ) {
            if (!isOutgoing) {
                Text(
                    text = senderName,
                    style = CometChatTheme.typography.caption2Medium,
                    color = CometChatTheme.colorScheme.textColorSecondary,
                    modifier = Modifier.padding(start = 12.dp, bottom = 2.dp)
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(
                        topStart = if (isOutgoing) 12.dp else 4.dp,
                        topEnd = if (isOutgoing) 4.dp else 12.dp,
                        bottomStart = 12.dp,
                        bottomEnd = 12.dp
                    ))
                    .background(
                        if (isOutgoing) CometChatTheme.colorScheme.primary.copy(alpha = 0.1f)
                        else CometChatTheme.colorScheme.backgroundColor3
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Column {
                    Text(
                        text = text,
                        style = CometChatTheme.typography.bodyRegular,
                        color = CometChatTheme.colorScheme.textColorPrimary
                    )
                    Text(
                        text = time,
                        style = CometChatTheme.typography.caption2Regular,
                        color = CometChatTheme.colorScheme.textColorTertiary,
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

// ============================================================================
// Helper: Simulated Date Separator
// ============================================================================

@Composable
private fun SimulatedDateSeparator(date: String = "Today") {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = date,
            style = CometChatTheme.typography.caption2Medium,
            color = CometChatTheme.colorScheme.textColorTertiary,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(CometChatTheme.colorScheme.backgroundColor3)
                .padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}

// ============================================================================
// SECTION 1: CONTENT STATE PREVIEWS
// ============================================================================

/**
 * Preview showing a simulated message list with user conversation.
 */
@Preview(showBackground = true, name = "MessageList - Content - User Conversation")
@Composable
fun PreviewMessageListUserConversation() {
    CometChatTheme {
        val style = CometChatMessageListStyle.default()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(style.backgroundColor)
        ) {
            SimulatedDateSeparator("Today")
            SimulatedMessageBubble("Hey! How are you?", "Alice Smith", isOutgoing = false, time = "10:30 AM")
            SimulatedMessageBubble("I'm doing great, thanks! How about you?", "Me", isOutgoing = true, time = "10:31 AM")
            SimulatedMessageBubble("Pretty good! Working on the new feature.", "Alice Smith", isOutgoing = false, time = "10:32 AM")
            SimulatedMessageBubble("That sounds exciting! Need any help?", "Me", isOutgoing = true, time = "10:33 AM")
            SimulatedMessageBubble("Sure, let's discuss in the meeting tomorrow.", "Alice Smith", isOutgoing = false, time = "10:34 AM")
            SimulatedMessageBubble("Sounds good! See you then 👍", "Me", isOutgoing = true, time = "10:35 AM")
        }
    }
}

/**
 * Preview showing a simulated message list with group conversation.
 */
@Preview(showBackground = true, name = "MessageList - Content - Group Conversation")
@Composable
fun PreviewMessageListGroupConversation() {
    CometChatTheme {
        val style = CometChatMessageListStyle.default()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(style.backgroundColor)
        ) {
            SimulatedDateSeparator("Yesterday")
            SimulatedMessageBubble("The deployment is complete!", "Alice Smith", isOutgoing = false, time = "3:00 PM")
            SimulatedMessageBubble("Great work team! 🎉", "Bob Johnson", isOutgoing = false, time = "3:01 PM")
            SimulatedMessageBubble("Thanks everyone for the effort!", "Me", isOutgoing = true, time = "3:02 PM")
            SimulatedDateSeparator("Today")
            SimulatedMessageBubble("Any issues reported overnight?", "Charlie Brown", isOutgoing = false, time = "9:00 AM")
            SimulatedMessageBubble("All clear! No incidents.", "Me", isOutgoing = true, time = "9:05 AM")
        }
    }
}

// ============================================================================
// SECTION 2: UI STATE PREVIEWS
// ============================================================================

/**
 * Preview showing the loading state.
 */
@Preview(showBackground = true, name = "MessageList - State - Loading")
@Composable
fun PreviewMessageListLoading() {
    CometChatTheme {
        val style = CometChatMessageListStyle.default()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(style.backgroundColor)
        ) {
            CometChatLoadingState()
        }
    }
}

/**
 * Preview showing the empty state.
 */
@Preview(showBackground = true, name = "MessageList - State - Empty")
@Composable
fun PreviewMessageListEmpty() {
    CometChatTheme {
        val style = CometChatMessageListStyle.default()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(style.backgroundColor)
        ) {
            CometChatEmptyState(
                title = "No Messages",
                subtitle = "Start a conversation by sending a message"
            )
        }
    }
}

/**
 * Preview showing the error state.
 */
@Preview(showBackground = true, name = "MessageList - State - Error")
@Composable
fun PreviewMessageListError() {
    CometChatTheme {
        val style = CometChatMessageListStyle.default()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(style.backgroundColor)
        ) {
            CometChatErrorState(
                title = "Failed to load messages",
                subtitle = "Please check your connection and try again.",
                onRetry = { }
            )
        }
    }
}

// ============================================================================
// SECTION 3: VISIBILITY PREVIEWS
// ============================================================================

/**
 * Preview showing messages without avatars.
 */
@Preview(showBackground = true, name = "MessageList - Visibility - No Avatar")
@Composable
fun PreviewMessageListNoAvatar() {
    CometChatTheme {
        val style = CometChatMessageListStyle.default()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(style.backgroundColor)
        ) {
            SimulatedDateSeparator()
            // Without avatar — just bubbles aligned left/right
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                Box(
                    modifier = Modifier
                        .widthIn(max = 260.dp)
                        .clip(RoundedCornerShape(4.dp, 12.dp, 12.dp, 12.dp))
                        .background(CometChatTheme.colorScheme.backgroundColor3)
                        .padding(12.dp)
                ) {
                    Text("Message without avatar", style = CometChatTheme.typography.bodyRegular)
                }
            }
            SimulatedMessageBubble("Reply without avatar", "Me", isOutgoing = true)
        }
    }
}

/**
 * Preview showing messages without date separator.
 */
@Preview(showBackground = true, name = "MessageList - Visibility - No Date Separator")
@Composable
fun PreviewMessageListNoDateSeparator() {
    CometChatTheme {
        val style = CometChatMessageListStyle.default()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(style.backgroundColor)
        ) {
            // No date separator between messages
            SimulatedMessageBubble("First message", "Alice", isOutgoing = false)
            SimulatedMessageBubble("Second message", "Me", isOutgoing = true)
            SimulatedMessageBubble("Third message", "Alice", isOutgoing = false)
        }
    }
}

// ============================================================================
// SECTION 4: ALIGNMENT PREVIEWS
// ============================================================================

/**
 * Preview showing standard alignment (outgoing right, incoming left).
 */
@Preview(showBackground = true, name = "MessageList - Alignment - Standard")
@Composable
fun PreviewMessageListStandardAlignment() {
    CometChatTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(CometChatMessageListStyle.default().backgroundColor)
        ) {
            SimulatedDateSeparator()
            SimulatedMessageBubble("Incoming message (left)", "Alice", isOutgoing = false)
            SimulatedMessageBubble("Outgoing message (right)", "Me", isOutgoing = true)
            SimulatedMessageBubble("Another incoming", "Alice", isOutgoing = false)
        }
    }
}

/**
 * Preview showing left-aligned messages (all messages on left).
 */
@Preview(showBackground = true, name = "MessageList - Alignment - Left Aligned")
@Composable
fun PreviewMessageListLeftAligned() {
    CometChatTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(CometChatMessageListStyle.default().backgroundColor)
        ) {
            SimulatedDateSeparator()
            SimulatedMessageBubble("Incoming message", "Alice", isOutgoing = false)
            SimulatedMessageBubble("Outgoing message (also left)", "Me", isOutgoing = false)
            SimulatedMessageBubble("Another incoming", "Alice", isOutgoing = false)
        }
    }
}

// ============================================================================
// SECTION 5: STYLE CUSTOMIZATION PREVIEWS
// ============================================================================

/**
 * Preview with custom background color.
 */
@Preview(showBackground = true, name = "MessageList - Style - Custom Background")
@Composable
fun PreviewMessageListCustomBackground() {
    CometChatTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF0F4FF))
        ) {
            SimulatedDateSeparator()
            SimulatedMessageBubble("Message on custom background", "Alice", isOutgoing = false)
            SimulatedMessageBubble("Reply on custom background", "Me", isOutgoing = true)
        }
    }
}

// ============================================================================
// SECTION 6: COMPREHENSIVE PREVIEWS
// ============================================================================

/**
 * Preview showing a full chat context (header + messages + composer).
 */
@Preview(showBackground = true, name = "MessageList - Comprehensive - Full Chat")
@Composable
fun PreviewMessageListFullChat() {
    CometChatTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(CometChatMessageListStyle.default().backgroundColor)
        ) {
            // Simulated header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CometChatTheme.colorScheme.backgroundColor1)
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(CometChatTheme.colorScheme.extendedPrimaryColor500),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("AS", color = Color.White, style = CometChatTheme.typography.caption1Bold)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Alice Smith", style = CometChatTheme.typography.heading4Medium, color = CometChatTheme.colorScheme.textColorPrimary)
                        Text("Online", style = CometChatTheme.typography.caption1Regular, color = CometChatTheme.colorScheme.successColor)
                    }
                }
            }

            // Messages
            Column(modifier = Modifier.weight(1f)) {
                SimulatedDateSeparator()
                SimulatedMessageBubble("Hey! How are you?", "Alice Smith", isOutgoing = false, time = "10:30 AM")
                SimulatedMessageBubble("I'm great! Working on the new feature.", "Me", isOutgoing = true, time = "10:31 AM")
                SimulatedMessageBubble("That's awesome! Let me know if you need help.", "Alice Smith", isOutgoing = false, time = "10:32 AM")
                SimulatedMessageBubble("Will do, thanks! 👍", "Me", isOutgoing = true, time = "10:33 AM")
            }

            // Simulated composer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CometChatTheme.colorScheme.backgroundColor1)
                    .padding(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(CometChatTheme.colorScheme.backgroundColor3)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "Type a message...",
                        style = CometChatTheme.typography.bodyRegular,
                        color = CometChatTheme.colorScheme.textColorTertiary
                    )
                }
            }
        }
    }
}

/**
 * Preview showing a thread conversation.
 */
@Preview(showBackground = true, name = "MessageList - Comprehensive - Thread")
@Composable
fun PreviewMessageListThread() {
    CometChatTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(CometChatMessageListStyle.default().backgroundColor)
        ) {
            // Parent message indicator
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CometChatTheme.colorScheme.backgroundColor3)
                    .padding(12.dp)
            ) {
                Text(
                    text = "Thread: \"Let's discuss the roadmap\"",
                    style = CometChatTheme.typography.caption1Medium,
                    color = CometChatTheme.colorScheme.textColorSecondary
                )
            }
            // Thread replies
            SimulatedMessageBubble("I think we should prioritize the API.", "Alice", isOutgoing = false, time = "2:00 PM")
            SimulatedMessageBubble("Agreed. Let's also add caching.", "Me", isOutgoing = true, time = "2:01 PM")
            SimulatedMessageBubble("Good idea! I'll create the tickets.", "Bob", isOutgoing = false, time = "2:02 PM")
        }
    }
}
