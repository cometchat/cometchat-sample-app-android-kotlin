package com.cometchat.uikit.kotlin.presentation.shared.aiconversationsummary

import android.content.Context
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.StyleRes
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.theme.CometChatTheme

/**
 * Style configuration for CometChatAIConversationSummaryView component.
 *
 * This data class holds all styling properties for the AI conversation summary view,
 * matching the XML attributes defined in attr_cometchat_ai_conversation_summary.xml.
 */
data class CometChatAIConversationSummaryStyle(
    // Title styling
    @ColorInt val titleTextColor: Int = 0,
    @StyleRes val titleTextAppearance: Int = 0,

    // Close icon styling
    @ColorInt val closeIconTint: Int = 0,
    val closeIconDrawable: Drawable? = null,

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
         * @return A CometChatAIConversationSummaryStyle with default values
         */
        fun default(context: Context): CometChatAIConversationSummaryStyle {
            return extractFromTypedArray(context, null)
        }

        /**
         * Creates a [CometChatAIConversationSummaryStyle] from a [TypedArray].
         *
         * This method extracts all properties from the TypedArray and handles
         * recycling the TypedArray after extraction.
         *
         * @param context The context to access theme resources
         * @param typedArray The TypedArray containing style attributes
         * @return A CometChatAIConversationSummaryStyle with values from the TypedArray
         */
        fun fromTypedArray(context: Context, typedArray: TypedArray): CometChatAIConversationSummaryStyle {
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
         * @return A CometChatAIConversationSummaryStyle with extracted values
         */
        fun extractFromTypedArray(
            context: Context,
            typedArray: TypedArray?
        ): CometChatAIConversationSummaryStyle {
            return CometChatAIConversationSummaryStyle(
                // Title styling
                titleTextColor = typedArray?.getColor(
                    R.styleable.CometChatAIConversationSummary_cometchatAIConversationSummaryTitleTextColor,
                    CometChatTheme.getTextColorPrimary(context)
                ) ?: CometChatTheme.getTextColorPrimary(context),
                titleTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatAIConversationSummary_cometchatAIConversationSummaryTitleTextAppearance, 0
                ) ?: 0,

                // Close icon styling
                closeIconTint = typedArray?.getColor(
                    R.styleable.CometChatAIConversationSummary_cometchatAIConversationSummaryCloseIconTint,
                    CometChatTheme.getIconTintPrimary(context)
                ) ?: CometChatTheme.getIconTintPrimary(context),
                closeIconDrawable = typedArray?.getDrawable(
                    R.styleable.CometChatAIConversationSummary_cometchatAIConversationSummaryCloseIconDrawable
                ),

                // Container styling
                backgroundColor = typedArray?.getColor(
                    R.styleable.CometChatAIConversationSummary_cometchatAIConversationSummaryBackgroundColor,
                    CometChatTheme.getBackgroundColor1(context)
                ) ?: CometChatTheme.getBackgroundColor1(context),
                backgroundDrawable = typedArray?.getDrawable(
                    R.styleable.CometChatAIConversationSummary_cometchatAIConversationSummaryBackgroundDrawable
                ),
                cornerRadius = typedArray?.getDimensionPixelSize(
                    R.styleable.CometChatAIConversationSummary_cometchatAIConversationSummaryCornerRadius, 0
                ) ?: 0,
                strokeWidth = typedArray?.getDimensionPixelSize(
                    R.styleable.CometChatAIConversationSummary_cometchatAIConversationSummaryStrokeWidth, 0
                ) ?: 0,
                strokeColor = typedArray?.getColor(
                    R.styleable.CometChatAIConversationSummary_cometchatAIConversationSummaryStrokeColor, 0
                ) ?: 0,

                // Item styling
                itemBackgroundColor = typedArray?.getColor(
                    R.styleable.CometChatAIConversationSummary_cometchatAIConversationSummaryItemBackgroundColor, 0
                ) ?: 0,
                itemBackgroundDrawable = typedArray?.getDrawable(
                    R.styleable.CometChatAIConversationSummary_cometchatAIConversationSummaryItemBackgroundDrawable
                ),
                itemCornerRadius = typedArray?.getDimensionPixelSize(
                    R.styleable.CometChatAIConversationSummary_cometchatAIConversationSummaryItemCornerRadius, 0
                ) ?: 0,
                itemStrokeWidth = typedArray?.getDimensionPixelSize(
                    R.styleable.CometChatAIConversationSummary_cometchatAIConversationSummaryItemStrokeWidth, 0
                ) ?: 0,
                itemStrokeColor = typedArray?.getColor(
                    R.styleable.CometChatAIConversationSummary_cometchatAIConversationSummaryItemStrokeColor, 0
                ) ?: 0,
                itemTextColor = typedArray?.getColor(
                    R.styleable.CometChatAIConversationSummary_cometchatAIConversationSummaryItemTextColor,
                    CometChatTheme.getTextColorPrimary(context)
                ) ?: CometChatTheme.getTextColorPrimary(context),
                itemTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatAIConversationSummary_cometchatAIConversationSummaryItemTextAppearance, 0
                ) ?: 0,

                // Error state styling
                errorStateTextColor = typedArray?.getColor(
                    R.styleable.CometChatAIConversationSummary_cometchatAIConversationSummaryErrorStateTextColor,
                    CometChatTheme.getTextColorSecondary(context)
                ) ?: CometChatTheme.getTextColorSecondary(context),
                errorStateTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatAIConversationSummary_cometchatAIConversationSummaryErrorStateTextAppearance, 0
                ) ?: 0
            )
        }
    }
}
