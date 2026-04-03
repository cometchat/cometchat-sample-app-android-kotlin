package com.cometchat.uikit.kotlin.presentation.shared.moderation

import android.content.Context
import android.content.res.TypedArray
import androidx.annotation.ColorInt
import androidx.annotation.StyleRes
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.theme.CometChatTheme

/**
 * Style configuration for CometChatModerationView component.
 *
 * This data class holds all styling properties for the moderation view,
 * matching the XML attributes defined in attr_cometchat_moderation_view.xml.
 */
data class CometChatModerationViewStyle(
    @ColorInt val backgroundColor: Int = 0,
    @StyleRes val textAppearance: Int = 0,
    @ColorInt val textColor: Int = 0,
    @ColorInt val iconTint: Int = 0
) {
    companion object {
        /**
         * Creates a default style with CometChatTheme defaults.
         */
        fun default(context: Context): CometChatModerationViewStyle {
            return CometChatModerationViewStyle(
                backgroundColor = CometChatTheme.getBackgroundColor2(context),
                textAppearance = CometChatTheme.getTextAppearanceCaption1Regular(context),
                textColor = CometChatTheme.getTextColorSecondary(context),
                iconTint = CometChatTheme.getIconTintSecondary(context)
            )
        }

        /**
         * Creates a style by extracting values from XML TypedArray.
         */
        fun fromTypedArray(context: Context, typedArray: TypedArray): CometChatModerationViewStyle {
            return try {
                extractFromTypedArray(context, typedArray)
            } finally {
                typedArray.recycle()
            }
        }

        private fun extractFromTypedArray(context: Context, typedArray: TypedArray?): CometChatModerationViewStyle {
            return CometChatModerationViewStyle(
                backgroundColor = typedArray?.getColor(
                    R.styleable.CometChatModerationView_cometchatModerationViewBackgroundColor,
                    CometChatTheme.getBackgroundColor2(context)
                ) ?: CometChatTheme.getBackgroundColor2(context),
                textAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatModerationView_cometchatModerationViewTextAppearance,
                    CometChatTheme.getTextAppearanceCaption1Regular(context)
                ) ?: CometChatTheme.getTextAppearanceCaption1Regular(context),
                textColor = typedArray?.getColor(
                    R.styleable.CometChatModerationView_cometchatModerationViewTextColor,
                    CometChatTheme.getTextColorSecondary(context)
                ) ?: CometChatTheme.getTextColorSecondary(context),
                iconTint = typedArray?.getColor(
                    R.styleable.CometChatModerationView_cometchatModerationViewIconTint,
                    CometChatTheme.getIconTintSecondary(context)
                ) ?: CometChatTheme.getIconTintSecondary(context)
            )
        }
    }
}
