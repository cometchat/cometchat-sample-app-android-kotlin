package com.cometchat.uikit.core.utils

import android.media.AudioAttributes
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.util.Log

/**
 * Per-bubble playback state wrapping a [MediaPlayer] instance.
 * Supports true pause/resume, seeking, and position polling.
 */
class AudioBubblePlaybackState(
    val id: Int,
    val audioUrl: String?,
    var localPath: String?
) {
    companion object {
        private const val TAG = "AudioBubblePlayback"
    }

    private var mediaPlayer: MediaPlayer? = null
    private var isPrepared: Boolean = false
    var playState: PlayState = PlayState.INIT
        private set
    var totalDuration: Long = 0L
        private set
    var currentPosition: Long = 0L
        private set
    var isInitializing: Boolean = false
        private set

    val progress: Float
        get() = if (totalDuration > 0) (currentPosition.toFloat() / totalDuration).coerceIn(0f, 1f) else 0f

    fun initFromFile(filePath: String, onError: () -> Unit = {}, onReady: () -> Unit = {}) {
        if (mediaPlayer != null && isPrepared) { onReady(); return }
        // Release any existing player first
        try { mediaPlayer?.release() } catch (_: Exception) {}
        mediaPlayer = null; isPrepared = false
        isInitializing = true
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(filePath)
                setAudioAttributes(AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
                setOnPreparedListener { mp ->
                    // MediaPlayer reports -1/0 for containers without a duration header (VBR MP3
                    // missing Xing, raw AAC/ADTS, AMR, some Opus voice notes) — fall back to the
                    // retriever, which parses frames instead of trusting the header.
                    totalDuration = mp.duration.toLong().takeIf { it > 0 }
                        ?: extractDurationMs(filePath)
                    isPrepared = true
                    isInitializing = false
                    playState = PlayState.STOPPED
                    onReady()
                }
                setOnCompletionListener {
                    onPlaybackCompleted()
                }
                setOnErrorListener { _, what, extra ->
                    Log.e(TAG, "[$id] error: what=$what, extra=$extra")
                    isInitializing = false
                    isPrepared = false
                    playState = PlayState.INIT
                    onError()
                    true
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            Log.e(TAG, "[$id] initFromFile failed: ${e.message}")
            isInitializing = false; isPrepared = false; playState = PlayState.INIT
            onError()
        }
    }

    private fun extractDurationMs(filePath: String): Long {
        return try {
            val retriever = MediaMetadataRetriever()
            try {
                retriever.setDataSource(filePath)
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
            } finally {
                runCatching { retriever.release() }
            }
        } catch (e: Exception) {
            Log.e(TAG, "[$id] duration fallback failed: ${e.message}")
            0L
        }
    }

    private fun onPlaybackCompleted() {
        currentPosition = 0L
        playState = PlayState.STOPPED
        try { mediaPlayer?.seekTo(0) } catch (_: Exception) {}
        Log.d(TAG, "[$id] Playback completed, ready for replay")
    }

    fun play() {
        if (!isPrepared) {
            Log.w(TAG, "[$id] play() called but not prepared, re-initializing")
            val path = localPath ?: return
            initFromFile(path) { play() }
            return
        }
        try {
            mediaPlayer?.let { mp ->
                AudioBubbleStateManager.pauseAllExcept(id)
                mp.start()
                playState = PlayState.PLAYING
            }
        } catch (e: Exception) {
            Log.e(TAG, "[$id] play failed: ${e.message}")
            // Player is in bad state — re-initialize
            isPrepared = false
            val path = localPath ?: return
            initFromFile(path) { play() }
        }
    }

    fun pause() {
        try {
            mediaPlayer?.let { mp ->
                if (mp.isPlaying) {
                    mp.pause()
                    currentPosition = mp.currentPosition.toLong()
                    playState = PlayState.PAUSED
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "[$id] pause failed: ${e.message}")
            playState = PlayState.STOPPED
        }
    }

    fun stop() {
        try {
            mediaPlayer?.let { mp ->
                if (mp.isPlaying) mp.pause()
                mp.seekTo(0)
                currentPosition = 0L
                playState = PlayState.STOPPED
            }
        } catch (e: Exception) {
            Log.e(TAG, "[$id] stop failed: ${e.message}")
            playState = PlayState.STOPPED; currentPosition = 0L
        }
    }

    fun seekTo(positionMs: Long) {
        try {
            val c = positionMs.coerceIn(0L, totalDuration)
            mediaPlayer?.seekTo(c.toInt())
            currentPosition = c
        } catch (e: Exception) {
            Log.e(TAG, "[$id] seekTo failed: ${e.message}")
        }
    }

    fun seekToProgress(progress: Float) {
        seekTo((totalDuration * progress.coerceIn(0f, 1f)).toLong())
    }

    fun updatePosition() {
        try {
            mediaPlayer?.let { mp ->
                if (mp.isPlaying) currentPosition = mp.currentPosition.toLong()
            }
        } catch (e: Exception) {
            Log.e(TAG, "[$id] updatePosition failed: ${e.message}")
        }
    }

    fun release() {
        try { mediaPlayer?.release() } catch (e: Exception) { Log.e(TAG, "[$id] release failed: ${e.message}") }
        mediaPlayer = null; isPrepared = false; playState = PlayState.INIT; currentPosition = 0L; totalDuration = 0L; isInitializing = false
    }
}
