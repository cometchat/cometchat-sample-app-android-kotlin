package com.cometchat.uikit.kotlin.presentation.polls.style

import android.content.Context
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.StyleRes
import androidx.core.content.ContextCompat
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.theme.CometChatTheme

/**
 * Style configuration for CometChatCreatePoll component.
 *
 * This data class holds all styling properties for the create poll view,
 * including container styling, question styling, option styling, and submit button styling.
 */
data class CometChatCreatePollStyle(
    // Container
    @ColorInt val backgroundColor: Int = 0,
    val backgroundDrawable: Drawable? = null,
    @Dimension val cornerRadius: Int = 0,
    @Dimension val strokeWidth: Int = 0,
    @ColorInt val strokeColor: Int = 0,
    
    // Title
    @ColorInt val titleTextColor: Int = 0,
    @StyleRes val titleTextAppearance: Int = 0,
    
    // Question section
    @ColorInt val questionTitleTextColor: Int = 0,
    @StyleRes val questionTitleTextAppearance: Int = 0,
    @ColorInt val questionTextColor: Int = 0,
    @StyleRes val questionTextAppearance: Int = 0,
    @ColorInt val questionHintColor: Int = 0,
    @Dimension val questionCornerRadius: Int = 0,
    @Dimension val questionStrokeWidth: Int = 0,
    @ColorInt val questionStrokeColor: Int = 0,
    
    // Options section
    @ColorInt val optionTitleTextColor: Int = 0,
    @StyleRes val optionTitleTextAppearance: Int = 0,
    @ColorInt val optionTextColor: Int = 0,
    @StyleRes val optionTextAppearance: Int = 0,
    @ColorInt val optionHintColor: Int = 0,
    @ColorInt val optionBackgroundColor: Int = 0,
    @Dimension val optionCornerRadius: Int = 0,
    @Dimension val optionStrokeWidth: Int = 0,
    @ColorInt val optionStrokeColor: Int = 0,

    // Icons
    @ColorInt val dragIconTint: Int = 0,
    val dragIcon: Drawable? = null,
    @ColorInt val backIconTint: Int = 0,
    val backIcon: Drawable? = null,
    
    // Separator
    @ColorInt val separatorColor: Int = 0,
    
    // Submit button
    @ColorInt val submitButtonBackgroundColor: Int = 0,
    @ColorInt val disabledSubmitButtonBackgroundColor: Int = 0,
    @Dimension val submitButtonCornerRadius: Int = 0,
    @Dimension val submitButtonStrokeWidth: Int = 0,
    @ColorInt val submitButtonStrokeColor: Int = 0,
    @ColorInt val submitButtonTextColor: Int = 0,
    @StyleRes val submitButtonTextAppearance: Int = 0,
    
    // Progress indicator
    @ColorInt val progressIndicatorColor: Int = 0,
    
    // Error
    @ColorInt val errorTextColor: Int = 0,
    @StyleRes val errorTextAppearance: Int = 0
) {
    companion object {
        /**
         * Creates a default style by extracting values from the theme's cometchatCreatePollStyle.
         *
         * @param context The context to access theme resources
         * @return A CometChatCreatePollStyle with values from theme or fallback defaults
         */
        fun default(context: Context): CometChatCreatePollStyle {
            return extractFromThemeStyle(context, R.attr.cometchatCreatePollStyle)
        }

        private fun extractFromThemeStyle(context: Context, themeStyleAttr: Int): CometChatCreatePollStyle {
            val themeTypedArray = context.obtainStyledAttributes(intArrayOf(themeStyleAttr))
            val styleResId = themeTypedArray.getResourceId(0, 0)
            themeTypedArray.recycle()
            return extractFromStyleResource(context, styleResId)
        }

        private fun extractFromStyleResource(context: Context, styleResId: Int): CometChatCreatePollStyle {
            if (styleResId == 0) {
                return extractFromTypedArray(context, null)
            }
            val typedArray = context.obtainStyledAttributes(styleResId, R.styleable.CometChatCreatePoll)
            return try {
                extractFromTypedArray(context, typedArray)
            } finally {
                typedArray.recycle()
            }
        }

        /**
         * Creates a style by extracting values from XML TypedArray.
         *
         * @param context The Android context for accessing theme resources
         * @param typedArray The TypedArray containing XML attribute values (will be recycled)
         * @return A CometChatCreatePollStyle with values from XML or theme defaults
         */
        fun fromTypedArray(context: Context, typedArray: TypedArray): CometChatCreatePollStyle {
            return try {
                extractFromTypedArray(context, typedArray)
            } finally {
                typedArray.recycle()
            }
        }


        private fun extractFromTypedArray(
            context: Context,
            typedArray: TypedArray?
        ): CometChatCreatePollStyle {
            val density = context.resources.displayMetrics.density
            val defaultCornerRadius = (8 * density).toInt()
            val defaultStrokeWidth = (1 * density).toInt()

            return CometChatCreatePollStyle(
                // Container styling
                backgroundColor = typedArray?.getColor(
                    R.styleable.CometChatCreatePoll_cometchatCreatePollBackgroundColor,
                    CometChatTheme.getBackgroundColor1(context)
                ) ?: CometChatTheme.getBackgroundColor1(context),
                backgroundDrawable = typedArray?.getDrawable(
                    R.styleable.CometChatCreatePoll_cometchatCreatePollBackgroundDrawable
                ),
                cornerRadius = typedArray?.getDimensionPixelSize(
                    R.styleable.CometChatCreatePoll_cometchatCreatePollCornerRadius,
                    0
                ) ?: 0,
                strokeWidth = typedArray?.getDimensionPixelSize(
                    R.styleable.CometChatCreatePoll_cometchatCreatePollStrokeWidth,
                    0
                ) ?: 0,
                strokeColor = typedArray?.getColor(
                    R.styleable.CometChatCreatePoll_cometchatCreatePollStrokeColor,
                    CometChatTheme.getStrokeColorLight(context)
                ) ?: CometChatTheme.getStrokeColorLight(context),

                // Title styling
                titleTextColor = typedArray?.getColor(
                    R.styleable.CometChatCreatePoll_cometchatCreatePollTitleTextColor,
                    CometChatTheme.getTextColorPrimary(context)
                ) ?: CometChatTheme.getTextColorPrimary(context),
                titleTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatCreatePoll_cometchatCreatePollTitleTextAppearance,
                    CometChatTheme.getTextAppearanceHeading3Bold(context)
                ) ?: CometChatTheme.getTextAppearanceHeading3Bold(context),

                // Question title styling
                questionTitleTextColor = typedArray?.getColor(
                    R.styleable.CometChatCreatePoll_cometchatCreatePollQuestionTitleTextColor,
                    CometChatTheme.getTextColorPrimary(context)
                ) ?: CometChatTheme.getTextColorPrimary(context),
                questionTitleTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatCreatePoll_cometchatCreatePollQuestionTitleTextAppearance,
                    CometChatTheme.getTextAppearanceBodyMedium(context)
                ) ?: CometChatTheme.getTextAppearanceBodyMedium(context),

                // Question input styling
                questionTextColor = typedArray?.getColor(
                    R.styleable.CometChatCreatePoll_cometchatCreatePollQuestionTextColor,
                    CometChatTheme.getTextColorPrimary(context)
                ) ?: CometChatTheme.getTextColorPrimary(context),
                questionTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatCreatePoll_cometchatCreatePollQuestionTextAppearance,
                    CometChatTheme.getTextAppearanceBodyRegular(context)
                ) ?: CometChatTheme.getTextAppearanceBodyRegular(context),
                questionHintColor = typedArray?.getColor(
                    R.styleable.CometChatCreatePoll_cometchatCreatePollQuestionHintColor,
                    CometChatTheme.getTextColorTertiary(context)
                ) ?: CometChatTheme.getTextColorTertiary(context),
                questionCornerRadius = typedArray?.getDimensionPixelSize(
                    R.styleable.CometChatCreatePoll_cometchatCreatePollQuestionCornerRadius,
                    defaultCornerRadius
                ) ?: defaultCornerRadius,
                questionStrokeWidth = typedArray?.getDimensionPixelSize(
                    R.styleable.CometChatCreatePoll_cometchatCreatePollQuestionStrokeWidth,
                    defaultStrokeWidth
                ) ?: defaultStrokeWidth,
                questionStrokeColor = typedArray?.getColor(
                    R.styleable.CometChatCreatePoll_cometchatCreatePollQuestionStrokeColor,
                    CometChatTheme.getStrokeColorLight(context)
                ) ?: CometChatTheme.getStrokeColorLight(context),


                // Option title styling
                optionTitleTextColor = typedArray?.getColor(
                    R.styleable.CometChatCreatePoll_cometchatCreatePollOptionTitleTextColor,
                    CometChatTheme.getTextColorPrimary(context)
                ) ?: CometChatTheme.getTextColorPrimary(context),
                optionTitleTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatCreatePoll_cometchatCreatePollOptionTitleTextAppearance,
                    CometChatTheme.getTextAppearanceBodyMedium(context)
                ) ?: CometChatTheme.getTextAppearanceBodyMedium(context),

                // Option input styling
                optionTextColor = typedArray?.getColor(
                    R.styleable.CometChatCreatePoll_cometchatCreatePollOptionTextColor,
                    CometChatTheme.getTextColorPrimary(context)
                ) ?: CometChatTheme.getTextColorPrimary(context),
                optionTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatCreatePoll_cometchatCreatePollOptionTextAppearance,
                    CometChatTheme.getTextAppearanceBodyRegular(context)
                ) ?: CometChatTheme.getTextAppearanceBodyRegular(context),
                optionHintColor = typedArray?.getColor(
                    R.styleable.CometChatCreatePoll_cometchatCreatePollOptionHintColor,
                    CometChatTheme.getTextColorTertiary(context)
                ) ?: CometChatTheme.getTextColorTertiary(context),
                // Option background uses main backgroundColor (same as reference)
                optionBackgroundColor = typedArray?.getColor(
                    R.styleable.CometChatCreatePoll_cometchatCreatePollBackgroundColor,
                    CometChatTheme.getBackgroundColor1(context)
                ) ?: CometChatTheme.getBackgroundColor1(context),
                optionCornerRadius = typedArray?.getDimensionPixelSize(
                    R.styleable.CometChatCreatePoll_cometchatCreatePollOptionCornerRadius,
                    defaultCornerRadius
                ) ?: defaultCornerRadius,
                optionStrokeWidth = typedArray?.getDimensionPixelSize(
                    R.styleable.CometChatCreatePoll_cometchatCreatePollOptionStrokeWidth,
                    defaultStrokeWidth
                ) ?: defaultStrokeWidth,
                optionStrokeColor = typedArray?.getColor(
                    R.styleable.CometChatCreatePoll_cometchatCreatePollOptionStrokeColor,
                    CometChatTheme.getStrokeColorLight(context)
                ) ?: CometChatTheme.getStrokeColorLight(context),

                // Icons
                dragIconTint = typedArray?.getColor(
                    R.styleable.CometChatCreatePoll_cometchatCreatePollDragIconTint,
                    CometChatTheme.getIconTintSecondary(context)
                ) ?: CometChatTheme.getIconTintSecondary(context),
                dragIcon = typedArray?.getDrawable(
                    R.styleable.CometChatCreatePoll_cometchatCreatePollDragIcon
                ) ?: ContextCompat.getDrawable(context, R.drawable.cometchat_ic_menu),
                backIconTint = typedArray?.getColor(
                    R.styleable.CometChatCreatePoll_cometchatCreatePollBackIconTint,
                    CometChatTheme.getIconTintPrimary(context)
                ) ?: CometChatTheme.getIconTintPrimary(context),
                backIcon = typedArray?.getDrawable(
                    R.styleable.CometChatCreatePoll_cometchatCreatePollBackIcon
                ) ?: ContextCompat.getDrawable(context, R.drawable.cometchat_ic_close),

                // Separator
                separatorColor = typedArray?.getColor(
                    R.styleable.CometChatCreatePoll_cometchatCreatePollSeparatorColor,
                    CometChatTheme.getStrokeColorDefault(context)
                ) ?: CometChatTheme.getStrokeColorDefault(context),


                // Submit button styling
                submitButtonBackgroundColor = typedArray?.getColor(
                    R.styleable.CometChatCreatePoll_cometchatCreatePollSubmitButtonBackgroundColor,
                    CometChatTheme.getPrimaryColor(context)
                ) ?: CometChatTheme.getPrimaryColor(context),
                disabledSubmitButtonBackgroundColor = typedArray?.getColor(
                    R.styleable.CometChatCreatePoll_cometchatCreatePollDisabledSubmitButtonBackgroundColor,
                    CometChatTheme.getBackgroundColor4(context)
                ) ?: CometChatTheme.getBackgroundColor4(context),
                submitButtonCornerRadius = typedArray?.getDimensionPixelSize(
                    R.styleable.CometChatCreatePoll_cometchatCreatePollSubmitButtonCornerRadius,
                    defaultCornerRadius
                ) ?: defaultCornerRadius,
                submitButtonStrokeWidth = typedArray?.getDimensionPixelSize(
                    R.styleable.CometChatCreatePoll_cometchatCreatePollSubmitButtonStrokeWidth,
                    0
                ) ?: 0,
                submitButtonStrokeColor = typedArray?.getColor(
                    R.styleable.CometChatCreatePoll_cometchatCreatePollSubmitButtonStrokeColor,
                    0
                ) ?: 0,
                submitButtonTextColor = typedArray?.getColor(
                    R.styleable.CometChatCreatePoll_cometchatCreatePollSubmitButtonTextColor,
                    CometChatTheme.getColorWhite(context)
                ) ?: CometChatTheme.getColorWhite(context),
                submitButtonTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatCreatePoll_cometchatCreatePollSubmitButtonTextAppearance,
                    CometChatTheme.getTextAppearanceButtonMedium(context)
                ) ?: CometChatTheme.getTextAppearanceButtonMedium(context),

                // Progress indicator
                progressIndicatorColor = typedArray?.getColor(
                    R.styleable.CometChatCreatePoll_cometchatCreatePollProgressIndeterminateTint,
                    CometChatTheme.getColorWhite(context)
                ) ?: CometChatTheme.getColorWhite(context),

                // Error styling
                errorTextColor = typedArray?.getColor(
                    R.styleable.CometChatCreatePoll_cometchatCreatePollErrorTextColor,
                    CometChatTheme.getErrorColor(context)
                ) ?: CometChatTheme.getErrorColor(context),
                errorTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatCreatePoll_cometchatCreatePollErrorTextAppearance,
                    CometChatTheme.getTextAppearanceCaption1Regular(context)
                ) ?: CometChatTheme.getTextAppearanceCaption1Regular(context)
            )
        }
    }
}
