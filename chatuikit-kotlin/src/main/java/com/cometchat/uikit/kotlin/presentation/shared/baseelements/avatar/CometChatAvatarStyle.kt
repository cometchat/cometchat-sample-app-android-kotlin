package com.cometchat.uikit.kotlin.presentation.shared.baseelements.avatar

import android.content.Context
import android.content.res.TypedArray
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.StyleRes
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.theme.CometChatTheme

/**
 * Style configuration for CometChatAvatar component.
 * 
 * This data class holds all styling properties for the avatar view,
 * including background color, stroke properties, and text appearance.
 */
data class CometChatAvatarStyle(
    @ColorInt val backgroundColor: Int = 0,
    @ColorInt val strokeColor: Int = 0,
    @Dimension val strokeWidth: Float = 0f,
    @Dimension val cornerRadius: Float = 0f,
    @StyleRes val placeHolderTextAppearance: Int = 0,
    @ColorInt val placeHolderTextColor: Int = 0
) {
    companion object {
        /**
         * Creates a default style by extracting values from the theme's cometchatAvatarStyle.
         *
         * @param context The context to access theme resources
         * @return A CometChatAvatarStyle with values from theme or fallback defaults
         */
        fun default(context: Context): CometChatAvatarStyle {
            return extractFromThemeStyle(context, R.attr.cometchatAvatarStyle)
        }

        /**
         * Extracts the avatar style from a theme style attribute.
         *
         * @param context The context to access theme resources
         * @param themeStyleAttr The attribute reference to the avatar style in theme
         * @return A CometChatAvatarStyle with values from the theme style
         */
        private fun extractFromThemeStyle(context: Context, themeStyleAttr: Int): CometChatAvatarStyle {
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
         * @return A CometChatAvatarStyle with values from the style resource or defaults
         */
        private fun extractFromStyleResource(context: Context, styleResId: Int): CometChatAvatarStyle {
            if (styleResId == 0) {
                return extractFromTypedArray(context, null)
            }
            val typedArray = context.obtainStyledAttributes(styleResId, R.styleable.CometChatAvatar)
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
         * @return A CometChatAvatarStyle with values from XML or theme defaults
         */
        fun fromTypedArray(context: Context, typedArray: TypedArray): CometChatAvatarStyle {
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
         * @return A CometChatAvatarStyle with extracted values or theme defaults
         */
        private fun extractFromTypedArray(context: Context, typedArray: TypedArray?): CometChatAvatarStyle {
            return CometChatAvatarStyle(
                // Background color
                backgroundColor = typedArray?.getColor(
                    R.styleable.CometChatAvatar_cometchatAvatarBackgroundColor,
                    CometChatTheme.getExtendedPrimaryColor500(context)
                ) ?: CometChatTheme.getExtendedPrimaryColor500(context),
                // Stroke properties
                strokeColor = typedArray?.getColor(
                    R.styleable.CometChatAvatar_cometchatAvatarStrokeColor,
                    0
                ) ?: 0,
                strokeWidth = typedArray?.getDimension(
                    R.styleable.CometChatAvatar_cometchatAvatarStrokeWidth,
                    0f
                ) ?: 0f,
                cornerRadius = typedArray?.getDimension(
                    R.styleable.CometChatAvatar_cometchatAvatarStrokeRadius,
                    Float.MAX_VALUE
                ) ?: Float.MAX_VALUE,
                // Text appearance
                placeHolderTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatAvatar_cometchatAvatarPlaceHolderTextAppearance,
                    CometChatTheme.getTextAppearanceHeading2Bold(context)
                ) ?: CometChatTheme.getTextAppearanceHeading2Bold(context),
                placeHolderTextColor = typedArray?.getColor(
                    R.styleable.CometChatAvatar_cometchatAvatarPlaceHolderTextColor,
                    CometChatTheme.getPrimaryButtonIconTint(context)
                ) ?: CometChatTheme.getPrimaryButtonIconTint(context)
            )
        }
    }
}
