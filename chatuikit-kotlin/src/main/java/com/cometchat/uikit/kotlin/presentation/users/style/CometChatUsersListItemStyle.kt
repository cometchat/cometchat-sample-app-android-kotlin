package com.cometchat.uikit.kotlin.presentation.users.style

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
 * Style configuration for CometChatUsersListItem component.
 */
data class CometChatUsersListItemStyle(
    // Background
    @ColorInt val backgroundColor: Int = 0,
    @ColorInt val selectedBackgroundColor: Int = 0,
    
    // Title
    @ColorInt val titleTextColor: Int = 0,
    @StyleRes val titleTextAppearance: Int = 0,
    
    // Separator
    @ColorInt val separatorColor: Int = 0,
    @Dimension val separatorHeight: Int = 0,
    
    // Checkbox
    @Dimension val checkBoxStrokeWidth: Int = 0,
    @Dimension val checkBoxCornerRadius: Int = 0,
    @ColorInt val checkBoxStrokeColor: Int = 0,
    @ColorInt val checkBoxBackgroundColor: Int = 0,
    @ColorInt val checkBoxCheckedBackgroundColor: Int = 0,
    val checkBoxSelectIcon: Drawable? = null,
    @ColorInt val checkBoxSelectIconTint: Int = 0,
    
    // Component styles
    val avatarStyle: CometChatAvatarStyle = CometChatAvatarStyle(),
    val statusIndicatorStyle: CometChatStatusIndicatorStyle = CometChatStatusIndicatorStyle()
) {
    companion object {
        /**
         * Creates a default style by extracting values from the theme's cometchatUsersStyle.
         */
        fun default(context: Context): CometChatUsersListItemStyle {
            return extractFromThemeStyle(context)
        }

        private fun extractFromThemeStyle(context: Context): CometChatUsersListItemStyle {
            val themeTypedArray = context.obtainStyledAttributes(intArrayOf(R.attr.cometchatUsersStyle))
            val styleResId = themeTypedArray.getResourceId(0, 0)
            themeTypedArray.recycle()
            return extractFromStyleResource(context, styleResId)
        }

        private fun extractFromStyleResource(context: Context, styleResId: Int): CometChatUsersListItemStyle {
            if (styleResId == 0) {
                return extractFromTypedArrayInternal(context, null)
            }
            val typedArray = context.obtainStyledAttributes(styleResId, R.styleable.CometChatUsers)
            return try {
                extractFromTypedArrayInternal(context, typedArray)
            } finally {
                typedArray.recycle()
            }
        }

        fun fromTypedArray(context: Context, typedArray: TypedArray): CometChatUsersListItemStyle {
            return try {
                extractFromTypedArrayInternal(context, typedArray)
            } finally {
                typedArray.recycle()
            }
        }

        /**
         * Internal extraction method that does NOT recycle the TypedArray.
         * Used by CometChatUsersStyle to extract item style from the same TypedArray.
         */
        internal fun extractFromTypedArrayInternal(context: Context, typedArray: TypedArray?): CometChatUsersListItemStyle {
            // Extract nested style resource IDs
            val avatarStyleResId = typedArray?.getResourceId(
                R.styleable.CometChatUsers_cometchatUsersAvatarStyle, 0
            ) ?: 0
            val statusIndicatorStyleResId = typedArray?.getResourceId(
                R.styleable.CometChatUsers_cometchatUsersStatusIndicator, 0
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

            return CometChatUsersListItemStyle(
                // Background styling
                backgroundColor = typedArray?.getColor(
                    R.styleable.CometChatUsers_cometchatUsersItemBackgroundColor,
                    Color.TRANSPARENT
                ) ?: Color.TRANSPARENT,
                selectedBackgroundColor = typedArray?.getColor(
                    R.styleable.CometChatUsers_cometchatUsersItemSelectedBackgroundColor,
                    CometChatTheme.getBackgroundColor3(context)
                ) ?: CometChatTheme.getBackgroundColor3(context),

                // Title styling
                titleTextColor = typedArray?.getColor(
                    R.styleable.CometChatUsers_cometchatUsersItemTitleTextColor,
                    CometChatTheme.getTextColorPrimary(context)
                ) ?: CometChatTheme.getTextColorPrimary(context),
                titleTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatUsers_cometchatUsersItemTitleTextAppearance,
                    CometChatTheme.getTextAppearanceHeading4Medium(context)
                ) ?: CometChatTheme.getTextAppearanceHeading4Medium(context),

                // Separator styling
                separatorColor = typedArray?.getColor(
                    R.styleable.CometChatUsers_cometchatUsersSeparatorColor,
                    CometChatTheme.getStrokeColorLight(context)
                ) ?: CometChatTheme.getStrokeColorLight(context),
                separatorHeight = 1,

                // Checkbox styling
                checkBoxStrokeWidth = typedArray?.getDimensionPixelSize(
                    R.styleable.CometChatUsers_cometchatUsersCheckBoxStrokeWidth, 0
                ) ?: 0,
                checkBoxCornerRadius = typedArray?.getDimensionPixelSize(
                    R.styleable.CometChatUsers_cometchatUsersCheckBoxCornerRadius, 0
                ) ?: 0,
                checkBoxStrokeColor = typedArray?.getColor(
                    R.styleable.CometChatUsers_cometchatUsersCheckBoxStrokeColor,
                    CometChatTheme.getStrokeColorDefault(context)
                ) ?: CometChatTheme.getStrokeColorDefault(context),
                checkBoxBackgroundColor = typedArray?.getColor(
                    R.styleable.CometChatUsers_cometchatUsersCheckBoxBackgroundColor,
                    CometChatTheme.getBackgroundColor1(context)
                ) ?: CometChatTheme.getBackgroundColor1(context),
                checkBoxCheckedBackgroundColor = typedArray?.getColor(
                    R.styleable.CometChatUsers_cometchatUsersCheckBoxCheckedBackgroundColor,
                    CometChatTheme.getPrimaryColor(context)
                ) ?: CometChatTheme.getPrimaryColor(context),
                checkBoxSelectIcon = typedArray?.getDrawable(
                    R.styleable.CometChatUsers_cometchatUsersCheckBoxSelectIcon
                ),
                checkBoxSelectIconTint = typedArray?.getColor(
                    R.styleable.CometChatUsers_cometchatUsersCheckBoxSelectIconTint,
                    CometChatTheme.getColorWhite(context)
                ) ?: CometChatTheme.getColorWhite(context),

                // Component styles
                avatarStyle = avatarStyle,
                statusIndicatorStyle = statusIndicatorStyle
            )
        }
    }
}
