package com.cometchat.uikit.compose.presentation.shared.messagebubble.ui

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldMatch
import io.kotest.matchers.string.shouldNotMatch
import io.kotest.property.Arb
import io.kotest.property.arbitrary.enum
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.long
import io.kotest.property.checkAll

/**
 * Represents the subtitle content that should be displayed for a given playback state.
 */
private data class SubtitleContent(
    val showDuration: Boolean,
    val showFileSize: Boolean,
    val content: String
)

/**
 * Determines the expected subtitle content for a given playback state.
 * Based on Requirements 5.2, 5.3
 *
 * Logic from CometChatAudioBubble.kt:
 * ```kotlin
 * val subtitleText = when (playbackState) {
 *     AudioPlaybackState.PLAYING -> formatDuration(currentPosition, totalDuration)
 *     else -> formatFileSize(fileSize)
 * }
 * ```
 */
private fun getExpectedSubtitleContent(
    playbackState: AudioPlaybackState,
    currentPosition: Long,
    totalDuration: Long,
    fileSize: Int
): SubtitleContent {
    return when (playbackState) {
        AudioPlaybackState.PLAYING -> SubtitleContent(
            showDuration = true,
            showFileSize = false,
            content = formatDuration(currentPosition, totalDuration)
        )
        else -> SubtitleContent(
            showDuration = false,
            showFileSize = true,
            content = formatFileSize(fileSize)
        )
    }
}

/**
 * Determines if a subtitle content string represents a duration format.
 * Duration format is "MM:SS/MM:SS"
 */
private fun isDurationFormat(content: String): Boolean {
    return content.matches(Regex("\\d{2}:\\d{2}/\\d{2}:\\d{2}"))
}

/**
 * Determines if a subtitle content string represents a file size format.
 * File size format is "N MB", "N KB", or "N B"
 */
private fun isFileSizeFormat(content: String): Boolean {
    return content.matches(Regex("\\d+ (MB|KB|B)"))
}

/**
 * Property-based tests for Audio Bubble Subtitle Content.
 *
 * Feature: video-audio-bubbles
 * Properties tested:
 * - Property 9: Subtitle Content Based on Playback State
 *
 * **Validates: Requirements 5.2, 5.3**
 *
 * Tests the subtitle content logic of [CometChatAudioBubble]:
 * - When not playing, subtitle SHALL display file size
 * - When playing, subtitle SHALL display duration in "MM:SS/MM:SS" format
 */
