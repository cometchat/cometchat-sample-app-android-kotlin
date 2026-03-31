package com.cometchat.chatuikit.shared.views.inlineaudiorecorder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents the internal state of the inline audio recorder.
 * <p>
 * This class holds all state information for the audio recorder including
 * current status, recording duration, playback position, file path, error messages,
 * and amplitude data for waveform visualization.
 * </p>
 *
 * @see InlineAudioRecorderStatus
 */
public class InlineAudioRecorderState {

    private InlineAudioRecorderStatus status;
    private long duration;
    private long currentPosition;
    private String filePath;
    private String errorMessage;
    private List<Float> amplitudes;

    /**
     * Creates a new InlineAudioRecorderState with default values.
     * Initial state is IDLE with zero duration and position.
     */
    public InlineAudioRecorderState() {
        this.status = InlineAudioRecorderStatus.IDLE;
        this.duration = 0;
        this.currentPosition = 0;
        this.filePath = null;
        this.errorMessage = null;
        this.amplitudes = new ArrayList<>();
    }

    /**
     * Creates a new InlineAudioRecorderState with specified values.
     *
     * @param status          the current recorder status
     * @param duration        the total recorded duration in milliseconds
     * @param currentPosition the current playback position in milliseconds
     * @param filePath        the path to the recorded audio file
     * @param errorMessage    the error message if status is ERROR
     * @param amplitudes      the list of amplitude values for waveform visualization
     */
    public InlineAudioRecorderState(InlineAudioRecorderStatus status, long duration,
                                    long currentPosition, String filePath,
                                    String errorMessage, List<Float> amplitudes) {
        this.status = status;
        this.duration = duration;
        this.currentPosition = currentPosition;
        this.filePath = filePath;
        this.errorMessage = errorMessage;
        this.amplitudes = amplitudes != null ? new ArrayList<>(amplitudes) : new ArrayList<>();
    }

    // Getters and Setters

    public InlineAudioRecorderStatus getStatus() {
        return status;
    }

    public void setStatus(InlineAudioRecorderStatus status) {
        this.status = status;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public long getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(long currentPosition) {
        this.currentPosition = currentPosition;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public List<Float> getAmplitudes() {
        return amplitudes;
    }

    public void setAmplitudes(List<Float> amplitudes) {
        this.amplitudes = amplitudes != null ? new ArrayList<>(amplitudes) : new ArrayList<>();
    }

    // Helper methods

    /**
     * Checks if the recorder is currently recording.
     *
     * @return true if status is RECORDING, false otherwise
     */
    public boolean isRecording() {
        return status == InlineAudioRecorderStatus.RECORDING;
    }

    /**
     * Checks if the recording is paused.
     *
     * @return true if status is PAUSED, false otherwise
     */
    public boolean isPaused() {
        return status == InlineAudioRecorderStatus.PAUSED;
    }

    /**
     * Checks if the recording is completed.
     *
     * @return true if status is COMPLETED, false otherwise
     */
    public boolean isCompleted() {
        return status == InlineAudioRecorderStatus.COMPLETED;
    }

    /**
     * Checks if the recorder is currently playing back audio.
     *
     * @return true if status is PLAYING, false otherwise
     */
    public boolean isPlaying() {
        return status == InlineAudioRecorderStatus.PLAYING;
    }

    /**
     * Checks if a recording exists (file path is set).
     *
     * @return true if filePath is not null and not empty, false otherwise
     */
    public boolean hasRecording() {
        return filePath != null && !filePath.isEmpty();
    }

    /**
     * Resets all state to initial values.
     * Sets status to IDLE, clears duration, position, file path, error message,
     * and amplitudes list.
     */
    public void reset() {
        this.status = InlineAudioRecorderStatus.IDLE;
        this.duration = 0;
        this.currentPosition = 0;
        this.filePath = null;
        this.errorMessage = null;
        this.amplitudes = new ArrayList<>();
    }

    /**
     * Creates a copy of this state object.
     * The copy is a new instance with the same field values.
     *
     * @return a new InlineAudioRecorderState with identical values
     */
    public InlineAudioRecorderState copy() {
        return new InlineAudioRecorderState(
                this.status,
                this.duration,
                this.currentPosition,
                this.filePath,
                this.errorMessage,
                this.amplitudes
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InlineAudioRecorderState that = (InlineAudioRecorderState) o;
        return duration == that.duration &&
                currentPosition == that.currentPosition &&
                status == that.status &&
                Objects.equals(filePath, that.filePath) &&
                Objects.equals(errorMessage, that.errorMessage) &&
                Objects.equals(amplitudes, that.amplitudes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, duration, currentPosition, filePath, errorMessage, amplitudes);
    }

    @Override
    public String toString() {
        return "InlineAudioRecorderState{" +
                "status=" + status +
                ", duration=" + duration +
                ", currentPosition=" + currentPosition +
                ", filePath='" + filePath + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                ", amplitudesCount=" + (amplitudes != null ? amplitudes.size() : 0) +
                '}';
    }
}
