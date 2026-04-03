package com.cometchat.uikit.kotlin.presentation.shared.inlineaudiorecorder

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
 */
interface InlineAudioRecorderCallback {
    fun onDurationUpdate(durationMs: Long)
    fun onAmplitudeUpdate(amplitude: Float)
    fun onStatusChange(status: InlineAudioRecorderStatus)
    fun onRecordingComplete(file: File, durationMs: Long, amplitudes: List<Float>)
    fun onPlaybackPositionUpdate(positionMs: Long)
    fun onPlaybackComplete()
    fun onError(message: String)
}

/**
 * InlineAudioRecorderManager handles audio recording and playback for chatuikit-kotlin.
 * Supports pause/resume recording (API 24+ for true pause).
 */
class InlineAudioRecorderManager(private val context: Context) {
    
    companion object {
        private const val TAG = "InlineAudioRecorderMgr"
        private const val AMPLITUDE_UPDATE_INTERVAL = 100L
        private const val TIMER_UPDATE_INTERVAL = 100L
        private const val PLAYBACK_POSITION_INTERVAL = 100L
        private const val MAX_AMPLITUDE = 32767f
    }
    
    // Recording State
    private var mediaRecorder: MediaRecorder? = null
    private var recordedFilePath: String? = null
    private var isRecording: Boolean = false
    private var isPaused: Boolean = false
    private var startTime: Long = 0
    private var pausedDuration: Long = 0
    private var recordingDurationMs: Long = 0
    private var currentStatus: InlineAudioRecorderStatus = InlineAudioRecorderStatus.IDLE
    
    // Amplitude Storage
    private val amplitudeHistory = mutableListOf<Float>()
    
    // Playback State
    private var mediaPlayer: MediaPlayer? = null
    private var isPlaybackActive: Boolean = false
    
    // Handlers
    private val timerHandler = Handler(Looper.getMainLooper())
    private val amplitudeHandler = Handler(Looper.getMainLooper())
    private val playbackHandler = Handler(Looper.getMainLooper())
    
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
    
    // Audio Focus
    private var audioManager: AudioManager? = null
    private var recordingAudioFocusRequest: AudioFocusRequest? = null
    private var playbackAudioFocusRequest: AudioFocusRequest? = null
    
