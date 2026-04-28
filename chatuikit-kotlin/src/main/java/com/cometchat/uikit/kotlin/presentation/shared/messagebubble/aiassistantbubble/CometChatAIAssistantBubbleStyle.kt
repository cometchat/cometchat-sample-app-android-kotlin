package com.cometchat.uikit.kotlin.presentation.shared.messagebubble.aiassistantbubble

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
 * Style configuration for CometChatAIAssistantBubble component.
 *
 * This data class holds all styling properties for the AI assistant bubble,
 * matching the XML attributes defined in att_cometchat_ai_assistant_bubble.xml.
 * Common bubble properties (background, corner radius, stroke, sender name,
 * thread indicator) use sentinel values ([STYLE_NOT_SET] or [DIMENSION_NOT_SET])
 * to indicate "not set". When not set, they fall back to values from
 * [CometChatMessageBubbleStyle] via [resolve].
 */
data class CometChatAIAssistantBubbleStyle(
    // Content-specific properties
    @ColorInt val textColor: Int = 0,
    @StyleRes val textAppearance: Int = 0,
    @StyleRes val avatarStyleRes: Int = 0,

    // Shimmer / thinking state
    @ColorInt val shimmerTextColor: Int = 0,
    @StyleRes val shimmerTextAppearance: Int = 0,

    // Error state
    @ColorInt val errorBackgroundColor: Int = 0,
    @ColorInt val errorTextColor: Int = 0,
    @StyleRes val errorTextAppearance: Int = 0,
    @ColorInt val errorIconTint: Int = 0,

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

    // Sub-component styles
    val dateStyle: CometChatDateStyle? = null,
    val messageReceiptStyle: CometChatReceiptStyle? = null
) {
    /**
     * Resolves this style against a [CometChatMessageBubbleStyle], using the bubble style's
     * values as fallbacks for any properties set to sentinel values.
     *
     * @param messageBubbleStyle The message bubble style to use as fallback
     * @return A new [CometChatAIAssistantBubbleStyle] with all sentinel values resolved
     */
    fun resolve(messageBubbleStyle: CometChatMessageBubbleStyle): CometChatAIAssistantBubbleStyle =
        mergeWithBase(this, messageBubbleStyle)

    companion object {
        /**
         * Creates a default style (delegates to incoming since AI assistant messages
         * are always incoming).
         *
         * Common bubble properties (backgroundColor, cornerRadius, etc.) are set
         * to sentinel values and should be resolved via [resolve] against a
         * [CometChatMessageBubbleStyle] at runtime.
         *
         * @param context The context to access theme resources
         * @return A CometChatAIAssistantBubbleStyle with default values
         */
        fun default(context: Context): CometChatAIAssistantBubbleStyle {
            return incoming(context)
        }

        /**
         * Creates a style for incoming messages by extracting content-specific
         * values from the theme's incoming message bubble style.
         *
         * AI assistant messages are always incoming, so there is no outgoing() variant.
         *
         * Common bubble properties (backgroundColor, cornerRadius, etc.) are set
         * to sentinel values and should be resolved via [resolve] against a
         * [CometChatMessageBubbleStyle] at runtime.
         *
         * @param context The context to access theme resources
         * @return A CometChatAIAssistantBubbleStyle configured for incoming messages
         */
        fun incoming(context: Context): CometChatAIAssistantBubbleStyle {
            return extractFromMessageBubbleStyle(context, R.attr.cometchatIncomingMessageBubbleStyle)
        }

        private fun extractFromMessageBubbleStyle(
            context: Context,
            messageBubbleStyleAttr: Int
        ): CometChatAIAssistantBubbleStyle {
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
            val aiAssistantBubbleStyleResId = messageBubbleTypedArray.getResourceId(
                R.styleable.CometChatMessageBubble_cometchatAIAssistantBubbleStyle,
                0
            )
            messageBubbleTypedArray.recycle()

            return extractFromStyleResource(context, aiAssistantBubbleStyleResId)
        }

        private fun extractFromStyleResource(
            context: Context,
            styleResId: Int
        ): CometChatAIAssistantBubbleStyle {
            if (styleResId == 0) {
                return extractFromTypedArray(context, null)
            }
            val typedArray = context.obtainStyledAttributes(
                styleResId,
                R.styleable.CometChatAIAssistantBubble
            )
            return try {
                extractFromTypedArray(context, typedArray)
            } finally {
                typedArray.recycle()
            }
        }

        /**
         * Creates a [CometChatAIAssistantBubbleStyle] from a [TypedArray].
         *
         * This method extracts all properties from the TypedArray, using sentinel
         * values for common properties when XML attributes are not specified.
         *
         * @param context The context to access theme resources
         * @param typedArray The TypedArray containing style attributes
         * @return A CometChatAIAssistantBubbleStyle with values from the TypedArray
         */
        fun fromTypedArray(context: Context, typedArray: TypedArray): CometChatAIAssistantBubbleStyle {
            return try {
                extractFromTypedArray(context, typedArray)
            } finally {
                typedArray.recycle()
            }
        }

        private fun extractFromTypedArray(
            context: Context,
            typedArray: TypedArray?
        ): CometChatAIAssistantBubbleStyle {
            return CometChatAIAssistantBubbleStyle(
                // Content-specific properties - extracted from XML with theme defaults
                textColor = typedArray?.getColor(
                    R.styleable.CometChatAIAssistantBubble_cometChatAIAssistantBubbleTextColor,
                    CometChatTheme.getTextColorPrimary(context)
                ) ?: CometChatTheme.getTextColorPrimary(context),
                textAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatAIAssistantBubble_cometChatAIAssistantBubbleTextAppearance,
                    CometChatTheme.getTextAppearanceBodyRegular(context)
                ) ?: CometChatTheme.getTextAppearanceBodyRegular(context),
                avatarStyleRes = typedArray?.getResourceId(
                    R.styleable.CometChatAIAssistantBubble_cometchatAIAssistantBubbleAvatarStyle,
                    0
                ) ?: 0,

                // Shimmer / thinking state
                shimmerTextColor = typedArray?.getColor(
                    R.styleable.CometChatAIAssistantBubble_cometchatAIAssistantBubbleShimmerTextColor,
                    CometChatTheme.getTextColorSecondary(context)
                ) ?: CometChatTheme.getTextColorSecondary(context),
                shimmerTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatAIAssistantBubble_cometchatAIAssistantBubbleShimmerTextAppearance,
                    CometChatTheme.getTextAppearanceBodyRegular(context)
                ) ?: CometChatTheme.getTextAppearanceBodyRegular(context),

                // Error state
                errorBackgroundColor = typedArray?.getColor(
                    R.styleable.CometChatAIAssistantBubble_cometchatAIAssistantBubbleErrorBackgroundColor,
                    CometChatTheme.getBackgroundColor3(context)
                ) ?: CometChatTheme.getBackgroundColor3(context),
                errorTextColor = typedArray?.getColor(
                    R.styleable.CometChatAIAssistantBubble_cometchatAIAssistantBubbleErrorTextColor,
                    CometChatTheme.getErrorColor(context)
                ) ?: CometChatTheme.getErrorColor(context),
                errorTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatAIAssistantBubble_cometchatAIAssistantBubbleErrorTextAppearance,
                    CometChatTheme.getTextAppearanceCaption1Regular(context)
                ) ?: CometChatTheme.getTextAppearanceCaption1Regular(context),
                errorIconTint = typedArray?.getColor(
                    R.styleable.CometChatAIAssistantBubble_cometchatAIAssistantBubbleErrorIconTint,
                    CometChatTheme.getErrorColor(context)
                ) ?: CometChatTheme.getErrorColor(context),

                // Common properties — READ from TypedArray with sentinel defaults
                // This allows XML-defined styles to override while falling back via resolve()
                backgroundColor = typedArray?.getColor(
                    R.styleable.CometChatAIAssistantBubble_cometChatAIAssistantBubbleBackgroundColor,
                    STYLE_NOT_SET
                ) ?: STYLE_NOT_SET,
                backgroundDrawable = typedArray?.getDrawable(
                    R.styleable.CometChatAIAssistantBubble_cometchatAIAssistantBubbleBackgroundDrawable
                ),
                cornerRadius = typedArray?.getDimension(
                    R.styleable.CometChatAIAssistantBubble_cometchatAIAssistantBubbleCornerRadius,
                    DIMENSION_NOT_SET
                ) ?: DIMENSION_NOT_SET,
                strokeWidth = typedArray?.getDimension(
                    R.styleable.CometChatAIAssistantBubble_cometchatAIAssistantBubbleStrokeWidth,
                    DIMENSION_NOT_SET
                ) ?: DIMENSION_NOT_SET,
                strokeColor = typedArray?.getColor(
                    R.styleable.CometChatAIAssistantBubble_cometchatAIAssistantBubbleStrokeColor,
                    STYLE_NOT_SET
                ) ?: STYLE_NOT_SET,
                senderNameTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatAIAssistantBubble_cometchatAIAssistantBubbleSenderNameTextAppearance,
                    STYLE_NOT_SET
                ) ?: STYLE_NOT_SET,
                senderNameTextColor = typedArray?.getColor(
                    R.styleable.CometChatAIAssistantBubble_cometchatAIAssistantBubbleSenderNameTextColor,
                    STYLE_NOT_SET
                ) ?: STYLE_NOT_SET,
                threadIndicatorTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatAIAssistantBubble_cometchatAIAssistantBubbleThreadIndicatorTextAppearance,
                    STYLE_NOT_SET
                ) ?: STYLE_NOT_SET,
                threadIndicatorTextColor = typedArray?.getColor(
                    R.styleable.CometChatAIAssistantBubble_cometchatAIAssistantBubbleThreadIndicatorTextColor,
                    STYLE_NOT_SET
                ) ?: STYLE_NOT_SET,
                threadIndicatorIconTint = typedArray?.getColor(
                    R.styleable.CometChatAIAssistantBubble_cometchatAIAssistantBubbleThreadIndicatorIconTint,
                    STYLE_NOT_SET
                ) ?: STYLE_NOT_SET,

                // Sub-component styles — read from TypedArray if specified, otherwise null (inherited via resolve())
                dateStyle = typedArray?.getResourceId(
                    R.styleable.CometChatAIAssistantBubble_cometchatAIAssistantBubbleDateStyle,
                    0
                )?.takeIf { it != 0 }?.let { CometChatDateStyle.fromStyleResource(context, it) },
                messageReceiptStyle = typedArray?.getResourceId(
                    R.styleable.CometChatAIAssistantBubble_cometchatAIAssistantBubbleMessageReceiptStyle,
                    0
                )?.takeIf { it != 0 }?.let { CometChatReceiptStyle.fromStyleResource(context, it) }
            )
        }
    }
}
