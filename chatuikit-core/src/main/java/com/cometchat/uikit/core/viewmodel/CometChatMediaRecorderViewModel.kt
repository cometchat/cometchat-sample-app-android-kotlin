package com.cometchat.uikit.core.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

/**
 * Shared ViewModel for CometChatMediaRecorder.
 * Manages recording state, timer, amplitude, and playback.
 *
 * This ViewModel is shared by both chatuikit-jetpack (Compose) and chatuikit-kotlin (Views)
 * implementations, ensuring consistent behavior across both UI frameworks.
 *
 * Features:
 * - Three-state machine management (IDLE, RECORDING, RECORDED)
 * - Recording time tracking with formatted display
 * - Audio amplitude for visualizer
 * - Playback progress and state management
 * - Recorded file reference
 *
 * State Machine:
 * ```
 * IDLE → RECORDING (startRecording)
 * RECORDING → RECORDED (stopRecording)
 * RECORDING → IDLE (deleteRecording, error)
 * RECORDED → IDLE (deleteRecording)
 * ```
 *
 * @see MediaRecorderState
 */
open class CometChatMediaRecorderViewModel : ViewModel() {

    // ==================== Recording State ====================

    /**
     * Current state of the media recorder.
     * Valid states: IDLE, RECORDING, RECORDED
     */
    private val _recordingState = MutableStateFlow(MediaRecorderState.IDLE)
    val recordingState: StateFlow<MediaRecorderState> = _recordingState.asStateFlow()

    // ==================== Timer State ====================

    /**
     * Current recording time formatted as MM:SS.
     */
    private val _recordingTime = MutableStateFlow("00:00")
    val recordingTime: StateFlow<String> = _recordingTime.asStateFlow()

    /**
     * Recording duration in milliseconds.
     */
    private val _recordingDurationMs = MutableStateFlow(0L)
    val recordingDurationMs: StateFlow<Long> = _recordingDurationMs.asStateFlow()

    // ==================== Audio Visualizer State ====================

    /**
     * Current audio amplitude for visualizer (0.0 to 1.0).
     */
    private val _audioAmplitude = MutableStateFlow(0f)
    val audioAmplitude: StateFlow<Float> = _audioAmplitude.asStateFlow()

    // ==================== Playback State ====================

    /**
     * Playback progress (0.0 to 1.0).
     */
    private val _playbackProgress = MutableStateFlow(0f)
    val playbackProgress: StateFlow<Float> = _playbackProgress.asStateFlow()

    /**
     * Whether audio is currently playing.
     */
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    // ==================== File State ====================

    /**
     * Reference to the recorded audio file.
     */
    private val _recordedFile = MutableStateFlow<File?>(null)
    val recordedFile: StateFlow<File?> = _recordedFile.asStateFlow()

    // ==================== Error State ====================

    /**
     * Last error that occurred, if any.
     */
    private val _error = MutableStateFlow<Exception?>(null)
    val error: StateFlow<Exception?> = _error.asStateFlow()

    // ==================== State Transition Methods ====================

    /**
     * Transitions from IDLE to RECORDING state.
     * Only valid when current state is IDLE.
     *
     * @return true if transition was successful, false otherwise
     */
    fun startRecording(): Boolean {
        if (_recordingState.value != MediaRecorderState.IDLE) {
            return false
        }
        _recordingState.value = MediaRecorderState.RECORDING
        _recordingTime.value = "00:00"
        _recordingDurationMs.value = 0L
        _audioAmplitude.value = 0f
        _error.value = null
        return true
    }

    /**
     * Transitions from RECORDING to RECORDED state.
     * Only valid when current state is RECORDING.
     *
     * @return true if transition was successful, false otherwise
     */
    fun stopRecording(): Boolean {
        if (_recordingState.value != MediaRecorderState.RECORDING) {
            return false
        }
        _recordingState.value = MediaRecorderState.RECORDED
        _audioAmplitude.value = 0f
        _playbackProgress.value = 0f
        _isPlaying.value = false
        return true
    }

    /**
     * Transitions from RECORDING or RECORDED to IDLE state.
     * Clears all recording data.
     *
     * @return true if transition was successful, false otherwise
     */
    fun deleteRecording(): Boolean {
        val currentState = _recordingState.value
        if (currentState == MediaRecorderState.IDLE) {
            return false
        }
        
        _recordingState.value = MediaRecorderState.IDLE
        _recordingTime.value = "00:00"
        _recordingDurationMs.value = 0L
        _audioAmplitude.value = 0f
        _playbackProgress.value = 0f
        _isPlaying.value = false
        _recordedFile.value = null
        return true
    }

    /**
     * Submits the recording and returns the recorded file.
     * Only valid when current state is RECORDED and a file exists.
     *
     * @return the recorded File if available, null otherwise
     */
    fun submitRecording(): File? {
        if (_recordingState.value != MediaRecorderState.RECORDED) {
            return null
        }
        return _recordedFile.value
    }

