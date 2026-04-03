package com.cometchat.uikit.compose.presentation.messagecomposer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cometchat.uikit.compose.R
import com.cometchat.uikit.compose.presentation.messagecomposer.style.CometChatMessageComposerStyle
import com.cometchat.uikit.compose.presentation.shared.mediarecorder.AudioWaveformView
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.uikit.core.viewmodel.RecordingState
import java.io.File

/**
 * @deprecated This component is deprecated and will be removed in a future release.
 * Use [com.cometchat.uikit.compose.presentation.shared.mediarecorder.ui.CometChatMediaRecorder] instead,
 * which provides a redesigned media recorder with proper state management, real-time audio visualization,
 * and playback controls.
 *
 * CometChatInlineMediaRecorder replaces the composer content during recording.
 * Displays recording controls, timer, and audio waveform visualization in a horizontal layout.
 *
 * Layout structure:
 * - START state: [Start button centered]
 * - RECORDING state: [Delete] [Audio Waveform] [Timer] [Pause] [Stop]
 * - PAUSED state: [Delete] [Static Waveform] [Timer] [Resume] [Stop]
 * - STOPPED state: [Delete] [Audio Preview] [Restart] [Send]
 *
 * Features:
 * - Inline horizontal layout within the composer area
 * - Audio waveform visualization with bars centered vertically
 * - Recording timer display in MM:SS format
 * - State-based control visibility
 * - Permission handling for microphone access
 *
 * @param modifier Modifier for the recorder
 * @param style Style configuration from the message composer
 * @param recordingState Current recording state
 * @param recordingTime Formatted recording time (MM:SS)
 * @param audioAmplitude Current audio amplitude for waveform (0.0 to 1.0)
 * @param onStartClick Callback when start button is clicked
 * @param onPauseClick Callback when pause button is clicked
 * @param onResumeClick Callback when resume button is clicked
 * @param onStopClick Callback when stop button is clicked
 * @param onSendClick Callback when send button is clicked with the recorded file
 * @param onDeleteClick Callback when delete button is clicked
 * @param onRestartClick Callback when restart button is clicked
 */
