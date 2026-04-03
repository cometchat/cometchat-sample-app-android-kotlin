package com.cometchat.uikit.kotlin.presentation.outgoingcall.style

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
 * Style configuration for CometChatOutgoingCall component.
 * Contains all visual styling properties for the outgoing call screen.
 *
 * This style class follows the CometChat Component Architecture Guide
 * and provides full customization of the outgoing call's appearance.
 *
 * **Validates: Requirements 12a.1-12a.15**
 *
 * @param backgroundColor Background color for the outgoing call container
 * @param cornerRadius Corner radius of the outgoing call container
 * @param strokeWidth Width of the container border
 * @param strokeColor Color of the container border
 * @param titleTextColor Color for the recipient name text
 * @param titleTextAppearance Text appearance resource for the recipient name
 * @param subtitleTextColor Color for the "Calling..." subtitle text
 * @param subtitleTextAppearance Text appearance resource for the subtitle
 * @param endCallIcon Drawable resource for the end call button icon
 * @param endCallIconTint Tint color for the end call icon
 * @param endCallButtonBackgroundColor Background color for the end call button
 * @param avatarStyle Style configuration for the recipient's avatar
 */
data class CometChatOutgoingCallStyle private constructor(
    // Container styling
    @ColorInt val backgroundColor: Int,
    @Dimension val cornerRadius: Float,
    @Dimension val strokeWidth: Int,
    @ColorInt val strokeColor: Int,
    
    // Title styling (recipient name)
    @ColorInt val titleTextColor: Int,
    @StyleRes val titleTextAppearance: Int,
    
    // Subtitle styling ("Calling...")
    @ColorInt val subtitleTextColor: Int,
    @StyleRes val subtitleTextAppearance: Int,
    
    // End call button styling
    @DrawableRes val endCallIcon: Int,
    @ColorInt val endCallIconTint: Int,
    @ColorInt val endCallButtonBackgroundColor: Int,
    
    // Avatar styling
    val avatarStyle: CometChatAvatarStyle?
) {
    /**
     * Builder class for creating CometChatOutgoingCallStyle instances.
     * Provides a fluent API for setting style properties.
     *
     * **Validates: Requirements 12a.15** - Builder pattern for construction
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
        
        // End call button styling
        @DrawableRes private var endCallIcon: Int = R.drawable.cometchat_ic_end_call
        @ColorInt private var endCallIconTint: Int = CometChatTheme.getColorWhite(context)
        @ColorInt private var endCallButtonBackgroundColor: Int = CometChatTheme.getErrorColor(context)
        
        // Avatar styling
        private var avatarStyle: CometChatAvatarStyle? = CometChatAvatarStyle.default(context)

        // Container setters
        /**
         * Sets the background color for the outgoing call container.
         * **Validates: Requirements 12a.2**
         */
        fun setBackgroundColor(@ColorInt color: Int) = apply { backgroundColor = color }
        
        /**
         * Sets the corner radius of the outgoing call container.
         * **Validates: Requirements 12a.3**
         */
        fun setCornerRadius(@Dimension radius: Float) = apply { cornerRadius = radius }
        
        /**
         * Sets the stroke width of the container border.
         * **Validates: Requirements 12a.4**
         */
        fun setStrokeWidth(@Dimension width: Int) = apply { strokeWidth = width }
        
        /**
         * Sets the stroke color of the container border.
         * **Validates: Requirements 12a.5**
         */
        fun setStrokeColor(@ColorInt color: Int) = apply { strokeColor = color }
        
        // Title setters
        /**
         * Sets the text color for the recipient name.
         * **Validates: Requirements 12a.6**
         */
        fun setTitleTextColor(@ColorInt color: Int) = apply { titleTextColor = color }
        
        /**
         * Sets the text appearance for the recipient name.
         * **Validates: Requirements 12a.7**
         */
        fun setTitleTextAppearance(@StyleRes appearance: Int) = apply { titleTextAppearance = appearance }
        
        // Subtitle setters
        /**
         * Sets the text color for the "Calling..." subtitle.
         * **Validates: Requirements 12a.8**
         */
        fun setSubtitleTextColor(@ColorInt color: Int) = apply { subtitleTextColor = color }
        
        /**
         * Sets the text appearance for the "Calling..." subtitle.
         * **Validates: Requirements 12a.9**
         */
        fun setSubtitleTextAppearance(@StyleRes appearance: Int) = apply { subtitleTextAppearance = appearance }
        
        // End call button setters
        /**
         * Sets the drawable resource for the end call button icon.
         * **Validates: Requirements 12a.10**
         */
        fun setEndCallIcon(@DrawableRes icon: Int) = apply { endCallIcon = icon }
        
        /**
         * Sets the tint color for the end call icon.
         * **Validates: Requirements 12a.11**
         */
        fun setEndCallIconTint(@ColorInt color: Int) = apply { endCallIconTint = color }
        
        /**
         * Sets the background color for the end call button.
         * **Validates: Requirements 12a.12**
         */
        fun setEndCallButtonBackgroundColor(@ColorInt color: Int) = apply { endCallButtonBackgroundColor = color }
        
        // Avatar setter
        /**
         * Sets the avatar style configuration.
         * **Validates: Requirements 12a.13**
         */
        fun setAvatarStyle(style: CometChatAvatarStyle?) = apply { avatarStyle = style }

        /**
         * Builds the CometChatOutgoingCallStyle instance.
         */
        fun build() = CometChatOutgoingCallStyle(
            backgroundColor = backgroundColor,
            cornerRadius = cornerRadius,
            strokeWidth = strokeWidth,
            strokeColor = strokeColor,
            titleTextColor = titleTextColor,
            titleTextAppearance = titleTextAppearance,
            subtitleTextColor = subtitleTextColor,
            subtitleTextAppearance = subtitleTextAppearance,
            endCallIcon = endCallIcon,
            endCallIconTint = endCallIconTint,
            endCallButtonBackgroundColor = endCallButtonBackgroundColor,
            avatarStyle = avatarStyle
        )
    }


    companion object {
        /**
         * Creates a default style configuration sourcing values from CometChatTheme.
         * All colors and typography are derived from the current theme.
         *
         * @param context The Android context for accessing theme attributes
         * @return A fully configured CometChatOutgoingCallStyle with theme defaults
         */
        fun default(context: Context): CometChatOutgoingCallStyle {
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
         * Creates a style by extracting values from the theme's cometchatOutgoingCallStyle.
         *
         * @param context The context to access theme resources
         * @return A CometChatOutgoingCallStyle with values from theme or fallback defaults
         */
        fun fromTheme(context: Context): CometChatOutgoingCallStyle {
            val themeTypedArray = context.obtainStyledAttributes(intArrayOf(R.attr.cometchatOutgoingCallStyle))
            val styleResId = themeTypedArray.getResourceId(0, 0)
            themeTypedArray.recycle()
            return fromStyleResource(context, styleResId)
        }
        
        private fun fromStyleResource(context: Context, styleResId: Int): CometChatOutgoingCallStyle {
            if (styleResId == 0) {
                return extractFromTypedArray(context, null)
            }
            val typedArray = context.obtainStyledAttributes(styleResId, R.styleable.CometChatOutgoingCall)
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
         * @return A CometChatOutgoingCallStyle with values from XML or theme defaults
         */
        fun fromTypedArray(context: Context, typedArray: TypedArray): CometChatOutgoingCallStyle {
            return try {
                extractFromTypedArray(context, typedArray)
            } finally {
                typedArray.recycle()
            }
        }
        
        private fun extractFromTypedArray(
            context: Context,
            typedArray: TypedArray?
        ): CometChatOutgoingCallStyle {
            // Extract nested avatar style resource ID
            val avatarStyleResId = typedArray?.getResourceId(
                R.styleable.CometChatOutgoingCall_cometchatOutgoingCallAvatarStyle, 0
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
            
            return CometChatOutgoingCallStyle(
                // Container styling
                backgroundColor = typedArray?.getColor(
                    R.styleable.CometChatOutgoingCall_cometchatOutgoingCallBackgroundColor,
                    CometChatTheme.getBackgroundColor3(context)
                ) ?: CometChatTheme.getBackgroundColor3(context),
                cornerRadius = typedArray?.getDimension(
                    R.styleable.CometChatOutgoingCall_cometchatOutgoingCallCornerRadius,
                    0f
                ) ?: 0f,
                strokeWidth = typedArray?.getDimensionPixelSize(
                    R.styleable.CometChatOutgoingCall_cometchatOutgoingCallStrokeWidth,
                    0
                ) ?: 0,
                strokeColor = typedArray?.getColor(
                    R.styleable.CometChatOutgoingCall_cometchatOutgoingCallStrokeColor,
                    Color.TRANSPARENT
                ) ?: Color.TRANSPARENT,
                
                // Title styling
                titleTextColor = typedArray?.getColor(
                    R.styleable.CometChatOutgoingCall_cometchatOutgoingCallTitleTextColor,
                    CometChatTheme.getTextColorPrimary(context)
                ) ?: CometChatTheme.getTextColorPrimary(context),
                titleTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatOutgoingCall_cometchatOutgoingCallTitleTextAppearance,
                    CometChatTheme.getTextAppearanceHeading2Bold(context)
                ) ?: CometChatTheme.getTextAppearanceHeading2Bold(context),
                
                // Subtitle styling
                subtitleTextColor = typedArray?.getColor(
                    R.styleable.CometChatOutgoingCall_cometchatOutgoingCallSubtitleTextColor,
                    CometChatTheme.getTextColorSecondary(context)
                ) ?: CometChatTheme.getTextColorSecondary(context),
                subtitleTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatOutgoingCall_cometchatOutgoingCallSubtitleTextAppearance,
                    CometChatTheme.getTextAppearanceBodyRegular(context)
                ) ?: CometChatTheme.getTextAppearanceBodyRegular(context),
                
                // End call button styling
                endCallIcon = typedArray?.getResourceId(
                    R.styleable.CometChatOutgoingCall_cometchatOutgoingCallEndCallIcon,
                    R.drawable.cometchat_ic_end_call
                ) ?: R.drawable.cometchat_ic_end_call,
                endCallIconTint = typedArray?.getColor(
                    R.styleable.CometChatOutgoingCall_cometchatOutgoingCallEndCallIconTint,
                    CometChatTheme.getColorWhite(context)
                ) ?: CometChatTheme.getColorWhite(context),
                endCallButtonBackgroundColor = typedArray?.getColor(
                    R.styleable.CometChatOutgoingCall_cometchatOutgoingCallEndCallButtonBackgroundColor,
                    CometChatTheme.getErrorColor(context)
                ) ?: CometChatTheme.getErrorColor(context),
                
                // Avatar styling
                avatarStyle = avatarStyle
            )
        }
    }
}
