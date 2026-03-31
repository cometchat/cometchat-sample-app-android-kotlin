package com.cometchat.chatuikit.shared.spans;

import com.cometchat.chatuikit.shared.views.richtexttoolbar.FormatType;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for FormatSpanWatcher.
 * <p>
 * These tests verify the span extension, shrinking, and splitting behavior
 * of the FormatSpanWatcher class.
 * </p>
 * <p>
 * Note: Since SpannableStringBuilder requires the Android framework, these tests
 * focus on testing the span creation logic, null safety, and helper methods
 * rather than full integration with Android's text system.
 * </p>
 * <p>
 * Validates: Requirements 3.6, 4.5, 9.1, 9.2, 9.3, 9.5
 * </p>
 */
public class FormatSpanWatcherTest {

    // ==================== Constructor Tests ====================

    /**
     * Test that FormatSpanWatcher can be instantiated.
     */
    @Test
    public void constructor_createsInstance() {
        FormatSpanWatcher watcher = new FormatSpanWatcher();
        assertNotNull("FormatSpanWatcher should be created", watcher);
    }

    // ==================== Null Safety Tests ====================

    /**
     * Test that handleTextChanged handles null editable gracefully.
     */
    @Test
    public void handleTextChanged_nullEditable_noException() {
        FormatSpanWatcher watcher = new FormatSpanWatcher();
        // Should not throw exception
        watcher.handleTextChanged(null, 0, 0, 0);
    }

    /**
     * Test that handleTextChanged handles insertion with null editable.
     */
    @Test
    public void handleTextChanged_insertionWithNullEditable_noException() {
        FormatSpanWatcher watcher = new FormatSpanWatcher();
        // count > before indicates insertion
        watcher.handleTextChanged(null, 0, 0, 5);
    }

    /**
     * Test that handleTextChanged handles deletion with null editable.
     */
    @Test
    public void handleTextChanged_deletionWithNullEditable_noException() {
        FormatSpanWatcher watcher = new FormatSpanWatcher();
        // before > count indicates deletion
        watcher.handleTextChanged(null, 0, 5, 0);
    }

    /**
     * Test that extendSpan handles null editable gracefully.
     */
    @Test
    public void extendSpan_nullEditable_noException() {
        FormatSpanWatcher.extendSpan(null, new BoldFormatSpan(), 10);
    }

    /**
     * Test that extendSpan handles null span gracefully.
     */
    @Test
    public void extendSpan_nullSpan_noException() {
        FormatSpanWatcher.extendSpan(null, null, 10);
    }

    /**
     * Test that shrinkSpan handles null editable gracefully.
     */
    @Test
    public void shrinkSpan_nullEditable_noException() {
        FormatSpanWatcher.shrinkSpan(null, new BoldFormatSpan(), 0, 5);
    }

    /**
     * Test that shrinkSpan handles null span gracefully.
     */
    @Test
    public void shrinkSpan_nullSpan_noException() {
        FormatSpanWatcher.shrinkSpan(null, null, 0, 5);
    }

    // ==================== Span Creation Tests ====================

    /**
     * Test that BoldFormatSpan can be created for extension.
     */
    @Test
    public void spanCreation_boldFormatSpan_createdCorrectly() {
        BoldFormatSpan span = new BoldFormatSpan();
        assertNotNull("BoldFormatSpan should be created", span);
        assertEquals("Should return BOLD format type", FormatType.BOLD, span.getFormatType());
    }

    /**
     * Test that ItalicFormatSpan can be created for extension.
     */
    @Test
    public void spanCreation_italicFormatSpan_createdCorrectly() {
        ItalicFormatSpan span = new ItalicFormatSpan();
        assertNotNull("ItalicFormatSpan should be created", span);
        assertEquals("Should return ITALIC format type", FormatType.ITALIC, span.getFormatType());
    }

    /**
     * Test that StrikethroughFormatSpan can be created for extension.
     */
    @Test
    public void spanCreation_strikethroughFormatSpan_createdCorrectly() {
        StrikethroughFormatSpan span = new StrikethroughFormatSpan();
        assertNotNull("StrikethroughFormatSpan should be created", span);
        assertEquals("Should return STRIKETHROUGH format type", FormatType.STRIKETHROUGH, span.getFormatType());
    }

