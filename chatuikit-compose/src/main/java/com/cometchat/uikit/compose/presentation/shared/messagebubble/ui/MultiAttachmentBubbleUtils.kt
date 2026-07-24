package com.cometchat.uikit.compose.presentation.shared.messagebubble.ui

import android.media.MediaMetadataRetriever
import android.util.LruCache
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.cometchat.chat.models.Attachment
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.MediaMessage
import androidx.compose.ui.text.TextStyle
import com.cometchat.uikit.compose.R
import com.cometchat.uikit.compose.presentation.shared.formatters.CometChatTextFormatter
import com.cometchat.uikit.compose.presentation.shared.formatters.FormatterUtils
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatImagesBubbleStyle
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatVideosBubbleStyle
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.uikit.compose.theme.cometchatFontBold
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.formatter.MarkdownRenderer
import com.cometchat.uikit.core.formatter.RichTextFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Shared building blocks for the ENG-36737 per-type multi-attachment bubbles
 * (CometChatImagesBubble / VideosBubble / AudiosBubble / VoiceNoteBubble / FilesBubble).
 *
 * The bubble is picked by the message `type`; attachments are categorized by
 * [Attachment.getFileMimeType]. A multi-attachment send is split upstream into one message per
 * type, all sharing a `batchId` (see the composer ViewModel); the message list groups them by
 * list-neighbour adjacency on that `batchId` (iOS-compatible contract). A server-sent message can
 * still mix attachment kinds under one `type` — every attachment stays in the bubble, with
 * kind-mismatched ones rendered as broken tiles/cards (unsupported-file glyph) instead of being
 * dropped.
 *
 * The media grid deliberately uses Coil's [AsyncImage] with only explicit Dp sizes and dp-based
 * shapes: the message bubble wraps content in `Modifier.width(IntrinsicSize.Max)`, where a
 * `fillMax*`/`aspectRatio`/percent-shape resolves to a NaN size during the intrinsic pass and
 * `CornerBasedShape.createOutline` throws "Corner size … NaN".
 */

private const val MAX_VISIBLE_MEDIA = 4
internal val MULTI_ATTACHMENT_BUBBLE_WIDTH = 240.dp

/**
 * Whether the message list renders the new per-type multi-attachment bubbles (`true`, default) or
 * the deprecated single-attachment bubbles (`false`). Provided by CometChatMessageList from its
 * `enableMultipleAttachments` prop and read by InternalContentRenderer.
 */
val LocalEnableMultipleAttachments = staticCompositionLocalOf { true }

/**
 * True when the message's timestamp/receipt row is hidden below the bubble (non-last message of a
 * multi-attachment batch). The per-type bubbles keep 0 bottom padding while the row provides the
 * visual gap, and read this to add a 4dp bottom inset in its place so the content isn't flush with
 * the bubble edge. Provided per-item by MessageListItem.
 */
internal val LocalTimestampHidden = compositionLocalOf { false }

// ---------------------------------------------------------------------------------------------
// Attachment resolution + categorization
// ---------------------------------------------------------------------------------------------

/**
 * Resolves the attachment list for [message], preferring the SDK-parsed array
 * ([MediaMessage.getAttachments]) and falling back to the legacy `metadata.attachments[]` grid path
 * and finally the single [MediaMessage.getAttachment].
 */
internal fun resolveAttachments(message: MediaMessage): List<Attachment> {
    message.attachments?.takeIf { it.isNotEmpty() }?.let { return it }
    resolveAttachmentsFromMetadata(message)?.let { return it }
    return message.attachment?.let { listOf(it) } ?: emptyList()
}

private fun resolveAttachmentsFromMetadata(message: MediaMessage): List<Attachment>? {
    return try {
        val metadata = message.metadata ?: return null
        if (!metadata.has("attachments")) return null
        val array = metadata.getJSONArray("attachments")
        val result = ArrayList<Attachment>(array.length())
        for (i in 0 until array.length()) {
            val json = array.getJSONObject(i)
            result.add(Attachment().apply {
                fileUrl = json.optString("url", "")
                fileName = json.optString("fileName", "")
                fileExtension = json.optString("extension", "")
                fileMimeType = json.optString("mimeType", "")
                fileSize = json.optLong("size", 0).toInt()
            })
        }
        result.takeIf { it.isNotEmpty() }
    } catch (e: Exception) {
        null
    }
}

