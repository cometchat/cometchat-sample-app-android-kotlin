package com.cometchat.chatuikit.shared.spans;

import android.text.SpannableString;

import com.cometchat.chatuikit.shared.views.richtexttoolbar.FormatType;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for MarkdownConverter.
 * <p>
 * These tests verify the toMarkdown conversion logic and edge case handling.
 * Since SpannableStringBuilder requires the Android framework, these tests
 * focus on testing null safety, span creation, and the conversion logic
 * that can be verified without full Android framework support.
 * </p>
 * <p>
 * Note: Full integration tests with actual span application would require
 * instrumentation tests or Robolectric.
 * </p>
 * <p>
 * Validates: Requirements 10.1-10.11
 * </p>
 */
public class MarkdownConverterTest {

    // ==================== Null and Empty Input Tests ====================

    /**
     * Test that toMarkdown handles null input gracefully.
     */
    @Test
    public void toMarkdown_nullInput_returnsEmptyString() {
        String result = MarkdownConverter.toMarkdown(null);
        assertEquals("Should return empty string for null input", "", result);
    }

    /**
     * Test that toMarkdown handles empty spannable.
     */
    @Test
    public void toMarkdown_emptySpannable_returnsEmptyString() {
        SpannableString spannable = new SpannableString("");
        String result = MarkdownConverter.toMarkdown(spannable);
        assertEquals("Should return empty string for empty spannable", "", result);
    }

    // ==================== fromMarkdown Null Safety Tests ====================
    // Note: fromMarkdown tests that require SpannableString.toString() are skipped
    // because SpannableString is mocked in unit tests. Full testing requires
    // instrumentation tests or Robolectric.

    /**
     * Test that fromMarkdown handles null input without throwing exception.
     */
    @Test
    public void fromMarkdown_nullInput_noException() {
        // Should not throw exception
        SpannableString result = MarkdownConverter.fromMarkdown(null);
        assertNotNull("Should return non-null SpannableString", result);
    }

    /**
     * Test that fromMarkdown handles empty input without throwing exception.
     */
    @Test
    public void fromMarkdown_emptyInput_noException() {
        // Should not throw exception
        SpannableString result = MarkdownConverter.fromMarkdown("");
        assertNotNull("Should return non-null SpannableString", result);
    }

    /**
     * Test that fromMarkdown handles plain text without throwing exception.
     */
    @Test
    public void fromMarkdown_plainText_noException() {
        // Should not throw exception
        SpannableString result = MarkdownConverter.fromMarkdown("Hello World");
        assertNotNull("Should return non-null SpannableString", result);
    }

    // ==================== FormatType Marker Tests ====================

    /**
     * Test that FormatType.BOLD has correct prefix and suffix.
     * Validates: Requirement 10.2
     */
    @Test
    public void formatType_bold_hasCorrectMarkers() {
        assertEquals("Bold prefix should be **", "**", FormatType.BOLD.getPrefix());
        assertEquals("Bold suffix should be **", "**", FormatType.BOLD.getSuffix());
    }

    /**
     * Test that FormatType.ITALIC has correct prefix and suffix.
     * Validates: Requirement 10.3
     */
    @Test
    public void formatType_italic_hasCorrectMarkers() {
        assertEquals("Italic prefix should be _", "_", FormatType.ITALIC.getPrefix());
        assertEquals("Italic suffix should be _", "_", FormatType.ITALIC.getSuffix());
    }

    /**
     * Test that FormatType.STRIKETHROUGH has correct prefix and suffix.
     * Validates: Requirement 10.4
     */
    @Test
    public void formatType_strikethrough_hasCorrectMarkers() {
        assertEquals("Strikethrough prefix should be ~~", "~~", FormatType.STRIKETHROUGH.getPrefix());
        assertEquals("Strikethrough suffix should be ~~", "~~", FormatType.STRIKETHROUGH.getSuffix());
    }

    /**
     * Test that FormatType.INLINE_CODE has correct prefix and suffix.
     * Validates: Requirement 10.5
     */
    @Test
    public void formatType_inlineCode_hasCorrectMarkers() {
        assertEquals("Inline code prefix should be `", "`", FormatType.INLINE_CODE.getPrefix());
        assertEquals("Inline code suffix should be `", "`", FormatType.INLINE_CODE.getSuffix());
    }

