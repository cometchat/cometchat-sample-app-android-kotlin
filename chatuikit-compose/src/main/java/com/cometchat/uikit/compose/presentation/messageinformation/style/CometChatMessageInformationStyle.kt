package com.cometchat.uikit.compose.presentation.messageinformation.style

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cometchat.uikit.compose.presentation.shared.baseelements.avatar.CometChatAvatarStyle
import com.cometchat.uikit.compose.presentation.shared.receipts.CometChatReceiptsStyle
import com.cometchat.uikit.compose.theme.CometChatTheme

/**
 * Style configuration for CometChatMessageInformation component.
 *
 * This immutable data class encapsulates all visual styling properties for the message information
 * bottom sheet, following Kotlin standards and integrating with the CometChatTheme system.
 *
 * Per design doc: Style Parameters section with 18 style parameters.
 *
 * @param titleTextStyle Text style for toolbar title
 * @param titleTextColor Color for toolbar title
 * @param backgroundColor Background color of the sheet
 * @param backgroundHighlightColor Background color behind message bubble
 * @param cornerRadius Corner radius for top corners
 * @param strokeWidth Border stroke width
 * @param strokeColor Border stroke color
 * @param itemNameTextStyle Text style for user name in list items
 * @param itemNameTextColor Color for user name in list items
 * @param itemReadTextStyle Text style for "Read" label
 * @param itemReadTextColor Color for "Read" label
 * @param itemReadDateTextStyle Text style for read timestamp
 * @param itemReadDateTextColor Color for read timestamp
 * @param itemDeliveredTextStyle Text style for "Delivered" label
 * @param itemDeliveredTextColor Color for "Delivered" label
 * @param itemDeliveredDateTextStyle Text style for delivered timestamp
 * @param itemDeliveredDateTextColor Color for delivered timestamp
 * @param itemAvatarStyle Style for avatar in list items
 * @param messageReceiptStyle Style for receipt icons (USER view)
 * @param separatorColor Color for separator lines
 * @param separatorHeight Height of separator lines
 */
@Immutable
data class CometChatMessageInformationStyle(
    // Container styling
    val backgroundColor: Color,
    val backgroundHighlightColor: Color,
    val cornerRadius: Dp,
    val strokeWidth: Dp,
    val strokeColor: Color,
    
    // Title styling
    val titleTextStyle: TextStyle,
    val titleTextColor: Color,
    
    // Item name styling
    val itemNameTextStyle: TextStyle,
    val itemNameTextColor: Color,
    
    // Read section styling
    val itemReadTextStyle: TextStyle,
    val itemReadTextColor: Color,
    val itemReadDateTextStyle: TextStyle,
    val itemReadDateTextColor: Color,
    
    // Delivered section styling
    val itemDeliveredTextStyle: TextStyle,
    val itemDeliveredTextColor: Color,
    val itemDeliveredDateTextStyle: TextStyle,
    val itemDeliveredDateTextColor: Color,
    
    // Component styles
    val itemAvatarStyle: CometChatAvatarStyle,
    val messageReceiptStyle: CometChatReceiptsStyle,
    
    // Separator styling
    val separatorColor: Color,
    val separatorHeight: Dp
) {
    companion object {
        /**
         * Creates a default CometChatMessageInformationStyle with values sourced from CometChatTheme.
         *
         * @return A new CometChatMessageInformationStyle instance with theme-based default values
         */
        @Composable
        fun default(
            // Container styling
            backgroundColor: Color = CometChatTheme.colorScheme.backgroundColor1,
            backgroundHighlightColor: Color = CometChatTheme.colorScheme.backgroundColor2,
            cornerRadius: Dp = 16.dp,
            strokeWidth: Dp = 0.dp,
            strokeColor: Color = CometChatTheme.colorScheme.strokeColorLight,
            
            // Title styling
            titleTextStyle: TextStyle = CometChatTheme.typography.heading2Bold,
            titleTextColor: Color = CometChatTheme.colorScheme.textColorPrimary,
            
            // Item name styling
            itemNameTextStyle: TextStyle = CometChatTheme.typography.heading4Medium,
            itemNameTextColor: Color = CometChatTheme.colorScheme.textColorPrimary,
            
            // Read section styling
            // For GROUP view: uses caption1Regular for both label and timestamp
            // For USER view: uses bodyRegular for label (handled in component)
            itemReadTextStyle: TextStyle = CometChatTheme.typography.caption1Regular,
            itemReadTextColor: Color = CometChatTheme.colorScheme.textColorSecondary,
            itemReadDateTextStyle: TextStyle = CometChatTheme.typography.caption1Regular,
            itemReadDateTextColor: Color = CometChatTheme.colorScheme.textColorSecondary,
            
            // Delivered section styling
            // For GROUP view: uses caption1Regular for both label and timestamp
            // For USER view: uses bodyRegular for label (handled in component)
            itemDeliveredTextStyle: TextStyle = CometChatTheme.typography.caption1Regular,
            itemDeliveredTextColor: Color = CometChatTheme.colorScheme.textColorSecondary,
            itemDeliveredDateTextStyle: TextStyle = CometChatTheme.typography.caption1Regular,
            itemDeliveredDateTextColor: Color = CometChatTheme.colorScheme.textColorSecondary,
            
            // Component styles
            itemAvatarStyle: CometChatAvatarStyle = CometChatAvatarStyle.default(),
            messageReceiptStyle: CometChatReceiptsStyle = CometChatReceiptsStyle.default(),
            
            // Separator styling
            separatorColor: Color = CometChatTheme.colorScheme.strokeColorLight,
            separatorHeight: Dp = 1.dp
        ): CometChatMessageInformationStyle = CometChatMessageInformationStyle(
            backgroundColor = backgroundColor,
            backgroundHighlightColor = backgroundHighlightColor,
            cornerRadius = cornerRadius,
            strokeWidth = strokeWidth,
            strokeColor = strokeColor,
            titleTextStyle = titleTextStyle,
            titleTextColor = titleTextColor,
            itemNameTextStyle = itemNameTextStyle,
            itemNameTextColor = itemNameTextColor,
            itemReadTextStyle = itemReadTextStyle,
            itemReadTextColor = itemReadTextColor,
            itemReadDateTextStyle = itemReadDateTextStyle,
            itemReadDateTextColor = itemReadDateTextColor,
            itemDeliveredTextStyle = itemDeliveredTextStyle,
            itemDeliveredTextColor = itemDeliveredTextColor,
            itemDeliveredDateTextStyle = itemDeliveredDateTextStyle,
            itemDeliveredDateTextColor = itemDeliveredDateTextColor,
            itemAvatarStyle = itemAvatarStyle,
            messageReceiptStyle = messageReceiptStyle,
            separatorColor = separatorColor,
            separatorHeight = separatorHeight
        )
    }
}
