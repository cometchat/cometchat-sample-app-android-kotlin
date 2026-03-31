package com.cometchat.chatuikit.shared.spans;

import com.cometchat.chatuikit.shared.views.richtexttoolbar.FormatType;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for list and blockquote format spans (BulletListFormatSpan, NumberedListFormatSpan, BlockquoteFormatSpan).
 * <p>
 * These tests verify:
 * 1. Span creation with default and custom constructors
 * 2. Correct format type identification
 * 3. Implementation of RichTextFormatSpan interface
 * 4. Configurable properties (bullet radius, gap width, stripe width, number storage)
 * </p>
 * <p>
 * Validates: Requirements 6.1, 7.1, 8.1
 * </p>
 */
public class ListAndBlockquoteSpanTest {

    // ==================== BulletListFormatSpan Tests ====================

    /**
     * Test that BulletListFormatSpan returns FormatType.BULLET_LIST.
     * Validates: Requirement 6.1
     */
    @Test
    public void bulletListFormatSpan_returnsCorrectFormatType() {
        BulletListFormatSpan span = new BulletListFormatSpan();
        assertEquals("BulletListFormatSpan should return FormatType.BULLET_LIST",
                FormatType.BULLET_LIST, span.getFormatType());
    }

    /**
     * Test that BulletListFormatSpan can be created with default constructor.
     * Validates: Requirement 6.1
     */
    @Test
    public void bulletListFormatSpan_canBeCreatedWithDefaultConstructor() {
        BulletListFormatSpan span = new BulletListFormatSpan();
        assertNotNull("BulletListFormatSpan should be created successfully", span);
    }

    /**
     * Test that BulletListFormatSpan has configurable bullet radius.
     * Validates: Requirement 6.1
     */
    @Test
    public void bulletListFormatSpan_hasConfigurableBulletRadius() {
        BulletListFormatSpan span = new BulletListFormatSpan();
        
        // Test default bullet radius is set
        int defaultBulletRadius = span.getBulletRadius();
        assertTrue("Default bullet radius should be positive", defaultBulletRadius > 0);
        
        // Test setting custom bullet radius
        int customBulletRadius = 8;
        span.setBulletRadius(customBulletRadius);
        assertEquals("Bullet radius should be updated to custom value", 
                customBulletRadius, span.getBulletRadius());
    }

    /**
     * Test that BulletListFormatSpan has configurable gap width.
     * Validates: Requirement 6.1
     */
    @Test
    public void bulletListFormatSpan_hasConfigurableGapWidth() {
        BulletListFormatSpan span = new BulletListFormatSpan();
        
        // Test default gap width is set
        int defaultGapWidth = span.getGapWidth();
        assertTrue("Default gap width should be positive", defaultGapWidth > 0);
        
        // Test setting custom gap width
        int customGapWidth = 24;
        span.setGapWidth(customGapWidth);
        assertEquals("Gap width should be updated to custom value", 
                customGapWidth, span.getGapWidth());
    }

    /**
     * Test that BulletListFormatSpan implements RichTextFormatSpan interface.
     * Validates: Requirement 6.1
     */
    @Test
    public void bulletListFormatSpan_implementsRichTextFormatSpan() {
        BulletListFormatSpan span = new BulletListFormatSpan();
        assertTrue("BulletListFormatSpan should implement RichTextFormatSpan",
                span instanceof RichTextFormatSpan);
    }

    /**
     * Test that BulletListFormatSpan can be created with custom styling.
     * Validates: Requirement 6.1
     */
    @Test
    public void bulletListFormatSpan_canBeCreatedWithCustomStyling() {
        int bulletColor = 0xFF000000;
        int bulletRadius = 6;
        int gapWidth = 20;
        
        BulletListFormatSpan span = new BulletListFormatSpan(bulletColor, bulletRadius, gapWidth);
        
        assertNotNull("BulletListFormatSpan should be created with custom styling", span);
        assertEquals("Bullet color should match", bulletColor, span.getBulletColor());
        assertEquals("Bullet radius should match", bulletRadius, span.getBulletRadius());
        assertEquals("Gap width should match", gapWidth, span.getGapWidth());
    }

