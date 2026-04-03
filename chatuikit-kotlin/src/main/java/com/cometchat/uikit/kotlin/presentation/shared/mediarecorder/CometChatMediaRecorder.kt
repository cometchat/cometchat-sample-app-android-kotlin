package com.cometchat.uikit.kotlin.presentation.shared.mediarecorder

import android.content.Context
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Chronometer
import android.widget.ImageView
import android.widget.SeekBar
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.StyleRes
import androidx.core.content.ContextCompat
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.databinding.CometchatMediaRecorderBinding
import com.cometchat.uikit.kotlin.shared.resources.utils.Utils
import com.cometchat.uikit.kotlin.theme.CometChatTheme
import com.google.android.material.card.MaterialCardView
import java.io.File

/**
 * Recording state enum for the media recorder.
 */
enum class RecordingState {
    IDLE,
    RECORDING,
    PAUSED,
    RECORDED,
    PLAYING
}

/**
 * Callback interface for media recorder events.
 */
interface MediaRecorderCallback {
    /**
     * Called when the recording is submitted.
     *
     * @param file The recorded audio file
     */
    fun onSubmit(file: File)
    
    /**
     * Called when the recording is cancelled/closed.
     */
    fun onClose()
    
    /**
     * Called when an error occurs during recording.
     *
     * @param exception The exception that occurred
     */
    fun onError(exception: Exception) {}
}

/**
 * CometChatMediaRecorder is a custom view for recording and playing audio.
 * It provides functionality for recording audio, playing recorded audio,
 * and visualizing the audio recording.
 */
