package com.cometchat.uikit.compose.presentation.shared.reaction

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cometchat.uikit.compose.theme.CometChatTheme

/**
 * Style configuration for CometChatReaction component.
 *
 * This data class holds all styling properties for the reaction view,
 * matching the XML attributes defined in attr_cometchat_reaction.xml.
 *
 * @property emojiTextStyle The text style for emoji display
 * @property emojiTextColor The color for emoji text
 * @property countTextStyle The text style for reaction count
 * @property countTextColor The color for reaction count text
 * @property backgroundColor The background color of the reaction chip
 * @property strokeWidth The border stroke width
 * @property strokeColor The border stroke color
 * @property cornerRadius The corner radius of the reaction chip
 * @property elevation The elevation of the reaction chip
 * @property activeBackgroundColor The background color when the reaction is active (selected by current user)
 * @property activeStrokeWidth The border stroke width when active
 * @property activeStrokeColor The border stroke color when active
 */
@Immutable
data class CometChatReactionStyle(
    val emojiTextStyle: TextStyle,
    val emojiTextColor: Color,
    val countTextStyle: TextStyle,
    val countTextColor: Color,
    val backgroundColor: Color,
    val strokeWidth: Dp,
    val strokeColor: Color,
    val cornerRadius: Dp,
    val elevation: Dp,
    val activeBackgroundColor: Color,
    val activeStrokeWidth: Dp,
    val activeStrokeColor: Color
) {
    companion object {
        /**
         * Creates a default reaction style using CometChat theme tokens.
         */
        @Composable
        fun default(
            emojiTextStyle: TextStyle = CometChatTheme.typography.bodyRegular,
            emojiTextColor: Color = CometChatTheme.colorScheme.textColorPrimary,
            countTextStyle: TextStyle = CometChatTheme.typography.caption1Regular,
            countTextColor: Color = CometChatTheme.colorScheme.textColorSecondary,
            backgroundColor: Color = CometChatTheme.colorScheme.backgroundColor1,
            strokeWidth: Dp = 0.dp,
            strokeColor: Color = CometChatTheme.colorScheme.borderColorDefault,
            cornerRadius: Dp = 0.dp,
            elevation: Dp = 0.dp,
            activeBackgroundColor: Color = CometChatTheme.colorScheme.backgroundColor3,
            activeStrokeWidth: Dp = 0.dp,
            activeStrokeColor: Color = CometChatTheme.colorScheme.primary
        ): CometChatReactionStyle = CometChatReactionStyle(
            emojiTextStyle = emojiTextStyle,
            emojiTextColor = emojiTextColor,
            countTextStyle = countTextStyle,
            countTextColor = countTextColor,
            backgroundColor = backgroundColor,
            strokeWidth = strokeWidth,
            strokeColor = strokeColor,
            cornerRadius = cornerRadius,
            elevation = elevation,
            activeBackgroundColor = activeBackgroundColor,
            activeStrokeWidth = activeStrokeWidth,
            activeStrokeColor = activeStrokeColor
        )
    }
}
