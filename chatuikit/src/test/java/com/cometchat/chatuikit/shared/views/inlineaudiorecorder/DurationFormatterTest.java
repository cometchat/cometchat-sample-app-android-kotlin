package com.cometchat.chatuikit.shared.views.inlineaudiorecorder;

import org.junit.Before;
import org.junit.Test;

import java.util.Random;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

/**
 * Property-based tests for duration formatting and seek calculation in InlineAudioRecorderViewModel.
 * <p>
 * Feature: inline-voice-recorder
 * Tests validate Properties 1 and 8 from the design document.
 * </p>
 */
public class DurationFormatterTest {

    private static final int PROPERTY_TEST_ITERATIONS = 100;
    private static final float DELTA = 0.0001f;
    private Random random;

    // Pattern for M:SS format (e.g., "0:00", "1:30", "59:59", "120:00")
    private static final Pattern DURATION_PATTERN = Pattern.compile("^\\d+:\\d{2}$");

    @Before
    public void setUp() {
        random = new Random(42); // Fixed seed for reproducibility
    }

    // ==================== Property 1: Duration Formatting ====================

    /**
     * Feature: inline-voice-recorder, Property 1: Duration Formatting
     * <p>
     * For any non-negative duration in milliseconds, the formatDuration() function
     * SHALL produce a string in "M:SS" format where M is minutes (0+) and SS is
     * zero-padded seconds (00-59).
     * </p>
     * Validates: Requirements 1.7
     */
    @Test
    public void property1_durationFormattingPattern() {
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            // Generate random non-negative duration (0 to 10 hours in ms)
            long durationMs = Math.abs(random.nextLong() % (10 * 60 * 60 * 1000));

            // Format the duration
            String formatted = InlineAudioRecorderViewModel.formatDuration(durationMs);

            // Verify format matches M:SS pattern
            assertTrue(
                    "Formatted duration '" + formatted + "' should match M:SS pattern for " + durationMs + "ms",
                    DURATION_PATTERN.matcher(formatted).matches()
            );
        }
    }

    /**
     * Feature: inline-voice-recorder, Property 1: Duration Formatting
     * <p>
     * Verifies that the minutes and seconds values are correctly calculated.
     * </p>
     * Validates: Requirements 1.7
     */
    @Test
    public void property1_durationFormattingCorrectValues() {
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            // Generate random non-negative duration
            long durationMs = Math.abs(random.nextLong() % (10 * 60 * 60 * 1000));

            // Format the duration
            String formatted = InlineAudioRecorderViewModel.formatDuration(durationMs);

            // Parse the formatted string
            String[] parts = formatted.split(":");
            int formattedMinutes = Integer.parseInt(parts[0]);
            int formattedSeconds = Integer.parseInt(parts[1]);

            // Calculate expected values
            long totalSeconds = durationMs / 1000;
            int expectedMinutes = (int) (totalSeconds / 60);
            int expectedSeconds = (int) (totalSeconds % 60);

            // Verify values match
            assertEquals(
                    "Minutes should be " + expectedMinutes + " for " + durationMs + "ms",
                    expectedMinutes,
                    formattedMinutes
            );

            assertEquals(
                    "Seconds should be " + expectedSeconds + " for " + durationMs + "ms",
                    expectedSeconds,
                    formattedSeconds
            );
        }
    }

    /**
     * Feature: inline-voice-recorder, Property 1: Duration Formatting
     * <p>
     * Verifies that seconds are always in range [0, 59].
     * </p>
     * Validates: Requirements 1.7
     */
    @Test
    public void property1_durationFormattingSecondsRange() {
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            // Generate random non-negative duration
            long durationMs = Math.abs(random.nextLong() % (10 * 60 * 60 * 1000));

            // Format the duration
            String formatted = InlineAudioRecorderViewModel.formatDuration(durationMs);

            // Parse seconds
            String[] parts = formatted.split(":");
            int formattedSeconds = Integer.parseInt(parts[1]);

            // Verify seconds are in valid range
            assertTrue(
                    "Seconds should be >= 0, but was " + formattedSeconds,
                    formattedSeconds >= 0
            );

            assertTrue(
                    "Seconds should be <= 59, but was " + formattedSeconds,
                    formattedSeconds <= 59
            );
        }
    }

    /**
     * Feature: inline-voice-recorder, Property 1: Duration Formatting
     * <p>
     * Tests specific known values for duration formatting.
     * </p>
     * Validates: Requirements 1.7
     */
    @Test
    public void property1_durationFormattingKnownValues() {
        // Test 0 milliseconds
        assertEquals("0:00", InlineAudioRecorderViewModel.formatDuration(0));

        // Test 1 second
        assertEquals("0:01", InlineAudioRecorderViewModel.formatDuration(1000));

        // Test 59 seconds
        assertEquals("0:59", InlineAudioRecorderViewModel.formatDuration(59000));

        // Test 1 minute
        assertEquals("1:00", InlineAudioRecorderViewModel.formatDuration(60000));

        // Test 1 minute 30 seconds
        assertEquals("1:30", InlineAudioRecorderViewModel.formatDuration(90000));

        // Test 10 minutes
        assertEquals("10:00", InlineAudioRecorderViewModel.formatDuration(600000));

        // Test 61 minutes 1 second (over an hour)
        assertEquals("61:01", InlineAudioRecorderViewModel.formatDuration(3661000));
    }

    /**
     * Feature: inline-voice-recorder, Property 1: Duration Formatting
     * <p>
     * Tests that negative durations are handled gracefully.
     * </p>
     * Validates: Requirements 1.7
     */
    @Test
    public void property1_durationFormattingNegativeValues() {
        // Negative values should be treated as 0
        String formatted = InlineAudioRecorderViewModel.formatDuration(-1000);
        assertTrue(
                "Negative duration should produce valid format",
                DURATION_PATTERN.matcher(formatted).matches()
        );
    }

    // ==================== Property 8: Seek Position Calculation ====================

    /**
     * Feature: inline-voice-recorder, Property 8: Seek Position Calculation
     * <p>
     * For any progress value in [0.0, 1.0] and any positive duration,
     * seekTo(progress) SHALL calculate position as (progress * duration)
     * clamped to [0, duration].
     * </p>
     * Validates: Requirements 4.6
     */
    @Test
    public void property8_seekPositionCalculationRange() {
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            // Generate random progress in [0.0, 1.0]
            float progress = random.nextFloat();

            // Generate random positive duration (1ms to 10 minutes)
            long duration = Math.abs(random.nextLong() % (10 * 60 * 1000)) + 1;

            // Calculate seek position
            int position = InlineAudioRecorderViewModel.calculateSeekPosition(progress, duration);

            // Verify position is in valid range [0, duration]
            assertTrue(
                    "Position should be >= 0, but was " + position + " for progress " + progress + " and duration " + duration,
                    position >= 0
            );

            assertTrue(
                    "Position should be <= duration (" + duration + "), but was " + position,
                    position <= duration
            );
        }
    }

    /**
     * Feature: inline-voice-recorder, Property 8: Seek Position Calculation
     * <p>
     * Verifies that the calculated position is approximately (progress * duration).
     * </p>
     * Validates: Requirements 4.6
     */
    @Test
    public void property8_seekPositionCalculationCorrectness() {
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            // Generate random progress in [0.0, 1.0]
            float progress = random.nextFloat();

            // Generate random positive duration
            long duration = Math.abs(random.nextLong() % (10 * 60 * 1000)) + 1;

            // Calculate seek position
            int position = InlineAudioRecorderViewModel.calculateSeekPosition(progress, duration);

            // Calculate expected position
            long expectedPosition = (long) (progress * duration);
            expectedPosition = Math.max(0, Math.min(duration, expectedPosition));

            // Verify position is correct (allow for rounding)
            assertEquals(
                    "Position should be approximately " + expectedPosition + " for progress " + progress + " and duration " + duration,
                    expectedPosition,
                    position,
                    1 // Allow 1ms difference for rounding
            );
        }
    }

    /**
     * Feature: inline-voice-recorder, Property 8: Seek Position Calculation
     * <p>
     * Tests boundary values for seek position calculation.
     * </p>
     * Validates: Requirements 4.6
     */
    @Test
    public void property8_seekPositionCalculationBoundaries() {
        long duration = 60000; // 1 minute

        // Test progress = 0.0
        int positionAtZero = InlineAudioRecorderViewModel.calculateSeekPosition(0.0f, duration);
        assertEquals("Position at progress 0.0 should be 0", 0, positionAtZero);

        // Test progress = 1.0
        int positionAtOne = InlineAudioRecorderViewModel.calculateSeekPosition(1.0f, duration);
        assertEquals("Position at progress 1.0 should be duration", duration, positionAtOne);

        // Test progress = 0.5
        int positionAtHalf = InlineAudioRecorderViewModel.calculateSeekPosition(0.5f, duration);
        assertEquals("Position at progress 0.5 should be half duration", duration / 2, positionAtHalf);
    }

    /**
     * Feature: inline-voice-recorder, Property 8: Seek Position Calculation
     * <p>
     * Tests that progress values outside [0.0, 1.0] are clamped.
     * </p>
     * Validates: Requirements 4.6
     */
    @Test
    public void property8_seekPositionCalculationClamping() {
        long duration = 60000; // 1 minute

        // Test progress < 0.0 (should clamp to 0)
        int positionNegative = InlineAudioRecorderViewModel.calculateSeekPosition(-0.5f, duration);
        assertEquals("Position for negative progress should be 0", 0, positionNegative);

        // Test progress > 1.0 (should clamp to duration)
        int positionAboveOne = InlineAudioRecorderViewModel.calculateSeekPosition(1.5f, duration);
        assertEquals("Position for progress > 1.0 should be duration", duration, positionAboveOne);
    }

    /**
     * Feature: inline-voice-recorder, Property 8: Seek Position Calculation
     * <p>
     * Tests that seek position is monotonically increasing with progress.
     * </p>
     * Validates: Requirements 4.6
     */
    @Test
    public void property8_seekPositionCalculationMonotonic() {
        long duration = 60000; // 1 minute
        int previousPosition = 0;

        // Test that position increases with progress
        for (float progress = 0.0f; progress <= 1.0f; progress += 0.01f) {
            int position = InlineAudioRecorderViewModel.calculateSeekPosition(progress, duration);

            assertTrue(
                    "Position should be monotonically increasing. " +
                            "For progress " + progress + ", position " + position +
                            " should be >= previous " + previousPosition,
                    position >= previousPosition
            );

            previousPosition = position;
        }
    }

    /**
     * Feature: inline-voice-recorder, Property 8: Seek Position Calculation
     * <p>
     * Tests seek position calculation with various duration values.
     * </p>
     * Validates: Requirements 4.6
     */
    @Test
    public void property8_seekPositionCalculationVariousDurations() {
        float progress = 0.5f;

        // Test with very short duration
        int positionShort = InlineAudioRecorderViewModel.calculateSeekPosition(progress, 100);
        assertEquals("Position for 100ms duration at 0.5 progress", 50, positionShort);

        // Test with 1 second duration
        int positionOneSecond = InlineAudioRecorderViewModel.calculateSeekPosition(progress, 1000);
        assertEquals("Position for 1s duration at 0.5 progress", 500, positionOneSecond);

        // Test with 1 minute duration
        int positionOneMinute = InlineAudioRecorderViewModel.calculateSeekPosition(progress, 60000);
        assertEquals("Position for 1min duration at 0.5 progress", 30000, positionOneMinute);

        // Test with 10 minute duration
        int positionTenMinutes = InlineAudioRecorderViewModel.calculateSeekPosition(progress, 600000);
        assertEquals("Position for 10min duration at 0.5 progress", 300000, positionTenMinutes);
    }

    /**
     * Feature: inline-voice-recorder, Property 8: Seek Position Calculation
     * <p>
     * Tests edge case with zero duration.
     * </p>
     * Validates: Requirements 4.6
     */
    @Test
    public void property8_seekPositionCalculationZeroDuration() {
        // With zero duration, position should always be 0
        int position = InlineAudioRecorderViewModel.calculateSeekPosition(0.5f, 0);
        assertEquals("Position for zero duration should be 0", 0, position);
    }
}
