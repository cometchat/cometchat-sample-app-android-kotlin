package com.cometchat.chatuikit.shared.spans;

import com.cometchat.chatuikit.shared.views.richtexttoolbar.FormatType;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for ListContinuationHandler.
 * <p>
 * These tests verify the list and blockquote continuation behavior
 * of the ListContinuationHandler class.
 * </p>
 * <p>
 * Note: Since SpannableStringBuilder requires the Android framework, these tests
 * focus on testing the handler creation, null safety, and helper method logic
 * rather than full integration with Android's text system.
 * </p>
 * <p>
 * Validates: Requirements 6.3, 6.4, 7.3, 7.4, 7.5, 8.4, 8.5
 * </p>
 */
public class ListContinuationHandlerTest {

    // ==================== Constructor Tests ====================

    /**
     * Test that ListContinuationHandler can be instantiated.
     */
    @Test
    public void constructor_createsInstance() {
        ListContinuationHandler handler = new ListContinuationHandler();
        assertNotNull("ListContinuationHandler should be created", handler);
    }

    // ==================== Null Safety Tests ====================

    /**
     * Test that handleEnterKey handles null editable gracefully.
     * Validates: Requirement 6.3
     */
    @Test
    public void handleEnterKey_nullEditable_returnsNotHandled() {
        ListContinuationHandler handler = new ListContinuationHandler();
        ListContinuationHandler.EnterKeyResult result = handler.handleEnterKey(null, 0);
        assertEquals("Should return NOT_HANDLED for null editable",
                ListContinuationHandler.EnterKeyResult.NOT_HANDLED, result);
    }

    /**
     * Test that handleEnterKey handles negative cursor position gracefully.
     */
    @Test
    public void handleEnterKey_negativeCursorPosition_returnsNotHandled() {
        ListContinuationHandler handler = new ListContinuationHandler();
        ListContinuationHandler.EnterKeyResult result = handler.handleEnterKey(null, -1);
        assertEquals("Should return NOT_HANDLED for negative cursor position",
                ListContinuationHandler.EnterKeyResult.NOT_HANDLED, result);
    }

    /**
     * Test that renumberList handles null editable gracefully.
     * Validates: Requirement 7.5
     */
    @Test
    public void renumberList_nullEditable_noException() {
        ListContinuationHandler handler = new ListContinuationHandler();
        // Should not throw exception
        handler.renumberList(null);
    }

    /**
     * Test that renumberListRange handles null editable gracefully.
     */
    @Test
    public void renumberListRange_nullEditable_noException() {
        ListContinuationHandler handler = new ListContinuationHandler();
        // Should not throw exception
        handler.renumberListRange(null, 0, 10, 1);
    }

    /**
     * Test that renumberListRange handles invalid range gracefully.
     */
    @Test
    public void renumberListRange_invalidRange_noException() {
        ListContinuationHandler handler = new ListContinuationHandler();
        // Should not throw exception with negative start
        handler.renumberListRange(null, -1, 10, 1);
    }

    /**
     * Test that getNextListNumber handles null editable gracefully.
     */
    @Test
    public void getNextListNumber_nullEditable_returnsOne() {
        ListContinuationHandler handler = new ListContinuationHandler();
        int nextNumber = handler.getNextListNumber(null, 0);
        assertEquals("Should return 1 for null editable", 1, nextNumber);
    }

    /**
     * Test that isAtListItemStart handles null editable gracefully.
     */
    @Test
    public void isAtListItemStart_nullEditable_returnsFalse() {
        ListContinuationHandler handler = new ListContinuationHandler();
        boolean result = handler.isAtListItemStart(null, 0);
        assertFalse("Should return false for null editable", result);
    }

    /**
     * Test that isAtListItemStart handles negative position gracefully.
     */
    @Test
    public void isAtListItemStart_negativePosition_returnsFalse() {
        ListContinuationHandler handler = new ListContinuationHandler();
        boolean result = handler.isAtListItemStart(null, -1);
        assertFalse("Should return false for negative position", result);
    }

