package com.cometchat.uikit.compose.presentation.shared.inlineaudiorecorder.utils

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
import com.cometchat.uikit.core.viewmodel.InlineAudioRecorderStatus
import java.io.File
import kotlin.math.sqrt

/**
 * Callback interface for inline audio recorder events.
 * Uses the six-state InlineAudioRecorderStatus enum.
 */
interface InlineAudioRecorderCallback {
    /**
     * Called when recording duration updates.
     * @param durationMs Duration in milliseconds
     */
    fun onDurationUpdate(durationMs: Long)
    
    /**
     * Called when audio amplitude changes (for waveform visualization).
     * @param amplitude Normalized amplitude (0.0 to 1.0)
     */
    fun onAmplitudeUpdate(amplitude: Float)
    
    /**
     * Called when recording status changes.
     * @param status The new recording status
     */
    fun onStatusChange(status: InlineAudioRecorderStatus)
    
    /**
     * Called when recording is complete.
     * @param file The recorded audio file
     * @param durationMs The recording duration in milliseconds
     * @param amplitudes The list of amplitude values captured during recording
     */
    fun onRecordingComplete(file: File, durationMs: Long, amplitudes: List<Float>)
    
    /**
     * Called when playback position updates.
     * @param positionMs Current playback position in milliseconds
     */
    fun onPlaybackPositionUpdate(positionMs: Long)
    
    /**
     * Called when playback reaches the end.
     */
    fun onPlaybackComplete()
    
    /**
     * Called when an error occurs.
     * @param message The error message
     */
    fun onError(message: String)
}

/**
 * InlineAudioRecorderManager handles the actual audio recording and playback logic.
 * Supports pause/resume recording (API 24+ for true pause).
 *
 * Features:
 * - Audio recording with start, pause, resume, stop functionality
 * - Audio playback with play, pause, seekTo functionality
 * - Amplitude capture and storage for playback visualization
 * - Playback position tracking via polling (100ms intervals)
 * - Audio focus management
 * - Resource cleanup
 *
 * State Machine:
 * - IDLE: Ready to record
 * - RECORDING: Actively recording audio
 * - PAUSED: Recording is paused
 * - COMPLETED: Recording complete, ready for playback/submit
 * - PLAYING: Playing back recorded audio
 * - ERROR: Error state
 *
 * Usage:
 * ```kotlin
 * val manager = InlineAudioRecorderManager(context)
 * manager.setCallback(object : InlineAudioRecorderCallback {
 *     override fun onDurationUpdate(durationMs: Long) { ... }
 *     override fun onAmplitudeUpdate(amplitude: Float) { ... }
 *     override fun onStatusChange(status: InlineAudioRecorderStatus) { ... }
 *     override fun onRecordingComplete(file: File, durationMs: Long, amplitudes: List<Float>) { ... }
 *     override fun onPlaybackPositionUpdate(positionMs: Long) { ... }
 *     override fun onPlaybackComplete() { ... }
 *     override fun onError(message: String) { ... }
 * })
 * manager.startRecording()
 * ```
 */
class InlineAudioRecorderManager(private val context: Context) {
    
    companion object {
        private const val TAG = "InlineAudioRecorderMgr"
        private const val AMPLITUDE_UPDATE_INTERVAL = 100L // ms (100-120ms as per spec)
        private const val TIMER_UPDATE_INTERVAL = 100L // ms for smooth timer updates
        private const val PLAYBACK_POSITION_INTERVAL = 100L // ms for playback position polling
        private const val MAX_AMPLITUDE = 32767f // MediaRecorder max amplitude
    }
    
    // ==================== Recording State ====================
    
    private var mediaRecorder: MediaRecorder? = null
    private var recordedFilePath: String? = null
    private var isRecording: Boolean = false
    private var isPaused: Boolean = false
    private var startTime: Long = 0
    private var pausedDuration: Long = 0 // Duration accumulated before pause
    private var recordingDurationMs: Long = 0
    private var currentStatus: InlineAudioRecorderStatus = InlineAudioRecorderStatus.IDLE
    
    // ==================== Amplitude Storage ====================
    
    private val amplitudeHistory = mutableListOf<Float>()
    
