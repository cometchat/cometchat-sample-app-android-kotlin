package com.cometchat.uikit.compose.presentation.shared.mediarecorder.style

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cometchat.uikit.compose.theme.CometChatTheme

/**
 * Immutable style configuration for CometChatMediaRecorder.
 * Contains all visual styling properties for the media recorder component.
 *
 * This style class follows the CometChat Compose Component Architecture Guide
 * and provides full customization of the media recorder's appearance.
 *
 * **Validates: Requirements 6.1, 6.2, 6.3, 6.4, 6.5, 6.6, 6.7**
 *
 * @param backgroundColor Background color for the media recorder container
 * @param cornerRadius Corner radius of the recorder container
 * @param strokeColor Color of the recorder border/stroke
 * @param strokeWidth Width of the recorder border
 *
 * @param recordingChunkColor Color for Audio_Visualizer bars during recording
 *
 * @param seekBarColor Color for the playback SeekBar progress
 * @param seekBarTrackColor Color for the SeekBar track (background)
 * @param recordedContainerColor Background color for the Recorded_Container
 *
 * @param playIconTint Tint color for the play icon
 * @param pauseIconTint Tint color for the pause icon
 * @param stopIconTint Tint color for the stop icon
 * @param startIconTint Tint color for the start/record icon
 * @param submitIconTint Tint color for the submit/send icon
 * @param deleteIconColor Tint color for the delete icon
 *
 * @param timerTextColor Color for the Chronometer/timer text
 * @param timerTextStyle Typography style for the timer text
 *
 * @param recordButtonBackgroundColor Background color for the record button
 * @param stopButtonBackgroundColor Background color for the stop button
 * @param deleteButtonBackgroundColor Background color for the delete button
 * @param submitButtonBackgroundColor Background color for the submit button
 * @param playButtonBackgroundColor Background color for the play/pause button
 *
 * @param visualizerStyle Style configuration for the audio visualizer component
 */
@Immutable
data class CometChatMediaRecorderStyle(
    // Container styling
    val backgroundColor: Color,
    val cornerRadius: Dp,
    val strokeColor: Color,
    val strokeWidth: Dp,
    
    // Visualizer styling
    val recordingChunkColor: Color,
    
    // Playback styling
    val seekBarColor: Color,
    val seekBarTrackColor: Color,
    val recordedContainerColor: Color,
    
    // Icon tints
    val playIconTint: Color,
    val pauseIconTint: Color,
    val stopIconTint: Color,
    val startIconTint: Color,
    val submitIconTint: Color,
    val deleteIconColor: Color,
    
    // Text styling
    val timerTextColor: Color,
    val timerTextStyle: TextStyle,
    
    // Button backgrounds
    val recordButtonBackgroundColor: Color,
    val stopButtonBackgroundColor: Color,
    val deleteButtonBackgroundColor: Color,
    val submitButtonBackgroundColor: Color,
    val playButtonBackgroundColor: Color,
    
    // Nested style for audio visualizer
    val visualizerStyle: CometChatAudioVisualizerStyle
) {
    companion object {
        /**
         * Creates a default style configuration sourcing values from CometChatTheme.
         * All colors and typography are derived from the current theme.
         *
         * **Validates: Requirements 6.7** - Default values from CometChatTheme
         */
        @Composable
        fun default(
            // Container
            backgroundColor: Color = CometChatTheme.colorScheme.backgroundColor1,
            cornerRadius: Dp = 12.dp,
            strokeColor: Color = CometChatTheme.colorScheme.strokeColorLight,
            strokeWidth: Dp = 1.dp,
            
            // Visualizer
            recordingChunkColor: Color = CometChatTheme.colorScheme.primary,
            
            // Playback
            seekBarColor: Color = CometChatTheme.colorScheme.primary,
            seekBarTrackColor: Color = CometChatTheme.colorScheme.backgroundColor3,
            recordedContainerColor: Color = CometChatTheme.colorScheme.backgroundColor2,
            
            // Icon tints
            playIconTint: Color = CometChatTheme.colorScheme.iconTintHighlight,
            pauseIconTint: Color = CometChatTheme.colorScheme.iconTintHighlight,
            stopIconTint: Color = CometChatTheme.colorScheme.iconTintSecondary,
            startIconTint: Color = CometChatTheme.colorScheme.errorColor,
            submitIconTint: Color = CometChatTheme.colorScheme.iconTintHighlight,
            deleteIconColor: Color = CometChatTheme.colorScheme.iconTintSecondary,
            
            // Text
            timerTextColor: Color = CometChatTheme.colorScheme.textColorSecondary,
            timerTextStyle: TextStyle = CometChatTheme.typography.bodyRegular,
            
            // Button backgrounds
            recordButtonBackgroundColor: Color = CometChatTheme.colorScheme.backgroundColor1,
            stopButtonBackgroundColor: Color = CometChatTheme.colorScheme.backgroundColor1,
            deleteButtonBackgroundColor: Color = CometChatTheme.colorScheme.backgroundColor1,
            submitButtonBackgroundColor: Color = CometChatTheme.colorScheme.backgroundColor1,
            playButtonBackgroundColor: Color = CometChatTheme.colorScheme.backgroundColor2,
            
            // Visualizer style
            visualizerStyle: CometChatAudioVisualizerStyle = CometChatAudioVisualizerStyle.default()
        ): CometChatMediaRecorderStyle = CometChatMediaRecorderStyle(
            backgroundColor = backgroundColor,
            cornerRadius = cornerRadius,
            strokeColor = strokeColor,
            strokeWidth = strokeWidth,
            recordingChunkColor = recordingChunkColor,
            seekBarColor = seekBarColor,
            seekBarTrackColor = seekBarTrackColor,
            recordedContainerColor = recordedContainerColor,
            playIconTint = playIconTint,
            pauseIconTint = pauseIconTint,
            stopIconTint = stopIconTint,
            startIconTint = startIconTint,
            submitIconTint = submitIconTint,
            deleteIconColor = deleteIconColor,
            timerTextColor = timerTextColor,
            timerTextStyle = timerTextStyle,
            recordButtonBackgroundColor = recordButtonBackgroundColor,
            stopButtonBackgroundColor = stopButtonBackgroundColor,
            deleteButtonBackgroundColor = deleteButtonBackgroundColor,
            submitButtonBackgroundColor = submitButtonBackgroundColor,
            playButtonBackgroundColor = playButtonBackgroundColor,
            visualizerStyle = visualizerStyle
        )
    }
}
