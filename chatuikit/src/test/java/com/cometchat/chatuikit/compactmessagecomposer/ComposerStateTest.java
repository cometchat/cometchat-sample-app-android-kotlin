package com.cometchat.chatuikit.compactmessagecomposer;

import com.cometchat.chatuikit.shared.views.richtexttoolbar.FormatType;

import org.junit.Before;
import org.junit.Test;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Unit tests for ComposerState class.
 */
public class ComposerStateTest {

    private ComposerState state;

    @Before
    public void setUp() {
        state = new ComposerState();
    }

    // ==================== Send Button State Tests ====================

    @Test
    public void sendButtonInactiveForEmptyText() {
        state.setText("");
        assertFalse("Send button should be inactive for empty text", state.isSendButtonActive());
    }

    @Test
    public void sendButtonInactiveForNullText() {
        state.setText(null);
        assertFalse("Send button should be inactive for null text", state.isSendButtonActive());
    }

    @Test
    public void sendButtonInactiveForWhitespaceOnlyText() {
        state.setText("   ");
        assertFalse("Send button should be inactive for whitespace-only text", state.isSendButtonActive());

        state.setText("\t\n");
        assertFalse("Send button should be inactive for tabs and newlines", state.isSendButtonActive());
    }

    @Test
    public void sendButtonActiveForNonWhitespaceText() {
        state.setText("Hello");
        assertTrue("Send button should be active for non-whitespace text", state.isSendButtonActive());

        state.setText("  Hello  ");
        assertTrue("Send button should be active for text with surrounding whitespace", state.isSendButtonActive());
    }

    @Test
    public void sendButtonActiveForSingleCharacter() {
        state.setText("a");
        assertTrue("Send button should be active for single character", state.isSendButtonActive());
    }

    @Test
    public void sendButtonInactiveForZeroWidthSpaceOnly() {
        // Zero-width space used as placeholder for code formatting
        state.setText("\u200B");
        assertFalse("Send button should be inactive for zero-width space only", state.isSendButtonActive());
    }

    @Test
    public void sendButtonInactiveForMultipleZeroWidthCharacters() {
        // Multiple zero-width characters
        state.setText("\u200B\u200C\u200D\uFEFF");
        assertFalse("Send button should be inactive for zero-width characters only", state.isSendButtonActive());
    }

    @Test
    public void sendButtonActiveForTextWithZeroWidthSpace() {
        // Text with zero-width space should still be active
        state.setText("\u200BHello\u200B");
        assertTrue("Send button should be active for text containing zero-width spaces", state.isSendButtonActive());
    }

    // ==================== Reset Tests ====================

    @Test
    public void resetClearsText() {
        state.setText("Hello World");
        state.reset();
        assertEquals("Text should be empty after reset", "", state.getText());
    }

    @Test
    public void resetClearsToolbarVisibility() {
        state.setToolbarVisible(true);
        state.reset();
        assertFalse("Toolbar should be hidden after reset", state.isToolbarVisible());
    }

    @Test
    public void resetClearsActiveFormats() {
        Set<FormatType> formats = EnumSet.of(FormatType.BOLD, FormatType.ITALIC);
        state.setActiveFormats(formats);
        state.reset();
        assertTrue("Active formats should be empty after reset", state.getActiveFormats().isEmpty());
    }

    @Test
    public void resetClearsEditingMessage() {
        // Note: We can't set a real TextMessage in unit tests, but we can verify null handling
        state.setEditingMessage(null);
        state.reset();
        assertNull("Editing message should be null after reset", state.getEditingMessage());
    }

    @Test
    public void resetClearsReplyingMessage() {
        state.setReplyingToMessage(null);
        state.reset();
        assertNull("Replying message should be null after reset", state.getReplyingToMessage());
    }

    @Test
    public void resetClearsRecordingState() {
        state.setRecording(true);
        state.setRecordingDuration(5000);
        state.reset();
        assertFalse("Recording should be false after reset", state.isRecording());
        assertEquals("Recording duration should be 0 after reset", 0, state.getRecordingDuration());
    }

    @Test
    public void resetClearsMentionState() {
        state.setMentionCount(5);
        state.setShowMentionsList(true);
        state.setShowMentionLimitBanner(true);
        state.reset();
        assertEquals("Mention count should be 0 after reset", 0, state.getMentionCount());
        assertFalse("Show mentions list should be false after reset", state.isShowMentionsList());
        assertFalse("Show mention limit banner should be false after reset", state.isShowMentionLimitBanner());
    }

    // ==================== Helper Method Tests ====================