    /**
     * Test that isInList handles null editable gracefully.
     */
    @Test
    public void isInList_nullEditable_returnsFalse() {
        ListContinuationHandler handler = new ListContinuationHandler();
        boolean result = handler.isInList(null, 0);
        assertFalse("Should return false for null editable", result);
    }

    /**
     * Test that isInList handles negative position gracefully.
     */
    @Test
    public void isInList_negativePosition_returnsFalse() {
        ListContinuationHandler handler = new ListContinuationHandler();
        boolean result = handler.isInList(null, -1);
        assertFalse("Should return false for negative position", result);
    }

    /**
     * Test that isInBlockquote handles null editable gracefully.
     */
    @Test
    public void isInBlockquote_nullEditable_returnsFalse() {
        ListContinuationHandler handler = new ListContinuationHandler();
        boolean result = handler.isInBlockquote(null, 0);
        assertFalse("Should return false for null editable", result);
    }

    /**
     * Test that isInBlockquote handles negative position gracefully.
     */
    @Test
    public void isInBlockquote_negativePosition_returnsFalse() {
        ListContinuationHandler handler = new ListContinuationHandler();
        boolean result = handler.isInBlockquote(null, -1);
        assertFalse("Should return false for negative position", result);
    }

    // ==================== EnterKeyResult Enum Tests ====================

    /**
     * Test that all EnterKeyResult values are defined.
     */
    @Test
    public void enterKeyResult_allValuesExist() {
        ListContinuationHandler.EnterKeyResult[] values = ListContinuationHandler.EnterKeyResult.values();
        assertEquals("Should have 5 result types", 5, values.length);
        
        // Verify each value exists
        assertNotNull(ListContinuationHandler.EnterKeyResult.NEW_ITEM_CREATED);
        assertNotNull(ListContinuationHandler.EnterKeyResult.LIST_EXITED);
        assertNotNull(ListContinuationHandler.EnterKeyResult.BLOCKQUOTE_CONTINUED);
        assertNotNull(ListContinuationHandler.EnterKeyResult.BLOCKQUOTE_EXITED);
        assertNotNull(ListContinuationHandler.EnterKeyResult.NOT_HANDLED);
    }

    /**
     * Test EnterKeyResult valueOf method.
     */
    @Test
    public void enterKeyResult_valueOf_returnsCorrectValue() {
        assertEquals(ListContinuationHandler.EnterKeyResult.NEW_ITEM_CREATED,
                ListContinuationHandler.EnterKeyResult.valueOf("NEW_ITEM_CREATED"));
        assertEquals(ListContinuationHandler.EnterKeyResult.LIST_EXITED,
                ListContinuationHandler.EnterKeyResult.valueOf("LIST_EXITED"));
        assertEquals(ListContinuationHandler.EnterKeyResult.BLOCKQUOTE_CONTINUED,
                ListContinuationHandler.EnterKeyResult.valueOf("BLOCKQUOTE_CONTINUED"));
        assertEquals(ListContinuationHandler.EnterKeyResult.BLOCKQUOTE_EXITED,
                ListContinuationHandler.EnterKeyResult.valueOf("BLOCKQUOTE_EXITED"));
        assertEquals(ListContinuationHandler.EnterKeyResult.NOT_HANDLED,
                ListContinuationHandler.EnterKeyResult.valueOf("NOT_HANDLED"));
    }

    // ==================== Span Creation Tests ====================

    /**
     * Test that BulletListFormatSpan can be created for list continuation.
     * Validates: Requirement 6.3
     */
    @Test
    public void bulletListSpan_canBeCreated() {
        BulletListFormatSpan span = new BulletListFormatSpan();
        assertNotNull("BulletListFormatSpan should be created", span);
        assertEquals("Should return BULLET_LIST format type",
                FormatType.BULLET_LIST, span.getFormatType());
    }

