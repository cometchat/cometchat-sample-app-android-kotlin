package com.cometchat.uikit.kotlin.presentation.shared.messagebubble.formbubble

import android.content.Context
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.StyleRes
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.CometChatMessageBubbleStyle
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.STYLE_NOT_SET
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.DIMENSION_NOT_SET
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.resolveStyleColor
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.resolveStyleRes
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.resolveStyleDimension
import com.cometchat.uikit.kotlin.theme.CometChatTheme

/**
 * Style configuration for CometChatFormBubble component.
 *
 * This data class holds all styling properties for the form bubble,
 * matching the XML attributes defined in attr_cometchat_form_bubble.xml.
 */
data class CometChatFormBubbleStyle(
    // Container styling
    @ColorInt val backgroundColor: Int = STYLE_NOT_SET,
    val backgroundDrawable: Drawable? = null,
    @Dimension val cornerRadius: Float = DIMENSION_NOT_SET,
    @Dimension val strokeWidth: Float = DIMENSION_NOT_SET,
    @ColorInt val strokeColor: Int = STYLE_NOT_SET,

    // Title styling
    @StyleRes val titleTextAppearance: Int = STYLE_NOT_SET,
    @ColorInt val titleColor: Int = STYLE_NOT_SET,

    // Subtitle styling
    @StyleRes val subTitleTextAppearance: Int = STYLE_NOT_SET,
    @ColorInt val subTitleColor: Int = STYLE_NOT_SET,

    // Icon styling
    @ColorInt val iconTint: Int = STYLE_NOT_SET,

    // Input styling
    @StyleRes val inputTextAppearance: Int = STYLE_NOT_SET,
    @ColorInt val inputTextColor: Int = STYLE_NOT_SET,
    @StyleRes val placeholderTextAppearance: Int = STYLE_NOT_SET,
    @ColorInt val placeholderTextColor: Int = STYLE_NOT_SET,
    @Dimension val inputStrokeWidth: Float = DIMENSION_NOT_SET,
    @ColorInt val inputStrokeColor: Int = STYLE_NOT_SET,
    @Dimension val inputCornerRadius: Float = DIMENSION_NOT_SET,

    // Button styling
    @StyleRes val buttonTextAppearance: Int = STYLE_NOT_SET,
    @ColorInt val buttonTextColor: Int = STYLE_NOT_SET,
    @ColorInt val buttonBackgroundColor: Int = STYLE_NOT_SET,

    // Common bubble properties
    @StyleRes val threadIndicatorTextAppearance: Int = STYLE_NOT_SET,
    @ColorInt val threadIndicatorTextColor: Int = STYLE_NOT_SET,
    @ColorInt val threadIndicatorIconTint: Int = STYLE_NOT_SET,
    @StyleRes val senderNameTextAppearance: Int = STYLE_NOT_SET,
    @ColorInt val senderNameTextColor: Int = STYLE_NOT_SET,

    // Sub-component style resource IDs
    @StyleRes val dateStyleRes: Int = 0,
    @StyleRes val messageReceiptStyleRes: Int = 0,
    @StyleRes val avatarStyleRes: Int = 0
) {
    /**
     * Resolves sentinel values against a base [CometChatMessageBubbleStyle].
     */
    fun resolve(base: CometChatMessageBubbleStyle): CometChatFormBubbleStyle {
        return copy(
            backgroundColor = resolveStyleColor(backgroundColor, base.backgroundColor),
            cornerRadius = resolveStyleDimension(cornerRadius, base.cornerRadius),
            strokeWidth = resolveStyleDimension(strokeWidth, base.strokeWidth),
            strokeColor = resolveStyleColor(strokeColor, base.strokeColor),
            threadIndicatorTextAppearance = resolveStyleRes(threadIndicatorTextAppearance, base.threadIndicatorTextAppearance),
            threadIndicatorTextColor = resolveStyleColor(threadIndicatorTextColor, base.threadIndicatorTextColor),
            threadIndicatorIconTint = resolveStyleColor(threadIndicatorIconTint, base.threadIndicatorIconTint),
            senderNameTextAppearance = resolveStyleRes(senderNameTextAppearance, base.senderNameTextAppearance),
            senderNameTextColor = resolveStyleColor(senderNameTextColor, base.senderNameTextColor)
        )
    }

    companion object {
        /**
         * Creates a default style with CometChatTheme defaults.
         */
        fun default(context: Context): CometChatFormBubbleStyle {
            return CometChatFormBubbleStyle(
                backgroundColor = CometChatTheme.getBackgroundColor3(context),
                titleTextAppearance = CometChatTheme.getTextAppearanceHeading4Bold(context),
                titleColor = CometChatTheme.getTextColorPrimary(context),
                subTitleTextAppearance = CometChatTheme.getTextAppearanceCaption1Regular(context),
                subTitleColor = CometChatTheme.getTextColorSecondary(context),
                iconTint = CometChatTheme.getIconTintSecondary(context),
                inputTextAppearance = CometChatTheme.getTextAppearanceBodyRegular(context),
                inputTextColor = CometChatTheme.getTextColorPrimary(context),
                placeholderTextAppearance = CometChatTheme.getTextAppearanceBodyRegular(context),
                placeholderTextColor = CometChatTheme.getTextColorTertiary(context),
                inputStrokeColor = CometChatTheme.getBorderColorDefault(context),
                buttonTextAppearance = CometChatTheme.getTextAppearanceButtonMedium(context),
                buttonTextColor = CometChatTheme.getPrimaryButtonTextColor(context),
                buttonBackgroundColor = CometChatTheme.getPrimaryColor(context)
            )
        }

        /**
         * Creates a style by extracting values from XML TypedArray.
         */
        fun fromTypedArray(context: Context, typedArray: TypedArray): CometChatFormBubbleStyle {
            return try {
                extractFromTypedArray(context, typedArray)
            } finally {
                typedArray.recycle()
            }
        }

        private fun extractFromTypedArray(context: Context, typedArray: TypedArray?): CometChatFormBubbleStyle {
            return CometChatFormBubbleStyle(
                backgroundColor = typedArray?.getColor(
                    R.styleable.CometChatFormBubble_cometchatFormBubbleBackgroundColor, STYLE_NOT_SET
                ) ?: STYLE_NOT_SET,
                backgroundDrawable = typedArray?.getDrawable(
                    R.styleable.CometChatFormBubble_cometchatFormBubbleBackgroundDrawable
                ),
                cornerRadius = typedArray?.getDimension(
                    R.styleable.CometChatFormBubble_cometchatFormBubbleCornerRadius, DIMENSION_NOT_SET
                ) ?: DIMENSION_NOT_SET,
                strokeWidth = typedArray?.getDimension(
                    R.styleable.CometChatFormBubble_cometchatFormBubbleStrokeWidth, DIMENSION_NOT_SET
                ) ?: DIMENSION_NOT_SET,
                strokeColor = typedArray?.getColor(
                    R.styleable.CometChatFormBubble_cometchatFormBubbleStrokeColor, STYLE_NOT_SET
                ) ?: STYLE_NOT_SET,
                titleTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatFormBubble_cometchatFormBubbleTitleTextAppearance, STYLE_NOT_SET
                ) ?: STYLE_NOT_SET,
                titleColor = typedArray?.getColor(
                    R.styleable.CometChatFormBubble_cometchatFormBubbleTitleColor, STYLE_NOT_SET
                ) ?: STYLE_NOT_SET,
                subTitleTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatFormBubble_cometchatFormBubbleSubTitleTextAppearance, STYLE_NOT_SET
                ) ?: STYLE_NOT_SET,
                subTitleColor = typedArray?.getColor(
                    R.styleable.CometChatFormBubble_cometchatFormBubbleSubTitleColor, STYLE_NOT_SET
                ) ?: STYLE_NOT_SET,
                iconTint = typedArray?.getColor(
                    R.styleable.CometChatFormBubble_cometchatFormBubbleIconTint, STYLE_NOT_SET
                ) ?: STYLE_NOT_SET,
                inputTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatFormBubble_cometchatFormBubbleInputTextAppearance, STYLE_NOT_SET
                ) ?: STYLE_NOT_SET,
                inputTextColor = typedArray?.getColor(
                    R.styleable.CometChatFormBubble_cometchatFormBubbleInputTextColor, STYLE_NOT_SET
                ) ?: STYLE_NOT_SET,
                placeholderTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatFormBubble_cometchatFormBubblePlaceholderTextAppearance, STYLE_NOT_SET
                ) ?: STYLE_NOT_SET,
                placeholderTextColor = typedArray?.getColor(
                    R.styleable.CometChatFormBubble_cometchatFormBubblePlaceholderTextColor, STYLE_NOT_SET
                ) ?: STYLE_NOT_SET,
                inputStrokeWidth = typedArray?.getDimension(
                    R.styleable.CometChatFormBubble_cometchatFormBubbleInputStrokeWidth, DIMENSION_NOT_SET
                ) ?: DIMENSION_NOT_SET,
                inputStrokeColor = typedArray?.getColor(
                    R.styleable.CometChatFormBubble_cometchatFormBubbleInputStrokeColor, STYLE_NOT_SET
                ) ?: STYLE_NOT_SET,
                inputCornerRadius = typedArray?.getDimension(
                    R.styleable.CometChatFormBubble_cometchatFormBubbleInputCornerRadius, DIMENSION_NOT_SET
                ) ?: DIMENSION_NOT_SET,
                buttonTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatFormBubble_cometchatFormBubbleButtonTextAppearance, STYLE_NOT_SET
                ) ?: STYLE_NOT_SET,
                buttonTextColor = typedArray?.getColor(
                    R.styleable.CometChatFormBubble_cometchatFormBubbleButtonTextColor, STYLE_NOT_SET
                ) ?: STYLE_NOT_SET,
                buttonBackgroundColor = typedArray?.getColor(
                    R.styleable.CometChatFormBubble_cometchatFormBubbleButtonBackgroundColor, STYLE_NOT_SET
                ) ?: STYLE_NOT_SET,
                threadIndicatorTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatFormBubble_cometchatFormBubbleThreadIndicatorTextAppearance, STYLE_NOT_SET
                ) ?: STYLE_NOT_SET,
                threadIndicatorTextColor = typedArray?.getColor(
                    R.styleable.CometChatFormBubble_cometchatFormBubbleThreadIndicatorTextColor, STYLE_NOT_SET
                ) ?: STYLE_NOT_SET,
                threadIndicatorIconTint = typedArray?.getColor(
                    R.styleable.CometChatFormBubble_cometchatFormBubbleThreadIndicatorIconTint, STYLE_NOT_SET
                ) ?: STYLE_NOT_SET,
                senderNameTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatFormBubble_cometchatFormBubbleSenderNameTextAppearance, STYLE_NOT_SET
                ) ?: STYLE_NOT_SET,
                senderNameTextColor = typedArray?.getColor(
                    R.styleable.CometChatFormBubble_cometchatFormBubbleSenderNameTextColor, STYLE_NOT_SET
                ) ?: STYLE_NOT_SET,
                dateStyleRes = typedArray?.getResourceId(
                    R.styleable.CometChatFormBubble_cometchatFormBubbleDateStyle, 0
                ) ?: 0,
                messageReceiptStyleRes = typedArray?.getResourceId(
                    R.styleable.CometChatFormBubble_cometchatFormBubbleMessageReceiptStyle, 0
                ) ?: 0,
                avatarStyleRes = typedArray?.getResourceId(
                    R.styleable.CometChatFormBubble_cometchatFormBubbleAvatarStyle, 0
                ) ?: 0
            )
        }
    }
}
