package com.cometchat.uikit.compose.presentation.shared.mentions

import androidx.compose.foundation.text.ClickableText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.TextMessage
import com.cometchat.chat.models.User
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.uikit.core.CometChatUIKit

/**
 * Enum representing the context in which mentions are displayed.
 * Different contexts have different default styling.
 */
enum class MentionDisplayContext {
    /** Incoming message bubble (messages from other users) */
    INCOMING_BUBBLE,
    /** Outgoing message bubble (messages sent by current user) */
    OUTGOING_BUBBLE,
    /** Conversation list subtitle preview */
    CONVERSATION_PREVIEW,
    /** Default/generic context */
    DEFAULT
}

/**
 * Style configuration for mention text display.
 *
 * This data class defines the visual appearance of mentions in different contexts.
 * Use the companion object factory methods for predefined styles, or create custom
 * configurations for specific use cases.
 *
 * ## Predefined Styles
 *
 * ```kotlin
 * // For incoming message bubbles (messages from other users)
 * val incomingStyle = MentionTextStyle.incomingBubble()
 *
 * // For outgoing message bubbles (messages sent by current user)
 * val outgoingStyle = MentionTextStyle.outgoingBubble()
 *
 * // For conversation list previews
 * val conversationStyle = MentionTextStyle.conversationPreview()
 *
 * // Context-based selection
 * val style = MentionTextStyle.forContext(MentionDisplayContext.OUTGOING_BUBBLE)
 * ```
 *
 * ## Custom Style
 *
 * ```kotlin
 * val customStyle = MentionTextStyle(
 *     mentionTextColor = Color.Blue,
 *     mentionBackgroundColor = Color.Blue.copy(alpha = 0.2f),
 *     selfMentionTextColor = Color.Red,
 *     selfMentionBackgroundColor = Color.Red.copy(alpha = 0.2f),
 *     mentionFontWeight = FontWeight.SemiBold
 * )
 * ```
 *
 * @param mentionTextColor Color for regular mention text
 * @param mentionBackgroundColor Background color for mention spans (with alpha)
 * @param selfMentionTextColor Color for self-mention text (when logged-in user is mentioned)
 * @param selfMentionBackgroundColor Background color for self-mention spans
 * @param mentionFontWeight Font weight for mention text
 *
 * @see MentionText Composable that uses this style
 * @see MentionDisplayContext Context enum for style selection
 */
data class MentionTextStyle(
    val mentionTextColor: Color,
    val mentionBackgroundColor: Color = Color.Transparent,
    val selfMentionTextColor: Color = mentionTextColor,
    val selfMentionBackgroundColor: Color = mentionBackgroundColor,
    val mentionFontWeight: FontWeight = FontWeight.Medium
) {
    companion object {
        /**
         * Creates a default style for incoming message bubbles.
         * Uses primary color for mentions on light background.
         */
        @Composable
        fun incomingBubble(): MentionTextStyle = MentionTextStyle(
            mentionTextColor = CometChatTheme.colorScheme.primary,
            mentionBackgroundColor = CometChatTheme.colorScheme.extendedPrimaryColor100,
            selfMentionTextColor = CometChatTheme.colorScheme.primary,
            selfMentionBackgroundColor = CometChatTheme.colorScheme.extendedPrimaryColor200,
            mentionFontWeight = FontWeight.SemiBold
        )

        /**
         * Creates a default style for outgoing message bubbles.
         * Uses white/light color for mentions on primary colored background.
         */
        @Composable
        fun outgoingBubble(): MentionTextStyle = MentionTextStyle(
            mentionTextColor = CometChatTheme.colorScheme.textColorWhite,
            mentionBackgroundColor = CometChatTheme.colorScheme.extendedPrimaryColor700,
            selfMentionTextColor = CometChatTheme.colorScheme.textColorWhite,
            selfMentionBackgroundColor = CometChatTheme.colorScheme.extendedPrimaryColor600,
            mentionFontWeight = FontWeight.SemiBold
        )

        /**
         * Creates a default style for conversation preview.
         * Uses highlight color for mentions.
         */
        @Composable
        fun conversationPreview(): MentionTextStyle = MentionTextStyle(
            mentionTextColor = CometChatTheme.colorScheme.textColorHighlight,
            mentionBackgroundColor = Color.Transparent,
            selfMentionTextColor = CometChatTheme.colorScheme.textColorHighlight,
            selfMentionBackgroundColor = Color.Transparent,
            mentionFontWeight = FontWeight.Medium
        )

        /**
         * Creates a default style based on the display context.
         */
        @Composable
        fun forContext(context: MentionDisplayContext): MentionTextStyle = when (context) {
            MentionDisplayContext.INCOMING_BUBBLE -> incomingBubble()
            MentionDisplayContext.OUTGOING_BUBBLE -> outgoingBubble()
            MentionDisplayContext.CONVERSATION_PREVIEW -> conversationPreview()
            MentionDisplayContext.DEFAULT -> incomingBubble()
        }
    }
}


