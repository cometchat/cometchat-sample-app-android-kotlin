package com.cometchat.uikit.compose.presentation.shared.mediarecorder

import android.Manifest
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaRecorder
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cometchat.uikit.compose.R
import com.cometchat.uikit.compose.presentation.shared.permission.rememberMultiplePermissionsState
import com.cometchat.uikit.compose.theme.CometChatTheme
import java.io.File
import java.util.*

/**
 * CometChatMediaRecorder is a Jetpack Compose component for recording and playing audio.
 * It provides functionality for recording audio, playing recorded audio, and visualizing the audio recording.
 *
 * Features:
 * - Audio recording with start, pause, resume, stop functionality
 * - Visual feedback with animated ripple effects during recording
 * - Permission handling for microphone access
 * - Audio focus management for proper recording behavior
 * - Customizable styling through MediaRecorderStyle
 * - Integration with CometChat theming system
 *
 * @param modifier Modifier for the media recorder container
 * @param style Styling configuration for the media recorder
 * @param onClose Callback invoked when the close/cancel action is triggered
 * @param onSubmit Callback invoked when the submit action is triggered with the recorded file
 *
 * @sample
 * ```
 * // Basic usage
 * CometChatMediaRecorder(
 *     onClose = { /* Handle close */ },
 *     onSubmit = { file, context -> /* Handle submit */ }
 * )
 *
 * // With custom styling
 * CometChatMediaRecorder(
 *     style = MediaRecorderStyle(
 *         backgroundColor = CometChatTheme.colorScheme.backgroundColor2,
 *         recordingIconBackgroundColor = CometChatTheme.colorScheme.primary,
 *         cornerRadius = 12.dp
 *     ),
 *     onClose = { /* Handle close */ },
 *     onSubmit = { file, context -> /* Handle submit */ }
 * )
 * ```
 */
@Composable
fun CometChatMediaRecorder(
    modifier: Modifier = Modifier,
    style: MediaRecorderStyle = MediaRecorderStyle(),
    autoStartRecording: Boolean = true,
    onClose: (() -> Unit)? = null,
    onSubmit: ((File?, Context) -> Unit)? = null
) {
    val context = LocalContext.current
    val colors = CometChatTheme.colorScheme
    val typography = style.typography ?: CometChatTheme.typography
    
    // Resolve colors from style or use theme defaults
    val backgroundColor = style.backgroundColor ?: colors.backgroundColor1
    val strokeColor = style.strokeColor ?: colors.strokeColorLight
    val recordingIconTint = style.recordingIconTint ?: colors.colorWhite
    val recordingIconBackgroundColor = style.recordingIconBackgroundColor ?: colors.iconTintHighlight
    val textColor = style.textColor ?: colors.textColorPrimary
    val textStyle = style.textStyle ?: typography.heading4Regular
    val deleteIconTint = style.deleteIconTint ?: colors.iconTintSecondary
    val deleteIconBackgroundColor = style.deleteIconBackgroundColor ?: colors.backgroundColor1
    val startIconTint = style.startIconTint ?: colors.errorColor
    val startIconBackgroundColor = style.startIconBackgroundColor ?: colors.backgroundColor1
    val pauseIconTint = style.pauseIconTint ?: colors.errorColor
    val pauseIconBackgroundColor = style.pauseIconBackgroundColor ?: colors.backgroundColor1
    val stopIconTint = style.stopIconTint ?: colors.iconTintSecondary
    val stopIconBackgroundColor = style.stopIconBackgroundColor ?: colors.backgroundColor1
    val sendIconTint = style.sendIconTint ?: colors.iconTintHighlight
    val sendIconBackgroundColor = style.sendIconBackgroundColor ?: colors.backgroundColor1
    val restartIconTint = style.restartIconTint ?: colors.iconTintSecondary
    val restartIconBackgroundColor = style.restartIconBackgroundColor ?: colors.backgroundColor1
    val messageBubbleBackgroundColor = style.messageBubbleBackgroundColor ?: colors.extendedPrimaryColor500
    val rippleColor = style.rippleColor ?: recordingIconBackgroundColor
    var recordingState by remember { mutableStateOf(RecordingState.START) }
    var recordingTime by remember { mutableStateOf("00:00") }
    var recordedFilePath by remember { mutableStateOf<String?>(null) }
    var mediaRecorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var startTime by remember { mutableLongStateOf(0L) }
    var pauseTime by remember { mutableLongStateOf(0L) }
    var isRecording by remember { mutableStateOf(false) }
    
    val handler = remember { Handler(Looper.getMainLooper()) }
    val timerRunnable = remember {
        object : Runnable {
            override fun run() {
                if (isRecording) {
                    val elapsedTime = System.currentTimeMillis() - startTime
                    val seconds = (elapsedTime / 1000).toInt()
                    val minutes = seconds / 60
                    val secs = seconds % 60
                    recordingTime = String.format(Locale.US, "%02d:%02d", minutes, secs)
                    handler.postDelayed(this, 500)
                }
            }
        }
    }

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

    // Audio focus management
    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    val audioFocusRequest = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()
            AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE)
                .setAudioAttributes(audioAttributes)
                .setOnAudioFocusChangeListener { focusChange ->
                    if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT || 
                        focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                        stopRecording(
                            mediaRecorder = mediaRecorder,
                            handler = handler,
                            timerRunnable = timerRunnable,
                            onStateChange = { recordingState = it },
                            onRecordingChange = { isRecording = it },
                            onTimeChange = { recordingTime = it },
                            onRecorderChange = { mediaRecorder = it }
                        )
                    }
                }
                .setAcceptsDelayedFocusGain(true)
                .build()
        } else null
    }

    // Request audio focus
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && audioFocusRequest != null) {
            audioManager.requestAudioFocus(audioFocusRequest)
        } else {
            audioManager.requestAudioFocus(
                { },
                AudioManager.STREAM_VOICE_CALL,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE
            )
        }
    }

    // Auto-start recording when component is shown
    LaunchedEffect(autoStartRecording, permissionState.allPermissionsGranted) {
        if (autoStartRecording && recordingState == RecordingState.START) {
            if (permissionState.allPermissionsGranted) {
                startRecording(
                    context = context,
                    onStateChange = { recordingState = it },
                    onRecordingChange = { isRecording = it },
                    onStartTimeChange = { startTime = it },
                    onRecorderChange = { mediaRecorder = it },
                    onFilePathChange = { recordedFilePath = it },
                    handler = handler,
                    timerRunnable = timerRunnable
                )
            } else {
                permissionState.launchPermissionsRequest()
            }
        }
    }

    // Cleanup on dispose
    DisposableEffect(Unit) {
        onDispose {
            stopRecording(
                mediaRecorder = mediaRecorder,
                handler = handler,
                timerRunnable = timerRunnable,
                onStateChange = { recordingState = it },
                onRecordingChange = { isRecording = it },
                onTimeChange = { recordingTime = it },
                onRecorderChange = { mediaRecorder = it }
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && audioFocusRequest != null) {
                audioManager.abandonAudioFocusRequest(audioFocusRequest)
            } else {
                audioManager.abandonAudioFocus { }
            }
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = if (style.strokeWidth > 0.dp) {
            BorderStroke(style.strokeWidth, strokeColor)
        } else null,
        shape = RoundedCornerShape(
            topStart = style.cornerRadius,
            topEnd = style.cornerRadius,
            bottomStart = 0.dp,
            bottomEnd = 0.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 5.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Popup slider indicator
            Box(
                modifier = Modifier
                    .width(32.dp)
                    .height(4.dp)
                    .padding(top = 3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(colors.neutralColor300.copy(alpha = 0.3f))
            )

            Spacer(modifier = Modifier.height(5.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(5.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (recordingState) {
                    RecordingState.START, RecordingState.RECORDING, RecordingState.PAUSED -> {
                        // Pre-recording view
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                contentAlignment = Alignment.Center
                            ) {
                                // Ripple effect
                                if (recordingState == RecordingState.RECORDING) {
                                    AudioCircleRippleView(
                                        size = 120.dp,
                                        color = rippleColor,
                                        isAnimating = true,
                                        rippleCount = style.rippleCount,
                                        animationDuration = style.rippleAnimationDuration
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier.size(120.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        // Static circle
                                        Box(
                                            modifier = Modifier
                                                .size(80.dp)
                                                .clip(CircleShape)
                                                .background(recordingIconBackgroundColor),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.cometchat_ic_media_recorder_icon),
                                                contentDescription = "Recording Icon",
                                                tint = recordingIconTint,
                                                modifier = Modifier.size(40.dp)
                                            )
                                        }
                                    }
                                }

                                // Center recording icon
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(CircleShape)
                                        .background(recordingIconBackgroundColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.cometchat_ic_media_recorder_icon),
                                        contentDescription = "Recording Icon",
                                        tint = recordingIconTint,
                                        modifier = Modifier.size(40.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = recordingTime,
                                style = textStyle,
                                color = textColor,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.height(19.dp)
                            )
                        }
                    }
                    RecordingState.STOPPED -> {
                        // Post-recording view with audio bubble
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = messageBubbleBackgroundColor),
                            border = if (style.messageBubbleStrokeWidth > 0.dp) {
                                BorderStroke(
                                    style.messageBubbleStrokeWidth,
                                    style.messageBubbleStrokeColor ?: colors.strokeColorLight
                                )
                            } else null,
                            shape = RoundedCornerShape(style.messageBubbleCornerRadius)
                        ) {
                            // Audio bubble placeholder - you would integrate with your actual audio bubble component
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(60.dp)
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Audio Recording",
                                    style = typography.bodyRegular,
                                    color = colors.textColorPrimary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Control buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.weight(1f))

                    // Left button (Delete)
                    if (recordingState != RecordingState.START) {
                        Card(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape),
                            colors = CardDefaults.cardColors(containerColor = deleteIconBackgroundColor),
                            border = if (style.deleteIconStrokeWidth > 0.dp) {
                                BorderStroke(
                                    style.deleteIconStrokeWidth,
                                    style.deleteIconStrokeColor ?: colors.strokeColorLight
                                )
                            } else null,
                            shape = CircleShape,
                            elevation = CardDefaults.cardElevation(defaultElevation = style.deleteIconElevation)
                        ) {
                            IconButton(
                                onClick = {
                                    deleteRecording(
                                        mediaRecorder = mediaRecorder,
                                        handler = handler,
                                        timerRunnable = timerRunnable,
                                        onStateChange = { recordingState = it },
                                        onRecordingChange = { isRecording = it },
                                        onTimeChange = { recordingTime = it },
                                        onRecorderChange = { mediaRecorder = it },
                                        onClose = onClose
                                    )
                                },
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.cometchat_ic_media_recorder_delete),
                                    contentDescription = "Delete",
                                    tint = deleteIconTint,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(20.dp))

                    // Center button (Start/Pause/Resume/Send)
                    Card(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape),
                        colors = CardDefaults.cardColors(
                            containerColor = when (recordingState) {
                                RecordingState.START, RecordingState.PAUSED -> startIconBackgroundColor
                                RecordingState.RECORDING -> pauseIconBackgroundColor
                                RecordingState.STOPPED -> sendIconBackgroundColor
                            }
                        ),
                        border = when (recordingState) {
                            RecordingState.START, RecordingState.PAUSED -> {
                                if (style.startIconStrokeWidth > 0.dp) {
                                    BorderStroke(
                                        style.startIconStrokeWidth,
                                        style.startIconStrokeColor ?: colors.strokeColorLight
                                    )
                                } else null
                            }
                            RecordingState.RECORDING -> {
                                if (style.pauseIconStrokeWidth > 0.dp) {
                                    BorderStroke(
                                        style.pauseIconStrokeWidth,
                                        style.pauseIconStrokeColor ?: colors.strokeColorLight
                                    )
                                } else null
                            }
                            RecordingState.STOPPED -> {
                                if (style.sendIconStrokeWidth > 0.dp) {
                                    BorderStroke(
                                        style.sendIconStrokeWidth,
                                        style.sendIconStrokeColor ?: colors.strokeColorLight
                                    )
                                } else null
                            }
                        },
                        shape = CircleShape,
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = when (recordingState) {
                                RecordingState.START, RecordingState.PAUSED -> style.startIconElevation
                                RecordingState.RECORDING -> style.pauseIconElevation
                                RecordingState.STOPPED -> style.sendIconElevation
                            }
                        )
                    ) {
                        IconButton(
                            onClick = {
                                when (recordingState) {
                                    RecordingState.START -> {
                                        if (permissionState.allPermissionsGranted) {
                                            startRecording(
                                                context = context,
                                                onStateChange = { recordingState = it },
                                                onRecordingChange = { isRecording = it },
                                                onStartTimeChange = { startTime = it },
                                                onRecorderChange = { mediaRecorder = it },
                                                onFilePathChange = { recordedFilePath = it },
                                                handler = handler,
                                                timerRunnable = timerRunnable
                                            )
                                        } else {
                                            permissionState.launchPermissionsRequest()
                                        }
                                    }
                                    RecordingState.RECORDING -> {
                                        pauseRecording(
                                            mediaRecorder = mediaRecorder,
                                            handler = handler,
                                            timerRunnable = timerRunnable,
                                            startTime = startTime,
                                            onStateChange = { recordingState = it },
                                            onRecordingChange = { isRecording = it },
                                            onPauseTimeChange = { pauseTime = it }
                                        )
                                    }
                                    RecordingState.PAUSED -> {
                                        resumeRecording(
                                            mediaRecorder = mediaRecorder,
                                            handler = handler,
                                            timerRunnable = timerRunnable,
                                            pauseTime = pauseTime,
                                            onStateChange = { recordingState = it },
                                            onRecordingChange = { isRecording = it },
                                            onStartTimeChange = { startTime = it }
                                        )
                                    }
                                    RecordingState.STOPPED -> {
                                        recordedFilePath?.let { path ->
                                            onSubmit?.invoke(File(path), context)
                                        }
                                        recordedFilePath = null
                                        deleteRecording(
                                            mediaRecorder = mediaRecorder,
                                            handler = handler,
                                            timerRunnable = timerRunnable,
                                            onStateChange = { recordingState = it },
                                            onRecordingChange = { isRecording = it },
                                            onTimeChange = { recordingTime = it },
                                            onRecorderChange = { mediaRecorder = it },
                                            onClose = onClose
                                        )
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                painter = painterResource(
                                    id = when (recordingState) {
                                        RecordingState.START, RecordingState.PAUSED -> R.drawable.cometchat_ic_media_recorder_start
                                        RecordingState.RECORDING -> R.drawable.cometchat_ic_media_recorder_pause
                                        RecordingState.STOPPED -> R.drawable.cometchat_ic_media_recorder_send
                                    }
                                ),
                                contentDescription = when (recordingState) {
                                    RecordingState.START -> "Start Recording"
                                    RecordingState.RECORDING -> "Pause Recording"
                                    RecordingState.PAUSED -> "Resume Recording"
                                    RecordingState.STOPPED -> "Send Recording"
                                },
                                tint = when (recordingState) {
                                    RecordingState.START, RecordingState.PAUSED -> startIconTint
                                    RecordingState.RECORDING -> pauseIconTint
                                    RecordingState.STOPPED -> sendIconTint
                                },
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(20.dp))

                    // Right button (Stop/Restart)
                    if (recordingState == RecordingState.RECORDING || recordingState == RecordingState.PAUSED) {
                        Card(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape),
                            colors = CardDefaults.cardColors(containerColor = stopIconBackgroundColor),
                            border = if (style.stopIconStrokeWidth > 0.dp) {
                                BorderStroke(
                                    style.stopIconStrokeWidth,
                                    style.stopIconStrokeColor ?: colors.strokeColorLight
                                )
                            } else null,
                            shape = CircleShape,
                            elevation = CardDefaults.cardElevation(defaultElevation = style.stopIconElevation)
                        ) {
                            IconButton(
                                onClick = {
                                    stopRecording(
                                        mediaRecorder = mediaRecorder,
                                        handler = handler,
                                        timerRunnable = timerRunnable,
                                        onStateChange = { recordingState = it },
                                        onRecordingChange = { isRecording = it },
                                        onTimeChange = { recordingTime = it },
                                        onRecorderChange = { mediaRecorder = it }
                                    )
                                },
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.cometchat_ic_media_recorder_stop),
                                    contentDescription = "Stop",
                                    tint = stopIconTint,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    } else if (recordingState == RecordingState.STOPPED) {
                        Card(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape),
                            colors = CardDefaults.cardColors(containerColor = restartIconBackgroundColor),
                            border = if (style.restartIconStrokeWidth > 0.dp) {
                                BorderStroke(
                                    style.restartIconStrokeWidth,
                                    style.restartIconStrokeColor ?: colors.strokeColorLight
                                )
                            } else null,
                            shape = CircleShape,
                            elevation = CardDefaults.cardElevation(defaultElevation = style.restartIconElevation)
                        ) {
                            IconButton(
                                onClick = {
                                    recordingState = RecordingState.START
                                    recordingTime = "00:00"
                                    recordedFilePath = null
                                },
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.cometchat_ic_media_recorder_start),
                                    contentDescription = "Restart",
                                    tint = restartIconTint,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
