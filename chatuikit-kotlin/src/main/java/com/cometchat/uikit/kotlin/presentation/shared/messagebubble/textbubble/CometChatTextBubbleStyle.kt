package com.cometchat.uikit.kotlin.presentation.shared.messagebubble.textbubble

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
 * Style configuration for CometChatTextBubble component.
 *
 * This data class holds all styling properties for the text bubble view,
 * including text colors, background, stroke, link preview, and translation styling.
 * Common bubble properties (background, corner radius, stroke, sender name,
 * thread indicator, timestamp) use sentinel values ([STYLE_NOT_SET] or
 * [DIMENSION_NOT_SET]) to indicate "not set". When not set, they fall back to
 * values from [CometChatMessageBubbleStyle] via [resolve].
 */
data class CometChatTextBubbleStyle(
    // Content-specific properties (always defined here)
    @ColorInt val textColor: Int = 0,
    @StyleRes val textAppearance: Int = 0,
    @ColorInt val textLinkColor: Int = 0,

    // Link preview styling (content-specific)
    @StyleRes val linkPreviewTitleAppearance: Int = 0,
    @ColorInt val linkPreviewTitleColor: Int = 0,
    @StyleRes val linkPreviewDescriptionAppearance: Int = 0,
    @ColorInt val linkPreviewDescriptionColor: Int = 0,
    @StyleRes val linkPreviewLinkAppearance: Int = 0,
    @ColorInt val linkPreviewLinkColor: Int = 0,
    @ColorInt val linkPreviewBackgroundColor: Int = 0,
    val linkPreviewBackgroundDrawable: Drawable? = null,
    @ColorInt val linkPreviewStrokeColor: Int = 0,
    @Dimension val linkPreviewStrokeWidth: Float = 0f,
    @Dimension val linkPreviewCornerRadius: Float = 0f,

    // Translation styling (content-specific)
    @ColorInt val translatedTextColor: Int = 0,
    @StyleRes val translatedTextAppearance: Int = 0,
    @ColorInt val separatorColor: Int = 0,

    // Common bubble properties — use sentinel defaults to fall back to MessageBubbleStyle
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
     * @return A new [CometChatTextBubbleStyle] with all sentinel values resolved
     */
    fun resolve(messageBubbleStyle: CometChatMessageBubbleStyle): CometChatTextBubbleStyle =
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
         * @return A CometChatTextBubbleStyle with default values
         */
        fun default(context: Context): CometChatTextBubbleStyle {
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
         * @return A CometChatTextBubbleStyle configured for outgoing messages
         */
        fun outgoing(context: Context): CometChatTextBubbleStyle {
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
         * @return A CometChatTextBubbleStyle configured for incoming messages
         */
        fun incoming(context: Context): CometChatTextBubbleStyle {
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
        ): CometChatTextBubbleStyle {
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
            val textBubbleStyleResId = messageBubbleTypedArray.getResourceId(
                R.styleable.CometChatMessageBubble_cometchatTextBubbleStyle,
                0
            )
            messageBubbleTypedArray.recycle()

            return extractFromStyleResource(context, textBubbleStyleResId, isOutgoing)
        }

        private fun extractFromStyleResource(
            context: Context,
            styleResId: Int,
            isOutgoing: Boolean = false
        ): CometChatTextBubbleStyle {
            if (styleResId == 0) {
                return extractFromTypedArray(context, null, isOutgoing)
            }
            val typedArray = context.obtainStyledAttributes(styleResId, R.styleable.CometChatTextBubble)
            return try {
                extractFromTypedArray(context, typedArray, isOutgoing)
            } finally {
                typedArray.recycle()
            }
        }

        /**
         * Creates a [CometChatTextBubbleStyle] from a [TypedArray].
         *
         * This method extracts all properties from the TypedArray, using sentinel
         * values for common properties when XML attributes are not specified.
         *
         * @param context The context to access theme resources
         * @param typedArray The TypedArray containing style attributes
         * @return A CometChatTextBubbleStyle with values from the TypedArray
         */
        fun fromTypedArray(context: Context, typedArray: TypedArray): CometChatTextBubbleStyle {
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
        ): CometChatTextBubbleStyle {
            // For outgoing bubbles, use extended primary color 900 (darker shade of primary)
            // For incoming bubbles, use the theme's background color 4
            val linkPreviewBackgroundFallback = if (isOutgoing) {
                CometChatTheme.getExtendedPrimaryColor900(context)
            } else {
                CometChatTheme.getBackgroundColor4(context)
            }
            
            return CometChatTextBubbleStyle(
                // Content-specific properties - extracted from XML with theme defaults
                textColor = typedArray?.getColor(
                    R.styleable.CometChatTextBubble_cometchatTextBubbleTextColor,
                    CometChatTheme.getTextColorPrimary(context)
                ) ?: CometChatTheme.getTextColorPrimary(context),
                textAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatTextBubble_cometchatTextBubbleTextAppearance,
                    CometChatTheme.getTextAppearanceBodyRegular(context)
                ) ?: CometChatTheme.getTextAppearanceBodyRegular(context),
                textLinkColor = typedArray?.getColor(
                    R.styleable.CometChatTextBubble_cometchatTextBubbleTextLinkColor,
                    CometChatTheme.getInfoColor(context)
                ) ?: CometChatTheme.getInfoColor(context),

                // Link preview styling - extracted from XML with theme defaults
                linkPreviewTitleAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatTextBubble_cometchatTextBubbleLinkPreviewTitleAppearance,
                    CometChatTheme.getTextAppearanceBodyBold(context)
                ) ?: CometChatTheme.getTextAppearanceBodyBold(context),
                linkPreviewTitleColor = typedArray?.getColor(
                    R.styleable.CometChatTextBubble_cometchatTextBubbleLinkPreviewTitleColor,
                    CometChatTheme.getTextColorPrimary(context)
                ) ?: CometChatTheme.getTextColorPrimary(context),
                linkPreviewDescriptionAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatTextBubble_cometchatTextBubbleLinkPreviewDescriptionAppearance,
                    CometChatTheme.getTextAppearanceCaption1Regular(context)
                ) ?: CometChatTheme.getTextAppearanceCaption1Regular(context),
                linkPreviewDescriptionColor = typedArray?.getColor(
                    R.styleable.CometChatTextBubble_cometchatTextBubbleLinkPreviewDescriptionColor,
                    CometChatTheme.getTextColorSecondary(context)
                ) ?: CometChatTheme.getTextColorSecondary(context),
                linkPreviewLinkAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatTextBubble_cometchatTextBubbleLinkPreviewLinkAppearance,
                    CometChatTheme.getTextAppearanceCaption1Regular(context)
                ) ?: CometChatTheme.getTextAppearanceCaption1Regular(context),
                linkPreviewLinkColor = typedArray?.getColor(
                    R.styleable.CometChatTextBubble_cometchatTextBubbleLinkPreviewLinkColor,
                    CometChatTheme.getInfoColor(context)
                ) ?: CometChatTheme.getInfoColor(context),
                linkPreviewBackgroundColor = typedArray?.getColor(
                    R.styleable.CometChatTextBubble_cometchatTextBubbleLinkPreviewBackgroundColor,
                    linkPreviewBackgroundFallback
                ) ?: linkPreviewBackgroundFallback,
                linkPreviewBackgroundDrawable = typedArray?.getDrawable(
                    R.styleable.CometChatTextBubble_cometchatTextBubbleLinkPreviewBackgroundDrawable
                ),
                linkPreviewStrokeColor = typedArray?.getColor(
                    R.styleable.CometChatTextBubble_cometchatTextBubbleLinkPreviewStrokeColor,
                    0
                ) ?: 0,
                linkPreviewStrokeWidth = typedArray?.getDimension(
                    R.styleable.CometChatTextBubble_cometchatTextBubbleLinkPreviewStrokeWidth,
                    0f
                ) ?: 0f,
                linkPreviewCornerRadius = typedArray?.getDimension(
                    R.styleable.CometChatTextBubble_cometchatTextBubbleLinkPreviewCornerRadius,
                    0f
                ) ?: 0f,

                // Translation styling - extracted from XML with theme defaults
                translatedTextColor = typedArray?.getColor(
                    R.styleable.CometChatTextBubble_cometchatTextBubbleTranslatedTextColor,
                    CometChatTheme.getTextColorSecondary(context)
                ) ?: CometChatTheme.getTextColorSecondary(context),
                translatedTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatTextBubble_cometchatTextBubbleTranslatedTextAppearance,
                    CometChatTheme.getTextAppearanceBodyRegular(context)
                ) ?: CometChatTheme.getTextAppearanceBodyRegular(context),
                separatorColor = typedArray?.getColor(
                    R.styleable.CometChatTextBubble_cometchatTextBubbleSeparatorColor,
                    CometChatTheme.getStrokeColorLight(context)
                ) ?: CometChatTheme.getStrokeColorLight(context),

                // Common properties — READ from TypedArray with sentinel defaults
                // This allows XML-defined styles to override while falling back via resolve()
                backgroundColor = typedArray?.getColor(
                    R.styleable.CometChatTextBubble_cometchatTextBubbleBackgroundColor,
                    STYLE_NOT_SET
                ) ?: STYLE_NOT_SET,
                backgroundDrawable = typedArray?.getDrawable(
                    R.styleable.CometChatTextBubble_cometchatTextBubbleBackgroundDrawable
                ),
                cornerRadius = typedArray?.getDimension(
                    R.styleable.CometChatTextBubble_cometchatTextBubbleCornerRadius,
                    DIMENSION_NOT_SET
                ) ?: DIMENSION_NOT_SET,
                strokeWidth = typedArray?.getDimension(
                    R.styleable.CometChatTextBubble_cometchatTextBubbleStrokeWidth,
                    DIMENSION_NOT_SET
                ) ?: DIMENSION_NOT_SET,
                strokeColor = typedArray?.getColor(
                    R.styleable.CometChatTextBubble_cometchatTextBubbleStrokeColor,
                    STYLE_NOT_SET
                ) ?: STYLE_NOT_SET,
                senderNameTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatTextBubble_cometchatTextBubbleSenderNameTextAppearance,
                    STYLE_NOT_SET
                ) ?: STYLE_NOT_SET,
                senderNameTextColor = typedArray?.getColor(
                    R.styleable.CometChatTextBubble_cometchatTextBubbleSenderNameTextColor,
                    STYLE_NOT_SET
                ) ?: STYLE_NOT_SET,
                threadIndicatorTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatTextBubble_cometchatTextBubbleThreadIndicatorTextAppearance,
                    STYLE_NOT_SET
                ) ?: STYLE_NOT_SET,
                threadIndicatorTextColor = typedArray?.getColor(
                    R.styleable.CometChatTextBubble_cometchatTextBubbleThreadIndicatorTextColor,
                    STYLE_NOT_SET
                ) ?: STYLE_NOT_SET,
                threadIndicatorIconTint = typedArray?.getColor(
                    R.styleable.CometChatTextBubble_cometchatTextBubbleThreadIndicatorIconTint,
                    STYLE_NOT_SET
                ) ?: STYLE_NOT_SET,
                // Timestamp properties don't have XML attributes, keep as sentinel values
                timestampTextAppearance = STYLE_NOT_SET,
                timestampTextColor = STYLE_NOT_SET,

                // Sub-component styles — read from TypedArray if specified, otherwise null (inherited via resolve())
                dateStyle = typedArray?.getResourceId(
                    R.styleable.CometChatTextBubble_cometchatTextBubbleDateStyle,
                    0
                )?.takeIf { it != 0 }?.let { CometChatDateStyle.fromStyleResource(context, it) },
                messageReceiptStyle = typedArray?.getResourceId(
                    R.styleable.CometChatTextBubble_cometchatTextBubbleMessageReceiptStyle,
                    0
                )?.takeIf { it != 0 }?.let { CometChatReceiptStyle.fromStyleResource(context, it) }
            )
        }
    }
}
