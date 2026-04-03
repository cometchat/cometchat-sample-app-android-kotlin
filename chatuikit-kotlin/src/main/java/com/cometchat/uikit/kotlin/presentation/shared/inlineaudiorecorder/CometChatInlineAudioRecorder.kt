package com.cometchat.uikit.kotlin.presentation.shared.inlineaudiorecorder

import android.Manifest
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.findViewTreeLifecycleOwner
import com.cometchat.uikit.core.viewmodel.CometChatInlineAudioRecorderViewModel
import com.cometchat.uikit.core.viewmodel.InlineAudioRecorderStatus
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.presentation.shared.permission.CometChatPermissionHandler
import com.cometchat.uikit.kotlin.presentation.shared.permission.PermissionType
import com.cometchat.uikit.kotlin.presentation.shared.permission.listener.PermissionResultListener
import com.cometchat.uikit.kotlin.theme.CometChatTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File

/**
 * CometChatInlineAudioRecorder provides inline audio recording within the message composer.
 *
 * This view implements a six-state media recorder:
 * - **IDLE**: Not visible (composer shows normal input)
 * - **RECORDING**: Actively recording, shows animated waveform
 * - **PAUSED**: Recording paused, shows static waveform with resume option
 * - **COMPLETED**: Recording complete, shows playback controls
 * - **PLAYING**: Playing back, shows progress on waveform
 * - **ERROR**: Error state with recovery option
 *
 * Layout structure (single row):
 * [Delete] [Record/Play] [Waveform] [Duration] [Pause/Mic] [Send]
 *
 * Features:
 * - Real-time waveform visualization during recording (bars scroll left)
 * - Pause/Resume recording (API 24+ for true pause)
 * - Playback with waveform progress visualization
 * - Seeking via tap or drag on waveform
 * - Amplitude storage for playback visualization
 * - Full style customization
 *
 * **Validates: Requirements 1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 1.7, 1.8, 1.9, 1.10, 1.11, 1.12, 1.13, 1.14, 1.15, 2.1, 14.1, 14.5**
 */
class CometChatInlineAudioRecorder @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), DefaultLifecycleObserver {

    // ==================== Views ====================
    
    private val container: View
    private val btnDelete: ImageView
    private val recordPlayContainer: FrameLayout
    private val recordingIndicator: View
    private val btnPlay: ImageView
    private val btnPausePlayback: ImageView
    private val waveform: CometChatInlineAudioWaveform
    private val tvDuration: TextView
    private val pauseMicContainer: FrameLayout
    private val btnPauseRecording: ImageView
    private val btnMic: ImageView
    private val sendButtonCard: com.google.android.material.card.MaterialCardView
    private val btnSend: ImageView
    
    // ==================== State ====================
    
    private var viewModel: CometChatInlineAudioRecorderViewModel? = null
    private var recorderManager: InlineAudioRecorderManager? = null
    private var style: CometChatInlineAudioRecorderStyle = CometChatInlineAudioRecorderStyle.default(context)
    
    // ==================== Coroutines ====================
    
    private var coroutineScope: CoroutineScope? = null
    private var stateCollectionJob: Job? = null
    
    // ==================== Animation ====================
    
    private var pulseAnimator: ObjectAnimator? = null
    private var isSlideAnimationInProgress = false
    
    // ==================== Callbacks ====================
    
    private var onSubmitListener: ((File) -> Unit)? = null
    private var onCancelListener: (() -> Unit)? = null
    private var onErrorListener: ((String) -> Unit)? = null
    
    companion object {
        private const val TAG = "InlineAudioRecorder"
    }
    
