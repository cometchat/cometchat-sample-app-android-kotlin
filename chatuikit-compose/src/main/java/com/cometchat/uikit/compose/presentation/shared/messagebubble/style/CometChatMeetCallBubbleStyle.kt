package com.cometchat.uikit.compose.presentation.shared.messagebubble.style

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cometchat.uikit.compose.theme.CometChatTheme

/**
 * Style configuration for [CometChatMeetCallBubble] composable.
 *
 * This immutable data class extends [CometChatMessageBubbleStyle] and defines the visual
 * appearance of meeting/call invitation message bubbles, which display video or voice call
 * meeting information with a "Join" button.
 *
 * Common wrapper properties (backgroundColor, cornerRadius, strokeWidth, strokeColor, padding,
 * senderName styling, threadIndicator styling) are inherited from the parent class.
 *
 * Use the companion object's factory functions to create instances:
 * - [default] for a neutral style
 * - [incoming] for incoming (left-aligned) messages
 * - [outgoing] for outgoing (right-aligned) messages
 *
 * Example usage:
 * ```kotlin
 * CometChatMeetCallBubble(
 *     message = customMessage,
 *     alignment = MessageBubbleAlignment.LEFT,
 *     style = CometChatMeetCallBubbleStyle.incoming()
 * )
 * ```
 *
 * @property callIconTint The tint color of the call type icon (voice/video)
 * @property iconBackgroundColor The background color of the call icon container
 * @property titleTextColor The color of the call title text
 * @property titleTextStyle The text style for the call title
 * @property subtitleTextColor The color of the call subtitle text
 * @property subtitleTextStyle The text style for the call subtitle
 * @property separatorColor The color of the separator line above the join button
 * @property joinButtonTextColor The color of the "Join" button text
 * @property joinButtonTextStyle The text style for the "Join" button
 * @property backgroundColor The background color of the bubble (inherited from parent)
 * @property cornerRadius The corner radius of the bubble (inherited from parent)
 * @property strokeWidth The stroke width of the bubble border (inherited from parent)
 * @property strokeColor The stroke color of the bubble border (inherited from parent)
 * @property padding The internal padding of the bubble content (inherited from parent)
 * @property senderNameTextColor The color of sender name text (inherited from parent)
 * @property senderNameTextStyle The text style for sender name (inherited from parent)
 * @property threadIndicatorTextColor The color of thread indicator text (inherited from parent)
 * @property threadIndicatorTextStyle The text style for thread indicator (inherited from parent)
 * @property threadIndicatorIconTint The tint color of thread indicator icon (inherited from parent)
 */
