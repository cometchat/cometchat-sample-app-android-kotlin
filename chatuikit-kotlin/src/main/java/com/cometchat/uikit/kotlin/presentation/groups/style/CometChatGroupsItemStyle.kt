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
import com.cometchat.uikit.kotlin.presentation.shared.baseelements.avatar.CometChatAvatarStyle
import com.cometchat.uikit.kotlin.presentation.shared.statusindicator.CometChatStatusIndicatorStyle

/**
 * Style configuration for CometChatGroupsItem component.
 * Mirrors the XML attributes from attr_cometchat_groups.xml
 */
data class CometChatGroupsItemStyle(
    // Background
    @ColorInt val backgroundColor: Int = 0,
    @ColorInt val selectedBackgroundColor: Int = 0,
    
    // Title
    @ColorInt val titleTextColor: Int = 0,
    @StyleRes val titleTextAppearance: Int = 0,
    
    // Subtitle (member count)
    @ColorInt val subtitleTextColor: Int = 0,
    @StyleRes val subtitleTextAppearance: Int = 0,
    
    // Separator
    @ColorInt val separatorColor: Int = 0,
    @Dimension val separatorHeight: Int = 0,
    
    // Component styles
    val avatarStyle: CometChatAvatarStyle = CometChatAvatarStyle(),
    val statusIndicatorStyle: CometChatStatusIndicatorStyle = CometChatStatusIndicatorStyle(),
    
    // Checkbox styling (for selection mode)
    @Dimension val checkBoxStrokeWidth: Int = 0,
    @Dimension val checkBoxCornerRadius: Int = 0,
    @ColorInt val checkBoxStrokeColor: Int = 0,
    @ColorInt val checkBoxBackgroundColor: Int = 0,
    @ColorInt val checkBoxCheckedBackgroundColor: Int = 0,
    val checkBoxSelectIcon: Drawable? = null,
    @ColorInt val checkBoxSelectIconTint: Int = 0
) {
    companion object {
        /**
         * Creates a default style by extracting values from the theme's cometchatGroupsStyle.
         */
        fun default(context: Context): CometChatGroupsItemStyle {
            return extractFromThemeStyle(context)
        }

        private fun extractFromThemeStyle(context: Context): CometChatGroupsItemStyle {
            val themeTypedArray = context.obtainStyledAttributes(intArrayOf(R.attr.cometchatGroupsStyle))
            val styleResId = themeTypedArray.getResourceId(0, 0)
            themeTypedArray.recycle()
            return extractFromStyleResource(context, styleResId)
        }

        private fun extractFromStyleResource(context: Context, styleResId: Int): CometChatGroupsItemStyle {
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

        fun fromTypedArray(context: Context, typedArray: TypedArray): CometChatGroupsItemStyle {
            return try {
                extractFromTypedArray(context, typedArray)
            } finally {
                typedArray.recycle()
            }
        }

        private fun extractFromTypedArray(context: Context, typedArray: TypedArray?): CometChatGroupsItemStyle {
            // Extract nested style resource IDs
            val avatarStyleResId = typedArray?.getResourceId(
                R.styleable.CometChatGroups_cometchatGroupsAvatarStyle, 0
            ) ?: 0
            val statusIndicatorStyleResId = typedArray?.getResourceId(
                R.styleable.CometChatGroups_cometchatGroupsStatusIndicatorStyle, 0
            ) ?: 0

            // Create nested styles from resource IDs or use defaults
            val avatarStyle = if (avatarStyleResId != 0) {
                val avatarTypedArray = context.theme.obtainStyledAttributes(
                    avatarStyleResId, R.styleable.CometChatAvatar
                )
                CometChatAvatarStyle.fromTypedArray(context, avatarTypedArray)
            } else {
                CometChatAvatarStyle.default(context)
            }

            val statusIndicatorStyle = if (statusIndicatorStyleResId != 0) {
                val statusIndicatorTypedArray = context.theme.obtainStyledAttributes(
                    statusIndicatorStyleResId, R.styleable.CometChatStatusIndicator
                )
                CometChatStatusIndicatorStyle.fromTypedArray(context, statusIndicatorTypedArray)
            } else {
                CometChatStatusIndicatorStyle.default(context)
            }

            return CometChatGroupsItemStyle(
                // Background styling
                backgroundColor = typedArray?.getColor(
                    R.styleable.CometChatGroups_cometchatGroupsItemBackgroundColor,
                    Color.TRANSPARENT
                ) ?: Color.TRANSPARENT,
                selectedBackgroundColor = typedArray?.getColor(
                    R.styleable.CometChatGroups_cometchatGroupsItemSelectedBackgroundColor,
                    CometChatTheme.getBackgroundColor3(context)
                ) ?: CometChatTheme.getBackgroundColor3(context),

                // Title styling
                titleTextColor = typedArray?.getColor(
                    R.styleable.CometChatGroups_cometchatGroupsItemTitleTextColor,
                    CometChatTheme.getTextColorPrimary(context)
                ) ?: CometChatTheme.getTextColorPrimary(context),
                titleTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatGroups_cometchatGroupsItemTitleTextAppearance,
                    CometChatTheme.getTextAppearanceHeading4Medium(context)
                ) ?: CometChatTheme.getTextAppearanceHeading4Medium(context),

                // Subtitle styling
                subtitleTextColor = typedArray?.getColor(
                    R.styleable.CometChatGroups_cometchatGroupsSubtitleColor,
                    CometChatTheme.getTextColorSecondary(context)
                ) ?: CometChatTheme.getTextColorSecondary(context),
                subtitleTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatGroups_cometchatGroupsSubtitleTextAppearance,
                    CometChatTheme.getTextAppearanceBodyRegular(context)
                ) ?: CometChatTheme.getTextAppearanceBodyRegular(context),

                // Separator styling
                separatorColor = typedArray?.getColor(
                    R.styleable.CometChatGroups_cometchatGroupsSeparatorColor,
                    CometChatTheme.getStrokeColorLight(context)
                ) ?: CometChatTheme.getStrokeColorLight(context),
                separatorHeight = typedArray?.getDimensionPixelSize(
                    R.styleable.CometChatGroups_cometchatGroupsSeparatorHeight, 1
                ) ?: 1,

                // Checkbox styling
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

                // Component styles
                avatarStyle = avatarStyle,
                statusIndicatorStyle = statusIndicatorStyle
            )
        }
    }
}