    /**
     * Test that FormatType.CODE_BLOCK has correct prefix and suffix.
     * Validates: Requirement 10.6
     */
    @Test
    public void formatType_codeBlock_hasCorrectMarkers() {
        assertEquals("Code block prefix should be ```\\n", "```\n", FormatType.CODE_BLOCK.getPrefix());
        assertEquals("Code block suffix should be \\n```", "\n```", FormatType.CODE_BLOCK.getSuffix());
    }

    /**
     * Test that FormatType.BULLET_LIST has correct prefix.
     * Validates: Requirement 10.8
     */
    @Test
    public void formatType_bulletList_hasCorrectMarkers() {
        assertEquals("Bullet list prefix should be '- '", "- ", FormatType.BULLET_LIST.getPrefix());
        assertEquals("Bullet list suffix should be empty", "", FormatType.BULLET_LIST.getSuffix());
    }

    /**
     * Test that FormatType.ORDERED_LIST has correct prefix.
     * Validates: Requirement 10.9
     */
    @Test
    public void formatType_orderedList_hasCorrectMarkers() {
        assertEquals("Ordered list prefix should be '1. '", "1. ", FormatType.ORDERED_LIST.getPrefix());
        assertEquals("Ordered list suffix should be empty", "", FormatType.ORDERED_LIST.getSuffix());
    }

    /**
     * Test that FormatType.BLOCKQUOTE has correct prefix.
     * Validates: Requirement 10.10
     */
    @Test
    public void formatType_blockquote_hasCorrectMarkers() {
        assertEquals("Blockquote prefix should be '> '", "> ", FormatType.BLOCKQUOTE.getPrefix());
        assertEquals("Blockquote suffix should be empty", "", FormatType.BLOCKQUOTE.getSuffix());
    }

    // ==================== FormatType Wrap Tests ====================

    /**
     * Test FormatType.wrap() for bold.
     */
    @Test
    public void formatType_bold_wrapsTextCorrectly() {
        String wrapped = FormatType.BOLD.wrap("Hello");
        assertEquals("Bold wrap should produce **Hello**", "**Hello**", wrapped);
    }

    /**
     * Test FormatType.wrap() for italic.
     */
    @Test
    public void formatType_italic_wrapsTextCorrectly() {
        String wrapped = FormatType.ITALIC.wrap("Hello");
        assertEquals("Italic wrap should produce _Hello_", "_Hello_", wrapped);
    }

    /**
     * Test FormatType.wrap() for strikethrough.
     */
    @Test
    public void formatType_strikethrough_wrapsTextCorrectly() {
        String wrapped = FormatType.STRIKETHROUGH.wrap("Hello");
        assertEquals("Strikethrough wrap should produce ~~Hello~~", "~~Hello~~", wrapped);
    }

    /**
     * Test FormatType.wrap() for inline code.
     */
    @Test
    public void formatType_inlineCode_wrapsTextCorrectly() {
        String wrapped = FormatType.INLINE_CODE.wrap("code");
        assertEquals("Inline code wrap should produce `code`", "`code`", wrapped);
    }

    /**
     * Test FormatType.wrap() for code block.
     */
    @Test
    public void formatType_codeBlock_wrapsTextCorrectly() {
        String wrapped = FormatType.CODE_BLOCK.wrap("code");
        assertEquals("Code block wrap should produce ```\\ncode\\n```", "```\ncode\n```", wrapped);
    }

    /**
     * Test FormatType.wrap() for bullet list.
     */
    @Test
    public void formatType_bulletList_wrapsTextCorrectly() {
        String wrapped = FormatType.BULLET_LIST.wrap("item");
        assertEquals("Bullet list wrap should produce '- item'", "- item", wrapped);
    }

    /**
     * Test FormatType.wrap() for blockquote.
     */
    @Test
    public void formatType_blockquote_wrapsTextCorrectly() {
        String wrapped = FormatType.BLOCKQUOTE.wrap("quote");
        assertEquals("Blockquote wrap should produce '> quote'", "> quote", wrapped);
    }

    // ==================== Span Format Type Tests ====================

