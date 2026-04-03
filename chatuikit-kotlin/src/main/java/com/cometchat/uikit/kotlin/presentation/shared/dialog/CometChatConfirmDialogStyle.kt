package com.cometchat.uikit.kotlin.presentation.shared.dialog

import android.content.Context
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.StyleRes
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.theme.CometChatTheme

/**
 * Style configuration for CometChatConfirmDialog component.
 *
 * This data class holds all styling properties for the confirmation dialog,
 * matching the XML attributes defined in attr_cometchat_confirm_dialog.xml.
 */
data class CometChatConfirmDialogStyle(
    // Container styling
    @ColorInt val strokeColor: Int = 0,
    @Dimension val strokeWidth: Int = 0,
    @Dimension val cornerRadius: Int = 0,
    @Dimension val elevation: Int = 0,
    @ColorInt val backgroundColor: Int = 0,

    // Icon styling
    val icon: Drawable? = null,
    @ColorInt val iconTint: Int = 0,
    @ColorInt val iconBackgroundColor: Int = 0,

    // Title styling
    @StyleRes val titleTextAppearance: Int = 0,
    @ColorInt val titleTextColor: Int = 0,

    // Subtitle styling
    @StyleRes val subtitleTextAppearance: Int = 0,
    @ColorInt val subtitleTextColor: Int = 0,

    // Positive button styling
    @StyleRes val positiveButtonTextAppearance: Int = 0,
    @ColorInt val positiveButtonTextColor: Int = 0,
    @ColorInt val positiveButtonBackgroundColor: Int = 0,
    @Dimension val positiveButtonStrokeWidth: Int = 0,
    @ColorInt val positiveButtonStrokeColor: Int = 0,
    @Dimension val positiveButtonRadius: Int = 0,

    // Negative button styling
    @StyleRes val negativeButtonTextAppearance: Int = 0,
    @ColorInt val negativeButtonTextColor: Int = 0,
    @ColorInt val negativeButtonBackgroundColor: Int = 0,
    @Dimension val negativeButtonStrokeWidth: Int = 0,
    @ColorInt val negativeButtonStrokeColor: Int = 0,
    @Dimension val negativeButtonRadius: Int = 0
) {
    companion object {
        /**
         * Creates a default style with theme-appropriate values.
         *
         * @param context The context to access theme resources
         * @return A CometChatConfirmDialogStyle with default values
         */
        fun default(context: Context): CometChatConfirmDialogStyle {
            return extractFromTypedArray(context, null)
        }

        /**
         * Creates a [CometChatConfirmDialogStyle] from a [TypedArray].
         *
         * This method extracts all properties from the TypedArray and handles
         * recycling the TypedArray after extraction.
         *
         * @param context The context to access theme resources
         * @param typedArray The TypedArray containing style attributes
         * @return A CometChatConfirmDialogStyle with values from the TypedArray
         */
        fun fromTypedArray(context: Context, typedArray: TypedArray): CometChatConfirmDialogStyle {
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
         * @return A CometChatConfirmDialogStyle with extracted values
         */
        fun extractFromTypedArray(
            context: Context,
            typedArray: TypedArray?
        ): CometChatConfirmDialogStyle {
            return CometChatConfirmDialogStyle(
                // Container styling
                strokeColor = typedArray?.getColor(
                    R.styleable.CometChatConfirmDialog_cometchatConfirmDialogStrokeColor,
                    CometChatTheme.getStrokeColorLight(context)
                ) ?: CometChatTheme.getStrokeColorLight(context),
                strokeWidth = typedArray?.getDimensionPixelSize(
                    R.styleable.CometChatConfirmDialog_cometchatConfirmDialogStrokeWidth, 0
                ) ?: 0,
                cornerRadius = typedArray?.getDimensionPixelSize(
                    R.styleable.CometChatConfirmDialog_cometchatConfirmDialogCornerRadius, 0
                ) ?: 0,
                elevation = typedArray?.getDimensionPixelSize(
                    R.styleable.CometChatConfirmDialog_cometchatConfirmDialogElevation, 0
                ) ?: 0,
                backgroundColor = typedArray?.getColor(
                    R.styleable.CometChatConfirmDialog_cometchatConfirmDialogBackgroundColor,
                    CometChatTheme.getBackgroundColor1(context)
                ) ?: CometChatTheme.getBackgroundColor1(context),

                // Icon styling
                icon = typedArray?.getDrawable(
                    R.styleable.CometChatConfirmDialog_cometchatConfirmDialogIcon
                ),
                iconTint = typedArray?.getColor(
                    R.styleable.CometChatConfirmDialog_cometchatConfirmDialogIconTint, 0
                ) ?: 0,
                iconBackgroundColor = typedArray?.getColor(
                    R.styleable.CometChatConfirmDialog_cometchatConfirmDialogIconBackgroundColor,
                    CometChatTheme.getBackgroundColor2(context)
                ) ?: CometChatTheme.getBackgroundColor2(context),

                // Title styling
                titleTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatConfirmDialog_cometchatConfirmDialogTitleTextAppearance, 0
                ) ?: 0,
                titleTextColor = typedArray?.getColor(
                    R.styleable.CometChatConfirmDialog_cometchatConfirmDialogTitleTextColor,
                    CometChatTheme.getTextColorPrimary(context)
                ) ?: CometChatTheme.getTextColorPrimary(context),

                // Subtitle styling
                subtitleTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatConfirmDialog_cometchatConfirmDialogSubtitleTextAppearance, 0
                ) ?: 0,
                subtitleTextColor = typedArray?.getColor(
                    R.styleable.CometChatConfirmDialog_cometchatConfirmDialogSubtitleTextColor,
                    CometChatTheme.getTextColorSecondary(context)
                ) ?: CometChatTheme.getTextColorSecondary(context),

                // Positive button styling
                positiveButtonTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatConfirmDialog_cometchatConfirmDialogPositiveButtonTextAppearance, 0
                ) ?: 0,
                positiveButtonTextColor = typedArray?.getColor(
                    R.styleable.CometChatConfirmDialog_cometchatConfirmDialogPositiveButtonTextColor,
                    CometChatTheme.getColorWhite(context)
                ) ?: CometChatTheme.getColorWhite(context),
                positiveButtonBackgroundColor = typedArray?.getColor(
                    R.styleable.CometChatConfirmDialog_cometchatConfirmDialogPositiveButtonBackgroundColor,
                    CometChatTheme.getErrorColor(context)
                ) ?: CometChatTheme.getErrorColor(context),
                positiveButtonStrokeWidth = typedArray?.getDimensionPixelSize(
                    R.styleable.CometChatConfirmDialog_cometchatConfirmDialogPositiveButtonStrokeWidth, 0
                ) ?: 0,
                positiveButtonStrokeColor = typedArray?.getColor(
                    R.styleable.CometChatConfirmDialog_cometchatConfirmDialogPositiveButtonStrokeColor, 0
                ) ?: 0,
                positiveButtonRadius = typedArray?.getDimensionPixelSize(
                    R.styleable.CometChatConfirmDialog_cometchatConfirmDialogPositiveButtonRadius, 0
                ) ?: 0,

                // Negative button styling
                negativeButtonTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatConfirmDialog_cometchatConfirmDialogNegativeButtonTextAppearance, 0
                ) ?: 0,
                negativeButtonTextColor = typedArray?.getColor(
                    R.styleable.CometChatConfirmDialog_cometchatConfirmDialogNegativeButtonTextColor,
                    CometChatTheme.getTextColorPrimary(context)
                ) ?: CometChatTheme.getTextColorPrimary(context),
                negativeButtonBackgroundColor = typedArray?.getColor(
                    R.styleable.CometChatConfirmDialog_cometchatConfirmDialogNegativeButtonBackgroundColor,
                    CometChatTheme.getTextColorWhite(context)
                ) ?: CometChatTheme.getTextColorWhite(context),
                negativeButtonStrokeWidth = typedArray?.getDimensionPixelSize(
                    R.styleable.CometChatConfirmDialog_cometchatConfirmDialogNegativeButtonStrokeWidth, 0
                ) ?: 0,
                negativeButtonStrokeColor = typedArray?.getColor(
                    R.styleable.CometChatConfirmDialog_cometchatConfirmDialogNegativeButtonStrokeColor,
                    CometChatTheme.getStrokeColorDark(context)
                ) ?: CometChatTheme.getStrokeColorDark(context),
                negativeButtonRadius = typedArray?.getDimensionPixelSize(
                    R.styleable.CometChatConfirmDialog_cometchatConfirmDialogNegativeButtonRadius, 0
                ) ?: 0
            )
        }
    }
}
