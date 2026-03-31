package com.cometchat.chatuikit.shared.formatters;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for CometChatRichTextFormatter.
 * <p>
 * These tests verify:
 * - Formatter extends correct base class
 * - Tracking character configuration
 * - No-op methods don't throw exceptions
 * - Null input handling
 * </p>
 * <p>
 * Note: Tests that require SpannableStringBuilder with actual span application
 * need instrumentation tests or Robolectric, as SpannableStringBuilder.toString()
 * returns null in pure JUnit tests without Android framework.
 * </p>
 * <p>
 * Validates: Requirements 1.1, 12.1
 * </p>
 */
public class CometChatRichTextFormatterTest {

    private CometChatRichTextFormatter formatter;

    @Before
    public void setUp() {
        formatter = new CometChatRichTextFormatter();
    }

    // ==================== Formatter Base Class Tests ====================

    /**
     * Test that CometChatRichTextFormatter extends CometChatTextFormatter.
     * Validates: Requirement 1.1
     */
    @Test
    public void formatter_extendsCometChatTextFormatter() {
        assertTrue("CometChatRichTextFormatter should extend CometChatTextFormatter",
                formatter instanceof CometChatTextFormatter);
    }

    /**
     * Test that formatter uses null character as tracking character (no suggestions).
     */
    @Test
    public void formatter_usesNullTrackingCharacter() {
        assertEquals("Tracking character should be null character",
                '\0', formatter.getTrackingCharacter());
    }

    /**
     * Test that getId returns the tracking character.
     */
    @Test
    public void formatter_getIdReturnsTrackingCharacter() {
        assertEquals("getId should return tracking character",
                '\0', formatter.getId());
    }

    /**
     * Test that search method doesn't throw exception.
     */
    @Test
    public void search_doesNotThrowException() {
        // Should not throw exception - it's a no-op
        formatter.search(null, "test");
        formatter.search(null, null);
        formatter.search(null, "");
    }

    /**
     * Test that onScrollToBottom method doesn't throw exception.
     */
    @Test
    public void onScrollToBottom_doesNotThrowException() {
        // Should not throw exception - it's a no-op
        formatter.onScrollToBottom();
    }

    // ==================== Null Input Tests ====================

    /**
     * Test that prepareLeftMessageBubbleSpan handles null input.
     * Validates: Requirement 12.1
     */
    @Test
    public void prepareLeftMessageBubbleSpan_nullInput_returnsNull() {
        Object result = formatter.prepareLeftMessageBubbleSpan(null, null, null);
        assertNull("Should return null for null input", result);
    }

    /**
     * Test that prepareRightMessageBubbleSpan handles null input.
     * Validates: Requirement 12.1
     */
    @Test
    public void prepareRightMessageBubbleSpan_nullInput_returnsNull() {
        Object result = formatter.prepareRightMessageBubbleSpan(null, null, null);
        assertNull("Should return null for null input", result);
    }

    // ==================== Formatter Configuration Tests ====================

    /**
     * Test that formatter can be instantiated multiple times.
     */
    @Test
    public void formatter_multipleInstances_areIndependent() {
        CometChatRichTextFormatter formatter1 = new CometChatRichTextFormatter();
        CometChatRichTextFormatter formatter2 = new CometChatRichTextFormatter();
        
        assertNotSame("Multiple instances should be different objects", formatter1, formatter2);
        assertEquals("Both should have same tracking character", 
                formatter1.getTrackingCharacter(), formatter2.getTrackingCharacter());
    }

    /**
     * Test that formatter doesn't disable suggestions by default.
     */
    @Test
    public void formatter_suggestionsNotDisabledByDefault() {
        // The formatter uses '\0' tracking character which effectively disables suggestions
        // but getDisableSuggestions should return false by default
        assertFalse("Suggestions should not be explicitly disabled by default",
                formatter.getDisableSuggestions());
    }

    /**
     * Test that formatter has empty selected list by default.
     */
    @Test
    public void formatter_hasEmptySelectedListByDefault() {
        assertNotNull("Selected list should not be null", formatter.getSelectedList());
        assertTrue("Selected list should be empty by default", formatter.getSelectedList().isEmpty());
    }

    /**
     * Test that formatter has null user by default.
     */
    @Test
    public void formatter_hasNullUserByDefault() {
        assertNull("User should be null by default", formatter.getUser());
    }

    /**
     * Test that formatter has null group by default.
     */
    @Test
    public void formatter_hasNullGroupByDefault() {
        assertNull("Group should be null by default", formatter.getGroup());
    }
}
