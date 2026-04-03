package com.cometchat.uikit.core.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Shared ViewModel for CometChatInlineAudioRecorder.
 * Manages six-state recording lifecycle, amplitude history, and playback.
 *
 * This ViewModel is shared by both chatuikit-jetpack (Compose) and chatuikit-kotlin (Views)
 * implementations, ensuring consistent behavior across both UI frameworks.
 *
 * Features:
 * - Six-state machine management (IDLE, RECORDING, PAUSED, COMPLETED, PLAYING, ERROR)
 * - Recording time tracking with formatted display
 * - Amplitude history storage for playback visualization
 * - Playback progress and seeking support
 * - Pause/Resume recording support
 *
 * State Machine:
 * ```
 * IDLE → RECORDING (startRecording)
 * RECORDING → PAUSED (pauseRecording)
 * RECORDING → COMPLETED (stopRecording)
 * RECORDING → IDLE (deleteRecording)
 * RECORDING → ERROR (onError)
 * PAUSED → RECORDING (resumeRecording)
 * PAUSED → COMPLETED (stopRecording)
 * PAUSED → IDLE (deleteRecording)
 * PAUSED → ERROR (onError)
 * COMPLETED → PLAYING (startPlayback)
 * COMPLETED → IDLE (deleteRecording)
 * PLAYING → COMPLETED (pausePlayback or playbackComplete)
 * PLAYING → IDLE (deleteRecording)
 * ERROR → IDLE (recover or deleteRecording)
 * ```
 *
 * @see InlineAudioRecorderState
 * @see InlineAudioRecorderStatus
 */
open class CometChatInlineAudioRecorderViewModel : ViewModel() {

    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    // ==================== State ====================

    /**
     * Complete state of the inline audio recorder.
     */
    private val _state = MutableStateFlow(InlineAudioRecorderState())
    val state: StateFlow<InlineAudioRecorderState> = _state.asStateFlow()

    // ==================== Convenience Accessors ====================

    /**
     * Current recording status.
     */
    val status: InlineAudioRecorderStatus
        get() = _state.value.status

    /**
     * Total recorded duration in milliseconds.
     */
    val duration: Long
        get() = _state.value.duration

    /**
     * Current playback position in milliseconds.
     */
    val currentPosition: Long
        get() = _state.value.currentPosition

    /**
     * List of amplitude values for waveform visualization.
     */
    val amplitudes: List<Float>
        get() = _state.value.amplitudes

    /**
     * Path to the recorded file.
     */
    val filePath: String?
        get() = _state.value.filePath

    /**
     * Error message if in ERROR state.
     */
    val errorMessage: String?
        get() = _state.value.errorMessage

    // ==================== Formatted Time ====================

    /**
     * Formats the total duration as MM:SS.
     */
    val formattedDuration: String
        get() = formatTime(_state.value.duration)

    /**
     * Formats the current playback position as MM:SS.
     */
    val formattedPosition: String
        get() = formatTime(_state.value.currentPosition)

    /**
     * Returns the appropriate time display based on current status.
     * - RECORDING: elapsed recording time
     * - PAUSED: total recorded duration
     * - COMPLETED: total recording duration
     * - PLAYING: current playback position
     */
    val displayTime: String
        get() = when (_state.value.status) {
            InlineAudioRecorderStatus.RECORDING -> formattedDuration
            InlineAudioRecorderStatus.PAUSED -> formattedDuration
            InlineAudioRecorderStatus.COMPLETED -> formattedDuration
            InlineAudioRecorderStatus.PLAYING -> formattedPosition
            else -> "00:00"
        }

    // ==================== State Transitions ====================

    /**
     * Transitions from IDLE to RECORDING state.
     * Only valid when current state is IDLE.
     *
     * @return true if transition was successful, false otherwise
     */
    fun startRecording(): Boolean {
        if (!isValidTransition(_state.value.status, InlineAudioRecorderStatus.RECORDING)) {
            return false
        }
        _state.value = _state.value.copy(
            status = InlineAudioRecorderStatus.RECORDING,
            duration = 0L,
            currentPosition = 0L,
            amplitudes = emptyList(),
            errorMessage = null
        )
        return true
    }

