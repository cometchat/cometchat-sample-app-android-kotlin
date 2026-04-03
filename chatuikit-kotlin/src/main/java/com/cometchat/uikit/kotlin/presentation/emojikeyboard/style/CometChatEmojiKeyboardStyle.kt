package com.cometchat.uikit.kotlin.presentation.emojikeyboard.style

import android.content.Context
import android.content.res.TypedArray
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.StyleRes
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.theme.CometChatTheme

/**
 * Style configuration for CometChatEmojiKeyboard component.
 *
 * This data class holds all styling properties for the emoji keyboard view,
 * including container styling, category text styling, and category icon styling.
 *
 * @param backgroundColor Background color for the emoji keyboard container
 * @param cornerRadius Corner radius for the emoji keyboard container
 * @param strokeWidth Stroke width for the emoji keyboard container border
 * @param strokeColor Stroke color for the emoji keyboard container border
 * @param separatorColor Color for the separator line between content and tab bar
 * @param categoryTextColor Text color for category name headers
 * @param categoryTextAppearance Text appearance resource for category name headers
 * @param categoryIconTint Tint color for unselected category tab icons
 * @param selectedCategoryIconTint Tint color for the selected category tab icon
 * @param selectedCategoryBackgroundColor Background color for the selected category tab
 */
data class CometChatEmojiKeyboardStyle(
    @ColorInt val backgroundColor: Int = 0,
    @Dimension val cornerRadius: Int = 0,
    @Dimension val strokeWidth: Int = 0,
    @ColorInt val strokeColor: Int = 0,
    @ColorInt val separatorColor: Int = 0,
    @ColorInt val categoryTextColor: Int = 0,
    @StyleRes val categoryTextAppearance: Int = 0,
    @ColorInt val categoryIconTint: Int = 0,
    @ColorInt val selectedCategoryIconTint: Int = 0,
    @ColorInt val selectedCategoryBackgroundColor: Int = 0
) {
    companion object {
        /**
         * Creates a default style by extracting values from the theme's cometchatEmojiKeyboardStyle.
         *
         * @param context The context to access theme resources
         * @return A CometChatEmojiKeyboardStyle with values from theme or fallback defaults
         */
        fun default(context: Context): CometChatEmojiKeyboardStyle {
            return extractFromThemeStyle(context, R.attr.cometchatEmojiKeyboardStyle)
        }

        private fun extractFromThemeStyle(context: Context, themeStyleAttr: Int): CometChatEmojiKeyboardStyle {
            val themeTypedArray = context.obtainStyledAttributes(intArrayOf(themeStyleAttr))
            val styleResId = themeTypedArray.getResourceId(0, 0)
            themeTypedArray.recycle()
            return extractFromStyleResource(context, styleResId)
        }

        private fun extractFromStyleResource(context: Context, styleResId: Int): CometChatEmojiKeyboardStyle {
            if (styleResId == 0) {
                return extractFromTypedArray(context, null)
            }
            val typedArray = context.obtainStyledAttributes(styleResId, R.styleable.CometChatEmojiKeyBoardView)
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
         * @return A CometChatEmojiKeyboardStyle with values from XML or theme defaults
         */
        fun fromTypedArray(context: Context, typedArray: TypedArray): CometChatEmojiKeyboardStyle {
            return try {
                extractFromTypedArray(context, typedArray)
            } finally {
                typedArray.recycle()
            }
        }

        private fun extractFromTypedArray(
            context: Context,
            typedArray: TypedArray?
        ): CometChatEmojiKeyboardStyle {
            return CometChatEmojiKeyboardStyle(
                backgroundColor = typedArray?.getColor(
                    R.styleable.CometChatEmojiKeyBoardView_cometchatEmojiKeyboardBackgroundColor,
                    CometChatTheme.getBackgroundColor1(context)
                ) ?: CometChatTheme.getBackgroundColor1(context),
                cornerRadius = typedArray?.getDimensionPixelSize(
                    R.styleable.CometChatEmojiKeyBoardView_cometchatEmojiKeyboardCornerRadius,
                    0
                ) ?: 0,
                strokeWidth = typedArray?.getDimensionPixelSize(
                    R.styleable.CometChatEmojiKeyBoardView_cometchatEmojiKeyboardStrokeWidth,
                    0
                ) ?: 0,
                strokeColor = typedArray?.getColor(
                    R.styleable.CometChatEmojiKeyBoardView_cometchatEmojiKeyboardStrokeColor,
                    CometChatTheme.getStrokeColorLight(context)
                ) ?: CometChatTheme.getStrokeColorLight(context),
                separatorColor = typedArray?.getColor(
                    R.styleable.CometChatEmojiKeyBoardView_cometchatEmojiKeyboardSeparatorColor,
                    CometChatTheme.getStrokeColorDefault(context)
                ) ?: CometChatTheme.getStrokeColorDefault(context),
                categoryTextColor = typedArray?.getColor(
                    R.styleable.CometChatEmojiKeyBoardView_cometchatEmojiKeyboardCategoryTextColor,
                    CometChatTheme.getTextColorTertiary(context)
                ) ?: CometChatTheme.getTextColorTertiary(context),
                categoryTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatEmojiKeyBoardView_cometchatEmojiKeyboardCategoryTextAppearance,
                    0
                ) ?: 0,
                categoryIconTint = typedArray?.getColor(
                    R.styleable.CometChatEmojiKeyBoardView_cometchatEmojiKeyboardCategoryIconTint,
                    CometChatTheme.getIconTintSecondary(context)
                ) ?: CometChatTheme.getIconTintSecondary(context),
                selectedCategoryIconTint = typedArray?.getColor(
                    R.styleable.CometChatEmojiKeyBoardView_cometchatEmojiKeyboardSelectedCategoryIconTint,
                    CometChatTheme.getIconTintHighlight(context)
                ) ?: CometChatTheme.getIconTintHighlight(context),
                selectedCategoryBackgroundColor = typedArray?.getColor(
                    R.styleable.CometChatEmojiKeyBoardView_cometchatEmojiKeyboardSelectedCategoryBackgroundColor,
                    CometChatTheme.getExtendedPrimaryColor100(context)
                ) ?: CometChatTheme.getExtendedPrimaryColor100(context)
            )
        }
    }
}
