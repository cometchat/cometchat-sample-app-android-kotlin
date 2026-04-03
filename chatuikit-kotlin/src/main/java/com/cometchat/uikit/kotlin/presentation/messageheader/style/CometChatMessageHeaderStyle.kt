package com.cometchat.uikit.kotlin.presentation.messageheader.style

import android.content.Context
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.StyleRes
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.presentation.shared.baseelements.avatar.CometChatAvatarStyle
import com.cometchat.uikit.kotlin.presentation.shared.statusindicator.CometChatStatusIndicatorStyle
import com.cometchat.uikit.kotlin.theme.CometChatTheme

/**
 * Style configuration for CometChatMessageHeader component.
 * 
 * This data class mirrors the Compose CometChatMessageHeaderStyle for consistency
 * across both UI implementations. All style properties can be customized either
 * programmatically or via XML attributes.
 * 
 * @param backgroundColor Background color for the header container
 * @param strokeColor Border/stroke color for the header container
 * @param strokeWidth Border/stroke width for the header container
 * @param cornerRadius Corner radius for the header container
 * @param titleTextColor Color for the title text (user/group name)
 * @param titleTextAppearance Text appearance resource for the title
 * @param subtitleTextColor Color for the subtitle text (status/member count)
 * @param subtitleTextAppearance Text appearance resource for the subtitle
 * @param backIcon Drawable for the back navigation button
 * @param backIconTint Tint color for the back icon
 * @param menuIcon Drawable for the overflow menu button
 * @param menuIconTint Tint color for the menu icon
 * @param typingIndicatorTextColor Color for the typing indicator text
 * @param typingIndicatorTextAppearance Text appearance resource for the typing indicator
 * @param avatarStyle Style configuration for the avatar component
 * @param statusIndicatorStyle Style configuration for the status indicator component
 * @param newChatIcon Drawable for the AI new chat button
 * @param newChatIconTint Tint color for the new chat icon
 * @param chatHistoryIcon Drawable for the AI chat history button
 * @param chatHistoryIconTint Tint color for the chat history icon
 * @param videoCallIcon Drawable for the video call button
 * @param videoCallIconTint Tint color for the video call icon
 * @param voiceCallIcon Drawable for the voice call button
 * @param voiceCallIconTint Tint color for the voice call icon
 */
