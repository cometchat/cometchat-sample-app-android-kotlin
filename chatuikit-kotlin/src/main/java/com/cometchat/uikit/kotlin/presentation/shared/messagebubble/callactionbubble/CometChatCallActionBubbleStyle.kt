package com.cometchat.uikit.kotlin.presentation.shared.messagebubble.callactionbubble

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.StyleRes
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.CometChatMessageBubbleStyle
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.DIMENSION_NOT_SET
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.STYLE_NOT_SET
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.mergeWithBase
import com.cometchat.uikit.kotlin.theme.CometChatTheme

/**
 * Style configuration for CometChatCallActionBubble component.
 *
 * CallActionBubble is a centered system message. Common bubble properties use sentinel
 * values and can be resolved against a [CometChatMessageBubbleStyle] via [resolve].
 */
data class CometChatCallActionBubbleStyle(
    // Content-specific properties
    @ColorInt val textColor: Int = 0,
    @StyleRes val textAppearance: Int = 0,
    @ColorInt val iconTint: Int = 0,
    @ColorInt val missedCallTextColor: Int = 0,
    @StyleRes val missedCallTextAppearance: Int = 0,
    @ColorInt val missedCallBackgroundColor: Int = 0,
    val missedCallBackgroundDrawable: Drawable? = null,
    @ColorInt val missedCallIconTint: Int = 0,

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
    @ColorInt val timestampTextColor: Int = STYLE_NOT_SET
) {
    /**
     * Resolves this style against a [CometChatMessageBubbleStyle], using the bubble style's
     * values as fallbacks for any properties set to sentinel values.
     */
    fun resolve(messageBubbleStyle: CometChatMessageBubbleStyle): CometChatCallActionBubbleStyle =
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
         * @return A CometChatCallActionBubbleStyle with default values
         */
        fun default(context: Context): CometChatCallActionBubbleStyle {
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
         * @return A CometChatCallActionBubbleStyle configured for outgoing messages
         */
        fun outgoing(context: Context): CometChatCallActionBubbleStyle {
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
         * @return A CometChatCallActionBubbleStyle configured for incoming messages
         */
        fun incoming(context: Context): CometChatCallActionBubbleStyle {
            return extractFromMessageBubbleStyle(
                context,
                R.attr.cometchatIncomingMessageBubbleStyle
            )
        }

        private fun extractFromMessageBubbleStyle(
            context: Context,
            @Suppress("UNUSED_PARAMETER") messageBubbleStyleAttr: Int
        ): CometChatCallActionBubbleStyle {
            // CallActionBubble is a centered system message without incoming/outgoing variants.
            // The messageBubbleStyleAttr is accepted for API consistency but not used.
            // Extract from the default call action bubble style resource.
            return extractFromStyleResource(context, 0)
        }

        private fun extractFromStyleResource(
            context: Context,
            styleResId: Int
        ): CometChatCallActionBubbleStyle {
            if (styleResId == 0) {
                return extractFromTypedArray(context, null)
            }
            val typedArray = context.obtainStyledAttributes(styleResId, R.styleable.CometChatCallActionBubble)
            return try {
                extractFromTypedArray(context, typedArray)
            } finally {
                typedArray.recycle()
            }
        }

        /**
         * Creates a [CometChatCallActionBubbleStyle] from a [TypedArray].
         *
         * This method extracts all properties from the TypedArray, using sentinel
         * values for common properties when XML attributes are not specified.
         *
         * @param context The context to access theme resources
         * @param typedArray The TypedArray containing style attributes
         * @return A CometChatCallActionBubbleStyle with values from the TypedArray
         */
        fun fromTypedArray(context: Context, typedArray: TypedArray): CometChatCallActionBubbleStyle {
            return try {
                extractFromTypedArray(context, typedArray)
            } finally {
                typedArray.recycle()
            }
        }

        private fun extractFromTypedArray(
            context: Context,
            typedArray: TypedArray?
        ): CometChatCallActionBubbleStyle {
            return CometChatCallActionBubbleStyle(
                // Content-specific properties - extracted from XML with theme defaults
                textColor = typedArray?.getColor(
                    R.styleable.CometChatCallActionBubble_cometchatCallActionBubbleTextColor,
                    CometChatTheme.getTextColorSecondary(context)
                ) ?: CometChatTheme.getTextColorSecondary(context),
                textAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatCallActionBubble_cometchatCallActionBubbleTextAppearance,
                    CometChatTheme.getTextAppearanceCaption1Regular(context)
                ) ?: CometChatTheme.getTextAppearanceCaption1Regular(context),
                iconTint = typedArray?.getColor(
                    R.styleable.CometChatCallActionBubble_cometchatCallActionBubbleIconTint,
                    CometChatTheme.getIconTintSecondary(context)
                ) ?: CometChatTheme.getIconTintSecondary(context),
                missedCallTextColor = typedArray?.getColor(
                    R.styleable.CometChatCallActionBubble_cometchatCallActionBubbleMissedCallTextColor,
                    CometChatTheme.getErrorColor(context)
                ) ?: CometChatTheme.getErrorColor(context),
                missedCallTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatCallActionBubble_cometchatCallActionBubbleMissedCallTextAppearance,
                    CometChatTheme.getTextAppearanceCaption1Regular(context)
                ) ?: CometChatTheme.getTextAppearanceCaption1Regular(context),
                missedCallBackgroundColor = typedArray?.getColor(
                    R.styleable.CometChatCallActionBubble_cometchatCallActionBubbleMissedCallBackgroundColor,
                    Color.parseColor("#1AFF0000")
                ) ?: Color.parseColor("#1AFF0000"),
                missedCallBackgroundDrawable = typedArray?.getDrawable(
                    R.styleable.CometChatCallActionBubble_cometchatCallActionBubbleMissedCallBackgroundDrawable
                ),
                missedCallIconTint = typedArray?.getColor(
                    R.styleable.CometChatCallActionBubble_cometchatCallActionBubbleMissedCallIconTint,
                    CometChatTheme.getErrorColor(context)
                ) ?: CometChatTheme.getErrorColor(context),

                // Common properties — READ from TypedArray with sentinel defaults
                // This allows XML-defined styles to override while falling back via resolve()
                backgroundColor = typedArray?.getColor(
                    R.styleable.CometChatCallActionBubble_cometchatCallActionBubbleBackgroundColor,
                    STYLE_NOT_SET
                ) ?: STYLE_NOT_SET,
                backgroundDrawable = typedArray?.getDrawable(
                    R.styleable.CometChatCallActionBubble_cometchatCallActionBubbleBackgroundDrawable
                ),
                cornerRadius = typedArray?.getDimension(
                    R.styleable.CometChatCallActionBubble_cometchatCallActionBubbleCornerRadius,
                    DIMENSION_NOT_SET
                ) ?: DIMENSION_NOT_SET,
                strokeWidth = typedArray?.getDimension(
                    R.styleable.CometChatCallActionBubble_cometchatCallActionBubbleStrokeWidth,
                    DIMENSION_NOT_SET
                ) ?: DIMENSION_NOT_SET,
                strokeColor = typedArray?.getColor(
                    R.styleable.CometChatCallActionBubble_cometchatCallActionBubbleStrokeColor,
                    STYLE_NOT_SET
                ) ?: STYLE_NOT_SET,
                // SenderName, threadIndicator, and timestamp properties don't have XML attributes
                // for CallActionBubble, keep as sentinel values
                senderNameTextAppearance = STYLE_NOT_SET,
                senderNameTextColor = STYLE_NOT_SET,
                threadIndicatorTextAppearance = STYLE_NOT_SET,
                threadIndicatorTextColor = STYLE_NOT_SET,
                threadIndicatorIconTint = STYLE_NOT_SET,
                timestampTextAppearance = STYLE_NOT_SET,
                timestampTextColor = STYLE_NOT_SET
            )
        }
    }
}
