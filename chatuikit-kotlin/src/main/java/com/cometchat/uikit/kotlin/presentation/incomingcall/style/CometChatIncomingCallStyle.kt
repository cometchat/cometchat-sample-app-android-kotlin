package com.cometchat.uikit.kotlin.presentation.incomingcall.style

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.DrawableRes
import androidx.annotation.StyleRes
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.presentation.shared.baseelements.avatar.CometChatAvatarStyle
import com.cometchat.uikit.kotlin.theme.CometChatTheme

/**
 * Style configuration for CometChatIncomingCall component.
 * Contains all visual styling properties for the incoming call screen.
 *
 * This style class follows the CometChat Component Architecture Guide
 * and provides full customization of the incoming call's appearance.
 *
 * **Validates: Requirements 8a.1-8a.19**
 *
 * @param backgroundColor Background color for the incoming call container
 * @param cornerRadius Corner radius of the incoming call container
 * @param strokeWidth Width of the container border
 * @param strokeColor Color of the container border
 * @param titleTextColor Color for the caller name text
 * @param titleTextAppearance Text appearance resource for the caller name
 * @param subtitleTextColor Color for the call type subtitle text
 * @param subtitleTextAppearance Text appearance resource for the subtitle
 * @param iconTint Tint color for the call type icon
 * @param voiceCallIcon Drawable resource for voice call icon
 * @param videoCallIcon Drawable resource for video call icon
 * @param acceptButtonBackgroundColor Background color for the accept button
 * @param rejectButtonBackgroundColor Background color for the reject button
 * @param acceptButtonTextColor Text color for the accept button
 * @param rejectButtonTextColor Text color for the reject button
 * @param acceptButtonTextAppearance Text appearance for the accept button
 * @param rejectButtonTextAppearance Text appearance for the reject button
 * @param avatarStyle Style configuration for the caller's avatar
 */