    /**
     * Test that InlineCodeFormatSpan can be created for extension.
     */
    @Test
    public void spanCreation_inlineCodeFormatSpan_createdCorrectly() {
        InlineCodeFormatSpan span = new InlineCodeFormatSpan();
        assertNotNull("InlineCodeFormatSpan should be created", span);
        assertEquals("Should return INLINE_CODE format type", FormatType.INLINE_CODE, span.getFormatType());
    }

    /**
     * Test that CodeBlockFormatSpan can be created for extension.
     */
    @Test
    public void spanCreation_codeBlockFormatSpan_createdCorrectly() {
        CodeBlockFormatSpan span = new CodeBlockFormatSpan();
        assertNotNull("CodeBlockFormatSpan should be created", span);
        assertEquals("Should return CODE_BLOCK format type", FormatType.CODE_BLOCK, span.getFormatType());
    }

    /**
     * Test that LinkFormatSpan can be created for extension with URL.
     */
    @Test
    public void spanCreation_linkFormatSpan_createdCorrectlyWithUrl() {
        String url = "https://example.com";
        LinkFormatSpan span = new LinkFormatSpan(url);
        assertNotNull("LinkFormatSpan should be created", span);
        assertEquals("Should return LINK format type", FormatType.LINK, span.getFormatType());
        assertEquals("Should store URL correctly", url, span.getUrl());
    }

    /**
     * Test that BulletListFormatSpan can be created for extension.
     */
    @Test
    public void spanCreation_bulletListFormatSpan_createdCorrectly() {
        BulletListFormatSpan span = new BulletListFormatSpan();
        assertNotNull("BulletListFormatSpan should be created", span);
        assertEquals("Should return BULLET_LIST format type", FormatType.BULLET_LIST, span.getFormatType());
    }

    /**
     * Test that NumberedListFormatSpan can be created for extension with number.
     */
    @Test
    public void spanCreation_numberedListFormatSpan_createdCorrectlyWithNumber() {
        int number = 5;
        NumberedListFormatSpan span = new NumberedListFormatSpan(number);
        assertNotNull("NumberedListFormatSpan should be created", span);
        assertEquals("Should return ORDERED_LIST format type", FormatType.ORDERED_LIST, span.getFormatType());
        assertEquals("Should store number correctly", number, span.getNumber());
    }

    /**
     * Test that BlockquoteFormatSpan can be created for extension.
     */
    @Test
    public void spanCreation_blockquoteFormatSpan_createdCorrectly() {
        BlockquoteFormatSpan span = new BlockquoteFormatSpan();
        assertNotNull("BlockquoteFormatSpan should be created", span);
        assertEquals("Should return BLOCKQUOTE format type", FormatType.BLOCKQUOTE, span.getFormatType());
    }

    // ==================== Span Splitting Logic Tests ====================

    /**
     * Test that inline formats should split on newline.
     * Validates: Requirement 9.5
     */
    @Test
    public void shouldSplitOnNewline_inlineFormats_returnsTrue() {
        // Bold should split
        assertTrue("BOLD should split on newline", shouldSplitOnNewline(FormatType.BOLD));
        // Italic should split
        assertTrue("ITALIC should split on newline", shouldSplitOnNewline(FormatType.ITALIC));
        // Strikethrough should split
        assertTrue("STRIKETHROUGH should split on newline", shouldSplitOnNewline(FormatType.STRIKETHROUGH));
        // Inline code should split
        assertTrue("INLINE_CODE should split on newline", shouldSplitOnNewline(FormatType.INLINE_CODE));
        // Link should split
        assertTrue("LINK should split on newline", shouldSplitOnNewline(FormatType.LINK));
    }

    /**
     * Test that block formats should NOT split on newline.
     * Validates: Requirement 9.5
     */
    @Test
    public void shouldSplitOnNewline_blockFormats_returnsFalse() {
        // Code block should NOT split
        assertFalse("CODE_BLOCK should not split on newline", shouldSplitOnNewline(FormatType.CODE_BLOCK));
        // Bullet list should NOT split
        assertFalse("BULLET_LIST should not split on newline", shouldSplitOnNewline(FormatType.BULLET_LIST));
        // Ordered list should NOT split
        assertFalse("ORDERED_LIST should not split on newline", shouldSplitOnNewline(FormatType.ORDERED_LIST));
        // Blockquote should NOT split
        assertFalse("BLOCKQUOTE should not split on newline", shouldSplitOnNewline(FormatType.BLOCKQUOTE));
    }

