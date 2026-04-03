package com.cometchat.uikit.core.viewmodel

/**
 * Recording status for the Inline Audio Recorder.
 * Six distinct states for complete recording lifecycle management.
 *
 * State Machine:
 * ```
 *                                     ┌─────────────────────────────────────┐
 *                                     │                                     │
 *                                     ▼                                     │
 * ┌────────┐   startRecording   ┌───────────┐   pauseRecording   ┌────────┐│
 * │  IDLE  │ ─────────────────► │ RECORDING │ ─────────────────► │ PAUSED ││
 * └────────┘                    └───────────┘                    └────────┘│
 *      ▲                              │                              │     │
 *      │                              │ stopRecording                │     │
 *      │                              │                              │     │
 *      │                              ▼                              │     │
 *      │      deleteRecording   ┌───────────┐   resumeRecording     │     │
 *      │◄─────────────────────── │ COMPLETED │ ◄────────────────────┘     │
 *      │                        └───────────┘                             │
 *      │                              │ ▲                                 │
 *      │                   startPlay │ │ pausePlayback / playbackComplete │
 *      │                              ▼ │                                 │
 *      │                        ┌───────────┐                             │
 *      │◄─────────────────────── │  PLAYING  │                            │
 *      │      deleteRecording   └───────────┘                             │
 *      │                                                                  │
 *      │                        ┌───────────┐                             │
 *      │◄─────────────────────── │   ERROR   │ ◄──────────────────────────┘
 *             recover           └───────────┘        onError
 * ```
 *
 * Valid State Transitions:
 * - IDLE → RECORDING (on startRecording)
 * - RECORDING → PAUSED (on pauseRecording)
 * - RECORDING → COMPLETED (on stopRecording)
 * - RECORDING → IDLE (on deleteRecording)
 * - RECORDING → ERROR (on error)
 * - PAUSED → RECORDING (on resumeRecording)
 * - PAUSED → COMPLETED (on stopRecording)
 * - PAUSED → IDLE (on deleteRecording)
 * - PAUSED → ERROR (on error)
 * - COMPLETED → PLAYING (on startPlayback)
 * - COMPLETED → IDLE (on deleteRecording)
 * - PLAYING → COMPLETED (on pausePlayback or playbackComplete)
 * - PLAYING → IDLE (on deleteRecording)
 * - ERROR → IDLE (on recover or deleteRecording)
 *
 * Note: This is a NEW enum separate from the existing [MediaRecorderState] enum.
 * The existing MediaRecorderState has 3 states (IDLE, RECORDING, RECORDED)
 * while this InlineAudioRecorderStatus has 6 states (IDLE, RECORDING, PAUSED,
 * COMPLETED, PLAYING, ERROR) to support pause/resume recording and playback.
 *
 * @see CometChatInlineAudioRecorderViewModel
 */
enum class InlineAudioRecorderStatus {
    /**
     * Initial state - not recording, recorder not visible.
     * Composer shows normal input in this state.
     */
    IDLE,

    /**
     * Currently recording audio.
     * Shows animated waveform, timer, pause button, delete button, and send button.
     */
    RECORDING,

    /**
     * Recording is paused.
     * Shows static waveform, play button, mic/resume button, delete button, and send button.
     */
    PAUSED,

    /**
     * Recording is complete and ready to send/preview.
     * Shows static waveform, play button, delete button, and send button.
     */
    COMPLETED,

    /**
     * Playing back the recorded audio.
     * Shows progress waveform, pause button, delete button, and send button.
     */
    PLAYING,

    /**
     * Error state.
     * Shows error message with recovery option.
     */
    ERROR
}
