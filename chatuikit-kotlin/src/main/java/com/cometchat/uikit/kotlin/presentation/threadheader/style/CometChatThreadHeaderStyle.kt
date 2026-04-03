package com.cometchat.uikit.kotlin.presentation.threadheader.style

import android.content.Context
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.StyleRes
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.CometChatMessageBubbleStyle
import com.cometchat.uikit.kotlin.theme.CometChatTheme

/**
 * Style configuration for CometChatThreadHeader component.
 *
 * This data class defines all styling properties for the thread header component,
 * allowing customization of:
 * - Container appearance (background, stroke, corner radius)
 * - Reply count text styling (color, appearance, background)
 * - Message bubble styles (incoming/outgoing)
 *
 * ## Usage
 *
 * ### Programmatic
 * ```kotlin
 * val style = CometChatThreadHeaderStyle.default(context)
 * threadHeader.setStyle(style)
 * ```
 *
 * ### XML Attributes
 * ```xml
 * <com.cometchat.uikit.kotlin.presentation.threadheader.ui.CometChatThreadHeader
 *     app:cometchatThreadHeaderBackgroundColor="@color/white"
 *     app:cometchatThreadHeaderCornerRadius="8dp"
 *     app:cometchatThreadHeaderReplyCountTextColor="@color/textSecondary" />
 * ```
 *
 * ## Factory Functions
 *
 * - [default]: Creates a style with theme defaults
 * - [fromTypedArray]: Creates a style from XML TypedArray (handles recycling)
 *
 * @property backgroundColor Background color for the thread header container
 * @property strokeColor Border/stroke color for the container
 * @property strokeWidth Border/stroke width for the container in pixels
 * @property cornerRadius Corner radius for the container in pixels
 * @property backgroundDrawable Optional drawable for the container background
 * @property replyCountBackgroundColor Background color for the reply count bar
 * @property replyCountTextColor Text color for the reply count text
 * @property replyCountTextAppearance Text appearance resource for the reply count text
 * @property incomingMessageBubbleStyle Style object for incoming message bubbles, or null to use default
 * @property outgoingMessageBubbleStyle Style object for outgoing message bubbles, or null to use default
 *
 * @see CometChatTheme
 */
