package com.cometchat.uikit.compose.presentation.shared.mediarecorder.ui

import android.Manifest
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cometchat.uikit.compose.R
import com.cometchat.uikit.compose.presentation.shared.mediarecorder.MediaRecorderCallback
import com.cometchat.uikit.compose.presentation.shared.mediarecorder.MediaRecorderManager
import com.cometchat.uikit.compose.presentation.shared.mediarecorder.style.CometChatAudioVisualizerStyle
import com.cometchat.uikit.compose.presentation.shared.mediarecorder.style.CometChatMediaRecorderStyle
import com.cometchat.uikit.compose.presentation.shared.permission.rememberMultiplePermissionsState
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.uikit.core.viewmodel.CometChatMediaRecorderViewModel
import com.cometchat.uikit.core.viewmodel.MediaRecorderState
import java.io.File

/**
 * CometChatMediaRecorder provides audio recording with visualization and playback.
 *
 * This composable implements a three-state media recorder:
 * - **IDLE**: Ready to record, displays centered record button
 * - **RECORDING**: Actively recording, displays timer, audio visualizer, and control buttons
 * - **RECORDED**: Recording complete, displays playback controls with SeekBar
 *
 * Layout structure:
 * - IDLE: [Record Button centered]
 * - RECORDING: [Timer] [AudioVisualizer] + [Delete] [Stop] [Submit(disabled)]
 * - RECORDED: [Play/Pause] [SeekBar] [Duration] + [Delete] [Submit]
 *
 * Features:
 * - Real-time audio visualization during recording
 * - Playback controls with SeekBar for recorded audio preview
 * - Full style customization via [CometChatMediaRecorderStyle]
 * - Custom view slots for recording, recorded, and control button views
 * - Proper resource management and cleanup
 * - Accessibility support with content descriptions
 *
 * **Validates: Requirements 1.2, 1.4, 1.6, 5.2, 5.3, 5.4**
 *
 * @param modifier Modifier for the media recorder container
 * @param viewModel ViewModel for managing recording state (shared with chatuikit-kotlin)
 * @param style Style configuration for the media recorder
 * @param onSubmit Callback invoked when the submit button is clicked with the recorded file
 * @param onClose Callback invoked when the recording is cancelled/deleted
 * @param onError Callback invoked when an error occurs during recording/playback
 * @param recordingView Custom view for the recording state (timer and visualizer)
 * @param recordedView Custom view for the recorded state (playback controls)
 * @param controlButtonsView Custom view for the control buttons
 */
