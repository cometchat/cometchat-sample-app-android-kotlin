package com.cometchat.uikit.kotlin.presentation.shared.messagepreview

import android.content.Context
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.StyleRes
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.theme.CometChatTheme

/**
 * Style configuration for CometChatMessagePreview component.
 *
 * This data class holds all styling properties for the message preview view,
 * matching the XML attributes defined in attr_cometchat_message_preview.xml.
 */
data class CometChatMessagePreviewStyle(
    @ColorInt val backgroundColor: Int = 0,
    @Dimension val strokeWidth: Float = 0f,
    @Dimension val cornerRadius: Float = 0f,
    @ColorInt val strokeColor: Int = 0,
    @ColorInt val separatorColor: Int = 0,
    @ColorInt val titleTextColor: Int = 0,
    @StyleRes val titleTextAppearance: Int = 0,
    @ColorInt val subtitleTextColor: Int = 0,
    @StyleRes val subtitleTextAppearance: Int = 0,
    val closeIcon: Drawable? = null,
    val messageIcon: Drawable? = null,
    @ColorInt val closeIconTint: Int = 0,
    @ColorInt val messageIconTint: Int = 0
) {
    companion object {
        /**
         * Creates a default style with CometChatTheme defaults.
         */
        fun default(context: Context): CometChatMessagePreviewStyle {
            return CometChatMessagePreviewStyle(
                backgroundColor = CometChatTheme.getBackgroundColor2(context),
                strokeWidth = 0f,
                cornerRadius = 0f,
                strokeColor = CometChatTheme.getBorderColorDefault(context),
                separatorColor = CometChatTheme.getBorderColorDefault(context),
                titleTextColor = CometChatTheme.getTextColorPrimary(context),
                titleTextAppearance = CometChatTheme.getTextAppearanceCaption1Medium(context),
                subtitleTextColor = CometChatTheme.getTextColorSecondary(context),
                subtitleTextAppearance = CometChatTheme.getTextAppearanceCaption1Regular(context),
                closeIcon = null,
                messageIcon = null,
                closeIconTint = CometChatTheme.getIconTintSecondary(context),
                messageIconTint = CometChatTheme.getIconTintSecondary(context)
            )
        }

        /**
         * Creates a style for incoming (left-aligned) message previews.
         * Uses a darker neutral background (NeutralColor400) to differentiate from
         * the incoming message bubble background (NeutralColor300).
         */
        fun incoming(context: Context): CometChatMessagePreviewStyle {
            return CometChatMessagePreviewStyle(
                backgroundColor = CometChatTheme.getNeutralColor400(context),
                strokeWidth = 0f,
                cornerRadius = context.resources.getDimension(R.dimen.cometchat_radius_2),
                strokeColor = 0,
                separatorColor = CometChatTheme.getPrimaryColor(context),
                titleTextColor = CometChatTheme.getTextColorHighlight(context),
                titleTextAppearance = CometChatTheme.getTextAppearanceCaption1Medium(context),
                subtitleTextColor = CometChatTheme.getTextColorSecondary(context),
                subtitleTextAppearance = CometChatTheme.getTextAppearanceCaption1Regular(context),
                closeIcon = null,
                messageIcon = null,
                closeIconTint = CometChatTheme.getIconTintSecondary(context),
                messageIconTint = CometChatTheme.getIconTintSecondary(context)
            )
        }

        /**
         * Creates a style for outgoing (right-aligned) message previews.
         * Uses primary color tones suitable for outgoing messages.
         */
        fun outgoing(context: Context): CometChatMessagePreviewStyle {
            return CometChatMessagePreviewStyle(
                backgroundColor = CometChatTheme.getExtendedPrimaryColor800(context),
                strokeWidth = 0f,
                cornerRadius = context.resources.getDimension(R.dimen.cometchat_radius_2),
                strokeColor = 0,
                separatorColor = CometChatTheme.getColorWhite(context),
                titleTextColor = CometChatTheme.getColorWhite(context),
                titleTextAppearance = CometChatTheme.getTextAppearanceCaption1Medium(context),
                subtitleTextColor = CometChatTheme.getColorWhite(context),
                subtitleTextAppearance = CometChatTheme.getTextAppearanceCaption1Regular(context),
                closeIcon = null,
                messageIcon = null,
                closeIconTint = CometChatTheme.getColorWhite(context),
                messageIconTint = CometChatTheme.getColorWhite(context)
            )
        }

        /**
         * Creates a style by extracting values from XML TypedArray.
         */
        fun fromTypedArray(context: Context, typedArray: TypedArray): CometChatMessagePreviewStyle {
            return try {
                extractFromTypedArray(context, typedArray)
            } finally {
                typedArray.recycle()
            }
        }

        private fun extractFromTypedArray(context: Context, typedArray: TypedArray?): CometChatMessagePreviewStyle {
            return CometChatMessagePreviewStyle(
                backgroundColor = typedArray?.getColor(
                    R.styleable.CometChatMessagePreview_cometChatMessagePreviewBackgroundColor,
                    CometChatTheme.getBackgroundColor2(context)
                ) ?: CometChatTheme.getBackgroundColor2(context),
                strokeWidth = typedArray?.getDimension(
                    R.styleable.CometChatMessagePreview_cometChatMessagePreviewStrokeWidth, 0f
                ) ?: 0f,
                cornerRadius = typedArray?.getDimension(
                    R.styleable.CometChatMessagePreview_cometChatMessagePreviewCornerRadius, 0f
                ) ?: 0f,
                strokeColor = typedArray?.getColor(
                    R.styleable.CometChatMessagePreview_cometChatMessagePreviewStrokeColor,
                    CometChatTheme.getBorderColorDefault(context)
                ) ?: CometChatTheme.getBorderColorDefault(context),
                separatorColor = typedArray?.getColor(
                    R.styleable.CometChatMessagePreview_cometChatMessagePreviewSeparatorColor,
                    CometChatTheme.getBorderColorDefault(context)
                ) ?: CometChatTheme.getBorderColorDefault(context),
                titleTextColor = typedArray?.getColor(
                    R.styleable.CometChatMessagePreview_cometChatMessagePreviewTitleTextColor,
                    CometChatTheme.getTextColorPrimary(context)
                ) ?: CometChatTheme.getTextColorPrimary(context),
                titleTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatMessagePreview_cometChatMessagePreviewTitleTextAppearance,
                    CometChatTheme.getTextAppearanceCaption1Medium(context)
                ) ?: CometChatTheme.getTextAppearanceCaption1Medium(context),
                subtitleTextColor = typedArray?.getColor(
                    R.styleable.CometChatMessagePreview_cometChatMessagePreviewSubtitleTextColor,
                    CometChatTheme.getTextColorSecondary(context)
                ) ?: CometChatTheme.getTextColorSecondary(context),
                subtitleTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatMessagePreview_cometChatMessagePreviewSubtitleTextAppearance,
                    CometChatTheme.getTextAppearanceCaption1Regular(context)
                ) ?: CometChatTheme.getTextAppearanceCaption1Regular(context),
                closeIcon = typedArray?.getDrawable(
                    R.styleable.CometChatMessagePreview_cometChatMessagePreviewCloseIcon
                ),
                messageIcon = typedArray?.getDrawable(
                    R.styleable.CometChatMessagePreview_cometChatMessagePreviewMessageIcon
                ),
                closeIconTint = typedArray?.getColor(
                    R.styleable.CometChatMessagePreview_cometChatMessagePreviewCloseIconTint,
                    CometChatTheme.getIconTintSecondary(context)
                ) ?: CometChatTheme.getIconTintSecondary(context),
                messageIconTint = typedArray?.getColor(
                    R.styleable.CometChatMessagePreview_cometChatMessagePreviewMessageIconTint,
                    CometChatTheme.getIconTintSecondary(context)
                ) ?: CometChatTheme.getIconTintSecondary(context)
            )
        }
    }
}
