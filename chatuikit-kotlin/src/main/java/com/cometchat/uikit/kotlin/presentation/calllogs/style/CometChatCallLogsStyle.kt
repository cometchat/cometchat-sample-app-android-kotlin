package com.cometchat.uikit.kotlin.presentation.calllogs.style

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.StyleRes
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.theme.CometChatTheme

/**
 * Style configuration for CometChatCallLogs component.
 * Contains all visual styling properties for the call logs screen.
 * 
 * Follows the same pattern as CometChatConversationsStyle - state styling
 * is handled inline rather than through separate state style classes.
 */
data class CometChatCallLogsStyle(
    // Container styling
    @ColorInt val backgroundColor: Int = 0,
    @ColorInt val strokeColor: Int = Color.TRANSPARENT,
    @Dimension val strokeWidth: Int = 0,
    @Dimension val cornerRadius: Int = 0,
    
    // Toolbar styling
    @ColorInt val titleTextColor: Int = 0,
    @StyleRes val titleTextAppearance: Int = 0,
    val backIcon: Drawable? = null,
    @ColorInt val backIconTint: Int = 0,
    @ColorInt val toolbarSeparatorColor: Int = 0,
    @Dimension val toolbarSeparatorHeight: Int = 1,
    val showToolbarSeparator: Boolean = true,
    
    // Empty state styling (inline, not separate class)
    @ColorInt val emptyStateTitleTextColor: Int = 0,
    @ColorInt val emptyStateSubtitleTextColor: Int = 0,
    @StyleRes val emptyStateTitleTextAppearance: Int = 0,
    @StyleRes val emptyStateSubtitleTextAppearance: Int = 0,
    val emptyStateIcon: Drawable? = null,
    @ColorInt val emptyStateIconTint: Int = 0,
    
    // Error state styling (inline, not separate class)
    @ColorInt val errorStateTitleTextColor: Int = 0,
    @ColorInt val errorStateSubtitleTextColor: Int = 0,
    @StyleRes val errorStateTitleTextAppearance: Int = 0,
    @StyleRes val errorStateSubtitleTextAppearance: Int = 0,
    val errorStateIcon: Drawable? = null,
    @ColorInt val errorStateIconTint: Int = 0,
    @ColorInt val retryButtonBackgroundColor: Int = 0,
    @ColorInt val retryButtonTextColor: Int = 0,
    
    // Item styling - NOTE: Default is empty, always use default(context) or fromTypedArray() to get proper icons
    val itemStyle: CometChatCallLogsListItemStyle = CometChatCallLogsListItemStyle()
) {
    /**
     * Returns a copy of this style with itemStyle properly initialized if it has empty icons.
     * Use this when you need to ensure icons are set.
     */
    fun withDefaultItemStyle(context: Context): CometChatCallLogsStyle {
        return if (itemStyle.incomingCallIcon == 0) {
            copy(itemStyle = CometChatCallLogsListItemStyle.default(context))
        } else {
            this
        }
    }
    companion object {
        /**
         * Creates a default style using CometChatTheme values.
         */
        fun default(context: Context): CometChatCallLogsStyle {
            return extractFromTypedArray(context, null)
        }
        
        /**
         * Creates a style by extracting values from the theme's cometchatCallLogsStyle.
         */
        fun fromTheme(context: Context): CometChatCallLogsStyle {
            val themeTypedArray = context.obtainStyledAttributes(intArrayOf(R.attr.cometchatCallLogsStyle))
            val styleResId = themeTypedArray.getResourceId(0, 0)
            themeTypedArray.recycle()
            return fromStyleResource(context, styleResId)
        }
        
        private fun fromStyleResource(context: Context, styleResId: Int): CometChatCallLogsStyle {
            if (styleResId == 0) {
                return extractFromTypedArray(context, null)
            }
            val typedArray = context.obtainStyledAttributes(styleResId, R.styleable.CometChatCallLogs)
            return try {
                extractFromTypedArray(context, typedArray)
            } finally {
                typedArray.recycle()
            }
        }
        
        /**
         * Creates a style from a TypedArray.
         * The TypedArray will be recycled after extraction.
         */
        fun fromTypedArray(context: Context, typedArray: TypedArray): CometChatCallLogsStyle {
            return try {
                extractFromTypedArray(context, typedArray)
            } finally {
                typedArray.recycle()
            }
        }

        
        private fun extractFromTypedArray(
            context: Context,
            typedArray: TypedArray?
        ): CometChatCallLogsStyle {
            // Extract nested style resource IDs for item components
            val avatarStyleResId = typedArray?.getResourceId(
                R.styleable.CometChatCallLogs_cometchatCallLogsAvatarStyle, 0
            ) ?: 0
            val dateStyleResId = typedArray?.getResourceId(
                R.styleable.CometChatCallLogs_cometchatCallLogsDateStyle, 0
            ) ?: 0
            
            // Create nested component styles
            val avatarStyle = if (avatarStyleResId != 0) {
                val avatarTypedArray = context.theme.obtainStyledAttributes(
                    avatarStyleResId, R.styleable.CometChatAvatar
                )
                com.cometchat.uikit.kotlin.presentation.shared.baseelements.avatar.CometChatAvatarStyle.fromTypedArray(context, avatarTypedArray)
            } else {
                com.cometchat.uikit.kotlin.presentation.shared.baseelements.avatar.CometChatAvatarStyle.default(context)
            }
            
            val dateStyle = if (dateStyleResId != 0) {
                val dateTypedArray = context.theme.obtainStyledAttributes(
                    dateStyleResId, R.styleable.CometChatDate
                )
                com.cometchat.uikit.kotlin.presentation.shared.baseelements.date.CometChatDateStyle.fromTypedArray(context, dateTypedArray)
            } else {
                com.cometchat.uikit.kotlin.presentation.shared.baseelements.date.CometChatDateStyle.default(context)
            }
            
            // Create item style from the same TypedArray
            val itemStyle = CometChatCallLogsListItemStyle(
                backgroundColor = Color.TRANSPARENT,
                titleTextColor = typedArray?.getColor(
                    R.styleable.CometChatCallLogs_cometchatCallLogsItemTitleTextColor,
                    CometChatTheme.getTextColorPrimary(context)
                ) ?: CometChatTheme.getTextColorPrimary(context),
                titleTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatCallLogs_cometchatCallLogsItemTitleTextAppearance,
                    CometChatTheme.getTextAppearanceHeading4Medium(context)
                ) ?: CometChatTheme.getTextAppearanceHeading4Medium(context),
                missedCallTitleColor = typedArray?.getColor(
                    R.styleable.CometChatCallLogs_cometchatCallLogsItemMissedCallTitleColor,
                    CometChatTheme.getErrorColor(context)
                ) ?: CometChatTheme.getErrorColor(context),
                subtitleTextColor = typedArray?.getColor(
                    R.styleable.CometChatCallLogs_cometchatCallLogsItemSubtitleTextColor,
                    CometChatTheme.getTextColorSecondary(context)
                ) ?: CometChatTheme.getTextColorSecondary(context),
                subtitleTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatCallLogs_cometchatCallLogsItemSubtitleTextAppearance,
                    CometChatTheme.getTextAppearanceBodyRegular(context)
                ) ?: CometChatTheme.getTextAppearanceBodyRegular(context),
                // Direction icons
                incomingCallIcon = typedArray?.getResourceId(
                    R.styleable.CometChatCallLogs_cometchatCallLogsItemIncomingCallIcon,
                    R.drawable.cometchat_ic_incoming_call
                ) ?: R.drawable.cometchat_ic_incoming_call,
                incomingCallIconTint = typedArray?.getColor(
                    R.styleable.CometChatCallLogs_cometchatCallLogsItemIncomingCallIconTint,
                    CometChatTheme.getSuccessColor(context)
                ) ?: CometChatTheme.getSuccessColor(context),
                outgoingCallIcon = typedArray?.getResourceId(
                    R.styleable.CometChatCallLogs_cometchatCallLogsItemOutgoingCallIcon,
                    R.drawable.cometchat_ic_outgoing_call
                ) ?: R.drawable.cometchat_ic_outgoing_call,
                outgoingCallIconTint = typedArray?.getColor(
                    R.styleable.CometChatCallLogs_cometchatCallLogsItemOutgoingCallIconTint,
                    CometChatTheme.getSuccessColor(context)
                ) ?: CometChatTheme.getSuccessColor(context),
                missedCallIcon = typedArray?.getResourceId(
                    R.styleable.CometChatCallLogs_cometchatCallLogsItemMissedCallIcon,
                    R.drawable.cometchat_ic_missed_call
                ) ?: R.drawable.cometchat_ic_missed_call,
                missedCallIconTint = typedArray?.getColor(
                    R.styleable.CometChatCallLogs_cometchatCallLogsItemMissedCallIconTint,
                    CometChatTheme.getErrorColor(context)
                ) ?: CometChatTheme.getErrorColor(context),
                // Call type icons
                audioCallIcon = typedArray?.getResourceId(
                    R.styleable.CometChatCallLogs_cometchatCallLogsItemAudioCallIcon,
                    R.drawable.cometchat_ic_call_voice
                ) ?: R.drawable.cometchat_ic_call_voice,
                audioCallIconTint = typedArray?.getColor(
                    R.styleable.CometChatCallLogs_cometchatCallLogsItemAudioCallIconTint,
                    CometChatTheme.getIconTintPrimary(context)
                ) ?: CometChatTheme.getIconTintPrimary(context),
                videoCallIcon = typedArray?.getResourceId(
                    R.styleable.CometChatCallLogs_cometchatCallLogsItemVideoCallIcon,
                    R.drawable.cometchat_ic_video_call
                ) ?: R.drawable.cometchat_ic_video_call,
                videoCallIconTint = typedArray?.getColor(
                    R.styleable.CometChatCallLogs_cometchatCallLogsItemVideoCallIconTint,
                    CometChatTheme.getIconTintPrimary(context)
                ) ?: CometChatTheme.getIconTintPrimary(context),
                // Separator
                separatorColor = typedArray?.getColor(
                    R.styleable.CometChatCallLogs_cometchatCallLogsSeparatorColor,
                    CometChatTheme.getStrokeColorLight(context)
                ) ?: CometChatTheme.getStrokeColorLight(context),
                separatorHeight = 1,
                // Component styles
                avatarStyle = avatarStyle,
                dateStyle = dateStyle
            )

            
            return CometChatCallLogsStyle(
                // Container styling
                backgroundColor = typedArray?.getColor(
                    R.styleable.CometChatCallLogs_cometchatCallLogsBackgroundColor,
                    CometChatTheme.getBackgroundColor1(context)
                ) ?: CometChatTheme.getBackgroundColor1(context),
                strokeColor = typedArray?.getColor(
                    R.styleable.CometChatCallLogs_cometchatCallLogsStrokeColor,
                    Color.TRANSPARENT
                ) ?: Color.TRANSPARENT,
                strokeWidth = typedArray?.getDimensionPixelSize(
                    R.styleable.CometChatCallLogs_cometchatCallLogsStrokeWidth, 0
                ) ?: 0,
                cornerRadius = typedArray?.getDimensionPixelSize(
                    R.styleable.CometChatCallLogs_cometchatCallLogsCornerRadius, 0
                ) ?: 0,
                
                // Toolbar styling
                titleTextColor = typedArray?.getColor(
                    R.styleable.CometChatCallLogs_cometchatCallLogsTitleTextColor,
                    CometChatTheme.getTextColorPrimary(context)
                ) ?: CometChatTheme.getTextColorPrimary(context),
                titleTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatCallLogs_cometchatCallLogsTitleTextAppearance,
                    CometChatTheme.getTextAppearanceHeading1Bold(context)
                ) ?: CometChatTheme.getTextAppearanceHeading1Bold(context),
                backIcon = typedArray?.getDrawable(
                    R.styleable.CometChatCallLogs_cometchatCallLogsBackIcon
                ),
                backIconTint = typedArray?.getColor(
                    R.styleable.CometChatCallLogs_cometchatCallLogsBackIconTint,
                    CometChatTheme.getIconTintPrimary(context)
                ) ?: CometChatTheme.getIconTintPrimary(context),
                toolbarSeparatorColor = CometChatTheme.getStrokeColorLight(context),
                toolbarSeparatorHeight = 1,
                showToolbarSeparator = true,
                
                // Empty state styling (inline)
                emptyStateTitleTextColor = typedArray?.getColor(
                    R.styleable.CometChatCallLogs_cometchatCallLogsEmptyStateTitleTextColor,
                    CometChatTheme.getTextColorPrimary(context)
                ) ?: CometChatTheme.getTextColorPrimary(context),
                emptyStateSubtitleTextColor = typedArray?.getColor(
                    R.styleable.CometChatCallLogs_cometchatCallLogsEmptyStateSubtitleTextColor,
                    CometChatTheme.getTextColorSecondary(context)
                ) ?: CometChatTheme.getTextColorSecondary(context),
                emptyStateTitleTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatCallLogs_cometchatCallLogsEmptyStateTitleTextAppearance,
                    CometChatTheme.getTextAppearanceHeading3Bold(context)
                ) ?: CometChatTheme.getTextAppearanceHeading3Bold(context),
                emptyStateSubtitleTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatCallLogs_cometchatCallLogsEmptyStateSubtitleTextAppearance,
                    CometChatTheme.getTextAppearanceBodyRegular(context)
                ) ?: CometChatTheme.getTextAppearanceBodyRegular(context),
                emptyStateIcon = null,
                emptyStateIconTint = 0,
                
                // Error state styling (inline)
                errorStateTitleTextColor = typedArray?.getColor(
                    R.styleable.CometChatCallLogs_cometchatCallLogsErrorTitleTextColor,
                    CometChatTheme.getTextColorPrimary(context)
                ) ?: CometChatTheme.getTextColorPrimary(context),
                errorStateSubtitleTextColor = typedArray?.getColor(
                    R.styleable.CometChatCallLogs_cometchatCallLogsErrorSubtitleTextColor,
                    CometChatTheme.getTextColorSecondary(context)
                ) ?: CometChatTheme.getTextColorSecondary(context),
                errorStateTitleTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatCallLogs_cometchatCallLogsErrorTitleTextAppearance,
                    CometChatTheme.getTextAppearanceHeading3Bold(context)
                ) ?: CometChatTheme.getTextAppearanceHeading3Bold(context),
                errorStateSubtitleTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatCallLogs_cometchatCallLogsErrorSubtitleTextAppearance,
                    CometChatTheme.getTextAppearanceBodyRegular(context)
                ) ?: CometChatTheme.getTextAppearanceBodyRegular(context),
                errorStateIcon = null,
                errorStateIconTint = 0,
                retryButtonBackgroundColor = CometChatTheme.getPrimaryColor(context),
                retryButtonTextColor = CometChatTheme.getColorWhite(context),
                
                // Item styling
                itemStyle = itemStyle
            )
        }
    }
}