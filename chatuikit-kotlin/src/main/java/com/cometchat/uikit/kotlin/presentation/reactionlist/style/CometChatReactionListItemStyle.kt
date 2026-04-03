package com.cometchat.uikit.kotlin.presentation.reactionlist.style

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.StyleRes
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.theme.CometChatTheme

/**
 * Style configuration for CometChatReactionListItem component.
 *
 * This data class holds all styling properties for the reaction list item view,
 * including background color, text colors, text appearances, and separator styling.
 *
 * @param backgroundColor Background color for the list item
 * @param titleTextColor Text color for the user name (or "You")
 * @param titleTextAppearance Text appearance resource for the user name
 * @param subtitleTextColor Text color for the subtitle ("Tap to remove")
 * @param subtitleTextAppearance Text appearance resource for the subtitle
 * @param tailViewTextColor Text color for the emoji in tail view
 * @param tailViewTextAppearance Text appearance resource for the emoji in tail view
 * @param avatarStyleResId Style resource ID for the avatar component
 * @param separatorColor Color for the item separator line
 * @param separatorHeight Height of the item separator line in pixels
 */
data class CometChatReactionListItemStyle(
    @ColorInt val backgroundColor: Int = 0,
    @ColorInt val titleTextColor: Int = 0,
    @StyleRes val titleTextAppearance: Int = 0,
    @ColorInt val subtitleTextColor: Int = 0,
    @StyleRes val subtitleTextAppearance: Int = 0,
    @ColorInt val tailViewTextColor: Int = 0,
    @StyleRes val tailViewTextAppearance: Int = 0,
    @StyleRes val avatarStyleResId: Int = 0,
    @ColorInt val separatorColor: Int = 0,
    @Dimension val separatorHeight: Int = 1
) {
    companion object {
        /**
         * Creates a default style by extracting values from the theme's cometchatReactionListStyle.
         *
         * @param context The context to access theme resources
         * @return A CometChatReactionListItemStyle with values from theme or fallback defaults
         */
        fun default(context: Context): CometChatReactionListItemStyle {
            return extractFromThemeStyle(context)
        }

        private fun extractFromThemeStyle(context: Context): CometChatReactionListItemStyle {
            val themeTypedArray = context.obtainStyledAttributes(intArrayOf(R.attr.cometchatReactionListStyle))
            val styleResId = themeTypedArray.getResourceId(0, 0)
            themeTypedArray.recycle()
            return extractFromStyleResource(context, styleResId)
        }

        private fun extractFromStyleResource(context: Context, styleResId: Int): CometChatReactionListItemStyle {
            if (styleResId == 0) {
                return extractFromTypedArrayInternal(context, null)
            }
            val typedArray = context.obtainStyledAttributes(styleResId, R.styleable.CometChatReactionList)
            return try {
                extractFromTypedArrayInternal(context, typedArray)
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
         * @return A CometChatReactionListItemStyle with values from XML or theme defaults
         */
        fun fromTypedArray(context: Context, typedArray: TypedArray): CometChatReactionListItemStyle {
            return try {
                extractFromTypedArrayInternal(context, typedArray)
            } finally {
                typedArray.recycle()
            }
        }

        /**
         * Internal extraction method that does NOT recycle the TypedArray.
         * Used by CometChatReactionListStyle to extract item style from the same TypedArray.
         *
         * @param context The Android context for accessing theme resources
         * @param typedArray The TypedArray to extract from, or null for defaults only
         * @return A CometChatReactionListItemStyle with extracted values or theme defaults
         */
        internal fun extractFromTypedArrayInternal(
            context: Context,
            typedArray: TypedArray?
        ): CometChatReactionListItemStyle {
            return CometChatReactionListItemStyle(
                // Background color
                backgroundColor = Color.TRANSPARENT,

                // Title styling (user name or "You")
                titleTextColor = typedArray?.getColor(
                    R.styleable.CometChatReactionList_cometchatReactionListTitleTextColor,
                    CometChatTheme.getTextColorPrimary(context)
                ) ?: CometChatTheme.getTextColorPrimary(context),
                titleTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatReactionList_cometchatReactionListTitleTextAppearance,
                    CometChatTheme.getTextAppearanceBodyRegular(context)
                ) ?: CometChatTheme.getTextAppearanceBodyRegular(context),

                // Subtitle styling ("Tap to remove")
                subtitleTextColor = typedArray?.getColor(
                    R.styleable.CometChatReactionList_cometchatReactionListSubTitleTextColor,
                    CometChatTheme.getTextColorSecondary(context)
                ) ?: CometChatTheme.getTextColorSecondary(context),
                subtitleTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatReactionList_cometchatReactionListSubTitleTextAppearance,
                    CometChatTheme.getTextAppearanceCaption1Regular(context)
                ) ?: CometChatTheme.getTextAppearanceCaption1Regular(context),

                // Tail view styling (emoji)
                tailViewTextColor = typedArray?.getColor(
                    R.styleable.CometChatReactionList_cometchatReactionListTitleTextColor,
                    CometChatTheme.getTextColorPrimary(context)
                ) ?: CometChatTheme.getTextColorPrimary(context),
                tailViewTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatReactionList_cometchatReactionListTailViewTextAppearance,
                    CometChatTheme.getTextAppearanceHeading2Regular(context)
                ) ?: CometChatTheme.getTextAppearanceHeading2Regular(context),

                // Avatar style
                avatarStyleResId = typedArray?.getResourceId(
                    R.styleable.CometChatReactionList_cometchatReactionListAvatarStyle,
                    0
                ) ?: 0,

                // Separator styling
                separatorColor = typedArray?.getColor(
                    R.styleable.CometChatReactionList_cometchatReactionListSeparatorColor,
                    CometChatTheme.getStrokeColorLight(context)
                ) ?: CometChatTheme.getStrokeColorLight(context),
                separatorHeight = 1
            )
        }
    }
}
