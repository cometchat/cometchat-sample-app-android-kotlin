package com.cometchat.uikit.compose.presentation.search.style

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cometchat.uikit.compose.R
import com.cometchat.uikit.compose.presentation.shared.defaultstates.CometChatEmptyStateStyle
import com.cometchat.uikit.compose.presentation.shared.defaultstates.CometChatErrorStateStyle
import com.cometchat.uikit.compose.presentation.shared.defaultstates.CometChatLoadingStateStyle
import com.cometchat.uikit.compose.theme.CometChatTheme

/**
 * Design constants for CometChatSearch component styling.
 * 
 * The pill shape corner radius uses a large value (1000dp) intentionally to create
 * a fully rounded capsule/pill shape regardless of the component's height.
 * This is a common pattern in Material Design where using a corner radius larger
 * than half the component height ensures consistent pill-shaped corners.
 */
private object SearchStyleConstants {
    /**
     * Corner radius for pill/capsule shaped components (search bar, filter chips).
     * Using a large value ensures fully rounded ends regardless of component height.
     */
    val pillShapeCornerRadius = 1000.dp
}

/**
 * Immutable style configuration for CometChatSearch component.
 */
@Immutable
data class CometChatSearchStyle(
    val backgroundColor: Color,
    val searchBarBackgroundColor: Color,
    val searchBarStrokeColor: Color,
    val searchBarStrokeWidth: Dp,
    val searchBarCornerRadius: Dp,
    val searchBarTextColor: Color,
    val searchBarTextStyle: TextStyle,
    val searchBarHintTextColor: Color,
    val searchBarHintTextStyle: TextStyle,
    val backIcon: Painter?,
    val backIconTint: Color,
    val clearIcon: Painter?,
    val clearIconTint: Color,
    val searchIcon: Painter?,
    val searchIconTint: Color,
    val filterChipBackgroundColor: Color,
    val filterChipSelectedBackgroundColor: Color,
    val filterChipTextColor: Color,
    val filterChipSelectedTextColor: Color,
    val filterChipTextStyle: TextStyle,
    val filterChipStrokeColor: Color,
    val filterChipSelectedStrokeColor: Color,
    val filterChipStrokeWidth: Dp,
    val filterChipCornerRadius: Dp,
    val sectionHeaderTextColor: Color,
    val sectionHeaderTextStyle: TextStyle,
    val sectionHeaderBackgroundColor: Color,
    val dateSeparatorBackgroundColor: Color,
    val dateSeparatorTextColor: Color,
    val dateSeparatorTextStyle: TextStyle,
    val seeMoreTextColor: Color,
    val seeMoreTextStyle: TextStyle,
    val conversationItemStyle: SearchConversationItemStyle,
    val messageItemStyle: SearchMessageItemStyle,
    val emptyStateStyle: CometChatEmptyStateStyle,
    val errorStateStyle: CometChatErrorStateStyle,
    val loadingStateStyle: CometChatLoadingStateStyle,
    val initialStateStyle: CometChatEmptyStateStyle
) {
    companion object {
        @Composable
        fun default(
            backgroundColor: Color = CometChatTheme.colorScheme.backgroundColor1,
            searchBarBackgroundColor: Color = CometChatTheme.colorScheme.backgroundColor3,
            searchBarStrokeColor: Color = CometChatTheme.colorScheme.strokeColorDark,
            searchBarStrokeWidth: Dp = 1.dp,
            searchBarCornerRadius: Dp = SearchStyleConstants.pillShapeCornerRadius,
            searchBarTextColor: Color = CometChatTheme.colorScheme.textColorPrimary,
            searchBarTextStyle: TextStyle = CometChatTheme.typography.heading4Regular,
            searchBarHintTextColor: Color = CometChatTheme.colorScheme.textColorTertiary,
            searchBarHintTextStyle: TextStyle = CometChatTheme.typography.heading4Regular,
            backIcon: Painter? = painterResource(R.drawable.cometchat_ic_back),
            backIconTint: Color = CometChatTheme.colorScheme.iconTintPrimary,
            clearIcon: Painter? = painterResource(R.drawable.cometchat_ic_cancel),
            clearIconTint: Color = CometChatTheme.colorScheme.iconTintSecondary,
            searchIcon: Painter? = painterResource(R.drawable.cometchat_ic_search),
            searchIconTint: Color = CometChatTheme.colorScheme.iconTintSecondary,
            filterChipBackgroundColor: Color = CometChatTheme.colorScheme.backgroundColor3,
            filterChipSelectedBackgroundColor: Color = CometChatTheme.colorScheme.secondaryButtonBackgroundColor,
            filterChipTextColor: Color = CometChatTheme.colorScheme.textColorSecondary,
            filterChipSelectedTextColor: Color = CometChatTheme.colorScheme.textColorWhite,
            filterChipTextStyle: TextStyle = CometChatTheme.typography.bodyRegular,
            filterChipStrokeColor: Color = Color.Transparent,
            filterChipSelectedStrokeColor: Color = Color.Transparent,
            filterChipStrokeWidth: Dp = 0.dp,
            filterChipCornerRadius: Dp = SearchStyleConstants.pillShapeCornerRadius,
            sectionHeaderTextColor: Color = CometChatTheme.colorScheme.textColorSecondary,
            sectionHeaderTextStyle: TextStyle = CometChatTheme.typography.caption1Medium,
            sectionHeaderBackgroundColor: Color = CometChatTheme.colorScheme.backgroundColor1,
            dateSeparatorBackgroundColor: Color = Color.Transparent,
            dateSeparatorTextColor: Color = CometChatTheme.colorScheme.textColorSecondary,
            dateSeparatorTextStyle: TextStyle = CometChatTheme.typography.caption1Medium,
            seeMoreTextColor: Color = CometChatTheme.colorScheme.primary,
            seeMoreTextStyle: TextStyle = CometChatTheme.typography.bodyMedium,
            conversationItemStyle: SearchConversationItemStyle = SearchConversationItemStyle.default(),
            messageItemStyle: SearchMessageItemStyle = SearchMessageItemStyle.default(),
            emptyStateStyle: CometChatEmptyStateStyle = CometChatEmptyStateStyle.default(
                backgroundColor = backgroundColor,
                icon = painterResource(R.drawable.cometchat_ic_search_large),
                iconTint = CometChatTheme.colorScheme.iconTintSecondary,
                iconSize = 120.dp
            ),
            errorStateStyle: CometChatErrorStateStyle = CometChatErrorStateStyle.default(
                backgroundColor = backgroundColor,
                icon = painterResource(R.drawable.cometchat_ic_search_large),
                iconTint = CometChatTheme.colorScheme.iconTintSecondary,
                iconSize = 120.dp
            ),
            loadingStateStyle: CometChatLoadingStateStyle = CometChatLoadingStateStyle.default(
                backgroundColor = backgroundColor
            ),
            initialStateStyle: CometChatEmptyStateStyle = CometChatEmptyStateStyle.default(
                backgroundColor = backgroundColor,
                icon = painterResource(R.drawable.cometchat_ic_search_large),
                iconTint = CometChatTheme.colorScheme.iconTintSecondary,
                iconSize = 120.dp
            )
        ): CometChatSearchStyle = CometChatSearchStyle(
            backgroundColor = backgroundColor,
            searchBarBackgroundColor = searchBarBackgroundColor,
            searchBarStrokeColor = searchBarStrokeColor,
            searchBarStrokeWidth = searchBarStrokeWidth,
            searchBarCornerRadius = searchBarCornerRadius,
            searchBarTextColor = searchBarTextColor,
            searchBarTextStyle = searchBarTextStyle,
            searchBarHintTextColor = searchBarHintTextColor,
            searchBarHintTextStyle = searchBarHintTextStyle,
            backIcon = backIcon,
            backIconTint = backIconTint,
            clearIcon = clearIcon,
            clearIconTint = clearIconTint,
            searchIcon = searchIcon,
            searchIconTint = searchIconTint,
            filterChipBackgroundColor = filterChipBackgroundColor,
            filterChipSelectedBackgroundColor = filterChipSelectedBackgroundColor,
            filterChipTextColor = filterChipTextColor,
            filterChipSelectedTextColor = filterChipSelectedTextColor,
            filterChipTextStyle = filterChipTextStyle,
            filterChipStrokeColor = filterChipStrokeColor,
            filterChipSelectedStrokeColor = filterChipSelectedStrokeColor,
            filterChipStrokeWidth = filterChipStrokeWidth,
            filterChipCornerRadius = filterChipCornerRadius,
            sectionHeaderTextColor = sectionHeaderTextColor,
            sectionHeaderTextStyle = sectionHeaderTextStyle,
            sectionHeaderBackgroundColor = sectionHeaderBackgroundColor,
            dateSeparatorBackgroundColor = dateSeparatorBackgroundColor,
            dateSeparatorTextColor = dateSeparatorTextColor,
            dateSeparatorTextStyle = dateSeparatorTextStyle,
            seeMoreTextColor = seeMoreTextColor,
            seeMoreTextStyle = seeMoreTextStyle,
            conversationItemStyle = conversationItemStyle,
            messageItemStyle = messageItemStyle,
            emptyStateStyle = emptyStateStyle,
            errorStateStyle = errorStateStyle,
            loadingStateStyle = loadingStateStyle,
            initialStateStyle = initialStateStyle
        )
    }
}