private fun Attachment.mime(): String = fileMimeType?.lowercase().orEmpty()
internal fun Attachment.isImage(): Boolean = mime().startsWith("image/")
internal fun Attachment.isVideo(): Boolean = mime().startsWith("video/")
internal fun Attachment.isAudio(): Boolean = mime().startsWith("audio/")
internal fun Attachment.isFileKind(): Boolean = !isImage() && !isVideo() && !isAudio()

/**
 * Whether [this] renders as real media in a grid of the given kind. A server-sent message can mix
 * attachment kinds under one message `type` (the UIKit composer never does) — mismatched
 * attachments stay in the grid as broken tiles rather than being dropped.
 */
internal fun Attachment.matchesGridKind(isVideo: Boolean): Boolean =
    if (isVideo) isVideo() else isImage()

/**
 * True when [this] is a mic-recorded voice note. Reads the DD / iOS contract
 * `metaData["audioType"] == "voice_note"`, falling back to the legacy `metaData["voiceNote"]` Bool
 * for messages sent before the key change.
 */
internal fun MediaMessage.isVoiceNote(): Boolean {
    val metadata = metadata ?: return false
    return metadata.optString(UIKitConstants.JSONKeys.AUDIO_TYPE, null) ==
        UIKitConstants.JSONKeys.AUDIO_TYPE_VOICE_NOTE ||
        metadata.optBoolean(UIKitConstants.JSONKeys.VOICE_NOTE, false)
}

// ---------------------------------------------------------------------------------------------
// Batch grouping — by batchId list adjacency (matches iOS; no index/count keys)
// ---------------------------------------------------------------------------------------------

/** The multi-attachment batch id this message belongs to, or null when it is not part of a batch. */
internal fun BaseMessage.batchId(): String? =
    metadata?.optString(UIKitConstants.JSONKeys.BATCH_ID, null)?.takeIf { it.isNotEmpty() }

// ---------------------------------------------------------------------------------------------
// Shared media grid (images + videos)
// ---------------------------------------------------------------------------------------------

/**
 * Type-erased grid styling shared by [CometChatImagesBubble] and [CometChatVideosBubble] — the
 * two public style classes ([CometChatImagesBubbleStyle] / [CometChatVideosBubbleStyle]) carry
 * different video-specific fields, so each converts to this before hitting the shared grid.
 */
internal data class MediaGridAppearance(
    val tileCornerRadius: Dp,
    val gridSpacing: Dp,
    val tilePlaceholderColor: Color,
    val moreOverlayBackgroundColor: Color,
    val moreOverlayTextColor: Color,
    val moreOverlayTextStyle: TextStyle,
    val playBadgeBackgroundColor: Color,
    val playIconTint: Color,
    val durationChipBackgroundColor: Color,
    val durationTextColor: Color,
    val durationTextStyle: TextStyle,
    val showVideoDuration: Boolean,
    val captionTextColor: Color,
    val captionTextStyle: TextStyle
)

/** Video-specific fields never render on image tiles, so they carry inert defaults. */
internal fun CometChatImagesBubbleStyle.toGridAppearance() = MediaGridAppearance(
    tileCornerRadius = tileCornerRadius,
    gridSpacing = gridSpacing,
    tilePlaceholderColor = tilePlaceholderColor,
    moreOverlayBackgroundColor = moreOverlayBackgroundColor,
    moreOverlayTextColor = moreOverlayTextColor,
    moreOverlayTextStyle = moreOverlayTextStyle,
    playBadgeBackgroundColor = Color.Transparent,
    playIconTint = Color.White,
    durationChipBackgroundColor = Color.Transparent,
    durationTextColor = Color.White,
    durationTextStyle = moreOverlayTextStyle,
    showVideoDuration = false,
    captionTextColor = captionTextColor,
    captionTextStyle = captionTextStyle
)

