package com.cometchat.uikit.compose.presentation.shared.mediarecorder

import android.content.Context
import android.media.MediaRecorder
import android.os.Handler
import android.util.Log

/**
 * Enum representing the various states of audio recording.
 */
internal enum class RecordingState {
    /**
     * The state when recording is ready to start.
     */
    START,
    /**
     * The state when audio is currently being recorded.
     */
    RECORDING,
    /**
     * The state when recording is temporarily paused.
     */
    PAUSED,
    /**
     * The state when recording has been stopped.
     */
    STOPPED
}

/**
 * Utility functions for media recording operations
 */

/**
 * Starts recording audio internally. Initializes the MediaRecorder, sets the
 * audio source, output format, output file, and audio encoder, and starts recording.
 */
internal fun startRecording(
    context: Context,
    onStateChange: (RecordingState) -> Unit,
    onRecordingChange: (Boolean) -> Unit,
    onStartTimeChange: (Long) -> Unit,
    onRecorderChange: (MediaRecorder?) -> Unit,
    onFilePathChange: (String?) -> Unit,
    handler: Handler,
    timerRunnable: Runnable
) {
    try {
        val recordedFilePath = "${context.externalCacheDir?.absolutePath}/audio_record.m4a"
        val recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setOutputFile(recordedFilePath)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        }
        
        recorder.prepare()
        recorder.start()
        
        onStateChange(RecordingState.RECORDING)
        onRecordingChange(true)
        onStartTimeChange(System.currentTimeMillis())
        onRecorderChange(recorder)
        onFilePathChange(recordedFilePath)
        
        handler.postDelayed(timerRunnable, 0)
        
        Log.d("MediaRecorder", "Recording started successfully")
    } catch (e: Exception) {
        Log.e("MediaRecorder", "Recording preparation failed: ${e.message}")
        onStateChange(RecordingState.START)
        onRecordingChange(false)
    }
}

/**
 * Pauses the audio recording if it is currently in progress.
 */
internal fun pauseRecording(
    mediaRecorder: MediaRecorder?,
    handler: Handler,
    timerRunnable: Runnable,
    startTime: Long,
    onStateChange: (RecordingState) -> Unit,
    onRecordingChange: (Boolean) -> Unit,
    onPauseTimeChange: (Long) -> Unit
) {
    try {
        mediaRecorder?.pause()
        onStateChange(RecordingState.PAUSED)
        onRecordingChange(false)
        onPauseTimeChange(System.currentTimeMillis() - startTime)
        handler.removeCallbacks(timerRunnable)
        
        Log.d("MediaRecorder", "Recording paused")
    } catch (e: Exception) {
        Log.e("MediaRecorder", "Failed to pause recording: ${e.message}")
    }
}

/**
 * Resumes the audio recording from the paused state.
 */
internal fun resumeRecording(
    mediaRecorder: MediaRecorder?,
    handler: Handler,
    timerRunnable: Runnable,
    pauseTime: Long,
    onStateChange: (RecordingState) -> Unit,
    onRecordingChange: (Boolean) -> Unit,
    onStartTimeChange: (Long) -> Unit
) {
    try {
        mediaRecorder?.resume()
        onStateChange(RecordingState.RECORDING)
        onRecordingChange(true)
        onStartTimeChange(System.currentTimeMillis() - pauseTime)
        handler.postDelayed(timerRunnable, 0)
        
        Log.d("MediaRecorder", "Recording resumed")
    } catch (e: Exception) {
        Log.e("MediaRecorder", "Failed to resume recording: ${e.message}")
    }
}

/**
 * Stops the audio recording if it is currently in progress. Releases the
 * MediaRecorder resources and resets the UI elements.
 */
internal fun stopRecording(
    mediaRecorder: MediaRecorder?,
    handler: Handler,
    timerRunnable: Runnable,
    onStateChange: (RecordingState) -> Unit,
    onRecordingChange: (Boolean) -> Unit,
    onTimeChange: (String) -> Unit,
    onRecorderChange: (MediaRecorder?) -> Unit
) {
    try {
        mediaRecorder?.apply {
            stop()
            release()
        }
        
        onStateChange(RecordingState.STOPPED)
        onRecordingChange(false)
        onTimeChange("00:00")
        onRecorderChange(null)
        handler.removeCallbacks(timerRunnable)
        
        Log.d("MediaRecorder", "Recording stopped")
    } catch (e: Exception) {
        Log.e("MediaRecorder", "Failed to stop recording: ${e.message}")
        // Even if stopping fails, clean up the state
        onStateChange(RecordingState.STOPPED)
        onRecordingChange(false)
        onTimeChange("00:00")
        onRecorderChange(null)
        handler.removeCallbacks(timerRunnable)
    }
}

/**
 * Deletes the recorded audio file and invokes the onClose callback if defined.
 */
internal fun deleteRecording(
    mediaRecorder: MediaRecorder?,
    handler: Handler,
    timerRunnable: Runnable,
    onStateChange: (RecordingState) -> Unit,
    onRecordingChange: (Boolean) -> Unit,
    onTimeChange: (String) -> Unit,
    onRecorderChange: (MediaRecorder?) -> Unit,
    onClose: (() -> Unit)?
) {
    stopRecording(
        mediaRecorder = mediaRecorder,
        handler = handler,
        timerRunnable = timerRunnable,
        onStateChange = onStateChange,
        onRecordingChange = onRecordingChange,
        onTimeChange = onTimeChange,
        onRecorderChange = onRecorderChange
    )
    
    onClose?.invoke()
    Log.d("MediaRecorder", "Recording deleted")
}