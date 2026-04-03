package com.cometchat.uikit.compose.presentation.shared.moderation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import com.cometchat.uikit.compose.theme.CometChatTheme

/**
 * Style configuration for CometChatModerationView component.
 *
 * This data class holds all styling properties for the moderation view,
 * matching the XML attributes defined in attr_cometchat_moderation_view.xml.
 *
 * @property backgroundColor The background color of the moderation view
 * @property textColor The color of the moderation text
 * @property textStyle The text style for the moderation text
 * @property iconTint The tint color for the moderation icon
 */
@Immutable
data class CometChatModerationViewStyle(
    val backgroundColor: Color,
    val textColor: Color,
    val textStyle: TextStyle,
    val iconTint: Color
) {
    companion object {
        /**
         * Creates a default moderation view style using CometChat theme tokens.
         */
        @Composable
        fun default(
            backgroundColor: Color = CometChatTheme.colorScheme.backgroundColor2,
            textColor: Color = CometChatTheme.colorScheme.textColorSecondary,
            textStyle: TextStyle = CometChatTheme.typography.caption1Regular,
            iconTint: Color = CometChatTheme.colorScheme.iconTintSecondary
        ): CometChatModerationViewStyle = CometChatModerationViewStyle(
            backgroundColor = backgroundColor,
            textColor = textColor,
            textStyle = textStyle,
            iconTint = iconTint
        )
    }
}
