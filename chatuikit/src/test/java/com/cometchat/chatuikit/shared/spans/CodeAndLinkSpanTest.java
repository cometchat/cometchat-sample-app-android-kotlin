package com.cometchat.chatuikit.shared.spans;

import com.cometchat.chatuikit.shared.views.richtexttoolbar.FormatType;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for code and link format spans (InlineCodeFormatSpan, CodeBlockFormatSpan, LinkFormatSpan).
 * <p>
 * These tests verify:
 * 1. Span creation with default and custom constructors
 * 2. Correct format type identification
 * 3. Implementation of RichTextFormatSpan interface
 * 4. Configurable properties (padding, corner radius, border width, URL storage)
 * </p>
 * <p>
 * Validates: Requirements 4.1, 5.4
 * </p>
 */
public class CodeAndLinkSpanTest {

    // ==================== InlineCodeFormatSpan Tests ====================

    /**
     * Test that InlineCodeFormatSpan returns FormatType.INLINE_CODE.
     * Validates: Requirement 4.1
     */
    @Test
    public void inlineCodeFormatSpan_returnsCorrectFormatType() {
        InlineCodeFormatSpan span = new InlineCodeFormatSpan();
        assertEquals("InlineCodeFormatSpan should return FormatType.INLINE_CODE",
                FormatType.INLINE_CODE, span.getFormatType());
    }

    /**
     * Test that InlineCodeFormatSpan can be created with default constructor.
     * Validates: Requirement 4.1
     */
    @Test
    public void inlineCodeFormatSpan_canBeCreatedWithDefaultConstructor() {
        InlineCodeFormatSpan span = new InlineCodeFormatSpan();
        assertNotNull("InlineCodeFormatSpan should be created successfully", span);
    }

    /**
     * Test that InlineCodeFormatSpan has configurable padding.
     * Validates: Requirement 4.1
     */
    @Test
    public void inlineCodeFormatSpan_hasConfigurablePadding() {
        InlineCodeFormatSpan span = new InlineCodeFormatSpan();
        
        // Test default padding is set
        float defaultPadding = span.getPadding();
        assertTrue("Default padding should be positive", defaultPadding > 0);
        
        // Test setting custom padding
        float customPadding = 10f;
        span.setPadding(customPadding);
        assertEquals("Padding should be updated to custom value", 
                customPadding, span.getPadding(), 0.001f);
    }

    /**
     * Test that InlineCodeFormatSpan has configurable corner radius.
     * Validates: Requirement 4.1
     */
    @Test
    public void inlineCodeFormatSpan_hasConfigurableCornerRadius() {
        InlineCodeFormatSpan span = new InlineCodeFormatSpan();
        
        // Test default corner radius is set
        float defaultCornerRadius = span.getCornerRadius();
        assertTrue("Default corner radius should be positive", defaultCornerRadius > 0);
        
        // Test setting custom corner radius
        float customCornerRadius = 12f;
        span.setCornerRadius(customCornerRadius);
        assertEquals("Corner radius should be updated to custom value", 
                customCornerRadius, span.getCornerRadius(), 0.001f);
    }

    /**
     * Test that InlineCodeFormatSpan implements RichTextFormatSpan interface.
     * Validates: Requirement 4.1
     */
    @Test
    public void inlineCodeFormatSpan_implementsRichTextFormatSpan() {
        InlineCodeFormatSpan span = new InlineCodeFormatSpan();
        assertTrue("InlineCodeFormatSpan should implement RichTextFormatSpan",
                span instanceof RichTextFormatSpan);
    }

    /**
     * Test that InlineCodeFormatSpan can be created with custom styling.
     * Validates: Requirement 4.1
     */
    @Test
    public void inlineCodeFormatSpan_canBeCreatedWithCustomStyling() {
        int backgroundColor = 0xFF000000;
        int textColor = 0xFFFFFFFF;
        float cornerRadius = 8f;
        float padding = 6f;
        
        InlineCodeFormatSpan span = new InlineCodeFormatSpan(backgroundColor, textColor, cornerRadius, padding);
        
        assertNotNull("InlineCodeFormatSpan should be created with custom styling", span);
        assertEquals("Background color should match", backgroundColor, span.getBackgroundColor());
        assertEquals("Text color should match", textColor, span.getTextColor());
        assertEquals("Corner radius should match", cornerRadius, span.getCornerRadius(), 0.001f);
        assertEquals("Padding should match", padding, span.getPadding(), 0.001f);
    }

