package com.cometchat.uikit.compose.presentation.shared.messagebubble.style

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp

/**
 * Style configuration for [CometChatStickerBubble] composable.
 *
 * This immutable data class extends [CometChatMessageBubbleStyle] and defines the visual
 * appearance of sticker message bubbles. Stickers have no content-specific properties —
 * they only use the wrapper properties inherited from the parent class.
 *
 * **Note:** Stickers use transparent background by default, unlike other bubble types
 * which use backgroundColor2 or backgroundColor3.
 *
 * Common wrapper properties (backgroundColor, cornerRadius, strokeWidth, strokeColor, padding,
 * senderName styling, threadIndicator styling) are inherited from the parent class.
 *
 * Use the companion object's factory functions to create instances:
 * - [default] for a neutral style (transparent background)
 * - [incoming] for incoming (left-aligned) messages (transparent background)
 * - [outgoing] for outgoing (right-aligned) messages (transparent background)
 *
 * Example usage:
 * ```kotlin
 * CometChatStickerBubble(
 *     message = customMessage,
 *     alignment = MessageBubbleAlignment.LEFT,
 *     style = CometChatStickerBubbleStyle.incoming()
 * )
 * ```
 *
 * @property backgroundColor The background color of the bubble (transparent by default for stickers)
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
data class CometChatStickerBubbleStyle(
    // No content-specific properties — stickers only have wrapper properties
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
         * Creates a default sticker bubble style using CometChat theme tokens.
         *
         * Note: Stickers use transparent background by default, unlike other bubble types.
         *
         * @param backgroundColor The background color of the bubble (transparent by default)
         * @param cornerRadius The corner radius of the bubble
         * @param strokeWidth The stroke width of the bubble border
         * @param strokeColor The stroke color of the bubble border
         * @param padding The internal padding of the bubble content
         * @param senderNameTextColor The color of sender name text
         * @param senderNameTextStyle The text style for sender name
         * @param threadIndicatorTextColor The color of thread indicator text
         * @param threadIndicatorTextStyle The text style for thread indicator
         * @param threadIndicatorIconTint The tint color of thread indicator icon
         * @return A new [CometChatStickerBubbleStyle] instance with default values
         */
        @Composable
        fun default(
            // No content-specific properties — stickers only have wrapper properties
            // CommonProperties default to sentinels (filled from messageBubbleStyle during merge)
            backgroundColor: Color = Color.Transparent,
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
        ): CometChatStickerBubbleStyle = CometChatStickerBubbleStyle(
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
         * Creates a style for incoming (left-aligned) sticker messages.
         *
         * Uses transparent background (stickers don't have colored backgrounds).
         *
         * @return A new [CometChatStickerBubbleStyle] configured for incoming messages
         */
        @Composable
        fun incoming(): CometChatStickerBubbleStyle = default()

        /**
         * Creates a style for outgoing (right-aligned) sticker messages.
         *
         * Uses transparent background (stickers don't have colored backgrounds).
         *
         * @return A new [CometChatStickerBubbleStyle] configured for outgoing messages
         */
        @Composable
        fun outgoing(): CometChatStickerBubbleStyle = default()
    }
}
