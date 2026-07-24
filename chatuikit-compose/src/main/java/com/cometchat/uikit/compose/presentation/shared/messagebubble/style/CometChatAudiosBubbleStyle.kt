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
 * Style configuration for [CometChatAudiosBubble] composable — the ENG-36737 multi-attachment
 * audio player-card bubble (picker audio; recorded voice notes use the voice-note bubble).
 *
 * This immutable data class extends [CometChatMessageBubbleStyle] and defines the visual
 * appearance of the stacked player cards (translucent card overlay, play/pause circle, flat
 * seek bar, name/time text, download icon), the "Show N more"/"Show less" toggle, and the
 * shared caption underneath.
 *
 * Note: [cardBackgroundColor] only applies when the message carries MULTIPLE audios — a single
 * player row sits directly on the bubble background (design-final, matches iOS).
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
 * CometChatAudiosBubble(
 *     message = mediaMessage,
 *     alignment = MessageBubbleAlignment.LEFT,
 *     style = CometChatAudiosBubbleStyle.incoming()
 * )
 * ```
 *
 * @property cardBackgroundColor The translucent overlay color of each player card (multiples only)
 * @property cardCornerRadius The corner radius of each player card
 * @property itemSpacing The vertical spacing between player cards
 * @property playButtonBackgroundColor The background color of the play/pause circle
 * @property playIconTint The tint color of the play/pause glyph (and its loading spinner)
 * @property seekFillColor The played portion color of the flat seek bar
 * @property seekTrackColor The track (unplayed) color of the flat seek bar
 * @property seekKnobColor The color of the seek bar knob
 * @property seekKnobBorderColor The border color of the seek bar knob
 * @property titleTextColor The color of the file name text
 * @property titleTextStyle The text style for the file name
 * @property durationTextColor The color of the "mm:ss/mm:ss" time text
 * @property durationTextStyle The text style for the time text
 * @property downloadIconTint The tint color of the per-card download icon
 * @property toggleTextColor The text color of the "Show N more"/"Show less" toggle
 * @property toggleTextStyle The text style for the toggle
 * @property captionTextColor The color of the caption text under the cards
 * @property captionTextStyle The text style for the caption
 */
@Immutable
data class CometChatAudiosBubbleStyle(
    // Content-specific properties ONLY
    val cardBackgroundColor: Color,
    val cardCornerRadius: Dp,
    val itemSpacing: Dp,
    val playButtonBackgroundColor: Color,
    val playIconTint: Color,
    val seekFillColor: Color,
    val seekTrackColor: Color,
    val seekKnobColor: Color,
    val seekKnobBorderColor: Color,
    val titleTextColor: Color,
    val titleTextStyle: TextStyle,
    val durationTextColor: Color,
    val durationTextStyle: TextStyle,
    val downloadIconTint: Color,
    val toggleTextColor: Color,
    val toggleTextStyle: TextStyle,
    val captionTextColor: Color,
    val captionTextStyle: TextStyle,
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
         * Creates a default audios bubble style using CometChat theme tokens.
         *
         * @param cardBackgroundColor The translucent overlay color of each player card
         * @param cardCornerRadius The corner radius of each player card
         * @param itemSpacing The vertical spacing between player cards
         * @param playButtonBackgroundColor The background color of the play/pause circle
         * @param playIconTint The tint color of the play/pause glyph
         * @param seekFillColor The played portion color of the flat seek bar
         * @param seekTrackColor The track (unplayed) color of the flat seek bar
         * @param seekKnobColor The color of the seek bar knob
         * @param seekKnobBorderColor The border color of the seek bar knob
         * @param titleTextColor The color of the file name text
         * @param titleTextStyle The text style for the file name
         * @param durationTextColor The color of the "mm:ss/mm:ss" time text
         * @param durationTextStyle The text style for the time text
         * @param downloadIconTint The tint color of the per-card download icon
         * @param toggleTextColor The text color of the "Show N more"/"Show less" toggle
         * @param toggleTextStyle The text style for the toggle
         * @param captionTextColor The color of the caption text under the cards
         * @param captionTextStyle The text style for the caption
         * @return A new [CometChatAudiosBubbleStyle] instance with default values
         */
        @Composable
        fun default(
            // Content-specific defaults (keep theme-based values)
            cardBackgroundColor: Color = Color.Black.copy(alpha = 0.04f),
            cardCornerRadius: Dp = 12.dp,
            itemSpacing: Dp = 4.dp,
            playButtonBackgroundColor: Color = CometChatTheme.colorScheme.primary,
            playIconTint: Color = Color.White,
            seekFillColor: Color = CometChatTheme.colorScheme.primary,
            seekTrackColor: Color = CometChatTheme.colorScheme.neutralColor400,
            seekKnobColor: Color = Color.White,
            seekKnobBorderColor: Color = CometChatTheme.colorScheme.neutralColor300,
            titleTextColor: Color = CometChatTheme.colorScheme.textColorPrimary,
            titleTextStyle: TextStyle = CometChatTheme.typography.bodyBold,
            durationTextColor: Color = CometChatTheme.colorScheme.textColorSecondary,
            durationTextStyle: TextStyle = CometChatTheme.typography.caption1Regular,
            downloadIconTint: Color = CometChatTheme.colorScheme.primary,
            toggleTextColor: Color = CometChatTheme.colorScheme.primary,
            toggleTextStyle: TextStyle = CometChatTheme.typography.bodyBold,
            captionTextColor: Color = CometChatTheme.colorScheme.textColorSecondary,
            captionTextStyle: TextStyle = CometChatTheme.typography.bodyRegular,
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
        ): CometChatAudiosBubbleStyle = CometChatAudiosBubbleStyle(
            cardBackgroundColor = cardBackgroundColor,
            cardCornerRadius = cardCornerRadius,
            itemSpacing = itemSpacing,
            playButtonBackgroundColor = playButtonBackgroundColor,
            playIconTint = playIconTint,
            seekFillColor = seekFillColor,
            seekTrackColor = seekTrackColor,
            seekKnobColor = seekKnobColor,
            seekKnobBorderColor = seekKnobBorderColor,
            titleTextColor = titleTextColor,
            titleTextStyle = titleTextStyle,
            durationTextColor = durationTextColor,
            durationTextStyle = durationTextStyle,
            downloadIconTint = downloadIconTint,
            toggleTextColor = toggleTextColor,
            toggleTextStyle = toggleTextStyle,
            captionTextColor = captionTextColor,
            captionTextStyle = captionTextStyle,
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
         * Creates a style for incoming (left-aligned) multi-audio messages.
         *
         * CommonProperties use sentinel values (filled from messageBubbleStyle during merge).
         * Content-specific properties use theme defaults for incoming messages — a primary
         * play circle with a white glyph and a subtle dark card tint.
         *
         * @return A new [CometChatAudiosBubbleStyle] configured for incoming messages
         */
        @Composable
        fun incoming(): CometChatAudiosBubbleStyle = default()

        /**
         * Creates a style for outgoing (right-aligned) multi-audio messages.
         *
         * CommonProperties use sentinel values (filled from messageBubbleStyle during merge).
         * Content-specific properties invert the play circle (white circle, primary glyph),
         * use a white-tint card overlay, and white text for contrast on the tinted outgoing
         * bubble background.
         *
         * @return A new [CometChatAudiosBubbleStyle] configured for outgoing messages
         */
        @Composable
        fun outgoing(): CometChatAudiosBubbleStyle = default(
            cardBackgroundColor = Color.White.copy(alpha = 0.16f),
            playButtonBackgroundColor = Color.White,
            playIconTint = CometChatTheme.colorScheme.primary,
            seekFillColor = Color.White,
            seekTrackColor = Color.White.copy(alpha = 0.3f),
            titleTextColor = Color.White,
            durationTextColor = Color.White,
            downloadIconTint = Color.White,
            toggleTextColor = Color.White,
            captionTextColor = Color.White
        )
    }
}