    /**
     * Test that BulletListFormatSpan can be created with custom leading margin.
     * Validates: Requirement 6.1
     */
    @Test
    public void bulletListFormatSpan_canBeCreatedWithCustomLeadingMargin() {
        int leadingMargin = 48;
        
        BulletListFormatSpan span = new BulletListFormatSpan(leadingMargin);
        
        assertNotNull("BulletListFormatSpan should be created with custom leading margin", span);
    }

    /**
     * Test that BulletListFormatSpan has configurable bullet color.
     * Validates: Requirement 6.1
     */
    @Test
    public void bulletListFormatSpan_hasConfigurableBulletColor() {
        BulletListFormatSpan span = new BulletListFormatSpan();
        
        // Test setting custom bullet color
        int customColor = 0xFFFF0000;
        span.setBulletColor(customColor);
        assertEquals("Bullet color should be updated to custom value", 
                customColor, span.getBulletColor());
    }

    // ==================== NumberedListFormatSpan Tests ====================

    /**
     * Test that NumberedListFormatSpan returns FormatType.ORDERED_LIST.
     * Validates: Requirement 7.1
     */
    @Test
    public void numberedListFormatSpan_returnsCorrectFormatType() {
        NumberedListFormatSpan span = new NumberedListFormatSpan(1);
        assertEquals("NumberedListFormatSpan should return FormatType.ORDERED_LIST",
                FormatType.ORDERED_LIST, span.getFormatType());
    }

    /**
     * Test that NumberedListFormatSpan stores and retrieves number correctly.
     * Validates: Requirement 7.1
     */
    @Test
    public void numberedListFormatSpan_storesAndRetrievesNumberCorrectly() {
        int testNumber = 5;
        NumberedListFormatSpan span = new NumberedListFormatSpan(testNumber);
        
        assertEquals("NumberedListFormatSpan should store and retrieve number correctly",
                testNumber, span.getNumber());
    }

    /**
     * Test that NumberedListFormatSpan number can be updated.
     * Validates: Requirement 7.1
     */
    @Test
    public void numberedListFormatSpan_numberCanBeUpdated() {
        int initialNumber = 1;
        int updatedNumber = 10;
        
        NumberedListFormatSpan span = new NumberedListFormatSpan(initialNumber);
        assertEquals("Initial number should be stored", initialNumber, span.getNumber());
        
        span.setNumber(updatedNumber);
        assertEquals("Number should be updated", updatedNumber, span.getNumber());
    }

    /**
     * Test that NumberedListFormatSpan can be created with number parameter.
     * Validates: Requirement 7.1
     */
    @Test
    public void numberedListFormatSpan_canBeCreatedWithNumberParameter() {
        int number = 3;
        NumberedListFormatSpan span = new NumberedListFormatSpan(number);
        
        assertNotNull("NumberedListFormatSpan should be created with number parameter", span);
        assertEquals("Number should be stored correctly", number, span.getNumber());
    }

    /**
     * Test that NumberedListFormatSpan implements RichTextFormatSpan interface.
     * Validates: Requirement 7.1
     */
    @Test
    public void numberedListFormatSpan_implementsRichTextFormatSpan() {
        NumberedListFormatSpan span = new NumberedListFormatSpan(1);
        assertTrue("NumberedListFormatSpan should implement RichTextFormatSpan",
                span instanceof RichTextFormatSpan);
    }

    /**
     * Test that NumberedListFormatSpan can be created with custom styling.
     * Validates: Requirement 7.1
     */
    @Test
    public void numberedListFormatSpan_canBeCreatedWithCustomStyling() {
        int number = 2;
        int textColor = 0xFF0000FF;
        int gapWidth = 20;
        
        NumberedListFormatSpan span = new NumberedListFormatSpan(number, textColor, gapWidth);
        
        assertNotNull("NumberedListFormatSpan should be created with custom styling", span);
        assertEquals("Number should match", number, span.getNumber());
        assertEquals("Text color should match", textColor, span.getTextColor());
        assertEquals("Gap width should match", gapWidth, span.getGapWidth());
    }