@Immutable
data class CometChatMeetCallBubbleStyle(
    // Content-specific properties ONLY
    val callIconTint: Color,
    val iconBackgroundColor: Color,
    val titleTextColor: Color,
    val titleTextStyle: TextStyle,
    val subtitleTextColor: Color,
    val subtitleTextStyle: TextStyle,
    val separatorColor: Color,
    val joinButtonTextColor: Color,
    val joinButtonTextStyle: TextStyle,
    // Common properties passed to parent via override
    override val backgroundColor: Color,
    override val cornerRadius: Dp,
    override val strokeWidth: Dp,
    override val strokeColor: Color,
    override val padding: PaddingValues,
    override val senderNameTextColor: Color,
    override val senderNameTextStyle: TextStyle,
    override val threadIndicatorTextColor: Color,
    override val threadIndicatorTextStyle: TextStyle,
    override val threadIndicatorIconTint: Color,
    override val timestampTextColor: Color,
    override val timestampTextStyle: TextStyle
) : CometChatMessageBubbleStyle(
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
    timestampTextStyle = timestampTextStyle
) {
    companion object {
        /**
         * Creates a default meet call bubble style using CometChat theme tokens.
         *
         * @param callIconTint The tint color of the call type icon
         * @param iconBackgroundColor The background color of the call icon container
         * @param titleTextColor The color of the call title text
         * @param titleTextStyle The text style for the call title
         * @param subtitleTextColor The color of the call subtitle text
         * @param subtitleTextStyle The text style for the call subtitle
         * @param separatorColor The color of the separator line above the join button
         * @param joinButtonTextColor The color of the "Join" button text
         * @param joinButtonTextStyle The text style for the "Join" button
         * @param backgroundColor The background color of the bubble
         * @param cornerRadius The corner radius of the bubble
         * @param strokeWidth The stroke width of the bubble border
         * @param strokeColor The stroke color of the bubble border
         * @param padding The internal padding of the bubble content
         * @param senderNameTextColor The color of sender name text
         * @param senderNameTextStyle The text style for sender name
         * @param threadIndicatorTextColor The color of thread indicator text
         * @param threadIndicatorTextStyle The text style for thread indicator
         * @param threadIndicatorIconTint The tint color of thread indicator icon
         * @return A new [CometChatMeetCallBubbleStyle] instance with default values
         */
        @Composable
        fun default(
            // Content-specific defaults (keep theme-based values)
            callIconTint: Color = CometChatTheme.colorScheme.primary,
            iconBackgroundColor: Color = CometChatTheme.colorScheme.colorWhite,
            titleTextColor: Color = CometChatTheme.colorScheme.textColorPrimary,
            titleTextStyle: TextStyle = CometChatTheme.typography.bodyMedium,
            subtitleTextColor: Color = CometChatTheme.colorScheme.textColorSecondary,
            subtitleTextStyle: TextStyle = CometChatTheme.typography.caption1Regular,
            separatorColor: Color = CometChatTheme.colorScheme.extendedPrimaryColor800,
            joinButtonTextColor: Color = CometChatTheme.colorScheme.primary,
            joinButtonTextStyle: TextStyle = CometChatTheme.typography.buttonMedium,
            // CommonProperties default to sentinels (filled from messageBubbleStyle during merge)
            backgroundColor: Color = UNSET_COLOR,
            cornerRadius: Dp = UNSET_DP,
            strokeWidth: Dp = UNSET_DP,
            strokeColor: Color = UNSET_COLOR,
            padding: PaddingValues = UNSET_PADDING,
            senderNameTextColor: Color = UNSET_COLOR,
            senderNameTextStyle: TextStyle = UNSET_TEXT_STYLE,
            threadIndicatorTextColor: Color = UNSET_COLOR,
            threadIndicatorTextStyle: TextStyle = UNSET_TEXT_STYLE,
            threadIndicatorIconTint: Color = UNSET_COLOR,
            timestampTextColor: Color = UNSET_COLOR,
            timestampTextStyle: TextStyle = UNSET_TEXT_STYLE
        ): CometChatMeetCallBubbleStyle = CometChatMeetCallBubbleStyle(
            callIconTint = callIconTint,
            iconBackgroundColor = iconBackgroundColor,
            titleTextColor = titleTextColor,
            titleTextStyle = titleTextStyle,
            subtitleTextColor = subtitleTextColor,
            subtitleTextStyle = subtitleTextStyle,
            separatorColor = separatorColor,
            joinButtonTextColor = joinButtonTextColor,
            joinButtonTextStyle = joinButtonTextStyle,
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
            timestampTextStyle = timestampTextStyle
        )

        /**
         * Creates a style for incoming (left-aligned) meet call messages.
         *
         * CommonProperties use sentinel values (filled from messageBubbleStyle during merge).
         * Content-specific properties use theme defaults with strokeColorDark separator.
         *
         * @return A new [CometChatMeetCallBubbleStyle] configured for incoming messages
         */
        @Composable
        fun incoming(): CometChatMeetCallBubbleStyle = default(
            separatorColor = CometChatTheme.colorScheme.strokeColorDark
        )

        /**
         * Creates a style for outgoing (right-aligned) meet call messages.
         *
         * CommonProperties use sentinel values (filled from messageBubbleStyle during merge).
         * Content-specific properties use white text for contrast.
         * Note: separatorColor uses default (extendedPrimaryColor800) as per Kotlin reference.
         *
         * @return A new [CometChatMeetCallBubbleStyle] configured for outgoing messages
         */
        @Composable
        fun outgoing(): CometChatMeetCallBubbleStyle = default(
            titleTextColor = CometChatTheme.colorScheme.colorWhite,
            subtitleTextColor = CometChatTheme.colorScheme.colorWhite,
            joinButtonTextColor = CometChatTheme.colorScheme.colorWhite
        )
    }
}
