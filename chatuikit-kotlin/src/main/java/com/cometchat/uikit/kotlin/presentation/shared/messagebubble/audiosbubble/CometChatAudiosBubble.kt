package com.cometchat.uikit.kotlin.presentation.shared.messagebubble.audiosbubble

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.media.MediaMetadataRetriever
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.util.LruCache
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.graphics.ColorUtils
import com.cometchat.chat.models.Attachment
import com.cometchat.chat.models.MediaMessage
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.utils.AudioBubblePlaybackState
import com.cometchat.uikit.core.utils.AudioBubbleStateManager
import com.cometchat.uikit.core.utils.PlayState
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.presentation.shared.mediaviewer.CometChatImageViewerActivity
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.audiobubble.CometChatAudioBubble
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.audiobubble.CometChatAudioBubbleStyle
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.multiattachment.MultiAttachmentUtils
import com.cometchat.uikit.kotlin.shared.formatters.CometChatTextFormatter
import com.cometchat.uikit.kotlin.shared.resources.utils.Utils
import com.cometchat.uikit.kotlin.theme.CometChatTheme
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

/**
 * ENG-36737 — per-type multi-attachment bubble for picker-selected audio (mp3 etc.). Renders every
 * audio attachment as a rounded player card: play/pause circle, file name, flat seek bar with
 * elapsed/total time, and a trailing download icon. More than 3 audios collapse behind a
 * "Show N more" / "Show less" toggle, and the caption rides below the cards on the last message of
 * a batch. Cards are a translucent overlay (white on the tinted outgoing bubble, subtle dark on
 * incoming), matching CometChatFilesBubble.
 *
 * Mic-recorded voice notes keep the existing waveform UI: when the bound message
 * [MultiAttachmentUtils.isVoiceNote], this view just embeds an unchanged [CometChatAudioBubble].
 * (Voice notes are always sent alone, so the embed never mixes with player cards.) The embed —
 * rather than a separate content-view class — keeps one view type per factory key so RecyclerView
 * recycling stays valid whichever audio flavor arrives.
 *
 * Playback state lives in [AudioBubbleStateManager] (download-to-cache on first play, single
 * playback enforcement, survives recycling), same as the single audio bubble.
 */
