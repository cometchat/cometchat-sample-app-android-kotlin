package com.cometchat.uikit.kotlin.presentation.shared.messagebubble.audiobubble

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.StyleRes
import com.cometchat.chat.models.MediaMessage
import com.cometchat.uikit.core.utils.AudioBubblePlaybackState
import com.cometchat.uikit.core.utils.AudioBubbleStateManager
import com.cometchat.uikit.core.utils.PlayState
import com.cometchat.uikit.core.utils.WaveformUtils
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.shared.interfaces.OnClick
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
 * Audio message bubble with waveform bars, playback progress visualization,
 * true pause/resume, lazy download, and tap seeking.
 *
 * Uses [AudioBubbleStateManager] and [AudioBubblePlaybackState] from chatuikit-core
 * for per-bubble state management and single-playback enforcement.
 */
class CometChatAudioBubble @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.cometchatAudioBubbleStyle
) : MaterialCardView(context, attrs, defStyleAttr) {

    companion object {
        private const val TAG = "CometChatAudioBubble"
        private const val BAR_COUNT = 28
        private const val POLL_INTERVAL_MS = 50L
    }

    // Views
    private lateinit var parentLayout: LinearLayout
    private lateinit var buttonCardView: MaterialCardView
    private lateinit var playIconImageView: ImageView
    private lateinit var pauseIconImageView: ImageView
    private lateinit var progressBar: ProgressBar
    private lateinit var waveformView: AudioWaveformBarsView
    private lateinit var subtitleTextView: TextView

    // State
    private var audioUrl: String? = null
    private var messageId: Int = 0
    private var fileName: String = ""
    private var onClick: OnClick? = null
    private val handler = Handler(Looper.getMainLooper())
    private var pollingRunnable: Runnable? = null
    private var playbackState: AudioBubblePlaybackState? = null
    private var style: CometChatAudioBubbleStyle = CometChatAudioBubbleStyle()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var isDownloading = false

    @DrawableRes private var playIcon: Int = R.drawable.cometchat_play_icon
    @DrawableRes private var pauseIcon: Int = R.drawable.cometchat_ic_pause

    init {
        inflateAndInitializeView(attrs, defStyleAttr)
    }

    private fun inflateAndInitializeView(attrs: AttributeSet?, defStyleAttr: Int) {
        Utils.initMaterialCard(this)
        val view = View.inflate(context, R.layout.cometchat_audio_bubble, null)
        addView(view)

        parentLayout = view.findViewById(R.id.parent)
        buttonCardView = view.findViewById(R.id.iv_button)
        playIconImageView = view.findViewById(R.id.iv_play)
        pauseIconImageView = view.findViewById(R.id.iv_pause)
        progressBar = view.findViewById(R.id.progress_bar)
        waveformView = view.findViewById(R.id.waveformView)
        subtitleTextView = view.findViewById(R.id.tv_subtitle)

        playIconImageView.visibility = View.GONE
        progressBar.visibility = View.GONE
        pauseIconImageView.visibility = View.GONE
        subtitleTextView.visibility = View.GONE

        playIconImageView.setOnClickListener {
            if (onClick != null) onClick?.onClick() else onPlayTap()
        }
        pauseIconImageView.setOnClickListener { onPauseTap() }

        waveformView.setOnSeekListener { fraction ->
            val ps = playbackState ?: return@setOnSeekListener
            if (ps.playState == PlayState.PLAYING || ps.playState == PlayState.PAUSED) {
                val seekPos = (fraction * ps.totalDuration).toLong()
                ps.seekTo(seekPos)
                waveformView.setProgress(fraction)
                updateDurationText(seekPos, ps.totalDuration)
            }
        }

        parentLayout.setOnLongClickListener { v -> Utils.performAdapterClick(v); true }

        applyStyleAttributes(attrs, defStyleAttr)
        applyDefaultStyles()
    }

    private fun applyDefaultStyles() {
        val padding3 = resources.getDimensionPixelSize(R.dimen.cometchat_padding_3)
        setContentPadding(padding3, padding3, padding3, 0)
        layoutParams = LayoutParams(
            resources.getDimensionPixelSize(R.dimen.cometchat_240dp),
            LayoutParams.WRAP_CONTENT
        )
    }

    private fun applyStyleAttributes(attrs: AttributeSet?, defStyleAttr: Int) {
        var typedArray = context.theme.obtainStyledAttributes(attrs, R.styleable.CometChatAudioBubble, defStyleAttr, 0)
        val styleResId = typedArray.getResourceId(R.styleable.CometChatAudioBubble_cometchatAudioBubbleStyle, 0)
        typedArray.recycle()
        typedArray = context.theme.obtainStyledAttributes(attrs, R.styleable.CometChatAudioBubble, defStyleAttr, styleResId)
        style = CometChatAudioBubbleStyle.fromTypedArray(context, typedArray)
        applyStyle()
    }

    private fun applyStyle() {
        if (style.cornerRadius != 0f) radius = style.cornerRadius
        if (style.strokeWidth != 0f) strokeWidth = style.strokeWidth.toInt()
        if (style.strokeColor != 0) strokeColor = style.strokeColor
        style.backgroundDrawable?.let { super.setBackgroundDrawable(it) }
        if (style.playIconTint != 0) playIconImageView.imageTintList = ColorStateList.valueOf(style.playIconTint)
        if (style.pauseIconTint != 0) pauseIconImageView.imageTintList = ColorStateList.valueOf(style.pauseIconTint)
        if (style.buttonBackgroundColor != 0) buttonCardView.setCardBackgroundColor(ColorStateList.valueOf(style.buttonBackgroundColor))
        if (style.playIconTint != 0) progressBar.indeterminateDrawable.setColorFilter(style.playIconTint, PorterDuff.Mode.SRC_IN)
        if (style.subtitleTextColor != 0) subtitleTextView.setTextColor(style.subtitleTextColor)
        if (style.subtitleTextAppearance != 0) subtitleTextView.setTextAppearance(style.subtitleTextAppearance)

        // Waveform colors — derive from audioWaveColor if not explicitly set
        // audioWaveColor is correctly set per incoming/outgoing from theme:
        //   incoming: iconTintSecondary (grey) → we use playIconTint (primary) instead
        //   outgoing: white (set by outgoing theme style)
        val played = if (style.playedWaveColor != 0) style.playedWaveColor
            else if (style.audioWaveColor != 0 && style.audioWaveColor != CometChatTheme.getIconTintSecondary(context)) style.audioWaveColor
            else CometChatTheme.getPrimaryColor(context)
        val unplayed = if (style.unplayedWaveColor != 0) style.unplayedWaveColor
            else (played and 0x00FFFFFF) or 0x80000000.toInt() // 50% alpha of played color
        waveformView.setPlayedWaveColor(played)
        waveformView.setUnplayedWaveColor(unplayed)
    }

    // ========================================
    // Public API — Message Setting
    // ========================================

    /**
     * Sets the media message to display.
     * Extracts the audio URL and file size from the message attachment.
     * Generates a deterministic waveform based on the message ID.
     *
     * @param mediaMessage The MediaMessage containing audio attachment
     */
    fun setMessage(mediaMessage: MediaMessage) {
        // Release previous state if re-binding
        resetVisualState()
        messageId = mediaMessage.id.toInt()
        val attachment = mediaMessage.attachment
        if (attachment != null) {
            audioUrl = attachment.fileUrl
            fileName = attachment.fileName ?: "audio_${messageId}.m4a"
        } else if (mediaMessage.file != null) {
            audioUrl = null
            fileName = "audio_${messageId}.m4a"
        }
        // Generate deterministic waveform
        val key = audioUrl ?: messageId.toString()
        waveformView.setBarHeights(WaveformUtils.generateDeterministicWaveform(key, BAR_COUNT))
        playIconImageView.visibility = View.VISIBLE
        subtitleTextView.visibility = View.VISIBLE
        subtitleTextView.text = "00:00 / --:--"
    }

    /**
     * Sets the audio URL and corresponding subtitle text.
     * Generates a deterministic waveform based on the URL.
     *
     * @param audioUrl The URL of the audio file to be played.
     * @param subtitleText The subtitle text to be displayed (typically file size).
     */
    fun setAudioUrl(audioUrl: String?, subtitleText: String) {
        resetVisualState()
        this.audioUrl = audioUrl
        this.fileName = "audio_${audioUrl.hashCode()}.m4a"
        this.messageId = audioUrl.hashCode()
        if (!audioUrl.isNullOrEmpty()) {
            playIconImageView.isEnabled = true
            waveformView.setBarHeights(WaveformUtils.generateDeterministicWaveform(audioUrl, BAR_COUNT))
        } else {
            playIconImageView.isEnabled = false
            waveformView.setBarHeights(WaveformUtils.generatePlaceholder(BAR_COUNT))
        }
        playIconImageView.visibility = View.VISIBLE
        subtitleTextView.visibility = View.VISIBLE
        subtitleTextView.text = "00:00 / --:--"
    }

    // ========================================
    // Playback Control
    // ========================================

    private fun onPlayTap() {
        val url = audioUrl ?: return
        if (url.isEmpty()) return

        val ps = playbackState
        if (ps != null && ps.playState == PlayState.PAUSED) {
            // Resume from pause
            AudioBubbleStateManager.pauseAllExcept(messageId)
            ps.play()
            showPauseState()
            startPolling()
            return
        }

        // Check cache first
        val cacheDir = context.externalCacheDir ?: context.cacheDir
        val cachedFile = File(cacheDir, "${messageId}_${fileName}")
        if (cachedFile.exists() && cachedFile.length() > 0) {
            initAndPlay(cachedFile.absolutePath)
            return
        }

        // Download then play
        if (isDownloading) return
        isDownloading = true
        showLoadingState()

        scope.launch {
            val localPath = downloadFile(url, cachedFile)
            isDownloading = false
            if (localPath != null) {
                initAndPlay(localPath)
            } else {
                showIdleState()
            }
        }
    }

    private fun initAndPlay(filePath: String) {
        showLoadingState()
        AudioBubbleStateManager.pauseAllExcept(messageId)
        val ps = AudioBubbleStateManager.getOrCreate(messageId, audioUrl, filePath)
        playbackState = ps
        ps.initFromFile(filePath) {
            handler.post {
                val dur = ps.totalDuration
                subtitleTextView.text = "00:00 / ${formatTime(dur)}"
                ps.play()
                showPauseState()
                startPolling()
            }
        }
    }

    private fun onPauseTap() {
        playbackState?.pause()
        stopPolling()
        showPlayState()
    }

    /**
     * Starts the audio playback. Downloads the file on first play,
     * then plays from cache on subsequent taps.
     */
    fun startPlaying() { onPlayTap() }

    /**
     * Pauses the audio playback, preserving the current position.
     */
    fun stopPlaying() { onPauseTap() }

    /**
     * Checks if audio is currently playing.
     *
     * @return true if audio is playing, false otherwise
     */
    fun isPlaying(): Boolean = playbackState?.playState == PlayState.PLAYING

    // ========================================
    // Polling
    // ========================================

    private fun startPolling() {
        stopPolling()
        pollingRunnable = object : Runnable {
            override fun run() {
                val ps = playbackState ?: return
                if (ps.playState == PlayState.PLAYING) {
                    ps.updatePosition()
                    val pos = ps.currentPosition
                    val dur = ps.totalDuration
                    val progress = if (dur > 0) (pos.toFloat() / dur).coerceIn(0f, 1f) else 0f
                    waveformView.setProgress(progress)
                    updateDurationText(pos, dur)
                    handler.postDelayed(this, POLL_INTERVAL_MS)
                } else if (ps.playState == PlayState.STOPPED) {
                    // Playback completed
                    waveformView.setProgress(0f)
                    subtitleTextView.text = "00:00 / ${formatTime(dur = ps.totalDuration)}"
                    showPlayState()
                }
            }
        }
        handler.post(pollingRunnable!!)
    }

    private fun stopPolling() {
        pollingRunnable?.let { handler.removeCallbacks(it) }
        pollingRunnable = null
    }

    // ========================================
    // UI State Helpers
    // ========================================

    private fun showLoadingState() {
        playIconImageView.visibility = View.GONE
        pauseIconImageView.visibility = View.GONE
        progressBar.visibility = View.VISIBLE
    }

    private fun showPauseState() {
        playIconImageView.visibility = View.GONE
        progressBar.visibility = View.GONE
        pauseIconImageView.visibility = View.VISIBLE
    }

    private fun showPlayState() {
        pauseIconImageView.visibility = View.GONE
        progressBar.visibility = View.GONE
        playIconImageView.visibility = View.VISIBLE
    }

    private fun showIdleState() {
        showPlayState()
        waveformView.setProgress(0f)
        subtitleTextView.text = "00:00 / --:--"
    }

    private fun resetVisualState() {
        stopPolling()
        playbackState = null
        waveformView.setProgress(0f)
        showPlayState()
    }

    private fun updateDurationText(posMs: Long, durMs: Long) {
        subtitleTextView.text = "${formatTime(posMs)} / ${formatTime(durMs)}"
    }

    private fun formatTime(dur: Long): String {
        if (dur <= 0) return "00:00"
        val totalSeconds = dur / 1000
        val minutes = (totalSeconds / 60).toInt()
        val seconds = (totalSeconds % 60).toInt()
        return String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }

    // ========================================
    // Download
    // ========================================

    private suspend fun downloadFile(url: String, targetFile: File): String? = withContext(Dispatchers.IO) {
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
                targetFile.outputStream().use { output -> input.copyTo(output) }
            }
            connection.disconnect()
            if (targetFile.exists() && targetFile.length() > 0) targetFile.absolutePath else { targetFile.delete(); null }
        } catch (e: Exception) {
            Log.e(TAG, "Download failed: ${e.message}")
            targetFile.delete()
            null
        }
    }

    // ========================================
    // Lifecycle
    // ========================================

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopPolling()
    }

    // ========================================
    // Public Style Methods
    // ========================================

    /**
     * Sets the style from a style object.
     */
    fun setStyle(style: CometChatAudioBubbleStyle) { this.style = style; applyStyle() }

    /**
     * Sets the style from a style resource.
     */
    fun setStyle(@StyleRes styleRes: Int) {
        if (styleRes != 0) {
            val ta = context.theme.obtainStyledAttributes(styleRes, R.styleable.CometChatAudioBubble)
            setStyle(CometChatAudioBubbleStyle.fromTypedArray(context, ta))
        }
    }

    /** Sets the tint color for the play icon. */
    fun setPlayIconTint(@ColorInt color: Int) { style = style.copy(playIconTint = color); playIconImageView.imageTintList = ColorStateList.valueOf(color) }
    /** Sets the tint color for the pause icon. */
    fun setPauseIconTint(@ColorInt color: Int) { style = style.copy(pauseIconTint = color); pauseIconImageView.imageTintList = ColorStateList.valueOf(color) }
    /** Sets the background color of the play/pause button. */
    fun setButtonTint(@ColorInt color: Int) { style = style.copy(buttonBackgroundColor = color); buttonCardView.setCardBackgroundColor(ColorStateList.valueOf(color)) }
    /** Sets the color for the audio wave (legacy, sets unplayed color). */
    fun setAudioWaveColor(@ColorInt color: Int) { style = style.copy(audioWaveColor = color); waveformView.setUnplayedWaveColor(color) }
    /** Sets the color for played (progress) waveform bars. */
    fun setPlayedWaveColor(@ColorInt color: Int) { style = style.copy(playedWaveColor = color); waveformView.setPlayedWaveColor(color) }
    /** Sets the color for unplayed waveform bars. */
    fun setUnplayedWaveColor(@ColorInt color: Int) { style = style.copy(unplayedWaveColor = color); waveformView.setUnplayedWaveColor(color) }
    /** Sets the color of the subtitle text. */
    fun setSubtitleTextColor(@ColorInt color: Int) { if (color != 0) { style = style.copy(subtitleTextColor = color); subtitleTextView.setTextColor(color) } }
    /** Sets the text appearance of the subtitle text. */
    fun setSubtitleTextAppearance(@StyleRes appearance: Int) { if (appearance != 0) { style = style.copy(subtitleTextAppearance = appearance); subtitleTextView.setTextAppearance(appearance) } }
    /** Sets the play icon drawable resource. */
    fun setPlayIcon(@DrawableRes icon: Int) { playIcon = icon; playIconImageView.setImageResource(icon) }
    /** Sets the pause icon drawable resource. */
    fun setPauseIcon(@DrawableRes icon: Int) { pauseIcon = icon; pauseIconImageView.setImageResource(icon) }
    /**
     * Sets an OnClick listener for the AudioBubble view.
     * It will be triggered when user clicks on the play button.
     *
     * @param onClick The OnClick listener to be set
     */
    fun setOnClick(onClick: OnClick?) { this.onClick = onClick }

    // Getters
    /** Gets the play icon tint color. */
    fun getPlayIconTint(): Int = style.playIconTint
    /** Gets the pause icon tint color. */
    fun getPauseIconTint(): Int = style.pauseIconTint
    /** Gets the button background color. */
    fun getButtonTint(): Int = style.buttonBackgroundColor
    /** Gets the audio wave color (legacy). */
    fun getAudioWaveColor(): Int = style.audioWaveColor
    /** Gets the played waveform bar color. */
    fun getPlayedWaveColor(): Int = style.playedWaveColor
    /** Gets the unplayed waveform bar color. */
    fun getUnplayedWaveColor(): Int = style.unplayedWaveColor
    /** Gets the subtitle text color. */
    fun getSubtitleTextColor(): Int = style.subtitleTextColor
    /** Gets the subtitle text appearance resource. */
    fun getSubtitleTextAppearance(): Int = style.subtitleTextAppearance
    /** Gets the play icon drawable resource. */
    fun getPlayIcon(): Int = playIcon
    /** Gets the pause icon drawable resource. */
    fun getPauseIcon(): Int = pauseIcon
    /** Gets the OnClick listener. */
    fun getOnClick(): OnClick? = onClick
    /** Gets the parent layout view. */
    fun getView(): LinearLayout = parentLayout
    /** Gets the subtitle TextView. */
    fun getSubtitle(): TextView = subtitleTextView
    /** Gets the play icon ImageView. */
    fun getPlayIconImageView(): View = playIconImageView
    /** Gets the pause icon ImageView. */
    fun getPauseIconImageView(): View = pauseIconImageView
}