data class CometChatMessageHeaderStyle(
    // Container styling
    @ColorInt val backgroundColor: Int = 0,
    @ColorInt val strokeColor: Int = 0,
    @Dimension val strokeWidth: Int = 0,
    @Dimension val cornerRadius: Int = 0,
    
    // Title styling
    @ColorInt val titleTextColor: Int = 0,
    @StyleRes val titleTextAppearance: Int = 0,
    
    // Subtitle styling
    @ColorInt val subtitleTextColor: Int = 0,
    @StyleRes val subtitleTextAppearance: Int = 0,
    
    // Back icon styling
    val backIcon: Drawable? = null,
    @ColorInt val backIconTint: Int = 0,
    
    // Menu icon styling
    val menuIcon: Drawable? = null,
    @ColorInt val menuIconTint: Int = 0,
    
    // Typing indicator styling
    @ColorInt val typingIndicatorTextColor: Int = 0,
    @StyleRes val typingIndicatorTextAppearance: Int = 0,
    
    // Component styles
    val avatarStyle: CometChatAvatarStyle = CometChatAvatarStyle(),
    val statusIndicatorStyle: CometChatStatusIndicatorStyle = CometChatStatusIndicatorStyle(),
    
    // AI assistant button styling
    val newChatIcon: Drawable? = null,
    @ColorInt val newChatIconTint: Int = 0,
    val chatHistoryIcon: Drawable? = null,
    @ColorInt val chatHistoryIconTint: Int = 0,
    
    // Call button styling
    val videoCallIcon: Drawable? = null,
    @ColorInt val videoCallIconTint: Int = 0,
    val voiceCallIcon: Drawable? = null,
    @ColorInt val voiceCallIconTint: Int = 0
) {
    companion object {
        /**
         * Creates a default style by extracting values from the theme's cometchatMessageHeaderStyle.
         *
         * This method obtains the style resource from the theme attribute and extracts
         * all styling properties including typography (text appearances) from it.
         *
         * @param context The context to access theme resources
         * @return A CometChatMessageHeaderStyle with values from theme or fallback defaults
         */
        fun default(context: Context): CometChatMessageHeaderStyle {
            return extractFromThemeStyle(context)
        }

        /**
         * Extracts the message header style from the theme's cometchatMessageHeaderStyle attribute.
         *
         * @param context The context to access theme resources
         * @return A CometChatMessageHeaderStyle with values from the theme style
         */
        private fun extractFromThemeStyle(context: Context): CometChatMessageHeaderStyle {
            val themeTypedArray = context.obtainStyledAttributes(intArrayOf(R.attr.cometchatMessageHeaderStyle))
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
         * @return A CometChatMessageHeaderStyle with values from the style resource or defaults
         */
        private fun extractFromStyleResource(context: Context, styleResId: Int): CometChatMessageHeaderStyle {
            if (styleResId == 0) {
                return extractFromTypedArray(context, null)
            }
            val typedArray = context.obtainStyledAttributes(styleResId, R.styleable.CometChatMessageHeader)
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
         * All attributes are extracted from the corresponding attr_cometchat_message_header.xml file.
         *
         * @param context The Android context for accessing theme resources
         * @param typedArray The TypedArray containing XML attribute values (will be recycled)
         * @return A CometChatMessageHeaderStyle with values from XML or theme defaults
         */
        fun fromTypedArray(context: Context, typedArray: TypedArray): CometChatMessageHeaderStyle {
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
         * @return A CometChatMessageHeaderStyle with extracted values or theme defaults
         */
        private fun extractFromTypedArray(context: Context, typedArray: TypedArray?): CometChatMessageHeaderStyle {
            // Extract nested style resource IDs
            val avatarStyleResId = typedArray?.getResourceId(
                R.styleable.CometChatMessageHeader_cometchatMessageHeaderAvatarStyle,
                0
            ) ?: 0
            val statusIndicatorStyleResId = typedArray?.getResourceId(
                R.styleable.CometChatMessageHeader_cometchatMessageHeaderStatusIndicatorStyle,
                0
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

            return CometChatMessageHeaderStyle(
                // Container styling
                backgroundColor = typedArray?.getColor(
                    R.styleable.CometChatMessageHeader_cometchatMessageHeaderBackgroundColor,
                    CometChatTheme.getBackgroundColor1(context)
                ) ?: CometChatTheme.getBackgroundColor1(context),
                strokeColor = typedArray?.getColor(
                    R.styleable.CometChatMessageHeader_cometchatMessageHeaderStrokeColor,
                    0
                ) ?: 0,
                strokeWidth = typedArray?.getDimensionPixelSize(
                    R.styleable.CometChatMessageHeader_cometchatMessageHeaderStrokeWidth,
                    0
                ) ?: 0,
                cornerRadius = typedArray?.getDimensionPixelSize(
                    R.styleable.CometChatMessageHeader_cometchatMessageHeaderCornerRadius,
                    0
                ) ?: 0,

                // Title styling
                titleTextColor = typedArray?.getColor(
                    R.styleable.CometChatMessageHeader_cometchatMessageHeaderTitleTextColor,
                    CometChatTheme.getTextColorPrimary(context)
                ) ?: CometChatTheme.getTextColorPrimary(context),
                titleTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatMessageHeader_cometchatMessageHeaderTitleTextAppearance,
                    CometChatTheme.getTextAppearanceHeading4Medium(context)
                ) ?: CometChatTheme.getTextAppearanceHeading4Medium(context),

                // Subtitle styling
                subtitleTextColor = typedArray?.getColor(
                    R.styleable.CometChatMessageHeader_cometchatMessageHeaderSubtitleTextColor,
                    CometChatTheme.getTextColorSecondary(context)
                ) ?: CometChatTheme.getTextColorSecondary(context),
                subtitleTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatMessageHeader_cometchatMessageHeaderSubtitleTextAppearance,
                    CometChatTheme.getTextAppearanceCaption1Regular(context)
                ) ?: CometChatTheme.getTextAppearanceCaption1Regular(context),

                // Back icon styling
                backIcon = typedArray?.getDrawable(
                    R.styleable.CometChatMessageHeader_cometchatMessageHeaderBackIcon
                ),
                backIconTint = typedArray?.getColor(
                    R.styleable.CometChatMessageHeader_cometchatMessageHeaderBackIconTint,
                    CometChatTheme.getIconTintPrimary(context)
                ) ?: CometChatTheme.getIconTintPrimary(context),

                // Menu icon styling
                menuIcon = typedArray?.getDrawable(
                    R.styleable.CometChatMessageHeader_cometchatMessageHeaderMenuIcon
                ),
                menuIconTint = typedArray?.getColor(
                    R.styleable.CometChatMessageHeader_cometchatMessageHeaderMenuIconTint,
                    CometChatTheme.getIconTintPrimary(context)
                ) ?: CometChatTheme.getIconTintPrimary(context),

                // Typing indicator styling
                typingIndicatorTextColor = typedArray?.getColor(
                    R.styleable.CometChatMessageHeader_cometchatMessageHeaderTypingIndicatorTextColor,
                    CometChatTheme.getTextColorHighlight(context)
                ) ?: CometChatTheme.getTextColorHighlight(context),
                typingIndicatorTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatMessageHeader_cometchatMessageHeaderTypingIndicatorTextAppearance,
                    CometChatTheme.getTextAppearanceCaption1Regular(context)
                ) ?: CometChatTheme.getTextAppearanceCaption1Regular(context),

                // Component styles (extracted above)
                avatarStyle = avatarStyle,
                statusIndicatorStyle = statusIndicatorStyle,

                // AI assistant button styling
                newChatIcon = typedArray?.getDrawable(
                    R.styleable.CometChatMessageHeader_cometchatMessageHeaderNewChatIcon
                ),
                newChatIconTint = typedArray?.getColor(
                    R.styleable.CometChatMessageHeader_cometchatMessageHeaderNewChatIconTint,
                    CometChatTheme.getIconTintSecondary(context)
                ) ?: CometChatTheme.getIconTintSecondary(context),
                chatHistoryIcon = typedArray?.getDrawable(
                    R.styleable.CometChatMessageHeader_cometchatMessageHeaderChatHistoryIcon
                ),
                chatHistoryIconTint = typedArray?.getColor(
                    R.styleable.CometChatMessageHeader_cometchatMessageHeaderChatHistoryIconTint,
                    CometChatTheme.getIconTintSecondary(context)
                ) ?: CometChatTheme.getIconTintSecondary(context),

                // Call button styling - use IconTintPrimary to match original Java chatuikit behavior
                videoCallIcon = typedArray?.getDrawable(
                    R.styleable.CometChatMessageHeader_cometchatMessageHeaderVideoCallIcon
                ),
                videoCallIconTint = typedArray?.getColor(
                    R.styleable.CometChatMessageHeader_cometchatMessageHeaderVideoCallIconTint,
                    CometChatTheme.getIconTintPrimary(context)
                ) ?: CometChatTheme.getIconTintPrimary(context),
                voiceCallIcon = typedArray?.getDrawable(
                    R.styleable.CometChatMessageHeader_cometchatMessageHeaderVoiceCallIcon
                ),
                voiceCallIconTint = typedArray?.getColor(
                    R.styleable.CometChatMessageHeader_cometchatMessageHeaderVoiceCallIconTint,
                    CometChatTheme.getIconTintPrimary(context)
                ) ?: CometChatTheme.getIconTintPrimary(context)
            )
        }
    }
}
