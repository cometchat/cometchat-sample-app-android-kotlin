package com.cometchat.uikit.kotlin.presentation.shared.messagebubble.imagebubble

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
 * Style configuration for CometChatImageBubble component.
 *
 * This data class holds all styling properties for the image bubble view.
 * Common bubble properties (background, corner radius, stroke, sender name,
 * thread indicator, timestamp) use sentinel values ([STYLE_NOT_SET] or
 * [DIMENSION_NOT_SET]) to indicate "not set". When not set, they fall back to
 * values from [CometChatMessageBubbleStyle] via [resolve].
 */
data class CometChatImageBubbleStyle(
    // Content-specific properties (always defined here)
    @Dimension val imageCornerRadius: Float = 0f,
    @Dimension val imageStrokeWidth: Float = 0f,
    @ColorInt val imageStrokeColor: Int = 0,
    @ColorInt val captionTextColor: Int = 0,
    @StyleRes val captionTextAppearance: Int = 0,
    @ColorInt val progressIndeterminateTint: Int = 0,

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
     * @return A new [CometChatImageBubbleStyle] with all sentinel values resolved
     */
    fun resolve(messageBubbleStyle: CometChatMessageBubbleStyle): CometChatImageBubbleStyle =
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
         * @return A CometChatImageBubbleStyle with default values
         */
        fun default(context: Context): CometChatImageBubbleStyle {
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
         * @return A CometChatImageBubbleStyle configured for outgoing messages
         */
        fun outgoing(context: Context): CometChatImageBubbleStyle {
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
         * @return A CometChatImageBubbleStyle configured for incoming messages
         */
        fun incoming(context: Context): CometChatImageBubbleStyle {
            return extractFromMessageBubbleStyle(
                context,
                R.attr.cometchatIncomingMessageBubbleStyle
            )
        }

        private fun extractFromMessageBubbleStyle(
            context: Context,
            messageBubbleStyleAttr: Int
        ): CometChatImageBubbleStyle {
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
            val imageBubbleStyleResId = messageBubbleTypedArray.getResourceId(
                R.styleable.CometChatMessageBubble_cometchatImageBubbleStyle,
                0
            )
            messageBubbleTypedArray.recycle()

            return extractFromStyleResource(context, imageBubbleStyleResId)
        }

        private fun extractFromStyleResource(
            context: Context,
            styleResId: Int
        ): CometChatImageBubbleStyle {
            if (styleResId == 0) {
                return extractFromTypedArray(context, null)
            }
            val typedArray = context.obtainStyledAttributes(styleResId, R.styleable.CometChatImageBubble)
            return try {
                extractFromTypedArray(context, typedArray)
            } finally {
                typedArray.recycle()
            }
        }

        /**
         * Creates a [CometChatImageBubbleStyle] from a [TypedArray].
         *
         * This method extracts all properties from the TypedArray, using sentinel
         * values for common properties when XML attributes are not specified.
         *
         * @param context The context to access theme resources
         * @param typedArray The TypedArray containing style attributes
         * @return A CometChatImageBubbleStyle with values from the TypedArray
         */
        fun fromTypedArray(context: Context, typedArray: TypedArray): CometChatImageBubbleStyle {
            return try {
                extractFromTypedArray(context, typedArray)
            } finally {
                typedArray.recycle()
            }
        }

        private fun extractFromTypedArray(
            context: Context,
            typedArray: TypedArray?
        ): CometChatImageBubbleStyle {
            return CometChatImageBubbleStyle(
                // Content-specific properties - extracted from XML with theme defaults
                imageCornerRadius = typedArray?.getDimension(
                    R.styleable.CometChatImageBubble_cometchatImageBubbleImageCornerRadius,
                    0f
                ) ?: 0f,
                imageStrokeWidth = typedArray?.getDimension(
                    R.styleable.CometChatImageBubble_cometchatImageBubbleImageStrokeWidth,
                    0f
                ) ?: 0f,
                imageStrokeColor = typedArray?.getColor(
                    R.styleable.CometChatImageBubble_cometchatImageBubbleImageStrokeColor,
                    0
                ) ?: 0,
                captionTextColor = typedArray?.getColor(
                    R.styleable.CometChatImageBubble_cometchatImageBubbleCaptionTextColor,
                    CometChatTheme.getTextColorPrimary(context)
                ) ?: CometChatTheme.getTextColorPrimary(context),
                captionTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatImageBubble_cometchatImageBubbleCaptionTextAppearance,
                    CometChatTheme.getTextAppearanceBodyRegular(context)
                ) ?: CometChatTheme.getTextAppearanceBodyRegular(context),
                progressIndeterminateTint = typedArray?.getColor(
                    R.styleable.CometChatImageBubble_cometchatImageBubbleProgressIndeterminateTint,
                    CometChatTheme.getIconTintSecondary(context)
                ) ?: CometChatTheme.getIconTintSecondary(context),

                // Common properties — READ from TypedArray with sentinel defaults
                // This allows XML-defined styles to override while falling back via resolve()
                backgroundColor = typedArray?.getColor(
                    R.styleable.CometChatImageBubble_cometchatImageBubbleBackgroundColor,
                    STYLE_NOT_SET
                ) ?: STYLE_NOT_SET,
                backgroundDrawable = typedArray?.getDrawable(
                    R.styleable.CometChatImageBubble_cometchatImageBubbleBackgroundDrawable
                ),
                cornerRadius = typedArray?.getDimension(
                    R.styleable.CometChatImageBubble_cometchatImageBubbleCornerRadius,
                    DIMENSION_NOT_SET
                ) ?: DIMENSION_NOT_SET,
                strokeWidth = typedArray?.getDimension(
                    R.styleable.CometChatImageBubble_cometchatImageBubbleStrokeWidth,
                    DIMENSION_NOT_SET
                ) ?: DIMENSION_NOT_SET,
                strokeColor = typedArray?.getColor(
                    R.styleable.CometChatImageBubble_cometchatImageBubbleStrokeColor,
                    STYLE_NOT_SET
                ) ?: STYLE_NOT_SET,
                senderNameTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatImageBubble_cometchatImageBubbleSenderNameTextAppearance,
                    STYLE_NOT_SET
                ) ?: STYLE_NOT_SET,
                senderNameTextColor = typedArray?.getColor(
                    R.styleable.CometChatImageBubble_cometchatImageBubbleSenderNameTextColor,
                    STYLE_NOT_SET
                ) ?: STYLE_NOT_SET,
                threadIndicatorTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatImageBubble_cometchatImageBubbleThreadIndicatorTextAppearance,
                    STYLE_NOT_SET
                ) ?: STYLE_NOT_SET,
                threadIndicatorTextColor = typedArray?.getColor(
                    R.styleable.CometChatImageBubble_cometchatImageBubbleThreadIndicatorTextColor,
                    STYLE_NOT_SET
                ) ?: STYLE_NOT_SET,
                threadIndicatorIconTint = typedArray?.getColor(
                    R.styleable.CometChatImageBubble_cometchatImageBubbleThreadIndicatorIconTint,
                    STYLE_NOT_SET
                ) ?: STYLE_NOT_SET,
                // Timestamp properties don't have XML attributes, keep as sentinel values
                timestampTextAppearance = STYLE_NOT_SET,
                timestampTextColor = STYLE_NOT_SET,

                // Sub-component styles — read from TypedArray if specified, otherwise null (inherited via resolve())
                dateStyle = typedArray?.getResourceId(
                    R.styleable.CometChatImageBubble_cometchatImageBubbleDateStyle,
                    0
                )?.takeIf { it != 0 }?.let { CometChatDateStyle.fromStyleResource(context, it) },
                messageReceiptStyle = typedArray?.getResourceId(
                    R.styleable.CometChatImageBubble_cometchatImageBubbleMessageReceiptStyle,
                    0
                )?.takeIf { it != 0 }?.let { CometChatReceiptStyle.fromStyleResource(context, it) }
            )
        }
    }
}
