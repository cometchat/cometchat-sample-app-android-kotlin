package com.cometchat.uikit.kotlin.presentation.shared.messagebubble.pollbubble

import android.content.Context
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.StyleRes
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.presentation.shared.baseelements.date.CometChatDateStyle
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.CometChatMessageBubbleStyle
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.DIMENSION_NOT_SET
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.STYLE_NOT_SET
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.mergeWithBase
import com.cometchat.uikit.kotlin.presentation.shared.receipts.CometChatReceiptStyle
import com.cometchat.uikit.kotlin.theme.CometChatTheme

/**
 * Style configuration for CometChatPollBubble component.
 *
 * This data class holds all styling properties for the poll message bubble view.
 * Common bubble properties (background, corner radius, stroke, sender name,
 * thread indicator, timestamp) use sentinel values ([STYLE_NOT_SET] or
 * [DIMENSION_NOT_SET]) to indicate "not set". When not set, they fall back to
 * values from [CometChatMessageBubbleStyle] via [resolve].
 */
data class CometChatPollBubbleStyle(
    // Content-specific properties - Title
    @StyleRes val titleTextAppearance: Int = 0,
    @ColorInt val titleTextColor: Int = 0,

    // Content-specific properties - Options
    @StyleRes val optionTextAppearance: Int = 0,
    @ColorInt val optionTextColor: Int = 0,

    // Content-specific properties - Selected radio button
    @ColorInt val selectedRadioButtonStrokeColor: Int = 0,
    @Dimension val selectedRadioButtonStrokeWidth: Int = 0,
    @Dimension val selectedRadioButtonCornerRadius: Int = 0,
    @ColorInt val selectedIconTint: Int = 0,
    val selectedStateDrawable: Drawable? = null,

    // Content-specific properties - Unselected radio button
    @ColorInt val unselectedRadioButtonStrokeColor: Int = 0,
    @Dimension val unselectedRadioButtonStrokeWidth: Int = 0,
    @Dimension val unselectedRadioButtonCornerRadius: Int = 0,
    @ColorInt val unselectedIconTint: Int = 0,
    val unselectedStateDrawable: Drawable? = null,

    // Option avatar style
    @StyleRes val optionAvatarStyle: Int = 0,

    // Content-specific properties - Progress
    @ColorInt val progressColor: Int = 0,
    @ColorInt val progressBackgroundColor: Int = 0,
    @ColorInt val progressIndeterminateTint: Int = 0,

    // Content-specific properties - Vote count
    @StyleRes val voteCountTextAppearance: Int = 0,
    @ColorInt val voteCountTextColor: Int = 0,

    // Common bubble properties — use sentinel defaults
    @ColorInt val backgroundColor: Int = STYLE_NOT_SET,
    val backgroundDrawable: Drawable? = null,
    @Dimension val cornerRadius: Float = DIMENSION_NOT_SET,
    @Dimension val strokeWidth: Float = DIMENSION_NOT_SET,
    @ColorInt val strokeColor: Int = STYLE_NOT_SET,
    @StyleRes val senderNameTextAppearance: Int = STYLE_NOT_SET,
    @ColorInt val senderNameTextColor: Int = STYLE_NOT_SET,
    @StyleRes val threadIndicatorTextAppearance: Int = STYLE_NOT_SET,
    @ColorInt val threadIndicatorTextColor: Int = STYLE_NOT_SET,
    @ColorInt val threadIndicatorIconTint: Int = STYLE_NOT_SET,
    @StyleRes val timestampTextAppearance: Int = STYLE_NOT_SET,
    @ColorInt val timestampTextColor: Int = STYLE_NOT_SET,

    // Sub-component styles — only dateStyle and messageReceiptStyle have XML attributes for PollBubble
    val dateStyle: CometChatDateStyle? = null,
    val messageReceiptStyle: CometChatReceiptStyle? = null
) {
    /**
     * Resolves this style against a [CometChatMessageBubbleStyle], using the bubble style's
     * values as fallbacks for any properties set to sentinel values.
     *
     * @param messageBubbleStyle The message bubble style to use as fallback
     * @return A new [CometChatPollBubbleStyle] with all sentinel values resolved
     */
    fun resolve(messageBubbleStyle: CometChatMessageBubbleStyle): CometChatPollBubbleStyle =
        mergeWithBase(this, messageBubbleStyle)

    companion object {
        /**
         * Creates a default style (delegates to outgoing).
         *
         * Common bubble properties (backgroundColor, cornerRadius, etc.) are set
         * to sentinel values and should be resolved via [resolve] against a
         * [CometChatMessageBubbleStyle] at runtime.
         *
         * @param context The context to access theme resources
         * @return A CometChatPollBubbleStyle with default values
         */
        fun default(context: Context): CometChatPollBubbleStyle {
            return outgoing(context)
        }

        /**
         * Creates a style for outgoing messages by extracting content-specific
         * values from the theme's outgoing message bubble style.
         *
         * Common bubble properties (backgroundColor, cornerRadius, etc.) are set
         * to sentinel values and should be resolved via [resolve] against a
         * [CometChatMessageBubbleStyle] at runtime.
         *
         * @param context The context to access theme resources
         * @return A CometChatPollBubbleStyle configured for outgoing messages
         */
        fun outgoing(context: Context): CometChatPollBubbleStyle {
            return extractFromMessageBubbleStyle(
                context,
                R.attr.cometchatOutgoingMessageBubbleStyle
            )
        }

        /**
         * Creates a style for incoming messages by extracting content-specific
         * values from the theme's incoming message bubble style.
         *
         * Common bubble properties (backgroundColor, cornerRadius, etc.) are set
         * to sentinel values and should be resolved via [resolve] against a
         * [CometChatMessageBubbleStyle] at runtime.
         *
         * @param context The context to access theme resources
         * @return A CometChatPollBubbleStyle configured for incoming messages
         */
        fun incoming(context: Context): CometChatPollBubbleStyle {
            return extractFromMessageBubbleStyle(
                context,
                R.attr.cometchatIncomingMessageBubbleStyle
            )
        }


        private fun extractFromMessageBubbleStyle(
            context: Context,
            messageBubbleStyleAttr: Int
        ): CometChatPollBubbleStyle {
            val themeTypedArray = context.obtainStyledAttributes(intArrayOf(messageBubbleStyleAttr))
            val messageBubbleStyleResId = themeTypedArray.getResourceId(0, 0)
            themeTypedArray.recycle()

            if (messageBubbleStyleResId == 0) {
                return extractFromStyleResource(context, 0)
            }

            val messageBubbleTypedArray = context.obtainStyledAttributes(
                messageBubbleStyleResId,
                R.styleable.CometChatMessageBubble
            )
            val pollBubbleStyleResId = messageBubbleTypedArray.getResourceId(
                R.styleable.CometChatMessageBubble_cometchatPollBubbleStyle,
                0
            )
            messageBubbleTypedArray.recycle()

            return extractFromStyleResource(context, pollBubbleStyleResId)
        }

        private fun extractFromStyleResource(
            context: Context,
            styleResId: Int
        ): CometChatPollBubbleStyle {
            if (styleResId == 0) {
                return extractFromTypedArray(context, null)
            }
            val typedArray = context.obtainStyledAttributes(styleResId, R.styleable.CometChatPollBubble)
            return try {
                extractFromTypedArray(context, typedArray)
            } finally {
                typedArray.recycle()
            }
        }

        /**
         * Creates a [CometChatPollBubbleStyle] from a [TypedArray].
         *
         * This method extracts all properties from the TypedArray, using sentinel
         * values for common properties when XML attributes are not specified.
         *
         * @param context The context to access theme resources
         * @param typedArray The TypedArray containing style attributes
         * @return A CometChatPollBubbleStyle with values from the TypedArray
         */
        fun fromTypedArray(context: Context, typedArray: TypedArray): CometChatPollBubbleStyle {
            return try {
                extractFromTypedArray(context, typedArray)
            } finally {
                typedArray.recycle()
            }
        }


        private fun extractFromTypedArray(
            context: Context,
            typedArray: TypedArray?
        ): CometChatPollBubbleStyle {
            return CometChatPollBubbleStyle(
                // Content-specific properties - Title styling
                titleTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatPollBubble_cometchatPollBubbleTitleTextAppearance,
                    CometChatTheme.getTextAppearanceBodyMedium(context)
                ) ?: CometChatTheme.getTextAppearanceBodyMedium(context),
                titleTextColor = typedArray?.getColor(
                    R.styleable.CometChatPollBubble_cometchatPollBubbleTitleTextColor,
                    CometChatTheme.getTextColorPrimary(context)
                ) ?: CometChatTheme.getTextColorPrimary(context),

                // Content-specific properties - Option styling
                optionTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatPollBubble_cometchatPollBubbleOptionTextAppearance,
                    CometChatTheme.getTextAppearanceBodyRegular(context)
                ) ?: CometChatTheme.getTextAppearanceBodyRegular(context),
                optionTextColor = typedArray?.getColor(
                    R.styleable.CometChatPollBubble_cometchatPollBubbleOptionTextColor,
                    CometChatTheme.getTextColorPrimary(context)
                ) ?: CometChatTheme.getTextColorPrimary(context),

                // Content-specific properties - Selected radio button styling
                selectedRadioButtonStrokeColor = typedArray?.getColor(
                    R.styleable.CometChatPollBubble_cometchatPollBubbleSelectedRadioButtonStrokeColor, 0
                ) ?: 0,
                selectedRadioButtonStrokeWidth = typedArray?.getDimensionPixelSize(
                    R.styleable.CometChatPollBubble_cometchatPollBubbleSelectedRadioButtonStrokeWidth, 0
                ) ?: 0,
                selectedRadioButtonCornerRadius = typedArray?.getDimensionPixelSize(
                    R.styleable.CometChatPollBubble_cometchatPollBubbleSelectedRadioButtonCornerRadius, 0
                ) ?: 0,
                selectedIconTint = typedArray?.getColor(
                    R.styleable.CometChatPollBubble_cometchatPollBubbleSelectedIconTint,
                    CometChatTheme.getPrimaryColor(context)
                ) ?: CometChatTheme.getPrimaryColor(context),
                selectedStateDrawable = typedArray?.getDrawable(
                    R.styleable.CometChatPollBubble_cometchatPollBubbleSelectedStateDrawable
                ),

                // Content-specific properties - Unselected radio button styling
                unselectedRadioButtonStrokeColor = typedArray?.getColor(
                    R.styleable.CometChatPollBubble_cometchatPollBubbleUnselectedRadioButtonStrokeColor,
                    CometChatTheme.getIconTintSecondary(context)
                ) ?: CometChatTheme.getIconTintSecondary(context),
                unselectedRadioButtonStrokeWidth = typedArray?.getDimensionPixelSize(
                    R.styleable.CometChatPollBubble_cometchatPollBubbleUnselectedRadioButtonStrokeWidth, 0
                ) ?: 0,
                unselectedRadioButtonCornerRadius = typedArray?.getDimensionPixelSize(
                    R.styleable.CometChatPollBubble_cometchatPollBubbleUnselectedRadioButtonCornerRadius, 0
                ) ?: 0,
                unselectedIconTint = typedArray?.getColor(
                    R.styleable.CometChatPollBubble_cometchatPollBubbleUnselectedIconTint, 0
                ) ?: 0,
                unselectedStateDrawable = typedArray?.getDrawable(
                    R.styleable.CometChatPollBubble_cometchatPollBubbleUnselectedStateDrawable
                ),

                // Content-specific properties - Option avatar style
                optionAvatarStyle = typedArray?.getResourceId(
                    R.styleable.CometChatPollBubble_cometchatPollBubbleOptionAvatarStyle, 0
                ) ?: 0,

                // Content-specific properties - Progress styling
                progressColor = typedArray?.getColor(
                    R.styleable.CometChatPollBubble_cometchatPollBubbleProgressColor,
                    CometChatTheme.getPrimaryColor(context)
                ) ?: CometChatTheme.getPrimaryColor(context),
                progressBackgroundColor = typedArray?.getColor(
                    R.styleable.CometChatPollBubble_cometchatPollBubbleProgressBackgroundColor,
                    CometChatTheme.getExtendedPrimaryColor700(context)
                ) ?: CometChatTheme.getExtendedPrimaryColor700(context),
                progressIndeterminateTint = typedArray?.getColor(
                    R.styleable.CometChatPollBubble_cometchatPollBubbleProgressIndeterminateTint,
                    CometChatTheme.getIconTintSecondary(context)
                ) ?: CometChatTheme.getIconTintSecondary(context),

                // Content-specific properties - Vote count styling
                voteCountTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatPollBubble_cometchatPollBubbleVoteCountTextAppearance,
                    CometChatTheme.getTextAppearanceCaption1Regular(context)
                ) ?: CometChatTheme.getTextAppearanceCaption1Regular(context),
                voteCountTextColor = typedArray?.getColor(
                    R.styleable.CometChatPollBubble_cometchatPollBubbleVoteCountTextColor,
                    CometChatTheme.getTextColorSecondary(context)
                ) ?: CometChatTheme.getTextColorSecondary(context),

                // Common properties — READ from TypedArray with sentinel defaults
                // This allows XML-defined styles to override while falling back via resolve()
                backgroundColor = typedArray?.getColor(
                    R.styleable.CometChatPollBubble_cometchatPollBubbleBackgroundColor,
                    STYLE_NOT_SET
                ) ?: STYLE_NOT_SET,
                backgroundDrawable = typedArray?.getDrawable(
                    R.styleable.CometChatPollBubble_cometchatPollBubbleBackgroundDrawable
                ),
                cornerRadius = typedArray?.getDimension(
                    R.styleable.CometChatPollBubble_cometchatPollBubbleCornerRadius,
                    DIMENSION_NOT_SET
                ) ?: DIMENSION_NOT_SET,
                strokeWidth = typedArray?.getDimension(
                    R.styleable.CometChatPollBubble_cometchatPollBubbleStrokeWidth,
                    DIMENSION_NOT_SET
                ) ?: DIMENSION_NOT_SET,
                strokeColor = typedArray?.getColor(
                    R.styleable.CometChatPollBubble_cometchatPollBubbleStrokeColor,
                    STYLE_NOT_SET
                ) ?: STYLE_NOT_SET,
                senderNameTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatPollBubble_cometchatPollBubbleSenderNameTextAppearance,
                    STYLE_NOT_SET
                ) ?: STYLE_NOT_SET,
                senderNameTextColor = typedArray?.getColor(
                    R.styleable.CometChatPollBubble_cometchatPollBubbleSenderNameTextColor,
                    STYLE_NOT_SET
                ) ?: STYLE_NOT_SET,
                threadIndicatorTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatPollBubble_cometchatPollBubbleThreadIndicatorTextAppearance,
                    STYLE_NOT_SET
                ) ?: STYLE_NOT_SET,
                threadIndicatorTextColor = typedArray?.getColor(
                    R.styleable.CometChatPollBubble_cometchatPollBubbleThreadIndicatorTextColor,
                    STYLE_NOT_SET
                ) ?: STYLE_NOT_SET,
                threadIndicatorIconTint = typedArray?.getColor(
                    R.styleable.CometChatPollBubble_cometchatPollBubbleThreadIndicatorIconTint,
                    STYLE_NOT_SET
                ) ?: STYLE_NOT_SET,
                // Timestamp properties don't have XML attributes, keep as sentinel values
                timestampTextAppearance = STYLE_NOT_SET,
                timestampTextColor = STYLE_NOT_SET,

                // Sub-component styles — only dateStyle and messageReceiptStyle have XML attributes for PollBubble
                dateStyle = typedArray?.getResourceId(
                    R.styleable.CometChatPollBubble_cometchatPollBubbleDateStyle,
                    0
                )?.takeIf { it != 0 }?.let { CometChatDateStyle.fromStyleResource(context, it) },
                messageReceiptStyle = typedArray?.getResourceId(
                    R.styleable.CometChatPollBubble_cometchatPollBubbleMessageReceiptStyle,
                    0
                )?.takeIf { it != 0 }?.let { CometChatReceiptStyle.fromStyleResource(context, it) }
            )
        }
    }
}