data class CometChatIncomingCallStyle private constructor(
    // Container styling
    @ColorInt val backgroundColor: Int,
    @Dimension val cornerRadius: Float,
    @Dimension val strokeWidth: Int,
    @ColorInt val strokeColor: Int,
    
    // Title styling (caller name)
    @ColorInt val titleTextColor: Int,
    @StyleRes val titleTextAppearance: Int,
    
    // Subtitle styling (call type)
    @ColorInt val subtitleTextColor: Int,
    @StyleRes val subtitleTextAppearance: Int,
    
    // Call type icon styling
    @ColorInt val iconTint: Int,
    @DrawableRes val voiceCallIcon: Int,
    @DrawableRes val videoCallIcon: Int,
    
    // Accept button styling
    @ColorInt val acceptButtonBackgroundColor: Int,
    @ColorInt val acceptButtonTextColor: Int,
    @StyleRes val acceptButtonTextAppearance: Int,
    
    // Reject button styling
    @ColorInt val rejectButtonBackgroundColor: Int,
    @ColorInt val rejectButtonTextColor: Int,
    @StyleRes val rejectButtonTextAppearance: Int,
    
    // Avatar styling
    val avatarStyle: CometChatAvatarStyle?
) {
    /**
     * Builder class for creating CometChatIncomingCallStyle instances.
     * Provides a fluent API for setting style properties.
     *
     * **Validates: Requirements 8a.19** - Builder pattern for construction
     */
    class Builder(private val context: Context) {
        // Container styling
        @ColorInt private var backgroundColor: Int = CometChatTheme.getBackgroundColor3(context)
        @Dimension private var cornerRadius: Float = 0f
        @Dimension private var strokeWidth: Int = 0
        @ColorInt private var strokeColor: Int = Color.TRANSPARENT
        
        // Title styling
        @ColorInt private var titleTextColor: Int = CometChatTheme.getTextColorPrimary(context)
        @StyleRes private var titleTextAppearance: Int = CometChatTheme.getTextAppearanceHeading2Bold(context)
        
        // Subtitle styling
        @ColorInt private var subtitleTextColor: Int = CometChatTheme.getTextColorSecondary(context)
        @StyleRes private var subtitleTextAppearance: Int = CometChatTheme.getTextAppearanceBodyRegular(context)
        
        // Call type icon styling
        @ColorInt private var iconTint: Int = CometChatTheme.getIconTintSecondary(context)
        @DrawableRes private var voiceCallIcon: Int = R.drawable.cometchat_ic_call_voice
        @DrawableRes private var videoCallIcon: Int = R.drawable.cometchat_ic_video_call
        
        // Accept button styling
        @ColorInt private var acceptButtonBackgroundColor: Int = CometChatTheme.getSuccessColor(context)
        @ColorInt private var acceptButtonTextColor: Int = CometChatTheme.getColorWhite(context)
        @StyleRes private var acceptButtonTextAppearance: Int = CometChatTheme.getTextAppearanceButtonMedium(context)
        
        // Reject button styling
        @ColorInt private var rejectButtonBackgroundColor: Int = CometChatTheme.getErrorColor(context)
        @ColorInt private var rejectButtonTextColor: Int = CometChatTheme.getColorWhite(context)
        @StyleRes private var rejectButtonTextAppearance: Int = CometChatTheme.getTextAppearanceButtonMedium(context)
        
        // Avatar styling
        private var avatarStyle: CometChatAvatarStyle? = CometChatAvatarStyle.default(context)

        // Container setters
        /**
         * Sets the background color for the incoming call container.
         * **Validates: Requirements 8a.2**
         */
        fun setBackgroundColor(@ColorInt color: Int) = apply { backgroundColor = color }
        
        /**
         * Sets the corner radius of the incoming call container.
         * **Validates: Requirements 8a.3**
         */
        fun setCornerRadius(@Dimension radius: Float) = apply { cornerRadius = radius }
        
        /**
         * Sets the stroke width of the container border.
         * **Validates: Requirements 8a.4**
         */
        fun setStrokeWidth(@Dimension width: Int) = apply { strokeWidth = width }
        
        /**
         * Sets the stroke color of the container border.
         * **Validates: Requirements 8a.5**
         */
        fun setStrokeColor(@ColorInt color: Int) = apply { strokeColor = color }
        
        // Title setters
        /**
         * Sets the text color for the caller name.
         * **Validates: Requirements 8a.6**
         */
        fun setTitleTextColor(@ColorInt color: Int) = apply { titleTextColor = color }
        
        /**
         * Sets the text appearance for the caller name.
         * **Validates: Requirements 8a.7**
         */
        fun setTitleTextAppearance(@StyleRes appearance: Int) = apply { titleTextAppearance = appearance }
        
        // Subtitle setters
        /**
         * Sets the text color for the call type subtitle.
         * **Validates: Requirements 8a.8**
         */
        fun setSubtitleTextColor(@ColorInt color: Int) = apply { subtitleTextColor = color }
        
        /**
         * Sets the text appearance for the call type subtitle.
         * **Validates: Requirements 8a.9**
         */
        fun setSubtitleTextAppearance(@StyleRes appearance: Int) = apply { subtitleTextAppearance = appearance }
        
        // Icon setters
        /**
         * Sets the tint color for the call type icon.
         * **Validates: Requirements 8a.10**
         */
        fun setIconTint(@ColorInt color: Int) = apply { iconTint = color }
        
        /**
         * Sets the drawable resource for the voice call icon.
         * **Validates: Requirements 8a.11**
         */
        fun setVoiceCallIcon(@DrawableRes icon: Int) = apply { voiceCallIcon = icon }
        
        /**
         * Sets the drawable resource for the video call icon.
         * **Validates: Requirements 8a.12**
         */
        fun setVideoCallIcon(@DrawableRes icon: Int) = apply { videoCallIcon = icon }
        
        // Accept button setters
        /**
         * Sets the background color for the accept button.
         * **Validates: Requirements 8a.13**
         */
        fun setAcceptButtonBackgroundColor(@ColorInt color: Int) = apply { acceptButtonBackgroundColor = color }
        
        /**
         * Sets the text color for the accept button.
         * **Validates: Requirements 8a.15**
         */
        fun setAcceptButtonTextColor(@ColorInt color: Int) = apply { acceptButtonTextColor = color }
        
        /**
         * Sets the text appearance for the accept button.
         * **Validates: Requirements 8a.16**
         */
        fun setAcceptButtonTextAppearance(@StyleRes appearance: Int) = apply { acceptButtonTextAppearance = appearance }
        
        // Reject button setters
        /**
         * Sets the background color for the reject button.
         * **Validates: Requirements 8a.14**
         */
        fun setRejectButtonBackgroundColor(@ColorInt color: Int) = apply { rejectButtonBackgroundColor = color }
        
        /**
         * Sets the text color for the reject button.
         * **Validates: Requirements 8a.16**
         */
        fun setRejectButtonTextColor(@ColorInt color: Int) = apply { rejectButtonTextColor = color }
        
        /**
         * Sets the text appearance for the reject button.
         * **Validates: Requirements 8a.17**
         */
        fun setRejectButtonTextAppearance(@StyleRes appearance: Int) = apply { rejectButtonTextAppearance = appearance }
        
        // Avatar setter
        /**
         * Sets the avatar style configuration.
         * **Validates: Requirements 8a.17**
         */
        fun setAvatarStyle(style: CometChatAvatarStyle?) = apply { avatarStyle = style }

        /**
         * Builds the CometChatIncomingCallStyle instance.
         */
        fun build() = CometChatIncomingCallStyle(
            backgroundColor = backgroundColor,
            cornerRadius = cornerRadius,
            strokeWidth = strokeWidth,
            strokeColor = strokeColor,
            titleTextColor = titleTextColor,
            titleTextAppearance = titleTextAppearance,
            subtitleTextColor = subtitleTextColor,
            subtitleTextAppearance = subtitleTextAppearance,
            iconTint = iconTint,
            voiceCallIcon = voiceCallIcon,
            videoCallIcon = videoCallIcon,
            acceptButtonBackgroundColor = acceptButtonBackgroundColor,
            acceptButtonTextColor = acceptButtonTextColor,
            acceptButtonTextAppearance = acceptButtonTextAppearance,
            rejectButtonBackgroundColor = rejectButtonBackgroundColor,
            rejectButtonTextColor = rejectButtonTextColor,
            rejectButtonTextAppearance = rejectButtonTextAppearance,
            avatarStyle = avatarStyle
        )
    }

    companion object {
        /**
         * Creates a default style configuration sourcing values from CometChatTheme.
         * All colors and typography are derived from the current theme.
         *
         * @param context The Android context for accessing theme attributes
         * @return A fully configured CometChatIncomingCallStyle with theme defaults
         */
        fun default(context: Context): CometChatIncomingCallStyle {
            return Builder(context).build()
        }

        /**
         * Creates a new Builder instance for fluent style construction.
         *
         * @param context The Android context for accessing theme attributes
         * @return A new Builder instance with theme defaults
         */
        fun builder(context: Context) = Builder(context)
        
        /**
         * Creates a style by extracting values from the theme's cometchatIncomingCallStyle.
         *
         * @param context The context to access theme resources
         * @return A CometChatIncomingCallStyle with values from theme or fallback defaults
         */
        fun fromTheme(context: Context): CometChatIncomingCallStyle {
            val themeTypedArray = context.obtainStyledAttributes(intArrayOf(R.attr.cometchatIncomingCallStyle))
            val styleResId = themeTypedArray.getResourceId(0, 0)
            themeTypedArray.recycle()
            return fromStyleResource(context, styleResId)
        }
        
        private fun fromStyleResource(context: Context, styleResId: Int): CometChatIncomingCallStyle {
            if (styleResId == 0) {
                return extractFromTypedArray(context, null)
            }
            val typedArray = context.obtainStyledAttributes(styleResId, R.styleable.CometChatIncomingCall)
            return try {
                extractFromTypedArray(context, typedArray)
            } finally {
                typedArray.recycle()
            }
        }
        
        /**
         * Creates a style from a TypedArray.
         * The TypedArray will be recycled after extraction.
         *
         * @param context The context to access theme resources
         * @param typedArray The TypedArray containing XML attribute values
         * @return A CometChatIncomingCallStyle with values from XML or theme defaults
         */
        fun fromTypedArray(context: Context, typedArray: TypedArray): CometChatIncomingCallStyle {
            return try {
                extractFromTypedArray(context, typedArray)
            } finally {
                typedArray.recycle()
            }
        }
        
        private fun extractFromTypedArray(
            context: Context,
            typedArray: TypedArray?
        ): CometChatIncomingCallStyle {
            // Extract nested avatar style resource ID
            val avatarStyleResId = typedArray?.getResourceId(
                R.styleable.CometChatIncomingCall_cometchatIncomingCallAvatarStyle, 0
            ) ?: 0
            
            // Create avatar style from resource ID or use default
            val avatarStyle = if (avatarStyleResId != 0) {
                val avatarTypedArray = context.theme.obtainStyledAttributes(
                    avatarStyleResId, R.styleable.CometChatAvatar
                )
                CometChatAvatarStyle.fromTypedArray(context, avatarTypedArray)
            } else {
                CometChatAvatarStyle.default(context)
            }
            
            return CometChatIncomingCallStyle(
                // Container styling
                backgroundColor = typedArray?.getColor(
                    R.styleable.CometChatIncomingCall_cometchatIncomingCallBackgroundColor,
                    CometChatTheme.getBackgroundColor3(context)
                ) ?: CometChatTheme.getBackgroundColor3(context),
                cornerRadius = typedArray?.getDimension(
                    R.styleable.CometChatIncomingCall_cometchatIncomingCallCornerRadius,
                    0f
                ) ?: 0f,
                strokeWidth = typedArray?.getDimensionPixelSize(
                    R.styleable.CometChatIncomingCall_cometchatIncomingCallStrokeWidth,
                    0
                ) ?: 0,
                strokeColor = typedArray?.getColor(
                    R.styleable.CometChatIncomingCall_cometchatIncomingCallStrokeColor,
                    Color.TRANSPARENT
                ) ?: Color.TRANSPARENT,
                
                // Title styling
                titleTextColor = typedArray?.getColor(
                    R.styleable.CometChatIncomingCall_cometchatIncomingCallTitleTextColor,
                    CometChatTheme.getTextColorPrimary(context)
                ) ?: CometChatTheme.getTextColorPrimary(context),
                titleTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatIncomingCall_cometchatIncomingCallTitleTextAppearance,
                    CometChatTheme.getTextAppearanceHeading2Bold(context)
                ) ?: CometChatTheme.getTextAppearanceHeading2Bold(context),
                
                // Subtitle styling
                subtitleTextColor = typedArray?.getColor(
                    R.styleable.CometChatIncomingCall_cometchatIncomingCallSubtitleTextColor,
                    CometChatTheme.getTextColorSecondary(context)
                ) ?: CometChatTheme.getTextColorSecondary(context),
                subtitleTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatIncomingCall_cometchatIncomingCallSubtitleTextAppearance,
                    CometChatTheme.getTextAppearanceBodyRegular(context)
                ) ?: CometChatTheme.getTextAppearanceBodyRegular(context),
                
                // Icon styling
                iconTint = typedArray?.getColor(
                    R.styleable.CometChatIncomingCall_cometchatIncomingCallIconTint,
                    CometChatTheme.getIconTintSecondary(context)
                ) ?: CometChatTheme.getIconTintSecondary(context),
                voiceCallIcon = typedArray?.getResourceId(
                    R.styleable.CometChatIncomingCall_cometchatIncomingCallVoiceCallIcon,
                    R.drawable.cometchat_ic_call_voice
                ) ?: R.drawable.cometchat_ic_call_voice,
                videoCallIcon = typedArray?.getResourceId(
                    R.styleable.CometChatIncomingCall_cometchatIncomingCallVideoCallIcon,
                    R.drawable.cometchat_ic_video_call
                ) ?: R.drawable.cometchat_ic_video_call,
                
                // Accept button styling
                acceptButtonBackgroundColor = typedArray?.getColor(
                    R.styleable.CometChatIncomingCall_cometchatIncomingCallAcceptButtonBackgroundColor,
                    CometChatTheme.getSuccessColor(context)
                ) ?: CometChatTheme.getSuccessColor(context),
                acceptButtonTextColor = typedArray?.getColor(
                    R.styleable.CometChatIncomingCall_cometchatIncomingCallAcceptButtonTextColor,
                    CometChatTheme.getColorWhite(context)
                ) ?: CometChatTheme.getColorWhite(context),
                acceptButtonTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatIncomingCall_cometchatIncomingCallAcceptButtonTextAppearance,
                    CometChatTheme.getTextAppearanceButtonMedium(context)
                ) ?: CometChatTheme.getTextAppearanceButtonMedium(context),
                
                // Reject button styling
                rejectButtonBackgroundColor = typedArray?.getColor(
                    R.styleable.CometChatIncomingCall_cometchatIncomingCallRejectButtonBackgroundColor,
                    CometChatTheme.getErrorColor(context)
                ) ?: CometChatTheme.getErrorColor(context),
                rejectButtonTextColor = typedArray?.getColor(
                    R.styleable.CometChatIncomingCall_cometchatIncomingCallRejectButtonTextColor,
                    CometChatTheme.getColorWhite(context)
                ) ?: CometChatTheme.getColorWhite(context),
                rejectButtonTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatIncomingCall_cometchatIncomingCallRejectButtonTextAppearance,
                    CometChatTheme.getTextAppearanceButtonMedium(context)
                ) ?: CometChatTheme.getTextAppearanceButtonMedium(context),
                
                // Avatar styling
                avatarStyle = avatarStyle
            )
        }
    }
}
