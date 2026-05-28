package com.cometchat.uikit.compose.presentation.notificationfeed.style

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cometchat.uikit.compose.theme.CometChatTheme

/**
 * Style configuration for the CometChatNotificationFeed component.
 * Defaults resolve from [CometChatTheme] at composition time, making
 * the component fully theme-aware and supporting light/dark modes.
 *
 * All color properties default to [Color.Unspecified], which signals the
 * component to read from CometChatTheme.colorScheme at render time.
 * This allows callers to override individual values without losing theme support.
 *
 * Figma reference: Android - Chat UI Kits / Campaigns section
 */
data class CometChatNotificationFeedStyle(
    // Screen
    val backgroundColor: Color = Color.Unspecified,

    // Header
    val headerBackgroundColor: Color = Color.Unspecified,
    val headerTitleColor: Color = Color.Unspecified,
    val headerBorderColor: Color = Color.Unspecified,

    // Filter Chips — active
    val chipActiveBackgroundColor: Color = Color.Unspecified,
    val chipActiveTextColor: Color = Color.Unspecified,

    // Filter Chips — inactive
    val chipInactiveBackgroundColor: Color = Color.Unspecified,
    val chipInactiveTextColor: Color = Color.Unspecified,
    val chipBorderColor: Color = Color.Unspecified,

    // Badge — active chip
    val badgeActiveBackgroundColor: Color = Color.Unspecified,
    val badgeActiveBorderColor: Color = Color.Unspecified,
    val badgeActiveTextColor: Color = Color.Unspecified,

    // Badge — inactive chip
    val badgeInactiveBackgroundColor: Color = Color.Unspecified,
    val badgeInactiveTextColor: Color = Color.Unspecified,

    // Timestamp header
    val timestampTextColor: Color = Color.Unspecified,

    // Cards
    val cardBackgroundColor: Color = Color.Unspecified,
    val cardBorderColor: Color = Color.Unspecified,
    val cardBorderRadius: Dp = 12.dp,
    val cardBorderWidth: Dp = 1.dp,

    // Card content
    val cardTitleColor: Color = Color.Unspecified,
    val cardPriceColor: Color = Color.Unspecified,
    val cardDescriptionColor: Color = Color.Unspecified,

    // Card buttons
    val primaryButtonBackgroundColor: Color = Color.Unspecified,
    val primaryButtonTextColor: Color = Color.Unspecified,
    val secondaryButtonBackgroundColor: Color = Color.Unspecified,
    val secondaryButtonBorderColor: Color = Color.Unspecified,
    val secondaryButtonTextColor: Color = Color.Unspecified,
    val buttonBorderRadius: Dp = 8.dp,

    // Unread indicator
    val unreadIndicatorColor: Color = Color.Unspecified,

    // Separator
    val separatorColor: Color = Color.Unspecified,

    // Empty / Error state
    val emptyTitleColor: Color = Color.Unspecified,
    val emptyDescriptionColor: Color = Color.Unspecified
) {
    companion object {
        /**
         * Resolves all [Color.Unspecified] values from [CometChatTheme.colorScheme].
         * Call this inside a @Composable to get a fully-resolved style instance.
         */
        @Composable
        fun fromTheme(overrides: CometChatNotificationFeedStyle = CometChatNotificationFeedStyle()): CometChatNotificationFeedStyle {
            val colorScheme = CometChatTheme.colorScheme
            val typography = CometChatTheme.typography

            return CometChatNotificationFeedStyle(
                // Screen: Figma background-02 → backgroundColor2 (was #FAFAFA)
                backgroundColor = overrides.backgroundColor.takeOrElse { colorScheme.backgroundColor2 },

                // Header: Figma background1 → backgroundColor1 (was white)
                headerBackgroundColor = overrides.headerBackgroundColor.takeOrElse { colorScheme.backgroundColor1 },
                headerTitleColor = overrides.headerTitleColor.takeOrElse { colorScheme.textColorPrimary },
                headerBorderColor = overrides.headerBorderColor.takeOrElse { colorScheme.strokeColorLight },

                // Active chip: Figma primary → primary
                chipActiveBackgroundColor = overrides.chipActiveBackgroundColor.takeOrElse { colorScheme.primary },
                chipActiveTextColor = overrides.chipActiveTextColor.takeOrElse { colorScheme.colorWhite },

                // Inactive chip: Figma background-01-hover → backgroundColor1
                chipInactiveBackgroundColor = overrides.chipInactiveBackgroundColor.takeOrElse { colorScheme.backgroundColor1 },
                chipInactiveTextColor = overrides.chipInactiveTextColor.takeOrElse { colorScheme.textColorTertiary },
                chipBorderColor = overrides.chipBorderColor.takeOrElse { colorScheme.strokeColorDefault },

                // Badge active: Figma ep50 bg, ep200 border, textHighlight text
                badgeActiveBackgroundColor = overrides.badgeActiveBackgroundColor.takeOrElse { colorScheme.extendedPrimaryColor50 },
                badgeActiveBorderColor = overrides.badgeActiveBorderColor.takeOrElse { colorScheme.extendedPrimaryColor200 },
                badgeActiveTextColor = overrides.badgeActiveTextColor.takeOrElse { colorScheme.textColorHighlight },

                // Badge inactive: Figma neutral600 bg, white text
                badgeInactiveBackgroundColor = overrides.badgeInactiveBackgroundColor.takeOrElse { colorScheme.neutralColor600 },
                badgeInactiveTextColor = overrides.badgeInactiveTextColor.takeOrElse { colorScheme.colorWhite },

                // Timestamp: Figma text-secondary
                timestampTextColor = overrides.timestampTextColor.takeOrElse { colorScheme.textColorSecondary },

                // Card: Figma background-01, border-default
                cardBackgroundColor = overrides.cardBackgroundColor.takeOrElse { colorScheme.backgroundColor1 },
                cardBorderColor = overrides.cardBorderColor.takeOrElse { colorScheme.strokeColorDefault },
                cardBorderRadius = overrides.cardBorderRadius,
                cardBorderWidth = overrides.cardBorderWidth,

                // Card content colors
                cardTitleColor = overrides.cardTitleColor.takeOrElse { colorScheme.textColorPrimary },
                cardPriceColor = overrides.cardPriceColor.takeOrElse { colorScheme.primary },
                cardDescriptionColor = overrides.cardDescriptionColor.takeOrElse { colorScheme.textColorSecondary },

                // Buttons
                primaryButtonBackgroundColor = overrides.primaryButtonBackgroundColor.takeOrElse { colorScheme.primaryButtonBackgroundColor },
                primaryButtonTextColor = overrides.primaryButtonTextColor.takeOrElse { colorScheme.colorWhite },
                secondaryButtonBackgroundColor = overrides.secondaryButtonBackgroundColor.takeOrElse { colorScheme.backgroundColor1 },
                secondaryButtonBorderColor = overrides.secondaryButtonBorderColor.takeOrElse { colorScheme.strokeColorDark },
                secondaryButtonTextColor = overrides.secondaryButtonTextColor.takeOrElse { colorScheme.textColorPrimary },
                buttonBorderRadius = overrides.buttonBorderRadius,

                // Unread indicator: primary color
                unreadIndicatorColor = overrides.unreadIndicatorColor.takeOrElse { colorScheme.primary },

                // Separator
                separatorColor = overrides.separatorColor.takeOrElse { colorScheme.strokeColorLight },

                // Empty/Error
                emptyTitleColor = overrides.emptyTitleColor.takeOrElse { colorScheme.textColorPrimary },
                emptyDescriptionColor = overrides.emptyDescriptionColor.takeOrElse { colorScheme.textColorSecondary }
            )
        }
    }
}

/**
 * Returns the color if it's specified (not [Color.Unspecified]), otherwise returns [fallback].
 */
private fun Color.takeOrElse(fallback: () -> Color): Color =
    if (this != Color.Unspecified) this else fallback()