    /**
     * Transitions from RECORDING to PAUSED state.
     * Only valid when current state is RECORDING.
     *
     * @return true if transition was successful, false otherwise
     */
    fun pauseRecording(): Boolean {
        if (!isValidTransition(_state.value.status, InlineAudioRecorderStatus.PAUSED)) {
            return false
        }
        _state.value = _state.value.copy(
            status = InlineAudioRecorderStatus.PAUSED
        )
        return true
    }

    /**
     * Transitions from PAUSED to RECORDING state.
     * Only valid when current state is PAUSED.
     *
     * @return true if transition was successful, false otherwise
     */
    fun resumeRecording(): Boolean {
        if (_state.value.status != InlineAudioRecorderStatus.PAUSED) {
            return false
        }
        _state.value = _state.value.copy(
            status = InlineAudioRecorderStatus.RECORDING
        )
        return true
    }

    /**
     * Transitions from RECORDING or PAUSED to COMPLETED state.
     * Only valid when current state is RECORDING or PAUSED.
     *
     * @return true if transition was successful, false otherwise
     */
    fun stopRecording(): Boolean {
        val currentStatus = _state.value.status
        if (currentStatus != InlineAudioRecorderStatus.RECORDING && 
            currentStatus != InlineAudioRecorderStatus.PAUSED) {
            return false
        }
        _state.value = _state.value.copy(
            status = InlineAudioRecorderStatus.COMPLETED,
            currentPosition = 0L
        )
        return true
    }

    /**
     * Transitions from any non-IDLE state to IDLE state.
     * Clears all recording data.
     *
     * @return true if transition was successful, false otherwise
     */
    fun deleteRecording(): Boolean {
        if (_state.value.status == InlineAudioRecorderStatus.IDLE) {
            return false
        }
        _state.value = InlineAudioRecorderState()
        return true
    }

    /**
     * Transitions from COMPLETED to PLAYING state.
     * Only valid when current state is COMPLETED.
     *
     * @return true if transition was successful, false otherwise
     */
    fun startPlayback(): Boolean {
        if (_state.value.status != InlineAudioRecorderStatus.COMPLETED) {
            return false
        }
        _state.value = _state.value.copy(
            status = InlineAudioRecorderStatus.PLAYING
        )
        return true
    }

    /**
     * Transitions from PLAYING to COMPLETED state.
     * Only valid when current state is PLAYING.
     *
     * @return true if transition was successful, false otherwise
     */
    fun pausePlayback(): Boolean {
        if (_state.value.status != InlineAudioRecorderStatus.PLAYING) {
            return false
        }
        _state.value = _state.value.copy(
            status = InlineAudioRecorderStatus.COMPLETED
        )
        return true
    }

    /**
     * Handles playback completion.
     * Resets playback position to beginning and transitions to COMPLETED state.
     */
    fun onPlaybackComplete() {
        if (_state.value.status == InlineAudioRecorderStatus.PLAYING) {
            _state.value = _state.value.copy(
                status = InlineAudioRecorderStatus.COMPLETED,
                currentPosition = 0L
            )
        }
    }

    /**
     * Seeks to a specific position in the playback.
     * Only valid when current state is COMPLETED or PLAYING.
     *
     * @param positionMs the position to seek to in milliseconds
     * @return true if seek was successful, false otherwise
     */
    fun seekTo(positionMs: Long): Boolean {
        val currentStatus = _state.value.status
        if (currentStatus != InlineAudioRecorderStatus.COMPLETED && 
            currentStatus != InlineAudioRecorderStatus.PLAYING) {
            return false
        }
        val clampedPosition = positionMs.coerceIn(0L, _state.value.duration)
        _state.value = _state.value.copy(
            currentPosition = clampedPosition
        )
        return true
    }

    /**
     * Handles an error during recording or playback.
     * Transitions to ERROR state and stores the error message.
     *
     * @param message the error message
     */
    fun handleError(message: String) {
        _state.value = _state.value.copy(
            status = InlineAudioRecorderStatus.ERROR,
            errorMessage = message
        )
    }

