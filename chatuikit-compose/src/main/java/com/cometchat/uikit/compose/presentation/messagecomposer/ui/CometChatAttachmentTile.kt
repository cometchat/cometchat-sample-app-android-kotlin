package com.cometchat.uikit.compose.presentation.messagecomposer.ui

import android.media.MediaPlayer
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.decode.VideoFrameDecoder
import coil.request.ImageRequest
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.uikit.compose.R
import com.cometchat.uikit.compose.presentation.messagecomposer.style.CometChatAttachmentTileStyle
import com.cometchat.uikit.compose.presentation.shared.messagebubble.utils.formatFileSize
import com.cometchat.uikit.compose.presentation.shared.messagebubble.utils.getFileExtension
import com.cometchat.uikit.compose.presentation.shared.messagebubble.utils.getFileType
import com.cometchat.uikit.compose.presentation.shared.messagebubble.utils.getFileTypeIcon
import com.cometchat.uikit.core.models.AttachmentUploadStatus
import com.cometchat.uikit.core.models.AttachmentUploadTile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * The shape a tile takes in the tray, decided by the tile's [AttachmentUploadTile.category] — the
 * **picker** the file came through, not its MIME type. Gallery/camera picks (`image`/`video`)
 * render as a square media tile; audio-picker picks render as an audio card (play glyph + name +
 * duration); anything chosen through the **file** picker renders as a document card even when it
 * is an image, video or audio file. Recorder voice notes are never staged in the tray (they are
 * sent standalone).
 */
internal enum class AttachmentTileKind { MEDIA, AUDIO, FILE }

internal fun AttachmentUploadTile.tileKind(): AttachmentTileKind = when (category) {
    CometChatConstants.MESSAGE_TYPE_IMAGE, CometChatConstants.MESSAGE_TYPE_VIDEO ->
        AttachmentTileKind.MEDIA
    CometChatConstants.MESSAGE_TYPE_AUDIO -> AttachmentTileKind.AUDIO
    else -> AttachmentTileKind.FILE
}

private fun AttachmentUploadTile.isVideo(): Boolean =
    category == CometChatConstants.MESSAGE_TYPE_VIDEO

/**
 * Renders a single staged attachment in the composer tray, keyed by its `fileId`.
 *
 * A **corner badge** straddles the top-end of every tile: a ✕ that **cancels** while
 * [AttachmentUploadStatus.UPLOADING] and **removes** (remove + cancel) otherwise.
 *
 * Upload state follows the design system's mobile card states:
 * - **uploading** — the tile's leading visual (thumbnail / file icon / play button) dims under a
 *   dark overlay with a white progress ring on top;
 * - **failed** ([AttachmentUploadStatus.FAILED], retryable) — the leading visual dims under a red
 *   round **retry** badge, the card border turns red and the meta line reads "Tap to retry" in
 *   the error color; tapping the tile retries;
 * - **rejected** ([AttachmentUploadStatus.REJECTED], not retryable) — same treatment with an
 *   **error** badge and an "Upload failed" meta line;
 * - **done** — media tiles show the thumbnail (and a play badge for video); tapping opens the
 *   preview.
 *
 * This composable is stateless: it reflects the tile and emits intents through the callbacks.
 *
 * @param tile The staged item to render.
 * @param style Visual styling; defaults to theme-token-backed [CometChatAttachmentTileStyle.default].
 * @param onCancel Invoked when the badge is tapped while uploading (cancel the in-flight upload).
 * @param onRemove Invoked when the ✕ is tapped in any non-uploading state (remove + cancel).
 * @param onRetry Invoked when a failed tile is tapped (retry the upload).
 * @param onClick Invoked when a successfully-uploaded tile is tapped (open the preview).
 * @param onRejected Invoked when a rejected tile is tapped (surface the SDK rejection reason, e.g.
 *   the size-limit message) — rejected tiles are not retryable, so tapping only explains the error.
 */
