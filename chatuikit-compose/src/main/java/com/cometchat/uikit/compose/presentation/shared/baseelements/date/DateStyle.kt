package com.cometchat.uikit.compose.presentation.shared.baseelements.date

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.uikit.compose.theme.CometChatTypography

/**
 * Configuration class for customizing the appearance of CometChatDate.
 *
 * @param backgroundColor Background color for the date container.
 *                        If null, uses Color.Transparent by default
 * @param borderColor Color of the date border/stroke. If null, no border is shown
 * @param borderWidth Width of the date border. Default is 0.dp (no border)
 * @param cornerRadius Corner radius of the date container. Default is 0.dp
 * @param textColor Color of the date text. If null, uses textColorPrimary from theme
 * @param textStyle Typography style for the date text. If null, uses bodyRegular from theme
 * @param textAlign Text alignment for the date text. Default is TextAlign.Center
 * @param typography Custom typography. If null, uses theme's typography
 */
@Immutable
data class DateStyle(
    val backgroundColor: Color? = null,
    val borderColor: Color? = null,
    val borderWidth: Dp = 0.dp,
    val cornerRadius: Dp = 0.dp,
    val textColor: Color? = null,
    val textStyle: TextStyle? = null,
    val textAlign: TextAlign = TextAlign.Center,
    val typography: CometChatTypography? = null
) {
    companion object {
        /**
         * Creates a default DateStyle with values sourced from CometChatTheme.
         *
         * @param backgroundColor Background color for the date container
         * @param borderColor Color of the date border/stroke
         * @param borderWidth Width of the date border
         * @param cornerRadius Corner radius of the date container
         * @param textColor Color of the date text
         * @param textStyle Typography style for the date text
         * @param textAlign Text alignment for the date text
         * @param typography Custom typography
         * @return A new DateStyle instance with theme-based default values
         */
        @Composable
        fun default(
            backgroundColor: Color = Color.Transparent,
            borderColor: Color? = null,
            borderWidth: Dp = 0.dp,
            cornerRadius: Dp = 0.dp,
            textColor: Color = CometChatTheme.colorScheme.textColorSecondary,
            textStyle: TextStyle = CometChatTheme.typography.caption1Regular,
            textAlign: TextAlign = TextAlign.Center,
            typography: CometChatTypography? = null
        ): DateStyle = DateStyle(
            backgroundColor = backgroundColor,
            borderColor = borderColor,
            borderWidth = borderWidth,
            cornerRadius = cornerRadius,
            textColor = textColor,
            textStyle = textStyle,
            textAlign = textAlign,
            typography = typography
        )
    }
}