    /**
     * Handles an error during recording.
     * Transitions to IDLE state and stores the error.
     *
     * @param exception the error that occurred
     */
    fun handleError(exception: Exception) {
        _error.value = exception
        _recordingState.value = MediaRecorderState.IDLE
        _recordingTime.value = "00:00"
        _recordingDurationMs.value = 0L
        _audioAmplitude.value = 0f
        _playbackProgress.value = 0f
        _isPlaying.value = false
    }

    // ==================== Playback Methods ====================

    /**
     * Starts playback of the recorded audio.
     * Only valid when current state is RECORDED.
     *
     * @return true if playback can start, false otherwise
     */
    fun startPlayback(): Boolean {
        if (_recordingState.value != MediaRecorderState.RECORDED) {
            return false
        }
        _isPlaying.value = true
        return true
    }

    /**
     * Pauses playback of the recorded audio.
     * Only valid when currently playing.
     *
     * @return true if playback was paused, false otherwise
     */
    fun pausePlayback(): Boolean {
        if (!_isPlaying.value) {
            return false
        }
        _isPlaying.value = false
        return true
    }

    /**
     * Toggles playback state (play/pause).
     * Only valid when current state is RECORDED.
     *
     * @return true if toggle was successful, false otherwise
     */
    fun togglePlayback(): Boolean {
        if (_recordingState.value != MediaRecorderState.RECORDED) {
            return false
        }
        _isPlaying.value = !_isPlaying.value
        return true
    }

    /**
     * Seeks to a specific position in the playback.
     * Progress is clamped to [0.0, 1.0].
     *
     * @param progress the position to seek to (0.0 to 1.0)
     */
    fun seekTo(progress: Float) {
        _playbackProgress.value = progress.coerceIn(0f, 1f)
    }

    /**
     * Handles playback completion.
     * Resets playback state to beginning.
     */
    fun onPlaybackComplete() {
        _isPlaying.value = false
        _playbackProgress.value = 0f
    }

    // ==================== Update Methods ====================

    /**
     * Updates the audio amplitude for visualizer.
     * Amplitude is clamped to [0.0, 1.0].
     *
     * @param amplitude the new amplitude value (0.0 to 1.0)
     */
    fun updateAmplitude(amplitude: Float) {
        _audioAmplitude.value = amplitude.coerceIn(0f, 1f)
    }

    /**
     * Updates the recording time from milliseconds.
     * Also updates the formatted time string.
     *
     * @param timeMs the recording time in milliseconds
     */
    fun updateRecordingTime(timeMs: Long) {
        _recordingDurationMs.value = timeMs
        _recordingTime.value = formatTime(timeMs)
    }

    /**
     * Updates the playback progress.
     * Progress is clamped to [0.0, 1.0].
     *
     * @param progress the playback progress (0.0 to 1.0)
     */
    fun updatePlaybackProgress(progress: Float) {
        _playbackProgress.value = progress.coerceIn(0f, 1f)
    }

    /**
     * Sets the recorded file reference.
     *
     * @param file the recorded audio file
     */
    fun setRecordedFile(file: File?) {
        _recordedFile.value = file
    }

    // ==================== Utility Methods ====================

    /**
     * Formats time in milliseconds to MM:SS format.
     *
     * @param durationMs the duration in milliseconds
     * @return formatted time string (e.g., "01:30")
     */
    fun formatTime(durationMs: Long): String {
        val totalSeconds = durationMs / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    /**
     * Checks if a state transition is valid according to the state machine.
     *
     * Valid transitions:
     * - IDLE → RECORDING
     * - RECORDING → RECORDED
     * - RECORDING → IDLE
     * - RECORDED → IDLE
     *
     * @param from the source state
     * @param to the target state
     * @return true if the transition is valid, false otherwise
     */
    fun isValidTransition(from: MediaRecorderState, to: MediaRecorderState): Boolean {
        if (from == to) return true // Same state is always valid (no-op)
        
        return when (from) {
            MediaRecorderState.IDLE -> to == MediaRecorderState.RECORDING
            MediaRecorderState.RECORDING -> to == MediaRecorderState.RECORDED || to == MediaRecorderState.IDLE
            MediaRecorderState.RECORDED -> to == MediaRecorderState.IDLE
        }
    }

    /**
     * Releases all resources.
     * Call this when the recorder is no longer needed.
     */
    fun release() {
        _recordingState.value = MediaRecorderState.IDLE
        _recordingTime.value = "00:00"
        _recordingDurationMs.value = 0L
        _audioAmplitude.value = 0f
        _playbackProgress.value = 0f
        _isPlaying.value = false
        _recordedFile.value = null
        _error.value = null
    }

    override fun onCleared() {
        super.onCleared()
        release()
    }
}