    init {
        // Inflate layout
        LayoutInflater.from(context).inflate(R.layout.cometchat_inline_audio_recorder, this, true)
        
        // Find views
        container = findViewById(R.id.inline_recorder_container)
        btnDelete = findViewById(R.id.btn_delete)
        recordPlayContainer = findViewById(R.id.record_play_container)
        recordingIndicator = findViewById(R.id.recording_indicator)
        btnPlay = findViewById(R.id.btn_play)
        btnPausePlayback = findViewById(R.id.btn_pause_playback)
        waveform = findViewById(R.id.waveform)
        tvDuration = findViewById(R.id.tv_duration)
        pauseMicContainer = findViewById(R.id.pause_mic_container)
        btnPauseRecording = findViewById(R.id.btn_pause_recording)
        btnMic = findViewById(R.id.btn_mic)
        sendButtonCard = findViewById(R.id.send_button_card)
        btnSend = findViewById(R.id.btn_send)
        
        // Set up click listeners
        setupClickListeners()
        
        // Apply default style
        applyStyle(style)
        
        // Initially hidden
        visibility = GONE
    }
    
    // ==================== Public API ====================
    
    /**
     * Sets the ViewModel for state management.
     * @param viewModel The shared ViewModel
     */
    fun setViewModel(viewModel: CometChatInlineAudioRecorderViewModel) {
        this.viewModel = viewModel
        startStateCollection()
    }
    
    /**
     * Sets the recorder manager for audio operations.
     * @param manager The InlineAudioRecorderManager
     */
    fun setRecorderManager(manager: InlineAudioRecorderManager) {
        this.recorderManager = manager
        setupRecorderCallback()
    }
    
