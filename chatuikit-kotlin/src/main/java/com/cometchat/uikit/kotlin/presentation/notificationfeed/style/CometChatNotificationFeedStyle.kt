package com.cometchat.uikit.kotlin.presentation.notificationfeed.style

import android.graphics.Color
import android.graphics.Typeface
import androidx.annotation.ColorInt
import androidx.annotation.Dimension

/**
 * Style configuration for the CometChatNotificationFeed XML View component.
 * All colors default to 0 (transparent/unset) to inherit from CometChatTheme.
 */
data class CometChatNotificationFeedStyle(
    // Screen
    @ColorInt val backgroundColor: Int = 0,

    // Header
    @ColorInt val headerTitleColor: Int = 0,
    val headerTitleTypeface: Typeface? = null,
    @Dimension val headerTitleSize: Float = 0f,

    // Filter Chips
    @ColorInt val chipActiveBackgroundColor: Int = 0,
    @ColorInt val chipActiveTextColor: Int = 0,
    @ColorInt val chipInactiveBackgroundColor: Int = 0,
    @ColorInt val chipInactiveTextColor: Int = 0,
    @ColorInt val chipBorderColor: Int = 0,

    // Badge
    @ColorInt val badgeBackgroundColor: Int = 0,
    @ColorInt val badgeTextColor: Int = 0,

    // Content
    @ColorInt val separatorColor: Int = 0,
    @ColorInt val timestampTextColor: Int = 0,
    val timestampTypeface: Typeface? = null,
    @Dimension val timestampTextSize: Float = 0f,

    // Cards (container around CometChatCardsRenderer output)
    @ColorInt val cardBackgroundColor: Int = 0,
    @ColorInt val cardBorderColor: Int = 0,
    @Dimension val cardBorderRadius: Float = 12f,
    @Dimension val cardBorderWidth: Float = 0f,

    // Unread indicator
    @ColorInt val unreadIndicatorColor: Int = 0
) {
    companion object {
        /**
         * Creates a default style with sensible defaults.
         */
        fun default(): CometChatNotificationFeedStyle {
            return CometChatNotificationFeedStyle(
                backgroundColor = Color.WHITE,
                headerTitleColor = Color.BLACK,
                chipActiveBackgroundColor = Color.parseColor("#3399FF"),
                chipActiveTextColor = Color.WHITE,
                chipInactiveBackgroundColor = Color.TRANSPARENT,
                chipInactiveTextColor = Color.DKGRAY,
                chipBorderColor = Color.LTGRAY,
                badgeBackgroundColor = Color.RED,
                badgeTextColor = Color.WHITE,
                separatorColor = Color.parseColor("#E0E0E0"),
                timestampTextColor = Color.GRAY,
                cardBackgroundColor = Color.WHITE,
                cardBorderColor = Color.parseColor("#E0E0E0"),
                unreadIndicatorColor = Color.parseColor("#3399FF")
            )
        }
    }
}
