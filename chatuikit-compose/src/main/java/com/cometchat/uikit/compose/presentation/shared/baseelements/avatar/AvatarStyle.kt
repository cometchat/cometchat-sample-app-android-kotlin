package com.cometchat.uikit.compose.presentation.shared.baseelements.avatar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cometchat.uikit.compose.theme.CometChatTheme

/**
 * Configuration class for customizing the appearance of CometChatAvatar.
 *
 * @param backgroundColor Background color for the avatar when displaying initials.
 *                        If null, uses extendedPrimaryColor500 from theme
 * @param borderColor Color of the avatar border/stroke. If null, no border is shown
 * @param borderWidth Width of the avatar border. Default is 0.dp (no border)
 * @param cornerRadius Corner radius of the avatar. Use 50.dp or higher for circular avatar. Default is 50.dp
 * @param textColor Color of the initials text. If null, uses primaryButtonIconTint from theme
 * @param textStyle Typography style for the initials text. If null, uses bodyBold from theme
 */
@Immutable
data class CometChatAvatarStyle(
    val backgroundColor: Color? = null,
    val borderColor: Color? = null,
    val borderWidth: Dp = 0.dp,
    val cornerRadius: Dp = 0.dp,
    val textColor: Color? = null,
    val textStyle: TextStyle? = null
) {
    companion object {
        /**
         * Creates a default CometChatAvatarStyle with values sourced from CometChatTheme.
         *
         * @param backgroundColor Background color for the avatar when displaying initials
         * @param borderColor Color of the avatar border/stroke
         * @param borderWidth Width of the avatar border
         * @param cornerRadius Corner radius of the avatar
         * @param textColor Color of the initials text
         * @param textStyle Typography style for the initials text
         * @return A new CometChatAvatarStyle instance with theme-based default values
         */
        @Composable
        fun default(
            backgroundColor: Color = CometChatTheme.colorScheme.extendedPrimaryColor500,
            borderColor: Color? = null,
            borderWidth: Dp = 0.dp,
            cornerRadius: Dp = 24.dp,
            textColor: Color = CometChatTheme.colorScheme.primaryButtonIconTint,
            textStyle: TextStyle = CometChatTheme.typography.heading2Bold
        ): CometChatAvatarStyle = CometChatAvatarStyle(
            backgroundColor = backgroundColor,
            borderColor = borderColor,
            borderWidth = borderWidth,
            cornerRadius = cornerRadius,
            textColor = textColor,
            textStyle = textStyle
        )
    }
}

/**
 * Type alias for backward compatibility with code using AvatarStyle.
 */
@Deprecated("Use CometChatAvatarStyle instead", ReplaceWith("CometChatAvatarStyle"))
typealias AvatarStyle = CometChatAvatarStyle