@Composable
fun CometChatAttachmentTile(
    tile: AttachmentUploadTile,
    modifier: Modifier = Modifier,
    style: CometChatAttachmentTileStyle = CometChatAttachmentTileStyle.default(),
    onCancel: () -> Unit = {},
    onRemove: () -> Unit = {},
    onRetry: () -> Unit = {},
    onClick: () -> Unit = {},
    onRejected: () -> Unit = {}
) {
    if (tile.status == AttachmentUploadStatus.CANCELLED) return

    val kind = tile.tileKind()
    val description = stringResource(
        R.string.cometchat_attachment_tile_description,
        tile.name,
        statusLabel(tile.status)
    )
    // Inset the body so the corner badge can straddle the top-end corner within the tile's bounds.
    val overhang = style.cornerBadgeOverhang

    Box(modifier = modifier.clearAndSetSemantics { contentDescription = description }) {
        Box(modifier = Modifier.padding(top = overhang, end = overhang)) {
            when (kind) {
                AttachmentTileKind.MEDIA -> MediaTileBody(tile, style, onRetry, onClick, onRejected)
                AttachmentTileKind.AUDIO -> AudioTileBody(tile, style, onRetry, onClick, onRejected)
                AttachmentTileKind.FILE -> FileTileBody(tile, style, onRetry, onClick, onRejected)
            }
        }

        CornerBadge(
            tile = tile,
            style = style,
            onCancel = onCancel,
            onRemove = onRemove,
            modifier = Modifier.align(Alignment.TopEnd)
        )
    }
}

/* ------------------------------------------------------------------------------------------------
 * Tile bodies
 * ---------------------------------------------------------------------------------------------- */

@Composable
private fun MediaTileBody(
    tile: AttachmentUploadTile,
    style: CometChatAttachmentTileStyle,
    onRetry: () -> Unit,
    onClick: () -> Unit,
    onRejected: () -> Unit
) {
    val uploaded = tile.status == AttachmentUploadStatus.DONE
    val failed = tile.status == AttachmentUploadStatus.FAILED
    val rejected = tile.status == AttachmentUploadStatus.REJECTED
    val isError = failed || rejected
    val uploading = tile.status == AttachmentUploadStatus.UPLOADING
    val shape = RoundedCornerShape(style.mediaCornerRadius)

    Box(
        modifier = Modifier
            .size(style.mediaTileSize)
            .clip(shape)
            .background(style.placeholderColor)
            .border(
                width = style.mediaStrokeWidth,
                color = if (isError) style.cardErrorStrokeColor else style.mediaStrokeColor,
                shape = shape
            )
            .clickable(enabled = uploaded || failed || rejected) {
                when {
                    failed -> onRetry()
                    rejected -> onRejected()
                    else -> onClick()
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // The thumbnail stays visible in every state — uploading / failed blur and dim it rather
        // than hiding it. (blur is a no-op below API 31; the dim still communicates the state.)
        // Videos need Coil's frame decoder — without it the request fails and the tile would
        // stay a blank placeholder.
        val model = tile.localUri ?: tile.attachment?.fileUrl
        if (model != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(model)
                    .apply { if (tile.isVideo()) decoderFactory(VideoFrameDecoder.Factory()) }
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .then(if (uploading || isError) Modifier.blur(3.dp) else Modifier)
            )
        }

        // Video play badge + duration chip, only once the upload has succeeded (the blurred
        // uploading / failed states hide both, per the design).
        if (uploaded && tile.isVideo()) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(style.videoPlayBadgeColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.cometchat_play_icon),
                    contentDescription = stringResource(R.string.cometchat_attachment_play),
                    tint = style.videoPlayIconTint,
                    modifier = Modifier.size(15.dp)
                )
            }
            tile.durationMillis?.let { duration ->
                Text(
                    text = formatVideoDuration(duration),
                    style = style.durationTextStyle,
                    color = style.durationTextColor,
                    maxLines = 1,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(4.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(style.durationLabelColor)
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                )
            }
        }

        if (uploading || isError) {
            // On a retryable failure the whole dimmed tile is a retry target; on a rejection it is
            // a "show the reason" target (in addition to the tile's own click handler), mirroring
            // the Views adapter's card-level behaviour.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(style.mediaDimColor)
                    .then(
                        when {
                            failed -> Modifier.clickable(onClick = onRetry)
                            rejected -> Modifier.clickable(onClick = onRejected)
                            else -> Modifier
                        }
                    )
            )
            if (uploading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 3.dp,
                    color = style.loadingIndicatorColor
                )
            } else {
                ErrorBadge(tile, style, onRetry, onRejected)
            }
        }
    }
}

