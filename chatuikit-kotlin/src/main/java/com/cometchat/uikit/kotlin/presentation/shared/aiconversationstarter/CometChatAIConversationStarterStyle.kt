package com.cometchat.uikit.kotlin.presentation.shared.aiconversationstarter

import android.content.Context
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.StyleRes
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.theme.CometChatTheme

/**
 * Style configuration for CometChatAIConversationStarterView component.
 *
 * This data class holds all styling properties for the AI conversation starter view,
 * matching the XML attributes defined in attr_cometchat_ai_conversation_starter.xml.
 */
data class CometChatAIConversationStarterStyle(
    // Container styling
    @ColorInt val backgroundColor: Int = 0,
    val backgroundDrawable: Drawable? = null,
    @Dimension val cornerRadius: Int = 0,
    @Dimension val strokeWidth: Int = 0,
    @ColorInt val strokeColor: Int = 0,

    // Item styling
    @ColorInt val itemBackgroundColor: Int = 0,
    val itemBackgroundDrawable: Drawable? = null,
    @Dimension val itemCornerRadius: Int = 0,
    @Dimension val itemStrokeWidth: Int = 0,
    @ColorInt val itemStrokeColor: Int = 0,
    @ColorInt val itemTextColor: Int = 0,
    @StyleRes val itemTextAppearance: Int = 0,

    // Error state styling
    @ColorInt val errorStateTextColor: Int = 0,
    @StyleRes val errorStateTextAppearance: Int = 0
) {
    companion object {
        /**
         * Creates a default style with theme-appropriate values.
         *
         * @param context The context to access theme resources
         * @return A CometChatAIConversationStarterStyle with default values
         */
        fun default(context: Context): CometChatAIConversationStarterStyle {
            return extractFromTypedArray(context, null)
        }

        /**
         * Creates a [CometChatAIConversationStarterStyle] from a [TypedArray].
         *
         * This method extracts all properties from the TypedArray and handles
         * recycling the TypedArray after extraction.
         *
         * @param context The context to access theme resources
         * @param typedArray The TypedArray containing style attributes
         * @return A CometChatAIConversationStarterStyle with values from the TypedArray
         */
        fun fromTypedArray(context: Context, typedArray: TypedArray): CometChatAIConversationStarterStyle {
            return try {
                extractFromTypedArray(context, typedArray)
            } finally {
                typedArray.recycle()
            }
        }

        /**
         * Extracts style properties from a TypedArray without recycling it.
         *
         * @param context The context to access theme resources
         * @param typedArray The TypedArray containing style attributes, or null for defaults
         * @return A CometChatAIConversationStarterStyle with extracted values
         */
        fun extractFromTypedArray(
            context: Context,
            typedArray: TypedArray?
        ): CometChatAIConversationStarterStyle {
            return CometChatAIConversationStarterStyle(
                // Container styling
                backgroundColor = typedArray?.getColor(
                    R.styleable.CometChatAIConversationStarter_cometchatAIConversationStarterBackgroundColor, 0
                ) ?: 0,
                backgroundDrawable = typedArray?.getDrawable(
                    R.styleable.CometChatAIConversationStarter_cometchatAIConversationStarterBackgroundDrawable
                ),
                cornerRadius = typedArray?.getDimensionPixelSize(
                    R.styleable.CometChatAIConversationStarter_cometchatAIConversationStarterCornerRadius, 0
                ) ?: 0,
                strokeWidth = typedArray?.getDimensionPixelSize(
                    R.styleable.CometChatAIConversationStarter_cometchatAIConversationStarterStrokeWidth, 0
                ) ?: 0,
                strokeColor = typedArray?.getColor(
                    R.styleable.CometChatAIConversationStarter_cometchatAIConversationStarterStrokeColor, 0
                ) ?: 0,

                // Item styling
                itemBackgroundColor = typedArray?.getColor(
                    R.styleable.CometChatAIConversationStarter_cometchatAIConversationStarterItemBackgroundColor,
                    CometChatTheme.getBackgroundColor1(context)
                ) ?: CometChatTheme.getBackgroundColor1(context),
                itemBackgroundDrawable = typedArray?.getDrawable(
                    R.styleable.CometChatAIConversationStarter_cometchatAIConversationStarterItemBackgroundDrawable
                ),
                itemCornerRadius = typedArray?.getDimensionPixelSize(
                    R.styleable.CometChatAIConversationStarter_cometchatAIConversationStarterItemCornerRadius, 0
                ) ?: 0,
                itemStrokeWidth = typedArray?.getDimensionPixelSize(
                    R.styleable.CometChatAIConversationStarter_cometchatAIConversationStarterItemStrokeWidth, 0
                ) ?: 0,
                itemStrokeColor = typedArray?.getColor(
                    R.styleable.CometChatAIConversationStarter_cometchatAIConversationStarterItemStrokeColor, 0
                ) ?: 0,
                itemTextColor = typedArray?.getColor(
                    R.styleable.CometChatAIConversationStarter_cometchatAIConversationStarterItemTextColor,
                    CometChatTheme.getTextColorPrimary(context)
                ) ?: CometChatTheme.getTextColorPrimary(context),
                itemTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatAIConversationStarter_cometchatAIConversationStarterItemTextAppearance, 0
                ) ?: 0,

                // Error state styling
                errorStateTextColor = typedArray?.getColor(
                    R.styleable.CometChatAIConversationStarter_cometchatAIConversationStarterErrorStateTextColor,
                    CometChatTheme.getTextColorSecondary(context)
                ) ?: CometChatTheme.getTextColorSecondary(context),
                errorStateTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatAIConversationStarter_cometchatAIConversationStarterErrorStateTextAppearance, 0
                ) ?: 0
            )
        }
    }
}