    /**
     * Test that InlineCodeFormatSpan has configurable background alpha.
     * Validates: Requirement 4.1
     */
    @Test
    public void inlineCodeFormatSpan_hasConfigurableBackgroundAlpha() {
        InlineCodeFormatSpan span = new InlineCodeFormatSpan();
        
        // Test default background alpha is set
        int defaultAlpha = span.getBackgroundAlpha();
        assertTrue("Default background alpha should be positive", defaultAlpha > 0);
        assertTrue("Default background alpha should be <= 255", defaultAlpha <= 255);
        
        // Test setting custom background alpha
        int customAlpha = 100;
        span.setBackgroundAlpha(customAlpha);
        assertEquals("Background alpha should be updated to custom value", 
                customAlpha, span.getBackgroundAlpha());
    }

    // ==================== CodeBlockFormatSpan Tests ====================

    /**
     * Test that CodeBlockFormatSpan returns FormatType.CODE_BLOCK.
     * Validates: Requirement 4.1
     */
    @Test
    public void codeBlockFormatSpan_returnsCorrectFormatType() {
        CodeBlockFormatSpan span = new CodeBlockFormatSpan();
        assertEquals("CodeBlockFormatSpan should return FormatType.CODE_BLOCK",
                FormatType.CODE_BLOCK, span.getFormatType());
    }

    /**
     * Test that CodeBlockFormatSpan can be created with default constructor.
     * Validates: Requirement 4.1
     */
    @Test
    public void codeBlockFormatSpan_canBeCreatedWithDefaultConstructor() {
        CodeBlockFormatSpan span = new CodeBlockFormatSpan();
        assertNotNull("CodeBlockFormatSpan should be created successfully", span);
    }

    /**
     * Test that CodeBlockFormatSpan has configurable border width.
     * Validates: Requirement 4.1
     */
    @Test
    public void codeBlockFormatSpan_hasConfigurableBorderWidth() {
        CodeBlockFormatSpan span = new CodeBlockFormatSpan();
        
        // Test default border width is set
        float defaultBorderWidth = span.getBorderWidth();
        assertTrue("Default border width should be positive", defaultBorderWidth > 0);
        
        // Test setting custom border width
        float customBorderWidth = 3f;
        span.setBorderWidth(customBorderWidth);
        assertEquals("Border width should be updated to custom value", 
                customBorderWidth, span.getBorderWidth(), 0.001f);
    }

    /**
     * Test that CodeBlockFormatSpan has configurable corner radius.
     * Validates: Requirement 4.1
     */
    @Test
    public void codeBlockFormatSpan_hasConfigurableCornerRadius() {
        CodeBlockFormatSpan span = new CodeBlockFormatSpan();
        
        // Test default corner radius is set
        float defaultCornerRadius = span.getCornerRadius();
        assertTrue("Default corner radius should be positive", defaultCornerRadius > 0);
        
        // Test setting custom corner radius
        float customCornerRadius = 16f;
        span.setCornerRadius(customCornerRadius);
        assertEquals("Corner radius should be updated to custom value", 
                customCornerRadius, span.getCornerRadius(), 0.001f);
    }

    /**
     * Test that CodeBlockFormatSpan implements RichTextFormatSpan interface.
     * Validates: Requirement 4.1
     */
    @Test
    public void codeBlockFormatSpan_implementsRichTextFormatSpan() {
        CodeBlockFormatSpan span = new CodeBlockFormatSpan();
        assertTrue("CodeBlockFormatSpan should implement RichTextFormatSpan",
                span instanceof RichTextFormatSpan);
    }

    /**
     * Test that CodeBlockFormatSpan can be created with custom styling.
     * Validates: Requirement 4.1
     */
    @Test
    public void codeBlockFormatSpan_canBeCreatedWithCustomStyling() {
        int backgroundColor = 0xFF111111;
        int borderColor = 0xFF222222;
        float borderWidth = 2f;
        float cornerRadius = 10f;
        
        CodeBlockFormatSpan span = new CodeBlockFormatSpan(backgroundColor, borderColor, borderWidth, cornerRadius);
        
        assertNotNull("CodeBlockFormatSpan should be created with custom styling", span);
        assertEquals("Background color should match", backgroundColor, span.getBackgroundColor());
        assertEquals("Border color should match", borderColor, span.getBorderColor());
        assertEquals("Border width should match", borderWidth, span.getBorderWidth(), 0.001f);
        assertEquals("Corner radius should match", cornerRadius, span.getCornerRadius(), 0.001f);
    }

    /**
     * Test that CodeBlockFormatSpan has configurable padding.
     * Validates: Requirement 4.1
     */
    @Test
    public void codeBlockFormatSpan_hasConfigurablePadding() {
        CodeBlockFormatSpan span = new CodeBlockFormatSpan();
        
        // Test default padding is set
        float defaultPadding = span.getPadding();
        assertTrue("Default padding should be positive", defaultPadding > 0);
        
        // Test setting custom padding
        float customPadding = 12f;
        span.setPadding(customPadding);
        assertEquals("Padding should be updated to custom value", 
                customPadding, span.getPadding(), 0.001f);
    }

