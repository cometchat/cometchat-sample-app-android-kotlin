package com.cometchat.uikit.compose.presentation.shared.messagebubble.ui

import android.media.MediaMetadataRetriever
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cometchat.chat.models.Attachment
import com.cometchat.chat.models.MediaMessage
import com.cometchat.uikit.compose.R
import com.cometchat.uikit.compose.presentation.imageviewer.ui.CometChatImageViewerActivity
import com.cometchat.uikit.compose.presentation.shared.formatters.CometChatTextFormatter
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatAudiosBubbleStyle
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.utils.AudioBubbleStateManager
import com.cometchat.uikit.core.utils.PlayState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ConcurrentHashMap

private const val TAG = "AudiosBubble"
private const val COLLAPSED_AUDIO_COUNT = 3
private const val POLL_INTERVAL_MS = 100L

/** Remote-URL duration cache so cards can show "00:00/00:32" before the first play. */
private val audioDurationCache = ConcurrentHashMap<String, Long>()

/**
 * Renders every audio attachment of [message] as a rounded player card: play/pause circle, file
 * name, flat seek bar with elapsed/total time, and a trailing download icon. More than
 * [COLLAPSED_AUDIO_COUNT] audios collapse behind a "Show N more" / "Show less" toggle. Picker audio
 * only — recorded voice notes render via [CometChatVoiceNoteBubble] (unchanged waveform UI). Part
 * of the ENG-36737 per-type multi-attachment bubbles.
 *
 * Cards are a translucent overlay on the message bubble (white-tint on the tinted outgoing bubble,
 * subtle dark-tint on incoming), matching [CometChatFilesBubble].
 *
 * @param message The [MediaMessage] whose audio attachments are rendered
 * @param alignment The bubble alignment (LEFT, RIGHT, or CENTER)
 * @param style Style configuration for the players + caption
 * @param caption Optional caption (defaults to the message caption, shown under the cards)
 * @param showDownloadIcon Whether to show the per-card download icon
 * @param onDownloadClick Callback when a card's download icon is tapped (index into the audio list)
 * @param onLongClick Callback when the bubble is long-pressed
 */
@Composable
fun CometChatAudiosBubble(
    message: MediaMessage,
    alignment: UIKitConstants.MessageBubbleAlignment,
    modifier: Modifier = Modifier,
    style: CometChatAudiosBubbleStyle = when (alignment) {
        UIKitConstants.MessageBubbleAlignment.RIGHT -> CometChatAudiosBubbleStyle.outgoing()
        else -> CometChatAudiosBubbleStyle.incoming()
    },
    caption: String? = message.caption,
    textFormatters: List<CometChatTextFormatter> = emptyList(),
    showDownloadIcon: Boolean = true,
    onDownloadClick: ((Int) -> Unit)? = null,
    onLongClick: (() -> Unit)? = null
) {
    // Every attachment stays in the list — a kind-mismatched one (server-sent mixed payload)
    // renders as an inert broken card rather than being dropped.
    val audios = remember(message.id) { resolveAttachments(message) }
    if (audios.isEmpty()) return

    var expanded by remember(message.id) { mutableStateOf(false) }
    val canCollapse = audios.size > COLLAPSED_AUDIO_COUNT
    val visibleCount = if (!canCollapse || expanded) audios.size else COLLAPSED_AUDIO_COUNT
    val hiddenCount = audios.size - visibleCount

    // Translucent overlay like the files bubble (the incoming/outgoing tint lives in the style
    // factories). A single audio sits directly on the bubble (no overlay); the card treatment
    // only separates multiples, regardless of the style.
    val cardColor = if (audios.size == 1) Color.Transparent else style.cardBackgroundColor

    Column(
        modifier = modifier
            .width(MULTI_ATTACHMENT_BUBBLE_WIDTH)
            // Match the media bubbles' inset so audios/images/videos in one batch align identically.
            // 0 bottom while the timestamp row below provides the visual gap; pad it when the row
            // is hidden (non-last batch bubble) so the cards aren't flush with the bubble edge.
            .padding(
                start = 5.dp,
                top = 5.dp,
                end = 5.dp,
                bottom = if (LocalTimestampHidden.current) 4.dp else 0.dp
            ),
        verticalArrangement = Arrangement.spacedBy(style.itemSpacing)
    ) {
        for (index in 0 until visibleCount) {
            val attachment = audios[index]
            if (attachment.isAudio()) {
                AudioAttachmentCard(
                    attachment = attachment,
                    cardColor = cardColor,
                    style = style,
                    showDownloadIcon = showDownloadIcon,
                    onDownloadClick = { onDownloadClick?.invoke(index) },
                    onLongClick = onLongClick
                )
            } else {
                BrokenAttachmentCard(
                    attachment = attachment,
                    cardColor = cardColor,
                    style = style,
                    showDownloadIcon = showDownloadIcon,
                    onDownloadClick = { onDownloadClick?.invoke(index) },
                    onLongClick = onLongClick
                )
            }
        }

        if (canCollapse) {
            AudioExpandToggle(
                expanded = expanded,
                hiddenCount = hiddenCount,
                style = style,
                cardColor = cardColor,
                onToggle = { expanded = !expanded }
            )
        }

        MultiAttachmentCaption(
            caption, style.captionTextColor, style.captionTextStyle,
            isEdited = message.editedAt > 0,
            message = message, alignment = alignment, textFormatters = textFormatters
        )
    }
}

