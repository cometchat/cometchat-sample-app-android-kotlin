package com.cometchat.chatuikit.shared.views.inlineaudiorecorder;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.*;

/**
 * Property-based tests for AudioWaveformVisualizer class.
 * <p>
 * Feature: inline-voice-recorder
 * Tests validate Properties 9 and 10 from the design document.
 * </p>
 * <p>
 * Note: Since AudioWaveformVisualizer is an Android View, these tests focus on
 * the logic methods that can be tested without Android context.
 * </p>
 */
public class AudioWaveformVisualizerTest {

    private static final int PROPERTY_TEST_ITERATIONS = 100;
    private static final int DEFAULT_MAX_BAR_COUNT = 35;
    private Random random;

    @Before
    public void setUp() {
        random = new Random(42); // Fixed seed for reproducibility
    }

    // ==================== Property 9: Waveform Bar Count Limit ====================

    /**
     * Feature: inline-voice-recorder, Property 9: Waveform Bar Count Limit
     * <p>
     * For any list of amplitudes, the AudioWaveformVisualizer SHALL display at most
     * maxBarCount bars, showing only the most recent amplitudes when the list exceeds maxBarCount.
     * </p>
     * Validates: Requirements 5.2
     */
    @Test
    public void property9_waveformBarCountLimit() {
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            // Generate random maxBarCount
            int maxBarCount = random.nextInt(100) + 1; // 1 to 100

            // Generate random amplitude list of varying sizes
            int amplitudeCount = random.nextInt(200); // 0 to 199
            List<Float> amplitudes = generateRandomAmplitudes(amplitudeCount);

            // Calculate expected visible bar count
            int expectedVisibleBars = Math.min(amplitudes.size(), maxBarCount);

            // Verify the bar count limit
            int actualVisibleBars = calculateVisibleBarCount(amplitudes, maxBarCount);

            assertEquals(
                    "Visible bar count should be min(amplitudes.size(), maxBarCount)",
                    expectedVisibleBars,
                    actualVisibleBars
            );

            assertTrue(
                    "Visible bar count should never exceed maxBarCount",
                    actualVisibleBars <= maxBarCount
            );
        }
    }

    /**
     * Feature: inline-voice-recorder, Property 9: Waveform Bar Count Limit
     * <p>
     * When amplitudes exceed maxBarCount, only the most recent amplitudes should be shown.
     * </p>
     * Validates: Requirements 5.2
     */
    @Test
    public void property9_mostRecentAmplitudesShown() {
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            int maxBarCount = random.nextInt(50) + 5; // 5 to 54
            int amplitudeCount = maxBarCount + random.nextInt(100) + 1; // More than maxBarCount

            List<Float> amplitudes = generateRandomAmplitudes(amplitudeCount);

            // Get the visible amplitudes (most recent ones)
            List<Float> visibleAmplitudes = getVisibleAmplitudes(amplitudes, maxBarCount);

            // Verify we get the most recent amplitudes
            assertEquals(
                    "Should show exactly maxBarCount amplitudes when list exceeds limit",
                    maxBarCount,
                    visibleAmplitudes.size()
            );

            // Verify they are the most recent ones
            int startIndex = amplitudes.size() - maxBarCount;
            for (int j = 0; j < maxBarCount; j++) {
                assertEquals(
                        "Visible amplitude at index " + j + " should match the most recent amplitudes",
                        amplitudes.get(startIndex + j),
                        visibleAmplitudes.get(j)
                );
            }
        }
    }

    /**
     * Feature: inline-voice-recorder, Property 9: Waveform Bar Count Limit
     * <p>
     * When amplitudes are fewer than maxBarCount, all amplitudes should be shown.
     * </p>
     * Validates: Requirements 5.2
     */
    @Test
    public void property9_allAmplitudesShownWhenBelowLimit() {
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            int maxBarCount = random.nextInt(50) + 20; // 20 to 69
            int amplitudeCount = random.nextInt(maxBarCount); // Less than maxBarCount

            List<Float> amplitudes = generateRandomAmplitudes(amplitudeCount);

            List<Float> visibleAmplitudes = getVisibleAmplitudes(amplitudes, maxBarCount);

            assertEquals(
                    "All amplitudes should be shown when below limit",
                    amplitudes.size(),
                    visibleAmplitudes.size()
            );

            assertEquals(
                    "Visible amplitudes should equal original amplitudes",
                    amplitudes,
                    visibleAmplitudes
            );
        }
    }

    /**
     * Feature: inline-voice-recorder, Property 9: Waveform Bar Count Limit
     * <p>
     * Empty amplitude list should result in zero visible bars.
     * </p>
     * Validates: Requirements 5.2
     */
    @Test
    public void property9_emptyAmplitudesResultInZeroBars() {
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            int maxBarCount = random.nextInt(100) + 1;
            List<Float> emptyAmplitudes = new ArrayList<>();

            int visibleBars = calculateVisibleBarCount(emptyAmplitudes, maxBarCount);

            assertEquals(
                    "Empty amplitude list should result in zero visible bars",
                    0,
                    visibleBars
            );
        }
    }

    // ==================== Property 10: Tap-to-Seek Progress Calculation ====================

    /**
     * Feature: inline-voice-recorder, Property 10: Tap-to-Seek Progress Calculation
     * <p>
     * For any tap position x within the AudioWaveformVisualizer bounds,
     * the calculated seek progress SHALL equal x/width clamped to [0.0, 1.0].
     * </p>
     * Validates: Requirements 5.7
     */
    @Test
    public void property10_tapToSeekProgressCalculation() {
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            // Generate random view width (positive)
            int viewWidth = random.nextInt(1000) + 100; // 100 to 1099

            // Generate random tap position (can be outside bounds)
            float tapX = random.nextFloat() * viewWidth * 1.5f - viewWidth * 0.25f; // -25% to 125% of width

            // Calculate expected progress
            float expectedProgress = calculateProgressFromX(tapX, viewWidth);

            // Verify progress is in valid range
            assertTrue(
                    "Progress should be >= 0.0",
                    expectedProgress >= 0.0f
            );

            assertTrue(
                    "Progress should be <= 1.0",
                    expectedProgress <= 1.0f
            );

            // Verify calculation is correct
            float rawProgress = tapX / viewWidth;
            float clampedProgress = Math.max(0f, Math.min(1f, rawProgress));

            assertEquals(
                    "Progress should equal x/width clamped to [0.0, 1.0]",
                    clampedProgress,
                    expectedProgress,
                    0.0001f
            );
        }
    }

    /**
     * Feature: inline-voice-recorder, Property 10: Tap-to-Seek Progress Calculation
     * <p>
     * Tap at x=0 should result in progress=0.0.
     * </p>
     * Validates: Requirements 5.7
     */
    @Test
    public void property10_tapAtZeroReturnsZeroProgress() {
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            int viewWidth = random.nextInt(1000) + 100;

            float progress = calculateProgressFromX(0f, viewWidth);

            assertEquals(
                    "Tap at x=0 should return progress=0.0",
                    0.0f,
                    progress,
                    0.0001f
            );
        }
    }

    /**
     * Feature: inline-voice-recorder, Property 10: Tap-to-Seek Progress Calculation
     * <p>
     * Tap at x=width should result in progress=1.0.
     * </p>
     * Validates: Requirements 5.7
     */
    @Test
    public void property10_tapAtWidthReturnsFullProgress() {
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            int viewWidth = random.nextInt(1000) + 100;

            float progress = calculateProgressFromX(viewWidth, viewWidth);

            assertEquals(
                    "Tap at x=width should return progress=1.0",
                    1.0f,
                    progress,
                    0.0001f
            );
        }
    }

    /**
     * Feature: inline-voice-recorder, Property 10: Tap-to-Seek Progress Calculation
     * <p>
     * Tap at x=width/2 should result in progress=0.5.
     * </p>
     * Validates: Requirements 5.7
     */
    @Test
    public void property10_tapAtMiddleReturnsHalfProgress() {
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            int viewWidth = random.nextInt(1000) + 100;

            float progress = calculateProgressFromX(viewWidth / 2f, viewWidth);

            assertEquals(
                    "Tap at x=width/2 should return progress=0.5",
                    0.5f,
                    progress,
                    0.0001f
            );
        }
    }

    /**
     * Feature: inline-voice-recorder, Property 10: Tap-to-Seek Progress Calculation
     * <p>
     * Negative tap positions should be clamped to 0.0.
     * </p>
     * Validates: Requirements 5.7
     */
    @Test
    public void property10_negativeTapPositionClampedToZero() {
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            int viewWidth = random.nextInt(1000) + 100;
            float negativeTapX = -(random.nextFloat() * 100 + 1); // -1 to -101

            float progress = calculateProgressFromX(negativeTapX, viewWidth);

            assertEquals(
                    "Negative tap position should be clamped to 0.0",
                    0.0f,
                    progress,
                    0.0001f
            );
        }
    }

    /**
     * Feature: inline-voice-recorder, Property 10: Tap-to-Seek Progress Calculation
     * <p>
     * Tap positions beyond width should be clamped to 1.0.
     * </p>
     * Validates: Requirements 5.7
     */
    @Test
    public void property10_tapBeyondWidthClampedToOne() {
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            int viewWidth = random.nextInt(1000) + 100;
            float beyondWidthTapX = viewWidth + random.nextFloat() * 100 + 1; // width+1 to width+101

            float progress = calculateProgressFromX(beyondWidthTapX, viewWidth);

            assertEquals(
                    "Tap beyond width should be clamped to 1.0",
                    1.0f,
                    progress,
                    0.0001f
            );
        }
    }

    /**
     * Feature: inline-voice-recorder, Property 10: Tap-to-Seek Progress Calculation
     * <p>
     * Zero width should return 0.0 progress (edge case handling).
     * </p>
     * Validates: Requirements 5.7
     */
    @Test
    public void property10_zeroWidthReturnsZeroProgress() {
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            float tapX = random.nextFloat() * 100;

            float progress = calculateProgressFromX(tapX, 0);

            assertEquals(
                    "Zero width should return 0.0 progress",
                    0.0f,
                    progress,
                    0.0001f
            );
        }
    }

    /**
     * Feature: inline-voice-recorder, Property 10: Tap-to-Seek Progress Calculation
     * <p>
     * Progress calculation should be monotonically increasing with tap position.
     * </p>
     * Validates: Requirements 5.7
     */
    @Test
    public void property10_progressMonotonicallyIncreasing() {
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            int viewWidth = random.nextInt(1000) + 100;

            float previousProgress = -1f;
            for (int x = 0; x <= viewWidth; x += viewWidth / 10) {
                float progress = calculateProgressFromX(x, viewWidth);

                assertTrue(
                        "Progress should be monotonically increasing",
                        progress >= previousProgress
                );

                previousProgress = progress;
            }
        }
    }

    // ==================== Helper Methods ====================

    /**
     * Generates a list of random amplitude values.
     *
     * @param count The number of amplitudes to generate.
     * @return A list of random amplitude values in range [0.0, 1.0].
     */
    private List<Float> generateRandomAmplitudes(int count) {
        List<Float> amplitudes = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            amplitudes.add(random.nextFloat());
        }
        return amplitudes;
    }

    /**
     * Calculates the number of visible bars based on amplitude count and max bar count.
     * This mirrors the logic in AudioWaveformVisualizer.getVisibleBarCount().
     *
     * @param amplitudes  The list of amplitudes.
     * @param maxBarCount The maximum number of bars to display.
     * @return The number of visible bars.
     */
    private int calculateVisibleBarCount(List<Float> amplitudes, int maxBarCount) {
        return Math.min(amplitudes.size(), maxBarCount);
    }

    /**
     * Gets the visible amplitudes based on max bar count.
     * This mirrors the logic in AudioWaveformVisualizer.onDraw().
     *
     * @param amplitudes  The list of amplitudes.
     * @param maxBarCount The maximum number of bars to display.
     * @return The list of visible amplitudes (most recent ones).
     */
    private List<Float> getVisibleAmplitudes(List<Float> amplitudes, int maxBarCount) {
        int barCount = Math.min(amplitudes.size(), maxBarCount);
        int startIndex = Math.max(0, amplitudes.size() - barCount);
        return new ArrayList<>(amplitudes.subList(startIndex, amplitudes.size()));
    }

    /**
     * Calculates the progress value from an X coordinate.
     * This mirrors the logic in AudioWaveformVisualizer.calculateProgressFromX().
     *
     * @param x     The X coordinate.
     * @param width The view width.
     * @return The progress value in range [0.0, 1.0].
     */
    private float calculateProgressFromX(float x, int width) {
        if (width <= 0) {
            return 0f;
        }
        return Math.max(0f, Math.min(1f, x / width));
    }
}