@Composable
private fun FileTileBody(
    tile: AttachmentUploadTile,
    style: CometChatAttachmentTileStyle,
    onRetry: () -> Unit,
    onClick: () -> Unit,
    onRejected: () -> Unit
) {
    val uploading = tile.status == AttachmentUploadStatus.UPLOADING
    val failed = tile.status == AttachmentUploadStatus.FAILED
    val rejected = tile.status == AttachmentUploadStatus.REJECTED
    val isError = failed || rejected
    val done = tile.status == AttachmentUploadStatus.DONE
    val shape = RoundedCornerShape(style.cardCornerRadius)

    Row(
        modifier = Modifier
            .width(style.cardWidth)
            .clip(shape)
            .background(style.cardBackgroundColor)
            .border(
                width = style.cardStrokeWidth,
                color = if (isError) style.cardErrorStrokeColor else style.cardStrokeColor,
                shape = shape
            )
            .clickable(enabled = done || failed || rejected) {
                when {
                    failed -> onRetry()
                    rejected -> onRejected()
                    else -> onClick()
                }
            }
            .padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // The leading slot is a rounded container holding the file-type glyph; while uploading or
        // failed, the glyph dims under a dark overlay carrying the state (ring / red badge).
        val iconShape = RoundedCornerShape(style.fileIconCornerRadius)
        Box(
            modifier = Modifier
                .size(style.fileIconSize)
                .clip(iconShape)
                .background(style.fileIconBackgroundColor)
                .border(
                    width = style.fileIconStrokeWidth,
                    color = style.fileIconStrokeColor,
                    shape = iconShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(getFileTypeIcon(getFileType(tile.mimeType, tile.name))),
                contentDescription = null,
                modifier = Modifier.size(style.fileTypeIconSize)
            )
            if (uploading || isError) SlotStateOverlay(tile, style, iconShape, onRetry, onRejected)
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = tile.name,
                style = style.fileNameTextStyle,
                color = style.fileNameTextColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = when {
                    failed -> stringResource(R.string.cometchat_attachment_tap_to_retry)
                    rejected -> stringResource(R.string.cometchat_attachment_upload_failed)
                    else -> fileMetaLabel(tile)
                },
                style = style.fileMetaTextStyle,
                color = if (isError) style.errorTextColor else style.fileMetaTextColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Audio card: round play/pause button + file name + seek bar + `elapsed/total` time, in the same
 * card chrome as a document. Tapping the play button plays/pauses the staged file inline (tapping
 * the seek bar seeks); while uploading or failed, the play button dims under the same state
 * overlay as a document card's icon, the seek bar hides and the time line swaps to the state text.
 */
@Composable
private fun AudioTileBody(
    tile: AttachmentUploadTile,
    style: CometChatAttachmentTileStyle,
    onRetry: () -> Unit,
    onClick: () -> Unit,
    onRejected: () -> Unit
) {
    val uploading = tile.status == AttachmentUploadStatus.UPLOADING
    val failed = tile.status == AttachmentUploadStatus.FAILED
    val rejected = tile.status == AttachmentUploadStatus.REJECTED
    val isError = failed || rejected
    val done = tile.status == AttachmentUploadStatus.DONE
    val shape = RoundedCornerShape(style.cardCornerRadius)

    // Inline playback of the staged file. The player is created lazily on the first play tap and
    // released when the tile leaves composition.
    val context = LocalContext.current
    val source = tile.localUri ?: tile.attachment?.fileUrl
    var player by remember(tile.fileId) { mutableStateOf<MediaPlayer?>(null) }
    var isPlaying by remember(tile.fileId) { mutableStateOf(false) }
    var positionMillis by remember(tile.fileId) { mutableLongStateOf(0L) }
    var totalMillis by remember(tile.fileId) { mutableLongStateOf(tile.durationMillis ?: 0L) }
    DisposableEffect(tile.fileId) {
        onDispose {
            runCatching { player?.release() }
            player = null
        }
    }
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            player?.let { positionMillis = it.currentPosition.toLong() }
            delay(200)
        }
    }

    val scope = rememberCoroutineScope()
    var isPreparing by remember(tile.fileId) { mutableStateOf(false) }

    fun togglePlayback() {
        val src = source ?: return
        val current = player
        if (current != null) {
            if (current.isPlaying) {
                runCatching { current.pause() }
                isPlaying = false
            } else {
                runCatching { current.start() }
                isPlaying = true
            }
            return
        }
        if (isPreparing) return
        isPreparing = true
        scope.launch {
            // prepare() is blocking I/O — keep it off the main thread.
            val prepared = withContext(Dispatchers.IO) {
                val mp = runCatching {
                    MediaPlayer().apply {
                        // localUri is a content:// uri string in Compose staging but may be a bare
                        // file path (e.g. tiles staged elsewhere) — Uri.parse would drop the
                        // scheme for those.
                        if (src.startsWith("/")) setDataSource(src)
                        else setDataSource(context, Uri.parse(src))
                        prepare()
                    }
                }.getOrNull()
                // If the tile left composition while preparing, the continuation below is skipped
                // (the scope is cancelled) and the player would never be assigned or released.
                // Release it here so a prepared-but-orphaned MediaPlayer can't leak.
                if (mp != null && !isActive) {
                    runCatching { mp.release() }
                    null
                } else {
                    mp
                }
            }
            isPreparing = false
            if (prepared == null) return@launch
            prepared.setOnCompletionListener {
                isPlaying = false
                positionMillis = 0L
                runCatching { it.seekTo(0) }
            }
            player = prepared
            totalMillis = runCatching { prepared.duration.toLong() }.getOrDefault(0L)
                .takeIf { it > 0 } ?: (tile.durationMillis ?: 0L)
            runCatching { prepared.start() }
            isPlaying = true
        }
    }

    Row(
        modifier = Modifier
            .width(style.audioCardWidth)
            .height(style.audioCardHeight)
            .clip(shape)
            .background(style.cardBackgroundColor)
            .border(
                width = style.cardStrokeWidth,
                color = if (isError) style.cardErrorStrokeColor else style.cardStrokeColor,
                shape = shape
            )
            .clickable(enabled = failed || rejected) { if (failed) onRetry() else onRejected() }
            .padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(style.audioPlayButtonSize)
                .clip(CircleShape)
                .background(style.audioPlayButtonColor)
                .clickable(enabled = done && source != null) { togglePlayback() },
            contentAlignment = Alignment.Center
        ) {
            // Same glyphs and proportions as CometChatAudioBubble's play button (28dp icon in
            // a 44dp circle -> 26dp in this 40dp button; the smaller button keeps the card at the
            // file card's footprint).
            Icon(
                painter = painterResource(
                    if (isPlaying) R.drawable.cometchat_ic_pause else R.drawable.cometchat_play_icon
                ),
                contentDescription = stringResource(
                    if (isPlaying) R.string.cometchat_attachment_pause
                    else R.string.cometchat_attachment_play
                ),
                tint = style.audioPlayIconTint,
                modifier = Modifier.size(26.dp)
            )
            if (uploading || isError) SlotStateOverlay(tile, style, CircleShape, onRetry, onRejected)
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = tile.name,
                style = style.fileNameTextStyle,
                color = style.fileNameTextColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!isError) {
                AudioSeekBar(
                    fraction = if (totalMillis > 0) {
                        (positionMillis.toFloat() / totalMillis).coerceIn(0f, 1f)
                    } else 0f,
                    style = style,
                    enabled = done && player != null,
                    onSeek = { f ->
                        player?.let {
                            val target = (f * totalMillis).toLong().coerceIn(0L, totalMillis)
                            runCatching { it.seekTo(target.toInt()) }
                            positionMillis = target
                        }
                    }
                )
            }
            Text(
                text = when {
                    failed -> stringResource(R.string.cometchat_attachment_tap_to_retry)
                    rejected -> stringResource(R.string.cometchat_attachment_upload_failed)
                    else -> formatDuration(positionMillis) + "/" + formatDuration(totalMillis)
                },
                style = style.audioTimeTextStyle,
                color = if (isError) style.errorTextColor else style.audioTimeTextColor,
                maxLines = 1
            )
        }
    }
}

