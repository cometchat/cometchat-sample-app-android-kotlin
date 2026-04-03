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
 * Style configuration for [CometChatTextBubble] composable.
 *
 * This immutable data class extends [CometChatMessageBubbleStyle] and defines the visual
 * appearance of text message bubbles, including text styling, link colors, translated text
 * styling, and link preview styling.
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
 * CometChatTextBubble(
 *     message = textMessage,
 *     alignment = MessageBubbleAlignment.LEFT,
 *     style = CometChatTextBubbleStyle.incoming()
 * )
 * ```
 *
 * @property textColor The color of the message text
 * @property textStyle The text style for the message
 * @property linkColor The color of clickable links in the text
 * @property translatedTextColor The color of translated text
 * @property translatedTextStyle The text style for translated text
 * @property separatorColor The color of the separator line
 * @property linkPreviewBackgroundColor The background color of link previews
 * @property linkPreviewTitleColor The color of link preview titles
 * @property linkPreviewTitleStyle The text style for link preview titles
 * @property linkPreviewDescriptionColor The color of link preview descriptions
 * @property linkPreviewDescriptionStyle The text style for link preview descriptions
 * @property linkPreviewLinkColor The color of link preview URL text
 * @property linkPreviewLinkStyle The text style for link preview URL text
 * @property linkPreviewCornerRadius The corner radius of link preview cards
 * @property linkPreviewStrokeWidth The stroke width of link preview cards
 * @property linkPreviewStrokeColor The stroke color of link preview cards
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
data class CometChatTextBubbleStyle(
    // Content-specific properties ONLY
    val textColor: Color,
    val textStyle: TextStyle,
    val linkColor: Color,
    val translatedTextColor: Color,
    val translatedTextStyle: TextStyle,
    val separatorColor: Color,
    val linkPreviewBackgroundColor: Color,
    val linkPreviewTitleColor: Color,
    val linkPreviewTitleStyle: TextStyle,
    val linkPreviewDescriptionColor: Color,
    val linkPreviewDescriptionStyle: TextStyle,
    val linkPreviewLinkColor: Color,
    val linkPreviewLinkStyle: TextStyle,
    val linkPreviewCornerRadius: Dp,
    val linkPreviewStrokeWidth: Dp,
    val linkPreviewStrokeColor: Color,
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
         * Creates a default text bubble style using CometChat theme tokens.
         *
         * @param textColor The color of the message text
         * @param textStyle The text style for the message
         * @param linkColor The color of clickable links in the text
         * @param translatedTextColor The color of translated text
         * @param translatedTextStyle The text style for translated text
         * @param separatorColor The color of the separator line
         * @param linkPreviewBackgroundColor The background color of link previews
         * @param linkPreviewTitleColor The color of link preview titles
         * @param linkPreviewTitleStyle The text style for link preview titles
         * @param linkPreviewDescriptionColor The color of link preview descriptions
         * @param linkPreviewDescriptionStyle The text style for link preview descriptions
         * @param linkPreviewLinkColor The color of link preview URL text
         * @param linkPreviewLinkStyle The text style for link preview URL text
         * @param linkPreviewCornerRadius The corner radius of link preview cards
         * @param linkPreviewStrokeWidth The stroke width of link preview cards
         * @param linkPreviewStrokeColor The stroke color of link preview cards
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
         * @param timestampTextColor The color of timestamp text in status info view
         * @param timestampTextStyle The text style for timestamp
         * @return A new [CometChatTextBubbleStyle] instance with default values
         */
        @Composable
        fun default(
            // Content-specific defaults (keep theme-based values)
            textColor: Color = CometChatTheme.colorScheme.textColorPrimary,
            textStyle: TextStyle = CometChatTheme.typography.bodyRegular,
            linkColor: Color = CometChatTheme.colorScheme.infoColor,
            translatedTextColor: Color = CometChatTheme.colorScheme.textColorSecondary,
            translatedTextStyle: TextStyle = CometChatTheme.typography.caption2Regular,
            separatorColor: Color = CometChatTheme.colorScheme.strokeColorLight,
            linkPreviewBackgroundColor: Color = CometChatTheme.colorScheme.backgroundColor4,
            linkPreviewTitleColor: Color = CometChatTheme.colorScheme.textColorPrimary,
            linkPreviewTitleStyle: TextStyle = CometChatTheme.typography.bodyBold,
            linkPreviewDescriptionColor: Color = CometChatTheme.colorScheme.textColorSecondary,
            linkPreviewDescriptionStyle: TextStyle = CometChatTheme.typography.caption1Regular,
            linkPreviewLinkColor: Color = CometChatTheme.colorScheme.infoColor,
            linkPreviewLinkStyle: TextStyle = CometChatTheme.typography.caption1Regular,
            linkPreviewCornerRadius: Dp = 8.dp,
            linkPreviewStrokeWidth: Dp = 0.dp,
            linkPreviewStrokeColor: Color = Color.Transparent,
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
        ): CometChatTextBubbleStyle = CometChatTextBubbleStyle(
            textColor = textColor,
            textStyle = textStyle,
            linkColor = linkColor,
            translatedTextColor = translatedTextColor,
            translatedTextStyle = translatedTextStyle,
            separatorColor = separatorColor,
            linkPreviewBackgroundColor = linkPreviewBackgroundColor,
            linkPreviewTitleColor = linkPreviewTitleColor,
            linkPreviewTitleStyle = linkPreviewTitleStyle,
            linkPreviewDescriptionColor = linkPreviewDescriptionColor,
            linkPreviewDescriptionStyle = linkPreviewDescriptionStyle,
            linkPreviewLinkColor = linkPreviewLinkColor,
            linkPreviewLinkStyle = linkPreviewLinkStyle,
            linkPreviewCornerRadius = linkPreviewCornerRadius,
            linkPreviewStrokeWidth = linkPreviewStrokeWidth,
            linkPreviewStrokeColor = linkPreviewStrokeColor,
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
         * Creates a style for incoming (left-aligned) text messages.
         *
         * CommonProperties use sentinel values (filled from messageBubbleStyle during merge).
         * Content-specific properties use theme defaults for incoming messages.
         *
         * @return A new [CometChatTextBubbleStyle] configured for incoming messages
         */
        @Composable
        fun incoming(): CometChatTextBubbleStyle = default()

        /**
         * Creates a style for outgoing (right-aligned) text messages.
         *
         * CommonProperties use sentinel values (filled from messageBubbleStyle during merge).
         * Content-specific properties use white text and link colors for contrast on
         * primary-colored backgrounds.
         *
         * @return A new [CometChatTextBubbleStyle] configured for outgoing messages
         */
        @Composable
        fun outgoing(): CometChatTextBubbleStyle = default(
            textColor = Color.White,
            linkColor = Color.White,
            translatedTextColor = Color.White.copy(alpha = 0.7f),
            linkPreviewBackgroundColor = CometChatTheme.colorScheme.extendedPrimaryColor900
        )
    }
}
