package com.cometchat.uikit.kotlin.presentation.conversations.style

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.StyleRes
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.theme.CometChatTheme
import com.cometchat.uikit.kotlin.presentation.shared.baseelements.avatar.CometChatAvatarStyle
import com.cometchat.uikit.kotlin.presentation.shared.baseelements.badgecount.CometChatBadgeCountStyle
import com.cometchat.uikit.kotlin.presentation.shared.baseelements.date.CometChatDateStyle
import com.cometchat.uikit.kotlin.presentation.shared.receipts.CometChatReceiptStyle
import com.cometchat.uikit.kotlin.presentation.shared.statusindicator.CometChatStatusIndicatorStyle
import com.cometchat.uikit.kotlin.presentation.shared.typingindicator.CometChatTypingIndicatorStyle
import com.cometchat.uikit.kotlin.shared.formatters.style.CometChatMentionStyle

/**
 * Style configuration for CometChatConversationListItem component.
 */
data class CometChatConversationListItemStyle(
    // Background
    @ColorInt val backgroundColor: Int = 0,
    @ColorInt val selectedBackgroundColor: Int = 0,
    
    // Title
    @ColorInt val titleTextColor: Int = 0,
    @StyleRes val titleTextAppearance: Int = 0,
    
    // Subtitle
    @ColorInt val subtitleTextColor: Int = 0,
    @StyleRes val subtitleTextAppearance: Int = 0,
    
    // Message type icon (for photo, video, audio, document icons in subtitle)
    @ColorInt val messageTypeIconTint: Int = 0,
    
    // Separator
    @ColorInt val separatorColor: Int = 0,
    @Dimension val separatorHeight: Int = 0,
    
    // Component styles
    val avatarStyle: CometChatAvatarStyle = CometChatAvatarStyle(),
    val statusIndicatorStyle: CometChatStatusIndicatorStyle = CometChatStatusIndicatorStyle(),
    val dateStyle: CometChatDateStyle = CometChatDateStyle(),
    val badgeCountStyle: CometChatBadgeCountStyle = CometChatBadgeCountStyle(),
    val receiptStyle: CometChatReceiptStyle = CometChatReceiptStyle(),
    val typingIndicatorStyle: CometChatTypingIndicatorStyle = CometChatTypingIndicatorStyle(),
    
    // Mention style for @mentions in conversation subtitles
    val mentionStyle: CometChatMentionStyle? = null
) {
    companion object {
        /**
         * Creates a default style by extracting values from the theme's cometchatConversationsStyle.
         */
        fun default(context: Context): CometChatConversationListItemStyle {
            return extractFromThemeStyle(context)
        }

        private fun extractFromThemeStyle(context: Context): CometChatConversationListItemStyle {
            val themeTypedArray = context.obtainStyledAttributes(intArrayOf(R.attr.cometchatConversationsStyle))
            val styleResId = themeTypedArray.getResourceId(0, 0)
            themeTypedArray.recycle()
            return extractFromStyleResource(context, styleResId)
        }

        private fun extractFromStyleResource(context: Context, styleResId: Int): CometChatConversationListItemStyle {
            if (styleResId == 0) {
                return extractFromTypedArray(context, null)
            }
            val typedArray = context.obtainStyledAttributes(styleResId, R.styleable.CometChatConversations)
            return try {
                extractFromTypedArray(context, typedArray)
            } finally {
                typedArray.recycle()
            }
        }

        fun fromTypedArray(context: Context, typedArray: TypedArray): CometChatConversationListItemStyle {
            return try {
                extractFromTypedArray(context, typedArray)
            } finally {
                typedArray.recycle()
            }
        }

        private fun extractFromTypedArray(context: Context, typedArray: TypedArray?): CometChatConversationListItemStyle {
            // Extract nested style resource IDs
            val avatarStyleResId = typedArray?.getResourceId(
                R.styleable.CometChatConversations_cometchatConversationsAvatarStyle, 0
            ) ?: 0
            val statusIndicatorStyleResId = typedArray?.getResourceId(
                R.styleable.CometChatConversations_cometchatConversationsStatusIndicatorStyle, 0
            ) ?: 0
            val dateStyleResId = typedArray?.getResourceId(
                R.styleable.CometChatConversations_cometchatConversationsDateStyle, 0
            ) ?: 0
            val badgeStyleResId = typedArray?.getResourceId(
                R.styleable.CometChatConversations_cometchatConversationsBadgeStyle, 0
            ) ?: 0
            val receiptStyleResId = typedArray?.getResourceId(
                R.styleable.CometChatConversations_cometchatConversationsReceiptStyle, 0
            ) ?: 0
            val typingIndicatorStyleResId = typedArray?.getResourceId(
                R.styleable.CometChatConversations_cometchatConversationsTypingIndicatorStyle, 0
            ) ?: 0

            // Create nested styles from resource IDs or use defaults
            val avatarStyle = if (avatarStyleResId != 0) {
                val avatarTypedArray = context.theme.obtainStyledAttributes(
                    avatarStyleResId, R.styleable.CometChatAvatar
                )
                CometChatAvatarStyle.fromTypedArray(context, avatarTypedArray)
            } else {
                CometChatAvatarStyle.default(context)
            }

            val statusIndicatorStyle = if (statusIndicatorStyleResId != 0) {
                val statusIndicatorTypedArray = context.theme.obtainStyledAttributes(
                    statusIndicatorStyleResId, R.styleable.CometChatStatusIndicator
                )
                CometChatStatusIndicatorStyle.fromTypedArray(context, statusIndicatorTypedArray)
            } else {
                CometChatStatusIndicatorStyle.default(context)
            }

            val dateStyle = if (dateStyleResId != 0) {
                val dateTypedArray = context.theme.obtainStyledAttributes(
                    dateStyleResId, R.styleable.CometChatDate
                )
                CometChatDateStyle.fromTypedArray(context, dateTypedArray)
            } else {
                CometChatDateStyle.default(context)
            }

            val badgeCountStyle = if (badgeStyleResId != 0) {
                val badgeTypedArray = context.theme.obtainStyledAttributes(
                    badgeStyleResId, R.styleable.CometChatBadge
                )
                CometChatBadgeCountStyle.fromTypedArray(context, badgeTypedArray)
            } else {
                CometChatBadgeCountStyle.default(context)
            }

            val receiptStyle = if (receiptStyleResId != 0) {
                val receiptTypedArray = context.theme.obtainStyledAttributes(
                    receiptStyleResId, R.styleable.CometChatMessageReceipt
                )
                CometChatReceiptStyle.fromTypedArray(context, receiptTypedArray)
            } else {
                CometChatReceiptStyle.default(context)
            }

            val typingIndicatorStyle = if (typingIndicatorStyleResId != 0) {
                val typingIndicatorTypedArray = context.theme.obtainStyledAttributes(
                    typingIndicatorStyleResId, R.styleable.CometChatTypingIndicator
                )
                CometChatTypingIndicatorStyle.fromTypedArray(context, typingIndicatorTypedArray)
            } else {
                CometChatTypingIndicatorStyle.default(context)
            }

            val mentionStyle = CometChatMentionStyle.default(context)

            return CometChatConversationListItemStyle(
                // Background styling
                backgroundColor = typedArray?.getColor(
                    R.styleable.CometChatConversations_cometchatConversationsItemBackgroundColor,
                    Color.TRANSPARENT
                ) ?: Color.TRANSPARENT,
                selectedBackgroundColor = typedArray?.getColor(
                    R.styleable.CometChatConversations_cometchatConversationsItemSelectedBackgroundColor,
                    CometChatTheme.getBackgroundColor3(context)
                ) ?: CometChatTheme.getBackgroundColor3(context),

                // Title styling
                titleTextColor = typedArray?.getColor(
                    R.styleable.CometChatConversations_cometchatConversationsItemTitleTextColor,
                    CometChatTheme.getTextColorPrimary(context)
                ) ?: CometChatTheme.getTextColorPrimary(context),
                titleTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatConversations_cometchatConversationsItemTitleTextAppearance,
                    CometChatTheme.getTextAppearanceHeading4Medium(context)
                ) ?: CometChatTheme.getTextAppearanceHeading4Medium(context),

                // Subtitle styling
                subtitleTextColor = typedArray?.getColor(
                    R.styleable.CometChatConversations_cometchatConversationsItemSubtitleTextColor,
                    CometChatTheme.getTextColorSecondary(context)
                ) ?: CometChatTheme.getTextColorSecondary(context),
                subtitleTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatConversations_cometchatConversationsItemSubtitleTextAppearance,
                    CometChatTheme.getTextAppearanceBodyRegular(context)
                ) ?: CometChatTheme.getTextAppearanceBodyRegular(context),

                // Message type icon styling
                messageTypeIconTint = typedArray?.getColor(
                    R.styleable.CometChatConversations_cometchatConversationsItemMessageTypeIconTint,
                    CometChatTheme.getTextColorSecondary(context)
                ) ?: CometChatTheme.getTextColorSecondary(context),

                // Separator styling
                separatorColor = typedArray?.getColor(
                    R.styleable.CometChatConversations_cometchatConversationsSeparatorColor,
                    CometChatTheme.getStrokeColorLight(context)
                ) ?: CometChatTheme.getStrokeColorLight(context),
                separatorHeight = typedArray?.getDimensionPixelSize(
                    R.styleable.CometChatConversations_cometchatConversationsSeparatorHeight, 1
                ) ?: 1,

                // Component styles
                avatarStyle = avatarStyle,
                statusIndicatorStyle = statusIndicatorStyle,
                dateStyle = dateStyle,
                badgeCountStyle = badgeCountStyle,
                receiptStyle = receiptStyle,
                typingIndicatorStyle = typingIndicatorStyle,
                mentionStyle = mentionStyle
            )
        }
    }
}
