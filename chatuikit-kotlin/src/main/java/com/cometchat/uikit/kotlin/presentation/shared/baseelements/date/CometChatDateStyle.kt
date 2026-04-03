package com.cometchat.uikit.kotlin.presentation.shared.baseelements.date

import android.content.Context
import android.content.res.TypedArray
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.StyleRes
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.theme.CometChatTheme

/**
 * Style configuration for CometChatDate component.
 */
data class CometChatDateStyle(
    @ColorInt val backgroundColor: Int = 0,
    @StyleRes val textAppearance: Int = 0,
    @ColorInt val textColor: Int = 0,
    @Dimension val cornerRadius: Int = 0,
    @Dimension val strokeWidth: Int = 0,
    @ColorInt val strokeColor: Int = 0
) {
    companion object {
        /**
         * Creates a default style by extracting values from the theme's cometchatDateStyle.
         *
         * @param context The context to access theme resources
         * @return A CometChatDateStyle with values from theme or fallback defaults
         */
        fun default(context: Context): CometChatDateStyle {
            return extractFromThemeStyle(context, R.attr.cometchatDateStyle)
        }

        /**
         * Extracts the date style from a theme style attribute.
         *
         * @param context The context to access theme resources
         * @param themeStyleAttr The attribute reference to the date style in theme
         * @return A CometChatDateStyle with values from the theme style
         */
        private fun extractFromThemeStyle(context: Context, themeStyleAttr: Int): CometChatDateStyle {
            val themeTypedArray = context.obtainStyledAttributes(intArrayOf(themeStyleAttr))
            val styleResId = themeTypedArray.getResourceId(0, 0)
            themeTypedArray.recycle()

            return extractFromStyleResource(context, styleResId)
        }

        /**
         * Creates a style from a specific style resource ID.
         * If styleResId is 0, returns a style with CometChatTheme defaults.
         *
         * @param context The context to access theme resources
         * @param styleResId The style resource ID to extract from (0 for defaults only)
         * @return A CometChatDateStyle with values from the style resource or defaults
         */
        fun fromStyleResource(context: Context, styleResId: Int): CometChatDateStyle {
            return extractFromStyleResource(context, styleResId)
        }

        /**
         * Extracts style values from a specific style resource ID.
         * If styleResId is 0, returns a style with CometChatTheme defaults.
         *
         * @param context The context to access theme resources
         * @param styleResId The style resource ID to extract from (0 for defaults only)
         * @return A CometChatDateStyle with values from the style resource or defaults
         */
        private fun extractFromStyleResource(context: Context, styleResId: Int): CometChatDateStyle {
            if (styleResId == 0) {
                return extractFromTypedArray(context, null)
            }
            val typedArray = context.obtainStyledAttributes(styleResId, R.styleable.CometChatDate)
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
         * @return A CometChatDateStyle with values from XML or theme defaults
         */
        fun fromTypedArray(context: Context, typedArray: TypedArray): CometChatDateStyle {
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
         * @return A CometChatDateStyle with extracted values or theme defaults
         */
        private fun extractFromTypedArray(context: Context, typedArray: TypedArray?): CometChatDateStyle {
            return CometChatDateStyle(
                // Background color
                backgroundColor = typedArray?.getColor(
                    R.styleable.CometChatDate_cometchatDateBackgroundColor,
                    0
                ) ?: 0,
                // Text properties
                textAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatDate_cometchatDateTextAppearance,
                    CometChatTheme.getTextAppearanceCaption1Regular(context)
                ) ?: CometChatTheme.getTextAppearanceCaption1Regular(context),
                textColor = typedArray?.getColor(
                    R.styleable.CometChatDate_cometchatDateTextColor,
                    CometChatTheme.getTextColorPrimary(context)
                ) ?: CometChatTheme.getTextColorPrimary(context),
                // Shape properties
                cornerRadius = typedArray?.getDimensionPixelSize(
                    R.styleable.CometChatDate_cometchatDateCornerRadius,
                    0
                ) ?: 0,
                // Stroke properties
                strokeWidth = typedArray?.getDimensionPixelSize(
                    R.styleable.CometChatDate_cometchatDateStrokeWidth,
                    0
                ) ?: 0,
                strokeColor = typedArray?.getColor(
                    R.styleable.CometChatDate_cometchatDateStrokeColor,
                    0
                ) ?: 0
            )
        }
    }
}