internal fun CometChatVideosBubbleStyle.toGridAppearance() = MediaGridAppearance(
    tileCornerRadius = tileCornerRadius,
    gridSpacing = gridSpacing,
    tilePlaceholderColor = tilePlaceholderColor,
    moreOverlayBackgroundColor = moreOverlayBackgroundColor,
    moreOverlayTextColor = moreOverlayTextColor,
    moreOverlayTextStyle = moreOverlayTextStyle,
    playBadgeBackgroundColor = playBadgeBackgroundColor,
    playIconTint = playIconTint,
    durationChipBackgroundColor = durationChipBackgroundColor,
    durationTextColor = durationTextColor,
    durationTextStyle = durationTextStyle,
    showVideoDuration = showVideoDuration,
    captionTextColor = captionTextColor,
    captionTextStyle = captionTextStyle
)

/**
 * `url_medium` from the Thumbnail Generation extension
 * (metadata.@injected.extensions.thumbnail-generation) — generated from the message's primary
 * (first) attachment, so it only stands in for tile/thumbnail index 0. The SDK appends a `fat=`
 * token that invalidates the CloudFront signature (HTTP 403), so it is stripped. Null when the
 * extension is disabled or the metadata is absent — callers fall back to the attachment fileUrl.
 */
internal fun MediaMessage.thumbnailUrl(): String? = try {
    val thumbnailGeneration = metadata
        ?.optJSONObject("@injected")
        ?.optJSONObject("extensions")
        ?.optJSONObject("thumbnail-generation")
    val urlMedium = thumbnailGeneration?.optString("url_medium", null)
    if (urlMedium.isNullOrEmpty()) null else sanitizeThumbnailUrl(urlMedium)
} catch (e: Exception) {
    null
}

// The `fat` (File Access Token) param the SDK appends breaks the CloudFront signature — signed
// thumbnail URLs already carry their own auth (Signature + Key-Pair-Id).
private fun sanitizeThumbnailUrl(url: String): String {
    val ampIndex = url.indexOf("&fat=")
    if (ampIndex > 0) return url.substring(0, ampIndex)
    val queryIndex = url.indexOf("?fat=")
    if (queryIndex > 0) return url.substring(0, queryIndex)
    return url
}

/** Column of a media grid (images/videos) plus an optional shared caption underneath. */
@Composable
internal fun MediaBubbleContent(
    attachments: List<Attachment>,
    style: MediaGridAppearance,
    isVideo: Boolean,
    caption: String?,
    onMediaClick: ((Int, Attachment) -> Unit)?,
    onMoreClick: ((List<Attachment>) -> Unit)?,
    onLongClick: (() -> Unit)?,
    firstTileThumbnailUrl: String? = null,
    isEdited: Boolean = false,
    modifier: Modifier = Modifier,
    message: BaseMessage? = null,
    alignment: UIKitConstants.MessageBubbleAlignment = UIKitConstants.MessageBubbleAlignment.LEFT,
    textFormatters: List<CometChatTextFormatter> = emptyList()
) {
    Column(
        modifier = modifier
            .width(MULTI_ATTACHMENT_BUBBLE_WIDTH)
            // 0 bottom while the timestamp row below provides the visual gap; when that row is
            // hidden (non-last batch bubble) pad the bottom so the grid isn't flush with the edge.
            .padding(
                start = 5.dp,
                top = 5.dp,
                end = 5.dp,
                bottom = if (LocalTimestampHidden.current) 4.dp else 0.dp
            )
            .semantics { contentDescription = "${if (isVideo) "Video" else "Image"} message with ${attachments.size} item(s)" }
    ) {
        MediaGrid(attachments, style, isVideo, onMediaClick, onMoreClick, onLongClick, firstTileThumbnailUrl)
        MultiAttachmentCaption(
            caption, style.captionTextColor, style.captionTextStyle, isEdited,
            message, alignment, textFormatters
        )
    }
}