    /**
     * Test that NumberedListFormatSpan can be created with custom leading margin.
     * Validates: Requirement 7.1
     */
    @Test
    public void numberedListFormatSpan_canBeCreatedWithCustomLeadingMargin() {
        int number = 1;
        int leadingMargin = 64;
        
        NumberedListFormatSpan span = new NumberedListFormatSpan(number, leadingMargin);
        
        assertNotNull("NumberedListFormatSpan should be created with custom leading margin", span);
        assertEquals("Number should be stored correctly", number, span.getNumber());
    }

    /**
     * Test that NumberedListFormatSpan has configurable text color.
     * Validates: Requirement 7.1
     */
    @Test
    public void numberedListFormatSpan_hasConfigurableTextColor() {
        NumberedListFormatSpan span = new NumberedListFormatSpan(1);
        
        // Test setting custom text color
        int customColor = 0xFF00FF00;
        span.setTextColor(customColor);
        assertEquals("Text color should be updated to custom value", 
                customColor, span.getTextColor());
    }

    /**
     * Test that NumberedListFormatSpan has configurable gap width.
     * Validates: Requirement 7.1
     */
    @Test
    public void numberedListFormatSpan_hasConfigurableGapWidth() {
        NumberedListFormatSpan span = new NumberedListFormatSpan(1);
        
        // Test default gap width is set
        int defaultGapWidth = span.getGapWidth();
        assertTrue("Default gap width should be positive", defaultGapWidth > 0);
        
        // Test setting custom gap width
        int customGapWidth = 32;
        span.setGapWidth(customGapWidth);
        assertEquals("Gap width should be updated to custom value", 
                customGapWidth, span.getGapWidth());
    }

    /**
     * Test that NumberedListFormatSpan handles various number values.
     * Validates: Requirement 7.1
     */
    @Test
    public void numberedListFormatSpan_handlesVariousNumberValues() {
        // Test with number 1
        NumberedListFormatSpan span1 = new NumberedListFormatSpan(1);
        assertEquals("Should handle number 1", 1, span1.getNumber());
        
        // Test with larger number
        NumberedListFormatSpan span100 = new NumberedListFormatSpan(100);
        assertEquals("Should handle number 100", 100, span100.getNumber());
        
        // Test with zero
        NumberedListFormatSpan span0 = new NumberedListFormatSpan(0);
        assertEquals("Should handle number 0", 0, span0.getNumber());
    }

    // ==================== BlockquoteFormatSpan Tests ====================

    /**
     * Test that BlockquoteFormatSpan returns FormatType.BLOCKQUOTE.
     * Validates: Requirement 8.1
     */
    @Test
    public void blockquoteFormatSpan_returnsCorrectFormatType() {
        BlockquoteFormatSpan span = new BlockquoteFormatSpan();
        assertEquals("BlockquoteFormatSpan should return FormatType.BLOCKQUOTE",
                FormatType.BLOCKQUOTE, span.getFormatType());
    }

    /**
     * Test that BlockquoteFormatSpan can be created with default constructor.
     * Validates: Requirement 8.1
     */
    @Test
    public void blockquoteFormatSpan_canBeCreatedWithDefaultConstructor() {
        BlockquoteFormatSpan span = new BlockquoteFormatSpan();
        assertNotNull("BlockquoteFormatSpan should be created successfully", span);
    }

    /**
     * Test that BlockquoteFormatSpan has configurable stripe width.
     * Validates: Requirement 8.1
     */
    @Test
    public void blockquoteFormatSpan_hasConfigurableStripeWidth() {
        BlockquoteFormatSpan span = new BlockquoteFormatSpan();
        
        // Test default stripe width is set
        int defaultStripeWidth = span.getStripeWidth();
        assertTrue("Default stripe width should be positive", defaultStripeWidth > 0);
        
        // Test setting custom stripe width
        int customStripeWidth = 6;
        span.setStripeWidth(customStripeWidth);
        assertEquals("Stripe width should be updated to custom value", 
                customStripeWidth, span.getStripeWidth());
    }