@Composable
fun CometChatMediaRecorder(
    modifier: Modifier = Modifier,
    viewModel: CometChatMediaRecorderViewModel = viewModel(),
    style: CometChatMediaRecorderStyle = CometChatMediaRecorderStyle.default(),
    // Callbacks
    onSubmit: ((File) -> Unit)? = null,
    onClose: (() -> Unit)? = null,
    onError: ((Exception) -> Unit)? = null,
    // Custom views
    recordingView: (@Composable (recordingTime: String, amplitude: Float) -> Unit)? = null,
    recordedView: (@Composable (duration: String, progress: Float, isPlaying: Boolean) -> Unit)? = null,
    controlButtonsView: (@Composable (state: MediaRecorderState) -> Unit)? = null
) {
    val context = LocalContext.current
    
    // Collect state from ViewModel
    val recordingState by viewModel.recordingState.collectAsState()
    val recordingTime by viewModel.recordingTime.collectAsState()
    val audioAmplitude by viewModel.audioAmplitude.collectAsState()
    val playbackProgress by viewModel.playbackProgress.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val recordedFile by viewModel.recordedFile.collectAsState()
    val error by viewModel.error.collectAsState()
    
    // Create MediaRecorderManager
    val mediaRecorderManager = remember { MediaRecorderManager(context) }
    
    // Set up MediaRecorderManager callback
    LaunchedEffect(mediaRecorderManager) {
        mediaRecorderManager.setCallback(object : MediaRecorderCallback {
            override fun onTimeUpdate(timeMs: Long, formattedTime: String) {
                viewModel.updateRecordingTime(timeMs)
            }
            
            override fun onAmplitudeUpdate(amplitude: Float) {
                viewModel.updateAmplitude(amplitude)
            }
            
            override fun onStateChange(state: MediaRecorderState) {
                when (state) {
                    MediaRecorderState.IDLE -> viewModel.deleteRecording()
                    MediaRecorderState.RECORDING -> viewModel.startRecording()
                    MediaRecorderState.RECORDED -> viewModel.stopRecording()
                }
            }
            
            override fun onRecordingComplete(file: File, durationMs: Long) {
                viewModel.setRecordedFile(file)
                viewModel.updateRecordingTime(durationMs)
            }
            
            override fun onPlaybackProgress(progress: Float) {
                viewModel.updatePlaybackProgress(progress)
            }
            
            override fun onPlaybackComplete() {
                viewModel.onPlaybackComplete()
            }
            
            override fun onError(exception: Exception) {
                viewModel.handleError(exception)
                onError?.invoke(exception)
            }
        })
    }
    
    // Handle error state
    LaunchedEffect(error) {
        error?.let { onError?.invoke(it) }
    }
    
    // Permission handling
    val permissions = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S_V2) {
        listOf(Manifest.permission.RECORD_AUDIO)
    } else {
        listOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }
    val permissionState = rememberMultiplePermissionsState(permissions = permissions)
    
    // Cleanup on disposal
    // **Validates: Requirements 9.1, 9.2, 9.3, 9.4, 9.5**
    DisposableEffect(Unit) {
        onDispose {
            mediaRecorderManager.release()
            viewModel.release()
        }
    }
    
    // Main container
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = style.backgroundColor,
                shape = RoundedCornerShape(style.cornerRadius)
            )
            .padding(16.dp)
            .semantics {
                contentDescription = when (recordingState) {
                    MediaRecorderState.IDLE -> "Media recorder ready to record"
                    MediaRecorderState.RECORDING -> "Recording in progress, $recordingTime elapsed"
                    MediaRecorderState.RECORDED -> "Recording complete, ready to play or submit"
                }
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // State-based content
        when (recordingState) {
            MediaRecorderState.IDLE -> {
                // IDLE State UI
                // **Validates: Requirements 1.2, 5.2**
                IdleStateContent(
                    style = style,
                    onStartRecording = {
                        if (permissionState.allPermissionsGranted) {
                            mediaRecorderManager.startRecording()
                        } else {
                            permissionState.launchPermissionsRequest()
                        }
                    }
                )
            }
            
            MediaRecorderState.RECORDING -> {
                // RECORDING State UI
                // **Validates: Requirements 1.4, 3.1, 5.3, 5.7**
                if (recordingView != null) {
                    recordingView(recordingTime, audioAmplitude)
                } else {
                    RecordingStateContent(
                        recordingTime = recordingTime,
                        amplitude = audioAmplitude,
                        style = style
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Control buttons
                if (controlButtonsView != null) {
                    controlButtonsView(recordingState)
                } else {
                    RecordingControlButtons(
                        style = style,
                        onDelete = {
                            mediaRecorderManager.deleteRecording()
                            onClose?.invoke()
                        },
                        onStop = {
                            mediaRecorderManager.stopRecording()
                        }
                    )
                }
            }
            
            MediaRecorderState.RECORDED -> {
                // RECORDED State UI - Single row layout matching Figma:
                // [Delete] [Play] [Waveform] [Duration] [Send]
                // **Validates: Requirements 1.6, 4.1, 4.4, 4.8, 5.4**
                if (recordedView != null) {
                    recordedView(recordingTime, playbackProgress, isPlaying)
                } else if (controlButtonsView != null) {
                    controlButtonsView(recordingState)
                } else {
                    RecordedStateRow(
                        duration = recordingTime,
                        progress = playbackProgress,
                        isPlaying = isPlaying,
                        style = style,
                        onPlayPauseClick = {
                            if (isPlaying) {
                                mediaRecorderManager.pausePlayback()
                                viewModel.pausePlayback()
                            } else {
                                mediaRecorderManager.startPlayback()
                                viewModel.startPlayback()
                            }
                        },
                        onDelete = {
                            mediaRecorderManager.deleteRecording()
                            onClose?.invoke()
                        },
                        onSubmit = {
                            recordedFile?.let { file ->
                                mediaRecorderManager.markAsSubmitted()
                                onSubmit?.invoke(file)
                            }
                        }
                    )
                }
            }
        }
    }
}

/**
 * IDLE state content - displays centered record button.
 * **Validates: Requirements 1.2, 5.2**
 */
@Composable
private fun IdleStateContent(
    style: CometChatMediaRecorderStyle,
    onStartRecording: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        contentAlignment = Alignment.Center
    ) {
        IconButton(
            onClick = onStartRecording,
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(style.recordButtonBackgroundColor)
                .semantics {
                    contentDescription = "Start recording"
                    role = Role.Button
                }
        ) {
            Icon(
                painter = painterResource(id = R.drawable.cometchat_ic_media_recorder_start),
                contentDescription = null,
                tint = style.startIconTint,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

/**
 * RECORDING state content - displays timer and audio visualizer.
 * **Validates: Requirements 1.4, 3.1, 5.3**
 */
@Composable
private fun RecordingStateContent(
    recordingTime: String,
    amplitude: Float,
    style: CometChatMediaRecorderStyle
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = style.recordedContainerColor,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Timer
        // **Validates: Requirements 3.1, 3.2**
        Text(
            text = recordingTime,
            color = style.timerTextColor,
            style = style.timerTextStyle,
            modifier = Modifier
                .width(60.dp)
                .semantics {
                    contentDescription = "Recording time: $recordingTime"
                }
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Audio Visualizer
        // **Validates: Requirements 2.1, 2.3, 2.4, 2.5**
        CometChatAudioVisualizer(
            modifier = Modifier.weight(1f),
            amplitude = amplitude,
            style = style.visualizerStyle,
            isAnimating = true
        )
    }
}

/**
 * RECORDED state row - single horizontal layout matching Figma design:
 * [Delete] [Play] [Waveform] [Duration] [Send]
 * **Validates: Requirements 1.6, 4.1, 4.4, 4.8, 5.4, 5.5, 5.6**
 */
@Composable
private fun RecordedStateRow(
    duration: String,
    progress: Float,
    isPlaying: Boolean,
    style: CometChatMediaRecorderStyle,
    onPlayPauseClick: () -> Unit,
    onDelete: () -> Unit,
    onSubmit: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 0.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Delete button (24x24 icon as per Figma)
        // **Validates: Requirements 5.5**
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDelete
                )
                .semantics {
                    contentDescription = "Delete recording"
                    role = Role.Button
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.cometchat_ic_media_recorder_delete),
                contentDescription = null,
                tint = style.deleteIconColor,
                modifier = Modifier.size(24.dp)
            )
        }
        
        // Play/Pause button (24x24 as per Figma)
        // **Validates: Requirements 4.1, 4.2, 4.3**
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onPlayPauseClick
                )
                .semantics {
                    contentDescription = if (isPlaying) "Pause playback" else "Play recording"
                    role = Role.Button
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(
                    id = if (isPlaying) {
                        R.drawable.cometchat_ic_media_recorder_pause
                    } else {
                        R.drawable.cometchat_ic_media_recorder_play
                    }
                ),
                contentDescription = null,
                tint = if (isPlaying) style.pauseIconTint else style.playIconTint,
                modifier = Modifier.size(24.dp)
            )
        }
        
        // Audio Waveform Visualization (matches Figma design)
        // **Validates: Requirements 2.1, 2.3, 2.4, 2.5**
        CometChatAudioVisualizer(
            modifier = Modifier
                .weight(1f)
                .height(24.dp),
            amplitude = 0f,
            style = style.visualizerStyle,
            isAnimating = isPlaying,
            progress = progress
        )
        
        // Duration text (Body/Regular 14sp, secondary color as per Figma)
        // **Validates: Requirements 4.8**
        Text(
            text = duration,
            color = CometChatTheme.colorScheme.textColorSecondary,
            style = CometChatTheme.typography.bodyRegular,
            textAlign = TextAlign.End,
            modifier = Modifier
                .semantics {
                    contentDescription = "Recording duration: $duration"
                }
        )
        
        // Send button (32x32 purple circular as per Figma)
        // **Validates: Requirements 5.6**
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(CometChatTheme.colorScheme.primary)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onSubmit
                )
                .semantics {
                    contentDescription = "Submit recording"
                    role = Role.Button
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.cometchat_ic_media_recorder_send),
                contentDescription = null,
                tint = CometChatTheme.colorScheme.colorWhite,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/**
 * RECORDED state content - displays delete, play/pause button, waveform, duration, and send button.
 * Matches Figma design: [Delete] [Play] [Waveform] [Duration] [Send]
 * **Validates: Requirements 1.6, 4.1, 4.4, 4.8**
 * @deprecated Use RecordedStateRow instead for single-row layout matching Figma
 */
