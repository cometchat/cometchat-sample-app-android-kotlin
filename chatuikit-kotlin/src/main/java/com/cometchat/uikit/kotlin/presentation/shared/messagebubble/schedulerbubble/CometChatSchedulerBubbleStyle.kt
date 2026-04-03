package com.cometchat.uikit.kotlin.presentation.shared.messagebubble.schedulerbubble

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
 * Style configuration for CometChatSchedulerBubble component.
 *
 * This data class holds all styling properties for the scheduler bubble,
 * matching the XML attributes defined in attr_cometchat_scheduler_bubble.xml.
 */
data class CometChatSchedulerBubbleStyle(
    // Container styling
    @ColorInt val backgroundColor: Int = STYLE_NOT_SET,
    val backgroundDrawable: Drawable? = null,
    @Dimension val cornerRadius: Float = DIMENSION_NOT_SET,
    @Dimension val strokeWidth: Float = DIMENSION_NOT_SET,
    @ColorInt val strokeColor: Int = STYLE_NOT_SET,

    // Title styling
    @StyleRes val titleTextAppearance: Int = STYLE_NOT_SET,
    @ColorInt val titleTextColor: Int = STYLE_NOT_SET,

    // Button styling
    @StyleRes val buttonTextAppearance: Int = STYLE_NOT_SET,
    @ColorInt val buttonTextColor: Int = STYLE_NOT_SET,
    @ColorInt val buttonBackgroundColor: Int = STYLE_NOT_SET,
    val buttonBackgroundDrawable: Drawable? = null,

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
    fun resolve(base: CometChatMessageBubbleStyle): CometChatSchedulerBubbleStyle {
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
        fun default(context: Context): CometChatSchedulerBubbleStyle {
            return CometChatSchedulerBubbleStyle(
                backgroundColor = CometChatTheme.getBackgroundColor3(context),
                titleTextAppearance = CometChatTheme.getTextAppearanceHeading4Bold(context),
                titleTextColor = CometChatTheme.getTextColorPrimary(context),
                buttonTextAppearance = CometChatTheme.getTextAppearanceButtonMedium(context),
                buttonTextColor = CometChatTheme.getPrimaryButtonTextColor(context),
                buttonBackgroundColor = CometChatTheme.getPrimaryColor(context)
            )
        }

        /**
         * Creates a style by extracting values from XML TypedArray.
         */
        fun fromTypedArray(context: Context, typedArray: TypedArray): CometChatSchedulerBubbleStyle {
            return try {
                extractFromTypedArray(context, typedArray)
            } finally {
                typedArray.recycle()
            }
        }

        private fun extractFromTypedArray(context: Context, typedArray: TypedArray?): CometChatSchedulerBubbleStyle {
            return CometChatSchedulerBubbleStyle(
                backgroundColor = typedArray?.getColor(
                    R.styleable.CometChatSchedulerBubble_cometchatSchedulerBubbleBackgroundColor, STYLE_NOT_SET
                ) ?: STYLE_NOT_SET,
                backgroundDrawable = typedArray?.getDrawable(
                    R.styleable.CometChatSchedulerBubble_cometchatSchedulerBubbleBackgroundDrawable
                ),
                cornerRadius = typedArray?.getDimension(
                    R.styleable.CometChatSchedulerBubble_cometchatSchedulerBubbleCornerRadius, DIMENSION_NOT_SET
                ) ?: DIMENSION_NOT_SET,
                strokeWidth = typedArray?.getInt(
                    R.styleable.CometChatSchedulerBubble_cometchatSchedulerBubbleStrokeWidth, STYLE_NOT_SET
                )?.toFloat() ?: DIMENSION_NOT_SET,
                strokeColor = typedArray?.getColor(
                    R.styleable.CometChatSchedulerBubble_cometchatSchedulerBubbleStrokeColor, STYLE_NOT_SET
                ) ?: STYLE_NOT_SET,
                titleTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatSchedulerBubble_cometchatSchedulerBubbleTitleTextAppearance, STYLE_NOT_SET
                ) ?: STYLE_NOT_SET,
                titleTextColor = typedArray?.getColor(
                    R.styleable.CometChatSchedulerBubble_cometchatSchedulerBubbleTitleTextColor, STYLE_NOT_SET
                ) ?: STYLE_NOT_SET,
                buttonTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatSchedulerBubble_cometchatSchedulerBubbleButtonTextAppearance, STYLE_NOT_SET
                ) ?: STYLE_NOT_SET,
                buttonTextColor = typedArray?.getColor(
                    R.styleable.CometChatSchedulerBubble_cometchatSchedulerBubbleButtonTextColor, STYLE_NOT_SET
                ) ?: STYLE_NOT_SET,
                buttonBackgroundColor = typedArray?.getColor(
                    R.styleable.CometChatSchedulerBubble_cometchatSchedulerBubbleButtonBackgroundColor, STYLE_NOT_SET
                ) ?: STYLE_NOT_SET,
                buttonBackgroundDrawable = typedArray?.getDrawable(
                    R.styleable.CometChatSchedulerBubble_cometchatSchedulerBubbleButtonBackgroundDrawable
                ),
                threadIndicatorTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatSchedulerBubble_cometchatSchedulerBubbleThreadIndicatorTextAppearance, STYLE_NOT_SET
                ) ?: STYLE_NOT_SET,
                threadIndicatorTextColor = typedArray?.getColor(
                    R.styleable.CometChatSchedulerBubble_cometchatSchedulerBubbleThreadIndicatorTextColor, STYLE_NOT_SET
                ) ?: STYLE_NOT_SET,
                threadIndicatorIconTint = typedArray?.getColor(
                    R.styleable.CometChatSchedulerBubble_cometchatSchedulerBubbleThreadIndicatorIconTint, STYLE_NOT_SET
                ) ?: STYLE_NOT_SET,
                senderNameTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatSchedulerBubble_cometchatSchedulerBubbleSenderNameTextAppearance, STYLE_NOT_SET
                ) ?: STYLE_NOT_SET,
                senderNameTextColor = typedArray?.getColor(
                    R.styleable.CometChatSchedulerBubble_cometchatSchedulerBubbleSenderNameTextColor, STYLE_NOT_SET
                ) ?: STYLE_NOT_SET,
                dateStyleRes = typedArray?.getResourceId(
                    R.styleable.CometChatSchedulerBubble_cometchatSchedulerBubbleDateStyle, 0
                ) ?: 0,
                messageReceiptStyleRes = typedArray?.getResourceId(
                    R.styleable.CometChatSchedulerBubble_cometchatSchedulerBubbleMessageReceiptStyle, 0
                ) ?: 0,
                avatarStyleRes = typedArray?.getResourceId(
                    R.styleable.CometChatSchedulerBubble_cometchatSchedulerBubbleAvatarStyle, 0
                ) ?: 0
            )
        }
    }
}
