package com.cometchat.uikit.kotlin.presentation.messagecomposer.style

import android.content.Context
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.StyleRes
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.theme.CometChatTheme

/**
 * Style configuration for CometChatMessageComposer component.
 * 
 * This data class mirrors the Compose CometChatMessageComposerStyle for consistency
 * across both UI implementations. All style properties can be customized either
 * programmatically or via XML attributes.
 */
data class CometChatMessageComposerStyle(
    // Container styling
    @ColorInt val backgroundColor: Int = 0,
    @ColorInt val strokeColor: Int = 0,
    @Dimension val strokeWidth: Int = 0,
    @Dimension val cornerRadius: Int = 0,

    // Compose box styling
    @ColorInt val composeBoxBackgroundColor: Int = 0,
    @ColorInt val composeBoxStrokeColor: Int = 0,
    @Dimension val composeBoxStrokeWidth: Int = 0,
    @Dimension val composeBoxCornerRadius: Int = 0,

    // Separator
    @ColorInt val separatorColor: Int = 0,

    // Attachment button styling
    val attachmentIcon: Drawable? = null,
    @ColorInt val attachmentIconTint: Int = 0,

    // Voice recording button styling
    val voiceRecordingIcon: Drawable? = null,
    @ColorInt val voiceRecordingIconTint: Int = 0,

    // AI button styling
    val aiIcon: Drawable? = null,
    @ColorInt val aiIconTint: Int = 0,

    // Sticker button styling
    val stickerIcon: Drawable? = null,
    @ColorInt val stickerIconTint: Int = 0,

    // Send button styling
    val sendButtonActiveIcon: Drawable? = null,
    val sendButtonInactiveIcon: Drawable? = null,
    @ColorInt val sendButtonActiveBackgroundColor: Int = 0,
    @ColorInt val sendButtonInactiveBackgroundColor: Int = 0,
    @ColorInt val sendButtonIconTint: Int = 0,
    val sendButtonStopIcon: Drawable? = null,

    // Edit preview styling
    @ColorInt val editPreviewTitleTextColor: Int = 0,
    @StyleRes val editPreviewTitleTextAppearance: Int = 0,
    @ColorInt val editPreviewMessageTextColor: Int = 0,
    @StyleRes val editPreviewMessageTextAppearance: Int = 0,
    @ColorInt val editPreviewBackgroundColor: Int = 0,
    @Dimension val editPreviewCornerRadius: Int = 0,
    @ColorInt val editPreviewStrokeColor: Int = 0,
    @Dimension val editPreviewStrokeWidth: Int = 0,
    val editPreviewCloseIcon: Drawable? = null,
    @ColorInt val editPreviewCloseIconTint: Int = 0,

    // Message preview styling (for reply/quote)
    @ColorInt val messagePreviewSeparatorColor: Int = 0,
    @ColorInt val messagePreviewTitleTextColor: Int = 0,
    @StyleRes val messagePreviewTitleTextAppearance: Int = 0,
    @ColorInt val messagePreviewSubtitleTextColor: Int = 0,
    @StyleRes val messagePreviewSubtitleTextAppearance: Int = 0,
    @ColorInt val messagePreviewBackgroundColor: Int = 0,
    @Dimension val messagePreviewCornerRadius: Int = 0,
    @ColorInt val messagePreviewStrokeColor: Int = 0,
    @Dimension val messagePreviewStrokeWidth: Int = 0,
    val messagePreviewCloseIcon: Drawable? = null,
    @ColorInt val messagePreviewCloseIconTint: Int = 0,

    // Info panel styling
    val infoIcon: Drawable? = null,
    @ColorInt val infoIconTint: Int = 0,
    @ColorInt val infoTextColor: Int = 0,
    @StyleRes val infoTextAppearance: Int = 0,
    @ColorInt val infoBackgroundColor: Int = 0,
    @Dimension val infoCornerRadius: Int = 0,
    @ColorInt val infoStrokeColor: Int = 0,
    @Dimension val infoStrokeWidth: Int = 0,

    // Input text styling
    @ColorInt val inputTextColor: Int = 0,
    @StyleRes val inputTextAppearance: Int = 0,
    @ColorInt val inputPlaceholderColor: Int = 0,
    @StyleRes val inputPlaceholderAppearance: Int = 0,

    // Rich text toolbar styling
    @ColorInt val richTextToolbarBackgroundColor: Int = 0,
    @ColorInt val richTextToolbarIconTint: Int = 0,
    @ColorInt val richTextToolbarActiveIconTint: Int = 0,
    
    // Rich text toggle button styling (DEPRECATED)
    @Deprecated("Toggle button removed - toolbar visibility is now automatic")
    val richTextToolbarToggleIcon: Drawable? = null,
    @Deprecated("Toggle button removed - toolbar visibility is now automatic")
    @ColorInt val richTextToolbarToggleIconTint: Int = 0,

    // Link edit dialog styling
    @ColorInt val linkDialogBackgroundColor: Int = 0,
    @ColorInt val linkDialogTitleTextColor: Int = 0,
    @StyleRes val linkDialogTitleTextAppearance: Int = 0,
    @ColorInt val linkDialogInputBackgroundColor: Int = 0,
    @ColorInt val linkDialogInputTextColor: Int = 0,
    @StyleRes val linkDialogInputTextAppearance: Int = 0,
    @ColorInt val linkDialogButtonTextColor: Int = 0,
    @StyleRes val linkDialogButtonTextAppearance: Int = 0
) {
    companion object {
        /**
         * Creates a default style by extracting values from the theme's cometchatMessageComposerStyle.
         */
        fun default(context: Context): CometChatMessageComposerStyle {
            return extractFromThemeStyle(context)
        }

        private fun extractFromThemeStyle(context: Context): CometChatMessageComposerStyle {
            val themeTypedArray = context.obtainStyledAttributes(intArrayOf(R.attr.cometchatMessageComposerStyle))
            val styleResId = themeTypedArray.getResourceId(0, 0)
            themeTypedArray.recycle()
            return extractFromStyleResource(context, styleResId)
        }

        private fun extractFromStyleResource(context: Context, styleResId: Int): CometChatMessageComposerStyle {
            if (styleResId == 0) {
                return extractFromTypedArray(context, null)
            }
            val typedArray = context.obtainStyledAttributes(styleResId, R.styleable.CometChatMessageComposer)
            return try {
                extractFromTypedArray(context, typedArray)
            } finally {
                typedArray.recycle()
            }
        }

        @Suppress("DEPRECATION")
        fun fromTypedArray(context: Context, typedArray: TypedArray): CometChatMessageComposerStyle {
            return try {
                extractFromTypedArray(context, typedArray)
            } finally {
                typedArray.recycle()
            }
        }

        @Suppress("DEPRECATION")
        private fun extractFromTypedArray(context: Context, typedArray: TypedArray?): CometChatMessageComposerStyle {
            return CometChatMessageComposerStyle(
                // Container styling
                backgroundColor = typedArray?.getColor(
                    R.styleable.CometChatMessageComposer_cometchatMessageComposerBackgroundColor,
                    CometChatTheme.getBackgroundColor3(context)
                ) ?: android.graphics.Color.TRANSPARENT,
                strokeColor = typedArray?.getColor(
                    R.styleable.CometChatMessageComposer_cometchatMessageComposerStrokeColor, 0
                ) ?: 0,
                strokeWidth = typedArray?.getDimensionPixelSize(
                    R.styleable.CometChatMessageComposer_cometchatMessageComposerStrokeWidth, 0
                ) ?: 0,
                cornerRadius = typedArray?.getDimensionPixelSize(
                    R.styleable.CometChatMessageComposer_cometchatMessageComposerCornerRadius, 0
                ) ?: 0,

                // Compose box styling
                composeBoxBackgroundColor = typedArray?.getColor(
                    R.styleable.CometChatMessageComposer_cometchatMessageComposerComposeBoxBackgroundColor,
                    CometChatTheme.getBackgroundColor1(context)
                ) ?: android.graphics.Color.TRANSPARENT,
                composeBoxStrokeColor = typedArray?.getColor(
                    R.styleable.CometChatMessageComposer_cometchatMessageComposerComposeBoxStrokeColor,
                    CometChatTheme.getBorderColorLight(context)
                ) ?: CometChatTheme.getBorderColorLight(context),
                composeBoxStrokeWidth = typedArray?.getDimensionPixelSize(
                    R.styleable.CometChatMessageComposer_cometchatMessageComposerComposeBoxStrokeWidth,
                    context.resources.getDimensionPixelSize(R.dimen.cometchat_1dp)
                ) ?: context.resources.getDimensionPixelSize(R.dimen.cometchat_1dp),
                composeBoxCornerRadius = typedArray?.getDimensionPixelSize(
                    R.styleable.CometChatMessageComposer_cometchatMessageComposerComposeBoxCornerRadius,
                    context.resources.getDimensionPixelSize(R.dimen.cometchat_corner_radius_6)
                ) ?: context.resources.getDimensionPixelSize(R.dimen.cometchat_corner_radius_6),

                // Separator styling
                separatorColor = typedArray?.getColor(
                    R.styleable.CometChatMessageComposer_cometchatMessageComposerSeparatorColor,
                    CometChatTheme.getBorderColorLight(context)
                ) ?: CometChatTheme.getBorderColorLight(context),

                // Attachment button styling
                attachmentIcon = typedArray?.getDrawable(
                    R.styleable.CometChatMessageComposer_cometchatMessageComposerAttachmentIcon
                ),
                attachmentIconTint = typedArray?.getColor(
                    R.styleable.CometChatMessageComposer_cometchatMessageComposerAttachmentIconTint,
                    CometChatTheme.getIconTintSecondary(context)
                ) ?: CometChatTheme.getIconTintSecondary(context),

                // Voice recording button styling
                voiceRecordingIcon = typedArray?.getDrawable(
                    R.styleable.CometChatMessageComposer_cometchatMessageComposerVoiceRecordingIcon
                ),
                voiceRecordingIconTint = typedArray?.getColor(
                    R.styleable.CometChatMessageComposer_cometchatMessageComposerVoiceRecordingIconTint,
                    CometChatTheme.getIconTintSecondary(context)
                ) ?: CometChatTheme.getIconTintSecondary(context),

                // AI button styling
                aiIcon = typedArray?.getDrawable(
                    R.styleable.CometChatMessageComposer_cometchatMessageComposerAIIcon
                ),
                aiIconTint = typedArray?.getColor(
                    R.styleable.CometChatMessageComposer_cometchatMessageComposerAIIconTint,
                    CometChatTheme.getIconTintSecondary(context)
                ) ?: CometChatTheme.getIconTintSecondary(context),

                // Sticker button styling
                stickerIcon = typedArray?.getDrawable(
                    R.styleable.CometChatMessageComposer_cometchatMessageComposerStickerIcon
                ),
                stickerIconTint = typedArray?.getColor(
                    R.styleable.CometChatMessageComposer_cometchatMessageComposerStickerIconTint,
                    CometChatTheme.getIconTintSecondary(context)
                ) ?: CometChatTheme.getIconTintSecondary(context),

                // Send button styling
                sendButtonActiveIcon = typedArray?.getDrawable(
                    R.styleable.CometChatMessageComposer_cometchatMessageComposerSendButtonActiveIcon
                ),
                sendButtonInactiveIcon = typedArray?.getDrawable(
                    R.styleable.CometChatMessageComposer_cometchatMessageComposerSendButtonInactiveIcon
                ),
                sendButtonActiveBackgroundColor = typedArray?.getColor(
                    R.styleable.CometChatMessageComposer_cometchatMessageComposerSendButtonActiveBackgroundColor,
                    CometChatTheme.getPrimaryColor(context)
                ) ?: CometChatTheme.getPrimaryColor(context),
                sendButtonInactiveBackgroundColor = typedArray?.getColor(
                    R.styleable.CometChatMessageComposer_cometchatMessageComposerSendButtonInactiveBackgroundColor,
                    CometChatTheme.getBackgroundColor3(context)
                ) ?: CometChatTheme.getBackgroundColor3(context),
                sendButtonIconTint = CometChatTheme.getColorWhite(context),
                sendButtonStopIcon = typedArray?.getDrawable(
                    R.styleable.CometChatMessageComposer_cometchatMessageComposerSendButtonStopIcon
                ),

                // Edit preview styling
                editPreviewTitleTextColor = typedArray?.getColor(
                    R.styleable.CometChatMessageComposer_cometchatMessageComposerEditPreviewTitleTextColor,
                    CometChatTheme.getTextColorPrimary(context)
                ) ?: CometChatTheme.getTextColorPrimary(context),
                editPreviewTitleTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatMessageComposer_cometchatMessageComposerEditPreviewTitleTextAppearance,
                    CometChatTheme.getTextAppearanceCaption1Medium(context)
                ) ?: CometChatTheme.getTextAppearanceCaption1Medium(context),
                editPreviewMessageTextColor = typedArray?.getColor(
                    R.styleable.CometChatMessageComposer_cometchatMessageComposerEditPreviewMessageTextColor,
                    CometChatTheme.getTextColorSecondary(context)
                ) ?: CometChatTheme.getTextColorSecondary(context),
                editPreviewMessageTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatMessageComposer_cometchatMessageComposerEditPreviewMessageTextAppearance,
                    CometChatTheme.getTextAppearanceCaption1Regular(context)
                ) ?: CometChatTheme.getTextAppearanceCaption1Regular(context),
                editPreviewBackgroundColor = typedArray?.getColor(
                    R.styleable.CometChatMessageComposer_cometchatMessageComposerEditPreviewBackgroundColor,
                    CometChatTheme.getBackgroundColor2(context)
                ) ?: CometChatTheme.getBackgroundColor2(context),
                editPreviewCornerRadius = typedArray?.getDimensionPixelSize(
                    R.styleable.CometChatMessageComposer_cometchatMessageComposerEditPreviewCornerRadius,
                    context.resources.getDimensionPixelSize(R.dimen.cometchat_corner_radius_2)
                ) ?: context.resources.getDimensionPixelSize(R.dimen.cometchat_corner_radius_2),
                editPreviewStrokeColor = typedArray?.getColor(
                    R.styleable.CometChatMessageComposer_cometchatMessageComposerEditPreviewStrokeColor,
                    CometChatTheme.getBorderColorLight(context)
                ) ?: CometChatTheme.getBorderColorLight(context),
                editPreviewStrokeWidth = typedArray?.getDimensionPixelSize(
                    R.styleable.CometChatMessageComposer_cometchatMessageComposerEditPreviewStrokeWidth,
                    context.resources.getDimensionPixelSize(R.dimen.cometchat_1dp)
                ) ?: context.resources.getDimensionPixelSize(R.dimen.cometchat_1dp),
                editPreviewCloseIcon = typedArray?.getDrawable(
                    R.styleable.CometChatMessageComposer_cometchatMessageComposerEditPreviewCloseIcon
                ),
                editPreviewCloseIconTint = typedArray?.getColor(
                    R.styleable.CometChatMessageComposer_cometchatMessageComposerEditPreviewCloseIconTint,
                    CometChatTheme.getIconTintSecondary(context)
                ) ?: CometChatTheme.getIconTintSecondary(context),

                // Message preview styling (for reply/quote)
                messagePreviewSeparatorColor = typedArray?.getColor(
                    R.styleable.CometChatMessageComposer_cometchatMessageComposerMessagePreviewSeparatorColor,
                    CometChatTheme.getPrimaryColor(context)
                ) ?: CometChatTheme.getPrimaryColor(context),
                messagePreviewTitleTextColor = typedArray?.getColor(
                    R.styleable.CometChatMessageComposer_cometchatMessageComposerMessagePreviewTitleTextColor,
                    CometChatTheme.getTextColorPrimary(context)
                ) ?: CometChatTheme.getTextColorPrimary(context),
                messagePreviewTitleTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatMessageComposer_cometchatMessageComposerMessagePreviewTitleTextAppearance,
                    CometChatTheme.getTextAppearanceCaption1Medium(context)
                ) ?: CometChatTheme.getTextAppearanceCaption1Medium(context),
                messagePreviewSubtitleTextColor = typedArray?.getColor(
                    R.styleable.CometChatMessageComposer_cometchatMessageComposerMessagePreviewSubtitleTextColor,
                    CometChatTheme.getTextColorSecondary(context)
                ) ?: CometChatTheme.getTextColorSecondary(context),
                messagePreviewSubtitleTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatMessageComposer_cometchatMessageComposerMessagePreviewSubtitleTextAppearance,
                    CometChatTheme.getTextAppearanceCaption1Regular(context)
                ) ?: CometChatTheme.getTextAppearanceCaption1Regular(context),
                messagePreviewBackgroundColor = typedArray?.getColor(
                    R.styleable.CometChatMessageComposer_cometchatMessageComposerMessagePreviewBackgroundColor,
                    CometChatTheme.getBackgroundColor2(context)
                ) ?: CometChatTheme.getBackgroundColor2(context),
                messagePreviewCornerRadius = typedArray?.getDimensionPixelSize(
                    R.styleable.CometChatMessageComposer_cometchatMessageComposerMessagePreviewCornerRadius,
                    context.resources.getDimensionPixelSize(R.dimen.cometchat_corner_radius_2)
                ) ?: context.resources.getDimensionPixelSize(R.dimen.cometchat_corner_radius_2),
                messagePreviewStrokeColor = typedArray?.getColor(
                    R.styleable.CometChatMessageComposer_cometchatMessageComposerMessagePreviewStrokeColor,
                    CometChatTheme.getBorderColorLight(context)
                ) ?: CometChatTheme.getBorderColorLight(context),
                messagePreviewStrokeWidth = typedArray?.getDimensionPixelSize(
                    R.styleable.CometChatMessageComposer_cometchatMessageComposerMessagePreviewStrokeWidth,
                    context.resources.getDimensionPixelSize(R.dimen.cometchat_1dp)
                ) ?: context.resources.getDimensionPixelSize(R.dimen.cometchat_1dp),
                messagePreviewCloseIcon = typedArray?.getDrawable(
                    R.styleable.CometChatMessageComposer_cometchatMessageComposerMessagePreviewCloseIcon
                ),
                messagePreviewCloseIconTint = typedArray?.getColor(
                    R.styleable.CometChatMessageComposer_cometchatMessageComposerMessagePreviewCloseIconTint,
                    CometChatTheme.getIconTintSecondary(context)
                ) ?: CometChatTheme.getIconTintSecondary(context),

                // Info panel styling
                infoIcon = typedArray?.getDrawable(
                    R.styleable.CometChatMessageComposer_cometchatMessageComposerInfoIcon
                ),
                infoIconTint = typedArray?.getColor(
                    R.styleable.CometChatMessageComposer_cometchatMessageComposerInfoIconTint,
                    CometChatTheme.getIconTintSecondary(context)
                ) ?: CometChatTheme.getIconTintSecondary(context),
                infoTextColor = typedArray?.getColor(
                    R.styleable.CometChatMessageComposer_cometchatMessageComposerInfoTextColor,
                    CometChatTheme.getTextColorSecondary(context)
                ) ?: CometChatTheme.getTextColorSecondary(context),
                infoTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatMessageComposer_cometchatMessageComposerInfoTextAppearance,
                    CometChatTheme.getTextAppearanceCaption1Regular(context)
                ) ?: CometChatTheme.getTextAppearanceCaption1Regular(context),
                infoBackgroundColor = typedArray?.getColor(
                    R.styleable.CometChatMessageComposer_cometchatMessageComposerInfoBackgroundColor,
                    CometChatTheme.getBackgroundColor2(context)
                ) ?: CometChatTheme.getBackgroundColor2(context),
                infoCornerRadius = typedArray?.getDimensionPixelSize(
                    R.styleable.CometChatMessageComposer_cometchatMessageComposerInfoCornerRadius,
                    context.resources.getDimensionPixelSize(R.dimen.cometchat_corner_radius_2)
                ) ?: context.resources.getDimensionPixelSize(R.dimen.cometchat_corner_radius_2),
                infoStrokeColor = typedArray?.getColor(
                    R.styleable.CometChatMessageComposer_cometchatMessageComposerInfoStrokeColor,
                    CometChatTheme.getBorderColorLight(context)
                ) ?: CometChatTheme.getBorderColorLight(context),
                infoStrokeWidth = typedArray?.getDimensionPixelSize(
                    R.styleable.CometChatMessageComposer_cometchatMessageComposerInfoStrokeWidth,
                    context.resources.getDimensionPixelSize(R.dimen.cometchat_1dp)
                ) ?: context.resources.getDimensionPixelSize(R.dimen.cometchat_1dp),

                // Input text styling
                inputTextColor = typedArray?.getColor(
                    R.styleable.CometChatMessageComposer_cometchatMessageComposerInputTextColor,
                    CometChatTheme.getTextColorPrimary(context)
                ) ?: CometChatTheme.getTextColorPrimary(context),
                inputTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatMessageComposer_cometchatMessageComposerInputTextAppearance,
                    CometChatTheme.getTextAppearanceBodyRegular(context)
                ) ?: CometChatTheme.getTextAppearanceBodyRegular(context),
                inputPlaceholderColor = typedArray?.getColor(
                    R.styleable.CometChatMessageComposer_cometchatMessageComposerInputPlaceholderColor,
                    CometChatTheme.getTextColorTertiary(context)
                ) ?: CometChatTheme.getTextColorTertiary(context),
                inputPlaceholderAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatMessageComposer_cometchatMessageComposerInputPlaceholderAppearance,
                    CometChatTheme.getTextAppearanceBodyRegular(context)
                ) ?: CometChatTheme.getTextAppearanceBodyRegular(context),

                // Rich text toolbar styling
                richTextToolbarBackgroundColor = typedArray?.getColor(
                    R.styleable.CometChatMessageComposer_cometchatMessageComposerRichTextToolbarBackgroundColor,
                    CometChatTheme.getBackgroundColor2(context)
                ) ?: CometChatTheme.getBackgroundColor2(context),
                richTextToolbarIconTint = typedArray?.getColor(
                    R.styleable.CometChatMessageComposer_cometchatMessageComposerRichTextToolbarIconTint,
                    CometChatTheme.getIconTintSecondary(context)
                ) ?: CometChatTheme.getIconTintSecondary(context),
                richTextToolbarActiveIconTint = typedArray?.getColor(
                    R.styleable.CometChatMessageComposer_cometchatMessageComposerRichTextToolbarActiveIconTint,
                    CometChatTheme.getPrimaryColor(context)
                ) ?: CometChatTheme.getPrimaryColor(context),
                richTextToolbarToggleIcon = typedArray?.getDrawable(
                    R.styleable.CometChatMessageComposer_cometchatMessageComposerRichTextToolbarToggleIcon
                ),
                richTextToolbarToggleIconTint = typedArray?.getColor(
                    R.styleable.CometChatMessageComposer_cometchatMessageComposerRichTextToolbarToggleIconTint,
                    CometChatTheme.getIconTintSecondary(context)
                ) ?: CometChatTheme.getIconTintSecondary(context),

                // Link edit dialog styling
                linkDialogBackgroundColor = typedArray?.getColor(
                    R.styleable.CometChatMessageComposer_cometchatMessageComposerLinkDialogBackgroundColor,
                    CometChatTheme.getBackgroundColor1(context)
                ) ?: CometChatTheme.getBackgroundColor1(context),
                linkDialogTitleTextColor = typedArray?.getColor(
                    R.styleable.CometChatMessageComposer_cometchatMessageComposerLinkDialogTitleTextColor,
                    CometChatTheme.getTextColorPrimary(context)
                ) ?: CometChatTheme.getTextColorPrimary(context),
                linkDialogTitleTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatMessageComposer_cometchatMessageComposerLinkDialogTitleTextAppearance,
                    CometChatTheme.getTextAppearanceHeading4Bold(context)
                ) ?: CometChatTheme.getTextAppearanceHeading4Bold(context),
                linkDialogInputBackgroundColor = typedArray?.getColor(
                    R.styleable.CometChatMessageComposer_cometchatMessageComposerLinkDialogInputBackgroundColor,
                    CometChatTheme.getBackgroundColor2(context)
                ) ?: CometChatTheme.getBackgroundColor2(context),
                linkDialogInputTextColor = typedArray?.getColor(
                    R.styleable.CometChatMessageComposer_cometchatMessageComposerLinkDialogInputTextColor,
                    CometChatTheme.getTextColorPrimary(context)
                ) ?: CometChatTheme.getTextColorPrimary(context),
                linkDialogInputTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatMessageComposer_cometchatMessageComposerLinkDialogInputTextAppearance,
                    CometChatTheme.getTextAppearanceBodyRegular(context)
                ) ?: CometChatTheme.getTextAppearanceBodyRegular(context),
                linkDialogButtonTextColor = typedArray?.getColor(
                    R.styleable.CometChatMessageComposer_cometchatMessageComposerLinkDialogButtonTextColor,
                    CometChatTheme.getPrimaryColor(context)
                ) ?: CometChatTheme.getPrimaryColor(context),
                linkDialogButtonTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatMessageComposer_cometchatMessageComposerLinkDialogButtonTextAppearance,
                    CometChatTheme.getTextAppearanceButtonMedium(context)
                ) ?: CometChatTheme.getTextAppearanceButtonMedium(context)
            )
        }
    }
}