    /**
     * Test that BlockquoteFormatSpan has configurable gap width.
     * Validates: Requirement 8.1
     */
    @Test
    public void blockquoteFormatSpan_hasConfigurableGapWidth() {
        BlockquoteFormatSpan span = new BlockquoteFormatSpan();
        
        // Test default gap width is set
        int defaultGapWidth = span.getGapWidth();
        assertTrue("Default gap width should be positive", defaultGapWidth > 0);
        
        // Test setting custom gap width
        int customGapWidth = 24;
        span.setGapWidth(customGapWidth);
        assertEquals("Gap width should be updated to custom value", 
                customGapWidth, span.getGapWidth());
    }

    /**
     * Test that BlockquoteFormatSpan implements RichTextFormatSpan interface.
     * Validates: Requirement 8.1
     */
    @Test
    public void blockquoteFormatSpan_implementsRichTextFormatSpan() {
        BlockquoteFormatSpan span = new BlockquoteFormatSpan();
        assertTrue("BlockquoteFormatSpan should implement RichTextFormatSpan",
                span instanceof RichTextFormatSpan);
    }

    /**
     * Test that BlockquoteFormatSpan can be created with custom styling.
     * Validates: Requirement 8.1
     */
    @Test
    public void blockquoteFormatSpan_canBeCreatedWithCustomStyling() {
        int stripeColor = 0xFF888888;
        int stripeWidth = 5;
        int gapWidth = 18;
        
        BlockquoteFormatSpan span = new BlockquoteFormatSpan(stripeColor, stripeWidth, gapWidth);
        
        assertNotNull("BlockquoteFormatSpan should be created with custom styling", span);
        assertEquals("Stripe color should match", stripeColor, span.getStripeColor());
        assertEquals("Stripe width should match", stripeWidth, span.getStripeWidth());
        assertEquals("Gap width should match", gapWidth, span.getGapWidth());
    }

    /**
     * Test that BlockquoteFormatSpan can be created with custom leading margin.
     * Validates: Requirement 8.1
     */
    @Test
    public void blockquoteFormatSpan_canBeCreatedWithCustomLeadingMargin() {
        int leadingMargin = 40;
        
        BlockquoteFormatSpan span = new BlockquoteFormatSpan(leadingMargin);
        
        assertNotNull("BlockquoteFormatSpan should be created with custom leading margin", span);
    }

    /**
     * Test that BlockquoteFormatSpan has configurable stripe color.
     * Validates: Requirement 8.1
     */
    @Test
    public void blockquoteFormatSpan_hasConfigurableStripeColor() {
        BlockquoteFormatSpan span = new BlockquoteFormatSpan();
        
        // Test setting custom stripe color
        int customColor = 0xFF999999;
        span.setStripeColor(customColor);
        assertEquals("Stripe color should be updated to custom value", 
                customColor, span.getStripeColor());
    }

    // ==================== Cross-Span Tests ====================

    /**
     * Test that all list and blockquote spans return distinct format types.
     * Validates: Requirements 6.1, 7.1, 8.1
     */
    @Test
    public void allListAndBlockquoteSpans_returnDistinctFormatTypes() {
        BulletListFormatSpan bulletSpan = new BulletListFormatSpan();
        NumberedListFormatSpan numberedSpan = new NumberedListFormatSpan(1);
        BlockquoteFormatSpan blockquoteSpan = new BlockquoteFormatSpan();

        FormatType bulletType = bulletSpan.getFormatType();
        FormatType numberedType = numberedSpan.getFormatType();
        FormatType blockquoteType = blockquoteSpan.getFormatType();

        assertNotEquals("Bullet list and numbered list format types should be different",
                bulletType, numberedType);
        assertNotEquals("Bullet list and blockquote format types should be different",
                bulletType, blockquoteType);
        assertNotEquals("Numbered list and blockquote format types should be different",
                numberedType, blockquoteType);
    }