    // ==================== Playback State ====================
    
    private var mediaPlayer: MediaPlayer? = null
    private var isPlaybackActive: Boolean = false
    
    // ==================== Timer Handler ====================
    
    private val timerHandler = Handler(Looper.getMainLooper())
    private val timerRunnable = object : Runnable {
        override fun run() {
            if (isRecording && !isPaused) {
                val elapsedTime = pausedDuration + (System.currentTimeMillis() - startTime)
                recordingDurationMs = elapsedTime
                callback?.onDurationUpdate(elapsedTime)
                timerHandler.postDelayed(this, TIMER_UPDATE_INTERVAL)
            }
        }
    }
    
    // ==================== Amplitude Handler ====================
    
    private val amplitudeHandler = Handler(Looper.getMainLooper())
    private val amplitudeRunnable = object : Runnable {
        override fun run() {
            if (isRecording && !isPaused && mediaRecorder != null) {
                try {
                    val rawAmplitude = mediaRecorder?.maxAmplitude ?: 0
                    val processedAmplitude = processAmplitude(rawAmplitude)
                    amplitudeHistory.add(processedAmplitude)
                    callback?.onAmplitudeUpdate(processedAmplitude)
                } catch (e: Exception) {
                    Log.e(TAG, "Error getting amplitude: ${e.message}")
                }
                amplitudeHandler.postDelayed(this, AMPLITUDE_UPDATE_INTERVAL)
            }
        }
    }
    
    // ==================== Playback Position Handler ====================
    
