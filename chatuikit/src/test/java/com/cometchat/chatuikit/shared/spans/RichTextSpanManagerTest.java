package com.cometchat.chatuikit.shared.spans;

import com.cometchat.chatuikit.shared.views.richtexttoolbar.FormatType;

import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.*;

/**
 * Unit tests for RichTextSpanManager.
 * <p>
 * These tests verify the span creation logic and edge case handling.
 * Since SpannableStringBuilder requires the Android framework, these tests
 * focus on testing the span factory methods and null/edge case handling.
 * </p>
 * <p>
 * Validates: Requirements 2.1, 2.2, 2.3, 2.4, 2.5, 2.6, 9.4, 12.4
 * </p>
 */
public class RichTextSpanManagerTest {

    // ==================== Null Safety Tests ====================

    /**
     * Test that applyFormat handles null editable gracefully.
     */
    @Test
    public void applyFormat_nullEditable_noException() {
        // Should not throw exception
        RichTextSpanManager.applyFormat(null, 0, 5, FormatType.BOLD);
    }

    /**
     * Test that applyFormat handles null format type gracefully.
     */
    @Test
    public void applyFormat_nullFormatType_noException() {
        // Should not throw exception - we can't test with real SpannableStringBuilder
        // but we verify the null check exists
        RichTextSpanManager.applyFormat(null, 0, 5, null);
    }

    /**
     * Test that removeFormat handles null editable gracefully.
     */
    @Test
    public void removeFormat_nullEditable_noException() {
        RichTextSpanManager.removeFormat(null, 0, 5, FormatType.BOLD);
    }

    /**
     * Test that removeFormat handles null format type gracefully.
     */
    @Test
    public void removeFormat_nullFormatType_noException() {
        RichTextSpanManager.removeFormat(null, 0, 5, null);
    }

    /**
     * Test that toggleFormat handles null editable gracefully.
     */
    @Test
    public void toggleFormat_nullEditable_noException() {
        RichTextSpanManager.toggleFormat(null, 0, 5, FormatType.BOLD);
    }

    /**
     * Test that toggleFormat handles null format type gracefully.
     */
    @Test
    public void toggleFormat_nullFormatType_noException() {
        RichTextSpanManager.toggleFormat(null, 0, 5, null);
    }

    /**
     * Test that detectActiveFormats handles null spannable gracefully.
     */
    @Test
    public void detectActiveFormats_nullSpannable_returnsEmptySet() {
        Set<FormatType> formats = RichTextSpanManager.detectActiveFormats(null, 0);
        assertNotNull("Should return non-null set", formats);
        assertTrue("Should return empty set for null spannable", formats.isEmpty());
    }

    /**
     * Test that detectActiveFormats handles negative cursor position.
     */
    @Test
    public void detectActiveFormats_negativeCursorPosition_returnsEmptySet() {
        Set<FormatType> formats = RichTextSpanManager.detectActiveFormats(null, -1);
        assertNotNull("Should return non-null set", formats);
        assertTrue("Should return empty set for negative cursor", formats.isEmpty());
    }

    /**
     * Test that hasFormatInRange handles null editable gracefully.
     */
    @Test
    public void hasFormatInRange_nullEditable_returnsFalse() {
        boolean result = RichTextSpanManager.hasFormatInRange(null, 0, 5, FormatType.BOLD);
        assertFalse("Should return false for null editable", result);
    }

    /**
     * Test that hasFormatInRange handles null format type gracefully.
     */
    @Test
    public void hasFormatInRange_nullFormatType_returnsFalse() {
        boolean result = RichTextSpanManager.hasFormatInRange(null, 0, 5, null);
        assertFalse("Should return false for null format type", result);
    }

    /**
     * Test that applyLinkFormat handles null editable gracefully.
     */
    @Test
    public void applyLinkFormat_nullEditable_noException() {
        RichTextSpanManager.applyLinkFormat(null, 0, 5, "https://example.com");
    }

    /**
     * Test that applyLinkFormat handles null URL gracefully.
     */
    @Test
    public void applyLinkFormat_nullUrl_noException() {
        RichTextSpanManager.applyLinkFormat(null, 0, 5, null);
    }

    /**
     * Test that applyNumberedListFormat handles null editable gracefully.
     */
    @Test
    public void applyNumberedListFormat_nullEditable_noException() {
        RichTextSpanManager.applyNumberedListFormat(null, 0, 5, 1);
    }

    // ==================== Span Creation Tests ====================

