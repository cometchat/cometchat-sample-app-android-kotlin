package com.cometchat.chatuikit.shared.views.inlineaudiorecorder;

/**
 * Enum defining the possible states of the inline audio recorder.
 * <p>
 * This enum represents the various states the audio recorder can be in during
 * its lifecycle, from initial idle state through recording, playback, and error states.
 * </p>
 *
 * @see InlineAudioRecorderState
 */
public enum InlineAudioRecorderStatus {
    /**
     * Initial state - recorder is not active and no recording exists.
     */
    IDLE,

    /**
     * Currently recording audio from the microphone.
     */
    RECORDING,

    /**
     * Recording has been paused and can be resumed.
     */
    PAUSED,

    /**
     * Recording is complete and ready to send or preview.
     */
    COMPLETED,

    /**
     * Playing back the recorded audio for preview.
     */
    PLAYING,

    /**
     * An error occurred during recording or playback.
     */
    ERROR
}
