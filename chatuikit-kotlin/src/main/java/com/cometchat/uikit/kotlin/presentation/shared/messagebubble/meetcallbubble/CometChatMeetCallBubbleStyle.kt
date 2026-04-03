package com.cometchat.uikit.kotlin.presentation.shared.messagebubble.meetcallbubble

import android.content.Context
import android.content.res.TypedArray
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
 * Style configuration for CometChatMeetCallBubble component.
 *
 * This data class holds all styling properties for the meet/call invitation message bubble view.
 * Common bubble properties use sentinel values and can be resolved against
 * a [CometChatMessageBubbleStyle] via [resolve].
 */
data class CometChatMeetCallBubbleStyle(
    // Content-specific properties - Call icons
    val incomingVoiceCallIcon: Drawable? = null,
    val incomingVideoCallIcon: Drawable? = null,
    val outgoingVoiceCallIcon: Drawable? = null,
    val outgoingVideoCallIcon: Drawable? = null,
    @ColorInt val callIconTint: Int = 0,
    @ColorInt val iconBackgroundColor: Int = 0,

    // Content-specific properties - Title and subtitle
    @StyleRes val titleTextAppearance: Int = 0,
    @ColorInt val titleTextColor: Int = 0,
    @StyleRes val subtitleTextAppearance: Int = 0,
    @ColorInt val subtitleTextColor: Int = 0,

    // Content-specific properties - Separator and button
    @ColorInt val separatorColor: Int = 0,
    @StyleRes val joinButtonTextAppearance: Int = 0,
    @ColorInt val joinButtonTextColor: Int = 0,

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
    // Note: MeetCallBubble has NO sub-component style XML attributes
) {
    /**
     * Resolves this style against a [CometChatMessageBubbleStyle], using the bubble style's
     * values as fallbacks for any properties set to sentinel values.
     */
    fun resolve(messageBubbleStyle: CometChatMessageBubbleStyle): CometChatMeetCallBubbleStyle =
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
         * @return A CometChatMeetCallBubbleStyle with default values
         */
        fun default(context: Context): CometChatMeetCallBubbleStyle {
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
         * @return A CometChatMeetCallBubbleStyle configured for outgoing messages
         */
        fun outgoing(context: Context): CometChatMeetCallBubbleStyle {
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
         * @return A CometChatMeetCallBubbleStyle configured for incoming messages
         */
        fun incoming(context: Context): CometChatMeetCallBubbleStyle {
            return extractFromMessageBubbleStyle(
                context,
                R.attr.cometchatIncomingMessageBubbleStyle
            )
        }

        private fun extractFromMessageBubbleStyle(
            context: Context,
            messageBubbleStyleAttr: Int
        ): CometChatMeetCallBubbleStyle {
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
            val meetCallBubbleStyleResId = messageBubbleTypedArray.getResourceId(
                R.styleable.CometChatMessageBubble_cometchatMeetCallBubbleStyle,
                0
            )
            messageBubbleTypedArray.recycle()

            return extractFromStyleResource(context, meetCallBubbleStyleResId)
        }

        private fun extractFromStyleResource(
            context: Context,
            styleResId: Int
        ): CometChatMeetCallBubbleStyle {
            if (styleResId == 0) {
                return extractFromTypedArray(context, null)
            }
            val typedArray = context.obtainStyledAttributes(styleResId, R.styleable.CometChatMeetCallBubble)
            return try {
                extractFromTypedArray(context, typedArray)
            } finally {
                typedArray.recycle()
            }
        }

        /**
         * Creates a [CometChatMeetCallBubbleStyle] from a [TypedArray].
         *
         * This method extracts all properties from the TypedArray, using sentinel
         * values for common properties when XML attributes are not specified.
         *
         * @param context The context to access theme resources
         * @param typedArray The TypedArray containing style attributes
         * @return A CometChatMeetCallBubbleStyle with values from the TypedArray
         */
        fun fromTypedArray(context: Context, typedArray: TypedArray): CometChatMeetCallBubbleStyle {
            return try {
                extractFromTypedArray(context, typedArray)
            } finally {
                typedArray.recycle()
            }
        }

        private fun extractFromTypedArray(
            context: Context,
            typedArray: TypedArray?
        ): CometChatMeetCallBubbleStyle {
            return CometChatMeetCallBubbleStyle(
                // Content-specific properties - Call icons
                incomingVoiceCallIcon = typedArray?.getDrawable(
                    R.styleable.CometChatMeetCallBubble_cometchatMeetCallBubbleIncomingVoiceCallIcon
                ),
                incomingVideoCallIcon = typedArray?.getDrawable(
                    R.styleable.CometChatMeetCallBubble_cometchatMeetCallBubbleIncomingVideoCallIcon
                ),
                outgoingVoiceCallIcon = typedArray?.getDrawable(
                    R.styleable.CometChatMeetCallBubble_cometchatMeetCallBubbleOutgoingVoiceCallIcon
                ),
                outgoingVideoCallIcon = typedArray?.getDrawable(
                    R.styleable.CometChatMeetCallBubble_cometchatMeetCallBubbleOutgoingVideoCallIcon
                ),
                callIconTint = typedArray?.getColor(
                    R.styleable.CometChatMeetCallBubble_cometchatMeetCallBubbleCallIconTint,
                    CometChatTheme.getPrimaryColor(context)
                ) ?: CometChatTheme.getPrimaryColor(context),
                iconBackgroundColor = typedArray?.getColor(
                    R.styleable.CometChatMeetCallBubble_cometchatMeetCallBubbleIconBackgroundColor,
                    CometChatTheme.getColorWhite(context)
                ) ?: CometChatTheme.getColorWhite(context),

                // Content-specific properties - Title and subtitle styling
                titleTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatMeetCallBubble_cometchatMeetCallBubbleTitleTextAppearance,
                    CometChatTheme.getTextAppearanceBodyMedium(context)
                ) ?: CometChatTheme.getTextAppearanceBodyMedium(context),
                titleTextColor = typedArray?.getColor(
                    R.styleable.CometChatMeetCallBubble_cometchatMeetCallBubbleTitleTextColor,
                    CometChatTheme.getTextColorPrimary(context)
                ) ?: CometChatTheme.getTextColorPrimary(context),
                subtitleTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatMeetCallBubble_cometchatMeetCallBubbleSubtitleTextAppearance,
                    CometChatTheme.getTextAppearanceCaption1Regular(context)
                ) ?: CometChatTheme.getTextAppearanceCaption1Regular(context),
                subtitleTextColor = typedArray?.getColor(
                    R.styleable.CometChatMeetCallBubble_cometchatMeetCallBubbleSubtitleTextColor,
                    CometChatTheme.getTextColorSecondary(context)
                ) ?: CometChatTheme.getTextColorSecondary(context),

                // Content-specific properties - Separator and button styling
                separatorColor = typedArray?.getColor(
                    R.styleable.CometChatMeetCallBubble_cometchatMeetCallBubbleSeparatorColor,
                    CometChatTheme.getExtendedPrimaryColor800(context)
                ) ?: CometChatTheme.getExtendedPrimaryColor800(context),
                joinButtonTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatMeetCallBubble_cometchatMeetCallBubbleJoinButtonTextAppearance,
                    CometChatTheme.getTextAppearanceButtonMedium(context)
                ) ?: CometChatTheme.getTextAppearanceButtonMedium(context),
                joinButtonTextColor = typedArray?.getColor(
                    R.styleable.CometChatMeetCallBubble_cometchatMeetCallBubbleJoinButtonTextColor,
                    CometChatTheme.getPrimaryColor(context)
                ) ?: CometChatTheme.getPrimaryColor(context),

                // Common properties — READ from TypedArray with sentinel defaults
                // This allows XML-defined styles to override while falling back via resolve()
                backgroundColor = typedArray?.getColor(
                    R.styleable.CometChatMeetCallBubble_cometchatMeetCallBubbleBackgroundColor,
                    STYLE_NOT_SET
                ) ?: STYLE_NOT_SET,
                backgroundDrawable = typedArray?.getDrawable(
                    R.styleable.CometChatMeetCallBubble_cometchatMeetCallBubbleBackgroundDrawable
                ),
                cornerRadius = typedArray?.getDimension(
                    R.styleable.CometChatMeetCallBubble_cometchatMeetCallBubbleCornerRadius,
                    DIMENSION_NOT_SET
                ) ?: DIMENSION_NOT_SET,
                strokeWidth = typedArray?.getDimension(
                    R.styleable.CometChatMeetCallBubble_cometchatMeetCallBubbleStrokeWidth,
                    DIMENSION_NOT_SET
                ) ?: DIMENSION_NOT_SET,
                strokeColor = typedArray?.getColor(
                    R.styleable.CometChatMeetCallBubble_cometchatMeetCallBubbleStrokeColor,
                    STYLE_NOT_SET
                ) ?: STYLE_NOT_SET,
                senderNameTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatMeetCallBubble_cometchatMeetCallBubbleSenderNameTextAppearance,
                    STYLE_NOT_SET
                ) ?: STYLE_NOT_SET,
                senderNameTextColor = typedArray?.getColor(
                    R.styleable.CometChatMeetCallBubble_cometchatMeetCallBubbleSenderNameTextColor,
                    STYLE_NOT_SET
                ) ?: STYLE_NOT_SET,
                threadIndicatorTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatMeetCallBubble_cometchatMeetCallBubbleThreadIndicatorTextAppearance,
                    STYLE_NOT_SET
                ) ?: STYLE_NOT_SET,
                threadIndicatorTextColor = typedArray?.getColor(
                    R.styleable.CometChatMeetCallBubble_cometchatMeetCallBubbleThreadIndicatorTextColor,
                    STYLE_NOT_SET
                ) ?: STYLE_NOT_SET,
                threadIndicatorIconTint = typedArray?.getColor(
                    R.styleable.CometChatMeetCallBubble_cometchatMeetCallBubbleThreadIndicatorIconTint,
                    STYLE_NOT_SET
                ) ?: STYLE_NOT_SET,
                // Timestamp properties don't have XML attributes, keep as sentinel values
                timestampTextAppearance = STYLE_NOT_SET,
                timestampTextColor = STYLE_NOT_SET
                // Note: MeetCallBubble has NO sub-component style XML attributes
            )
        }
    }
}
