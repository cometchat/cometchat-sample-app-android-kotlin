package com.cometchat.chatuikit.shared.spans;

import com.cometchat.chatuikit.shared.views.richtexttoolbar.FormatType;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for basic format spans (BoldFormatSpan, ItalicFormatSpan, StrikethroughFormatSpan).
 * <p>
 * These tests verify:
 * 1. Span creation without errors
 * 2. Correct format type identification
 * 3. Implementation of RichTextFormatSpan interface
 * </p>
 * <p>
 * Validates: Requirements 1.1, 1.2, 1.3
 * </p>
 */
public class BasicFormatSpanTest {

    // ==================== BoldFormatSpan Tests ====================

    /**
     * Test that BoldFormatSpan can be created without errors.
     * Validates: Requirement 1.1
     */
    @Test
    public void boldFormatSpan_canBeCreated() {
        BoldFormatSpan span = new BoldFormatSpan();
        assertNotNull("BoldFormatSpan should be created successfully", span);
    }

    /**
     * Test that BoldFormatSpan returns FormatType.BOLD.
     * Validates: Requirement 1.1
     */
    @Test
    public void boldFormatSpan_returnsCorrectFormatType() {
        BoldFormatSpan span = new BoldFormatSpan();
        assertEquals("BoldFormatSpan should return FormatType.BOLD", 
                FormatType.BOLD, span.getFormatType());
    }

    /**
     * Test that BoldFormatSpan implements RichTextFormatSpan interface.
     * Validates: Requirement 1.1
     */
    @Test
    public void boldFormatSpan_implementsRichTextFormatSpan() {
        BoldFormatSpan span = new BoldFormatSpan();
        assertTrue("BoldFormatSpan should implement RichTextFormatSpan", 
                span instanceof RichTextFormatSpan);
    }

    // ==================== ItalicFormatSpan Tests ====================

    /**
     * Test that ItalicFormatSpan can be created without errors.
     * Validates: Requirement 1.2
     */
    @Test
    public void italicFormatSpan_canBeCreated() {
        ItalicFormatSpan span = new ItalicFormatSpan();
        assertNotNull("ItalicFormatSpan should be created successfully", span);
    }

    /**
     * Test that ItalicFormatSpan returns FormatType.ITALIC.
     * Validates: Requirement 1.2
     */
    @Test
    public void italicFormatSpan_returnsCorrectFormatType() {
        ItalicFormatSpan span = new ItalicFormatSpan();
        assertEquals("ItalicFormatSpan should return FormatType.ITALIC", 
                FormatType.ITALIC, span.getFormatType());
    }

    /**
     * Test that ItalicFormatSpan implements RichTextFormatSpan interface.
     * Validates: Requirement 1.2
     */
    @Test
    public void italicFormatSpan_implementsRichTextFormatSpan() {
        ItalicFormatSpan span = new ItalicFormatSpan();
        assertTrue("ItalicFormatSpan should implement RichTextFormatSpan", 
                span instanceof RichTextFormatSpan);
    }

    // ==================== StrikethroughFormatSpan Tests ====================

    /**
     * Test that StrikethroughFormatSpan can be created without errors.
     * Validates: Requirement 1.3
     */
    @Test
    public void strikethroughFormatSpan_canBeCreated() {
        StrikethroughFormatSpan span = new StrikethroughFormatSpan();
        assertNotNull("StrikethroughFormatSpan should be created successfully", span);
    }

    /**
     * Test that StrikethroughFormatSpan returns FormatType.STRIKETHROUGH.
     * Validates: Requirement 1.3
     */
    @Test
    public void strikethroughFormatSpan_returnsCorrectFormatType() {
        StrikethroughFormatSpan span = new StrikethroughFormatSpan();
        assertEquals("StrikethroughFormatSpan should return FormatType.STRIKETHROUGH", 
                FormatType.STRIKETHROUGH, span.getFormatType());
    }

