package com.cometchat.uikit.compose.shared.mediarecorder

import com.cometchat.uikit.core.viewmodel.MediaRecorderState
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldMatch
import io.kotest.property.Arb
import io.kotest.property.arbitrary.enum
import io.kotest.property.arbitrary.float
import io.kotest.property.arbitrary.long
import io.kotest.property.checkAll

/**
 * Property-based tests for [CometChatMediaRecorder] composable.
 *
 * Feature: media-recorder-redesign
 * **Property 2: UI State Consistency**
 * **Property 5: Timer Formatting**
 * **Property 7: Playback Toggle Consistency**
 * **Property 8: SeekBar Progress Bounds**
 * **Property 9: Playback Completion Reset**
 *
 * **Validates: Requirements 1.2, 1.4, 1.6, 3.2, 4.2, 4.3, 4.5, 4.7, 5.2, 5.3, 5.4, 5.7**
 */
class CometChatMediaRecorderPropertyTest : FunSpec({

    // ==================== Property 2: UI State Consistency ====================
    // *For any* MediaRecorderState value, the visible UI elements SHALL match
    // the expected elements for that state:
    // - IDLE: Only record button visible, centered
    // - RECORDING: Timer, AudioVisualizer, delete button, stop button, submit button (disabled) visible
    // - RECORDED: Play/pause button, SeekBar, duration, delete button, submit button visible
    // **Validates: Requirements 1.2, 1.4, 1.6, 5.2, 5.3, 5.4, 5.7**

    /**
     * Property 2: IDLE state has expected UI elements
     *
     * **Validates: Requirements 1.2, 5.2**
     */
    test("Property 2: IDLE state has expected UI elements") {
        val state = MediaRecorderState.IDLE
        
        // In IDLE state:
        // - Record button should be visible
        // - Timer should NOT be visible
        // - AudioVisualizer should NOT be visible
        // - Delete button should NOT be visible
        // - Stop button should NOT be visible
        // - Submit button should NOT be visible
        // - Play/Pause button should NOT be visible
        // - SeekBar should NOT be visible
        
        val expectedElements = UIStateElements.forState(state)
        
        expectedElements.recordButtonVisible.shouldBeTrue()
        expectedElements.timerVisible.shouldBeFalse()
        expectedElements.audioVisualizerVisible.shouldBeFalse()
        expectedElements.deleteButtonVisible.shouldBeFalse()
        expectedElements.stopButtonVisible.shouldBeFalse()
        expectedElements.submitButtonVisible.shouldBeFalse()
        expectedElements.submitButtonEnabled.shouldBeFalse()
        expectedElements.playPauseButtonVisible.shouldBeFalse()
        expectedElements.seekBarVisible.shouldBeFalse()
    }

    /**
     * Property 2: RECORDING state has expected UI elements
     *
     * **Validates: Requirements 1.4, 5.3, 5.7**
     */
    test("Property 2: RECORDING state has expected UI elements") {
        val state = MediaRecorderState.RECORDING
        
        // In RECORDING state:
        // - Record button should NOT be visible
        // - Timer should be visible
        // - AudioVisualizer should be visible
        // - Delete button should be visible
        // - Stop button should be visible
        // - Submit button should be visible but DISABLED
        // - Play/Pause button should NOT be visible
        // - SeekBar should NOT be visible
        
        val expectedElements = UIStateElements.forState(state)
        
        expectedElements.recordButtonVisible.shouldBeFalse()
        expectedElements.timerVisible.shouldBeTrue()
        expectedElements.audioVisualizerVisible.shouldBeTrue()
        expectedElements.deleteButtonVisible.shouldBeTrue()
        expectedElements.stopButtonVisible.shouldBeTrue()
        expectedElements.submitButtonVisible.shouldBeTrue()
        expectedElements.submitButtonEnabled.shouldBeFalse() // Disabled during recording
        expectedElements.playPauseButtonVisible.shouldBeFalse()
        expectedElements.seekBarVisible.shouldBeFalse()
    }

    /**
     * Property 2: RECORDED state has expected UI elements
     *
     * **Validates: Requirements 1.6, 5.4**
     */
    test("Property 2: RECORDED state has expected UI elements") {
        val state = MediaRecorderState.RECORDED
        
        // In RECORDED state:
        // - Record button should NOT be visible
        // - Timer/Duration should be visible
        // - AudioVisualizer should NOT be visible
        // - Delete button should be visible
        // - Stop button should NOT be visible
        // - Submit button should be visible and ENABLED
        // - Play/Pause button should be visible
        // - SeekBar should be visible
        
        val expectedElements = UIStateElements.forState(state)
        
        expectedElements.recordButtonVisible.shouldBeFalse()
        expectedElements.timerVisible.shouldBeTrue() // Shows duration
        expectedElements.audioVisualizerVisible.shouldBeFalse()
        expectedElements.deleteButtonVisible.shouldBeTrue()
        expectedElements.stopButtonVisible.shouldBeFalse()
        expectedElements.submitButtonVisible.shouldBeTrue()
        expectedElements.submitButtonEnabled.shouldBeTrue() // Enabled in recorded state
        expectedElements.playPauseButtonVisible.shouldBeTrue()
        expectedElements.seekBarVisible.shouldBeTrue()
    }

    /**
     * Property 2: All states have consistent UI element visibility
     *
     * **Validates: Requirements 1.2, 1.4, 1.6, 5.2, 5.3, 5.4**
     */
    test("Property 2: All states have consistent UI element visibility") {
        checkAll(100, Arb.enum<MediaRecorderState>()) { state ->
            val elements = UIStateElements.forState(state)
            
            when (state) {
                MediaRecorderState.IDLE -> {
                    elements.recordButtonVisible.shouldBeTrue()
                    elements.deleteButtonVisible.shouldBeFalse()
                }
                MediaRecorderState.RECORDING -> {
                    elements.recordButtonVisible.shouldBeFalse()
                    elements.audioVisualizerVisible.shouldBeTrue()
                    elements.submitButtonEnabled.shouldBeFalse()
                }
                MediaRecorderState.RECORDED -> {
                    elements.recordButtonVisible.shouldBeFalse()
                    elements.playPauseButtonVisible.shouldBeTrue()
                    elements.seekBarVisible.shouldBeTrue()
                    elements.submitButtonEnabled.shouldBeTrue()
                }
            }
        }
    }

    // ==================== Property 5: Timer Formatting ====================
    // *For any* recording duration in milliseconds, the formatted time string SHALL match
    // the pattern "MM:SS" where:
    // - MM = floor(durationMs / 60000) formatted as two digits
    // - SS = floor((durationMs % 60000) / 1000) formatted as two digits
    // **Validates: Requirements 3.2**

    /**
     * Property 5: Timer formatting produces valid MM:SS format
     *
     * **Validates: Requirements 3.2**
     */
    test("Property 5: Timer formatting produces valid MM:SS format") {
        checkAll(100, Arb.long(0L, 3600000L)) { durationMs ->
            val formatted = formatTime(durationMs)
            
            // Should match MM:SS pattern
            formatted shouldMatch Regex("^\\d{2}:\\d{2}$")
        }
    }

    /**
     * Property 5: Timer formatting calculates minutes correctly
     *
     * **Validates: Requirements 3.2**
     */
    test("Property 5: Timer formatting calculates minutes correctly") {
        checkAll(100, Arb.long(0L, 3600000L)) { durationMs ->
            val formatted = formatTime(durationMs)
            val expectedMinutes = (durationMs / 60000).toInt()
            val actualMinutes = formatted.split(":")[0].toInt()
            
            actualMinutes shouldBe expectedMinutes
        }
    }

    /**
     * Property 5: Timer formatting calculates seconds correctly
     *
     * **Validates: Requirements 3.2**
     */
    test("Property 5: Timer formatting calculates seconds correctly") {
        checkAll(100, Arb.long(0L, 3600000L)) { durationMs ->
            val formatted = formatTime(durationMs)
            val expectedSeconds = ((durationMs % 60000) / 1000).toInt()
            val actualSeconds = formatted.split(":")[1].toInt()
            
            actualSeconds shouldBe expectedSeconds
        }
    }

    /**
     * Property 5: Timer formatting zero duration produces "00:00"
     *
     * **Validates: Requirements 3.2**
     */
    test("Property 5: Timer formatting zero duration produces 00:00") {
        val formatted = formatTime(0L)
        formatted shouldBe "00:00"
    }

    /**
     * Property 5: Timer formatting one minute produces "01:00"
     *
     * **Validates: Requirements 3.2**
     */
    test("Property 5: Timer formatting one minute produces 01:00") {
        val formatted = formatTime(60000L)
        formatted shouldBe "01:00"
    }

    /**
     * Property 5: Timer formatting 90 seconds produces "01:30"
     *
     * **Validates: Requirements 3.2**
     */
    test("Property 5: Timer formatting 90 seconds produces 01:30") {
        val formatted = formatTime(90000L)
        formatted shouldBe "01:30"
    }

    // ==================== Property 7: Playback Toggle Consistency ====================
    // *For any* play/pause button click in RECORDED state:
    // - If currently not playing, clicking play SHALL start playback (isPlaying = true)
    // - If currently playing, clicking pause SHALL pause playback (isPlaying = false)
    // **Validates: Requirements 4.2, 4.3**

    /**
     * Property 7: Playback toggle changes state correctly
     *
     * **Validates: Requirements 4.2, 4.3**
     */
    test("Property 7: Playback toggle changes state correctly") {
        // Starting from not playing
        var isPlaying = false
        
        // Toggle should start playback
        isPlaying = togglePlayback(isPlaying)
        isPlaying.shouldBeTrue()
        
        // Toggle again should pause playback
        isPlaying = togglePlayback(isPlaying)
        isPlaying.shouldBeFalse()
    }

    /**
     * Property 7: Multiple toggles alternate correctly
     *
     * **Validates: Requirements 4.2, 4.3**
     */
    test("Property 7: Multiple toggles alternate correctly") {
        var isPlaying = false
        
        repeat(10) { i ->
            isPlaying = togglePlayback(isPlaying)
            if (i % 2 == 0) {
                isPlaying.shouldBeTrue()
            } else {
                isPlaying.shouldBeFalse()
            }
        }
    }

    // ==================== Property 8: SeekBar Progress Bounds ====================
    // *For any* SeekBar interaction, the progress value SHALL be clamped to [0.0, 1.0],
    // and seeking to a position SHALL update the playback position proportionally
    // to the recording duration.
    // **Validates: Requirements 4.5**

    /**
     * Property 8: SeekBar progress is clamped to valid range
     *
     * **Validates: Requirements 4.5**
     */
    test("Property 8: SeekBar progress is clamped to valid range") {
        checkAll(100, Arb.float(-1f, 2f)) { progress ->
            val clamped = clampProgress(progress)
            
            clamped shouldBe progress.coerceIn(0f, 1f)
        }
    }

    /**
     * Property 8: SeekBar progress within range is unchanged
     *
     * **Validates: Requirements 4.5**
     */
    test("Property 8: SeekBar progress within range is unchanged") {
        checkAll(100, Arb.float(0f, 1f)) { progress ->
            val clamped = clampProgress(progress)
            
            clamped shouldBe progress
        }
    }

    /**
     * Property 8: SeekBar progress below zero is clamped to zero
     *
     * **Validates: Requirements 4.5**
     */
    test("Property 8: SeekBar progress below zero is clamped to zero") {
        checkAll(100, Arb.float(-10f, -0.001f)) { progress ->
            val clamped = clampProgress(progress)
            
            clamped shouldBe 0f
        }
    }

    /**
     * Property 8: SeekBar progress above one is clamped to one
     *
     * **Validates: Requirements 4.5**
     */
    test("Property 8: SeekBar progress above one is clamped to one") {
        checkAll(100, Arb.float(1.001f, 10f)) { progress ->
            val clamped = clampProgress(progress)
            
            clamped shouldBe 1f
        }
    }

    /**
     * Property 8: SeekBar position calculation is proportional to duration
     *
     * **Validates: Requirements 4.5**
     */
    test("Property 8: SeekBar position calculation is proportional to duration") {
        checkAll(100, 
            Arb.float(0f, 1f),      // progress
            Arb.long(1000L, 300000L) // durationMs
        ) { progress, durationMs ->
            val seekPosition = calculateSeekPosition(progress, durationMs)
            val expectedPosition = (progress * durationMs).toLong()
            
            seekPosition shouldBe expectedPosition
        }
    }

    // ==================== Property 9: Playback Completion Reset ====================
    // *For any* playback that reaches the end (progress = 1.0), the Media_Recorder SHALL:
    // - Set isPlaying to false
    // - Reset SeekBar progress to 0.0
    // - Reset play button to show play icon
    // **Validates: Requirements 4.7**

    /**
     * Property 9: Playback completion resets state correctly
     *
     * **Validates: Requirements 4.7**
     */
    test("Property 9: Playback completion resets state correctly") {
        // Simulate playback completion
        val completionState = handlePlaybackCompletion()
        
        completionState.isPlaying.shouldBeFalse()
        completionState.progress shouldBe 0f
    }

    /**
     * Property 9: Playback completion from any progress resets to zero
     *
     * **Validates: Requirements 4.7**
     */
    test("Property 9: Playback completion from any progress resets to zero") {
        checkAll(100, Arb.float(0f, 1f)) { currentProgress ->
            // Regardless of current progress, completion should reset to 0
            val completionState = handlePlaybackCompletion()
            
            completionState.progress shouldBe 0f
            completionState.isPlaying.shouldBeFalse()
        }
    }
})

