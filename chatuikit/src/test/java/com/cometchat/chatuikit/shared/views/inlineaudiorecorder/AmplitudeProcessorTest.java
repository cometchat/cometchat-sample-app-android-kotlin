package com.cometchat.chatuikit.shared.views.inlineaudiorecorder;

import org.junit.Before;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;

/**
 * Property-based tests for amplitude processing in InlineAudioRecorderViewModel.
 * <p>
 * Feature: inline-voice-recorder
 * Tests validate Properties 6 and 7 from the design document.
 * </p>
 */
public class AmplitudeProcessorTest {

    private static final int PROPERTY_TEST_ITERATIONS = 100;
    private static final float DELTA = 0.0001f;
    private Random random;

    @Before
    public void setUp() {
        random = new Random(42); // Fixed seed for reproducibility
    }

    // ==================== Property 6: Amplitude Normalization Range ====================

    /**
     * Feature: inline-voice-recorder, Property 6: Amplitude Normalization Range
     * <p>
     * For any raw amplitude value from MediaRecorder.getMaxAmplitude() (0 to 32767),
     * the normalized amplitude SHALL be in the range [0.0, 1.0].
     * </p>
     * Validates: Requirements 3.5
     */
    @Test
    public void property6_amplitudeNormalizationRange() {
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            // Generate random raw amplitude in valid range [0, 32767]
            int rawAmplitude = random.nextInt(32768); // 0 to 32767 inclusive

            // Normalize the amplitude
            float normalized = InlineAudioRecorderViewModel.normalizeAmplitude(rawAmplitude);

            // Verify normalized value is in range [0.0, 1.0]
            assertTrue(
                    "Normalized amplitude should be >= 0.0, but was " + normalized + " for raw " + rawAmplitude,
                    normalized >= 0.0f
            );

            assertTrue(
                    "Normalized amplitude should be <= 1.0, but was " + normalized + " for raw " + rawAmplitude,
                    normalized <= 1.0f
            );
        }
    }

    /**
     * Feature: inline-voice-recorder, Property 6: Amplitude Normalization Range
     * <p>
     * Tests boundary values for amplitude normalization.
     * </p>
     * Validates: Requirements 3.5
     */
    @Test
    public void property6_amplitudeNormalizationBoundaries() {
        // Test minimum value
        float normalizedMin = InlineAudioRecorderViewModel.normalizeAmplitude(0);
        assertEquals("Normalized amplitude for 0 should be 0.0", 0.0f, normalizedMin, DELTA);

        // Test maximum value
        float normalizedMax = InlineAudioRecorderViewModel.normalizeAmplitude(32767);
        assertEquals("Normalized amplitude for 32767 should be 1.0", 1.0f, normalizedMax, DELTA);

        // Test mid value
        float normalizedMid = InlineAudioRecorderViewModel.normalizeAmplitude(16383);
        assertTrue(
                "Normalized amplitude for 16383 should be approximately 0.5",
                normalizedMid >= 0.49f && normalizedMid <= 0.51f
        );
    }

    /**
     * Feature: inline-voice-recorder, Property 6: Amplitude Normalization Range
     * <p>
     * Tests that normalization handles edge cases correctly.
     * </p>
     * Validates: Requirements 3.5
     */
    @Test
    public void property6_amplitudeNormalizationEdgeCases() {
        // Test negative values (should be clamped to 0)
        float normalizedNegative = InlineAudioRecorderViewModel.normalizeAmplitude(-100);
        assertTrue(
                "Normalized amplitude for negative value should be >= 0.0",
                normalizedNegative >= 0.0f
        );

        // Test values above max (should be clamped to 1.0)
        float normalizedAboveMax = InlineAudioRecorderViewModel.normalizeAmplitude(50000);
        assertTrue(
                "Normalized amplitude for value above max should be <= 1.0",
                normalizedAboveMax <= 1.0f
        );
    }

    // ==================== Property 7: Amplitude Amplification Range ====================

    /**
     * Feature: inline-voice-recorder, Property 7: Amplitude Amplification Range
     * <p>
     * For any normalized amplitude in [0.0, 1.0], the amplified visual amplitude
     * SHALL be in the range [0.15, 1.0] to ensure minimum bar visibility.
     * </p>
     * Validates: Requirements 3.6
     */
    @Test
    public void property7_amplitudeAmplificationRange() {
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            // Generate random normalized amplitude in [0.0, 1.0]
            float normalized = random.nextFloat();

            // Amplify the amplitude
            float amplified = InlineAudioRecorderViewModel.amplifyAmplitude(normalized);

            // Verify amplified value is in range [0.15, 1.0]
            assertTrue(
                    "Amplified amplitude should be >= 0.15, but was " + amplified + " for normalized " + normalized,
                    amplified >= 0.15f
            );

            assertTrue(
                    "Amplified amplitude should be <= 1.0, but was " + amplified + " for normalized " + normalized,
                    amplified <= 1.0f
            );
        }
    }

    /**
     * Feature: inline-voice-recorder, Property 7: Amplitude Amplification Range
     * <p>
     * Tests boundary values for amplitude amplification.
     * </p>
     * Validates: Requirements 3.6
     */
    @Test
    public void property7_amplitudeAmplificationBoundaries() {
        // Test minimum normalized value (0.0)
        float amplifiedMin = InlineAudioRecorderViewModel.amplifyAmplitude(0.0f);
        assertTrue(
                "Amplified amplitude for 0.0 should be >= 0.15",
                amplifiedMin >= 0.15f
        );
        assertTrue(
                "Amplified amplitude for 0.0 should be <= 1.0",
                amplifiedMin <= 1.0f
        );

        // Test maximum normalized value (1.0)
        float amplifiedMax = InlineAudioRecorderViewModel.amplifyAmplitude(1.0f);
        assertTrue(
                "Amplified amplitude for 1.0 should be >= 0.15",
                amplifiedMax >= 0.15f
        );
        assertTrue(
                "Amplified amplitude for 1.0 should be <= 1.0",
                amplifiedMax <= 1.0f
        );
    }

    /**
     * Feature: inline-voice-recorder, Property 7: Amplitude Amplification Range
     * <p>
     * Tests that amplification is monotonically increasing (higher input = higher output).
     * </p>
     * Validates: Requirements 3.6
     */
    @Test
    public void property7_amplitudeAmplificationMonotonic() {
        float previousAmplified = 0.0f;

        // Test that amplification is monotonically increasing
        for (float normalized = 0.0f; normalized <= 1.0f; normalized += 0.01f) {
            float amplified = InlineAudioRecorderViewModel.amplifyAmplitude(normalized);

            assertTrue(
                    "Amplification should be monotonically increasing. " +
                            "For normalized " + normalized + ", amplified " + amplified +
                            " should be >= previous " + previousAmplified,
                    amplified >= previousAmplified - DELTA // Allow small floating point errors
            );

            previousAmplified = amplified;
        }
    }

    /**
     * Feature: inline-voice-recorder, Property 7: Amplitude Amplification Range
     * <p>
     * Tests the piecewise linear function breakpoints.
     * </p>
     * Validates: Requirements 3.6
     */
    @Test
    public void property7_amplitudeAmplificationBreakpoints() {
        // Test breakpoint at 0.1
        float amplifiedAt01 = InlineAudioRecorderViewModel.amplifyAmplitude(0.1f);
        assertTrue(
                "Amplified amplitude at 0.1 should be in valid range",
                amplifiedAt01 >= 0.15f && amplifiedAt01 <= 1.0f
        );

        // Test breakpoint at 0.4
        float amplifiedAt04 = InlineAudioRecorderViewModel.amplifyAmplitude(0.4f);
        assertTrue(
                "Amplified amplitude at 0.4 should be in valid range",
                amplifiedAt04 >= 0.15f && amplifiedAt04 <= 1.0f
        );

        // Verify ordering: amplified(0.1) < amplified(0.4) < amplified(1.0)
        float amplifiedAt10 = InlineAudioRecorderViewModel.amplifyAmplitude(1.0f);
        assertTrue(
                "Amplified values should be ordered: 0.1 < 0.4 < 1.0",
                amplifiedAt01 <= amplifiedAt04 && amplifiedAt04 <= amplifiedAt10
        );
    }

    /**
     * Feature: inline-voice-recorder, Property 7: Amplitude Amplification Range
     * <p>
     * Tests that amplification handles edge cases correctly.
     * </p>
     * Validates: Requirements 3.6
     */
    @Test
    public void property7_amplitudeAmplificationEdgeCases() {
        // Test negative values (should be clamped)
        float amplifiedNegative = InlineAudioRecorderViewModel.amplifyAmplitude(-0.5f);
        assertTrue(
                "Amplified amplitude for negative value should be >= 0.15",
                amplifiedNegative >= 0.15f
        );
        assertTrue(
                "Amplified amplitude for negative value should be <= 1.0",
                amplifiedNegative <= 1.0f
        );

        // Test values above 1.0 (should be clamped)
        float amplifiedAboveMax = InlineAudioRecorderViewModel.amplifyAmplitude(1.5f);
        assertTrue(
                "Amplified amplitude for value above 1.0 should be >= 0.15",
                amplifiedAboveMax >= 0.15f
        );
        assertTrue(
                "Amplified amplitude for value above 1.0 should be <= 1.0",
                amplifiedAboveMax <= 1.0f
        );
    }

    // ==================== Combined Property Tests ====================

    /**
     * Feature: inline-voice-recorder, Properties 6 & 7: End-to-End Amplitude Processing
     * <p>
     * Tests the complete amplitude processing pipeline from raw to visual amplitude.
     * </p>
     * Validates: Requirements 3.5, 3.6
     */
    @Test
    public void property6And7_endToEndAmplitudeProcessing() {
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            // Generate random raw amplitude
            int rawAmplitude = random.nextInt(32768);

            // Process through the pipeline
            float normalized = InlineAudioRecorderViewModel.normalizeAmplitude(rawAmplitude);
            float amplified = InlineAudioRecorderViewModel.amplifyAmplitude(normalized);

            // Verify final result is in valid range
            assertTrue(
                    "Final amplified value should be >= 0.15 for raw " + rawAmplitude,
                    amplified >= 0.15f
            );

            assertTrue(
                    "Final amplified value should be <= 1.0 for raw " + rawAmplitude,
                    amplified <= 1.0f
            );
        }
    }
}