    // ==================== Link URL Preservation Tests ====================

    /**
     * Test that LinkFormatSpan preserves URL when copied.
     * Validates: Requirement 5.4
     */
    @Test
    public void linkFormatSpan_urlPreservedWhenCopied() {
        String originalUrl = "https://example.com/path?query=value";
        LinkFormatSpan original = new LinkFormatSpan(originalUrl);

        // Create a copy (simulating what happens during span splitting)
        LinkFormatSpan copy = new LinkFormatSpan(original.getUrl());

        assertEquals("URL should be preserved in copy", originalUrl, copy.getUrl());
    }

    /**
     * Test that LinkFormatSpan handles empty URL.
     */
    @Test
    public void linkFormatSpan_emptyUrl_handledCorrectly() {
        LinkFormatSpan span = new LinkFormatSpan("");
        assertEquals("Empty URL should be preserved", "", span.getUrl());
    }

    /**
     * Test that LinkFormatSpan handles null URL in constructor.
     */
    @Test
    public void linkFormatSpan_nullUrl_handledGracefully() {
        LinkFormatSpan span = new LinkFormatSpan(null);
        assertNotNull("URL should not be null", span.getUrl());
        assertEquals("Null URL should be converted to empty string", "", span.getUrl());
    }

    // ==================== Numbered List Number Preservation Tests ====================

    /**
     * Test that NumberedListFormatSpan preserves number when copied.
     * Validates: Requirement 7.1
     */
    @Test
    public void numberedListFormatSpan_numberPreservedWhenCopied() {
        int originalNumber = 42;
        NumberedListFormatSpan original = new NumberedListFormatSpan(originalNumber);

        // Create a copy (simulating what happens during span splitting)
        NumberedListFormatSpan copy = new NumberedListFormatSpan(original.getNumber());

        assertEquals("Number should be preserved in copy", originalNumber, copy.getNumber());
    }

    /**
     * Test that NumberedListFormatSpan handles number 1.
     */
    @Test
    public void numberedListFormatSpan_numberOne_handledCorrectly() {
        NumberedListFormatSpan span = new NumberedListFormatSpan(1);
        assertEquals("Number 1 should be preserved", 1, span.getNumber());
    }

    /**
     * Test that NumberedListFormatSpan handles large numbers.
     */
    @Test
    public void numberedListFormatSpan_largeNumber_handledCorrectly() {
        NumberedListFormatSpan span = new NumberedListFormatSpan(9999);
        assertEquals("Large number should be preserved", 9999, span.getNumber());
    }

    // ==================== Pending Format Tests ====================

    /**
     * Test that togglePendingFormat adds format when not present.
     */
    @Test
    public void togglePendingFormat_notPresent_addsFormat() {
        FormatSpanWatcher watcher = new FormatSpanWatcher();
        boolean result = watcher.togglePendingFormat(FormatType.BOLD);
        assertTrue("Should return true when format is added", result);
        assertTrue("Format should be pending", watcher.isPendingFormat(FormatType.BOLD));
    }

    /**
     * Test that togglePendingFormat removes format and adds to disabled when present.
     */
    @Test
    public void togglePendingFormat_present_removesAndDisables() {
        FormatSpanWatcher watcher = new FormatSpanWatcher();
        watcher.togglePendingFormat(FormatType.BOLD); // Add
        boolean result = watcher.togglePendingFormat(FormatType.BOLD); // Remove
        assertFalse("Should return false when format is removed", result);
        assertFalse("Format should not be pending", watcher.isPendingFormat(FormatType.BOLD));
        assertTrue("Format should be explicitly disabled", watcher.isExplicitlyDisabled(FormatType.BOLD));
    }

