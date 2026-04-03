# CometChat Mentions Feature - Jetpack Compose (chatuikit-jetpack)

This module provides @mention functionality for the Jetpack Compose UI Kit.

## Overview

The mentions feature allows users to @mention other users or group members in the message composer. The Compose implementation uses `AnnotatedString` and state management patterns native to Jetpack Compose.

## Components

### MentionTextFieldState

State holder for managing mention-aware text input.

```kotlin
// Create state
val mentionState = rememberMentionTextFieldState()

// Or with initial text
val mentionState = rememberMentionTextFieldState(initialText = "Hello ")

// Access current text
val currentText = mentionState.textFieldValue.text

// Get processed text for sending (with underlying mention format)
val processedText = mentionState.getProcessedText()

// Get all selected mentions
val mentions = mentionState.getMentions()

// Clear all state
mentionState.clear()
```

### MentionAwareTextField

A text field composable with built-in mention support.

```kotlin
@Composable
fun MessageInput() {
    val mentionState = rememberMentionTextFieldState()
    
    MentionAwareTextField(
        state = mentionState,
        onValueChange = { newValue ->
            // Handle text changes
        },
        modifier = Modifier.fillMaxWidth(),
        textStyle = MaterialTheme.typography.bodyLarge,
        mentionStyle = SpanStyle(
            color = MaterialTheme.colorScheme.primary,
            background = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        ),
        selfMentionStyle = SpanStyle(
            color = MaterialTheme.colorScheme.secondary,
            background = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
        ),
        loggedInUserId = currentUserId,
        onMentionDeleted = { deletedMention ->
            // Handle mention deletion
        },
        onCursorPositionChanged = { position ->
            // Handle cursor position changes for mention detection
        }
    )
}
```

### MentionText

Composable for displaying text with styled and clickable mentions.

```kotlin
// Basic usage with raw text
@Composable
fun MessageBubble(message: TextMessage) {
    MentionText(
        text = message.text,
        mentionedUsers = message.mentionedUsers ?: emptyList(),
        onMentionClick = { user ->
            // Navigate to user profile
        },
        onMentionAllClick = {
            // Handle @all click
        },
        style = MentionTextStyle.incomingBubble()
    )
}

// Using BaseMessage directly
@Composable
fun MessageBubble(message: BaseMessage) {
    MentionText(
        message = message,
        onMentionClick = { user -> showUserProfile(user) },
        displayContext = MentionDisplayContext.INCOMING_BUBBLE
    )
}

// With pre-built AnnotatedString
@Composable
fun CustomMentionDisplay(annotatedText: AnnotatedString) {
    MentionText(
        annotatedText = annotatedText,
        onMentionClick = { userId -> handleMentionClick(userId) }
    )
}
```

### MentionTextStyle

Style configuration for mention text display.

```kotlin
// Use predefined styles
val incomingStyle = MentionTextStyle.incomingBubble()
val outgoingStyle = MentionTextStyle.outgoingBubble()
val conversationStyle = MentionTextStyle.conversationPreview()

// Or create custom style
val customStyle = MentionTextStyle(
    mentionTextColor = Color.Blue,
    mentionBackgroundColor = Color.Blue.copy(alpha = 0.2f),
    selfMentionTextColor = Color.Red,
    selfMentionBackgroundColor = Color.Red.copy(alpha = 0.2f),
    mentionFontWeight = FontWeight.SemiBold
)

// Context-based style selection
val style = MentionTextStyle.forContext(MentionDisplayContext.OUTGOING_BUBBLE)
```

### MentionDisplayContext

Enum for different display contexts with appropriate default styling.

```kotlin
enum class MentionDisplayContext {
    INCOMING_BUBBLE,    // Messages from other users
    OUTGOING_BUBBLE,    // Messages sent by current user
    CONVERSATION_PREVIEW, // Conversation list subtitle
    DEFAULT             // Generic context
}
```

## Mention Detection

The `MentionDetection.kt` file provides utilities for detecting mention triggers.

```kotlin
// Detect if mention is active
val mentionState = detectMention(
    text = textFieldValue.text,
    cursorPosition = textFieldValue.selection.start,
    trackingCharacter = '@'
)

if (mentionState.isActive) {
    // Show suggestion list
    val query = mentionState.query
    searchSuggestions(query)
}
```

### ComposeMentionState

```kotlin
data class ComposeMentionState(
    val isActive: Boolean,      // Whether mention mode is active
    val query: String,          // Search query after @
    val triggerIndex: Int,      // Position of @ character
    val cursorPosition: Int     // Current cursor position
)
```

## Mention Insertion

```kotlin
// Insert a mention into the text field state
insertMentionIntoState(
    state = mentionState,
    mentionState = detectedMention,
    id = user.uid,
    name = user.name,
    promptText = "@${user.name}",
    underlyingText = "<@uid:${user.uid}>"
)
```

## CometChatSuggestionList (Compose)

A composable for displaying suggestion items.

```kotlin
@Composable
fun SuggestionOverlay(
    suggestions: List<SuggestionItem>,
    isLoading: Boolean,
    onItemClick: (SuggestionItem) -> Unit
) {
    CometChatSuggestionList(
        suggestions = suggestions,
        isLoading = isLoading,
        style = CometChatSuggestionListStyle.default(),
        showAvatar = true,
        onItemClick = onItemClick,
        onScrollToBottom = {
            // Load more suggestions
        }
    )
}
```

