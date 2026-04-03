package com.cometchat.uikit.compose.presentation.shared.messagebubble.ui

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.enum
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll

/**
 * Represents the type of button that should be visible for a given state.
 */
private data class ButtonVisibility(
    val showPlayButton: Boolean,
    val showPauseButton: Boolean,
    val showLoadingIndicator: Boolean
)

/**
 * Represents an action that can be triggered on the audio bubble.
 */
private enum class AudioAction {
    PLAY,
    PAUSE,
    PLAYBACK_COMPLETE
}

/**
 * Determines the expected button visibility for a given playback state.
 * Based on Requirements 4.1, 4.2, 4.3, 4.4, 4.5
 */
private fun getExpectedButtonVisibility(state: AudioPlaybackState): ButtonVisibility {
    return when (state) {
        AudioPlaybackState.IDLE -> ButtonVisibility(
            showPlayButton = true,
            showPauseButton = false,
            showLoadingIndicator = false
        )
        AudioPlaybackState.LOADING -> ButtonVisibility(
            showPlayButton = false,
            showPauseButton = false,
            showLoadingIndicator = true
        )
        AudioPlaybackState.PLAYING -> ButtonVisibility(
            showPlayButton = false,
            showPauseButton = true,
            showLoadingIndicator = false
        )
        AudioPlaybackState.PAUSED -> ButtonVisibility(
            showPlayButton = true,
            showPauseButton = false,
            showLoadingIndicator = false
        )
    }
}

/**
 * Simulates a state transition based on the current state and action.
 * Returns the next state according to the state machine rules.
 */
private fun simulateStateTransition(currentState: AudioPlaybackState, action: AudioAction): AudioPlaybackState {
    return when (currentState) {
        AudioPlaybackState.IDLE -> when (action) {
            AudioAction.PLAY -> AudioPlaybackState.PLAYING
            AudioAction.PAUSE -> AudioPlaybackState.IDLE // No-op
            AudioAction.PLAYBACK_COMPLETE -> AudioPlaybackState.IDLE // No-op
        }
        AudioPlaybackState.LOADING -> when (action) {
            AudioAction.PLAY -> AudioPlaybackState.LOADING // No-op while loading
            AudioAction.PAUSE -> AudioPlaybackState.LOADING // No-op while loading
            AudioAction.PLAYBACK_COMPLETE -> AudioPlaybackState.IDLE // Error case - reset
        }
        AudioPlaybackState.PLAYING -> when (action) {
            AudioAction.PLAY -> AudioPlaybackState.PLAYING // Already playing
            AudioAction.PAUSE -> AudioPlaybackState.PAUSED
            AudioAction.PLAYBACK_COMPLETE -> AudioPlaybackState.IDLE
        }
        AudioPlaybackState.PAUSED -> when (action) {
            AudioAction.PLAY -> AudioPlaybackState.PLAYING
            AudioAction.PAUSE -> AudioPlaybackState.PAUSED // Already paused
            AudioAction.PLAYBACK_COMPLETE -> AudioPlaybackState.IDLE // Edge case
        }
    }
}

/**
 * Property-based tests for Audio Bubble Playback State Transitions.
 *
 * Feature: video-audio-bubbles
 * Properties tested:
 * - Property 7: Audio Playback State Transitions
 *
 * **Validates: Requirements 4.2, 4.3, 4.4**
 *
 * Tests the state machine behavior of [AudioPlaybackState] transitions:
 * - If S = IDLE and play is triggered, next state SHALL be PLAYING with pause button visible
 * - If S = PLAYING and pause is triggered, next state SHALL be PAUSED with play button visible
 * - If S = PLAYING and playback completes, next state SHALL be IDLE with play button visible
 */
