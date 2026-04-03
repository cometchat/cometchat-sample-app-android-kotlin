package com.cometchat.uikit.compose.presentation.shared.formatters.style

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.TextStyle
import com.cometchat.uikit.compose.theme.CometChatTheme

/**
 * Style configuration for @mentions in CometChat UI components.
 *
 * This immutable data class provides unified styling for mentions across different contexts
 * including message bubbles, conversation previews, and message composer. It supports
 * distinct styling for self-mentions (when the logged-in user is mentioned) and
 * other-user mentions.
 *
 * @param textColor Text color for other-user mentions
 * @param textStyle Typography style for other-user mentions
 * @param backgroundColor Background color for other-user mentions
 * @param selfTextColor Text color for self-mentions (when the logged-in user is mentioned)
 * @param selfTextStyle Typography style for self-mentions
 * @param selfBackgroundColor Background color for self-mentions
 */
@Immutable
data class CometChatMentionStyle(
    val textColor: Color,
    val textStyle: TextStyle,
    val backgroundColor: Color,
    val selfTextColor: Color,
    val selfTextStyle: TextStyle,
    val selfBackgroundColor: Color
) {
    companion object {
        /**
         * Alpha value for default translucent background colors (51/255 ≈ 0.2 or 20% opacity)
         */
        private const val DEFAULT_BACKGROUND_ALPHA = 51f / 255f
        
        /**
         * Creates a default CometChatMentionStyle with values sourced from CometChatTheme.
         * 
         * By default, background colors are set to translucent versions of the text colors
         * with 20% opacity (alpha 51).
         *
         * @param textColor Text color for other-user mentions. Defaults to theme's highlight color.
         * @param textStyle Typography for other-user mentions. Defaults to theme's body medium style.
         * @param backgroundColor Background color for other-user mentions. Defaults to textColor with 20% opacity.
         * @param selfTextColor Text color for self-mentions. Defaults to theme's warning color.
         * @param selfTextStyle Typography for self-mentions. Defaults to theme's body medium style.
         * @param selfBackgroundColor Background color for self-mentions. Defaults to selfTextColor with 20% opacity.
         * @return A new CometChatMentionStyle instance with theme-based default values
         */
        @Composable
        fun default(
            textColor: Color = CometChatTheme.colorScheme.textColorHighlight,
            textStyle: TextStyle = CometChatTheme.typography.bodyMedium,
            backgroundColor: Color = textColor.copy(alpha = DEFAULT_BACKGROUND_ALPHA),
            selfTextColor: Color = CometChatTheme.colorScheme.warningColor,
            selfTextStyle: TextStyle = CometChatTheme.typography.bodyMedium,
            selfBackgroundColor: Color = selfTextColor.copy(alpha = DEFAULT_BACKGROUND_ALPHA)
        ): CometChatMentionStyle = CometChatMentionStyle(
            textColor = textColor,
            textStyle = textStyle,
            backgroundColor = backgroundColor,
            selfTextColor = selfTextColor,
            selfTextStyle = selfTextStyle,
            selfBackgroundColor = selfBackgroundColor
        )

        /**
         * Creates a CometChatMentionStyle for incoming (left-aligned) message bubbles.
         *
         * Incoming bubble mentions use:
         * - Text color: Primary/highlight color (#6852D6)
         * - Background color: Primary color with 20% opacity
         * - Self-mention text color: Warning/amber color (#FFAB00)
         * - Self-mention background color: Warning color with 20% opacity
         *
         * @return A new CometChatMentionStyle configured for incoming message bubbles
         */
        @Composable
        fun incoming(): CometChatMentionStyle {
            val textColor = CometChatTheme.colorScheme.primary
            val selfTextColor = CometChatTheme.colorScheme.warningColor
            return CometChatMentionStyle(
                textColor = textColor,
                textStyle = CometChatTheme.typography.bodyMedium,
                backgroundColor = textColor.copy(alpha = DEFAULT_BACKGROUND_ALPHA),
                selfTextColor = selfTextColor,
                selfTextStyle = CometChatTheme.typography.bodyMedium,
                selfBackgroundColor = selfTextColor.copy(alpha = DEFAULT_BACKGROUND_ALPHA)
            )
        }

        /**
         * Creates a CometChatMentionStyle for outgoing (right-aligned) message bubbles.
         *
         * Outgoing bubble mentions use:
         * - Text color: White (#FFFFFF) for contrast on primary-colored backgrounds
         * - Background color: White with 20% opacity
         * - Self-mention text color: Warning/amber color (#FFAB00)
         * - Self-mention background color: Warning color with 20% opacity
         *
         * @return A new CometChatMentionStyle configured for outgoing message bubbles
         */
        @Composable
        fun outgoing(): CometChatMentionStyle {
            val textColor = Color.White
            val selfTextColor = CometChatTheme.colorScheme.warningColor
            return CometChatMentionStyle(
                textColor = textColor,
                textStyle = CometChatTheme.typography.bodyMedium,
                backgroundColor = textColor.copy(alpha = DEFAULT_BACKGROUND_ALPHA),
                selfTextColor = selfTextColor,
                selfTextStyle = CometChatTheme.typography.bodyMedium,
                selfBackgroundColor = selfTextColor.copy(alpha = DEFAULT_BACKGROUND_ALPHA)
            )
        }
    }

    /**
     * Converts this style to a [PromptTextStyle] for other-user mentions.
     *
     * This function bridges Compose types to Android View types for use with
     * the existing formatter infrastructure.
     *
     * @return A PromptTextStyle configured with textColor and backgroundColor
     */
    fun toPromptTextStyle(): PromptTextStyle {
        return PromptTextStyle()
            .setColor(textColor.toArgb())
            .setBackgroundColor(backgroundColor.toArgb())
    }

    /**
     * Converts this style to a [PromptTextStyle] for self-mentions.
     *
     * This function bridges Compose types to Android View types for use with
     * the existing formatter infrastructure, using self-mention specific properties.
     *
     * @return A PromptTextStyle configured with selfTextColor and selfBackgroundColor
     */
    fun toSelfPromptTextStyle(): PromptTextStyle {
        return PromptTextStyle()
            .setColor(selfTextColor.toArgb())
            .setBackgroundColor(selfBackgroundColor.toArgb())
    }
}