class CometChatMediaRecorder @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.cometchatMediaRecorderStyle
) : MaterialCardView(context, attrs, defStyleAttr) {

    companion object {
        private val TAG = CometChatMediaRecorder::class.java.simpleName
    }

    // View Binding
    private val binding: CometchatMediaRecorderBinding

    // Recording state
    private var recordingState: RecordingState = RecordingState.IDLE
    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var recordedFilePath: String? = null

    // Timer
    private val timerHandler = Handler(Looper.getMainLooper())
    private var recordingStartTime: Long = 0
    private var pausedDuration: Long = 0

    // Audio focus
    private var audioManager: AudioManager? = null
    private var audioFocusRequest: AudioFocusRequest? = null
    private val focusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT || 
            focusChange == AudioManager.AUDIOFOCUS_LOSS) {
            if (recordingState == RecordingState.RECORDING) {
                stopRecording()
            }
        }
    }

    // Callback
    private var callback: MediaRecorderCallback? = null

    // Style properties
    @ColorInt private var containerBackgroundColor: Int = 0
    @ColorInt private var containerStrokeColor: Int = 0
    @Dimension private var containerStrokeWidth: Int = 0
    @ColorInt private var deleteIconTint: Int = 0
    @ColorInt private var recordIconTint: Int = 0
    @ColorInt private var pauseIconTint: Int = 0
    @ColorInt private var stopIconTint: Int = 0
    @ColorInt private var playIconTint: Int = 0
    @ColorInt private var submitIconTint: Int = 0
    @ColorInt private var submitButtonBackgroundColor: Int = 0
    @ColorInt private var timerTextColor: Int = 0
    @ColorInt private var visualizerColor: Int = 0

    // Auto-start recording flag
    private var autoStartRecording: Boolean = false

    init {
        binding = CometchatMediaRecorderBinding.inflate(
            LayoutInflater.from(context), this
        )
        addView(binding.root)
        Utils.initMaterialCard(this)
        setupAudioManager()
        applyStyleAttributes(attrs, defStyleAttr)
        setupClickListeners()
        updateUI()
    }

    /**
     * Sets up the audio manager for audio focus handling.
     */
    private fun setupAudioManager() {
        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()
            audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE)
                .setAudioAttributes(audioAttributes)
                .setOnAudioFocusChangeListener(focusChangeListener)
                .setAcceptsDelayedFocusGain(true)
                .build()
        }
    }

    private fun requestAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { audioManager?.requestAudioFocus(it) }
        } else {
            @Suppress("DEPRECATION")
            audioManager?.requestAudioFocus(
                focusChangeListener,
                AudioManager.STREAM_VOICE_CALL,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE
            )
        }
    }

    private fun abandonAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { audioManager?.abandonAudioFocusRequest(it) }
        } else {
            @Suppress("DEPRECATION")
            audioManager?.abandonAudioFocus(focusChangeListener)
        }
    }

    /**
     * Applies style attributes from XML.
     */
    private fun applyStyleAttributes(attrs: AttributeSet?, defStyleAttr: Int) {
        val typedArray: TypedArray = context.obtainStyledAttributes(
            attrs,
            R.styleable.CometChatMediaRecorder,
            defStyleAttr,
            R.style.CometChatMediaRecorderStyle
        )

        try {
            containerBackgroundColor = typedArray.getColor(
                R.styleable.CometChatMediaRecorder_cometchatMediaRecorderBackgroundColor,
                CometChatTheme.getBackgroundColor1(context)
            )
            containerStrokeColor = typedArray.getColor(
                R.styleable.CometChatMediaRecorder_cometchatMediaRecorderStrokeColor,
                CometChatTheme.getBorderColorLight(context)
            )
            containerStrokeWidth = typedArray.getDimensionPixelSize(
                R.styleable.CometChatMediaRecorder_cometchatMediaRecorderStrokeWidth,
                resources.getDimensionPixelSize(R.dimen.cometchat_1dp)
            )
            deleteIconTint = typedArray.getColor(
                R.styleable.CometChatMediaRecorder_cometchatMediaRecorderDeleteIconTint,
                CometChatTheme.getIconTintSecondary(context)
            )
            recordIconTint = typedArray.getColor(
                R.styleable.CometChatMediaRecorder_cometchatMediaRecorderStartIconTint,
                CometChatTheme.getErrorColor(context)
            )
            pauseIconTint = typedArray.getColor(
                R.styleable.CometChatMediaRecorder_cometchatMediaRecorderPauseIconTint,
                CometChatTheme.getIconTintSecondary(context)
            )
            stopIconTint = typedArray.getColor(
                R.styleable.CometChatMediaRecorder_cometchatMediaRecorderStopIconTint,
                CometChatTheme.getIconTintSecondary(context)
            )
            playIconTint = typedArray.getColor(
                R.styleable.CometChatMediaRecorder_cometchatMediaRecorderPlayIconTint,
                CometChatTheme.getIconTintHighlight(context)
            )
            submitIconTint = typedArray.getColor(
                R.styleable.CometChatMediaRecorder_cometchatMediaRecorderSendIconTint,
                CometChatTheme.getColorWhite(context)
            )
            submitButtonBackgroundColor = typedArray.getColor(
                R.styleable.CometChatMediaRecorder_cometchatMediaRecorderSendIconBackgroundColor,
                CometChatTheme.getIconTintHighlight(context)
            )
            timerTextColor = typedArray.getColor(
                R.styleable.CometChatMediaRecorder_cometchatMediaRecorderTextColor,
                CometChatTheme.getTextColorSecondary(context)
            )
            visualizerColor = typedArray.getColor(
                R.styleable.CometChatMediaRecorder_cometchatMediaRecorderVisualizerColor,
                CometChatTheme.getIconTintSecondary(context)
            )
        } finally {
            typedArray.recycle()
        }

        applyStyles()
    }

    /**
     * Applies the parsed style properties to the views.
     */
    private fun applyStyles() {
        // Recording container
        binding.recordingContainer.setCardBackgroundColor(containerBackgroundColor)
        binding.recordingContainer.strokeColor = containerStrokeColor
        binding.recordingContainer.strokeWidth = containerStrokeWidth

        // Recorded container
        binding.recordedContainer.setCardBackgroundColor(containerBackgroundColor)
        binding.recordedContainer.strokeColor = containerStrokeColor
        binding.recordedContainer.strokeWidth = containerStrokeWidth

        // Icons
        binding.deleteButton.setColorFilter(deleteIconTint)
        binding.recordButton.setColorFilter(recordIconTint)
        binding.pauseRecordingButton.setColorFilter(pauseIconTint)
        binding.stopButton.setColorFilter(stopIconTint)
        binding.submitButton.setColorFilter(submitIconTint)
        binding.submitButtonContainer.setCardBackgroundColor(submitButtonBackgroundColor)

        // Recorded state icons
        binding.recordedDeleteButton.setColorFilter(deleteIconTint)
        binding.playButton.setColorFilter(playIconTint)
        binding.pauseButton.setColorFilter(pauseIconTint)
        binding.rerecordButton.setColorFilter(deleteIconTint)
        binding.recordedSubmitButton.setColorFilter(submitIconTint)
        binding.recordedSubmitButtonContainer.setCardBackgroundColor(submitButtonBackgroundColor)

        // Timer
        binding.recordingTime.setTextColor(timerTextColor)
        binding.recordedTime.setTextColor(timerTextColor)

        // Visualizer
        binding.audioRecordingView.setBarColor(visualizerColor)
        binding.recordedWaveform.setBarColor(visualizerColor)
    }

    /**
     * Sets up click listeners for control buttons.
     */
    private fun setupClickListeners() {
        // Recording state buttons
        binding.deleteButton.setOnClickListener { deleteRecording() }
        binding.recordButton.setOnClickListener { startRecording() }
        binding.pauseRecordingButton.setOnClickListener { pauseRecording() }
        binding.stopButton.setOnClickListener { stopRecording() }
        binding.submitButtonContainer.setOnClickListener { submitRecording() }

        // Recorded state buttons
        binding.recordedDeleteButton.setOnClickListener { deleteRecording() }
        binding.playButton.setOnClickListener { playRecording() }
        binding.pauseButton.setOnClickListener { pausePlayback() }
        binding.rerecordButton.setOnClickListener { rerecord() }
        binding.recordedSubmitButtonContainer.setOnClickListener { submitRecording() }
    }

    /**
     * Updates the UI based on the current recording state.
     */
    private fun updateUI() {
        when (recordingState) {
            RecordingState.IDLE -> {
                binding.recordingContainer.visibility = View.VISIBLE
                binding.recordedContainer.visibility = View.GONE
                binding.deleteButton.visibility = View.GONE
                binding.recordButton.visibility = View.VISIBLE
                binding.recordingIndicator.visibility = View.GONE
                binding.pauseRecordingButton.visibility = View.GONE
                binding.stopButton.visibility = View.GONE
                binding.recordingTime.stop()
                binding.recordingTime.base = SystemClock.elapsedRealtime()
            }
            RecordingState.RECORDING -> {
                binding.recordingContainer.visibility = View.VISIBLE
                binding.recordedContainer.visibility = View.GONE
                binding.deleteButton.visibility = View.VISIBLE
                binding.recordButton.visibility = View.GONE
                binding.recordingIndicator.visibility = View.VISIBLE
                binding.pauseRecordingButton.visibility = View.VISIBLE
                binding.stopButton.visibility = View.VISIBLE
            }
            RecordingState.PAUSED -> {
                binding.recordingContainer.visibility = View.VISIBLE
                binding.recordedContainer.visibility = View.GONE
                binding.deleteButton.visibility = View.VISIBLE
                binding.recordButton.visibility = View.VISIBLE
                binding.recordingIndicator.visibility = View.GONE
                binding.pauseRecordingButton.visibility = View.GONE
                binding.stopButton.visibility = View.VISIBLE
            }
            RecordingState.RECORDED -> {
                binding.recordingContainer.visibility = View.GONE
                binding.recordedContainer.visibility = View.VISIBLE
                binding.playButton.visibility = View.VISIBLE
                binding.pauseButton.visibility = View.GONE
            }
            RecordingState.PLAYING -> {
                binding.recordingContainer.visibility = View.GONE
                binding.recordedContainer.visibility = View.VISIBLE
                binding.playButton.visibility = View.GONE
                binding.pauseButton.visibility = View.VISIBLE
            }
        }
    }

    /**
     * Starts audio recording.
     */
    fun startRecording() {
        try {
            requestAudioFocus()
            
            recordedFilePath = "${context.externalCacheDir?.absolutePath}/audio_record_${System.currentTimeMillis()}.m4a"
            
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }
            
            mediaRecorder?.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setOutputFile(recordedFilePath)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                prepare()
                start()
            }

            recordingStartTime = SystemClock.elapsedRealtime()
            binding.recordingTime.base = recordingStartTime
            binding.recordingTime.start()
            
            recordingState = RecordingState.RECORDING
            updateUI()
            startVisualizerUpdate()

            Log.d(TAG, "Recording started: $recordedFilePath")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start recording: ${e.message}")
            callback?.onError(e)
            recordingState = RecordingState.IDLE
            updateUI()
        }
    }

    /**
     * Pauses audio recording.
     */
    private fun pauseRecording() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mediaRecorder?.pause()
                pausedDuration = SystemClock.elapsedRealtime() - binding.recordingTime.base
                binding.recordingTime.stop()
                recordingState = RecordingState.PAUSED
                updateUI()
                Log.d(TAG, "Recording paused")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to pause recording: ${e.message}")
        }
    }

    /**
     * Resumes audio recording from paused state.
     */
    private fun resumeRecording() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mediaRecorder?.resume()
                binding.recordingTime.base = SystemClock.elapsedRealtime() - pausedDuration
                binding.recordingTime.start()
                recordingState = RecordingState.RECORDING
                updateUI()
                Log.d(TAG, "Recording resumed")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to resume recording: ${e.message}")
        }
    }

    /**
     * Stops audio recording.
     */
    private fun stopRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            binding.recordingTime.stop()
            stopVisualizerUpdate()
            
            // Set recorded duration
            recordedFilePath?.let { path ->
                val player = MediaPlayer()
                player.setDataSource(path)
                player.prepare()
                val duration = player.duration
                player.release()
                binding.recordedTime.base = SystemClock.elapsedRealtime() - duration
            }
            
            recordingState = RecordingState.RECORDED
            updateUI()

            Log.d(TAG, "Recording stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop recording: ${e.message}")
            recordingState = RecordingState.IDLE
            updateUI()
        }
    }

    /**
     * Deletes the current recording and resets.
     */
    private fun deleteRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping recorder: ${e.message}")
        }
        
        mediaRecorder = null
        mediaPlayer?.release()
        mediaPlayer = null
        binding.recordingTime.stop()
        stopVisualizerUpdate()
        
        // Delete the recorded file
        recordedFilePath?.let { path ->
            File(path).delete()
        }
        recordedFilePath = null
        
        recordingState = RecordingState.IDLE
        updateUI()
        callback?.onClose()

        Log.d(TAG, "Recording deleted")
    }

    /**
     * Re-records (deletes current and starts fresh).
     */
    private fun rerecord() {
        mediaPlayer?.release()
        mediaPlayer = null
        
        recordedFilePath?.let { path ->
            File(path).delete()
        }
        recordedFilePath = null
        
        recordingState = RecordingState.IDLE
        updateUI()
        
        // Start new recording
        startRecording()
    }

    /**
     * Plays the recorded audio.
     */
    private fun playRecording() {
        recordedFilePath?.let { path ->
            try {
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(path)
                    prepare()
                    start()
                    setOnCompletionListener {
                        recordingState = RecordingState.RECORDED
                        updateUI()
                    }
                }
                recordingState = RecordingState.PLAYING
                updateUI()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to play recording: ${e.message}")
                callback?.onError(e)
            }
        }
    }

    /**
     * Pauses playback.
     */
    private fun pausePlayback() {
        mediaPlayer?.pause()
        recordingState = RecordingState.RECORDED
        updateUI()
    }

    /**
     * Submits the recorded audio.
     */
    private fun submitRecording() {
        mediaPlayer?.release()
        mediaPlayer = null
        
        recordedFilePath?.let { path ->
            val file = File(path)
            if (file.exists()) {
                callback?.onSubmit(file)
            } else {
                callback?.onError(Exception("Recorded file not found"))
            }
        } ?: run {
            callback?.onError(Exception("No recording available"))
        }
        
        // Reset state
        recordedFilePath = null
        recordingState = RecordingState.IDLE
        updateUI()
        callback?.onClose()
    }

    private val visualizerRunnable = object : Runnable {
        override fun run() {
            if (recordingState == RecordingState.RECORDING) {
                mediaRecorder?.let { recorder ->
                    val amplitude = recorder.maxAmplitude
                    val normalizedAmplitude = (amplitude / 32767f).coerceIn(0f, 1f)
                    binding.audioRecordingView.setAmplitude(normalizedAmplitude)
                }
                timerHandler.postDelayed(this, 100)
            }
        }
    }

    private fun startVisualizerUpdate() {
        timerHandler.post(visualizerRunnable)
    }

    private fun stopVisualizerUpdate() {
        timerHandler.removeCallbacks(visualizerRunnable)
    }

    // Public API methods

    /**
     * Sets the callback for media recorder events.
     */
    fun setCallback(callback: MediaRecorderCallback) {
        this.callback = callback
    }

    /**
     * Applies a style to the media recorder.
     */
    fun setStyle(style: CometChatMediaRecorderStyle) {
        if (style.backgroundColor != 0) {
            containerBackgroundColor = style.backgroundColor
        }
        if (style.strokeColor != 0) {
            containerStrokeColor = style.strokeColor
        }
        if (style.strokeWidth != 0) {
            containerStrokeWidth = style.strokeWidth
        }
        if (style.deleteIconColor != 0) {
            deleteIconTint = style.deleteIconColor
        }
        if (style.startIconTint != 0) {
            recordIconTint = style.startIconTint
        }
        if (style.pauseIconTint != 0) {
            pauseIconTint = style.pauseIconTint
        }
        if (style.stopIconTint != 0) {
            stopIconTint = style.stopIconTint
        }
        if (style.playIconTint != 0) {
            playIconTint = style.playIconTint
        }
        if (style.submitIconTint != 0) {
            submitIconTint = style.submitIconTint
        }
        if (style.submitButtonBackgroundColor != 0) {
            submitButtonBackgroundColor = style.submitButtonBackgroundColor
        }
        if (style.timerTextColor != 0) {
            timerTextColor = style.timerTextColor
        }
        if (style.recordingChunkColor != 0) {
            visualizerColor = style.recordingChunkColor
        }

        applyStyles()
    }

    /**
     * Sets whether recording should start automatically when the view is attached.
     */
    fun setAutoStartRecording(autoStart: Boolean) {
        this.autoStartRecording = autoStart
    }

    /**
     * Returns whether auto-start recording is enabled.
     */
    fun isAutoStartRecordingEnabled(): Boolean {
        return autoStartRecording
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (autoStartRecording && recordingState == RecordingState.IDLE) {
            post { startRecording() }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing recorder: ${e.message}")
        }
        mediaRecorder = null
        
        mediaPlayer?.release()
        mediaPlayer = null
        
        stopVisualizerUpdate()
        abandonAudioFocus()
    }
}