@Composable
private fun MediaGrid(
    attachments: List<Attachment>,
    style: MediaGridAppearance,
    isVideo: Boolean,
    onMediaClick: ((Int, Attachment) -> Unit)?,
    onMoreClick: ((List<Attachment>) -> Unit)?,
    onLongClick: (() -> Unit)?,
    firstTileThumbnailUrl: String? = null
) {
    if (attachments.isEmpty()) return
    val visible = minOf(attachments.size, MAX_VISIBLE_MEDIA)
    val moreCount = attachments.size - MAX_VISIBLE_MEDIA

    // Fixed dp sizes only (see file header — IntrinsicSize.Max NaN trap). Squares computed from the
    // known bubble width.
    val contentWidth = MULTI_ATTACHMENT_BUBBLE_WIDTH - 10.dp // 5dp start + 5dp end padding
    val spacing = style.gridSpacing
    val tileSize = (contentWidth - spacing) / 2

    if (attachments.size == 1) {
        MediaTile(
            attachment = attachments[0],
            style = style,
            isVideo = isVideo,
            width = contentWidth,
            height = 224.dp,
            previewUrl = firstTileThumbnailUrl,
            onClick = { onMediaClick?.invoke(0, attachments[0]) },
            onLongClick = onLongClick
        )
        return
    }

    if (attachments.size == 2) {
        // Two items sit side-by-side as squares (matches the design), not stacked rectangles.
        Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
            for (i in 0..1) {
                MediaTile(
                    attachment = attachments[i],
                    style = style,
                    isVideo = isVideo,
                    width = tileSize,
                    height = tileSize,
                    previewUrl = if (i == 0) firstTileThumbnailUrl else null,
                    onClick = { onMediaClick?.invoke(i, attachments[i]) },
                    onLongClick = onLongClick
                )
            }
        }
        return
    }

    if (attachments.size == 3) {
        // The first tile follows its own orientation: a PORTRAIT first image becomes a tall tile on
        // the left with the other two stacked beside it; a LANDSCAPE (or still-loading) first image
        // becomes a wide tile on top with the other two as squares below.
        val fullHeight = tileSize * 2 + spacing
        // A broken first tile has no decodable media — skip detection, keep the wide-top layout.
        val portrait = if (attachments[0].matchesGridKind(isVideo)) {
            rememberFirstMediaPortrait(firstTileThumbnailUrl ?: attachments[0].fileUrl, isVideo)
        } else false
        if (portrait == true) {
            Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
                MediaTile(
                    attachment = attachments[0],
                    style = style,
                    isVideo = isVideo,
                    width = tileSize,
                    height = fullHeight,
                    previewUrl = firstTileThumbnailUrl,
                    onClick = { onMediaClick?.invoke(0, attachments[0]) },
                    onLongClick = onLongClick
                )
                Column(verticalArrangement = Arrangement.spacedBy(spacing)) {
                    for (i in 1..2) {
                        MediaTile(
                            attachment = attachments[i],
                            style = style,
                            isVideo = isVideo,
                            width = tileSize,
                            height = tileSize,
                            previewUrl = null,
                            onClick = { onMediaClick?.invoke(i, attachments[i]) },
                            onLongClick = onLongClick
                        )
                    }
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(spacing)) {
                MediaTile(
                    attachment = attachments[0],
                    style = style,
                    isVideo = isVideo,
                    width = contentWidth,
                    height = tileSize,
                    previewUrl = firstTileThumbnailUrl,
                    onClick = { onMediaClick?.invoke(0, attachments[0]) },
                    onLongClick = onLongClick
                )
                Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
                    for (i in 1..2) {
                        MediaTile(
                            attachment = attachments[i],
                            style = style,
                            isVideo = isVideo,
                            width = tileSize,
                            height = tileSize,
                            previewUrl = null,
                            onClick = { onMediaClick?.invoke(i, attachments[i]) },
                            onLongClick = onLongClick
                        )
                    }
                }
            }
        }
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(spacing)) {
        var index = 0
        while (index < visible) {
            if (visible - index == 1) {
                // Odd final tile (e.g. the 3rd of 3) spans the full width — no empty half-cell.
                val i = index
                MediaTile(
                    attachment = attachments[i],
                    style = style,
                    isVideo = isVideo,
                    width = contentWidth,
                    height = tileSize,
                    previewUrl = if (i == 0) firstTileThumbnailUrl else null,
                    onClick = { onMediaClick?.invoke(i, attachments[i]) },
                    onLongClick = onLongClick
                )
                index++
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
                    var col = 0
                    while (col < 2) {
                        val i = index
                        val showMore = i == visible - 1 && moreCount > 0
                        MediaTile(
                            attachment = attachments[i],
                            style = style,
                            isVideo = isVideo,
                            width = tileSize,
                            height = tileSize,
                            previewUrl = if (i == 0) firstTileThumbnailUrl else null,
                            overlayMoreCount = if (showMore) moreCount else 0,
                            onClick = {
                                if (showMore) onMoreClick?.invoke(attachments)
                                else onMediaClick?.invoke(i, attachments[i])
                            },
                            onLongClick = onLongClick
                        )
                        index++
                        col++
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MediaTile(
    attachment: Attachment,
    style: MediaGridAppearance,
    isVideo: Boolean,
    width: Dp,
    height: Dp,
    previewUrl: String? = null,
    overlayMoreCount: Int = 0,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)?
) {
    // Kind-mismatched attachment (server-sent mixed payload) — broken tile: placeholder + the
    // unknown-file "?" glyph. Tapping it opens the viewer like any tile; the viewer renders the
    // page as "No preview available" with a Download button.
    val isBroken = !attachment.matchesGridKind(isVideo)
    val shape = RoundedCornerShape(style.tileCornerRadius)
    Box(
        modifier = Modifier
            .size(width, height)
            .clip(shape)
            .background(style.tilePlaceholderColor)
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        if (isBroken) {
            Image(
                painter = painterResource(id = R.drawable.cometchat_unsupported_file_icon),
                contentDescription = attachment.fileName ?: "Unsupported attachment",
                modifier = Modifier
                    .size(width = 36.dp, height = 44.dp)
                    .align(Alignment.Center)
            )
        } else if (isVideo) {
            // Extension thumbnail (url_medium) when available for this tile; otherwise the frame
            // + duration are loaded on-the-fly from the (re-signed) video URL — iOS-compatible.
            VideoTileContent(
                url = attachment.fileUrl ?: "",
                thumbnailUrl = previewUrl,
                style = style,
                width = width,
                height = height,
                showBadge = overlayMoreCount == 0
            )
        } else {
            AsyncImage(
                model = previewUrl ?: attachment.fileUrl ?: "",
                contentDescription = attachment.fileName ?: "Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(width, height)
            )
        }

        if (overlayMoreCount > 0) {
            Box(
                modifier = Modifier
                    .size(width, height)
                    .background(style.moreOverlayBackgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+$overlayMoreCount",
                    style = style.moreOverlayTextStyle,
                    color = style.moreOverlayTextColor
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Video thumbnail + duration, loaded on-the-fly from the video URL (iOS-compatible; no metadata).
// ---------------------------------------------------------------------------------------------

private data class VideoTileMeta(val thumbnail: ImageBitmap?, val durationLabel: String?)

/** Max decoded video frames kept in memory; each entry holds a full-frame bitmap. */
private const val VIDEO_META_CACHE_MAX = 50

/**
 * URL-keyed, LRU-bounded cache so tiles don't re-decode a frame on every recompose/scroll while
 * still capping how many full-frame bitmaps stay resident (avoids unbounded heap growth / OOM in
 * long media-heavy sessions). [LruCache] is internally synchronized, so concurrent IO reads/writes
 * are safe.
 */
private val videoMetaCache = LruCache<String, VideoTileMeta>(VIDEO_META_CACHE_MAX)

/**
 * First frame of a (remote) video url as state — same retriever + cache the video tiles use.
 * Null until decoded, and stays null on failure (callers keep their placeholder).
 */
/**
 * Resolves whether the first media item is portrait (taller than wide) so the 3-item grid can pick
 * the tall-left vs wide-top layout. Returns null while still loading (caller shows the wide-top
 * default until it settles). Images use Coil's decoder; videos reuse the [MediaMetadataRetriever]
 * frame already cached for the tile.
 */
@Composable
private fun rememberFirstMediaPortrait(url: String?, isVideo: Boolean): Boolean? {
    val context = LocalContext.current
    return produceState<Boolean?>(initialValue = null, url, isVideo) {
        val target = url
        if (target.isNullOrEmpty()) {
            value = null
            return@produceState
        }
        value = withContext(Dispatchers.IO) {
            try {
                if (isVideo) {
                    val meta = videoMetaCache.get(target) ?: loadVideoMeta(target).also { videoMetaCache.put(target, it) }
                    meta.thumbnail?.let { it.height > it.width }
                } else {
                    val result = context.imageLoader.execute(
                        ImageRequest.Builder(context).data(target).allowHardware(false).build()
                    )
                    (result as? SuccessResult)?.drawable?.let { d ->
                        if (d.intrinsicWidth > 0 && d.intrinsicHeight > 0) d.intrinsicHeight > d.intrinsicWidth else null
                    }
                }
            } catch (e: Exception) {
                null
            }
        }
    }.value
}

@Composable
internal fun rememberVideoFrame(url: String?): ImageBitmap? {
    val key = url.orEmpty()
    var meta by remember(key) { mutableStateOf(videoMetaCache.get(key)) }
    LaunchedEffect(key) {
        if (key.isNotEmpty() && videoMetaCache.get(key) == null) {
            val loaded = loadVideoMeta(key)
            videoMetaCache.put(key, loaded)
            meta = loaded
        }
    }
    return meta?.thumbnail
}

@Composable
private fun VideoTileContent(
    url: String,
    style: MediaGridAppearance,
    width: Dp,
    height: Dp,
    showBadge: Boolean,
    thumbnailUrl: String? = null
) {
    var meta by remember(url) { mutableStateOf(videoMetaCache.get(url)) }
    LaunchedEffect(url) {
        if (url.isNotEmpty() && videoMetaCache.get(url) == null) {
            val loaded = loadVideoMeta(url)
            videoMetaCache.put(url, loaded)
            meta = loaded
        }
    }

    Box(modifier = Modifier.size(width, height)) {
        if (thumbnailUrl != null) {
            // Extension-generated thumbnail — the retriever still runs above for the duration.
            AsyncImage(
                model = thumbnailUrl,
                contentDescription = "Video",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else meta?.thumbnail?.let { bmp ->
            Image(
                bitmap = bmp,
                contentDescription = "Video",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        if (showBadge) {
            // Play badge — dark scrim darker when there's no frame yet.
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(40.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(style.playBadgeBackgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.cometchat_play_icon),
                    contentDescription = "Play",
                    tint = style.playIconTint,
                    modifier = Modifier.size(24.dp)
                )
            }

            if (style.showVideoDuration) meta?.durationLabel?.let { label ->
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(style.durationChipBackgroundColor)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = label,
                        style = style.durationTextStyle,
                        color = style.durationTextColor
                    )
                }
            }
        }
    }
}

/**
 * Reads a preview frame + duration from a (re-signed, remote) video URL via
 * [MediaMetadataRetriever] on the IO dispatcher. Returns a null thumbnail/label on failure (e.g. no
 * network) so the tile falls back to the dark play-badge placeholder.
 */
private suspend fun loadVideoMeta(url: String): VideoTileMeta = withContext(Dispatchers.IO) {
    val retriever = MediaMetadataRetriever()
    try {
        retriever.setDataSource(url, HashMap())
        val durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull()
        val frame = retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
        VideoTileMeta(
            thumbnail = frame?.asImageBitmap(),
            durationLabel = durationMs?.takeIf { it > 0 }?.let { formatDurationMs(it) }
        )
    } catch (e: Exception) {
        VideoTileMeta(null, null)
    } finally {
        runCatching { retriever.release() }
    }
}

/**
 * Shared caption row shown under a media bubble's content (multi- and single-attachment alike).
 *
 * Captions travel as markdown — the composer serializes the styled compose text with
 * `toMarkdown()` on send, exactly like a plain text message — so they are parsed back through
 * [MarkdownRenderer] here and rendered with inline styling (bold, italic, underline,
 * strikethrough, inline code, links). Rendering into a single [Text] means blockquotes and
 * fenced code blocks get inline-level styling only (no quote bar / code container sub-views),
 * matching iOS `applyCaption`.
 *
 * [message] and [textFormatters] enable the same formatter pass the text bubble runs, so a
 * caption's mentions resolve to display names instead of raw `<@uid:...>` tokens.
 */
@Composable
internal fun MultiAttachmentCaption(
    caption: String?,
    color: Color,
    textStyle: TextStyle? = null,
    isEdited: Boolean = false,
    message: BaseMessage? = null,
    alignment: UIKitConstants.MessageBubbleAlignment = UIKitConstants.MessageBubbleAlignment.LEFT,
    textFormatters: List<CometChatTextFormatter> = emptyList()
) {
    if (caption.isNullOrEmpty()) return
    val formatted = rememberCaptionFormatterOutput(caption, message, alignment, textFormatters)
    val captionStyle = textStyle ?: CometChatTheme.typography.bodyRegular
    MarkdownSegments(
        sourceText = formatted?.text ?: caption,
        formattedText = formatted,
        textStyle = captionStyle,
        textColor = color,
        linkColor = color,
        isOutgoing = alignment == UIKitConstants.MessageBubbleAlignment.RIGHT,
        // 0 bottom — the timestamp row below the bubble provides the gap (same rule as the
        // bubble containers); a bottom inset here doubles up and bloats captioned bubbles.
        modifier = Modifier.padding(start = 8.dp, top = 6.dp, end = 8.dp)
    )
    if (isEdited) {
        Text(
            text = stringResource(R.string.cometchat_edited),
            style = CometChatTheme.typography.caption2Regular,
            color = color,
            modifier = Modifier.padding(start = 8.dp, top = 2.dp, end = 8.dp)
        )
    }
}

/**
 * Runs the first stage of the text bubble's pipeline on a caption: the [textFormatters] (turning
 * e.g. a `<@uid:...>` mention token into a styled display name). The result's `text` is what the
 * markdown is then parsed from, so the formatter spans and markdown spans line up. Null when there
 * is nothing to format — the caller falls back to the raw caption. Used by both the single [Text]
 * caption path ([buildCaptionAnnotatedString]) and the block-level [MarkdownSegments] path.
 */
@Composable
internal fun rememberCaptionFormatterOutput(
    caption: String,
    message: BaseMessage?,
    alignment: UIKitConstants.MessageBubbleAlignment,
    textFormatters: List<CometChatTextFormatter>
): AnnotatedString? {
    val context = LocalContext.current
    // mentionedUsers is part of the key: an in-progress message has it set after the first
    // composition, and the caption must restyle when it lands.
    return remember(caption, message?.id, message?.muid, message?.mentionedUsers, alignment, textFormatters) {
        if (message != null && textFormatters.isNotEmpty()) {
            FormatterUtils.getFormattedText(
                context = context,
                baseMessage = message,
                formattingType = UIKitConstants.FormattingType.MESSAGE_BUBBLE,
                alignment = alignment,
                text = caption,
                formatters = textFormatters
            )
        } else {
            null
        }
    }
}

/**
 * Multi-line [AnnotatedString] from caption markdown. Colors are deliberately not set on any
 * span — everything (links and code included) inherits the caption's base color, which is
 * white on the tinted outgoing bubble and the theme text color on incoming.
 *
 * [formattedText] is the formatter output [markdown] was taken from (null when no formatters
 * ran); its spans are overlaid onto the marker-stripped text, remapping offsets as the text
 * bubble does.
 */
internal fun buildCaptionAnnotatedString(
    markdown: String,
    formattedText: AnnotatedString? = null
): AnnotatedString = buildAnnotatedString {
    val segments = MarkdownRenderer.parse(markdown)
    // Segments are matched back to their slice of [markdown] in order, so a repeated line styles
    // its own occurrence rather than the first one.
    var searchFrom = 0
    fun sourceStartOf(rawText: String): Int {
        val start = markdown.indexOf(rawText, searchFrom)
        if (start >= 0) searchFrom = start + rawText.length
        return start
    }
    for ((index, segment) in segments.withIndex()) {
        if (index > 0) append("\n")
        when (segment) {
            is MarkdownRenderer.RenderedSegment.CodeBlock -> {
                val start = length
                append(segment.code)
                addStyle(SpanStyle(fontFamily = FontFamily.Monospace), start, length)
            }
            is MarkdownRenderer.RenderedSegment.Blockquote ->
                appendCaptionSegment("▎ ", segment.text, formattedText, sourceStartOf(segment.text))
            is MarkdownRenderer.RenderedSegment.BulletItem ->
                appendCaptionSegment("• ", segment.text, formattedText, sourceStartOf(segment.text))
            is MarkdownRenderer.RenderedSegment.OrderedItem ->
                appendCaptionSegment("${segment.number}. ", segment.text, formattedText, sourceStartOf(segment.text))
            is MarkdownRenderer.RenderedSegment.Text ->
                appendCaptionSegment("", segment.text, formattedText, sourceStartOf(segment.text))
        }
    }
}

/**
 * Appends one segment's text (markers stripped), applies its inline format spans, then overlays
 * any [formattedText] spans (mentions) that fall inside the segment. [sourceStart] is where the
 * segment's raw text begins in the formatter output, which is what those spans are indexed
 * against; -1 when the segment couldn't be located and the overlay is skipped.
 */
private fun AnnotatedString.Builder.appendCaptionSegment(
    prefix: String,
    rawText: String,
    formattedText: AnnotatedString? = null,
    sourceStart: Int = -1
) {
    val (plain, spans) = MarkdownRenderer.parseInline(rawText)
    append(prefix)
    val base = length
    append(plain)
    for (span in spans) {
        val start = (base + span.start).coerceAtMost(length)
        val end = (base + span.end).coerceAtMost(length)
        if (start >= end) continue
        when (span.format) {
            RichTextFormat.BOLD -> addStyle(
                SpanStyle(fontWeight = FontWeight.Bold, fontFamily = cometchatFontBold), start, end
            )
            RichTextFormat.ITALIC -> addStyle(
                SpanStyle(fontStyle = FontStyle.Italic, fontFamily = FontFamily.Default), start, end
            )
            RichTextFormat.UNDERLINE -> addStyle(
                SpanStyle(textDecoration = TextDecoration.Underline), start, end
            )
            RichTextFormat.STRIKETHROUGH -> addStyle(
                SpanStyle(textDecoration = TextDecoration.LineThrough), start, end
            )
            RichTextFormat.INLINE_CODE -> addStyle(
                SpanStyle(fontFamily = FontFamily.Monospace), start, end
            )
            RichTextFormat.LINK -> addStyle(
                SpanStyle(textDecoration = TextDecoration.Underline), start, end
            )
            else -> { /* block-level formats handled at segment level */ }
        }
    }

    if (formattedText == null || sourceStart < 0) return
    val positionMap = buildPositionMap(rawText, plain)
    val sourceEnd = sourceStart + rawText.length
    for (span in formattedText.spanStyles) {
        if (span.start >= sourceEnd || span.end <= sourceStart) continue
        val relativeStart = (span.start - sourceStart).coerceAtLeast(0)
        val relativeEnd = (span.end - sourceStart).coerceAtMost(rawText.length)
        val plainStart = mapPositionUsingMap(relativeStart, positionMap, plain.length)
        val plainEnd = mapPositionUsingMap(relativeEnd, positionMap, plain.length)
        if (plainStart in 0 until plainEnd && plainEnd <= plain.length) {
            addStyle(span.item, base + plainStart, base + plainEnd)
        }
    }
}
