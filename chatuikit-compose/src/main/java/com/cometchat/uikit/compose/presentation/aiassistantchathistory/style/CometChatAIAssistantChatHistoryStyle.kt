package com.cometchat.uikit.compose.presentation.aiassistantchathistory.style

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import com.cometchat.uikit.compose.R
import com.cometchat.uikit.compose.theme.CometChatTheme

/**
 * Immutable style configuration for CometChatAIAssistantChatHistory.
 * Contains all 21 visual styling properties for the AI assistant chat history component,
 * covering the component background, header section, new chat section, date separator,
 * message item, and delete option (popup menu).
 *
 * Uses Compose-native types: [Color] for colors, [TextStyle] for text appearances,
 * and [Painter] for icons.
 */
@Immutable
data class CometChatAIAssistantChatHistoryStyle(
    // Component background
    val chatHistoryBackgroundColor: Color,

    // Header section
    val chatHistoryHeaderBackgroundColor: Color,
    val chatHistoryHeaderTextColor: Color,
    val chatHistoryHeaderTextStyle: TextStyle,
    val chatHistoryHeaderCloseIcon: Painter?,
    val chatHistoryHeaderCloseIconTint: Color,

    // New Chat section
    val newChatBackgroundColor: Color,
    val newChatTextColor: Color,
    val newChatTextStyle: TextStyle,
    val newChatIcon: Painter?,
    val newChatIconTint: Color,

    // Date separator
    val dateSeparatorBackgroundColor: Color,
    val dateSeparatorTextColor: Color,
    val dateSeparatorTextStyle: TextStyle,

    // Message item
    val itemBackgroundColor: Color,
    val itemTextColor: Color,
    val itemTextStyle: TextStyle,

    // Delete option (popup menu)
    val deleteOptionIcon: Painter?,
    val deleteOptionIconTint: Color,
    val deleteOptionTextColor: Color,
    val deleteOptionTextStyle: TextStyle
) {
    companion object {
        /**
         * Creates a default style configuration sourcing values from CometChatTheme.
         *
         * Theme token mapping:
         * - Backgrounds: backgroundColor3
         * - Text colors: textColorPrimary (header, new chat, item, delete option), textColorTertiary (date separator)
         * - Icon tints: iconTintSecondary (header close, new chat), errorColor (delete option)
         * - Typography: heading4Medium (header, new chat), caption1Medium (date separator),
         *   bodyRegular (item, delete option)
         */
        @Composable
        fun default(
            // Component background
            chatHistoryBackgroundColor: Color = CometChatTheme.colorScheme.backgroundColor3,

            // Header section
            chatHistoryHeaderBackgroundColor: Color = CometChatTheme.colorScheme.backgroundColor3,
            chatHistoryHeaderTextColor: Color = CometChatTheme.colorScheme.textColorPrimary,
            chatHistoryHeaderTextStyle: TextStyle = CometChatTheme.typography.heading4Medium,
            chatHistoryHeaderCloseIcon: Painter? = painterResource(R.drawable.cometchat_ic_close),
            chatHistoryHeaderCloseIconTint: Color = CometChatTheme.colorScheme.iconTintSecondary,

            // New Chat section
            newChatBackgroundColor: Color = CometChatTheme.colorScheme.backgroundColor3,
            newChatTextColor: Color = CometChatTheme.colorScheme.textColorPrimary,
            newChatTextStyle: TextStyle = CometChatTheme.typography.buttonRegular,
            newChatIcon: Painter? = painterResource(R.drawable.cometchat_ic_add),
            newChatIconTint: Color = CometChatTheme.colorScheme.iconTintSecondary,

            // Date separator
            dateSeparatorBackgroundColor: Color = Color.Transparent,
            dateSeparatorTextColor: Color = CometChatTheme.colorScheme.textColorTertiary,
            dateSeparatorTextStyle: TextStyle = CometChatTheme.typography.caption1Medium,

            // Message item
            itemBackgroundColor: Color = CometChatTheme.colorScheme.backgroundColor3,
            itemTextColor: Color = CometChatTheme.colorScheme.textColorPrimary,
            itemTextStyle: TextStyle = CometChatTheme.typography.bodyRegular,

            // Delete option (popup menu)
            deleteOptionIcon: Painter? = painterResource(R.drawable.cometchat_ic_delete),
            deleteOptionIconTint: Color = CometChatTheme.colorScheme.errorColor,
            deleteOptionTextColor: Color = CometChatTheme.colorScheme.textColorPrimary,
            deleteOptionTextStyle: TextStyle = CometChatTheme.typography.bodyRegular
        ): CometChatAIAssistantChatHistoryStyle = CometChatAIAssistantChatHistoryStyle(
            chatHistoryBackgroundColor = chatHistoryBackgroundColor,
            chatHistoryHeaderBackgroundColor = chatHistoryHeaderBackgroundColor,
            chatHistoryHeaderTextColor = chatHistoryHeaderTextColor,
            chatHistoryHeaderTextStyle = chatHistoryHeaderTextStyle,
            chatHistoryHeaderCloseIcon = chatHistoryHeaderCloseIcon,
            chatHistoryHeaderCloseIconTint = chatHistoryHeaderCloseIconTint,
            newChatBackgroundColor = newChatBackgroundColor,
            newChatTextColor = newChatTextColor,
            newChatTextStyle = newChatTextStyle,
            newChatIcon = newChatIcon,
            newChatIconTint = newChatIconTint,
            dateSeparatorBackgroundColor = dateSeparatorBackgroundColor,
            dateSeparatorTextColor = dateSeparatorTextColor,
            dateSeparatorTextStyle = dateSeparatorTextStyle,
            itemBackgroundColor = itemBackgroundColor,
            itemTextColor = itemTextColor,
            itemTextStyle = itemTextStyle,
            deleteOptionIcon = deleteOptionIcon,
            deleteOptionIconTint = deleteOptionIconTint,
            deleteOptionTextColor = deleteOptionTextColor,
            deleteOptionTextStyle = deleteOptionTextStyle
        )
    }
}
