package com.cometchat.uikit.core.viewmodel

/**
 * Recording state enum for the redesigned CometChatMediaRecorder component.
 * Represents the three distinct states of the audio recording process.
 *
 * State Machine:
 * ```
 * ┌────────┐   startRecording   ┌───────────┐
 * │  IDLE  │ ─────────────────► │ RECORDING │
 * └────────┘                    └───────────┘
 *      ▲                              │
 *      │                        stopRecording
 *      │                              │
 *      │      deleteRecording   ┌───────────┐
 *      └─────────────────────── │ RECORDED  │
 *                               └───────────┘
 * ```
 *
 * Valid State Transitions:
 * - IDLE → RECORDING (on startRecording)
 * - RECORDING → RECORDED (on stopRecording)
 * - RECORDING → IDLE (on deleteRecording or error)
 * - RECORDED → IDLE (on deleteRecording)
 *
 * Note: This is a NEW enum separate from the existing [RecordingState] enum.
 * The existing RecordingState has 4 states (START, RECORDING, PAUSED, STOPPED)
 * while this new MediaRecorderState has 3 states (IDLE, RECORDING, RECORDED)
 * following the redesigned architecture.
 *
 * @see CometChatMediaRecorderViewModel
 */
enum class MediaRecorderState {
    /**
     * Initial state, ready to start recording.
     * Only the record button is visible in this state.
     */
    IDLE,

    /**
     * Actively recording audio.
     * Shows timer, audio visualizer, delete button, stop button, and submit button (disabled).
     */
    RECORDING,

    /**
     * Recording is complete, ready for playback/submit.
     * Shows playback controls (play/pause, seekbar, duration), delete button, and submit button.
     */
    RECORDED
}