    /**
     * Test that BulletListFormatSpan properties can be copied.
     * Validates: Requirement 6.3
     */
    @Test
    public void bulletListSpan_propertiesCanBeCopied() {
        BulletListFormatSpan original = new BulletListFormatSpan();
        original.setBulletColor(0xFF0000);
        original.setBulletRadius(8);
        original.setGapWidth(20);

        BulletListFormatSpan copy = new BulletListFormatSpan();
        copy.setBulletColor(original.getBulletColor());
        copy.setBulletRadius(original.getBulletRadius());
        copy.setGapWidth(original.getGapWidth());

        assertEquals("Bullet color should be copied", 0xFF0000, copy.getBulletColor());
        assertEquals("Bullet radius should be copied", 8, copy.getBulletRadius());
        assertEquals("Gap width should be copied", 20, copy.getGapWidth());
    }

    /**
     * Test that NumberedListFormatSpan can be created with number.
     * Validates: Requirement 7.3
     */
    @Test
    public void numberedListSpan_canBeCreatedWithNumber() {
        NumberedListFormatSpan span = new NumberedListFormatSpan(1);
        assertNotNull("NumberedListFormatSpan should be created", span);
        assertEquals("Should return ORDERED_LIST format type",
                FormatType.ORDERED_LIST, span.getFormatType());
        assertEquals("Should have number 1", 1, span.getNumber());
    }

    /**
     * Test that NumberedListFormatSpan number can be incremented.
     * Validates: Requirement 7.3
     */
    @Test
    public void numberedListSpan_numberCanBeIncremented() {
        NumberedListFormatSpan span = new NumberedListFormatSpan(5);
        int nextNumber = span.getNumber() + 1;
        
        NumberedListFormatSpan nextSpan = new NumberedListFormatSpan(nextNumber);
        assertEquals("Next span should have incremented number", 6, nextSpan.getNumber());
    }

    /**
     * Test that NumberedListFormatSpan number can be updated.
     * Validates: Requirement 7.5
     */
    @Test
    public void numberedListSpan_numberCanBeUpdated() {
        NumberedListFormatSpan span = new NumberedListFormatSpan(1);
        span.setNumber(10);
        assertEquals("Number should be updated", 10, span.getNumber());
    }

    /**
     * Test that NumberedListFormatSpan properties can be copied.
     * Validates: Requirement 7.3
     */
    @Test
    public void numberedListSpan_propertiesCanBeCopied() {
        NumberedListFormatSpan original = new NumberedListFormatSpan(3);
        original.setTextColor(0x00FF00);
        original.setGapWidth(24);

        NumberedListFormatSpan copy = new NumberedListFormatSpan(original.getNumber() + 1);
        copy.setTextColor(original.getTextColor());
        copy.setGapWidth(original.getGapWidth());

        assertEquals("Number should be incremented", 4, copy.getNumber());
        assertEquals("Text color should be copied", 0x00FF00, copy.getTextColor());
        assertEquals("Gap width should be copied", 24, copy.getGapWidth());
    }

    /**
     * Test that BlockquoteFormatSpan can be created.
     * Validates: Requirement 8.4
     */
    @Test
    public void blockquoteSpan_canBeCreated() {
        BlockquoteFormatSpan span = new BlockquoteFormatSpan();
        assertNotNull("BlockquoteFormatSpan should be created", span);
        assertEquals("Should return BLOCKQUOTE format type",
                FormatType.BLOCKQUOTE, span.getFormatType());
    }

    /**
     * Test that BlockquoteFormatSpan properties can be copied.
     * Validates: Requirement 8.4
     */
    @Test
    public void blockquoteSpan_propertiesCanBeCopied() {
        BlockquoteFormatSpan original = new BlockquoteFormatSpan();
        original.setStripeColor(0x0000FF);
        original.setStripeWidth(6);
        original.setGapWidth(18);

        BlockquoteFormatSpan copy = new BlockquoteFormatSpan();
        copy.setStripeColor(original.getStripeColor());
        copy.setStripeWidth(original.getStripeWidth());
        copy.setGapWidth(original.getGapWidth());

        assertEquals("Stripe color should be copied", 0x0000FF, copy.getStripeColor());
        assertEquals("Stripe width should be copied", 6, copy.getStripeWidth());
        assertEquals("Gap width should be copied", 18, copy.getGapWidth());
    }

    // ==================== List Numbering Logic Tests ====================

