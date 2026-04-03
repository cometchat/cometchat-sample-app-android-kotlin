package com.cometchat.uikit.compose.presentation.shared.messagebubble.style

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cometchat.uikit.compose.presentation.shared.baseelements.avatar.CometChatAvatarStyle
import com.cometchat.uikit.compose.theme.CometChatTheme

/**
 * Style configuration for [CometChatPollBubble] composable.
 *
 * This immutable data class extends [CometChatMessageBubbleStyle] and defines the visual
 * appearance of poll message bubbles, which display interactive polls with voting options
 * and results.
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
 * CometChatPollBubble(
 *     message = customMessage,
 *     alignment = MessageBubbleAlignment.LEFT,
 *     style = CometChatPollBubbleStyle.incoming()
 * )
 * ```
 *
 * @property titleTextColor The color of the poll question text
 * @property titleTextStyle The text style for the poll question
 * @property optionTextColor The color of the poll option text
 * @property optionTextStyle The text style for the poll options
 * @property selectedRadioButtonStrokeColor The stroke color of selected radio button
 * @property selectedRadioButtonStrokeWidth The stroke width of selected radio button
 * @property selectedRadioButtonCornerRadius The corner radius of selected radio button
 * @property selectedIconTint The tint color of selected option icon
 * @property unselectedRadioButtonStrokeColor The stroke color of unselected radio button
 * @property unselectedRadioButtonStrokeWidth The stroke width of unselected radio button
 * @property unselectedRadioButtonCornerRadius The corner radius of unselected radio button
 * @property unselectedIconTint The tint color of unselected option icon
 * @property progressColor The color of the vote progress bar
 * @property progressBackgroundColor The background color of the vote progress bar
 * @property progressIndeterminateTint The tint color for indeterminate progress indicator
 * @property voteCountTextColor The color of vote count text
 * @property voteCountTextStyle The text style for vote count
 * @property optionAvatarStyle The style for voter avatars displayed next to poll options
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
data class CometChatPollBubbleStyle(
    // Content-specific properties ONLY
    val titleTextColor: Color,
    val titleTextStyle: TextStyle,
    val optionTextColor: Color,
    val optionTextStyle: TextStyle,
    val selectedRadioButtonStrokeColor: Color,
    val selectedRadioButtonStrokeWidth: Dp,
    val selectedRadioButtonCornerRadius: Dp,
    val selectedIconTint: Color,
    val unselectedRadioButtonStrokeColor: Color,
    val unselectedRadioButtonStrokeWidth: Dp,
    val unselectedRadioButtonCornerRadius: Dp,
    val unselectedIconTint: Color,
    val progressColor: Color,
    val progressBackgroundColor: Color,
    val progressIndeterminateTint: Color,
    val voteCountTextColor: Color,
    val voteCountTextStyle: TextStyle,
    val optionAvatarStyle: CometChatAvatarStyle,
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
         * Creates a default poll bubble style using CometChat theme tokens.
         *
         * @param titleTextColor The color of the poll question text
         * @param titleTextStyle The text style for the poll question
         * @param optionTextColor The color of the poll option text
         * @param optionTextStyle The text style for the poll options
         * @param selectedRadioButtonStrokeColor The stroke color of selected radio button
         * @param selectedRadioButtonStrokeWidth The stroke width of selected radio button
         * @param selectedRadioButtonCornerRadius The corner radius of selected radio button
         * @param selectedIconTint The tint color of selected option icon
         * @param unselectedRadioButtonStrokeColor The stroke color of unselected radio button
         * @param unselectedRadioButtonStrokeWidth The stroke width of unselected radio button
         * @param unselectedRadioButtonCornerRadius The corner radius of unselected radio button
         * @param unselectedIconTint The tint color of unselected option icon
         * @param progressColor The color of the vote progress bar
         * @param progressBackgroundColor The background color of the vote progress bar
         * @param progressIndeterminateTint The tint color for indeterminate progress indicator
         * @param voteCountTextColor The color of vote count text
         * @param voteCountTextStyle The text style for vote count
         * @param optionAvatarStyle The style for voter avatars displayed next to poll options
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
         * @return A new [CometChatPollBubbleStyle] instance with default values
         */
        @Composable
        fun default(
            // Content-specific defaults (keep theme-based values)
            titleTextColor: Color = CometChatTheme.colorScheme.textColorPrimary,
            titleTextStyle: TextStyle = CometChatTheme.typography.bodyMedium,
            optionTextColor: Color = CometChatTheme.colorScheme.textColorPrimary,
            optionTextStyle: TextStyle = CometChatTheme.typography.bodyRegular,
            selectedRadioButtonStrokeColor: Color = CometChatTheme.colorScheme.primary,
            selectedRadioButtonStrokeWidth: Dp = 2.dp,
            selectedRadioButtonCornerRadius: Dp = 12.dp,
            selectedIconTint: Color = CometChatTheme.colorScheme.primary,
            unselectedRadioButtonStrokeColor: Color = CometChatTheme.colorScheme.iconTintSecondary,
            unselectedRadioButtonStrokeWidth: Dp = 1.dp,
            unselectedRadioButtonCornerRadius: Dp = 12.dp,
            unselectedIconTint: Color = CometChatTheme.colorScheme.iconTintSecondary,
            progressColor: Color = CometChatTheme.colorScheme.primary,
            progressBackgroundColor: Color = CometChatTheme.colorScheme.extendedPrimaryColor700,
            progressIndeterminateTint: Color = CometChatTheme.colorScheme.iconTintSecondary,
            voteCountTextColor: Color = CometChatTheme.colorScheme.textColorSecondary,
            voteCountTextStyle: TextStyle = CometChatTheme.typography.caption1Regular,
            optionAvatarStyle: CometChatAvatarStyle = CometChatAvatarStyle.default(
                cornerRadius = 10.dp // Small avatars for poll options
            ),
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
        ): CometChatPollBubbleStyle = CometChatPollBubbleStyle(
            titleTextColor = titleTextColor,
            titleTextStyle = titleTextStyle,
            optionTextColor = optionTextColor,
            optionTextStyle = optionTextStyle,
            selectedRadioButtonStrokeColor = selectedRadioButtonStrokeColor,
            selectedRadioButtonStrokeWidth = selectedRadioButtonStrokeWidth,
            selectedRadioButtonCornerRadius = selectedRadioButtonCornerRadius,
            selectedIconTint = selectedIconTint,
            unselectedRadioButtonStrokeColor = unselectedRadioButtonStrokeColor,
            unselectedRadioButtonStrokeWidth = unselectedRadioButtonStrokeWidth,
            unselectedRadioButtonCornerRadius = unselectedRadioButtonCornerRadius,
            unselectedIconTint = unselectedIconTint,
            progressColor = progressColor,
            progressBackgroundColor = progressBackgroundColor,
            progressIndeterminateTint = progressIndeterminateTint,
            voteCountTextColor = voteCountTextColor,
            voteCountTextStyle = voteCountTextStyle,
            optionAvatarStyle = optionAvatarStyle,
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
         * Creates a style for incoming (left-aligned) poll messages.
         *
         * CommonProperties use sentinel values (filled from messageBubbleStyle during merge).
         * Content-specific properties use theme defaults for incoming messages.
         *
         * @return A new [CometChatPollBubbleStyle] configured for incoming messages
         */
        @Composable
        fun incoming(): CometChatPollBubbleStyle = default()

        /**
         * Creates a style for outgoing (right-aligned) poll messages.
         *
         * CommonProperties use sentinel values (filled from messageBubbleStyle during merge).
         * Content-specific properties use white colors for text and icons
         * to contrast with the typically darker outgoing message bubble background.
         *
         * @return A new [CometChatPollBubbleStyle] configured for outgoing messages
         */
        @Composable
        fun outgoing(): CometChatPollBubbleStyle = default(
            titleTextColor = CometChatTheme.colorScheme.colorWhite,
            optionTextColor = CometChatTheme.colorScheme.colorWhite,
            selectedRadioButtonStrokeColor = CometChatTheme.colorScheme.colorWhite,
            selectedIconTint = CometChatTheme.colorScheme.colorWhite,
            unselectedRadioButtonStrokeColor = CometChatTheme.colorScheme.colorWhite,
            unselectedIconTint = CometChatTheme.colorScheme.colorWhite,
            progressColor = CometChatTheme.colorScheme.colorWhite,
            progressBackgroundColor = CometChatTheme.colorScheme.extendedPrimaryColor700,
            voteCountTextColor = CometChatTheme.colorScheme.colorWhite
        )
    }
}