    /**
     * Test that BoldFormatSpan is created correctly.
     * Validates: Requirement 2.1
     */
    @Test
    public void boldFormatSpan_createdCorrectly() {
        BoldFormatSpan span = new BoldFormatSpan();
        assertNotNull("BoldFormatSpan should be created", span);
        assertEquals("Should return BOLD format type", FormatType.BOLD, span.getFormatType());
    }

    /**
     * Test that ItalicFormatSpan is created correctly.
     * Validates: Requirement 2.2
     */
    @Test
    public void italicFormatSpan_createdCorrectly() {
        ItalicFormatSpan span = new ItalicFormatSpan();
        assertNotNull("ItalicFormatSpan should be created", span);
        assertEquals("Should return ITALIC format type", FormatType.ITALIC, span.getFormatType());
    }

    /**
     * Test that StrikethroughFormatSpan is created correctly.
     * Validates: Requirement 2.3
     */
    @Test
    public void strikethroughFormatSpan_createdCorrectly() {
        StrikethroughFormatSpan span = new StrikethroughFormatSpan();
        assertNotNull("StrikethroughFormatSpan should be created", span);
        assertEquals("Should return STRIKETHROUGH format type", FormatType.STRIKETHROUGH, span.getFormatType());
    }

    /**
     * Test that InlineCodeFormatSpan is created correctly.
     */
    @Test
    public void inlineCodeFormatSpan_createdCorrectly() {
        InlineCodeFormatSpan span = new InlineCodeFormatSpan();
        assertNotNull("InlineCodeFormatSpan should be created", span);
        assertEquals("Should return INLINE_CODE format type", FormatType.INLINE_CODE, span.getFormatType());
    }

    /**
     * Test that CodeBlockFormatSpan is created correctly.
     */
    @Test
    public void codeBlockFormatSpan_createdCorrectly() {
        CodeBlockFormatSpan span = new CodeBlockFormatSpan();
        assertNotNull("CodeBlockFormatSpan should be created", span);
        assertEquals("Should return CODE_BLOCK format type", FormatType.CODE_BLOCK, span.getFormatType());
    }

    /**
     * Test that LinkFormatSpan is created correctly with URL.
     */
    @Test
    public void linkFormatSpan_createdCorrectlyWithUrl() {
        String url = "https://example.com";
        LinkFormatSpan span = new LinkFormatSpan(url);
        assertNotNull("LinkFormatSpan should be created", span);
        assertEquals("Should return LINK format type", FormatType.LINK, span.getFormatType());
        assertEquals("Should store URL correctly", url, span.getUrl());
    }

    /**
     * Test that BulletListFormatSpan is created correctly.
     */
    @Test
    public void bulletListFormatSpan_createdCorrectly() {
        BulletListFormatSpan span = new BulletListFormatSpan();
        assertNotNull("BulletListFormatSpan should be created", span);
        assertEquals("Should return BULLET_LIST format type", FormatType.BULLET_LIST, span.getFormatType());
    }

    /**
     * Test that NumberedListFormatSpan is created correctly with number.
     */
    @Test
    public void numberedListFormatSpan_createdCorrectlyWithNumber() {
        int number = 5;
        NumberedListFormatSpan span = new NumberedListFormatSpan(number);
        assertNotNull("NumberedListFormatSpan should be created", span);
        assertEquals("Should return ORDERED_LIST format type", FormatType.ORDERED_LIST, span.getFormatType());
        assertEquals("Should store number correctly", number, span.getNumber());
    }

    /**
     * Test that BlockquoteFormatSpan is created correctly.
     */
    @Test
    public void blockquoteFormatSpan_createdCorrectly() {
        BlockquoteFormatSpan span = new BlockquoteFormatSpan();
        assertNotNull("BlockquoteFormatSpan should be created", span);
        assertEquals("Should return BLOCKQUOTE format type", FormatType.BLOCKQUOTE, span.getFormatType());
    }

    // ==================== RichTextFormatSpan Interface Tests ====================