    /**
     * Test that CodeBlockFormatSpan can be created and used as a marker span.
     * Validates: Requirement 4.1
     */
    @Test
    public void codeBlockFormatSpan_canBeCreatedAsMarkerSpan() {
        CodeBlockFormatSpan span = new CodeBlockFormatSpan();
        
        // Verify span was created successfully and has correct format type
        assertNotNull("CodeBlockFormatSpan should be created successfully", span);
        assertEquals("CodeBlockFormatSpan should return FormatType.CODE_BLOCK",
                FormatType.CODE_BLOCK, span.getFormatType());
    }

    // ==================== LinkFormatSpan Tests ====================

    /**
     * Test that LinkFormatSpan returns FormatType.LINK.
     * Validates: Requirement 5.4
     */
    @Test
    public void linkFormatSpan_returnsCorrectFormatType() {
        LinkFormatSpan span = new LinkFormatSpan("https://example.com");
        assertEquals("LinkFormatSpan should return FormatType.LINK",
                FormatType.LINK, span.getFormatType());
    }

    /**
     * Test that LinkFormatSpan stores and retrieves URL correctly.
     * Validates: Requirement 5.4
     */
    @Test
    public void linkFormatSpan_storesAndRetrievesUrlCorrectly() {
        String testUrl = "https://example.com/path?query=value";
        LinkFormatSpan span = new LinkFormatSpan(testUrl);
        
        assertEquals("LinkFormatSpan should store and retrieve URL correctly",
                testUrl, span.getUrl());
    }

    /**
     * Test that LinkFormatSpan can be created with URL parameter.
     * Validates: Requirement 5.4
     */
    @Test
    public void linkFormatSpan_canBeCreatedWithUrlParameter() {
        String url = "https://cometchat.com";
        LinkFormatSpan span = new LinkFormatSpan(url);
        
        assertNotNull("LinkFormatSpan should be created with URL parameter", span);
        assertEquals("URL should be stored correctly", url, span.getUrl());
    }

    /**
     * Test that LinkFormatSpan implements RichTextFormatSpan interface.
     * Validates: Requirement 5.4
     */
    @Test
    public void linkFormatSpan_implementsRichTextFormatSpan() {
        LinkFormatSpan span = new LinkFormatSpan("https://example.com");
        assertTrue("LinkFormatSpan should implement RichTextFormatSpan",
                span instanceof RichTextFormatSpan);
    }

    /**
     * Test that LinkFormatSpan URL can be updated.
     * Validates: Requirement 5.4
     */
    @Test
    public void linkFormatSpan_urlCanBeUpdated() {
        String initialUrl = "https://initial.com";
        String updatedUrl = "https://updated.com";
        
        LinkFormatSpan span = new LinkFormatSpan(initialUrl);
        assertEquals("Initial URL should be stored", initialUrl, span.getUrl());
        
        span.setUrl(updatedUrl);
        assertEquals("URL should be updated", updatedUrl, span.getUrl());
    }

    /**
     * Test that LinkFormatSpan handles empty URL.
     * Validates: Requirement 5.4
     */
    @Test
    public void linkFormatSpan_handlesEmptyUrl() {
        LinkFormatSpan span = new LinkFormatSpan("");
        
        assertNotNull("LinkFormatSpan should handle empty URL", span);
        assertEquals("Empty URL should be stored as empty string", "", span.getUrl());
    }

    /**
     * Test that LinkFormatSpan can be created with custom styling.
     * Validates: Requirement 5.4
     */
    @Test
    public void linkFormatSpan_canBeCreatedWithCustomStyling() {
        String url = "https://example.com";
        int linkColor = 0xFF0000FF;
        boolean underlineEnabled = false;
        
        LinkFormatSpan span = new LinkFormatSpan(url, linkColor, underlineEnabled);
        
        assertNotNull("LinkFormatSpan should be created with custom styling", span);
        assertEquals("URL should match", url, span.getUrl());
        assertEquals("Link color should match", linkColor, span.getLinkColor());
        assertFalse("Underline should be disabled", span.isUnderlineEnabled());
    }

    /**
     * Test that LinkFormatSpan has configurable underline.
     * Validates: Requirement 5.4
     */
    @Test
    public void linkFormatSpan_hasConfigurableUnderline() {
        LinkFormatSpan span = new LinkFormatSpan("https://example.com");
        
        // Test default underline is enabled
        assertTrue("Default underline should be enabled", span.isUnderlineEnabled());
        
        // Test disabling underline
        span.setUnderlineEnabled(false);
        assertFalse("Underline should be disabled", span.isUnderlineEnabled());
        
        // Test re-enabling underline
        span.setUnderlineEnabled(true);
        assertTrue("Underline should be re-enabled", span.isUnderlineEnabled());
    }

