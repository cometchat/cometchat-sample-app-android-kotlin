package com.cometchat.uikit.compose.presentation.shared.baseelements.badgecount

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cometchat.uikit.compose.theme.CometChatTheme


/**
 * CometChatBadgeCount is a composable that displays a badge with a count.
 * It represents a count inside a card view and can be highly customized.
 *
 * Features:
 * - Displays count in a badge format
 * - Handles counts up to 999 with "+99" overflow indicator when count >= 1000
 * - Fully customizable styling through BadgeCountStyle
 * - Supports custom background colors, text colors, and borders
 * - Configurable size and shape
 *
 * @param modifier Modifier for the badge container
 * @param count The count value to display (will show "+99" if count >= 1000)
 * @param style Styling configuration for the badge. Use BadgeCountStyle.default() for theme-based defaults
 *
 * @sample
 * ```
 * // Basic badge with count and default styling
 * CometChatBadgeCount(
 *     count = 5,
 *     style = BadgeCountStyle.default()
 * )
 *
 * // Badge with custom styling
 * CometChatBadgeCount(
 *     count = 42,
 *     style = BadgeCountStyle.default(
 *         backgroundColor = Color.Red,
 *         textColor = Color.White,
 *         cornerRadius = 12.dp,
 *         borderColor = Color.DarkRed,
 *         borderWidth = 1.dp
 *     )
 * )
 *
 * // Circular badge
 * CometChatBadgeCount(
 *     count = 999,
 *     style = BadgeCountStyle.default(
 *         size = 32.dp,
 *         cornerRadius = 16.dp
 *     )
 * )
 * ```
 */
@Composable
fun CometChatBadgeCount(
    modifier: Modifier = Modifier,
    count: Int = 0,
    style: BadgeCountStyle = BadgeCountStyle.default()
) {
    val typography = style.typography ?: CometChatTheme.typography
    
    // Use style values directly - defaults are now in BadgeCountStyle.default()
    val bgColor = style.backgroundColor ?: CometChatTheme.colorScheme.primary
    val txtColor = style.textColor ?: CometChatTheme.colorScheme.primaryButtonIconTint
    val txtStyle = style.textStyle ?: typography.caption1Regular
    
    // Format count display - show "+99" only when count is 1000 or above
    val countText = when {
        count < 1000 -> count.toString()
        else -> "999+"
    }
    
    // Determine shape
    val shape = RoundedCornerShape(style.cornerRadius)
    
    Box(
        modifier = modifier
            .heightIn(min = style.size)
            .widthIn(min = style.size)
            .clip(shape)
            .background(bgColor)
            .then(
                if (style.borderWidth > 0.dp && style.borderColor != null) {
                    Modifier.border(style.borderWidth, style.borderColor, shape)
                } else {
                    Modifier
                }
            )
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = countText,
            color = txtColor,
            style = txtStyle,
            textAlign = TextAlign.Center
        )
    }
}
