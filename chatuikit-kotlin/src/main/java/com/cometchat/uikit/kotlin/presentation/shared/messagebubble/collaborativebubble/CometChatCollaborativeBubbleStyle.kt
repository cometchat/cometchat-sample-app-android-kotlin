package com.cometchat.uikit.kotlin.presentation.shared.messagebubble.collaborativebubble

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
 * Style configuration for CometChatCollaborativeBubble component.
 *
 * This data class holds all styling properties for the collaborative document/whiteboard
 * message bubble view. Common bubble properties use sentinel values and can be
 * resolved against a [CometChatMessageBubbleStyle] via [resolve].
 */
data class CometChatCollaborativeBubbleStyle(
    // Content-specific properties
    @StyleRes val titleTextAppearance: Int = 0,
    @ColorInt val titleTextColor: Int = 0,
    @StyleRes val subtitleTextAppearance: Int = 0,
    @ColorInt val subtitleTextColor: Int = 0,
    @ColorInt val iconTint: Int = 0,
    val iconDrawable: Drawable? = null,
    @StyleRes val buttonTextAppearance: Int = 0,
    @ColorInt val buttonTextColor: Int = 0,
    @ColorInt val separatorColor: Int = 0,
    @Dimension val imageStrokeWidth: Float = 0f,
    @ColorInt val imageStrokeColor: Int = 0,
    @Dimension val imageCornerRadius: Float = 0f,

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

    // Sub-component styles — only dateStyle and messageReceiptStyle have XML attributes for CollaborativeBubble
    val dateStyle: CometChatDateStyle? = null,
    val messageReceiptStyle: CometChatReceiptStyle? = null
) {
    /**
     * Resolves this style against a [CometChatMessageBubbleStyle], using the bubble style's
     * values as fallbacks for any properties set to sentinel values.
     */
    fun resolve(messageBubbleStyle: CometChatMessageBubbleStyle): CometChatCollaborativeBubbleStyle =
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
         * @return A CometChatCollaborativeBubbleStyle with default values
         */
        fun default(context: Context): CometChatCollaborativeBubbleStyle {
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
         * @return A CometChatCollaborativeBubbleStyle configured for outgoing messages
         */
        fun outgoing(context: Context): CometChatCollaborativeBubbleStyle {
            return extractFromMessageBubbleStyle(
                context,
                R.attr.cometchatOutgoingMessageBubbleStyle,
                isOutgoing = true
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
         * @return A CometChatCollaborativeBubbleStyle configured for incoming messages
         */
        fun incoming(context: Context): CometChatCollaborativeBubbleStyle {
            return extractFromMessageBubbleStyle(
                context,
                R.attr.cometchatIncomingMessageBubbleStyle,
                isOutgoing = false
            )
        }

        private fun extractFromMessageBubbleStyle(
            context: Context,
            messageBubbleStyleAttr: Int,
            isOutgoing: Boolean
        ): CometChatCollaborativeBubbleStyle {
            val themeTypedArray = context.obtainStyledAttributes(intArrayOf(messageBubbleStyleAttr))
            val messageBubbleStyleResId = themeTypedArray.getResourceId(0, 0)
            themeTypedArray.recycle()

            if (messageBubbleStyleResId == 0) {
                return extractFromStyleResource(context, 0, isOutgoing)
            }

            val messageBubbleTypedArray = context.obtainStyledAttributes(
                messageBubbleStyleResId,
                R.styleable.CometChatMessageBubble
            )
            val collaborativeBubbleStyleResId = messageBubbleTypedArray.getResourceId(
                R.styleable.CometChatMessageBubble_cometchatCollaborativeBubbleStyle,
                0
            )
            messageBubbleTypedArray.recycle()

            return extractFromStyleResource(context, collaborativeBubbleStyleResId, isOutgoing)
        }

        private fun extractFromStyleResource(
            context: Context,
            styleResId: Int,
            isOutgoing: Boolean = false
        ): CometChatCollaborativeBubbleStyle {
            if (styleResId == 0) {
                return extractFromTypedArray(context, null, isOutgoing)
            }
            val typedArray = context.obtainStyledAttributes(styleResId, R.styleable.CometChatCollaborativeBubble)
            return try {
                extractFromTypedArray(context, typedArray, isOutgoing)
            } finally {
                typedArray.recycle()
            }
        }

        /**
         * Creates a [CometChatCollaborativeBubbleStyle] from a [TypedArray].
         *
         * This method extracts all properties from the TypedArray, using sentinel
         * values for common properties when XML attributes are not specified.
         *
         * @param context The context to access theme resources
         * @param typedArray The TypedArray containing style attributes
         * @return A CometChatCollaborativeBubbleStyle with values from the TypedArray
         */
        fun fromTypedArray(context: Context, typedArray: TypedArray): CometChatCollaborativeBubbleStyle {
            return try {
                extractFromTypedArray(context, typedArray, isOutgoing = false)
            } finally {
                typedArray.recycle()
            }
        }

        private fun extractFromTypedArray(
            context: Context,
            typedArray: TypedArray?,
            isOutgoing: Boolean = false
        ): CometChatCollaborativeBubbleStyle {
            // For outgoing bubbles, use extended primary color 800
            // For incoming bubbles, use stroke color dark
            val separatorColorFallback = if (isOutgoing) {
                CometChatTheme.getExtendedPrimaryColor800(context)
            } else {
                CometChatTheme.getStrokeColorDark(context)
            }

            return CometChatCollaborativeBubbleStyle(
                // Content-specific properties - extracted from XML with theme defaults
                titleTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatCollaborativeBubble_cometchatCollaborativeBubbleTitleTextAppearance,
                    CometChatTheme.getTextAppearanceBodyMedium(context)
                ) ?: CometChatTheme.getTextAppearanceBodyMedium(context),
                titleTextColor = typedArray?.getColor(
                    R.styleable.CometChatCollaborativeBubble_cometchatCollaborativeBubbleTitleTextColor,
                    CometChatTheme.getTextColorPrimary(context)
                ) ?: CometChatTheme.getTextColorPrimary(context),
                subtitleTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatCollaborativeBubble_cometchatCollaborativeBubbleSubtitleTextAppearance,
                    CometChatTheme.getTextAppearanceCaption1Regular(context)
                ) ?: CometChatTheme.getTextAppearanceCaption1Regular(context),
                subtitleTextColor = typedArray?.getColor(
                    R.styleable.CometChatCollaborativeBubble_cometchatCollaborativeBubbleSubtitleTextColor,
                    CometChatTheme.getTextColorSecondary(context)
                ) ?: CometChatTheme.getTextColorSecondary(context),
                iconTint = typedArray?.getColor(
                    R.styleable.CometChatCollaborativeBubble_cometchatCollaborativeBubbleIconTint,
                    CometChatTheme.getPrimaryColor(context)
                ) ?: CometChatTheme.getPrimaryColor(context),
                iconDrawable = typedArray?.getDrawable(
                    R.styleable.CometChatCollaborativeBubble_cometchatCollaborativeBubbleIconDrawable
                ),
                buttonTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatCollaborativeBubble_cometchatCollaborativeBubbleButtonTextAppearance,
                    CometChatTheme.getTextAppearanceButtonMedium(context)
                ) ?: CometChatTheme.getTextAppearanceButtonMedium(context),
                buttonTextColor = typedArray?.getColor(
                    R.styleable.CometChatCollaborativeBubble_cometchatCollaborativeBubbleButtonTextColor,
                    CometChatTheme.getPrimaryColor(context)
                ) ?: CometChatTheme.getPrimaryColor(context),
                separatorColor = typedArray?.getColor(
                    R.styleable.CometChatCollaborativeBubble_cometchatCollaborativeBubbleSeparatorColor,
                    separatorColorFallback
                ) ?: separatorColorFallback,
                imageStrokeWidth = typedArray?.getDimension(
                    R.styleable.CometChatCollaborativeBubble_cometchatCollaborativeBubbleImageStrokeWidth, 0f
                ) ?: 0f,
                imageStrokeColor = typedArray?.getColor(
                    R.styleable.CometChatCollaborativeBubble_cometchatCollaborativeBubbleImageStrokeColor, 0
                ) ?: 0,
                imageCornerRadius = typedArray?.getDimension(
                    R.styleable.CometChatCollaborativeBubble_cometchatCollaborativeBubbleImageCornerRadius, 0f
                ) ?: 0f,

                // Common properties — READ from TypedArray with sentinel defaults
                // This allows XML-defined styles to override while falling back via resolve()
                backgroundColor = typedArray?.getColor(
                    R.styleable.CometChatCollaborativeBubble_cometchatCollaborativeBubbleBackgroundColor,
                    STYLE_NOT_SET
                ) ?: STYLE_NOT_SET,
                backgroundDrawable = typedArray?.getDrawable(
                    R.styleable.CometChatCollaborativeBubble_cometchatCollaborativeBubbleBackgroundDrawable
                ),
                cornerRadius = typedArray?.getDimension(
                    R.styleable.CometChatCollaborativeBubble_cometchatCollaborativeBubbleCornerRadius,
                    DIMENSION_NOT_SET
                ) ?: DIMENSION_NOT_SET,
                strokeWidth = typedArray?.getDimension(
                    R.styleable.CometChatCollaborativeBubble_cometchatCollaborativeBubbleStrokeWidth,
                    DIMENSION_NOT_SET
                ) ?: DIMENSION_NOT_SET,
                strokeColor = typedArray?.getColor(
                    R.styleable.CometChatCollaborativeBubble_cometchatCollaborativeBubbleStrokeColor,
                    STYLE_NOT_SET
                ) ?: STYLE_NOT_SET,
                senderNameTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatCollaborativeBubble_cometchatCollaborativeBubbleSenderNameTextAppearance,
                    STYLE_NOT_SET
                ) ?: STYLE_NOT_SET,
                senderNameTextColor = typedArray?.getColor(
                    R.styleable.CometChatCollaborativeBubble_cometchatCollaborativeBubbleSenderNameTextColor,
                    STYLE_NOT_SET
                ) ?: STYLE_NOT_SET,
                threadIndicatorTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatCollaborativeBubble_cometchatCollaborativeBubbleThreadIndicatorTextAppearance,
                    STYLE_NOT_SET
                ) ?: STYLE_NOT_SET,
                threadIndicatorTextColor = typedArray?.getColor(
                    R.styleable.CometChatCollaborativeBubble_cometchatCollaborativeBubbleThreadIndicatorTextColor,
                    STYLE_NOT_SET
                ) ?: STYLE_NOT_SET,
                threadIndicatorIconTint = typedArray?.getColor(
                    R.styleable.CometChatCollaborativeBubble_cometchatCollaborativeBubbleThreadIndicatorIconTint,
                    STYLE_NOT_SET
                ) ?: STYLE_NOT_SET,
                // Timestamp properties don't have XML attributes, keep as sentinel values
                timestampTextAppearance = STYLE_NOT_SET,
                timestampTextColor = STYLE_NOT_SET,

                // Sub-component styles — only dateStyle and messageReceiptStyle have XML attributes for CollaborativeBubble
                dateStyle = typedArray?.getResourceId(
                    R.styleable.CometChatCollaborativeBubble_cometchatCollaborativeBubbleDateStyle,
                    0
                )?.takeIf { it != 0 }?.let { CometChatDateStyle.fromStyleResource(context, it) },
                messageReceiptStyle = typedArray?.getResourceId(
                    R.styleable.CometChatCollaborativeBubble_cometchatCollaborativeBubbleMessageReceiptStyle,
                    0
                )?.takeIf { it != 0 }?.let { CometChatReceiptStyle.fromStyleResource(context, it) }
            )
        }
    }
}
