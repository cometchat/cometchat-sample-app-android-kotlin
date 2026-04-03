package com.cometchat.uikit.compose.presentation.shared.messagebubble.style

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cometchat.uikit.compose.presentation.shared.baseelements.avatar.CometChatAvatarStyle
import com.cometchat.uikit.compose.presentation.shared.baseelements.date.DateStyle
import com.cometchat.uikit.compose.presentation.shared.formatters.style.CometChatMentionStyle
import com.cometchat.uikit.compose.presentation.shared.messagebubble.aiassistantbubble.CometChatAIAssistantBubbleStyle
import com.cometchat.uikit.compose.presentation.shared.messagepreview.CometChatMessagePreviewStyle
import com.cometchat.uikit.compose.presentation.shared.moderation.CometChatModerationViewStyle
import com.cometchat.uikit.compose.presentation.shared.reaction.CometChatReactionStyle
import com.cometchat.uikit.compose.presentation.shared.receipts.CometChatReceiptsStyle
import com.cometchat.uikit.compose.theme.CometChatTheme

/**
 * Base style configuration for [CometChatMessageBubble] composable.
 *
 * This open class defines the shared visual appearance properties common to all
 * message bubble types: background color, corner radius, border styling, padding,
 * sender name styling, thread indicator styling, and timestamp styling.
 *
 * Per-bubble-type style classes (e.g., [CometChatTextBubbleStyle],
 * [CometChatAudioBubbleStyle]) extend this class and add content-specific
 * properties while inheriting the common wrapper properties.
 *
 * Use the companion object's factory functions to create instances:
 * - [default] for a neutral style
 * - [incoming] for incoming (left-aligned) messages
 * - [outgoing] for outgoing (right-aligned) messages
 *
 * Example usage:
 * ```kotlin
 * CometChatMessageBubble(
 *     message = message,
 *     alignment = MessageBubbleAlignment.LEFT,
 *     style = CometChatMessageBubbleStyle.incoming()
 * )
 * ```
 *
 * @property backgroundColor The background color of the bubble
 * @property cornerRadius The corner radius of the bubble
 * @property strokeWidth The width of the border stroke (0.dp for no border)
 * @property strokeColor The color of the border stroke
 * @property padding The internal padding of the bubble content
 * @property senderNameTextColor The color of sender name text
 * @property senderNameTextStyle The text style for sender name
 * @property threadIndicatorTextColor The color of thread indicator text
 * @property threadIndicatorTextStyle The text style for thread indicator
 * @property threadIndicatorIconTint The tint color of thread indicator icon
 * @property timestampTextColor The color of timestamp text in status info view
 * @property timestampTextStyle The text style for timestamp
 * @property dateStyle Optional style for the date component within the bubble
 * @property messageReceiptStyle Optional style for the message receipt indicator
 * @property avatarStyle Optional style for the avatar component
 * @property reactionStyle Optional style for the reaction component
 * @property mentionStyle Optional style for mention text rendering
 * @property messagePreviewStyle Optional style for the quoted message preview within the bubble
 * @property moderationViewStyle Optional style for the moderation view
 * @property aiAssistantBubbleStyle Optional style for AI assistant bubble content
 * @property textBubbleStyle Optional style override for text bubble content
 * @property imageBubbleStyle Optional style override for image bubble content
 * @property videoBubbleStyle Optional style override for video bubble content
 * @property fileBubbleStyle Optional style override for file bubble content
 * @property audioBubbleStyle Optional style override for audio bubble content
 * @property deleteBubbleStyle Optional style override for deleted message bubble content
 * @property stickerBubbleStyle Optional style override for sticker bubble content
 * @property pollBubbleStyle Optional style override for poll bubble content
 * @property collaborativeBubbleStyle Optional style override for collaborative bubble content
 * @property meetCallBubbleStyle Optional style override for meet/call bubble content
 */
