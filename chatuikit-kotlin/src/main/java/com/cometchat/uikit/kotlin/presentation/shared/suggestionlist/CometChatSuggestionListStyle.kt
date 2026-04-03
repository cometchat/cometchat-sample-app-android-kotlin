package com.cometchat.uikit.kotlin.presentation.shared.suggestionlist

import android.content.Context
import android.content.res.TypedArray
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.StyleRes
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.theme.CometChatTheme

/**
 * Style configuration for CometChatSuggestionList component.
 * 
 * This class holds all styling properties for the suggestion list including
 * background colors, stroke properties, text styles, and avatar styles.
 */
data class CometChatSuggestionListStyle(
    @ColorInt var backgroundColor: Int = 0,
    @ColorInt var strokeColor: Int = 0,
    @Dimension var strokeWidth: Int = 0,
    @Dimension var cornerRadius: Int = 0,
    @Dimension var maxHeight: Int = 0,
    @StyleRes var itemAvatarStyle: Int = 0,
    @StyleRes var itemTextAppearance: Int = 0,
    @ColorInt var itemTextColor: Int = 0,
    @StyleRes var itemInfoTextAppearance: Int = 0,
    @ColorInt var itemInfoTextColor: Int = 0,
    @ColorInt var separatorColor: Int = 0,
    @Dimension var separatorHeight: Int = 0
) {
    companion object {
        /**
         * Creates a default CometChatSuggestionListStyle by extracting values from the theme's
         * cometchatSuggestionListStyle attribute.
         * 
         * @param context The context to resolve theme colors
         * @return A CometChatSuggestionListStyle with values from theme or fallback defaults
         */
        fun default(context: Context): CometChatSuggestionListStyle {
            return extractFromThemeStyle(context)
        }

        /**
         * Extracts the suggestion list style from the theme attribute.
         *
         * @param context The context to access theme resources
         * @return A CometChatSuggestionListStyle with values from the theme style
         */
        private fun extractFromThemeStyle(context: Context): CometChatSuggestionListStyle {
            val themeTypedArray = context.obtainStyledAttributes(intArrayOf(R.attr.cometchatSuggestionListStyle))
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
         * @return A CometChatSuggestionListStyle with values from the style resource or defaults
         */
        private fun extractFromStyleResource(context: Context, styleResId: Int): CometChatSuggestionListStyle {
            if (styleResId == 0) {
                return extractFromTypedArray(context, null)
            }
            val typedArray = context.obtainStyledAttributes(styleResId, R.styleable.CometChatSuggestionList)
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
         * @return A CometChatSuggestionListStyle with values from XML or theme defaults
         */
        fun fromTypedArray(context: Context, typedArray: TypedArray): CometChatSuggestionListStyle {
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
         * @return A CometChatSuggestionListStyle with extracted values or theme defaults
         */
        private fun extractFromTypedArray(context: Context, typedArray: TypedArray?): CometChatSuggestionListStyle {
            val resources = context.resources
            return CometChatSuggestionListStyle(
                backgroundColor = typedArray?.getColor(
                    R.styleable.CometChatSuggestionList_cometchatSuggestionListBackgroundColor,
                    CometChatTheme.getBackgroundColor1(context)
                ) ?: CometChatTheme.getBackgroundColor1(context),

                strokeColor = typedArray?.getColor(
                    R.styleable.CometChatSuggestionList_cometchatSuggestionListStrokeColor,
                    CometChatTheme.getBorderColorLight(context)
                ) ?: CometChatTheme.getBorderColorLight(context),

                strokeWidth = typedArray?.getDimensionPixelSize(
                    R.styleable.CometChatSuggestionList_cometchatSuggestionListStrokeWidth,
                    resources.getDimensionPixelSize(R.dimen.cometchat_1dp)
                ) ?: resources.getDimensionPixelSize(R.dimen.cometchat_1dp),

                cornerRadius = typedArray?.getDimensionPixelSize(
                    R.styleable.CometChatSuggestionList_cometchatSuggestionListCornerRadius,
                    resources.getDimensionPixelSize(R.dimen.cometchat_corner_radius_2)
                ) ?: resources.getDimensionPixelSize(R.dimen.cometchat_corner_radius_2),

                maxHeight = resources.getDimensionPixelSize(R.dimen.cometchat_200dp),

                itemAvatarStyle = typedArray?.getResourceId(
                    R.styleable.CometChatSuggestionList_cometchatSuggestionListItemAvatarStyle,
                    R.style.CometChatAvatarStyle
                ) ?: R.style.CometChatAvatarStyle,

                itemTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatSuggestionList_cometchatSuggestionListItemTextAppearance,
                    CometChatTheme.getTextAppearanceHeading4Medium(context)
                ) ?: CometChatTheme.getTextAppearanceHeading4Medium(context),

                itemTextColor = typedArray?.getColor(
                    R.styleable.CometChatSuggestionList_cometchatSuggestionListItemTextColor,
                    CometChatTheme.getTextColorPrimary(context)
                ) ?: CometChatTheme.getTextColorPrimary(context),

                itemInfoTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatSuggestionList_cometchatSuggestionListItemInfoTextAppearance,
                    CometChatTheme.getTextAppearanceBodyRegular(context)
                ) ?: CometChatTheme.getTextAppearanceBodyRegular(context),

                itemInfoTextColor = typedArray?.getColor(
                    R.styleable.CometChatSuggestionList_cometchatSuggestionListItemInfoTextColor,
                    CometChatTheme.getTextColorSecondary(context)
                ) ?: CometChatTheme.getTextColorSecondary(context),

                separatorColor = CometChatTheme.getBorderColorLight(context),

                separatorHeight = resources.getDimensionPixelSize(R.dimen.cometchat_1dp) / 2
            )
        }
    }
}