    /**
     * Test that StrikethroughFormatSpan implements RichTextFormatSpan interface.
     * Validates: Requirement 1.3
     */
    @Test
    public void strikethroughFormatSpan_implementsRichTextFormatSpan() {
        StrikethroughFormatSpan span = new StrikethroughFormatSpan();
        assertTrue("StrikethroughFormatSpan should implement RichTextFormatSpan", 
                span instanceof RichTextFormatSpan);
    }

    // ==================== Cross-Span Tests ====================

    /**
     * Test that all basic format spans return distinct format types.
     * Validates: Requirements 1.1, 1.2, 1.3
     */
    @Test
    public void allBasicSpans_returnDistinctFormatTypes() {
        BoldFormatSpan boldSpan = new BoldFormatSpan();
        ItalicFormatSpan italicSpan = new ItalicFormatSpan();
        StrikethroughFormatSpan strikethroughSpan = new StrikethroughFormatSpan();

        FormatType boldType = boldSpan.getFormatType();
        FormatType italicType = italicSpan.getFormatType();
        FormatType strikethroughType = strikethroughSpan.getFormatType();

        assertNotEquals("Bold and Italic format types should be different", 
                boldType, italicType);
        assertNotEquals("Bold and Strikethrough format types should be different", 
                boldType, strikethroughType);
        assertNotEquals("Italic and Strikethrough format types should be different", 
                italicType, strikethroughType);
    }

    /**
     * Test that all basic format spans can be cast to RichTextFormatSpan.
     * Validates: Requirements 1.1, 1.2, 1.3
     */
    @Test
    public void allBasicSpans_canBeCastToRichTextFormatSpan() {
        RichTextFormatSpan boldSpan = new BoldFormatSpan();
        RichTextFormatSpan italicSpan = new ItalicFormatSpan();
        RichTextFormatSpan strikethroughSpan = new StrikethroughFormatSpan();

        assertNotNull("BoldFormatSpan should be castable to RichTextFormatSpan", boldSpan);
        assertNotNull("ItalicFormatSpan should be castable to RichTextFormatSpan", italicSpan);
        assertNotNull("StrikethroughFormatSpan should be castable to RichTextFormatSpan", strikethroughSpan);

        // Verify format types are accessible through the interface
        assertEquals("BoldFormatSpan should return BOLD through interface", 
                FormatType.BOLD, boldSpan.getFormatType());
        assertEquals("ItalicFormatSpan should return ITALIC through interface", 
                FormatType.ITALIC, italicSpan.getFormatType());
        assertEquals("StrikethroughFormatSpan should return STRIKETHROUGH through interface", 
                FormatType.STRIKETHROUGH, strikethroughSpan.getFormatType());
    }

    /**
     * Test that multiple instances of the same span type return the same format type.
     * Validates: Requirements 1.1, 1.2, 1.3
     */
    @Test
    public void multipleInstances_returnConsistentFormatTypes() {
        // Create multiple instances of each span type
        BoldFormatSpan bold1 = new BoldFormatSpan();
        BoldFormatSpan bold2 = new BoldFormatSpan();
        
        ItalicFormatSpan italic1 = new ItalicFormatSpan();
        ItalicFormatSpan italic2 = new ItalicFormatSpan();
        
        StrikethroughFormatSpan strikethrough1 = new StrikethroughFormatSpan();
        StrikethroughFormatSpan strikethrough2 = new StrikethroughFormatSpan();

        // Verify consistency
        assertEquals("Multiple BoldFormatSpan instances should return same format type", 
                bold1.getFormatType(), bold2.getFormatType());
        assertEquals("Multiple ItalicFormatSpan instances should return same format type", 
                italic1.getFormatType(), italic2.getFormatType());
        assertEquals("Multiple StrikethroughFormatSpan instances should return same format type", 
                strikethrough1.getFormatType(), strikethrough2.getFormatType());
    }
}
