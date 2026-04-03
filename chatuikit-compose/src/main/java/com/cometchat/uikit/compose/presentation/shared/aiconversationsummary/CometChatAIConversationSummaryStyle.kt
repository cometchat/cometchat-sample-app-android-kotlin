package com.cometchat.uikit.compose.presentation.shared.aiconversationsummary

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cometchat.uikit.compose.theme.CometChatTheme

/**
 * Style configuration for CometChatAIConversationSummaryView component.
 *
 * This class defines all visual properties for the AI conversation summary view,
 * including container styling, title styling, close icon styling, item styling,
 * error state styling, and shimmer effects.
 *
 * @property backgroundColor Background color of the main container
 * @property cornerRadius Corner radius of the main container
 * @property strokeWidth Border/stroke width of the main container
 * @property strokeColor Border/stroke color of the main container
 * @property maxHeight Maximum height of the view (0.dp for no limit)
 * @property titleTextColor Text color for the title
 * @property titleTextStyle Text style for the title
 * @property closeIconTint Tint color for the close icon
 * @property itemBackgroundColor Background color of the summary content card
 * @property itemCornerRadius Corner radius of the summary content card
 * @property itemStrokeWidth Border/stroke width of the summary content card
 * @property itemStrokeColor Border/stroke color of the summary content card
 * @property itemTextColor Text color for the summary text
 * @property itemTextStyle Text style for the summary text
 * @property errorStateTextColor Text color for error state message
 * @property errorStateTextStyle Text style for error state message
 * @property shimmerBaseColor Base color for shimmer loading effect
 * @property shimmerHighlightColor Highlight color for shimmer loading effect
 */
@Immutable
data class CometChatAIConversationSummaryStyle(
    val backgroundColor: Color,
    val cornerRadius: Dp,
    val strokeWidth: Dp,
    val strokeColor: Color,
    val maxHeight: Dp,
    val titleTextColor: Color,
    val titleTextStyle: TextStyle,
    val closeIconTint: Color,
    val itemBackgroundColor: Color,
    val itemCornerRadius: Dp,
    val itemStrokeWidth: Dp,
    val itemStrokeColor: Color,
    val itemTextColor: Color,
    val itemTextStyle: TextStyle,
    val errorStateTextColor: Color,
    val errorStateTextStyle: TextStyle,
    val shimmerBaseColor: Color,
    val shimmerHighlightColor: Color
) {
    companion object {
        /**
         * Creates a default CometChatAIConversationSummaryStyle using CometChatTheme colors.
         *
         * @param backgroundColor Background color of the main container
         * @param cornerRadius Corner radius of the main container
         * @param strokeWidth Border/stroke width of the main container
         * @param strokeColor Border/stroke color of the main container
         * @param maxHeight Maximum height of the view (0.dp for no limit)
         * @param titleTextColor Text color for the title
         * @param titleTextStyle Text style for the title
         * @param closeIconTint Tint color for the close icon
         * @param itemBackgroundColor Background color of the summary content card
         * @param itemCornerRadius Corner radius of the summary content card
         * @param itemStrokeWidth Border/stroke width of the summary content card
         * @param itemStrokeColor Border/stroke color of the summary content card
         * @param itemTextColor Text color for the summary text
         * @param itemTextStyle Text style for the summary text
         * @param errorStateTextColor Text color for error state message
         * @param errorStateTextStyle Text style for error state message
         * @param shimmerBaseColor Base color for shimmer loading effect
         * @param shimmerHighlightColor Highlight color for shimmer loading effect
         */
        @Composable
        fun default(
            backgroundColor: Color = CometChatTheme.colorScheme.backgroundColor1,
            cornerRadius: Dp = 8.dp,
            strokeWidth: Dp = 1.dp,
            strokeColor: Color = CometChatTheme.colorScheme.borderColorLight,
            maxHeight: Dp = 0.dp,
            titleTextColor: Color = CometChatTheme.colorScheme.textColorPrimary,
            titleTextStyle: TextStyle = CometChatTheme.typography.heading4Bold,
            closeIconTint: Color = CometChatTheme.colorScheme.iconTintPrimary,
            itemBackgroundColor: Color = CometChatTheme.colorScheme.backgroundColor2,
            itemCornerRadius: Dp = 8.dp,
            itemStrokeWidth: Dp = 0.dp,
            itemStrokeColor: Color = Color.Transparent,
            itemTextColor: Color = CometChatTheme.colorScheme.textColorPrimary,
            itemTextStyle: TextStyle = CometChatTheme.typography.bodyRegular,
            errorStateTextColor: Color = CometChatTheme.colorScheme.textColorSecondary,
            errorStateTextStyle: TextStyle = CometChatTheme.typography.bodyRegular,
            shimmerBaseColor: Color = CometChatTheme.colorScheme.backgroundColor3,
            shimmerHighlightColor: Color = CometChatTheme.colorScheme.backgroundColor1
        ): CometChatAIConversationSummaryStyle = CometChatAIConversationSummaryStyle(
            backgroundColor = backgroundColor,
            cornerRadius = cornerRadius,
            strokeWidth = strokeWidth,
            strokeColor = strokeColor,
            maxHeight = maxHeight,
            titleTextColor = titleTextColor,
            titleTextStyle = titleTextStyle,
            closeIconTint = closeIconTint,
            itemBackgroundColor = itemBackgroundColor,
            itemCornerRadius = itemCornerRadius,
            itemStrokeWidth = itemStrokeWidth,
            itemStrokeColor = itemStrokeColor,
            itemTextColor = itemTextColor,
            itemTextStyle = itemTextStyle,
            errorStateTextColor = errorStateTextColor,
            errorStateTextStyle = errorStateTextStyle,
            shimmerBaseColor = shimmerBaseColor,
            shimmerHighlightColor = shimmerHighlightColor
        )
    }
}