/**
 * One audio attachment player card. Playback goes through [AudioBubbleStateManager] (download to
 * cache on first play, single-playback enforcement, state survives recomposition/recycling), same
 * as the single audio bubble — only the chrome differs: name + flat seek bar instead of the
 * waveform.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AudioAttachmentCard(
    attachment: Attachment,
    cardColor: Color,
    style: CometChatAudiosBubbleStyle,
    showDownloadIcon: Boolean,
    onDownloadClick: () -> Unit,
    onLongClick: (() -> Unit)?
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()

    val audioUrl = attachment.fileUrl ?: ""
    // Keyed by URL so playback state survives recycling and message re-fetches.
    val stateId = remember(audioUrl) { (audioUrl.ifEmpty { attachment.fileName ?: "" }).hashCode() }
    val fileName = attachment.fileName ?: "Audio"

    var isDownloaded by remember(stateId) { mutableStateOf(false) }
    var isDownloading by remember(stateId) { mutableStateOf(false) }
    var isInitializing by remember(stateId) { mutableStateOf(false) }
    var progress by remember(stateId) { mutableFloatStateOf(0f) }
    var durationText by remember(stateId) { mutableStateOf("00:00/00:00") }

    val playbackState = remember(stateId) { AudioBubbleStateManager.getOrCreate(stateId, audioUrl, null) }
    var playState by remember(stateId) { mutableStateOf(playbackState.playState) }

    fun refreshTime() {
        val dur = playbackState.totalDuration.takeIf { it > 0 } ?: audioDurationCache[audioUrl] ?: 0L
        durationText = "${formatDurationMs(playbackState.currentPosition)}/${formatDurationMs(dur)}"
    }

    LaunchedEffect(stateId) {
        val cacheDir = context.externalCacheDir ?: context.cacheDir
        val cachedFile = File(cacheDir, "${stateId}_${fileName}")
        if (cachedFile.exists() && cachedFile.length() > 0) {
            isDownloaded = true
            playbackState.localPath = cachedFile.absolutePath
            playbackState.initFromFile(cachedFile.absolutePath, onError = {
                // Corrupt cache entry — evict it and fall back to the download flow on next tap.
                cachedFile.delete()
                isDownloaded = false
                refreshTime()
            }) { refreshTime() }
        } else if (audioUrl.isNotEmpty() && playbackState.totalDuration <= 0 && !audioDurationCache.containsKey(audioUrl)) {
            // Pre-fetch the duration from the remote URL so the card shows "00:00/00:32" before
            // the first play (same on-the-fly approach as the video tiles; cached per URL).
            val durationMs = withContext(Dispatchers.IO) {
                try {
                    val retriever = MediaMetadataRetriever()
                    try {
                        retriever.setDataSource(audioUrl, HashMap())
                        retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
                    } finally {
                        runCatching { retriever.release() }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Duration preload failed: ${e.message}")
                    0L
                }
            }
            if (durationMs > 0) audioDurationCache[audioUrl] = durationMs
        }
        playState = playbackState.playState
        progress = playbackState.progress
        refreshTime()
    }

    LaunchedEffect(playState) {
        while (playState == PlayState.PLAYING) {
            playbackState.updatePosition()
            progress = playbackState.progress
            refreshTime()
            playState = playbackState.playState
            delay(POLL_INTERVAL_MS)
        }
        if (playState == PlayState.STOPPED) progress = 0f
        refreshTime()
    }

    fun onPlayTap() {
        if (audioUrl.isEmpty() && !isDownloaded) return
        if (!isDownloaded) {
            scope.launch {
                isDownloading = true
                val cacheDir = context.externalCacheDir ?: context.cacheDir
                val targetFile = File(cacheDir, "${stateId}_${fileName}")
                val localPath = downloadAudioFile(audioUrl, targetFile)
                isDownloading = false
                if (localPath != null) {
                    isDownloaded = true
                    playbackState.localPath = localPath
                    isInitializing = true
                    playbackState.initFromFile(localPath, onError = {
                        File(localPath).delete()
                        isInitializing = false
                        isDownloaded = false
                        refreshTime()
                    }) {
                        isInitializing = false
                        playbackState.play()
                        playState = PlayState.PLAYING
                    }
                } else {
                    refreshTime()
                }
            }
        } else {
            when (playbackState.playState) {
                PlayState.PLAYING -> { playbackState.pause(); playState = PlayState.PAUSED }
                else -> { playbackState.play(); playState = PlayState.PLAYING }
            }
        }
    }

    fun onSeek(fraction: Float) {
        if (playState != PlayState.PLAYING && playState != PlayState.PAUSED) return
        playbackState.seekToProgress(fraction)
        progress = fraction
        refreshTime()
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(style.cardCornerRadius))
            .background(cardColor)
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {},
                onLongClick = onLongClick
            )
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 34dp play/pause circle with a 24dp glyph (same as the voice-note bubble's
        // button) — outgoing: white on the tinted bubble; incoming: primary.
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(style.playButtonBackgroundColor)
                .then(if (!isDownloading && !isInitializing) Modifier.clickable(onClick = ::onPlayTap) else Modifier),
            contentAlignment = Alignment.Center
        ) {
            when {
                isDownloading || isInitializing ->
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.5.dp, color = style.playIconTint)
                playState == PlayState.PLAYING ->
                    Icon(painter = painterResource(id = R.drawable.cometchat_ic_pause), contentDescription = "Pause", tint = style.playIconTint, modifier = Modifier.size(24.dp))
                else ->
                    Icon(painter = painterResource(id = R.drawable.cometchat_play_icon), contentDescription = "Play", tint = style.playIconTint, modifier = Modifier.size(24.dp))
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = fileName,
                style = style.titleTextStyle,
                color = style.titleTextColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            FlatSeekBar(
                progress = progress,
                playedColor = style.seekFillColor,
                trackColor = style.seekTrackColor,
                knobColor = style.seekKnobColor,
                knobBorderColor = style.seekKnobBorderColor,
                onSeek = if (isDownloaded) ::onSeek else null
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (isDownloading) "Downloading…" else durationText,
                color = style.durationTextColor,
                style = style.durationTextStyle,
                maxLines = 1
            )
        }

        if (showDownloadIcon && audioUrl.isNotEmpty()) {
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                painter = painterResource(id = R.drawable.cometchat_download_icon),
                contentDescription = "Download audio",
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onDownloadClick() },
                tint = style.downloadIconTint
            )
        }
    }
}

/**
 * Card for a kind-mismatched attachment inside an audio-type message (server-sent mixed payload —
 * the UIKit composer never produces one). File-card-like row with nothing to play: just the
 * unsupported-file glyph and the file name — no seek track, no size/type meta. Tapping the card
 * opens the viewer's "No preview available" page (with its Download button); the trailing
 * download icon stays live too.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BrokenAttachmentCard(
    attachment: Attachment,
    cardColor: Color,
    style: CometChatAudiosBubbleStyle,
    showDownloadIcon: Boolean,
    onDownloadClick: () -> Unit,
    onLongClick: (() -> Unit)?
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(style.cardCornerRadius))
            .background(cardColor)
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    context.startActivity(
                        CometChatImageViewerActivity.createIntent(
                            context = context,
                            imageUrl = attachment.fileUrl.orEmpty(),
                            fileName = attachment.fileName.orEmpty(),
                            mimeType = attachment.fileMimeType.orEmpty()
                        )
                    )
                },
                onLongClick = onLongClick
            )
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // File-card-like row: just the unsupported glyph + name (no track, no meta line).
        Image(
            painter = painterResource(id = R.drawable.cometchat_unsupported_file_icon),
            contentDescription = attachment.fileName ?: "Unsupported attachment",
            modifier = Modifier.size(width = 26.dp, height = 32.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = attachment.fileName ?: "",
            style = style.titleTextStyle,
            color = style.titleTextColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        if (showDownloadIcon && !attachment.fileUrl.isNullOrEmpty()) {
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                painter = painterResource(id = R.drawable.cometchat_download_icon),
                contentDescription = "Download attachment",
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onDownloadClick() },
                tint = style.downloadIconTint
            )
        }
    }
}

/**
 * Flat rounded seek bar with a white knob (composer-tray audio tile look). Tap or drag to seek
 * when [onSeek] is non-null.
 */