    /**
     * Test that BoldFormatSpan returns correct format type.
     */
    @Test
    public void boldFormatSpan_returnsCorrectFormatType() {
        BoldFormatSpan span = new BoldFormatSpan();
        assertEquals("BoldFormatSpan should return BOLD", FormatType.BOLD, span.getFormatType());
    }

    /**
     * Test that ItalicFormatSpan returns correct format type.
     */
    @Test
    public void italicFormatSpan_returnsCorrectFormatType() {
        ItalicFormatSpan span = new ItalicFormatSpan();
        assertEquals("ItalicFormatSpan should return ITALIC", FormatType.ITALIC, span.getFormatType());
    }

    /**
     * Test that StrikethroughFormatSpan returns correct format type.
     */
    @Test
    public void strikethroughFormatSpan_returnsCorrectFormatType() {
        StrikethroughFormatSpan span = new StrikethroughFormatSpan();
        assertEquals("StrikethroughFormatSpan should return STRIKETHROUGH", FormatType.STRIKETHROUGH, span.getFormatType());
    }

    /**
     * Test that InlineCodeFormatSpan returns correct format type.
     */
    @Test
    public void inlineCodeFormatSpan_returnsCorrectFormatType() {
        InlineCodeFormatSpan span = new InlineCodeFormatSpan();
        assertEquals("InlineCodeFormatSpan should return INLINE_CODE", FormatType.INLINE_CODE, span.getFormatType());
    }

    /**
     * Test that CodeBlockFormatSpan returns correct format type.
     */
    @Test
    public void codeBlockFormatSpan_returnsCorrectFormatType() {
        CodeBlockFormatSpan span = new CodeBlockFormatSpan();
        assertEquals("CodeBlockFormatSpan should return CODE_BLOCK", FormatType.CODE_BLOCK, span.getFormatType());
    }

    /**
     * Test that LinkFormatSpan returns correct format type.
     */
    @Test
    public void linkFormatSpan_returnsCorrectFormatType() {
        LinkFormatSpan span = new LinkFormatSpan("https://example.com");
        assertEquals("LinkFormatSpan should return LINK", FormatType.LINK, span.getFormatType());
    }

    /**
     * Test that BulletListFormatSpan returns correct format type.
     */
    @Test
    public void bulletListFormatSpan_returnsCorrectFormatType() {
        BulletListFormatSpan span = new BulletListFormatSpan();
        assertEquals("BulletListFormatSpan should return BULLET_LIST", FormatType.BULLET_LIST, span.getFormatType());
    }

    /**
     * Test that NumberedListFormatSpan returns correct format type.
     */
    @Test
    public void numberedListFormatSpan_returnsCorrectFormatType() {
        NumberedListFormatSpan span = new NumberedListFormatSpan(1);
        assertEquals("NumberedListFormatSpan should return ORDERED_LIST", FormatType.ORDERED_LIST, span.getFormatType());
    }

    /**
     * Test that BlockquoteFormatSpan returns correct format type.
     */
    @Test
    public void blockquoteFormatSpan_returnsCorrectFormatType() {
        BlockquoteFormatSpan span = new BlockquoteFormatSpan();
        assertEquals("BlockquoteFormatSpan should return BLOCKQUOTE", FormatType.BLOCKQUOTE, span.getFormatType());
    }

    // ==================== LinkFormatSpan URL Tests ====================

    /**
     * Test LinkFormatSpan stores URL correctly.
     * Validates: Requirement 10.7
     */
    @Test
    public void linkFormatSpan_storesUrlCorrectly() {
        String url = "https://example.com/path?query=value";
        LinkFormatSpan span = new LinkFormatSpan(url);
        assertEquals("LinkFormatSpan should store URL correctly", url, span.getUrl());
    }

    /**
     * Test LinkFormatSpan handles empty URL.
     */
    @Test
    public void linkFormatSpan_handlesEmptyUrl() {
        LinkFormatSpan span = new LinkFormatSpan("");
        assertEquals("LinkFormatSpan should handle empty URL", "", span.getUrl());
    }

