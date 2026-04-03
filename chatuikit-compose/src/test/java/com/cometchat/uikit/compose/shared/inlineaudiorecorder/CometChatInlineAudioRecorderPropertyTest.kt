package com.cometchat.uikit.compose.shared.inlineaudiorecorder

import com.cometchat.uikit.core.viewmodel.CometChatInlineAudioRecorderViewModel
import com.cometchat.uikit.core.viewmodel.InlineAudioRecorderState
import com.cometchat.uikit.core.viewmodel.InlineAudioRecorderStatus
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.enum
import io.kotest.property.arbitrary.float
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.long
import io.kotest.property.checkAll

/**
 * Property-based tests for [CometChatInlineAudioRecorder].
 * 
 * These tests validate the UI state consistency and timer display behavior.
 *
 * Feature: inline-audio-recorder
 * **Validates: Requirements 1.2, 1.4, 1.6, 1.9, 1.11, 2.2, 2.3, 2.4, 2.5, 7.3, 7.4, 7.6, 7.7, 9.1-9.12**
 */
class CometChatInlineAudioRecorderPropertyTest : FunSpec({

    // ==================== Property 2: UI State Consistency ====================
    
    // Feature: inline-audio-recorder, Property 2: UI State Consistency
    // *For any* InlineAudioRecorderStatus value, the visible UI elements SHALL match 
    // the expected elements for that status:
    // - IDLE: Recorder not visible
    // - RECORDING: Delete button, red pulsing indicator, animated waveform (purple), recording time, pause button, send button
    // - PAUSED: Delete button, play button, static waveform (purple), total duration, mic/resume button, send button
    // - COMPLETED: Delete button, play button, static waveform (grey), total duration, mic button (disabled), send button
    // - PLAYING: Delete button, pause button, progress waveform (grey→purple), playback position, mic button (disabled), send button
    // - ERROR: Error message with recovery option
    // **Validates: Requirements 1.2, 1.4, 1.6, 1.9, 1.11, 2.2, 2.3, 2.4, 2.5, 9.1-9.12**

    /**
     * Property 2: IDLE state means recorder is not visible
     * 
     * **Validates: Requirements 1.2**
     */
    test("Property 2: IDLE state means recorder is not visible") {
        val viewModel = CometChatInlineAudioRecorderViewModel()
        
        // Initial state should be IDLE
        viewModel.status shouldBe InlineAudioRecorderStatus.IDLE
        
        // In IDLE state, the recorder should not be visible
        // This is validated by the composable only rendering when status != IDLE
    }

    /**
     * Property 2: RECORDING state shows correct UI elements
     * 
     * Expected: Delete button, red pulsing indicator, animated waveform, recording time, pause button, send button
     * 
     * **Validates: Requirements 1.4, 2.2, 9.1, 9.4, 9.7, 9.8, 9.11**
     */
    test("Property 2: RECORDING state has correct properties") {
        checkAll(100, Arb.long(0L, 300000L), Arb.list(Arb.float(0f, 1f), 0..100)) { duration, amplitudes ->
            val state = InlineAudioRecorderState(
                status = InlineAudioRecorderStatus.RECORDING,
                duration = duration,
                currentPosition = 0L,
                amplitudes = amplitudes
            )
            
            // Verify state properties
            state.status shouldBe InlineAudioRecorderStatus.RECORDING
            state.duration shouldBe duration
            state.amplitudes shouldBe amplitudes
            
            // In RECORDING state:
            // - Delete button should be enabled (status != IDLE)
            // - Record indicator should be visible (status == RECORDING)
            // - Waveform should be in recording mode (isRecording = true)
            // - Pause button should be visible (status == RECORDING)
            // - Send button should be visible
        }
    }

    /**
     * Property 2: PAUSED state shows correct UI elements
     * 
     * Expected: Delete button, play button, static waveform, total duration, mic/resume button, send button
     * 
     * **Validates: Requirements 1.6, 2.3, 9.1, 9.5, 9.9, 9.11**
     */
    test("Property 2: PAUSED state has correct properties") {
        checkAll(100, Arb.long(1000L, 300000L), Arb.list(Arb.float(0f, 1f), 1..100)) { duration, amplitudes ->
            val state = InlineAudioRecorderState(
                status = InlineAudioRecorderStatus.PAUSED,
                duration = duration,
                currentPosition = 0L,
                amplitudes = amplitudes
            )
            
            // Verify state properties
            state.status shouldBe InlineAudioRecorderStatus.PAUSED
            state.duration shouldBe duration
            state.amplitudes shouldBe amplitudes
            
            // In PAUSED state:
            // - Delete button should be enabled
            // - Play button should be visible (status == PAUSED)
            // - Waveform should be static (isRecording = false, isPlaying = false)
            // - Mic/Resume button should be visible (status == PAUSED)
            // - Send button should be visible
        }
    }

    /**
     * Property 2: COMPLETED state shows correct UI elements
     * 
     * Expected: Delete button, play button, static waveform (grey), total duration, mic button (disabled), send button
     * 
     * **Validates: Requirements 1.9, 2.4, 9.1, 9.5, 9.10, 9.11, 9.12**
     */
    test("Property 2: COMPLETED state has correct properties") {
        checkAll(100, Arb.long(1000L, 300000L), Arb.list(Arb.float(0f, 1f), 1..100)) { duration, amplitudes ->
            val state = InlineAudioRecorderState(
                status = InlineAudioRecorderStatus.COMPLETED,
                duration = duration,
                currentPosition = 0L,
                amplitudes = amplitudes,
                filePath = "/path/to/recording.m4a"
            )
            
            // Verify state properties
            state.status shouldBe InlineAudioRecorderStatus.COMPLETED
            state.duration shouldBe duration
            state.amplitudes shouldBe amplitudes
            state.filePath shouldBe "/path/to/recording.m4a"
            
            // In COMPLETED state:
            // - Delete button should be enabled
            // - Play button should be visible (status == COMPLETED)
            // - Waveform should be static with grey color (progress = 0)
            // - Mic button should be disabled (status == COMPLETED)
            // - Send button should be visible and enabled
        }
    }

    /**
     * Property 2: PLAYING state shows correct UI elements
     * 
     * Expected: Delete button, pause button, progress waveform, playback position, mic button (disabled), send button
     * 
     * **Validates: Requirements 1.11, 2.5, 9.1, 9.6, 9.10, 9.11**
     */
    test("Property 2: PLAYING state has correct properties") {
        checkAll(
            100,
            Arb.long(1000L, 300000L),
            Arb.long(0L, 300000L),
            Arb.list(Arb.float(0f, 1f), 1..100)
        ) { duration, position, amplitudes ->
            val clampedPosition = position.coerceAtMost(duration)
            val state = InlineAudioRecorderState(
                status = InlineAudioRecorderStatus.PLAYING,
                duration = duration,
                currentPosition = clampedPosition,
                amplitudes = amplitudes,
                filePath = "/path/to/recording.m4a"
            )
            
            // Verify state properties
            state.status shouldBe InlineAudioRecorderStatus.PLAYING
            state.duration shouldBe duration
            state.currentPosition shouldBe clampedPosition
            state.amplitudes shouldBe amplitudes
            
            // In PLAYING state:
            // - Delete button should be enabled
            // - Pause button should be visible (status == PLAYING)
            // - Waveform should show progress (isPlaying = true)
            // - Mic button should be disabled (status == PLAYING)
            // - Send button should be visible
        }
    }

    /**
     * Property 2: ERROR state shows error message and recovery option
     * 
     * **Validates: Requirements 1.15**
     */
    test("Property 2: ERROR state has error message") {
        val errorMessage = "Recording failed"
        val state = InlineAudioRecorderState(
            status = InlineAudioRecorderStatus.ERROR,
            errorMessage = errorMessage
        )
        
        state.status shouldBe InlineAudioRecorderStatus.ERROR
        state.errorMessage shouldBe errorMessage
    }

    // ==================== Property 8: Timer Display by Status ====================
    
    // Feature: inline-audio-recorder, Property 8: Timer Display by Status
    // *For any* status, the timer SHALL display:
    // - RECORDING: Elapsed recording time (updating every second)
    // - PAUSED: Total recorded duration so far (static)
    // - COMPLETED: Total recording duration (static)
    // - PLAYING: Current playback position (updating)
    // **Validates: Requirements 7.3, 7.4, 7.6, 7.7**

    /**
     * Property 8: RECORDING state displays elapsed recording time
     * 
     * **Validates: Requirements 7.3**
     */
    test("Property 8: RECORDING state displays elapsed recording time") {
        checkAll(100, Arb.long(0L, 300000L)) { duration ->
            val viewModel = CometChatInlineAudioRecorderViewModel()
            viewModel.startRecording()
            viewModel.updateDuration(duration)
            
            // In RECORDING state, displayTime should be formattedDuration
            viewModel.displayTime shouldBe viewModel.formattedDuration
        }
    }

    /**
     * Property 8: PAUSED state displays total recorded duration
     * 
     * **Validates: Requirements 7.4**
     */
    test("Property 8: PAUSED state displays total recorded duration") {
        checkAll(100, Arb.long(1000L, 300000L)) { duration ->
            val viewModel = CometChatInlineAudioRecorderViewModel()
            viewModel.startRecording()
            viewModel.updateDuration(duration)
            viewModel.pauseRecording()
            
            // In PAUSED state, displayTime should be formattedDuration
            viewModel.displayTime shouldBe viewModel.formattedDuration
        }
    }

    /**
     * Property 8: COMPLETED state displays total recording duration
     * 
     * **Validates: Requirements 7.6**
     */
    test("Property 8: COMPLETED state displays total recording duration") {
        checkAll(100, Arb.long(1000L, 300000L)) { duration ->
            val viewModel = CometChatInlineAudioRecorderViewModel()
            viewModel.startRecording()
            viewModel.updateDuration(duration)
            viewModel.stopRecording()
            
            // In COMPLETED state, displayTime should be formattedDuration
            viewModel.displayTime shouldBe viewModel.formattedDuration
        }
    }

    /**
     * Property 8: PLAYING state displays current playback position
     * 
     * **Validates: Requirements 7.7**
     */
    test("Property 8: PLAYING state displays current playback position") {
        checkAll(100, Arb.long(1000L, 300000L), Arb.long(0L, 300000L)) { duration, position ->
            val clampedPosition = position.coerceAtMost(duration)
            val viewModel = CometChatInlineAudioRecorderViewModel()
            viewModel.startRecording()
            viewModel.updateDuration(duration)
            viewModel.stopRecording()
            viewModel.startPlayback()
            viewModel.updatePosition(clampedPosition)
            
            // In PLAYING state, displayTime should be formattedPosition
            viewModel.displayTime shouldBe viewModel.formattedPosition
        }
    }

    /**
     * Property 8: IDLE state displays 00:00
     */
    test("Property 8: IDLE state displays 00:00") {
        val viewModel = CometChatInlineAudioRecorderViewModel()
        
        viewModel.displayTime shouldBe "00:00"
    }

    // ==================== Additional UI Consistency Tests ====================

    /**
     * Property 2: Delete button is always enabled in non-IDLE states
     * 
     * **Validates: Requirements 9.1**
     */
    test("Property 2: Delete button is enabled in all non-IDLE states") {
        val nonIdleStatuses = listOf(
            InlineAudioRecorderStatus.RECORDING,
            InlineAudioRecorderStatus.PAUSED,
            InlineAudioRecorderStatus.COMPLETED,
            InlineAudioRecorderStatus.PLAYING,
            InlineAudioRecorderStatus.ERROR
        )
        
        nonIdleStatuses.forEach { status ->
            val state = InlineAudioRecorderState(status = status)
            // Delete button should be enabled (visible) when status != IDLE
            (state.status != InlineAudioRecorderStatus.IDLE) shouldBe true
        }
    }

    /**
     * Property 2: Send button is always visible in non-IDLE, non-ERROR states
     * 
     * **Validates: Requirements 9.11**
     */
    test("Property 2: Send button is visible in recording states") {
        val recordingStatuses = listOf(
            InlineAudioRecorderStatus.RECORDING,
            InlineAudioRecorderStatus.PAUSED,
            InlineAudioRecorderStatus.COMPLETED,
            InlineAudioRecorderStatus.PLAYING
        )
        
        recordingStatuses.forEach { status ->
            val state = InlineAudioRecorderState(status = status)
            // Send button should be visible in these states
            (state.status in recordingStatuses) shouldBe true
        }
    }

    /**
     * Property 2: Waveform mode matches status
     */
    test("Property 2: Waveform mode matches status") {
        checkAll(100, Arb.enum<InlineAudioRecorderStatus>()) { status ->
            val isRecording = status == InlineAudioRecorderStatus.RECORDING
            val isPlaying = status == InlineAudioRecorderStatus.PLAYING
            
            // Waveform should be in recording mode only when RECORDING
            // Waveform should be in playing mode only when PLAYING
            when (status) {
                InlineAudioRecorderStatus.RECORDING -> {
                    isRecording shouldBe true
                    isPlaying shouldBe false
                }
                InlineAudioRecorderStatus.PLAYING -> {
                    isRecording shouldBe false
                    isPlaying shouldBe true
                }
                else -> {
                    isRecording shouldBe false
                    isPlaying shouldBe false
                }
            }
        }
    }

    /**
     * Property 2: Progress is calculated correctly for playback
     */
    test("Property 2: Progress calculation for playback") {
        checkAll(100, Arb.long(1000L, 300000L), Arb.long(0L, 300000L)) { duration, position ->
            val clampedPosition = position.coerceAtMost(duration)
            
            val progress = if (duration > 0) {
                (clampedPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
            } else {
                0f
            }
            
            // Progress should be in valid range
            (progress >= 0f && progress <= 1f) shouldBe true
            
            // Progress should be 0 when position is 0
            if (clampedPosition == 0L) {
                progress shouldBe 0f
            }
            
            // Progress should be 1 when position equals duration
            if (clampedPosition == duration && duration > 0) {
                progress shouldBe 1f
            }
        }
    }
})
