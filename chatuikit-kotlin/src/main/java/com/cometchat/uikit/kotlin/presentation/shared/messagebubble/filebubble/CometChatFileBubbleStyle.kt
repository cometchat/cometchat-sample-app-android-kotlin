package com.cometchat.uikit.kotlin.presentation.shared.messagebubble.filebubble

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
 * Style configuration for CometChatFileBubble component.
 *
 * This data class holds all styling properties for the file bubble view.
 * Common bubble properties (background, corner radius, stroke, sender name,
 * thread indicator, timestamp) use sentinel values ([STYLE_NOT_SET] or
 * [DIMENSION_NOT_SET]) to indicate "not set". When not set, they fall back to
 * values from [CometChatMessageBubbleStyle] via [resolve].
 */
data class CometChatFileBubbleStyle(
    // Content-specific properties
    @StyleRes val titleTextAppearance: Int = 0,
    @ColorInt val titleColor: Int = 0,
    @StyleRes val subtitleTextAppearance: Int = 0,
    @ColorInt val subtitleColor: Int = 0,
    @ColorInt val fileDownloadIconTint: Int = 0,

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
     * @return A new [CometChatFileBubbleStyle] with all sentinel values resolved
     */
    fun resolve(messageBubbleStyle: CometChatMessageBubbleStyle): CometChatFileBubbleStyle =
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
         * @return A CometChatFileBubbleStyle with default values
         */
        fun default(context: Context): CometChatFileBubbleStyle {
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
         * @return A CometChatFileBubbleStyle configured for outgoing messages
         */
        fun outgoing(context: Context): CometChatFileBubbleStyle {
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
         * @return A CometChatFileBubbleStyle configured for incoming messages
         */
        fun incoming(context: Context): CometChatFileBubbleStyle {
            return extractFromMessageBubbleStyle(
                context,
                R.attr.cometchatIncomingMessageBubbleStyle
            )
        }

        private fun extractFromMessageBubbleStyle(
            context: Context,
            messageBubbleStyleAttr: Int
        ): CometChatFileBubbleStyle {
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
            val fileBubbleStyleResId = messageBubbleTypedArray.getResourceId(
                R.styleable.CometChatMessageBubble_cometchatFileBubbleStyle,
                0
            )
            messageBubbleTypedArray.recycle()

            return extractFromStyleResource(context, fileBubbleStyleResId)
        }

        private fun extractFromStyleResource(
            context: Context,
            styleResId: Int
        ): CometChatFileBubbleStyle {
            if (styleResId == 0) {
                return extractFromTypedArray(context, null)
            }
            val typedArray = context.obtainStyledAttributes(styleResId, R.styleable.CometChatFileBubble)
            return try {
                extractFromTypedArray(context, typedArray)
            } finally {
                typedArray.recycle()
            }
        }

        /**
         * Creates a [CometChatFileBubbleStyle] from a [TypedArray].
         *
         * This method extracts all properties from the TypedArray, using sentinel
         * values for common properties when XML attributes are not specified.
         *
         * @param context The context to access theme resources
         * @param typedArray The TypedArray containing style attributes
         * @return A CometChatFileBubbleStyle with values from the TypedArray
         */
        fun fromTypedArray(context: Context, typedArray: TypedArray): CometChatFileBubbleStyle {
            return try {
                extractFromTypedArray(context, typedArray)
            } finally {
                typedArray.recycle()
            }
        }

        private fun extractFromTypedArray(
            context: Context,
            typedArray: TypedArray?
        ): CometChatFileBubbleStyle {
            return CometChatFileBubbleStyle(
                // Content-specific properties - extracted from XML with theme defaults
                titleTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatFileBubble_cometchatFileBubbleTitleTextAppearance,
                    CometChatTheme.getTextAppearanceBodyMedium(context)
                ) ?: CometChatTheme.getTextAppearanceBodyMedium(context),
                titleColor = typedArray?.getColor(
                    R.styleable.CometChatFileBubble_cometchatFileBubbleTitleColor,
                    CometChatTheme.getTextColorPrimary(context)
                ) ?: CometChatTheme.getTextColorPrimary(context),
                subtitleTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatFileBubble_cometchatFileBubbleSubTitleTextAppearance,
                    CometChatTheme.getTextAppearanceCaption1Regular(context)
                ) ?: CometChatTheme.getTextAppearanceCaption1Regular(context),
                subtitleColor = typedArray?.getColor(
                    R.styleable.CometChatFileBubble_cometchatFileBubbleSubTitleColor,
                    CometChatTheme.getTextColorSecondary(context)
                ) ?: CometChatTheme.getTextColorSecondary(context),
                fileDownloadIconTint = typedArray?.getColor(
                    R.styleable.CometChatFileBubble_cometchatFileBubbleFileDownloadIconTint,
                    CometChatTheme.getIconTintSecondary(context)
                ) ?: CometChatTheme.getIconTintSecondary(context),

                // Common properties — READ from TypedArray with sentinel defaults
                // This allows XML-defined styles to override while falling back via resolve()
                backgroundColor = typedArray?.getColor(
                    R.styleable.CometChatFileBubble_cometchatFileBubbleBackgroundColor,
                    STYLE_NOT_SET
                ) ?: STYLE_NOT_SET,
                backgroundDrawable = typedArray?.getDrawable(
                    R.styleable.CometChatFileBubble_cometchatFileBubbleBackgroundDrawable
                ),
                cornerRadius = typedArray?.getDimension(
                    R.styleable.CometChatFileBubble_cometchatFileBubbleCornerRadius,
                    DIMENSION_NOT_SET
                ) ?: DIMENSION_NOT_SET,
                strokeWidth = typedArray?.getDimension(
                    R.styleable.CometChatFileBubble_cometchatFileBubbleStrokeWidth,
                    DIMENSION_NOT_SET
                ) ?: DIMENSION_NOT_SET,
                strokeColor = typedArray?.getColor(
                    R.styleable.CometChatFileBubble_cometchatFileBubbleStrokeColor,
                    STYLE_NOT_SET
                ) ?: STYLE_NOT_SET,
                senderNameTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatFileBubble_cometchatFileBubbleSenderNameTextAppearance,
                    STYLE_NOT_SET
                ) ?: STYLE_NOT_SET,
                senderNameTextColor = typedArray?.getColor(
                    R.styleable.CometChatFileBubble_cometchatFileBubbleSenderNameTextColor,
                    STYLE_NOT_SET
                ) ?: STYLE_NOT_SET,
                threadIndicatorTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatFileBubble_cometchatFileBubbleThreadIndicatorTextAppearance,
                    STYLE_NOT_SET
                ) ?: STYLE_NOT_SET,
                threadIndicatorTextColor = typedArray?.getColor(
                    R.styleable.CometChatFileBubble_cometchatFileBubbleThreadIndicatorTextColor,
                    STYLE_NOT_SET
                ) ?: STYLE_NOT_SET,
                threadIndicatorIconTint = typedArray?.getColor(
                    R.styleable.CometChatFileBubble_cometchatFileBubbleThreadIndicatorIconTint,
                    STYLE_NOT_SET
                ) ?: STYLE_NOT_SET,
                // Timestamp properties don't have XML attributes, keep as sentinel values
                timestampTextAppearance = STYLE_NOT_SET,
                timestampTextColor = STYLE_NOT_SET,

                // Sub-component styles — read from TypedArray if specified, otherwise null (inherited via resolve())
                dateStyle = typedArray?.getResourceId(
                    R.styleable.CometChatFileBubble_cometchatFileBubbleDateStyle,
                    0
                )?.takeIf { it != 0 }?.let { CometChatDateStyle.fromStyleResource(context, it) },
                messageReceiptStyle = typedArray?.getResourceId(
                    R.styleable.CometChatFileBubble_cometchatFileBubbleMessageReceiptStyle,
                    0
                )?.takeIf { it != 0 }?.let { CometChatReceiptStyle.fromStyleResource(context, it) }
            )
        }
    }
}
