package com.cometchat.uikit.kotlin.presentation.calllogs.style

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.DrawableRes
import androidx.annotation.StyleRes
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.theme.CometChatTheme
import com.cometchat.uikit.kotlin.presentation.shared.baseelements.avatar.CometChatAvatarStyle
import com.cometchat.uikit.kotlin.presentation.shared.baseelements.date.CometChatDateStyle

/**
 * Style configuration for CometChatCallLogsListItem component.
 * Contains all visual styling properties for a single call log item.
 */
data class CometChatCallLogsListItemStyle(
    // Background
    @ColorInt val backgroundColor: Int = 0,
    
    // Title styling
    @ColorInt val titleTextColor: Int = 0,
    @StyleRes val titleTextAppearance: Int = 0,
    @ColorInt val missedCallTitleColor: Int = 0,
    
    // Subtitle styling
    @ColorInt val subtitleTextColor: Int = 0,
    @StyleRes val subtitleTextAppearance: Int = 0,
    
    // Direction icons (subtitle)
    @DrawableRes val incomingCallIcon: Int = 0,
    @ColorInt val incomingCallIconTint: Int = 0,
    @DrawableRes val outgoingCallIcon: Int = 0,
    @ColorInt val outgoingCallIconTint: Int = 0,
    @DrawableRes val missedCallIcon: Int = 0,
    @ColorInt val missedCallIconTint: Int = 0,
    
    // Call type icons (trailing)
    @DrawableRes val audioCallIcon: Int = 0,
    @ColorInt val audioCallIconTint: Int = 0,
    @DrawableRes val videoCallIcon: Int = 0,
    @ColorInt val videoCallIconTint: Int = 0,
    
    // Separator
    @ColorInt val separatorColor: Int = 0,
    @Dimension val separatorHeight: Int = 1,
    
    // Component styles
    val avatarStyle: CometChatAvatarStyle = CometChatAvatarStyle(),
    val dateStyle: CometChatDateStyle = CometChatDateStyle()
) {
    companion object {
        /**
         * Creates a default style using CometChatTheme values.
         */
        fun default(context: Context): CometChatCallLogsListItemStyle {
            return CometChatCallLogsListItemStyle(
                // Background
                backgroundColor = Color.TRANSPARENT,
                
                // Title styling
                titleTextColor = CometChatTheme.getTextColorPrimary(context),
                titleTextAppearance = CometChatTheme.getTextAppearanceHeading4Medium(context),
                missedCallTitleColor = CometChatTheme.getErrorColor(context),
                
                // Subtitle styling
                subtitleTextColor = CometChatTheme.getTextColorSecondary(context),
                subtitleTextAppearance = CometChatTheme.getTextAppearanceBodyRegular(context),
                
                // Direction icons
                incomingCallIcon = R.drawable.cometchat_ic_incoming_call,
                incomingCallIconTint = CometChatTheme.getSuccessColor(context),
                outgoingCallIcon = R.drawable.cometchat_ic_outgoing_call,
                outgoingCallIconTint = CometChatTheme.getSuccessColor(context),
                missedCallIcon = R.drawable.cometchat_ic_missed_call,
                missedCallIconTint = CometChatTheme.getErrorColor(context),
                
                // Call type icons
                audioCallIcon = R.drawable.cometchat_ic_call_voice,
                audioCallIconTint = CometChatTheme.getIconTintPrimary(context),
                videoCallIcon = R.drawable.cometchat_ic_video_call,
                videoCallIconTint = CometChatTheme.getIconTintPrimary(context),
                
                // Separator
                separatorColor = CometChatTheme.getStrokeColorLight(context),
                separatorHeight = 1,
                
                // Component styles
                avatarStyle = CometChatAvatarStyle.default(context),
                dateStyle = CometChatDateStyle.default(context)
            )
        }
        
        /**
         * Creates a style from a TypedArray.
         * The TypedArray will be recycled after extraction.
         */
        fun fromTypedArray(context: Context, typedArray: TypedArray): CometChatCallLogsListItemStyle {
            return try {
                extractFromTypedArray(context, typedArray)
            } finally {
                typedArray.recycle()
            }
        }
        
        private fun extractFromTypedArray(
            context: Context,
            typedArray: TypedArray?
        ): CometChatCallLogsListItemStyle {
            val defaultStyle = default(context)
            
            // Extract nested style resource IDs
            val avatarStyleResId = typedArray?.getResourceId(
                R.styleable.CometChatCallLogs_cometchatCallLogsAvatarStyle, 0
            ) ?: 0
            val dateStyleResId = typedArray?.getResourceId(
                R.styleable.CometChatCallLogs_cometchatCallLogsDateStyle, 0
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
            
            val dateStyle = if (dateStyleResId != 0) {
                val dateTypedArray = context.theme.obtainStyledAttributes(
                    dateStyleResId, R.styleable.CometChatDate
                )
                CometChatDateStyle.fromTypedArray(context, dateTypedArray)
            } else {
                CometChatDateStyle.default(context)
            }
            
            return CometChatCallLogsListItemStyle(
                // Background
                backgroundColor = defaultStyle.backgroundColor,
                
                // Title styling
                titleTextColor = typedArray?.getColor(
                    R.styleable.CometChatCallLogs_cometchatCallLogsItemTitleTextColor,
                    defaultStyle.titleTextColor
                ) ?: defaultStyle.titleTextColor,
                titleTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatCallLogs_cometchatCallLogsItemTitleTextAppearance,
                    defaultStyle.titleTextAppearance
                ) ?: defaultStyle.titleTextAppearance,
                missedCallTitleColor = typedArray?.getColor(
                    R.styleable.CometChatCallLogs_cometchatCallLogsItemMissedCallTitleColor,
                    defaultStyle.missedCallTitleColor
                ) ?: defaultStyle.missedCallTitleColor,
                
                // Subtitle styling
                subtitleTextColor = typedArray?.getColor(
                    R.styleable.CometChatCallLogs_cometchatCallLogsItemSubtitleTextColor,
                    defaultStyle.subtitleTextColor
                ) ?: defaultStyle.subtitleTextColor,
                subtitleTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatCallLogs_cometchatCallLogsItemSubtitleTextAppearance,
                    defaultStyle.subtitleTextAppearance
                ) ?: defaultStyle.subtitleTextAppearance,
                
                // Direction icons
                incomingCallIcon = typedArray?.getResourceId(
                    R.styleable.CometChatCallLogs_cometchatCallLogsItemIncomingCallIcon,
                    defaultStyle.incomingCallIcon
                ) ?: defaultStyle.incomingCallIcon,
                incomingCallIconTint = typedArray?.getColor(
                    R.styleable.CometChatCallLogs_cometchatCallLogsItemIncomingCallIconTint,
                    defaultStyle.incomingCallIconTint
                ) ?: defaultStyle.incomingCallIconTint,
                outgoingCallIcon = typedArray?.getResourceId(
                    R.styleable.CometChatCallLogs_cometchatCallLogsItemOutgoingCallIcon,
                    defaultStyle.outgoingCallIcon
                ) ?: defaultStyle.outgoingCallIcon,
                outgoingCallIconTint = typedArray?.getColor(
                    R.styleable.CometChatCallLogs_cometchatCallLogsItemOutgoingCallIconTint,
                    defaultStyle.outgoingCallIconTint
                ) ?: defaultStyle.outgoingCallIconTint,
                missedCallIcon = typedArray?.getResourceId(
                    R.styleable.CometChatCallLogs_cometchatCallLogsItemMissedCallIcon,
                    defaultStyle.missedCallIcon
                ) ?: defaultStyle.missedCallIcon,
                missedCallIconTint = typedArray?.getColor(
                    R.styleable.CometChatCallLogs_cometchatCallLogsItemMissedCallIconTint,
                    defaultStyle.missedCallIconTint
                ) ?: defaultStyle.missedCallIconTint,
                
                // Call type icons
                audioCallIcon = typedArray?.getResourceId(
                    R.styleable.CometChatCallLogs_cometchatCallLogsItemAudioCallIcon,
                    defaultStyle.audioCallIcon
                ) ?: defaultStyle.audioCallIcon,
                audioCallIconTint = typedArray?.getColor(
                    R.styleable.CometChatCallLogs_cometchatCallLogsItemAudioCallIconTint,
                    defaultStyle.audioCallIconTint
                ) ?: defaultStyle.audioCallIconTint,
                videoCallIcon = typedArray?.getResourceId(
                    R.styleable.CometChatCallLogs_cometchatCallLogsItemVideoCallIcon,
                    defaultStyle.videoCallIcon
                ) ?: defaultStyle.videoCallIcon,
                videoCallIconTint = typedArray?.getColor(
                    R.styleable.CometChatCallLogs_cometchatCallLogsItemVideoCallIconTint,
                    defaultStyle.videoCallIconTint
                ) ?: defaultStyle.videoCallIconTint,
                
                // Separator
                separatorColor = typedArray?.getColor(
                    R.styleable.CometChatCallLogs_cometchatCallLogsSeparatorColor,
                    defaultStyle.separatorColor
                ) ?: defaultStyle.separatorColor,
                separatorHeight = 1,
                
                // Component styles
                avatarStyle = avatarStyle,
                dateStyle = dateStyle
            )
        }
    }
}
