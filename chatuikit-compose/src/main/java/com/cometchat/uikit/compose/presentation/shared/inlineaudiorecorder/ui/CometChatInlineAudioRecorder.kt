package com.cometchat.uikit.compose.presentation.shared.inlineaudiorecorder.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cometchat.uikit.compose.R
import com.cometchat.uikit.compose.presentation.shared.inlineaudiorecorder.style.CometChatInlineAudioRecorderStyle
import com.cometchat.uikit.compose.presentation.shared.inlineaudiorecorder.utils.InlineAudioRecorderCallback
import com.cometchat.uikit.compose.presentation.shared.inlineaudiorecorder.utils.InlineAudioRecorderManager
import com.cometchat.uikit.compose.presentation.shared.permission.PermissionType
import com.cometchat.uikit.compose.presentation.shared.permission.getPermissionsForType
import com.cometchat.uikit.compose.presentation.shared.permission.rememberMultiplePermissionsState
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.uikit.core.viewmodel.CometChatInlineAudioRecorderViewModel
import com.cometchat.uikit.core.viewmodel.InlineAudioRecorderStatus
import kotlinx.coroutines.launch
import java.io.File

/**
 * Enum to track the direction of dismiss animation.
 */
private enum class DismissDirection {
    LEFT,   // Slide left when delete is clicked
    RIGHT,  // Slide right when send is clicked
    NONE    // No directional animation (default hide)
}

/**
 * CometChatInlineAudioRecorder provides inline audio recording within the message composer.
 *
 * This composable implements a six-state media recorder:
 * - **IDLE**: Not visible (composer shows normal input)
 * - **RECORDING**: Actively recording, shows animated waveform
 * - **PAUSED**: Recording paused, shows static waveform with resume option
 * - **COMPLETED**: Recording complete, shows playback controls
 * - **PLAYING**: Playing back, shows progress on waveform
 * - **ERROR**: Error state with recovery option
 *
 * Layout structure (single row):
 * [Delete] [Record/Play] [Waveform] [Duration] [Pause/Mic] [Send]
 *
 * Features:
 * - Real-time waveform visualization during recording (bars scroll left)
 * - Pause/Resume recording (API 24+ for true pause)
 * - Playback with waveform progress visualization
 * - Seeking via tap or drag on waveform
 * - Amplitude storage for playback visualization
 * - Full style customization
 *
 * **Validates: Requirements 1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 1.7, 1.8, 1.9, 1.10, 1.11, 1.12, 1.13, 1.14, 1.15, 2.1, 2.2, 2.3, 2.4, 2.5**
 *
 * @param modifier Modifier for the recorder container
 * @param viewModel ViewModel for managing recording state (shared with chatuikit-kotlin)
 * @param style Style configuration for the recorder
 * @param onSubmit Callback invoked when the submit button is clicked with the recorded file
 * @param onCancel Callback invoked when the recording is cancelled/deleted
 * @param onError Callback invoked when an error occurs during recording/playback
 * @param waveformView Custom view for the waveform visualization
 * @param controlsView Custom view for the control buttons
 */