/**
 * Flat audio seek bar: a rounded track with the played portion filled and a round knob at the
 * playhead. Tapping seeks when [enabled].
 */
@Composable
private fun AudioSeekBar(
    fraction: Float,
    style: CometChatAttachmentTileStyle,
    enabled: Boolean,
    onSeek: (Float) -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(10.dp)
            .pointerInput(enabled) {
                if (enabled) {
                    detectTapGestures { offset ->
                        onSeek((offset.x / size.width).coerceIn(0f, 1f))
                    }
                }
            },
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(style.audioSeekTrackColor)
        )
        if (fraction > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(style.audioSeekFillColor)
            )
        }
        val knobSize = 10.dp
        val knobOffset = (maxWidth - knobSize) * fraction
        Box(
            modifier = Modifier
                .offset(x = knobOffset)
                .size(knobSize)
                .clip(CircleShape)
                .background(style.audioSeekKnobColor)
                .border(1.dp, style.audioSeekKnobStrokeColor, CircleShape)
        )
    }
}

/* ------------------------------------------------------------------------------------------------
 * Shared state visuals
 * ---------------------------------------------------------------------------------------------- */

/**
 * The upload-state overlay for a card's leading slot: a dark scrim over the slot's visual with a
 * white progress ring while uploading, or the red retry / error badge on failure.
 */
