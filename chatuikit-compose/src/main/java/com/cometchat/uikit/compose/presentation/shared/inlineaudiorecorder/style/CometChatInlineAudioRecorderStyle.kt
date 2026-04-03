package com.cometchat.uikit.compose.presentation.shared.inlineaudiorecorder.style

import androidx.compose.foundation.BorderStroke
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cometchat.uikit.compose.theme.CometChatTheme

/**
 * Immutable style configuration for CometChatInlineAudioRecorder.
 * Contains all visual styling properties for the inline audio recorder component.
 *
 * This style class follows the CometChat Compose Component Architecture Guide
 * and provides full customization of the inline audio recorder's appearance.
 *
 * Layout structure (single row):
 * [Delete] [Record/Play] [Waveform] [Duration] [Pause/Mic] [Send]
 *
 * **Validates: Requirements 10.1, 10.2, 10.3, 10.4, 10.5, 10.6, 10.7, 10.8, 10.9**
 *
 * @param backgroundColor Background color for the recorder container
 * @param border Border stroke for the recorder container (null for no border)
 * @param borderRadius Corner radius of the recorder container
 *
 * @param waveformStyle Nested style for the waveform visualization component
 *
 * @param durationTextColor Color for the duration/timer text
 * @param durationTextStyle Typography style for the duration text
 *
 * @param recordButtonIconColor Icon color for the record button (red pulsing dot)
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
 */
@Immutable
data class CometChatInlineAudioRecorderStyle(
    // Container styling
    val backgroundColor: Color,
    val border: BorderStroke?,
    val borderRadius: Dp,
    
    // Waveform styling (nested)
    val waveformStyle: CometChatInlineAudioWaveformStyle,
    
    // Duration text styling
    val durationTextColor: Color,
    val durationTextStyle: TextStyle,
    
    // Record button styling (center-left position)
    val recordButtonIconColor: Color,
    val recordButtonBackgroundColor: Color,
    val recordingIndicatorColor: Color,
    
    // Play button styling
    val playButtonIconColor: Color,
    val playButtonBackgroundColor: Color,
    
    // Pause button styling
    val pauseButtonIconColor: Color,
    val pauseButtonBackgroundColor: Color,
    
    // Delete button styling (left side)
    val deleteButtonIconColor: Color,
    val deleteButtonBackgroundColor: Color,
    
    // Send button styling (right side, purple circular)
    val sendButtonIconColor: Color,
    val sendButtonBackgroundColor: Color,
    
    // Mic/Resume button styling (center-right position)
    val micButtonIconColor: Color,
    val micButtonBackgroundColor: Color
) {
    companion object {
        /**
         * Creates a default style configuration sourcing values from CometChatTheme.
         * All colors and typography are derived from the current theme.
         * Container styling matches MessageComposer compose box for visual consistency.
         *
         * Default values:
         * - backgroundColor: backgroundColor1 (matches compose box)
         * - border: 1dp strokeColorLight (matches compose box)
         * - borderRadius: 8.dp (matches compose box)
         * - recordingIndicatorColor: errorColor (red)
         * - sendButtonBackgroundColor: primary (purple)
         * - sendButtonIconColor: colorWhite
         *
         * **Validates: Requirements 10.9** - Default values from CometChatTheme
         */
        @Composable
        fun default(
            // Container - matches MessageComposer compose box styling
            backgroundColor: Color = CometChatTheme.colorScheme.backgroundColor1,
            border: BorderStroke? = BorderStroke(1.dp, CometChatTheme.colorScheme.strokeColorLight),
            borderRadius: Dp = 8.dp,
            
            // Waveform
            waveformStyle: CometChatInlineAudioWaveformStyle = CometChatInlineAudioWaveformStyle.default(),
            
            // Duration text
            durationTextColor: Color = CometChatTheme.colorScheme.textColorSecondary,
            durationTextStyle: TextStyle = CometChatTheme.typography.bodyRegular,
            
            // Record button
            recordButtonIconColor: Color = CometChatTheme.colorScheme.errorColor,
            recordButtonBackgroundColor: Color = Color.Transparent,
            recordingIndicatorColor: Color = CometChatTheme.colorScheme.errorColor,
            
            // Play button
            playButtonIconColor: Color = CometChatTheme.colorScheme.iconTintPrimary,
            playButtonBackgroundColor: Color = Color.Transparent,
            
            // Pause button
            pauseButtonIconColor: Color = CometChatTheme.colorScheme.iconTintPrimary,
            pauseButtonBackgroundColor: Color = Color.Transparent,
            
            // Delete button
            deleteButtonIconColor: Color = CometChatTheme.colorScheme.iconTintSecondary,
            deleteButtonBackgroundColor: Color = Color.Transparent,
            
            // Send button (purple circular)
            sendButtonIconColor: Color = CometChatTheme.colorScheme.colorWhite,
            sendButtonBackgroundColor: Color = CometChatTheme.colorScheme.primary,
            
            // Mic/Resume button
            micButtonIconColor: Color = CometChatTheme.colorScheme.iconTintPrimary,
            micButtonBackgroundColor: Color = Color.Transparent
        ): CometChatInlineAudioRecorderStyle = CometChatInlineAudioRecorderStyle(
            backgroundColor = backgroundColor,
            border = border,
            borderRadius = borderRadius,
            waveformStyle = waveformStyle,
            durationTextColor = durationTextColor,
            durationTextStyle = durationTextStyle,
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
            micButtonBackgroundColor = micButtonBackgroundColor
        )
    }
}