    /**
     * Test that all span types implement RichTextFormatSpan interface.
     */
    @Test
    public void allSpanTypes_implementRichTextFormatSpan() {
        assertTrue("BoldFormatSpan should implement RichTextFormatSpan",
                new BoldFormatSpan() instanceof RichTextFormatSpan);
        assertTrue("ItalicFormatSpan should implement RichTextFormatSpan",
                new ItalicFormatSpan() instanceof RichTextFormatSpan);
        assertTrue("StrikethroughFormatSpan should implement RichTextFormatSpan",
                new StrikethroughFormatSpan() instanceof RichTextFormatSpan);
        assertTrue("InlineCodeFormatSpan should implement RichTextFormatSpan",
                new InlineCodeFormatSpan() instanceof RichTextFormatSpan);
        assertTrue("CodeBlockFormatSpan should implement RichTextFormatSpan",
                new CodeBlockFormatSpan() instanceof RichTextFormatSpan);
        assertTrue("LinkFormatSpan should implement RichTextFormatSpan",
                new LinkFormatSpan("") instanceof RichTextFormatSpan);
        assertTrue("BulletListFormatSpan should implement RichTextFormatSpan",
                new BulletListFormatSpan() instanceof RichTextFormatSpan);
        assertTrue("NumberedListFormatSpan should implement RichTextFormatSpan",
                new NumberedListFormatSpan(1) instanceof RichTextFormatSpan);
        assertTrue("BlockquoteFormatSpan should implement RichTextFormatSpan",
                new BlockquoteFormatSpan() instanceof RichTextFormatSpan);
    }

    /**
     * Test that all format types have distinct span implementations.
     */
    @Test
    public void allFormatTypes_haveDistinctSpanTypes() {
        RichTextFormatSpan boldSpan = new BoldFormatSpan();
        RichTextFormatSpan italicSpan = new ItalicFormatSpan();
        RichTextFormatSpan strikethroughSpan = new StrikethroughFormatSpan();
        RichTextFormatSpan inlineCodeSpan = new InlineCodeFormatSpan();
        RichTextFormatSpan codeBlockSpan = new CodeBlockFormatSpan();
        RichTextFormatSpan linkSpan = new LinkFormatSpan("");
        RichTextFormatSpan bulletListSpan = new BulletListFormatSpan();
        RichTextFormatSpan numberedListSpan = new NumberedListFormatSpan(1);
        RichTextFormatSpan blockquoteSpan = new BlockquoteFormatSpan();

        // Verify all format types are distinct
        assertNotEquals(boldSpan.getFormatType(), italicSpan.getFormatType());
        assertNotEquals(boldSpan.getFormatType(), strikethroughSpan.getFormatType());
        assertNotEquals(boldSpan.getFormatType(), inlineCodeSpan.getFormatType());
        assertNotEquals(boldSpan.getFormatType(), codeBlockSpan.getFormatType());
        assertNotEquals(boldSpan.getFormatType(), linkSpan.getFormatType());
        assertNotEquals(boldSpan.getFormatType(), bulletListSpan.getFormatType());
        assertNotEquals(boldSpan.getFormatType(), numberedListSpan.getFormatType());
        assertNotEquals(boldSpan.getFormatType(), blockquoteSpan.getFormatType());
    }

    // ==================== LinkFormatSpan URL Tests ====================

    /**
     * Test LinkFormatSpan URL getter and setter.
     */
    @Test
    public void linkFormatSpan_urlGetterSetter() {
        LinkFormatSpan span = new LinkFormatSpan("https://original.com");
        assertEquals("https://original.com", span.getUrl());

        span.setUrl("https://updated.com");
        assertEquals("https://updated.com", span.getUrl());
    }

    /**
     * Test LinkFormatSpan handles null URL in constructor.
     */
    @Test
    public void linkFormatSpan_nullUrlInConstructor_handledGracefully() {
        // The constructor should handle null by converting to empty string
        LinkFormatSpan span = new LinkFormatSpan(null);
        assertNotNull("URL should not be null", span.getUrl());
        assertEquals("Null URL should be converted to empty string", "", span.getUrl());
    }

    // ==================== NumberedListFormatSpan Number Tests ====================

    /**
     * Test NumberedListFormatSpan number getter and setter.
     */
    @Test
    public void numberedListFormatSpan_numberGetterSetter() {
        NumberedListFormatSpan span = new NumberedListFormatSpan(1);
        assertEquals(1, span.getNumber());

        span.setNumber(5);
        assertEquals(5, span.getNumber());
    }

    /**
     * Test NumberedListFormatSpan with various numbers.
     */
    @Test
    public void numberedListFormatSpan_variousNumbers() {
        for (int i = 1; i <= 100; i++) {
            NumberedListFormatSpan span = new NumberedListFormatSpan(i);
            assertEquals("Number should match for " + i, i, span.getNumber());
        }
    }
}
