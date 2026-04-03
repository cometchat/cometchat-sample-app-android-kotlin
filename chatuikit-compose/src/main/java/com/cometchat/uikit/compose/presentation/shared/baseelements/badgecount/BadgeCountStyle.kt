package com.cometchat.uikit.compose.presentation.shared.baseelements.badgecount

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.uikit.compose.theme.CometChatTypography

/**
 * Configuration class for customizing the appearance of CometChatBadgeCount.
 *
 * @param backgroundColor Background color for the badge.
 *                        If null, uses primaryColor from theme
 * @param borderColor Color of the badge border/stroke. If null, no border is shown
 * @param borderWidth Width of the badge border. Default is 0.dp (no border)
 * @param cornerRadius Corner radius of the badge. Use 50% of size for circular badge. Default is 0.dp
 * @param textColor Color of the count text. If null, uses primaryButtonIconTint from theme
 * @param textStyle Typography style for the count text. If null, uses caption1 from theme
 * @param size Size of the badge. Default is 24.dp
 * @param typography Custom typography. If null, uses theme's typography
 */
@Immutable
data class BadgeCountStyle(
    val backgroundColor: Color? = null,
    val borderColor: Color? = null,
    val borderWidth: Dp = 0.dp,
    val cornerRadius: Dp = 0.dp,
    val textColor: Color? = null,
    val textStyle: TextStyle? = null,
    val size: Dp = 24.dp,
    val typography: CometChatTypography? = null
) {
    companion object {
        /**
         * Creates a default BadgeCountStyle with values sourced from CometChatTheme.
         *
         * @param backgroundColor Background color for the badge
         * @param borderColor Color of the badge border/stroke
         * @param borderWidth Width of the badge border
         * @param cornerRadius Corner radius of the badge
         * @param textColor Color of the count text
         * @param textStyle Typography style for the count text
         * @param size Size of the badge
         * @param typography Custom typography
         * @return A new BadgeCountStyle instance with theme-based default values
         */
        @Composable
        fun default(
            backgroundColor: Color = CometChatTheme.colorScheme.primary,
            borderColor: Color? = null,
            borderWidth: Dp = 0.dp,
            cornerRadius: Dp = 12.dp,
            textColor: Color = CometChatTheme.colorScheme.primaryButtonIconTint,
            textStyle: TextStyle = CometChatTheme.typography.caption1Regular,
            size: Dp = 24.dp,
            typography: CometChatTypography? = null
        ): BadgeCountStyle = BadgeCountStyle(
            backgroundColor = backgroundColor,
            borderColor = borderColor,
            borderWidth = borderWidth,
            cornerRadius = cornerRadius,
            textColor = textColor,
            textStyle = textStyle,
            size = size,
            typography = typography
        )
    }
}