package com.cometchat.uikit.kotlin.presentation.shared.messagebubble.actionbubble

import android.content.Context
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.StyleRes
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.theme.CometChatTheme

/**
 * Style configuration for CometChatActionBubble component.
 *
 * ActionBubble is a centered system message (e.g., "User joined the group").
 * Unlike directional message bubbles, action bubbles do not have incoming/outgoing variants.
 */
data class CometChatActionBubbleStyle(
    // Content-specific properties
    @ColorInt val textColor: Int = 0,
    @StyleRes val textAppearance: Int = 0,

    // Common bubble properties — use simple defaults (no style resolution/merging)
    @ColorInt val backgroundColor: Int = 0,
    val backgroundDrawable: Drawable? = null,
    @Dimension val cornerRadius: Float = 0f,
    @Dimension val strokeWidth: Float = 0f,
    @ColorInt val strokeColor: Int = 0
) {
    companion object {
        /**
         * Creates a default style with theme-appropriate values.
         *
         * @param context The context to access theme resources
         * @return A CometChatActionBubbleStyle with default values
         */
        fun default(context: Context): CometChatActionBubbleStyle {
            return extractFromStyleResource(context, 0)
        }

        private fun extractFromStyleResource(
            context: Context,
            styleResId: Int
        ): CometChatActionBubbleStyle {
            if (styleResId == 0) {
                return extractFromTypedArray(context, null)
            }
            val typedArray = context.obtainStyledAttributes(styleResId, R.styleable.CometChatActionBubble)
            return try {
                extractFromTypedArray(context, typedArray)
            } finally {
                typedArray.recycle()
            }
        }

        /**
         * Creates a [CometChatActionBubbleStyle] from a [TypedArray].
         *
         * This method extracts all properties from the TypedArray, using simple
         * default values (0, 0f, null) when XML attributes are not specified.
         * Since action bubbles don't have style resolution/merging, sentinel
         * values are not needed.
         *
         * @param context The context to access theme resources
         * @param typedArray The TypedArray containing style attributes
         * @return A CometChatActionBubbleStyle with values from the TypedArray
         */
        fun fromTypedArray(context: Context, typedArray: TypedArray): CometChatActionBubbleStyle {
            return try {
                extractFromTypedArray(context, typedArray)
            } finally {
                typedArray.recycle()
            }
        }

        private fun extractFromTypedArray(
            context: Context,
            typedArray: TypedArray?
        ): CometChatActionBubbleStyle {
            return CometChatActionBubbleStyle(
                // Content-specific properties - extracted from XML with theme defaults
                textColor = typedArray?.getColor(
                    R.styleable.CometChatActionBubble_cometchatActionBubbleTextColor,
                    CometChatTheme.getTextColorSecondary(context)
                ) ?: CometChatTheme.getTextColorSecondary(context),
                textAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatActionBubble_cometchatActionBubbleTextAppearance,
                    CometChatTheme.getTextAppearanceCaption1Regular(context)
                ) ?: CometChatTheme.getTextAppearanceCaption1Regular(context),

                // Common properties — use simple defaults (no style resolution/merging)
                // Use backgroundColor2 as the default for action bubbles per Java reference
                backgroundColor = typedArray?.getColor(
                    R.styleable.CometChatActionBubble_cometchatActionBubbleBackgroundColor,
                    CometChatTheme.getBackgroundColor2(context)
                ) ?: CometChatTheme.getBackgroundColor2(context),
                backgroundDrawable = typedArray?.getDrawable(
                    R.styleable.CometChatActionBubble_cometchatActionBubbleBackgroundDrawable
                ),
                cornerRadius = typedArray?.getDimension(
                    R.styleable.CometChatActionBubble_cometchatActionBubbleCornerRadius,
                    0f
                ) ?: 0f,
                strokeWidth = typedArray?.getDimension(
                    R.styleable.CometChatActionBubble_cometchatActionBubbleStrokeWidth,
                    0f
                ) ?: 0f,
                strokeColor = typedArray?.getColor(
                    R.styleable.CometChatActionBubble_cometchatActionBubbleStrokeColor,
                    CometChatTheme.getStrokeColorDefault(context)
                ) ?: CometChatTheme.getStrokeColorDefault(context)
            )
        }
    }
}
