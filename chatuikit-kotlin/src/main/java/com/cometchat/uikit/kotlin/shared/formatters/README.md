# CometChat Mentions Feature - View-based (chatuikit-kotlin)

This module provides @mention functionality for the View-based Android UI Kit.

## Overview

The mentions feature allows users to @mention other users or group members in the message composer. When a user types `@`, a suggestion list appears showing matching users/group members. Selecting a mention creates a styled, non-editable span in the text.

## Components

### CometChatMentionsFormatter

The main formatter class that handles mention detection, search, and formatting.

```kotlin
// Create a mentions formatter
val mentionsFormatter = CometChatMentionsFormatter(context)

// Configure mention behavior
mentionsFormatter.setMentionLimit(10)
mentionsFormatter.setMentionsType(UIKitConstants.MentionsType.USERS_AND_GROUP_MEMBERS)
mentionsFormatter.setDisableMentionAll(false)

// Set click handlers
mentionsFormatter.setOnMentionClick { context, user ->
    // Handle mention click in message bubbles
    showUserProfile(user)
}

mentionsFormatter.setOnMentionAllClick {
    // Handle @all mention click
}
```

### Style Configuration

#### Using Style Resources (Recommended)

```kotlin
// Apply styles from XML resources
mentionsFormatter.setMessageComposerMentionTextStyle(context, R.style.CometChatMessageComposerMentionsStyle)
mentionsFormatter.setIncomingBubbleMentionTextStyle(context, R.style.CometChatIncomingBubbleMentionsStyle)
mentionsFormatter.setOutgoingBubbleMentionTextStyle(context, R.style.CometChatOutgoingBubbleMentionsStyle)
mentionsFormatter.setConversationsMentionTextStyle(context, R.style.CometChatConversationsMentionsStyle)
```

#### Using CometChatMentionStyle

```kotlin
val mentionStyle = CometChatMentionStyle(
    mentionTextColor = Color.BLUE,
    mentionBackgroundColor = Color.LTGRAY,
    selfMentionTextColor = Color.RED,
    selfMentionBackgroundColor = Color.YELLOW
)
mentionsFormatter.setMentionStyle(mentionStyle)
```

#### Using PromptTextStyle (Granular Control)

```kotlin
val tagStyle = PromptTextStyle()
    .setColor(Color.BLUE)
    .setBackgroundColor(Color.argb(51, 0, 0, 255)) // 20% alpha
    .setTextAppearance(Typeface.DEFAULT_BOLD)

mentionsFormatter.setTagStyle(tagStyle)
mentionsFormatter.setSelfTagStyle(tagStyle)
```

### Custom Search Builders

```kotlin
// Custom user search
mentionsFormatter.setUsersRequestBuilder(
    UsersRequest.UsersRequestBuilder()
        .setLimit(20)
        .setRoles(listOf("admin", "moderator"))
)

// Custom group member search
mentionsFormatter.setGroupMembersRequestBuilder { group ->
    GroupMembersRequest.GroupMembersRequestBuilder(group.guid)
        .setLimit(20)
        .setScopes(listOf("admin", "moderator"))
}
```

### Mention All Configuration

```kotlin
// Customize "Mention All" feature
mentionsFormatter.setMentionAllLabel("everyone", "Notify Everyone")
mentionsFormatter.setMentionAllInfoText("This will notify all members in the group")
mentionsFormatter.setDisableMentionAll(false) // Enable @all mentions
```

## CometChatSuggestionList

A custom view that displays suggestion items for mentions.

### XML Usage

```xml
<com.cometchat.uikit.kotlin.presentation.shared.suggestionlist.CometChatSuggestionList
    android:id="@+id/suggestionList"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:cometchatSuggestionListStyle="@style/CometChatSuggestionListStyle" />
```

### Programmatic Usage

```kotlin
val suggestionList = CometChatSuggestionList(context)

// Configure appearance
suggestionList.setMaxHeightLimit(250.dp)
suggestionList.showAvatar(true)
suggestionList.setSuggestionListBackgroundColor(Color.WHITE)
suggestionList.setSuggestionListCornerRadius(8.dp)

// Set listeners
suggestionList.setItemClickListener(object : OnItemClickListener<SuggestionItem> {
    override fun OnItemClick(item: SuggestionItem, position: Int) {
        // Handle selection
    }
    
    override fun OnItemLongClick(item: SuggestionItem, position: Int) {
        // Handle long press
    }
})

suggestionList.setOnScrollToBottomListener {
    // Load more suggestions (pagination)
}

// Update suggestions
suggestionList.setList(suggestions)

// Show/hide loading state
suggestionList.showShimmer(true)
```

### Style Configuration

```kotlin
val style = CometChatSuggestionListStyle(
    backgroundColor = Color.WHITE,
    strokeColor = Color.LTGRAY,
    strokeWidth = 1.dp,
    cornerRadius = 8.dp,
    maxHeight = 250.dp,
    itemTextColor = Color.BLACK,
    itemInfoTextColor = Color.GRAY
)
suggestionList.setStyle(style)
```

## Integration with Message Composer

The mentions feature integrates automatically with `CometChatMessageComposer`:

```kotlin
val messageComposer = CometChatMessageComposer(context)

// Set text formatters (includes mentions formatter)
val formatters = listOf(mentionsFormatter)
messageComposer.setTextFormatters(formatters)

// Set mention text style
messageComposer.setMentionTextStyle(R.style.CometChatMessageComposerMentionsStyle)
```

## Integration with Message List

```kotlin
val messageList = CometChatMessageList(context)

// Configure mentions
messageList.setDisableMentionAll(false)
messageList.setMentionAllLabelId("all", "Notify All")

// Get mentions formatter for click handling
val mentionsFormatter = messageList.getMentionsFormatter()
mentionsFormatter?.setOnMentionClick { context, user ->
    // Navigate to user profile
}
```

## Mention Format

Mentions are stored in the following format:
- User mentions: `<@uid:userId>` → displayed as `@userName`
- Mention all: `<@all:all>` → displayed as `@Notify All`

## Parameters Reference

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| trackingCharacter | Char | '@' | Character that triggers mention |
| mentionLimit | Int | 10 | Max mentions per message |
| mentionType | MentionsType | USERS_AND_GROUP_MEMBERS | Who can be mentioned |
| mentionsVisibility | MentionsVisibility | BOTH | Where mentions are enabled |
| disableMentionAll | Boolean | false | Disable @all mention |
| mentionAllId | String | "all" | ID for mention all |
| mentionAllLabelText | String | "Notify All" | Display text for mention all |

## Callbacks

| Callback | Description |
|----------|-------------|
| onMentionClick | Called when a mention is clicked in message bubbles |
| onMentionAllClick | Called when @all is clicked |
| search | Called when query changes for suggestions |
| onItemClick | Called when a suggestion is selected |
| onScrollToBottom | Called for pagination |
| observeSelectionList | Called when selected mentions change |
