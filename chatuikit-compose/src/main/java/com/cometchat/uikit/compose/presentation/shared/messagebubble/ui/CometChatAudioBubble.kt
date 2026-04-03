package com.cometchat.uikit.compose.presentation.shared.messagebubble.ui

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cometchat.chat.models.MediaMessage
import com.cometchat.uikit.compose.R
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatAudioBubbleStyle
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.utils.AudioBubbleStateManager
import com.cometchat.uikit.core.utils.PlayState
import com.cometchat.uikit.core.utils.WaveformUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

private const val TAG = "AudioBubble"
private const val BAR_COUNT = 28
private const val POLL_INTERVAL_MS = 50L

@Composable
fun CometChatAudioBubble(
    message: MediaMessage,
    alignment: UIKitConstants.MessageBubbleAlignment,
    modifier: Modifier = Modifier,
    style: CometChatAudioBubbleStyle = when (alignment) {
        UIKitConstants.MessageBubbleAlignment.RIGHT -> CometChatAudioBubbleStyle.outgoing()
        else -> CometChatAudioBubbleStyle.incoming()
    },
    onLongClick: (() -> Unit)? = null
) {
    val audioUrl = message.attachment?.fileUrl ?: ""
    val messageId = message.id.toInt()
    val fileName = message.attachment?.fileName ?: "audio_${messageId}.m4a"
    CometChatAudioBubbleContent(audioUrl, messageId, fileName, style, alignment, onLongClick, modifier)
}

@Composable
fun CometChatAudioBubble(
    audioUrl: String, fileSize: Int,
    alignment: UIKitConstants.MessageBubbleAlignment,
    modifier: Modifier = Modifier,
    style: CometChatAudioBubbleStyle = when (alignment) {
        UIKitConstants.MessageBubbleAlignment.RIGHT -> CometChatAudioBubbleStyle.outgoing()
        else -> CometChatAudioBubbleStyle.incoming()
    },
    onLongClick: (() -> Unit)? = null
) {
    CometChatAudioBubbleContent(audioUrl, audioUrl.hashCode(), "audio_${audioUrl.hashCode()}.m4a", style, alignment, onLongClick, modifier)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CometChatAudioBubbleContent(
    audioUrl: String, messageId: Int, fileName: String,
    style: CometChatAudioBubbleStyle,
    alignment: UIKitConstants.MessageBubbleAlignment,
    onLongClick: (() -> Unit)?, modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val playedColor = style.playedBarColor
    val unplayedColor = style.unplayedBarColor
    val durationColor = style.durationTextColor

    var waveformData by remember { mutableStateOf(WaveformUtils.generatePlaceholder(BAR_COUNT)) }
    var isDownloaded by remember { mutableStateOf(false) }
    var isDownloading by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableFloatStateOf(0f) }
    var progress by remember { mutableFloatStateOf(0f) }
    var durationText by remember { mutableStateOf("00:00 / --:--") }

    val playbackState = remember(messageId) { AudioBubbleStateManager.getOrCreate(messageId, audioUrl, null) }
    var playState by remember { mutableStateOf(playbackState.playState) }
    var isInitializing by remember { mutableStateOf(playbackState.isInitializing) }

    LaunchedEffect(messageId) {
        val cacheDir = context.externalCacheDir ?: context.cacheDir
        val cachedFile = File(cacheDir, "${messageId}_${fileName}")
        if (cachedFile.exists() && cachedFile.length() > 0) {
            isDownloaded = true
            playbackState.localPath = cachedFile.absolutePath
            waveformData = WaveformUtils.generateDeterministicWaveform(cachedFile.absolutePath, BAR_COUNT)
            playbackState.initFromFile(cachedFile.absolutePath) {
                durationText = "00:00 / ${formatDurationMs(playbackState.totalDuration)}"
            }
        } else if (audioUrl.isNotEmpty()) {
            waveformData = WaveformUtils.generateDeterministicWaveform(audioUrl, BAR_COUNT)
        }
        playState = playbackState.playState
        if (playbackState.totalDuration > 0) {
            durationText = "${formatDurationMs(playbackState.currentPosition)} / ${formatDurationMs(playbackState.totalDuration)}"
            progress = playbackState.progress
        }
    }

    LaunchedEffect(playState) {
        while (playState == PlayState.PLAYING) {
            playbackState.updatePosition()
            progress = playbackState.progress
            val pos = playbackState.currentPosition; val dur = playbackState.totalDuration
            durationText = if (dur > 0) "${formatDurationMs(pos)} / ${formatDurationMs(dur)}" else "00:00 / --:--"
            playState = playbackState.playState
            delay(POLL_INTERVAL_MS)
        }
        if (playState == PlayState.STOPPED || playState == PlayState.PAUSED) {
            val dur = playbackState.totalDuration; val pos = playbackState.currentPosition
            if (dur > 0) { durationText = "${formatDurationMs(pos)} / ${formatDurationMs(dur)}"; progress = playbackState.progress }
        }
    }

    DisposableEffect(messageId) { onDispose { } }

    fun onPlayTap() {
        if (!isDownloaded) {
            scope.launch {
                isDownloading = true; downloadProgress = 0f
                val cacheDir = context.externalCacheDir ?: context.cacheDir
                val targetFile = File(cacheDir, "${messageId}_${fileName}")
                val localPath = downloadFile(audioUrl, targetFile) { downloadProgress = it }
                isDownloading = false
                if (localPath != null) {
                    isDownloaded = true; playbackState.localPath = localPath
                    waveformData = WaveformUtils.generateDeterministicWaveform(localPath, BAR_COUNT)
                    isInitializing = true
                    playbackState.initFromFile(localPath) {
                        isInitializing = false
                        durationText = "00:00 / ${formatDurationMs(playbackState.totalDuration)}"
                        playbackState.play(); playState = PlayState.PLAYING
                    }
                }
            }
        } else {
            when (playbackState.playState) {
                PlayState.PLAYING -> { playbackState.pause(); playState = PlayState.PAUSED }
                else -> { playbackState.play(); playState = PlayState.PLAYING }
            }
        }
    }

    fun onSeek(seekProgress: Float) {
        playbackState.seekToProgress(seekProgress); progress = seekProgress
        val dur = playbackState.totalDuration; val pos = (dur * seekProgress).toLong()
        durationText = "${formatDurationMs(pos)} / ${formatDurationMs(dur)}"
    }

    Row(
        modifier = modifier.width(240.dp)
            .combinedClickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = {}, onLongClick = onLongClick)
            .padding(start = 8.dp, end = 8.dp, top = 10.dp, bottom = 4.dp)
            .semantics { contentDescription = "Audio message" },
        verticalAlignment = Alignment.CenterVertically
    ) {
        AudioBubblePlayPauseButton(playState, isDownloading, downloadProgress, isInitializing, ::onPlayTap, style.playIconTint, style.buttonBackgroundColor)
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            SeekableWaveform(waveformData, progress, playedColor, unplayedColor, if (isDownloaded) ::onSeek else null, isDownloaded, height = 32.dp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = durationText, color = durationColor, style = style.durationTextStyle, fontSize = 12.sp, maxLines = 1)
        }
    }
}