    private val playbackHandler = Handler(Looper.getMainLooper())
    private val playbackRunnable = object : Runnable {
        override fun run() {
            if (isPlaybackActive && mediaPlayer != null) {
                try {
                    val player = mediaPlayer
                    if (player != null && player.isPlaying) {
                        val currentPosition = player.currentPosition.toLong()
                        callback?.onPlaybackPositionUpdate(currentPosition)
                        playbackHandler.postDelayed(this, PLAYBACK_POSITION_INTERVAL)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error updating playback position: ${e.message}")
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
                // Pause recording when audio focus is lost
                if (isRecording && !isPaused) {
                    Log.d(TAG, "Audio focus lost during recording, pausing")
                    pauseRecording()
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
    
    private var callback: InlineAudioRecorderCallback? = null
    
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
     * Processes raw amplitude values for better visual response.
     * Uses non-linear mapping (sqrt) to make quiet sounds more visible.
     *
     * @param rawAmplitude Raw amplitude from MediaRecorder.getMaxAmplitude()
     * @return Processed amplitude in range [0.0, 1.0]
     */
    private fun processAmplitude(rawAmplitude: Int): Float {
        val normalized = (rawAmplitude / MAX_AMPLITUDE).coerceIn(0f, 1f)
        // Apply non-linear amplification for better visual response
        // sqrt provides good balance between quiet and loud sounds
        return sqrt(normalized).coerceIn(0f, 1f)
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
     * Sets the callback for inline audio recorder events.
     */
    fun setCallback(callback: InlineAudioRecorderCallback) {
        this.callback = callback
    }
    
    /**
     * Gets the current recording status.
     */
    fun getCurrentStatus(): InlineAudioRecorderStatus = currentStatus
    
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
     * Gets the amplitude history.
     */
    fun getAmplitudeHistory(): List<Float> = amplitudeHistory.toList()
    
    /**
     * Checks if playback is currently active.
     */
    fun isPlaying(): Boolean = isPlaybackActive && (mediaPlayer?.isPlaying == true)
    
    /**
     * Checks if pause/resume is supported on this device.
     * True pause/resume requires API 24+.
     */
    fun isPauseResumeSupported(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
    
    // ==================== Recording Methods ====================
    
    /**
     * Starts audio recording.
     * Transitions from IDLE to RECORDING state.
     * @return true if recording started successfully, false otherwise
     */
    fun startRecording(): Boolean {
        if (currentStatus != InlineAudioRecorderStatus.IDLE) {
            Log.w(TAG, "Cannot start recording: current status is $currentStatus")
            return false
        }
        
        if (!requestRecordingAudioFocus()) {
            callback?.onError("Failed to acquire audio focus")
            return false
        }
        
        try {
            recordedFilePath = "${context.externalCacheDir?.absolutePath}/inline_audio_${System.currentTimeMillis()}.m4a"
            
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
            isPaused = false
            startTime = System.currentTimeMillis()
            pausedDuration = 0
            recordingDurationMs = 0
            amplitudeHistory.clear()
            currentStatus = InlineAudioRecorderStatus.RECORDING
            
            // Start timer and amplitude updates
            timerHandler.postDelayed(timerRunnable, 0)
            amplitudeHandler.postDelayed(amplitudeRunnable, AMPLITUDE_UPDATE_INTERVAL)
            
            callback?.onStatusChange(InlineAudioRecorderStatus.RECORDING)
            Log.d(TAG, "Recording started: $recordedFilePath")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start recording: ${e.message}")
            callback?.onError("Failed to start recording: ${e.message}")
            currentStatus = InlineAudioRecorderStatus.ERROR
            callback?.onStatusChange(InlineAudioRecorderStatus.ERROR)
            abandonRecordingAudioFocus()
            return false
        }
    }

    
    /**
     * Pauses audio recording.
     * Transitions from RECORDING to PAUSED state.
     * On API 24+, uses MediaRecorder.pause() for true pause.
     * On older APIs, stops recording (resume will restart with fresh data).
     * @return true if pause was successful, false otherwise
     */
    fun pauseRecording(): Boolean {
        if (currentStatus != InlineAudioRecorderStatus.RECORDING || !isRecording || isPaused) {
            Log.w(TAG, "Cannot pause recording: current status is $currentStatus, isRecording=$isRecording, isPaused=$isPaused")
            return false
        }
        
        try {
            // Calculate duration up to this point
            pausedDuration += System.currentTimeMillis() - startTime
            recordingDurationMs = pausedDuration
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                // API 24+: Use true pause
                mediaRecorder?.pause()
                Log.d(TAG, "Recording paused (API 24+ true pause)")
            } else {
                // Older APIs: Stop recording, will restart on resume
                // Note: This means resume will start fresh recording
                Log.d(TAG, "Recording paused (pre-API 24 fallback - will restart on resume)")
            }
            
            isPaused = true
            currentStatus = InlineAudioRecorderStatus.PAUSED
            
            // Stop timer and amplitude updates
            timerHandler.removeCallbacks(timerRunnable)
            amplitudeHandler.removeCallbacks(amplitudeRunnable)
            
            callback?.onStatusChange(InlineAudioRecorderStatus.PAUSED)
            callback?.onDurationUpdate(recordingDurationMs)
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to pause recording: ${e.message}")
            callback?.onError("Failed to pause recording: ${e.message}")
            return false
        }
    }
    
    /**
     * Resumes audio recording.
     * Transitions from PAUSED to RECORDING state.
     * On API 24+, uses MediaRecorder.resume() for true resume.
     * On older APIs, restarts recording with fresh duration and amplitudes.
     * @return true if resume was successful, false otherwise
     */
    fun resumeRecording(): Boolean {
        if (currentStatus != InlineAudioRecorderStatus.PAUSED || !isRecording || !isPaused) {
            Log.w(TAG, "Cannot resume recording: current status is $currentStatus, isRecording=$isRecording, isPaused=$isPaused")
            return false
        }
        
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                // API 24+: Use true resume
                mediaRecorder?.resume()
                startTime = System.currentTimeMillis()
                Log.d(TAG, "Recording resumed (API 24+ true resume)")
            } else {
                // Older APIs: Restart recording from scratch
                // Clear previous data and start fresh
                pausedDuration = 0
                recordingDurationMs = 0
                amplitudeHistory.clear()
                startTime = System.currentTimeMillis()
                
                // Need to restart MediaRecorder
                try {
                    mediaRecorder?.stop()
                    mediaRecorder?.release()
                } catch (e: Exception) {
                    Log.e(TAG, "Error stopping old recorder: ${e.message}")
                }
                
                // Create new file for fresh recording
                recordedFilePath = "${context.externalCacheDir?.absolutePath}/inline_audio_${System.currentTimeMillis()}.m4a"
                
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
                
                Log.d(TAG, "Recording restarted (pre-API 24 fallback)")
            }
            
            isPaused = false
            currentStatus = InlineAudioRecorderStatus.RECORDING
            
            // Restart timer and amplitude updates
            timerHandler.postDelayed(timerRunnable, 0)
            amplitudeHandler.postDelayed(amplitudeRunnable, AMPLITUDE_UPDATE_INTERVAL)
            
            callback?.onStatusChange(InlineAudioRecorderStatus.RECORDING)
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to resume recording: ${e.message}")
            callback?.onError("Failed to resume recording: ${e.message}")
            currentStatus = InlineAudioRecorderStatus.ERROR
            callback?.onStatusChange(InlineAudioRecorderStatus.ERROR)
            return false
        }
    }
    
    /**
     * Stops audio recording.
     * Transitions from RECORDING or PAUSED to COMPLETED state.
     * @return the recorded File if successful, null otherwise
     */
    fun stopRecording(): File? {
        if (currentStatus != InlineAudioRecorderStatus.RECORDING && 
            currentStatus != InlineAudioRecorderStatus.PAUSED) {
            Log.w(TAG, "Cannot stop recording: current status is $currentStatus")
            return null
        }
        
        try {
            // Calculate final duration
            if (!isPaused) {
                recordingDurationMs = pausedDuration + (System.currentTimeMillis() - startTime)
            }
            
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            isRecording = false
            isPaused = false
            
            // Stop updates
            timerHandler.removeCallbacks(timerRunnable)
            amplitudeHandler.removeCallbacks(amplitudeRunnable)
            
            currentStatus = InlineAudioRecorderStatus.COMPLETED
            callback?.onStatusChange(InlineAudioRecorderStatus.COMPLETED)
            
            // Notify recording complete
            recordedFilePath?.let { path ->
                val file = File(path)
                if (file.exists()) {
                    callback?.onRecordingComplete(file, recordingDurationMs, amplitudeHistory.toList())
                    Log.d(TAG, "Recording stopped: $path, duration: ${recordingDurationMs}ms, amplitudes: ${amplitudeHistory.size}")
                    return file
                }
            }
            
            return null
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop recording: ${e.message}")
            callback?.onError("Failed to stop recording: ${e.message}")
            currentStatus = InlineAudioRecorderStatus.ERROR
            callback?.onStatusChange(InlineAudioRecorderStatus.ERROR)
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
                if (!isPaused && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    mediaRecorder?.stop()
                } else if (!isPaused) {
                    mediaRecorder?.stop()
                }
                mediaRecorder?.release()
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping recorder: ${e.message}")
            }
            mediaRecorder = null
            isRecording = false
            isPaused = false
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
        pausedDuration = 0
        amplitudeHistory.clear()
        
        currentStatus = InlineAudioRecorderStatus.IDLE
        callback?.onStatusChange(InlineAudioRecorderStatus.IDLE)
        callback?.onDurationUpdate(0)
        callback?.onAmplitudeUpdate(0f)
        callback?.onPlaybackPositionUpdate(0)
        
        abandonAudioFocus()
        Log.d(TAG, "Recording deleted, state reset to IDLE")
    }
    
    // ==================== Playback Methods ====================
    
    /**
     * Starts playback of the recorded audio.
     * Only valid when in COMPLETED state.
     */
    fun startPlayback() {
        if (currentStatus != InlineAudioRecorderStatus.COMPLETED) {
            Log.w(TAG, "Cannot start playback: current status is $currentStatus")
            return
        }
        
        val filePath = recordedFilePath
        if (filePath == null) {
            Log.w(TAG, "Cannot start playback: no recorded file")
            return
        }
        
        if (!requestPlaybackAudioFocus()) {
            callback?.onError("Failed to acquire audio focus for playback")
            return
        }
        
        try {
            // If MediaPlayer already exists and is paused, just resume
            if (mediaPlayer != null) {
                mediaPlayer?.start()
                isPlaybackActive = true
                currentStatus = InlineAudioRecorderStatus.PLAYING
                callback?.onStatusChange(InlineAudioRecorderStatus.PLAYING)
                playbackHandler.postDelayed(playbackRunnable, PLAYBACK_POSITION_INTERVAL)
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
                    callback?.onError("Playback error: $what")
                    releaseMediaPlayer()
                    currentStatus = InlineAudioRecorderStatus.COMPLETED
                    callback?.onStatusChange(InlineAudioRecorderStatus.COMPLETED)
                    true
                }
                prepare()
                start()
            }
            
            isPlaybackActive = true
            currentStatus = InlineAudioRecorderStatus.PLAYING
            callback?.onStatusChange(InlineAudioRecorderStatus.PLAYING)
            playbackHandler.postDelayed(playbackRunnable, PLAYBACK_POSITION_INTERVAL)
            Log.d(TAG, "Playback started: $filePath")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start playback: ${e.message}")
            callback?.onError("Failed to start playback: ${e.message}")
            releaseMediaPlayer()
        }
    }
    
    /**
     * Pauses playback of the recorded audio.
     */
    fun pausePlayback() {
        if (!isPlaybackActive || currentStatus != InlineAudioRecorderStatus.PLAYING) {
            Log.w(TAG, "Cannot pause playback: not playing")
            return
        }
        
        try {
            mediaPlayer?.pause()
            isPlaybackActive = false
            playbackHandler.removeCallbacks(playbackRunnable)
            
            currentStatus = InlineAudioRecorderStatus.COMPLETED
            callback?.onStatusChange(InlineAudioRecorderStatus.COMPLETED)
            abandonPlaybackAudioFocus()
            Log.d(TAG, "Playback paused")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to pause playback: ${e.message}")
            callback?.onError("Failed to pause playback: ${e.message}")
        }
    }
    
    /**
     * Seeks to a specific position in the playback.
     * @param positionMs Position to seek to in milliseconds
     */
    fun seekTo(positionMs: Long) {
        if (currentStatus != InlineAudioRecorderStatus.COMPLETED && 
            currentStatus != InlineAudioRecorderStatus.PLAYING) {
            Log.w(TAG, "Cannot seek: current status is $currentStatus")
            return
        }
        
        val player = mediaPlayer
        if (player == null) {
            // If no player exists, just update the callback
            callback?.onPlaybackPositionUpdate(positionMs.coerceIn(0L, recordingDurationMs))
            return
        }
        
        try {
            val clampedPosition = positionMs.coerceIn(0L, recordingDurationMs)
            player.seekTo(clampedPosition.toInt())
            callback?.onPlaybackPositionUpdate(clampedPosition)
            Log.d(TAG, "Seeked to position: $clampedPosition ms")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to seek: ${e.message}")
            callback?.onError("Failed to seek: ${e.message}")
        }
    }
    
    /**
     * Gets the current playback position.
     * @return Current position in milliseconds, or 0 if not playing
     */
    fun getPlaybackPosition(): Long {
        return try {
            mediaPlayer?.currentPosition?.toLong() ?: 0L
        } catch (e: Exception) {
            0L
        }
    }
    
    /**
     * Handles playback completion.
     * Resets playback to the beginning and transitions to COMPLETED state.
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
        
        currentStatus = InlineAudioRecorderStatus.COMPLETED
        callback?.onPlaybackPositionUpdate(0)
        callback?.onPlaybackComplete()
        callback?.onStatusChange(InlineAudioRecorderStatus.COMPLETED)
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
     * - Clears amplitude history
     */
    fun release() {
        Log.d(TAG, "Releasing InlineAudioRecorderManager resources")
        
        // Release MediaRecorder
        try {
            if (isRecording && !isPaused) {
                mediaRecorder?.stop()
            }
            mediaRecorder?.release()
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing MediaRecorder: ${e.message}")
        }
        mediaRecorder = null
        isRecording = false
        isPaused = false
        
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
        pausedDuration = 0
        
        // Clear amplitude history
        amplitudeHistory.clear()
        
        // Abandon all audio focus
        abandonAudioFocus()
        
        // Reset state
        currentStatus = InlineAudioRecorderStatus.IDLE
        callback = null
        
        Log.d(TAG, "InlineAudioRecorderManager released")
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