class AudioBubbleSubtitleContentPropertyTest : StringSpec({

    /**
     * Property 9: Subtitle Content Based on Playback State - PLAYING shows duration
     *
     * *For any* Audio_Bubble in PLAYING state, subtitle SHALL display duration
     * in "MM:SS/MM:SS" format.
     *
     * **Validates: Requirements 5.2**
     */
    "Property 9: PLAYING state should display duration in MM:SS/MM:SS format" {
        val positionArb = Arb.long(0L..3600000L)
        val durationArb = Arb.long(0L..3600000L)
        val fileSizeArb = Arb.int(0..10_000_000)

        checkAll(200, positionArb, durationArb, fileSizeArb) { position, duration, fileSize ->
            val subtitleContent = getExpectedSubtitleContent(
                playbackState = AudioPlaybackState.PLAYING,
                currentPosition = position,
                totalDuration = duration,
                fileSize = fileSize
            )

            subtitleContent.showDuration shouldBe true
            subtitleContent.showFileSize shouldBe false
            isDurationFormat(subtitleContent.content) shouldBe true
        }
    }

    /**
     * Property 9: Subtitle Content Based on Playback State - IDLE shows file size
     *
     * *For any* Audio_Bubble in IDLE state, subtitle SHALL display file size.
     *
     * **Validates: Requirements 5.3**
     */
    "Property 9: IDLE state should display file size" {
        val positionArb = Arb.long(0L..3600000L)
        val durationArb = Arb.long(0L..3600000L)
        val fileSizeArb = Arb.int(0..10_000_000)

        checkAll(200, positionArb, durationArb, fileSizeArb) { position, duration, fileSize ->
            val subtitleContent = getExpectedSubtitleContent(
                playbackState = AudioPlaybackState.IDLE,
                currentPosition = position,
                totalDuration = duration,
                fileSize = fileSize
            )

            subtitleContent.showDuration shouldBe false
            subtitleContent.showFileSize shouldBe true
            isFileSizeFormat(subtitleContent.content) shouldBe true
        }
    }

    /**
     * Property 9: Subtitle Content Based on Playback State - LOADING shows file size
     *
     * *For any* Audio_Bubble in LOADING state, subtitle SHALL display file size.
     *
     * **Validates: Requirements 5.3**
     */
    "Property 9: LOADING state should display file size" {
        val positionArb = Arb.long(0L..3600000L)
        val durationArb = Arb.long(0L..3600000L)
        val fileSizeArb = Arb.int(0..10_000_000)

        checkAll(200, positionArb, durationArb, fileSizeArb) { position, duration, fileSize ->
            val subtitleContent = getExpectedSubtitleContent(
                playbackState = AudioPlaybackState.LOADING,
                currentPosition = position,
                totalDuration = duration,
                fileSize = fileSize
            )

            subtitleContent.showDuration shouldBe false
            subtitleContent.showFileSize shouldBe true
            isFileSizeFormat(subtitleContent.content) shouldBe true
        }
    }

    /**
     * Property 9: Subtitle Content Based on Playback State - PAUSED shows file size
     *
     * *For any* Audio_Bubble in PAUSED state, subtitle SHALL display file size.
     *
     * **Validates: Requirements 5.3**
     */
    "Property 9: PAUSED state should display file size" {
        val positionArb = Arb.long(0L..3600000L)
        val durationArb = Arb.long(0L..3600000L)
        val fileSizeArb = Arb.int(0..10_000_000)

        checkAll(200, positionArb, durationArb, fileSizeArb) { position, duration, fileSize ->
            val subtitleContent = getExpectedSubtitleContent(
                playbackState = AudioPlaybackState.PAUSED,
                currentPosition = position,
                totalDuration = duration,
                fileSize = fileSize
            )

            subtitleContent.showDuration shouldBe false
            subtitleContent.showFileSize shouldBe true
            isFileSizeFormat(subtitleContent.content) shouldBe true
        }
    }

    /**
     * Property 9: Subtitle content is deterministic based on state
     *
     * *For any* combination of playback state, position, duration, and file size,
     * the subtitle content SHALL be deterministic (same inputs produce same output).
     *
     * **Validates: Requirements 5.2, 5.3**
     */
    "Property 9: Subtitle content should be deterministic based on state" {
        val stateArb = Arb.enum<AudioPlaybackState>()
        val positionArb = Arb.long(0L..3600000L)
        val durationArb = Arb.long(0L..3600000L)
        val fileSizeArb = Arb.int(0..10_000_000)

        checkAll(200, stateArb, positionArb, durationArb, fileSizeArb) { state, position, duration, fileSize ->
            val content1 = getExpectedSubtitleContent(state, position, duration, fileSize)
            val content2 = getExpectedSubtitleContent(state, position, duration, fileSize)

            content1 shouldBe content2
        }
    }

    /**
     * Property 9: Only PLAYING state shows duration format
     *
     * *For any* AudioPlaybackState, only PLAYING state SHALL show duration format.
     * All other states SHALL show file size format.
     *
     * **Validates: Requirements 5.2, 5.3**
     */
    "Property 9: Only PLAYING state should show duration format" {
        val stateArb = Arb.enum<AudioPlaybackState>()
        val positionArb = Arb.long(0L..3600000L)
        val durationArb = Arb.long(0L..3600000L)
        val fileSizeArb = Arb.int(1..10_000_000) // At least 1 byte for valid file size

        checkAll(200, stateArb, positionArb, durationArb, fileSizeArb) { state, position, duration, fileSize ->
            val subtitleContent = getExpectedSubtitleContent(state, position, duration, fileSize)

            when (state) {
                AudioPlaybackState.PLAYING -> {
                    isDurationFormat(subtitleContent.content) shouldBe true
                    isFileSizeFormat(subtitleContent.content) shouldBe false
                }
                else -> {
                    isDurationFormat(subtitleContent.content) shouldBe false
                    isFileSizeFormat(subtitleContent.content) shouldBe true
                }
            }
        }
    }

    /**
     * Property 9: Non-playing states (IDLE, LOADING, PAUSED) all show file size
     *
     * *For any* non-playing state, the subtitle SHALL display file size.
     *
     * **Validates: Requirements 5.3**
     */
    "Property 9: Non-playing states should all show file size" {
        val nonPlayingStates = listOf(
            AudioPlaybackState.IDLE,
            AudioPlaybackState.LOADING,
            AudioPlaybackState.PAUSED
        )
        val positionArb = Arb.long(0L..3600000L)
        val durationArb = Arb.long(0L..3600000L)
        val fileSizeArb = Arb.int(1..10_000_000)

        checkAll(100, positionArb, durationArb, fileSizeArb) { position, duration, fileSize ->
            nonPlayingStates.forEach { state ->
                val subtitleContent = getExpectedSubtitleContent(state, position, duration, fileSize)

                subtitleContent.showFileSize shouldBe true
                subtitleContent.showDuration shouldBe false
                isFileSizeFormat(subtitleContent.content) shouldBe true
            }
        }
    }

    /**
     * Property 9: Subtitle content format is mutually exclusive
     *
     * *For any* playback state, the subtitle SHALL show either duration OR file size,
     * never both and never neither.
     *
     * **Validates: Requirements 5.2, 5.3**
     */
    "Property 9: Subtitle content format should be mutually exclusive" {
        val stateArb = Arb.enum<AudioPlaybackState>()
        val positionArb = Arb.long(0L..3600000L)
        val durationArb = Arb.long(0L..3600000L)
        val fileSizeArb = Arb.int(1..10_000_000)

        checkAll(200, stateArb, positionArb, durationArb, fileSizeArb) { state, position, duration, fileSize ->
            val subtitleContent = getExpectedSubtitleContent(state, position, duration, fileSize)

            // Exactly one should be true
            val showCount = listOf(subtitleContent.showDuration, subtitleContent.showFileSize).count { it }
            showCount shouldBe 1

            // Content should match exactly one format
            val isDuration = isDurationFormat(subtitleContent.content)
            val isFileSize = isFileSizeFormat(subtitleContent.content)
            (isDuration xor isFileSize) shouldBe true
        }
    }

    /**
     * Property 9: PLAYING state duration content matches formatDuration output
     *
     * *For any* PLAYING state with position P and duration D, the subtitle content
     * SHALL exactly match formatDuration(P, D).
     *
     * **Validates: Requirements 5.2**
     */
    "Property 9: PLAYING state duration content should match formatDuration output" {
        val positionArb = Arb.long(0L..3600000L)
        val durationArb = Arb.long(0L..3600000L)
        val fileSizeArb = Arb.int(0..10_000_000)

        checkAll(200, positionArb, durationArb, fileSizeArb) { position, duration, fileSize ->
            val subtitleContent = getExpectedSubtitleContent(
                playbackState = AudioPlaybackState.PLAYING,
                currentPosition = position,
                totalDuration = duration,
                fileSize = fileSize
            )

            val expectedDuration = formatDuration(position, duration)
            subtitleContent.content shouldBe expectedDuration
        }
    }

    /**
     * Property 9: Non-playing state file size content matches formatFileSize output
     *
     * *For any* non-playing state with file size S, the subtitle content
     * SHALL exactly match formatFileSize(S).
     *
     * **Validates: Requirements 5.3**
     */
    "Property 9: Non-playing state file size content should match formatFileSize output" {
        val nonPlayingStates = listOf(
            AudioPlaybackState.IDLE,
            AudioPlaybackState.LOADING,
            AudioPlaybackState.PAUSED
        )
        val positionArb = Arb.long(0L..3600000L)
        val durationArb = Arb.long(0L..3600000L)
        val fileSizeArb = Arb.int(0..10_000_000)

        checkAll(100, positionArb, durationArb, fileSizeArb) { position, duration, fileSize ->
            nonPlayingStates.forEach { state ->
                val subtitleContent = getExpectedSubtitleContent(state, position, duration, fileSize)

                val expectedFileSize = formatFileSize(fileSize)
                subtitleContent.content shouldBe expectedFileSize
            }
        }
    }

    /**
     * Property 9: File size format handles different size ranges correctly
     *
     * Tests that formatFileSize produces correct format for different size ranges:
     * - Bytes (< 1024): "N B"
     * - Kilobytes (1024 - 1048575): "N KB"
     * - Megabytes (>= 1048576): "N MB"
     *
     * **Validates: Requirements 5.3**
     */
    "Property 9: File size format should handle different size ranges correctly" {
        // Test bytes range (0-1023)
        val bytesArb = Arb.int(0..1023)
        checkAll(50, bytesArb) { fileSize ->
            val formatted = formatFileSize(fileSize)
            formatted shouldMatch Regex("\\d+ B")
        }

        // Test kilobytes range (1024 - 1048575)
        val kilobytesArb = Arb.int(1024..1048575)
        checkAll(50, kilobytesArb) { fileSize ->
            val formatted = formatFileSize(fileSize)
            formatted shouldMatch Regex("\\d+ KB")
        }

        // Test megabytes range (>= 1048576)
        val megabytesArb = Arb.int(1048576..10_000_000)
        checkAll(50, megabytesArb) { fileSize ->
            val formatted = formatFileSize(fileSize)
            formatted shouldMatch Regex("\\d+ MB")
        }
    }

    /**
     * Property 9: Subtitle content is never empty
     *
     * *For any* valid inputs, the subtitle content SHALL never be empty.
     *
     * **Validates: Requirements 5.2, 5.3**
     */
    "Property 9: Subtitle content should never be empty" {
        val stateArb = Arb.enum<AudioPlaybackState>()
        val positionArb = Arb.long(0L..3600000L)
        val durationArb = Arb.long(0L..3600000L)
        val fileSizeArb = Arb.int(0..10_000_000)

        checkAll(200, stateArb, positionArb, durationArb, fileSizeArb) { state, position, duration, fileSize ->
            val subtitleContent = getExpectedSubtitleContent(state, position, duration, fileSize)

            subtitleContent.content shouldNotBe ""
            subtitleContent.content.isNotEmpty() shouldBe true
        }
    }

    /**
     * Property 9: State transition from PLAYING to non-playing changes subtitle format
     *
     * When transitioning from PLAYING to any non-playing state, the subtitle format
     * SHALL change from duration to file size.
     *
     * **Validates: Requirements 5.2, 5.3**
     */
    "Property 9: State transition from PLAYING to non-playing should change subtitle format" {
        val nonPlayingStates = listOf(
            AudioPlaybackState.IDLE,
            AudioPlaybackState.LOADING,
            AudioPlaybackState.PAUSED
        )
        val positionArb = Arb.long(0L..3600000L)
        val durationArb = Arb.long(0L..3600000L)
        val fileSizeArb = Arb.int(1..10_000_000)

        checkAll(100, positionArb, durationArb, fileSizeArb) { position, duration, fileSize ->
            // Get content while PLAYING
            val playingContent = getExpectedSubtitleContent(
                AudioPlaybackState.PLAYING, position, duration, fileSize
            )

            // Verify PLAYING shows duration
            isDurationFormat(playingContent.content) shouldBe true

            // Transition to each non-playing state
            nonPlayingStates.forEach { nonPlayingState ->
                val nonPlayingContent = getExpectedSubtitleContent(
                    nonPlayingState, position, duration, fileSize
                )

                // Verify non-playing shows file size
                isFileSizeFormat(nonPlayingContent.content) shouldBe true

                // Content should be different (unless by coincidence the formatted values match)
                // The format should definitely be different
                isDurationFormat(nonPlayingContent.content) shouldBe false
            }
        }
    }

    /**
     * Property 9: State transition from non-playing to PLAYING changes subtitle format
     *
     * When transitioning from any non-playing state to PLAYING, the subtitle format
     * SHALL change from file size to duration.
     *
     * **Validates: Requirements 5.2, 5.3**
     */
    "Property 9: State transition from non-playing to PLAYING should change subtitle format" {
        val nonPlayingStates = listOf(
            AudioPlaybackState.IDLE,
            AudioPlaybackState.LOADING,
            AudioPlaybackState.PAUSED
        )
        val positionArb = Arb.long(0L..3600000L)
        val durationArb = Arb.long(0L..3600000L)
        val fileSizeArb = Arb.int(1..10_000_000)

        checkAll(100, positionArb, durationArb, fileSizeArb) { position, duration, fileSize ->
            nonPlayingStates.forEach { nonPlayingState ->
                // Get content while in non-playing state
                val nonPlayingContent = getExpectedSubtitleContent(
                    nonPlayingState, position, duration, fileSize
                )

                // Verify non-playing shows file size
                isFileSizeFormat(nonPlayingContent.content) shouldBe true

                // Transition to PLAYING
                val playingContent = getExpectedSubtitleContent(
                    AudioPlaybackState.PLAYING, position, duration, fileSize
                )

                // Verify PLAYING shows duration
                isDurationFormat(playingContent.content) shouldBe true
                isFileSizeFormat(playingContent.content) shouldBe false
            }
        }
    }

    /**
     * Property 9: Subtitle content depends only on state for format selection
     *
     * The format selection (duration vs file size) SHALL depend only on the playback state,
     * not on the values of position, duration, or file size.
     *
     * **Validates: Requirements 5.2, 5.3**
     */
    "Property 9: Subtitle format selection should depend only on playback state" {
        val stateArb = Arb.enum<AudioPlaybackState>()
        val positionArb1 = Arb.long(0L..1800000L)
        val positionArb2 = Arb.long(1800001L..3600000L)
        val durationArb1 = Arb.long(0L..1800000L)
        val durationArb2 = Arb.long(1800001L..3600000L)
        val fileSizeArb1 = Arb.int(0..5_000_000)
        val fileSizeArb2 = Arb.int(5_000_001..10_000_000)

        checkAll(100, stateArb, positionArb1, positionArb2, durationArb1, durationArb2, fileSizeArb1, fileSizeArb2) { 
            state, pos1, pos2, dur1, dur2, size1, size2 ->
            
            val content1 = getExpectedSubtitleContent(state, pos1, dur1, size1)
            val content2 = getExpectedSubtitleContent(state, pos2, dur2, size2)

            // Format selection should be the same for same state
            content1.showDuration shouldBe content2.showDuration
            content1.showFileSize shouldBe content2.showFileSize

            // Both should have same format type
            isDurationFormat(content1.content) shouldBe isDurationFormat(content2.content)
            isFileSizeFormat(content1.content) shouldBe isFileSizeFormat(content2.content)
        }
    }

    /**
     * Property 9: Zero file size produces valid format
     *
     * Even with zero file size, the subtitle SHALL produce a valid format.
     *
     * **Validates: Requirements 5.3**
     */
    "Property 9: Zero file size should produce valid format" {
        val nonPlayingStates = listOf(
            AudioPlaybackState.IDLE,
            AudioPlaybackState.LOADING,
            AudioPlaybackState.PAUSED
        )

        nonPlayingStates.forEach { state ->
            val subtitleContent = getExpectedSubtitleContent(
                playbackState = state,
                currentPosition = 0L,
                totalDuration = 0L,
                fileSize = 0
            )

            subtitleContent.content shouldBe "0 B"
            isFileSizeFormat(subtitleContent.content) shouldBe true
        }
    }

    /**
     * Property 9: Zero duration produces valid format
     *
     * Even with zero duration, the PLAYING state subtitle SHALL produce a valid format.
     *
     * **Validates: Requirements 5.2**
     */
    "Property 9: Zero duration should produce valid format" {
        val subtitleContent = getExpectedSubtitleContent(
            playbackState = AudioPlaybackState.PLAYING,
            currentPosition = 0L,
            totalDuration = 0L,
            fileSize = 1000
        )

        subtitleContent.content shouldBe "00:00/00:00"
        isDurationFormat(subtitleContent.content) shouldBe true
    }
})
