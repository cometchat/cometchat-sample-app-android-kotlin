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
 * Style configuration for [CometChatCollaborativeBubble] composable.
 *
 * This immutable data class extends [CometChatMessageBubbleStyle] and defines the visual
 * appearance of collaborative document/whiteboard message bubbles, which display links
 * to collaborative documents with a "Join" or "Open" button.
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
 * CometChatCollaborativeBubble(
 *     message = customMessage,
 *     alignment = MessageBubbleAlignment.LEFT,
 *     style = CometChatCollaborativeBubbleStyle.incoming()
 * )
 * ```
 *
 * @property titleTextColor The color of the document title text
 * @property titleTextStyle The text style for the document title
 * @property subtitleTextColor The color of the document subtitle text
 * @property subtitleTextStyle The text style for the document subtitle
 * @property iconTint The tint color of the collaborative icon
 * @property buttonTextColor The color of the "Join" button text
 * @property buttonTextStyle The text style for the "Join" button
 * @property separatorColor The color of the separator line
 * @property imageStrokeWidth The stroke width of the image border
 * @property imageStrokeColor The stroke color of the image border
 * @property imageCornerRadius The corner radius of the image
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
data class CometChatCollaborativeBubbleStyle(
    // Content-specific properties ONLY
    val titleTextColor: Color,
    val titleTextStyle: TextStyle,
    val subtitleTextColor: Color,
    val subtitleTextStyle: TextStyle,
    val iconTint: Color,
    val buttonTextColor: Color,
    val buttonTextStyle: TextStyle,
    val separatorColor: Color,
    val imageStrokeWidth: Dp,
    val imageStrokeColor: Color,
    val imageCornerRadius: Dp,
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
         * Creates a default collaborative bubble style using CometChat theme tokens.
         *
         * @param titleTextColor The color of the document title text
         * @param titleTextStyle The text style for the document title
         * @param subtitleTextColor The color of the document subtitle text
         * @param subtitleTextStyle The text style for the document subtitle
         * @param iconTint The tint color of the collaborative icon
         * @param buttonTextColor The color of the "Join" button text
         * @param buttonTextStyle The text style for the "Join" button
         * @param separatorColor The color of the separator line
         * @param imageStrokeWidth The stroke width of the image border
         * @param imageStrokeColor The stroke color of the image border
         * @param imageCornerRadius The corner radius of the image
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
         * @return A new [CometChatCollaborativeBubbleStyle] instance with default values
         */
        @Composable
        fun default(
            // Content-specific defaults (keep theme-based values)
            titleTextColor: Color = CometChatTheme.colorScheme.textColorPrimary,
            titleTextStyle: TextStyle = CometChatTheme.typography.bodyMedium,
            subtitleTextColor: Color = CometChatTheme.colorScheme.textColorSecondary,
            subtitleTextStyle: TextStyle = CometChatTheme.typography.caption1Regular,
            iconTint: Color = CometChatTheme.colorScheme.primary,
            buttonTextColor: Color = CometChatTheme.colorScheme.primary,
            buttonTextStyle: TextStyle = CometChatTheme.typography.buttonMedium,
            separatorColor: Color = CometChatTheme.colorScheme.strokeColorDark,
            imageStrokeWidth: Dp = 0.dp,
            imageStrokeColor: Color = Color.Transparent,
            imageCornerRadius: Dp = 8.dp,
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
        ): CometChatCollaborativeBubbleStyle = CometChatCollaborativeBubbleStyle(
            titleTextColor = titleTextColor,
            titleTextStyle = titleTextStyle,
            subtitleTextColor = subtitleTextColor,
            subtitleTextStyle = subtitleTextStyle,
            iconTint = iconTint,
            buttonTextColor = buttonTextColor,
            buttonTextStyle = buttonTextStyle,
            separatorColor = separatorColor,
            imageStrokeWidth = imageStrokeWidth,
            imageStrokeColor = imageStrokeColor,
            imageCornerRadius = imageCornerRadius,
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
         * Creates a style for incoming (left-aligned) collaborative messages.
         *
         * CommonProperties use sentinel values (filled from messageBubbleStyle during merge).
         * Content-specific properties use theme defaults with strokeColorDefault separator.
         *
         * @return A new [CometChatCollaborativeBubbleStyle] configured for incoming messages
         */
        @Composable
        fun incoming(): CometChatCollaborativeBubbleStyle = default(
            separatorColor = CometChatTheme.colorScheme.strokeColorDark
        )

        /**
         * Creates a style for outgoing (right-aligned) collaborative messages.
         *
         * CommonProperties use sentinel values (filled from messageBubbleStyle during merge).
         * Content-specific properties use white text for contrast.
         *
         * @return A new [CometChatCollaborativeBubbleStyle] configured for outgoing messages
         */
        @Composable
        fun outgoing(): CometChatCollaborativeBubbleStyle = default(
            titleTextColor = CometChatTheme.colorScheme.colorWhite,
            subtitleTextColor = CometChatTheme.colorScheme.colorWhite,
            iconTint = CometChatTheme.colorScheme.colorWhite,
            buttonTextColor = CometChatTheme.colorScheme.colorWhite,
            separatorColor = CometChatTheme.colorScheme.extendedPrimaryColor800
        )
    }
}