@Composable
private fun AudioBubblePlayPauseButton(
    playState: PlayState, isDownloading: Boolean, downloadProgress: Float, isInitializing: Boolean,
    onClick: () -> Unit, iconColor: androidx.compose.ui.graphics.Color, backgroundColor: androidx.compose.ui.graphics.Color
) {
    Box(
        modifier = Modifier.size(44.dp).clip(CircleShape).background(backgroundColor)
            .then(if (!isDownloading && !isInitializing) Modifier.clickable(onClick = onClick) else Modifier)
            .semantics { contentDescription = when { isDownloading -> "Downloading"; isInitializing -> "Loading"; playState == PlayState.PLAYING -> "Pause"; else -> "Play" } },
        contentAlignment = Alignment.Center
    ) {
        when {
            isDownloading || isInitializing -> {
                if (isDownloading && downloadProgress > 0f) CircularProgressIndicator(progress = { downloadProgress }, modifier = Modifier.size(44.dp), strokeWidth = 3.dp, color = iconColor)
                else CircularProgressIndicator(modifier = Modifier.size(30.dp), strokeWidth = 3.dp, color = iconColor)
            }
            playState == PlayState.PLAYING -> Icon(painter = painterResource(id = R.drawable.cometchat_ic_pause), contentDescription = null, tint = iconColor, modifier = Modifier.size(28.dp))
            else -> Icon(painter = painterResource(id = R.drawable.cometchat_play_icon), contentDescription = null, tint = iconColor, modifier = Modifier.size(28.dp))
        }
    }
}

private suspend fun downloadFile(url: String, targetFile: File, onProgress: (Float) -> Unit): String? = withContext(Dispatchers.IO) {
    try {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.connectTimeout = 15_000; connection.readTimeout = 15_000; connection.connect()
        if (connection.responseCode !in 200..299) { connection.disconnect(); return@withContext null }
        val totalBytes = connection.contentLength.toLong(); var downloaded = 0L
        connection.inputStream.use { input -> targetFile.outputStream().use { output ->
            val buffer = ByteArray(8192); var bytesRead: Int
            while (input.read(buffer).also { bytesRead = it } != -1) {
                output.write(buffer, 0, bytesRead); downloaded += bytesRead
                if (totalBytes > 0) withContext(Dispatchers.Main) { onProgress((downloaded.toFloat() / totalBytes).coerceIn(0f, 1f)) }
            }
        }}
        connection.disconnect()
        if (targetFile.exists() && targetFile.length() > 0) targetFile.absolutePath else { targetFile.delete(); null }
    } catch (e: Exception) { Log.e(TAG, "Download failed: ${e.message}"); targetFile.delete(); null }
}

fun formatDurationMs(ms: Long): String {
    if (ms <= 0) return "00:00"
    val totalSeconds = ms / 1000; val minutes = (totalSeconds / 60).toInt(); val seconds = (totalSeconds % 60).toInt()
    return String.format(Locale.US, "%02d:%02d", minutes, seconds)
}