data class CometChatThreadHeaderStyle(
    // Container styling
    @ColorInt val backgroundColor: Int = 0,
    @ColorInt val strokeColor: Int = 0,
    @Dimension val strokeWidth: Int = 0,
    @Dimension val cornerRadius: Int = 0,
    val backgroundDrawable: Drawable? = null,

    // Reply count styling
    @ColorInt val replyCountBackgroundColor: Int = 0,
    @ColorInt val replyCountTextColor: Int = 0,
    @StyleRes val replyCountTextAppearance: Int = 0,

    // Message bubble styles (nested component style objects)
    val incomingMessageBubbleStyle: CometChatMessageBubbleStyle? = null,
    val outgoingMessageBubbleStyle: CometChatMessageBubbleStyle? = null
) {
    companion object {
        /**
         * Creates a default style by extracting values from the theme's cometchatThreadHeaderStyle.
         *
         * This method looks up the `cometchatThreadHeaderStyle` attribute in the current theme
         * and extracts all style properties from it. If the attribute is not defined,
         * fallback defaults from [CometChatTheme] are used.
         *
         * @param context The Android context to access theme resources.
         * @return A [CometChatThreadHeaderStyle] with values from theme or fallback defaults.
         */
        fun default(context: Context): CometChatThreadHeaderStyle {
            return extractFromThemeStyle(context)
        }

        /**
         * Extracts the thread header style from the theme's cometchatThreadHeaderStyle attribute.
         *
         * @param context The context to access theme resources
         * @return A CometChatThreadHeaderStyle with values from the theme style
         */
        private fun extractFromThemeStyle(context: Context): CometChatThreadHeaderStyle {
            val themeTypedArray = context.obtainStyledAttributes(intArrayOf(R.attr.cometchatThreadHeaderStyle))
            val styleResId = themeTypedArray.getResourceId(0, 0)
            themeTypedArray.recycle()

            return extractFromStyleResource(context, styleResId)
        }

        /**
         * Extracts style values from a specific style resource ID.
         *
         * If [styleResId] is 0, returns a style with [CometChatTheme] defaults.
         *
         * @param context The Android context to access theme resources.
         * @param styleResId The style resource ID to extract from (0 for defaults only).
         * @return A [CometChatThreadHeaderStyle] with values from the style resource or defaults.
         */
        private fun extractFromStyleResource(context: Context, styleResId: Int): CometChatThreadHeaderStyle {
            if (styleResId == 0) {
                return extractFromTypedArray(context, null)
            }
            val typedArray = context.obtainStyledAttributes(styleResId, R.styleable.CometChatThreadHeader)
            return try {
                extractFromTypedArray(context, typedArray)
            } finally {
                typedArray.recycle()
            }
        }

        /**
         * Creates a style by extracting values from an XML [TypedArray].
         *
         * This method handles [TypedArray] recycling internally using try-finally.
         * All attributes are extracted from the corresponding `attr_cometchat_thread_header.xml` file.
         *
         * Use this method when creating a style from XML attributes in a custom view constructor.
         *
         * @param context The Android context for accessing theme resources.
         * @param typedArray The [TypedArray] containing XML attribute values (will be recycled).
         * @return A [CometChatThreadHeaderStyle] with values from XML or theme defaults.
         */
        fun fromTypedArray(context: Context, typedArray: TypedArray): CometChatThreadHeaderStyle {
            return try {
                extractFromTypedArray(context, typedArray)
            } finally {
                typedArray.recycle()
            }
        }

        /**
         * Core extraction method that reads style values from a [TypedArray].
         *
         * **Note:** This method does NOT handle recycling - the caller is responsible
         * for recycling the [TypedArray].
         *
         * @param context The Android context for accessing theme resources.
         * @param typedArray The [TypedArray] to extract from, or `null` for defaults only.
         * @return A [CometChatThreadHeaderStyle] with extracted values or theme defaults.
         */
        private fun extractFromTypedArray(context: Context, typedArray: TypedArray?): CometChatThreadHeaderStyle {
            // Extract nested style resource IDs for message bubble styles
            val incomingMessageBubbleStyleResId = typedArray?.getResourceId(
                R.styleable.CometChatThreadHeader_cometchatThreadHeaderIncomingMessageBubbleStyle, 0
            ) ?: 0
            val outgoingMessageBubbleStyleResId = typedArray?.getResourceId(
                R.styleable.CometChatThreadHeader_cometchatThreadHeaderOutgoingMessageBubbleStyle, 0
            ) ?: 0

            return CometChatThreadHeaderStyle(
                // Container styling
                backgroundColor = typedArray?.getColor(
                    R.styleable.CometChatThreadHeader_cometchatThreadHeaderBackgroundColor,
                    CometChatTheme.getBackgroundColor3(context)
                ) ?: CometChatTheme.getBackgroundColor3(context),
                strokeColor = typedArray?.getColor(
                    R.styleable.CometChatThreadHeader_cometchatThreadHeaderStrokeColor,
                    0
                ) ?: 0,
                strokeWidth = typedArray?.getDimensionPixelSize(
                    R.styleable.CometChatThreadHeader_cometchatThreadHeaderStrokeWidth,
                    0
                ) ?: 0,
                cornerRadius = typedArray?.getDimensionPixelSize(
                    R.styleable.CometChatThreadHeader_cometchatThreadHeaderCornerRadius,
                    0
                ) ?: 0,
                backgroundDrawable = typedArray?.getDrawable(
                    R.styleable.CometChatThreadHeader_cometchatThreadHeaderBackgroundDrawable
                ),

                // Reply count styling
                replyCountBackgroundColor = typedArray?.getColor(
                    R.styleable.CometChatThreadHeader_cometchatThreadHeaderReplyCountBackgroundColor,
                    CometChatTheme.getExtendedPrimaryColor100(context)
                ) ?: CometChatTheme.getExtendedPrimaryColor100(context),
                replyCountTextColor = typedArray?.getColor(
                    R.styleable.CometChatThreadHeader_cometchatThreadHeaderReplyCountTextColor,
                    CometChatTheme.getTextColorSecondary(context)
                ) ?: CometChatTheme.getTextColorSecondary(context),
                replyCountTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatThreadHeader_cometchatThreadHeaderReplyCountTextAppearance,
                    CometChatTheme.getTextAppearanceCaption1Medium(context)
                ) ?: CometChatTheme.getTextAppearanceCaption1Medium(context),

                // Nested component style objects - parse XML resource IDs into style objects
                // Use fromTypedArrayIncoming/Outgoing to ensure correct default background colors
                incomingMessageBubbleStyle = if (incomingMessageBubbleStyleResId != 0) {
                    val ta = context.obtainStyledAttributes(incomingMessageBubbleStyleResId, R.styleable.CometChatMessageBubble)
                    CometChatMessageBubbleStyle.fromTypedArrayIncoming(context, ta)
                } else null,

                outgoingMessageBubbleStyle = if (outgoingMessageBubbleStyleResId != 0) {
                    val ta = context.obtainStyledAttributes(outgoingMessageBubbleStyleResId, R.styleable.CometChatMessageBubble)
                    CometChatMessageBubbleStyle.fromTypedArrayOutgoing(context, ta)
                } else null
            )
        }
    }
}
