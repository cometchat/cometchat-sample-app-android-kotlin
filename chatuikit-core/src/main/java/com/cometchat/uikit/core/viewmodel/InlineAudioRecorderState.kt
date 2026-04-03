package com.cometchat.uikit.core.viewmodel

/**
 * Immutable state for the Inline Audio Recorder.
 * Contains all state needed for UI rendering.
 *
 * This data class represents the complete state of the inline audio recorder,
 * including recording status, duration, playback position, file path, error message,
 * and amplitude history for waveform visualization.
 *
 * @property status Current recording status (IDLE, RECORDING, PAUSED, COMPLETED, PLAYING, ERROR)
 * @property duration Total recorded duration in milliseconds
 * @property currentPosition Current playback position in milliseconds (used during PLAYING state)
 * @property filePath Path to the recorded audio file (null when no recording exists)
 * @property errorMessage Error message if status is ERROR (null otherwise)
 * @property amplitudes List of stored amplitude values (0.0 to 1.0) captured during recording,
 *                      used for playback waveform visualization
 *
 * @see InlineAudioRecorderStatus
 * @see CometChatInlineAudioRecorderViewModel
 */
data class InlineAudioRecorderState(
    /**
     * Current status of the inline audio recorder.
     * Determines which UI elements are visible and their behavior.
     */
    val status: InlineAudioRecorderStatus = InlineAudioRecorderStatus.IDLE,

    /**
     * Total recorded duration in milliseconds.
     * Updated during recording and preserved after recording completes.
     */
    val duration: Long = 0L,

    /**
     * Current playback position in milliseconds.
     * Only relevant during PLAYING state, updated via polling at 100ms intervals.
     */
    val currentPosition: Long = 0L,

    /**
     * Path to the recorded audio file.
     * Set when recording completes, cleared when recording is deleted.
     */
    val filePath: String? = null,

    /**
     * Error message when status is ERROR.
     * Null when no error has occurred.
     */
    val errorMessage: String? = null,

    /**
     * List of amplitude values captured during recording.
     * Each value is normalized to range [0.0, 1.0].
     * Used for waveform visualization during playback.
     * New amplitudes are appended to the end (bars appear on right, scroll left).
     */
    val amplitudes: List<Float> = emptyList()
) {
    /**
     * Calculates the playback progress as a value between 0.0 and 1.0.
     * Returns 0.0 if duration is 0 to avoid division by zero.
     */
    val playbackProgress: Float
        get() = if (duration > 0) (currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f) else 0f

    /**
     * Returns true if the recorder is in a state where recording can be started.
     */
    val canStartRecording: Boolean
        get() = status == InlineAudioRecorderStatus.IDLE

    /**
     * Returns true if the recorder is in a state where recording can be paused.
     */
    val canPauseRecording: Boolean
        get() = status == InlineAudioRecorderStatus.RECORDING

    /**
     * Returns true if the recorder is in a state where recording can be resumed.
     */
    val canResumeRecording: Boolean
        get() = status == InlineAudioRecorderStatus.PAUSED

    /**
     * Returns true if the recorder is in a state where recording can be stopped.
     */
    val canStopRecording: Boolean
        get() = status == InlineAudioRecorderStatus.RECORDING || status == InlineAudioRecorderStatus.PAUSED

    /**
     * Returns true if the recorder is in a state where playback can be started.
     */
    val canStartPlayback: Boolean
        get() = status == InlineAudioRecorderStatus.COMPLETED

    /**
     * Returns true if the recorder is in a state where playback can be paused.
     */
    val canPausePlayback: Boolean
        get() = status == InlineAudioRecorderStatus.PLAYING

    /**
     * Returns true if the recorder is in a state where the recording can be deleted.
     */
    val canDelete: Boolean
        get() = status != InlineAudioRecorderStatus.IDLE

    /**
     * Returns true if the recorder is in a state where the recording can be submitted.
     */
    val canSubmit: Boolean
        get() = status == InlineAudioRecorderStatus.COMPLETED || 
                status == InlineAudioRecorderStatus.PLAYING ||
                status == InlineAudioRecorderStatus.PAUSED

    /**
     * Returns true if the recorder is actively recording (not paused).
     */
    val isRecording: Boolean
        get() = status == InlineAudioRecorderStatus.RECORDING

    /**
     * Returns true if the recorder is playing back audio.
     */
    val isPlaying: Boolean
        get() = status == InlineAudioRecorderStatus.PLAYING

    /**
     * Returns true if the recorder has an error.
     */
    val hasError: Boolean
        get() = status == InlineAudioRecorderStatus.ERROR
}