@Composable
private fun RecordedStateContent(
    duration: String,
    progress: Float,
    isPlaying: Boolean,
    style: CometChatMediaRecorderStyle,
    onPlayPauseClick: () -> Unit,
    onSeek: (Float) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Play/Pause button (24x24 as per Figma)
        // **Validates: Requirements 4.1, 4.2, 4.3**
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onPlayPauseClick
                )
                .semantics {
                    contentDescription = if (isPlaying) "Pause playback" else "Play recording"
                    role = Role.Button
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(
                    id = if (isPlaying) {
                        R.drawable.cometchat_ic_media_recorder_pause
                    } else {
                        R.drawable.cometchat_ic_media_recorder_play
                    }
                ),
                contentDescription = null,
                tint = if (isPlaying) style.pauseIconTint else style.playIconTint,
                modifier = Modifier.size(24.dp)
            )
        }
        
        // Audio Waveform Visualization (matches Figma design)
        // **Validates: Requirements 2.1, 2.3, 2.4, 2.5**
        CometChatAudioVisualizer(
            modifier = Modifier
                .weight(1f)
                .height(24.dp),
            amplitude = progress, // Use progress to show playback position
            style = style.visualizerStyle.copy(
                barColor = style.visualizerStyle.barColor.copy(alpha = 0.2f),
                activeBarColor = style.visualizerStyle.barColor
            ),
            isAnimating = isPlaying,
            progress = progress
        )
        
        // Duration text (Body/Regular 14sp, secondary color as per Figma)
        // **Validates: Requirements 4.8**
        Text(
            text = duration,
            color = style.timerTextColor,
            style = style.timerTextStyle,
            textAlign = TextAlign.End,
            modifier = Modifier
                .semantics {
                    contentDescription = "Recording duration: $duration"
                }
        )
    }
}

