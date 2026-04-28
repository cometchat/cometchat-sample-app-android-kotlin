package com.cometchat.uikit.core.viewmodel

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.element
import io.kotest.property.arbitrary.enum
import io.kotest.property.arbitrary.float
import io.kotest.property.arbitrary.long
import io.kotest.property.checkAll

/**
 * Property-based tests for [CometChatInlineAudioRecorderViewModel].
 * Each test validates a correctness property from the design document.
 *
 * Feature: inline-audio-recorder
 * **Validates: Requirements 1.1, 1.3, 1.5, 1.7, 1.8, 1.10, 1.12, 1.13, 1.14, 1.15, 7.1, 7.2, 9.2, 13.1, 13.6**
 */
class CometChatInlineAudioRecorderViewModelPropertyTest : FunSpec({

    // ==================== Property 1: State Machine Validity ====================
    
    // Feature: inline-audio-recorder, Property 1: State Machine Validity
    // *For any* sequence of user actions, the Inline_Audio_Recorder status SHALL always be
    // one of exactly six states (IDLE, RECORDING, PAUSED, COMPLETED, PLAYING, ERROR),
    // and state transitions SHALL follow the valid transition graph.
    // **Validates: Requirements 1.1, 1.3, 1.5, 1.7, 1.8, 1.10, 1.12, 1.13, 1.14, 1.15**
    test("Property 1: State machine only allows valid transitions") {
        checkAll(100, Arb.enum<InlineAudioRecorderStatus>(), Arb.enum<InlineAudioRecorderStatus>()) { from, to ->
            val viewModel = CometChatInlineAudioRecorderViewModel()
            
            // Define valid transitions according to the state machine
            val validTransitions = setOf(
                InlineAudioRecorderStatus.IDLE to InlineAudioRecorderStatus.RECORDING,
                InlineAudioRecorderStatus.RECORDING to InlineAudioRecorderStatus.PAUSED,
                InlineAudioRecorderStatus.RECORDING to InlineAudioRecorderStatus.COMPLETED,
                InlineAudioRecorderStatus.RECORDING to InlineAudioRecorderStatus.IDLE,
                InlineAudioRecorderStatus.RECORDING to InlineAudioRecorderStatus.ERROR,
                InlineAudioRecorderStatus.PAUSED to InlineAudioRecorderStatus.RECORDING,
                InlineAudioRecorderStatus.PAUSED to InlineAudioRecorderStatus.COMPLETED,
                InlineAudioRecorderStatus.PAUSED to InlineAudioRecorderStatus.IDLE,
                InlineAudioRecorderStatus.PAUSED to InlineAudioRecorderStatus.ERROR,
                InlineAudioRecorderStatus.COMPLETED to InlineAudioRecorderStatus.PLAYING,
                InlineAudioRecorderStatus.COMPLETED to InlineAudioRecorderStatus.IDLE,
                InlineAudioRecorderStatus.PLAYING to InlineAudioRecorderStatus.COMPLETED,
                InlineAudioRecorderStatus.PLAYING to InlineAudioRecorderStatus.IDLE,
                InlineAudioRecorderStatus.ERROR to InlineAudioRecorderStatus.IDLE
            )
            
            val isValidTransition = (from to to) in validTransitions || from == to
            viewModel.isValidTransition(from, to) shouldBe isValidTransition
        }
    }

    // Feature: inline-audio-recorder, Property 1: State Machine Validity (continued)
    // Verify that startRecording only works from IDLE state
    // **Validates: Requirements 1.1, 1.3**
    test("Property 1: startRecording only succeeds from IDLE state") {
        checkAll(100, Arb.enum<InlineAudioRecorderStatus>()) { initialStatus ->
            val viewModel = CometChatInlineAudioRecorderViewModel()
            
            // Set up the initial state
            viewModel.setState(InlineAudioRecorderState(status = initialStatus))
            viewModel.status shouldBe initialStatus
            
            // Try to start recording
            val result = viewModel.startRecording()
            
            // Should only succeed from IDLE
            if (initialStatus == InlineAudioRecorderStatus.IDLE) {
                result shouldBe true
                viewModel.status shouldBe InlineAudioRecorderStatus.RECORDING
            } else {
                result shouldBe false
                viewModel.status shouldBe initialStatus
            }
        }
    }

    // Feature: inline-audio-recorder, Property 1: State Machine Validity (continued)
    // Verify that pauseRecording only works from RECORDING state
    // **Validates: Requirements 1.5**
    test("Property 1: pauseRecording only succeeds from RECORDING state") {
        checkAll(100, Arb.enum<InlineAudioRecorderStatus>()) { initialStatus ->
            val viewModel = CometChatInlineAudioRecorderViewModel()
            
            // Set up the initial state
            viewModel.setState(InlineAudioRecorderState(status = initialStatus))
            viewModel.status shouldBe initialStatus
            
            // Try to pause recording
            val result = viewModel.pauseRecording()
            
            // Should only succeed from RECORDING
            if (initialStatus == InlineAudioRecorderStatus.RECORDING) {
                result shouldBe true
                viewModel.status shouldBe InlineAudioRecorderStatus.PAUSED
            } else {
                result shouldBe false
                viewModel.status shouldBe initialStatus
            }
        }
    }

    // Feature: inline-audio-recorder, Property 1: State Machine Validity (continued)
    // Verify that resumeRecording only works from PAUSED state
    // **Validates: Requirements 1.7**
    test("Property 1: resumeRecording only succeeds from PAUSED state") {
        checkAll(100, Arb.enum<InlineAudioRecorderStatus>()) { initialStatus ->
            val viewModel = CometChatInlineAudioRecorderViewModel()
            
            // Set up the initial state
            viewModel.setState(InlineAudioRecorderState(status = initialStatus))
            viewModel.status shouldBe initialStatus
            
            // Try to resume recording
            val result = viewModel.resumeRecording()
            
            // Should only succeed from PAUSED
            if (initialStatus == InlineAudioRecorderStatus.PAUSED) {
                result shouldBe true
                viewModel.status shouldBe InlineAudioRecorderStatus.RECORDING
            } else {
                result shouldBe false
                viewModel.status shouldBe initialStatus
            }
        }
    }

    // Feature: inline-audio-recorder, Property 1: State Machine Validity (continued)
    // Verify that stopRecording works from RECORDING or PAUSED states
    // **Validates: Requirements 1.8**
    test("Property 1: stopRecording succeeds from RECORDING or PAUSED states") {
        checkAll(100, Arb.enum<InlineAudioRecorderStatus>()) { initialStatus ->
            val viewModel = CometChatInlineAudioRecorderViewModel()
            
            // Set up the initial state
            viewModel.setState(InlineAudioRecorderState(status = initialStatus))
            viewModel.status shouldBe initialStatus
            
            // Try to stop recording
            val result = viewModel.stopRecording()
            
            // Should succeed from RECORDING or PAUSED
            if (initialStatus == InlineAudioRecorderStatus.RECORDING || 
                initialStatus == InlineAudioRecorderStatus.PAUSED) {
                result shouldBe true
                viewModel.status shouldBe InlineAudioRecorderStatus.COMPLETED
            } else {
                result shouldBe false
                viewModel.status shouldBe initialStatus
            }
        }
    }

    // Feature: inline-audio-recorder, Property 1: State Machine Validity (continued)
    // Verify that startPlayback only works from COMPLETED state
    // **Validates: Requirements 1.10**
    test("Property 1: startPlayback only succeeds from COMPLETED state") {
        checkAll(100, Arb.enum<InlineAudioRecorderStatus>()) { initialStatus ->
            val viewModel = CometChatInlineAudioRecorderViewModel()
            
            // Set up the initial state
            viewModel.setState(InlineAudioRecorderState(status = initialStatus))
            viewModel.status shouldBe initialStatus
            
            // Try to start playback
            val result = viewModel.startPlayback()
            
            // Should only succeed from COMPLETED
            if (initialStatus == InlineAudioRecorderStatus.COMPLETED) {
                result shouldBe true
                viewModel.status shouldBe InlineAudioRecorderStatus.PLAYING
            } else {
                result shouldBe false
                viewModel.status shouldBe initialStatus
            }
        }
    }

    // Feature: inline-audio-recorder, Property 1: State Machine Validity (continued)
    // Verify that pausePlayback only works from PLAYING state
    // **Validates: Requirements 1.12**
    test("Property 1: pausePlayback only succeeds from PLAYING state") {
        checkAll(100, Arb.enum<InlineAudioRecorderStatus>()) { initialStatus ->
            val viewModel = CometChatInlineAudioRecorderViewModel()
            
            // Set up the initial state
            viewModel.setState(InlineAudioRecorderState(status = initialStatus))
            viewModel.status shouldBe initialStatus
            
            // Try to pause playback
            val result = viewModel.pausePlayback()
            
            // Should only succeed from PLAYING
            if (initialStatus == InlineAudioRecorderStatus.PLAYING) {
                result shouldBe true
                viewModel.status shouldBe InlineAudioRecorderStatus.COMPLETED
            } else {
                result shouldBe false
                viewModel.status shouldBe initialStatus
            }
        }
    }

    // Feature: inline-audio-recorder, Property 1: State Machine Validity (continued)
    // Verify that onPlaybackComplete transitions from PLAYING to COMPLETED
    // **Validates: Requirements 1.13**
    test("Property 1: onPlaybackComplete transitions from PLAYING to COMPLETED") {
        checkAll(100, Arb.enum<InlineAudioRecorderStatus>()) { initialStatus ->
            val viewModel = CometChatInlineAudioRecorderViewModel()
            
            // Set up the initial state with some position
            viewModel.setState(InlineAudioRecorderState(
                status = initialStatus,
                duration = 5000L,
                currentPosition = 3000L
            ))
            
            // Call onPlaybackComplete
            viewModel.onPlaybackComplete()
            
            // Should only transition from PLAYING
            if (initialStatus == InlineAudioRecorderStatus.PLAYING) {
                viewModel.status shouldBe InlineAudioRecorderStatus.COMPLETED
                viewModel.currentPosition shouldBe 0L // Position should reset
            } else {
                viewModel.status shouldBe initialStatus
            }
        }
    }

    // Feature: inline-audio-recorder, Property 1: State Machine Validity (continued)
    // Verify that deleteRecording works from any non-IDLE state
    // **Validates: Requirements 1.14**
    test("Property 1: deleteRecording succeeds from any non-IDLE state") {
        val nonIdleStates = listOf(
            InlineAudioRecorderStatus.RECORDING,
            InlineAudioRecorderStatus.PAUSED,
            InlineAudioRecorderStatus.COMPLETED,
            InlineAudioRecorderStatus.PLAYING,
            InlineAudioRecorderStatus.ERROR
        )
        
        checkAll(100, Arb.element(nonIdleStates)) { initialStatus ->
            val viewModel = CometChatInlineAudioRecorderViewModel()
            
            // Set up the initial state
            viewModel.setState(InlineAudioRecorderState(status = initialStatus))
            viewModel.status shouldBe initialStatus
            
            // Try to delete recording
            val result = viewModel.deleteRecording()
            
            // Should succeed from any non-IDLE state
            result shouldBe true
            viewModel.status shouldBe InlineAudioRecorderStatus.IDLE
        }
    }

    // Feature: inline-audio-recorder, Property 1: State Machine Validity (continued)
    // Verify that deleteRecording fails from IDLE state
    // **Validates: Requirements 1.14**
    test("Property 1: deleteRecording fails from IDLE state") {
        val viewModel = CometChatInlineAudioRecorderViewModel()
        
        // Already in IDLE state
        viewModel.status shouldBe InlineAudioRecorderStatus.IDLE
        
        // Try to delete recording
        val result = viewModel.deleteRecording()
        
        // Should fail from IDLE
        result shouldBe false
        viewModel.status shouldBe InlineAudioRecorderStatus.IDLE
    }

    // Feature: inline-audio-recorder, Property 1: State Machine Validity (continued)
    // Verify that handleError transitions to ERROR state
    // **Validates: Requirements 1.15**
    test("Property 1: handleError transitions to ERROR state") {
        checkAll(100, Arb.enum<InlineAudioRecorderStatus>()) { initialStatus ->
            val viewModel = CometChatInlineAudioRecorderViewModel()
            
            // Set up the initial state
            viewModel.setState(InlineAudioRecorderState(status = initialStatus))
            
            // Handle an error
            val errorMessage = "Test error message"
            viewModel.handleError(errorMessage)
            
            // Should always transition to ERROR
            viewModel.status shouldBe InlineAudioRecorderStatus.ERROR
            viewModel.errorMessage shouldBe errorMessage
        }
    }

    // Feature: inline-audio-recorder, Property 1: State Machine Validity (continued)
    // Verify that recover only works from ERROR state
    // **Validates: Requirements 1.15**
    test("Property 1: recover only succeeds from ERROR state") {
        checkAll(100, Arb.enum<InlineAudioRecorderStatus>()) { initialStatus ->
            val viewModel = CometChatInlineAudioRecorderViewModel()
            
            // Set up the initial state
            viewModel.setState(InlineAudioRecorderState(status = initialStatus))
            viewModel.status shouldBe initialStatus
            
            // Try to recover
            val result = viewModel.recover()
            
            // Should only succeed from ERROR
            if (initialStatus == InlineAudioRecorderStatus.ERROR) {
                result shouldBe true
                viewModel.status shouldBe InlineAudioRecorderStatus.IDLE
            } else {
                result shouldBe false
                viewModel.status shouldBe initialStatus
            }
        }
    }

    // Feature: inline-audio-recorder, Property 1: State Machine Validity (continued)
    // Verify that state is always one of exactly six valid states
    // **Validates: Requirements 1.1**
    test("Property 1: State is always one of exactly six valid states") {
        val viewModel = CometChatInlineAudioRecorderViewModel()
        val validStates = InlineAudioRecorderStatus.values().toSet()
        
        // Initial state
        validStates.contains(viewModel.status) shouldBe true
        
        // After startRecording
        viewModel.startRecording()
        validStates.contains(viewModel.status) shouldBe true
        
        // After pauseRecording
        viewModel.pauseRecording()
        validStates.contains(viewModel.status) shouldBe true
        
        // After resumeRecording
        viewModel.resumeRecording()
        validStates.contains(viewModel.status) shouldBe true
        
        // After stopRecording
        viewModel.stopRecording()
        validStates.contains(viewModel.status) shouldBe true
        
        // After startPlayback
        viewModel.startPlayback()
        validStates.contains(viewModel.status) shouldBe true
        
        // After pausePlayback
        viewModel.pausePlayback()
        validStates.contains(viewModel.status) shouldBe true
        
        // After deleteRecording
        viewModel.deleteRecording()
        validStates.contains(viewModel.status) shouldBe true
    }

    // ==================== Property 7: Timer Formatting ====================

    // Feature: inline-audio-recorder, Property 7: Timer Formatting
    // *For any* duration in milliseconds, the formatted time string SHALL match
    // the pattern "MM:SS" where MM and SS are two-digit numbers.
    // **Validates: Requirements 7.1, 7.2**
    test("Property 7: Timer formatting produces valid MM:SS format") {
        checkAll(100, Arb.long(0L, 3600000L)) { durationMs ->
            val viewModel = CometChatInlineAudioRecorderViewModel()
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

    // Feature: inline-audio-recorder, Property 7: Timer Formatting (continued)
    // Verify formattedDuration and formattedPosition use formatTime correctly
    // **Validates: Requirements 7.1, 7.2**
    test("Property 7: formattedDuration and formattedPosition are correct") {
        checkAll(100, Arb.long(0L, 300000L), Arb.long(0L, 300000L)) { duration, position ->
            val viewModel = CometChatInlineAudioRecorderViewModel()
            
            viewModel.updateDuration(duration)
            viewModel.updatePosition(position.coerceAtMost(duration))
            
            viewModel.formattedDuration shouldBe viewModel.formatTime(duration)
            viewModel.formattedPosition shouldBe viewModel.formatTime(position.coerceAtMost(duration))
        }
    }

    // ==================== Property 12: Delete Action Cleanup ====================

    // Feature: inline-audio-recorder, Property 12: Delete Action Cleanup
    // *For any* delete action from any non-IDLE state:
    // - The status SHALL transition to IDLE
    // - The amplitude history SHALL be cleared
    // - The duration SHALL be reset to 0
    // - The playback position SHALL be reset to 0
    // **Validates: Requirements 1.14, 9.2, 13.1, 13.6**
    test("Property 12: Delete from any non-IDLE state transitions to IDLE and clears state") {
        val nonIdleStates = listOf(
            InlineAudioRecorderStatus.RECORDING,
            InlineAudioRecorderStatus.PAUSED,
            InlineAudioRecorderStatus.COMPLETED,
            InlineAudioRecorderStatus.PLAYING,
            InlineAudioRecorderStatus.ERROR
        )
        
        checkAll(100, Arb.element(nonIdleStates)) { initialStatus ->
            val viewModel = CometChatInlineAudioRecorderViewModel()
            
            // Set up state with data
            viewModel.setState(InlineAudioRecorderState(
                status = initialStatus,
                duration = 5000L,
                currentPosition = 2500L,
                filePath = "/path/to/file.m4a",
                amplitudes = listOf(0.1f, 0.5f, 0.8f, 0.3f)
            ))
            
            // Delete recording
            viewModel.deleteRecording()
            
            // Verify cleanup
            viewModel.status shouldBe InlineAudioRecorderStatus.IDLE
            viewModel.amplitudes shouldBe emptyList()
            viewModel.duration shouldBe 0L
            viewModel.currentPosition shouldBe 0L
            viewModel.filePath shouldBe null
        }
    }

    // Feature: inline-audio-recorder, Property 12: Delete Action Cleanup (continued)
    // Verify that delete clears amplitude history
    // **Validates: Requirements 13.6**
    test("Property 12: Delete clears amplitude history") {
        val viewModel = CometChatInlineAudioRecorderViewModel()
        
        // Start recording and add amplitudes
        viewModel.startRecording()
        viewModel.addAmplitude(0.1f)
        viewModel.addAmplitude(0.5f)
        viewModel.addAmplitude(0.8f)
        viewModel.amplitudes.size shouldBe 3
        
        // Delete recording
        viewModel.deleteRecording()
        
        // Amplitudes should be cleared
        viewModel.amplitudes shouldBe emptyList()
    }

    // ==================== Additional Property Tests ====================

    // Feature: inline-audio-recorder, Property: Amplitude bounds
    // Verify amplitude is always clamped to [0.0, 1.0]
    test("Amplitude is clamped to valid range") {
        checkAll(100, Arb.float(-1f, 2f)) { amplitude ->
            val viewModel = CometChatInlineAudioRecorderViewModel()
            
            viewModel.addAmplitude(amplitude)
            
            val storedAmplitude = viewModel.amplitudes.last()
            storedAmplitude shouldBe amplitude.coerceIn(0f, 1f)
            (storedAmplitude >= 0f && storedAmplitude <= 1f) shouldBe true
        }
    }

    // Feature: inline-audio-recorder, Property: Seek position bounds
    // Verify seek position is clamped to [0, duration]
    test("Seek position is clamped to valid range") {
        checkAll(100, Arb.long(1000L, 60000L), Arb.long(-1000L, 120000L)) { duration, seekPosition ->
            val viewModel = CometChatInlineAudioRecorderViewModel()
            
            // Set up COMPLETED state with duration
            viewModel.setState(InlineAudioRecorderState(
                status = InlineAudioRecorderStatus.COMPLETED,
                duration = duration
            ))
            
            // Seek to position
            viewModel.seekTo(seekPosition)
            
            // Position should be clamped
            val expectedPosition = seekPosition.coerceIn(0L, duration)
            viewModel.currentPosition shouldBe expectedPosition
        }
    }

    // Feature: inline-audio-recorder, Property: Initial state
    // Verify initial state is IDLE with all values at defaults
    test("Initial state is IDLE with default values") {
        val viewModel = CometChatInlineAudioRecorderViewModel()
        
        viewModel.status shouldBe InlineAudioRecorderStatus.IDLE
        viewModel.duration shouldBe 0L
        viewModel.currentPosition shouldBe 0L
        viewModel.amplitudes shouldBe emptyList()
        viewModel.filePath shouldBe null
        viewModel.errorMessage shouldBe null
        viewModel.formattedDuration shouldBe "00:00"
        viewModel.formattedPosition shouldBe "00:00"
    }

    // Feature: inline-audio-recorder, Property: Release clears all state
    test("Release clears all state") {
        val viewModel = CometChatInlineAudioRecorderViewModel()
        
        // Set up some state
        viewModel.startRecording()
        viewModel.updateDuration(5000L)
        viewModel.addAmplitude(0.5f)
        viewModel.addAmplitude(0.8f)
        viewModel.stopRecording()
        viewModel.setFilePath("/path/to/file.m4a")
        viewModel.startPlayback()
        viewModel.updatePosition(2500L)
        
        // Release
        viewModel.release()
        
        // All state should be reset
        viewModel.status shouldBe InlineAudioRecorderStatus.IDLE
        viewModel.duration shouldBe 0L
        viewModel.currentPosition shouldBe 0L
        viewModel.amplitudes shouldBe emptyList()
        viewModel.filePath shouldBe null
        viewModel.errorMessage shouldBe null
    }

    // Feature: inline-audio-recorder, Property: Playback progress calculation
    test("Playback progress is calculated correctly") {
        checkAll(100, Arb.long(1000L, 60000L), Arb.long(0L, 60000L)) { duration, position ->
            val viewModel = CometChatInlineAudioRecorderViewModel()
            
            val clampedPosition = position.coerceAtMost(duration)
            viewModel.setState(InlineAudioRecorderState(
                status = InlineAudioRecorderStatus.PLAYING,
                duration = duration,
                currentPosition = clampedPosition
            ))
            
            val expectedProgress = clampedPosition.toFloat() / duration.toFloat()
            val actualProgress = viewModel.state.value.playbackProgress
            
            // Allow small floating point tolerance
            kotlin.math.abs(actualProgress - expectedProgress) < 0.001f shouldBe true
        }
    }

    // Feature: inline-audio-recorder, Property: Playback progress is 0 when duration is 0
    test("Playback progress is 0 when duration is 0") {
        val viewModel = CometChatInlineAudioRecorderViewModel()
        
        viewModel.setState(InlineAudioRecorderState(
            status = InlineAudioRecorderStatus.COMPLETED,
            duration = 0L,
            currentPosition = 0L
        ))
        
        viewModel.state.value.playbackProgress shouldBe 0f
    }

    // Feature: inline-audio-recorder, Property: Display time varies by status
    test("Display time shows correct value based on status") {
        val viewModel = CometChatInlineAudioRecorderViewModel()
        val duration = 5000L
        val position = 2500L
        
        // RECORDING: shows duration (elapsed time)
        viewModel.setState(InlineAudioRecorderState(
            status = InlineAudioRecorderStatus.RECORDING,
            duration = duration,
            currentPosition = position
        ))
        viewModel.displayTime shouldBe viewModel.formatTime(duration)
        
        // PAUSED: shows duration
        viewModel.setState(InlineAudioRecorderState(
            status = InlineAudioRecorderStatus.PAUSED,
            duration = duration,
            currentPosition = position
        ))
        viewModel.displayTime shouldBe viewModel.formatTime(duration)
        
        // COMPLETED: shows duration
        viewModel.setState(InlineAudioRecorderState(
            status = InlineAudioRecorderStatus.COMPLETED,
            duration = duration,
            currentPosition = position
        ))
        viewModel.displayTime shouldBe viewModel.formatTime(duration)
        
        // PLAYING: shows current position
        viewModel.setState(InlineAudioRecorderState(
            status = InlineAudioRecorderStatus.PLAYING,
            duration = duration,
            currentPosition = position
        ))
        viewModel.displayTime shouldBe viewModel.formatTime(position)
        
        // IDLE: shows 00:00
        viewModel.setState(InlineAudioRecorderState(
            status = InlineAudioRecorderStatus.IDLE,
            duration = duration,
            currentPosition = position
        ))
        viewModel.displayTime shouldBe "00:00"
    }
})