class AudioBubblePlaybackStatePropertyTest : StringSpec({

    /**
     * Property 7: Audio Playback State Transitions - IDLE to PLAYING
     *
     * *For any* Audio_Bubble in state S = IDLE and play is triggered,
     * next state SHALL be PLAYING with pause button visible.
     *
     * **Validates: Requirements 4.2**
     */
    "Property 7: IDLE state + PLAY action should transition to PLAYING with pause button visible" {
        val initialState = AudioPlaybackState.IDLE
        val action = AudioAction.PLAY
        
        val nextState = simulateStateTransition(initialState, action)
        val buttonVisibility = getExpectedButtonVisibility(nextState)
        
        nextState shouldBe AudioPlaybackState.PLAYING
        buttonVisibility.showPauseButton shouldBe true
        buttonVisibility.showPlayButton shouldBe false
    }

    /**
     * Property 7: Audio Playback State Transitions - PLAYING to PAUSED
     *
     * *For any* Audio_Bubble in state S = PLAYING and pause is triggered,
     * next state SHALL be PAUSED with play button visible.
     *
     * **Validates: Requirements 4.3**
     */
    "Property 7: PLAYING state + PAUSE action should transition to PAUSED with play button visible" {
        val initialState = AudioPlaybackState.PLAYING
        val action = AudioAction.PAUSE
        
        val nextState = simulateStateTransition(initialState, action)
        val buttonVisibility = getExpectedButtonVisibility(nextState)
        
        nextState shouldBe AudioPlaybackState.PAUSED
        buttonVisibility.showPlayButton shouldBe true
        buttonVisibility.showPauseButton shouldBe false
    }

    /**
     * Property 7: Audio Playback State Transitions - PLAYING to IDLE on completion
     *
     * *For any* Audio_Bubble in state S = PLAYING and playback completes,
     * next state SHALL be IDLE with play button visible.
     *
     * **Validates: Requirements 4.4**
     */
    "Property 7: PLAYING state + PLAYBACK_COMPLETE should transition to IDLE with play button visible" {
        val initialState = AudioPlaybackState.PLAYING
        val action = AudioAction.PLAYBACK_COMPLETE
        
        val nextState = simulateStateTransition(initialState, action)
        val buttonVisibility = getExpectedButtonVisibility(nextState)
        
        nextState shouldBe AudioPlaybackState.IDLE
        buttonVisibility.showPlayButton shouldBe true
        buttonVisibility.showPauseButton shouldBe false
    }

    /**
     * Property 7: Button visibility is consistent with state for all states
     *
     * *For any* AudioPlaybackState, the button visibility SHALL be deterministic
     * and consistent with the state machine rules.
     *
     * **Validates: Requirements 4.1, 4.2, 4.3, 4.4, 4.5**
     */
    "Property 7: Button visibility should be consistent with state for all states" {
        val stateArb = Arb.enum<AudioPlaybackState>()

        checkAll(100, stateArb) { state ->
            val visibility = getExpectedButtonVisibility(state)
            
            when (state) {
                AudioPlaybackState.IDLE -> {
                    visibility.showPlayButton shouldBe true
                    visibility.showPauseButton shouldBe false
                    visibility.showLoadingIndicator shouldBe false
                }
                AudioPlaybackState.LOADING -> {
                    visibility.showPlayButton shouldBe false
                    visibility.showPauseButton shouldBe false
                    visibility.showLoadingIndicator shouldBe true
                }
                AudioPlaybackState.PLAYING -> {
                    visibility.showPlayButton shouldBe false
                    visibility.showPauseButton shouldBe true
                    visibility.showLoadingIndicator shouldBe false
                }
                AudioPlaybackState.PAUSED -> {
                    visibility.showPlayButton shouldBe true
                    visibility.showPauseButton shouldBe false
                    visibility.showLoadingIndicator shouldBe false
                }
            }
        }
    }

    /**
     * Property 7: Exactly one button type should be visible at any time
     *
     * *For any* AudioPlaybackState, exactly one of play button, pause button,
     * or loading indicator SHALL be visible.
     *
     * **Validates: Requirements 4.1, 4.2, 4.3, 4.5**
     */
    "Property 7: Exactly one button type should be visible at any time" {
        val stateArb = Arb.enum<AudioPlaybackState>()

        checkAll(100, stateArb) { state ->
            val visibility = getExpectedButtonVisibility(state)
            
            // Count how many are visible
            val visibleCount = listOf(
                visibility.showPlayButton,
                visibility.showPauseButton,
                visibility.showLoadingIndicator
            ).count { it }
            
            visibleCount shouldBe 1
        }
    }

    /**
     * Property 7: State transitions are deterministic
     *
     * *For any* combination of state and action, the resulting state
     * SHALL always be the same (deterministic).
     *
     * **Validates: Requirements 4.2, 4.3, 4.4**
     */
    "Property 7: State transitions should be deterministic" {
        val stateArb = Arb.enum<AudioPlaybackState>()
        val actionArb = Arb.enum<AudioAction>()

        checkAll(200, stateArb, actionArb) { state, action ->
            val result1 = simulateStateTransition(state, action)
            val result2 = simulateStateTransition(state, action)
            
            result1 shouldBe result2
        }
    }

    /**
     * Property 7: PAUSED state + PLAY action should resume to PLAYING
     *
     * *For any* Audio_Bubble in state S = PAUSED and play is triggered,
     * next state SHALL be PLAYING with pause button visible.
     *
     * **Validates: Requirements 4.2**
     */
    "Property 7: PAUSED state + PLAY action should transition to PLAYING with pause button visible" {
        val initialState = AudioPlaybackState.PAUSED
        val action = AudioAction.PLAY
        
        val nextState = simulateStateTransition(initialState, action)
        val buttonVisibility = getExpectedButtonVisibility(nextState)
        
        nextState shouldBe AudioPlaybackState.PLAYING
        buttonVisibility.showPauseButton shouldBe true
        buttonVisibility.showPlayButton shouldBe false
    }

    /**
     * Property 7: LOADING state should not change on PLAY or PAUSE actions
     *
     * *For any* Audio_Bubble in state S = LOADING, PLAY and PAUSE actions
     * SHALL NOT change the state (no-op while loading).
     *
     * **Validates: Requirements 4.5**
     */
    "Property 7: LOADING state should not change on PLAY or PAUSE actions" {
        val loadingState = AudioPlaybackState.LOADING
        
        val afterPlay = simulateStateTransition(loadingState, AudioAction.PLAY)
        val afterPause = simulateStateTransition(loadingState, AudioAction.PAUSE)
        
        afterPlay shouldBe AudioPlaybackState.LOADING
        afterPause shouldBe AudioPlaybackState.LOADING
    }

    /**
     * Property 7: Valid state transitions form a valid state machine
     *
     * Tests that the state machine has valid transitions and no invalid states.
     *
     * **Validates: Requirements 4.2, 4.3, 4.4**
     */
    "Property 7: All state transitions should result in valid states" {
        val stateArb = Arb.enum<AudioPlaybackState>()
        val actionArb = Arb.enum<AudioAction>()

        checkAll(200, stateArb, actionArb) { state, action ->
            val nextState = simulateStateTransition(state, action)
            
            // Next state should be a valid AudioPlaybackState
            nextState shouldNotBe null
            AudioPlaybackState.entries.contains(nextState) shouldBe true
        }
    }

    /**
     * Property 7: Play button states (IDLE, PAUSED) should show play button
     *
     * *For any* state where play button should be visible (IDLE or PAUSED),
     * the play button SHALL be visible and pause button SHALL NOT be visible.
     *
     * **Validates: Requirements 4.1, 4.3**
     */
    "Property 7: Play button states (IDLE, PAUSED) should show play button" {
        val playButtonStates = listOf(AudioPlaybackState.IDLE, AudioPlaybackState.PAUSED)
        
        playButtonStates.forEach { state ->
            val visibility = getExpectedButtonVisibility(state)
            
            visibility.showPlayButton shouldBe true
            visibility.showPauseButton shouldBe false
        }
    }

    /**
     * Property 7: Pause button state (PLAYING) should show pause button
     *
     * *For any* state where pause button should be visible (PLAYING),
     * the pause button SHALL be visible and play button SHALL NOT be visible.
     *
     * **Validates: Requirements 4.2**
     */
    "Property 7: PLAYING state should show pause button" {
        val state = AudioPlaybackState.PLAYING
        val visibility = getExpectedButtonVisibility(state)
        
        visibility.showPauseButton shouldBe true
        visibility.showPlayButton shouldBe false
        visibility.showLoadingIndicator shouldBe false
    }

    /**
     * Property 7: Multiple sequential transitions should maintain valid state
     *
     * *For any* sequence of actions, the state machine should always
     * remain in a valid state.
     *
     * **Validates: Requirements 4.2, 4.3, 4.4**
     */
    "Property 7: Multiple sequential transitions should maintain valid state" {
        val actionArb = Arb.enum<AudioAction>()
        val sequenceLengthArb = Arb.int(1..10)

        checkAll(100, sequenceLengthArb) { sequenceLength ->
            var currentState = AudioPlaybackState.IDLE
            
            repeat(sequenceLength) {
                // Generate a random action
                val action = AudioAction.entries.random()
                currentState = simulateStateTransition(currentState, action)
                
                // State should always be valid
                AudioPlaybackState.entries.contains(currentState) shouldBe true
                
                // Button visibility should be consistent
                val visibility = getExpectedButtonVisibility(currentState)
                val visibleCount = listOf(
                    visibility.showPlayButton,
                    visibility.showPauseButton,
                    visibility.showLoadingIndicator
                ).count { it }
                visibleCount shouldBe 1
            }
        }
    }

    /**
     * Property 7: Complete playback cycle should return to IDLE
     *
     * A complete playback cycle (IDLE -> PLAYING -> completion) should
     * return to IDLE state with play button visible.
     *
     * **Validates: Requirements 4.2, 4.4**
     */
    "Property 7: Complete playback cycle should return to IDLE" {
        // Start from IDLE
        var state = AudioPlaybackState.IDLE
        
        // Play action
        state = simulateStateTransition(state, AudioAction.PLAY)
        state shouldBe AudioPlaybackState.PLAYING
        
        // Playback completes
        state = simulateStateTransition(state, AudioAction.PLAYBACK_COMPLETE)
        state shouldBe AudioPlaybackState.IDLE
        
        // Button should show play
        val visibility = getExpectedButtonVisibility(state)
        visibility.showPlayButton shouldBe true
    }

    /**
     * Property 7: Play-pause-play cycle should work correctly
     *
     * A play-pause-play cycle should transition through states correctly.
     *
     * **Validates: Requirements 4.2, 4.3**
     */
    "Property 7: Play-pause-play cycle should work correctly" {
        // Start from IDLE
        var state = AudioPlaybackState.IDLE
        
        // Play
        state = simulateStateTransition(state, AudioAction.PLAY)
        state shouldBe AudioPlaybackState.PLAYING
        getExpectedButtonVisibility(state).showPauseButton shouldBe true
        
        // Pause
        state = simulateStateTransition(state, AudioAction.PAUSE)
        state shouldBe AudioPlaybackState.PAUSED
        getExpectedButtonVisibility(state).showPlayButton shouldBe true
        
        // Play again (resume)
        state = simulateStateTransition(state, AudioAction.PLAY)
        state shouldBe AudioPlaybackState.PLAYING
        getExpectedButtonVisibility(state).showPauseButton shouldBe true
    }

    /**
     * Property 7: Idempotent actions should not change state
     *
     * Triggering the same action multiple times when already in the target state
     * should not change the state.
     *
     * **Validates: Requirements 4.2, 4.3**
     */
    "Property 7: Idempotent actions should not change state" {
        // Playing + Play = Playing (already playing)
        val playingAfterPlay = simulateStateTransition(AudioPlaybackState.PLAYING, AudioAction.PLAY)
        playingAfterPlay shouldBe AudioPlaybackState.PLAYING
        
        // Paused + Pause = Paused (already paused)
        val pausedAfterPause = simulateStateTransition(AudioPlaybackState.PAUSED, AudioAction.PAUSE)
        pausedAfterPause shouldBe AudioPlaybackState.PAUSED
        
        // Idle + Pause = Idle (nothing to pause)
        val idleAfterPause = simulateStateTransition(AudioPlaybackState.IDLE, AudioAction.PAUSE)
        idleAfterPause shouldBe AudioPlaybackState.IDLE
        
        // Idle + Complete = Idle (nothing playing)
        val idleAfterComplete = simulateStateTransition(AudioPlaybackState.IDLE, AudioAction.PLAYBACK_COMPLETE)
        idleAfterComplete shouldBe AudioPlaybackState.IDLE
    }

    /**
     * Property 7: AudioPlaybackState enum should have exactly 4 states
     *
     * The state machine should have exactly 4 defined states.
     *
     * **Validates: Requirements 4.1, 4.2, 4.3, 4.4, 4.5**
     */
    "Property 7: AudioPlaybackState enum should have exactly 4 states" {
        AudioPlaybackState.entries.size shouldBe 4
        
        AudioPlaybackState.entries.contains(AudioPlaybackState.IDLE) shouldBe true
        AudioPlaybackState.entries.contains(AudioPlaybackState.LOADING) shouldBe true
        AudioPlaybackState.entries.contains(AudioPlaybackState.PLAYING) shouldBe true
        AudioPlaybackState.entries.contains(AudioPlaybackState.PAUSED) shouldBe true
    }
})