@Composable
private fun FlatSeekBar(
    progress: Float,
    playedColor: Color,
    trackColor: Color,
    knobColor: Color,
    knobBorderColor: Color,
    onSeek: ((Float) -> Unit)?
) {
    val fraction = progress.coerceIn(0f, 1f)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(14.dp)
            .then(
                if (onSeek != null) {
                    Modifier
                        .pointerInput(onSeek) {
                            detectTapGestures { offset -> onSeek((offset.x / size.width).coerceIn(0f, 1f)) }
                        }
                        .pointerInput(onSeek) {
                            detectHorizontalDragGestures { change, _ ->
                                onSeek((change.position.x / size.width).coerceIn(0f, 1f))
                            }
                        }
                } else Modifier
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .align(Alignment.CenterStart)
                .clip(RoundedCornerShape(3.dp))
                .background(trackColor)
        )
        if (fraction > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .height(6.dp)
                    .align(Alignment.CenterStart)
                    .clip(RoundedCornerShape(3.dp))
                    .background(playedColor)
            )
        }
        Box(
            modifier = Modifier
                .size(14.dp)
                .align(BiasAlignment(horizontalBias = fraction * 2f - 1f, verticalBias = 0f))
                .clip(CircleShape)
                .background(knobColor)
                .border(1.dp, knobBorderColor, CircleShape)
        )
    }
}

