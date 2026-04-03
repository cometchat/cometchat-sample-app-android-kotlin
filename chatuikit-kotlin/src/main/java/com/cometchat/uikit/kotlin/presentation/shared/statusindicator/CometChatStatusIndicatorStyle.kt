package com.cometchat.uikit.kotlin.presentation.shared.statusindicator

import android.content.Context
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.theme.CometChatTheme

/**
 * Style configuration for CometChatStatusIndicator component.
 */
data class CometChatStatusIndicatorStyle(
    @ColorInt val strokeColor: Int = 0,
    @Dimension val strokeWidth: Float = 0f,
    @Dimension val cornerRadius: Float = 0f,
    val onlineIcon: Drawable? = null,
    val privateGroupIcon: Drawable? = null,
    val protectedGroupIcon: Drawable? = null
) {
    companion object {
        /**
         * Creates a default style by extracting values from the theme's cometchatStatusIndicatorStyle.
         *
         * @param context The context to access theme resources
         * @return A CometChatStatusIndicatorStyle with values from theme or fallback defaults
         */
        fun default(context: Context): CometChatStatusIndicatorStyle {
            return extractFromThemeStyle(context, R.attr.cometchatStatusIndicatorStyle)
        }

        /**
         * Extracts the status indicator style from a theme style attribute.
         *
         * @param context The context to access theme resources
         * @param themeStyleAttr The attribute reference to the status indicator style in theme
         * @return A CometChatStatusIndicatorStyle with values from the theme style
         */
        private fun extractFromThemeStyle(context: Context, themeStyleAttr: Int): CometChatStatusIndicatorStyle {
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
         * @return A CometChatStatusIndicatorStyle with values from the style resource or defaults
         */
        private fun extractFromStyleResource(context: Context, styleResId: Int): CometChatStatusIndicatorStyle {
            if (styleResId == 0) {
                return extractFromTypedArray(context, null)
            }
            val typedArray = context.obtainStyledAttributes(styleResId, R.styleable.CometChatStatusIndicator)
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
         * @return A CometChatStatusIndicatorStyle with values from XML or theme defaults
         */
        fun fromTypedArray(context: Context, typedArray: TypedArray): CometChatStatusIndicatorStyle {
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
         * @return A CometChatStatusIndicatorStyle with extracted values or theme defaults
         */
        private fun extractFromTypedArray(context: Context, typedArray: TypedArray?): CometChatStatusIndicatorStyle {
            return CometChatStatusIndicatorStyle(
                // Stroke properties
                strokeColor = typedArray?.getColor(
                    R.styleable.CometChatStatusIndicator_cometchatStatusIndicatorStrokeColor,
                    CometChatTheme.getBackgroundColor1(context)
                ) ?: CometChatTheme.getBackgroundColor1(context),
                strokeWidth = typedArray?.getDimension(
                    R.styleable.CometChatStatusIndicator_cometchatStatusIndicatorStrokeWidth,
                    0f
                ) ?: 0f,
                cornerRadius = typedArray?.getDimension(
                    R.styleable.CometChatStatusIndicator_cometchatStatusIndicatorCornerRadius,
                    Float.MAX_VALUE
                ) ?: Float.MAX_VALUE,
                // Icon drawables
                onlineIcon = typedArray?.getDrawable(
                    R.styleable.CometChatStatusIndicator_cometchatStatusIndicatorOnlineIcon
                ),
                privateGroupIcon = typedArray?.getDrawable(
                    R.styleable.CometChatStatusIndicator_cometchatStatusIndicatorPrivateGroupIcon
                ),
                protectedGroupIcon = typedArray?.getDrawable(
                    R.styleable.CometChatStatusIndicator_cometchatStatusIndicatorProtectedGroupIcon
                )
            )
        }
    }
}