@Immutable
open class CometChatMessageBubbleStyle(
    open val backgroundColor: Color,
    open val cornerRadius: Dp,
    open val strokeWidth: Dp,
    open val strokeColor: Color,
    open val padding: PaddingValues,
    open val senderNameTextColor: Color,
    open val senderNameTextStyle: TextStyle,
    open val threadIndicatorTextColor: Color,
    open val threadIndicatorTextStyle: TextStyle,
    open val threadIndicatorIconTint: Color,
    open val timestampTextColor: Color,
    open val timestampTextStyle: TextStyle,
    // Sub-component style references
    open val dateStyle: DateStyle? = null,
    open val messageReceiptStyle: CometChatReceiptsStyle? = null,
    open val avatarStyle: CometChatAvatarStyle? = null,
    open val reactionStyle: CometChatReactionStyle? = null,
    open val mentionStyle: CometChatMentionStyle? = null,
    open val moderationViewStyle: CometChatModerationViewStyle? = null,
    open val aiAssistantBubbleStyle: CometChatAIAssistantBubbleStyle? = null,
    open val messagePreviewStyle: CometChatMessagePreviewStyle? = null,
    // Per-bubble-type style references
    open val textBubbleStyle: CometChatTextBubbleStyle? = null,
    open val imageBubbleStyle: CometChatImageBubbleStyle? = null,
    open val videoBubbleStyle: CometChatVideoBubbleStyle? = null,
    open val fileBubbleStyle: CometChatFileBubbleStyle? = null,
    open val audioBubbleStyle: CometChatAudioBubbleStyle? = null,
    open val deleteBubbleStyle: CometChatDeleteBubbleStyle? = null,
    open val stickerBubbleStyle: CometChatStickerBubbleStyle? = null,
    open val pollBubbleStyle: CometChatPollBubbleStyle? = null,
    open val collaborativeBubbleStyle: CometChatCollaborativeBubbleStyle? = null,
    open val meetCallBubbleStyle: CometChatMeetCallBubbleStyle? = null
) {
    companion object {
        /**
         * Creates a default message bubble style using CometChat theme tokens.
         *
         * @param backgroundColor The background color, defaults to backgroundColor2
         * @param cornerRadius The corner radius, defaults to 12.dp
         * @param strokeWidth The border width, defaults to 0.dp (no border)
         * @param strokeColor The border color, defaults to transparent
         * @param padding The internal padding, defaults to 0.dp
         * @param senderNameTextColor The color of sender name text
         * @param senderNameTextStyle The text style for sender name
         * @param threadIndicatorTextColor The color of thread indicator text
         * @param threadIndicatorTextStyle The text style for thread indicator
         * @param threadIndicatorIconTint The tint color of thread indicator icon
         * @param timestampTextColor The color of timestamp text in status info view
         * @param timestampTextStyle The text style for timestamp
         * @param dateStyle Optional style for the date component
         * @param messageReceiptStyle Optional style for the receipt indicator
         * @param avatarStyle Optional style for the avatar component
         * @param reactionStyle Optional style for the reaction component
         * @param mentionStyle Optional style for mention text
         * @param moderationViewStyle Optional style for the moderation view
         * @param aiAssistantBubbleStyle Optional style for AI assistant bubble
         * @param messagePreviewStyle Optional style for the quoted message preview
         * @param textBubbleStyle Optional style for text bubble content
         * @param imageBubbleStyle Optional style for image bubble content
         * @param videoBubbleStyle Optional style for video bubble content
         * @param fileBubbleStyle Optional style for file bubble content
         * @param audioBubbleStyle Optional style for audio bubble content
         * @param deleteBubbleStyle Optional style for deleted message bubble content
         * @param stickerBubbleStyle Optional style for sticker bubble content
         * @param pollBubbleStyle Optional style for poll bubble content
         * @param collaborativeBubbleStyle Optional style for collaborative bubble content
         * @param meetCallBubbleStyle Optional style for meet/call bubble content
         * @return A new [CometChatMessageBubbleStyle] instance
         */
        @Composable
        fun default(
            backgroundColor: Color = CometChatTheme.colorScheme.backgroundColor2,
            cornerRadius: Dp = 12.dp,
            strokeWidth: Dp = 0.dp,
            strokeColor: Color = Color.Transparent,
            padding: PaddingValues = PaddingValues(0.dp),
            senderNameTextColor: Color = CometChatTheme.colorScheme.textColorHighlight,
            senderNameTextStyle: TextStyle = CometChatTheme.typography.caption1Medium,
            threadIndicatorTextColor: Color = CometChatTheme.colorScheme.textColorSecondary,
            threadIndicatorTextStyle: TextStyle = CometChatTheme.typography.caption1Regular,
            threadIndicatorIconTint: Color = CometChatTheme.colorScheme.iconTintSecondary,
            timestampTextColor: Color = CometChatTheme.colorScheme.textColorSecondary,
            timestampTextStyle: TextStyle = CometChatTheme.typography.caption1Regular,
            dateStyle: DateStyle? = null,
            messageReceiptStyle: CometChatReceiptsStyle? = null,
            avatarStyle: CometChatAvatarStyle? = null,
            reactionStyle: CometChatReactionStyle? = null,
            mentionStyle: CometChatMentionStyle? = null,
            moderationViewStyle: CometChatModerationViewStyle? = null,
            aiAssistantBubbleStyle: CometChatAIAssistantBubbleStyle? = null,
            messagePreviewStyle: CometChatMessagePreviewStyle? = null,
            textBubbleStyle: CometChatTextBubbleStyle? = null,
            imageBubbleStyle: CometChatImageBubbleStyle? = null,
            videoBubbleStyle: CometChatVideoBubbleStyle? = null,
            fileBubbleStyle: CometChatFileBubbleStyle? = null,
            audioBubbleStyle: CometChatAudioBubbleStyle? = null,
            deleteBubbleStyle: CometChatDeleteBubbleStyle? = null,
            stickerBubbleStyle: CometChatStickerBubbleStyle? = null,
            pollBubbleStyle: CometChatPollBubbleStyle? = null,
            collaborativeBubbleStyle: CometChatCollaborativeBubbleStyle? = null,
            meetCallBubbleStyle: CometChatMeetCallBubbleStyle? = null
        ): CometChatMessageBubbleStyle = CometChatMessageBubbleStyle(
            backgroundColor = backgroundColor,
            cornerRadius = cornerRadius,
            strokeWidth = strokeWidth,
            strokeColor = strokeColor,
            padding = padding,
            senderNameTextColor = senderNameTextColor,
            senderNameTextStyle = senderNameTextStyle,
            threadIndicatorTextColor = threadIndicatorTextColor,
            threadIndicatorTextStyle = threadIndicatorTextStyle,
            threadIndicatorIconTint = threadIndicatorIconTint,
            timestampTextColor = timestampTextColor,
            timestampTextStyle = timestampTextStyle,
            dateStyle = dateStyle,
            messageReceiptStyle = messageReceiptStyle,
            avatarStyle = avatarStyle,
            reactionStyle = reactionStyle,
            mentionStyle = mentionStyle,
            moderationViewStyle = moderationViewStyle,
            aiAssistantBubbleStyle = aiAssistantBubbleStyle,
            messagePreviewStyle = messagePreviewStyle,
            textBubbleStyle = textBubbleStyle,
            imageBubbleStyle = imageBubbleStyle,
            videoBubbleStyle = videoBubbleStyle,
            fileBubbleStyle = fileBubbleStyle,
            audioBubbleStyle = audioBubbleStyle,
            deleteBubbleStyle = deleteBubbleStyle,
            stickerBubbleStyle = stickerBubbleStyle,
            pollBubbleStyle = pollBubbleStyle,
            collaborativeBubbleStyle = collaborativeBubbleStyle,
            meetCallBubbleStyle = meetCallBubbleStyle
        )

        /**
         * Creates a style for incoming (left-aligned) messages.
         *
         * Uses backgroundColor4 (neutralColor300) for visible contrast against the message list background (backgroundColor3).
         *
         * @return A new [CometChatMessageBubbleStyle] configured for incoming messages
         */
        @Composable
        fun incoming(
            messagePreviewStyle: CometChatMessagePreviewStyle? = null
        ): CometChatMessageBubbleStyle = default(
            backgroundColor = CometChatTheme.colorScheme.backgroundColor4,
            messagePreviewStyle = messagePreviewStyle
        )

        /**
         * Creates a style for outgoing (right-aligned) messages.
         *
         * Uses a tinted primary color for visual distinction with white timestamp
         * for proper contrast.
         *
         * @return A new [CometChatMessageBubbleStyle] configured for outgoing messages
         */
        @Composable
        fun outgoing(
            messagePreviewStyle: CometChatMessagePreviewStyle? = null
        ): CometChatMessageBubbleStyle = default(
            backgroundColor = CometChatTheme.colorScheme.primary,
            timestampTextColor = Color.White.copy(alpha = 0.8f),
            messagePreviewStyle = messagePreviewStyle
        )
    }
}