@Composable
private fun AudioExpandToggle(
    expanded: Boolean,
    hiddenCount: Int,
    style: CometChatAudiosBubbleStyle,
    cardColor: Color,
    onToggle: () -> Unit
) {
    val label = if (expanded) stringResource(R.string.cometchat_show_less)
    else stringResource(R.string.cometchat_show_n_more, hiddenCount)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(style.cardCornerRadius))
            .background(cardColor)
            .clickable(onClick = onToggle)
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.cometchat_ic_arrow_down),
            contentDescription = null,
            tint = style.toggleTextColor,
            modifier = Modifier
                .size(18.dp)
                .rotate(if (expanded) 180f else 0f)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = style.toggleTextStyle,
            color = style.toggleTextColor
        )
    }
}

private suspend fun downloadAudioFile(url: String, targetFile: File): String? = withContext(Dispatchers.IO) {
    // Stream into a temp file and rename on completion — if the process dies mid-download, no
    // truncated file survives to pass the cache check and feed MediaPlayer a corrupt source.
    val tempFile = File(targetFile.parentFile, "${targetFile.name}.part")
    try {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.connectTimeout = 15_000
        connection.readTimeout = 15_000
        connection.connect()
        if (connection.responseCode !in 200..299) {
            connection.disconnect()
            return@withContext null
        }
        connection.inputStream.use { input ->
            tempFile.outputStream().use { output -> input.copyTo(output) }
        }
        connection.disconnect()
        if (tempFile.length() > 0 && tempFile.renameTo(targetFile)) targetFile.absolutePath else { tempFile.delete(); null }
    } catch (e: Exception) {
        Log.e(TAG, "Download failed: ${e.message}")
        tempFile.delete()
        null
    }
}