@Composable
private fun BoxScope.SlotStateOverlay(
    tile: AttachmentUploadTile,
    style: CometChatAttachmentTileStyle,
    shape: Shape,
    onRetry: () -> Unit = {},
    onRejected: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .matchParentSize()
            .clip(shape)
            .background(style.stateOverlayColor),
        contentAlignment = Alignment.Center
    ) {
        if (tile.status == AttachmentUploadStatus.UPLOADING) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.5.dp,
                color = style.loadingIndicatorColor
            )
        } else {
            ErrorBadge(tile, style, onRetry, onRejected)
        }
    }
}

/**
 * The round red badge carrying the retry (failed) / error (rejected) glyph. On a retryable
 * failure the badge is a retry target; on a rejection it is a "show the reason" target — the
 * center the user taps in either case.
 */
@Composable
private fun ErrorBadge(
    tile: AttachmentUploadTile,
    style: CometChatAttachmentTileStyle,
    onRetry: () -> Unit = {},
    onRejected: () -> Unit = {}
) {
    val failed = tile.status == AttachmentUploadStatus.FAILED
    Box(
        modifier = Modifier
            .size(16.dp)
            .clip(CircleShape)
            .background(style.errorBadgeColor)
            .clickable(onClick = if (failed) onRetry else onRejected),
        contentAlignment = Alignment.Center
    ) {
        // The glyph drawables carry the badge's inner margin in their viewport, so they render at
        // the badge's full size rather than an inset icon box.
        Icon(
            painter = painterResource(
                if (failed) R.drawable.cometchat_ic_upload_retry else R.drawable.cometchat_ic_upload_error
            ),
            contentDescription = stringResource(
                if (failed) R.string.cometchat_attachment_retry
                else R.string.cometchat_attachment_upload_failed
            ),
            tint = style.errorBadgeIconTint,
            modifier = Modifier.matchParentSize()
        )
    }
}

/* ------------------------------------------------------------------------------------------------
 * Corner badge (✕)
 * ---------------------------------------------------------------------------------------------- */

/**
 * The ✕ at the top-end of every tile (all kinds). The upload state is shown in the body — the
 * leading slot's overlay — so the corner is always the dismissal control: it **cancels** the
 * in-flight upload while [AttachmentUploadStatus.UPLOADING] and **removes** (remove + cancel)
 * otherwise.
 */
@Composable
private fun CornerBadge(
    tile: AttachmentUploadTile,
    style: CometChatAttachmentTileStyle,
    onCancel: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uploading = tile.status == AttachmentUploadStatus.UPLOADING
    val onClick = if (uploading) onCancel else onRemove
    val contentDescription = stringResource(
        if (uploading) R.string.cometchat_attachment_cancel else R.string.cometchat_attachment_remove
    )

    Box(
        modifier = modifier
            .size(style.cornerBadgeSize)
            .clip(CircleShape)
            .background(style.cornerBadgeBackgroundColor)
            .border(style.cornerBadgeBorderWidth, style.cornerBadgeBorderColor, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.cometchat_ic_close),
            contentDescription = contentDescription,
            tint = style.cornerBadgeIconTint,
            modifier = Modifier.size(style.cornerBadgeSize / 2)
        )
    }
}

/* ------------------------------------------------------------------------------------------------
 * Helpers
 * ---------------------------------------------------------------------------------------------- */

@Composable
private fun statusLabel(status: AttachmentUploadStatus): String = when (status) {
    AttachmentUploadStatus.UPLOADING -> stringResource(R.string.cometchat_attachment_uploading)
    AttachmentUploadStatus.FAILED -> stringResource(R.string.cometchat_attachment_tap_to_retry)
    AttachmentUploadStatus.REJECTED -> stringResource(R.string.cometchat_attachment_upload_failed)
    AttachmentUploadStatus.DONE, AttachmentUploadStatus.CANCELLED -> ""
}

/** Meta line under a file name: `EXT · size` (e.g. "PDF · 2.4 MB"), or just the size when the
 *  file has no extension. */
private fun fileMetaLabel(tile: AttachmentUploadTile): String {
    val ext = getFileExtension(tile.name).uppercase()
    val size = formatFileSize(tile.size)
    return if (ext.isNotEmpty()) "$ext · $size" else size
}

/** Formats a millisecond duration as `mm:ss` (e.g. 32_000 → "00:32") — audio card time line. */
private fun formatDuration(durationMillis: Long): String {
    val totalSeconds = (durationMillis / 1000L).coerceAtLeast(0L)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}

/** Formats a millisecond duration as `m:ss` (e.g. 12_000 → "0:12") — video tile duration chip. */
private fun formatVideoDuration(durationMillis: Long): String {
    val totalSeconds = (durationMillis / 1000L).coerceAtLeast(0L)
    return "%d:%02d".format(totalSeconds / 60, totalSeconds % 60)
}
