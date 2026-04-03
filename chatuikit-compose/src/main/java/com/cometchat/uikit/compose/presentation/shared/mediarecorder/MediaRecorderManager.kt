package com.cometchat.uikit.compose.presentation.shared.mediarecorder

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.cometchat.uikit.core.viewmodel.MediaRecorderState
import java.io.File
import java.util.Locale

/**
 * Callback interface for media recorder events.
 * Uses the new MediaRecorderState enum (IDLE, RECORDING, RECORDED).
 */
interface MediaRecorderCallback {
    /**
     * Called when recording time updates.
     * @param timeMs Time in milliseconds
     * @param formattedTime Formatted time string (MM:SS)
     */
    fun onTimeUpdate(timeMs: Long, formattedTime: String)
    
    /**
     * Called when audio amplitude changes (for waveform visualization).
     * @param amplitude Normalized amplitude (0.0 to 1.0)
     */
    fun onAmplitudeUpdate(amplitude: Float)
    
    /**
     * Called when recording state changes.
     * @param state The new recording state (IDLE, RECORDING, or RECORDED)
     */
    fun onStateChange(state: MediaRecorderState)
    
    /**
     * Called when recording is complete.
     * @param file The recorded audio file
     * @param durationMs The recording duration in milliseconds
     */
    fun onRecordingComplete(file: File, durationMs: Long)
    
    /**
     * Called when playback progress updates.
     * @param progress Playback progress (0.0 to 1.0)
     */
    fun onPlaybackProgress(progress: Float)
    
    /**
     * Called when playback reaches the end.
     */
    fun onPlaybackComplete()
    
    /**
     * Called when an error occurs.
     * @param exception The exception that occurred
     */
    fun onError(exception: Exception)
}

/**
 * MediaRecorderManager handles the actual audio recording and playback logic.
 * This class manages MediaRecorder, MediaPlayer, audio focus, timer, and amplitude capture.
 *
 * Features:
 * - Audio recording with start, stop functionality
 * - Audio playback with play, pause, seekTo functionality
 * - Audio focus management for proper recording/playback behavior
 * - Timer updates for recording duration display (500ms interval)
 * - Amplitude capture for waveform visualization (100ms interval)
 * - Playback progress updates (~100ms interval)
 * - Resource cleanup on release
 *
 * State Machine:
 * - IDLE: Ready to record
 * - RECORDING: Actively recording audio
 * - RECORDED: Recording complete, ready for playback/submit
 *
 * Usage:
 * ```kotlin
 * val manager = MediaRecorderManager(context)
 * manager.setCallback(object : MediaRecorderCallback {
 *     override fun onTimeUpdate(timeMs: Long, formattedTime: String) { ... }
 *     override fun onAmplitudeUpdate(amplitude: Float) { ... }
 *     override fun onStateChange(state: MediaRecorderState) { ... }
 *     override fun onRecordingComplete(file: File, durationMs: Long) { ... }
 *     override fun onPlaybackProgress(progress: Float) { ... }
 *     override fun onPlaybackComplete() { ... }
 *     override fun onError(exception: Exception) { ... }
 * })
 * manager.startRecording()
 * ```
 */
class MediaRecorderManager(private val context: Context) {
    
    companion object {
        private const val TAG = "MediaRecorderManager"
        private const val AMPLITUDE_UPDATE_INTERVAL = 100L // ms
        private const val TIMER_UPDATE_INTERVAL = 500L // ms
        private const val PLAYBACK_UPDATE_INTERVAL = 100L // ms
        private const val MAX_AMPLITUDE = 32767f // MediaRecorder max amplitude
    }
    
    // ==================== Recording State ====================
    
    private var mediaRecorder: MediaRecorder? = null
    private var recordedFilePath: String? = null
    private var isRecording: Boolean = false
    private var startTime: Long = 0
    private var recordingDurationMs: Long = 0
    private var currentState: MediaRecorderState = MediaRecorderState.IDLE
    
    // ==================== Playback State ====================
    
    private var mediaPlayer: MediaPlayer? = null
    private var isPlaybackActive: Boolean = false
    
    // ==================== Timer Handler ====================
    
