package com.cometchat.uikit.kotlin.presentation.shared.messagebubble.aiassistantbubble

import android.content.Context
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.StyleRes
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.theme.CometChatTheme

/**
 * Style configuration for CometChatAIAssistantBubble component.
 *
 * This data class holds all styling properties for the AI assistant bubble,
 * matching the XML attributes defined in att_cometchat_ai_assistant_bubble.xml.
 */
data class CometChatAIAssistantBubbleStyle(
    @ColorInt val backgroundColor: Int = 0,
    val backgroundDrawable: Drawable? = null,
    @ColorInt val textColor: Int = 0,
    @StyleRes val textAppearance: Int = 0,
    @Dimension val cornerRadius: Float = 0f,
    @Dimension val strokeWidth: Float = 0f,
    @ColorInt val strokeColor: Int = 0,
    @StyleRes val avatarStyleRes: Int = 0
) {
    companion object {
        /**
         * Creates a default style with CometChatTheme defaults.
         */
        fun default(context: Context): CometChatAIAssistantBubbleStyle {
            return CometChatAIAssistantBubbleStyle(
                backgroundColor = CometChatTheme.getBackgroundColor3(context),
                backgroundDrawable = null,
                textColor = CometChatTheme.getTextColorPrimary(context),
                textAppearance = CometChatTheme.getTextAppearanceBodyRegular(context),
                cornerRadius = 0f,
                strokeWidth = 0f,
                strokeColor = 0,
                avatarStyleRes = 0
            )
        }

        /**
         * Creates a style by extracting values from XML TypedArray.
         */
        fun fromTypedArray(context: Context, typedArray: TypedArray): CometChatAIAssistantBubbleStyle {
            return try {
                extractFromTypedArray(context, typedArray)
            } finally {
                typedArray.recycle()
            }
        }

        private fun extractFromTypedArray(context: Context, typedArray: TypedArray?): CometChatAIAssistantBubbleStyle {
            return CometChatAIAssistantBubbleStyle(
                backgroundColor = typedArray?.getColor(
                    R.styleable.CometChatAIAssistantBubble_cometChatAIAssistantBubbleBackgroundColor,
                    CometChatTheme.getBackgroundColor3(context)
                ) ?: CometChatTheme.getBackgroundColor3(context),
                backgroundDrawable = typedArray?.getDrawable(
                    R.styleable.CometChatAIAssistantBubble_cometchatAIAssistantBubbleBackgroundDrawable
                ),
                textColor = typedArray?.getColor(
                    R.styleable.CometChatAIAssistantBubble_cometChatAIAssistantBubbleTextColor,
                    CometChatTheme.getTextColorPrimary(context)
                ) ?: CometChatTheme.getTextColorPrimary(context),
                textAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatAIAssistantBubble_cometChatAIAssistantBubbleTextAppearance,
                    CometChatTheme.getTextAppearanceBodyRegular(context)
                ) ?: CometChatTheme.getTextAppearanceBodyRegular(context),
                cornerRadius = typedArray?.getDimension(
                    R.styleable.CometChatAIAssistantBubble_cometchatAIAssistantBubbleCornerRadius, 0f
                ) ?: 0f,
                strokeWidth = typedArray?.getDimension(
                    R.styleable.CometChatAIAssistantBubble_cometchatAIAssistantBubbleStrokeWidth, 0f
                ) ?: 0f,
                strokeColor = typedArray?.getColor(
                    R.styleable.CometChatAIAssistantBubble_cometchatAIAssistantBubbleStrokeColor, 0
                ) ?: 0,
                avatarStyleRes = typedArray?.getResourceId(
                    R.styleable.CometChatAIAssistantBubble_cometchatAIAssistantBubbleAvatarStyle, 0
                ) ?: 0
            )
        }
    }
}