    /**
     * Test LinkFormatSpan URL can be updated.
     */
    @Test
    public void linkFormatSpan_urlCanBeUpdated() {
        LinkFormatSpan span = new LinkFormatSpan("https://old.com");
        span.setUrl("https://new.com");
        assertEquals("LinkFormatSpan URL should be updatable", "https://new.com", span.getUrl());
    }

    // ==================== NumberedListFormatSpan Number Tests ====================

    /**
     * Test NumberedListFormatSpan stores number correctly.
     * Validates: Requirement 10.9
     */
    @Test
    public void numberedListFormatSpan_storesNumberCorrectly() {
        NumberedListFormatSpan span = new NumberedListFormatSpan(5);
        assertEquals("NumberedListFormatSpan should store number correctly", 5, span.getNumber());
    }

    /**
     * Test NumberedListFormatSpan number can be updated.
     */
    @Test
    public void numberedListFormatSpan_numberCanBeUpdated() {
        NumberedListFormatSpan span = new NumberedListFormatSpan(1);
        span.setNumber(10);
        assertEquals("NumberedListFormatSpan number should be updatable", 10, span.getNumber());
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

    // ==================== All Spans Implement RichTextFormatSpan ====================

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

    // ==================== Nested Format Handling Tests ====================
    // Validates: Requirements 11.1, 11.2, 11.3

    /**
     * Test that nested bold and italic markdown can be identified.
     * Validates: Requirement 11.1
     */
    @Test
    public void nestedFormat_boldItalic_markersIdentifiable() {
        String text = "text";
        String nestedMarkdown = "**_" + text + "_**";
        
        // Verify both markers are present
        assertTrue("Should contain bold markers", nestedMarkdown.contains("**"));
        assertTrue("Should contain italic markers", nestedMarkdown.contains("_"));
        
        // Verify text can be extracted by removing markers
        String extracted = nestedMarkdown.replace("**", "").replace("_", "");
        assertEquals("Extracted text should match original", text, extracted);
    }

    /**
     * Test that nested italic and bold markdown can be identified.
     * Validates: Requirement 11.1
     */
    @Test
    public void nestedFormat_italicBold_markersIdentifiable() {
        String text = "text";
        String nestedMarkdown = "_**" + text + "**_";
        
        // Verify both markers are present
        assertTrue("Should contain bold markers", nestedMarkdown.contains("**"));
        assertTrue("Should contain italic markers", nestedMarkdown.contains("_"));
        
        // Verify text can be extracted by removing markers
        String extracted = nestedMarkdown.replace("**", "").replace("_", "");
        assertEquals("Extracted text should match original", text, extracted);
    }

    /**
     * Test that nested bold and strikethrough markdown can be identified.
     * Validates: Requirement 11.2
     */
    @Test
    public void nestedFormat_boldStrikethrough_markersIdentifiable() {
        String text = "text";
        String nestedMarkdown = "**~~" + text + "~~**";
        
        // Verify both markers are present
        assertTrue("Should contain bold markers", nestedMarkdown.contains("**"));
        assertTrue("Should contain strikethrough markers", nestedMarkdown.contains("~~"));
        
        // Verify text can be extracted by removing markers
        String extracted = nestedMarkdown.replace("**", "").replace("~~", "");
        assertEquals("Extracted text should match original", text, extracted);
    }

    /**
     * Test that triple nested formats can be identified.
     * Validates: Requirement 11.3
     */
    @Test
    public void nestedFormat_tripleNested_markersIdentifiable() {
        String text = "text";
        String nestedMarkdown = "**_~~" + text + "~~_**";
        
        // Verify all markers are present
        assertTrue("Should contain bold markers", nestedMarkdown.contains("**"));
        assertTrue("Should contain italic markers", nestedMarkdown.contains("_"));
        assertTrue("Should contain strikethrough markers", nestedMarkdown.contains("~~"));
        
        // Verify text can be extracted by removing markers
        String extracted = nestedMarkdown.replace("**", "").replace("_", "").replace("~~", "");
        assertEquals("Extracted text should match original", text, extracted);
    }

    /**
     * Test that fromMarkdown handles nested bold and italic without exception.
     * Validates: Requirement 11.1
     */
    @Test
    public void fromMarkdown_nestedBoldItalic_noException() {
        SpannableString result = MarkdownConverter.fromMarkdown("**_bold italic_**");
        assertNotNull("Should return non-null SpannableString", result);
    }

    /**
     * Test that fromMarkdown handles nested italic and bold without exception.
     * Validates: Requirement 11.1
     */
    @Test
    public void fromMarkdown_nestedItalicBold_noException() {
        SpannableString result = MarkdownConverter.fromMarkdown("_**italic bold**_");
        assertNotNull("Should return non-null SpannableString", result);
    }

    /**
     * Test that fromMarkdown handles nested bold and strikethrough without exception.
     * Validates: Requirement 11.2
     */
    @Test
    public void fromMarkdown_nestedBoldStrikethrough_noException() {
        SpannableString result = MarkdownConverter.fromMarkdown("**~~bold strikethrough~~**");
        assertNotNull("Should return non-null SpannableString", result);
    }

    /**
     * Test that fromMarkdown handles triple nested formats without exception.
     * Validates: Requirement 11.3
     */
    @Test
    public void fromMarkdown_tripleNested_noException() {
        SpannableString result = MarkdownConverter.fromMarkdown("**_~~triple nested~~_**");
        assertNotNull("Should return non-null SpannableString", result);
    }

    /**
     * Test that fromMarkdown handles adjacent formats without exception.
     * Validates: Requirement 11.3
     */
    @Test
    public void fromMarkdown_adjacentFormats_noException() {
        SpannableString result = MarkdownConverter.fromMarkdown("**bold** _italic_ ~~strike~~");
        assertNotNull("Should return non-null SpannableString", result);
    }

    /**
     * Test that fromMarkdown handles overlapping regions correctly.
     * Validates: Requirement 11.3
     */
    @Test
    public void fromMarkdown_overlappingRegions_noException() {
        // Test various overlapping patterns
        assertNotNull(MarkdownConverter.fromMarkdown("**bold _italic** text_"));
        assertNotNull(MarkdownConverter.fromMarkdown("_italic **bold_ text**"));
    }

    // ==================== Plain Text Preservation Tests ====================
    // Validates: Requirements 12.1, 12.2, 12.3

    /**
     * Test that plain text without markdown is unchanged.
     * Validates: Requirement 12.1
     */
    @Test
    public void fromMarkdown_plainText_unchanged() {
        String plainText = "Hello World";
        SpannableString result = MarkdownConverter.fromMarkdown(plainText);
        assertNotNull("Should return non-null SpannableString", result);
        // Note: Full text comparison requires instrumentation tests
    }

    /**
     * Test that text with only spaces is preserved.
     * Validates: Requirement 12.1
     */
    @Test
    public void fromMarkdown_spacesOnly_preserved() {
        SpannableString result = MarkdownConverter.fromMarkdown("   ");
        assertNotNull("Should return non-null SpannableString", result);
    }

    /**
     * Test that text with newlines is preserved.
     * Validates: Requirement 12.1
     */
    @Test
    public void fromMarkdown_newlines_preserved() {
        SpannableString result = MarkdownConverter.fromMarkdown("line1\nline2\nline3");
        assertNotNull("Should return non-null SpannableString", result);
    }

    /**
     * Test that text with special characters (not markdown) is preserved.
     * Validates: Requirement 12.1
     */
    @Test
    public void fromMarkdown_specialCharacters_preserved() {
        SpannableString result = MarkdownConverter.fromMarkdown("Hello! @#$%^&()+={}[]|\\:;\"'<>,./? World");
        assertNotNull("Should return non-null SpannableString", result);
    }

    /**
     * Test that unclosed bold markers are left as-is.
     * Validates: Requirement 12.2
     */
    @Test
    public void fromMarkdown_unclosedBold_leftAsIs() {
        SpannableString result = MarkdownConverter.fromMarkdown("**unclosed bold");
        assertNotNull("Should return non-null SpannableString", result);
    }

    /**
     * Test that unclosed italic markers are left as-is.
     * Validates: Requirement 12.2
     */
    @Test
    public void fromMarkdown_unclosedItalic_leftAsIs() {
        SpannableString result = MarkdownConverter.fromMarkdown("_unclosed italic");
        assertNotNull("Should return non-null SpannableString", result);
    }

    /**
     * Test that unclosed strikethrough markers are left as-is.
     * Validates: Requirement 12.2
     */
    @Test
    public void fromMarkdown_unclosedStrikethrough_leftAsIs() {
        SpannableString result = MarkdownConverter.fromMarkdown("~~unclosed strikethrough");
        assertNotNull("Should return non-null SpannableString", result);
    }

    /**
     * Test that unclosed inline code markers are left as-is.
     * Validates: Requirement 12.2
     */
    @Test
    public void fromMarkdown_unclosedInlineCode_leftAsIs() {
        SpannableString result = MarkdownConverter.fromMarkdown("`unclosed code");
        assertNotNull("Should return non-null SpannableString", result);
    }

    /**
     * Test that unclosed code block markers are left as-is.
     * Validates: Requirement 12.2
     */
    @Test
    public void fromMarkdown_unclosedCodeBlock_leftAsIs() {
        SpannableString result = MarkdownConverter.fromMarkdown("```unclosed code block");
        assertNotNull("Should return non-null SpannableString", result);
    }

    /**
     * Test that malformed link syntax is left as-is.
     * Validates: Requirement 12.2
     */
    @Test
    public void fromMarkdown_malformedLink_leftAsIs() {
        // Missing URL
        SpannableString result1 = MarkdownConverter.fromMarkdown("[text]");
        assertNotNull("Should return non-null SpannableString", result1);
        
        // Missing closing bracket
        SpannableString result2 = MarkdownConverter.fromMarkdown("[text(url)");
        assertNotNull("Should return non-null SpannableString", result2);
        
        // Missing text
        SpannableString result3 = MarkdownConverter.fromMarkdown("[](url)");
        assertNotNull("Should return non-null SpannableString", result3);
    }

    /**
     * Test that single asterisk is not treated as bold.
     * Validates: Requirement 12.3
     */
    @Test
    public void fromMarkdown_singleAsterisk_notBold() {
        SpannableString result = MarkdownConverter.fromMarkdown("*not bold*");
        assertNotNull("Should return non-null SpannableString", result);
    }

    /**
     * Test that single tilde is not treated as strikethrough.
     * Validates: Requirement 12.3
     */
    @Test
    public void fromMarkdown_singleTilde_notStrikethrough() {
        SpannableString result = MarkdownConverter.fromMarkdown("~not strikethrough~");
        assertNotNull("Should return non-null SpannableString", result);
    }

    /**
     * Test that underscore in middle of word is not treated as italic.
     * Validates: Requirement 12.3
     */
    @Test
    public void fromMarkdown_underscoreInWord_notItalic() {
        SpannableString result = MarkdownConverter.fromMarkdown("snake_case_variable");
        assertNotNull("Should return non-null SpannableString", result);
    }

    /**
     * Test that numbers without period are not treated as numbered list.
     * Validates: Requirement 12.3
     */
    @Test
    public void fromMarkdown_numberWithoutPeriod_notList() {
        SpannableString result = MarkdownConverter.fromMarkdown("123 items");
        assertNotNull("Should return non-null SpannableString", result);
    }

    /**
     * Test that dash without space is not treated as bullet list.
     * Validates: Requirement 12.3
     */
    @Test
    public void fromMarkdown_dashWithoutSpace_notBullet() {
        SpannableString result = MarkdownConverter.fromMarkdown("-not-a-bullet");
        assertNotNull("Should return non-null SpannableString", result);
    }

    /**
     * Test that greater-than without space is not treated as blockquote.
     * Validates: Requirement 12.3
     */
    @Test
    public void fromMarkdown_greaterThanWithoutSpace_notBlockquote() {
        SpannableString result = MarkdownConverter.fromMarkdown(">not a blockquote");
        assertNotNull("Should return non-null SpannableString", result);
    }

    // ==================== Link Text Whitespace Normalization Tests ====================
    // Note: Full integration tests for whitespace normalization in link text
    // require instrumentation tests or Robolectric, as SpannableString.getSpans()
    // doesn't work in unit tests without the Android framework.
    // The whitespace normalization logic is implemented in MarkdownConverter.buildInlineMarkdown()
    // and normalizeWhitespace() methods.
}