class CometChatAudiosBubble @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var style: CometChatAudioBubbleStyle = CometChatAudioBubbleStyle()
    private var isOutgoing: Boolean = false
    private var statusInfoVisible = true
    private var expanded: Boolean = false
    private var audios: List<Attachment> = emptyList()
    private var caption: String? = null
    private var isEdited: Boolean = false

    // The message currently bound, so a rebind of the *same* message (e.g. the search-highlight
    // fade ending in notifyItemChanged) preserves the expanded toggle instead of collapsing it.
    // Reset only when the recycled view binds a different message — the imperative equivalent of
    // the Compose bubble's `remember(message.id) { mutableStateOf(false) }`. (ENG-37143)
    private var boundMessageId: Long = -1L

    /** Kept for the caption's formatter pass (mentions are resolved against the message). */
    private var captionMessage: MediaMessage? = null
    private var textFormatters: List<CometChatTextFormatter> = emptyList()
    private var messageAlignment = UIKitConstants.MessageBubbleAlignment.LEFT

    private val holders = mutableListOf<CardHolder>()
    private val handler = Handler(Looper.getMainLooper())
    private var ticker: Runnable? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val bubbleWidthPx = resources.getDimensionPixelSize(R.dimen.cometchat_240dp)

    init {
        orientation = VERTICAL
        layoutParams = LayoutParams(bubbleWidthPx, LayoutParams.WRAP_CONTENT)
    }

    fun setStyle(style: CometChatAudioBubbleStyle) {
        this.style = style
    }

    fun setOutgoing(outgoing: Boolean) {
        isOutgoing = outgoing
    }

    /**
     * Tells the bubble whether the message's timestamp/receipt row is visible below it. Voice
     * notes forward to the embedded [CometChatAudioBubble]; the player-card layout compensates
     * with bottom padding itself (0 while the row provides the gap, 4dp when it is hidden so the
     * cards aren't flush with the bubble edge).
     */
    fun setStatusInfoVisible(visible: Boolean) {
        statusInfoVisible = visible
        val voiceEmbed = getChildAt(0) as? CometChatAudioBubble
        if (voiceEmbed != null) {
            voiceEmbed.setStatusInfoVisible(visible)
        } else {
            applyRootPadding()
        }
    }

    private fun applyRootPadding() {
        setPadding(dp(5), dp(5), dp(5), if (statusInfoVisible) 0 else dp(5))
    }

    /**
     * Formatters applied to the caption, exactly as the text bubble applies them to its text (so a
     * mention resolves to a display name instead of a raw `<@uid:...>` token). Call before
     * [setMessage] — the caption is rendered there.
     */
    fun setTextFormatters(
        formatters: List<CometChatTextFormatter>?,
        alignment: UIKitConstants.MessageBubbleAlignment
    ) {
        textFormatters = formatters ?: emptyList()
        messageAlignment = alignment
    }

    fun setMessage(message: MediaMessage) {
        stopTicker()
        holders.clear()
        removeAllViews()
        // A recycled bubble may have been padded for a hidden timestamp row — restore the default;
        // the adapter re-hides after bind.
        statusInfoVisible = true

        if (MultiAttachmentUtils.isVoiceNote(message)) {
            // Voice notes keep the existing waveform bubble untouched — no wrapper chrome.
            setPadding(0, 0, 0, 0)
            addView(CometChatAudioBubble(context).apply {
                setStyle(style)
                setTextFormatters(textFormatters, messageAlignment)
                setMessage(message)
            })
            return
        }

        applyRootPadding()
        // Every attachment stays in the list — a kind-mismatched one (server-sent mixed payload)
        // renders as an inert broken card rather than being dropped.
        audios = MultiAttachmentUtils.resolveAttachments(message)
        captionMessage = message
        caption = message.caption
        isEdited = message.editedAt > 0
        // Keep the toggle open across a rebind of the same message; collapse only when this
        // recycled view is reused for a different message. (ENG-37143)
        if (message.id != boundMessageId) {
            expanded = false
            boundMessageId = message.id
        }
        render()
    }

    private fun render() {
        // Keep the voice-note embed out of here — render() only lays out player cards.
        stopTicker()
        holders.clear()
        removeAllViews()
        if (audios.isEmpty()) return

        val canCollapse = audios.size > COLLAPSED_COUNT
        val visibleCount = if (!canCollapse || expanded) audios.size else COLLAPSED_COUNT

        for (i in 0 until visibleCount) {
            val isAudio = MultiAttachmentUtils.kindOf(audios[i]) == MultiAttachmentUtils.KIND_AUDIO
            addView(if (isAudio) buildAudioCard(audios[i], i) else buildBrokenCard(audios[i], i))
        }
        if (canCollapse) {
            addView(buildToggle(audios.size - visibleCount))
        }
        caption?.takeIf { it.isNotEmpty() }?.let {
            addView(buildCaption(it))
            if (isEdited) addView(buildEditedLabel())
        }

        // Resume the progress ticker if a card in this bubble is already playing (re-bind while
        // audio keeps playing across RecyclerView recycling).
        if (holders.any { it.state?.playState == PlayState.PLAYING }) startTicker()
    }

    // ========================================
    // Card construction
    // ========================================

    private fun buildAudioCard(attachment: Attachment, index: Int): View {
        val card = MaterialCardView(context)
        Utils.initMaterialCard(card)
        card.radius = dp(12).toFloat()
        card.cardElevation = 0f
        card.strokeWidth = 0
        // A single audio sits directly on the bubble — the card overlay only separates multiples.
        card.setCardBackgroundColor(if (audios.size == 1) CometChatTheme.getColorTransparent(context) else cardColor())
        card.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
            topMargin = if (index == 0) 0 else dp(4)
        }
        card.setOnLongClickListener { v -> Utils.performAdapterClick(v); true }

        val row = View.inflate(context, R.layout.cometchat_audios_bubble_item, null)
        val holder = CardHolder(
            attachment = attachment,
            playButton = row.findViewById(R.id.audio_play_button),
            playIcon = row.findViewById(R.id.iv_play),
            pauseIcon = row.findViewById(R.id.iv_pause),
            buttonProgress = row.findViewById(R.id.pb_button),
            name = row.findViewById(R.id.tv_audio_name),
            seekBar = row.findViewById(R.id.sb_audio_seek),
            time = row.findViewById(R.id.tv_audio_time),
            downloadContainer = row.findViewById(R.id.download_container),
            downloadIcon = row.findViewById(R.id.iv_download),
            downloadProgress = row.findViewById(R.id.pb_download)
        )
        holders.add(holder)
        bindCard(holder)
        card.addView(row)
        return card
    }

    private fun bindCard(holder: CardHolder) {
        val primary = CometChatTheme.getPrimaryColor(context)
        val white = CometChatTheme.getColorWhite(context)
        val buttonBg = if (isOutgoing) white else primary
        val glyphTint = if (isOutgoing) primary else white
        val playedColor = if (isOutgoing) white else primary
        val trackColor = if (isOutgoing) ColorUtils.setAlphaComponent(white, 77) /* ~0.3 */ else CometChatTheme.getNeutralColor400(context)

        holder.playButton.setCardBackgroundColor(buttonBg)
        holder.playIcon.imageTintList = ColorStateList.valueOf(glyphTint)
        holder.pauseIcon.imageTintList = ColorStateList.valueOf(glyphTint)
        holder.buttonProgress.indeterminateTintList = ColorStateList.valueOf(glyphTint)

        holder.name.text = holder.attachment.fileName ?: DEFAULT_FILE_NAME
        holder.name.setTextColor(titleColor())
        holder.name.setTypeface(holder.name.typeface, Typeface.BOLD)

        holder.time.setTextColor(subtitleColor())
        holder.seekBar.progressTintList = ColorStateList.valueOf(playedColor)
        holder.seekBar.progressBackgroundTintList = ColorStateList.valueOf(trackColor)
        holder.seekBar.max = SEEK_MAX
        holder.seekBar.progress = 0
        holder.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (!fromUser) return
                val ps = holder.state ?: return
                if (ps.playState == PlayState.PLAYING || ps.playState == PlayState.PAUSED) {
                    val posMs = (progress.toFloat() / SEEK_MAX * ps.totalDuration).toLong()
                    ps.seekTo(posMs)
                    holder.updateTime(posMs, ps.totalDuration)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) = Unit
            override fun onStopTrackingTouch(seekBar: SeekBar) = Unit
        })

        holder.downloadIcon.imageTintList = ColorStateList.valueOf(accentColor())
        holder.downloadProgress.indeterminateTintList = ColorStateList.valueOf(accentColor())
        // Always show the download icon (unless there's no file to fetch).
        holder.downloadContainer.visibility =
            if (holder.attachment.fileUrl.isNullOrEmpty()) View.GONE else View.VISIBLE
        holder.downloadIcon.setOnClickListener {
            val url = holder.attachment.fileUrl
            if (!url.isNullOrEmpty()) {
                Utils.downloadFile(context, url, holder.attachment.fileName ?: System.currentTimeMillis().toString())
            }
        }

        holder.playIcon.setOnClickListener { onPlayTap(holder) }
        holder.pauseIcon.setOnClickListener { onPauseTap(holder) }

        // Restore live state if this attachment is already playing/paused (RecyclerView re-bind),
        // without creating playback state for idle cards.
        holder.state = AudioBubbleStateManager.peek(holder.stateId)
        holder.refreshFromState()
        if (holder.state == null) {
            holder.updateTime(0L, durationCache.get(holder.attachment.fileUrl) ?: 0L)
            preloadDuration(holder)
        }
    }

    /**
     * Card for a kind-mismatched attachment inside an audio-type message (server-sent mixed
     * payload — the UIKit composer never produces one). File-card-like row with nothing to play:
     * just the unsupported-file glyph and the file name — no seek track, no size/type meta.
     * Tapping the card opens the viewer's "No preview available" page (with its Download button);
     * the trailing download icon stays live too. Not registered in [holders] — the playback
     * ticker must never touch it.
     */
    private fun buildBrokenCard(attachment: Attachment, index: Int): View {
        val card = MaterialCardView(context)
        Utils.initMaterialCard(card)
        card.radius = dp(12).toFloat()
        card.cardElevation = 0f
        card.strokeWidth = 0
        // A single attachment sits directly on the bubble — the card overlay only separates multiples.
        card.setCardBackgroundColor(if (audios.size == 1) CometChatTheme.getColorTransparent(context) else cardColor())
        card.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
            topMargin = if (index == 0) 0 else dp(4)
        }
        card.setOnClickListener {
            context.startActivity(
                CometChatImageViewerActivity.createIntent(
                    context,
                    listOf(attachment.fileUrl.orEmpty()),
                    listOf(attachment.fileMimeType.orEmpty()),
                    listOf(attachment.fileName.orEmpty())
                )
            )
        }
        card.setOnLongClickListener { v -> Utils.performAdapterClick(v); true }

        val row = View.inflate(context, R.layout.cometchat_audios_bubble_item, null)
        row.findViewById<MaterialCardView>(R.id.audio_play_button).apply {
            // The play-button slot is a 16dp-radius circle card — zero the radius so the file
            // glyph isn't clipped to that circle.
            radius = 0f
            setCardBackgroundColor(CometChatTheme.getColorTransparent(context))
        }
        row.findViewById<ImageView>(R.id.iv_play).apply {
            imageTintList = null
            scaleType = ImageView.ScaleType.FIT_CENTER
            setPadding(dp(3), 0, dp(3), 0)
            setImageResource(R.drawable.cometchat_unsupported_file_icon)
        }
        row.findViewById<ImageView>(R.id.iv_pause).visibility = View.GONE
        row.findViewById<ProgressBar>(R.id.pb_button).visibility = View.GONE
        row.findViewById<View>(R.id.sb_audio_seek).visibility = View.GONE

        row.findViewById<TextView>(R.id.tv_audio_name).apply {
            text = attachment.fileName ?: DEFAULT_FILE_NAME
            setTextColor(titleColor())
            setTypeface(typeface, Typeface.BOLD)
        }
        row.findViewById<TextView>(R.id.tv_audio_time).visibility = View.GONE

        row.findViewById<View>(R.id.download_container).visibility =
            if (attachment.fileUrl.isNullOrEmpty()) View.GONE else View.VISIBLE
        row.findViewById<ImageView>(R.id.iv_download).apply {
            imageTintList = ColorStateList.valueOf(accentColor())
            setOnClickListener {
                val url = attachment.fileUrl
                if (!url.isNullOrEmpty()) {
                    Utils.downloadFile(context, url, attachment.fileName ?: System.currentTimeMillis().toString())
                }
            }
        }

        card.addView(row)
        return card
    }

    private fun buildToggle(hidden: Int): View {
        val card = MaterialCardView(context)
        Utils.initMaterialCard(card)
        card.radius = dp(12).toFloat()
        card.cardElevation = 0f
        card.strokeWidth = 0
        card.setCardBackgroundColor(cardColor())
        card.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
            topMargin = dp(4)
        }
        card.setOnClickListener {
            expanded = !expanded
            render()
        }
        val row = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER
            setPadding(dp(10), dp(10), dp(10), dp(10))
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        }
        val chevron = ImageView(context).apply {
            setImageResource(R.drawable.cometchat_ic_arrow_down)
            imageTintList = ColorStateList.valueOf(accentColor())
            rotation = if (expanded) 180f else 0f
            layoutParams = LinearLayout.LayoutParams(dp(18), dp(18)).apply { marginEnd = dp(4) }
        }
        val label = TextView(context).apply {
            text = if (expanded) context.getString(R.string.cometchat_show_less)
            else context.getString(R.string.cometchat_show_n_more, hidden)
            gravity = android.view.Gravity.CENTER
            setTextColor(accentColor())
            textSize = 15f
            setTypeface(typeface, Typeface.BOLD)
        }
        row.addView(chevron)
        row.addView(label)
        card.addView(row)
        return card
    }

    private fun buildEditedLabel(): View = MultiAttachmentUtils.createEditedLabel(context).apply {
        visibility = View.VISIBLE
        setTextColor(captionColor())
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
    }

    private fun buildCaption(caption: String): View = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        // 0 bottom — the timestamp row below the bubble provides the gap (same rule as the
        // root padding); a bottom inset here doubles up and bloats captioned bubbles.
        setPadding(dp(8), dp(6), dp(8), 0)
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        // Captions travel as markdown — render the same block-level views as the text bubble.
        MultiAttachmentUtils.renderCaptionInto(
            this, caption, captionMessage, textFormatters, messageAlignment,
            captionTextColor = captionColor()
        )
    }

    private fun cardColor(): Int =
        if (isOutgoing) ColorUtils.setAlphaComponent(CometChatTheme.getColorWhite(context), 41) // ~0.16
        else ColorUtils.setAlphaComponent(CometChatTheme.getColorBlack(context), 10) // ~0.04

    private fun titleColor(): Int =
        if (isOutgoing) CometChatTheme.getColorWhite(context) else CometChatTheme.getTextColorPrimary(context)

    private fun subtitleColor(): Int =
        if (style.subtitleTextColor != 0) style.subtitleTextColor
        else if (isOutgoing) ColorUtils.setAlphaComponent(CometChatTheme.getColorWhite(context), 204) // ~0.8
        else CometChatTheme.getTextColorSecondary(context)

    // The caption is the user's message text, so it matches the image/video bubble caption
    // (full white on the tinted outgoing bubble, primary on incoming) rather than the dimmer
    // subtitle used for the elapsed/total time.
    private fun captionColor(): Int =
        if (isOutgoing) CometChatTheme.getColorWhite(context)
        else CometChatTheme.getTextColorPrimary(context)

    // Download icon + "Show N more" toggle use the primary accent on incoming; white on the tinted
    // outgoing bubble so they stay visible.
    private fun accentColor(): Int =
        if (isOutgoing) CometChatTheme.getColorWhite(context) else CometChatTheme.getPrimaryColor(context)

    // ========================================
    // Playback
    // ========================================

    private fun onPlayTap(holder: CardHolder) {
        val url = holder.attachment.fileUrl ?: return
        if (url.isEmpty()) return

        val ps = holder.state
        if (ps != null && ps.playState == PlayState.PAUSED) {
            AudioBubbleStateManager.pauseAllExcept(holder.stateId)
            ps.play()
            refreshAllCards()
            startTicker()
            return
        }

        val cacheDir = context.externalCacheDir ?: context.cacheDir
        val cachedFile = File(cacheDir, "${holder.stateId}_${holder.attachment.fileName ?: "audio.m4a"}")
        if (cachedFile.exists() && cachedFile.length() > 0) {
            initAndPlay(holder, cachedFile.absolutePath)
            return
        }

        if (holder.isDownloading) return
        holder.isDownloading = true
        holder.showDownloadingState()
        scope.launch {
            val localPath = downloadFile(url, cachedFile)
            holder.isDownloading = false
            if (localPath != null) {
                initAndPlay(holder, localPath)
            } else {
                holder.refreshFromState()
                holder.updateTime(0L, durationCache.get(holder.attachment.fileUrl) ?: 0L)
            }
        }
    }

    private fun initAndPlay(holder: CardHolder, filePath: String) {
        holder.showLoadingState()
        AudioBubbleStateManager.pauseAllExcept(holder.stateId)
        val ps = AudioBubbleStateManager.getOrCreate(holder.stateId, holder.attachment.fileUrl, filePath)
        holder.state = ps
        ps.initFromFile(filePath, onError = {
            // Evict the file — a truncated cache entry would otherwise fail on every replay.
            File(filePath).delete()
            handler.post {
                holder.state = null
                holder.refreshFromState()
                holder.updateTime(0L, durationCache.get(holder.attachment.fileUrl) ?: 0L)
            }
        }) {
            handler.post {
                ps.play()
                refreshAllCards()
                startTicker()
            }
        }
    }

    private fun onPauseTap(holder: CardHolder) {
        holder.state?.pause()
        holder.refreshFromState()
    }

    /** Re-syncs every card's button/progress with its playback state (e.g. after pauseAllExcept). */
    private fun refreshAllCards() {
        holders.forEach { it.refreshFromState() }
    }

    private fun startTicker() {
        stopTicker()
        ticker = object : Runnable {
            override fun run() {
                var anyPlaying = false
                holders.forEach { holder ->
                    val ps = holder.state ?: return@forEach
                    when (ps.playState) {
                        PlayState.PLAYING -> {
                            anyPlaying = true
                            ps.updatePosition()
                            holder.updateProgress(ps)
                        }
                        PlayState.STOPPED -> {
                            // Completed — reset to start.
                            if (holder.seekBar.progress != 0) {
                                holder.seekBar.progress = 0
                                holder.updateTime(0L, ps.totalDuration)
                            }
                            holder.refreshFromState()
                        }
                        else -> holder.refreshFromState()
                    }
                }
                if (anyPlaying) handler.postDelayed(this, POLL_INTERVAL_MS)
            }
        }
        handler.post(ticker!!)
    }

    private fun stopTicker() {
        ticker?.let { handler.removeCallbacks(it) }
        ticker = null
    }

    /**
     * Pre-fetches the audio duration from the remote URL so the card can show "00:00/00:32" before
     * the first play (same on-the-fly approach as the videos bubble; cached per URL).
     */
    private fun preloadDuration(holder: CardHolder) {
        val url = holder.attachment.fileUrl ?: return
        if (url.isEmpty() || durationCache.get(url) != null) return
        Thread {
            val durationMs = try {
                val retriever = MediaMetadataRetriever()
                try {
                    retriever.setDataSource(url, HashMap())
                    retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
                } finally {
                    runCatching { retriever.release() }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Duration preload failed: ${e.message}")
                0L
            }
            if (durationMs > 0) {
                durationCache.put(url, durationMs)
                handler.post {
                    // Only touch idle cards — a playing card's time is owned by the ticker.
                    if (holder.state == null && holders.contains(holder)) holder.updateTime(0L, durationMs)
                }
            }
        }.start()
    }

    private suspend fun downloadFile(url: String, targetFile: File): String? = withContext(Dispatchers.IO) {
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

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopTicker()
    }

    // ========================================
    // Per-card view holder
    // ========================================

    private inner class CardHolder(
        val attachment: Attachment,
        val playButton: MaterialCardView,
        val playIcon: ImageView,
        val pauseIcon: ImageView,
        val buttonProgress: ProgressBar,
        val name: TextView,
        val seekBar: SeekBar,
        val time: TextView,
        val downloadContainer: View,
        val downloadIcon: ImageView,
        val downloadProgress: ProgressBar
    ) {
        // Keyed by URL so playback state survives recycling and message re-fetches.
        val stateId: Int = (attachment.fileUrl ?: attachment.fileName ?: "").hashCode()
        var state: AudioBubblePlaybackState? = null
        var isDownloading = false

        fun refreshFromState() {
            val ps = state
            downloadProgress.visibility = View.GONE
            downloadIcon.visibility = View.VISIBLE
            when (ps?.playState) {
                PlayState.PLAYING -> {
                    playIcon.visibility = View.GONE
                    buttonProgress.visibility = View.GONE
                    pauseIcon.visibility = View.VISIBLE
                    updateProgress(ps)
                }
                PlayState.PAUSED -> {
                    pauseIcon.visibility = View.GONE
                    buttonProgress.visibility = View.GONE
                    playIcon.visibility = View.VISIBLE
                    updateProgress(ps)
                }
                else -> {
                    pauseIcon.visibility = View.GONE
                    buttonProgress.visibility = View.GONE
                    playIcon.visibility = View.VISIBLE
                    if (ps != null) updateTime(0L, ps.totalDuration)
                }
            }
        }

        fun showLoadingState() {
            playIcon.visibility = View.GONE
            pauseIcon.visibility = View.GONE
            buttonProgress.visibility = View.VISIBLE
        }

        /** Inline download-for-playback: spinner replaces the download icon, time shows the label. */
        fun showDownloadingState() {
            showLoadingState()
            downloadIcon.visibility = View.GONE
            downloadProgress.visibility = View.VISIBLE
            time.text = "${context.getString(R.string.cometchat_downloading)}…"
        }

        fun updateProgress(ps: AudioBubblePlaybackState) {
            seekBar.progress = (ps.progress * SEEK_MAX).toInt()
            updateTime(ps.currentPosition, ps.totalDuration)
        }

        fun updateTime(posMs: Long, durMs: Long) {
            time.text = "${formatTime(posMs)}/${formatTime(durMs)}"
        }
    }

    private fun formatTime(ms: Long): String {
        if (ms <= 0) return "00:00"
        val totalSeconds = ms / 1000
        return String.format(Locale.US, "%02d:%02d", totalSeconds / 60, totalSeconds % 60)
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    companion object {
        private const val TAG = "CometChatAudiosBubble"
        private const val COLLAPSED_COUNT = 3
        private const val POLL_INTERVAL_MS = 200L
        private const val SEEK_MAX = 1000
        private const val DEFAULT_FILE_NAME = "Audio"
        /** Max cached remote-URL durations kept (cheap longs, but bounded to avoid unbounded growth). */
        private const val DURATION_CACHE_MAX = 256
        /**
         * Remote-URL duration cache shared across bubbles (like the videos bubble's meta cache).
         * LRU-bounded and internally synchronized (preload runs on a background thread).
         */
        private val durationCache = LruCache<String, Long>(DURATION_CACHE_MAX)
    }
}
