package com.cometchat.uikit.core.viewmodel

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.enum
import io.kotest.property.arbitrary.float
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.long
import io.kotest.property.checkAll

/**
 * Property-based tests for [CometChatMediaRecorderViewModel].
 * Each test validates a correctness property from the design document.
 *
 * Feature: media-recorder-redesign
 * **Validates: Requirements 1.1, 1.3, 1.5, 1.7, 1.8**
 */
class CometChatMediaRecorderViewModelPropertyTest : FunSpec({

    // Feature: media-recorder-redesign, Property 1: State Machine Validity
    // *For any* sequence of user actions (startRecording, stopRecording, deleteRecording, submitRecording),
    // the Media_Recorder state SHALL always be one of exactly three states (IDLE, RECORDING, RECORDED),
    // and state transitions SHALL follow the valid transition graph.
    // **Validates: Requirements 1.1, 1.3, 1.5, 1.7, 1.8**
    test("Property 1: State machine only allows valid transitions") {
        checkAll(100, Arb.enum<MediaRecorderState>(), Arb.enum<MediaRecorderState>()) { from, to ->
            val viewModel = CometChatMediaRecorderViewModel()
            
            // Define valid transitions according to the state machine
            val validTransitions = setOf(
                MediaRecorderState.IDLE to MediaRecorderState.RECORDING,
                MediaRecorderState.RECORDING to MediaRecorderState.RECORDED,
                MediaRecorderState.RECORDING to MediaRecorderState.IDLE,
                MediaRecorderState.RECORDED to MediaRecorderState.IDLE
            )
            
            val isValidTransition = (from to to) in validTransitions || from == to
            viewModel.isValidTransition(from, to) shouldBe isValidTransition
        }
    }

    // Feature: media-recorder-redesign, Property 1: State Machine Validity (continued)
    // Verify that startRecording only works from IDLE state
    // **Validates: Requirements 1.1, 1.3**
    test("Property 1: startRecording only succeeds from IDLE state") {
        checkAll(100, Arb.enum<MediaRecorderState>()) { initialState ->
            val viewModel = CometChatMediaRecorderViewModel()
            
            // Set up the initial state
            when (initialState) {
                MediaRecorderState.IDLE -> { /* Already in IDLE */ }
                MediaRecorderState.RECORDING -> {
                    viewModel.startRecording()
                }
                MediaRecorderState.RECORDED -> {
                    viewModel.startRecording()
                    viewModel.stopRecording()
                }
            }
            
            // Verify we're in the expected state
            viewModel.recordingState.value shouldBe initialState
            
            // Try to start recording
            val result = viewModel.startRecording()
            
            // Should only succeed from IDLE
            if (initialState == MediaRecorderState.IDLE) {
                result shouldBe true
                viewModel.recordingState.value shouldBe MediaRecorderState.RECORDING
            } else {
                result shouldBe false
                viewModel.recordingState.value shouldBe initialState
            }
        }
    }

    // Feature: media-recorder-redesign, Property 1: State Machine Validity (continued)
    // Verify that stopRecording only works from RECORDING state
    // **Validates: Requirements 1.5**
    test("Property 1: stopRecording only succeeds from RECORDING state") {
        checkAll(100, Arb.enum<MediaRecorderState>()) { initialState ->
            val viewModel = CometChatMediaRecorderViewModel()
            
            // Set up the initial state
            when (initialState) {
                MediaRecorderState.IDLE -> { /* Already in IDLE */ }
                MediaRecorderState.RECORDING -> {
                    viewModel.startRecording()
                }
                MediaRecorderState.RECORDED -> {
                    viewModel.startRecording()
                    viewModel.stopRecording()
                }
            }
            
            // Verify we're in the expected state
            viewModel.recordingState.value shouldBe initialState
            
            // Try to stop recording
            val result = viewModel.stopRecording()
            
            // Should only succeed from RECORDING
            if (initialState == MediaRecorderState.RECORDING) {
                result shouldBe true
                viewModel.recordingState.value shouldBe MediaRecorderState.RECORDED
            } else {
                result shouldBe false
                viewModel.recordingState.value shouldBe initialState
            }
        }
    }

    // Feature: media-recorder-redesign, Property 1: State Machine Validity (continued)
    // Verify that deleteRecording works from RECORDING or RECORDED states
    // **Validates: Requirements 1.7**
    test("Property 1: deleteRecording succeeds from RECORDING or RECORDED states") {
        checkAll(100, Arb.enum<MediaRecorderState>()) { initialState ->
            val viewModel = CometChatMediaRecorderViewModel()
            
            // Set up the initial state
            when (initialState) {
                MediaRecorderState.IDLE -> { /* Already in IDLE */ }
                MediaRecorderState.RECORDING -> {
                    viewModel.startRecording()
                }
                MediaRecorderState.RECORDED -> {
                    viewModel.startRecording()
                    viewModel.stopRecording()
                }
            }
            
            // Verify we're in the expected state
            viewModel.recordingState.value shouldBe initialState
            
            // Try to delete recording
            val result = viewModel.deleteRecording()
            
            // Should succeed from RECORDING or RECORDED, fail from IDLE
            if (initialState == MediaRecorderState.IDLE) {
                result shouldBe false
                viewModel.recordingState.value shouldBe MediaRecorderState.IDLE
            } else {
                result shouldBe true
                viewModel.recordingState.value shouldBe MediaRecorderState.IDLE
            }
        }
    }

    // Feature: media-recorder-redesign, Property 1: State Machine Validity (continued)
    // Verify that handleError transitions to IDLE from any state
    // **Validates: Requirements 1.8**
    test("Property 1: handleError transitions to IDLE from any state") {
        checkAll(100, Arb.enum<MediaRecorderState>()) { initialState ->
            val viewModel = CometChatMediaRecorderViewModel()
            
            // Set up the initial state
            when (initialState) {
                MediaRecorderState.IDLE -> { /* Already in IDLE */ }
                MediaRecorderState.RECORDING -> {
                    viewModel.startRecording()
                }
                MediaRecorderState.RECORDED -> {
                    viewModel.startRecording()
                    viewModel.stopRecording()
                }
            }
            
            // Verify we're in the expected state
            viewModel.recordingState.value shouldBe initialState
            
            // Handle an error
            val testException = Exception("Test error")
            viewModel.handleError(testException)
            
            // Should always transition to IDLE
            viewModel.recordingState.value shouldBe MediaRecorderState.IDLE
            viewModel.error.value shouldBe testException
        }
    }

    // Feature: media-recorder-redesign, Property 1: State Machine Validity (continued)
    // Verify that state is always one of exactly three states
    // **Validates: Requirements 1.1**
    test("Property 1: State is always one of exactly three valid states") {
        val viewModel = CometChatMediaRecorderViewModel()
        val validStates = setOf(MediaRecorderState.IDLE, MediaRecorderState.RECORDING, MediaRecorderState.RECORDED)
        
        // Initial state
        validStates.contains(viewModel.recordingState.value) shouldBe true
        
        // After startRecording
        viewModel.startRecording()
        validStates.contains(viewModel.recordingState.value) shouldBe true
        
        // After stopRecording
        viewModel.stopRecording()
        validStates.contains(viewModel.recordingState.value) shouldBe true
        
        // After deleteRecording
        viewModel.deleteRecording()
        validStates.contains(viewModel.recordingState.value) shouldBe true
    }

    // Feature: media-recorder-redesign, Property 5: Timer Formatting
    // *For any* recording duration in milliseconds, the formatted time string SHALL match
    // the pattern "MM:SS" where MM and SS are two-digit numbers.
    // **Validates: Requirements 3.2**
    test("Property 5: Timer formatting produces valid MM:SS format") {
        checkAll(100, Arb.long(0L, 3600000L)) { durationMs ->
            val viewModel = CometChatMediaRecorderViewModel()
            val formatted = viewModel.formatTime(durationMs)
            
            // Should match MM:SS pattern
            val pattern = Regex("^\\d{2}:\\d{2}$")
            pattern.matches(formatted) shouldBe true
            
            // Verify the values are correct
            val parts = formatted.split(":")
            val minutes = parts[0].toInt()
            val seconds = parts[1].toInt()
            
            val expectedMinutes = (durationMs / 1000 / 60).toInt()
            val expectedSeconds = ((durationMs / 1000) % 60).toInt()
            
            minutes shouldBe expectedMinutes
            seconds shouldBe expectedSeconds
        }
    }

    // Feature: media-recorder-redesign, Property 6: Timer State Preservation
    // *For any* state transition RECORDING → RECORDED, timer value SHALL be preserved
    // **Validates: Requirements 3.4**
    test("Property 6: Timer is preserved when transitioning from RECORDING to RECORDED") {
        checkAll(100, Arb.long(1000L, 60000L)) { durationMs ->
            val viewModel = CometChatMediaRecorderViewModel()
            
            // Start recording and update time
            viewModel.startRecording()
            viewModel.updateRecordingTime(durationMs)
            val expectedTime = viewModel.formatTime(durationMs)
            viewModel.recordingTime.value shouldBe expectedTime
            
            // Stop recording (transition to RECORDED)
            viewModel.stopRecording()
            
            // Timer should be preserved
            viewModel.recordingTime.value shouldBe expectedTime
            viewModel.recordingDurationMs.value shouldBe durationMs
        }
    }

    // Feature: media-recorder-redesign, Property 6: Timer State Preservation (continued)
    // *For any* state transition to IDLE, timer SHALL reset to "00:00"
    // **Validates: Requirements 3.5**
    test("Property 6: Timer resets to 00:00 when transitioning to IDLE") {
        checkAll(100, Arb.long(1000L, 60000L)) { durationMs ->
            val viewModel = CometChatMediaRecorderViewModel()
            
            // Start recording and update time
            viewModel.startRecording()
            viewModel.updateRecordingTime(durationMs)
            viewModel.recordingTime.value shouldBe viewModel.formatTime(durationMs)
            
            // Delete recording (transition to IDLE)
            viewModel.deleteRecording()
            
            // Timer should be reset
            viewModel.recordingTime.value shouldBe "00:00"
            viewModel.recordingDurationMs.value shouldBe 0L
        }
    }

    // Feature: media-recorder-redesign, Property 6: Timer State Preservation (continued)
    // Timer resets when transitioning from RECORDED to IDLE via deleteRecording
    // **Validates: Requirements 3.5**
    test("Property 6: Timer resets when deleting from RECORDED state") {
        checkAll(100, Arb.long(1000L, 60000L)) { durationMs ->
            val viewModel = CometChatMediaRecorderViewModel()
            
            // Set up RECORDED state with timer
            viewModel.startRecording()
            viewModel.updateRecordingTime(durationMs)
            viewModel.stopRecording()
            
            // Verify timer is preserved in RECORDED state
            viewModel.recordingTime.value shouldBe viewModel.formatTime(durationMs)
            
            // Delete recording (transition to IDLE)
            viewModel.deleteRecording()
            
            // Timer should be reset
            viewModel.recordingTime.value shouldBe "00:00"
            viewModel.recordingDurationMs.value shouldBe 0L
        }
    }

    // Feature: media-recorder-redesign, Property 6: Timer State Preservation (continued)
    // Timer resets when error occurs
    // **Validates: Requirements 3.5**
    test("Property 6: Timer resets on error") {
        checkAll(100, Arb.long(1000L, 60000L)) { durationMs ->
            val viewModel = CometChatMediaRecorderViewModel()
            
            // Start recording and update time
            viewModel.startRecording()
            viewModel.updateRecordingTime(durationMs)
            viewModel.recordingTime.value shouldBe viewModel.formatTime(durationMs)
            
            // Handle error (transition to IDLE)
            viewModel.handleError(Exception("Test error"))
            
            // Timer should be reset
            viewModel.recordingTime.value shouldBe "00:00"
            viewModel.recordingDurationMs.value shouldBe 0L
        }
    }

    // Feature: media-recorder-redesign, Property 8: SeekBar Progress Bounds
    // *For any* SeekBar interaction, the progress value SHALL be clamped to [0.0, 1.0]
    // **Validates: Requirements 4.5**
    test("Property 8: SeekBar progress is clamped to valid range") {
        checkAll(100, Arb.float(-1f, 2f)) { progress ->
            val viewModel = CometChatMediaRecorderViewModel()
            
            viewModel.seekTo(progress)
            
            val clampedProgress = viewModel.playbackProgress.value
            clampedProgress shouldBe progress.coerceIn(0f, 1f)
            (clampedProgress >= 0f && clampedProgress <= 1f) shouldBe true
        }
    }

    // Feature: media-recorder-redesign, Property 8: SeekBar Progress Bounds (continued)
    // Verify updatePlaybackProgress also clamps values
    // **Validates: Requirements 4.5**
    test("Property 8: updatePlaybackProgress clamps to valid range") {
        checkAll(100, Arb.float(-1f, 2f)) { progress ->
            val viewModel = CometChatMediaRecorderViewModel()
            
            viewModel.updatePlaybackProgress(progress)
            
            val clampedProgress = viewModel.playbackProgress.value
            clampedProgress shouldBe progress.coerceIn(0f, 1f)
            (clampedProgress >= 0f && clampedProgress <= 1f) shouldBe true
        }
    }

    // Feature: media-recorder-redesign, Property 7: Playback Toggle Consistency
    // *For any* play/pause button click in RECORDED state, the isPlaying state SHALL toggle
    // **Validates: Requirements 4.2, 4.3**
    test("Property 7: Playback toggle is consistent") {
        val viewModel = CometChatMediaRecorderViewModel()
        
        // Set up RECORDED state
        viewModel.startRecording()
        viewModel.stopRecording()
        viewModel.recordingState.value shouldBe MediaRecorderState.RECORDED
        
        // Initial state should be not playing
        viewModel.isPlaying.value shouldBe false
        
        // Toggle to playing
        viewModel.togglePlayback() shouldBe true
        viewModel.isPlaying.value shouldBe true
        
        // Toggle to not playing
        viewModel.togglePlayback() shouldBe true
        viewModel.isPlaying.value shouldBe false
    }

    // Feature: media-recorder-redesign, Property 7: Playback Toggle Consistency (continued)
    // Verify startPlayback and pausePlayback work correctly
    // **Validates: Requirements 4.2, 4.3**
    test("Property 7: startPlayback and pausePlayback work correctly") {
        val viewModel = CometChatMediaRecorderViewModel()
        
        // Set up RECORDED state
        viewModel.startRecording()
        viewModel.stopRecording()
        
        // Start playback
        viewModel.startPlayback() shouldBe true
        viewModel.isPlaying.value shouldBe true
        
        // Pause playback
        viewModel.pausePlayback() shouldBe true
        viewModel.isPlaying.value shouldBe false
        
        // Pause when already paused should fail
        viewModel.pausePlayback() shouldBe false
    }

    // Feature: media-recorder-redesign, Property 9: Playback Completion Reset
    // *For any* playback that reaches the end, the Media_Recorder SHALL reset
    // **Validates: Requirements 4.7**
    test("Property 9: Playback completion resets state") {
        checkAll(100, Arb.float(0.1f, 0.9f)) { progress ->
            val viewModel = CometChatMediaRecorderViewModel()
            
            // Set up RECORDED state with playback in progress
            viewModel.startRecording()
            viewModel.stopRecording()
            viewModel.startPlayback()
            viewModel.updatePlaybackProgress(progress)
            
            viewModel.isPlaying.value shouldBe true
            viewModel.playbackProgress.value shouldBe progress
            
            // Simulate playback completion
            viewModel.onPlaybackComplete()
            
            // Should reset
            viewModel.isPlaying.value shouldBe false
            viewModel.playbackProgress.value shouldBe 0f
        }
    }

    // Feature: media-recorder-redesign, Property: Amplitude bounds
    // Verify amplitude is always clamped to [0.0, 1.0]
    test("Amplitude is clamped to valid range") {
        checkAll(100, Arb.float(-1f, 2f)) { amplitude ->
            val viewModel = CometChatMediaRecorderViewModel()
            
            viewModel.updateAmplitude(amplitude)
            
            val clampedAmplitude = viewModel.audioAmplitude.value
            clampedAmplitude shouldBe amplitude.coerceIn(0f, 1f)
            (clampedAmplitude >= 0f && clampedAmplitude <= 1f) shouldBe true
        }
    }

    // Feature: media-recorder-redesign, Property: Initial state
    // Verify initial state is IDLE
    test("Initial state is IDLE") {
        val viewModel = CometChatMediaRecorderViewModel()
        
        viewModel.recordingState.value shouldBe MediaRecorderState.IDLE
        viewModel.recordingTime.value shouldBe "00:00"
        viewModel.recordingDurationMs.value shouldBe 0L
        viewModel.audioAmplitude.value shouldBe 0f
        viewModel.playbackProgress.value shouldBe 0f
        viewModel.isPlaying.value shouldBe false
        viewModel.recordedFile.value shouldBe null
        viewModel.error.value shouldBe null
    }

    // Feature: media-recorder-redesign, Property: Release clears all state
    test("Release clears all state") {
        val viewModel = CometChatMediaRecorderViewModel()
        
        // Set up some state
        viewModel.startRecording()
        viewModel.updateRecordingTime(5000L)
        viewModel.updateAmplitude(0.5f)
        viewModel.stopRecording()
        viewModel.startPlayback()
        viewModel.updatePlaybackProgress(0.5f)
        
        // Release
        viewModel.release()
        
        // All state should be reset
        viewModel.recordingState.value shouldBe MediaRecorderState.IDLE
        viewModel.recordingTime.value shouldBe "00:00"
        viewModel.recordingDurationMs.value shouldBe 0L
        viewModel.audioAmplitude.value shouldBe 0f
        viewModel.playbackProgress.value shouldBe 0f
        viewModel.isPlaying.value shouldBe false
        viewModel.recordedFile.value shouldBe null
        viewModel.error.value shouldBe null
    }

    // Feature: media-recorder-redesign, Property 12: Delete Action File Cleanup
    // *For any* delete action (deleteRecording), if a recorded file exists,
    // the file reference SHALL be cleared and the state SHALL transition to IDLE.
    // **Validates: Requirements 1.7, 5.5**
    test("Property 12: Delete action clears file reference and transitions to IDLE") {
        checkAll(100, Arb.enum<MediaRecorderState>()) { initialState ->
            val viewModel = CometChatMediaRecorderViewModel()
            
            // Set up the initial state
            when (initialState) {
                MediaRecorderState.IDLE -> { /* Already in IDLE */ }
                MediaRecorderState.RECORDING -> {
                    viewModel.startRecording()
                }
                MediaRecorderState.RECORDED -> {
                    viewModel.startRecording()
                    viewModel.stopRecording()
                }
            }
            
            // Verify we're in the expected state
            viewModel.recordingState.value shouldBe initialState
            
            // Try to delete recording
            val result = viewModel.deleteRecording()
            
            // Should succeed from RECORDING or RECORDED, fail from IDLE
            if (initialState == MediaRecorderState.IDLE) {
                result shouldBe false
                viewModel.recordingState.value shouldBe MediaRecorderState.IDLE
            } else {
                result shouldBe true
                viewModel.recordingState.value shouldBe MediaRecorderState.IDLE
                // File reference should be cleared
                viewModel.recordedFile.value shouldBe null
            }
        }
    }

    // Feature: media-recorder-redesign, Property 12: Delete Action File Cleanup (continued)
    // Verify that delete action clears file reference when file was set
    // **Validates: Requirements 1.7, 5.5**
    test("Property 12: Delete action clears recorded file reference") {
        val viewModel = CometChatMediaRecorderViewModel()
        
        // Set up RECORDED state with a file reference
        viewModel.startRecording()
        viewModel.stopRecording()
        
        // Simulate setting a recorded file (in real usage, MediaRecorderManager would do this)
        val mockFile = java.io.File.createTempFile("test_recording", ".m4a")
        try {
            viewModel.setRecordedFile(mockFile)
            viewModel.recordedFile.value shouldBe mockFile
            
            // Delete recording
            viewModel.deleteRecording()
            
            // File reference should be cleared
            viewModel.recordedFile.value shouldBe null
            viewModel.recordingState.value shouldBe MediaRecorderState.IDLE
        } finally {
            // Clean up temp file
            mockFile.delete()
        }
    }

    // Feature: media-recorder-redesign, Property 12: Delete Action File Cleanup (continued)
    // Verify delete from RECORDING state also clears any partial file reference
    // **Validates: Requirements 1.7**
    test("Property 12: Delete from RECORDING state clears file reference") {
        val viewModel = CometChatMediaRecorderViewModel()
        
        // Start recording
        viewModel.startRecording()
        viewModel.recordingState.value shouldBe MediaRecorderState.RECORDING
        
        // Simulate setting a file reference during recording
        val mockFile = java.io.File.createTempFile("test_recording", ".m4a")
        try {
            viewModel.setRecordedFile(mockFile)
            
            // Delete recording while still recording
            viewModel.deleteRecording()
            
            // File reference should be cleared
            viewModel.recordedFile.value shouldBe null
            viewModel.recordingState.value shouldBe MediaRecorderState.IDLE
        } finally {
            // Clean up temp file
            mockFile.delete()
        }
    }
})