    @Test
    public void isEditingReturnsFalseWhenNull() {
        state.setEditingMessage(null);
        assertFalse("isEditing should return false when editingMessage is null", state.isEditing());
    }

    @Test
    public void isReplyingReturnsFalseWhenNull() {
        state.setReplyingToMessage(null);
        assertFalse("isReplying should return false when replyingToMessage is null", state.isReplying());
    }

    // ==================== Copy Constructor Tests ====================

    @Test
    public void copyConstructorCopiesAllFields() {
        // Set up original state
        ComposerState original = new ComposerState();
        original.setText("Test message");
        original.setToolbarVisible(true);
        original.setRecording(true);
        original.setRecordingDuration(3000);
        original.setShowMentionsList(true);
        original.setShowMentionLimitBanner(true);
        original.setMentionCount(5);

        // Create copy
        ComposerState copy = new ComposerState(original);

        // Verify all fields are copied
        assertEquals("Text should be copied", original.getText(), copy.getText());
        assertEquals("Toolbar visibility should be copied", original.isToolbarVisible(), copy.isToolbarVisible());
        assertEquals("Recording state should be copied", original.isRecording(), copy.isRecording());
        assertEquals("Recording duration should be copied", original.getRecordingDuration(), copy.getRecordingDuration());
        assertEquals("Show mentions list should be copied", original.isShowMentionsList(), copy.isShowMentionsList());
        assertEquals("Show mention limit banner should be copied", original.isShowMentionLimitBanner(), copy.isShowMentionLimitBanner());
        assertEquals("Mention count should be copied", original.getMentionCount(), copy.getMentionCount());
    }

    @Test
    public void copyConstructorCreatesIndependentCopy() {
        ComposerState original = new ComposerState();
        original.setText("Original");

        ComposerState copy = new ComposerState(original);
        copy.setText("Modified");

        assertNotEquals("Modifying copy should not affect original", original.getText(), copy.getText());
        assertEquals("Original should remain unchanged", "Original", original.getText());
    }

    // ==================== Null Handling Tests ====================

    @Test
    public void nullTextConvertedToEmptyString() {
        state.setText(null);
        assertEquals("Null text should be converted to empty string", "", state.getText());
    }

    @Test
    public void nullActiveFormatsConvertedToEmptySet() {
        state.setActiveFormats(null);
        assertNotNull("Active formats should never be null", state.getActiveFormats());
        assertTrue("Active formats should be empty when set to null", state.getActiveFormats().isEmpty());
    }

    // ==================== Active Formats Tests ====================

    @Test
    public void activeFormatsStoredCorrectly() {
        Set<FormatType> formats = EnumSet.of(FormatType.BOLD, FormatType.ITALIC, FormatType.STRIKETHROUGH);
        state.setActiveFormats(formats);
        assertEquals("Active formats should match what was set", formats, state.getActiveFormats());
    }

    @Test
    public void emptyActiveFormatsSet() {
        Set<FormatType> formats = new HashSet<>();
        state.setActiveFormats(formats);
        assertTrue("Active formats should be empty", state.getActiveFormats().isEmpty());
    }

    @Test
    public void allFormatTypesCanBeActive() {
        Set<FormatType> allFormats = EnumSet.allOf(FormatType.class);
        state.setActiveFormats(allFormats);
        assertEquals("All format types should be stored", allFormats.size(), state.getActiveFormats().size());
    }

    // ==================== Recording State Tests ====================

    @Test
    public void recordingStateInitiallyFalse() {
        assertFalse("Recording should be false initially", state.isRecording());
    }

    @Test
    public void recordingDurationInitiallyZero() {
        assertEquals("Recording duration should be 0 initially", 0, state.getRecordingDuration());
    }

    @Test
    public void recordingStateCanBeSet() {
        state.setRecording(true);
        assertTrue("Recording should be true after setting", state.isRecording());

        state.setRecording(false);
        assertFalse("Recording should be false after setting", state.isRecording());
    }

    @Test
    public void recordingDurationCanBeSet() {
        state.setRecordingDuration(5000);
        assertEquals("Recording duration should be set correctly", 5000, state.getRecordingDuration());
    }

    // ==================== Mention State Tests ====================

    @Test
    public void mentionCountInitiallyZero() {
        assertEquals("Mention count should be 0 initially", 0, state.getMentionCount());
    }

    @Test
    public void mentionCountCanBeIncremented() {
        state.setMentionCount(1);
        assertEquals("Mention count should be 1", 1, state.getMentionCount());

        state.setMentionCount(state.getMentionCount() + 1);
        assertEquals("Mention count should be 2", 2, state.getMentionCount());
    }