/**
 * Control buttons for RECORDING state.
 * **Validates: Requirements 5.3, 5.7**
 */
@Composable
private fun RecordingControlButtons(
    style: CometChatMediaRecorderStyle,
    onDelete: () -> Unit,
    onStop: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Delete button
        // **Validates: Requirements 5.5**
        ControlButton(
            iconRes = R.drawable.cometchat_ic_media_recorder_delete,
            contentDescription = "Delete recording",
            tint = style.deleteIconColor,
            backgroundColor = style.deleteButtonBackgroundColor,
            onClick = onDelete
        )
        
        // Stop button
        ControlButton(
            iconRes = R.drawable.cometchat_ic_media_recorder_stop,
            contentDescription = "Stop recording",
            tint = style.stopIconTint,
            backgroundColor = style.stopButtonBackgroundColor,
            onClick = onStop
        )
        
        // Submit button (disabled during recording)
        // **Validates: Requirements 5.7**
        ControlButton(
            iconRes = R.drawable.cometchat_ic_media_recorder_send,
            contentDescription = "Submit recording (disabled)",
            tint = style.submitIconTint.copy(alpha = 0.4f),
            backgroundColor = style.submitButtonBackgroundColor,
            onClick = { /* Disabled during recording */ },
            enabled = false
        )
    }
}

/**
 * Control buttons for RECORDED state - matches Figma design.
 * Layout: [Delete] [Content] [Send (purple circular)]
 * **Validates: Requirements 5.4**
 */
@Composable
private fun RecordedControlButtons(
    style: CometChatMediaRecorderStyle,
    onDelete: () -> Unit,
    onSubmit: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Delete button (24x24 icon)
        // **Validates: Requirements 5.5**
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDelete
                )
                .semantics {
                    contentDescription = "Delete recording"
                    role = Role.Button
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.cometchat_ic_media_recorder_delete),
                contentDescription = null,
                tint = style.deleteIconColor,
                modifier = Modifier.size(24.dp)
            )
        }
        
        // Send button (32x32 purple circular as per Figma)
        // **Validates: Requirements 5.6**
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(CometChatTheme.colorScheme.primary)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onSubmit
                )
                .semantics {
                    contentDescription = "Submit recording"
                    role = Role.Button
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.cometchat_ic_media_recorder_send),
                contentDescription = null,
                tint = CometChatTheme.colorScheme.colorWhite,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/**
 * Reusable control button component.
 * **Validates: Requirements 10.4** (Accessibility)
 */
@Composable
private fun ControlButton(
    iconRes: Int,
    contentDescription: String,
    tint: Color,
    backgroundColor: Color,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .semantics {
                this.contentDescription = contentDescription
                role = Role.Button
            }
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(24.dp)
        )
    }
}
