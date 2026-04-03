package com.cometchat.uikit.kotlin.presentation.shared.messagebubble.stickerbubble

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
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

/**
 * Style configuration for CometChatStickerBubble component.
 *
 * This data class holds all styling properties for the sticker message bubble view.
 * Stickers typically use a transparent background, so the default explicitly sets
 * backgroundColor to transparent rather than falling back to MessageBubbleStyle.
 *
 * Common bubble properties use sentinel values and can be resolved against
 * a [CometChatMessageBubbleStyle] via [resolve].
 *
 * Note: For stickers, backgroundColor defaults to transparent (Color.TRANSPARENT) rather
 * than using the sentinel value, since stickers should typically have no background.
 */
data class CometChatStickerBubbleStyle(
    // Common bubble properties — use sentinel defaults
    // Note: backgroundColor defaults to transparent for stickers
    @ColorInt val backgroundColor: Int = Color.TRANSPARENT,
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

    // Sub-component styles — only dateStyle and messageReceiptStyle have XML attributes for StickerBubble
    val dateStyle: CometChatDateStyle? = null,
    val messageReceiptStyle: CometChatReceiptStyle? = null
) {
    /**
     * Resolves this style against a [CometChatMessageBubbleStyle], using the bubble style's
     * values as fallbacks for any properties set to sentinel values.
     *
     * Note: For stickers, backgroundColor is NOT resolved against MessageBubbleStyle
     * since stickers should typically have transparent background. The value is used as-is.
     */
    fun resolve(messageBubbleStyle: CometChatMessageBubbleStyle): CometChatStickerBubbleStyle =
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
         * @return A CometChatStickerBubbleStyle with default values
         */
        fun default(context: Context): CometChatStickerBubbleStyle {
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
         * @return A CometChatStickerBubbleStyle configured for outgoing messages
         */
        fun outgoing(context: Context): CometChatStickerBubbleStyle {
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
         * @return A CometChatStickerBubbleStyle configured for incoming messages
         */
        fun incoming(context: Context): CometChatStickerBubbleStyle {
            return extractFromMessageBubbleStyle(
                context,
                R.attr.cometchatIncomingMessageBubbleStyle
            )
        }

        private fun extractFromMessageBubbleStyle(
            context: Context,
            messageBubbleStyleAttr: Int
        ): CometChatStickerBubbleStyle {
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
            val stickerBubbleStyleResId = messageBubbleTypedArray.getResourceId(
                R.styleable.CometChatMessageBubble_cometchatStickerBubbleStyle,
                0
            )
            messageBubbleTypedArray.recycle()

            return extractFromStyleResource(context, stickerBubbleStyleResId)
        }

        private fun extractFromStyleResource(
            context: Context,
            styleResId: Int
        ): CometChatStickerBubbleStyle {
            if (styleResId == 0) {
                return extractFromTypedArray(context, null)
            }
            val typedArray = context.obtainStyledAttributes(styleResId, R.styleable.CometChatStickerBubble)
            return try {
                extractFromTypedArray(context, typedArray)
            } finally {
                typedArray.recycle()
            }
        }

        /**
         * Creates a [CometChatStickerBubbleStyle] from a [TypedArray].
         *
         * This method extracts all properties from the TypedArray, using sentinel
         * values for common properties when XML attributes are not specified.
         *
         * @param context The context to access theme resources
         * @param typedArray The TypedArray containing style attributes
         * @return A CometChatStickerBubbleStyle with values from the TypedArray
         */
        fun fromTypedArray(context: Context, typedArray: TypedArray): CometChatStickerBubbleStyle {
            return try {
                extractFromTypedArray(context, typedArray)
            } finally {
                typedArray.recycle()
            }
        }

        private fun extractFromTypedArray(
            context: Context,
            typedArray: TypedArray?
        ): CometChatStickerBubbleStyle {
            // Stickers have NO content-specific properties - they use only common properties
            // Common properties are READ from TypedArray with sentinel defaults
            // Note: backgroundColor defaults to transparent for stickers when not specified in XML
            return CometChatStickerBubbleStyle(
                // Common properties — READ from TypedArray with sentinel defaults
                // This allows XML-defined styles to override while falling back via resolve()
                // Note: Stickers use transparent background by default when not specified
                backgroundColor = typedArray?.getColor(
                    R.styleable.CometChatStickerBubble_cometchatStickerBubbleBackgroundColor,
                    Color.TRANSPARENT
                ) ?: Color.TRANSPARENT,
                backgroundDrawable = typedArray?.getDrawable(
                    R.styleable.CometChatStickerBubble_cometchatStickerBubbleBackgroundDrawable
                ),
                cornerRadius = typedArray?.getDimension(
                    R.styleable.CometChatStickerBubble_cometchatStickerBubbleCornerRadius,
                    DIMENSION_NOT_SET
                ) ?: DIMENSION_NOT_SET,
                strokeWidth = typedArray?.getDimension(
                    R.styleable.CometChatStickerBubble_cometchatStickerBubbleStrokeWidth,
                    DIMENSION_NOT_SET
                ) ?: DIMENSION_NOT_SET,
                strokeColor = typedArray?.getColor(
                    R.styleable.CometChatStickerBubble_cometchatStickerBubbleStrokeColor,
                    STYLE_NOT_SET
                ) ?: STYLE_NOT_SET,
                senderNameTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatStickerBubble_cometchatStickerBubbleSenderNameTextAppearance,
                    STYLE_NOT_SET
                ) ?: STYLE_NOT_SET,
                senderNameTextColor = typedArray?.getColor(
                    R.styleable.CometChatStickerBubble_cometchatStickerBubbleSenderNameTextColor,
                    STYLE_NOT_SET
                ) ?: STYLE_NOT_SET,
                threadIndicatorTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatStickerBubble_cometchatStickerBubbleThreadIndicatorTextAppearance,
                    STYLE_NOT_SET
                ) ?: STYLE_NOT_SET,
                threadIndicatorTextColor = typedArray?.getColor(
                    R.styleable.CometChatStickerBubble_cometchatStickerBubbleThreadIndicatorTextColor,
                    STYLE_NOT_SET
                ) ?: STYLE_NOT_SET,
                threadIndicatorIconTint = typedArray?.getColor(
                    R.styleable.CometChatStickerBubble_cometchatStickerBubbleThreadIndicatorIconTint,
                    STYLE_NOT_SET
                ) ?: STYLE_NOT_SET,
                // Timestamp properties don't have XML attributes, keep as sentinel values
                timestampTextAppearance = STYLE_NOT_SET,
                timestampTextColor = STYLE_NOT_SET,

                // Sub-component styles — only dateStyle and messageReceiptStyle have XML attributes for StickerBubble
                dateStyle = typedArray?.getResourceId(
                    R.styleable.CometChatStickerBubble_cometchatStickerBubbleDateStyle,
                    0
                )?.takeIf { it != 0 }?.let { CometChatDateStyle.fromStyleResource(context, it) },
                messageReceiptStyle = typedArray?.getResourceId(
                    R.styleable.CometChatStickerBubble_cometchatStickerBubbleMessageReceiptStyle,
                    0
                )?.takeIf { it != 0 }?.let { CometChatReceiptStyle.fromStyleResource(context, it) }
            )
        }
    }
}
