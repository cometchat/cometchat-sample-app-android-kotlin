package com.cometchat.uikit.kotlin.presentation.messagecomposer.ui

import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.multiattachment.MultiAttachmentUtils
import com.cometchat.uikit.kotlin.theme.CometChatTheme
import com.cometchat.uikit.core.models.AttachmentUploadStatus
import com.cometchat.uikit.core.models.AttachmentUploadTile

/**
 * Renders the composer attachment tray: one [CometChatAttachmentTile]-style row item per staged
 * [AttachmentUploadTile]. Image/video files render as a square media tile, picker-selected audio
 * as an audio card (play glyph + name + duration), and everything else as a document card. The
 * Views counterpart of the Compose `CometChatAttachmentTile`.
 *
 * Upload state follows the design system's mobile card states: the tile's leading visual
 * (thumbnail / file-type icon / play button) dims under a dark overlay — a white progress ring
 * while uploading, a round red retry / error badge on failure — while a failed / rejected card
 * also swaps its border to the error color and its meta line to "Tap to retry" / "Upload failed".
 * The corner ✕ cancels while uploading and removes otherwise. Intents are surfaced via the
 * callbacks.
 *
 * @param onCancel Invoked when the badge is tapped while uploading (cancel the in-flight upload).
 * @param onRemove Invoked when the ✕ is tapped in any non-uploading state (remove + cancel).
 * @param onRetry Invoked when a failed tile is tapped (retry the upload).
 * @param onClick Invoked when a successfully-uploaded tile is tapped (open the preview).
 * @param onRejected Invoked when a rejected tile is tapped (surface the SDK rejection reason, e.g.
 *   the size-limit message) — rejected tiles are not retryable, so tapping only explains the error.
 */