    /**
     * Test that LinkFormatSpan has configurable link color.
     * Validates: Requirement 5.4
     */
    @Test
    public void linkFormatSpan_hasConfigurableLinkColor() {
        LinkFormatSpan span = new LinkFormatSpan("https://example.com");
        
        // Test setting custom link color
        int customColor = 0xFFFF0000;
        span.setLinkColor(customColor);
        assertEquals("Link color should be updated to custom value", 
                customColor, span.getLinkColor());
    }

    // ==================== Cross-Span Tests ====================

    /**
     * Test that all code and link spans return distinct format types.
     * Validates: Requirements 4.1, 5.4
     */
    @Test
    public void allCodeAndLinkSpans_returnDistinctFormatTypes() {
        InlineCodeFormatSpan inlineCodeSpan = new InlineCodeFormatSpan();
        CodeBlockFormatSpan codeBlockSpan = new CodeBlockFormatSpan();
        LinkFormatSpan linkSpan = new LinkFormatSpan("https://example.com");

        FormatType inlineCodeType = inlineCodeSpan.getFormatType();
        FormatType codeBlockType = codeBlockSpan.getFormatType();
        FormatType linkType = linkSpan.getFormatType();

        assertNotEquals("Inline code and code block format types should be different",
                inlineCodeType, codeBlockType);
        assertNotEquals("Inline code and link format types should be different",
                inlineCodeType, linkType);
        assertNotEquals("Code block and link format types should be different",
                codeBlockType, linkType);
    }

    /**
     * Test that all code and link spans can be cast to RichTextFormatSpan.
     * Validates: Requirements 4.1, 5.4
     */
    @Test
    public void allCodeAndLinkSpans_canBeCastToRichTextFormatSpan() {
        RichTextFormatSpan inlineCodeSpan = new InlineCodeFormatSpan();
        RichTextFormatSpan codeBlockSpan = new CodeBlockFormatSpan();
        RichTextFormatSpan linkSpan = new LinkFormatSpan("https://example.com");

        assertNotNull("InlineCodeFormatSpan should be castable to RichTextFormatSpan", inlineCodeSpan);
        assertNotNull("CodeBlockFormatSpan should be castable to RichTextFormatSpan", codeBlockSpan);
        assertNotNull("LinkFormatSpan should be castable to RichTextFormatSpan", linkSpan);

        // Verify format types are accessible through the interface
        assertEquals("InlineCodeFormatSpan should return INLINE_CODE through interface",
                FormatType.INLINE_CODE, inlineCodeSpan.getFormatType());
        assertEquals("CodeBlockFormatSpan should return CODE_BLOCK through interface",
                FormatType.CODE_BLOCK, codeBlockSpan.getFormatType());
        assertEquals("LinkFormatSpan should return LINK through interface",
                FormatType.LINK, linkSpan.getFormatType());
    }

    /**
     * Test that multiple instances of the same span type return the same format type.
     * Validates: Requirements 4.1, 5.4
     */
    @Test
    public void multipleInstances_returnConsistentFormatTypes() {
        // Create multiple instances of each span type
        InlineCodeFormatSpan inlineCode1 = new InlineCodeFormatSpan();
        InlineCodeFormatSpan inlineCode2 = new InlineCodeFormatSpan();

        CodeBlockFormatSpan codeBlock1 = new CodeBlockFormatSpan();
        CodeBlockFormatSpan codeBlock2 = new CodeBlockFormatSpan();

        LinkFormatSpan link1 = new LinkFormatSpan("https://example1.com");
        LinkFormatSpan link2 = new LinkFormatSpan("https://example2.com");

        // Verify consistency
        assertEquals("Multiple InlineCodeFormatSpan instances should return same format type",
                inlineCode1.getFormatType(), inlineCode2.getFormatType());
        assertEquals("Multiple CodeBlockFormatSpan instances should return same format type",
                codeBlock1.getFormatType(), codeBlock2.getFormatType());
        assertEquals("Multiple LinkFormatSpan instances should return same format type",
                link1.getFormatType(), link2.getFormatType());
    }

    /**
     * Test that LinkFormatSpan instances with different URLs return the same format type.
     * Validates: Requirement 5.4
     */
    @Test
    public void linkFormatSpan_differentUrlsReturnSameFormatType() {
        LinkFormatSpan span1 = new LinkFormatSpan("https://example1.com");
        LinkFormatSpan span2 = new LinkFormatSpan("https://example2.com/path");
        LinkFormatSpan span3 = new LinkFormatSpan("");

        assertEquals("LinkFormatSpan with different URLs should return same format type",
                span1.getFormatType(), span2.getFormatType());
        assertEquals("LinkFormatSpan with empty URL should return same format type",
                span1.getFormatType(), span3.getFormatType());
    }
}
