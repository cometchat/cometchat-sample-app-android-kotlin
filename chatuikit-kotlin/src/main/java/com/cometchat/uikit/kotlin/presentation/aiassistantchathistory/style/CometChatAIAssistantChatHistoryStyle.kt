package com.cometchat.uikit.kotlin.presentation.aiassistantchathistory.style

import android.content.Context
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.StyleRes
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.theme.CometChatTheme

/**
 * Style configuration for CometChatAIAssistantChatHistory component.
 *
 * Contains 21 style properties covering the component background, header section,
 * new chat section, date separator, message item, and delete option (popup menu).
 * All properties use Android-native types (@ColorInt Int, @StyleRes Int, Drawable,
 * @DrawableRes Int) with sentinel default values (0 for ints, null for Drawables).
 */
data class CometChatAIAssistantChatHistoryStyle(
    // Component background
    @ColorInt val chatHistoryBackgroundColor: Int = 0,

    // Header section
    @ColorInt val chatHistoryHeaderBackgroundColor: Int = 0,
    @ColorInt val chatHistoryHeaderTextColor: Int = 0,
    @StyleRes val chatHistoryHeaderTextAppearance: Int = 0,
    val chatHistoryHeaderCloseIcon: Drawable? = null,
    @ColorInt val chatHistoryHeaderCloseIconTint: Int = 0,

    // New Chat section
    @ColorInt val newChatBackgroundColor: Int = 0,
    @ColorInt val newChatTextColor: Int = 0,
    @StyleRes val newChatTextAppearance: Int = 0,
    @DrawableRes val newChatIcon: Int = 0,
    @ColorInt val newChatIconTint: Int = 0,

    // Date separator
    @ColorInt val dateSeparatorBackgroundColor: Int = 0,
    @ColorInt val dateSeparatorTextColor: Int = 0,
    @StyleRes val dateSeparatorTextAppearance: Int = 0,

    // Message item
    @ColorInt val itemBackgroundColor: Int = 0,
    @ColorInt val itemTextColor: Int = 0,
    @StyleRes val itemTextAppearance: Int = 0,

    // Delete option (popup menu)
    val deleteOptionIcon: Drawable? = null,
    @ColorInt val deleteOptionIconTint: Int = 0,
    @ColorInt val deleteOptionTextColor: Int = 0,
    @StyleRes val deleteOptionTextAppearance: Int = 0
) {
    companion object {
        /**
         * Creates a default style by extracting values from the theme's
         * cometChatAIAssistantChatHistoryStyle attribute.
         *
         * @param context The context to access theme resources
         * @return A CometChatAIAssistantChatHistoryStyle with values from theme or fallback defaults
         */
        fun default(context: Context): CometChatAIAssistantChatHistoryStyle {
            return extractFromThemeStyle(context, R.attr.cometChatAIAssistantChatHistoryStyle)
        }

        /**
         * Creates a style by extracting values from XML TypedArray.
         *
         * This method handles TypedArray recycling internally using try-finally.
         * All attributes are extracted from the corresponding
         * attr_cometchat_ai_assistant_chat_history.xml file.
         *
         * @param context The Android context for accessing theme resources
         * @param typedArray The TypedArray containing XML attribute values (will be recycled)
         * @return A CometChatAIAssistantChatHistoryStyle with values from XML or theme defaults
         */
        fun fromTypedArray(
            context: Context,
            typedArray: TypedArray
        ): CometChatAIAssistantChatHistoryStyle {
            return try {
                extractFromTypedArray(context, typedArray)
            } finally {
                typedArray.recycle()
            }
        }

        /**
         * Extracts the style from a theme style attribute.
         *
         * @param context The context to access theme resources
         * @param themeStyleAttr The attribute reference to the chat history style in theme
         * @return A CometChatAIAssistantChatHistoryStyle with values from the theme style
         */
        private fun extractFromThemeStyle(
            context: Context,
            themeStyleAttr: Int
        ): CometChatAIAssistantChatHistoryStyle {
            val themeTypedArray = context.obtainStyledAttributes(intArrayOf(themeStyleAttr))
            val styleResId = themeTypedArray.getResourceId(0, 0)
            themeTypedArray.recycle()

            return extractFromStyleResource(context, styleResId)
        }

        /**
         * Extracts style values from a specific style resource ID.
         * If styleResId is 0, returns a style with CometChatTheme defaults.
         *
         * @param context The context to access theme resources
         * @param styleResId The style resource ID to extract from (0 for defaults only)
         * @return A CometChatAIAssistantChatHistoryStyle with values from the style resource or defaults
         */
        private fun extractFromStyleResource(
            context: Context,
            styleResId: Int
        ): CometChatAIAssistantChatHistoryStyle {
            if (styleResId == 0) {
                return extractFromTypedArray(context, null)
            }
            val typedArray = context.obtainStyledAttributes(
                styleResId,
                R.styleable.CometChatAIAssistantChatHistory
            )
            return try {
                extractFromTypedArray(context, typedArray)
            } finally {
                typedArray.recycle()
            }
        }

        /**
         * Core extraction method that reads style values from a TypedArray.
         * Does NOT handle recycling — caller is responsible for recycling the TypedArray.
         *
         * @param context The Android context for accessing theme resources
         * @param typedArray The TypedArray to extract from, or null for defaults only
         * @return A CometChatAIAssistantChatHistoryStyle with extracted values or theme defaults
         */
        private fun extractFromTypedArray(
            context: Context,
            typedArray: TypedArray?
        ): CometChatAIAssistantChatHistoryStyle {
            return CometChatAIAssistantChatHistoryStyle(
                // Component background
                chatHistoryBackgroundColor = typedArray?.getColor(
                    R.styleable.CometChatAIAssistantChatHistory_cometChatAIAssistantChatHistoryBackgroundColor,
                    CometChatTheme.getBackgroundColor3(context)
                ) ?: CometChatTheme.getBackgroundColor3(context),

                // Header section
                chatHistoryHeaderBackgroundColor = typedArray?.getColor(
                    R.styleable.CometChatAIAssistantChatHistory_cometChatAIAssistantChatHistoryHeaderBackgroundColor,
                    CometChatTheme.getBackgroundColor3(context)
                ) ?: CometChatTheme.getBackgroundColor3(context),

                chatHistoryHeaderTextColor = typedArray?.getColor(
                    R.styleable.CometChatAIAssistantChatHistory_cometChatAIAssistantChatHistoryHeaderTextColor,
                    CometChatTheme.getTextColorPrimary(context)
                ) ?: CometChatTheme.getTextColorPrimary(context),

                chatHistoryHeaderTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatAIAssistantChatHistory_cometChatAIAssistantChatHistoryHeaderTextAppearance,
                    CometChatTheme.getTextAppearanceHeading4Medium(context)
                ) ?: CometChatTheme.getTextAppearanceHeading4Medium(context),

                chatHistoryHeaderCloseIcon = typedArray?.getDrawable(
                    R.styleable.CometChatAIAssistantChatHistory_cometChatAIAssistantChatHistoryHeaderCloseIcon
                ),

                chatHistoryHeaderCloseIconTint = typedArray?.getColor(
                    R.styleable.CometChatAIAssistantChatHistory_cometChatAIAssistantChatHistoryHeaderCloseIconTint,
                    CometChatTheme.getIconTintSecondary(context)
                ) ?: CometChatTheme.getIconTintSecondary(context),

                // New Chat section
                newChatBackgroundColor = typedArray?.getColor(
                    R.styleable.CometChatAIAssistantChatHistory_cometChatAIAssistantChatHistoryNewChatBackgroundColor,
                    CometChatTheme.getBackgroundColor3(context)
                ) ?: CometChatTheme.getBackgroundColor3(context),

                newChatTextColor = typedArray?.getColor(
                    R.styleable.CometChatAIAssistantChatHistory_cometChatAIAssistantChatHistoryNewChatTextColor,
                    CometChatTheme.getTextColorPrimary(context)
                ) ?: CometChatTheme.getTextColorPrimary(context),

                newChatTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatAIAssistantChatHistory_cometChatAIAssistantChatHistoryNewChatTextAppearance,
                    CometChatTheme.getTextAppearanceButtonRegular(context)
                ) ?: CometChatTheme.getTextAppearanceButtonRegular(context),

                newChatIcon = typedArray?.getResourceId(
                    R.styleable.CometChatAIAssistantChatHistory_cometChatAIAssistantChatHistoryNewChatIcon,
                    0
                ) ?: 0,

                newChatIconTint = typedArray?.getColor(
                    R.styleable.CometChatAIAssistantChatHistory_cometChatAIAssistantChatHistoryNewChatIconTint,
                    CometChatTheme.getIconTintSecondary(context)
                ) ?: CometChatTheme.getIconTintSecondary(context),

                // Date separator
                dateSeparatorBackgroundColor = typedArray?.getColor(
                    R.styleable.CometChatAIAssistantChatHistory_cometChatAIAssistantChatHistoryDateSeparatorBackgroundColor,
                    android.graphics.Color.TRANSPARENT
                ) ?: android.graphics.Color.TRANSPARENT,

                dateSeparatorTextColor = typedArray?.getColor(
                    R.styleable.CometChatAIAssistantChatHistory_cometChatAIAssistantChatHistoryDateSeparatorTextColor,
                    CometChatTheme.getTextColorTertiary(context)
                ) ?: CometChatTheme.getTextColorTertiary(context),

                dateSeparatorTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatAIAssistantChatHistory_cometChatAIAssistantChatHistoryDateSeparatorTextAppearance,
                    CometChatTheme.getTextAppearanceCaption1Medium(context)
                ) ?: CometChatTheme.getTextAppearanceCaption1Medium(context),

                // Message item
                itemBackgroundColor = typedArray?.getColor(
                    R.styleable.CometChatAIAssistantChatHistory_cometChatAIAssistantChatHistoryItemBackgroundColor,
                    CometChatTheme.getBackgroundColor3(context)
                ) ?: CometChatTheme.getBackgroundColor3(context),

                itemTextColor = typedArray?.getColor(
                    R.styleable.CometChatAIAssistantChatHistory_cometChatAIAssistantChatHistoryItemTextColor,
                    CometChatTheme.getTextColorPrimary(context)
                ) ?: CometChatTheme.getTextColorPrimary(context),

                itemTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatAIAssistantChatHistory_cometChatAIAssistantChatHistoryItemTextAppearance,
                    CometChatTheme.getTextAppearanceBodyRegular(context)
                ) ?: CometChatTheme.getTextAppearanceBodyRegular(context),

                // Delete option (popup menu)
                deleteOptionIcon = typedArray?.getDrawable(
                    R.styleable.CometChatAIAssistantChatHistory_cometChatAIAssistantChatHistoryDeleteOptionIcon
                ),

                deleteOptionIconTint = typedArray?.getColor(
                    R.styleable.CometChatAIAssistantChatHistory_cometChatAIAssistantChatHistoryDeleteOptionIconTint,
                    CometChatTheme.getErrorColor(context)
                ) ?: CometChatTheme.getErrorColor(context),

                deleteOptionTextColor = typedArray?.getColor(
                    R.styleable.CometChatAIAssistantChatHistory_cometChatAIAssistantChatHistoryDeleteOptionTextColor,
                    CometChatTheme.getTextColorPrimary(context)
                ) ?: CometChatTheme.getTextColorPrimary(context),

                deleteOptionTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatAIAssistantChatHistory_cometChatAIAssistantChatHistoryDeleteOptionTextAppearance,
                    CometChatTheme.getTextAppearanceBodyRegular(context)
                ) ?: CometChatTheme.getTextAppearanceBodyRegular(context)
            )
        }
    }
}