    @Test
    public void mentionCountCanBeDecremented() {
        state.setMentionCount(5);
        state.setMentionCount(state.getMentionCount() - 1);
        assertEquals("Mention count should be 4", 4, state.getMentionCount());
    }

    @Test
    public void showMentionsListInitiallyFalse() {
        assertFalse("Show mentions list should be false initially", state.isShowMentionsList());
    }

    @Test
    public void showMentionLimitBannerInitiallyFalse() {
        assertFalse("Show mention limit banner should be false initially", state.isShowMentionLimitBanner());
    }

    // ==================== Inline Recording Mode Tests ====================

    /**
     * Feature: inline-voice-recorder, Property 15: Voice Button Mode Selection
     * Validates: Requirements 8.3, 8.4
     * 
     * For any SingleLineComposer configuration, tapping Voice_Record_Button SHALL:
     * - Show inline recorder when useInlineAudioRecorder is true
     * - Open bottom sheet when useInlineAudioRecorder is false
     */
    @Test
    public void inlineRecordingModeInitiallyFalse() {
        assertFalse("Inline recording mode should be false initially", state.isInlineRecordingMode());
    }

    @Test
    public void inlineRecordingModeCanBeSet() {
        state.setInlineRecordingMode(true);
        assertTrue("Inline recording mode should be true after setting", state.isInlineRecordingMode());

        state.setInlineRecordingMode(false);
        assertFalse("Inline recording mode should be false after setting", state.isInlineRecordingMode());
    }

    @Test
    public void resetClearsInlineRecordingMode() {
        state.setInlineRecordingMode(true);
        state.reset();
        assertFalse("Inline recording mode should be false after reset", state.isInlineRecordingMode());
    }

    @Test
    public void copyConstructorCopiesInlineRecordingMode() {
        ComposerState original = new ComposerState();
        original.setInlineRecordingMode(true);

        ComposerState copy = new ComposerState(original);

        assertEquals("Inline recording mode should be copied", 
                original.isInlineRecordingMode(), copy.isInlineRecordingMode());
    }

    /**
     * Property test: Voice Button Mode Selection
     * For any boolean value of useInlineAudioRecorder, the state should correctly
     * track whether inline recording mode is active.
     * 
     * **Validates: Requirements 8.3, 8.4**
     */
    @Test
    public void voiceButtonModeSelectionProperty() {
        // Test with useInlineAudioRecorder = true
        state.setInlineRecordingMode(true);
        assertTrue("When inline recording mode is set to true, isInlineRecordingMode() should return true",
                state.isInlineRecordingMode());

        // Test with useInlineAudioRecorder = false
        state.setInlineRecordingMode(false);
        assertFalse("When inline recording mode is set to false, isInlineRecordingMode() should return false",
                state.isInlineRecordingMode());

        // Test toggling multiple times
        for (int i = 0; i < 10; i++) {
            boolean expected = (i % 2 == 0);
            state.setInlineRecordingMode(expected);
            assertEquals("Inline recording mode should match the set value after " + (i + 1) + " toggles",
                    expected, state.isInlineRecordingMode());
        }
    }

    /**
     * Property test: Inline Recording Mode Independence
     * Setting inline recording mode should not affect other state properties.
     * 
     * **Validates: Requirements 8.9**
     */
    @Test
    public void inlineRecordingModeIndependence() {
        // Set up initial state
        state.setText("Test message");
        state.setToolbarVisible(true);
        state.setRecording(true);
        state.setRecordingDuration(5000);
        state.setMentionCount(3);

        // Toggle inline recording mode
        state.setInlineRecordingMode(true);

        // Verify other state properties are unchanged
        assertEquals("Text should be unchanged", "Test message", state.getText());
        assertTrue("Toolbar visibility should be unchanged", state.isToolbarVisible());
        assertTrue("Recording state should be unchanged", state.isRecording());
        assertEquals("Recording duration should be unchanged", 5000, state.getRecordingDuration());
        assertEquals("Mention count should be unchanged", 3, state.getMentionCount());

        // Toggle back
        state.setInlineRecordingMode(false);

        // Verify other state properties are still unchanged
        assertEquals("Text should still be unchanged", "Test message", state.getText());
        assertTrue("Toolbar visibility should still be unchanged", state.isToolbarVisible());
        assertTrue("Recording state should still be unchanged", state.isRecording());
        assertEquals("Recording duration should still be unchanged", 5000, state.getRecordingDuration());
        assertEquals("Mention count should still be unchanged", 3, state.getMentionCount());
    }
}