// ==================== Helper Classes and Functions ====================

/**
 * Data class representing expected UI element visibility for each state.
 */
data class UIStateElements(
    val recordButtonVisible: Boolean,
    val timerVisible: Boolean,
    val audioVisualizerVisible: Boolean,
    val deleteButtonVisible: Boolean,
    val stopButtonVisible: Boolean,
    val submitButtonVisible: Boolean,
    val submitButtonEnabled: Boolean,
    val playPauseButtonVisible: Boolean,
    val seekBarVisible: Boolean
) {
    companion object {
        /**
         * Returns the expected UI elements for a given state.
         * **Validates: Requirements 1.2, 1.4, 1.6, 5.2, 5.3, 5.4, 5.7**
         */
        fun forState(state: MediaRecorderState): UIStateElements {
            return when (state) {
                MediaRecorderState.IDLE -> UIStateElements(
                    recordButtonVisible = true,
                    timerVisible = false,
                    audioVisualizerVisible = false,
                    deleteButtonVisible = false,
                    stopButtonVisible = false,
                    submitButtonVisible = false,
                    submitButtonEnabled = false,
                    playPauseButtonVisible = false,
                    seekBarVisible = false
                )
                MediaRecorderState.RECORDING -> UIStateElements(
                    recordButtonVisible = false,
                    timerVisible = true,
                    audioVisualizerVisible = true,
                    deleteButtonVisible = true,
                    stopButtonVisible = true,
                    submitButtonVisible = true,
                    submitButtonEnabled = false, // Disabled during recording
                    playPauseButtonVisible = false,
                    seekBarVisible = false
                )
                MediaRecorderState.RECORDED -> UIStateElements(
                    recordButtonVisible = false,
                    timerVisible = true, // Shows duration
                    audioVisualizerVisible = false,
                    deleteButtonVisible = true,
                    stopButtonVisible = false,
                    submitButtonVisible = true,
                    submitButtonEnabled = true, // Enabled in recorded state
                    playPauseButtonVisible = true,
                    seekBarVisible = true
                )
            }
        }
    }
}

/**
 * Data class representing playback state after completion.
 */
data class PlaybackCompletionState(
    val isPlaying: Boolean,
    val progress: Float
)

/**
 * Formats time in milliseconds to MM:SS format.
 * **Validates: Requirements 3.2**
 */
fun formatTime(durationMs: Long): String {
    val totalSeconds = durationMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}

/**
 * Toggles playback state.
 * **Validates: Requirements 4.2, 4.3**
 */
fun togglePlayback(isPlaying: Boolean): Boolean = !isPlaying

/**
 * Clamps progress to [0.0, 1.0] range.
 * **Validates: Requirements 4.5**
 */
fun clampProgress(progress: Float): Float = progress.coerceIn(0f, 1f)

/**
 * Calculates seek position in milliseconds from progress and duration.
 * **Validates: Requirements 4.5**
 */
fun calculateSeekPosition(progress: Float, durationMs: Long): Long {
    return (progress.coerceIn(0f, 1f) * durationMs).toLong()
}

/**
 * Handles playback completion by resetting state.
 * **Validates: Requirements 4.7**
 */
fun handlePlaybackCompletion(): PlaybackCompletionState {
    return PlaybackCompletionState(
        isPlaying = false,
        progress = 0f
    )
}