    private val timerHandler = Handler(Looper.getMainLooper())
    private val timerRunnable = object : Runnable {
        override fun run() {
            if (isRecording) {
                val elapsedTime = System.currentTimeMillis() - startTime
                recordingDurationMs = elapsedTime
                val seconds = (elapsedTime / 1000).toInt()
                val minutes = seconds / 60
                val secs = seconds % 60
                val formattedTime = String.format(Locale.US, "%02d:%02d", minutes, secs)
                callback?.onTimeUpdate(elapsedTime, formattedTime)
                timerHandler.postDelayed(this, TIMER_UPDATE_INTERVAL)
            }
        }
    }
    
    // ==================== Amplitude Handler ====================
    
    private val amplitudeHandler = Handler(Looper.getMainLooper())
    private val amplitudeRunnable = object : Runnable {
        override fun run() {
            if (isRecording && mediaRecorder != null) {
                try {
                    val amplitude = mediaRecorder?.maxAmplitude ?: 0
                    val normalizedAmplitude = (amplitude / MAX_AMPLITUDE).coerceIn(0f, 1f)
                    callback?.onAmplitudeUpdate(normalizedAmplitude)
                } catch (e: Exception) {
                    Log.e(TAG, "Error getting amplitude: ${e.message}")
                }
                amplitudeHandler.postDelayed(this, AMPLITUDE_UPDATE_INTERVAL)
            }
        }
    }
    
    // ==================== Playback Progress Handler ====================
    
