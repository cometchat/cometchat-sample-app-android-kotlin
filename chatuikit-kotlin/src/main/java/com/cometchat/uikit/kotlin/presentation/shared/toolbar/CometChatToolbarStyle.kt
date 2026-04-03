package com.cometchat.uikit.kotlin.presentation.shared.toolbar

import android.content.Context
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.DrawableRes
import androidx.annotation.StyleRes
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.theme.CometChatTheme

/**
 * Style configuration for CometChatToolbar.
 */
data class CometChatToolbarStyle(
    @ColorInt val backgroundColor: Int = 0,
    @ColorInt val titleTextColor: Int = 0,
    @StyleRes val titleTextAppearance: Int = 0,
    @ColorInt val navigationIconTint: Int = 0,
    @DrawableRes val navigationIcon: Int = 0,
    @Dimension val height: Int = 0,
    @Dimension val elevation: Float = 0f,
    val showSeparator: Boolean = true,
    @ColorInt val separatorColor: Int = 0,
    @Dimension val separatorHeight: Int = 0,
    @ColorInt val discardIconTint: Int = 0,
    @DrawableRes val discardIcon: Int = 0,
    @ColorInt val submitIconTint: Int = 0,
    @DrawableRes val submitIcon: Int = 0,
    @ColorInt val selectionCountTextColor: Int = 0,
    @StyleRes val selectionCountTextAppearance: Int = 0
) {
    companion object {
        /**
         * Creates a default style using CometChatTheme values.
         */
        fun default(context: Context): CometChatToolbarStyle {
            return CometChatToolbarStyle(
                backgroundColor = CometChatTheme.getBackgroundColor1(context),
                titleTextColor = CometChatTheme.getTextColorPrimary(context),
                navigationIconTint = CometChatTheme.getIconTintPrimary(context),
                separatorColor = CometChatTheme.getStrokeColorLight(context),
                discardIconTint = CometChatTheme.getIconTintPrimary(context),
                submitIconTint = CometChatTheme.getPrimaryColor(context),
                selectionCountTextColor = CometChatTheme.getTextColorPrimary(context)
            )
        }

        /**
         * Creates a style by extracting values from XML TypedArray.
         * 
         * This method handles TypedArray recycling internally using try-finally.
         * 
         * @param context The Android context for accessing theme resources
         * @param typedArray The TypedArray containing XML attribute values (will be recycled)
         * @return A CometChatToolbarStyle with values from XML or theme defaults
         */
        fun fromTypedArray(context: Context, typedArray: TypedArray): CometChatToolbarStyle {
            return try {
                CometChatToolbarStyle(
                    // Background
                    backgroundColor = typedArray.getColor(
                        R.styleable.CometChatToolbar_cometchatToolbarBackgroundColor,
                        CometChatTheme.getBackgroundColor1(context)
                    ),
                    // Title properties
                    titleTextColor = typedArray.getColor(
                        R.styleable.CometChatToolbar_cometchatToolbarTitleTextColor,
                        CometChatTheme.getTextColorPrimary(context)
                    ),
                    titleTextAppearance = typedArray.getResourceId(
                        R.styleable.CometChatToolbar_cometchatToolbarTitleTextAppearance,
                        0
                    ),
                    // Navigation icon
                    navigationIconTint = typedArray.getColor(
                        R.styleable.CometChatToolbar_cometchatToolbarBackIconTint,
                        CometChatTheme.getIconTintPrimary(context)
                    ),
                    navigationIcon = typedArray.getResourceId(
                        R.styleable.CometChatToolbar_cometchatToolbarBackIcon,
                        R.drawable.cometchat_ic_back
                    ),
                    // Separator
                    showSeparator = typedArray.getBoolean(
                        R.styleable.CometChatToolbar_cometchatToolbarShowSeparator,
                        true
                    ),
                    separatorColor = typedArray.getColor(
                        R.styleable.CometChatToolbar_cometchatToolbarSeparatorColor,
                        CometChatTheme.getStrokeColorLight(context)
                    ),
                    separatorHeight = typedArray.getDimensionPixelSize(
                        R.styleable.CometChatToolbar_cometchatToolbarSeparatorHeight,
                        0
                    ),
                    // Discard icon
                    discardIconTint = typedArray.getColor(
                        R.styleable.CometChatToolbar_cometchatToolbarDiscardIconTint,
                        CometChatTheme.getIconTintPrimary(context)
                    ),
                    discardIcon = typedArray.getResourceId(
                        R.styleable.CometChatToolbar_cometchatToolbarDiscardIcon,
                        R.drawable.cometchat_ic_close
                    ),
                    // Submit icon
                    submitIconTint = typedArray.getColor(
                        R.styleable.CometChatToolbar_cometchatToolbarSubmitIconTint,
                        CometChatTheme.getPrimaryColor(context)
                    ),
                    submitIcon = typedArray.getResourceId(
                        R.styleable.CometChatToolbar_cometchatToolbarSubmitIcon,
                        R.drawable.cometchat_ic_check
                    ),
                    // Selection count
                    selectionCountTextColor = typedArray.getColor(
                        R.styleable.CometChatToolbar_cometchatToolbarSelectionCountTextColor,
                        CometChatTheme.getTextColorPrimary(context)
                    ),
                    selectionCountTextAppearance = typedArray.getResourceId(
                        R.styleable.CometChatToolbar_cometchatToolbarSelectionCountTextAppearance,
                        0
                    )
                )
            } finally {
                typedArray.recycle()
            }
        }
    }
}
