package com.cometchat.uikit.kotlin.presentation.groups.style

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.StyleRes
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.theme.CometChatTheme

/**
 * Style configuration for CometChatGroups component.
 */
data class CometChatGroupsStyle(
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
    
    // Empty state
    @ColorInt val emptyStateTitleTextColor: Int = 0,
    @ColorInt val emptyStateSubtitleTextColor: Int = 0,
    @StyleRes val emptyStateTitleTextAppearance: Int = 0,
    @StyleRes val emptyStateSubtitleTextAppearance: Int = 0,
    val emptyStateIcon: Drawable? = null,
    @ColorInt val emptyStateIconTint: Int = 0,
    
    // Error state
    @ColorInt val errorStateTitleTextColor: Int = 0,
    @ColorInt val errorStateSubtitleTextColor: Int = 0,
    @StyleRes val errorStateTitleTextAppearance: Int = 0,
    @StyleRes val errorStateSubtitleTextAppearance: Int = 0,
    val errorStateIcon: Drawable? = null,
    @ColorInt val errorStateIconTint: Int = 0,
    @ColorInt val retryButtonBackgroundColor: Int = 0,
    @ColorInt val retryButtonTextColor: Int = 0,
    
    // Selection
    @Dimension val checkBoxStrokeWidth: Int = 0,
    @Dimension val checkBoxCornerRadius: Int = 0,
    @ColorInt val checkBoxStrokeColor: Int = 0,
    @ColorInt val checkBoxBackgroundColor: Int = 0,
    @ColorInt val checkBoxCheckedBackgroundColor: Int = 0,
    val checkBoxSelectIcon: Drawable? = null,
    @ColorInt val checkBoxSelectIconTint: Int = 0,
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
    @StyleRes val optionListStyleResId: Int = 0,
    
    // Item style
    val itemStyle: CometChatGroupsItemStyle = CometChatGroupsItemStyle()
) {
    companion object {
        /**
         * Creates a default style by extracting values from the theme's cometchatGroupsStyle.
         */
        fun default(context: Context): CometChatGroupsStyle {
            return extractFromThemeStyle(context, R.attr.cometchatGroupsStyle)
        }

        private fun extractFromThemeStyle(context: Context, themeStyleAttr: Int): CometChatGroupsStyle {
            val themeTypedArray = context.obtainStyledAttributes(intArrayOf(themeStyleAttr))
            val styleResId = themeTypedArray.getResourceId(0, 0)
            themeTypedArray.recycle()
            return extractFromStyleResource(context, styleResId)
        }

        private fun extractFromStyleResource(context: Context, styleResId: Int): CometChatGroupsStyle {
            if (styleResId == 0) {
                return extractFromTypedArray(context, null)
            }
            val typedArray = context.obtainStyledAttributes(styleResId, R.styleable.CometChatGroups)
            return try {
                extractFromTypedArray(context, typedArray)
            } finally {
                typedArray.recycle()
            }
        }

        fun fromTypedArray(context: Context, typedArray: TypedArray): CometChatGroupsStyle {
            return try {
                extractFromTypedArray(context, typedArray)
            } finally {
                typedArray.recycle()
            }
        }

        private fun extractFromTypedArray(context: Context, typedArray: TypedArray?): CometChatGroupsStyle {
            // Extract nested style resource IDs
            val avatarStyleResId = typedArray?.getResourceId(
                R.styleable.CometChatGroups_cometchatGroupsAvatarStyle, 0
            ) ?: 0
            val statusIndicatorStyleResId = typedArray?.getResourceId(
                R.styleable.CometChatGroups_cometchatGroupsStatusIndicatorStyle, 0
            ) ?: 0
            val optionListStyleResId = typedArray?.getResourceId(
                R.styleable.CometChatGroups_cometchatGroupsOptionListStyle, 0
            ) ?: 0

            // Create nested component styles
            val avatarStyle = if (avatarStyleResId != 0) {
                val avatarTypedArray = context.theme.obtainStyledAttributes(
                    avatarStyleResId, R.styleable.CometChatAvatar
                )
                com.cometchat.uikit.kotlin.presentation.shared.baseelements.avatar.CometChatAvatarStyle.fromTypedArray(context, avatarTypedArray)
            } else {
                com.cometchat.uikit.kotlin.presentation.shared.baseelements.avatar.CometChatAvatarStyle.default(context)
            }

            val statusIndicatorStyle = if (statusIndicatorStyleResId != 0) {
                val statusIndicatorTypedArray = context.theme.obtainStyledAttributes(
                    statusIndicatorStyleResId, R.styleable.CometChatStatusIndicator
                )
                com.cometchat.uikit.kotlin.presentation.shared.statusindicator.CometChatStatusIndicatorStyle.fromTypedArray(context, statusIndicatorTypedArray)
            } else {
                com.cometchat.uikit.kotlin.presentation.shared.statusindicator.CometChatStatusIndicatorStyle.default(context)
            }

            // Create item style
            val itemStyle = CometChatGroupsItemStyle(
                backgroundColor = typedArray?.getColor(
                    R.styleable.CometChatGroups_cometchatGroupsItemBackgroundColor,
                    Color.TRANSPARENT
                ) ?: Color.TRANSPARENT,
                selectedBackgroundColor = typedArray?.getColor(
                    R.styleable.CometChatGroups_cometchatGroupsItemSelectedBackgroundColor,
                    CometChatTheme.getBackgroundColor3(context)
                ) ?: CometChatTheme.getBackgroundColor3(context),
                titleTextColor = typedArray?.getColor(
                    R.styleable.CometChatGroups_cometchatGroupsItemTitleTextColor,
                    CometChatTheme.getTextColorPrimary(context)
                ) ?: CometChatTheme.getTextColorPrimary(context),
                titleTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatGroups_cometchatGroupsItemTitleTextAppearance,
                    CometChatTheme.getTextAppearanceHeading4Medium(context)
                ) ?: CometChatTheme.getTextAppearanceHeading4Medium(context),
                subtitleTextColor = typedArray?.getColor(
                    R.styleable.CometChatGroups_cometchatGroupsSubtitleColor,
                    CometChatTheme.getTextColorSecondary(context)
                ) ?: CometChatTheme.getTextColorSecondary(context),
                subtitleTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatGroups_cometchatGroupsSubtitleTextAppearance,
                    CometChatTheme.getTextAppearanceBodyRegular(context)
                ) ?: CometChatTheme.getTextAppearanceBodyRegular(context),
                separatorColor = typedArray?.getColor(
                    R.styleable.CometChatGroups_cometchatGroupsSeparatorColor,
                    CometChatTheme.getStrokeColorLight(context)
                ) ?: CometChatTheme.getStrokeColorLight(context),
                separatorHeight = typedArray?.getDimensionPixelSize(
                    R.styleable.CometChatGroups_cometchatGroupsSeparatorHeight, 1
                ) ?: 1,
                avatarStyle = avatarStyle,
                statusIndicatorStyle = statusIndicatorStyle,
                checkBoxStrokeWidth = typedArray?.getDimensionPixelSize(
                    R.styleable.CometChatGroups_cometchatGroupsCheckBoxStrokeWidth, 0
                ) ?: 0,
                checkBoxCornerRadius = typedArray?.getDimensionPixelSize(
                    R.styleable.CometChatGroups_cometchatGroupsCheckBoxCornerRadius, 0
                ) ?: 0,
                checkBoxStrokeColor = typedArray?.getColor(
                    R.styleable.CometChatGroups_cometchatGroupsCheckBoxStrokeColor,
                    CometChatTheme.getStrokeColorDefault(context)
                ) ?: CometChatTheme.getStrokeColorDefault(context),
                checkBoxBackgroundColor = typedArray?.getColor(
                    R.styleable.CometChatGroups_cometchatGroupsCheckBoxBackgroundColor,
                    CometChatTheme.getBackgroundColor1(context)
                ) ?: CometChatTheme.getBackgroundColor1(context),
                checkBoxCheckedBackgroundColor = typedArray?.getColor(
                    R.styleable.CometChatGroups_cometchatGroupsCheckBoxCheckedBackgroundColor,
                    CometChatTheme.getPrimaryColor(context)
                ) ?: CometChatTheme.getPrimaryColor(context),
                checkBoxSelectIcon = typedArray?.getDrawable(
                    R.styleable.CometChatGroups_cometchatGroupsCheckBoxSelectIcon
                ),
                checkBoxSelectIconTint = typedArray?.getColor(
                    R.styleable.CometChatGroups_cometchatGroupsCheckBoxSelectIconTint,
                    CometChatTheme.getColorWhite(context)
                ) ?: CometChatTheme.getColorWhite(context)
            )

            return CometChatGroupsStyle(
                // Container
                backgroundColor = typedArray?.getColor(
                    R.styleable.CometChatGroups_cometchatGroupsBackgroundColor,
                    CometChatTheme.getBackgroundColor1(context)
                ) ?: CometChatTheme.getBackgroundColor1(context),
                strokeColor = typedArray?.getColor(
                    R.styleable.CometChatGroups_cometchatGroupsStrokeColor, 0
                ) ?: 0,
                strokeWidth = typedArray?.getDimensionPixelSize(
                    R.styleable.CometChatGroups_cometchatGroupsStrokeWidth, 0
                ) ?: 0,
                cornerRadius = typedArray?.getDimensionPixelSize(
                    R.styleable.CometChatGroups_cometchatGroupsCornerRadius, 0
                ) ?: 0,

                // Toolbar
                titleTextColor = typedArray?.getColor(
                    R.styleable.CometChatGroups_cometchatGroupsTitleTextColor,
                    CometChatTheme.getTextColorPrimary(context)
                ) ?: CometChatTheme.getTextColorPrimary(context),
                titleTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatGroups_cometchatGroupsTitleTextAppearance,
                    CometChatTheme.getTextAppearanceHeading1Bold(context)
                ) ?: CometChatTheme.getTextAppearanceHeading1Bold(context),
                backIcon = typedArray?.getDrawable(
                    R.styleable.CometChatGroups_cometchatGroupsBackIcon
                ),
                backIconTint = typedArray?.getColor(
                    R.styleable.CometChatGroups_cometchatGroupsBackIconTint,
                    CometChatTheme.getIconTintPrimary(context)
                ) ?: CometChatTheme.getIconTintPrimary(context),

                // Search box
                searchBackgroundColor = typedArray?.getColor(
                    R.styleable.CometChatGroups_cometchatGroupsSearchInputBackgroundColor,
                    CometChatTheme.getBackgroundColor3(context)
                ) ?: CometChatTheme.getBackgroundColor3(context),
                searchTextColor = typedArray?.getColor(
                    R.styleable.CometChatGroups_cometchatGroupsSearchInputTextColor,
                    CometChatTheme.getTextColorPrimary(context)
                ) ?: CometChatTheme.getTextColorPrimary(context),
                searchTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatGroups_cometchatGroupsSearchInputTextAppearance,
                    CometChatTheme.getTextAppearanceHeading4Regular(context)
                ) ?: CometChatTheme.getTextAppearanceHeading4Regular(context),
                searchPlaceholderColor = typedArray?.getColor(
                    R.styleable.CometChatGroups_cometchatGroupsSearchInputPlaceHolderTextColor,
                    CometChatTheme.getTextColorTertiary(context)
                ) ?: CometChatTheme.getTextColorTertiary(context),
                searchPlaceholderTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatGroups_cometchatGroupsSearchInputPlaceHolderTextAppearance,
                    CometChatTheme.getTextAppearanceHeading4Regular(context)
                ) ?: CometChatTheme.getTextAppearanceHeading4Regular(context),
                searchStartIcon = typedArray?.getDrawable(
                    R.styleable.CometChatGroups_cometchatGroupsSearchInputStartIcon
                ),
                searchStartIconTint = typedArray?.getColor(
                    R.styleable.CometChatGroups_cometchatGroupsSearchInputStartIconTint,
                    CometChatTheme.getIconTintSecondary(context)
                ) ?: CometChatTheme.getIconTintSecondary(context),
                searchEndIcon = typedArray?.getDrawable(
                    R.styleable.CometChatGroups_cometchatGroupsSearchInputEndIcon
                ),
                searchEndIconTint = typedArray?.getColor(
                    R.styleable.CometChatGroups_cometchatGroupsSearchInputEndIconTint,
                    CometChatTheme.getIconTintSecondary(context)
                ) ?: CometChatTheme.getIconTintSecondary(context),
                searchCornerRadius = typedArray?.getDimensionPixelSize(
                    R.styleable.CometChatGroups_cometchatGroupsSearchInputCornerRadius,
                    context.resources.getDimensionPixelSize(R.dimen.cometchat_radius_max)
                ) ?: context.resources.getDimensionPixelSize(R.dimen.cometchat_radius_max),
                searchStrokeWidth = typedArray?.getDimensionPixelSize(
                    R.styleable.CometChatGroups_cometchatGroupsSearchInputStrokeWidth, 0
                ) ?: 0,
                searchStrokeColor = typedArray?.getColor(
                    R.styleable.CometChatGroups_cometchatGroupsSearchInputStrokeColor, 0
                ) ?: 0,

                // Empty state
                emptyStateTitleTextColor = typedArray?.getColor(
                    R.styleable.CometChatGroups_cometchatGroupsEmptyStateTitleTextColor,
                    CometChatTheme.getTextColorPrimary(context)
                ) ?: CometChatTheme.getTextColorPrimary(context),
                emptyStateSubtitleTextColor = typedArray?.getColor(
                    R.styleable.CometChatGroups_cometchatGroupsEmptyStateSubtitleTextColor,
                    CometChatTheme.getTextColorSecondary(context)
                ) ?: CometChatTheme.getTextColorSecondary(context),
                emptyStateTitleTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatGroups_cometchatGroupsEmptyStateTextTitleAppearance,
                    CometChatTheme.getTextAppearanceHeading3Bold(context)
                ) ?: CometChatTheme.getTextAppearanceHeading3Bold(context),
                emptyStateSubtitleTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatGroups_cometchatGroupsEmptyStateTextSubtitleAppearance,
                    CometChatTheme.getTextAppearanceBodyRegular(context)
                ) ?: CometChatTheme.getTextAppearanceBodyRegular(context),

                // Error state
                errorStateTitleTextColor = typedArray?.getColor(
                    R.styleable.CometChatGroups_cometchatGroupsErrorStateTitleTextColor,
                    CometChatTheme.getTextColorPrimary(context)
                ) ?: CometChatTheme.getTextColorPrimary(context),
                errorStateSubtitleTextColor = typedArray?.getColor(
                    R.styleable.CometChatGroups_cometchatGroupsErrorStateSubtitleTextColor,
                    CometChatTheme.getTextColorSecondary(context)
                ) ?: CometChatTheme.getTextColorSecondary(context),
                errorStateTitleTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatGroups_cometchatGroupsErrorStateTextTitleAppearance,
                    CometChatTheme.getTextAppearanceHeading3Bold(context)
                ) ?: CometChatTheme.getTextAppearanceHeading3Bold(context),
                errorStateSubtitleTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatGroups_cometchatGroupsErrorStateTextSubtitleAppearance,
                    CometChatTheme.getTextAppearanceBodyRegular(context)
                ) ?: CometChatTheme.getTextAppearanceBodyRegular(context),

                // Selection / Checkbox
                checkBoxStrokeWidth = typedArray?.getDimensionPixelSize(
                    R.styleable.CometChatGroups_cometchatGroupsCheckBoxStrokeWidth, 0
                ) ?: 0,
                checkBoxCornerRadius = typedArray?.getDimensionPixelSize(
                    R.styleable.CometChatGroups_cometchatGroupsCheckBoxCornerRadius, 0
                ) ?: 0,
                checkBoxStrokeColor = typedArray?.getColor(
                    R.styleable.CometChatGroups_cometchatGroupsCheckBoxStrokeColor,
                    CometChatTheme.getStrokeColorDefault(context)
                ) ?: CometChatTheme.getStrokeColorDefault(context),
                checkBoxBackgroundColor = typedArray?.getColor(
                    R.styleable.CometChatGroups_cometchatGroupsCheckBoxBackgroundColor,
                    CometChatTheme.getBackgroundColor1(context)
                ) ?: CometChatTheme.getBackgroundColor1(context),
                checkBoxCheckedBackgroundColor = typedArray?.getColor(
                    R.styleable.CometChatGroups_cometchatGroupsCheckBoxCheckedBackgroundColor,
                    CometChatTheme.getPrimaryColor(context)
                ) ?: CometChatTheme.getPrimaryColor(context),
                checkBoxSelectIcon = typedArray?.getDrawable(
                    R.styleable.CometChatGroups_cometchatGroupsCheckBoxSelectIcon
                ),
                checkBoxSelectIconTint = typedArray?.getColor(
                    R.styleable.CometChatGroups_cometchatGroupsCheckBoxSelectIconTint,
                    CometChatTheme.getColorWhite(context)
                ) ?: CometChatTheme.getColorWhite(context),
                discardSelectionIcon = typedArray?.getDrawable(
                    R.styleable.CometChatGroups_cometchatGroupsDiscardSelectionIcon
                ),
                discardSelectionIconTint = typedArray?.getColor(
                    R.styleable.CometChatGroups_cometchatGroupsDiscardSelectionIconTint,
                    CometChatTheme.getIconTintPrimary(context)
                ) ?: CometChatTheme.getIconTintPrimary(context),
                submitSelectionIcon = typedArray?.getDrawable(
                    R.styleable.CometChatGroups_cometchatGroupsSubmitSelectionIcon
                ),
                submitSelectionIconTint = typedArray?.getColor(
                    R.styleable.CometChatGroups_cometchatGroupsSubmitSelectionIconTint,
                    CometChatTheme.getPrimaryColor(context)
                ) ?: CometChatTheme.getPrimaryColor(context),

                // Separator
                separatorHeight = typedArray?.getDimensionPixelSize(
                    R.styleable.CometChatGroups_cometchatGroupsSeparatorHeight, 1
                ) ?: 1,
                separatorColor = typedArray?.getColor(
                    R.styleable.CometChatGroups_cometchatGroupsSeparatorColor,
                    CometChatTheme.getStrokeColorLight(context)
                ) ?: CometChatTheme.getStrokeColorLight(context),

                // Nested component style resource IDs
                avatarStyleResId = avatarStyleResId,
                statusIndicatorStyleResId = statusIndicatorStyleResId,
                optionListStyleResId = optionListStyleResId,

                // Item style
                itemStyle = itemStyle
            )
        }
    }
}