    /**
     * Applies a style to the recorder.
     * @param style The style to apply
     */
    fun applyStyle(style: CometChatInlineAudioRecorderStyle) {
        this.style = style
        
        // Apply container styling
        val backgroundDrawable = GradientDrawable().apply {
            setColor(style.backgroundColor)
            cornerRadius = style.cornerRadius.toFloat()
            if (style.strokeWidth > 0) {
                setStroke(style.strokeWidth, style.strokeColor)
            }
        }
        container.background = backgroundDrawable
        
        // Apply waveform styling
        waveform.applyStyle(style)
        
        // Apply duration text styling
        tvDuration.setTextColor(style.durationTextColor)
        if (style.durationTextAppearance != 0) {
            tvDuration.setTextAppearance(style.durationTextAppearance)
        }
        
        // Apply button colors
        btnDelete.setColorFilter(style.deleteButtonIconColor)
        btnPlay.setColorFilter(style.playButtonIconColor)
        btnPausePlayback.setColorFilter(style.pauseButtonIconColor)
        btnPauseRecording.setColorFilter(style.pauseButtonIconColor)
        btnMic.setColorFilter(style.micButtonIconColor)
        btnSend.setColorFilter(style.sendButtonIconColor)
        
        // Apply send button card background (matches MessageComposer send button)
        sendButtonCard.setCardBackgroundColor(style.sendButtonBackgroundColor)
        
        // Apply recording indicator color
        val indicatorBackground = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(style.recordingIndicatorColor)
        }
        recordingIndicator.background = indicatorBackground
    }
    
    /**
     * Sets the submit callback.
     * @param listener Callback with the recorded file
     */
    fun setOnSubmitListener(listener: (File) -> Unit) {
        onSubmitListener = listener
    }
    
    /**
     * Sets the cancel callback.
     * @param listener Callback when recording is cancelled
     */
    fun setOnCancelListener(listener: () -> Unit) {
        onCancelListener = listener
    }
    
    /**
     * Sets the error callback.
     * @param listener Callback with error message
     */
    fun setOnErrorListener(listener: (String) -> Unit) {
        onErrorListener = listener
    }
    
    /**
     * Starts recording from IDLE state.
     * Checks for RECORD_AUDIO permission before starting.
     * If permission is not granted, requests it using CometChatPermissionHandler.
     * 
     * The permission handler uses a separate transparent Activity to request permissions,
     * which allows it to work from any context without requiring ActivityResultLauncher
     * registration in onCreate.
     * 
     * @return true if recording started successfully or permission request was initiated
     */
    fun startRecording(): Boolean {
        val vm = viewModel ?: return false
        val manager = recorderManager ?: return false
        
        if (vm.status != InlineAudioRecorderStatus.IDLE) {
            return false
        }
        
        // Check if permission is already granted
        if (CometChatPermissionHandler.arePermissionsGranted(context, PermissionType.MICROPHONE)) {
            return startRecordingInternal()
        }
        
        // Request permission using the Activity-based handler
        Log.d(TAG, "RECORD_AUDIO permission not granted, requesting via CometChatPermissionHandler...")
        CometChatPermissionHandler.withContext(context)
            .withPermissions(CometChatPermissionHandler.getPermissionsForType(PermissionType.MICROPHONE))
            .withListener(object : PermissionResultListener {
                override fun permissionResult(grantedPermissions: List<String>, deniedPermissions: List<String>) {
                    if (deniedPermissions.isEmpty()) {
                        startRecordingInternal()
                    } else {
                        Log.e(TAG, "RECORD_AUDIO permission denied: $deniedPermissions")
                        onErrorListener?.invoke("RECORD_AUDIO permission denied")
                    }
                }
            })
            .check()
        
        return true // Return true to indicate we're handling it
    }
    
    /**
     * Internal method to start recording after permission is granted.
     */
    private fun startRecordingInternal(): Boolean {
        val vm = viewModel ?: return false
        val manager = recorderManager ?: return false
        
        val started = manager.startRecording()
        if (started) {
            vm.startRecording()
        }
        return started
    }
    
    // ==================== Private Methods ====================
    
    private fun setupClickListeners() {
        btnDelete.setOnClickListener {
            // Slide left towards delete button and then trigger callback
            hideWithSlideLeftAnimation()
            recorderManager?.deleteRecording()
            viewModel?.deleteRecording()
            onCancelListener?.invoke()
        }
        
        btnPlay.setOnClickListener {
            val vm = viewModel ?: return@setOnClickListener
            val manager = recorderManager ?: return@setOnClickListener
            
            if (vm.status == InlineAudioRecorderStatus.PAUSED) {
                // Stop recording first to finalize the file, then start playback
                manager.stopRecording()
                vm.stopRecording()
                // Start playback after stopping
                manager.startPlayback()
                vm.startPlayback()
            } else if (vm.status == InlineAudioRecorderStatus.COMPLETED) {
                manager.startPlayback()
                vm.startPlayback()
            }
        }
        
        btnPausePlayback.setOnClickListener {
            val vm = viewModel ?: return@setOnClickListener
            if (vm.status == InlineAudioRecorderStatus.PLAYING) {
                recorderManager?.pausePlayback()
                vm.pausePlayback()
            }
        }
        
        btnPauseRecording.setOnClickListener {
            val vm = viewModel ?: return@setOnClickListener
            if (vm.status == InlineAudioRecorderStatus.RECORDING) {
                recorderManager?.pauseRecording()
                vm.pauseRecording()
            }
        }
        
        btnMic.setOnClickListener {
            val vm = viewModel ?: return@setOnClickListener
            if (vm.status == InlineAudioRecorderStatus.PAUSED) {
                recorderManager?.resumeRecording()
                vm.resumeRecording()
            }
        }
        
        btnSend.setOnClickListener {
            handleSendClick()
        }
        
        // Also set click listener on the card for better touch target
        sendButtonCard.setOnClickListener {
            handleSendClick()
        }
        
        // Set up waveform seeking
        waveform.setOnSeekListener { progress ->
            val vm = viewModel ?: return@setOnSeekListener
            val manager = recorderManager ?: return@setOnSeekListener
            
            if (vm.status == InlineAudioRecorderStatus.COMPLETED ||
                vm.status == InlineAudioRecorderStatus.PLAYING) {
                val seekPositionMs = (progress * vm.duration).toLong()
                manager.seekTo(seekPositionMs)
                vm.seekTo(seekPositionMs)
            }
        }
    }
    
    /**
     * Handles the send button click action.
     * Stops recording if still in progress, plays slide-right animation, and submits the recorded file.
     */
    private fun handleSendClick() {
        val vm = viewModel ?: run {
            Log.e(TAG, "handleSendClick: viewModel is null")
            return
        }
        val manager = recorderManager ?: run {
            Log.e(TAG, "handleSendClick: recorderManager is null")
            return
        }
        
        Log.d(TAG, "handleSendClick: current status = ${vm.status}")
        
        var recordedFile: File? = null
        
        // Stop recording BEFORE animation to ensure file is finalized
        if (vm.status == InlineAudioRecorderStatus.RECORDING ||
            vm.status == InlineAudioRecorderStatus.PAUSED) {
            Log.d(TAG, "handleSendClick: stopping recording...")
            // stopRecording() now waits for file to be written and returns it
            recordedFile = manager.stopRecording()
            vm.stopRecording()
            Log.d(TAG, "handleSendClick: stopRecording returned file = ${recordedFile?.absolutePath}, exists = ${recordedFile?.exists()}, size = ${recordedFile?.length()}")
        } else {
            // Already completed, get the existing file
            recordedFile = manager.getRecordedFile()
            Log.d(TAG, "handleSendClick: already completed, getRecordedFile returned = ${recordedFile?.absolutePath}, exists = ${recordedFile?.exists()}, size = ${recordedFile?.length()}")
        }
        
        // Slide right towards send button and then submit
        hideWithSlideRightAnimation()
        
        // Submit the file only if it exists and has content
        if (recordedFile != null && recordedFile.exists() && recordedFile.length() > 0) {
            Log.d(TAG, "handleSendClick: invoking onSubmitListener with file size = ${recordedFile.length()}")
            // Mark as submitted BEFORE invoking listener to prevent file deletion during cleanup
            manager.markAsSubmitted()
            onSubmitListener?.invoke(recordedFile)
        } else {
            Log.e(TAG, "handleSendClick: file is null, doesn't exist, or has 0 bytes. file = $recordedFile, exists = ${recordedFile?.exists()}, size = ${recordedFile?.length()}")
        }
    }
    
    private fun setupRecorderCallback() {
        recorderManager?.setCallback(object : InlineAudioRecorderCallback {
            override fun onDurationUpdate(durationMs: Long) {
                viewModel?.updateDuration(durationMs)
            }
            
            override fun onAmplitudeUpdate(amplitude: Float) {
                viewModel?.addAmplitude(amplitude)
            }
            
            override fun onStatusChange(status: InlineAudioRecorderStatus) {
                // Status changes are handled by ViewModel
            }
            
            override fun onRecordingComplete(file: File, durationMs: Long, amplitudes: List<Float>) {
                viewModel?.setFilePath(file.absolutePath)
                viewModel?.updateDuration(durationMs)
            }
            
            override fun onPlaybackPositionUpdate(positionMs: Long) {
                viewModel?.updatePosition(positionMs)
            }
            
            override fun onPlaybackComplete() {
                viewModel?.onPlaybackComplete()
            }
            
            override fun onError(message: String) {
                viewModel?.handleError(message)
                onErrorListener?.invoke(message)
            }
        })
    }
    
    private fun startStateCollection() {
        stateCollectionJob?.cancel()
        
        if (coroutineScope == null) {
            coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        }
        
        stateCollectionJob = coroutineScope?.launch {
            viewModel?.state?.collectLatest { state ->
                updateUI(state.status, state.duration, state.currentPosition, state.amplitudes)
            }
        }
    }
    
    private fun updateUI(
        status: InlineAudioRecorderStatus,
        duration: Long,
        currentPosition: Long,
        amplitudes: List<Float>
    ) {
        // Update visibility with animation
        val shouldBeVisible = status != InlineAudioRecorderStatus.IDLE
        val isCurrentlyVisible = visibility == VISIBLE
        
        if (shouldBeVisible && !isCurrentlyVisible) {
            // Show with animation
            showWithAnimation()
        } else if (!shouldBeVisible && isCurrentlyVisible && !isSlideAnimationInProgress) {
            // Hide with animation (only if no slide animation is in progress)
            hideWithAnimation()
        }
        
        // Update waveform
        waveform.setAmplitudes(amplitudes)
        waveform.setRecording(status == InlineAudioRecorderStatus.RECORDING)
        waveform.setPlaying(status == InlineAudioRecorderStatus.PLAYING)
        
        // Calculate progress for playback
        val progress = if (duration > 0) {
            (currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }
        waveform.setProgress(progress)
        
        // Update duration text
        val displayTime = when (status) {
            InlineAudioRecorderStatus.PLAYING -> viewModel?.formattedPosition ?: "00:00"
            else -> viewModel?.formattedDuration ?: "00:00"
        }
        tvDuration.text = displayTime
        
        // Update button visibility based on status
        updateButtonVisibility(status)
        
        // Update recording indicator animation
        updateRecordingIndicator(status)
        
        // Update accessibility
        updateAccessibility(status, displayTime)
    }
    
    private fun updateButtonVisibility(status: InlineAudioRecorderStatus) {
        val isPauseResumeSupported = recorderManager?.isPauseResumeSupported() ?: false
        
        when (status) {
            InlineAudioRecorderStatus.RECORDING -> {
                recordingIndicator.visibility = VISIBLE
                btnPlay.visibility = GONE
                btnPausePlayback.visibility = GONE
                btnPauseRecording.visibility = if (isPauseResumeSupported) VISIBLE else GONE
                btnMic.visibility = if (isPauseResumeSupported) GONE else VISIBLE
                btnMic.alpha = if (isPauseResumeSupported) 1f else 0.4f
            }
            InlineAudioRecorderStatus.PAUSED -> {
                recordingIndicator.visibility = GONE
                btnPlay.visibility = VISIBLE
                btnPausePlayback.visibility = GONE
                btnPauseRecording.visibility = GONE
                btnMic.visibility = VISIBLE
                btnMic.alpha = 1f
            }
            InlineAudioRecorderStatus.COMPLETED -> {
                recordingIndicator.visibility = GONE
                btnPlay.visibility = VISIBLE
                btnPausePlayback.visibility = GONE
                btnPauseRecording.visibility = GONE
                btnMic.visibility = VISIBLE
                btnMic.alpha = 0.4f
            }
            InlineAudioRecorderStatus.PLAYING -> {
                recordingIndicator.visibility = GONE
                btnPlay.visibility = GONE
                btnPausePlayback.visibility = VISIBLE
                btnPauseRecording.visibility = GONE
                btnMic.visibility = VISIBLE
                btnMic.alpha = 0.4f
            }
            else -> {
                recordingIndicator.visibility = GONE
                btnPlay.visibility = GONE
                btnPausePlayback.visibility = GONE
                btnPauseRecording.visibility = GONE
                btnMic.visibility = GONE
            }
        }
    }
    
    private fun updateRecordingIndicator(status: InlineAudioRecorderStatus) {
        if (status == InlineAudioRecorderStatus.RECORDING) {
            startPulseAnimation()
        } else {
            stopPulseAnimation()
        }
    }
    
    private fun startPulseAnimation() {
        if (pulseAnimator?.isRunning == true) return
        
        pulseAnimator = ObjectAnimator.ofFloat(recordingIndicator, "alpha", 1f, 0.3f).apply {
            duration = 800
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            start()
        }
    }
    
    private fun stopPulseAnimation() {
        pulseAnimator?.cancel()
        pulseAnimator = null
        recordingIndicator.alpha = 1f
    }
    
    /**
     * Shows the recorder with a smooth slide-in + fade animation.
     * Slides in from the bottom with a fade effect.
     */
    private fun showWithAnimation() {
        if (visibility == VISIBLE) return
        
        visibility = VISIBLE
        alpha = 0f
        translationY = height.toFloat() * 0.3f  // Start slightly below
        scaleY = 0.95f
        
        animate()
            .alpha(1f)
            .translationY(0f)
            .scaleY(1f)
            .setDuration(250)
            .setInterpolator(android.view.animation.DecelerateInterpolator())
            .start()
    }
    
    /**
     * Hides the recorder with a smooth collapse + fade animation.
     */
    private fun hideWithAnimation() {
        if (visibility != VISIBLE) return
        
        animate()
            .alpha(0f)
            .translationY(height.toFloat() * 0.3f)
            .scaleY(0.95f)
            .setDuration(200)
            .setInterpolator(android.view.animation.AccelerateInterpolator())
            .withEndAction {
                visibility = GONE
                // Reset for next show
                alpha = 1f
                scaleY = 1f
                translationX = 0f
                translationY = 0f
            }
            .start()
    }
    
    /**
     * Hides the recorder with a slide-right animation (towards send button).
     * Used when the send button is clicked.
     */
    private fun hideWithSlideRightAnimation() {
        if (visibility != VISIBLE) return
        
        // Set flag to prevent updateUI from overriding this animation
        isSlideAnimationInProgress = true
        
        // Use measured width or fallback to a reasonable default
        val slideDistance = if (width > 0) width.toFloat() else 500f
        
        animate()
            .alpha(0f)
            .translationX(slideDistance)
            .setDuration(250)
            .setInterpolator(android.view.animation.AccelerateInterpolator())
            .withEndAction {
                visibility = GONE
                // Reset for next show
                alpha = 1f
                translationX = 0f
                isSlideAnimationInProgress = false
            }
            .start()
    }
    
    /**
     * Hides the recorder with a slide-left animation (towards delete button).
     * Used when the delete button is clicked.
     */
    private fun hideWithSlideLeftAnimation() {
        if (visibility != VISIBLE) return
        
        // Set flag to prevent updateUI from overriding this animation
        isSlideAnimationInProgress = true
        
        // Use measured width or fallback to a reasonable default
        val slideDistance = if (width > 0) width.toFloat() else 500f
        
        animate()
            .alpha(0f)
            .translationX(-slideDistance)
            .setDuration(250)
            .setInterpolator(android.view.animation.AccelerateInterpolator())
            .withEndAction {
                visibility = GONE
                // Reset for next show
                alpha = 1f
                translationX = 0f
                isSlideAnimationInProgress = false
            }
            .start()
    }
    
    private fun updateAccessibility(status: InlineAudioRecorderStatus, displayTime: String) {
        contentDescription = when (status) {
            InlineAudioRecorderStatus.RECORDING -> "Recording in progress, $displayTime elapsed"
            InlineAudioRecorderStatus.PAUSED -> "Recording paused at $displayTime"
            InlineAudioRecorderStatus.COMPLETED -> "Recording complete, $displayTime duration"
            InlineAudioRecorderStatus.PLAYING -> "Playing recording, $displayTime"
            InlineAudioRecorderStatus.ERROR -> "Recording error"
            else -> "Audio recorder"
        }
    }
    
    // ==================== Lifecycle ====================
    
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        findViewTreeLifecycleOwner()?.lifecycle?.addObserver(this)
        
        if (coroutineScope == null) {
            coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        }
        
        if (viewModel != null) {
            startStateCollection()
        }
    }
    
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        findViewTreeLifecycleOwner()?.lifecycle?.removeObserver(this)
        cleanup()
    }
    
    override fun onDestroy(owner: LifecycleOwner) {
        cleanup()
    }
    
    private fun cleanup() {
        stateCollectionJob?.cancel()
        stateCollectionJob = null
        coroutineScope?.cancel()
        coroutineScope = null
        stopPulseAnimation()
        recorderManager?.release()
        viewModel?.release()
    }
}
