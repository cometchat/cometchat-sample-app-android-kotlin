package com.cometchat.chatuikit.shared.views.inlineaudiorecorder;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.cometchat.chatuikit.shared.resources.utils.AudioPlayer;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * ViewModel managing audio recording and playback for the inline audio recorder.
 * <p>
 * This ViewModel handles:
 * <ul>
 *     <li>MediaRecorder lifecycle for audio recording</li>
 *     <li>MediaPlayer lifecycle for audio playback</li>
 *     <li>State management via LiveData</li>
 *     <li>Amplitude polling for waveform visualization</li>
 *     <li>Duration tracking during recording and playback</li>
 * </ul>
 * </p>
 *
 * @see InlineAudioRecorderState
 * @see InlineAudioRecorderStatus
 */
public class InlineAudioRecorderViewModel extends ViewModel {

    // Polling intervals
    private static final int AMPLITUDE_POLL_INTERVAL_MS = 120;
    private static final int DURATION_UPDATE_INTERVAL_MS = 500;
    private static final int PLAYBACK_POLL_INTERVAL_MS = 100;

    // Maximum raw amplitude value from MediaRecorder
    private static final float MAX_AMPLITUDE = 32767.0f;

    // State
    private final MutableLiveData<InlineAudioRecorderState> _state = new MutableLiveData<>(new InlineAudioRecorderState());

    // Native audio components
    private MediaRecorder mediaRecorder;
    private MediaPlayer mediaPlayer;

    // Handler for polling
    private final Handler handler = new Handler(Looper.getMainLooper());

    // Recording state tracking
    private long recordingStartTime = 0;
    private long pausedDuration = 0;
    private long pauseStartTime = 0;
    private String currentFilePath;
    
    // Segment tracking for continue-after-playback feature
    private List<String> recordingSegments = new ArrayList<>();
    private long previousSegmentsDuration = 0;
    private List<Float> previousSegmentsAmplitudes = new ArrayList<>();
    private File outputDirectory;

    // Runnables for polling
    private Runnable amplitudeRunnable;
    private Runnable durationRunnable;
    private Runnable playbackRunnable;

    /**
     * Default constructor.
     */
    public InlineAudioRecorderViewModel() {
        // Initialize with default state
    }

    // ==================== LiveData Getters ====================

    /**
     * Returns the LiveData for the recorder state.
     *
     * @return LiveData containing the InlineAudioRecorderState.
     */
    public LiveData<InlineAudioRecorderState> getState() {
        return _state;
    }

    // ==================== Recording Methods ====================

    /**
     * Starts audio recording.
     *
     * @param outputDir The directory to save the recording to.
     * @return true if recording started successfully, false otherwise.
     */
    public boolean startRecording(@NonNull File outputDir) {
        try {
            // Stop any audio that might be playing in AudioPlayer (e.g., from audio bubbles)
            // This ensures mutual exclusion between recording and playback
            AudioPlayer.getInstance().reset();

            // Ensure output directory exists
            if (!outputDir.exists()) {
                if (!outputDir.mkdirs()) {
                    handleError("Failed to start recording: Unable to create output directory");
                    return false;
                }
            }
            
            // Store output directory for continue-after-playback feature
            this.outputDirectory = outputDir;
            
            // Clear any previous segments
            recordingSegments.clear();
            previousSegmentsDuration = 0;
            previousSegmentsAmplitudes.clear();

            // Generate filename with timestamp - use same pattern as CometChatMediaRecorder
            String filename = generateFilename();
            currentFilePath = outputDir.getAbsolutePath() + "/" + filename;

            // Setup MediaRecorder - following CometChatMediaRecorder pattern
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setOutputFile(currentFilePath);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

            // Set error listener for runtime errors
            mediaRecorder.setOnErrorListener((mr, what, extra) -> {
                handleError("MediaRecorder error occurred: what=" + what + ", extra=" + extra);
            });

            mediaRecorder.prepare();
            mediaRecorder.start();

            // Update state
            InlineAudioRecorderState state = getCurrentState();
            state.setStatus(InlineAudioRecorderStatus.RECORDING);
            state.setFilePath(currentFilePath);
            state.setDuration(0);
            state.setCurrentPosition(0);
            state.setAmplitudes(new ArrayList<>());
            _state.setValue(state);

            // Start tracking
            recordingStartTime = System.currentTimeMillis();
            pausedDuration = 0;

            // Start amplitude polling
            startAmplitudePolling();

            // Start duration updates
            startDurationUpdates();

            return true;
        } catch (IOException e) {
            handleError("Failed to start recording: Unable to prepare MediaRecorder - " + e.getMessage());
            return false;
        } catch (IllegalStateException e) {
            handleError("Failed to start recording: MediaRecorder in invalid state - " + e.getMessage());
            return false;
        } catch (SecurityException e) {
            handleError("Failed to start recording: Microphone permission not granted - " + e.getMessage());
            return false;
        } catch (RuntimeException e) {
            handleError("Failed to start recording: " + e.getMessage());
            return false;
        }
    }

