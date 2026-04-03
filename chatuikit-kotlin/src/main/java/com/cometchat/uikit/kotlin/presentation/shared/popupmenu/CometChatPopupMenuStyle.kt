package com.cometchat.uikit.kotlin.presentation.shared.popupmenu

import android.content.Context
import android.content.res.TypedArray
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.StyleRes
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.theme.CometChatTheme

/**
 * Style configuration for CometChatPopupMenu component.
 *
 * This data class holds all styling properties for the popup menu,
 * matching the XML attributes defined in R.styleable.CometChatPopupMenu.
 *
 * @param elevation Shadow elevation for the popup menu container (in pixels)
 * @param cornerRadius Corner radius for the popup menu container (in pixels)
 * @param backgroundColor Background color for the popup menu container
 * @param itemTextColor Default text color for menu items
 * @param itemTextAppearance Default text appearance resource for menu items
 * @param strokeColor Border/stroke color for the popup menu container
 * @param strokeWidth Border/stroke width for the popup menu container (in pixels)
 * @param itemStartIconTint Default tint color for start icons in menu items
 * @param itemEndIconTint Default tint color for end icons in menu items
 * @param itemPaddingHorizontal Horizontal padding for each menu item (in pixels)
 * @param itemPaddingVertical Vertical padding for each menu item (in pixels)
 * @param minWidth Minimum width for the popup menu (in pixels)
 */
data class CometChatPopupMenuStyle(
    @Dimension val elevation: Int = 0,
    @Dimension val cornerRadius: Int = 0,
    @ColorInt val backgroundColor: Int = 0,
    @ColorInt val itemTextColor: Int = 0,
    @StyleRes val itemTextAppearance: Int = 0,
    @ColorInt val strokeColor: Int = 0,
    @Dimension val strokeWidth: Int = 0,
    @ColorInt val itemStartIconTint: Int = 0,
    @ColorInt val itemEndIconTint: Int = 0,
    @Dimension val itemPaddingHorizontal: Int = 0,
    @Dimension val itemPaddingVertical: Int = 0,
    @Dimension val minWidth: Int = 0
) {
    companion object {
        /**
         * Creates a default style with theme-appropriate values.
         *
         * @param context The context to access theme resources
         * @return A CometChatPopupMenuStyle with default values
         */
        fun default(context: Context): CometChatPopupMenuStyle {
            return extractFromTypedArray(context, null)
        }

        /**
         * Creates a [CometChatPopupMenuStyle] from a [TypedArray].
         *
         * This method extracts all properties from the TypedArray and handles
         * recycling the TypedArray after extraction.
         *
         * @param context The context to access theme resources
         * @param typedArray The TypedArray containing style attributes
         * @return A CometChatPopupMenuStyle with values from the TypedArray
         */
        fun fromTypedArray(context: Context, typedArray: TypedArray): CometChatPopupMenuStyle {
            return try {
                extractFromTypedArray(context, typedArray)
            } finally {
                typedArray.recycle()
            }
        }

        /**
         * Extracts style properties from a TypedArray without recycling it.
         *
         * @param context The context to access theme resources
         * @param typedArray The TypedArray containing style attributes, or null for defaults
         * @return A CometChatPopupMenuStyle with extracted values
         */
        fun extractFromTypedArray(
            context: Context,
            typedArray: TypedArray?
        ): CometChatPopupMenuStyle {
            val density = context.resources.displayMetrics.density
            // Default values matching Jetpack: 16dp horizontal, 8dp vertical, 128dp minWidth
            val defaultHorizontalPadding = (16 * density + 0.5f).toInt()
            val defaultVerticalPadding = (8 * density + 0.5f).toInt()
            val defaultMinWidth = (128 * density + 0.5f).toInt()

            return CometChatPopupMenuStyle(
                elevation = typedArray?.getDimensionPixelSize(
                    R.styleable.CometChatPopupMenu_cometchatPopupMenuElevation, 0
                ) ?: 0,
                cornerRadius = typedArray?.getDimensionPixelSize(
                    R.styleable.CometChatPopupMenu_cometchatPopupMenuCornerRadius, 0
                ) ?: 0,
                backgroundColor = typedArray?.getColor(
                    R.styleable.CometChatPopupMenu_cometchatPopupMenuBackgroundColor,
                    CometChatTheme.getBackgroundColor1(context)
                ) ?: CometChatTheme.getBackgroundColor1(context),
                itemTextColor = typedArray?.getColor(
                    R.styleable.CometChatPopupMenu_cometchatPopupMenuItemTextColor,
                    CometChatTheme.getTextColorPrimary(context)
                ) ?: CometChatTheme.getTextColorPrimary(context),
                itemTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatPopupMenu_cometchatPopupMenuItemTextAppearance, 0
                ) ?: 0,
                strokeColor = typedArray?.getColor(
                    R.styleable.CometChatPopupMenu_cometchatPopupMenuStrokeColor,
                    CometChatTheme.getStrokeColorLight(context)
                ) ?: CometChatTheme.getStrokeColorLight(context),
                strokeWidth = typedArray?.getDimensionPixelSize(
                    R.styleable.CometChatPopupMenu_cometchatPopupMenuStrokeWidth, 0
                ) ?: 0,
                itemStartIconTint = typedArray?.getColor(
                    R.styleable.CometChatPopupMenu_cometchatPopupMenuItemStartIconTint,
                    CometChatTheme.getIconTintPrimary(context)
                ) ?: CometChatTheme.getIconTintPrimary(context),
                itemEndIconTint = typedArray?.getColor(
                    R.styleable.CometChatPopupMenu_cometchatPopupMenuItemEndIconTint,
                    CometChatTheme.getIconTintPrimary(context)
                ) ?: CometChatTheme.getIconTintPrimary(context),
                itemPaddingHorizontal = typedArray?.getDimensionPixelSize(
                    R.styleable.CometChatPopupMenu_cometchatPopupMenuItemPaddingHorizontal,
                    defaultHorizontalPadding
                ) ?: defaultHorizontalPadding,
                itemPaddingVertical = typedArray?.getDimensionPixelSize(
                    R.styleable.CometChatPopupMenu_cometchatPopupMenuItemPaddingVertical,
                    defaultVerticalPadding
                ) ?: defaultVerticalPadding,
                minWidth = typedArray?.getDimensionPixelSize(
                    R.styleable.CometChatPopupMenu_cometchatPopupMenuMinWidth,
                    defaultMinWidth
                ) ?: defaultMinWidth
            )
        }
    }
}
