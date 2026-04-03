package com.cometchat.uikit.kotlin.presentation.stickerbubble.style

import android.content.Context
import android.content.res.TypedArray
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.StyleRes
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.theme.CometChatTheme

/**
 * Style configuration for CometChatStickerBubble component.
 *
 * This data class holds all styling properties for the sticker bubble view,
 * including container styling, sender name styling, and thread indicator styling.
 *
 * Note: Stickers use transparent background by default, unlike other bubble types.
 *
 * @param backgroundColor Background color for the sticker bubble (transparent by default)
 * @param cornerRadius Corner radius of the bubble in pixels
 * @param strokeWidth Width of the bubble stroke/border in pixels
 * @param strokeColor Stroke/border color for the bubble
 * @param senderNameTextColor Text color for sender name
 * @param senderNameTextAppearance Text appearance resource for sender name
 * @param threadIndicatorTextColor Text color for thread indicator
 * @param threadIndicatorTextAppearance Text appearance resource for thread indicator
 * @param threadIndicatorIconTint Tint color for thread indicator icon
 */
data class CometChatStickerBubbleStyle(
    // Container
    @ColorInt val backgroundColor: Int = 0,
    @Dimension val cornerRadius: Int = 0,
    @Dimension val strokeWidth: Int = 0,
    @ColorInt val strokeColor: Int = 0,

    // Sender name styling
    @ColorInt val senderNameTextColor: Int = 0,
    @StyleRes val senderNameTextAppearance: Int = 0,

    // Thread indicator styling
    @ColorInt val threadIndicatorTextColor: Int = 0,
    @StyleRes val threadIndicatorTextAppearance: Int = 0,
    @ColorInt val threadIndicatorIconTint: Int = 0
) {
    companion object {
        /**
         * Creates a default style by extracting values from the theme's cometchatStickerBubbleStyle.
         *
         * @param context The context to access theme resources
         * @return A CometChatStickerBubbleStyle with values from theme or fallback defaults
         */
        fun default(context: Context): CometChatStickerBubbleStyle {
            return extractFromThemeStyle(context, R.attr.cometchatStickerBubbleStyle)
        }

        /**
         * Creates a style for incoming (left-aligned) sticker messages.
         *
         * Uses transparent background (stickers don't have colored backgrounds).
         *
         * @param context The context to access theme resources
         * @return A CometChatStickerBubbleStyle configured for incoming messages
         */
        fun incoming(context: Context): CometChatStickerBubbleStyle {
            return default(context)
        }

        /**
         * Creates a style for outgoing (right-aligned) sticker messages.
         *
         * Uses transparent background (stickers don't have colored backgrounds).
         *
         * @param context The context to access theme resources
         * @return A CometChatStickerBubbleStyle configured for outgoing messages
         */
        fun outgoing(context: Context): CometChatStickerBubbleStyle {
            return default(context)
        }

        private fun extractFromThemeStyle(context: Context, themeStyleAttr: Int): CometChatStickerBubbleStyle {
            val themeTypedArray = context.obtainStyledAttributes(intArrayOf(themeStyleAttr))
            val styleResId = themeTypedArray.getResourceId(0, 0)
            themeTypedArray.recycle()
            return extractFromStyleResource(context, styleResId)
        }

        private fun extractFromStyleResource(context: Context, styleResId: Int): CometChatStickerBubbleStyle {
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
         * Creates a style by extracting values from XML TypedArray.
         *
         * This method handles TypedArray recycling internally using try-finally.
         *
         * @param context The Android context for accessing theme resources
         * @param typedArray The TypedArray containing XML attribute values (will be recycled)
         * @return A CometChatStickerBubbleStyle with values from XML or theme defaults
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
            val density = context.resources.displayMetrics.density
            val defaultCornerRadius = (12 * density).toInt()

            return CometChatStickerBubbleStyle(
                // Container styling - transparent background for stickers
                backgroundColor = typedArray?.getColor(
                    R.styleable.CometChatStickerBubble_cometchatStickerBubbleBackgroundColor,
                    android.graphics.Color.TRANSPARENT
                ) ?: android.graphics.Color.TRANSPARENT,
                cornerRadius = typedArray?.getDimensionPixelSize(
                    R.styleable.CometChatStickerBubble_cometchatStickerBubbleCornerRadius,
                    defaultCornerRadius
                ) ?: defaultCornerRadius,
                strokeWidth = typedArray?.getInt(
                    R.styleable.CometChatStickerBubble_cometchatStickerBubbleStrokeWidth,
                    0
                ) ?: 0,
                strokeColor = typedArray?.getColor(
                    R.styleable.CometChatStickerBubble_cometchatStickerBubbleStrokeColor,
                    android.graphics.Color.TRANSPARENT
                ) ?: android.graphics.Color.TRANSPARENT,

                // Sender name styling
                senderNameTextColor = typedArray?.getColor(
                    R.styleable.CometChatStickerBubble_cometchatStickerBubbleSenderNameTextColor,
                    CometChatTheme.getTextColorSecondary(context)
                ) ?: CometChatTheme.getTextColorSecondary(context),
                senderNameTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatStickerBubble_cometchatStickerBubbleSenderNameTextAppearance,
                    CometChatTheme.getTextAppearanceCaption1Medium(context)
                ) ?: CometChatTheme.getTextAppearanceCaption1Medium(context),

                // Thread indicator styling
                threadIndicatorTextColor = typedArray?.getColor(
                    R.styleable.CometChatStickerBubble_cometchatStickerBubbleThreadIndicatorTextColor,
                    CometChatTheme.getTextColorSecondary(context)
                ) ?: CometChatTheme.getTextColorSecondary(context),
                threadIndicatorTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatStickerBubble_cometchatStickerBubbleThreadIndicatorTextAppearance,
                    CometChatTheme.getTextAppearanceCaption1Regular(context)
                ) ?: CometChatTheme.getTextAppearanceCaption1Regular(context),
                threadIndicatorIconTint = typedArray?.getColor(
                    R.styleable.CometChatStickerBubble_cometchatStickerBubbleThreadIndicatorIconTint,
                    CometChatTheme.getIconTintSecondary(context)
                ) ?: CometChatTheme.getIconTintSecondary(context)
            )
        }
    }
}
