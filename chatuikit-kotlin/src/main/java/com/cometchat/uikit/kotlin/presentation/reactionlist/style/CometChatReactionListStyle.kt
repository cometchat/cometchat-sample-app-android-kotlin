package com.cometchat.uikit.kotlin.presentation.reactionlist.style

import android.content.Context
import android.content.res.TypedArray
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.StyleRes
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.theme.CometChatTheme

/**
 * Style configuration for CometChatReactionList component.
 *
 * This data class holds all styling properties for the reaction list view,
 * including container styling, tab styling, error styling, and nested item style.
 *
 * @param backgroundColor Background color for the reaction list container
 * @param strokeColor Stroke/border color for the container
 * @param strokeWidth Width of the container stroke/border in pixels
 * @param cornerRadius Corner radius of the container in pixels
 * @param tabTextColor Text color for inactive tabs
 * @param tabTextActiveColor Text color for the active tab
 * @param tabTextAppearance Text appearance resource for tab labels
 * @param tabActiveIndicatorColor Color of the active tab indicator (underline)
 * @param errorTextColor Text color for error messages
 * @param errorTextAppearance Text appearance resource for error messages
 * @param separatorColor Color for the separator between header and list
 * @param separatorHeight Height of the separator in pixels
 * @param itemStyle Style configuration for individual reaction list items
 */
data class CometChatReactionListStyle(
    // Container
    @ColorInt val backgroundColor: Int = 0,
    @ColorInt val strokeColor: Int = 0,
    @Dimension val strokeWidth: Int = 0,
    @Dimension val cornerRadius: Int = 0,

    // Tab styling
    @ColorInt val tabTextColor: Int = 0,
    @ColorInt val tabTextActiveColor: Int = 0,
    @StyleRes val tabTextAppearance: Int = 0,
    @ColorInt val tabActiveIndicatorColor: Int = 0,

    // Error styling
    @ColorInt val errorTextColor: Int = 0,
    @StyleRes val errorTextAppearance: Int = 0,

    // Separator
    @ColorInt val separatorColor: Int = 0,
    @Dimension val separatorHeight: Int = 2,

    // Nested item style
    val itemStyle: CometChatReactionListItemStyle = CometChatReactionListItemStyle()
) {
    companion object {
        /**
         * Creates a default style by extracting values from the theme's cometchatReactionListStyle.
         *
         * @param context The context to access theme resources
         * @return A CometChatReactionListStyle with values from theme or fallback defaults
         */
        fun default(context: Context): CometChatReactionListStyle {
            return extractFromThemeStyle(context, R.attr.cometchatReactionListStyle)
        }

        private fun extractFromThemeStyle(context: Context, themeStyleAttr: Int): CometChatReactionListStyle {
            val themeTypedArray = context.obtainStyledAttributes(intArrayOf(themeStyleAttr))
            val styleResId = themeTypedArray.getResourceId(0, 0)
            themeTypedArray.recycle()
            return extractFromStyleResource(context, styleResId)
        }

        private fun extractFromStyleResource(context: Context, styleResId: Int): CometChatReactionListStyle {
            if (styleResId == 0) {
                return extractFromTypedArray(context, null)
            }
            val typedArray = context.obtainStyledAttributes(styleResId, R.styleable.CometChatReactionList)
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
         * @return A CometChatReactionListStyle with values from XML or theme defaults
         */
        fun fromTypedArray(context: Context, typedArray: TypedArray): CometChatReactionListStyle {
            return try {
                extractFromTypedArray(context, typedArray)
            } finally {
                typedArray.recycle()
            }
        }

        private fun extractFromTypedArray(
            context: Context,
            typedArray: TypedArray?
        ): CometChatReactionListStyle {
            // Extract nested item style from the same TypedArray
            val itemStyle = CometChatReactionListItemStyle.extractFromTypedArrayInternal(context, typedArray)

            // Default corner radius is 16dp (cometchat_radius_4)
            val defaultCornerRadius = (16 * context.resources.displayMetrics.density).toInt()

            return CometChatReactionListStyle(
                // Container styling
                backgroundColor = typedArray?.getColor(
                    R.styleable.CometChatReactionList_cometchatReactionListBackgroundColor,
                    CometChatTheme.getBackgroundColor1(context)
                ) ?: CometChatTheme.getBackgroundColor1(context),
                strokeColor = typedArray?.getColor(
                    R.styleable.CometChatReactionList_cometchatReactionListStrokeColor,
                    CometChatTheme.getStrokeColorLight(context)
                ) ?: CometChatTheme.getStrokeColorLight(context),
                strokeWidth = typedArray?.getDimensionPixelSize(
                    R.styleable.CometChatReactionList_cometchatReactionListStrokeWidth,
                    0
                ) ?: 0,
                cornerRadius = typedArray?.getDimensionPixelSize(
                    R.styleable.CometChatReactionList_cometchatReactionListCornerRadius,
                    defaultCornerRadius
                ) ?: defaultCornerRadius,

                // Tab styling
                tabTextColor = typedArray?.getColor(
                    R.styleable.CometChatReactionList_cometchatReactionListTabTextColor,
                    CometChatTheme.getTextColorSecondary(context)
                ) ?: CometChatTheme.getTextColorSecondary(context),
                tabTextActiveColor = typedArray?.getColor(
                    R.styleable.CometChatReactionList_cometchatReactionListTabTextActiveColor,
                    CometChatTheme.getTextColorHighlight(context)
                ) ?: CometChatTheme.getTextColorHighlight(context),
                tabTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatReactionList_cometchatReactionListTabTextAppearance,
                    CometChatTheme.getTextAppearanceBodyMedium(context)
                ) ?: CometChatTheme.getTextAppearanceBodyMedium(context),
                tabActiveIndicatorColor = typedArray?.getColor(
                    R.styleable.CometChatReactionList_cometchatReactionListTabActiveIndicatorColor,
                    CometChatTheme.getPrimaryColor(context)
                ) ?: CometChatTheme.getPrimaryColor(context),

                // Error styling
                errorTextColor = typedArray?.getColor(
                    R.styleable.CometChatReactionList_cometchatReactionListErrorTextColor,
                    CometChatTheme.getErrorColor(context)
                ) ?: CometChatTheme.getErrorColor(context),
                errorTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatReactionList_cometchatReactionListErrorTextApAppearance,
                    CometChatTheme.getTextAppearanceBodyRegular(context)
                ) ?: CometChatTheme.getTextAppearanceBodyRegular(context),

                // Separator styling
                separatorColor = typedArray?.getColor(
                    R.styleable.CometChatReactionList_cometchatReactionListSeparatorColor,
                    CometChatTheme.getStrokeColorDefault(context)
                ) ?: CometChatTheme.getStrokeColorDefault(context),
                separatorHeight = 2,

                // Nested item style
                itemStyle = itemStyle
            )
        }
    }
}