    private val recordingFocusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT,
            AudioManager.AUDIOFOCUS_LOSS -> {
                if (isRecording && !isPaused) {
                    pauseRecording()
                }
            }
        }
    }
    
    private val playbackFocusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT,
            AudioManager.AUDIOFOCUS_LOSS -> {
                if (isPlaybackActive) {
                    pausePlayback()
                }
            }
        }
    }
    
    private var callback: InlineAudioRecorderCallback? = null
    
    init {
        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }
    
    private fun processAmplitude(rawAmplitude: Int): Float {
        val normalized = (rawAmplitude / MAX_AMPLITUDE).coerceIn(0f, 1f)
        return sqrt(normalized).coerceIn(0f, 1f)
    }
    
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
    
    private fun abandonAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            recordingAudioFocusRequest?.let { audioManager?.abandonAudioFocusRequest(it) }
            playbackAudioFocusRequest?.let { audioManager?.abandonAudioFocusRequest(it) }
        } else {
            @Suppress("DEPRECATION")
            audioManager?.abandonAudioFocus(recordingFocusChangeListener)
            @Suppress("DEPRECATION")
            audioManager?.abandonAudioFocus(playbackFocusChangeListener)
        }
        recordingAudioFocusRequest = null
        playbackAudioFocusRequest = null
    }
    
    // Public API
    
    fun setCallback(callback: InlineAudioRecorderCallback) {
        this.callback = callback
    }
    
    fun getCurrentStatus(): InlineAudioRecorderStatus = currentStatus
    
    fun getRecordedFilePath(): String? = recordedFilePath
    
    fun getRecordedFile(): File? = recordedFilePath?.let { File(it) }
    
    fun getRecordingDurationMs(): Long = recordingDurationMs
    
    fun getAmplitudeHistory(): List<Float> = amplitudeHistory.toList()
    
    fun isPlaying(): Boolean = isPlaybackActive && (mediaPlayer?.isPlaying == true)
    
    fun isPauseResumeSupported(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
    
    fun startRecording(): Boolean {
        Log.d(TAG, "startRecording: called, currentStatus = $currentStatus")
        
        if (currentStatus != InlineAudioRecorderStatus.IDLE) {
            Log.w(TAG, "startRecording: invalid status, returning false")
            return false
        }
        
        if (!requestRecordingAudioFocus()) {
            Log.e(TAG, "startRecording: failed to acquire audio focus")
            callback?.onError("Failed to acquire audio focus")
            return false
        }
        
        try {
            recordedFilePath = "${context.externalCacheDir?.absolutePath}/inline_audio_${System.currentTimeMillis()}.m4a"
            Log.d(TAG, "startRecording: file path = $recordedFilePath")
            
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
            
            timerHandler.postDelayed(timerRunnable, 0)
            amplitudeHandler.postDelayed(amplitudeRunnable, AMPLITUDE_UPDATE_INTERVAL)
            
            callback?.onStatusChange(InlineAudioRecorderStatus.RECORDING)
            Log.d(TAG, "startRecording: SUCCESS - recording started")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start recording: ${e.message}")
            callback?.onError("Failed to start recording: ${e.message}")
            currentStatus = InlineAudioRecorderStatus.ERROR
            callback?.onStatusChange(InlineAudioRecorderStatus.ERROR)
            abandonAudioFocus()
            return false
        }
    }
    
    fun pauseRecording(): Boolean {
        if (currentStatus != InlineAudioRecorderStatus.RECORDING || !isRecording || isPaused) {
            return false
        }
        
        try {
            pausedDuration += System.currentTimeMillis() - startTime
            recordingDurationMs = pausedDuration
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mediaRecorder?.pause()
            }
            
            isPaused = true
            currentStatus = InlineAudioRecorderStatus.PAUSED
            
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
    
    fun resumeRecording(): Boolean {
        if (currentStatus != InlineAudioRecorderStatus.PAUSED || !isRecording || !isPaused) {
            return false
        }
        
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mediaRecorder?.resume()
                startTime = System.currentTimeMillis()
            } else {
                // Restart recording for older APIs
                pausedDuration = 0
                recordingDurationMs = 0
                amplitudeHistory.clear()
                startTime = System.currentTimeMillis()
                
                try {
                    mediaRecorder?.stop()
                    mediaRecorder?.release()
                } catch (e: Exception) {
                    Log.e(TAG, "Error stopping old recorder: ${e.message}")
                }
                
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
            }
            
            isPaused = false
            currentStatus = InlineAudioRecorderStatus.RECORDING
            
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
    
    fun stopRecording(): File? {
        Log.d(TAG, "stopRecording: called, currentStatus = $currentStatus")
        
        if (currentStatus != InlineAudioRecorderStatus.RECORDING && 
            currentStatus != InlineAudioRecorderStatus.PAUSED) {
            Log.w(TAG, "stopRecording: invalid status, returning null")
            return null
        }
        
        try {
            if (!isPaused) {
                recordingDurationMs = pausedDuration + (System.currentTimeMillis() - startTime)
            }
            Log.d(TAG, "stopRecording: recordingDurationMs = $recordingDurationMs")
            
            // Stop and release MediaRecorder - this finalizes the file
            Log.d(TAG, "stopRecording: stopping MediaRecorder...")
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            isRecording = false
            isPaused = false
            Log.d(TAG, "stopRecording: MediaRecorder stopped and released")
            
            timerHandler.removeCallbacks(timerRunnable)
            amplitudeHandler.removeCallbacks(amplitudeRunnable)
            
            currentStatus = InlineAudioRecorderStatus.COMPLETED
            callback?.onStatusChange(InlineAudioRecorderStatus.COMPLETED)
            
            recordedFilePath?.let { path ->
                Log.d(TAG, "stopRecording: checking file at path = $path")
                val file = File(path)
                
                // Check file immediately
                Log.d(TAG, "stopRecording: immediate check - exists = ${file.exists()}, size = ${file.length()}")
                
                // Wait for file to be fully written to disk (up to 500ms)
                var attempts = 0
                while (attempts < 10 && (!file.exists() || file.length() == 0L)) {
                    Log.d(TAG, "stopRecording: waiting for file, attempt ${attempts + 1}")
                    try {
                        Thread.sleep(50)
                    } catch (e: InterruptedException) {
                        Log.w(TAG, "stopRecording: sleep interrupted")
                        break
                    }
                    attempts++
                    Log.d(TAG, "stopRecording: after wait - exists = ${file.exists()}, size = ${file.length()}")
                }
                
                if (file.exists() && file.length() > 0) {
                    Log.d(TAG, "stopRecording: SUCCESS - file ready, size = ${file.length()} bytes, duration = ${recordingDurationMs}ms")
                    callback?.onRecordingComplete(file, recordingDurationMs, amplitudeHistory.toList())
                    return file
                } else {
                    Log.e(TAG, "stopRecording: FAILED - file is empty or doesn't exist after ${attempts} attempts. exists = ${file.exists()}, size = ${file.length()}")
                }
            } ?: Log.e(TAG, "stopRecording: recordedFilePath is null")
            
            return null
        } catch (e: Exception) {
            Log.e(TAG, "stopRecording: exception - ${e.message}", e)
            callback?.onError("Failed to stop recording: ${e.message}")
            currentStatus = InlineAudioRecorderStatus.ERROR
            callback?.onStatusChange(InlineAudioRecorderStatus.ERROR)
            abandonAudioFocus()
            return null
        }
    }
    
    fun deleteRecording() {
        if (isRecording) {
            try {
                if (!isPaused) {
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
        
        releaseMediaPlayer()
        
        timerHandler.removeCallbacks(timerRunnable)
        amplitudeHandler.removeCallbacks(amplitudeRunnable)
        playbackHandler.removeCallbacks(playbackRunnable)
        
        recordedFilePath?.let { path ->
            try {
                val file = File(path)
                if (file.exists()) {
                    file.delete()
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
    }
    
    fun startPlayback() {
        if (currentStatus != InlineAudioRecorderStatus.COMPLETED) {
            return
        }
        
        val filePath = recordedFilePath ?: return
        
        if (!requestPlaybackAudioFocus()) {
            callback?.onError("Failed to acquire audio focus for playback")
            return
        }
        
        try {
            if (mediaPlayer != null) {
                mediaPlayer?.start()
                isPlaybackActive = true
                currentStatus = InlineAudioRecorderStatus.PLAYING
                callback?.onStatusChange(InlineAudioRecorderStatus.PLAYING)
                playbackHandler.postDelayed(playbackRunnable, PLAYBACK_POSITION_INTERVAL)
                return
            }
            
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
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start playback: ${e.message}")
            callback?.onError("Failed to start playback: ${e.message}")
            releaseMediaPlayer()
        }
    }
    
    fun pausePlayback() {
        if (!isPlaybackActive || currentStatus != InlineAudioRecorderStatus.PLAYING) {
            return
        }
        
        try {
            mediaPlayer?.pause()
            isPlaybackActive = false
            playbackHandler.removeCallbacks(playbackRunnable)
            
            currentStatus = InlineAudioRecorderStatus.COMPLETED
            callback?.onStatusChange(InlineAudioRecorderStatus.COMPLETED)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to pause playback: ${e.message}")
            callback?.onError("Failed to pause playback: ${e.message}")
        }
    }
    
    fun seekTo(positionMs: Long) {
        if (currentStatus != InlineAudioRecorderStatus.COMPLETED && 
            currentStatus != InlineAudioRecorderStatus.PLAYING) {
            return
        }
        
        val player = mediaPlayer
        if (player == null) {
            callback?.onPlaybackPositionUpdate(positionMs.coerceIn(0L, recordingDurationMs))
            return
        }
        
        try {
            val clampedPosition = positionMs.coerceIn(0L, recordingDurationMs)
            player.seekTo(clampedPosition.toInt())
            callback?.onPlaybackPositionUpdate(clampedPosition)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to seek: ${e.message}")
        }
    }
    
    private fun handlePlaybackCompletion() {
        isPlaybackActive = false
        playbackHandler.removeCallbacks(playbackRunnable)
        currentStatus = InlineAudioRecorderStatus.COMPLETED
        callback?.onStatusChange(InlineAudioRecorderStatus.COMPLETED)
        callback?.onPlaybackComplete()
        callback?.onPlaybackPositionUpdate(0)
    }
    
    private fun releaseMediaPlayer() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing MediaPlayer: ${e.message}")
        }
        mediaPlayer = null
        isPlaybackActive = false
        playbackHandler.removeCallbacks(playbackRunnable)
    }
    
    fun release() {
        deleteRecording()
    }
    
    /**
     * Marks the recording as submitted (prevents deletion on release).
     * Call this before release() if the recording was successfully submitted.
     */
    fun markAsSubmitted() {
        Log.d(TAG, "markAsSubmitted: clearing file path to prevent deletion")
        recordedFilePath = null
    }
}