    /**
     * Generates a filename for the recording with timestamp pattern.
     *
     * @return The generated filename in format "audio-recording-{yyyyMMddHHmmss}.m4a"
     */
    @NonNull
    static String generateFilename() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
        String timestamp = sdf.format(new Date());
        return "audio-recording-" + timestamp + ".m4a";
    }

    /**
     * Starts polling for amplitude values from MediaRecorder.
     */
    private void startAmplitudePolling() {
        amplitudeRunnable = new Runnable() {
            @Override
            public void run() {
                if (mediaRecorder != null && isRecording()) {
                    try {
                        int rawAmplitude = mediaRecorder.getMaxAmplitude();
                        float normalizedAmplitude = normalizeAmplitude(rawAmplitude);
                        float visualAmplitude = amplifyAmplitude(normalizedAmplitude);

                        InlineAudioRecorderState state = getCurrentState();
                        List<Float> amplitudes = new ArrayList<>(state.getAmplitudes());
                        amplitudes.add(visualAmplitude);
                        state.setAmplitudes(amplitudes);
                        _state.setValue(state);
                    } catch (IllegalStateException e) {
                        // MediaRecorder may have been released
                    }
                    handler.postDelayed(this, AMPLITUDE_POLL_INTERVAL_MS);
                }
            }
        };
        handler.post(amplitudeRunnable);
    }

    /**
     * Starts updating the duration during recording.
     */
    private void startDurationUpdates() {
        durationRunnable = new Runnable() {
            @Override
            public void run() {
                if (isRecording()) {
                    long currentDuration = System.currentTimeMillis() - recordingStartTime - pausedDuration;
                    InlineAudioRecorderState state = getCurrentState();
                    state.setDuration(currentDuration);
                    _state.setValue(state);
                    handler.postDelayed(this, DURATION_UPDATE_INTERVAL_MS);
                }
            }
        };
        handler.post(durationRunnable);
    }

    /**
     * Normalizes a raw amplitude value to the range [0.0, 1.0].
     *
     * @param rawAmplitude The raw amplitude value from MediaRecorder (0-32767).
     * @return The normalized amplitude in range [0.0, 1.0].
     */
    static float normalizeAmplitude(int rawAmplitude) {
        return Math.max(0.0f, Math.min(1.0f, rawAmplitude / MAX_AMPLITUDE));
    }

    /**
     * Amplifies a normalized amplitude for better visual response.
     * Uses a piecewise linear function to ensure minimum bar visibility.
     *
     * @param normalized The normalized amplitude in range [0.0, 1.0].
     * @return The amplified amplitude in range [0.15, 1.0].
     */
    static float amplifyAmplitude(float normalized) {
        float amplified;
        if (normalized < 0.1f) {
            amplified = 0.15f + normalized * 2.0f;
        } else if (normalized < 0.4f) {
            amplified = 0.35f + (normalized - 0.1f) * 1.17f;
        } else {
            amplified = 0.7f + (normalized - 0.4f) * 0.5f;
        }
        return Math.max(0.15f, Math.min(1.0f, amplified));
    }

    // ==================== Pause/Resume Recording ====================

    /**
     * Pauses the current recording.
     * On API 24+, uses MediaRecorder.pause(). On lower APIs, stops recording.
     *
     * @return true if pause was successful, false otherwise.
     */
    public boolean pauseRecording() {
        if (mediaRecorder == null || !isRecording()) {
            return false;
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mediaRecorder.pause();
                pauseStartTime = System.currentTimeMillis();

                stopAmplitudePolling();
                stopDurationUpdates();

                InlineAudioRecorderState state = getCurrentState();
                state.setStatus(InlineAudioRecorderStatus.PAUSED);
                _state.setValue(state);

                return true;
            } else {
                return stopRecording() != null;
            }
        } catch (IllegalStateException e) {
            handleError("Failed to pause recording: " + e.getMessage());
            return false;
        }
    }

    /**
     * Resumes a paused recording or continues recording from COMPLETED state.
     * <p>
     * If the recording is in PAUSED state with MediaRecorder still active, it will
     * resume the existing recording. If the recording is in COMPLETED state (after
     * playback), it will start a new recording segment that will be concatenated
     * with the existing recording when stopped.
     * </p>
     * Only supported on API 24+.
     */
    public void resumeRecording() {
        // Stop any audio bubble playback to ensure mutual exclusion
        // between recording and external audio playback
        AudioPlayer.getInstance().reset();

        InlineAudioRecorderState currentState = getCurrentState();
        
        // If we're playing, stop playback first
        if (currentState.getStatus() == InlineAudioRecorderStatus.PLAYING) {
            stopPlayback();
            currentState = getCurrentState();
        }
        
        // If we're in COMPLETED state, start a new recording segment
        if (currentState.getStatus() == InlineAudioRecorderStatus.COMPLETED) {
            continueRecordingFromCompleted(currentState);
            return;
        }
        
        if (mediaRecorder == null || currentState.getStatus() != InlineAudioRecorderStatus.PAUSED) {
            return;
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                // Calculate paused duration
                if (pauseStartTime > 0) {
                    pausedDuration += System.currentTimeMillis() - pauseStartTime;
                    pauseStartTime = 0;
                }

                mediaRecorder.resume();

                // Calculate paused duration for current segment timing
                // (pauseStartTime was set in pauseRecording)

                // Update state
                InlineAudioRecorderState state = getCurrentState();
                state.setStatus(InlineAudioRecorderStatus.RECORDING);
                _state.setValue(state);

                // Restart polling
                startAmplitudePolling();
                // Use offset-aware duration updates when there are previous
                // segments, so the timer continues from the total accumulated
                // duration instead of resetting to 00:00.
                if (previousSegmentsDuration > 0) {
                    startDurationUpdatesWithOffset();
                } else {
                    startDurationUpdates();
                }

            } else {
                // For API < 24, we can't resume
            }
        } catch (IllegalStateException e) {
            handleError("Failed to resume recording: " + e.getMessage());
        }
    }
    
    /**
     * Continues recording from COMPLETED state by starting a new segment.
     * <p>
     * This method saves the current recording as a segment and starts a new
     * recording. When stopRecording() is called, all segments will be
     * concatenated into a single file.
     * </p>
     *
     * @param currentState The current state (must be COMPLETED).
     * @return true if new recording segment started successfully, false otherwise.
     */
    private boolean continueRecordingFromCompleted(InlineAudioRecorderState currentState) {
        if (outputDirectory == null) {
            handleError("Failed to continue recording: Output directory not set");
            return false;
        }
        
        // Save current file as a segment for future concatenation.
        // If recordingSegments is NOT empty, playRecording() already preserved
        // the concatenated file as a single segment entry and set
        // previousSegmentsDuration / previousSegmentsAmplitudes to the correct
        // accumulated values — skip adding currentFilePath again to avoid
        // double-counting the segment, duration, and amplitudes.
        if (!recordingSegments.isEmpty()) {
            // playRecording() already preserved state — nothing to do here.
            // recordingSegments contains the concatenated file,
            // previousSegmentsDuration and previousSegmentsAmplitudes are correct.
        } else if (currentFilePath != null) {
            File currentFile = new File(currentFilePath);
            if (currentFile.exists() && currentFile.length() > 0) {
                recordingSegments.add(currentFilePath);
                previousSegmentsDuration += currentState.getDuration();
                previousSegmentsAmplitudes.addAll(currentState.getAmplitudes());
            }
        }
        
        try {
            // Stop any audio that might be playing
            AudioPlayer.getInstance().reset();
            releaseMediaPlayer();
            
            // Generate new filename for the new segment
            String filename = generateFilename();
            currentFilePath = outputDirectory.getAbsolutePath() + "/" + filename;
            
            // Setup new MediaRecorder
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setOutputFile(currentFilePath);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            
            mediaRecorder.setOnErrorListener((mr, what, extra) -> {
                handleError("MediaRecorder error occurred: what=" + what + ", extra=" + extra);
            });
            
            mediaRecorder.prepare();
            mediaRecorder.start();
            
            // Update state - keep previous amplitudes and add new ones
            InlineAudioRecorderState state = getCurrentState();
            state.setStatus(InlineAudioRecorderStatus.RECORDING);
            state.setFilePath(currentFilePath);
            // Keep the total duration from previous segments
            state.setDuration(previousSegmentsDuration);
            state.setCurrentPosition(0);
            // Keep the amplitudes from previous segments
            state.setAmplitudes(new ArrayList<>(previousSegmentsAmplitudes));
            _state.setValue(state);
            
            // Reset timing for new segment
            recordingStartTime = System.currentTimeMillis();
            pausedDuration = 0;
            
            // Start polling
            startAmplitudePolling();
            startDurationUpdatesWithOffset();
            
            return true;
        } catch (IOException e) {
            handleError("Failed to continue recording: Unable to prepare MediaRecorder - " + e.getMessage());
            return false;
        } catch (IllegalStateException e) {
            handleError("Failed to continue recording: MediaRecorder in invalid state - " + e.getMessage());
            return false;
        } catch (SecurityException e) {
            handleError("Failed to continue recording: Microphone permission not granted - " + e.getMessage());
            return false;
        } catch (RuntimeException e) {
            handleError("Failed to continue recording: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Starts updating the duration during recording with offset from previous segments.
     */
    private void startDurationUpdatesWithOffset() {
        durationRunnable = new Runnable() {
            @Override
            public void run() {
                if (isRecording()) {
                    long currentSegmentDuration = System.currentTimeMillis() - recordingStartTime - pausedDuration;
                    long totalDuration = previousSegmentsDuration + currentSegmentDuration;
                    InlineAudioRecorderState state = getCurrentState();
                    state.setDuration(totalDuration);
                    _state.setValue(state);
                    handler.postDelayed(this, DURATION_UPDATE_INTERVAL_MS);
                }
            }
        };
        handler.post(durationRunnable);
    }

    // ==================== Stop/Cancel Recording ====================

    /**
     * Stops the current recording and returns the file path.
     * <p>
     * This method ensures the recording is properly finalized before returning.
     * If the recording is in PAUSED state, it will be stopped. If already in
     * COMPLETED state (e.g., after playback), it just returns the existing file path.
     * If there are multiple segments, they will be concatenated into a single file.
     * </p>
     *
     * @return The path to the recorded file, or null if no recording exists.
     */
    @Nullable
    public String stopRecording() {
        // Stop polling
        stopAmplitudePolling();
        stopDurationUpdates();
        stopPlaybackPolling();
        
        // Release MediaPlayer if active
        releaseMediaPlayer();

        if (mediaRecorder != null) {
            try {
                mediaRecorder.stop();
            } catch (IllegalStateException e) {
                // May already be stopped
            }
            releaseMediaRecorder();
        }

        // Verify the file exists and has content
        if (currentFilePath != null) {
            File file = new File(currentFilePath);
            if (!file.exists()) {
                // File doesn't exist - recording may have failed
                currentFilePath = null;
                InlineAudioRecorderState state = getCurrentState();
                state.setStatus(InlineAudioRecorderStatus.ERROR);
                state.setErrorMessage("Recording file not found");
                state.setFilePath(null);
                _state.setValue(state);
                return null;
            }
            if (file.length() == 0) {
                // File is empty - recording may have failed
                file.delete();
                currentFilePath = null;
                InlineAudioRecorderState state = getCurrentState();
                state.setStatus(InlineAudioRecorderStatus.ERROR);
                state.setErrorMessage("Recording file is empty");
                state.setFilePath(null);
                _state.setValue(state);
                return null;
            }
        } else {
            // No file path set
            return null;
        }
        
        // If there are multiple segments, concatenate them.
        // Only add currentFilePath as a new segment if it isn't already tracked
        // (playRecording() stores the finalized file in recordingSegments for the
        // continue-recording flow; adding it again would duplicate the audio).
        if (!recordingSegments.isEmpty()) {
            if (!recordingSegments.contains(currentFilePath)) {
                recordingSegments.add(currentFilePath);
            }

            // Only concatenate when there are 2+ segments.
            // A single segment means the file is already the final recording.
            if (recordingSegments.size() > 1) {
                String concatenatedPath = concatenateAudioSegments(recordingSegments);
                if (concatenatedPath != null) {
                    // Delete individual segment files (but not the concatenated output)
                    for (String segmentPath : recordingSegments) {
                        if (!segmentPath.equals(concatenatedPath)) {
                            File segmentFile = new File(segmentPath);
                            if (segmentFile.exists()) {
                                segmentFile.delete();
                            }
                        }
                    }
                    currentFilePath = concatenatedPath;
                }
            }
            
            // Clear segments list
            recordingSegments.clear();
            previousSegmentsDuration = 0;
            previousSegmentsAmplitudes.clear();
        }

        // Update state
        InlineAudioRecorderState state = getCurrentState();
        state.setStatus(InlineAudioRecorderStatus.COMPLETED);
        state.setFilePath(currentFilePath);
        _state.setValue(state);

        return currentFilePath;
    }
    
    /**
     * Concatenates multiple audio segments into a single file.
     *
     * @param segmentPaths List of paths to audio segment files.
     * @return Path to the concatenated file, or null if concatenation failed.
     */
    @Nullable
    private String concatenateAudioSegments(List<String> segmentPaths) {
        if (segmentPaths == null || segmentPaths.isEmpty()) {
            return null;
        }
        
        if (segmentPaths.size() == 1) {
            return segmentPaths.get(0);
        }
        
        if (outputDirectory == null) {
            return segmentPaths.get(segmentPaths.size() - 1);
        }
        
        String outputPath = outputDirectory.getAbsolutePath() + "/" + generateFilename();
        MediaMuxer muxer = null;
        
        try {
            muxer = new MediaMuxer(outputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            int audioTrackIndex = -1;
            long totalDuration = 0;
            
            for (String segmentPath : segmentPaths) {
                File segmentFile = new File(segmentPath);
                if (!segmentFile.exists() || segmentFile.length() == 0) {
                    continue;
                }
                
                MediaExtractor extractor = new MediaExtractor();
                try {
                    extractor.setDataSource(segmentPath);
                    
                    // Find audio track
                    int trackIndex = -1;
                    for (int i = 0; i < extractor.getTrackCount(); i++) {
                        MediaFormat format = extractor.getTrackFormat(i);
                        String mime = format.getString(MediaFormat.KEY_MIME);
                        if (mime != null && mime.startsWith("audio/")) {
                            trackIndex = i;
                            break;
                        }
                    }
                    
                    if (trackIndex < 0) {
                        continue;
                    }
                    
                    extractor.selectTrack(trackIndex);
                    MediaFormat format = extractor.getTrackFormat(trackIndex);
                    
                    // Add track to muxer if not already added
                    if (audioTrackIndex < 0) {
                        audioTrackIndex = muxer.addTrack(format);
                        muxer.start();
                    }
                    
                    // Copy samples
                    ByteBuffer buffer = ByteBuffer.allocate(1024 * 1024);
                    MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
                    
                    while (true) {
                        int sampleSize = extractor.readSampleData(buffer, 0);
                        if (sampleSize < 0) {
                            break;
                        }
                        
                        bufferInfo.offset = 0;
                        bufferInfo.size = sampleSize;
                        bufferInfo.presentationTimeUs = extractor.getSampleTime() + totalDuration;
                        bufferInfo.flags = extractor.getSampleFlags();
                        
                        muxer.writeSampleData(audioTrackIndex, buffer, bufferInfo);
                        extractor.advance();
                    }
                    
                    // Get duration of this segment
                    long segmentDuration = format.getLong(MediaFormat.KEY_DURATION);
                    totalDuration += segmentDuration;
                    
                } finally {
                    extractor.release();
                }
            }
            
            return outputPath;
        } catch (IOException e) {
            // Concatenation failed, return the last segment
            return segmentPaths.get(segmentPaths.size() - 1);
        } catch (Exception e) {
            // Concatenation failed, return the last segment
            return segmentPaths.get(segmentPaths.size() - 1);
        } finally {
            if (muxer != null) {
                try {
                    muxer.stop();
                    muxer.release();
                } catch (Exception e) {
                    // Ignore
                }
            }
        }
    }

    /**
     * Cancels the current recording and deletes the file.
     */
    public void cancelRecording() {
        // Stop polling
        stopAmplitudePolling();
        stopDurationUpdates();
        stopPlaybackPolling();

        // Stop and release MediaRecorder
        if (mediaRecorder != null) {
            try {
                mediaRecorder.stop();
            } catch (IllegalStateException e) {
                // May already be stopped
            }
            releaseMediaRecorder();
        }

        // Stop and release MediaPlayer
        releaseMediaPlayer();

        // Delete the current file
        if (currentFilePath != null) {
            File file = new File(currentFilePath);
            if (file.exists()) {
                file.delete();
            }
            currentFilePath = null;
        }
        
        // Delete all segment files
        for (String segmentPath : recordingSegments) {
            File segmentFile = new File(segmentPath);
            if (segmentFile.exists()) {
                segmentFile.delete();
            }
        }
        recordingSegments.clear();
        previousSegmentsDuration = 0;
        previousSegmentsAmplitudes.clear();

        // Reset state
        InlineAudioRecorderState state = getCurrentState();
        state.reset();
        _state.setValue(state);
    }

    // ==================== Playback Methods ====================

    /**
     * Starts playback of the recorded audio.
     * <p>
     * Note: Playback is only possible when the recording is in COMPLETED state.
     * When the recording is PAUSED, the MediaRecorder still holds the file open,
     * so we need to stop the recording first before playback can work.
     * </p>
     *
     * @return true if playback started successfully, false otherwise.
     */
    public boolean playRecording() {
        // Stop any audio that might be playing in AudioPlayer (e.g., from audio bubbles)
        AudioPlayer.getInstance().reset();

        InlineAudioRecorderState currentState = getCurrentState();

        // Track whether we just finalized a recording (need segment handling)
        boolean justFinalized = false;

        // If recording is paused, we need to stop it first to finalize the file
        if (currentState.getStatus() == InlineAudioRecorderStatus.PAUSED) {
            stopAmplitudePolling();
            stopDurationUpdates();

            if (mediaRecorder != null) {
                try {
                    mediaRecorder.stop();
                } catch (IllegalStateException e) {
                    // May already be stopped
                }
                releaseMediaRecorder();
            }

            currentState.setStatus(InlineAudioRecorderStatus.COMPLETED);
            _state.setValue(currentState);
            justFinalized = true;
        }

        if (currentFilePath == null ||
            currentState.getStatus() != InlineAudioRecorderStatus.COMPLETED) {
            return false;
        }

        // Only handle segment concatenation when we just finalized a recording.
        if (justFinalized) {
            if (!recordingSegments.isEmpty()) {
                recordingSegments.add(currentFilePath);
                String concatenatedPath = concatenateAudioSegments(recordingSegments);
                if (concatenatedPath != null) {
                    currentFilePath = concatenatedPath;
                }
            }

            recordingSegments.clear();
            recordingSegments.add(currentFilePath);
            previousSegmentsDuration = currentState.getDuration();
            previousSegmentsAmplitudes = new ArrayList<>(currentState.getAmplitudes());

            currentState.setFilePath(currentFilePath);
            _state.setValue(currentState);
        }

        // Check if file exists
        File audioFile = new File(currentFilePath);
        if (!audioFile.exists()) {
            handleError("Failed to play recording: Audio file not found");
            return false;
        }

        if (audioFile.length() == 0) {
            handleError("Failed to play recording: Audio file is empty");
            return false;
        }

        try {
            // If MediaPlayer already exists and is paused, just resume it
            if (mediaPlayer != null) {
                try {
                    // If currentPosition is 0, playback completed — seek to
                    // the start before resuming so the waveform doesn't flash
                    // full progress from the previous end position.
                    InlineAudioRecorderState state = getCurrentState();
                    if (state.getCurrentPosition() == 0) {
                        mediaPlayer.seekTo(0);
                    }
                    mediaPlayer.start();

                    state.setStatus(InlineAudioRecorderStatus.PLAYING);
                    _state.setValue(state);

                    startPlaybackPolling();
                    return true;
                } catch (IllegalStateException e) {
                    releaseMediaPlayer();
                }
            }

            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(currentFilePath);
            mediaPlayer.prepare();

            // Update state duration to match actual file duration from MediaPlayer.
            // The recording timer can drift slightly from the real audio length,
            // so we use the authoritative value to keep the progress bar accurate.
            int mpDuration = mediaPlayer.getDuration();
            if (mpDuration > 0) {
                InlineAudioRecorderState durationState = getCurrentState();
                durationState.setDuration(mpDuration);
                _state.setValue(durationState);
                // Keep previousSegmentsDuration in sync so that
                // continueRecordingFromCompleted uses the correct offset.
                previousSegmentsDuration = mpDuration;
            }

            mediaPlayer.setOnCompletionListener(mp -> {
                InlineAudioRecorderState state = getCurrentState();
                state.setStatus(InlineAudioRecorderStatus.COMPLETED);
                state.setCurrentPosition(0);
                _state.setValue(state);
                stopPlaybackPolling();
            });

            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                handleError("MediaPlayer error occurred: what=" + what + ", extra=" + extra);
                return true;
            });

            mediaPlayer.start();

            InlineAudioRecorderState state = getCurrentState();
            state.setStatus(InlineAudioRecorderStatus.PLAYING);
            _state.setValue(state);

            startPlaybackPolling();
            return true;
        } catch (IOException e) {
            handleError("Failed to play recording: Unable to prepare MediaPlayer - " + e.getMessage());
            return false;
        } catch (IllegalStateException e) {
            handleError("Failed to play recording: MediaPlayer in invalid state - " + e.getMessage());
            return false;
        }
    }

    /**
     * Pauses audio playback.
     *
     * @return true if pause was successful, false otherwise.
     */
    public boolean pausePlayback() {
        if (mediaPlayer == null || !isPlaying()) {
            return false;
        }

        try {
            mediaPlayer.pause();
            stopPlaybackPolling();

            InlineAudioRecorderState state = getCurrentState();
            state.setStatus(InlineAudioRecorderStatus.COMPLETED);
            state.setCurrentPosition(mediaPlayer.getCurrentPosition());
            _state.setValue(state);

            return true;
        } catch (IllegalStateException e) {
            handleError("Failed to pause playback: " + e.getMessage());
            return false;
        }
    }

    /**
     * Resumes audio playback.
     *
     * @return true if resume was successful, false otherwise.
     */
    public boolean resumePlayback() {
        if (mediaPlayer == null || getCurrentState().getStatus() != InlineAudioRecorderStatus.COMPLETED) {
            return false;
        }

        try {
            mediaPlayer.start();

            // Update state
            InlineAudioRecorderState state = getCurrentState();
            state.setStatus(InlineAudioRecorderStatus.PLAYING);
            _state.setValue(state);

            // Start playback position polling
            startPlaybackPolling();

            return true;
        } catch (IllegalStateException e) {
            handleError("Failed to resume playback: " + e.getMessage());
            return false;
        }
    }

    /**
     * Seeks to a position in the playback.
     *
     * @param progress The progress value in range [0.0, 1.0].
     * @return true if seek was successful, false otherwise.
     */
    public boolean seekTo(float progress) {
        InlineAudioRecorderState currentState = getCurrentState();
        if (currentFilePath == null || 
            (currentState.getStatus() != InlineAudioRecorderStatus.PLAYING &&
             currentState.getStatus() != InlineAudioRecorderStatus.COMPLETED &&
             currentState.getStatus() != InlineAudioRecorderStatus.PAUSED)) {
            return false;
        }

        try {
            // Calculate position
            long duration = currentState.getDuration();
            int position = calculateSeekPosition(progress, duration);

            // If MediaPlayer doesn't exist, create it
            if (mediaPlayer == null) {
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setDataSource(currentFilePath);
                mediaPlayer.prepare();

                mediaPlayer.setOnCompletionListener(mp -> {
                    InlineAudioRecorderState state = getCurrentState();
                    state.setStatus(InlineAudioRecorderStatus.COMPLETED);
                    state.setCurrentPosition(0);
                    _state.setValue(state);
                    stopPlaybackPolling();
                });
            }

            mediaPlayer.seekTo(position);

            // Update state
            InlineAudioRecorderState state = getCurrentState();
            state.setCurrentPosition(position);

            // Start playback if not already playing
            if (!mediaPlayer.isPlaying()) {
                mediaPlayer.start();
                state.setStatus(InlineAudioRecorderStatus.PLAYING);
                startPlaybackPolling();
            }

            _state.setValue(state);

            return true;
        } catch (IOException e) {
            handleError("Failed to seek: Unable to prepare MediaPlayer - " + e.getMessage());
            return false;
        } catch (IllegalStateException e) {
            handleError("Failed to seek: MediaPlayer in invalid state - " + e.getMessage());
            return false;
        }
    }

    /**
     * Calculates the seek position in milliseconds from a progress value.
     *
     * @param progress The progress value in range [0.0, 1.0].
     * @param duration The total duration in milliseconds.
     * @return The calculated position clamped to [0, duration].
     */
    static int calculateSeekPosition(float progress, long duration) {
        float clampedProgress = Math.max(0.0f, Math.min(1.0f, progress));
        long position = (long) (clampedProgress * duration);
        return (int) Math.max(0, Math.min(duration, position));
    }

    /**
     * Stops audio playback.
     */
    public void stopPlayback() {
        stopPlaybackPolling();
        releaseMediaPlayer();

        InlineAudioRecorderState state = getCurrentState();
        if (state.getStatus() == InlineAudioRecorderStatus.PLAYING) {
            state.setStatus(InlineAudioRecorderStatus.COMPLETED);
            state.setCurrentPosition(0);
            _state.setValue(state);
        }
    }

    /**
     * Starts polling for playback position.
     */
    private void startPlaybackPolling() {
        playbackRunnable = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && isPlaying()) {
                    try {
                        int position = mediaPlayer.getCurrentPosition();
                        InlineAudioRecorderState state = getCurrentState();
                        state.setCurrentPosition(position);
                        _state.setValue(state);
                    } catch (IllegalStateException e) {
                        // MediaPlayer may have been released
                    }
                    handler.postDelayed(this, PLAYBACK_POLL_INTERVAL_MS);
                }
            }
        };
        handler.post(playbackRunnable);
    }

    // ==================== Error Handling ====================

    /**
     * Handles an error by setting the ERROR status and error message.
     * <p>
     * This method performs the following cleanup:
     * <ul>
     *     <li>Stops all polling (amplitude, duration, playback)</li>
     *     <li>Releases MediaRecorder resources</li>
     *     <li>Releases MediaPlayer resources</li>
     *     <li>Deletes any partial recording file if recording was in progress</li>
     *     <li>Sets the state to ERROR with the provided error message</li>
     * </ul>
     * </p>
     *
     * @param message The error message describing what went wrong.
     * @see InlineAudioRecorderStatus#ERROR
     */
    void handleError(String message) {
        // Stop all polling
        stopAmplitudePolling();
        stopDurationUpdates();
        stopPlaybackPolling();

        // Release resources
        releaseMediaRecorder();
        releaseMediaPlayer();

        // Delete partial recording file if it exists and recording was in progress
        InlineAudioRecorderState currentState = _state.getValue();
        if (currentState != null && 
            currentState.getStatus() == InlineAudioRecorderStatus.RECORDING &&
            currentFilePath != null) {
            File file = new File(currentFilePath);
            if (file.exists()) {
                file.delete();
            }
        }

        // Update state
        InlineAudioRecorderState state = getCurrentState();
        state.setStatus(InlineAudioRecorderStatus.ERROR);
        state.setErrorMessage(message);
        _state.setValue(state);
    }

    /**
     * Checks if the current state is in ERROR status.
     *
     * @return true if status is ERROR, false otherwise.
     */
    public boolean isError() {
        InlineAudioRecorderState state = _state.getValue();
        return state != null && state.getStatus() == InlineAudioRecorderStatus.ERROR;
    }

    /**
     * Gets the current error message if in ERROR state.
     *
     * @return The error message, or null if not in ERROR state.
     */
    @Nullable
    public String getErrorMessage() {
        InlineAudioRecorderState state = _state.getValue();
        if (state != null && state.getStatus() == InlineAudioRecorderStatus.ERROR) {
            return state.getErrorMessage();
        }
        return null;
    }

    /**
     * Clears the error state and resets to IDLE.
     * Use this to recover from an error state.
     */
    public void clearError() {
        InlineAudioRecorderState state = getCurrentState();
        if (state.getStatus() == InlineAudioRecorderStatus.ERROR) {
            state.reset();
            _state.setValue(state);
        }
    }

    /**
     * Resets the recorder to its initial idle state.
     * This clears all state including amplitudes, duration, file path, and sets status to IDLE.
     * <p>
     * Use this method after submitting a recording to ensure the next
     * recording starts with a clean state.
     * </p>
     */
    public void reset() {
        // Stop all polling
        stopAmplitudePolling();
        stopDurationUpdates();
        stopPlaybackPolling();

        // Release resources
        releaseMediaRecorder();
        releaseMediaPlayer();

        // Reset state
        InlineAudioRecorderState state = getCurrentState();
        state.reset();
        _state.setValue(state);

        // Clear file path
        currentFilePath = null;
        recordingStartTime = 0;
        pausedDuration = 0;
        pauseStartTime = 0;
    }

    // ==================== Helper Methods ====================

    /**
     * Gets the current state, creating a copy to avoid mutation issues.
     *
     * @return A copy of the current state.
     */
    @NonNull
    private InlineAudioRecorderState getCurrentState() {
        InlineAudioRecorderState state = _state.getValue();
        return state != null ? state.copy() : new InlineAudioRecorderState();
    }

    /**
     * Checks if currently recording.
     *
     * @return true if status is RECORDING.
     */
    private boolean isRecording() {
        InlineAudioRecorderState state = _state.getValue();
        return state != null && state.getStatus() == InlineAudioRecorderStatus.RECORDING;
    }

    /**
     * Checks if currently playing.
     *
     * @return true if status is PLAYING.
     */
    private boolean isPlaying() {
        InlineAudioRecorderState state = _state.getValue();
        return state != null && state.getStatus() == InlineAudioRecorderStatus.PLAYING;
    }

    /**
     * Stops amplitude polling.
     */
    private void stopAmplitudePolling() {
        if (amplitudeRunnable != null) {
            handler.removeCallbacks(amplitudeRunnable);
            amplitudeRunnable = null;
        }
    }

    /**
     * Stops duration updates.
     */
    private void stopDurationUpdates() {
        if (durationRunnable != null) {
            handler.removeCallbacks(durationRunnable);
            durationRunnable = null;
        }
    }

    /**
     * Stops playback polling.
     */
    private void stopPlaybackPolling() {
        if (playbackRunnable != null) {
            handler.removeCallbacks(playbackRunnable);
            playbackRunnable = null;
        }
    }

    /**
     * Releases MediaRecorder resources.
     */
    private void releaseMediaRecorder() {
        if (mediaRecorder != null) {
            try {
                mediaRecorder.release();
            } catch (Exception e) {
                // Ignore
            }
            mediaRecorder = null;
        }
    }

    /**
     * Releases MediaPlayer resources.
     */
    private void releaseMediaPlayer() {
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.release();
            } catch (Exception e) {
                // Ignore
            }
            mediaPlayer = null;
        }
    }

    // ==================== Utility Methods ====================

    /**
     * Formats a duration in milliseconds to M:SS format.
     *
     * @param durationMs The duration in milliseconds.
     * @return The formatted duration string.
     */
    @NonNull
    public static String formatDuration(long durationMs) {
        if (durationMs < 0) {
            durationMs = 0;
        }
        long totalSeconds = durationMs / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format(Locale.US, "%d:%02d", minutes, seconds);
    }

    // ==================== Lifecycle ====================

    @Override
    protected void onCleared() {
        super.onCleared();

        // Stop all polling
        stopAmplitudePolling();
        stopDurationUpdates();
        stopPlaybackPolling();

        // Release all resources
        releaseMediaRecorder();
        releaseMediaPlayer();

        // Delete file if recording was in progress
        if (currentFilePath != null) {
            InlineAudioRecorderState state = _state.getValue();
            if (state != null && state.getStatus() == InlineAudioRecorderStatus.RECORDING) {
                File file = new File(currentFilePath);
                if (file.exists()) {
                    file.delete();
                }
            }
        }
    }
}