    private val playbackHandler = Handler(Looper.getMainLooper())
    private val playbackRunnable = object : Runnable {
        override fun run() {
            if (isPlaybackActive && mediaPlayer != null) {
                try {
                    val player = mediaPlayer
                    if (player != null && player.isPlaying) {
                        val currentPosition = player.currentPosition
                        val duration = player.duration
                        if (duration > 0) {
                            val progress = currentPosition.toFloat() / duration.toFloat()
                            callback?.onPlaybackProgress(progress.coerceIn(0f, 1f))
                        }
                        playbackHandler.postDelayed(this, PLAYBACK_UPDATE_INTERVAL)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error updating playback progress: ${e.message}")
                }
            }
        }
    }
    
    // ==================== Audio Focus ====================
    
    private var audioManager: AudioManager? = null
    private var recordingAudioFocusRequest: AudioFocusRequest? = null
    private var playbackAudioFocusRequest: AudioFocusRequest? = null
    
    private val recordingFocusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT,
            AudioManager.AUDIOFOCUS_LOSS -> {
                // Stop recording when audio focus is lost
                if (isRecording) {
                    Log.d(TAG, "Audio focus lost during recording, stopping")
                    stopRecording()
                }
            }
        }
    }
    
    private val playbackFocusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT,
            AudioManager.AUDIOFOCUS_LOSS -> {
                // Pause playback when audio focus is lost
                if (isPlaybackActive) {
                    Log.d(TAG, "Audio focus lost during playback, pausing")
                    pausePlayback()
                }
            }
        }
    }
    
    // ==================== Callback ====================
    
    private var callback: MediaRecorderCallback? = null
    
    init {
        setupAudioManager()
    }
    
    /**
     * Sets up the audio manager for audio focus handling.
     */
    private fun setupAudioManager() {
        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }
    
    /**
     * Requests audio focus for recording.
     * Uses AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE for exclusive recording access.
     * @return True if audio focus was granted
     */
    private fun requestRecordingAudioFocus(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()
            recordingAudioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE)
                .setAudioAttributes(audioAttributes)
                .setOnAudioFocusChangeListener(recordingFocusChangeListener)
                .setAcceptsDelayedFocusGain(true)
                .build()
            val result = audioManager?.requestAudioFocus(recordingAudioFocusRequest!!)
            result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        } else {
            @Suppress("DEPRECATION")
            val result = audioManager?.requestAudioFocus(
                recordingFocusChangeListener,
                AudioManager.STREAM_VOICE_CALL,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE
            )
            result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        }
    }
    
    /**
     * Requests audio focus for playback.
     * @return True if audio focus was granted
     */
    private fun requestPlaybackAudioFocus(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
            playbackAudioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                .setAudioAttributes(audioAttributes)
                .setOnAudioFocusChangeListener(playbackFocusChangeListener)
                .setAcceptsDelayedFocusGain(true)
                .build()
            val result = audioManager?.requestAudioFocus(playbackAudioFocusRequest!!)
            result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        } else {
            @Suppress("DEPRECATION")
            val result = audioManager?.requestAudioFocus(
                playbackFocusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
            )
            result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        }
    }
    
    /**
     * Abandons recording audio focus.
     */
    private fun abandonRecordingAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            recordingAudioFocusRequest?.let { audioManager?.abandonAudioFocusRequest(it) }
        } else {
            @Suppress("DEPRECATION")
            audioManager?.abandonAudioFocus(recordingFocusChangeListener)
        }
        recordingAudioFocusRequest = null
    }
    
    /**
     * Abandons playback audio focus.
     */
    private fun abandonPlaybackAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            playbackAudioFocusRequest?.let { audioManager?.abandonAudioFocusRequest(it) }
        } else {
            @Suppress("DEPRECATION")
            audioManager?.abandonAudioFocus(playbackFocusChangeListener)
        }
        playbackAudioFocusRequest = null
    }
    
    /**
     * Abandons all audio focus (both recording and playback).
     */
    private fun abandonAudioFocus() {
        abandonRecordingAudioFocus()
        abandonPlaybackAudioFocus()
    }
    
    // ==================== Public API ====================
    
    /**
     * Sets the callback for media recorder events.
     */
    fun setCallback(callback: MediaRecorderCallback) {
        this.callback = callback
    }
    
    /**
     * Gets the current recording state.
     */
    fun getCurrentState(): MediaRecorderState = currentState
    
    /**
     * Gets the recorded file path.
     */
    fun getRecordedFilePath(): String? = recordedFilePath
    
    /**
     * Gets the recorded file.
     */
    fun getRecordedFile(): File? = recordedFilePath?.let { File(it) }
    
    /**
     * Gets the recording duration in milliseconds.
     */
    fun getRecordingDurationMs(): Long = recordingDurationMs
    
    /**
     * Checks if playback is currently active.
     */
    fun isPlaying(): Boolean = isPlaybackActive && (mediaPlayer?.isPlaying == true)
    
    // ==================== Recording Methods ====================
    
    /**
     * Starts audio recording.
     * Transitions from IDLE to RECORDING state.
     * @return true if recording started successfully, false otherwise
     */
    fun startRecording(): Boolean {
        if (currentState != MediaRecorderState.IDLE) {
            Log.w(TAG, "Cannot start recording: current state is $currentState")
            return false
        }
        
        if (!requestRecordingAudioFocus()) {
            callback?.onError(Exception("Failed to acquire audio focus"))
            return false
        }
        
        try {
            recordedFilePath = "${context.externalCacheDir?.absolutePath}/audio_record_${System.currentTimeMillis()}.m4a"
            
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setOutputFile(recordedFilePath)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                prepare()
                start()
            }
            
            isRecording = true
            startTime = System.currentTimeMillis()
            recordingDurationMs = 0
            currentState = MediaRecorderState.RECORDING
            
            // Start timer and amplitude updates
            timerHandler.postDelayed(timerRunnable, 0)
            amplitudeHandler.postDelayed(amplitudeRunnable, AMPLITUDE_UPDATE_INTERVAL)
            
            callback?.onStateChange(MediaRecorderState.RECORDING)
            Log.d(TAG, "Recording started: $recordedFilePath")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start recording: ${e.message}")
            callback?.onError(e)
            currentState = MediaRecorderState.IDLE
            callback?.onStateChange(MediaRecorderState.IDLE)
            abandonRecordingAudioFocus()
            return false
        }
    }
    
    /**
     * Stops audio recording.
     * Transitions from RECORDING to RECORDED state.
     * @return the recorded File if successful, null otherwise
     */
    fun stopRecording(): File? {
        if (currentState != MediaRecorderState.RECORDING) {
            Log.w(TAG, "Cannot stop recording: current state is $currentState")
            return null
        }
        
        try {
            // Calculate final duration before stopping
            recordingDurationMs = System.currentTimeMillis() - startTime
            
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            isRecording = false
            
            // Stop updates
            timerHandler.removeCallbacks(timerRunnable)
            amplitudeHandler.removeCallbacks(amplitudeRunnable)
            
            currentState = MediaRecorderState.RECORDED
            callback?.onStateChange(MediaRecorderState.RECORDED)
            
            // Abandon recording audio focus
            abandonRecordingAudioFocus()
            
            // Notify recording complete
            recordedFilePath?.let { path ->
                val file = File(path)
                if (file.exists()) {
                    callback?.onRecordingComplete(file, recordingDurationMs)
                    Log.d(TAG, "Recording stopped: $path, duration: ${recordingDurationMs}ms")
                    return file
                }
            }
            
            return null
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop recording: ${e.message}")
            callback?.onError(e)
            currentState = MediaRecorderState.IDLE
            callback?.onStateChange(MediaRecorderState.IDLE)
            abandonRecordingAudioFocus()
            return null
        }
    }
    
    /**
     * Deletes the current recording and resets state.
     * Transitions to IDLE state.
     */
    fun deleteRecording() {
        // Stop recording if in progress
        if (isRecording) {
            try {
                mediaRecorder?.apply {
                    stop()
                    release()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping recorder: ${e.message}")
            }
            mediaRecorder = null
            isRecording = false
        }
        
        // Stop playback if active
        releaseMediaPlayer()
        
        // Stop all handler callbacks
        timerHandler.removeCallbacks(timerRunnable)
        amplitudeHandler.removeCallbacks(amplitudeRunnable)
        playbackHandler.removeCallbacks(playbackRunnable)
        
        // Delete the recorded file
        recordedFilePath?.let { path ->
            try {
                val file = File(path)
                if (file.exists()) {
                    file.delete()
                    Log.d(TAG, "Deleted recording file: $path")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting file: ${e.message}")
            }
        }
        recordedFilePath = null
        recordingDurationMs = 0
        
        currentState = MediaRecorderState.IDLE
        callback?.onStateChange(MediaRecorderState.IDLE)
        callback?.onTimeUpdate(0, "00:00")
        callback?.onAmplitudeUpdate(0f)
        callback?.onPlaybackProgress(0f)
        
        abandonAudioFocus()
        Log.d(TAG, "Recording deleted, state reset to IDLE")
    }
    
    // ==================== Playback Methods ====================
    
    /**
     * Starts playback of the recorded audio.
     * Only valid when in RECORDED state.
     */
    fun startPlayback() {
        if (currentState != MediaRecorderState.RECORDED) {
            Log.w(TAG, "Cannot start playback: current state is $currentState")
            return
        }
        
        val filePath = recordedFilePath
        if (filePath == null) {
            Log.w(TAG, "Cannot start playback: no recorded file")
            return
        }
        
        if (!requestPlaybackAudioFocus()) {
            callback?.onError(Exception("Failed to acquire audio focus for playback"))
            return
        }
        
        try {
            // If MediaPlayer already exists and is paused, just resume
            if (mediaPlayer != null) {
                mediaPlayer?.start()
                isPlaybackActive = true
                playbackHandler.postDelayed(playbackRunnable, PLAYBACK_UPDATE_INTERVAL)
                Log.d(TAG, "Playback resumed")
                return
            }
            
            // Create new MediaPlayer
            mediaPlayer = MediaPlayer().apply {
                setDataSource(filePath)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                setOnCompletionListener {
                    handlePlaybackCompletion()
                }
                setOnErrorListener { _, what, extra ->
                    Log.e(TAG, "MediaPlayer error: what=$what, extra=$extra")
                    callback?.onError(Exception("Playback error: $what"))
                    releaseMediaPlayer()
                    true
                }
                prepare()
                start()
            }
            
            isPlaybackActive = true
            playbackHandler.postDelayed(playbackRunnable, PLAYBACK_UPDATE_INTERVAL)
            Log.d(TAG, "Playback started: $filePath")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start playback: ${e.message}")
            callback?.onError(e)
            releaseMediaPlayer()
        }
    }
    
    /**
     * Pauses playback of the recorded audio.
     */
    fun pausePlayback() {
        if (!isPlaybackActive) {
            Log.w(TAG, "Cannot pause playback: not playing")
            return
        }
        
        try {
            mediaPlayer?.pause()
            isPlaybackActive = false
            playbackHandler.removeCallbacks(playbackRunnable)
            abandonPlaybackAudioFocus()
            Log.d(TAG, "Playback paused")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to pause playback: ${e.message}")
            callback?.onError(e)
        }
    }
    
    /**
     * Seeks to a specific position in the playback.
     * @param progress Position to seek to (0.0 to 1.0)
     */
    fun seekTo(progress: Float) {
        if (currentState != MediaRecorderState.RECORDED) {
            Log.w(TAG, "Cannot seek: current state is $currentState")
            return
        }
        
        val player = mediaPlayer
        if (player == null) {
            // If no player exists, just update the progress callback
            callback?.onPlaybackProgress(progress.coerceIn(0f, 1f))
            return
        }
        
        try {
            val duration = player.duration
            if (duration > 0) {
                val seekPosition = (progress.coerceIn(0f, 1f) * duration).toInt()
                player.seekTo(seekPosition)
                callback?.onPlaybackProgress(progress.coerceIn(0f, 1f))
                Log.d(TAG, "Seeked to position: $seekPosition ms (${(progress * 100).toInt()}%)")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to seek: ${e.message}")
            callback?.onError(e)
        }
    }
    
    /**
     * Handles playback completion.
     * Resets playback to the beginning.
     */
    private fun handlePlaybackCompletion() {
        isPlaybackActive = false
        playbackHandler.removeCallbacks(playbackRunnable)
        
        // Reset to beginning
        try {
            mediaPlayer?.seekTo(0)
        } catch (e: Exception) {
            Log.e(TAG, "Error resetting playback position: ${e.message}")
        }
        
        callback?.onPlaybackProgress(0f)
        callback?.onPlaybackComplete()
        abandonPlaybackAudioFocus()
        Log.d(TAG, "Playback completed, reset to beginning")
    }
    
    /**
     * Releases the MediaPlayer resources.
     */
    private fun releaseMediaPlayer() {
        try {
            mediaPlayer?.apply {
                if (isPlaying) {
                    stop()
                }
                release()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing MediaPlayer: ${e.message}")
        }
        mediaPlayer = null
        isPlaybackActive = false
        playbackHandler.removeCallbacks(playbackRunnable)
        abandonPlaybackAudioFocus()
    }
    
    // ==================== Resource Cleanup ====================
    
    /**
     * Releases all resources.
     * Call this when the recorder is no longer needed.
     * 
     * This method:
     * - Releases MediaRecorder resources
     * - Releases MediaPlayer resources
     * - Removes all handler callbacks
     * - Deletes temporary recording files (if not submitted)
     * - Abandons audio focus
     */
    fun release() {
        Log.d(TAG, "Releasing MediaRecorderManager resources")
        
        // Release MediaRecorder
        try {
            if (isRecording) {
                mediaRecorder?.stop()
            }
            mediaRecorder?.release()
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing MediaRecorder: ${e.message}")
        }
        mediaRecorder = null
        isRecording = false
        
        // Release MediaPlayer
        releaseMediaPlayer()
        
        // Remove all handler callbacks
        timerHandler.removeCallbacks(timerRunnable)
        amplitudeHandler.removeCallbacks(amplitudeRunnable)
        playbackHandler.removeCallbacks(playbackRunnable)
        
        // Delete temporary file if not submitted
        recordedFilePath?.let { path ->
            try {
                val file = File(path)
                if (file.exists()) {
                    file.delete()
                    Log.d(TAG, "Deleted temporary recording file: $path")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting file: ${e.message}")
            }
        }
        recordedFilePath = null
        recordingDurationMs = 0
        
        // Abandon all audio focus
        abandonAudioFocus()
        
        // Reset state
        currentState = MediaRecorderState.IDLE
        callback = null
        
        Log.d(TAG, "MediaRecorderManager released")
    }
    
    /**
     * Marks the recording as submitted (prevents deletion on release).
     * Call this before release() if the recording was successfully submitted.
     */
    fun markAsSubmitted() {
        // Clear the file path so it won't be deleted on release
        recordedFilePath = null
        Log.d(TAG, "Recording marked as submitted")
    }
}
