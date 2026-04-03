package com.cometchat.uikit.kotlin.presentation.stickerkeyboard.style

import android.content.Context
import android.content.res.TypedArray
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.StyleRes
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.theme.CometChatTheme

/**
 * Style configuration for CometChatStickerKeyboard component.
 *
 * This data class holds all styling properties for the sticker keyboard view,
 * including container styling, tab styling, empty state styling, and error state styling.
 *
 * @param backgroundColor Background color for the sticker keyboard container
 * @param separatorColor Color for the separator line between content and tabs
 * @param tabIconSize Size of the tab icons (width and height) in pixels
 * @param tabActiveIndicatorColor Color of the active tab indicator
 * @param stickerItemSize Size of individual sticker items in the grid in pixels
 * @param emptyStateTitleTextColor Text color for empty state title
 * @param emptyStateTitleTextAppearance Text appearance resource for empty state title
 * @param emptyStateSubtitleTextColor Text color for empty state subtitle
 * @param emptyStateSubtitleTextAppearance Text appearance resource for empty state subtitle
 * @param errorStateTextColor Text color for error state message
 * @param errorStateTextAppearance Text appearance resource for error state message
 */
data class CometChatStickerKeyboardStyle(
    // Container
    @ColorInt val backgroundColor: Int = 0,
    @ColorInt val separatorColor: Int = 0,

    // Tab styling
    @Dimension val tabIconSize: Int = 0,
    @ColorInt val tabActiveIndicatorColor: Int = 0,

    // Sticker grid
    @Dimension val stickerItemSize: Int = 0,

    // Empty state styling
    @ColorInt val emptyStateTitleTextColor: Int = 0,
    @StyleRes val emptyStateTitleTextAppearance: Int = 0,
    @ColorInt val emptyStateSubtitleTextColor: Int = 0,
    @StyleRes val emptyStateSubtitleTextAppearance: Int = 0,

    // Error state styling
    @ColorInt val errorStateTextColor: Int = 0,
    @StyleRes val errorStateTextAppearance: Int = 0
) {
    companion object {
        /**
         * Creates a default style by extracting values from the theme's cometchatStickerKeyboardStyle.
         *
         * @param context The context to access theme resources
         * @return A CometChatStickerKeyboardStyle with values from theme or fallback defaults
         */
        fun default(context: Context): CometChatStickerKeyboardStyle {
            return extractFromThemeStyle(context, R.attr.cometchatStickerKeyboardStyle)
        }

        private fun extractFromThemeStyle(context: Context, themeStyleAttr: Int): CometChatStickerKeyboardStyle {
            val themeTypedArray = context.obtainStyledAttributes(intArrayOf(themeStyleAttr))
            val styleResId = themeTypedArray.getResourceId(0, 0)
            themeTypedArray.recycle()
            return extractFromStyleResource(context, styleResId)
        }

        private fun extractFromStyleResource(context: Context, styleResId: Int): CometChatStickerKeyboardStyle {
            if (styleResId == 0) {
                return extractFromTypedArray(context, null)
            }
            val typedArray = context.obtainStyledAttributes(styleResId, R.styleable.CometChatStickerKeyboard)
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
         * @return A CometChatStickerKeyboardStyle with values from XML or theme defaults
         */
        fun fromTypedArray(context: Context, typedArray: TypedArray): CometChatStickerKeyboardStyle {
            return try {
                extractFromTypedArray(context, typedArray)
            } finally {
                typedArray.recycle()
            }
        }

        private fun extractFromTypedArray(
            context: Context,
            typedArray: TypedArray?
        ): CometChatStickerKeyboardStyle {
            val density = context.resources.displayMetrics.density
            val defaultTabIconSize = (36 * density).toInt()
            val defaultStickerItemSize = (80 * density).toInt()

            return CometChatStickerKeyboardStyle(
                // Container styling
                backgroundColor = typedArray?.getColor(
                    R.styleable.CometChatStickerKeyboard_cometchatStickerKeyboardBackgroundColor,
                    CometChatTheme.getBackgroundColor1(context)
                ) ?: CometChatTheme.getBackgroundColor1(context),
                separatorColor = typedArray?.getColor(
                    R.styleable.CometChatStickerKeyboard_cometchatStickerKeyboardSeparatorColor,
                    CometChatTheme.getStrokeColorDefault(context)
                ) ?: CometChatTheme.getStrokeColorDefault(context),

                // Tab styling
                tabIconSize = defaultTabIconSize,
                tabActiveIndicatorColor = CometChatTheme.getPrimaryColor(context),

                // Sticker grid
                stickerItemSize = defaultStickerItemSize,

                // Empty state styling
                emptyStateTitleTextColor = typedArray?.getColor(
                    R.styleable.CometChatStickerKeyboard_cometchatStickerKeyboardEmptyStateTitleTextColor,
                    CometChatTheme.getTextColorPrimary(context)
                ) ?: CometChatTheme.getTextColorPrimary(context),
                emptyStateTitleTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatStickerKeyboard_cometchatStickerKeyboardEmptyStateTitleTextAppearance,
                    CometChatTheme.getTextAppearanceHeading3Bold(context)
                ) ?: CometChatTheme.getTextAppearanceHeading3Bold(context),
                emptyStateSubtitleTextColor = typedArray?.getColor(
                    R.styleable.CometChatStickerKeyboard_cometchatStickerKeyboardEmptyStateSubtitleTextColor,
                    CometChatTheme.getTextColorSecondary(context)
                ) ?: CometChatTheme.getTextColorSecondary(context),
                emptyStateSubtitleTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatStickerKeyboard_cometchatStickerKeyboardEmptyStateSubtitleTextAppearance,
                    CometChatTheme.getTextAppearanceBodyRegular(context)
                ) ?: CometChatTheme.getTextAppearanceBodyRegular(context),

                // Error state styling
                errorStateTextColor = typedArray?.getColor(
                    R.styleable.CometChatStickerKeyboard_cometchatStickerKeyboardErrorStateTextColor,
                    CometChatTheme.getTextColorSecondary(context)
                ) ?: CometChatTheme.getTextColorSecondary(context),
                errorStateTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatStickerKeyboard_cometchatStickerKeyboardErrorStateTextAppearance,
                    CometChatTheme.getTextAppearanceBodyRegular(context)
                ) ?: CometChatTheme.getTextAppearanceBodyRegular(context)
            )
        }
    }
}
