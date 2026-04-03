package com.cometchat.uikit.kotlin.presentation.shared.aismartreplies

import android.content.Context
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.StyleRes
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.theme.CometChatTheme

/**
 * Style configuration for CometChatAISmartRepliesView component.
 *
 * This data class holds all styling properties for the AI smart replies view,
 * matching the XML attributes defined in attr_cometchat_ai_smart_replies.xml.
 */
data class CometChatAISmartRepliesStyle(
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
         * @return A CometChatAISmartRepliesStyle with default values
         */
        fun default(context: Context): CometChatAISmartRepliesStyle {
            return extractFromTypedArray(context, null)
        }

        /**
         * Creates a [CometChatAISmartRepliesStyle] from a [TypedArray].
         *
         * This method extracts all properties from the TypedArray and handles
         * recycling the TypedArray after extraction.
         *
         * @param context The context to access theme resources
         * @param typedArray The TypedArray containing style attributes
         * @return A CometChatAISmartRepliesStyle with values from the TypedArray
         */
        fun fromTypedArray(context: Context, typedArray: TypedArray): CometChatAISmartRepliesStyle {
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
         * @return A CometChatAISmartRepliesStyle with extracted values
         */
        fun extractFromTypedArray(
            context: Context,
            typedArray: TypedArray?
        ): CometChatAISmartRepliesStyle {
            return CometChatAISmartRepliesStyle(
                // Title styling
                titleTextColor = typedArray?.getColor(
                    R.styleable.CometChatAISmartReplies_cometchatAISmartRepliesTitleTextColor,
                    CometChatTheme.getTextColorPrimary(context)
                ) ?: CometChatTheme.getTextColorPrimary(context),
                titleTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatAISmartReplies_cometchatAISmartRepliesTitleTextAppearance, 0
                ) ?: 0,

                // Close icon styling
                closeIconTint = typedArray?.getColor(
                    R.styleable.CometChatAISmartReplies_cometchatAISmartRepliesCloseIconTint,
                    CometChatTheme.getIconTintPrimary(context)
                ) ?: CometChatTheme.getIconTintPrimary(context),
                closeIconDrawable = typedArray?.getDrawable(
                    R.styleable.CometChatAISmartReplies_cometchatAISmartRepliesCloseIconDrawable
                ),

                // Container styling
                backgroundColor = typedArray?.getColor(
                    R.styleable.CometChatAISmartReplies_cometchatAISmartRepliesBackgroundColor,
                    CometChatTheme.getBackgroundColor1(context)
                ) ?: CometChatTheme.getBackgroundColor1(context),
                backgroundDrawable = typedArray?.getDrawable(
                    R.styleable.CometChatAISmartReplies_cometchatAISmartRepliesBackgroundDrawable
                ),
                cornerRadius = typedArray?.getDimensionPixelSize(
                    R.styleable.CometChatAISmartReplies_cometchatAISmartRepliesCornerRadius, 0
                ) ?: 0,
                strokeWidth = typedArray?.getDimensionPixelSize(
                    R.styleable.CometChatAISmartReplies_cometchatAISmartRepliesStrokeWidth, 0
                ) ?: 0,
                strokeColor = typedArray?.getColor(
                    R.styleable.CometChatAISmartReplies_cometchatAISmartRepliesStrokeColor, 0
                ) ?: 0,

                // Item styling
                itemBackgroundColor = typedArray?.getColor(
                    R.styleable.CometChatAISmartReplies_cometchatAISmartRepliesItemBackgroundColor,
                    CometChatTheme.getBackgroundColor1(context)
                ) ?: CometChatTheme.getBackgroundColor1(context),
                itemBackgroundDrawable = typedArray?.getDrawable(
                    R.styleable.CometChatAISmartReplies_cometchatAISmartRepliesItemBackgroundDrawable
                ),
                itemCornerRadius = typedArray?.getDimensionPixelSize(
                    R.styleable.CometChatAISmartReplies_cometchatAISmartRepliesItemCornerRadius, 0
                ) ?: 0,
                itemStrokeWidth = typedArray?.getDimensionPixelSize(
                    R.styleable.CometChatAISmartReplies_cometchatAISmartRepliesItemStrokeWidth, 0
                ) ?: 0,
                itemStrokeColor = typedArray?.getColor(
                    R.styleable.CometChatAISmartReplies_cometchatAISmartRepliesItemStrokeColor,
                    CometChatTheme.getStrokeColorLight(context)
                ) ?: CometChatTheme.getStrokeColorLight(context),
                itemTextColor = typedArray?.getColor(
                    R.styleable.CometChatAISmartReplies_cometchatAISmartRepliesItemTextColor,
                    CometChatTheme.getTextColorPrimary(context)
                ) ?: CometChatTheme.getTextColorPrimary(context),
                itemTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatAISmartReplies_cometchatAISmartRepliesItemTextAppearance, 0
                ) ?: 0,

                // Error state styling
                errorStateTextColor = typedArray?.getColor(
                    R.styleable.CometChatAISmartReplies_cometchatAISmartRepliesErrorStateTextColor,
                    CometChatTheme.getTextColorSecondary(context)
                ) ?: CometChatTheme.getTextColorSecondary(context),
                errorStateTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatAISmartReplies_cometchatAISmartRepliesErrorStateTextAppearance, 0
                ) ?: 0
            )
        }
    }
}
