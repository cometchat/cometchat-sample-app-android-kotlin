package com.cometchat.uikit.kotlin.presentation.shared.reaction

import android.content.Context
import android.content.res.TypedArray
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.StyleRes
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.theme.CometChatTheme

/**
 * Style configuration for CometChatReaction component.
 *
 * This data class holds all styling properties for the reaction view,
 * matching the XML attributes defined in attr_cometchat_reaction.xml.
 */
data class CometChatReactionStyle(
    @StyleRes val emojiTextAppearance: Int = 0,
    @ColorInt val emojiTextColor: Int = 0,
    @StyleRes val countTextAppearance: Int = 0,
    @ColorInt val countTextColor: Int = 0,
    @ColorInt val backgroundColor: Int = 0,
    @Dimension val strokeWidth: Float = 0f,
    @ColorInt val strokeColor: Int = 0,
    @Dimension val cornerRadius: Float = 0f,
    @Dimension val elevation: Float = 0f,
    @ColorInt val activeBackgroundColor: Int = 0,
    @Dimension val activeStrokeWidth: Float = 0f,
    @ColorInt val activeStrokeColor: Int = 0
) {
    companion object {
        /**
         * Creates a default style with CometChatTheme defaults.
         */
        fun default(context: Context): CometChatReactionStyle {
            return CometChatReactionStyle(
                emojiTextAppearance = CometChatTheme.getTextAppearanceBodyRegular(context),
                emojiTextColor = CometChatTheme.getTextColorPrimary(context),
                countTextAppearance = CometChatTheme.getTextAppearanceCaption1Regular(context),
                countTextColor = CometChatTheme.getTextColorSecondary(context),
                backgroundColor = CometChatTheme.getBackgroundColor1(context),
                strokeWidth = 0f,
                strokeColor = CometChatTheme.getBorderColorDefault(context),
                cornerRadius = 0f,
                elevation = 0f,
                activeBackgroundColor = CometChatTheme.getBackgroundColor3(context),
                activeStrokeWidth = 0f,
                activeStrokeColor = CometChatTheme.getPrimaryColor(context)
            )
        }

        /**
         * Creates a style by extracting values from XML TypedArray.
         */
        fun fromTypedArray(context: Context, typedArray: TypedArray): CometChatReactionStyle {
            return try {
                extractFromTypedArray(context, typedArray)
            } finally {
                typedArray.recycle()
            }
        }

        private fun extractFromTypedArray(context: Context, typedArray: TypedArray?): CometChatReactionStyle {
            return CometChatReactionStyle(
                emojiTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatReaction_cometchatReactionEmojiTextAppearance,
                    CometChatTheme.getTextAppearanceBodyRegular(context)
                ) ?: CometChatTheme.getTextAppearanceBodyRegular(context),
                emojiTextColor = typedArray?.getColor(
                    R.styleable.CometChatReaction_cometchatReactionEmojiTextColor,
                    CometChatTheme.getTextColorPrimary(context)
                ) ?: CometChatTheme.getTextColorPrimary(context),
                countTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatReaction_cometchatReactionCountTextAppearance,
                    CometChatTheme.getTextAppearanceCaption1Regular(context)
                ) ?: CometChatTheme.getTextAppearanceCaption1Regular(context),
                countTextColor = typedArray?.getColor(
                    R.styleable.CometChatReaction_cometchatReactionCountTextColor,
                    CometChatTheme.getTextColorSecondary(context)
                ) ?: CometChatTheme.getTextColorSecondary(context),
                backgroundColor = typedArray?.getColor(
                    R.styleable.CometChatReaction_cometchatReactionBackgroundColor,
                    CometChatTheme.getBackgroundColor1(context)
                ) ?: CometChatTheme.getBackgroundColor1(context),
                strokeWidth = typedArray?.getDimension(
                    R.styleable.CometChatReaction_cometchatReactionStrokeWidth, 0f
                ) ?: 0f,
                strokeColor = typedArray?.getColor(
                    R.styleable.CometChatReaction_cometchatReactionStrokeColor,
                    CometChatTheme.getBorderColorDefault(context)
                ) ?: CometChatTheme.getBorderColorDefault(context),
                cornerRadius = typedArray?.getDimension(
                    R.styleable.CometChatReaction_cometchatReactionCornerRadius, 0f
                ) ?: 0f,
                elevation = typedArray?.getDimension(
                    R.styleable.CometChatReaction_cometchatReactionElevation, 0f
                ) ?: 0f,
                activeBackgroundColor = typedArray?.getColor(
                    R.styleable.CometChatReaction_cometchatActiveReactionBackgroundColor,
                    CometChatTheme.getBackgroundColor3(context)
                ) ?: CometChatTheme.getBackgroundColor3(context),
                activeStrokeWidth = typedArray?.getDimension(
                    R.styleable.CometChatReaction_cometchatActiveReactionStrokeWidth, 0f
                ) ?: 0f,
                activeStrokeColor = typedArray?.getColor(
                    R.styleable.CometChatReaction_cometchatActiveReactionStrokeColor,
                    CometChatTheme.getPrimaryColor(context)
                ) ?: CometChatTheme.getPrimaryColor(context)
            )
        }
    }
}