    /**
     * Recovers from ERROR state.
     * Transitions to IDLE state and clears all data.
     *
     * @return true if recovery was successful, false otherwise
     */
    fun recover(): Boolean {
        if (_state.value.status != InlineAudioRecorderStatus.ERROR) {
            return false
        }
        _state.value = InlineAudioRecorderState()
        return true
    }

    // ==================== Update Methods ====================

    /**
     * Updates the recording duration.
     *
     * @param durationMs the new duration in milliseconds
     */
    fun updateDuration(durationMs: Long) {
        _state.value = _state.value.copy(
            duration = durationMs.coerceAtLeast(0L)
        )
    }

    /**
     * Updates the playback position.
     *
     * @param positionMs the new position in milliseconds
     */
    fun updatePosition(positionMs: Long) {
        _state.value = _state.value.copy(
            currentPosition = positionMs.coerceIn(0L, _state.value.duration)
        )
    }

    /**
     * Adds an amplitude value to the history.
     * Amplitude is clamped to [0.0, 1.0].
     *
     * @param amplitude the amplitude value to add
     */
    fun addAmplitude(amplitude: Float) {
        val clampedAmplitude = amplitude.coerceIn(0f, 1f)
        _state.value = _state.value.copy(
            amplitudes = _state.value.amplitudes + clampedAmplitude
        )
    }

    /**
     * Sets the recorded file path.
     *
     * @param path the file path, or null to clear
     */
    fun setFilePath(path: String?) {
        _state.value = _state.value.copy(
            filePath = path
        )
    }

    /**
     * Clears all amplitude values.
     */
    fun clearAmplitudes() {
        _state.value = _state.value.copy(
            amplitudes = emptyList()
        )
    }

    /**
     * Sets the complete state (used for restoring state).
     *
     * @param newState the new state to set
     */
    fun setState(newState: InlineAudioRecorderState) {
        _state.value = newState
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
     * - RECORDING → PAUSED, COMPLETED, IDLE, ERROR
     * - PAUSED → RECORDING, COMPLETED, IDLE, ERROR
     * - COMPLETED → PLAYING, IDLE
     * - PLAYING → COMPLETED, IDLE
     * - ERROR → IDLE
     *
     * @param from the source state
     * @param to the target state
     * @return true if the transition is valid, false otherwise
     */
    fun isValidTransition(from: InlineAudioRecorderStatus, to: InlineAudioRecorderStatus): Boolean {
        if (from == to) return true // Same state is always valid (no-op)

        return when (from) {
            InlineAudioRecorderStatus.IDLE -> to == InlineAudioRecorderStatus.RECORDING
            InlineAudioRecorderStatus.RECORDING -> to in listOf(
                InlineAudioRecorderStatus.PAUSED,
                InlineAudioRecorderStatus.COMPLETED,
                InlineAudioRecorderStatus.IDLE,
                InlineAudioRecorderStatus.ERROR
            )
            InlineAudioRecorderStatus.PAUSED -> to in listOf(
                InlineAudioRecorderStatus.RECORDING,
                InlineAudioRecorderStatus.COMPLETED,
                InlineAudioRecorderStatus.IDLE,
                InlineAudioRecorderStatus.ERROR
            )
            InlineAudioRecorderStatus.COMPLETED -> to in listOf(
                InlineAudioRecorderStatus.PLAYING,
                InlineAudioRecorderStatus.IDLE
            )
            InlineAudioRecorderStatus.PLAYING -> to in listOf(
                InlineAudioRecorderStatus.COMPLETED,
                InlineAudioRecorderStatus.IDLE
            )
            InlineAudioRecorderStatus.ERROR -> to == InlineAudioRecorderStatus.IDLE
        }
    }

    /**
     * Releases all resources.
     * Call this when the recorder is no longer needed.
     */
    fun release() {
        _state.value = InlineAudioRecorderState()
    }

    override fun onCleared() {
        super.onCleared()
        release()
    }
}