class CometChatAttachmentTileAdapter(
    private val onCancel: (AttachmentUploadTile) -> Unit = {},
    private val onRemove: (AttachmentUploadTile) -> Unit = {},
    private val onRetry: (AttachmentUploadTile) -> Unit = {},
    private val onClick: (AttachmentUploadTile) -> Unit = {},
    private val onRejected: (AttachmentUploadTile) -> Unit = {}
) : RecyclerView.Adapter<CometChatAttachmentTileAdapter.TileViewHolder>() {

    private val tiles = mutableListOf<AttachmentUploadTile>()

    // Inline audio playback for audio cards: one shared player, so at most one staged audio plays
    // at a time. The ticker pushes position updates into whichever holder currently shows the
    // playing tile.
    private var mediaPlayer: android.media.MediaPlayer? = null
    private var playingFileId: String? = null
    private var isAudioPlaying: Boolean = false
    private val tickHandler = android.os.Handler(android.os.Looper.getMainLooper())
    private val progressTicker = object : Runnable {
        override fun run() {
            val player = mediaPlayer ?: return
            activeAudioHolder?.takeIf { it.boundFileId == playingFileId }
                ?.updateAudioProgress(runCatching { player.currentPosition }.getOrDefault(0))
            if (isAudioPlaying) tickHandler.postDelayed(this, 200)
        }
    }
    private var activeAudioHolder: TileViewHolder? = null

    /** Stops and releases any in-flight audio playback, resetting the visible card if any. */
    fun stopAudioPlayback() {
        tickHandler.removeCallbacks(progressTicker)
        runCatching { mediaPlayer?.release() }
        mediaPlayer = null
        isAudioPlaying = false
        val stoppedFileId = playingFileId
        playingFileId = null
        activeAudioHolder?.takeIf { it.boundFileId == stoppedFileId }?.resetAudioPlaybackUi()
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        stopAudioPlayback()
        activeAudioHolder = null
        super.onDetachedFromRecyclerView(recyclerView)
    }

    fun submitList(newTiles: List<AttachmentUploadTile>) {
        // The playing tile got removed (or the tray cleared on send) — stop the audio with it.
        if (playingFileId != null && newTiles.none { it.fileId == playingFileId }) {
            stopAudioPlayback()
        }
        val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = tiles.size
            override fun getNewListSize() = newTiles.size
            override fun areItemsTheSame(oldPos: Int, newPos: Int) =
                tiles[oldPos].fileId == newTiles[newPos].fileId
            override fun areContentsTheSame(oldPos: Int, newPos: Int) =
                tiles[oldPos] == newTiles[newPos]
        })
        tiles.clear()
        tiles.addAll(newTiles)
        diff.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TileViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.cometchat_attachment_tile, parent, false)
        return TileViewHolder(view)
    }

    override fun getItemCount(): Int = tiles.size

    override fun onBindViewHolder(holder: TileViewHolder, position: Int) {
        holder.bind(tiles[position])
    }

    inner class TileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ctx = itemView.context
        private val density = ctx.resources.displayMetrics.density

        private val mediaCard = itemView.findViewById<com.google.android.material.card.MaterialCardView>(R.id.mediaCard)
        private val ivMediaThumb = itemView.findViewById<ImageView>(R.id.ivMediaThumb)
        private val playBadge = itemView.findViewById<FrameLayout>(R.id.playBadge)
        private val ivPlayIcon = itemView.findViewById<ImageView>(R.id.ivPlayIcon)
        private val tvVideoDuration = itemView.findViewById<TextView>(R.id.tvVideoDuration)
        private val vMediaDim = itemView.findViewById<View>(R.id.vMediaDim)
        private val mediaProgress = itemView.findViewById<ProgressBar>(R.id.mediaProgress)
        private val mediaErrorBadge = itemView.findViewById<FrameLayout>(R.id.mediaErrorBadge)
        private val ivMediaErrorIcon = itemView.findViewById<ImageView>(R.id.ivMediaErrorIcon)

        private val fileCard = itemView.findViewById<com.google.android.material.card.MaterialCardView>(R.id.fileCard)
        private val fileIconContainer = itemView.findViewById<FrameLayout>(R.id.fileIconContainer)
        private val ivFileIcon = itemView.findViewById<ImageView>(R.id.ivFileIcon)
        private val fileIconOverlay = itemView.findViewById<FrameLayout>(R.id.fileIconOverlay)
        private val fileProgress = itemView.findViewById<ProgressBar>(R.id.fileProgress)
        private val fileErrorBadge = itemView.findViewById<FrameLayout>(R.id.fileErrorBadge)
        private val ivFileErrorIcon = itemView.findViewById<ImageView>(R.id.ivFileErrorIcon)
        private val tvFileName = itemView.findViewById<TextView>(R.id.tvFileName)
        private val tvFileMeta = itemView.findViewById<TextView>(R.id.tvFileMeta)

        private val audioCard = itemView.findViewById<com.google.android.material.card.MaterialCardView>(R.id.audioCard)
        private val audioPlayButton = itemView.findViewById<FrameLayout>(R.id.audioPlayButton)
        private val audioPlayOverlay = itemView.findViewById<FrameLayout>(R.id.audioPlayOverlay)
        private val ivAudioPlayIcon = itemView.findViewById<ImageView>(R.id.ivAudioPlayIcon)
        private val audioProgress = itemView.findViewById<ProgressBar>(R.id.audioProgress)
        private val audioErrorBadge = itemView.findViewById<FrameLayout>(R.id.audioErrorBadge)
        private val ivAudioErrorIcon = itemView.findViewById<ImageView>(R.id.ivAudioErrorIcon)
        private val tvAudioName = itemView.findViewById<TextView>(R.id.tvAudioName)
        private val sbAudioSeek = itemView.findViewById<android.widget.SeekBar>(R.id.sbAudioSeek)
        private val tvAudioTime = itemView.findViewById<TextView>(R.id.tvAudioTime)

        /** fileId of the tile this holder currently renders — lets the ticker target the holder. */
        var boundFileId: String? = null
            private set

        private var audioTotalMillis: Long = 0L

        private val cornerBadge = itemView.findViewById<FrameLayout>(R.id.cornerBadge)
        private val ivBadgeIcon = itemView.findViewById<ImageView>(R.id.ivBadgeIcon)

        fun bind(tile: AttachmentUploadTile) {
            boundFileId = tile.fileId
            val errorColor = CometChatTheme.getErrorColor(ctx)
            // Tile shape follows the picker-decided category, not the MIME type: a photo picked
            // through the file picker renders as a document card.
            val isMedia = tile.category == CometChatConstants.MESSAGE_TYPE_IMAGE ||
                tile.category == CometChatConstants.MESSAGE_TYPE_VIDEO
            val isAudio = tile.category == CometChatConstants.MESSAGE_TYPE_AUDIO
            val uploading = tile.status == AttachmentUploadStatus.UPLOADING
            val failed = tile.status == AttachmentUploadStatus.FAILED
            val rejected = tile.status == AttachmentUploadStatus.REJECTED
            val done = tile.status == AttachmentUploadStatus.DONE

            mediaCard.visibility = if (isMedia) View.VISIBLE else View.GONE
            audioCard.visibility = if (isAudio) View.VISIBLE else View.GONE
            fileCard.visibility = if (!isMedia && !isAudio) View.VISIBLE else View.GONE

            when {
                isMedia -> bindMedia(tile, errorColor, uploading, failed, rejected, done)
                isAudio -> bindAudio(tile, errorColor, uploading, failed, rejected, done)
                else -> bindFile(tile, errorColor, uploading, failed, rejected, done)
            }

            bindCornerBadge(tile, uploading)
        }

        private fun bindMedia(
            tile: AttachmentUploadTile,
            errorColor: Int,
            uploading: Boolean,
            failed: Boolean,
            rejected: Boolean,
            done: Boolean
        ) {
            val isError = failed || rejected
            mediaCard.setCardBackgroundColor(CometChatTheme.getBackgroundColor3(ctx))
            mediaCard.strokeColor =
                if (isError) errorColor else CometChatTheme.getStrokeColorDefault(ctx)

            // The thumbnail stays visible in every state — uploading / failed blur and dim it
            // rather than hiding it (blur needs API 31; the dim still communicates the state
            // below that). Glide's String loader resolves file paths, content:// and http(s)
            // uris alike, so we pass the raw string (Uri.parse would break bare file paths).
            val model = tile.localUri ?: tile.attachment?.fileUrl
            if (model != null) {
                ivMediaThumb.visibility = View.VISIBLE
                Glide.with(ivMediaThumb).load(model).centerCrop().into(ivMediaThumb)
            } else {
                ivMediaThumb.visibility = View.GONE
                Glide.with(ivMediaThumb).clear(ivMediaThumb)
            }
            if (android.os.Build.VERSION.SDK_INT >= 31) {
                ivMediaThumb.setRenderEffect(
                    if (uploading || isError) {
                        android.graphics.RenderEffect.createBlurEffect(
                            3 * density, 3 * density, android.graphics.Shader.TileMode.CLAMP
                        )
                    } else null
                )
            }

            vMediaDim.visibility = if (uploading || isError) View.VISIBLE else View.GONE
            if (uploading || isError) {
                vMediaDim.setBackgroundColor(
                    ColorUtils.setAlphaComponent(CometChatTheme.getColorBlack(ctx), 89) // ~0.35
                )
            }

            mediaProgress.visibility = if (uploading) View.VISIBLE else View.GONE
            mediaProgress.indeterminateTintList =
                ColorStateList.valueOf(CometChatTheme.getColorWhite(ctx))

            mediaErrorBadge.visibility = if (isError) View.VISIBLE else View.GONE
            if (isError) {
                bindErrorBadge(mediaErrorBadge, ivMediaErrorIcon, errorColor, failed)
            }

            val isVideo = tile.category == CometChatConstants.MESSAGE_TYPE_VIDEO
            playBadge.visibility = if (done && isVideo) View.VISIBLE else View.GONE
            if (done && isVideo) {
                playBadge.background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(ColorUtils.setAlphaComponent(CometChatTheme.getColorBlack(ctx), 115)) // ~0.45
                }
                ivPlayIcon.setColorFilter(CometChatTheme.getColorWhite(ctx))
            }

            // Duration chip, bottom-start, only on a successfully-uploaded video.
            val durationMillis = tile.durationMillis
            if (done && isVideo && durationMillis != null) {
                tvVideoDuration.visibility = View.VISIBLE
                val totalSeconds = (durationMillis / 1000L).coerceAtLeast(0L)
                tvVideoDuration.text = String.format("%d:%02d", totalSeconds / 60, totalSeconds % 60)
                tvVideoDuration.setTextColor(CometChatTheme.getColorWhite(ctx))
                tvVideoDuration.background = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = 4 * density
                    setColor(ColorUtils.setAlphaComponent(CometChatTheme.getColorBlack(ctx), 153)) // ~0.6
                }
            } else {
                tvVideoDuration.visibility = View.GONE
            }

            bindTileClick(mediaCard, tile, done, failed, rejected)
        }

        private fun bindFile(
            tile: AttachmentUploadTile,
            errorColor: Int,
            uploading: Boolean,
            failed: Boolean,
            rejected: Boolean,
            done: Boolean
        ) {
            val isError = failed || rejected
            // Colored per-type glyph (PDF red, DOC blue, XLS green, …) — the same mapping the
            // Compose tray and the bubbles use.
            ivFileIcon.setImageResource(MultiAttachmentUtils.fileIconRes(tile.mimeType, tile.name))
            fileCard.setCardBackgroundColor(CometChatTheme.getBackgroundColor2(ctx))
            fileCard.strokeColor =
                if (isError) errorColor else CometChatTheme.getStrokeColorDefault(ctx)
            fileIconContainer.background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 12 * density
                setColor(CometChatTheme.getColorWhite(ctx))
                setStroke((1 * density).toInt(), CometChatTheme.getStrokeColorLight(ctx))
            }

            bindSlotOverlay(
                overlay = fileIconOverlay,
                progress = fileProgress,
                errorBadge = fileErrorBadge,
                errorIcon = ivFileErrorIcon,
                cornerRadius = 12 * density,
                errorColor = errorColor,
                uploading = uploading,
                failed = failed,
                rejected = rejected
            )

            tvFileName.text = tile.name
            tvFileName.setTextColor(CometChatTheme.getTextColorPrimary(ctx))
            tvFileMeta.text = when {
                failed -> ctx.getString(R.string.cometchat_attachment_tap_to_retry)
                rejected -> ctx.getString(R.string.cometchat_attachment_upload_failed)
                else -> fileMetaLabel(tile)
            }
            tvFileMeta.setTextColor(
                if (isError) errorColor else CometChatTheme.getTextColorSecondary(ctx)
            )

            bindTileClick(fileCard, tile, done, failed, rejected)
        }

        private fun bindAudio(
            tile: AttachmentUploadTile,
            errorColor: Int,
            uploading: Boolean,
            failed: Boolean,
            rejected: Boolean,
            done: Boolean
        ) {
            val isError = failed || rejected
            audioCard.setCardBackgroundColor(CometChatTheme.getBackgroundColor2(ctx))
            audioCard.strokeColor =
                if (isError) errorColor else CometChatTheme.getStrokeColorDefault(ctx)
            audioPlayButton.background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(CometChatTheme.getPrimaryColor(ctx))
            }
            ivAudioPlayIcon.setColorFilter(CometChatTheme.getColorWhite(ctx))

            bindSlotOverlay(
                overlay = audioPlayOverlay,
                progress = audioProgress,
                errorBadge = audioErrorBadge,
                errorIcon = ivAudioErrorIcon,
                cornerRadius = 20 * density, // circular: half the 40dp play button
                errorColor = errorColor,
                uploading = uploading,
                failed = failed,
                rejected = rejected
            )

            tvAudioName.text = tile.name
            tvAudioName.setTextColor(CometChatTheme.getTextColorPrimary(ctx))

            bindAudioPlayback(tile, isError, done)

            tvAudioTime.text = when {
                failed -> ctx.getString(R.string.cometchat_attachment_tap_to_retry)
                rejected -> ctx.getString(R.string.cometchat_attachment_upload_failed)
                else -> formatDuration(currentAudioPositionMillis(tile)) + "/" + formatDuration(audioTotalMillis)
            }
            tvAudioTime.setTextColor(
                if (isError) errorColor else CometChatTheme.getTextColorTertiary(ctx)
            )

            // The card itself retries on failure / explains a rejection — playback lives on the
            // play button.
            audioCard.isClickable = failed || rejected
            when {
                failed -> audioCard.setOnClickListener { onRetry(tile) }
                rejected -> audioCard.setOnClickListener { onRejected(tile) }
                else -> audioCard.setOnClickListener(null)
            }
        }

        /** Wires the seek bar + play/pause button; restores live state when this holder shows the
         *  currently-playing tile (e.g. after a rebind). */
        private fun bindAudioPlayback(tile: AttachmentUploadTile, isError: Boolean, done: Boolean) {
            val isThisPlaying = tile.fileId == playingFileId
            if (isThisPlaying) activeAudioHolder = this

            audioTotalMillis = if (isThisPlaying) {
                runCatching { mediaPlayer?.duration?.toLong() }.getOrNull() ?: (tile.durationMillis ?: 0L)
            } else {
                tile.durationMillis ?: 0L
            }

            sbAudioSeek.visibility = if (isError) View.GONE else View.VISIBLE
            sbAudioSeek.progressTintList = ColorStateList.valueOf(CometChatTheme.getPrimaryColor(ctx))
            sbAudioSeek.progressBackgroundTintList =
                ColorStateList.valueOf(CometChatTheme.getNeutralColor300(ctx))
            sbAudioSeek.max = audioTotalMillis.toInt().coerceAtLeast(1)
            sbAudioSeek.progress = currentAudioPositionMillis(tile).toInt()
            sbAudioSeek.isEnabled = isThisPlaying
            sbAudioSeek.setOnSeekBarChangeListener(object : android.widget.SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: android.widget.SeekBar, progress: Int, fromUser: Boolean) {
                    if (fromUser && tile.fileId == playingFileId) {
                        runCatching { mediaPlayer?.seekTo(progress) }
                        tvAudioTime.text =
                            formatDuration(progress.toLong()) + "/" + formatDuration(audioTotalMillis)
                    }
                }
                override fun onStartTrackingTouch(seekBar: android.widget.SeekBar) = Unit
                override fun onStopTrackingTouch(seekBar: android.widget.SeekBar) = Unit
            })

            setPlayButtonState(playing = isThisPlaying && isAudioPlaying)
            audioPlayButton.isClickable = done
            if (done) audioPlayButton.setOnClickListener { toggleAudioPlayback(tile) }
            else audioPlayButton.setOnClickListener(null)
        }

        private fun currentAudioPositionMillis(tile: AttachmentUploadTile): Long =
            if (tile.fileId == playingFileId) {
                runCatching { mediaPlayer?.currentPosition?.toLong() }.getOrNull() ?: 0L
            } else 0L

        private fun toggleAudioPlayback(tile: AttachmentUploadTile) {
            // Pause / resume the tile that already owns the player.
            if (tile.fileId == playingFileId) {
                val player = mediaPlayer ?: return
                if (runCatching { player.isPlaying }.getOrDefault(false)) {
                    runCatching { player.pause() }
                    isAudioPlaying = false
                    tickHandler.removeCallbacks(progressTicker)
                    setPlayButtonState(playing = false)
                } else {
                    runCatching { player.start() }
                    isAudioPlaying = true
                    tickHandler.post(progressTicker)
                    setPlayButtonState(playing = true)
                }
                return
            }

            // Switch playback to this tile (stops whichever audio was playing). prepareAsync
            // keeps the (potentially slow) data-source open off the main thread; playback starts
            // from the onPrepared callback.
            stopAudioPlayback()
            val source = tile.localUri ?: tile.attachment?.fileUrl ?: return
            val player = runCatching {
                android.media.MediaPlayer().apply {
                    // localUri is a bare file path in Views staging; content://, file:// and
                    // http(s) uris go through the Uri overload.
                    if (source.startsWith("/")) setDataSource(source)
                    else setDataSource(ctx, Uri.parse(source))
                    setOnCompletionListener { stopAudioPlayback() }
                    setOnPreparedListener { prepared -> onAudioPrepared(tile, prepared) }
                    prepareAsync()
                }
            }.getOrNull() ?: return

            mediaPlayer = player
            playingFileId = tile.fileId
            activeAudioHolder = this
        }

        /** Starts playback once the async preparation finishes (main-thread callback). */
        private fun onAudioPrepared(tile: AttachmentUploadTile, player: android.media.MediaPlayer) {
            // Playback may have been stopped / moved to another tile while preparing.
            if (tile.fileId != playingFileId || player !== mediaPlayer) return
            audioTotalMillis = runCatching { player.duration.toLong() }.getOrNull()
                ?.takeIf { it > 0 } ?: (tile.durationMillis ?: 0L)
            if (boundFileId == tile.fileId) {
                sbAudioSeek.max = audioTotalMillis.toInt().coerceAtLeast(1)
                sbAudioSeek.isEnabled = true
                setPlayButtonState(playing = true)
            }
            runCatching { player.start() }
            isAudioPlaying = true
            tickHandler.post(progressTicker)
        }

        /** Swaps the play/pause glyph and its accessibility label together. */
        private fun setPlayButtonState(playing: Boolean) {
            ivAudioPlayIcon.setImageResource(
                if (playing) R.drawable.cometchat_ic_pause else R.drawable.cometchat_play_icon
            )
            audioPlayButton.contentDescription =
                ctx.getString(if (playing) R.string.cometchat_pause else R.string.cometchat_play)
        }

        /** Pushes a live playback position into the seek bar + time label (called by the ticker). */
        fun updateAudioProgress(positionMillis: Int) {
            sbAudioSeek.progress = positionMillis
            tvAudioTime.text =
                formatDuration(positionMillis.toLong()) + "/" + formatDuration(audioTotalMillis)
        }

        /** Resets the card to the not-playing state (playback stopped or moved to another tile). */
        fun resetAudioPlaybackUi() {
            sbAudioSeek.progress = 0
            sbAudioSeek.isEnabled = false
            setPlayButtonState(playing = false)
            tvAudioTime.text = formatDuration(0L) + "/" + formatDuration(audioTotalMillis)
        }

        /**
         * The upload-state overlay for a card's leading slot: a dark scrim over the slot's visual
         * with a white progress ring while uploading, or the red retry / error badge on failure.
         */
        private fun bindSlotOverlay(
            overlay: FrameLayout,
            progress: ProgressBar,
            errorBadge: FrameLayout,
            errorIcon: ImageView,
            cornerRadius: Float,
            errorColor: Int,
            uploading: Boolean,
            failed: Boolean,
            rejected: Boolean
        ) {
            val isError = failed || rejected
            overlay.visibility = if (uploading || isError) View.VISIBLE else View.GONE
            if (uploading || isError) {
                overlay.background = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    this.cornerRadius = cornerRadius
                    setColor(ColorUtils.setAlphaComponent(CometChatTheme.getColorBlack(ctx), 158)) // ~0.62
                }
            }
            progress.visibility = if (uploading) View.VISIBLE else View.GONE
            progress.indeterminateTintList =
                ColorStateList.valueOf(CometChatTheme.getColorWhite(ctx))
            errorBadge.visibility = if (isError) View.VISIBLE else View.GONE
            if (isError) bindErrorBadge(errorBadge, errorIcon, errorColor, failed)
        }

        /** The round red badge carrying the retry (failed) / error (rejected) glyph. */
        private fun bindErrorBadge(
            badge: FrameLayout,
            icon: ImageView,
            errorColor: Int,
            failed: Boolean
        ) {
            badge.background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(errorColor)
            }
            // The glyph drawables carry the badge's inner margin in their viewport, so the icon
            // view fills the badge rather than sitting in an inset box.
            icon.setImageResource(
                if (failed) R.drawable.cometchat_ic_upload_retry else R.drawable.cometchat_ic_upload_error
            )
            icon.setColorFilter(CometChatTheme.getColorWhite(ctx))
        }

        /** Done → open the preview; failed → tap anywhere on the tile to retry; rejected → tap to
         *  see why it was rejected (e.g. the size-limit reason). */
        private fun bindTileClick(
            card: View,
            tile: AttachmentUploadTile,
            done: Boolean,
            failed: Boolean,
            rejected: Boolean
        ) {
            card.isClickable = done || failed || rejected
            when {
                failed -> card.setOnClickListener { onRetry(tile) }
                rejected -> card.setOnClickListener { onRejected(tile) }
                done -> card.setOnClickListener { onClick(tile) }
                else -> card.setOnClickListener(null)
            }
        }

        private fun bindCornerBadge(tile: AttachmentUploadTile, uploading: Boolean) {
            cornerBadge.background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(CometChatTheme.getNeutralColor500(ctx))
                setStroke((2 * density).toInt(), CometChatTheme.getColorWhite(ctx))
            }
            ivBadgeIcon.setImageResource(R.drawable.cometchat_ic_close)
            ivBadgeIcon.setColorFilter(CometChatTheme.getColorWhite(ctx))
            cornerBadge.setOnClickListener { if (uploading) onCancel(tile) else onRemove(tile) }
        }

        /** Meta line under a file name: `EXT · size` (e.g. "PDF · 2.4 MB"), or just the size when
         *  the file has no extension. */
        private fun fileMetaLabel(tile: AttachmentUploadTile): String {
            val dot = tile.name.lastIndexOf('.')
            val ext = if (dot in 0 until tile.name.length - 1) tile.name.substring(dot + 1) else ""
            val size = formatSize(tile.size)
            return if (ext.isNotEmpty()) "${ext.uppercase()} · $size" else size
        }

        /** Formats a millisecond duration as `mm:ss` (e.g. 32_000 → "00:32"). */
        private fun formatDuration(durationMillis: Long): String {
            val totalSeconds = (durationMillis / 1000L).coerceAtLeast(0L)
            return String.format("%02d:%02d", totalSeconds / 60, totalSeconds % 60)
        }

        private fun formatSize(bytes: Long): String {
            if (bytes <= 0) return "0 B"
            val units = arrayOf("B", "KB", "MB", "GB")
            var size = bytes.toDouble()
            var i = 0
            while (size >= 1024 && i < units.size - 1) { size /= 1024; i++ }
            return if (size == size.toLong().toDouble()) "${size.toLong()} ${units[i]}"
            else String.format("%.1f %s", size, units[i])
        }
    }
}