    /**
     * Test that all list and blockquote spans can be cast to RichTextFormatSpan.
     * Validates: Requirements 6.1, 7.1, 8.1
     */
    @Test
    public void allListAndBlockquoteSpans_canBeCastToRichTextFormatSpan() {
        RichTextFormatSpan bulletSpan = new BulletListFormatSpan();
        RichTextFormatSpan numberedSpan = new NumberedListFormatSpan(1);
        RichTextFormatSpan blockquoteSpan = new BlockquoteFormatSpan();

        assertNotNull("BulletListFormatSpan should be castable to RichTextFormatSpan", bulletSpan);
        assertNotNull("NumberedListFormatSpan should be castable to RichTextFormatSpan", numberedSpan);
        assertNotNull("BlockquoteFormatSpan should be castable to RichTextFormatSpan", blockquoteSpan);

        // Verify format types are accessible through the interface
        assertEquals("BulletListFormatSpan should return BULLET_LIST through interface",
                FormatType.BULLET_LIST, bulletSpan.getFormatType());
        assertEquals("NumberedListFormatSpan should return ORDERED_LIST through interface",
                FormatType.ORDERED_LIST, numberedSpan.getFormatType());
        assertEquals("BlockquoteFormatSpan should return BLOCKQUOTE through interface",
                FormatType.BLOCKQUOTE, blockquoteSpan.getFormatType());
    }

    /**
     * Test that multiple instances of the same span type return the same format type.
     * Validates: Requirements 6.1, 7.1, 8.1
     */
    @Test
    public void multipleInstances_returnConsistentFormatTypes() {
        // Create multiple instances of each span type
        BulletListFormatSpan bullet1 = new BulletListFormatSpan();
        BulletListFormatSpan bullet2 = new BulletListFormatSpan();

        NumberedListFormatSpan numbered1 = new NumberedListFormatSpan(1);
        NumberedListFormatSpan numbered2 = new NumberedListFormatSpan(5);

        BlockquoteFormatSpan blockquote1 = new BlockquoteFormatSpan();
        BlockquoteFormatSpan blockquote2 = new BlockquoteFormatSpan();

        // Verify consistency
        assertEquals("Multiple BulletListFormatSpan instances should return same format type",
                bullet1.getFormatType(), bullet2.getFormatType());
        assertEquals("Multiple NumberedListFormatSpan instances should return same format type",
                numbered1.getFormatType(), numbered2.getFormatType());
        assertEquals("Multiple BlockquoteFormatSpan instances should return same format type",
                blockquote1.getFormatType(), blockquote2.getFormatType());
    }

    /**
     * Test that NumberedListFormatSpan instances with different numbers return the same format type.
     * Validates: Requirement 7.1
     */
    @Test
    public void numberedListFormatSpan_differentNumbersReturnSameFormatType() {
        NumberedListFormatSpan span1 = new NumberedListFormatSpan(1);
        NumberedListFormatSpan span2 = new NumberedListFormatSpan(10);
        NumberedListFormatSpan span3 = new NumberedListFormatSpan(100);

        assertEquals("NumberedListFormatSpan with different numbers should return same format type",
                span1.getFormatType(), span2.getFormatType());
        assertEquals("NumberedListFormatSpan with different numbers should return same format type",
                span1.getFormatType(), span3.getFormatType());
    }

    /**
     * Test that all spans implement RichTextFormatSpan interface correctly.
     * Validates: Requirements 6.1, 7.1, 8.1
     */
    @Test
    public void allSpans_implementRichTextFormatSpanInterface() {
        // Create instances
        BulletListFormatSpan bulletSpan = new BulletListFormatSpan();
        NumberedListFormatSpan numberedSpan = new NumberedListFormatSpan(1);
        BlockquoteFormatSpan blockquoteSpan = new BlockquoteFormatSpan();

        // Verify interface implementation
        assertTrue("BulletListFormatSpan should implement RichTextFormatSpan",
                bulletSpan instanceof RichTextFormatSpan);
        assertTrue("NumberedListFormatSpan should implement RichTextFormatSpan",
                numberedSpan instanceof RichTextFormatSpan);
        assertTrue("BlockquoteFormatSpan should implement RichTextFormatSpan",
                blockquoteSpan instanceof RichTextFormatSpan);

        // Verify getFormatType() returns non-null values
        assertNotNull("BulletListFormatSpan.getFormatType() should not return null",
                bulletSpan.getFormatType());
        assertNotNull("NumberedListFormatSpan.getFormatType() should not return null",
                numberedSpan.getFormatType());
        assertNotNull("BlockquoteFormatSpan.getFormatType() should not return null",
                blockquoteSpan.getFormatType());
    }
}
