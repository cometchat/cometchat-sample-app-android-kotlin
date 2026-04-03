package com.cometchat.uikit.kotlin.presentation.shared.typingindicator

import android.content.Context
import android.content.res.TypedArray
import androidx.annotation.ColorInt
import androidx.annotation.StyleRes
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.theme.CometChatTheme

/**
 * Style configuration for CometChatTypingIndicator component.
 */
data class CometChatTypingIndicatorStyle(
    @ColorInt val textColor: Int = 0,
    @StyleRes val textAppearance: Int = 0,
    @ColorInt val dotColor: Int = 0,
    val animationDuration: Long = 300L
) {
    companion object {
        /**
         * Creates a default style by extracting values from the theme's cometchatTypingIndicatorStyle.
         *
         * This method obtains the style resource from the theme attribute and extracts
         * all styling properties including typography (text appearances) from it.
         *
         * @param context The context to access theme resources
         * @return A CometChatTypingIndicatorStyle with values from theme or fallback defaults
         */
        fun default(context: Context): CometChatTypingIndicatorStyle {
            return extractFromThemeStyle(context)
        }

        /**
         * Extracts the typing indicator style from the theme's cometchatTypingIndicatorStyle attribute.
         *
         * @param context The context to access theme resources
         * @return A CometChatTypingIndicatorStyle with values from the theme style
         */
        private fun extractFromThemeStyle(context: Context): CometChatTypingIndicatorStyle {
            val themeTypedArray = context.obtainStyledAttributes(intArrayOf(R.attr.cometchatTypingIndicatorStyle))
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
         * @return A CometChatTypingIndicatorStyle with values from the style resource or defaults
         */
        private fun extractFromStyleResource(context: Context, styleResId: Int): CometChatTypingIndicatorStyle {
            if (styleResId == 0) {
                return extractFromTypedArray(context, null)
            }
            val typedArray = context.obtainStyledAttributes(styleResId, R.styleable.CometChatTypingIndicator)
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
         * @return A CometChatTypingIndicatorStyle with values from XML or theme defaults
         */
        fun fromTypedArray(context: Context, typedArray: TypedArray): CometChatTypingIndicatorStyle {
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
         * @return A CometChatTypingIndicatorStyle with extracted values or theme defaults
         */
        private fun extractFromTypedArray(context: Context, typedArray: TypedArray?): CometChatTypingIndicatorStyle {
            return CometChatTypingIndicatorStyle(
                // Text properties
                textColor = typedArray?.getColor(
                    R.styleable.CometChatTypingIndicator_cometchatTypingIndicatorTextColor,
                    CometChatTheme.getTextColorHighlight(context)
                ) ?: CometChatTheme.getTextColorHighlight(context),
                textAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatTypingIndicator_cometchatTypingIndicatorTextAppearance,
                    CometChatTheme.getTextAppearanceBodyRegular(context)
                ) ?: CometChatTheme.getTextAppearanceBodyRegular(context),
                // Dot color defaults to text color
                dotColor = typedArray?.getColor(
                    R.styleable.CometChatTypingIndicator_cometchatTypingIndicatorTextColor,
                    CometChatTheme.getTextColorHighlight(context)
                ) ?: CometChatTheme.getTextColorHighlight(context),
                animationDuration = 300L
            )
        }
    }
}
