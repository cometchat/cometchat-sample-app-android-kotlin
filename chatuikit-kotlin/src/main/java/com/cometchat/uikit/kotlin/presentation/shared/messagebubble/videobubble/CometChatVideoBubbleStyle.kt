package com.cometchat.uikit.kotlin.presentation.shared.messagebubble.videobubble

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
 * Style configuration for CometChatVideoBubble component.
 *
 * This data class holds all styling properties for the video bubble view.
 * Common bubble properties (background, corner radius, stroke, sender name,
 * thread indicator, timestamp) use sentinel values ([STYLE_NOT_SET] or
 * [DIMENSION_NOT_SET]) to indicate "not set". When not set, they fall back to
 * values from [CometChatMessageBubbleStyle] via [resolve].
 */
data class CometChatVideoBubbleStyle(
    // Content-specific properties
    @Dimension val videoCornerRadius: Float = 0f,
    @Dimension val videoStrokeWidth: Float = 0f,
    @ColorInt val videoStrokeColor: Int = 0,
    @ColorInt val playIconTint: Int = 0,
    @ColorInt val playIconBackgroundColor: Int = 0,
    @ColorInt val captionTextColor: Int = 0,
    @StyleRes val captionTextAppearance: Int = 0,
    @ColorInt val progressIndeterminateTint: Int = 0,
    @Dimension val gridSpacing: Float = 0f,
    @Dimension val maxGridWidth: Float = 0f,
    @ColorInt val moreOverlayBackgroundColor: Int = 0,
    @ColorInt val moreOverlayTextColor: Int = 0,
    @StyleRes val moreOverlayTextAppearance: Int = 0,

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

    // Sub-component styles — only dateStyle and messageReceiptStyle have XML attributes
    val dateStyle: CometChatDateStyle? = null,
    val messageReceiptStyle: CometChatReceiptStyle? = null
) {
    /**
     * Resolves this style against a [CometChatMessageBubbleStyle], using the bubble style's
     * values as fallbacks for any properties set to sentinel values.
     *
     * @param messageBubbleStyle The message bubble style to use as fallback
     * @return A new [CometChatVideoBubbleStyle] with all sentinel values resolved
     */
    fun resolve(messageBubbleStyle: CometChatMessageBubbleStyle): CometChatVideoBubbleStyle =
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
         * @return A CometChatVideoBubbleStyle with default values
         */
        fun default(context: Context): CometChatVideoBubbleStyle {
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
         * @return A CometChatVideoBubbleStyle configured for outgoing messages
         */
        fun outgoing(context: Context): CometChatVideoBubbleStyle {
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
         * @return A CometChatVideoBubbleStyle configured for incoming messages
         */
        fun incoming(context: Context): CometChatVideoBubbleStyle {
            return extractFromMessageBubbleStyle(
                context,
                R.attr.cometchatIncomingMessageBubbleStyle
            )
        }

        private fun extractFromMessageBubbleStyle(
            context: Context,
            messageBubbleStyleAttr: Int
        ): CometChatVideoBubbleStyle {
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
            val videoBubbleStyleResId = messageBubbleTypedArray.getResourceId(
                R.styleable.CometChatMessageBubble_cometchatVideoBubbleStyle,
                0
            )
            messageBubbleTypedArray.recycle()

            return extractFromStyleResource(context, videoBubbleStyleResId)
        }

        private fun extractFromStyleResource(
            context: Context,
            styleResId: Int
        ): CometChatVideoBubbleStyle {
            if (styleResId == 0) {
                return extractFromTypedArray(context, null)
            }
            val typedArray = context.obtainStyledAttributes(styleResId, R.styleable.CometChatVideoBubble)
            return try {
                extractFromTypedArray(context, typedArray)
            } finally {
                typedArray.recycle()
            }
        }

        /**
         * Creates a [CometChatVideoBubbleStyle] from a [TypedArray].
         *
         * This method extracts all properties from the TypedArray, using sentinel
         * values for common properties when XML attributes are not specified.
         *
         * @param context The context to access theme resources
         * @param typedArray The TypedArray containing style attributes
         * @return A CometChatVideoBubbleStyle with values from the TypedArray
         */
        fun fromTypedArray(context: Context, typedArray: TypedArray): CometChatVideoBubbleStyle {
            return try {
                extractFromTypedArray(context, typedArray)
            } finally {
                typedArray.recycle()
            }
        }

        private fun extractFromTypedArray(
            context: Context,
            typedArray: TypedArray?
        ): CometChatVideoBubbleStyle {
            return CometChatVideoBubbleStyle(
                // Content-specific properties - extracted from XML with theme defaults
                videoCornerRadius = typedArray?.getDimension(
                    R.styleable.CometChatVideoBubble_cometchatVideoBubbleVideoCornerRadius,
                    0f
                ) ?: 0f,
                videoStrokeWidth = typedArray?.getInt(
                    R.styleable.CometChatVideoBubble_cometchatVideoBubbleVideoStrokeWidth,
                    0
                )?.toFloat() ?: 0f,
                videoStrokeColor = typedArray?.getColor(
                    R.styleable.CometChatVideoBubble_cometchatVideoBubbleVideoStrokeColor,
                    0
                ) ?: 0,
                playIconTint = typedArray?.getColor(
                    R.styleable.CometChatVideoBubble_cometchatVideoBubblePlayIconTint,
                    CometChatTheme.getColorWhite(context)
                ) ?: CometChatTheme.getColorWhite(context),
                playIconBackgroundColor = typedArray?.getColor(
                    R.styleable.CometChatVideoBubble_cometchatVideoBubblePlayIconBackgroundColor,
                    CometChatTheme.getPrimaryColor(context)
                ) ?: CometChatTheme.getPrimaryColor(context),
                captionTextColor = typedArray?.getColor(
                    R.styleable.CometChatVideoBubble_cometchatVideoBubbleCaptionTextColor,
                    CometChatTheme.getTextColorPrimary(context)
                ) ?: CometChatTheme.getTextColorPrimary(context),
                captionTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatVideoBubble_cometchatVideoBubbleCaptionTextAppearance,
                    CometChatTheme.getTextAppearanceBodyRegular(context)
                ) ?: CometChatTheme.getTextAppearanceBodyRegular(context),
                progressIndeterminateTint = typedArray?.getColor(
                    R.styleable.CometChatVideoBubble_cometchatVideoBubbleProgressIndeterminateTint,
                    CometChatTheme.getIconTintSecondary(context)
                ) ?: CometChatTheme.getIconTintSecondary(context),
                gridSpacing = typedArray?.getDimension(
                    R.styleable.CometChatVideoBubble_cometchatVideoBubbleGridSpacing,
                    0f
                ) ?: 0f,
                maxGridWidth = typedArray?.getDimension(
                    R.styleable.CometChatVideoBubble_cometchatVideoBubbleMaxGridWidth,
                    0f
                ) ?: 0f,
                moreOverlayBackgroundColor = typedArray?.getColor(
                    R.styleable.CometChatVideoBubble_cometchatVideoBubbleMoreOverlayBackgroundColor,
                    0x99000000.toInt()
                ) ?: 0x99000000.toInt(),
                moreOverlayTextColor = typedArray?.getColor(
                    R.styleable.CometChatVideoBubble_cometchatVideoBubbleMoreOverlayTextColor,
                    CometChatTheme.getColorWhite(context)
                ) ?: CometChatTheme.getColorWhite(context),
                moreOverlayTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatVideoBubble_cometchatVideoBubbleMoreOverlayTextAppearance,
                    CometChatTheme.getTextAppearanceHeading2Bold(context)
                ) ?: CometChatTheme.getTextAppearanceHeading2Bold(context),

                // Common properties — READ from TypedArray with sentinel defaults
                // This allows XML-defined styles to override while falling back via resolve()
                backgroundColor = typedArray?.getColor(
                    R.styleable.CometChatVideoBubble_cometchatVideoBubbleBackgroundColor,
                    STYLE_NOT_SET
                ) ?: STYLE_NOT_SET,
                backgroundDrawable = typedArray?.getDrawable(
                    R.styleable.CometChatVideoBubble_cometchatVideoBubbleBackgroundDrawable
                ),
                cornerRadius = typedArray?.getDimension(
                    R.styleable.CometChatVideoBubble_cometchatVideoBubbleCornerRadius,
                    DIMENSION_NOT_SET
                ) ?: DIMENSION_NOT_SET,
                strokeWidth = typedArray?.getInt(
                    R.styleable.CometChatVideoBubble_cometchatVideoBubbleStrokeWidth,
                    DIMENSION_NOT_SET.toInt()
                )?.toFloat() ?: DIMENSION_NOT_SET,
                strokeColor = typedArray?.getColor(
                    R.styleable.CometChatVideoBubble_cometchatVideoBubbleStrokeColor,
                    STYLE_NOT_SET
                ) ?: STYLE_NOT_SET,
                senderNameTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatVideoBubble_cometchatVideoBubbleSenderNameTextAppearance,
                    STYLE_NOT_SET
                ) ?: STYLE_NOT_SET,
                senderNameTextColor = typedArray?.getColor(
                    R.styleable.CometChatVideoBubble_cometchatVideoBubbleSenderNameTextColor,
                    STYLE_NOT_SET
                ) ?: STYLE_NOT_SET,
                threadIndicatorTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatVideoBubble_cometchatVideoBubbleThreadIndicatorTextAppearance,
                    STYLE_NOT_SET
                ) ?: STYLE_NOT_SET,
                threadIndicatorTextColor = typedArray?.getColor(
                    R.styleable.CometChatVideoBubble_cometchatVideoBubbleThreadIndicatorTextColor,
                    STYLE_NOT_SET
                ) ?: STYLE_NOT_SET,
                threadIndicatorIconTint = typedArray?.getColor(
                    R.styleable.CometChatVideoBubble_cometchatVideoBubbleThreadIndicatorIconTint,
                    STYLE_NOT_SET
                ) ?: STYLE_NOT_SET,
                // Timestamp properties don't have XML attributes, keep as sentinel values
                timestampTextAppearance = STYLE_NOT_SET,
                timestampTextColor = STYLE_NOT_SET,

                // Sub-component styles — read from TypedArray if specified, otherwise null (inherited via resolve())
                dateStyle = typedArray?.getResourceId(
                    R.styleable.CometChatVideoBubble_cometchatVideoBubbleDateStyle,
                    0
                )?.takeIf { it != 0 }?.let { CometChatDateStyle.fromStyleResource(context, it) },
                messageReceiptStyle = typedArray?.getResourceId(
                    R.styleable.CometChatVideoBubble_cometchatVideoBubbleMessageReceiptStyle,
                    0
                )?.takeIf { it != 0 }?.let { CometChatReceiptStyle.fromStyleResource(context, it) }
            )
        }
    }
}
