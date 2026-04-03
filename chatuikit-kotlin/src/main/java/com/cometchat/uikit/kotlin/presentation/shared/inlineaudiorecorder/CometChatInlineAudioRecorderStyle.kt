package com.cometchat.uikit.kotlin.presentation.shared.inlineaudiorecorder

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.StyleRes
import com.cometchat.uikit.kotlin.theme.CometChatTheme

/**
 * Style configuration for CometChatInlineAudioRecorder.
 * Contains all visual styling properties for the inline audio recorder component.
 *
 * This style class follows the CometChat Compose Component Architecture Guide
 * and provides full customization of the inline audio recorder's appearance.
 *
 * Layout structure (single row):
 * [Delete] [Record/Play] [Waveform] [Duration] [Pause/Mic] [Send]
 *
 * **Validates: Requirements 10.1, 10.2, 10.3, 10.4, 10.5, 10.6, 10.7, 10.8, 10.9, 14.4**
 *
 * @param backgroundColor Background color for the recorder container
 * @param cornerRadius Corner radius of the recorder container
 * @param strokeColor Color of the recorder border/stroke
 * @param strokeWidth Width of the recorder border
 *
 * @param barColor Color for inactive waveform bars (grey)
 * @param recordingBarColor Color for waveform bars during recording (purple)
 * @param playingBarColor Color for active waveform bars during playback (purple)
 * @param barWidth Width of each waveform bar
 * @param barSpacing Spacing between waveform bars
 * @param barMinHeight Minimum height of waveform bars
 * @param barMaxHeight Maximum height of waveform bars
 * @param barCornerRadius Corner radius of waveform bars
 * @param barCount Number of waveform bars to display
 *
 * @param durationTextColor Color for the duration/timer text
 * @param durationTextAppearance Text appearance resource for the duration text
 *
 * @param recordButtonIconColor Icon color for the record button (red)
 * @param recordButtonBackgroundColor Background color for the record button
 * @param recordingIndicatorColor Color for the pulsing red dot indicator during recording
 *
 * @param playButtonIconColor Icon color for the play button
 * @param playButtonBackgroundColor Background color for the play button
 *
 * @param pauseButtonIconColor Icon color for the pause button
 * @param pauseButtonBackgroundColor Background color for the pause button
 *
 * @param deleteButtonIconColor Icon color for the delete button
 * @param deleteButtonBackgroundColor Background color for the delete button
 *
 * @param sendButtonIconColor Icon color for the send button (white)
 * @param sendButtonBackgroundColor Background color for the send button (purple circular)
 *
 * @param micButtonIconColor Icon color for the mic/resume button
 * @param micButtonBackgroundColor Background color for the mic button
 *
 * @param playIcon Drawable for the play icon
 * @param pauseIcon Drawable for the pause icon
 * @param deleteIcon Drawable for the delete icon
 * @param sendIcon Drawable for the send icon
 * @param micIcon Drawable for the mic icon
 */
