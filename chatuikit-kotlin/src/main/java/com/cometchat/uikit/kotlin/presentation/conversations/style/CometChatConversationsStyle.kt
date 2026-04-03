package com.cometchat.uikit.kotlin.presentation.conversations.style

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.StyleRes
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.presentation.shared.baseelements.avatar.CometChatAvatarStyle
import com.cometchat.uikit.kotlin.presentation.shared.baseelements.badgecount.CometChatBadgeCountStyle
import com.cometchat.uikit.kotlin.presentation.shared.baseelements.date.CometChatDateStyle
import com.cometchat.uikit.kotlin.presentation.shared.receipts.CometChatReceiptStyle
import com.cometchat.uikit.kotlin.presentation.shared.statusindicator.CometChatStatusIndicatorStyle
import com.cometchat.uikit.kotlin.presentation.shared.typingindicator.CometChatTypingIndicatorStyle
import com.cometchat.uikit.kotlin.shared.formatters.style.CometChatMentionStyle
import com.cometchat.uikit.kotlin.theme.CometChatTheme

/**
 * Style configuration for CometChatConversations component.
 */
data class CometChatConversationsStyle(
    // Container
    @ColorInt val backgroundColor: Int = 0,
    @ColorInt val strokeColor: Int = 0,
    @Dimension val strokeWidth: Int = 0,
    @Dimension val cornerRadius: Int = 0,
    
    // Toolbar
    @ColorInt val titleTextColor: Int = 0,
    @StyleRes val titleTextAppearance: Int = 0,
    val backIcon: Drawable? = null,
    @ColorInt val backIconTint: Int = 0,
    
    // Search box
    @ColorInt val searchBackgroundColor: Int = 0,
    @ColorInt val searchTextColor: Int = 0,
    @StyleRes val searchTextAppearance: Int = 0,
    @ColorInt val searchPlaceholderColor: Int = 0,
    @StyleRes val searchPlaceholderTextAppearance: Int = 0,
    val searchStartIcon: Drawable? = null,
    @ColorInt val searchStartIconTint: Int = 0,
    val searchEndIcon: Drawable? = null,
    @ColorInt val searchEndIconTint: Int = 0,
    @Dimension val searchCornerRadius: Int = 0,
    @Dimension val searchStrokeWidth: Int = 0,
    @ColorInt val searchStrokeColor: Int = 0,
    
    // Empty state
    @ColorInt val emptyStateTitleTextColor: Int = 0,
    @ColorInt val emptyStateSubtitleTextColor: Int = 0,
    @StyleRes val emptyStateTitleTextAppearance: Int = 0,
    @StyleRes val emptyStateSubtitleTextAppearance: Int = 0,
    val emptyStateIcon: Drawable? = null,
    @ColorInt val emptyStateIconTint: Int = 0,
    
    // Error state
    @ColorInt val errorStateTitleTextColor: Int = 0,
    @ColorInt val errorStateSubtitleTextColor: Int = 0,
    @StyleRes val errorStateTitleTextAppearance: Int = 0,
    @StyleRes val errorStateSubtitleTextAppearance: Int = 0,
    val errorStateIcon: Drawable? = null,
    @ColorInt val errorStateIconTint: Int = 0,
    @ColorInt val retryButtonBackgroundColor: Int = 0,
    @ColorInt val retryButtonTextColor: Int = 0,
    
    // Selection
    @Dimension val checkBoxStrokeWidth: Int = 0,
    @Dimension val checkBoxCornerRadius: Int = 0,
    @ColorInt val checkBoxStrokeColor: Int = 0,
    @ColorInt val checkBoxBackgroundColor: Int = 0,
    @ColorInt val checkBoxCheckedBackgroundColor: Int = 0,
    val checkBoxSelectIcon: Drawable? = null,
    @ColorInt val checkBoxSelectIconTint: Int = 0,
    val discardSelectionIcon: Drawable? = null,
    @ColorInt val discardSelectionIconTint: Int = 0,
    val submitSelectionIcon: Drawable? = null,
    @ColorInt val submitSelectionIconTint: Int = 0,
    
    // Delete option
    @ColorInt val deleteOptionTextColor: Int = 0,
    @StyleRes val deleteOptionTextAppearance: Int = 0,
    val deleteOptionIcon: Drawable? = null,
    @ColorInt val deleteOptionIconTint: Int = 0,
    
    // Separator
    @Dimension val separatorHeight: Int = 0,
    @ColorInt val separatorColor: Int = 0,
    
    // Nested component style resource IDs
    @StyleRes val avatarStyleResId: Int = 0,
    @StyleRes val statusIndicatorStyleResId: Int = 0,
    @StyleRes val dateStyleResId: Int = 0,
    @StyleRes val badgeStyleResId: Int = 0,
    @StyleRes val receiptStyleResId: Int = 0,
    @StyleRes val typingIndicatorStyleResId: Int = 0,
    @StyleRes val mentionsStyleResId: Int = 0,
    @StyleRes val optionListStyleResId: Int = 0,
    
    // Item style
    val itemStyle: CometChatConversationListItemStyle = CometChatConversationListItemStyle()
) {
    companion object {
        /**
         * Creates a default style by extracting values from the theme's cometchatConversationsStyle.
         *
         * @param context The context to access theme resources
         * @return A CometChatConversationsStyle with values from theme or fallback defaults
         */
        fun default(context: Context): CometChatConversationsStyle {
            return extractFromThemeStyle(context, R.attr.cometchatConversationsStyle)
        }

        /**
         * Extracts the conversation list style from a theme style attribute.
         *
         * @param context The context to access theme resources
         * @param themeStyleAttr The attribute reference to the conversations style in theme
         * @return A CometChatConversationsStyle with values from the theme style
         */
        private fun extractFromThemeStyle(context: Context, themeStyleAttr: Int): CometChatConversationsStyle {
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
         * @return A CometChatConversationsStyle with values from the style resource or defaults
         */
        private fun extractFromStyleResource(context: Context, styleResId: Int): CometChatConversationsStyle {
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

        /**
         * Creates a style by extracting values from XML TypedArray.
         *
         * This method handles TypedArray recycling internally using try-finally.
         * All attributes are extracted from the corresponding attr_cometchat_conversations.xml file.
         *
         * @param context The Android context for accessing theme resources
         * @param typedArray The TypedArray containing XML attribute values (will be recycled)
         * @return A CometChatConversationsStyle with values from XML or theme defaults
         */
        fun fromTypedArray(context: Context, typedArray: TypedArray): CometChatConversationsStyle {
            return try {
                extractFromTypedArray(context, typedArray)
            } finally {
                typedArray.recycle()
            }
        }

        /**
         * Core extraction method that reads style values from a TypedArray.
         * Does NOT handle recycling - caller is responsible for recycling the TypedArray.
         *
         * @param context The Android context for accessing theme resources
         * @param typedArray The TypedArray to extract from, or null for defaults only
         * @return A CometChatConversationsStyle with extracted values or theme defaults
         */
        private fun extractFromTypedArray(context: Context, typedArray: TypedArray?): CometChatConversationsStyle {
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
            val mentionsStyleResId = typedArray?.getResourceId(
                R.styleable.CometChatConversations_cometchatConversationsMentionsStyle, 0
            ) ?: 0
            val optionListStyleResId = typedArray?.getResourceId(
                R.styleable.CometChatConversations_cometchatConversationsOptionListStyle, 0
            ) ?: 0

            // Create nested component styles from resource IDs or use defaults
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

            // Note: MentionStyle doesn't have a fromTypedArray method as it uses Typeface
            // which cannot be directly extracted from XML attributes
            val mentionStyle = CometChatMentionStyle.default(context)

            // Create item style from the same TypedArray (item-specific attributes)
            // Include nested component styles that were extracted above
            val itemStyle = CometChatConversationListItemStyle(
                backgroundColor = typedArray?.getColor(
                    R.styleable.CometChatConversations_cometchatConversationsItemBackgroundColor,
                    Color.TRANSPARENT
                ) ?: Color.TRANSPARENT,
                selectedBackgroundColor = typedArray?.getColor(
                    R.styleable.CometChatConversations_cometchatConversationsItemSelectedBackgroundColor,
                    CometChatTheme.getBackgroundColor3(context)
                ) ?: CometChatTheme.getBackgroundColor3(context),
                titleTextColor = typedArray?.getColor(
                    R.styleable.CometChatConversations_cometchatConversationsItemTitleTextColor,
                    CometChatTheme.getTextColorPrimary(context)
                ) ?: CometChatTheme.getTextColorPrimary(context),
                titleTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatConversations_cometchatConversationsItemTitleTextAppearance,
                    CometChatTheme.getTextAppearanceHeading4Medium(context)
                ) ?: CometChatTheme.getTextAppearanceHeading4Medium(context),
                subtitleTextColor = typedArray?.getColor(
                    R.styleable.CometChatConversations_cometchatConversationsItemSubtitleTextColor,
                    CometChatTheme.getTextColorSecondary(context)
                ) ?: CometChatTheme.getTextColorSecondary(context),
                subtitleTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatConversations_cometchatConversationsItemSubtitleTextAppearance,
                    CometChatTheme.getTextAppearanceBodyRegular(context)
                ) ?: CometChatTheme.getTextAppearanceBodyRegular(context),
                messageTypeIconTint = typedArray?.getColor(
                    R.styleable.CometChatConversations_cometchatConversationsItemMessageTypeIconTint,
                    CometChatTheme.getTextColorSecondary(context)
                ) ?: CometChatTheme.getTextColorSecondary(context),
                separatorColor = typedArray?.getColor(
                    R.styleable.CometChatConversations_cometchatConversationsSeparatorColor,
                    CometChatTheme.getStrokeColorLight(context)
                ) ?: CometChatTheme.getStrokeColorLight(context),
                separatorHeight = typedArray?.getDimensionPixelSize(
                    R.styleable.CometChatConversations_cometchatConversationsSeparatorHeight, 1
                ) ?: 1,
                // Include nested component styles
                avatarStyle = avatarStyle,
                statusIndicatorStyle = statusIndicatorStyle,
                dateStyle = dateStyle,
                badgeCountStyle = badgeCountStyle,
                receiptStyle = receiptStyle,
                typingIndicatorStyle = typingIndicatorStyle,
                mentionStyle = mentionStyle
            )

            return CometChatConversationsStyle(
                // Container
                backgroundColor = typedArray?.getColor(
                    R.styleable.CometChatConversations_cometchatConversationsBackgroundColor,
                    CometChatTheme.getBackgroundColor1(context)
                ) ?: CometChatTheme.getBackgroundColor1(context),
                strokeColor = typedArray?.getColor(
                    R.styleable.CometChatConversations_cometchatConversationsStrokeColor, 0
                ) ?: 0,
                strokeWidth = typedArray?.getDimensionPixelSize(
                    R.styleable.CometChatConversations_cometchatConversationsStrokeWidth, 0
                ) ?: 0,
                cornerRadius = typedArray?.getDimensionPixelSize(
                    R.styleable.CometChatConversations_cometchatConversationsCornerRadius, 0
                ) ?: 0,

                // Toolbar
                titleTextColor = typedArray?.getColor(
                    R.styleable.CometChatConversations_cometchatConversationsTitleTextColor,
                    CometChatTheme.getTextColorPrimary(context)
                ) ?: CometChatTheme.getTextColorPrimary(context),
                titleTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatConversations_cometchatConversationsTitleTextAppearance,
                    CometChatTheme.getTextAppearanceHeading1Bold(context)
                ) ?: CometChatTheme.getTextAppearanceHeading1Bold(context),
                backIcon = typedArray?.getDrawable(
                    R.styleable.CometChatConversations_cometchatConversationsBackIcon
                ),
                backIconTint = typedArray?.getColor(
                    R.styleable.CometChatConversations_cometchatConversationsBackIconTint,
                    CometChatTheme.getIconTintPrimary(context)
                ) ?: CometChatTheme.getIconTintPrimary(context),

                // Search box
                searchBackgroundColor = typedArray?.getColor(
                    R.styleable.CometChatConversations_cometchatConversationsSearchInputBackgroundColor,
                    CometChatTheme.getBackgroundColor3(context)
                ) ?: CometChatTheme.getBackgroundColor3(context),
                searchTextColor = typedArray?.getColor(
                    R.styleable.CometChatConversations_cometchatConversationsSearchInputTextColor,
                    CometChatTheme.getTextColorPrimary(context)
                ) ?: CometChatTheme.getTextColorPrimary(context),
                searchTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatConversations_cometchatConversationsSearchInputTextAppearance,
                    CometChatTheme.getTextAppearanceHeading4Regular(context)
                ) ?: CometChatTheme.getTextAppearanceHeading4Regular(context),
                searchPlaceholderColor = typedArray?.getColor(
                    R.styleable.CometChatConversations_cometchatConversationsSearchInputPlaceHolderTextColor,
                    CometChatTheme.getTextColorTertiary(context)
                ) ?: CometChatTheme.getTextColorTertiary(context),
                searchPlaceholderTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatConversations_cometchatConversationsSearchInputPlaceHolderTextAppearance,
                    CometChatTheme.getTextAppearanceHeading4Regular(context)
                ) ?: CometChatTheme.getTextAppearanceHeading4Regular(context),
                searchStartIcon = typedArray?.getDrawable(
                    R.styleable.CometChatConversations_cometchatConversationsSearchInputStartIcon
                ),
                searchStartIconTint = typedArray?.getColor(
                    R.styleable.CometChatConversations_cometchatConversationsSearchInputStartIconTint,
                    CometChatTheme.getIconTintSecondary(context)
                ) ?: CometChatTheme.getIconTintSecondary(context),
                searchEndIcon = typedArray?.getDrawable(
                    R.styleable.CometChatConversations_cometchatConversationsSearchInputEndIcon
                ),
                searchEndIconTint = typedArray?.getColor(
                    R.styleable.CometChatConversations_cometchatConversationsSearchInputEndIconTint,
                    CometChatTheme.getIconTintSecondary(context)
                ) ?: CometChatTheme.getIconTintSecondary(context),
                searchCornerRadius = typedArray?.getDimensionPixelSize(
                    R.styleable.CometChatConversations_cometchatConversationsSearchInputCornerRadius,
                    context.resources.getDimensionPixelSize(R.dimen.cometchat_radius_max)
                ) ?: context.resources.getDimensionPixelSize(R.dimen.cometchat_radius_max),
                searchStrokeWidth = typedArray?.getDimensionPixelSize(
                    R.styleable.CometChatConversations_cometchatConversationsSearchInputStrokeWidth, 0
                ) ?: 0,
                searchStrokeColor = typedArray?.getColor(
                    R.styleable.CometChatConversations_cometchatConversationsSearchInputStrokeColor, 0
                ) ?: 0,

                // Empty state
                emptyStateTitleTextColor = typedArray?.getColor(
                    R.styleable.CometChatConversations_cometchatConversationsEmptyStateTitleTextColor,
                    CometChatTheme.getTextColorPrimary(context)
                ) ?: CometChatTheme.getTextColorPrimary(context),
                emptyStateSubtitleTextColor = typedArray?.getColor(
                    R.styleable.CometChatConversations_cometchatConversationsEmptyStateSubtitleTextColor,
                    CometChatTheme.getTextColorSecondary(context)
                ) ?: CometChatTheme.getTextColorSecondary(context),
                emptyStateTitleTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatConversations_cometchatConversationsEmptyStateTextTitleAppearance,
                    CometChatTheme.getTextAppearanceHeading3Bold(context)
                ) ?: CometChatTheme.getTextAppearanceHeading3Bold(context),
                emptyStateSubtitleTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatConversations_cometchatConversationsEmptyStateTextSubtitleAppearance,
                    CometChatTheme.getTextAppearanceBodyRegular(context)
                ) ?: CometChatTheme.getTextAppearanceBodyRegular(context),

                // Error state
                errorStateTitleTextColor = typedArray?.getColor(
                    R.styleable.CometChatConversations_cometchatConversationsErrorStateTitleTextColor,
                    CometChatTheme.getTextColorPrimary(context)
                ) ?: CometChatTheme.getTextColorPrimary(context),
                errorStateSubtitleTextColor = typedArray?.getColor(
                    R.styleable.CometChatConversations_cometchatConversationsErrorStateSubtitleTextColor,
                    CometChatTheme.getTextColorSecondary(context)
                ) ?: CometChatTheme.getTextColorSecondary(context),
                errorStateTitleTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatConversations_cometchatConversationsErrorStateTextTitleAppearance,
                    CometChatTheme.getTextAppearanceHeading3Bold(context)
                ) ?: CometChatTheme.getTextAppearanceHeading3Bold(context),
                errorStateSubtitleTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatConversations_cometchatConversationsErrorStateTextSubtitleAppearance,
                    CometChatTheme.getTextAppearanceBodyRegular(context)
                ) ?: CometChatTheme.getTextAppearanceBodyRegular(context),

                // Selection / Checkbox
                checkBoxStrokeWidth = typedArray?.getDimensionPixelSize(
                    R.styleable.CometChatConversations_cometchatConversationsCheckBoxStrokeWidth, 0
                ) ?: 0,
                checkBoxCornerRadius = typedArray?.getDimensionPixelSize(
                    R.styleable.CometChatConversations_cometchatConversationsCheckBoxCornerRadius, 0
                ) ?: 0,
                checkBoxStrokeColor = typedArray?.getColor(
                    R.styleable.CometChatConversations_cometchatConversationsCheckBoxStrokeColor,
                    CometChatTheme.getStrokeColorDefault(context)
                ) ?: CometChatTheme.getStrokeColorDefault(context),
                checkBoxBackgroundColor = typedArray?.getColor(
                    R.styleable.CometChatConversations_cometchatConversationsCheckBoxBackgroundColor,
                    CometChatTheme.getBackgroundColor1(context)
                ) ?: CometChatTheme.getBackgroundColor1(context),
                checkBoxCheckedBackgroundColor = typedArray?.getColor(
                    R.styleable.CometChatConversations_cometchatConversationsCheckBoxCheckedBackgroundColor,
                    CometChatTheme.getPrimaryColor(context)
                ) ?: CometChatTheme.getPrimaryColor(context),
                checkBoxSelectIcon = typedArray?.getDrawable(
                    R.styleable.CometChatConversations_cometchatConversationsCheckBoxSelectIcon
                ),
                checkBoxSelectIconTint = typedArray?.getColor(
                    R.styleable.CometChatConversations_cometchatConversationsCheckBoxSelectIconTint,
                    CometChatTheme.getColorWhite(context)
                ) ?: CometChatTheme.getColorWhite(context),
                discardSelectionIcon = typedArray?.getDrawable(
                    R.styleable.CometChatConversations_cometchatConversationsDiscardSelectionIcon
                ),
                discardSelectionIconTint = typedArray?.getColor(
                    R.styleable.CometChatConversations_cometchatConversationsDiscardSelectionIconTint,
                    CometChatTheme.getIconTintPrimary(context)
                ) ?: CometChatTheme.getIconTintPrimary(context),
                submitSelectionIcon = typedArray?.getDrawable(
                    R.styleable.CometChatConversations_cometchatConversationsSubmitSelectionIcon
                ),
                submitSelectionIconTint = typedArray?.getColor(
                    R.styleable.CometChatConversations_cometchatConversationsSubmitSelectionIconTint,
                    CometChatTheme.getPrimaryColor(context)
                ) ?: CometChatTheme.getPrimaryColor(context),

                // Delete option
                deleteOptionTextColor = typedArray?.getColor(
                    R.styleable.CometChatConversations_cometchatConversationsDeleteOptionTextColor,
                    CometChatTheme.getErrorColor(context)
                ) ?: CometChatTheme.getErrorColor(context),
                deleteOptionTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatConversations_cometchatConversationsDeleteOptionTextAppearance,
                    CometChatTheme.getTextAppearanceBodyMedium(context)
                ) ?: CometChatTheme.getTextAppearanceBodyMedium(context),
                deleteOptionIcon = typedArray?.getDrawable(
                    R.styleable.CometChatConversations_cometchatConversationsDeleteOptionIcon
                ),
                deleteOptionIconTint = typedArray?.getColor(
                    R.styleable.CometChatConversations_cometchatConversationsDeleteOptionIconTint,
                    CometChatTheme.getErrorColor(context)
                ) ?: CometChatTheme.getErrorColor(context),

                // Separator
                separatorHeight = typedArray?.getDimensionPixelSize(
                    R.styleable.CometChatConversations_cometchatConversationsSeparatorHeight, 1
                ) ?: 1,
                separatorColor = typedArray?.getColor(
                    R.styleable.CometChatConversations_cometchatConversationsSeparatorColor,
                    CometChatTheme.getStrokeColorLight(context)
                ) ?: CometChatTheme.getStrokeColorLight(context),

                // Nested component style resource IDs
                avatarStyleResId = avatarStyleResId,
                statusIndicatorStyleResId = statusIndicatorStyleResId,
                dateStyleResId = dateStyleResId,
                badgeStyleResId = badgeStyleResId,
                receiptStyleResId = receiptStyleResId,
                typingIndicatorStyleResId = typingIndicatorStyleResId,
                mentionsStyleResId = mentionsStyleResId,
                optionListStyleResId = optionListStyleResId,

                // Item style
                itemStyle = itemStyle
            )
        }
    }
}