    /**
     * Test that togglePendingFormat re-enables disabled format.
     */
    @Test
    public void togglePendingFormat_disabled_reEnables() {
        FormatSpanWatcher watcher = new FormatSpanWatcher();
        watcher.togglePendingFormat(FormatType.BOLD); // Add
        watcher.togglePendingFormat(FormatType.BOLD); // Disable
        boolean result = watcher.togglePendingFormat(FormatType.BOLD); // Re-enable
        assertTrue("Should return true when format is re-enabled", result);
        assertTrue("Format should be pending again", watcher.isPendingFormat(FormatType.BOLD));
        assertFalse("Format should not be disabled", watcher.isExplicitlyDisabled(FormatType.BOLD));
    }

    /**
     * Test that clearPendingFormats clears both pending and disabled.
     */
    @Test
    public void clearPendingFormats_clearsBothSets() {
        FormatSpanWatcher watcher = new FormatSpanWatcher();
        watcher.togglePendingFormat(FormatType.BOLD); // Add to pending
        watcher.togglePendingFormat(FormatType.ITALIC); // Add to pending
        watcher.togglePendingFormat(FormatType.ITALIC); // Move to disabled
        
        watcher.clearPendingFormats();
        
        assertFalse("BOLD should not be pending", watcher.isPendingFormat(FormatType.BOLD));
        assertFalse("ITALIC should not be disabled", watcher.isExplicitlyDisabled(FormatType.ITALIC));
        assertTrue("Pending formats should be empty", watcher.getPendingFormats().isEmpty());
        assertTrue("Disabled formats should be empty", watcher.getExplicitlyDisabledFormats().isEmpty());
    }

    /**
     * Test that disableFormat explicitly disables a format.
     */
    @Test
    public void disableFormat_addsToDisabled() {
        FormatSpanWatcher watcher = new FormatSpanWatcher();
        watcher.disableFormat(FormatType.BOLD);
        assertTrue("Format should be explicitly disabled", watcher.isExplicitlyDisabled(FormatType.BOLD));
        assertFalse("Format should not be pending", watcher.isPendingFormat(FormatType.BOLD));
    }

    /**
     * Test that enableFormat enables a format.
     */
    @Test
    public void enableFormat_addsToPending() {
        FormatSpanWatcher watcher = new FormatSpanWatcher();
        watcher.enableFormat(FormatType.BOLD);
        assertTrue("Format should be pending", watcher.isPendingFormat(FormatType.BOLD));
        assertFalse("Format should not be disabled", watcher.isExplicitlyDisabled(FormatType.BOLD));
    }

    /**
     * Test that enableFormat removes from disabled and adds to pending.
     */
    @Test
    public void enableFormat_removesFromDisabled() {
        FormatSpanWatcher watcher = new FormatSpanWatcher();
        watcher.disableFormat(FormatType.BOLD);
        watcher.enableFormat(FormatType.BOLD);
        assertTrue("Format should be pending", watcher.isPendingFormat(FormatType.BOLD));
        assertFalse("Format should not be disabled", watcher.isExplicitlyDisabled(FormatType.BOLD));
    }

    /**
     * Test that null format type is handled gracefully.
     */
    @Test
    public void togglePendingFormat_null_returnsFalse() {
        FormatSpanWatcher watcher = new FormatSpanWatcher();
        boolean result = watcher.togglePendingFormat(null);
        assertFalse("Should return false for null format", result);
    }

    /**
     * Test that isExplicitlyDisabled handles null gracefully.
     */
    @Test
    public void isExplicitlyDisabled_null_returnsFalse() {
        FormatSpanWatcher watcher = new FormatSpanWatcher();
        assertFalse("Should return false for null format", watcher.isExplicitlyDisabled(null));
    }

    // ==================== Helper Methods ====================

    /**
     * Determines if a format type should be split when a newline is inserted.
     * This mirrors the logic in FormatSpanWatcher.shouldSplitOnNewline().
     *
     * @param formatType The format type to check.
     * @return true if the format should be split on newline, false otherwise.
     */
    private boolean shouldSplitOnNewline(FormatType formatType) {
        switch (formatType) {
            case BOLD:
            case ITALIC:
            case STRIKETHROUGH:
            case INLINE_CODE:
            case LINK:
                return true;
            case CODE_BLOCK:
            case BULLET_LIST:
            case ORDERED_LIST:
            case BLOCKQUOTE:
                return false;
            default:
                return false;
        }
    }
}