@Deprecated(
    message = "Use CometChatMediaRecorder from com.cometchat.uikit.compose.presentation.shared.mediarecorder.ui instead",
    replaceWith = ReplaceWith(
        "CometChatMediaRecorder(modifier, viewModel, style, onSubmit, onClose, onError)",
        "com.cometchat.uikit.compose.presentation.shared.mediarecorder.ui.CometChatMediaRecorder"
    )
)
@Composable
fun CometChatInlineMediaRecorder(
    modifier: Modifier = Modifier,
    style: CometChatMessageComposerStyle = CometChatMessageComposerStyle.default(),
    recordingState: RecordingState = RecordingState.START,
    recordingTime: String = "00:00",
    audioAmplitude: Float = 0f,
    onStartClick: () -> Unit = {},
    onPauseClick: () -> Unit = {},
    onResumeClick: () -> Unit = {},
    onStopClick: () -> Unit = {},
    onSendClick: (File) -> Unit = {},
    onDeleteClick: () -> Unit = {},
    onRestartClick: () -> Unit = {},
    recordedFile: File? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        when (recordingState) {
            RecordingState.START -> {
                // Centered start button
                Spacer(modifier = Modifier.weight(1f))
                RecorderButton(
                    icon = R.drawable.cometchat_ic_media_recorder_start,
                    contentDescription = "Start recording",
                    tint = CometChatTheme.colorScheme.errorColor,
                    backgroundColor = CometChatTheme.colorScheme.backgroundColor1,
                    onClick = onStartClick
                )
                Spacer(modifier = Modifier.weight(1f))
            }
            
            RecordingState.RECORDING -> {
                // [Delete] [Waveform] [Timer] [Pause] [Stop]
                RecorderButton(
                    icon = R.drawable.cometchat_ic_media_recorder_delete,
                    contentDescription = "Delete recording",
                    tint = CometChatTheme.colorScheme.iconTintSecondary,
                    backgroundColor = CometChatTheme.colorScheme.backgroundColor1,
                    onClick = onDeleteClick
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Audio waveform
                AudioWaveformView(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp),
                    amplitude = audioAmplitude,
                    barCount = 15,
                    barColor = CometChatTheme.colorScheme.primary,
                    isAnimating = true
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Timer
                Text(
                    text = recordingTime,
                    color = CometChatTheme.colorScheme.textColorPrimary,
                    style = CometChatTheme.typography.bodyRegular,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .width(50.dp)
                        .semantics { contentDescription = "Recording time: $recordingTime" }
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Pause button
                RecorderButton(
                    icon = R.drawable.cometchat_ic_media_recorder_pause,
                    contentDescription = "Pause recording",
                    tint = CometChatTheme.colorScheme.errorColor,
                    backgroundColor = CometChatTheme.colorScheme.backgroundColor1,
                    onClick = onPauseClick
                )
                
                Spacer(modifier = Modifier.width(4.dp))
                
                // Stop button
                RecorderButton(
                    icon = R.drawable.cometchat_ic_media_recorder_stop,
                    contentDescription = "Stop recording",
                    tint = CometChatTheme.colorScheme.iconTintSecondary,
                    backgroundColor = CometChatTheme.colorScheme.backgroundColor1,
                    onClick = onStopClick
                )
            }
            
            RecordingState.PAUSED -> {
                // [Delete] [Static Waveform] [Timer] [Resume] [Stop]
                RecorderButton(
                    icon = R.drawable.cometchat_ic_media_recorder_delete,
                    contentDescription = "Delete recording",
                    tint = CometChatTheme.colorScheme.iconTintSecondary,
                    backgroundColor = CometChatTheme.colorScheme.backgroundColor1,
                    onClick = onDeleteClick
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Static waveform (paused)
                AudioWaveformView(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp),
                    amplitude = audioAmplitude,
                    barCount = 15,
                    barColor = CometChatTheme.colorScheme.primary,
                    isAnimating = false // Static bars when paused
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Timer
                Text(
                    text = recordingTime,
                    color = CometChatTheme.colorScheme.textColorPrimary,
                    style = CometChatTheme.typography.bodyRegular,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .width(50.dp)
                        .semantics { contentDescription = "Recording time: $recordingTime" }
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Resume button (same as start icon)
                RecorderButton(
                    icon = R.drawable.cometchat_ic_media_recorder_start,
                    contentDescription = "Resume recording",
                    tint = CometChatTheme.colorScheme.errorColor,
                    backgroundColor = CometChatTheme.colorScheme.backgroundColor1,
                    onClick = onResumeClick
                )
                
                Spacer(modifier = Modifier.width(4.dp))
                
                // Stop button
                RecorderButton(
                    icon = R.drawable.cometchat_ic_media_recorder_stop,
                    contentDescription = "Stop recording",
                    tint = CometChatTheme.colorScheme.iconTintSecondary,
                    backgroundColor = CometChatTheme.colorScheme.backgroundColor1,
                    onClick = onStopClick
                )
            }
            
            RecordingState.STOPPED -> {
                // [Delete] [Audio Preview] [Restart] [Send]
                RecorderButton(
                    icon = R.drawable.cometchat_ic_media_recorder_delete,
                    contentDescription = "Delete recording",
                    tint = CometChatTheme.colorScheme.iconTintSecondary,
                    backgroundColor = CometChatTheme.colorScheme.backgroundColor1,
                    onClick = onDeleteClick
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Audio preview bubble
                AudioPreviewBubble(
                    modifier = Modifier.weight(1f),
                    duration = recordingTime
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Restart button
                RecorderButton(
                    icon = R.drawable.cometchat_ic_media_recorder_restart,
                    contentDescription = "Restart recording",
                    tint = CometChatTheme.colorScheme.iconTintSecondary,
                    backgroundColor = CometChatTheme.colorScheme.backgroundColor1,
                    onClick = onRestartClick
                )
                
                Spacer(modifier = Modifier.width(4.dp))
                
                // Send button
                RecorderButton(
                    icon = R.drawable.cometchat_ic_media_recorder_send,
                    contentDescription = "Send recording",
                    tint = CometChatTheme.colorScheme.iconTintHighlight,
                    backgroundColor = CometChatTheme.colorScheme.backgroundColor1,
                    onClick = { recordedFile?.let { onSendClick(it) } }
                )
            }
        }
    }
}

/**
 * Recorder control button with consistent styling.
 */
@Composable
private fun RecorderButton(
    icon: Int,
    contentDescription: String,
    tint: Color,
    backgroundColor: Color,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .semantics { this.contentDescription = contentDescription }
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(24.dp)
        )
    }
}

/**
 * Audio preview bubble shown in STOPPED state.
 * Displays a play button and duration.
 */
@Composable
private fun AudioPreviewBubble(
    modifier: Modifier = Modifier,
    duration: String
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(CometChatTheme.colorScheme.backgroundColor2)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.cometchat_ic_media_recorder_play),
            contentDescription = "Play recording",
            tint = CometChatTheme.colorScheme.iconTintHighlight,
            modifier = Modifier.size(20.dp)
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = "Audio Recording",
            color = CometChatTheme.colorScheme.textColorPrimary,
            style = CometChatTheme.typography.bodyRegular,
            modifier = Modifier.weight(1f)
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = duration,
            color = CometChatTheme.colorScheme.textColorSecondary,
            style = CometChatTheme.typography.caption1Regular
        )
    }
}