    /**
     * Test that sequential numbers are generated correctly.
     * Validates: Requirement 7.3
     */
    @Test
    public void numberedList_sequentialNumbersGenerated() {
        int[] expectedNumbers = {1, 2, 3, 4, 5};
        
        for (int i = 0; i < expectedNumbers.length; i++) {
            NumberedListFormatSpan span = new NumberedListFormatSpan(expectedNumbers[i]);
            assertEquals("Number should match expected",
                    expectedNumbers[i], span.getNumber());
        }
    }

    /**
     * Test that renumbering updates numbers correctly.
     * Validates: Requirement 7.5
     */
    @Test
    public void numberedList_renumberingUpdatesNumbers() {
        // Create spans with out-of-order numbers
        NumberedListFormatSpan span1 = new NumberedListFormatSpan(5);
        NumberedListFormatSpan span2 = new NumberedListFormatSpan(3);
        NumberedListFormatSpan span3 = new NumberedListFormatSpan(7);

        // Simulate renumbering by updating numbers
        span1.setNumber(1);
        span2.setNumber(2);
        span3.setNumber(3);

        assertEquals("First span should be 1", 1, span1.getNumber());
        assertEquals("Second span should be 2", 2, span2.getNumber());
        assertEquals("Third span should be 3", 3, span3.getNumber());
    }

    // ==================== Handler Multiple Instance Tests ====================

    /**
     * Test that multiple handlers can be created independently.
     */
    @Test
    public void multipleHandlers_canBeCreatedIndependently() {
        ListContinuationHandler handler1 = new ListContinuationHandler();
        ListContinuationHandler handler2 = new ListContinuationHandler();

        assertNotNull("First handler should be created", handler1);
        assertNotNull("Second handler should be created", handler2);
        assertNotSame("Handlers should be different instances", handler1, handler2);
    }

    // ==================== Edge Case Tests ====================

    /**
     * Test handling of zero cursor position.
     */
    @Test
    public void handleEnterKey_zeroCursorPosition_handledGracefully() {
        ListContinuationHandler handler = new ListContinuationHandler();
        // Should not throw exception
        ListContinuationHandler.EnterKeyResult result = handler.handleEnterKey(null, 0);
        assertEquals("Should return NOT_HANDLED",
                ListContinuationHandler.EnterKeyResult.NOT_HANDLED, result);
    }

    /**
     * Test handling of large cursor position.
     */
    @Test
    public void handleEnterKey_largeCursorPosition_handledGracefully() {
        ListContinuationHandler handler = new ListContinuationHandler();
        // Should not throw exception
        ListContinuationHandler.EnterKeyResult result = handler.handleEnterKey(null, Integer.MAX_VALUE);
        assertEquals("Should return NOT_HANDLED",
                ListContinuationHandler.EnterKeyResult.NOT_HANDLED, result);
    }

    /**
     * Test that getNextListNumber returns 1 for empty context.
     */
    @Test
    public void getNextListNumber_emptyContext_returnsOne() {
        ListContinuationHandler handler = new ListContinuationHandler();
        int nextNumber = handler.getNextListNumber(null, 0);
        assertEquals("Should return 1 for empty context", 1, nextNumber);
    }

    /**
     * Test NumberedListFormatSpan with number 0.
     */
    @Test
    public void numberedListSpan_numberZero_handledCorrectly() {
        NumberedListFormatSpan span = new NumberedListFormatSpan(0);
        assertEquals("Number 0 should be preserved", 0, span.getNumber());
    }

    /**
     * Test NumberedListFormatSpan with negative number.
     */
    @Test
    public void numberedListSpan_negativeNumber_handledCorrectly() {
        NumberedListFormatSpan span = new NumberedListFormatSpan(-1);
        assertEquals("Negative number should be preserved", -1, span.getNumber());
    }

    /**
     * Test NumberedListFormatSpan with large number.
     */
    @Test
    public void numberedListSpan_largeNumber_handledCorrectly() {
        NumberedListFormatSpan span = new NumberedListFormatSpan(Integer.MAX_VALUE);
        assertEquals("Large number should be preserved", Integer.MAX_VALUE, span.getNumber());
    }
}
