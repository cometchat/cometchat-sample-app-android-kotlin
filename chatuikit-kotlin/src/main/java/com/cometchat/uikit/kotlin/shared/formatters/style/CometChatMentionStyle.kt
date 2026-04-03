package com.cometchat.uikit.kotlin.shared.formatters.style

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.Typeface
import androidx.annotation.ColorInt
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.theme.CometChatTheme

/**
 * Style configuration for @mentions in CometChat UI components.
 *
 * This class provides unified styling for mentions across different contexts
 * including message bubbles, conversation previews, and message composer. It supports
 * distinct styling for self-mentions (when the logged-in user is mentioned) and
 * other-user mentions.
 *
 * @param textColor Text color for other-user mentions
 * @param textStyle Typography style for other-user mentions
 * @param backgroundColor Background color for other-user mentions
 * @param selfTextColor Text color for self-mentions (when the logged-in user is mentioned)
 * @param selfTextStyle Typography style for self-mentions
 * @param selfBackgroundColor Background color for self-mentions
 */
data class CometChatMentionStyle(
    @ColorInt val textColor: Int,
    val textStyle: Typeface?,
    @ColorInt val backgroundColor: Int,
    @ColorInt val selfTextColor: Int,
    val selfTextStyle: Typeface?,
    @ColorInt val selfBackgroundColor: Int
) {
    companion object {
        /**
         * Alpha value for default translucent background colors (51/255 ≈ 0.2 or 20% opacity)
         */
        private const val DEFAULT_BACKGROUND_ALPHA = 51

        /**
         * Creates a default CometChatMentionStyle with values sourced from CometChatTheme.
         *
         * By default, background colors are set to translucent versions of the text colors
         * with 20% opacity (alpha 51).
         *
         * @param context The Android context for accessing theme resources
         * @param textColor Text color for other-user mentions. Defaults to theme's highlight color.
         * @param textStyle Typography for other-user mentions. Defaults to null (system default).
         * @param backgroundColor Background color for other-user mentions. Defaults to textColor with 20% opacity.
         * @param selfTextColor Text color for self-mentions. Defaults to theme's warning color.
         * @param selfTextStyle Typography for self-mentions. Defaults to null (system default).
         * @param selfBackgroundColor Background color for self-mentions. Defaults to selfTextColor with 20% opacity.
         * @return A new CometChatMentionStyle instance with theme-based default values
         */
        fun default(
            context: Context,
            @ColorInt textColor: Int = CometChatTheme.getTextColorHighlight(context),
            textStyle: Typeface? = null,
            @ColorInt backgroundColor: Int = applyAlpha(textColor, DEFAULT_BACKGROUND_ALPHA),
            @ColorInt selfTextColor: Int = CometChatTheme.getWarningColor(context),
            selfTextStyle: Typeface? = null,
            @ColorInt selfBackgroundColor: Int = applyAlpha(selfTextColor, DEFAULT_BACKGROUND_ALPHA)
        ): CometChatMentionStyle = CometChatMentionStyle(
            textColor = textColor,
            textStyle = textStyle,
            backgroundColor = backgroundColor,
            selfTextColor = selfTextColor,
            selfTextStyle = selfTextStyle,
            selfBackgroundColor = selfBackgroundColor
        )

        /**
         * Applies alpha to a color.
         *
         * @param color The original color
         * @param alpha The alpha value (0-255)
         * @return The color with applied alpha
         */
        private fun applyAlpha(@ColorInt color: Int, alpha: Int): Int {
            return Color.argb(
                alpha,
                Color.red(color),
                Color.green(color),
                Color.blue(color)
            )
        }

        /**
         * Creates a style by extracting values from XML TypedArray.
         *
         * @param context The Android context for accessing theme resources
         * @param typedArray The TypedArray containing XML attribute values (will be recycled)
         * @return A CometChatMentionStyle with values from XML or theme defaults
         */
        fun fromTypedArray(context: Context, typedArray: TypedArray): CometChatMentionStyle {
            return try {
                val defaultTextColor = CometChatTheme.getTextColorHighlight(context)
                val defaultSelfTextColor = CometChatTheme.getWarningColor(context)
                
                val textColor = typedArray.getColor(
                    R.styleable.CometChatMentionStyle_cometchatMentionTextColor,
                    defaultTextColor
                )
                val backgroundColor = typedArray.getColor(
                    R.styleable.CometChatMentionStyle_cometchatMentionBackgroundColor,
                    applyAlpha(textColor, DEFAULT_BACKGROUND_ALPHA)
                )
                val selfTextColor = typedArray.getColor(
                    R.styleable.CometChatMentionStyle_cometchatSelfMentionTextColor,
                    defaultSelfTextColor
                )
                val selfBackgroundColor = typedArray.getColor(
                    R.styleable.CometChatMentionStyle_cometchatSelfMentionBackgroundColor,
                    applyAlpha(selfTextColor, DEFAULT_BACKGROUND_ALPHA)
                )
                
                CometChatMentionStyle(
                    textColor = textColor,
                    textStyle = null,
                    backgroundColor = backgroundColor,
                    selfTextColor = selfTextColor,
                    selfTextStyle = null,
                    selfBackgroundColor = selfBackgroundColor
                )
            } finally {
                typedArray.recycle()
            }
        }
    }

    /**
     * Converts this style to a [PromptTextStyle] for other-user mentions.
     *
     * This function creates a PromptTextStyle configured with textColor and backgroundColor
     * for use with the formatter infrastructure.
     *
     * @return A PromptTextStyle configured with textColor and backgroundColor
     */
    fun toPromptTextStyle(): PromptTextStyle {
        return PromptTextStyle()
            .setColor(textColor)
            .setBackgroundColor(backgroundColor)
            .apply { textStyle?.let { setTextAppearance(it) } }
    }

    /**
     * Converts this style to a [PromptTextStyle] for self-mentions.
     *
     * This function creates a PromptTextStyle configured with selfTextColor and selfBackgroundColor
     * for use with the formatter infrastructure.
     *
     * @return A PromptTextStyle configured with selfTextColor and selfBackgroundColor
     */
    fun toSelfPromptTextStyle(): PromptTextStyle {
        return PromptTextStyle()
            .setColor(selfTextColor)
            .setBackgroundColor(selfBackgroundColor)
            .apply { selfTextStyle?.let { setTextAppearance(it) } }
    }
}