/**
 * A composable that displays message text with styled and clickable mentions.
 *
 * This composable handles the full flow of:
 * 1. Parsing mention patterns from the message text
 * 2. Building an AnnotatedString with appropriate styling
 * 3. Rendering clickable text that responds to mention taps
 *
 * ## Mention Patterns Recognized
 *
 * - User mentions: `<@uid:userId>` → displayed as `@userName`
 * - Mention all: `<@all:all>` → displayed as `@mentionAllLabel`
 *
 * ## Basic Usage
 *
 * ```kotlin
 * @Composable
 * fun MessageBubble(message: TextMessage) {
 *     MentionText(
 *         text = message.text,
 *         mentionedUsers = message.mentionedUsers ?: emptyList(),
 *         onMentionClick = { user ->
 *             // Navigate to user profile
 *         },
 *         style = MentionTextStyle.incomingBubble()
 *     )
 * }
 * ```
 *
 * ## With Mention All Support
 *
 * ```kotlin
 * MentionText(
 *     text = messageText,
 *     mentionedUsers = mentionedUsers,
 *     onMentionClick = { user -> showUserProfile(user) },
 *     onMentionAllClick = { showGroupMembers() },
 *     mentionAllLabel = "Notify All"
 * )
 * ```
 *
 * @param text The raw message text containing mention patterns
 * @param mentionedUsers List of User objects for resolving mention display names
 * @param onMentionClick Callback invoked when a user mention is clicked, receives the User object
 * @param modifier Modifier for the composable
 * @param onMentionAllClick Optional callback invoked when "mention all" is clicked
 * @param style MentionTextStyle for customizing mention appearance
 * @param textStyle Base TextStyle for the text
 * @param mentionAllLabel Display label for "mention all" (default: "all")
 * @param trackingCharacter Character used for mentions (default: '@')
 * @param softWrap Whether the text should wrap at soft line breaks
 * @param maxLines Maximum number of lines to display
 * @param overflow How to handle text overflow
 *
 * @see MentionTextStyle Style configuration for mentions
 * @see MentionDisplayContext Context enum for style selection
 */
@Composable
fun MentionText(
    text: String,
    mentionedUsers: List<User>,
    onMentionClick: (User) -> Unit,
    modifier: Modifier = Modifier,
    onMentionAllClick: (() -> Unit)? = null,
    style: MentionTextStyle = MentionTextStyle.incomingBubble(),
    textStyle: TextStyle = TextStyle.Default,
    mentionAllLabel: String = "all",
    trackingCharacter: Char = '@',
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip
) {
    val layoutDirection = LocalLayoutDirection.current
    val loggedInUserId = remember { CometChatUIKit.getLoggedInUser()?.uid }
    
    // Build map of user IDs to names for parsing
    val userMap = remember(mentionedUsers) {
        mentionedUsers.associate { it.uid to it.name }
    }
    
    // Parse mentions and build annotated string
    val annotatedString = remember(text, userMap, loggedInUserId, style) {
        buildMentionAnnotatedStringFromText(
            text = text,
            mentionedUsers = userMap,
            loggedInUserId = loggedInUserId,
            mentionAllLabel = mentionAllLabel,
            trackingCharacter = trackingCharacter,
            style = style
        )
    }
    
    // Apply RTL-aware text style
    val effectiveTextStyle = remember(textStyle, layoutDirection) {
        if (layoutDirection == LayoutDirection.Rtl) {
            textStyle.copy(
                textDirection = androidx.compose.ui.text.style.TextDirection.Rtl
            )
        } else {
            textStyle
        }
    }
    
    ClickableText(
        text = annotatedString,
        modifier = modifier,
        style = effectiveTextStyle,
        softWrap = softWrap,
        maxLines = maxLines,
        overflow = overflow,
        onClick = { offset ->
            // Check for regular mention annotation
            annotatedString.getStringAnnotations(tag = MENTION_TAG, start = offset, end = offset)
                .firstOrNull()?.let { annotation ->
                    val userId = annotation.item
                    mentionedUsers.find { it.uid == userId }?.let { user ->
                        onMentionClick(user)
                    }
                    return@ClickableText
                }
            
            // Check for mention all annotation
            annotatedString.getStringAnnotations(tag = MENTION_ALL_TAG, start = offset, end = offset)
                .firstOrNull()?.let {
                    onMentionAllClick?.invoke()
                }
        }
    )
}

/**
 * A composable that displays message text with styled and clickable mentions.
 * This overload accepts a BaseMessage directly for convenience.
 *
 * @param message The BaseMessage containing text and mentionedUsers
 * @param onMentionClick Callback invoked when a user mention is clicked
 * @param modifier Modifier for the composable
 * @param displayContext The context in which mentions are displayed (affects default styling)
 * @param onMentionAllClick Optional callback invoked when "mention all" is clicked
 * @param style Optional custom MentionTextStyle (if null, uses context-based default)
 * @param textStyle Base TextStyle for the text
 * @param mentionAllLabel Display label for "mention all"
 * @param trackingCharacter Character used for mentions
 * @param softWrap Whether the text should wrap at soft line breaks
 * @param maxLines Maximum number of lines to display
 * @param overflow How to handle text overflow
 */
