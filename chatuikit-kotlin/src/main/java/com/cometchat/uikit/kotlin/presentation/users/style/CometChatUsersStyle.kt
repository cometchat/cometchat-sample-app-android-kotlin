package com.cometchat.uikit.kotlin.presentation.users.style

import android.content.Context
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.StyleRes
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.theme.CometChatTheme

/**
 * Style configuration for CometChatUsers component.
 */
data class CometChatUsersStyle(
    // Container
    @ColorInt val backgroundColor: Int = 0,
    @ColorInt val strokeColor: Int = 0,
    @Dimension val strokeWidth: Int = 0,
    @Dimension val cornerRadius: Int = 0,
    
    // Toolbar
    @ColorInt val titleTextColor: Int = 0,
    @StyleRes val titleTextAppearance: Int = 0,
    val backIcon: Drawable? = null,
    @ColorInt val backIconTint: Int = 0,
    
    // Search box
    @ColorInt val searchBackgroundColor: Int = 0,
    @ColorInt val searchTextColor: Int = 0,
    @StyleRes val searchTextAppearance: Int = 0,
    @ColorInt val searchPlaceholderColor: Int = 0,
    @StyleRes val searchPlaceholderTextAppearance: Int = 0,
    val searchStartIcon: Drawable? = null,
    @ColorInt val searchStartIconTint: Int = 0,
    val searchEndIcon: Drawable? = null,
    @ColorInt val searchEndIconTint: Int = 0,
    @Dimension val searchCornerRadius: Int = 0,
    @Dimension val searchStrokeWidth: Int = 0,
    @ColorInt val searchStrokeColor: Int = 0,
    
    // Sticky header
    @ColorInt val stickyHeaderBackgroundColor: Int = 0,
    @ColorInt val stickyHeaderTextColor: Int = 0,
    @StyleRes val stickyHeaderTextAppearance: Int = 0,
    
    // Empty state
    @ColorInt val emptyStateTitleTextColor: Int = 0,
    @ColorInt val emptyStateSubtitleTextColor: Int = 0,
    @StyleRes val emptyStateTitleTextAppearance: Int = 0,
    @StyleRes val emptyStateSubtitleTextAppearance: Int = 0,
    
    // Error state
    @ColorInt val errorStateTitleTextColor: Int = 0,
    @ColorInt val errorStateSubtitleTextColor: Int = 0,
    @StyleRes val errorStateTitleTextAppearance: Int = 0,
    @StyleRes val errorStateSubtitleTextAppearance: Int = 0,
    @ColorInt val retryButtonBackgroundColor: Int = 0,
    @ColorInt val retryButtonTextColor: Int = 0,
    @StyleRes val retryButtonTextAppearance: Int = 0,
    @ColorInt val retryButtonStrokeColor: Int = 0,
    @Dimension val retryButtonStrokeWidth: Int = 0,
    @Dimension val retryButtonCornerRadius: Int = 0,
    
    // Selection
    val discardSelectionIcon: Drawable? = null,
    @ColorInt val discardSelectionIconTint: Int = 0,
    val submitSelectionIcon: Drawable? = null,
    @ColorInt val submitSelectionIconTint: Int = 0,
    
    // Separator
    @Dimension val separatorHeight: Int = 0,
    @ColorInt val separatorColor: Int = 0,
    
    // Nested component style resource IDs
    @StyleRes val avatarStyleResId: Int = 0,
    @StyleRes val statusIndicatorStyleResId: Int = 0,
    
    // Item style
    val itemStyle: CometChatUsersListItemStyle = CometChatUsersListItemStyle()
) {
    companion object {
        /**
         * Creates a default style by extracting values from the theme's cometchatUsersStyle.
         */
        fun default(context: Context): CometChatUsersStyle {
            return extractFromThemeStyle(context, R.attr.cometchatUsersStyle)
        }

        private fun extractFromThemeStyle(context: Context, themeStyleAttr: Int): CometChatUsersStyle {
            val themeTypedArray = context.obtainStyledAttributes(intArrayOf(themeStyleAttr))
            val styleResId = themeTypedArray.getResourceId(0, 0)
            themeTypedArray.recycle()
            return extractFromStyleResource(context, styleResId)
        }

        private fun extractFromStyleResource(context: Context, styleResId: Int): CometChatUsersStyle {
            if (styleResId == 0) {
                return extractFromTypedArray(context, null)
            }
            val typedArray = context.obtainStyledAttributes(styleResId, R.styleable.CometChatUsers)
            return try {
                extractFromTypedArray(context, typedArray)
            } finally {
                typedArray.recycle()
            }
        }

        fun fromTypedArray(context: Context, typedArray: TypedArray): CometChatUsersStyle {
            return try {
                extractFromTypedArray(context, typedArray)
            } finally {
                typedArray.recycle()
            }
        }

        private fun extractFromTypedArray(context: Context, typedArray: TypedArray?): CometChatUsersStyle {
            // Extract nested style resource IDs
            val avatarStyleResId = typedArray?.getResourceId(
                R.styleable.CometChatUsers_cometchatUsersAvatarStyle, 0
            ) ?: 0
            val statusIndicatorStyleResId = typedArray?.getResourceId(
                R.styleable.CometChatUsers_cometchatUsersStatusIndicator, 0
            ) ?: 0

            // Create item style from the same TypedArray
            val itemStyle = CometChatUsersListItemStyle.extractFromTypedArrayInternal(context, typedArray)

            return CometChatUsersStyle(
                // Container
                backgroundColor = typedArray?.getColor(
                    R.styleable.CometChatUsers_cometchatUsersBackgroundColor,
                    CometChatTheme.getBackgroundColor1(context)
                ) ?: CometChatTheme.getBackgroundColor1(context),
                strokeColor = typedArray?.getColor(
                    R.styleable.CometChatUsers_cometchatUsersStrokeColor, 0
                ) ?: 0,
                strokeWidth = typedArray?.getDimensionPixelSize(
                    R.styleable.CometChatUsers_cometchatUsersStrokeWidth, 0
                ) ?: 0,
                cornerRadius = typedArray?.getDimensionPixelSize(
                    R.styleable.CometChatUsers_cometchatUsersCornerRadius, 0
                ) ?: 0,

                // Toolbar
                titleTextColor = typedArray?.getColor(
                    R.styleable.CometChatUsers_cometchatUsersTitleTextColor,
                    CometChatTheme.getTextColorPrimary(context)
                ) ?: CometChatTheme.getTextColorPrimary(context),
                titleTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatUsers_cometchatUsersTitleTextAppearance,
                    CometChatTheme.getTextAppearanceHeading1Bold(context)
                ) ?: CometChatTheme.getTextAppearanceHeading1Bold(context),
                backIcon = typedArray?.getDrawable(
                    R.styleable.CometChatUsers_cometchatUsersBackIcon
                ),
                backIconTint = typedArray?.getColor(
                    R.styleable.CometChatUsers_cometchatUsersBackIconTint,
                    CometChatTheme.getIconTintPrimary(context)
                ) ?: CometChatTheme.getIconTintPrimary(context),

                // Search box
                searchBackgroundColor = typedArray?.getColor(
                    R.styleable.CometChatUsers_cometchatUsersSearchInputBackgroundColor,
                    CometChatTheme.getBackgroundColor3(context)
                ) ?: CometChatTheme.getBackgroundColor3(context),
                searchTextColor = typedArray?.getColor(
                    R.styleable.CometChatUsers_cometchatUsersSearchInputTextColor,
                    CometChatTheme.getTextColorPrimary(context)
                ) ?: CometChatTheme.getTextColorPrimary(context),
                searchTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatUsers_cometchatUsersSearchInputTextAppearance,
                    CometChatTheme.getTextAppearanceHeading4Regular(context)
                ) ?: CometChatTheme.getTextAppearanceHeading4Regular(context),
                searchPlaceholderColor = typedArray?.getColor(
                    R.styleable.CometChatUsers_cometchatUsersSearchInputPlaceHolderTextColor,
                    CometChatTheme.getTextColorTertiary(context)
                ) ?: CometChatTheme.getTextColorTertiary(context),
                searchPlaceholderTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatUsers_cometchatUsersSearchInputPlaceHolderTextAppearance,
                    CometChatTheme.getTextAppearanceHeading4Regular(context)
                ) ?: CometChatTheme.getTextAppearanceHeading4Regular(context),
                searchStartIcon = typedArray?.getDrawable(
                    R.styleable.CometChatUsers_cometchatUsersSearchInputIcon
                ),
                searchStartIconTint = typedArray?.getColor(
                    R.styleable.CometChatUsers_cometchatUsersSearchInputIconTint,
                    CometChatTheme.getIconTintSecondary(context)
                ) ?: CometChatTheme.getIconTintSecondary(context),
                searchEndIcon = typedArray?.getDrawable(
                    R.styleable.CometChatUsers_cometchatUsersSearchInputEndIcon
                ),
                searchEndIconTint = typedArray?.getColor(
                    R.styleable.CometChatUsers_cometchatUsersSearchInputEndIconTint,
                    CometChatTheme.getIconTintSecondary(context)
                ) ?: CometChatTheme.getIconTintSecondary(context),
                searchCornerRadius = typedArray?.getDimensionPixelSize(
                    R.styleable.CometChatUsers_cometchatUsersSearchInputCornerRadius,
                    context.resources.getDimensionPixelSize(R.dimen.cometchat_radius_max)
                ) ?: context.resources.getDimensionPixelSize(R.dimen.cometchat_radius_max),
                searchStrokeWidth = typedArray?.getDimensionPixelSize(
                    R.styleable.CometChatUsers_cometchatUsersSearchInputStrokeWidth, 0
                ) ?: 0,
                searchStrokeColor = typedArray?.getColor(
                    R.styleable.CometChatUsers_cometchatUsersSearchInputStrokeColor, 0
                ) ?: 0,

                // Sticky header
                stickyHeaderBackgroundColor = typedArray?.getColor(
                    R.styleable.CometChatUsers_cometchatUsersStickyTitleBackgroundColor,
                    CometChatTheme.getBackgroundColor1(context)
                ) ?: CometChatTheme.getBackgroundColor1(context),
                stickyHeaderTextColor = typedArray?.getColor(
                    R.styleable.CometChatUsers_cometchatUsersStickyTitleColor,
                    CometChatTheme.getTextColorHighlight(context)
                ) ?: CometChatTheme.getTextColorHighlight(context),
                stickyHeaderTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatUsers_cometchatUsersStickyTitleAppearance,
                    CometChatTheme.getTextAppearanceCaption1Medium(context)
                ) ?: CometChatTheme.getTextAppearanceCaption1Medium(context),

                // Empty state
                emptyStateTitleTextColor = typedArray?.getColor(
                    R.styleable.CometChatUsers_cometchatUsersEmptyStateTextColor,
                    CometChatTheme.getTextColorPrimary(context)
                ) ?: CometChatTheme.getTextColorPrimary(context),
                emptyStateSubtitleTextColor = typedArray?.getColor(
                    R.styleable.CometChatUsers_cometchatUsersEmptyStateSubtitleTextColor,
                    CometChatTheme.getTextColorSecondary(context)
                ) ?: CometChatTheme.getTextColorSecondary(context),
                emptyStateTitleTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatUsers_cometchatUsersEmptyStateTextAppearance,
                    CometChatTheme.getTextAppearanceHeading3Bold(context)
                ) ?: CometChatTheme.getTextAppearanceHeading3Bold(context),
                emptyStateSubtitleTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatUsers_cometchatUsersEmptyStateSubTitleTextAppearance,
                    CometChatTheme.getTextAppearanceBodyRegular(context)
                ) ?: CometChatTheme.getTextAppearanceBodyRegular(context),

                // Error state
                errorStateTitleTextColor = typedArray?.getColor(
                    R.styleable.CometChatUsers_cometchatUsersErrorStateTextColor,
                    CometChatTheme.getTextColorPrimary(context)
                ) ?: CometChatTheme.getTextColorPrimary(context),
                errorStateSubtitleTextColor = typedArray?.getColor(
                    R.styleable.CometChatUsers_cometchatUsersErrorStateSubtitleColor,
                    CometChatTheme.getTextColorSecondary(context)
                ) ?: CometChatTheme.getTextColorSecondary(context),
                errorStateTitleTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatUsers_cometchatUsersErrorStateTextAppearance,
                    CometChatTheme.getTextAppearanceHeading3Bold(context)
                ) ?: CometChatTheme.getTextAppearanceHeading3Bold(context),
                errorStateSubtitleTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatUsers_cometchatUsersErrorStateSubtitleTextAppearance,
                    CometChatTheme.getTextAppearanceBodyRegular(context)
                ) ?: CometChatTheme.getTextAppearanceBodyRegular(context),
                retryButtonBackgroundColor = typedArray?.getColor(
                    R.styleable.CometChatUsers_cometchatUsersRetryButtonBackgroundColor,
                    CometChatTheme.getPrimaryColor(context)
                ) ?: CometChatTheme.getPrimaryColor(context),
                retryButtonTextColor = typedArray?.getColor(
                    R.styleable.CometChatUsers_cometchatUsersRetryButtonTextColor,
                    CometChatTheme.getColorWhite(context)
                ) ?: CometChatTheme.getColorWhite(context),
                retryButtonTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatUsers_cometchatUsersRetryButtonTextAppearance,
                    CometChatTheme.getTextAppearanceButtonMedium(context)
                ) ?: CometChatTheme.getTextAppearanceButtonMedium(context),
                retryButtonStrokeColor = typedArray?.getColor(
                    R.styleable.CometChatUsers_cometchatUsersRetryButtonStrokeColor, 0
                ) ?: 0,
                retryButtonStrokeWidth = typedArray?.getDimensionPixelSize(
                    R.styleable.CometChatUsers_cometchatUsersRetryButtonStrokeWidth, 0
                ) ?: 0,
                retryButtonCornerRadius = typedArray?.getDimensionPixelSize(
                    R.styleable.CometChatUsers_cometchatUsersRetryButtonCornerRadius, 0
                ) ?: 0,

                // Selection
                discardSelectionIcon = typedArray?.getDrawable(
                    R.styleable.CometChatUsers_cometchatUsersDiscardSelectionIcon
                ),
                discardSelectionIconTint = typedArray?.getColor(
                    R.styleable.CometChatUsers_cometchatUsersDiscardSelectionIconTint,
                    CometChatTheme.getIconTintPrimary(context)
                ) ?: CometChatTheme.getIconTintPrimary(context),
                submitSelectionIcon = typedArray?.getDrawable(
                    R.styleable.CometChatUsers_cometchatUsersSubmitSelectionIcon
                ),
                submitSelectionIconTint = typedArray?.getColor(
                    R.styleable.CometChatUsers_cometchatUsersSubmitSelectionIconTint,
                    CometChatTheme.getPrimaryColor(context)
                ) ?: CometChatTheme.getPrimaryColor(context),

                // Separator
                separatorHeight = 1,
                separatorColor = typedArray?.getColor(
                    R.styleable.CometChatUsers_cometchatUsersSeparatorColor,
                    CometChatTheme.getStrokeColorLight(context)
                ) ?: CometChatTheme.getStrokeColorLight(context),

                // Nested component style resource IDs
                avatarStyleResId = avatarStyleResId,
                statusIndicatorStyleResId = statusIndicatorStyleResId,

                // Item style
                itemStyle = itemStyle
            )
        }
    }
}
