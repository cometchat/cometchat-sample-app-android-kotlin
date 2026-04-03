package com.cometchat.uikit.compose.presentation.reactionlist.style

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cometchat.uikit.compose.theme.CometChatTheme

/**
 * Style configuration for CometChatReactionList component.
 *
 * This immutable data class encapsulates all visual styling properties for the reaction list,
 * following Kotlin standards and integrating with the CometChatTheme system.
 *
 * @param backgroundColor Background color for the reaction list container
 * @param strokeColor Stroke/border color for the container
 * @param strokeWidth Width of the container stroke/border
 * @param cornerRadius Corner radius of the container
 * @param tabTextColor Text color for inactive tabs
 * @param tabTextActiveColor Text color for the active tab
 * @param tabTextStyle Text style for tab labels
 * @param tabActiveIndicatorColor Color of the active tab indicator (underline)
 * @param errorTextColor Text color for error messages
 * @param errorTextStyle Text style for error messages
 * @param separatorColor Color for the separator between header and list
 * @param separatorHeight Height of the separator
 * @param itemStyle Style configuration for individual reaction list items
 */
@Immutable
data class CometChatReactionListStyle(
    val backgroundColor: Color,
    val strokeColor: Color,
    val strokeWidth: Dp,
    val cornerRadius: Dp,
    val tabTextColor: Color,
    val tabTextActiveColor: Color,
    val tabTextStyle: TextStyle,
    val tabActiveIndicatorColor: Color,
    val errorTextColor: Color,
    val errorTextStyle: TextStyle,
    val separatorColor: Color,
    val separatorHeight: Dp,
    val itemStyle: CometChatReactionListItemStyle
) {
    companion object {
        /**
         * Creates a default CometChatReactionListStyle with values sourced from CometChatTheme.
         *
         * @return A new CometChatReactionListStyle instance with theme-based default values
         */
        @Composable
        fun default(
            backgroundColor: Color = CometChatTheme.colorScheme.backgroundColor1,
            strokeColor: Color = CometChatTheme.colorScheme.strokeColorLight,
            strokeWidth: Dp = 2.dp,
            cornerRadius: Dp = 16.dp,
            tabTextColor: Color = CometChatTheme.colorScheme.textColorSecondary,
            tabTextActiveColor: Color = CometChatTheme.colorScheme.textColorHighlight,
            tabTextStyle: TextStyle = CometChatTheme.typography.bodyMedium,
            tabActiveIndicatorColor: Color = CometChatTheme.colorScheme.primary,
            errorTextColor: Color = CometChatTheme.colorScheme.errorColor,
            errorTextStyle: TextStyle = CometChatTheme.typography.bodyRegular,
            separatorColor: Color = CometChatTheme.colorScheme.strokeColorDefault,
            separatorHeight: Dp = 2.dp,
            itemStyle: CometChatReactionListItemStyle = CometChatReactionListItemStyle.default()
        ): CometChatReactionListStyle = CometChatReactionListStyle(
            backgroundColor = backgroundColor,
            strokeColor = strokeColor,
            strokeWidth = strokeWidth,
            cornerRadius = cornerRadius,
            tabTextColor = tabTextColor,
            tabTextActiveColor = tabTextActiveColor,
            tabTextStyle = tabTextStyle,
            tabActiveIndicatorColor = tabActiveIndicatorColor,
            errorTextColor = errorTextColor,
            errorTextStyle = errorTextStyle,
            separatorColor = separatorColor,
            separatorHeight = separatorHeight,
            itemStyle = itemStyle
        )
    }
}