### Custom Item View

```kotlin
CometChatSuggestionList(
    suggestions = suggestions,
    itemView = { suggestion ->
        // Custom item composable
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = suggestion.leadingIconUrl,
                contentDescription = null,
                modifier = Modifier.size(40.dp).clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = suggestion.name)
        }
    },
    onItemClick = { suggestion -> handleSelection(suggestion) }
)
```

### Style Configuration

```kotlin
val style = CometChatSuggestionListStyle(
    backgroundColor = Color.White,
    strokeColor = Color.LightGray,
    strokeWidth = 1.dp,
    cornerRadius = 8.dp,
    maxHeight = 250.dp,
    itemTextColor = Color.Black,
    itemTextStyle = MaterialTheme.typography.bodyMedium,
    itemInfoTextColor = Color.Gray,
    itemInfoTextStyle = MaterialTheme.typography.bodySmall,
    separatorColor = Color.LightGray,
    separatorHeight = 1.dp,
    shimmerBaseColor = Color.LightGray,
    shimmerHighlightColor = Color.White
)
```

## CometChatMentionsFormatter (Compose)

The Compose version of the mentions formatter.

```kotlin
val mentionsFormatter = CometChatMentionsFormatter(context)

// Configure
mentionsFormatter.setMentionLimit(10)
mentionsFormatter.setGroup(group)

// Search for suggestions
mentionsFormatter.search(context, query)

// Observe suggestions
mentionsFormatter.suggestionItemList.observe(lifecycleOwner) { suggestions ->
    // Update UI
}

// Handle message send
mentionsFormatter.handlePreMessageSend(context, message)
```

## Complete Integration Example

```kotlin
@Composable
fun MentionEnabledComposer(
    group: Group?,
    onSendMessage: (String, List<User>) -> Unit
) {
    val mentionState = rememberMentionTextFieldState()
    var detectedMention by remember { mutableStateOf(ComposeMentionState()) }
    var suggestions by remember { mutableStateOf<List<SuggestionItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    
    val mentionsFormatter = remember { CometChatMentionsFormatter(LocalContext.current) }
    
    LaunchedEffect(group) {
        mentionsFormatter.setGroup(group)
    }
    
    Column {
        // Suggestion list (shown above composer)
        if (detectedMention.isActive) {
            CometChatSuggestionList(
                suggestions = suggestions,
                isLoading = isLoading,
                onItemClick = { suggestion ->
                    insertMentionIntoState(
                        state = mentionState,
                        mentionState = detectedMention,
                        id = suggestion.id,
                        name = suggestion.name,
                        promptText = suggestion.promptText,
                        underlyingText = suggestion.underlyingText
                    )
                    detectedMention = ComposeMentionState() // Reset
                }
            )
        }
        
        // Text input
        MentionAwareTextField(
            state = mentionState,
            onValueChange = { newValue ->
                // Detect mentions
                detectedMention = detectMention(
                    text = newValue.text,
                    cursorPosition = newValue.selection.start,
                    trackingCharacter = '@'
                )
                
                if (detectedMention.isActive) {
                    isLoading = true
                    mentionsFormatter.search(context, detectedMention.query)
                }
            },
            mentionStyle = SpanStyle(
                color = MaterialTheme.colorScheme.primary,
                background = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            )
        )
        
        // Send button
        IconButton(
            onClick = {
                val processedText = mentionState.getProcessedText()
                val mentionedUsers = mentionState.getMentions().mapNotNull { 
                    // Convert to User objects
                }
                onSendMessage(processedText, mentionedUsers)
                mentionState.clear()
            }
        ) {
            Icon(Icons.Default.Send, contentDescription = "Send")
        }
    }
}
```

## Extension Functions

```kotlin
// Check if message has mentions
if (message.hasMentions()) {
    // Show mention indicator
}

// Check if current user is mentioned
if (message.mentionsCurrentUser()) {
    // Highlight message
}
```

## Mention Format

Mentions are stored in the following format:
- User mentions: `<@uid:userId>` → displayed as `@userName`
- Mention all: `<@all:all>` → displayed as `@Notify All`

## Parameters Reference

### MentionAwareTextField

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| state | MentionTextFieldState | required | State holder for text and mentions |
| onValueChange | (TextFieldValue) -> Unit | required | Callback for text changes |
| mentionStyle | SpanStyle | Blue with 20% alpha bg | Style for mentions |
| selfMentionStyle | SpanStyle? | null | Style for self-mentions |
| loggedInUserId | String? | null | User ID for self-mention detection |
| onMentionDeleted | ((SelectedMention) -> Unit)? | null | Callback when mention deleted |
| onCursorPositionChanged | ((Int) -> Unit)? | null | Callback for cursor changes |

### MentionText

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| text | String | required | Raw message text |
| mentionedUsers | List<User> | required | Users mentioned in message |
| onMentionClick | (User) -> Unit | required | Callback for mention clicks |
| style | MentionTextStyle | incomingBubble() | Style configuration |
| mentionAllLabel | String | "all" | Display label for @all |
| trackingCharacter | Char | '@' | Mention trigger character |