@Composable
fun CometChatInlineAudioRecorder(
    modifier: Modifier = Modifier,
    viewModel: CometChatInlineAudioRecorderViewModel = viewModel(),
    style: CometChatInlineAudioRecorderStyle = CometChatInlineAudioRecorderStyle.default(),
    // Callbacks
    onSubmit: ((File) -> Unit)? = null,
    onCancel: (() -> Unit)? = null,
    onError: ((String) -> Unit)? = null,
    // Custom views
    waveformView: (@Composable (amplitudes: List<Float>, progress: Float, isRecording: Boolean) -> Unit)? = null,
    controlsView: (@Composable (status: InlineAudioRecorderStatus) -> Unit)? = null
) {
    val context = LocalContext.current
    
    // Collect state from ViewModel
    val state by viewModel.state.collectAsState()
    val status = state.status
    val duration = state.duration
    val currentPosition = state.currentPosition
    val amplitudes = state.amplitudes
    val filePath = state.filePath
    val errorMessage = state.errorMessage
    
    // Create InlineAudioRecorderManager
    val recorderManager = remember { InlineAudioRecorderManager(context) }
    
    // Set up InlineAudioRecorderManager callback
    LaunchedEffect(recorderManager) {
        recorderManager.setCallback(object : InlineAudioRecorderCallback {
            override fun onDurationUpdate(durationMs: Long) {
                viewModel.updateDuration(durationMs)
            }
            
            override fun onAmplitudeUpdate(amplitude: Float) {
                viewModel.addAmplitude(amplitude)
            }
            
            override fun onStatusChange(newStatus: InlineAudioRecorderStatus) {
                // Status changes are handled by the manager calling specific methods
            }
            
            override fun onRecordingComplete(file: File, durationMs: Long, amplitudeList: List<Float>) {
                viewModel.setFilePath(file.absolutePath)
                viewModel.updateDuration(durationMs)
            }
            
            override fun onPlaybackPositionUpdate(positionMs: Long) {
                viewModel.updatePosition(positionMs)
            }
            
            override fun onPlaybackComplete() {
                viewModel.onPlaybackComplete()
            }
            
            override fun onError(message: String) {
                viewModel.handleError(message)
                onError?.invoke(message)
            }
        })
    }
    
    // Handle error state
    LaunchedEffect(errorMessage) {
        errorMessage?.let { onError?.invoke(it) }
    }
    
    // Permission handling using CometChatPermissionHandler
    val permissions = remember { getPermissionsForType(PermissionType.MICROPHONE) }
    var pendingRecordingStart by remember { mutableStateOf(false) }
    
    // Use rememberUpdatedState to ensure the callback always has the latest values
    val currentRecorderManager by rememberUpdatedState(recorderManager)
    val currentViewModel by rememberUpdatedState(viewModel)
    val currentOnError by rememberUpdatedState(onError)
    val currentPendingRecordingStart by rememberUpdatedState(pendingRecordingStart)
    
    val permissionState = rememberMultiplePermissionsState(
        permissions = permissions,
        onPermissionsResult = { grantedPermissions, deniedPermissions ->
            val allGranted = deniedPermissions.isEmpty()
            if (allGranted) {
                // Always try to start recording when permission is granted
                // The pendingRecordingStart flag may not be reliable due to recomposition
                pendingRecordingStart = false
                // Only start if we're still in IDLE state (not already recording)
                if (currentViewModel.status == InlineAudioRecorderStatus.IDLE) {
                    val started = currentRecorderManager.startRecording()
                    if (started) {
                        currentViewModel.startRecording()
                    }
                }
            } else if (currentPendingRecordingStart) {
                pendingRecordingStart = false
                currentOnError?.invoke("RECORD_AUDIO permission denied")
            }
        }
    )
    
    // Function to start recording with permission check
    val startRecordingWithPermissionCheck: () -> Unit = {
        if (permissionState.allPermissionsGranted) {
            val started = recorderManager.startRecording()
            if (started) {
                viewModel.startRecording()
            }
        } else {
            pendingRecordingStart = true
            permissionState.launchPermissionsRequest()
        }
    }
    
    // Auto-start recording when component is first shown (status is IDLE)
    // This handles the case when the inline recorder is displayed
    LaunchedEffect(status) {
        if (status == InlineAudioRecorderStatus.IDLE) {
            startRecordingWithPermissionCheck()
        }
    }
    
    // Cleanup on disposal
    // **Validates: Requirements 13.1, 13.2, 13.3, 13.4, 13.5, 13.6**
    DisposableEffect(Unit) {
        onDispose {
            recorderManager.release()
            viewModel.release()
        }
    }
    
    // Calculate progress for playback visualization
    val progress = if (duration > 0) {
        (currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }
    
    // Determine display time based on status
    val displayTime = when (status) {
        InlineAudioRecorderStatus.PLAYING -> viewModel.formattedPosition
        else -> viewModel.formattedDuration
    }
    
    // State for directional slide animation
    var dismissDirection by remember { mutableStateOf(DismissDirection.NONE) }
    val offsetX = remember { Animatable(0f) }
    val animatedAlpha = remember { Animatable(1f) }
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    
    // Function to handle dismiss with slide animation
    val dismissWithSlide: (DismissDirection, () -> Unit) -> Unit = { direction, onComplete ->
        dismissDirection = direction
        coroutineScope.launch {
            // Calculate target offset based on direction (slide by screen width)
            val targetOffset = when (direction) {
                DismissDirection.LEFT -> -500f  // Slide left
                DismissDirection.RIGHT -> 500f  // Slide right
                DismissDirection.NONE -> 0f
            }
            
            // Animate offset and alpha simultaneously
            launch {
                offsetX.animateTo(
                    targetValue = targetOffset,
                    animationSpec = tween(durationMillis = 250)
                )
            }
            launch {
                animatedAlpha.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = 250)
                )
            }.join()
            
            // Execute completion callback after animation
            onComplete()
            
            // Reset animation state
            offsetX.snapTo(0f)
            animatedAlpha.snapTo(1f)
            dismissDirection = DismissDirection.NONE
        }
    }
    
    // Main container - animated visibility when showing/hiding
    // Container styling matches MessageComposer compose box
    AnimatedVisibility(
        visible = status != InlineAudioRecorderStatus.IDLE,
        enter = expandVertically(
            animationSpec = tween(durationMillis = 250),
            expandFrom = Alignment.Top
        ) + fadeIn(animationSpec = tween(durationMillis = 250)),
        exit = shrinkVertically(
            animationSpec = tween(durationMillis = 200),
            shrinkTowards = Alignment.Top
        ) + fadeOut(animationSpec = tween(durationMillis = 200))
    ) {
        val containerModifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                translationX = offsetX.value
                alpha = animatedAlpha.value
            }
            .background(
                color = style.backgroundColor,
                shape = RoundedCornerShape(style.borderRadius)
            )
            .then(
                if (style.border != null) {
                    Modifier.border(style.border, RoundedCornerShape(style.borderRadius))
                } else {
                    Modifier
                }
            )
            .padding(12.dp)  // Figma: 12dp padding inside compose box (same as MessageComposer)
            .semantics {
                contentDescription = when (status) {
                    InlineAudioRecorderStatus.RECORDING -> "Recording in progress, $displayTime elapsed"
                    InlineAudioRecorderStatus.PAUSED -> "Recording paused at $displayTime"
                    InlineAudioRecorderStatus.COMPLETED -> "Recording complete, $displayTime duration"
                    InlineAudioRecorderStatus.PLAYING -> "Playing recording, $displayTime"
                    InlineAudioRecorderStatus.ERROR -> "Recording error: $errorMessage"
                    else -> "Audio recorder"
                }
            }
        
        when (status) {
            InlineAudioRecorderStatus.ERROR -> {
                // ERROR State UI
                // **Validates: Requirements 1.15**
                ErrorStateContent(
                    errorMessage = errorMessage ?: "Unknown error",
                    style = style,
                    onRecover = {
                        viewModel.recover()
                        recorderManager.deleteRecording()
                    },
                    modifier = containerModifier
                )
            }
            else -> {
                // Use custom controls view if provided
                if (controlsView != null) {
                    controlsView(status)
                } else {
                    // Standard layout matching XML: [Delete] [8dp] [Record/Play] [8dp] [Waveform (weight=1)] [8dp] [Duration (48dp)] [8dp] [Pause/Mic] [8dp] [Send]
                    Row(
                        modifier = containerModifier,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Delete button (always visible)
                        // **Validates: Requirements 9.1, 9.2**
                        DeleteButton(
                            style = style,
                            onClick = {
                                // Slide left animation when delete is clicked
                                dismissWithSlide(DismissDirection.LEFT) {
                                    recorderManager.deleteRecording()
                                    viewModel.deleteRecording()
                                    onCancel?.invoke()
                                }
                            }
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        // Record/Play button area
                        // **Validates: Requirements 9.4, 9.5, 9.6**
                        RecordPlayButton(
                            status = status,
                            style = style,
                            onRecordClick = {
                                // This shouldn't happen in non-IDLE states
                            },
                            onPlayClick = {
                                if (status == InlineAudioRecorderStatus.PAUSED) {
                                    // Stop recording first to finalize the file, then start playback
                                    recorderManager.stopRecording()
                                    viewModel.stopRecording()
                                    // Start playback after stopping
                                    recorderManager.startPlayback()
                                    viewModel.startPlayback()
                                } else if (status == InlineAudioRecorderStatus.COMPLETED) {
                                    recorderManager.startPlayback()
                                    viewModel.startPlayback()
                                } else if (status == InlineAudioRecorderStatus.PLAYING) {
                                    recorderManager.pausePlayback()
                                    viewModel.pausePlayback()
                                }
                            }
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        // Waveform visualization - takes all remaining space
                        // **Validates: Requirements 3.1, 3.3, 3.4, 3.5, 4.1, 4.2, 4.3, 4.4**
                        if (waveformView != null) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(24.dp)
                            ) {
                                waveformView(
                                    amplitudes,
                                    progress,
                                    status == InlineAudioRecorderStatus.RECORDING
                                )
                            }
                        } else {
                            CometChatInlineAudioWaveform(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(24.dp),
                                amplitudes = amplitudes,
                                progress = progress,
                                isRecording = status == InlineAudioRecorderStatus.RECORDING,
                                isPlaying = status == InlineAudioRecorderStatus.PLAYING,
                                style = style.waveformStyle,
                                onSeek = if (status == InlineAudioRecorderStatus.COMPLETED || 
                                             status == InlineAudioRecorderStatus.PLAYING) {
                                    { seekProgress ->
                                        val seekPositionMs = (seekProgress * duration).toLong()
                                        recorderManager.seekTo(seekPositionMs)
                                        viewModel.seekTo(seekPositionMs)
                                    }
                                } else null
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        // Duration text with fixed width (48dp matching XML)
                        // **Validates: Requirements 7.1, 7.2, 7.3, 7.4, 7.6, 7.7**
                        Text(
                            text = displayTime,
                            color = style.durationTextColor,
                            style = style.durationTextStyle,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier
                                .width(48.dp)
                                .semantics {
                                    contentDescription = "Duration: $displayTime"
                                }
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        // Pause/Mic button
                        // **Validates: Requirements 9.7, 9.8, 9.9, 9.10**
                        PauseMicButton(
                            status = status,
                            style = style,
                            isPauseResumeSupported = recorderManager.isPauseResumeSupported(),
                            onPauseClick = {
                                recorderManager.pauseRecording()
                                viewModel.pauseRecording()
                            },
                            onResumeClick = {
                                recorderManager.resumeRecording()
                                viewModel.resumeRecording()
                            }
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        // Send button
                        // **Validates: Requirements 9.11**
                        SendButton(
                            style = style,
                            onClick = {
                                // Stop recording BEFORE animation to ensure file is finalized
                                // Capture the current status before any state changes
                                val currentStatus = status
                                if (currentStatus == InlineAudioRecorderStatus.RECORDING || 
                                    currentStatus == InlineAudioRecorderStatus.PAUSED) {
                                    recorderManager.stopRecording()
                                    viewModel.stopRecording()
                                }
                                
                                // Get the file AFTER stopping (file is now finalized)
                                val recordedFile = recorderManager.getRecordedFile()
                                
                                // Slide right animation when send is clicked
                                dismissWithSlide(DismissDirection.RIGHT) {
                                    // Submit the file after animation
                                    recordedFile?.let { file ->
                                        if (file.exists() && file.length() > 0) {
                                            onSubmit?.invoke(file)
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Delete button component.
 * **Validates: Requirements 9.1, 9.2**
 */
@Composable
private fun DeleteButton(
    style: CometChatInlineAudioRecorderStyle,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(style.deleteButtonBackgroundColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
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
            tint = style.deleteButtonIconColor,
            modifier = Modifier.size(20.dp)
        )
    }
}


/**
 * Record/Play button component.
 * Shows different icons based on status:
 * - RECORDING: Red pulsing dot
 * - PAUSED/COMPLETED: Play button
 * - PLAYING: Pause button
 * **Validates: Requirements 9.4, 9.5, 9.6**
 */
@Composable
private fun RecordPlayButton(
    status: InlineAudioRecorderStatus,
    style: CometChatInlineAudioRecorderStyle,
    onRecordClick: () -> Unit,
    onPlayClick: () -> Unit
) {
    when (status) {
        InlineAudioRecorderStatus.RECORDING -> {
            // Red pulsing dot indicator
            // **Validates: Requirements 1.4, 2.2**
            PulsingRecordIndicator(style = style)
        }
        InlineAudioRecorderStatus.PAUSED, InlineAudioRecorderStatus.COMPLETED -> {
            // Play button
            // **Validates: Requirements 9.5**
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(style.playButtonBackgroundColor)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onPlayClick
                    )
                    .semantics {
                        contentDescription = "Play recording"
                        role = Role.Button
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.cometchat_ic_media_recorder_play),
                    contentDescription = null,
                    tint = style.playButtonIconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        InlineAudioRecorderStatus.PLAYING -> {
            // Pause button
            // **Validates: Requirements 9.6**
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(style.pauseButtonBackgroundColor)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onPlayClick
                    )
                    .semantics {
                        contentDescription = "Pause playback"
                        role = Role.Button
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.cometchat_ic_media_recorder_pause),
                    contentDescription = null,
                    tint = style.pauseButtonIconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        else -> {
            // Placeholder for other states
            Spacer(modifier = Modifier.size(24.dp))
        }
    }
}

/**
 * Pulsing red dot indicator for recording state.
 * **Validates: Requirements 1.4, 2.2**
 */
@Composable
private fun PulsingRecordIndicator(style: CometChatInlineAudioRecorderStyle) {
    val infiniteTransition = rememberInfiniteTransition(label = "recordingPulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )
    
    Box(
        modifier = Modifier
            .size(24.dp)
            .semantics {
                contentDescription = "Recording in progress"
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .alpha(alpha)
                .clip(CircleShape)
                .background(style.recordingIndicatorColor)
        )
    }
}

/**
 * Pause/Mic button component.
 * Shows different icons based on status:
 * - RECORDING: Pause button
 * - PAUSED: Mic/Resume button
 * - COMPLETED/PLAYING: Mic button (disabled)
 * **Validates: Requirements 9.7, 9.8, 9.9, 9.10**
 */
@Composable
private fun PauseMicButton(
    status: InlineAudioRecorderStatus,
    style: CometChatInlineAudioRecorderStyle,
    isPauseResumeSupported: Boolean,
    onPauseClick: () -> Unit,
    onResumeClick: () -> Unit
) {
    when (status) {
        InlineAudioRecorderStatus.RECORDING -> {
            // Pause button (only if pause/resume is supported)
            // **Validates: Requirements 9.7, 9.8**
            if (isPauseResumeSupported) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(style.pauseButtonBackgroundColor)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onPauseClick
                        )
                        .semantics {
                            contentDescription = "Pause recording"
                            role = Role.Button
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.cometchat_ic_media_recorder_pause),
                        contentDescription = null,
                        tint = style.pauseButtonIconColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else {
                // Show disabled mic button if pause not supported
                DisabledMicButton(style = style)
            }
        }
        InlineAudioRecorderStatus.PAUSED -> {
            // Mic/Resume button
            // **Validates: Requirements 9.9**
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(style.micButtonBackgroundColor)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onResumeClick
                    )
                    .semantics {
                        contentDescription = "Resume recording"
                        role = Role.Button
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.cometchat_ic_mic),
                    contentDescription = null,
                    tint = style.micButtonIconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        InlineAudioRecorderStatus.COMPLETED, InlineAudioRecorderStatus.PLAYING -> {
            // Disabled mic button
            // **Validates: Requirements 9.10**
            DisabledMicButton(style = style)
        }
        else -> {
            Spacer(modifier = Modifier.size(24.dp))
        }
    }
}

/**
 * Disabled mic button component.
 * **Validates: Requirements 9.10**
 */
@Composable
private fun DisabledMicButton(style: CometChatInlineAudioRecorderStyle) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(style.micButtonBackgroundColor)
            .semantics {
                contentDescription = "Microphone (disabled)"
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = R.drawable.cometchat_ic_mic),
            contentDescription = null,
            tint = style.micButtonIconColor.copy(alpha = 0.4f),
            modifier = Modifier.size(20.dp)
        )
    }
}

/**
 * Send button component - matches the MessageComposer send button style.
 * 32dp circular button with primary background color.
 * **Validates: Requirements 9.11**
 */
@Composable
private fun SendButton(
    style: CometChatInlineAudioRecorderStyle,
    onClick: () -> Unit
) {
    // Use Card for consistent styling with MessageComposer send button
    androidx.compose.material3.Card(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .semantics {
                contentDescription = "Send recording"
                role = Role.Button
            },
        shape = CircleShape,
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = style.sendButtonBackgroundColor
        ),
        elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier.size(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.cometchat_ic_send_active),
                contentDescription = null,
                tint = style.sendButtonIconColor,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

/**
 * Error state content.
 * **Validates: Requirements 1.15**
 */
@Composable
private fun ErrorStateContent(
    errorMessage: String,
    style: CometChatInlineAudioRecorderStyle,
    onRecover: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Error icon
        Icon(
            painter = painterResource(id = R.drawable.cometchat_ic_alert_circle),
            contentDescription = null,
            tint = CometChatTheme.colorScheme.errorColor,
            modifier = Modifier.size(24.dp)
        )
        
        // Error message
        Text(
            text = errorMessage,
            color = CometChatTheme.colorScheme.errorColor,
            style = CometChatTheme.typography.bodyRegular,
            modifier = Modifier
                .weight(1f)
                .semantics {
                    contentDescription = "Error: $errorMessage"
                }
        )
        
        // Retry button
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(CometChatTheme.colorScheme.primary)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onRecover
                )
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .semantics {
                    contentDescription = "Retry"
                    role = Role.Button
                }
        ) {
            Text(
                text = "Retry",
                color = CometChatTheme.colorScheme.colorWhite,
                style = CometChatTheme.typography.buttonMedium
            )
        }
    }
}

/**
 * Starts the inline audio recorder from IDLE state.
 * This is a convenience function to be called from the MessageComposer.
 *
 * @param viewModel The ViewModel to update
 * @param recorderManager The manager to start recording
 * @param permissionState The permission state to check
 * @return true if recording started successfully
 */
fun startInlineRecording(
    viewModel: CometChatInlineAudioRecorderViewModel,
    recorderManager: InlineAudioRecorderManager
): Boolean {
    if (viewModel.status != InlineAudioRecorderStatus.IDLE) {
        return false
    }
    
    val started = recorderManager.startRecording()
    if (started) {
        viewModel.startRecording()
    }
    return started
}