data class CometChatInlineAudioRecorderStyle(
    // Container styling
    @ColorInt val backgroundColor: Int = 0,
    @Dimension val cornerRadius: Int = 0,
    @ColorInt val strokeColor: Int = 0,
    @Dimension val strokeWidth: Int = 0,
    
    // Waveform styling
    @ColorInt val barColor: Int = 0,
    @ColorInt val recordingBarColor: Int = 0,
    @ColorInt val playingBarColor: Int = 0,
    @Dimension val barWidth: Int = 0,
    @Dimension val barSpacing: Int = 0,
    @Dimension val barMinHeight: Int = 0,
    @Dimension val barMaxHeight: Int = 0,
    @Dimension val barCornerRadius: Int = 0,
    val barCount: Int = 45,
    
    // Duration text styling
    @ColorInt val durationTextColor: Int = 0,
    @StyleRes val durationTextAppearance: Int = 0,
    
    // Record button styling
    @ColorInt val recordButtonIconColor: Int = 0,
    @ColorInt val recordButtonBackgroundColor: Int = 0,
    @ColorInt val recordingIndicatorColor: Int = 0,
    
    // Play button styling
    @ColorInt val playButtonIconColor: Int = 0,
    @ColorInt val playButtonBackgroundColor: Int = 0,
    
    // Pause button styling
    @ColorInt val pauseButtonIconColor: Int = 0,
    @ColorInt val pauseButtonBackgroundColor: Int = 0,
    
    // Delete button styling
    @ColorInt val deleteButtonIconColor: Int = 0,
    @ColorInt val deleteButtonBackgroundColor: Int = 0,
    
    // Send button styling
    @ColorInt val sendButtonIconColor: Int = 0,
    @ColorInt val sendButtonBackgroundColor: Int = 0,
    
    // Mic/Resume button styling
    @ColorInt val micButtonIconColor: Int = 0,
    @ColorInt val micButtonBackgroundColor: Int = 0,
    
    // Icon drawables
    val playIcon: Drawable? = null,
    val pauseIcon: Drawable? = null,
    val deleteIcon: Drawable? = null,
    val sendIcon: Drawable? = null,
    val micIcon: Drawable? = null
) {
    /**
     * Builder class for creating CometChatInlineAudioRecorderStyle instances.
     * Provides a fluent API for setting style properties.
     * Container styling matches MessageComposer compose box for visual consistency.
     */
    class Builder(private val context: Context) {
        // Container styling - matches MessageComposer compose box
        @ColorInt private var backgroundColor: Int = CometChatTheme.getBackgroundColor1(context)
        @Dimension private var cornerRadius: Int = context.resources.getDimensionPixelSize(
            com.cometchat.uikit.kotlin.R.dimen.cometchat_corner_radius_2
        )
        @ColorInt private var strokeColor: Int = CometChatTheme.getBorderColorLight(context)
        @Dimension private var strokeWidth: Int = context.resources.getDimensionPixelSize(
            com.cometchat.uikit.kotlin.R.dimen.cometchat_1dp
        )
        
        // Waveform styling
        @ColorInt private var barColor: Int = CometChatTheme.getNeutralColor300(context)
        @ColorInt private var recordingBarColor: Int = CometChatTheme.getPrimaryColor(context)
        @ColorInt private var playingBarColor: Int = CometChatTheme.getPrimaryColor(context)
        @Dimension private var barWidth: Int = context.resources.getDimensionPixelSize(
            com.cometchat.uikit.kotlin.R.dimen.cometchat_2dp
        )
        @Dimension private var barSpacing: Int = context.resources.getDimensionPixelSize(
            com.cometchat.uikit.kotlin.R.dimen.cometchat_2dp
        )
        @Dimension private var barMinHeight: Int = context.resources.getDimensionPixelSize(
            com.cometchat.uikit.kotlin.R.dimen.cometchat_2dp
        )
        @Dimension private var barMaxHeight: Int = context.resources.getDimensionPixelSize(
            com.cometchat.uikit.kotlin.R.dimen.cometchat_16dp
        )
        @Dimension private var barCornerRadius: Int = context.resources.getDimensionPixelSize(
            com.cometchat.uikit.kotlin.R.dimen.cometchat_1dp
        )
        private var barCount: Int = 45
        
        // Duration text styling
        @ColorInt private var durationTextColor: Int = CometChatTheme.getTextColorSecondary(context)
        @StyleRes private var durationTextAppearance: Int = CometChatTheme.getTextAppearanceBodyRegular(context)
        
        // Record button styling
        @ColorInt private var recordButtonIconColor: Int = CometChatTheme.getErrorColor(context)
        @ColorInt private var recordButtonBackgroundColor: Int = android.graphics.Color.TRANSPARENT
        @ColorInt private var recordingIndicatorColor: Int = CometChatTheme.getErrorColor(context)
        
        // Play button styling
        @ColorInt private var playButtonIconColor: Int = CometChatTheme.getIconTintPrimary(context)
        @ColorInt private var playButtonBackgroundColor: Int = android.graphics.Color.TRANSPARENT
        
        // Pause button styling
        @ColorInt private var pauseButtonIconColor: Int = CometChatTheme.getIconTintPrimary(context)
        @ColorInt private var pauseButtonBackgroundColor: Int = android.graphics.Color.TRANSPARENT
        
        // Delete button styling
        @ColorInt private var deleteButtonIconColor: Int = CometChatTheme.getIconTintSecondary(context)
        @ColorInt private var deleteButtonBackgroundColor: Int = android.graphics.Color.TRANSPARENT
        
        // Send button styling
        @ColorInt private var sendButtonIconColor: Int = CometChatTheme.getColorWhite(context)
        @ColorInt private var sendButtonBackgroundColor: Int = CometChatTheme.getPrimaryColor(context)
        
        // Mic/Resume button styling
        @ColorInt private var micButtonIconColor: Int = CometChatTheme.getIconTintPrimary(context)
        @ColorInt private var micButtonBackgroundColor: Int = android.graphics.Color.TRANSPARENT
        
        // Icon drawables
        private var playIcon: Drawable? = null
        private var pauseIcon: Drawable? = null
        private var deleteIcon: Drawable? = null
        private var sendIcon: Drawable? = null
        private var micIcon: Drawable? = null

        // Container setters
        fun backgroundColor(@ColorInt color: Int) = apply { backgroundColor = color }
        fun cornerRadius(@Dimension radius: Int) = apply { cornerRadius = radius }
        fun strokeColor(@ColorInt color: Int) = apply { strokeColor = color }
        fun strokeWidth(@Dimension width: Int) = apply { strokeWidth = width }
        
        // Waveform setters
        fun barColor(@ColorInt color: Int) = apply { barColor = color }
        fun recordingBarColor(@ColorInt color: Int) = apply { recordingBarColor = color }
        fun playingBarColor(@ColorInt color: Int) = apply { playingBarColor = color }
        fun barWidth(@Dimension width: Int) = apply { barWidth = width }
        fun barSpacing(@Dimension spacing: Int) = apply { barSpacing = spacing }
        fun barMinHeight(@Dimension height: Int) = apply { barMinHeight = height }
        fun barMaxHeight(@Dimension height: Int) = apply { barMaxHeight = height }
        fun barCornerRadius(@Dimension radius: Int) = apply { barCornerRadius = radius }
        fun barCount(count: Int) = apply { barCount = count }
        
        // Duration text setters
        fun durationTextColor(@ColorInt color: Int) = apply { durationTextColor = color }
        fun durationTextAppearance(@StyleRes appearance: Int) = apply { durationTextAppearance = appearance }
        
        // Record button setters
        fun recordButtonIconColor(@ColorInt color: Int) = apply { recordButtonIconColor = color }
        fun recordButtonBackgroundColor(@ColorInt color: Int) = apply { recordButtonBackgroundColor = color }
        fun recordingIndicatorColor(@ColorInt color: Int) = apply { recordingIndicatorColor = color }
        
        // Play button setters
        fun playButtonIconColor(@ColorInt color: Int) = apply { playButtonIconColor = color }
        fun playButtonBackgroundColor(@ColorInt color: Int) = apply { playButtonBackgroundColor = color }
        
        // Pause button setters
        fun pauseButtonIconColor(@ColorInt color: Int) = apply { pauseButtonIconColor = color }
        fun pauseButtonBackgroundColor(@ColorInt color: Int) = apply { pauseButtonBackgroundColor = color }
        
        // Delete button setters
        fun deleteButtonIconColor(@ColorInt color: Int) = apply { deleteButtonIconColor = color }
        fun deleteButtonBackgroundColor(@ColorInt color: Int) = apply { deleteButtonBackgroundColor = color }
        
        // Send button setters
        fun sendButtonIconColor(@ColorInt color: Int) = apply { sendButtonIconColor = color }
        fun sendButtonBackgroundColor(@ColorInt color: Int) = apply { sendButtonBackgroundColor = color }
        
        // Mic button setters
        fun micButtonIconColor(@ColorInt color: Int) = apply { micButtonIconColor = color }
        fun micButtonBackgroundColor(@ColorInt color: Int) = apply { micButtonBackgroundColor = color }
        
        // Icon drawable setters
        fun playIcon(icon: Drawable?) = apply { playIcon = icon }
        fun pauseIcon(icon: Drawable?) = apply { pauseIcon = icon }
        fun deleteIcon(icon: Drawable?) = apply { deleteIcon = icon }
        fun sendIcon(icon: Drawable?) = apply { sendIcon = icon }
        fun micIcon(icon: Drawable?) = apply { micIcon = icon }

        /**
         * Builds the CometChatInlineAudioRecorderStyle instance.
         */
        fun build() = CometChatInlineAudioRecorderStyle(
            backgroundColor = backgroundColor,
            cornerRadius = cornerRadius,
            strokeColor = strokeColor,
            strokeWidth = strokeWidth,
            barColor = barColor,
            recordingBarColor = recordingBarColor,
            playingBarColor = playingBarColor,
            barWidth = barWidth,
            barSpacing = barSpacing,
            barMinHeight = barMinHeight,
            barMaxHeight = barMaxHeight,
            barCornerRadius = barCornerRadius,
            barCount = barCount,
            durationTextColor = durationTextColor,
            durationTextAppearance = durationTextAppearance,
            recordButtonIconColor = recordButtonIconColor,
            recordButtonBackgroundColor = recordButtonBackgroundColor,
            recordingIndicatorColor = recordingIndicatorColor,
            playButtonIconColor = playButtonIconColor,
            playButtonBackgroundColor = playButtonBackgroundColor,
            pauseButtonIconColor = pauseButtonIconColor,
            pauseButtonBackgroundColor = pauseButtonBackgroundColor,
            deleteButtonIconColor = deleteButtonIconColor,
            deleteButtonBackgroundColor = deleteButtonBackgroundColor,
            sendButtonIconColor = sendButtonIconColor,
            sendButtonBackgroundColor = sendButtonBackgroundColor,
            micButtonIconColor = micButtonIconColor,
            micButtonBackgroundColor = micButtonBackgroundColor,
            playIcon = playIcon,
            pauseIcon = pauseIcon,
            deleteIcon = deleteIcon,
            sendIcon = sendIcon,
            micIcon = micIcon
        )
    }

    companion object {
        /**
         * Creates a default style configuration sourcing values from CometChatTheme.
         * All colors and typography are derived from the current theme.
         *
         * **Validates: Requirements 10.9** - Default values from CometChatTheme
         *
         * @param context The Android context for accessing theme attributes
         * @return A fully configured CometChatInlineAudioRecorderStyle with theme defaults
         */
        fun default(context: Context): CometChatInlineAudioRecorderStyle {
            return Builder(context).build()
        }

        /**
         * Creates a new Builder instance for fluent style construction.
         *
         * @param context The Android context for accessing theme attributes
         * @return A new Builder instance with theme defaults
         */
        fun builder(context: Context) = Builder(context)
    }
}
