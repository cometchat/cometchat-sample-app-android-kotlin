package com.cometchat.uikit.kotlin.presentation.shared.baseelements.badgecount

import android.content.Context
import android.content.res.TypedArray
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.StyleRes
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.theme.CometChatTheme

/**
 * Style configuration for CometChatBadgeCount component.
 */
data class CometChatBadgeCountStyle(
    @ColorInt val backgroundColor: Int = 0,
    @ColorInt val textColor: Int = 0,
    @StyleRes val textAppearance: Int = 0,
    @Dimension val cornerRadius: Float = 0f,
    @ColorInt val borderColor: Int = 0,
    @Dimension val borderWidth: Float = 0f
) {
    companion object {
        /**
         * Creates a default style by extracting values from the theme's cometchatBadgeStyle.
         *
         * @param context The context to access theme resources
         * @return A CometChatBadgeCountStyle with values from theme or fallback defaults
         */
        fun default(context: Context): CometChatBadgeCountStyle {
            return extractFromThemeStyle(context, R.attr.cometchatBadgeStyle)
        }

        /**
         * Extracts the badge style from a theme style attribute.
         *
         * @param context The context to access theme resources
         * @param themeStyleAttr The attribute reference to the badge style in theme
         * @return A CometChatBadgeCountStyle with values from the theme style
         */
        private fun extractFromThemeStyle(context: Context, themeStyleAttr: Int): CometChatBadgeCountStyle {
            val themeTypedArray = context.obtainStyledAttributes(intArrayOf(themeStyleAttr))
            val styleResId = themeTypedArray.getResourceId(0, 0)
            themeTypedArray.recycle()

            return extractFromStyleResource(context, styleResId)
        }

        /**
         * Extracts style values from a specific style resource ID.
         * If styleResId is 0, returns a style with CometChatTheme defaults.
         *
         * @param context The context to access theme resources
         * @param styleResId The style resource ID to extract from (0 for defaults only)
         * @return A CometChatBadgeCountStyle with values from the style resource or defaults
         */
        private fun extractFromStyleResource(context: Context, styleResId: Int): CometChatBadgeCountStyle {
            if (styleResId == 0) {
                return extractFromTypedArray(context, null)
            }
            val typedArray = context.obtainStyledAttributes(styleResId, R.styleable.CometChatBadge)
            return try {
                extractFromTypedArray(context, typedArray)
            } finally {
                typedArray.recycle()
            }
        }

        /**
         * Creates a style by extracting values from XML TypedArray.
         *
         * This method handles TypedArray recycling internally using try-finally.
         *
         * @param context The Android context for accessing theme resources
         * @param typedArray The TypedArray containing XML attribute values (will be recycled)
         * @return A CometChatBadgeCountStyle with values from XML or theme defaults
         */
        fun fromTypedArray(context: Context, typedArray: TypedArray): CometChatBadgeCountStyle {
            return try {
                extractFromTypedArray(context, typedArray)
            } finally {
                typedArray.recycle()
            }
        }

        /**
         * Core extraction method that reads style values from a TypedArray.
         * Does NOT handle recycling - caller is responsible for recycling the TypedArray.
         *
         * @param context The Android context for accessing theme resources
         * @param typedArray The TypedArray to extract from, or null for defaults only
         * @return A CometChatBadgeCountStyle with extracted values or theme defaults
         */
        private fun extractFromTypedArray(context: Context, typedArray: TypedArray?): CometChatBadgeCountStyle {
            return CometChatBadgeCountStyle(
                // Background color
                backgroundColor = typedArray?.getColor(
                    R.styleable.CometChatBadge_cometchatBadgeBackgroundColor,
                    CometChatTheme.getPrimaryColor(context)
                ) ?: CometChatTheme.getPrimaryColor(context),
                // Text properties
                textColor = typedArray?.getColor(
                    R.styleable.CometChatBadge_cometchatBadgeTextColor,
                    CometChatTheme.getPrimaryButtonIconTint(context)
                ) ?: CometChatTheme.getPrimaryButtonIconTint(context),
                textAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatBadge_cometchatBadgeTextAppearance,
                    CometChatTheme.getTextAppearanceCaption1Regular(context)
                ) ?: CometChatTheme.getTextAppearanceCaption1Regular(context),
                // Shape properties
                cornerRadius = typedArray?.getDimension(
                    R.styleable.CometChatBadge_cometchatBadgeCornerRadius,
                    Float.MAX_VALUE
                ) ?: Float.MAX_VALUE,
                // Border properties
                borderColor = typedArray?.getColor(
                    R.styleable.CometChatBadge_cometchatBadgeStrokeColor,
                    0
                ) ?: 0,
                borderWidth = typedArray?.getDimension(
                    R.styleable.CometChatBadge_cometchatBadgeStrokeWidth,
                    0f
                ) ?: 0f
            )
        }
    }
}