@Composable
fun MentionText(
    message: BaseMessage,
    onMentionClick: (User) -> Unit,
    modifier: Modifier = Modifier,
    displayContext: MentionDisplayContext = MentionDisplayContext.DEFAULT,
    onMentionAllClick: (() -> Unit)? = null,
    style: MentionTextStyle? = null,
    textStyle: TextStyle = TextStyle.Default,
    mentionAllLabel: String = "all",
    trackingCharacter: Char = '@',
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip
) {
    val messageText = when (message) {
        is TextMessage -> message.text ?: ""
        else -> ""
    }
    
    val mentionedUsers = message.mentionedUsers ?: emptyList()
    val effectiveStyle = style ?: MentionTextStyle.forContext(displayContext)
    
    MentionText(
        text = messageText,
        mentionedUsers = mentionedUsers,
        onMentionClick = onMentionClick,
        modifier = modifier,
        onMentionAllClick = onMentionAllClick,
        style = effectiveStyle,
        textStyle = textStyle,
        mentionAllLabel = mentionAllLabel,
        trackingCharacter = trackingCharacter,
        softWrap = softWrap,
        maxLines = maxLines,
        overflow = overflow
    )
}


/**
 * A simpler composable for displaying pre-built AnnotatedString with mentions.
 * Use this when you've already built the AnnotatedString via [buildMentionAnnotatedString].
 *
 * @param annotatedText The pre-built AnnotatedString with mention annotations
 * @param onMentionClick Callback invoked when a mention is clicked, receives the user ID
 * @param modifier Modifier for the composable
 * @param onMentionAllClick Optional callback invoked when "mention all" is clicked
 * @param textStyle TextStyle to apply to the text
 * @param softWrap Whether the text should wrap at soft line breaks
 * @param maxLines Maximum number of lines to display
 * @param overflow How to handle text overflow
 */
@Composable
fun MentionText(
    annotatedText: AnnotatedString,
    onMentionClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    onMentionAllClick: (() -> Unit)? = null,
    textStyle: TextStyle = TextStyle.Default,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip
) {
    val layoutDirection = LocalLayoutDirection.current
    
    val effectiveTextStyle = remember(textStyle, layoutDirection) {
        if (layoutDirection == LayoutDirection.Rtl) {
            textStyle.copy(
                textDirection = androidx.compose.ui.text.style.TextDirection.Rtl
            )
        } else {
            textStyle
        }
    }
    
    ClickableText(
        text = annotatedText,
        modifier = modifier,
        style = effectiveTextStyle,
        softWrap = softWrap,
        maxLines = maxLines,
        overflow = overflow,
        onClick = { offset ->
            // Check for regular mention annotation
            annotatedText.getStringAnnotations(tag = MENTION_TAG, start = offset, end = offset)
                .firstOrNull()?.let { annotation ->
                    onMentionClick(annotation.item)
                    return@ClickableText
                }
            
            // Check for mention all annotation
            annotatedText.getStringAnnotations(tag = MENTION_ALL_TAG, start = offset, end = offset)
                .firstOrNull()?.let {
                    onMentionAllClick?.invoke()
                }
        }
    )
}

/**
 * Internal helper function to build an AnnotatedString from raw text with mention patterns.
 */
private fun buildMentionAnnotatedStringFromText(
    text: String,
    mentionedUsers: Map<String, String>,
    loggedInUserId: String?,
    mentionAllLabel: String,
    trackingCharacter: Char,
    style: MentionTextStyle
): AnnotatedString {
    // Parse mentions from text
    val parseResult = parseMentionsFromText(
        text = text,
        mentionedUsers = mentionedUsers,
        loggedInUserId = loggedInUserId,
        mentionAllId = "all",
        mentionAllLabel = mentionAllLabel,
        trackingCharacter = trackingCharacter
    )
    
    // Build span styles
    val mentionSpanStyle = SpanStyle(
        color = style.mentionTextColor,
        background = style.mentionBackgroundColor,
        fontWeight = style.mentionFontWeight
    )
    
    val selfMentionSpanStyle = SpanStyle(
        color = style.selfMentionTextColor,
        background = style.selfMentionBackgroundColor,
        fontWeight = style.mentionFontWeight
    )
    
    // Build annotated string with styles
    return buildMentionAnnotatedString(
        text = parseResult.displayText,
        mentions = parseResult.mentions,
        mentionStyle = mentionSpanStyle,
        selfMentionStyle = selfMentionSpanStyle
    )
}

/**
 * Extension function to check if a BaseMessage contains mentions.
 */
fun BaseMessage.hasMentions(): Boolean {
    return !mentionedUsers.isNullOrEmpty()
}

/**
 * Extension function to check if the logged-in user is mentioned in a message.
 */
fun BaseMessage.mentionsCurrentUser(): Boolean {
    val loggedInUserId = CometChatUIKit.getLoggedInUser()?.uid ?: return false
    return mentionedUsers?.any { it.uid == loggedInUserId } == true
}
