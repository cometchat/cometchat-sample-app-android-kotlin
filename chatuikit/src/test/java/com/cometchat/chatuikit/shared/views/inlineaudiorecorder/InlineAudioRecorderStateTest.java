package com.cometchat.chatuikit.shared.views.inlineaudiorecorder;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.*;

/**
 * Property-based tests for InlineAudioRecorderState class.
 * <p>
 * Feature: inline-voice-recorder
 * Tests validate Properties 2, 3, and 4 from the design document.
 * </p>
 */
public class InlineAudioRecorderStateTest {

    private static final int PROPERTY_TEST_ITERATIONS = 100;
    private Random random;

    @Before
    public void setUp() {
        random = new Random(42); // Fixed seed for reproducibility
    }

    // ==================== Property 2: State Helper Methods Consistency ====================

    /**
     * Feature: inline-voice-recorder, Property 2: State Helper Methods Consistency
     * <p>
     * For any InlineAudioRecorderState with a given status, the helper methods
     * isRecording(), isPaused(), isCompleted(), isPlaying() SHALL return true
     * if and only if the status matches their respective InlineAudioRecorderStatus value.
     * </p>
     * Validates: Requirements 2.3
     */
    @Test
    public void property2_stateHelperMethodsConsistency() {
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            // Generate random status
            InlineAudioRecorderStatus[] allStatuses = InlineAudioRecorderStatus.values();
            InlineAudioRecorderStatus randomStatus = allStatuses[random.nextInt(allStatuses.length)];

            // Create state with random status
            InlineAudioRecorderState state = new InlineAudioRecorderState();
            state.setStatus(randomStatus);

            // Verify helper methods return correct values
            assertEquals(
                    "isRecording() should return true iff status is RECORDING",
                    randomStatus == InlineAudioRecorderStatus.RECORDING,
                    state.isRecording()
            );

            assertEquals(
                    "isPaused() should return true iff status is PAUSED",
                    randomStatus == InlineAudioRecorderStatus.PAUSED,
                    state.isPaused()
            );

            assertEquals(
                    "isCompleted() should return true iff status is COMPLETED",
                    randomStatus == InlineAudioRecorderStatus.COMPLETED,
                    state.isCompleted()
            );

            assertEquals(
                    "isPlaying() should return true iff status is PLAYING",
                    randomStatus == InlineAudioRecorderStatus.PLAYING,
                    state.isPlaying()
            );
        }
    }

    /**
     * Feature: inline-voice-recorder, Property 2: State Helper Methods Consistency
     * <p>
     * Exhaustive test for all status values to ensure helper methods are mutually exclusive.
     * </p>
     * Validates: Requirements 2.3
     */
    @Test
    public void property2_helperMethodsMutuallyExclusive() {
        for (InlineAudioRecorderStatus status : InlineAudioRecorderStatus.values()) {
            InlineAudioRecorderState state = new InlineAudioRecorderState();
            state.setStatus(status);

            // Count how many helper methods return true
            int trueCount = 0;
            if (state.isRecording()) trueCount++;
            if (state.isPaused()) trueCount++;
            if (state.isCompleted()) trueCount++;
            if (state.isPlaying()) trueCount++;

            // For IDLE and ERROR, no helper should return true
            // For other statuses, exactly one helper should return true
            if (status == InlineAudioRecorderStatus.IDLE || status == InlineAudioRecorderStatus.ERROR) {
                assertEquals(
                        "For status " + status + ", no helper method should return true",
                        0,
                        trueCount
                );
            } else {
                assertEquals(
                        "For status " + status + ", exactly one helper method should return true",
                        1,
                        trueCount
                );
            }
        }
    }

    // ==================== Property 3: State Reset Idempotence ====================

    /**
     * Feature: inline-voice-recorder, Property 3: State Reset Idempotence
     * <p>
     * For any InlineAudioRecorderState with arbitrary values, calling reset()
     * SHALL produce a state equivalent to a newly constructed InlineAudioRecorderState
     * with status=IDLE, duration=0, currentPosition=0, filePath=null, errorMessage=null,
     * and empty amplitudes list.
     * </p>
     * Validates: Requirements 2.4
     */
    @Test
    public void property3_stateResetIdempotence() {
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            // Generate state with random values
            InlineAudioRecorderState state = generateRandomState();

            // Reset the state
            state.reset();

            // Verify reset state matches initial values
            assertEquals(
                    "Status should be IDLE after reset",
                    InlineAudioRecorderStatus.IDLE,
                    state.getStatus()
            );

            assertEquals(
                    "Duration should be 0 after reset",
                    0,
                    state.getDuration()
            );

            assertEquals(
                    "CurrentPosition should be 0 after reset",
                    0,
                    state.getCurrentPosition()
            );

            assertNull(
                    "FilePath should be null after reset",
                    state.getFilePath()
            );

            assertNull(
                    "ErrorMessage should be null after reset",
                    state.getErrorMessage()
            );

            assertNotNull(
                    "Amplitudes should not be null after reset",
                    state.getAmplitudes()
            );

            assertTrue(
                    "Amplitudes should be empty after reset",
                    state.getAmplitudes().isEmpty()
            );
        }
    }

    /**
     * Feature: inline-voice-recorder, Property 3: State Reset Idempotence
     * <p>
     * Calling reset() multiple times should produce the same result.
     * </p>
     * Validates: Requirements 2.4
     */
    @Test
    public void property3_multipleResetsProduceSameResult() {
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            InlineAudioRecorderState state = generateRandomState();

            // Reset multiple times
            state.reset();
            InlineAudioRecorderState afterFirstReset = state.copy();

            state.reset();
            InlineAudioRecorderState afterSecondReset = state.copy();

            state.reset();
            InlineAudioRecorderState afterThirdReset = state.copy();

            // All should be equal
            assertEquals(
                    "Multiple resets should produce identical states",
                    afterFirstReset,
                    afterSecondReset
            );

            assertEquals(
                    "Multiple resets should produce identical states",
                    afterSecondReset,
                    afterThirdReset
            );
        }
    }

    // ==================== Property 4: State Copy Equivalence ====================

    /**
     * Feature: inline-voice-recorder, Property 4: State Copy Equivalence
     * <p>
     * For any InlineAudioRecorderState, calling copy() SHALL produce a new object
     * that is equal in all field values but is not the same instance.
     * </p>
     * Validates: Requirements 2.5
     */
    @Test
    public void property4_stateCopyEquivalence() {
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            // Generate random state
            InlineAudioRecorderState original = generateRandomState();

            // Create copy
            InlineAudioRecorderState copy = original.copy();

            // Verify copy is equal but not same instance
            assertNotSame(
                    "Copy should be a different instance",
                    original,
                    copy
            );

            assertEquals(
                    "Copy should be equal to original",
                    original,
                    copy
            );

            // Verify all fields are equal
            assertEquals(
                    "Status should be equal",
                    original.getStatus(),
                    copy.getStatus()
            );

            assertEquals(
                    "Duration should be equal",
                    original.getDuration(),
                    copy.getDuration()
            );

            assertEquals(
                    "CurrentPosition should be equal",
                    original.getCurrentPosition(),
                    copy.getCurrentPosition()
            );

            assertEquals(
                    "FilePath should be equal",
                    original.getFilePath(),
                    copy.getFilePath()
            );

            assertEquals(
                    "ErrorMessage should be equal",
                    original.getErrorMessage(),
                    copy.getErrorMessage()
            );

            assertEquals(
                    "Amplitudes should be equal",
                    original.getAmplitudes(),
                    copy.getAmplitudes()
            );
        }
    }

    /**
     * Feature: inline-voice-recorder, Property 4: State Copy Equivalence
     * <p>
     * Modifying the copy should not affect the original.
     * </p>
     * Validates: Requirements 2.5
     */
    @Test
    public void property4_copyIsIndependent() {
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            InlineAudioRecorderState original = generateRandomState();
            InlineAudioRecorderState copy = original.copy();

            // Store original values
            InlineAudioRecorderStatus originalStatus = original.getStatus();
            long originalDuration = original.getDuration();
            long originalPosition = original.getCurrentPosition();
            String originalFilePath = original.getFilePath();
            String originalErrorMessage = original.getErrorMessage();
            List<Float> originalAmplitudes = new ArrayList<>(original.getAmplitudes());

            // Modify copy
            copy.setStatus(InlineAudioRecorderStatus.ERROR);
            copy.setDuration(999999);
            copy.setCurrentPosition(888888);
            copy.setFilePath("modified/path.m4a");
            copy.setErrorMessage("Modified error");
            copy.setAmplitudes(Arrays.asList(0.1f, 0.2f, 0.3f));

            // Verify original is unchanged
            assertEquals(
                    "Original status should be unchanged",
                    originalStatus,
                    original.getStatus()
            );

            assertEquals(
                    "Original duration should be unchanged",
                    originalDuration,
                    original.getDuration()
            );

            assertEquals(
                    "Original position should be unchanged",
                    originalPosition,
                    original.getCurrentPosition()
            );

            assertEquals(
                    "Original filePath should be unchanged",
                    originalFilePath,
                    original.getFilePath()
            );

            assertEquals(
                    "Original errorMessage should be unchanged",
                    originalErrorMessage,
                    original.getErrorMessage()
            );

            assertEquals(
                    "Original amplitudes should be unchanged",
                    originalAmplitudes,
                    original.getAmplitudes()
            );
        }
    }

    // ==================== hasRecording() Tests ====================

    /**
     * Tests hasRecording() returns correct values based on filePath.
     */
    @Test
    public void hasRecording_returnsTrueForNonEmptyFilePath() {
        InlineAudioRecorderState state = new InlineAudioRecorderState();

        state.setFilePath(null);
        assertFalse("hasRecording should return false for null filePath", state.hasRecording());

        state.setFilePath("");
        assertFalse("hasRecording should return false for empty filePath", state.hasRecording());

        state.setFilePath("   ");
        assertTrue("hasRecording should return true for whitespace-only filePath", state.hasRecording());

        state.setFilePath("/path/to/audio.m4a");
        assertTrue("hasRecording should return true for valid filePath", state.hasRecording());
    }

    // ==================== Helper Methods ====================

    /**
     * Generates a random InlineAudioRecorderState for property testing.
     */
    private InlineAudioRecorderState generateRandomState() {
        InlineAudioRecorderStatus[] allStatuses = InlineAudioRecorderStatus.values();
        InlineAudioRecorderStatus randomStatus = allStatuses[random.nextInt(allStatuses.length)];

        long randomDuration = Math.abs(random.nextLong() % 600000); // 0-10 minutes
        long randomPosition = Math.abs(random.nextLong() % randomDuration + 1);

        String randomFilePath = random.nextBoolean() ? null : "/path/audio-" + random.nextInt(1000) + ".m4a";
        String randomErrorMessage = random.nextBoolean() ? null : "Error " + random.nextInt(100);

        List<Float> randomAmplitudes = new ArrayList<>();
        int amplitudeCount = random.nextInt(50);
        for (int j = 0; j < amplitudeCount; j++) {
            randomAmplitudes.add(random.nextFloat());
        }

        return new InlineAudioRecorderState(
                randomStatus,
                randomDuration,
                randomPosition,
                randomFilePath,
                randomErrorMessage,
                randomAmplitudes
        );
    }
}
